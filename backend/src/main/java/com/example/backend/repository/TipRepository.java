package com.example.backend.repository;

import com.example.backend.model.Tip;
import com.example.backend.model.Tip.TipCategory;
import com.example.backend.model.Tip.TipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipRepository extends JpaRepository<Tip, Long> {

    Optional<Tip> findByTipCode(String tipCode);

    List<Tip> findByStatus(TipStatus status);

    List<Tip> findByCategory(TipCategory category);

    List<Tip> findByStatusOrderByDisplayOrderAsc(TipStatus status);

    List<Tip> findByStatusAndCategoryOrderByDisplayOrderAsc(TipStatus status, TipCategory category);

    List<Tip> findAllByOrderByDisplayOrderAsc();

    boolean existsByTipCode(String tipCode);

    long countByStatus(TipStatus status);
}
