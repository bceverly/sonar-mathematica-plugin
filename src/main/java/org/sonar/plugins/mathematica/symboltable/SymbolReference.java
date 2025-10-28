package org.sonar.plugins.mathematica.symboltable;

/**
 * Represents a single reference to a symbol (variable) in the code.
 * This can be a read, write, or read-write operation.
 */
public class SymbolReference {
    private final int line;
    private final int column;
    private final ReferenceType type;
    private final String context;

    /**
     * Creates a symbol reference.
     *
     * @param line Line number (1-based)
     * @param column Column number (0-based)
     * @param type Type of reference (READ, WRITE, READ_WRITE)
     * @param context Surrounding code context (for debugging/reporting)
     */
    public SymbolReference(int line, int column, ReferenceType type, String context) {
        this.line = line;
        this.column = column;
        this.type = type;
        this.context = context;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public ReferenceType getType() {
        return type;
    }

    public String getContext() {
        return context;
    }

    public boolean isRead() {
        return type == ReferenceType.READ || type == ReferenceType.READ_WRITE;
    }

    public boolean isWrite() {
        return type == ReferenceType.WRITE || type == ReferenceType.READ_WRITE;
    }

    @Override
    public String toString() {
        return String.format("SymbolReference{line=%d, col=%d, type=%s, context='%s'}",
            line, column, type, context);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SymbolReference that = (SymbolReference) obj;
        return line == that.line && column == that.column && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        result = 31 * result + type.hashCode();
        return result;
    }
}
