package com.example.vehicleverification.application.dto.reviewmeeting;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewMeetingUpdateResponse {

    private Long id;
    private String title;
    private LocalDate scheduledDate;
    private String status;
    private String notes;
    private Long modelId;
    private String modelName;
    private Long organizerId;
    private String organizerName;
    private Long version;
    private String eventCode;

    public ReviewMeetingUpdateResponse(
            Long id,
            String title,
            LocalDate scheduledDate,
            String status,
            String notes,
            Long modelId,
            String modelName,
            Long organizerId,
            String organizerName,
            Long version,
            String eventCode) {
        this.id = id;
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.notes = notes;
        this.modelId = modelId;
        this.modelName = modelName;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.version = version;
        this.eventCode = eventCode;
    }
}
