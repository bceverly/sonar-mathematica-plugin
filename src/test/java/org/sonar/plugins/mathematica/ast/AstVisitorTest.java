package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AstVisitorTest {

    private TestVisitor visitor;
    private List<String> visitedNodes;

    @BeforeEach
    void setUp() {
        visitedNodes = new ArrayList<>();
        visitor = new TestVisitor(visitedNodes);
    }

    @Test
    void testVisitFunctionDefNode() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 12);
        FunctionDefNode node = new FunctionDefNode("testFunc", Collections.emptyList(), body, true, 1, 1, 1, 12);

        visitor.visit(node);

        assertThat(visitedNodes).contains("FunctionDefNode:testFunc");
    }

    @Test
    void testVisitFunctionCallNode() {
        FunctionCallNode node = new FunctionCallNode("Sin", Collections.emptyList(), 1, 1, 1, 6);

        visitor.visit(node);

        assertThat(visitedNodes).contains("FunctionCallNode:Sin");
    }

    @Test
    void testVisitIdentifierNode() {
        IdentifierNode node = new IdentifierNode("myVar", 1, 1, 1, 6);

        visitor.visit(node);

        assertThat(visitedNodes).contains("IdentifierNode:myVar");
    }

    @Test
    void testVisitLiteralNode() {
        LiteralNode node = new LiteralNode(123, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 4);

        visitor.visit(node);

        assertThat(visitedNodes).contains("LiteralNode:123");
    }

    @Test
    void testVisitAssignmentNode() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);

        node.accept(visitor);

        // Default implementation calls visitChildren, which visits lhs and rhs
        assertThat(visitedNodes).contains("IdentifierNode:x", "LiteralNode:10");
    }

    @Test
    void testVisitOperatorNode() {
        IdentifierNode left = new IdentifierNode("a", 1, 1, 1, 2);
        IdentifierNode right = new IdentifierNode("b", 1, 5, 1, 6);
        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.ADD,
            left,
            right,
            "+",
            1, 1, 1, 6
        );

        node.accept(visitor);

        // Default implementation calls visitChildren
        assertThat(visitedNodes).contains("IdentifierNode:a", "IdentifierNode:b");
    }

    @Test
    void testVisitListNode() {
        List<AstNode> elements = Arrays.asList(
            new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 2, 1, 3),
            new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6)
        );
        ListNode node = new ListNode(elements, 1, 1, 1, 7);

        node.accept(visitor);

        // Default implementation calls visitChildren
        assertThat(visitedNodes).contains("LiteralNode:1", "LiteralNode:2");
    }

    @Test
    void testVisitAssociationNode() {
        AstNode key = new IdentifierNode("name", 1, 1, 1, 5);
        AstNode value = new LiteralNode("John", LiteralNode.LiteralType.STRING, 1, 7, 1, 13);
        List<AssociationNode.KeyValuePair> pairs = Collections.singletonList(
            new AssociationNode.KeyValuePair(key, value)
        );
        AssociationNode node = new AssociationNode(pairs, 1, 1, 1, 14);

        node.accept(visitor);

        // Default implementation calls visitChildren
        assertThat(visitedNodes).contains("IdentifierNode:name", "LiteralNode:John");
    }

    @Test
    void testVisitControlFlowNode() {
        AstNode condition = new IdentifierNode("x > 0", 1, 4, 1, 9);
        List<AstNode> branches = Collections.singletonList(
            new IdentifierNode("then", 1, 11, 1, 15)
        );
        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            condition,
            branches,
            null,
            1, 1, 1, 16
        );

        node.accept(visitor);

        // Default implementation calls visitChildren
        assertThat(visitedNodes).contains("IdentifierNode:x > 0", "IdentifierNode:then");
    }

    @Test
    void testVisitLoopNode() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 20);
        AstNode start = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11);
        AstNode end = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 13, 1, 15);
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

        node.accept(visitor);

        // Default implementation calls visitChildren
        assertThat(visitedNodes).contains("LiteralNode:1", "LiteralNode:10", "IdentifierNode:Print[i]");
    }

    @Test
    void testVisitScopingNode() {
        List<String> vars = Arrays.asList("x", "y");
        List<AstNode> initializers = Arrays.asList(
            new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11),
            new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 1, 13, 1, 14)
        );
        AstNode body = new IdentifierNode("x + y", 1, 17, 1, 22);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            vars,
            initializers,
            body,
            1, 1, 1, 24
        );

        node.accept(visitor);

        // Default implementation calls visitChildren
        assertThat(visitedNodes).contains("LiteralNode:1", "LiteralNode:2", "IdentifierNode:x + y");
    }

    @Test
    void testVisitChildrenWithEmptyList() {
        LiteralNode node = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 3);

        visitor.visitChildren(node);

        // LiteralNode has no children, so visitedNodes should be empty
        assertThat(visitedNodes).isEmpty();
    }

    @Test
    void testVisitChildrenWithMultipleChildren() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 7);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 7);

        visitor.visitChildren(node);

        assertThat(visitedNodes)
            .hasSize(2)
            .contains("IdentifierNode:x", "LiteralNode:10");
    }

    @Test
    void testVisitChildrenCalledByDefaultImplementations() {
        IdentifierNode left = new IdentifierNode("a", 1, 1, 1, 2);
        IdentifierNode right = new IdentifierNode("b", 1, 5, 1, 6);
        OperatorNode node = new OperatorNode(
            OperatorNode.OperatorType.MULTIPLY,
            left,
            right,
            "*",
            1, 1, 1, 6
        );

        // Call the default visit method for OperatorNode via accept
        node.accept(visitor);

        // Should have visited both children
        assertThat(visitedNodes).contains("IdentifierNode:a", "IdentifierNode:b");
    }

    @Test
    void testNestedVisitorCalls() {
        // Create nested structure: FunctionDef with FunctionCall body
        FunctionCallNode bodyCall = new FunctionCallNode("Sin", Collections.emptyList(), 1, 10, 1, 15);
        FunctionDefNode node = new FunctionDefNode("wrapper", Collections.emptyList(), bodyCall, true, 1, 1, 1, 15);

        node.accept(visitor);

        assertThat(visitedNodes).contains("FunctionDefNode:wrapper", "FunctionCallNode:Sin");
    }

    @Test
    void testVisitMultipleNodesInSequence() {
        IdentifierNode node1 = new IdentifierNode("var1", 1, 1, 1, 5);
        IdentifierNode node2 = new IdentifierNode("var2", 2, 1, 2, 5);
        IdentifierNode node3 = new IdentifierNode("var3", 3, 1, 3, 5);

        visitor.visit(node1);
        visitor.visit(node2);
        visitor.visit(node3);

        assertThat(visitedNodes)
            .hasSize(3)
            .containsExactly(
                "IdentifierNode:var1",
                "IdentifierNode:var2",
                "IdentifierNode:var3"
            );
    }

    @Test
    void testVisitWithMockVisitor() {
        AstVisitor mockVisitor = mock(AstVisitor.class);
        FunctionCallNode node = new FunctionCallNode("Test", Collections.emptyList(), 1, 1, 1, 7);

        node.accept(mockVisitor);

        verify(mockVisitor).visit(node);
    }

    @Test
    void testDefaultImplementationsCallVisitChildren() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6);
        AssignmentNode node = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 6);

        // Default implementation should call visitChildren
        node.accept(visitor);

        // Verify children were visited
        assertThat(visitedNodes).contains("IdentifierNode:x", "LiteralNode:5");
    }

    @Test
    void testVisitAllNodeTypes() {
        // Test that visitor can handle all node types
        FunctionDefNode funcDef = new FunctionDefNode("f", Collections.emptyList(), null, true, 1, 1, 1, 5);
        FunctionCallNode funcCall = new FunctionCallNode("g", Collections.emptyList(), 2, 1, 2, 5);
        IdentifierNode identifier = new IdentifierNode("x", 3, 1, 3, 2);
        LiteralNode literal = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 4, 1, 4, 3);

        visitor.visit(funcDef);
        visitor.visit(funcCall);
        visitor.visit(identifier);
        visitor.visit(literal);

        assertThat(visitedNodes).hasSize(4);
    }

    @Test
    void testVisitComplexNestedStructure() {
        // Create: f[x_] := Module[{y = 1}, x + y]
        LiteralNode init = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 20, 1, 21);
        IdentifierNode body = new IdentifierNode("x + y", 1, 24, 1, 29);
        ScopingNode moduleNode = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            Collections.singletonList("y"),
            Collections.singletonList(init),
            body,
            1, 15, 1, 30
        );
        FunctionDefNode funcDef = new FunctionDefNode("f", Collections.singletonList("x_"), moduleNode, true, 1, 1, 1, 30);

        funcDef.accept(visitor);

        assertThat(visitedNodes).contains(
            "FunctionDefNode:f",
            "LiteralNode:1",
            "IdentifierNode:x + y"
        );
    }

    @Test
    void testVisitEmptyListNode() {
        ListNode node = new ListNode(null, 1, 1, 1, 3);

        node.accept(visitor);

        // Empty list has no children to visit
        assertThat(visitedNodes).isEmpty();
    }

    @Test
    void testVisitEmptyAssociationNode() {
        AssociationNode node = new AssociationNode(null, 1, 1, 1, 5);

        node.accept(visitor);

        // Empty association has no children to visit
        assertThat(visitedNodes).isEmpty();
    }

    @Test
    void testVisitControlFlowWithElseClause() {
        AstNode condition = new IdentifierNode("x > 0", 1, 4, 1, 9);
        List<AstNode> branches = Collections.singletonList(
            new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 11, 1, 12)
        );
        AstNode elseClause = new LiteralNode(0, LiteralNode.LiteralType.INTEGER, 1, 14, 1, 15);

        ControlFlowNode node = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            condition,
            branches,
            elseClause,
            1, 1, 1, 16
        );

        node.accept(visitor);

        assertThat(visitedNodes).contains("IdentifierNode:x > 0", "LiteralNode:1", "LiteralNode:0");
    }

    @Test
    void testVisitLoopWithStep() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 25);
        AstNode start = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11);
        AstNode end = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 13, 1, 15);
        AstNode step = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 1, 17, 1, 18);
        AstNode body = new IdentifierNode("body", 1, 20, 1, 24);

        LoopNode node = new LoopNode(
            LoopNode.LoopType.DO,
            "i",
            start,
            end,
            step,
            body,
            loc
        );

        node.accept(visitor);

        assertThat(visitedNodes).contains("LiteralNode:1", "LiteralNode:10", "LiteralNode:2", "IdentifierNode:body");
    }

    @Test
    void testVisitFunctionCallWithArguments() {
        List<AstNode> args = Arrays.asList(
            new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6),
            new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9)
        );
        FunctionCallNode node = new FunctionCallNode("Plus", args, 1, 1, 1, 10);

        node.accept(visitor);

        // FunctionCallNode visit is abstract, must be implemented
        assertThat(visitedNodes).contains("FunctionCallNode:Plus");
    }

    @Test
    void testVisitDifferentLiteralTypes() {
        LiteralNode intNode = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 3);
        LiteralNode strNode = new LiteralNode("hello", LiteralNode.LiteralType.STRING, 2, 1, 2, 8);
        LiteralNode realNode = new LiteralNode(3.14, LiteralNode.LiteralType.REAL, 3, 1, 3, 5);
        LiteralNode boolNode = new LiteralNode(true, LiteralNode.LiteralType.BOOLEAN, 4, 1, 4, 5);

        visitor.visit(intNode);
        visitor.visit(strNode);
        visitor.visit(realNode);
        visitor.visit(boolNode);

        assertThat(visitedNodes).contains(
            "LiteralNode:42",
            "LiteralNode:hello",
            "LiteralNode:3.14",
            "LiteralNode:true"
        );
    }

    @Test
    void testVisitOperatorTypes() {
        IdentifierNode left = new IdentifierNode("x", 1, 1, 1, 2);
        IdentifierNode right = new IdentifierNode("y", 1, 5, 1, 6);

        OperatorNode addNode = new OperatorNode(OperatorNode.OperatorType.ADD, left, right, "+", 1, 1, 1, 6);
        OperatorNode mulNode = new OperatorNode(OperatorNode.OperatorType.MULTIPLY, left, right, "*", 2, 1, 2, 6);

        addNode.accept(visitor);
        visitedNodes.clear();
        mulNode.accept(visitor);

        assertThat(visitedNodes).contains("IdentifierNode:x", "IdentifierNode:y");
    }

    @Test
    void testVisitScopingTypes() {
        AstNode body = new IdentifierNode("body", 1, 10, 1, 14);

        ScopingNode moduleNode = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            Collections.singletonList("x"),
            null,
            body,
            1, 1, 1, 15
        );

        ScopingNode blockNode = new ScopingNode(
            ScopingNode.ScopingType.BLOCK,
            Collections.singletonList("y"),
            null,
            body,
            2, 1, 2, 15
        );

        ScopingNode withNode = new ScopingNode(
            ScopingNode.ScopingType.WITH,
            Collections.singletonList("z"),
            null,
            body,
            3, 1, 3, 15
        );

        moduleNode.accept(visitor);
        blockNode.accept(visitor);
        withNode.accept(visitor);

        // Each should visit the body
        assertThat(visitedNodes).filteredOn(s -> s.equals("IdentifierNode:body")).hasSize(3);
    }

    @Test
    void testVisitLoopTypes() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 20);
        AstNode condition = new IdentifierNode("cond", 1, 10, 1, 14);
        AstNode body = new IdentifierNode("body", 1, 16, 1, 20);

        LoopNode doLoop = new LoopNode(LoopNode.LoopType.DO, "i", null, condition, null, body, loc);
        LoopNode whileLoop = new LoopNode(LoopNode.LoopType.WHILE, null, null, condition, null, body, loc);
        LoopNode forLoop = new LoopNode(LoopNode.LoopType.FOR, "j", null, condition, null, body, loc);

        doLoop.accept(visitor);
        whileLoop.accept(visitor);
        forLoop.accept(visitor);

        // Each should visit condition and body
        assertThat(visitedNodes).filteredOn(s -> s.equals("IdentifierNode:cond")).hasSize(3);
        assertThat(visitedNodes).filteredOn(s -> s.equals("IdentifierNode:body")).hasSize(3);
    }

    @Test
    void testVisitControlFlowTypes() {
        AstNode condition = new IdentifierNode("cond", 1, 4, 1, 8);
        List<AstNode> branches = Collections.singletonList(new IdentifierNode("branch", 1, 10, 1, 16));

        ControlFlowNode ifNode = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            condition,
            branches,
            null,
            1, 1, 1, 17
        );

        ControlFlowNode whichNode = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.WHICH,
            null,
            branches,
            null,
            2, 1, 2, 17
        );

        ControlFlowNode switchNode = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.SWITCH,
            condition,
            branches,
            null,
            3, 1, 3, 17
        );

        ifNode.accept(visitor);
        whichNode.accept(visitor);
        switchNode.accept(visitor);

        // Each should visit their branches
        assertThat(visitedNodes).filteredOn(s -> s.equals("IdentifierNode:branch")).hasSize(3);
    }

    @Test
    void testMultipleVisitorInstances() {
        List<String> visited1 = new ArrayList<>();
        List<String> visited2 = new ArrayList<>();
        TestVisitor visitor1 = new TestVisitor(visited1);
        TestVisitor visitor2 = new TestVisitor(visited2);

        IdentifierNode node = new IdentifierNode("shared", 1, 1, 1, 7);

        visitor1.visit(node);
        visitor2.visit(node);

        assertThat(visited1).containsExactly("IdentifierNode:shared");
        assertThat(visited2).containsExactly("IdentifierNode:shared");
    }

    @Test
    void testVisitorStateMaintainedAcrossVisits() {
        IdentifierNode node1 = new IdentifierNode("first", 1, 1, 1, 6);
        IdentifierNode node2 = new IdentifierNode("second", 2, 1, 2, 7);

        visitor.visit(node1);
        int sizeAfterFirst = visitedNodes.size();
        visitor.visit(node2);
        int sizeAfterSecond = visitedNodes.size();

        assertThat(sizeAfterFirst).isEqualTo(1);
        assertThat(sizeAfterSecond).isEqualTo(2);
        assertThat(visitedNodes).containsExactly("IdentifierNode:first", "IdentifierNode:second");
    }

    @Test
    void testDefaultVisitMethodsAreCallable() {
        // Create minimal visitor that doesn't override defaults
        MinimalVisitor minimalVisitor = new MinimalVisitor();

        // Test that default implementations can be called
        AssignmentNode assignmentNode = new AssignmentNode("=",
            new IdentifierNode("x", 1, 1, 1, 2),
            new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6),
            1, 1, 1, 6);
        minimalVisitor.visit(assignmentNode);

        OperatorNode operatorNode = new OperatorNode(OperatorNode.OperatorType.ADD,
            new IdentifierNode("a", 1, 1, 1, 2),
            new IdentifierNode("b", 1, 5, 1, 6),
            "+", 1, 1, 1, 6);
        minimalVisitor.visit(operatorNode);

        ListNode listNode = new ListNode(Collections.singletonList(
            new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 2, 1, 3)
        ), 1, 1, 1, 4);
        minimalVisitor.visit(listNode);

        AssociationNode assocNode = new AssociationNode(Collections.singletonList(
            new AssociationNode.KeyValuePair(
                new IdentifierNode("k", 1, 1, 1, 2),
                new LiteralNode("v", LiteralNode.LiteralType.STRING, 1, 4, 1, 7)
            )
        ), 1, 1, 1, 8);
        minimalVisitor.visit(assocNode);

        ControlFlowNode controlNode = new ControlFlowNode(
            ControlFlowNode.ControlFlowType.IF,
            new IdentifierNode("cond", 1, 1, 1, 5),
            Collections.emptyList(),
            null,
            1, 1, 1, 10);
        minimalVisitor.visit(controlNode);

        LoopNode loopNode = new LoopNode(
            LoopNode.LoopType.DO,
            "i",
            new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11),
            new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 13, 1, 15),
            null,
            new IdentifierNode("body", 1, 17, 1, 21),
            new SourceLocation(1, 1, 1, 22));
        minimalVisitor.visit(loopNode);

        ScopingNode scopingNode = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            Collections.singletonList("x"),
            null,
            new IdentifierNode("body", 1, 10, 1, 14),
            1, 1, 1, 15);
        minimalVisitor.visit(scopingNode);

        // Verify default implementations were called (by checking children were visited)
        assertThat(minimalVisitor.visitCount).isGreaterThan(0);
    }

    /**
     * Concrete test implementation of AstVisitor for testing purposes.
     */
    private static class TestVisitor implements AstVisitor {
        private final List<String> visitedNodes;

        TestVisitor(List<String> visitedNodes) {
            this.visitedNodes = visitedNodes;
        }

        @Override
        public void visit(FunctionDefNode node) {
            visitedNodes.add("FunctionDefNode:" + node.getFunctionName());
            if (node.getBody() != null) {
                node.getBody().accept(this);
            }
        }

        @Override
        public void visit(FunctionCallNode node) {
            visitedNodes.add("FunctionCallNode:" + node.getFunctionName());
        }

        @Override
        public void visit(IdentifierNode node) {
            visitedNodes.add("IdentifierNode:" + node.getName());
        }

        @Override
        public void visit(LiteralNode node) {
            visitedNodes.add("LiteralNode:" + node.getValue());
        }
    }

    /**
     * Minimal visitor that only implements abstract methods to test default implementations.
     */
    private static final class MinimalVisitor implements AstVisitor {
        int visitCount = 0;

        @Override
        public void visit(FunctionDefNode node) {
            visitCount++;
        }

        @Override
        public void visit(FunctionCallNode node) {
            visitCount++;
        }

        @Override
        public void visit(IdentifierNode node) {
            visitCount++;
        }

        @Override
        public void visit(LiteralNode node) {
            visitCount++;
        }
    }
}
