# Bug Detection Rules

This page documents the **162 Bug Detection rules** that find reliability issues - actual defects that will cause runtime errors, incorrect results, or program crashes.

**See also:** [[Security Vulnerabilities]] | [[Code Smells]] | [[Rule Catalog]]

---

## Top 20 Critical Bugs (with examples)

### 1. Division by Zero
```mathematica
(* BUG *)
Calculate[x_, y_] := x / y  (* Crashes when y = 0 *)

(* FIX *)
CalculateSafe[x_, y_] := If[y == 0, $Failed, x / y]
```

### 2. Infinite Loops
```mathematica
(* BUG *)
Process[] := While[True, DoSomething[]]  (* Never exits *)

(* FIX *)
Process[] := Module[{iter = 0, maxIter = 1000},
  While[iter < maxIter && !Done[], iter++; DoSomething[]]
]
```

### 3. Uninitialized Variables
```mathematica
(* BUG *)
Sum[i, {i, n}]  (* If n undefined, error *)

(* FIX *)
If[!NumericQ[n], Return[$Failed]]; Sum[i, {i, n}]
```

### 4. Array Index Out of Bounds
```mathematica
(* BUG *)
GetElement[list_, i_] := list[[i]]  (* Crashes if i > Length[list] *)

(* FIX *)
GetElement[list_, i_] := If[1 <= i <= Length[list], list[[i]], $Failed]
```

### 5. Null Pointer ($Failed, Missing[])
```mathematica
(* BUG *)
Process[data_] := Module[{result},
  result = ComputeValue[data];  (* May return $Failed *)
  result + 1  (* Fails *)
]

(* FIX *)
Process[data_] := Module[{result},
  result = ComputeValue[data];
  If[FailureQ[result], Return[$Failed]];
  result + 1
]
```

### 6. Resource Leaks (Files Not Closed)
```mathematica
(* BUG *)
ReadData[] := Module[{stream, data},
  stream = OpenRead["data.txt"];
  data = ReadList[stream];
  (* Forgot to close! *)
  data
]

(* FIX *)
ReadData[] := Module[{stream, data},
  stream = OpenRead["data.txt"];
  data = ReadList[stream];
  Close[stream];
  data
]

(* BEST *)
ReadData[] := Import["data.txt", "Table"]
```

### 7. Type Mismatches
```mathematica
(* BUG *)
Add[x_, y_] := x + y
Add["hello", 5]  (* String + Number error *)

(* FIX *)
Add[x_?NumericQ, y_?NumericQ] := x + y
Add[___] := $Failed
```

###8. Undefined Symbols
```mathematica
(* BUG *)
Calculate[] := unknownFunction[data]  (* unknownFunction undefined *)

(* FIX *)
If[!ValueQ[unknownFunction], Return[$Failed]];
Calculate[] := unknownFunction[data]
```

### 9. Pattern Matching Errors
```mathematica
(* BUG *)
f[x__] := x  (* Matches 1+ args, returns Sequence *)

(* FIX *)
f[x__] := {x}  (* Wrap in List *)
```

### 10. Recursion Without Base Case
```mathematica
(* BUG *)
Factorial[n_] := n * Factorial[n - 1]  (* Infinite recursion! *)

(* FIX *)
Factorial[0] = 1;
Factorial[n_?Positive] := n * Factorial[n - 1]
```

---

## Bug Categories (162 total rules)

### Initialization & Variables (25 rules)
- Uninitialized variable use
- Use before definition
- Variable shadowing bugs
- Undefined symbol references
- Missing Module/Block scoping

### Null Safety (18 rules)
- $Failed propagation
- Missing[] handling
- Null checks
- Failed function calls
- Error propagation

### Resource Management (15 rules)
- File handles not closed
- Stream leaks
- Database connections
- Memory leaks
- Temporary file cleanup

### Data Flow (20 rules)
- Uninitialized reads
- Dead stores
- Use-after-free equivalent
- Double evaluation
- Side effects in pure functions

### Control Flow (22 rules)
- Unreachable code
- Dead branches
- Missing return statements
- Infinite loops
- Recursion depth

### Type Safety (18 rules)
- Type mismatches
- Pattern type errors
- List/Association confusion
- String/Symbol confusion
- Numeric precision

### Array & Lists (15 rules)
- Index out of bounds
- Empty list operations
- Part[] errors
- List length mismatches
- Ragged arrays

### Function Calls (12 rules)
- Wrong argument count
- Incorrect argument types
- Missing required parameters
- Apply/Map errors
- Hold attribute violations

### Pattern Matching (10 rules)
- Ambiguous patterns
- Overlapping definitions
- Pattern test failures
- Blank vs BlankSequence
- Optional parameter bugs

### Symbol Table (7 rules)
- Circular dependencies
- Undefined functions
- Missing package imports
- Context resolution

---

## Detection Methods

### AST-Based Detection
Analyzes syntax tree structure to find:
- Missing base cases
- Unreachable code
- Pattern errors

### Data Flow Analysis
Tracks values through code to find:
- Uninitialized reads
- Null propagation
- Resource leaks

### Control Flow Analysis
Builds execution graph to find:
- Infinite loops
- Dead code
- Missing returns

### Type Inference
Infers types to find:
- Type mismatches
- Invalid operations
- Precision loss

---

## Fixing Strategy

1. **Review all CRITICAL and HIGH bugs first**
2. **Test fixes thoroughly** - bugs indicate edge cases
3. **Add defensive checks** - validate inputs
4. **Use Quick Fixes** when available
5. **Add tests** to prevent regression

See [[Rule Catalog]] for all 162 bug rules.
