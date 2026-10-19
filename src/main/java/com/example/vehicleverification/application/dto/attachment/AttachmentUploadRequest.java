package com.example.vehicleverification.application.dto.attachment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// multipart/form-data のフォーム項目をまとめて受け取る（@ModelAttribute でバインド）
// ファイルもこのDTOに含めることで、OpenAPI(Swagger)のスキーマにファイル選択欄が出る
// ファイル形式はファイル名の拡張子からサーバー側で判定するため、リクエストでは受け取らない
// Swagger UIの初期値はOpenApiConfigで空にしている
@Getter
@Setter
public class AttachmentUploadRequest {
    // 紐づけ先は任意(null可)。存在しないIDはService側の存在チェックで404になる
    // @Positiveは付けない: Swagger UIが最小値の1を初期表示し、消し忘れると黙ってID=1に紐づくため
    private Long reviewMeetingId;

    private Long testRecordId;

    private Long userId;

    @NotNull(message = "ファイルは必須です")
    private MultipartFile file;
}
