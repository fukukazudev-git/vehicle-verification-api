package com.example.vehicleverification.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateRequest;
import com.example.vehicleverification.application.dto.testrecord.TestRecordCreateResponse;
import com.example.vehicleverification.application.dto.testrecord.TestRecordUpdateRequest;
import com.example.vehicleverification.application.service.TestRecordService;
import com.example.vehicleverification.domain.entity.TestResult;
import com.example.vehicleverification.infrastructure.config.SecurityConfig;
import com.example.vehicleverification.infrastructure.security.CustomUserDetailsService;
import com.example.vehicleverification.infrastructure.security.JwtAccessDeniedHandler;
import com.example.vehicleverification.infrastructure.security.JwtAuthenticationEntryPoint;
import com.example.vehicleverification.infrastructure.security.JwtTokenProvider;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TestRecordController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@WithMockUser(username = "testuser", roles = "ADMIN")
public class TestRecordControllerTest {

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
    private TestRecordService testRecordService;

    // JwtAuthenticationFilterの依存関係をモックにすることで、実物のフィルタを通す
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // 全項目が妥当なリクエスト。異常系はここから1項目だけ崩して使う
    private TestRecordCreateRequest createRequest() {
        TestRecordCreateRequest request = new TestRecordCreateRequest();
        request.setReviewMeetingId(1L);
        request.setTestName("ブレーキ試験");
        request.setResult(TestResult.OK);
        request.setNotes("備考");
        request.setRecordedById(1L);
        return request;
    }

    @Test
    void POST_createTestRecord_正常系_201とresultのコード値を返す() throws Exception {
        given(testRecordService.createTestRecord(any()))
                .willReturn(new TestRecordCreateResponse(
                        1L,
                        "ブレーキ試験",
                        TestResult.OK,
                        "備考",
                        1L,
                        "定例レビュー",
                        1L,
                        "記録者名",
                        LocalDateTime.of(2026, 1, 1, 0, 0)));

        mockMvc.perform(post("/api/test-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.result").value("OK"));

        ArgumentCaptor<TestRecordCreateRequest> captor = ArgumentCaptor.forClass(TestRecordCreateRequest.class);
        verify(testRecordService).createTestRecord(captor.capture());
        assertThat(captor.getValue().getResult()).isEqualTo(TestResult.OK);
    }

    @Test
    void POST_createTestRecord_testNameが空_400とfieldErrorsを返す() throws Exception {
        TestRecordCreateRequest request = createRequest();
        request.setTestName(""); // @NotBlank違反

        mockMvc.perform(post("/api/test-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("testName"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("テスト名は必須です"));

        verify(testRecordService, never()).createTestRecord(any());
    }

    @Test
    void POST_createTestRecord_result省略_400とfieldErrorsを返す() throws Exception {
        TestRecordCreateRequest request = createRequest();
        request.setResult(null); // @NotNull違反

        mockMvc.perform(post("/api/test-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("result"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("結果は必須です"));

        verify(testRecordService, never()).createTestRecord(any());
    }

    @Test
    void POST_createTestRecord_resultに不正値_400と許容値のfieldErrorsを返す() throws Exception {
        // 不正なEnum値はDTOに詰められないため、JSON文字列を直接送る
        String body =
                """
                {
                  "reviewMeetingId": 1,
                  "testName": "ブレーキ試験",
                  "result": "FAIL",
                  "recordedById": 1
                }
                """;

        mockMvc.perform(post("/api/test-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("result"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("許容値: OK, NG, PENDING"));

        verify(testRecordService, never()).createTestRecord(any());
    }

    @Test
    void PUT_updateTestRecord_versionなし_400とfieldErrorsを返す() throws Exception {
        TestRecordUpdateRequest request = new TestRecordUpdateRequest();
        request.setTestName("ブレーキ試験");
        request.setResult(TestResult.NG);
        // versionを設定しない(@NotNull違反)

        mockMvc.perform(put("/api/test-records/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("version"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("バージョンは必須です"));

        verify(testRecordService, never()).updateTestRecord(any(), any());
    }

    @Test
    void GET_getTestRecordsByReviewMeeting_resultクエリに不正値_400を返す() throws Exception {
        mockMvc.perform(get("/api/test-records").param("result", "FAIL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("リクエストの形式が不正です。"));

        verify(testRecordService, never()).getTestRecordsByReviewMeeting(any(), any());
    }
}
