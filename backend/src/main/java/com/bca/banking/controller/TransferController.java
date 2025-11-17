package com.bca.banking.controller;

import com.bca.banking.dto.TransferRequest;
import com.bca.banking.service.TransferService;
import com.bca.banking.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {
    
    @Autowired
    private TransferService transferService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }
    
    @PostMapping
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            boolean success = transferService.transfer(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    request.getDescription(),
                    userId
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Transfer completed successfully"));
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Transfer failed. Check account balances and ownership."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error processing transfer: " + e.getMessage()));
        }
    }
}

