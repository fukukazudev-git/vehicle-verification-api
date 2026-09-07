package com.example.vehicleverification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VehicleVerificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleVerificationApplication.class, args);
    }
}
