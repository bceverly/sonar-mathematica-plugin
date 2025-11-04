package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FunctionDefNodeTest {

    @Test
    void testSimpleFunctionWithNoParameters() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 12);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 12);

        assertThat(node.getFunctionName()).isEqualTo("f");
        assertThat(node.getParameters()).isEmpty();
        assertThat(node.getBody()).isEqualTo(body);
        assertThat(node.isDelayed()).isTrue();
    }

    @Test
    void testFunctionWithSingleParameter() {
        List<String> params = Collections.singletonList("x_");
        LiteralNode body = new LiteralNode("x^2", LiteralNode.LiteralType.STRING, 1, 10, 1, 13);
        FunctionDefNode node = new FunctionDefNode("square", params, body, true, 1, 1, 1, 13);

        assertThat(node.getFunctionName()).isEqualTo("square");
        assertThat(node.getParameters()).hasSize(1);
        assertThat(node.getParameters().get(0)).isEqualTo("x_");
        assertThat(node.isDelayed()).isTrue();
    }

    @Test
    void testFunctionWithMultipleParameters() {
        List<String> params = Arrays.asList("x_", "y_", "z_");
        LiteralNode body = new LiteralNode("x+y+z", LiteralNode.LiteralType.STRING, 1, 18, 1, 23);
        FunctionDefNode node = new FunctionDefNode("add3", params, body, true, 1, 1, 1, 23);

        assertThat(node.getParameters()).hasSize(3);
        assertThat(node.getParameters().get(0)).isEqualTo("x_");
        assertThat(node.getParameters().get(1)).isEqualTo("y_");
        assertThat(node.getParameters().get(2)).isEqualTo("z_");
    }

    @Test
    void testImmediateAssignment() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("constant", Collections.emptyList(), body, false, 1, 1, 1, 9);

        assertThat(node.isDelayed()).isFalse();
    }

    @Test
    void testDelayedAssignment() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("delayed", Collections.emptyList(), body, true, 1, 1, 1, 9);

        assertThat(node.isDelayed()).isTrue();
    }

    @Test
    void testFunctionWithNullBody() {
        FunctionDefNode node = new FunctionDefNode("empty", Collections.emptyList(), null, true, 1, 1, 1, 8);

        assertThat(node.getBody()).isNull();
    }

    @Test
    void testFunctionWithNullParameters() {
        LiteralNode body = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 10);
        FunctionDefNode node = new FunctionDefNode("nullParams", null, body, true, 1, 1, 1, 10);

        assertThat(node.getParameters()).isNull();
    }

    @Test
    void testNodeType() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 9);

        assertThat(node.getType()).isEqualTo(AstNode.NodeType.FUNCTION_DEF);
    }

    @Test
    void testLocationInfo() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 5, 10, 5, 11);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 5, 1, 5, 11);

        assertThat(node.getStartLine()).isEqualTo(5);
        assertThat(node.getStartColumn()).isEqualTo(1);
        assertThat(node.getEndLine()).isEqualTo(5);
        assertThat(node.getEndColumn()).isEqualTo(11);
    }

    @Test
    void testToStringWithNoParams() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 10);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 10);

        String result = node.toString();
        assertThat(result).contains("FunctionDef", "f", "delayed=true");
    }

    @Test
    void testToStringWithParams() {
        List<String> params = Arrays.asList("x_", "y_");
        LiteralNode body = new LiteralNode("x+y", LiteralNode.LiteralType.STRING, 1, 15, 1, 18);
        FunctionDefNode node = new FunctionDefNode("add", params, body, true, 1, 1, 1, 18);

        String result = node.toString();
        assertThat(result).contains("add", "x_", "y_");
    }

    @Test
    void testToStringImmediate() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("g", Collections.emptyList(), body, false, 1, 1, 1, 9);

        String result = node.toString();
        assertThat(result).contains("delayed=false");
    }

    @Test
    void testAcceptVisitor() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 9);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(node);
        verify(visitor).visit(body);
    }

    @Test
    void testAcceptVisitorWithNullBody() {
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), null, true, 1, 1, 1, 5);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(node);
        verify(visitor, times(0)).visit((LiteralNode) null);
    }

    @Test
    void testBodyAddedAsChild() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 9);

        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isEqualTo(body);
    }

    @Test
    void testNoChildrenWhenBodyNull() {
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), null, true, 1, 1, 1, 5);

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    void testFunctionWithPatternParameter() {
        List<String> params = Collections.singletonList("x_?NumericQ");
        LiteralNode body = new LiteralNode("x^2", LiteralNode.LiteralType.STRING, 1, 22, 1, 25);
        FunctionDefNode node = new FunctionDefNode("f", params, body, true, 1, 1, 1, 25);

        assertThat(node.getParameters().get(0)).isEqualTo("x_?NumericQ");
    }

    @Test
    void testFunctionWithBlankSequence() {
        List<String> params = Collections.singletonList("x__");
        LiteralNode body = new LiteralNode("Plus[x]", LiteralNode.LiteralType.STRING, 1, 12, 1, 19);
        FunctionDefNode node = new FunctionDefNode("sum", params, body, true, 1, 1, 1, 19);

        assertThat(node.getParameters().get(0)).isEqualTo("x__");
    }

    @Test
    void testFunctionWithBlankNullSequence() {
        List<String> params = Collections.singletonList("x___");
        LiteralNode body = new LiteralNode("List[x]", LiteralNode.LiteralType.STRING, 1, 13, 1, 20);
        FunctionDefNode node = new FunctionDefNode("makeList", params, body, true, 1, 1, 1, 20);

        assertThat(node.getParameters().get(0)).isEqualTo("x___");
    }

    @Test
    void testFunctionWithListParameter() {
        List<String> params = Collections.singletonList("x_List");
        LiteralNode body = new LiteralNode("Length[x]", LiteralNode.LiteralType.STRING, 1, 15, 1, 24);
        FunctionDefNode node = new FunctionDefNode("len", params, body, true, 1, 1, 1, 24);

        assertThat(node.getParameters().get(0)).isEqualTo("x_List");
    }

    @Test
    void testFunctionWithDefaultValue() {
        List<String> params = Collections.singletonList("x_:1");
        LiteralNode body = new LiteralNode("x+1", LiteralNode.LiteralType.STRING, 1, 13, 1, 16);
        FunctionDefNode node = new FunctionDefNode("inc", params, body, true, 1, 1, 1, 16);

        assertThat(node.getParameters().get(0)).isEqualTo("x_:1");
    }

    @Test
    void testFunctionWithOptionsPattern() {
        List<String> params = Collections.singletonList("opts:OptionsPattern[]");
        LiteralNode body = new LiteralNode("OptionValue[opt]", LiteralNode.LiteralType.STRING, 1, 30, 1, 46);
        FunctionDefNode node = new FunctionDefNode("withOpts", params, body, true, 1, 1, 1, 46);

        assertThat(node.getParameters().get(0)).isEqualTo("opts:OptionsPattern[]");
    }

    @Test
    void testRecursiveFunction() {
        List<String> params = Collections.singletonList("n_");
        FunctionCallNode recursiveCall = new FunctionCallNode("factorial", null, 1, 20, 1, 32);
        FunctionDefNode node = new FunctionDefNode("factorial", params, recursiveCall, true, 1, 1, 1, 32);

        assertThat(node.getFunctionName()).isEqualTo("factorial");
        assertThat(node.getBody()).isInstanceOf(FunctionCallNode.class);
    }

    @Test
    void testFunctionWithModuleBody() {
        List<String> params = Collections.singletonList("x_");
        FunctionCallNode moduleCall = new FunctionCallNode("Module", null, 1, 10, 1, 25);
        FunctionDefNode node = new FunctionDefNode("complex", params, moduleCall, true, 1, 1, 1, 25);

        assertThat(node.getBody()).isInstanceOf(FunctionCallNode.class);
    }

    @Test
    void testLongFunctionName() {
        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 40, 1, 41);
        FunctionDefNode node = new FunctionDefNode("veryLongFunctionNameForTesting", Collections.emptyList(),
                                                   body, true, 1, 1, 1, 41);

        assertThat(node.getFunctionName()).isEqualTo("veryLongFunctionNameForTesting");
    }

    @Test
    void testFunctionWithContextName() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 20, 1, 22);
        FunctionDefNode node = new FunctionDefNode("MyPackage`func", Collections.emptyList(), body, true, 1, 1, 1, 22);

        assertThat(node.getFunctionName()).isEqualTo("MyPackage`func");
    }

    @Test
    void testPrivateFunctionName() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 30, 1, 31);
        FunctionDefNode node = new FunctionDefNode("Package`Private`helper", Collections.emptyList(),
                                                   body, true, 1, 1, 1, 31);

        assertThat(node.getFunctionName()).isEqualTo("Package`Private`helper");
    }

    @Test
    void testManyParameters() {
        List<String> params = Arrays.asList("a_", "b_", "c_", "d_", "e_");
        LiteralNode body = new LiteralNode("result", LiteralNode.LiteralType.STRING, 1, 30, 1, 38);
        FunctionDefNode node = new FunctionDefNode("manyParams", params, body, true, 1, 1, 1, 38);

        assertThat(node.getParameters()).hasSize(5);
    }

    @Test
    void testSingleCharacterFunctionName() {
        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 9);

        assertThat(node.getFunctionName())
            .isEqualTo("f")
            .hasSize(1);
    }

    @Test
    void testMultilineFunction() {
        LiteralNode body = new LiteralNode("body", LiteralNode.LiteralType.STRING, 2, 1, 5, 10);
        FunctionDefNode node = new FunctionDefNode("multiline", Collections.emptyList(), body, true, 1, 1, 5, 10);

        assertThat(node.getStartLine()).isEqualTo(1);
        assertThat(node.getEndLine()).isEqualTo(5);
    }

    @Test
    void testLocationStartLine() {
        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 10, 10, 10, 11);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 10, 1, 10, 11);

        assertThat(node.getStartLine()).isEqualTo(10);
    }

    @Test
    void testLocationStartColumn() {
        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 20, 1, 21);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 15, 1, 21);

        assertThat(node.getStartColumn()).isEqualTo(15);
    }

    @Test
    void testLocationEndLine() {
        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 5, 10, 5, 11);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 5, 11);

        assertThat(node.getEndLine()).isEqualTo(5);
    }

    @Test
    void testLocationEndColumn() {
        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 30);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 30);

        assertThat(node.getEndColumn()).isEqualTo(30);
    }

    @Test
    void testMultipleVisitorCalls() {
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode node = new FunctionDefNode("f", Collections.emptyList(), body, true, 1, 1, 1, 9);
        AstVisitor visitor1 = mock(AstVisitor.class);
        AstVisitor visitor2 = mock(AstVisitor.class);

        node.accept(visitor1);
        node.accept(visitor2);

        verify(visitor1).visit(node);
        verify(visitor1).visit(body);
        verify(visitor2).visit(node);
        verify(visitor2).visit(body);
    }

    @Test
    void testFunctionGetters() {
        List<String> params = Collections.singletonList("x_");
        LiteralNode body = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11);
        FunctionDefNode node = new FunctionDefNode("test", params, body, true, 1, 1, 1, 11);

        assertThat(node.getFunctionName()).isNotNull();
        assertThat(node.getParameters()).isNotNull();
        assertThat(node.getBody()).isNotNull();
    }

    @Test
    void testEmptyFunctionName() {
        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6);
        FunctionDefNode node = new FunctionDefNode("", Collections.emptyList(), body, true, 1, 1, 1, 6);

        assertThat(node.getFunctionName()).isEmpty();
    }

    @Test
    void testFunctionWithTypedParameter() {
        List<String> params = Collections.singletonList("x_Integer");
        LiteralNode body = new LiteralNode("x*2", LiteralNode.LiteralType.STRING, 1, 18, 1, 21);
        FunctionDefNode node = new FunctionDefNode("double", params, body, true, 1, 1, 1, 21);

        assertThat(node.getParameters().get(0)).isEqualTo("x_Integer");
    }

    @Test
    void testFunctionWithSymbolParameter() {
        List<String> params = Collections.singletonList("x_Symbol");
        LiteralNode body = new LiteralNode("ToString[x]", LiteralNode.LiteralType.STRING, 1, 17, 1, 28);
        FunctionDefNode node = new FunctionDefNode("stringify", params, body, true, 1, 1, 1, 28);

        assertThat(node.getParameters().get(0)).isEqualTo("x_Symbol");
    }

    @Test
    void testFunctionWithStringParameter() {
        List<String> params = Collections.singletonList("s_String");
        LiteralNode body = new LiteralNode("StringLength[s]", LiteralNode.LiteralType.STRING, 1, 17, 1, 32);
        FunctionDefNode node = new FunctionDefNode("strLen", params, body, true, 1, 1, 1, 32);

        assertThat(node.getParameters().get(0)).isEqualTo("s_String");
    }

    @Test
    void testFunctionWithRealParameter() {
        List<String> params = Collections.singletonList("x_Real");
        LiteralNode body = new LiteralNode("Floor[x]", LiteralNode.LiteralType.STRING, 1, 14, 1, 22);
        FunctionDefNode node = new FunctionDefNode("floorReal", params, body, true, 1, 1, 1, 22);

        assertThat(node.getParameters().get(0)).isEqualTo("x_Real");
    }

    @Test
    void testIsDelayedGetter() {
        LiteralNode body1 = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9);
        FunctionDefNode delayed = new FunctionDefNode("f", Collections.emptyList(), body1, true, 1, 1, 1, 9);

        LiteralNode body2 = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 2, 8, 2, 9);
        FunctionDefNode immediate = new FunctionDefNode("g", Collections.emptyList(), body2, false, 2, 1, 2, 9);

        assertThat(delayed.isDelayed()).isTrue();
        assertThat(immediate.isDelayed()).isFalse();
    }

    @Test
    void testTwoParameterFunction() {
        List<String> params = Arrays.asList("x_", "y_");
        LiteralNode body = new LiteralNode("x+y", LiteralNode.LiteralType.STRING, 1, 15, 1, 18);
        FunctionDefNode node = new FunctionDefNode("add", params, body, true, 1, 1, 1, 18);

        assertThat(node.getParameters()).hasSize(2);
        assertThat(node.getParameters().get(0)).isEqualTo("x_");
        assertThat(node.getParameters().get(1)).isEqualTo("y_");
    }

    @Test
    void testFunctionWithComplexBody() {
        List<String> params = Collections.singletonList("n_");
        AssignmentNode assignment = new AssignmentNode("=",
            new IdentifierNode("result", 1, 15, 1, 21),
            new LiteralNode(0, LiteralNode.LiteralType.INTEGER, 1, 24, 1, 25),
            1, 15, 1, 25);
        FunctionDefNode node = new FunctionDefNode("initialize", params, assignment, true, 1, 1, 1, 25);

        assertThat(node.getBody()).isInstanceOf(AssignmentNode.class);
    }

    @Test
    void testParameterCount() {
        List<String> params0 = Collections.emptyList();
        List<String> params1 = Collections.singletonList("x_");
        List<String> params2 = Arrays.asList("x_", "y_");
        List<String> params3 = Arrays.asList("x_", "y_", "z_");

        LiteralNode body = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11);

        FunctionDefNode node0 = new FunctionDefNode("f0", params0, body, true, 1, 1, 1, 11);
        FunctionDefNode node1 = new FunctionDefNode("f1", params1, body, true, 1, 1, 1, 11);
        FunctionDefNode node2 = new FunctionDefNode("f2", params2, body, true, 1, 1, 1, 11);
        FunctionDefNode node3 = new FunctionDefNode("f3", params3, body, true, 1, 1, 1, 11);

        assertThat(node0.getParameters()).isEmpty();
        assertThat(node1.getParameters()).hasSize(1);
        assertThat(node2.getParameters()).hasSize(2);
        assertThat(node3.getParameters()).hasSize(3);
    }

    @Test
    void testFunctionInDifferentContexts() {
        LiteralNode body1 = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 20, 1, 21);
        FunctionDefNode global = new FunctionDefNode("Global`func", Collections.emptyList(), body1, true, 1, 1, 1, 21);

        LiteralNode body2 = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 2, 22, 2, 23);
        FunctionDefNode pkg = new FunctionDefNode("MyPkg`func", Collections.emptyList(), body2, true, 2, 1, 2, 23);

        assertThat(global.getFunctionName()).contains("Global");
        assertThat(pkg.getFunctionName()).contains("MyPkg");
    }
}
