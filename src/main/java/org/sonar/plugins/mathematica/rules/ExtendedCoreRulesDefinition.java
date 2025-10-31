package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DEEPLY_NESTED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DEPRECATED_FUNCTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DUPLICATE_FUNCTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EMPTY_STATEMENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ENVIRONMENT_VARIABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FILE_SYSTEM_MODIFICATIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FLOATING_POINT_EQUALITY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FUNCTION_WITHOUT_RETURN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.IDENTICAL_BRANCHES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCONSISTENT_NAMING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INFINITE_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INSECURE_RANDOM_EXPANDED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISMATCHED_DIMENSIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_DOCUMENTATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_SANITIZATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NETWORK_OPERATIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.OFF_BY_ONE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_CRITICAL;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MAJOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MINOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SUSPICIOUS_PATTERN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_RELIABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_SECURITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_UNUSED;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_15MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_45MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TOO_MANY_PARAMETERS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TYPE_MISMATCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNSAFE_SYMBOL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_VARIABLES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_BEFORE_ASSIGNMENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.XXE_KEY;

/**
 * Extended Core rule definitions.
 * Extensions of code smell, bug, vulnerability, and security hotspot rules.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
class ExtendedCoreRulesDefinition {

    private ExtendedCoreRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineNewCodeSmellRules(repository);
        defineNewBugRules(repository);
        defineNewVulnerabilityRules(repository);
        defineNewSecurityHotspotRules(repository);
    }

    /**
     * NEW CODE SMELL RULES (Phase 2) (10 rules)
     */
    private static void defineNewCodeSmellRules(NewRepository repository) {
        // ===== NEW CODE SMELL RULES (Phase 2) =====

        // Unused Variables
        NewRule rule25 = repository.createRule(UNUSED_VARIABLES_KEY)
            .setName("Variables should not be declared and not used")
            .setHtmlDescription(
                "<p>Variables declared in Module, Block, or With but never used waste memory and reduce code clarity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Module[{x, y, z},\n"
                + "  x = 5;\n"
                + "  x + 10\n"
                + "];  (* y and z are declared but never used *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "Module[{x},\n"
                + "  x = 5;\n"
                + "  x + 10\n"
                + "];\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_UNUSED, "clutter");

            rule25.setDebtRemediationFunction(rule25.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Duplicate Function Definitions
        NewRule rule26 = repository.createRule(DUPLICATE_FUNCTION_KEY)
            .setName("Functions should not be redefined with same signature")
            .setHtmlDescription(
                "<p>Defining the same function multiple times with the same pattern signature overwrites previous definitions.</p>"
                + "<p>This is usually a mistake and causes confusion about which definition is active.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "myFunc[x_] := x^2;  (* First definition *)\n"
                + "myFunc[y_] := y^3;  (* Overwrites first! Both have same pattern x_ *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use different patterns *)\n"
                + "myFunc[x_Integer] := x^2;\n"
                + "myFunc[x_Real] := x^3;\n"
                + "\n"
                + "(* Or use different function names *)\n"
                + "myFuncSquare[x_] := x^2;\n"
                + "myFuncCube[x_] := x^3;\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("confusing", "pitfall");

            rule26.setDebtRemediationFunction(rule26.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Too Many Parameters
        NewRule rule27 = repository.createRule(TOO_MANY_PARAMETERS_KEY)
            .setName("Functions should not have too many parameters")
            .setHtmlDescription(
                "<p>Functions with more than 7 parameters are difficult to use and maintain.</p>"
                + "<p>Consider using associations or grouping related parameters into structures.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "processData[name_, age_, address_, phone_, email_, city_, state_, zip_] := ...\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use an Association *)\n"
                + "processData[userData_Association] := ...\n"
                + "processData[<|\"name\" -> \"John\", \"age\" -> 30, ...|>]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

            rule27.setDebtRemediationFunction(rule27.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Deeply Nested Conditionals
        NewRule rule28 = repository.createRule(DEEPLY_NESTED_KEY)
            .setName("Conditionals should not be nested too deeply")
            .setHtmlDescription(
                "<p>Deeply nested If/Which/Switch statements (more than 3 levels) are difficult to understand and test.</p>"
                + "<p>Consider extracting nested logic into separate functions or using Which for multiple conditions.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "If[a,\n"
                + "  If[b,\n"
                + "    If[c,\n"
                + "      If[d, result]  (* 4 levels deep! *)\n"
                + "    ]\n"
                + "  ]\n"
                + "]\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use Which for multiple conditions *)\n"
                + "Which[\n"
                + "  a && b && c && d, result,\n"
                + "  a && b && c, otherResult,\n"
                + "  True, defaultResult\n"
                + "]\n"
                + "\n"
                + "(* Or extract to helper functions *)\n"
                + "If[a, processA[], defaultResult]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload", "complexity");

            rule28.setDebtRemediationFunction(rule28.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Missing Documentation
        NewRule rule29 = repository.createRule(MISSING_DOCUMENTATION_KEY)
            .setName("Public functions should be documented")
            .setHtmlDescription(
                "<p>Public functions (starting with uppercase) should have usage documentation.</p>"
                + "<p>This helps users understand what the function does without reading the implementation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "ProcessUserData[data_, options_] := Module[{...}, ...]\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* ProcessUserData[data, options] processes user data and returns cleaned result.\n"
                + "   Parameters:\n"
                + "     data: List of user records\n"
                + "     options: Association of processing options\n"
                + "   Returns: Processed data list\n"
                + "*)\n"
                + "ProcessUserData[data_, options_] := Module[{...}, ...]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation");

            rule29.setDebtRemediationFunction(rule29.debtRemediationFunctions().constantPerIssue("5min"));

        // Inconsistent Naming
        NewRule rule30 = repository.createRule(INCONSISTENT_NAMING_KEY)
            .setName("Naming conventions should be consistent")
            .setHtmlDescription(
                "<p>Mixing different naming conventions (camelCase, PascalCase, snake_case) in the same file reduces readability.</p>"
                + "<p>Mathematica convention: PascalCase for public functions, camelCase for private.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "ProcessData[x_] := ...    (* PascalCase *)\n"
                + "calculateResult[y_] := ... (* camelCase *)\n"
                + "get_user_name[] := ...    (* snake_case - inconsistent! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Consistent PascalCase for public *)\n"
                + "ProcessData[x_] := ...\n"
                + "CalculateResult[y_] := ...\n"
                + "GetUserName[] := ...\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("convention");

            rule30.setDebtRemediationFunction(rule30.debtRemediationFunctions().constantPerIssue("5min"));

        // Identical Branches
        NewRule rule31 = repository.createRule(IDENTICAL_BRANCHES_KEY)
            .setName("Conditional branches should not be identical")
            .setHtmlDescription(
                "<p>If/Which with identical then/else branches is a copy-paste error or indicates dead code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "If[condition, DoSomething[], DoSomething[]]  (* Both branches identical *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* If both branches are same, condition is useless *)\n"
                + "DoSomething[]\n"
                + "\n"
                + "(* Or fix the copy-paste error *)\n"
                + "If[condition, DoSomething[], DoSomethingElse[]]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious", "pitfall");

            rule31.setDebtRemediationFunction(rule31.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Expression Too Complex
        NewRule rule32 = repository.createRule(EXPRESSION_TOO_COMPLEX_KEY)
            .setName("Expressions should not be too complex")
            .setHtmlDescription(
                "<p>Single expressions with more than 20 operations should be split into intermediate steps.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "result = a + b * c - d / e ^ f + g * h - i / j + k * l - m / n + o * p;\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "term1 = b * c + a;\n"
                + "term2 = d / e ^ f;\n"
                + "term3 = g * h - i / j;\n"
                + "result = term1 - term2 + term3;\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

            rule32.setDebtRemediationFunction(rule32.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Deprecated Functions
        NewRule rule33 = repository.createRule(DEPRECATED_FUNCTION_KEY)
            .setName("Deprecated functions should not be used")
            .setHtmlDescription(
                "<p>Some Mathematica functions have been deprecated in favor of newer alternatives.</p>"
                + "<p>Using deprecated functions may cause compatibility issues in future versions.</p>"
                + "<h2>Examples of Deprecated Functions</h2>"
                + "<ul>"
                + "<li><code>$RecursionLimit</code> - Use <code>$IterationLimit</code> or explicit checks</li>"
                + "<li><code>Sqrt[-1]</code> pattern - Use <code>I</code> directly</li>"
                + "</ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("obsolete");

            rule33.setDebtRemediationFunction(rule33.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Empty Statement
        NewRule rule34 = repository.createRule(EMPTY_STATEMENT_KEY)
            .setName("Empty statements should be removed")
            .setHtmlDescription(
                "<p>Empty statements created by double semicolons or misplaced semicolons are usually mistakes.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "x = 5;;  (* Double semicolon *)\n"
                + "If[condition, ;]  (* Empty statement in branch *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "x = 5;\n"
                + "If[condition, DoSomething[]]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious");

            rule34.setDebtRemediationFunction(rule34.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * NEW BUG RULES (Phase 2) (8 rules)
     */
    private static void defineNewBugRules(NewRepository repository) {
        // ===== NEW BUG RULES (Phase 2) =====

        // Floating Point Equality
        NewRule rule35 = repository.createRule(FLOATING_POINT_EQUALITY_KEY)
            .setName("Floating point numbers should not be tested for equality")
            .setHtmlDescription(
                "<p>Using == or === to compare floating point numbers is unreliable due to rounding errors.</p>"
                + "<p>Use a tolerance-based comparison instead.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "If[0.1 + 0.2 == 0.3, ...]  (* May be False due to rounding! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use tolerance-based comparison *)\n"
                + "If[Abs[(0.1 + 0.2) - 0.3] < 10^-10, ...]\n"
                + "\n"
                + "(* Or use Mathematica's Chop *)\n"
                + "If[Chop[0.1 + 0.2 - 0.3] == 0, ...]\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul>"
                + "<li><a href='https://cwe.mitre.org/data/definitions/1077.html'>CWE-1077</a> - Floating Point Comparison</li>"
                + "</ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "floating-point");

            rule35.setDebtRemediationFunction(rule35.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Function Without Return
        NewRule rule36 = repository.createRule(FUNCTION_WITHOUT_RETURN_KEY)
            .setName("Functions should return a value")
            .setHtmlDescription(
                "<p>Functions that end with a semicolon return Null, which is usually unintended.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "calculateResult[x_] := (\n"
                + "  result = x^2 + x;\n"
                + "  Print[result];\n"
                + ");  (* Returns Null! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "calculateResult[x_] := (\n"
                + "  result = x^2 + x;\n"
                + "  Print[result];\n"
                + "  result  (* Return the value *)\n"
                + ");\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY);

            rule36.setDebtRemediationFunction(rule36.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Variable Before Assignment
        NewRule rule37 = repository.createRule(VARIABLE_BEFORE_ASSIGNMENT_KEY)
            .setName("Variables should not be used before assignment")
            .setHtmlDescription(
                "<p>Using a variable before assigning it a value leads to undefined behavior.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Module[{x, y},\n"
                + "  y = x + 5;  (* x used before being assigned *)\n"
                + "  x = 10;\n"
                + "  y\n"
                + "]\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "Module[{x, y},\n"
                + "  x = 10;\n"
                + "  y = x + 5;\n"
                + "  y\n"
                + "]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY);

            rule37.setDebtRemediationFunction(rule37.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        // Off-by-One
        NewRule rule38 = repository.createRule(OFF_BY_ONE_KEY)
            .setName("Loop ranges should not cause off-by-one errors")
            .setHtmlDescription(
                "<p>Common indexing errors: starting at 0 (Mathematica lists are 1-indexed) or going beyond Length.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Do[Print[list[[i]]], {i, 0, Length[list]}]  (* 0 is invalid! *)\n"
                + "Do[Print[list[[i]]], {i, 1, Length[list] + 1}]  (* Beyond length! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "Do[Print[list[[i]]], {i, 1, Length[list]}]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY);

            rule38.setDebtRemediationFunction(rule38.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Infinite Loop
        NewRule rule39 = repository.createRule(INFINITE_LOOP_KEY)
            .setName("While loops should have an exit condition")
            .setHtmlDescription(
                "<p>While[True] without Break or Return inside creates an infinite loop.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "While[True, DoSomething[]]  (* No way to exit! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "While[True,\n"
                + "  DoSomething[];\n"
                + "  If[condition, Break[]]\n"
                + "]\n"
                + "\n"
                + "(* Or use proper condition *)\n"
                + "While[!done, DoSomething[]]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY);

            rule39.setDebtRemediationFunction(rule39.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        // Mismatched Dimensions
        NewRule rule40 = repository.createRule(MISMATCHED_DIMENSIONS_KEY)
            .setName("Matrix operations should use rectangular arrays")
            .setHtmlDescription(
                "<p>Operations like Transpose, Dot assume rectangular (uniform) arrays.</p>"
                + "<p>Non-rectangular arrays cause errors or unexpected results.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "data = {{1, 2}, {3, 4, 5}};  (* Non-rectangular *)\n"
                + "Transpose[data]  (* Will fail *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Ensure rectangular before matrix ops *)\n"
                + "If[Apply[SameQ, Map[Length, data]],\n"
                + "  Transpose[data],\n"
                + "  $Failed\n"
                + "]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY);

            rule40.setDebtRemediationFunction(rule40.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Type Mismatch
        NewRule rule41 = repository.createRule(TYPE_MISMATCH_KEY)
            .setName("Operations should use compatible types")
            .setHtmlDescription(
                "<p>Mixing incompatible types in operations leads to errors or unexpected results.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "result = \"hello\" + 5  (* String + Number error *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "result = \"hello\" <> ToString[5]  (* String concatenation *)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY);

            rule41.setDebtRemediationFunction(rule41.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Suspicious Pattern
        NewRule rule42 = repository.createRule(SUSPICIOUS_PATTERN_KEY)
            .setName("Pattern matching should not have contradictions")
            .setHtmlDescription(
                "<p>Patterns with contradictory constraints will never match or match too broadly.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "(* Impossible constraint *)\n"
                + "func[x_Integer /; x > 10 && x < 5] := ...  (* Never matches *)\n"
                + "\n"
                + "(* Too broad *)\n"
                + "func[___] := ...  (* Matches everything including 0 arguments *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "func[x_Integer /; x > 10] := ...\n"
                + "func[x__] := ...  (* At least one argument *)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_RELIABILITY, "pattern-matching");

            rule42.setDebtRemediationFunction(rule42.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

    /**
     * NEW VULNERABILITY RULES (Phase 2) (4 rules)
     */
    private static void defineNewVulnerabilityRules(NewRepository repository) {
        // ===== NEW VULNERABILITY RULES (Phase 2) =====

        // Unsafe Symbol Construction
        NewRule rule43 = repository.createRule(UNSAFE_SYMBOL_KEY)
            .setName("Symbol construction from user input should be avoided")
            .setHtmlDescription(
                "<p>Using Symbol[] or ToExpression to dynamically construct function names from user input allows code injection.</p>"
                + "<p>Attackers can control which functions are called with your data.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "funcName = userInput;  (* e.g., \"DeleteFile\" *)\n"
                + "Symbol[funcName][userFile]  (* User controls function call! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use explicit whitelist *)\n"
                + "allowedFunctions = <|\"Save\" -> SaveData, \"Load\" -> LoadData|>;\n"
                + "If[KeyExistsQ[allowedFunctions, userInput],\n"
                + "  allowedFunctions[userInput][userFile],\n"
                + "  $Failed\n"
                + "]\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul>"
                + "<li><a href='https://cwe.mitre.org/data/definitions/470.html'>CWE-470</a> - Use of Externally-Controlled Input</li>"
                + "<li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li>"
                + "</ul>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "injection", TAG_SECURITY);

            rule43.setDebtRemediationFunction(rule43.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        // XML External Entity (XXE)
        NewRule rule44 = repository.createRule(XXE_KEY)
            .setName("XML imports should disable external entity processing")
            .setHtmlDescription(
                "<p>Importing XML without disabling external entities allows XML External Entity (XXE) attacks.</p>"
                + "<p>Attackers can read local files, perform SSRF, or cause denial of service.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Import[userFile, \"XML\"]  (* XXE vulnerable! *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Disable DTD processing *)\n"
                + "Import[userFile, {\"XML\", \"ProcessDTD\" -> False}]\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul>"
                + "<li><a href='https://cwe.mitre.org/data/definitions/611.html'>CWE-611</a> - XML External Entity</li>"
                + "<li><a href='https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing'>OWASP</a> - XXE</li>"
                + "</ul>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "xxe", TAG_SECURITY);

            rule44.setDebtRemediationFunction(rule44.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        // Missing Input Sanitization
        NewRule rule45 = repository.createRule(MISSING_SANITIZATION_KEY)
            .setName("User input should be sanitized before use with dangerous functions")
            .setHtmlDescription(
                "<p>User input passed directly to dangerous functions without validation enables attacks.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "DeleteFile[userProvidedPath]  (* User could delete anything! *)\n"
                + "SystemOpen[userURL]  (* User could open malicious URLs *)\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Validate against whitelist *)\n"
                + "allowedFiles = {\"/tmp/file1.txt\", \"/tmp/file2.txt\"};\n"
                + "If[MemberQ[allowedFiles, userPath],\n"
                + "  DeleteFile[userPath],\n"
                + "  $Failed\n"
                + "]\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul>"
                + "<li><a href='https://cwe.mitre.org/data/definitions/20.html'>CWE-20</a> - Improper Input Validation</li>"
                + "</ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "validation", TAG_SECURITY);

            rule45.setDebtRemediationFunction(rule45.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        // Insecure Random (Expanded)
        NewRule rule46 = repository.createRule(INSECURE_RANDOM_EXPANDED_KEY)
            .setName("Secure random should be used for security-sensitive operations")
            .setHtmlDescription(
                "<p>Using Random[] or predictable RandomChoice for session tokens, passwords, or keys is insecure.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "(* Insecure random for tokens *)\n"
                + "sessionToken = StringJoin @@ RandomChoice[CharacterRange[\"a\", \"z\"], 20];\n"
                + "\n"
                + "(* Random[] is not cryptographically secure *)\n"
                + "secretKey = Table[Random[], {32}];\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "(* Use RandomInteger for crypto *)\n"
                + "sessionToken = IntegerString[RandomInteger[{10^40, 10^41 - 1}], 16];\n"
                + "secretKey = RandomInteger[{0, 255}, 32];\n"
                + "</pre>"
                + "<h2>See</h2>"
                + "<ul>"
                + "<li><a href='https://cwe.mitre.org/data/definitions/338.html'>CWE-338</a> - Weak PRNG</li>"
                + "</ul>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "cryptography", TAG_SECURITY);

            rule46.setDebtRemediationFunction(rule46.debtRemediationFunctions().constantPerIssue(TIME_30MIN));
    }

    /**
     * NEW SECURITY HOTSPOT RULES (Phase 2) (3 rules)
     */
    private static void defineNewSecurityHotspotRules(NewRepository repository) {
        // ===== NEW SECURITY HOTSPOT RULES (Phase 2) =====

        // Network Operations
        NewRule rule47 = repository.createRule(NETWORK_OPERATIONS_KEY)
            .setName("Network operations should be reviewed for security")
            .setHtmlDescription(
                "<p>Network operations expose the application to external systems and should be reviewed.</p>"
                + "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper security measures.</p>"
                + "<h2>What to Review</h2>"
                + "<p>When you see this issue, verify that:</p>"
                + "<ul>"
                + "<li>TLS/SSL is used for sensitive data</li>"
                + "<li>Certificate validation is enabled</li>"
                + "<li>Timeouts are set to prevent hanging</li>"
                + "<li>Authentication is required where appropriate</li>"
                + "<li>Error messages don't leak sensitive information</li>"
                + "</ul>"
                + "<h2>Example Operations to Review</h2>"
                + "<pre>\n"
                + "SocketConnect[\"server.com\", 8080]  (* Check: TLS? Auth? *)\n"
                + "SocketOpen[8080]  (* Check: What data is exposed? *)\n"
                + "WebExecute[session, \"Click\", ...]  (* Check: Auth? Session security? *)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags(TAG_SECURITY, "network");

            rule47.setDebtRemediationFunction(rule47.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // File System Modifications
        NewRule rule48 = repository.createRule(FILE_SYSTEM_MODIFICATIONS_KEY)
            .setName("Destructive file operations should be reviewed")
            .setHtmlDescription(
                "<p>Destructive file system operations should be reviewed for security and safety.</p>"
                + "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper safeguards.</p>"
                + "<h2>What to Review</h2>"
                + "<p>When you see this issue, verify that:</p>"
                + "<ul>"
                + "<li>Path validation prevents directory traversal</li>"
                + "<li>User permissions are checked</li>"
                + "<li>Critical files are protected</li>"
                + "<li>Operations are logged for audit</li>"
                + "<li>Rollback/undo is possible if needed</li>"
                + "</ul>"
                + "<h2>Example Operations to Review</h2>"
                + "<pre>\n"
                + "DeleteFile[path]  (* Check: Path validated? Logged? *)\n"
                + "RenameFile[old, new]  (* Check: Both paths validated? *)\n"
                + "SetFileDate[file, ...]  (* Check: Why modifying timestamps? *)\n"
                + "CopyFile[src, dst]  (* Check: Destination validated? *)\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags(TAG_SECURITY, "file-system");

            rule48.setDebtRemediationFunction(rule48.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        // Environment Variables
        NewRule rule49 = repository.createRule(ENVIRONMENT_VARIABLE_KEY)
            .setName("Environment variable access should be reviewed")
            .setHtmlDescription(
                "<p>Environment variables may contain secrets and should be reviewed for proper handling.</p>"
                + "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure secrets are protected.</p>"
                + "<h2>What to Review</h2>"
                + "<p>When you see this issue, verify that:</p>"
                + "<ul>"
                + "<li>Environment variable values are not logged</li>"
                + "<li>Values are not exposed in error messages</li>"
                + "<li>Secrets are not passed to external systems</li>"
                + "<li>Variables are not used in URLs or queries</li>"
                + "<li>Consider using secure secret management instead</li>"
                + "</ul>"
                + "<h2>Example to Review</h2>"
                + "<pre>\n"
                + "apiKey = Environment[\"SECRET_API_KEY\"]  (* Check: Logged anywhere? *)\n"
                + "URLFetch[\"https://api.com?key=\" <> Environment[\"KEY\"]]  (* Exposed in URL! *)\n"
                + "</pre>"
                + "<h2>Secure Practices</h2>"
                + "<pre>\n"
                + "(* Don't log secrets *)\n"
                + "apiKey = Environment[\"SECRET_API_KEY\"];\n"
                + "(* Use in headers, not URLs *)\n"
                + "URLFetch[url, \"Headers\" -> {\"Authorization\" -> \"Bearer \" <> apiKey}]\n"
                + "</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags(TAG_SECURITY, "secrets");

            rule49.setDebtRemediationFunction(rule49.debtRemediationFunctions().constantPerIssue(TIME_10MIN));
    }

}
