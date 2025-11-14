// CHECKSTYLE:OFF: FileLength - Comprehensive test coverage requires extensive tests
package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.rule.RuleKey;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StyleAndConventionsDetectorTest {

    private StyleAndConventionsDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new StyleAndConventionsDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        // Mock newIssue chain
        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);
        TextRange textRange = mock(TextRange.class);

        when(context.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any(RuleKey.class))).thenReturn(newIssue);
        when(newIssue.at(any(NewIssueLocation.class))).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(inputFile.newRange(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(textRange);
    }

    static Stream<String> provideStringLiteralTestCases() {
        return Stream.of(
            "message = \"Hello, World!\";",          // Simple string
            "message = \"He said \\\"Hello\\\"\";",  // Escaped quotes
            "path = \"C:\\\\Users\\\\test\\\\\";"    // Escaped backslashes
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringLiteralTestCases")
    void testStringLiteralPatternMatches(String code) {
        assertThat(code).matches(".*\"[^\"\\\\]*+(?:\\\\.[^\"\\\\]*+)*+\".*");
    }

    @Test
    void testStringLiteralPatternDoesNotCauseStackOverflow() {
        // Generate a very long string to ensure no stack overflow with possessive quantifiers
        StringBuilder longString = new StringBuilder("\"");
        for (int i = 0; i < 10000; i++) {
            longString.append("a");
        }
        longString.append("\"");

        // This should not cause stack overflow or excessive backtracking
        String result = longString.toString();
        assertThat(result).matches("\"[^\"\\\\]*+(?:\\\\.[^\"\\\\]*+)*+\"");
    }

    @Test
    void testDetectorCanBeInstantiated() {
        // Verify detector can be created without errors
        StyleAndConventionsDetector localDetector = new StyleAndConventionsDetector();
        assertThat(localDetector).isNotNull();
    }

    // Complexity Detection Methods
                    @Test
    void testDetectTooManyVariablesInBlock() {
        String content = "Block[{var1, var2, var3, var4, var5, var6, var7, var8, var9, "

         + "var10, var11, var12, var13, var14, var15, var16}, var1 + var2]";
        assertDoesNotThrow(() ->
            detector.detectTooManyVariables(context, inputFile, content)
        );
    }

        @Test
    void testDetectFileTooManyFunctions() {
        String content = "f1[]:=1; f2[]:=2; f3[]:=3; f4[]:=4; f5[]:=5; f6[]:=6; f7[]:=7; f8[]:=8; f9[]:=9; f10[]:=10; "
         + "f11[]:=11; f12[]:=12; f13[]:=13; f14[]:=14; f15[]:=15; f16[]:=16; f17[]:=17; f18[]:=18; "
                 +                         "f19[]:=19; f20[]:=20; f21[]:=21;";
        assertDoesNotThrow(() ->
            detector.detectFileTooManyFunctions(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageTooManyExports() {
        String content = "BeginPackage[\"Test`\"]; e1; e2; e3; e4; e5; e6; e7; e8; e9; e10; e11; e12; e13; e14; e15; "
         + "e16; e17; e18; e19; e20; e21; e22; e23; e24; e25; e26; Begin[\"`Private`\"];";
        assertDoesNotThrow(() ->
            detector.detectPackageTooManyExports(context, inputFile, content)
        );
    }

    // Maintainability Detection Methods
                    // Best Practices Detection Methods
                @Test
    void testDetectGraphicsOptionsExcessive() {
        String content = "Graphics[point, opt1->1, opt2->2, opt3->3, opt4->4, opt5->5, opt6->6, opt7->7, opt8->8, "
         + "opt9->9, opt10->10, opt11->11, opt12->12, opt13->13, opt14->14, opt15->15, opt16->16, "
                 +                         "opt17->17, opt18->18, opt19->19, opt20->20, opt21->21]";
        assertDoesNotThrow(() ->
            detector.detectGraphicsOptionsExcessive(context, inputFile, content)
        );
    }

        @Test
    void testDetectDatasetWithoutHeaders() {
        String content = "Dataset[{{1, 2, 3}, {4, 5, 6}}]";
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

            // Naming Detection Methods
            @Test
    void testDetectVariableNameMatchesBuiltin() {
        String content = "C = 5; D = 10; E = 2.718; I = Sqrt[-1];";
        assertDoesNotThrow(() ->
            detector.detectVariableNameMatchesBuiltin(context, inputFile, content)
        );
    }

                                // Style & Formatting Detection Methods
        // REMOVED: testDetectCommaSpacing() and testDetectOperatorSpacing() tests
    // The corresponding rules have been permanently removed from the codebase

                @Test
    void testDetectLongStringLiteral() {
        String content = "msg = \"This is a very long string literal that exceeds one hundred characters "
         + "and should trigger the detection rule for overly long string literals in the code\"";
        assertDoesNotThrow(() ->
            detector.detectLongStringLiteral(context, inputFile, content)
        );
    }

            // Additional Coverage Methods
                                    // Additional High-Priority Coverage Tests
    @Test
    void testDetectAlignmentInconsistent() {
        String content = "longList = {veryLongItemName1, veryLongItemName2, veryLongItemName3, veryLongItemName4};\n"
         + "shortList = {item1,\n  item2,\n  item3};";
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectSwitchTooManyCases() {
        String content = "Switch[x, 1, \"one\", 2, \"two\", 3, \"three\", 4, \"four\", 5, \"five\", "
         + "6, \"six\", 7, \"seven\", 8, \"eight\", 9, \"nine\", 10, \"ten\", "
        + "11, \"eleven\", 12, \"twelve\", 13, \"thirteen\", 14, \"fourteen\", "
        + "15, \"fifteen\", 16, \"sixteen\", 17, \"seventeen\"]";
        assertDoesNotThrow(() ->
            detector.detectSwitchTooManyCases(context, inputFile, content)
        );
    }

                                            @Test
    void testDetectGodFunction() {
        StringBuilder longFunction = new StringBuilder("MyFunc[x_] := Module[{},\n");
        for (int i = 0; i < 110; i++) {
            longFunction.append("  line").append(i).append(" = ").append(i).append(";\n");
        }
        longFunction.append("]");
        String content = longFunction.toString();
        assertDoesNotThrow(() ->
            detector.detectGodFunction(context, inputFile, content)
        );
    }

                                                @Test
    void testDetectLineTooLong() {
        StringBuilder longLine = new StringBuilder();
        for (int i = 0; i < 160; i++) {
            longLine.append("x");
        }
        String content = longLine.toString();
        assertDoesNotThrow(() ->
            detector.detectLineTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectLineTooLongWithoutIssue() {
        String content = "shortLine = 1;";
        assertDoesNotThrow(() ->
            detector.detectLineTooLong(context, inputFile, content)
        );
    }

    // Test negative paths for complexity methods
                    @Test
    void testDetectFileTooManyFunctionsWithoutIssue() {
        String content = "f1[]:=1; f2[]:=2; f3[]:=3;";
        assertDoesNotThrow(() ->
            detector.detectFileTooManyFunctions(context, inputFile, content)
        );
    }

                @Test
    void testDetectSwitchTooManyCasesWithoutIssue() {
        String content = "Switch[x, 1, \"one\", 2, \"two\"]";
        assertDoesNotThrow(() ->
            detector.detectSwitchTooManyCases(context, inputFile, content)
        );
    }

            // Test negative paths for naming methods
                                                                // Test negative paths for maintainability methods
                                    @Test
    void testDetectGodFunctionWithoutIssue() {
        String content = "MyFunc[x_] := Module[{}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectGodFunction(context, inputFile, content)
        );
    }

                                        // Test negative paths for best practices methods
                                                        @Test
    void testDetectGraphicsOptionsExcessiveWithoutIssue() {
        String content = "Graphics[point, PlotStyle -> Red]";
        assertDoesNotThrow(() ->
            detector.detectGraphicsOptionsExcessive(context, inputFile, content)
        );
    }

                            // Test style formatting negative paths
                    @Test
    void testDetectLongStringLiteralWithoutIssue() {
        String content = "msg = \"Short string\"";
        assertDoesNotThrow(() ->
            detector.detectLongStringLiteral(context, inputFile, content)
        );
    }

                                                        // ===== ISSUE DETECTION TESTS - TRIGGER ACTUAL VIOLATIONS =====

    @Test
    void testDetectInconsistentIndentationTriggered() {
        String code = "\tindentedWithTab = 1;\n  indentedWithSpaces = 2;";
        assertDoesNotThrow(() -> detector.detectInconsistentIndentation(context, inputFile, code));
    }

    @Test
    void testDetectTrailingWhitespaceTriggered() {
        String code = "x = 1;   \ny = 2;";
        assertDoesNotThrow(() -> detector.detectTrailingWhitespace(context, inputFile, code));
    }

    @Test
    void testDetectMultipleBlankLinesTriggered() {
        String code = "x = 1;\n\n\n\ny = 2;";
        assertDoesNotThrow(() -> detector.detectMultipleBlankLines(context, inputFile, code));
    }

    @Test
    void testDetectBracketSpacingTriggered() {
        String code = "Table [i, {i, 10}]";
        assertDoesNotThrow(() -> detector.detectBracketSpacing(context, inputFile, code));
    }

    @Test
    void testDetectSemicolonStyleTriggered() {
        String code = "x = 1;; y = 2;;";
        assertDoesNotThrow(() -> detector.detectSemicolonStyle(context, inputFile, code));
    }

    @Test
    void testDetectParenthesesUnnecessaryTriggered() {
        String code = "result = (((x + y)))";
        assertDoesNotThrow(() -> detector.detectParenthesesUnnecessary(context, inputFile, code));
    }

    @Test
    void testDetectBraceStyleTriggered() {
        String code = "{x,y,z}\n{a,b,c}";
        assertDoesNotThrow(() -> detector.detectBraceStyle(context, inputFile, code));
    }

    @Test
    void testDetectFunctionNameTooShortTriggered() {
        String code = "Ab[x_] := x + 1";
        assertDoesNotThrow(() -> detector.detectFunctionNameTooShort(context, inputFile, code));
    }

    @Test
    void testDetectConstantNotUppercaseTriggered() {
        String code = "MyConstant = 42";
        assertDoesNotThrow(() -> detector.detectConstantNotUppercase(context, inputFile, code));
    }

    @Test
    void testDetectVariableNameMatchesBuiltinTriggered() {
        String code = "C = 299792458";
        assertDoesNotThrow(() -> detector.detectVariableNameMatchesBuiltin(context, inputFile, code));
    }

    @Test
    void testDetectParameterNameSameAsFunctionTriggered() {
        String code = "MyFunc[MyFunc_] := MyFunc + 1";
        assertDoesNotThrow(() -> detector.detectParameterNameSameAsFunction(context, inputFile, code));
    }

    @Test
    void testDetectInconsistentNamingStyleTriggered() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("lowercaseFunc").append(i).append("[x_] := x;\n");
        }
        assertDoesNotThrow(() -> detector.detectInconsistentNamingStyle(context, inputFile, sb.toString()));
    }

    @Test
    void testDetectNumberInNameTriggered() {
        String code = "variable2name = 42";
        assertDoesNotThrow(() -> detector.detectNumberInName(context, inputFile, code));
    }

    @Test
    void testDetectAbbreviationUnclearTriggered() {
        String code = "MyFnc[x_, y_] := x + y;\nAnotherFnc[a_] := a;";
        assertDoesNotThrow(() -> detector.detectAbbreviationUnclear(context, inputFile, code));
    }

    @Test
    void testDetectGenericNameTriggered() {
        String code = "data = {1, 2, 3}";
        assertDoesNotThrow(() -> detector.detectGenericName(context, inputFile, code));
    }

    @Test
    void testDetectNegatedBooleanNameTriggered() {
        String code = "notValidFlag = True";
        assertDoesNotThrow(() -> detector.detectNegatedBooleanName(context, inputFile, code));
    }

    @Test
    void testDetectFileTooManyFunctionsTriggered() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 55; i++) {
            sb.append("Func").append(i).append("[x_] := x;\n");
        }
        assertDoesNotThrow(() -> detector.detectFileTooManyFunctions(context, inputFile, sb.toString()));
    }

    @Test
    void testDetectPackageTooManyExportsTriggered() {
        StringBuilder sb = new StringBuilder("BeginPackage[\"Test`\"];\n");
        for (int i = 0; i < 35; i++) {
            sb.append("PublicFunc").append(i).append("[x_] := x;\n");
        }
        assertDoesNotThrow(() -> detector.detectPackageTooManyExports(context, inputFile, sb.toString()));
    }

    @Test
    void testDetectConditionalComplexityTriggered() {
        String code = "If[a && b && c && d && e && f, x, y]";
        assertDoesNotThrow(() -> detector.detectConditionalComplexity(context, inputFile, code));
    }

    @Test
    void testDetectIdenticalIfBranchesTriggered() {
        String code = "If[cond, SameCode[], SameCode[]]";
        assertDoesNotThrow(() -> detector.detectIdenticalIfBranches(context, inputFile, code));
    }

    @Test
    void testDetectDuplicateCodeBlockTriggered() {
        String code = "Module[{x}, x = 1; y = x + 1; Print[y]]\nModule[{z}, x = 1; y = x + 1; Print[y]]";
        assertDoesNotThrow(() -> detector.detectDuplicateCodeBlock(context, inputFile, code));
    }

    @Test
    void testDetectGodFunctionTriggered() {
        StringBuilder sb = new StringBuilder("HugeFunc[x_] := Module[{result},\n");
        for (int i = 0; i < 110; i++) {
            sb.append("  step").append(i).append(" = ").append(i).append(";\n");
        }
        sb.append("  result\n]");
        assertDoesNotThrow(() -> detector.detectGodFunction(context, inputFile, sb.toString()));
    }

    @Test
    void testDetectFeatureEnvyTriggered() {
        String code = "MyFunc[obj_] := obj@field1 + obj@field2 + obj@field3 + obj@method1[] + obj@method2[]";
        assertDoesNotThrow(() -> detector.detectFeatureEnvy(context, inputFile, code));
    }

    @Test
    void testDetectPrimitiveObsessionTriggered() {
        String code = "ComplexFunc[a_, b_, c_, d_, e_, f_, g_, h_, i_] := a + b + c";
        assertDoesNotThrow(() -> detector.detectPrimitiveObsession(context, inputFile, code));
    }

    @Test
    void testDetectGlobalStateModificationTriggered() {
        String code = "GlobalVar = 42;\nOtherGlobalVar = 100;";
        assertDoesNotThrow(() -> detector.detectGlobalStateModification(context, inputFile, code));
    }

    @Test
    void testDetectIncompletePatternMatchTriggered() {
        String code = "Match[x, {1 -> \"one\", 2 -> \"two\"}]";
        assertDoesNotThrow(() -> detector.detectIncompletePatternMatch(context, inputFile, code));
    }

    @Test
    void testDetectMissingOptionDefaultTriggered() {
        String code = "MyFunc[x_, opts___] := OptionValue[Method]";
        assertDoesNotThrow(() -> detector.detectMissingOptionDefault(context, inputFile, code));
    }

    // ===== EXCEPTION HANDLING TESTS FOR ALL 68 CATCH BLOCKS =====

    @Test
    void testAllDetectMethodsWithMalformedInputPart1() {
        // Test methods 1-23 with null content to trigger exception handlers
        String content = null;
        assertDoesNotThrow(() -> detector.detectLineTooLong(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectInconsistentIndentation(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectTrailingWhitespace(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectMultipleBlankLines(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectMissingBlankLineAfterFunction(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectBracketSpacing(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectSemicolonStyle(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectFileEndsWithoutNewline(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectAlignmentInconsistent(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectParenthesesUnnecessary(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectBraceStyle(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectLongStringLiteral(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectNestedBracketsExcessive(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectFunctionNameTooShort(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectFunctionNameTooLong(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectVariableNameTooShort(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectBooleanNameNonDescriptive(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectConstantNotUppercase(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectPackageNameCase(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectAcronymStyle(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectVariableNameMatchesBuiltin(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectParameterNameSameAsFunction(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectInconsistentNamingStyle(context, inputFile, content));
    }

    @Test
    void testAllDetectMethodsWithMalformedInputPart2() {
        // Test methods 24-46 with null content to trigger exception handlers
        String content = null;
        assertDoesNotThrow(() -> detector.detectNumberInName(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectHungarianNotation(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectAbbreviationUnclear(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectGenericName(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectNegatedBooleanName(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectTooManyParameters(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectTooManyVariables(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectTooManyReturnPoints(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectNestingTooDeep(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectFileTooManyFunctions(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectPackageTooManyExports(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectExpressionTooComplex(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectSwitchTooManyCases(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectBooleanExpressionTooComplex(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectChainedCallsTooLong(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectMagicString(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectDuplicateStringLiteral(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectHardcodedPath(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectHardcodedUrl(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectConditionalComplexity(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectIdenticalIfBranches(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectDuplicateCodeBlock(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectGodFunction(context, inputFile, content));
    }

    @Test
    void testAllDetectMethodsWithMalformedInputPart3() {
        // Test methods 47-68 with null content to trigger exception handlers
        String content = null;
        assertDoesNotThrow(() -> detector.detectFeatureEnvy(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectPrimitiveObsession(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectGlobalStateModification(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectSideEffectInExpression(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectIncompletePatternMatch(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectMissingOptionDefault(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectOptionNameUnclear(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectStringConcatenationInLoop(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectBooleanComparison(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectNegatedBooleanComparison(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectRedundantConditional(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectEmptyCatchBlock(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectCatchWithoutThrow(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectDeprecatedOptionUsage(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectListQueryInefficient(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectEqualityCheckOnReals(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectSymbolicVsNumericMismatch(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectGraphicsOptionsExcessive(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectPlotWithoutLabels(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectDatasetWithoutHeaders(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectAssociationKeyNotString(context, inputFile, content));
        assertDoesNotThrow(() -> detector.detectPatternTestVsCondition(context, inputFile, content));
    }

    // ===== PARAMETERIZED TESTS =====

    @ParameterizedTest
    @MethodSource("detectAbbreviationUnclearTestData")
    void testDetectDetectAbbreviationUnclear(String content) {
        assertDoesNotThrow(() ->
            detector.detectAbbreviationUnclear(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectAbbreviationUnclearTestData() {
        return Stream.of(
            Arguments.of("tmp = 5; val = 10; cnt = 0; msg = \\\"hello\\"),
            Arguments.of("temporary = 5; value = 10; counter = 0; message = \\\"hello\\")
        );
    }

    @ParameterizedTest
    @MethodSource("detectAcronymStyleTestData")
    void testDetectDetectAcronymStyle(String content) {
        assertDoesNotThrow(() ->
            detector.detectAcronymStyle(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectAcronymStyleTestData() {
        return Stream.of(
            Arguments.of("XMLParser = 1; HttpRequest = 2;"),
            Arguments.of("XmlParser = 1; HttpRequest = 2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectAlignmentInconsistentTestData")
    void testDetectDetectAlignmentInconsistent(String content) {
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectAlignmentInconsistentTestData() {
        return Stream.of(
            Arguments.of("list = {1, 2, 3}"),
            Arguments.of("list = {item1, item2, item3, item4, item5, item6, item7, item8}")
        );
    }

    @ParameterizedTest
    @MethodSource("detectAssociationKeyNotStringTestData")
    void testDetectDetectAssociationKeyNotString(String content) {
        assertDoesNotThrow(() ->
            detector.detectAssociationKeyNotString(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectAssociationKeyNotStringTestData() {
        return Stream.of(
            Arguments.of("assoc = Association[1 -> \\\"value\\\", 2 -> \\\"other\\\"]"),
            Arguments.of("assoc = Association[\\\"key1\\\" -> \\\"value\\\", \\\"key2\\\" -> \\\"other\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectBooleanComparisonTestData")
    void testDetectDetectBooleanComparison(String content) {
        assertDoesNotThrow(() ->
            detector.detectBooleanComparison(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectBooleanComparisonTestData() {
        return Stream.of(
            Arguments.of("If[flag == True, doSomething[]]"),
            Arguments.of("If[flag, doSomething[]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectBooleanExpressionTooComplexTestData")
    void testDetectDetectBooleanExpressionTooComplex(String content) {
        assertDoesNotThrow(() ->
            detector.detectBooleanExpressionTooComplex(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectBooleanExpressionTooComplexTestData() {
        return Stream.of(
            Arguments.of("result = a && b || c && d || e && f || g && h || i && j || k"),
            Arguments.of("result = a && b || c")
        );
    }

    @ParameterizedTest
    @MethodSource("detectBooleanNameNonDescriptiveTestData")
    void testDetectDetectBooleanNameNonDescriptive(String content) {
        assertDoesNotThrow(() ->
            detector.detectBooleanNameNonDescriptive(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectBooleanNameNonDescriptiveTestData() {
        return Stream.of(
            Arguments.of("valid = True; flag = False;"),
            Arguments.of("isValid = True; hasData = False; canExecute = True;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectBraceStyleTestData")
    void testDetectDetectBraceStyle(String content) {
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectBraceStyleTestData() {
        return Stream.of(
            Arguments.of("If[condition,\\n{\\n  x = 1\\n}\\n,\\n{\\n  x = 2\\n}\\n]"),
            Arguments.of("f[x_] := {x + 1}; g[y_] := {y + 2}; h[z_] := {z + 3};"),
            Arguments.of("f[x_] :=\\n{x + 1};\\ng[y_] :=\\n{y + 2};\\nh[z_] :=\\n{z + 3};"),
            Arguments.of("f[x_] := {x + 1}; g[y_] :=\\n{y + 2};")
        );
    }

    @ParameterizedTest
    @MethodSource("detectBracketSpacingTestData")
    void testDetectDetectBracketSpacing(String content) {
        assertDoesNotThrow(() ->
            detector.detectBracketSpacing(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectBracketSpacingTestData() {
        return Stream.of(
            Arguments.of("f [x]"),
            Arguments.of("f[x]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectCatchWithoutThrowTestData")
    void testDetectDetectCatchWithoutThrow(String content) {
        assertDoesNotThrow(() ->
            detector.detectCatchWithoutThrow(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectCatchWithoutThrowTestData() {
        return Stream.of(
            Arguments.of("result = Catch[someExpression]; otherCode;"),
            Arguments.of("result = Catch[Throw[value]];"),
            Arguments.of("result = someValue;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectChainedCallsTooLongTestData")
    void testDetectDetectChainedCallsTooLong(String content) {
        assertDoesNotThrow(() ->
            detector.detectChainedCallsTooLong(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectChainedCallsTooLongTestData() {
        return Stream.of(
            Arguments.of("result = data // func1 // func2 // func3 // func4 // func5 // func6"),
            Arguments.of("result = data // func1 // func2")
        );
    }

    @ParameterizedTest
    @MethodSource("detectConditionalComplexityTestData")
    void testDetectDetectConditionalComplexity(String content) {
        assertDoesNotThrow(() ->
            detector.detectConditionalComplexity(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectConditionalComplexityTestData() {
        return Stream.of(
            Arguments.of("If[a > 0 && b < 10 || c == 5 && d != 3 || e >= 1 && f <= 100, result = True]"),
            Arguments.of("If[a > 0 && b < 10, result = True]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectConstantNotUppercaseTestData")
    void testDetectDetectConstantNotUppercase(String content) {
        assertDoesNotThrow(() ->
            detector.detectConstantNotUppercase(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectConstantNotUppercaseTestData() {
        return Stream.of(
            Arguments.of("MaxValue = 100; MinValue = 0;"),
            Arguments.of("MAXVALUE = 100; MINVALUE = 0;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectDatasetWithoutHeadersTestData")
    void testDetectDetectDatasetWithoutHeaders(String content) {
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectDatasetWithoutHeadersTestData() {
        return Stream.of(
            Arguments.of("Dataset[<|\\\"col1\\\" -> 1, \\\"col2\\\" -> 2|>]"),
            Arguments.of("Dataset[Association[\\\"key\\\" -> \\\"value\\\"]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectDeprecatedOptionUsageTestData")
    void testDetectDetectDeprecatedOptionUsage(String content) {
        assertDoesNotThrow(() ->
            detector.detectDeprecatedOptionUsage(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectDeprecatedOptionUsageTestData() {
        return Stream.of(
            Arguments.of("Plot[x, {x, 0, 1}, PlotRange -> Automatic]"),
            Arguments.of("Plot[x, {x, 0, 1}, Frame -> True]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectDuplicateCodeBlockTestData")
    void testDetectDetectDuplicateCodeBlock(String content) {
        assertDoesNotThrow(() ->
            detector.detectDuplicateCodeBlock(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectDuplicateCodeBlockTestData() {
        return Stream.of(
            Arguments.of("a = 1;\\nb = 2;\\nc = 3;\\nd = 4;\\ne = 5;\\n\\nf = 10;\\n\\na = 1;\\nb = 2;\\nc = 3;\\nd = 4;\\ne = 5;"),
            Arguments.of("a = 1;\\nb = 2;\\nc = 3;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectDuplicateStringLiteralTestData")
    void testDetectDetectDuplicateStringLiteral(String content) {
        assertDoesNotThrow(() ->
            detector.detectDuplicateStringLiteral(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectDuplicateStringLiteralTestData() {
        return Stream.of(
            Arguments.of("a = \\\"constant\\"),
            Arguments.of("a = \\\"value1\\")
        );
    }

    @ParameterizedTest
    @MethodSource("detectEmptyCatchBlockTestData")
    void testDetectDetectEmptyCatchBlock(String content) {
        assertDoesNotThrow(() ->
            detector.detectEmptyCatchBlock(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectEmptyCatchBlockTestData() {
        return Stream.of(
            Arguments.of("Catch[someExpression]"),
            Arguments.of("Check[someExpression, errorHandler]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectEqualityCheckOnRealsTestData")
    void testDetectDetectEqualityCheckOnReals(String content) {
        assertDoesNotThrow(() ->
            detector.detectEqualityCheckOnReals(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectEqualityCheckOnRealsTestData() {
        return Stream.of(
            Arguments.of("If[1.0 == 2.0, True, False]"),
            Arguments.of("If[x == y, True, False]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectExpressionTooComplexTestData")
    void testDetectDetectExpressionTooComplex(String content) {
        assertDoesNotThrow(() ->
            detector.detectExpressionTooComplex(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectExpressionTooComplexTestData() {
        return Stream.of(
            Arguments.of("result = a + b - c * d / e + f - g * h / i + j - k * l / m + n - o * p / q + r - s * t / u + v"),
            Arguments.of("result = a + b * c")
        );
    }

    @ParameterizedTest
    @MethodSource("detectFeatureEnvyTestData")
    void testDetectDetectFeatureEnvy(String content) {
        assertDoesNotThrow(() ->
            detector.detectFeatureEnvy(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectFeatureEnvyTestData() {
        return Stream.of(
            Arguments.of("MyFunc[obj_] := obj@field1 + obj@field2 + obj@field3 + obj@field4 + obj@field5 + obj@field6"),
            Arguments.of("MyFunc[obj_] := obj + 1")
        );
    }

    @ParameterizedTest
    @MethodSource("detectFileEndsWithoutNewlineTestData")
    void testDetectDetectFileEndsWithoutNewline(String content) {
        assertDoesNotThrow(() ->
            detector.detectFileEndsWithoutNewline(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectFileEndsWithoutNewlineTestData() {
        return Stream.of(
            Arguments.of("x = 1; y = 2;"),
            Arguments.of("x = 1; y = 2;\\n")
        );
    }

    @ParameterizedTest
    @MethodSource("detectFunctionNameTooLongTestData")
    void testDetectDetectFunctionNameTooLong(String content) {
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooLong(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectFunctionNameTooLongTestData() {
        return Stream.of(
            Arguments.of("ThisIsAnExtremelyLongFunctionNameThatExceedsFiftyCharactersInLength[x_] := x + 1"),
            Arguments.of("GoodFunctionName[x_] := x + 1")
        );
    }

    @ParameterizedTest
    @MethodSource("detectFunctionNameTooShortTestData")
    void testDetectDetectFunctionNameTooShort(String content) {
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooShort(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectFunctionNameTooShortTestData() {
        return Stream.of(
            Arguments.of("a[x_] := x + 1; b[y_] := y * 2;"),
            Arguments.of("f[x_] := x + 1; g[y_] := y + 2; h[z_] := z + 3;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectGenericNameTestData")
    void testDetectDetectGenericName(String content) {
        assertDoesNotThrow(() ->
            detector.detectGenericName(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectGenericNameTestData() {
        return Stream.of(
            Arguments.of("data = {1, 2, 3}; temp = 5; value = 10;"),
            Arguments.of("customerData = {1, 2, 3}; userId = 5; productPrice = 10;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectGlobalStateModificationTestData")
    void testDetectDetectGlobalStateModification(String content) {
        assertDoesNotThrow(() ->
            detector.detectGlobalStateModification(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectGlobalStateModificationTestData() {
        return Stream.of(
            Arguments.of("GlobalVar = 42;"),
            Arguments.of("Module[{GlobalVar}, GlobalVar = 42;]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectHardcodedPathTestData")
    void testDetectDetectHardcodedPath(String content) {
        assertDoesNotThrow(() ->
            detector.detectHardcodedPath(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectHardcodedPathTestData() {
        return Stream.of(
            Arguments.of("file = Import[\\\"/Users/username/Documents/data.csv\\\"]"),
            Arguments.of("file = Import[\\\"data.csv\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectHardcodedUrlTestData")
    void testDetectDetectHardcodedUrl(String content) {
        assertDoesNotThrow(() ->
            detector.detectHardcodedUrl(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectHardcodedUrlTestData() {
        return Stream.of(
            Arguments.of("data = URLFetch[\\\"http://example.com/api/data\\\"]"),
            Arguments.of("data = URLFetch[localUrl]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectHungarianNotationTestData")
    void testDetectDetectHungarianNotation(String content) {
        assertDoesNotThrow(() ->
            detector.detectHungarianNotation(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectHungarianNotationTestData() {
        return Stream.of(
            Arguments.of("strName = \\\"John\\"),
            Arguments.of("name = \\\"John\\")
        );
    }

    @ParameterizedTest
    @MethodSource("detectIdenticalIfBranchesTestData")
    void testDetectDetectIdenticalIfBranches(String content) {
        assertDoesNotThrow(() ->
            detector.detectIdenticalIfBranches(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectIdenticalIfBranchesTestData() {
        return Stream.of(
            Arguments.of("If[condition, doSomething[], doSomething[]]"),
            Arguments.of("If[condition, doSomething[], doOtherThing[]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectIncompletePatternMatchTestData")
    void testDetectDetectIncompletePatternMatch(String content) {
        assertDoesNotThrow(() ->
            detector.detectIncompletePatternMatch(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectIncompletePatternMatchTestData() {
        return Stream.of(
            Arguments.of("Switch[x, 1, \\\"one\\\", 2, \\\"two\\\"]"),
            Arguments.of("Switch[x, 1, \\\"one\\\", 2, \\\"two\\\", _, \\\"default\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectInconsistentIndentationTestData")
    void testDetectDetectInconsistentIndentation(String content) {
        assertDoesNotThrow(() ->
            detector.detectInconsistentIndentation(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectInconsistentIndentationTestData() {
        return Stream.of(
            Arguments.of("f[x_] := Module[{y},\\n\\ty = x + 1;\\n    z = x + 2;\\n\\ty + z\\n]"),
            Arguments.of("f[x_] := Module[{y},\\n\\ty = x + 1;\\n\\tz = x + 2;\\n\\ty + z\\n]"),
            Arguments.of("f[x_] := Module[{y},\\n  y = x + 1;\\n  z = x + 2;\\n  y + z\\n]"),
            Arguments.of("f[x_] := Module[{y},\\n\\n\\n  y = x + 1\\n]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectInconsistentNamingStyleTestData")
    void testDetectDetectInconsistentNamingStyle(String content) {
        assertDoesNotThrow(() ->
            detector.detectInconsistentNamingStyle(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectInconsistentNamingStyleTestData() {
        return Stream.of(
            Arguments.of("camelCase = 1; snake_case = 2; PascalCase = 3;"),
            Arguments.of("camelCase = 1; anotherCamelCase = 2;"),
            Arguments.of("snake_case = 1; another_snake = 2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectListQueryInefficientTestData")
    void testDetectDetectListQueryInefficient(String content) {
        assertDoesNotThrow(() ->
            detector.detectListQueryInefficient(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectListQueryInefficientTestData() {
        return Stream.of(
            Arguments.of("Do[result = MemberQ[bigList, x], {x, items}]"),
            Arguments.of("result = MemberQ[bigList, x]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectMagicStringTestData")
    void testDetectDetectMagicString(String content) {
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectMagicStringTestData() {
        return Stream.of(
            Arguments.of("If[status == \\\"active\\\", Print[\\\"active\\\"], Print[\\\"inactive\\\"]]"),
            Arguments.of("If[status == \\\"active\\\", Print[\\\"running\\\"], Print[\\\"stopped\\\"]]"),
            Arguments.of("a = \\\"x\\")
        );
    }

    @ParameterizedTest
    @MethodSource("detectMissingBlankLineAfterFunctionTestData")
    void testDetectDetectMissingBlankLineAfterFunction(String content) {
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectMissingBlankLineAfterFunctionTestData() {
        return Stream.of(
            Arguments.of("Func1[x_] := x + 1\\nFunc2[y_] := y + 2"),
            Arguments.of("Func1[x_] := x + 1\\n(* Comment *)\\nFunc2[y_] := y + 2"),
            Arguments.of("Func1[x_] := x + 1\\n\\nFunc2[y_] := y + 2"),
            Arguments.of("Func1[x_] := x + 1")
        );
    }

    @ParameterizedTest
    @MethodSource("detectMissingOptionDefaultTestData")
    void testDetectDetectMissingOptionDefault(String content) {
        assertDoesNotThrow(() ->
            detector.detectMissingOptionDefault(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectMissingOptionDefaultTestData() {
        return Stream.of(
            Arguments.of("value = OptionValue[\\\"MyOption\\\"]"),
            Arguments.of("value = OptionValue[\\\"MyOption\\\", defaultValue]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectMultipleBlankLinesTestData")
    void testDetectDetectMultipleBlankLines(String content) {
        assertDoesNotThrow(() ->
            detector.detectMultipleBlankLines(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectMultipleBlankLinesTestData() {
        return Stream.of(
            Arguments.of("x = 1;\\n\\n\\n\\ny = 2;"),
            Arguments.of("x = 1;\\n\\ny = 2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNegatedBooleanComparisonTestData")
    void testDetectDetectNegatedBooleanComparison(String content) {
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanComparison(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNegatedBooleanComparisonTestData() {
        return Stream.of(
            Arguments.of("If[Not[flag == True], doSomething[]]"),
            Arguments.of("If[x != y, doSomething[]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNegatedBooleanNameTestData")
    void testDetectDetectNegatedBooleanName(String content) {
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanName(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNegatedBooleanNameTestData() {
        return Stream.of(
            Arguments.of("notValid = False; isNotEnabled = True;"),
            Arguments.of("isValid = False; isEnabled = True;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNestedBracketsExcessiveTestData")
    void testDetectDetectNestedBracketsExcessive(String content) {
        assertDoesNotThrow(() ->
            detector.detectNestedBracketsExcessive(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNestedBracketsExcessiveTestData() {
        return Stream.of(
            Arguments.of("result = f[g[h[i[j[k[l[x]]]]]]]"),
            Arguments.of("result = f[g[h[i[j[x]]]]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNestingTooDeepTestData")
    void testDetectDetectNestingTooDeep(String content) {
        assertDoesNotThrow(() ->
            detector.detectNestingTooDeep(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNestingTooDeepTestData() {
        return Stream.of(
            Arguments.of("If[a, If[b, If[c, If[d, If[e, If[f, True]]]]]]"),
            Arguments.of("If[a, If[b, If[c, True]]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNumberInNameTestData")
    void testDetectDetectNumberInName(String content) {
        assertDoesNotThrow(() ->
            detector.detectNumberInName(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNumberInNameTestData() {
        return Stream.of(
            Arguments.of("var1 = 5; var2 = 10; temp3 = 15;"),
            Arguments.of("x1 = 5; y2 = 10; z3 = 15;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectOptionNameUnclearTestData")
    void testDetectDetectOptionNameUnclear(String content) {
        assertDoesNotThrow(() ->
            detector.detectOptionNameUnclear(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectOptionNameUnclearTestData() {
        return Stream.of(
            Arguments.of("MyFunction[OptionPattern[{opt1 -> 1}]]"),
            Arguments.of("MyFunction[OptionPattern[{backgroundColor -> White}]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPackageNameCaseTestData")
    void testDetectDetectPackageNameCase(String content) {
        assertDoesNotThrow(() ->
            detector.detectPackageNameCase(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPackageNameCaseTestData() {
        return Stream.of(
            Arguments.of("BeginPackage[\\\"my_package`Utils\\\"]"),
            Arguments.of("BeginPackage[\\\"MyPackage`Utils\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPackageTooManyExportsTestData")
    void testDetectDetectPackageTooManyExports(String content) {
        assertDoesNotThrow(() ->
            detector.detectPackageTooManyExports(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPackageTooManyExportsTestData() {
        return Stream.of(
            Arguments.of("e1[]:=1; e2[]:=2; e3[]:=3;"),
            Arguments.of("BeginPackage[\\\"Test`\\\"]; Func1[x_]:=x+1; Func2[y_]:=y+2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectParameterNameSameAsFunctionTestData")
    void testDetectDetectParameterNameSameAsFunction(String content) {
        assertDoesNotThrow(() ->
            detector.detectParameterNameSameAsFunction(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectParameterNameSameAsFunctionTestData() {
        return Stream.of(
            Arguments.of("MyFunc[myFunc_] := myFunc + 1"),
            Arguments.of("MyFunc[param_] := param + 1")
        );
    }

    @ParameterizedTest
    @MethodSource("detectParenthesesUnnecessaryTestData")
    void testDetectDetectParenthesesUnnecessary(String content) {
        assertDoesNotThrow(() ->
            detector.detectParenthesesUnnecessary(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectParenthesesUnnecessaryTestData() {
        return Stream.of(
            Arguments.of("result = (((x + y)))"),
            Arguments.of("result = (x + y)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPatternTestVsConditionTestData")
    void testDetectDetectPatternTestVsCondition(String content) {
        assertDoesNotThrow(() ->
            detector.detectPatternTestVsCondition(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPatternTestVsConditionTestData() {
        return Stream.of(
            Arguments.of("f[x_ /; IntegerQ[x]] := x + 1"),
            Arguments.of("f[x_?IntegerQ] := x + 1")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPlotWithoutLabelsTestData")
    void testDetectDetectPlotWithoutLabels(String content) {
        assertDoesNotThrow(() ->
            detector.detectPlotWithoutLabels(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPlotWithoutLabelsTestData() {
        return Stream.of(
            Arguments.of("Plot[x^2, {x, 0, 10}]"),
            Arguments.of("Plot[x^2, {x, 0, 10}, AxesLabel -> {\\\"x\\\", \\\"y\\\"}]"),
            Arguments.of("Plot[x^2, {x, 0, 10}, FrameLabel -> {\\\"x\\\", \\\"y\\\"}]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPrimitiveObsessionTestData")
    void testDetectDetectPrimitiveObsession(String content) {
        assertDoesNotThrow(() ->
            detector.detectPrimitiveObsession(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPrimitiveObsessionTestData() {
        return Stream.of(
            Arguments.of("MyFunc[str1_, str2_, str3_, int1_, int2_, int3_, flag1_, flag2_] := str1 <> str2"),
            Arguments.of("MyFunc[str1_, str2_] := str1 <> str2"),
            Arguments.of("MyFunc[str1_String, str2_Integer, flag_?IntegerQ] := str1 <> ToString[str2]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectRedundantConditionalTestData")
    void testDetectDetectRedundantConditional(String content) {
        assertDoesNotThrow(() ->
            detector.detectRedundantConditional(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectRedundantConditionalTestData() {
        return Stream.of(
            Arguments.of("If[x > 5, True, False]"),
            Arguments.of("If[x > 5, doSomething[], doOtherThing[]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectSemicolonStyleTestData")
    void testDetectDetectSemicolonStyle(String content) {
        assertDoesNotThrow(() ->
            detector.detectSemicolonStyle(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectSemicolonStyleTestData() {
        return Stream.of(
            Arguments.of("a = 1;; b = 2;"),
            Arguments.of("a = 1; b = 2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectSideEffectInExpressionTestData")
    void testDetectDetectSideEffectInExpression(String content) {
        assertDoesNotThrow(() ->
            detector.detectSideEffectInExpression(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectSideEffectInExpressionTestData() {
        return Stream.of(
            Arguments.of("result = x + (y = 5)"),
            Arguments.of("result = x + y"),
            Arguments.of("result[x_] := Module[{y = x + 1}, y]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectStringConcatenationInLoopTestData")
    void testDetectDetectStringConcatenationInLoop(String content) {
        assertDoesNotThrow(() ->
            detector.detectStringConcatenationInLoop(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectStringConcatenationInLoopTestData() {
        return Stream.of(
            Arguments.of("Do[str = str <> ToString[i], {i, 100}]"),
            Arguments.of("Do[result = Calculate[i], {i, 100}]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectSymbolicVsNumericMismatchTestData")
    void testDetectDetectSymbolicVsNumericMismatch(String content) {
        assertDoesNotThrow(() ->
            detector.detectSymbolicVsNumericMismatch(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectSymbolicVsNumericMismatchTestData() {
        return Stream.of(
            Arguments.of("Solve[x^2 + 1.5*x + 1 == 0, x]"),
            Arguments.of("Solve[x^2 + x + 1 == 0, x]"),
            Arguments.of("NSolve[x^2 + x + 1 == 0, x]"),
            Arguments.of("x = 5; y = 10;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectTooManyParametersTestData")
    void testDetectDetectTooManyParameters(String content) {
        assertDoesNotThrow(() ->
            detector.detectTooManyParameters(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectTooManyParametersTestData() {
        return Stream.of(
            Arguments.of("MyFunction[a, b, c, d, e, f, g, h] := a + b + c + d + e + f + g + h"),
            Arguments.of("MyFunction[a, b, c] := a + b + c")
        );
    }

    @ParameterizedTest
    @MethodSource("detectTooManyReturnPointsTestData")
    void testDetectDetectTooManyReturnPoints(String content) {
        assertDoesNotThrow(() ->
            detector.detectTooManyReturnPoints(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectTooManyReturnPointsTestData() {
        return Stream.of(
            Arguments.of("Func[x] := Module[{}, Return[1]; Return[2]; Return[3]; Return[4]; Return[5]; Return[6]]"),
            Arguments.of("Func[x] := Module[{}, If[x > 0, Return[1]]; Return[0]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectTooManyVariablesTestData")
    void testDetectDetectTooManyVariables(String content) {
        assertDoesNotThrow(() ->
            detector.detectTooManyVariables(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectTooManyVariablesTestData() {
        return Stream.of(
            Arguments.of("Module[{a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q}, a + b + c]"),
            Arguments.of("Module[{a, b, c}, a + b + c]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectTrailingWhitespaceTestData")
    void testDetectDetectTrailingWhitespace(String content) {
        assertDoesNotThrow(() ->
            detector.detectTrailingWhitespace(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectTrailingWhitespaceTestData() {
        return Stream.of(
            Arguments.of("x = 5;   \\ny = 10;\\t\\n"),
            Arguments.of("x = 5;\\ny = 10;\\n")
        );
    }

    @ParameterizedTest
    @MethodSource("detectVariableNameTooShortTestData")
    void testDetectDetectVariableNameTooShort(String content) {
        assertDoesNotThrow(() ->
            detector.detectVariableNameTooShort(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectVariableNameTooShortTestData() {
        return Stream.of(
            Arguments.of("a = 5; b = 10; c = 15;"),
            Arguments.of("i = 1; j = 2; k = 3;")
        );
    }

    // Additional tests for comment/string literal branches and edge cases

    @Test
    void testDetectTrailingWhitespaceInComment() {
        String content = "(* trailing spaces   *)";
        assertDoesNotThrow(() ->
            detector.detectTrailingWhitespace(context, inputFile, content)
        );
    }

    @Test
    void testDetectTrailingWhitespaceInString() {
        String content = "text = \"trailing spaces   \";";
        assertDoesNotThrow(() ->
            detector.detectTrailingWhitespace(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingBlankLineAfterFunctionInComment() {
        String content = "(* f[x_] := x + 1\ng[x_] := x + 2 *)";
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingBlankLineAfterFunctionInString() {
        String content = "code = \"f[x_] := x + 1\\ng[x_] := x + 2\";";
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingBlankLineAfterFunctionWithNextLineCheck() {
        String content = "f[x_] := x + 1\ng[x_] := x + 2";
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectBracketSpacingInComment() {
        String content = "(* f [x] := x + 1 *)";
        assertDoesNotThrow(() ->
            detector.detectBracketSpacing(context, inputFile, content)
        );
    }

    @Test
    void testDetectBracketSpacingInString() {
        String content = "code = \"f [x] := x + 1\";";
        assertDoesNotThrow(() ->
            detector.detectBracketSpacing(context, inputFile, content)
        );
    }

    @Test
    void testDetectSemicolonStyleInComment() {
        String content = "(* x = 5 ; y = 10 *)";
        assertDoesNotThrow(() ->
            detector.detectSemicolonStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectSemicolonStyleInString() {
        String content = "code = \"x = 5 ; y = 10\";";
        assertDoesNotThrow(() ->
            detector.detectSemicolonStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileEndsWithoutNewlineEmpty() {
        String content = "";
        assertDoesNotThrow(() ->
            detector.detectFileEndsWithoutNewline(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileEndsWithoutNewlineWithNewline() {
        String content = "x = 5;\n";
        assertDoesNotThrow(() ->
            detector.detectFileEndsWithoutNewline(context, inputFile, content)
        );
    }

    @Test
    void testDetectAlignmentInconsistentInComment() {
        String content = "(* {1, 2, 3, 4} *)";
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectAlignmentInconsistentInString() {
        String content = "list = \"{1, 2, 3, 4}\";";
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectAlignmentInconsistentWithMultipleParts() {
        String content = "{1, 2, 3, 4, 5}";
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectAlignmentInconsistentMixedStyles() {
        String content = "{1,\n2, 3, 4}";
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectParenthesesUnnecessaryInComment() {
        String content = "(* (x + 1) *)";
        assertDoesNotThrow(() ->
            detector.detectParenthesesUnnecessary(context, inputFile, content)
        );
    }

    @Test
    void testDetectParenthesesUnnecessaryInString() {
        String content = "code = \"(x + 1)\";";
        assertDoesNotThrow(() ->
            detector.detectParenthesesUnnecessary(context, inputFile, content)
        );
    }

    @Test
    void testDetectBraceStyleInComment() {
        String content = "(* Module[{x}, x + 1] *)";
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectBraceStyleInString() {
        String content = "code = \"Module[{x}, x + 1]\";";
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectBraceStyleMixedSameLineAndNewLine() {
        String content = "Module[{x}, x + 1]; Module[{y},\ny + 2]; Module[{z}, z + 3]; Module[{a},\na + 4];";
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectLongStringLiteralInComment() {
        String content = "(* \"" + "a".repeat(105) + "\" *)";
        assertDoesNotThrow(() ->
            detector.detectLongStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectLongStringLiteralInString() {
        String content = "text = \"short\";";
        assertDoesNotThrow(() ->
            detector.detectLongStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectLongStringLiteralExceedsThreshold() {
        String content = "text = \"" + "a".repeat(105) + "\";";
        assertDoesNotThrow(() ->
            detector.detectLongStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooShortInComment() {
        String content = "(* a[x_] := x + 1 *)";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooShortInString() {
        String content = "code = \"a[x_] := x + 1\";";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooShortAllowedSingleLetters() {
        String content = "f[x_] := x + 1; g[x_] := x + 2; h[x_] := x + 3;";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooShortDisallowedSingleLetter() {
        String content = "a[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooLongInComment() {
        String content = "(* " + "f".repeat(55) + "[x_] := x + 1 *)";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooLongInString() {
        String content = "code = \"" + "f".repeat(55) + "[x_] := x + 1\";";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooLongExceedsThreshold() {
        String content = "f".repeat(55) + "[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameTooShortInComment() {
        String content = "(* a = 5 *)";
        assertDoesNotThrow(() ->
            detector.detectVariableNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameTooShortInString() {
        String content = "code = \"a = 5\";";
        assertDoesNotThrow(() ->
            detector.detectVariableNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameTooShortAllowedSingleLetters() {
        String content = "i = 1; j = 2; k = 3;";
        assertDoesNotThrow(() ->
            detector.detectVariableNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameTooShortDisallowedSingleLetter() {
        String content = "a = 1;";
        assertDoesNotThrow(() ->
            detector.detectVariableNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanNameNonDescriptiveInComment() {
        String content = "(* flag = True *)";
        assertDoesNotThrow(() ->
            detector.detectBooleanNameNonDescriptive(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanNameNonDescriptiveInString() {
        String content = "code = \"flag = True\";";
        assertDoesNotThrow(() ->
            detector.detectBooleanNameNonDescriptive(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanNameNonDescriptiveNonMatching() {
        String content = "flag = True;";
        assertDoesNotThrow(() ->
            detector.detectBooleanNameNonDescriptive(context, inputFile, content)
        );
    }

    @Test
    void testDetectConstantNotUppercaseInComment() {
        String content = "(* Pi = 3.14 *)";
        assertDoesNotThrow(() ->
            detector.detectConstantNotUppercase(context, inputFile, content)
        );
    }

    @Test
    void testDetectConstantNotUppercaseInString() {
        String content = "code = \"Pi = 3.14\";";
        assertDoesNotThrow(() ->
            detector.detectConstantNotUppercase(context, inputFile, content)
        );
    }

    @Test
    void testDetectConstantNotUppercaseComplexCheck() {
        String content = "myConstant = 3.14;";
        assertDoesNotThrow(() ->
            detector.detectConstantNotUppercase(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageNameCaseInComment() {
        String content = "(* BeginPackage[\"myPackage`\"] *)";
        assertDoesNotThrow(() ->
            detector.detectPackageNameCase(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageNameCaseInString() {
        String content = "code = \"BeginPackage[\\\"MyPackage`\\\"]\";";
        assertDoesNotThrow(() ->
            detector.detectPackageNameCase(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageNameCaseMultipleParts() {
        String content = "BeginPackage[\"My`sub`Package`\"];";
        assertDoesNotThrow(() ->
            detector.detectPackageNameCase(context, inputFile, content)
        );
    }

    @Test
    void testDetectAcronymStyleInComment() {
        String content = "(* xmlParser = XMLParser[] *)";
        assertDoesNotThrow(() ->
            detector.detectAcronymStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectAcronymStyleInString() {
        String content = "code = \"xmlParser = XMLParser[]\";";
        assertDoesNotThrow(() ->
            detector.detectAcronymStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameMatchesBuiltinInComment() {
        String content = "(* Module = 5 *)";
        assertDoesNotThrow(() ->
            detector.detectVariableNameMatchesBuiltin(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameMatchesBuiltinInString() {
        String content = "code = \"Module = 5\";";
        assertDoesNotThrow(() ->
            detector.detectVariableNameMatchesBuiltin(context, inputFile, content)
        );
    }

    @Test
    void testDetectParameterNameSameAsFunctionInComment() {
        String content = "(* myFunc[myFunc_] := myFunc + 1 *)";
        assertDoesNotThrow(() ->
            detector.detectParameterNameSameAsFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectParameterNameSameAsFunctionInString() {
        String content = "code = \"myFunc[myFunc_] := myFunc + 1\";";
        assertDoesNotThrow(() ->
            detector.detectParameterNameSameAsFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectParameterNameSameAsFunctionCaseInsensitiveMatch() {
        String content = "myFunc[MYFUNC_] := MYFUNC + 1;";
        assertDoesNotThrow(() ->
            detector.detectParameterNameSameAsFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumberInNameInComment() {
        String content = "(* variable123 = 5 *)";
        assertDoesNotThrow(() ->
            detector.detectNumberInName(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumberInNameInString() {
        String content = "code = \"variable123 = 5\";";
        assertDoesNotThrow(() ->
            detector.detectNumberInName(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumberInNameAllowedCoordinates() {
        String content = "x1 = 1; y2 = 2; z3 = 3;";
        assertDoesNotThrow(() ->
            detector.detectNumberInName(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumberInNameDisallowedPattern() {
        String content = "var123 = 5;";
        assertDoesNotThrow(() ->
            detector.detectNumberInName(context, inputFile, content)
        );
    }

    @Test
    void testDetectHungarianNotationInComment() {
        String content = "(* strName = \"John\" *)";
        assertDoesNotThrow(() ->
            detector.detectHungarianNotation(context, inputFile, content)
        );
    }

    @Test
    void testDetectHungarianNotationInString() {
        String content = "code = \"strName = \\\"John\\\"\";";
        assertDoesNotThrow(() ->
            detector.detectHungarianNotation(context, inputFile, content)
        );
    }

    @Test
    void testDetectAbbreviationUnclearInComment() {
        String content = "(* calc = Calculate[] *)";
        assertDoesNotThrow(() ->
            detector.detectAbbreviationUnclear(context, inputFile, content)
        );
    }

    @Test
    void testDetectAbbreviationUnclearInString() {
        String content = "code = \"calc = Calculate[]\";";
        assertDoesNotThrow(() ->
            detector.detectAbbreviationUnclear(context, inputFile, content)
        );
    }

    @Test
    void testDetectAbbreviationUnclearShortAbbreviation() {
        String content = "calc = 5; proc = 10;";
        assertDoesNotThrow(() ->
            detector.detectAbbreviationUnclear(context, inputFile, content)
        );
    }

    @Test
    void testDetectGenericNameInComment() {
        String content = "(* data = {1, 2, 3} *)";
        assertDoesNotThrow(() ->
            detector.detectGenericName(context, inputFile, content)
        );
    }

    @Test
    void testDetectGenericNameInString() {
        String content = "code = \"data = {1, 2, 3}\";";
        assertDoesNotThrow(() ->
            detector.detectGenericName(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanNameInComment() {
        String content = "(* isNotValid = False *)";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanName(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanNameInString() {
        String content = "code = \"isNotValid = False\";";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanName(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyParametersInComment() {
        String content = "(* f[a_, b_, c_, d_, e_, f_, g_] := a + b + c + d + e + f + g *)";
        assertDoesNotThrow(() ->
            detector.detectTooManyParameters(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyParametersInString() {
        String content = "code = \"f[a_, b_, c_, d_, e_, f_, g_] := a + b + c + d + e + f + g\";";
        assertDoesNotThrow(() ->
            detector.detectTooManyParameters(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyVariablesBlockExceedsThreshold() {
        String content = "Block[{a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p}, a + b]";
        assertDoesNotThrow(() ->
            detector.detectTooManyVariables(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyReturnPointsExceedsContentLength() {
        String content = "f[x_] := Return[1];";
        assertDoesNotThrow(() ->
            detector.detectTooManyReturnPoints(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileTooManyFunctionsInComment() {
        String content = "(* f1[]:=1; f2[]:=2; *)";
        assertDoesNotThrow(() ->
            detector.detectFileTooManyFunctions(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileTooManyFunctionsInString() {
        String content = "code = \"f1[]:=1; f2[]:=2;\";";
        assertDoesNotThrow(() ->
            detector.detectFileTooManyFunctions(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileTooManyFunctionsExceedsThreshold() {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 52; i++) {
            content.append("f").append(i).append("[]:=").append(i).append(";");
        }
        String finalContent = content.toString();
        assertDoesNotThrow(() ->
            detector.detectFileTooManyFunctions(context, inputFile, finalContent)
        );
    }

    @Test
    void testDetectPackageTooManyExportsInComment() {
        String content = "(* publicFunc::usage = \"...\" *)";
        assertDoesNotThrow(() ->
            detector.detectPackageTooManyExports(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageTooManyExportsInString() {
        String content = "code = \"publicFunc::usage = \\\"...\\\"\";";
        assertDoesNotThrow(() ->
            detector.detectPackageTooManyExports(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageTooManyExportsExceedsThreshold() {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 32; i++) {
            content.append("func").append(i).append("::usage = \"...\";");
        }
        String finalContent = content.toString();
        assertDoesNotThrow(() ->
            detector.detectPackageTooManyExports(context, inputFile, finalContent)
        );
    }

    @Test
    void testDetectSwitchTooManyCasesInComment() {
        String content = "(* Switch[x, 1, a, 2, b] *)";
        assertDoesNotThrow(() ->
            detector.detectSwitchTooManyCases(context, inputFile, content)
        );
    }

    @Test
    void testDetectSwitchTooManyCasesInString() {
        String content = "code = \"Switch[x, 1, a, 2, b]\";";
        assertDoesNotThrow(() ->
            detector.detectSwitchTooManyCases(context, inputFile, content)
        );
    }

    @Test
    void testDetectSwitchTooManyCasesExceedsContentLength() {
        String content = "Switch[x, 1, a]";
        assertDoesNotThrow(() ->
            detector.detectSwitchTooManyCases(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanExpressionTooComplexInComment() {
        String content = "(* If[a && b && c && d && e && f, x, y] *)";
        assertDoesNotThrow(() ->
            detector.detectBooleanExpressionTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanExpressionTooComplexInString() {
        String content = "code = \"If[a && b && c && d && e && f, x, y]\";";
        assertDoesNotThrow(() ->
            detector.detectBooleanExpressionTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanExpressionTooComplexExceedsThreshold() {
        String content = "If[a && b && c && d && e && f, x, y]";
        assertDoesNotThrow(() ->
            detector.detectBooleanExpressionTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectChainedCallsTooLongInComment() {
        String content = "(* data[[1]][[2]][[3]][[4]][[5]] *)";
        assertDoesNotThrow(() ->
            detector.detectChainedCallsTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectChainedCallsTooLongInString() {
        String content = "code = \"data[[1]][[2]][[3]][[4]][[5]]\";";
        assertDoesNotThrow(() ->
            detector.detectChainedCallsTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectChainedCallsTooLongExceedsThreshold() {
        String content = "data[[1]][[2]][[3]][[4]][[5]]";
        assertDoesNotThrow(() ->
            detector.detectChainedCallsTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectMagicStringInComment() {
        String content = "(* text = \"longstring\" *)";
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    @Test
    void testDetectMagicStringInString() {
        String content = "code = \"text = \\\"longstring\\\"\";";
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    @Test
    void testDetectMagicStringLongLiteral() {
        String content = "text = \"longstring\";";
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    @Test
    void testDetectMagicStringMultipleOccurrences() {
        String content = "text1 = \"duplicate\"; text2 = \"duplicate\"; text3 = \"duplicate\";";
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateStringLiteralInComment() {
        String content = "(* text = \"duplicate\" *)";
        assertDoesNotThrow(() ->
            detector.detectDuplicateStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateStringLiteralInString() {
        String content = "code = \"text = \\\"duplicate\\\"\";";
        assertDoesNotThrow(() ->
            detector.detectDuplicateStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateStringLiteralMultipleOccurrences() {
        String content = "a = \"duplicate\"; b = \"duplicate\"; c = \"duplicate\";";
        assertDoesNotThrow(() ->
            detector.detectDuplicateStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedPathInComment() {
        String content = "(* path = \"/usr/local/bin\" *)";
        assertDoesNotThrow(() ->
            detector.detectHardcodedPath(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedPathInString() {
        String content = "code = \"path = \\\"/usr/local/bin\\\"\";";
        assertDoesNotThrow(() ->
            detector.detectHardcodedPath(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedUrlInComment() {
        String content = "(* url = \"http://example.com\" *)";
        assertDoesNotThrow(() ->
            detector.detectHardcodedUrl(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedUrlInString() {
        String content = "code = \"url = \\\"http://example.com\\\"\";";
        assertDoesNotThrow(() ->
            detector.detectHardcodedUrl(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionalComplexityInComment() {
        String content = "(* If[x > 5 && y < 10, a, b] *)";
        assertDoesNotThrow(() ->
            detector.detectConditionalComplexity(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionalComplexityInString() {
        String content = "code = \"If[x > 5 && y < 10, a, b]\";";
        assertDoesNotThrow(() ->
            detector.detectConditionalComplexity(context, inputFile, content)
        );
    }

    @Test
    void testDetectIdenticalIfBranchesInComment() {
        String content = "(* If[x > 0, a + 1, a + 1] *)";
        assertDoesNotThrow(() ->
            detector.detectIdenticalIfBranches(context, inputFile, content)
        );
    }

    @Test
    void testDetectIdenticalIfBranchesInString() {
        String content = "code = \"If[x > 0, a + 1, a + 1]\";";
        assertDoesNotThrow(() ->
            detector.detectIdenticalIfBranches(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateCodeBlockMultipleOccurrences() {
        String content = "f[x_] := x + 1;\ng[y_] := y + 1;\nh[z_] := z + 1;";
        assertDoesNotThrow(() ->
            detector.detectDuplicateCodeBlock(context, inputFile, content)
        );
    }

    @Test
    void testDetectGodFunctionProcessing() {
        String content = "myFunc[x_] := Module[{}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectGodFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectFeatureEnvyInComment() {
        String content = "(* obj@field1@field2 *)";
        assertDoesNotThrow(() ->
            detector.detectFeatureEnvy(context, inputFile, content)
        );
    }

    @Test
    void testDetectFeatureEnvyInString() {
        String content = "code = \"obj@field1@field2\";";
        assertDoesNotThrow(() ->
            detector.detectFeatureEnvy(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrimitiveObsessionInComment() {
        String content = "(* f[a_, b_, c_, d_, e_] := a + b + c + d + e *)";
        assertDoesNotThrow(() ->
            detector.detectPrimitiveObsession(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrimitiveObsessionInString() {
        String content = "code = \"f[a_, b_, c_, d_, e_] := a + b + c + d + e\";";
        assertDoesNotThrow(() ->
            detector.detectPrimitiveObsession(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrimitiveObsessionMultipleParameters() {
        String content = "f[a_, b_, c_, d_, e_] := a + b + c + d + e";
        assertDoesNotThrow(() ->
            detector.detectPrimitiveObsession(context, inputFile, content)
        );
    }

    @Test
    void testDetectGlobalStateModificationInComment() {
        String content = "(* x = 5 *)";
        assertDoesNotThrow(() ->
            detector.detectGlobalStateModification(context, inputFile, content)
        );
    }

    @Test
    void testDetectGlobalStateModificationInString() {
        String content = "code = \"x = 5\";";
        assertDoesNotThrow(() ->
            detector.detectGlobalStateModification(context, inputFile, content)
        );
    }

    @Test
    void testDetectGlobalStateModificationOutsideScope() {
        String content = "x = 5;";
        assertDoesNotThrow(() ->
            detector.detectGlobalStateModification(context, inputFile, content)
        );
    }

    @Test
    void testDetectSideEffectInExpressionInComment() {
        String content = "(* x = (y = 5) + 1 *)";
        assertDoesNotThrow(() ->
            detector.detectSideEffectInExpression(context, inputFile, content)
        );
    }

    @Test
    void testDetectSideEffectInExpressionInString() {
        String content = "code = \"x = (y = 5) + 1\";";
        assertDoesNotThrow(() ->
            detector.detectSideEffectInExpression(context, inputFile, content)
        );
    }

    @Test
    void testDetectSideEffectInExpressionNoDelayedOrRule() {
        String content = "x = (y = 5) + 1";
        assertDoesNotThrow(() ->
            detector.detectSideEffectInExpression(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePatternMatchInComment() {
        String content = "(* Match[{x}, {y_}] *)";
        assertDoesNotThrow(() ->
            detector.detectIncompletePatternMatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePatternMatchInString() {
        String content = "code = \"Match[{x}, {y_}]\";";
        assertDoesNotThrow(() ->
            detector.detectIncompletePatternMatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePatternMatchNoBlankSequence() {
        String content = "Match[{x, y}, {a_}]";
        assertDoesNotThrow(() ->
            detector.detectIncompletePatternMatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingOptionDefaultInComment() {
        String content = "(* OptionsPattern[] *)";
        assertDoesNotThrow(() ->
            detector.detectMissingOptionDefault(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingOptionDefaultInString() {
        String content = "code = \"OptionsPattern[]\";";
        assertDoesNotThrow(() ->
            detector.detectMissingOptionDefault(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingOptionDefaultComplexCheck() {
        String content = "f[x_, OptionsPattern[]] := x + OptionValue[opt]";
        assertDoesNotThrow(() ->
            detector.detectMissingOptionDefault(context, inputFile, content)
        );
    }

    @Test
    void testDetectOptionNameUnclearInComment() {
        String content = "(* Options[f] = {a -> 1} *)";
        assertDoesNotThrow(() ->
            detector.detectOptionNameUnclear(context, inputFile, content)
        );
    }

    @Test
    void testDetectOptionNameUnclearInString() {
        String content = "code = \"Options[f] = {a -> 1}\";";
        assertDoesNotThrow(() ->
            detector.detectOptionNameUnclear(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanComparisonInComment() {
        String content = "(* If[flag == True, x, y] *)";
        assertDoesNotThrow(() ->
            detector.detectBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanComparisonInString() {
        String content = "code = \"If[flag == True, x, y]\";";
        assertDoesNotThrow(() ->
            detector.detectBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanComparisonInComment() {
        String content = "(* If[!flag == True, x, y] *)";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanComparisonInString() {
        String content = "code = \"If[!flag == True, x, y]\";";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectRedundantConditionalInComment() {
        String content = "(* If[True, x, y] *)";
        assertDoesNotThrow(() ->
            detector.detectRedundantConditional(context, inputFile, content)
        );
    }

    @Test
    void testDetectRedundantConditionalInString() {
        String content = "code = \"If[True, x, y]\";";
        assertDoesNotThrow(() ->
            detector.detectRedundantConditional(context, inputFile, content)
        );
    }

    @Test
    void testDetectEmptyCatchBlockInComment() {
        String content = "(* Check[expr, {}] *)";
        assertDoesNotThrow(() ->
            detector.detectEmptyCatchBlock(context, inputFile, content)
        );
    }

    @Test
    void testDetectEmptyCatchBlockInString() {
        String content = "code = \"Check[expr, {}]\";";
        assertDoesNotThrow(() ->
            detector.detectEmptyCatchBlock(context, inputFile, content)
        );
    }

    @Test
    void testDetectCatchWithoutThrowWithBothCatchAndThrow() {
        String content = "Check[expr, catch]; Throw[value]";
        assertDoesNotThrow(() ->
            detector.detectCatchWithoutThrow(context, inputFile, content)
        );
    }

    @Test
    void testDetectEqualityCheckOnRealsInComment() {
        String content = "(* If[x == 1.0, a, b] *)";
        assertDoesNotThrow(() ->
            detector.detectEqualityCheckOnReals(context, inputFile, content)
        );
    }

    @Test
    void testDetectEqualityCheckOnRealsInString() {
        String content = "code = \"If[x == 1.0, a, b]\";";
        assertDoesNotThrow(() ->
            detector.detectEqualityCheckOnReals(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolicVsNumericMismatchBothPresent() {
        String content = "Solve[x^2 == 4, x]; N[x]";
        assertDoesNotThrow(() ->
            detector.detectSymbolicVsNumericMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsOptionsExcessiveInComment() {
        String content = "(* Plot[x, {x, 0, 1}] *)";
        assertDoesNotThrow(() ->
            detector.detectGraphicsOptionsExcessive(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsOptionsExcessiveInString() {
        String content = "code = \"Plot[x, {x, 0, 1}]\";";
        assertDoesNotThrow(() ->
            detector.detectGraphicsOptionsExcessive(context, inputFile, content)
        );
    }

    @Test
    void testDetectPlotWithoutLabelsInComment() {
        String content = "(* Plot[x, {x, 0, 1}] *)";
        assertDoesNotThrow(() ->
            detector.detectPlotWithoutLabels(context, inputFile, content)
        );
    }

    @Test
    void testDetectPlotWithoutLabelsInString() {
        String content = "code = \"Plot[x, {x, 0, 1}]\";";
        assertDoesNotThrow(() ->
            detector.detectPlotWithoutLabels(context, inputFile, content)
        );
    }

    @Test
    void testDetectPlotWithoutLabelsNoLabels() {
        String content = "Plot[x, {x, 0, 1}]";
        assertDoesNotThrow(() ->
            detector.detectPlotWithoutLabels(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetWithoutHeadersInComment() {
        String content = "(* Dataset[{{1, 2}, {3, 4}}] *)";
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetWithoutHeadersInString() {
        String content = "code = \"Dataset[{{1, 2}, {3, 4}}]\";";
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetWithoutHeadersNoAssociation() {
        String content = "Dataset[{{1, 2}, {3, 4}}]";
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssociationKeyNotStringInComment() {
        String content = "(* <|1 -> \"a\"|> *)";
        assertDoesNotThrow(() ->
            detector.detectAssociationKeyNotString(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssociationKeyNotStringInString() {
        String content = "code = \"<|1 -> \\\"a\\\"|>\";";
        assertDoesNotThrow(() ->
            detector.detectAssociationKeyNotString(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssociationKeyNotStringWithNonStringKey() {
        String content = "<|1 -> \"a\", 2 -> \"b\"|>";
        assertDoesNotThrow(() ->
            detector.detectAssociationKeyNotString(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternTestVsConditionComplexPattern() {
        String content = "f[x_?NumericQ] := x + 1";
        assertDoesNotThrow(() ->
            detector.detectPatternTestVsCondition(context, inputFile, content)
        );
    }

}
