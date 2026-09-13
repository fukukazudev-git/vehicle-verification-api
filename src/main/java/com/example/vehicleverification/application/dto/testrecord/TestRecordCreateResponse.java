package com.example.vehicleverification.application.dto.testrecord;

import com.example.vehicleverification.domain.entity.TestResult;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TestRecordCreateResponse {

    private Long id;
    private String testName;
    private TestResult result;
    private String notes;
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime recordedAt;
}
