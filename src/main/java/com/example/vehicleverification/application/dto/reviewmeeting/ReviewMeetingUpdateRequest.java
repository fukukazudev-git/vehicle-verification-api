package com.example.vehicleverification.application.dto.reviewmeeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewMeetingUpdateRequest {

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200)
    private String title;

    @NotNull
    private LocalDate scheduledDate;

    @NotBlank(message = "ステータスは必須です")
    private String status;

    @NotNull
    private Long organizerId;

    private String notes;

    @Size(max = 20)
    @NotBlank(message = "イベントコードは必須です")
    private String eventCode;

    @NotNull
    private Long version;

}
