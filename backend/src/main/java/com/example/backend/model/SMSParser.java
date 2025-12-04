package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bo_phan_tich_sms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSParser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_ngan_hang", nullable = false, length = 100)
    private String bankName;

    @Column(name = "ma_ngan_hang", nullable = false, length = 20)
    private String bankCode;

    @Column(name = "so_dien_thoai_gui", length = 20)
    private String senderNumber;

    @Column(name = "mau_regex", nullable = false, columnDefinition = "TEXT")
    private String regexPattern;

    @Column(name = "anh_xa_truong", columnDefinition = "JSON")
    private String fieldMappings;

    @Column(name = "sms_mau", columnDefinition = "TEXT")
    private String sampleSms;

    @Column(name = "dang_hoat_dong", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "do_uu_tien", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @CreationTimestamp
    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;
}
