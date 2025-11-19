package com.example.backend.controller;

import com.example.backend.dto.SMSParserRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.service.SMSTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SMSController {

    private final SMSTransactionService smsTransactionService;

    @PostMapping("/receive")
    public ResponseEntity<?> receiveSMS(
            @Valid @RequestBody SMSParserRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();

            TransactionResponse response = smsTransactionService.processSMSTransaction(
                    request.getSmsContent(),
                    request.getSenderPhone(),
                    username);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage()));
        }
    }

    // Simple error response DTO
    record ErrorResponse(String message) {
    }
}
