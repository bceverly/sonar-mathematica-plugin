package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.BREAK_OUTSIDE_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CODE_AFTER_ABORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CODE_INJECTION_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMMAND_INJECTION_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONDITION_ALWAYS_EVALUATES_SAME_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ELSE_BRANCH_NEVER_TAKEN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EMPTY_CATCH_BLOCK_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EMPTY_IF_BRANCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EXCEPTION_NEVER_THROWN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.HARD_CODED_CREDENTIALS_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.IMPOSSIBLE_PATTERN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INFINITE_LOOP_PROVEN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INSECURE_RANDOMNESS_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LDAP_INJECTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LOOP_NEVER_EXECUTES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MASS_ASSIGNMENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_DEFAULT_CASE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_ELSE_CONSIDERED_HARMFUL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MULTIPLE_RETURNS_MAKE_CODE_UNREACHABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NESTED_IF_DEPTH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATH_TRAVERSAL_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_DEFINITION_SHADOWED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REGEX_DOS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SENSITIVE_DATA_IN_LOGS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_CRITICAL;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MAJOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MINOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SQL_INJECTION_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SSRF_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SWITCH_CASE_SHADOWED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_DEAD_CODE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_READABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_45MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TOO_MANY_RETURN_POINTS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNREACHABLE_BRANCH_ALWAYS_FALSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNREACHABLE_BRANCH_ALWAYS_TRUE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNREACHABLE_CODE_AFTER_RETURN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNSAFE_DESERIALIZATION_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.WEAK_CRYPTOGRAPHY_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.XSS_TAINT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.XXE_TAINT_KEY;

