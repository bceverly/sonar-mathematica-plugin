package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing scoping constructs: Module, Block, With.
 *
 * Mathematica has three main scoping constructs:
 * - Module[{vars}, body]: Creates local variables with unique names
 * - Block[{vars}, body]: Temporarily blocks outer definitions
 * - With[{vars}, body]: Lexical scoping with immediate substitution
 *
 * This is Item 1 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class ScopingNode extends AstNode {

    public enum ScopingType {
        MODULE,
        BLOCK,
        WITH
    }

    private final ScopingType scopingType;
    private final List<String> variables;
    private final List<AstNode> initializers;  // Optional initializers for variables
    private final AstNode body;

    public ScopingNode(
        ScopingType scopingType,
        List<String> variables,
        List<AstNode> initializers,
        AstNode body,
        SourceLocation location
    ) {
        super(NodeType.SCOPING, location);
        this.scopingType = scopingType;
        this.variables = variables;
        this.initializers = initializers != null ? initializers : new ArrayList<>();
        this.body = body;
    }

    public ScopingNode(
        ScopingType scopingType,
        List<String> variables,
        List<AstNode> initializers,
        AstNode body,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        this(scopingType, variables, initializers, body,
             new SourceLocation(startLine, startColumn, endLine, endColumn));
    }

    public ScopingType getScopingType() {
        return scopingType;
    }

    public List<String> getVariables() {
        return variables;
    }

    public List<AstNode> getInitializers() {
        return initializers;
    }

    public AstNode getBody() {
        return body;
    }

    @Override
    public void accept(AstVisitor visitor) {
        // Visit initializers first
        for (AstNode initializer : initializers) {
            if (initializer != null) {
                initializer.accept(visitor);
            }
        }
        // Then visit body
        if (body != null) {
            body.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return String.format("%s[vars=%s, body=%s]", scopingType, variables, body);
    }
}
