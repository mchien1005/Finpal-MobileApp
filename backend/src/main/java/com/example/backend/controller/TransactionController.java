package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.TransactionFilter;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.createTransaction(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String merchant,
            @RequestParam(required = false) Boolean isAuto,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {

        TransactionFilter filter = new TransactionFilter();
        filter.setAccountId(accountId);
        filter.setCategoryId(categoryId);
        filter.setType(type);
        filter.setStartDate(startDate != null ? startDate.atStartOfDay() : null);
        filter.setEndDate(endDate != null ? endDate.atTime(23, 59, 59) : null);
        filter.setMerchant(merchant);
        filter.setIsAuto(isAuto);
        filter.setIsVerified(isVerified);
        filter.setKeyword(keyword);

        String username = authentication.getName();
        PageResponse<TransactionResponse> response = transactionService.getTransactions(
                username, filter, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.getTransactionById(id, username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.updateTransaction(id, request, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        transactionService.deleteTransaction(id, username);
        return ResponseEntity.noContent().build();
    }
}
