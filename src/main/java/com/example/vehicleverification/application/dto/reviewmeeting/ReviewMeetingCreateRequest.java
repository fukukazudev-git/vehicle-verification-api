package com.example.vehicleverification.application.dto.reviewmeeting;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewMeetingCreateRequest {

    @NotNull
    private Long modelId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    private LocalDate scheduledDate;

    @NotBlank
    private String status;

    @NotNull
    private Long organizerId;

    private String notes;

    public ReviewMeetingCreateRequest(Long modelId, String title, LocalDate scheduledDate, String status,
            Long organizerId, String notes) {
        this.modelId = modelId;
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.organizerId = organizerId;
        this.notes = notes;
    }

}
