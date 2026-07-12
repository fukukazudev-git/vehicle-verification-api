package com.example.vehicleverification.application.service;

import java.util.List;

import com.example.vehicleverification.dto.model.ModelDto;
import com.example.vehicleverification.dto.model.*;

public interface ModelService {

    public List<ModelDto> getModelAll();

    public ModelDetailResponse getModelById(Long id);

    public ModelCreateResponse createModel(ModelCreateRequest request);

    public ModelUpdateResponse updateModel(Long id, ModelUpdateRequest request);

    public void deleteModel(Long id);

}
