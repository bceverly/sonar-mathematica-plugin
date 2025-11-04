package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FunctionCallNodeTest {

    @Test
    void testFunctionCallWithNoArguments() {
        FunctionCallNode node = new FunctionCallNode("Func", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getFunctionName()).isEqualTo("Func");
        assertThat(node.getArguments()).isEmpty();
    }

    @Test
    void testFunctionCallWithSingleArgument() {
        List<AstNode> args = Collections.singletonList(
            new LiteralNode("5", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6)
        );
        FunctionCallNode node = new FunctionCallNode("Sin", args, 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Sin");
        assertThat(node.getArguments()).hasSize(1);
    }

    @Test
    void testFunctionCallWithMultipleArguments() {
        List<AstNode> args = Arrays.asList(
            new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6),
            new LiteralNode("2", LiteralNode.LiteralType.INTEGER, 1, 8, 1, 9),
            new LiteralNode("3", LiteralNode.LiteralType.INTEGER, 1, 11, 1, 12)
        );
        FunctionCallNode node = new FunctionCallNode("Plus", args, 1, 1, 1, 14);

        assertThat(node.getArguments()).hasSize(3);
    }

    @Test
    void testFunctionCallWithNullArguments() {
        FunctionCallNode node = new FunctionCallNode("Func", null, 1, 1, 1, 7);

        assertThat(node.getArguments()).isNull();
    }

    @Test
    void testGetFunctionName() {
        FunctionCallNode node = new FunctionCallNode("Map", Collections.emptyList(), 1, 1, 1, 6);

        assertThat(node.getFunctionName()).isEqualTo("Map");
    }

    @Test
    void testBuiltinFunction() {
        FunctionCallNode node = new FunctionCallNode("Print", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Print");
    }

    @Test
    void testUserDefinedFunction() {
        FunctionCallNode node = new FunctionCallNode("myFunction", Collections.emptyList(), 1, 1, 1, 13);

        assertThat(node.getFunctionName()).isEqualTo("myFunction");
    }

    @Test
    void testNestedFunctionCall() {
        List<AstNode> innerArgs = Collections.singletonList(
            new LiteralNode("x", LiteralNode.LiteralType.STRING, 1, 5, 1, 6)
        );
        FunctionCallNode innerCall = new FunctionCallNode("Sin", innerArgs, 1, 4, 1, 9);

        List<AstNode> outerArgs = Collections.singletonList(innerCall);
        FunctionCallNode outerCall = new FunctionCallNode("Cos", outerArgs, 1, 1, 1, 11);

        assertThat(outerCall.getArguments()).hasSize(1);
        assertThat(outerCall.getArguments().get(0)).isInstanceOf(FunctionCallNode.class);
    }

    @Test
    void testToStringWithNoArgs() {
        FunctionCallNode node = new FunctionCallNode("Func", Collections.emptyList(), 1, 1, 1, 7);

        String result = node.toString();
        assertThat(result).contains("FunctionCall");
        assertThat(result).contains("Func");
        assertThat(result).contains("args=0");
    }

    @Test
    void testToStringWithArgs() {
        List<AstNode> args = Collections.singletonList(
            new LiteralNode("5", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6)
        );
        FunctionCallNode node = new FunctionCallNode("Sin", args, 1, 1, 1, 8);

        String result = node.toString();
        assertThat(result).contains("args=1");
    }

    @Test
    void testAcceptVisitor() {
        List<AstNode> args = Collections.singletonList(
            new LiteralNode("x", LiteralNode.LiteralType.STRING, 1, 5, 1, 6)
        );
        FunctionCallNode node = new FunctionCallNode("Sin", args, 1, 1, 1, 8);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);

        verify(visitor).visit(node);
    }

    @Test
    void testNodeType() {
        FunctionCallNode node = new FunctionCallNode("Func", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getType()).isEqualTo(AstNode.NodeType.FUNCTION_CALL);
    }

    @Test
    void testLocationInfo() {
        FunctionCallNode node = new FunctionCallNode("Func", Collections.emptyList(), 5, 10, 5, 15);

        assertThat(node.getStartLine()).isEqualTo(5);
        assertThat(node.getStartColumn()).isEqualTo(10);
        assertThat(node.getEndLine()).isEqualTo(5);
        assertThat(node.getEndColumn()).isEqualTo(15);
    }

    @Test
    void testMapFunction() {
        FunctionCallNode node = new FunctionCallNode("Map", Collections.emptyList(), 1, 1, 1, 6);

        assertThat(node.getFunctionName()).isEqualTo("Map");
    }

    @Test
    void testTableFunction() {
        FunctionCallNode node = new FunctionCallNode("Table", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Table");
    }

    @Test
    void testIfFunction() {
        FunctionCallNode node = new FunctionCallNode("If", Collections.emptyList(), 1, 1, 1, 5);

        assertThat(node.getFunctionName()).isEqualTo("If");
    }

    @Test
    void testModuleFunction() {
        FunctionCallNode node = new FunctionCallNode("Module", Collections.emptyList(), 1, 1, 1, 9);

        assertThat(node.getFunctionName()).isEqualTo("Module");
    }

    @Test
    void testBlockFunction() {
        FunctionCallNode node = new FunctionCallNode("Block", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Block");
    }

    @Test
    void testWithFunction() {
        FunctionCallNode node = new FunctionCallNode("With", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getFunctionName()).isEqualTo("With");
    }

    @Test
    void testWhileFunction() {
        FunctionCallNode node = new FunctionCallNode("While", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("While");
    }

    @Test
    void testForFunction() {
        FunctionCallNode node = new FunctionCallNode("For", Collections.emptyList(), 1, 1, 1, 6);

        assertThat(node.getFunctionName()).isEqualTo("For");
    }

    @Test
    void testDoFunction() {
        FunctionCallNode node = new FunctionCallNode("Do", Collections.emptyList(), 1, 1, 1, 5);

        assertThat(node.getFunctionName()).isEqualTo("Do");
    }

    @Test
    void testContextualFunction() {
        FunctionCallNode node = new FunctionCallNode("MyPackage`MyFunc", Collections.emptyList(), 1, 1, 1, 19);

        assertThat(node.getFunctionName()).isEqualTo("MyPackage`MyFunc");
    }

    @Test
    void testSystemContextFunction() {
        FunctionCallNode node = new FunctionCallNode("System`Print", Collections.emptyList(), 1, 1, 1, 15);

        assertThat(node.getFunctionName()).isEqualTo("System`Print");
    }

    @Test
    void testPrivateFunction() {
        FunctionCallNode node = new FunctionCallNode("Package`Private`helper", Collections.emptyList(), 1, 1, 1, 25);

        assertThat(node.getFunctionName()).isEqualTo("Package`Private`helper");
    }

    @Test
    void testArgumentsListIsMutable() {
        List<AstNode> args = new ArrayList<>();
        args.add(new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 5, 1, 6));
        FunctionCallNode node = new FunctionCallNode("Func", args, 1, 1, 1, 8);

        assertThat(node.getArguments()).hasSize(1);
    }

    @Test
    void testTwoArgumentFunction() {
        List<AstNode> args = Arrays.asList(
            new LiteralNode("x", LiteralNode.LiteralType.STRING, 1, 5, 1, 6),
            new LiteralNode("y", LiteralNode.LiteralType.STRING, 1, 8, 1, 9)
        );
        FunctionCallNode node = new FunctionCallNode("Power", args, 1, 1, 1, 11);

        assertThat(node.getArguments()).hasSize(2);
    }

    @Test
    void testManyArgumentsFunction() {
        List<AstNode> args = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            args.add(new LiteralNode(String.valueOf(i), LiteralNode.LiteralType.INTEGER, 1, i * 2, 1, i * 2 + 1));
        }
        FunctionCallNode node = new FunctionCallNode("Plus", args, 1, 1, 1, 25);

        assertThat(node.getArguments()).hasSize(10);
    }

    @Test
    void testApplyFunction() {
        FunctionCallNode node = new FunctionCallNode("Apply", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Apply");
    }

    @Test
    void testFoldFunction() {
        FunctionCallNode node = new FunctionCallNode("Fold", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getFunctionName()).isEqualTo("Fold");
    }

    @Test
    void testScanFunction() {
        FunctionCallNode node = new FunctionCallNode("Scan", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getFunctionName()).isEqualTo("Scan");
    }

    @Test
    void testNestFunction() {
        FunctionCallNode node = new FunctionCallNode("Nest", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getFunctionName()).isEqualTo("Nest");
    }

    @Test
    void testNestListFunction() {
        FunctionCallNode node = new FunctionCallNode("NestList", Collections.emptyList(), 1, 1, 1, 11);

        assertThat(node.getFunctionName()).isEqualTo("NestList");
    }

    @Test
    void testSelectFunction() {
        FunctionCallNode node = new FunctionCallNode("Select", Collections.emptyList(), 1, 1, 1, 9);

        assertThat(node.getFunctionName()).isEqualTo("Select");
    }

    @Test
    void testFilterFunction() {
        FunctionCallNode node = new FunctionCallNode("Cases", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Cases");
    }

    @Test
    void testReplaceFunction() {
        FunctionCallNode node = new FunctionCallNode("Replace", Collections.emptyList(), 1, 1, 1, 10);

        assertThat(node.getFunctionName()).isEqualTo("Replace");
    }

    @Test
    void testReplaceAllFunction() {
        FunctionCallNode node = new FunctionCallNode("ReplaceAll", Collections.emptyList(), 1, 1, 1, 13);

        assertThat(node.getFunctionName()).isEqualTo("ReplaceAll");
    }

    @Test
    void testPartFunction() {
        FunctionCallNode node = new FunctionCallNode("Part", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getFunctionName()).isEqualTo("Part");
    }

    @Test
    void testAppendFunction() {
        FunctionCallNode node = new FunctionCallNode("Append", Collections.emptyList(), 1, 1, 1, 9);

        assertThat(node.getFunctionName()).isEqualTo("Append");
    }

    @Test
    void testPrependFunction() {
        FunctionCallNode node = new FunctionCallNode("Prepend", Collections.emptyList(), 1, 1, 1, 10);

        assertThat(node.getFunctionName()).isEqualTo("Prepend");
    }

    @Test
    void testJoinFunction() {
        FunctionCallNode node = new FunctionCallNode("Join", Collections.emptyList(), 1, 1, 1, 7);

        assertThat(node.getFunctionName()).isEqualTo("Join");
    }

    @Test
    void testLengthFunction() {
        FunctionCallNode node = new FunctionCallNode("Length", Collections.emptyList(), 1, 1, 1, 9);

        assertThat(node.getFunctionName()).isEqualTo("Length");
    }

    @Test
    void testRangeFunction() {
        FunctionCallNode node = new FunctionCallNode("Range", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Range");
    }

    @Test
    void testArrayFunction() {
        FunctionCallNode node = new FunctionCallNode("Array", Collections.emptyList(), 1, 1, 1, 8);

        assertThat(node.getFunctionName()).isEqualTo("Array");
    }

    @Test
    void testConstantArrayFunction() {
        FunctionCallNode node = new FunctionCallNode("ConstantArray", Collections.emptyList(), 1, 1, 1, 16);

        assertThat(node.getFunctionName()).isEqualTo("ConstantArray");
    }

    @Test
    void testAssociationFunction() {
        FunctionCallNode node = new FunctionCallNode("Association", Collections.emptyList(), 1, 1, 1, 14);

        assertThat(node.getFunctionName()).isEqualTo("Association");
    }

    @Test
    void testKeyValueMapFunction() {
        FunctionCallNode node = new FunctionCallNode("KeyValueMap", Collections.emptyList(), 1, 1, 1, 14);

        assertThat(node.getFunctionName()).isEqualTo("KeyValueMap");
    }

    @Test
    void testCompileFunction() {
        FunctionCallNode node = new FunctionCallNode("Compile", Collections.emptyList(), 1, 1, 1, 10);

        assertThat(node.getFunctionName()).isEqualTo("Compile");
    }

    @Test
    void testParallelMapFunction() {
        FunctionCallNode node = new FunctionCallNode("ParallelMap", Collections.emptyList(), 1, 1, 1, 14);

        assertThat(node.getFunctionName()).isEqualTo("ParallelMap");
    }

    @Test
    void testParallelTableFunction() {
        FunctionCallNode node = new FunctionCallNode("ParallelTable", Collections.emptyList(), 1, 1, 1, 16);

        assertThat(node.getFunctionName()).isEqualTo("ParallelTable");
    }

    @Test
    void testImportFunction() {
        FunctionCallNode node = new FunctionCallNode("Import", Collections.emptyList(), 1, 1, 1, 9);

        assertThat(node.getFunctionName()).isEqualTo("Import");
    }

    @Test
    void testExportFunction() {
        FunctionCallNode node = new FunctionCallNode("Export", Collections.emptyList(), 1, 1, 1, 9);

        assertThat(node.getFunctionName()).isEqualTo("Export");
    }
}
