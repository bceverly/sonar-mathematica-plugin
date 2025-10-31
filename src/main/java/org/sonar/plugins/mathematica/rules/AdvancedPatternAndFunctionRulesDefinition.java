package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DATEOBJECT_VALIDATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DELETEDUPS_ON_LARGE_DATA_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EVALUATION_ORDER_ASSUMPTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EXCESSIVE_PURE_FUNCTIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EXPLICIT_GLOBAL_CONTEXT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EXPOSING_SENSITIVE_DATA_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FLATTEN_TABLE_ANTIPATTERN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GLOBAL_STATE_MODIFICATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.HARDCODED_API_KEYS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.HARDCODED_FILE_PATHS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCONSISTENT_RETURN_TYPES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCONSISTENT_RULE_TYPES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCORRECT_ASSOCIATION_OPERATIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCORRECT_LEVEL_SPECIFICATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCORRECT_SET_IN_SCOPING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LINEAR_SEARCH_INSTEAD_LOOKUP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MACHINE_PRECISION_IN_SYMBOLIC_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_CLOUD_AUTH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_COMPILATION_TARGET_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_DOWNVALUES_DOC_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_EMPTY_LIST_CHECK_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_ERROR_MESSAGES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_FAILED_CHECK_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_FORMFUNCTION_VALIDATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_FUNCTION_ATTRIBUTES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_HOLD_ATTRIBUTES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_LOCALIZATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_MATRIX_DIMENSION_CHECK_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_MEMOIZATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_OPERATOR_PRECEDENCE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_PARALLELIZATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_PATTERN_TEST_VALIDATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_SPARSE_ARRAY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_SPECIAL_CASE_HANDLING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_TEMPORARY_CLEANUP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NEEDS_GET_UNTRUSTED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NESTED_LISTS_INSTEAD_ASSOCIATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.OVERCOMPLEX_PATTERNS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.POSITION_INSTEAD_PATTERN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.QUANTITY_UNIT_MISMATCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPEATED_CALCULATIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPEATED_PART_EXTRACTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPEATED_STRING_PARSING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.STRINGJOIN_FOR_TEMPLATES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PERFORMANCE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_READABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_RELIABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_15MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_45MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TOEXPRESSION_ON_INPUT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TOTAL_MEAN_ON_NON_NUMERIC_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNNECESSARY_TRANSPOSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNPACKING_PACKED_ARRAYS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNSANITIZED_RUNPROCESS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ZERO_DENOMINATOR_KEY;

