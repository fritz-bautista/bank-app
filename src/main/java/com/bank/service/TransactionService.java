package com.bank.service;

import com.bank.config.DbConnection;
import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InvalidAmountBalance;
import com.bank.exception.InvalidTransactionException;
import com.bank.models.Account;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.util.ReferenceGenerator;

import javax.crypto.spec.PSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Ref;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;


public class TransactionService {
    private final ReferenceGenerator referenceGenerator;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final AccountService accountService;
    private final Scanner input;

    private static final String line = "---------------------------";
    private static final String DEPOSIT = "|---------- Deposit ----------|";
    private static final String WITHDRAW = "|---------- Withdraw ----------|";
    private static final String TRANSFER = "|---------- Transfer ----------|";

    public TransactionService(ReferenceGenerator referenceGenerator, AccountDAO accountDAO, TransactionDAO transactionDAO, AccountService accountService, Scanner input) {
        this.referenceGenerator = referenceGenerator;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.accountService = accountService;
        this.input = input;
    }

    public void deposit() throws SQLException {
        System.out.println(DEPOSIT);

        try {
            Account account = requestAccountNumber();

            BigDecimal amount = requestPositiveAmount("Enter amount to deposit: ");

            BigDecimal newBalance = account.getBalance().add(amount);
            accountDAO.updateBalance(account.getAccountNumber(), newBalance);

            Transaction transaction = new Transaction(
                    referenceGenerator.generateReference(),
                    account.getAccountNumber(),
                    TransactionType.DEPOSIT,
                    amount,
                    newBalance
            );
            transaction.setCreatedAt(LocalDateTime.now());

            transactionDAO.save(transaction);

            System.out.println("Deposit Transaction Successful!");
            printReceipt(transaction);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void withdraw() throws SQLException {
        System.out.println(WITHDRAW);

        try {
            Account account = requestAccountNumber();

            String accountNumber = account.getAccountNumber();
            BigDecimal amount = requestPositiveAmount("Enter amount to withdraw: ");

            validateAmountBalance(account, amount);

            BigDecimal newBalance = account.getBalance().subtract(amount);
            accountDAO.updateBalance(accountNumber, newBalance);

            Transaction transaction = new Transaction(
                    referenceGenerator.generateReference(),
                    accountNumber,
                    TransactionType.WITHDRAW,
                    amount,
                    newBalance
            );
            transaction.setCreatedAt(LocalDateTime.now());

            transactionDAO.save(transaction);

            System.out.println("Withdraw Transaction Successful!");
            printReceipt(transaction);

        } catch (SQLException e) {

        }
    }

    public void transfer() throws SQLException{
        System.out.println(TRANSFER);

        Account accountSender = null;
        Account accountReceiver = null;
        BigDecimal amount = null;

        try {
            accountSender = requestAccountNumber("Enter account number of sender: ");
            accountReceiver = requestAccountNumber("Enter account number of receiver: ");

            if (Objects.equals(accountSender.getAccountName(), accountReceiver.getAccountName())) {
                throw new InvalidTransactionException("Sender and Receiver cannot be the same");
            }
            amount = requestPositiveAmount("Enter amount to send: ");
            validateAmountBalance(accountSender, amount);
        } catch (AccountNotFoundException | InvalidTransactionException | InvalidAmountBalance e) {
        } catch (SQLException e) {
        }

        assert accountSender != null;
        BigDecimal newBalanceSender = accountSender.getBalance().subtract(amount);
        assert accountReceiver != null;
        BigDecimal newBalanceReceiver = accountReceiver.getBalance().add(amount);

        String accountNumberSender = accountSender.getAccountNumber();
        String accountNumberReceiver = accountReceiver.getAccountNumber();

        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                accountDAO.updateBalance(connection, accountNumberSender, newBalanceSender);
                accountDAO.updateBalance(connection, accountNumberReceiver, newBalanceReceiver);

                Transaction transactionOut = new Transaction(
                        referenceGenerator.generateReference(),
                        accountNumberSender,
                        TransactionType.TRANSFER_OUT,
                        amount,
                        newBalanceSender
                );
                transactionOut.setReferenceAccount(accountNumberReceiver);
                transactionOut.setCreatedAt(LocalDateTime.now());

                transactionDAO.save(connection, transactionOut);

                Transaction transactionIn = new Transaction(
                        referenceGenerator.generateReference(),
                        accountNumberReceiver,
                        TransactionType.TRANSFER_IN,
                        amount,
                        newBalanceReceiver
                );
                transactionIn.setReferenceAccount(accountNumberSender);
                transactionIn.setCreatedAt(LocalDateTime.now());

                transactionDAO.save(connection, transactionIn);

                connection.commit();
                System.out.println("Transfer success!");
                printReceipt(transactionOut);
                printReceipt(transactionIn);
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void transactionHistory() throws AccountNotFoundException, SQLException {
        try {
            List<Transaction> transactions = new ArrayList<>();
            Account account = requestAccountNumber();

            transactions = transactionDAO.findTransactionByAccountNum(account.getAccountNumber());

            if (transactions.isEmpty()) {
                System.out.println("There are no transactions for this account.");
            }

            printTransactionHistory(transactions);

        } catch (SQLException e) {

        }
    }

    public void validateAmountBalance(Account account, BigDecimal amount) throws InvalidAmountBalance {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InvalidAmountBalance(account.getBalance(), amount);
        }
    }

    public void printReceipt(Transaction transaction) {
        System.out.println(line);
        System.out.println("Account number: " + transaction.getAccountNumber());
        System.out.println("Transaction Reference: " + transaction.getTransactionReference());
        System.out.println("Transaction Type: " + transaction.getTransactionType());
        System.out.println("Amount: " + transaction.getAmount());
        System.out.println("Balance: " + transaction.getBalanceAfter());
        if (transaction.getReferenceAccount() != null) {
            System.out.println("Reference Account: " + transaction.getReferenceAccount());
        }
        System.out.println("Created at: " + transaction.getCreatedAt());
        System.out.println(line);
    }

    public void printTransactionHistory(List<Transaction> transactions) {
        String line = "|-------|-----------------|--------------------------------|----------------------|---------------|---------------|----------------------|----------------------|";
        System.out.println(line);
        System.out.printf("| %-5s | %-15s | %-30s | %-20s | %-13s | %-13s | %-20s | %-20s | %n",
                "Id", "Account Number", "Transaction Reference", "Transaction Type", "Amount", "Balance", "Account Reference", "Created at");
        System.out.println(line);

        int id = 1;
        for (Transaction transaction : transactions) {
            System.out.printf("| %-5s | %-15s | %-30s | %-20s | %-13s | %-13s | %-20s | %-20s | %n",
                    id++,
                    transaction.getAccountNumber(),
                    transaction.getTransactionReference(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    transaction.getBalanceAfter(),
                    transaction.getReferenceAccount(),
                    transaction.getCreatedAt()
            );
        }
        System.out.println(line);
        System.out.println("Total Transactions created: " + transactions.size());
    }

    private Account requestAccountNumber() throws AccountNotFoundException, SQLException {
        System.out.print("Enter account number: ");
        String accountNumber = input.nextLine().trim();
        return accountService.findOrThrow(accountNumber);
    }

    private Account requestAccountNumber(String prompt) throws AccountNotFoundException, SQLException {
        System.out.print(prompt);
        String accountNumber = input.nextLine().trim();
        return accountService.findOrThrow(accountNumber);
    }

    public BigDecimal requestPositiveAmount(String message) throws InvalidAmountBalance {
        System.out.print(message);
        String amountInput = input.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(amountInput);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidTransactionException("The amount cannot be Zero");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new InvalidTransactionException("Invalid Input");
        }
    }
}
