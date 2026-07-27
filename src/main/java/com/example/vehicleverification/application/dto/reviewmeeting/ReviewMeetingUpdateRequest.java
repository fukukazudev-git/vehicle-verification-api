package com.example.vehicleverification.application.dto.reviewmeeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewMeetingUpdateRequest {

    @NotNull
    @Size(max = 200)
    private String title;

    @NotNull
    private LocalDate scheduledDate;

    @NotBlank
    private String status;

    @NotNull
    private Long organizerId;

    private String notes;

    @NotNull
    private Long version;

    public ReviewMeetingUpdateRequest(String title, LocalDate scheduledDate, String status, Long organizerId,
            String notes, Long version) {
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.organizerId = organizerId;
        this.notes = notes;
        this.version = version;
    }

}
