package com.example.vehicleverification.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vehicleverification.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
