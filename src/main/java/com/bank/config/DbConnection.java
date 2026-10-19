package com.bank.config;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbConnection {
    private static final String PROP_URL = "db.url";
    private static final String PROP_USERNAME = "db.username";
    private static final String PROP_PASSWORD = "db.password";

    private DbConnection() {
    }

    public static Connection getConnection() throws SQLException {
        PropertyLoader config = PropertyLoader.getInstance();
        String url = config.getProperty(PROP_URL);
        String username = config.getProperty(PROP_USERNAME);
        String password = config.getProperty(PROP_PASSWORD);

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            throw e;
        }
    }
}
