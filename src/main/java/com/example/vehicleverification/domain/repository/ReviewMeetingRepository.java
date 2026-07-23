package com.example.vehicleverification.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.vehicleverification.domain.entity.ReviewMeeting;

public interface ReviewMeetingRepository extends JpaRepository<ReviewMeeting, Long> {

    List<ReviewMeeting> findByModelId(long modelId);

    List<ReviewMeeting> findByStatus(String status);

    List<ReviewMeeting> findByModelIdAndStatus(Long modelId, String status);

}
