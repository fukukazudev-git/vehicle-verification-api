package com.example.vehicleverification.domain.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "test_records")
@Getter
@Setter
public class TestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_meeting_id")
    private ReviewMeeting reviewMeeting;

    @Size(max = 100)
    @NotNull
    private String testName;

    @Size(max = 20)
    @NotNull
    private String result;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime recordedAt;

    @Version
    private Long version;

    protected TestRecord() {
        // JPAの仕様上、エンティティクラスには引数なしのコンストラクタが必要
    }

    public TestRecord(ReviewMeeting reviewMeeting, String testName, String result, String notes, User recordedBy) {
        this.reviewMeeting = reviewMeeting;
        this.testName = testName;
        this.result = result;
        this.notes = notes;
        this.recordedBy = recordedBy;
    }

}
