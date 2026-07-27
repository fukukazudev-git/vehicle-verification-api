package com.example.vehicleverification.application.dto.reviewmeeting;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

//詳細用
@Getter
@Setter
public class ReviewMeetingDetailResponse {

    private Long id;
    private String title;
    private LocalDate scheduleDate;
    private String status;
    private String notes;
    private Long modelId;
    private String modelName;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime createdAt;

    public ReviewMeetingDetailResponse(Long id, String title, LocalDate scheduleDate, String status, String notes,
            Long modelId, String modelName, Long organizerId, String organizerName, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.scheduleDate = scheduleDate;
        this.status = status;
        this.notes = notes;
        this.modelId = modelId;
        this.modelName = modelName;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.createdAt = createdAt;
    }

}
