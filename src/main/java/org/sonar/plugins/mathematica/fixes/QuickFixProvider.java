package org.sonar.plugins.mathematica.fixes;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.fix.NewQuickFix;
import org.sonar.api.batch.sensor.issue.fix.NewInputFileEdit;
import org.sonar.api.batch.sensor.issue.fix.NewTextEdit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quick Fix Provider for Mathematica code issues.
 *
 * Provides automated one-click fixes for 50+ common code issues.
 * Integrates with SonarLint to show "Quick Fix" buttons in IDEs.
 *
 * ARCHITECTURE:
 * - NewQuickFix → NewInputFileEdit → NewTextEdit
 * - Each fix method creates appropriate text replacements
 * - Supports single-edit and multi-edit fixes
 *
 * See QUICK_FIXES.md for complete documentation.
 */
public class QuickFixProvider {

    /**
     * Adds a Quick Fix to an issue if available for the rule.
     *
     * @param issue The NewIssue to potentially add a fix to
     * @param inputFile The file containing the issue
     * @param ruleKey The rule key identifying which rule triggered
     * @param fileContent The full file content
     * @param issueStartOffset Character offset where issue starts
     * @param issueEndOffset Character offset where issue ends
     * @param context Additional context for the fix
     */
    public void addQuickFix(
            NewIssue issue,
            InputFile inputFile,
            String ruleKey,
            String fileContent,
            int issueStartOffset,
            int issueEndOffset,
            QuickFixContext context) {

        try {
            // Dispatch to rule-specific fix handler
            switch (ruleKey) {
                // ===== CODE SMELLS =====
                case "EmptyBlock":
                    addEmptyBlockFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "DebugCodeLeftInProduction":
                    addRemoveDebugCodeFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "DoubleSemicolon":
                    addRemoveDoubleSemicolonFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "DoubleTranspose":
                    addRemoveDoubleTransposeFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "DoubleNegation":
                    addRemoveDoubleNegationFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "UnnecessaryBooleanConversion":
                    addSimplifyBooleanFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "IdentityOperation":
                    addRemoveIdentityOperationFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "ReverseReverse":
                    addRemoveDoubleReverseFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "GlobalContext":
                    addRemoveGlobalContextFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "ComparisonWithNull":
                    addFixComparisonWithNullFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                // ===== PHASE 3: SIMPLE REPLACEMENTS =====
                case "DeprecatedFunction":
                    addDeprecatedFunctionFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "StringConcatInLoop":
                    addStringConcatInLoopFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "AppendInLoop":
                    addAppendInLoopFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "StringJoinForTemplates":
                    addStringJoinForTemplatesFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "PositionInsteadOfPattern":
                    addPositionInsteadOfPatternFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "FlattenTableAntipattern":
                    addFlattenTableFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "UnnecessaryTranspose":
                    addRemoveDoubleTransposeFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                // ===== PHASE 4: COMMON BUG FIXES =====
                case "AssignmentInConditional":
                    addAssignmentInConditionalFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "FloatingPointEquality":
                    addFloatingPointEqualityFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "SetDelayedConfusion":
                    addSetDelayedConfusionFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "FunctionWithoutReturn":
                    addFunctionWithoutReturnFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "IdenticalBranches":
                    addIdenticalBranchesFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "InconsistentRuleTypes":
                    addInconsistentRuleTypesFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "OffByOne":
                    addOffByOneFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "IncorrectSetInScoping":
                    addIncorrectSetInScopingFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "EmptyStatement":
                    addRemoveDoubleSemicolonFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                // ===== PHASE 5: ADDING SAFETY =====
                default:
                    addAdditionalQuickFixes(issue, inputFile, ruleKey, fileContent, issueStartOffset, issueEndOffset, context);
                    break;
            }
        } catch (Exception e) {
            // If fix generation fails, just skip it (don't break the analysis)
            // The issue will still be reported, just without a quick fix
        }
    }

