package com.example.vehicleverification.application.dto.reviewmeeting;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

// 一覧用
@Getter
@Setter
public class ReviewMeetingDto {

    private Long id;
    private String title;
    private LocalDate scheduledDate;
    private String status;
    private String modelId;
    private String modelName;
    private String organizerId;
    private String organizerName;
    private LocalDateTime createdAt;

    public ReviewMeetingDto(Long id, String title, LocalDate scheduledDate, String status, String modelId,
            String modelName, String organizerId, String organizerName, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.modelId = modelId;
        this.modelName = modelName;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.createdAt = createdAt;
    }

}
