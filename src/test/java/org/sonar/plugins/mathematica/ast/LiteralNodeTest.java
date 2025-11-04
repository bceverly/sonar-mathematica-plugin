package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LiteralNodeTest {

    @Test
    void testIntegerLiteralZero() {
        LiteralNode node = new LiteralNode(0, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);

        assertThat(node.getValue()).isEqualTo(0);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testIntegerLiteralPositive() {
        LiteralNode node = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 3);

        assertThat(node.getValue()).isEqualTo(42);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testIntegerLiteralNegative() {
        LiteralNode node = new LiteralNode(-123, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 5);

        assertThat(node.getValue()).isEqualTo(-123);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testIntegerLiteralLarge() {
        LiteralNode node = new LiteralNode(1000000, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 8);

        assertThat(node.getValue()).isEqualTo(1000000);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testIntegerLiteralString() {
        LiteralNode node = new LiteralNode("5", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);

        assertThat(node.getValue()).isEqualTo("5");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testRealLiteralSimple() {
        LiteralNode node = new LiteralNode(3.14, LiteralNode.LiteralType.REAL, 1, 1, 1, 5);

        assertThat(node.getValue()).isEqualTo(3.14);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.REAL);
    }

    @Test
    void testRealLiteralZero() {
        LiteralNode node = new LiteralNode(0.0, LiteralNode.LiteralType.REAL, 1, 1, 1, 4);

        assertThat(node.getValue()).isEqualTo(0.0);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.REAL);
    }

    @Test
    void testRealLiteralNegative() {
        LiteralNode node = new LiteralNode(-2.718, LiteralNode.LiteralType.REAL, 1, 1, 1, 7);

        assertThat(node.getValue()).isEqualTo(-2.718);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.REAL);
    }

    @Test
    void testRealLiteralScientific() {
        LiteralNode node = new LiteralNode(1.23e-5, LiteralNode.LiteralType.REAL, 1, 1, 1, 9);

        assertThat(node.getValue()).isEqualTo(1.23e-5);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.REAL);
    }

    @Test
    void testRealLiteralString() {
        LiteralNode node = new LiteralNode("3.14159", LiteralNode.LiteralType.REAL, 1, 1, 1, 8);

        assertThat(node.getValue()).isEqualTo("3.14159");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.REAL);
    }

    @Test
    void testStringLiteralEmpty() {
        LiteralNode node = new LiteralNode("", LiteralNode.LiteralType.STRING, 1, 1, 1, 3);

        assertThat(node.getValue()).isEqualTo("");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
    }

    @Test
    void testStringLiteralSimple() {
        LiteralNode node = new LiteralNode("hello", LiteralNode.LiteralType.STRING, 1, 1, 1, 8);

        assertThat(node.getValue()).isEqualTo("hello");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
    }

    @Test
    void testStringLiteralWithSpaces() {
        LiteralNode node = new LiteralNode("hello world", LiteralNode.LiteralType.STRING, 1, 1, 1, 14);

        assertThat(node.getValue()).isEqualTo("hello world");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
    }

    @Test
    void testStringLiteralWithNumbers() {
        LiteralNode node = new LiteralNode("test123", LiteralNode.LiteralType.STRING, 1, 1, 1, 10);

        assertThat(node.getValue()).isEqualTo("test123");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
    }

    @Test
    void testStringLiteralWithSpecialChars() {
        LiteralNode node = new LiteralNode("test\n\t\"quoted\"", LiteralNode.LiteralType.STRING, 1, 1, 1, 20);

        assertThat(node.getValue()).isEqualTo("test\n\t\"quoted\"");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
    }

    @Test
    void testStringLiteralUnicode() {
        LiteralNode node = new LiteralNode("hello α β γ", LiteralNode.LiteralType.STRING, 1, 1, 1, 15);

        assertThat(node.getValue()).isEqualTo("hello α β γ");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
    }

    @Test
    void testBooleanLiteralTrue() {
        LiteralNode node = new LiteralNode(true, LiteralNode.LiteralType.BOOLEAN, 1, 1, 1, 5);

        assertThat(node.getValue()).isEqualTo(true);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.BOOLEAN);
    }

    @Test
    void testBooleanLiteralFalse() {
        LiteralNode node = new LiteralNode(false, LiteralNode.LiteralType.BOOLEAN, 1, 1, 1, 6);

        assertThat(node.getValue()).isEqualTo(false);
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.BOOLEAN);
    }

    @Test
    void testBooleanLiteralStringTrue() {
        LiteralNode node = new LiteralNode("True", LiteralNode.LiteralType.BOOLEAN, 1, 1, 1, 5);

        assertThat(node.getValue()).isEqualTo("True");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.BOOLEAN);
    }

    @Test
    void testBooleanLiteralStringFalse() {
        LiteralNode node = new LiteralNode("False", LiteralNode.LiteralType.BOOLEAN, 1, 1, 1, 6);

        assertThat(node.getValue()).isEqualTo("False");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.BOOLEAN);
    }

    @Test
    void testNodeType() {
        LiteralNode node = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 3);

        assertThat(node.getType()).isEqualTo(AstNode.NodeType.LITERAL);
    }

    @Test
    void testLocationInfo() {
        LiteralNode node = new LiteralNode(123, LiteralNode.LiteralType.INTEGER, 5, 10, 5, 13);

        assertThat(node.getStartLine()).isEqualTo(5);
        assertThat(node.getStartColumn()).isEqualTo(10);
        assertThat(node.getEndLine()).isEqualTo(5);
        assertThat(node.getEndColumn()).isEqualTo(13);
    }

    @Test
    void testToStringInteger() {
        LiteralNode node = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 3);

        String result = node.toString();
        assertThat(result).contains("Literal");
        assertThat(result).contains("42");
        assertThat(result).contains("INTEGER");
    }

    @Test
    void testToStringReal() {
        LiteralNode node = new LiteralNode(3.14, LiteralNode.LiteralType.REAL, 1, 1, 1, 5);

        String result = node.toString();
        assertThat(result).contains("Literal");
        assertThat(result).contains("3.14");
        assertThat(result).contains("REAL");
    }

    @Test
    void testToStringString() {
        LiteralNode node = new LiteralNode("hello", LiteralNode.LiteralType.STRING, 1, 1, 1, 8);

        String result = node.toString();
        assertThat(result).contains("Literal");
        assertThat(result).contains("hello");
        assertThat(result).contains("STRING");
    }

    @Test
    void testToStringBoolean() {
        LiteralNode node = new LiteralNode(true, LiteralNode.LiteralType.BOOLEAN, 1, 1, 1, 5);

        String result = node.toString();
        assertThat(result).contains("Literal");
        assertThat(result).contains("true");
        assertThat(result).contains("BOOLEAN");
    }

    @Test
    void testAcceptVisitor() {
        LiteralNode node = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 3);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(node);
    }

    @Test
    void testMultipleVisitorCalls() {
        LiteralNode node = new LiteralNode("test", LiteralNode.LiteralType.STRING, 1, 1, 1, 7);
        AstVisitor visitor1 = mock(AstVisitor.class);
        AstVisitor visitor2 = mock(AstVisitor.class);

        node.accept(visitor1);
        node.accept(visitor2);

        verify(visitor1).visit(node);
        verify(visitor2).visit(node);
    }

    @Test
    void testIntegerOne() {
        LiteralNode node = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);

        assertThat(node.getValue()).isEqualTo(1);
    }

    @Test
    void testIntegerMaxValue() {
        LiteralNode node = new LiteralNode(Integer.MAX_VALUE, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 11);

        assertThat(node.getValue()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void testIntegerMinValue() {
        LiteralNode node = new LiteralNode(Integer.MIN_VALUE, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 12);

        assertThat(node.getValue()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void testRealVerySmall() {
        LiteralNode node = new LiteralNode(0.000001, LiteralNode.LiteralType.REAL, 1, 1, 1, 9);

        assertThat(node.getValue()).isEqualTo(0.000001);
    }

    @Test
    void testRealVeryLarge() {
        LiteralNode node = new LiteralNode(1.0e20, LiteralNode.LiteralType.REAL, 1, 1, 1, 10);

        assertThat(node.getValue()).isEqualTo(1.0e20);
    }

    @Test
    void testRealPi() {
        LiteralNode node = new LiteralNode(Math.PI, LiteralNode.LiteralType.REAL, 1, 1, 1, 19);

        assertThat(node.getValue()).isEqualTo(Math.PI);
    }

    @Test
    void testRealE() {
        LiteralNode node = new LiteralNode(Math.E, LiteralNode.LiteralType.REAL, 1, 1, 1, 19);

        assertThat(node.getValue()).isEqualTo(Math.E);
    }

    @Test
    void testStringLiteralLong() {
        String longString = "This is a very long string that contains multiple words and sentences.";
        LiteralNode node = new LiteralNode(longString, LiteralNode.LiteralType.STRING, 1, 1, 1, 73);

        assertThat(node.getValue()).isEqualTo(longString);
    }

    @Test
    void testStringLiteralSingleChar() {
        LiteralNode node = new LiteralNode("a", LiteralNode.LiteralType.STRING, 1, 1, 1, 4);

        assertThat(node.getValue()).isEqualTo("a");
    }

    @Test
    void testStringLiteralPath() {
        LiteralNode node = new LiteralNode("/path/to/file.txt", LiteralNode.LiteralType.STRING, 1, 1, 1, 20);

        assertThat(node.getValue()).isEqualTo("/path/to/file.txt");
    }

    @Test
    void testStringLiteralUrl() {
        LiteralNode node = new LiteralNode("http://example.com", LiteralNode.LiteralType.STRING, 1, 1, 1, 21);

        assertThat(node.getValue()).isEqualTo("http://example.com");
    }

    @Test
    void testStringLiteralJson() {
        LiteralNode node = new LiteralNode("{\"key\":\"value\"}", LiteralNode.LiteralType.STRING, 1, 1, 1, 17);

        assertThat(node.getValue()).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void testStringLiteralMathematicaExpression() {
        LiteralNode node = new LiteralNode("x^2 + 2*x + 1", LiteralNode.LiteralType.STRING, 1, 1, 1, 16);

        assertThat(node.getValue()).isEqualTo("x^2 + 2*x + 1");
    }

    @Test
    void testLocationStartLine() {
        LiteralNode node = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 10, 5, 10, 6);

        assertThat(node.getStartLine()).isEqualTo(10);
    }

    @Test
    void testLocationStartColumn() {
        LiteralNode node = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 10, 15, 10, 16);

        assertThat(node.getStartColumn()).isEqualTo(15);
    }

    @Test
    void testLocationEndLine() {
        LiteralNode node = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 10, 5, 11, 2);

        assertThat(node.getEndLine()).isEqualTo(11);
    }

    @Test
    void testLocationEndColumn() {
        LiteralNode node = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 10, 5, 10, 25);

        assertThat(node.getEndColumn()).isEqualTo(25);
    }

    @Test
    void testIntegerLiteralHex() {
        LiteralNode node = new LiteralNode("0xFF", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 5);

        assertThat(node.getValue()).isEqualTo("0xFF");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testIntegerLiteralOctal() {
        LiteralNode node = new LiteralNode("0o77", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 5);

        assertThat(node.getValue()).isEqualTo("0o77");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testIntegerLiteralBinary() {
        LiteralNode node = new LiteralNode("0b1010", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 7);

        assertThat(node.getValue()).isEqualTo("0b1010");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
    }

    @Test
    void testRealLiteralPrecision() {
        LiteralNode node = new LiteralNode("1.23456789012345", LiteralNode.LiteralType.REAL, 1, 1, 1, 17);

        assertThat(node.getValue()).isEqualTo("1.23456789012345");
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.REAL);
    }

    @Test
    void testNullValue() {
        LiteralNode node = new LiteralNode(null, LiteralNode.LiteralType.STRING, 1, 1, 1, 5);

        assertThat(node.getValue()).isNull();
        assertThat(node.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
    }

    @Test
    void testComplexNumberAsString() {
        LiteralNode node = new LiteralNode("3 + 4*I", LiteralNode.LiteralType.STRING, 1, 1, 1, 10);

        assertThat(node.getValue()).isEqualTo("3 + 4*I");
    }

    @Test
    void testSymbolAsString() {
        LiteralNode node = new LiteralNode("x", LiteralNode.LiteralType.STRING, 1, 1, 1, 4);

        assertThat(node.getValue()).isEqualTo("x");
    }

    @Test
    void testMathematicaInfinity() {
        LiteralNode node = new LiteralNode("Infinity", LiteralNode.LiteralType.STRING, 1, 1, 1, 9);

        assertThat(node.getValue()).isEqualTo("Infinity");
    }

    @Test
    void testDifferentLiteralTypes() {
        LiteralNode intNode = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 3);
        LiteralNode realNode = new LiteralNode(3.14, LiteralNode.LiteralType.REAL, 2, 1, 2, 5);
        LiteralNode strNode = new LiteralNode("test", LiteralNode.LiteralType.STRING, 3, 1, 3, 7);
        LiteralNode boolNode = new LiteralNode(true, LiteralNode.LiteralType.BOOLEAN, 4, 1, 4, 5);

        assertThat(intNode.getLiteralType()).isEqualTo(LiteralNode.LiteralType.INTEGER);
        assertThat(realNode.getLiteralType()).isEqualTo(LiteralNode.LiteralType.REAL);
        assertThat(strNode.getLiteralType()).isEqualTo(LiteralNode.LiteralType.STRING);
        assertThat(boolNode.getLiteralType()).isEqualTo(LiteralNode.LiteralType.BOOLEAN);
    }
}
