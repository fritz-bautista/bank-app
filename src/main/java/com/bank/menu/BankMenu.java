package com.bank.menu;

import com.bank.dao.AccountDAOImpl;
import com.bank.dao.TransactionDAOImpl;
import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;
import com.bank.util.ReferenceGenerator;

import java.sql.SQLException;
import java.util.Scanner;

public class BankMenu {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final Scanner input;

    public BankMenu() {
        input = new Scanner(System.in);

        AccountDAO accountDAO = new AccountDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();
        ReferenceGenerator referenceGenerator = new ReferenceGenerator();
        TransactionType transactionType;
        accountService = new AccountService(accountDAO, input);
        transactionService = new TransactionService(referenceGenerator, accountDAO, transactionDAO, accountService,
                input);
    }

    public void start() throws SQLException {
        typeOfTransaction();
    }

    public void typeOfTransaction() throws SQLException {
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Choose action: ");
            String transaction = input.nextLine();

            switch (transaction) {
                case "1" -> accountService.createAccount();
                case "2" -> accountService.balanceInquiry();
                case "3" -> accountService.listAllAccount();
                case "4" -> transactionService.deposit();
                case "5" -> transactionService.withdraw();
                case "6" -> transactionService.transfer();
                case "7" -> transactionService.transactionHistory();
                case "8" -> {
                    running = false;
                    System.out.println("Thank you for using the Banking Application System");
                }
                default -> System.out.println("Invalid choice " + transaction + ". Please choose between 1-8 only.");
            }
        }
        input.close();

    }

    public void printMenu() {
        System.out.println("===========================");
        System.out.println("Welcome to Bank Application");
        System.out.println("===========================");
        System.out.println("1. Create Account");
        System.out.println("2. Balance Inquiry");
        System.out.println("3. List Accounts");
        System.out.println("4. Deposit");
        System.out.println("5. Withdraw");
        System.out.println("6. Transfer");
        System.out.println("7. Transaction History");
        System.out.println("===========================");
    }
}
