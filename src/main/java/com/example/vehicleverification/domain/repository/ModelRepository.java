package com.example.vehicleverification.domain.repository;

import com.example.vehicleverification.domain.entity.Model;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {

    // 機種コード・機種名のどちらかに部分一致（大文字小文字区別なし）。
    // Containingは%や_をエスケープするため、入力値はそのまま渡してよい
    List<Model> findByModelCodeContainingIgnoreCaseOrModelNameContainingIgnoreCase(String modelCode, String modelName);
}
