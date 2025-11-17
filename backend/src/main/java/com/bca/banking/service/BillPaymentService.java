package com.bca.banking.service;

import com.bca.banking.model.Account;
import com.bca.banking.model.Payee;
import com.bca.banking.model.Transaction;
import com.bca.banking.model.User;
import com.bca.banking.repository.AccountRepository;
import com.bca.banking.repository.PayeeRepository;
import com.bca.banking.repository.TransactionRepository;
import com.bca.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BillPaymentService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PayeeRepository payeeRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<Payee> getPayeesByUserId(Long userId) {
        return payeeRepository.findByUserId(userId);
    }
    
    public Payee addPayee(String name, String accountNumber, String category, Long userId) {
        if (userId == null) {
            return null;
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        
        Payee payee = new Payee();
        payee.setName(name);
        payee.setAccountNumber(accountNumber);
        payee.setCategory(category);
        payee.setUser(userOpt.get());
        
        return payeeRepository.save(payee);
    }
    
    public boolean deletePayee(Long payeeId, Long userId) {
        if (payeeId == null || userId == null) {
            return false;
        }
        
        Optional<Payee> payeeOpt = payeeRepository.findById(payeeId);
        if (payeeOpt.isEmpty()) {
            return false;
        }
        
        Payee payee = payeeOpt.get();
        if (!payee.getUser().getId().equals(userId)) {
            return false;
        }
        
        payeeRepository.delete(payee);
        return true;
    }
    
    @Transactional
    public boolean processBillPayment(Long accountId, Long payeeId, BigDecimal amount, String memo, Long userId) {
        if (accountId == null || payeeId == null || userId == null) {
            return false;
        }
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        Optional<Payee> payeeOpt = payeeRepository.findById(payeeId);
        
        if (accountOpt.isEmpty() || payeeOpt.isEmpty()) {
            return false;
        }
        
        Account account = accountOpt.get();
        Payee payee = payeeOpt.get();
        
        // Verify ownership
        if (!account.getUser().getId().equals(userId) || !payee.getUser().getId().equals(userId)) {
            return false;
        }
        
        // Check sufficient balance
        if (account.getBalance().compareTo(amount) < 0) {
            return false;
        }
        
        // Process payment
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setType("DEBIT");
        transaction.setAmount(amount);
        transaction.setDescription(memo != null ? memo : "Bill payment to " + payee.getName());
        transaction.setDate(LocalDateTime.now());
        transaction.setAccount(account);
        transactionRepository.save(transaction);
        
        return true;
    }
}

