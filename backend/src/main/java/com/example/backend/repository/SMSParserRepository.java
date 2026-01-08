package com.example.backend.repository;

import com.example.backend.model.SMSParser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SMSParserRepository extends JpaRepository<SMSParser, Long> {

    Optional<SMSParser> findBySenderNumberAndIsActiveTrue(String senderNumber);

    List<SMSParser> findByIsActiveTrueOrderByPriorityDesc();

    List<SMSParser> findByIsActiveTrue();
}
