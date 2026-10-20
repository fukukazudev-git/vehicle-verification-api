# EC2へのデプロイ手順（systemd）

EC2（Amazon Linux 2023）上で vehicle-verification-api を systemd のサービスとして常駐させる手順です。
OS起動時の自動起動、異常終了時の自動再起動、journald へのログ出力までを扱います。

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

### 1-5. jar の配置

`build/libs/` には `-plain` が付いた jar もできますが、それは依存ライブラリを含まないので使いません。

```bash
sudo install -o root -g root -m 644 \
  "$(ls build/libs/*.jar | grep -v plain)" \
  /opt/vehicle-verification-api/app.jar
```

### 1-6. 環境変数ファイルの配置

サンプルをコピーして、値を書き換えます。

```bash
sudo install -o root -g root -m 600 deploy/app.env.example /etc/vehicle-verification/app.env
sudo vi /etc/vehicle-verification/app.env
```

- `JWT_SECRET` は `openssl rand -base64 48` などで生成した値にします
- `ADMIN_PASSWORD` はデフォルトの `admin` のままにしないでください（起動時に警告が出ます）
- systemd が root 権限で読み込むため、`vvapi` に読み取り権限を付ける必要はありません

### 1-7. unit ファイルの登録と起動

```bash
sudo install -o root -g root -m 644 \
  deploy/systemd/vehicle-verification-api.service \
  /etc/systemd/system/vehicle-verification-api.service
sudo systemctl daemon-reload
sudo systemctl enable --now vehicle-verification-api
```

`enable` は OS 起動時の自動起動を有効にし、`--now` を付けるとその場で起動もします。

## 2. 更新手順

```bash
cd ~/vehicle-verification-api
git pull
./gradlew bootJar
sudo install -o root -g root -m 644 \
  "$(ls build/libs/*.jar | grep -v plain)" \
  /opt/vehicle-verification-api/app.jar
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
systemctl is-active vehicle-verification-api   # active と表示される
sudo ss -ltnp | grep 8080                      # java が 8080 番で待ち受けている
```

### 4-2. OS 起動時に自動起動すること

```bash
sudo reboot
# 再接続してから
systemctl is-active vehicle-verification-api
journalctl -u vehicle-verification-api -b | grep "Started"
```

### 4-3. 異常終了したときに自動再起動すること

```bash
sudo systemctl kill -s KILL vehicle-verification-api
# 10秒（RestartSec）ほど待ってから
systemctl status vehicle-verification-api
```

`status` の出力で、プロセスの PID が変わり、`active (running)` に戻っていることを確認します。
なお、`systemctl stop` で止めた場合は正常停止なので再起動しません。

## トラブルシューティング

| 症状 | 確認すること |
|---|---|
| `status` が `activating (auto-restart)` を繰り返す | `journalctl -u vehicle-verification-api -n 200` で起動時の例外を確認する |
| DB に接続できない | RDS のセキュリティグループで EC2 からの 3306 番が許可されているか、`SPRING_DATASOURCE_URL` が正しいか |
| S3 へのアップロードで 403 | EC2 に IAM ロールが付いているか、そのロールに対象バケットへの権限があるか |
| `Schema-validation` で起動に失敗する | `ddl-auto: validate` なので、RDS のテーブル定義が `sql/init.sql` と一致しているか |
