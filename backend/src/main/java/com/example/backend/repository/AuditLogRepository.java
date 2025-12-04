package com.example.backend.repository;

import com.example.backend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId ORDER BY al.createdAt DESC")
    List<AuditLog> findByEntityTypeAndEntityId(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.createdAt >= :startDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByUserIdAndCreatedAtAfter(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate);
}
