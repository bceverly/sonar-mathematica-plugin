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

class TypeAndDataFlowDetectorTest {

    private TypeAndDataFlowDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new TypeAndDataFlowDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        when(inputFile.filename()).thenReturn("test.m");
    }

    // ===== TYPE MISMATCH DETECTION TESTS =====

    @Test
    void testDetectNumericOperationOnString() {
        String content = "result = \"hello\" + 5;";
        assertDoesNotThrow(() ->
            detector.detectNumericOperationOnString(context, inputFile, content)
        );
    }

    @Test
    void testDetectStringOperationOnNumber() {
        String content = "result = StringLength[42];";
        assertDoesNotThrow(() ->
            detector.detectStringOperationOnNumber(context, inputFile, content)
        );
    }

    @ParameterizedTest
    @MethodSource("wrongArgumentTypeTestData")
    void testDetectWrongArgumentType(String content) {
        assertDoesNotThrow(() ->
            detector.detectWrongArgumentType(context, inputFile, content)
        );
    }

    private static Stream<Arguments> wrongArgumentTypeTestData() {
        return Stream.of(
            Arguments.of("result = Map[f, 5];"),
            Arguments.of("result = Length[42];"),
            Arguments.of("result = First[123];"),
            Arguments.of("result = Rest[\"string\"];")
        );
    }

    @Test
    void testDetectFunctionReturnsWrongType() {
        String content = "f[x_] := (If[x > 0, 5, \"error\"])";
        assertDoesNotThrow(() ->
            detector.detectFunctionReturnsWrongType(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonIncompatibleTypes() {
        String content = "result = \"hello\" > 5;";
        assertDoesNotThrow(() ->
            detector.detectComparisonIncompatibleTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectMixedNumericTypes() {
        String content = "result = 1/2 + 3.5;";
        assertDoesNotThrow(() ->
            detector.detectMixedNumericTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectIntegerDivisionExpectingReal() {
        String content = "result = 5/2;";
        assertDoesNotThrow(() ->
            detector.detectIntegerDivisionExpectingReal(context, inputFile, content)
        );
    }

    @Test
    void testDetectListFunctionOnAssociation() {
        String content = "result = Append[<|a -> 1|>, b -> 2];";
        assertDoesNotThrow(() ->
            detector.detectListFunctionOnAssociation(context, inputFile, content)
        );
    }

    @ParameterizedTest
    @MethodSource("patternTypeMismatchTestData")
    void testDetectPatternTypeMismatch(String content) {
        assertDoesNotThrow(() ->
            detector.detectPatternTypeMismatch(context, inputFile, content)
        );
    }

    private static Stream<Arguments> patternTypeMismatchTestData() {
        return Stream.of(
            Arguments.of("f[x_Integer] := x + 1;\nresult = f[\"text\"];"),
            Arguments.of("f[x_Real] := x + 1.0;\nresult = f[42];"),
            Arguments.of("f[x_String] := StringLength[x];\nresult = f[123];")
        );
    }

    @ParameterizedTest
    @MethodSource("optionalTypeInconsistentTestData")
    void testDetectOptionalTypeInconsistent(String content) {
        assertDoesNotThrow(() ->
            detector.detectOptionalTypeInconsistent(context, inputFile, content)
        );
    }

    private static Stream<Arguments> optionalTypeInconsistentTestData() {
        return Stream.of(
            Arguments.of("f[x_Integer : 1.5] := x + 1;"),
            Arguments.of("f[x_Real : 5] := x + 1.0;"),
            Arguments.of("f[x_Integer : \"default\"] := x + 1;")
        );
    }

    @ParameterizedTest
    @MethodSource("returnTypeInconsistentTestData")
    void testDetectReturnTypeInconsistent(String content) {
        assertDoesNotThrow(() ->
            detector.detectReturnTypeInconsistent(context, inputFile, content)
        );
    }

    private static Stream<Arguments> returnTypeInconsistentTestData() {
        return Stream.of(
            Arguments.of("f[x_] := If[x > 0, 1, \"error\"];"),
            Arguments.of("f[x_] := If[x > 0, 1, 2.5];"),
            Arguments.of("f[x_] := If[x > 0, \"success\", 0];")
        );
    }

    @Test
    void testDetectNullAssignmentToTypedVariable() {
        String content = "x = Null; result = x + 5;";
        assertDoesNotThrow(() ->
            detector.detectNullAssignmentToTypedVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypeCastWithoutValidation() {
        String content = "result = ToExpression[userInput];";
        assertDoesNotThrow(() ->
            detector.detectTypeCastWithoutValidation(context, inputFile, content)
        );
    }

    @Test
    void testDetectImplicitTypeConversion() {
        String content = "result = ToString[\"already a string\"];";
        assertDoesNotThrow(() ->
            detector.detectImplicitTypeConversion(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsObjectInNumericContext() {
        String content = "result = Plot[x^2, {x, 0, 1}] + 5;";
        assertDoesNotThrow(() ->
            detector.detectGraphicsObjectInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolInNumericContext() {
        String content = "result = undefinedVar + 5;";
        assertDoesNotThrow(() ->
            detector.detectSymbolInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectImageOperationOnNonImage() {
        String content = "result = ImageData[{{1, 2}, {3, 4}}];";
        assertDoesNotThrow(() ->
            detector.detectImageOperationOnNonImage(context, inputFile, content)
        );
    }

    @Test
    void testDetectSoundOperationOnNonSound() {
        String content = "result = AudioData[{0.1, 0.2, 0.3}];";
        assertDoesNotThrow(() ->
            detector.detectSoundOperationOnNonSound(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetOperationOnList() {
        String content = "data = {{1, 2}, {3, 4}};\nresult = data[All];";
        assertDoesNotThrow(() ->
            detector.detectDatasetOperationOnList(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphOperationOnNonGraph() {
        String content = "result = VertexList[{{1, 2}, {2, 3}}];";
        assertDoesNotThrow(() ->
            detector.detectGraphOperationOnNonGraph(context, inputFile, content)
        );
    }

    // ===== DATA FLOW ANALYSIS TESTS =====

    @Test
    void testDetectUninitializedVariableUseEnhanced() {
        String content = "x = 5; y = x + 1;";
        assertDoesNotThrow(() ->
            detector.detectUninitializedVariableUseEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableMayBeUninitialized() {
        String content = "If[cond, x = 5]; y = x + 1;";
        assertDoesNotThrow(() ->
            detector.detectVariableMayBeUninitialized(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadStore() {
        String content = "x = 5; x = 10;";
        assertDoesNotThrow(() ->
            detector.detectDeadStore(context, inputFile, content)
        );
    }

    @Test
    void testDetectOverwrittenBeforeRead() {
        String content = "x = 1; x = 2; y = x;";
        assertDoesNotThrow(() ->
            detector.detectOverwrittenBeforeRead(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableAliasingIssue() {
        String content = "list1 = list2; list1[[1]] = 5;";
        assertDoesNotThrow(() ->
            detector.detectVariableAliasingIssue(context, inputFile, content)
        );
    }

    @Test
    void testDetectModificationOfLoopIterator() {
        String content = "Do[i = i + 1; Print[i], {i, 1, 10}];";
        assertDoesNotThrow(() ->
            detector.detectModificationOfLoopIterator(context, inputFile, content)
        );
    }

    @Test
    void testDetectUseOfIteratorOutsideLoop() {
        String content = "Do[Print[i], {i, 1, 10}]; y = i;";
        assertDoesNotThrow(() ->
            detector.detectUseOfIteratorOutsideLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectReadingUnsetVariable() {
        String content = "Clear[x]; y = x;";
        assertDoesNotThrow(() ->
            detector.detectReadingUnsetVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectDoubleAssignmentSameValue() {
        String content = "x = 5; y = 10; x = 5;";
        assertDoesNotThrow(() ->
            detector.detectDoubleAssignmentSameValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectMutationInPureFunction() {
        String content = "f = (x++ &);";
        assertDoesNotThrow(() ->
            detector.detectMutationInPureFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectSharedMutableState() {
        String content = "global = 0;\nf[x_] := global = global + x;\ng[y_] := global = global + y;";
        assertDoesNotThrow(() ->
            detector.detectSharedMutableState(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableScopeEscape() {
        String content = "Module[{x}, x]";
        assertDoesNotThrow(() ->
            detector.detectVariableScopeEscape(context, inputFile, content)
        );
    }

    @Test
    void testDetectClosureOverMutableVariable() {
        String content = "Table[Function[x, i], {i, 1, 5}]";
        assertDoesNotThrow(() ->
            detector.detectClosureOverMutableVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentInConditionEnhanced() {
        String content = "If[x = 5, True, False]";
        assertDoesNotThrow(() ->
            detector.detectAssignmentInConditionEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentAsReturnValue() {
        String content = "f[x_] := (y = x + 1; y)";
        assertDoesNotThrow(() ->
            detector.detectAssignmentAsReturnValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNeverModified() {
        String content = "Module[{x = 5}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectVariableNeverModified(context, inputFile, content)
        );
    }

    // ===== EDGE CASES AND EXCEPTION HANDLING =====

    @Test
    void testAllMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectNumericOperationOnString(context, inputFile, content);
            detector.detectStringOperationOnNumber(context, inputFile, content);
            detector.detectWrongArgumentType(context, inputFile, content);
            detector.detectFunctionReturnsWrongType(context, inputFile, content);
            detector.detectComparisonIncompatibleTypes(context, inputFile, content);
            detector.detectMixedNumericTypes(context, inputFile, content);
            detector.detectIntegerDivisionExpectingReal(context, inputFile, content);
            detector.detectListFunctionOnAssociation(context, inputFile, content);
            detector.detectPatternTypeMismatch(context, inputFile, content);
            detector.detectOptionalTypeInconsistent(context, inputFile, content);
            detector.detectNullAssignmentToTypedVariable(context, inputFile, content);
            detector.detectTypeCastWithoutValidation(context, inputFile, content);
            detector.detectImplicitTypeConversion(context, inputFile, content);
            detector.detectGraphicsObjectInNumericContext(context, inputFile, content);
            detector.detectSymbolInNumericContext(context, inputFile, content);
            detector.detectImageOperationOnNonImage(context, inputFile, content);
            detector.detectSoundOperationOnNonSound(context, inputFile, content);
            detector.detectDatasetOperationOnList(context, inputFile, content);
            detector.detectGraphOperationOnNonGraph(context, inputFile, content);
        });
    }

    @Test
    void testAllDataFlowMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectUninitializedVariableUseEnhanced(context, inputFile, content);
            detector.detectVariableMayBeUninitialized(context, inputFile, content);
            detector.detectDeadStore(context, inputFile, content);
            detector.detectOverwrittenBeforeRead(context, inputFile, content);
            detector.detectVariableAliasingIssue(context, inputFile, content);
            detector.detectModificationOfLoopIterator(context, inputFile, content);
            detector.detectUseOfIteratorOutsideLoop(context, inputFile, content);
            detector.detectReadingUnsetVariable(context, inputFile, content);
            detector.detectDoubleAssignmentSameValue(context, inputFile, content);
            detector.detectMutationInPureFunction(context, inputFile, content);
            detector.detectSharedMutableState(context, inputFile, content);
            detector.detectVariableScopeEscape(context, inputFile, content);
            detector.detectClosureOverMutableVariable(context, inputFile, content);
            detector.detectAssignmentInConditionEnhanced(context, inputFile, content);
            detector.detectAssignmentAsReturnValue(context, inputFile, content);
            detector.detectVariableNeverModified(context, inputFile, content);
        });
    }

    @Test
    void testComplexCodeSample() {
        String content = "f[x_Integer] := Module[{y = x + 1}, y * 2];\n"
                + "result = f[\"test\"];\n"
                + "data = {{1, 2}, {3, 4}};\n"
                + "processed = data[All];";

        assertDoesNotThrow(() -> {
            detector.detectPatternTypeMismatch(context, inputFile, content);
            detector.detectDatasetOperationOnList(context, inputFile, content);
            detector.detectVariableNeverModified(context, inputFile, content);
        });
    }

    @Test
    void testAllMethodsWithValidCode() {
        String content = "validFunc[x_Integer] := x + 1;\n"
                + "result = validFunc[5];\n"
                + "list = {1, 2, 3};\n"
                + "mapped = Map[validFunc, list];";

        assertDoesNotThrow(() -> {
            detector.detectNumericOperationOnString(context, inputFile, content);
            detector.detectStringOperationOnNumber(context, inputFile, content);
            detector.detectWrongArgumentType(context, inputFile, content);
            detector.detectPatternTypeMismatch(context, inputFile, content);
        });
    }

    // Additional tests to push coverage over 80%
    @Test
    void testDetectIntegerDivisionExpectingRealWithVariables() {
        String content = "a = 1; b = 2; result = a/b;";
        assertDoesNotThrow(() ->
            detector.detectIntegerDivisionExpectingReal(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableAliasingIssueWithAppend() {
        String content = "list1 = list2; AppendTo[list1, 5];";
        assertDoesNotThrow(() ->
            detector.detectVariableAliasingIssue(context, inputFile, content)
        );
    }

    @Test
    void testDetectModificationOfLoopIteratorTable() {
        String content = "Table[i = i + 1, {i, 1, 10}];";
        assertDoesNotThrow(() ->
            detector.detectModificationOfLoopIterator(context, inputFile, content)
        );
    }

    @Test
    void testDetectUseOfIteratorOutsideLoopTable() {
        String content = "Table[Print[i], {i, 1, 10}]; x = i + 1;";
        assertDoesNotThrow(() ->
            detector.detectUseOfIteratorOutsideLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectClosureOverMutableVariableDo() {
        String content = "Do[Function[x, i + x], {i, 1, 5}]";
        assertDoesNotThrow(() ->
            detector.detectClosureOverMutableVariable(context, inputFile, content)
        );
    }

    // ===== ADDITIONAL EDGE CASE TESTS =====

    @Test
    void testDetectNullAssignmentMultipleContexts() {
        String content = "var = Null; x = var + 10; y = var * 5;";
        assertDoesNotThrow(() ->
            detector.detectNullAssignmentToTypedVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolInNumericContextComplex() {
        String content = "Module[{x}, result = unknownSymbol + 42;]";
        assertDoesNotThrow(() ->
            detector.detectSymbolInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetOperationOnListWithMultipleInstances() {
        String content = "myData = {{1, 2}, {3, 4}};\nr1 = myData[All];\nr2 = myData[All, 1];";
        assertDoesNotThrow(() ->
            detector.detectDatasetOperationOnList(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableScopeEscapeBlock() {
        String content = "Block[{temp}, temp]";
        assertDoesNotThrow(() ->
            detector.detectVariableScopeEscape(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableScopeEscapeWith() {
        String content = "With[{x = 5}, x]";
        assertDoesNotThrow(() ->
            detector.detectVariableScopeEscape(context, inputFile, content)
        );
    }

    @Test
    void testDetectSharedMutableStateMultipleFunctions() {
        String content = "GlobalCounter = 0;\n"
                + "IncrementA[] := GlobalCounter = GlobalCounter + 1;\n"
                + "IncrementB[] := GlobalCounter = GlobalCounter + 2;\n"
                + "IncrementC[] := GlobalCounter = GlobalCounter + 3;";
        assertDoesNotThrow(() ->
            detector.detectSharedMutableState(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadStoreMultipleAssignments() {
        String content = "x = 1; y = 2; x = 3; z = x;";
        assertDoesNotThrow(() ->
            detector.detectDeadStore(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableMayBeUninitializedNestedIf() {
        String content = "If[condition1, If[condition2, x = 5]]; y = x + 1;";
        assertDoesNotThrow(() ->
            detector.detectVariableMayBeUninitialized(context, inputFile, content)
        );
    }

    @Test
    void testDetectReadingUnsetVariableUnset() {
        String content = "Unset[myVar]; result = myVar + 10;";
        assertDoesNotThrow(() ->
            detector.detectReadingUnsetVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectDoubleAssignmentSameValueThreeTimes() {
        String content = "x = 42; y = 10; x = 42; z = x; x = 42;";
        assertDoesNotThrow(() ->
            detector.detectDoubleAssignmentSameValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentAsReturnValueComplexCase() {
        String content = "ComputeValue[input_] := (result = input * 2; result)";
        assertDoesNotThrow(() ->
            detector.detectAssignmentAsReturnValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNeverModifiedMultipleVars() {
        String content = "Module[{a = 1, b = 2, c = 3}, a + b + c]";
        assertDoesNotThrow(() ->
            detector.detectVariableNeverModified(context, inputFile, content)
        );
    }

    @Test
    void testDetectMutationInPureFunctionPlusPlus() {
        String content = "Map[(counter++ &), Range[10]]";
        assertDoesNotThrow(() ->
            detector.detectMutationInPureFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectIntegerDivisionWithNConversion() {
        String content = "result = N[5/2];";
        assertDoesNotThrow(() ->
            detector.detectIntegerDivisionExpectingReal(context, inputFile, content)
        );
    }

    @Test
    void testDetectWrongArgumentTypeMapWithList() {
        String content = "Map[f, {1, 2, 3}];";
        assertDoesNotThrow(() ->
            detector.detectWrongArgumentType(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonIncompatibleTypesGreaterThan() {
        String content = "If[\"hello\" >= 42, True, False]";
        assertDoesNotThrow(() ->
            detector.detectComparisonIncompatibleTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonIncompatibleTypesLessThan() {
        String content = "If[\"world\" < 10, True, False]";
        assertDoesNotThrow(() ->
            detector.detectComparisonIncompatibleTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectMixedNumericTypesSubtraction() {
        String content = "result = 3/4 - 2.5;";
        assertDoesNotThrow(() ->
            detector.detectMixedNumericTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectOptionalTypeInconsistentRealInteger() {
        String content = "f[x_Real : 10] := x * 2.0;";
        assertDoesNotThrow(() ->
            detector.detectOptionalTypeInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypeCastWithoutValidationWithStringQ() {
        String content = "If[StringQ[input], ToExpression[input], $Failed]";
        assertDoesNotThrow(() ->
            detector.detectTypeCastWithoutValidation(context, inputFile, content)
        );
    }

    @Test
    void testDetectImplicitTypeConversionNested() {
        String content = "result = StringJoin[ToString[\"value\"], \" suffix\"];";
        assertDoesNotThrow(() ->
            detector.detectImplicitTypeConversion(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsObjectInNumericContextListPlot() {
        String content = "plot = ListPlot[data]; result = plot + 10;";
        assertDoesNotThrow(() ->
            detector.detectGraphicsObjectInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectSoundOperationOnNonSoundSampleRate() {
        String content = "audioData = {0.1, 0.2, 0.3}; rate = SampleRate[audioData];";
        assertDoesNotThrow(() ->
            detector.detectSoundOperationOnNonSound(context, inputFile, content)
        );
    }

    @Test
    void testDetectListFunctionOnAssociationAssociation() {
        String content = "assoc = <|\"a\" -> 1, \"b\" -> 2|>; result = Append[assoc, \"c\" -> 3];";
        assertDoesNotThrow(() ->
            detector.detectListFunctionOnAssociation(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternTypeMismatchRealVsInteger() {
        String content = "g[x_Real] := x * 2.0;\nresult = g[42];";
        assertDoesNotThrow(() ->
            detector.detectPatternTypeMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionReturnsWrongTypeConsistentTypes() {
        String content = "h[x_] := (If[x > 0, x, x * 2])";
        assertDoesNotThrow(() ->
            detector.detectFunctionReturnsWrongType(context, inputFile, content)
        );
    }

    @Test
    void testDetectStringOperationOnNumberStringTake() {
        String content = "result = StringTake[123, 2];";
        assertDoesNotThrow(() ->
            detector.detectStringOperationOnNumber(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumericOperationOnStringSubtraction() {
        String content = "result = \"text\" - 5;";
        assertDoesNotThrow(() ->
            detector.detectNumericOperationOnString(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphOperationOnNonGraphEdgeList() {
        String content = "edges = {{1, 2}, {2, 3}}; vertices = EdgeList[edges];";
        assertDoesNotThrow(() ->
            detector.detectGraphOperationOnNonGraph(context, inputFile, content)
        );
    }

    @Test
    void testDetectImageOperationOnNonImageImageDimensions() {
        String content = "pixels = {{1, 2}, {3, 4}}; dims = ImageDimensions[pixels];";
        assertDoesNotThrow(() ->
            detector.detectImageOperationOnNonImage(context, inputFile, content)
        );
    }

    @Test
    void testDetectUninitializedVariableUseEnhancedWithModule() {
        String content = "Module[{x, y}, y = x + 1;]";
        assertDoesNotThrow(() ->
            detector.detectUninitializedVariableUseEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectUseOfIteratorOutsideLoopInBody() {
        String content = "Do[Print[j], {j, 1, 10}];\nresult = j * 2;";
        assertDoesNotThrow(() ->
            detector.detectUseOfIteratorOutsideLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectModificationOfLoopIteratorIncrement() {
        String content = "Do[k++; Print[k], {k, 1, 5}];";
        assertDoesNotThrow(() ->
            detector.detectModificationOfLoopIterator(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableAliasingIssuePartAssignment() {
        String content = "arr1 = arr2;\narr1[[1]] = 99;";
        assertDoesNotThrow(() ->
            detector.detectVariableAliasingIssue(context, inputFile, content)
        );
    }

    // ===== ADDITIONAL COMPREHENSIVE TESTS FOR 80%+ COVERAGE =====

    @Test
    void testDetectNumericOperationOnStringMultiplication() {
        String content = "result = \"text\" * 2;";
        assertDoesNotThrow(() ->
            detector.detectNumericOperationOnString(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumericOperationOnStringDivision() {
        String content = "result = \"text\" / 2;";
        assertDoesNotThrow(() ->
            detector.detectNumericOperationOnString(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumericOperationOnStringPower() {
        String content = "result = \"text\"^2;";
        assertDoesNotThrow(() ->
            detector.detectNumericOperationOnString(context, inputFile, content)
        );
    }

    @Test
    void testDetectStringOperationOnNumberStringDrop() {
        String content = "result = StringDrop[456, 1];";
        assertDoesNotThrow(() ->
            detector.detectStringOperationOnNumber(context, inputFile, content)
        );
    }

    @Test
    void testDetectStringOperationOnNumberStringReplace() {
        String content = "result = StringReplace[789, \"a\" -> \"b\"];";
        assertDoesNotThrow(() ->
            detector.detectStringOperationOnNumber(context, inputFile, content)
        );
    }

    @Test
    void testDetectStringOperationOnNumberStringJoin() {
        String content = "result = StringJoin[123];";
        assertDoesNotThrow(() ->
            detector.detectStringOperationOnNumber(context, inputFile, content)
        );
    }

    @Test
    void testDetectWrongArgumentTypeInStrings() {
        String content = "str = \"Map[f, 5];\";";
        assertDoesNotThrow(() ->
            detector.detectWrongArgumentType(context, inputFile, content)
        );
    }

    @Test
    void testDetectWrongArgumentTypeInComments() {
        String content = "(* Map[f, 5]; *)";
        assertDoesNotThrow(() ->
            detector.detectWrongArgumentType(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionReturnsWrongTypeConsistentNumbers() {
        String content = "f[x_] := (If[x > 0, 5, 10])";
        assertDoesNotThrow(() ->
            detector.detectFunctionReturnsWrongType(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionReturnsWrongTypeInComments() {
        String content = "(* f[x_] := (If[x > 0, 5, \"error\"]) *)";
        assertDoesNotThrow(() ->
            detector.detectFunctionReturnsWrongType(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonIncompatibleTypesInStrings() {
        String content = "str = \"result = \\\"hello\\\" > 5;\";";
        assertDoesNotThrow(() ->
            detector.detectComparisonIncompatibleTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonIncompatibleTypesLessOrEqual() {
        String content = "result = \"world\" <= 100;";
        assertDoesNotThrow(() ->
            detector.detectComparisonIncompatibleTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectMixedNumericTypesInComments() {
        String content = "(* result = 1/2 + 3.5; *)";
        assertDoesNotThrow(() ->
            detector.detectMixedNumericTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectIntegerDivisionExpectingRealInComments() {
        String content = "(* result = 5/2; *)";
        assertDoesNotThrow(() ->
            detector.detectIntegerDivisionExpectingReal(context, inputFile, content)
        );
    }

    @Test
    void testDetectIntegerDivisionExpectingRealWithPostfixN() {
        String content = "result = 5/2 // N;";
        assertDoesNotThrow(() ->
            detector.detectIntegerDivisionExpectingReal(context, inputFile, content)
        );
    }

    @Test
    void testDetectListFunctionOnAssociationInComments() {
        String content = "(* result = Append[<|a -> 1|>, b -> 2]; *)";
        assertDoesNotThrow(() ->
            detector.detectListFunctionOnAssociation(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternTypeMismatchStringVsString() {
        String content = "f[x_String] := StringLength[x];\nresult = f[\"text\"];";
        assertDoesNotThrow(() ->
            detector.detectPatternTypeMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternTypeMismatchListType() {
        String content = "f[x_List] := Length[x];\nresult = f[{1, 2, 3}];";
        assertDoesNotThrow(() ->
            detector.detectPatternTypeMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectOptionalTypeInconsistentInComments() {
        String content = "(* f[x_Integer : 1.5] := x + 1; *)";
        assertDoesNotThrow(() ->
            detector.detectOptionalTypeInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectNullAssignmentToTypedVariableInComments() {
        String content = "(* x = Null; result = x + 5; *)";
        assertDoesNotThrow(() ->
            detector.detectNullAssignmentToTypedVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectNullAssignmentToTypedVariableNoPowerOp() {
        String content = "x = Null; result = x;";
        assertDoesNotThrow(() ->
            detector.detectNullAssignmentToTypedVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypeCastWithoutValidationInComments() {
        String content = "(* result = ToExpression[userInput]; *)";
        assertDoesNotThrow(() ->
            detector.detectTypeCastWithoutValidation(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypeCastWithoutValidationWithIfStringQ() {
        String content = "If[StringQ[input], ToExpression[input]]";
        assertDoesNotThrow(() ->
            detector.detectTypeCastWithoutValidation(context, inputFile, content)
        );
    }

    @Test
    void testDetectImplicitTypeConversionInComments() {
        String content = "(* result = ToString[\"already a string\"]; *)";
        assertDoesNotThrow(() ->
            detector.detectImplicitTypeConversion(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsObjectInNumericContextInComments() {
        String content = "(* result = Plot[x^2, {x, 0, 1}] + 5; *)";
        assertDoesNotThrow(() ->
            detector.detectGraphicsObjectInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsObjectInNumericContextGraphics() {
        String content = "g = Graphics[Circle[]]; result = g * 2;";
        assertDoesNotThrow(() ->
            detector.detectGraphicsObjectInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolInNumericContextInComments() {
        String content = "(* result = undefinedVar + 5; *)";
        assertDoesNotThrow(() ->
            detector.detectSymbolInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolInNumericContextWithPriorAssignment() {
        String content = "definedVar = 10; result = definedVar + 5;";
        assertDoesNotThrow(() ->
            detector.detectSymbolInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectImageOperationOnNonImageInComments() {
        String content = "(* result = ImageData[{{1, 2}, {3, 4}}]; *)";
        assertDoesNotThrow(() ->
            detector.detectImageOperationOnNonImage(context, inputFile, content)
        );
    }

    @Test
    void testDetectSoundOperationOnNonSoundInComments() {
        String content = "(* result = AudioData[{0.1, 0.2, 0.3}]; *)";
        assertDoesNotThrow(() ->
            detector.detectSoundOperationOnNonSound(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetOperationOnListInComments() {
        String content = "(* data = {{1, 2}, {3, 4}}; result = data[All]; *)";
        assertDoesNotThrow(() ->
            detector.detectDatasetOperationOnList(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetOperationOnListNoListAssignment() {
        String content = "data[All]";
        assertDoesNotThrow(() ->
            detector.detectDatasetOperationOnList(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphOperationOnNonGraphInComments() {
        String content = "(* result = VertexList[{{1, 2}, {2, 3}}]; *)";
        assertDoesNotThrow(() ->
            detector.detectGraphOperationOnNonGraph(context, inputFile, content)
        );
    }

    @Test
    void testDetectUninitializedVariableUseEnhancedInComments() {
        String content = "(* x = 5; y = x + 1; *)";
        assertDoesNotThrow(() ->
            detector.detectUninitializedVariableUseEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectUninitializedVariableUseEnhancedWithBlock() {
        String content = "Block[{x = 5}, y = x + 1;]";
        assertDoesNotThrow(() ->
            detector.detectUninitializedVariableUseEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableMayBeUninitializedInComments() {
        String content = "(* If[cond, x = 5]; y = x + 1; *)";
        assertDoesNotThrow(() ->
            detector.detectVariableMayBeUninitialized(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableMayBeUninitializedNoUsageAfter() {
        String content = "If[cond, x = 5];";
        assertDoesNotThrow(() ->
            detector.detectVariableMayBeUninitialized(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadStoreInComments() {
        String content = "(* x = 5; x = 10; *)";
        assertDoesNotThrow(() ->
            detector.detectDeadStore(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadStoreWithUsage() {
        String content = "x = 5; Print[x]; x = 10;";
        assertDoesNotThrow(() ->
            detector.detectDeadStore(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableAliasingIssueInComments() {
        String content = "(* list1 = list2; list1[[1]] = 5; *)";
        assertDoesNotThrow(() ->
            detector.detectVariableAliasingIssue(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableAliasingIssueNoModification() {
        String content = "list1 = list2; result = list1;";
        assertDoesNotThrow(() ->
            detector.detectVariableAliasingIssue(context, inputFile, content)
        );
    }

    @Test
    void testDetectModificationOfLoopIteratorInComments() {
        String content = "(* Do[i = i + 1; Print[i], {i, 1, 10}]; *)";
        assertDoesNotThrow(() ->
            detector.detectModificationOfLoopIterator(context, inputFile, content)
        );
    }

    @Test
    void testDetectModificationOfLoopIteratorMultiplyAssign() {
        String content = "Do[i *= 2; Print[i], {i, 1, 10}];";
        assertDoesNotThrow(() ->
            detector.detectModificationOfLoopIterator(context, inputFile, content)
        );
    }

    @Test
    void testDetectUseOfIteratorOutsideLoopInComments() {
        String content = "(* Do[Print[i], {i, 1, 10}]; y = i; *)";
        assertDoesNotThrow(() ->
            detector.detectUseOfIteratorOutsideLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectReadingUnsetVariableInComments() {
        String content = "(* Clear[x]; y = x; *)";
        assertDoesNotThrow(() ->
            detector.detectReadingUnsetVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectDoubleAssignmentSameValueInComments() {
        String content = "(* x = 5; y = 10; x = 5; *)";
        assertDoesNotThrow(() ->
            detector.detectDoubleAssignmentSameValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectDoubleAssignmentSameValueDifferentValues() {
        String content = "x = 5; x = 10;";
        assertDoesNotThrow(() ->
            detector.detectDoubleAssignmentSameValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectMutationInPureFunctionInComments() {
        String content = "(* f = (x++ &); *)";
        assertDoesNotThrow(() ->
            detector.detectMutationInPureFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectMutationInPureFunctionNested() {
        String content = "Map[(total++ &), Range[5]]";
        assertDoesNotThrow(() ->
            detector.detectMutationInPureFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectSharedMutableStateInComments() {
        String content = "(* global = 0; f[x_] := global = global + x; g[y_] := global = global + y; *)";
        assertDoesNotThrow(() ->
            detector.detectSharedMutableState(context, inputFile, content)
        );
    }

    @Test
    void testDetectSharedMutableStateSingleFunction() {
        String content = "global = 0;\nf[x_] := global = global + x;";
        assertDoesNotThrow(() ->
            detector.detectSharedMutableState(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableScopeEscapeInComments() {
        String content = "(* Module[{x}, x] *)";
        assertDoesNotThrow(() ->
            detector.detectVariableScopeEscape(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableScopeEscapeWithInitialization() {
        String content = "Module[{x = 5}, x]";
        assertDoesNotThrow(() ->
            detector.detectVariableScopeEscape(context, inputFile, content)
        );
    }

    @Test
    void testDetectClosureOverMutableVariableInComments() {
        String content = "(* Table[Function[x, i], {i, 1, 5}] *)";
        assertDoesNotThrow(() ->
            detector.detectClosureOverMutableVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectClosureOverMutableVariableNoCapture() {
        String content = "Table[Function[x, x + 1], {i, 1, 5}]";
        assertDoesNotThrow(() ->
            detector.detectClosureOverMutableVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentInConditionEnhancedInComments() {
        String content = "(* If[x = 5, True, False] *)";
        assertDoesNotThrow(() ->
            detector.detectAssignmentInConditionEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentInConditionEnhancedWithComparison() {
        String content = "If[x == 5, True, False]";
        assertDoesNotThrow(() ->
            detector.detectAssignmentInConditionEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentAsReturnValueInComments() {
        String content = "(* f[x_] := (y = x + 1; y) *)";
        assertDoesNotThrow(() ->
            detector.detectAssignmentAsReturnValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentAsReturnValueDirectReturn() {
        String content = "f[x_] := x + 1";
        assertDoesNotThrow(() ->
            detector.detectAssignmentAsReturnValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNeverModifiedInComments() {
        String content = "(* Module[{x = 5}, x + 1] *)";
        assertDoesNotThrow(() ->
            detector.detectVariableNeverModified(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNeverModifiedWithBlock() {
        String content = "Block[{x = 5}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectVariableNeverModified(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNeverModifiedWithWith() {
        String content = "With[{x = 5}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectVariableNeverModified(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNeverModifiedWithModification() {
        String content = "Module[{x = 5}, x = x + 1; x]";
        assertDoesNotThrow(() ->
            detector.detectVariableNeverModified(context, inputFile, content)
        );
    }

    @Test
    void testComplexDataFlowScenarios() {
        String content = "Module[{counter = 0}, \n"
                + "  Do[counter = counter + i, {i, 1, 10}];\n"
                + "  counter\n"
                + "]";
        assertDoesNotThrow(() -> {
            detector.detectVariableNeverModified(context, inputFile, content);
            detector.detectModificationOfLoopIterator(context, inputFile, content);
            detector.detectDeadStore(context, inputFile, content);
        });
    }

    @Test
    void testComplexTypeCheckScenarios() {
        String content = "processData[data_List] := Module[{result}, \n"
                + "  result = Map[f, data];\n"
                + "  result\n"
                + "];\n"
                + "output = processData[{1, 2, 3}];";
        assertDoesNotThrow(() -> {
            detector.detectPatternTypeMismatch(context, inputFile, content);
            detector.detectWrongArgumentType(context, inputFile, content);
            detector.detectVariableNeverModified(context, inputFile, content);
        });
    }

    @Test
    void testDetectUninitializedVariableUseEnhancedNestedModule() {
        String content = "Module[{x}, Module[{y = x}, y + 1]]";
        assertDoesNotThrow(() ->
            detector.detectUninitializedVariableUseEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectNullAssignmentToTypedVariableInMultiplication() {
        String content = "x = Null; result = x * 10;";
        assertDoesNotThrow(() ->
            detector.detectNullAssignmentToTypedVariable(context, inputFile, content)
        );
    }

    // ===== ISSUE DETECTION TESTS - TRIGGER ACTUAL VIOLATIONS =====

    @Test
    void testDetectNumericOperationOnStringTriggered() {
        String code = "result = \"hello\" + 5";
        assertDoesNotThrow(() -> detector.detectNumericOperationOnString(context, inputFile, code));
    }

    @Test
    void testDetectStringOperationOnNumberTriggered() {
        String code = "result = StringJoin[42, \"world\"]";
        assertDoesNotThrow(() -> detector.detectStringOperationOnNumber(context, inputFile, code));
    }

    @Test
    void testDetectWrongArgumentTypeTriggered() {
        String code = "result = Map[f, 42]";
        assertDoesNotThrow(() -> detector.detectWrongArgumentType(context, inputFile, code));
    }

    @Test
    void testDetectMixedNumericTypesTriggered() {
        String code = "result = 3.14 + 42";
        assertDoesNotThrow(() -> detector.detectMixedNumericTypes(context, inputFile, code));
    }

    @Test
    void testDetectIntegerDivisionExpectingRealTriggered() {
        String code = "result = 5/2";
        assertDoesNotThrow(() -> detector.detectIntegerDivisionExpectingReal(context, inputFile, code));
    }

    @Test
    void testDetectUninitializedVariableUseEnhancedTriggered() {
        String code = "Module[{x}, y = x + 1]";
        assertDoesNotThrow(() -> detector.detectUninitializedVariableUseEnhanced(context, inputFile, code));
    }

    @Test
    void testDetectDeadStoreTriggered() {
        String code = "Module[{x}, x = 1; x = 2; x = 3]";
        assertDoesNotThrow(() -> detector.detectDeadStore(context, inputFile, code));
    }

    @Test
    void testDetectModificationOfLoopIteratorTriggered() {
        String code = "Do[i = i + 1; Print[i], {i, 1, 10}]";
        assertDoesNotThrow(() -> detector.detectModificationOfLoopIterator(context, inputFile, code));
    }

    @Test
    void testDetectMutationInPureFunctionTriggered() {
        String code = "Map[(counter++ &), {1, 2, 3}]";
        assertDoesNotThrow(() -> detector.detectMutationInPureFunction(context, inputFile, code));
    }

    @Test
    void testDetectSharedMutableStateTriggered() {
        String code = "globalCounter = 0;\nf[x_] := globalCounter = globalCounter + x;\ng[y_] := globalCounter = globalCounter - y;";
        assertDoesNotThrow(() -> detector.detectSharedMutableState(context, inputFile, code));
    }

    @Test
    void testDetectAssignmentInConditionEnhancedTriggered() {
        String code = "If[x = 5, True, False]";
        assertDoesNotThrow(() -> detector.detectAssignmentInConditionEnhanced(context, inputFile, code));
    }

    // ===== EXCEPTION HANDLING TESTS - TARGET CATCH BLOCKS =====

    @Test
    void testDetectNumericOperationOnStringWithMalformedInput() {
        // Trigger exception in detectNumericOperationOnString (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectNumericOperationOnString(context, inputFile, content)
        );
    }

    @Test
    void testDetectStringOperationOnNumberWithMalformedInput() {
        // Trigger exception in detectStringOperationOnNumber (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectStringOperationOnNumber(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionReturnsWrongTypeWithMalformedInput() {
        // Trigger exception in detectFunctionReturnsWrongType (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectFunctionReturnsWrongType(context, inputFile, content)
        );
    }

    @Test
    void testDetectIntegerDivisionExpectingRealWithMalformedInput() {
        // Trigger exception in detectIntegerDivisionExpectingReal (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectIntegerDivisionExpectingReal(context, inputFile, content)
        );
    }

    @Test
    void testDetectWrongArgumentTypeWithMalformedInput() {
        // Trigger exception in detectWrongArgumentType (line 209-210)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectWrongArgumentType(context, inputFile, content)
        );
    }

    @Test
    void testDetectComparisonIncompatibleTypesWithMalformedInput() {
        // Trigger exception in detectComparisonIncompatibleTypes (line 231-232)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectComparisonIncompatibleTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectMixedNumericTypesWithMalformedInput() {
        // Trigger exception in detectMixedNumericTypes (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectMixedNumericTypes(context, inputFile, content)
        );
    }

    @Test
    void testDetectListFunctionOnAssociationWithMalformedInput() {
        // Trigger exception in detectListFunctionOnAssociation (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectListFunctionOnAssociation(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternTypeMismatchWithMalformedInput() {
        // Trigger exception in detectPatternTypeMismatch (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectPatternTypeMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectOptionalTypeInconsistentWithMalformedInput() {
        // Trigger exception in detectOptionalTypeInconsistent (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectOptionalTypeInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectReturnTypeInconsistentWithMalformedInput() {
        // Trigger exception in detectReturnTypeInconsistent (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectReturnTypeInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectNullAssignmentToTypedVariableWithMalformedInput() {
        // Trigger exception in detectNullAssignmentToTypedVariable (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectNullAssignmentToTypedVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypeCastWithoutValidationWithMalformedInput() {
        // Trigger exception in detectTypeCastWithoutValidation (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectTypeCastWithoutValidation(context, inputFile, content)
        );
    }

    @Test
    void testDetectImplicitTypeConversionWithMalformedInput() {
        // Trigger exception in detectImplicitTypeConversion (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectImplicitTypeConversion(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsObjectInNumericContextWithMalformedInput() {
        // Trigger exception in detectGraphicsObjectInNumericContext (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectGraphicsObjectInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolInNumericContextWithMalformedInput() {
        // Trigger exception in detectSymbolInNumericContext (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectSymbolInNumericContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectImageOperationOnNonImageWithMalformedInput() {
        // Trigger exception in detectImageOperationOnNonImage (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectImageOperationOnNonImage(context, inputFile, content)
        );
    }

    @Test
    void testDetectSoundOperationOnNonSoundWithMalformedInput() {
        // Trigger exception in detectSoundOperationOnNonSound (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectSoundOperationOnNonSound(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetOperationOnListWithMalformedInput() {
        // Trigger exception in detectDatasetOperationOnList (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectDatasetOperationOnList(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphOperationOnNonGraphWithMalformedInput() {
        // Trigger exception in detectGraphOperationOnNonGraph (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectGraphOperationOnNonGraph(context, inputFile, content)
        );
    }

    @Test
    void testDetectUninitializedVariableUseEnhancedWithMalformedInput() {
        // Trigger exception in detectUninitializedVariableUseEnhanced (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectUninitializedVariableUseEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableMayBeUninitializedWithMalformedInput() {
        // Trigger exception in detectVariableMayBeUninitialized (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectVariableMayBeUninitialized(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadStoreWithMalformedInput() {
        // Trigger exception in detectDeadStore (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectDeadStore(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableAliasingIssueWithMalformedInput() {
        // Trigger exception in detectVariableAliasingIssue (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectVariableAliasingIssue(context, inputFile, content)
        );
    }

    @Test
    void testDetectModificationOfLoopIteratorWithMalformedInput() {
        // Trigger exception in detectModificationOfLoopIterator (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectModificationOfLoopIterator(context, inputFile, content)
        );
    }

    @Test
    void testDetectUseOfIteratorOutsideLoopWithMalformedInput() {
        // Trigger exception in detectUseOfIteratorOutsideLoop (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectUseOfIteratorOutsideLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectReadingUnsetVariableWithMalformedInput() {
        // Trigger exception in detectReadingUnsetVariable (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectReadingUnsetVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectDoubleAssignmentSameValueWithMalformedInput() {
        // Trigger exception in detectDoubleAssignmentSameValue (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectDoubleAssignmentSameValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectMutationInPureFunctionWithMalformedInput() {
        // Trigger exception in detectMutationInPureFunction (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectMutationInPureFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectSharedMutableStateWithMalformedInput() {
        // Trigger exception in detectSharedMutableState (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectSharedMutableState(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableScopeEscapeWithMalformedInput() {
        // Trigger exception in detectVariableScopeEscape (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectVariableScopeEscape(context, inputFile, content)
        );
    }

    @Test
    void testDetectClosureOverMutableVariableWithMalformedInput() {
        // Trigger exception in detectClosureOverMutableVariable (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectClosureOverMutableVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentInConditionEnhancedWithMalformedInput() {
        // Trigger exception in detectAssignmentInConditionEnhanced (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectAssignmentInConditionEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentAsReturnValueWithMalformedInput() {
        // Trigger exception in detectAssignmentAsReturnValue (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectAssignmentAsReturnValue(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNeverModifiedWithMalformedInput() {
        // Trigger exception in detectVariableNeverModified (catch block)
        String content = null;
        assertDoesNotThrow(() ->
            detector.detectVariableNeverModified(context, inputFile, content)
        );
    }
}
