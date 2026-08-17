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
@Table(name = "review_meetings")
@Getter
@Setter
public class ReviewMeeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 複数のReviewMeetingが1つのModelに紐づく
    // FetchType.LAZY 関連エンティティを必要になるまで取得しない設定
    // ManyToOneのデフォルトはEAGERだが、その設定だとReviewMeetingを1件取得するたびにModelとUserが芋づる式にJOINされて、
    // N+1問題やパフォーマンスの低下を招く可能性があるため、LAZYに設定するのが実務上のベストプラクティス
    @ManyToOne(fetch = FetchType.LAZY)
    // 実際のDB上の外部キーカラム名を指定する
    @JoinColumn(name = "model_id")
    private Model model;

    @NotNull
    @Size(max = 200)
    private String title;

    @NotNull
    private LocalDate scheduledDate;

    @NotNull
    @Size(max = 20)
    private String status;

    // DB側の設定で organizer_idカラムはusersテーブルを参照する外部キー制約が設定されているため、
    // usersテーブルのidカラムを参照することが可能。
    // user.getUsername()のようにオブジェクトの関連をたどるとJPA/HibernateがSQLを発行する。
    // → SELECT * FROM users WHERE id = ? のtupleがUserオブジェクトとして返される
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    private String notes;

    @NotNull
    @Size(max = 20)
    private String eventCode;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    protected ReviewMeeting() {}

    public ReviewMeeting(
            Model model,
            String title,
            LocalDate scheduledDate,
            String status,
            User organizer,
            String notes,
            String eventCode) {
        this.model = model;
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.organizer = organizer;
        this.notes = notes;
        this.eventCode = eventCode;
    }
}
