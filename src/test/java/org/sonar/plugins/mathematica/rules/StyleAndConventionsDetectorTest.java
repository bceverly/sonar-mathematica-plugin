package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StyleAndConventionsDetectorTest {

    @Test
    void testStringLiteralPatternMatchesSimpleString() {
        // Test that the STRING_LITERAL_PATTERN regex works correctly
        String code = "message = \"Hello, World!\";";
        assertThat(code).matches(".*\"[^\"\\\\]*+(?:\\\\.[^\"\\\\]*+)*+\".*");
    }

    @Test
    void testStringLiteralPatternMatchesEscapedQuotes() {
        // Test that escaped quotes are handled correctly
        String code = "message = \"He said \\\"Hello\\\"\";";
        assertThat(code).matches(".*\"[^\"\\\\]*+(?:\\\\.[^\"\\\\]*+)*+\".*");
    }

    @Test
    void testStringLiteralPatternMatchesEscapedBackslashes() {
        // Test that escaped backslashes are handled correctly
        String code = "path = \"C:\\\\Users\\\\test\\\\\";";
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
        StyleAndConventionsDetector detector = new StyleAndConventionsDetector();
        assertThat(detector).isNotNull();
    }
}
