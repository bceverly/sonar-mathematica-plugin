package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdvancedAnalysisDetectorTest {

    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        context = mock(SensorContext.class, RETURNS_DEEP_STUBS);
        inputFile = mock(InputFile.class);

        when(inputFile.filename()).thenReturn("test.m");
        when(inputFile.selectLine(anyInt())).thenReturn(mock(org.sonar.api.batch.fs.TextRange.class));
    }

    // ===== NULL SAFETY TESTS (15 rules) =====

    @Test
    void testDetectNullDereference() {
        String content = "x = Null;\ny = x[[1]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectNullDereference(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingNullCheck() {
        String content = "f[x_] := x[[1]]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMissingNullCheck(context, inputFile, content)
        );
    }

    @Test
    void testDetectNullPassedToNonNullable() {
        String content = "result = Length[Null];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectNullPassedToNonNullable(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentNullHandling() {
        String content = "If[x > 0, result, other];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectInconsistentNullHandling(context, inputFile, content)
        );
    }

    @Test
    void testDetectNullReturnNotDocumented() {
        String content = "f[x_] := Null";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectNullReturnNotDocumented(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonWithNull() {
        String content = "If[x == Null, True, False]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectComparisonWithNull(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingCheckLeadsToNullPropagation() {
        String content = "result = a[b[c[d]]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMissingCheckLeadsToNullPropagation(context, inputFile, content)
        );
    }

    @Test
    void testDetectCheckPatternDoesntHandleAllCases() {
        String content = "Check[expr, default]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectCheckPatternDoesntHandleAllCases(context, inputFile, content)
        );
    }

    @Test
    void testDetectQuietSuppressingImportantMessages() {
        String content = "Quiet[expr]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectQuietSuppressingImportantMessages(context, inputFile, content)
        );
    }

    @Test
    void testDetectOffDisablingImportantWarnings() {
        String content = "Off[General::spell]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectOffDisablingImportantWarnings(context, inputFile, content)
        );
    }

    @Test
    void testDetectCatchAllExceptionHandler() {
        String content = "Catch[expr]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectCatchAllExceptionHandler(context, inputFile, content)
        );
    }

    @Test
    void testDetectEmptyExceptionHandler() {
        String content = "Catch[expr, _, Null &]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectEmptyExceptionHandler(context, inputFile, content)
        );
    }

    @Test
    void testDetectThrowWithoutCatch() {
        String content = "Throw[error]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectThrowWithoutCatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectAbortInLibraryCode() {
        String content = "BeginPackage[\"MyPackage`\"];\nAbort[];\nEndPackage[];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectAbortInLibraryCode(context, inputFile, content)
        );
    }

    @Test
    void testDetectMessageWithoutDefinition() {
        String content = "Message[func::error, arg];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMessageWithoutDefinition(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingMessageDefinition() {
        String content = "Message[func::error];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMissingMessageDefinition(context, inputFile, content)
        );
    }

    // ===== CONSTANT & EXPRESSION ANALYSIS TESTS (14 rules) =====

    @Test
    void testDetectConditionAlwaysTrueConstantPropagation() {
        String content = "If[True, result, other];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectConditionAlwaysTrueConstantPropagation(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionAlwaysFalseConstantPropagation() {
        String content = "If[False, result, other];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectConditionAlwaysFalseConstantPropagation(context, inputFile, content)
        );
    }

    @Test
    void testDetectLoopBoundConstant() {
        String content = "n = 10; Do[expr, {i, 1, n}];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectLoopBoundConstant(context, inputFile, content)
        );
    }

    @Test
    void testDetectRedundantComputation() {
        String content = "result = f[x] + f[x];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectRedundantComputation(context, inputFile, content)
        );
    }

    @Test
    void testDetectPureExpressionInLoop() {
        String content = "Do[x = Sqrt[2], {i, 1, 10}];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectPureExpressionInLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectConstantExpression() {
        String content = "result = x * 1;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectConstantExpression(context, inputFile, content)
        );
    }

    @Test
    void testDetectIdentityOperation() {
        String content = "result = Reverse[Reverse[list]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectIdentityOperation(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonOfIdenticalExpressions() {
        String content = "If[x == x, True, False];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectComparisonOfIdenticalExpressions(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanExpressionAlwaysTrue() {
        String content = "result = x || !x;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectBooleanExpressionAlwaysTrue(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanExpressionAlwaysFalse() {
        String content = "result = x && !x;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectBooleanExpressionAlwaysFalse(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnnecessaryBooleanConversion() {
        String content = "If[x, True, False]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectUnnecessaryBooleanConversion(context, inputFile, content)
        );
    }

    @Test
    void testDetectDoubleNegation() {
        String content = "result = Not[Not[x]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectDoubleNegation(context, inputFile, content)
        );
    }

    @Test
    void testDetectComplexBooleanExpressionEnhanced() {
        String content = "result = a && b || c && d || e && f || g;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectComplexBooleanExpressionEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeMorgansLawOpportunity() {
        String content = "result = !(x && y);";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectDeMorgansLawOpportunity(context, inputFile, content)
        );
    }

    // ===== MATHEMATICA-SPECIFIC PATTERNS TESTS (20 rules) =====

    @Test
    void testDetectHoldAttributeMissing() {
        String content = "f[x_] := Hold[x]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectHoldAttributeMissing(context, inputFile, content)
        );
    }

    @Test
    void testDetectHoldFirstButUsesSecondArgumentFirst() {
        String content = "SetAttributes[f, HoldFirst];\nf[x_, y_] := y;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectHoldFirstButUsesSecondArgumentFirst(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingUnevaluatedWrapper() {
        String content = "SetAttributes[f, HoldAll];\nheldFunc[arg];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMissingUnevaluatedWrapper(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnnecessaryHold() {
        String content = "result = Hold[5];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectUnnecessaryHold(context, inputFile, content)
        );
    }

    @Test
    void testDetectReleaseHoldAfterHold() {
        String content = "result = ReleaseHold[Hold[x]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectReleaseHoldAfterHold(context, inputFile, content)
        );
    }

    @Test
    void testDetectEvaluateInHeldContext() {
        String content = "Hold[Evaluate[expr]]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectEvaluateInHeldContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternWithSideEffect() {
        String content = "f[x_?(Print[\"test\"]&)] := x;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectPatternWithSideEffect(context, inputFile, content)
        );
    }

    @Test
    void testDetectReplacementRuleOrderMatters() {
        String content = "{x_ -> 1, a -> 2}";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectReplacementRuleOrderMatters(context, inputFile, content)
        );
    }

    @Test
    void testDetectReplaceAllVsReplaceConfusion() {
        String content = "expr /. rule";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectReplaceAllVsReplaceConfusion(context, inputFile, content)
        );
    }

    @Test
    void testDetectRuleDoesntMatchDueToEvaluation() {
        String content = "expr /. {1 + 2 -> result}";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectRuleDoesntMatchDueToEvaluation(context, inputFile, content)
        );
    }

    @Test
    void testDetectPartSpecificationOutOfBounds() {
        String content = "result = list[[150]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectPartSpecificationOutOfBounds(context, inputFile, content)
        );
    }

    @Test
    void testDetectSpanSpecificationInvalid() {
        String content = "result = list[[10;;5]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectSpanSpecificationInvalid(context, inputFile, content)
        );
    }

    @Test
    void testDetectAllSpecificationInefficient() {
        String content = "result = list[[All]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectAllSpecificationInefficient(context, inputFile, content)
        );
    }

    @Test
    void testDetectThreadingOverNonLists() {
        String content = "SetAttributes[f, Listable];\nlistableFunc[5];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectThreadingOverNonLists(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingAttributesDeclaration() {
        String content = "Map[operation, list]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMissingAttributesDeclaration(context, inputFile, content)
        );
    }

    @Test
    void testDetectOneIdentityAttributeMisuse() {
        String content = "SetAttributes[f, OneIdentity];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectOneIdentityAttributeMisuse(context, inputFile, content)
        );
    }

    @Test
    void testDetectOrderlessAttributeOnNonCommutative() {
        String content = "SetAttributes[subtract, Orderless];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectOrderlessAttributeOnNonCommutative(context, inputFile, content)
        );
    }

    @Test
    void testDetectFlatAttributeMisuse() {
        String content = "SetAttributes[subtract, Flat];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectFlatAttributeMisuse(context, inputFile, content)
        );
    }

    @Test
    void testDetectSequenceInUnexpectedContext() {
        String content = "result = Sequence[1, 2, 3];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectSequenceInUnexpectedContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingSequenceWrapper() {
        String content = "If[cond, {a, b}, {}]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMissingSequenceWrapper(context, inputFile, content)
        );
    }

    // ===== TEST COVERAGE TESTS (4 rules) =====

    @Test
    void testDetectLowTestCoverageWarning() {
        String content = "f[x_] := x + 1;\n".repeat(60);
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectLowTestCoverageWarning(context, inputFile, content)
        );
    }

    @Test
    void testDetectUntestedPublicFunction() {
        String content = "f::usage = \"Test function\";\nf[x_] := x + 1;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectUntestedPublicFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectUntestedBranch() {
        String content = "f[x_] := If[x > 0, result, other];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectUntestedBranch(context, inputFile, content)
        );
    }

    @Test
    void testDetectTestOnlyCodeInProduction() {
        String content = "If[$TestMode, debug, production];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectTestOnlyCodeInProduction(context, inputFile, content)
        );
    }

    // ===== PERFORMANCE ANALYSIS TESTS (9 rules) =====

    @Test
    void testDetectCompilableFunctionNotCompiled() {
        String content = "f[x_] := Sin[x] + Cos[x];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectCompilableFunctionNotCompiled(context, inputFile, content)
        );
    }

    @Test
    void testDetectCompilationTargetMissing() {
        String content = "Compile[{x}, x + 1]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectCompilationTargetMissing(context, inputFile, content)
        );
    }

    @Test
    void testDetectNonCompilableConstructInCompile() {
        String content = "Compile[{x}, Sort[x]]";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectNonCompilableConstructInCompile(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackedArrayUnpacked() {
        String content = "arr[[1]] = 5;";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectPackedArrayUnpacked(context, inputFile, content)
        );
    }

    @Test
    void testDetectInefficientPatternInPerformanceCriticalCode() {
        String content = "Do[Match[expr, pattern], {i, 1, 1000}];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectInefficientPatternInPerformanceCriticalCode(context, inputFile, content)
        );
    }

    @Test
    void testDetectNAppliedTooLate() {
        String content = "result = N[Integrate[x^2, {x, 0, 1}]];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectNAppliedTooLate(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingMemoizationOpportunityEnhanced() {
        String content = "f[x_] := f[x - 1] + f[x - 2];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectMissingMemoizationOpportunityEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectInefficientStringConcatenationEnhanced() {
        String content = "Do[str = str <> \"x\", {i, 1, 100}];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectInefficientStringConcatenationEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectListConcatenationInLoop() {
        String content = "Do[list = Join[list, {x}], {i, 1, 100}];";
        assertDoesNotThrow(() ->
            AdvancedAnalysisDetector.detectListConcatenationInLoop(context, inputFile, content)
        );
    }

    // ===== EDGE CASES AND COMPREHENSIVE TESTS =====

    @Test
    void testAllNullSafetyMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            AdvancedAnalysisDetector.detectNullDereference(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingNullCheck(context, inputFile, content);
            AdvancedAnalysisDetector.detectNullPassedToNonNullable(context, inputFile, content);
            AdvancedAnalysisDetector.detectInconsistentNullHandling(context, inputFile, content);
            AdvancedAnalysisDetector.detectNullReturnNotDocumented(context, inputFile, content);
            AdvancedAnalysisDetector.detectComparisonWithNull(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingCheckLeadsToNullPropagation(context, inputFile, content);
            AdvancedAnalysisDetector.detectCheckPatternDoesntHandleAllCases(context, inputFile, content);
            AdvancedAnalysisDetector.detectQuietSuppressingImportantMessages(context, inputFile, content);
            AdvancedAnalysisDetector.detectOffDisablingImportantWarnings(context, inputFile, content);
            AdvancedAnalysisDetector.detectCatchAllExceptionHandler(context, inputFile, content);
            AdvancedAnalysisDetector.detectEmptyExceptionHandler(context, inputFile, content);
            AdvancedAnalysisDetector.detectThrowWithoutCatch(context, inputFile, content);
            AdvancedAnalysisDetector.detectAbortInLibraryCode(context, inputFile, content);
            AdvancedAnalysisDetector.detectMessageWithoutDefinition(context, inputFile, content);
        });
    }

    @Test
    void testAllExpressionAnalysisMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            AdvancedAnalysisDetector.detectConditionAlwaysTrueConstantPropagation(context, inputFile, content);
            AdvancedAnalysisDetector.detectConditionAlwaysFalseConstantPropagation(context, inputFile, content);
            AdvancedAnalysisDetector.detectLoopBoundConstant(context, inputFile, content);
            AdvancedAnalysisDetector.detectRedundantComputation(context, inputFile, content);
            AdvancedAnalysisDetector.detectPureExpressionInLoop(context, inputFile, content);
            AdvancedAnalysisDetector.detectConstantExpression(context, inputFile, content);
            AdvancedAnalysisDetector.detectIdentityOperation(context, inputFile, content);
            AdvancedAnalysisDetector.detectComparisonOfIdenticalExpressions(context, inputFile, content);
            AdvancedAnalysisDetector.detectBooleanExpressionAlwaysTrue(context, inputFile, content);
            AdvancedAnalysisDetector.detectBooleanExpressionAlwaysFalse(context, inputFile, content);
            AdvancedAnalysisDetector.detectUnnecessaryBooleanConversion(context, inputFile, content);
            AdvancedAnalysisDetector.detectDoubleNegation(context, inputFile, content);
            AdvancedAnalysisDetector.detectComplexBooleanExpressionEnhanced(context, inputFile, content);
            AdvancedAnalysisDetector.detectDeMorgansLawOpportunity(context, inputFile, content);
        });
    }

    @Test
    void testAllMathematicaPatternMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            AdvancedAnalysisDetector.detectHoldAttributeMissing(context, inputFile, content);
            AdvancedAnalysisDetector.detectHoldFirstButUsesSecondArgumentFirst(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingUnevaluatedWrapper(context, inputFile, content);
            AdvancedAnalysisDetector.detectUnnecessaryHold(context, inputFile, content);
            AdvancedAnalysisDetector.detectReleaseHoldAfterHold(context, inputFile, content);
            AdvancedAnalysisDetector.detectEvaluateInHeldContext(context, inputFile, content);
            AdvancedAnalysisDetector.detectPatternWithSideEffect(context, inputFile, content);
            AdvancedAnalysisDetector.detectReplacementRuleOrderMatters(context, inputFile, content);
            AdvancedAnalysisDetector.detectReplaceAllVsReplaceConfusion(context, inputFile, content);
            AdvancedAnalysisDetector.detectRuleDoesntMatchDueToEvaluation(context, inputFile, content);
            AdvancedAnalysisDetector.detectPartSpecificationOutOfBounds(context, inputFile, content);
            AdvancedAnalysisDetector.detectSpanSpecificationInvalid(context, inputFile, content);
            AdvancedAnalysisDetector.detectAllSpecificationInefficient(context, inputFile, content);
            AdvancedAnalysisDetector.detectThreadingOverNonLists(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingAttributesDeclaration(context, inputFile, content);
            AdvancedAnalysisDetector.detectOneIdentityAttributeMisuse(context, inputFile, content);
            AdvancedAnalysisDetector.detectOrderlessAttributeOnNonCommutative(context, inputFile, content);
            AdvancedAnalysisDetector.detectFlatAttributeMisuse(context, inputFile, content);
            AdvancedAnalysisDetector.detectSequenceInUnexpectedContext(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingSequenceWrapper(context, inputFile, content);
        });
    }

    @Test
    void testAllPerformanceMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            AdvancedAnalysisDetector.detectCompilableFunctionNotCompiled(context, inputFile, content);
            AdvancedAnalysisDetector.detectCompilationTargetMissing(context, inputFile, content);
            AdvancedAnalysisDetector.detectNonCompilableConstructInCompile(context, inputFile, content);
            AdvancedAnalysisDetector.detectPackedArrayUnpacked(context, inputFile, content);
            AdvancedAnalysisDetector.detectInefficientPatternInPerformanceCriticalCode(context, inputFile, content);
            AdvancedAnalysisDetector.detectNAppliedTooLate(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingMemoizationOpportunityEnhanced(context, inputFile, content);
            AdvancedAnalysisDetector.detectInefficientStringConcatenationEnhanced(context, inputFile, content);
            AdvancedAnalysisDetector.detectListConcatenationInLoop(context, inputFile, content);
        });
    }

    @Test
    void testComplexCodeSampleWithMultipleIssues() {
        String content = "BeginPackage[\"Test`\"];\n"
                + "f[x_] := Hold[x];\n"
                + "If[True, result, other];\n"
                + "result = Reverse[Reverse[list]];\n"
                + "Quiet[riskyOperation];\n"
                + "Do[str = str <> \"x\", {i, 1, 100}];\n"
                + "EndPackage[];";

        assertDoesNotThrow(() -> {
            AdvancedAnalysisDetector.detectHoldAttributeMissing(context, inputFile, content);
            AdvancedAnalysisDetector.detectConditionAlwaysTrueConstantPropagation(context, inputFile, content);
            AdvancedAnalysisDetector.detectIdentityOperation(context, inputFile, content);
            AdvancedAnalysisDetector.detectQuietSuppressingImportantMessages(context, inputFile, content);
            AdvancedAnalysisDetector.detectInefficientStringConcatenationEnhanced(context, inputFile, content);
        });
    }
}
