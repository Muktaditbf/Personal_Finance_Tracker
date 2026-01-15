@echo off
setlocal

echo Running Finance Application...
echo.

REM Attempt to launch the app using Maven; keep messages minimal per request.
mvn clean javafx:run %*
if ERRORLEVEL 1 (
    echo ERROR: Failed to launch the application. Ensure 'mvn' is on your PATH and try again.
    pause
    endlocal
    exit /b 1
)

pause
endlocal
exit /b 0
