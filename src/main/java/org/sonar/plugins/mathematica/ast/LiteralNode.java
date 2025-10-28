package org.sonar.plugins.mathematica.ast;

/**
 * AST node representing a literal value (number, string, boolean).
 *
 * Examples:
 * - 42
 * - 3.14
 * - "hello"
 * - True
 */
public class LiteralNode extends AstNode {

    private final Object value;
    private final LiteralType literalType;

    public enum LiteralType {
        INTEGER,
        REAL,
        STRING,
        BOOLEAN
    }

    public LiteralNode(Object value, LiteralType literalType,
                      int startLine, int startColumn, int endLine, int endColumn) {
        super(NodeType.LITERAL, startLine, startColumn, endLine, endColumn);
        this.value = value;
        this.literalType = literalType;
    }

    public Object getValue() {
        return value;
    }

    public LiteralType getLiteralType() {
        return literalType;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Literal(%s:%s)", value, literalType);
    }
}
