package com.bca.banking.service;

import com.bca.banking.model.Account;
import com.bca.banking.model.Transaction;
import com.bca.banking.repository.AccountRepository;
import com.bca.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InteracService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Transactional
    public boolean sendMoney(Long accountId, String recipientEmail, BigDecimal amount, String message, Long userId) {
        if (accountId == null || userId == null) {
            return false;
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            return false;
        }
        
        Account account = accountOpt.get();
        
        // Verify ownership
        if (!account.getUser().getId().equals(userId)) {
            return false;
        }
        
        // Check sufficient balance
        if (account.getBalance().compareTo(amount) < 0) {
            return false;
        }
        
        // Process Interac e-Transfer (simulated)
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setType("DEBIT");
        transaction.setAmount(amount);
        transaction.setDescription(message != null ? message : "Interac e-Transfer to " + recipientEmail);
        transaction.setDate(LocalDateTime.now());
        transaction.setAccount(account);
        transactionRepository.save(transaction);
        
        return true;
    }
    
    public boolean requestMoney(Long accountId, String requestorEmail, BigDecimal amount, String message, Long userId) {
        if (accountId == null || userId == null) {
            return false;
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            return false;
        }
        
        Account account = accountOpt.get();
        
        // Verify ownership
        if (!account.getUser().getId().equals(userId)) {
            return false;
        }
        
        // For request, we just create a record (simulated)
        // In a real system, this would send a notification to the requestor
        return true;
    }
}

