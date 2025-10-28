package org.sonar.plugins.mathematica.symboltable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a symbol (variable) in Mathematica code.
 * Tracks all assignments and references to this variable.
 */
public class Symbol {
    private final String name;
    private final int declarationLine;
    private final Scope scope;
    private final List<SymbolReference> assignments;
    private final List<SymbolReference> references;
    private final boolean isParameter;
    private final boolean isModuleVariable;

    /**
     * Creates a symbol.
     *
     * @param name Variable name
     * @param declarationLine Line where variable is declared (or first seen)
     * @param scope Scope containing this symbol
     * @param isParameter True if this is a function parameter
     * @param isModuleVariable True if declared in Module/Block/With
     */
    public Symbol(String name, int declarationLine, Scope scope,
                  boolean isParameter, boolean isModuleVariable) {
        this.name = name;
        this.declarationLine = declarationLine;
        this.scope = scope;
        this.isParameter = isParameter;
        this.isModuleVariable = isModuleVariable;
        this.assignments = new ArrayList<>();
        this.references = new ArrayList<>();
    }

    /**
     * Adds an assignment to this symbol.
     */
    public void addAssignment(SymbolReference assignment) {
        if (!assignment.isWrite()) {
            throw new IllegalArgumentException(
                "Assignment must be WRITE or READ_WRITE type");
        }
        assignments.add(assignment);
    }

    /**
     * Adds a reference (read) to this symbol.
     */
    public void addReference(SymbolReference reference) {
        if (!reference.isRead()) {
            throw new IllegalArgumentException(
                "Reference must be READ or READ_WRITE type");
        }
        references.add(reference);
    }

    public String getName() {
        return name;
    }

    public int getDeclarationLine() {
        return declarationLine;
    }

    public Scope getScope() {
        return scope;
    }

    public List<SymbolReference> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public List<SymbolReference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public boolean isParameter() {
        return isParameter;
    }

    public boolean isModuleVariable() {
        return isModuleVariable;
    }

    /**
     * Returns true if symbol is never used (no references).
     */
    public boolean isUnused() {
        return references.isEmpty();
    }

    /**
     * Returns true if symbol is assigned but never read.
     */
    public boolean isAssignedButNeverRead() {
        return !assignments.isEmpty() && references.stream().noneMatch(SymbolReference::isRead);
    }

    /**
     * Returns true if symbol is read but never assigned.
     */
    public boolean isReadButNeverAssigned() {
        return !references.isEmpty() && assignments.isEmpty();
    }

    /**
     * Gets all references (both assignments and reads) sorted by line number.
     */
    public List<SymbolReference> getAllReferencesSorted() {
        List<SymbolReference> all = new ArrayList<>();
        all.addAll(assignments);
        all.addAll(references);
        all.sort((a, b) -> Integer.compare(a.getLine(), b.getLine()));
        return all;
    }

    @Override
    public String toString() {
        return String.format("Symbol{name='%s', line=%d, scope=%s, assignments=%d, references=%d}",
            name, declarationLine, scope.getType(), assignments.size(), references.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Symbol symbol = (Symbol) obj;
        return name.equals(symbol.name) && scope.equals(symbol.scope);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + scope.hashCode();
        return result;
    }
}
