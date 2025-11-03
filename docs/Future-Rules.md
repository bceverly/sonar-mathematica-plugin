# Future Rule Ideas for Mathematica Plugin

**Date**: 2025-10-30
**Current Status**: 430+ rules implemented
**Source**: Extracted from ROADMAP_325.md

This document catalogs **300+ specific rule ideas** for future implementation. These are organized by category and include "What", "Why", and "Example" for each rule.

---

## Pattern System Rules (15 rules)

### Unrestricted Blank Pattern Warning
**Type**: Code Smell
**What**: Detect `f[x_] := ...` without type restriction when type checking would help
**Why**: Runtime errors from wrong types
**Example**: `f[x_] := x^2` should be `f[x_?NumericQ] := x^2`

### Pattern Test vs Condition Confusion
**Type**: Bug
**What**: Detect incorrect use of `?` vs `/;`
**Why**: Different evaluation semantics
**Example**: `f[x_?IntegerQ] := x` (correct) vs `f[x_ /; IntegerQ[x]] := x` (works but inefficient)

### BlankSequence Without Restriction
**Type**: Performance
**What**: Detect `f[x__] := ...` that could be `f[x__Integer] := ...`
**Why**: Performance and correctness

### Nested Optional Patterns
**Type**: Bug
**What**: Detect `f[x_:1, y_:x] := ...` (y default depends on x)
**Why**: Evaluation order issues

### Pattern Naming Conflicts
**Type**: Bug
**What**: Detect same pattern name used with different restrictions
**Example**: `f[x_Integer, x_Real] := ...` (impossible to match)

### Repeated Pattern Alternatives
**Type**: Code Smell
**What**: Detect `f[x_Integer | x_Real] := ...` (redundant pattern name)
**Why**: Should be `f[x:(_Integer | _Real)] := ...`

### Pattern Test With Pure Function
**Type**: Performance
**What**: Detect `x_?(# > 0 &)` in hot code
**Why**: Creates closure on each match

### Missing Pattern Defaults
**Type**: Bug
**What**: Detect optional arguments without sensible defaults
**Example**: `f[x_, opts___] := ...` (should validate opts)

### Order-Dependent Pattern Definitions
**Type**: Bug
**What**: Detect when specific patterns defined after general ones
**Example**: `f[x_] := ...; f[0] := ...` (second never matches)

### Verbatim Pattern Misuse
**Type**: Bug
**What**: Detect incorrect use of `Verbatim`
**Why**: Tricky semantics

### HoldPattern Unnecessary
**Type**: Code Smell
**What**: Detect `HoldPattern` where not needed
**Why**: Clutter

### Longest/Shortest Without Ordering
**Type**: Bug
**What**: Detect `Longest[x__]` without proper context
**Why**: May not work as expected

### Pattern Repeated With Different Types
**Type**: Bug
**What**: Detect `f[{x_, x_}] := ...` expecting same value twice
**Why**: Should use `f[{x_, y_} /; x == y] := ...`

### Alternatives Too Complex
**Type**: Performance
**What**: Detect `x:(a|b|c|d|e|f|g|h|...)` with many alternatives
**Why**: Backtracking explosion

### Pattern Matching On Large Lists
**Type**: Performance
**What**: Detect pattern matching on lists with thousands of elements
**Why**: Use other approaches (Select, Pick)

---

## List/Array Rules (10 rules)

### Empty List Indexing
**Type**: Bug
**What**: Detect `list[[1]]` without checking `Length[list] > 0`
**Why**: Runtime error

### Negative Index Without Validation
**Type**: Bug
**What**: Detect `list[[-n]]` without checking `n <= Length[list]`
**Why**: Runtime error

### Part Assignment To Immutable
**Type**: Bug
**What**: Detect `list[[i]] = x` where list isn't a variable
**Why**: Doesn't modify original

### Inefficient List Concatenation
**Type**: Performance
**What**: Detect `Join[list, {x}]` in loop
**Why**: Quadratic complexity
**Example**: Use `AppendTo` or pre-allocate

### Unnecessary Flatten
**Type**: Performance
**What**: Detect `Flatten[{a, b, c}]` where already flat
**Why**: Wasted computation

