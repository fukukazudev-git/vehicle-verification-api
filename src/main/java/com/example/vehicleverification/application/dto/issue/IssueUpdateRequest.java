package com.example.vehicleverification.application.dto.issue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueUpdateRequest {
    // 部分更新（PATCH）: nullの項目は更新対象外（据え置き）
    private String answer;

    private Long answererId;

    private LocalDate resolvedAt;

    // 送られた場合のみ更新するため@NotBlankは付けない。値がある場合の長さのみEntityに合わせて検証
    @Size(max = 20)
    private String status;

    @NotNull
    private Long version;
}
