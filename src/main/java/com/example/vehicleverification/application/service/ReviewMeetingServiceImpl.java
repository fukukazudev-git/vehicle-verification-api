package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateResponse;
import com.example.vehicleverification.application.dto.summary.IssueSummaryResponse;
import com.example.vehicleverification.application.dto.summary.TestSummaryResponse;
import com.example.vehicleverification.domain.entity.Attachment;
import com.example.vehicleverification.domain.entity.Model;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.AttachmentRepository;
import com.example.vehicleverification.domain.repository.IssueRepository;
import com.example.vehicleverification.domain.repository.ModelRepository;
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
public class ReviewMeetingServiceImpl implements ReviewMeetingService {

    private final ReviewMeetingRepository reviewMeetingRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final TestRecordRepository testRecordRepository;
    private final IssueRepository issueRepository;

    public ReviewMeetingServiceImpl(
            ReviewMeetingRepository reviewMeetingRepository,
            ModelRepository modelRepository,
            UserRepository userRepository,
            AttachmentRepository attachmentRepository,
            TestRecordRepository testRecordRepository,
            IssueRepository issueRepository) {
        this.reviewMeetingRepository = reviewMeetingRepository;
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.testRecordRepository = testRecordRepository;
        this.issueRepository = issueRepository;
    }

    private ReviewMeetingDto convertToDto(ReviewMeeting reviewMeeting) {
        return new ReviewMeetingDto(
                reviewMeeting.getId(),
                reviewMeeting.getTitle(),
                reviewMeeting.getScheduledDate(),
                reviewMeeting.getStatus(),
                reviewMeeting.getModel().getId(),
                reviewMeeting.getModel().getModelName(),
                reviewMeeting.getOrganizer().getId(),
                reviewMeeting.getOrganizer().getUsername(),
                reviewMeeting.getCreatedAt(),
                reviewMeeting.getEventCode());
    }

    private AttachmentDto convertToAttachmentDto(Attachment attachment) {
        return new AttachmentDto(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getStoredPath(),
                attachment.getReviewMeeting() != null
                        ? attachment.getReviewMeeting().getId()
                        : null,
                attachment.getTestRecord() != null ? attachment.getTestRecord().getId() : null,
                attachment.getUser() != null ? attachment.getUser().getId() : null,
                attachment.getUploadedBy().getId(),
                attachment.getUploadedBy().getDisplayName(),
                attachment.getUploadedAt());
    }

