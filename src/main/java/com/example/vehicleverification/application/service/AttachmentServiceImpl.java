package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.attachment.AttachmentDownloadResponse;
import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.attachment.AttachmentUploadRequest;
import com.example.vehicleverification.domain.entity.Attachment;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.TestRecord;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.AttachmentRepository;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.TestRecordRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final ReviewMeetingRepository reviewMeetingRepository;
    private final TestRecordRepository testRecordRepository;
    private final UserRepository userRepository;

    private final StorageService storageService;

    public AttachmentServiceImpl(
            AttachmentRepository attachmentRepository,
            ReviewMeetingRepository reviewMeetingRepository,
            TestRecordRepository testRecordRepository,
            UserRepository userRepository,
            StorageService storageService) {
        this.attachmentRepository = attachmentRepository;
        this.reviewMeetingRepository = reviewMeetingRepository;
        this.testRecordRepository = testRecordRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Override
    public AttachmentDownloadResponse getDownloadUrl(Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        String downloadUrl = storageService.getDownloadUrl(attachment.getStoredPath());
        return new AttachmentDownloadResponse(
                attachment.getFileName(), downloadUrl, storageService.getUrlExpiration() // Local→MAX/ S3→now+1h を実装側が返す
                );
    }

    @Override
    @Transactional
    public AttachmentDto upload(AttachmentUploadRequest request, MultipartFile file, Long uploadedById) {
        // 紐づけ先チェック
        if (request.getReviewMeetingId() == null && request.getTestRecordId() == null && request.getUserId() == null) {
            throw new IllegalArgumentException("紐づけ先(検証会・テスト・記録・ユーザー)を最低1つ選択してください");
        }
        // ファイル拡張子チェック
        List<String> permitted =
                List.of("pdf", "xlsx", "xls", "docx", "doc", "csv", "txt", "png", "jpg", "jpeg", "pptx", "zip");
        String originalFileName = file.getOriginalFilename();
        String ext = originalFileName != null && originalFileName.contains(".")
                ? originalFileName
                        .substring(originalFileName.lastIndexOf('.') + 1)
                        .toLowerCase()
                : "";
        if (!permitted.contains(ext)) {
            throw new IllegalArgumentException("許可されていないファイル形式です: " + ext);
        }

        // サイズチェック
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("ファイルサイズが上限(10MB)を超えています: " + file.getSize());
        }

        // 紐づけ先の存在チェック
        ReviewMeeting reviewMeeting = null;
        TestRecord testRecord = null;
        User user = null;

        if (request.getReviewMeetingId() != null) {
            reviewMeeting = reviewMeetingRepository
                    .findById(request.getReviewMeetingId())
                    .orElseThrow(() -> new ResourceNotFoundException(request.getReviewMeetingId()));
        }
        if (request.getTestRecordId() != null) {
            testRecord = testRecordRepository
                    .findById(request.getTestRecordId())
                    .orElseThrow(() -> new ResourceNotFoundException(request.getTestRecordId()));
        }

        if (request.getUserId() != null) {
            user = userRepository
                    .findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(request.getUserId()));
        }

        // ファイルをストレージに保存するためのキーを生成
        String key = "attachments/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        User uploadedBy =
                userRepository.findById(uploadedById).orElseThrow(() -> new ResourceNotFoundException(uploadedById));

        String storedPath = storageService.store(file, key);

        Attachment attachment = new Attachment(
                reviewMeeting, testRecord, user, file.getOriginalFilename(), storedPath, ext, uploadedBy);

        Attachment saved = attachmentRepository.save(attachment);

        return new AttachmentDto(
                saved.getId(),
                saved.getFileName(),
                saved.getFileType(),
                saved.getStoredPath(),
                saved.getReviewMeeting() != null ? saved.getReviewMeeting().getId() : null,
                saved.getTestRecord() != null ? saved.getTestRecord().getId() : null,
                saved.getUser() != null ? saved.getUser().getId() : null,
                saved.getUploadedBy() != null ? saved.getUploadedBy().getId() : null,
                saved.getUploadedBy() != null ? saved.getUploadedBy().getDisplayName() : null,
                saved.getUploadedAt());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        storageService.delete(attachment.getStoredPath());
        attachmentRepository.delete(attachment);
    }
}
