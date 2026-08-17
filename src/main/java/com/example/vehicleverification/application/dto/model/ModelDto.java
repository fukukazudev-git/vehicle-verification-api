package com.example.vehicleverification.application.dto.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 一覧用
@Getter
@Setter
@AllArgsConstructor
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
    private String destination;
    private String powertrainType;
}
