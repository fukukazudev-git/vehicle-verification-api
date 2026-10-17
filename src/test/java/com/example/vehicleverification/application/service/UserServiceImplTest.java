package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.vehicleverification.application.dto.user.PasswordChangeRequest;
import com.example.vehicleverification.application.dto.user.PasswordResetRequest;
import com.example.vehicleverification.application.dto.user.UserCreateRequest;
import com.example.vehicleverification.application.dto.user.UserCreateResponse;
import com.example.vehicleverification.application.dto.user.UserDetailResponse;
import com.example.vehicleverification.application.dto.user.UserDto;
import com.example.vehicleverification.application.dto.user.UserRoleResponse;
import com.example.vehicleverification.application.dto.user.UserUpdateRequest;
import com.example.vehicleverification.application.dto.user.UserUpdateResponse;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.entity.UserRole;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.AttachmentRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User createDummyUser(Long id, UserRole role) {
        User user = new User("user" + id, "pass", "表示名" + id, role, "開発部");
        user.setId(id);
        return user;
    }

    @Test
    void getUserRoles_全ロールをcodeと日本語表示名で返す() {
        List<UserRoleResponse> result = userService.getUserRoles();

        assertThat(result)
                .extracting(UserRoleResponse::getCode, UserRoleResponse::getDisplayName)
                .containsExactly(tuple("ADMIN", "管理者"), tuple("MEMBER", "メンバー"));
    }

    @Test
    void changePassword_現在のパスワードが一致しない場合_IllegalArgumentExceptionを投げる() {
        User user = createDummyUser(1L, UserRole.MEMBER);
        String wrongCurrentPassword = "wrongCurrentPassword";
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword(wrongCurrentPassword);
        passwordChangeRequest.setNewPassword("newPassword");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(wrongCurrentPassword, user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, passwordChangeRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_現在のパスワードが一致する場合_エンコードした新しいパスワードで更新する() {
        User user = createDummyUser(1L, UserRole.MEMBER);
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword("currentPassword");
        passwordChangeRequest.setNewPassword("newPassword");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("currentPassword", user.getPassword())).willReturn(true);
        given(passwordEncoder.encode("newPassword")).willReturn("encodedNewPassword");

        userService.changePassword(1L, passwordChangeRequest);

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void resetPassword_存在するIDを指定した場合_エンコードした新しいパスワードで更新する() {
        User user = createDummyUser(1L, UserRole.MEMBER);
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setNewPassword("newPassword");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPassword")).willReturn("encodedNewPassword");

        userService.resetPassword(1L, passwordResetRequest);

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void resetPassword_存在しないIDを指定した場合_ResourceNotFoundExceptionを投げる() {
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setNewPassword("newPassword");

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(999L, passwordResetRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getUserAll_roleのコード値と日本語表示名を両方返す() {
        given(userRepository.findAll())
                .willReturn(List.of(createDummyUser(1L, UserRole.ADMIN), createDummyUser(2L, UserRole.MEMBER)));

        List<UserDto> result = userService.getUserAll();

        assertThat(result)
                .extracting(UserDto::getRole, UserDto::getRoleDisplayName)
                .containsExactly(tuple(UserRole.ADMIN, "管理者"), tuple(UserRole.MEMBER, "メンバー"));
    }

    @Test
    void getUserById_roleのコード値と日本語表示名を両方返す() {
        User user = createDummyUser(1L, UserRole.MEMBER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(attachmentRepository.findByUserId(1L)).willReturn(List.of());

        UserDetailResponse result = userService.getUserById(1L);

        assertThat(result.getRole()).isEqualTo(UserRole.MEMBER);
        assertThat(result.getRoleDisplayName()).isEqualTo("メンバー");
    }

    @Test
    void createUser_リクエストのroleがそのまま保存される() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newUser");
        request.setPassword("rawPassword");
        request.setDisplayName("新規ユーザー");
        request.setRole(UserRole.ADMIN);
        request.setDepartment("開発部");

        given(userRepository.existsByUsername("newUser")).willReturn(false);
        given(passwordEncoder.encode("rawPassword")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserCreateResponse result = userService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateUser_roleを変更できる() {
        User user = createDummyUser(1L, UserRole.MEMBER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setDisplayName("更新後表示名");
        request.setRole(UserRole.ADMIN);
        request.setDepartment("品質保証部");

        UserUpdateResponse result = userService.updateUser(1L, request);

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }
}
