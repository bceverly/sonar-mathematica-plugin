package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ANALYSIS_TIMEOUT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ASSIGNED_BUT_NEVER_READ_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CIRCULAR_VARIABLE_DEPENDENCIES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMPILABLE_FUNCTION_NOT_COMPILED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMPILATION_TARGET_MISSING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONSTANT_NOT_MARKED_AS_CONSTANT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DEAD_STORE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FILE_EXCEEDS_ANALYSIS_LIMIT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GLOBAL_VARIABLE_POLLUTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCORRECT_CLOSURE_CAPTURE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INEFFICIENT_PATTERN_IN_PERFORMANCE_CRITICAL_CODE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INEFFICIENT_STRING_CONCATENATION_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LIFETIME_EXTENDS_BEYOND_SCOPE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LIST_CONCATENATION_IN_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LOW_TEST_COVERAGE_WARNING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_MEMOIZATION_OPPORTUNITY_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MODIFIED_IN_UNEXPECTED_SCOPE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NAMING_CONVENTION_VIOLATIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NON_COMPILABLE_CONSTRUCT_IN_COMPILE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.N_APPLIED_TOO_LATE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKED_ARRAY_UNPACKED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REDUNDANT_ASSIGNMENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SCOPE_LEAK_THROUGH_DYNAMIC_EVALUATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_CRITICAL;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MAJOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MINOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_DEAD_CODE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PATTERNS;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PERFORMANCE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_READABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_SECURITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_UNUSED;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TEST_ONLY_CODE_IN_PRODUCTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_15MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TYPE_INCONSISTENCY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNTESTED_BRANCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNTESTED_PUBLIC_FUNCTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_PARAMETER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_VARIABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.USED_BEFORE_ASSIGNMENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_ESCAPES_SCOPE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_IN_WRONG_SCOPE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_REUSE_WITH_DIFFERENT_SEMANTICS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_SHADOWING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.WRITE_ONLY_VARIABLE_KEY;

