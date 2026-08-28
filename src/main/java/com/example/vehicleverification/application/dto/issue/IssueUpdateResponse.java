package com.example.vehicleverification.application.dto.issue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IssueUpdateResponse {
    private Long id;
    private String status;
    private String content;
    private String answer;
    private LocalDate resolvedAt;
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private Long reporterId;
    private String reporterName;
    private Long answererId;
    private String answererName;
    private LocalDateTime createdAt;
    private Long version;
}
