package com.example.backend.repository;

import com.example.backend.model.FAQ;
import com.example.backend.model.FAQ.FAQCategory;
import com.example.backend.model.FAQ.FAQStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

    Optional<FAQ> findByFaqCode(String faqCode);

    List<FAQ> findByStatus(FAQStatus status);

    List<FAQ> findByCategory(FAQCategory category);

    List<FAQ> findByStatusOrderByDisplayOrderAsc(FAQStatus status);

    List<FAQ> findByStatusAndCategoryOrderByDisplayOrderAsc(FAQStatus status, FAQCategory category);

    List<FAQ> findAllByOrderByDisplayOrderAsc();

    boolean existsByFaqCode(String faqCode);

    long countByStatus(FAQStatus status);
}
