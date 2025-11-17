package com.bca.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String type; // DEBIT, CREDIT, DEPOSIT, WITHDRAWAL
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}

