package com.example.vehicleverification.presentation.controller;

import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDetailResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDto;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestResultResponse;
import com.example.vehicleverification.application.service.TestRecordService;
import com.example.vehicleverification.domain.entity.TestResult;
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
@RequestMapping("/api/test-records")
public class TestRecordController {

    private final TestRecordService testRecordService;

    public TestRecordController(TestRecordService testRecordService) {
        this.testRecordService = testRecordService;
    }

    @GetMapping
    public List<TestRecordDto> getTestRecordsByReviewMeeting(
            @RequestParam(required = false) Long reviewMeetingId, @RequestParam(required = false) TestResult result) {
        return testRecordService.getTestRecordsByReviewMeeting(reviewMeetingId, result);
    }

    // 試験結果の全語彙(code + 日本語表示名)を返すマスタAPI。/{id} より前に定義し、リテラルパスを優先させる
    @GetMapping("/results")
    public List<TestResultResponse> getTestRecordResults() {
        return testRecordService.getTestRecordResults();
    }

    @GetMapping("/{id}")
    public TestRecordDetailResponse getTestRecordById(@PathVariable Long id) {
        return testRecordService.getTestRecordById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestRecordCreateResponse createTestRecord(@Valid @RequestBody TestRecordCreateRequest request) {
        return testRecordService.createTestRecord(request);
    }

    @PutMapping("/{id}")
    public TestRecordUpdateResponse updateTestRecord(
            @PathVariable Long id, @Valid @RequestBody TestRecordUpdateRequest request) {
        return testRecordService.updateTestRecord(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTestRecord(@PathVariable Long id) {
        testRecordService.deleteTestRecord(id);
    }
}
