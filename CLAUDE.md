# アーキテクチャルール

## レイヤー構造
- Controller: リクエスト受付・レスポンス返却のみ。ビジネスロジック禁止
- Service: ビジネスロジックはここに書く。インターフェースとImplに分ける
- Repository: JpaRepositoryを継承したインターフェースのみ
- Entity: DBテーブルとのマッピングのみ。ロジック禁止
- Infrastructure: フレームワーク・外部連携の設定（Spring Securityなどの@Configuration）を置く

## 必須ルール
- バリデーションは@ValidとBean Validationアノテーションで行う
- 例外処理は@ControllerAdviceで一元管理する
- Controllerに@Autowiredは使わない（コンストラクタインジェクション）
- ServiceImplに@Transactionalをつける

## パッケージ構成
com.example.vehicleverification
├── domain/entity/
├── domain/repository/
├── application/service/
├── presentation/controller/
├── dto/
└── infrastructure/config/
