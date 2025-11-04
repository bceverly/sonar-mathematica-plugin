package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import org.sonar.plugins.mathematica.ast.AstNode.NodeType;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ScopingNodeTest {

    @Test
    void testModuleNode() {
        List<String> vars = Arrays.asList("x", "y");
        List<AstNode> initializers = Arrays.asList(
            new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11),
            new LiteralNode("2", LiteralNode.LiteralType.INTEGER, 1, 13, 1, 14)
        );
        AstNode body = new IdentifierNode("x + y", 1, 17, 1, 22);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            vars,
            initializers,
            body,
            1, 1, 1, 24
        );

        assertThat(node.getScopingType()).isEqualTo(ScopingNode.ScopingType.MODULE);
        assertThat(node.getVariables()).hasSize(2);
        assertThat(node.getVariables()).containsExactly("x", "y");
        assertThat(node.getInitializers()).hasSize(2);
        assertThat(node.getBody()).isEqualTo(body);
        assertThat(node.getType()).isEqualTo(NodeType.SCOPING);
    }

    @Test
    void testBlockNode() {
        List<String> vars = Arrays.asList("temp");
        AstNode body = new IdentifierNode("temp * 2", 1, 15, 1, 23);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.BLOCK,
            vars,
            null,
            body,
            1, 1, 1, 25
        );

        assertThat(node.getScopingType()).isEqualTo(ScopingNode.ScopingType.BLOCK);
        assertThat(node.getInitializers()).isEmpty();
    }

    @Test
    void testWithNode() {
        List<String> vars = Arrays.asList("a", "b");
        List<AstNode> initializers = Arrays.asList(
            new LiteralNode("5", LiteralNode.LiteralType.INTEGER, 1, 9, 1, 10),
            new LiteralNode("10", LiteralNode.LiteralType.INTEGER, 1, 12, 1, 14)
        );
        AstNode body = new IdentifierNode("a * b", 1, 17, 1, 22);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.WITH,
            vars,
            initializers,
            body,
            1, 1, 1, 24
        );

        assertThat(node.getScopingType()).isEqualTo(ScopingNode.ScopingType.WITH);
    }

    @Test
    void testNullInitializers() {
        List<String> vars = Arrays.asList("x");
        AstNode body = new IdentifierNode("x", 1, 10, 1, 11);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            vars,
            null,
            body,
            1, 1, 1, 13
        );

        assertThat(node.getInitializers()).isEmpty();
    }

    @Test
    void testAcceptVisitor() {
        List<String> vars = Arrays.asList("i", "j");
        List<AstNode> initializers = Arrays.asList(
            new LiteralNode("0", LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11),
            new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 13, 1, 14)
        );
        AstNode body = new IdentifierNode("i + j", 1, 17, 1, 22);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            vars,
            initializers,
            body,
            1, 1, 1, 24
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should process without exceptions
        node.accept(visitor);
        assertThat(node).isNotNull();
    }

    @Test
    void testToString() {
        List<String> vars = Arrays.asList("local");
        AstNode body = new IdentifierNode("local * 2", 1, 15, 1, 24);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.BLOCK,
            vars,
            null,
            body,
            1, 1, 1, 26
        );

        String str = node.toString();
        assertThat(str).contains("BLOCK").contains("vars=[local]");
    }

    @Test
    void testNullBody() {
        List<String> vars = Arrays.asList("x");

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            vars,
            null,
            null,
            1, 1, 1, 10
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should not crash with null body
        node.accept(visitor);
        assertThat(node).isNotNull();
    }

    @Test
    void testNullInitializerInList() {
        List<String> vars = Arrays.asList("a", "b");
        List<AstNode> initializers = Arrays.asList(
            new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11),
            null
        );
        AstNode body = new IdentifierNode("a", 1, 15, 1, 16);

        ScopingNode node = new ScopingNode(
            ScopingNode.ScopingType.MODULE,
            vars,
            initializers,
            body,
            1, 1, 1, 18
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should not crash with null initializer
        node.accept(visitor);
        assertThat(node).isNotNull();
    }
}
