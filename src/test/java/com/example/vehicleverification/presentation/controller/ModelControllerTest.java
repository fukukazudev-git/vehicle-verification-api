package com.example.vehicleverification.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vehicleverification.application.dto.model.ModelCreateRequest;
import com.example.vehicleverification.application.dto.model.ModelCreateResponse;
import com.example.vehicleverification.application.dto.model.ModelUpdateRequest;
import com.example.vehicleverification.application.service.ModelService;
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

@WebMvcTest(ModelController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@WithMockUser(username = "testuser", roles = "ADMIN")
public class ModelControllerTest {

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
    private ModelService modelService;

    // JwtAuthenticationFilterの依存関係をモックにすることで、実物のフィルタを通す
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // 全項目が妥当なリクエスト。異常系はここから1項目だけ崩して使う
    private ModelCreateRequest createRequest() {
        ModelCreateRequest request = new ModelCreateRequest();
        request.setModelCode("M001");
        request.setModelName("テスト機種");
        request.setModelYear(2026);
        request.setEcuType("ECU-A");
        request.setEngineType("ENG-A");
        request.setDriveType("FF");
        request.setDescription("備考");
        request.setDestination("JP");
        request.setPowertrainType("HEV");
        return request;
    }

    @Test
    void POST_createModel_正常系_201を返す() throws Exception {
        given(modelService.createModel(any()))
                .willReturn(new ModelCreateResponse(
                        1L,
                        "M001",
                        "テスト機種",
                        2026,
                        "ECU-A",
                        "ENG-A",
                        "FF",
                        "備考",
                        LocalDateTime.of(2026, 1, 1, 0, 0),
                        "JP",
                        "HEV"));

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.modelCode").value("M001"));

        ArgumentCaptor<ModelCreateRequest> captor = ArgumentCaptor.forClass(ModelCreateRequest.class);
        verify(modelService).createModel(captor.capture());
        assertThat(captor.getValue().getModelYear()).isEqualTo(2026);
    }

    @Test
    void POST_createModel_modelCodeが空_400とfieldErrorsを返す() throws Exception {
        ModelCreateRequest request = createRequest();
        request.setModelCode(""); // @NotBlank違反

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("入力値が不正です。"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("modelCode"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("機種コードは必須です"));

        verify(modelService, never()).createModel(any());
    }

    @Test
    void POST_createModel_modelCodeが11文字_400とfieldErrorsを返す() throws Exception {
        ModelCreateRequest request = createRequest();
        request.setModelCode("A".repeat(11)); // @Size(max = 10)違反

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("modelCode"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("機種コードは10文字以内で入力してください"));

        verify(modelService, never()).createModel(any());
    }

    @Test
    void POST_createModel_modelYearが下限未満_400とfieldErrorsを返す() throws Exception {
        ModelCreateRequest request = createRequest();
        request.setModelYear(1899); // @Min(1900)違反

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("modelYear"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("モデル年式は1900以上で入力してください"));

        verify(modelService, never()).createModel(any());
    }

    @Test
    void PUT_updateModel_versionなし_400とfieldErrorsを返す() throws Exception {
        ModelUpdateRequest request = new ModelUpdateRequest();
        request.setModelCode("M001");
        request.setModelName("テスト機種");
        request.setModelYear(2026);
        request.setEcuType("ECU-A");
        request.setEngineType("ENG-A");
        request.setDriveType("FF");
        request.setDestination("JP");
        request.setPowertrainType("HEV");
        // versionを設定しない(@NotNull違反)

        mockMvc.perform(put("/api/models/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("version"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("バージョンは必須です"));

        verify(modelService, never()).updateModel(any(), any());
    }
}
