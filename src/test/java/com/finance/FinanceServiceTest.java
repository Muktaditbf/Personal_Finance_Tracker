package com.finance;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FinanceServiceTest {
    private static FinanceService svc;

    @BeforeAll
    public static void setup() {
        // Ensure DB initialized
        DatabaseHelper.getInstance().initializeDatabase();
        svc = new FinanceService();
    }

    @AfterAll
    public static void teardown() {
        // no-op for now
    }

    @Test
    public void testDatabaseSeeding() {
        assertFalse(svc.getAllAccounts().isEmpty(), "Accounts should be seeded");
        assertFalse(svc.getAllCategories().isEmpty(), "Categories should be seeded");
    }

    @Test
    public void testResetAndClearTransactions() {
        // Create a backup, clear transactions, reset balances
        String backup = DatabaseHelper.getInstance().backupDatabase();
        assertNotNull(backup, "Backup should be created");

        boolean cleared = svc.clearAllTransactions();
        assertTrue(cleared, "Transactions should be cleared without error");

        boolean reset = svc.resetAllAccountBalances();
        assertTrue(reset, "Account balances should be reset without error");
    }

    @Test
    public void testExportXlsx() throws Exception {
        YearMonth m = YearMonth.now();
        String path = svc.exportMonthlyReportXlsx(m);
        assertNotNull(path, "Export should return path");
        Path p = Path.of(path);
        assertTrue(Files.exists(p), "Export file should exist");
        // Basic check for file size > 0
        assertTrue(Files.size(p) > 0, "Export file should not be empty");

        // Check XLSX contains expected sheets
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(Files.newInputStream(p))) {
            assertNotNull(wb.getSheet("Summary"), "Summary sheet should exist");
            assertNotNull(wb.getSheet("Transactions"), "Transactions sheet should exist");
        }
    }
}