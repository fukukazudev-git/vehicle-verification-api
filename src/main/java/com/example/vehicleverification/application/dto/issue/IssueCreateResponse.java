package com.example.vehicleverification.application.dto.issue;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IssueCreateResponse {
    private Long id;
    private String status;
    private String content;
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime createdAt;
}
