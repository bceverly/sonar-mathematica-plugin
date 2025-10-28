package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing compound expressions (multiple expressions separated by semicolons).
 *
 * Mathematica compound expressions:
 * - expr1; expr2; expr3: Execute in sequence, return last value
 * - expr1; expr2;: Trailing semicolon suppresses output
 *
 * This is Item 13 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class CompoundExpressionNode extends AstNode {

    private final List<AstNode> expressions;
    private final boolean suppressOutput;  // True if ends with semicolon

    public CompoundExpressionNode(
        List<AstNode> expressions,
        boolean suppressOutput,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        super(NodeType.COMPOUND, startLine, startColumn, endLine, endColumn);
        this.expressions = expressions != null ? expressions : new ArrayList<>();
        this.suppressOutput = suppressOutput;
    }

    public List<AstNode> getExpressions() {
        return expressions;
    }

    public boolean isSuppressOutput() {
        return suppressOutput;
    }

    public int getExpressionCount() {
        return expressions.size();
    }

    @Override
    public void accept(AstVisitor visitor) {
        for (AstNode expression : expressions) {
            if (expression != null) {
                expression.accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("CompoundExpression[count=%d, suppress=%b]",
            expressions.size(), suppressOutput);
    }
}
