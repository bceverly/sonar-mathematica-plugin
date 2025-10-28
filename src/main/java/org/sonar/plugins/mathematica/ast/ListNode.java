package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing list literals and nested structures.
 *
 * Mathematica lists are the primary data structure:
 * - {1, 2, 3}: Flat list
 * - {{1, 2}, {3, 4}}: Nested list (matrix)
 * - {}: Empty list
 *
 * This is Item 5 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class ListNode extends AstNode {

    private final List<AstNode> elements;

    public ListNode(
        List<AstNode> elements,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        super(NodeType.LIST, startLine, startColumn, endLine, endColumn);
        this.elements = elements != null ? elements : new ArrayList<>();
    }

    public List<AstNode> getElements() {
        return elements;
    }

    public int getSize() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public void accept(AstVisitor visitor) {
        for (AstNode element : elements) {
            if (element != null) {
                element.accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("List[size=%d]", elements.size());
    }
}
