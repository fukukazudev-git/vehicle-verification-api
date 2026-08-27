package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDetailResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateRequest;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.TestRecord;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.TestRecordRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestRecordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMeetingRepository reviewMeetingRepository;

    @Mock
    private TestRecordRepository testRecordRepository;

    @InjectMocks
    private TestRecordServiceImpl testRecordService;

    private TestRecord createDummyTestRecord(Long id, String testName, String result) {
        ReviewMeeting reviewMeeting =
                new ReviewMeeting(null, "レビュー会議", LocalDate.of(2026, 1, 1), "予定", null, "備考", "EVT" + id);
        reviewMeeting.setId(id);

        User recordedBy = new User("testuser" + id, "pass", "記録者" + id, "ADMIN", "開発部");
        recordedBy.setId(id);

        TestRecord testRecord = new TestRecord(reviewMeeting, testName, result, "備考", recordedBy);
        testRecord.setId(id);

        return testRecord;
    }

    @Test
    void getTestRecordById_存在するIDを指定した場合_DetailResponseを返す() {

        TestRecord testRecord = createDummyTestRecord(1L, "テスト1", "合格");
        Long testRecordId = testRecord.getId();
        given(testRecordRepository.findById(testRecordId)).willReturn(Optional.of(testRecord));

        TestRecordDetailResponse response = testRecordService.getTestRecordById(testRecordId);

        assertThat(response.getId()).isEqualTo(testRecordId);
        assertThat(response.getTestName()).isEqualTo(testRecord.getTestName());
        assertThat(response.getReviewMeetingId())
                .isEqualTo(testRecord.getReviewMeeting().getId());
        assertThat(response.getRecordedById())
                .isEqualTo(testRecord.getRecordedBy().getId());
    }

    @Test
    void getTestRecordById_存在しないIDを指定した場合_例外をスローする() {

        Long nonExistentId = 999L;
        given(testRecordRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> testRecordService.getTestRecordById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createTestRecord_正常系_CreateResponseを返す() {
        // Arrange
        TestRecord testRecord = createDummyTestRecord(1L, "テスト1", "合格");
        Long reviewMeetingId = testRecord.getReviewMeeting().getId();
        Long recordedById = testRecord.getRecordedBy().getId();

        TestRecordCreateRequest request = new TestRecordCreateRequest();
        request.setReviewMeetingId(reviewMeetingId);
        request.setTestName("テスト1");
        request.setResult("合格");
        request.setNotes("備考");
        request.setRecordedById(recordedById);

        given(reviewMeetingRepository.findById(reviewMeetingId)).willReturn(Optional.of(testRecord.getReviewMeeting()));
        given(userRepository.findById(recordedById)).willReturn(Optional.of(testRecord.getRecordedBy()));
        given(testRecordRepository.save(any(TestRecord.class))).willReturn(testRecord);

        // Act
        TestRecordCreateResponse response = testRecordService.createTestRecord(request);

        // Assert
        assertThat(response.getTestName()).isEqualTo(request.getTestName());
        assertThat(response.getReviewMeetingId()).isEqualTo(reviewMeetingId);
        assertThat(response.getRecordedById()).isEqualTo(recordedById);
    }

    @Test
    void createTestRecord_存在しないreviewMeetingIdを指定した場合_例外をスローする() {
        // Arrange
        Long nonExistentReviewMeetingId = 999L;
        TestRecordCreateRequest request = new TestRecordCreateRequest();
        request.setReviewMeetingId(nonExistentReviewMeetingId);
        request.setTestName("テスト1");
        request.setResult("合格");
        request.setNotes("備考");
        request.setRecordedById(1L);

        given(reviewMeetingRepository.findById(nonExistentReviewMeetingId)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> testRecordService.createTestRecord(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTestRecord_楽観的ロック違反_例外をスローする() {
        // Arrange
        TestRecord testRecord = createDummyTestRecord(1L, "テスト1", "合格");
        testRecord.setVersion(0L);
        Long testRecordId = testRecord.getId();

        TestRecordUpdateRequest updateRequest = new TestRecordUpdateRequest();
        updateRequest.setTestName("テスト1更新");
        updateRequest.setResult("不合格");
        updateRequest.setNotes("更新備考");
        updateRequest.setVersion(1L); // 現在のバージョンと異なる値を設定

        given(testRecordRepository.findById(testRecordId)).willReturn(Optional.of(testRecord));

        // Act & Assert
        assertThatThrownBy(() -> testRecordService.updateTestRecord(testRecordId, updateRequest))
                // 単体テストではSpringの例外トランスレーションが適用されないため、JPAのOptimisticLockExceptionがスローされることを確認
                .isInstanceOf(OptimisticLockException.class);
    }
}
