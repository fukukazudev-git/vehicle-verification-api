package com.example.vehicleverification.infrastructure.security;

import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// DBからユーザ情報を取得して、Spring SecurityのUserDetailsに変換する
// パスワード照合そのものはSpring Securityが行うので、ここではハッシュ済みパスワードを返すのみ
@Service
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        // domain(entity)のUserとSpring SecurityのUserとで名前衝突が起きるので、
        // ここではフルパスでSpring SecurityのUserを指定する。
        // Spring SecurityのUserDetailsに変換して返す
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // DBのハッシュ済みパスワード
                .roles(user.getRole()) // ここでROLE_を前置してGrantedAuthority("ROLE_XXX")を生成
                .build();
    }
}
