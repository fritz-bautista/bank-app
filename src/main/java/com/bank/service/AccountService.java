package com.bank.service;

import com.bank.exception.InvalidAccountNameDuplicate;
import com.bank.models.Account;
import com.bank.dao.AccountDAO;
import com.bank.exception.AccountNotFoundException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class AccountService {
    private final AccountDAO accountDao;
    private final Scanner input;

    private static final String CREATE_ACCOUNT = "|---------- Create Account ----------|";
    private static final String BALANCE_INQUIRY = "|---------- Balance Inquiry ----------|";
    private static final String LIST_ALL = "|---------- List All ----------|";
    private static final String ACCOUNT_PROMPT = "Enter account holder name: ";

    public AccountService(AccountDAO accountDao, Scanner input) {
        this.accountDao = accountDao;
        this.input = input;
    }

    //    This is the business logic for creating the account, it will call the AccountDAO then the AccountDAOImpl for the
    //    backend or SQL query
    public void createAccount() throws SQLException {
        System.out.println(CREATE_ACCOUNT);

        System.out.print(ACCOUNT_PROMPT);
        String accountName = input.nextLine().trim();

        System.out.print("Enter starting balance of the account: ");
        BigDecimal startingBalance = new BigDecimal(input.nextLine());

        if (accountName.isEmpty()) {
            System.out.println("Account name cannot be empty.");
            return;
        }

        if (startingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Account starting balance cannot be empty.");
            return;
        }

        String accountNumber = generateAccountNumber();

        try {
            checkIfNameHasDuplicate(accountName);
            Account account = new Account(accountNumber, accountName, startingBalance);
            accountDao.createAccount(account);
            System.out.println("Account Created Successfully!");

            printAccountById(account);
        } catch (SQLException e) {
            throw e;
        }
    }

    //    This is the business logic for creating the account, it will call the AccountDAO then the AccountDAOImpl for the
    //    backend or SQL query
    public void checkIfNameHasDuplicate(String accountName) throws InvalidAccountNameDuplicate, SQLException {
        try {
            boolean duplicateName = accountDao.checkNames(accountName);
            if (duplicateName) {
                throw new InvalidAccountNameDuplicate("Entered name already exists.");
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void listAllAccount() throws AccountNotFoundException, SQLException {
        System.out.println(LIST_ALL);
        try {
            List<Account> accounts = accountDao.listAccounts();

            if (accounts.isEmpty()) {
                System.out.println("No accounts found");
                return;
            }
            printAccounts(accounts);
        } catch (SQLException e) {
            System.out.println("Failed to retrieve the accounts: " + e.getMessage());
        }

    }

    public void balanceInquiry() {
        System.out.println(BALANCE_INQUIRY);
        System.out.print(ACCOUNT_PROMPT);
        String accountNumber = input.nextLine();

        try {
            Account account = findOrThrow(accountNumber);
            System.out.println("[SUCCESS] Balance Inquiry");
            printAccountById(account);
        } catch (AccountNotFoundException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[ERROR] Database error: " + e.getMessage());
        }
    }

    public Account findOrThrow(String accountNumber) throws AccountNotFoundException, SQLException {
        Optional<Account> opt = accountDao.findById(accountNumber);
        return opt.orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public String generateAccountNumber() {
        long seed = System.currentTimeMillis() % 1_000_000_000L;
        return String.format("ACC-%010d", seed);
    }

    public void printAccountById(Account account) {
        System.out.println("Account number: " + account.getAccountNumber());
        System.out.println("Account name: " + account.getAccountName());
        System.out.println("Account balance: " + account.getBalance());
        if (account.getCreatedAt() != null) {
            System.out.println("Account created: " + account.getCreatedAt());
        }
    }

    public void printAccounts(List<Account> accounts) {
        String line = "|-------|----------------------|-------------------------|---------------|----------------------|";
        System.out.println(line);
        System.out.printf("| %-5s | %-20s | %-23s | %-13s | %-20s | %n",
                "Id", "Account Number", "Account Name", "Balance", "Created at");
        System.out.println(line);

        int id = 1;
        for (Account account : accounts) {
            System.out.printf("| %-5s | %-20s | %-23s | %-13s | %-20s | %n",
                    id++,
                    account.getAccountNumber(),
                    account.getAccountName(),
                    account.getBalance(),
                    account.getCreatedAt());
        }
        System.out.println(line);
        System.out.println("Total accounts created: " + accounts.size());
    }
}
