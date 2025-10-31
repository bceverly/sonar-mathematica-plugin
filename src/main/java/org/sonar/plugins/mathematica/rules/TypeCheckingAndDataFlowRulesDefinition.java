package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ASSIGNMENT_AS_RETURN_VALUE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITION_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CLOSURE_OVER_MUTABLE_VARIABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMPARISON_INCOMPATIBLE_TYPES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DATASET_OPERATION_ON_LIST_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DEAD_STORE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DOUBLE_ASSIGNMENT_SAME_VALUE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FUNCTION_RETURNS_WRONG_TYPE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GRAPHICS_OBJECT_IN_NUMERIC_CONTEXT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GRAPH_OPERATION_ON_NON_GRAPH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.IMAGE_OPERATION_ON_NON_IMAGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.IMPLICIT_TYPE_CONVERSION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INTEGER_DIVISION_EXPECTING_REAL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LIST_FUNCTION_ON_ASSOCIATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MIXED_NUMERIC_TYPES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MODIFICATION_OF_LOOP_ITERATOR_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MUTATION_IN_PURE_FUNCTION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NULL_ASSIGNMENT_TO_TYPED_VARIABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.NUMERIC_OPERATION_ON_STRING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.OPTIONAL_TYPE_INCONSISTENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.OVERWRITTEN_BEFORE_READ_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PATTERN_TYPE_MISMATCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.READING_UNSET_VARIABLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.RETURN_TYPE_INCONSISTENT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SHARED_MUTABLE_STATE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SOUND_OPERATION_ON_NON_SOUND_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.STRING_OPERATION_ON_NUMBER_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SYMBOL_IN_NUMERIC_CONTEXT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PATTERNS;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_PERFORMANCE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TYPE_CAST_WITHOUT_VALIDATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNINITIALIZED_VARIABLE_USE_ENHANCED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.USE_OF_ITERATOR_OUTSIDE_LOOP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_ALIASING_ISSUE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_MAY_BE_UNINITIALIZED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_NEVER_MODIFIED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VARIABLE_SCOPE_ESCAPE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.WRONG_ARGUMENT_TYPE_KEY;

