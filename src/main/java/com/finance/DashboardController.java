package com.finance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    
    @FXML
    private Label totalBalanceLabel;
    
    @FXML
    private PieChart expensesChart;
    
    @FXML
    private TextField amountField;
    
    @FXML
    private ComboBox<Category> categoryComboBox;
    
    @FXML
    private ComboBox<Account> accountComboBox;
    
    @FXML
    private TextArea noteField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Label statusBarLabel;
    
    @FXML
    private ComboBox<java.time.YearMonth> monthComboBox;

    @FXML
    private Button exportButton;

    @FXML
    private ToggleButton themeToggle;
    
    @FXML
    private Button settingsButton;
    
    private FinanceService financeService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        financeService = new FinanceService();
        
        // Load total balance
        updateTotalBalance();
        
        // Initialize month selector (last 12 months)
        populateMonthSelector();

        // Load PieChart data for the selected month
        loadExpensesChart(java.time.YearMonth.now());
        
        // Load ComboBoxes
        loadAccounts();
        loadCategories();
        
        // Check for recurring bill alerts
        checkRecurringAlerts();

        // Wire export button
        exportButton.setOnAction(e -> {
            applyPulseAnimation(exportButton);
            handleExport();
        });

        // Show a subtle notification after successful export (uses ControlsFX)
        // Add hover animation for save button
        saveButton.setOnAction(e -> {
            applyPressAnimation(saveButton);
            handleSave();
        });

        // Initialize theme toggle
        try {
            boolean dark = SettingsService.isDarkTheme();
            themeToggle.setSelected(dark);
        } catch (Exception ex) {
            themeToggle.setSelected(false);
            System.err.println("Warning: Could not read theme setting: " + ex.getMessage());
        }

        themeToggle.selectedProperty().addListener((obs, oldV, newV) -> {
            SettingsService.setDarkTheme(newV);
            // apply theme immediately
            if (totalBalanceLabel.getScene() != null) {
                totalBalanceLabel.getScene().getStylesheets().clear();
                String sheet = newV ? "/styles/dark.css" : "/styles/styles.css";
                totalBalanceLabel.getScene().getStylesheets().add(App.class.getResource(sheet).toExternalForm());
            }
        });

        // Add small hover for settings and open dialog on click
        settingsButton.setOnMouseEntered(e -> settingsButton.setStyle("-fx-opacity: 0.9;"));
        settingsButton.setOnMouseExited(e -> settingsButton.setStyle("-fx-opacity: 1.0;"));
        settingsButton.setOnAction(e -> showSettingsDialog());

        // Try to ensure stylesheet is applied
        try {
            if (totalBalanceLabel.getScene() != null && !totalBalanceLabel.getScene().getStylesheets().isEmpty()) {
                // already applied
            }
        } catch (Exception ignore) {}

        // Add fade-in for main content
        javafx.application.Platform.runLater(() -> {
            // no-op placeholder for future transitions
        });

    }

    private void applyPressAnimation(javafx.scene.Node n) {
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(120), n);
        st.setToX(0.98);
        st.setToY(0.98);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private void applyPulseAnimation(javafx.scene.Node n) {
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), n);
        st.setToX(1.04);
        st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }



    private void showSettingsDialog() {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("Settings");
        dialog.setHeaderText("Application Settings");
        javafx.scene.control.ButtonType resetBtn = new javafx.scene.control.ButtonType("Reset Balances", javafx.scene.control.ButtonBar.ButtonData.LEFT);
        javafx.scene.control.ButtonType resetClearBtn = new javafx.scene.control.ButtonType("Reset & Clear All Transactions", javafx.scene.control.ButtonBar.ButtonData.OTHER);
        javafx.scene.control.ButtonType closeBtn = new javafx.scene.control.ButtonType("Close", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(resetBtn, resetClearBtn, closeBtn);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt == resetBtn) {
                SettingsDialog.showResetBalancesConfirmation();
            } else if (bt == resetClearBtn) {
                SettingsDialog.showResetAndClearConfirmation();
            }
        });
    }

    /**
     * Updates the total balance label with the sum of all account balances.
     */
    private void updateTotalBalance() {
        double totalBalance = financeService.getTotalBalance();
        totalBalanceLabel.setText(String.format("Total Balance: $%.2f", totalBalance));
    }
    
    /**
     * Loads the expenses by category data into the PieChart.
     */
    private void loadExpensesChart(java.time.YearMonth month) {
        Map<String, Double> expensesByCategory = financeService.getExpensesByCategory(month);
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        // If no data, show a placeholder slice
        boolean empty = pieChartData.isEmpty();
        if (empty) {
            pieChartData.add(new PieChart.Data("No expenses", 1));
            expensesChart.setLegendVisible(false);
            expensesChart.setLabelsVisible(false);
        } else {
            expensesChart.setLegendVisible(true);
            expensesChart.setLabelsVisible(true);
        }

        expensesChart.setData(pieChartData);
        expensesChart.setStartAngle(90);

        // Palette
        String[] colors = new String[] {"#3db2a3", "#ffb86b", "#ff6b6b", "#6b8cff", "#9b59b6", "#f39c12", "#7bd389", "#e37bff"};

        // Apply colors and tooltips when nodes are available
        for (int i = 0; i < pieChartData.size(); i++) {
            final int idx = i;
            PieChart.Data d = pieChartData.get(i);
            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + colors[idx % colors.length] + ";");
                    javafx.scene.control.Tooltip t = new javafx.scene.control.Tooltip(d.getName() + ": " + String.format("$%.2f", d.getPieValue()));
                    javafx.scene.control.Tooltip.install(newNode, t);
                }
            });
        }
    }
    
    /**
     * Loads all accounts into the account ComboBox.
     */
    private void loadAccounts() {
        List<Account> accounts = financeService.getAllAccounts();
        ObservableList<Account> accountList = FXCollections.observableArrayList(accounts);
        accountComboBox.setItems(accountList);
        
        // Set cell factory to display account with an icon
        accountComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Account>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.Node icon = getAccountIconNode(item);
                    icon.setStyle("-fx-font-size: 14px; -fx-padding: 0 8 0 0;");
                    Label text = new Label(item.getName() + " (" + item.getType() + ")");
                    HBox h = new HBox(icon, text);
                    h.setSpacing(6);
                    setGraphic(h);
                }
            }
        });
        
        // Set button cell to display account with icon
        accountComboBox.setButtonCell(new javafx.scene.control.ListCell<Account>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.Node icon = getAccountIconNode(item);
                    icon.setStyle("-fx-font-size: 14px; -fx-padding: 0 8 0 0;");
                    Label text = new Label(item.getName() + " (" + item.getType() + ")");
                    HBox h = new HBox(icon, text);
                    h.setSpacing(6);
                    setGraphic(h);
                }
            }
        });
    }
    
    /**
     * Loads all categories into the category ComboBox.
     */
    private void loadCategories() {
        List<Category> categories = financeService.getAllCategories();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
        categoryComboBox.setItems(categoryList);
        
        // Set cell factory to display category with an icon
        categoryComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.Node icon = getCategoryIconNode(item);
                    icon.setStyle("-fx-font-size: 14px; -fx-padding: 0 8 0 0;");
                    Label text = new Label(item.getName() + " (" + item.getType() + ")");
                    HBox h = new HBox(icon, text);
                    h.setSpacing(6);
                    setGraphic(h);
                }
            }
        });
        
        // Set button cell to display category with icon
        categoryComboBox.setButtonCell(new javafx.scene.control.ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.Node icon = getCategoryIconNode(item);
                    icon.setStyle("-fx-font-size: 14px; -fx-padding: 0 8 0 0;");
                    Label text = new Label(item.getName() + " (" + item.getType() + ")");
                    HBox h = new HBox(icon, text);
                    h.setSpacing(6);
                    setGraphic(h);
                }
            }
        });
    }
    
    /**
     * Checks for recurring expenses due today and displays alerts.
     */
    private void checkRecurringAlerts() {
        List<String> alerts = financeService.checkRecurringDue();
        
        if (!alerts.isEmpty()) {
            StringBuilder alertMessage = new StringBuilder("Recurring Bills Due Today:\n\n");
            for (String alert : alerts) {
                alertMessage.append("• ").append(alert).append("\n");
            }
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Recurring Bills Due");
            alert.setHeaderText("You have recurring expenses due today!");
            alert.setContentText(alertMessage.toString());
            alert.showAndWait();
        }
    }

    // Helper to map a category to an ikon FontIcon node
    private javafx.scene.Node getCategoryIconNode(Category category) {
        org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon();
        if (category == null || category.getName() == null) {
            icon.setIconLiteral("fas-bullseye");
            return icon;
        }
        String name = category.getName().toLowerCase();
        if (name.contains("groc")) icon.setIconLiteral("fas-shopping-cart");
        else if (name.contains("util")) icon.setIconLiteral("fas-lightbulb");
        else if (name.contains("trans") || name.contains("taxi") || name.contains("uber") || name.contains("bus")) icon.setIconLiteral("fas-car");
        else if (name.contains("salary") || name.contains("income")) icon.setIconLiteral("fas-briefcase");
        else icon.setIconLiteral("fas-bullseye");
        return icon;
    }

    // Helper to map an account to an icon node
    private javafx.scene.Node getAccountIconNode(Account account) {
        org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon();
        icon.getStyleClass().add("icon");
        if (account == null || account.getType() == null) {
            icon.setIconLiteral("fas-credit-card");
            return icon;
        }
        switch (account.getType()) {
            case CASH:
                icon.setIconLiteral("fas-money-bill-wave");
                break;
            case BANK:
                icon.setIconLiteral("fas-university");
                break;
            case DIGITAL:
            default:
                icon.setIconLiteral("fas-credit-card");
                break;
        }
        return icon;
    }
    
    /**
     * Public method called to refresh the dashboard UI (balances, charts, lists).
     */
    public void refreshDashboard() {
        javafx.application.Platform.runLater(() -> {
            updateTotalBalance();
            java.time.YearMonth sel = monthComboBox.getValue() != null ? monthComboBox.getValue() : java.time.YearMonth.now();
            loadExpensesChart(sel);
            loadAccounts();
            loadCategories();
            statusBarLabel.setText("Status: Dashboard refreshed.");
        });
    }

    private void populateMonthSelector() {
        java.util.List<java.time.YearMonth> months = new java.util.ArrayList<>();
        java.time.YearMonth now = java.time.YearMonth.now();
        for (int i = 0; i < 12; i++) {
            months.add(now.minusMonths(i));
        }
        monthComboBox.setItems(javafx.collections.FXCollections.observableArrayList(months));
        monthComboBox.setConverter(new javafx.util.StringConverter<java.time.YearMonth>() {
            @Override
            public String toString(java.time.YearMonth object) {
                if (object == null) return "";
                java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
                return object.format(f);
            }

            @Override
            public java.time.YearMonth fromString(String string) {
                return null;
            }
        });
        monthComboBox.getSelectionModel().selectFirst();
        monthComboBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadExpensesChart(newV);
        });
    }

    /**
     * Handles the Save button click event.
     * Validates input, checks budget warning, and saves the transaction.
     */
    @FXML
    private void handleSave() {
        // Validate input
        if (amountField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Please enter an amount.");
            return;
        }
        
        if (categoryComboBox.getSelectionModel().getSelectedItem() == null) {
            showErrorAlert("Validation Error", "Please select a category.");
            return;
        }
        
        if (accountComboBox.getSelectionModel().getSelectedItem() == null) {
            showErrorAlert("Validation Error", "Please select an account.");
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showErrorAlert("Validation Error", "Amount must be greater than zero.");
                return;
            }
            
            Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
            Account selectedAccount = accountComboBox.getSelectionModel().getSelectedItem();
            String note = noteField.getText().trim();
            
            // Check budget warning before saving
            String budgetWarning = financeService.checkBudgetWarning(selectedCategory.getId(), amount);
            
            if (budgetWarning != null) {
                // Show warning dialog
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle("Budget Warning");
                warningAlert.setHeaderText("Budget Limit Exceeded");
                warningAlert.setContentText(budgetWarning + "\n\nDo you still want to proceed?");
                
                // Add buttons
                javafx.scene.control.ButtonType proceedButton = new javafx.scene.control.ButtonType("Proceed", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                warningAlert.getButtonTypes().setAll(proceedButton, cancelButton);
                
                // Show dialog and wait for user response
                warningAlert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == proceedButton) {
                        // User chose to proceed, save the transaction
                        saveTransaction(selectedAccount.getId(), selectedCategory.getId(), amount, note);
                    }
                    // If cancel, do nothing
                });
            } else {
                // No warning, proceed with saving
                saveTransaction(selectedAccount.getId(), selectedCategory.getId(), amount, note);
            }
            
        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Please enter a valid number for the amount.");
        }
    }
    
    /**
     * Saves the transaction and refreshes the UI.
     */
    private void saveTransaction(int accountId, int categoryId, double amount, String note) {
        boolean success = financeService.addTransaction(
            accountId,
            categoryId,
            amount,
            LocalDate.now(),
            note.isEmpty() ? null : note,
            null // image_path not implemented yet
        );
        
        if (success) {
            // Clear form
            amountField.clear();
            categoryComboBox.getSelectionModel().clearSelection();
            accountComboBox.getSelectionModel().clearSelection();
            noteField.clear();
            
            // Refresh UI
            updateTotalBalance();
            java.time.YearMonth sel = monthComboBox.getValue() != null ? monthComboBox.getValue() : java.time.YearMonth.now();
            loadExpensesChart(sel);
            
            // Update status bar
            statusBarLabel.setText("Status: Transaction saved successfully!");
            
            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Transaction Saved");
            successAlert.setContentText("The transaction has been saved successfully.");
            successAlert.showAndWait();
        } else {
            showErrorAlert("Error", "Failed to save transaction. Please try again.");
            statusBarLabel.setText("Status: Error saving transaction!");
        }
    }
    
    /**
     * Handles export of the selected month to CSV.
     */
    private void handleExport() {
        java.time.YearMonth sel = monthComboBox.getValue() != null ? monthComboBox.getValue() : java.time.YearMonth.now();
        // Prefer XLSX export per user request
        String out = financeService.exportMonthlyReportXlsx(sel);
        if (out != null) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Export Complete");
            info.setHeaderText(null);
            info.setContentText("Exported report to: " + out);
            info.showAndWait();

            // Try opening the folder
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(out).getParentFile());
            } catch (Exception ex) {
                // ignore
            }
        } else {
            showErrorAlert("Export Failed", "Failed to export report. Check logs.");
        }
    }

    /**
     * Shows an error alert dialog.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
