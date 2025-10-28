package org.sonar.plugins.mathematica.ast;

/**
 * AST node representing Span expressions (range specifications).
 *
 * Mathematica Span:
 * - 1;;10: Elements 1 through 10
 * - ;;-1: From beginning to second-to-last
 * - 2;;: From element 2 to end
 * - 1;;10;;2: Elements 1 through 10 by steps of 2
 *
 * This is Item 15 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class SpanNode extends AstNode {

    private final AstNode start;  // Start index (null means beginning)
    private final AstNode end;    // End index (null means end)
    private final AstNode step;   // Step (null means 1)

    public SpanNode(
        AstNode start,
        AstNode end,
        AstNode step,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        super(NodeType.SPAN, startLine, startColumn, endLine, endColumn);
        this.start = start;
        this.end = end;
        this.step = step;
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

    public boolean hasStart() {
        return start != null;
    }

    public boolean hasEnd() {
        return end != null;
    }

    public boolean hasStep() {
        return step != null;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (start != null) {
            start.accept(visitor);
        }
        if (end != null) {
            end.accept(visitor);
        }
        if (step != null) {
            step.accept(visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Span[");
        sb.append(start != null ? start : "");
        sb.append(";;");
        sb.append(end != null ? end : "");
        if (step != null) {
            sb.append(";;").append(step);
        }
        sb.append("]");
        return sb.toString();
    }
}
