package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.mathematica.ast.AstNode.NodeType;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LoopNodeTest {

    @Test
    void testDoLoop() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 20);
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.STRING, 1, 10, 1, 11);
        AstNode end = new LiteralNode("10", LiteralNode.LiteralType.STRING, 1, 13, 1, 15);
        AstNode body = new IdentifierNode("Print[i]", 1, 17, 1, 25);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.DO,
            "i",
            start,
            end,
            null,
            body,
            loc
        );

        assertThat(node.getLoopType()).isEqualTo(LoopNode.LoopType.DO);
        assertThat(node.getIteratorVariable()).isEqualTo("i");
        assertThat(node.getStart()).isEqualTo(start);
        assertThat(node.getEnd()).isEqualTo(end);
        assertThat(node.getStep()).isNull();
        assertThat(node.getBody()).isEqualTo(body);
        assertThat(node.getType()).isEqualTo(NodeType.LOOP);
    }

    @Test
    void testWhileLoop() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 20);
        AstNode condition = new IdentifierNode("x > 0", 1, 7, 1, 12);
        AstNode body = new IdentifierNode("x--", 1, 14, 1, 17);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.WHILE,
            null,
            null,
            condition,
            null,
            body,
            loc
        );

        assertThat(node.getLoopType()).isEqualTo(LoopNode.LoopType.WHILE);
        assertThat(node.getIteratorVariable()).isNull();
        assertThat(node.getEnd()).isEqualTo(condition);
    }

    @Test
    void testForLoop() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 30);
        AstNode init = new IdentifierNode("i=0", 1, 5, 1, 8);
        AstNode test = new IdentifierNode("i<10", 1, 10, 1, 14);
        AstNode incr = new IdentifierNode("i++", 1, 16, 1, 19);
        AstNode body = new IdentifierNode("body", 1, 21, 1, 25);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.FOR,
            "i",
            init,
            test,
            incr,
            body,
            loc
        );

        assertThat(node.getLoopType()).isEqualTo(LoopNode.LoopType.FOR);
        assertThat(node.getStart()).isEqualTo(init);
        assertThat(node.getEnd()).isEqualTo(test);
        assertThat(node.getStep()).isEqualTo(incr);
    }

    @Test
    void testTableLoop() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 25);
        AstNode start = new LiteralNode("1", LiteralNode.LiteralType.STRING, 1, 10, 1, 11);
        AstNode end = new LiteralNode("10", LiteralNode.LiteralType.STRING, 1, 13, 1, 15);
        AstNode expr = new IdentifierNode("i^2", 1, 7, 1, 10);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.TABLE,
            "i",
            start,
            end,
            null,
            expr,
            loc
        );

        assertThat(node.getLoopType()).isEqualTo(LoopNode.LoopType.TABLE);
        assertThat(node.getIteratorVariable()).isEqualTo("i");
    }

    @Test
    void testNestWhileLoop() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 30);
        AstNode func = new IdentifierNode("f", 1, 11, 1, 12);
        AstNode expr = new IdentifierNode("x0", 1, 14, 1, 16);
        AstNode test = new IdentifierNode("test", 1, 18, 1, 22);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.NEST_WHILE,
            null,
            expr,
            test,
            null,
            func,
            loc
        );

        assertThat(node.getLoopType()).isEqualTo(LoopNode.LoopType.NEST_WHILE);
    }

    @Test
    void testAcceptVisitor() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 20);
        AstNode start = new IdentifierNode("start", 1, 1, 1, 5);
        AstNode end = new IdentifierNode("end", 1, 7, 1, 10);
        AstNode step = new IdentifierNode("step", 1, 12, 1, 16);
        AstNode body = new IdentifierNode("body", 1, 18, 1, 22);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.FOR,
            "i",
            start,
            end,
            step,
            body,
            loc
        );

        AstVisitor visitor = mock(AstVisitor.class);
        node.accept(visitor);

        // Should visit start, end, step, and body
        // Visitor should process without exceptions
    }

    @Test
    void testToString() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 10);
        AstNode body = new IdentifierNode("body", 1, 5, 1, 9);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.DO,
            "i",
            null,
            null,
            null,
            body,
            loc
        );

        String str = node.toString();
        assertThat(str)
            .contains("DO")
            .contains("i")
            .contains("body");
    }

    @Test
    void testNullBodyHandling() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 10);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.WHILE,
            null,
            null,
            new IdentifierNode("true", 1, 1, 1, 4),
            null,
            null,
            loc
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should not crash with null body
        node.accept(visitor);
    }
}
