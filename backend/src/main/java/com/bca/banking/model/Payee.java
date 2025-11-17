package com.bca.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String accountNumber;
    
    @Column
    private String category; // UTILITY, CREDIT_CARD, INSURANCE, etc.
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

