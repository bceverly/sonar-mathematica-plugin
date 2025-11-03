# Mathematica Best Practices Guide

**For use with the SonarQube Mathematica Plugin**

This guide provides comprehensive best practices for writing clean, secure, performant, and maintainable Wolfram Mathematica code. All recommendations are backed by the plugin's 529 static analysis rules.

---

## Table of Contents

1. [Writing Clean Mathematica Code](#writing-clean-mathematica-code)
2. [Security Best Practices](#security-best-practices)
3. [Performance Best Practices](#performance-best-practices)
4. [Code Organization](#code-organization)
5. [Naming Conventions](#naming-conventions)
6. [Documentation Standards](#documentation-standards)
7. [Testing Practices](#testing-practices)
8. [Paclet Development Patterns](#paclet-development-patterns)

---

## Writing Clean Mathematica Code

### 1. Avoid Magic Numbers

Magic numbers make code harder to understand and maintain.

**Bad:**
```mathematica
(* What does 86400 represent? *)
secondsInPeriod = 86400 * dayCount;

(* What is 3.14159? *)
circleArea = 3.14159 * r^2;
```

**Good:**
```mathematica
(* Use named constants *)
secondsPerDay = 86400;
secondsInPeriod = secondsPerDay * dayCount;

(* Use built-in constants *)
circleArea = Pi * r^2;

(* Or define constants at the top of your module *)
$MaxRetries = 3;
$TimeoutSeconds = 30;
```

**Plugin Rule:** `MagicNumber` (Code Smell, Major)

---

### 2. Remove Commented-Out Code

Commented-out code clutters your codebase and confuses future developers.

**Bad:**
```mathematica
ProcessData[data_] := Module[{result},
  (* Old approach - doesn't work
  result = Map[OldTransform, data];
  result = Select[result, # > 0 &];
  *)

  (* Another attempt
  result = Cases[data, x_ /; x > 0];
  *)

  (* Yet another try
  result = Pick[data, Thread[data > 0]];
  *)

  result = Select[data, Positive]
];
```

**Good:**
```mathematica
ProcessData[data_] := Select[data, Positive];

(* Use version control (Git) for history, not comments *)
```

**Plugin Rule:** `CommentedCode` (Code Smell, Major)

---

### 3. Avoid Deep Nesting

Deeply nested code is hard to read and understand.

**Bad:**
```mathematica
ProcessTransaction[txn_] := Module[{result},
  If[ValidateTransaction[txn],
    If[CheckBalance[txn],
      If[CheckLimits[txn],
        If[ApproveTransaction[txn],
          result = CompleteTransaction[txn];
          If[result === $Failed,
            LogError["Transaction failed"];
            result = RollbackTransaction[txn];
            If[result === $Failed,
              LogCritical["Rollback failed!"];
            ];
          ];
        ];
      ];
    ];
  ];
  result
];
```

**Good:**
```mathematica
ProcessTransaction[txn_] := Module[{result},
  (* Early returns reduce nesting *)
  If[!ValidateTransaction[txn], Return[$Failed]];
  If[!CheckBalance[txn], Return[$Failed]];
  If[!CheckLimits[txn], Return[$Failed]];
  If[!ApproveTransaction[txn], Return[$Failed]];

  result = CompleteTransaction[txn];
  If[result === $Failed,
    LogError["Transaction failed"];
    RollbackTransaction[txn]
  ];

  result
];

(* Or use functional composition *)
ProcessTransaction[txn_] :=
  Fold[If[#2[txn], #1, Return[$Failed]] &,
    txn,
    {ValidateTransaction, CheckBalance, CheckLimits, ApproveTransaction}
  ] // CompleteTransaction;
```

**Plugin Rule:** `DeeplyNestedConditionals` (Code Smell, Major)

---

### 4. Keep Functions Short

Long functions are hard to test, understand, and maintain.

**Bad:**
```mathematica
(* 200+ lines in one function *)
AnalyzeDataSet[data_] := Module[
  {cleaned, validated, transformed, aggregated, formatted, exported, ...},

  (* 50 lines of data cleaning *)
  cleaned = ...;

  (* 30 lines of validation *)
  validated = ...;

  (* 40 lines of transformation *)
  transformed = ...;

  (* 30 lines of aggregation *)
  aggregated = ...;

  (* 50 lines of formatting *)
  formatted = ...;

  formatted
];
```

**Good:**
```mathematica
(* Break into smaller, focused functions *)
CleanData[data_] := DeleteCases[data, Missing[]];

ValidateData[data_] := Select[data, NumericQ];

TransformData[data_] := Log[data + 1];

AggregateData[data_] := Mean /@ Partition[data, 10];

FormatResults[data_] := TableForm[data, TableHeadings -> {"Group", "Mean"}];

(* Main function orchestrates *)
AnalyzeDataSet[data_] :=
  data // CleanData // ValidateData // TransformData //
    AggregateData // FormatResults;
```

**Plugin Rule:** `FunctionLength` (Code Smell, Major)

---

### 5. Avoid Too Many Parameters

Functions with many parameters are hard to use correctly.

**Bad:**
```mathematica
GenerateReport[
  data_, startDate_, endDate_, format_, includeGraphs_,
  includeTable_, outputPath_, emailRecipients_, includeRaw_,
  compressionLevel_, colorScheme_, fontSize_
] := Module[{...}, ...];
```

**Good:**
```mathematica
(* Use an options pattern *)
Options[GenerateReport] = {
  "Format" -> "PDF",
  "IncludeGraphs" -> True,
  "IncludeTable" -> True,
  "OutputPath" -> "~/reports/",
  "EmailRecipients" -> {},
  "IncludeRawData" -> False,
  "CompressionLevel" -> 6,
  "ColorScheme" -> "Default",
  "FontSize" -> 12
};

GenerateReport[data_, dateRange_DateInterval, opts : OptionsPattern[]] :=
  Module[{format, graphs, ...},
    format = OptionValue["Format"];
    graphs = OptionValue["IncludeGraphs"];
    (* ... *)
  ];

(* Usage *)
GenerateReport[
  data,
  DateInterval[{startDate, endDate}],
  "Format" -> "HTML",
  "IncludeGraphs" -> False
];
```

**Plugin Rule:** `TooManyParameters` (Code Smell, Major)

---

### 6. Use Pattern Testing Properly

Unrestricted blank patterns can lead to unexpected behavior.

**Bad:**
```mathematica
(* Accepts anything - may fail at runtime *)
Divide[x_, y_] := x / y;

Divide[5, 0]  (* Division by zero! *)
Divide["hello", "world"]  (* Nonsense result *)
```

**Good:**
```mathematica
(* Restrict patterns with tests *)
Divide[x_?NumericQ, y_?NumericQ] /; y =!= 0 := x / y;
Divide[_, 0] := (Message[Divide::divzero]; $Failed);
Divide[_, _] := (Message[Divide::argtype]; $Failed);

(* Or use more specific patterns *)
SafeDivide[x : (_Integer | _Real), y : (_Integer | _Real)] /; y != 0 :=
  x / y;
```

**Plugin Rule:** `UnrestrictedBlankPattern` (Bug, Major)

---

### 7. Avoid Floating Point Equality

Floating point comparisons are unreliable due to rounding errors.

**Bad:**
```mathematica
x = 0.1 + 0.2;
If[x == 0.3, Print["Equal"], Print["Not Equal"]]
(* Prints "Not Equal" due to floating point precision! *)

IsZero[x_] := x == 0.0;  (* Unreliable *)
```

**Good:**
```mathematica
(* Use tolerance-based comparison *)
ApproxEqual[x_, y_, tol_ : 10^-10] := Abs[x - y] < tol;

x = 0.1 + 0.2;
If[ApproxEqual[x, 0.3], Print["Equal"], Print["Not Equal"]]
(* Prints "Equal" *)

(* Or use exact arithmetic *)
x = 1/10 + 2/10;  (* Exact rational *)
If[x == 3/10, Print["Equal"], Print["Not Equal"]]
(* Prints "Equal" *)

(* For checking zero *)
IsZero[x_, tol_ : 10^-10] := Abs[x] < tol;
```

**Plugin Rule:** `FloatingPointEquality` (Bug, Major)

---

### 8. Handle Empty Lists

Many list operations fail on empty lists.

**Bad:**
```mathematica
GetFirstElement[list_] := list[[1]];  (* Fails on {} *)

CalculateStatistics[data_] := {
  Mean[data],      (* Fails on {} *)
  Max[data],       (* Fails on {} *)
  Min[data]        (* Fails on {} *)
};
```

**Good:**
```mathematica
(* Guard against empty lists *)
GetFirstElement[{}] := $Failed;
GetFirstElement[list_] := First[list];

(* Or use pattern matching *)
GetFirstElement[{first_, ___}] := first;
GetFirstElement[{}] := $Failed;

CalculateStatistics[{}] := Missing["NotEnoughData"];
CalculateStatistics[data_List] := {
  Mean[data],
  Max[data],
  Min[data]
};

(* Or check within the function *)
CalculateStatistics[data_List] /; Length[data] > 0 :=
  {Mean[data], Max[data], Min[data]};
CalculateStatistics[_] := $Failed;
```

**Plugin Rule:** `MissingEmptyListCheck` (Bug, Major)

---

## Security Best Practices

### 1. Never Hardcode Credentials

Hardcoded credentials in source code are a critical security vulnerability.

**Bad:**
```mathematica
(* CRITICAL: Hardcoded credentials *)
apiKey = "sk_live_51H9nQyP5NbR8...";
password = "SuperSecret123!";
awsAccessKey = "AKIAIOSFODNN7EXAMPLE";
dbConnectionString = "Server=db.example.com;Password=admin123;";
```

**Good:**
```mathematica
(* Load from environment variables *)
apiKey = Environment["API_KEY"];
password = Environment["DB_PASSWORD"];
awsAccessKey = Environment["AWS_ACCESS_KEY_ID"];

(* Load from secure configuration file (outside source control) *)
config = Import["~/.config/myapp/secrets.json", "JSON"];
apiKey = config["apiKey"];

(* Use system keychain *)
password = RunProcess[{"security", "find-generic-password",
  "-s", "myapp", "-w"}, "StandardOutput"];

(* Use AWS IAM roles (best for cloud) *)
(* No credentials needed - use instance metadata *)
```

**Security Checklist:**
- ✅ Never commit credentials to version control
- ✅ Use environment variables or secure vaults
- ✅ Add `.env`, `secrets.json` to `.gitignore`
- ✅ Rotate credentials regularly
- ✅ Use least-privilege access

**Plugin Rule:** `HardcodedCredentials` (Vulnerability, Blocker)

---

### 2. Prevent Command Injection

Never construct shell commands from untrusted input.

**Bad:**
```mathematica
(* CRITICAL: Command injection vulnerability *)
userFile = GetUserInput[];
Run["cat " <> userFile];
(* User can input: "file.txt; rm -rf /" *)

searchTerm = GetHTTPParameter["search"];
RunProcess[{"sh", "-c", "grep " <> searchTerm <> " data.txt"}];
(* User can input: "term && cat /etc/passwd" *)

command = FormValue["cmd"];
Import["!" <> command, "Text"];
```

**Good:**
```mathematica
(* Use array form - arguments cannot be interpreted as shell commands *)
userFile = GetUserInput[];
RunProcess[{"cat", userFile}];  (* Safe: userFile is single argument *)

searchTerm = GetHTTPParameter["search"];
RunProcess[{"grep", searchTerm, "data.txt"}];  (* Safe: no shell *)

(* Better: Use Mathematica APIs instead of shell *)
userFile = GetUserInput[];
fileContent = Import[userFile, "Text"];  (* No shell involved *)

(* Validate input against whitelist *)
ValidateFileName[name_String] := StringMatchQ[name,
  RegularExpression["^[a-zA-Z0-9._-]+$"]];

If[ValidateFileName[userFile],
  Import[userFile, "Text"],
  $Failed
];
```

**Security Checklist:**
- ✅ Never use `Run[]` with string concatenation
- ✅ Use `RunProcess[{cmd, arg1, arg2}]` array form
- ✅ Avoid `"sh", "-c"` pattern with user input
- ✅ Validate all input against whitelists
- ✅ Prefer Mathematica APIs over shell commands

**Plugin Rule:** `CommandInjection` (Vulnerability, Critical)

---

### 3. Prevent SQL Injection

Always use parameterized queries, never string concatenation.

**Bad:**
```mathematica
(* CRITICAL: SQL injection vulnerability *)
userId = GetHTTPParameter["id"];
SQLExecute[conn, "SELECT * FROM users WHERE id=" <> userId];
(* User can input: "1 OR 1=1" to dump entire table *)

userName = FormValue["username"];
query = "DELETE FROM sessions WHERE user='" <> userName <> "'";
SQLExecute[conn, query];
(* User can input: "admin'; DROP TABLE users; --" *)
```

**Good:**
```mathematica
(* Use parameterized queries with ? placeholders *)
userId = GetHTTPParameter["id"];
SQLExecute[conn, "SELECT * FROM users WHERE id=?", {userId}];

userName = FormValue["username"];
SQLExecute[conn, "DELETE FROM sessions WHERE user=?", {userName}];

(* Use SQLWhere for safe filtering *)
SQLSelect[
  conn,
  SQLColumn["id", "email"],
  SQLTable["users"],
  SQLWhere["id" == userId]
];

(* Validate input types *)
ValidateUserId[id_String] := StringMatchQ[id, RegularExpression["^[0-9]+$"]];

If[ValidateUserId[userId],
  SQLExecute[conn, "SELECT * FROM users WHERE id=?", {userId}],
  $Failed
];
```

**Security Checklist:**
- ✅ Always use parameterized queries (`?` placeholders)
- ✅ Never concatenate user input into SQL strings
- ✅ Use `SQLWhere` for dynamic filtering
- ✅ Validate input types and ranges
- ✅ Use principle of least privilege for database users

**Plugin Rule:** `SqlInjection` (Vulnerability, Critical)

---

### 4. Prevent Code Injection

Never evaluate untrusted data as code.

**Bad:**
```mathematica
(* CRITICAL: Code injection vulnerability *)
userFormula = GetHTTPParameter["formula"];
result = ToExpression[userFormula];
(* User can input: "DeleteFile[\"/important/data\"]" *)

apiData = URLFetch["https://api.example.com/formula"];
Evaluate[ToExpression[apiData]];
(* Compromised API could execute arbitrary code *)

dynamicCode = Import["user_uploaded.m"];
Get[dynamicCode];  (* Executes arbitrary code from upload *)
```

**Good:**
```mathematica
(* Validate input against strict whitelist *)
SafeEvaluate[input_String] := Module[{pattern},
  pattern = RegularExpression["^[x0-9+\\-*/^()\\s]+$"];
  If[StringMatchQ[input, pattern],
    ToExpression[input],
    $Failed
  ]
];

userFormula = GetHTTPParameter["formula"];
result = SafeEvaluate[userFormula];  (* Only allows safe math *)

(* Use a sandbox with restricted functions *)
allowedFunctions = {"Sin", "Cos", "Exp", "Log", "Plus", "Times"};
SafeEvaluate[input_String] := Module[{expr},
  expr = ToExpression[input, StandardForm, HoldComplete];
  If[AllTrue[Cases[expr, _Symbol, Infinity],
      MemberQ[allowedFunctions, SymbolName[#]] &],
    ReleaseHold[expr],
    $Failed
  ]
];

(* Better: Don't use ToExpression at all *)
(* Parse and interpret input in a controlled way *)
ParseFormula[input_String] := Module[{tokens, tree},
  tokens = StringSplit[input, RegularExpression["\\s+"]];
  (* Build AST and evaluate safely *)
];
```

**Security Checklist:**
- ✅ Never use `ToExpression` on untrusted input
- ✅ Never use `Get[]` on user-provided files
- ✅ Validate input against strict whitelists
- ✅ Use sandboxing with function restrictions
- ✅ Consider alternative parsing approaches

**Plugin Rule:** `CodeInjection` (Vulnerability, Critical)

---

### 5. Prevent Path Traversal

Never construct file paths from untrusted input.

**Bad:**
```mathematica
(* CRITICAL: Path traversal vulnerability *)
baseDir = "/app/data/";
userFile = GetHTTPParameter["file"];
data = Import[baseDir <> userFile];
(* User can input: "../../etc/passwd" to access system files *)

outputDir = "/output/";
fileName = FormValue["filename"];
Export[outputDir <> fileName, data];
(* User can input: "../../../tmp/evil.sh" to write anywhere *)
```

**Good:**
```mathematica
(* Use FileNameTake to strip directory components *)
baseDir = "/app/data/";
userFile = GetHTTPParameter["file"];
safeFile = FileNameTake[userFile];  (* Strips ../ components *)
safePath = FileNameJoin[{baseDir, safeFile}];

If[FileExistsQ[safePath],
  Import[safePath],
  $Failed
];

(* Validate path stays within base directory *)
ValidatePath[basePath_String, requestedPath_String] := Module[{full},
  full = FileNameJoin[{basePath, requestedPath}];
  StringStartsQ[AbsoluteFileName[full], AbsoluteFileName[basePath]]
];

If[ValidatePath[baseDir, userFile],
  Import[FileNameJoin[{baseDir, userFile}]],
  $Failed
];

(* Use whitelist of allowed files *)
allowedFiles = {"report.pdf", "data.csv", "config.json"};
If[MemberQ[allowedFiles, userFile],
  Import[FileNameJoin[{baseDir, userFile}]],
  $Failed
];
```

**Security Checklist:**
- ✅ Never concatenate user input directly into paths
- ✅ Use `FileNameTake` to extract basename
- ✅ Validate paths stay within base directory
- ✅ Use whitelist of allowed files when possible
- ✅ Check canonical paths to prevent symlink attacks

**Plugin Rule:** `PathTraversal` (Vulnerability, High)

---

### 6. Use Strong Cryptography

Avoid weak or deprecated cryptographic functions.

**Bad:**
```mathematica
(* Weak cryptography *)
hash = Hash[password, "MD5"];  (* MD5 is broken *)
hash = Hash[data, "SHA1"];     (* SHA1 is deprecated *)

(* Weak encryption *)
encrypted = Encrypt["DES", data, key];  (* DES is weak *)

(* Insecure random for security *)
token = RandomInteger[{1, 10^6}];  (* Predictable *)
```

**Good:**
```mathematica
(* Use strong hashing algorithms *)
hash = Hash[password, "SHA256"];
hash = Hash[password, "SHA512"];

(* For passwords, use key derivation *)
salt = RandomBytes[16];
passwordHash = Hash[password <> salt, "SHA256", "ByteArray"];

(* Use strong encryption *)
encrypted = Encrypt["AES256", data, key];

(* Use cryptographically secure random *)
token = RandomBytes[32];
randomKey = ByteArray[RandomInteger[{0, 255}, 32]];

(* For security tokens *)
secureToken = IntegerString[
  Fold[256 #1 + #2 &, 0, Normal[RandomBytes[32]]],
  16
];
```

**Security Checklist:**
- ✅ Use SHA-256 or SHA-512, not MD5 or SHA-1
- ✅ Use AES-256 for encryption
- ✅ Use `RandomBytes[]` for cryptographic randomness
- ✅ Use proper key derivation for passwords
- ✅ Keep cryptographic libraries updated

**Plugin Rule:** `WeakCryptography` (Vulnerability, Critical)

---

## Performance Best Practices

### 1. Avoid Append in Loops

`Append` and `AppendTo` in loops are extremely slow (O(n²) complexity).

**Bad:**
```mathematica
(* Very slow: O(n²) performance *)
results = {};
Do[
  results = Append[results, ComputeValue[i]],
  {i, 10000}
];
(* Takes seconds to minutes! *)

(* Also bad *)
results = {};
For[i = 1, i <= 10000, i++,
  AppendTo[results, ComputeValue[i]]
];
```

**Good:**
```mathematica
(* Fast: O(n) performance using Table *)
results = Table[ComputeValue[i], {i, 10000}];
(* Takes milliseconds! *)

(* Or use Map *)
results = Map[ComputeValue, Range[10000]];

(* Or if you must use Do, preallocate *)
results = ConstantArray[0, 10000];
Do[
  results[[i]] = ComputeValue[i],
  {i, 10000}
];

(* For dynamic cases, use Reap/Sow *)
results = Reap[
  Do[Sow[ComputeValue[i]], {i, 10000}]
][[2, 1]];
```

**Performance Impact:**
- Append in loop: **O(n²)** - 10,000 items takes ~30 seconds
- Table/Map: **O(n)** - 10,000 items takes ~0.03 seconds
- **1000× speedup!**

**Plugin Rule:** `AppendInLoop` (Code Smell, Critical)

---

### 2. Use Compilation for Numerical Code

Compiled code can be 10-1000× faster for numerical operations.

**Bad:**
```mathematica
(* Slow: interpreted *)
SlowFunction[n_Integer] := Module[{sum = 0},
  Do[sum += Sin[i/100.0]^2 + Cos[i/100.0)^2, {i, n}];
  sum
];

SlowFunction[10^6]  (* Takes ~5 seconds *)
```

**Good:**
```mathematica
(* Fast: compiled *)
FastFunction = Compile[{{n, _Integer}},
  Module[{sum = 0.0, i},
    Do[sum += Sin[i/100.0]^2 + Cos[i/100.0]^2, {i, n}];
    sum
  ],
  CompilationTarget -> "C"
];

FastFunction[10^6]  (* Takes ~0.01 seconds *)

(* For listable operations, compile with Parallelization *)
FastListOperation = Compile[{{data, _Real, 1}},
  Map[Sin[#]^2 + Cos[#]^2 &, data],
  CompilationTarget -> "C",
  Parallelization -> True,
  RuntimeAttributes -> {Listable}
];
```

**When to Compile:**
- ✅ Numerical computations with loops
- ✅ Functions with integer/real arguments only
- ✅ No symbolic operations
- ✅ Functions called repeatedly

**Plugin Rule:** `UncompiledNumerical` (Code Smell, Major)

---

### 3. Preserve Packed Arrays

Packed arrays are 10-100× faster but easily broken.

**Bad:**
```mathematica
(* Breaks packing *)
data = Range[1.0, 10^6];  (* Packed *)
Developer`PackedArrayQ[data]  (* True *)

result = Map[If[# > 0.5, #, 0] &, data];  (* If breaks packing! *)
Developer`PackedArrayQ[result]  (* False - now slow! *)

(* Also breaks packing *)
mixed = {1.0, 2.0, "text", 3.0};  (* Non-homogeneous *)
withNull = Append[data, Null];     (* Non-numeric *)
```

**Good:**
```mathematica
(* Preserve packing *)
data = Range[1.0, 10^6];

(* Use vectorized operations - stays packed *)
result = Clip[data, {0.5, ∞}, {0, data}];
Developer`PackedArrayQ[result]  (* True - fast! *)

(* Or use Map with compilable function *)
result = Map[Max[#, 0.5] &, data];

(* Keep arrays homogeneous and numeric *)
integers = Range[100];           (* Packed *)
reals = Range[1.0, 100.0];      (* Packed *)
complex = Range[100] + 0.0 I;   (* Packed *)

(* Check if operation preserves packing *)
CheckPacked[expr_] := Developer`PackedArrayQ[expr];
```

**Operations That Preserve Packing:**
- ✅ Arithmetic: `+`, `-`, `*`, `/`, `^`
- ✅ Math functions: `Sin`, `Cos`, `Exp`, `Log`
- ✅ Comparisons: `>`, `<`, `==` (return packed)
- ✅ `Clip`, `Round`, `Floor`, `Ceiling`
- ✅ `Total`, `Mean`, `Dot`

**Operations That Break Packing:**
- ❌ `If`, `Which`, `Switch` in mapped functions
- ❌ Mixing types (integers + symbols)
- ❌ `Append`, `Prepend`, `Join` with non-numeric
- ❌ `Part` assignment with non-numeric

**Plugin Rule:** `PackedArrayBreaking` (Code Smell, Major)

---

### 4. Cache Repeated Function Calls

Memoization can provide huge speedups for expensive computations.

**Bad:**
```mathematica
(* Recomputes same values repeatedly *)
Fibonacci[n_] := If[n <= 1, n, Fibonacci[n - 1] + Fibonacci[n - 2]];

Fibonacci[30]  (* Takes ~seconds, exponential complexity! *)
```

**Good:**
```mathematica
(* Memoization: cache results *)
Fibonacci[n_] := Fibonacci[n] =
  If[n <= 1, n, Fibonacci[n - 1] + Fibonacci[n - 2]];

Fibonacci[30]  (* Fast: each value computed once *)
Fibonacci[100] (* Fast: O(n) instead of O(2^n) *)

(* Or use explicit caching *)
$CostCache = <||>;
ComputeCost[item_] :=
  Lookup[$CostCache, item,
    $CostCache[item] = ExpensiveComputation[item]
  ];

(* Or use Mathematica's built-in caching *)
ExpensiveFunction[x_] := ExpensiveFunction[x] = SlowComputation[x];
```

**When to Use Memoization:**
- ✅ Recursive functions
- ✅ Expensive computations called multiple times
- ✅ Functions with discrete inputs
- ✅ Limited input space (avoid memory issues)

**Plugin Rule:** `MissingMemoization` (Code Smell, Major)

---

### 5. Use Efficient Data Structures

Choose the right data structure for your use case.

**Bad:**
```mathematica
(* Slow: Linear search in list - O(n) *)
userList = {{"alice", 25}, {"bob", 30}, {"charlie", 35}, ...};
GetUserAge[name_] :=
  Cases[userList, {name, age_} :> age][[1]];

(* Slow: Nested lists as key-value store *)
config = {{"timeout", 30}, {"maxRetries", 3}, {"url", "..."}};
GetConfig[key_] := Cases[config, {key, val_} :> val][[1]];
```

**Good:**
```mathematica
(* Fast: Association lookup - O(1) *)
userData = <|"alice" -> 25, "bob" -> 30, "charlie" -> 35|>;
GetUserAge[name_] := Lookup[userData, name, $Failed];

(* Fast: Association for key-value data *)
config = <|"timeout" -> 30, "maxRetries" -> 3, "url" -> "..."|>;
GetConfig[key_] := Lookup[config, key, $Failed];

(* For large sparse data *)
sparseMatrix = SparseArray[{
  {1, 1} -> 1.5,
  {100, 200} -> 2.3,
  {1000, 1000} -> 4.2
}, {10000, 10000}];
(* Uses ~100 bytes instead of ~800MB for dense array *)

(* For graph-like relationships *)
graph = Graph[{1 -> 2, 2 -> 3, 3 -> 1}];
VertexOutComponent[graph, 1]  (* Fast graph algorithms *)
```

**Data Structure Guide:**
- **List**: Sequential access, small collections
- **Association**: O(1) key-value lookup, dictionaries
- **SparseArray**: Large arrays with few non-zero elements
- **Dataset**: Tabular data with column operations
- **Graph**: Network/relationship data

**Plugin Rule:** `NestedListsInsteadOfAssociation` (Code Smell, Major)

---

### 6. Avoid String Concatenation in Loops

String concatenation is slow and memory-inefficient.

**Bad:**
```mathematica
(* Slow: O(n²) string concatenation *)
result = "";
Do[
  result = result <> ToString[i] <> ", ",
  {i, 10000}
];
(* Very slow and memory-intensive! *)
```

**Good:**
```mathematica
(* Fast: Build list then join once *)
parts = Table[ToString[i], {i, 10000}];
result = StringRiffle[parts, ", "];

(* Or use StringJoin with list *)
result = StringJoin[Riffle[
  Table[ToString[i], {i, 10000}],
  ", "
]];

(* Or use StringTemplate *)
template = StringTemplate["Item ``: ``"];
items = Table[template[i, ComputeValue[i]], {i, 1000}];
result = StringRiffle[items, "\n"];

(* For complex formatting, use Row/ToString *)
result = ToString[Row[Range[10000], ", "], StandardForm];
```

**Plugin Rule:** `StringConcatInLoop` (Code Smell, Major)

---

## Code Organization

### 1. Use Module for Local Variables

Always use proper scoping constructs for local variables.

**Bad:**
```mathematica
(* Pollutes global namespace *)
ComputeStatistics[data_] := (
  mean = Mean[data];      (* Global variable! *)
  stddev = StandardDeviation[data];  (* Global! *)
  {mean, stddev}
);

ComputeStatistics[{1, 2, 3}];
mean  (* Still defined globally - bad! *)
```

**Good:**
```mathematica
(* Use Module for local variables *)
ComputeStatistics[data_] := Module[{mean, stddev},
  mean = Mean[data];
  stddev = StandardDeviation[data];
  {mean, stddev}
];

ComputeStatistics[{1, 2, 3}];
mean  (* Not defined - good! *)

(* Or use Block for dynamic scoping *)
WithLocalSettings[settings_, code_] := Block[
  {$Setting1 = settings["setting1"], $Setting2 = settings["setting2"]},
  code
];

(* Or use With for constant substitution *)
ComputeArea[radius_] := With[{pi = 3.14159265},
  pi * radius^2
];
```

**Scoping Guide:**
- **Module**: Lexical scoping, use for local variables
- **Block**: Dynamic scoping, use for temporary global changes
- **With**: Constant substitution, use for local constants

**Plugin Rule:** `GlobalStateModification` (Code Smell, Major)

---

### 2. Package Structure for Reusable Code

Organize code into packages for maintainability.

**Bad:**
```mathematica
(* Everything in one file, no organization *)
(* utils.m *)

Function1[x_] := ...;
Function2[x_] := ...;
helperFunction[x_] := ...;  (* Should be private *)
$GlobalVar = 5;  (* Exposed to users *)
InternalHelper[x_] := ...;  (* Should be private *)
PublicAPI[x_] := ...;
```

**Good:**
```mathematica
(* MyPackage/MyPackage.m *)
BeginPackage["MyPackage`"];

(* Public API - documented *)
PublicFunction1::usage = "PublicFunction1[x] computes ...";
PublicFunction2::usage = "PublicFunction2[x] performs ...";

Begin["`Private`"];

(* Private helper functions *)
privateHelper[x_] := x^2 + 1;
anotherHelper[data_] := Mean[data];

(* Public implementations *)
PublicFunction1[x_?NumericQ] := privateHelper[x] * 2;

PublicFunction2[data_List] := Module[{processed},
  processed = Map[privateHelper, data];
  anotherHelper[processed]
];

End[];  (* End `Private` *)
EndPackage[];  (* End MyPackage` *)

(* Usage *)
Get["MyPackage`"];
PublicFunction1[5]  (* Works *)
MyPackage`Private`privateHelper[5]  (* Not exposed *)
```

**Package Best Practices:**
- ✅ Use `BeginPackage`/`EndPackage` for public context
- ✅ Use `Begin["`Private`"]`/`End[]` for private functions
- ✅ Document public functions with `usage` messages
- ✅ Keep implementation details private
- ✅ One package per file, file name matches package name

**Plugin Rule:** `MissingPackageDocumentation` (Code Smell, Major)

---

### 3. Separate Interface from Implementation

Keep public API clean and stable.

**Bad:**
```mathematica
(* Implementation details in public API *)
BeginPackage["DataProcessor`"];

ProcessData::usage = "Processes data";
InternalBufferSize::usage = "";  (* Internal detail exposed! *)
HelperParseRow::usage = "";      (* Implementation exposed! *)
$TempStorage::usage = "";        (* Internal state exposed! *)

Begin["`Private`"];
(* ... *)
End[];
EndPackage[];
```

**Good:**
```mathematica
(* Clean public API *)
BeginPackage["DataProcessor`"];

(* Only public interface *)
ProcessData::usage = "ProcessData[data] processes the input data.";
Options[ProcessData] = {"Format" -> "Auto", "Validate" -> True};

Begin["`Private`"];

(* Private implementation details *)
$internalBufferSize = 1024;
helperParseRow[row_] := StringSplit[row, ","];
$tempStorage = <||>;

(* Private validation *)
validateData[data_] := ListQ[data] && Length[data] > 0;

(* Public implementation *)
ProcessData[data_List, opts : OptionsPattern[]] := Module[
  {format, validate, result},

  format = OptionValue["Format"];
  validate = OptionValue["Validate"];

  If[validate && !validateData[data], Return[$Failed]];

  result = Map[helperParseRow, data];
  (* ... *)
  result
];

End[];
EndPackage[];
```

**Plugin Rule:** `InternalImplementationExposed` (Code Smell, Major)

---

## Naming Conventions

### 1. Follow Mathematica Naming Standards

Consistent naming makes code more readable.

**Bad:**
```mathematica
(* Inconsistent naming *)
process_data[x_] := ...;        (* snake_case - not Mathematica style *)
MAXVALUE = 100;                 (* ALL_CAPS - not Mathematica style *)
getxmldata[url_] := ...;        (* No capitalization *)
XML_Parse[data_] := ...;        (* Mixed styles *)
```

**Good:**
```mathematica
(* Mathematica conventions *)
ProcessData[x_] := ...;         (* PascalCase for functions *)
maxValue = 100;                 (* camelCase for local variables *)
$MaxValue = 100;                (* $ prefix for global constants *)
GetXMLData[url_] := ...;        (* Acronyms: XML, HTTP, URL *)
ParseXML[data_] := ...;

(* Private functions *)
Begin["`Private`"];
helperFunction[x_] := ...;      (* camelCase for private helpers *)
processRow[data_] := ...;
End[];

(* Options *)
Options[ProcessData] = {
  "Format" -> "Auto",            (* Strings for option names *)
  "MaxRetries" -> 3,
  "Timeout" -> 30
};
```

**Naming Standards:**
- **Public Functions**: `PascalCase` (e.g., `ProcessData`, `GetUser`)
- **Private Functions**: `camelCase` (e.g., `processRow`, `validateInput`)
- **Local Variables**: `camelCase` (e.g., `result`, `tempData`)
- **Global Variables**: `$PascalCase` (e.g., `$MaxValue`, `$DefaultTimeout`)
- **Options**: `"PascalCase"` strings (e.g., `"Format"`, `"Timeout"`)
- **Packages**: `PascalCase` (e.g., `MyPackage``, `DataProcessor``)

**Plugin Rule:** `InconsistentNamingConvention` (Code Smell, Minor)

---

### 2. Avoid Generic Names

Use descriptive names that convey meaning.

**Bad:**
```mathematica
(* Generic, meaningless names *)
Process[x_] := ...;
DoIt[data_] := ...;
temp = ...;
x = ...;
result = ...;
thing = ...;
```

**Good:**
```mathematica
(* Descriptive, meaningful names *)
ParseCSVFile[filePath_] := ...;
ComputeStatistics[dataset_] := ...;
sanitizedInput = RemoveSpecialCharacters[rawInput];
userId = ExtractUserIdFromToken[token];
aggregatedResults = GroupBy[data, "category"];
userAccount = LookupUserAccount[userId];
```

**Plugin Rule:** `GenericVariableNames` (Code Smell, Minor)

---

### 3. Name Boolean Variables Clearly

Boolean variables should indicate true/false state.

**Bad:**
```mathematica
(* Unclear *)
valid = CheckData[data];
status = IsConnected[];
flag = TestCondition[x];
```

**Good:**
```mathematica
(* Clear boolean names *)
isValid = CheckData[data];
isConnected = IsConnected[];
hasPermission = CheckPermission[user];
shouldRetry = (attemptCount < maxRetries);
canProceed = (isValid && isConnected && hasPermission);

(* Or use question-mark functions *)
ValidDataQ[data_] := ...;
ConnectedQ[] := ...;
PermissionGrantedQ[user_] := ...;
```

**Plugin Rule:** `GenericVariableNames` (Code Smell, Minor)

---

## Documentation Standards

### 1. Document All Public Functions

Every public function must have a usage message.

**Bad:**
```mathematica
(* No documentation *)
BeginPackage["MyPackage`"];

ProcessData;  (* No usage message! *)
ComputeStatistics;  (* No usage message! *)

Begin["`Private`"];
ProcessData[x_] := ...;
ComputeStatistics[data_] := ...;
End[];
EndPackage[];
```

**Good:**
```mathematica
BeginPackage["MyPackage`"];

ProcessData::usage = "\
ProcessData[data] processes the input data and returns cleaned results.
ProcessData[data, format] processes data using the specified format.

Options:
  - \"Validate\" (True) - whether to validate input
  - \"Format\" (\"Auto\") - output format: \"Auto\", \"JSON\", \"CSV\"

Examples:
  ProcessData[{1, 2, 3}]
  ProcessData[data, \"Format\" -> \"JSON\"]";

ComputeStatistics::usage = "\
ComputeStatistics[data] computes mean, median, and standard deviation.

Returns an Association with keys: \"Mean\", \"Median\", \"StandardDeviation\"

Example:
  stats = ComputeStatistics[{1, 2, 3, 4, 5}]
  stats[\"Mean\"]  (* Returns 3 *)";

Begin["`Private`"];
(* Implementation... *)
End[];
EndPackage[];
```

**Documentation Checklist:**
- ✅ One-line summary at the top
- ✅ Parameter descriptions
- ✅ Return value description
- ✅ Available options with defaults
- ✅ Usage examples
- ✅ Any important caveats or warnings

**Plugin Rule:** `MissingUsageMessage` (Code Smell, Major)

---

### 2. Document Complex Algorithms

Add comments explaining non-obvious logic.

**Bad:**
```mathematica
(* No explanation of algorithm *)
Process[data_] := Module[{a, b, c, d, result},
  a = Partition[data, 10];
  b = Map[Total, a];
  c = Differences[b];
  d = Sign[c];
  result = Pick[Range[Length[d]], d, 1];
  result
];
```

**Good:**
```mathematica
(* Finds increasing windows in time series data *)
FindIncreasingWindows[data_] := Module[
  {windows, windowSums, differences, signs, indices},

  (* Break data into windows of size 10 *)
  windows = Partition[data, 10];

  (* Sum each window to get trend *)
  windowSums = Map[Total, windows];

  (* Compute differences between consecutive windows *)
  differences = Differences[windowSums];

  (* Positive differences indicate increasing trend *)
  signs = Sign[differences];

  (* Return indices of increasing windows *)
  indices = Pick[Range[Length[signs]], signs, 1];

  indices
];
```

**When to Add Comments:**
- ✅ Complex algorithms or mathematical formulas
- ✅ Non-obvious optimizations
- ✅ Workarounds for bugs or limitations
- ✅ Security-sensitive code
- ✅ Performance-critical sections

**Plugin Rule:** `MissingDocumentation` (Code Smell, Major)

---

### 3. Document Error Messages

Define custom error messages for functions.

**Bad:**
```mathematica
(* No error messages *)
Divide[x_, y_] := If[y == 0, $Failed, x/y];

ProcessFile[path_] := If[!FileExistsQ[path], $Failed, Import[path]];
```

**Good:**
```mathematica
(* Define error messages *)
Divide::divzero = "Cannot divide by zero.";
Divide::argtype = "Arguments must be numeric.";

Divide[x_?NumericQ, y_?NumericQ] /; y != 0 := x / y;
Divide[_, 0] := (Message[Divide::divzero]; $Failed);
Divide[_, _] := (Message[Divide::argtype]; $Failed);

ProcessFile::nofile = "File `` does not exist.";
ProcessFile::readerr = "Error reading file ``: ``.";

ProcessFile[path_String] := Module[{result},
  If[!FileExistsQ[path],
    Message[ProcessFile::nofile, path];
    Return[$Failed]
  ];

  result = Check[
    Import[path],
    Message[ProcessFile::readerr, path, $MessageList];
    $Failed
  ];

  result
];
```

**Plugin Rule:** `MissingErrorMessages` (Code Smell, Major)

---

## Testing Practices

### 1. Write Unit Tests for All Functions

Use `VerificationTest` for systematic testing.

**Bad:**
```mathematica
(* No tests - hope it works! *)
Factorial[n_] := If[n <= 1, 1, n * Factorial[n - 1]];
```

**Good:**
```mathematica
(* Function implementation *)
Factorial[n_Integer?NonNegative] := If[n <= 1, 1, n * Factorial[n - 1]];
Factorial[_] := $Failed;

(* Comprehensive tests *)
VerificationTest[
  Factorial[0],
  1,
  TestID -> "Factorial-Base-0"
];

VerificationTest[
  Factorial[1],
  1,
  TestID -> "Factorial-Base-1"
];

VerificationTest[
  Factorial[5],
  120,
  TestID -> "Factorial-Normal"
];

VerificationTest[
  Factorial[10],
  3628800,
  TestID -> "Factorial-Large"
];

VerificationTest[
  Factorial[-1],
  $Failed,
  TestID -> "Factorial-Negative"
];

VerificationTest[
  Factorial[3.5],
  $Failed,
  TestID -> "Factorial-NonInteger"
];

VerificationTest[
  Factorial["abc"],
  $Failed,
  TestID -> "Factorial-InvalidType"
];
```

**Test Coverage Checklist:**
- ✅ Normal/happy path cases
- ✅ Edge cases (empty input, zero, one)
- ✅ Boundary conditions
- ✅ Invalid input (wrong type, negative, etc.)
- ✅ Error conditions
- ✅ Large inputs (performance/scalability)

**Plugin Rule:** `ImplementationWithoutTests` (Code Smell, Major)

---

### 2. Use Descriptive Test IDs

Test IDs should clearly indicate what is being tested.

**Bad:**
```mathematica
(* Unclear test IDs *)
VerificationTest[ProcessData[{1, 2, 3}], {1, 2, 3}, TestID -> "Test1"];
VerificationTest[ProcessData[{}], $Failed, TestID -> "Test2"];
VerificationTest[ProcessData["x"], $Failed, TestID -> "Test3"];
```

**Good:**
```mathematica
(* Clear, descriptive test IDs *)
VerificationTest[
  ProcessData[{1, 2, 3}],
  {1, 2, 3},
  TestID -> "ProcessData-ValidList-ReturnsUnmodified"
];

VerificationTest[
  ProcessData[{}],
  $Failed,
  TestID -> "ProcessData-EmptyList-ReturnsFailed"
];

VerificationTest[
  ProcessData["invalid"],
  $Failed,
  TestID -> "ProcessData-StringInput-ReturnsFailed"
];

(* Pattern: FunctionName-InputCondition-ExpectedBehavior *)
```

**Plugin Rule:** Testing quality rules

---

### 3. Test Error Handling

Verify that errors are handled correctly.

**Bad:**
```mathematica
(* Only test success cases *)
VerificationTest[SafeDivide[10, 2], 5];
VerificationTest[SafeDivide[15, 3], 5];
```

**Good:**
```mathematica
(* Test success cases *)
VerificationTest[
  SafeDivide[10, 2],
  5,
  TestID -> "SafeDivide-Normal"
];

(* Test error cases *)
VerificationTest[
  SafeDivide[10, 0],
  $Failed,
  {SafeDivide::divzero},  (* Expect this message *)
  TestID -> "SafeDivide-DivisionByZero"
];

VerificationTest[
  SafeDivide["ten", 2],
  $Failed,
  {SafeDivide::argtype},  (* Expect this message *)
  TestID -> "SafeDivide-NonNumericInput"
];

(* Test edge cases *)
VerificationTest[
  SafeDivide[0, 5],
  0,
  TestID -> "SafeDivide-ZeroNumerator"
];

VerificationTest[
  SafeDivide[1, 10^-20],
  10^20,
  TestID -> "SafeDivide-VerySmallDenominator"
];
```

**Plugin Rule:** Testing quality rules

---

## Paclet Development Patterns

### 1. Proper Paclet Structure

Organize paclets following standard structure.

**Good Structure:**
```
MyPaclet/
├── PacletInfo.wl              # Paclet metadata
├── Kernel/
│   ├── init.m                 # Initialization
│   └── MyPaclet.wl            # Main code
├── Documentation/
│   └── English/
│       ├── Guides/
│       ├── ReferencePages/
│       └── Tutorials/
├── Tests/
│   ├── Basic.wlt              # Basic tests
│   └── Advanced.wlt           # Advanced tests
├── Resources/
│   └── data/                  # Data files
└── FrontEnd/
    └── StyleSheets/           # Custom styles
```

**PacletInfo.wl Example:**
```mathematica
PacletObject[
  <|
    "Name" -> "MyPaclet",
    "Version" -> "1.0.0",
    "WolframVersion" -> "13.0+",
    "Description" -> "A useful paclet for...",
    "Creator" -> "Your Name",
    "License" -> "MIT",
    "Extensions" -> {
      {"Kernel", "Root" -> "Kernel", "Context" -> "MyPaclet`"},
      {"Documentation", "Language" -> "English"},
      {"Asset", "Root" -> "Resources"}
    }
  |>
]
```

---

### 2. Declare Dependencies

Always declare paclet dependencies explicitly.

**Bad:**
```mathematica
(* PacletInfo.wl - no dependencies declared *)
PacletObject[<|
  "Name" -> "MyPaclet",
  "Version" -> "1.0.0"
  (* Missing dependencies! *)
|>]

(* Code uses GeneralUtilities but doesn't declare it *)
Needs["GeneralUtilities`"];
```

**Good:**
```mathematica
(* PacletInfo.wl - dependencies declared *)
PacletObject[<|
  "Name" -> "MyPaclet",
  "Version" -> "1.0.0",
  "Extensions" -> {
    {"Kernel", "Root" -> "Kernel", "Context" -> "MyPaclet`"}
  },
  "Dependencies" -> {
    "GeneralUtilities",
    {"HTTPClient", "1.0.0"},  (* Minimum version *)
    {"DataRepository", "2.0+"}  (* 2.0 or higher *)
  }
|>]
```

**Plugin Rule:** `MissingImport`, `UnusedImport`

---

### 3. Version Your API Changes

Follow semantic versioning for paclets.

**Semantic Versioning:**
- **Major** (1.0.0 → 2.0.0): Breaking changes
- **Minor** (1.0.0 → 1.1.0): New features, backward compatible
- **Patch** (1.0.0 → 1.0.1): Bug fixes, backward compatible

**Example:**
```mathematica
(* Version 1.0.0 - Initial release *)
ProcessData[data_List] := ...;

(* Version 1.1.0 - Added new optional parameter, backward compatible *)
ProcessData[data_List, format_: "Auto"] := ...;

(* Version 1.2.0 - Added new function, backward compatible *)
ValidateData[data_List] := ...;

(* Version 2.0.0 - Changed return type, BREAKING CHANGE *)
(* Old: returned List, New: returns Association *)
ProcessData[data_List, format_: "Auto"] := <|"Result" -> ...|>;
```

**Plugin Rule:** `PublicAPIChangedWithoutVersionBump`

---

### 4. Write Documentation Notebooks

Provide comprehensive documentation for paclets.

**Guide Page Example:**
```mathematica
(* Documentation/English/Guides/MyPacletGuide.nb *)

Cell["MyPaclet Guide", "GuideTitle"],

Cell["MyPaclet provides tools for ...", "GuideAbstract"],

Cell[CellGroupData[{
  Cell["Core Functions", "GuideFunctionsSubsection"],
  Cell[TextData[{
    Cell[BoxData[ButtonBox["ProcessData", ...]], "InlineGuideFunction"],
    " processes input data"
  }], "GuideText"],
  Cell[TextData[{
    Cell[BoxData[ButtonBox["ValidateData", ...]], "InlineGuideFunction"],
    " validates data integrity"
  }], "GuideText"]
}]]

(* Reference Page for Each Function *)
(* Documentation/English/ReferencePages/Symbols/ProcessData.nb *)
```

---

## Summary

Following these best practices will help you write:
- **Cleaner code** that is easier to understand and maintain
- **More secure applications** resistant to common vulnerabilities
- **Faster programs** that make efficient use of Mathematica's capabilities
- **Better organized projects** that scale well
- **Well-documented APIs** that others can use confidently
- **Thoroughly tested code** that behaves correctly

The SonarQube Mathematica Plugin's 529 rules are designed to automatically detect violations of these practices, helping you maintain high code quality throughout your project's lifecycle.

For more information:
- See the full rule catalog at `/docs/rule-catalog.json`
- Review example code in `/examples/`
- Check the README for plugin installation and usage
- Visit the SonarQube documentation for quality gate configuration
