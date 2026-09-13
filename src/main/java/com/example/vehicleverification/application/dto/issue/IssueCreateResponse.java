package com.example.vehicleverification.application.dto.issue;

import com.example.vehicleverification.domain.entity.IssueStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IssueCreateResponse {
    private Long id;
    private IssueStatus status;
    private String content;
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime createdAt;
}
