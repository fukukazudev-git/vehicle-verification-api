package com.example.vehicleverification.presentation.dto.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

//一覧用
@Getter
@Setter
public class ModelDto {

    private Long id;
    private String modelCode;
    private String modelName;
    private Integer modelYear;
    private String ecuType;
    private String engineType;
    private String driveType;
    private String description;
    private LocalDateTime createdAt;

    public ModelDto(Long id, String modelCode, String modelName, Integer modelYear, String ecuType,
            String engineType, String driveType, String description, LocalDateTime createdAt) {
        this.id = id;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.modelYear = modelYear;
        this.ecuType = ecuType;
        this.engineType = engineType;
        this.driveType = driveType;
        this.description = description;
        this.createdAt = createdAt;
    }

}
