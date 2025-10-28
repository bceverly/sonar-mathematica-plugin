# SonarQube Mathematica Plugin - Complete Roadmap to 450 Rules

**Current Status**: 124 rules (Tier 3)
**Target**: 450 rules (Tier 2 - Scala level)
**Gap**: 325 additional rules/features
**Date**: 2025-10-28

---

## 📊 Implementation Strategy

This roadmap is organized into **7 chunks of ~50 items each**, ordered by dependency:

| Chunk | Items | Technology Foundation | Duration |
|-------|-------|----------------------|----------|
| **Chunk 1** | 1-50 | Enhanced AST Parser | 3-4 months |
| **Chunk 2** | 51-100 | Symbol Table & Scope Analysis | 3-4 months |
| **Chunk 3** | 101-150 | Type Inference & Basic Data Flow | 3-4 months |
| **Chunk 4** | 151-200 | Control Flow & Taint Analysis | 3-4 months |
| **Chunk 5** | 201-250 | Cross-File & Architecture Analysis | 3-4 months |
| **Chunk 6** | 251-300 | Advanced Semantic Analysis | 3-4 months |
| **Chunk 7** | 301-325 | Coverage, Performance & Polish | 2-3 months |

**Total Timeline**: 20-27 months (part-time) to reach Scala parity

**Key Principle**: Each chunk builds on the previous one. Don't skip ahead - the technology foundation must be in place first.

---

## 🏗️ CHUNK 1: Enhanced AST Parser Foundation (Items 1-50)

**Technology Milestone**: Complete AST parser covering all Mathematica language constructs

**Why First**: Everything depends on having a full AST. Without it, we're limited to regex patterns (current state).

**Duration**: 3-4 months

### Core AST Infrastructure (Items 1-15)

#### 1. ✅ Module/Block/With Scoping Constructs
**Type**: Infrastructure
**What**: Parse `Module[{vars}, body]`, `Block[{vars}, body]`, `With[{vars}, body]`
**Why**: Foundation for scope-aware analysis (80% of code uses these)
**Enables**: Variable scoping rules, shadowing detection
**Effort**: 3 days

#### 2. ✅ If/Which/Switch Control Flow
**Type**: Infrastructure
**What**: Parse conditional statements with proper nesting
**Why**: Control flow analysis foundation
**Enables**: Dead branch detection, complexity metrics
**Effort**: 2 days

#### 3. ✅ Loop Constructs (Do/While/For/Table/NestWhile)
**Type**: Infrastructure
**What**: Parse all iteration constructs
**Why**: Loop analysis, performance rules
**Enables**: Infinite loop detection, loop optimization rules
**Effort**: 3 days

#### 4. ✅ Pattern Matching Expressions
**Type**: Infrastructure
**What**: Parse `_`, `__`, `___`, `x_Integer`, `PatternTest`, `Condition`
**Why**: Core Mathematica feature, many bugs involve patterns
**Enables**: Pattern-specific rules (20+ rules)
**Effort**: 5 days

#### 5. ✅ List Literals and Nested Structures
**Type**: Infrastructure
**What**: Parse `{1, 2, 3}`, `{{1, 2}, {3, 4}}`, arbitrary nesting
**Why**: Primary data structure
**Enables**: List manipulation rules
**Effort**: 2 days

#### 6. ✅ Association Literals
**Type**: Infrastructure
**What**: Parse `<|"a" -> 1, "b" -> 2|>`, nested associations
**Why**: Modern Mathematica dictionary type
**Enables**: Association-specific rules
**Effort**: 2 days

#### 7. ✅ Binary Operators with Precedence
**Type**: Infrastructure
**What**: Parse `+, -, *, /, ^, &&, ||, ==, !=, <, >, <=, >=` with correct precedence
**Why**: Expression evaluation order
**Enables**: Expression analysis rules
**Effort**: 3 days

#### 8. ✅ Unary Operators
**Type**: Infrastructure
**What**: Parse `!, -, +, Not[]`
**Why**: Complete expression coverage
**Enables**: Expression simplification rules
**Effort**: 1 day

#### 9. ✅ Special Operators (Map, Apply, etc.)
**Type**: Infrastructure
**What**: Parse `/@, @@, @@@, /., //., /;, @@, ~f~, #, &`
**Why**: Mathematica-specific functional operators
**Enables**: Functional programming rules
**Effort**: 4 days

#### 10. ✅ Anonymous Functions (Pure Functions)
**Type**: Infrastructure
**What**: Parse `#1 + #2 &`, `Function[{x, y}, x + y]`, slot notation
**Why**: Very common in modern Mathematica
**Enables**: Closure and lambda rules
**Effort**: 3 days

#### 11. ✅ Function Definitions (All Forms)
**Type**: Infrastructure
**What**: Parse `f[x_] := body`, `f[x_] = body`, `f = Function[...]`
**Why**: Primary code organization
**Enables**: Function-level analysis
**Effort**: 2 days

#### 12. ✅ String Expressions and Templates
**Type**: Infrastructure
**What**: Parse string literals, `StringTemplate`, `StringExpression`
**Why**: String manipulation and security
**Enables**: Injection detection rules
**Effort**: 2 days

#### 13. ✅ Compound Expressions
**Type**: Infrastructure
**What**: Parse `expr1; expr2; expr3`, statement sequences
**Why**: Multi-statement blocks
**Enables**: Dead code detection
**Effort**: 1 day

#### 14. ✅ Part Specification
**Type**: Infrastructure
**What**: Parse `list[[i]]`, `list[[i, j]]`, `list[[-1]]`, `list[[All]]`
**Why**: Array indexing
**Enables**: Index bounds checking
**Effort**: 2 days

#### 15. ✅ Span Expressions
**Type**: Infrastructure
**What**: Parse `1;;10`, `;;-1`, `1;;10;;2`
**Why**: Array slicing
**Enables**: Slice validation rules
**Effort**: 2 days

### Pattern System Rules (Items 16-30)

#### 16. ❌ Unrestricted Blank Pattern Warning
**Type**: Code Smell
**What**: Detect `f[x_] := ...` without type restriction when type checking would help
**Why**: Runtime errors from wrong types
**Example**: `f[x_] := x^2` should be `f[x_?NumericQ] := x^2`
**Effort**: 1 day

#### 17. ❌ Pattern Test vs Condition Confusion
**Type**: Bug
**What**: Detect incorrect use of `?` vs `/;`
**Why**: Different evaluation semantics
**Example**: `f[x_?IntegerQ] := x` (correct) vs `f[x_ /; IntegerQ[x]] := x` (works but inefficient)
**Effort**: 1 day

#### 18. ❌ BlankSequence Without Restriction
**Type**: Performance
**What**: Detect `f[x__] := ...` that could be `f[x__Integer] := ...`
**Why**: Performance and correctness
**Effort**: 1 day

#### 19. ❌ Nested Optional Patterns
**Type**: Bug
**What**: Detect `f[x_:1, y_:x] := ...` (y default depends on x)
**Why**: Evaluation order issues
**Effort**: 1 day

#### 20. ❌ Pattern Naming Conflicts
**Type**: Bug
**What**: Detect same pattern name used with different restrictions
**Example**: `f[x_Integer, x_Real] := ...` (impossible to match)
**Effort**: 1 day

#### 21. ❌ Repeated Pattern Alternatives
**Type**: Code Smell
**What**: Detect `f[x_Integer | x_Real] := ...` (redundant pattern name)
**Why**: Should be `f[x:(_Integer | _Real)] := ...`
**Effort**: 1 day

#### 22. ❌ Pattern Test With Pure Function
**Type**: Performance
**What**: Detect `x_?(# > 0 &)` in hot code
**Why**: Creates closure on each match
**Effort**: 1 day

#### 23. ❌ Missing Pattern Defaults
**Type**: Bug
**What**: Detect optional arguments without sensible defaults
**Example**: `f[x_, opts___] := ...` (should validate opts)
**Effort**: 1 day

#### 24. ❌ Order-Dependent Pattern Definitions
**Type**: Bug
**What**: Detect when specific patterns defined after general ones
**Example**: `f[x_] := ...; f[0] := ...` (second never matches)
**Effort**: 2 days

#### 25. ❌ Verbatim Pattern Misuse
**Type**: Bug
**What**: Detect incorrect use of `Verbatim`
**Why**: Tricky semantics
**Effort**: 1 day

#### 26. ❌ HoldPattern Unnecessary
**Type**: Code Smell
**What**: Detect `HoldPattern` where not needed
**Why**: Clutter
**Effort**: 1 day

#### 27. ❌ Longest/Shortest Without Ordering
**Type**: Bug
**What**: Detect `Longest[x__]` without proper context
**Why**: May not work as expected
**Effort**: 1 day

#### 28. ❌ Pattern Repeated With Different Types
**Type**: Bug
**What**: Detect `f[{x_, x_}] := ...` expecting same value twice
**Why**: Should use `f[{x_, y_} /; x == y] := ...`
**Effort**: 1 day

#### 29. ❌ Alternatives Too Complex
**Type**: Performance
**What**: Detect `x:(a|b|c|d|e|f|g|h|...)` with many alternatives
**Why**: Backtracking explosion
**Effort**: 1 day

#### 30. ❌ Pattern Matching On Large Lists
**Type**: Performance
**What**: Detect pattern matching on lists with thousands of elements
**Why**: Use other approaches (Select, Pick)
**Effort**: 2 days

### List/Array Rules (Items 31-40)

#### 31. ❌ Empty List Indexing
**Type**: Bug
**What**: Detect `list[[1]]` without checking `Length[list] > 0`
**Why**: Runtime error
**Effort**: 1 day

#### 32. ❌ Negative Index Without Validation
**Type**: Bug
**What**: Detect `list[[-n]]` without checking `n <= Length[list]`
**Why**: Runtime error
**Effort**: 1 day

