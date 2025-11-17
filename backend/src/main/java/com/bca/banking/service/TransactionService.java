package com.bca.banking.service;

import com.bca.banking.model.Transaction;
import com.bca.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountIdOrderByDateDesc(accountId);
    }
}

