package com.example.vehicleverification.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.vehicleverification.application.service.UserService;
import com.example.vehicleverification.application.dto.user.UserCreateRequest;
import com.example.vehicleverification.application.dto.user.UserCreateResponse;
import com.example.vehicleverification.application.dto.user.UserDetailResponse;
import com.example.vehicleverification.application.dto.user.UserDto;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUserList() {
        return userService.getUserAll();
    }

    @GetMapping("/{id}")
    public UserDetailResponse getUserById(
            @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public UserCreateResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
