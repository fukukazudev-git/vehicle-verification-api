package com.example.vehicleverification.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vehicleverification.application.dto.attachment.AttachmentDto;
import com.example.vehicleverification.application.dto.attachment.AttachmentUploadRequest;
import com.example.vehicleverification.application.service.AttachmentService;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.entity.UserRole;
import com.example.vehicleverification.infrastructure.config.SecurityConfig;
import com.example.vehicleverification.infrastructure.security.CustomUserDetails;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@WebMvcTest(AttachmentController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
public class AttachmentControllerTest {

    // Boot 4 の @WebMvcTest は MockMvc に springSecurity() を自動適用しないため
    // WebApplicationContext から明示的に組み立てる。
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @MockitoBean
    private AttachmentService attachmentService;

    // JwtAuthenticationFilterの依存関係をモックにすることで、実物のフィルタを通す
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private static final Long UPLOADER_ID = 10L;

    // Controllerは@AuthenticationPrincipal CustomUserDetailsからアップロード者IDを取るため、
    // @WithMockUserではなくCustomUserDetailsを認証情報として渡す
    private CustomUserDetails uploader() {
        User user = new User("uploader", "pass", "アップロード者", UserRole.MEMBER, "開発部");
        user.setId(UPLOADER_ID);
        return new CustomUserDetails(user);
    }

    private MockMultipartFile txt() {
        return new MockMultipartFile("file", "sample.txt", "text/plain", "content".getBytes());
    }

    private AttachmentDto dummyDto() {
        return new AttachmentDto(
                1L,
                "sample.txt",
                "txt",
                "attachments/uuid_sample.txt",
                5L,
                null,
                null,
                UPLOADER_ID,
                "アップロード者",
                LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void POST_uploadAttachment_フォーム項目とファイルで送信_201を返す() throws Exception {
        // Arrange
        given(attachmentService.upload(any(), any(), any())).willReturn(dummyDto());

        // Act & Assert
        mockMvc.perform(multipart("/api/attachments")
                        .file(txt())
                        .param("reviewMeetingId", "5")
                        .with(user(uploader())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileType").value("txt"));

        // フォーム項目がリクエストDTOにバインドされ、ログインユーザーのIDがアップロード者として渡ること
        ArgumentCaptor<AttachmentUploadRequest> captor = ArgumentCaptor.forClass(AttachmentUploadRequest.class);
        verify(attachmentService).upload(captor.capture(), any(), eq(UPLOADER_ID));
        assertThat(captor.getValue().getReviewMeetingId()).isEqualTo(5L);
        assertThat(captor.getValue().getTestRecordId()).isNull();
        assertThat(captor.getValue().getUserId()).isNull();
    }

    @Test
    void POST_uploadAttachment_ID欄に数値以外_400と固定文言のfieldErrorsを返す() throws Exception {
        mockMvc.perform(multipart("/api/attachments")
                        .file(txt())
                        .param("reviewMeetingId", "abc")
                        .with(user(uploader())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("入力値が不正です。"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("reviewMeetingId"))
                // 内部の型名(java.lang.Long等)を含む既定メッセージを返さないこと
                .andExpect(jsonPath("$.fieldErrors[0].message").value("値の形式が不正です"));

        verify(attachmentService, never()).upload(any(), any(), any());
    }

    @Test
    void POST_uploadAttachment_JSONで送信_415を返す() throws Exception {
        mockMvc.perform(post("/api/attachments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewMeetingId\": 5, \"file\": \"C:\\\\sample.txt\"}")
                        .with(user(uploader())))
                .andExpect(status().isUnsupportedMediaType());

        verify(attachmentService, never()).upload(any(), any(), any());
    }

    @Test
    void POST_uploadAttachment_MultipartException発生_400を返す() throws Exception {
        // 本文がmultipartとして解析できない場合の例外がControllerAdviceで400になること
        given(attachmentService.upload(any(), any(), any()))
                .willThrow(new MultipartException("Failed to parse multipart servlet request"));

        mockMvc.perform(multipart("/api/attachments")
                        .file(txt())
                        .param("reviewMeetingId", "5")
                        .with(user(uploader())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ファイルのアップロード形式が不正です。multipart/form-dataで送信してください。"));
    }

    @Test
    void POST_uploadAttachment_サイズ超過_MultipartExceptionより優先して413を返す() throws Exception {
        given(attachmentService.upload(any(), any(), any())).willThrow(new MaxUploadSizeExceededException(1L));

        mockMvc.perform(multipart("/api/attachments")
                        .file(txt())
                        .param("reviewMeetingId", "5")
                        .with(user(uploader())))
                .andExpect(status().isContentTooLarge());
    }

    @Test
    void POST_uploadAttachment_ファイルなし_400とfieldErrorsを返す() throws Exception {
        mockMvc.perform(multipart("/api/attachments")
                        .param("reviewMeetingId", "5")
                        .with(user(uploader())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("file"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ファイルは必須です"));

        verify(attachmentService, never()).upload(any(), any(), any());
    }

    @Test
    void POST_uploadAttachment_未認証_401を返す() throws Exception {
        mockMvc.perform(multipart("/api/attachments").file(txt()).param("reviewMeetingId", "5"))
                .andExpect(status().isUnauthorized());

        verify(attachmentService, never()).upload(any(), any(), any());
    }
}
