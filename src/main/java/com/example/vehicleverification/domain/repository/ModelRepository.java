package com.example.vehicleverification.domain.repository;

import com.example.vehicleverification.domain.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {

    boolean existsByModelCode(String modelCode);

    // 更新時は自分自身を除外して重複を判定する
    boolean existsByModelCodeAndIdNot(String modelCode, Long id);
}
