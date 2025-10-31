package org.sonar.plugins.mathematica.ast;

/**
 * Represents a source code location with line and column information.
 * Used to reduce parameter count in AST node constructors.
 */
public final class SourceLocation {
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    public SourceLocation(int startLine, int startColumn, int endLine, int endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }
}