    private void addAdditionalQuickFixes(
            NewIssue issue,
            InputFile inputFile,
            String ruleKey,
            String fileContent,
            int issueStartOffset,
            int issueEndOffset,
            QuickFixContext context) {

        try {
            switch (ruleKey) {
                case "MissingFailedCheck":
                    addMissingFailedCheckFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "MissingEmptyListCheck":
                    addMissingEmptyListCheckFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "MissingPatternTest":
                    addMissingPatternTestFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "MissingCompilationTarget":
                    addMissingCompilationTargetFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                // ===== PHASE 6: SIMPLIFICATIONS =====
                case "MachinePrecisionInSymbolic":
                    addMachinePrecisionInSymbolicFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "ComplexBooleanExpression":
                    addComplexBooleanFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                // ===== PHASE 7: ADDITIONAL PERFORMANCE FIXES =====
                case "LinearSearchInsteadOfLookup":
                    addLinearSearchInsteadOfLookupFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "DeleteDuplicatesOnLargeData":
                    addDeleteDuplicatesOnLargeDataFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "NestedMapTable":
                    addNestedMapTableFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "RepeatedCalculations":
                    addRepeatedCalculationsFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "PackedArrayBreaking":
                    addPackedArrayBreakingFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "UnpackingPackedArrays":
                    addUnpackingPackedArraysFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                // ===== PHASE 8: ADDITIONAL BUG & PATTERN FIXES =====
                case "TypeMismatch":
                    addTypeMismatchFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "BlockModuleMisuse":
                    addBlockModuleMisuseFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "PatternBlanksMisuse":
                    addPatternBlanksMisuseFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "ExcessivePureFunctions":
                    addExcessivePureFunctionsFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "MissingOperatorPrecedence":
                    addMissingOperatorPrecedenceFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "MismatchedDimensions":
                    addMismatchedDimensionsFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "ZeroDenominator":
                    addZeroDenominatorFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "MissingHoldAttributes":
                    addMissingHoldAttributesFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "UnprotectedSymbols":
                    addUnprotectedSymbolsFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                // ===== PHASE 9: CODE ORGANIZATION FIXES =====
                case "UnusedVariables":
                    addUnusedVariablesFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "EmptyCatchBlock":
                    addEmptyCatchBlockFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "RepeatedPartExtraction":
                    addRepeatedPartExtractionFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "NestedListsInsteadAssociation":
                    addNestedListsInsteadAssociationFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "MissingMemoization":
                    addMissingMemoizationFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                case "HardcodedFilePaths":
                    addHardcodedFilePathsFix(issue, inputFile, fileContent, issueStartOffset, issueEndOffset);
                    break;

                default:
                    // No quick fix available for this rule
                    break;
            }
        } catch (Exception e) {
            // Quick fix generation failed - skip silently
        }
    }

    // ============================================================================
    // CODE SMELL FIXES
    // ============================================================================

    /**
     * Fix: Remove empty block (Module[], Block[], With[] with no body)
     * Example: Module[{x}, ] → (removed)
     */
    private void addEmptyBlockFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove empty block");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(""); // Delete the empty block

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Remove debug code (Print[], Echo[], etc.)
     * Example: Print["debug: ", x]; → (removed)
     */
    private void addRemoveDebugCodeFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove debug code");

