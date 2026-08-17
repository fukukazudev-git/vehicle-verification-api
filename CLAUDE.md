# アーキテクチャルール

## レイヤー構造
- Controller: リクエスト受付・レスポンス返却のみ。ビジネスロジック禁止
- Service: ビジネスロジックはここに書く。インターフェースとImplに分ける
- Repository: JpaRepositoryを継承したインターフェースのみ
- Entity: DBテーブルとのマッピングのみ。ロジック禁止
- Infrastructure: フレームワーク・外部連携の設定（Spring Securityなどの@Configuration）を置く
- DTO: 置き場は使う人で決める。ControllerとServiceの境界をまたぐものはapplication/dto/、HTTPの表示専用のものはpresentation/dto/へ（依存はpresentation→application→domainの内向きに揃える）

## 必須ルール
- バリデーションは@ValidとBean Validationアノテーションで行う
- 例外処理は@ControllerAdviceで一元管理する
- Controllerに@Autowiredは使わない（コンストラクタインジェクション）
- ServiceImplに@Transactionalをつける

## パッケージ構成
com.example.vehicleverification
├── domain/entity/
├── domain/repository/
├── domain/exception/
├── application/service/
├── application/dto/          … ControllerとServiceの境界をまたぐDTO
├── presentation/controller/
├── presentation/advice/
├── presentation/dto/error/   … HTTPのエラー表現専用DTO
├── infrastructure/config/    … @Configuration・@ConfigurationProperties等の設定
└── infrastructure/security/  … 認証部品（JWTのProvider/Filter/UserDetailsService等）

## DTOのコンストラクタ方針
- Response/Dto系（こちらが `new` して返すもの）: 原則 `@AllArgsConstructor`
- Request系（Jacksonがデシリアライズするもの）: `@Getter @Setter` ＋ 引数なしコンストラクタ（＝独自コンストラクタを書かない）
- 一部フィールドだけ埋めたい・同型フィールドが多く順序ミスが怖い場合: `@Builder`
- 手書きコンストラクタはLombokで表現できない場合のみ（例: `ErrorResponse` の `timestamp` 内部生成）
- `@AllArgsConstructor` はフィールド宣言順で生成される点に注意（フィールド順とコンストラクタ順を一致させる）

## コミット規約
- タイトル・本文とも日本語。タイトルは簡潔に

## コード整形（Spotless）
- 整形の正解は Spotless(palantir-java-format)。`./gradlew spotlessApply` で整形、`spotlessCheck` は `build` で自動実行
- コミット時に pre-commit フックが自動整形する。クローン後に一度だけ有効化: `git config core.hooksPath .githooks`
- VS Codeの保存時整形(Java)はOFF推奨（Spotlessと競合させない）
