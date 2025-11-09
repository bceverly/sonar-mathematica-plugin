# Installation Guide

This guide provides detailed instructions for installing the SonarQube Mathematica Plugin across different platforms and deployment scenarios.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation Methods](#installation-methods)
  - [Method 1: Manual Installation](#method-1-manual-installation)
  - [Method 2: Marketplace Installation](#method-2-marketplace-installation-coming-soon)
  - [Method 3: Docker Installation](#method-3-docker-installation)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Platform-Specific Notes](#platform-specific-notes)

## Prerequisites

Before installing the Mathematica plugin, ensure you have the following:

### Required Software

1. **SonarQube Server**
   - Minimum version: **9.9 LTS** or higher
   - Recommended: SonarQube 10.x or later
   - Download from: https://www.sonarqube.org/downloads/

2. **Java Development Kit (JDK)**
   - Minimum version: **Java 11** (for plugin development)
   - Recommended: Java 17 LTS or Java 21 LTS
   - For runtime: SonarQube's bundled Java is sufficient

3. **SonarScanner**
   - Latest version recommended
   - Download from: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/
   - Alternative: Use sonar-scanner-gradle or sonar-scanner-maven

### System Requirements

- **Memory**: Minimum 2GB RAM for SonarQube (4GB+ recommended)
- **Disk Space**:
  - Plugin file: ~10-20 MB
  - Analysis results: Varies by project size
- **Network**: Access to SonarQube server (default port 9000)

### Build Prerequisites (Only for Building from Source)

If you plan to build the plugin from source:

- **Gradle**: Version 7.x or higher (or use included Gradle Wrapper)
- **Git**: For cloning the repository
- **Java JDK 11+**: For compilation

## Installation Methods

### Method 1: Manual Installation

This is the most common method for installing the plugin.

#### Step 1: Build or Download the Plugin JAR

**Option A: Build from Source**

```bash
# Clone the repository
cd ~/dev
git clone https://github.com/bceverly/wolfralyze.git
cd wolfralyze

# Build the plugin
./gradlew clean build

# The JAR file will be created at:
# build/libs/wolfralyze-<version>.jar
```

**Option B: Download Pre-built Release**

```bash
# Download the latest release from GitHub
# (Replace <version> with the latest version number)
curl -L -o wolfralyze.jar \
  https://github.com/bceverly/wolfralyze/releases/download/v<version>/wolfralyze-<version>.jar

# Download the SBOM (Software Bill of Materials) - optional but recommended
curl -L -o wolfralyze-sbom.json \
  https://github.com/bceverly/wolfralyze/releases/download/v<version>/wolfralyze-<version>-sbom.json
```

**Verify the Download (Recommended)**

Each release includes SHA256 checksums for security verification:

```bash
# Verify the JAR file
shasum -a 256 wolfralyze.jar
# Compare with the hash shown in the release notes

# Verify the SBOM file (if downloaded)
shasum -a 256 wolfralyze-sbom.json
# Compare with the SBOM hash in the release notes
```

The SBOM (Software Bill of Materials) provides:
- Complete list of dependencies and their versions
- License information for compliance
- Security vulnerability tracking
- Supply chain transparency

See the [SBOM Documentation](https://github.com/bceverly/wolfralyze/wiki/SBOM) for more details.

#### Step 2: Install the Plugin

**On Linux/macOS:**

```bash
# Stop SonarQube (if running)
$SONARQUBE_HOME/bin/linux-x86-64/sonar.sh stop

# Copy the plugin JAR to the extensions/plugins directory
cp build/libs/wolfralyze-*.jar $SONARQUBE_HOME/extensions/plugins/

# Verify the file was copied
ls -lh $SONARQUBE_HOME/extensions/plugins/ | grep mathematica

# Start SonarQube
$SONARQUBE_HOME/bin/linux-x86-64/sonar.sh start
```

**On macOS (with different architecture):**

```bash
# Stop SonarQube
$SONARQUBE_HOME/bin/macosx-universal-64/sonar.sh stop

# Copy plugin
cp build/libs/wolfralyze-*.jar $SONARQUBE_HOME/extensions/plugins/

# Start SonarQube
$SONARQUBE_HOME/bin/macosx-universal-64/sonar.sh start
```

**On Windows:**

```cmd
REM Stop SonarQube
%SONARQUBE_HOME%\bin\windows-x86-64\StopSonar.bat

REM Copy plugin
copy build\libs\wolfralyze-*.jar %SONARQUBE_HOME%\extensions\plugins\

REM Start SonarQube
%SONARQUBE_HOME%\bin\windows-x86-64\StartSonar.bat
```

#### Step 3: Using the Gradle Install Task (Alternative)

The plugin includes a convenience Gradle task that automatically installs to `~/.sonar/extensions/plugins/`:

```bash
# Build and install in one step
./gradlew installPlugin

# Output will show:
# Plugin installed to: /Users/username/.sonar/extensions/plugins
# Restart SonarQube to activate the plugin
```

**Note**: This installs to the user's home directory, which is useful for development but may not be the location of your production SonarQube instance.

### Method 2: Marketplace Installation (Coming Soon)

Once the plugin is published to the SonarQube Marketplace, you can install it directly from the SonarQube UI.

#### Steps (When Available):

1. Log in to SonarQube as an administrator
2. Navigate to **Administration → Marketplace**
3. Click the **"Plugins"** tab
4. Search for **"Mathematica"**
5. Click **"Install"** next to the Mathematica plugin
6. Wait for the download to complete
7. Click **"Restart Server"** when prompted

**Current Status**: The plugin is not yet published to the Marketplace. Please use Manual Installation in the meantime.

### Method 3: Docker Installation

#### Option A: Using Docker Compose

Create a `docker-compose.yml` file:

```yaml
version: '3.8'

services:
  sonarqube:
    image: sonarqube:10-community
    container_name: sonarqube
    ports:
      - "9000:9000"
    environment:
      - SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_logs:/opt/sonarqube/logs
      - sonarqube_extensions:/opt/sonarqube/extensions
      # Mount the plugin directory
      - ./plugins:/opt/sonarqube/extensions/plugins
    networks:
      - sonarqube_network

volumes:
  sonarqube_data:
  sonarqube_logs:
  sonarqube_extensions:

networks:
  sonarqube_network:
    driver: bridge
```

**Install the plugin:**

```bash
# Create plugins directory
mkdir -p plugins

# Copy or download the plugin JAR
cp build/libs/wolfralyze-*.jar plugins/

# Start SonarQube
docker-compose up -d

# View logs
docker-compose logs -f sonarqube
```

#### Option B: Using Docker Run Command

```bash
# Create a volume for plugins
docker volume create sonarqube_plugins

# Copy plugin to a temporary container
docker container create --name temp_plugins -v sonarqube_plugins:/plugins alpine
docker cp wolfralyze-*.jar temp_plugins:/plugins/
docker rm temp_plugins

# Run SonarQube with the plugin volume
docker run -d \
  --name sonarqube \
  -p 9000:9000 \
  -v sonarqube_plugins:/opt/sonarqube/extensions/plugins \
  sonarqube:10-community
```

#### Option C: Custom Dockerfile

Create a custom Docker image with the plugin pre-installed:

```dockerfile
FROM sonarqube:10-community

# Set user to root to install plugin
USER root

# Copy plugin JAR
COPY wolfralyze-*.jar /opt/sonarqube/extensions/plugins/

# Set ownership
RUN chown sonarqube:sonarqube /opt/sonarqube/extensions/plugins/wolfralyze-*.jar

# Switch back to sonarqube user
USER sonarqube
```

Build and run:

```bash
# Build custom image
docker build -t sonarqube-mathematica .

# Run the container
docker run -d \
  --name sonarqube \
  -p 9000:9000 \
  sonarqube-mathematica
```

## Verification

After installation, verify that the plugin is properly loaded:

### Step 1: Check SonarQube Logs

```bash
# Linux/macOS
tail -f $SONARQUBE_HOME/logs/sonar.log

# Look for lines like:
# INFO  web[][o.s.s.p.PluginLoader] Load plugin 'Mathematica' [mathematica]
# INFO  web[][o.s.s.p.ServerPluginRepository] Deploy plugin Mathematica / 0.1.0 / ...
```

**Docker:**

```bash
docker logs -f sonarqube | grep -i mathematica
```

### Step 2: Verify in SonarQube UI

1. Open your browser and navigate to **http://localhost:9000**
2. Log in as administrator (default: `admin` / `admin`)
3. Go to **Administration → Marketplace → Installed**
4. Look for **"Mathematica"** in the list of installed plugins

**Expected Information:**
- **Name**: Mathematica
- **Version**: (e.g., 0.1.0)
- **License**: GPL-3.0
- **Organization**: Community

### Step 3: Check for Mathematica Language

1. Create a test project with a `.m` file
2. Go to **Administration → Configuration → Languages**
3. Verify that **"Mathematica"** appears in the list

### Step 4: Verify Quality Profile

1. Navigate to **Quality Profiles**
2. Look for **"Sonar way"** profile for **Mathematica**
3. Click on it to see the activated rules

**Expected Rule Count**: 464 rules across various categories

### Step 5: Test Analysis

Run a test analysis on a sample Mathematica file:

```bash
# Create a test project directory
mkdir -p ~/test-mathematica-project
cd ~/test-mathematica-project

# Create a sample Mathematica file
cat > sample.m << 'EOF'
(* Simple Mathematica function *)
factorial[n_] := If[n <= 1, 1, n * factorial[n - 1]]

(* Test the function *)
Print[factorial[5]]
EOF

# Create sonar-project.properties
cat > sonar-project.properties << 'EOF'
sonar.projectKey=mathematica-test
sonar.projectName=Mathematica Test Project
sonar.projectVersion=1.0
sonar.sources=.
sonar.sourceEncoding=UTF-8
sonar.mathematica.file.suffixes=.m,.wl,.wls
EOF

# Run analysis
sonar-scanner
```

**Successful Output:**

```
INFO: Analysis report generated in XXms
INFO: Analysis report compressed in XXms
INFO: Analysis report uploaded in XXms
INFO: ANALYSIS SUCCESSFUL
```

Check the project in SonarQube UI to see analysis results.

## Troubleshooting

### Issue: Plugin Not Appearing in SonarQube

**Symptoms:**
- Plugin not listed in Administration → Marketplace → Installed
- No Mathematica language option

**Solutions:**

1. **Verify JAR location:**
   ```bash
   ls -lh $SONARQUBE_HOME/extensions/plugins/ | grep mathematica
   ```

2. **Check file permissions:**
   ```bash
   # Ensure the plugin JAR is readable
   chmod 644 $SONARQUBE_HOME/extensions/plugins/wolfralyze-*.jar
   ```

3. **Check SonarQube logs for errors:**
   ```bash
   grep -i "mathematica\|error\|exception" $SONARQUBE_HOME/logs/sonar.log
   ```

4. **Verify SonarQube version:**
   - The plugin requires SonarQube 9.9+
   - Check your version: **Administration → System → System Info**

5. **Remove old plugin versions:**
   ```bash
   # Remove any old versions
   rm $SONARQUBE_HOME/extensions/plugins/wolfralyze-old-version.jar
   ```

### Issue: "Java version is too old"

**Error Message:**
```
ERROR: Java 1.8 is not supported. Please upgrade to Java 11 or higher
```

**Solution:**

```bash
# Check Java version
java -version

# Install Java 11+ (macOS with Homebrew)
brew install openjdk@11

# Set JAVA_HOME (add to ~/.bashrc or ~/.zshrc)
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-11-jdk

# Verify
java -version
```

### Issue: Files Not Being Analyzed

**Symptoms:**
- SonarQube shows 0 lines of Mathematica code
- No issues reported

**Solutions:**

1. **Verify file extensions:**
   ```bash
   # In sonar-project.properties
   sonar.mathematica.file.suffixes=.m,.wl,.wls
   ```

2. **Check exclusion patterns:**
   ```bash
   # Remove or verify exclusions
   # sonar.exclusions=**/test/**,**/temp/**
   ```

3. **Run scanner with verbose output:**
   ```bash
   sonar-scanner -X 2>&1 | grep -i mathematica
   ```

4. **Verify source directory:**
   ```bash
   # In sonar-project.properties
   sonar.sources=.
   # Or specify exact paths
   sonar.sources=src,lib
   ```

### Issue: "Plugin incompatible with SonarQube version"

**Error Message:**
```
Plugin 'mathematica' requires SonarQube 9.9+
```

**Solution:**

1. **Check your SonarQube version:**
   - **Administration → System → System Info**
   - Look for "SonarQube" version

2. **Upgrade SonarQube:**
   ```bash
   # Download latest version
   wget https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-10.x.zip

   # Extract and migrate
   unzip sonarqube-10.x.zip
   # Follow SonarQube upgrade documentation
   ```

3. **Or downgrade plugin requirements** (not recommended):
   - Modify `build.gradle` and rebuild

### Issue: Docker Container Not Starting

**Symptoms:**
- Container exits immediately
- Plugin not loading in Docker

**Solutions:**

1. **Check container logs:**
   ```bash
   docker logs sonarqube
   ```

2. **Verify plugin permissions:**
   ```dockerfile
   # In Dockerfile, ensure proper ownership
   RUN chown sonarqube:sonarqube /opt/sonarqube/extensions/plugins/*.jar
   ```

3. **Check volume mounts:**
   ```bash
   docker inspect sonarqube | grep -A 10 Mounts
   ```

4. **Increase memory:**
   ```yaml
   # In docker-compose.yml
   environment:
     - SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true
     - SONAR_JAVA_OPTS=-Xmx2G -Xms512m
   ```

### Issue: Analysis Timeout or Performance Issues

**Symptoms:**
- Analysis takes very long
- SonarQube becomes unresponsive

**Solutions:**

1. **Check file size limits:**
   - Plugin skips files > 25,000 lines (by design)
   - Check logs for SKIP or TIMEOUT warnings

2. **Exclude large or generated files:**
   ```bash
   # In sonar-project.properties
   sonar.exclusions=**/generated/**,**/Experiment.m
   ```

3. **Adjust performance settings:**
   ```bash
   # Increase timeout (not recommended, use exclusions instead)
   # The 30-second timeout is intentional
   ```

4. **Review logs:**
   ```bash
   grep -i "TIMEOUT\|SKIP" $SONARQUBE_HOME/logs/sonar.log
   ```

## Platform-Specific Notes

### macOS

#### Silicon (M1/M2) vs Intel

- SonarQube binaries: Use `macosx-universal-64` for both architectures
- Java: Install native ARM64 version for better performance on M1/M2

```bash
# Check architecture
uname -m
# arm64 = Apple Silicon (M1/M2)
# x86_64 = Intel

# Start SonarQube on macOS
$SONARQUBE_HOME/bin/macosx-universal-64/sonar.sh start
```

#### Gatekeeper Issues

If macOS blocks the plugin JAR:

```bash
# Remove quarantine attribute
xattr -d com.apple.quarantine $SONARQUBE_HOME/extensions/plugins/wolfralyze-*.jar
```

### Linux

#### SELinux

If SELinux is enforcing, you may need to adjust contexts:

```bash
# Check SELinux status
getenforce

# Set proper context (if needed)
chcon -t lib_t $SONARQUBE_HOME/extensions/plugins/wolfralyze-*.jar
```

#### Service Manager

Install SonarQube as a systemd service:

```bash
# Create service file: /etc/systemd/system/sonarqube.service
[Unit]
Description=SonarQube service
After=network.target

[Service]
Type=forking
ExecStart=/opt/sonarqube/bin/linux-x86-64/sonar.sh start
ExecStop=/opt/sonarqube/bin/linux-x86-64/sonar.sh stop
User=sonarqube
Group=sonarqube
Restart=always

[Install]
WantedBy=multi-user.target

# Enable and start
sudo systemctl enable sonarqube
sudo systemctl start sonarqube
```

### Windows

#### Path Length Limits

Windows has a 260-character path limit (by default). Enable long paths:

```cmd
REM Run as Administrator
reg add HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1 /f
```

#### Running as a Service

Install SonarQube as a Windows service:

```cmd
cd %SONARQUBE_HOME%\bin\windows-x86-64
InstallNTService.bat

REM Start the service
StartNTService.bat
```

#### Firewall

Ensure port 9000 is open:

```cmd
netsh advfirewall firewall add rule name="SonarQube" dir=in action=allow protocol=TCP localport=9000
```

### Docker Specific

#### Persistence

Always use volumes for persistence:

```yaml
volumes:
  - sonarqube_data:/opt/sonarqube/data
  - sonarqube_logs:/opt/sonarqube/logs
  - sonarqube_extensions:/opt/sonarqube/extensions
```

#### Network Access

If using Docker Compose with sonar-scanner:

```yaml
services:
  sonarqube:
    networks:
      - sonar_network

  scanner:
    image: sonarsource/sonar-scanner-cli
    networks:
      - sonar_network
    environment:
      - SONAR_HOST_URL=http://sonarqube:9000
```

#### Resource Limits

Set appropriate resource limits:

```yaml
services:
  sonarqube:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 4G
        reservations:
          memory: 2G
```

## Next Steps

After successful installation:

1. **Configure your project**: See [Configuration.md](Configuration.md)
2. **Review Quality Profile**: Customize rules for your team
3. **Set up CI/CD integration**: Automate analysis in your pipeline
4. **Install SonarLint**: Get IDE integration with Quick Fixes

## Additional Resources

- **Official Documentation**: [SonarQube Docs](https://docs.sonarqube.org/)
- **Plugin Repository**: [GitHub](https://github.com/bceverly/wolfralyze)
- **Issue Tracker**: [GitHub Issues](https://github.com/bceverly/wolfralyze/issues)
- **Community Support**: [SonarSource Community](https://community.sonarsource.com/)

---

**Version**: Compatible with SonarQube 9.9+
**Last Updated**: November 2025
**License**: GPL-3.0
