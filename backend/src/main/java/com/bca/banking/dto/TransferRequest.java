package com.bca.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull(message = "Source account ID is required")
    private Long fromAccountId;
    
    @NotNull(message = "Destination account ID is required")
    private Long toAccountId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String description;
}

