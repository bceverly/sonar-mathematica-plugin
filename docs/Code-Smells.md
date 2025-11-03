# Code Smells

This page documents the **247 Code Smell rules** that detect maintainability issues. Code smells don't cause immediate bugs, but make code harder to understand, modify, and maintain.

**See also:** [[Bug Detection]] | [[Best Practices]] | [[Rule Catalog]]

---

## Top 30 Code Smells (with examples and impact)

### Performance Smells (High Impact)

#### 1. AppendTo in Loop (1000× SLOWER!)
```mathematica
(* BAD: O(n²) performance *)
result = {};
Do[result = AppendTo[result, i^2], {i, 10000}]
(* Time: 5.2 seconds *)

(* GOOD: O(n) performance *)
result = Table[i^2, {i, 10000}]
(* Time: 0.005 seconds - 1000× faster! *)
```

#### 2. String Concatenation in Loop (100× slower)
```mathematica
(* BAD *)
str = "";
Do[str = str <> ToString[i], {i, 1000}]

(* GOOD *)
str = StringJoin[Table[ToString[i], {i, 1000}]]
```

#### 3. Nested Map/Table
```mathematica
(* BAD: Inefficient *)
Map[Function[x, Map[Function[y, x*y], list2]], list1]

(* GOOD: Use Outer or direct operation *)
Outer[Times, list1, list2]
```

### Complexity Smells

#### 4. Too Many Parameters (> 5)
```mathematica
(* BAD: Hard to remember order *)
ProcessData[input, output, format, encoding, compression, validation, logging]

(* GOOD: Use Association *)
ProcessData[opts_Association] := Module[{...},
  input = opts["Input"];
  output = opts["Output"];
  ...
]
```

#### 5. Deep Nesting (> 4 levels)
```mathematica
(* BAD *)
If[cond1,
  If[cond2,
    If[cond3,
      If[cond4,
        If[cond5,
          DoSomething[]
        ]
      ]
    ]
  ]
]

(* GOOD: Early returns *)
ProcessWithChecks[] := Module[{},
  If[!cond1, Return[$Failed]];
  If[!cond2, Return[$Failed]];
  If[!cond3, Return[$Failed]];
  If[!cond4, Return[$Failed]];
  If[!cond5, Return[$Failed]];
  DoSomething[]
]
```

#### 6. Long Functions (> 150 lines)
Break into smaller, focused functions

### Clarity Smells

#### 7. Magic Numbers
```mathematica
(* BAD *)
price = amount * 1.0825

(* GOOD *)
salesTaxRate = 0.0825;
price = amount * (1 + salesTaxRate)
```

#### 8. Generic Variable Names
```mathematica
(* BAD *)
Process[data_, temp_, x_, result_] := ...

(* GOOD *)
ProcessSalesData[transactions_, taxRate_, discount_, summary_] := ...
```

#### 9. Commented-Out Code
```mathematica
(* BAD *)
(* oldResult = OldCalculation[data]; *)
(* processedData = Transform[oldResult]; *)
newResult = NewCalculation[data];

(* GOOD: Delete it, use version control *)
newResult = NewCalculation[data];
```

###Maintainability Smells

#### 10. No Error Handling
```mathematica
(* BAD *)
data = Import[file];
process = Transform[data];

(* GOOD *)
data = Quiet[Check[Import[file], $Failed]];
If[FailureQ[data], Return[$Failed, "Import failed"]];
process = Transform[data];
```

---

## All 247 Code Smell Categories

### Performance Issues (35 rules)
- AppendTo in loops → Use Table/Join
- String concatenation in loops → Use StringJoin
- Repeated computation → Use memoization
- Unpacking packed arrays → Preserve packing
- Inefficient pattern matching → Optimize patterns
- Nested loops → Use vectorization
- Unnecessary copying → Use in-place operations
- No compilation opportunities → Use Compile[]
- Slow list operations → Use associations
- Repeated file I/O → Cache results

