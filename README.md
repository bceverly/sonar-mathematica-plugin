# SonarQube Plugin for Wolfram Mathematica

A comprehensive SonarQube plugin providing code quality analysis, security scanning, and duplication detection for Wolfram Mathematica code.

## Features Overview

| Feature | Rules | Type | Status | Quick UI Path |
|---------|-------|------|--------|---------------|
| **Code Duplication Detection** | CPD Engine | Duplication | ✅ Active | Overview → Duplications % → Click |
| **Code Smells** | 8 rules | Code Quality | ✅ Active | Issues → Type: Code Smell |
| **Security Vulnerabilities** | 8 rules | Security | ✅ Active | Issues → Type: Vulnerability |
| **OWASP Top 10 2021 Coverage** | 9 of 10 categories | Security | ✅ Active | Issues → Type: Vulnerability |
| **Total Rules** | 16 rules + CPD | All | ✅ Active | Issues tab |

## Quick Navigation Cheat Sheet

**To see ALL issues at once:**
- Go to project → **Issues** tab

**To see Duplications:**
- Overview → Click **Duplications %** number
- Or: Measures → Duplications → Click any metric

**To see Code Smells:**
- Overview → Click **Code Smells** number
- Or: Issues → Filter Type → Check "Code Smell"

**To see Security Vulnerabilities:**
- Overview → Click **Vulnerabilities** number (in Security section)
- Or: Issues → Filter Type → Check "Vulnerability"

**To filter by severity:**
- Issues → Filter Severity → Check: Blocker / Critical / Major / Minor / Info

**To search for specific rule:**
- Issues → Search box → Type rule keywords (e.g., "commented", "SQL", "hardcoded")

---

## 1. Code Duplication Detection

Automatically detects copy-pasted code blocks using SonarQube's built-in CPD (Copy-Paste Detection) engine.

### 📍 How to View in SonarQube UI

**Method 1: Overview → Duplications Metric**
1. Go to `http://localhost:9000`
2. Click on your project name (e.g., "SLL Mathematica Project")
3. You're now on the **Overview** tab
4. Scroll down to find the **"Duplications"** metric (shows percentage like "5.2%")
5. Click on the percentage number
6. → Takes you to a list of all duplicated blocks

**Method 2: Measures Tab → Duplications**
1. From project page, click **"Measures"** in left sidebar
2. Click **"Duplications"** in the metrics categories
3. You'll see:
   - **Duplicated Lines (%)** - Click to see which files have duplications
   - **Duplicated Blocks** - Click to see specific code blocks duplicated
   - **Duplicated Files** - Click to see which files contain duplicates
4. Click any metric number (e.g., "47" duplicated blocks)
5. → Shows list of all duplications with file locations
6. Click any duplication entry
7. → Shows the exact code block and all locations where it appears

**Method 3: Code Tab → File View**
1. From project page, click **"Code"** tab in left sidebar
2. Navigate through your directory tree (e.g., `Analysis/sources/`)
3. Click on any `.m` file
4. Duplicated code blocks are highlighted with a colored bar on the left
5. Click the colored bar or line numbers
6. → Popup shows all locations where this code block appears
7. Click "See all X duplications" link
8. → Shows side-by-side comparison with all duplicated instances

### Configuration

```properties
# In sonar-project.properties
sonar.cpd.mathematica.minimumTokens=250  # Default: 250
sonar.cpd.mathematica.minimumLines=25    # Default: 25
```

**Note:** If you see "Too many duplication references" warnings, this is normal for files with >100 duplications. To reduce noise, exclude documentation directories:

```properties
sonar.exclusions=**/docs/**,**/reference_pages/**
```

---

## 2. Code Smell Rules (8 Total)

### 2.1 Commented-Out Code
**Severity:** MAJOR | **Type:** CODE_SMELL

Detects code that has been commented out instead of removed.

**Example:**
```mathematica
(* VIOLATION *)
(* oldFunction[x_] := x^2 + x; *)

(* COMPLIANT - Actual documentation *)
(* This function calculates the area *)
```

