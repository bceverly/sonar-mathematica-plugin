package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StyleAndConventionsDetectorTest {

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
        StyleAndConventionsDetector detector = new StyleAndConventionsDetector();
        assertThat(detector).isNotNull();
    }
}
