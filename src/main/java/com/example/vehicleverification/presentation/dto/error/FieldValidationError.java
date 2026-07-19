package com.example.vehicleverification.presentation.dto.error;

import lombok.Getter;

@Getter
public class FieldValidationError {

    private String field;
    private String message;

    public FieldValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }

}
