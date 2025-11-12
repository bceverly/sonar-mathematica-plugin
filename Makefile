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

.PHONY: help build clean install test version check-sonarqube-home lint self-scan update-wiki release-notes

# Default target - show help
help:
	@echo ""
	@echo "SonarQube Mathematica Plugin - Build System"
	@echo "============================================"
	@echo ""
	@echo "Available targets:"
	@echo ""
	@echo "  make help        - Show this help message (default)"
	@echo "  make build       - Compile and build the plugin JAR"
	@echo "  make test        - Run all unit tests"
	@echo "  make lint        - Run code style checks with Checkstyle"
	@echo "  make clean       - Remove all build artifacts and intermediate files"
	@echo "  make install     - Stop SonarQube, install plugin, restart, wait for ready"
	@echo "  make self-scan      - Scan this plugin's Java code with SonarQube"
	@echo "  make update-wiki    - Update GitHub Wiki with latest documentation"
	@echo "  make release-notes  - Generate RELEASE_NOTES.md from git commits"
	@echo "  make version        - Show current version from git tag"
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
	@echo "  make self-scan                              # Analyze plugin code with SonarQube"
	@echo ""

# Build the plugin JAR
build:
	@echo "Building SonarQube Mathematica Plugin..."
	@# Remove any old plugin JARs from build/libs/ first
	@if ls build/libs/wolfralyze-*.jar 1> /dev/null 2>&1; then \
		echo "Removing old plugin JARs from build/libs/:"; \
		ls -lh build/libs/wolfralyze-*.jar | awk '{print "  " $$9}'; \
		rm -f build/libs/wolfralyze-*.jar; \
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

