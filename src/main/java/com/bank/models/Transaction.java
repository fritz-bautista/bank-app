package com.bank.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter

public class Transaction {
    private Long id;
    private String transactionReference;
    private String accountNumber;
    TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceAccount;
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(String transactionReference, String accountNumber, TransactionType transactionType, BigDecimal amount, BigDecimal balance) {
        this.transactionReference = transactionReference;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balance;
    }
}


