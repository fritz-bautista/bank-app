package com.bank.dao;

import com.bank.config.DbConnection;
import com.bank.exception.AccountNotFoundException;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {
    private static final String SAVE_TRANSACTION = "INSERT INTO transactions (account_number, transaction_reference, transaction_type, amount, balance, reference_account) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String FIND_TRANSACTION = "SELECT * FROM transactions WHERE account_number = ? ORDER BY created_at DESC";
    private static final String FIND_ALL = "SELECT * FROM transactions WHERE account_number = ? ORDER BY created_at DESC LIMIT ?";

    @Override
    public void save(Transaction transaction) throws SQLException {
        try (Connection connection = DbConnection.getConnection();
        ) {
            save(connection, transaction);
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void save(Connection connection, Transaction transaction) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_TRANSACTION, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, transaction.getAccountNumber());
            preparedStatement.setString(2, transaction.getTransactionReference());
            preparedStatement.setString(3, transaction.getTransactionType().name());
            preparedStatement.setBigDecimal(4, transaction.getAmount());
            preparedStatement.setBigDecimal(5, transaction.getBalanceAfter());
            preparedStatement.setString(6, transaction.getReferenceAccount());
            preparedStatement.executeUpdate();
            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setId(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public List<Transaction> findTransactionByAccountNum(String accountNumber) throws AccountNotFoundException, SQLException {
        List<Transaction> results = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_TRANSACTION);
        ) {
            preparedStatement.setString(1, accountNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(initRows(resultSet));
            }
            return results;
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public List<Transaction> findRecentTransaction(String accountNumber, int limit) throws SQLException {
        List<Transaction> results = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)
        ) {
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setInt(2, limit);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(initRows(resultSet));
                }
            }
            return results;
        } catch (SQLException e) {
            throw e;
        }
    }

    public Transaction initRows(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getLong("id"));
        transaction.setAccountNumber(resultSet.getString("account_number"));
        transaction.setTransactionReference(resultSet.getString("transaction_reference"));
        transaction.setTransactionType(TransactionType.valueOf(resultSet.getString("transaction_type")));
        transaction.setAmount(resultSet.getBigDecimal("amount"));
        transaction.setBalanceAfter(resultSet.getBigDecimal("balance"));
        transaction.setReferenceAccount(resultSet.getString("reference_account"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }

        return transaction;
    }
}
