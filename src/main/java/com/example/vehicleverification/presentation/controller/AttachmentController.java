package com.example.vehicleverification.presentation.controller;

import com.example.vehicleverification.application.dto.attachment.AttachmentDownloadResponse;
import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.attachment.AttachmentUploadRequest;
import com.example.vehicleverification.application.service.AttachmentService;
import com.example.vehicleverification.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    // 紐づけ先IDとファイルをフォーム項目としてまとめて受け取る。JSONパートはパート単位のContent-Type指定が必要で、
    // Swagger UIなどから送ると415になるため使わない
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentDto uploadAttachment(
            @Valid @ModelAttribute AttachmentUploadRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return attachmentService.upload(request, request.getFile(), principal.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attachmentService.delete(id);
    }
}
