# EC2へのデプロイ手順（systemd）

EC2（Amazon Linux 2023）上で vehicle-verification-api を systemd のサービスとして常駐させる手順です。
OS起動時の自動起動、異常終了時の自動再起動、journald へのログ出力までを扱います。

> コマンドはすべて1行ずつ、上から順にそのまま実行できる形で書いています。

## 前提

| 項目 | 内容 |
|---|---|
| OS | Amazon Linux 2023 |
| DB | Amazon RDS（MySQL 8.0）。EC2 のセキュリティグループから 3306 番に接続できること |
| 添付ファイル | S3（`prod` プロファイル） |
| S3 の認証 | EC2 に IAM ロール（インスタンスプロファイル）を付ける。アクセスキーは使わない |
| ビルド | EC2 上でリポジトリを clone し、`./gradlew bootJar` で jar を作る |

## 配置先

| パス | 内容 | 所有者・権限 |
|---|---|---|
| `~/vehicle-verification-api/` | リポジトリの clone（ビルド用） | ec2-user |
| `/opt/vehicle-verification-api/app.jar` | 実行する jar | root:root 644 |
| `/etc/vehicle-verification/app.env` | 環境変数（秘密情報） | root:root 600 |
| `/etc/systemd/system/vehicle-verification-api.service` | unit ファイル | root:root 644 |

アプリは専用のシステムユーザー `vvapi` で実行します。

> アプリの起動・停止は **systemctl だけで行ってください**。手で `java -jar` や `./gradlew bootRun` を実行すると、systemd 側と 8080 番を取り合って起動に失敗します。

## 1. 初回セットアップ

### 1-1. Java と git のインストール

EC2 上でビルドするため、JRE ではなく JDK を入れます。

```bash
sudo dnf install -y java-21-amazon-corretto-devel git
java -version
```

### 1-2. 実行ユーザーの作成

ログインできないシステムユーザーを作ります。

```bash
sudo useradd --system --no-create-home --shell /sbin/nologin vvapi
```

### 1-3. ディレクトリの作成

```bash
sudo mkdir -p /opt/vehicle-verification-api /etc/vehicle-verification
```

### 1-4. リポジトリの clone とビルド

```bash
cd ~
git clone <リポジトリのURL> vehicle-verification-api
cd vehicle-verification-api
./gradlew bootJar
```

> メモリの小さいインスタンス（t3.micro など）では、ビルド中にメモリ不足で落ちることがあります。
> その場合は swap を追加してください。
>
> ```bash
> sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
> sudo chmod 600 /swapfile
> sudo mkswap /swapfile
> sudo swapon /swapfile
> ```

### 1-5. RDS にデータベースとテーブルを作成

`ddl-auto: validate` のため、アプリはテーブルを作りません。起動する前に RDS 側を用意します。
エンドポイントは RDS コンソールの「接続とセキュリティ」タブ、マスターユーザー名は「設定」タブで確認できます。

```bash
sudo dnf install -y mariadb105
mysql -h <RDSのエンドポイント> -u <マスターユーザー名> -p -e "CREATE DATABASE IF NOT EXISTS verification_db"
mysql -h <RDSのエンドポイント> -u <マスターユーザー名> -p verification_db < ~/vehicle-verification-api/sql/init.sql
```

`sql/init.sql` には `CREATE DATABASE` も `USE` も書かれていないので、データベース名を引数で指定します。

### 1-6. jar の配置

`build/libs/` には `-plain` が付いた jar もできますが、それは依存ライブラリを含まないので使いません。

```bash
sudo install -o root -g root -m 644 "$(ls build/libs/*.jar | grep -v plain)" /opt/vehicle-verification-api/app.jar
ls -l /opt/vehicle-verification-api/
```

`app.jar` が `-rw-r--r-- 1 root root` で表示されれば OK です。
ここで jar を置かずに 1-8 へ進むと、`Unable to access jarfile` で起動に失敗します。

### 1-7. 環境変数ファイルの配置

サンプルをコピーして、値を書き換えます。

```bash
sudo install -o root -g root -m 600 deploy/app.env.example /etc/vehicle-verification/app.env
sudo vi /etc/vehicle-verification/app.env
```

| 変数 | 入れる値 |
|---|---|
| `SPRING_DATASOURCE_URL` | `change-me-rds-endpoint...` の部分を RDS のエンドポイントに置き換える |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | RDS の MySQL ユーザー（マスターユーザー、またはアプリ専用に作ったユーザー）。アプリのログインユーザーとは別物 |
| `JWT_SECRET` | 別のターミナルで `openssl rand -base64 48` を実行し、出力された文字列を引用符なしで貼り付ける（コマンドを書いても実行されない） |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | アプリの初期管理者。20文字以内。`admin` のままにしない（起動時に警告が出る） |
| `S3_BUCKET_NAME` | バケット名だけ（ARN ではない） |

> **初期管理者が作られるのは、users テーブルが空のとき（最初の起動時）だけです。**
> すでにユーザーがいる DB では `ADMIN_USERNAME` / `ADMIN_PASSWORD` は使われず、既存の管理者のパスワードがそのまま残ります。

> vi の操作: 開いた直後はコマンドモードです。`i` で入力モードに入り、`Esc` でコマンドモードに戻り、`:wq` で保存して終了します。
> 保存せずに抜けるときは `:q!` です（`Esc` の隣の `F1` を押すとヘルプが開くので、そのときは `:q` でヘルプだけ閉じます）。

