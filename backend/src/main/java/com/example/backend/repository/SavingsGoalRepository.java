package com.example.backend.repository;

import com.example.backend.model.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserId(Long userId);

    List<SavingsGoal> findByUserIdAndStatus(Long userId, SavingsGoal.GoalStatus status);

    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);

    List<SavingsGoal> findByUserIdOrderByCreatedAtDesc(Long userId);
}
