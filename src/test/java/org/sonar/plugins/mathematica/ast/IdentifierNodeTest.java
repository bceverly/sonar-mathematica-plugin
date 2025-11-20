package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IdentifierNodeTest {

    @Test
    void testBasicIdentifier() {
        IdentifierNode node = new IdentifierNode("x", 1, 1, 1, 2);

        assertThat(node.getName()).isEqualTo("x");
        assertThat(node.getType()).isEqualTo(AstNode.NodeType.IDENTIFIER);
    }

    @Test
    void testLongIdentifierName() {
        IdentifierNode node = new IdentifierNode("myVeryLongVariableName", 1, 1, 1, 23);

        assertThat(node.getName()).isEqualTo("myVeryLongVariableName");
    }

    @Test
    void testIdentifierWithNumbers() {
        IdentifierNode node = new IdentifierNode("var123", 1, 1, 1, 7);

        assertThat(node.getName()).isEqualTo("var123");
    }

    @Test
    void testIdentifierWithUnderscores() {
        IdentifierNode node = new IdentifierNode("my_variable_name", 1, 1, 1, 17);

        assertThat(node.getName()).isEqualTo("my_variable_name");
    }

    @Test
    void testCapitalizedIdentifier() {
        IdentifierNode node = new IdentifierNode("MyFunction", 1, 1, 1, 11);

        assertThat(node.getName()).isEqualTo("MyFunction");
    }

    @Test
    void testAllCapsIdentifier() {
        IdentifierNode node = new IdentifierNode("CONSTANT", 1, 1, 1, 9);

        assertThat(node.getName()).isEqualTo("CONSTANT");
    }

    @Test
    void testSingleCharacterIdentifier() {
        IdentifierNode node = new IdentifierNode("a", 1, 1, 1, 2);

        assertThat(node.getName()).isEqualTo("a");
    }

    @Test
    void testIdentifierWithDollarSign() {
        IdentifierNode node = new IdentifierNode("$var", 1, 1, 1, 5);

        assertThat(node.getName()).isEqualTo("$var");
    }

    @Test
    void testToString() {
        IdentifierNode node = new IdentifierNode("testVar", 1, 1, 1, 8);

        String result = node.toString();
        assertThat(result).contains("Identifier", "testVar");
    }

    @Test
    void testAcceptVisitor() {
        IdentifierNode node = new IdentifierNode("x", 1, 1, 1, 2);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(node);
    }

    @Test
    void testLocationStartLine() {
        IdentifierNode node = new IdentifierNode("var", 5, 10, 5, 13);

        assertThat(node.getStartLine()).isEqualTo(5);
    }

    @Test
    void testLocationStartColumn() {
        IdentifierNode node = new IdentifierNode("var", 5, 10, 5, 13);

        assertThat(node.getStartColumn()).isEqualTo(10);
    }

    @Test
    void testLocationEndLine() {
        IdentifierNode node = new IdentifierNode("var", 5, 10, 5, 13);

        assertThat(node.getEndLine()).isEqualTo(5);
    }

    @Test
    void testLocationEndColumn() {
        IdentifierNode node = new IdentifierNode("var", 5, 10, 5, 13);

        assertThat(node.getEndColumn()).isEqualTo(13);
    }

    @Test
    void testMultipleIdentifiersWithSameName() {
        IdentifierNode node1 = new IdentifierNode("x", 1, 1, 1, 2);
        IdentifierNode node2 = new IdentifierNode("x", 2, 1, 2, 2);

        assertThat(node1.getName()).isEqualTo(node2.getName());
    }

    @Test
    void testIdentifierAtDifferentLocation() {
        IdentifierNode node = new IdentifierNode("var", 10, 20, 10, 23);

        assertThat(node.getStartLine()).isEqualTo(10);
        assertThat(node.getStartColumn()).isEqualTo(20);
    }

    @Test
    void testGreekLetterIdentifier() {
        IdentifierNode node = new IdentifierNode("α", 1, 1, 1, 2);

        assertThat(node.getName()).isEqualTo("α");
    }

    @Test
    void testIdentifierWithMixedCase() {
        IdentifierNode node = new IdentifierNode("myVarName", 1, 1, 1, 10);

        assertThat(node.getName()).isEqualTo("myVarName");
    }

    @Test
    void testIdentifierForParameter() {
        IdentifierNode node = new IdentifierNode("param_", 1, 1, 1, 7);

        assertThat(node.getName()).isEqualTo("param_");
    }

    @Test
    void testIdentifierForPattern() {
        IdentifierNode node = new IdentifierNode("x_", 1, 1, 1, 3);

        assertThat(node.getName()).isEqualTo("x_");
    }

    @Test
    void testIdentifierForBlankPattern() {
        IdentifierNode node = new IdentifierNode("x__", 1, 1, 1, 4);

        assertThat(node.getName()).isEqualTo("x__");
    }

    @Test
    void testIdentifierForBlankNullSequence() {
        IdentifierNode node = new IdentifierNode("x___", 1, 1, 1, 5);

        assertThat(node.getName()).isEqualTo("x___");
    }

    @Test
    void testBuiltinFunctionName() {
        IdentifierNode node = new IdentifierNode("Sin", 1, 1, 1, 4);

        assertThat(node.getName()).isEqualTo("Sin");
    }

    @Test
    void testBuiltinFunctionMap() {
        IdentifierNode node = new IdentifierNode("Map", 1, 1, 1, 4);

        assertThat(node.getName()).isEqualTo("Map");
    }

    @Test
    void testBuiltinFunctionTable() {
        IdentifierNode node = new IdentifierNode("Table", 1, 1, 1, 6);

        assertThat(node.getName()).isEqualTo("Table");
    }

    @Test
    void testSystemContextName() {
        IdentifierNode node = new IdentifierNode("System`Print", 1, 1, 1, 13);

        assertThat(node.getName()).isEqualTo("System`Print");
    }

    @Test
    void testCustomContextName() {
        IdentifierNode node = new IdentifierNode("MyPackage`MyFunction", 1, 1, 1, 21);

        assertThat(node.getName()).isEqualTo("MyPackage`MyFunction");
    }

    @Test
    void testNestedContextName() {
        IdentifierNode node = new IdentifierNode("Package`SubPackage`Func", 1, 1, 1, 24);

        assertThat(node.getName()).isEqualTo("Package`SubPackage`Func");
    }

    @Test
    void testGlobalContextName() {
        IdentifierNode node = new IdentifierNode("Global`var", 1, 1, 1, 11);

        assertThat(node.getName()).isEqualTo("Global`var");
    }

    @Test
    void testPrivateContextName() {
        IdentifierNode node = new IdentifierNode("MyPackage`Private`helper", 1, 1, 1, 25);

        assertThat(node.getName()).isEqualTo("MyPackage`Private`helper");
    }

    @Test
    void testIdentifierFromLoop() {
        IdentifierNode node = new IdentifierNode("i", 3, 5, 3, 6);

        assertThat(node.getName()).isEqualTo("i");
    }

    @Test
    void testIdentifierFromFunction() {
        IdentifierNode node = new IdentifierNode("result", 10, 5, 10, 11);

        assertThat(node.getName()).isEqualTo("result");
    }

    @Test
    void testTemporaryIdentifier() {
        IdentifierNode node = new IdentifierNode("temp$123", 1, 1, 1, 9);

        assertThat(node.getName()).isEqualTo("temp$123");
    }

    @Test
    void testModuleLocalVar() {
        IdentifierNode node = new IdentifierNode("x$456", 1, 1, 1, 6);

        assertThat(node.getName()).isEqualTo("x$456");
    }

    @Test
    void testSlotName() {
        IdentifierNode node = new IdentifierNode("#1", 1, 1, 1, 3);

        assertThat(node.getName()).isEqualTo("#1");
    }

    @Test
    void testSlotSequence() {
        IdentifierNode node = new IdentifierNode("##", 1, 1, 1, 3);

        assertThat(node.getName()).isEqualTo("##");
    }

    @Test
    void testOutVariable() {
        IdentifierNode node = new IdentifierNode("%", 1, 1, 1, 2);

        assertThat(node.getName()).isEqualTo("%");
    }

    @Test
    void testOutVariableNumber() {
        IdentifierNode node = new IdentifierNode("%5", 1, 1, 1, 3);

        assertThat(node.getName()).isEqualTo("%5");
    }

    @Test
    void testMessageName() {
        IdentifierNode node = new IdentifierNode("func::usage", 1, 1, 1, 12);

        assertThat(node.getName()).isEqualTo("func::usage");
    }

    @Test
    void testMessageNameCustom() {
        IdentifierNode node = new IdentifierNode("myFunc::error", 1, 1, 1, 14);

        assertThat(node.getName()).isEqualTo("myFunc::error");
    }

    @Test
    void testIdentifierWithNumbers2() {
        IdentifierNode node = new IdentifierNode("x2y3", 1, 1, 1, 5);

        assertThat(node.getName()).isEqualTo("x2y3");
    }

    @Test
    void testIdentifierCamelCase() {
        IdentifierNode node = new IdentifierNode("camelCaseVariable", 1, 1, 1, 18);

        assertThat(node.getName()).isEqualTo("camelCaseVariable");
    }

    @Test
    void testIdentifierSnakeCase() {
        IdentifierNode node = new IdentifierNode("snake_case_variable", 1, 1, 1, 20);

        assertThat(node.getName()).isEqualTo("snake_case_variable");
    }

    @Test
    void testIdentifierPascalCase() {
        IdentifierNode node = new IdentifierNode("PascalCaseFunction", 1, 1, 1, 19);

        assertThat(node.getName()).isEqualTo("PascalCaseFunction");
    }

    @Test
    void testIdentifierMixedDelimiters() {
        IdentifierNode node = new IdentifierNode("my_Var123Name", 1, 1, 1, 14);

        assertThat(node.getName()).isEqualTo("my_Var123Name");
    }

    @Test
    void testLongContextPath() {
        IdentifierNode node = new IdentifierNode("A`B`C`D`E`func", 1, 1, 1, 15);

        assertThat(node.getName()).isEqualTo("A`B`C`D`E`func");
    }

    @Test
    void testIdentifierFromAssignment() {
        IdentifierNode node = new IdentifierNode("myVar", 5, 1, 5, 6);

        assertThat(node.getName()).isEqualTo("myVar");
        assertThat(node.getStartLine()).isEqualTo(5);
    }

    @Test
    void testIdentifierFromExpression() {
        IdentifierNode node = new IdentifierNode("expr", 8, 10, 8, 14);

        assertThat(node.getName()).isEqualTo("expr");
    }

    @Test
    void testMultipleVisitorCalls() {
        IdentifierNode node = new IdentifierNode("x", 1, 1, 1, 2);
        AstVisitor visitor1 = mock(AstVisitor.class);
        AstVisitor visitor2 = mock(AstVisitor.class);

        node.accept(visitor1);
        node.accept(visitor2);

        verify(visitor1).visit(node);
        verify(visitor2).visit(node);
    }

    @Test
    void testNodeType() {
        IdentifierNode node = new IdentifierNode("test", 1, 1, 1, 5);

        assertThat(node.getType()).isEqualTo(AstNode.NodeType.IDENTIFIER);
    }

    @Test
    void testSpanningMultipleLines() {
        IdentifierNode node = new IdentifierNode("longName", 1, 50, 2, 5);

        assertThat(node.getStartLine()).isEqualTo(1);
        assertThat(node.getEndLine()).isEqualTo(2);
    }

    @Test
    void testAddChildWithNull() {
        // Test the null check in AstNode.addChild() - covers line 88
        IdentifierNode node = new IdentifierNode("parent", 1, 1, 1, 7);

        node.addChild(null);  // Should not throw, just skip adding

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    void testGetChildren() {
        // Test AstNode.getChildren() returns unmodifiable list - covers line 84
        IdentifierNode node = new IdentifierNode("parent", 1, 1, 1, 7);

        assertThat(node.getChildren()).isNotNull();
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    void testToTreeMethod() {
        // Test AstNode.toTree() method - covers lines 101-103, 105-113
        IdentifierNode node = new IdentifierNode("parent", 1, 1, 1, 7);

        String tree = node.toTree();

        assertThat(tree).isNotNull().contains("parent");
    }
}
