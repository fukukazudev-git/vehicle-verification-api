package com.example.vehicleverification.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.repository.UserRepository;
import com.example.vehicleverification.presentation.dto.user.*;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt());
    }

    @Override
    public List<UserDto> getUserAll() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException());

        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt());
    }

    @Override
    public UserCreateResponse createUser(UserCreateRequest request) {
        User user = new User(
                request.getUsername(),
                request.getPassword(),
                request.getDisplayName(),
                request.getRole());

        User saved = userRepository.save(user);
        return new UserCreateResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getDisplayName(),
                saved.getRole(),
                saved.getCreatedAt());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException());

        userRepository.delete(user);

    }

}
