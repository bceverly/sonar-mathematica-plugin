package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import org.sonar.plugins.mathematica.ast.AstNode.NodeType;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PureFunctionNodeTest {

    @Test
    void testSlotBasedFunction() {
        AstNode body = new IdentifierNode("#1 + #2", 1, 1, 1, 8);

        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.SLOT_BASED,
            null,
            body,
            2,
            1, 1, 1, 10
        );

        assertThat(node.getForm()).isEqualTo(PureFunctionNode.PureFunctionForm.SLOT_BASED);
        assertThat(node.getBody()).isEqualTo(body);
        assertThat(node.getMaxSlotNumber()).isEqualTo(2);
        assertThat(node.isSlotBased()).isTrue();
        assertThat(node.getParameters()).isEmpty();
        assertThat(node.getType()).isEqualTo(NodeType.PURE_FUNCTION);
    }

    @Test
    void testFunctionFormWithParameters() {
        List<String> params = Arrays.asList("x", "y");
        AstNode body = new IdentifierNode("x + y", 1, 15, 1, 20);

        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.FUNCTION_FORM,
            params,
            body,
            0,
            1, 1, 1, 22
        );

        assertThat(node.getForm()).isEqualTo(PureFunctionNode.PureFunctionForm.FUNCTION_FORM);
        assertThat(node.getParameters()).hasSize(2);
        assertThat(node.getParameters()).containsExactly("x", "y");
        assertThat(node.getMaxSlotNumber()).isZero();
        assertThat(node.isSlotBased()).isFalse();
    }

    @Test
    void testSlotBasedWithHighSlotNumber() {
        AstNode body = new IdentifierNode("#1 * #5", 1, 1, 1, 8);

        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.SLOT_BASED,
            null,
            body,
            5,
            1, 1, 1, 10
        );

        assertThat(node.getMaxSlotNumber()).isEqualTo(5);
    }

    @Test
    void testNullParameters() {
        AstNode body = new IdentifierNode("body", 1, 1, 1, 4);

        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.FUNCTION_FORM,
            null,
            body,
            0,
            1, 1, 1, 6
        );

        assertThat(node.getParameters()).isEmpty();
    }

    @Test
    void testAcceptVisitor() {
        AstNode body = new IdentifierNode("#^2", 1, 1, 1, 4);

        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.SLOT_BASED,
            null,
            body,
            1,
            1, 1, 1, 6
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should process without exceptions
        node.accept(visitor);
        assertThat(node).isNotNull();
    }

    @Test
    void testToStringSlotBased() {
        AstNode body = new IdentifierNode("#1 + #2", 1, 1, 1, 8);

        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.SLOT_BASED,
            null,
            body,
            2,
            1, 1, 1, 10
        );

        String str = node.toString();
        assertThat(str).contains("PureFunction").contains("slots=2");
    }

    @Test
    void testToStringFunctionForm() {
        List<String> params = Arrays.asList("a", "b", "c");
        AstNode body = new IdentifierNode("a*b*c", 1, 15, 1, 20);

        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.FUNCTION_FORM,
            params,
            body,
            0,
            1, 1, 1, 22
        );

        String str = node.toString();
        assertThat(str).contains("Function").contains("params=[a, b, c]");
    }

    @Test
    void testNullBody() {
        PureFunctionNode node = new PureFunctionNode(
            PureFunctionNode.PureFunctionForm.SLOT_BASED,
            null,
            null,
            1,
            1, 1, 1, 5
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should not crash with null body
        node.accept(visitor);
        assertThat(node).isNotNull();
    }
}