### 📍 How to View in SonarQube UI

**Method 1: Issues Tab → Filter by Rule**
1. From project page, click **"Issues"** tab in left sidebar
2. In the filter panel on the left, find **"Type"**
3. Check the box for **"Code Smell"**
4. In the search box at top, type: `commented`
5. → Shows all "Sections of code should not be commented out" issues
6. Click any issue to see the exact line of code
7. Click the file name to see it in context

**Method 2: Overview → Code Smells Count**
1. From project **Overview** tab
2. Find the **"Code Smells"** metric (e.g., "245")
3. Click the number
4. → Takes you to **Issues** tab filtered to Code Smells
5. Use search box to type `commented`
6. → Filters to just commented code issues

**Method 3: Rules Tab → View Rule Details**
1. Click **"Rules"** in the top navigation bar
2. In left panel, filter **"Language"** → Select **"Mathematica"**
3. Find and click **"Sections of code should not be commented out"**
4. → Shows rule description, examples, and severity
5. Click **"See Issues"** button
6. → Shows all instances of this issue in your project

---

### 2.2 Magic Numbers
**Severity:** MINOR | **Type:** CODE_SMELL

Flags unexplained numeric literals (except 0-10).

**Example:**
```mathematica
(* VIOLATION *)
area = radius * 3.14159;

(* COMPLIANT *)
pi = 3.14159;
area = radius * pi;
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Severity"** → Check **"Minor"**
2. In search box, type: `magic`
3. → Shows all magic number issues
4. Or filter by **"Type"** → **"Code Smell"** then search `magic`

---

### 2.3 TODO/FIXME Comments
**Severity:** INFO | **Type:** CODE_SMELL

Tracks technical debt markers for prioritization.

**Example:**
```mathematica
(* TODO: Add error handling *)
(* FIXME: Performance issue here *)
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Severity"** → Check **"Info"**
2. → Shows all TODO/FIXME comments tracked
3. Or search: `TODO` or `FIXME`
4. Click any issue to see the comment location

---

### 2.4 Empty Blocks
**Severity:** MAJOR | **Type:** CODE_SMELL

Detects empty `Module`, `Block`, or `With` constructs.

**Example:**
```mathematica
(* VIOLATION *)
Module[{x}, ]

(* COMPLIANT *)
Module[{x}, x = 5; x + 1]
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → **"Code Smell"**
2. Search: `empty`
3. → Shows all empty block issues
4. Click to see the exact Module/Block/With construct

---

### 2.5 Function Length
**Severity:** MAJOR | **Type:** CODE_SMELL | **Default:** 150 lines

Flags functions exceeding line limit (configurable).

**Configuration:**
```properties
sonar.mathematica.function.maximumLines=150
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Search: `Functions should not be too long`
2. → Shows all long function violations
3. Each issue shows function name and line count (e.g., "Function 'ProcessData' has 234 lines")
4. Click to see the function definition

---

### 2.6 File Length
**Severity:** MAJOR | **Type:** CODE_SMELL | **Default:** 1000 lines

Flags files exceeding line limit (configurable).

**Configuration:**
```properties
sonar.mathematica.file.maximumLines=1000
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Search: `Files should not be too long`
2. → Shows all long file violations
3. Each issue shows file name and line count (e.g., "File has 2,543 lines")
4. Click to navigate to the file

---

### 2.7 Empty Catch Blocks
**Severity:** MAJOR | **Type:** CODE_SMELL
**CWE:** [CWE-391](https://cwe.mitre.org/data/definitions/391.html) | **OWASP:** A09:2021 - Security Logging and Monitoring Failures

Detects exception handlers that silently ignore errors without logging or handling them properly.

**Examples:**
```mathematica
(* VIOLATIONS *)
result = Check[riskyOperation[], $Failed];  (* No logging *)
Quiet[securityCheck[]];                      (* Suppresses all errors *)

