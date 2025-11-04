package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.mathematica.ast.AstNode.NodeType;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AssociationNodeTest {

    @Test
    void testEmptyAssociation() {
        AssociationNode node = new AssociationNode(null, 1, 1, 1, 10);

        assertThat(node.getPairs()).isEmpty();
        assertThat(node.getSize()).isZero();
        assertThat(node.isEmpty()).isTrue();
        assertThat(node.getType()).isEqualTo(NodeType.ASSOCIATION);
    }

    @Test
    void testAssociationWithPairs() {
        List<AssociationNode.KeyValuePair> pairs = Arrays.asList(
            new AssociationNode.KeyValuePair(
                new IdentifierNode("key1", 1, 1, 1, 5),
                new LiteralNode("value1", LiteralNode.LiteralType.STRING, 1, 7, 1, 14)
            ),
            new AssociationNode.KeyValuePair(
                new IdentifierNode("key2", 2, 1, 2, 5),
                new LiteralNode("value2", LiteralNode.LiteralType.STRING, 2, 7, 2, 14)
            )
        );

        AssociationNode node = new AssociationNode(pairs, 1, 1, 2, 15);

        assertThat(node.getPairs()).hasSize(2);
        assertThat(node.getSize()).isEqualTo(2);
        assertThat(node.isEmpty()).isFalse();
    }

    @Test
    void testKeyValuePair() {
        AstNode key = new IdentifierNode("name", 1, 1, 1, 4);
        AstNode value = new LiteralNode("John", LiteralNode.LiteralType.STRING, 1, 6, 1, 10);
        AssociationNode.KeyValuePair pair = new AssociationNode.KeyValuePair(key, value);

        assertThat(pair.getKey()).isEqualTo(key);
        assertThat(pair.getValue()).isEqualTo(value);
        assertThat(pair.toString()).contains("name");
        assertThat(pair.toString()).contains("John");
    }

    @Test
    void testAcceptVisitor() {
        AstNode key = new IdentifierNode("key", 1, 1, 1, 3);
        AstNode value = new LiteralNode("val", LiteralNode.LiteralType.STRING, 1, 5, 1, 8);
        List<AssociationNode.KeyValuePair> pairs = Arrays.asList(
            new AssociationNode.KeyValuePair(key, value)
        );

        AssociationNode node = new AssociationNode(pairs, 1, 1, 1, 10);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);
        assertThat(node).isNotNull();

        // Visitor should process without exceptions
    }

    @Test
    void testToString() {
        AssociationNode node = new AssociationNode(new ArrayList<>(), 1, 1, 1, 5);

        assertThat(node.toString()).contains("Association");
        assertThat(node.toString()).contains("size=0");
    }

    @Test
    void testNestedAssociation() {
        AstNode key = new IdentifierNode("outer", 1, 1, 1, 5);
        AssociationNode innerAssoc = new AssociationNode(new ArrayList<>(), 1, 7, 1, 10);
        List<AssociationNode.KeyValuePair> pairs = Arrays.asList(
            new AssociationNode.KeyValuePair(key, innerAssoc)
        );

        AssociationNode outerNode = new AssociationNode(pairs, 1, 1, 1, 12);

        assertThat(outerNode.getSize()).isEqualTo(1);
        assertThat(outerNode.getPairs().get(0).getValue()).isInstanceOf(AssociationNode.class);
    }
}
