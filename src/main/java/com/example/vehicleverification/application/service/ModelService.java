package com.example.vehicleverification.application.service;

import java.util.List;

import com.example.vehicleverification.application.dto.model.ModelCreateRequest;
import com.example.vehicleverification.application.dto.model.ModelCreateResponse;
import com.example.vehicleverification.application.dto.model.ModelDetailResponse;
import com.example.vehicleverification.application.dto.model.ModelDto;
import com.example.vehicleverification.application.dto.model.ModelUpdateRequest;
import com.example.vehicleverification.application.dto.model.ModelUpdateResponse;

public interface ModelService {

    public List<ModelDto> getModelAll();

    public ModelDetailResponse getModelById(Long id);

    public ModelCreateResponse createModel(ModelCreateRequest request);

    public ModelUpdateResponse updateModel(Long id, ModelUpdateRequest request);

    public void deleteModel(Long id);

}
