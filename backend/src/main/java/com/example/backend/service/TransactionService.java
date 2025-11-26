package com.example.backend.service;

import com.example.backend.dto.CategoryPrediction;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.TransactionFilter;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.model.*;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Giao dịch (Transaction)
 * Chức năng: CRUD giao dịch, lọc/phân trang, tự động phân loại category bằng AI
 * hoặc rule-based
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryRuleService categoryRuleService;
    private final AICategorizationService aiCategorizationService;
    private final EntityManager entityManager;

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

        // Kiểm tra tài khoản có thuộc về user không
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUserId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to user");
        }

        // Tạo đối tượng transaction mới
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.valueOf(request.getType()));
        transaction.setMerchant(request.getMerchant());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setIsAuto(request.getIsAuto());
        transaction.setNotes(request.getNotes());
        transaction.setIsVerified(false);
        transaction.setIsAnomaly(false);

        // Tự động phân loại category nếu user chưa chọn
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.setCategory(category);
        } else if (request.getMerchant() != null && !request.getMerchant().trim().isEmpty()) {
            // Bước 1: Thử dùng AI để phân loại trước
            CategoryPrediction aiPrediction = aiCategorizationService.predictCategory(
                    request.getMerchant(),
                    request.getAmount().doubleValue(),
                    request.getDescription());

            if (aiPrediction != null && aiCategorizationService.isConfidentPrediction(aiPrediction)) {
                // Dùng kết quả AI nếu confidence >= threshold (mặc định 70%)
                Category category = categoryRepository.findByName(aiPrediction.getCategory())
                        .orElse(null);
                if (category != null) {
                    transaction.setCategory(category);
                }
            } else {
                // Fallback to rule-based categorization
                Long suggestedCategoryId = categoryRuleService.suggestCategoryByMerchant(request.getMerchant());
                if (suggestedCategoryId != null) {
                    Category category = categoryRepository.findById(suggestedCategoryId)
                            .orElse(null);
                    if (category != null) {
                        transaction.setCategory(category);
                    }
                }
            }
        }

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved);
    }

    /**
     * Lấy danh sách giao dịch với lọc và phân trang
     * 
     * @param username      Tên đăng nhập của user
     * @param filter        Điều kiện lọc (accountId, categoryId, type, startDate,
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
            if (filter.getAccountId() != null) {
                predicates.add(cb.equal(root.get("account").get("id"), filter.getAccountId()));
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
            if (filter.getMerchant() != null && !filter.getMerchant().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("merchant")), "%" + filter.getMerchant().toLowerCase() + "%"));
            }
            if (filter.getIsAuto() != null) {
                predicates.add(cb.equal(root.get("isAuto"), filter.getIsAuto()));
            }
            if (filter.getIsVerified() != null) {
                predicates.add(cb.equal(root.get("isVerified"), filter.getIsVerified()));
            }
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
                Predicate merchantPredicate = cb.like(cb.lower(root.get("merchant")), keyword);
                Predicate notesPredicate = cb.like(cb.lower(root.get("notes")), keyword);
                predicates.add(cb.or(descPredicate, merchantPredicate, notesPredicate));
            }
        }

        query.where(predicates.toArray(new Predicate[0]));

        // Áp dụng sắp xếp
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            query.orderBy(cb.desc(root.get(sortBy)));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)));
        }

        // Thực thi query và lấy tất cả kết quả
        List<Transaction> allResults = entityManager.createQuery(query).getResultList();

        // Phân trang thủ công (manual pagination)
        int start = page * size;
        int end = Math.min(start + size, allResults.size());
        List<Transaction> pageContent = allResults.subList(start, end);

        List<TransactionResponse> responses = pageContent.stream()
                .map(TransactionResponse::fromEntity)
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

        return TransactionResponse.fromEntity(transaction);
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

        // Cập nhật các trường dữ liệu
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            if (!account.getUserId().equals(user.getId())) {
                throw new RuntimeException("Account does not belong to user");
            }
            transaction.setAccount(account);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.setCategory(category);
        }

        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.valueOf(request.getType()));
        transaction.setMerchant(request.getMerchant());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());

        Transaction updated = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(updated);
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
