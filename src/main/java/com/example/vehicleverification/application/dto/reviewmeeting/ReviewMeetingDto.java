package com.example.vehicleverification.application.dto.reviewmeeting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

// 一覧用
@Getter
@Setter
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

    public ReviewMeetingDto(
            Long id,
            String title,
            LocalDate scheduledDate,
            String status,
            Long modelId,
            String modelName,
            Long organizerId,
            String organizerName,
            LocalDateTime createdAt,
            String eventCode) {
        this.id = id;
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.modelId = modelId;
        this.modelName = modelName;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.createdAt = createdAt;
        this.eventCode = eventCode;
    }
}
