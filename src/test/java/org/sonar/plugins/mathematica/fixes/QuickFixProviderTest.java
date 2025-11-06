package org.sonar.plugins.mathematica.fixes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.issue.NewIssue;

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

    @Test
    void testAddQuickFixDoubleSemicolon() {
        String content = "x = 5;;";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "DoubleSemicolon", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixDoubleTranspose() {
        String content = "Transpose[Transpose[matrix]]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "DoubleTranspose", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixDoubleNegation() {
        String content = "Not[Not[x]]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "DoubleNegation", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixUnnecessaryBooleanConversion() {
        String content = "If[x, True, False]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "UnnecessaryBooleanConversion", content, 0, content.length(), context)
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
    void testAddQuickFixUnknownRule() {
        String content = "x = 1";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        // Unknown rule should complete without error (falls through to default case)
        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "UnknownRuleKey", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixAssignmentInConditional() {
        String content = "If[x = 5, true, false]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "AssignmentInConditional", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixFloatingPointEquality() {
        String content = "x == 1.5";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "FloatingPointEquality", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixDeprecatedFunction() {
        String content = "$RecursionLimit";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "DeprecatedFunction", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixComparisonWithNull() {
        String content = "x == Null";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "ComparisonWithNull", content, 0, content.length(), context)
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
    void testAddQuickFixStringConcatInLoop() {
        String content = "Do[str = str <> x, {x, n}]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "StringConcatInLoop", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixAppendInLoop() {
        String content = "Do[list = Append[list, x], {x, n}]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "AppendInLoop", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixSetDelayedConfusion() {
        String content = "f[x_] = x^2";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "SetDelayedConfusion", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixOffByOne() {
        String content = "Do[expr, {i, 0, n}]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "OffByOne", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixIdenticalBranches() {
        String content = "If[cond, result, result]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "IdenticalBranches", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixIncorrectSetInScoping() {
        String content = "Module[{x = 5}, body]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "IncorrectSetInScoping", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixFunctionWithoutReturn() {
        String content = "f[x_] := (y = x + 1;)";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "FunctionWithoutReturn", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixMissingMemoization() {
        String content = "f[x_] := f[x-1] + f[x-2]";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "MissingMemoization", content, 0, content.length(), context)
        );
    }

    @Test
    void testAddQuickFixZeroDenominator() {
        String content = "x / y";
        QuickFixProvider.QuickFixContext context = new QuickFixProvider.QuickFixContext();

        assertDoesNotThrow(() ->
            provider.addQuickFix(issue, inputFile, "ZeroDenominator", content, 0, content.length(), context)
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
}
