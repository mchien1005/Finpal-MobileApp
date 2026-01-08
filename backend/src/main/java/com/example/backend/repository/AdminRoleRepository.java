package com.example.backend.repository;

import com.example.backend.model.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {

    Optional<AdminRole> findByRoleCode(String roleCode);

    List<AdminRole> findByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsByRoleCode(String roleCode);
}
