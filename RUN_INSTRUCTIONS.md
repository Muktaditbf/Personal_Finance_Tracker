# How to Run the Finance Application

## Problem
The `NoClassDefFoundError: Stage` error occurs because JavaFX modules are not on the module path. JavaFX is not included in the JDK, so it must be explicitly added.

## Solution 1: Use Maven (Recommended - Easiest)

Maven automatically handles all JavaFX module path configuration. Simply run:

```powershell
# Navigate to project directory
cd c:\Users\MKTD\.cursor

# Run the application
mvn javafx:run
```

If Maven is not installed or not in PATH:
1. Install Maven from https://maven.apache.org/download.cgi
2. Add Maven to your system PATH
3. Or use the full path to Maven executable

## Solution 2: Use VS Code Launch Configuration

1. **First, compile the project:**
   - Press `Ctrl+Shift+P`
   - Type "Tasks: Run Task"
   - Select "maven-compile"
   - Or run in terminal: `mvn clean compile`

2. **Then run the application:**
   - Press `F5` or go to Run and Debug
   - Select "Launch Finance App (Recommended - Use Maven)"
   - Click the play button

## Solution 3: Manual JavaFX Module Path Setup

If you want to run directly with Java, you need to:

1. **Download JavaFX SDK** (if not using Maven dependencies):
   - Download from https://openjfx.io/
   - Extract to a folder (e.g., `C:\javafx-sdk-21.0.2`)

2. **Run with proper module path:**
```powershell
java --module-path "C:\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "target/classes;target/dependency/*" com.finance.App
```

## Quick Fix: Install Maven

If Maven is not installed:

1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to a folder (e.g., `C:\Program Files\Apache\maven`)
3. Add to PATH:
   - Open System Properties → Environment Variables
   - Add `C:\Program Files\Apache\maven\bin` to PATH
4. Restart VS Code
5. Run: `mvn javafx:run`

## Verify Maven is Working

Test if Maven is installed:
```powershell
mvn --version
```

If this works, you can run the app with:
```powershell
mvn javafx:run
```

## Troubleshooting

- **"Maven not found"**: Install Maven and add to PATH
- **"JavaFX modules not found"**: Run `mvn clean install` first to download dependencies
- **"Database error"**: Ensure write permissions in project directory
- **"FXML not found"**: Ensure `MainDashboard.fxml` is in `src/main/resources/com/finance/`
