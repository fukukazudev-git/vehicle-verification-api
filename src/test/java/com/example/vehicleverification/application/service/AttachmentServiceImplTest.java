package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.attachment.AttachmentUploadRequest;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.AttachmentRepository;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.TestRecordRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private ReviewMeetingRepository reviewMeetingRepository;

    @Mock
    private TestRecordRepository testRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private static final Long UPLOADER_ID = 99L;

    private AttachmentUploadRequest request(Long reviewMeetingId, Long testRecordId, Long userId) {
        AttachmentUploadRequest request = new AttachmentUploadRequest();
        request.setReviewMeetingId(reviewMeetingId);
        request.setTestRecordId(testRecordId);
        request.setUserId(userId);
        return request;
    }

    private MockMultipartFile pdf(String fileName) {
        return new MockMultipartFile("file", fileName, "application/pdf", "content".getBytes());
    }

    private ReviewMeeting dummyReviewMeeting(Long id) {
        ReviewMeeting reviewMeeting =
                new ReviewMeeting(null, "レビュー会議", LocalDate.of(2026, 1, 1), "予定", null, "備考", "EVT" + id);
        reviewMeeting.setId(id);
        return reviewMeeting;
    }

    private User dummyUser(Long id) {
        User user = new User("user" + id, "pass", "表示名" + id, "ADMIN", "開発部");
        user.setId(id);
        return user;
    }

    @Test
    void upload_紐づけ先を1つも指定しない場合_IllegalArgumentExceptionを投げる() {
        MockMultipartFile file = pdf("test.pdf");

        assertThatThrownBy(() -> attachmentService.upload(request(null, null, null), file, UPLOADER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("最低1つ");

        verify(storageService, never()).store(any(), any());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void upload_許可されていない拡張子の場合_IllegalArgumentExceptionを投げる() {
        MockMultipartFile file = pdf("malware.exe");

        assertThatThrownBy(() -> attachmentService.upload(request(1L, null, null), file, UPLOADER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("許可されていないファイル形式です");

        verify(storageService, never()).store(any(), any());
    }

    @Test
    void upload_拡張子が無いファイル名の場合_IllegalArgumentExceptionを投げる() {
        MockMultipartFile file = pdf("noextension");

        assertThatThrownBy(() -> attachmentService.upload(request(1L, null, null), file, UPLOADER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("許可されていないファイル形式です");
    }

    @Test
    void upload_ファイルサイズが上限を超える場合_IllegalArgumentExceptionを投げる() {
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("big.pdf");
        given(file.getSize()).willReturn(100L * 1024 * 1024 + 1);

        assertThatThrownBy(() -> attachmentService.upload(request(1L, null, null), file, UPLOADER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("上限");

        verify(storageService, never()).store(any(), any());
    }

    @Test
    void upload_指定した検証会が存在しない場合_ResourceNotFoundExceptionを投げる() {
        MockMultipartFile file = pdf("test.pdf");
        given(reviewMeetingRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(request(1L, null, null), file, UPLOADER_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storageService, never()).store(any(), any());
    }

    @Test
    void upload_指定したテスト記録が存在しない場合_ResourceNotFoundExceptionを投げる() {
        MockMultipartFile file = pdf("test.pdf");
        given(testRecordRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(request(null, 2L, null), file, UPLOADER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upload_指定したユーザーが存在しない場合_ResourceNotFoundExceptionを投げる() {
        MockMultipartFile file = pdf("test.pdf");
        given(userRepository.findById(3L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(request(null, null, 3L), file, UPLOADER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upload_アップロード者が存在しない場合_ResourceNotFoundExceptionを投げる() {
        MockMultipartFile file = pdf("test.pdf");
        given(reviewMeetingRepository.findById(1L)).willReturn(Optional.of(dummyReviewMeeting(1L)));
        given(userRepository.findById(UPLOADER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(request(1L, null, null), file, UPLOADER_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void upload_正常系_ストレージ保存と永続化を行いDtoを返す() {
        MockMultipartFile file = pdf("設計書.pdf");
        ReviewMeeting reviewMeeting = dummyReviewMeeting(10L);
        User uploader = dummyUser(UPLOADER_ID);

        given(reviewMeetingRepository.findById(10L)).willReturn(Optional.of(reviewMeeting));
        given(userRepository.findById(UPLOADER_ID)).willReturn(Optional.of(uploader));
        // save() は永続化したエンティティを返すため、渡された引数をそのまま返す
        given(attachmentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        AttachmentDto dto = attachmentService.upload(request(10L, null, null), file, UPLOADER_ID);

        assertThat(dto.getFileName()).isEqualTo("設計書.pdf");
        assertThat(dto.getFileType()).isEqualTo("pdf");
        assertThat(dto.getReviewMeetingId()).isEqualTo(10L);
        assertThat(dto.getTestRecordId()).isNull();
        assertThat(dto.getUserId()).isNull();
        assertThat(dto.getUploadedById()).isEqualTo(UPLOADER_ID);
        assertThat(dto.getUploadedByName()).isEqualTo("表示名" + UPLOADER_ID);

        verify(storageService).store(any(MultipartFile.class), any(String.class));
        verify(attachmentRepository).save(any());
    }
}
