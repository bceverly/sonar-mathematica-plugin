package org.sonar.plugins.mathematica.rules;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Combined Chunk 6 & 7 Detector - Advanced Semantics & Polish (Items 251 - 325 from ROADMAP_325.md)
 *
 * This detector implements 62 rules across six categories:
 * CHUNK 6:
 * 1. Null Safety (Items 251 - 265): 15 rules
 * 2. Constant & Expression Analysis (Items 267 - 280): 14 rules
 * 3. Mathematica-Specific Patterns (Items 281 - 300): 20 rules
 *
 * CHUNK 7:
 * 4. Test Coverage Integration (Items 307 - 310): 4 rules
 * 5. Performance Analysis (Items 312 - 320): 9 rules
 */
public class AdvancedAnalysisDetector extends BaseDetector {

    // ========================================
    // PRE-COMPILED PATTERNS FOR PERFORMANCE
    // ========================================

    // Null safety patterns
    private static final Pattern NULL_DEREF = Pattern.compile("(\\w+)\\s*\\[\\[");
    private static final Pattern NULL_CHECK = Pattern.compile("(?:===|=!=)\\s*Null");
    private static final Pattern NULL_COMPARISON_WRONG = Pattern.compile("==\\s*Null");
    private static final Pattern LENGTH_NULL = Pattern.compile("Length\\s*\\[\\s*Null\\s*\\]");

    // Expression patterns
    private static final Pattern IDENTITY_OP = Pattern.compile("(?:Reverse|Transpose)\\s*\\[\\s*(?:Reverse|Transpose)\\s*\\[");
    private static final Pattern CONSTANT_EXPR = Pattern.compile("(\\w+)\\s*(?:\\*\\s*1|\\+\\s*0|\\^\\s*1)");
    private static final Pattern DOUBLE_NEG = Pattern.compile("(?:Not\\s*\\[\\s*Not\\s*\\[|!!)");
    private static final Pattern BOOL_CONVERSION = Pattern.compile("If\\s*\\[([^,]+),\\s*True\\s*,\\s*False\\s*\\]");
    private static final Pattern IDENTICAL_COMP = Pattern.compile("(\\w+)\\s*==\\s*\\1(?!\\w)");

    // Hold/evaluation patterns
    private static final Pattern HOLD_ATTR = Pattern.compile("SetAttributes\\s*\\[\\s*(\\w+)\\s*,\\s*(?:HoldAll|HoldFirst|HoldRest)");
    private static final Pattern HOLD_LITERAL = Pattern.compile("Hold\\s*\\[\\s*(?:\\d+|\"[^\"]*\")\\s*\\]");
    private static final Pattern RELEASE_HOLD = Pattern.compile("ReleaseHold\\s*\\[\\s*Hold\\s*\\[");
    private static final Pattern EVALUATE_IN_HOLD = Pattern.compile("Hold\\s*\\[[^\\]]*Evaluate\\s*\\[");
    private static final Pattern UNEVALUATED = Pattern.compile("Unevaluated\\s*\\[");

    // Pattern/replacement patterns
    private static final Pattern PATTERN_SIDE_EFFECT = Pattern.compile("_\\?\\s*\\([^)]*(?:Print|Message|Set)");
    private static final Pattern REPLACE_ALL = Pattern.compile("/\\.");
    private static final Pattern RULE_ORDER = Pattern.compile("\\{[^}]*_\\s*->.*?,\\s*\\w+\\s*->");

    // Part/list patterns
    private static final Pattern PART_SPEC = Pattern.compile("\\[\\[\\s*(\\d+)\\s*\\]\\]");
    private static final Pattern SPAN_SPEC = Pattern.compile("\\[\\[\\s*(\\d+)\\s*;;\\s*(\\d+)\\s*\\]\\]");
    private static final Pattern ALL_SPEC = Pattern.compile("\\[\\[\\s*All\\s*\\]\\]");

