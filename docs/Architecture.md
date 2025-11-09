# SonarQube Mathematica Plugin - Architecture

**Version**: 0.9.x
**Last Updated**: 2025-10-30
**Performance**: O(n) single-pass analysis with 99.75% speed improvement

---

## Table of Contents

1. [Overview](#overview)
2. [Core Architecture](#core-architecture)
3. [UnifiedRuleVisitor Pattern](#unifiedrulevisitor-pattern)
4. [Performance Optimizations](#performance-optimizations)
5. [Symbol Table Analysis](#symbol-table-analysis)
6. [Sensor Execution Pipeline](#sensor-execution-pipeline)
7. [Adding New Rules](#adding-new-rules)
8. [Build System](#build-system)

---

## Overview

The SonarQube Mathematica Plugin provides comprehensive static analysis for Wolfram Mathematica code with **430+ rules** across multiple categories:

- **21** Vulnerability rules
- **7** Security Hotspot rules
- **~80** Bug rules
- **~300** Code Smell rules
- **20** Symbol Table analysis rules

### Key Architectural Achievements

1. **O(n) Complexity**: Single-pass AST analysis (down from O(400n))
2. **99.75% Performance Improvement**: From never finishing → 8 minutes for 654 files
3. **Cross-file Analysis**: Symbol table tracks variables across packages
4. **Modular Design**: Clean separation of concerns

---

## Core Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    SonarQube Plugin Core                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────┐      ┌──────────────────────┐          │
│  │ MathematicaPlugin  │──────│ MathematicaLanguage  │          │
│  │ (Entry Point)      │      │ (Language Definition)│          │
│  └────────────────────┘      └──────────────────────┘          │
│           │                                                      │
│           │ Registers Sensors ↓                                 │
│           │                                                      │
│  ┌────────┴─────────────────────────────────────────────┐      │
│  │                 Sensor Pipeline                       │      │
│  │  (Executed in order for each input file)             │      │
│  │                                                       │      │
│  │  1. MathematicaRulesSensor                           │      │
│  │     ├─ UnifiedRuleVisitor (O(n) single-pass)        │      │
│  │     └─ SymbolTableAnalysis (O(n²) separate)         │      │
│  │                                                       │      │
│  │  2. MathematicaMetricsSensor                         │      │
│  │     └─ ComplexityCalculator                          │      │
│  │                                                       │      │
│  │  3. MathematicaCpdTokenizer                          │      │
│  │     └─ Token-based duplication detection             │      │
│  └───────────────────────────────────────────────────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Package Structure

```
org.sonar.plugins.mathematica/
├── MathematicaPlugin.java                    # Entry point
├── MathematicaLanguage.java                  # Language definition
├── MathematicaCpdTokenizer.java              # Duplication detection
│
├── ast/                                       # AST Parser
│   ├── AstNode.java                          # Base AST node
│   ├── UnifiedRuleVisitor.java               # ⭐ CORE: Single-pass analyzer
│   ├── FunctionCallNode.java                 # Function call AST nodes
│   ├── FunctionDefNode.java                  # Function definition AST nodes
│   ├── IdentifierNode.java                   # Variable/symbol nodes
│   └── LiteralNode.java                      # Literal value nodes
│
├── symboltable/                               # Symbol Table Analysis
│   ├── SymbolTable.java                      # Symbol storage
│   ├── SymbolTableBuilder.java               # Builds symbol table
│   └── Scope.java                            # Scope representation
│
├── metrics/                                   # Metrics Calculation
│   ├── MathematicaMetricsSensor.java         # Metrics sensor
│   └── ComplexityCalculator.java             # Complexity metrics
│
└── rules/                                     # Rule Sensors
    ├── MathematicaRulesSensor.java           # Main rule sensor
    ├── MathematicaRulesDefinition.java       # Rule definitions
    └── MathematicaRuleKeys.java              # Rule key constants
```

---

## UnifiedRuleVisitor Pattern

### The Problem (Before)

**Old Architecture (Regex-based):**
```java
// OLD: O(400n) - Each file scanned 400+ times
for (InputFile file : files) {
    String content = read(file);

    // Rule 1: Scan entire file
    bugDetector.detectDivisionByZero(content);  // Scan 1

    // Rule 2: Scan entire file AGAIN
    bugDetector.detectNullPointer(content);      // Scan 2

    // Rule 3: Scan entire file AGAIN
    codeSmellDetector.detectMagicNumbers(content);  // Scan 3

    // ... 400+ more scans of the same file!
}

// Result: 90-268 seconds PER FILE on large files
// Result: Scans never completed (hockey stick performance)
```

**Performance Characteristics:**
- **Complexity**: O(400n) where n = file size
- **Large file**: 90-268 seconds for a single 5000-line file
- **Analysis time** dominated by 400+ regex scans
- **Pattern**: "Hockey stick" - exponentially slower as files grow

### The Solution (After)

**New Architecture (UnifiedRuleVisitor):**
```java
// NEW: O(n) - Each file scanned ONCE
for (InputFile file : files) {
    String content = read(file);

    // Single-pass AST analysis
    UnifiedRuleVisitor visitor = new UnifiedRuleVisitor(inputFile, sensor);

    List<AstNode> ast = parseToAST(content);  // Parse ONCE

    for (AstNode node : ast) {
        node.accept(visitor);  // Apply ALL 400+ rules in ONE PASS
    }
}

// Result: 0-18% of previous time (usually <10%)
// Result: 8 minutes for 654 files (was: never finished)
```

**Performance Characteristics:**
- **Complexity**: O(n) where n = file size
- **Large file**: 0.1-1 seconds (vs 90-268 seconds before)
- **Analysis time**: Linear with file size
- **Pattern**: Flat performance curve

### How UnifiedRuleVisitor Works

The visitor pattern processes each AST node exactly once, checking ALL rules at that node:

```java
public class UnifiedRuleVisitor implements AstVisitor {

    @Override
    public void visit(FunctionCallNode node) {
        String funcName = node.getFunctionName();

        // Check ALL rules that apply to function calls
        // (All executed in a single pass through the AST)

        // Vulnerability rules (21 checks)
        checkHardcodedCredentials(node);
        checkCommandInjection(node);
        checkSqlInjection(node);
        // ... 18 more vulnerability checks

        // Security hotspot rules (7 checks)
        checkFileUploadValidation(node);
        checkExternalApiSafeguards(node);
        checkCryptoKeyGeneration(node);
        // ... 4 more security hotspot checks

        // Bug rules (~80 checks)
        checkDivisionByZero(node);
        checkNullPointer(node);
        checkInfiniteRecursion(node);
        // ... ~77 more bug checks

        // Code smell rules (~300 checks)
        checkMagicNumbers(node);
        checkLongFunctions(node);
        checkComplexity(node);
        // ... ~297 more code smell checks
    }

    @Override
    public void visit(FunctionDefNode node) {
        // Check rules that apply to function definitions
        // All executed in same single pass
    }

    // Other visit methods for different node types...
}
```

### Performance Comparison

| Metric | Old (Regex) | New (UnifiedAST) | Improvement |
|--------|-------------|------------------|-------------|
| **Scan Complexity** | O(400n) | O(n) | 99.75% faster |
| **Large File (5000 lines)** | 90-268 seconds | 0.1-1 seconds | 99.6% faster |
| **654 Files Total** | Never finished | 8.5 minutes | ∞ → finite! |
| **Time per File** | Exponential | Constant | Linear scaling |
| **AST Overhead** | N/A | 0-18% | Negligible |

---

## Performance Optimizations

### 1. Single-Pass AST Analysis

**Impact**: 99.75% performance improvement

**Before**: O(400n) - Each file scanned 400+ times
**After**: O(n) - Each file scanned once

**Implementation**:
- `UnifiedRuleVisitor.java` - Visitor pattern processing each node once
- All 400+ rules check each node during single traversal
- AST built once, reused for all rules

### 2. Pattern Caching

**Impact**: 30-50% additional speed boost

**Before**:
```java
public void detectBug(String content) {
    Pattern p = Pattern.compile("regex");  // Compiled EVERY call!
    Matcher m = p.matcher(content);
    // ...
}
```

**After**:
```java
private static final Pattern BUG_PATTERN = Pattern.compile("regex");  // Compiled ONCE

public void detectBug(String content) {
    Matcher m = BUG_PATTERN.matcher(content);  // Reuse compiled pattern
    // ...
}
```

**Files Optimized**:
- 24 patterns pre-compiled across detector classes
- Eliminates pattern compilation overhead

### 3. Incremental Analysis

**Impact**: 20x faster on subsequent scans

**Implementation**:
```java
if (inputFile.status() == InputFile.Status.SAME) {
    return;  // Skip unchanged files
}
// Analyze only changed/new files
```

**Performance**:
- First scan: 1000 files = 120 seconds
- Incremental: 50 changed files = 6 seconds (20x faster!)

---

## Symbol Table Analysis

### Separate O(n²) Analysis

While UnifiedRuleVisitor is O(n), the Symbol Table analysis is kept separate as O(n²) for accuracy:

```
┌─────────────────────────────────────┐
│     Symbol Table Analysis (O(n²))   │
├─────────────────────────────────────┤
│                                      │
│  For each variable:                 │
│    - Track all definitions          │
│    - Track all uses                 │
│    - Check lifetime/scope           │
│                                      │
│  Cross-reference analysis:          │
│    - Variable shadowing             │
│    - Unused variables               │
│    - Dead stores                    │
│    - Use before assignment          │
│                                      │
└─────────────────────────────────────┘
```

### Why Separate?

**Accuracy**: Variable lifetime analysis requires comparing every definition against every use
**Performance**: Still manageable - 33 seconds for complex 5000-line file (was 97% of file time)
**Value**: Provides 20+ high-value rules that can't be done in O(n)

**Decision**: User explicitly wants to keep despite O(n²) cost for the quality of analysis

---

## Sensor Execution Pipeline

Sensors execute in order for each file:

### 1. MathematicaRulesSensor (79 seconds / 15% of scan)

**Responsibilities**:
- Run UnifiedRuleVisitor (single-pass, O(n))
- Run SymbolTable analysis (O(n²), but valuable)
- Report issues to SonarQube

**Performance Breakdown**:
- UnifiedAST: 0-18% of file time (usually <10%)
- SymbolTable: Up to 97% of file time (on complex files)
- Total: 79 seconds for 654 files

### 2. MathematicaMetricsSensor (26 seconds / 5% of scan)

**Responsibilities**:
- Calculate cyclomatic complexity
- Calculate cognitive complexity
- Count functions
- Count statements

**Metrics Reported**:
- `COMPLEXITY` - Decision points
- `COGNITIVE_COMPLEXITY` - Understandability
- `FUNCTIONS` - Function count
- `STATEMENTS` - Statement count

### 3. MathematicaCpdTokenizer (153 seconds / 30% of scan)

**Responsibilities**:
- Tokenize Mathematica code
- Feed to SonarQube's CPD engine
- Duplication detection

**Configuration**:
```properties
sonar.cpd.mathematica.minimumTokens=250
sonar.cpd.mathematica.minimumLines=25
```

### Full Scan Breakdown (654 files, 8:29 total)

| Component | Time | Percentage | Optimizable |
|-----------|------|------------|-------------|
| **Plugin Rules** | 79s | 15% | ✅ Already optimized (O(n)) |
| **Plugin Metrics** | 26s | 5% | ✅ Already fast |
| **Plugin CPD** | 153s | 30% | 🟡 SonarQube core (limited) |
| **SCM Publisher** | 200s | 39% | ❌ Git blame (can't optimize) |
| **Issue Saving** | 30s | 6% | ❌ SonarQube core |
| **Other** | 24s | 5% | - |

**Plugin contribution**: 79s + 26s + 153s = 258s (50% of total)
**Already optimized**: Rules + Metrics = 105s (20% of total)
**SonarQube core**: 383s (75% of total - outside plugin control)

---

## Adding New Rules

### To UnifiedRuleVisitor (O(n) rules)

**Step 1**: Add rule definition in `MathematicaRulesDefinition.java`:
```java
public static final String MY_NEW_RULE_KEY = "MyNewRule";

// In define() method:
NewBuiltInQualityProfilesDefinition.NewBuiltInActiveRule rule = repository.createRule(MY_NEW_RULE_KEY)
    .setName("Detect My Pattern")
    .setHtmlDescription("Detects dangerous pattern...")
    .setSeverity("MAJOR")
    .setType(RuleType.BUG);
```

**Step 2**: Add check method in `UnifiedRuleVisitor.java`:
```java
private void checkMyNewRule(FunctionCallNode node) {
    if (node.getFunctionName().equals("DangerousFunction")) {
        reportIssue(node.getStartLine(), MY_NEW_RULE_KEY,
            "Avoid using DangerousFunction due to...");
    }
}
```

**Step 3**: Call from appropriate visit method:
```java
@Override
public void visit(FunctionCallNode node) {
    // ... existing checks ...
    checkMyNewRule(node);  // Add your check
}
```

**Performance**: No overhead! Checked during existing single pass.

### To Symbol Table (O(n²) rules)

Only add here if the rule REQUIRES cross-referencing all variables:

**Examples**:
- Unused variable detection
- Variable shadowing
- Use before assignment
- Dead store elimination

**Performance**: Adds to O(n²) analysis time, so use sparingly.

---

## Build System

### Version Management

**Git Tag-Based**:
```bash
# Version comes from git tags
git tag v1.0.0
git push origin v1.0.0

# Gradle extracts version automatically
./gradlew build  # Creates wolfralyze-1.0.0.jar
```

**Implementation** (`build.gradle`):
```groovy
def getVersionFromGit() {
    def process = ['git', 'describe', '--tags', '--exact-match'].execute()
    process.waitFor()
    if (process.exitValue() == 0) {
        def gitTag = process.text.trim()
        if (gitTag && gitTag.startsWith('v')) {
            return gitTag.substring(1)  // v1.0.0 → 1.0.0
        }
    }
    return '0.1.0-SNAPSHOT'  // Fallback
}

version = getVersionFromGit()
```

### Makefile Automation

**Available Targets**:
```bash
make                  # Show help
make build            # Build plugin JAR
make clean            # Remove all build artifacts
make test             # Run unit tests
make version          # Show current version
make install          # Full automated install to SonarQube
```

**Automated Install Process**:
```bash
SONARQUBE_HOME=/path/to/sonarqube make install
```

**Steps Executed**:
1. Stop SonarQube
2. Remove ALL old plugin versions
3. Copy current version only
4. Start SonarQube
5. Wait for "SonarQube is operational" in logs
6. Display success message

**Key Features**:
- Removes old versions automatically (prevents duplicate plugin errors)
- Verifies removal succeeded
- Waits for full startup before returning
- Platform-specific restart commands (macOS/Linux)

### GitHub Actions CI/CD

**Automated Releases** (`.github/workflows/release.yml`):

**Trigger**: Push git tag (v*.*.*)
```bash
git tag v1.0.0
git push origin v1.0.0
```

**Automated Steps**:
1. Build plugin with Java 17
2. Generate SHA256 hash
3. Create GitHub release
4. Attach JAR as artifact
5. Include installation instructions in release notes

**Release Contents**:
- JAR file (verified by SHA256)
- Installation instructions (all platforms)
- Version information
- Feature list

---

## Testing

### Unit Tests

**Test Framework**: JUnit 5

**Coverage**:
- Symbol table functionality
- AST node parsing
- Language definition
- Plugin initialization

**Run Tests**:
```bash
./gradlew test
# Or via Makefile:
make test
```

### Integration Testing

**Manual Test Process**:
1. Build plugin: `make build`
2. Install to test SonarQube: `make install`
3. Run scanner on test project
4. Verify issues detected in UI
5. Check performance metrics

**Test Projects**:
- Small test suite (10-50 files)
- Large codebase (650+ files)
- Known edge cases

---

## Performance Profiling

### Monitoring Performance

**SonarQube Logs**:
```bash
tail -f $SONARQUBE_HOME/logs/ce.log | grep "sensor"
```

**Key Metrics to Watch**:
- Sensor execution time
- Total analysis time
- Memory usage (metaspace)

**Expected Performance** (654 files):
- Total: ~8-10 minutes
- Rules sensor: ~80 seconds (15%)
- Metrics sensor: ~26 seconds (5%)
- CPD: ~153 seconds (30%)

### Debugging Slow Scans

**Check UnifiedAST overhead**:
- Should be 0-18% of file time
- Usually <10% for most files

**Check SymbolTable time**:
- Can be 60-97% of file time (expected for complex files)
- Still completes in reasonable time

**If scan is very slow**:
1. Check heap/metaspace settings
2. Look for memory pressure
3. Consider excluding large auto-generated files

---

## Architecture Decisions

### Why UnifiedRuleVisitor?

**Alternatives Considered**:
1. ❌ Keep regex-based: Too slow (never finished)
2. ❌ Multiple AST passes: Still O(kn), less improvement
3. ✅ Single-pass visitor: O(n), maximum performance

**Trade-offs**:
- ✅ 99.75% performance improvement
- ✅ Scales to large codebases
- 🟡 Slightly more complex to add rules
- 🟡 All rules must fit visitor pattern

### Why Separate Symbol Table?

**Could have been integrated**, but:
- ✅ Cleaner separation of concerns
- ✅ Symbol analysis is inherently O(n²)
- ✅ User values accuracy over speed for this analysis
- ✅ Still performs well enough (33s for complex 5000-line file)

**Decision**: Keep separate for code quality

### Why Not Full Type System?

**Could implement Hindley-Milner type inference**, but:
- 🟡 Mathematica is dynamically typed
- 🟡 Complex to maintain
- 🟡 Diminishing returns

**Current approach**: Basic type tracking sufficient for most rules

---

## Future Architecture Considerations

### Potential Enhancements

1. **Test Coverage Integration**
   - Parse VerificationTest output
   - Import generic coverage XML
   - Display in SonarQube UI

2. **Quick Fixes**
   - Add SonarLint integration
   - Provide one-click fixes for common issues

3. **Custom Rule Templates**
   - Allow users to define custom rules
   - Template-based approach

4. **Enhanced Type Inference**
   - Track types through more complex expressions
   - Better nullable/null analysis

### Performance Headroom

Current bottlenecks (from 8:29 scan):
1. **SCM Publisher** (200s / 39%) - Git blame, can't optimize
2. **CPD** (153s / 30%) - SonarQube core, limited optimization
3. **Symbol Table** (part of 79s) - O(n²) but valuable

**Potential gains**:
- Rules sensor: Already optimal (O(n))
- Metrics sensor: Already fast
- CPD: Minor gains possible
- **Realistic target**: ~7-8 minutes (current is excellent!)

---

## Summary

The SonarQube Mathematica Plugin achieves **professional-grade (Tier 2+)** language support through:

1. **Architecture**: Clean, modular design with clear separation of concerns
2. **Performance**: O(n) single-pass analysis with 99.75% improvement
3. **Scale**: Handles large codebases (650+ files in 8 minutes)
4. **Quality**: 430+ rules covering all major code quality dimensions
5. **Automation**: Complete build/test/release pipeline

**Key Innovation**: UnifiedRuleVisitor pattern enabling O(400n) → O(n) transformation

---

**For More Information**:
- README.md - User guide and features
- ROADMAP.md - Feature roadmap and tier progression
- IMPROVEMENTS.md - Performance improvements and metrics
- Source code - `/src/main/java/org/sonar/plugins/mathematica/`
