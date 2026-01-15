# How to Install Maven on Windows

## Quick Install Steps

1. **Download Maven:**
   - Go to: https://maven.apache.org/download.cgi
   - Download the `apache-maven-3.9.x-bin.zip` file (latest version)

2. **Extract Maven:**
   - Extract to `C:\Program Files\Apache\maven` (or any location you prefer)
   - Make sure the folder structure is: `C:\Program Files\Apache\maven\bin\mvn.cmd`

3. **Add to PATH:**
   - Press `Win + X` and select "System"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Under "System variables", find and select "Path", then click "Edit"
   - Click "New" and add: `C:\Program Files\Apache\maven\bin`
   - Click "OK" on all dialogs

4. **Verify Installation:**
   - Open a NEW PowerShell window (important - restart to pick up PATH changes)
   - Run: `mvn --version`
   - You should see Maven version information

5. **Run Your App:**
   ```powershell
   cd c:\Users\MKTD\.cursor
   mvn javafx:run
   ```

## Alternative: Use Chocolatey (if installed)

If you have Chocolatey package manager:
```powershell
choco install maven
```

## After Installation

Once Maven is installed, you can run:
```powershell
mvn javafx:run
```

This will automatically:
- Download all dependencies (JavaFX, SQLite, etc.)
- Compile your project
- Run the JavaFX application
