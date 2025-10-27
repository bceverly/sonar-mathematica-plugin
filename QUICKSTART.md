# Quick Start Guide

## Prerequisites Check

Before building the plugin, ensure you have:

1. **Java 11 or higher** installed:
```bash
java -version
```

2. **Gradle** (optional - the project can use Gradle Wrapper):
```bash
gradle --version
```

## Getting Started

### Step 1: Download Gradle Wrapper (if needed)

If you don't have Gradle installed, you can use the Gradle Wrapper. Download the wrapper files:

```bash
cd ~/dev/sonar-mathematica-plugin

# Download gradle wrapper jar
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar

# Download gradlew scripts
curl -L -o gradlew \
  https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradlew

curl -L -o gradlew.bat \
  https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradlew.bat

# Make gradlew executable
chmod +x gradlew
```

### Step 2: Build the Plugin

```bash
cd ~/dev/sonar-mathematica-plugin

# If you have Gradle installed:
gradle clean build

# Or using Gradle wrapper:
./gradlew clean build
```

Expected output:
```
BUILD SUCCESSFUL in Xs
```

The plugin JAR will be at: `build/libs/sonar-mathematica-plugin-0.1.0-SNAPSHOT.jar`

### Step 3: Run Tests

```bash
# Using Gradle:
gradle test

# Or using wrapper:
./gradlew test
```

### Step 4: Install to Local SonarQube

**Option A:** Copy manually to SonarQube installation:
```bash
cp build/libs/sonar-mathematica-plugin-*.jar $SONARQUBE_HOME/extensions/plugins/
```

**Option B:** Use the install task (copies to `~/.sonar/extensions/plugins/`):
```bash
./gradlew installPlugin
```

### Step 5: Restart SonarQube

```bash
# Navigate to SonarQube directory
cd $SONARQUBE_HOME

# Restart (Linux/Mac)
./bin/macosx-universal-64/sonar.sh restart

# Or on Linux
./bin/linux-x86-64/sonar.sh restart
```

### Step 6: Verify Installation

1. Open SonarQube: http://localhost:9000
2. Login as admin
3. Go to **Administration → Marketplace → Installed**
4. Look for "Mathematica" plugin

### Step 7: Test with Example Project

```bash
cd ~/dev/sonar-mathematica-plugin/examples

# Make sure you have sonar-scanner installed
sonar-scanner

# Or specify SonarQube URL:
sonar-scanner -Dsonar.host.url=http://localhost:9000
```

## Troubleshooting

### "Java version is too old"

Upgrade to Java 11 or higher:
```bash
# Check current version
java -version

# Install Java 11+ (using brew on macOS)
brew install openjdk@11
```

### "Permission denied: ./gradlew"

Make the wrapper executable:
```bash
chmod +x gradlew
```

### "Plugin not appearing in SonarQube"

1. Check the plugin was copied to the right location:
```bash
ls -la $SONARQUBE_HOME/extensions/plugins/ | grep mathematica
```

2. Check SonarQube logs for errors:
```bash
tail -f $SONARQUBE_HOME/logs/sonar.log
```

3. Make sure you restarted SonarQube after copying the plugin

### "Files not being analyzed"

1. Make sure your files have the right extensions (`.m`, `.wl`, `.wls`)
2. Check your `sonar-project.properties` has correct settings
3. Run sonar-scanner with verbose output:
```bash
sonar-scanner -X
```

## Development Workflow

### Making Changes

1. Edit Java files in `src/main/java/`
2. Run tests: `./gradlew test`
3. Build: `./gradlew build`
4. Install: `./gradlew installPlugin`
5. Restart SonarQube
6. Test your changes

### Adding New Features

See the main README.md for architecture details and roadmap.

## Next Steps

- Read the full [README.md](README.md) for detailed documentation
- Check out [examples/sample.m](examples/sample.m) for test code
- Review the source code to understand how it works
- Consider contributing improvements!
