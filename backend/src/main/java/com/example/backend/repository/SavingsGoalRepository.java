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

    // Đếm số savings goal theo trạng thái (dùng String để linh hoạt hơn)
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM SavingsGoal s WHERE s.userId = :userId AND s.status = :status")
    Long countByUserIdAndStatus(@org.springframework.data.repository.query.Param("userId") Long userId, 
                                @org.springframework.data.repository.query.Param("status") String status);
}
