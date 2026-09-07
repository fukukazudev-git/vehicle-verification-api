package com.example.vehicleverification.application.dto.testrecord;

import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TestRecordDetailResponse {

    private Long id;
    private String testName;
    private String result;
    private String notes;
    private Long reviewMeetingId;
    private String reviewMeetingTitle;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime recordedAt;
    private Long version;
    private List<AttachmentDto> attachments;
}