#### 33. ❌ Part Assignment To Immutable
**Type**: Bug
**What**: Detect `list[[i]] = x` where list isn't a variable
**Why**: Doesn't modify original
**Effort**: 1 day

#### 34. ❌ Inefficient List Concatenation
**Type**: Performance
**What**: Detect `Join[list, {x}]` in loop
**Why**: Quadratic complexity
**Example**: Use `AppendTo` or pre-allocate
**Effort**: 1 day

#### 35. ❌ Unnecessary Flatten
**Type**: Performance
**What**: Detect `Flatten[{a, b, c}]` where already flat
**Why**: Wasted computation
**Effort**: 1 day

#### 36. ❌ Length In Loop Condition
**Type**: Performance
**What**: Detect `Do[..., {i, 1, Length[list]}]` recalculating length
**Why**: Cache length value
**Effort**: 1 day

#### 37. ❌ Reverse Twice
**Type**: Code Smell
**What**: Detect `Reverse[Reverse[x]]`
**Why**: No-op
**Effort**: 1 day

#### 38. ❌ Sort Without Comparison
**Type**: Performance
**What**: Detect `Sort[list, Greater]` when could use `Reverse[Sort[list]]`
**Why**: Built-in sort is optimized
**Effort**: 1 day

#### 39. ❌ Position vs Select
**Type**: Performance
**What**: Detect `Extract[list, Position[list, pattern]]` → use `Select`
**Why**: Cleaner and faster
**Effort**: 1 day

#### 40. ❌ Nested Part Extraction
**Type**: Code Smell
**What**: Detect `list[[i]][[j]]` → use `list[[i, j]]`
**Why**: Cleaner syntax
**Effort**: 1 day

### Association Rules (Items 41-50)

#### 41. ❌ Missing Key Check
**Type**: Bug
**What**: Detect `assoc["key"]` without checking `KeyExistsQ`
**Why**: Returns `Missing["KeyAbsent", "key"]`
**Effort**: 1 day

#### 42. ❌ Association vs List Confusion
**Type**: Bug
**What**: Detect list operations on associations
**Example**: `Length[assoc]` (correct) vs `assoc[[1]]` (wrong)
**Effort**: 1 day

#### 43. ❌ Inefficient Key Lookup
**Type**: Performance
**What**: Detect `Select[Keys[assoc], pred]` → use `KeySelect`
**Why**: Built-in is optimized
**Effort**: 1 day

#### 44. ❌ Query On Non-Dataset
**Type**: Bug
**What**: Detect `Query[...]` on regular list
**Why**: Needs Dataset wrapper
**Effort**: 1 day

#### 45. ❌ Association Update Pattern
**Type**: Code Smell
**What**: Detect `assoc["key"] = value` → use `AssociateTo` or `Append`
**Why**: Cleaner semantics
**Effort**: 1 day

#### 46. ❌ Merge Without Conflict Strategy
**Type**: Bug
**What**: Detect `Merge[{a1, a2}]` without specifying merge function
**Why**: Default may not be what you want
**Effort**: 1 day

#### 47. ❌ AssociateTo On Non-Symbol
**Type**: Bug
**What**: Detect `AssociateTo[expr, ...]` where expr isn't a symbol
**Why**: Won't modify original
**Effort**: 1 day

#### 48. ❌ KeyDrop Multiple Times
**Type**: Performance
**What**: Detect `KeyDrop[KeyDrop[assoc, "a"], "b"]` → `KeyDrop[assoc, {"a", "b"}]`
**Why**: Single pass
**Effort**: 1 day

#### 49. ❌ Lookup With Missing Default
**Type**: Code Smell
**What**: Detect `Lookup[assoc, key, Missing[]]` (redundant)
**Why**: Missing is default
**Effort**: 1 day

#### 50. ❌ Group By Without Aggregation
**Type**: Code Smell
**What**: Detect `GroupBy` where `GatherBy` more appropriate
**Why**: Semantic clarity
**Effort**: 1 day

---

## 🧠 CHUNK 2: Symbol Table & Scope Analysis (Items 51-100)

**Technology Milestone**: Complete symbol table with scope tracking and definition/usage tracking

**What Unlocks**: Unused code detection, shadowing detection, undefined symbol detection, cross-reference analysis

**Duration**: 3-4 months

### Symbol Table Infrastructure (Items 51-60)

#### 51. ✅ Build Symbol Table (Single File)
**Type**: Infrastructure
**What**: Track all function/variable definitions and their scopes in one file
**Why**: Foundation for all symbol-based analysis
**Enables**: 50+ rules
**Effort**: 4 days

#### 52. ✅ Scope Tree Construction
**Type**: Infrastructure
**What**: Build tree of nested scopes (Module inside Module, etc.)
**Why**: Accurate variable resolution
**Enables**: Shadowing detection
**Effort**: 3 days

#### 53. ✅ Definition Location Tracking
**Type**: Infrastructure
**What**: Track line/column of every definition
**Why**: Precise error reporting
**Enables**: Better UX
**Effort**: 2 days

#### 54. ✅ Usage Location Tracking
**Type**: Infrastructure
**What**: Track every symbol reference
**Why**: Unused detection, cross-reference
**Enables**: Navigation features
**Effort**: 2 days

#### 55. ✅ Function Signature Registry
**Type**: Infrastructure
**What**: Track function name, parameter count, parameter types (if specified)
**Why**: Detect wrong argument counts
**Enables**: API misuse detection
**Effort**: 3 days

#### 56. ✅ Variable Mutability Tracking
**Type**: Infrastructure
**What**: Track which variables are modified vs read-only
**Why**: Detect unintended mutations
**Enables**: Mutation analysis rules
**Effort**: 2 days

#### 57. ✅ Context/Package Tracking
**Type**: Infrastructure
**What**: Track `Begin[]`/`End[]` context boundaries
**Why**: Package-level analysis
**Enables**: Export/import rules
**Effort**: 3 days

#### 58. ✅ Symbol Shadowing Detection
**Type**: Infrastructure
**What**: Detect when inner scope shadows outer scope
**Why**: Foundation for shadowing rules
**Enables**: 10+ rules
**Effort**: 2 days

#### 59. ✅ Built-in Symbol Registry
**Type**: Infrastructure
**What**: Database of ~6000 Mathematica built-ins with signatures
**Why**: Detect typos, deprecated functions
**Enables**: Built-in misuse rules
**Effort**: 5 days (data entry)

#### 60. ✅ Attribute Tracking
**Type**: Infrastructure
**What**: Track `SetAttributes[f, {Listable, Protected, ...}]`
**Why**: Function behavior analysis
**Enables**: Attribute-based rules
**Effort**: 2 days

### Unused Code Detection (Items 61-75)

#### 61. ❌ Unused Function (Private)
**Type**: Code Smell
**What**: Detect private functions never called
**Why**: Dead code
**Severity**: Minor
**Effort**: 1 day

#### 62. ❌ Unused Function Parameter
**Type**: Code Smell
**What**: Detect function parameters never used in body (enhanced from Chunk 1)
**Why**: May indicate logic error
**Severity**: Minor
**Effort**: 1 day

#### 63. ❌ Unused Module Variable
**Type**: Code Smell
**What**: Detect `Module[{x, y}, ...]` where y never used
**Why**: Clutter
**Severity**: Minor
**Effort**: 1 day

#### 64. ❌ Unused With Variable
**Type**: Code Smell
**What**: Similar to Module but for With
**Why**: With variables are constants, unused is clearer mistake
**Severity**: Minor
**Effort**: 1 day

#### 65. ❌ Unused Import
**Type**: Code Smell
**What**: Detect `Needs["Package`"]` where nothing from package is used
**Why**: Load time, dependencies
**Severity**: Minor
**Effort**: 1 day

#### 66. ❌ Unused Pattern Name
**Type**: Code Smell
**What**: Detect `f[x_, y_] := x` (y not used)
**Why**: Should be `f[x_, _] := x`
**Severity**: Minor
**Effort**: 1 day

#### 67. ❌ Unused Optional Parameter
**Type**: Code Smell
**What**: Detect optional param never used even when provided
**Why**: Confusing API
**Severity**: Minor
**Effort**: 1 day

#### 68. ❌ Dead After Return
**Type**: Bug
**What**: Detect code after `Return[]` in same scope
**Why**: Never executes
**Severity**: Major
**Effort**: 1 day

#### 69. ❌ Unreachable After Abort/Throw
**Type**: Bug
**What**: Detect code after `Abort[]` or `Throw[]`
**Why**: Never executes
**Severity**: Major
**Effort**: 1 day

#### 70. ❌ Assignment Never Read
**Type**: Code Smell
**What**: Detect `x = value` where x never read before next assignment
**Why**: Useless work
**Severity**: Minor
**Effort**: 2 days

#### 71. ❌ Function Defined But Never Called
**Type**: Code Smell
**What**: Global-scope function never called
**Why**: Dead code (may be API)
**Severity**: Info
**Effort**: 1 day

#### 72. ❌ Redefined Without Use
**Type**: Bug
**What**: Detect `x = 1; x = 2` where first value never used
**Why**: Logic error
**Severity**: Major
**Effort**: 1 day

#### 73. ❌ Loop Variable Unused
**Type**: Code Smell
**What**: Detect `Do[..., {i, 1, 10}]` where i never used in body
**Why**: Use `Do[..., 10]` form
**Severity**: Minor
**Effort**: 1 day

#### 74. ❌ Catch Without Throw
**Type**: Code Smell
**What**: Detect `Catch[...]` where no `Throw` in body
**Why**: Unnecessary
**Severity**: Minor
**Effort**: 1 day

#### 75. ❌ Condition Always False
**Type**: Bug
**What**: Detect `If[False, ...]` or pattern `/; False`
**Why**: Dead code or logic error
**Severity**: Major
**Effort**: 1 day

### Shadowing & Naming (Items 76-90)

#### 76. ❌ Local Shadows Global
**Type**: Code Smell
**What**: Detect `Module[{x}, ...]` when x is global variable
**Why**: Confusing, may be unintended
**Severity**: Minor
**Effort**: 1 day

