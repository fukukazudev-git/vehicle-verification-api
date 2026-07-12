package com.example.vehicleverification.dto.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelCreateRequest {

    private String modelCode;
    private String modelName;
    private Integer modelYear;
    private String ecuType;
    private String engineType;
    private String driveType;
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
