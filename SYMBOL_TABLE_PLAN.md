# Enhanced Symbol Table Implementation Plan

**Goal:** Build sophisticated symbol table for variable lifetime tracking, scope analysis, and dead store elimination

**Duration:** 4-6 weeks
**Target:** 15-20 new rules
**Builds on:** Chunk 5's cross-file analysis infrastructure

---

## Architecture Overview

### What We're Building

```
┌─────────────────────────────────────────────────────────────┐
│                    Symbol Table System                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────┐      ┌──────────────────┐              │
│  │ Symbol Table   │      │ Scope Manager    │              │
│  │ - Variables    │◄────►│ - Module scopes  │              │
│  │ - Assignments  │      │ - Block scopes   │              │
│  │ - References   │      │ - With scopes    │              │
│  │ - Lifetimes    │      │ - Global scope   │              │
│  └────────────────┘      └──────────────────┘              │
│         ▲                         ▲                         │
│         │                         │                         │
│  ┌──────┴─────────────────────────┴──────┐                 │
│  │     Variable Lifetime Analyzer         │                 │
│  │  - Definitions                         │                 │
│  │  - Uses                                │                 │
│  │  - Dead stores                         │                 │
│  │  - Scope violations                    │                 │
│  └────────────────────────────────────────┘                 │
│         ▲                                                    │
│         │                                                    │
│  ┌──────┴──────────────────────────────────┐               │
│  │    New Rule Detectors (15-20 rules)     │               │
│  └──────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

### Core Components

1. **SymbolTable** - Stores variable information per file
2. **Scope** - Represents Module/Block/With/Global scopes
3. **Symbol** - Represents a variable with:
   - Name
   - Declaration location
   - All assignments
   - All references
   - Scope
   - Type hints (if available)
4. **LifetimeAnalyzer** - Analyzes variable usage patterns
5. **DeadStoreDetector** - Finds assignments that are never read

---

## Week-by-Week Plan

### Week 1: Core Symbol Table Infrastructure (Days 1-7)

**Goal:** Build foundation classes and basic tracking

#### Day 1-2: Create Core Classes
- [ ] Create `Symbol.java` - represents a single variable
  ```java
  class Symbol {
    String name;
    int declarationLine;
    Scope scope;
    List<SymbolReference> assignments;
    List<SymbolReference> references;
    boolean isParameter;
    boolean isModuleVariable;
  }
  ```
- [ ] Create `SymbolReference.java` - represents use/assignment
  ```java
  class SymbolReference {
    int line;
    int column;
    ReferenceType type; // READ, WRITE, READ_WRITE
    String context; // surrounding code
  }
  ```
- [ ] Create `Scope.java` - represents a lexical scope
  ```java
  class Scope {
    ScopeType type; // MODULE, BLOCK, WITH, GLOBAL, FUNCTION
    int startLine;
    int endLine;
    Scope parent;
    Map<String, Symbol> symbols;
    List<Scope> children;
  }
  ```

#### Day 3-4: Build Symbol Table Manager
- [ ] Create `SymbolTableManager.java`
  - Manages symbol tables per file
  - Provides lookup methods
  - Handles scope hierarchy
  - Thread-safe for parallel processing

#### Day 5-7: Basic Variable Tracking
- [ ] Implement simple variable detection
  - Pattern: `var = value` (assignments)
  - Pattern: `{var1, var2, ...}` (declarations)
  - Pattern: `var` (references)
- [ ] Build initial scope detection
  - Module[{vars}, body]
  - Block[{vars}, body]
  - Function parameters
- [ ] Write unit tests
  - Test symbol creation
  - Test scope hierarchy
  - Test basic tracking

**Deliverable:** Core classes + basic variable tracking working

**Test file:**
```mathematica
(* test-symbols-week1.m *)
Module[{x, y},
  x = 5;
  y = 10;
  Print[x + y]
]
```
Expected: Track x, y as Module variables with correct line numbers

---

### Week 2: Scope Tracking & Hierarchy (Days 8-14)

**Goal:** Accurate scope detection and nesting

#### Day 8-10: Enhanced Scope Detection
- [ ] Parse Module constructs accurately
  ```java
  Pattern MODULE = Pattern.compile(
    "Module\\s*\\[\\s*\\{([^}]+)\\}\\s*,(.+)\\]",
    Pattern.DOTALL
  );
  ```
- [ ] Parse Block constructs
- [ ] Parse With constructs
- [ ] Handle nested scopes
  ```mathematica
  Module[{x},
    x = 5;
    Block[{y},  (* nested scope *)
      y = x + 1;
      Print[y]
    ]
  ]
  ```

#### Day 11-12: Scope Hierarchy Management
- [ ] Build parent/child scope relationships
- [ ] Implement scope lookup (check current, then parent, then global)
- [ ] Track variable shadowing
  ```mathematica
  x = 1;  (* global *)
  Module[{x},  (* shadows global x *)
    x = 2;
    Print[x]  (* uses Module's x *)
  ]
  ```

#### Day 13-14: Function Parameter Scoping
- [ ] Detect function definitions: `f[x_] := ...`
- [ ] Track parameters as symbols in function scope
- [ ] Handle pattern variables: `f[x_Integer] := ...`
- [ ] Write comprehensive tests

**Deliverable:** Full scope hierarchy with nesting support

**Test file:**
```mathematica
(* test-scopes-week2.m *)
x = 1;  (* global *)
Module[{x, y},  (* Module scope *)
  x = 2;
  y = 3;
  Block[{z},  (* nested Block scope *)
    z = x + y;
    Print[z]
  ]
]
```
Expected: 3 scopes (global, Module, Block), correct symbol tracking in each

---

### Week 3: Variable Lifetime Analysis (Days 15-21)

**Goal:** Track variable usage patterns and identify dead stores

#### Day 15-17: Definition-Use Chain Analysis
- [ ] Build definition-use (def-use) chains
  - Track where each variable is defined
  - Track all locations where it's used
  - Identify if definition is ever used
- [ ] Implement simple data flow
  ```mathematica
  x = 5;      (* def 1 *)
  x = 10;     (* def 2 - kills def 1 *)
  Print[x]    (* use of def 2 *)
  ```

#### Day 18-19: Dead Store Detection
- [ ] Detect assignments never read:
  ```mathematica
  x = 5;      (* Dead - immediately reassigned *)
  x = 10;
  Print[x]
  ```
- [ ] Detect assignments before return:
  ```mathematica
  f[] := Module[{x},
    x = 5;
    x = 10;   (* Dead - function returns before use *)
    Return[42]
  ]
  ```
- [ ] Handle conditional dead stores:
  ```mathematica
  If[condition,
    x = 5,    (* Dead if condition false *)
    x = 10
  ];
  Print[x]
  ```

#### Day 20-21: Variable Never Used Detection
- [ ] Detect variables declared but never referenced:
  ```mathematica
  Module[{x, y, z},
    x = 5;
    y = 10;
    Print[x + y]  (* z never used! *)
  ]
  ```
- [ ] Distinguish from intentionally unused (like `_` pattern)
- [ ] Write comprehensive tests

**Deliverable:** Working dead store detection + unused variable detection

**Test file:**
```mathematica
(* test-lifetime-week3.m *)
Module[{x, y, unused},
  x = 5;      (* Dead store *)
  x = 10;     (* Used *)
  y = 20;     (* Dead store *)
  y = 30;     (* Dead store *)
  Print[x]    (* unused and y are issues *)
]
```
Expected: Report dead stores for x=5, y=20, y=30; report unused variable

---

### Week 4: Integration & New Rules (Days 22-28)

**Goal:** Wire into sensor and implement first batch of rules

#### Day 22-23: Sensor Integration
- [ ] Create `SymbolTableDetector.java`
- [ ] Integrate with `MathematicaRulesSensor.java`
- [ ] Add two-phase analysis:
  - Phase 1: Build symbol tables
  - Phase 2: Run analysis on completed tables
- [ ] Test with real codebase

#### Day 24-28: Implement First 10 Rules

##### Rule 1: Dead Store (Variable Reassigned Before Use)
```java
public static void detectDeadStore(SensorContext ctx,
    InputFile file, SymbolTable symbolTable) {
  for (Symbol symbol : symbolTable.getAllSymbols()) {
    for (int i = 0; i < symbol.assignments.size() - 1; i++) {
      SymbolReference assignment = symbol.assignments.get(i);
      SymbolReference nextAssignment = symbol.assignments.get(i + 1);

      // Check if any uses between assignments
      boolean hasUseBetween = symbol.references.stream()
        .anyMatch(ref -> ref.line > assignment.line &&
                        ref.line < nextAssignment.line &&
                        ref.type != ReferenceType.WRITE);

      if (!hasUseBetween) {
        createIssue(ctx, file, "DeadStore", assignment.line,
          "Value assigned to '" + symbol.name +
          "' is never used before reassignment");
      }
    }
  }
}
```

##### Rule 2: Variable Declared But Never Used
##### Rule 3: Variable Used Before Assignment
##### Rule 4: Parameter Shadows Outer Variable
##### Rule 5: Local Variable Shadows Parameter
##### Rule 6: Assignment to Parameter (Suspicious)
##### Rule 7: Variable Escapes Scope
##### Rule 8: Variable Only Used in Dead Code
##### Rule 9: Multiple Assignments Same Value
##### Rule 10: Variable Read But Never Written

**Deliverable:** 10 working rules integrated and tested

---

### Week 5-6: Additional Rules & Polish (Days 29-42)

**Goal:** Implement remaining rules, optimize, and document

#### Week 5 (Days 29-35): Rules 11-15

##### Rule 11: Unused Function Parameter
- Detect parameters never referenced in function body
```mathematica
f[x_, y_, z_] := x + y  (* z never used *)
```

##### Rule 12: Variable Assigned But Never Read (Enhanced)
- More sophisticated than Rule 2
- Considers all code paths

##### Rule 13: Assignment in Condition (Suspicious)
```mathematica
If[x = 5, ...]  (* Should be x == 5? *)
```

##### Rule 14: Variable Leaks Through SetDelayed
```mathematica
Module[{x},
  x = 5;
  f[] := x  (* x captured, will fail outside Module *)
]
```

##### Rule 15: Redefining Imported Symbol
```mathematica
Needs["Package`"];
PackageFunction = ...  (* Shadowing imported symbol *)
```

#### Week 6 (Days 36-42): Final Rules & Optimization

##### Rules 16-20: Advanced Patterns
- Rule 16: Variable modified in pure function
- Rule 17: Global variable modified in Module
- Rule 18: Loop variable modified inside loop body
- Rule 19: Scope violation in nested structures
- Rule 20: Symbol table consistency checks

#### Performance Optimization
- [ ] Profile symbol table building
- [ ] Optimize lookups (use HashMap)
- [ ] Cache scope hierarchy
- [ ] Parallel processing where possible

#### Documentation
- [ ] Update README with new rules
- [ ] Create SYMBOL_TABLE.md explaining architecture
- [ ] Add examples for each rule
- [ ] Document performance characteristics

**Deliverable:** 20 rules total, optimized, documented

---

## Success Criteria

### Week 1 ✅
- Core classes compile
- Basic variable tracking works
- Unit tests pass

### Week 2 ✅
- Scope hierarchy builds correctly
- Nested scopes work
- Shadowing detected

### Week 3 ✅
- Dead stores identified
- Unused variables found
- Lifetime analysis accurate

### Week 4 ✅
- 10 rules working in SonarQube
- Integration with sensor complete
- Real codebase tested

### Week 5-6 ✅
- 20 rules total implemented
- Performance acceptable (<2x current scan time)
- Documentation complete

---

## Risk Mitigation

### Risk 1: Complex Nested Scopes
**Mitigation:** Start simple (flat scopes), add nesting incrementally

### Risk 2: Performance Degradation
**Mitigation:** Profile early (Week 2), optimize as we go

### Risk 3: False Positives
**Mitigation:** Conservative detection, extensive testing on real code

### Risk 4: Scope Creep
**Mitigation:** Strict focus on 20 rules, defer other features

---

## Testing Strategy

### Unit Tests (Throughout)
- Test each component in isolation
- Mock dependencies
- Fast execution (<1 second total)

### Integration Tests (Week 4+)
- Test full pipeline
- Use real Mathematica files
- Verify issues created correctly

### Real Codebase Testing (Week 4+)
- Run on SLL codebase (12,368 files)
- Verify no crashes
- Check performance
- Review sample of detected issues for accuracy

---

## Deliverables Summary

1. **Code:**
   - `Symbol.java` (~100 lines)
   - `SymbolReference.java` (~50 lines)
   - `Scope.java` (~150 lines)
   - `SymbolTable.java` (~200 lines)
   - `SymbolTableManager.java` (~250 lines)
   - `LifetimeAnalyzer.java` (~300 lines)
   - `SymbolTableDetector.java` (~1,000 lines with 20 detection methods)

2. **Documentation:**
   - `SYMBOL_TABLE.md` - Architecture guide
   - Updated `README.md` - New rules documented
   - Updated `ROADMAP_325.md` - Progress marked

3. **Tests:**
   - 50+ unit tests
   - 20+ integration tests
   - Real codebase validation

---

## Next Steps

1. **Review this plan** - Any changes needed?
2. **Start Week 1** - Create core classes
3. **Daily check-ins** - Report progress, blockers
4. **Iterate** - Adjust plan based on findings

**Ready to start Week 1?** 🚀
