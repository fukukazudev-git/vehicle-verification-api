package com.example.vehicleverification.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Bearer検証
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Authorizationヘッダがない、またはBearerトークンでない場合はそのまま次のフィルタに処理を渡す
        if (header == null || !header.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;

        } else {

            String token = header.substring(7);

            // Bearer以降を切り出したトークン文字列に対して、署名検証・有効期限検証を行う
            if (!jwtTokenProvider.validateToken(token)) {

                filterChain.doFilter(request, response);
                return;

            } else {

                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Spring Securityの認証情報をSecurityContextにセットする
                // 2引数版のコンストラクタは未認証扱いのため、3引数版のコンストラクタを使って認証済み扱いにする
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, // principal (本人)
                        null, // credentials (パスワードは不要)
                        userDetails.getAuthorities()); // 権限リスト

                // IP 等の付帯情報も載せる
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // トークン完成後にContextに入れる
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 次のフィルタに処理を渡す
        filterChain.doFilter(request, response);
    }
}
