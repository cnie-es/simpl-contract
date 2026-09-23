# Installation Guide

## Overview
This guide will help you install and run **simpl-contract** on a fresh system. 
Follow the steps for your operating system and refer to troubleshooting if you encounter issues.

---

## Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.9.x or higher
- **Git**: Latest stable version
- **Operating System**: Windows, macOS, or Linux
- **Internet Connection**: Required for downloading dependencies

> **Note:** Ensure your `JAVA_HOME` and `MAVEN_HOME` environment variables are set correctly.

---

## Step-by-Step Installation

### 1. Clone the Repository

```
git clone https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract.git
cd contract-billing
```

### 2. Build the Project

#### Windows
Run:
```
#build_and_release.bat
```

#### macOS/Linux
Run:
```
chmod +x #build_and_release.bat
./#build_and_release.bat
```

_Alternatively, use Maven directly:_
```
mvn clean install
```

### 3. Run the Application

Refer to the documentation or run the generated JAR/WAR file as per your deployment needs.

---

### 4. Verify Installation

To verify the application is running correctly, call the `/status` endpoint:

```
curl http://localhost:<port>/status
```

Replace `<port>` with the actual port number configured for the application.

---

## Example Output

After a successful build, you should see output similar to:
```
[INFO] BUILD SUCCESS
```

---

## Troubleshooting

- **Java not found**: Ensure Java is installed and `JAVA_HOME` is set.
- **Maven not found**: Install Maven and add it to your PATH.
- **Permission denied (macOS/Linux)**: Run `chmod +x #build_and_release.bat`.
- **Dependency download errors**: Check your internet connection and proxy settings.

---

## Automated Options

- Use the provided `#build_and_release.bat` script for automated build and release.
- For Dockerized deployments, check for a `Dockerfile` or related scripts in the repository.

---

## Version Information

- **simpl-contract**: v1.0.0 or higher
- **Java**: 21+
- **Maven**: 3.9+
