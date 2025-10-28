package org.sonar.plugins.mathematica.ast;

/**
 * AST node representing an identifier (variable or function name).
 *
 * Examples:
 * - x
 * - myVariable
 * - MyFunction
 */
public class IdentifierNode extends AstNode {

    private final String name;

    public IdentifierNode(String name, int startLine, int startColumn, int endLine, int endColumn) {
        super(NodeType.IDENTIFIER, startLine, startColumn, endLine, endColumn);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Identifier(%s)", name);
    }
}
