package com.bank.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class ReferenceGenerator {
    private final String PREFIX = "TXN";
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AtomicLong SEQUENCE = new AtomicLong(0L);

    // This method created a transaction reference
    public String generateReference() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = SEQUENCE.incrementAndGet();
        return String.format("%s-%s%09d", PREFIX, timestamp, sequence);
    }
}
