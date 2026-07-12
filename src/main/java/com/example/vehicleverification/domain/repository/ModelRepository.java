package com.example.vehicleverification.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vehicleverification.domain.entity.Model;

public interface ModelRepository extends JpaRepository<Model, Long> {

}
