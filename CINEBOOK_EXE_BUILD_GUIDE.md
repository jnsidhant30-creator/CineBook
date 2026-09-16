# CineBook Executable Build Guide

This document describes how to package the CineBook Movie Ticket Management System into a standalone Windows Executable (`CineBook.exe`) using Java's `jpackage` utility.

## Prerequisites
1. **Java Development Kit (JDK) 21**: Must be installed and configured in your `PATH`.
2. **Apache Maven**: Must be installed and configured in your `PATH`.
3. **WiX Toolset (v3.0 or later)**: Required by `jpackage` to build Windows `.exe` installers. Must be installed and in your `PATH`.

## How to Rebuild the EXE

A batch script `build-exe.bat` has been provided in the root directory to automate the entire build process.

To rebuild the application, open a terminal in the project root and run:
```cmd
.\build-exe.bat
```

**What the script does:**
1. Runs `mvn clean package -DskipTests` to compile the Java source code and build the main JAR.
2. The `maven-dependency-plugin` automatically copies all necessary runtime dependencies into `target/lib`.
3. Prepares a `build_input` directory with the main JAR and `lib` folder.
4. Executes `jpackage` twice:
   - Once for `--type exe` to generate a Windows installer (`CineBook-1.0.0.exe`).
   - Once for `--type app-image` to generate a portable application folder.

## Output Location
Once the build completes successfully, you will find the outputs in the `dist/` directory:
- `dist/CineBook-1.0.0.exe`: The Windows Installer.
- `dist/CineBook-Portable/`: A portable folder containing the `.exe` that does not require installation.

## How to Change the Application Version
1. Update the `<version>` tag in `pom.xml`.
2. Update the `--app-version` parameter in `build-exe.bat`.
   *Note: Windows versioning requires the format `major.minor.patch` (e.g., `1.0.0` or `1.1.2`).*

## How to Change the Icon
1. Replace `src/main/resources/cinebook.ico` with your new `.ico` file.
2. Rerun `build-exe.bat`.

## Distributing the Application
To distribute the application to end-users, you can provide either:
- The **Installer** (`dist/CineBook-1.0.0.exe`): Users double-click to install it on their machine like standard software.
- The **Portable Image** (`dist/CineBook-Portable/`): Zip this entire folder and send it. Users can extract it and run `CineBook-Portable\CineBook.exe` without installation.

## Runtime Requirements
Even though Java is bundled inside the executable, the application still requires the following environment to function properly:

### Database Requirements
- A local or remote MySQL server must be running.
- The database schema must be created (`movie_ticket_db`).
- The application uses standard JDBC connections. Ensure the credentials defined in your codebase (or external configs) match the running MySQL instance.

### OMDb Configuration Requirements
- The application relies on the OMDb API for movie posters and details.
- Ensure the `API_KEY` in `OmdbService` is valid, or the environment variable is configured if the application was modified to read from the environment.
