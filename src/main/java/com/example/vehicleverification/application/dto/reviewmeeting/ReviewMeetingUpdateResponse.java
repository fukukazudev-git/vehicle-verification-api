package com.example.vehicleverification.application.dto.reviewmeeting;

import com.example.vehicleverification.domain.entity.ReviewMeetingStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReviewMeetingUpdateResponse {

    private Long id;
    private String title;
    private LocalDate scheduledDate;
    private ReviewMeetingStatus status;
    private String notes;
    private Long modelId;
    private String modelName;
    private Long organizerId;
    private String organizerName;
    private Long version;
    private String eventCode;
}
