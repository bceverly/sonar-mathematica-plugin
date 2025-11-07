package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ComprehensiveParserTest {

    static Stream<String> provideComplexCodeSamples() {
        return Stream.of(
            "f[x_, y_] := Module[{z}, z = x + y; z * 2]",
            "(* outer (* nested *) comment *) x = 1;",
            "(* comment\nline2\nline3 *)\nx = 1;",
            "(* unclosed comment\nx = 1;",
            "result = Sin[x];",
            "result = Plus[a, b, c];",
            "result = f[g[x], h[y]];",
            "x = 42; y = 3.14; z = 1.5e-10;",
            "x := RandomReal[]",
            "f[x_Integer, y_Real] := x + y",
            "]][[[invalid syntax"
        );
    }

    @Test
    void testParserCanBeInstantiated() {
        ComprehensiveParser parser = new ComprehensiveParser();
        assertThat(parser).isNotNull();
    }

    @Test
    void testParseSimpleAssignment() {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse("x = 1;");

        assertThat(nodes).isNotNull().isNotEmpty();
    }

    @Test
    void testParseFunctionDefinition() {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse("f[x_] := x + 1");

        assertThat(nodes).isNotNull().hasSizeGreaterThan(0);
    }

    static Stream<String> provideParseTestCases() {
        StringBuilder longString = new StringBuilder("message = \"");
        for (int i = 0; i < 10000; i++) {
            longString.append("a");
        }
        longString.append("\";");

        return Stream.of(
            "message = \"Hello, World!\";",  // String literals with possessive quantifiers
            "message = \"He said \\\"Hello\\\"\";",  // Escaped quotes
            longString.toString(),  // Long string (no stack overflow with possessive quantifiers)
            "(* comment *) x = 1;"  // Comment removal
        );
    }

    @ParameterizedTest
    @MethodSource("provideParseTestCases")
    void testParseVariousInputs(String code) {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("provideComplexCodeSamples")
    void testParseVariousComplexExpressions(String code) {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseEmptyContent() {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse("");

        assertThat(nodes)
            .isNotNull()
            .isEmpty();
    }

    @Test
    void testParseWithOperators() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "result = a + b * c - d / e;";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }


    @Test
    void testParseWithMixedContent() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "(* comment *)\nf[x_] := x^2;\ny = f[5];\n(* another comment *)";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes)
            .isNotNull()
            .isNotEmpty();
    }
}
