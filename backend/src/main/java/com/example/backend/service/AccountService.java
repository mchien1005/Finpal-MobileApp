package com.example.backend.service;

import com.example.backend.dto.AccountRequest;
import com.example.backend.dto.AccountResponse;
import com.example.backend.model.Account;
import com.example.backend.model.User;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Tài khoản Ngân hàng (Account Management)
 * Chức năng: CRUD tài khoản (bank account, e-wallet, cash, ...)
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả tài khoản đang hoạt động của user
     * 
     * @param username Tên đăng nhập của user
     * @return List<AccountResponse> chứa danh sách tài khoản
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết tài khoản theo ID
     * 
     * @param id       ID của tài khoản
     * @param username Tên đăng nhập của user (kiểm tra quyền sở hữu)
     * @return AccountResponse chứa thông tin chi tiết tài khoản
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUserId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to user");
        }

        return AccountResponse.fromEntity(account);
    }

    /**
     * Tạo tài khoản mới
     * 
     * @param request  Dữ liệu tài khoản (bankName, accountName, accountType,
     *                 balance, accountNumber, ...)
     * @param username Tên đăng nhập của user
     * @return AccountResponse chứa thông tin tài khoản vừa tạo
     */
    @Transactional
    public AccountResponse createAccount(AccountRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setUserId(user.getId());
        account.setBankName(request.getBankName());
        account.setAccountName(request.getAccountName());
        account.setAccountType(Account.AccountType.valueOf(request.getAccountType()));
        account.setBalance(request.getBalance());
        account.setAccountNumber(request.getAccountNumber());
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "VND");
        account.setIsActive(true);
        account.setIcon(request.getIcon());
        account.setColor(request.getColor());
        account.setNotes(request.getNotes());

        Account saved = accountRepository.save(account);
        return AccountResponse.fromEntity(saved);
    }

    /**
     * Cập nhật thông tin tài khoản
     * 
     * @param id       ID của tài khoản cần cập nhật
     * @param request  Dữ liệu mới
     * @param username Tên đăng nhập của user (kiểm tra quyền sở hữu)
     * @return AccountResponse chứa thông tin tài khoản sau khi cập nhật
     */
    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUserId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to user");
        }

        account.setBankName(request.getBankName());
        account.setAccountName(request.getAccountName());
        account.setAccountType(Account.AccountType.valueOf(request.getAccountType()));
        account.setBalance(request.getBalance());
        account.setAccountNumber(request.getAccountNumber());
        account.setCurrency(request.getCurrency());
        account.setIcon(request.getIcon());
        account.setColor(request.getColor());
        account.setNotes(request.getNotes());

        Account updated = accountRepository.save(account);
        return AccountResponse.fromEntity(updated);
    }

    /**
     * Xóa tài khoản (soft delete - chỉ set isActive = false)
     * 
     * @param id       ID của tài khoản cần xóa
     * @param username Tên đăng nhập của user (kiểm tra quyền sở hữu)
     */
    @Transactional
    public void deleteAccount(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUserId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to user");
        }

        // Soft delete (không xóa hẳn khỏi database)
        account.setIsActive(false);
        accountRepository.save(account);
    }
}
