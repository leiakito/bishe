package com.example.demo.repository;

import com.example.demo.entity.CompetitionCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionCategoryRepository extends JpaRepository<CompetitionCategoryEntity, Long> {
    Optional<CompetitionCategoryEntity> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<CompetitionCategoryEntity> findByStatusOrderByUpdatedAtDesc(CompetitionCategoryEntity.Status status);

    List<CompetitionCategoryEntity> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}
