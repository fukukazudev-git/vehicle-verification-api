package com.example.vehicleverification.presentation.dto.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public ModelUpdateResponse(Long id, String modelCode, String modelName, Integer modelYear, String ecuType,
            String engineType, String driveType, String description, Long version) {
        this.id = id;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.modelYear = modelYear;
        this.ecuType = ecuType;
        this.engineType = engineType;
        this.driveType = driveType;
        this.description = description;
        this.version = version;
    }

}
