package com.bank.dao;
import com.bank.exception.AccountNotFoundException;
import com.bank.models.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDAO {
    void createAccount(Account account) throws SQLException;
    Optional<Account> findById(String accountNumber) throws AccountNotFoundException, SQLException;
    List<Account> listAccounts() throws SQLException;
    void updateBalance(String accountNumber, BigDecimal balance) throws SQLException;
    void updateBalance(Connection connection,String accountNumber, BigDecimal balance) throws SQLException;
    boolean checkNames(String accountName) throws SQLException;
}
