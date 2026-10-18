package com.example.vehicleverification.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vehicleverification.application.dto.user.PasswordChangeRequest;
import com.example.vehicleverification.application.dto.user.PasswordResetRequest;
import com.example.vehicleverification.application.dto.user.UserCreateRequest;
import com.example.vehicleverification.application.dto.user.UserCreateResponse;
import com.example.vehicleverification.application.dto.user.UserDto;
import com.example.vehicleverification.application.dto.user.UserRoleResponse;
import com.example.vehicleverification.application.dto.user.UserUpdateRequest;
import com.example.vehicleverification.application.service.UserService;
import com.example.vehicleverification.domain.entity.UserRole;
import com.example.vehicleverification.infrastructure.config.SecurityConfig;
import com.example.vehicleverification.infrastructure.security.CustomUserDetailsService;
import com.example.vehicleverification.infrastructure.security.JwtAccessDeniedHandler;
import com.example.vehicleverification.infrastructure.security.JwtAuthenticationEntryPoint;
import com.example.vehicleverification.infrastructure.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.List;
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

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@WithMockUser(username = "testuser", roles = "ADMIN")
public class UserControllerTest {

    // Boot4 の @WebMvcTest は MockMvc に springSecurity() を自動適用しないため
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
    private UserService userService;

    // JwtAuthenticationFilter の依存関係をモックにすることで、実物のフィルタを通す
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserCreateRequest createRequest(UserRole role) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("createdUser");
        request.setPassword("testPassword");
        request.setDisplayName("testDisplayName");
        request.setRole(role);
        request.setDepartment("開発部");
        return request;
    }

    @Test
    void PUT_resetPassword_正常系_204を返す() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("newPassword");

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).resetPassword(eq(1L), any(PasswordResetRequest.class));
    }

    @Test
    void PUT_resetPassword_8文字未満_400を返す() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("short");

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("newPassword"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("新しいパスワードは8文字以上で入力してください"));

        verify(userService, never()).resetPassword(any(), any());
    }

    @Test
    void PUT_changePassword_currentPasswordが空_400とfieldErrorsを返す() throws Exception {
        // 本人用は /me/password。/{id}/password は管理者用リセットでDTOが別物になる
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword(""); // @NotBlank違反
        request.setNewPassword("newPassword");

        // fieldErrorsの並び順は保証されないため、違反は1項目だけに絞って位置で検証する
        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("currentPassword"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("現在のパスワードは必須です"));

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @WithMockUser(username = "member", roles = "MEMBER")
    void PUT_resetPassword_MEMBERロール_403を返す() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("newPassword");

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).resetPassword(any(), any());
    }

    @Test
    void POST_createUser_role省略_400とroleのfieldErrorsを返す() throws Exception {
        UserCreateRequest request = createRequest(null); // @NotNull違反

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("role"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ロールは必須です"));

        verify(userService, never()).createUser(any());
    }

    @Test
    void POST_createUser_roleに不正値_400と許容値のfieldErrorsを返す() throws Exception {
        // 不正なEnum値はDTOに詰められないため、JSON文字列を直接送る
        String body =
                """
                {
                  "username": "createdUser",
                  "password": "testPassword",
                  "displayName": "testDisplayName",
                  "role": "SUPERUSER",
                  "department": "開発部"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("role"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("許容値: ADMIN, MEMBER"));

        verify(userService, never()).createUser(any());
    }

    @Test
    void POST_createUser_正常系_roleがEnumで渡り201とroleのコード値を返す() throws Exception {
        given(userService.createUser(any()))
                .willReturn(new UserCreateResponse(
                        10L,
                        "createdUser",
                        "testDisplayName",
                        UserRole.MEMBER,
                        LocalDateTime.of(2026, 1, 1, 0, 0),
                        "開発部"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(UserRole.MEMBER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        ArgumentCaptor<UserCreateRequest> captor = ArgumentCaptor.forClass(UserCreateRequest.class);
        verify(userService).createUser(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.MEMBER);
    }

    @Test
    @WithMockUser(username = "member", roles = "MEMBER")
    void POST_createUser_MEMBERロール_403を返す() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(UserRole.MEMBER))))
                .andExpect(status().isForbidden());

        verify(userService, never()).createUser(any());
    }

    @Test
    void POST_createUser_usernameが空_400とfieldErrorsを返す() throws Exception {
        UserCreateRequest request = createRequest(UserRole.MEMBER);
        request.setUsername(""); // @NotBlank違反

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ユーザー名は必須です"));

        verify(userService, never()).createUser(any());
    }

    @Test
    void PUT_updateUser_role省略_400とroleのfieldErrorsを返す() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setDisplayName("更新後表示名");
        request.setRole(null); // @NotNull違反
        request.setDepartment("開発部");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("role"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ロールは必須です"));

        verify(userService, never()).updateUser(eq(1L), any());
    }

    @Test
    void PUT_updateUser_roleに不正値_400と許容値のfieldErrorsを返す() throws Exception {
        String body =
                """
                {
                  "displayName": "更新後表示名",
                  "role": "admin",
                  "department": "開発部"
                }
                """;

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("role"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("許容値: ADMIN, MEMBER"));

        verify(userService, never()).updateUser(eq(1L), any());
    }

    @Test
    void GET_getUserById_IDに数値以外_400を返す() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("リクエストの形式が不正です。"));

        verify(userService, never()).getUserById(any());
    }

    @Test
    void GET_getUserAll_roleのコード値と日本語表示名を返す() throws Exception {
        given(userService.getUserAll())
                .willReturn(List.of(new UserDto(
                        1L, "admin", "管理者ユーザー", UserRole.ADMIN, "管理者", LocalDateTime.of(2026, 1, 1, 0, 0), "開発部")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[0].roleDisplayName").value("管理者"));
    }

    @Test
    @WithMockUser(username = "member", roles = "MEMBER")
    void GET_getUserRoles_全ロールをcodeと日本語表示名で返す() throws Exception {
        // 参照系はADMIN以外のログインユーザーにも公開されることを兼ねて確認する
        given(userService.getUserRoles())
                .willReturn(List.of(new UserRoleResponse("ADMIN", "管理者"), new UserRoleResponse("MEMBER", "メンバー")));

        mockMvc.perform(get("/api/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("ADMIN"))
                .andExpect(jsonPath("$[0].displayName").value("管理者"))
                .andExpect(jsonPath("$[1].code").value("MEMBER"))
                .andExpect(jsonPath("$[1].displayName").value("メンバー"));
    }
}