# Run linter (Checkstyle + Java Deprecation Warnings)
lint:
	@echo "========================================"
	@echo "Code Quality Checks"
	@echo "========================================"
	@echo ""
	@echo "[1/3] Compiling with deprecation warnings..."
	@echo "========================================"
	@./gradlew compileJava compileTestJava 2>&1 | tee /tmp/lint_compile.log
	@echo ""
	@# Count and display Java warnings
	@JAVA_WARNINGS=$$(grep -c "warning:" /tmp/lint_compile.log 2>/dev/null || echo "0" | tr -d '\n'); \
	if [ "$${JAVA_WARNINGS:-0}" -gt 0 ] 2>/dev/null; then \
		echo "❌ JAVA DEPRECATION WARNINGS: $$JAVA_WARNINGS"; \
		echo ""; \
		echo "First 20 warnings:"; \
		grep "warning:" /tmp/lint_compile.log | head -20; \
		echo ""; \
	else \
		echo "✅ JAVA DEPRECATION WARNINGS: 0"; \
		echo ""; \
	fi
	@echo "[2/3] Running Checkstyle on main sources..."
	@echo "========================================"
	@./gradlew checkstyleMain
	@MAIN_VIOLATIONS=$$(grep -c "<error" build/reports/checkstyle/main.xml 2>/dev/null || echo "0" | tr -d '\n'); \
	if [ "$${MAIN_VIOLATIONS:-0}" -gt 0 ] 2>/dev/null; then \
		echo ""; \
		echo "❌ CHECKSTYLE VIOLATIONS (main): $$MAIN_VIOLATIONS"; \
	else \
		echo ""; \
		echo "✅ CHECKSTYLE VIOLATIONS (main): 0"; \
	fi
	@echo ""
	@echo "[3/3] Running Checkstyle on test sources..."
	@echo "========================================"
	@./gradlew checkstyleTest
	@TEST_VIOLATIONS=$$(grep -c "<error" build/reports/checkstyle/test.xml 2>/dev/null || echo "0" | tr -d '\n'); \
	echo ""; \
	if [ "$${TEST_VIOLATIONS:-0}" -gt 0 ] 2>/dev/null; then \
		echo "❌ CHECKSTYLE VIOLATIONS (test): $$TEST_VIOLATIONS"; \
	else \
		echo "✅ CHECKSTYLE VIOLATIONS (test): 0"; \
	fi
	@echo ""
	@echo "========================================"
	@echo "Summary"
	@echo "========================================"
	@# Calculate totals
	@JAVA_WARNINGS=$$(grep -c "warning:" /tmp/lint_compile.log 2>/dev/null | head -1); \
	if [ -z "$$JAVA_WARNINGS" ]; then JAVA_WARNINGS=0; fi; \
	MAIN_VIOLATIONS=$$(grep -c "<error" build/reports/checkstyle/main.xml 2>/dev/null | head -1); \
	if [ -z "$$MAIN_VIOLATIONS" ]; then MAIN_VIOLATIONS=0; fi; \
	TEST_VIOLATIONS=$$(grep -c "<error" build/reports/checkstyle/test.xml 2>/dev/null | head -1); \
	if [ -z "$$TEST_VIOLATIONS" ]; then TEST_VIOLATIONS=0; fi; \
	TOTAL_CHECKSTYLE=$$((MAIN_VIOLATIONS + TEST_VIOLATIONS)); \
	TOTAL_ISSUES=$$((JAVA_WARNINGS + TOTAL_CHECKSTYLE)); \
	echo ""; \
	echo "Java deprecation warnings: $$JAVA_WARNINGS"; \
	echo "Checkstyle violations:     $$TOTAL_CHECKSTYLE"; \
	echo "Total issues:              $$TOTAL_ISSUES"; \
	echo ""; \
	if [ "$$TOTAL_ISSUES" -gt 0 ]; then \
		echo "❌ LINT FAILED - Issues found!"; \
		echo ""; \
		echo "View detailed reports:"; \
		echo "  - build/reports/checkstyle/main.html"; \
		echo "  - build/reports/checkstyle/test.html"; \
		echo ""; \
		exit 1; \
	else \
		echo "✅ LINT PASSED - Zero issues!"; \
		echo ""; \
	fi

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	@# Remove all version-numbered JARs FIRST (before gradle clean)
	@if ls build/libs/wolfralyze-*.jar 1> /dev/null 2>&1; then \
		echo "  Removing old JAR files from build/libs/:"; \
		ls -lh build/libs/wolfralyze-*.jar | awk '{print "    " $$9}'; \
		rm -f build/libs/wolfralyze-*.jar; \
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
	@printf "Plugin version: "
	@(git describe --tags --exact-match 2>/dev/null || \
	  git describe --tags --abbrev=0 2>/dev/null || \
	  echo "v0.1.0-SNAPSHOT") | sed 's/^v//'
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
	@# Wait for shutdown and verify port 9000 is released
	@echo "Waiting for shutdown..."
	@sleep 3
	@echo ""
	@echo "Verifying port 9000 is released..."
	@# Try 3 times with normal wait
	@RETRY=0; \
	while [ $$RETRY -lt 3 ]; do \
		PORT_CHECK=$$(lsof -i :9000 2>/dev/null || true); \
		if [ -z "$$PORT_CHECK" ]; then \
			echo "✓ Port 9000 is clear"; \
			break; \
		fi; \
		RETRY=$$((RETRY + 1)); \
		echo ""; \
		echo "⚠️  WARNING: Port 9000 is still occupied (attempt $$RETRY/3)"; \
		echo "   SonarQube is not shutting down cleanly."; \
		echo "   Processes using port 9000:"; \
		lsof -i :9000 2>/dev/null | sed 's/^/   /'; \
		echo "   Waiting 5 seconds and retrying..."; \
		sleep 5; \
	done; \
	if [ $$RETRY -eq 3 ]; then \
		PORT_CHECK=$$(lsof -i :9000 2>/dev/null || true); \
		if [ -n "$$PORT_CHECK" ]; then \
			echo ""; \
			echo "⚠️  Port 9000 still occupied after 3 attempts. Using kill -9..."; \
			PIDS=$$(lsof -ti :9000 2>/dev/null || true); \
			if [ -n "$$PIDS" ]; then \
				echo "   Killing processes: $$PIDS"; \
				echo "$$PIDS" | xargs kill -9 2>/dev/null || true; \
				echo "   Waiting 3 seconds..."; \
				sleep 3; \
				RETRY2=0; \
				while [ $$RETRY2 -lt 3 ]; do \
					PORT_CHECK=$$(lsof -i :9000 2>/dev/null || true); \
					if [ -z "$$PORT_CHECK" ]; then \
						echo "   ✓ Port 9000 is now clear after kill -9"; \
						break; \
					fi; \
					RETRY2=$$((RETRY2 + 1)); \
					echo "   Port still occupied (attempt $$RETRY2/3 after kill -9)"; \
					sleep 3; \
				done; \
				if [ $$RETRY2 -eq 3 ]; then \
					echo ""; \
					echo "==========================================";\
					echo "ERROR: Cannot release port 9000!";\
					echo "==========================================";\
					echo ""; \
					echo "Port 9000 is still occupied after shutdown and kill -9."; \
					echo ""; \
					echo "Processes still using port 9000:"; \
					lsof -i :9000 2>/dev/null | sed 's/^/  /'; \
					echo ""; \
					echo "Please manually stop these processes and try again:"; \
					echo "  sudo lsof -ti :9000 | xargs kill -9"; \
					echo ""; \
					exit 1; \
				fi; \
			fi; \
		fi; \
	fi
	@echo ""
	@# Step 2: Remove old versions
	@echo "=========================================="
	@echo "Step 2/5: Removing old plugin versions..."
	@echo "=========================================="
	@if ls $(SONARQUBE_HOME)/extensions/plugins/wolfralyze-*.jar 1> /dev/null 2>&1; then \
		echo "Removing:"; \
		ls -lh $(SONARQUBE_HOME)/extensions/plugins/wolfralyze-*.jar | awk '{print "  " $$9}'; \
		rm -f $(SONARQUBE_HOME)/extensions/plugins/wolfralyze-*.jar; \
		if ls $(SONARQUBE_HOME)/extensions/plugins/wolfralyze-*.jar 1> /dev/null 2>&1; then \
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
	@# Get the current version and copy only that specific JAR (matches build.gradle logic)
	@VERSION=$$(git describe --tags --abbrev=0 2>/dev/null | sed 's/^v//'); \
	test -n "$$VERSION" || VERSION="0.1.0-SNAPSHOT"; \
	JAR_FILE="build/libs/wolfralyze-$$VERSION.jar"; \
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
	ls -lh $(SONARQUBE_HOME)/extensions/plugins/wolfralyze-$$VERSION.jar | awk '{print "  " $$9 " (" $$5 ")"}'
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
	@# Wait for log files to exist
	@for i in 1 2 3 4 5; do \
		if [ -f "$(SONARQUBE_HOME)/logs/sonar.log" ] && [ -f "$(SONARQUBE_HOME)/logs/web.log" ]; then \
			break; \
		fi; \
		sleep 1; \
	done
	@# Tail both logs and check for success or failure
	@set +m; \
	STARTUP_RESULT=0; \
	(tail -f $(SONARQUBE_HOME)/logs/sonar.log & echo $$! > /tmp/sonar_tail_sonar.pid) | while read line; do \
		echo "$$line"; \
		if echo "$$line" | grep -q "SonarQube is operational"; then \
			echo ""; \
			echo "=========================================="; \
			echo "  ✓ SonarQube is ready!"; \
			echo "=========================================="; \
			echo ""; \
			kill $$(cat /tmp/sonar_tail_sonar.pid 2>/dev/null) 2>/dev/null || true; \
			rm -f /tmp/sonar_tail_sonar.pid; \
			exit 0; \
		fi; \
	done & \
	SONAR_PID=$$!; \
	(tail -f $(SONARQUBE_HOME)/logs/web.log & echo $$! > /tmp/sonar_tail_web.pid) | while read line; do \
		if echo "$$line" | grep -q "BindException: Address already in use"; then \
			echo ""; \
			echo "=========================================="; \
			echo "ERROR: Failed to start web server!"; \
			echo "=========================================="; \
			echo ""; \
			echo "Tomcat failed to bind to port 9000."; \
			echo ""; \
			echo "Error details from web.log:"; \
			grep -A 5 "BindException" $(SONARQUBE_HOME)/logs/web.log | tail -10 | sed 's/^/  /'; \
			echo ""; \
			echo "This usually means:"; \
			echo "  1. Port 9000 was not properly released after shutdown"; \
			echo "  2. Another process is using port 9000"; \
			echo ""; \
			echo "Check what's using port 9000:"; \
			echo "  lsof -i :9000"; \
			echo ""; \
			kill $$(cat /tmp/sonar_tail_web.pid 2>/dev/null) 2>/dev/null || true; \
			kill $$(cat /tmp/sonar_tail_sonar.pid 2>/dev/null) 2>/dev/null || true; \
			rm -f /tmp/sonar_tail_web.pid /tmp/sonar_tail_sonar.pid; \
			kill $$SONAR_PID 2>/dev/null || true; \
			exit 1; \
		fi; \
	done & \
	WEB_PID=$$!; \
	wait $$SONAR_PID; \
	RESULT=$$?; \
	kill $$WEB_PID 2>/dev/null || true; \
	kill $$(cat /tmp/sonar_tail_web.pid 2>/dev/null) 2>/dev/null || true; \
	rm -f /tmp/sonar_tail_web.pid /tmp/sonar_tail_sonar.pid; \
	if [ $$RESULT -ne 0 ]; then \
		exit $$RESULT; \
	fi
	@# Verify port 9000 is actually listening
	@echo "Verifying web server is accessible..."
	@sleep 2
	@for i in 1 2 3 4 5; do \
		if lsof -i :9000 >/dev/null 2>&1; then \
			echo "✓ Port 9000 is listening"; \
			break; \
		fi; \
		if [ $$i -eq 5 ]; then \
			echo ""; \
			echo "⚠️  WARNING: SonarQube reported operational but port 9000 is not listening!"; \
			echo "   This may indicate a startup problem."; \
			echo "   Check $(SONARQUBE_HOME)/logs/web.log for details."; \
			echo ""; \
		fi; \
		sleep 1; \
	done
	@echo ""
	@echo "Plugin installed successfully:"; \
	ls -lh $(SONARQUBE_HOME)/extensions/plugins/wolfralyze-*.jar | awk '{print "  " $$9 " (" $$5 ")"}'; \
	echo ""; \
	echo "Web interface: http://localhost:9000"; \
	echo ""

