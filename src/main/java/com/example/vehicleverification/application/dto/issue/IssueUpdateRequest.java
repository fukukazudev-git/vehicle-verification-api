package com.example.vehicleverification.application.dto.issue;

import com.example.vehicleverification.domain.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;
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

    // 送られた場合のみ更新（null許容）。値の妥当性はEnum型で担保される
    private IssueStatus status;

    @NotNull
    private Long version;
}