### Complexity Issues (40 rules)
- Functions too long (> 150 lines)
- Too many parameters (> 5)
- Deep nesting (> 4 levels)
- High cyclomatic complexity (> 15)
- High cognitive complexity (> 15)
- Too many local variables (> 15)
- Duplicated code blocks
- Complex boolean expressions
- Switch with too many cases
- Nested ternary operators

### Readability Issues (45 rules)
- Magic numbers
- Generic variable names (temp, data, x)
- Single-letter names (except counters)
- Commented-out code
- Empty blocks
- Redundant code
- Inconsistent naming
- Missing comments for complex logic
- Unclear function purpose
- Boolean variable names

### Pattern & Function Design (30 rules)
- Overly complex patterns
- Ambiguous pattern matching
- Too many function definitions
- Inconsistent function overloads
- Mixed pattern types
- Unused pattern variables
- Pattern performance issues
- Hold attribute misuse
- Attribute conflicts
- Pure function complexity

### Documentation Issues (20 rules)
- Missing function documentation
- Missing usage messages
- Incomplete parameter docs
- No examples
- Outdated documentation
- Missing return value docs
- No error documentation
- Poor API documentation
- Missing package metadata
- No version information

### Code Organization (25 rules)
- God functions (do too much)
- Feature envy (use other module's data)
- Data clumps (repeated parameter groups)
- Long parameter lists
- Primitive obsession (not using custom types)
- Inappropriate intimacy (tight coupling)
- Middle man (unnecessary delegation)
- Dead code
- Speculative generality
- Refused bequest (unused inheritance)

### Best Practices Violations (25 rules)
- Global variable modification
- Side effects in pure functions
- Improper scoping (Module vs Block vs With)
- Missing input validation
- No null checks
- Hardcoded paths
- Platform-specific code
- Deprecated function usage
- Obsolete syntax
- Non-idiomatic code

### Testing Issues (12 rules)
- No test coverage
- Tests not independent
- Magic values in tests
- No negative tests
- Missing edge case tests
- Flaky tests
- Slow tests
- Duplicate test logic
- Poor test names
- No test documentation

### Naming Conventions (15 rules)
- Functions should start with capital
- Private functions with lowercase
- Constants in ALLCAPS
- No Hungarian notation
- Consistent abbreviations
- Meaningful names
- No misleading names
- Boolean names (is/has/can)
- Plural for collections
- Verb-noun for functions

---

## Impact Levels

### HIGH Impact (60 rules)
Performance issues that cause 10×+ slowdowns
- AppendTo in loops (1000× slower!)
- String concatenation
- Nested Map/Table
- No memoization
- Unpacking arrays

### MEDIUM Impact (120 rules)
Maintainability issues
- Code complexity
- Poor readability
- Missing documentation
- Code organization

### LOW Impact (67 rules)
Style preferences
- Naming conventions
- Code formatting
- Comment style
- Minor optimizations

---

## Fixing Priority

1. **HIGH Performance Smells** - Fix immediately (huge speedups)
2. **Complexity Issues** - Refactor when modifying
3. **Readability Issues** - Clean up in batches
4. **Documentation** - Add when touching code
5. **Naming/Style** - Fix opportunistically

---

## Performance Metrics

| Issue | Bad Performance | Good Performance | Speedup |
|-------|----------------|------------------|---------|
| AppendTo in loop | O(n²) 5.2s | O(n) 0.005s | **1000×** |
| String concat loop | 2.1s | 0.021s | **100×** |
| Nested Map | 0.8s | 0.08s (Outer) | **10×** |
| No memoization | 12.5s | 0.001s | **12500×** |
| Pattern backtracking | 45s | 0.5s | **90×** |
| Unpacking arrays | 1.5s | 0.05s | **30×** |
| No compilation | 8.2s | 0.15s (Compile) | **55×** |

---

See [[Rule Catalog]] and [[Best Practices]] for all 247 code smell rules and how to fix them.
