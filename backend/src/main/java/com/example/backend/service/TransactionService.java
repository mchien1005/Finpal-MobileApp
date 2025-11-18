package com.example.backend.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryRuleService categoryRuleService;
    private final EntityManager entityManager;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, String username) {
        // Get current user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate account belongs to user
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUserId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to user");
        }

        // Create transaction
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
        transaction.setTags(request.getTags());
        transaction.setIsVerified(false);
        transaction.setIsAnomaly(false);

        // Auto-categorize if category not provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.setCategory(category);
        } else if (request.getMerchant() != null && !request.getMerchant().trim().isEmpty()) {
            // Auto-categorize based on merchant using CategoryRuleService
            Long suggestedCategoryId = categoryRuleService.suggestCategoryByMerchant(request.getMerchant());
            if (suggestedCategoryId != null) {
                Category category = categoryRepository.findById(suggestedCategoryId)
                        .orElse(null);
                if (category != null) {
                    transaction.setCategory(category);
                }
            }
        }

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
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

        // Build predicates
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

        // Sorting
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            query.orderBy(cb.desc(root.get(sortBy)));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)));
        }

        // Execute query
        List<Transaction> allResults = entityManager.createQuery(query).getResultList();

        // Manual pagination
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

    @Transactional(readOnly = true)
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

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Transaction does not belong to user");
        }

        // Update fields
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
        transaction.setTags(request.getTags());

        Transaction updated = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(updated);
    }

    @Transactional
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
