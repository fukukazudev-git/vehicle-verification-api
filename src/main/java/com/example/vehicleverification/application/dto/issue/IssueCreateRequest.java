package com.example.vehicleverification.application.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueCreateRequest {
    @NotNull
    private Long reviewMeetingId;

    @NotBlank
    private String content;

    @NotNull
    private Long reporterId;
}
