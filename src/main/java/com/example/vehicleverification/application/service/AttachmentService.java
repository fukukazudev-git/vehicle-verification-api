package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.attachment.AttachmentDownloadResponse;
import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.attachment.AttachmentUploadRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    public AttachmentDownloadResponse getDownloadUrl(Long id);

    public AttachmentDto upload(AttachmentUploadRequest request, MultipartFile file, Long uploadedById);

    public void delete(Long id);
}
