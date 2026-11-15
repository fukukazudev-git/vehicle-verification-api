package com.example.vehicleverification.application.service;

import com.example.vehicleverification.application.dto.auth.LoginRequest;
import com.example.vehicleverification.application.dto.auth.LoginResponse;
import com.example.vehicleverification.infrastructure.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    // Configで@Bean化したAuthenticationManagerをDIして認証処理を行う
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // ログイン試行回数をチェック
        loginAttemptService.checkLocked(request.getUsername());

        // 入力用の認証トークンを作成(未認証・authorityは空)
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        try {
            // AuthenticationManagerに認証トークンを渡すと、CustomUserDetailsServiceが呼ばれ、DBのユーザ情報と照合される
            Authentication authentication = authenticationManager.authenticate(authRequest);

            // 認証成功時はAuthenticationManagerが返すAuthenticationに、認証済みのユーザ情報と権限が入っている
            String token = jwtTokenProvider.generateToken(authentication.getName());

            // Spring Securityの認証情報から権限を取り出す。GrantedAuthorityの文字列はROLE_XXX形式なので、ROLE_ を取り除く
            String role = authentication
                    .getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority()
                    .replace("ROLE_", "");

            // 認証成功時はログイン試行回数をリセット
            loginAttemptService.recordSuccess(request.getUsername());
            return new LoginResponse(token, authentication.getName(), role);
        } catch (BadCredentialsException e) {
            // 認証失敗時はログイン試行回数を記録
            loginAttemptService.recordFailure(request.getUsername());
            throw e;
        }
    }
}
