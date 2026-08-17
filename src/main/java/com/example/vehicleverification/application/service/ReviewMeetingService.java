package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDto;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingUpdateResponse;
import java.util.List;

public interface ReviewMeetingService {

    List<ReviewMeetingDto> getReviewMeetingAll(Long modelId, String status);

    ReviewMeetingDetailResponse getReviewMeetingById(Long id);

    ReviewMeetingCreateResponse createReviewMeeting(ReviewMeetingCreateRequest request);

    ReviewMeetingUpdateResponse updateReviewMeeting(Long id, ReviewMeetingUpdateRequest request);

    void deleteReviewMeeting(Long id);
}
