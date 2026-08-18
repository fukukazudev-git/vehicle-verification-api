package com.example.vehicleverification.infrastructure.config;

import com.example.vehicleverification.infrastructure.security.JwtAccessDeniedHandler;
import com.example.vehicleverification.infrastructure.security.JwtAuthenticationEntryPoint;
import com.example.vehicleverification.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 認可設定、認証機構配線
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    // Springがパスワード照合に使用
    // ログイン時は入力された生パスワードをBCryptでハッシュ化して、DBのハッシュ済みパスワードと照合する
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthControllerがログイン認証に使用する
    // DaoAuthenticationProviderが使用するAuthenticationManagerを取り出して@Beanとして再公開
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Spring Securityの認可設定
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // httpBasic認証を無効化するとデフォルトでは403にフォールバックするため401を返すEntryPointを明示
                // 未認証は401、認可拒否(権限不足)は403を、いずれもJSONで返す
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .csrf(csrf -> csrf.disable()) // 認証情報はAuthorizationヘッダーで明示送信&ステートレスであるためCSRF対策無効化
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT認証のため認証状態はサーバに保持せず、JWTで自己申告させる
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasRole("ADMIN") // ユーザ登録は管理者のみ許可
                        .requestMatchers(HttpMethod.PUT, "/api/users/*")
                        .hasRole("ADMIN") // ユーザ更新は管理者のみ許可
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*")
                        .hasRole("ADMIN") // ユーザ削除は管理者のみ許可
                        .anyRequest() // マッチャは上から順に評価されるため具体的なルールをこの位置より前に置く
                        .authenticated()) // /api/auth/**以外は認証必須
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class); // 標準の認証フィルタの前にJWT認証フィルタを挿入する

        return http.build();
    }
}
