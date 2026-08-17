package com.example.vehicleverification.application.dto.testrecord;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TestRecordUpdateResponse {

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
}
