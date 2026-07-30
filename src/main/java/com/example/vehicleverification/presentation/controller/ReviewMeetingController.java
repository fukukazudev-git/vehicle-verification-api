package com.example.vehicleverification.presentation.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.example.vehicleverification.application.service.ReviewMeetingService;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;

@RestController
@RequestMapping("/api/review-meetings")
public class ReviewMeetingController {

    private final ReviewMeetingService reviewMeetingService;

    public ReviewMeetingController(ReviewMeetingService reviewMeetingService) {
        this.reviewMeetingService = reviewMeetingService;
    }

    @GetMapping
    public List<ReviewMeetingDto> getReviewMeetingAll(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String status) {
        return reviewMeetingService.getReviewMeetingAll(modelId, status);
    }

    @GetMapping("/{id}")
    public ReviewMeetingDetailResponse getReviewMeetingById(
            @PathVariable Long id) {
        return reviewMeetingService.getReviewMeetingById(id);
    }

    @PostMapping
    public ReviewMeetingCreateResponse createReviewMeeting(@Valid @RequestBody ReviewMeetingCreateRequest request) {
        return reviewMeetingService.createReviewMeeting(request);
    }

    @PutMapping("/{id}")
    public ReviewMeetingUpdateResponse updateReviewMeeting(
            @PathVariable Long id,
            @Valid @RequestBody ReviewMeetingUpdateRequest request) {
        return reviewMeetingService.updateReviewMeeting(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteReviewMeeting(
            @PathVariable Long id) {
        reviewMeetingService.deleteReviewMeeting(id);
    }

}
