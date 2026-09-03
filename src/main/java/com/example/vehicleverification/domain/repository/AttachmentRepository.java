package com.example.vehicleverification.domain.repository;

import com.example.vehicleverification.domain.entity.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByReviewMeetingId(Long reviewMeetingId);

    List<Attachment> findByTestRecordId(Long testRecordId);

    List<Attachment> findByUserId(Long userId);
}
