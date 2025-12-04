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
 * Service quản lý Mục tiêu Tiết kiệm (Savings Goals) - FR3.3
 * Chức năng: Tạo/sửa/xóa mục tiêu, đóng góp tiền, theo dõi tiến độ, cảnh báo
 * deadline
 */
@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsContributionRepository savingsContributionRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả mục tiêu tiết kiệm của user
     * 
     * @param username Tên đăng nhập của user
     * @param status   Lọc theo trạng thái (ACTIVE/COMPLETED/PAUSED/CANCELLED, nếu
     *                 null thì lấy tất cả)
     * @return List<SavingsGoalResponse> chứa danh sách mục tiêu và tiến độ
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
     * Lấy chi tiết mục tiêu tiết kiệm theo ID (bao gồm 5 đóng góp gần nhất)
     * 
     * @param username Tên đăng nhập của user (kiểm tra quyền sở hữu)
     * @param goalId   ID của mục tiêu
     * @return SavingsGoalResponse chứa thông tin chi tiết và tiến độ
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
     * Tạo mục tiêu tiết kiệm mới
     * 
     * @param username Tên đăng nhập của user
     * @param request  Dữ liệu mục tiêu (name, description, targetAmount, deadline,
     *                 icon, color)
     * @return SavingsGoalResponse chứa thông tin mục tiêu vừa tạo (currentAmount =
     *         0)
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
        goal.setStatus(SavingsGoal.GoalStatus.ACTIVE);

        SavingsGoal savedGoal = savingsGoalRepository.save(goal);
        return convertToResponse(savedGoal, false);
    }

    /**
     * Cập nhật mục tiêu tiết kiệm
     * 
     * @param username Tên đăng nhập của user
     * @param goalId   ID của mục tiêu cần cập nhật
     * @param request  Dữ liệu mới (name, description, targetAmount, deadline)
     * @return SavingsGoalResponse chứa thông tin mục tiêu sau khi cập nhật
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

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);
        return convertToResponse(updatedGoal, false);
    }

    /**
     * Xóa mục tiêu tiết kiệm (hard delete)
     * 
     * @param username Tên đăng nhập của user
     * @param goalId   ID của mục tiêu cần xóa
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
     * Đóng góp tiền vào mục tiêu tiết kiệm
     * - Cập nhật currentAmount
     * - Tự động chuyển trạng thái sang COMPLETED nếu đạt mục tiêu
     * 
     * @param username Tên đăng nhập của user
     * @param goalId   ID của mục tiêu
     * @param request  Dữ liệu đóng góp (amount, contributionDate, notes)
     * @return SavingsGoalResponse chứa thông tin mục tiêu sau khi đóng góp
     */
    @Transactional
    public SavingsGoalResponse addContribution(String username, Long goalId, SavingsContributionRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        // Kiểm tra quyền sở hữu
        if (!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to savings goal");
        }

        // Kiểm tra mục tiêu có đang hoạt động không
        if (goal.getStatus() != SavingsGoal.GoalStatus.ACTIVE) {
            throw new RuntimeException("Cannot add contribution to inactive goal");
        }

        // Tạo contribution mới
        SavingsContribution contribution = SavingsContribution.builder()
                .savingsGoalId(goalId)
                .amount(request.getAmount())
                .contributionDate(
                        request.getContributionDate() != null ? request.getContributionDate() : LocalDate.now())
                .notes(request.getNotes())
                .build();

        savingsContributionRepository.save(contribution);

        // Cập nhật số tiền hiện tại của mục tiêu
        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        // Kiểm tra xem đã hoàn thành mục tiêu chưa
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.COMPLETED);
            goal.setCompletedAt(java.time.LocalDateTime.now());
        }

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);
        return convertToResponse(updatedGoal, true);
    }

    /**
     * Lấy danh sách tất cả các lần đóng góp của một mục tiêu
     * 
     * @param username Tên đăng nhập của user
     * @param goalId   ID của mục tiêu
     * @return List<SavingsContributionResponse> chứa danh sách đóng góp (sắp xếp
     *         theo ngày giảm dần)
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
     * Chuyển SavingsGoal entity sang SavingsGoalResponse DTO
     * 
     * @param goal                 Entity cần chuyển
     * @param includeContributions Có bao gồm 5 đóng góp gần nhất không
     * @return SavingsGoalResponse chứa đầy đủ thông tin và tiến độ
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
        response.setStatus(goal.getStatus().toString());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());
        response.setCompletedAt(goal.getCompletedAt());

        // Tính toán tiến độ (%, số tiền còn lại, số ngày còn lại, trạng thái)
        calculateProgress(goal, response);

        // Bao gồm 5 đóng góp gần nhất nếu yêu cầu
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
     * Tính toán tiến độ mục tiêu tiết kiệm
     * - Phần trăm hoàn thành (progressPercentage)
     * - Số tiền còn thiếu (remainingAmount)
     * - Số ngày còn lại (daysRemaining)
     * - Trạng thái tiến độ (COMPLETED/ON_TRACK/AT_RISK/OVERDUE)
     */
    private void calculateProgress(SavingsGoal goal, SavingsGoalResponse response) {
        // Tính phần trăm tiến độ
        double progressPercentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        response.setProgressPercentage(Math.min(progressPercentage, 100.0));

        // Số tiền còn thiếu
        response.setRemainingAmount(goal.getTargetAmount().subtract(goal.getCurrentAmount()));

        // Số ngày còn lại (tính từ hôm nay đến deadline)
        if (goal.getDeadline() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(goal.getDeadline())) {
                response.setDaysRemaining(0);
            } else {
                response.setDaysRemaining((int) ChronoUnit.DAYS.between(today, goal.getDeadline()) + 1);
            }
        }

        // Xác định trạng thái tiến độ (COMPLETED/ON_TRACK/AT_RISK/OVERDUE)
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
     * Chuyển SavingsContribution entity sang SavingsContributionResponse DTO
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
