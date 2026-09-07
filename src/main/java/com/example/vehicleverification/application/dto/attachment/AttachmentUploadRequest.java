package com.example.vehicleverification.application.dto.attachment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentUploadRequest {
    private Long reviewMeetingId;

    private Long testRecordId;

    private Long userId;

    @NotBlank
    private String fileType;
}
