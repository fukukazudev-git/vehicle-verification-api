package com.example.vehicleverification.domain.repository;

import com.example.vehicleverification.domain.entity.Issue;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    @EntityGraph(attributePaths = {"reviewMeeting", "reporter", "answerer"})
    List<Issue> findByReviewMeetingId(Long reviewMeetingId);

    @EntityGraph(attributePaths = {"reviewMeeting", "reporter", "answerer"})
    List<Issue> findByReviewMeetingIdAndStatus(Long reviewMeetingId, String status);

    @EntityGraph(attributePaths = {"reviewMeeting", "reporter", "answerer"})
    List<Issue> findByStatus(String status);

    @Override
    @EntityGraph(attributePaths = {"reviewMeeting", "reporter", "answerer"})
    List<Issue> findAll();
}
