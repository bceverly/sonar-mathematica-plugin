package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.APPEND_IN_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.BLOCK_MODULE_MISUSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMPLEX_BOOLEAN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DYNAMIC_INJECTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GENERIC_VARIABLE_NAMES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GROWING_DEFINITION_CHAIN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.IMPORT_WITHOUT_FORMAT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LARGE_TEMP_EXPRESSIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_OPTIONS_PATTERN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_PATTERN_TEST_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_RETURN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_USAGE_MESSAGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NESTED_MAP_TABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKED_ARRAY_BREAKING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_BLANKS_MISUSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PLOT_IN_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPEATED_FUNCTION_CALLS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SET_DELAYED_CONFUSION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_CRITICAL;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MAJOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MINOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SIDE_EFFECTS_NAMING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.STRING_CONCAT_IN_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SYMBOL_NAME_COLLISION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PERFORMANCE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_READABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_RELIABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_SECURITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_15MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_45MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNCLOSED_FILE_HANDLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNCOMPILED_NUMERICAL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNPROTECTED_SYMBOLS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNSAFE_CLOUD_DEPLOY_KEY;

/**
 * Performance rule definitions.
 * Covers performance issues, optimization patterns, and efficient coding practices.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class PerformanceRulesDefinition {

    private PerformanceRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        definePhase3Rules(repository);
        definePhase3Rules2(repository);
    }

    /**
     * PHASE 3 RULES (25 rules) (Part 1) (12 rules)
     */
    private static void definePhase3Rules(NewRepository repository) {
        // ===== PHASE 3 RULES (25 rules) =====

        // PERFORMANCE ISSUES (8 rules)

        NewRule rule50 = repository.createRule(APPEND_IN_LOOP_KEY)
            .setName("AppendTo should not be used in loops")
            .setHtmlDescription(
                "<p>Using AppendTo or Append inside loops creates O(n²) performance due to repeated list copying.</p>"
                + "<p>Use Table, Sow/Reap, or build a list then Join instead.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "result = {};\n"
                + "Do[result = Append[result, f[i]], {i, 1000}]  (* O(n²) - Very slow! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use Table - O(n) *)\n"
                + "result = Table[f[i], {i, 1000}]\n\n"
                + "(* Or Sow/Reap *)\n"
                + "result = Reap[Do[Sow[f[i]], {i, 1000}]][[2, 1]]\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/1050.html'>CWE-1050</a> - Excessive Platform Resource Consumption</li></ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "mathematica-specific");

            rule50.setDebtRemediationFunction(rule50.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule51 = repository.createRule(REPEATED_FUNCTION_CALLS_KEY)
            .setName("Expensive function calls should not be repeated")
            .setHtmlDescription(
                "<p>Calling the same function multiple times with identical arguments wastes computation.</p>"
                + "<p>Cache the result in a variable or use memoization.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "result = ExpensiveComputation[data] + ExpensiveComputation[data]  (* Computes twice! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "cached = ExpensiveComputation[data];\n"
                + "result = cached + cached\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE);

            rule51.setDebtRemediationFunction(rule51.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule52 = repository.createRule(STRING_CONCAT_IN_LOOP_KEY)
            .setName("String concatenation should not be used in loops")
            .setHtmlDescription(
                "<p>Using <> to concatenate strings in loops is O(n²) due to string immutability.</p>"
                + "<p>Collect strings in a list, then use StringJoin.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "result = \"\";\n"
                + "Do[result = result <> ToString[i], {i, 1000}]  (* O(n²) *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "result = StringJoin[Table[ToString[i], {i, 1000}]]  (* O(n) *)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE);

            rule52.setDebtRemediationFunction(rule52.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule53 = repository.createRule(UNCOMPILED_NUMERICAL_KEY)
            .setName("Numerical loops should use Compile")
            .setHtmlDescription(
                "<p>Numerical computations in loops can be 10-100x faster when compiled.</p>"
                + "<p>Consider using Compile for numerical code with loops.</p>"
                + "<h2>Example</h2>"
                + "<pre>\n"
                + "(* Without Compile - slower *)\n"
                + "sum = 0; Do[sum += i^2, {i, 10000}]\n\n"
                + "(* With Compile - much faster *)\n"
                + "compiled = Compile[{}, Module[{sum = 0}, Do[sum += i^2, {i, 10000}]; sum]];\n"
                + "result = compiled[]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "optimization");

            rule53.setDebtRemediationFunction(rule53.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule54 = repository.createRule(PACKED_ARRAY_BREAKING_KEY)
            .setName("Operations should preserve packed arrays")
            .setHtmlDescription(
                "<p>Packed arrays are 10x+ faster than unpacked arrays.</p>"
                + "<p>Avoid operations that unpack arrays (mixing types, using symbolic expressions).</p>"
                + "<h2>Operations that Unpack Arrays</h2>"
                + "<ul>"
                + "<li>Mixing integers and reals</li>"
                + "<li>Using symbolic values</li>"
                + "<li>Applying non-numerical functions</li>"
                + "</ul>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "arrays");

            rule54.setDebtRemediationFunction(rule54.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule55 = repository.createRule(NESTED_MAP_TABLE_KEY)
            .setName("Nested Map/Table should be refactored")
            .setHtmlDescription(
                "<p>Nested Map or Table calls can often be replaced with more efficient single operations.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "result = Table[Table[i*j, {j, 10}], {i, 10}]\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "result = Table[i*j, {i, 10}, {j, 10}]  (* More efficient *)\n"
                + "(* Or use Outer for many cases *)\n"
                + "result = Outer[Times, Range[10], Range[10]]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, TAG_READABILITY);

            rule55.setDebtRemediationFunction(rule55.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule56 = repository.createRule(LARGE_TEMP_EXPRESSIONS_KEY)
            .setName("Large temporary expressions should be assigned to variables")
            .setHtmlDescription(
                "<p>Large intermediate results (>100MB) that aren't assigned can cause memory issues.</p>"
                + "<p>Assign large results to variables to make memory usage explicit.</p>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("memory", TAG_PERFORMANCE);

            rule56.setDebtRemediationFunction(rule56.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule57 = repository.createRule(PLOT_IN_LOOP_KEY)
            .setName("Plotting functions should not be called in loops")
            .setHtmlDescription(
                "<p>Generating plots in loops is very slow. Collect data first, then plot once.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Do[ListPlot[data[[i]]], {i, 100}]  (* Creates 100 separate plots *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "ListPlot[data, PlotRange -> All]  (* One plot with all data *)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "visualization");

            rule57.setDebtRemediationFunction(rule57.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // PATTERN MATCHING ISSUES (5 rules)

        NewRule rule58 = repository.createRule(MISSING_PATTERN_TEST_KEY)
            .setName("Numeric functions should test argument types")
            .setHtmlDescription(
                "<p>Functions expecting numeric arguments should use pattern tests to prevent symbolic evaluation errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "f[x_] := Sqrt[x] + x^2  (* Will evaluate symbolically for f[a] *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "f[x_?NumericQ] := Sqrt[x] + x^2  (* Only evaluates for numbers *)\n"
                + "(* Or use _Real, _Integer, etc. *)\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/704.html'>CWE-704</a> - Incorrect Type Conversion</li></ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "type-safety");

            rule58.setDebtRemediationFunction(rule58.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule59 = repository.createRule(PATTERN_BLANKS_MISUSE_KEY)
            .setName("Pattern blanks should be used correctly")
            .setHtmlDescription(
                "<p>Using __ or ___ creates sequences, not lists. This often causes errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "f[x__] := Length[x]  (* ERROR: x is a sequence, Length expects list *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "f[x__] := Length[{x}]  (* Wrap sequence in list *)\n"
                + "(* Or use List pattern *)\n"
                + "f[x_List] := Length[x]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "pattern-matching");

            rule59.setDebtRemediationFunction(rule59.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule60 = repository.createRule(SET_DELAYED_CONFUSION_KEY)
            .setName("Use SetDelayed (:=) for function definitions")
            .setHtmlDescription(
                "<p>Using Set (=) instead of SetDelayed (:=) evaluates the right-hand side immediately, which is usually wrong for functions.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "f[x_] = RandomReal[]  (* Evaluates once, same random number always returned! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "f[x_] := RandomReal[]  (* Evaluates each time function is called *)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "common-mistake");

            rule60.setDebtRemediationFunction(rule60.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule61 = repository.createRule(SYMBOL_NAME_COLLISION_KEY)
            .setName("User symbols should not shadow built-in functions")
            .setHtmlDescription(
                "<p>Defining functions with single-letter names or common words collides with Mathematica built-ins.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "N[x_] := ...  (* Shadows built-in N[] for numerical evaluation! *)\n"
                + "D[x_] := ...  (* Shadows built-in D[] for derivatives! *)\n"
                + "I = 5;  (* Shadows imaginary unit I! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "myN[x_] := ...\n"
                + "derivative[x_] := ...\n"
                + "index = 5;\n"
                + "</pre>"
                + "<h2>Common Built-ins to Avoid</h2>"
                + "<p>N, D, I, C, O, E, K, Pi, Re, Im, Abs, Min, Max, Log, Sin, Cos</p>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "naming");

            rule61.setDebtRemediationFunction(rule61.debtRemediationFunctions().constantPerIssue(TIME_30MIN));
    }

    /**
     * PHASE 3 RULES (25 rules) (Part 2) (13 rules)
     */
    private static void definePhase3Rules2(NewRepository repository) {

        NewRule rule62 = repository.createRule(BLOCK_MODULE_MISUSE_KEY)
            .setName("Block and Module should be used correctly")
            .setHtmlDescription(
                "<p>Block provides dynamic scope, Module provides lexical scope. Using the wrong one causes bugs.</p>"
                + "<h2>When to Use Each</h2>"
                + "<ul>"
                + "<li><strong>Module</strong>: For local variables (most common case)</li>"
                + "<li><strong>Block</strong>: To temporarily change global values</li>"
                + "<li><strong>With</strong>: For constant local values</li>"
                + "</ul>"
                + "<h2>Example</h2>"
                + "<pre>\n"
                + "(* Use Module for local variables *)\n"
                + "f[x_] := Module[{temp = x^2}, temp + 1]\n\n"
                + "(* Use Block to temporarily override globals *)\n"
                + "Block[{$RecursionLimit = 1024}, RecursiveFunction[]]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "scope");

            rule62.setDebtRemediationFunction(rule62.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // BEST PRACTICES (7 rules)

        NewRule rule63 = repository.createRule(GENERIC_VARIABLE_NAMES_KEY)
            .setName("Variables should have meaningful names")
            .setHtmlDescription(
                "<p>Generic names like 'x', 'temp', 'data' provide no context and hurt readability.</p>"
                + "<p>Use descriptive names except in very small scopes (&lt;5 lines) or mathematical contexts.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "data = Import[\"file.csv\"];\n"
                + "result = ProcessData[data];  (* What kind of data? *)\n"
                + "temp = result[[1]];  (* Temp what? *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "salesData = Import[\"sales.csv\"];\n"
                + "processedSales = ProcessSalesData[salesData];\n"
                + "firstQuarterSales = processedSales[[1]];\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_READABILITY, "naming");

            rule63.setDebtRemediationFunction(rule63.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule64 = repository.createRule(MISSING_USAGE_MESSAGE_KEY)
            .setName("Public functions should have usage messages")
            .setHtmlDescription(
                "<p>Public functions (starting with uppercase) should define usage messages for documentation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "ProcessUserData[data_, options___] := Module[...]\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "ProcessUserData::usage = \"ProcessUserData[data, options] processes user data with specified options.\";\n"
                + "ProcessUserData[data_, options___] := Module[...]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation");

            rule64.setDebtRemediationFunction(rule64.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule65 = repository.createRule(MISSING_OPTIONS_PATTERN_KEY)
            .setName("Functions with multiple optional parameters should use OptionsPattern")
            .setHtmlDescription(
                "<p>Functions with 3+ optional parameters should use OptionsPattern for better maintainability.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "PlotData[data_, color_: Blue, size_: 10, style_: Solid, width_: 2] := ...\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "Options[PlotData] = {\"Color\" -> Blue, \"Size\" -> 10, \"Style\" -> Solid, \"Width\" -> 2};\n"
                + "PlotData[data_, opts: OptionsPattern[]] := Module[{color, size, style, width},\n"
                + "  color = OptionValue[\"Color\"];\n"
                + "  size = OptionValue[\"Size\"];\n"
                + "  ...\n"
                + "]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("maintainability", "api-design");

            rule65.setDebtRemediationFunction(rule65.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule66 = repository.createRule(SIDE_EFFECTS_NAMING_KEY)
            .setName("Functions with side effects should have descriptive names")
            .setHtmlDescription(
                "<p>Functions that modify global state should use naming conventions: SetXXX or ending with !.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Process[data_] := (globalCache = data; data)  (* Hidden side effect! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "SetCache[data_] := (globalCache = data; data)  (* Clear from name *)\n"
                + "(* Or use ! suffix like Mathematica built-ins *)\n"
                + "UpdateCache![data_] := (globalCache = data; data)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", "side-effects");

            rule66.setDebtRemediationFunction(rule66.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule67 = repository.createRule(COMPLEX_BOOLEAN_KEY)
            .setName("Complex boolean expressions should be simplified")
            .setHtmlDescription(
                "<p>Boolean expressions with >5 operators without clear grouping are hard to understand.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "If[a && b || c && d && !e || f && g, ...]\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "condition1 = a && b;\n"
                + "condition2 = c && d && !e;\n"
                + "condition3 = f && g;\n"
                + "If[condition1 || condition2 || condition3, ...]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_READABILITY, "complexity");

            rule67.setDebtRemediationFunction(rule67.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule68 = repository.createRule(UNPROTECTED_SYMBOLS_KEY)
            .setName("Public API symbols should be protected")
            .setHtmlDescription(
                "<p>Public functions in packages should be Protected to prevent accidental redefinition by users.</p>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* At end of package *)\n"
                + "Protect[PublicFunction1, PublicFunction2, PublicConstant];\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("api-design", "safety");

            rule68.setDebtRemediationFunction(rule68.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule69 = repository.createRule(MISSING_RETURN_KEY)
            .setName("Complex functions should have explicit Return statements")
            .setHtmlDescription(
                "<p>Functions with multiple branches or complex logic should use explicit Return[] for clarity.</p>"
                + "<h2>Example</h2>"
                + "<pre>\n"
                + "ProcessData[data_] := Module[{result},\n"
                + "  If[data === {}, Return[$Failed]];\n"
                + "  result = ComputeResult[data];\n"
                + "  If[!ValidQ[result], Return[Default]];\n"
                + "  Return[result]\n"
                + "]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_READABILITY);

            rule69.setDebtRemediationFunction(rule69.debtRemediationFunctions().constantPerIssue("5min"));

        // SECURITY & SAFETY (3 rules)

        NewRule rule70 = repository.createRule(UNSAFE_CLOUD_DEPLOY_KEY)
            .setName("CloudDeploy should specify Permissions")
            .setHtmlDescription(
                "<p>CloudDeploy without Permissions parameter creates public cloud objects accessible to anyone.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "CloudDeploy[form]  (* Public by default! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "CloudDeploy[form, Permissions -> \"Private\"]\n"
                + "CloudDeploy[form, Permissions -> {\"user@example.com\" -> \"Read\"}]\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/276.html'>CWE-276</a> - Incorrect Default Permissions</li></ul>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags(TAG_SECURITY, "cloud", "permissions");

            rule70.setDebtRemediationFunction(rule70.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule71 = repository.createRule(DYNAMIC_INJECTION_KEY)
            .setName("Dynamic content should not use ToExpression on user input")
            .setHtmlDescription(
                "<p>Using ToExpression or Symbol on user input in Dynamic creates code injection vulnerabilities.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Dynamic[ToExpression[userInput]]  (* User can execute arbitrary code! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "Dynamic[SafeEvaluate[userInput]]  (* Use whitelist/sanitization *)\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li></ul>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags(TAG_SECURITY, "injection");

            rule71.setDebtRemediationFunction(rule71.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule72 = repository.createRule(IMPORT_WITHOUT_FORMAT_KEY)
            .setName("Import should specify format explicitly")
            .setHtmlDescription(
                "<p>Import without format specification guesses by file extension, which attackers can manipulate.</p>"
                + "<p><strong>This is a Security Hotspot</strong> - Review to ensure format is validated.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Import[userFile]  (* Guesses format - could execute .mx! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "Import[userFile, \"CSV\"]  (* Explicit format, safe *)\n"
                + "Import[userFile, \"JSON\"]\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/434.html'>CWE-434</a> - Unrestricted Upload of File with Dangerous Type</li></ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags(TAG_SECURITY, "file-upload");

            rule72.setDebtRemediationFunction(rule72.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // RESOURCE MANAGEMENT (2 rules)

        NewRule rule73 = repository.createRule(UNCLOSED_FILE_HANDLE_KEY)
            .setName("File handles should be closed")
            .setHtmlDescription(
                "<p>OpenRead, OpenWrite, OpenAppend create file handles that must be closed with Close[].</p>"
                + "<p>Unclosed file handles leak resources and can prevent file access.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "stream = OpenRead[\"file.txt\"];\n"
                + "data = Read[stream, String];\n"
                + "(* Missing Close[stream]! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "stream = OpenRead[\"file.txt\"];\n"
                + "data = Read[stream, String];\n"
                + "Close[stream];\n\n"
                + "(* Or use Import which handles cleanup automatically *)\n"
                + "data = Import[\"file.txt\", \"String\"];\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/772.html'>CWE-772</a> - Missing Release of Resource</li></ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "resource-leak");

            rule73.setDebtRemediationFunction(rule73.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule74 = repository.createRule(GROWING_DEFINITION_CHAIN_KEY)
            .setName("Definitions should not grow unbounded")
            .setHtmlDescription(
                "<p>Repeatedly adding definitions (e.g., memoization in loop) without clearing causes memory leaks.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "(* Memoization in loop - definitions grow forever! *)\n"
                + "Do[\n"
                + "  f[i] = ExpensiveComputation[i],\n"
                + "  {i, 1, 100000}\n"
                + "]  (* Creates 100k definitions, never cleared! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use temporary memoization *)\n"
                + "Block[{f},\n"
                + "  Do[f[i] = ExpensiveComputation[i], {i, 1, 100000}];\n"
                + "  (* Use f here *)\n"
                + "]  (* Definitions cleared when Block exits *)\n\n"
                + "(* Or use Association/Dictionary *)\n"
                + "cache = Association[];\n"
                + "Do[cache[i] = ExpensiveComputation[i], {i, 1, 100000}];\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul><li><a href='https://cwe.mitre.org/data/definitions/401.html'>CWE-401</a> - Memory Leak</li></ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "memory-leak");

            rule74.setDebtRemediationFunction(rule74.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

}
