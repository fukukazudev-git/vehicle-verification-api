package com.example.vehicleverification.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class CustomUserDetailsTest {

    // SecurityConfigのhasRole("ADMIN")はROLE_ADMINと照合されるため、Enumのコード値で権限が組み立つことを確認する
    @Test
    void getAuthorities_ADMINの場合_ROLE_ADMINを返す() {
        User user = new User("admin", "pass", "管理者ユーザー", UserRole.ADMIN, "開発部");

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void getAuthorities_MEMBERの場合_ROLE_MEMBERを返す() {
        User user = new User("member", "pass", "メンバーユーザー", UserRole.MEMBER, "開発部");

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MEMBER");
    }
}
