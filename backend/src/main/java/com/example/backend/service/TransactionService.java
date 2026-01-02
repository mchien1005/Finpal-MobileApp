package com.example.backend.service;

import com.example.backend.dto.CategoryPrediction;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.TransactionFilter;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.model.*;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.EncryptionUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

/**
 * Service quản lý Giao dịch (Transaction)
 * Chức năng: CRUD giao dịch, lọc/phân trang, tự động phân loại category bằng AI
 * hoặc rule-based, phát hiện giao dịch bất thường realtime
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryRuleService categoryRuleService;
    private final AICategorizationService aiCategorizationService;
    private final AIInsightsService aiInsightsService;
    private final NotificationService notificationService;
    private final EntityManager entityManager;
    private final EncryptionUtil encryptionUtil;

    public enum CategorizationStrategy {
        AI_FIRST, // Ưu tiên AI → Rule fallback
        RULE_FIRST, // Ưu tiên Rule → AI fallback (mặc định)
        HYBRID, // Dùng cả 2, chọn confidence cao hơn
        RULE_ONLY // Chỉ dùng Rule
    }

    @Value("${ai.categorization.strategy:AI_FIRST}")
    private String strategyConfig;

    private CategorizationStrategy getStrategy() {
        try {
            return CategorizationStrategy.valueOf(strategyConfig);
        } catch (Exception e) {
            return CategorizationStrategy.RULE_FIRST;
        }
    }

    /**
     * Tạo giao dịch mới
     * - Tự động phân loại category: Ưu tiên AI, nếu không đủ confidence thì dùng
     * rule-based
     * 
     * @param request  Dữ liệu giao dịch (amount, type, merchant, description, ...)
     * @param username Tên đăng nhập của user hiện tại
     * @return TransactionResponse chứa thông tin giao dịch vừa tạo
     */
    @Transactional
    @SuppressWarnings("null")
    public TransactionResponse createTransaction(TransactionRequest request, String username) {
        // Lấy thông tin user hiện tại
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo đối tượng transaction mới
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionSource(request.getTransactionSource()); // VCB, TCB, CASH, MOMO...
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.valueOf(request.getType()));
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setIsAuto(request.getIsAuto());
        transaction.setNotes(request.getNotes());
        // Lưu SMS hash vào cột smsContentEncrypted (không cần encrypt vì đã là hash)
        // Hash được tính từ SMSTransactionService và truyền qua smsContent field
        if (request.getSmsContent() != null && !request.getSmsContent().isEmpty()) {
            transaction.setSmsContentEncrypted(request.getSmsContent());
        }
        transaction.setIsVerified(false);
        transaction.setIsAnomaly(false);

        // Tự động phân loại category nếu user chưa chọn
        // QUAN TRỌNG: Phân loại TRƯỚC khi mã hóa description
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.setCategory(category);
            transaction.setCategorizationSource("USER"); // User tự chọn category
        } else {
            // Ưu tiên merchantHint (từ SMS) để phân loại, fallback sang description
            // merchantHint chứa thông tin nhạy cảm nên chỉ dùng để phân loại, KHÔNG lưu DB
            String textToAnalyze = request.getMerchantHint();
            if (textToAnalyze == null || textToAnalyze.trim().isEmpty()) {
                textToAnalyze = request.getDescription();
            }
            if (textToAnalyze != null && !textToAnalyze.trim().isEmpty()) {
                autoCategorizeTransaction(transaction, textToAnalyze.trim(),
                        request.getAmount().doubleValue(), user.getId());
            }
        }

        // MÃ HÓA dữ liệu nhạy cảm trước khi lưu vào DB
        // Description có thể chứa thông tin cá nhân từ giao dịch nhập tay
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            transaction.setDescription(encryptionUtil.encrypt(request.getDescription()));
        }
        // Notes cũng cần được mã hóa
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            transaction.setNotes(encryptionUtil.encrypt(request.getNotes()));
        }

        Transaction saved = transactionRepository.save(transaction);

        // ========================================
        // REALTIME ANOMALY DETECTION
        // Kiểm tra giao dịch bất thường sau khi lưu
        // ========================================
        if (Transaction.TransactionType.EXPENSE.name().equals(request.getType())) {
            try {
                String categoryName = saved.getCategory() != null ? saved.getCategory().getName() : "Khác";

                com.example.backend.dto.AnomalyDetectionResult anomalyResult = aiInsightsService.checkAnomaly(
                        user.getId(),
                        request.getAmount().doubleValue(),
                        request.getDescription(),
                        categoryName);

                if (anomalyResult != null && Boolean.TRUE.equals(anomalyResult.getIsAnomaly())) {
                    // Đánh dấu giao dịch là bất thường
                    saved.setIsAnomaly(true);
                    transactionRepository.save(saved);

                    // Push notification cho user
                    notificationService.sendAnomalyWarning(
                            user,
                            anomalyResult.getMessage() != null ? anomalyResult.getMessage() : anomalyResult.getReason(),
                            saved);

                    log.warn("🚨 Anomaly detected for transaction {}: {} (score: {})",
                            saved.getId(),
                            anomalyResult.getReason(),
                            anomalyResult.getAnomalyScore());
                }
            } catch (Exception e) {
                log.warn("Failed to check anomaly for transaction {}: {}",
                        saved.getId(), e.getMessage());
                // Không throw exception - anomaly check là optional
            }
        }

        return TransactionResponse.fromEntityDecrypted(saved, encryptionUtil::decrypt);
    }

    /**
     * Tự động phân loại transaction theo strategy
     * Nếu không phân loại được → fallback sang danh mục "Khác"
     */
    private void autoCategorizeTransaction(Transaction transaction, String textToAnalyze,
            Double amount, Long userId) {
        CategorizationStrategy strategy = getStrategy();
        log.debug("Using categorization strategy: {}", strategy);

        switch (strategy) {
            case AI_FIRST:
                autoCategorizeAIFirst(transaction, textToAnalyze, amount, userId);
                break;
            case RULE_FIRST:
                autoCategorizeRuleFirst(transaction, textToAnalyze, amount, userId);
                break;
            case HYBRID:
                autoCategorizeHybrid(transaction, textToAnalyze, amount, userId);
                break;
            case RULE_ONLY:
                autoCategorizeRuleOnly(transaction, textToAnalyze);
                break;
        }

        // Fallback: Nếu vẫn chưa có category → gán vào "Khác"
        if (transaction.getCategory() == null) {
            assignDefaultCategory(transaction);
        }
    }

    /**
     * Gán danh mục mặc định "Khác" khi không thể phân loại
     * - EXPENSE → "Khác (Chi)"
     * - INCOME → "Khác (Thu)"
     */
    private void assignDefaultCategory(Transaction transaction) {
        String defaultCategoryName = transaction.getType() == Transaction.TransactionType.EXPENSE
                ? "Khác"
                : "Khác";

        Category defaultCategory = categoryRepository.findByName(defaultCategoryName).orElse(null);

        if (defaultCategory != null) {
            transaction.setCategory(defaultCategory);
            transaction.setCategorizationSource("DEFAULT");
            log.info("📌 Fallback to default category: '{}'", defaultCategoryName);
        } else {
            log.warn("⚠️ Default category '{}' not found in database!", defaultCategoryName);
        }
    }

    /**
     * Strategy 1: AI_FIRST - Ưu tiên AI, fallback sang Rule
     */
    private void autoCategorizeAIFirst(Transaction transaction, String textToAnalyze,
            Double amount, Long userId) {
        // Bước 1: Thử AI trước
        CategoryPrediction aiPrediction = aiCategorizationService.predictCategory(
                textToAnalyze, amount, textToAnalyze, userId);

        if (aiPrediction != null && aiCategorizationService.isConfidentPrediction(aiPrediction)) {
            Category category = categoryRepository.findByName(aiPrediction.getCategory()).orElse(null);
            if (category != null) {
                transaction.setCategory(category);
                transaction.setCategorizationSource("AI");
                transaction.setAiConfidence(aiPrediction.getConfidence());
                log.info("✅ AI_FIRST: AI categorized '{}' -> {} (confidence: {}%)",
                        textToAnalyze, category.getName(), aiPrediction.getConfidence() * 100);
                return;
            }
        }

        // Bước 2: Fallback sang Rule
        Long ruleId = categoryRuleService.suggestCategoryByMerchant(textToAnalyze);
        if (ruleId != null) {
            Category category = categoryRepository.findById(ruleId).orElse(null);
            if (category != null) {
                transaction.setCategory(category);
                transaction.setCategorizationSource("RULE_BASED");
                log.info("⚡ AI_FIRST → RULE fallback: '{}' -> {}", textToAnalyze, category.getName());
            }
        }
    }

    /**
     * Strategy 2: RULE_FIRST - Ưu tiên Rule, fallback sang AI (mặc định)
     */
    private void autoCategorizeRuleFirst(Transaction transaction, String textToAnalyze,
            Double amount, Long userId) {
        // Bước 1: Thử Rule trước
        Long ruleId = categoryRuleService.suggestCategoryByMerchant(textToAnalyze);
        if (ruleId != null) {
            Category category = categoryRepository.findById(ruleId).orElse(null);
            if (category != null) {
                transaction.setCategory(category);
                transaction.setCategorizationSource("RULE_BASED");
                log.info("✅ RULE_FIRST: Rule categorized '{}' -> {}", textToAnalyze, category.getName());
                return;
            }
        }

        // Bước 2: Fallback sang AI
        CategoryPrediction aiPrediction = aiCategorizationService.predictCategory(
                textToAnalyze, amount, textToAnalyze, userId);

        if (aiPrediction != null && aiCategorizationService.isConfidentPrediction(aiPrediction)) {
            Category category = categoryRepository.findByName(aiPrediction.getCategory()).orElse(null);
            if (category != null) {
                transaction.setCategory(category);
                transaction.setCategorizationSource("AI");
                transaction.setAiConfidence(aiPrediction.getConfidence());
                log.info("🤖 RULE_FIRST → AI fallback: '{}' -> {} (confidence: {}%)",
                        textToAnalyze, category.getName(), aiPrediction.getConfidence() * 100);
            }
        }
    }

    /**
     * Strategy 3: HYBRID - Dùng cả Rule và AI, chọn kết quả tốt hơn
     */
    private void autoCategorizeHybrid(Transaction transaction, String textToAnalyze,
            Double amount, Long userId) {
        // Gọi cả 2 song song
        Long ruleId = categoryRuleService.suggestCategoryByMerchant(textToAnalyze);
        CategoryPrediction aiPrediction = aiCategorizationService.predictCategory(
                textToAnalyze, amount, textToAnalyze, userId);

        Category ruleCategory = ruleId != null ? categoryRepository.findById(ruleId).orElse(null) : null;
        Category aiCategory = (aiPrediction != null && aiCategorizationService.isConfidentPrediction(aiPrediction))
                ? categoryRepository.findByName(aiPrediction.getCategory()).orElse(null)
                : null;

        // So sánh và chọn
        if (ruleCategory != null && aiCategory != null) {
            // Cả 2 đều có kết quả
            if (ruleCategory.getId().equals(aiCategory.getId())) {
                // Trùng nhau → chắc chắn hơn
                transaction.setCategory(ruleCategory);
                transaction.setCategorizationSource("HYBRID_MATCHED");
                transaction.setAiConfidence(aiPrediction.getConfidence());
                log.info("🎯 HYBRID: Both match '{}' -> {} (AI confidence: {}%)",
                        textToAnalyze, ruleCategory.getName(), aiPrediction.getConfidence() * 100);
            } else {
                // Khác nhau → ưu tiên AI nếu confidence cao (>= 85%), ngược lại dùng rule
                if (aiPrediction.getConfidence() >= 0.85) {
                    transaction.setCategory(aiCategory);
                    transaction.setCategorizationSource("HYBRID_AI");
                    transaction.setAiConfidence(aiPrediction.getConfidence());
                    log.info("🤖 HYBRID: AI wins (high confidence) '{}' -> {} ({}% > rule: {})",
                            textToAnalyze, aiCategory.getName(),
                            aiPrediction.getConfidence() * 100, ruleCategory.getName());
                } else {
                    transaction.setCategory(ruleCategory);
                    transaction.setCategorizationSource("HYBRID_RULE");
                    log.info("⚡ HYBRID: Rule wins (AI confidence low) '{}' -> {} (AI: {} {}%)",
                            textToAnalyze, ruleCategory.getName(),
                            aiCategory.getName(), aiPrediction.getConfidence() * 100);
                }
            }
        } else if (ruleCategory != null) {
            // Chỉ rule match
            transaction.setCategory(ruleCategory);
            transaction.setCategorizationSource("HYBRID_RULE_ONLY");
            log.info("⚡ HYBRID: Only rule matched '{}' -> {}", textToAnalyze, ruleCategory.getName());
        } else if (aiCategory != null) {
            // Chỉ AI match
            transaction.setCategory(aiCategory);
            transaction.setCategorizationSource("HYBRID_AI_ONLY");
            transaction.setAiConfidence(aiPrediction.getConfidence());
            log.info("🤖 HYBRID: Only AI matched '{}' -> {} ({}%)",
                    textToAnalyze, aiCategory.getName(), aiPrediction.getConfidence() * 100);
        }
    }

    /**
     * Strategy 4: RULE_ONLY - Chỉ dùng Rule, không call AI
     */
    private void autoCategorizeRuleOnly(Transaction transaction, String textToAnalyze) {
        Long ruleId = categoryRuleService.suggestCategoryByMerchant(textToAnalyze);
        if (ruleId != null) {
            Category category = categoryRepository.findById(ruleId).orElse(null);
            if (category != null) {
                transaction.setCategory(category);
                transaction.setCategorizationSource("RULE_BASED");
                log.info("✅ RULE_ONLY: '{}' -> {}", textToAnalyze, category.getName());
            }
        }
    }

    /**
     * Lấy danh sách giao dịch với lọc và phân trang
     * 
     * @param username      Tên đăng nhập của user
     * @param filter        Điều kiện lọc (transactionSource, categoryId, type,
     *                      startDate,
     *                      endDate, merchant, keyword, ...)
     * @param page          Số trang (bắt đầu từ 0)
     * @param size          Số lượng items trên 1 trang
     * @param sortBy        Trường để sắp xếp (ví dụ: "transactionDate")
     * @param sortDirection Hướng sắp xếp ("ASC" hoặc "DESC")
     * @return PageResponse chứa danh sách giao dịch và thông tin phân trang
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PageResponse<TransactionResponse> getTransactions(
            String username,
            TransactionFilter filter,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        Root<Transaction> root = query.from(Transaction.class);

        // Xây dựng các điều kiện lọc (Criteria API)
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("user").get("id"), user.getId()));

        if (filter != null) {
            if (filter.getTransactionSource() != null && !filter.getTransactionSource().isBlank()) {
                predicates.add(cb.equal(root.get("transactionSource"), filter.getTransactionSource()));
            }
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), Transaction.TransactionType.valueOf(filter.getType())));
            }
            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getEndDate()));
            }
            // Merchant filter đã được gộp vào description
            if (filter.getIsAuto() != null) {
                predicates.add(cb.equal(root.get("isAuto"), filter.getIsAuto()));
            }
            if (filter.getIsVerified() != null) {
                predicates.add(cb.equal(root.get("isVerified"), filter.getIsVerified()));
            }
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                // Tìm trong description
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
                // Tìm trong notes
                Predicate notesPredicate = cb.like(cb.lower(root.get("notes")), keyword);
                // Tìm trong tên danh mục (category name)
                Join<Transaction, Category> categoryJoin = root.join("category", JoinType.LEFT);
                Predicate categoryPredicate = cb.like(cb.lower(categoryJoin.get("name")), keyword);
                // Gộp tất cả điều kiện với OR
                predicates.add(cb.or(descPredicate, notesPredicate, categoryPredicate));
            }
        }

        query.where(predicates.toArray(new Predicate[0]));

        // Áp dụng sắp xếp với secondary sort theo ID để đảm bảo thứ tự ổn định
        // Khi nhiều giao dịch có cùng transactionDate, sẽ sắp xếp theo ID giảm dần
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            query.orderBy(cb.desc(root.get(sortBy)), cb.desc(root.get("id")));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)), cb.asc(root.get("id")));
        }

        // Thực thi query và lấy tất cả kết quả
        List<Transaction> allResults = entityManager.createQuery(query).getResultList();

        // Phân trang thủ công (manual pagination)
        int start = page * size;
        int end = Math.min(start + size, allResults.size());
        List<Transaction> pageContent = allResults.subList(start, end);

        List<TransactionResponse> responses = pageContent.stream()
                .map(t -> TransactionResponse.fromEntityDecrypted(t, encryptionUtil::decrypt))
                .collect(Collectors.toList());

        return PageResponse.<TransactionResponse>builder()
                .content(responses)
                .pageNumber(page)
                .pageSize(size)
                .totalElements((long) allResults.size())
                .totalPages((int) Math.ceil((double) allResults.size() / size))
                .first(page == 0)
                .last(end >= allResults.size())
                .build();
    }

    /**
     * Lấy chi tiết giao dịch theo ID
     * 
     * @param id       ID của giao dịch
     * @param username Tên đăng nhập của user (dùng để kiểm tra quyền sở hữu)
     * @return TransactionResponse chứa thông tin chi tiết giao dịch
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public TransactionResponse getTransactionById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Transaction does not belong to user");
        }

        return TransactionResponse.fromEntityDecrypted(transaction, encryptionUtil::decrypt);
    }

    /**
     * Cập nhật thông tin giao dịch
     * 
     * @param id       ID của giao dịch cần cập nhật
     * @param request  Dữ liệu mới (amount, type, merchant, categoryId, ...)
     * @param username Tên đăng nhập của user (dùng để kiểm tra quyền sở hữu)
     * @return TransactionResponse chứa thông tin giao dịch sau khi cập nhật
     */
    @Transactional
    @SuppressWarnings("null")
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Transaction does not belong to user");
        }

        // Cập nhật nguồn giao dịch nếu có
        if (request.getTransactionSource() != null) {
            transaction.setTransactionSource(request.getTransactionSource());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.setCategory(category);
        }

        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.valueOf(request.getType()));
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());

        // Mã hóa description và notes trước khi lưu
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            transaction.setDescription(encryptionUtil.encrypt(request.getDescription()));
        }
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            transaction.setNotes(encryptionUtil.encrypt(request.getNotes()));
        }

        Transaction updated = transactionRepository.save(transaction);
        return TransactionResponse.fromEntityDecrypted(updated, encryptionUtil::decrypt);
    }

    /**
     * Xóa giao dịch
     * 
     * @param id       ID của giao dịch cần xóa
     * @param username Tên đăng nhập của user (dùng để kiểm tra quyền sở hữu)
     */
    @Transactional
    @SuppressWarnings("null")
    public void deleteTransaction(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Transaction does not belong to user");
        }

        transactionRepository.delete(transaction);
    }
}
