package com.example.vehicleverification.presentation.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.vehicleverification.application.dto.auth.LoginRequest;
import com.example.vehicleverification.application.dto.auth.LoginResponse;
import com.example.vehicleverification.infrastructure.security.JwtTokenProvider;

import jakarta.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {

        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;

    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        // 入力用の認証トークンを作成(未認証・authorityは空)
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword());

        // 認証実行
        Authentication authentication = authenticationManager.authenticate(authRequest);

        // JWT発行
        String token = jwtTokenProvider.generateToken(authentication.getName());

        // roleを取り出してLoginResponseを返す
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""); // ROLE_を削除して返す

        return new LoginResponse(token, authentication.getName(), role);

    }

}