### Length In Loop Condition
**Type**: Performance
**What**: Detect `Do[..., {i, 1, Length[list]}]` recalculating length
**Why**: Cache length value

### Reverse Twice
**Type**: Code Smell
**What**: Detect `Reverse[Reverse[x]]`
**Why**: No-op

### Sort Without Comparison
**Type**: Performance
**What**: Detect `Sort[list, Greater]` when could use `Reverse[Sort[list]]`
**Why**: Built-in sort is optimized

### Position vs Select
**Type**: Performance
**What**: Detect `Extract[list, Position[list, pattern]]` → use `Select`
**Why**: Cleaner and faster

### Nested Part Extraction
**Type**: Code Smell
**What**: Detect `list[[i]][[j]]` → use `list[[i, j]]`
**Why**: Cleaner syntax

---

## Association Rules (10 rules)

### Missing Key Check
**Type**: Bug
**What**: Detect `assoc["key"]` without checking `KeyExistsQ`
**Why**: Returns `Missing["KeyAbsent", "key"]`

### Association vs List Confusion
**Type**: Bug
**What**: Detect list operations on associations
**Example**: `Length[assoc]` (correct) vs `assoc[[1]]` (wrong)

### Inefficient Key Lookup
**Type**: Performance
**What**: Detect `Select[Keys[assoc], pred]` → use `KeySelect`
**Why**: Built-in is optimized

### Query On Non-Dataset
**Type**: Bug
**What**: Detect `Query[...]` on regular list
**Why**: Needs Dataset wrapper

### Association Update Pattern
**Type**: Code Smell
**What**: Detect `assoc["key"] = value` → use `AssociateTo` or `Append`
**Why**: Cleaner semantics

### Merge Without Conflict Strategy
**Type**: Bug
**What**: Detect `Merge[{a1, a2}]` without specifying merge function
**Why**: Default may not be what you want

### AssociateTo On Non-Symbol
**Type**: Bug
**What**: Detect `AssociateTo[expr, ...]` where expr isn't a symbol
**Why**: Won't modify original

### KeyDrop Multiple Times
**Type**: Performance
**What**: Detect `KeyDrop[KeyDrop[assoc, "a"], "b"]` → `KeyDrop[assoc, {"a", "b"}]`
**Why**: Single pass

### Lookup With Missing Default
**Type**: Code Smell
**What**: Detect `Lookup[assoc, key, Missing[]]` (redundant)
**Why**: Missing is default

### Group By Without Aggregation
**Type**: Code Smell
**What**: Detect `GroupBy` where `GatherBy` more appropriate
**Why**: Semantic clarity

---

## Unused Code Detection (15 rules)

### Unused Function (Private)
**Type**: Code Smell
**What**: Detect private functions never called
**Why**: Dead code

### Unused Module Variable
**Type**: Code Smell
**What**: Detect `Module[{x, y}, ...]` where y never used
**Why**: Clutter

### Unused With Variable
**Type**: Code Smell
**What**: Similar to Module but for With
**Why**: With variables are constants, unused is clearer mistake

### Unused Pattern Name
**Type**: Code Smell
**What**: Detect `f[x_, y_] := x` (y not used)
**Why**: Should be `f[x_, _] := x`

### Unused Optional Parameter
**Type**: Code Smell
**What**: Detect optional param never used even when provided
**Why**: Confusing API

### Dead After Return
**Type**: Bug
**What**: Detect code after `Return[]` in same scope
**Why**: Never executes
**Severity**: Major

### Unreachable After Abort/Throw
**Type**: Bug
**What**: Detect code after `Abort[]` or `Throw[]`
**Why**: Never executes

### Assignment Never Read
**Type**: Code Smell
**What**: Detect `x = value` where x never read before next assignment
**Why**: Useless work

### Function Defined But Never Called
**Type**: Code Smell
**What**: Global-scope function never called
**Why**: Dead code (may be API)

### Redefined Without Use
**Type**: Bug
**What**: Detect `x = 1; x = 2` where first value never used
**Why**: Logic error

### Loop Variable Unused
**Type**: Code Smell
**What**: Detect `Do[..., {i, 1, 10}]` where i never used in body
**Why**: Use `Do[..., 10]` form

### Catch Without Throw
**Type**: Code Smell
**What**: Detect `Catch[...]` where no `Throw` in body
**Why**: Unnecessary

