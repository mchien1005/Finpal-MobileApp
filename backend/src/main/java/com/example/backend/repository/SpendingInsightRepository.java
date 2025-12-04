package com.example.backend.repository;

import com.example.backend.model.SpendingInsightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpendingInsightRepository extends JpaRepository<SpendingInsightEntity, Long> {

    @Query("SELECT si FROM SpendingInsightEntity si WHERE si.userId = :userId AND si.periodType = :periodType AND si.startDate = :startDate")
    Optional<SpendingInsightEntity> findByUserIdAndPeriodTypeAndStartDate(
            @Param("userId") Long userId,
            @Param("periodType") SpendingInsightEntity.PeriodType periodType,
            @Param("startDate") LocalDate startDate);

    @Query("SELECT si FROM SpendingInsightEntity si WHERE si.userId = :userId ORDER BY si.startDate DESC")
    List<SpendingInsightEntity> findByUserIdOrderByStartDateDesc(@Param("userId") Long userId);

    @Query("SELECT si FROM SpendingInsightEntity si WHERE si.userId = :userId AND si.periodType = :periodType ORDER BY si.startDate DESC")
    List<SpendingInsightEntity> findByUserIdAndPeriodTypeOrderByStartDateDesc(
            @Param("userId") Long userId,
            @Param("periodType") SpendingInsightEntity.PeriodType periodType);
}
