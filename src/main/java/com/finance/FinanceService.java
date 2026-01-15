package com.finance;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceService {
    private DatabaseHelper dbHelper;

    public FinanceService() {
        this.dbHelper = DatabaseHelper.getInstance();
    }

    /**
     * Checks if adding a new transaction amount would exceed the category's budget limit.
     * Calculates total spent in the category for the current month.
     * 
     * @param categoryId The ID of the category to check
     * @param newAmount The amount of the new transaction to be added
     * @return Warning message if budget would be exceeded, null otherwise
     */
    public String checkBudgetWarning(int categoryId, double newAmount) {
        String warning = null;
        
        try (Connection conn = dbHelper.getConnection()) {
            // Get category budget limit and type
            String categoryQuery = "SELECT budget_limit, type FROM Categories WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(categoryQuery)) {
                pstmt.setInt(1, categoryId);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    return "Category not found.";
                }
                
                double budgetLimit = rs.getDouble("budget_limit");
                String categoryType = rs.getString("type");
                
                // Only check budget for EXPENSE categories
                if (!"EXPENSE".equals(categoryType)) {
                    return null; // No budget warning for income categories
                }
                
                // Calculate total spent this month
                YearMonth currentMonth = YearMonth.now();
                LocalDate startOfMonth = currentMonth.atDay(1);
                LocalDate endOfMonth = currentMonth.atEndOfMonth();
                
                String totalSpentQuery = """
                    SELECT COALESCE(SUM(amount), 0) as total_spent
                    FROM Transactions
                    WHERE category_id = ? 
                    AND date >= ? 
                    AND date <= ?
                    """;
                
                try (PreparedStatement totalPstmt = conn.prepareStatement(totalSpentQuery)) {
                    totalPstmt.setInt(1, categoryId);
                    totalPstmt.setString(2, startOfMonth.toString());
                    totalPstmt.setString(3, endOfMonth.toString());
                    
                    ResultSet totalRs = totalPstmt.executeQuery();
                    if (totalRs.next()) {
                        double totalSpent = totalRs.getDouble("total_spent");
                        double projectedTotal = totalSpent + newAmount;
                        
                        if (projectedTotal > budgetLimit) {
                            double overBudget = projectedTotal - budgetLimit;
                            warning = String.format(
                                "Budget Warning: Adding this transaction (%.2f) would exceed the budget limit (%.2f) by %.2f. " +
                                "Current month spending: %.2f, Projected total: %.2f",
                                newAmount, budgetLimit, overBudget, totalSpent, projectedTotal
                            );
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking budget warning: " + e.getMessage());
            e.printStackTrace();
            return "Error checking budget: " + e.getMessage();
        }
        
        return warning;
    }

    /**
     * Checks for recurring expenses that are due today based on the due_day field.
     * 
     * @return List of alert messages for recurring expenses due today
     */
    public List<String> checkRecurringDue() {
        List<String> alerts = new ArrayList<>();
        
        try (Connection conn = dbHelper.getConnection()) {
            int todayDay = LocalDate.now().getDayOfMonth();
            
            String query = "SELECT id, name, amount, due_day FROM RecurringExpenses WHERE due_day = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, todayDay);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    String name = rs.getString("name");
                    double amount = rs.getDouble("amount");
                    int dueDay = rs.getInt("due_day");
                    
                    String alert = String.format(
                        "Recurring Expense Due Today (Day %d): %s - Amount: %.2f",
                        dueDay, name, amount
                    );
                    alerts.add(alert);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking recurring expenses: " + e.getMessage());
            e.printStackTrace();
            alerts.add("Error checking recurring expenses: " + e.getMessage());
        }
        
        return alerts;
    }

    /**
     * Adds a new transaction and updates the corresponding account balance.
     * For EXPENSE categories, deducts the amount from account balance.
     * For INCOME categories, adds the amount to account balance.
     * 
     * @param accountId The ID of the account
     * @param categoryId The ID of the category
     * @param amount The transaction amount (positive value)
     * @param date The transaction date
     * @param note Optional note for the transaction
     * @param imagePath Optional path to transaction image
     * @return true if transaction was added successfully, false otherwise
     */
    public boolean addTransaction(int accountId, int categoryId, double amount, 
                                   LocalDate date, String note, String imagePath) {
        Connection conn = null;
        try {
            conn = dbHelper.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Get category type to determine if it's income or expense
            String categoryQuery = "SELECT type FROM Categories WHERE id = ?";
            String categoryType;
            try (PreparedStatement pstmt = conn.prepareStatement(categoryQuery)) {
                pstmt.setInt(1, categoryId);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    System.err.println("Category not found: " + categoryId);
                    conn.rollback();
                    return false;
                }
                
                categoryType = rs.getString("type");
            }
            
            // Insert transaction
            String insertTransactionQuery = """
                INSERT INTO Transactions (account_id, category_id, amount, date, note, image_path)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertTransactionQuery)) {
                pstmt.setInt(1, accountId);
                pstmt.setInt(2, categoryId);
                pstmt.setDouble(3, amount);
                pstmt.setString(4, date.toString());
                pstmt.setString(5, note);
                pstmt.setString(6, imagePath);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            // Update account balance based on category type
            String updateBalanceQuery;
            if ("INCOME".equals(categoryType)) {
                // Add amount for income
                updateBalanceQuery = "UPDATE Accounts SET balance = balance + ? WHERE id = ?";
            } else {
                // Deduct amount for expense
                updateBalanceQuery = "UPDATE Accounts SET balance = balance - ? WHERE id = ?";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(updateBalanceQuery)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, accountId);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    System.err.println("Account not found: " + accountId);
                    conn.rollback();
                    return false;
                }
            }
            
            // Commit transaction
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Resets all account balances to zero. Returns true on success.
     */
    public boolean resetAllAccountBalances() {
        try (Connection conn = dbHelper.getConnection()) {
            String update = "UPDATE Accounts SET balance = 0";
            try (PreparedStatement pstmt = conn.prepareStatement(update)) {
                pstmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error resetting account balances: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes all transactions from the Transactions table.
     */
    public boolean clearAllTransactions() {
        try (Connection conn = dbHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Transactions")) {
                pstmt.executeUpdate();
            }
            // Optionally reset autoincrement sequence
            try (PreparedStatement seq = conn.prepareStatement("DELETE FROM sqlite_sequence WHERE name='Transactions'")) {
                seq.executeUpdate();
            } catch (SQLException ignore) {
                // Not critical if sqlite_sequence does not exist
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error clearing transactions: " + e.getMessage());
            e.printStackTrace();
            try (Connection conn = dbHelper.getConnection()) {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back after clearing transactions: " + ex.getMessage());
            }
            return false;
        } finally {
            try (Connection conn = dbHelper.getConnection()) {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Exports transactions and summary for the given month to a CSV file. Returns the path to the file on success.
     */
    public String exportMonthlyReport(YearMonth month) {
        // Backward-compatible CSV export kept for reference; prefer XLSX via exportMonthlyReportXlsx
        return exportMonthlyReportXlsx(month);
    }

    /**
     * Exports transactions and summary for the given month to an XLSX spreadsheet. Returns the path to the file on success.
     */
    public String exportMonthlyReportXlsx(YearMonth month) {
        java.nio.file.Path exportDir = java.nio.file.Paths.get("exports");
        try {
            if (!java.nio.file.Files.exists(exportDir)) {
                java.nio.file.Files.createDirectories(exportDir);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error creating exports directory: " + e.getMessage());
            return null;
        }

        String filename = String.format("finance-report-%s.xlsx", month.toString());
        java.nio.file.Path out = exportDir.resolve(filename);

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        String txQuery = """
            SELECT t.date, t.amount, t.note, a.name AS account_name, c.name AS category_name
            FROM Transactions t
            LEFT JOIN Accounts a ON t.account_id = a.id
            LEFT JOIN Categories c ON t.category_id = c.id
            WHERE (t.date >= ? AND t.date <= ?)
            ORDER BY t.date ASC
            """;

        String summaryQuery = """
            SELECT c.name AS category_name, COALESCE(SUM(t.amount), 0) AS total
            FROM Categories c
            LEFT JOIN Transactions t ON c.id = t.category_id
            WHERE c.type = 'EXPENSE' AND (t.date >= ? AND t.date <= ?)
            GROUP BY c.id, c.name
            HAVING total > 0
            ORDER BY total DESC
            """;

        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmtTx = conn.prepareStatement(txQuery);
             PreparedStatement pstmtSummary = conn.prepareStatement(summaryQuery)) {

            pstmtTx.setString(1, start.toString());
            pstmtTx.setString(2, end.toString());

            pstmtSummary.setString(1, start.toString());
            pstmtSummary.setString(2, end.toString());

            try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
                org.apache.poi.ss.usermodel.Font bold = wb.createFont();
                bold.setBold(true);
                headerStyle.setFont(bold);

                // Summary sheet
                org.apache.poi.ss.usermodel.Sheet sum = wb.createSheet("Summary");
                int row = 0;
                org.apache.poi.ss.usermodel.Row r0 = sum.createRow(row++);
                r0.createCell(0).setCellValue("Finance Report");
                r0.createCell(1).setCellValue(month.toString());

                row++;
                org.apache.poi.ss.usermodel.Row hdr = sum.createRow(row++);
                org.apache.poi.ss.usermodel.Cell c0 = hdr.createCell(0);
                c0.setCellValue("Category");
                c0.setCellStyle(headerStyle);
                org.apache.poi.ss.usermodel.Cell c1 = hdr.createCell(1);
                c1.setCellValue("Total");
                c1.setCellStyle(headerStyle);

                try (java.sql.ResultSet rs = pstmtSummary.executeQuery()) {
                    while (rs.next()) {
                        String cat = rs.getString("category_name");
                        double tot = rs.getDouble("total");
                        org.apache.poi.ss.usermodel.Row rr = sum.createRow(row++);
                        rr.createCell(0).setCellValue(cat);
                        rr.createCell(1).setCellValue(tot);
                    }
                }

                // Autosize
                sum.autoSizeColumn(0);
                sum.autoSizeColumn(1);

                // Transactions sheet
                org.apache.poi.ss.usermodel.Sheet tx = wb.createSheet("Transactions");
                int trow = 0;
                org.apache.poi.ss.usermodel.Row th = tx.createRow(trow++);
                String[] ths = new String[] {"Date", "Account", "Category", "Amount", "Note"};
                for (int i = 0; i < ths.length; i++) {
                    org.apache.poi.ss.usermodel.Cell ch = th.createCell(i);
                    ch.setCellValue(ths[i]);
                    ch.setCellStyle(headerStyle);
                }

                try (java.sql.ResultSet rs = pstmtTx.executeQuery()) {
                    while (rs.next()) {
                        String date = rs.getString("date");
                        String acct = rs.getString("account_name");
                        String cat = rs.getString("category_name");
                        double amt = rs.getDouble("amount");
                        String note = rs.getString("note");
                        org.apache.poi.ss.usermodel.Row tr = tx.createRow(trow++);
                        tr.createCell(0).setCellValue(date);
                        tr.createCell(1).setCellValue(acct);
                        tr.createCell(2).setCellValue(cat);
                        tr.createCell(3).setCellValue(amt);
                        tr.createCell(4).setCellValue(note != null ? note : "");
                    }
                }

                for (int i = 0; i < ths.length; i++) tx.autoSizeColumn(i);

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(out.toFile())) {
                    wb.write(fos);
                }
            }

            System.out.println("Exported XLSX report: " + out.toString());
            return out.toString();

        } catch (SQLException | java.io.IOException e) {
            System.err.println("Error exporting monthly report XLSX: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String escapeCsv(String in) {
        if (in == null) return "";
        String s = in.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    /**
     * Gets the total balance across all accounts.
     * 
     * @return Total balance from all accounts
     */
    public double getTotalBalance() {
        double totalBalance = 0.0;
        
        try (Connection conn = dbHelper.getConnection()) {
            String query = "SELECT COALESCE(SUM(balance), 0) as total FROM Accounts";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    totalBalance = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total balance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return totalBalance;
    }

    /**
     * Gets expenses by category for the current month.
     * Returns a map where key is category name and value is total amount spent.
     * 
     * @return Map of category names to expense amounts
     */
    public Map<String, Double> getExpensesByCategory() {
        return getExpensesByCategory(YearMonth.now());
    }

    /**
     * Gets expenses by category for a specific month.
     */
    public Map<String, Double> getExpensesByCategory(YearMonth month) {
        Map<String, Double> expensesByCategory = new HashMap<>();
        
        try (Connection conn = dbHelper.getConnection()) {
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            
            String query = """
                SELECT c.name, COALESCE(SUM(t.amount), 0) as total
                FROM Categories c
                LEFT JOIN Transactions t ON c.id = t.category_id
                WHERE c.type = 'EXPENSE'
                AND (t.date IS NULL OR (t.date >= ? AND t.date <= ?))
                GROUP BY c.id, c.name
                HAVING total > 0
                """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, startOfMonth.toString());
                pstmt.setString(2, endOfMonth.toString());
                
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String categoryName = rs.getString("name");
                    double total = rs.getDouble("total");
                    expensesByCategory.put(categoryName, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting expenses by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expensesByCategory;
    }

    /**
     * Gets all accounts from the database.
     * 
     * @return List of Account objects
     */
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        
        try (Connection conn = dbHelper.getConnection()) {
            String query = "SELECT id, name, type, balance FROM Accounts ORDER BY name";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Account account = new Account();
                    account.setId(rs.getInt("id"));
                    account.setName(rs.getString("name"));
                    account.setType(AccountType.valueOf(rs.getString("type")));
                    account.setBalance(rs.getDouble("balance"));
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting accounts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return accounts;
    }

    /**
     * Gets all categories from the database.
     * 
     * @return List of Category objects
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = dbHelper.getConnection()) {
            String query = "SELECT id, name, budget_limit, type FROM Categories ORDER BY name";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getInt("id"));
                    category.setName(rs.getString("name"));
                    category.setBudgetLimit(rs.getDouble("budget_limit"));
                    category.setType(CategoryType.valueOf(rs.getString("type")));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
}
