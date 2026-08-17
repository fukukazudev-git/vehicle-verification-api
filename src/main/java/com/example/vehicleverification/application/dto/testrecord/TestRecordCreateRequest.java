package com.example.vehicleverification.application.dto.testrecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestRecordCreateRequest {

    @NotNull(message = "検証会IDは必須です")
    private Long reviewMeetingId;

    @NotBlank(message = "テスト名は必須です")
    @Size(max = 100)
    private String testName;

    @NotBlank(message = "結果は必須です")
    @Size(max = 20)
    private String result;

    private String notes;

    @NotNull(message = "テスト者IDは必須です")
    private Long recordedById;
}
