package org.sonar.plugins.mathematica.ast;

/**
 * AST node representing assignments: =, :=, ^=, /., etc.
 */
public class AssignmentNode extends AstNode {

    private final String operator;  // "=", ":=", "^=", etc.
    private final AstNode lhs;      // Left-hand side
    private final AstNode rhs;      // Right-hand side

    public AssignmentNode(String operator, AstNode lhs, AstNode rhs,
                         int startLine, int startColumn, int endLine, int endColumn) {
        super(NodeType.OPERATOR, startLine, startColumn, endLine, endColumn);
        this.operator = operator;
        this.lhs = lhs;
        this.rhs = rhs;
        if (lhs != null) addChild(lhs);
        if (rhs != null) addChild(rhs);
    }

    public String getOperator() {
        return operator;
    }

    public AstNode getLhs() {
        return lhs;
    }

    public AstNode getRhs() {
        return rhs;
    }

    @Override
    public void accept(AstVisitor visitor) {
        // Assignment is a type of operator, visitor handles it
        if (lhs != null) lhs.accept(visitor);
        if (rhs != null) rhs.accept(visitor);
    }

    @Override
    public String toString() {
        return "Assignment[" + operator + "]";
    }
}
