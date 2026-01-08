package com.example.backend.repository;

import com.example.backend.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionCode(String permissionCode);

    List<Permission> findByPermissionGroupOrderByDisplayOrderAsc(String permissionGroup);

    List<Permission> findAllByOrderByPermissionGroupAscDisplayOrderAsc();

    boolean existsByPermissionCode(String permissionCode);
}
