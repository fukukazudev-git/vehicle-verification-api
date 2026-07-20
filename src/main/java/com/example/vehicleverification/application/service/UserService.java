package com.example.vehicleverification.application.service;

import java.util.List;

import com.example.vehicleverification.application.dto.user.UserCreateRequest;
import com.example.vehicleverification.application.dto.user.UserCreateResponse;
import com.example.vehicleverification.application.dto.user.UserDetailResponse;
import com.example.vehicleverification.application.dto.user.UserDto;

public interface UserService {

    public List<UserDto> getUserAll();

    public UserDetailResponse getUserById(Long id);

    public UserCreateResponse createUser(UserCreateRequest request);

    public void deleteUser(Long id);

}
