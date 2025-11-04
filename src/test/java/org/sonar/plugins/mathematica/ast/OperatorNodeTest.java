package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import org.sonar.plugins.mathematica.ast.AstNode.NodeType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OperatorNodeTest {

    @Test
    void testBinaryOperator() {
        AstNode left = new IdentifierNode("a", 1, 1, 1, 1);
        AstNode right = new IdentifierNode("b", 1, 5, 1, 5);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.ADD,
            left,
            right,
            "+",
            1, 1, 1, 6
        );

        assertThat(node.getOperatorType()).isEqualTo(OperatorNode.OperatorType.ADD);
        assertThat(node.getLeftOperand()).isEqualTo(left);
        assertThat(node.getRightOperand()).isEqualTo(right);
        assertThat(node.getOperatorSymbol()).isEqualTo("+");
        assertThat(node.isBinary()).isTrue();
        assertThat(node.isUnary()).isFalse();
        assertThat(node.getType()).isEqualTo(NodeType.OPERATOR);
    }

    @Test
    void testUnaryOperator() {
        AstNode operand = new IdentifierNode("x", 1, 2, 1, 2);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.UNARY_MINUS,
            null,
            operand,
            "-",
            1, 1, 1, 3
        );

        assertThat(node.getOperatorType()).isEqualTo(OperatorNode.OperatorType.UNARY_MINUS);
        assertThat(node.getLeftOperand()).isNull();
        assertThat(node.getRightOperand()).isEqualTo(operand);
        assertThat(node.isUnary()).isTrue();
        assertThat(node.isBinary()).isFalse();
    }

    @Test
    void testComparisonOperator() {
        AstNode left = new IdentifierNode("x", 1, 1, 1, 1);
        AstNode right = new LiteralNode("10", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.GREATER_THAN,
            left,
            right,
            ">",
            1, 1, 1, 8
        );

        assertThat(node.getOperatorType()).isEqualTo(OperatorNode.OperatorType.GREATER_THAN);
        assertThat(node.isBinary()).isTrue();
    }

    @Test
    void testLogicalOperator() {
        AstNode left = new IdentifierNode("a", 1, 1, 1, 1);
        AstNode right = new IdentifierNode("b", 1, 6, 1, 6);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.AND,
            left,
            right,
            "&&",
            1, 1, 1, 7
        );

        assertThat(node.getOperatorType()).isEqualTo(OperatorNode.OperatorType.AND);
    }

    @Test
    void testMathematicaSpecialOperators() {
        AstNode left = new IdentifierNode("func", 1, 1, 1, 4);
        AstNode right = new IdentifierNode("list", 1, 9, 1, 13);

        OperatorNode mapNode = new OperatorNode(
            OperatorNode.OperatorType.MAP,
            left,
            right,
            "/@",
            1, 1, 1, 14
        );

        assertThat(mapNode.getOperatorType()).isEqualTo(OperatorNode.OperatorType.MAP);
        assertThat(mapNode.getOperatorSymbol()).isEqualTo("/@");
    }

    @Test
    void testRuleOperator() {
        AstNode left = new IdentifierNode("pattern", 1, 1, 1, 7);
        AstNode right = new IdentifierNode("replacement", 1, 12, 1, 23);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.RULE,
            left,
            right,
            "->",
            1, 1, 1, 24
        );

        assertThat(node.getOperatorType()).isEqualTo(OperatorNode.OperatorType.RULE);
    }

    @Test
    void testDelayedRuleOperator() {
        AstNode left = new IdentifierNode("pattern", 1, 1, 1, 7);
        AstNode right = new IdentifierNode("replacement", 1, 12, 1, 23);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.DELAYED_RULE,
            left,
            right,
            ":>",
            1, 1, 1, 24
        );

        assertThat(node.getOperatorType()).isEqualTo(OperatorNode.OperatorType.DELAYED_RULE);
    }

    @Test
    void testAcceptVisitor() {
        AstNode left = new IdentifierNode("x", 1, 1, 1, 1);
        AstNode right = new IdentifierNode("y", 1, 5, 1, 5);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.MULTIPLY,
            left,
            right,
            "*",
            1, 1, 1, 6
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should process without exceptions
        node.accept(visitor);
        assertThat(node).isNotNull();
    }

    @Test
    void testToStringBinary() {
        AstNode left = new IdentifierNode("a", 1, 1, 1, 1);
        AstNode right = new IdentifierNode("b", 1, 5, 1, 5);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.ADD,
            left,
            right,
            "+",
            1, 1, 1, 6
        );

        String str = node.toString();
        assertThat(str).contains("+");
    }

    @Test
    void testToStringUnary() {
        AstNode operand = new IdentifierNode("x", 1, 2, 1, 2);

        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.LOGICAL_NOT,
            null,
            operand,
            "!",
            1, 1, 1, 3
        );

        String str = node.toString();
        assertThat(str).contains("!");
    }
}
