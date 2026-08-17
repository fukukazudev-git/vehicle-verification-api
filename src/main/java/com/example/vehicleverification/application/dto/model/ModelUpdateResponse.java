package com.example.vehicleverification.application.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ModelUpdateResponse {

    private Long id;
    private String modelCode;
    private String modelName;
    private Integer modelYear;
    private String ecuType;
    private String engineType;
    private String driveType;
    private String description;
    private Long version;
    private String destination;
    private String powertrainType;
}
