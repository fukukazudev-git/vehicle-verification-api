package com.example.vehicleverification.application.dto.user;

import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.domain.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDetailResponse {

    private Long id;
    private String username;
    private String displayName;
    private UserRole role;
    private String roleDisplayName;
    private LocalDateTime createdAt;
    private String department;
    private List<AttachmentDto> attachments;
}