保存したら、ダミー値の残りと権限を確認します。

```bash
sudo grep -n "change-me" /etc/vehicle-verification/app.env
sudo ls -l /etc/vehicle-verification/app.env
```

1つ目は何も表示されなければ OK、2つ目は `-rw------- 1 root root` なら OK です。
systemd が root 権限で読み込むため、`vvapi` に読み取り権限を付ける必要はありません。

### 1-8. unit ファイルの登録と起動

```bash
sudo install -o root -g root -m 644 deploy/systemd/vehicle-verification-api.service /etc/systemd/system/vehicle-verification-api.service
sudo systemctl daemon-reload
sudo systemctl enable --now vehicle-verification-api
journalctl -u vehicle-verification-api -f
```

`enable` は OS 起動時の自動起動を有効にし、`--now` を付けるとその場で起動もします。
ログに `Started VehicleVerificationApplication in ... seconds` が出れば起動成功です（`Ctrl+C` で抜けてもアプリは止まりません）。

## 2. 更新手順

```bash
cd ~/vehicle-verification-api
git pull
./gradlew bootJar
sudo install -o root -g root -m 644 "$(ls build/libs/*.jar | grep -v plain)" /opt/vehicle-verification-api/app.jar
sudo systemctl restart vehicle-verification-api
journalctl -u vehicle-verification-api -f
```

unit ファイルを変更したときは、コピーし直したあとに `sudo systemctl daemon-reload` を実行してから再起動します。
`app.env` だけを変更したときは `restart` だけで反映されます。

## 3. よく使うコマンド

| やりたいこと | コマンド |
|---|---|
| 状態を確認する | `systemctl status vehicle-verification-api` |
| 起動・停止・再起動 | `sudo systemctl start` / `stop` / `restart vehicle-verification-api` |
| 自動起動を有効・無効にする | `sudo systemctl enable` / `disable vehicle-verification-api` |
| ログをリアルタイムで見る | `journalctl -u vehicle-verification-api -f` |
| 今回の OS 起動以降のログを見る | `journalctl -u vehicle-verification-api -b` |
| 直近のログを見る | `journalctl -u vehicle-verification-api -n 200 --no-pager` |

## 4. 動作確認

### 4-1. 起動していること

```bash
systemctl is-active vehicle-verification-api
sudo ss -ltnp | grep 8080
```

1つ目が `active`、2つ目の `pid=` が `systemctl status` の Main PID と一致すれば OK です（一致しない場合は、手動で起動したプロセスが 8080 番を使っています）。

### 4-2. OS 起動時に自動起動すること

```bash
sudo reboot
```

1〜2分待って SSH で再接続してから、次を実行します。reboot ではパブリック IP は変わりません。

```bash
systemctl is-active vehicle-verification-api
journalctl -u vehicle-verification-api -b | grep "Started VehicleVerification"
```

### 4-3. 異常終了したときに自動再起動すること

```bash
systemctl status vehicle-verification-api --no-pager | grep "Main PID"
sudo systemctl kill -s KILL vehicle-verification-api
```

20〜30秒（RestartSec の10秒＋起動の約15秒）待ってから確認します。

```bash
systemctl status vehicle-verification-api --no-pager | grep -E "Active|Main PID"
```

`active (running)` に戻り、Main PID が kill 前と変わっていれば OK です。
なお、`systemctl stop` で止めた場合は正常停止なので再起動しません。

## トラブルシューティング

起動に失敗したときは、まず原因の例外を探します。例外が何重にも連鎖しているときは、**最後のほうの `Caused by:` が根本原因**です。

```bash
journalctl -u vehicle-verification-api -n 300 --no-pager | grep -n -A 15 "APPLICATION FAILED TO START\|Caused by\|ERROR"
```

| 症状（ログの文言） | 原因と対処 |
|---|---|
| Spring のバナーが何度も流れる / `status` が `activating (auto-restart)` | 起動途中で落ちて再起動を繰り返している。上のコマンドで原因を確認する |
| `Unable to access jarfile` | `/opt/vehicle-verification-api/app.jar` がない。1-6 を実行する |
| `UnknownHostException` | `SPRING_DATASOURCE_URL` のホスト名が間違っている（ダミー値のまま等） |
| `Communications link failure` / `Connection timed out` | RDS のセキュリティグループで EC2 からの 3306 番が許可されているか |
| `Access denied for user` | `SPRING_DATASOURCE_USERNAME` / `PASSWORD` が間違っている |
| `Unknown database` / `Schema-validation: missing table` | RDS にデータベースやテーブルがない。1-5 を実行する。定義がずれている場合は `sql/init.sql` と一致しているか |
| `Port 8080 was already in use` | 手動で起動したプロセスが残っている。`sudo ss -ltnp \| grep 8080` で PID を調べ、`kill <PID>` で止めてから `sudo systemctl restart vehicle-verification-api` |
| `WeakKeyException` | `JWT_SECRET` が32バイト未満 |
| S3 へのアップロードで 403 | EC2 に IAM ロールが付いているか、そのロールに対象バケットへの権限があるか |
| app.env の `ADMIN_PASSWORD` でログインできない | users テーブルに既存ユーザーがいて、初期管理者が作られていない。既存の管理者のパスワードを使う |
