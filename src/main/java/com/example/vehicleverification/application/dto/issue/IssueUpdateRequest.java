package com.example.vehicleverification.application.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueUpdateRequest {
    @NotBlank
    private String content;

    private String answer;

    private Long answererId;

    private LocalDate resolvedAt;

    @NotBlank
    private String status;

    @NotNull
    private Long version;
}
