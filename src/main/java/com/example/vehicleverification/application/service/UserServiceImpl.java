package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.user.PasswordChangeRequest;
import com.example.vehicleverification.application.dto.user.PasswordResetRequest;
import com.example.vehicleverification.application.dto.user.UserCreateRequest;
import com.example.vehicleverification.application.dto.user.UserCreateResponse;
import com.example.vehicleverification.application.dto.user.UserDetailResponse;
import com.example.vehicleverification.application.dto.user.UserDto;
import com.example.vehicleverification.application.dto.user.UserRoleResponse;
import com.example.vehicleverification.application.dto.user.UserUpdateRequest;
import com.example.vehicleverification.application.dto.user.UserUpdateResponse;
import com.example.vehicleverification.domain.entity.Attachment;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.entity.UserRole;
import com.example.vehicleverification.domain.exception.DuplicateResourceException;
import com.example.vehicleverification.domain.exception.ResourceInUseException;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.AttachmentRepository;
import com.example.vehicleverification.domain.repository.IssueRepository;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.TestRecordRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 参照系を既定とし、更新系のメソッドで個別に上書きする
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ReviewMeetingRepository reviewMeetingRepository;
    private final IssueRepository issueRepository;
    private final TestRecordRepository testRecordRepository;
    private final AttachmentRepository attachmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AttachmentRepository attachmentRepository,
            ReviewMeetingRepository reviewMeetingRepository,
            IssueRepository issueRepository,
            TestRecordRepository testRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.attachmentRepository = attachmentRepository;
        this.reviewMeetingRepository = reviewMeetingRepository;
        this.issueRepository = issueRepository;
        this.testRecordRepository = testRecordRepository;
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getRole().getDisplayName(),
                user.getCreatedAt(),
                user.getDepartment());
    }

    private AttachmentDto convertToAttachmentDto(Attachment attachment) {
        return new AttachmentDto(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getStoredPath(),
                attachment.getReviewMeeting() != null
                        ? attachment.getReviewMeeting().getId()
                        : null,
                attachment.getTestRecord() != null ? attachment.getTestRecord().getId() : null,
                attachment.getUser() != null ? attachment.getUser().getId() : null,
                attachment.getUploadedBy().getId(),
                attachment.getUploadedBy().getDisplayName(),
                attachment.getUploadedAt());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("現在のパスワードが正しくありません。");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Override
    public List<UserDto> getUserAll() {
        return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<UserRoleResponse> getUserRoles() {
        return Arrays.stream(UserRole.values())
                .map(role -> new UserRoleResponse(role.name(), role.getDisplayName()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        List<Attachment> attachments = attachmentRepository.findByUserId(user.getId());

        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getRole().getDisplayName(),
                user.getCreatedAt(),
                user.getDepartment(),
                attachments.stream().map(this::convertToAttachmentDto).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void resetPassword(Long id, PasswordResetRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Override
    @Transactional
    public UserCreateResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("username", "このユーザー名は既に登録されています");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()), // パスワードはハッシュ化して保存する
                request.getDisplayName(),
                request.getRole(),
                request.getDepartment());

        User saved = userRepository.save(user);
        return new UserCreateResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getDisplayName(),
                saved.getRole(),
                saved.getCreatedAt(),
                saved.getDepartment());
    }

    @Override
    @Transactional
    public UserUpdateResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        user.setDisplayName(request.getDisplayName());
        user.setRole(request.getRole());
        user.setDepartment(request.getDepartment());

        User saved = userRepository.save(user);
        return new UserUpdateResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getDisplayName(),
                saved.getRole(),
                saved.getDepartment(),
                saved.getCreatedAt());
    }

    private void validateUserNotInUse(User user) {
        if (reviewMeetingRepository.existsByOrganizerId(user.getId())) {
            throw new ResourceInUseException("このユーザーは検証会の主催者として登録されているため削除できません。");
        }
        if (issueRepository.existsByReporterId(user.getId())) {
            throw new ResourceInUseException("このユーザーは指摘事項の報告者として登録されているため削除できません。");
        }
        if (issueRepository.existsByAnswererId(user.getId())) {
            throw new ResourceInUseException("このユーザーは指摘事項の回答者として登録されているため削除できません。");
        }
        if (testRecordRepository.existsByRecordedById(user.getId())) {
            throw new ResourceInUseException("このユーザーは試験記録の記録者として登録されているため削除できません。");
        }
        if (attachmentRepository.existsByUserId(user.getId())) {
            throw new ResourceInUseException("このユーザーには添付ファイルが登録されているため削除できません。");
        }
        if (attachmentRepository.existsByUploadedById(user.getId())) {
            throw new ResourceInUseException("このユーザーは添付ファイルのアップロード者として登録されているため削除できません。");
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        validateUserNotInUse(user);

        userRepository.delete(user);
    }
}
