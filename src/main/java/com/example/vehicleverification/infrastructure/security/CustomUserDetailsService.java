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

        // domain(entity)のUserを、idを保持する独自UserDetails(CustomUserDetails)に変換して返す。
        // ROLE_の前置はCustomUserDetails#getAuthorities内で行う。
        return new CustomUserDetails(user);
    }
}
