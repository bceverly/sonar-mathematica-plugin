package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ASSIGNMENT_NEVER_READ_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.BUILTIN_NAME_IN_LOCAL_SCOPE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CATCH_WITHOUT_THROW_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CIRCULAR_NEEDS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONDITION_ALWAYS_FALSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONTEXT_CONFLICTS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONTEXT_NOT_FOUND_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DEAD_AFTER_RETURN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FORWARD_REFERENCE_WITHOUT_DECLARATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FUNCTION_DEFINED_NEVER_CALLED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GLOBAL_IN_PACKAGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCONSISTENT_NAMING_CONVENTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LOCAL_SHADOWS_GLOBAL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LOCAL_SHADOWS_PARAMETER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LOOP_VARIABLE_UNUSED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISMATCHED_BEGIN_END_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_IMPORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_PATH_ENTRY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MULTIPLE_DEFINITIONS_SAME_SYMBOL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PARAMETER_SHADOWS_BUILTIN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PRIVATE_CONTEXT_SYMBOL_PUBLIC_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REDEFINED_WITHOUT_USE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.RESERVED_NAME_USAGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SYMBOL_AFTER_ENDPACKAGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SYMBOL_MASKED_BY_IMPORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SYMBOL_NAME_TOO_LONG_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SYMBOL_NAME_TOO_SHORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_DEAD_CODE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PATTERNS;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_READABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_UNUSED;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TEMP_VARIABLE_NOT_TEMP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TYPO_IN_BUILTIN_NAME_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNDEFINED_FUNCTION_CALL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNDEFINED_VARIABLE_REFERENCE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNREACHABLE_AFTER_ABORT_THROW_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_FUNCTION_PARAMETER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_IMPORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_MODULE_VARIABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_OPTIONAL_PARAMETER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_PATTERN_NAME_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_PRIVATE_FUNCTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_WITH_VARIABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.WRONG_CAPITALIZATION_KEY;

