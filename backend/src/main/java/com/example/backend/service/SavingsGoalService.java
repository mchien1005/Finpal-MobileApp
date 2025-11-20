package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.SavingsContribution;
import com.example.backend.model.SavingsGoal;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Savings Goals Management (FR3.3)
 */
@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsContributionRepository savingsContributionRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả savings goals của user
     */
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getAllSavingsGoals(String username, String status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SavingsGoal> goals;
        if (status != null && !status.isEmpty()) {
            goals = savingsGoalRepository.findByUserIdAndStatus(
                    user.getId(), SavingsGoal.GoalStatus.valueOf(status));
        } else {
            goals = savingsGoalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return goals.stream()
                .map(goal -> convertToResponse(goal, false))
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết savings goal theo ID
     */
    @Transactional(readOnly = true)
    public SavingsGoalResponse getSavingsGoalById(String username, Long goalId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        // Check ownership
        if (!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to savings goal");
        }

        return convertToResponse(goal, true);
    }

    /**
     * Tạo savings goal mới
     */
    @Transactional
    public SavingsGoalResponse createSavingsGoal(String username, SavingsGoalRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = new SavingsGoal();
        goal.setUserId(user.getId());
        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(request.getDeadline());
        goal.setIcon(request.getIcon());
        goal.setColor(request.getColor());
        goal.setStatus(SavingsGoal.GoalStatus.ACTIVE);

        SavingsGoal savedGoal = savingsGoalRepository.save(goal);
        return convertToResponse(savedGoal, false);
    }

    /**
     * Cập nhật savings goal
     */
    @Transactional
    public SavingsGoalResponse updateSavingsGoal(String username, Long goalId, SavingsGoalRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        // Check ownership
        if (!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to savings goal");
        }

        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setDeadline(request.getDeadline());
        goal.setIcon(request.getIcon());
        goal.setColor(request.getColor());

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);
        return convertToResponse(updatedGoal, false);
    }

    /**
     * Xóa savings goal
     */
    @Transactional
    public void deleteSavingsGoal(String username, Long goalId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        // Check ownership
        if (!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to savings goal");
        }

        savingsGoalRepository.delete(goal);
    }

    /**
     * Đóng góp vào savings goal
     */
    @Transactional
    public SavingsGoalResponse addContribution(String username, Long goalId, SavingsContributionRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        // Check ownership
        if (!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to savings goal");
        }

        // Check if goal is active
        if (goal.getStatus() != SavingsGoal.GoalStatus.ACTIVE) {
            throw new RuntimeException("Cannot add contribution to inactive goal");
        }

        // Create contribution
        SavingsContribution contribution = SavingsContribution.builder()
                .savingsGoalId(goalId)
                .amount(request.getAmount())
                .contributionDate(
                        request.getContributionDate() != null ? request.getContributionDate() : LocalDate.now())
                .notes(request.getNotes())
                .build();

        savingsContributionRepository.save(contribution);

        // Update goal's current amount
        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        // Check if goal is completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.COMPLETED);
            goal.setCompletedAt(java.time.LocalDateTime.now());
        }

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);
        return convertToResponse(updatedGoal, true);
    }

    /**
     * Lấy contributions của một savings goal
     */
    @Transactional(readOnly = true)
    public List<SavingsContributionResponse> getContributions(String username, Long goalId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        // Check ownership
        if (!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to savings goal");
        }

        List<SavingsContribution> contributions = savingsContributionRepository
                .findBySavingsGoalIdOrderByContributionDateDesc(goalId);

        return contributions.stream()
                .map(this::convertContributionToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert SavingsGoal to Response DTO
     */
    private SavingsGoalResponse convertToResponse(SavingsGoal goal, boolean includeContributions) {
        SavingsGoalResponse response = new SavingsGoalResponse();
        response.setId(goal.getId());
        response.setUserId(goal.getUserId());
        response.setName(goal.getName());
        response.setDescription(goal.getDescription());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCurrentAmount(goal.getCurrentAmount());
        response.setDeadline(goal.getDeadline());
        response.setIcon(goal.getIcon());
        response.setColor(goal.getColor());
        response.setStatus(goal.getStatus().toString());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());
        response.setCompletedAt(goal.getCompletedAt());

        // Calculate progress
        calculateProgress(goal, response);

        // Include recent contributions if requested
        if (includeContributions) {
            List<SavingsContribution> contributions = savingsContributionRepository
                    .findBySavingsGoalIdOrderByContributionDateDesc(goal.getId())
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            response.setRecentContributions(
                    contributions.stream()
                            .map(this::convertContributionToResponse)
                            .collect(Collectors.toList()));
        }

        return response;
    }

    /**
     * Tính toán tiến độ savings goal
     */
    private void calculateProgress(SavingsGoal goal, SavingsGoalResponse response) {
        // Progress percentage
        double progressPercentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        response.setProgressPercentage(Math.min(progressPercentage, 100.0));

        // Remaining amount
        response.setRemainingAmount(goal.getTargetAmount().subtract(goal.getCurrentAmount()));

        // Days remaining
        if (goal.getDeadline() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(goal.getDeadline())) {
                response.setDaysRemaining(0);
            } else {
                response.setDaysRemaining((int) ChronoUnit.DAYS.between(today, goal.getDeadline()) + 1);
            }
        }

        // Progress status
        String progressStatus;
        if (goal.getStatus() == SavingsGoal.GoalStatus.COMPLETED) {
            progressStatus = "COMPLETED";
        } else if (goal.getDeadline() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(goal.getDeadline())) {
                progressStatus = "OVERDUE";
            } else if (progressPercentage >= 70) {
                progressStatus = "ON_TRACK";
            } else {
                long totalDays = ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), goal.getDeadline());
                long daysPassed = ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), today);
                double timeProgress = totalDays > 0 ? (double) daysPassed / totalDays * 100 : 0;

                if (progressPercentage < timeProgress - 20) {
                    progressStatus = "AT_RISK";
                } else {
                    progressStatus = "ON_TRACK";
                }
            }
        } else {
            progressStatus = progressPercentage >= 50 ? "ON_TRACK" : "AT_RISK";
        }
        response.setProgressStatus(progressStatus);
    }

    /**
     * Convert Contribution to Response DTO
     */
    private SavingsContributionResponse convertContributionToResponse(SavingsContribution contribution) {
        return SavingsContributionResponse.builder()
                .id(contribution.getId())
                .savingsGoalId(contribution.getSavingsGoalId())
                .amount(contribution.getAmount())
                .contributionDate(contribution.getContributionDate())
                .notes(contribution.getNotes())
                .createdAt(contribution.getCreatedAt())
                .build();
    }
}
