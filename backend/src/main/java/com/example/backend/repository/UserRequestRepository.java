package com.example.backend.repository;

import com.example.backend.model.UserRequest;
import com.example.backend.model.UserRequest.RequestStatus;
import com.example.backend.model.UserRequest.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {

    // Tìm tất cả yêu cầu của một người dùng
    List<UserRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Tìm yêu cầu theo trạng thái
    Page<UserRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    // Tìm yêu cầu theo loại
    Page<UserRequest> findByRequestTypeOrderByCreatedAtDesc(RequestType requestType, Pageable pageable);

    // Tìm yêu cầu theo loại và trạng thái
    Page<UserRequest> findByRequestTypeAndStatusOrderByCreatedAtDesc(
            RequestType requestType,
            RequestStatus status,
            Pageable pageable);

    // Đếm số yêu cầu đang chờ duyệt
    long countByStatus(RequestStatus status);

    // Đếm số yêu cầu của một user theo trạng thái
    long countByUserIdAndStatus(Long userId, RequestStatus status);

    // Kiểm tra xem user có yêu cầu PENDING nào không
    boolean existsByUserIdAndStatusAndRequestType(
            Long userId,
            RequestStatus status,
            RequestType requestType);

    // Tìm yêu cầu đã được duyệt nhưng chưa hoàn thành (để xử lý)
    @Query("SELECT ur FROM UserRequest ur " +
            "WHERE ur.status = 'APPROVED' " +
            "AND ur.requestType = :requestType " +
            "ORDER BY ur.approvedAt ASC")
    List<UserRequest> findApprovedRequestsToProcess(@Param("requestType") RequestType requestType);

    // Tìm yêu cầu trong khoảng thời gian
    @Query("SELECT ur FROM UserRequest ur " +
            "WHERE ur.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY ur.createdAt DESC")
    List<UserRequest> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Thống kê yêu cầu theo loại và trạng thái
    @Query("SELECT ur.requestType, ur.status, COUNT(ur) " +
            "FROM UserRequest ur " +
            "GROUP BY ur.requestType, ur.status")
    List<Object[]> getRequestStatistics();

    // Tìm yêu cầu xóa tài khoản đã hoàn thành và đã qua thời gian chờ (24h)
    @Query("SELECT ur FROM UserRequest ur " +
            "WHERE ur.requestType = 'DELETE_ACCOUNT' " +
            "AND ur.status = 'COMPLETED' " +
            "AND ur.emailSentAt IS NOT NULL " +
            "AND ur.emailSentAt < :cutoffTime " +
            "ORDER BY ur.emailSentAt ASC")
    List<UserRequest> findCompletedDeletionRequestsBeforeTime(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Tìm yêu cầu xóa tài khoản sắp được thực thi
    @Query("SELECT ur FROM UserRequest ur " +
            "WHERE ur.requestType = 'DELETE_ACCOUNT' " +
            "AND ur.status = 'COMPLETED' " +
            "AND ur.emailSentAt IS NOT NULL " +
            "AND ur.emailSentAt BETWEEN :startTime AND :endTime " +
            "ORDER BY ur.emailSentAt ASC")
    List<UserRequest> findCompletedDeletionRequestsBetweenTimes(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