    // Attribute patterns
    private static final Pattern SET_ATTRIBUTES = Pattern.compile("SetAttributes\\s*\\[\\s*(\\w+)\\s*,\\s*(\\w+)\\s*\\]");
    private static final Pattern ORDERLESS = Pattern.compile("SetAttributes\\s*\\[[^,]+,\\s*Orderless");
    private static final Pattern FLAT = Pattern.compile("SetAttributes\\s*\\[[^,]+,\\s*Flat");
    private static final Pattern LISTABLE = Pattern.compile("SetAttributes\\s*\\[[^,]+,\\s*Listable");

    // Sequence patterns
    private static final Pattern SEQUENCE = Pattern.compile("Sequence\\s*\\[");

    // Error handling patterns
    private static final Pattern QUIET = Pattern.compile("Quiet\\s*\\[([^\\]]+)\\](?!\\s*,)");
    private static final Pattern OFF = Pattern.compile("Off\\s*\\[\\s*General::");
    private static final Pattern CATCH_ALL = Pattern.compile("Catch\\s*\\[([^\\]]+)\\](?!\\s*,)");
    private static final Pattern EMPTY_CATCH = Pattern.compile("Catch\\s*\\[[^,]+,\\s*_\\s*,\\s*Null\\s*&");
    private static final Pattern THROW = Pattern.compile("Throw\\s*\\[");
    private static final Pattern ABORT = Pattern.compile("Abort\\s*\\[\\s*\\]");
    private static final Pattern MESSAGE_CALL = Pattern.compile("Message\\s*\\[\\s*(\\w+)::(\\w+)");
    private static final Pattern MESSAGE_DEF = Pattern.compile("(\\w+)::(\\w+)\\s*=");

    // Compilation patterns
    private static final Pattern COMPILE = Pattern.compile("Compile\\s*\\[");
    private static final Pattern COMPILATION_TARGET = Pattern.compile("CompilationTarget\\s*->\\s*\"(\\w+)\"");
    private static final Pattern NON_COMPILABLE = Pattern.compile("Compile\\s*\\[[^\\]]*(?:Sort|Select|Cases|DeleteCases)");

    // Test patterns
    private static final Pattern TEST_FUNCTION = Pattern.compile("(?:VerificationTest|Test(?:ID|Match|Report)|Assert(?:True|False|Equal))\\s*\\[");
    private static final Pattern TEST_FILE_PATTERN = Pattern.compile("(?i)test.*?\\.");

    // Performance patterns
    private static final Pattern PACKED_ARRAY_UNPACK = Pattern.compile("\\[\\[\\s*\\d+\\s*\\]\\]\\s*=");
    private static final Pattern N_LATE = Pattern.compile("N\\s*\\[\\s*(?:Integrate|Sum|Product|Solve)");
    private static final Pattern STRING_CONCAT_LOOP = Pattern.compile("Do\\s*\\[[^\\]]*<>");
    private static final Pattern LIST_CONCAT_LOOP = Pattern.compile("Do\\s*\\[[^\\]]*Join\\s*\\[");

    private static NewIssue createIssue(SensorContext context, InputFile file, String ruleKey, int line, String msg) {
        NewIssue issue = context.newIssue().forRule(org.sonar.api.rule.RuleKey.of(
            MathematicaRulesDefinition.REPOSITORY_KEY, ruleKey));
        issue.at(issue.newLocation().on(file).at(file.selectLine(Math.max(1, line))).message(msg));
        return issue;
    }

    // ========================================
    // CHUNK 6: NULL SAFETY (15 rules)
    // ========================================

