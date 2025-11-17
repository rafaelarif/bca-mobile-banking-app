package com.bca.banking.service;

import com.bca.banking.model.Account;
import com.bca.banking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }
    
    public Optional<Account> getAccountById(Long accountId) {
        if (accountId == null) {
            return Optional.empty();
        }
        return accountRepository.findById(accountId);
    }
    
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
}

