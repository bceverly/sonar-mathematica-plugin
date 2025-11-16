package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CodingStandardDetector (32 coding standard rules).
 */
class CodingStandardDetectorTest {

    private CodingStandardDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new CodingStandardDetector();
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

        // Set sensor (required for BaseDetector)
        MathematicaRulesSensor mockSensor = mock(MathematicaRulesSensor.class);
        detector.setSensor(mockSensor);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codingStandardTestCases")
    void testCodingStandards(String testName, String code) {
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    private static Stream<Arguments> codingStandardTestCases() {
        return Stream.concat(
            codingStandardTestCasesPart1(),
            codingStandardTestCasesPart2()
        );
    }

    private static Stream<Arguments> codingStandardTestCasesPart1() {
        return Stream.concat(
            codingStandardTestCasesPart1a(),
            codingStandardTestCasesPart1b()
        );
    }

    private static Stream<Arguments> codingStandardTestCasesPart1a() {
        return Stream.of(
                // Non-compliant: space before [
                Arguments.of("bracketSpacingBefore", "function [x_] := x + 1\n"),
                // Non-compliant: assignment in Module def
                Arguments.of("variableAssignmentInModuleDef", "Module[{x = 1}, x + 2]\n"),
                Arguments.of("complexBooleanShorthand", "If[a > 0 &&\n  b > 0 ||\n  c > 0, True, False]\n"),
                // Non-compliant: /@ with newline
                Arguments.of("multilineMapShorthand", "func /@ \n  list\n"),
                // Non-compliant: := for message
                Arguments.of("errorMessageDelayed", "myFunc::error := \"Error message\"\n"),
                // Non-compliant: /; in definition
                Arguments.of("conditionalFunctionDef", "func[x_] := x + 1 /; x > 0\n"),
                // Non-compliant: dereferencing
                Arguments.of("dereferencingSyntax", "result = $PersonID[Name]\n"),
                // Non-compliant: in-place modification
                Arguments.of("inPlaceModification", "AppendTo[list, newItem]\n"),
                // Warning: Composition
                Arguments.of("nonLinearEvaluation", "result = Composition[f, g, h][x]\n"),
                // Non-compliant: >3 words
                Arguments.of("longVariableName", "currentActiveUserProfileData = getData[]\n"),
                // Non-compliant: >3 words
                Arguments.of("longFunctionName", "GetCurrentActiveUserProfileData[id_] := id\n"),
                // Non-compliant: Do is litter word
                Arguments.of("litterWords", "DoCalculation[x_] := x + 1\n"),
                // CRITICAL: TimeConstrained
                Arguments.of("timeConstrained", "result = TimeConstrained[expr, 10]\n"),
                // Non-compliant: should be UserP
                Arguments.of("patternNoPSuffix", "User = _String | _Integer\n"),
                // Should be ValidUserQ
                Arguments.of("patternTestNoQSuffix", "ValidUser[x_] := StringQ[x] && StringLength[x] > 0\n"),
                // Non-compliant: strings not symbols
                Arguments.of("enumeratedStringPattern", "ColorP = \"Red\" | \"Green\" | \"Blue\"\n"),
                // Compliant: no space before [
                Arguments.of("compliantBracketSpacing", "function[x_] := x + 1\n"),
                // Compliant: assignment in body
                Arguments.of("compliantModuleDefinition", "Module[{x}, x = 1; x + 2]\n"),
                // Compliant: = for message
                Arguments.of("compliantErrorMessage", "myFunc::error = \"Error message\"\n"),
                // Compliant: ends with P
                Arguments.of("compliantPatternName", "UserP = _String | _Integer\n"),
                // Compliant: ends with Q
                Arguments.of("compliantPatternTestName", "ValidUserQ[x_] := StringQ[x]\n"),
                // Compliant: symbols not strings
                Arguments.of("compliantEnumeratedPattern", "ColorP = Red | Green | Blue\n"),
                Arguments.of("emptyContent", ""),
                Arguments.of("whitespaceOnlyContent", "   \n\n\t\t  \n  "),
                Arguments.of("contentWithOnlyComments", "(* This is a comment *)\n(* Another comment *)"),
                // Space after [
                Arguments.of("bracketSpacingAfter", "function[ x_] := x + 1\n"),
                // Spaces on both sides
                Arguments.of("bracketSpacingBothSides", "function [ x_ ] := x + 1\n"),
                // Multiple violations
                Arguments.of("multipleViolationsInOneLine", "function [x = 1] := x + 1 /; x > 0\n"),
                // Assignment in Block def
                Arguments.of("blockAssignmentInDef", "Block[{x = 1}, x + 2]\n"),
                // Assignment in With def (allowed)
                Arguments.of("withAssignmentInDef", "With[{x = 1}, x + 2]\n"),
                Arguments.of("nestedModuleWithAssignment", "Module[{x = 1}, Module[{y = 2}, x + y]]\n"),
                Arguments.of("complexBooleanWithParens", "If[(a > 0 && b > 0) || (c > 0 && d > 0), True, False]\n"),
                // @@ is Apply
                Arguments.of("applyShorthand", "func @@ list\n"),
                // @@ with newline
                Arguments.of("multilineApplyShorthand", "func @@ \n  list\n"),
                // := for usage
                Arguments.of("usageMessageDelayed", "myFunc::usage := \"Usage message\"\n"),
                Arguments.of("multipleErrorMessages", "func::error1 = \"Error 1\"\nfunc::error2 := \"Error 2\"\n"),
                // /; in parameter (allowed)
                Arguments.of("conditionalInFunction", "func[x_ /; x > 0] := x + 1\n"),
                Arguments.of("dereferencingWithContext", "result = MyContext`$PersonID[Name]\n"),
                // In-place modification
                Arguments.of("prependTo", "PrependTo[list, newItem]\n"),
                // In-place modification
                Arguments.of("addTo", "x += 1\n"),
                // In-place modification
                Arguments.of("multiplyBy", "x *= 2\n"),
                Arguments.of("operatorForm", "result = OperatorApplied[f, 2][x, y]\n"),
                Arguments.of("rightComposition", "result = RightComposition[f, g, h][x]\n")
        );
    }

    private static Stream<Arguments> codingStandardTestCasesPart1b() {
        return Stream.of(
                Arguments.of("veryLongVariableName", "thisIsAnExtremelyLongVariableNameWithMoreThanThreeWordsInIt = 1\n"),
                Arguments.of("veryLongFunctionName", "GetCurrentActiveUserProfileDataFromDatabaseWithCaching[id_] := id\n"),
                // Multiple litter words
                Arguments.of("multipleLitterWords", "DoPerformCalculateProcess[x_] := x\n"),
                Arguments.of("memoryConstrained", "result = MemoryConstrained[expr, 1000000]\n"),
                Arguments.of("checkAbort", "result = CheckAbort[riskyExpr, defaultValue]\n"),
                // P suffix but strings
                Arguments.of("patternWithPSuffixButStrings", "UserP = \"admin\" | \"user\" | \"guest\"\n"),
                Arguments.of("patternTestComplexBody", "ValidUserQ[x_] := StringQ[x] && StringLength[x] > 3 && StringLength[x] < 20\n"),
                // Mix of symbols and strings
                Arguments.of("enumeratedMixedPattern", "MixedP = Red | \"Green\" | Blue\n"),
                Arguments.of("complexNestedCode", "Module[{x = 1}, \n"
                        + "  Block[{y = 2}, \n"
                        + "    With[{z = 3}, \n"
                        + "      DoPerformCalculation[x, y, z] := \n"
                        + "        TimeConstrained[\n"
                        + "          Composition[f, g][x + y + z] /; x > 0,\n"
                        + "          10\n"
                        + "        ]\n"
                        + "    ]\n"
                        + "  ]\n"
                        + "]\n"),
                Arguments.of("allRulesWithNoViolations", "myFunc[x_] := x + 1\n"
                        + "UserP = _String\n"
                        + "ValidQ[x_] := StringQ[x]\n"
                        + "ColorP = Red | Green | Blue\n"),
                Arguments.of("codeInStringsNotDetected", "message = \"Do not use TimeConstrained[expr] or AppendTo[list]\"\n"
                        + "normalFunc[x_] := x + 1\n"),
                Arguments.of("codeInCommentsNotDetected", "(* func [x_] := TimeConstrained[x] *)\n"
                        + "normalFunc[x_] := x + 1\n"),
                Arguments.of("noEmptyLineBetweenCodeSections", "result1 = func1[];\nresult2 = func2[];"),
                Arguments.of("noEmptyLineWithFunctionCall", "Process[data];\nTransform[result];"),
                Arguments.of("abbreviatedVariableShort", "x = 5;\ny = 10;"),
                Arguments.of("abbreviatedVariableConsonantCluster", "strng = \"hello\";\nmprt = Import[file];"),
                Arguments.of("abbreviatedVariableCamelCase", "xPos = 10;\nyVal = 20;"),
                Arguments.of("abbreviatedVariableStandardOps", "ops = {};\nqual = True;"),
                Arguments.of("customAssociationAsInput", "ProcessData[data_] := Module[{}, data[\"key\"]]\nMyFunc[<|\"x\" -> 1|>] := x"),
                Arguments.of("customAssociationListOfRules", "ConfigFunc[{a -> 1, b -> 2}] := Process[a, b]"),
                Arguments.of("globalNoDollarWithUppercase", "CONSTANT_VALUE = 100\nANOTHER_CONST = 200"),
                Arguments.of("globalNoDollarFunctionDefinition", "MyFunction[x_] := x + 1"),
                Arguments.of("litterWordsMake", "MakeData[x_] := x * 2"),
                Arguments.of("litterWordsGet", "GetValue[key_] := lookup[key]"),
                Arguments.of("litterWordsAnd", "ProcessAndSave[data_] := Save[Process[data]]"),
                Arguments.of("longPureFunctionMultiline", "Map[Function[x,\n  result = x^2;\n  result + 1\n] &, list]"),
                Arguments.of("patternNoPSuffixShortName", "X = _Integer"),
                Arguments.of("patternNoPSuffixProperEnding", "UserP = _String\nNumberP = _Integer"),
                Arguments.of("patternTestComplexWithMatchQ", "ValidData[x_] := MatchQ[x, {_Integer ..}] && Length[x] > 0"),
                Arguments.of("enumeratedPatternManyStrings", "StatusP = \"active\" | \"inactive\" | \"pending\" | \"completed\" | \"cancelled\""),
                Arguments.of("multipleBracketSpacingIssues", "func1 [x_] := x\nfunc2 [y_] := y\nfunc3 [z_] := z"),
                Arguments.of("multipleErrorMessagesDelayed", "func1::error1 := \"Error 1\"\nfunc2::error2 := \"Error 2\""),
                Arguments.of("multipleConditionalFunctionDefs", "f[x_] := x + 1 /; x > 0\ng[y_] := y * 2 /; y < 100"),
                Arguments.of("multipleDereferencingSyntax", "$UserID[Name]\n$SessionID[Token]\n$ConfigVar[Setting]"),
                Arguments.of("nonLinearEvaluationFold", "result = Fold[Plus, 0, list]"),
                Arguments.of("nonLinearEvaluationNest", "result = Nest[f, x, 10]"),
                Arguments.of("inPlaceModificationPrepend", "PrependTo[list, newItem]"),
                Arguments.of("inPlaceModificationAssociateTo", "AssociateTo[assoc, key -> value]"),
                Arguments.of("complexBooleanSingleLine", "If[a > 0 && b > 0 || c > 0, True, False]"),
                Arguments.of("multilineMapSingleLine", "Map[func, list]"),
                Arguments.of("conditionalInParameterNotBody", "func[x_ /; x > 0, y_ /; y < 100] := x + y"),
                Arguments.of("allRulesWithManyViolations", "function [x = 1] := (\n"
                        + "  temp = Import[\"file.dat\"];\n"
                        + "  currentActiveUserSessionData = temp;\n"
                        + "  DoCalculateAndProcessData[currentActiveUserSessionData] := \n"
                        + "    TimeConstrained[result /@ \n"
                        + "      input && flag ||\n"
                        + "      condition, 10] /; result > 0;\n"
                        + "  AppendTo[list, item];\n"
                        + "  User = _String | \"admin\" | \"guest\";\n"
                        + "  ValidateUser[x_] := StringQ[x];\n"
                        + ")"),
                Arguments.of("nestedModulesWithAssignments", "Module[{x = 1}, Block[{y = 2}, With[{z = x + y}, z * 2]]]"),
                Arguments.of("foldList", "result = FoldList[Plus, 0, list]"),
                Arguments.of("nestList", "result = NestList[f, x0, 10]"),
                Arguments.of("rightCompositionPipeline", "pipeline = RightComposition[f, g, h]"),
                Arguments.of("patternWithAlternatives", "TypeP = Alternatives[_Integer, _Real, _String]"),
                Arguments.of("errorMessageWithSetNotDelayed", "myFunc::usage = \"Usage message for myFunc\""),
                Arguments.of("properFunctionDefinitionNoIssues", "myFunc[x_Integer] := x^2\n"
                        + "helperFunc[y_Real] := Sqrt[y]\n"
                        + "processData[data_List] := Map[transform, data]"),
                Arguments.of("codeWithNoViolations", "calculateSum[numbers_List] := Total[numbers]\n"
                        + "UserP = _String\n"
                        + "ValidQ[x_] := StringQ[x]\n"
                        + "ColorP = Red | Green | Blue"),
                Arguments.of("detectBracketSpacingBeforeTriggered", "result = Table [i, {i, 10}]"),
                Arguments.of("detectAbbreviatedVariablesTriggered", "Module[{x, y, z}, x + y + z]"),
                Arguments.of("detectLongVariableNamesTriggered", "thisIsAnExtremelyLongVariableNameThatExceedsTheMaximumAllowedLength = 42"),
                Arguments.of("detectGlobalNoDollarTriggered", "GlobalVariable = 100; AnotherGlobal = 200;"),
                Arguments.of("detectLongFunctionNamesTriggered", "thisIsAnExtremelyLongFunctionNameThatExceedsMaximumLength[x_] := x + 1"),
                Arguments.of("detectPatternNoPSuffixTriggered", "User = _String | _Integer"),
                Arguments.of("detectPatternTestNoQSuffixTriggered", "ValidUser[x_] := StringQ[x]"),
                Arguments.of("detectTimeConstrainedTriggered", "result = TimeConstrained[longComputation[], 10]"),
                Arguments.of("detectWithCodeInComments", "(* _Integer *) (* _String *) (* If[x > 0, True, False] *)"),
                Arguments.of("detectWithCodeInStringLiterals", "str = \"_Integer\"; str2 = \"If[x > 0, True, False]\";"),
                Arguments.of("detectWithEdgeCases", "F[x_] := x; G[y__] := y; H[z___] := z;")
        );
    }

    private static Stream<Arguments> codingStandardTestCasesPart2() {
        return Stream.concat(
            codingStandardTestCasesPart2a(),
            codingStandardTestCasesPart2b()
        );
    }

    private static Stream<Arguments> codingStandardTestCasesPart2a() {
        return Stream.of(
                Arguments.of("detectWithOnlyWhitespace", "   \n\n\t\t  \n"),
                Arguments.of("detectWithMixedPatterns", "F[x_?NumericQ] := x^2; G[y_Integer] := y + 1;"),
                // Target line 310-311: looksAbbreviated() returns false for "ops"
                Arguments.of("abbreviatedVariableOpsException", "ops = GetOptions[];"),
                // Target line 310-311: looksAbbreviated() returns false for "qual"
                Arguments.of("abbreviatedVariableQualException", "qual = QualityCheck[data];"),
                // Target line 358: UPPERCASE_CLUSTER_PATTERN match (skip reporting)
                Arguments.of("globalNoDollarWithUppercaseCluster", "MYCONST = 100;"),
                // Target line 358: contains varName + "[" (skip reporting as it's a function)
                Arguments.of("globalNoDollarIsFunctionDefinition", "MyFunc = 5; result = MyFunc[x];"),
                // Target line 432: pattern name length <= 2 (skip reporting)
                Arguments.of("patternNoPSuffixShortPatternName", "X = _Integer; Y = _String;"),
                // Target line 432: pattern already ends with P (skip reporting)
                Arguments.of("patternNoPSuffixAlreadyHasP", "NumberP = _Integer | _Real;"),
                // Target line 448: function already ends with Q (skip reporting)
                Arguments.of("patternTestNoQSuffixAlreadyHasQ", "IsValidQ[x_] := IntegerQ[x] && x > 0;"),
                // Test isInsideComment check for detectLongFunctionNames (line 370)
                Arguments.of("longFunctionNameInComment", "(* GetCurrentActiveUserProfileDataFromDatabase[id_] := id *)"),
                // Test isInsideComment check for detectCustomAssociationParams (line 339)
                Arguments.of("customAssociationParamsInComment", "(* ProcessData[<|\"key\" -> value|>] := value *)"),
                // Test isInsideComment check for detectLongVariableNames (line 323)
                Arguments.of("longVariableNameInComment", "(* currentActiveUserProfileData = getData[] *)"),
                // Test isInsideComment check for detectGlobalNoDollar (line 353)
                Arguments.of("globalNoDollarInComment", "(* GlobalVar = 100 *)"),
                // Test isInsideComment check for detectPatternNoPSuffix (line 427)
                Arguments.of("patternNoPSuffixInComment", "(* User = _String | _Integer *)"),
                // Test isInsideComment check for detectPatternTestNoQSuffix (line 444)
                Arguments.of("patternTestNoQSuffixInComment", "(* ValidUser[x_] := StringQ[x] *)"),
                // Test that comments don't trigger false positives (line 251-252)
                Arguments.of("noEmptyLineBetweenCodeWithCommentLine", "result1 = func1[];\n(* Comment *)\nresult2 = func2[];"),
                // Test lines that are empty (line 249-250)
                Arguments.of("noEmptyLineBetweenCodeEmptyLines", "result1 = func1[];\n\nresult2 = func2[];"),
                // Test line ending with semicolon but next line doesn't match pattern (line 253-254)
                Arguments.of("noEmptyLineBetweenCodeWithSemicolonButNoMatch", "result1 = func1[];\nlocalVar = 5;"),
                // Test line ending with ] but next line doesn't match pattern (line 253-254)
                Arguments.of("noEmptyLineBetweenCodeWithBracketButNoMatch", "result1 = func1[x];\nlocalVar = 5;"),
                // Bracket spacing inside string literal should be ignored
                Arguments.of("bracketSpacingBeforeInString", "message = \"function [x] is invalid\";"),
                // Module assignment pattern inside string should be ignored
                Arguments.of("moduleAssignmentInString", "str = \"Module[{x = 1}, body]\";"),
                // Complex boolean pattern inside string should be ignored
                Arguments.of("complexBooleanInString", "helpText = \"Use a && b ||\n c instead\";"),
                // Multiline map shorthand inside string should be ignored
                Arguments.of("multilineMapShorthandInString", "example = \"Use /@\n for mapping\";"),
                // Error message pattern inside string should be ignored
                Arguments.of("errorMessageDelayedInString", "helpText = \"Define myFunc::error := message\";"),
                // Conditional function definition inside string should be ignored
                Arguments.of("conditionalFunctionDefInString", "example = \"func[x_] := body /; condition\";"),
                // Dereferencing syntax inside string should be ignored
                Arguments.of("dereferencingSyntaxInString", "example = \"Access $UserID[Name]\";"),
                // Non-linear evaluation inside string should be ignored
                Arguments.of("nonLinearEvaluationInString", "helpText = \"Try Composition[f, g, h]\";"),
                // In-place modification inside string should be ignored
                Arguments.of("inPlaceModificationInString", "warning = \"Avoid AppendTo[list, item]\";"),
                // Long variable name inside string should be ignored
                Arguments.of("longVariableNameInString", "example = \"currentActiveUserProfileData = value\";"),
                // TimeConstrained inside string should be ignored
                Arguments.of("timeConstrainedInString", "warning = \"Avoid TimeConstrained[expr, timeout]\";"),
                // Enumerated string pattern inside string should be ignored
                Arguments.of("enumeratedStringPatternInString", "example = \"Define as = \\\"a\\\" | \\\"b\\\" | \\\"c\\\"\";"),
                // Variable with exactly 3 characters (boundary case)
                Arguments.of("abbreviatedVariableLengthExactly3", "val = 10; str = \"hello\"; num = 42;"),
                // Variable with consonant cluster pattern
                Arguments.of("abbreviatedVariableConsonantClusterEdgeCase", "xcvbn = Import[file]; qwrty = Process[data];"),
                // Variable with camel case short pattern
                Arguments.of("abbreviatedVariableCamelCaseShort", "aB = 5; xY = 10; zA = 15;"),
                // Custom association parameter with nested brackets
                Arguments.of("customAssociationParamsWithBrackets", "ProcessConfig[<|\"nested\" -> <|\"key\" -> value|>|>] := body"),
                // Custom association with list of rules in nested structure
                Arguments.of("customAssociationParamsListOfRulesNested", "ConfigureSystem[{outer -> {inner -> value, other -> data}}] := process"),
                // Global variable without uppercase cluster
                Arguments.of("globalNoDollarMixedCaseNoUppercaseCluster", "MyVariable = 100;"),
                // Global variable not followed by bracket (not a function)
                Arguments.of("globalNoDollarNotFollowedByBracket", "GlobalData = {1, 2, 3}; result = GlobalData[[1]];"),
                // Litter words combined with other words
                Arguments.of("litterWordsCombinedWithOthers",
                    "DoSomething[x_] := x; MakeSomething[y_] := y; GetSomething[z_] := z; AndCombine[a_, b_] := a + b;"),
                // Litter words at the end of function name
                Arguments.of("litterWordsAtEndOfName", "ProcessAndDo[x_] := x; CalculateMake[y_] := y; FetchGet[z_] := z;")
        );
    }

    private static Stream<Arguments> codingStandardTestCasesPart2b() {
        return Stream.of(
                // Pure function that's exactly one line (boundary)
                Arguments.of("longPureFunctionExactlyOneLine", "Map[Function[x, x^2 + x + 1] &, list]"),
                // Pure function with newline but short body
                Arguments.of("longPureFunctionWithNewlineButShort", "Map[Function[x,\n x + 1\n] &, list]"),
                // Pattern name with exactly 3 characters
                Arguments.of("patternNoPSuffixExactly3Chars", "Usr = _String;"),
                // Pattern using Alternatives function explicitly
                Arguments.of("patternNoPSuffixWithAlternativesFunction", "Type = Alternatives[_Integer, _String];"),
                // Pattern test function that doesn't use MatchQ
                Arguments.of("patternTestNoQSuffixWithoutMatchQ", "ValidData[x_] := IntegerQ[x] && x > 0;"),
                // Pattern test with complex pattern but no Q suffix
                Arguments.of("patternTestNoQSuffixComplexPattern", "ValidInput[x_] := MatchQ[x, {_Integer, _String}] && Length[x] == 2;"),
                // Enumerated pattern with exactly 2 strings (boundary)
                Arguments.of("enumeratedStringPatternWithFewStrings", "BinaryP = \"yes\" | \"no\";"),
                // Enumerated pattern with many string options
                Arguments.of("enumeratedStringPatternManyEnumerations",
                    "DayP = \"Monday\" | \"Tuesday\" | \"Wednesday\" | \"Thursday\" | \"Friday\" | \"Saturday\" | \"Sunday\";"),
                // Current line is empty (skip check)
                Arguments.of("noEmptyLineBetweenCodeCurrentLineEmpty", "\nresult = func[];"),
                // Next line is empty (skip check)
                Arguments.of("noEmptyLineBetweenCodeNextLineEmpty", "result = func[];\n"),
                // Current line starts with comment marker
                Arguments.of("noEmptyLineBetweenCodeCurrentStartsWithComment", "(* Comment *)\nresult = func[];"),
                // Next line starts with comment marker
                Arguments.of("noEmptyLineBetweenCodeNextStartsWithComment", "result1 = func1[];\n(* Comment *)"),
                // Current line doesn't end with semicolon or bracket
                Arguments.of("noEmptyLineBetweenCodeCurrentNoSemicolonOrBracket", "x = 5\nresult = func[];"),
                // Next line doesn't match function call or assignment pattern
                Arguments.of("noEmptyLineBetweenCodeNextNoFunctionOrAssignment", "result = func[];\n  (* Just a comment *)"),
                // Code with multiple pattern matches to test loop coverage
                Arguments.of("multiplePatternMatches", "Module[{a = 1, b = 2, c = 3}, Block[{x = a, y = b, z = c}, x + y + z]]"),
                // Multiple long function names in one file
                Arguments.of("multipleLongFunctionNames", "GetCurrentActiveUserData[id_] := id;\n"
                        + "CalculateAdvancedStatisticalMetrics[data_] := data;\n"
                        + "ProcessComplexMultiStepOperation[input_] := input;"),
                // Multiple abbreviated variables
                Arguments.of("multipleAbbreviatedVariables", "x = 1; y = 2; z = 3; ab = 4; cd = 5; xP = 6;"),
                // Multiple long variable names
                Arguments.of("multipleLongVariableNames", "currentActiveUserSessionData = 1;\n"
                        + "previousProcessedTransactionRecord = 2;\n"
                        + "temporaryIntermediateCalculationResult = 3;"),
                // Multiple global variables without $ prefix
                Arguments.of("multipleGlobalVariables", "Config = <||>; Settings = {}; Params = Association[];"),
                // Multiple bracket spacing violations
                Arguments.of("bracketSpacingBeforeMultipleMatches", "func1 [x_] := x; func2 [y_] := y; func3 [z_] := z;"),
                // Multiple complex boolean expressions
                Arguments.of("complexBooleanMultipleMatches", "If[a && b ||\n c && d, True] && If[x || y &&\n z, False]"),
                // Multiple error messages with :=
                Arguments.of("errorMessageMultipleDelayed", "func1::err1 := \"Error 1\";\nfunc2::err2 := \"Error 2\";\nfunc3::err3 := \"Error 3\";"),
                // Multiple conditional function definitions
                Arguments.of("conditionalFunctionDefMultiple", "f[x_] := x /; x > 0;\ng[y_] := y /; y < 10;\nh[z_] := z /; z != 0;"),
                // Multiple dereferencing syntax uses
                Arguments.of("dereferencingMultiple", "$UserID[Name]; $SessionID[Token]; $ConfigVar[Key]; $DataStore[Value];"),
                // Test all non-linear evaluation types
                Arguments.of("nonLinearEvaluationAllTypes", "Composition[f, g][x];\nRightComposition[a, b][y];\nFold[Plus, 0, list];\n"
                        + "FoldList[Times, 1, data];\nNest[func, init, 10];\nNestList[func, init, 5];"),
                // Test all in-place modification types
                Arguments.of("inPlaceModificationAllTypes", "AppendTo[list, item];\nPrependTo[list, first];\nAssociateTo[assoc, key -> val];"),
                // Test all litter word types
                Arguments.of("litterWordsAllTypes", "DoProcess[x_] := x;\nMakeData[y_] := y;\nGetValue[z_] := z;\nAndCombine[a_] := a;"),
                // Pattern with very long name without P suffix
                Arguments.of("patternNoPSuffixVeryLongName", "VeryLongPatternNameThatShouldEndWithP = _Integer | _String;"),
                // Pattern test with very long name without Q suffix
                Arguments.of("patternTestNoQSuffixVeryLongName", "VeryLongPatternTestNameThatShouldEndWithQ[x_] := StringQ[x] && IntegerQ[x];"),
                // Comprehensive test with many different violation types
                Arguments.of("mixedViolationsComprehensive", "Module[{longVariableNameWithFourWords = 1}, \n"
                        + "  GlobalConfig = <||>;\n"
                        + "  func1 [x_] := x;\n"
                        + "  DoCalculation[data_] := AppendTo[result, data];\n"
                        + "  GetUserData[id_] := $UserID[id];\n"
                        + "  Type = \"int\" | \"string\" | \"bool\";\n"
                        + "  ValidInput[x_] := MatchQ[x, _Integer];\n"
                        + "  TimeConstrained[longOp[], 10];\n"
                        + "  Composition[f, g, h][x];\n"
                        + "  error::msg := \"Error occurred\";\n"
                        + "]")
        );
    }

    @Test
    void testDetectLongPureFunctionsTriggered() {
        StringBuilder sb = new StringBuilder("result = Map[(");
        for (int i = 0; i < 25; i++) {
            sb.append("#").append(i > 0 ? " + " : "");
        }
        sb.append(") &, data]");
        assertDoesNotThrow(() -> detector.detect(context, inputFile, sb.toString(), sb.toString()));
    }

    @Test
    void testDetectWithEmptyContent() {
        String code = "";
        String cleanedCode = "";
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, cleanedCode));
        // Verify detector handles empty content and cleaned code gracefully
        verify(context, never()).newIssue();
    }
}
