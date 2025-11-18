package com.example.backend.repository;

import com.example.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

        List<Transaction> findByUserIdAndTransactionDateBetween(
                        Long userId,
                        LocalDateTime startDate,
                        LocalDateTime endDate);

        List<Transaction> findByUserIdAndType(Long userId, Transaction.TransactionType type);

        List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

        List<Transaction> findByAccountId(Long accountId);

        @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
        BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
                        @Param("userId") Long userId,
                        @Param("type") Transaction.TransactionType type,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.transactionDate BETWEEN :startDate AND :endDate")
        List<Transaction> findByCategoryAndDateRange(
                        @Param("userId") Long userId,
                        @Param("categoryId") Long categoryId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.isAuto = true")
        Long countAutoTransactionsByUserId(@Param("userId") Long userId);
}
