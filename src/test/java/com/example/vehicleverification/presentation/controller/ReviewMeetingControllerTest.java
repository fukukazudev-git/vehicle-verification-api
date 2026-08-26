package com.example.vehicleverification.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateRequest;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingCreateResponse;
import com.example.vehicleverification.application.dto.reviewmeeting.ReviewMeetingDetailResponse;
import com.example.vehicleverification.application.service.ReviewMeetingService;
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

@WebMvcTest(ReviewMeetingController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@WithMockUser(username = "testuser", roles = "ADMIN")
public class ReviewMeetingControllerTest {

    // Boot 4 の @WebMvcTest は MockMvc に springSecurity() を自動適用しないため
    // WebApplicationContext から明示的に組み立てる。これで @WithMockUser が効くようになる
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                // springSecurityFilterChain(FilterChainProxy)をMockMvcの処理経路に接続し、
                // TestSecurityContextHolderPostProcessorにより@WithMockUserの認証情報を反映させる
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @MockitoBean
    private ReviewMeetingService reviewMeetingService;

    // JwtAuthenticationFilter自体はSecurityConfigで実物として組み立てられる。
    // モックにするとdoFilterが空になり後段に進めなくなるため、
    // 依存先のJwtTokenProvider/CustomUserDetailsServiceだけをモックにして実物フィルタを通す
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void POST_createReviewMeeting_正常系_200を返す() throws Exception {
        // Arrange
        ReviewMeetingCreateRequest request = new ReviewMeetingCreateRequest();
        LocalDate scheduledDate = LocalDate.of(2026, 1, 1);
        request.setModelId(1L);
        request.setTitle("定例レビュー");
        request.setScheduledDate(scheduledDate);
        request.setStatus("予定");
        request.setOrganizerId(1L);
        request.setEventCode("EVT001");

        ReviewMeetingCreateResponse response = new ReviewMeetingCreateResponse(
                1L, "定例レビュー", scheduledDate, "予定", 1L, "モデル名", 1L, "主催者名", LocalDateTime.now(), "EVT001");

        given(reviewMeetingService.createReviewMeeting(any(ReviewMeetingCreateRequest.class)))
                .willReturn(response);

        // Act
        mockMvc.perform(post("/api/review-meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.title").value(response.getTitle()));

        // Assert
        ArgumentCaptor<ReviewMeetingCreateRequest> captor = ArgumentCaptor.forClass(ReviewMeetingCreateRequest.class);
        verify(reviewMeetingService).createReviewMeeting(captor.capture());

        ReviewMeetingCreateRequest captured = captor.getValue();
        assertThat(captured.getTitle()).isEqualTo("定例レビュー");
        assertThat(captured.getModelId()).isEqualTo(1L);
        assertThat(captured.getScheduledDate()).isEqualTo(scheduledDate);
    }

    @Test
    void POST_createReviewMeeting_titleが空_400を返す() throws Exception {
        ReviewMeetingCreateRequest request = new ReviewMeetingCreateRequest();
        request.setModelId(1L);
        request.setTitle(""); // @NotBlank違反
        request.setScheduledDate(LocalDate.of(2026, 1, 1));
        request.setStatus("予定");
        request.setOrganizerId(1L);
        request.setEventCode("EVT001");

        mockMvc.perform(post("/api/review-meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'title')]").exists());
    }

    @Test
    void GET_getReviewMeetingById_存在するID_200を返す() throws Exception {
        ReviewMeetingDetailResponse response = new ReviewMeetingDetailResponse(
                1L,
                "定例レビュー",
                LocalDate.of(2026, 1, 1),
                "予定",
                "備考",
                1L,
                "モデル名",
                1L,
                "主催者名",
                LocalDateTime.now(),
                0L,
                "EVT001");

        given(reviewMeetingService.getReviewMeetingById(eq(1L))).willReturn(response);

        mockMvc.perform(get("/api/review-meetings/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.title").value(response.getTitle()));
    }

    @Test
    @WithAnonymousUser
    void GET_getReviewMeetingById_未認証_401を返す() throws Exception {
        mockMvc.perform(get("/api/review-meetings/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("認証が必要です。"))
                .andExpect(jsonPath("$.path").value("/api/review-meetings/1"));

        // 未認証で弾かれるべきであるため、Controller/Serviceには到達していないことも確認する
        verify(reviewMeetingService, never()).getReviewMeetingById(any());
    }
}
