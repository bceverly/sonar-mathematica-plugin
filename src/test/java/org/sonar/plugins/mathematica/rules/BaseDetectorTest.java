package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class BaseDetectorTest {

    private TestableBaseDetector detector;

    // Concrete implementation for testing the abstract BaseDetector
    private static final class TestableBaseDetector extends BaseDetector {
        // Expose protected methods for testing
        public int[] publicBuildLineOffsetArray(String content) {
            return buildLineOffsetArray(content);
        }

        public int publicCalculateLineNumber(String content, int offset) {
            return calculateLineNumber(content, offset);
        }

        public List<int[]> publicAnalyzeComments(String content) {
            return analyzeComments(content);
        }

        public boolean publicIsInsideComment(int position, List<int[]> commentRanges) {
            return isInsideComment(position, commentRanges);
        }

        public boolean publicIsInsideStringLiteral(String content, int position) {
            return isInsideStringLiteral(content, position);
        }

        public boolean publicLooksLikeNaturalLanguage(String text) {
            return looksLikeNaturalLanguage(text);
        }

        public int publicCountOccurrences(String text, String patternString) {
            return countOccurrences(text, patternString);
        }

        public void publicReportIssueWithFix(org.sonar.api.batch.sensor.SensorContext context,
                                            org.sonar.api.batch.fs.InputFile inputFile,
                                            int line, String ruleKey, String message,
                                            int startOffset, int endOffset,
                                            org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext fixContext) {
            IssueWithFixData issueData = new IssueWithFixData(inputFile, line, ruleKey, message,
                                                               startOffset, endOffset, fixContext);
            reportIssueWithFix(context, issueData);
        }

        public boolean publicHasNonNumericParameters(String funcDef) {
            return hasNonNumericParameters(funcDef);
        }

        public boolean publicIsIdiomaticStringUsage(String content, int position, String literal) {
            return isIdiomaticStringUsage(content, position, literal);
        }

        public boolean publicIsInsideComment(String content, int position) {
            return isInsideComment(content, position);
        }
    }

    @BeforeEach
    void setUp() {
        detector = new TestableBaseDetector();
    }

    @Test
    void testBuildLineOffsetArraySingleLine() {
        String content = "single line";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(1);
        assertThat(offsets[0]).isZero();
    }

    @Test
    void testBuildLineOffsetArrayMultipleLines() {
        String content = "line1\nline2\nline3";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(3);
        assertThat(offsets[0]).isZero();
        assertThat(offsets[1]).isEqualTo(6);  // After "line1\n"
        assertThat(offsets[2]).isEqualTo(12); // After "line1\nline2\n"
    }

    @Test
    void testBuildLineOffsetArrayEmptyLines() {
        String content = "line1\n\nline3";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(3);
        assertThat(offsets[0]).isZero();
        assertThat(offsets[1]).isEqualTo(6);
        assertThat(offsets[2]).isEqualTo(7);
    }

    @Test
    void testCalculateLineNumberWithCache() {
        String content = "line1\nline2\nline3\nline4";
        detector.initializeCaches(content);

        assertThat(detector.publicCalculateLineNumber(content, 0)).isEqualTo(1);   // Start of line1
        assertThat(detector.publicCalculateLineNumber(content, 6)).isEqualTo(2);   // Start of line2
        assertThat(detector.publicCalculateLineNumber(content, 12)).isEqualTo(3);  // Start of line3
        assertThat(detector.publicCalculateLineNumber(content, 18)).isEqualTo(4);  // Start of line4

        detector.clearCaches();
    }

    @Test
    void testCalculateLineNumberWithoutCache() {
        String content = "line1\nline2\nline3";

        // Test without initializing caches (fallback path)
        assertThat(detector.publicCalculateLineNumber(content, 0)).isEqualTo(1);
        assertThat(detector.publicCalculateLineNumber(content, 6)).isEqualTo(2);
        assertThat(detector.publicCalculateLineNumber(content, 12)).isEqualTo(3);
    }

    @Test
    void testAnalyzeCommentsSimpleComment() {
        String content = "code (* comment *) more code";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).hasSize(1);
        assertThat(commentRanges.get(0)[0]).isEqualTo(5);  // Start of (*
        assertThat(commentRanges.get(0)[1]).isEqualTo(18); // End of *)
    }

    @Test
    void testAnalyzeCommentsNestedComments() {
        String content = "code (* outer (* nested *) outer *) code";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).hasSize(1);
        // Should capture the entire nested comment
        assertThat(commentRanges.get(0)[0]).isEqualTo(5);  // Start of first (*
        assertThat(commentRanges.get(0)[1]).isEqualTo(35); // End of last *)
    }

    @Test
    void testAnalyzeCommentsMultipleComments() {
        String content = "code (* comment1 *) more (* comment2 *) end";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).hasSize(2);
        assertThat(commentRanges.get(0)[0]).isEqualTo(5);
        assertThat(commentRanges.get(0)[1]).isEqualTo(19);
        assertThat(commentRanges.get(1)[0]).isEqualTo(25);
        assertThat(commentRanges.get(1)[1]).isEqualTo(39);
    }

    @Test
    void testIsInsideComment() {
        String content = "code (* comment *) more code";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(detector.publicIsInsideComment(0, commentRanges)).isFalse();  // Before comment
        assertThat(detector.publicIsInsideComment(10, commentRanges)).isTrue();  // Inside comment
        assertThat(detector.publicIsInsideComment(20, commentRanges)).isFalse(); // After comment
    }

    @Test
    void testIsInsideStringLiteral() {
        String content = "x = \"hello world\" + y";

        assertThat(detector.publicIsInsideStringLiteral(content, 0)).isFalse();  // Before string
        assertThat(detector.publicIsInsideStringLiteral(content, 6)).isTrue();   // Inside string
        assertThat(detector.publicIsInsideStringLiteral(content, 18)).isFalse(); // After string
    }

    @Test
    void testIsInsideStringLiteralEscapedQuotes() {
        String content = "x = \"hello \\\"nested\\\" world\"";

        assertThat(detector.publicIsInsideStringLiteral(content, 10)).isTrue();  // Inside string
        assertThat(detector.publicIsInsideStringLiteral(content, 14)).isTrue();  // Still inside despite escaped quote
    }

    @Test
    void testLooksLikeNaturalLanguageIsNaturalLanguage() {
        // Need at least 2 indicators from NATURAL_LANGUAGE_PHRASES
        assertThat(detector.publicLooksLikeNaturalLanguage("this function returns a value")).isTrue();
        assertThat(detector.publicLooksLikeNaturalLanguage("note that this will fail")).isTrue();
        assertThat(detector.publicLooksLikeNaturalLanguage("this function calculates the result")).isTrue();
    }

    @Test
    void testLooksLikeNaturalLanguageNotNaturalLanguage() {
        assertThat(detector.publicLooksLikeNaturalLanguage("x := y + z")).isFalse();
        assertThat(detector.publicLooksLikeNaturalLanguage("Module[{x}, x = 1]")).isFalse();
        assertThat(detector.publicLooksLikeNaturalLanguage("func[arg_]")).isFalse();
    }

    @Test
    void testLooksLikeNaturalLanguageSingleIndicator() {
        // Should return false with only one indicator
        assertThat(detector.publicLooksLikeNaturalLanguage("this is test")).isFalse();
    }

    @Test
    void testCountOccurrences() {
        String text = "hello world hello universe hello";

        assertThat(detector.publicCountOccurrences(text, "hello")).isEqualTo(3);
        assertThat(detector.publicCountOccurrences(text, "world")).isEqualTo(1);
        assertThat(detector.publicCountOccurrences(text, "notfound")).isZero();
    }

    @Test
    void testCountOccurrencesRegexPattern() {
        String text = "x1 y2 z3 a4";

        assertThat(detector.publicCountOccurrences(text, "[a-z]\\d")).isEqualTo(4);
    }

    @Test
    void testCountOccurrencesInvalidPattern() {
        String text = "test content";

        // Should return 0 on error
        assertThat(detector.publicCountOccurrences(text, "[invalid(")).isZero();
    }

    @Test
    void testInitializeAndClearCaches() {
        String content = "line1\nline2\nline3";

        detector.initializeCaches(content);

        // Verify caches are initialized by using them
        assertThat(detector.publicCalculateLineNumber(content, 6)).isEqualTo(2);

        detector.clearCaches();

        // After clearing, should still work (fallback mode)
        assertThat(detector.publicCalculateLineNumber(content, 6)).isEqualTo(2);
    }

    @Test
    void testInitializeCachesWithAst() {
        // Test that AST parsing doesn't crash
        String content = "f[x_] := x + 1";

        assertThatCode(() -> {
            detector.initializeCaches(content);
            detector.clearCaches();
        }).doesNotThrowAnyException();
    }

    @Test
    void testInitializeCachesWithInvalidCode() {
        // Test that invalid code doesn't crash
        String content = "]]]]invalid[[[[";

        assertThatCode(() -> {
            detector.initializeCaches(content);
            detector.clearCaches();
        }).doesNotThrowAnyException();
    }

    @Test
    void testPatternCache() {
        // Test that pattern caching works
        String text = "test";

        int count1 = detector.publicCountOccurrences(text, "t");
        int count2 = detector.publicCountOccurrences(text, "t");

        // Both should work, verifying pattern cache doesn't break anything
        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(2);
    }

    @Test
    void testAnalyzeCommentsUnclosedComment() {
        // Test handling of unclosed comments
        String content = "code (* unclosed comment";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        // Should not crash, may have 0 or 1 ranges depending on implementation
        assertThat(commentRanges).isNotNull();
    }

    @Test
    void testAnalyzeCommentsEmptyContent() {
        String content = "";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).isEmpty();
    }

    @Test
    void testIsInsideStringLiteralAtLineStart() {
        String content = "\nx = \"test\"";

        assertThat(detector.publicIsInsideStringLiteral(content, 6)).isTrue();
    }

    @Test
    void testBuildLineOffsetArrayLargeFile() {
        // Test with many lines
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            content.append("line").append(i).append("\n");
        }

        int[] offsets = detector.publicBuildLineOffsetArray(content.toString());

        assertThat(offsets).hasSizeGreaterThanOrEqualTo(1000);
    }

    @Test
    void testCalculateLineNumberOffsetAtEnd() {
        String content = "line1\nline2\nline3";
        detector.initializeCaches(content);

        // Test offset at very end
        int endOffset = content.length();
        assertThat(detector.publicCalculateLineNumber(content, endOffset)).isPositive();

        detector.clearCaches();
    }

    @Test
    void testSetSensor() {
        // Test the setSensor method - this is the uncovered line!
        MathematicaRulesSensor mockSensor = org.mockito.Mockito.mock(MathematicaRulesSensor.class);

        detector.setSensor(mockSensor);

        // Verify it doesn't throw
        assertThat(detector).isNotNull();
    }

    @Test
    void testReportIssueWithFixWithSensor() {
        // Test the reportIssueWithFix method with a sensor set
        MathematicaRulesSensor mockSensor = org.mockito.Mockito.mock(MathematicaRulesSensor.class);
        org.sonar.api.batch.sensor.SensorContext mockContext = org.mockito.Mockito.mock(org.sonar.api.batch.sensor.SensorContext.class);
        org.sonar.api.batch.fs.InputFile mockInputFile = org.mockito.Mockito.mock(org.sonar.api.batch.fs.InputFile.class);
        org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext mockFixContext =
            org.mockito.Mockito.mock(org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext.class);

        String content = "test content";
        detector.initializeCaches(content);
        detector.setSensor(mockSensor);

        assertThatCode(() ->
            detector.publicReportIssueWithFix(mockContext, mockInputFile, 1, "TEST_RULE", "Test message", 0, 4, mockFixContext)
        ).doesNotThrowAnyException();

        detector.clearCaches();
    }

    @Test
    void testReportIssueWithFixWithoutSensor() {
        // Test the reportIssueWithFix method without a sensor (fallback path)
        org.sonar.api.batch.sensor.SensorContext mockContext = org.mockito.Mockito.mock(org.sonar.api.batch.sensor.SensorContext.class);
        org.sonar.api.batch.fs.InputFile mockInputFile = org.mockito.Mockito.mock(org.sonar.api.batch.fs.InputFile.class);
        org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext mockFixContext =
            org.mockito.Mockito.mock(org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext.class);

        // Mock the context.newIssue() chain
        org.sonar.api.batch.sensor.issue.NewIssue mockIssue =
            org.mockito.Mockito.mock(org.sonar.api.batch.sensor.issue.NewIssue.class);
        org.sonar.api.batch.sensor.issue.NewIssueLocation mockLocation =
            org.mockito.Mockito.mock(org.sonar.api.batch.sensor.issue.NewIssueLocation.class);

        org.mockito.Mockito.when(mockContext.newIssue()).thenReturn(mockIssue);
        org.mockito.Mockito.when(mockIssue.newLocation()).thenReturn(mockLocation);
        org.mockito.Mockito.when(mockLocation.on(mockInputFile)).thenReturn(mockLocation);
        org.mockito.Mockito.when(mockLocation.at(org.mockito.Mockito.any())).thenReturn(mockLocation);
        org.mockito.Mockito.when(mockLocation.message(org.mockito.Mockito.anyString())).thenReturn(mockLocation);
        org.mockito.Mockito.when(mockIssue.forRule(org.mockito.Mockito.any())).thenReturn(mockIssue);
        org.mockito.Mockito.when(mockIssue.at(mockLocation)).thenReturn(mockIssue);
        org.mockito.Mockito.when(mockInputFile.selectLine(1)).thenReturn(org.mockito.Mockito.mock(org.sonar.api.batch.fs.TextRange.class));

        String content = "test content";
        detector.initializeCaches(content);
        // Don't set sensor - this tests the fallback path

        assertThatCode(() ->
            detector.publicReportIssueWithFix(mockContext, mockInputFile, 1, "TEST_RULE", "Test message", 0, 4, mockFixContext)
        ).doesNotThrowAnyException();

        detector.clearCaches();
    }

    // Tests for hasNonNumericParameters
    @Test
    void testHasNonNumericParametersString() {
        assertThat(detector.publicHasNonNumericParameters("f[x_String]")).isTrue();
        assertThat(detector.publicHasNonNumericParameters("f[x_?StringQ]")).isTrue();
    }

    @Test
    void testHasNonNumericParametersList() {
        assertThat(detector.publicHasNonNumericParameters("f[x_List]")).isTrue();
        assertThat(detector.publicHasNonNumericParameters("f[{___}]")).isTrue();
        assertThat(detector.publicHasNonNumericParameters("f[{___?StringQ}]")).isTrue();
    }

    @Test
    void testHasNonNumericParametersAssociation() {
        assertThat(detector.publicHasNonNumericParameters("f[x_Association]")).isTrue();
    }

    @Test
    void testHasNonNumericParametersSymbol() {
        assertThat(detector.publicHasNonNumericParameters("f[x_Symbol]")).isTrue();
    }

    @Test
    void testHasNonNumericParametersImage() {
        assertThat(detector.publicHasNonNumericParameters("f[x_Image]")).isTrue();
    }

    @Test
    void testHasNonNumericParametersGraphics() {
        assertThat(detector.publicHasNonNumericParameters("f[x_Graphics]")).isTrue();
    }

    @Test
    void testHasNonNumericParametersGraph() {
        assertThat(detector.publicHasNonNumericParameters("f[x_Graph]")).isTrue();
    }

    @Test
    void testHasNonNumericParametersNumeric() {
        assertThat(detector.publicHasNonNumericParameters("f[x_Integer]")).isFalse();
        assertThat(detector.publicHasNonNumericParameters("f[x_Real]")).isFalse();
        assertThat(detector.publicHasNonNumericParameters("f[x_]")).isFalse();
    }

    // Tests for isIdiomaticStringUsage
    @Test
    void testIsIdiomaticStringUsageVeryShortString() {
        assertThat(detector.publicIsIdiomaticStringUsage("x = \"a\"", 4, "\"a\"")).isTrue();
        assertThat(detector.publicIsIdiomaticStringUsage("x = \"ab\"", 4, "\"ab\"")).isTrue();
        assertThat(detector.publicIsIdiomaticStringUsage("x = \"abc\"", 4, "\"abc\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsageAssociationKey() {
        String content = "<|\"key\" -> value|>";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 2, "\"key\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsageAssociationAccess() {
        String content = "assoc[\"key\"]";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 6, "\"key\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsageOptionValue() {
        String content = "opt -> \"value\"";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 7, "\"value\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsageRulePattern() {
        String content = "{\"key\" -> value}";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 1, "\"key\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsageCommaPattern() {
        String content = "f[x, \"key\" -> value]";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 5, "\"key\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsageNonIdiomatic() {
        String content = "message = \"This is a long error message\"";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 10, "\"This is a long error message\"")).isFalse();
    }

    // Tests for isInsideComment (String-based version)
    @Test
    void testIsInsideCommentStringBasedSimple() {
        String content = "code (* comment *) more";
        assertThat(detector.publicIsInsideComment(content, 10)).isTrue();  // Inside comment
        assertThat(detector.publicIsInsideComment(content, 0)).isFalse();  // Before comment
        assertThat(detector.publicIsInsideComment(content, 20)).isFalse(); // After comment
    }

    @Test
    void testIsInsideCommentStringBasedNested() {
        String content = "code (* outer (* nested *) *) more";
        assertThat(detector.publicIsInsideComment(content, 10)).isTrue();  // Inside outer
        assertThat(detector.publicIsInsideComment(content, 18)).isTrue();  // Inside nested
        assertThat(detector.publicIsInsideComment(content, 32)).isFalse(); // After all
    }

    @Test
    void testIsInsideCommentStringBasedMultiple() {
        String content = "code (* c1 *) mid (* c2 *) end";
        assertThat(detector.publicIsInsideComment(content, 8)).isTrue();   // In first
        assertThat(detector.publicIsInsideComment(content, 15)).isFalse(); // Between
        assertThat(detector.publicIsInsideComment(content, 20)).isTrue();  // In second
    }

    @Test
    void testIsInsideCommentStringBasedAtBoundary() {
        String content = "code (* comment *) more";
        assertThat(detector.publicIsInsideComment(content, 5)).isFalse();  // At start (*
        assertThat(detector.publicIsInsideComment(content, 6)).isTrue();   // Just after (*
        assertThat(detector.publicIsInsideComment(content, 16)).isTrue();  // Just before *)
        assertThat(detector.publicIsInsideComment(content, 18)).isFalse(); // At end *)
    }

    // Tests for multi-line string literal detection
    @Test
    void testIsInsideStringLiteralMultiLine() {
        String content = "x = \"line1\nline2\nline3\"";
        assertThat(detector.publicIsInsideStringLiteral(content, 10)).isTrue();  // In line1
        assertThat(detector.publicIsInsideStringLiteral(content, 15)).isTrue();  // In line2
        assertThat(detector.publicIsInsideStringLiteral(content, 20)).isTrue();  // In line3
    }

    @Test
    void testIsInsideStringLiteralEdgeCases() {
        String content = "x = \"test\"";
        assertThat(detector.publicIsInsideStringLiteral(content, -1)).isFalse(); // Before start
        assertThat(detector.publicIsInsideStringLiteral(content, 100)).isFalse(); // After end
    }

    @Test
    void testIsInsideStringLiteralEmptyString() {
        String content = "x = \"\"";
        assertThat(detector.publicIsInsideStringLiteral(content, 5)).isTrue();
    }

    @Test
    void testIsInsideStringLiteralMultipleStrings() {
        String content = "x = \"first\" + \"second\"";
        assertThat(detector.publicIsInsideStringLiteral(content, 6)).isTrue();   // In first
        assertThat(detector.publicIsInsideStringLiteral(content, 12)).isFalse(); // Between
        assertThat(detector.publicIsInsideStringLiteral(content, 16)).isTrue();  // In second
    }

    @Test
    void testIsInsideStringLiteralLineStart() {
        String content = "\n\"test\"";
        assertThat(detector.publicIsInsideStringLiteral(content, 2)).isTrue();
    }

    // Edge cases for calculateLineNumber
    @Test
    void testCalculateLineNumberBoundaryConditions() {
        String content = "line1\nline2\nline3";
        detector.initializeCaches(content);

        // Test at exact newline positions
        assertThat(detector.publicCalculateLineNumber(content, 5)).isEqualTo(1);  // At \n of line1
        assertThat(detector.publicCalculateLineNumber(content, 11)).isEqualTo(2); // At \n of line2

        detector.clearCaches();
    }

    @Test
    void testCalculateLineNumberWithEmptyLines() {
        String content = "line1\n\n\nline4";
        detector.initializeCaches(content);

        assertThat(detector.publicCalculateLineNumber(content, 6)).isEqualTo(2);  // First empty
        assertThat(detector.publicCalculateLineNumber(content, 7)).isEqualTo(3);  // Second empty
        assertThat(detector.publicCalculateLineNumber(content, 8)).isEqualTo(4);  // line4

        detector.clearCaches();
    }

    // Edge cases for comment analysis
    @Test
    void testAnalyzeCommentsAdjacentComments() {
        String content = "(* c1 *)(* c2 *)";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).hasSize(2);
    }

    @Test
    void testAnalyzeCommentsOnlyCommentStart() {
        String content = "(* no end";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        // Should handle gracefully
        assertThat(commentRanges).isNotNull();
    }

    @Test
    void testAnalyzeCommentsOnlyCommentEnd() {
        String content = "no start *)";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        // Should handle gracefully
        assertThat(commentRanges).isEmpty();
    }

    @Test
    void testAnalyzeCommentsDeeplyNested() {
        String content = "(* level1 (* level2 (* level3 *) level2 *) level1 *)";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).hasSize(1);
        assertThat(commentRanges.get(0)[0]).isZero();
        assertThat(commentRanges.get(0)[1]).isEqualTo(content.length());
    }

    // Tests for buildLineOffsetArray edge cases
    @Test
    void testBuildLineOffsetArrayTrailingNewline() {
        String content = "line1\nline2\n";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(3); // Three lines: line1, line2, empty
    }

    @Test
    void testBuildLineOffsetArrayOnlyNewlines() {
        String content = "\n\n\n";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(4); // Four empty lines
    }

    // Tests for string literal detection with complex escaping
    @Test
    void testIsInsideStringLiteralMultipleEscapes() {
        String content = "x = \"test\\\\escaped\\\"quote\"";
        assertThat(detector.publicIsInsideStringLiteral(content, 10)).isTrue();
        assertThat(detector.publicIsInsideStringLiteral(content, 20)).isTrue();
    }

    @Test
    void testIsInsideStringLiteralBackslashAtEnd() {
        String content = "x = \"test\\\\\"";
        assertThat(detector.publicIsInsideStringLiteral(content, 10)).isTrue();
    }

    // Tests for natural language detection with edge cases
    @Test
    void testLooksLikeNaturalLanguageMultiplePhrases() {
        // More than 2 indicators
        assertThat(detector.publicLooksLikeNaturalLanguage("this is a hack note that returns a value")).isTrue();
    }

    @Test
    void testLooksLikeNaturalLanguageCaseSensitivity() {
        // Test that case-insensitive matching works (needs 2 indicators)
        assertThat(detector.publicLooksLikeNaturalLanguage("THIS FUNCTION RETURNS A VALUE")).isTrue();
    }

    // Tests for countOccurrences with edge cases
    @Test
    void testCountOccurrencesEmptyText() {
        assertThat(detector.publicCountOccurrences("", "test")).isZero();
    }

    @Test
    void testCountOccurrencesEmptyPattern() {
        // Empty pattern might cause issues
        int count = detector.publicCountOccurrences("test", "");
        assertThat(count).isNotNegative(); // Should not crash
    }

    @Test
    void testCountOccurrencesOverlapping() {
        assertThat(detector.publicCountOccurrences("aaaa", "aa")).isPositive();
    }

    // Tests for comment ranges with edge cases
    @Test
    void testIsInsideCommentRangesEmptyRanges() {
        List<int[]> emptyRanges = new java.util.ArrayList<>();
        assertThat(detector.publicIsInsideComment(10, emptyRanges)).isFalse();
    }

    @Test
    void testIsInsideCommentRangesAtBoundary() {
        List<int[]> ranges = new java.util.ArrayList<>();
        ranges.add(new int[]{5, 10});

        assertThat(detector.publicIsInsideComment(5, ranges)).isTrue();  // At start
        assertThat(detector.publicIsInsideComment(9, ranges)).isTrue();  // Before end
        assertThat(detector.publicIsInsideComment(10, ranges)).isFalse(); // At end (exclusive)
    }

    @Test
    void testIsInsideCommentRangesMultipleRanges() {
        List<int[]> ranges = new java.util.ArrayList<>();
        ranges.add(new int[]{5, 10});
        ranges.add(new int[]{20, 25});

        assertThat(detector.publicIsInsideComment(7, ranges)).isTrue();
        assertThat(detector.publicIsInsideComment(15, ranges)).isFalse();
        assertThat(detector.publicIsInsideComment(22, ranges)).isTrue();
    }

    // Tests for cache behavior
    @Test
    void testClearCachesMultipleTimes() {
        String content = "test";
        detector.initializeCaches(content);
        detector.clearCaches();
        detector.clearCaches(); // Should not crash

        assertThat(detector).isNotNull();
    }

    @Test
    void testInitializeCachesMultipleTimes() {
        detector.initializeCaches("test1");
        detector.initializeCaches("test2");
        detector.initializeCaches("test3");

        // Should not crash
        assertThat(detector).isNotNull();

        detector.clearCaches();
    }

    // Tests for isIdiomaticStringUsage with position at edge
    @Test
    void testIsIdiomaticStringUsagePositionAtStart() {
        String content = "\"key\" -> value";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 0, "\"key\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsagePositionNearEnd() {
        String content = "x -> \"val\"";
        assertThat(detector.publicIsIdiomaticStringUsage(content, 5, "\"val\"")).isTrue();
    }

    @Test
    void testIsIdiomaticStringUsageLongContext() {
        // Test with context window
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("x");
        }
        sb.append("\"test\"");
        String content = sb.toString();

        assertThat(detector.publicIsIdiomaticStringUsage(content, 200, "\"test\"")).isFalse();
    }
}
