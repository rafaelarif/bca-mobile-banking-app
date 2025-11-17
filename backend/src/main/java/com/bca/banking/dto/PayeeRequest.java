package com.bca.banking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayeeRequest {
    @NotBlank(message = "Payee name is required")
    private String name;
    
    private String accountNumber;
    
    private String category; // UTILITY, CREDIT_CARD, INSURANCE, etc.
}

