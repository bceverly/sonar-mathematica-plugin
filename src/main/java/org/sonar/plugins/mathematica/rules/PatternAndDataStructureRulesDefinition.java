package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ALTERNATIVES_TOO_COMPLEX_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ASSOCIATETO_ON_NON_SYMBOL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ASSOCIATION_UPDATE_PATTERN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ASSOCIATION_VS_LIST_CONFUSION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.BLANKSEQUENCE_WITHOUT_RESTRICTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.EMPTY_LIST_INDEXING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GROUPBY_WITHOUT_AGGREGATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.HOLDPATTERN_UNNECESSARY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INEFFICIENT_KEY_LOOKUP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INEFFICIENT_LIST_CONCATENATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.KEYDROP_MULTIPLE_TIMES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LENGTH_IN_LOOP_CONDITION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LONGEST_SHORTEST_WITHOUT_ORDERING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LOOKUP_WITH_MISSING_DEFAULT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MERGE_WITHOUT_CONFLICT_STRATEGY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_KEY_CHECK_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_PATTERN_DEFAULTS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NEGATIVE_INDEX_WITHOUT_VALIDATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NESTED_OPTIONAL_PATTERNS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NESTED_PART_EXTRACTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ORDER_DEPENDENT_PATTERNS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PART_ASSIGNMENT_TO_IMMUTABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_MATCHING_LARGE_LISTS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_NAMING_CONFLICTS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_REPEATED_DIFFERENT_TYPES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_TEST_PURE_FUNCTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_TEST_VS_CONDITION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.POSITION_VS_SELECT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.QUERY_ON_NON_DATASET_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPEATED_PATTERN_ALTERNATIVES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REVERSE_TWICE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MAJOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SEVERITY_MINOR;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SORT_WITHOUT_COMPARISON_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PATTERNS;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PERFORMANCE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_15MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNNECESSARY_FLATTEN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNRESTRICTED_BLANK_PATTERN_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VERBATIM_PATTERN_MISUSE_KEY;