        // Find the full statement (up to semicolon or newline)
        int statementEnd = findStatementEnd(content, end);

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, statementEnd);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(""); // Delete the debug statement

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Remove double semicolon
     * Example: x = 5;; → x = 5;
     */
    private void addRemoveDoubleSemicolonFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove extra semicolon");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceFirst(";;", ";");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Remove double transpose
     * Example: Transpose[Transpose[x]] → x
     */
    private void addRemoveDoubleTransposeFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove redundant double transpose");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("Transpose\\s*+\\[\\s*+Transpose\\s*+\\[([^\\]]+)\\]\\s*+\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String innerExpression = matcher.group(1);

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(innerExpression);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Remove double negation
     * Example: !!x → x or Not[Not[x]] → x
     */
    private void addRemoveDoubleNegationFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove double negation");

        String originalText = content.substring(start, end);
        String fixedText = originalText
            .replaceFirst("!!([a-zA-Z]\\w*)", "$1")
            .replaceFirst("Not\\s*+\\[\\s*+Not\\s*+\\[([^\\]]+)\\]\\s*+\\]", "$1");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Simplify unnecessary boolean conversion
     * Example: If[x, True, False] → x
     */
    private void addSimplifyBooleanFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Simplify boolean expression");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("If\\s*+\\[([^,]+),\\s*+True\\s*+,\\s*+False\\s*+\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String condition = matcher.group(1).trim();

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(condition);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Remove identity operations
     * Example: x + 0 → x, x * 1 → x, x^1 → x
     */
    private void addRemoveIdentityOperationFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove identity operation");

        String originalText = content.substring(start, end);
        String fixedText = originalText
            .replaceFirst("([a-zA-Z]\\w*)\\s*+\\+\\s*+0", "$1")
            .replaceFirst("([a-zA-Z]\\w*)\\s*+\\*\\s*+1", "$1")
            .replaceFirst("([a-zA-Z]\\w*)\\s*+\\^\\s*+1", "$1")
            .replaceFirst("0\\s*+\\+\\s*+([a-zA-Z]\\w*)", "$1")
            .replaceFirst("1\\s*+\\*\\s*+([a-zA-Z]\\w*)", "$1");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Remove double Reverse
     * Example: Reverse[Reverse[x]] → x
     */
    private void addRemoveDoubleReverseFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove redundant double reverse");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("Reverse\\s*+\\[\\s*+Reverse\\s*+\\[([^\\]]+)\\]\\s*+\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String innerExpression = matcher.group(1);

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(innerExpression);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Remove Global` context prefix
     * Example: Global`x → x
     */
    private void addRemoveGlobalContextFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove Global` context prefix");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replace("Global`", "");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    // ============================================================================
    // BUG FIXES
    // ============================================================================

    /**
     * Fix: Change == to === for Null comparison
     * Example: x == Null → x === Null
     */
    private void addFixComparisonWithNullFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use === for Null comparison");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replace("== Null", "=== Null")
                                      .replace("==Null", "===Null");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    // ============================================================================
    // PHASE 3: SIMPLE REPLACEMENTS
    // ============================================================================

    /**
     * Fix: Replace deprecated $RecursionLimit with $IterationLimit
     * Example: $RecursionLimit → $IterationLimit
     */
    private void addDeprecatedFunctionFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Replace with $IterationLimit");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replace("$RecursionLimit", "$IterationLimit");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Replace string concatenation in loop with StringJoin
     * Example: Do[str = str <> x, ...] → str = StringJoin[Table[x, ...]]
     */
    private void addStringConcatInLoopFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use StringJoin with Table instead");

        // Note: This is a suggestion-level fix that requires manual adaptation
        // We'll add a comment explaining the pattern
        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: str = StringJoin[Table[expr, iterator]] *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Replace AppendTo in loop with Table
     * Example: Do[list = Append[list, x], ...] → list = Table[x, ...]
     */
    private void addAppendInLoopFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use Table instead of AppendTo in loop");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: list = Table[expr, iterator] *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Replace multiple StringJoin with StringTemplate
     * Example: a <> b <> c <> d → StringJoin[a, b, c, d]
     */
    private void addStringJoinForTemplatesFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Simplify with StringJoin");

        String originalText = content.substring(start, end);
        // Extract parts between <>
        String[] parts = originalText.split("\\s*+<>\\s*+");
        String fixedText = "StringJoin[" + String.join(", ", parts) + "]";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Replace Extract[list, Position[...]] with Cases
     * Example: Extract[list, Position[list, pattern]] → Cases[list, pattern]
     */
    private void addPositionInsteadOfPatternFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use Cases instead of Extract/Position");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("Extract\\s*+\\[([^,]+),\\s*+Position\\s*+\\[\\1,\\s*+([^\\]]+)\\]\\s*+\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String listVar = matcher.group(1);
            String patternExpr = matcher.group(2);
            String fixedText = "Cases[" + listVar + ", " + patternExpr + "]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Replace Flatten[Table[...]] with Catenate
     * Example: Flatten[Table[f[x], {x, n}]] → Array[f, n]
     */
    private void addFlattenTableFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use Array or Catenate instead");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: Array[func, n] or Catenate[Table[...]] *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    // ============================================================================
    // PHASE 4: COMMON BUG FIXES
    // ============================================================================

    /**
     * Fix: Replace = with == in conditional
     * Example: If[x = 5, ...] → If[x == 5, ...]
     */
    private void addAssignmentInConditionalFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Change assignment (=) to comparison (==)");

        String originalText = content.substring(start, end);
        // Replace single = with == but not := or ===
        String fixedText = originalText.replaceAll("(?<![:=])=(?!=)", "==");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Replace floating point == with tolerance check
     * Example: x == 1.5 → Abs[x - 1.5] < 10^-6
     */
    private void addFloatingPointEqualityFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use tolerance-based comparison");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*+==\\s*+(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String variable = matcher.group(1);
            String value = matcher.group(2);
            String fixedText = "Abs[" + variable + " - " + value + "] < 10^-6";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Replace = with := in function definition
     * Example: f[x_] = x^2 → f[x_] := x^2
     */
    private void addSetDelayedConfusionFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use SetDelayed (:=) for function definitions");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceFirst("([a-zA-Z]\\w*\\[[^\\]]+\\])\\s*+=\\s*+", "$1 := ");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Remove trailing semicolon from function body
     * Example: f[x_] := (y = x + 1;) → f[x_] := (y = x + 1)
     */
    private void addFunctionWithoutReturnFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove trailing semicolon");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceFirst(";\\s*+\\)\\s*+$", ")");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Simplify If with identical branches
     * Example: If[cond, x, x] → x
     */
    private void addIdenticalBranchesFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove conditional with identical branches");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("If\\s*+\\[[^,]+,\\s*+([^,]+),\\s*+\\1\\s*+\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String result = matcher.group(1).trim();

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(result);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Standardize rule types to :>
     * Example: {a -> 1, b :> 2} → {a :> 1, b :> 2}
     */
    private void addInconsistentRuleTypesFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Standardize to RuleDelayed (:>)");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceAll("([a-zA-Z]\\w*)\\s*+->\\s*+", "$1 :> ");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Fix loop bounds (0-indexed to 1-indexed)
     * Example: Do[..., {i, 0, n}] → Do[..., {i, 1, n}]
     */
    private void addOffByOneFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Fix loop bounds (start from 1)");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceFirst("\\{([a-zA-Z]\\w*),\\s*+0,", "{$1, 1,");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Move assignment out of Module variable list
     * Example: Module[{x = 5}, ...] → Module[{x}, x = 5; ...]
     */
    private void addIncorrectSetInScopingFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Move assignment into Module body");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("Module\\s*+\\[\\s*+\\{([a-zA-Z]\\w*)\\s*+=\\s*+([^}]+)\\}\\s*+,\\s*+(.+)\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String varName = matcher.group(1);
            String value = matcher.group(2).trim();
            String body = matcher.group(3).trim();
            String fixedText = "Module[{" + varName + "}, " + varName + " = " + value + "; " + body + "]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    // ============================================================================
    // PHASE 5: ADDING SAFETY
    // ============================================================================

    /**
     * Fix: Add $Failed check after Import/Get
     * Example: data = Import["file"] → data = Import["file"]; If[data === $Failed, Message[...]]
     */
    private void addMissingFailedCheckFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add $Failed check");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*+=\\s*+(Import|Get)\\s*+\\[");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String varName = matcher.group(1);
            String suggestion = originalText + ";\nIf[" + varName + " === $Failed, (* handle error *)]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(suggestion);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Add empty list check before First/Last
     * Example: First[list] → If[list =!= {}, First[list], (* default *)]
     */
    private void addMissingEmptyListCheckFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add empty list check");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("(First|Last)\\s*+\\[([a-zA-Z]\\w*)\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String func = matcher.group(1);
            String listVar = matcher.group(2);
            String fixedText = "If[" + listVar + " =!= {}, " + func + "[" + listVar + "], (* default *)]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Add ?NumericQ pattern test
     * Example: f[x_] := Sqrt[x] → f[x_?NumericQ] := Sqrt[x]
     */
    private void addMissingPatternTestFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add ?NumericQ pattern test");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceFirst("([a-zA-Z]\\w*)_\\s*+\\]", "$1_?NumericQ]");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Add CompilationTarget -> "C"
     * Example: Compile[{x}, ...] → Compile[{x}, ..., CompilationTarget -> "C"]
     */
    private void addMissingCompilationTargetFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add CompilationTarget -> \"C\"");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceFirst("\\]\\s*+$", ", CompilationTarget -> \"C\"]");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    // ============================================================================
    // PHASE 6: SIMPLIFICATIONS
    // ============================================================================

    /**
     * Fix: Replace machine precision with exact number in symbolic computation
     * Example: Solve[x^2 == 2.0] → Solve[x^2 == 2]
     */
    private void addMachinePrecisionInSymbolicFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use exact number instead of floating point");

        String originalText = content.substring(start, end);
        // Remove .0 from numbers
        String fixedText = originalText.replaceAll("(\\d+)\\.0\\b", "$1");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Extract complex boolean to variable
     * Example: If[a && b && c && d && e, ...] → valid = a && b && c && d && e; If[valid, ...]
     */
    private void addComplexBooleanFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Extract complex boolean to variable");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("If\\s*+\\[([^,]+),");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String condition = matcher.group(1).trim();
            String suggestion = "isValid = " + condition + ";\n" + originalText.replace(condition, "isValid");

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(suggestion);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    // ============================================================================
    // PHASE 7: ADDITIONAL PERFORMANCE FIXES
    // ============================================================================

    /**
     * Fix: Replace linear search with Association lookup
     * Example: Select[list, #[[key]] == val &] → Suggest using Association
     */
    private void addLinearSearchInsteadOfLookupFix(NewIssue issue, InputFile inputFile, String content,
                                                     int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use Association for O(1) lookup");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: assoc = Association[Table[item[[key]] -> item, {item, list}]]; assoc[val] *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Replace DeleteDuplicates with GroupBy for large data
     * Example: DeleteDuplicates[list] → Keys@GroupBy[list, Identity]
     */
    private void addDeleteDuplicatesOnLargeDataFix(NewIssue issue, InputFile inputFile, String content,
                                                     int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use GroupBy for better performance");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("DeleteDuplicates\\s*+\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String listExpr = matcher.group(1);
            String fixedText = "Keys@GroupBy[" + listExpr + ", Identity]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Suggest Outer for nested Map/Table
     * Example: Map[f, Map[g, list]] → Suggest using Outer or composition
     */
    private void addNestedMapTableFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Consider using Outer or function composition");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: Outer[..., ..., 1] or composing functions *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Cache repeated calculations
     * Example: expr used multiple times → cachedValue = expr; use cachedValue
     */
    private void addRepeatedCalculationsFix(NewIssue issue, InputFile inputFile, String content,
                                             int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Extract to variable to cache result");

        String originalText = content.substring(start, end);
        String suggestion = "cachedValue = " + originalText + "; (* Then use cachedValue *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Suggest avoiding operations that break packed arrays
     */
    private void addPackedArrayBreakingFix(NewIssue issue, InputFile inputFile, String content,
                                            int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use vectorized operations to maintain packed array");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider using vectorized operations: Map → Listable, Part → Span *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Use vectorized operations instead of element-wise operations
     */
    private void addUnpackingPackedArraysFix(NewIssue issue, InputFile inputFile, String content,
                                              int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Replace with vectorized operation");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: Total, Dot, or Listable functions *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    // ============================================================================
    // PHASE 8: ADDITIONAL BUG & PATTERN FIXES
    // ============================================================================

    /**
     * Fix: Fix type mismatch (string + number)
     * Example: "text" + 5 → "text" <> ToString[5]
     */
    private void addTypeMismatchFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Fix type mismatch with ToString");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*+\\+\\s*+(\\d+)");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String stringPart = matcher.group(1);
            String numberPart = matcher.group(2);
            String fixedText = "\"" + stringPart + "\" <> ToString[" + numberPart + "]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Suggest Module instead of Block
     * Example: Block[{x}, ...] → Module[{x}, ...]
     */
    private void addBlockModuleMisuseFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use Module for lexical scoping");

        String originalText = content.substring(start, end);
        String fixedText = originalText.replaceFirst("Block\\s*+\\[", "Module[");

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(fixedText);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Fix pattern blank misuse
     * Example: Length[x__] → Length[{x}]
     */
    private void addPatternBlanksMisuseFix(NewIssue issue, InputFile inputFile, String content,
                                            int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Wrap sequence pattern in list");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("(\\w+)\\s*+\\[([a-zA-Z]\\w*)__\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String func = matcher.group(1);
            String var = matcher.group(2);
            String fixedText = func + "[{" + var + "}]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Replace excessive slots with Function
     * Example: #1 + #2 + #3 & → Function[{x, y, z}, x + y + z]
     */
    private void addExcessivePureFunctionsFix(NewIssue issue, InputFile inputFile, String content,
                                               int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use Function with named parameters");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: Function[{x, y, z}, ...] for readability *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Add parentheses for operator precedence clarity
     * Example: a /@ b @@ c → (a /@ b) @@ c
     */
    private void addMissingOperatorPrecedenceFix(NewIssue issue, InputFile inputFile, String content,
                                                  int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add parentheses for clarity");

        String originalText = content.substring(start, end);
        String suggestion = "(" + originalText + ") (* Add explicit parentheses to clarify precedence *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Add dimension check for matrix operations
     */
    private void addMismatchedDimensionsFix(NewIssue issue, InputFile inputFile, String content,
                                             int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add dimension validation");

        String originalText = content.substring(start, end);
        String suggestion = originalText + ";\nIf[Dimensions[matrix1][[2]] != Dimensions[matrix2][[1]], Message[...]]";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Add zero denominator check
     */
    private void addZeroDenominatorFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add zero check before division");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*+/\\s*+([a-zA-Z]\\w*)");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String numerator = matcher.group(1);
            String denominator = matcher.group(2);
            String fixedText = "If[" + denominator + " != 0, " + numerator + " / " + denominator + ", (* handle zero *)]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Add SetAttributes for Hold attributes
     */
    private void addMissingHoldAttributesFix(NewIssue issue, InputFile inputFile, String content,
                                              int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add HoldAll attribute");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*+\\[");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String funcName = matcher.group(1);
            String suggestion = "SetAttributes[" + funcName + ", HoldAll];\n" + originalText;

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(suggestion);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Add Protect for public symbols
     */
    private void addUnprotectedSymbolsFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add Protect after definition");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*+\\[");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String funcName = matcher.group(1);
            String suggestion = originalText + ";\nProtect[" + funcName + "]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(suggestion);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    // ============================================================================
    // PHASE 9: CODE ORGANIZATION FIXES
    // ============================================================================

    /**
     * Fix: Remove unused variables from Module
     * Example: Module[{x, unused}, ...] → Module[{x}, ...]
     */
    private void addUnusedVariablesFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Remove unused variable");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Remove unused variable from declaration *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Add error handling to empty catch block
     * Example: Quiet[expr] → Check[expr, $Failed]
     */
    private void addEmptyCatchBlockFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Replace with Check for proper error handling");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("Quiet\\s*+\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String expr = matcher.group(1);
            String fixedText = "Check[" + expr + ", $Failed]";

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Use destructuring for repeated Part extraction
     * Example: {x[[1]], x[[2]]} → {a, b} = x[[{1, 2}]]
     */
    private void addRepeatedPartExtractionFix(NewIssue issue, InputFile inputFile, String content,
                                               int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use destructuring for efficiency");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: {a, b, c} = x[[{1, 2, 3}]] *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Suggest using Association instead of nested lists
     */
    private void addNestedListsInsteadAssociationFix(NewIssue issue, InputFile inputFile, String content,
                                                      int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use Association for key-value data");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: <|key1 -> val1, key2 -> val2|> *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    /**
     * Fix: Add memoization to recursive function
     * Example: f[x_] := expr → f[x_] := f[x] = expr
     */
    private void addMissingMemoizationFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Add memoization pattern");

        String originalText = content.substring(start, end);
        Pattern pattern = Pattern.compile("([a-zA-Z]\\w*\\[[^\\]]+\\])\\s*+:=\\s*+(.++)");
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            String signature = matcher.group(1);
            String body = matcher.group(2);
            String fixedText = signature + " := " + signature.replace("_", "") + " = " + body;

            NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
                .on(inputFile);

            TextRange range = createTextRange(inputFile, content, start, end);
            NewTextEdit textEdit = inputFileEdit.newTextEdit()
                .at(range)
                .withNewText(fixedText);

            inputFileEdit.addTextEdit(textEdit);
            quickFix.addInputFileEdit(inputFileEdit);
            issue.addQuickFix(quickFix);
        }
    }

    /**
     * Fix: Suggest using FileNameJoin for portable paths
     */
    private void addHardcodedFilePathsFix(NewIssue issue, InputFile inputFile, String content, int start, int end) {
        NewQuickFix quickFix = issue.newQuickFix()
            .message("Use FileNameJoin for portable paths");

        String originalText = content.substring(start, end);
        String suggestion = originalText + " (* Consider: FileNameJoin[{$HomeDirectory, \"subdir\", \"file.txt\"}] *)";

        NewInputFileEdit inputFileEdit = quickFix.newInputFileEdit()
            .on(inputFile);

        TextRange range = createTextRange(inputFile, content, start, end);
        NewTextEdit textEdit = inputFileEdit.newTextEdit()
            .at(range)
            .withNewText(suggestion);

        inputFileEdit.addTextEdit(textEdit);
        quickFix.addInputFileEdit(inputFileEdit);
        issue.addQuickFix(quickFix);
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Creates a TextRange from character offsets.
     */
    private TextRange createTextRange(InputFile inputFile, String content, int start, int end) {
        int startLine = calculateLineNumber(content, start);
        int endLine = calculateLineNumber(content, end);

        int startLineOffset = getLineOffset(content, startLine);
        int endLineOffset = getLineOffset(content, endLine);

        int startColumn = start - startLineOffset;
        int endColumn = end - endLineOffset;

        try {
            return inputFile.newRange(startLine, startColumn, endLine, endColumn);
        } catch (Exception e) {
            // Fallback to line-based range if column calculation fails
            return inputFile.selectLine(startLine);
        }
    }

    /**
     * Calculates line number from character offset.
     */
    private int calculateLineNumber(String content, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    /**
     * Gets the character offset where a line starts.
     */
    private int getLineOffset(String content, int lineNumber) {
        int currentLine = 1;
        for (int i = 0; i < content.length(); i++) {
            if (currentLine == lineNumber) {
                return i;
            }
            if (content.charAt(i) == '\n') {
                currentLine++;
            }
        }
        return content.length();
    }

    /**
     * Finds the end of a statement (semicolon or newline).
     */
    private int findStatementEnd(String content, int start) {
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == ';' || c == '\n') {
                return i + 1;
            }
        }
        return content.length();
    }

    /**
     * Context object for passing additional information to fix generators.
     */
    public static class QuickFixContext {
        private String matchedText;
        private String[] regexGroups;
        private Map<String, String> metadata;

        public QuickFixContext() {
            this.metadata = new HashMap<>();
        }

        public String getMatchedText() {
            return matchedText;
        }

        public void setMatchedText(String text) {
            this.matchedText = text;
        }

        public String[] getRegexGroups() {
            return regexGroups;
        }

        public void setRegexGroups(String[] groups) {
            this.regexGroups = groups;
        }

        public void putMetadata(String key, String value) {
            metadata.put(key, value);
        }

        public String getMetadata(String key) {
            return metadata.get(key);
        }
    }
}
