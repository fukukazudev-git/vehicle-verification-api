package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingStatusResponse;
import com.example.vehicleverification.domain.entity.Model;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.ReviewMeetingStatus;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.AttachmentRepository;
import com.example.vehicleverification.domain.repository.ModelRepository;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewMeetingServiceImplTest {
    @Mock
    private ReviewMeetingRepository reviewMeetingRepository;

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private ReviewMeetingServiceImpl reviewMeetingService;

    // 1件分(model + organizer + reviewMeeting)まとめて生成
    private ReviewMeeting createDummyReviewMeeting(Long id, String title, ReviewMeetingStatus status) {
        LocalDate scheduledDate = LocalDate.of(2026, 1, 1);

        Model model = new Model("MC0" + id, "モデル" + id, 2026, "テストECU", "エンジン", "AWD", "詳細", "仕向け地", "HEV");
        model.setId(id);

        User organizer = new User("testuser" + id, "pass", "主催者" + id, "ADMIN", "開発部");
        organizer.setId(id);

        ReviewMeeting reviewMeeting =
                new ReviewMeeting(model, title, scheduledDate, status, organizer, "備考", "EVT" + id);
        reviewMeeting.setId(id);

        return reviewMeeting;
    }

    @Test
    void getReviewMeetingStatuses_全ステータスをcodeと日本語表示名で返す() {
        List<ReviewMeetingStatusResponse> result = reviewMeetingService.getReviewMeetingStatuses();

        assertThat(result)
                .extracting(ReviewMeetingStatusResponse::getCode, ReviewMeetingStatusResponse::getDisplayName)
                .containsExactly(
                        tuple("BEFORE_VERIFICATION", "検証前"),
                        tuple("IN_VERIFICATION", "検証中"),
                        tuple("COMPLETED", "完了"),
                        tuple("CANCELLED", "中断"));
    }

    @Test
    void getReviewMeetingById_存在するIDを指定した場合_DetailResponseを返す() {
        ReviewMeeting reviewMeeting = createDummyReviewMeeting(1L, "定例レビュー", ReviewMeetingStatus.BEFORE_VERIFICATION);

        Long reviewMeetingId = reviewMeeting.getId();
        given(reviewMeetingRepository.findById(reviewMeetingId)).willReturn(Optional.of(reviewMeeting));

        ReviewMeetingDetailResponse response = reviewMeetingService.getReviewMeetingById(reviewMeetingId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(reviewMeetingId);
        assertThat(response.getTitle()).isEqualTo("定例レビュー");
        assertThat(response.getScheduledDate()).isEqualTo(reviewMeeting.getScheduledDate());
        assertThat(response.getModelId()).isEqualTo(reviewMeeting.getModel().getId());
        assertThat(response.getOrganizerId())
                .isEqualTo(reviewMeeting.getOrganizer().getId());
    }

    @Test
    void getReviewMeetingById_存在しないIDを指定した場合_例外をスローする() {
        Long nonExistentId = 999L;
        given(reviewMeetingRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewMeetingService.getReviewMeetingById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getReviewMeetingAll_modelIdとstatusが両方nullの場合_全件返す() {
        ReviewMeeting reviewMeeting1 = createDummyReviewMeeting(1L, "定例レビュー1", ReviewMeetingStatus.BEFORE_VERIFICATION);
        ReviewMeeting reviewMeeting2 = createDummyReviewMeeting(2L, "定例レビュー2", ReviewMeetingStatus.COMPLETED);

        given(reviewMeetingRepository.findAll()).willReturn(List.of(reviewMeeting1, reviewMeeting2));

        List<ReviewMeetingDto> responses = reviewMeetingService.getReviewMeetingAll(null, null);

        assertThat(responses).hasSize(2);
    }

    @Test
    void getReviewMeetingAll_modelIdのみ指定した場合_modelIdで絞り込む() {
        // Arrange
        ReviewMeeting reviewMeeting = createDummyReviewMeeting(1L, "定例レビュー1", ReviewMeetingStatus.BEFORE_VERIFICATION);
        Long modelId = reviewMeeting.getModel().getId();

        given(reviewMeetingRepository.findByModelId(modelId)).willReturn(List.of(reviewMeeting));

        // Act
        List<ReviewMeetingDto> responses = reviewMeetingService.getReviewMeetingAll(modelId, null);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getModelId()).isEqualTo(modelId);

        // Repository自体への問い合わせ自体がない誤検知を防ぐためfindByModelIdが呼ばれたことを検証
        verify(reviewMeetingRepository).findByModelId(modelId);
        verify(reviewMeetingRepository, never()).findByModelIdAndStatus(anyLong(), any(ReviewMeetingStatus.class));
    }

    @Test
    void createReviewMeeting_正常系_CreateResponseを返す() {
        // Arrange
        ReviewMeeting reviewMeeting = createDummyReviewMeeting(1L, "定例レビュー", ReviewMeetingStatus.BEFORE_VERIFICATION);
        Long modelId = reviewMeeting.getModel().getId();
        Long organizerId = reviewMeeting.getOrganizer().getId();

        ReviewMeetingCreateRequest request = new ReviewMeetingCreateRequest();
        request.setModelId(modelId);
        request.setTitle("定例レビュー");
        request.setScheduledDate(LocalDate.of(2026, 1, 1));
        request.setStatus(ReviewMeetingStatus.BEFORE_VERIFICATION);
        request.setOrganizerId(organizerId);
        request.setNotes("備考");
        request.setEventCode("EVT123");

        Model model = reviewMeeting.getModel();
        User organizer = reviewMeeting.getOrganizer();

        given(modelRepository.findById(modelId)).willReturn(Optional.of(model));
        given(userRepository.findById(organizerId)).willReturn(Optional.of(organizer));
        // ReviewMeetingはequals()を持たない(=参照一致)ため引数マッチャで受ける
        given(reviewMeetingRepository.save(any(ReviewMeeting.class))).willReturn(reviewMeeting);

        // Act
        ReviewMeetingCreateResponse response = reviewMeetingService.createReviewMeeting(request);

        // Assert
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getModelId()).isEqualTo(request.getModelId());
        assertThat(response.getOrganizerId()).isEqualTo(request.getOrganizerId());
    }

    @Test
    void createReviewMeeting_存在しないmodelIdを指定した場合_例外をスローする() {

        Long nonExistentModelId = 999L;

        ReviewMeetingCreateRequest request = new ReviewMeetingCreateRequest();
        request.setModelId(nonExistentModelId);
        request.setTitle("定例レビュー");
        request.setScheduledDate(LocalDate.of(2026, 1, 1));
        request.setStatus(ReviewMeetingStatus.BEFORE_VERIFICATION);
        request.setOrganizerId(nonExistentModelId);
        request.setNotes("備考");
        request.setEventCode("EVT123");

        given(modelRepository.findById(nonExistentModelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewMeetingService.createReviewMeeting(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
