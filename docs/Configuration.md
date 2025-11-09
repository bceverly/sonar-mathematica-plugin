# Configuration Guide

This guide covers all configuration options for the SonarQube Mathematica Plugin, from basic setup to advanced multi-module projects.

## Table of Contents

- [Basic Configuration](#basic-configuration)
- [File Inclusion/Exclusion Patterns](#file-inclusionexclusion-patterns)
- [Quality Profile Activation](#quality-profile-activation)
- [Rule Customization](#rule-customization)
- [Performance Tuning](#performance-tuning-for-large-projects)
- [Multi-Module Project Setup](#multi-module-project-setup)
- [Scanner Properties Reference](#scanner-properties-reference)
- [Complete Working Examples](#complete-working-examples)

## Basic Configuration

The primary configuration file is `sonar-project.properties`, which must be in the root directory of your project.

### Minimal Configuration

Create a file named `sonar-project.properties` in your project root:

```properties
# Project identification (required)
sonar.projectKey=my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.projectVersion=1.0

# Source code location (required)
sonar.sources=.

# File encoding (recommended)
sonar.sourceEncoding=UTF-8
```

### Standard Configuration

A more complete configuration includes additional settings:

```properties
# ============================================
# Project Identification
# ============================================
sonar.projectKey=my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.projectVersion=1.0.0

# Optional: Project description
sonar.projectDescription=Mathematica code for scientific computing

# ============================================
# Source Code
# ============================================
# Source directories (comma-separated)
sonar.sources=src,lib

# Test directories (optional, comma-separated)
sonar.tests=tests

# ============================================
# Encoding
# ============================================
sonar.sourceEncoding=UTF-8

# ============================================
# Mathematica-Specific Settings
# ============================================
# File extensions to analyze (default: .m,.wl,.wls)
sonar.mathematica.file.suffixes=.m,.wl,.wls

# ============================================
# Code Duplication Detection (CPD)
# ============================================
# Minimum tokens to consider as duplication (default: 100)
sonar.cpd.mathematica.minimumTokens=50

# Minimum lines to consider as duplication (default: 10)
sonar.cpd.mathematica.minimumLines=5
```

### Running the Analysis

After creating `sonar-project.properties`:

```bash
# From project root directory
sonar-scanner

# Or specify SonarQube server URL
sonar-scanner -Dsonar.host.url=http://localhost:9000

# With authentication token
sonar-scanner \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=your-authentication-token
```

## File Inclusion/Exclusion Patterns

Control which files are analyzed using inclusion and exclusion patterns.

### Exclusion Patterns

```properties
# ============================================
# File Exclusions
# ============================================

# Exclude specific files
sonar.exclusions=**/Experiment.m,**/OldCode.m

# Exclude directories
sonar.exclusions=**/archive/**,**/deprecated/**

# Exclude generated code
sonar.exclusions=**/generated/**,**/build/**

# Exclude test data
sonar.exclusions=**/test-data/**,**/fixtures/**

# Multiple patterns (comma-separated)
sonar.exclusions=\
  **/archive/**,\
  **/deprecated/**,\
  **/generated/**,\
  **/build/**,\
  **/Experiment.m,\
  **/LargeDataFile.m
```

### Inclusion Patterns

```properties
# Only analyze specific patterns (overrides default)
sonar.inclusions=**/*.m,**/*.wl

# Only analyze specific directories
sonar.inclusions=src/**/*.m,lib/**/*.wl
```

### Test File Exclusions

```properties
# Exclude test files from main analysis
sonar.test.inclusions=**/test/**,**/tests/**,**/*Test.m

# Exclude test files from duplication detection
sonar.cpd.exclusions=**/test/**,**/*Test.m
```

### Coverage Exclusions

```properties
# Exclude files from coverage calculation
sonar.coverage.exclusions=**/test/**,**/generated/**
```

### Pattern Syntax

The plugin uses Ant-style glob patterns:

| Pattern | Description | Example |
|---------|-------------|---------|
| `*` | Matches any characters except `/` | `*.m` matches `foo.m` but not `dir/foo.m` |
| `**` | Matches any characters including `/` | `**/*.m` matches `foo.m` and `dir/foo.m` |
| `?` | Matches single character | `test?.m` matches `test1.m` and `testA.m` |
| `[abc]` | Matches any character in set | `test[123].m` matches `test1.m`, `test2.m` |

**Examples:**

```properties
# All .m files recursively
**/*.m

# Files in specific directory (not recursive)
src/*.m

# Files in specific directory (recursive)
src/**/*.m

# Multiple extensions
**/*.{m,wl,wls}

# Exclude all test directories anywhere
**/test/**

# Exclude specific file names anywhere
**/config.m,**/settings.m
```

## Quality Profile Activation

Quality Profiles define which rules are active and their severity levels.

### Using the Default Profile

The plugin comes with a **"Sonar way"** profile with 464 rules activated by default.

**To verify:**
1. Go to **Quality Profiles** in SonarQube
2. Find **"Sonar way"** for **Mathematica** language
3. Set it as default if not already

### Creating a Custom Profile

1. Go to **Quality Profiles**
2. Click **"Create"**
3. Enter profile details:
   - **Name**: "My Team Profile"
   - **Language**: Mathematica
   - **Parent**: Sonar way (optional)
4. Click **"Create"**

### Activating/Deactivating Rules

**In the UI:**
1. Open your Quality Profile
2. Click **"Activate More"** to add rules
3. Click on a rule and select **"Deactivate"** to remove it
4. Use filters to find specific rules:
   - By **Type**: Bug, Vulnerability, Code Smell, Security Hotspot
   - By **Severity**: Blocker, Critical, Major, Minor, Info
   - By **Tag**: performance, security, documentation, etc.

**Rule Categories:**

| Category | Rule Count | Description |
|----------|-----------|-------------|
| Code Smells | 76 | Maintainability issues |
| Security Vulnerabilities | 21 | Security flaws |
| Bugs (Reliability) | 45 | Potential runtime errors |
| Security Hotspots | 7 | Security-sensitive code requiring review |
| Performance | 26 | Performance optimization opportunities |
| OWASP Top 10 | Subset | Coverage of OWASP security risks |

### Specifying Profile in Analysis

```bash
# Use specific profile
sonar-scanner -Dsonar.profile="My Team Profile"

# Or in sonar-project.properties
sonar.profile=My Team Profile
```

## Rule Customization

### Changing Rule Severity

1. Open your Quality Profile
2. Find the rule using search
3. Click on the rule
4. Select **"Change Severity"**
5. Choose: Blocker, Critical, Major, Minor, or Info

**Example: Make "Division by Zero" a Blocker:**
1. Search for "DivisionByZero"
2. Change severity from "Major" to "Blocker"

### Rule Parameters

Some rules accept parameters to customize their behavior.

**Example: Copy-Paste Detection**

```properties
# Minimum tokens for duplication (default: 100)
sonar.cpd.mathematica.minimumTokens=50

# Minimum lines for duplication (default: 10)
sonar.cpd.mathematica.minimumLines=5
```

**Note**: Currently, most rules do not have configurable parameters. This may be expanded in future versions.

### Bulk Rule Changes

**Export/Import Quality Profile:**

1. **Export** your profile:
   - Open Quality Profile → Click **"Back up"**
   - Download XML file

2. **Modify** the XML file to change rules

3. **Import** modified profile:
   - Quality Profiles → Click **"Restore"**
   - Upload modified XML

**Example XML snippet:**

```xml
<profile>
  <name>Custom Mathematica Profile</name>
  <language>mathematica</language>
  <rules>
    <rule>
      <repositoryKey>mathematica</repositoryKey>
      <key>DivisionByZero</key>
      <priority>BLOCKER</priority>
    </rule>
    <!-- More rules... -->
  </rules>
</profile>
```

## Performance Tuning for Large Projects

The plugin includes built-in safeguards for large codebases, but you can optimize performance further.

### File Size Limits

**Built-in Limits:**
- Maximum file size: **25,000 lines** (files larger are skipped)
- Symbol table analysis timeout: **30 seconds per file**

**When limits are triggered:**
```
WARN: SKIP: File Experiment.m has 31719 lines (exceeds limit of 25000)
WARN: TIMEOUT: SymbolTable analysis for Export.m exceeded 30 seconds
```

**Recommendation**: Exclude these files explicitly:

```properties
sonar.exclusions=**/Experiment.m,**/Export.m
```

### Excluding Large or Generated Files

```properties
# ============================================
# Performance Optimizations
# ============================================

# Exclude very large files (> 10,000 lines)
sonar.exclusions=\
  **/LargeDataProcessing.m,\
  **/ExperimentalResults.m

# Exclude generated code
sonar.exclusions=**/generated/**,**/build/**

# Exclude vendored/third-party code
sonar.exclusions=**/vendor/**,**/external/**

# Skip duplication detection on large files
sonar.cpd.exclusions=\
  **/LargeDataProcessing.m,\
  **/ExperimentalResults.m
```

### Incremental Analysis

For faster re-analysis, use incremental mode:

```properties
# Only analyze changed files (SonarQube 9.7+)
sonar.scm.provider=git

# This enables automatic detection of changed files
```

### Parallel Processing

SonarQube automatically parallelizes analysis. To control:

```bash
# Increase analysis threads (default: number of CPU cores)
sonar-scanner -Dsonar.technicalDebt.hoursInDay=8
```

### Memory Configuration

For very large projects:

```bash
# Increase scanner memory
export SONAR_SCANNER_OPTS="-Xmx2G"
sonar-scanner
```

### Analysis Performance Characteristics

Based on real-world data (12,500+ files):

| File Size | Analysis Time | Recommendation |
|-----------|---------------|----------------|
| < 1,000 lines | 0.1 - 2 sec | Analyze normally |
| 1,000 - 5,000 | 2 - 10 sec | Analyze normally |
| 5,000 - 15,000 | 10 - 30 sec | Monitor performance |
| 15,000 - 25,000 | 30 - 60 sec | Consider exclusion |
| > 25,000 | Auto-skipped | Excluded automatically |

## Multi-Module Project Setup

For projects with multiple sub-projects or modules:

### Basic Multi-Module Setup

**Root `sonar-project.properties`:**

```properties
# ============================================
# Multi-Module Project Configuration
# ============================================

# Root project identification
sonar.projectKey=my-multimodule-project
sonar.projectName=My Multi-Module Mathematica Project
sonar.projectVersion=1.0.0

# Define modules (comma-separated directory names)
sonar.modules=core-lib,analysis-tools,visualization

# Module 1: Core Library
core-lib.sonar.projectName=Core Library
core-lib.sonar.sources=core-lib/src
core-lib.sonar.tests=core-lib/tests

# Module 2: Analysis Tools
analysis-tools.sonar.projectName=Analysis Tools
analysis-tools.sonar.sources=analysis-tools/src
analysis-tools.sonar.tests=analysis-tools/tests

# Module 3: Visualization
visualization.sonar.projectName=Visualization
visualization.sonar.sources=visualization/src
visualization.sonar.tests=visualization/tests

# Global settings (apply to all modules)
sonar.sourceEncoding=UTF-8
sonar.mathematica.file.suffixes=.m,.wl,.wls
```

### Module-Specific Configuration Files

**Alternative approach:** Use separate `sonar-project.properties` files per module:

**Root directory structure:**
```
my-project/
├── sonar-project.properties (root config)
├── core-lib/
│   └── sonar-project.properties
├── analysis-tools/
│   └── sonar-project.properties
└── visualization/
    └── sonar-project.properties
```

**Root `sonar-project.properties`:**

```properties
sonar.projectKey=my-multimodule-project
sonar.projectName=My Multi-Module Project
sonar.projectVersion=1.0.0

# Modules will use their own sonar-project.properties
sonar.modules=core-lib,analysis-tools,visualization
```

**Module `core-lib/sonar-project.properties`:**

```properties
sonar.projectKey=my-multimodule-project:core-lib
sonar.projectName=Core Library
sonar.sources=src
sonar.tests=tests
sonar.sourceEncoding=UTF-8
```

### Aggregated Analysis

Run analysis from root directory:

```bash
cd my-project/
sonar-scanner
```

**Result**: All modules are analyzed together, with aggregated metrics visible in SonarQube.

### Independent Module Analysis

To analyze modules independently:

```bash
# Analyze only one module
cd my-project/core-lib
sonar-scanner

# This creates a separate project in SonarQube
```

## Scanner Properties Reference

### Project Identification

| Property | Required | Description | Example |
|----------|----------|-------------|---------|
| `sonar.projectKey` | Yes | Unique project identifier | `my-mathematica-project` |
| `sonar.projectName` | Yes | Human-readable project name | `My Mathematica Project` |
| `sonar.projectVersion` | No | Project version | `1.0.0` |
| `sonar.projectDescription` | No | Project description | `Scientific computing library` |

### Source Configuration

| Property | Required | Description | Example |
|----------|----------|-------------|---------|
| `sonar.sources` | Yes | Source code directories | `.` or `src,lib` |
| `sonar.tests` | No | Test directories | `tests,test` |
| `sonar.sourceEncoding` | No | File encoding (default: platform) | `UTF-8` |

### Mathematica-Specific

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `sonar.mathematica.file.suffixes` | `.m,.wl,.wls` | File extensions to analyze | `.m,.wl,.wls,.mt` |

### Duplication Detection (CPD)

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `sonar.cpd.mathematica.minimumTokens` | 100 | Min tokens for duplication | `50` |
| `sonar.cpd.mathematica.minimumLines` | 10 | Min lines for duplication | `5` |
| `sonar.cpd.exclusions` | (none) | Files to exclude from CPD | `**/test/**` |

### File Filters

| Property | Description | Example |
|----------|-------------|---------|
| `sonar.inclusions` | Only analyze matching files | `**/*.m,**/*.wl` |
| `sonar.exclusions` | Exclude matching files | `**/test/**,**/generated/**` |
| `sonar.test.inclusions` | Test file patterns | `**/test/**,**/*Test.m` |
| `sonar.test.exclusions` | Exclude from tests | `**/integration/**` |
| `sonar.coverage.exclusions` | Exclude from coverage | `**/test/**` |

### Quality Profile

| Property | Description | Example |
|----------|-------------|---------|
| `sonar.profile` | Quality profile to use | `My Custom Profile` |

### Multi-Module

| Property | Description | Example |
|----------|-------------|---------|
| `sonar.modules` | Comma-separated module list | `core,utils,viz` |
| `<module>.sonar.projectName` | Module-specific name | `core.sonar.projectName=Core Library` |
| `<module>.sonar.sources` | Module-specific sources | `core.sonar.sources=core/src` |

### Server Connection

| Property | Description | Example |
|----------|-------------|---------|
| `sonar.host.url` | SonarQube server URL | `http://localhost:9000` |
| `sonar.token` | Authentication token | `sqp_abc123...` |
| `sonar.login` | Username (deprecated, use token) | `admin` |
| `sonar.password` | Password (deprecated, use token) | `admin` |

### SCM (Source Control Management)

| Property | Description | Example |
|----------|-------------|---------|
| `sonar.scm.provider` | SCM system | `git` |
| `sonar.scm.disabled` | Disable SCM detection | `false` |

### Analysis Parameters

| Property | Description | Example |
|----------|-------------|---------|
| `sonar.verbose` | Verbose logging | `true` |
| `sonar.showProfiling` | Show performance profiling | `true` |
| `sonar.working.directory` | Working directory for temp files | `.scannerwork` |

## Complete Working Examples

### Example 1: Simple Single-File Project

**Directory structure:**
```
my-simple-project/
├── sample.m
└── sonar-project.properties
```

**`sonar-project.properties`:**

```properties
# Simple Mathematica project
sonar.projectKey=simple-mathematica
sonar.projectName=Simple Mathematica Project
sonar.projectVersion=1.0

# Analyze current directory
sonar.sources=.

# Encoding
sonar.sourceEncoding=UTF-8

# Mathematica file extensions
sonar.mathematica.file.suffixes=.m,.wl,.wls
```

**Run analysis:**

```bash
cd my-simple-project
sonar-scanner
```

### Example 2: Paclet Project

A typical Wolfram Language Paclet with organized structure.

**Directory structure:**
```
MyPaclet/
├── Kernel/
│   ├── Init.m
│   └── MyPaclet.m
├── Tests/
│   ├── TestSuite.m
│   └── UnitTests.m
├── Documentation/
│   └── English/
│       └── Guides/
├── PacletInfo.m
└── sonar-project.properties
```

**`sonar-project.properties`:**

```properties
# ============================================
# Paclet Project Configuration
# ============================================

# Project identification
sonar.projectKey=my-paclet
sonar.projectName=MyPaclet
sonar.projectVersion=1.2.0

# Source code in Kernel directory
sonar.sources=Kernel

# Tests in Tests directory
sonar.tests=Tests

# Exclude documentation and generated files
sonar.exclusions=\
  Documentation/**,\
  **/Generated/**

# File encoding
sonar.sourceEncoding=UTF-8

# Mathematica extensions
sonar.mathematica.file.suffixes=.m,.wl,.wls

# Duplication detection settings
sonar.cpd.mathematica.minimumTokens=50
sonar.cpd.mathematica.minimumLines=5

# Exclude tests from duplication detection
sonar.cpd.exclusions=Tests/**
```

**Run analysis:**

```bash
cd MyPaclet
sonar-scanner -Dsonar.host.url=http://localhost:9000
```

### Example 3: Multi-Package Project

A large project with multiple packages and modules.

**Directory structure:**
```
mathematica-suite/
├── packages/
│   ├── DataProcessing/
│   │   ├── src/
│   │   └── tests/
│   ├── Visualization/
│   │   ├── src/
│   │   └── tests/
│   └── Utilities/
│       ├── src/
│       └── tests/
├── scripts/
├── data/
├── docs/
└── sonar-project.properties
```

**`sonar-project.properties`:**

```properties
# ============================================
# Multi-Package Mathematica Suite
# ============================================

# Root project
sonar.projectKey=mathematica-suite
sonar.projectName=Mathematica Computing Suite
sonar.projectVersion=2.0.0
sonar.projectDescription=Suite of Mathematica packages for data processing and visualization

# ============================================
# Multi-Module Configuration
# ============================================

# Define modules
sonar.modules=data-processing,visualization,utilities

# Module 1: Data Processing
data-processing.sonar.projectKey=mathematica-suite:data-processing
data-processing.sonar.projectName=Data Processing Package
data-processing.sonar.sources=packages/DataProcessing/src
data-processing.sonar.tests=packages/DataProcessing/tests

# Module 2: Visualization
visualization.sonar.projectKey=mathematica-suite:visualization
visualization.sonar.projectName=Visualization Package
visualization.sonar.sources=packages/Visualization/src
visualization.sonar.tests=packages/Visualization/tests

# Module 3: Utilities
utilities.sonar.projectKey=mathematica-suite:utilities
utilities.sonar.projectName=Utilities Package
utilities.sonar.sources=packages/Utilities/src
utilities.sonar.tests=packages/Utilities/tests

# ============================================
# Global Settings (Apply to All Modules)
# ============================================

# Encoding
sonar.sourceEncoding=UTF-8

# File extensions
sonar.mathematica.file.suffixes=.m,.wl,.wls

# Global exclusions
sonar.exclusions=\
  **/archive/**,\
  **/deprecated/**,\
  data/**,\
  docs/**,\
  scripts/**

# Duplication settings
sonar.cpd.mathematica.minimumTokens=50
sonar.cpd.mathematica.minimumLines=5
sonar.cpd.exclusions=**/tests/**
```

**Run analysis:**

```bash
cd mathematica-suite
sonar-scanner
```

**Result in SonarQube:**
- One parent project: "Mathematica Computing Suite"
- Three sub-projects visible in dashboard
- Aggregated metrics for entire suite

### Example 4: Notebook Export Workflow

For projects using Mathematica notebooks (`.nb` files) that are exported to `.m` files.

**Directory structure:**
```
notebook-project/
├── notebooks/
│   ├── Analysis.nb
│   ├── Visualization.nb
│   └── Export.nb
├── exported/
│   ├── Analysis.m
│   ├── Visualization.m
│   └── Export.m
├── scripts/
│   └── export-notebooks.wls
└── sonar-project.properties
```

**`scripts/export-notebooks.wls` (Notebook export script):**

```mathematica
#!/usr/bin/env wolframscript
(* Export all notebooks to .m files for SonarQube analysis *)

notebookDir = FileNameJoin[{NotebookDirectory[], "..", "notebooks"}];
exportDir = FileNameJoin[{NotebookDirectory[], "..", "exported"}];

(* Create export directory if it doesn't exist *)
If[!DirectoryQ[exportDir], CreateDirectory[exportDir]];

(* Find all notebooks *)
notebooks = FileNames["*.nb", notebookDir];

(* Export each notebook to .m format *)
Do[
  nb = NotebookOpen[file];
  baseName = FileBaseName[file];
  outputFile = FileNameJoin[{exportDir, baseName <> ".m"}];

  (* Export as Mathematica package *)
  Export[outputFile, nb, "Package"];
  NotebookClose[nb];
  Print["Exported: ", baseName];
,
  {file, notebooks}
];

Print["Export complete. ", Length[notebooks], " notebooks exported."];
```

**`sonar-project.properties`:**

```properties
# ============================================
# Notebook-Based Project Configuration
# ============================================

# Project identification
sonar.projectKey=notebook-project
sonar.projectName=Mathematica Notebook Project
sonar.projectVersion=1.0.0

# Analyze exported .m files (not .nb notebooks)
sonar.sources=exported

# Encoding
sonar.sourceEncoding=UTF-8

# Mathematica file extensions
sonar.mathematica.file.suffixes=.m,.wl,.wls

# Exclude notebooks (cannot be analyzed directly)
sonar.exclusions=notebooks/**

# Duplication detection
sonar.cpd.mathematica.minimumTokens=50
sonar.cpd.mathematica.minimumLines=5
```

**Workflow:**

```bash
# Step 1: Export notebooks to .m files
./scripts/export-notebooks.wls

# Step 2: Run SonarQube analysis on exported files
sonar-scanner

# Step 3: View results in SonarQube UI
open http://localhost:9000/dashboard?id=notebook-project
```

**Automation with Git Hooks:**

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# Export notebooks before commit

echo "Exporting Mathematica notebooks..."
./scripts/export-notebooks.wls

# Add exported files to commit
git add exported/*.m

echo "Notebooks exported successfully"
```

Make it executable:

```bash
chmod +x .git/hooks/pre-commit
```

### Example 5: CI/CD Integration

**`.github/workflows/sonarqube.yml` (GitHub Actions):**

```yaml
name: SonarQube Analysis

on:
  push:
    branches:
      - main
      - develop
  pull_request:
    branches:
      - main

jobs:
  sonarqube:
    name: SonarQube Scan
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Full history for better analysis

      - name: SonarQube Scan
        uses: sonarsource/sonarqube-scan-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}

      - name: SonarQube Quality Gate Check
        uses: sonarsource/sonarqube-quality-gate-action@master
        timeout-minutes: 5
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**`sonar-project.properties` for CI/CD:**

```properties
# Project identification
sonar.projectKey=my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.projectVersion=1.0.0

# Sources
sonar.sources=src
sonar.tests=tests

# Encoding
sonar.sourceEncoding=UTF-8

# Mathematica extensions
sonar.mathematica.file.suffixes=.m,.wl,.wls

# Exclusions
sonar.exclusions=**/archive/**,**/deprecated/**

# SCM (for incremental analysis)
sonar.scm.provider=git

# Pull request decoration (for PRs)
sonar.pullrequest.branch=${GITHUB_HEAD_REF}
sonar.pullrequest.base=${GITHUB_BASE_REF}
sonar.pullrequest.key=${GITHUB_PR_NUMBER}
```

**GitLab CI/CD (`.gitlab-ci.yml`):**

```yaml
sonarqube-check:
  stage: test
  image:
    name: sonarsource/sonar-scanner-cli:latest
    entrypoint: [""]
  variables:
    SONAR_USER_HOME: "${CI_PROJECT_DIR}/.sonar"
    GIT_DEPTH: "0"
  cache:
    key: "${CI_JOB_NAME}"
    paths:
      - .sonar/cache
  script:
    - sonar-scanner
  only:
    - branches
    - merge_requests
```

## Advanced Configuration Topics

### Dynamic Configuration with Environment Variables

```properties
# Use environment variables in configuration
sonar.projectVersion=${env.PROJECT_VERSION}
sonar.host.url=${env.SONAR_HOST_URL}
sonar.token=${env.SONAR_TOKEN}
```

```bash
# Set environment variables before running
export PROJECT_VERSION="2.1.0"
export SONAR_HOST_URL="http://localhost:9000"
export SONAR_TOKEN="sqp_abc123..."

sonar-scanner
```

### Branch Analysis

```properties
# Long-lived branch
sonar.branch.name=develop

# Short-lived branch (will be compared to main branch)
sonar.branch.name=feature/new-feature
sonar.branch.target=main
```

### Pull Request Decoration

```properties
# Pull request analysis (SonarQube 8.0+)
sonar.pullrequest.key=123
sonar.pullrequest.branch=feature/my-feature
sonar.pullrequest.base=main
```

## Best Practices

### 1. Version Control Configuration

**Commit `sonar-project.properties` to Git:**

```bash
git add sonar-project.properties
git commit -m "Add SonarQube configuration"
```

**Add `.scannerwork/` to `.gitignore`:**

```bash
echo ".scannerwork/" >> .gitignore
git add .gitignore
git commit -m "Ignore SonarQube scanner working directory"
```

### 2. Consistent Encoding

Always specify UTF-8 encoding:

```properties
sonar.sourceEncoding=UTF-8
```

### 3. Meaningful Project Keys

Use descriptive, unique project keys:

```properties
# Good
sonar.projectKey=company-mathematica-analysis-suite

# Avoid
sonar.projectKey=project1
```

### 4. Semantic Versioning

Use semantic versioning for `projectVersion`:

```properties
sonar.projectVersion=2.1.0
```

### 5. Exclude Generated Code

Always exclude generated or vendored code:

```properties
sonar.exclusions=**/generated/**,**/vendor/**,**/external/**
```

### 6. Document Custom Settings

Add comments to explain non-obvious settings:

```properties
# Lowered token threshold to catch smaller duplications in utility functions
sonar.cpd.mathematica.minimumTokens=30
```

## Next Steps

- **Review Analysis Results**: Check your SonarQube dashboard
- **Customize Quality Profile**: Adjust rules for your team's needs
- **Set Up Quality Gate**: Define pass/fail criteria for builds
- **Integrate with CI/CD**: Automate analysis on every commit
- **Install SonarLint**: Get real-time feedback in your IDE

## Additional Resources

- **Installation Guide**: [Installation.md](Installation.md)
- **SonarQube Documentation**: https://docs.sonarqube.org/
- **SonarScanner Documentation**: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/
- **Plugin Repository**: https://github.com/bceverly/wolfralyze

---

**Version**: Compatible with SonarQube 9.9+
**Last Updated**: November 2025
**License**: GPL-3.0
