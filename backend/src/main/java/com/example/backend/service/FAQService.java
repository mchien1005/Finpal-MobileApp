package com.example.backend.service;

import com.example.backend.dto.FAQDTO;
import com.example.backend.model.FAQ;
import com.example.backend.model.FAQ.FAQCategory;
import com.example.backend.model.FAQ.FAQStatus;
import com.example.backend.repository.FAQRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service quản lý FAQ (Frequently Asked Questions)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FAQService {

    private final FAQRepository faqRepository;

    /**
     * Lấy tất cả FAQs (Admin)
     */
    @Transactional(readOnly = true)
    public List<FAQDTO> getAllFAQs() {
        return faqRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy FAQs đang active (User)
     */
    @Transactional(readOnly = true)
    public List<FAQDTO> getActiveFAQs() {
        return faqRepository.findByStatusOrderByDisplayOrderAsc(FAQStatus.ACTIVE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy FAQs theo category (User)
     */
    @Transactional(readOnly = true)
    public List<FAQDTO> getFAQsByCategory(FAQCategory category) {
        return faqRepository.findByStatusAndCategoryOrderByDisplayOrderAsc(FAQStatus.ACTIVE, category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy FAQ theo ID
     */
    @Transactional(readOnly = true)
    public FAQDTO getFAQById(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        return convertToDTO(faq);
    }

    /**
     * Tạo FAQ mới (Admin)
     */
    @Transactional
    public FAQDTO createFAQ(FAQDTO.CreateRequest request, Long adminId) {
        // Check duplicate code
        if (faqRepository.existsByFaqCode(request.getFaqCode())) {
            throw new RuntimeException("FAQ code already exists: " + request.getFaqCode());
        }

        FAQ faq = new FAQ();
        faq.setFaqCode(request.getFaqCode());
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setCategory(request.getCategory() != null ? request.getCategory() : FAQCategory.GENERAL);
        faq.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        faq.setStatus(request.getStatus() != null ? request.getStatus() : FAQStatus.DRAFT);
        faq.setViewCount(0);
        faq.setHelpfulCount(0);
        faq.setNotHelpfulCount(0);
        faq.setCreatedBy(adminId);

        faq = faqRepository.save(faq);
        log.info("Created FAQ: {} by admin {}", faq.getFaqCode(), adminId);

        return convertToDTO(faq);
    }

    /**
     * Cập nhật FAQ (Admin)
     */
    @Transactional
    public FAQDTO updateFAQ(Long id, FAQDTO.UpdateRequest request) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));

        if (request.getQuestion() != null) {
            faq.setQuestion(request.getQuestion());
        }
        if (request.getAnswer() != null) {
            faq.setAnswer(request.getAnswer());
        }
        if (request.getCategory() != null) {
            faq.setCategory(request.getCategory());
        }
        if (request.getDisplayOrder() != null) {
            faq.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getStatus() != null) {
            faq.setStatus(request.getStatus());
        }

        faq = faqRepository.save(faq);
        log.info("Updated FAQ: {}", faq.getFaqCode());

        return convertToDTO(faq);
    }

    /**
     * Xóa FAQ (Admin)
     */
    @Transactional
    public void deleteFAQ(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));

        faqRepository.delete(faq);
        log.info("Deleted FAQ: {}", faq.getFaqCode());
    }

    /**
     * Tăng view count (User xem FAQ)
     */
    @Transactional
    public void incrementViewCount(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));

        faq.setViewCount(faq.getViewCount() + 1);
        faqRepository.save(faq);
    }

    /**
     * Submit feedback (User đánh giá FAQ)
     */
    @Transactional
    public void submitFeedback(Long id, boolean helpful) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));

        if (helpful) {
            faq.setHelpfulCount(faq.getHelpfulCount() + 1);
        } else {
            faq.setNotHelpfulCount(faq.getNotHelpfulCount() + 1);
        }
        faqRepository.save(faq);
    }

    /**
     * Convert entity to DTO
     */
    private FAQDTO convertToDTO(FAQ faq) {
        return FAQDTO.builder()
                .id(faq.getId())
                .faqCode(faq.getFaqCode())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .viewCount(faq.getViewCount())
                .helpfulCount(faq.getHelpfulCount())
                .notHelpfulCount(faq.getNotHelpfulCount())
                .displayOrder(faq.getDisplayOrder())
                .status(faq.getStatus())
                .createdBy(faq.getCreatedBy())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    /**
     * Get statistics (Admin)
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatistics() {
        return Map.of(
                "total", faqRepository.count(),
                "active", faqRepository.countByStatus(FAQStatus.ACTIVE),
                "inactive", faqRepository.countByStatus(FAQStatus.INACTIVE),
                "draft", faqRepository.countByStatus(FAQStatus.DRAFT));
    }
}
