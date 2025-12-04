package com.example.backend.service;

import com.example.backend.dto.TipDTO;
import com.example.backend.model.Tip;
import com.example.backend.model.Tip.TipCategory;
import com.example.backend.model.Tip.TipStatus;
import com.example.backend.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service quản lý Tips & Suggestions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TipService {

    private final TipRepository tipRepository;

    /**
     * Lấy tất cả tips (Admin)
     */
    @Transactional(readOnly = true)
    public List<TipDTO> getAllTips() {
        return tipRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tips đang active (User)
     */
    @Transactional(readOnly = true)
    public List<TipDTO> getActiveTips() {
        return tipRepository.findByStatusOrderByDisplayOrderAsc(TipStatus.ACTIVE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tips theo category (User)
     */
    @Transactional(readOnly = true)
    public List<TipDTO> getTipsByCategory(TipCategory category) {
        return tipRepository.findByStatusAndCategoryOrderByDisplayOrderAsc(TipStatus.ACTIVE, category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tip theo ID
     */
    @Transactional(readOnly = true)
    public TipDTO getTipById(Long id) {
        Tip tip = tipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tip not found with id: " + id));
        return convertToDTO(tip);
    }

    /**
     * Tạo tip mới (Admin)
     */
    @Transactional
    public TipDTO createTip(TipDTO.CreateRequest request, Long adminId) {
        // Check duplicate code
        if (tipRepository.existsByTipCode(request.getTipCode())) {
            throw new RuntimeException("Tip code already exists: " + request.getTipCode());
        }

        Tip tip = new Tip();
        tip.setTipCode(request.getTipCode());
        tip.setTitle(request.getTitle());
        tip.setContent(request.getContent());
        tip.setCategory(request.getCategory() != null ? request.getCategory() : TipCategory.GENERAL);
        tip.setIcon(request.getIcon());
        tip.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        tip.setStatus(request.getStatus() != null ? request.getStatus() : TipStatus.DRAFT);
        tip.setViewCount(0);
        tip.setLikeCount(0);
        tip.setCreatedBy(adminId);

        tip = tipRepository.save(tip);
        log.info("Created tip: {} by admin {}", tip.getTipCode(), adminId);

        return convertToDTO(tip);
    }

    /**
     * Cập nhật tip (Admin)
     */
    @Transactional
    public TipDTO updateTip(Long id, TipDTO.UpdateRequest request) {
        Tip tip = tipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tip not found with id: " + id));

        if (request.getTitle() != null) {
            tip.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            tip.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            tip.setCategory(request.getCategory());
        }
        if (request.getIcon() != null) {
            tip.setIcon(request.getIcon());
        }
        if (request.getDisplayOrder() != null) {
            tip.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getStatus() != null) {
            tip.setStatus(request.getStatus());
        }

        tip = tipRepository.save(tip);
        log.info("Updated tip: {}", tip.getTipCode());

        return convertToDTO(tip);
    }

    /**
     * Xóa tip (Admin)
     */
    @Transactional
    public void deleteTip(Long id) {
        Tip tip = tipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tip not found with id: " + id));

        tipRepository.delete(tip);
        log.info("Deleted tip: {}", tip.getTipCode());
    }

    /**
     * Tăng view count (User xem tip)
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Tip tip = tipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tip not found with id: " + id));

        tip.setViewCount(tip.getViewCount() + 1);
        tipRepository.save(tip);
    }

    /**
     * Like tip (User)
     */
    @Transactional
    public void likeTip(Long id) {
        Tip tip = tipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tip not found with id: " + id));

        tip.setLikeCount(tip.getLikeCount() + 1);
        tipRepository.save(tip);
    }

    /**
     * Convert entity to DTO
     */
    private TipDTO convertToDTO(Tip tip) {
        return TipDTO.builder()
                .id(tip.getId())
                .tipCode(tip.getTipCode())
                .title(tip.getTitle())
                .content(tip.getContent())
                .category(tip.getCategory())
                .icon(tip.getIcon())
                .viewCount(tip.getViewCount())
                .likeCount(tip.getLikeCount())
                .displayOrder(tip.getDisplayOrder())
                .status(tip.getStatus())
                .createdBy(tip.getCreatedBy())
                .createdAt(tip.getCreatedAt())
                .updatedAt(tip.getUpdatedAt())
                .build();
    }

    /**
     * Get statistics (Admin)
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatistics() {
        return Map.of(
                "total", tipRepository.count(),
                "active", tipRepository.countByStatus(TipStatus.ACTIVE),
                "inactive", tipRepository.countByStatus(TipStatus.INACTIVE),
                "draft", tipRepository.countByStatus(TipStatus.DRAFT));
    }
}
