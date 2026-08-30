package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.issue.IssueCreateRequest;
import com.example.vehicleverification.application.dto.issue.IssueCreateResponse;
import com.example.vehicleverification.application.dto.issue.IssueDetailResponse;
import com.example.vehicleverification.application.dto.issue.IssueDto;
import com.example.vehicleverification.application.dto.issue.IssueUpdateRequest;
import com.example.vehicleverification.application.dto.issue.IssueUpdateResponse;
import java.util.List;

public interface IssueService {
    List<IssueDto> getIssueAll(Long reviewMeetingId, String status);

    IssueDetailResponse getIssueById(Long id);

    IssueCreateResponse createIssue(IssueCreateRequest request);

    IssueUpdateResponse updateIssue(Long id, IssueUpdateRequest request);

    void deleteIssue(Long id);
}
