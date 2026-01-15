package com.finance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SettingsService {
    private static final Path SETTINGS_PATH = Paths.get(System.getProperty("user.home"), ".finance_app.properties");
    private static final String THEME_KEY = "theme.dark";

    public static boolean isDarkTheme() {
        Properties props = loadProps();
        return Boolean.parseBoolean(props.getProperty(THEME_KEY, "false"));
    }

    public static void setDarkTheme(boolean dark) {
        Properties props = loadProps();
        props.setProperty(THEME_KEY, Boolean.toString(dark));
        saveProps(props);
    }

    private static Properties loadProps() {
        Properties props = new Properties();
        if (Files.exists(SETTINGS_PATH)) {
            try (InputStream in = Files.newInputStream(SETTINGS_PATH)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("SettingsService: Error loading settings: " + e.getMessage());
            }
        }
        return props;
    }

    private static void saveProps(Properties props) {
        try {
            if (!Files.exists(SETTINGS_PATH.getParent())) {
                Files.createDirectories(SETTINGS_PATH.getParent());
            }
            try (OutputStream out = Files.newOutputStream(SETTINGS_PATH)) {
                props.store(out, "Finance App Settings");
            }
        } catch (IOException e) {
            System.err.println("SettingsService: Error saving settings: " + e.getMessage());
        }
    }
}
