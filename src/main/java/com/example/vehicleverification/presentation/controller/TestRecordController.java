package com.example.vehicleverification.presentation.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDetailResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDto;
import com.example.vehicleverification.application.service.TestRecordService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateRequest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/test-records")
public class TestRecordController {

    private final TestRecordService testRecordService;

    public TestRecordController(TestRecordService testRecordService) {
        this.testRecordService = testRecordService;
    }

    @GetMapping
    public List<TestRecordDto> getTestRecordsByReviewMeeting(
            @RequestParam(required = false) Long reviewMeetingId,
            @RequestParam(required = false) String result) {
        return testRecordService.getTestRecordsByReviewMeeting(reviewMeetingId, result);
    }

    @GetMapping("/{id}")
    public TestRecordDetailResponse getTestRecordById(
            @PathVariable Long id) {
        return testRecordService.getTestRecordById(id);
    }

    @PostMapping
    public TestRecordCreateResponse createTestRecord(
            @Valid @RequestBody TestRecordCreateRequest request) {
        return testRecordService.createTestRecord(request);
    }

    @PutMapping("/{id}")
    public TestRecordUpdateResponse updateTestRecord(
            @PathVariable Long id,
            @Valid @RequestBody TestRecordUpdateRequest request) {
        return testRecordService.updateTestRecord(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTestRecord(
            @PathVariable Long id) {
        testRecordService.deleteTestRecord(id);
    }

}
