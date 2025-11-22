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

/**
 * Controller quản lý Tài khoản người dùng (Accounts)
 *
 * Chức năng chính:
 * - Lấy danh sách tài khoản của user
 * - Lấy chi tiết 1 tài khoản
 * - Tạo / Cập nhật / Xóa tài khoản
 *
 * Lưu ý:
 * - Mọi thao tác đều kiểm tra ownership dựa trên `Authentication`
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    // Service chứa business logic cho accounts
    private final AccountService accountService;

    /**
     * GET /api/accounts
     * Lấy tất cả tài khoản của user hiện tại
     *
     * @param authentication - thông tin user (lấy username)
     * @return List<AccountResponse>
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(Authentication authentication) {
        String username = authentication.getName();
        List<AccountResponse> accounts = accountService.getAllAccounts(username);
        return ResponseEntity.ok(accounts);
    }

    /**
     * GET /api/accounts/{id}
     * Lấy chi tiết 1 tài khoản theo id (kiểm tra user sở hữu)
     *
     * @param id tài khoản id
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        AccountResponse account = accountService.getAccountById(id, username);
        return ResponseEntity.ok(account);
    }

    /**
     * POST /api/accounts
     * Tạo tài khoản mới cho user
     *
     * @param request - AccountRequest chứa tên, loại, số tài khoản, balance...
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        AccountResponse account = accountService.createAccount(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    /**
     * PUT /api/accounts/{id}
     * Cập nhật thông tin tài khoản (chỉ owner mới được phép)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        AccountResponse account = accountService.updateAccount(id, request, username);
        return ResponseEntity.ok(account);
    }

    /**
     * DELETE /api/accounts/{id}
     * Xóa tài khoản (owner kiểm tra) - trả về 204 No Content nếu thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        accountService.deleteAccount(id, username);
        return ResponseEntity.noContent().build();
    }
}
