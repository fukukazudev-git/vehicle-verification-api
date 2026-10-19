package com.example.vehicleverification.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Verification API")
                        .description("検証会・テスト記録管理システムのREST API")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWTトークンを入力してください")));
    }

    // Swagger UIはinteger項目の初期値にサンプル値0を入れるため、そのまま送ると存在しないIDとして404になる。
    // 任意の紐づけ先IDに空文字のexampleを明示し、フォームを空欄で表示させる
    // （example: null はSwagger UIが文字列"null"として入力欄に入れてしまうため使えない）
    // プロパティ名は AttachmentUploadRequest のフィールド名と対応
    @Bean
    public OpenApiCustomizer attachmentUploadExampleCustomizer() {
        return api -> {
            if (api.getComponents() == null || api.getComponents().getSchemas() == null) {
                return;
            }
            Schema<?> schema = api.getComponents().getSchemas().get("AttachmentUploadRequest");
            if (schema == null || schema.getProperties() == null) {
                return;
            }
            for (String name : List.of("reviewMeetingId", "testRecordId", "userId")) {
                Schema<?> property = schema.getProperties().get(name);
                if (property != null) {
                    property.setExample("");
                }
            }
        };
    }
}
