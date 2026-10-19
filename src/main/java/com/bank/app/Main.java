package com.bank.app;
import com.bank.menu.BankMenu;

public class Main {
    public static void main(String[] args) {
        try {
            BankMenu bankingMenu = new BankMenu();
            bankingMenu.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