/**
 * Advanced Pattern and Function rule definitions.
 * Covers advanced pattern matching, function attributes, and best practices.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class AdvancedPatternAndFunctionRulesDefinition {

    private AdvancedPatternAndFunctionRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineAdvancedFunctionDesignAndClarityRules(repository);
        defineDataStructureAndPerformanceOptimizationRules(repository);
        defineEvaluationAndArrayOptimizationRules(repository);
        definePerformanceOptimizationAndSecurityVulnerabilityRules(repository);
        defineAdvancedPatternRulesContinued(repository);
        defineDataStructureRulesContinued(repository);
    }

    /**
     * PHASE 4: 50 NEW RULES (Part 1) (12 rules)
     */
    private static void defineAdvancedFunctionDesignAndClarityRules(NewRepository repository) {
        // ===== PHASE 4: 50 NEW RULES =====

        // Pattern Matching & Function Definition Rules (5 rules)

        NewRule rule75 = repository.createRule(OVERCOMPLEX_PATTERNS_KEY)
            .setName("Pattern definitions should not be overly complex")
            .setHtmlDescription(
                "<p>Patterns with more than 5 alternatives or deeply nested conditions are difficult to understand and maintain.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nf[x_Integer | x_Real | x_Rational | x_String | x_Symbol | x_List] := x  (* 6 alternatives *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nf[x_?AtomQ] := x  (* Simplified using pattern test *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("complexity", "maintainability");

            rule75.setDebtRemediationFunction(rule75.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule76 = repository.createRule(INCONSISTENT_RULE_TYPES_KEY)
            .setName("Rule and RuleDelayed should be used consistently")
            .setHtmlDescription(
                "<p>Mixing Rule (->) and RuleDelayed (:>) without clear justification makes code behavior unpredictable.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nrules = {a -> RandomReal[], b :> RandomReal[]}  (* Inconsistent *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nrules = {a :> RandomReal[], b :> RandomReal[]}  (* Both delayed *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("consistency");

            rule76.setDebtRemediationFunction(rule76.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule77 = repository.createRule(MISSING_FUNCTION_ATTRIBUTES_KEY)
            .setName("Public functions should have appropriate attributes")
            .setHtmlDescription(
                "<p>Functions should use Listable, HoldAll, Protected, or other attributes where appropriate.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nMyFunction[x_] := x^2  (* Could be Listable *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nSetAttributes[MyFunction, Listable];\nMyFunction[x_] := x^2\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("best-practice");

            rule77.setDebtRemediationFunction(rule77.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule78 = repository.createRule(MISSING_DOWNVALUES_DOC_KEY)
            .setName("Complex pattern-based functions should have documentation")
            .setHtmlDescription(
                "<p>Functions with multiple DownValues patterns should include usage messages.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nMyFunc::usage = \"MyFunc[x] computes...\";\nMyFunc[x_Integer] := x;\nMyFunc[x_Real] := x^2;\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation");

            rule78.setDebtRemediationFunction(rule78.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule79 = repository.createRule(MISSING_PATTERN_TEST_VALIDATION_KEY)
            .setName("Functions should validate input types with pattern tests")
            .setHtmlDescription(
                "<p>Use ?NumericQ, ?ListQ, ?MatrixQ to validate inputs and prevent runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nf[x_] := x^2  (* Accepts anything *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nf[x_?NumericQ] := x^2  (* Only numeric input *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, "validation");

            rule79.setDebtRemediationFunction(rule79.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Code Clarity Rules (8 rules)

        NewRule rule80 = repository.createRule(EXCESSIVE_PURE_FUNCTIONS_KEY)
            .setName("Complex pure functions should use named parameters")
            .setHtmlDescription(
                "<p>Pure functions with #, #2, #3 in complex expressions are hard to read.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nMap[#1^2 + #2*#3 - #1/#2 &, data]  (* Hard to read *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nMap[Function[{x, y, z}, x^2 + y*z - x/y], data]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY);

            rule80.setDebtRemediationFunction(rule80.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule81 = repository.createRule(MISSING_OPERATOR_PRECEDENCE_KEY)
            .setName("Complex operator expressions should use parentheses for clarity")
            .setHtmlDescription(
                "<p>Mixing /@, @@, //@, @@ without parentheses makes precedence unclear.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nf /@ g @@ data  (* Unclear precedence *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nf /@ (g @@ data)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY);

            rule81.setDebtRemediationFunction(rule81.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule82 = repository.createRule(HARDCODED_FILE_PATHS_KEY)
            .setName("File paths should not be hardcoded")
            .setHtmlDescription(
                "<p>Absolute paths like C:\\ or /Users/ are not portable. Use FileNameJoin, $HomeDirectory.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\ndata = Import[\"/Users/john/data.csv\"];\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\ndata = Import[FileNameJoin[{$HomeDirectory, \"data.csv\"}]];\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("portability");

            rule82.setDebtRemediationFunction(rule82.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule83 = repository.createRule(INCONSISTENT_RETURN_TYPES_KEY)
            .setName("Functions should return consistent types")
            .setHtmlDescription(
                "<p>A function that returns List in one case and Association in another is confusing.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nf[x_Integer] := {x};\nf[x_Real] := <|\"value\" -> x|>;  (* Inconsistent *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("consistency");

            rule83.setDebtRemediationFunction(rule83.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        defineAdvancedPatternRules2(repository);
    }

    private static void defineAdvancedPatternRules2(NewRepository repository) {
        NewRule rule84 = repository.createRule(MISSING_ERROR_MESSAGES_KEY)
            .setName("Custom functions should define error messages")
            .setHtmlDescription(
                "<p>Use Message[...] definitions to provide helpful error feedback.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nMyFunc::badarg = \"Invalid argument: `1`\";\nMyFunc[x_] := Message[MyFunc::badarg, x] /; x < 0\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("usability");

            rule84.setDebtRemediationFunction(rule84.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule85 = repository.createRule(GLOBAL_STATE_MODIFICATION_KEY)
            .setName("Functions modifying global state should be clearly named")
            .setHtmlDescription(
                "<p>Functions with side effects should end with ! to indicate state modification.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nUpdateCounter[n_] := (globalCount = n)  (* No ! suffix *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nUpdateCounter![n_] := (globalCount = n)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("conventions", "side-effects");

            rule85.setDebtRemediationFunction(rule85.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule86 = repository.createRule(MISSING_LOCALIZATION_KEY)
            .setName("Dynamic interfaces should use LocalizeVariables")
            .setHtmlDescription(
                "<p>Manipulate and DynamicModule should localize variables properly.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nManipulate[Plot[Sin[a x], {x, 0, 2Pi}], {a, 1, 5}]  (* 'a' may leak *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("scoping");

            rule86.setDebtRemediationFunction(rule86.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * PHASE 4: 50 NEW RULES (Part 2) (12 rules)
     */
    private static void defineDataStructureAndPerformanceOptimizationRules(NewRepository repository) {

        NewRule rule87 = repository.createRule(EXPLICIT_GLOBAL_CONTEXT_KEY)
            .setName("Global` context should not be used explicitly")
            .setHtmlDescription(
                "<p>Using Global`symbol explicitly is a code smell indicating namespace confusion.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nGlobal`myVariable = 5;\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nmyVariable = 5;  (* Implicit global *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("conventions");

            rule87.setDebtRemediationFunction(rule87.debtRemediationFunctions().constantPerIssue("5min"));

        // Data Structure Rules (5 rules)

        NewRule rule88 = repository.createRule(MISSING_TEMPORARY_CLEANUP_KEY)
            .setName("Temporary files and directories should be cleaned up")
            .setHtmlDescription(
                "<p>CreateFile, CreateDirectory should use auto-deletion or manual cleanup.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\ntempFile = CreateFile[]  (* Auto-deleted at end of session *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("resource-management");

            rule88.setDebtRemediationFunction(rule88.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule89 = repository.createRule(NESTED_LISTS_INSTEAD_ASSOCIATION_KEY)
            .setName("Use Association instead of nested indexed lists")
            .setHtmlDescription(
                "<p>Accessing data[[5]], data[[7]] repeatedly suggests Association would be clearer.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\ndata = {\"John\", 25, \"Engineer\", 50000};\nname = data[[1]]; salary = data[[4]];\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\ndata = <|\"name\" -> \"John\", \"age\" -> 25, \"salary\" -> 50000|>;\n"
                + "name = data[\"name\"]; salary = data[\"salary\"];\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY, "maintainability");

            rule89.setDebtRemediationFunction(rule89.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule90 = repository.createRule(REPEATED_PART_EXTRACTION_KEY)
            .setName("Repeated Part extractions should be destructured")
            .setHtmlDescription(
                "<p>Use destructuring to extract multiple parts at once.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nx = data[[1]]; y = data[[2]]; z = data[[3]];\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n{x, y, z} = data[[{1, 2, 3}]];\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("clarity");

            rule90.setDebtRemediationFunction(rule90.debtRemediationFunctions().constantPerIssue("5min"));

        defineHoldAttributesAndEvaluationRules(repository);
    }

    private static void defineHoldAttributesAndEvaluationRules(NewRepository repository) {
        NewRule rule91 = repository.createRule(MISSING_MEMOIZATION_KEY)
            .setName("Expensive pure computations should use memoization")
            .setHtmlDescription(
                "<p>Functions doing expensive calculations should cache results.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nf[x_] := f[x] = ExpensiveComputation[x]  (* Memoized *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE);

            rule91.setDebtRemediationFunction(rule91.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule92 = repository.createRule(STRINGJOIN_FOR_TEMPLATES_KEY)
            .setName("Use StringTemplate instead of repeated StringJoin")
            .setHtmlDescription(
                "<p>Multiple <> operations are less readable than StringTemplate.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nmsg = \"User: \" <> name <> \", Age: \" <> ToString[age];\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nmsg = StringTemplate[\"User: `name`, Age: `age`\"][<|\"name\" -> name, \"age\" -> age|>];\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY);

            rule92.setDebtRemediationFunction(rule92.debtRemediationFunctions().constantPerIssue("5min"));

        // Type & Value Error Rules (8 new bugs)

        NewRule rule93 = repository.createRule(MISSING_EMPTY_LIST_CHECK_KEY)
            .setName("First, Last, and Part should check for empty lists")
            .setHtmlDescription(
                "<p>Using First, Last, Part on empty lists causes runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nresult = First[data]  (* Crashes if data == {} *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nresult = If[Length[data] > 0, First[data], defaultValue]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags(TAG_RELIABILITY, "crash");

            rule93.setDebtRemediationFunction(rule93.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule94 = repository.createRule(MACHINE_PRECISION_IN_SYMBOLIC_KEY)
            .setName("Avoid machine precision floats in symbolic calculations")
            .setHtmlDescription(
                "<p>Using 1.5 instead of 3/2 in exact calculations loses precision.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nSolve[x^2 == 2.0, x]  (* Machine precision contaminates result *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nSolve[x^2 == 2, x]  (* Exact *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("precision", "correctness");

            rule94.setDebtRemediationFunction(rule94.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule95 = repository.createRule(MISSING_FAILED_CHECK_KEY)
            .setName("Check for $Failed after Import, Get, URLFetch operations")
            .setHtmlDescription(
                "<p>Operations like Import can return $Failed on error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\ndata = Import[\"file.csv\"];\nresult = Mean[data]  (* Crashes if Import failed *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\ndata = Import[\"file.csv\"];\nIf[data === $Failed, Return[$Failed]];\nresult = Mean[data]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, "error-handling");

            rule95.setDebtRemediationFunction(rule95.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule96 = repository.createRule(ZERO_DENOMINATOR_KEY)
            .setName("Division operations should guard against zero denominators")
            .setHtmlDescription(
                "<p>Symbolic division can produce ComplexInfinity if denominator is zero.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nresult = a / b  (* May produce ComplexInfinity *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nresult = If[b != 0, a / b, $Failed]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY);

            rule96.setDebtRemediationFunction(rule96.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule97 = repository.createRule(MISSING_MATRIX_DIMENSION_CHECK_KEY)
            .setName("Matrix operations should validate compatible dimensions")
            .setHtmlDescription(
                "<p>Dot, Times on matrices with incompatible dimensions cause errors.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nIf[Dimensions[a][[2]] == Dimensions[b][[1]], a.b, $Failed]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, "linear-algebra");

            rule97.setDebtRemediationFunction(rule97.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule98 = repository.createRule(INCORRECT_SET_IN_SCOPING_KEY)
            .setName("Use proper assignment inside Module and Block")
            .setHtmlDescription(
                "<p>Using = instead of := in scoping constructs can cause evaluation issues.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nModule[{x = RandomReal[]}, ...]  (* Evaluated at definition time *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nModule[{x}, x = RandomReal[]; ...]  (* Evaluated at runtime *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("scoping", "evaluation");

            rule98.setDebtRemediationFunction(rule98.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

    /**
     * PHASE 4: 50 NEW RULES (Part 3) (12 rules)
     */
    private static void defineEvaluationAndArrayOptimizationRules(NewRepository repository) {

        defineEvaluationAndEdgeCaseRules(repository);
    }

    private static void defineEvaluationAndEdgeCaseRules(NewRepository repository) {
        NewRule rule99 = repository.createRule(MISSING_HOLD_ATTRIBUTES_KEY)
            .setName("Functions delaying evaluation should use Hold attributes")
            .setHtmlDescription(
                "<p>Functions that manipulate unevaluated expressions need HoldAll, HoldFirst, etc.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nSetAttributes[MyIf, HoldRest];\nMyIf[test_, true_, false_] := If[test, true, false]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("evaluation");

            rule99.setDebtRemediationFunction(rule99.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        defineAdvancedPatternRulesContinued(repository);
    }

    private static void defineAdvancedPatternRulesContinued(NewRepository repository) {
        defineAdvancedPatternRulesAdditional(repository);
    }

    private static void defineAdvancedPatternRulesAdditional(NewRepository repository) {
        defineAdvancedPatternRulesFinal(repository);
    }

    private static void defineAdvancedPatternRulesFinal(NewRepository repository) {
        NewRule rule100 = repository.createRule(EVALUATION_ORDER_ASSUMPTION_KEY)
            .setName("Do not rely on implicit evaluation order")
            .setHtmlDescription(
                "<p>Mathematica evaluation order is not always left-to-right.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n{i++, i++, i++}  (* Order not guaranteed *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("undefined-behavior");

            rule100.setDebtRemediationFunction(rule100.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Data Handling Bug Rules (7 rules)

        NewRule rule101 = repository.createRule(INCORRECT_LEVEL_SPECIFICATION_KEY)
            .setName("Map, Apply, Cases should use correct level specifications")
            .setHtmlDescription(
                "<p>Wrong level specifications cause silent failures or unexpected results.</p>"
                + "<h2>Example</h2>"
                + "<pre>\nMap[f, {{1, 2}, {3, 4}}]  (* Level 1 by default *)\nMap[f, {{1, 2}, {3, 4}}, {2}]  (* Level 2 *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("correctness");

            rule101.setDebtRemediationFunction(rule101.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule102 = repository.createRule(UNPACKING_PACKED_ARRAYS_KEY)
            .setName("Avoid operations that unpack packed arrays")
            .setHtmlDescription(
                "<p>Operations like Append, Delete on packed arrays cause 10-100x slowdowns.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nDo[data = Append[data, i], {i, 1000}]  (* Unpacks repeatedly *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\ndata = Range[1000]  (* Stays packed *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags(TAG_PERFORMANCE, "packed-arrays");

            rule102.setDebtRemediationFunction(rule102.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule103 = repository.createRule(MISSING_SPECIAL_CASE_HANDLING_KEY)
            .setName("Handle special values: 0, Infinity, ComplexInfinity, Indeterminate")
            .setHtmlDescription(
                "<p>Functions should handle edge cases like division by zero, limits at infinity.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nf[x_] := f[x] = Which[\n  x === 0, 0,\n  x === Infinity, Infinity,\n  True, NormalComputation[x]\n]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("edge-cases");

            rule103.setDebtRemediationFunction(rule103.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule104 = repository.createRule(INCORRECT_ASSOCIATION_OPERATIONS_KEY)
            .setName("Association operations differ from List operations")
            .setHtmlDescription(
                "<p>Join on Associations merges by keys, not concatenates like Lists.</p>"
                + "<h2>Example</h2>"
                + "<pre>\nJoin[<|a->1|>, <|a->2|>]  (* Result: <|a->2|>, not <|a->1, a->2|> *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("correctness", "associations");

            rule104.setDebtRemediationFunction(rule104.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule105 = repository.createRule(DATEOBJECT_VALIDATION_KEY)
            .setName("Validate DateObject inputs for invalid dates")
            .setHtmlDescription(
                "<p>DateObject can accept invalid dates without errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\ndate = DateObject[{2024, 2, 30}]  (* Invalid date *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("validation");

            rule105.setDebtRemediationFunction(rule105.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        defineDataStructureRulesContinued(repository);
    }

    private static void defineDataStructureRulesContinued(NewRepository repository) {
        defineDataStructureRulesAdditional(repository);
    }

    private static void defineDataStructureRulesAdditional(NewRepository repository) {
        defineDataStructureRulesFinal(repository);
    }

    private static void defineDataStructureRulesFinal(NewRepository repository) {
        NewRule rule106 = repository.createRule(TOTAL_MEAN_ON_NON_NUMERIC_KEY)
            .setName("Total, Mean should only operate on numeric data")
            .setHtmlDescription(
                "<p>Statistical functions on mixed or symbolic data produce unexpected results.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nMean[{1, 2, \"x\"}]  (* Produces symbolic result *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("correctness", "statistics");

            rule106.setDebtRemediationFunction(rule106.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule107 = repository.createRule(QUANTITY_UNIT_MISMATCH_KEY)
            .setName("Quantity operations should have compatible units")
            .setHtmlDescription(
                "<p>Adding Quantity[5, \"Meters\"] + Quantity[3, \"Seconds\"] produces errors.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nUnitConvert[Quantity[5, \"Meters\"] + Quantity[3, \"Centimeters\"], \"Meters\"]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("units", "correctness");

            rule107.setDebtRemediationFunction(rule107.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Performance Rules (10 rules)

        NewRule rule108 = repository.createRule(LINEAR_SEARCH_INSTEAD_LOOKUP_KEY)
            .setName("Use Association or Dispatch for lookups instead of Select")
            .setHtmlDescription(
                "<p>Select[list, #[[1]] == key &] is O(n), Association lookup is O(1).</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nFirst@Select[data, #[[\"id\"]] == targetId &]  (* O(n) *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nassoc = AssociationThread[data[[All, \"id\"]] -> data];\nassoc[targetId]  (* O(1) *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, "algorithmic");

            rule108.setDebtRemediationFunction(rule108.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule109 = repository.createRule(REPEATED_CALCULATIONS_KEY)
            .setName("Expensive expressions should not be recalculated in loops")
            .setHtmlDescription(
                "<p>Hoist invariant calculations out of loops.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nDo[result += data[[i]] * ExpensiveFunc[], {i, n}]  (* Recalculates each time *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nconst = ExpensiveFunc[];\nDo[result += data[[i]] * const, {i, n}]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE);

            rule109.setDebtRemediationFunction(rule109.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule110 = repository.createRule(POSITION_INSTEAD_PATTERN_KEY)
            .setName("Use pattern matching instead of Position when possible")
            .setHtmlDescription(
                "<p>Cases is often faster and clearer than Position + Part extraction.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\npositions = Position[data, _?EvenQ];\nresults = Extract[data, positions]\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nresults = Cases[data, _?EvenQ]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE, "idiomatic");

            rule110.setDebtRemediationFunction(rule110.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * PHASE 4: 50 NEW RULES (Part 4) (14 rules)
     */
    private static void definePerformanceOptimizationAndSecurityVulnerabilityRules(NewRepository repository) {

        NewRule rule111 = repository.createRule(FLATTEN_TABLE_ANTIPATTERN_KEY)
            .setName("Avoid Flatten[Table[...]] pattern")
            .setHtmlDescription(
                "<p>Use Catenate, Join, or vectorization instead of Flatten@Table.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nFlatten[Table[f[i], {i, n}]]  (* Creates intermediate list *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nCatenate[Table[f[i], {i, n}]]  (* More efficient *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE);

            rule111.setDebtRemediationFunction(rule111.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule112 = repository.createRule(MISSING_PARALLELIZATION_KEY)
            .setName("Large independent iterations should use parallelization")
            .setHtmlDescription(
                "<p>Use ParallelTable, ParallelMap for CPU-bound independent tasks.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nParallelTable[ExpensiveFunc[i], {i, 10000}]  (* Uses all cores *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE, "parallelization");

            rule112.setDebtRemediationFunction(rule112.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule113 = repository.createRule(MISSING_SPARSE_ARRAY_KEY)
            .setName("Use SparseArray for arrays with >80% zeros")
            .setHtmlDescription(
                "<p>Dense arrays waste memory and computation on zeros.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nmatrix = SparseArray[{{1,1}->5, {100,100}->3}, {100, 100}]  (* Efficient *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE, "memory");

            rule113.setDebtRemediationFunction(rule113.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule114 = repository.createRule(UNNECESSARY_TRANSPOSE_KEY)
            .setName("Avoid repeatedly transposing data")
            .setHtmlDescription(
                "<p>Work column-wise or row-wise consistently instead of transposing back and forth.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nresult = Transpose[Map[f, Transpose[data]]]  (* Transpose twice *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE);

            rule114.setDebtRemediationFunction(rule114.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule115 = repository.createRule(DELETEDUPS_ON_LARGE_DATA_KEY)
            .setName("DeleteDuplicates on large lists should use alternative methods")
            .setHtmlDescription(
                "<p>For lists >10,000 elements, use Association or Dataset for faster deduplication.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nKeys@GroupBy[largeList, Identity]  (* Faster than DeleteDuplicates *)\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE);

            rule115.setDebtRemediationFunction(rule115.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule116 = repository.createRule(REPEATED_STRING_PARSING_KEY)
            .setName("Parsing the same string multiple times should be avoided")
            .setHtmlDescription(
                "<p>Cache parsed results instead of re-parsing.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nDo[result += ToExpression[str], {i, 1000}]  (* Parses 1000 times *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nexpr = ToExpression[str];\nDo[result += expr, {i, 1000}]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE);

            rule116.setDebtRemediationFunction(rule116.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule117 = repository.createRule(MISSING_COMPILATION_TARGET_KEY)
            .setName("Numerical code should use CompilationTarget->C")
            .setHtmlDescription(
                "<p>Compiled Mathematica code runs 10-100x faster with C compilation.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nCompile[{{x, _Real}}, x^2 + Sin[x], CompilationTarget -> \"C\"]\n</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE, "compilation");

            rule117.setDebtRemediationFunction(rule117.debtRemediationFunctions().constantPerIssue("5min"));

        // Security Vulnerability Rules (7 rules)

        defineSecurityAndCompilationRules(repository);
    }

    private static void defineSecurityAndCompilationRules(NewRepository repository) {
        NewRule rule118 = repository.createRule(TOEXPRESSION_ON_INPUT_KEY)
            .setName("ToExpression on external input enables code injection")
            .setHtmlDescription(
                "<p>ToExpression[userInput] allows arbitrary code execution.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nresult = ToExpression[input]  (* CRITICAL VULNERABILITY *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nresult = Interpreter[\"Number\"][input]  (* Safe parsing *)\n</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li></ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags("cwe", "injection", "owasp-a03");

            rule118.setDebtRemediationFunction(rule118.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule119 = repository.createRule(UNSANITIZED_RUNPROCESS_KEY)
            .setName("RunProcess with user input enables command injection")
            .setHtmlDescription(
                "<p>Shell command injection via RunProcess or Run with unsanitized input.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nRunProcess[{\"bash\", \"-c\", userInput}]  (* Command injection *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n(* Validate and sanitize input first *)\n</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/78.html'>CWE-78</a> - OS Command Injection</li></ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags("cwe", "injection", "owasp-a03");

            rule119.setDebtRemediationFunction(rule119.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule120 = repository.createRule(MISSING_CLOUD_AUTH_KEY)
            .setName("Cloud functions should have authentication and authorization")
            .setHtmlDescription(
                "<p>APIFunction and FormFunction without Permissions checks are publicly accessible.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nCloudDeploy[APIFunction[{}, DoSomething[]]]  (* No auth *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nCloudDeploy[APIFunction[{}, DoSomething[], Permissions -> \"Private\"]]\n</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/306.html'>CWE-306</a> - Missing Authentication</li></ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags("cwe", "authentication", "owasp-a01");

            rule120.setDebtRemediationFunction(rule120.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule121 = repository.createRule(HARDCODED_API_KEYS_KEY)
            .setName("API keys and tokens should not be hardcoded")
            .setHtmlDescription(
                "<p>Hardcoded credentials in CloudDeploy, ServiceConnect expose secrets.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nServiceConnect[\"OpenAI\", \"APIKey\" -> \"sk-abc123...\"]  (* Exposed *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nServiceConnect[\"OpenAI\", \"APIKey\" -> SystemCredential[\"OpenAIKey\"]]\n</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/798.html'>CWE-798</a> - Hard-coded Credentials</li></ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags("cwe", "credentials", "owasp-a07");

            rule121.setDebtRemediationFunction(rule121.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule122 = repository.createRule(NEEDS_GET_UNTRUSTED_KEY)
            .setName("Needs and Get should not load code from untrusted paths")
            .setHtmlDescription(
                "<p>Loading packages from user-controlled paths enables code execution.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nGet[userProvidedPath]  (* Code injection *)\n</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/829.html'>CWE-829</a> - Untrusted Control Sphere</li></ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags("cwe", "code-injection");

            rule122.setDebtRemediationFunction(rule122.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule123 = repository.createRule(EXPOSING_SENSITIVE_DATA_KEY)
            .setName("Cloud functions should not expose sensitive system information")
            .setHtmlDescription(
                "<p>Deploying functions that return $UserName, $MachineName, credentials leaks information.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nCloudDeploy[APIFunction[{}, $UserName]]  (* Leaks username *)\n</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/200.html'>CWE-200</a> - Information Exposure</li></ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags("cwe", "information-disclosure");

            rule123.setDebtRemediationFunction(rule123.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule124 = repository.createRule(MISSING_FORMFUNCTION_VALIDATION_KEY)
            .setName("FormFunction inputs should be validated and sanitized")
            .setHtmlDescription(
                "<p>Cloud forms accepting arbitrary input without validation enable attacks.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nFormFunction[{\"age\" -> Restricted[\"Integer\", {1, 120}]}, DoSomething]\n</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/20.html'>CWE-20</a> - Improper Input Validation</li></ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags("cwe", "validation", "owasp-a03");

            rule124.setDebtRemediationFunction(rule124.debtRemediationFunctions().constantPerIssue(TIME_30MIN));
    }

    }
