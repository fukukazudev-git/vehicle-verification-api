package com.example.vehicleverification.application.dto.testrecord;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestRecordDto {

    private Long id;
    private String testName;
    private String result;
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime recordedAt;

    public TestRecordDto(
            Long id,
            String testName,
            String result,
            Long reviewMeetingId,
            String reviewMeetingTitle,
            Long recordedById,
            String recordedByName,
            LocalDateTime recordedAt) {
        this.id = id;
        this.testName = testName;
        this.result = result;
        this.reviewMeetingId = reviewMeetingId;
        this.reviewMeetingTitle = reviewMeetingTitle;
        this.recordedById = recordedById;
        this.recordedByName = recordedByName;
        this.recordedAt = recordedAt;
    }
}
