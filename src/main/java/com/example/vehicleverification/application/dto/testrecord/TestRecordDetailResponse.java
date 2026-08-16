package com.example.vehicleverification.application.dto.testrecord;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TestRecordDetailResponse {

    private Long id;
    private String testName;
    private String result;
    private String notes;
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime recordedAt;
    private Long version;

    public TestRecordDetailResponse(Long id, String testName, String result, String notes, Long reviewMeetingId,
            String reviewMeetingTitle, Long recordedById, String recordedByName, LocalDateTime recordedAt,
            Long version) {
        this.id = id;
        this.testName = testName;
        this.result = result;
        this.notes = notes;
        this.reviewMeetingId = reviewMeetingId;
        this.reviewMeetingTitle = reviewMeetingTitle;
        this.recordedById = recordedById;
        this.recordedByName = recordedByName;
        this.recordedAt = recordedAt;
        this.version = version;
    }

}
