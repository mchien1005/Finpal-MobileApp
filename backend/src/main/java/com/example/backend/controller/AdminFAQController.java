package com.example.backend.controller;

import com.example.backend.dto.FAQDTO;
import com.example.backend.model.FAQ.FAQCategory;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.FAQService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Controller - Quản lý FAQs
 */
@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
@Tag(name = "Admin - FAQs", description = "Quản lý câu hỏi thường gặp (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFAQController {

    private final FAQService faqService;
    private final UserRepository userRepository;

    /**
     * GET /api/admin/faqs - Lấy tất cả FAQs
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả FAQs")
    public ResponseEntity<List<FAQDTO>> getAllFAQs() {
        return ResponseEntity.ok(faqService.getAllFAQs());
    }

    /**
     * GET /api/admin/faqs/{id} - Lấy FAQ theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy FAQ theo ID")
    public ResponseEntity<FAQDTO> getFAQById(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.getFAQById(id));
    }

    /**
     * POST /api/admin/faqs - Tạo FAQ mới
     */
    @PostMapping
    @Operation(summary = "Tạo FAQ mới")
    public ResponseEntity<FAQDTO> createFAQ(
            @RequestBody FAQDTO.CreateRequest request,
            Authentication authentication) {
        Long adminId = getAdminId(authentication);
        return ResponseEntity.ok(faqService.createFAQ(request, adminId));
    }

    /**
     * PUT /api/admin/faqs/{id} - Cập nhật FAQ
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật FAQ")
    public ResponseEntity<FAQDTO> updateFAQ(
            @PathVariable Long id,
            @RequestBody FAQDTO.UpdateRequest request) {
        return ResponseEntity.ok(faqService.updateFAQ(id, request));
    }

    /**
     * DELETE /api/admin/faqs/{id} - Xóa FAQ
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa FAQ")
    public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
        faqService.deleteFAQ(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/faqs/statistics - Lấy thống kê
     */
    @GetMapping("/statistics")
    @Operation(summary = "Lấy thống kê FAQs")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(faqService.getStatistics());
    }

    private Long getAdminId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}

/**
 * Public Controller - FAQs cho Users
 */
@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
@Tag(name = "FAQs", description = "Câu hỏi thường gặp cho users")
class FAQController {

    private final FAQService faqService;

    /**
     * GET /api/faqs - Lấy tất cả FAQs đang active
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả FAQs đang active")
    public ResponseEntity<List<FAQDTO>> getActiveFAQs(
            @RequestParam(required = false) FAQCategory category) {
        List<FAQDTO> faqs;
        if (category != null) {
            faqs = faqService.getFAQsByCategory(category);
        } else {
            faqs = faqService.getActiveFAQs();
        }
        return ResponseEntity.ok(faqs);
    }

    /**
     * GET /api/faqs/{id} - Lấy FAQ và tăng view count
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy FAQ theo ID")
    public ResponseEntity<FAQDTO> getFAQById(@PathVariable Long id) {
        faqService.incrementViewCount(id);
        return ResponseEntity.ok(faqService.getFAQById(id));
    }

    /**
     * POST /api/faqs/{id}/feedback - Feedback cho FAQ
     */
    @PostMapping("/{id}/feedback")
    @Operation(summary = "Submit feedback cho FAQ (helpful/not helpful)")
    public ResponseEntity<Map<String, String>> submitFeedback(
            @PathVariable Long id,
            @RequestBody FAQDTO.FeedbackRequest request) {
        faqService.submitFeedback(id, request.isHelpful());
        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
    }
}
