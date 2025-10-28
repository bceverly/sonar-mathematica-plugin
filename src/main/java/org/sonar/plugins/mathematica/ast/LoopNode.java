package org.sonar.plugins.mathematica.ast;

/**
 * AST node representing loop constructs: Do, While, For, Table, NestWhile.
 *
 * Mathematica loops:
 * - Do[body, {i, n}]: Execute body n times
 * - While[test, body]: While loop
 * - For[init, test, incr, body]: Traditional for loop
 * - Table[expr, {i, n}]: Build list by evaluation
 * - NestWhile[f, expr, test]: Functional iteration
 *
 * This is Item 3 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class LoopNode extends AstNode {

    public enum LoopType {
        DO,
        WHILE,
        FOR,
        TABLE,
        NEST_WHILE
    }

    private final LoopType loopType;
    private final String iteratorVariable;  // For Do, For, Table
    private final AstNode start;  // Start value or init expression
    private final AstNode end;    // End value or test condition
    private final AstNode step;   // Step value or increment expression
    private final AstNode body;   // Loop body

    public LoopNode(
        LoopType loopType,
        String iteratorVariable,
        AstNode start,
        AstNode end,
        AstNode step,
        AstNode body,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        super(NodeType.LOOP, startLine, startColumn, endLine, endColumn);
        this.loopType = loopType;
        this.iteratorVariable = iteratorVariable;
        this.start = start;
        this.end = end;
        this.step = step;
        this.body = body;
    }

    public LoopType getLoopType() {
        return loopType;
    }

    public String getIteratorVariable() {
        return iteratorVariable;
    }

    public AstNode getStart() {
        return start;
    }

    public AstNode getEnd() {
        return end;
    }

    public AstNode getStep() {
        return step;
    }

    public AstNode getBody() {
        return body;
    }

    @Override
    public void accept(AstVisitor visitor) {
        // Visit range expressions
        if (start != null) {
            start.accept(visitor);
        }
        if (end != null) {
            end.accept(visitor);
        }
        if (step != null) {
            step.accept(visitor);
        }
        // Visit body
        if (body != null) {
            body.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return String.format("%s[var=%s, body=%s]", loopType, iteratorVariable, body);
    }
}
