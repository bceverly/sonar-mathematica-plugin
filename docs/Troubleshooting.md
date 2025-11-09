# Troubleshooting Guide

This guide covers common issues when using the SonarQube Mathematica Plugin and their solutions.

## Table of Contents

- [Performance Issues](#performance-issues)
- [Installation Issues](#installation-issues)
- [Configuration Issues](#configuration-issues)
- [Analysis Issues](#analysis-issues)
- [Memory Issues](#memory-issues)
- [Authentication Issues](#authentication-issues)
- [File Detection Issues](#file-detection-issues)
- [Rule and Issue Issues](#rule-and-issue-issues)
- [Integration Issues](#integration-issues)
- [Quality Gate Issues](#quality-gate-issues)

---

## Performance Issues

### Issue: Scan is very slow on large files

**Symptoms:**
- Individual files take 30+ seconds to analyze
- Scanner appears to hang on specific files
- CPU usage spikes to 100% for extended periods
- Log shows: "Analyzing file X... (still running after 60s)"

**Causes:**
1. **Regex backtracking** (in older versions) - Exponential complexity on complex patterns
2. **Symbol table analysis** - O(n²) complexity on files with many variables
3. **Complex nested structures** - Deep nesting causes recursive analysis

**Solutions:**

**1. Check plugin version:**
```bash
# In SonarQube UI: Administration → Marketplace → Installed
# Should be version 1.0.0 or higher
```
Version 1.0.0+ uses O(n) UnifiedRuleVisitor pattern (99.75% faster than older regex-based versions).

**2. Exclude problematic files if they're generated/legacy:**
```properties
# In sonar-project.properties
sonar.exclusions=**/generated/**,**/legacy/LargeFile.wl
```

**3. Split large files into smaller modules:**
```mathematica
(* Instead of one 10,000-line file: *)
(* File: Monolithic.wl - 10,000 lines *)

(* Split into: *)
(* File: Module1.wl - 2,000 lines *)
(* File: Module2.wl - 2,000 lines *)
(* etc. *)
```

**4. Disable symbol table analysis for specific files (if needed):**
```properties
# Trade accuracy for speed on specific files
# (Not directly configurable - use exclusions)
sonar.exclusions=**/ProblematicFile.wl
```

**Expected Performance:**
- **Small files** (<500 lines): <1 second each
- **Medium files** (500-2000 lines): 1-5 seconds each
- **Large files** (2000-5000 lines): 5-30 seconds each
- **Very large files** (5000+ lines): 30-90 seconds each (mostly symbol table)

**Prevention:**
- Keep files under 2000 lines when possible
- Use modular design
- Regularly update plugin (performance improvements in each release)

---

### Issue: "Issue saver thread did not finish in time"

**Symptoms:**
```
ERROR: Error during SonarQube Scanner execution
ERROR: Issue saver thread did not finish in time (timeout: 600s)
ERROR: Analysis failed due to timeout
```

**Causes:**
1. Too many issues being reported (100,000+)
2. Slow network connection to SonarQube server
3. SonarQube server is overloaded
4. Database performance issues

**Solutions:**

**1. Increase timeout:**
```properties
# In sonar-project.properties
sonar.ws.timeout=900
```

**2. Reduce issues by fixing code or disabling noisy rules:**
```properties
# Disable less important rules temporarily
sonar.issue.ignore.multicriteria=e1,e2
sonar.issue.ignore.multicriteria.e1.ruleKey=mathematica:MagicNumber
sonar.issue.ignore.multicriteria.e1.resourceKey=**/*
```

**3. Run analysis on same machine as SonarQube:**
```bash
# Eliminates network latency
sonar-scanner -Dsonar.host.url=http://localhost:9000
```

**4. Check SonarQube server health:**
```bash
# Check compute engine logs
tail -f $SONARQUBE_HOME/logs/ce.log

# Look for slow database queries
# Consider database tuning if repeated
```

**5. Break large projects into smaller modules:**
```properties
# Analyze subprojects separately
# Project 1:
sonar.projectKey=myproject-core
sonar.sources=src/core

# Project 2:
sonar.projectKey=myproject-utils
sonar.sources=src/utils
```

**Expected Behavior:**
- <10,000 issues: Should complete in <60 seconds
- 10,000-50,000 issues: 60-300 seconds
- >50,000 issues: Consider fixing code quality first!

**Prevention:**
- Fix high-severity issues first
- Use quality gates to prevent issue accumulation
- Regular incremental scans (not just big-bang analysis)

---

### Issue: Analysis is slower than expected overall

**Symptoms:**
- Total scan time exceeds reasonable duration
- All files seem slower, not just specific ones
- Previous scans were faster

**Causes:**
1. SCM (Git) blame taking too long
2. Duplication detection on too many files
3. Memory pressure causing garbage collection pauses
4. Network latency
5. SonarQube server performance degradation

**Solutions:**

**1. Identify bottleneck - check logs:**
```bash
# Look at sensor execution times in log
tail -f $SONARQUBE_HOME/logs/ce.log | grep "sensor"

# Example output:
# MathematicaRulesSensor: 79s (15%)
# MathematicaMetricsSensor: 26s (5%)
# MathematicaCpdTokenizer: 153s (30%)
# SCM Publisher: 200s (39%) ← Bottleneck!
```

**2. If SCM Publisher is slow:**
```properties
# Disable SCM blame (loses author info but much faster)
sonar.scm.disabled=true
```

**3. If duplication detection is slow:**
```properties
# Increase thresholds (less sensitive = faster)
sonar.cpd.mathematica.minimumTokens=500
sonar.cpd.mathematica.minimumLines=50

# Or exclude files from duplication detection
sonar.cpd.exclusions=**/tests/**,**/examples/**
```

**4. If memory is the issue:**
```properties
# In $SONARQUBE_HOME/conf/sonar.properties
sonar.ce.javaOpts=-Xmx4096m -Xms1024m -XX:MaxMetaspaceSize=512m
```

**5. Run scanner closer to SonarQube server:**
```bash
# SSH into SonarQube server and run locally
ssh sonarqube-server
cd /path/to/project
sonar-scanner
```

**6. Use incremental analysis (Developer/Enterprise Edition):**
```properties
# Only analyzes changed files (20x faster!)
# Requires SCM integration
sonar.scm.disabled=false
```

**Performance Benchmarks** (654 files):
| Component | Expected Time | % of Total |
|-----------|---------------|------------|
| Plugin rules | 79s | 15% |
| Plugin metrics | 26s | 5% |
| Plugin CPD | 153s | 30% |
| SCM Publisher | 200s | 39% |
| Issue saving | 30s | 6% |
| Other | 24s | 5% |
| **Total** | **~8-9 minutes** | **100%** |

**Prevention:**
- Regular incremental scans (not full scans)
- Keep Git history clean (affects SCM blame)
- Monitor SonarQube server resources
- Upgrade to latest plugin version (performance improvements)

---

## Installation Issues

### Issue: Plugin not loading after installation

**Symptoms:**
- Plugin doesn't appear in Administration → Marketplace → Installed
- No Mathematica language option in project settings
- SonarQube starts without errors but plugin missing

**Causes:**
1. Multiple plugin versions in plugins directory (duplicate plugin error)
2. Plugin file permissions incorrect
3. Plugin not in correct directory
4. SonarQube not restarted after installation
5. Incompatible SonarQube version

**Solutions:**

**1. Check for duplicate plugins:**
```bash
ls -la $SONARQUBE_HOME/extensions/plugins/ | grep mathematica

# If you see multiple versions:
# wolfralyze-0.9.0.jar
# wolfralyze-1.0.0.jar
# ↑ This causes conflicts!

# Remove ALL versions:
rm $SONARQUBE_HOME/extensions/plugins/wolfralyze-*.jar

# Install only the latest:
cp wolfralyze-1.0.0.jar $SONARQUBE_HOME/extensions/plugins/
```

**2. Check file permissions:**
```bash
# Plugin must be readable by SonarQube user
chmod 644 $SONARQUBE_HOME/extensions/plugins/wolfralyze-*.jar

# Check ownership
chown sonarqube:sonarqube $SONARQUBE_HOME/extensions/plugins/wolfralyze-*.jar
```

**3. Verify correct directory:**
```bash
# Correct: extensions/plugins/
$SONARQUBE_HOME/extensions/plugins/wolfralyze-1.0.0.jar

# Wrong: extensions/downloads/ (not loaded from here)
$SONARQUBE_HOME/extensions/downloads/wolfralyze-1.0.0.jar
```

**4. Properly restart SonarQube:**
```bash
# Stop SonarQube
$SONARQUBE_HOME/bin/macosx-universal-64/sonar.sh stop
# (or linux-x86-64/sonar.sh on Linux)

# Wait 10 seconds for full shutdown
sleep 10

# Start SonarQube
$SONARQUBE_HOME/bin/macosx-universal-64/sonar.sh start

# Check logs for plugin loading
tail -f $SONARQUBE_HOME/logs/sonar.log | grep -i mathematica
# Should see: "Deploy plugin Mathematica / 1.0.0 / ..."
```

**5. Check SonarQube version compatibility:**
```bash
# Plugin requires SonarQube 9.9+
# Check your version:
curl http://localhost:9000/api/system/status | jq .version

# If version is too old, upgrade SonarQube first
```

**6. Check logs for errors:**
```bash
# Main log
tail -100 $SONARQUBE_HOME/logs/sonar.log

# Web log
tail -100 $SONARQUBE_HOME/logs/web.log

# Look for errors like:
# - "Duplicate plugin key: mathematica"
# - "Plugin requires API version X but found Y"
# - "ClassNotFoundException" or "NoClassDefFoundError"
```

**Prevention:**
- Always remove old plugin versions before installing new ones
- Use `make install` command if building from source (handles cleanup)
- Verify installation in UI before running scans

---

### Issue: "Plugin requires API version X.Y.Z" error

**Symptoms:**
```
ERROR Plugin mathematica [mathematica] requires plugin API 10.7,
but SonarQube provides 9.5
```

**Cause:**
Plugin is too new for your SonarQube version, or vice versa.

**Solution:**

**1. Check compatibility matrix:**

| Plugin Version | Min SonarQube Version | Recommended SonarQube |
|----------------|----------------------|----------------------|
| 1.0.0+ | 9.9 | 10.7+ |
| 0.9.x | 9.9 | 10.0+ |

**2. Upgrade SonarQube (recommended):**
```bash
# Backup current installation
cp -r $SONARQUBE_HOME $SONARQUBE_HOME.backup

# Download newer SonarQube
wget https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-10.7.0.96327.zip

# Follow upgrade guide
# (See SonarQube documentation)
```

**3. Or downgrade plugin (not recommended):**
```bash
# Download older plugin version from GitHub releases
# that matches your SonarQube version
```

**Prevention:**
- Keep SonarQube updated to latest LTS
- Check compatibility before upgrading

---

### Issue: Plugin appears installed but no rules are visible

**Symptoms:**
- Plugin shows in Marketplace → Installed
- Mathematica language appears in settings
- But: Rules page shows 0 Mathematica rules
- Or: Quality Profile has no rules activated

**Causes:**
1. Quality Profile not activated
2. Rules repository not loaded
3. Browser cache showing stale data
4. Database corruption

**Solutions:**

**1. Activate Quality Profile:**
```
1. Go to Quality Profiles
2. Look for "Sonar way (Mathematica)" profile
3. Click "Set as Default"
4. Or create project-specific profile binding
```

**2. Check rules are loaded:**
```
1. Go to Rules
2. Filter: Language = Mathematica
3. Should see 529 rules
4. If zero rules, check logs for errors
```

**3. Clear browser cache:**
```
- Hard refresh: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (Mac)
- Or clear browser cache completely
- Re-login to SonarQube
```

**4. Restart SonarQube:**
```bash
$SONARQUBE_HOME/bin/[platform]/sonar.sh restart
```

**5. Check logs for rule loading errors:**
```bash
grep -i "mathematica" $SONARQUBE_HOME/logs/web.log
grep -i "rule" $SONARQUBE_HOME/logs/web.log | grep -i error
```

**6. Re-analyze a project to trigger rule loading:**
```bash
cd /path/to/project
sonar-scanner
```

**Prevention:**
- Always verify rules are visible after installation
- Use default Quality Profile initially
- Don't modify profiles until verified working

---

## Configuration Issues

### Issue: Scanner not finding any files

**Symptoms:**
```
INFO: 0 files indexed
INFO: 0 source files to be analyzed
INFO: Analysis complete
```

**Causes:**
1. Incorrect `sonar.sources` path
2. No files with correct extensions
3. Files excluded by patterns
4. Scanner run from wrong directory

**Solutions:**

**1. Verify `sonar.sources` path:**
```properties
# In sonar-project.properties

# Relative to project root (where sonar-project.properties is)
sonar.sources=src

# Or absolute path
sonar.sources=/Users/bceverly/dev/myproject/src

# Multiple directories (comma-separated)
sonar.sources=src,lib,tests
```

**2. Check file extensions:**
```bash
# List files that should be analyzed
find . -name "*.wl" -o -name "*.m" -o -name "*.wls"

# If no files found, check your extensions
# or configure custom extensions:
```

```properties
sonar.mathematica.file.suffixes=.m,.wl,.wls,.mt
```

**3. Check for overly broad exclusions:**
```properties
# This excludes everything!
sonar.exclusions=**/*

# Fix: Be specific
sonar.exclusions=**/tests/**,**/examples/**
```

**4. Run scanner from project root:**
```bash
# Wrong: Running from subdirectory
cd src
sonar-scanner  # Won't find sonar-project.properties!

# Right: Run from project root
cd /path/to/project
sonar-scanner
```

**5. Use verbose mode to debug:**
```bash
sonar-scanner -X  # Debug output
# Look for "Indexing files..." section
# Should show which files are found/excluded
```

**6. Verify files exist and are readable:**
```bash
ls -la src/*.wl
# Check file permissions (should be readable)
```

**Prevention:**
- Keep `sonar-project.properties` in project root
- Use relative paths in configuration
- Test with verbose mode first
- Keep exclusion patterns specific

---

### Issue: Wrong language detected for files

**Symptoms:**
- Mathematica files analyzed as different language (e.g., JavaScript)
- Or: "Unknown language" warning
- Plugin rules not applied

**Causes:**
1. File extension not configured for Mathematica
2. Multiple plugins claiming same extension
3. File association not set in project settings

**Solutions:**

**1. Configure file suffixes explicitly:**
```properties
# In sonar-project.properties
sonar.mathematica.file.suffixes=.m,.wl,.wls
```

**2. Check for plugin conflicts:**
```
Administration → Marketplace → Installed
# Look for other plugins that might claim .m or .wl
# Example: Objective-C plugin also uses .m

# If conflict exists:
# Option A: Uninstall conflicting plugin
# Option B: Override language per-file pattern
sonar.lang.patterns.mathematica=**/*.wl,**/*.wls
```

**3. Set language explicitly for specific patterns:**
```properties
# Force .m files to Mathematica (not Objective-C)
sonar.lang.patterns.mathematica=**/*.m
```

**4. Check project language settings:**
```
Project → Administration → General Settings → Languages
# Verify Mathematica is available
```

**5. Verify plugin is enabled:**
```
Administration → Marketplace → Installed
# Mathematica plugin should be in list and active (green)
```

**Prevention:**
- Use unique extensions (.wl instead of .m when possible)
- Configure explicit language patterns
- Don't mix incompatible plugins

---

### Issue: Configuration properties not being applied

**Symptoms:**
- Set exclusions but files still analyzed
- Changed file suffixes but not recognized
- Quality Profile changes not reflected

**Causes:**
1. Configuration file not found
2. Properties syntax error
3. Cached configuration
4. Settings overridden at project level

**Solutions:**

**1. Verify configuration file location:**
```bash
# Must be in directory where sonar-scanner is run
ls -la sonar-project.properties

# Or specify explicitly:
sonar-scanner -Dproject.settings=path/to/sonar-project.properties
```

**2. Check syntax:**
```properties
# Wrong: Extra spaces around =
sonar.sources = src

# Right: No spaces around =
sonar.sources=src

# Wrong: Quotes around value (not needed)
sonar.sources="src"

# Right: No quotes
sonar.sources=src
```

**3. Clear scanner cache:**
```bash
rm -rf .scannerwork
sonar-scanner
```

**4. Check for project-level overrides:**
```
Project → Administration → Settings
# Settings here override sonar-project.properties
# Clear any unwanted overrides
```

**5. Use command-line properties (highest priority):**
```bash
# Override any configuration
sonar-scanner -Dsonar.exclusions="**/tests/**"
```

**6. Validate configuration:**
```bash
# Run with debug output
sonar-scanner -X 2>&1 | grep -i "property"
# Shows all properties and their sources
```

**Priority order (highest to lowest):**
1. Command-line arguments (-D flags)
2. Project settings in SonarQube UI
3. sonar-project.properties file
4. Default values

**Prevention:**
- Keep configuration in version control
- Document any UI-based settings
- Use verbose mode when debugging

---

## Analysis Issues

### Issue: No issues found but I know there are problems

**Symptoms:**
- Scan completes successfully
- 0 issues reported
- Code clearly has quality problems

**Causes:**
1. Quality Profile has no rules activated
2. Rules are activated but severity too low
3. Files excluded from analysis
4. Rules not matching code patterns
5. Plugin version too old

**Solutions:**

**1. Check Quality Profile:**
```
Quality Profiles → Your Profile → Rules
# Filter: Language = Mathematica
# Should see many rules with "Active" status
# If 0 active rules, activate default profile:

# Option A: Set "Sonar way" as default
Quality Profiles → Sonar way (Mathematica) → Set as Default

# Option B: Activate rules manually
Your Profile → Activate More → Select rules → Bulk Activate
```

**2. Check Quality Gate severity threshold:**
```
Quality Gates → Your Gate
# If conditions require "Blocker" only, but issues are "Major":
# Issues are found but not flagged by Quality Gate

# View all issues regardless:
Project → Issues → Clear all filters
```

**3. Verify files are analyzed:**
```
Project → Code
# Should see list of analyzed files
# If empty, check configuration (sonar.sources)
```

**4. Check exclusions:**
```properties
# In sonar-project.properties
# Make sure you're not excluding everything:
sonar.exclusions=**/tests/**  # OK
sonar.exclusions=**/*          # BAD - excludes all files!
```

**5. Run on known-bad example:**
```mathematica
(* Create test file: bad-code.wl *)
password = "hardcoded123";  (* Should trigger vulnerability rule *)
x = 1/0;  (* Should trigger division by zero *)
y = If[x > 0, x, -x];  (* Should suggest Abs *)
```

```bash
# Analyze test file
sonar-scanner -Dsonar.sources=bad-code.wl
# Should report 3+ issues
```

**6. Check plugin version:**
```bash
# In SonarQube UI: Administration → Marketplace → Installed
# Should be 1.0.0 or later
# If older, upgrade:
```

```bash
# Download latest from GitHub
# Install and restart SonarQube
```

**7. Check logs for rule execution:**
```bash
tail -f $SONARQUBE_HOME/logs/ce.log | grep -i "rule"
# Should see: "Executing Mathematica rules..."
```

**Prevention:**
- Always use default Quality Profile initially
- Test with known-bad code first
- Keep plugin updated

---

### Issue: Too many false positives

**Symptoms:**
- Hundreds of issues reported
- Many issues are false positives
- Code is actually correct but flagged

**Causes:**
1. Rules too strict for your codebase
2. Rules misunderstanding Mathematica idioms
3. Generated or third-party code analyzed
4. Context-specific patterns not recognized

**Solutions:**

**1. Review and adjust Quality Profile:**
```
Quality Profiles → Your Profile
# For each noisy rule:
# - Deactivate if not useful
# - Change severity (Major → Minor)
# - Or keep but mark as "Won't Fix" in issues
```

**2. Mark false positives:**
```
Issues → Select issue → "..." menu → Change Status
# Options:
# - "False Positive" - Rule is wrong
# - "Won't Fix" - Issue exists but intentional
# - Add comment explaining why
```

**3. Exclude generated/third-party code:**
```properties
sonar.exclusions=**/generated/**,**/external/**,**/vendor/**
```

**4. Configure rule parameters (if available):**
```
Rules → Search for rule → Parameters tab
# Example: Complexity threshold
# Default: 10
# Adjust: 20 (less strict)
```

**5. Use inline suppressions:**
```mathematica
(* NOSONAR - Suppress next line *)
ComplexFunction[...];  (* Not flagged *)

(* Or suppress specific rule: *)
(* sonar.issue.ignore: RuleKey *)
```

**6. Report bugs:**
```
If rule is genuinely wrong:
# GitHub Issues: github.com/bceverly/wolfralyze/issues
# Include:
# - Code sample
# - Why it's a false positive
# - Expected behavior
```

**7. Create custom Quality Profile:**
```
Quality Profiles → Create
# Start with "Sonar way"
# Customize for your project
# Deactivate problematic rules
```

**Prevention:**
- Start with fewer rules, add gradually
- Review rules before enabling
- Use project-specific profiles for legacy code

---

### Issue: Duplication detection showing incorrect results

**Symptoms:**
- Duplication detected in unrelated code
- Or: Obvious duplication not detected
- Duplication percentage seems wrong

**Causes:**
1. Threshold too sensitive (or not sensitive enough)
2. Tokenization not handling Mathematica patterns correctly
3. Comments included in duplication
4. Generated code causing false duplications

**Solutions:**

**1. Adjust duplication thresholds:**
```properties
# More strict (detect smaller duplications)
sonar.cpd.mathematica.minimumTokens=100
sonar.cpd.mathematica.minimumLines=10

# Less strict (fewer false positives)
sonar.cpd.mathematica.minimumTokens=500
sonar.cpd.mathematica.minimumLines=50
```

**2. Exclude files from duplication detection:**
```properties
# Exclude tests (often similar)
sonar.cpd.exclusions=**/tests/**

# Exclude generated code
sonar.cpd.exclusions=**/generated/**,**/auto/**
```

**3. Review specific duplication blocks:**
```
Project → Overview → Click "Duplications %"
# Or: Measures → Duplications → Duplications
# Review each block to confirm it's real duplication
```

**4. Understand tokenization:**
```mathematica
(* These are considered duplicates (tokens match): *)
f[x_] := x^2 + 2*x + 1
g[y_] := y^2 + 2*y + 1

(* Variable names are ignored in tokenization *)
(* Only operators and structure matter *)
```

**5. Exclude boilerplate:**
```mathematica
(* If you have package boilerplate: *)
BeginPackage[...];
Begin[...];
End[];
EndPackage[];

(* This appears in every file - exclude pattern: *)
```

```properties
# Not directly possible to exclude patterns
# But exclude small duplications:
sonar.cpd.mathematica.minimumLines=50
```

**Prevention:**
- Tune thresholds for your codebase
- Exclude test and generated code
- Refactor genuine duplications

---

## Memory Issues

### Issue: "OutOfMemoryError: Java heap space"

**Symptoms:**
```
ERROR: java.lang.OutOfMemoryError: Java heap space
ERROR: Analysis failed
```

**Causes:**
1. Heap size too small for codebase
2. Memory leak in analysis
3. Very large files
4. Too many issues in memory

**Solutions:**

**1. Increase scanner heap:**
```bash
# Linux/Mac
export SONAR_SCANNER_OPTS="-Xmx4096m"
sonar-scanner

# Windows
set SONAR_SCANNER_OPTS=-Xmx4096m
sonar-scanner
```

**2. Increase SonarQube Compute Engine heap:**
```properties
# In $SONARQUBE_HOME/conf/sonar.properties
sonar.ce.javaOpts=-Xmx4096m -Xms1024m
```

**3. Restart SonarQube after changing heap:**
```bash
$SONARQUBE_HOME/bin/[platform]/sonar.sh restart
```

**4. Analyze in smaller batches:**
```bash
# Instead of analyzing entire project:
# Split into modules and analyze separately

# Module 1:
sonar-scanner -Dsonar.sources=src/module1 \
  -Dsonar.projectKey=myproject-module1

# Module 2:
sonar-scanner -Dsonar.sources=src/module2 \
  -Dsonar.projectKey=myproject-module2
```

**5. Exclude large or generated files:**
```properties
sonar.exclusions=**/generated/**,**/LargeFile.wl
```

**6. Check for memory leaks:**
```bash
# Monitor memory during scan
# If memory keeps growing, may be leak
top -p $(pgrep -f sonar-scanner)

# Report to plugin maintainer if consistent
```

**Recommended heap sizes:**

| Codebase Size | Scanner Heap | Compute Engine Heap |
|---------------|-------------|---------------------|
| Small (<100 files) | 1GB | 2GB |
| Medium (100-500 files) | 2GB | 4GB |
| Large (500-2000 files) | 4GB | 6GB |
| Very Large (2000+ files) | 8GB | 8GB+ |

**Prevention:**
- Set adequate heap size from the start
- Monitor memory usage
- Keep files reasonably sized

---

### Issue: "OutOfMemoryError: Metaspace"

**Symptoms:**
```
ERROR: java.lang.OutOfMemoryError: Metaspace
ERROR: Could not load class org.sonar...
```

**Causes:**
1. Too many plugins loaded
2. Metaspace size too small
3. Plugin class loader leak

**Solutions:**

**1. Increase metaspace:**
```properties
# In $SONARQUBE_HOME/conf/sonar.properties
sonar.ce.javaOpts=-Xmx4096m -Xms1024m -XX:MaxMetaspaceSize=512m

# For scanner:
export SONAR_SCANNER_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"
```

**2. Restart SonarQube:**
```bash
$SONARQUBE_HOME/bin/[platform]/sonar.sh restart
```

**3. Remove unnecessary plugins:**
```bash
# List installed plugins
ls $SONARQUBE_HOME/extensions/plugins/

# Remove unused plugins
rm $SONARQUBE_HOME/extensions/plugins/unused-plugin-*.jar

# Restart
```

**4. Check for duplicate plugin JARs:**
```bash
ls $SONARQUBE_HOME/extensions/plugins/ | sort
# Remove any duplicates
```

**Expected metaspace:**
- Default: 256MB (often too small)
- Recommended: 512MB (for multiple plugins)
- Large installation: 1GB

**Prevention:**
- Set adequate metaspace from start
- Remove unused plugins
- Ensure no duplicate plugin versions

---

### Issue: Slow analysis with frequent garbage collection

**Symptoms:**
- Analysis is slow
- Logs show frequent GC:
  ```
  [GC pause (G1 Evacuation Pause) 1024M->512M, 0.5s]
  ```
- CPU usage spikes periodically

**Causes:**
1. Heap size barely sufficient
2. Too many short-lived objects
3. Memory churn

**Solutions:**

**1. Increase heap AND initial size:**
```bash
# Give GC more room to work
export SONAR_SCANNER_OPTS="-Xmx4096m -Xms2048m"
```

**2. Use G1 garbage collector (modern):**
```bash
export SONAR_SCANNER_OPTS="-Xmx4096m -Xms2048m -XX:+UseG1GC"
```

**3. Tune GC (advanced):**
```bash
export SONAR_SCANNER_OPTS="-Xmx4096m -Xms2048m -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=45"
```

**4. Monitor GC behavior:**
```bash
# Add GC logging
export SONAR_SCANNER_OPTS="-Xmx4096m -Xlog:gc*:file=gc.log"
sonar-scanner
# Review gc.log for patterns
```

**Prevention:**
- Set initial heap size (-Xms) to at least 50% of max heap (-Xmx)
- Use modern GC (G1 for Java 11+)
- Ensure adequate total memory (heap + metaspace + OS)

---

## Authentication Issues

### Issue: "Authentication failed" or 401 errors

**Symptoms:**
```
ERROR: Error during SonarQube Scanner execution
ERROR: Not authorized. Please check the user token or the credentials
ERROR: 401 Unauthorized
```

**Causes:**
1. Missing or incorrect token
2. Token expired or revoked
3. Incorrect username/password
4. Wrong SonarQube URL
5. Network/proxy issues

**Solutions:**

**1. Generate new token:**
```
1. Login to SonarQube
2. User menu → My Account → Security
3. Generate Token:
   - Name: "CI Scanner"
   - Type: User Token
   - Expiration: 90 days (or never)
4. Copy token immediately (shown only once!)
```

**2. Use token in scanner:**
```bash
# Method 1: Command line
sonar-scanner -Dsonar.login=YOUR_TOKEN_HERE

# Method 2: sonar-project.properties
sonar.login=YOUR_TOKEN_HERE

# Method 3: Environment variable (more secure)
export SONAR_TOKEN=YOUR_TOKEN_HERE
sonar-scanner
```

**3. If using username/password (legacy):**
```bash
sonar-scanner \
  -Dsonar.login=admin \
  -Dsonar.password=admin123
# Not recommended - use tokens instead!
```

**4. Verify SonarQube URL:**
```bash
# Test connection
curl http://localhost:9000/api/system/status

# If using HTTPS:
curl https://sonarqube.company.com/api/system/status

# Update in configuration:
sonar.host.url=http://localhost:9000
```

**5. Check proxy settings:**
```bash
# If behind proxy:
export HTTP_PROXY=http://proxy.company.com:8080
export HTTPS_PROXY=http://proxy.company.com:8080
export NO_PROXY=localhost,127.0.0.1

sonar-scanner
```

**6. Test token validity:**
```bash
# Using curl
curl -u YOUR_TOKEN: http://localhost:9000/api/authentication/validate

# Should return: {"valid":true}
# If false, token is invalid or expired
```

**Prevention:**
- Use tokens, not passwords
- Set long expiration (1 year) or no expiration
- Store tokens securely (environment variables, secrets management)
- Document token renewal process

---

### Issue: "Insufficient privileges" or 403 errors

**Symptoms:**
```
ERROR: You're not authorized to run analysis
ERROR: 403 Forbidden
ERROR: Insufficient privileges
```

**Causes:**
1. User doesn't have "Execute Analysis" permission
2. Project permissions not set correctly
3. Using wrong user account
4. Global permission issue

**Solutions:**

**1. Grant analysis permission (global):**
```
As administrator:
1. Administration → Security → Global Permissions
2. Find "Execute Analysis" permission
3. Grant to appropriate group (e.g., "sonar-users")
4. Or grant directly to user
```

**2. Grant project-specific permission:**
```
As project administrator:
1. Project → Administration → Permissions
2. Find "Execute Analysis" permission
3. Grant to user or group
```

**3. Check user is in correct group:**
```
Administration → Security → Users
# Find your user
# Check groups membership
# Add to "sonar-users" or analysis group if needed
```

**4. Use admin token temporarily (to debug):**
```
# Generate token from admin account
# Run scan with admin token
# If works: Permission issue confirmed
# If fails: Different issue
```

**5. Verify authentication:**
```bash
# Check who you're authenticated as
curl -u YOUR_TOKEN: http://localhost:9000/api/authentication/validate
```

**Required permissions for analysis:**
- **Execute Analysis** (minimum)
- **Browse** (to see project)

**Prevention:**
- Set up service account for CI/CD with correct permissions
- Use group-based permissions (not individual users)
- Document permission requirements

---

## File Detection Issues

### Issue: Files with correct extension not analyzed

**Symptoms:**
- Files exist with `.wl`, `.m`, or `.wls` extensions
- Scanner runs successfully
- But specific files not analyzed (missing from file list)

**Causes:**
1. Files excluded by exclusion patterns
2. Files in directories not specified in `sonar.sources`
3. Files unreadable (permissions)
4. Symbolic links not followed
5. Files ignored by SCM (.gitignore)

**Solutions:**

**1. Check exclusion patterns:**
```properties
# Review sonar-project.properties
sonar.exclusions=**/tests/**  # Are your files in tests/?

# Temporarily disable exclusions to debug:
# sonar.exclusions=
```

**2. Verify file is in source path:**
```properties
# If file is in: src/subdir/file.wl
# Ensure sonar.sources includes it:
sonar.sources=src

# Not:
sonar.sources=src/otherdir  # Wrong!
```

**3. Check file permissions:**
```bash
ls -la path/to/file.wl
# Should be readable: -rw-r--r--

# If not:
chmod 644 path/to/file.wl
```

**4. Follow symbolic links:**
```properties
# SonarQube doesn't follow symlinks by default
# Options:
# A. Copy files instead of symlinking
# B. Use real path in sonar.sources
```

**5. Check .gitignore:**
```bash
# If file is gitignored, SonarQube may skip it
# Check: .gitignore, .sonarignore

# Override:
# Remove from .gitignore or add to sonar.sources explicitly
```

**6. Use verbose mode to debug:**
```bash
sonar-scanner -X 2>&1 | grep "file.wl"
# Should show why file was included/excluded
```

**7. Verify file encoding:**
```bash
file -I path/to/file.wl
# Should be: text/plain; charset=utf-8
# Binary files are skipped
```

**Prevention:**
- Keep source structure simple
- Avoid symlinks
- Use inclusive sonar.sources patterns

---

### Issue: Binary or data files incorrectly analyzed

**Symptoms:**
- `.mx` (compiled) files being analyzed
- Data files (`.json`, `.csv`) treated as code
- Analysis errors on binary files

**Causes:**
1. File extensions overlap
2. Missing exclusion patterns
3. File type misdetection

**Solutions:**

**1. Exclude binary and data files:**
```properties
sonar.exclusions=**/*.mx,**/*.json,**/*.csv,**/*.dat
```

**2. Be explicit about source files:**
```properties
# Instead of analyzing entire directory:
sonar.sources=.

# Be specific:
sonar.sources=src
sonar.inclusions=**/*.wl,**/*.m,**/*.wls
```

**3. Separate data from code:**
```
project/
  src/        ← Code (analyze this)
  data/       ← Data (exclude this)
  build/      ← Generated (exclude this)
```

```properties
sonar.sources=src
sonar.exclusions=data/**,build/**
```

**4. Check file extensions:**
```properties
# Ensure only source extensions:
sonar.mathematica.file.suffixes=.m,.wl,.wls
# Not: .mx, .dat, etc.
```

**Prevention:**
- Keep code and data separate
- Explicit inclusion patterns
- Document exclusions in configuration

---

## Rule and Issue Issues

### Issue: Specific rule not detecting expected issues

**Symptoms:**
- Rule is activated in Quality Profile
- Code clearly violates rule
- But no issue reported

**Causes:**
1. Rule pattern doesn't match your code style
2. Rule has bugs or limitations
3. Code context prevents detection
4. Rule parameters need adjustment

**Solutions:**

**1. Verify rule is active:**
```
Quality Profiles → Your Profile → Search for rule
# Status should be "Active"
# If inactive: Click "Activate"
```

**2. Review rule description and examples:**
```
Rules → Search for rule → Click rule name
# Read "Description" tab
# Check "Noncompliant Code Example"
# Compare with your code
```

**3. Test with exact example from docs:**
```mathematica
(* Copy noncompliant example from rule docs *)
(* Create test file: test-rule.wl *)
(* Analyze: sonar-scanner -Dsonar.sources=test-rule.wl *)
(* Check if issue is reported *)
```

**4. Check rule parameters:**
```
Quality Profiles → Your Profile → Search rule → Parameters tab
# Example: Complexity threshold
# May need adjustment for your code
```

**5. Report bug if rule is wrong:**
```
GitHub Issues: github.com/bceverly/wolfralyze/issues
# Title: "Rule XYZ not detecting ..."
# Include:
# - Code sample
# - Expected behavior
# - Actual behavior
# - Plugin version
```

**6. Check logs for rule execution:**
```bash
tail -f $SONARQUBE_HOME/logs/ce.log | grep -i "rule"
# Verify rule is running
```

**7. Verify project language:**
```
Project → Administration → General Settings
# Ensure language is "Mathematica"
```

**Prevention:**
- Test new rules with known examples
- Report bugs to maintainer
- Use multiple complementary rules

---

### Issue: Can't mark issue as false positive

**Symptoms:**
- Click on issue
- "..." menu doesn't show "Mark as False Positive"
- Or: Option is grayed out

**Causes:**
1. Insufficient permissions
2. Issue already resolved
3. Quality Gate locked issue status
4. Browser extension interfering

**Solutions:**

**1. Check permissions:**
```
Project → Administration → Permissions
# User needs: "Administer Issues" or "Browse" + "Issue" permissions
```

**2. Verify issue is "Open":**
```
# Can only change status if issue is:
# - Open (can change)
# - Reopened (can change)
# - Closed (cannot change - reopen first)
# - Resolved (cannot change - reopen first)
```

**3. Use issue workflow:**
```
Issue → "..." menu → Change Status
# Options:
# - Confirm (mark as legitimate issue)
# - Resolve as Fixed (code was fixed)
# - Resolve as False Positive
# - Resolve as Won't Fix
```

**4. Try different browser:**
```
# Some browser extensions interfere
# Try:
# - Incognito/private mode
# - Different browser
# - Disable ad blockers
```

**5. Check Quality Gate:**
```
# Some organizations lock issues via Quality Gate
# Contact SonarQube admin
```

**Prevention:**
- Ensure users have correct permissions
- Document issue workflow for team

---

## Integration Issues

### Issue: SonarLint not connecting to SonarQube

**Symptoms:**
- SonarLint plugin installed
- Can't connect to SonarQube server
- Error: "Unable to connect" or timeout

**Causes:**
1. Wrong server URL
2. Network/firewall blocking connection
3. Certificate issues (HTTPS)
4. Token authentication not configured
5. SonarQube server not running

**Solutions:**

**1. Verify SonarQube URL:**
```
# Test in browser first
http://localhost:9000
# Or
https://sonarqube.company.com

# Should see SonarQube login page
```

**2. Configure SonarLint connection:**
```
# IntelliJ IDEA:
Preferences → Tools → SonarLint → Settings
# Add connection:
# - Server URL: http://localhost:9000
# - Authentication: Token
# - Generate token in SonarQube: User → My Account → Security
# - Paste token in SonarLint
```

**3. Check network connectivity:**
```bash
# From machine running IDE:
curl http://localhost:9000/api/system/status

# Should return JSON with status: "UP"
# If fails: Network/firewall issue
```

**4. Handle HTTPS certificate issues:**
```
# If using self-signed certificate:
# Option A: Add certificate to Java trust store
keytool -import -trustcacerts -file sonarqube.crt \
  -alias sonarqube -keystore $JAVA_HOME/lib/security/cacerts

# Option B: Disable certificate validation (not recommended)
# In SonarLint settings: Check "Disable certificate validation"
```

**5. Check proxy settings:**
```
# If behind corporate proxy:
IDE → Preferences → Appearance & Behavior → System Settings → HTTP Proxy
# Configure proxy settings
# SonarLint uses IDE proxy settings
```

**6. Update SonarLint version:**
```
# Ensure SonarLint 7.0+ for Connected Mode
Preferences → Plugins → Installed → SonarLint → Check for updates
```

**7. Check SonarQube server status:**
```bash
# Verify server is running
curl http://localhost:9000/api/system/status
# Or check logs:
tail -f $SONARQUBE_HOME/logs/sonar.log
```

**Prevention:**
- Document connection setup for team
- Use tokens, not passwords
- Test connection before developing

---

### Issue: Quick Fixes not appearing in IDE

**Symptoms:**
- SonarLint connected to SonarQube
- Issues appear in IDE
- But no "Quick Fix" button

**Causes:**
1. SonarLint version too old (<7.0)
2. Plugin doesn't support Quick Fixes for that rule
3. Connected Mode not enabled
4. IDE plugin not updated

**Solutions:**

**1. Update SonarLint:**
```
Preferences → Plugins → Marketplace
# Search "SonarLint"
# Update to 7.0+
# Restart IDE
```

**2. Enable Connected Mode:**
```
# IntelliJ IDEA:
Preferences → Tools → SonarLint → Settings
# Ensure connection is active
# Bind project to SonarQube project
```

**3. Check which rules support Quick Fixes:**
```
# In SonarQube:
Rules → Filter: Language = Mathematica
# Look for rules with "Quick Fix Available" tag
# Currently: 53 rules support Quick Fixes
```

**4. Verify project binding:**
```
# Right-click project → SonarLint → Bind to SonarQube
# Select your server
# Choose project
# Click "Bind"
```

**5. Update project analysis:**
```
# In SonarLint tool window:
# Click "Update Binding" or "Sync with SonarQube"
```

**6. Check IDE compatibility:**
```
# Quick Fixes supported in:
# - IntelliJ IDEA 2020.3+
# - Visual Studio Code (with SonarLint extension)
# - Eclipse 2021+
# - Visual Studio 2019+
```

**Prevention:**
- Keep SonarLint updated
- Enable auto-updates for plugins
- Use Connected Mode from start

---

## Quality Gate Issues

### Issue: Quality Gate failing but don't know why

**Symptoms:**
- Quality Gate status: "Failed"
- Build fails in CI/CD
- Not clear which condition failed

**Causes:**
1. Multiple conditions, unclear which failed
2. Thresholds too strict
3. New issues introduced
4. Coverage/duplication thresholds not met

**Solutions:**

**1. Check Quality Gate details in UI:**
```
Project → Overview → Quality Gate section
# Shows failed conditions in red:
# Example: "Bugs: 5 (threshold: 0)"
```

**2. View specific failed conditions:**
```
Project → Overview → Click "Failed" status
# Shows detailed breakdown:
# - Condition name
# - Actual value
# - Threshold value
# - Difference
```

**3. Check scanner logs:**
```bash
# If running in CI/CD:
# Look for quality gate check output:
sonar-scanner
# ...
# QUALITY GATE STATUS: FAILED
# Conditions:
# - Bugs: 5 > 0 (FAILED)
# - Vulnerabilities: 0 ≤ 0 (PASSED)
```

**4. Adjust Quality Gate conditions:**
```
Quality Gates → Your Gate → Conditions
# Review each condition:
# - On Overall Code / New Code
# - Metric (Bugs, Vulnerabilities, etc.)
# - Operator (≤, ≥, =)
# - Threshold value

# Adjust if too strict:
# Example: Bugs ≤ 0 → Bugs ≤ 5 (allow 5 bugs)
```

**5. Focus on "New Code" conditions:**
```
# Best practice: Fail only on new issues
# Not: "Overall Code" → "Bugs ≤ 0"
# But: "New Code" → "New Bugs ≤ 0"

# This allows fixing legacy issues gradually
```

**6. Use "Leak Period" wisely:**
```
Project → Administration → General Settings → New Code
# Options:
# - Previous version (recommended)
# - Number of days (e.g., 30 days)
# - Specific analysis
# - Reference branch
```

**Default Quality Gate conditions:**
- **On New Code:**
  - Bugs ≤ 0
  - Vulnerabilities ≤ 0
  - Security Hotspots Reviewed = 100%
  - Code Coverage ≥ 80% (if coverage enabled)
  - Duplications ≤ 3%

**Prevention:**
- Set realistic thresholds
- Focus on new code quality
- Review Quality Gate regularly

---

### Issue: Quality Gate always passing (too lenient)

**Symptoms:**
- Code has obvious quality issues
- But Quality Gate shows "Passed"
- No failures in CI/CD

**Causes:**
1. Quality Gate has no conditions
2. Conditions too lenient
3. Wrong Quality Gate assigned to project
4. Conditions apply to wrong metric period

**Solutions:**

**1. Check Quality Gate has conditions:**
```
Quality Gates → Your Gate
# Should have multiple conditions
# If zero conditions: All projects pass!
```

**2. Verify correct Quality Gate assigned:**
```
Project → Administration → Quality Gate
# Should be: "Sonar way" or your custom gate
# Not: "None" or empty
```

**3. Review condition thresholds:**
```
Quality Gates → Your Gate → Conditions
# Example too-lenient conditions:
# - Bugs ≤ 1000 (way too high!)
# - Code Smells ≤ 10000 (way too high!)

# Recommended:
# - Bugs ≤ 0 (on new code)
# - Vulnerabilities ≤ 0 (on new code)
# - Coverage ≥ 80% (on new code)
```

**4. Check "On New Code" vs "On Overall Code":**
```
# If conditions are on "Overall Code":
# Legacy issues may be grandfathered in
# Change to "On New Code" to focus on new issues
```

**5. Add missing conditions:**
```
Quality Gates → Your Gate → Add Condition
# Recommended conditions:
# - Security Rating: A (on Overall Code)
# - Reliability Rating: A (on Overall Code)
# - Maintainability Rating: A (on New Code)
# - Coverage: ≥ 80% (on New Code)
# - Duplications: ≤ 3% (on New Code)
```

**6. Set as default for all projects:**
```
Quality Gates → Your Gate → Set as Default
# Ensures all new projects use strict gate
```

**Prevention:**
- Start with "Sonar way" Quality Gate (recommended)
- Review and tighten conditions quarterly
- Monitor Quality Gate pass/fail rates

---

## Summary

This troubleshooting guide covers the most common issues when using the SonarQube Mathematica Plugin. For issues not covered here:

1. **Check documentation:**
   - README.md - General usage
   - FAQ.md - Frequently asked questions
   - ARCHITECTURE.md - Technical details

2. **Search existing issues:**
   - GitHub Issues: github.com/bceverly/wolfralyze/issues

3. **Ask the community:**
   - GitHub Discussions: github.com/bceverly/wolfralyze/discussions
   - SonarQube Community: community.sonarsource.com (tag: mathematica)

4. **Report bugs:**
   - GitHub Issues with:
     - Plugin version
     - SonarQube version
     - Steps to reproduce
     - Expected vs actual behavior
     - Log excerpts (if applicable)

5. **Contribute fixes:**
   - Pull requests welcome!
   - See CONTRIBUTING.md (coming soon)

**Emergency contacts:**
- Security issues: Email maintainer directly (see README)
- Critical bugs: GitHub Issues with "critical" label

**Keep plugin updated:**
```bash
# Check for updates:
# Administration → Marketplace → Updates Available
# Or: GitHub Releases
```

**Version compatibility:**
- **Plugin 1.0.0+** requires **SonarQube 9.9+**
- **SonarLint 7.0+** required for Quick Fixes
- **Java 11+** required for all components

Thank you for using the SonarQube Mathematica Plugin!
