package com.example.vehicleverification.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "models")
@Getter
@Setter
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 10, min = 3)
    private String modelCode;

    @NotNull
    @Size(max = 20)
    private String modelName;

    @NotNull
    @Min(1900)
    @Max(9999)
    private Integer modelYear;

    @Size(max = 20)
    private String ecuType;

    @Size(max = 20)
    private String engineType;

    @Size(max = 10)
    private String driveType;

    @Size(max = 500)
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    protected Model() {
    }

    public Model(String modelCode, String modelName, Integer modelYear, String ecuType, String engineType,
            String driveType, String description) {
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.modelYear = modelYear;
        this.ecuType = ecuType;
        this.engineType = engineType;
        this.driveType = driveType;
        this.description = description;
    }

}