/**
 * Type Checking and Data Flow rule definitions.
 * Covers type mismatch detection and data flow analysis.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class TypeCheckingAndDataFlowRulesDefinition {

    private TypeCheckingAndDataFlowRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineTypeCheckingRules(repository);
        defineTypeCheckingRulesContinued(repository);
        defineDataFlowRules(repository);
        defineDataFlowRulesContinued(repository);
        defineNumericAndEdgeCaseRules(repository);
        defineNumericAndEdgeCaseRulesContinued(repository);
    }

    /**
     * CHUNK 3 RULE DEFINITIONS (Items 111-150 from ROADMAP_325.md) (Part 1) (12 rules)
     */
    private static void defineTypeCheckingRules(NewRepository repository) {
        // ===== CHUNK 3 RULE DEFINITIONS (Items 111-150 from ROADMAP_325.md) =====

        // Type Mismatch Detection Rules (Items 111-130)

        NewRule rule200 = repository.createRule(NUMERIC_OPERATION_ON_STRING_KEY)
            .setName("Numeric operations on strings cause runtime errors")
            .setHtmlDescription(
                "<p>Performing arithmetic operations on string values produces unexpected results or errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\"hello\" + 1  (* Concatenates, doesn't add *)\n"
                + "\"hello\"^2  (* Returns unevaluated *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>ToExpression[\"5\"] + 1  (* Convert string to number first *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "runtime-error");

            rule200.setDebtRemediationFunction(rule200.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule201 = repository.createRule(STRING_OPERATION_ON_NUMBER_KEY)
            .setName("String operations on numbers cause runtime errors")
            .setHtmlDescription(
                "<p>Using string functions on numeric values causes runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>StringJoin[123, \"abc\"]  (* Wrong argument type *)\n"
                + "StringLength[42]  (* Expects string *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>StringJoin[ToString[123], \"abc\"]  (* Convert to string first *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "runtime-error");

            rule201.setDebtRemediationFunction(rule201.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule202 = repository.createRule(WRONG_ARGUMENT_TYPE_KEY)
            .setName("Function called with wrong argument type")
            .setHtmlDescription(
                "<p>Passing wrong types to built-in functions causes runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Map[f, 123]  (* Expects list, not integer *)\n"
                + "Length[5]  (* Expects list/association/string *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Map[f, {1,2,3}]  (* Use correct type *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "argument-type");

            rule202.setDebtRemediationFunction(rule202.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule203 = repository.createRule(FUNCTION_RETURNS_WRONG_TYPE_KEY)
            .setName("Function returns type different from declaration")
            .setHtmlDescription(
                "<p>Functions should return consistent types matching their documentation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* Documented to return Integer *)\n"
                + "calculate[x_] := If[x > 0, x, \"error\"]  (* Returns String sometimes *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>calculate[x_?Positive] := x  (* Type-safe *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "return-type");

            rule203.setDebtRemediationFunction(rule203.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule204 = repository.createRule(COMPARISON_INCOMPATIBLE_TYPES_KEY)
            .setName("Comparison of incompatible types")
            .setHtmlDescription(
                "<p>Comparing values of incompatible types produces meaningless results.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\"hello\" < 5  (* Compares but meaningless *)\n"
                + "{1,2} == 3  (* Always False *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>StringLength[\"hello\"] < 5  (* Compare compatible types *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("type-mismatch", "comparison");

            rule204.setDebtRemediationFunction(rule204.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule205 = repository.createRule(MIXED_NUMERIC_TYPES_KEY)
            .setName("Mixing exact and approximate numbers loses precision")
            .setHtmlDescription(
                "<p>Mixing exact (Integer/Rational) with approximate (Real) numbers causes precision loss.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>1/3 + 0.5  (* Converts to approximate *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>1/3 + 1/2  (* Keep exact *)\nN[1/3] + 0.5  (* Or be explicit *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("numeric-precision", "type-mismatch");

            rule205.setDebtRemediationFunction(rule205.debtRemediationFunctions().constantPerIssue("5min"));

    }

    private static void defineTypeCheckingRulesContinued(NewRepository repository) {
        NewRule rule206 = repository.createRule(INTEGER_DIVISION_EXPECTING_REAL_KEY)
            .setName("Integer division stays symbolic, use real division for numeric result")
            .setHtmlDescription(
                "<p>Division of integers stays symbolic unless converted to real.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>1/2  (* Evaluates to 1/2, not 0.5 *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>1./2  (* Evaluates to 0.5 *)\nN[1/2]  (* Explicit conversion *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("numeric-precision", "integer-division");

            rule206.setDebtRemediationFunction(rule206.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule207 = repository.createRule(LIST_FUNCTION_ON_ASSOCIATION_KEY)
            .setName("List functions should not be used on associations")
            .setHtmlDescription(
                "<p>Using list functions on associations has different semantics than association functions.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Append[<|a->1|>, b->2]  (* Wrong semantics *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>AssociateTo[<|a->1|>, b->2]  (* Correct for associations *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "associations");

            rule207.setDebtRemediationFunction(rule207.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule208 = repository.createRule(PATTERN_TYPE_MISMATCH_KEY)
            .setName("Function call doesn't match pattern types")
            .setHtmlDescription(
                "<p>Calling function with argument that doesn't match pattern type constraint.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_Integer] := x^2;\n"
                + "f[\"hello\"]  (* Won't match, returns unevaluated *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[5]  (* Matches pattern *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_PATTERNS, "type-mismatch");

            rule208.setDebtRemediationFunction(rule208.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule209 = repository.createRule(OPTIONAL_TYPE_INCONSISTENT_KEY)
            .setName("Optional parameter default has wrong type")
            .setHtmlDescription(
                "<p>Default value for optional parameter should match the pattern type.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_Integer : 1.5] := x  (* Default is Real, not Integer *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_Integer : 1] := x  (* Consistent types *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_PATTERNS, "optional-parameters");

            rule209.setDebtRemediationFunction(rule209.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule210 = repository.createRule(RETURN_TYPE_INCONSISTENT_KEY)
            .setName("Function returns inconsistent types")
            .setHtmlDescription(
                "<p>Functions that return different types from different branches are confusing.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := If[x > 0, x, \"negative\"]  (* Returns Integer or String *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := If[x > 0, x, -1]  (* Consistent Integer return *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("return-type", "api-design");

            rule210.setDebtRemediationFunction(rule210.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule211 = repository.createRule(NULL_ASSIGNMENT_TO_TYPED_VARIABLE_KEY)
            .setName("Null assigned to variable expected to be numeric")
            .setHtmlDescription(
                "<p>Assigning Null to variables used in numeric contexts causes errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = Null; result = x + 1  (* Error *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 0; result = x + 1  (* Or use Missing[\"NotAvailable\"] *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("null-safety", "type-mismatch");

            rule211.setDebtRemediationFunction(rule211.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }

    /**
     * CHUNK 3 RULE DEFINITIONS (Items 111-150 from ROADMAP_325.md) (Part 2) (12 rules)
     */
    private static void defineDataFlowRules(NewRepository repository) {

        NewRule rule212 = repository.createRule(TYPE_CAST_WITHOUT_VALIDATION_KEY)
            .setName("Type conversion without validation")
            .setHtmlDescription(
                "<p>Converting types without checking validity can cause runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>ToExpression[userInput]  (* May not be valid expression *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[StringQ[userInput], ToExpression[userInput], $Failed]</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-casting", "validation");

            rule212.setDebtRemediationFunction(rule212.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule213 = repository.createRule(IMPLICIT_TYPE_CONVERSION_KEY)
            .setName("Redundant type conversion")
            .setHtmlDescription(
                "<p>Converting values that are already the target type is redundant.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>ToString[\"hello\"]  (* Already a string *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\"hello\"  (* No conversion needed *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("redundant", "type-conversion");

            rule213.setDebtRemediationFunction(rule213.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule214 = repository.createRule(GRAPHICS_OBJECT_IN_NUMERIC_CONTEXT_KEY)
            .setName("Graphics object used in numeric computation")
            .setHtmlDescription(
                "<p>Using graphics objects in numeric contexts doesn't make sense.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Plot[x^2, {x, 0, 1}] + 1  (* Graphics + Number? *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Extract data first or fix logic error *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "graphics");

            rule214.setDebtRemediationFunction(rule214.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule215 = repository.createRule(SYMBOL_IN_NUMERIC_CONTEXT_KEY)
            .setName("Symbolic variable in numeric context")
            .setHtmlDescription(
                "<p>Using undefined symbolic variables in numeric computations may not evaluate.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x + 1  (* If x undefined, returns x+1 symbolically *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5; x + 1  (* Assign value first *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("symbolic", "numeric-context");

            rule215.setDebtRemediationFunction(rule215.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule216 = repository.createRule(IMAGE_OPERATION_ON_NON_IMAGE_KEY)
            .setName("Image operation on non-Image object")
            .setHtmlDescription(
                "<p>Image functions require Image objects, not raw arrays.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>ImageData[{{0,0},{1,1}}]  (* Expects Image, not array *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>ImageData[Image[{{0,0},{1,1}}]]  (* Wrap in Image first *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "image-processing");

            rule216.setDebtRemediationFunction(rule216.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule217 = repository.createRule(SOUND_OPERATION_ON_NON_SOUND_KEY)
            .setName("Audio operation on non-Audio object")
            .setHtmlDescription(
                "<p>Audio functions require Audio objects, not raw arrays.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>AudioData[{1,2,3}]  (* Expects Audio, not list *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>AudioData[Audio[{1,2,3}]]  (* Wrap in Audio first *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "audio-processing");

            rule217.setDebtRemediationFunction(rule217.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

    }

    private static void defineDataFlowRulesContinued(NewRepository repository) {
        NewRule rule218 = repository.createRule(DATASET_OPERATION_ON_LIST_KEY)
            .setName("Dataset operations require Dataset wrapper")
            .setHtmlDescription(
                "<p>Dataset-specific operations need data wrapped in Dataset.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>data = {{1,2},{3,4}};\ndata[All, \"col1\"]  (* Doesn't work on list *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Dataset[data][All, \"col1\"]  (* Wrap in Dataset *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "dataset");

            rule218.setDebtRemediationFunction(rule218.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule219 = repository.createRule(GRAPH_OPERATION_ON_NON_GRAPH_KEY)
            .setName("Graph operation on non-Graph object")
            .setHtmlDescription(
                "<p>Graph functions require Graph objects, not edge lists.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>VertexList[{{1,2},{2,3}}]  (* Expects Graph, not edge list *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>VertexList[Graph[{{1,2},{2,3}}]]  (* Create Graph first *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("type-mismatch", "graph-theory");

            rule219.setDebtRemediationFunction(rule219.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        // Data Flow Analysis Rules (Items 135-150)

        NewRule rule220 = repository.createRule(UNINITIALIZED_VARIABLE_USE_ENHANCED_KEY)
            .setName("Variable used before initialization")
            .setHtmlDescription(
                "<p>Using variables before assigning a value causes runtime errors or unexpected behavior.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = x + 1  (* x never initialized *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 0; result = x + 1  (* Initialize first *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("uninitialized", "data-flow");

            rule220.setDebtRemediationFunction(rule220.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule221 = repository.createRule(VARIABLE_MAY_BE_UNINITIALIZED_KEY)
            .setName("Variable may be uninitialized in some code paths")
            .setHtmlDescription(
                "<p>Variable initialized in some branches but not all causes logic errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[condition, x = 1];\nresult = x  (* x undefined if condition False *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = If[condition, 1, 0];\nresult = x  (* Always initialized *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("uninitialized", "data-flow");

            rule221.setDebtRemediationFunction(rule221.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule222 = repository.createRule(DEAD_STORE_KEY)
            .setName("Value assigned but never read")
            .setHtmlDescription(
                "<p>Assigning values that are never read is useless computation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = expensiveComputation[];  (* Value never used *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove unused assignment or use the value *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("dead-store", TAG_PERFORMANCE);

            rule222.setDebtRemediationFunction(rule222.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule223 = repository.createRule(OVERWRITTEN_BEFORE_READ_KEY)
            .setName("Assignment overwritten before being read")
            .setHtmlDescription(
                "<p>Assigning a value that's overwritten before being read is wasteful.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 1; x = 2; Print[x]  (* First assignment wasted *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 2; Print[x]  (* Remove redundant assignment *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("redundant", "data-flow");

            rule223.setDebtRemediationFunction(rule223.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * CHUNK 3 RULE DEFINITIONS (Items 111-150 from ROADMAP_325.md) (Part 3) (12 rules)
     */
    private static void defineNumericAndEdgeCaseRules(NewRepository repository) {

        NewRule rule224 = repository.createRule(VARIABLE_ALIASING_ISSUE_KEY)
            .setName("Multiple variables point to same mutable structure")
            .setHtmlDescription(
                "<p>Aliasing mutable structures causes unexpected modifications.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>list1 = {1,2,3};\nlist2 = list1;\nlist2[[1]] = 99  (* Also modifies list1 *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>list2 = list1  (* Copy if needed for independent modification *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("aliasing", "mutable-state");

            rule224.setDebtRemediationFunction(rule224.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule225 = repository.createRule(MODIFICATION_OF_LOOP_ITERATOR_KEY)
            .setName("Loop iterator should not be modified inside loop")
            .setHtmlDescription(
                "<p>Modifying loop iterators inside the loop body is confusing and error-prone.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[i = i + 1; Print[i], {i, 1, 10}]  (* Modifying iterator *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Do[Print[i], {i, 1, 10}]  (* Don't modify iterator *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("loops", "iterator-modification");

            rule225.setDebtRemediationFunction(rule225.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule226 = repository.createRule(USE_OF_ITERATOR_OUTSIDE_LOOP_KEY)
            .setName("Loop iterator value after loop is undefined")
            .setHtmlDescription(
                "<p>Using loop iterator after loop ends is unreliable - value is implementation-dependent.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Do[..., {i, 1, 10}];\nPrint[i]  (* i value after loop undefined *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Don't rely on iterator value after loop *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("loops", "iterator-scope");

            rule226.setDebtRemediationFunction(rule226.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule227 = repository.createRule(READING_UNSET_VARIABLE_KEY)
            .setName("Reading variable after Unset or Clear")
            .setHtmlDescription(
                "<p>Reading a variable after Unset/Clear returns the symbol itself, not a value.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5; Unset[x]; Print[x]  (* Prints symbol x, not value *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5; Print[x]; Unset[x]  (* Read before unsetting *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("unset", "data-flow");

            rule227.setDebtRemediationFunction(rule227.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule228 = repository.createRule(DOUBLE_ASSIGNMENT_SAME_VALUE_KEY)
            .setName("Variable assigned same value twice")
            .setHtmlDescription(
                "<p>Assigning the same value to a variable multiple times is redundant.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>x = 5; ...; x = 5  (* Same value assigned twice *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>x = 5;  (* Remove redundant assignment *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("redundant", "code-smell");

            rule228.setDebtRemediationFunction(rule228.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule229 = repository.createRule(MUTATION_IN_PURE_FUNCTION_KEY)
            .setName("Pure function mutates outer variable")
            .setHtmlDescription(
                "<p>Pure functions with side effects are confusing and break functional paradigm.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>counter = 0;\nMap[(counter++; #) &, list]  (* Side effect in pure function *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>MapIndexed[...  (* Use stateless approach *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("pure-functions", "side-effects");

            rule229.setDebtRemediationFunction(rule229.debtRemediationFunctions().constantPerIssue("5min"));

    }

    private static void defineNumericAndEdgeCaseRulesContinued(NewRepository repository) {
        NewRule rule230 = repository.createRule(SHARED_MUTABLE_STATE_KEY)
            .setName("Global mutable state accessed from multiple functions")
            .setHtmlDescription(
                "<p>Shared mutable global state is hard to reason about and debug.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>globalCounter = 0;\nf[] := globalCounter++\ng[] := globalCounter--  (* Both modify global *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Pass state as parameters or use Module *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("global-state", "mutable-state");

            rule230.setDebtRemediationFunction(rule230.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule231 = repository.createRule(VARIABLE_SCOPE_ESCAPE_KEY)
            .setName("Module local variable escapes its scope")
            .setHtmlDescription(
                "<p>Returning Module local variables causes them to escape as symbols.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x}, x]  (* Returns symbol, not value *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Module[{x = 5}, x]  (* Return value, not symbol *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("scope", "module");

            rule231.setDebtRemediationFunction(rule231.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule232 = repository.createRule(CLOSURE_OVER_MUTABLE_VARIABLE_KEY)
            .setName("Pure function captures mutable variable")
            .setHtmlDescription(
                "<p>Closures capturing mutable variables may not capture the expected value.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>funcs = Table[Function[x + i], {i, 3}]  (* All capture final i *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>funcs = Table[With[{j = i}, Function[x + j]], {i, 3}]  (* Capture value *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("closures", "variable-capture");

            rule232.setDebtRemediationFunction(rule232.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule233 = repository.createRule(ASSIGNMENT_IN_CONDITION_ENHANCED_KEY)
            .setName("Assignment in condition instead of comparison")
            .setHtmlDescription(
                "<p>Using = instead of == in conditions is almost always a bug.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[x = 5, ...]  (* Assigns 5 to x, always true *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>If[x == 5, ...]  (* Compare, don't assign *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("assignment", "condition");

            rule233.setDebtRemediationFunction(rule233.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule234 = repository.createRule(ASSIGNMENT_AS_RETURN_VALUE_KEY)
            .setName("Unnecessary variable assignment before return")
            .setHtmlDescription(
                "<p>Assigning to variable just to return it immediately is unnecessary.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := (y = x; y)  (* Unnecessary variable *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := x  (* Return directly *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("redundant", "return-value");

            rule234.setDebtRemediationFunction(rule234.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule235 = repository.createRule(VARIABLE_NEVER_MODIFIED_KEY)
            .setName("Module variable never modified, use With instead")
            .setHtmlDescription(
                "<p>Variables that are never modified should use With for immutability guarantees.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Module[{x = 1}, computeWith[x]]  (* x never modified *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>With[{x = 1}, computeWith[x]]  (* Immutable *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("immutability", "best-practice");

            rule235.setDebtRemediationFunction(rule235.debtRemediationFunctions().constantPerIssue("2min"));
    }

}
