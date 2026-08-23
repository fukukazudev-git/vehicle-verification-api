package com.example.vehicleverification.infrastructure.security;

import com.example.vehicleverification.presentation.dto.error.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

// フィルタ層ではControllerAdviceが効かないためレスポンスに直接JSONに書き込む
// 未認証時に401を返すためのEntryPoint。AccessDeniedHandlerと対になる。
// フィルタ層のエラーはセキュリティの部品としてここで処理する
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "認証が必要です。",
                request.getRequestURI(),
                null);

        // オブジェクトをJSONに直列化して出力先に書き出す
        // response.getWriter() → レスポンス本文につながったWriterを取得
        objectMapper.writeValue(response.getWriter(), body); // writeValue(出力先, オブジェクト)
    }
}
