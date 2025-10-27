# SonarQube Plugin for Wolfram Mathematica

A comprehensive SonarQube plugin providing code quality analysis, security scanning, and duplication detection for Wolfram Mathematica code.

## Features Overview

| Feature | Rules | Type | Status | Quick UI Path |
|---------|-------|------|--------|---------------|
| **Code Duplication Detection** | CPD Engine | Duplication | ✅ Active | Overview → Duplications % → Click |
| **Code Smells** | 8 rules | Code Quality | ✅ Active | Issues → Type: Code Smell |
| **Security Vulnerabilities** | 8 rules | Security | ✅ Active | Issues → Type: Vulnerability |
| **Bugs (Reliability)** | 5 rules | Reliability | ✅ Active | Issues → Type: Bug |
| **Security Hotspots** | 3 rules | Security Review | ✅ Active | Security Hotspots tab |
| **Complexity Metrics** | Cyclomatic & Cognitive | Complexity | ✅ Active | Code tab → Function details |
| **OWASP Top 10 2021 Coverage** | 9 of 10 categories | Security | ✅ Active | Issues → Type: Vulnerability |
| **Total Rules** | 26 rules + CPD + Metrics | All | ✅ Active | Issues tab |

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

**To see Bugs (Reliability):**
- Overview → Click **Bugs** number (in Reliability section)
- Or: Issues → Filter Type → Check "Bug"

**To see Security Hotspots:**
- Left sidebar → **Security Hotspots** tab (primary way)
- Or: Overview → Click **Security Review** percentage
- Or: Issues → Filter Type → Check "Security Hotspot"

**To see Complexity Metrics:**
- Measures → Complexity → View all metrics
- Or: Issues → Search "complexity"
- Or: Code tab → Navigate to file → See per-function complexity

**To filter by severity:**
- Issues → Filter Severity → Check: Blocker / Critical / Major / Minor / Info

**To search for specific rule:**
- Issues → Search box → Type rule keywords (e.g., "commented", "SQL", "hardcoded", "division", "recursion")

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

## 3. Bug Rules - Reliability (5 Total)

Bug rules detect logic errors and runtime issues that cause program crashes or incorrect behavior.