#### 77. ❌ Parameter Shadows Built-in
**Type**: Bug
**What**: Detect `f[List_] := ...` (shadows built-in)
**Why**: Won't work as expected
**Severity**: Major
**Effort**: 1 day

#### 78. ❌ Local Variable Shadows Parameter
**Type**: Bug
**What**: Detect `f[x_] := Module[{x}, ...]`
**Why**: Confusing, probably error
**Severity**: Major
**Effort**: 1 day

#### 79. ❌ Multiple Definitions Same Symbol
**Type**: Code Smell
**What**: Detect function redefined multiple times
**Why**: May be intentional (patterns) or error
**Severity**: Info
**Effort**: 1 day

#### 80. ❌ Symbol Name Too Short
**Type**: Code Smell
**What**: Detect single-letter variable names in large functions
**Why**: Readability
**Severity**: Info
**Effort**: 1 day

#### 81. ❌ Symbol Name Too Long
**Type**: Code Smell
**What**: Detect variables longer than 50 characters
**Why**: Readability
**Severity**: Info
**Effort**: 1 day

#### 82. ❌ Inconsistent Naming Convention
**Type**: Code Smell
**What**: Detect mix of camelCase, snake_case, PascalCase
**Why**: Consistency
**Severity**: Info
**Effort**: 2 days

#### 83. ❌ Built-in Name In Local Scope
**Type**: Code Smell
**What**: Detect `Module[{Map, Apply}, ...]` (shadowing built-ins)
**Why**: Confusing
**Severity**: Minor
**Effort**: 1 day

#### 84. ❌ Context Conflicts
**Type**: Bug
**What**: Detect symbol defined in multiple contexts
**Why**: Ambiguity
**Severity**: Major
**Effort**: 2 days

#### 85. ❌ Reserved Name Usage
**Type**: Bug
**What**: Detect use of $SystemID, $Version, etc. as variable names
**Why**: Can cause issues
**Severity**: Major
**Effort**: 1 day

#### 86. ❌ Private Context Symbol Public
**Type**: Code Smell
**What**: Detect symbols in `Private` context used outside package
**Why**: Breaks encapsulation
**Severity**: Minor
**Effort**: 2 days

#### 87. ❌ Mismatched Begin/End
**Type**: Bug
**What**: Detect `BeginPackage` without matching `EndPackage`
**Why**: Context corruption
**Severity**: Critical
**Effort**: 1 day

#### 88. ❌ Symbol After EndPackage
**Type**: Bug
**What**: Detect definitions after `EndPackage[]`
**Why**: Wrong context
**Severity**: Major
**Effort**: 1 day

#### 89. ❌ Global in Package
**Type**: Code Smell
**What**: Detect use of `Global` context in package code
**Why**: Should use package context
**Severity**: Minor
**Effort**: 1 day

#### 90. ❌ Temp Variable Not Temp
**Type**: Code Smell
**What**: Detect variables named `temp`, `tmp` used multiple times
**Why**: Should have better names
**Severity**: Info
**Effort**: 1 day

### Undefined Symbol Detection (Items 91-100)

#### 91. ❌ Undefined Function Call
**Type**: Bug
**What**: Detect call to function not defined or imported
**Why**: Runtime error
**Severity**: Critical
**Effort**: 2 days

#### 92. ❌ Undefined Variable Reference
**Type**: Bug
**What**: Detect use of variable before definition
**Why**: Runtime error (or typo)
**Severity**: Critical
**Effort**: 2 days

#### 93. ❌ Typo In Built-in Name
**Type**: Bug
**What**: Detect `Lenght` instead of `Length` (Levenshtein distance)
**Why**: Common typo
**Severity**: Critical
**Effort**: 2 days

#### 94. ❌ Wrong Capitalization
**Type**: Bug
**What**: Detect `length` when meant `Length`
**Why**: Mathematica is case-sensitive
**Severity**: Major
**Effort**: 1 day

#### 95. ❌ Missing Import
**Type**: Bug
**What**: Detect use of package symbol without `Needs[]`
**Why**: Works in notebook (might be loaded) but fails in script
**Severity**: Major
**Effort**: 2 days

#### 96. ❌ Context Not Found
**Type**: Bug
**What**: Detect `Needs["NonExistent`"]`
**Why**: Runtime error
**Severity**: Critical
**Effort**: 2 days

#### 97. ❌ Symbol Masked By Import
**Type**: Bug
**What**: Detect local symbol masked when package imported
**Why**: Silent behavior change
**Severity**: Major
**Effort**: 2 days

#### 98. ❌ Missing $Path Entry
**Type**: Bug
**What**: Detect `Get["file.m"]` where file not in $Path
**Why**: Runtime error
**Severity**: Major
**Effort**: 2 days

#### 99. ❌ Circular Needs
**Type**: Bug
**What**: Detect A.m needs B.m needs A.m
**Why**: Load error
**Severity**: Critical
**Effort**: 2 days

#### 100. ❌ Forward Reference Without Declaration
**Type**: Bug
**What**: Detect use before definition without explicit forward declaration
**Why**: May fail in fresh kernel
**Severity**: Major
**Effort**: 2 days

---

## 🔍 CHUNK 3: Type Inference & Basic Data Flow (Items 101-150)

**Technology Milestone**: Type inference system tracking types through expressions + basic data flow within functions

**What Unlocks**: Type mismatch detection, null safety, better security analysis, numeric precision rules

**Duration**: 3-4 months

### Type System Infrastructure (Items 101-110)

#### 101. ✅ Type Lattice Definition
**Type**: Infrastructure
**What**: Define type hierarchy: Integer ⊂ Rational ⊂ Real ⊂ Complex ⊂ Number
**Why**: Foundation for type checking
**Enables**: All type rules
**Effort**: 3 days

#### 102. ✅ Pattern Type Extraction
**Type**: Infrastructure
**What**: Extract type constraints from patterns: `f[x_Integer]` → x: Integer
**Why**: Function signatures
**Enables**: Call validation
**Effort**: 2 days

#### 103. ✅ Expression Type Inference
**Type**: Infrastructure
**What**: Infer types: `x + 1` where x: Integer → Integer
**Why**: Track types through computation
**Enables**: Type propagation
**Effort**: 5 days

#### 104. ✅ Built-in Return Types
**Type**: Infrastructure
**What**: Database of return types for built-in functions
**Why**: Type inference across function calls
**Enables**: Better inference
**Effort**: 4 days (data entry)

#### 105. ✅ Union Types
**Type**: Infrastructure
**What**: Support `x: Integer | Real | String`
**Why**: Handle uncertainty
**Enables**: Realistic type tracking
**Effort**: 3 days

#### 106. ✅ Container Types
**Type**: Infrastructure
**What**: Track `List[Integer]`, `Association[String -> Integer]`
**Why**: Structured data types
**Enables**: Deep type checking
**Effort**: 4 days

#### 107. ✅ Null/Missing Type Tracking
**Type**: Infrastructure
**What**: Track `Null`, `Missing[]`, `$Failed` as special types
**Why**: Null safety
**Enables**: Null propagation rules
**Effort**: 2 days

#### 108. ✅ Type Widening
**Type**: Infrastructure
**What**: Handle `If[test, 1, "x"]` → Integer | String
**Why**: Control flow type merging
**Enables**: Branch-aware inference
**Effort**: 3 days

#### 109. ✅ Type Constraint Tracking
**Type**: Infrastructure
**What**: Track `If[IntegerQ[x], ...]` → x: Integer in branch
**Why**: Conditional type refinement
**Enables**: Smart narrowing
**Effort**: 4 days

#### 110. ✅ Function Type Signatures
**Type**: Infrastructure
**What**: Infer and store function type: `f: Integer -> String`
**Why**: Type-check calls
**Enables**: API validation
**Effort**: 3 days

### Type Mismatch Detection (Items 111-130)

#### 111. ❌ Numeric Operation On String
**Type**: Bug
**What**: Detect `"hello" + 1` or `"hello"^2`
**Why**: Runtime error or unexpected result
**Severity**: Major
**Effort**: 1 day

#### 112. ❌ String Operation On Number
**Type**: Bug
**What**: Detect `StringJoin[123, "abc"]` (wrong arg type)
**Why**: Runtime error
**Severity**: Major
**Effort**: 1 day

#### 113. ❌ Wrong Argument Type
**Type**: Bug
**What**: Detect `Map[f, 123]` (expects list)
**Why**: Runtime error
**Severity**: Major
**Effort**: 1 day

#### 114. ❌ Function Returns Wrong Type
**Type**: Bug
**What**: Detect function declared to return Integer but returns String
**Why**: Contract violation
**Severity**: Major
**Effort**: 2 days

