package com.finance;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class SettingsDialog {
    public static void showResetBalancesConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Balances");
        alert.setHeaderText("Reset all account balances to zero");
        alert.setContentText("This will set all account balances to 0. This action cannot be undone. It will NOT delete transactions.\n\nMake a backup of finance.db before proceeding.\n\nDo you want to proceed?");

        ButtonType proceed = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(proceed, cancel);

        alert.showAndWait().ifPresent(bt -> {
            if (bt == proceed) {
                boolean ok = new FinanceService().resetAllAccountBalances();
                if (ok) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Done");
                    info.setHeaderText(null);
                    info.setContentText("All account balances set to 0.");
                    info.showAndWait();
                    // Refresh main dashboard UI after successful reset
                    App.refreshMainDashboard();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Error");
                    err.setHeaderText(null);
                    err.setContentText("Failed to reset balances. Check logs.");
                    err.showAndWait();
                }
            }
        });
    }

    public static void showResetAndClearConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset & Clear Transactions");
        alert.setHeaderText("Reset balances AND delete all transactions");
        alert.setContentText("This will set all account balances to 0 AND delete ALL transactions (this cannot be undone).\n\nA backup of the database will be created before proceeding.\n\nDo you want to proceed?");

        ButtonType proceed = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(proceed, cancel);

        alert.showAndWait().ifPresent(bt -> {
            if (bt == proceed) {
                // Create backup
                String backupPath = DatabaseHelper.getInstance().backupDatabase();
                if (backupPath == null) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Backup Failed");
                    err.setHeaderText(null);
                    err.setContentText("Failed to create database backup. Aborting operation.");
                    err.showAndWait();
                    return;
                }

                FinanceService svc = new FinanceService();
                boolean cleared = svc.clearAllTransactions();
                boolean ok = svc.resetAllAccountBalances();
                if (cleared && ok) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Done");
                    info.setHeaderText(null);
                    info.setContentText("All transactions deleted and balances set to 0. Backup: " + backupPath);
                    info.showAndWait();
                    App.refreshMainDashboard();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Error");
                    err.setHeaderText(null);
                    err.setContentText("Failed to complete the operation. Check logs.");
                    err.showAndWait();
                }
            }
        });
    }
}
