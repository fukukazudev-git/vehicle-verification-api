package com.example.vehicleverification.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.DuplicateResourceException;
import com.example.vehicleverification.domain.repository.UserRepository;
import com.example.vehicleverification.application.dto.user.UserCreateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.vehicleverification.application.dto.user.UserCreateResponse;
import com.example.vehicleverification.application.dto.user.UserDetailResponse;
import com.example.vehicleverification.application.dto.user.UserDto;
import com.example.vehicleverification.application.dto.user.UserUpdateRequest;
import com.example.vehicleverification.application.dto.user.UserUpdateResponse;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;

@Service
// 参照系を既定とし、更新系のメソッドで個別に上書きする
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // パスワードのハッシュ化に使用するPasswordEncoderをDIする
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getDepartment());
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
                .orElseThrow(() -> new ResourceNotFoundException(id));

        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getDepartment());
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

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

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        userRepository.delete(user);

    }

}
