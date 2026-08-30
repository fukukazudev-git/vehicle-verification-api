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
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "issues")
@Getter
@Setter
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_meeting_id")
    private ReviewMeeting reviewMeeting;

    @NotNull
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerer_id")
    private User answerer;

    // LocalDateTimeだとHibernateはTIMESTAMP相当を期待し、validateで型不一致になるためLocalDateを使用
    private LocalDate resolvedAt;

    @NotNull
    @Size(max = 20)
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    protected Issue() {}

    public Issue(ReviewMeeting reviewMeeting, String content, User reporter, String status) {
        this.reviewMeeting = reviewMeeting;
        this.content = content;
        this.reporter = reporter;
        this.status = status;
    }
}
