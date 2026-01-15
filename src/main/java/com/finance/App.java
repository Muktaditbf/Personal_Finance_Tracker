package com.finance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static DashboardController mainController = null;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        DatabaseHelper.getInstance().initializeDatabase();

        // DEBUG: Print categories present at startup
        try {
            java.util.List<Category> _cats = new FinanceService().getAllCategories();
            System.out.println("DEBUG: Categories count at startup = " + _cats.size());
            for (Category c : _cats) {
                System.out.println("DEBUG: Category -> id=" + c.getId() + " name=\"" + c.getName() + "\" type=" + c.getType());
            }
        } catch (Exception ex) {
            System.err.println("DEBUG: Error printing categories: " + ex.getMessage());
        }

        // Load FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/finance/MainDashboard.fxml"));
        javafx.scene.Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        // Keep a static reference to the main controller so other dialogs can refresh the UI
        DashboardController mainCtrl = fxmlLoader.getController();
        App.mainController = mainCtrl;

        // Apply stylesheet based on saved theme
        try {
            boolean dark = SettingsService.isDarkTheme();
            String sheet = dark ? "/styles/dark.css" : "/styles/styles.css";
            scene.getStylesheets().add(App.class.getResource(sheet).toExternalForm());
        } catch (Exception e) {
            System.err.println("Warning: Could not load stylesheet: " + e.getMessage());
        }

        // Set stage properties
        stage.setTitle("Finance Management Application");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        
        // Show the stage
        stage.show();
    }

    public static void refreshMainDashboard() {
        if (mainController != null) {
            mainController.refreshDashboard();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
