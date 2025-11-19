package com.example.backend.repository;

import com.example.backend.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    List<Account> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

    List<Account> findByUserIdAndAccountType(Long userId, Account.AccountType accountType);

    @Query("SELECT a.balance FROM Account a WHERE a.userId = :userId AND a.isActive = true")
    List<Object[]> findAccountBalancesByUserId(@Param("userId") Long userId);

    List<Account> findByUserIdAndAccountNameContaining(Long userId, String accountNamePart);
}