(* COMPLIANT *)
result = Check[riskyOperation[],
  (Print["Error: ", $MessageList]; LogError[$MessageList]; $Failed)
];
```

### 📍 How to View in SonarQube UI

**Method 1: Issues Tab → Filter by Rule**
1. **Issues** tab → Filter **"Type"** → Check **"Code Smell"**
2. Search: `exception` or `silently ignored`
3. → Shows "Exceptions should not be silently ignored" issues
4. Click any issue to see the Check[] or Quiet[] call

**Method 2: Overview → Code Smells**
1. From project **Overview** tab
2. Click **"Code Smells"** count
3. Search: `Quiet` or `Check`
4. → Filters to exception handling issues
5. Review each to ensure proper error logging

**Method 3: Rules Tab**
1. **Rules** tab in top navigation
2. Filter **"Language"** → **"Mathematica"**
3. Search: `exception`
4. Click **"Exceptions should not be silently ignored"**
5. Click **"See Issues"** button
6. → Shows all empty catch block violations

---

### 2.8 Debug Code
**Severity:** MAJOR | **Type:** CODE_SMELL
**CWE:** [CWE-489](https://cwe.mitre.org/data/definitions/489.html), [CWE-215](https://cwe.mitre.org/data/definitions/215.html) | **OWASP:** A05:2021 - Security Misconfiguration

Detects debug statements that should not be left in production code (Print, Echo, Trace, etc.).

**Examples:**
```mathematica
(* VIOLATIONS *)
Print["User password: ", userPassword];      (* Exposes sensitive data *)
Echo[databaseCredentials, "DB Config:"];     (* Debug output *)
TracePrint[AuthenticationLogic[]];           (* Exposes execution flow *)
$DebugMessages = True;                       (* Global debug mode *)

(* COMPLIANT *)
If[$DevelopmentMode,
  WriteLog["Auth attempt for: " <> username, "DEBUG"]
];
```

### 📍 How to View in SonarQube UI

**Method 1: Issues Tab → Search**
1. **Issues** tab → Filter **"Type"** → Check **"Code Smell"**
2. Search: `debug` or `Print` or `production`
3. → Shows "Debug code should not be left in production" issues
4. Click any issue to see Print[], Echo[], Trace[], or Monitor[] calls

**Method 2: Overview → Code Smells → Filter**
1. From project **Overview** tab
2. Click **"Code Smells"** count
3. Filter **"Severity"** → Check **"Major"**
4. Search: `debug`
5. → Shows all debug code violations
6. Review and remove before production deployment

**Method 3: Code Tab → File View**
1. **Code** tab in left sidebar
2. Navigate to a file with debug code
3. Look for highlighted lines with warning icons
4. Hover to see "Remove this debug print statement before deploying to production"
5. Click the line to see full issue details

---

## 3. Security Vulnerability Rules (8 Total)

### 3.1 Hardcoded Credentials
**Severity:** BLOCKER | **Type:** VULNERABILITY
**CWE:** [CWE-798](https://cwe.mitre.org/data/definitions/798.html) | **OWASP:** A07:2021

Detects passwords, API keys, tokens, and other credentials in source code.

**Example:**
```mathematica
(* VIOLATION *)
apiKey = "sk_live_1234567890abcdef";
password = "MyPassword123";

(* COMPLIANT *)
apiKey = Environment["API_KEY"];
```

### 📍 How to View in SonarQube UI

**Method 1: Overview → Vulnerabilities**
1. From project **Overview** tab
2. Find the **"Vulnerabilities"** metric in the Security section (e.g., "3" with a red icon)
3. Click the number
4. → Takes you to **Issues** tab filtered to Vulnerabilities
5. Look for issues with severity **BLOCKER** (red circle icon)
6. → Hardcoded credential issues will be marked "Credentials should not be hard-coded"

**Method 2: Issues Tab → Direct Filter**
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Blocker"**
3. → Shows all blocker vulnerabilities (hardcoded credentials)
4. Click any issue to see: variable name (e.g., `apiKey`), file location, line number
5. Click "Why is this an issue?" to see CWE-798 and OWASP references

**Method 3: Security Rating (Overview)**
1. From project **Overview** tab
2. Look for **"Security Rating"** (letter grade A-E)
3. Click the letter grade
4. → Shows breakdown of vulnerabilities by severity
5. Click **"Blocker"** count
6. → Filtered list of hardcoded credentials

---

### 3.2 Command Injection
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-78](https://cwe.mitre.org/data/definitions/78.html) | **OWASP:** A03:2021

Detects OS commands constructed from user input.

**Example:**
```mathematica
(* VIOLATION *)
Run["cat " <> userFile];
RunProcess[{"sh", "-c", "grep " <> searchTerm}];