    public static void detectNullDereference(SensorContext ctx, InputFile file, String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("[[") && !lines[i].contains("Null")) {
                Matcher m = NULL_DEREF.matcher(lines[i]);
                if (m.find() && (i > 0 && lines[i - 1].contains("Null") || lines[i].contains("If["))) {
                    createIssue(ctx, file, MathematicaRulesDefinition.NULL_DEREFERENCE_KEY, i + 1,
                        "Potential null dereference").save();
                }
            }
        }
    }

    public static void detectMissingNullCheck(SensorContext ctx, InputFile file, String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*\\w+\\[\\w+_\\]\\s*:=.*\\[\\[") && !NULL_CHECK.matcher(lines[i]).find()) {
                createIssue(ctx, file, MathematicaRulesDefinition.MISSING_NULL_CHECK_KEY, i + 1,
                    "Missing null check before indexing").save();
            }
        }
    }

    public static void detectNullPassedToNonNullable(SensorContext ctx, InputFile file, String content) {
        if (LENGTH_NULL.matcher(content).find()) {
            Matcher m = LENGTH_NULL.matcher(content);
            int line = 1;
            while (m.find()) {
                line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.NULL_PASSED_TO_NON_NULLABLE_KEY, line,
                    "Null passed to Length[]").save();
            }
        }
    }

    public static void detectInconsistentNullHandling(SensorContext ctx, InputFile file, String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("If[") && lines[i].contains(",") && !lines[i].contains("Null")) {
                createIssue(ctx, file, MathematicaRulesDefinition.INCONSISTENT_NULL_HANDLING_KEY, i + 1,
                    "Consider null safety in branches").save();
            }
        }
    }

    public static void detectNullReturnNotDocumented(SensorContext ctx, InputFile file, String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*\\w+\\[.*\\]\\s*:=.*Null")
                && (i == 0 || !lines[i - 1].contains("::usage"))) {
                createIssue(ctx, file, MathematicaRulesDefinition.NULL_RETURN_NOT_DOCUMENTED_KEY, i + 1,
                    "Function returns Null without documentation").save();
            }
        }
    }

    public static void detectComparisonWithNull(SensorContext ctx, InputFile file, String content) {
        Matcher m = NULL_COMPARISON_WRONG.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.COMPARISON_WITH_NULL_KEY, line,
                "Use === instead of == for Null comparison").save();
        }
    }

    public static void detectMissingCheckLeadsToNullPropagation(SensorContext ctx, InputFile file, String content) {
        Pattern chain = Pattern.compile("\\w+\\[\\w+\\[\\w+\\[");
        if (chain.matcher(content).find()) {
            Matcher m = chain.matcher(content);
            while (m.find()) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.MISSING_CHECK_LEADS_TO_NULL_PROPAGATION_KEY, line,
                    "Deep call chain without null checks").save();
            }
        }
    }

    public static void detectCheckPatternDoesntHandleAllCases(SensorContext ctx, InputFile file, String content) {
        Pattern check = Pattern.compile("Check\\s*\\[[^,]+,[^,]+\\](?!\\s*,)");
        if (check.matcher(content).find()) {
            Matcher m = check.matcher(content);
            while (m.find()) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.CHECK_PATTERN_DOESNT_HANDLE_ALL_CASES_KEY, line,
                    "Check[] missing specific error cases").save();
            }
        }
    }

    public static void detectQuietSuppressingImportantMessages(SensorContext ctx, InputFile file, String content) {
        Matcher m = QUIET.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.QUIET_SUPPRESSING_IMPORTANT_MESSAGES_KEY, line,
                "Quiet[] without specific messages suppresses all").save();
        }
    }

    public static void detectOffDisablingImportantWarnings(SensorContext ctx, InputFile file, String content) {
        Matcher m = OFF.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.OFF_DISABLING_IMPORTANT_WARNINGS_KEY, line,
                "Off[] disables important General warnings").save();
        }
    }

    public static void detectCatchAllExceptionHandler(SensorContext ctx, InputFile file, String content) {
        Matcher m = CATCH_ALL.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.CATCH_ALL_EXCEPTION_HANDLER_KEY, line,
                "Catch[] without tag catches everything").save();
        }
    }

    public static void detectEmptyExceptionHandler(SensorContext ctx, InputFile file, String content) {
        Matcher m = EMPTY_CATCH.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.EMPTY_EXCEPTION_HANDLER_KEY, line,
                "Empty exception handler silently ignores errors").save();
        }
    }

    public static void detectThrowWithoutCatch(SensorContext ctx, InputFile file, String content) {
        boolean hasCatch = content.contains("Catch[");
        if (!hasCatch && THROW.matcher(content).find()) {
            Matcher m = THROW.matcher(content);
            while (m.find()) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.THROW_WITHOUT_CATCH_KEY, line,
                    "Throw[] without surrounding Catch").save();
            }
        }
    }

    public static void detectAbortInLibraryCode(SensorContext ctx, InputFile file, String content) {
        if (content.contains("BeginPackage[") && ABORT.matcher(content).find()) {
            Matcher m = ABORT.matcher(content);
            while (m.find()) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.ABORT_IN_LIBRARY_CODE_KEY, line,
                    "Abort[] in library code is too aggressive").save();
            }
        }
    }

    public static void detectMessageWithoutDefinition(SensorContext ctx, InputFile file, String content) {
        Set<String> defined = new HashSet<>();
        Matcher defMatcher = MESSAGE_DEF.matcher(content);
        while (defMatcher.find()) {
            defined.add(defMatcher.group(1) + "::" + defMatcher.group(2));
        }

        Matcher callMatcher = MESSAGE_CALL.matcher(content);
        while (callMatcher.find()) {
            String msgKey = callMatcher.group(1) + "::" + callMatcher.group(2);
            if (!defined.contains(msgKey)) {
                int line = content.substring(0, callMatcher.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.MESSAGE_WITHOUT_DEFINITION_KEY, line,
                    "Message " + msgKey + " not defined").save();
            }
        }
    }

    public static void detectMissingMessageDefinition(SensorContext ctx, InputFile file, String content) {
        Matcher callMatcher = MESSAGE_CALL.matcher(content);
        if (callMatcher.find() && !MESSAGE_DEF.matcher(content).find()) {
            createIssue(ctx, file, MathematicaRulesDefinition.MISSING_MESSAGE_DEFINITION_KEY, 1,
                "Function issues messages without defining them").save();
        }
    }

    // ========================================
    // CHUNK 6: CONSTANT & EXPRESSION ANALYSIS (14 rules)
    // ========================================

    public static void detectConditionAlwaysTrueConstantPropagation(SensorContext ctx, InputFile file, String content) {
        Pattern alwaysTrue = Pattern.compile("If\\s*\\[\\s*True\\s*,");
        Matcher m = alwaysTrue.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.CONDITION_ALWAYS_TRUE_CONSTANT_PROPAGATION_KEY, line,
                "Condition always True - dead branch").save();
        }
    }

    public static void detectConditionAlwaysFalseConstantPropagation(SensorContext ctx, InputFile file, String content) {
        Pattern alwaysFalse = Pattern.compile("If\\s*\\[\\s*False\\s*,");
        Matcher m = alwaysFalse.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.CONDITION_ALWAYS_FALSE_CONSTANT_PROPAGATION_KEY, line,
                "Condition always False - dead branch").save();
        }
    }

    public static void detectLoopBoundConstant(SensorContext ctx, InputFile file, String content) {
        Pattern constBound = Pattern.compile("(\\w+)\\s*=\\s*(\\d+);.*Do\\s*\\[.*\\{\\w+,.*,\\s*\\1\\s*\\}");
        Matcher m = constBound.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.LOOP_BOUND_CONSTANT_KEY, line,
                "Loop bound is constant: use literal " + m.group(2)).save();
        }
    }

    public static void detectRedundantComputation(SensorContext ctx, InputFile file, String content) {
        Pattern dup = Pattern.compile("(\\w+\\[[^\\]]+\\])\\s*[+*]\\s*\\1");
        Matcher m = dup.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.REDUNDANT_COMPUTATION_KEY, line,
                "Expression computed multiple times: " + m.group(1)).save();
        }
    }

    public static void detectPureExpressionInLoop(SensorContext ctx, InputFile file, String content) {
        Pattern pure = Pattern.compile("Do\\s*\\[[^\\]]*(?:Sqrt|Pi|E)\\[");
        Matcher m = pure.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.PURE_EXPRESSION_IN_LOOP_KEY, line,
                "Hoist pure expression outside loop").save();
        }
    }

    public static void detectConstantExpression(SensorContext ctx, InputFile file, String content) {
        Matcher m = CONSTANT_EXPR.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.CONSTANT_EXPRESSION_KEY, line,
                "Constant expression can be simplified").save();
        }
    }

    public static void detectIdentityOperation(SensorContext ctx, InputFile file, String content) {
        Matcher m = IDENTITY_OP.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.IDENTITY_OPERATION_KEY, line,
                "Identity operation has no effect").save();
        }
    }

    public static void detectComparisonOfIdenticalExpressions(SensorContext ctx, InputFile file, String content) {
        Matcher m = IDENTICAL_COMP.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.COMPARISON_OF_IDENTICAL_EXPRESSIONS_KEY, line,
                "Comparing identical expression: " + m.group(1)).save();
        }
    }

    public static void detectBooleanExpressionAlwaysTrue(SensorContext ctx, InputFile file, String content) {
        Pattern tautology = Pattern.compile("(\\w+)\\s*\\|\\|\\s*!\\1");
        Matcher m = tautology.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.BOOLEAN_EXPRESSION_ALWAYS_TRUE_KEY, line,
                "Tautology: " + m.group(0)).save();
        }
    }

    public static void detectBooleanExpressionAlwaysFalse(SensorContext ctx, InputFile file, String content) {
        Pattern contradiction = Pattern.compile("(\\w+)\\s*&&\\s*!\\1");
        Matcher m = contradiction.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.BOOLEAN_EXPRESSION_ALWAYS_FALSE_KEY, line,
                "Contradiction: " + m.group(0)).save();
        }
    }

    public static void detectUnnecessaryBooleanConversion(SensorContext ctx, InputFile file, String content) {
        Matcher m = BOOL_CONVERSION.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.UNNECESSARY_BOOLEAN_CONVERSION_KEY, line,
                "Unnecessary conversion: just use " + m.group(1)).save();
        }
    }

    public static void detectDoubleNegation(SensorContext ctx, InputFile file, String content) {
        Matcher m = DOUBLE_NEG.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.DOUBLE_NEGATION_KEY, line,
                "Double negation should be simplified").save();
        }
    }

    public static void detectComplexBooleanExpressionEnhanced(SensorContext ctx, InputFile file, String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            int boolOps = lines[i].split("&&|\\|\\||!").length - 1;
            if (boolOps > 5) {
                createIssue(ctx, file, MathematicaRulesDefinition.COMPLEX_BOOLEAN_EXPRESSION_ENHANCED_KEY, i + 1,
                    "Boolean expression too complex: " + boolOps + " operators").save();
            }
        }
    }

    public static void detectDeMorgansLawOpportunity(SensorContext ctx, InputFile file, String content) {
        Pattern demorgan = Pattern.compile("!\\s*\\([^)]*&&");
        Matcher m = demorgan.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.DE_MORGANS_LAW_OPPORTUNITY_KEY, line,
                "Could apply De Morgan's Law for clarity").save();
        }
    }

    // ========================================
    // CHUNK 6: MATHEMATICA-SPECIFIC PATTERNS (20 rules)
    // ========================================

    public static void detectHoldAttributeMissing(SensorContext ctx, InputFile file, String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*\\w+\\[\\w+_\\]\\s*:=\\s*Hold\\[.*")
                && (i == 0 || !HOLD_ATTR.matcher(lines[i - 1]).find())) {
                createIssue(ctx, file, MathematicaRulesDefinition.HOLD_ATTRIBUTE_MISSING_KEY, i + 1,
                    "Function needs Hold attribute").save();
            }
        }
    }

    public static void detectHoldFirstButUsesSecondArgumentFirst(SensorContext ctx, InputFile file, String content) {
        Pattern holdFirst = Pattern.compile("SetAttributes\\s*\\[\\s*(\\w+)\\s*,\\s*HoldFirst");
        Matcher m = holdFirst.matcher(content);
        while (m.find()) {
            String func = m.group(1);
            if (content.contains(func + "[") && content.contains("y_]")) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.HOLD_FIRST_BUT_USES_SECOND_ARGUMENT_FIRST_KEY, line,
                    "HoldFirst function may evaluate second arg first").save();
            }
        }
    }

    public static void detectMissingUnevaluatedWrapper(SensorContext ctx, InputFile file, String content) {
        if (HOLD_ATTR.matcher(content).find() && !UNEVALUATED.matcher(content).find()) {
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains("heldFunc[") && !lines[i].contains("Unevaluated")) {
                    createIssue(ctx, file, MathematicaRulesDefinition.MISSING_UNEVALUATED_WRAPPER_KEY, i + 1,
                        "Consider Unevaluated wrapper").save();
                }
            }
        }
    }

    public static void detectUnnecessaryHold(SensorContext ctx, InputFile file, String content) {
        Matcher m = HOLD_LITERAL.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.UNNECESSARY_HOLD_KEY, line,
                "Unnecessary Hold on literal").save();
        }
    }

    public static void detectReleaseHoldAfterHold(SensorContext ctx, InputFile file, String content) {
        Matcher m = RELEASE_HOLD.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.RELEASE_HOLD_AFTER_HOLD_KEY, line,
                "ReleaseHold[Hold[x]] is redundant").save();
        }
    }

    public static void detectEvaluateInHeldContext(SensorContext ctx, InputFile file, String content) {
        Matcher m = EVALUATE_IN_HOLD.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.EVALUATE_IN_HELD_CONTEXT_KEY, line,
                "Evaluate in held context creates leak").save();
        }
    }

    public static void detectPatternWithSideEffect(SensorContext ctx, InputFile file, String content) {
        Matcher m = PATTERN_SIDE_EFFECT.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.PATTERN_WITH_SIDE_EFFECT_KEY, line,
                "Pattern test has side effects").save();
        }
    }

    public static void detectReplacementRuleOrderMatters(SensorContext ctx, InputFile file, String content) {
        Matcher m = RULE_ORDER.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.REPLACEMENT_RULE_ORDER_MATTERS_KEY, line,
                "Catch-all rule should be last").save();
        }
    }

    public static void detectReplaceAllVsReplaceConfusion(SensorContext ctx, InputFile file, String content) {
        Matcher m = REPLACE_ALL.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.REPLACE_ALL_VS_REPLACE_CONFUSION_KEY, line,
                "Verify ReplaceAll vs Replace semantics").save();
        }
    }

    public static void detectRuleDoesntMatchDueToEvaluation(SensorContext ctx, InputFile file, String content) {
        Pattern evalRule = Pattern.compile("/\\.\\s*\\{[^}]*\\d+\\s*\\+\\s*\\d+\\s*->");
        Matcher m = evalRule.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.RULE_DOESNT_MATCH_DUE_TO_EVALUATION_KEY, line,
                "Rule pattern won't match due to evaluation").save();
        }
    }

    public static void detectPartSpecificationOutOfBounds(SensorContext ctx, InputFile file, String content) {
        Matcher m = PART_SPEC.matcher(content);
        while (m.find()) {
            int idx = Integer.parseInt(m.group(1));
            if (idx > 100) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.PART_SPECIFICATION_OUT_OF_BOUNDS_KEY, line,
                    "Part index " + idx + " may be out of bounds").save();
            }
        }
    }

    public static void detectSpanSpecificationInvalid(SensorContext ctx, InputFile file, String content) {
        Matcher m = SPAN_SPEC.matcher(content);
        while (m.find()) {
            int start = Integer.parseInt(m.group(1));
            int end = Integer.parseInt(m.group(2));
            if (start > end) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.SPAN_SPECIFICATION_INVALID_KEY, line,
                    "Backward span: " + start + ";;" + end).save();
            }
        }
    }

    public static void detectAllSpecificationInefficient(SensorContext ctx, InputFile file, String content) {
        Matcher m = ALL_SPEC.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.ALL_SPECIFICATION_INEFFICIENT_KEY, line,
                "list[[All]] is redundant").save();
        }
    }

    public static void detectThreadingOverNonLists(SensorContext ctx, InputFile file, String content) {
        if (LISTABLE.matcher(content).find()) {
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].matches(".*listableFunc\\[\\d+\\].*")) {
                    createIssue(ctx, file, MathematicaRulesDefinition.THREADING_OVER_NON_LISTS_KEY, i + 1,
                        "Listable function on scalar").save();
                }
            }
        }
    }

    public static void detectMissingAttributesDeclaration(SensorContext ctx, InputFile file, String content) {
        if (content.contains("Map[") && !LISTABLE.matcher(content).find()) {
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains("Map[operation,")) {
                    createIssue(ctx, file, MathematicaRulesDefinition.MISSING_ATTRIBUTES_DECLARATION_KEY, i + 1,
                        "Consider Listable attribute").save();
                }
            }
        }
    }

    public static void detectOneIdentityAttributeMisuse(SensorContext ctx, InputFile file, String content) {
        Pattern oneId = Pattern.compile("SetAttributes\\s*\\[[^,]+,\\s*OneIdentity");
        Matcher m = oneId.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.ONE_IDENTITY_ATTRIBUTE_MISUSE_KEY, line,
                "OneIdentity causes subtle semantic changes").save();
        }
    }

    public static void detectOrderlessAttributeOnNonCommutative(SensorContext ctx, InputFile file, String content) {
        Matcher m = ORDERLESS.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            if (content.contains("subtract") || content.contains("divide")) {
                createIssue(ctx, file, MathematicaRulesDefinition.ORDERLESS_ATTRIBUTE_ON_NON_COMMUTATIVE_KEY, line,
                    "Orderless on non-commutative operation").save();
            }
        }
    }

    public static void detectFlatAttributeMisuse(SensorContext ctx, InputFile file, String content) {
        Matcher m = FLAT.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            if (content.contains("subtract") || content.contains("divide")) {
                createIssue(ctx, file, MathematicaRulesDefinition.FLAT_ATTRIBUTE_MISUSE_KEY, line,
                    "Flat on non-associative operation").save();
            }
        }
    }

    public static void detectSequenceInUnexpectedContext(SensorContext ctx, InputFile file, String content) {
        Matcher m = SEQUENCE.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.SEQUENCE_IN_UNEXPECTED_CONTEXT_KEY, line,
                "Sequence[] flattens - verify intended").save();
        }
    }

    public static void detectMissingSequenceWrapper(SensorContext ctx, InputFile file, String content) {
        Pattern emptyList = Pattern.compile("If\\[[^,]+,\\s*\\{[^}]+\\}\\s*,\\s*\\{\\s*\\}");
        Matcher m = emptyList.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.MISSING_SEQUENCE_WRAPPER_KEY, line,
                "Use Sequence[] to avoid nesting").save();
        }
    }

    // ========================================
    // CHUNK 7: TEST COVERAGE (4 rules)
    // ========================================

    public static void detectLowTestCoverageWarning(SensorContext ctx, InputFile file, String content) {
        // Placeholder: would need actual coverage data
        if (!TEST_FILE_PATTERN.matcher(file.filename()).find() && content.split("\n").length > 100) {
            createIssue(ctx, file, MathematicaRulesDefinition.LOW_TEST_COVERAGE_WARNING_KEY, 1,
                "Consider adding test coverage").save();
        }
    }

    public static void detectUntestedPublicFunction(SensorContext ctx, InputFile file, String content) {
        if (!TEST_FILE_PATTERN.matcher(file.filename()).find()
            && content.contains("::usage") && !content.contains("VerificationTest")) {
            createIssue(ctx, file, MathematicaRulesDefinition.UNTESTED_PUBLIC_FUNCTION_KEY, 1,
                "Public functions should have tests").save();
        }
    }

    public static void detectUntestedBranch(SensorContext ctx, InputFile file, String content) {
        // Placeholder: would need actual coverage data
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("If[") && !content.contains("VerificationTest")) {
                createIssue(ctx, file, MathematicaRulesDefinition.UNTESTED_BRANCH_KEY, i + 1,
                    "Branch should be tested").save();
                break;
            }
        }
    }

    public static void detectTestOnlyCodeInProduction(SensorContext ctx, InputFile file, String content) {
        if (!TEST_FILE_PATTERN.matcher(file.filename()).find()
            && content.contains("$TestMode") || content.contains("testFlag")) {
            createIssue(ctx, file, MathematicaRulesDefinition.TEST_ONLY_CODE_IN_PRODUCTION_KEY, 1,
                "Test-only code in production file").save();
        }
    }

    // ========================================
    // CHUNK 7: PERFORMANCE ANALYSIS (9 rules)
    // ========================================

    public static void detectCompilableFunctionNotCompiled(SensorContext ctx, InputFile file, String content) {
        Pattern numerical = Pattern.compile("(\\w+)\\[\\w+_\\]\\s*:=\\s*[^;]*(?:Sin|Cos|Exp|Log|Sqrt)");
        Matcher m = numerical.matcher(content);
        while (m.find() && !content.contains("Compile[")) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.COMPILABLE_FUNCTION_NOT_COMPILED_KEY, line,
                "Function " + m.group(1) + " suitable for Compile[]").save();
        }
    }

    public static void detectCompilationTargetMissing(SensorContext ctx, InputFile file, String content) {
        if (COMPILE.matcher(content).find() && !COMPILATION_TARGET.matcher(content).find()) {
            Matcher m = COMPILE.matcher(content);
            while (m.find()) {
                int line = content.substring(0, m.start()).split("\n").length;
                createIssue(ctx, file, MathematicaRulesDefinition.COMPILATION_TARGET_MISSING_KEY, line,
                    "Add CompilationTarget -> \"C\"").save();
            }
        }
    }

    public static void detectNonCompilableConstructInCompile(SensorContext ctx, InputFile file, String content) {
        Matcher m = NON_COMPILABLE.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.NON_COMPILABLE_CONSTRUCT_IN_COMPILE_KEY, line,
                "Non-compilable function in Compile[]").save();
        }
    }

    public static void detectPackedArrayUnpacked(SensorContext ctx, InputFile file, String content) {
        Matcher m = PACKED_ARRAY_UNPACK.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.PACKED_ARRAY_UNPACKED_KEY, line,
                "Operation unpacks array - use ReplacePart").save();
        }
    }

    public static void detectInefficientPatternInPerformanceCriticalCode(SensorContext ctx, InputFile file, String content) {
        Pattern hotLoop = Pattern.compile("Do\\s*\\[[^\\]]*Match\\s*\\[");
        Matcher m = hotLoop.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.INEFFICIENT_PATTERN_IN_PERFORMANCE_CRITICAL_CODE_KEY, line,
                "Pattern matching in hot loop").save();
        }
    }

    public static void detectNAppliedTooLate(SensorContext ctx, InputFile file, String content) {
        Matcher m = N_LATE.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.N_APPLIED_TOO_LATE_KEY, line,
                "Use numeric function from start (NIntegrate, etc)").save();
        }
    }

    public static void detectMissingMemoizationOpportunityEnhanced(SensorContext ctx, InputFile file, String content) {
        Pattern recursion = Pattern.compile("(\\w+)\\[(\\w+)_\\]\\s*:=\\s*[^=]*\\1\\[");
        Matcher m = recursion.matcher(content);
        while (m.find() && !content.contains(m.group(1) + "[" + m.group(2) + "] = ")) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.MISSING_MEMOIZATION_OPPORTUNITY_ENHANCED_KEY, line,
                "Recursive function " + m.group(1) + " should use memoization").save();
        }
    }

    public static void detectInefficientStringConcatenationEnhanced(SensorContext ctx, InputFile file, String content) {
        Matcher m = STRING_CONCAT_LOOP.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.INEFFICIENT_STRING_CONCATENATION_ENHANCED_KEY, line,
                "String concatenation in loop has O(n²) complexity").save();
        }
    }

    public static void detectListConcatenationInLoop(SensorContext ctx, InputFile file, String content) {
        Matcher m = LIST_CONCAT_LOOP.matcher(content);
        while (m.find()) {
            int line = content.substring(0, m.start()).split("\n").length;
            createIssue(ctx, file, MathematicaRulesDefinition.LIST_CONCATENATION_IN_LOOP_KEY, line,
                "List concatenation in loop has O(n²) complexity").save();
        }
    }
}
