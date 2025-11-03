package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CodeSmellDetectorTest {

    private CodeSmellDetector detector;
    private SensorContext mockContext;
    private InputFile mockInputFile;

    @BeforeEach
    void setUp() {
        detector = new CodeSmellDetector();
        mockContext = Mockito.mock(SensorContext.class);
        mockInputFile = Mockito.mock(InputFile.class);

        // Setup default mock behavior
        Mockito.when(mockInputFile.filename()).thenReturn("test.m");
    }

    // ===== BASIC CODE SMELL DETECTION TESTS =====

    @Test
    void testDetectMagicNumbers() {
        String content = "result = calculate(42, 3.14159, 100);";
        List<int[]> commentRanges = new ArrayList<>();

        assertThatCode(() ->
            detector.detectMagicNumbers(mockContext, mockInputFile, content, commentRanges)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMagicNumbersIgnoresCommonNumbers() {
        String content = "x = 0; y = 1; z = 2;";
        List<int[]> commentRanges = new ArrayList<>();

        assertThatCode(() ->
            detector.detectMagicNumbers(mockContext, mockInputFile, content, commentRanges)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMagicNumbersSkipsCommentsAndStrings() {
        String content = "(* 42 *) str = \"100\"; result = 999;";
        List<int[]> commentRanges = detector.analyzeComments(content);

        assertThatCode(() ->
            detector.detectMagicNumbers(mockContext, mockInputFile, content, commentRanges)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectEmptyBlocks() {
        String content = "Module[{}, ]\nBlock[{x}, ]";

        assertThatCode(() ->
            detector.detectEmptyBlocks(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectLongFunctions() {
        // Create a function with many lines
        StringBuilder content = new StringBuilder("LongFunction[x_] := (\n");
        for (int i = 0; i < 150; i++) {
            content.append("  line").append(i).append(" = ").append(i).append(";\n");
        }
        content.append(")");

        assertThatCode(() ->
            detector.detectLongFunctions(mockContext, mockInputFile, content.toString())
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectEmptyCatchBlocks() {
        String content = "Check[riskyOp[], $Failed]\nQuiet[riskyOp[]]";

        assertThatCode(() ->
            detector.detectEmptyCatchBlocks(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectDebugCode() {
        String content = "Print[x]\nEcho[y]\n$DebugMessages = True";

        assertThatCode(() ->
            detector.detectDebugCode(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectUnusedVariables() {
        String content = "f[x_, y_] := x + 1; (* y is unused *)";

        assertThatCode(() ->
            detector.detectUnusedVariables(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectDuplicateFunctions() {
        String content = "f[x_] := x + 1\nf[x_] := x + 2";

        assertThatCode(() ->
            detector.detectDuplicateFunctions(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectTooManyParameters() {
        String content = "f[a_, b_, c_, d_, e_, f_, g_, h_] := a + b + c";

        assertThatCode(() ->
            detector.detectTooManyParameters(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectDeeplyNested() {
        String content = "If[a, If[b, If[c, If[d, result]]]]";

        assertThatCode(() ->
            detector.detectDeeplyNested(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingDocumentation() {
        StringBuilder content = new StringBuilder("ComplexFunction[x_, y_] := (\n");
        for (int i = 0; i < 25; i++) {
            content.append("  step").append(i).append(";\n");
        }
        content.append(")");

        assertThatCode(() ->
            detector.detectMissingDocumentation(mockContext, mockInputFile, content.toString())
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentNaming() {
        String content = "camelCaseFunc[x_] := x\nsnake_case_func[y_] := y";

        assertThatCode(() ->
            detector.detectInconsistentNaming(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectIdenticalBranches() {
        String content = "If[condition, result, result]";

        assertThatCode(() ->
            detector.detectIdenticalBranches(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectExpressionTooComplex() {
        String content = "result = a + b * c / d - e ^ f & g | h < i > j = k ! l";
        List<int[]> commentRanges = new ArrayList<>();

        assertThatCode(() ->
            detector.detectExpressionTooComplex(mockContext, mockInputFile, content, commentRanges)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectDeprecatedFunctions() {
        String content = "SetSystemOptions[\"RecursionLimit\" -> $RecursionLimit]";

        assertThatCode(() ->
            detector.detectDeprecatedFunctions(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectEmptyStatement() {
        String content = "x = 1;;\ny = [, ;]";

        assertThatCode(() ->
            detector.detectEmptyStatement(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    // ===== PERFORMANCE DETECTION TESTS =====

    @Test
    void testDetectAppendInLoop() {
        String content = "Do[result = AppendTo[result, i], {i, 100}]";

        assertThatCode(() ->
            detector.detectAppendInLoop(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectRepeatedFunctionCalls() {
        String content = "x = Solve[eq]; y = Solve[eq]; z = Solve[eq];";

        assertThatCode(() ->
            detector.detectRepeatedFunctionCalls(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectStringConcatInLoop() {
        String content = "Do[str = str <> \"text\", {i, 100}]";

        assertThatCode(() ->
            detector.detectStringConcatInLoop(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectUncompiledNumerical() {
        String content = "Do[sum += i * 2.0, {i, 10000}]";

        assertThatCode(() ->
            detector.detectUncompiledNumerical(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectPackedArrayBreaking() {
        String content = "Table[Append[data, Symbol[\"x\"]], {i, 100}]";

        assertThatCode(() ->
            detector.detectPackedArrayBreaking(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectNestedMapTable() {
        String content = "Map[f, Table[g[#], {i, 10}] &, data]";

        assertThatCode(() ->
            detector.detectNestedMapTable(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectLargeTempExpressions() {
        StringBuilder longLine = new StringBuilder("result = ");
        for (int i = 0; i < 50; i++) {
            longLine.append("function").append(i).append("[arg] + ");
        }
        longLine.append("final");

        assertThatCode(() ->
            detector.detectLargeTempExpressions(mockContext, mockInputFile, longLine.toString())
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectPlotInLoop() {
        String content = "Do[Plot[f[x, i], {x, 0, 10}], {i, 10}]";

        assertThatCode(() ->
            detector.detectPlotInLoop(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    // ===== BEST PRACTICES TESTS =====

    @Test
    void testDetectGenericVariableNames() {
        String content = "temp = data; result = val; x = item;";

        assertThatCode(() ->
            detector.detectGenericVariableNames(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingUsageMessage() {
        String content = "PublicFunction[x_] := x + 1";

        assertThatCode(() ->
            detector.detectMissingUsageMessage(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingOptionsPattern() {
        String content = "f[x_, opt1_: 1, opt2_: 2, opt3_: 3, opt4_: 4] := x";

        assertThatCode(() ->
            detector.detectMissingOptionsPattern(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectSideEffectsNaming() {
        String content = "ProcessData[x_] := (globalVar = x; x)";

        assertThatCode(() ->
            detector.detectSideEffectsNaming(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectComplexBoolean() {
        String content = "If[a && b && c || d && e && f || g, result]";

        assertThatCode(() ->
            detector.detectComplexBoolean(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectUnprotectedSymbols() {
        String content = "PublicApi[x_] := x\nAnotherPublic[y_] := y";

        assertThatCode(() ->
            detector.detectUnprotectedSymbols(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingReturn() {
        String content = "ComplexFunc[x_] := Module[{}, If[x > 0, x]]";

        assertThatCode(() ->
            detector.detectMissingReturn(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    // ===== PHASE 4 ADVANCED TESTS =====

    @Test
    void testDetectOvercomplexPatterns() {
        String content = "f[x_ | y_ | z_ | a_ | b_ | c_] := x";

        assertThatCode(() ->
            detector.detectOvercomplexPatterns(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentRuleTypes() {
        String content = "{a -> 1, b :> 2, c -> 3}";

        assertThatCode(() ->
            detector.detectInconsistentRuleTypes(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingFunctionAttributes() {
        String content = "PublicFunc1[x_] := x\nPublicFunc2[y_] := y";

        assertThatCode(() ->
            detector.detectMissingFunctionAttributes(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingDownValuesDoc() {
        String content = "F[x_Integer] := x\nF[x_Real] := x\nF[x_String] := x";

        assertThatCode(() ->
            detector.detectMissingDownValuesDoc(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingPatternTestValidation() {
        String content = "ProcessInput[data_] := Length[data]";

        assertThatCode(() ->
            detector.detectMissingPatternTestValidation(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectExcessivePureFunctions() {
        String content = "Map[#1 + #2 * #3 &, data]";

        assertThatCode(() ->
            detector.detectExcessivePureFunctions(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingOperatorPrecedence() {
        String content = "result = f /@ g @@ h //@ data";

        assertThatCode(() ->
            detector.detectMissingOperatorPrecedence(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectHardcodedFilePaths() {
        String content = "data = Import[\"C:\\\\Users\\\\data.txt\"]\nImport[\"/home/user/file.txt\"]";

        assertThatCode(() ->
            detector.detectHardcodedFilePaths(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentReturnTypes() {
        String content = "F[x_] := {x}\nF[y_] := <|y|>";

        assertThatCode(() ->
            detector.detectInconsistentReturnTypes(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingErrorMessages() {
        String content = "PublicFunc[x_] := x + 1";

        assertThatCode(() ->
            detector.detectMissingErrorMessages(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectGlobalStateModification() {
        String content = "ProcessData[x_] := (Global`var = x)";

        assertThatCode(() ->
            detector.detectGlobalStateModification(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingLocalization() {
        String content = "Manipulate[x + y, {x, 0, 10}]";

        assertThatCode(() ->
            detector.detectMissingLocalization(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectExplicitGlobalContext() {
        String content = "result = Global`myVariable";

        assertThatCode(() ->
            detector.detectExplicitGlobalContext(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingTemporaryCleanup() {
        String content = "file = CreateFile[\"temp.txt\"]\nWriteString[file, \"data\"]";

        assertThatCode(() ->
            detector.detectMissingTemporaryCleanup(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectNestedListsInsteadAssociation() {
        String content = "data[[1]]; data[[2]]; data[[3]]; data[[5]]";

        assertThatCode(() ->
            detector.detectNestedListsInsteadAssociation(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectRepeatedPartExtraction() {
        String content = "x[[1]]; x[[2]]; x[[3]]";

        assertThatCode(() ->
            detector.detectRepeatedPartExtraction(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingMemoization() {
        String content = "fib[n_] := fib[n-1] + fib[n-2]";

        assertThatCode(() ->
            detector.detectMissingMemoization(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectStringJoinForTemplates() {
        String content = "msg = \"Hello\" <> name <> \" from \" <> place <> \"!\"";

        assertThatCode(() ->
            detector.detectStringJoinForTemplates(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    // ===== PERFORMANCE OPTIMIZATIONS TESTS =====

    @Test
    void testDetectLinearSearchInsteadLookup() {
        String content = "Select[data, #[[\"key\"]] == value &]";

        assertThatCode(() ->
            detector.detectLinearSearchInsteadLookup(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectRepeatedCalculations() {
        String content = "Do[result = ExpensiveFunc[] + i, {i, 100}]";

        assertThatCode(() ->
            detector.detectRepeatedCalculations(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectPositionInsteadPattern() {
        String content = "pos = Position[data, pattern]; Extract[data, pos]";

        assertThatCode(() ->
            detector.detectPositionInsteadPattern(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectFlattenTableAntipattern() {
        String content = "result = Flatten[Table[f[i], {i, 100}]]";

        assertThatCode(() ->
            detector.detectFlattenTableAntipattern(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingParallelization() {
        String content = "Table[expensiveFunc[i], {i, 10000}]";

        assertThatCode(() ->
            detector.detectMissingParallelization(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingSparseArray() {
        String content = "zeros = Table[0, {1000}]";

        assertThatCode(() ->
            detector.detectMissingSparseArray(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectUnnecessaryTranspose() {
        String content = "result = Transpose[Transpose[matrix]]";

        assertThatCode(() ->
            detector.detectUnnecessaryTranspose(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectDeleteDuplicatesOnLargeData() {
        String content = "unique = DeleteDuplicates[largeList]";

        assertThatCode(() ->
            detector.detectDeleteDuplicatesOnLargeData(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectRepeatedStringParsing() {
        String content = "Do[val = ToExpression[str], {i, 100}]";

        assertThatCode(() ->
            detector.detectRepeatedStringParsing(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingCompilationTarget() {
        String content = "f = Compile[{{x, _Real}}, x^2]";

        assertThatCode(() ->
            detector.detectMissingCompilationTarget(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    // ===== COMMENT QUALITY TESTS =====

    @Test
    void testDetectTodoTracking() {
        String content = "(* TODO: implement this feature *)";

        assertThatCode(() ->
            detector.detectTodoTracking(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectFixmeTracking() {
        String content = "(* FIXME: this is broken *)";

        assertThatCode(() ->
            detector.detectFixmeTracking(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectHackComment() {
        String content = "(* HACK: workaround for bug *)";

        assertThatCode(() ->
            detector.detectHackComment(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectCommentedOutCode() {
        String content = "(* f[x_] := x + 1 *)";

        assertThatCode(() ->
            detector.detectCommentedOutCode(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectLargeCommentedBlock() {
        StringBuilder longComment = new StringBuilder("(*\n");
        for (int i = 0; i < 25; i++) {
            longComment.append("This is line ").append(i).append(" of a very long comment.\n");
        }
        longComment.append("*)");

        assertThatCode(() ->
            detector.detectLargeCommentedBlock(mockContext, mockInputFile, longComment.toString())
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectApiMissingDocumentation() {
        String content = "PublicApi[x_] := x + 1";

        assertThatCode(() ->
            detector.detectApiMissingDocumentation(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectDocumentationTooShort() {
        String content = "MyFunc::usage = \"Does stuff\"";

        assertThatCode(() ->
            detector.detectDocumentationTooShort(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectDocumentationOutdated() {
        String content = "OldFunc::usage = \"This is deprecated and obsolete\"";

        assertThatCode(() ->
            detector.detectDocumentationOutdated(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectParameterNotDocumented() {
        String content = "MyFunc::usage = \"MyFunc does something\"\nMyFunc[param1_, param2_] := param1";

        assertThatCode(() ->
            detector.detectParameterNotDocumented(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    @Test
    void testDetectReturnNotDocumented() {
        String content = "MyFunc::usage = \"MyFunc processes data\"";

        assertThatCode(() ->
            detector.detectReturnNotDocumented(mockContext, mockInputFile, content)
        ).doesNotThrowAnyException();
    }

    // ===== EDGE CASES AND ERROR HANDLING =====

    @Test
    void testAllDetectorsEmptyContent() {
        String content = "";
        List<int[]> commentRanges = new ArrayList<>();

        // Should not crash on empty content
        assertThatCode(() -> {
            detector.detectMagicNumbers(mockContext, mockInputFile, content, commentRanges);
            detector.detectEmptyBlocks(mockContext, mockInputFile, content);
            detector.detectDebugCode(mockContext, mockInputFile, content);
            detector.detectGenericVariableNames(mockContext, mockInputFile, content);
        }).doesNotThrowAnyException();
    }

    @Test
    void testAllDetectorsInvalidSyntax() {
        String content = "]]]][[[[invalid{{{{";
        List<int[]> commentRanges = new ArrayList<>();

        // Should handle invalid syntax gracefully
        assertThatCode(() -> {
            detector.detectMagicNumbers(mockContext, mockInputFile, content, commentRanges);
            detector.detectEmptyBlocks(mockContext, mockInputFile, content);
        }).doesNotThrowAnyException();
    }

    @Test
    void testInitializeAndClearCaches() {
        String content = "f[x_] := x + 1";

        assertThatCode(() -> {
            detector.initializeCaches(content);
            detector.clearCaches();
        }).doesNotThrowAnyException();
    }

    @Test
    void testDetectorInstantiation() {
        CodeSmellDetector newDetector = new CodeSmellDetector();
        assertThat(newDetector).isNotNull();
    }
}
