package com.example.vehicleverification.application.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TestSummaryResponse {
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private long totalCount;
    private long okCount;
    private long ngCount;
    private long pendingCount;
    private double okRate;
}
