package com.example.vehicleverification.domain.repository;

import com.example.vehicleverification.domain.entity.TestRecord;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRecordRepository extends JpaRepository<TestRecord, Long> {

    @EntityGraph(attributePaths = {"reviewMeeting", "recordedBy"})
    List<TestRecord> findByReviewMeetingId(Long reviewMeetingId);

    @EntityGraph(attributePaths = {"reviewMeeting", "recordedBy"})
    List<TestRecord> findByReviewMeetingIdAndResult(Long reviewMeetingId, String result);

    @EntityGraph(attributePaths = {"reviewMeeting", "recordedBy"})
    List<TestRecord> findByResult(String result);

    @EntityGraph(attributePaths = {"reviewMeeting", "recordedBy"})
    @Override
    List<TestRecord> findAll();
}
