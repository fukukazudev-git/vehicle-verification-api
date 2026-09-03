package com.example.vehicleverification.presentation.controller;

import com.example.vehicleverification.application.dto.attachment.AttachmentDownloadResponse;
import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.attachment.AttachmentUploadRequest;
import com.example.vehicleverification.application.service.AttachmentService;
import com.example.vehicleverification.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/{id}/download")
    public AttachmentDownloadResponse getDownloadUrl(@PathVariable Long id) {
        return attachmentService.getDownloadUrl(id);
    }

    @PostMapping
    public AttachmentDto uploadAttachment(
            @RequestPart("request") AttachmentUploadRequest request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return attachmentService.upload(request, file, principal.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        attachmentService.delete(id);
    }
}
