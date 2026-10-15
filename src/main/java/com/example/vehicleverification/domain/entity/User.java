package com.example.vehicleverification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 20)
    private String username;

    @NotNull
    @Size(max = 255)
    private String password;

    @NotNull
    @Size(max = 50)
    private String displayName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @NotNull
    @Size(max = 50)
    private String department;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    protected User() {}

    public User(String username, String password, String displayName, UserRole role, String department) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.role = role;
        this.department = department;
    }
}
