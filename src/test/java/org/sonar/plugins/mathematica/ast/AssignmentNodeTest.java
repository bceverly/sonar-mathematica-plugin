package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AssignmentNodeTest {

    @Test
    void testSimpleAssignment() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getOperator()).isEqualTo("=");
        assertThat(node.getLhs()).isEqualTo(lhs);
        assertThat(node.getRhs()).isEqualTo(rhs);
    }

    @Test
    void testDelayedAssignment() {
        IdentifierNode lhs = new IdentifierNode("f", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode("x^2", LiteralNode.LiteralType.STRING, 1, 6, 1, 9);
        AssignmentNode node = new AssignmentNode(":=", lhs, rhs, 1, 1, 1, 9);

        assertThat(node.getOperator()).isEqualTo(":=");
        assertThat(node.getLhs()).isEqualTo(lhs);
        assertThat(node.getRhs()).isEqualTo(rhs);
    }

    @Test
    void testUpSetAssignment() {
        IdentifierNode lhs = new IdentifierNode("g", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 8);
        AssignmentNode node = new AssignmentNode("^=", lhs, rhs, 1, 1, 1, 8);

        assertThat(node.getOperator()).isEqualTo("^=");
    }

    @Test
    void testTagSetAssignment() {
        IdentifierNode lhs = new IdentifierNode("h", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(20, LiteralNode.LiteralType.INTEGER, 1, 7, 1, 9);
        AssignmentNode node = new AssignmentNode("/:=", lhs, rhs, 1, 1, 1, 9);

        assertThat(node.getOperator()).isEqualTo("/:=");
    }

    @Test
    void testAssignmentWithNullLhs() {
        LiteralNode rhs = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 3, 1, 4);
        AssignmentNode node = new AssignmentNode("=", null, rhs, 1, 1, 1, 4);

        assertThat(node.getLhs()).isNull();
        assertThat(node.getRhs()).isEqualTo(rhs);
    }

    @Test
    void testAssignmentWithNullRhs() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        AssignmentNode node = new AssignmentNode("=", lhs, null, 1, 1, 1, 2);

        assertThat(node.getLhs()).isEqualTo(lhs);
        assertThat(node.getRhs()).isNull();
    }

    @Test
    void testAssignmentWithBothNull() {
        AssignmentNode node = new AssignmentNode("=", null, null, 1, 1, 1, 2);

        assertThat(node.getLhs()).isNull();
        assertThat(node.getRhs()).isNull();
    }

    @Test
    void testNodeType() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getType()).isEqualTo(AstNode.NodeType.OPERATOR);
    }

    @Test
    void testLocationInfo() {
        IdentifierNode lhs = new IdentifierNode("x", 5, 10, 5, 11);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 5, 14, 5, 16);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 5, 10, 5, 16);

        assertThat(node.getStartLine()).isEqualTo(5);
        assertThat(node.getStartColumn()).isEqualTo(10);
        assertThat(node.getEndLine()).isEqualTo(5);
        assertThat(node.getEndColumn()).isEqualTo(16);
    }

    @Test
    void testToString() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);

        String result = node.toString();
        assertThat(result).contains("Assignment");
        assertThat(result).contains("=");
    }

    @Test
    void testToStringDelayed() {
        IdentifierNode lhs = new IdentifierNode("f", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7);
        AssignmentNode node = new AssignmentNode(":=", lhs, rhs, 1, 1, 1, 7);

        String result = node.toString();
        assertThat(result).contains(":=");
    }

    @Test
    void testAcceptVisitor() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(lhs);
        verify(visitor).visit(rhs);
    }

    @Test
    void testAcceptVisitorWithNullLhs() {
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", null, rhs, 1, 1, 1, 7);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(rhs);
        verify(visitor, times(0)).visit((IdentifierNode) null);
    }

    @Test
    void testAcceptVisitorWithNullRhs() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        AssignmentNode node = new AssignmentNode("=", lhs, null, 1, 1, 1, 2);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(lhs);
    }

    @Test
    void testChildrenAdded() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getChildren()).hasSize(2);
        assertThat(node.getChildren().get(0)).isEqualTo(lhs);
        assertThat(node.getChildren().get(1)).isEqualTo(rhs);
    }

    @Test
    void testChildrenWithNullLhs() {
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", null, rhs, 1, 1, 1, 7);

        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isEqualTo(rhs);
    }

    @Test
    void testChildrenWithNullRhs() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        AssignmentNode node = new AssignmentNode("=", lhs, null, 1, 1, 1, 2);

        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isEqualTo(lhs);
    }

    @Test
    void testAssignmentToList() {
        IdentifierNode lhs = new IdentifierNode("{a, b}", 1, 1, 1, 7);
        LiteralNode rhs = new LiteralNode("{1, 2}", LiteralNode.LiteralType.STRING, 1, 11, 1, 17);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 17);

        assertThat(node.getLhs()).isEqualTo(lhs);
        assertThat(node.getRhs()).isEqualTo(rhs);
    }

    @Test
    void testAssignmentToPattern() {
        IdentifierNode lhs = new IdentifierNode("x_", 1, 1, 1, 3);
        LiteralNode rhs = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 7, 1, 8);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 8);

        assertThat(node.getLhs().toString()).contains("x_");
    }

    @Test
    void testCompoundAssignment() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 8);
        AssignmentNode node = new AssignmentNode("+=", lhs, rhs, 1, 1, 1, 8);

        assertThat(node.getOperator()).isEqualTo("+=");
    }

    @Test
    void testSubtractAssignment() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7);
        AssignmentNode node = new AssignmentNode("-=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getOperator()).isEqualTo("-=");
    }

    @Test
    void testMultiplyAssignment() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7);
        AssignmentNode node = new AssignmentNode("*=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getOperator()).isEqualTo("*=");
    }

    @Test
    void testDivideAssignment() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7);
        AssignmentNode node = new AssignmentNode("/=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getOperator()).isEqualTo("/=");
    }

    @Test
    void testAssignmentWithFunctionCall() {
        IdentifierNode lhs = new IdentifierNode("result", 1, 1, 1, 7);
        FunctionCallNode rhs = new FunctionCallNode("Sin", null, 1, 11, 1, 16);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 16);

        assertThat(node.getRhs()).isInstanceOf(FunctionCallNode.class);
    }

    @Test
    void testNestedAssignment() {
        IdentifierNode innerLhs = new IdentifierNode("y", 1, 1, 1, 2);
        LiteralNode innerRhs = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7);
        AssignmentNode innerAssignment = new AssignmentNode("=", innerLhs, innerRhs, 1, 1, 1, 7);

        IdentifierNode outerLhs = new IdentifierNode("x", 1, 9, 1, 10);
        AssignmentNode outerAssignment = new AssignmentNode("=", outerLhs, innerAssignment, 1, 9, 1, 16);

        assertThat(outerAssignment.getRhs()).isInstanceOf(AssignmentNode.class);
    }

    @Test
    void testAssignmentAtDifferentLocation() {
        IdentifierNode lhs = new IdentifierNode("x", 10, 5, 10, 6);
        LiteralNode rhs = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 10, 9, 10, 11);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 10, 5, 10, 11);

        assertThat(node.getStartLine()).isEqualTo(10);
        assertThat(node.getEndLine()).isEqualTo(10);
    }

    @Test
    void testMultilineAssignment() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode("long value", LiteralNode.LiteralType.STRING, 2, 1, 2, 12);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 2, 12);

        assertThat(node.getStartLine()).isEqualTo(1);
        assertThat(node.getEndLine()).isEqualTo(2);
    }

    @Test
    void testSetDelayedOperator() {
        IdentifierNode lhs = new IdentifierNode("f", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode("body", LiteralNode.LiteralType.STRING, 1, 7, 1, 13);
        AssignmentNode node = new AssignmentNode(":=", lhs, rhs, 1, 1, 1, 13);

        assertThat(node.getOperator()).isEqualTo(":=");
    }

    @Test
    void testTagSet() {
        IdentifierNode lhs = new IdentifierNode("expr", 1, 1, 1, 5);
        LiteralNode rhs = new LiteralNode("value", LiteralNode.LiteralType.STRING, 1, 10, 1, 17);
        AssignmentNode node = new AssignmentNode("/:", lhs, rhs, 1, 1, 1, 17);

        assertThat(node.getOperator()).isEqualTo("/:");
    }

    @Test
    void testUpSet() {
        IdentifierNode lhs = new IdentifierNode("g", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(100, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 9);
        AssignmentNode node = new AssignmentNode("^=", lhs, rhs, 1, 1, 1, 9);

        assertThat(node.getOperator()).isEqualTo("^=");
    }

    @Test
    void testUpSetDelayed() {
        IdentifierNode lhs = new IdentifierNode("h", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode("expr", LiteralNode.LiteralType.STRING, 1, 7, 1, 13);
        AssignmentNode node = new AssignmentNode("^:=", lhs, rhs, 1, 1, 1, 13);

        assertThat(node.getOperator()).isEqualTo("^:=");
    }

    @Test
    void testPartAssignment() {
        IdentifierNode lhs = new IdentifierNode("list[[1]]", 1, 1, 1, 10);
        LiteralNode rhs = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 14, 1, 16);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 16);

        assertThat(node.getLhs().toString()).contains("list");
    }

    @Test
    void testAssignmentWithRealNumber() {
        IdentifierNode lhs = new IdentifierNode("pi", 1, 1, 1, 3);
        LiteralNode rhs = new LiteralNode(3.14159, LiteralNode.LiteralType.REAL, 1, 7, 1, 14);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 14);

        assertThat(node.getRhs().toString()).contains("3.14159");
    }

    @Test
    void testAssignmentWithString() {
        IdentifierNode lhs = new IdentifierNode("msg", 1, 1, 1, 4);
        LiteralNode rhs = new LiteralNode("Hello", LiteralNode.LiteralType.STRING, 1, 8, 1, 15);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 15);

        assertThat(((LiteralNode) node.getRhs()).getValue()).isEqualTo("Hello");
    }

    @Test
    void testAssignmentWithBoolean() {
        IdentifierNode lhs = new IdentifierNode("flag", 1, 1, 1, 5);
        LiteralNode rhs = new LiteralNode(true, LiteralNode.LiteralType.BOOLEAN, 1, 9, 1, 13);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 13);

        assertThat(((LiteralNode) node.getRhs()).getValue()).isEqualTo(true);
    }

    @Test
    void testMultipleAssignmentsInSequence() {
        IdentifierNode lhs1 = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs1 = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6);
        AssignmentNode assign1 = new AssignmentNode("=", lhs1, rhs1, 1, 1, 1, 6);

        IdentifierNode lhs2 = new IdentifierNode("y", 2, 1, 2, 2);
        LiteralNode rhs2 = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 2, 5, 2, 6);
        AssignmentNode assign2 = new AssignmentNode("=", lhs2, rhs2, 2, 1, 2, 6);

        assertThat(assign1.getStartLine()).isEqualTo(1);
        assertThat(assign2.getStartLine()).isEqualTo(2);
    }

    @Test
    void testOperatorGetters() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getOperator()).isNotNull();
        assertThat(node.getLhs()).isNotNull();
        assertThat(node.getRhs()).isNotNull();
    }

    @Test
    void testEmptyOperator() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getOperator()).isEmpty();
    }

    @Test
    void testLongOperator() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("/:=", lhs, rhs, 1, 1, 1, 7);

        assertThat(node.getOperator()).isEqualTo("/:=");
        assertThat(node.getOperator().length()).isEqualTo(3);
    }

    @Test
    void testAssignmentInDifferentContexts() {
        IdentifierNode lhs1 = new IdentifierNode("Global`x", 1, 1, 1, 9);
        LiteralNode rhs1 = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 13, 1, 14);
        AssignmentNode node1 = new AssignmentNode("=", lhs1, rhs1, 1, 1, 1, 14);

        IdentifierNode lhs2 = new IdentifierNode("Package`y", 2, 1, 2, 10);
        LiteralNode rhs2 = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 2, 14, 2, 15);
        AssignmentNode node2 = new AssignmentNode("=", lhs2, rhs2, 2, 1, 2, 15);

        assertThat(node1.getLhs().toString()).contains("Global");
        assertThat(node2.getLhs().toString()).contains("Package");
    }

    @Test
    void testAssignmentWithComplexRhs() {
        IdentifierNode lhs = new IdentifierNode("result", 1, 1, 1, 7);
        IdentifierNode innerLhs = new IdentifierNode("a", 1, 11, 1, 12);
        LiteralNode innerRhs = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 15, 1, 16);
        AssignmentNode innerAssign = new AssignmentNode("=", innerLhs, innerRhs, 1, 11, 1, 16);
        AssignmentNode outerAssign = new AssignmentNode("=", lhs, innerAssign, 1, 1, 1, 16);

        assertThat(outerAssign.getRhs()).isEqualTo(innerAssign);
    }

    @Test
    void testLocationStartLine() {
        IdentifierNode lhs = new IdentifierNode("x", 15, 1, 15, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 15, 5, 15, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 15, 1, 15, 7);

        assertThat(node.getStartLine()).isEqualTo(15);
    }

    @Test
    void testLocationStartColumn() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 20, 1, 21);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 24, 1, 26);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 20, 1, 26);

        assertThat(node.getStartColumn()).isEqualTo(20);
    }

    @Test
    void testLocationEndLine() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 3, 1, 3, 3);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 3, 3);

        assertThat(node.getEndLine()).isEqualTo(3);
    }

    @Test
    void testLocationEndColumn() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 30);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 30);

        assertThat(node.getEndColumn()).isEqualTo(30);
    }

    @Test
    void testMultipleVisitorAccepts() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);
        AstVisitor visitor1 = mock(AstVisitor.class);
        AstVisitor visitor2 = mock(AstVisitor.class);

        node.accept(visitor1);
        node.accept(visitor2);

        verify(visitor1).visit(lhs);
        verify(visitor1).visit(rhs);
        verify(visitor2).visit(lhs);
        verify(visitor2).visit(rhs);
    }
}
