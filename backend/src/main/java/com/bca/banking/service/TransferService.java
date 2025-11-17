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
public class TransferService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Transactional
    public boolean transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description, Long userId) {
        if (fromAccountId == null || toAccountId == null || userId == null) {
            return false;
        }
        Optional<Account> fromAccountOpt = accountRepository.findById(fromAccountId);
        Optional<Account> toAccountOpt = accountRepository.findById(toAccountId);
        
        if (fromAccountOpt.isEmpty() || toAccountOpt.isEmpty()) {
            return false;
        }
        
        Account fromAccount = fromAccountOpt.get();
        Account toAccount = toAccountOpt.get();
        
        // Verify ownership
        if (!fromAccount.getUser().getId().equals(userId)) {
            return false;
        }
        
        // Check sufficient balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            return false;
        }
        
        // Perform transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Create transactions
        Transaction debitTransaction = new Transaction();
        debitTransaction.setType("DEBIT");
        debitTransaction.setAmount(amount);
        debitTransaction.setDescription(description != null ? description : "Transfer to " + toAccount.getAccountNumber());
        debitTransaction.setDate(LocalDateTime.now());
        debitTransaction.setAccount(fromAccount);
        transactionRepository.save(debitTransaction);
        
        Transaction creditTransaction = new Transaction();
        creditTransaction.setType("CREDIT");
        creditTransaction.setAmount(amount);
        creditTransaction.setDescription(description != null ? description : "Transfer from " + fromAccount.getAccountNumber());
        creditTransaction.setDate(LocalDateTime.now());
        creditTransaction.setAccount(toAccount);
        transactionRepository.save(creditTransaction);
        
        return true;
    }
}

