# Makefile for SonarQube Mathematica Plugin
# ===========================================
#
# Environment Variables:
#   SONARQUBE_HOME - Root directory of SonarQube installation (required for 'install' target)
#
# Examples:
#   make                           # Show this help
#   make build                     # Build the plugin
#   make clean                     # Clean build artifacts
#   SONARQUBE_HOME=/path make install  # Install to SonarQube

.PHONY: help build clean install test version check-sonarqube-home

# Default target - show help
help:
	@echo ""
	@echo "SonarQube Mathematica Plugin - Build System"
	@echo "============================================"
	@echo ""
	@echo "Available targets:"
	@echo ""
	@echo "  make help       - Show this help message (default)"
	@echo "  make build      - Compile and build the plugin JAR"
	@echo "  make test       - Run all unit tests"
	@echo "  make clean      - Remove all build artifacts and intermediate files"
	@echo "  make install    - Stop SonarQube, install plugin, restart, wait for ready"
	@echo "  make version    - Show current version from git tag"
	@echo ""
	@echo "Environment Variables:"
	@echo ""
	@echo "  SONARQUBE_HOME  - Root directory of SonarQube installation"
	@echo "                    Required for 'install' target"
	@echo "                    Example: export SONARQUBE_HOME=/opt/sonarqube"
	@echo ""
	@echo "Examples:"
	@echo ""
	@echo "  make build                                  # Build the plugin"
	@echo "  make clean build                            # Clean and build"
	@echo "  SONARQUBE_HOME=/opt/sonarqube make install  # Install to SonarQube"
	@echo ""

