package com.bca.banking.controller;

import com.bca.banking.dto.InteracRequestRequest;
import com.bca.banking.dto.InteracSendRequest;
import com.bca.banking.service.InteracService;
import com.bca.banking.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/interac")
@CrossOrigin(origins = "*")
public class InteracController {
    
    @Autowired
    private InteracService interacService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }
    
    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(
            @Valid @RequestBody InteracSendRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            boolean success = interacService.sendMoney(
                    request.getAccountId(),
                    request.getRecipientEmail(),
                    request.getAmount(),
                    request.getMessage(),
                    userId
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Interac e-Transfer sent successfully"));
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Transfer failed. Check account balance and ownership."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error processing transfer: " + e.getMessage()));
        }
    }
    
    @PostMapping("/request")
    public ResponseEntity<?> requestMoney(
            @Valid @RequestBody InteracRequestRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            boolean success = interacService.requestMoney(
                    request.getAccountId(),
                    request.getRequestorEmail(),
                    request.getAmount(),
                    request.getMessage(),
                    userId
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Money request sent successfully"));
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Request failed. Check account ownership."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error processing request: " + e.getMessage()));
        }
    }
}

