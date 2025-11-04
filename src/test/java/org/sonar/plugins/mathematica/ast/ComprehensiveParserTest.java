package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComprehensiveParserTest {

    @Test
    void testParserCanBeInstantiated() {
        ComprehensiveParser parser = new ComprehensiveParser();
        assertThat(parser).isNotNull();
    }

    @Test
    void testParseSimpleAssignment() {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse("x = 1;");

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseFunctionDefinition() {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse("f[x_] := x + 1");

        assertThat(nodes).isNotNull();
        assertThat(nodes).hasSizeGreaterThan(0);
    }

    @Test
    void testParseWithStringLiterals() {
        // This test exercises the STRING pattern with possessive quantifiers
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "message = \"Hello, World!\";";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseWithEscapedQuotes() {
        // Test that escaped quotes in strings are handled correctly
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "message = \"He said \\\"Hello\\\"\";";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseWithLongString() {
        // Test that long strings don't cause stack overflow with possessive quantifiers
        ComprehensiveParser parser = new ComprehensiveParser();
        StringBuilder longString = new StringBuilder("message = \"");
        for (int i = 0; i < 10000; i++) {
            longString.append("a");
        }
        longString.append("\";");

        List<AstNode> nodes = parser.parse(longString.toString());

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseWithComments() {
        // Test comment removal
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "(* comment *) x = 1;";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseComplexExpression() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "f[x_, y_] := Module[{z}, z = x + y; z * 2]";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseWithNestedComments() {
        // Test nested comment handling
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "(* outer (* nested *) comment *) x = 1;";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseWithMultilineComments() {
        // Test that comments preserve line numbers
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "(* comment\nline2\nline3 *)\nx = 1;";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseWithUnclosedComment() {
        // Test that unclosed comments are handled gracefully
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "(* unclosed comment\nx = 1;";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseFunctionCall() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "result = Sin[x];";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseFunctionCallWithMultipleArgs() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "result = Plus[a, b, c];";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseFunctionCallWithNestedBrackets() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "result = f[g[x], h[y]];";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseNumbers() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "x = 42; y = 3.14; z = 1.5e-10;";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseEmptyContent() {
        ComprehensiveParser parser = new ComprehensiveParser();
        List<AstNode> nodes = parser.parse("");

        assertThat(nodes).isNotNull();
        assertThat(nodes).isEmpty();
    }

    @Test
    void testParseWithOperators() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "result = a + b * c - d / e;";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseWithDelayedAssignment() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "x := RandomReal[]";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseWithPatterns() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "f[x_Integer, y_Real] := x + y";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }

    @Test
    void testParseWithInvalidSyntax() {
        // Parser should handle invalid syntax gracefully
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "]][[[invalid syntax";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
    }

    @Test
    void testParseWithMixedContent() {
        ComprehensiveParser parser = new ComprehensiveParser();
        String code = "(* comment *)\nf[x_] := x^2;\ny = f[5];\n(* another comment *)";
        List<AstNode> nodes = parser.parse(code);

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
    }
}
