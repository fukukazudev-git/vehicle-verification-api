package com.example.vehicleverification.presentation.controller;

import com.example.vehicleverification.application.dto.issue.IssueCreateRequest;
import com.example.vehicleverification.application.dto.issue.IssueCreateResponse;
import com.example.vehicleverification.application.dto.issue.IssueDetailResponse;
import com.example.vehicleverification.application.dto.issue.IssueDto;
import com.example.vehicleverification.application.dto.issue.IssueUpdateRequest;
import com.example.vehicleverification.application.dto.issue.IssueUpdateResponse;
import com.example.vehicleverification.application.service.IssueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public List<IssueDto> getIssueAll(
            @RequestParam(required = false) Long reviewMeetingId, @RequestParam(required = false) String status) {
        return issueService.getIssueAll(reviewMeetingId, status);
    }

    @GetMapping("/{id}")
    public IssueDetailResponse getIssueById(@PathVariable Long id) {
        return issueService.getIssueById(id);
    }

    @PostMapping
    public IssueCreateResponse createIssue(@Valid @RequestBody IssueCreateRequest request) {
        return issueService.createIssue(request);
    }

    @PatchMapping("/{id}")
    public IssueUpdateResponse updateIssue(@PathVariable Long id, @Valid @RequestBody IssueUpdateRequest request) {
        return issueService.updateIssue(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
    }
}
