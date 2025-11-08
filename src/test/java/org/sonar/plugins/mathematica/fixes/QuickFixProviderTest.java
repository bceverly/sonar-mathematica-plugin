package org.sonar.plugins.mathematica.fixes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.issue.NewIssue;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class QuickFixProviderTest {

    private QuickFixProvider provider;
    private NewIssue issue;
    private InputFile inputFile;
    private TextRange textRange;

    @BeforeEach
    void setUp() {
        provider = new QuickFixProvider();
        issue = mock(NewIssue.class, RETURNS_DEEP_STUBS);
        inputFile = mock(InputFile.class);
        textRange = mock(TextRange.class);

        // Setup mocks
        when(inputFile.newRange(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(textRange);
        when(inputFile.selectLine(anyInt())).thenReturn(textRange);
    }

    @Test
    void testAddQuickFixEmptyBlock() {
        String content = "Module[{x}, ]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "EmptyBlock", content, 0, content.length(), context)
        );

        verify(issue, atLeastOnce()).newQuickFix();
    }

    @Test
    void testAddQuickFixDebugCodeLeftInProduction() {
        String content = "Print[\"debug\"];";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "DebugCodeLeftInProduction", content, 0, 15, context)
        );
    }

    /**
     * Provides test data for parameterized quick fix tests.
     * Each argument contains: rule key and content to test.
     */
    private static Stream<Arguments> quickFixTestData() {
        return Stream.of(
            Arguments.of("DoubleSemicolon", "x = 5;;"),
            Arguments.of("DoubleTranspose", "Transpose[Transpose[matrix]]"),
            Arguments.of("DoubleNegation", "Not[Not[x]]"),
            Arguments.of("UnnecessaryBooleanConversion", "If[x, True, False]"),
            Arguments.of("UnknownRuleKey", "x = 1"),
            Arguments.of("AssignmentInConditional", "If[x = 5, true, false]"),
            Arguments.of("FloatingPointEquality", "x == 1.5"),
            Arguments.of("DeprecatedFunction", "$RecursionLimit"),
            Arguments.of("ComparisonWithNull", "x == Null"),
            Arguments.of("StringConcatInLoop", "Do[str = str <> x, {x, n}]"),
            Arguments.of("AppendInLoop", "Do[list = Append[list, x], {x, n}]"),
            Arguments.of("SetDelayedConfusion", "f[x_] = x^2"),
            Arguments.of("OffByOne", "Do[expr, {i, 0, n}]"),
            Arguments.of("IdenticalBranches", "If[cond, result, result]"),
            Arguments.of("IncorrectSetInScoping", "Module[{x = 5}, body]"),
            Arguments.of("FunctionWithoutReturn", "f[x_] := (y = x + 1;)"),
            Arguments.of("MissingMemoization", "f[x_] := f[x-1] + f[x-2]"),
            Arguments.of("ZeroDenominator", "x / y"),
            Arguments.of("IdentityOperation", "x + 0"),
            Arguments.of("ReverseReverse", "Reverse[Reverse[list]]"),
            Arguments.of("GlobalContext", "Global`variable"),
            Arguments.of("StringJoinForTemplates", "a <> b <> c"),
            Arguments.of("PositionInsteadOfPattern", "Extract[list, Position[list, _Integer]]"),
            Arguments.of("FlattenTableAntipattern", "Flatten[Table[f[x], {x, n}]]"),
            Arguments.of("InconsistentRuleTypes", "a -> 1"),
            Arguments.of("MissingFailedCheck", "data = Import[\"file.txt\"]"),
            Arguments.of("MissingEmptyListCheck", "First[myList]"),
            Arguments.of("MissingPatternTest", "f[x_] := Sqrt[x]"),
            Arguments.of("MissingCompilationTarget", "Compile[{x}, x^2]"),
            Arguments.of("MachinePrecisionInSymbolic", "Solve[x^2 == 2.0]"),
            Arguments.of("ComplexBooleanExpression", "If[a && b && c, result]"),
            Arguments.of("DeleteDuplicatesOnLargeData", "DeleteDuplicates[largeList]"),
            Arguments.of("TypeMismatch", "\"text\" + 5"),
            Arguments.of("BlockModuleMisuse", "Block[{x}, x = 5]"),
            Arguments.of("PatternBlanksMisuse", "Length[args__]"),
            Arguments.of("MissingHoldAttributes", "myFunc[expr]"),
            Arguments.of("UnprotectedSymbols", "publicFunc[x_] := x^2"),
            Arguments.of("EmptyCatchBlock", "Quiet[riskyOperation[]]")
        );
    }

    @ParameterizedTest
    @MethodSource("quickFixTestData")
    void testAddQuickFix(String ruleKey, String content) {
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, ruleKey, content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixWithException() {
        // Use invalid offsets to trigger exception
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        // Should handle exception gracefully and not throw
        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "EmptyBlock", "test", -1, 1000, context)
        );
    }

    @Test
    void testAddQuickFixMultipleRules() {
        String content = "test content";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        // Test several rules from addAdditionalQuickFixes
        String[] rules = {
            "MissingFailedCheck",
            "MissingEmptyListCheck",
            "MissingPatternTest",
            "MissingCompilationTarget",
            "MachinePrecisionInSymbolic",
            "ComplexBooleanExpression",
            "LinearSearchInsteadOfLookup",
            "TypeMismatch",
            "BlockModuleMisuse",
            "UnusedVariables",
            "EmptyCatchBlock",
            "HardcodedFilePaths"
        };

        for (String rule : rules) {
            assertDoesNotThrow(() ->
                provider.addQuickFix(issue, inputFile, rule, content, 0, content.length(), context)
            );
        }
    }

    @Test
    void testQuickFixContextGettersAndSetters() {
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        // Test matched text
        context.setMatchedText("test text");
        assertEquals("test text", context.getMatchedText());

        // Test regex groups
        String[] groups = {"group1", "group2"};
        context.setRegexGroups(groups);
        assertArrayEquals(groups, context.getRegexGroups());

        // Test metadata
        context.putMetadata("key1", "value1");
        assertEquals("value1", context.getMetadata("key1"));
        assertNull(context.getMetadata("nonexistent"));
    }

    @Test
    void testAddQuickFixWithContext() {
        String content = "Module[{x}, ]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();
        context.setMatchedText(content);
        context.putMetadata("test", "value");

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "EmptyBlock", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixAllPhases() {
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        // Test rules from each phase to ensure comprehensive coverage
        String[][] rulesByPhase = {
            // Phase 1-2: Code smells
            {"EmptyBlock", "DebugCodeLeftInProduction", "DoubleSemicolon", "DoubleTranspose"},
            // Phase 3: Simple replacements
            {"DeprecatedFunction", "StringConcatInLoop", "AppendInLoop", "StringJoinForTemplates"},
            // Phase 4: Bug fixes
            {"AssignmentInConditional", "FloatingPointEquality", "SetDelayedConfusion", "OffByOne"},
            // Phase 5: Safety
            {"MissingFailedCheck", "MissingEmptyListCheck", "MissingPatternTest"},
            // Phase 6: Simplifications
            {"MachinePrecisionInSymbolic", "ComplexBooleanExpression"},
            // Phase 7: Performance
            {"LinearSearchInsteadOfLookup", "DeleteDuplicatesOnLargeData", "NestedMapTable"},
            // Phase 8: Patterns
            {"TypeMismatch", "BlockModuleMisuse", "PatternBlanksMisuse"},
            // Phase 9: Organization
            {"UnusedVariables", "EmptyCatchBlock", "RepeatedPartExtraction"}
        };

        for (String[] phaseRules : rulesByPhase) {
            for (String rule : phaseRules) {
                assertDoesNotThrow(() ->
                    provider.addQuickFix(issue, inputFile, rule, "test content", 0, 12, context),
                    "Rule " + rule + " should not throw exception"
                );
            }
        }
    }

    @Test
    void testAddQuickFixMoreRules() {
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        // Test additional rules from addAdditionalQuickFixes
        String[] additionalRules = {
            "IdentityOperation",
            "ReverseReverse",
            "GlobalContext",
            "PositionInsteadOfPattern",
            "FlattenTableAntipattern",
            "UnnecessaryTranspose",
            "InconsistentRuleTypes",
            "EmptyStatement",
            "RepeatedCalculations",
            "PackedArrayBreaking",
            "UnpackingPackedArrays",
            "ExcessivePureFunctions",
            "MissingOperatorPrecedence",
            "MismatchedDimensions",
            "MissingHoldAttributes",
            "UnprotectedSymbols",
            "NestedListsInsteadAssociation"
        };

        for (String rule : additionalRules) {
            assertDoesNotThrow(() ->
                provider.addQuickFix(issue, inputFile, rule, "test", 0, 4, context)
            );
        }
    }

}

