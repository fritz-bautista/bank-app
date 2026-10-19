package com.bank.exception;

import java.math.BigDecimal;

public class InvalidAmountBalance extends RuntimeException {
    private final BigDecimal currentBalance;
    private final BigDecimal amountRequested;

    public InvalidAmountBalance(BigDecimal currentBalance, BigDecimal amountRequested) {
        super(
                String.format("Invalid Amount. Available balance: %.2f Requested balance: %.2f",
                        currentBalance, amountRequested)
        );
        this.currentBalance = currentBalance;
        this.amountRequested = amountRequested;
    }

    public BigDecimal getCurrentBalance() {return currentBalance; }
    public BigDecimal getRuestedAmount() {return amountRequested; }

}
