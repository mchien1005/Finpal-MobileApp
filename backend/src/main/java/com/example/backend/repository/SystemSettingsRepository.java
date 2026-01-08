package com.example.backend.repository;

import com.example.backend.model.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    Optional<SystemSettings> findBySettingKey(String settingKey);

    List<SystemSettings> findBySettingGroup(String settingGroup);

    List<SystemSettings> findAllByOrderBySettingGroupAscSettingKeyAsc();

    boolean existsBySettingKey(String settingKey);
}
