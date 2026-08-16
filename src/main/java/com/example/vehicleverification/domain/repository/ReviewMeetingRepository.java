package com.example.vehicleverification.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.vehicleverification.domain.entity.ReviewMeeting;

public interface ReviewMeetingRepository extends JpaRepository<ReviewMeeting, Long> {

    @EntityGraph(attributePaths = { "model", "organizer" })
    List<ReviewMeeting> findByModelId(long modelId);

    @EntityGraph(attributePaths = { "model", "organizer" })
    List<ReviewMeeting> findByStatus(String status);

    @EntityGraph(attributePaths = { "model", "organizer" })
    List<ReviewMeeting> findByModelIdAndStatus(Long modelId, String status);

    @EntityGraph(attributePaths = { "model", "organizer" })
    @Override
    List<ReviewMeeting> findAll();

}
