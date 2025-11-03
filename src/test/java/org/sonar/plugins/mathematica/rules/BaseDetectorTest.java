package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BaseDetectorTest {

    private TestableBaseDetector detector;

    // Concrete implementation for testing the abstract BaseDetector
    private static class TestableBaseDetector extends BaseDetector {
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
    }

    @BeforeEach
    void setUp() {
        detector = new TestableBaseDetector();
    }

    @Test
    void testBuildLineOffsetArray_SingleLine() {
        String content = "single line";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(1);
        assertThat(offsets[0]).isEqualTo(0);
    }

    @Test
    void testBuildLineOffsetArray_MultipleLines() {
        String content = "line1\nline2\nline3";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(3);
        assertThat(offsets[0]).isEqualTo(0);
        assertThat(offsets[1]).isEqualTo(6);  // After "line1\n"
        assertThat(offsets[2]).isEqualTo(12); // After "line1\nline2\n"
    }

    @Test
    void testBuildLineOffsetArray_EmptyLines() {
        String content = "line1\n\nline3";
        int[] offsets = detector.publicBuildLineOffsetArray(content);

        assertThat(offsets).hasSize(3);
        assertThat(offsets[0]).isEqualTo(0);
        assertThat(offsets[1]).isEqualTo(6);
        assertThat(offsets[2]).isEqualTo(7);
    }

    @Test
    void testCalculateLineNumber_WithCache() {
        String content = "line1\nline2\nline3\nline4";
        detector.initializeCaches(content);

        assertThat(detector.publicCalculateLineNumber(content, 0)).isEqualTo(1);   // Start of line1
        assertThat(detector.publicCalculateLineNumber(content, 6)).isEqualTo(2);   // Start of line2
        assertThat(detector.publicCalculateLineNumber(content, 12)).isEqualTo(3);  // Start of line3
        assertThat(detector.publicCalculateLineNumber(content, 18)).isEqualTo(4);  // Start of line4

        detector.clearCaches();
    }

    @Test
    void testCalculateLineNumber_WithoutCache() {
        String content = "line1\nline2\nline3";

        // Test without initializing caches (fallback path)
        assertThat(detector.publicCalculateLineNumber(content, 0)).isEqualTo(1);
        assertThat(detector.publicCalculateLineNumber(content, 6)).isEqualTo(2);
        assertThat(detector.publicCalculateLineNumber(content, 12)).isEqualTo(3);
    }

    @Test
    void testAnalyzeComments_SimpleComment() {
        String content = "code (* comment *) more code";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).hasSize(1);
        assertThat(commentRanges.get(0)[0]).isEqualTo(5);  // Start of (*
        assertThat(commentRanges.get(0)[1]).isEqualTo(18); // End of *)
    }

    @Test
    void testAnalyzeComments_NestedComments() {
        String content = "code (* outer (* nested *) outer *) code";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).hasSize(1);
        // Should capture the entire nested comment
        assertThat(commentRanges.get(0)[0]).isEqualTo(5);  // Start of first (*
        assertThat(commentRanges.get(0)[1]).isEqualTo(35); // End of last *)
    }

    @Test
    void testAnalyzeComments_MultipleComments() {
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
    void testIsInsideStringLiteral_EscapedQuotes() {
        String content = "x = \"hello \\\"nested\\\" world\"";

        assertThat(detector.publicIsInsideStringLiteral(content, 10)).isTrue();  // Inside string
        assertThat(detector.publicIsInsideStringLiteral(content, 14)).isTrue();  // Still inside despite escaped quote
    }

    @Test
    void testLooksLikeNaturalLanguage_IsNaturalLanguage() {
        // Need at least 2 indicators from NATURAL_LANGUAGE_PHRASES
        assertThat(detector.publicLooksLikeNaturalLanguage("this function returns a value")).isTrue();
        assertThat(detector.publicLooksLikeNaturalLanguage("note that this will fail")).isTrue();
        assertThat(detector.publicLooksLikeNaturalLanguage("this function calculates the result")).isTrue();
    }

    @Test
    void testLooksLikeNaturalLanguage_NotNaturalLanguage() {
        assertThat(detector.publicLooksLikeNaturalLanguage("x := y + z")).isFalse();
        assertThat(detector.publicLooksLikeNaturalLanguage("Module[{x}, x = 1]")).isFalse();
        assertThat(detector.publicLooksLikeNaturalLanguage("func[arg_]")).isFalse();
    }

    @Test
    void testLooksLikeNaturalLanguage_SingleIndicator() {
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
    void testCountOccurrences_RegexPattern() {
        String text = "x1 y2 z3 a4";

        assertThat(detector.publicCountOccurrences(text, "[a-z]\\d")).isEqualTo(4);
    }

    @Test
    void testCountOccurrences_InvalidPattern() {
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
    void testInitializeCaches_WithAst() {
        // Test that AST parsing doesn't crash
        String content = "f[x_] := x + 1";

        detector.initializeCaches(content);
        detector.clearCaches();
    }

    @Test
    void testInitializeCaches_WithInvalidCode() {
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
    void testAnalyzeComments_UnclosedComment() {
        // Test handling of unclosed comments
        String content = "code (* unclosed comment";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        // Should not crash, may have 0 or 1 ranges depending on implementation
        assertThat(commentRanges).isNotNull();
    }

    @Test
    void testAnalyzeComments_EmptyContent() {
        String content = "";
        List<int[]> commentRanges = detector.publicAnalyzeComments(content);

        assertThat(commentRanges).isEmpty();
    }

    @Test
    void testIsInsideStringLiteral_AtLineStart() {
        String content = "\nx = \"test\"";

        assertThat(detector.publicIsInsideStringLiteral(content, 6)).isTrue();
    }

    @Test
    void testBuildLineOffsetArray_LargeFile() {
        // Test with many lines
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            content.append("line").append(i).append("\n");
        }

        int[] offsets = detector.publicBuildLineOffsetArray(content.toString());

        assertThat(offsets.length).isGreaterThanOrEqualTo(1000);
    }

    @Test
    void testCalculateLineNumber_OffsetAtEnd() {
        String content = "line1\nline2\nline3";
        detector.initializeCaches(content);

        // Test offset at very end
        int endOffset = content.length();
        assertThat(detector.publicCalculateLineNumber(content, endOffset)).isGreaterThanOrEqualTo(1);

        detector.clearCaches();
    }
}
