package com.example.vehicleverification.application.dto.reviewmeeting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 一覧用
@Getter
@Setter
@AllArgsConstructor
public class ReviewMeetingDto {

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
