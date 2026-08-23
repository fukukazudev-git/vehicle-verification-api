package com.example.vehicleverification.presentation.controller;

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
import org.springframework.web.bind.annotation.RestController;

// ログイン認証を受け付けるController
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Configで@Bean化したAuthenticationManagerをDIし、AuthControllerから認証処理を呼び出す
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {

        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        // 入力用の認証トークンを作成(未認証・authorityは空)
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        // AuthenticationManagerに認証トークンを渡すと、CustomUserDetailsServiceが呼ばれ、DBのユーザ情報と照合される
        Authentication authentication = authenticationManager.authenticate(authRequest);

        // 認証成功時はAuthenticationManagerが返すAuthenticationに、認証済みのユーザ情報と権限が入っている
        String token = jwtTokenProvider.generateToken(authentication.getName());

        // Spring Securityの認証情報から権限を取り出す。GrantedAuthorityの文字列はROLE_XXX形式なので、ROLE_を削除する
        String role =
                authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return new LoginResponse(token, authentication.getName(), role);
    }
}
