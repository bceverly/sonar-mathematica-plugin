package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import org.sonar.plugins.mathematica.ast.AstNode.NodeType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SpanNodeTest {

    @Test
    void testFullSpan() {
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);
        AstNode end = new LiteralNode("10", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);

        SpanNode node = new SpanNode(start, end, null, 1, 1, 1, 8);

        assertThat(node.getStart()).isEqualTo(start);
        assertThat(node.getEnd()).isEqualTo(end);
        assertThat(node.getStep()).isNull();
        assertThat(node.hasStart()).isTrue();
        assertThat(node.hasEnd()).isTrue();
        assertThat(node.hasStep()).isFalse();
        assertThat(node.getType()).isEqualTo(NodeType.SPAN);
    }

    @Test
    void testSpanWithStep() {
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);
        AstNode end = new LiteralNode("10", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AstNode step = new LiteralNode("2", LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11);

        SpanNode node = new SpanNode(start, end, step, 1, 1, 1, 12);

        assertThat(node.getStart()).isEqualTo(start);
        assertThat(node.getEnd()).isEqualTo(end);
        assertThat(node.getStep()).isEqualTo(step);
        assertThat(node.hasStep()).isTrue();
    }

    @Test
    void testSpanFromBeginning() {
        AstNode end = new LiteralNode("5", LiteralNode.LiteralType.INTEGER, 1, 4, 1, 5);

        SpanNode node = new SpanNode(null, end, null, 1, 1, 1, 6);

        assertThat(node.getStart()).isNull();
        assertThat(node.getEnd()).isEqualTo(end);
        assertThat(node.hasStart()).isFalse();
        assertThat(node.hasEnd()).isTrue();
    }

    @Test
    void testSpanToEnd() {
        AstNode start = new LiteralNode("5", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);

        SpanNode node = new SpanNode(start, null, null, 1, 1, 1, 4);

        assertThat(node.getStart()).isEqualTo(start);
        assertThat(node.getEnd()).isNull();
        assertThat(node.hasStart()).isTrue();
        assertThat(node.hasEnd()).isFalse();
    }

    @Test
    void testSpanAllElements() {
        SpanNode node = new SpanNode(null, null, null, 1, 1, 1, 3);

        assertThat(node.hasStart()).isFalse();
        assertThat(node.hasEnd()).isFalse();
        assertThat(node.hasStep()).isFalse();
    }

    @Test
    void testNegativeIndices() {
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);
        AstNode end = new LiteralNode("-1", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);

        SpanNode node = new SpanNode(start, end, null, 1, 1, 1, 8);

        assertThat(node.hasStart()).isTrue();
        assertThat(node.hasEnd()).isTrue();
    }

    @Test
    void testAcceptVisitor() {
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);
        AstNode end = new LiteralNode("10", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AstNode step = new LiteralNode("2", LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11);

        SpanNode node = new SpanNode(start, end, step, 1, 1, 1, 12);

        AstVisitor visitor = mock(AstVisitor.class);
        // Should process without exceptions
        node.accept(visitor);
    }

    @Test
    void testToString() {
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);
        AstNode end = new LiteralNode("10", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);

        SpanNode node = new SpanNode(start, end, null, 1, 1, 1, 8);

        String str = node.toString();
        assertThat(str).contains("Span");
        assertThat(str).contains(";;");
    }

    @Test
    void testToStringWithStep() {
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);
        AstNode end = new LiteralNode("10", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AstNode step = new LiteralNode("3", LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11);

        SpanNode node = new SpanNode(start, end, step, 1, 1, 1, 12);

        String str = node.toString();
        assertThat(str).contains(";;");
    }

    @Test
    void testToStringWithNullStartEnd() {
        SpanNode node = new SpanNode(null, null, null, 1, 1, 1, 3);

        String str = node.toString();
        assertThat(str).contains("Span");
        assertThat(str).contains(";;");
    }
}
