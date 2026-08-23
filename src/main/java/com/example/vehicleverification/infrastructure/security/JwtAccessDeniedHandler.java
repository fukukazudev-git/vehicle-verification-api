package com.example.vehicleverification.infrastructure.security;

import com.example.vehicleverification.presentation.dto.error.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

// 認可拒否(権限不足)時に、EntryPoint(401)と対になる形で 403 の JSON を直接返す。
// デフォルトの AccessDeniedHandler は sendError(403) で /error ディスパッチを起こし、
// ステートレス構成では再認証チェックで 401 にすり替わってしまうため、直接書き込む。
// フィルタ層のエラーはセキュリティの部品としてここで処理する
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "権限がありません。",
                request.getRequestURI(),
                null);

        objectMapper.writeValue(response.getWriter(), body);
    }
}
