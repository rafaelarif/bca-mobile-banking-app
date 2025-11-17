package com.bca.banking.controller;

import com.bca.banking.model.Transaction;
import com.bca.banking.service.AccountService;
import com.bca.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private AccountService accountService;
    
    private Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
            Object userIdObj = details.get("userId");
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
        }
        return null;
    }
    
    @GetMapping
    public ResponseEntity<?> getTransactions(@PathVariable Long accountId) {
        try {
            Long userId = getUserIdFromAuthentication();
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            return accountService.getAccountById(accountId)
                    .map(account -> {
                        if (!account.getUser().getId().equals(userId)) {
                            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
                        }
                        
                        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
                        List<Map<String, Object>> transactionList = transactions.stream()
                                .map(transaction -> {
                                    Map<String, Object> transactionMap = new HashMap<>();
                                    transactionMap.put("id", transaction.getId());
                                    transactionMap.put("type", transaction.getType());
                                    transactionMap.put("amount", transaction.getAmount());
                                    transactionMap.put("description", transaction.getDescription());
                                    transactionMap.put("date", transaction.getDate().toString());
                                    return transactionMap;
                                })
                                .collect(Collectors.toList());
                        
                        return ResponseEntity.ok(transactionList);
                    })
                    .orElse(ResponseEntity.status(404).body(Map.of("message", "Account not found")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching transactions"));
        }
    }
}

