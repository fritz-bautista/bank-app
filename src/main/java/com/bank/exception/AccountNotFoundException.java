package com.bank.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {
        super("Account is not found" + accountNumber);
    }
}
