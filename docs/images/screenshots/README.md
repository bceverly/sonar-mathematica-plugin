# Wolfralyze Screenshots

This directory contains screenshots demonstrating Wolfralyze's features and capabilities in SonarQube.

## Screenshot Guide

### 01-dashboard-overview.png
**Project Dashboard Overview**

Shows the SonarQube project dashboard with Wolfralyze analyzing a Mathematica project.

**What to capture:**
- Quality Gate status
- Bug count (~15+)
- Vulnerability count (~12+)
- Security Hotspot count (~5+)
- Code Smell count (~25+)
- Coverage percentage
- Lines of code metrics

**URL:** `http://localhost:9000/dashboard?id=wolfralyze-test-project`

**Purpose:** Demonstrates the plugin working end-to-end, detecting comprehensive issues in Mathematica code.

---

### 02-issues-list.png
**Issues List**

Displays the issues list showing multiple Mathematica-specific code quality problems.

**What to capture:**
- Multiple issues with different severities (Blocker, Critical, Major)
- Mathematica file names (*.wl, *.m)
- Rule names (e.g., "Division by zero", "SQL injection", "Hardcoded credentials")
- At least 3-5 different issue types visible

**URL:** `http://localhost:9000/project/issues?id=wolfralyze-test-project&resolved=false`

**Purpose:** Shows the variety and depth of rule coverage across different issue categories.

---

### 03-code-with-issue.png
**Code Viewer with Highlighted Issue**

Shows Mathematica code with syntax highlighting and an issue highlighted inline.

**What to capture:**
- Mathematica code with proper syntax highlighting
- Highlighted issue line (red/orange marker)
- Issue description in sidebar or popup
- Line numbers
- File path showing .wl extension

**Suggested file:** `src/SecurityIssues.wl` (contains dramatic security vulnerabilities)

**URL:** `http://localhost:9000/code?id=wolfralyze-test-project`

**Purpose:** Demonstrates syntax highlighting and inline issue detection in the code viewer.

---

### 04-rule-detail.png
**Rule Detail Page**

Shows detailed information about a specific Mathematica rule.

**What to capture:**
- Rule name and description
- "Mathematica" language indicator
- Severity level
- Detailed rule description
- Code examples (if available)
- Tags (e.g., security, bug, code-smell)

**Suggested rules:** "SQL Injection", "Division by zero", "Command Injection"

**URL:** Click on any rule from the issues list or navigate through Quality Profiles

**Purpose:** Shows comprehensive rule documentation and Mathematica-specific guidance.

---

### 05-quality-profile.png
**Quality Profile - Mathematica Rules**

Displays the Mathematica quality profile with the complete rule set.

**What to capture:**
- "Mathematica" language profile
- "Sonar way" profile name
- Total active rules (~529+)
- Breakdown by type:
  - Bugs
  - Vulnerabilities
  - Security Hotspots
  - Code Smells

**URL:** `Administration → Quality Profiles → Mathematica → Sonar way`

**Purpose:** Demonstrates the comprehensive 529+ rule coverage comparable to Tier 1 languages.

---

### 06-plugin-installed.png
**Installed Plugin in Marketplace**

Shows Wolfralyze successfully installed in SonarQube's marketplace.

**What to capture:**
- Plugin name: "Mathematica" or "Wolfram Mathematica"
- Version number (1.0.0)
- Status: "Installed"
- Plugin key: "mathematica"

**URL:** `Administration → Marketplace → Installed`

**Purpose:** Proves the plugin installs cleanly and is recognized by SonarQube.

---

### 07-metrics.png (Optional)
**Project Metrics and Measures**

Displays code metrics calculated by Wolfralyze for Mathematica code.

**What to capture:**
- Lines of code
- Complexity metrics (cyclomatic, cognitive)
- Coverage percentage
- File count
- Language: Mathematica

**URL:** `http://localhost:9000/component_measures?id=wolfralyze-test-project`

**Purpose:** Shows the plugin calculates comprehensive metrics for Mathematica code.

---

## Usage

### For Marketplace Submission
Attach these screenshots when submitting to:
- SonarSource Community Forum
- Email to marketplace@sonarsource.com
- Include in any promotional materials

### For Website
Screenshots are automatically copied to `wolfralyze-docs/assets/images/screenshots/` and displayed on https://wolfralyze.org

### For Documentation
Reference screenshots in README.md and documentation using:
```markdown
![Dashboard Overview](docs/images/screenshots/01-dashboard-overview.png)
```

## Taking Screenshots

### Prerequisites
1. Ensure SonarQube is running with Wolfralyze 1.0.0 installed
2. Scan the test project:
   ```bash
   cd ~/dev/wolfralyze-test-project
   make scan
   ```
3. Open SonarQube: http://localhost:9000

### Best Practices
- Use 100% browser zoom (Cmd+0)
- Clean browser window (close unnecessary tabs)
- Light theme preferred (more professional)
- Ensure text is readable
- Capture enough context without excessive whitespace
- Hide any sensitive information

### File Naming
Follow the naming convention: `NN-descriptive-name.png`
- `01-` through `07-` for ordering
- Use lowercase with hyphens
- `.png` format for best quality

## Updating Screenshots

When updating screenshots for a new version:
1. Take new screenshots following the guide above
2. Replace old files with same filenames
3. Update this README if screenshot content changes significantly
4. Copy to website: `cp *.png /Users/bceverly/dev/wolfralyze-docs/assets/images/screenshots/`
5. Commit to both repositories

---

Generated for Wolfralyze v1.0.0
Last updated: 2025-11-09