# Build the plugin JAR
build:
	@echo "Building SonarQube Mathematica Plugin..."
	@# Remove any old plugin JARs from build/libs/ first
	@if ls build/libs/sonar-mathematica-plugin-*.jar 1> /dev/null 2>&1; then \
		echo "Removing old plugin JARs from build/libs/:"; \
		ls -lh build/libs/sonar-mathematica-plugin-*.jar | awk '{print "  " $$9}'; \
		rm -f build/libs/sonar-mathematica-plugin-*.jar; \
	fi
	@./gradlew build
	@echo ""
	@echo "Build complete!"
	@./gradlew -q properties | grep "^version:" || echo "Version: (see build/libs/ for JAR filename)"
	@echo "JAR location: build/libs/"
	@ls -lh build/libs/*.jar 2>/dev/null || echo "No JAR files found"
	@echo ""

# Run tests
test:
	@echo "Running tests..."
	@./gradlew test
	@echo ""
	@echo "Tests complete!"
	@echo ""

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	@# Remove all version-numbered JARs FIRST (before gradle clean)
	@if ls build/libs/sonar-mathematica-plugin-*.jar 1> /dev/null 2>&1; then \
		echo "  Removing old JAR files from build/libs/:"; \
		ls -lh build/libs/sonar-mathematica-plugin-*.jar | awk '{print "    " $$9}'; \
		rm -f build/libs/sonar-mathematica-plugin-*.jar; \
	fi
	@# Clean Gradle build artifacts
	@./gradlew clean
	@# Remove build directories completely
	@rm -rf build/
	@rm -rf .gradle/
	@rm -rf out/
	@# Verify clean
	@if ls build/libs/*.jar 1> /dev/null 2>&1; then \
		echo ""; \
		echo "WARNING: JAR files still present after clean:"; \
		ls -lh build/libs/*.jar; \
		echo ""; \
	fi
	@echo "Clean complete!"
	@echo ""

# Show version from git tag
version:
	@echo -n "Plugin version: "
	@git describe --tags --exact-match 2>/dev/null | sed 's/^v//' || echo "0.1.0-SNAPSHOT (no git tag)"
	@echo ""

# Check if SONARQUBE_HOME is set
check-sonarqube-home:
	@if [ -z "$(SONARQUBE_HOME)" ]; then \
		echo ""; \
		echo "ERROR: SONARQUBE_HOME environment variable is not set!"; \
		echo ""; \
		echo "The SONARQUBE_HOME variable must point to your SonarQube installation directory."; \
		echo ""; \
		echo "Examples:"; \
		echo "  export SONARQUBE_HOME=/opt/sonarqube"; \
		echo "  export SONARQUBE_HOME=~/sonarqube"; \
		echo "  export SONARQUBE_HOME=/Users/username/sonarqube"; \
		echo ""; \
		echo "Then run:"; \
		echo "  make install"; \
		echo ""; \
		echo "Or set it inline:"; \
		echo "  SONARQUBE_HOME=/path/to/sonarqube make install"; \
		echo ""; \
		exit 1; \
	fi
	@if [ ! -d "$(SONARQUBE_HOME)" ]; then \
		echo ""; \
		echo "ERROR: SONARQUBE_HOME directory does not exist: $(SONARQUBE_HOME)"; \
		echo ""; \
		echo "Please set SONARQUBE_HOME to a valid SonarQube installation directory."; \
		echo ""; \
		exit 1; \
	fi
	@if [ ! -d "$(SONARQUBE_HOME)/extensions/plugins" ]; then \
		echo ""; \
		echo "ERROR: $(SONARQUBE_HOME)/extensions/plugins directory not found!"; \
		echo ""; \
		echo "This doesn't appear to be a valid SonarQube installation."; \
		echo "Please verify SONARQUBE_HOME points to the root of your SonarQube directory."; \
		echo ""; \
		exit 1; \
	fi

# Install plugin to SonarQube
install: check-sonarqube-home build
	@echo "Installing plugin to SonarQube..."
	@echo "  Source: build/libs/"
	@echo "  Target: $(SONARQUBE_HOME)/extensions/plugins/"
	@echo ""
	@# Step 1: Stop SonarQube
	@echo "=========================================="
	@echo "Step 1/5: Stopping SonarQube..."
	@echo "=========================================="
	@if [ -f "$(SONARQUBE_HOME)/bin/macosx-universal-64/sonar.sh" ]; then \
		$(SONARQUBE_HOME)/bin/macosx-universal-64/sonar.sh stop; \
	elif [ -f "$(SONARQUBE_HOME)/bin/linux-x86-64/sonar.sh" ]; then \
		$(SONARQUBE_HOME)/bin/linux-x86-64/sonar.sh stop; \
	else \
		echo "ERROR: Cannot find SonarQube start script"; \
		exit 1; \
	fi
	@# Wait for shutdown
	@echo "Waiting for shutdown..."
	@sleep 3
	@echo ""
	@# Step 2: Remove old versions
	@echo "=========================================="
	@echo "Step 2/5: Removing old plugin versions..."
	@echo "=========================================="
	@if ls $(SONARQUBE_HOME)/extensions/plugins/sonar-mathematica-plugin-*.jar 1> /dev/null 2>&1; then \
		echo "Removing:"; \
		ls -lh $(SONARQUBE_HOME)/extensions/plugins/sonar-mathematica-plugin-*.jar | awk '{print "  " $$9}'; \
		rm -f $(SONARQUBE_HOME)/extensions/plugins/sonar-mathematica-plugin-*.jar; \
		if ls $(SONARQUBE_HOME)/extensions/plugins/sonar-mathematica-plugin-*.jar 1> /dev/null 2>&1; then \
			echo ""; \
			echo "ERROR: Failed to remove old plugin versions!"; \
			echo "Please check file permissions and try again."; \
			echo ""; \
			exit 1; \
		fi; \
		echo "Old versions removed successfully."; \
	else \
		echo "No old versions found."; \
	fi
	@echo ""
	@# Step 3: Install new version
	@echo "=========================================="
	@echo "Step 3/5: Installing new plugin version..."
	@echo "=========================================="
	@# Get the current version and copy only that specific JAR
	@VERSION=$$(git describe --tags --exact-match 2>/dev/null | sed 's/^v//' || echo "0.1.0-SNAPSHOT"); \
	JAR_FILE="build/libs/sonar-mathematica-plugin-$$VERSION.jar"; \
	if [ ! -f "$$JAR_FILE" ]; then \
		echo ""; \
		echo "ERROR: Expected JAR file not found: $$JAR_FILE"; \
		echo "Available JARs in build/libs/:"; \
		ls -lh build/libs/*.jar 2>/dev/null || echo "  (none)"; \
		echo ""; \
		exit 1; \
	fi; \
	cp "$$JAR_FILE" $(SONARQUBE_HOME)/extensions/plugins/; \
	echo "Installed:"; \
	ls -lh $(SONARQUBE_HOME)/extensions/plugins/sonar-mathematica-plugin-$$VERSION.jar | awk '{print "  " $$9 " (" $$5 ")"}'
	@echo ""
	@# Step 4: Start SonarQube
	@echo "=========================================="
	@echo "Step 4/5: Starting SonarQube..."
	@echo "=========================================="
	@if [ -f "$(SONARQUBE_HOME)/bin/macosx-universal-64/sonar.sh" ]; then \
		$(SONARQUBE_HOME)/bin/macosx-universal-64/sonar.sh start; \
	elif [ -f "$(SONARQUBE_HOME)/bin/linux-x86-64/sonar.sh" ]; then \
		$(SONARQUBE_HOME)/bin/linux-x86-64/sonar.sh start; \
	else \
		echo "ERROR: Cannot find SonarQube start script"; \
		exit 1; \
	fi
	@echo ""
	@# Step 5: Wait for startup
	@echo "=========================================="
	@echo "Step 5/5: Waiting for SonarQube startup..."
	@echo "=========================================="
	@echo "Monitoring $(SONARQUBE_HOME)/logs/sonar.log"
	@echo "(This may take 30-60 seconds...)"
	@echo ""
	@# Wait for log file to exist
	@for i in 1 2 3 4 5; do \
		if [ -f "$(SONARQUBE_HOME)/logs/sonar.log" ]; then \
			break; \
		fi; \
		sleep 1; \
	done
	@# Tail log until we see "SonarQube is operational"
	@tail -f $(SONARQUBE_HOME)/logs/sonar.log | while read line; do \
		echo "$$line"; \
		if echo "$$line" | grep -q "SonarQube is operational"; then \
			echo ""; \
			echo "=========================================="; \
			echo "  ✓ SonarQube is ready!"; \
			echo "=========================================="; \
			echo ""; \
			echo "Plugin installed successfully:"; \
			ls -lh $(SONARQUBE_HOME)/extensions/plugins/sonar-mathematica-plugin-*.jar | awk '{print "  " $$9 " (" $$5 ")"}'; \
			echo ""; \
			echo "Web interface: http://localhost:9000"; \
			echo ""; \
			pkill -P $$$$ tail; \
			break; \
		fi; \
	done
	@echo ""
