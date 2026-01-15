package com.finance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:finance.db";
    private static DatabaseHelper instance;
    private Connection connection;

    // Private constructor for singleton pattern
    private DatabaseHelper() {
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Singleton instance getter
    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // Get database connection
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("Error getting database connection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    // Initialize database and create tables if they don't exist
    public void initializeDatabase() {
        String createAccountsTable = """
            CREATE TABLE IF NOT EXISTS Accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('CASH', 'DIGITAL', 'BANK')),
                balance REAL NOT NULL DEFAULT 0.0
            )
            """;

        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS Categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                budget_limit REAL NOT NULL DEFAULT 0.0,
                type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))
            )
            """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS Transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id INTEGER NOT NULL,
                category_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                note TEXT,
                image_path TEXT,
                FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES Categories(id) ON DELETE CASCADE
            )
            """;

        String createRecurringExpensesTable = """
            CREATE TABLE IF NOT EXISTS RecurringExpenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                amount REAL NOT NULL,
                due_day INTEGER NOT NULL CHECK(due_day >= 1 AND due_day <= 31)
            )
            """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createAccountsTable);
            stmt.execute(createCategoriesTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createRecurringExpensesTable);

            // Seed default accounts if table is empty
            try (java.sql.ResultSet rsAcc = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM Accounts")) {
                if (rsAcc.next() && rsAcc.getInt("cnt") == 0) {
                    stmt.executeUpdate("INSERT INTO Accounts (name, type, balance) VALUES "
                            + "('Cash', 'CASH', 100.0),"
                            + "('Checking', 'BANK', 1000.0),"
                            + "('Credit Card', 'DIGITAL', 500.0)");
                    System.out.println("Database seeded with default accounts.");
                }
            } catch (SQLException sea) {
                System.err.println("Error checking/seeding accounts: " + sea.getMessage());
                sea.printStackTrace();
            }

            // Seed default categories if table is empty
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM Categories")) {
                if (rs.next() && rs.getInt("cnt") == 0) {
                    stmt.executeUpdate("INSERT INTO Categories (name, budget_limit, type) VALUES "
                            + "('Groceries', 500.0, 'EXPENSE'),"
                            + "('Utilities', 200.0, 'EXPENSE'),"
                            + "('Transport', 150.0, 'EXPENSE'),"
                            + "('Salary', 0.0, 'INCOME')");
                    System.out.println("Database seeded with default categories.");
                }
            } catch (SQLException se) {
                System.err.println("Error checking/seeding categories: " + se.getMessage());
                se.printStackTrace();
            }

            System.out.println("Database initialized successfully. All tables created.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Close database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a timestamped backup copy of the database file (finance.db).
     * Returns the path to the backup file on success, or null on failure.
     */
    public String backupDatabase() {
        try {
            java.nio.file.Path src = java.nio.file.Paths.get("finance.db");
            if (!java.nio.file.Files.exists(src)) {
                System.err.println("Database file not found for backup: finance.db");
                return null;
            }

            java.nio.file.Path backupDir = java.nio.file.Paths.get("db_backups");
            if (!java.nio.file.Files.exists(backupDir)) {
                java.nio.file.Files.createDirectories(backupDir);
            }

            String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            java.nio.file.Path dest = backupDir.resolve("finance-db-backup-" + ts + ".db");
            java.nio.file.Files.copy(src, dest);
            System.out.println("Database backup created: " + dest.toString());
            return dest.toString();
        } catch (java.io.IOException e) {
            System.err.println("Error creating database backup: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
