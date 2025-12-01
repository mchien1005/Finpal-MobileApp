package com.example.backend.repository;

import com.example.backend.model.NotificationTemplate;
import com.example.backend.model.NotificationTemplate.TemplateStatus;
import com.example.backend.model.NotificationTemplate.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    List<NotificationTemplate> findByStatus(TemplateStatus status);

    List<NotificationTemplate> findByType(TemplateType type);

    List<NotificationTemplate> findByStatusOrderByCreatedAtDesc(TemplateStatus status);

    List<NotificationTemplate> findAllByOrderByCreatedAtDesc();

    boolean existsByTemplateCode(String templateCode);

    long countByStatus(TemplateStatus status);
}
