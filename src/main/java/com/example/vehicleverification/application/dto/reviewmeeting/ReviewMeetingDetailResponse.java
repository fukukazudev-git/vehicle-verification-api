package com.example.vehicleverification.application.dto.reviewmeeting;

import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReviewMeetingDetailResponse {

    private Long id;
    private String title;
    private LocalDate scheduledDate;
    private String status;
    private String notes;
    private Long modelId;
    private String modelName;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime createdAt;
    private Long version;
    private String eventCode;
    private List<AttachmentDto> attachments;
}