(* COMPLIANT *)
RunProcess[{"grep", searchTerm, "file.txt"}]; (* Array form *)
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `OS commands`
4. → Shows "Make sure that executing this OS command is safe"
5. Click to see `Run`, `RunProcess`, or `Import` usage with string concatenation

---

### 3.3 SQL Injection
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-89](https://cwe.mitre.org/data/definitions/89.html) | **OWASP:** A03:2021

Detects SQL queries built with string concatenation.

**Example:**
```mathematica
(* VIOLATION *)
SQLExecute[conn, "SELECT * FROM users WHERE id=" <> userId];

(* COMPLIANT *)
SQLExecute[conn, "SELECT * FROM users WHERE id=?", {userId}];
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `SQL`
4. → Shows "Use parameterized queries to prevent SQL injection"
5. Click to see `SQLExecute`, `SQLSelect`, `SQLInsert`, etc. with string concatenation

---

### 3.4 Code Injection
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-94](https://cwe.mitre.org/data/definitions/94.html) | **OWASP:** A03:2021

Detects dynamic code evaluation from user input.

**Example:**
```mathematica
(* VIOLATION *)
result = ToExpression[userInput];

(* COMPLIANT *)
If[StringMatchQ[input, SafePattern], ToExpression[input], $Failed]
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `evaluating`
4. → Shows "Make sure that evaluating this expression is safe"
5. Click to see `ToExpression` or `Evaluate` usage on non-string-literal input

---

### 3.5 Path Traversal
**Severity:** HIGH | **Type:** VULNERABILITY
**CWE:** [CWE-22](https://cwe.mitre.org/data/definitions/22.html) | **OWASP:** A01:2021

Detects file operations with unsanitized paths.

**Example:**
```mathematica
(* VIOLATION *)
Import[baseDir <> userFileName]; (* User supplies: "../../etc/passwd" *)

(* COMPLIANT *)
safeFile = FileNameTake[userFileName];
Import[FileNameJoin[{baseDir, safeFile}]];
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"High"**
3. Search: `path`
4. → Shows "Validate and sanitize this file path to prevent path traversal attacks"
5. Click to see `Import`, `Export`, `Get`, `Put`, etc. with string concatenation for paths

---

### 3.6 Weak Cryptography
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-327](https://cwe.mitre.org/data/definitions/327.html), [CWE-338](https://cwe.mitre.org/data/definitions/338.html) | **OWASP:** A02:2021 - Cryptographic Failures

Detects use of weak cryptographic algorithms (MD5, SHA1) and insecure random number generation for security purposes.

**Examples:**
```mathematica
(* VIOLATIONS *)
hash = Hash[data, "MD5"];           (* MD5 is broken *)
signature = Hash[msg, "SHA1"];      (* SHA1 is broken *)
token = ToString[Random[]];          (* Random[] is not cryptographically secure *)

(* COMPLIANT *)
hash = Hash[data, "SHA256"];        (* Use SHA256 or SHA512 *)
token = IntegerString[RandomInteger[{10^20, 10^21}], 16];  (* Use RandomInteger *)
```

### 📍 How to View in SonarQube UI

**Method 1: Overview → Vulnerabilities**
1. From project **Overview** tab
2. Find the **"Vulnerabilities"** metric (e.g., "15" with red icon)
3. Click the number
4. → Takes you to **Issues** tab filtered to Vulnerabilities
5. Search: `crypto` or `weak`
6. → Shows issues related to weak cryptography

**Method 2: Issues Tab → Direct Filter**
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `MD5` or `SHA1` or `Random`
4. → Shows "Use a stronger hash algorithm" or "Use RandomInteger instead of Random"
5. Click to see `Hash[..., "MD5"]`, `Hash[..., "SHA1"]`, or `Random[]` usage

**Method 3: Rules Tab**
1. **Rules** tab in top navigation
2. Search: `weak crypto` or `cryptographic`
3. Click **"Weak cryptographic algorithms should not be used"**
4. → Shows rule details, examples, and CWE references
5. Click **"See Issues"** button (top right)
6. → Shows all violations of this rule in your project

---

### 3.7 Server-Side Request Forgery (SSRF)
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-918](https://cwe.mitre.org/data/definitions/918.html) | **OWASP:** A10:2021 - SSRF

Detects URL construction from user input which can lead to SSRF attacks allowing access to internal services or cloud metadata endpoints.

**Examples:**
```mathematica
(* VIOLATIONS *)
URLFetch["https://api.example.com/" <> userEndpoint];  (* User controls endpoint *)
Import["https://" <> userDomain <> "/data.json"];      (* User controls domain *)
(* User could supply: 169.254.169.254/latest/meta-data to access AWS metadata *)

(* COMPLIANT *)
allowedEndpoints = {"users", "posts"};
If[MemberQ[allowedEndpoints, userEndpoint],
  URLFetch["https://api.example.com/" <> userEndpoint],
  $Failed
];
```

### 📍 How to View in SonarQube UI

**Method 1: Overview → Vulnerabilities**
1. From project **Overview** tab
2. Find the **"Vulnerabilities"** metric
3. Click the number
4. Search: `SSRF` or `URL`
5. → Shows "Validate and sanitize URLs to prevent Server-Side Request Forgery"

**Method 2: Issues Tab → Direct Filter**
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `URL` or `request forgery`
4. → Shows SSRF vulnerabilities
5. Click to see `URLFetch`, `URLRead`, `URLExecute`, or `ServiceExecute` with concatenated URLs

**Method 3: Code Tab → Hotspots**
1. **Code** tab in left sidebar
2. Navigate to files with network operations
3. Look for highlighted lines with SSRF issues
4. Hover over the highlighted region
5. → Shows "Validate and sanitize URLs" message
6. Click to see full issue details

---

### 3.8 Insecure Deserialization
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-502](https://cwe.mitre.org/data/definitions/502.html) | **OWASP:** A08:2021 - Software and Data Integrity Failures

Detects deserialization of untrusted data including loading .mx/.wdx files or using Get[] on user-controlled paths, which can lead to remote code execution.

**Examples:**
```mathematica
(* VIOLATIONS *)
Import[userFile, "MX"];              (* MX files can execute arbitrary code *)
Import[uploadedFile, "WDX"];         (* WDX files can also execute code *)
Get["/packages/" <> userPackage];    (* Loading code from user input *)
Get["http://" <> userDomain <> "/lib.m"];  (* Loading from user URL *)

(* COMPLIANT *)
Import[userFile, "JSON"];            (* Use safe data-only formats *)
Import[userFile, "CSV"];

(* Whitelist trusted packages *)
trustedPackages = {"/usr/local/math/TrustedPackage.m"};
If[MemberQ[trustedPackages, path], Get[path], $Failed];

(* Verify integrity before loading *)
If[Hash[Import[file, "String"], "SHA256"] === expectedHash, Get[file], $Failed];
```

### 📍 How to View in SonarQube UI

**Method 1: Overview → Vulnerabilities**
1. From project **Overview** tab
2. Find the **"Vulnerabilities"** metric
3. Click the number
4. Search: `deserialization` or `MX` or `Get`
5. → Shows "Avoid importing MX/WDX files" or "Avoid loading code from untrusted sources"

**Method 2: Issues Tab → Direct Filter**
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `deserializ` or `Get[`
4. → Shows insecure deserialization issues
5. Click to see `Import[..., "MX"]`, `Import[..., "WDX"]`, or `Get[]` with user input

**Method 3: Measures Tab → Security**
1. **Measures** tab in left sidebar
2. Click **"Security"** category
3. Scroll to **"Vulnerabilities"**
4. Click the vulnerabilities count
5. → Lists all security vulnerabilities including deserialization issues
6. Use search or filter to find specific deserialization violations

---

## OWASP Top 10 2021 Coverage Summary

This plugin now covers **9 out of 10** OWASP Top 10 2021 categories:

| OWASP Category | Coverage | Rules in Plugin |
|----------------|----------|-----------------|
| ✅ **A01** - Broken Access Control | **Covered** | Path Traversal |
| ✅ **A02** - Cryptographic Failures | **Covered** | Weak Cryptography |
| ✅ **A03** - Injection | **Covered** | Command, SQL, Code Injection |
| ❌ **A04** - Insecure Design | Not Covered | Too abstract for static analysis |
| ✅ **A05** - Security Misconfiguration | **Covered** | Debug Code Detection |
| ❌ **A06** - Vulnerable Components | Not Covered | No package manager in Mathematica |
| ✅ **A07** - Identification/Auth Failures | **Covered** | Hardcoded Credentials |
| ✅ **A08** - Software/Data Integrity | **Covered** | Insecure Deserialization |
| ✅ **A09** - Logging/Monitoring Failures | **Covered** | Empty Catch Blocks |
| ✅ **A10** - Server-Side Request Forgery | **Covered** | SSRF Detection |

**Coverage: 9/10 (90%)** - Industry-leading security scanning for Mathematica code

---

## Complete SonarQube UI Navigation Guide

### 🎯 First-Time User Walkthrough

**Step 1: Access Your Project**
1. Open browser → Go to `http://localhost:9000`
2. You'll see the SonarQube dashboard with all projects
3. Click on your project name (e.g., "SLL Mathematica Project")
4. → You're now on the **Overview** tab (this is your starting point)

**Step 2: Understand the Overview Tab**
The Overview tab shows your project's health at a glance:

**Reliability Section:**
- **Bugs**: 0 (we don't detect these yet)

**Security Section:**
- **Vulnerabilities**: Shows count (e.g., "20") - Our 8 security rules findings
  - Click this number → See all security issues
- **Security Rating**: Letter grade A-E
  - Click the grade → See rating breakdown
- **Security Hotspots**: 0 (we don't detect these)

**Maintainability Section:**
- **Code Smells**: Shows count (e.g., "300") - Our 8 code quality rules findings
  - Click this number → See all code smells
- **Maintainability Rating**: Letter grade A-E
- **Technical Debt**: Estimated time to fix all code smells

**Coverage Section:**
- **Coverage**: N/A (we don't measure test coverage)

**Duplications Section:**
- **Duplications**: Shows percentage (e.g., "5.2%")
  - Click the percentage → See all duplicated code blocks

**Step 3: Navigate to Issues**
Click **"Issues"** in the left sidebar → This is where you'll spend most of your time

**Step 4: Filter Issues**
Use the left panel filters:
- **Type**: Vulnerability, Code Smell, Bug, Security Hotspot
- **Severity**: Blocker, Critical, Major, Minor, Info
- **Rule**: Search or select specific rules
- **File**: Filter by specific files/directories
- **Assignee**: Filter by team member

**Step 5: View an Issue**
1. Click any issue in the list
2. You'll see:
   - **Code snippet** with the issue highlighted
   - **Issue message** explaining the problem
   - **Rule description** ("Why is this an issue?")
   - **Location** (file path and line number)
   - **Effort** (estimated time to fix)

**Step 6: Take Action on Issues**
For each issue, you can:
- **Assign** to a developer
- **Comment** to discuss
- **Change Severity** (if you disagree with default)
- **Resolve as**: Fixed, Won't Fix, False Positive

---

### Overview Tab
**What:** High-level project health dashboard

**Key Metrics:**
- **Bugs** - Logic errors (not detected by this plugin yet)
- **Vulnerabilities** - Our 8 security rules
- **Code Smells** - Our 6 maintainability rules
- **Security Hotspots** - Manual review locations
- **Duplications** - Copy-pasted code percentage

**Quick Actions:**
- Click any number to jump to detailed issues
- View **Maintainability Rating** (A-E)
- View **Security Rating** (A-E)

---

### Issues Tab
**What:** Detailed list of all detected issues

**Essential Filters:**
1. **Type** - Vulnerability, Code Smell, Bug, Security Hotspot
2. **Severity** - Blocker, Critical, Major, Minor, Info
3. **Rule** - Search by rule name
4. **Resolution** - Open, Fixed, Won't Fix, False Positive
5. **Assignee** - Filter by developer
6. **File/Directory** - Specific paths

**Actions:**
- **Assign** issue to team member
- **Comment** on issues
- **Change Severity** (override)
- **Mark as** False Positive / Won't Fix / etc.

---

### Measures Tab
**What:** Detailed metrics and drilldowns

**Categories:**
- **Reliability** - Bug metrics
- **Security** - Vulnerability metrics (our 8 rules)
- **Maintainability** - Code smell metrics (our 8 rules)
- **Coverage** - Test coverage (not applicable)
- **Duplications** - Our CPD engine results
- **Size** - LOC, files, functions counted

**Navigation:**
- Click metric → See file-by-file breakdown
- Click file → View code with issues highlighted

---

### Code Tab
**What:** Browse source files with inline issue highlighting

**Features:**
- View all scanned files
- Issues highlighted directly in code
- Click line number to see issue details
- Duplications shown with clickable links

---

### Rules Tab
**What:** View and manage all available rules

**Access:**
1. **Quality Profiles** → **Mathematica** → **Sonar way**
2. Or: Top navigation → **Rules** → Language filter → **Mathematica**

**Shows:**
- All 11 active rules (6 code smells + 5 security)
- Rule descriptions
- CWE/OWASP mappings
- Compliant/noncompliant examples

**Actions:**
- Activate/deactivate rules
- Change severities
- Create custom quality profiles

---

## Installation

### Prerequisites
- SonarQube 9.9+ (or SonarQube 10.x)
- Java 11+
- Gradle (for building)

### Build & Install

```bash
# 1. Build the plugin
cd ~/dev/sonar-mathematica-plugin
gradle clean build -x test

# 2. Copy to SonarQube plugins directory
cp build/libs/sonar-mathematica-plugin-0.1.0-SNAPSHOT.jar \
   <SONARQUBE_HOME>/extensions/plugins/

# 3. Restart SonarQube
<SONARQUBE_HOME>/bin/[OS]/sonar.sh restart
```

### Verify Installation

1. Log in to SonarQube as admin
2. Navigate to **Administration** → **Marketplace** → **Installed**
3. Find "Mathematica" in the list
4. Go to **Administration** → **General Settings** → **Mathematica** category
5. You should see configuration options for duplication thresholds and code quality

---

## Usage

### 1. Project Configuration

Create `sonar-project.properties` in your Mathematica project root:

```properties
# Project identification
sonar.projectKey=my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.projectVersion=1.0

# Source code
sonar.sources=.
sonar.sourceEncoding=UTF-8

# Mathematica file extensions
sonar.mathematica.file.suffixes=.m,.wl,.wls

# Duplication Detection (optional)
sonar.cpd.mathematica.minimumTokens=250
sonar.cpd.mathematica.minimumLines=25

# Code Quality Thresholds (optional)
sonar.mathematica.function.maximumLines=150
sonar.mathematica.file.maximumLines=1000

# Exclusions (optional - reduces duplication warnings)
sonar.exclusions=**/docs/**,**/reference_pages/**,**/tests/**
```

### 2. Run Analysis

```bash
sonar-scanner \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token-or-username> \
  -Dsonar.password=<your-password>
```

Or create a token:
1. SonarQube → **My Account** → **Security** → **Generate Tokens**
2. Use token instead of password:

```bash
sonar-scanner \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>
```

### 3. View Results

Open http://localhost:9000 and navigate to your project.

---

## Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `sonar.mathematica.file.suffixes` | `.m,.wl,.wls` | File extensions to analyze |
| `sonar.cpd.mathematica.minimumTokens` | `250` | Min tokens for duplication |
| `sonar.cpd.mathematica.minimumLines` | `25` | Min lines for duplication |
| `sonar.mathematica.function.maximumLines` | `150` | Max function length |
| `sonar.mathematica.file.maximumLines` | `1000` | Max file length |

---

## Performance

### File Size Limits
The plugin automatically skips:
- Files >10,000 lines
- Files >1MB

This prevents performance issues on extremely large files.

### Expected Scan Times
- **Small projects** (<100 files): Seconds
- **Medium projects** (1,000 files): 1-3 minutes
- **Large projects** (10,000+ files): 5-15 minutes

### Optimization Tips

1. **Exclude documentation:**
   ```properties
   sonar.exclusions=**/docs/**,**/examples/**
   ```

2. **Increase duplication thresholds** (reduces sensitivity):
   ```properties
   sonar.cpd.mathematica.minimumTokens=500
   sonar.cpd.mathematica.minimumLines=50
   ```

3. **Parallel scanning** (if using sonar-scanner):
   ```properties
   sonar.cpd.threads=4
   ```

---

## Examples

Example files demonstrating all rules:

| File | Demonstrates |
|------|--------------|
| `examples/sample.m` | Basic duplication |
| `examples/commented-code-example.m` | All 6 code smell rules |
| `examples/security-example.m` | All 5 security rules |
| `examples/all-rules-example.m` | Complete rule showcase |

---

## Supported File Types

- `.m` - Mathematica package/source files
- `.wl` - Wolfram Language files
- `.wls` - Wolfram Language script files

**Not yet supported:**
- `.nb` - Mathematica notebook files (binary/XML format)

---

## Troubleshooting

### Plugin not visible in SonarQube
- Check `<SONARQUBE_HOME>/extensions/plugins/` for the JAR
- Review logs: `<SONARQUBE_HOME>/logs/sonar.log`
- Ensure SonarQube 9.9+ and Java 11+
- Restart SonarQube completely

### No issues detected
- Verify file extensions match `sonar.mathematica.file.suffixes`
- Check `sonar.sources` path is correct
- Look for scan errors in output
- Ensure quality profile has rules activated

### Scan is very slow
- Large files (>10,000 lines) are auto-skipped
- Exclude documentation directories
- Increase duplication thresholds

### Too many duplication warnings
This is normal! It means SonarQube found blocks duplicated >100 times. The duplications are still counted, just references are capped.

**Solution:** Exclude repetitive documentation:
```properties
sonar.exclusions=**/docs/**,**/reference_pages/**
```

---

## Development

### Project Structure
```
src/main/java/org/sonar/plugins/mathematica/
├── MathematicaPlugin.java              # Plugin entry point
├── MathematicaLanguage.java            # Language definition
├── MathematicaQualityProfile.java      # Default quality profile
├── MathematicaCpdTokenizer.java        # Duplication detection
└── rules/
    ├── MathematicaRulesDefinition.java # Rule definitions (11 rules)
    └── MathematicaRulesSensor.java     # Detection logic
```

### Adding New Rules

1. Add rule key in `MathematicaRulesDefinition.java`
2. Define rule with `repository.createRule()`
3. Add detection pattern/method in `MathematicaRulesSensor.java`
4. Activate in `MathematicaQualityProfile.java`
5. Rebuild and test

---

## License

GPL-3.0

---

## References

- [SonarQube Plugin API](https://docs.sonarqube.org/latest/extend/developing-a-plugin/)
- [OWASP Top 10 2021](https://owasp.org/Top10/)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [Wolfram Language Docs](https://reference.wolfram.com/)

---

## Contributing

Contributions welcome! Open issues or submit PRs on GitHub.

**Areas needing help:**
- Additional security rules
- More code smell patterns
- Notebook (`.nb`) file support
- Test coverage
- Documentation improvements
