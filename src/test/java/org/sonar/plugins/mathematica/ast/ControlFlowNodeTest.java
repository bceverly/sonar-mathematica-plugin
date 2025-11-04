package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import org.sonar.plugins.mathematica.ast.AstNode.NodeType;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ControlFlowNodeTest {

    @Test
    void testIfNode() {
        AstNode condition = new IdentifierNode("x > 0", 1, 4, 1, 9);
        List<AstNode> branches = Arrays.asList(
            new IdentifierNode("then", 1, 11, 1, 15)
        );
        AstNode elseClause = new IdentifierNode("else", 1, 17, 1, 21);

        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            condition,
            branches,
            elseClause,
            1, 1, 1, 22
        );

        assertThat(node.getControlFlowType()).isEqualTo(ControlFlowNode.ControlFlowType.IF);
        assertThat(node.getCondition()).isEqualTo(condition);
        assertThat(node.getBranches()).hasSize(1);
        assertThat(node.getElseClause()).isEqualTo(elseClause);
        assertThat(node.getType()).isEqualTo(NodeType.CONTROL_FLOW);
    }

    @Test
    void testWhichNode() {
        List<AstNode> branches = Arrays.asList(
            new IdentifierNode("cond1", 1, 7, 1, 12),
            new IdentifierNode("result1", 1, 14, 1, 21),
            new IdentifierNode("cond2", 2, 7, 2, 12),
            new IdentifierNode("result2", 2, 14, 2, 21)
        );

        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.WHICH,
            null,
            branches,
            null,
            1, 1, 2, 22
        );

        assertThat(node.getControlFlowType()).isEqualTo(ControlFlowNode.ControlFlowType.WHICH);
        assertThat(node.getCondition()).isNull();
        assertThat(node.getBranches()).hasSize(4);
        assertThat(node.getElseClause()).isNull();
    }

    @Test
    void testSwitchNode() {
        AstNode expression = new IdentifierNode("x", 1, 8, 1, 9);
        List<AstNode> branches = Arrays.asList(
            new IdentifierNode("pattern1", 1, 11, 1, 19),
            new IdentifierNode("result1", 1, 21, 1, 28)
        );

        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.SWITCH,
            expression,
            branches,
            null,
            1, 1, 1, 30
        );

        assertThat(node.getControlFlowType()).isEqualTo(ControlFlowNode.ControlFlowType.SWITCH);
        assertThat(node.getCondition()).isEqualTo(expression);
    }

    @Test
    void testNullBranches() {
        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            new IdentifierNode("cond", 1, 1, 1, 4),
            null,
            null,
            1, 1, 1, 10
        );

        assertThat(node.getBranches()).isEmpty();
    }

    @Test
    void testAcceptVisitor() {
        AstNode condition = new IdentifierNode("test", 1, 1, 1, 4);
        List<AstNode> branches = Arrays.asList(
            new IdentifierNode("branch1", 1, 6, 1, 13),
            new IdentifierNode("branch2", 1, 15, 1, 22)
        );
        AstNode elseClause = new IdentifierNode("elsePart", 1, 24, 1, 32);

        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            condition,
            branches,
            elseClause,
            1, 1, 1, 35
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should process without exceptions
        node.accept(visitor);
        assertThat(node).isNotNull();
    }

    @Test
    void testToString() {
        AstNode condition = new IdentifierNode("x > 5", 1, 1, 1, 6);
        List<AstNode> branches = Arrays.asList(
            new IdentifierNode("result", 1, 8, 1, 14)
        );

        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            condition,
            branches,
            null,
            1, 1, 1, 15
        );

        String str = node.toString();
        assertThat(str).contains("IF");
        assertThat(str).contains("branches=1");
    }

    @Test
    void testNullConditionHandling() {
        List<AstNode> branches = Arrays.asList(
            new IdentifierNode("branch", 1, 1, 1, 6)
        );

        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.WHICH,
            null,
            branches,
            null,
            1, 1, 1, 10
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should not crash with null condition
        node.accept(visitor);
    }
}
