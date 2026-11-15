package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.auth.LoginRequest;
import com.example.vehicleverification.application.dto.auth.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
