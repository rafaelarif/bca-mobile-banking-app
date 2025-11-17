package com.bca.banking.controller;

import com.bca.banking.dto.BillPaymentRequest;
import com.bca.banking.model.Payee;
import com.bca.banking.service.BillPaymentService;
import com.bca.banking.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bill-payments")
@CrossOrigin(origins = "*")
public class BillPaymentController {
    
    @Autowired
    private BillPaymentService billPaymentService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }
    
    @GetMapping("/payees")
    public ResponseEntity<?> getPayees(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            List<Payee> payees = billPaymentService.getPayeesByUserId(userId);
            List<Map<String, Object>> payeeList = payees.stream()
                    .map(payee -> {
                        Map<String, Object> payeeMap = new HashMap<>();
                        payeeMap.put("id", payee.getId());
                        payeeMap.put("name", payee.getName());
                        payeeMap.put("accountNumber", payee.getAccountNumber());
                        payeeMap.put("category", payee.getCategory());
                        return payeeMap;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(payeeList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching payees"));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> processBillPayment(
            @Valid @RequestBody BillPaymentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            boolean success = billPaymentService.processBillPayment(
                    request.getAccountId(),
                    request.getPayeeId(),
                    request.getAmount(),
                    request.getMemo(),
                    userId
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Bill payment processed successfully"));
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Bill payment failed. Check account balance and payee."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error processing bill payment: " + e.getMessage()));
        }
    }
    
    @PostMapping("/payees")
    public ResponseEntity<?> addPayee(
            @Valid @RequestBody com.bca.banking.dto.PayeeRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            com.bca.banking.model.Payee payee = billPaymentService.addPayee(
                    request.getName(),
                    request.getAccountNumber(),
                    request.getCategory(),
                    userId
            );
            
            if (payee != null) {
                Map<String, Object> payeeMap = new HashMap<>();
                payeeMap.put("id", payee.getId());
                payeeMap.put("name", payee.getName());
                payeeMap.put("accountNumber", payee.getAccountNumber());
                payeeMap.put("category", payee.getCategory());
                return ResponseEntity.ok(payeeMap);
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Failed to add payee"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error adding payee: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/payees/{payeeId}")
    public ResponseEntity<?> deletePayee(
            @PathVariable Long payeeId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            boolean success = billPaymentService.deletePayee(payeeId, userId);
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Payee deleted successfully"));
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Failed to delete payee"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting payee: " + e.getMessage()));
        }
    }
}

