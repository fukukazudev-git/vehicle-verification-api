package com.example.vehicleverification.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.vehicleverification.domain.entity.Model;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.ModelRepository;
import com.example.vehicleverification.presentation.dto.model.*;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ModelServiceImpl implements ModelService {

        private final ModelRepository modelRepository;

        public ModelServiceImpl(ModelRepository modelRepository) {
                this.modelRepository = modelRepository;
        }

        private ModelDto convertToDto(Model model) {
                return new ModelDto(
                                model.getId(),
                                model.getModelCode(),
                                model.getModelName(),
                                model.getModelYear(),
                                model.getEcuType(),
                                model.getEngineType(),
                                model.getDriveType(),
                                model.getDescription(),
                                model.getCreatedAt());
        }

        @Override
        public List<ModelDto> getModelAll() {
                return modelRepository.findAll()
                                .stream()
                                .map(this::convertToDto)
                                .collect(Collectors.toList());
        }

        @Override
        public ModelDetailResponse getModelById(Long id) {
                Model model = modelRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(id));

                return new ModelDetailResponse(
                                model.getId(),
                                model.getModelCode(),
                                model.getModelName(),
                                model.getModelYear(),
                                model.getEcuType(),
                                model.getEngineType(),
                                model.getDriveType(),
                                model.getDescription(),
                                model.getCreatedAt());
        }

        @Override
        public ModelCreateResponse createModel(ModelCreateRequest request) {
                Model model = new Model(
                                request.getModelCode(),
                                request.getModelName(),
                                request.getModelYear(),
                                request.getEcuType(),
                                request.getEngineType(),
                                request.getDriveType(),
                                request.getDescription());

                Model saved = modelRepository.save(model);

                return new ModelCreateResponse(
                                saved.getId(),
                                saved.getModelCode(),
                                saved.getModelName(),
                                saved.getModelYear(),
                                saved.getEcuType(),
                                saved.getEngineType(),
                                saved.getDriveType(),
                                saved.getDescription(),
                                saved.getCreatedAt());
        }

        @Override
        @Transactional
        public ModelUpdateResponse updateModel(Long id, ModelUpdateRequest request) {
                Model model = modelRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(id));

                if (!model.getVersion().equals(request.getVersion())) {
                        throw new OptimisticLockException();
                }

                model.setModelCode(request.getModelCode());
                model.setModelName(request.getModelName());
                model.setModelYear(request.getModelYear());
                model.setEcuType(request.getEcuType());
                model.setEngineType(request.getEngineType());
                model.setDriveType(request.getDriveType());
                model.setDescription(request.getDescription());

                return new ModelUpdateResponse(
                                model.getId(),
                                model.getModelCode(),
                                model.getModelName(),
                                model.getModelYear(),
                                model.getEcuType(),
                                model.getEngineType(),
                                model.getDriveType(),
                                model.getDescription(),
                                model.getVersion());
        }

        @Override
        @Transactional
        public void deleteModel(Long id) {
                Model model = modelRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(id));

                modelRepository.delete(model);
        }

}
