package com.example.vehicleverification.presentation.controller;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingStatusResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateResponse;
import com.example.vehicleverification.application.dto.summary.IssueSummaryResponse;
import com.example.vehicleverification.application.dto.summary.TestSummaryResponse;
import com.example.vehicleverification.application.service.ReviewMeetingService;
import com.example.vehicleverification.domain.entity.ReviewMeetingStatus;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review-meetings")
public class ReviewMeetingController {

    private final ReviewMeetingService reviewMeetingService;

    public ReviewMeetingController(ReviewMeetingService reviewMeetingService) {
        this.reviewMeetingService = reviewMeetingService;
    }

    @GetMapping
    public List<ReviewMeetingDto> getReviewMeetingAll(
            @RequestParam(required = false) Long modelId, @RequestParam(required = false) ReviewMeetingStatus status) {
        return reviewMeetingService.getReviewMeetingAll(modelId, status);
    }

    // ステータスの全語彙(code + 日本語表示名)を返すマスタAPI。/{id} より前に定義し、リテラルパスを優先させる
    @GetMapping("/statuses")
    public List<ReviewMeetingStatusResponse> getReviewMeetingStatuses() {
        return reviewMeetingService.getReviewMeetingStatuses();
    }

    @GetMapping("/{id}")
    public ReviewMeetingDetailResponse getReviewMeetingById(@PathVariable Long id) {
        return reviewMeetingService.getReviewMeetingById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewMeetingCreateResponse createReviewMeeting(@Valid @RequestBody ReviewMeetingCreateRequest request) {
        return reviewMeetingService.createReviewMeeting(request);
    }

    @PutMapping("/{id}")
    public ReviewMeetingUpdateResponse updateReviewMeeting(
            @PathVariable Long id, @Valid @RequestBody ReviewMeetingUpdateRequest request) {
        return reviewMeetingService.updateReviewMeeting(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReviewMeeting(@PathVariable Long id) {
        reviewMeetingService.deleteReviewMeeting(id);
    }

    @GetMapping("/{id}/test-summary")
    public TestSummaryResponse getTestSummary(@PathVariable Long id) {
        return reviewMeetingService.getTestSummary(id);
    }

    @GetMapping("/{id}/issue-summary")
    public IssueSummaryResponse getIssueSummary(@PathVariable Long id) {
        return reviewMeetingService.getIssueSummary(id);
    }
}