# Self-scan: Analyze this plugin's Java code with SonarQube
self-scan: build
	@echo "=========================================="
	@echo "Self-Scanning Java Code with SonarQube"
	@echo "=========================================="
	@echo ""
	@# Check if sonar-scanner is available
	@if ! command -v sonar-scanner >/dev/null 2>&1; then \
		echo "ERROR: sonar-scanner not found in PATH"; \
		echo ""; \
		echo "Please install sonar-scanner:"; \
		echo "  - macOS: brew install sonar-scanner"; \
		echo "  - Linux: Download from https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/"; \
		echo ""; \
		exit 1; \
	fi
	@# Check if SONAR_TOKEN is set
	@if [ -z "$$SONAR_TOKEN" ]; then \
		echo "ERROR: SONAR_TOKEN environment variable is not set"; \
		echo ""; \
		echo "To generate a token:"; \
		echo "  1. Go to http://localhost:9000/account/security"; \
		echo "  2. Generate a new token (e.g., 'plugin-scan')"; \
		echo "  3. Set the token:"; \
		echo "       export SONAR_TOKEN=your_token_here"; \
		echo "  4. Run 'make self-scan' again"; \
		echo ""; \
		exit 1; \
	fi
	@# Check if SonarQube is running
	@echo "Checking if SonarQube is running..."
	@if ! curl -s http://localhost:9000/api/system/status >/dev/null 2>&1; then \
		echo ""; \
		echo "ERROR: Cannot connect to SonarQube at http://localhost:9000"; \
		echo ""; \
		echo "Please ensure SonarQube is running:"; \
		echo "  cd $(SONARQUBE_HOME) && ./bin/macosx-universal-64/sonar.sh start"; \
		echo ""; \
		exit 1; \
	fi
	@echo "✓ SonarQube is running"
	@echo "✓ SONAR_TOKEN is set"
	@echo ""
	@# Run the scan
	@echo "Running sonar-scanner..."
	@echo "Project: wolfralyze"
	@echo "Server: http://localhost:9000"
	@echo ""
	@sonar-scanner -Dsonar.host.url=http://localhost:9000
	@echo ""
	@echo "=========================================="
	@echo "✓ Self-scan complete!"
	@echo "=========================================="
	@echo ""
	@echo "View results at:"
	@echo "  http://localhost:9000/dashboard?id=wolfralyze"
	@echo ""

