# Environment Configuration Guide

## Overview

The BrowserStack Selenium Framework now uses a `.env` file for secure credential management. This allows you to store sensitive information (like BrowserStack credentials) without committing them to version control.

---

## Setup Instructions

### 1. Locate the .env File

The `.env` file is located in the project root directory:
```
E:\DON BOSCO\BrowserstackAssignment\.env
```

### 2. View Current Configuration

Your `.env` file contains:
```
BROWSERSTACK_USERNAME=priyansmehta_iL4f0W
BROWSERSTACK_ACCESSKEY=qm2cL7t6LKeR49mZHCQJ
```

These credentials are now automatically loaded when the project runs.

### 3. How It Works

The configuration loading hierarchy is:
1. **`.env` file** (highest priority) - Environment variables
2. **`config.properties`** (fallback) - Properties file
3. **Environment variables** (system level) - OS environment

---

## Configuration Files

### `.env` (Actual Credentials)
- **Location:** `E:\DON BOSCO\BrowserstackAssignment\.env`
- **Contains:** Your actual BrowserStack credentials
- **Security:** Added to `.gitignore` - NOT committed to version control
- **Visibility:** Keep private and secure

### `.env.example` (Template)
- **Location:** `E:\DON BOSCO\BrowserstackAssignment\.env.example`
- **Contains:** Example template with placeholder values
- **Purpose:** Shows what variables are needed
- **Visibility:** Can be committed to version control

### `config.properties` (Defaults)
- **Location:** `src/main/resources/config.properties`
- **Contains:** Application settings and defaults
- **Visibility:** Can be committed to version control

---

## Environment Variables

### BrowserStack Credentials
```
BROWSERSTACK_USERNAME=priyansmehta_iL4f0W
BROWSERSTACK_ACCESSKEY=qm2cL7t6LKeR49mZHCQJ
```

### Application Settings
```
APP_URL=https://elpais.com
APP_LANGUAGE=es
```

### Timeout Settings
```
TIMEOUT_IMPLICIT=10        # Implicit wait timeout in seconds
TIMEOUT_EXPLICIT=15        # Explicit wait timeout in seconds
TIMEOUT_PAGELOAD=20        # Page load timeout in seconds
```

### File Paths
```
DOWNLOAD_PATH=./downloads/images
RESULTS_PATH=./results/articles
LOGS_PATH=./logs
```

### Parallel Execution
```
PARALLEL_THREADS=5         # Number of parallel test threads
RETRY_ATTEMPTS=3           # Number of retry attempts
RETRY_DELAY_MS=1000        # Delay between retries in ms
```

### Logging
```
LOG_LEVEL=INFO             # Log level (INFO, DEBUG, WARN, ERROR)
LOG_FILE=browserstack_test.log
```

### Feature Flags
```
FEATURE_DOWNLOAD_IMAGES=true
FEATURE_TRANSLATE_ARTICLES=true
FEATURE_ANALYZE_WORDS=true
FEATURE_TAKE_SCREENSHOTS=true
```

---

## Security Best Practices

### ✅ DO:
- Keep `.env` file local and never commit it
- Use `.gitignore` to prevent accidental commits
- Rotate credentials periodically
- Use strong, unique credentials
- Share credentials securely (not via email)
- Review `.gitignore` is working

### ❌ DON'T:
- Commit `.env` file to version control
- Share credentials in code or comments
- Use placeholder credentials in production
- Hardcode sensitive information
- Share `.env` files via unsecured channels

---

## Usage in Code

### Accessing Configuration

The configuration is accessed through `ConfigManager`:

```java
// Get BrowserStack credentials
String username = ConfigManager.get("BROWSERSTACK_USERNAME");
String accessKey = ConfigManager.get("BROWSERSTACK_ACCESSKEY");

// Get application URL
String appUrl = ConfigManager.get("APP_URL");

// Get integer value
int timeout = ConfigManager.getInt("TIMEOUT_IMPLICIT", 10);

// Get boolean value
boolean downloadImages = ConfigManager.getBoolean("FEATURE_DOWNLOAD_IMAGES", true);
```

### In BrowserStackConfig

The credentials are automatically loaded:
```java
public BrowserStackConfig() {
    this.username = ConfigManager.get("BROWSERSTACK_USERNAME");
    this.accessKey = ConfigManager.get("BROWSERSTACK_ACCESSKEY");
    // ...
}
```

---

## Modifying Configuration

### To Change a Value:

1. **Edit `.env` file directly:**
   ```
   # Before
   BROWSERSTACK_USERNAME=priyansmehta_iL4f0W
   
   # After (if needed)
   BROWSERSTACK_USERNAME=new_username
   ```

2. **Save the file** - Changes take effect on next run

3. **For runtime environment variables:**
   ```bash
   # Windows PowerShell
   $env:BROWSERSTACK_USERNAME = "new_username"
   
   # Windows CMD
   set BROWSERSTACK_USERNAME=new_username
   ```

---

## Troubleshooting

### Credentials Not Loading

**Problem:** "BROWSERSTACK_USERNAME not found in .env file"

**Solutions:**
1. Verify `.env` file exists in project root
2. Check file format (UTF-8, LF line endings)
3. Ensure no spaces around `=` sign
4. Restart IDE/application
5. Check ConfigManager logs for details

### Configuration Not Applied

**Problem:** Old configuration still being used

**Solutions:**
1. Clean Maven cache: `mvn clean`
2. Rebuild project: `mvn build`
3. Restart IDE
4. Check for environment variable override

### .env File Committed by Mistake

**Recovery:**
```bash
# Remove from Git history
git rm --cached .env
git commit -m "Remove .env from tracking"

# Verify .gitignore includes .env
echo ".env" >> .gitignore
```

---

## Integration with CI/CD

### For GitHub Actions

```yaml
env:
  BROWSERSTACK_USERNAME: ${{ secrets.BROWSERSTACK_USERNAME }}
  BROWSERSTACK_ACCESSKEY: ${{ secrets.BROWSERSTACK_ACCESSKEY }}
```

### For Jenkins

Set environment variables in Jenkins job configuration or Jenkinsfile:
```groovy
environment {
    BROWSERSTACK_USERNAME = credentials('browserstack_username')
    BROWSERSTACK_ACCESSKEY = credentials('browserstack_accesskey')
}
```

---

## Maven Dependency

The project now includes the `java-dotenv` library:
```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>java-dotenv</artifactId>
    <version>5.4.1</version>
</dependency>
```

This enables automatic loading of `.env` files.

---

## Verification

To verify configuration is loaded correctly:

```java
// Print current configuration (for debugging)
String username = ConfigManager.get("BROWSERSTACK_USERNAME");
logger.info("BrowserStack username: {}", username.substring(0, 5) + "***");
```

---

## Related Files

- `.env` - Your actual credentials (SECURED)
- `.env.example` - Template for reference
- `.gitignore` - Prevents accidental commits
- `config.properties` - Default configuration
- `ConfigManager.java` - Configuration loader class
- `BrowserStackConfig.java` - BrowserStack setup

---

## Support

For issues with configuration:
1. Check this guide first
2. Review ConfigManager logs
3. Verify .env file format
4. Check .gitignore is working

---

**Last Updated:** February 14, 2026  
**Version:** 1.0.0

