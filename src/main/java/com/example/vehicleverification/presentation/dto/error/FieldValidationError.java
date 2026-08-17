package com.example.vehicleverification.presentation.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FieldValidationError {

    private String field;
    private String message;
}
