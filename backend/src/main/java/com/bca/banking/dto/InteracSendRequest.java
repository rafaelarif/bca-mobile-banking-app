package com.bca.banking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InteracSendRequest {
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String message;
}

