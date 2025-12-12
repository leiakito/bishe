package com.example.demo.service;

import com.example.demo.entity.CompetitionCategoryEntity;
import com.example.demo.repository.CompetitionCategoryRepository;
import com.example.demo.repository.CompetitionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompetitionCategoryService {

    @Autowired
    private CompetitionCategoryRepository categoryRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    private static final Map<String, String> DEFAULT_NAMES = Map.of(
            "PROGRAMMING", "程序设计",
            "MATHEMATICS", "数学竞赛",
            "PHYSICS", "物理竞赛",
            "CHEMISTRY", "化学竞赛",
            "BIOLOGY", "生物竞赛",
            "ENGLISH", "英语竞赛",
            "DESIGN", "设计竞赛",
            "INNOVATION", "创新创业",
            "OTHER", "其他"
    );

    @PostConstruct
    public void initDefaults() {
        DEFAULT_NAMES.forEach((code, name) -> {
            categoryRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
                CompetitionCategoryEntity category = new CompetitionCategoryEntity();
                category.setCode(code);
                category.setName(name);
                category.setDescription("内置分类");
                category.setStatus(CompetitionCategoryEntity.Status.ACTIVE);
                return categoryRepository.save(category);
            });
        });
    }

    public List<CompetitionCategoryEntity> listAll(String status, String keyword) {
        if (status != null && !status.isBlank()) {
            CompetitionCategoryEntity.Status parsed = CompetitionCategoryEntity.Status.valueOf(status.toUpperCase());
            return filterByKeyword(categoryRepository.findByStatusOrderByUpdatedAtDesc(parsed), keyword);
        }
        return filterByKeyword(categoryRepository.findAll(), keyword);
    }

    private List<CompetitionCategoryEntity> filterByKeyword(List<CompetitionCategoryEntity> categories, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return categories.stream()
                    .sorted(Comparator.comparing(CompetitionCategoryEntity::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        String lower = keyword.toLowerCase();
        return categories.stream()
                .filter(c ->
                        (c.getName() != null && c.getName().toLowerCase().contains(lower)) ||
                        (c.getCode() != null && c.getCode().toLowerCase().contains(lower)) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(lower))
                )
                .sorted(Comparator.comparing(CompetitionCategoryEntity::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Transactional
    public CompetitionCategoryEntity create(CompetitionCategoryEntity payload) {
        String code = normalizeCode(payload.getCode());
        ensureCodeUnique(code, null);
        payload.setCode(code);
        payload.setName(normalizeName(payload.getName()));
        if (payload.getStatus() == null) {
            payload.setStatus(CompetitionCategoryEntity.Status.ACTIVE);
        }
        return categoryRepository.save(payload);
    }

    @Transactional
    public CompetitionCategoryEntity update(Long id, CompetitionCategoryEntity payload) {
        CompetitionCategoryEntity existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        if (payload.getCode() != null) {
            String normalizedCode = normalizeCode(payload.getCode());
            ensureCodeUnique(normalizedCode, id);
            existing.setCode(normalizedCode);
        }

        if (payload.getName() != null) {
            existing.setName(normalizeName(payload.getName()));
        }

        if (payload.getDescription() != null) {
            existing.setDescription(payload.getDescription());
        }

        if (payload.getStatus() != null) {
            existing.setStatus(payload.getStatus());
        }

        return categoryRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    @Transactional
    public CompetitionCategoryEntity updateStatus(Long id, CompetitionCategoryEntity.Status status) {
        CompetitionCategoryEntity existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        existing.setStatus(status);
        return categoryRepository.save(existing);
    }

    public List<Map<String, Object>> listWithUsage(String status, String keyword) {
        List<CompetitionCategoryEntity> categories = listAll(status, keyword);
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listActiveOptions() {
        return categoryRepository.findByStatusOrderByUpdatedAtDesc(CompetitionCategoryEntity.Status.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toResponse(CompetitionCategoryEntity category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("code", category.getCode());
        map.put("description", category.getDescription());
        map.put("status", category.getStatus());
        map.put("usage", countUsage(category.getCode()));
        map.put("updatedAt", category.getUpdatedAt());
        map.put("createdAt", category.getCreatedAt());
        return map;
    }

    private long countUsage(String code) {
        try {
            return competitionRepository.countByCategory(code);
        } catch (Exception e) {
            return 0L;
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("分类编码不能为空");
        }
        return code.trim().toUpperCase();
    }

    private void ensureCodeUnique(String code, Long excludeId) {
        Optional<CompetitionCategoryEntity> existing = categoryRepository.findByCodeIgnoreCase(code);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new RuntimeException("分类编码已存在: " + code);
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
        }
        return name.trim();
    }
}
