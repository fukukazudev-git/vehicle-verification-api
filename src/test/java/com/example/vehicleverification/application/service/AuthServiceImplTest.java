package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.vehicleverification.application.dto.auth.LoginRequest;
import com.example.vehicleverification.application.dto.auth.LoginResponse;
import com.example.vehicleverification.domain.exception.LoginLockedException;
import com.example.vehicleverification.infrastructure.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthServiceImpl service;

    private LoginRequest request;

    @BeforeEach
    void setUp() {
        request = new LoginRequest();
        request.setUsername("mockuser");
        request.setPassword("password");
    }

    @Test
    void login_ロック中はauthenticateが呼ばれない() {

        willThrow(new LoginLockedException(10)).given(loginAttemptService).checkLocked("mockuser");

        assertThatThrownBy(() -> service.login(request)).isInstanceOf(LoginLockedException.class);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_認証失敗時にBadCredentialsExceptionを投げる() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        willThrow(exception).given(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .isSameAs(exception);

        verify(loginAttemptService).recordFailure("mockuser");
        verify(loginAttemptService, never()).recordSuccess(any());
    }

    @Test
    void login_成功時にrecordSuccessが呼ばれる() {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "mockuser", null, List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
        given(authenticationManager.authenticate(any())).willReturn(authToken);
        given(jwtTokenProvider.generateToken("mockuser")).willReturn("testToken");
        LoginResponse response = service.login(request);

        assertThat(response.getToken()).isEqualTo("testToken");
        assertThat(response.getUsername()).isEqualTo("mockuser");
        assertThat(response.getRole()).isEqualTo("MEMBER");

        verify(loginAttemptService).recordSuccess("mockuser");
        verify(loginAttemptService, never()).recordFailure("mockuser");
    }
}