# Update GitHub Wiki with latest documentation
WIKI_DIR := .wiki-temp
WIKI_REPO := git@github.com:bceverly/wolfralyze.wiki.git

update-wiki:
	@echo "=========================================="
	@echo "Updating GitHub Wiki Documentation"
	@echo "=========================================="
	@echo ""
	@# Run the entire process in a single shell to allow early exit
	@set -e; \
	echo "Step 1/5: Cloning/updating wiki repository..."; \
	if [ -d "$(WIKI_DIR)" ]; then \
		echo "Cleaning up old wiki directory..."; \
		rm -rf $(WIKI_DIR); \
	fi; \
	echo "Cloning wiki from $(WIKI_REPO)..."; \
	if ! git clone $(WIKI_REPO) $(WIKI_DIR) 2>/dev/null; then \
		echo ""; \
		echo "ERROR: Failed to clone wiki repository"; \
		echo ""; \
		echo "Make sure:"; \
		echo "  1. Wiki is initialized (create first page at https://github.com/bceverly/wolfralyze/wiki)"; \
		echo "  2. SSH key is configured for git@github.com"; \
		echo "  3. You have write access to the repository"; \
		echo ""; \
		rm -rf $(WIKI_DIR); \
		exit 1; \
	fi; \
	echo "✓ Wiki repository ready"; \
	echo ""; \
	echo "Step 2/5: Copying documentation files..."; \
	for file in docs/*.md; do \
		filename=$$(basename "$$file"); \
		if [ "$$filename" != "README.md" ]; then \
			cp "$$file" "$(WIKI_DIR)/$$filename"; \
			echo "  ✓ $$filename"; \
		fi; \
	done; \
	echo ""; \
	echo "Step 3/5: Checking for changes..."; \
	cd $(WIKI_DIR); \
	if [ -z "$$(git status --porcelain)" ]; then \
		echo "✓ No changes detected - wiki is already up to date"; \
		cd ..; \
		rm -rf $(WIKI_DIR); \
		echo ""; \
		echo "=========================================="; \
		echo "✓ Wiki is already up to date!"; \
		echo "=========================================="; \
		echo ""; \
		echo "View wiki at:"; \
		echo "  https://github.com/bceverly/wolfralyze/wiki"; \
		echo ""; \
		exit 0; \
	fi; \
	echo "✓ Changes detected"; \
	echo ""; \
	echo "Changed files:"; \
	git status --short | sed 's/^/  /'; \
	echo ""; \
	echo "Step 4/5: Committing changes..."; \
	git add *.md; \
	git commit -m "Update documentation (automated via make update-wiki)" -m "- Updated from docs/ directory" -m "- Generated: $$(date '+%Y-%m-%d %H:%M:%S')"; \
	echo "✓ Changes committed"; \
	echo ""; \
	echo "Step 5/5: Pushing to GitHub..."; \
	if git push origin master; then \
		echo "✓ Wiki updated successfully"; \
	else \
		echo ""; \
		echo "ERROR: Failed to push to wiki repository"; \
		echo ""; \
		cd ..; \
		rm -rf $(WIKI_DIR); \
		exit 1; \
	fi; \
	echo ""; \
	echo "Cleaning up temporary files..."; \
	cd ..; \
	rm -rf $(WIKI_DIR); \
	echo "✓ Cleanup complete"; \
	echo ""; \
	echo "=========================================="; \
	echo "✓ Wiki Update Complete!"; \
	echo "=========================================="; \
	echo ""; \
	echo "Documentation published:"; \
	WIKI_PAGES=$$(ls -1 docs/*.md | grep -v README.md | wc -l | tr -d ' '); \
	echo "  $$WIKI_PAGES wiki pages updated"; \
	echo ""; \
	echo "View wiki at:"; \
	echo "  https://github.com/bceverly/wolfralyze/wiki"; \
	echo ""

# Generate release notes from git commits
release-notes:
	@echo "Generating release notes..."
	@LATEST_TAG=$$(git describe --tags --abbrev=0); \
	PREV_TAG=$$(git describe --tags --abbrev=0 $${LATEST_TAG}^); \
	VERSION=$${LATEST_TAG#v}; \
	echo "## Changes in version $${VERSION}" > RELEASE_NOTES.md; \
	echo "" >> RELEASE_NOTES.md; \
	git log $${PREV_TAG}..$${LATEST_TAG} --pretty=format:"- %s" --no-merges >> RELEASE_NOTES.md; \
	echo "" >> RELEASE_NOTES.md; \
	echo "" >> RELEASE_NOTES.md; \
	echo "**Full Changelog**: https://github.com/bceverly/wolfralyze/compare/$${PREV_TAG}...$${LATEST_TAG}" >> RELEASE_NOTES.md; \
	echo "✅ Release notes generated in RELEASE_NOTES.md"