### Condition Always False
**Type**: Bug
**What**: Detect `If[False, ...]` or pattern `/; False`
**Why**: Dead code or logic error

### Unreachable Code After Return
**Type**: Bug
**What**: Detect code after `Return[]` that can never execute
**Why**: Dead code

### Unreachable Branch
**Type**: Bug
**What**: Detect `If[True, a, b]` where b is unreachable
**Why**: Dead code or logic error

---

## Shadowing & Naming (15 rules)

### Local Shadows Global
**Type**: Code Smell
**What**: Detect `Module[{x}, ...]` when x is global variable
**Why**: Confusing, may be unintended

### Parameter Shadows Built-in
**Type**: Bug
**What**: Detect `f[List_] := ...` (shadows built-in)
**Why**: Won't work as expected
**Severity**: Major

### Local Variable Shadows Parameter
**Type**: Bug
**What**: Detect `f[x_] := Module[{x}, ...]`
**Why**: Confusing, probably error

### Multiple Definitions Same Symbol
**Type**: Code Smell
**What**: Detect function redefined multiple times
**Why**: May be intentional (patterns) or error

### Symbol Name Too Short
**Type**: Code Smell
**What**: Detect single-letter variable names in large functions
**Why**: Readability

### Symbol Name Too Long
**Type**: Code Smell
**What**: Detect variables longer than 50 characters
**Why**: Readability

### Inconsistent Naming Convention
**Type**: Code Smell
**What**: Detect mix of camelCase, snake_case, PascalCase
**Why**: Consistency

### Built-in Name In Local Scope
**Type**: Code Smell
**What**: Detect `Module[{Map, Apply}, ...]` (shadowing built-ins)
**Why**: Confusing

### Context Conflicts
**Type**: Bug
**What**: Detect symbol defined in multiple contexts
**Why**: Ambiguity

### Reserved Name Usage
**Type**: Bug
**What**: Detect use of $SystemID, $Version, etc. as variable names
**Why**: Can cause issues

### Private Context Symbol Public
**Type**: Code Smell
**What**: Detect symbols in `Private` context used outside package
**Why**: Breaks encapsulation

### Mismatched Begin/End
**Type**: Bug
**What**: Detect `BeginPackage` without matching `EndPackage`
**Why**: Context corruption
**Severity**: Critical

### Symbol After EndPackage
**Type**: Bug
**What**: Detect definitions after `EndPackage[]`
**Why**: Wrong context

### Global in Package
**Type**: Code Smell
**What**: Detect use of `Global` context in package code
**Why**: Should use package context

### Temp Variable Not Temp
**Type**: Code Smell
**What**: Detect variables named `temp`, `tmp` used multiple times
**Why**: Should have better names

---

## Type Mismatch Detection (20 rules)

### Numeric Operation On String
**Type**: Bug
**What**: Detect `"hello" + 1` or `"hello"^2`
**Why**: Runtime error or unexpected result
**Severity**: Major

### String Operation On Number
**Type**: Bug
**What**: Detect `StringJoin[123, "abc"]` (wrong arg type)
**Why**: Runtime error

### Wrong Argument Type
**Type**: Bug
**What**: Detect `Map[f, 123]` (expects list)
**Why**: Runtime error

### Function Returns Wrong Type
**Type**: Bug
**What**: Detect function declared to return Integer but returns String
**Why**: Contract violation

