package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import org.sonar.plugins.mathematica.ast.AstNode.NodeType;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PartNodeTest {

    @Test
    void testSingleIndex() {
        AstNode expression = new IdentifierNode("list", 1, 1, 1, 4);
        List<AstNode> indices = Arrays.asList(
            new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 7, 1, 8)
        );

        PartNode node = new PartNode(expression, indices, 1, 1, 1, 10);

        assertThat(node.getExpression()).isEqualTo(expression);
        assertThat(node.getIndices()).hasSize(1);
        assertThat(node.getIndexCount()).isEqualTo(1);
        assertThat(node.getType()).isEqualTo(NodeType.PART);
    }

    @Test
    void testMultipleIndices() {
        AstNode expression = new IdentifierNode("matrix", 1, 1, 1, 6);
        List<AstNode> indices = Arrays.asList(
            new LiteralNode("2", LiteralNode.LiteralType.INTEGER, 1, 9, 1, 10),
            new LiteralNode("3", LiteralNode.LiteralType.INTEGER, 1, 12, 1, 13)
        );

        PartNode node = new PartNode(expression, indices, 1, 1, 1, 15);

        assertThat(node.getIndices()).hasSize(2);
        assertThat(node.getIndexCount()).isEqualTo(2);
    }

    @Test
    void testNullIndices() {
        AstNode expression = new IdentifierNode("list", 1, 1, 1, 4);

        PartNode node = new PartNode(expression, null, 1, 1, 1, 10);

        assertThat(node.getIndices()).isEmpty();
        assertThat(node.getIndexCount()).isZero();
    }

    @Test
    void testNegativeIndex() {
        AstNode expression = new IdentifierNode("list", 1, 1, 1, 4);
        List<AstNode> indices = Arrays.asList(
            new LiteralNode("-1", LiteralNode.LiteralType.INTEGER, 1, 7, 1, 9)
        );

        PartNode node = new PartNode(expression, indices, 1, 1, 1, 11);

        assertThat(node.getIndices()).hasSize(1);
    }

    @Test
    void testAcceptVisitor() {
        AstNode expression = new IdentifierNode("data", 1, 1, 1, 4);
        List<AstNode> indices = Arrays.asList(
            new LiteralNode("5", LiteralNode.LiteralType.INTEGER, 1, 7, 1, 8)
        );

        PartNode node = new PartNode(expression, indices, 1, 1, 1, 10);

        AstVisitor visitor = mock(AstVisitor.class);
        // Should process without exceptions
        node.accept(visitor);
        assertThat(node).isNotNull();
    }

    @Test
    void testToString() {
        AstNode expression = new IdentifierNode("arr", 1, 1, 1, 3);
        List<AstNode> indices = Arrays.asList(
            new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7),
            new LiteralNode("2", LiteralNode.LiteralType.INTEGER, 1, 9, 1, 10)
        );

        PartNode node = new PartNode(expression, indices, 1, 1, 1, 12);

        String str = node.toString();
        assertThat(str).contains("2 indices");
    }

    @Test
    void testNullIndexInList() {
        AstNode expression = new IdentifierNode("list", 1, 1, 1, 4);
        List<AstNode> indices = Arrays.asList(
            new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 7, 1, 8),
            null,
            new LiteralNode("3", LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11)
        );

        PartNode node = new PartNode(expression, indices, 1, 1, 1, 13);

        AstVisitor visitor = mock(AstVisitor.class);
        // Should not crash with null index
        node.accept(visitor);

        assertThat(node.getIndexCount()).isEqualTo(3);
    }
}
