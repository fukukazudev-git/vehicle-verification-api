package com.example.vehicleverification.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vehicleverification.application.dto.issue.IssueUpdateRequest;
import com.example.vehicleverification.application.dto.issue.IssueUpdateResponse;
import com.example.vehicleverification.application.service.IssueService;
import com.example.vehicleverification.infrastructure.config.SecurityConfig;
import com.example.vehicleverification.infrastructure.security.CustomUserDetailsService;
import com.example.vehicleverification.infrastructure.security.JwtAccessDeniedHandler;
import com.example.vehicleverification.infrastructure.security.JwtAuthenticationEntryPoint;
import com.example.vehicleverification.infrastructure.security.JwtTokenProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(IssueController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@WithMockUser(username = "testuser", roles = "ADMIN")
public class IssueControllerTest {

    // Boot 4 の @WebMvcTest は MockMvc に springSecurity() を自動適用しないため
    // WebApplicationContext から明示的に組み立てる。
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @MockitoBean
    private IssueService issueService;

    // JwtAuthenticationFilterの依存関係をモックにすることで、実物のフィルタを通す
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void PATCH_updateIssue_正常系_200を返す() throws Exception {
        // Arrange
        Long id = 1L;
        LocalDate resolvedAt = LocalDate.of(2026, 1, 1);
        IssueUpdateRequest request = new IssueUpdateRequest();
        request.setAnswer("更新された回答");
        request.setAnswererId(1L);
        request.setResolvedAt(resolvedAt);
        request.setStatus("完了");
        request.setVersion(0L);

        IssueUpdateResponse response = new IssueUpdateResponse(
                1L,
                "完了",
                "更新された内容",
                "更新された回答",
                resolvedAt,
                1L,
                "レビュー会議タイトル",
                1L,
                "報告者名",
                1L,
                "回答者名",
                LocalDateTime.now(),
                0L);

        given(issueService.updateIssue(any(Long.class), any(IssueUpdateRequest.class)))
                .willReturn(response);

        // Act
        mockMvc.perform(patch("/api/issues/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.status").value(response.getStatus()));

        // Assert
        ArgumentCaptor<IssueUpdateRequest> captor = ArgumentCaptor.forClass(IssueUpdateRequest.class);
        verify(issueService).updateIssue(eq(id), captor.capture());
        IssueUpdateRequest captured = captor.getValue();
        assertThat(captured.getAnswer()).isEqualTo(request.getAnswer());
        assertThat(captured.getAnswererId()).isEqualTo(request.getAnswererId());
        assertThat(captured.getResolvedAt()).isEqualTo(request.getResolvedAt());
        assertThat(captured.getStatus()).isEqualTo(request.getStatus());
        assertThat(captured.getVersion()).isEqualTo(request.getVersion());
    }

    @Test
    void PATCH_updateIssue_versionなし_400を返す() throws Exception {
        IssueUpdateRequest request = new IssueUpdateRequest();
        request.setAnswer("更新された回答");
        request.setAnswererId(1L);
        request.setResolvedAt(LocalDate.of(2026, 1, 1));
        request.setStatus("完了");
        // versionを設定しない

        mockMvc.perform(patch("/api/issues/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'version')]").exists());
    }

    @Test
    @WithAnonymousUser
    void GET_getIssueById_未認証_401を返す() throws Exception {
        mockMvc.perform(get("/api/issues/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("認証が必要です。"))
                .andExpect(jsonPath("$.path").value("/api/issues/1"));

        // Controller/Serviceには到達しないことの確認
        verify(issueService, never()).getIssueById(any(Long.class));
    }
}
