package com.finance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class DbInspector {
    public static void main(String[] args) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("DbInspector: Categories in database:");
            int catCount = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT id, name, budget_limit, type FROM Categories ORDER BY id")) {
                while (rs.next()) {
                    catCount++;
                    System.out.println("  id=" + rs.getInt("id") + " name=\"" + rs.getString("name") + "\" budget_limit=" + rs.getDouble("budget_limit") + " type=" + rs.getString("type"));
                }
            }
            System.out.println("DbInspector: total categories = " + catCount);

            // Try seeding accounts if empty (for debugging)
            try (ResultSet rsAccCheck = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM Accounts")) {
                if (rsAccCheck.next() && rsAccCheck.getInt("cnt") == 0) {
                    System.out.println("DbInspector: Accounts empty — attempting to insert default accounts...");
                    try {
                        stmt.executeUpdate("INSERT INTO Accounts (name, type, balance) VALUES ('Cash', 'CASH', 100.0)");
                        stmt.executeUpdate("INSERT INTO Accounts (name, type, balance) VALUES ('Checking', 'BANK', 1000.0)");
                        stmt.executeUpdate("INSERT INTO Accounts (name, type, balance) VALUES ('Credit Card', 'DIGITAL', 500.0)");
                        System.out.println("DbInspector: Default accounts inserted.");
                    } catch (SQLException insEx) {
                        System.err.println("DbInspector: Error inserting accounts: " + insEx.getMessage());
                        insEx.printStackTrace();
                    }
                } else {
                    System.out.println("DbInspector: Accounts already present (count > 0). No seed needed.");
                }
            }

            System.out.println("DbInspector: Accounts in database:");
            int accCount = 0;
            try (ResultSet rs2 = stmt.executeQuery("SELECT id, name, type, balance FROM Accounts ORDER BY id")) {
                while (rs2.next()) {
                    accCount++;
                    System.out.println("  id=" + rs2.getInt("id") + " name=\"" + rs2.getString("name") + "\" type=" + rs2.getString("type") + " balance=" + rs2.getDouble("balance"));
                }
            }
            System.out.println("DbInspector: total accounts = " + accCount);

        } catch (Exception e) {
            System.err.println("DbInspector: Error querying database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
