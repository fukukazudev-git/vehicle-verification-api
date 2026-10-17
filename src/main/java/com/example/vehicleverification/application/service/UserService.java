package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.user.PasswordChangeRequest;
import com.example.vehicleverification.application.dto.user.PasswordResetRequest;
import com.example.vehicleverification.application.dto.user.UserCreateRequest;
import com.example.vehicleverification.application.dto.user.UserCreateResponse;
import com.example.vehicleverification.application.dto.user.UserDetailResponse;
import com.example.vehicleverification.application.dto.user.UserDto;
import com.example.vehicleverification.application.dto.user.UserRoleResponse;
import com.example.vehicleverification.application.dto.user.UserUpdateRequest;
import com.example.vehicleverification.application.dto.user.UserUpdateResponse;
import java.util.List;

public interface UserService {

    public void changePassword(Long userId, PasswordChangeRequest request);

    public List<UserDto> getUserAll();

    public List<UserRoleResponse> getUserRoles();

    public UserDetailResponse getUserById(Long id);

    public void resetPassword(Long id, PasswordResetRequest request);

    public UserCreateResponse createUser(UserCreateRequest request);

    public UserUpdateResponse updateUser(Long id, UserUpdateRequest request);

    public void deleteUser(Long id);
}