### Comparison Of Incompatible Types
**Type**: Bug
**What**: Detect `"hello" < 5` (doesn't error but meaningless)
**Why**: Logic error

### Mixed Numeric Types In Computation
**Type**: Code Smell
**What**: Detect mix of exact and approximate in same calculation
**Why**: Precision loss

### Integer Division Expecting Real
**Type**: Bug
**What**: Detect `1/2` in numeric context (evaluates symbolically)
**Why**: Use `1./2` for numeric

### List Function On Association
**Type**: Bug
**What**: Detect `Append[assoc, elem]` (should use AssociateTo)
**Why**: Different semantics

### Pattern Type Mismatch
**Type**: Bug
**What**: Detect `f[x_Integer] := ...; f["hello"]` call
**Why**: Won't match, returns unevaluated

### Optional Type Inconsistent
**Type**: Bug
**What**: Detect `f[x_Integer:1.5] := ...` (default wrong type)
**Why**: Default should match pattern type

### Return Type Inconsistent
**Type**: Code Smell
**What**: Detect function that sometimes returns Integer, sometimes String
**Why**: Confusing API

### Null Assignment To Typed Variable
**Type**: Bug
**What**: Detect `x = Null` where x expected to be Integer
**Why**: Type violation

### Type Cast Without Validation
**Type**: Bug
**What**: Detect use of `ToExpression[str]` without checking `StringQ`
**Why**: May fail

### Implicit Type Conversion
**Type**: Code Smell
**What**: Detect `ToString[x] <> y` where x already String
**Why**: Redundant

### Graphics Object In Numeric Context
**Type**: Bug
**What**: Detect `Plot[...] + 1`
**Why**: Doesn't make sense

### Symbol In Numeric Context
**Type**: Bug
**What**: Detect `x + 1` where x is symbolic and expected numeric
**Why**: Won't evaluate

### Image Operation On Non-Image
**Type**: Bug
**What**: Detect `ImageData[list]` (expects Image object)
**Why**: Runtime error

### Sound Operation On Non-Sound
**Type**: Bug
**What**: Detect `AudioData[list]` (expects Audio object)
**Why**: Runtime error

### Dataset Operation On List
**Type**: Bug
**What**: Detect Dataset operations on regular lists
**Why**: Needs Dataset wrapper

### Graph Operation On Non-Graph
**Type**: Bug
**What**: Detect `VertexList[list]` (expects Graph)
**Why**: Runtime error

---

## Data Flow Analysis (20 rules)

### Uninitialized Variable Use
**Type**: Bug
**What**: Detect variable used before any definition (data flow based)
**Why**: Runtime error or wrong value
**Severity**: Critical

### Variable May Be Uninitialized
**Type**: Bug
**What**: Detect variable initialized in some branches but not all
**Why**: Logic error

### Dead Store
**Type**: Code Smell
**What**: Detect `x = value` where value never read
**Why**: Useless computation

### Overwritten Before Read
**Type**: Code Smell
**What**: Detect `x = 1; x = 2` where first value never used
**Why**: First assignment wasted

### Variable Aliasing Issue
**Type**: Bug
**What**: Detect when two variables point to same mutable structure
**Why**: Unexpected modifications

### Modification Of Loop Iterator
**Type**: Bug
**What**: Detect `Do[...; i = i+1; ..., {i, 1, 10}]` (modifying iterator)
**Why**: Confusing, may not work as expected

### Use Of Modified Iterator Outside Loop
**Type**: Bug
**What**: Detect use of loop iterator after loop ends (value undefined)
**Why**: Iterator value after loop is implementation detail

### Reading Unset Variable
**Type**: Bug
**What**: Detect read of variable after `Unset[x]` or `Clear[x]`
**Why**: Returns Symbol, not value

### Double Assignment Same Value
**Type**: Code Smell
**What**: Detect `x = 5; ...; x = 5` (same value)
**Why**: Redundant

### Mutation In Pure Function
**Type**: Bug
**What**: Detect mutation of outer variable in pure function
**Why**: Side effect, confusing

### Shared Mutable State
**Type**: Bug
**What**: Detect global mutable state accessed from multiple functions
**Why**: Hard to reason about

### Variable Scope Escape
**Type**: Bug
**What**: Detect `Module[{x}, x]` returning local variable
**Why**: Variable leaks (as Symbol)

### Closure Over Mutable Variable
**Type**: Bug
**What**: Detect pure function capturing variable that changes
**Why**: May not capture expected value

### Assignment In Condition
**Type**: Bug
**What**: Detect `If[x = 5, ...]` (should be `x == 5`)
**Why**: Always true, side effect
**Severity**: Critical

### Assignment As Return Value
**Type**: Code Smell
**What**: Detect `f[x_] := (y = x; y)` (unnecessary variable)
**Why**: Could be `f[x_] := x`

### Variable Never Modified
**Type**: Code Smell
**What**: Detect `Module[{x = 1}, ...; Return[x]]` where x never modified
**Why**: Could use `With` for immutability

### Infinite Loop (Proven)
**Type**: Bug
**What**: Detect loop with no exit condition (CFG-based)
**Why**: Hangs
**Severity**: Critical

### Loop Never Executes
**Type**: Bug
**What**: Detect `While[False, ...]` or `Do[..., {i, 10, 1}]`
**Why**: Dead code

### Code After Abort
**Type**: Bug
**What**: Detect code after `Abort[]`
**Why**: Never executes

### Multiple Returns Make Code Unreachable
**Type**: Code Smell
**What**: Detect early returns leaving later code unreachable
**Why**: Dead code

---

## Security Vulnerabilities (20 rules)

### SQL Injection
**Type**: Vulnerability
**What**: Detect tainted data flowing to `SQLExecute[]`
**Why**: Database compromise
**Severity**: Critical

### Command Injection
**Type**: Vulnerability
**What**: Detect tainted data flowing to `RunProcess[]`, `Run[]`
**Why**: System compromise
**Severity**: Critical

### Code Injection
**Type**: Vulnerability
**What**: Detect tainted data flowing to `ToExpression[]`
**Why**: Arbitrary code execution
**Severity**: Critical

### Path Traversal
**Type**: Vulnerability
**What**: Detect tainted data in file paths: `Import[untrusted]`
**Why**: Unauthorized file access
**Severity**: Critical

### XSS (Cross-Site Scripting)
**Type**: Vulnerability
**What**: Detect tainted data in `XMLElement[]`, `ExportString[..., "HTML"]`
**Why**: Client-side code injection
**Severity**: Critical

### LDAP Injection
**Type**: Vulnerability
**What**: Detect tainted data in LDAP queries
**Why**: Authentication bypass
**Severity**: Critical

### XML External Entity (XXE)
**Type**: Vulnerability
**What**: Detect unsafe XML parsing with external entities enabled
**Why**: Information disclosure
**Severity**: Critical

### Unsafe Deserialization
**Type**: Vulnerability
**What**: Detect `Import[untrusted, "MX"]` or `Get[untrusted]`
**Why**: Arbitrary code execution
**Severity**: Critical

### Server-Side Request Forgery (SSRF)
**Type**: Vulnerability
**What**: Detect tainted URLs in `URLFetch[]`, `URLExecute[]`
**Why**: Internal network access
**Severity**: Critical

### Insecure Randomness
**Type**: Vulnerability
**What**: Detect `RandomInteger[]` used for security-sensitive values
**Why**: Predictable
**Severity**: Major

### Weak Cryptography
**Type**: Vulnerability
**What**: Detect use of MD5, SHA1 for security purposes
**Why**: Broken algorithms
**Severity**: Major

### Hard-Coded Credentials
**Type**: Vulnerability
**What**: Detect string literals flowing to authentication
**Why**: Credential leak
**Severity**: Critical

### Sensitive Data In Logs
**Type**: Vulnerability
**What**: Detect tainted data from authentication flowing to `Print[]`, logging
**Why**: Information disclosure
**Severity**: Major

### Mass Assignment
**Type**: Vulnerability
**What**: Detect tainted association directly used in database update
**Why**: Privilege escalation
**Severity**: Critical

### Regex DoS
**Type**: Vulnerability
**What**: Detect tainted data in regex patterns (catastrophic backtracking)
**Why**: Denial of service
**Severity**: Major

### Missing Default Case
**Type**: Bug
**What**: Detect `Switch` without default case
**Why**: May return unevaluated

### Empty If Branch
**Type**: Code Smell
**What**: Detect `If[cond, , else]` (empty true branch)
**Why**: Confusing, use `If[!cond, else]`

### Nested If Depth
**Type**: Code Smell
**What**: Detect deeply nested If statements (>4 levels)
**Why**: Complexity

### Too Many Return Points
**Type**: Code Smell
**What**: Detect function with >5 return statements
**Why**: Hard to reason about

### Missing Else Considered Harmful
**Type**: Code Smell
**What**: Detect `If[cond, action]` that should have else
**Why**: Unclear intent

---

## Cross-File & Architecture (20 rules)

### Circular Package Dependency
**Type**: Bug
**What**: Detect A.m needs B.m needs A.m
**Why**: Load order issues
**Severity**: Critical

### Unused Package Import
**Type**: Code Smell
**What**: Detect `Needs["Package`"]` where no symbols from Package used
**Why**: Unnecessary dependency

### Missing Package Import
**Type**: Bug
**What**: Detect use of symbol from package without Needs
**Why**: May fail in fresh kernel
**Severity**: Major

### Transitive Dependency Could Be Direct
**Type**: Code Smell
**What**: Detect reliance on transitive import
**Why**: Fragile

### Diamond Dependency
**Type**: Code Smell
**What**: Detect A depends on B and C, both depend on D
**Why**: Version conflicts

### God Package
**Type**: Code Smell
**What**: Detect package that depends on >10 other packages
**Why**: High coupling

### Package Depends On Application Code
**Type**: Bug
**What**: Detect library package depending on application-specific code
**Why**: Wrong dependency direction

### Cyclic Call Between Packages
**Type**: Code Smell
**What**: Detect PackageA calls PackageB calls PackageA
**Why**: Tight coupling

### Layer Violation
**Type**: Code Smell
**What**: Detect lower layer depending on higher layer
**Why**: Architectural violation

### Unstable Dependency
**Type**: Code Smell
**What**: Detect stable package depending on unstable package
**Why**: Ripple effects

### Package Too Large
**Type**: Code Smell
**What**: Detect package with >3000 lines
**Why**: Should be split

### Package Too Small
**Type**: Code Smell
**What**: Detect package with <50 lines
**Why**: Over-modularization

### Inconsistent Package Naming
**Type**: Code Smell
**What**: Detect package names not following convention
**Why**: Consistency

### Package Exports Too Much
**Type**: Code Smell
**What**: Detect package with >50 public symbols
**Why**: Poor cohesion

### Package Exports Too Little
**Type**: Code Smell
**What**: Detect package with 0-1 public symbols
**Why**: Questionable design

### Incomplete Public API
**Type**: Code Smell
**What**: Detect public function relying on private function
**Why**: Breaks encapsulation

### Private Symbol Used Externally
**Type**: Bug
**What**: Detect symbol in Private` context used from another package
**Why**: Breaks encapsulation

### Internal Implementation Exposed
**Type**: Code Smell
**What**: Detect "Internal`" symbols used from outside
**Why**: Unstable API

### Missing Package Documentation
**Type**: Code Smell
**What**: Detect package without top-level usage message
**Why**: Discoverability

### Public API Changed Without Version Bump
**Type**: Code Smell
**What**: Detect breaking changes to public symbols
**Why**: Semantic versioning

---

## Null Safety & Error Handling (15 rules)

### Null Dereference
**Type**: Bug
**What**: Detect operations on potentially null values
**Why**: Runtime error
**Severity**: Major

### Missing Null Check
**Type**: Bug
**What**: Detect function result used without checking for $Failed
**Why**: Error propagation

### Null Return Not Documented
**Type**: Code Smell
**What**: Detect function that can return Null without documenting it
**Why**: API surprise

### Comparison With Null
**Type**: Bug
**What**: Detect `x == Null` (should use `x === Null`)
**Why**: Semantic difference

### Missing Check Leads To Null Propagation
**Type**: Bug
**What**: Detect chain where null propagates through multiple operations
**Why**: Error cascade

### Check Pattern Doesn't Handle All Cases
**Type**: Bug
**What**: Detect `Check[expr, fallback]` missing some error cases
**Why**: Incomplete error handling

### Quiet Suppressing Important Messages
**Type**: Bug
**What**: Detect `Quiet[...]` suppressing critical messages
**Why**: Masks real errors

### Off Disabling Important Warnings
**Type**: Bug
**What**: Detect `Off[General::...]` for important warnings
**Why**: Masks problems

### Catch-All Exception Handler
**Type**: Bug
**What**: Detect `Catch[expr]` catching all tags
**Why**: Too broad

### Empty Exception Handler
**Type**: Bug
**What**: Detect `Catch[expr, _, Null &]` (silently ignoring errors)
**Why**: Error information lost

### Throw Without Catch
**Type**: Bug
**What**: Detect `Throw[...]` with no surrounding Catch
**Why**: Will abort evaluation

### Abort In Library Code
**Type**: Bug
**What**: Detect `Abort[]` in public library function
**Why**: Too aggressive, should return error

### Message Without Definition
**Type**: Bug
**What**: Detect message issued but not defined: `Message[f::undefined]`
**Why**: Generic message shown

### Missing Message Definition
**Type**: Code Smell
**What**: Detect function issuing messages without defining them first
**Why**: Unclear error messages

### Break Outside Loop
**Type**: Bug
**What**: Detect `Break[]` outside loop context
**Why**: Runtime error
**Severity**: Critical

---

## Constant & Expression Analysis (15 rules)

### Condition Always True (Constant Propagation)
**Type**: Bug
**What**: Detect `If[True, ...]` from constant propagation
**Why**: Dead branch or logic error

### Condition Always False (Constant Propagation)
**Type**: Bug
**What**: Detect `If[False, ...]` from constant propagation
**Why**: Dead branch or logic error

### Loop Bound Constant
**Type**: Code Smell
**What**: Detect `Do[..., {i, 1, x}]` where x is constant
**Why**: Use literal value

### Redundant Computation
**Type**: Performance
**What**: Detect same expression computed multiple times with same inputs
**Why**: Could cache result

### Pure Expression In Loop
**Type**: Performance
**What**: Detect pure (side-effect-free) expression computed in every iteration
**Why**: Hoist outside loop

### Constant Expression
**Type**: Code Smell
**What**: Detect `x + 0`, `x * 1`, `x^1`
**Why**: Simplify to `x`

### Identity Operation
**Type**: Code Smell
**What**: Detect `Reverse[Reverse[x]]`, `Transpose[Transpose[x]]`
**Why**: No-op

### Comparison Of Identical Expressions
**Type**: Bug
**What**: Detect `x == x` (always true)
**Why**: Logic error or typo

### Boolean Expression Always True
**Type**: Bug
**What**: Detect `x || !x` (tautology)
**Why**: Logic error

### Boolean Expression Always False
**Type**: Bug
**What**: Detect `x && !x` (contradiction)
**Why**: Logic error

### Unnecessary Boolean Conversion
**Type**: Code Smell
**What**: Detect `If[cond, True, False]` (should just be `cond`)
**Why**: Redundant

### Double Negation
**Type**: Code Smell
**What**: Detect `!!x` or `Not[Not[x]]`
**Why**: Simplify to `x`

### Complex Boolean Expression
**Type**: Code Smell
**What**: Detect boolean expression with >5 operators
**Why**: Hard to understand

### De Morgan's Law Opportunity
**Type**: Code Smell
**What**: Detect `!(a && b)` (could be `!a || !b`)
**Why**: Clarity

### Part Specification Out Of Bounds
**Type**: Bug
**What**: Detect `list[[100]]` where list has <100 elements
**Why**: Runtime error
**Severity**: Critical

---

## Mathematica-Specific Patterns (20 rules)

### Hold Attribute Missing
**Type**: Bug
**What**: Detect function manipulating unevaluated expressions without Hold attribute
**Why**: Arguments will evaluate

### HoldFirst But Uses Second Argument First
**Type**: Bug
**What**: Detect function with HoldFirst that evaluates second arg first
**Why**: Unexpected evaluation order

### Missing Unevaluated Wrapper
**Type**: Bug
**What**: Detect passing expression to held parameter without Unevaluated
**Why**: Premature evaluation

### Unnecessary Hold
**Type**: Code Smell
**What**: Detect `Hold[literal]` where literal doesn't evaluate
**Why**: Redundant

### ReleaseHold After Hold
**Type**: Code Smell
**What**: Detect `ReleaseHold[Hold[x]]`
**Why**: Redundant

### Evaluate In Held Context
**Type**: Bug
**What**: Detect `Hold[..., Evaluate[x], ...]` (evaluation leak)
**Why**: May not be intended

### Pattern With Side Effect
**Type**: Bug
**What**: Detect pattern test with side effects: `x_?(Print[#]; True &)`
**Why**: Evaluated multiple times

### Replacement Rule Order Matters
**Type**: Bug
**What**: Detect replacement rules where order affects result
**Why**: Non-deterministic behavior

### ReplaceAll vs Replace Confusion
**Type**: Bug
**What**: Detect `/. list` where `Replace[..., list, {1}]` intended
**Why**: Different semantics

### Rule Doesn't Match Due To Evaluation
**Type**: Bug
**What**: Detect rule that won't match due to evaluation timing
**Why**: Subtle bug

### Span Specification Invalid
**Type**: Bug
**What**: Detect `list[[10;;1]]` (backwards span)
**Why**: Empty result

### All Specification Inefficient
**Type**: Performance
**What**: Detect `list[[All]]` (redundant, just use `list`)
**Why**: Unnecessary operation

### Threading Over Non-Lists
**Type**: Bug
**What**: Detect function with Listable attribute called on non-list
**Why**: Unexpected result

### Missing Attributes Declaration
**Type**: Code Smell
**What**: Detect function that should have Listable but doesn't
**Why**: Performance opportunity

### OneIdentity Attribute Misuse
**Type**: Bug
**What**: Detect OneIdentity on function where it causes issues
**Why**: Subtle semantic change

### Orderless Attribute On Non-Commutative
**Type**: Bug
**What**: Detect Orderless on non-commutative operation
**Why**: Wrong semantics
**Severity**: Critical

### Flat Attribute Misuse
**Type**: Bug
**What**: Detect Flat on non-associative operation
**Why**: Wrong semantics
**Severity**: Critical

### Sequence In Unexpected Context
**Type**: Bug
**What**: Detect `Sequence[...]` flattening where not intended
**Why**: Lost structure

### Missing Sequence Wrapper
**Type**: Bug
**What**: Detect place where `Sequence[]` should be used but isn't
**Why**: Extra nesting

### Impossible Pattern
**Type**: Bug
**What**: Detect pattern that can never match
**Why**: Dead code

---

## Performance Analysis (10 rules)

### Compilable Function Not Compiled
**Type**: Performance
**What**: Suggest `Compile[]` for suitable functions
**Why**: 10-100x speedup potential

### Compilation Target Missing
**Type**: Performance
**What**: Detect `Compile[..., CompilationTarget -> "MVM"]` (should be "C")
**Why**: C compilation much faster

### Non-Compilable Construct In Compile
**Type**: Bug
**What**: Detect use of non-compilable functions in `Compile[]`
**Why**: Falls back to slow evaluation

### Packed Array Unpacked
**Type**: Performance
**What**: Detect operations that unpack packed arrays
**Why**: 10-100x slower
**Severity**: Major

### Inefficient Pattern In Performance Critical Code
**Type**: Performance
**What**: Detect complex patterns in hot loops
**Why**: Pattern matching is slow

### N Applied Too Late
**Type**: Performance
**What**: Detect symbolic computation followed by `N[]`
**Why**: Do numeric from start

### Missing Memoization Opportunity
**Type**: Performance
**What**: Detect recursive function without memoization
**Why**: Exponential time complexity

### Inefficient String Concatenation
**Type**: Performance
**What**: Detect repeated `<>` in loop
**Why**: Quadratic complexity

### List Concatenation In Loop
**Type**: Performance
**What**: Detect `Join[list, {x}]` in loop
**Why**: Quadratic complexity

### Undefined Function Call
**Type**: Bug
**What**: Detect call to function not defined or imported
**Why**: Runtime error
**Severity**: Critical

---

## Summary

**Total Rule Ideas**: 300+

**By Category**:
- Pattern System: 15 rules
- List/Array: 10 rules
- Association: 10 rules
- Unused Code: 15 rules
- Shadowing & Naming: 15 rules
- Type Mismatch: 20 rules
- Data Flow: 20 rules
- Security: 20 rules
- Cross-File/Architecture: 20 rules
- Null Safety: 15 rules
- Constant/Expression: 15 rules
- Mathematica-Specific: 20 rules
- Performance: 10 rules
- Additional semantic rules: 95+

**By Severity**:
- Critical: ~50 rules
- Major: ~100 rules
- Minor/Code Smell: ~150 rules

**Implementation Notes**:
- Many of these rules require the infrastructure that's already in place (AST, Symbol Table, Type System, Data Flow, CFG)
- Rules should be prioritized based on:
  1. User impact (how many bugs will this catch?)
  2. False positive rate (will users trust this rule?)
  3. Implementation complexity
  4. Performance impact on scan time

**Next Steps**:
See ROADMAP.md for implementation priorities and phases.
