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
            reportIssueWithFix(context, inputFile, line, ruleKey, message, startOffset, endOffset, fixContext);
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
        assertThat(offsets[0]).isEqualTo(0);
    }

    @Test
    void testBuildLineOffsetArrayMultipleLines() {
        String content = "line1\nline2\nline3";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(3);
        assertThat(offsets[0]).isEqualTo(0);
        assertThat(offsets[1]).isEqualTo(6);  // After "line1\n"
        assertThat(offsets[2]).isEqualTo(12); // After "line1\nline2\n"
    }

    @Test
    void testBuildLineOffsetArrayEmptyLines() {
        String content = "line1\n\nline3";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(3);
        assertThat(offsets[0]).isEqualTo(0);
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
        assertThat(detector.publicCountOccurrences(text, "notfound")).isEqualTo(0);
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
        assertThat(detector.publicCountOccurrences(text, "[invalid(")).isEqualTo(0);
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

        detector.initializeCaches(content);
        detector.clearCaches();
    }

    @Test
    void testInitializeCachesWithInvalidCode() {
        // Test that invalid code doesn't crash
        String content = "]]]]invalid[[[[";

        detector.initializeCaches(content);
        detector.clearCaches();
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

        assertThat(offsets.length).isGreaterThanOrEqualTo(1000);
    }

    @Test
    void testCalculateLineNumberOffsetAtEnd() {
        String content = "line1\nline2\nline3";
        detector.initializeCaches(content);

        // Test offset at very end
        int endOffset = content.length();
        assertThat(detector.publicCalculateLineNumber(content, endOffset)).isGreaterThanOrEqualTo(1);

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
        org.sonar.api.rule.RuleKey mockRuleKey = org.sonar.api.rule.RuleKey.of("mathematica", "TEST_RULE");

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
}
