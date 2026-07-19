package com.example.vehicleverification.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vehicleverification.application.service.ModelService;
import com.example.vehicleverification.application.dto.model.ModelCreateRequest;
import com.example.vehicleverification.application.dto.model.ModelCreateResponse;
import com.example.vehicleverification.application.dto.model.ModelDetailResponse;
import com.example.vehicleverification.application.dto.model.ModelDto;
import com.example.vehicleverification.application.dto.model.ModelUpdateRequest;
import com.example.vehicleverification.application.dto.model.ModelUpdateResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping
    public List<ModelDto> getModelAll() {
        return modelService.getModelAll();
    }

    @GetMapping("/{id}")
    public ModelDetailResponse getModelById(
            @PathVariable Long id) {
        return modelService.getModelById(id);
    }

    @PostMapping
    public ModelCreateResponse createModel(@Valid @RequestBody ModelCreateRequest request) {
        return modelService.createModel(request);
    }

    @PutMapping("/{id}")
    public ModelUpdateResponse updateModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelUpdateRequest request) {
        return modelService.updateModel(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteModel(
            @PathVariable Long id) {
        modelService.deleteModel(id);
    }

}
