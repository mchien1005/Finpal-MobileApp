package com.example.backend.repository;

import com.example.backend.model.Role;
import com.example.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Tìm tất cả users theo role
    List<User> findByRole(Role role);

    // Tìm users theo role và trạng thái hoạt động
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    // Tìm kiếm users theo keyword (username, email, fullName, phone)
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE %:keyword% OR " +
           "LOWER(u.email) LIKE %:keyword% OR " +
           "LOWER(u.fullName) LIKE %:keyword% OR " +
           "u.phone LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    // Đếm số users theo trạng thái
    Long countByIsActive(Boolean isActive);

    // Đếm số users theo role
    Long countByRole(Role role);
}
