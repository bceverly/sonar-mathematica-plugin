# Frequently Asked Questions (FAQ)

## Table of Contents

- [General Questions](#general-questions)
- [Installation Questions](#installation-questions)
- [Configuration Questions](#configuration-questions)
- [Rule Questions](#rule-questions)
- [File Format Questions](#file-format-questions)
- [Performance Questions](#performance-questions)
- [Security Questions](#security-questions)
- [Integration Questions](#integration-questions)
- [Custom Rules Questions](#custom-rules-questions)
- [Cost and Licensing Questions](#cost-and-licensing-questions)

---

## General Questions

### 1. What is the SonarQube Mathematica Plugin?

The SonarQube Mathematica Plugin is a comprehensive static analysis tool for Wolfram Mathematica code. It provides:

- **529 code quality rules** covering bugs, vulnerabilities, code smells, and security hotspots
- **Code duplication detection** using SonarQube's CPD engine
- **Complexity metrics** (cyclomatic and cognitive complexity)
- **Quick Fixes** for common issues via SonarLint integration
- **Symbol table analysis** for cross-file variable tracking
- **Performance analysis** optimized for large codebases

The plugin integrates seamlessly with SonarQube's web interface and provides real-time feedback in your IDE through SonarLint.

### 2. How does this plugin compare to Wolfram's built-in code analysis tools?

The plugin complements Wolfram's tools by providing:

**Unique Features:**
- **Continuous integration** - Automated scanning in your CI/CD pipeline
- **Historical tracking** - Track code quality metrics over time
- **Team collaboration** - Centralized code quality dashboard
- **Security scanning** - OWASP Top 10 vulnerability detection
- **Code duplication detection** - Find copy-paste code across projects
- **IDE integration** - Real-time feedback while coding via SonarLint

**Wolfram Workbench** provides syntax checking and debugging, while this plugin focuses on code quality, security, and maintainability analysis. They work together - use Workbench for development and this plugin for continuous quality monitoring.

### 3. What version of SonarQube is required?

**Minimum version:** SonarQube 9.9 or higher

**Recommended versions:**
- SonarQube 10.x (LTS) - Best compatibility and performance
- SonarQube Community Edition - Fully supported (free)
- SonarQube Developer Edition - All features work
- SonarQube Enterprise Edition - All features work

The plugin is tested against SonarQube 10.7 and later versions.

### 4. What programming languages does the plugin support?

The plugin analyzes:

- **Wolfram Language** (.wl files)
- **Mathematica packages** (.m files)
- **Wolfram Language Scripts** (.wls files)
- **Notebook cells** (.nb files) - Coming soon

Currently, the plugin focuses on pure Wolfram Language code. Support for notebooks is planned for a future release.

### 5. Is this an official Wolfram product?

No, this is a **community-developed plugin** not affiliated with Wolfram Research. It is open-source (AGPL-3.0 license) and maintained by the community. However, it follows Wolfram Language best practices and coding standards.

### 6. What are the main benefits of using this plugin?

**For Individual Developers:**
- Catch bugs before they reach production
- Learn Wolfram Language best practices
- Get automated code fixes via Quick Fixes
- Improve code readability and maintainability

**For Teams:**
- Enforce consistent coding standards
- Track technical debt over time
- Security vulnerability scanning
- Code review automation
- Identify code duplication

**For Organizations:**
- Compliance reporting (OWASP, CWE)
- Quality gates for release management
- Historical quality metrics
- CI/CD integration

---

## Installation Questions

### 7. How do I install the plugin?

**Option 1: Marketplace (Recommended)**
1. Login to SonarQube as administrator
2. Go to **Administration → Marketplace → All**
3. Search for "Mathematica"
4. Click **Install**
5. Restart SonarQube

**Option 2: Manual Installation**
1. Download the latest `.jar` from [GitHub Releases](https://github.com/bceverly/wolfralyze/releases)
2. Copy to `$SONARQUBE_HOME/extensions/plugins/`
3. Remove any old versions (important!)
4. Restart SonarQube
5. Verify in **Administration → Marketplace → Installed**

**Option 3: Build from Source**
```bash
git clone https://github.com/bceverly/wolfralyze.git
cd wolfralyze
./gradlew build
cp build/libs/wolfralyze-*.jar $SONARQUBE_HOME/extensions/plugins/
# Restart SonarQube
```

### 8. Why doesn't the plugin appear after installation?

**Common causes:**

1. **Old plugin version still present**
   - Solution: Delete ALL `wolfralyze-*.jar` files from `extensions/plugins/`
   - Then copy only the latest version
   - Restart SonarQube

2. **SonarQube not restarted**
   - Solution: Always restart SonarQube after plugin installation
   - Check logs: `tail -f $SONARQUBE_HOME/logs/sonar.log`

3. **Plugin file permissions**
   - Solution: Ensure the `.jar` file is readable by SonarQube user
   - `chmod 644 wolfralyze-*.jar`

4. **Java version mismatch**
   - Solution: Plugin requires Java 11+
   - Check: `java -version`

5. **Plugin deployment location**
   - Solution: Verify correct directory: `$SONARQUBE_HOME/extensions/plugins/`
   - NOT `extensions/downloads/` (wrong location)

### 9. Can I install the plugin on SonarCloud?

No, **SonarCloud does not support custom plugins**. You must use a self-hosted SonarQube instance (Community, Developer, or Enterprise Edition).

### 10. How do I upgrade to a newer version?

**Safe upgrade process:**

1. **Stop SonarQube**
   ```bash
   $SONARQUBE_HOME/bin/[platform]/sonar.sh stop
   ```

2. **Remove old version**
   ```bash
   rm $SONARQUBE_HOME/extensions/plugins/wolfralyze-*.jar
   ```

3. **Install new version**
   ```bash
   cp wolfralyze-NEW-VERSION.jar $SONARQUBE_HOME/extensions/plugins/
   ```

4. **Start SonarQube**
   ```bash
   $SONARQUBE_HOME/bin/[platform]/sonar.sh start
   ```

5. **Verify installation**
   - Check Administration → Marketplace → Installed
   - Look for correct version number

**Important:** Always remove old versions first to avoid duplicate plugin errors.

---

## Configuration Questions

### 11. How do I configure the plugin for my project?

Create a `sonar-project.properties` file in your project root:

```properties
# Project identification
sonar.projectKey=my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.projectVersion=1.0

# Source code location
sonar.sources=src

# File extensions to analyze
sonar.mathematica.file.suffixes=.m,.wl,.wls

# Encoding
sonar.sourceEncoding=UTF-8

# Exclude generated or third-party code
sonar.exclusions=**/external/**,**/generated/**

# SonarQube server URL
sonar.host.url=http://localhost:9000
```

Then run the scanner:
```bash
sonar-scanner
```

### 12. Which files are analyzed by default?

**Default file extensions:**
- `.m` - Mathematica package files
- `.wl` - Wolfram Language source files
- `.wls` - Wolfram Language script files

**Configuration:**
```properties
# Change file extensions (optional)
sonar.mathematica.file.suffixes=.m,.wl,.wls,.mt
```

**Note:** Notebook files (`.nb`) are not yet supported but planned for future releases.

### 13. How do I exclude files or directories from analysis?

**Exclude by pattern:**
```properties
# Exclude specific directories
sonar.exclusions=**/tests/**,**/examples/**,**/external/**

# Exclude specific files
sonar.exclusions=**/generated/*.wl,**/temp/*.m

# Multiple patterns (comma-separated)
sonar.exclusions=**/tests/**,**/docs/**,**/build/**
```

**Exclude from duplication detection only:**
```properties
sonar.cpd.exclusions=**/generated/**
```

**Exclude from coverage:**
```properties
sonar.coverage.exclusions=**/tests/**
```

### 14. Can I configure rule severity levels?

Yes, through the SonarQube web interface:

1. Go to **Quality Profiles**
2. Select your Mathematica profile (or create a custom one)
3. Search for the rule you want to modify
4. Click **Activate/Deactivate** or **Change Severity**

**Available severities:**
- **Blocker** - Must be fixed immediately
- **Critical** - Should be fixed quickly
- **Major** - Should be fixed (default for most bugs)
- **Minor** - Nice to fix
- **Info** - Informational only

You can also configure rules via JSON/XML files for automated deployment.

### 15. How do I create a custom quality profile?

1. Go to **Quality Profiles**
2. Click **Create**
3. Name: "My Mathematica Profile"
4. Language: Mathematica
5. Parent: "Sonar way" (optional, inherits rules)
6. Click **Create**

Then customize:
- **Activate/Deactivate** specific rules
- **Change severity** levels
- **Set rule parameters** (if available)
- **Export** profile as XML for version control

**Set as default:**
- Click **Set as Default** to apply to all new projects
- Or set per-project in Project Settings → Quality Profiles

### 16. What configuration options are available for performance tuning?

**Memory settings** (in `sonar.properties`):
```properties
# Increase heap size for large codebases
sonar.ce.javaOpts=-Xmx4096m -Xms1024m

# Increase metaspace for many rules
sonar.ce.javaOpts=-XX:MaxMetaspaceSize=512m
```

**Scanner settings** (in `sonar-project.properties`):
```properties
# Skip unchanged files (default: enabled)
sonar.scm.disabled=false

# Duplication detection (adjust for performance)
sonar.cpd.mathematica.minimumTokens=250
sonar.cpd.mathematica.minimumLines=25
```

**Large codebase optimizations:**
- Use incremental analysis (analyze only changed files)
- Exclude test files and examples if not needed
- Consider splitting very large monorepos into multiple projects

---

## Rule Questions

### 17. How many rules does the plugin have?

**Total: 529 rules** (as of version 1.0.0)

**Breakdown by category:**
- **76** Code Smell rules (maintainability)
- **45** Bug rules (reliability)
- **21** Vulnerability rules (security)
- **7** Security Hotspot rules (security review areas)
- **Plus:** Complexity metrics, duplication detection, and symbol table analysis

This exceeds the rule count of Scala (450 rules) and approaches Java's coverage (733 rules), placing it at **Tier 1** language support level.

### 18. Can I customize or disable specific rules?

**Yes!** Three ways:

**1. Via Quality Profile (Recommended)**
- Go to Quality Profiles → Your Profile → Rules
- Search for the rule
- Click **Deactivate** or change severity
- Changes apply to all projects using this profile

**2. Via Project Settings**
- Project → Administration → Quality Profiles
- Select a different profile or create project-specific one

**3. Via Inline Comments (File-level)**
```mathematica
(* NOSONAR - Disable all rules for next line *)
DangerousFunction[];

(* sonar.issue.ignore.multicriteria - More granular control *)
```

**Disable specific rule by key:**
```properties
# In sonar-project.properties
sonar.issue.ignore.multicriteria=e1
sonar.issue.ignore.multicriteria.e1.ruleKey=mathematica:HardcodedCredentials
sonar.issue.ignore.multicriteria.e1.resourceKey=**/config/*.wl
```

### 19. What types of issues does the plugin detect?

**Bug Detection (Reliability):**
- Division by zero
- Null pointer dereferences
- Infinite recursion
- Array index out of bounds
- Type mismatches
- Uninitialized variables
- Unreachable code

**Security Vulnerabilities:**
- SQL injection risks
- Command injection
- Path traversal
- Hardcoded credentials
- Weak cryptography
- XML External Entity (XXE) attacks
- Insecure deserialization

**Security Hotspots (Review Areas):**
- File upload validation
- External API usage
- Cryptographic key generation
- Network connections
- Authentication mechanisms

**Code Smells (Maintainability):**
- Complex functions (high cyclomatic complexity)
- Long functions
- Duplicated code
- Magic numbers
- Nested conditionals
- Poor naming conventions
- Commented-out code

**Mathematica-Specific Issues:**
- Inefficient pattern matching
- Improper use of Hold attributes
- Performance issues (unpacked arrays, etc.)
- Symbol shadowing
- Package dependency cycles

### 20. Are there rules for Mathematica-specific patterns?

**Yes!** The plugin includes 20+ rules for Mathematica-specific idioms:

**Pattern Matching:**
- Inefficient pattern usage
- Overly complex patterns
- Pattern optimization opportunities

**Evaluation Control:**
- Improper use of Hold/HoldAll/HoldFirst
- Missing attribute declarations
- Evaluation leaks

**Performance:**
- Unpacked array operations
- Compilation opportunities
- Listable function usage

**Symbols and Scoping:**
- Symbol shadowing (e.g., redefining `List`)
- Improper use of Module/Block/With
- Global symbol pollution

**Example detected issues:**
```mathematica
(* BAD: Inefficient pattern *)
f[x_] := If[x > 0, x, -x]  (* Use Abs instead *)

(* BAD: Missing Hold attribute *)
SetAttributes[myFunc, {}]  (* Should be HoldAll *)

(* BAD: Symbol shadowing *)
List = 5;  (* Shadowing built-in symbol! *)
```

### 21. Can I see examples of what each rule detects?

**Yes!** Two ways:

**1. In SonarQube UI:**
- Go to Rules → Search for rule name
- Click on rule → See **Description** tab
- **"Noncompliant Code Example"** shows bad code
- **"Compliant Solution"** shows fixed code

**2. In the documentation:**
- Check `docs/rules/` directory in the plugin repository
- Each rule has markdown documentation with examples

**Example rule documentation:**
```markdown
## Rule: HardcodedCredentials

**Severity:** Critical
**Type:** Vulnerability

### Noncompliant Code:
password = "admin123"  (* Hardcoded! *)

### Compliant Solution:
password = Environment["APP_PASSWORD"]
```

### 22. Do rules support Quick Fixes?

**Yes!** 53 rules support automated Quick Fixes via SonarLint:

**Quick Fix examples:**
- Replace `If[x > 0, x, -x]` → `Abs[x]`
- Remove commented-out code
- Simplify Boolean expressions
- Fix incorrect pattern syntax
- Remove unused variables

**How to use:**
1. Install SonarLint in your IDE (IntelliJ, VS Code, Eclipse)
2. Connect to your SonarQube server (Connected Mode)
3. Open a Mathematica file with issues
4. Click **"Quick Fix"** button in the issue tooltip
5. Code is automatically corrected!

**Supported IDEs:**
- IntelliJ IDEA (with SonarLint plugin)
- Visual Studio Code (with SonarLint extension)
- Eclipse (with SonarLint plugin)
- Visual Studio (with SonarLint extension)

### 23. How does the plugin detect code duplication?

The plugin uses **SonarQube's CPD (Copy-Paste Detector)** engine with Mathematica-specific tokenization:

**How it works:**
1. Tokenizes Mathematica code (function names, operators, literals)
2. Normalizes tokens (ignores whitespace, comments, variable names)
3. Compares token sequences across all files
4. Reports duplicated blocks above threshold

**Default thresholds:**
- **Minimum tokens:** 250 tokens
- **Minimum lines:** 25 lines

**Configuration:**
```properties
# Adjust sensitivity
sonar.cpd.mathematica.minimumTokens=100  # More sensitive
sonar.cpd.mathematica.minimumLines=10

# Or less sensitive (for large codebases)
sonar.cpd.mathematica.minimumTokens=500
sonar.cpd.mathematica.minimumLines=50
```

**View duplications:**
- Overview → Click **Duplications %**
- Measures → Duplications → Density

---

## File Format Questions

### 24. Does the plugin support Mathematica notebook files (.nb)?

**Not yet, but planned.** Current status:

**Supported formats:**
- `.wl` - Wolfram Language source files ✅
- `.m` - Package files ✅
- `.wls` - Script files ✅

**Not yet supported:**
- `.nb` - Notebook files (coming in future release)
- `.mx` - Compiled files (not planned)
- `.cdf` - CDF documents (not planned)

**Workaround for notebooks:**
- Export notebook cells to `.wl` files: `Export["code.wl", NotebookRead[...]]`
- Analyze the exported code

**Roadmap:**
- Version 2.0 will support extracting and analyzing code cells from `.nb` files

### 25. Can I analyze package files (.m)?

**Yes!** Package files are fully supported. The plugin correctly handles:

**Package structure:**
- `BeginPackage["MyPackage`"]` declarations
- Context management
- Public/private symbol detection
- Package dependencies

**Example package:**
```mathematica
BeginPackage["MyPackage`"]

(* Public symbols *)
PublicFunction::usage = "Does something";

Begin["`Private`"]

(* Private implementation *)
PublicFunction[x_] := privateHelper[x]
privateHelper[x_] := x^2

End[]
EndPackage[]
```

**Analysis features:**
- Detects unused exports
- Finds package dependency cycles
- Checks public symbol documentation
- Cross-file symbol resolution

### 26. What about Wolfram Language scripts (.wls)?

**Fully supported!** Script files are analyzed just like regular `.wl` files.

**Script features:**
- Shebang line is recognized and ignored: `#!/usr/bin/env wolframscript`
- Command-line argument handling is understood
- Script-specific patterns are detected

**Example script:**
```mathematica
#!/usr/bin/env wolframscript
(* SonarQube analyzes this file *)

result = Total[Range[100]]
Print[result]
```

**Best practices detected:**
- Proper error handling
- Command-line argument validation
- Exit code usage

### 27. How does the plugin handle multi-file projects?

The plugin provides **cross-file analysis** via the Symbol Table:

**Features:**
1. **Package dependency tracking**
   - Detects circular dependencies
   - Finds unused package imports

2. **Cross-file symbol resolution**
   - Tracks variables across files
   - Detects undefined symbols in other packages

3. **Unused export detection**
   - Finds public functions that are never called
   - Identifies dead code across packages

4. **Architecture violations**
   - Enforces layering rules
   - Detects dependency rule violations

**Example:**
```mathematica
(* File: Math.wl *)
BeginPackage["Math`"]
Unused::usage = "Never called"  (* ← Detected! *)
End[]

(* File: Main.wl *)
<< Math`
(* Unused function not called anywhere *)
```

The plugin will report "Unused public symbol: Math`Unused".

### 28. Can I analyze generated code?

**Yes, but you probably shouldn't.** Best practice:

**Exclude generated code:**
```properties
sonar.exclusions=**/generated/**,**/auto/**
```

**Reasons to exclude:**
- Generated code often violates style rules
- Can't be fixed manually (will be regenerated)
- Clutters quality reports
- Slows down analysis

**When to include:**
- If you want to verify generator quality
- If generated code is committed and maintained
- For security scanning (vulnerabilities matter even in generated code)

**Compromise:**
```properties
# Exclude from duplication and code smells
sonar.cpd.exclusions=**/generated/**
sonar.issue.ignore.multicriteria=e1
sonar.issue.ignore.multicriteria.e1.ruleKey=mathematica:*
sonar.issue.ignore.multicriteria.e1.resourceKey=**/generated/**

# But keep security scanning active
```

---

## Performance Questions

### 29. How fast is the plugin?

**Very fast!** Performance characteristics:

**Small projects** (< 100 files):
- Analysis time: **1-3 minutes**
- Dominated by SonarQube overhead

**Medium projects** (100-500 files):
- Analysis time: **3-8 minutes**
- Plugin contributes ~50% of time

**Large projects** (500-1000 files):
- Analysis time: **8-15 minutes**
- Optimized single-pass O(n) analysis

**Performance breakdown** (654 files, ~8.5 minutes):
- **Plugin rules:** 79 seconds (15%)
- **Metrics:** 26 seconds (5%)
- **Duplication:** 153 seconds (30%)
- **SonarQube core:** 250+ seconds (50%)

**Key optimization:** 99.75% improvement from O(400n) → O(n) single-pass analysis using UnifiedRuleVisitor pattern.

### 30. Why is my scan slow?

**Common causes:**

**1. Large individual files**
- **Symptom:** One file takes several minutes
- **Cause:** Symbol table analysis is O(n²) for complex files
- **Solution:** Split large files into smaller modules

**2. Many small files**
- **Symptom:** Scan takes long but individual files are fast
- **Cause:** SonarQube startup overhead per file
- **Solution:** This is normal, no action needed

**3. SCM (Git) blame**
- **Symptom:** "SCM Publisher" step is slow
- **Cause:** Git blame for every line (not plugin-related)
- **Solution:** `sonar.scm.disabled=true` (loses author info)

**4. Duplication detection**
- **Symptom:** CPD step is slow
- **Cause:** Comparing all files for duplications
- **Solution:** Increase thresholds or exclude files

**5. Network latency**
- **Symptom:** "Publishing issues" is slow
- **Cause:** Slow connection to SonarQube server
- **Solution:** Run scanner on same machine or improve network

**6. Memory pressure**
- **Symptom:** GC pauses, OutOfMemoryError
- **Solution:** Increase heap size:
  ```properties
  sonar.ce.javaOpts=-Xmx4096m -Xms1024m
  ```

### 31. Can I run incremental analysis?

**Yes, automatically!** SonarQube supports incremental analysis:

**How it works:**
1. First scan: Analyzes all files
2. Subsequent scans: Only analyzes changed files
3. Unchanged files are skipped automatically

**Requirements:**
- SonarQube 9.9+ (Developer/Enterprise Edition)
- SCM integration enabled (Git, SVN, etc.)
- `sonar.scm.disabled=false` (default)

**Performance improvement:**
- First scan: 1000 files = 120 seconds
- Incremental: 50 changed files = 6 seconds (**20x faster!**)

**Configuration:**
```properties
# Enable incremental analysis (default)
sonar.scm.disabled=false

# For pull requests (Developer Edition+)
sonar.pullrequest.key=PR-123
sonar.pullrequest.branch=feature/my-feature
sonar.pullrequest.base=main
```

### 32. Does the plugin work with large codebases?

**Yes!** Tested on codebases up to **12,000+ files**:

**Largest tested:**
- **Files:** 12,000+ Mathematica files
- **Lines of code:** 500,000+ LOC
- **Analysis time:** ~2 hours (initial scan)
- **Incremental scans:** 5-10 minutes

**Optimizations for large codebases:**

1. **Use incremental analysis** (Developer/Enterprise Edition)
2. **Exclude unnecessary files**:
   ```properties
   sonar.exclusions=**/tests/**,**/examples/**
   ```
3. **Increase memory**:
   ```properties
   sonar.ce.javaOpts=-Xmx8192m -Xms2048m
   ```
4. **Split monorepos** into multiple SonarQube projects
5. **Run analysis during off-hours** (initial scan)

**Architecture:** The plugin uses O(n) single-pass analysis, so it scales linearly with codebase size.

---

## Security Questions

### 33. What security vulnerabilities does the plugin detect?

**OWASP Top 10 2021 coverage** (9 of 10 categories):

**A01: Broken Access Control**
- Hardcoded credentials
- Missing authentication checks

**A02: Cryptographic Failures**
- Weak cryptography algorithms
- Hardcoded encryption keys
- Insecure random number generation

**A03: Injection**
- SQL injection via SQLExecute
- Command injection via RunProcess
- Path traversal via File operations
- XML External Entity (XXE) attacks

**A04: Insecure Design**
- Missing input validation
- Insufficient error handling

**A05: Security Misconfiguration**
- Overly permissive file permissions
- Debug code in production

**A06: Vulnerable and Outdated Components**
- Use of deprecated functions
- Insecure API usage

**A07: Identification and Authentication Failures**
- Weak password validation
- Session management issues

**A08: Software and Data Integrity Failures**
- Insecure deserialization
- Missing integrity checks

**A09: Security Logging and Monitoring Failures**
- Missing audit logs
- Error message information disclosure

**A10: Server-Side Request Forgery (SSRF)**
- (Not currently covered - planned for future release)

### 34. Does the plugin detect hardcoded credentials?

**Yes!** Multiple detection methods:

**Pattern-based detection:**
```mathematica
(* DETECTED: Hardcoded password *)
password = "admin123"
dbPassword = "secret"

(* DETECTED: API keys *)
apiKey = "sk_live_1234567890abcdef"
accessToken = "ghp_xxxxxxxxxxxxxxxxxxxx"

(* DETECTED: Connection strings *)
conn = SQLConnection["jdbc:mysql://localhost/db?user=admin&password=secret"]
```

**Safe alternatives:**
```mathematica
(* GOOD: Environment variables *)
password = Environment["DB_PASSWORD"]

(* GOOD: Configuration files (outside repo) *)
config = Import["config.json"];
password = config["database"]["password"]

(* GOOD: Secrets management *)
password = CloudGet["mysecret/password"]
```

**Rule:** `mathematica:HardcodedCredentials` (Severity: Critical)

### 35. Can the plugin help with compliance requirements?

**Yes!** The plugin supports:

**OWASP Top 10 compliance:**
- All vulnerability rules are tagged with OWASP categories
- Security Hotspots highlight review areas
- Generate compliance reports via SonarQube

**CWE (Common Weakness Enumeration) mapping:**
- Each vulnerability maps to CWE IDs
- Useful for CVE reporting
- Required by some compliance frameworks

**PCI-DSS:**
- Detects hardcoded credentials (requirement 6.5.3)
- SQL injection prevention (requirement 6.5.1)
- Proper error handling (requirement 6.5.5)

**Generate compliance reports:**
1. Go to project → **More → Security Reports**
2. Select **OWASP Top 10** or **CWE Top 25**
3. Export as PDF or CSV

**Quality Gates for compliance:**
```properties
# Fail builds if security issues found
sonar.qualitygate.wait=true
sonar.qualitygate.timeout=300
```

Then configure Quality Gate:
- **Vulnerabilities:** 0 allowed (fail if any found)
- **Security Hotspots:** 100% reviewed
- **Security Rating:** A (no vulnerabilities)

### 36. Are Security Hotspots the same as vulnerabilities?

**No, they're different:**

**Vulnerabilities:**
- **Definite security issues** that should be fixed
- Example: SQL injection, hardcoded credentials
- **Severity:** Blocker/Critical/Major
- **Action:** Fix immediately
- **Location:** Issues tab → Filter: Type=Vulnerability

**Security Hotspots:**
- **Code that requires security review** (may or may not be vulnerable)
- Example: File upload functionality, external API calls
- **Severity:** (Not rated - requires review)
- **Action:** Review and mark as "Safe" or "Fixed"
- **Location:** Security Hotspots tab (separate tab)

**Example:**
```mathematica
(* VULNERABILITY: SQL Injection *)
SQLExecute[conn, "SELECT * FROM users WHERE id = " <> id]
(* ↑ Definite issue - concatenating user input *)

(* SECURITY HOTSPOT: File Upload *)
Import[uploadedFile]
(* ↑ Requires review - may be safe if properly validated *)
```

**Workflow:**
1. Fix all **Vulnerabilities** immediately
2. **Review** all Security Hotspots
3. Mark as "Safe" (with justification) or "Fixed"
4. Track review progress in Security Hotspots tab

---

## Integration Questions

### 37. Can I use the plugin with my IDE?

**Yes!** Via SonarLint integration:

**Supported IDEs:**
- **IntelliJ IDEA** (all editions)
- **Visual Studio Code**
- **Eclipse**
- **Visual Studio** (Windows)

**Setup (IntelliJ IDEA example):**

1. **Install SonarLint plugin:**
   - Preferences → Plugins → Marketplace
   - Search "SonarLint"
   - Install and restart

2. **Connect to SonarQube (Connected Mode):**
   - Preferences → Tools → SonarLint → Settings
   - Click "+" to add connection
   - Enter SonarQube URL: `http://localhost:9000`
   - Generate token in SonarQube: User → My Account → Security → Generate Token
   - Paste token in SonarLint

3. **Bind project:**
   - Right-click project → SonarLint → Bind to SonarQube/SonarCloud
   - Select your SonarQube server
   - Choose project
   - Click "Bind"

4. **Use Quick Fixes:**
   - Open a Mathematica file
   - See issues highlighted in editor
   - Click "Quick Fix" button (lightbulb icon)
   - Code is automatically corrected!

**Benefits:**
- Real-time feedback while coding
- No need to run full scan
- Quick Fix integration (53 rules)
- Consistent rules with SonarQube server

### 38. How do I integrate with CI/CD pipelines?

**GitHub Actions example:**

```yaml
name: SonarQube Analysis

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Full history for better analysis

      - name: SonarQube Scan
        uses: sonarsource/sonarqube-scan-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}

      - name: SonarQube Quality Gate
        uses: sonarsource/sonarqube-quality-gate-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**GitLab CI example:**

```yaml
sonarqube-check:
  image: sonarsource/sonar-scanner-cli:latest
  variables:
    SONAR_USER_HOME: "${CI_PROJECT_DIR}/.sonar"
  script:
    - sonar-scanner
      -Dsonar.projectKey=$CI_PROJECT_NAME
      -Dsonar.sources=.
      -Dsonar.host.url=$SONAR_HOST_URL
      -Dsonar.login=$SONAR_TOKEN
  only:
    - main
    - merge_requests
```

**Jenkins example:**

```groovy
pipeline {
    agent any
    stages {
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'sonar-scanner'
                }
            }
        }
        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
```

**Azure Pipelines example:**

```yaml
trigger:
  - main

pool:
  vmImage: 'ubuntu-latest'

steps:
  - task: SonarQubePrepare@5
    inputs:
      SonarQube: 'SonarQube Connection'
      scannerMode: 'CLI'
      configMode: 'file'

  - task: SonarQubeAnalyze@5

  - task: SonarQubePublish@5
    inputs:
      pollingTimeoutSec: '300'
```

### 39. Can I use the plugin with Wolfram Workbench?

**Not directly, but complementary:**

**Wolfram Workbench:**
- Real-time syntax checking
- Debugging
- Auto-completion
- Refactoring tools

**SonarQube Mathematica Plugin:**
- Code quality analysis
- Security scanning
- Historical metrics
- Team collaboration

**Recommended workflow:**

1. **Develop in Workbench:**
   - Write code with syntax checking
   - Use debugger
   - Run unit tests

2. **Commit to Git:**
   - Push changes

3. **CI/CD runs SonarQube:**
   - Automated quality scan
   - Security analysis
   - Quality gate check

4. **Review in SonarQube UI:**
   - Check issues
   - Track technical debt
   - View trends

5. **Fix issues in Workbench:**
   - Use SonarLint for real-time feedback (if IDE supported)
   - Or manually fix based on SonarQube report

### 40. Does the plugin work with Docker?

**Yes!** Example Docker setup:

**docker-compose.yml:**
```yaml
version: '3'
services:
  sonarqube:
    image: sonarqube:10-community
    ports:
      - "9000:9000"
    environment:
      - SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_extensions:/opt/sonarqube/extensions
      - sonarqube_logs:/opt/sonarqube/logs
      # Mount plugin
      - ./wolfralyze.jar:/opt/sonarqube/extensions/plugins/wolfralyze.jar

volumes:
  sonarqube_data:
  sonarqube_extensions:
  sonarqube_logs:
```

**Start SonarQube:**
```bash
docker-compose up -d
```

**Run scanner (from project directory):**
```bash
docker run --rm \
  --network="host" \
  -v "$(pwd):/usr/src" \
  sonarsource/sonar-scanner-cli \
  -Dsonar.projectKey=my-project \
  -Dsonar.sources=. \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_TOKEN
```

**Install plugin in running container:**
```bash
docker cp wolfralyze.jar sonarqube:/opt/sonarqube/extensions/plugins/
docker restart sonarqube
```

---

## Custom Rules Questions

### 41. Can I create custom rules?

**Yes!** Three approaches:

**1. Rule Templates (Easy)**
- Use existing rule templates in SonarQube
- Configure parameters via UI
- No coding required

**2. External Rules (Medium)**
- Write rules in external tool
- Import findings via Generic Issue Import format
- Requires external tool development

**3. Plugin Extension (Advanced)**
- Fork the plugin repository
- Add custom rules in Java
- Rebuild and deploy plugin

**Rule Template example:**
1. Go to Rules → Search "template"
2. Find a rule template (e.g., "Custom pattern detector")
3. Click **Create**
4. Configure parameters:
   - **Name:** "Detect Legacy API"
   - **Pattern:** `OldFunction\[`
   - **Message:** "Use NewFunction instead"
5. Activate in Quality Profile

### 42. How do I contribute new rules to the plugin?

**Contribution process:**

1. **Propose rule:**
   - Open GitHub Issue with rule proposal
   - Include: Name, description, examples, rationale
   - Wait for community feedback

2. **Implement rule:**
   - Fork repository: `github.com/bceverly/wolfralyze`
   - Create branch: `git checkout -b feature/my-rule`
   - Add rule in `UnifiedRuleVisitor.java` (for O(n) rules)
   - Add rule definition in `MathematicaRulesDefinition.java`
   - Add tests in `src/test/java/`

3. **Test rule:**
   - Run unit tests: `./gradlew test`
   - Build plugin: `./gradlew build`
   - Test on real codebase

4. **Submit PR:**
   - Push branch: `git push origin feature/my-rule`
   - Create Pull Request on GitHub
   - Respond to code review feedback

5. **Documentation:**
   - Add rule documentation in `docs/rules/`
   - Include examples (noncompliant and compliant)
   - Update changelog

**Example rule implementation:**
```java
// In UnifiedRuleVisitor.java
private void checkMyCustomRule(FunctionCallNode node) {
    if (node.getFunctionName().equals("DeprecatedFunc")) {
        reportIssue(
            node.getStartLine(),
            "MyCustomRule",
            "Avoid using DeprecatedFunc; use NewFunc instead."
        );
    }
}
```

### 43. Can I import issues from external tools?

**Yes!** Via Generic Issue Import format:

**1. Generate JSON file:**
```json
{
  "issues": [
    {
      "engineId": "myanalyzer",
      "ruleId": "MY-001",
      "severity": "MAJOR",
      "type": "BUG",
      "primaryLocation": {
        "message": "Issue description",
        "filePath": "src/MyFile.wl",
        "textRange": {
          "startLine": 10,
          "endLine": 10,
          "startColumn": 1,
          "endColumn": 20
        }
      }
    }
  ]
}
```

**2. Import into SonarQube:**
```properties
# In sonar-project.properties
sonar.externalIssuesReportPaths=issues.json
```

**3. Run scanner:**
```bash
sonar-scanner
```

**Issues appear in:**
- Issues tab → Filter: Rules → External
- Labeled with source: "myanalyzer:MY-001"

**Use cases:**
- Import findings from Wolfram Workbench
- Custom static analysis tools
- Third-party scanners

---

## Cost and Licensing Questions

### 44. Is the plugin free to use?

**Yes! The plugin is 100% free and open-source:**

- **License:** AGPL-3.0 (GNU General Public License v3.0)
- **Cost:** $0 (free forever)
- **Source code:** Available on GitHub
- **Commercial use:** Allowed
- **Modification:** Allowed
- **Distribution:** Allowed (with same AGPL-3.0 license)

**SonarQube editions:**
- **Community Edition:** Free (unlimited projects)
- **Developer Edition:** Paid (additional features)
- **Enterprise Edition:** Paid (advanced features)

**Plugin works with all editions**, including the free Community Edition.

### 45. What SonarQube edition do I need?

**Community Edition (Free):**
- ✅ All 529 rules
- ✅ Code quality analysis
- ✅ Security scanning
- ✅ Duplication detection
- ✅ Complexity metrics
- ✅ Quality gates
- ❌ Branch analysis (main branch only)
- ❌ Pull request analysis
- ❌ Portfolio management

**Developer Edition (Paid):**
- ✅ Everything in Community
- ✅ **Branch analysis** (analyze feature branches)
- ✅ **Pull request analysis** (PR decoration)
- ✅ Incremental analysis (20x faster)
- ✅ Taint analysis (advanced security)
- ✅ 8 languages supported

**Enterprise Edition (Paid):**
- ✅ Everything in Developer
- ✅ **Portfolio management** (aggregate metrics)
- ✅ Advanced security features
- ✅ More languages
- ✅ Enterprise support

**Recommendation:**
- **Individuals/small teams:** Community Edition (free)
- **Teams using Git branches/PRs:** Developer Edition
- **Large organizations:** Enterprise Edition

### 46. Can I use this in a commercial product?

**Yes!** The AGPL-3.0 license allows commercial use with conditions:

**You CAN:**
- ✅ Use in commercial projects
- ✅ Analyze proprietary code
- ✅ Use in corporate environments
- ✅ Charge customers for services using the plugin

**You MUST:**
- ⚠️ Keep AGPL-3.0 license if redistributing
- ⚠️ Provide source code if distributing modified plugin
- ⚠️ Include license and copyright notices

**You DON'T need to:**
- ✅ Open-source your analyzed code (code being analyzed is separate)
- ✅ Pay licensing fees
- ✅ Get permission (it's open-source!)

**Clarification:**
- **Analyzing commercial code:** No restrictions (just like using GCC to compile proprietary software)
- **Modifying and distributing plugin:** Must share modifications under AGPL-3.0

### 47. Who maintains the plugin?

**Community-maintained:**

- **Primary maintainer:** Brian Ceverly (GitHub: @bceverly)
- **Contributors:** Community contributions welcome
- **Organization:** Not affiliated with Wolfram Research
- **Support:** Community forums, GitHub Issues
- **Updates:** Regular releases on GitHub

**Contributing:**
- Report bugs: GitHub Issues
- Suggest features: GitHub Discussions
- Submit code: Pull Requests
- Documentation: Wiki contributions

**Commercial support:**
- Not officially available
- Community support via GitHub
- Consider Enterprise SonarQube for general SonarQube support

### 48. What's the roadmap for future features?

**Planned features (Version 2.0):**

1. **Notebook support (.nb files)**
   - Extract and analyze code cells
   - Ignore text/markdown cells
   - Handle notebook-specific constructs

2. **Enhanced type inference**
   - Better nullable analysis
   - Type propagation through functions
   - Generic type support

3. **More Quick Fixes**
   - Expand from 53 to 100+ fixes
   - Complex refactorings
   - Multi-line fixes

4. **Performance analysis**
   - Detect compilation opportunities
   - Memory usage analysis
   - Benchmark recommendations

5. **Test coverage integration**
   - Parse VerificationTest results
   - Import coverage XML
   - Coverage-based rules

**Long-term (Version 3.0+):**
- AI-powered issue detection
- Custom rule DSL (no Java required)
- Cloud-based analysis
- Integration with Wolfram Cloud

**See full roadmap:** `ROADMAP.md` in repository

### 49. How do I get support?

**Community support (Free):**

1. **GitHub Issues:** Report bugs, request features
   - URL: `github.com/bceverly/wolfralyze/issues`
   - Response time: 1-5 business days

2. **GitHub Discussions:** Ask questions, share ideas
   - URL: `github.com/bceverly/wolfralyze/discussions`
   - Community-driven

3. **Documentation:**
   - `README.md` - User guide
   - `ARCHITECTURE.md` - Technical details
   - `FAQ.md` - This document
   - `Troubleshooting.md` - Common issues

4. **SonarQube Community Forum:**
   - URL: `community.sonarsource.com`
   - Tag: `mathematica`

**Professional support (Paid):**
- Contact SonarSource for Enterprise support (covers SonarQube, not plugin-specific)
- Consider hiring consultants for custom development

**Response expectations:**
- Bug reports: 1-2 weeks for fix
- Feature requests: Evaluated quarterly
- Security issues: 24-48 hours (email maintainer)

### 50. Can I contribute to the project?

**Yes! Contributions welcome:**

**Ways to contribute:**

1. **Code contributions:**
   - New rules
   - Bug fixes
   - Performance improvements
   - Test coverage

2. **Documentation:**
   - Improve README
   - Add examples
   - Translate docs
   - Write tutorials

3. **Testing:**
   - Test on large codebases
   - Report bugs
   - Verify fixes

4. **Community:**
   - Answer questions in Discussions
   - Share use cases
   - Write blog posts

**Getting started:**
1. Read `CONTRIBUTING.md` (coming soon)
2. Check "good first issue" labels on GitHub
3. Fork repository
4. Make changes
5. Submit Pull Request

**Recognition:**
- Contributors listed in CHANGELOG
- Credit in release notes
- GitHub contributor badge

**Thank you for using the SonarQube Mathematica Plugin!**
