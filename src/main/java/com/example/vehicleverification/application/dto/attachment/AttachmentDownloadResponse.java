package com.example.vehicleverification.application.dto.attachment;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AttachmentDownloadResponse {
    private String fileName;
    private String downloadUrl;
    private LocalDateTime expiresAt;
}
