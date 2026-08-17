package com.example.vehicleverification.application.dto.reviewmeeting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReviewMeetingCreateResponse {

    private Long id;
    private String title;
    private LocalDate scheduledDate;
    private String status;
    private Long modelId;
    private String modelName;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime createdAt;
    private String eventCode;
}
