---
name: JavaFX Maven Setup
overview: Create a Maven pom.xml file for a JavaFX project with Java 21 and required dependencies (javafx-controls, javafx-fxml, sqlite-jdbc, controlsfx), plus set up the basic folder structure.
todos:
  - id: create-pom
    content: Create pom.xml with Java 21, JavaFX dependencies (javafx-controls, javafx-fxml), sqlite-jdbc, and controlsfx
    status: completed
  - id: create-folders
    content: "Create folder structure: src/main/java/com/finance and src/main/resources/com/finance"
    status: completed
---

# JavaFX Maven Project Setup

## Overview

Set up a Maven project structure for a JavaFX application using Java 21 with the specified dependencies and folder structure.

## Files to Create

### 1. `pom.xml` (root directory)

Create a Maven POM file with:

- Java 21 configuration
- JavaFX dependencies (javafx-controls, javafx-fxml)
- SQLite JDBC driver (sqlite-jdbc)
- ControlsFX library
- Maven Compiler Plugin configured for Java 21
- JavaFX Maven Plugin for running the application

**Key configuration details:**

- Project coordinates: `com.finance` groupId, `finance-app` artifactId
- Java version: 21
- JavaFX version: 21.0.2 (compatible with Java 21)
- SQLite JDBC version: 3.44.1.0
- ControlsFX version: 11.2.0

### 2. Folder Structure

Create the following directories:

- `src/main/java/com/finance/` - for Java source files
- `src/main/resources/com/finance/` - for resources (FXML files, images, etc.)

## Implementation Notes

- JavaFX modules need to be properly declared in the POM
- The JavaFX Maven plugin will be configured to handle module path requirements
- All dependencies will use compatible versions for Java 21
- Standard Maven directory structure will be followed (src/main/java, src/main/resources, src/test/java)