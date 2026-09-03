package com.example.vehicleverification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "attachments")
@Getter
@Setter
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_meeting_id")
    private ReviewMeeting reviewMeeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_record_id")
    private TestRecord testRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Size(max = 255)
    private String fileName;

    @NotNull
    @Size(max = 500)
    private String storedPath;

    @NotNull
    @Size(max = 50)
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    @NotNull
    private User uploadedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    protected Attachment() {}

    public Attachment(
            ReviewMeeting reviewMeeting,
            TestRecord testRecord,
            User user,
            String fileName,
            String storedPath,
            String fileType,
            User uploadedBy) {
        this.reviewMeeting = reviewMeeting;
        this.testRecord = testRecord;
        this.user = user;
        this.fileName = fileName;
        this.storedPath = storedPath;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
    }
}
