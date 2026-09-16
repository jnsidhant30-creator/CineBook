@echo off
echo ==========================================
echo   MySQL Root Password Reset + DB Setup
echo ==========================================
echo.

:: Check for admin rights
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: This script must be run as Administrator!
    echo Right-click this file and select "Run as administrator"
    pause
    exit /b 1
)

set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin
set MYSQL_INI=C:\ProgramData\MySQL\MySQL Server 8.4\my.ini
set PROJECT_DIR=%~dp0

echo ==========================================
echo   PHASE 1: Reset MySQL Root Password
echo ==========================================
echo.

echo [1/7] Stopping MySQL80 service...
net stop MySQL80
timeout /t 5 /nobreak >nul
echo.

echo [2/7] Adding skip-grant-tables to MySQL config...
echo skip-grant-tables >> "%MYSQL_INI%"
echo Done.
echo.

echo [3/7] Starting MySQL80 with skip-grant-tables...
net start MySQL80
echo Waiting 8 seconds for MySQL to fully start...
timeout /t 8 /nobreak >nul
echo.

echo [4/7] Resetting root password...
"%MYSQL_BIN%\mysql.exe" -u root -e "FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';"
if %errorlevel% equ 0 (
    echo Password reset command executed successfully!
) else (
    echo ERROR: Password reset command failed!
    echo Cleaning up config and restarting...
    powershell -Command "(Get-Content '%MYSQL_INI%') | Where-Object { $_ -ne 'skip-grant-tables' } | Set-Content '%MYSQL_INI%'"
    net stop MySQL80
    timeout /t 3 /nobreak >nul
    net start MySQL80
    pause
    exit /b 1
)
echo.

echo [5/7] Removing skip-grant-tables from MySQL config...
powershell -Command "(Get-Content '%MYSQL_INI%') | Where-Object { $_ -ne 'skip-grant-tables' } | Set-Content '%MYSQL_INI%'"
echo Done.
echo.

echo [6/7] Restarting MySQL80 normally...
net stop MySQL80
timeout /t 5 /nobreak >nul
net start MySQL80
timeout /t 5 /nobreak >nul
echo.

echo [7/7] Verifying MySQL connection...
"%MYSQL_BIN%\mysql.exe" -u root -proot123 -e "SELECT 'MySQL connection successful!' AS Status;" 2>nul
if %errorlevel% neq 0 (
    echo FAILED: Password reset did not work.
    pause
    exit /b 1
)
echo.
echo ==========================================
echo   PASSWORD RESET SUCCESSFUL!
echo   Username: root
echo   Password: root123
echo ==========================================
echo.

echo ==========================================
echo   PHASE 2: Setup Database
echo ==========================================
echo.

echo Creating database and tables...
"%MYSQL_BIN%\mysql.exe" -u root -proot123 < "%PROJECT_DIR%database\schema.sql" 2>nul
if %errorlevel% equ 0 (
    echo Database schema created!
) else (
    echo Schema may already exist, continuing...
)
echo.

echo Loading sample data...
"%MYSQL_BIN%\mysql.exe" -u root -proot123 < "%PROJECT_DIR%database\sample_data.sql" 2>nul
if %errorlevel% equ 0 (
    echo Sample data loaded!
) else (
    echo Sample data may already exist, continuing...
)
echo.

echo Verifying database...
"%MYSQL_BIN%\mysql.exe" -u root -proot123 -e "USE movie_ticket_management; SELECT '--- TABLES ---' AS Info; SHOW TABLES; SELECT '--- USERS ---' AS Info; SELECT user_id, username, role FROM users;"
echo.

echo ==========================================
echo   ALL DONE! Everything is set up.
echo.
echo   MySQL Credentials:
echo     Username: root
echo     Password: root123
echo.
echo   App Login Credentials:
echo     Admin:  admin / admin123
echo     User:   user  / user123
echo.
echo   Run the app with:
echo     mvn compile exec:java -Dexec.mainClass=com.movieticket.Main
echo ==========================================
echo.
pause
