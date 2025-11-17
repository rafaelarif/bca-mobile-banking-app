package com.bca.banking.config;

import com.bca.banking.model.Account;
import com.bca.banking.model.Payee;
import com.bca.banking.model.Transaction;
import com.bca.banking.model.User;
import com.bca.banking.repository.AccountRepository;
import com.bca.banking.repository.PayeeRepository;
import com.bca.banking.repository.TransactionRepository;
import com.bca.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private PayeeRepository payeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("DataInitializer: Starting data initialization...");
        // Create sample user
        if (userRepository.findByUsername("demo").isEmpty()) {
            logger.info("DataInitializer: Creating demo user with username 'demo' and password 'demo123'");
            User user = new User();
            user.setUsername("demo");
            user.setPassword(passwordEncoder.encode("demo123"));
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setEmail("demo@bca.ca");
            user = userRepository.save(user);
            
            // Create sample accounts
            Account account1 = new Account();
            account1.setAccountNumber("BCA001234567");
            account1.setAccountType("CHEQUING");
            account1.setBalance(new BigDecimal("5000.00"));
            account1.setDescription("Main Chequing Account");
            account1.setUser(user);
            account1 = accountRepository.save(account1);
            
            Account account2 = new Account();
            account2.setAccountNumber("BCA007654321");
            account2.setAccountType("SAVINGS");
            account2.setBalance(new BigDecimal("15000.50"));
            account2.setDescription("Savings Account");
            account2.setUser(user);
            account2 = accountRepository.save(account2);
            
            // Create sample transactions
            Transaction t1 = new Transaction();
            t1.setType("DEPOSIT");
            t1.setAmount(new BigDecimal("1000.00"));
            t1.setDescription("Salary Deposit");
            t1.setDate(LocalDateTime.now().minusDays(5));
            t1.setAccount(account1);
            transactionRepository.save(t1);
            
            Transaction t2 = new Transaction();
            t2.setType("DEBIT");
            t2.setAmount(new BigDecimal("250.00"));
            t2.setDescription("Grocery Store Purchase");
            t2.setDate(LocalDateTime.now().minusDays(3));
            t2.setAccount(account1);
            transactionRepository.save(t2);
            
            Transaction t3 = new Transaction();
            t3.setType("CREDIT");
            t3.setAmount(new BigDecimal("500.00"));
            t3.setDescription("Transfer from Savings");
            t3.setDate(LocalDateTime.now().minusDays(1));
            t3.setAccount(account1);
            transactionRepository.save(t3);
            
            Transaction t4 = new Transaction();
            t4.setType("DEPOSIT");
            t4.setAmount(new BigDecimal("2000.00"));
            t4.setDescription("Initial Deposit");
            t4.setDate(LocalDateTime.now().minusDays(10));
            t4.setAccount(account2);
            transactionRepository.save(t4);
            
            // Create sample payees
            Payee payee1 = new Payee();
            payee1.setName("Hydro Quebec");
            payee1.setAccountNumber("HQ123456789");
            payee1.setCategory("UTILITY");
            payee1.setUser(user);
            payeeRepository.save(payee1);
            
            Payee payee2 = new Payee();
            payee2.setName("Visa Credit Card");
            payee2.setAccountNumber("****1234");
            payee2.setCategory("CREDIT_CARD");
            payee2.setUser(user);
            payeeRepository.save(payee2);
            
            Payee payee3 = new Payee();
            payee3.setName("Rogers Communications");
            payee3.setAccountNumber("RG987654321");
            payee3.setCategory("UTILITY");
            payee3.setUser(user);
            payeeRepository.save(payee3);
            
            Payee payee4 = new Payee();
            payee4.setName("Bell Canada");
            payee4.setAccountNumber("BC456789012");
            payee4.setCategory("UTILITY");
            payee4.setUser(user);
            payeeRepository.save(payee4);
            
            logger.info("DataInitializer: Demo user and sample data created successfully!");
        } else {
            logger.info("DataInitializer: Demo user already exists, skipping initialization.");
        }
    }
}

