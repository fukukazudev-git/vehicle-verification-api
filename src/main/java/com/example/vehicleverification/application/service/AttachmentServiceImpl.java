package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.attachment.AttachmentDownloadResponse;
import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.attachment.AttachmentUploadRequest;
import com.example.vehicleverification.domain.repository.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public AttachmentDownloadResponse getDownloadUrl(Long id) {}

    @Override
    @Transactional
    public AttachmentDto upload(AttachmentUploadRequest request, MultipartFile file, Long uploadedById) {}

    @Override
    @Transactional
    public void delete(Long id) {}
}