    @Override
    public List<ReviewMeetingDto> getReviewMeetingAll(Long modelId, String status) {

        if (modelId != null && status != null) {
            return reviewMeetingRepository.findByModelIdAndStatus(modelId, status).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (modelId != null) {
            return reviewMeetingRepository.findByModelId(modelId).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (status != null) {
            return reviewMeetingRepository.findByStatus(status).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return reviewMeetingRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public ReviewMeetingDetailResponse getReviewMeetingById(Long id) {
        ReviewMeeting reviewMeeting =
                reviewMeetingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        List<Attachment> attachments = attachmentRepository.findByReviewMeetingId(reviewMeeting.getId());

        return new ReviewMeetingDetailResponse(
                reviewMeeting.getId(),
                reviewMeeting.getTitle(),
                reviewMeeting.getScheduledDate(),
                reviewMeeting.getStatus(),
                reviewMeeting.getNotes(),
                reviewMeeting.getModel().getId(),
                reviewMeeting.getModel().getModelName(),
                reviewMeeting.getOrganizer().getId(),
                reviewMeeting.getOrganizer().getUsername(),
                reviewMeeting.getCreatedAt(),
                reviewMeeting.getVersion(),
                reviewMeeting.getEventCode(),
                attachments.stream().map(this::convertToAttachmentDto).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public ReviewMeetingCreateResponse createReviewMeeting(ReviewMeetingCreateRequest request) {

        Model model = modelRepository
                .findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getModelId()));

        User organizer = userRepository
                .findById(request.getOrganizerId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getOrganizerId()));

        ReviewMeeting reviewMeeting = new ReviewMeeting(
                model,
                request.getTitle(),
                request.getScheduledDate(),
                request.getStatus(),
                organizer,
                request.getNotes(),
                request.getEventCode());

        ReviewMeeting saved = reviewMeetingRepository.save(reviewMeeting);

        return new ReviewMeetingCreateResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getScheduledDate(),
                saved.getStatus(),
                saved.getModel().getId(),
                saved.getModel().getModelName(),
                saved.getOrganizer().getId(),
                saved.getOrganizer().getUsername(),
                saved.getCreatedAt(),
                saved.getEventCode());
    }

    @Override
    @Transactional
    public ReviewMeetingUpdateResponse updateReviewMeeting(Long id, ReviewMeetingUpdateRequest request) {
        ReviewMeeting existingReviewMeeting =
                reviewMeetingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        if (!existingReviewMeeting.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException();
        }

        existingReviewMeeting.setTitle(request.getTitle());
        existingReviewMeeting.setScheduledDate(request.getScheduledDate());
        existingReviewMeeting.setStatus(request.getStatus());
        existingReviewMeeting.setNotes(request.getNotes());
        existingReviewMeeting.setEventCode(request.getEventCode());

        ReviewMeeting saved = reviewMeetingRepository.saveAndFlush(existingReviewMeeting);

        return new ReviewMeetingUpdateResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getScheduledDate(),
                saved.getStatus(),
                saved.getNotes(),
                saved.getModel().getId(),
                saved.getModel().getModelName(),
                saved.getOrganizer().getId(),
                saved.getOrganizer().getUsername(),
                saved.getVersion(),
                saved.getEventCode());
    }

    @Override
    @Transactional
    public void deleteReviewMeeting(Long id) {
        ReviewMeeting reviewMeeting =
                reviewMeetingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        reviewMeetingRepository.delete(reviewMeeting);
    }

    @Override
    public TestSummaryResponse getTestSummary(Long reviewMeetingId) {
        ReviewMeeting reviewMeeting = reviewMeetingRepository
                .findById(reviewMeetingId)
                .orElseThrow(() -> new ResourceNotFoundException(reviewMeetingId));

        long okCount = testRecordRepository.countByReviewMeetingIdAndResult(reviewMeetingId, "OK");
        long ngCount = testRecordRepository.countByReviewMeetingIdAndResult(reviewMeetingId, "NG");
        long pendingCount = testRecordRepository.countByReviewMeetingIdAndResult(reviewMeetingId, "保留");
        long totalCount = okCount + ngCount + pendingCount;
        double okRate = totalCount > 0 ? Math.round((double) okCount / totalCount * 1000) / 10.0 : 0;

        return new TestSummaryResponse(
                reviewMeetingId, reviewMeeting.getTitle(), totalCount, okCount, ngCount, pendingCount, okRate);
    }

    @Override
    public IssueSummaryResponse getIssueSummary(Long reviewMeetingId) {
        ReviewMeeting reviewMeeting = reviewMeetingRepository
                .findById(reviewMeetingId)
                .orElseThrow(() -> new ResourceNotFoundException(reviewMeetingId));

        long unresolvedCount = issueRepository.countByReviewMeetingIdAndStatus(reviewMeetingId, "未対応");
        long inProgressCount = issueRepository.countByReviewMeetingIdAndStatus(reviewMeetingId, "対応中");
        long pendingApprovalCount = issueRepository.countByReviewMeetingIdAndStatus(reviewMeetingId, "承認待ち");
        long resolvedCount = issueRepository.countByReviewMeetingIdAndStatus(reviewMeetingId, "完了");
        long totalCount = unresolvedCount + inProgressCount + pendingApprovalCount + resolvedCount;
        boolean allResolved = totalCount > 0 && resolvedCount == totalCount;

        return new IssueSummaryResponse(
                reviewMeetingId,
                reviewMeeting.getTitle(),
                totalCount,
                unresolvedCount,
                inProgressCount,
                pendingApprovalCount,
                resolvedCount,
                allResolved);
    }
}
