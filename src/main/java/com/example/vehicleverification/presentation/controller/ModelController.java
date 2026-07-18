package com.example.vehicleverification.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.vehicleverification.application.service.ModelService;
import com.example.vehicleverification.presentation.dto.model.*;

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
