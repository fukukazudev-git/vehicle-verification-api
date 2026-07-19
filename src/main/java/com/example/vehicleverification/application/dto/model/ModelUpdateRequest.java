package com.example.vehicleverification.application.dto.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class ModelUpdateRequest {

    private Long id;

    @NotBlank(message = "機種コードは必須です")
    @Size(max = 10, message = "機種コードは10文字以内で入力してください")
    private String modelCode;

    @NotBlank(message = "機種名は必須です")
    @Size(max = 20, message = "機種名は20文字以内で入力してください")
    private String modelName;

    @NotNull(message = "モデル年式は必須です")
    @Min(value = 1900, message = "モデル年式は1900以上で入力してください")
    @Max(value = 9999, message = "モデル年式は9999以下で入力してください")
    private Integer modelYear;

    @NotBlank(message = "ECUタイプは必須です")
    @Size(max = 20, message = "ECUタイプは20文字以内で入力してください")
    private String ecuType;

    @NotBlank(message = "エンジンタイプは必須です")
    @Size(max = 20, message = "エンジンタイプは20文字以内で入力してください")
    private String engineType;

    @NotBlank(message = "駆動方式は必須です")
    @Size(max = 10, message = "駆動方式は10文字以内で入力してください")
    private String driveType;

    @Size(max = 500, message = "備考は500文字以内で入力してください")
    private String description;

    @NotNull(message = "バージョンは必須です")
    private Long version;

}
