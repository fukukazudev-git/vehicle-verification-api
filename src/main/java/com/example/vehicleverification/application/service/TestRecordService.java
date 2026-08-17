package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDetailResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDto;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateResponse;
import java.util.List;

public interface TestRecordService {

    List<TestRecordDto> getTestRecordsByReviewMeeting(Long reviewMeetingId, String result);

    TestRecordDetailResponse getTestRecordById(Long id);

    TestRecordCreateResponse createTestRecord(TestRecordCreateRequest request);

    TestRecordUpdateResponse updateTestRecord(Long id, TestRecordUpdateRequest request);

    void deleteTestRecord(Long id);
}
