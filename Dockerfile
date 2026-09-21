# ビルドステージ
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# 実行ステージ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# セキュリティのためrootで動かさない
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app/uploads
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

# アップロードディレクトリの作成
# ローカルプロファイルでファイルを保存するために必要
VOLUME /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]