package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.issue.IssueCreateRequest;
import com.example.vehicleverification.application.dto.issue.IssueCreateResponse;
import com.example.vehicleverification.application.dto.issue.IssueDetailResponse;
import com.example.vehicleverification.application.dto.issue.IssueDto;
import com.example.vehicleverification.application.dto.issue.IssueUpdateRequest;
import com.example.vehicleverification.application.dto.issue.IssueUpdateResponse;
import com.example.vehicleverification.domain.entity.Issue;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.IssueRepository;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    private final ReviewMeetingRepository reviewMeetingRepository;
    private final UserRepository userRepository;

    public IssueServiceImpl(
            IssueRepository issueRepository,
            ReviewMeetingRepository reviewMeetingRepository,
            UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.reviewMeetingRepository = reviewMeetingRepository;
        this.userRepository = userRepository;
    }

    private IssueDto convertToDto(Issue issue) {
        return new IssueDto(
                issue.getId(),
                issue.getStatus(),
                issue.getContent(),
                issue.getReviewMeeting().getId(),
                issue.getReviewMeeting().getTitle(),
                issue.getReporter().getId(),
                issue.getReporter().getDisplayName(),
                issue.getCreatedAt());
    }

    @Override
    public List<IssueDto> getIssueAll(Long reviewMeetingId, String status) {
        if (reviewMeetingId != null && status != null) {
            return issueRepository.findByReviewMeetingIdAndStatus(reviewMeetingId, status).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (reviewMeetingId != null) {
            return issueRepository.findByReviewMeetingId(reviewMeetingId).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (status != null) {
            return issueRepository.findByStatus(status).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return issueRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
        }
    }

    @Override
    public IssueDetailResponse getIssueById(Long id) {
        Issue issue = issueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        return new IssueDetailResponse(
                issue.getId(),
                issue.getStatus(),
                issue.getContent(),
                issue.getAnswer(),
                issue.getResolvedAt(),
                issue.getReviewMeeting().getId(),
                issue.getReviewMeeting().getTitle(),
                issue.getReporter().getId(),
                issue.getReporter().getDisplayName(),
                issue.getAnswerer() != null ? issue.getAnswerer().getId() : null,
                issue.getAnswerer() != null ? issue.getAnswerer().getDisplayName() : null,
                issue.getCreatedAt(),
                issue.getVersion());
    }

    @Override
    @Transactional
    public IssueCreateResponse createIssue(IssueCreateRequest request) {
        ReviewMeeting reviewMeeting = reviewMeetingRepository
                .findById(request.getReviewMeetingId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getReviewMeetingId()));
        User reporter = userRepository
                .findById(request.getReporterId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getReporterId()));

        Issue issue = new Issue(reviewMeeting, request.getContent(), reporter, null);

        Issue saved = issueRepository.save(issue);

        return new IssueCreateResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getContent(),
                saved.getReviewMeeting().getId(),
                saved.getReviewMeeting().getTitle(),
                saved.getReporter().getId(),
                saved.getReporter().getDisplayName(),
                saved.getCreatedAt());
    }

    @Override
    @Transactional
    public IssueUpdateResponse updateIssue(Long id, IssueUpdateRequest request) {
        Issue issue = issueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        if (!issue.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException();
        }

        // 部分更新（PATCH）: リクエストに載っていない（null）項目は既存値のまま据え置く
        if (request.getAnswer() != null) {
            issue.setAnswer(request.getAnswer());
        }
        if (request.getResolvedAt() != null) {
            issue.setResolvedAt(request.getResolvedAt());
        }
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getAnswererId() != null) {
            User answerer = userRepository
                    .findById(request.getAnswererId())
                    .orElseThrow(() -> new ResourceNotFoundException(request.getAnswererId()));
            issue.setAnswerer(answerer);
        }

        Issue saved = issueRepository.saveAndFlush(issue);
        return new IssueUpdateResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getContent(),
                saved.getAnswer(),
                saved.getResolvedAt(),
                saved.getReviewMeeting().getId(),
                saved.getReviewMeeting().getTitle(),
                saved.getReporter().getId(),
                saved.getReporter().getDisplayName(),
                saved.getAnswerer() != null ? saved.getAnswerer().getId() : null,
                saved.getAnswerer() != null ? saved.getAnswerer().getDisplayName() : null,
                saved.getCreatedAt(),
                saved.getVersion());
    }

    @Override
    @Transactional
    public void deleteIssue(Long id) {
        Issue issue = issueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        issueRepository.delete(issue);
    }
}
