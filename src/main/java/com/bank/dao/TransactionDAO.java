package com.bank.dao;

import com.bank.models.Account;
import com.bank.models.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TransactionDAO {
    void save(Transaction transaction) throws SQLException;
    void save(Connection connection, Transaction transaction) throws SQLException;
    List<Transaction> findTransactionByAccountNum (String accountNumber) throws SQLException;
    List<Transaction> findRecentTransaction (String accountNumber, int limit) throws SQLException;
}
