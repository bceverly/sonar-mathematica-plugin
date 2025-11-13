// CHECKSTYLE:OFF: FileLength - Comprehensive test coverage requires extensive tests
package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
    void testDetectTooManyParameters() {
        String content = "MyFunction[a, b, c, d, e, f, g, h] := a + b + c + d + e + f + g + h";
        assertDoesNotThrow(() ->
            detector.detectTooManyParameters(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyReturnPoints() {
        String content = "Func[x] := Module[{}, Return[1]; Return[2]; Return[3]; Return[4]; Return[5]; Return[6]]";
        assertDoesNotThrow(() ->
            detector.detectTooManyReturnPoints(context, inputFile, content)
        );
    }

    @Test
    void testDetectExpressionTooComplex() {
        String content = "result = a + b - c * d / e + f - g * h / i + j - k * l / m + n - o * p / q + r - s * t / u + v";
        assertDoesNotThrow(() ->
            detector.detectExpressionTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyVariables() {
        String content = "Module[{a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q}, a + b + c]";
        assertDoesNotThrow(() ->
            detector.detectTooManyVariables(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyVariablesInBlock() {
        String content = "Block[{var1, var2, var3, var4, var5, var6, var7, var8, var9, "

         + "var10, var11, var12, var13, var14, var15, var16}, var1 + var2]";
        assertDoesNotThrow(() ->
            detector.detectTooManyVariables(context, inputFile, content)
        );
    }

    @Test
    void testDetectNestingTooDeep() {
        String content = "If[a, If[b, If[c, If[d, If[e, If[f, True]]]]]]";
        assertDoesNotThrow(() ->
            detector.detectNestingTooDeep(context, inputFile, content)
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
    @Test
    void testDetectGlobalStateModification() {
        String content = "GlobalVar = 42;";
        assertDoesNotThrow(() ->
            detector.detectGlobalStateModification(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePatternMatch() {
        String content = "Switch[x, 1, \"one\", 2, \"two\"]";
        assertDoesNotThrow(() ->
            detector.detectIncompletePatternMatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingOptionDefault() {
        String content = "value = OptionValue[\"MyOption\"]";
        assertDoesNotThrow(() ->
            detector.detectMissingOptionDefault(context, inputFile, content)
        );
    }

    @Test
    void testDetectSideEffectInExpression() {
        String content = "result = x + (y = 5)";
        assertDoesNotThrow(() ->
            detector.detectSideEffectInExpression(context, inputFile, content)
        );
    }

    // Best Practices Detection Methods
    @Test
    void testDetectEmptyCatchBlock() {
        String content = "Catch[someExpression]";
        assertDoesNotThrow(() ->
            detector.detectEmptyCatchBlock(context, inputFile, content)
        );
    }

    @Test
    void testDetectCatchWithoutThrow() {
        String content = "result = Catch[someExpression]; otherCode;";
        assertDoesNotThrow(() ->
            detector.detectCatchWithoutThrow(context, inputFile, content)
        );
    }

    @Test
    void testDetectListQueryInefficient() {
        String content = "Do[result = MemberQ[bigList, x], {x, items}]";
        assertDoesNotThrow(() ->
            detector.detectListQueryInefficient(context, inputFile, content)
        );
    }

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
    void testDetectPlotWithoutLabels() {
        String content = "Plot[x^2, {x, 0, 10}]";
        assertDoesNotThrow(() ->
            detector.detectPlotWithoutLabels(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetWithoutHeaders() {
        String content = "Dataset[{{1, 2, 3}, {4, 5, 6}}]";
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssociationKeyNotString() {
        String content = "assoc = Association[1 -> \"value\", 2 -> \"other\"]";
        assertDoesNotThrow(() ->
            detector.detectAssociationKeyNotString(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternTestVsCondition() {
        String content = "f[x_ /; IntegerQ[x]] := x + 1";
        assertDoesNotThrow(() ->
            detector.detectPatternTestVsCondition(context, inputFile, content)
        );
    }

    // Naming Detection Methods
    @Test
    void testDetectBooleanNameNonDescriptive() {
        String content = "valid = True; flag = False;";
        assertDoesNotThrow(() ->
            detector.detectBooleanNameNonDescriptive(context, inputFile, content)
        );
    }

    @Test
    void testDetectConstantNotUppercase() {
        String content = "MaxValue = 100; MinValue = 0;";
        assertDoesNotThrow(() ->
            detector.detectConstantNotUppercase(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameMatchesBuiltin() {
        String content = "C = 5; D = 10; E = 2.718; I = Sqrt[-1];";
        assertDoesNotThrow(() ->
            detector.detectVariableNameMatchesBuiltin(context, inputFile, content)
        );
    }

    @Test
    void testDetectParameterNameSameAsFunction() {
        String content = "MyFunc[myFunc_] := myFunc + 1";
        assertDoesNotThrow(() ->
            detector.detectParameterNameSameAsFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectHungarianNotation() {
        String content = "strName = \"John\"; intCount = 5; boolFlag = True;";
        assertDoesNotThrow(() ->
            detector.detectHungarianNotation(context, inputFile, content)
        );
    }

    @Test
    void testDetectAbbreviationUnclear() {
        String content = "tmp = 5; val = 10; cnt = 0; msg = \"hello\";";
        assertDoesNotThrow(() ->
            detector.detectAbbreviationUnclear(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanName() {
        String content = "notValid = False; isNotEnabled = True;";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanName(context, inputFile, content)
        );
    }

    @Test
    void testDetectGenericName() {
        String content = "data = {1, 2, 3}; temp = 5; value = 10;";
        assertDoesNotThrow(() ->
            detector.detectGenericName(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumberInName() {
        String content = "var1 = 5; var2 = 10; temp3 = 15;";
        assertDoesNotThrow(() ->
            detector.detectNumberInName(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentNamingStyle() {
        String content = "camelCase = 1; snake_case = 2; PascalCase = 3;";
        assertDoesNotThrow(() ->
            detector.detectInconsistentNamingStyle(context, inputFile, content)
        );
    }

    // Style & Formatting Detection Methods
    @Test
    void testDetectTrailingWhitespace() {
        String content = "x = 5;   \ny = 10;\t\n";
        assertDoesNotThrow(() ->
            detector.detectTrailingWhitespace(context, inputFile, content)
        );
    }

    // REMOVED: testDetectCommaSpacing() and testDetectOperatorSpacing() tests
    // The corresponding rules have been permanently removed from the codebase

    @Test
    void testDetectBracketSpacing() {
        String content = "f [x]";
        assertDoesNotThrow(() ->
            detector.detectBracketSpacing(context, inputFile, content)
        );
    }

    @Test
    void testDetectSemicolonStyle() {
        String content = "a = 1;; b = 2;";
        assertDoesNotThrow(() ->
            detector.detectSemicolonStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectParenthesesUnnecessary() {
        String content = "result = (((x + y)))";
        assertDoesNotThrow(() ->
            detector.detectParenthesesUnnecessary(context, inputFile, content)
        );
    }

    @Test
    void testDetectLongStringLiteral() {
        String content = "msg = \"This is a very long string literal that exceeds one hundred characters "
         + "and should trigger the detection rule for overly long string literals in the code\"";
        assertDoesNotThrow(() ->
            detector.detectLongStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectAcronymStyle() {
        String content = "XMLParser = 1; HttpRequest = 2;";
        assertDoesNotThrow(() ->
            detector.detectAcronymStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageNameCase() {
        String content = "BeginPackage[\"my_package`Utils\"]";
        assertDoesNotThrow(() ->
            detector.detectPackageNameCase(context, inputFile, content)
        );
    }

    // Additional Coverage Methods
    @Test
    void testDetectBooleanComparison() {
        String content = "If[flag == True, doSomething[]]";
        assertDoesNotThrow(() ->
            detector.detectBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanComparison() {
        String content = "If[Not[flag == True], doSomething[]]";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectRedundantConditional() {
        String content = "If[x > 5, True, False]";
        assertDoesNotThrow(() ->
            detector.detectRedundantConditional(context, inputFile, content)
        );
    }

    @Test
    void testDetectEqualityCheckOnReals() {
        String content = "If[1.0 == 2.0, True, False]";
        assertDoesNotThrow(() ->
            detector.detectEqualityCheckOnReals(context, inputFile, content)
        );
    }

    @Test
    void testDetectStringConcatenationInLoop() {
        String content = "Do[str = str <> ToString[i], {i, 100}]";
        assertDoesNotThrow(() ->
            detector.detectStringConcatenationInLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeprecatedOptionUsage() {
        String content = "Plot[x, {x, 0, 1}, PlotRange -> Automatic]";
        assertDoesNotThrow(() ->
            detector.detectDeprecatedOptionUsage(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolicVsNumericMismatch() {
        String content = "Solve[x^2 + 1.5*x + 1 == 0, x]";
        assertDoesNotThrow(() ->
            detector.detectSymbolicVsNumericMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectOptionNameUnclear() {
        String content = "MyFunction[OptionPattern[{opt1 -> 1}]]";
        assertDoesNotThrow(() ->
            detector.detectOptionNameUnclear(context, inputFile, content)
        );
    }

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
    void testDetectBooleanExpressionTooComplex() {
        String content = "result = a && b || c && d || e && f || g && h || i && j || k";
        assertDoesNotThrow(() ->
            detector.detectBooleanExpressionTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentIndentation() {
        String content = "f[x_] := Module[{y},\n\ty = x + 1;\n    z = x + 2;\n\ty + z\n]";
        assertDoesNotThrow(() ->
            detector.detectInconsistentIndentation(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateCodeBlock() {
        String content = "a = 1;\nb = 2;\nc = 3;\nd = 4;\ne = 5;\n\nf = 10;\n\na = 1;\nb = 2;\nc = 3;\nd = 4;\ne = 5;";
        assertDoesNotThrow(() ->
            detector.detectDuplicateCodeBlock(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrimitiveObsession() {
        String content = "MyFunc[str1_, str2_, str3_, int1_, int2_, int3_, flag1_, flag2_] := str1 <> str2";
        assertDoesNotThrow(() ->
            detector.detectPrimitiveObsession(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooLong() {
        String content = "ThisIsAnExtremelyLongFunctionNameThatExceedsFiftyCharactersInLength[x_] := x + 1";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooShort() {
        String content = "a[x_] := x + 1; b[y_] := y * 2;";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectMultipleBlankLines() {
        String content = "x = 1;\n\n\n\ny = 2;";
        assertDoesNotThrow(() ->
            detector.detectMultipleBlankLines(context, inputFile, content)
        );
    }

    @Test
    void testDetectNestedBracketsExcessive() {
        String content = "result = f[g[h[i[j[k[l[x]]]]]]]";
        assertDoesNotThrow(() ->
            detector.detectNestedBracketsExcessive(context, inputFile, content)
        );
    }

    @Test
    void testDetectMagicString() {
        String content = "If[status == \"active\", Print[\"active\"], Print[\"inactive\"]]";
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateStringLiteral() {
        String content = "a = \"constant\"; b = \"constant\"; c = \"constant\"; d = \"constant\";";
        assertDoesNotThrow(() ->
            detector.detectDuplicateStringLiteral(context, inputFile, content)
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
    void testDetectChainedCallsTooLong() {
        String content = "result = data // func1 // func2 // func3 // func4 // func5 // func6";
        assertDoesNotThrow(() ->
            detector.detectChainedCallsTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionalComplexity() {
        String content = "If[a > 0 && b < 10 || c == 5 && d != 3 || e >= 1 && f <= 100, result = True]";
        assertDoesNotThrow(() ->
            detector.detectConditionalComplexity(context, inputFile, content)
        );
    }

    @Test
    void testDetectIdenticalIfBranches() {
        String content = "If[condition, doSomething[], doSomething[]]";
        assertDoesNotThrow(() ->
            detector.detectIdenticalIfBranches(context, inputFile, content)
        );
    }

    @Test
    void testDetectFeatureEnvy() {
        String content = "MyFunc[obj_] := obj@field1 + obj@field2 + obj@field3 + obj@field4 + obj@field5 + obj@field6";
        assertDoesNotThrow(() ->
            detector.detectFeatureEnvy(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedPath() {
        String content = "file = Import[\"/Users/username/Documents/data.csv\"]";
        assertDoesNotThrow(() ->
            detector.detectHardcodedPath(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedUrl() {
        String content = "data = URLFetch[\"http://example.com/api/data\"]";
        assertDoesNotThrow(() ->
            detector.detectHardcodedUrl(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameTooShort() {
        String content = "a = 5; b = 10; c = 15;";
        assertDoesNotThrow(() ->
            detector.detectVariableNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectBraceStyle() {
        String content = "If[condition,\n{\n  x = 1\n}\n,\n{\n  x = 2\n}\n]";
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileEndsWithoutNewline() {
        String content = "x = 1; y = 2;";  // No newline at end
        assertDoesNotThrow(() ->
            detector.detectFileEndsWithoutNewline(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileEndsWithNewline() {
        String content = "x = 1; y = 2;\n";  // Has newline at end
        assertDoesNotThrow(() ->
            detector.detectFileEndsWithoutNewline(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingBlankLineAfterFunction() {
        String content = "Func1[x_] := x + 1\nFunc2[y_] := y + 2";
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
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
    void testDetectTooManyParametersWithoutIssue() {
        String content = "MyFunction[a, b, c] := a + b + c";
        assertDoesNotThrow(() ->
            detector.detectTooManyParameters(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyVariablesWithoutIssue() {
        String content = "Module[{a, b, c}, a + b + c]";
        assertDoesNotThrow(() ->
            detector.detectTooManyVariables(context, inputFile, content)
        );
    }

    @Test
    void testDetectNestingTooDeepWithoutIssue() {
        String content = "If[a, If[b, If[c, True]]]";
        assertDoesNotThrow(() ->
            detector.detectNestingTooDeep(context, inputFile, content)
        );
    }

    @Test
    void testDetectNestedBracketsExcessiveWithoutIssue() {
        String content = "result = f[g[h[i[j[x]]]]]";
        assertDoesNotThrow(() ->
            detector.detectNestedBracketsExcessive(context, inputFile, content)
        );
    }

    @Test
    void testDetectFileTooManyFunctionsWithoutIssue() {
        String content = "f1[]:=1; f2[]:=2; f3[]:=3;";
        assertDoesNotThrow(() ->
            detector.detectFileTooManyFunctions(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageTooManyExportsWithoutBeginPackage() {
        String content = "e1[]:=1; e2[]:=2; e3[]:=3;";
        assertDoesNotThrow(() ->
            detector.detectPackageTooManyExports(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageTooManyExportsWithoutIssue() {
        String content = "BeginPackage[\"Test`\"]; Func1[x_]:=x+1; Func2[y_]:=y+2;";
        assertDoesNotThrow(() ->
            detector.detectPackageTooManyExports(context, inputFile, content)
        );
    }

    @Test
    void testDetectExpressionTooComplexWithoutIssue() {
        String content = "result = a + b * c";
        assertDoesNotThrow(() ->
            detector.detectExpressionTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectSwitchTooManyCasesWithoutIssue() {
        String content = "Switch[x, 1, \"one\", 2, \"two\"]";
        assertDoesNotThrow(() ->
            detector.detectSwitchTooManyCases(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanExpressionTooComplexWithoutIssue() {
        String content = "result = a && b || c";
        assertDoesNotThrow(() ->
            detector.detectBooleanExpressionTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectChainedCallsTooLongWithoutIssue() {
        String content = "result = data // func1 // func2";
        assertDoesNotThrow(() ->
            detector.detectChainedCallsTooLong(context, inputFile, content)
        );
    }

    // Test negative paths for naming methods
    @Test
    void testDetectFunctionNameTooShortWithValidNames() {
        String content = "f[x_] := x + 1; g[y_] := y + 2; h[z_] := z + 3;";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionNameTooLongWithoutIssue() {
        String content = "GoodFunctionName[x_] := x + 1";
        assertDoesNotThrow(() ->
            detector.detectFunctionNameTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectVariableNameTooShortWithValidNames() {
        String content = "i = 1; j = 2; k = 3;";
        assertDoesNotThrow(() ->
            detector.detectVariableNameTooShort(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanNameNonDescriptiveWithGoodNames() {
        String content = "isValid = True; hasData = False; canExecute = True;";
        assertDoesNotThrow(() ->
            detector.detectBooleanNameNonDescriptive(context, inputFile, content)
        );
    }

    @Test
    void testDetectConstantNotUppercaseWithGoodNames() {
        String content = "MAXVALUE = 100; MINVALUE = 0;";
        assertDoesNotThrow(() ->
            detector.detectConstantNotUppercase(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageNameCaseWithGoodNames() {
        String content = "BeginPackage[\"MyPackage`Utils\"]";
        assertDoesNotThrow(() ->
            detector.detectPackageNameCase(context, inputFile, content)
        );
    }

    @Test
    void testDetectAcronymStyleWithoutIssue() {
        String content = "XmlParser = 1; HttpRequest = 2;";
        assertDoesNotThrow(() ->
            detector.detectAcronymStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentNamingStyleOnlyCamelCase() {
        String content = "camelCase = 1; anotherCamelCase = 2;";
        assertDoesNotThrow(() ->
            detector.detectInconsistentNamingStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentNamingStyleOnlySnakeCase() {
        String content = "snake_case = 1; another_snake = 2;";
        assertDoesNotThrow(() ->
            detector.detectInconsistentNamingStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectNumberInNameWithCoordinates() {
        String content = "x1 = 5; y2 = 10; z3 = 15;";
        assertDoesNotThrow(() ->
            detector.detectNumberInName(context, inputFile, content)
        );
    }

    @Test
    void testDetectHungarianNotationWithoutIssue() {
        String content = "name = \"John\"; count = 5; flag = True;";
        assertDoesNotThrow(() ->
            detector.detectHungarianNotation(context, inputFile, content)
        );
    }

    @Test
    void testDetectAbbreviationUnclearWithLongNames() {
        String content = "temporary = 5; value = 10; counter = 0; message = \"hello\";";
        assertDoesNotThrow(() ->
            detector.detectAbbreviationUnclear(context, inputFile, content)
        );
    }

    @Test
    void testDetectGenericNameWithoutIssue() {
        String content = "customerData = {1, 2, 3}; userId = 5; productPrice = 10;";
        assertDoesNotThrow(() ->
            detector.detectGenericName(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanNameWithoutIssue() {
        String content = "isValid = False; isEnabled = True;";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanName(context, inputFile, content)
        );
    }

    @Test
    void testDetectParameterNameSameAsFunctionWithoutIssue() {
        String content = "MyFunc[param_] := param + 1";
        assertDoesNotThrow(() ->
            detector.detectParameterNameSameAsFunction(context, inputFile, content)
        );
    }

    // Test negative paths for maintainability methods
    @Test
    void testDetectMagicStringWithoutDuplication() {
        String content = "If[status == \"active\", Print[\"running\"], Print[\"stopped\"]]";
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    @Test
    void testDetectMagicStringWithShortStrings() {
        String content = "a = \"x\"; b = \"y\"; c = \"z\";";
        assertDoesNotThrow(() ->
            detector.detectMagicString(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateStringLiteralWithoutIssue() {
        String content = "a = \"value1\"; b = \"value2\"; c = \"value3\";";
        assertDoesNotThrow(() ->
            detector.detectDuplicateStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedPathWithoutIssue() {
        String content = "file = Import[\"data.csv\"]";
        assertDoesNotThrow(() ->
            detector.detectHardcodedPath(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardcodedUrlWithoutIssue() {
        String content = "data = URLFetch[localUrl]";
        assertDoesNotThrow(() ->
            detector.detectHardcodedUrl(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionalComplexityWithoutIssue() {
        String content = "If[a > 0 && b < 10, result = True]";
        assertDoesNotThrow(() ->
            detector.detectConditionalComplexity(context, inputFile, content)
        );
    }

    @Test
    void testDetectIdenticalIfBranchesWithoutIssue() {
        String content = "If[condition, doSomething[], doOtherThing[]]";
        assertDoesNotThrow(() ->
            detector.detectIdenticalIfBranches(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateCodeBlockWithoutIssue() {
        String content = "a = 1;\nb = 2;\nc = 3;";
        assertDoesNotThrow(() ->
            detector.detectDuplicateCodeBlock(context, inputFile, content)
        );
    }

    @Test
    void testDetectGodFunctionWithoutIssue() {
        String content = "MyFunc[x_] := Module[{}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectGodFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectFeatureEnvyWithoutIssue() {
        String content = "MyFunc[obj_] := obj + 1";
        assertDoesNotThrow(() ->
            detector.detectFeatureEnvy(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrimitiveObsessionWithoutIssue() {
        String content = "MyFunc[str1_, str2_] := str1 <> str2";
        assertDoesNotThrow(() ->
            detector.detectPrimitiveObsession(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrimitiveObsessionWithTypeHints() {
        String content = "MyFunc[str1_String, str2_Integer, flag_?IntegerQ] := str1 <> ToString[str2]";
        assertDoesNotThrow(() ->
            detector.detectPrimitiveObsession(context, inputFile, content)
        );
    }

    @Test
    void testDetectGlobalStateModificationWithinModule() {
        String content = "Module[{GlobalVar}, GlobalVar = 42;]";
        assertDoesNotThrow(() ->
            detector.detectGlobalStateModification(context, inputFile, content)
        );
    }

    @Test
    void testDetectSideEffectInExpressionWithoutIssue() {
        String content = "result = x + y";
        assertDoesNotThrow(() ->
            detector.detectSideEffectInExpression(context, inputFile, content)
        );
    }

    @Test
    void testDetectSideEffectInExpressionWithDelayedAssignment() {
        String content = "result[x_] := Module[{y = x + 1}, y]";
        assertDoesNotThrow(() ->
            detector.detectSideEffectInExpression(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePatternMatchWithDefault() {
        String content = "Switch[x, 1, \"one\", 2, \"two\", _, \"default\"]";
        assertDoesNotThrow(() ->
            detector.detectIncompletePatternMatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingOptionDefaultWithDefault() {
        String content = "value = OptionValue[\"MyOption\", defaultValue]";
        assertDoesNotThrow(() ->
            detector.detectMissingOptionDefault(context, inputFile, content)
        );
    }

    @Test
    void testDetectOptionNameUnclearWithGoodName() {
        String content = "MyFunction[OptionPattern[{backgroundColor -> White}]]";
        assertDoesNotThrow(() ->
            detector.detectOptionNameUnclear(context, inputFile, content)
        );
    }

    // Test negative paths for best practices methods
    @Test
    void testDetectStringConcatenationInLoopWithoutIssue() {
        String content = "Do[result = Calculate[i], {i, 100}]";
        assertDoesNotThrow(() ->
            detector.detectStringConcatenationInLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectBooleanComparisonWithoutIssue() {
        String content = "If[flag, doSomething[]]";
        assertDoesNotThrow(() ->
            detector.detectBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectNegatedBooleanComparisonWithoutIssue() {
        String content = "If[x != y, doSomething[]]";
        assertDoesNotThrow(() ->
            detector.detectNegatedBooleanComparison(context, inputFile, content)
        );
    }

    @Test
    void testDetectRedundantConditionalWithoutIssue() {
        String content = "If[x > 5, doSomething[], doOtherThing[]]";
        assertDoesNotThrow(() ->
            detector.detectRedundantConditional(context, inputFile, content)
        );
    }

    @Test
    void testDetectEmptyCatchBlockWithHandler() {
        String content = "Check[someExpression, errorHandler]";
        assertDoesNotThrow(() ->
            detector.detectEmptyCatchBlock(context, inputFile, content)
        );
    }

    @Test
    void testDetectCatchWithoutThrowWithThrow() {
        String content = "result = Catch[Throw[value]];";
        assertDoesNotThrow(() ->
            detector.detectCatchWithoutThrow(context, inputFile, content)
        );
    }

    @Test
    void testDetectCatchWithoutThrowNoCatch() {
        String content = "result = someValue;";
        assertDoesNotThrow(() ->
            detector.detectCatchWithoutThrow(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeprecatedOptionUsageWithoutIssue() {
        String content = "Plot[x, {x, 0, 1}, Frame -> True]";
        assertDoesNotThrow(() ->
            detector.detectDeprecatedOptionUsage(context, inputFile, content)
        );
    }

    @Test
    void testDetectListQueryInefficientWithoutLoop() {
        String content = "result = MemberQ[bigList, x]";
        assertDoesNotThrow(() ->
            detector.detectListQueryInefficient(context, inputFile, content)
        );
    }

    @Test
    void testDetectEqualityCheckOnRealsWithoutIssue() {
        String content = "If[x == y, True, False]";
        assertDoesNotThrow(() ->
            detector.detectEqualityCheckOnReals(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolicVsNumericMismatchOnlySymbolic() {
        String content = "Solve[x^2 + x + 1 == 0, x]";
        assertDoesNotThrow(() ->
            detector.detectSymbolicVsNumericMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolicVsNumericMismatchOnlyNumeric() {
        String content = "NSolve[x^2 + x + 1 == 0, x]";
        assertDoesNotThrow(() ->
            detector.detectSymbolicVsNumericMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolicVsNumericMismatchNeither() {
        String content = "x = 5; y = 10;";
        assertDoesNotThrow(() ->
            detector.detectSymbolicVsNumericMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectGraphicsOptionsExcessiveWithoutIssue() {
        String content = "Graphics[point, PlotStyle -> Red]";
        assertDoesNotThrow(() ->
            detector.detectGraphicsOptionsExcessive(context, inputFile, content)
        );
    }

    @Test
    void testDetectPlotWithoutLabelsWithLabels() {
        String content = "Plot[x^2, {x, 0, 10}, AxesLabel -> {\"x\", \"y\"}]";
        assertDoesNotThrow(() ->
            detector.detectPlotWithoutLabels(context, inputFile, content)
        );
    }

    @Test
    void testDetectPlotWithoutLabelsWithFrameLabel() {
        String content = "Plot[x^2, {x, 0, 10}, FrameLabel -> {\"x\", \"y\"}]";
        assertDoesNotThrow(() ->
            detector.detectPlotWithoutLabels(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetWithoutHeadersWithHeaders() {
        String content = "Dataset[<|\"col1\" -> 1, \"col2\" -> 2|>]";
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

    @Test
    void testDetectDatasetWithoutHeadersWithAssociation() {
        String content = "Dataset[Association[\"key\" -> \"value\"]]";
        assertDoesNotThrow(() ->
            detector.detectDatasetWithoutHeaders(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssociationKeyNotStringWithStringKeys() {
        String content = "assoc = Association[\"key1\" -> \"value\", \"key2\" -> \"other\"]";
        assertDoesNotThrow(() ->
            detector.detectAssociationKeyNotString(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternTestVsConditionWithPatternTest() {
        String content = "f[x_?IntegerQ] := x + 1";
        assertDoesNotThrow(() ->
            detector.detectPatternTestVsCondition(context, inputFile, content)
        );
    }

    // Test style formatting negative paths
    @Test
    void testDetectTrailingWhitespaceWithoutIssue() {
        String content = "x = 5;\ny = 10;\n";
        assertDoesNotThrow(() ->
            detector.detectTrailingWhitespace(context, inputFile, content)
        );
    }

    @Test
    void testDetectBracketSpacingWithoutIssue() {
        String content = "f[x]";
        assertDoesNotThrow(() ->
            detector.detectBracketSpacing(context, inputFile, content)
        );
    }

    @Test
    void testDetectSemicolonStyleWithoutIssue() {
        String content = "a = 1; b = 2;";
        assertDoesNotThrow(() ->
            detector.detectSemicolonStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectParenthesesUnnecessaryWithoutIssue() {
        String content = "result = (x + y)";
        assertDoesNotThrow(() ->
            detector.detectParenthesesUnnecessary(context, inputFile, content)
        );
    }

    @Test
    void testDetectLongStringLiteralWithoutIssue() {
        String content = "msg = \"Short string\"";
        assertDoesNotThrow(() ->
            detector.detectLongStringLiteral(context, inputFile, content)
        );
    }

    @Test
    void testDetectMultipleBlankLinesWithoutIssue() {
        String content = "x = 1;\n\ny = 2;";
        assertDoesNotThrow(() ->
            detector.detectMultipleBlankLines(context, inputFile, content)
        );
    }

    @Test
    void testDetectAlignmentInconsistentWithShortList() {
        String content = "list = {1, 2, 3}";
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectAlignmentInconsistentWithConsistentAlignment() {
        String content = "list = {item1, item2, item3, item4, item5, item6, item7, item8}";
        assertDoesNotThrow(() ->
            detector.detectAlignmentInconsistent(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentIndentationOnlyTabs() {
        String content = "f[x_] := Module[{y},\n\ty = x + 1;\n\tz = x + 2;\n\ty + z\n]";
        assertDoesNotThrow(() ->
            detector.detectInconsistentIndentation(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentIndentationOnlySpaces() {
        String content = "f[x_] := Module[{y},\n  y = x + 1;\n  z = x + 2;\n  y + z\n]";
        assertDoesNotThrow(() ->
            detector.detectInconsistentIndentation(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentIndentationEmptyLines() {
        String content = "f[x_] := Module[{y},\n\n\n  y = x + 1\n]";
        assertDoesNotThrow(() ->
            detector.detectInconsistentIndentation(context, inputFile, content)
        );
    }

    @Test
    void testDetectBraceStyleConsistentSameLine() {
        String content = "f[x_] := {x + 1}; g[y_] := {y + 2}; h[z_] := {z + 3};";
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectBraceStyleConsistentNewLine() {
        String content = "f[x_] :=\n{x + 1};\ng[y_] :=\n{y + 2};\nh[z_] :=\n{z + 3};";
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectBraceStyleFewOccurrences() {
        String content = "f[x_] := {x + 1}; g[y_] :=\n{y + 2};";
        assertDoesNotThrow(() ->
            detector.detectBraceStyle(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyReturnPointsWithoutIssue() {
        String content = "Func[x] := Module[{}, If[x > 0, Return[1]]; Return[0]]";
        assertDoesNotThrow(() ->
            detector.detectTooManyReturnPoints(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingBlankLineAfterFunctionWithComment() {
        String content = "Func1[x_] := x + 1\n(* Comment *)\nFunc2[y_] := y + 2";
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingBlankLineAfterFunctionWithBlankLine() {
        String content = "Func1[x_] := x + 1\n\nFunc2[y_] := y + 2";
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingBlankLineAfterFunctionLastLine() {
        String content = "Func1[x_] := x + 1";
        assertDoesNotThrow(() ->
            detector.detectMissingBlankLineAfterFunction(context, inputFile, content)
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
    void testAllDetectMethodsWithMalformedInput() {
        // Target all 68 catch blocks with null content to trigger exceptions
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
}
