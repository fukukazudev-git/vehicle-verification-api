package com.example.vehicleverification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

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
    private String status = "予定";

    // DB側の設定で organizer_idカラムはusersテーブルを参照する外部キー制約が設定されているため、
    // usersテーブルのidカラムを参照することが可能。
    // user.getUsername()のようにオブジェクトの関連をたどるとJPA/HibernateがSQLを発行する。
    // → SELECT * FROM users WHERE id = ? のtupleがUserオブジェクトとして返される
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    protected ReviewMeeting() {
    }

    public ReviewMeeting(Model model, String title, LocalDate scheduledDate,
            String status, User organizer, String notes) {
        this.model = model;
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.organizer = organizer;
        this.notes = notes;
    }

}
