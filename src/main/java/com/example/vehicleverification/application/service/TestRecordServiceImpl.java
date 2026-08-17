package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDetailResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDto;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateResponse;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.TestRecord;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.TestRecordRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TestRecordServiceImpl implements TestRecordService {

    private final TestRecordRepository testRecordRepository;
    private final ReviewMeetingRepository reviewMeetingRepository;
    private final UserRepository userRepository;

    public TestRecordServiceImpl(
            TestRecordRepository testRecordRepository,
            ReviewMeetingRepository reviewMeetingRepository,
            UserRepository userRepository) {
        this.testRecordRepository = testRecordRepository;
        this.reviewMeetingRepository = reviewMeetingRepository;
        this.userRepository = userRepository;
    }

    private TestRecordDto convertToDto(TestRecord testRecord) {
        return new TestRecordDto(
                testRecord.getId(),
                testRecord.getTestName(),
                testRecord.getResult(),
                testRecord.getReviewMeeting().getId(),
                testRecord.getReviewMeeting().getTitle(),
                testRecord.getRecordedBy().getId(),
                testRecord.getRecordedBy().getDisplayName(),
                testRecord.getRecordedAt());
    }

    @Override
    public List<TestRecordDto> getTestRecordsByReviewMeeting(Long reviewMeetingId, String result) {
        if (reviewMeetingId != null && result != null) {
            return testRecordRepository.findByReviewMeetingIdAndResult(reviewMeetingId, result).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (reviewMeetingId != null) {
            return testRecordRepository.findByReviewMeetingId(reviewMeetingId).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (result != null) {
            return testRecordRepository.findByResult(result).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return testRecordRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public TestRecordDetailResponse getTestRecordById(Long id) {
        TestRecord testRecord = testRecordRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        return new TestRecordDetailResponse(
                testRecord.getId(),
                testRecord.getTestName(),
                testRecord.getResult(),
                testRecord.getNotes(),
                testRecord.getReviewMeeting().getId(),
                testRecord.getReviewMeeting().getTitle(),
                testRecord.getRecordedBy().getId(),
                testRecord.getRecordedBy().getDisplayName(),
                testRecord.getRecordedAt(),
                testRecord.getVersion());
    }

    @Override
    @Transactional
    public TestRecordCreateResponse createTestRecord(TestRecordCreateRequest request) {

        ReviewMeeting reviewMeeting = reviewMeetingRepository
                .findById(request.getReviewMeetingId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getReviewMeetingId()));

        User recordedBy = userRepository
                .findById(request.getRecordedById())
                .orElseThrow(() -> new ResourceNotFoundException(request.getRecordedById()));

        TestRecord testRecord = new TestRecord(
                reviewMeeting, request.getTestName(), request.getResult(), request.getNotes(), recordedBy);

        TestRecord saved = testRecordRepository.save(testRecord);

        return new TestRecordCreateResponse(
                saved.getId(),
                saved.getTestName(),
                saved.getResult(),
                saved.getNotes(),
                saved.getReviewMeeting().getId(),
                saved.getReviewMeeting().getTitle(),
                saved.getRecordedBy().getId(),
                saved.getRecordedBy().getDisplayName(),
                saved.getRecordedAt());
    }

    @Override
    @Transactional
    public TestRecordUpdateResponse updateTestRecord(Long id, TestRecordUpdateRequest request) {
        TestRecord testRecord = testRecordRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        if (!testRecord.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException();
        }

        testRecord.setTestName(request.getTestName());
        testRecord.setResult(request.getResult());
        testRecord.setNotes(request.getNotes());

        TestRecord saved = testRecordRepository.saveAndFlush(testRecord);

        return new TestRecordUpdateResponse(
                saved.getId(),
                saved.getTestName(),
                saved.getResult(),
                saved.getNotes(),
                saved.getReviewMeeting().getId(),
                saved.getReviewMeeting().getTitle(),
                saved.getRecordedBy().getId(),
                saved.getRecordedBy().getDisplayName(),
                saved.getRecordedAt(),
                saved.getVersion());
    }

    @Override
    @Transactional
    public void deleteTestRecord(Long id) {

        TestRecord testRecord = testRecordRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        testRecordRepository.delete(testRecord);
    }
}
