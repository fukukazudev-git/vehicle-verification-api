package com.example.vehicleverification.application.service;

import java.util.List;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDetailResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordDto;

public interface TestRecordService {

    List<TestRecordDto> getTestRecordsByReviewMeeting(Long reviewMeetingId, String result);

    TestRecordDetailResponse getTestRecordById(Long id);

    TestRecordCreateResponse createTestRecord(TestRecordCreateRequest request);

    TestRecordUpdateResponse updateTestRecord(Long id, TestRecordUpdateRequest request);

    void deleteTestRecord(Long id);

}
