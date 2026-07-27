package com.example.vehicleverification.application.service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateResponse;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.exception.DuplicateResourceException;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;

import jakarta.persistence.OptimisticLockException;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;

@Service
@Transactional(readOnly = true)
public class ReviewMeetingServiceImpl implements ReviewMeetingService {

    // Implement the methods defined in the ReviewMeetingService interface here
    private final ReviewMeetingRepository reviewMeetingRepository;

    public ReviewMeetingServiceImpl(ReviewMeetingRepository reviewMeetingRepository) {
        this.reviewMeetingRepository = reviewMeetingRepository;
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
                reviewMeeting.getCreatedAt());
    }

    @Override
    public List<ReviewMeetingDto> getReviewMeetingAll(ReviewMeeting reviewMeeting) {

        if (reviewMeetingRepository.findByModelIdAndStatus(
                reviewMeeting.getModel().getId(),
                reviewMeeting.getStatus())
                .isEmpty()) {
            throw new ResourceNotFoundException(reviewMeeting.getModel().getId());
        }

        if (reviewMeeting.getModel().getId() == null && reviewMeeting.getStatus() == null) {
            return reviewMeetingRepository.findAll()
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (reviewMeeting.getModel().getId() != null && reviewMeeting.getStatus() == null) {
            return reviewMeetingRepository.findByModelId(reviewMeeting.getModel().getId())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (reviewMeeting.getModel().getId() == null && reviewMeeting.getStatus() != null) {
            return reviewMeetingRepository.findByStatus(reviewMeeting.getStatus())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return reviewMeetingRepository
                    .findByModelIdAndStatus(reviewMeeting.getModel().getId(), reviewMeeting.getStatus())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

    }

    @Override
    public ReviewMeetingDetailResponse getReviewMeetingById(Long id) {
        ReviewMeeting reviewMeeting = reviewMeetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

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
                reviewMeeting.getCreatedAt());

    }

    @Override
    @Transactional
    public ReviewMeetingDetailResponse createReviewMeeting(ReviewMeeting reviewMeeting) {
    }

    @Override
    @Transactional
    public ReviewMeetingUpdateResponse updateReviewMeeting(Long id, ReviewMeetingUpdateRequest request) {
        ReviewMeeting existingReviewMeeting = reviewMeetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (!existingReviewMeeting.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException();
        }

        if (reviewMeetingRepository.existsByReviewMeetingTitleAndIdNot(request.getTitle(), id)) {
            throw new DuplicateResourceException("title", "このレビュー会議のタイトルは既に登録されています");
        }

        existingReviewMeeting.setTitle(request.getTitle());
        existingReviewMeeting.setScheduledDate(request.getScheduledDate());
        existingReviewMeeting.setStatus(request.getStatus());
        existingReviewMeeting.setNotes(request.getNotes());

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
                saved.getVersion());
    }

    @Override
    @Transactional
    public void deleteReviewMeeting(Long id) {
        ReviewMeeting reviewMeeting = reviewMeetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        reviewMeetingRepository.delete(reviewMeeting);

    }

}
