package com.example.backend.repository;

import com.example.backend.model.Role;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Tìm tất cả users theo role
    List<User> findByRole(Role role);

    // Tìm users theo role và trạng thái hoạt động
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);
}
