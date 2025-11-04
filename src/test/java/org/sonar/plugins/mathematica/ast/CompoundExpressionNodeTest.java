package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.mathematica.ast.AstNode.NodeType;


import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CompoundExpressionNodeTest {

    @Test
    void testEmptyCompoundExpression() {
        CompoundExpressionNode node = new CompoundExpressionNode(null, false, 1, 1, 1, 2);

        assertThat(node.getExpressions()).isEmpty();
        assertThat(node.getExpressionCount()).isZero();
        assertThat(node.isSuppressOutput()).isFalse();
        assertThat(node.getType()).isEqualTo(NodeType.COMPOUND);
    }

    @Test
    void testCompoundExpressionWithSuppressionFalse() {
        List<AstNode> exprs = Arrays.asList(
            new IdentifierNode("x", 1, 1, 1, 1),
            new IdentifierNode("y", 1, 3, 1, 3)
        );

        CompoundExpressionNode node = new CompoundExpressionNode(exprs, false, 1, 1, 1, 5);

        assertThat(node.getExpressions()).hasSize(2);
        assertThat(node.getExpressionCount()).isEqualTo(2);
        assertThat(node.isSuppressOutput()).isFalse();
    }

    @Test
    void testCompoundExpressionWithSuppressionTrue() {
        List<AstNode> exprs = Arrays.asList(
            new IdentifierNode("Print[1]", 1, 1, 1, 8),
            new IdentifierNode("Print[2]", 1, 10, 1, 18)
        );

        CompoundExpressionNode node = new CompoundExpressionNode(exprs, true, 1, 1, 1, 20);

        assertThat(node.isSuppressOutput()).isTrue();
    }

    @Test
    void testAcceptVisitor() {
        List<AstNode> exprs = Arrays.asList(
            new IdentifierNode("a", 1, 1, 1, 1),
            new IdentifierNode("b", 1, 3, 1, 3)
        );

        CompoundExpressionNode node = new CompoundExpressionNode(exprs, false, 1, 1, 1, 5);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        // Visitor should process without exceptions
    }

    @Test
    void testToString() {
        List<AstNode> exprs = Arrays.asList(
            new IdentifierNode("a", 1, 1, 1, 1)
        );

        CompoundExpressionNode node = new CompoundExpressionNode(exprs, true, 1, 1, 1, 3);
        String str = node.toString();

        assertThat(str).contains("CompoundExpression");
        assertThat(str).contains("count=1");
        assertThat(str).contains("suppress=true");
    }
}
