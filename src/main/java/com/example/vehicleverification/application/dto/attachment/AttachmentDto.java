package com.example.vehicleverification.application.dto.attachment;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 一覧・詳細共通
@Getter
@Setter
@AllArgsConstructor
public class AttachmentDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String storedPath;
    private Long reviewMeetingId;
    private Long testRecordId;
    private Long userId;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}
