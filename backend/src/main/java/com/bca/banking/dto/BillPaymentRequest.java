package com.bca.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BillPaymentRequest {
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "Payee ID is required")
    private Long payeeId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String memo;
}