/**
 * Control Flow and Taint Analysis rule definitions.
 * Covers dead code, reachability, taint analysis, and control flow patterns.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class ControlFlowAndTaintRulesDefinition {

    private ControlFlowAndTaintRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineChunk4RuleDefinitions(repository);
        defineChunk4RuleDefinitionsPart2(repository);
        defineChunk4RuleDefinitions2(repository);
        defineChunk4RuleDefinitions2Part2(repository);
        defineChunk4RuleDefinitions3(repository);
        defineChunk4RuleDefinitions3Part2(repository);
    }

    /**
     * CHUNK 4 RULE DEFINITIONS (Items 161-200 from ROADMAP_325.md) (Part 1) (11 rules)
     */
    private static void defineChunk4RuleDefinitions(NewRepository repository) {
        // ===== CHUNK 4 RULE DEFINITIONS (Items 161-200 from ROADMAP_325.md) =====

        // Dead Code & Reachability (Items 161-175)

        NewRule rule236 = repository.createRule(UNREACHABLE_CODE_AFTER_RETURN_KEY)
            .setName("Code after Return[] is unreachable")
            .setHtmlDescription(
                "<p>Code placed after a Return[] statement can never execute and should be removed.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := Module[{},\n"
                + "  If[x &lt; 0, Return[-1]];\n"
                + "  Print[\"Processing\"];  (* Unreachable when x < 0 *)\n"
                + "  Return[x^2];\n"
                + "  Print[\"Done\"]  (* Always unreachable *)\n"
                + "]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := Module[{},\n"
                + "  If[x &lt; 0, Return[-1]];\n"
                + "  Print[\"Processing\"];\n"
                + "  x^2\n"
                + "]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "unreachable");

            rule236.setDebtRemediationFunction(rule236.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule237 = repository.createRule(UNREACHABLE_BRANCH_ALWAYS_TRUE_KEY)
            .setName("If condition always true makes else branch unreachable")
            .setHtmlDescription(
                "<p>When an If condition is always True, the else branch can never execute.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[True, action1[], action2[]]  (* action2 never runs *)\n"
                + "If[1 == 1, \"yes\", \"no\"]  (* \"no\" never returned *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>action1[]  (* Remove the conditional *)\n"
                + "\"yes\"  (* Simplify to constant *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "logic-error");

            rule237.setDebtRemediationFunction(rule237.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule238 = repository.createRule(UNREACHABLE_BRANCH_ALWAYS_FALSE_KEY)
            .setName("If condition always false makes true branch unreachable")
            .setHtmlDescription(
                "<p>When an If condition is always False, the true branch can never execute.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[False, action1[], action2[]]  (* action1 never runs *)\n"
                + "If[1 == 2, \"yes\", \"no\"]  (* \"yes\" never returned *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>action2[]  (* Remove the conditional *)\n"
                + "\"no\"  (* Simplify to constant *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "logic-error");

            rule238.setDebtRemediationFunction(rule238.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule239 = repository.createRule(IMPOSSIBLE_PATTERN_KEY)
            .setName("Pattern can never match any input")
            .setHtmlDescription(
                "<p>Some patterns are impossible to satisfy, making the function definition dead code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_Integer?StringQ] := x  (* Integer can't satisfy StringQ *)\n"
                + "f[x_ /; x &gt; 10 &amp;&amp; x &lt; 5] := x  (* Contradiction *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_Integer] := x  (* Remove contradictory test *)\n"
                + "f[x_ /; x &gt; 10] := x  (* Remove contradiction *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "pattern-matching");

            rule239.setDebtRemediationFunction(rule239.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule240 = repository.createRule(EMPTY_CATCH_BLOCK_ENHANCED_KEY)
            .setName("Catch block with no handlers is pointless")
            .setHtmlDescription(
                "<p>A Catch that never handles any exceptions serves no purpose.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Catch[computation[]]  (* No Throw in computation *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>computation[]  (* Remove unnecessary Catch *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "error-handling");

            rule240.setDebtRemediationFunction(rule240.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule241 = repository.createRule(CONDITION_ALWAYS_EVALUATES_SAME_KEY)
            .setName("Condition always evaluates to the same value")
            .setHtmlDescription(
                "<p>Conditions that always produce the same result indicate logic errors or dead branches.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5;\n"
                + "If[x == 5, ...]  (* Always true at this point *)\n"
                + "While[x != 5, ...]  (* Never executes *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5;\n"
                + "(* Remove always-true condition *)\n"
                + "(* Remove never-executing loop *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("logic-error", "control-flow");

            rule241.setDebtRemediationFunction(rule241.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

    }

    private static void defineChunk4RuleDefinitionsPart2(NewRepository repository) {
        NewRule rule242 = repository.createRule(INFINITE_LOOP_PROVEN_KEY)
            .setName("Loop has no exit condition (proven infinite)")
            .setHtmlDescription(
                "<p>Loops without reachable exit conditions will hang indefinitely.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>While[True, process[]]  (* Never terminates *)\n"
                + "While[x &lt; 10, x--]  (* x decreases, never reaches 10 *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>While[condition[], process[]]  (* Add exit condition *)\n"
                + "While[x &lt; 10, x++]  (* Fix increment direction *)</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("infinite-loop", "hang");

            rule242.setDebtRemediationFunction(rule242.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule243 = repository.createRule(LOOP_NEVER_EXECUTES_KEY)
            .setName("Loop body never executes")
            .setHtmlDescription(
                "<p>Loops with impossible entry conditions are dead code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>While[False, process[]]  (* Never runs *)\n"
                + "Do[action[], {i, 10, 1}]  (* start > end, empty range *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>While[condition[], process[]]  (* Fix condition *)\n"
                + "Do[action[], {i, 1, 10}]  (* Fix range *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "loop");

            rule243.setDebtRemediationFunction(rule243.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule244 = repository.createRule(CODE_AFTER_ABORT_KEY)
            .setName("Code after Abort[] is unreachable")
            .setHtmlDescription(
                "<p>Abort[] immediately terminates evaluation; any following code never runs.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[error, Abort[]];\n"
                + "Print[\"Continuing\"];  (* Unreachable if error *)\n"
                + "Abort[]; Print[\"Done\"]  (* Always unreachable *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[error, Return[$Failed]];\n"
                + "Print[\"Continuing\"];</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "abort");

            rule244.setDebtRemediationFunction(rule244.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule245 = repository.createRule(MULTIPLE_RETURNS_MAKE_CODE_UNREACHABLE_KEY)
            .setName("Early returns make subsequent code unreachable")
            .setHtmlDescription(
                "<p>Multiple return statements can leave code paths unreachable.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := (\n"
                + "  If[x &lt; 0, Return[-1]];\n"
                + "  If[x == 0, Return[0]];\n"
                + "  Return[1];\n"
                + "  cleanup[]  (* Never runs *)\n"
                + ")</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := (\n"
                + "  result = Which[x &lt; 0, -1, x == 0, 0, True, 1];\n"
                + "  cleanup[];\n"
                + "  result\n"
                + ")</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_DEAD_CODE, "return");

            rule245.setDebtRemediationFunction(rule245.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule246 = repository.createRule(ELSE_BRANCH_NEVER_TAKEN_KEY)
            .setName("Else branch is never reachable")
            .setHtmlDescription(
                "<p>When analysis proves the else branch can never execute, it's dead code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5;\n"
                + "If[x == 5, actionA[], actionB[]]  (* actionB never runs *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5;\n"
                + "actionA[]  (* Remove unreachable branch *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "conditional");

            rule246.setDebtRemediationFunction(rule246.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

    /**
     * CHUNK 4 RULE DEFINITIONS (Items 161-200 from ROADMAP_325.md) (Part 2) (11 rules)
     */
    private static void defineChunk4RuleDefinitions2(NewRepository repository) {

        NewRule rule247 = repository.createRule(SWITCH_CASE_SHADOWED_KEY)
            .setName("Switch case is shadowed by earlier more general case")
            .setHtmlDescription(
                "<p>Later Switch cases that can never match due to earlier catch-all cases are dead code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Switch[x,\n"
                + "  _, \"default\",\n"
                + "  5, \"five\"  (* Never matches, _ already caught it *)\n"
                + "]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Switch[x,\n"
                + "  5, \"five\",\n"
                + "  _, \"default\"  (* Default last *)\n"
                + "]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "switch");

            rule247.setDebtRemediationFunction(rule247.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule248 = repository.createRule(PATTERN_DEFINITION_SHADOWED_KEY)
            .setName("Specific pattern definition shadowed by more general one")
            .setHtmlDescription(
                "<p>When a general pattern is defined before a specific one, the specific pattern never matches.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := x^2;  (* General case *)\n"
                + "f[0] := 0;  (* Never matches, x_ already caught it *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[0] := 0;  (* Specific case first *)\n"
                + "f[x_] := x^2;  (* General case last *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_DEAD_CODE, "pattern-matching");

            rule248.setDebtRemediationFunction(rule248.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule249 = repository.createRule(EXCEPTION_NEVER_THROWN_KEY)
            .setName("Catch handles exception tag that is never thrown")
            .setHtmlDescription(
                "<p>Catching exception tags that are never thrown in the protected code is unnecessary.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Catch[normalComputation[], \"error\"]  (* No Throw[_, \"error\"] in code *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>normalComputation[]  (* Remove unnecessary Catch *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_DEAD_CODE, "exception");

            rule249.setDebtRemediationFunction(rule249.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule250 = repository.createRule(BREAK_OUTSIDE_LOOP_KEY)
            .setName("Break[] outside loop context causes runtime error")
            .setHtmlDescription(
                "<p>Break[] is only valid inside Do, While, For loops. Using it elsewhere is an error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := If[x &lt; 0, Break[]];  (* Error: not in loop *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := If[x &lt; 0, Return[$Failed]];  (* Use Return instead *)</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("runtime-error", "control-flow");

            rule250.setDebtRemediationFunction(rule250.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        // Taint Analysis for Security (Items 181-195)

        NewRule rule251 = repository.createRule(SQL_INJECTION_TAINT_KEY)
            .setName("SQL injection: untrusted data flows to SQLExecute")
            .setHtmlDescription(
                "<p>Executing SQL queries with untrusted user input can lead to SQL injection attacks.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>userInput = Import[\"https://example.com/data\", \"String\"];\n"
                + "SQLExecute[conn, \"SELECT * FROM users WHERE name='\" &lt;&gt; userInput &lt;&gt; \"'\"]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>userInput = Import[\"https://example.com/data\", \"String\"];\n"
                + "SQLExecute[conn, \"SELECT * FROM users WHERE name=?\", {userInput}]  (* Parameterized *)</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("sql-injection", "cwe-89", "owasp-a03");

            rule251.setDebtRemediationFunction(rule251.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

    }

    private static void defineChunk4RuleDefinitions2Part2(NewRepository repository) {
        NewRule rule252 = repository.createRule(COMMAND_INJECTION_TAINT_KEY)
            .setName("Command injection: untrusted data flows to RunProcess")
            .setHtmlDescription(
                "<p>Executing system commands with untrusted input can lead to command injection.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>fileName = URLFetch[\"https://evil.com/file\"];\n"
                + "RunProcess[{\"cat\", fileName}]  (* Dangerous *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>fileName = URLFetch[\"https://evil.com/file\"];\n"
                + "If[StringMatchQ[fileName, RegularExpression[\"^[a-zA-Z0-9_.-]+$\"]],\n"
                + "  RunProcess[{\"cat\", fileName}]\n"
                + "]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("command-injection", "cwe-78", "owasp-a03");

            rule252.setDebtRemediationFunction(rule252.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule253 = repository.createRule(CODE_INJECTION_TAINT_KEY)
            .setName("Code injection: untrusted data flows to ToExpression")
            .setHtmlDescription(
                "<p>Evaluating untrusted input as code can lead to arbitrary code execution.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>userCode = Import[\"https://untrusted.com\", \"String\"];\n"
                + "ToExpression[userCode]  (* Executes arbitrary code *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Avoid ToExpression on untrusted data entirely *)\n"
                + "(* Or use sandboxing: ToExpression[userCode, StandardForm, HoldForm] *)</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("code-injection", "cwe-94", "owasp-a03");

            rule253.setDebtRemediationFunction(rule253.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule254 = repository.createRule(PATH_TRAVERSAL_TAINT_KEY)
            .setName("Path traversal: untrusted data flows to file operations")
            .setHtmlDescription(
                "<p>Using untrusted input in file paths can allow access to unauthorized files.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>fileName = URLFetch[\"https://attacker.com/path\"];\n"
                + "Import[fileName]  (* Could be \"../../../etc/passwd\" *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>fileName = URLFetch[\"https://attacker.com/path\"];\n"
                + "fileName = FileNameJoin[{$BaseDirectory, \"data\", FileNameTake[fileName]}];\n"
                + "Import[fileName]  (* Confined to safe directory *)</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("path-traversal", "cwe-22", "owasp-a01");

            rule254.setDebtRemediationFunction(rule254.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule255 = repository.createRule(XSS_TAINT_KEY)
            .setName("XSS: untrusted data in HTML/XML output without sanitization")
            .setHtmlDescription(
                "<p>Embedding untrusted input in HTML/XML can lead to cross-site scripting attacks.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>userName = Import[\"https://form.com/name\", \"String\"];\n"
                + "ExportString[XMLElement[\"p\", {}, {userName}], \"HTML\"]  (* XSS risk *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>userName = Import[\"https://form.com/name\", \"String\"];\n"
                + "safe = StringReplace[userName, {\"&lt;\" -&gt; \"&amp;lt;\", \"&gt;\" -&gt; \"&amp;gt;\"}];\n"
                + "ExportString[XMLElement[\"p\", {}, {safe}], \"HTML\"]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("xss", "cwe-79", "owasp-a03");

            rule255.setDebtRemediationFunction(rule255.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule256 = repository.createRule(LDAP_INJECTION_KEY)
            .setName("LDAP injection: untrusted data in LDAP queries")
            .setHtmlDescription(
                "<p>Using untrusted input in LDAP queries can lead to authentication bypass.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>userName = Import[\"https://form.com/user\", \"String\"];\n"
                + "(* LDAPQuery[\"(uid=\" &lt;&gt; userName &lt;&gt; \")\"] - hypothetical *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Sanitize LDAP special characters: *, (, ), \\, NUL *)\n"
                + "safe = StringReplace[userName, {\"*\" -&gt; \"\\\\*\", \"(\" -&gt; \"\\\\(\"}]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("ldap-injection", "cwe-90", "owasp-a03");

            rule256.setDebtRemediationFunction(rule256.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule257 = repository.createRule(XXE_TAINT_KEY)
            .setName("XXE: XML External Entity attack via untrusted XML")
            .setHtmlDescription(
                "<p>Parsing XML with external entities enabled can expose internal files.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>xml = Import[\"https://attacker.com/xml\", \"XML\"]  (* May contain XXE *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Disable external entity processing or validate XML structure first *)</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("xxe", "cwe-611", "owasp-a05");

            rule257.setDebtRemediationFunction(rule257.debtRemediationFunctions().constantPerIssue(TIME_45MIN));
    }

    /**
     * CHUNK 4 RULE DEFINITIONS (Items 161-200 from ROADMAP_325.md) (Part 3) (13 rules)
     */
    private static void defineChunk4RuleDefinitions3(NewRepository repository) {

        NewRule rule258 = repository.createRule(UNSAFE_DESERIALIZATION_TAINT_KEY)
            .setName("Unsafe deserialization: untrusted data to Import[..., \"MX\"]")
            .setHtmlDescription(
                "<p>Deserializing untrusted MX files can execute arbitrary code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>data = URLFetch[\"https://attacker.com/data.mx\"];\n"
                + "Import[data, \"MX\"]  (* Can execute embedded code *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Never deserialize untrusted MX files *)\n"
                + "(* Use JSON or other safe formats *)</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("deserialization", "cwe-502", "owasp-a08");

            rule258.setDebtRemediationFunction(rule258.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule259 = repository.createRule(SSRF_TAINT_KEY)
            .setName("SSRF: untrusted URLs in URLFetch/URLExecute")
            .setHtmlDescription(
                "<p>Fetching URLs from untrusted input can allow access to internal network resources.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>url = Import[\"https://form.com/url\", \"String\"];\n"
                + "URLFetch[url]  (* Could be \"http://localhost:8080/admin\" *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>url = Import[\"https://form.com/url\", \"String\"];\n"
                + "If[StringStartsQ[url, \"https://trusted.com/\"],\n"
                + "  URLFetch[url]\n"
                + "]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("ssrf", "cwe-918", "owasp-a10");

            rule259.setDebtRemediationFunction(rule259.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule260 = repository.createRule(INSECURE_RANDOMNESS_ENHANCED_KEY)
            .setName("Insecure randomness: RandomInteger for security-sensitive values")
            .setHtmlDescription(
                "<p>RandomInteger uses a predictable PRNG unsuitable for cryptographic purposes.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>sessionToken = RandomInteger[{10^9, 10^10 - 1}]  (* Predictable *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Needs[\"Cryptography`\"];\n"
                + "sessionToken = RandomBytes[16]  (* Cryptographically secure *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("randomness", "cwe-330", "crypto");

            rule260.setDebtRemediationFunction(rule260.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule261 = repository.createRule(WEAK_CRYPTOGRAPHY_ENHANCED_KEY)
            .setName("Weak cryptography: MD5 or SHA1 used for security")
            .setHtmlDescription(
                "<p>MD5 and SHA1 are cryptographically broken and should not be used for security.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>hash = Hash[password, \"MD5\"]  (* Broken algorithm *)\n"
                + "hash = Hash[data, \"SHA1\"]  (* Also broken *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>hash = Hash[password, \"SHA256\"]  (* Use SHA-256 or stronger *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("crypto", "cwe-327", "owasp-a02");

            rule261.setDebtRemediationFunction(rule261.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule262 = repository.createRule(HARD_CODED_CREDENTIALS_TAINT_KEY)
            .setName("Hard-coded credentials: string literals in authentication")
            .setHtmlDescription(
                "<p>Hard-coded passwords and API keys in source code are security risks.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>password = \"admin123\";\n"
                + "DatabaseConnect[\"server\", Username -&gt; \"user\", Password -&gt; password]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>password = Environment[\"DB_PASSWORD\"];\n"
                + "DatabaseConnect[\"server\", Username -&gt; \"user\", Password -&gt; password]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("credentials", "cwe-798", "owasp-a07");

            rule262.setDebtRemediationFunction(rule262.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule263 = repository.createRule(SENSITIVE_DATA_IN_LOGS_KEY)
            .setName("Sensitive data: credentials or tokens in Print/logs")
            .setHtmlDescription(
                "<p>Logging sensitive data exposes it to unauthorized access.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>password = getPassword[];\n"
                + "Print[\"Authenticating with: \", password]  (* Logs password *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>password = getPassword[];\n"
                + "Print[\"Authenticating\"]  (* Don't log sensitive data *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("sensitive-data", "cwe-532", "logging");

            rule263.setDebtRemediationFunction(rule263.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

    }

    private static void defineChunk4RuleDefinitions3Part2(NewRepository repository) {
        NewRule rule264 = repository.createRule(MASS_ASSIGNMENT_KEY)
            .setName("Mass assignment: untrusted association directly used in updates")
            .setHtmlDescription(
                "<p>Using untrusted associations directly in database updates can allow privilege escalation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>userData = Import[\"https://form.com/data\", \"JSON\"];\n"
                + "SQLExecute[conn, \"UPDATE users SET ...\", userData]  (* Could include isAdmin *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>userData = Import[\"https://form.com/data\", \"JSON\"];\n"
                + "allowed = KeyTake[userData, {\"name\", \"email\"}];\n"
                + "SQLExecute[conn, \"UPDATE users SET ...\", allowed]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("mass-assignment", "cwe-915", "owasp-a04");

            rule264.setDebtRemediationFunction(rule264.debtRemediationFunctions().constantPerIssue(TIME_45MIN));

        NewRule rule265 = repository.createRule(REGEX_DOS_KEY)
            .setName("ReDoS: untrusted data in regex can cause catastrophic backtracking")
            .setHtmlDescription(
                "<p>Untrusted input in regex patterns can cause exponential execution time (Regex Denial of Service).</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>pattern = Import[\"https://user.com/pattern\", \"String\"];\n"
                + "StringMatchQ[text, RegularExpression[pattern]]  (* ReDoS risk *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Validate pattern complexity or use fixed patterns only *)\n"
                + "StringMatchQ[text, RegularExpression[\"^[a-z]+$\"]]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("redos", "cwe-1333", "dos");

            rule265.setDebtRemediationFunction(rule265.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        // Additional Control Flow Rules (Items 196-200)

        NewRule rule266 = repository.createRule(MISSING_DEFAULT_CASE_KEY)
            .setName("Switch without default case may return unevaluated")
            .setHtmlDescription(
                "<p>Switch statements without a default case can return the Switch expression unevaluated.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = Switch[x, 1, \"one\", 2, \"two\"]  (* Returns Switch[x, ...] if x is 3 *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>result = Switch[x, 1, \"one\", 2, \"two\", _, \"other\"]  (* Add default *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("switch", "completeness");

            rule266.setDebtRemediationFunction(rule266.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule267 = repository.createRule(EMPTY_IF_BRANCH_KEY)
            .setName("Empty If true branch should be inverted")
            .setHtmlDescription(
                "<p>If statements with empty true branches are confusing; invert the condition instead.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[condition, , elseAction[]]  (* Empty true branch *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[!condition, elseAction[]]  (* Inverted, clearer *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_READABILITY, "conditional");

            rule267.setDebtRemediationFunction(rule267.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule268 = repository.createRule(NESTED_IF_DEPTH_KEY)
            .setName("Deeply nested If statements (>4 levels) are hard to understand")
            .setHtmlDescription(
                "<p>Functions with deeply nested conditionals are difficult to reason about and test.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[a, If[b, If[c, If[d, If[e, action[]]]]]]  (* 5 levels deep *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[a &amp;&amp; b &amp;&amp; c &amp;&amp; d &amp;&amp; e, action[]]  (* Flatten conditions *)\n"
                + "(* Or use Which for multiple cases *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("complexity", "nesting");

            rule268.setDebtRemediationFunction(rule268.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule269 = repository.createRule(TOO_MANY_RETURN_POINTS_KEY)
            .setName("Function with more than 5 Return statements is hard to reason about")
            .setHtmlDescription(
                "<p>Functions with many return points have complex control flow.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := (\n"
                + "  If[x &lt; 0, Return[-1]];\n"
                + "  If[x == 0, Return[0]];\n"
                + "  If[x &lt; 10, Return[1]];\n"
                + "  If[x &lt; 100, Return[2]];\n"
                + "  If[x &lt; 1000, Return[3]];\n"
                + "  Return[4]  (* 6 return points *)\n"
                + ")</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := Which[\n"
                + "  x &lt; 0, -1,\n"
                + "  x == 0, 0,\n"
                + "  x &lt; 10, 1,\n"
                + "  x &lt; 100, 2,\n"
                + "  x &lt; 1000, 3,\n"
                + "  True, 4\n"
                + "]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("complexity", "return");

            rule269.setDebtRemediationFunction(rule269.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule270 = repository.createRule(MISSING_ELSE_CONSIDERED_HARMFUL_KEY)
            .setName("If without else can have unclear intent")
            .setHtmlDescription(
                "<p>If statements without else branches may have unclear behavior when the condition is false.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[condition, action[]]  (* What happens when false? *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* If side effect is intended: *)\n"
                + "If[condition, action[], Null]  (* Explicit no-op *)\n"
                + "(* Or use pattern: *)\n"
                + "If[condition, action[]; result, result]</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("clarity", "conditional");

            rule270.setDebtRemediationFunction(rule270.debtRemediationFunctions().constantPerIssue("2min"));
    }

}
