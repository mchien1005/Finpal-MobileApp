package com.example.backend.repository;

import com.example.backend.model.SavingsContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsContributionRepository extends JpaRepository<SavingsContribution, Long> {

    List<SavingsContribution> findBySavingsGoalIdOrderByContributionDateDesc(Long savingsGoalId);

    @Query("SELECT SUM(sc.amount) FROM SavingsContribution sc WHERE sc.savingsGoalId = :goalId")
    java.math.BigDecimal sumContributionsByGoalId(@Param("goalId") Long goalId);
}
