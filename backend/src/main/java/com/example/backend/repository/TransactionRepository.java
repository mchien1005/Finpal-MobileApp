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

        List<Transaction> findByUserIdAndTransactionSource(Long userId, String transactionSource);

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

        // Dashboard queries
        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
        BigDecimal sumByUserIdAndTypeAndDateRange(
                        @Param("userId") Long userId,
                        @Param("type") Transaction.TransactionType type,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Sum by user and type (without date range - for total balance calculation)
        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
        BigDecimal sumByUserIdAndType(
                        @Param("userId") Long userId,
                        @Param("type") Transaction.TransactionType type);

        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate")
        Long countByUserIdAndDateRange(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
        Long countByUserIdAndTypeAndDateRange(
                        @Param("userId") Long userId,
                        @Param("type") Transaction.TransactionType type,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
        BigDecimal sumByUserIdAndCategoryIdAndTypeAndDateRange(
                        @Param("userId") Long userId,
                        @Param("categoryId") Long categoryId,
                        @Param("type") Transaction.TransactionType type,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT t.category.id, t.category.name, t.category.icon, t.category.color, SUM(t.amount), COUNT(t) " +
                        "FROM Transaction t " +
                        "WHERE t.user.id = :userId " +
                        "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                        "AND (:type IS NULL OR t.type = :type) " +
                        "AND t.category IS NOT NULL " +
                        "GROUP BY t.category.id, t.category.name, t.category.icon, t.category.color " +
                        "ORDER BY SUM(t.amount) DESC")
        List<Object[]> getSpendingByCategory(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("type") Transaction.TransactionType type);

        @Query("SELECT t.id, t.type, t.amount, t.merchant, t.description, t.category.name, t.transactionDate, t.transactionSource "
                        +
                        "FROM Transaction t " +
                        "WHERE t.user.id = :userId " +
                        "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                        "AND (:type IS NULL OR t.type = :type) " +
                        "ORDER BY t.amount DESC")
        List<Object[]> getTopTransactions(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("type") Transaction.TransactionType type,
                        @Param("limit") int limit);

        /**
         * Kiểm tra giao dịch trùng lặp từ SMS
         * Dùng để tránh tạo giao dịch trùng khi user quét SMS nhiều lần
         */
        @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.user.id = :userId " +
                        "AND t.amount = :amount " +
                        "AND t.transactionSource = :transactionSource " +
                        "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                        "AND t.isAuto = true")
        boolean existsDuplicateSMSTransaction(
                        @Param("userId") Long userId,
                        @Param("amount") BigDecimal amount,
                        @Param("transactionSource") String transactionSource,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}