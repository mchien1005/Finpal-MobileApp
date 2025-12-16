package com.example.backend.repository;

import com.example.backend.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUserId(Long userId);

    @Query("SELECT a FROM AdminUser a WHERE a.user.username = :username")
    Optional<AdminUser> findByUsername(@Param("username") String username);

    List<AdminUser> findByIsActiveTrueOrderByCreatedAtDesc();

    List<AdminUser> findByAdminRoleIdAndIsActiveTrue(Long roleId);

    @Query("SELECT a FROM AdminUser a WHERE a.user.email = :email")
    Optional<AdminUser> findByEmail(@Param("email") String email);

    boolean existsByUserId(Long userId);
}
