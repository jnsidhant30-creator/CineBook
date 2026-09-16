@echo off
echo Starting CineBook Packaging Process...

echo [1/4] Cleaning and Building Project...
call mvn clean package -Dmaven.test.skip=true
if %errorlevel% neq 0 (
    echo Maven build failed.
    exit /b %errorlevel%
)

echo [2/4] Preparing Input Directory...
if exist build_input rmdir /s /q build_input
mkdir build_input
copy target\MovieTicketManagementSystem-1.0-SNAPSHOT.jar build_input\
xcopy target\lib build_input\lib\ /E /I /Q

echo [3/4] Running jpackage for Windows EXE Installer...
if exist dist rmdir /s /q dist
jpackage ^
  --type exe ^
  --name CineBook ^
  --app-version 1.0.0 ^
  --vendor CineBook ^
  --description "CineBook Movie Ticket Management System" ^
  --icon src\main\resources\cinebook.ico ^
  --dest dist ^
  --input build_input ^
  --main-jar MovieTicketManagementSystem-1.0-SNAPSHOT.jar ^
  --main-class com.movieticket.Main ^
  --win-shortcut ^
  --win-menu

echo [4/4] Running jpackage for Portable App Image...
jpackage ^
  --type app-image ^
  --name CineBook-App ^
  --app-version 1.0.0 ^
  --vendor CineBook ^
  --description "CineBook Movie Ticket Management System" ^
  --icon src\main\resources\cinebook.ico ^
  --dest dist ^
  --input build_input ^
  --main-jar MovieTicketManagementSystem-1.0-SNAPSHOT.jar ^
  --main-class com.movieticket.Main


echo Packaging Complete! Check the 'dist' directory.