/**
 * Pattern and Data Structure rule definitions.
 * Covers pattern matching, list/array operations, and association rules.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class PatternAndDataStructureRulesDefinition {

    private PatternAndDataStructureRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineChunk1Rules(repository);
        defineChunk1Rules2(repository);
        defineChunk1Rules3(repository);
    }

    /**
     * CHUNK 1 RULES (Items 16-50 from ROADMAP_325.md) (Part 1) (11 rules)
     */
    private static void defineChunk1Rules(NewRepository repository) {
        // ===== CHUNK 1 RULES (Items 16-50 from ROADMAP_325.md) =====

        // Pattern System Rules (Items 16-30)
        NewRule rule125 = repository.createRule(UNRESTRICTED_BLANK_PATTERN_KEY)
            .setName("Blank patterns should have type restrictions when appropriate")
            .setHtmlDescription(
                "<p>Unrestricted blank patterns like <code>f[x_] := ...</code> accept any type, potentially causing runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := x^2  (* Fails on non-numeric input *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_?NumericQ] := x^2  (* Type-safe *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PATTERNS, "type-safety");

            rule125.setDebtRemediationFunction(rule125.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule126 = repository.createRule(PATTERN_TEST_VS_CONDITION_KEY)
            .setName("PatternTest (?) is more efficient than Condition (/;) for simple tests")
            .setHtmlDescription(
                "<p>PatternTest (<code>?</code>) evaluates during pattern matching, while Condition (<code>/;</code>) evaluates after.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_ /; IntegerQ[x]] := x  (* Inefficient *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_?IntegerQ] := x  (* More efficient *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS, TAG_PERFORMANCE);

            rule126.setDebtRemediationFunction(rule126.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule127 = repository.createRule(BLANKSEQUENCE_WITHOUT_RESTRICTION_KEY)
            .setName("BlankSequence should have type restrictions when possible")
            .setHtmlDescription(
                "<p>Unrestricted <code>x__</code> patterns can match inappropriate sequences.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>sum[x__] := Total[{x}]  (* No type check *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>sum[x__?NumericQ] := Total[{x}]  (* Type-safe *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PATTERNS, TAG_PERFORMANCE);

            rule127.setDebtRemediationFunction(rule127.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule128 = repository.createRule(NESTED_OPTIONAL_PATTERNS_KEY)
            .setName("Optional pattern defaults should not depend on other parameters")
            .setHtmlDescription(
                "<p>Patterns like <code>f[x_:1, y_:x]</code> have evaluation order issues.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_:1, y_:x] := x + y  (* y default depends on x *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_:1, y_:1] := x + y  (* Independent defaults *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS, "evaluation-order");

            rule128.setDebtRemediationFunction(rule128.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule129 = repository.createRule(PATTERN_NAMING_CONFLICTS_KEY)
            .setName("Pattern names should not have conflicting type restrictions")
            .setHtmlDescription(
                "<p>Using the same pattern name with different types creates impossible-to-match patterns.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_Integer, x_Real] := x  (* Impossible to match *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_Integer, y_Real] := x + y  (* Use different names *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS);

            rule129.setDebtRemediationFunction(rule129.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule130 = repository.createRule(REPEATED_PATTERN_ALTERNATIVES_KEY)
            .setName("Pattern alternatives should use correct syntax")
            .setHtmlDescription(
                "<p>Redundant pattern names in alternatives should be refactored.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_Integer | x_Real] := x  (* Redundant *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x:(_Integer | _Real)] := x  (* Correct syntax *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PATTERNS, "clarity");

            rule130.setDebtRemediationFunction(rule130.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule131 = repository.createRule(PATTERN_TEST_PURE_FUNCTION_KEY)
            .setName("Avoid pure functions in PatternTest for hot code")
            .setHtmlDescription(
                "<p>Pure functions in patterns create closures on each match attempt.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_?(# > 0 &)] := x  (* Creates closure each time *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_?Positive] := x  (* Built-in predicate *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PATTERNS, TAG_PERFORMANCE);

            rule131.setDebtRemediationFunction(rule131.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule132 = repository.createRule(MISSING_PATTERN_DEFAULTS_KEY)
            .setName("Optional arguments should have sensible defaults")
            .setHtmlDescription(
                "<p>Optional parameters without validation can cause issues.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_, opts___] := DoSomething[x, opts]  (* No validation *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_, opts:OptionsPattern[]] := DoSomething[x, opts]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS, "validation");

            rule132.setDebtRemediationFunction(rule132.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule133 = repository.createRule(ORDER_DEPENDENT_PATTERNS_KEY)
            .setName("Specific patterns should be defined before general ones")
            .setHtmlDescription(
                "<p>Pattern matching tries definitions in order, so specific patterns after general ones never match.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\nf[x_] := x^2;\nf[0] := 0;  (* Never matches - f[x_] already handles f[0] *)\n</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\nf[0] := 0;  (* Specific first *)\nf[x_] := x^2;  (* General second *)\n</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS, "unreachable-code");

            rule133.setDebtRemediationFunction(rule133.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule134 = repository.createRule(VERBATIM_PATTERN_MISUSE_KEY)
            .setName("Verbatim should only be used when necessary")
            .setHtmlDescription(
                "<p>Verbatim has tricky semantics and is often misused.</p>"
                + "<h2>Example</h2>"
                + "<pre>Cases[{1, 2, x, y}, Verbatim[x]]  (* Only matches literal x *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS);

            rule134.setDebtRemediationFunction(rule134.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule135 = repository.createRule(HOLDPATTERN_UNNECESSARY_KEY)
            .setName("HoldPattern should be removed when not needed")
            .setHtmlDescription(
                "<p>Unnecessary HoldPattern adds clutter without benefit.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Cases[list, HoldPattern[_Integer]]  (* Unnecessary *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Cases[list, _Integer]  (* HoldPattern not needed *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PATTERNS, "clutter");

            rule135.setDebtRemediationFunction(rule135.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * CHUNK 1 RULES (Items 16-50 from ROADMAP_325.md) (Part 2) (11 rules)
     */
    private static void defineChunk1Rules2(NewRepository repository) {

        NewRule rule136 = repository.createRule(LONGEST_SHORTEST_WITHOUT_ORDERING_KEY)
            .setName("Longest and Shortest require proper context")
            .setHtmlDescription(
                "<p>Longest and Shortest modifiers may not work as expected without proper alternatives.</p>"
                + "<h2>Example</h2>"
                + "<pre>StringCases[str, Longest[__]]  (* Needs alternatives to be useful *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS);

            rule136.setDebtRemediationFunction(rule136.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule137 = repository.createRule(PATTERN_REPEATED_DIFFERENT_TYPES_KEY)
            .setName("Use conditions instead of repeated pattern names for equality checks")
            .setHtmlDescription(
                "<p>Pattern <code>f[{x_, x_}]</code> matches lists with same symbolic name, not same value.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[{x_, x_}] := x  (* Doesn't check equality *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[{x_, y_} /; x == y] := x  (* Checks equality *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags(TAG_PATTERNS);

            rule137.setDebtRemediationFunction(rule137.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule138 = repository.createRule(ALTERNATIVES_TOO_COMPLEX_KEY)
            .setName("Pattern alternatives with many options cause backtracking")
            .setHtmlDescription(
                "<p>Alternatives with 10+ options can cause exponential backtracking.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x:(a|b|c|d|e|f|g|h|i|j|k|l) := ...  (* Slow matching *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x_ /; MemberQ[{a,b,c,d,e,f,g,h,i,j,k,l}, x] := ...</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PATTERNS, TAG_PERFORMANCE);

            rule138.setDebtRemediationFunction(rule138.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule139 = repository.createRule(PATTERN_MATCHING_LARGE_LISTS_KEY)
            .setName("Avoid pattern matching on large lists")
            .setHtmlDescription(
                "<p>Pattern matching lists with thousands of elements is inefficient.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Cases[Range[10000], x_ /; x > 100]  (* Slow *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Select[Range[10000], # > 100 &]  (* Much faster *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags(TAG_PATTERNS, TAG_PERFORMANCE);

            rule139.setDebtRemediationFunction(rule139.debtRemediationFunctions().constantPerIssue("5min"));

        // List/Array Rules (Items 31-40)
        NewRule rule140 = repository.createRule(EMPTY_LIST_INDEXING_KEY)
            .setName("Check list length before indexing")
            .setHtmlDescription(
                "<p>Indexing empty lists causes runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = list[[1]]  (* Error if list is {} *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[Length[list] > 0, result = list[[1]], result = Missing[]]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("lists", "bounds-check");

            rule140.setDebtRemediationFunction(rule140.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule141 = repository.createRule(NEGATIVE_INDEX_WITHOUT_VALIDATION_KEY)
            .setName("Validate negative indices against list length")
            .setHtmlDescription(
                "<p>Negative index <code>list[[-n]]</code> fails if n > Length[list].</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>last = list[[-n]]  (* Error if n too large *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[n <= Length[list], last = list[[-n]], ...]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("lists", "bounds-check");

            rule141.setDebtRemediationFunction(rule141.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule142 = repository.createRule(PART_ASSIGNMENT_TO_IMMUTABLE_KEY)
            .setName("Part assignment requires a variable")
            .setHtmlDescription(
                "<p>Assigning to <code>expr[[i]]</code> where expr is not a variable doesn't modify anything.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>{1,2,3}[[1]] = 5  (* Doesn't modify anything *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>list = {1,2,3}; list[[1]] = 5  (* Modifies list *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("lists", "mutation");

            rule142.setDebtRemediationFunction(rule142.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule143 = repository.createRule(INEFFICIENT_LIST_CONCATENATION_KEY)
            .setName("Avoid repeated Join operations in loops")
            .setHtmlDescription(
                "<p>Using <code>Join[list, {x}]</code> in a loop has O(n²) complexity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[result = Join[result, {i}], {i, 1000}]  (* Quadratic *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>result = Table[i, {i, 1000}]  (* Linear *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", TAG_PERFORMANCE);

            rule143.setDebtRemediationFunction(rule143.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule144 = repository.createRule(UNNECESSARY_FLATTEN_KEY)
            .setName("Don't flatten already-flat lists")
            .setHtmlDescription(
                "<p>Flatten on flat lists wastes computation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Flatten[{a, b, c}]  (* Already flat *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>{a, b, c}  (* No Flatten needed *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", TAG_PERFORMANCE);

            rule144.setDebtRemediationFunction(rule144.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule145 = repository.createRule(LENGTH_IN_LOOP_CONDITION_KEY)
            .setName("Cache list length outside loops")
            .setHtmlDescription(
                "<p>Recalculating Length in loop conditions is wasteful.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[..., {i, 1, Length[list]}]  (* Recalculates each iteration *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>n = Length[list]; Do[..., {i, 1, n}]</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", TAG_PERFORMANCE);

            rule145.setDebtRemediationFunction(rule145.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule146 = repository.createRule(REVERSE_TWICE_KEY)
            .setName("Double Reverse is a no-op")
            .setHtmlDescription(
                "<p>Reversing a list twice returns the original.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Reverse[Reverse[list]]  (* No-op *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>list  (* Remove double Reverse *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "redundant");

            rule146.setDebtRemediationFunction(rule146.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * CHUNK 1 RULES (Items 16-50 from ROADMAP_325.md) (Part 3) (13 rules)
     */
    private static void defineChunk1Rules3(NewRepository repository) {

        NewRule rule147 = repository.createRule(SORT_WITHOUT_COMPARISON_KEY)
            .setName("Use Reverse[Sort[list]] instead of Sort with Greater")
            .setHtmlDescription(
                "<p>Built-in Sort is optimized; custom comparisons are slower.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Sort[list, Greater]  (* Slower *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Reverse[Sort[list]]  (* Faster *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", TAG_PERFORMANCE);

            rule147.setDebtRemediationFunction(rule147.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule148 = repository.createRule(POSITION_VS_SELECT_KEY)
            .setName("Use Select instead of Extract with Position")
            .setHtmlDescription(
                "<p>Combining Extract and Position is inefficient and unclear.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Extract[list, Position[list, _?EvenQ]]  (* Complex *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Select[list, EvenQ]  (* Clearer and faster *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "clarity");

            rule148.setDebtRemediationFunction(rule148.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule149 = repository.createRule(NESTED_PART_EXTRACTION_KEY)
            .setName("Use multi-dimensional Part syntax")
            .setHtmlDescription(
                "<p>Nested Part extractions should use direct syntax.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>list[[i]][[j]]  (* Nested *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>list[[i, j]]  (* Cleaner *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "clarity");

            rule149.setDebtRemediationFunction(rule149.debtRemediationFunctions().constantPerIssue("5min"));

        // Association Rules (Items 41-50)
        NewRule rule150 = repository.createRule(MISSING_KEY_CHECK_KEY)
            .setName("Check if association key exists before accessing")
            .setHtmlDescription(
                "<p>Accessing non-existent keys returns Missing[\"KeyAbsent\", key].</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>value = assoc[\"key\"]  (* May return Missing *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[KeyExistsQ[assoc, \"key\"], value = assoc[\"key\"], ...]</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "validation");

            rule150.setDebtRemediationFunction(rule150.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule151 = repository.createRule(ASSOCIATION_VS_LIST_CONFUSION_KEY)
            .setName("Don't use list operations on associations")
            .setHtmlDescription(
                "<p>Some list operations don't work correctly on associations.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>assoc[[1]]  (* Wrong - associations aren't positional *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>First[Values[assoc]]  (* Correct *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations");

            rule151.setDebtRemediationFunction(rule151.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule152 = repository.createRule(INEFFICIENT_KEY_LOOKUP_KEY)
            .setName("Use KeySelect instead of Select on Keys")
            .setHtmlDescription(
                "<p>KeySelect is optimized for association key filtering.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Select[Keys[assoc], StringQ]  (* Inefficient *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>KeySelect[assoc, StringQ]  (* Faster *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", TAG_PERFORMANCE);

            rule152.setDebtRemediationFunction(rule152.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule153 = repository.createRule(QUERY_ON_NON_DATASET_KEY)
            .setName("Query requires Dataset wrapper")
            .setHtmlDescription(
                "<p>Query syntax only works on Dataset objects.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Query[All, \"name\"][list]  (* Error on plain list *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Query[All, \"name\"][Dataset[list]]  (* Correct *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "datasets");

            rule153.setDebtRemediationFunction(rule153.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule154 = repository.createRule(ASSOCIATION_UPDATE_PATTERN_KEY)
            .setName("Use AssociateTo or Append for association updates")
            .setHtmlDescription(
                "<p>Direct assignment syntax <code>assoc[\"key\"] = value</code> creates confusion.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>assoc[\"key\"] = value  (* Ambiguous *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>AssociateTo[assoc, \"key\" -> value]  (* Clear intent *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "clarity");

            rule154.setDebtRemediationFunction(rule154.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule155 = repository.createRule(MERGE_WITHOUT_CONFLICT_STRATEGY_KEY)
            .setName("Specify merge function for Merge")
            .setHtmlDescription(
                "<p>Merge without a combining function uses List by default, which may not be desired.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Merge[{a1, a2}]  (* Uses List by default *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Merge[{a1, a2}, Total]  (* Explicit strategy *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "clarity");

            rule155.setDebtRemediationFunction(rule155.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule156 = repository.createRule(ASSOCIATETO_ON_NON_SYMBOL_KEY)
            .setName("AssociateTo requires a symbol")
            .setHtmlDescription(
                "<p>AssociateTo modifies in place, so the first argument must be a symbol.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>AssociateTo[<|\"a\"->1|>, \"b\"->2]  (* Doesn't modify anything *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>assoc = <|\"a\"->1|>; AssociateTo[assoc, \"b\"->2]  (* Modifies assoc *)</pre>"
            )
            .setSeverity(SEVERITY_MAJOR)
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "mutation");

            rule156.setDebtRemediationFunction(rule156.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule157 = repository.createRule(KEYDROP_MULTIPLE_TIMES_KEY)
            .setName("Drop multiple keys in one call")
            .setHtmlDescription(
                "<p>Chained KeyDrop is less efficient than a single call.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>KeyDrop[KeyDrop[assoc, \"a\"], \"b\"]  (* Two passes *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>KeyDrop[assoc, {\"a\", \"b\"}]  (* Single pass *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", TAG_PERFORMANCE);

            rule157.setDebtRemediationFunction(rule157.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule158 = repository.createRule(LOOKUP_WITH_MISSING_DEFAULT_KEY)
            .setName("Don't specify Missing as Lookup default")
            .setHtmlDescription(
                "<p>Missing is already the default return value for Lookup.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Lookup[assoc, key, Missing[]]  (* Redundant *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Lookup[assoc, key]  (* Same behavior *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "redundant");

            rule158.setDebtRemediationFunction(rule158.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule159 = repository.createRule(GROUPBY_WITHOUT_AGGREGATION_KEY)
            .setName("Use GatherBy when not aggregating")
            .setHtmlDescription(
                "<p>GroupBy creates associations; GatherBy creates lists and may be clearer without aggregation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>GroupBy[data, First]  (* No aggregation *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>GatherBy[data, First]  (* Clearer intent *)</pre>"
            )
            .setSeverity(SEVERITY_MINOR)
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "clarity");

            rule159.setDebtRemediationFunction(rule159.debtRemediationFunctions().constantPerIssue("5min"));
    }

}
