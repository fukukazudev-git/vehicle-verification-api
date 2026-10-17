package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.vehicleverification.domain.entity.Model;
import com.example.vehicleverification.domain.exception.ResourceInUseException;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.ModelRepository;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelServiceImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private ReviewMeetingRepository reviewMeetingRepository;

    @InjectMocks
    private ModelServiceImpl modelService;

    private Model createDummyModel(Long id) {
        Model model = new Model("M" + String.format("%03d", id), "機種" + id, 2026, null, null, null, null, null, null);
        model.setId(id);
        return model;
    }

    @Test
    void deleteModel_存在する機種を削除する場合_正常に削除される() {
        Model model = createDummyModel(1L);
        given(modelRepository.findById(1L)).willReturn(Optional.of(model));

        modelService.deleteModel(1L);

        verify(modelRepository).delete(model);
    }

    @Test
    void deleteModel_存在しない機種を削除する場合_ResourceNotFoundExceptionを投げる() {
        given(modelRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> modelService.deleteModel(999L)).isInstanceOf(ResourceNotFoundException.class);
        verify(modelRepository, never()).delete(any(Model.class));
    }

    @Test
    void deleteModel_ReviewMeetingから参照されている場合_ResourceInUseExceptionを投げる() {
        Model model = createDummyModel(1L);
        given(modelRepository.findById(1L)).willReturn(Optional.of(model));
        given(reviewMeetingRepository.existsByModelId(1L)).willReturn(true);

        assertThatThrownBy(() -> modelService.deleteModel(1L)).isInstanceOf(ResourceInUseException.class);
        verify(modelRepository, never()).delete(any(Model.class));
    }
}
