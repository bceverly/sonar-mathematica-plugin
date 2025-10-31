package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ABORT_IN_LIBRARY_CODE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ALL_SPECIFICATION_INEFFICIENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.BOOLEAN_EXPRESSION_ALWAYS_FALSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.BOOLEAN_EXPRESSION_ALWAYS_TRUE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CATCH_ALL_EXCEPTION_HANDLER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CHECK_PATTERN_DOESNT_HANDLE_ALL_CASES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMPARISON_OF_IDENTICAL_EXPRESSIONS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMPARISON_WITH_NULL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMPLEX_BOOLEAN_EXPRESSION_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONDITION_ALWAYS_FALSE_CONSTANT_PROPAGATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONDITION_ALWAYS_TRUE_CONSTANT_PROPAGATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONSTANT_EXPRESSION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DE_MORGANS_LAW_OPPORTUNITY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DOUBLE_NEGATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EMPTY_EXCEPTION_HANDLER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EVALUATE_IN_HELD_CONTEXT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FLAT_ATTRIBUTE_MISUSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.HOLD_ATTRIBUTE_MISSING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.HOLD_FIRST_BUT_USES_SECOND_ARGUMENT_FIRST_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.IDENTITY_OPERATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCONSISTENT_NULL_HANDLING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LOOP_BOUND_CONSTANT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MESSAGE_WITHOUT_DEFINITION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_ATTRIBUTES_DECLARATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_CHECK_LEADS_TO_NULL_PROPAGATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_MESSAGE_DEFINITION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_NULL_CHECK_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_SEQUENCE_WRAPPER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_UNEVALUATED_WRAPPER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NULL_DEREFERENCE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NULL_PASSED_TO_NON_NULLABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NULL_RETURN_NOT_DOCUMENTED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.OFF_DISABLING_IMPORTANT_WARNINGS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ONE_IDENTITY_ATTRIBUTE_MISUSE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ORDERLESS_ATTRIBUTE_ON_NON_COMMUTATIVE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PART_SPECIFICATION_OUT_OF_BOUNDS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_WITH_SIDE_EFFECT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PURE_EXPRESSION_IN_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.QUIET_SUPPRESSING_IMPORTANT_MESSAGES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REDUNDANT_COMPUTATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.RELEASE_HOLD_AFTER_HOLD_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPLACEMENT_RULE_ORDER_MATTERS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPLACE_ALL_VS_REPLACE_CONFUSION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.RULE_DOESNT_MATCH_DUE_TO_EVALUATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEQUENCE_IN_UNEXPECTED_CONTEXT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_CRITICAL;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MAJOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MINOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SPAN_SPECIFICATION_INVALID_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_DEAD_CODE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PATTERNS;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PERFORMANCE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_READABILITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.THREADING_OVER_NON_LISTS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.THROW_WITHOUT_CATCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNNECESSARY_BOOLEAN_CONVERSION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNNECESSARY_HOLD_KEY;

