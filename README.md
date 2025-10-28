# SonarQube Plugin for Wolfram Mathematica

A comprehensive SonarQube plugin providing code quality analysis, security scanning, and duplication detection for Wolfram Mathematica code.

## Features Overview

| Feature | Rules | Type | Status | Quick UI Path |
|---------|-------|------|--------|---------------|
| **Code Duplication Detection** | CPD Engine | Duplication | ✅ Active | Overview → Duplications % → Click |
| **Code Smells** | 76 rules | Code Quality | ✅ Active | Issues → Type: Code Smell |
| **Security Vulnerabilities** | 21 rules | Security | ✅ Active | Issues → Type: Vulnerability |
| **Bugs (Reliability)** | 45 rules | Reliability | ✅ Active | Issues → Type: Bug |
| **Security Hotspots** | 7 rules | Security Review | ✅ Active | Security Hotspots tab |
| **Complexity Metrics** | Cyclomatic & Cognitive | Complexity | ✅ Active | Code tab → Function details |
| **Performance Rules** | 26 rules | Performance | ✅ Active | Issues → Search "performance" |
| **Pattern System Rules** | 15 rules | Patterns | ✅ Active | Issues → Search "pattern" |
| **List/Array Rules** | 10 rules | Data Structures | ✅ Active | Issues → Search "list" |
| **Association Rules** | 10 rules | Data Structures | ✅ Active | Issues → Search "association" |
| **Unused Code Detection** | 15 rules | Dead Code | ✅ Active | Issues → Search "unused" |
| **Shadowing & Naming** | 15 rules | Code Quality | ✅ Active | Issues → Search "shadow" |
| **Undefined Symbol Detection** | 10 rules | Reliability | ✅ Active | Issues → Search "undefined" |
| **Type Mismatch Detection** | 20 rules | Type Safety | ✅ Active | Issues → Search "type" |
| **Data Flow Analysis** | 16 rules | Data Flow | ✅ Active | Issues → Search "uninitialized" |
| **OWASP Top 10 2021 Coverage** | 9 of 10 categories | Security | ✅ Active | Issues → Type: Vulnerability |
| **Total Rules** | **245 rules** + CPD + Metrics | All | ✅ Active | Issues tab |

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

## 2. Code Smell Rules (33 Total)

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

### 2.9 Unused Variables
**Severity:** MAJOR | **Type:** CODE_SMELL

Detects variables declared in Module/Block/With but never used.

**Example:**
```mathematica
(* VIOLATION *)
Module[{x, y, z},
  x = 5;
  x + 10
];  (* y and z are unused *)

(* COMPLIANT *)
Module[{x},
  x = 5;
  x + 10
];
```

### 2.10 Duplicate Function Definitions
**Severity:** MAJOR | **Type:** CODE_SMELL

Same function redefined with identical pattern signature overwrites previous definition.

**Example:**
```mathematica
(* VIOLATION *)
myFunc[x_] := x^2;
myFunc[y_] := y^3;  (* Overwrites first! *)

(* COMPLIANT *)
myFuncSquare[x_] := x^2;
myFuncCube[x_] := x^3;
```

### 2.11 Too Many Parameters
**Severity:** MAJOR | **Type:** CODE_SMELL

Functions with >7 parameters are hard to use and maintain.

**Example:**
```mathematica
(* VIOLATION *)
processData[name_, age_, address_, phone_, email_, city_, state_, zip_] := ...

(* COMPLIANT - Use Association *)
processData[userData_Association] := ...
```

### 2.12 Deeply Nested Conditionals
**Severity:** MAJOR | **Type:** CODE_SMELL

More than 3 levels of nested If/Which/Switch statements.

**Example:**
```mathematica
(* VIOLATION *)
If[a, If[b, If[c, If[d, result]]]]  (* 4 levels *)

(* COMPLIANT *)
Which[
  a && b && c && d, result,
  True, defaultResult
]
```

### 2.13 Missing Documentation
**Severity:** MINOR | **Type:** CODE_SMELL

Public functions (starting with uppercase) should have documentation.

**Example:**
```mathematica
(* VIOLATION *)
ProcessUserData[data_, options_] := Module[{...}, ...]

(* COMPLIANT *)
(* ProcessUserData[data, options] processes user data.
   Returns: Processed data list *)
ProcessUserData[data_, options_] := Module[{...}, ...]
```

### 2.14 Inconsistent Naming
**Severity:** MINOR | **Type:** CODE_SMELL

Mixing camelCase, PascalCase, and snake_case in same file.

**Example:**
```mathematica
(* VIOLATION *)
ProcessData[x_] := ...     (* PascalCase *)
calculateResult[y_] := ...  (* camelCase *)
get_user_name[] := ...     (* snake_case - inconsistent! *)

(* COMPLIANT - Pick one style *)
ProcessData[x_] := ...
CalculateResult[y_] := ...
GetUserName[] := ...
```

### 2.15 Identical Branches
**Severity:** MAJOR | **Type:** CODE_SMELL

If statement with identical then and else branches.

**Example:**
```mathematica
(* VIOLATION *)
If[condition, DoSomething[], DoSomething[]]

(* COMPLIANT *)
DoSomething[]  (* Condition is useless *)
```

### 2.16 Expression Too Complex
**Severity:** MAJOR | **Type:** CODE_SMELL

Single expression with >20 operators should be split.

**Example:**
```mathematica
(* VIOLATION *)
result = a + b * c - d / e ^ f + g * h - i / j + k * l - m / n + o * p;

(* COMPLIANT *)
term1 = b * c + a;
term2 = d / e ^ f;
result = term1 - term2 + ...;
```

### 2.17 Deprecated Functions
**Severity:** MAJOR | **Type:** CODE_SMELL

Using deprecated Mathematica functions like `$RecursionLimit`.

### 2.18 Empty Statement
**Severity:** MINOR | **Type:** CODE_SMELL

Double semicolons or misplaced semicolons create empty statements.

**Example:**
```mathematica
(* VIOLATION *)
x = 5;;  (* Double semicolon *)

(* COMPLIANT *)
x = 5;
```

---

### 2.19 Append/AppendTo in Loop (Performance)
**Severity:** MAJOR | **Type:** CODE_SMELL

Using AppendTo or Append inside loops creates O(n²) performance due to repeated list copying.

**Example:**
```mathematica
(* VIOLATION - O(n²) performance! *)
result = {};
Do[result = Append[result, f[i]], {i, 1000}]

(* COMPLIANT - O(n) performance *)
result = Table[f[i], {i, 1000}]
(* Or use Sow/Reap *)
result = Reap[Do[Sow[f[i]], {i, 1000}]][[2, 1]]
```

---

### 2.20 Repeated Function Calls (Performance)
**Severity:** MINOR | **Type:** CODE_SMELL

Calling the same expensive function multiple times with identical arguments wastes computation.

**Example:**
```mathematica
(* VIOLATION *)
result = ExpensiveComputation[data] + ExpensiveComputation[data]

(* COMPLIANT *)
cached = ExpensiveComputation[data];
result = cached + cached
```

---

### 2.21 String Concatenation in Loop (Performance)
**Severity:** MAJOR | **Type:** CODE_SMELL

Using `<>` to concatenate strings in loops is O(n²) due to string immutability.

**Example:**
```mathematica
(* VIOLATION *)
result = "";
Do[result = result <> ToString[i], {i, 1000}]

(* COMPLIANT *)
result = StringJoin[Table[ToString[i], {i, 1000}]]
```

---

### 2.22 Uncompiled Numerical Code (Performance)
**Severity:** MINOR | **Type:** CODE_SMELL

Numerical computations in loops can be 10-100x faster when compiled.

**Example:**
```mathematica
(* WITHOUT COMPILE - slower *)
sum = 0; Do[sum += i^2, {i, 10000}]

(* WITH COMPILE - much faster *)
compiled = Compile[{}, Module[{sum = 0}, Do[sum += i^2, {i, 10000}]; sum]];
result = compiled[]
```

---

### 2.23 Nested Map/Table (Performance)
**Severity:** MINOR | **Type:** CODE_SMELL

Nested Map or Table calls can often be replaced with more efficient single operations.

**Example:**
```mathematica
(* VIOLATION *)
result = Table[Table[i*j, {j, 10}], {i, 10}]

(* COMPLIANT *)
result = Table[i*j, {i, 10}, {j, 10}]
(* Or use Outer *)
result = Outer[Times, Range[10], Range[10]]
```

---

### 2.24 Plot in Loop (Performance)
**Severity:** MAJOR | **Type:** CODE_SMELL

Generating plots in loops is very slow. Collect data first, then plot once.

