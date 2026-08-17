package com.example.vehicleverification.domain.repository;

import com.example.vehicleverification.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    // Spring Security の UserDetailsService 用、usernameで検索するメソッドを定義
    Optional<User> findByUsername(String username);
}