/**
 * Null Safety and Resource Management rule definitions.
 * Covers null safety, exception handling, and resource management patterns.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class NullSafetyAndResourceRulesDefinition {

    private NullSafetyAndResourceRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineNullSafetyRules(repository);
        defineNullSafetyRulesContinued(repository);
        defineResourceManagementRules(repository);
        defineResourceManagementRulesContinued(repository);
        defineErrorHandlingRules(repository);
        defineErrorHandlingRulesContinued(repository);
        defineStateManagementRules(repository);
        defineStateManagementRulesContinued(repository);
    }

    /**
     * CHUNK 6 RULE DEFINITIONS (Items 251-300) (Part 1) (12 rules)
     */
    private static void defineNullSafetyRules(NewRepository repository) {
        // ===== CHUNK 6 RULE DEFINITIONS (Items 251-300) =====

        // Null Safety (Items 251-265)

        NewRule rule311 = repository.createRule(NULL_DEREFERENCE_KEY)
            .setName("Null dereference causes runtime error")
            .setHtmlDescription(
                "<p>Accessing properties or methods of Null causes runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result[[1]]  (* If result is Null, this fails *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[result =!= Null, result[[1]], defaultValue]</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("null-safety", "runtime-error");

            rule311.setDebtRemediationFunction(rule311.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule312 = repository.createRule(MISSING_NULL_CHECK_KEY)
            .setName("Missing null check before usage")
            .setHtmlDescription(
                "<p>Function parameters should be checked for Null before use.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>ProcessData[data_] := data[[1]]  (* No null check *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>ProcessData[data_] := If[data === Null, Null, data[[1]]]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("null-safety");

            rule312.setDebtRemediationFunction(rule312.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule313 = repository.createRule(NULL_PASSED_TO_NON_NULLABLE_KEY)
            .setName("Null passed to parameter expecting non-null value")
            .setHtmlDescription(
                "<p>Passing Null to functions that expect non-null values causes errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Length[Null]  (* Error *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[data =!= Null, Length[data], 0]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("null-safety");

            rule313.setDebtRemediationFunction(rule313.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule314 = repository.createRule(INCONSISTENT_NULL_HANDLING_KEY)
            .setName("Inconsistent null handling across branches")
            .setHtmlDescription(
                "<p>Handle Null consistently across all code paths.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[cond, process[x], x]  (* x might be Null in else *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[cond && x =!= Null, process[x], defaultValue]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("null-safety", "consistency");

            rule314.setDebtRemediationFunction(rule314.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule315 = repository.createRule(NULL_RETURN_NOT_DOCUMENTED_KEY)
            .setName("Function returns Null without documenting it")
            .setHtmlDescription(
                "<p>Document when functions can return Null to avoid surprises.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>FindUser[id_] := If[..., userData, Null]  (* Not documented *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>FindUser::usage = \"Returns user data or Null if not found.\";</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation", "null-safety");

            rule315.setDebtRemediationFunction(rule315.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule316 = repository.createRule(COMPARISON_WITH_NULL_KEY)
            .setName("Use === for Null comparison, not ==")
            .setHtmlDescription(
                "<p>Use SameQ (===) instead of Equal (==) for Null comparisons.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[x == Null, ...]  (* Wrong *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[x === Null, ...]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("null-safety", "semantics");

            rule316.setDebtRemediationFunction(rule316.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

    }

    private static void defineNullSafetyRulesContinued(NewRepository repository) {
        NewRule rule317 = repository.createRule(MISSING_CHECK_LEADS_TO_NULL_PROPAGATION_KEY)
            .setName("Missing null check causes error cascade")
            .setHtmlDescription(
                "<p>Null propagating through operations causes cascading errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = f[g[h[x]]]  (* If h returns Null, all fail *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Check each step for Null</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("null-safety", "error-handling");

            rule317.setDebtRemediationFunction(rule317.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule318 = repository.createRule(CHECK_PATTERN_DOESNT_HANDLE_ALL_CASES_KEY)
            .setName("Check pattern missing error cases")
            .setHtmlDescription(
                "<p>Check[] should handle all possible error conditions.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Check[expr, fallback]  (* What about other errors? *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Check[expr, fallback, {f::error1, f::error2}]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling");

            rule318.setDebtRemediationFunction(rule318.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule319 = repository.createRule(QUIET_SUPPRESSING_IMPORTANT_MESSAGES_KEY)
            .setName("Quiet suppresses critical error messages")
            .setHtmlDescription(
                "<p>Quiet[] can hide important errors. Be specific about what to suppress.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Quiet[ImportantOperation[]]  (* Hides all messages *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Quiet[operation[], {f::msg1}]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling", "bad-practice");

            rule319.setDebtRemediationFunction(rule319.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule320 = repository.createRule(OFF_DISABLING_IMPORTANT_WARNINGS_KEY)
            .setName("Off[] disables important warnings")
            .setHtmlDescription(
                "<p>Disabling warnings with Off[] can mask real problems.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Off[General::stop]  (* Masks errors *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Fix the underlying issue instead</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling", "bad-practice");

            rule320.setDebtRemediationFunction(rule320.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule321 = repository.createRule(CATCH_ALL_EXCEPTION_HANDLER_KEY)
            .setName("Catch-all exception handler is too broad")
            .setHtmlDescription(
                "<p>Catch[] without specific tag catches everything, including intended throws.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Catch[expr]  (* Catches all tags *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Catch[expr, \"myTag\"]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling");

            rule321.setDebtRemediationFunction(rule321.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule322 = repository.createRule(EMPTY_EXCEPTION_HANDLER_KEY)
            .setName("Empty exception handler silently ignores errors")
            .setHtmlDescription(
                "<p>Catching exceptions and doing nothing loses error information.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Catch[expr, _, Null &]  (* Silently ignores *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Log or handle the error appropriately</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling", "bad-practice");

            rule322.setDebtRemediationFunction(rule322.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

    /**
     * CHUNK 6 RULE DEFINITIONS (Items 251-300) (Part 2) (12 rules)
     */
    private static void defineResourceManagementRules(NewRepository repository) {

        NewRule rule323 = repository.createRule(THROW_WITHOUT_CATCH_KEY)
            .setName("Throw without surrounding Catch aborts evaluation")
            .setHtmlDescription(
                "<p>Throw[] without Catch will abort the entire evaluation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Throw[\"error\"]  (* No catch *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Catch[code, tag]; Throw[\"error\", tag]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling");

            rule323.setDebtRemediationFunction(rule323.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule324 = repository.createRule(ABORT_IN_LIBRARY_CODE_KEY)
            .setName("Abort[] in library code is too aggressive")
            .setHtmlDescription(
                "<p>Library functions should return errors, not call Abort[].</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>MyLibFunc[x_] := If[invalid[x], Abort[], ...]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Return $Failed or Throw with tag</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling", "library-design");

            rule324.setDebtRemediationFunction(rule324.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule325 = repository.createRule(MESSAGE_WITHOUT_DEFINITION_KEY)
            .setName("Message issued but not defined")
            .setHtmlDescription(
                "<p>Define message templates before issuing messages.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Message[f::undefined]  (* Message not defined *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f::undefined = \"Error: undefined value\";\n"
                + "Message[f::undefined]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("error-handling", "messaging");

            rule325.setDebtRemediationFunction(rule325.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule326 = repository.createRule(MISSING_MESSAGE_DEFINITION_KEY)
            .setName("Function issues messages without defining them")
            .setHtmlDescription(
                "<p>All messages should be defined before use for clarity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Function issues messages without ::msg definitions</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f::err = \"Error message template\";</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation", "messaging");

            rule326.setDebtRemediationFunction(rule326.debtRemediationFunctions().constantPerIssue("5min"));

        // Constant & Expression Analysis (Items 267-280)

        NewRule rule327 = repository.createRule(CONDITION_ALWAYS_TRUE_CONSTANT_PROPAGATION_KEY)
            .setName("Condition always evaluates to True")
            .setHtmlDescription(
                "<p>Constant propagation reveals condition that's always True.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5; If[x > 0, ...]  (* Always True *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Remove dead branch or fix logic</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("constant-propagation", TAG_DEAD_CODE);

            rule327.setDebtRemediationFunction(rule327.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule328 = repository.createRule(CONDITION_ALWAYS_FALSE_CONSTANT_PROPAGATION_KEY)
            .setName("Condition always evaluates to False")
            .setHtmlDescription(
                "<p>Constant propagation reveals condition that's always False.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = -5; If[x > 0, ...]  (* Always False *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Remove dead branch or fix logic</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("constant-propagation", TAG_DEAD_CODE);

            rule328.setDebtRemediationFunction(rule328.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

    }

    private static void defineResourceManagementRulesContinued(NewRepository repository) {
        NewRule rule329 = repository.createRule(LOOP_BOUND_CONSTANT_KEY)
            .setName("Loop bound is constant - use literal")
            .setHtmlDescription(
                "<p>If loop bound is constant, use the literal value for clarity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>n = 100; Do[..., {i, 1, n}]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Do[..., {i, 1, 100}]</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("clarity");

            rule329.setDebtRemediationFunction(rule329.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule330 = repository.createRule(REDUNDANT_COMPUTATION_KEY)
            .setName("Same expression computed multiple times")
            .setHtmlDescription(
                "<p>Cache results of expensive computations.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x] + f[x] + f[x]  (* f called 3 times *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>result = f[x]; result + result + result</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "caching");

            rule330.setDebtRemediationFunction(rule330.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule331 = repository.createRule(PURE_EXPRESSION_IN_LOOP_KEY)
            .setName("Pure expression computed in every iteration")
            .setHtmlDescription(
                "<p>Hoist side-effect-free expressions outside loops.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[... + Sqrt[2] * ..., {i, 1, n}]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>val = Sqrt[2]; Do[... + val * ..., {i, 1, n}]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "loop-optimization");

            rule331.setDebtRemediationFunction(rule331.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule332 = repository.createRule(CONSTANT_EXPRESSION_KEY)
            .setName("Constant expression should be simplified")
            .setHtmlDescription(
                "<p>Simplify x + 0, x * 1, x^1 to just x.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result * 1 + 0</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>result</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("simplification");

            rule332.setDebtRemediationFunction(rule332.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule333 = repository.createRule(IDENTITY_OPERATION_KEY)
            .setName("Identity operation has no effect")
            .setHtmlDescription(
                "<p>Reverse[Reverse[x]] or Transpose[Transpose[x]] equals x.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Reverse[Reverse[list]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>list</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("simplification");

            rule333.setDebtRemediationFunction(rule333.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule334 = repository.createRule(COMPARISON_OF_IDENTICAL_EXPRESSIONS_KEY)
            .setName("Comparing identical expressions")
            .setHtmlDescription(
                "<p>x == x is always True (unless x is a pattern).</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[value == value, ...]  (* Always True *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Check for typo or logic error</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("logic-error");

            rule334.setDebtRemediationFunction(rule334.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

    /**
     * CHUNK 6 RULE DEFINITIONS (Items 251-300) (Part 3) (12 rules)
     */
    private static void defineErrorHandlingRules(NewRepository repository) {

        NewRule rule335 = repository.createRule(BOOLEAN_EXPRESSION_ALWAYS_TRUE_KEY)
            .setName("Boolean expression is tautology")
            .setHtmlDescription(
                "<p>Expression like x || !x is always True.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[x || !x, ...]  (* Tautology *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Fix logic error</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("logic-error");

            rule335.setDebtRemediationFunction(rule335.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule336 = repository.createRule(BOOLEAN_EXPRESSION_ALWAYS_FALSE_KEY)
            .setName("Boolean expression is contradiction")
            .setHtmlDescription(
                "<p>Expression like x && !x is always False.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[x && !x, ...]  (* Contradiction *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Fix logic error</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("logic-error");

            rule336.setDebtRemediationFunction(rule336.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule337 = repository.createRule(UNNECESSARY_BOOLEAN_CONVERSION_KEY)
            .setName("Unnecessary boolean conversion")
            .setHtmlDescription(
                "<p>If[cond, True, False] should just be cond.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[x > 0, True, False]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x > 0</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("simplification");

            rule337.setDebtRemediationFunction(rule337.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule338 = repository.createRule(DOUBLE_NEGATION_KEY)
            .setName("Double negation should be simplified")
            .setHtmlDescription(
                "<p>!!x or Not[Not[x]] should be simplified to x.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Not[Not[condition]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>condition</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("simplification");

            rule338.setDebtRemediationFunction(rule338.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule339 = repository.createRule(COMPLEX_BOOLEAN_EXPRESSION_ENHANCED_KEY)
            .setName("Boolean expression too complex")
            .setHtmlDescription(
                "<p>Boolean expressions with >5 operators are hard to understand.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>a && b || c && d || e && f && g</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Break into intermediate variables</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("complexity", TAG_READABILITY);

            rule339.setDebtRemediationFunction(rule339.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule340 = repository.createRule(DE_MORGANS_LAW_OPPORTUNITY_KEY)
            .setName("De Morgan's Law could improve clarity")
            .setHtmlDescription(
                "<p>!(a && b) could be !a || !b for better readability.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>!(valid && ready)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>!valid || !ready</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("clarity");

            rule340.setDebtRemediationFunction(rule340.debtRemediationFunctions().constantPerIssue("2min"));

        // Mathematica-Specific Patterns (Items 281-300)

    }

    private static void defineErrorHandlingRulesContinued(NewRepository repository) {
        NewRule rule341 = repository.createRule(HOLD_ATTRIBUTE_MISSING_KEY)
            .setName("Function manipulates unevaluated expressions without Hold attribute")
            .setHtmlDescription(
                "<p>Functions that manipulate expressions should have Hold attributes.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>MyHold[x_] := Hold[x]  (* x already evaluated! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>SetAttributes[MyHold, HoldAll]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("evaluation", "hold");

            rule341.setDebtRemediationFunction(rule341.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule342 = repository.createRule(HOLD_FIRST_BUT_USES_SECOND_ARGUMENT_FIRST_KEY)
            .setName("HoldFirst but uses second argument first")
            .setHtmlDescription(
                "<p>Function with HoldFirst shouldn't evaluate second argument first.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>SetAttributes[f, HoldFirst];\n"
                + "f[x_, y_] := evaluate[y]  (* y evaluates first! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Use HoldAll or fix evaluation order</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("evaluation", "hold");

            rule342.setDebtRemediationFunction(rule342.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule343 = repository.createRule(MISSING_UNEVALUATED_WRAPPER_KEY)
            .setName("Missing Unevaluated wrapper causes premature evaluation")
            .setHtmlDescription(
                "<p>Pass unevaluated expressions with Unevaluated wrapper.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>heldFunc[x + 1]  (* x + 1 evaluates first *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>heldFunc[Unevaluated[x + 1]]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("evaluation");

            rule343.setDebtRemediationFunction(rule343.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule344 = repository.createRule(UNNECESSARY_HOLD_KEY)
            .setName("Unnecessary Hold on literal")
            .setHtmlDescription(
                "<p>Hold[5] is redundant; literals don't evaluate.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Hold[42]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>42</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("simplification");

            rule344.setDebtRemediationFunction(rule344.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule345 = repository.createRule(RELEASE_HOLD_AFTER_HOLD_KEY)
            .setName("ReleaseHold after Hold is redundant")
            .setHtmlDescription(
                "<p>ReleaseHold[Hold[x]] is just x.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>ReleaseHold[Hold[expr]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>expr</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("simplification");

            rule345.setDebtRemediationFunction(rule345.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule346 = repository.createRule(EVALUATE_IN_HELD_CONTEXT_KEY)
            .setName("Evaluate in held context may not be intended")
            .setHtmlDescription(
                "<p>Evaluate inside Hold creates evaluation leak.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Hold[x, Evaluate[y], z]  (* y evaluates! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Ensure this is intentional</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("evaluation", "hold");

            rule346.setDebtRemediationFunction(rule346.debtRemediationFunctions().constantPerIssue(TIME_10MIN));
    }

    /**
     * CHUNK 6 RULE DEFINITIONS (Items 251-300) (Part 4) (14 rules)
     */
    private static void defineStateManagementRules(NewRepository repository) {

        NewRule rule347 = repository.createRule(PATTERN_WITH_SIDE_EFFECT_KEY)
            .setName("Pattern test with side effects evaluated multiple times")
            .setHtmlDescription(
                "<p>Pattern tests can be evaluated multiple times during matching.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x_?(Print[#]; True &)  (* Prints multiple times *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Use pure pattern test without side effects</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS, "side-effects");

            rule347.setDebtRemediationFunction(rule347.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule348 = repository.createRule(REPLACEMENT_RULE_ORDER_MATTERS_KEY)
            .setName("Replacement rule order affects result")
            .setHtmlDescription(
                "<p>Order of replacement rules matters; specific should come before general.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>{_ -> 0, 2 -> 5}  (* Catch-all first! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>{2 -> 5, _ -> 0}</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS, "replacement-rules");

            rule348.setDebtRemediationFunction(rule348.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule349 = repository.createRule(REPLACE_ALL_VS_REPLACE_CONFUSION_KEY)
            .setName("ReplaceAll vs Replace confusion")
            .setHtmlDescription(
                "<p>ReplaceAll (/.) and Replace have different semantics.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>list /. rules  (* May not be intended *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Replace[list, rules, {1}]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("replacement-rules");

            rule349.setDebtRemediationFunction(rule349.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule350 = repository.createRule(RULE_DOESNT_MATCH_DUE_TO_EVALUATION_KEY)
            .setName("Rule won't match due to evaluation timing")
            .setHtmlDescription(
                "<p>Rule may not match due to when expressions evaluate.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x /. {1 + 1 -> 5}  (* Won't match 2 *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x /. {2 -> 5}</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("replacement-rules", "evaluation");

            rule350.setDebtRemediationFunction(rule350.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule351 = repository.createRule(PART_SPECIFICATION_OUT_OF_BOUNDS_KEY)
            .setName("Part specification out of bounds")
            .setHtmlDescription(
                "<p>Accessing list[[100]] when list has <100 elements causes error.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>list = {1, 2, 3}; list[[10]]  (* Error! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Check length first or use Take/Drop</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("bounds-check", "runtime-error");

            rule351.setDebtRemediationFunction(rule351.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule352 = repository.createRule(SPAN_SPECIFICATION_INVALID_KEY)
            .setName("Span specification is invalid")
            .setHtmlDescription(
                "<p>Backward spans like list[[10;;1]] produce empty result.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>list[[10;;1]]  (* Backward span *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>list[[1;;10]]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("spans");

            rule352.setDebtRemediationFunction(rule352.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule353 = repository.createRule(ALL_SPECIFICATION_INEFFICIENT_KEY)
            .setName("Using [[All]] is redundant")
            .setHtmlDescription(
                "<p>list[[All]] is just list.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>list[[All]]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>list</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("simplification");

            rule353.setDebtRemediationFunction(rule353.debtRemediationFunctions().constantPerIssue("2min"));

    }

    private static void defineStateManagementRulesContinued(NewRepository repository) {
        NewRule rule354 = repository.createRule(THREADING_OVER_NON_LISTS_KEY)
            .setName("Threading over non-list with Listable attribute")
            .setHtmlDescription(
                "<p>Listable functions thread over lists; unexpected on scalars.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>listableFunc[5]  (* Returns {result} not result *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Ensure input type matches expectation</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("listable", "attributes");

            rule354.setDebtRemediationFunction(rule354.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule355 = repository.createRule(MISSING_ATTRIBUTES_DECLARATION_KEY)
            .setName("Function should have Listable attribute")
            .setHtmlDescription(
                "<p>Functions that map element-wise should have Listable.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_List] := Map[operation, x]</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>SetAttributes[f, Listable]; f[x_] := operation[x]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PERFORMANCE, "attributes");

            rule355.setDebtRemediationFunction(rule355.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule356 = repository.createRule(ONE_IDENTITY_ATTRIBUTE_MISUSE_KEY)
            .setName("OneIdentity attribute causes subtle issues")
            .setHtmlDescription(
                "<p>OneIdentity changes pattern matching semantics subtly.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>SetAttributes[f, OneIdentity]  (* Be very careful *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Only use when truly needed</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("attributes", "semantics");

            rule356.setDebtRemediationFunction(rule356.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule357 = repository.createRule(ORDERLESS_ATTRIBUTE_ON_NON_COMMUTATIVE_KEY)
            .setName("Orderless on non-commutative operation")
            .setHtmlDescription(
                "<p>Orderless should only be used on commutative operations.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>SetAttributes[subtract, Orderless]  (* Wrong! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Remove Orderless attribute</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("attributes", "semantics");

            rule357.setDebtRemediationFunction(rule357.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule358 = repository.createRule(FLAT_ATTRIBUTE_MISUSE_KEY)
            .setName("Flat attribute on non-associative operation")
            .setHtmlDescription(
                "<p>Flat should only be used on associative operations.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>SetAttributes[divide, Flat]  (* Wrong! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Remove Flat attribute</pre>"
            )
            .setSeverity(SEVERITY_CRITICAL)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("attributes", "semantics");

            rule358.setDebtRemediationFunction(rule358.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule359 = repository.createRule(SEQUENCE_IN_UNEXPECTED_CONTEXT_KEY)
            .setName("Sequence flattens unexpectedly")
            .setHtmlDescription(
                "<p>Sequence[] flattens into its context, possibly losing structure.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>{1, Sequence[2, 3], 4}  (* Becomes {1, 2, 3, 4} *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Be aware of flattening behavior</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("sequence", "structure");

            rule359.setDebtRemediationFunction(rule359.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule360 = repository.createRule(MISSING_SEQUENCE_WRAPPER_KEY)
            .setName("Should use Sequence to avoid extra nesting")
            .setHtmlDescription(
                "<p>Use Sequence[] to flatten results into parent expression.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[cond, {a, b}, {}]  (* Empty list creates nesting *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[cond, Sequence[a, b], Sequence[]]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("sequence", "idiom");

            rule360.setDebtRemediationFunction(rule360.debtRemediationFunctions().constantPerIssue("5min"));
    }

}