**Example:**
```mathematica
(* VIOLATION *)
Do[ListPlot[data[[i]]], {i, 100}]

(* COMPLIANT *)
ListPlot[data, PlotRange -> All]
```

---

### 2.25 Generic Variable Names
**Severity:** MINOR | **Type:** CODE_SMELL

Generic names like 'temp', 'data', 'result' provide no context and hurt readability.

**Example:**
```mathematica
(* VIOLATION *)
data = Import["file.csv"];
result = ProcessData[data];
temp = result[[1]];

(* COMPLIANT *)
salesData = Import["sales.csv"];
processedSales = ProcessSalesData[salesData];
firstQuarterSales = processedSales[[1]];
```

---

### 2.26 Missing Usage Message
**Severity:** MINOR | **Type:** CODE_SMELL

Public functions (starting with uppercase) should define usage messages for documentation.

**Example:**
```mathematica
(* VIOLATION *)
ProcessUserData[data_, options___] := Module[...]

(* COMPLIANT *)
ProcessUserData::usage = "ProcessUserData[data, options] processes user data.";
ProcessUserData[data_, options___] := Module[...]
```

---

### 2.27 Missing OptionsPattern
**Severity:** MINOR | **Type:** CODE_SMELL

Functions with 3+ optional parameters should use OptionsPattern for maintainability.

**Example:**
```mathematica
(* VIOLATION *)
PlotData[data_, color_: Blue, size_: 10, style_: Solid] := ...

(* COMPLIANT *)
Options[PlotData] = {"Color" -> Blue, "Size" -> 10, "Style" -> Solid};
PlotData[data_, opts: OptionsPattern[]] := Module[
  {color = OptionValue["Color"], size = OptionValue["Size"]},
  ...
]
```

---

### 2.28 Side Effects Without Clear Naming
**Severity:** MINOR | **Type:** CODE_SMELL

Functions that modify global state should use naming conventions: SetXXX or ending with !.

**Example:**
```mathematica
(* VIOLATION *)
Process[data_] := (globalCache = data; data)

(* COMPLIANT *)
SetCache[data_] := (globalCache = data; data)
(* Or *)
UpdateCache![data_] := (globalCache = data; data)
```

---

### 2.29 Complex Boolean Expression
**Severity:** MINOR | **Type:** CODE_SMELL

Boolean expressions with 5+ operators without clear grouping are hard to understand.

**Example:**
```mathematica
(* VIOLATION *)
If[a && b || c && d && !e || f && g, ...]

(* COMPLIANT *)
condition1 = a && b;
condition2 = c && d && !e;
condition3 = f && g;
If[condition1 || condition2 || condition3, ...]
```

---

### 2.30 Unprotected Public Symbols
**Severity:** MINOR | **Type:** CODE_SMELL

Public functions in packages should be Protected to prevent accidental redefinition.

**Example:**
```mathematica
(* At end of package *)
Protect[PublicFunction1, PublicFunction2, PublicConstant];
```

---

### 2.31 Missing Return in Complex Function
**Severity:** MINOR | **Type:** CODE_SMELL

Functions with multiple branches should use explicit Return[] for clarity.

**Example:**
```mathematica
ProcessData[data_] := Module[{result},
  If[data === {}, Return[$Failed]];
  result = ComputeResult[data];
  If[!ValidQ[result], Return[Default]];
  Return[result]
]
```

---

### 2.32 Packed Array Breaking (Performance)
**Severity:** MINOR | **Type:** CODE_SMELL

Packed arrays are 10x+ faster than unpacked arrays. Avoid operations that unpack them.

**Operations that Unpack Arrays:**
- Mixing integers and reals in same array
- Using symbolic values in numerical arrays
- Applying non-numerical functions to packed arrays

**Example:**
```mathematica
(* Check if arrays stay packed *)
data = Range[1000];
PackedArrayQ[data]  (* True *)

(* Mixing types unpacks *)
result = Join[data, {3.5}];  (* Now unpacked! *)
PackedArrayQ[result]  (* False *)

(* Keep packed by using consistent types *)
result = Join[data, {3}];  (* Stays packed *)
PackedArrayQ[result]  (* True *)
```

---

### 2.33 Large Temporary Expressions (Performance)
**Severity:** MINOR | **Type:** CODE_SMELL

Large intermediate results (>100MB) that aren't assigned can cause memory issues.

**Example:**
```mathematica
(* HARD TO DEBUG - large temp not visible *)
result = ProcessData[HugeComputation[data]];

(* BETTER - make memory usage explicit *)
temp = HugeComputation[data];  (* Can monitor size *)
ByteCount[temp]  (* Check memory usage *)
result = ProcessData[temp];
```

---

## 2A. Phase 4: Additional Code Smell & Performance Rules (28 New Rules)

This section documents 50 additional rules added in Phase 4 to enhance code quality analysis.

### Pattern Matching & Function Definition (5 rules)

#### 2A.1 Overcomplex Patterns
**Severity:** MAJOR | **Type:** CODE_SMELL

Pattern definitions with >5 alternatives (|) are hard to understand and maintain.

**Example:**
```mathematica
(* VIOLATION - 7 alternatives *)
process[x_Integer | x_Real | x_Rational | x_Complex | x_String | x_Symbol | x_List] := ...

(* COMPLIANT - Use pattern tests or split into separate functions *)
process[x_?NumericQ] := numericProcess[x];
process[x_String] := stringProcess[x];
process[x_List] := listProcess[x];
```

#### 2A.2 Inconsistent Rule Types
**Severity:** MINOR | **Type:** CODE_SMELL

Mixing Rule (->) and RuleDelayed (:>) in same list creates confusion about evaluation timing.

**Example:**
```mathematica
(* VIOLATION *)
rules = {a -> Compute[a], b :> Compute[b]};

(* COMPLIANT *)
rules = {a :> Compute[a], b :> Compute[b]};  (* Consistent *)
```

#### 2A.3 Missing Function Attributes
**Severity:** MINOR | **Type:** CODE_SMELL

Public functions should consider using attributes (Listable, Protected, etc.) for better behavior.

**Example:**
```mathematica
(* COMPLIANT *)
SetAttributes[MyFunction, {Listable, NumericFunction, Protected}];
```

#### 2A.4 Missing DownValues Documentation
**Severity:** MINOR | **Type:** CODE_SMELL

Functions with 3+ pattern definitions should have ::usage documentation.

**Example:**
```mathematica
(* VIOLATION *)
ProcessData[x_Integer] := ...
ProcessData[x_Real] := ...
ProcessData[x_String] := ...
(* No ::usage message! *)

(* COMPLIANT *)
ProcessData::usage = "ProcessData[x] processes data of various types.";
ProcessData[x_Integer] := ...
ProcessData[x_Real] := ...
ProcessData[x_String] := ...
```

#### 2A.5 Missing Pattern Test Validation
**Severity:** MAJOR | **Type:** CODE_SMELL

Functions should validate input types with pattern tests (?NumericQ, ?ListQ, etc.).

**Example:**
```mathematica
(* VIOLATION *)
f[x_] := Sqrt[x] + x^2  (* No type validation *)

(* COMPLIANT *)
f[x_?NumericQ] := Sqrt[x] + x^2
```

---

### Code Clarity (8 rules)

#### 2A.6 Excessive Pure Functions
**Severity:** MINOR | **Type:** CODE_SMELL

Complex pure functions with multiple # slots should use Function[{x, y, z}, ...] for clarity.

**Example:**
```mathematica
(* VIOLATION *)
Map[#1 + #2 * #3 - #4 / #1 &, data]

(* COMPLIANT *)
Map[Function[{a, b, c, d}, a + b * c - d / a], data]
```

#### 2A.7 Missing Operator Precedence
**Severity:** MINOR | **Type:** CODE_SMELL

