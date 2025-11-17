package com.bca.banking.controller;

import com.bca.banking.model.Account;
import com.bca.banking.service.AccountService;
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
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {
    
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
    public ResponseEntity<?> getAccounts() {
        try {
            Long userId = getUserIdFromAuthentication();
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            List<Map<String, Object>> accountList = accounts.stream()
                    .map(account -> {
                        Map<String, Object> accountMap = new HashMap<>();
                        accountMap.put("id", account.getId());
                        accountMap.put("accountNumber", account.getAccountNumber());
                        accountMap.put("accountType", account.getAccountType());
                        accountMap.put("balance", account.getBalance());
                        accountMap.put("description", account.getDescription());
                        return accountMap;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(accountList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching accounts"));
        }
    }
    
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<?> getAccountBalance(@PathVariable Long accountId) {
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
                        Map<String, Object> response = new HashMap<>();
                        response.put("balance", account.getBalance());
                        response.put("accountNumber", account.getAccountNumber());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(404).body(Map.of("message", "Account not found")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching balance"));
        }
    }
}

