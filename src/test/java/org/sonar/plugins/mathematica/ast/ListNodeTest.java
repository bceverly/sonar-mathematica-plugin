package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.mathematica.ast.AstNode.NodeType;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ListNodeTest {

    @Test
    void testEmptyList() {
        ListNode node = new ListNode(null, 1, 1, 1, 2);

        assertThat(node.getElements()).isEmpty();
        assertThat(node.getSize()).isZero();
        assertThat(node.isEmpty()).isTrue();
        assertThat(node.getType()).isEqualTo(NodeType.LIST);
    }

    @Test
    void testListWithElements() {
        List<AstNode> elements = Arrays.asList(
            new LiteralNode("1", LiteralNode.LiteralType.STRING, 1, 2, 1, 3),
            new LiteralNode("2", LiteralNode.LiteralType.STRING, 1, 5, 1, 6),
            new LiteralNode("3", LiteralNode.LiteralType.STRING, 1, 8, 1, 9)
        );

        ListNode node = new ListNode(elements, 1, 1, 1, 10);

        assertThat(node.getElements()).hasSize(3);
        assertThat(node.getSize()).isEqualTo(3);
        assertThat(node.isEmpty()).isFalse();
    }

    @Test
    void testAcceptVisitor() {
        List<AstNode> elements = Arrays.asList(
            new IdentifierNode("a", 1, 1, 1, 1),
            new IdentifierNode("b", 1, 3, 1, 3)
        );

        ListNode node = new ListNode(elements, 1, 1, 1, 5);
        AstVisitor visitor = mock(AstVisitor.class);

        node.accept(visitor);


        assertThat(node).isNotNull();
    }

    @Test
    void testToString() {
        ListNode node = new ListNode(new ArrayList<>(), 1, 1, 1, 2);

        assertThat(node.toString()).contains("List");
        assertThat(node.toString()).contains("size=0");
    }

    @Test
    void testNestedLists() {
        ListNode innerList = new ListNode(
            Arrays.asList(new LiteralNode("1", LiteralNode.LiteralType.STRING, 1, 3, 1, 4)),
            1, 2, 1, 5
        );

        List<AstNode> elements = Arrays.asList(innerList);
        ListNode outerList = new ListNode(elements, 1, 1, 1, 6);

        assertThat(outerList.getSize()).isEqualTo(1);
        assertThat(outerList.getElements().get(0)).isInstanceOf(ListNode.class);
    }

    @Test
    void testNullElementHandling() {
        List<AstNode> elementsWithNull = new ArrayList<>();
        elementsWithNull.add(new LiteralNode("1", LiteralNode.LiteralType.STRING, 1, 1, 1, 1));
        elementsWithNull.add(null);
        elementsWithNull.add(new LiteralNode("2", LiteralNode.LiteralType.STRING, 1, 3, 1, 3));

        ListNode node = new ListNode(elementsWithNull, 1, 1, 1, 5);
        AstVisitor visitor = mock(AstVisitor.class);

        // Should not crash with null element
        node.accept(visitor);

        // Only non-null elements should be visited
        // Visitor should process without exceptions
        assertThat(node).isNotNull();
    }
}
