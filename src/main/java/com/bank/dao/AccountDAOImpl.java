package com.bank.dao;

import com.bank.config.DbConnection;
import com.bank.exception.AccountNotFoundException;
import com.bank.models.Account;
import jdk.swing.interop.DropTargetContextWrapper;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAOImpl implements AccountDAO {

    private static final String INSERT_QUERY = "INSERT INTO accounts (account_number, account_name, balance) VALUES (?, ?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM accounts";
    private static final String SELECT_ACCOUNT = "SELECT * FROM accounts WHERE account_number = ?";
    private static final String UPDATE_ACCOUNT = "UPDATE accounts SET balance = ? WHERE account_number = ?";
    private static final String CHECK_ALL_NAMES = "SELECT account_name FROM accounts WHERE account_name = ? LIMIT 1";

    @Override
    public void createAccount(Account account) throws SQLException {
        try (Connection connection = DbConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setString(2, account.getAccountName());
            preparedStatement.setBigDecimal(3, account.getBalance());
            preparedStatement.executeUpdate();
            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public Optional<Account> findById(String accountNumber) throws AccountNotFoundException, SQLException {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ACCOUNT)
        ) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(initRows(result));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding account", e);
        }
    }

    @Override
    public List<Account> listAccounts() {
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                accounts.add(initRows(resultSet));
            }
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkNames(String accountName) throws SQLException {
        boolean hasData = false;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_ALL_NAMES)
        ) {
            preparedStatement.setString(1, accountName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    hasData = true;
                }
            }
            return hasData;
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal amount) throws SQLException {
        try (Connection connection = DbConnection.getConnection()) {
            updateBalance(connection, accountNumber, amount);
        } catch (SQLException e) {
            throw e;
        }
    }

    public void updateBalance(Connection connection, String accountNumber, BigDecimal amount) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ACCOUNT)) {
            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setString(2, accountNumber);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Balance update affected rows are 0");
            }
        }
    }

    public Account initRows(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(resultSet.getLong("id"));
        account.setAccountName(resultSet.getString("account_name"));
        account.setAccountNumber(resultSet.getString("account_number"));
        account.setBalance(resultSet.getBigDecimal("balance"));
        account.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return account;
    }
}
