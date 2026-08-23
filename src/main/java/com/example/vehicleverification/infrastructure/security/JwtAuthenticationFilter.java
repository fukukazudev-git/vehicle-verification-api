package com.example.vehicleverification.infrastructure.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        // Authorizationヘッダがない/Bearerトークンでなければ素通し(EntryPointが最終的に401判定)
        if (header == null || !header.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);

        // Bearer以降を切り出す
        try {

            // パースは1回のみ(署名有効期限検証も兼ねる)。不正なら例外処理
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // 2引数版のコンストラクタは未認証扱いのため、3引数版のコンストラクタを使って認証済み扱いにする
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // IP 等の付帯情報も載せる
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContextHolder → スレッドスコープのホルダー
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // RuntimeExceptionだが、500エラーでなく401エラーとしたいためcatchする
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {

            // トークン不正/該当ユーザー無し → 認証情報を残さず消す(後段はEntryPointが401判定)
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