Complex operator expressions (/@, @@, //@) should use parentheses for clarity.

**Example:**
```mathematica
(* VIOLATION *)
result = f /@ g @@ h //@ data

(* COMPLIANT *)
result = (f /@ (g @@ h)) //@ data
```

#### 2A.8 Hardcoded File Paths
**Severity:** MAJOR | **Type:** CODE_SMELL

Use FileNameJoin and $HomeDirectory instead of hardcoded paths.

**Example:**
```mathematica
(* VIOLATION *)
Import["C:\\Users\\John\\data.csv"]
Import["/Users/john/data.csv"]

(* COMPLIANT *)
Import[FileNameJoin[{$HomeDirectory, "data.csv"}]]
```

#### 2A.9 Inconsistent Return Types
**Severity:** MINOR | **Type:** CODE_SMELL

Function definitions should return consistent types across all pattern branches.

**Example:**
```mathematica
(* VIOLATION *)
ProcessData[x_Integer] := {result}      (* Returns List *)
ProcessData[x_String] := <|"result" -> value|>  (* Returns Association *)

(* COMPLIANT *)
ProcessData[x_Integer] := <|"result" -> result|>
ProcessData[x_String] := <|"result" -> value|>
```

#### 2A.10 Missing Error Messages
**Severity:** MINOR | **Type:** CODE_SMELL

Custom functions should define error messages for better usability.

**Example:**
```mathematica
(* COMPLIANT *)
MyFunction::badarg = "Argument `1` is invalid. Expected positive integer.";
MyFunction[x_] := If[x <= 0, Message[MyFunction::badarg, x]; $Failed, x^2]
```

#### 2A.11 Global State Modification
**Severity:** MINOR | **Type:** CODE_SMELL

Functions modifying state should use ! suffix or Set*/Update* prefix.

**Example:**
```mathematica
(* VIOLATION *)
UpdateCache[data_] := (cache = data; data)

(* COMPLIANT *)
UpdateCache![data_] := (cache = data; data)
SetCache[data_] := (cache = data; data)
```

#### 2A.12 Missing Localization
**Severity:** MINOR | **Type:** CODE_SMELL

Manipulate should use LocalizeVariables to prevent variable leakage.

**Example:**
```mathematica
(* COMPLIANT *)
Manipulate[Plot[Sin[a x], {x, 0, 2Pi}],
  {a, 1, 10},
  LocalizeVariables -> True
]
```

#### 2A.13 Explicit Global` Context
**Severity:** MINOR | **Type:** CODE_SMELL

Using Global` explicitly indicates namespace confusion.

**Example:**
```mathematica
(* VIOLATION *)
Global`myVariable = 5;

(* COMPLIANT *)
myVariable = 5;  (* Already in Global by default *)
```

---

### Data Structure (5 rules)

#### 2A.14 Missing Temporary Cleanup
**Severity:** MINOR | **Type:** CODE_SMELL

Temporary files/directories should be cleaned up or use auto-deletion.

**Example:**
```mathematica
(* VIOLATION *)
CreateFile["temp.dat"]
(* No cleanup *)

(* COMPLIANT *)
file = CreateFile[];  (* Auto-deleted on kernel quit *)
(* Or explicit cleanup *)
file = CreateFile["temp.dat"];
(* ... use file ... *)
DeleteFile[file];
```

#### 2A.15 Nested Lists Instead of Association
**Severity:** MINOR | **Type:** CODE_SMELL

Multiple indexed accesses suggest Association would be clearer.

**Example:**
```mathematica
(* VIOLATION *)
data[[1]], data[[5]], data[[7]]

(* COMPLIANT *)
data = <|"name" -> "John", "age" -> 30, "city" -> "NYC"|>;
data["name"], data["age"], data["city"]
```

#### 2A.16 Repeated Part Extraction
**Severity:** MINOR | **Type:** CODE_SMELL

Multiple Part extractions should use destructuring.

**Example:**
```mathematica
(* VIOLATION *)
x = data[[1]];
y = data[[2]];
z = data[[3]];

(* COMPLIANT *)
{x, y, z} = data;
```

#### 2A.17 Missing Memoization
**Severity:** MINOR | **Type:** CODE_SMELL

Recursive functions should consider memoization for performance.

**Example:**
```mathematica
(* WITHOUT MEMOIZATION *)
fib[n_] := fib[n-1] + fib[n-2]
fib[0] = 0; fib[1] = 1;

(* WITH MEMOIZATION *)
fib[n_] := fib[n] = fib[n-1] + fib[n-2]
fib[0] = 0; fib[1] = 1;
```

#### 2A.18 StringJoin for Templates
**Severity:** MINOR | **Type:** CODE_SMELL

Multiple StringJoin operations should use StringTemplate.

**Example:**
```mathematica
(* VIOLATION *)
"Hello " <> name <> ", your score is " <> ToString[score]

(* COMPLIANT *)
template = StringTemplate["Hello `name`, your score is `score`"];
template[<|"name" -> name, "score" -> score|>]
```

---

### Performance Rules (10 rules)

#### 2A.19 Linear Search Instead of Lookup
**Severity:** MAJOR | **Type:** CODE_SMELL

Use Association or Dispatch for O(1) lookup instead of Select (O(n)).

**Example:**
```mathematica
(* VIOLATION - O(n) *)
Select[data, #[[2]] == searchKey &]

(* COMPLIANT - O(1) *)
lookup = Association[Map[#[[2]] -> # &, data]];
lookup[searchKey]
```

#### 2A.20 Repeated Calculations
**Severity:** MINOR | **Type:** CODE_SMELL

Expensive expressions calculated repeatedly in loop should be hoisted out.

**Example:**
```mathematica
(* VIOLATION *)
Do[process[ExpensiveComputation[] + i], {i, 1000}]

(* COMPLIANT *)
cached = ExpensiveComputation[];
Do[process[cached + i], {i, 1000}]
```

#### 2A.21 Position Instead of Pattern
**Severity:** MINOR | **Type:** CODE_SMELL

Consider Cases or Select with pattern matching instead of Position + Extract.

**Example:**
```mathematica
(* VIOLATION *)
positions = Position[data, _?EvenQ];
Extract[data, positions]

(* COMPLIANT *)
Cases[data, x_?EvenQ]
```

#### 2A.22 Flatten[Table[...]] Antipattern
**Severity:** MINOR | **Type:** CODE_SMELL

Use Catenate, Join, or vectorization instead of Flatten[Table[...]].

**Example:**
```mathematica
(* VIOLATION *)
Flatten[Table[{i, i^2}, {i, 10}]]

(* COMPLIANT *)
Catenate[Table[{i, i^2}, {i, 10}]]
```

#### 2A.23 Missing Parallelization
**Severity:** MINOR | **Type:** CODE_SMELL

Large independent iterations should use ParallelTable or ParallelMap.

**Example:**
```mathematica
(* VIOLATION *)
Table[ExpensiveFunction[i], {i, 10000}]

(* COMPLIANT *)
ParallelTable[ExpensiveFunction[i], {i, 10000}]
```

#### 2A.24 Missing SparseArray
**Severity:** MINOR | **Type:** CODE_SMELL

Large arrays with many zeros should use SparseArray for efficiency.

**Example:**
```mathematica
(* VIOLATION *)
zeros = Table[0, {1000}, {1000}]

(* COMPLIANT *)
zeros = SparseArray[{}, {1000, 1000}]
```

#### 2A.25 Unnecessary Transpose
**Severity:** MINOR | **Type:** CODE_SMELL

Repeated Transpose operations suggest inconsistent row/column handling.

**Example:**
```mathematica
(* VIOLATION *)
Transpose[Transpose[matrix]]

(* COMPLIANT *)
matrix  (* Just use original! *)
```

#### 2A.26 DeleteDuplicates on Large Data
**Severity:** MINOR | **Type:** CODE_SMELL

For large lists, Keys@GroupBy is faster than DeleteDuplicates.

**Example:**
```mathematica
(* SLOWER *)
DeleteDuplicates[largeList]

(* FASTER *)
Keys[GroupBy[largeList, Identity]]
```

#### 2A.27 Repeated String Parsing
**Severity:** MINOR | **Type:** CODE_SMELL

Parsing the same string repeatedly in loop - cache the result.

**Example:**
```mathematica
(* VIOLATION *)
Table[ToExpression["2*Pi"], {i, 1000}]

(* COMPLIANT *)
cached = ToExpression["2*Pi"];
Table[cached, {i, 1000}]
```

#### 2A.28 Missing CompilationTarget
**Severity:** MINOR | **Type:** CODE_SMELL

Compile should use CompilationTarget->"C" for 10-100x speedup.

**Example:**
```mathematica
(* WITHOUT C TARGET *)
compiled = Compile[{{x, _Real}}, x^2];

(* WITH C TARGET *)
compiled = Compile[{{x, _Real}}, x^2, CompilationTarget -> "C"];
```

---

## 3. Bug Rules - Reliability (35 Total)

### 3.1-3.20 Original Bug Rules

(Original 20 bug rules documented above remain unchanged)

---

### 3A. Phase 4: Additional Bug Rules (15 New Rules)

#### 3A.1 Missing Empty List Check
**Severity:** MAJOR | **Type:** BUG
**CWE:** [CWE-129](https://cwe.mitre.org/data/definitions/129.html)

Using First/Last/Part without checking for empty list causes crashes.

**Example:**
```mathematica
(* VIOLATION *)
first = First[myList];

(* COMPLIANT *)
If[Length[myList] > 0, First[myList], $Failed]
```

#### 3A.2 Negative Index Access
**Severity:** MAJOR | **Type:** BUG

Negative indices access from end, but -1 means last element, not invalid.

**Example:**
```mathematica
(* CAREFUL *)
list[[-1]]  (* Last element, valid *)
list[[-Length[list]-1]]  (* Out of bounds! *)

(* SAFE *)
If[-index <= Length[list], list[[index]], $Failed]
```

#### 3A.3 String to Number Without Validation
**Severity:** MAJOR | **Type:** BUG

ToExpression on strings should be validated first.

**Example:**
```mathematica
(* VIOLATION *)
num = ToExpression[userInput]

(* COMPLIANT *)
If[StringMatchQ[userInput, NumberString],
  ToExpression[userInput],
  $Failed
]
```

#### 3A.4 Missing Numeric Check
**Severity:** MAJOR | **Type:** BUG

Mathematical operations should validate numeric inputs.

**Example:**
```mathematica
(* VIOLATION *)
Sqrt[x]  (* What if x is a string? *)

(* COMPLIANT *)
If[NumericQ[x], Sqrt[x], $Failed]
```

#### 3A.5 Undefined Function Call
**Severity:** CRITICAL | **Type:** BUG

Calling functions that aren't defined causes errors.

**Example:**
```mathematica
(* VIOLATION *)
result = NonExistentFunction[data]

(* CHECK FIRST *)
If[NameQ["NonExistentFunction"], NonExistentFunction[data], $Failed]
```

#### 3A.6 Mismatched Dimensions in Operations
**Severity:** MAJOR | **Type:** BUG

Array operations need dimension compatibility.

**Example:**
```mathematica
(* VIOLATION *)
matrix1 + matrix2  (* No dimension check! *)

(* COMPLIANT *)
If[Dimensions[matrix1] === Dimensions[matrix2],
  matrix1 + matrix2,
  $Failed
]
```

#### 3A.7 Symbolic vs Numeric Confusion
**Severity:** MAJOR | **Type:** BUG

Mixing symbolic and numeric operations can cause unexpected results.

**Example:**
```mathematica
(* CAREFUL *)
Solve[x^2 == 4, x]  (* Symbolic: {x -> 2}, {x -> -2} *)
NSolve[x^2 == 4, x]  (* Numeric: {{x -> 2.}, {x -> -2.}} *)
```

#### 3A.8 Missing Null Check
**Severity:** MAJOR | **Type:** BUG

Operations on potentially Null values need checks.

**Example:**
```mathematica
(* VIOLATION *)
result = data + 5  (* What if data is Null? *)

(* COMPLIANT *)
If[data =!= Null, data + 5, $Failed]
```

#### 3A.9 Range Specification Error
**Severity:** MAJOR | **Type:** BUG

Ensure range specifications are valid (min <= max).

**Example:**
```mathematica
(* VIOLATION *)
Table[i, {i, 10, 1}]  (* Empty! Need step: {i, 10, 1, -1} *)

(* COMPLIANT *)
Table[i, {i, 10, 1, -1}]
```

#### 3A.10 Incorrect Default Value
**Severity:** MAJOR | **Type:** BUG

Default parameter values should match expected types.

**Example:**
```mathematica
(* VIOLATION *)
f[x_, threshold_: "auto"] := If[NumericQ[threshold], ...]  (* Type mismatch! *)

(* COMPLIANT *)
f[x_, threshold_: 0.5] := If[NumericQ[threshold], ...]
```

#### 3A.11 List Assignment Length Mismatch
**Severity:** MAJOR | **Type:** BUG

List unpacking must match lengths.

**Example:**
```mathematica
(* VIOLATION *)
{a, b} = {1, 2, 3}  (* Loses 3! *)

(* SAFE *)
If[Length[data] == 2, {a, b} = data, $Failed]
```

#### 3A.12 Unintended Global Assignment
**Severity:** MAJOR | **Type:** BUG

Missing Module/Block creates global variables.

**Example:**
```mathematica
(* VIOLATION *)
f[x_] := (temp = x^2; temp + 1)  (* temp is global! *)

(* COMPLIANT *)
f[x_] := Module[{temp = x^2}, temp + 1]
```

#### 3A.13 Missing List Check Before Operations
**Severity:** MAJOR | **Type:** BUG

Operations expecting lists should validate input type.

**Example:**
```mathematica
(* VIOLATION *)
Total[data]  (* What if data is not a list? *)

(* COMPLIANT *)
If[ListQ[data], Total[data], $Failed]
```

#### 3A.14 Wrong Equality Operator
**Severity:** CRITICAL | **Type:** BUG

Using = instead of == in conditions.

**Example:**
```mathematica
(* VIOLATION *)
If[x = 5, doSomething[]]  (* Assigns x=5, always True! *)

(* COMPLIANT *)
If[x == 5, doSomething[]]
```

#### 3A.15 Pattern Matching Scope Error
**Severity:** MAJOR | **Type:** BUG

Pattern variables in nested scopes can collide.

**Example:**
```mathematica
(* CAREFUL *)
f[x_] := Map[Function[x, x^2], data]  (* Inner x shadows parameter x *)

(* BETTER *)
f[x_] := Map[Function[y, y^2], data]
```

---

## 4. Security Vulnerability Rules (21 Total)

### 4.1-4.14 Original Vulnerability Rules

(Original 14 vulnerability rules documented above remain unchanged)

---

### 4A. Phase 4: Additional Vulnerability Rules (7 New Rules)

#### 4A.1 ToExpression on External Input (CRITICAL)
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-94](https://cwe.mitre.org/data/definitions/94.html) | **OWASP:** A03:2021

ToExpression on external input enables arbitrary code execution.

**Example:**
```mathematica
(* VIOLATION *)
result = ToExpression[userInput]  (* User can execute ANY code! *)

(* COMPLIANT *)
result = Interpreter["Number"][userInput]  (* Safe parsing *)
```

#### 4A.2 Unsafe Symbol Creation
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-470](https://cwe.mitre.org/data/definitions/470.html)

Creating symbols from user input allows access to any function/variable.

**Example:**
```mathematica
(* VIOLATION *)
func = Symbol[userFunctionName]  (* User controls what function! *)

(* COMPLIANT *)
allowedFunctions = {"Mean", "Median", "Total"};
If[MemberQ[allowedFunctions, userFunctionName],
  Symbol[userFunctionName],
  $Failed
]
```

#### 4A.3 RunProcess Shell Injection
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-78](https://cwe.mitre.org/data/definitions/78.html)

Shell commands with user input enable command injection.

**Example:**
```mathematica
(* VIOLATION *)
RunProcess[{"sh", "-c", "cat " <> userFile}]

(* COMPLIANT - Use array form *)
RunProcess[{"cat", userFile}]
```

#### 4A.4 Import Without Validation
**Severity:** HIGH | **Type:** VULNERABILITY
**CWE:** [CWE-434](https://cwe.mitre.org/data/definitions/434.html)

Importing files without validation can execute malicious code.

**Example:**
```mathematica
(* VIOLATION *)
Import[uploadedFile]  (* Could be .mx with code! *)

(* COMPLIANT *)
If[StringEndsQ[uploadedFile, ".csv"],
  Import[uploadedFile, "CSV"],
  $Failed
]
```

#### 4A.5 CloudPut Without Permissions
**Severity:** HIGH | **Type:** VULNERABILITY
**CWE:** [CWE-732](https://cwe.mitre.org/data/definitions/732.html)

Cloud objects without permissions are public.

**Example:**
```mathematica
(* VIOLATION *)
CloudPut[sensitiveData, "data.mx"]  (* Public! *)

(* COMPLIANT *)
CloudPut[sensitiveData, "data.mx", Permissions -> "Private"]
```

#### 4A.6 Unsafe APIFunction
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-20](https://cwe.mitre.org/data/definitions/20.html)

APIFunction without input validation exposes vulnerabilities.

**Example:**
```mathematica
(* VIOLATION *)
APIFunction[{"x" -> "String"}, ToExpression[#x] &]  (* Code injection! *)

(* COMPLIANT *)
APIFunction[{"x" -> "Integer"}, #x^2 &]  (* Type-safe *)
```

#### 4A.7 FormFunction Without Validation
**Severity:** HIGH | **Type:** VULNERABILITY
**CWE:** [CWE-20](https://cwe.mitre.org/data/definitions/20.html)

FormFunction inputs need validation and sanitization.

**Example:**
```mathematica
(* VIOLATION *)
FormFunction[{"file" -> "String"}, Import[#file] &]

(* COMPLIANT *)
FormFunction[{"file" -> "String"},
  If[StringEndsQ[#file, ".csv"], Import[#file, "CSV"], $Failed] &
]
```

---

## 3. Bug Rules - Reliability (20 Total)

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

### 3.6 Floating Point Equality
**Severity:** MAJOR | **Type:** BUG
**CWE:** [CWE-1077](https://cwe.mitre.org/data/definitions/1077.html)

Using == or === to compare floating point numbers is unreliable due to rounding errors.

**Example:**
```mathematica
(* VIOLATION *)
If[0.1 + 0.2 == 0.3, ...]  (* May be False due to rounding! *)
result = (x == 0.5);

(* COMPLIANT - Use tolerance-based comparison *)
If[Abs[(0.1 + 0.2) - 0.3] < 10^-10, ...]
If[Chop[x - 0.5] == 0, ...]
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Search: `floating` or `equality`
3. → Shows floating point comparison issues
4. These can cause subtle correctness bugs in numerical code

---

### 3.7 Function Without Return
**Severity:** MAJOR | **Type:** BUG

Functions that appear to compute a result but don't return it properly.

**Example:**
```mathematica
(* VIOLATION *)
calculateTotal[items_] := Module[{total},
  total = Total[items];
  (* total is computed but not returned! *)
];

(* COMPLIANT *)
calculateTotal[items_] := Module[{total},
  total = Total[items];
  total  (* Return the result *)
];
```

---

### 3.8 Variable Before Assignment
**Severity:** MAJOR | **Type:** BUG

Using a variable before it has been assigned a value.

**Example:**
```mathematica
(* VIOLATION *)
Module[{x, y},
  y = x + 5;  (* x not yet assigned! *)
  x = 10;
];

(* COMPLIANT *)
Module[{x, y},
  x = 10;
  y = x + 5;
];
```

---

### 3.9 Off-by-One Errors
**Severity:** MAJOR | **Type:** BUG

Common indexing mistakes in loops and list operations.

**Example:**
```mathematica
(* VIOLATION *)
Do[process[list[[i]]], {i, 0, Length[list]}]  (* 0 is invalid, Length+1 too! *)

(* COMPLIANT - Mathematica uses 1-based indexing *)
Do[process[list[[i]]], {i, 1, Length[list]}]
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Search: `off-by-one` or `index`
3. → Shows potential indexing errors
4. Critical to fix: these cause out-of-bounds crashes

---

### 3.10 Infinite Loop
**Severity:** CRITICAL | **Type:** BUG

While loops with conditions that never become false.

**Example:**
```mathematica
(* VIOLATION *)
While[True, doSomething[]]  (* Runs forever! *)

(* COMPLIANT *)
While[condition && counter < maxIterations,
  doSomething[];
  counter++;
]
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Bug"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `infinite` or `While`
4. → Shows `While[True, ...]` patterns
5. These cause program hangs - CRITICAL severity

---

### 3.11 Mismatched Matrix Dimensions
**Severity:** MAJOR | **Type:** BUG

Matrix operations with incompatible dimensions.

**Example:**
```mathematica
(* VIOLATION *)
result = matrix1 . matrix2;  (* No dimension check! *)

(* COMPLIANT *)
If[Dimensions[matrix1][[2]] == Dimensions[matrix2][[1]],
  result = matrix1 . matrix2,
  $Failed
]
```

---

### 3.12 Type Mismatch
**Severity:** MAJOR | **Type:** BUG

Operations mixing incompatible types (string + number).

**Example:**
```mathematica
(* VIOLATION *)
result = "Count: " + count;  (* String + Integer - won't work! *)

(* COMPLIANT *)
result = "Count: " <> ToString[count];  (* Use <> for strings *)
```

---

### 3.13 Suspicious Pattern Matching
**Severity:** MAJOR | **Type:** BUG

Pattern definitions that likely don't work as intended.

**Example:**
```mathematica
(* VIOLATION *)
func[x__] := Length[x];  (* x__ is a sequence, not a list! *)

(* COMPLIANT *)
func[x__] := Length[{x}];  (* Wrap sequence in {} to get list *)
(* Or better: *)
func[x_List] := Length[x];
```

---

### 3.14 Missing Pattern Test
**Severity:** MAJOR | **Type:** BUG
**CWE:** [CWE-704](https://cwe.mitre.org/data/definitions/704.html)

Functions expecting numeric arguments should use pattern tests to prevent symbolic evaluation errors.

**Example:**
```mathematica
(* VIOLATION *)
f[x_] := Sqrt[x] + x^2  (* Will evaluate symbolically for f[a] *)

(* COMPLIANT *)
f[x_?NumericQ] := Sqrt[x] + x^2  (* Only evaluates for numbers *)
(* Or use specific types *)
f[x_Real] := Sqrt[x] + x^2
```

---

### 3.15 Pattern Blanks Misuse
**Severity:** MAJOR | **Type:** BUG

Using __ or ___ creates sequences, not lists. This often causes errors.

**Example:**
```mathematica
(* VIOLATION *)
f[x__] := Length[x]  (* ERROR: x is a sequence, Length expects list *)

(* COMPLIANT *)
f[x__] := Length[{x}]  (* Wrap sequence in list *)
(* Or use List pattern *)
f[x_List] := Length[x]
```

---

### 3.16 Set vs SetDelayed Confusion
**Severity:** CRITICAL | **Type:** BUG

Using Set (=) instead of SetDelayed (:=) evaluates the right-hand side immediately.

**Example:**
```mathematica
(* VIOLATION *)
f[x_] = RandomReal[]  (* Evaluates once, same value always returned! *)

(* COMPLIANT *)
f[x_] := RandomReal[]  (* Evaluates each time function is called *)
```

---

### 3.17 Symbol Name Collision
**Severity:** CRITICAL | **Type:** BUG

Defining functions with single-letter names collides with Mathematica built-ins.

**Example:**
```mathematica
(* VIOLATIONS *)
N[x_] := ...  (* Shadows built-in N[] for numerical evaluation! *)
D[x_] := ...  (* Shadows built-in D[] for derivatives! *)
I = 5;  (* Shadows imaginary unit I! *)

(* COMPLIANT *)
myN[x_] := ...
derivative[x_] := ...
index = 5;
```

**Common built-ins to avoid:** N, D, I, C, O, E, K, Pi, Re, Im, Abs, Min, Max, Log, Sin, Cos

---

### 3.18 Unclosed File Handle
**Severity:** MAJOR | **Type:** BUG
**CWE:** [CWE-772](https://cwe.mitre.org/data/definitions/772.html)

OpenRead/OpenWrite/OpenAppend create file handles that must be closed with Close[].

**Example:**
```mathematica
(* VIOLATION *)
stream = OpenRead["file.txt"];
data = Read[stream, String];
(* Missing Close[stream]! *)

(* COMPLIANT *)
stream = OpenRead["file.txt"];
data = Read[stream, String];
Close[stream];

(* Or use Import which handles cleanup automatically *)
data = Import["file.txt", "String"];
```

---

### 3.19 Growing Definition Chain
**Severity:** MAJOR | **Type:** BUG
**CWE:** [CWE-401](https://cwe.mitre.org/data/definitions/401.html)

Repeatedly adding definitions (e.g., memoization in loop) without clearing causes memory leaks.

**Example:**
```mathematica
(* VIOLATION - Memory leak! *)
Do[f[i] = ExpensiveComputation[i], {i, 1, 100000}]
(* Creates 100k definitions, never cleared! *)

(* COMPLIANT - Use temporary memoization *)
Block[{f},
  Do[f[i] = ExpensiveComputation[i], {i, 1, 100000}];
  (* Use f here *)
]  (* Definitions cleared when Block exits *)

(* Or use Association *)
cache = Association[];
Do[cache[i] = ExpensiveComputation[i], {i, 1, 100000}];
```

---

### 3.20 Block/Module Misuse
**Severity:** MAJOR | **Type:** BUG

Block provides dynamic scope, Module provides lexical scope. Using the wrong one causes bugs.

**When to Use Each:**
- **Module**: For local variables (most common case - lexical scope)
- **Block**: To temporarily change global values (dynamic scope)
- **With**: For constant local values

**Example:**
```mathematica
(* Use Module for local variables *)
f[x_] := Module[{temp = x^2}, temp + 1]

(* Use Block to temporarily override globals *)
Block[{$RecursionLimit = 1024}, RecursiveFunction[]]

(* WRONG - Block with local assignments suggests Module is better *)
Block[{x = 5, y = 10}, x + y]  (* Should probably be Module *)
```

---

## 4. Security Vulnerability Rules (14 Total)

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

### 4.9 Unsafe Symbol/ToExpression
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-94](https://cwe.mitre.org/data/definitions/94.html) | **OWASP:** A03:2021

Using Symbol[] or ToExpression[] to dynamically create symbols from user input allows code injection.

**Examples:**
```mathematica
(* VIOLATIONS *)
Symbol[userInput];  (* User can access any symbol in memory! *)
ToExpression[userFunction <> "[data]"];  (* User controls what function runs *)

(* COMPLIANT *)
allowedSymbols = {"Mean", "Median", "Total"};
If[MemberQ[allowedSymbols, userInput],
  Symbol[userInput],
  $Failed
]
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"Critical"**
3. Search: `Symbol` or `ToExpression`
4. → Shows dynamic symbol creation vulnerabilities
5. These allow arbitrary code execution - CRITICAL severity

---

### 4.10 XML External Entity (XXE)
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-611](https://cwe.mitre.org/data/definitions/611.html) | **OWASP:** A05:2021

Importing XML files without disabling external entity processing can lead to information disclosure or DoS.

**Examples:**
```mathematica
(* VIOLATION *)
Import[userFile, "XML"];  (* May contain malicious external entities *)

(* COMPLIANT - Use safe import formats *)
Import[userFile, "JSON"];  (* JSON doesn't support external entities *)

(* Or validate XML before importing *)
xmlContent = Import[file, "String"];
If[StringContainsQ[xmlContent, "<!ENTITY"],
  $Failed,  (* Reject XML with entities *)
  Import[file, "XML"]
]
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Search: `XML` or `XXE`
3. → Shows "Validate XML imports to prevent XXE attacks"
4. Review all Import[..., "XML"] calls for proper validation

---

### 4.11 Missing Input Sanitization
**Severity:** HIGH | **Type:** VULNERABILITY
**CWE:** [CWE-20](https://cwe.mitre.org/data/definitions/20.html) | **OWASP:** A03:2021

User input used in file operations, URLs, or commands without validation.

**Examples:**
```mathematica
(* VIOLATIONS *)
Export[fileName, data];  (* fileName from user - path traversal! *)
URLFetch["https://api.com/" <> userEndpoint];  (* SSRF! *)

(* COMPLIANT *)
(* Whitelist allowed values *)
allowedFiles = {"output.csv", "report.txt"};
If[MemberQ[allowedFiles, fileName],
  Export[fileName, data],
  $Failed
];

(* Sanitize input *)
safeFileName = StringReplace[fileName,
  RegularExpression["[^a-zA-Z0-9_.-]"] -> ""
];
Export[safeFileName, data];
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Filter **"Severity"** → Check **"High"**
3. Search: `sanitiz` or `validat`
4. → Shows operations using unsanitized input
5. Review each to add proper input validation

---

### 4.12 Insecure Random Number Generation
**Severity:** HIGH | **Type:** VULNERABILITY
**CWE:** [CWE-338](https://cwe.mitre.org/data/definitions/338.html) | **OWASP:** A02:2021

Using Random[] instead of RandomInteger[] for security-sensitive operations like tokens or keys.

**Examples:**
```mathematica
(* VIOLATIONS *)
sessionToken = ToString[Random[]];  (* Predictable! *)
cryptoKey = Table[Random[], {32}];  (* Not cryptographically secure! *)

(* COMPLIANT *)
sessionToken = IntegerString[RandomInteger[{10^30, 10^31}], 16];
cryptoKey = RandomInteger[{0, 255}, 32];  (* 256-bit key *)
```

### 📍 How to View in SonarQube UI
1. **Issues** tab → Filter **"Type"** → Check **"Vulnerability"**
2. Search: `Random` or `insecure random`
3. → Shows "Use RandomInteger instead of Random for security"
4. Click to see Random[] usage in security contexts
5. Replace with RandomInteger[] for cryptographic operations

---

### 4.13 Unsafe CloudDeploy
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-276](https://cwe.mitre.org/data/definitions/276.html)

CloudDeploy without Permissions parameter creates public cloud objects accessible to anyone.

**Example:**
```mathematica
(* VIOLATION *)
CloudDeploy[form]  (* Public by default! *)

(* COMPLIANT *)
CloudDeploy[form, Permissions -> "Private"]
CloudDeploy[form, Permissions -> {"user@example.com" -> "Read"}]
```

---

### 4.14 Dynamic Content Injection
**Severity:** CRITICAL | **Type:** VULNERABILITY
**CWE:** [CWE-94](https://cwe.mitre.org/data/definitions/94.html)

Using ToExpression or Symbol on user input in Dynamic creates code injection vulnerabilities.

**Example:**
```mathematica
(* VIOLATION *)
Dynamic[ToExpression[userInput]]  (* User can execute arbitrary code! *)

(* COMPLIANT *)
Dynamic[SafeEvaluate[userInput]]  (* Use whitelist/sanitization *)
```

---

## 5. Security Hotspot Rules (7 Total)

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

### 5.4 Network Operations
**Severity:** MAJOR | **Type:** SECURITY_HOTSPOT
**CWE:** [CWE-918](https://cwe.mitre.org/data/definitions/918.html), [CWE-400](https://cwe.mitre.org/data/definitions/400.html)

Flags network operations (sockets, HTTP requests) for security review.

**Example:**
```mathematica
(* REQUIRES REVIEW *)
socket = SocketConnect[{serverAddress, port}];  (* Check: authentication? encryption? *)
URLExecute["POST", apiUrl, data];  (* Check: HTTPS? timeout? input validation? *)
SocketListen[8080];  (* Check: access control? rate limiting? *)

(* GOOD PRACTICES *)
(* 1. Always use HTTPS for sensitive data *)
URLFetch["https://api.example.com/endpoint"];  (* Not HTTP! *)

(* 2. Validate remote addresses *)
allowedServers = {"api.trusted.com", "data.example.org"};
If[MemberQ[allowedServers, serverAddress],
  SocketConnect[{serverAddress, 443}],
  $Failed
];

(* 3. Add timeouts *)
TimeConstrained[URLRead[apiUrl], 30];  (* 30 second timeout *)

(* 4. Implement rate limiting *)
If[requestCount > maxRequestsPerMinute, Pause[60]];
```

### 📍 How to View in SonarQube UI

**Method 1: Security Hotspots Tab**
1. **Security Hotspots** tab in left sidebar
2. Look for "Network Operations" category
3. Review each network call to ensure:
   - HTTPS is used (not HTTP)
   - Timeouts are configured
   - Remote addresses are validated
   - Rate limiting is implemented
4. Mark as **Safe** if all safeguards are present

**Method 2: Filter by Category**
1. **Security Hotspots** tab
2. Filter **"Category"** → Select network-related
3. Review each operation
4. Mark status: Safe / Fix Required / Acknowledged

---

### 5.5 File System Modifications
**Severity:** MAJOR | **Type:** SECURITY_HOTSPOT
**CWE:** [CWE-732](https://cwe.mitre.org/data/definitions/732.html), [CWE-434](https://cwe.mitre.org/data/definitions/434.html)

Flags file write, delete, and modification operations for security review.

**Example:**
```mathematica
(* REQUIRES REVIEW *)
Export[fileName, data];  (* Check: path validation? permissions? overwrite protection? *)
DeleteFile[filePath];  (* Check: authorization? irreversible! *)
CreateDirectory[dirPath];  (* Check: path traversal? permissions? *)

(* GOOD PRACTICES *)
(* 1. Validate file paths *)
If[!StringStartsQ[fileName, "/safe/data/directory/"],
  $Failed,  (* Reject paths outside allowed directory *)
  Export[fileName, data]
];

(* 2. Sanitize file names *)
safeFileName = FileNameTake[userFileName];  (* Remove directory traversal *)
safeFileName = StringReplace[safeFileName,
  RegularExpression["[^a-zA-Z0-9_.-]"] -> ""  (* Remove dangerous chars *)
];

(* 3. Check before destructive operations *)
If[FileExistsQ[filePath] && confirmDelete === True,
  DeleteFile[filePath],
  Print["File not deleted - confirmation required"]
];

(* 4. Set restrictive permissions *)
Export["/secure/data.bin", sensitiveData, "Byte"];
(* Then use shell to set permissions: chmod 600 /secure/data.bin *)
```

### 📍 How to View in SonarQube UI

**Method 1: Security Hotspots Tab**
1. **Security Hotspots** tab
2. Look for "File System Modifications" category
3. Review each file operation to ensure:
   - Paths are validated (no directory traversal)
   - File names are sanitized
   - Destructive operations have confirmation
   - Sensitive files have restrictive permissions
4. Mark as **Safe** if proper safeguards are in place

**Method 2: High Priority First**
1. **Security Hotspots** tab
2. Filter **"Priority"** → **"High"**
3. → File delete operations typically marked high priority
4. Review authorization checks
5. Mark status appropriately

---

### 5.6 Environment Variable Access
**Severity:** MINOR | **Type:** SECURITY_HOTSPOT
**CWE:** [CWE-526](https://cwe.mitre.org/data/definitions/526.html), [CWE-214](https://cwe.mitre.org/data/definitions/214.html)

Flags Environment[] calls for review of information disclosure risks.

**Example:**
```mathematica
(* REQUIRES REVIEW *)
apiKey = Environment["API_KEY"];  (* Check: logging? error messages? *)
path = Environment["PATH"];  (* Check: exposed in logs or UI? *)

(* GOOD PRACTICES *)
(* 1. Don't log environment variables *)
apiKey = Environment["API_KEY"];
(* BAD: Print["Using API key: " <> apiKey]; *)
(* GOOD: If[StringQ[apiKey], Print["API key loaded"], Print["Warning: API key missing"]]; *)

(* 2. Don't expose in error messages *)
result = Check[
  APICall[Environment["SECRET_TOKEN"]],
  (* BAD: Print["Failed with token: " <> $MessageList]; *)
  Print["API call failed"];  (* GOOD: Generic message *)
  $Failed
];

(* 3. Whitelist safe environment variables *)
publicEnvVars = {"LANG", "TZ", "USER"};
If[MemberQ[publicEnvVars, varName],
  Environment[varName],  (* Safe to use/log *)
  $Failed  (* Don't expose secrets *)
];
```

### 📍 How to View in SonarQube UI

**Method 1: Security Hotspots Tab**
1. **Security Hotspots** tab
2. Look for "Environment Variable" category
3. Review each Environment[] call to ensure:
   - Environment variables are not logged
   - Secrets are not exposed in error messages
   - Variables are not displayed in UI or reports
4. Mark as **Safe** if properly protected

**Method 2: Search and Review**
1. **Security Hotspots** tab
2. Search: `Environment`
3. → Shows all Environment[] calls
4. Review each for potential information disclosure
5. Mark status: Safe / Fix Required / Acknowledged

**Workflow Tip:**
Environment variable hotspots are usually MINOR priority unless they're exposed in logs or UI. Focus on reviewing:
- Calls in logging/error handling code (high risk)
- Calls in UI display code (high risk)
- Calls in internal processing (low risk if not logged)

---

### 5.7 Import Without Format Specification
**Severity:** MAJOR | **Type:** SECURITY_HOTSPOT
**CWE:** [CWE-434](https://cwe.mitre.org/data/definitions/434.html)

Import without format specification guesses by file extension, which attackers can manipulate.

**Example:**
```mathematica
(* REQUIRES REVIEW *)
Import[userFile]  (* Guesses format - could execute .mx! *)

(* GOOD PRACTICES *)
(* 1. Always specify format explicitly *)
Import[userFile, "CSV"]  (* Explicit format, safe *)
Import[userFile, "JSON"]

(* 2. Validate file extension before import *)
If[StringEndsQ[userFile, ".csv"],
  Import[userFile, "CSV"],
  $Failed  (* Reject unexpected extensions *)
];

(* 3. Use safe formats only *)
safeFormats = {"CSV", "JSON", "TSV", "Text"};
If[MemberQ[safeFormats, format],
  Import[userFile, format],
  $Failed  (* Reject dangerous formats like MX *)
];
```

### 📍 How to View in SonarQube UI

**Method 1: Security Hotspots Tab**
1. **Security Hotspots** tab
2. Look for "Import Without Format" category
3. Review each Import[] call to ensure:
   - Format is explicitly specified
   - File extension is validated
   - Only safe formats are allowed
4. Mark as **Safe** if proper validation is in place

**Method 2: Search and Review**
1. **Security Hotspots** tab
2. Search: `Import`
3. → Shows all Import[] calls without format
4. Review each to add explicit format specification
5. Mark status: Safe / Fix Required / Acknowledged

**Dangerous Formats to Avoid:**
- `.mx` files can execute arbitrary code during import
- `.wl` and `.m` files can contain executable code
- `.nb` notebook files may contain Dynamic content

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

## 7. Advanced Rules - Chunk 1 (35 New Rules)

**Introduced:** Chunk 1 from ROADMAP_325.md (Items 16-50)
**Focus:** Pattern system optimization, list/array safety, and association best practices

### 7.1 Pattern System Rules (15 Rules)

Advanced pattern matching analysis for better type safety and performance:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Unrestricted Blank Pattern | Code Smell | `f[x_] := x^2` without type restrictions on numeric operations |
| PatternTest vs Condition | Bug | Inefficient `/;` when `?` would be better |
| BlankSequence Without Restriction | Code Smell | `x__` without type restrictions |
| Nested Optional Patterns | Bug | Optional defaults depending on other parameters |
| Pattern Naming Conflicts | Bug | Same pattern name with conflicting types |
| Repeated Pattern Alternatives | Code Smell | `x_Integer | x_Real` instead of `x:(_Integer | _Real)` |
| PatternTest With Pure Function | Performance | Pure functions in `?` creating closures |
| Missing Pattern Defaults | Bug | Optional arguments without proper validation |
| Order-Dependent Patterns | Bug | Specific patterns defined after general ones |
| Verbatim Pattern Misuse | Bug | Incorrect use of `Verbatim` |
| HoldPattern Unnecessary | Code Smell | Unnecessary `HoldPattern` adding clutter |
| Longest/Shortest Without Ordering | Bug | `Longest`/`Shortest` without alternatives |
| Pattern Repeated Different Types | Bug | `{x_, x_}` not checking equality |
| Alternatives Too Complex | Performance | 10+ alternatives causing backtracking |
| Pattern Matching Large Lists | Performance | Pattern matching on 1000+ element lists |

**📍 How to View:** Issues → Search "pattern" → Filter by rule name

### 7.2 List/Array Rules (10 Rules)

Safety and performance rules for list operations:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Empty List Indexing | Bug | `list[[1]]` without `Length` check |
| Negative Index Without Validation | Bug | `list[[-n]]` without bounds check |
| Part Assignment To Immutable | Bug | `{1,2,3}[[1]] = 5` doesn't modify anything |
| Inefficient List Concatenation | Performance | `Join` in loops (O(n²) complexity) |
| Unnecessary Flatten | Performance | `Flatten` on already-flat lists |
| Length In Loop Condition | Performance | Recalculating `Length` in loop iterator |
| Reverse Twice | Code Smell | `Reverse[Reverse[...]]` is a no-op |
| Sort Without Comparison | Performance | `Sort[list, Greater]` vs `Reverse[Sort[list]]` |
| Position vs Select | Performance | `Extract[..., Position[...]]` vs `Select` |
| Nested Part Extraction | Code Smell | `list[[i]][[j]]` vs `list[[i, j]]` |

**📍 How to View:** Issues → Search "list" → Filter by rule name

### 7.3 Association Rules (10 Rules)

Best practices for Mathematica's dictionary/map type:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Missing Key Check | Bug | Accessing keys without `KeyExistsQ` |
| Association vs List Confusion | Bug | Using list operations on associations |
| Inefficient Key Lookup | Performance | `Select[Keys[...]]` vs `KeySelect` |
| Query On Non-Dataset | Bug | `Query` without `Dataset` wrapper |
| Association Update Pattern | Code Smell | Ambiguous direct assignment vs `AssociateTo` |
| Merge Without Conflict Strategy | Bug | `Merge` without specifying merge function |
| AssociateTo On Non-Symbol | Bug | `AssociateTo` on literal instead of variable |
| KeyDrop Multiple Times | Performance | Chained `KeyDrop` vs single call |
| Lookup With Missing Default | Code Smell | Redundant `Missing[]` default |
| GroupBy Without Aggregation | Code Smell | `GroupBy` where `GatherBy` clearer |

**📍 How to View:** Issues → Search "association" → Filter by rule name

**Rule Count Summary:**
- Pattern System: 15 rules (type safety + performance)
- List/Array: 10 rules (safety + performance)
- Association: 10 rules (correctness + clarity)
- **Total: 35 new rules**

---

## 8. Advanced Rules - Chunk 2 (50 New Rules)

**Introduced:** Chunk 2 from ROADMAP_325.md (Items 61-100)
**Focus:** Symbol table analysis, unused code detection, shadowing/naming issues, and undefined symbol detection

### 8.1 Unused Code Detection (15 Rules)

Dead code and unused variable detection for cleaner code:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Unused Private Function | Code Smell | Private functions never called anywhere |
| Unused Function Parameter | Code Smell | Parameters declared but never referenced in body |
| Unused Module Variable | Code Smell | Variables in `Module[{x,y}, ...]` never used |
| Unused With Variable | Code Smell | Variables in `With[{a=1, b=2}, ...]` never used |
| Unused Import | Code Smell | `Needs[]` packages never referenced |
| Unused Pattern Name | Code Smell | Named patterns like `f[x_, y_]` where `y` unused |
| Unused Optional Parameter | Code Smell | Optional parameters `f[x_, opts___]` never accessed |
| Dead Code After Return | Bug | Code following `Return[]` is unreachable |
| Unreachable After Abort/Throw | Bug | Code after `Abort[]` or `Throw[]` never executes |
| Assignment Never Read | Code Smell | Variable assigned but overwritten before use |
| Function Defined But Never Called | Code Smell | Global functions defined but never invoked |
| Redefined Without Use | Bug | Variable reassigned before previous value is used |
| Loop Iterator Unused | Code Smell | `Do[..., {i, 1, n}]` where `i` never referenced |
| Catch Without Throw | Code Smell | `Catch[]` without corresponding `Throw` |
| Condition Always False | Bug | `If[False, ...]` branch never executes |

**📍 How to View:** Issues → Search "unused" or "dead" → Filter by rule name

### 8.2 Shadowing & Naming (15 Rules)

Variable naming and scoping best practices:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Local Shadows Global | Code Smell | Module variable shadows global variable name |
| Parameter Shadows Built-in | Bug | Parameter like `f[List_]` shadows `List` |
| Local Shadows Parameter | Bug | `Module[{x}, ...]` inside `f[x_]` shadows parameter |
| Multiple Definitions Same Symbol | Code Smell | Same function name defined multiple times |
| Symbol Name Too Short | Code Smell | Single-letter vars in large (>50 line) functions |
| Symbol Name Too Long | Code Smell | Variable names exceeding 50 characters |
| Inconsistent Naming Convention | Code Smell | Mix of camelCase, snake_case, PascalCase |
| Built-in Name In Local Scope | Code Smell | `Module[{Map, Apply}, ...]` shadows built-ins |
| Context Conflicts | Bug | Symbol defined in multiple contexts |
| Reserved Name Usage | Bug | Assigning to `$SystemID`, `$Version`, etc. |
| Private Context Symbol Public | Code Smell | Using `Package`Private`symbol` from outside |
| Mismatched Begin/End | Bug | Unmatched `BeginPackage`/`EndPackage` pairs |
| Symbol After EndPackage | Bug | Definition after `EndPackage[]` in wrong context |
| Global In Package | Code Smell | `Global`temp` used in package code |
| Temp Variable Not Temp | Code Smell | Variable named `temp`/`tmp` used repeatedly |

**📍 How to View:** Issues → Search "shadow" or "naming" → Filter by rule name

### 8.3 Undefined Symbol Detection (10 Rules)

Catch typos and missing imports before runtime:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Undefined Function Call | Bug | Calling function that's never defined or imported |
| Undefined Variable Reference | Bug | Using variable before it's assigned |
| Typo In Built-in Name | Bug | Common typos like `Lenght`, `Frist`, `Lats` |
| Wrong Capitalization | Bug | Using `length` instead of `Length` |
| Missing Import | Bug | Using `Package`Symbol` without `Needs["Package`"]` |
| Context Not Found | Bug | `Needs["NonExistent`"]` for package that doesn't exist |
| Symbol Masked By Import | Bug | Local symbol overridden by package import |
| Missing $Path Entry | Bug | `Get["file.m"]` where file not in `$Path` |
| Circular Needs | Bug | Package A needs B which needs A |
| Forward Reference Without Declaration | Bug | Using `g[f[x]]` before `f` is defined |

**📍 How to View:** Issues → Search "undefined" or "typo" → Filter by rule name

**Rule Count Summary:**
- Unused Code Detection: 15 rules (dead code elimination)
- Shadowing & Naming: 15 rules (code clarity)
- Undefined Symbol Detection: 10 rules (runtime error prevention)
- **Total: 50 new rules**

---

## 9. Advanced Rules - Chunk 3 (36 New Rules)

**Introduced:** Chunk 3 from ROADMAP_325.md (Items 111-150)
**Focus:** Type mismatch detection and data flow analysis for enhanced type safety and variable tracking

### 9.1 Type Mismatch Detection (20 Rules)

Catch type errors before runtime:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Numeric Operation On String | Bug | Arithmetic operations on string literals (e.g., `"hello" + 1`) |
| String Operation On Number | Bug | String functions on numeric values (e.g., `StringLength[42]`) |
| Wrong Argument Type | Bug | Functions called with incorrect argument types |
| Function Returns Wrong Type | Bug | Return value doesn't match expected type from signature |
| Comparison Incompatible Types | Bug | Comparing incompatible types (e.g., `"hello" == 5`) |
| Mixed Numeric Types | Code Smell | Mixing Integer and Real without explicit conversion |
| Integer Division Expecting Real | Bug | Using `/` when `N[...]` or explicit conversion needed |
| List Function On Association | Bug | Using `Part`, `Length` on Association (use `Lookup`, `KeyTake` instead) |
| Pattern Type Mismatch | Bug | Pattern declares type that doesn't match usage (e.g., `f[x_Integer] := x/2.0`) |
| Optional Type Inconsistent | Bug | Optional parameter type doesn't match default value |
| Return Type Inconsistent | Bug | Function returns different types in different branches |
| Null Assignment To Typed Variable | Bug | Assigning `Null` to variable with type annotation |
| Type Cast Without Validation | Security Hotspot | `ToExpression` without `Check` or validation |
| Implicit Type Conversion | Code Smell | Relying on automatic type coercion (make explicit) |
| Graphics Object In Numeric Context | Bug | Using Graphics/Graphics3D where number expected |
| Symbol In Numeric Context | Bug | Unevaluated symbol in arithmetic (e.g., `x + 1` where `x` undefined) |
| Image Operation On Non-Image | Bug | Using `ImageData`, `ImageDimensions` on non-Image |
| Sound Operation On Non-Sound | Bug | Using `AudioData` on non-Audio object |
| Dataset Operation On List | Code Smell | Using Dataset functions on plain List (inefficient) |
| Graph Operation On Non-Graph | Bug | Using `VertexList`, `EdgeList` on non-Graph |

**📍 How to View:** Issues → Search "type" or "mismatch" → Filter by rule name

### 9.2 Data Flow Analysis (16 Rules)

Track variable initialization, assignment, and usage:

| Rule | Type | What It Detects |
|------|------|-----------------|
| Uninitialized Variable Use Enhanced | Bug | Using variable before any assignment (enhanced detection) |
| Variable May Be Uninitialized | Bug | Variable may be uninitialized in some code paths |
| Dead Store | Code Smell | Assignment immediately overwritten (never read) |
| Overwritten Before Read | Code Smell | Variable assigned, then reassigned before first use |
| Variable Aliasing Issue | Bug | Multiple references to mutable structure causing confusion |
| Modification Of Loop Iterator | Bug | Changing loop iterator variable inside loop body |
| Use Of Iterator Outside Loop | Bug | Referencing loop iterator after loop completes |
| Reading Unset Variable | Bug | Using variable after `Unset` or `Clear` |
| Double Assignment Same Value | Code Smell | Assigning same value twice (e.g., `x=5; x=5;`) |
| Mutation In Pure Function | Bug | Side-effect in pure function (`Function`, `&`) |
| Shared Mutable State | Code Smell | Global variable modified by multiple functions |
| Variable Scope Escape | Bug | Local variable reference escapes scope (e.g., in closure) |
| Closure Over Mutable Variable | Code Smell | Function captures variable that changes after definition |
| Assignment In Condition Enhanced | Bug | Assignment in `If`, `While` condition (likely typo) |
| Assignment As Return Value | Code Smell | Using `x = value` as return (use `;` or explicit `Return`) |
| Variable Never Modified | Code Smell | Variable assigned once, never changed (consider constant) |

**📍 How to View:** Issues → Search "uninitialized" or "assignment" → Filter by rule name

**Rule Count Summary:**
- Type Mismatch Detection: 20 rules (type safety)
- Data Flow Analysis: 16 rules (variable tracking)
- **Total: 36 new rules**

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
- These files ARE fully analyzed up to 35,000 lines

**Performance Protection:**
- Files >35,000 lines: Reported for File Length violation, then skip detailed analysis
- Files >2MB: Skip detailed analysis (too large to process efficiently)

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
