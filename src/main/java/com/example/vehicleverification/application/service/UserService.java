package com.example.vehicleverification.application.service;

import java.util.List;

import com.example.vehicleverification.dto.user.*;

public interface UserService {

    public List<UserDto> getUserAll();

    public UserDetailResponse getUserById(Long id);

    public UserCreateResponse createUser(UserCreateRequest request);

    public void deleteUser(Long id);

}
