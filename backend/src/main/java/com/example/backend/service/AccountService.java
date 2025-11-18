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

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public void deleteAccount(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUserId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to user");
        }

        // Soft delete
        account.setIsActive(false);
        accountRepository.save(account);
    }
}
