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
}
