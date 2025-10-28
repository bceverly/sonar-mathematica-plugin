package org.sonar.plugins.mathematica.ast;

/**
 * Visitor pattern for traversing the AST.
 *
 * This allows different analyses to be performed on the AST
 * without modifying the node classes themselves.
 *
 * Example visitors:
 * - ComplexityVisitor: Calculate complexity metrics
 * - SymbolTableVisitor: Build symbol table
 * - UnusedVariableVisitor: Find unused variables
 * - DeadCodeVisitor: Find unreachable code
 */
public interface AstVisitor {

    void visit(FunctionDefNode node);

    void visit(FunctionCallNode node);

    void visit(IdentifierNode node);

    void visit(LiteralNode node);

    /**
     * Default implementation that can be overridden for specific node types.
     */
    default void visitChildren(AstNode node) {
        for (AstNode child : node.getChildren()) {
            child.accept(this);
        }
    }
}
