package com.example.vehicleverification.application.service;

import com.example.vehicleverification.domain.repository.ModelRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import com.example.vehicleverification.domain.entity.Model;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateResponse;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateResponse;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;

import jakarta.persistence.OptimisticLockException;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;

@Service
@Transactional(readOnly = true)
public class ReviewMeetingServiceImpl implements ReviewMeetingService {

    private final ReviewMeetingRepository reviewMeetingRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;

    public ReviewMeetingServiceImpl(
            ReviewMeetingRepository reviewMeetingRepository,
            ModelRepository modelRepository,
            UserRepository userRepository) {
        this.reviewMeetingRepository = reviewMeetingRepository;
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
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
    public List<ReviewMeetingDto> getReviewMeetingAll(Long modelId, String status) {

        if (modelId != null && status != null) {
            return reviewMeetingRepository.findByModelIdAndStatus(modelId, status)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (modelId != null) {
            return reviewMeetingRepository.findByModelId(modelId)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (status != null) {
            return reviewMeetingRepository.findByStatus(status)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return reviewMeetingRepository.findAll()
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
                reviewMeeting.getCreatedAt(),
                reviewMeeting.getVersion());

    }

    @Override
    @Transactional
    public ReviewMeetingCreateResponse createReviewMeeting(ReviewMeetingCreateRequest request) {

        Model model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getModelId()));

        User organizer = userRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getOrganizerId()));

        ReviewMeeting reviewMeeting = new ReviewMeeting(
                model,
                request.getTitle(),
                request.getScheduledDate(),
                request.getStatus(),
                organizer,
                request.getNotes());

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
                saved.getCreatedAt());

    }

    @Override
    @Transactional
    public ReviewMeetingUpdateResponse updateReviewMeeting(Long id, ReviewMeetingUpdateRequest request) {
        ReviewMeeting existingReviewMeeting = reviewMeetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (!existingReviewMeeting.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException();
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
