package com.example.backend.controller;

import com.example.backend.dto.AccountRequest;
import com.example.backend.dto.AccountResponse;
import com.example.backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(Authentication authentication) {
        String username = authentication.getName();
        List<AccountResponse> accounts = accountService.getAllAccounts(username);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        AccountResponse account = accountService.getAccountById(id, username);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        AccountResponse account = accountService.createAccount(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        AccountResponse account = accountService.updateAccount(id, request, username);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        accountService.deleteAccount(id, username);
        return ResponseEntity.noContent().build();
    }
}
