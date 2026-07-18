package com.example.vehicleverification.presentation.dto.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelCreateRequest {

    @NotBlank(message = "機種コードは必須です")
    @Size(max = 10, message = "機種コードは10文字以内で入力してください")
    private String modelCode;

    @NotBlank(message = "機種名は必須です")
    @Size(max = 20, message = "機種名は20文字以内で入力してください")
    private String modelName;

    @NotBlank(message = "モデル年式は必須です")
    @Size(max = 4, message = "モデル年式は4文字以内で入力してください")
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

    public ModelCreateRequest(String modelCode, String modelName, Integer modelYear, String ecuType,
            String engineType, String driveType, String description) {

        this.modelCode = modelCode;
        this.modelName = modelName;
        this.modelYear = modelYear;
        this.ecuType = ecuType;
        this.engineType = engineType;
        this.driveType = driveType;
        this.description = description;
    }

}