/**
 * Unused Code and Naming rule definitions.
 * Covers unused code detection, shadowing, naming conventions, and undefined symbols.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class UnusedCodeAndNamingRulesDefinition {

    private static final String SCOPING = "scoping";
    private static final String IMPORTS = "imports";
    private static final String SHADOWING = "shadowing";
    private static final String NAMING = "naming";
    private static final String BUILT_INS = "built-ins";
    private static final String CONTEXTS = "contexts";
    private static final String PACKAGES = "packages";
    private static final String RUNTIME_ERROR = "runtime-error";

    private UnusedCodeAndNamingRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineUnusedCodeRules(repository);
        defineUnusedCodeRulesContinued(repository);
        defineNamingConventionRules(repository);
        defineNamingConventionRulesContinued(repository);
        defineCodeQualityRules(repository);
        defineCodeQualityRulesContinued(repository);
    }

    /**
     * CHUNK 2 RULE DEFINITIONS (Items 61-100 from ROADMAP_325.md) (Part 1) (13 rules)
     */
    private static void defineUnusedCodeRules(NewRepository repository) {
        // ===== CHUNK 2 RULE DEFINITIONS (Items 61-100 from ROADMAP_325.md) =====

        // Unused Code Detection Rules (Items 61-75)

        NewRule rule160 = repository.createRule(UNUSED_PRIVATE_FUNCTION_KEY)
            .setName("Unused private functions should be removed")
            .setHtmlDescription(
                "<p>Private functions that are never called are dead code and should be removed.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>privateHelper[x_] := x^2  (* Never called *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove unused function *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule160.setDebtRemediationFunction(rule160.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule161 = repository.createRule(UNUSED_FUNCTION_PARAMETER_KEY)
            .setName("Unused function parameters should be removed or prefixed with underscore")
            .setHtmlDescription(
                "<p>Function parameters that are never used in the body may indicate a logic error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>compute[x_, y_] := x^2  (* y is unused *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>compute[x_, _] := x^2  (* Use blank for unused parameter *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, "parameters");

            rule161.setDebtRemediationFunction(rule161.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule162 = repository.createRule(UNUSED_MODULE_VARIABLE_KEY)
            .setName("Unused Module variables should be removed")
            .setHtmlDescription(
                "<p>Variables declared in Module but never used are clutter.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x, y}, x = 5; x^2]  (* y is unused *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{x}, x = 5; x^2]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, SCOPING);

            rule162.setDebtRemediationFunction(rule162.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule163 = repository.createRule(UNUSED_WITH_VARIABLE_KEY)
            .setName("Unused With variables should be removed")
            .setHtmlDescription(
                "<p>Variables declared in With but never used indicate unclear intent.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>With[{a = 1, b = 2}, a^2]  (* b is unused *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>With[{a = 1}, a^2]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, SCOPING);

            rule163.setDebtRemediationFunction(rule163.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule164 = repository.createRule(UNUSED_IMPORT_KEY)
            .setName("Unused package imports should be removed")
            .setHtmlDescription(
                "<p>Importing packages that are never used adds load time and unnecessary dependencies.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Needs[\"MyPackage`\"]  (* No symbols from MyPackage used *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove unused Needs *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, IMPORTS);

            rule164.setDebtRemediationFunction(rule164.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule165 = repository.createRule(UNUSED_PATTERN_NAME_KEY)
            .setName("Unused pattern names should use blank patterns")
            .setHtmlDescription(
                "<p>Named patterns that are never referenced should use unnamed blanks.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_, y_] := x  (* y is named but unused *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_, _] := x  (* Use blank for unused pattern *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, TAG_PATTERNS);

            rule165.setDebtRemediationFunction(rule165.debtRemediationFunctions().constantPerIssue("5min"));

    }

    private static void defineUnusedCodeRulesContinued(NewRepository repository) {
        NewRule rule166 = repository.createRule(UNUSED_OPTIONAL_PARAMETER_KEY)
            .setName("Unused optional parameters should be removed")
            .setHtmlDescription(
                "<p>Optional parameters that are never used even when provided create confusing APIs.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_, opts___] := x  (* opts never used *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := x  (* Remove unused optional *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, "parameters");

            rule166.setDebtRemediationFunction(rule166.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule167 = repository.createRule(DEAD_AFTER_RETURN_KEY)
            .setName("Code after Return statement is unreachable")
            .setHtmlDescription(
                "<p>Code after a Return statement in the same scope will never execute.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := (Return[x]; Print[\"Never executes\"])</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := Return[x]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_DEAD_CODE, "control-flow");

            rule167.setDebtRemediationFunction(rule167.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule168 = repository.createRule(UNREACHABLE_AFTER_ABORT_THROW_KEY)
            .setName("Code after Abort or Throw is unreachable")
            .setHtmlDescription(
                "<p>Code after Abort[] or Throw[] will never execute.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[error, Abort[]]; processData[]  (* Never executes if error *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[!error, processData[]]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_DEAD_CODE, "control-flow");

            rule168.setDebtRemediationFunction(rule168.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule169 = repository.createRule(ASSIGNMENT_NEVER_READ_KEY)
            .setName("Assignment value is never read")
            .setHtmlDescription(
                "<p>Assigning a value that is never read before being overwritten is useless work.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 1; x = 2; Print[x]  (* First assignment wasted *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 2; Print[x]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule169.setDebtRemediationFunction(rule169.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule170 = repository.createRule(FUNCTION_DEFINED_NEVER_CALLED_KEY)
            .setName("Global function defined but never called")
            .setHtmlDescription(
                "<p>Global-scope functions that are never called may be dead code or part of a public API.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>utilityFunction[x_] := x^2  (* Never called anywhere *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove or document as public API *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule170.setDebtRemediationFunction(rule170.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule171 = repository.createRule(REDEFINED_WITHOUT_USE_KEY)
            .setName("Variable redefined without using previous value")
            .setHtmlDescription(
                "<p>Redefining a variable without using its previous value indicates a logic error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = Solve[eq1]; result = Solve[eq2]  (* First solve wasted *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>result = Solve[eq2]  (* Remove first assignment *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("logic-error", TAG_DEAD_CODE);

            rule171.setDebtRemediationFunction(rule171.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule172 = repository.createRule(LOOP_VARIABLE_UNUSED_KEY)
            .setName("Loop iterator variable is never used in body")
            .setHtmlDescription(
                "<p>When the loop iterator is never used, use the simpler form without iterator.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[Print[\"Hello\"], {i, 1, 10}]  (* i is unused *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Do[Print[\"Hello\"], 10]  (* Simpler form *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, "loops");

            rule172.setDebtRemediationFunction(rule172.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * CHUNK 2 RULE DEFINITIONS (Items 61-100 from ROADMAP_325.md) (Part 2) (13 rules)
     */
    private static void defineNamingConventionRules(NewRepository repository) {

        NewRule rule173 = repository.createRule(CATCH_WITHOUT_THROW_KEY)
            .setName("Catch statement without corresponding Throw")
            .setHtmlDescription(
                "<p>A Catch without any Throw in its body is unnecessary overhead.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Catch[result = compute[x]]  (* No Throw in compute *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>result = compute[x]  (* Remove unnecessary Catch *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, "error-handling");

            rule173.setDebtRemediationFunction(rule173.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule174 = repository.createRule(CONDITION_ALWAYS_FALSE_KEY)
            .setName("Condition is always false")
            .setHtmlDescription(
                "<p>Conditions that are always false indicate dead code or logic errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[False, doSomething[]]  (* Never executes *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove dead branch or fix condition *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_DEAD_CODE, "logic-error");

            rule174.setDebtRemediationFunction(rule174.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Shadowing & Naming Rules (Items 76-90)

        NewRule rule175 = repository.createRule(LOCAL_SHADOWS_GLOBAL_KEY)
            .setName("Local variable shadows global variable")
            .setHtmlDescription(
                "<p>Local variables shadowing global variables can be confusing and may be unintended.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>data = {1,2,3};\nModule[{data}, data = {4,5,6}]  (* Shadows global *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{localData}, localData = {4,5,6}]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(SHADOWING, NAMING);

            rule175.setDebtRemediationFunction(rule175.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule176 = repository.createRule(PARAMETER_SHADOWS_BUILTIN_KEY)
            .setName("Parameter shadows built-in function")
            .setHtmlDescription(
                "<p>Parameters that shadow built-in functions will prevent their use and cause confusion.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[List_] := Length[List]  (* Shadows built-in List *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[list_] := Length[list]  (* Use lowercase *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(SHADOWING, BUILT_INS);

            rule176.setDebtRemediationFunction(rule176.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule177 = repository.createRule(LOCAL_SHADOWS_PARAMETER_KEY)
            .setName("Local variable shadows function parameter")
            .setHtmlDescription(
                "<p>Local variables shadowing parameters is confusing and probably an error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := Module[{x}, x = 5; x^2]  (* Shadows parameter *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := Module[{y}, y = 5; y^2]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(SHADOWING, SCOPING);

            rule177.setDebtRemediationFunction(rule177.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule178 = repository.createRule(MULTIPLE_DEFINITIONS_SAME_SYMBOL_KEY)
            .setName("Symbol defined multiple times")
            .setHtmlDescription(
                "<p>Redefining the same symbol multiple times may be intentional (patterns) or an error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := x^2;\nf[x_] := x^3  (* Overwrites previous definition *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_Integer] := x^2;\nf[x_Real] := x^3  (* Pattern-based overloading *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("redefinition", TAG_PATTERNS);

            rule178.setDebtRemediationFunction(rule178.debtRemediationFunctions().constantPerIssue("2min"));

    }

    private static void defineNamingConventionRulesContinued(NewRepository repository) {
        NewRule rule179 = repository.createRule(SYMBOL_NAME_TOO_SHORT_KEY)
            .setName("Symbol name is too short in large function")
            .setHtmlDescription(
                "<p>Single-letter variable names in large functions reduce readability.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>largeFunction[x_] := Module[{a,b,c,d,e}, ...]  (* Many single letters *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>largeFunction[input_] := Module[{result, temp, index}, ...]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(NAMING, TAG_READABILITY);

            rule179.setDebtRemediationFunction(rule179.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule180 = repository.createRule(SYMBOL_NAME_TOO_LONG_KEY)
            .setName("Symbol name exceeds 50 characters")
            .setHtmlDescription(
                "<p>Very long variable names (>50 characters) reduce readability.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>thisIsAReallyLongVariableNameThatExceedsFiftyCharactersAndIsHardToRead = 5</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>computationResult = 5  (* Concise but descriptive *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(NAMING, TAG_READABILITY);

            rule180.setDebtRemediationFunction(rule180.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule181 = repository.createRule(INCONSISTENT_NAMING_CONVENTION_KEY)
            .setName("Inconsistent naming convention (mix of camelCase, snake_case, PascalCase)")
            .setHtmlDescription(
                "<p>Mixing naming conventions reduces code consistency.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>myVariable = 1;\nmy_other_variable = 2;\nMyThirdVariable = 3</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>myVariable = 1;\nmyOtherVariable = 2;\nmyThirdVariable = 3  (* Consistent camelCase *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(NAMING, "consistency");

            rule181.setDebtRemediationFunction(rule181.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule182 = repository.createRule(BUILTIN_NAME_IN_LOCAL_SCOPE_KEY)
            .setName("Built-in function name used in local scope")
            .setHtmlDescription(
                "<p>Using built-in names as local variables is confusing and prevents using those built-ins.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{Map, Apply}, ...]  (* Shadows built-ins *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{mapper, applier}, ...]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(SHADOWING, BUILT_INS);

            rule182.setDebtRemediationFunction(rule182.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule183 = repository.createRule(CONTEXT_CONFLICTS_KEY)
            .setName("Symbol defined in multiple contexts")
            .setHtmlDescription(
                "<p>Symbols defined in multiple contexts cause ambiguity and confusion.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>PackageA`mySymbol = 1;\nPackageB`mySymbol = 2  (* Ambiguous *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Use unique names or proper context management *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(CONTEXTS, "ambiguity");

            rule183.setDebtRemediationFunction(rule183.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule184 = repository.createRule(RESERVED_NAME_USAGE_KEY)
            .setName("Reserved system variable name used")
            .setHtmlDescription(
                "<p>Using reserved names like $SystemID, $Version as variable names can cause issues.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>$SystemID = \"custom\"  (* Overwrites system variable *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>systemID = \"custom\"  (* Use non-reserved name *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("reserved", "system-variables");

            rule184.setDebtRemediationFunction(rule184.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule185 = repository.createRule(PRIVATE_CONTEXT_SYMBOL_PUBLIC_KEY)
            .setName("Private context symbol used from outside package")
            .setHtmlDescription(
                "<p>Symbols in Private` context should not be used from outside the package.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>MyPackage`Private`helperFunction[x]  (* Breaks encapsulation *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Export function or use public API *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("encapsulation", PACKAGES);

            rule185.setDebtRemediationFunction(rule185.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * CHUNK 2 RULE DEFINITIONS (Items 61-100 from ROADMAP_325.md) (Part 3) (14 rules)
     */
    private static void defineCodeQualityRules(NewRepository repository) {

        NewRule rule186 = repository.createRule(MISMATCHED_BEGIN_END_KEY)
            .setName("Mismatched BeginPackage/EndPackage or Begin/End")
            .setHtmlDescription(
                "<p>Mismatched package/context delimiters corrupt the context system.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"]\n(* Missing EndPackage *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"]\n...\nEndPackage[]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags(PACKAGES, CONTEXTS);

            rule186.setDebtRemediationFunction(rule186.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule187 = repository.createRule(SYMBOL_AFTER_ENDPACKAGE_KEY)
            .setName("Symbol defined after EndPackage")
            .setHtmlDescription(
                "<p>Symbols defined after EndPackage[] are in the wrong context.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"]\n...\nEndPackage[]\nf[x_] := x  (* Wrong context *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"]\nf[x_] := x\nEndPackage[]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(PACKAGES, CONTEXTS);

            rule187.setDebtRemediationFunction(rule187.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule188 = repository.createRule(GLOBAL_IN_PACKAGE_KEY)
            .setName("Global context used in package code")
            .setHtmlDescription(
                "<p>Package code should use the package context, not Global`.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Global`temp = 5  (* In package code *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>temp = 5  (* Uses package context *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(PACKAGES, CONTEXTS);

            rule188.setDebtRemediationFunction(rule188.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule189 = repository.createRule(TEMP_VARIABLE_NOT_TEMP_KEY)
            .setName("Variables named 'temp' or 'tmp' used multiple times")
            .setHtmlDescription(
                "<p>Variables named 'temp' or 'tmp' that persist should have better names.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>temp = compute1[];\nresult1 = process[temp];\ntemp = compute2[]  (* Reused multiple times *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>intermediateResult1 = compute1[];\nresult1 = process[intermediateResult1]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(NAMING, TAG_READABILITY);

            rule189.setDebtRemediationFunction(rule189.debtRemediationFunctions().constantPerIssue("2min"));

        // Undefined Symbol Detection Rules (Items 91-100)

        NewRule rule190 = repository.createRule(UNDEFINED_FUNCTION_CALL_KEY)
            .setName("Call to undefined function")
            .setHtmlDescription(
                "<p>Calling a function that is not defined or imported will cause a runtime error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = undefinedFunction[x]  (* Function not defined *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Define function or import package *)\nundefinedFunction[x_] := x^2</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("undefined", RUNTIME_ERROR);

            rule190.setDebtRemediationFunction(rule190.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule191 = repository.createRule(UNDEFINED_VARIABLE_REFERENCE_KEY)
            .setName("Reference to undefined variable")
            .setHtmlDescription(
                "<p>Using a variable before it is defined will return the symbol itself or cause an error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = x + 1  (* x never defined *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5;\nresult = x + 1</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("undefined", RUNTIME_ERROR);

            rule191.setDebtRemediationFunction(rule191.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule192 = repository.createRule(TYPO_IN_BUILTIN_NAME_KEY)
            .setName("Possible typo in built-in function name")
            .setHtmlDescription(
                "<p>Common typos in built-in names like 'Lenght' instead of 'Length'.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>size = Lenght[list]  (* Typo: should be Length *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>size = Length[list]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("typo", BUILT_INS);

            rule192.setDebtRemediationFunction(rule192.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

    }

    private static void defineCodeQualityRulesContinued(NewRepository repository) {
        NewRule rule193 = repository.createRule(WRONG_CAPITALIZATION_KEY)
            .setName("Wrong capitalization of built-in function")
            .setHtmlDescription(
                "<p>Mathematica is case-sensitive; 'length' is not the same as 'Length'.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>size = length[list]  (* Should be Length with capital L *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>size = Length[list]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("capitalization", BUILT_INS);

            rule193.setDebtRemediationFunction(rule193.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule194 = repository.createRule(MISSING_IMPORT_KEY)
            .setName("Missing package import for external symbol")
            .setHtmlDescription(
                "<p>Using package symbols without Needs[] may work in notebook but fail in scripts.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = ExternalPackage`Function[x]  (* Package not imported *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Needs[\"ExternalPackage`\"]\nresult = ExternalPackage`Function[x]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(IMPORTS, PACKAGES);

            rule194.setDebtRemediationFunction(rule194.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule195 = repository.createRule(CONTEXT_NOT_FOUND_KEY)
            .setName("Needs references non-existent context")
            .setHtmlDescription(
                "<p>Attempting to load a package that doesn't exist causes a runtime error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Needs[\"NonExistentPackage`\"]  (* Package doesn't exist *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Use correct package name or create package *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags(IMPORTS, RUNTIME_ERROR);

            rule195.setDebtRemediationFunction(rule195.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule196 = repository.createRule(SYMBOL_MASKED_BY_IMPORT_KEY)
            .setName("Local symbol masked by package import")
            .setHtmlDescription(
                "<p>Importing a package can silently override local symbols with same name.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>myFunction[x_] := x^2;\nNeeds[\"Package`\"]  (* Package also defines myFunction *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Rename local symbol or manage contexts carefully *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(SHADOWING, IMPORTS);

            rule196.setDebtRemediationFunction(rule196.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule197 = repository.createRule(MISSING_PATH_ENTRY_KEY)
            .setName("Get references file not in $Path")
            .setHtmlDescription(
                "<p>Loading a file with Get[] that's not in $Path will cause a runtime error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Get[\"myfile.m\"]  (* File not in $Path *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Get[FileNameJoin[{Directory[], \"myfile.m\"}]]  (* Use absolute path *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(IMPORTS, "file-system");

            rule197.setDebtRemediationFunction(rule197.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule198 = repository.createRule(CIRCULAR_NEEDS_KEY)
            .setName("Circular package dependency detected")
            .setHtmlDescription(
                "<p>Package A needs Package B which needs Package A causes load errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* In PackageA.m *)\nNeeds[\"PackageB`\"]\n(* In PackageB.m *)\nNeeds[\"PackageA`\"]  (* Circular *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Refactor to break circular dependency *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("circular-dependency", PACKAGES);

            rule198.setDebtRemediationFunction(rule198.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule199 = repository.createRule(FORWARD_REFERENCE_WITHOUT_DECLARATION_KEY)
            .setName("Forward reference without explicit declaration")
            .setHtmlDescription(
                "<p>Using a symbol before defining it may fail in a fresh kernel without forward declaration.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>g[x_] := f[x] + 1;\nf[x_] := x^2  (* f used before defined *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_];  (* Forward declaration *)\ng[x_] := f[x] + 1;\nf[x_] := x^2</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("forward-reference", "declaration");

            rule199.setDebtRemediationFunction(rule199.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

}
