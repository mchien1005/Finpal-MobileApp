package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "nhat_ky_he_thong", indexes = {
        @Index(name = "idx_nguoi_dung", columnList = "id_nguoi_dung"),
        @Index(name = "idx_ngay_tao", columnList = "ngay_tao"),
        @Index(name = "idx_doi_tuong", columnList = "loai_doi_tuong,id_doi_tuong")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_nguoi_dung")
    private Long userId;

    @Column(name = "hanh_dong", nullable = false, length = 100)
    private String action;

    @Column(name = "loai_doi_tuong", nullable = false, length = 50)
    private String entityType;

    @Column(name = "id_doi_tuong")
    private Long entityId;

    @Column(name = "gia_tri_cu", columnDefinition = "JSON")
    private String oldValue;

    @Column(name = "gia_tri_moi", columnDefinition = "JSON")
    private String newValue;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
