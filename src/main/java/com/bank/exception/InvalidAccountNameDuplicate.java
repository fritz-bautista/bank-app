package com.bank.exception;

public class InvalidAccountNameDuplicate extends RuntimeException {
    public InvalidAccountNameDuplicate(String message) {
        super(message);
    }
}