### 3.1 Division by Zero
**Severity:** MAJOR | **Type:** BUG
**CWE:** [CWE-369](https://cwe.mitre.org/data/definitions/369.html)

Detects division operations that may cause divide-by-zero runtime errors.

**Example:**
```mathematica
(* VIOLATION *)
result = numerator / denominator;  (* What if denominator is 0? *)
ratio = total / count;

(* COMPLIANT *)
If[denominator != 0, numerator / denominator, $Failed];
result = Check[numerator / denominator, $Failed];
```

### 📍 How to View in SonarQube UI

1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Search: `division` or `divisor`
3. → Shows all division-by-zero issues
4. Click to see the exact division operation
5. **Reliability Rating** on Overview shows letter grade based on bugs

---

### 3.2 Assignment in Conditional
**Severity:** CRITICAL | **Type:** BUG
**CWE:** [CWE-480](https://cwe.mitre.org/data/definitions/480.html)

Detects assignment (=) used instead of comparison (==, ===) in If/While/Which statements.

**Example:**
```mathematica
(* VIOLATION - Sets x to 5, always true! *)
If[x = 5, doSomething[]];

(* COMPLIANT *)
If[x == 5, doSomething[]];
If[x === 5, doSomething[]];  (* Strict equality *)
```

### 📍 How to View in SonarQube UI

1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `assignment` or `conditional`
4. → Shows all assignment-in-conditional bugs
5. These are CRITICAL because they always cause incorrect behavior

---

### 3.3 List Index Out of Bounds
**Severity:** MAJOR | **Type:** BUG
**CWE:** [CWE-125](https://cwe.mitre.org/data/definitions/125.html)

Detects list element access without bounds checking.

**Example:**
```mathematica
(* VIOLATION *)
element = myList[[index]];     (* What if index > Length[myList]? *)
first = items[[1]];            (* What if items is empty? *)

(* COMPLIANT *)
If[1 <= index <= Length[myList], myList[[index]], $Failed];
element = If[Length[items] > 0, First[items], $Failed];
```

### 📍 How to View in SonarQube UI

1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Search: `bounds` or `index`
3. → Shows all list access issues
4. Click to see the `[[index]]` usage
5. Review each to add bounds checking

---

### 3.4 Infinite Recursion
**Severity:** CRITICAL | **Type:** BUG
**CWE:** [CWE-674](https://cwe.mitre.org/data/definitions/674.html)

Detects recursive functions without proper base cases that may cause stack overflow.

**Example:**
```mathematica
(* VIOLATION - No base case! *)
factorial[n_] := n * factorial[n - 1];

(* COMPLIANT *)
factorial[0] = 1;
factorial[n_] := n * factorial[n - 1];
```

### 📍 How to View in SonarQube UI

1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `recursion` or `base case`
4. → Shows functions that may recurse infinitely
5. These are CRITICAL because they crash the program

---

### 3.5 Unreachable Pattern Definitions
**Severity:** MAJOR | **Type:** BUG

Detects pattern definitions that will never match because a more general pattern was defined earlier.

**Example:**
```mathematica
(* VIOLATION - Specific patterns after general pattern never match! *)
process[x_] := defaultProcess[x];
process[x_Integer] := integerProcess[x];  (* NEVER CALLED *)
process[x_String] := stringProcess[x];    (* NEVER CALLED *)

(* COMPLIANT - Specific patterns first *)
process[x_Integer] := integerProcess[x];
process[x_String] := stringProcess[x];
process[x_] := defaultProcess[x];  (* Catch-all last *)
```

### 📍 How to View in SonarQube UI

1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Search: `unreachable` or `pattern`
3. → Shows pattern definitions that will never execute
4. Click to see the function with multiple pattern definitions
5. Review and reorder patterns (specific → general)

---

## 4. Security Vulnerability Rules (8 Total)

### 4.1 Hardcoded Credentials
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

## 5. Security Hotspot Rules (3 Total)

Security Hotspots are code locations that require manual security review. Unlike Vulnerabilities (which are definite security issues), Hotspots highlight sensitive operations that **may** be secure if proper safeguards are in place.

**Key Difference:**
- **VULNERABILITY** = Definite security issue (must be fixed)
- **SECURITY_HOTSPOT** = Needs manual review (may be OK if validated properly)

### 5.1 File Upload Validation
**Severity:** MAJOR | **Type:** SECURITY_HOTSPOT
**CWE:** [CWE-434](https://cwe.mitre.org/data/definitions/434.html)

Flags file import/upload operations for manual security review.

**Example:**
```mathematica
(* REQUIRES REVIEW *)
Import[uploadedFile];              (* Check: file type? size? content validation? *)
Get[userProvidedPath];             (* Check: path sanitization? *)

(* GOOD PRACTICES *)
(* 1. Validate extension *)
allowedExtensions = {".csv", ".json", ".txt"};
If[!MemberQ[allowedExtensions, FileExtension[file]], Return[$Failed]];

(* 2. Check file size *)
If[FileSize[file] > 10*1024*1024, Return[$Failed]];  (* 10MB max *)

(* 3. Use safe formats only *)
Import[uploadedFile, "CSV"];  (* CSV is data-only, not executable *)
```

### 📍 How to View in SonarQube UI

**Method 1: Security Hotspots Tab (Primary)**
1. From project page, click **"Security Hotspots"** in left sidebar
2. → Shows all hotspots categorized by priority
3. Each hotspot shows:
   - **Status**: To Review / Acknowledged / Fixed / Safe
   - **Priority**: High / Medium / Low
   - **Location**: File and line number
4. Click a hotspot to review
5. Mark as **Safe** (if validated properly) or **Fix Required** (if missing safeguards)

**Method 2: Overview → Security Review**
1. From project **Overview** tab
2. Find **"Security Review"** metric (shows percentage and count)
3. Click the count (e.g., "15 hotspots")
4. → Takes you to Security Hotspots tab filtered to "To Review"

**Method 3: Issues Tab (Alternative)**
1. **Issues** tab → Filter **"Type"** → Check **"Security Hotspot"**
2. → Shows all hotspots as issues
3. But Security Hotspots tab provides better workflow

**Workflow for Reviewing Hotspots:**
1. Open hotspot
2. Read the security guidance (shows best practices)
3. Review the actual code
4. Mark status:
   - **Safe** - Code has proper validation
   - **Fix Required** - Missing safeguards
   - **Acknowledged** - Known, will fix later

---

### 5.2 External API Safeguards
**Severity:** MAJOR | **Type:** SECURITY_HOTSPOT
**CWE:** [CWE-400](https://cwe.mitre.org/data/definitions/400.html)

Flags external API calls for review of timeout, rate limiting, and error handling.

**Example:**
```mathematica
(* REQUIRES REVIEW *)
URLRead[apiEndpoint];              (* Check: timeout? error handling? *)
URLExecute["POST", url, data];     (* Check: rate limiting? retry logic? *)

(* GOOD PRACTICES *)
(* 1. Add timeout *)
result = TimeConstrained[
  Check[URLRead[apiEndpoint], (LogError["API failed"]; $Failed)],
  30  (* 30 second timeout *)
];

(* 2. Implement rate limiting *)
If[apiCallCount > maxCallsPerMinute, Pause[60]];

(* 3. Retry with exponential backoff *)
retries = 0;
While[result === $Failed && retries < 3,
  Pause[2^retries];
  result = URLRead[apiEndpoint];
  retries++;
];
```

### 📍 How to View in SonarQube UI

1. **Security Hotspots** tab in left sidebar
2. Filter by **"Category"** → Look for API-related hotspots
3. Review each API call to ensure:
   - Timeout is set
   - Rate limiting is implemented
   - Errors are logged (not exposed to users)
   - Sensitive data is not logged
4. Mark as **Safe** if all safeguards are present

---

### 5.3 Cryptographic Key Generation
**Severity:** MAJOR | **Type:** SECURITY_HOTSPOT
**CWE:** [CWE-326](https://cwe.mitre.org/data/definitions/326.html), [CWE-330](https://cwe.mitre.org/data/definitions/330.html)

Flags cryptographic key/token generation for security review.

**Example:**
```mathematica
(* REQUIRES REVIEW *)
key = Table[Random[], {16}];       (* Check: Is Random[] cryptographically secure? NO! *)
token = ToString[RandomInteger[999999]];  (* Check: Sufficient entropy? NO! *)

(* GOOD PRACTICES *)
(* 1. Use RandomInteger (not Random) *)
aesKey = RandomInteger[{0, 255}, 32];  (* 256-bit key *)

(* 2. Generate secure tokens *)
token = IntegerString[RandomInteger[{10^30, 10^31 - 1}], 16];

(* 3. Sufficient key length *)
(* - Symmetric: 256 bits minimum (AES-256) *)
(* - RSA: 2048 bits minimum *)
(* - ECC: 256 bits minimum *)

(* 4. Store securely *)
Export["/secure/path/key.bin", aesKey, "Byte"];
(* Set restrictive permissions: chmod 600 *)
```

### 📍 How to View in SonarQube UI

1. **Security Hotspots** tab
2. Look for "Cryptographic" category
3. Review each key generation to verify:
   - Using `RandomInteger` (not `Random`)
   - Key length is sufficient (≥256 bits)
   - Keys are stored securely (not in code/logs)
4. Mark as **Safe** if crypto practices are correct
5. Mark as **Fix Required** if using weak crypto

---

## 6. Complexity Metrics

The plugin calculates two types of complexity metrics for every function:

### 6.1 Cyclomatic Complexity
**Formula:** Count of decision points + 1

**Decision Points Counted:**
- `If`, `Which`, `Switch` statements
- `While`, `Do`, `For`, `Table` loops
- `&&`, `||` logical operators
- `/;` (Condition operator)

**Thresholds:**
- **1-10**: Simple function (low risk)
- **11-15**: Moderate complexity (acceptable)
- **16-20**: High complexity (consider refactoring)
- **21+**: Very high complexity (refactor recommended)

**Example:**
```mathematica
(* Cyclomatic Complexity = 5 *)
processData[data_, threshold_] := Module[{result},
  If[Length[data] == 0, Return[$Failed]];        (* +1 *)
  If[threshold < 0, threshold = 10];             (* +1 *)
  result = Select[data, # > threshold &];        (* +0 *)
  If[Length[result] > 100, result = Take[result, 100]];  (* +1 *)
  Which[                                          (* +2 *)
    Length[result] == 0, {},
    Length[result] < 10, result,
    True, Take[result, 10]
  ]
];
(* CC = 1 (base) + 4 (decisions) = 5 *)
```

---

### 6.2 Cognitive Complexity
**Formula:** Complexity + Nesting Penalty

Cognitive Complexity is more sophisticated than Cyclomatic - it penalizes **nested** control structures because they're harder to understand.

**Nesting Rules:**
- Each control structure adds: 1 + current nesting level
- Nesting level increases inside blocks
- Logical operators (`&&`, `||`) add +1 (no nesting penalty)

**Thresholds:**
- **1-10**: Easy to understand
- **11-15**: Acceptable complexity
- **16-25**: Getting difficult (consider refactoring)
- **26+**: Very difficult to understand (refactor strongly recommended)

**Example:**
```mathematica
(* Cognitive Complexity = 9 *)
validateInput[data_] := Module[{errors = {}},
  If[Length[data] == 0,                    (* +1, nesting=0 → score +1 *)
    AppendTo[errors, "Empty data"],
    If[!ListQ[data],                        (* +2, nesting=1 → score +2 *)
      AppendTo[errors, "Not a list"],
      (* Nested deeper *)
      Do[                                    (* +3, nesting=2 → score +3 *)
        If[!NumberQ[data[[i]]],             (* +4, nesting=3 → score +4 *)
          AppendTo[errors, "Non-numeric"]
        ],
        {i, Length[data]}
      ]
    ]
  ];
  errors
];
(* Cognitive Complexity = 1 + 2 + 3 + 4 = 10 *)
(* This is harder to understand than CC suggests due to nesting! *)
```

---

### 📍 How to View Complexity Metrics in SonarQube UI

**Method 1: Code Tab → File View**
1. **Code** tab in left sidebar
2. Navigate to a `.m` file
3. Look for complexity indicators next to function definitions
4. Hover over the metric to see Cyclomatic vs Cognitive values

**Method 2: Issues Tab → Filter by Complexity**
1. **Issues** tab
2. Search: `complexity`
3. → Shows functions exceeding complexity thresholds
4. Each issue shows: `Function 'name' has cyclomatic complexity of 25 (max recommended: 15)`

**Method 3: Measures Tab → Complexity Category**
1. **Measures** tab in left sidebar
2. Click **"Complexity"** category
3. View metrics:
   - **Complexity** - Total complexity across all functions
   - **Cognitive Complexity** - Total cognitive complexity
   - **Complexity per Function** - Average complexity
4. Click any metric to see file-by-file breakdown

**Method 4: Function-Level Details**
1. Go to specific file in **Code** tab
2. Complexity shown per function (if SonarQube version supports it)
3. Functions with complexity > 15 are flagged as issues

**Best Practices:**
- **Cyclomatic Complexity > 15** → Consider splitting function
- **Cognitive Complexity > 15** → Function is hard to understand, refactor
- **High nesting (>3 levels)** → Extract helper functions

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
- **Bugs**: Shows count (e.g., "45") - Our 5 BUG rules findings
  - Click this number → See all bugs
- **Reliability Rating**: Letter grade A-E based on bug severity
  - Click the grade → See rating breakdown

**Security Section:**
- **Vulnerabilities**: Shows count (e.g., "20") - Our 8 security VULNERABILITY rules findings
  - Click this number → See all security issues
- **Security Rating**: Letter grade A-E
  - Click the grade → See rating breakdown
- **Security Review**: Shows percentage reviewed (e.g., "15 to review")
  - Click this → See Security Hotspots from our 3 SECURITY_HOTSPOT rules

**Maintainability Section:**
- **Code Smells**: Shows count (e.g., "300") - Our 8 CODE_SMELL rules findings
  - Click this number → See all code smells
- **Maintainability Rating**: Letter grade A-E
- **Technical Debt**: Estimated time to fix all code smells

**Coverage Section:**
- **Coverage**: N/A (we don't measure test coverage)

**Duplications Section:**
- **Duplications**: Shows percentage (e.g., "5.2%") - From our CPD engine
  - Click the percentage → See all duplicated code blocks

**Complexity Section:**
- **Complexity**: Total cyclomatic complexity
  - Click → See file-by-file complexity breakdown
- **Cognitive Complexity**: Total cognitive complexity
  - Click → See complexity details

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
- **Bugs** - Logic errors → Our 5 BUG rules
- **Vulnerabilities** - Security issues → Our 8 VULNERABILITY rules
- **Code Smells** - Code quality → Our 8 CODE_SMELL rules
- **Security Hotspots** - Manual review locations → Our 3 SECURITY_HOTSPOT rules
- **Duplications** - Copy-pasted code percentage → CPD engine
- **Complexity** - Function complexity → Cyclomatic & Cognitive metrics

**Quick Actions:**
- Click any number to jump to detailed issues
- View **Reliability Rating** (A-E) - Based on bugs
- View **Security Rating** (A-E) - Based on vulnerabilities
- View **Maintainability Rating** (A-E) - Based on code smells

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
- **Reliability** - Bug metrics (our 5 BUG rules)
- **Security** - Vulnerability metrics (our 8 VULNERABILITY rules)
- **Maintainability** - Code smell metrics (our 8 CODE_SMELL rules)
- **Coverage** - Test coverage (not applicable to this plugin)
- **Duplications** - Our CPD engine results
- **Size** - LOC, files, functions counted automatically
- **Complexity** - Cyclomatic & Cognitive complexity metrics

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
- Complexity shown per function (where supported)

---

### Security Hotspots Tab
**What:** Review security-sensitive code locations

**Features:**
- Shows all locations flagged by our 3 SECURITY_HOTSPOT rules
- Categorized by priority (High/Medium/Low)
- Status tracking: To Review / Safe / Fix Required / Acknowledged
- Guidance for each hotspot on what to verify
- Workflow: Review → Mark status → Track progress

---

### Rules Tab
**What:** View and manage all available rules

**Access:**
1. **Quality Profiles** → **Mathematica** → **Sonar way**
2. Or: Top navigation → **Rules** → Language filter → **Mathematica**

**Shows:**
- All 26 active rules:
  - 8 CODE_SMELL rules
  - 8 VULNERABILITY rules
  - 5 BUG rules
  - 3 SECURITY_HOTSPOT rules
  - 2 Complexity metrics (reported as issues when thresholds exceeded)
- Rule descriptions with examples
- CWE/OWASP mappings for security rules
- Compliant/noncompliant code examples

**Actions:**
- Activate/deactivate rules
- Change severities (customize for your team)
- Create custom quality profiles
- Export quality profile

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
The plugin has intelligent performance protections:

**File Length Violations:**
- Files >1,000 lines (configurable) are flagged with "File Length" violation
- These files ARE fully analyzed up to 25,000 lines

**Performance Protection:**
- Files >25,000 lines: Reported for File Length violation, then skip detailed analysis
- Files >1MB: Skip detailed analysis (too large to process efficiently)

This ensures:
- All large files are reported for review
- Extremely large files don't slow down the scan
- Your project scan completes in reasonable time

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