/**
 * Chunk7 And SymbolTable Rules definitions.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class SymbolTableAndTestingRulesDefinition {

    private SymbolTableAndTestingRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineChunk7RuleDefinitions(repository);
        defineSymbolTableAnalysisRules(repository);
        defineSymbolTableAnalysisRules2(repository);
        definePerformanceLimits(repository);
    }

    /**
     * CHUNK 7 RULE DEFINITIONS (Items 307-310, 312-320) (13 rules)
     */
    private static void defineChunk7RuleDefinitions(NewRepository repository) {
        // ===== CHUNK 7 RULE DEFINITIONS (Items 307-310, 312-320) =====

        // Test Coverage Integration (Items 307-310)

        NewRule rule361 = repository.createRule(LOW_TEST_COVERAGE_WARNING_KEY)
            .setName("File has low test coverage")
            .setHtmlDescription(
                "<p>Files should have at least 80% line coverage.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>File with <80% coverage</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Add tests to improve coverage</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("testing", "coverage");

            rule361.setDebtRemediationFunction(rule361.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule362 = repository.createRule(UNTESTED_PUBLIC_FUNCTION_KEY)
            .setName("Public function has no tests")
            .setHtmlDescription(
                "<p>All public API functions should have tests.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Public function with no VerificationTest</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Add VerificationTest for the function</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("testing", "coverage");

            rule362.setDebtRemediationFunction(rule362.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule363 = repository.createRule(UNTESTED_BRANCH_KEY)
            .setName("Branch never executed in tests")
            .setHtmlDescription(
                "<p>All If/Which branches should be tested.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If branch with 0% coverage</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Add test for uncovered branch</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("testing", "coverage");

            rule363.setDebtRemediationFunction(rule363.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule364 = repository.createRule(TEST_ONLY_CODE_IN_PRODUCTION_KEY)
            .setName("Code only executed in tests is dead in production")
            .setHtmlDescription(
                "<p>Code paths only hit during tests are dead in production.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Code only executed when testing flag is set</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Remove test-only code or make it test utility</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("testing", TAG_DEAD_CODE);

            rule364.setDebtRemediationFunction(rule364.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        // Performance Analysis (Items 312-320)

        NewRule rule365 = repository.createRule(COMPILABLE_FUNCTION_NOT_COMPILED_KEY)
            .setName("Function suitable for Compile[] is not compiled")
            .setHtmlDescription(
                "<p>Suitable numerical functions should use Compile[] for 10-100x speedup.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>slowFunc[x_] := x^2 + Sin[x]  (* Could compile *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>fastFunc = Compile[{{x, _Real}}, x^2 + Sin[x]]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "compilation");

            rule365.setDebtRemediationFunction(rule365.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule366 = repository.createRule(COMPILATION_TARGET_MISSING_KEY)
            .setName("Compile should target C not MVM")
            .setHtmlDescription(
                "<p>C compilation is much faster than MVM (Mathematica Virtual Machine).</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Compile[{x}, expr, CompilationTarget -> \"MVM\"]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Compile[{x}, expr, CompilationTarget -> \"C\"]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "compilation");

            rule366.setDebtRemediationFunction(rule366.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule367 = repository.createRule(NON_COMPILABLE_CONSTRUCT_IN_COMPILE_KEY)
            .setName("Non-compilable function in Compile[] falls back to slow evaluation")
            .setHtmlDescription(
                "<p>Using non-compilable functions defeats the purpose of Compile[].</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Compile[{x}, Sort[x]]  (* Sort not compilable *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Use only compilable functions</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PERFORMANCE, "compilation");

            rule367.setDebtRemediationFunction(rule367.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule368 = repository.createRule(PACKED_ARRAY_UNPACKED_KEY)
            .setName("Operation unpacks packed array")
            .setHtmlDescription(
                "<p>Unpacking packed arrays causes 10-100x slowdown.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>list[[1]] = x  (* Unpacks array *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Use ReplacePart instead</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "packed-arrays");

            rule368.setDebtRemediationFunction(rule368.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule369 = repository.createRule(INEFFICIENT_PATTERN_IN_PERFORMANCE_CRITICAL_CODE_KEY)
            .setName("Complex pattern matching in hot loop")
            .setHtmlDescription(
                "<p>Pattern matching is slow in performance-critical loops.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[Match[x, complex_pattern], {i, 1, million}]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Use direct comparison or simplify pattern</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, TAG_PATTERNS);

            rule369.setDebtRemediationFunction(rule369.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule370 = repository.createRule(N_APPLIED_TOO_LATE_KEY)
            .setName("N[] applied after symbolic computation")
            .setHtmlDescription(
                "<p>Do numeric computation from the start for better performance.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>N[Integrate[f[x], {x, 0, 1}]]  (* Symbolic first *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>NIntegrate[f[x], {x, 0, 1}]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "numeric");

            rule370.setDebtRemediationFunction(rule370.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule371 = repository.createRule(MISSING_MEMOIZATION_OPPORTUNITY_ENHANCED_KEY)
            .setName("Recursive function without memoization")
            .setHtmlDescription(
                "<p>Recursive functions should use memoization to avoid exponential time.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>fib[n_] := fib[n-1] + fib[n-2]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>fib[n_] := fib[n] = fib[n-1] + fib[n-2]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "memoization");

            rule371.setDebtRemediationFunction(rule371.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule372 = repository.createRule(INEFFICIENT_STRING_CONCATENATION_ENHANCED_KEY)
            .setName("Repeated string concatenation in loop")
            .setHtmlDescription(
                "<p>Repeated <> in loop has quadratic complexity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[str = str <> \"x\", {n}]  (* O(n²) *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>StringJoin[Table[\"x\", {n}]]  (* O(n) *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "strings");

            rule372.setDebtRemediationFunction(rule372.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule373 = repository.createRule(LIST_CONCATENATION_IN_LOOP_KEY)
            .setName("List concatenation in loop has quadratic complexity")
            .setHtmlDescription(
                "<p>Join[list, {x}] in loop has O(n²) complexity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[list = Join[list, {x}], {n}]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Use Sow/Reap or Table instead</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "lists");

            rule373.setDebtRemediationFunction(rule373.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * SYMBOL TABLE ANALYSIS RULES (10 new rules) (Part 1) (9 rules)
     */
    private static void defineSymbolTableAnalysisRules(NewRepository repository) {
        // ===== SYMBOL TABLE ANALYSIS RULES (10 new rules) =====

        NewRule rule374 = repository.createRule(UNUSED_VARIABLE_KEY)
            .setName("Variable declared but never used")
            .setHtmlDescription(
                "<p>Variable is declared but never referenced anywhere in the code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x, unused}, x = 5; Print[x]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{x}, x = 5; Print[x]]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule374.setDebtRemediationFunction(rule374.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule375 = repository.createRule(ASSIGNED_BUT_NEVER_READ_KEY)
            .setName("Variable assigned but value never read")
            .setHtmlDescription(
                "<p>Variable is assigned a value but that value is never used.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5; y = 10; Print[x]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5; Print[x]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule375.setDebtRemediationFunction(rule375.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Note: DEAD_STORE_KEY rule already defined in Chunk 3 section (line ~4297)
        // Reusing existing definition: "Value assigned but never read"

        NewRule rule376 = repository.createRule(USED_BEFORE_ASSIGNMENT_KEY)
            .setName("Variable used before being assigned")
            .setHtmlDescription(
                "<p>Variable is referenced before it has been assigned a value, leading to potential uninitialized use.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Print[x]; x = 5</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5; Print[x]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("uninitialized", "logic-error");

            rule376.setDebtRemediationFunction(rule376.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule377 = repository.createRule(VARIABLE_SHADOWING_KEY)
            .setName("Variable shadows outer scope variable")
            .setHtmlDescription(
                "<p>Inner scope variable has same name as outer scope variable, potentially causing confusion.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 1; Module[{x}, x = 2; Print[x]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 1; Module[{y}, y = 2; Print[y]]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("confusing", "naming");

            rule377.setDebtRemediationFunction(rule377.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule378 = repository.createRule(UNUSED_PARAMETER_KEY)
            .setName("Function parameter is never used")
            .setHtmlDescription(
                "<p>Function parameter is declared but never referenced in the function body.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_, y_] := x * 2</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := x * 2</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_UNUSED, "parameters");

            rule378.setDebtRemediationFunction(rule378.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule379 = repository.createRule(WRITE_ONLY_VARIABLE_KEY)
            .setName("Variable is only written to, never read")
            .setHtmlDescription(
                "<p>Variable has assignments but is never read, making all writes pointless.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = expensive[]; (* x never used *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = expensive[]; Print[x]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule379.setDebtRemediationFunction(rule379.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule380 = repository.createRule(REDUNDANT_ASSIGNMENT_KEY)
            .setName("Variable assigned same value multiple times")
            .setHtmlDescription(
                "<p>Variable is assigned the same value repeatedly without changes.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5; x = 5</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("redundant", "code-smell");

            rule380.setDebtRemediationFunction(rule380.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule381 = repository.createRule(VARIABLE_IN_WRONG_SCOPE_KEY)
            .setName("Variable could be declared in more specific scope")
            .setHtmlDescription(
                "<p>Variable is only used within an inner scope and could be declared there instead.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x}, Block[{}, x = 5; Print[x]]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{}, Block[{x}, x = 5; Print[x]]]</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("scope", "best-practice");

            rule381.setDebtRemediationFunction(rule381.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule382 = repository.createRule(VARIABLE_ESCAPES_SCOPE_KEY)
            .setName("Module variable captured in closure may fail")
            .setHtmlDescription(
                "<p>Module variable is captured in a closure (function definition). Will fail after Module exits.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x}, x = 5; f[] := x]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>With[{x = 5}, f[] := x]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("scope", "closure", "logic-error");

            rule382.setDebtRemediationFunction(rule382.debtRemediationFunctions().constantPerIssue(TIME_30MIN));
    }

    /**
     * SYMBOL TABLE ANALYSIS RULES (10 new rules) (Part 2) (10 rules)
     */
    private static void defineSymbolTableAnalysisRules2(NewRepository repository) {

        // Advanced Symbol Table Analysis Rules (10 rules)

        NewRule rule383 = repository.createRule(LIFETIME_EXTENDS_BEYOND_SCOPE_KEY)
            .setName("Variable lifetime extends beyond necessary scope")
            .setHtmlDescription(
                "<p>Variable is used in a narrow range but declared in wider scope, wasting memory.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x}, x = 5; (* 100 lines *); Block[{}, Print[x]]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{}, (* 100 lines *); Block[{x}, x = 5; Print[x]]]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("scope", "memory", "maintainability");

            rule383.setDebtRemediationFunction(rule383.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule384 = repository.createRule(MODIFIED_IN_UNEXPECTED_SCOPE_KEY)
            .setName("Variable modified in unexpected scope")
            .setHtmlDescription(
                "<p>Variable is read in one scope but modified in unrelated scope, making dataflow hard to track.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x}, x = 5; Block[{}, x = 10]; Print[x]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{x}, x = 5; Block[{y}, y = 10; x = y]; Print[x]]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("scope", "data-flow", "maintainability");

            rule384.setDebtRemediationFunction(rule384.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule385 = repository.createRule(GLOBAL_VARIABLE_POLLUTION_KEY)
            .setName("Too many global variables defined")
            .setHtmlDescription(
                "<p>File defines many global variables, polluting global namespace. Use Package or Context instead.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x=1; y=2; z=3; (* ... 20+ globals *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"]; x=1; y=2; EndPackage[]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("global", "namespace", "architecture");

            rule385.setDebtRemediationFunction(rule385.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule386 = repository.createRule(CIRCULAR_VARIABLE_DEPENDENCIES_KEY)
            .setName("Circular variable dependencies detected")
            .setHtmlDescription(
                "<p>Variables have circular dependencies (A depends on B, B depends on C, C depends on A).</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = y + 1; y = z + 1; z = x + 1</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 0; y = x + 1; z = y + 1</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("circular-dependency", "logic-error");

            rule386.setDebtRemediationFunction(rule386.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule387 = repository.createRule(NAMING_CONVENTION_VIOLATIONS_KEY)
            .setName("Variable naming convention violations")
            .setHtmlDescription(
                "<p>Variables should follow consistent naming conventions (descriptive names, avoid single letters).</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x, y, TEMP}, ...]  (* Single letters, all-caps for non-constants *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{result, count, tempValue}, ...]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", TAG_READABILITY);

            rule387.setDebtRemediationFunction(rule387.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule388 = repository.createRule(CONSTANT_NOT_MARKED_AS_CONSTANT_KEY)
            .setName("Variable assigned once should be constant")
            .setHtmlDescription(
                "<p>Variable assigned once and read multiple times should use With[] for constants.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{pi}, pi = 3.14159; f[x_] := pi * x; ...]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>With[{pi = 3.14159}, f[x_] := pi * x; ...]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("constants", "best-practice");

            rule388.setDebtRemediationFunction(rule388.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule389 = repository.createRule(TYPE_INCONSISTENCY_KEY)
            .setName("Variable used with inconsistent types")
            .setHtmlDescription(
                "<p>Variable used as different types (number, string, list) in different contexts.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5; x = x + 1; x = \"result: \" <> ToString[x]; x[[1]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>num = 5; num = num + 1; str = \"result: \" <> ToString[num]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type", "logic-error");

            rule389.setDebtRemediationFunction(rule389.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule390 = repository.createRule(VARIABLE_REUSE_WITH_DIFFERENT_SEMANTICS_KEY)
            .setName("Variable reused for different purposes")
            .setHtmlDescription(
                "<p>Variable is reused for different purposes (counter, then accumulator), reducing clarity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 0; Do[x++, {10}]; x = Total[data]; Print[x]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>count = 0; Do[count++, {10}]; sum = Total[data]; Print[sum]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("clarity", "maintainability");

            rule390.setDebtRemediationFunction(rule390.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule391 = repository.createRule(INCORRECT_CLOSURE_CAPTURE_KEY)
            .setName("Loop variable incorrectly captured in closure")
            .setHtmlDescription(
                "<p>Loop variable captured in closure will capture final value only, not current iteration value.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[funcs[[i]] = Function[], i -> Range[5], {i, 5}]  (* All capture final i *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Do[With[{j = i}, funcs[[i]] = Function[j]], {i, 5}]  (* Each captures own j *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("closure", "loop", "logic-error");

            rule391.setDebtRemediationFunction(rule391.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule392 = repository.createRule(SCOPE_LEAK_THROUGH_DYNAMIC_EVALUATION_KEY)
            .setName("Variable scope may leak through dynamic evaluation")
            .setHtmlDescription(
                "<p>Module variable used in ToExpression or similar dynamic evaluation may leak scope.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x}, x = 5; ToExpression[\"x + 1\"]]  (* x escapes Module scope *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{x}, x = 5; With[{val = x}, ToExpression[\"val + 1\"]]]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("scope", "dynamic", TAG_SECURITY);

            rule392.setDebtRemediationFunction(rule392.debtRemediationFunctions().constantPerIssue(TIME_30MIN));
    }

    /**
     * PERFORMANCE LIMITS (INFO-level informational rules) (2 rules)
     */
    private static void definePerformanceLimits(NewRepository repository) {
        // ===== PERFORMANCE LIMITS (INFO-level informational rules) =====

        NewRule rule393 = repository.createRule(FILE_EXCEEDS_ANALYSIS_LIMIT_KEY)
            .setName("File exceeds analysis size limit")
            .setHtmlDescription(
                "<p>This file exceeds the maximum analysis size limit (25,000 lines) and has been skipped to prevent analysis timeouts.</p>"
                + "<h2>Why This Limit Exists</h2>"
                + "<p>Extremely large files can cause exponential complexity in advanced symbol table analysis, "
                + "potentially leading to analysis hangs.</p>"
                + "<h2>Recommendations</h2>"
                + "<ul>"
                + "<li><strong>Refactor large files</strong> into smaller, more maintainable modules</li>"
                + "<li><strong>Split functionality</strong> across multiple files for better organization</li>"
                + "<li><strong>Use packages</strong> to group related functions in separate files</li>"
                + "</ul>"
                + "<p>This is an <strong>informational message</strong>, not a code quality issue. "
                + "The file is simply too large for automated analysis.</p>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "file-size", "maintainability");

            rule393.setDebtRemediationFunction(rule393.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule394 = repository.createRule(ANALYSIS_TIMEOUT_KEY)
            .setName("Symbol table analysis timeout")
            .setHtmlDescription(
                "<p>Symbol table analysis for this file exceeded the 120-second timeout limit and was terminated.</p>"
                + "<h2>What This Means</h2>"
                + "<p>Advanced variable lifetime and scope analysis rules (20 rules) were skipped for this file. "
                + "All other code quality, security, and bug rules still ran normally.</p>"
                + "<h2>Why This Happened</h2>"
                + "<p>Certain code patterns can trigger pathological O(n²) or exponential complexity in symbol table analysis:</p>"
                + "<ul>"
                + "<li><strong>Heavy Export/Import usage</strong> with complex variable scoping</li>"
                + "<li><strong>Deeply nested Module/Block/With constructs</strong></li>"
                + "<li><strong>Large number of symbol definitions</strong> with complex dependencies</li>"
                + "</ul>"
                + "<h2>Recommendations</h2>"
                + "<ul>"
                + "<li><strong>Simplify variable scoping</strong> by reducing nesting depth</li>"
                + "<li><strong>Reduce Export/Import complexity</strong> by using explicit symbol lists</li>"
                + "<li><strong>Break up complex functions</strong> into smaller, focused units</li>"
                + "</ul>"
                + "<p>This is an <strong>informational message</strong>, not a code quality issue. "
                + "The file's complexity exceeded analysis capacity.</p>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "complexity", "analysis-limits");

            rule394.setDebtRemediationFunction(rule394.debtRemediationFunctions().constantPerIssue("2min"));
    }

}
