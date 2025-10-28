package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing Part specification (array/list indexing).
 *
 * Mathematica Part:
 * - list[[i]]: Extract element at position i
 * - list[[i, j]]: Extract from nested list
 * - list[[-1]]: Last element (negative indexing)
 * - list[[All]]: All elements
 * - list[[1;;3]]: Range specification (see SpanNode)
 *
 * This is Item 14 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class PartNode extends AstNode {

    private final AstNode expression;  // The expression being indexed
    private final List<AstNode> indices;  // Index specifications

    public PartNode(
        AstNode expression,
        List<AstNode> indices,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        super(NodeType.PART, startLine, startColumn, endLine, endColumn);
        this.expression = expression;
        this.indices = indices != null ? indices : new ArrayList<>();
    }

    public AstNode getExpression() {
        return expression;
    }

    public List<AstNode> getIndices() {
        return indices;
    }

    public int getIndexCount() {
        return indices.size();
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
        for (AstNode index : indices) {
            if (index != null) {
                index.accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s[[%d indices]]", expression, indices.size());
    }
}
