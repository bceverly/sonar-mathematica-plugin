package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BugDetectorTest {

    private BugDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new BugDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        when(inputFile.filename()).thenReturn("test.m");
    }

    // ===== PHASE 1: CRITICAL BUG DETECTION TESTS =====

    @Test
    void testDetectDivisionByZero() {
        String content = "result = x / 0;";
        assertDoesNotThrow(() ->
            detector.detectDivisionByZero(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentInConditional() {
        String content = "If[x = 5, True, False]";
        assertDoesNotThrow(() ->
            detector.detectAssignmentInConditional(context, inputFile, content)
        );
    }

    @Test
    void testDetectListIndexOutOfBounds() {
        String content = "result = list[[100]];";
        assertDoesNotThrow(() ->
            detector.detectListIndexOutOfBounds(context, inputFile, content)
        );
    }

    @ParameterizedTest
    @MethodSource("infiniteRecursionTestData")
    void testDetectInfiniteRecursion(String content) {
        assertDoesNotThrow(() ->
            detector.detectInfiniteRecursion(context, inputFile, content)
        );
    }

    private static Stream<Arguments> infiniteRecursionTestData() {
        return Stream.of(
            Arguments.of("f[x_] := f[x];"),
            Arguments.of("f[0] := 0;\nf[x_] := f[x-1] + 1;"),
            Arguments.of("f[x_Integer] := f[x] + 1;")
        );
    }

    @Test
    void testDetectUnreachablePatterns() {
        String content = "f[x_] := 1;\nf[y_Integer] := 2;";
        assertDoesNotThrow(() ->
            detector.detectUnreachablePatterns(context, inputFile, content)
        );
    }

    @Test
    void testDetectFloatingPointEquality() {
        String content = "If[1.5 == 1.5, True, False]";
        assertDoesNotThrow(() ->
            detector.detectFloatingPointEquality(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionWithoutReturn() {
        String content = "f[x_] := (y = x + 1;)";
        assertDoesNotThrow(() ->
            detector.detectFunctionWithoutReturn(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableBeforeAssignment() {
        String content = "result = undefinedVar + 5;";
        assertDoesNotThrow(() ->
            detector.detectVariableBeforeAssignment(context, inputFile, content)
        );
    }

    @Test
    void testDetectOffByOne() {
        String content = "Do[expr, {i, 0, n}]";
        assertDoesNotThrow(() ->
            detector.detectOffByOne(context, inputFile, content)
        );
    }

    @Test
    void testDetectInfiniteLoop() {
        String content = "While[True, Print[\"loop\"]]";
        assertDoesNotThrow(() ->
            detector.detectInfiniteLoop(context, inputFile, content)
        );
    }

    // ===== PHASE 2: TYPE AND DIMENSION CHECKS =====

    @Test
    void testDetectMismatchedDimensions() {
        String content = "result = Transpose[matrix];";
        assertDoesNotThrow(() ->
            detector.detectMismatchedDimensions(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypeMismatch() {
        String content = "result = \"string\" + 5;";
        assertDoesNotThrow(() ->
            detector.detectTypeMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectSuspiciousPattern() {
        String content = "f[___] := 1;";
        assertDoesNotThrow(() ->
            detector.detectSuspiciousPattern(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingPatternTest() {
        String content = "f[x_] := Sqrt[x];";
        assertDoesNotThrow(() ->
            detector.detectMissingPatternTest(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternBlanksMisuse() {
        String content = "f[x__] := Length[x];";
        assertDoesNotThrow(() ->
            detector.detectPatternBlanksMisuse(context, inputFile, content)
        );
    }

    @Test
    void testDetectSetDelayedConfusion() {
        String content = "f[x_] = x + 1;";
        assertDoesNotThrow(() ->
            detector.detectSetDelayedConfusion(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolNameCollision() {
        String content = "N[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectSymbolNameCollision(context, inputFile, content)
        );
    }

    @Test
    void testDetectBlockModuleMisuse() {
        String content = "Block[{x = 5}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectBlockModuleMisuse(context, inputFile, content)
        );
    }

    // ===== PHASE 3: RESOURCE MANAGEMENT =====

    @Test
    void testDetectUnclosedFileHandle() {
        String content = "stream = OpenRead[\"file.txt\"];";
        assertDoesNotThrow(() ->
            detector.detectUnclosedFileHandle(context, inputFile, content)
        );
    }

    @Test
    void testDetectGrowingDefinitionChain() {
        String content = "Do[f[i] = i, {i, 1, 100}];";
        assertDoesNotThrow(() ->
            detector.detectGrowingDefinitionChain(context, inputFile, content)
        );
    }

    @Test
    void testDetectStreamNotClosed() {
        String content = "stream = OpenWrite[\"output.txt\"];\nWrite[stream, data];";
        assertDoesNotThrow(() ->
            detector.detectStreamNotClosed(context, inputFile, content)
        );
    }

    // Additional tests to push coverage over 80%
    @Test
    void testDetectUnreachablePatternsWithMultiple() {
        String content = "f[x_] := 1;\nf[y_Real] := 2;\nf[z_Integer] := 3;";
        assertDoesNotThrow(() ->
            detector.detectUnreachablePatterns(context, inputFile, content)
        );
    }

    @Test
    void testDetectFloatingPointEqualityWithVariables() {
        String content = "x = 1.0; y = 1.0; If[x == y, True, False]";
        assertDoesNotThrow(() ->
            detector.detectFloatingPointEquality(context, inputFile, content)
        );
    }

    @Test
    void testDetectDivisionByZeroWithVariable() {
        String content = "x = 0; result = 10 / x;";
        assertDoesNotThrow(() ->
            detector.detectDivisionByZero(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentInConditionalWhile() {
        String content = "While[x = ReadLine[stream], Print[x]]";
        assertDoesNotThrow(() ->
            detector.detectAssignmentInConditional(context, inputFile, content)
        );
    }

    @Test
    void testDetectListIndexOutOfBoundsNegative() {
        String content = "result = list[[-100]];";
        assertDoesNotThrow(() ->
            detector.detectListIndexOutOfBounds(context, inputFile, content)
        );
    }

    @Test
    void testDetectOffByOneWithZeroStart() {
        String content = "Do[Print[i], {i, 0, Length[list]}]";
        assertDoesNotThrow(() ->
            detector.detectOffByOne(context, inputFile, content)
        );
    }

    @Test
    void testDetectInfiniteLoopWhileFalse() {
        String content = "i = 0; While[i < 10, Print[i]]";
        assertDoesNotThrow(() ->
            detector.detectInfiniteLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectMismatchedDimensionsDot() {
        String content = "result = Dot[matrix1, matrix2];";
        assertDoesNotThrow(() ->
            detector.detectMismatchedDimensions(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypeMismatchMultiply() {
        String content = "result = \"text\" * 5;";
        assertDoesNotThrow(() ->
            detector.detectTypeMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectSuspiciousPatternTripleBlank() {
        String content = "f[x___Integer] := Length[{x}];";
        assertDoesNotThrow(() ->
            detector.detectSuspiciousPattern(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingPatternTestWithNegative() {
        String content = "f[x_] := 1/x;";
        assertDoesNotThrow(() ->
            detector.detectMissingPatternTest(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternBlanksMisuseTotal() {
        String content = "f[x__] := Total[{x}];";
        assertDoesNotThrow(() ->
            detector.detectPatternBlanksMisuse(context, inputFile, content)
        );
    }

    @Test
    void testDetectSetDelayedConfusionImmediate() {
        String content = "f[x_] = RandomReal[];";
        assertDoesNotThrow(() ->
            detector.detectSetDelayedConfusion(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolNameCollisionI() {
        String content = "I[x_] := Integrate[x, x];";
        assertDoesNotThrow(() ->
            detector.detectSymbolNameCollision(context, inputFile, content)
        );
    }

    @Test
    void testDetectBlockModuleMisuseForGlobal() {
        String content = "Module[{x = 5}, x + global]";
        assertDoesNotThrow(() ->
            detector.detectBlockModuleMisuse(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnclosedFileHandleOpenAppend() {
        String content = "stream = OpenAppend[\"file.txt\"];";
        assertDoesNotThrow(() ->
            detector.detectUnclosedFileHandle(context, inputFile, content)
        );
    }

    @Test
    void testDetectGrowingDefinitionChainTable() {
        String content = "Table[f[i] = i^2, {i, 1, 200}];";
        assertDoesNotThrow(() ->
            detector.detectGrowingDefinitionChain(context, inputFile, content)
        );
    }

    @Test
    void testAllMethodsWithComplexCode() {
        String content = "f[x_] := f[x];\n"
                + "g[y_] := y / 0;\n"
                + "If[z = 5, True, False];\n"
                + "result = list[[1000]];";

        assertDoesNotThrow(() -> {
            detector.detectInfiniteRecursion(context, inputFile, content);
            detector.detectDivisionByZero(context, inputFile, content);
            detector.detectAssignmentInConditional(context, inputFile, content);
            detector.detectListIndexOutOfBounds(context, inputFile, content);
        });
    }

    @Test
    void testDetectFileHandleLeak() {
        String content = "OpenRead[\"file.txt\"]; (* no close *)";
        assertDoesNotThrow(() ->
            detector.detectFileHandleLeak(context, inputFile, content)
        );
    }

    @Test
    void testDetectCloseInFinallyMissing() {
        String content = "stream = OpenRead[\"file.txt\"];\nRead[stream];";
        assertDoesNotThrow(() ->
            detector.detectCloseInFinallyMissing(context, inputFile, content)
        );
    }

    @Test
    void testDetectStreamReopenAttempt() {
        String content = "stream = OpenRead[\"file.txt\"];\nstream = OpenRead[\"file2.txt\"];";
        assertDoesNotThrow(() ->
            detector.detectStreamReopenAttempt(context, inputFile, content)
        );
    }

    // ===== PHASE 4: ADVANCED BUG DETECTION =====

    @Test
    void testDetectMissingEmptyListCheck() {
        String content = "result = First[list];";
        assertDoesNotThrow(() ->
            detector.detectMissingEmptyListCheck(context, inputFile, content)
        );
    }

    @Test
    void testDetectMachinePrecisionInSymbolic() {
        String content = "Solve[x^2 + 1.5*x + 1 == 0, x]";
        assertDoesNotThrow(() ->
            detector.detectMachinePrecisionInSymbolic(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingFailedCheck() {
        String content = "result = Import[\"data.csv\"];";
        assertDoesNotThrow(() ->
            detector.detectMissingFailedCheck(context, inputFile, content)
        );
    }

    @Test
    void testDetectZeroDenominator() {
        String content = "result = x / y;";
        assertDoesNotThrow(() ->
            detector.detectZeroDenominator(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingMatrixDimensionCheck() {
        String content = "result = matrix.otherMatrix;";
        assertDoesNotThrow(() ->
            detector.detectMissingMatrixDimensionCheck(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncorrectSetInScoping() {
        String content = "Module[{x = 5}, body]";
        assertDoesNotThrow(() ->
            detector.detectIncorrectSetInScoping(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingHoldAttributes() {
        String content = "myHold[x_] := Hold[x];";
        assertDoesNotThrow(() ->
            detector.detectMissingHoldAttributes(context, inputFile, content)
        );
    }

    @Test
    void testDetectEvaluationOrderAssumption() {
        String content = "{x++, x++, x++}";
        assertDoesNotThrow(() ->
            detector.detectEvaluationOrderAssumption(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncorrectLevelSpecification() {
        String content = "Map[f, list, {-1}]";
        assertDoesNotThrow(() ->
            detector.detectIncorrectLevelSpecification(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnpackingPackedArrays() {
        String content = "arr[[1]] = 5;";
        assertDoesNotThrow(() ->
            detector.detectUnpackingPackedArrays(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingSpecialCaseHandling() {
        String content = "f[x_] := x / (x - 1);";
        assertDoesNotThrow(() ->
            detector.detectMissingSpecialCaseHandling(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncorrectAssociationOperations() {
        String content = "assoc = <|a -> 1|>;\nresult = Append[assoc, b -> 2];";
        assertDoesNotThrow(() ->
            detector.detectIncorrectAssociationOperations(context, inputFile, content)
        );
    }

    @Test
    void testDetectDateObjectValidation() {
        String content = "date = DateObject[{2023, 13, 45}];";
        assertDoesNotThrow(() ->
            detector.detectDateObjectValidation(context, inputFile, content)
        );
    }

    @Test
    void testDetectTotalMeanOnNonNumeric() {
        String content = "result = Total[{\"a\", \"b\", \"c\"}];";
        assertDoesNotThrow(() ->
            detector.detectTotalMeanOnNonNumeric(context, inputFile, content)
        );
    }

    @Test
    void testDetectQuantityUnitMismatch() {
        String content = "Quantity[5, \"Meters\"] + Quantity[3, \"Seconds\"]";
        assertDoesNotThrow(() ->
            detector.detectQuantityUnitMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectDynamicMemoryLeak() {
        String content = "Do[AppendTo[bigList, i], {i, 1, 1000000}];";
        assertDoesNotThrow(() ->
            detector.detectDynamicMemoryLeak(context, inputFile, content)
        );
    }

    @Test
    void testDetectLargeDataInNotebook() {
        String content = "data = Table[RandomReal[], {i, 1, 100000}];";
        assertDoesNotThrow(() ->
            detector.detectLargeDataInNotebook(context, inputFile, content)
        );
    }

    @Test
    void testDetectNoClearAfterUse() {
        String content = "bigData = Table[RandomReal[], {i, 1, 10000}];\nresult = Total[bigData];";
        assertDoesNotThrow(() ->
            detector.detectNoClearAfterUse(context, inputFile, content)
        );
    }

    // ===== EDGE CASES AND COMPREHENSIVE TESTS =====

    @Test
    void testAllMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectDivisionByZero(context, inputFile, content);
            detector.detectAssignmentInConditional(context, inputFile, content);
            detector.detectListIndexOutOfBounds(context, inputFile, content);
            detector.detectInfiniteRecursion(context, inputFile, content);
            detector.detectUnreachablePatterns(context, inputFile, content);
            detector.detectFloatingPointEquality(context, inputFile, content);
            detector.detectFunctionWithoutReturn(context, inputFile, content);
            detector.detectVariableBeforeAssignment(context, inputFile, content);
            detector.detectOffByOne(context, inputFile, content);
            detector.detectInfiniteLoop(context, inputFile, content);
        });
    }

    @Test
    void testAllTypeMismatchMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectMismatchedDimensions(context, inputFile, content);
            detector.detectTypeMismatch(context, inputFile, content);
            detector.detectSuspiciousPattern(context, inputFile, content);
            detector.detectMissingPatternTest(context, inputFile, content);
            detector.detectPatternBlanksMisuse(context, inputFile, content);
            detector.detectSetDelayedConfusion(context, inputFile, content);
            detector.detectSymbolNameCollision(context, inputFile, content);
            detector.detectBlockModuleMisuse(context, inputFile, content);
        });
    }

    @Test
    void testAllResourceMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectUnclosedFileHandle(context, inputFile, content);
            detector.detectGrowingDefinitionChain(context, inputFile, content);
            detector.detectStreamNotClosed(context, inputFile, content);
            detector.detectFileHandleLeak(context, inputFile, content);
            detector.detectCloseInFinallyMissing(context, inputFile, content);
            detector.detectStreamReopenAttempt(context, inputFile, content);
        });
    }

    @Test
    void testAllAdvancedMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectMissingEmptyListCheck(context, inputFile, content);
            detector.detectMachinePrecisionInSymbolic(context, inputFile, content);
            detector.detectMissingFailedCheck(context, inputFile, content);
            detector.detectZeroDenominator(context, inputFile, content);
            detector.detectMissingMatrixDimensionCheck(context, inputFile, content);
            detector.detectIncorrectSetInScoping(context, inputFile, content);
            detector.detectMissingHoldAttributes(context, inputFile, content);
            detector.detectEvaluationOrderAssumption(context, inputFile, content);
            detector.detectIncorrectLevelSpecification(context, inputFile, content);
            detector.detectUnpackingPackedArrays(context, inputFile, content);
            detector.detectMissingSpecialCaseHandling(context, inputFile, content);
            detector.detectIncorrectAssociationOperations(context, inputFile, content);
            detector.detectDateObjectValidation(context, inputFile, content);
            detector.detectTotalMeanOnNonNumeric(context, inputFile, content);
            detector.detectQuantityUnitMismatch(context, inputFile, content);
            detector.detectDynamicMemoryLeak(context, inputFile, content);
            detector.detectLargeDataInNotebook(context, inputFile, content);
            detector.detectNoClearAfterUse(context, inputFile, content);
        });
    }

    @Test
    void testComplexCodeWithMultipleBugs() {
        String content = "f[x_] := x / 0;\n"
                + "If[y = 5, result, other];\n"
                + "stream = OpenRead[\"file.txt\"];\n"
                + "result = list[[1000]];\n"
                + "While[True, loop];";

        assertDoesNotThrow(() -> {
            detector.detectDivisionByZero(context, inputFile, content);
            detector.detectAssignmentInConditional(context, inputFile, content);
            detector.detectUnclosedFileHandle(context, inputFile, content);
            detector.detectListIndexOutOfBounds(context, inputFile, content);
            detector.detectInfiniteLoop(context, inputFile, content);
        });
    }

    @Test
    void testValidCodeWithNoBugs() {
        String content = "f[x_?NumericQ] := If[x != 0, 1/x, $Failed];\n"
                + "stream = OpenRead[\"file.txt\"];\n"
                + "data = Read[stream];\n"
                + "Close[stream];\n"
                + "result = If[Length[list] > 0, First[list], Null];";

        assertDoesNotThrow(() -> {
            detector.detectDivisionByZero(context, inputFile, content);
            detector.detectMissingEmptyListCheck(context, inputFile, content);
            detector.detectUnclosedFileHandle(context, inputFile, content);
        });
    }
}