#### 115. ❌ Comparison Of Incompatible Types
**Type**: Bug
**What**: Detect `"hello" < 5` (doesn't error but meaningless)
**Why**: Logic error
**Severity**: Minor
**Effort**: 1 day

#### 116. ❌ Mixed Numeric Types In Computation
**Type**: Code Smell
**What**: Detect mix of exact and approximate in same calculation
**Why**: Precision loss
**Severity**: Minor
**Effort**: 1 day

#### 117. ❌ Integer Division Expecting Real
**Type**: Bug
**What**: Detect `1/2` in numeric context (evaluates symbolically)
**Why**: Use `1./2` for numeric
**Severity**: Major
**Effort**: 1 day

#### 118. ❌ List Function On Association
**Type**: Bug
**What**: Detect `Append[assoc, elem]` (should use AssociateTo)
**Why**: Different semantics
**Severity**: Major
**Effort**: 1 day

#### 119. ❌ Pattern Type Mismatch
**Type**: Bug
**What**: Detect `f[x_Integer] := ...; f["hello"]` call
**Why**: Won't match, returns unevaluated
**Severity**: Major
**Effort**: 1 day

#### 120. ❌ Optional Type Inconsistent
**Type**: Bug
**What**: Detect `f[x_Integer:1.5] := ...` (default wrong type)
**Why**: Default should match pattern type
**Severity**: Major
**Effort**: 1 day

#### 121. ❌ Return Type Inconsistent
**Type**: Code Smell
**What**: Detect function that sometimes returns Integer, sometimes String
**Why**: Confusing API
**Severity**: Minor
**Effort**: 2 days

#### 122. ❌ Null Assignment To Typed Variable
**Type**: Bug
**What**: Detect `x = Null` where x expected to be Integer
**Why**: Type violation
**Severity**: Major
**Effort**: 1 day

#### 123. ❌ Type Cast Without Validation
**Type**: Bug
**What**: Detect use of `ToExpression[str]` without checking `StringQ`
**Why**: May fail
**Severity**: Major
**Effort**: 1 day

#### 124. ❌ Implicit Type Conversion
**Type**: Code Smell
**What**: Detect `ToString[x] <> y` where x already String
**Why**: Redundant
**Severity**: Minor
**Effort**: 1 day

#### 125. ❌ Graphics Object In Numeric Context
**Type**: Bug
**What**: Detect `Plot[...] + 1`
**Why**: Doesn't make sense
**Severity**: Major
**Effort**: 1 day

#### 126. ❌ Symbol In Numeric Context
**Type**: Bug
**What**: Detect `x + 1` where x is symbolic and expected numeric
**Why**: Won't evaluate
**Severity**: Minor
**Effort**: 1 day

#### 127. ❌ Image Operation On Non-Image
**Type**: Bug
**What**: Detect `ImageData[list]` (expects Image object)
**Why**: Runtime error
**Severity**: Major
**Effort**: 1 day

#### 128. ❌ Sound Operation On Non-Sound
**Type**: Bug
**What**: Detect `AudioData[list]` (expects Audio object)
**Why**: Runtime error
**Severity**: Major
**Effort**: 1 day

#### 129. ❌ Dataset Operation On List
**Type**: Bug
**What**: Detect Dataset operations on regular lists
**Why**: Needs Dataset wrapper
**Severity**: Major
**Effort**: 1 day

#### 130. ❌ Graph Operation On Non-Graph
**Type**: Bug
**What**: Detect `VertexList[list]` (expects Graph)
**Why**: Runtime error
**Severity**: Major
**Effort**: 1 day

### Data Flow Analysis (Items 131-150)

#### 131. ✅ Reaching Definitions Analysis
**Type**: Infrastructure
**What**: Track which definitions reach each use
**Why**: Foundation for uninitialized detection
**Enables**: 10+ rules
**Effort**: 4 days

#### 132. ✅ Live Variables Analysis
**Type**: Infrastructure
**What**: Track which variables are live at each point
**Why**: Dead store detection
**Enables**: Optimization rules
**Effort**: 3 days

#### 133. ✅ Def-Use Chains
**Type**: Infrastructure
**What**: Track definition-to-use relationships
**Why**: Better unused detection
**Enables**: Enhanced unused rules
**Effort**: 3 days

#### 134. ✅ Use-Def Chains
**Type**: Infrastructure
**What**: Track use-to-definition relationships
**Why**: Uninitialized detection
**Enables**: Better bug detection
**Effort**: 3 days

#### 135. ❌ Uninitialized Variable Use (Enhanced)
**Type**: Bug
**What**: Detect variable used before any definition (data flow based)
**Why**: Runtime error or wrong value
**Severity**: Critical
**Effort**: 2 days

#### 136. ❌ Variable May Be Uninitialized
**Type**: Bug
**What**: Detect variable initialized in some branches but not all
**Why**: Logic error
**Severity**: Major
**Effort**: 2 days

#### 137. ❌ Dead Store
**Type**: Code Smell
**What**: Detect `x = value` where value never read
**Why**: Useless computation
**Severity**: Minor
**Effort**: 2 days

#### 138. ❌ Overwritten Before Read
**Type**: Code Smell
**What**: Detect `x = 1; x = 2` where first value never used
**Why**: First assignment wasted
**Severity**: Minor
**Effort**: 1 day

#### 139. ❌ Variable Aliasing Issue
**Type**: Bug
**What**: Detect when two variables point to same mutable structure
**Why**: Unexpected modifications
**Severity**: Major
**Effort**: 3 days

#### 140. ❌ Modification Of Loop Iterator
**Type**: Bug
**What**: Detect `Do[...; i = i+1; ..., {i, 1, 10}]` (modifying iterator)
**Why**: Confusing, may not work as expected
**Severity**: Major
**Effort**: 1 day

#### 141. ❌ Use Of Modified Iterator Outside Loop
**Type**: Bug
**What**: Detect use of loop iterator after loop ends (value undefined)
**Why**: Iterator value after loop is implementation detail
**Severity**: Minor
**Effort**: 1 day

#### 142. ❌ Reading Unset Variable
**Type**: Bug
**What**: Detect read of variable after `Unset[x]` or `Clear[x]`
**Why**: Returns Symbol, not value
**Severity**: Major
**Effort**: 1 day

#### 143. ❌ Double Assignment Same Value
**Type**: Code Smell
**What**: Detect `x = 5; ...; x = 5` (same value)
**Why**: Redundant
**Severity**: Info
**Effort**: 1 day

#### 144. ❌ Mutation In Pure Function
**Type**: Bug
**What**: Detect mutation of outer variable in pure function
**Why**: Side effect, confusing
**Severity**: Minor
**Effort**: 2 days

#### 145. ❌ Shared Mutable State
**Type**: Bug
**What**: Detect global mutable state accessed from multiple functions
**Why**: Hard to reason about
**Severity**: Minor
**Effort**: 2 days

#### 146. ❌ Variable Scope Escape
**Type**: Bug
**What**: Detect `Module[{x}, x]` returning local variable
**Why**: Variable leaks (as Symbol)
**Severity**: Minor
**Effort**: 1 day

#### 147. ❌ Closure Over Mutable Variable
**Type**: Bug
**What**: Detect pure function capturing variable that changes
**Why**: May not capture expected value
**Severity**: Major
**Effort**: 2 days

#### 148. ❌ Assignment In Condition
**Type**: Bug
**What**: Detect `If[x = 5, ...]` (should be `x == 5`)
**Why**: Always true, side effect
**Severity**: Critical
**Effort**: 1 day

#### 149. ❌ Assignment As Return Value
**Type**: Code Smell
**What**: Detect `f[x_] := (y = x; y)` (unnecessary variable)
**Why**: Could be `f[x_] := x`
**Severity**: Info
**Effort**: 1 day

#### 150. ❌ Variable Never Modified
**Type**: Code Smell
**What**: Detect `Module[{x = 1}, ...; Return[x]]` where x never modified
**Why**: Could use `With` for immutability
**Severity**: Info
**Effort**: 1 day

---

## 🌊 CHUNK 4: Control Flow & Taint Analysis (Items 151-200)

**Technology Milestone**: Control Flow Graph (CFG) construction + Taint tracking for security

**What Unlocks**: Dead code detection, reachability analysis, security vulnerability detection (injection attacks)

**Duration**: 3-4 months

### Control Flow Infrastructure (Items 151-160)

#### 151. ✅ Build Control Flow Graph
**Type**: Infrastructure
**What**: Graph showing all possible execution paths through function
**Why**: Foundation for advanced analysis
**Enables**: 30+ rules
**Effort**: 6 days

#### 152. ✅ Basic Block Identification
**Type**: Infrastructure
**What**: Identify straight-line code sequences
**Why**: CFG nodes
**Enables**: CFG construction
**Effort**: 2 days

#### 153. ✅ Branch Node Analysis
**Type**: Infrastructure
**What**: Analyze If/Which/Switch branch conditions
**Why**: Conditional flow
**Enables**: Branch rules
**Effort**: 3 days

#### 154. ✅ Loop Node Analysis
**Type**: Infrastructure
**What**: Analyze loop entry/exit/back-edges
**Why**: Loop flow
**Enables**: Loop rules
**Effort**: 3 days

#### 155. ✅ Exception Flow Tracking
**Type**: Infrastructure
**What**: Track Throw/Catch/Abort flow
**Why**: Exception paths
**Enables**: Exception rules
**Effort**: 3 days

#### 156. ✅ Return Statement Flow
**Type**: Infrastructure
**What**: Track all return points and paths to them
**Why**: Return analysis
**Enables**: Return rules
**Effort**: 2 days

#### 157. ✅ Dominance Analysis
**Type**: Infrastructure
**What**: Compute which blocks dominate which
**Why**: Required for various optimizations
**Enables**: Advanced rules
**Effort**: 3 days

#### 158. ✅ Post-Dominance Analysis
**Type**: Infrastructure
**What**: Compute post-dominance relationships
**Why**: Convergence points
**Enables**: Advanced rules
**Effort**: 3 days

#### 159. ✅ Loop Detection
**Type**: Infrastructure
**What**: Identify natural loops in CFG
**Why**: Loop-specific analysis
**Enables**: Loop rules
**Effort**: 2 days

#### 160. ✅ Path Feasibility Analysis
**Type**: Infrastructure
**What**: Determine if a path can actually be taken
**Why**: Avoid false positives
**Enables**: Better accuracy
**Effort**: 4 days

### Dead Code & Reachability (Items 161-175)

#### 161. ❌ Unreachable Code After Return
**Type**: Bug
**What**: Detect code after `Return[]` that can never execute
**Why**: Dead code
**Severity**: Major
**Effort**: 1 day

#### 162. ❌ Unreachable Branch (Always True)
**Type**: Bug
**What**: Detect `If[True, a, b]` where b is unreachable
**Why**: Dead code or logic error
**Severity**: Major
**Effort**: 1 day

#### 163. ❌ Unreachable Branch (Always False)
**Type**: Bug
**What**: Detect `If[False, a, b]` where a is unreachable
**Why**: Dead code or logic error
**Severity**: Major
**Effort**: 1 day

#### 164. ❌ Impossible Pattern
**Type**: Bug
**What**: Detect pattern that can never match
**Why**: Dead code
**Severity**: Major
**Effort**: 2 days

#### 165. ❌ Empty Catch Block
**Type**: Bug
**What**: Detect `Catch[...]` that catches nothing
**Why**: Either dead or error handling missing
**Severity**: Major
**Effort**: 1 day

#### 166. ❌ Condition Always Evaluates Same
**Type**: Bug
**What**: Detect condition that's always true or always false
**Why**: Logic error
**Severity**: Major
**Effort**: 2 days

#### 167. ❌ Infinite Loop (Proven)
**Type**: Bug
**What**: Detect loop with no exit condition (CFG-based)
**Why**: Hangs
**Severity**: Critical
**Effort**: 2 days

#### 168. ❌ Loop Never Executes
**Type**: Bug
**What**: Detect `While[False, ...]` or `Do[..., {i, 10, 1}]`
**Why**: Dead code
**Severity**: Major
**Effort**: 1 day

#### 169. ❌ Code After Abort
**Type**: Bug
**What**: Detect code after `Abort[]`
**Why**: Never executes
**Severity**: Major
**Effort**: 1 day

#### 170. ❌ Multiple Returns Make Code Unreachable
**Type**: Code Smell
**What**: Detect early returns leaving later code unreachable
**Why**: Dead code
**Severity**: Minor
**Effort**: 1 day

#### 171. ❌ Else Branch Never Taken
**Type**: Bug
**What**: Detect `If[cond, a, b]` where b never reached
**Why**: Logic error
**Severity**: Major
**Effort**: 1 day

#### 172. ❌ Switch Case Shadowed
**Type**: Bug
**What**: Detect later `Switch` case that's shadowed by earlier one
**Why**: Never matches
**Severity**: Major
**Effort**: 1 day

#### 173. ❌ Pattern Definition Shadowed
**Type**: Bug
**What**: Detect pattern definition that can never match (more specific after general)
**Why**: Dead code
**Severity**: Major
**Effort**: 1 day

#### 174. ❌ Exception Never Thrown
**Type**: Code Smell
**What**: Detect `Catch[expr, tag]` where tag never thrown in expr
**Why**: Unnecessary Catch
**Severity**: Minor
**Effort**: 2 days

#### 175. ❌ Break Outside Loop
**Type**: Bug
**What**: Detect `Break[]` outside loop context
**Why**: Runtime error
**Severity**: Critical
**Effort**: 1 day

### Taint Analysis for Security (Items 176-195)

#### 176. ✅ Taint Source Identification
**Type**: Infrastructure
**What**: Identify untrusted input sources: `Import[]`, `URLFetch[]`, `FormFunction[]`, etc.
**Why**: Where tainted data enters
**Enables**: All taint rules
**Effort**: 3 days

#### 177. ✅ Taint Sink Identification
**Type**: Infrastructure
**What**: Identify dangerous sinks: `ToExpression[]`, `RunProcess[]`, `SQLExecute[]`, etc.
**Why**: Where tainted data causes damage
**Enables**: All taint rules
**Effort**: 3 days

#### 178. ✅ Taint Propagation Through Variables
**Type**: Infrastructure
**What**: Track taint as data flows through assignments
**Why**: Follow the data
**Enables**: Taint tracking
**Effort**: 4 days

#### 179. ✅ Taint Propagation Through Functions
**Type**: Infrastructure
**What**: Track taint through function calls
**Why**: Inter-procedural taint
**Enables**: Better coverage
**Effort**: 5 days

#### 180. ✅ Sanitizer Recognition
**Type**: Infrastructure
**What**: Recognize sanitization functions that clean tainted data
**Why**: Know when data is safe
**Enables**: Reduce false positives
**Effort**: 3 days

#### 181. ❌ SQL Injection
**Type**: Vulnerability
**What**: Detect tainted data flowing to `SQLExecute[]`
**Why**: Database compromise
**Severity**: Critical
**Effort**: 2 days

#### 182. ❌ Command Injection
**Type**: Vulnerability
**What**: Detect tainted data flowing to `RunProcess[]`, `Run[]`
**Why**: System compromise
**Severity**: Critical
**Effort**: 2 days

#### 183. ❌ Code Injection
**Type**: Vulnerability
**What**: Detect tainted data flowing to `ToExpression[]`
**Why**: Arbitrary code execution
**Severity**: Critical
**Effort**: 2 days

#### 184. ❌ Path Traversal
**Type**: Vulnerability
**What**: Detect tainted data in file paths: `Import[untrusted]`
**Why**: Unauthorized file access
**Severity**: Critical
**Effort**: 2 days

#### 185. ❌ XSS (Cross-Site Scripting)
**Type**: Vulnerability
**What**: Detect tainted data in `XMLElement[]`, `ExportString[..., "HTML"]`
**Why**: Client-side code injection
**Severity**: Critical
**Effort**: 2 days

#### 186. ❌ LDAP Injection
**Type**: Vulnerability
**What**: Detect tainted data in LDAP queries
**Why**: Authentication bypass
**Severity**: Critical
**Effort**: 2 days

#### 187. ❌ XML External Entity (XXE)
**Type**: Vulnerability
**What**: Detect unsafe XML parsing with external entities enabled
**Why**: Information disclosure
**Severity**: Critical
**Effort**: 2 days

#### 188. ❌ Unsafe Deserialization
**Type**: Vulnerability
**What**: Detect `Import[untrusted, "MX"]` or `Get[untrusted]`
**Why**: Arbitrary code execution
**Severity**: Critical
**Effort**: 2 days

#### 189. ❌ Server-Side Request Forgery (SSRF)
**Type**: Vulnerability
**What**: Detect tainted URLs in `URLFetch[]`, `URLExecute[]`
**Why**: Internal network access
**Severity**: Critical
**Effort**: 2 days

#### 190. ❌ Insecure Randomness
**Type**: Vulnerability
**What**: Detect `RandomInteger[]` used for security-sensitive values
**Why**: Predictable
**Severity**: Major
**Effort**: 1 day

#### 191. ❌ Weak Cryptography
**Type**: Vulnerability
**What**: Detect use of MD5, SHA1 for security purposes
**Why**: Broken algorithms
**Severity**: Major
**Effort**: 1 day

#### 192. ❌ Hard-Coded Credentials (Taint-Based)
**Type**: Vulnerability
**What**: Detect string literals flowing to authentication
**Why**: Credential leak
**Severity**: Critical
**Effort**: 1 day

#### 193. ❌ Sensitive Data In Logs
**Type**: Vulnerability
**What**: Detect tainted data from authentication flowing to `Print[]`, logging
**Why**: Information disclosure
**Severity**: Major
**Effort**: 2 days

#### 194. ❌ Mass Assignment
**Type**: Vulnerability
**What**: Detect tainted association directly used in database update
**Why**: Privilege escalation
**Severity**: Critical
**Effort**: 2 days

#### 195. ❌ Regex DoS
**Type**: Vulnerability
**What**: Detect tainted data in regex patterns (catastrophic backtracking)
**Why**: Denial of service
**Severity**: Major
**Effort**: 2 days

### Additional Control Flow Rules (Items 196-200)

#### 196. ❌ Missing Default Case
**Type**: Bug
**What**: Detect `Switch` without default case
**Why**: May return unevaluated
**Severity**: Minor
**Effort**: 1 day

#### 197. ❌ Empty If Branch
**Type**: Code Smell
**What**: Detect `If[cond, , else]` (empty true branch)
**Why**: Confusing, use `If[!cond, else]`
**Severity**: Minor
**Effort**: 1 day

#### 198. ❌ Nested If Depth
**Type**: Code Smell
**What**: Detect deeply nested If statements (>4 levels)
**Why**: Complexity
**Severity**: Minor
**Effort**: 1 day

#### 199. ❌ Too Many Return Points
**Type**: Code Smell
**What**: Detect function with >5 return statements
**Why**: Hard to reason about
**Severity**: Minor
**Effort**: 1 day

#### 200. ❌ Missing Else Considered Harmful
**Type**: Code Smell
**What**: Detect `If[cond, action]` that should have else
**Why**: Unclear intent
**Severity**: Info
**Effort**: 1 day

---

## 🔗 CHUNK 5: Cross-File & Architecture Analysis (Items 201-250)

**Technology Milestone**: Multi-file analysis, dependency graph, package structure validation

**What Unlocks**: Architecture rules, circular dependency detection, unused exports, API consistency

**Duration**: 3-4 months

### Cross-File Infrastructure (Items 201-210)

#### 201. ✅ Multi-File Symbol Table
**Type**: Infrastructure
**What**: Build symbol table spanning multiple files
**Why**: Cross-file analysis
**Enables**: 40+ rules
**Effort**: 5 days

#### 202. ✅ Package Dependency Graph
**Type**: Infrastructure
**What**: Graph of which packages depend on which
**Why**: Architecture analysis
**Enables**: Dependency rules
**Effort**: 4 days

#### 203. ✅ Export/Import Tracking
**Type**: Infrastructure
**What**: Track what each package exports and who imports it
**Why**: Unused export detection
**Enables**: API rules
**Effort**: 3 days

#### 204. ✅ Call Graph (Cross-File)
**Type**: Infrastructure
**What**: Graph of all function calls across files
**Why**: Unused function detection
**Enables**: Dead code rules
**Effort**: 5 days

#### 205. ✅ Type Propagation Across Files
**Type**: Infrastructure
**What**: Track types across function boundaries between files
**Why**: Better type inference
**Enables**: Cross-file type rules
**Effort**: 4 days

#### 206. ✅ Package Structure Validation
**Type**: Infrastructure
**What**: Validate BeginPackage/Begin/End/EndPackage structure
**Why**: Correct encapsulation
**Enables**: Structure rules
**Effort**: 3 days

#### 207. ✅ Context Path Analysis
**Type**: Infrastructure
**What**: Analyze $ContextPath and symbol resolution
**Why**: Understand imports
**Enables**: Import rules
**Effort**: 3 days

#### 208. ✅ Initialization Order Analysis
**Type**: Infrastructure
**What**: Determine safe package loading order
**Why**: Avoid initialization errors
**Enables**: Load order rules
**Effort**: 4 days

#### 209. ✅ Public API Surface Analysis
**Type**: Infrastructure
**What**: Identify all public symbols vs private
**Why**: API documentation, encapsulation
**Enables**: API rules
**Effort**: 3 days

#### 210. ✅ Package Metrics Collection
**Type**: Infrastructure
**What**: Collect metrics: lines of code, function count, dependency count per package
**Why**: Architecture metrics
**Enables**: Quality metrics
**Effort**: 2 days

### Dependency & Architecture Rules (Items 211-230)

#### 211. ❌ Circular Package Dependency
**Type**: Bug
**What**: Detect A.m needs B.m needs A.m
**Why**: Load order issues
**Severity**: Critical
**Effort**: 2 days

#### 212. ❌ Unused Package Import
**Type**: Code Smell
**What**: Detect `Needs["Package`"]` where no symbols from Package used
**Why**: Unnecessary dependency
**Severity**: Minor
**Effort**: 2 days

#### 213. ❌ Missing Package Import
**Type**: Bug
**What**: Detect use of symbol from package without Needs
**Why**: May fail in fresh kernel
**Severity**: Major
**Effort**: 2 days

#### 214. ❌ Transitive Dependency Could Be Direct
**Type**: Code Smell
**What**: Detect reliance on transitive import
**Why**: Fragile
**Severity**: Minor
**Effort**: 2 days

#### 215. ❌ Diamond Dependency
**Type**: Code Smell
**What**: Detect A depends on B and C, both depend on D
**Why**: Version conflicts
**Severity**: Info
**Effort**: 2 days

#### 216. ❌ God Package (Too Many Dependencies)
**Type**: Code Smell
**What**: Detect package that depends on >10 other packages
**Why**: High coupling
**Severity**: Minor
**Effort**: 1 day

#### 217. ❌ Package Depends On Application Code
**Type**: Bug
**What**: Detect library package depending on application-specific code
**Why**: Wrong dependency direction
**Severity**: Major
**Effort**: 2 days

#### 218. ❌ Cyclic Call Between Packages
**Type**: Code Smell
**What**: Detect PackageA calls PackageB calls PackageA
**Why**: Tight coupling
**Severity**: Minor
**Effort**: 2 days

#### 219. ❌ Layer Violation
**Type**: Code Smell
**What**: Detect lower layer depending on higher layer
**Why**: Architectural violation
**Severity**: Minor
**Effort**: 3 days

#### 220. ❌ Unstable Dependency
**Type**: Code Smell
**What**: Detect stable package depending on unstable package
**Why**: Ripple effects
**Severity**: Minor
**Effort**: 2 days

#### 221. ❌ Package Too Large
**Type**: Code Smell
**What**: Detect package with >3000 lines
**Why**: Should be split
**Severity**: Info
**Effort**: 1 day

#### 222. ❌ Package Too Small
**Type**: Code Smell
**What**: Detect package with <50 lines
**Why**: Over-modularization
**Severity**: Info
**Effort**: 1 day

#### 223. ❌ Inconsistent Package Naming
**Type**: Code Smell
**What**: Detect package names not following convention
**Why**: Consistency
**Severity**: Info
**Effort**: 1 day

#### 224. ❌ Package Exports Too Much
**Type**: Code Smell
**What**: Detect package with >50 public symbols
**Why**: Poor cohesion
**Severity**: Info
**Effort**: 1 day

#### 225. ❌ Package Exports Too Little
**Type**: Code Smell
**What**: Detect package with 0-1 public symbols
**Why**: Questionable design
**Severity**: Info
**Effort**: 1 day

#### 226. ❌ Incomplete Public API
**Type**: Code Smell
**What**: Detect public function relying on private function
**Why**: Breaks encapsulation
**Severity**: Minor
**Effort**: 2 days

#### 227. ❌ Private Symbol Used Externally
**Type**: Bug
**What**: Detect symbol in Private` context used from another package
**Why**: Breaks encapsulation
**Severity**: Major
**Effort**: 2 days

#### 228. ❌ Internal Implementation Exposed
**Type**: Code Smell
**What**: Detect "Internal`" symbols used from outside
**Why**: Unstable API
**Severity**: Major
**Effort**: 2 days

#### 229. ❌ Missing Package Documentation
**Type**: Code Smell
**What**: Detect package without top-level usage message
**Why**: Discoverability
**Severity**: Info
**Effort**: 1 day

#### 230. ❌ Public API Changed Without Version Bump
**Type**: Code Smell
**What**: Detect breaking changes to public symbols
**Why**: Semantic versioning
**Severity**: Minor
**Effort**: 3 days

### Unused Export & Dead Code (Cross-File) (Items 231-245)

#### 231. ❌ Unused Public Function
**Type**: Code Smell
**What**: Detect public function never called from outside
**Why**: Dead code or over-engineering
**Severity**: Minor
**Effort**: 2 days

#### 232. ❌ Unused Export
**Type**: Code Smell
**What**: Detect symbol exported but never imported anywhere
**Why**: Unnecessary API surface
**Severity**: Minor
**Effort**: 2 days

#### 233. ❌ Dead Package
**Type**: Code Smell
**What**: Detect package never imported by anyone
**Why**: Dead code
**Severity**: Minor
**Effort**: 2 days

#### 234. ❌ Function Only Called Once
**Type**: Code Smell
**What**: Detect function called from exactly one place
**Why**: Should be inlined
**Severity**: Info
**Effort**: 2 days

#### 235. ❌ Over-Abstracted API
**Type**: Code Smell
**What**: Detect API with single implementation
**Why**: YAGNI violation
**Severity**: Info
**Effort**: 2 days

#### 236. ❌ Orphaned Test File
**Type**: Code Smell
**What**: Detect test file for non-existent implementation
**Why**: Stale tests
**Severity**: Minor
**Effort**: 1 day

#### 237. ❌ Implementation Without Tests
**Type**: Code Smell
**What**: Detect implementation file without corresponding test file
**Why**: Test coverage
**Severity**: Info
**Effort**: 1 day

#### 238. ❌ Deprecated API Still Used Internally
**Type**: Code Smell
**What**: Detect deprecated function still called from same package
**Why**: Should migrate
**Severity**: Minor
**Effort**: 2 days

#### 239. ❌ Internal API Used Like Public
**Type**: Bug
**What**: Detect Internal` symbol called from >1 package
**Why**: Should be public or private
**Severity**: Minor
**Effort**: 2 days

#### 240. ❌ Commented Out Package Load
**Type**: Code Smell
**What**: Detect commented `(* Needs["..."] *)`
**Why**: Dead dependency or TODO
**Severity**: Info
**Effort**: 1 day

#### 241. ❌ Conditional Package Load
**Type**: Code Smell
**What**: Detect `If[condition, Needs[...]]`
**Why**: Fragile dependency
**Severity**: Minor
**Effort**: 1 day

#### 242. ❌ Package Loaded But Not Listed In Metadata
**Type**: Code Smell
**What**: Detect Needs not reflected in PacletInfo.m
**Why**: Incomplete metadata
**Severity**: Minor
**Effort**: 2 days

#### 243. ❌ Duplicate Symbol Definition Across Packages
**Type**: Bug
**What**: Detect same symbol defined in multiple packages
**Why**: Conflict
**Severity**: Major
**Effort**: 2 days

#### 244. ❌ Symbol Redefinition After Import
**Type**: Bug
**What**: Detect symbol defined locally after importing package with same symbol
**Why**: Confusing shadowing
**Severity**: Major
**Effort**: 2 days

#### 245. ❌ Package Version Mismatch
**Type**: Bug
**What**: Detect imports of incompatible package versions
**Why**: Runtime errors
**Severity**: Critical
**Effort**: 3 days

### Documentation & Consistency (Items 246-250)

#### 246. ❌ Missing Usage Message
**Type**: Code Smell
**What**: Detect public function without usage message
**Why**: Discoverability
**Severity**: Info
**Effort**: 1 day

#### 247. ❌ Inconsistent Parameter Names Across Overloads
**Type**: Code Smell
**What**: Detect `f[x_]` and `f[y_, z_]` (inconsistent naming)
**Why**: Confusing API
**Severity**: Info
**Effort**: 2 days

#### 248. ❌ Public Function With Implementation Details In Name
**Type**: Code Smell
**What**: Detect public symbols with "Internal", "Helper", "Private" in name
**Why**: Leaky abstraction
**Severity**: Minor
**Effort**: 1 day

#### 249. ❌ Public API Not In Package Context
**Type**: Bug
**What**: Detect public symbol not in package context
**Why**: Wrong context
**Severity**: Major
**Effort**: 1 day

#### 250. ❌ Test Function In Production Code
**Type**: Code Smell
**What**: Detect functions with "Test" in name in production package
**Why**: Should be in test package
**Severity**: Minor
**Effort**: 1 day

---

## 🧩 CHUNK 6: Advanced Semantic Analysis (Items 251-300)

**Technology Milestone**: Advanced semantic understanding - null propagation, constant propagation, advanced pattern analysis

**What Unlocks**: Subtle bug detection, code quality improvements, Mathematica-specific patterns

**Duration**: 3-4 months

### Null Safety & Error Handling (Items 251-265)

#### 251. ✅ Null Propagation Analysis
**Type**: Infrastructure
**What**: Track how `$Failed`, `Null`, `Missing[]` propagate through code
**Why**: Null safety
**Enables**: Null-related rules
**Effort**: 4 days

#### 252. ❌ Null Dereference
**Type**: Bug
**What**: Detect operations on potentially null values
**Why**: Runtime error
**Severity**: Major
**Effort**: 2 days

#### 253. ❌ Missing Null Check
**Type**: Bug
**What**: Detect function result used without checking for $Failed
**Why**: Error propagation
**Severity**: Major
**Effort**: 2 days

#### 254. ❌ Null Return Not Documented
**Type**: Code Smell
**What**: Detect function that can return Null without documenting it
**Why**: API surprise
**Severity**: Minor
**Effort**: 1 day

#### 255. ❌ Comparison With Null
**Type**: Bug
**What**: Detect `x == Null` (should use `x === Null`)
**Why**: Semantic difference
**Severity**: Minor
**Effort**: 1 day

#### 256. ❌ Missing Check Leads To Null Propagation
**Type**: Bug
**What**: Detect chain where null propagates through multiple operations
**Why**: Error cascade
**Severity**: Major
**Effort**: 2 days

#### 257. ❌ Check Pattern Doesn't Handle All Cases
**Type**: Bug
**What**: Detect `Check[expr, fallback]` missing some error cases
**Why**: Incomplete error handling
**Severity**: Minor
**Effort**: 2 days

#### 258. ❌ Quiet Suppressing Important Messages
**Type**: Bug
**What**: Detect `Quiet[...]` suppressing critical messages
**Why**: Masks real errors
**Severity**: Major
**Effort**: 2 days

#### 259. ❌ Off Disabling Important Warnings
**Type**: Bug
**What**: Detect `Off[General::...]` for important warnings
**Why**: Masks problems
**Severity**: Major
**Effort**: 1 day

#### 260. ❌ Catch-All Exception Handler
**Type**: Bug
**What**: Detect `Catch[expr]` catching all tags
**Why**: Too broad
**Severity**: Minor
**Effort**: 1 day

#### 261. ❌ Empty Exception Handler
**Type**: Bug
**What**: Detect `Catch[expr, _, Null &]` (silently ignoring errors)
**Why**: Error information lost
**Severity**: Major
**Effort**: 1 day

#### 262. ❌ Throw Without Catch
**Type**: Bug
**What**: Detect `Throw[...]` with no surrounding Catch
**Why**: Will abort evaluation
**Severity**: Major
**Effort**: 2 days

#### 263. ❌ Abort In Library Code
**Type**: Bug
**What**: Detect `Abort[]` in public library function
**Why**: Too aggressive, should return error
**Severity**: Major
**Effort**: 1 day

#### 264. ❌ Message Without Definition
**Type**: Bug
**What**: Detect message issued but not defined: `Message[f::undefined]`
**Why**: Generic message shown
**Severity**: Minor
**Effort**: 2 days

#### 265. ❌ Missing Message Definition
**Type**: Code Smell
**What**: Detect function issuing messages without defining them first
**Why**: Unclear error messages
**Severity**: Minor
**Effort**: 1 day

### Constant & Expression Analysis (Items 266-280)

#### 266. ✅ Constant Propagation
**Type**: Infrastructure
**What**: Track constant values through code: `x = 5; If[x > 0, ...]`
**Why**: Find always-true/false conditions
**Enables**: Dead code rules
**Effort**: 4 days

#### 267. ❌ Condition Always True (Constant Propagation)
**Type**: Bug
**What**: Detect `If[True, ...]` from constant propagation
**Why**: Dead branch or logic error
**Severity**: Major
**Effort**: 1 day

#### 268. ❌ Condition Always False (Constant Propagation)
**Type**: Bug
**What**: Detect `If[False, ...]` from constant propagation
**Why**: Dead branch or logic error
**Severity**: Major
**Effort**: 1 day

#### 269. ❌ Loop Bound Constant
**Type**: Code Smell
**What**: Detect `Do[..., {i, 1, x}]` where x is constant
**Why**: Use literal value
**Severity**: Info
**Effort**: 1 day

#### 270. ❌ Redundant Computation
**Type**: Performance
**What**: Detect same expression computed multiple times with same inputs
**Why**: Could cache result
**Severity**: Minor
**Effort**: 2 days

#### 271. ❌ Pure Expression In Loop
**Type**: Performance
**What**: Detect pure (side-effect-free) expression computed in every iteration
**Why**: Hoist outside loop
**Severity**: Minor
**Effort**: 2 days

#### 272. ❌ Constant Expression
**Type**: Code Smell
**What**: Detect `x + 0`, `x * 1`, `x^1`
**Why**: Simplify to `x`
**Severity**: Info
**Effort**: 1 day

#### 273. ❌ Identity Operation
**Type**: Code Smell
**What**: Detect `Reverse[Reverse[x]]`, `Transpose[Transpose[x]]`
**Why**: No-op
**Severity**: Info
**Effort**: 1 day

#### 274. ❌ Comparison Of Identical Expressions
**Type**: Bug
**What**: Detect `x == x` (always true)
**Why**: Logic error or typo
**Severity**: Major
**Effort**: 1 day

#### 275. ❌ Boolean Expression Always True
**Type**: Bug
**What**: Detect `x || !x` (tautology)
**Why**: Logic error
**Severity**: Major
**Effort**: 1 day

#### 276. ❌ Boolean Expression Always False
**Type**: Bug
**What**: Detect `x && !x` (contradiction)
**Why**: Logic error
**Severity**: Major
**Effort**: 1 day

#### 277. ❌ Unnecessary Boolean Conversion
**Type**: Code Smell
**What**: Detect `If[cond, True, False]` (should just be `cond`)
**Why**: Redundant
**Severity**: Info
**Effort**: 1 day

#### 278. ❌ Double Negation
**Type**: Code Smell
**What**: Detect `!!x` or `Not[Not[x]]`
**Why**: Simplify to `x`
**Severity**: Info
**Effort**: 1 day

#### 279. ❌ Complex Boolean Expression
**Type**: Code Smell
**What**: Detect boolean expression with >5 operators
**Why**: Hard to understand
**Severity**: Minor
**Effort**: 1 day

#### 280. ❌ De Morgan's Law Opportunity
**Type**: Code Smell
**What**: Detect `!(a && b)` (could be `!a || !b`)
**Why**: Clarity
**Severity**: Info
**Effort**: 2 days

### Mathematica-Specific Patterns (Items 281-300)

#### 281. ❌ Hold Attribute Missing
**Type**: Bug
**What**: Detect function manipulating unevaluated expressions without Hold attribute
**Why**: Arguments will evaluate
**Severity**: Major
**Effort**: 2 days

#### 282. ❌ HoldFirst But Uses Second Argument First
**Type**: Bug
**What**: Detect function with HoldFirst that evaluates second arg first
**Why**: Unexpected evaluation order
**Severity**: Major
**Effort**: 2 days

#### 283. ❌ Missing Unevaluated Wrapper
**Type**: Bug
**What**: Detect passing expression to held parameter without Unevaluated
**Why**: Premature evaluation
**Severity**: Major
**Effort**: 2 days

#### 284. ❌ Unnecessary Hold
**Type**: Code Smell
**What**: Detect `Hold[literal]` where literal doesn't evaluate
**Why**: Redundant
**Severity**: Info
**Effort**: 1 day

#### 285. ❌ ReleaseHold After Hold
**Type**: Code Smell
**What**: Detect `ReleaseHold[Hold[x]]`
**Why**: Redundant
**Severity**: Info
**Effort**: 1 day

#### 286. ❌ Evaluate In Held Context
**Type**: Bug
**What**: Detect `Hold[..., Evaluate[x], ...]` (evaluation leak)
**Why**: May not be intended
**Severity**: Minor
**Effort**: 1 day

#### 287. ❌ Pattern With Side Effect
**Type**: Bug
**What**: Detect pattern test with side effects: `x_?(Print[#]; True &)`
**Why**: Evaluated multiple times
**Severity**: Major
**Effort**: 2 days

#### 288. ❌ Replacement Rule Order Matters
**Type**: Bug
**What**: Detect replacement rules where order affects result
**Why**: Non-deterministic behavior
**Severity**: Major
**Effort**: 2 days

#### 289. ❌ ReplaceAll vs Replace Confusion
**Type**: Bug
**What**: Detect `/. list` where `Replace[..., list, {1}]` intended
**Why**: Different semantics
**Severity**: Major
**Effort**: 1 day

#### 290. ❌ Rule Doesn't Match Due To Evaluation
**Type**: Bug
**What**: Detect rule that won't match due to evaluation timing
**Why**: Subtle bug
**Severity**: Major
**Effort**: 2 days

#### 291. ❌ Part Specification Out Of Bounds
**Type**: Bug
**What**: Detect `list[[100]]` where list has <100 elements
**Why**: Runtime error
**Severity**: Critical
**Effort**: 2 days

#### 292. ❌ Span Specification Invalid
**Type**: Bug
**What**: Detect `list[[10;;1]]` (backwards span)
**Why**: Empty result
**Severity**: Minor
**Effort**: 1 day

#### 293. ❌ All Specification Inefficient
**Type**: Performance
**What**: Detect `list[[All]]` (redundant, just use `list`)
**Why**: Unnecessary operation
**Severity**: Info
**Effort**: 1 day

#### 294. ❌ Threading Over Non-Lists
**Type**: Bug
**What**: Detect function with Listable attribute called on non-list
**Why**: Unexpected result
**Severity**: Minor
**Effort**: 1 day

#### 295. ❌ Missing Attributes Declaration
**Type**: Code Smell
**What**: Detect function that should have Listable but doesn't
**Why**: Performance opportunity
**Severity**: Minor
**Effort**: 2 days

#### 296. ❌ OneIdentity Attribute Misuse
**Type**: Bug
**What**: Detect OneIdentity on function where it causes issues
**Why**: Subtle semantic change
**Severity**: Major
**Effort**: 2 days

#### 297. ❌ Orderless Attribute On Non-Commutative
**Type**: Bug
**What**: Detect Orderless on non-commutative operation
**Why**: Wrong semantics
**Severity**: Critical
**Effort**: 2 days

#### 298. ❌ Flat Attribute Misuse
**Type**: Bug
**What**: Detect Flat on non-associative operation
**Why**: Wrong semantics
**Severity**: Critical
**Effort**: 2 days

#### 299. ❌ Sequence In Unexpected Context
**Type**: Bug
**What**: Detect `Sequence[...]` flattening where not intended
**Why**: Lost structure
**Severity**: Major
**Effort**: 2 days

#### 300. ❌ Missing Sequence Wrapper
**Type**: Bug
**What**: Detect place where `Sequence[]` should be used but isn't
**Why**: Extra nesting
**Severity**: Minor
**Effort**: 1 day

---

## 🎓 CHUNK 7: Coverage, Performance & Polish (Items 301-325)

**Technology Milestone**: Test coverage integration + Performance analysis + Developer experience features

**What Unlocks**: Quality metrics, optimization guidance, IDE-like features

**Duration**: 2-3 months

### Test Coverage Integration (Items 301-310)

#### 301. ✅ VerificationTest Parser
**Type**: Infrastructure
**What**: Parse Mathematica `VerificationTest[]` format and extract results
**Why**: Native Mathematica testing support
**Enables**: Coverage visualization
**Effort**: 4 days

#### 302. ✅ MUnit Integration
**Type**: Infrastructure
**What**: Parse MUnit test framework results
**Why**: Alternative test framework
**Enables**: Coverage visualization
**Effort**: 3 days

#### 303. ✅ Coverage Report Import
**Type**: Infrastructure
**What**: Import coverage reports (LCOV, Cobertura, JaCoCo formats)
**Why**: External coverage tools
**Enables**: Flexible coverage
**Effort**: 3 days

#### 304. ✅ Line Coverage Calculation
**Type**: Infrastructure
**What**: Calculate line coverage percentage per file
**Why**: Standard coverage metric
**Enables**: Quality gates
**Effort**: 2 days

#### 305. ✅ Branch Coverage Calculation
**Type**: Infrastructure
**What**: Calculate branch coverage (If/Which/Switch branches)
**Why**: Better than line coverage
**Enables**: Quality gates
**Effort**: 3 days

#### 306. ✅ Function Coverage Tracking
**Type**: Infrastructure
**What**: Track which functions are tested
**Why**: Identify untested code
**Enables**: Test gap analysis
**Effort**: 2 days

#### 307. ❌ Low Test Coverage Warning
**Type**: Code Smell
**What**: Warn when file has <80% line coverage
**Why**: Quality metric
**Severity**: Info
**Effort**: 1 day

#### 308. ❌ Untested Public Function
**Type**: Code Smell
**What**: Warn when public function has no tests
**Why**: API quality
**Severity**: Minor
**Effort**: 1 day

#### 309. ❌ Untested Branch
**Type**: Code Smell
**What**: Warn when If/Which branch never executed in tests
**Why**: Test gap
**Severity**: Minor
**Effort**: 1 day

#### 310. ❌ Test-Only Code In Production
**Type**: Bug
**What**: Detect code only executed during tests
**Why**: Dead code in production
**Severity**: Minor
**Effort**: 2 days

### Performance Analysis (Items 311-320)

#### 311. ✅ Compilation Candidate Detection
**Type**: Infrastructure
**What**: Identify functions suitable for `Compile[]`
**Why**: Performance optimization
**Enables**: Performance suggestions
**Effort**: 4 days

#### 312. ❌ Compilable Function Not Compiled
**Type**: Performance
**What**: Suggest `Compile[]` for suitable functions
**Why**: 10-100x speedup potential
**Severity**: Minor
**Effort**: 2 days

#### 313. ❌ Compilation Target Missing
**Type**: Performance
**What**: Detect `Compile[..., CompilationTarget -> "MVM"]` (should be "C")
**Why**: C compilation much faster
**Severity**: Minor
**Effort**: 1 day

#### 314. ❌ Non-Compilable Construct In Compile
**Type**: Bug
**What**: Detect use of non-compilable functions in `Compile[]`
**Why**: Falls back to slow evaluation
**Severity**: Major
**Effort**: 2 days

#### 315. ❌ Packed Array Unpacked
**Type**: Performance
**What**: Detect operations that unpack packed arrays
**Why**: 10-100x slower
**Severity**: Major
**Effort**: 3 days

#### 316. ❌ Inefficient Pattern In Performance Critical Code
**Type**: Performance
**What**: Detect complex patterns in hot loops
**Why**: Pattern matching is slow
**Severity**: Minor
**Effort**: 2 days

#### 317. ❌ N Applied Too Late
**Type**: Performance
**What**: Detect symbolic computation followed by `N[]`
**Why**: Do numeric from start
**Severity**: Minor
**Effort**: 1 day

#### 318. ❌ Missing Memoization Opportunity
**Type**: Performance
**What**: Detect recursive function without memoization
**Why**: Exponential time complexity
**Severity**: Minor
**Effort**: 2 days

#### 319. ❌ Inefficient String Concatenation
**Type**: Performance
**What**: Detect repeated `<>` in loop
**Why**: Quadratic complexity
**Severity**: Minor
**Effort**: 1 day

#### 320. ❌ List Concatenation In Loop
**Type**: Performance
**What**: Detect `Join[list, {x}]` in loop
**Why**: Quadratic complexity
**Severity**: Minor
**Effort**: 1 day

### Developer Experience & Quick Fixes (Items 321-325)

#### 321. ✅ Quick Fix Infrastructure
**Type**: Infrastructure
**What**: Framework for suggesting automatic fixes
**Why**: Developer productivity
**Enables**: Quick fixes
**Effort**: 5 days

#### 322. ✅ Auto-Import Suggestion
**Type**: Feature
**What**: Suggest `Needs[]` for undefined symbols
**Why**: Convenience
**Effort**: 2 days

#### 323. ✅ Rename Symbol Safely
**Type**: Feature
**What**: Rename symbol across all usages
**Why**: Refactoring support
**Effort**: 3 days

#### 324. ✅ Extract Function Refactoring
**Type**: Feature
**What**: Extract selected code to new function
**Why**: Refactoring support
**Effort**: 4 days

#### 325. ✅ Generate Tests Skeleton
**Type**: Feature
**What**: Generate VerificationTest template for function
**Why**: Test writing convenience
**Effort**: 2 days

---

## 📊 Summary Statistics

### Implementation Timeline

| Chunk | Items | Duration | Cumulative | Key Technology |
|-------|-------|----------|------------|----------------|
| **Chunk 1** | 1-50 | 3-4 months | 3-4 months | Enhanced AST Parser |
| **Chunk 2** | 51-100 | 3-4 months | 6-8 months | Symbol Table |
| **Chunk 3** | 101-150 | 3-4 months | 9-12 months | Type Inference & Data Flow |
| **Chunk 4** | 151-200 | 3-4 months | 12-16 months | Control Flow & Taint |
| **Chunk 5** | 201-250 | 3-4 months | 15-20 months | Cross-File Analysis |
| **Chunk 6** | 251-300 | 3-4 months | 18-24 months | Advanced Semantics |
| **Chunk 7** | 301-325 | 2-3 months | 20-27 months | Coverage & Polish |

**Total**: 20-27 months (part-time, ~20 hrs/week)

### Rule Distribution

| Category | Count | Percentage |
|----------|-------|------------|
| **Bug** | 130 | 40% |
| **Code Smell** | 115 | 35% |
| **Vulnerability** | 50 | 15% |
| **Performance** | 30 | 9% |

Total: **325 new rules** → **449 total rules** (with current 124)

### Technology Milestones

| Milestone | Chunk | Impact |
|-----------|-------|--------|
| **Enhanced AST** | 1 | Foundation for everything |
| **Symbol Table** | 2 | Enables semantic analysis |
| **Type Inference** | 3 | Type safety |
| **Control Flow Graph** | 4 | Advanced bug detection |
| **Cross-File Analysis** | 5 | Architecture rules |
| **Advanced Semantics** | 6 | Subtle bugs |
| **Coverage & Perf** | 7 | Quality metrics |

---

## 🎯 Critical Success Factors

### 1. **Don't Skip Ahead**
Each chunk builds on previous chunks. Trying to implement Chunk 5 rules without Chunk 1-4 infrastructure will fail.

### 2. **Infrastructure First**
Within each chunk, implement infrastructure items (✅) before rules (❌). Rules depend on infrastructure.

### 3. **Test Incrementally**
After each chunk, ensure all new rules work correctly before moving to next chunk.

### 4. **Measure Impact**
Track false positive rate, false negative rate, and user feedback for each chunk.

### 5. **Prioritize User Value**
If a rule isn't providing value, deprioritize or remove it. Focus on rules that find real bugs.

---

## 🚀 Getting Started

**Immediate Next Steps:**

1. ✅ **Wait for current scan to complete** (verify fixes work)
2. ✅ **Start Chunk 1** - Enhanced AST Parser
3. ✅ **Focus on items 1-15 first** (core AST infrastructure)
4. ✅ **Then add rules 16-50** (pattern system, lists, associations)
5. ✅ **Validate** chunk 1 before moving to chunk 2

**Estimated Timeline:**
- **Month 1-3**: Chunk 1 (Enhanced AST)
- **Month 4-6**: Chunk 2 (Symbol Table)
- **Month 7-9**: Chunk 3 (Type Inference)
- **Month 10-12**: Chunk 4 (Control Flow)
- **Month 13-15**: Chunk 5 (Cross-File)
- **Month 16-18**: Chunk 6 (Advanced)
- **Month 19-21**: Chunk 7 (Coverage)
- **Month 22-27**: Buffer for testing, refinement, documentation

**End Result**: Professional-grade Mathematica support rivaling Scala plugin! 🎉

---

**Remember**: This is a marathon, not a sprint. Each chunk provides value independently, so you'll see continuous improvement throughout the 20-27 month journey.

Good luck! 🚀
