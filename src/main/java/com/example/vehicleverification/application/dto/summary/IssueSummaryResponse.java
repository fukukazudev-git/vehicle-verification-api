package com.example.vehicleverification.application.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssueSummaryResponse {
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private long totalCount;
    private long unresolvedCount;
    private long inProgressCount;
    private long pendingApprovalCount;
    private long resolvedCount;
    private boolean allResolved;
}
