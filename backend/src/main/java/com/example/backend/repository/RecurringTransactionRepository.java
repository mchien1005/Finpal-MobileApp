package com.example.backend.repository;

import com.example.backend.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.userId = :userId AND rt.isActive = true AND rt.nextOccurrence <= :date")
    List<RecurringTransaction> findDueRecurringTransactions(@Param("userId") Long userId,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(rt) FROM RecurringTransaction rt WHERE rt.userId = :userId AND rt.isActive = true")
    Long countActiveByUserId(@Param("userId") Long userId);
}
