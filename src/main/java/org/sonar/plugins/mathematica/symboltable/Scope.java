package org.sonar.plugins.mathematica.symboltable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a lexical scope in Mathematica code.
 * Scopes can be nested (parent/child relationships).
 */
public class Scope {
    private final ScopeType type;
    private final int startLine;
    private final int endLine;
    private final Scope parent;
    private final Map<String, Symbol> symbols;
    private final List<Scope> children;
    private final String name; // Optional name (e.g., function name)

    /**
     * Creates a scope.
     *
     * @param type Type of scope
     * @param startLine Starting line number
     * @param endLine Ending line number
     * @param parent Parent scope (null for global scope)
     */
    public Scope(ScopeType type, int startLine, int endLine, Scope parent) {
        this(type, startLine, endLine, parent, null);
    }

    /**
     * Creates a named scope.
     *
     * @param type Type of scope
     * @param startLine Starting line number
     * @param endLine Ending line number
     * @param parent Parent scope (null for global scope)
     * @param name Optional name for scope (e.g., function name)
     */
    public Scope(ScopeType type, int startLine, int endLine, Scope parent, String name) {
        this.type = type;
        this.startLine = startLine;
        this.endLine = endLine;
        this.parent = parent;
        this.name = name;
        this.symbols = new HashMap<>();
        this.children = new ArrayList<>();

        // Register with parent
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * Adds a symbol to this scope.
     */
    public void addSymbol(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }

    /**
     * Looks up a symbol in this scope only (not parent scopes).
     */
    public Symbol getSymbol(String name) {
        return symbols.get(name);
    }

    /**
     * Looks up a symbol in this scope or any parent scope.
     * Follows Mathematica's scoping rules.
     */
    public Symbol resolveSymbol(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }

        // Check parent scope
        if (parent != null) {
            return parent.resolveSymbol(name);
        }

        return null;
    }

    /**
     * Returns all symbols directly in this scope.
     */
    public Collection<Symbol> getSymbols() {
        return Collections.unmodifiableCollection(symbols.values());
    }

    /**
     * Returns all symbols including those in child scopes (recursive).
     */
    public List<Symbol> getAllSymbolsRecursive() {
        List<Symbol> allSymbols = new ArrayList<>(symbols.values());
        for (Scope child : children) {
            allSymbols.addAll(child.getAllSymbolsRecursive());
        }
        return allSymbols;
    }

    /**
     * Adds a child scope.
     */
    private void addChild(Scope child) {
        children.add(child);
    }

    /**
     * Returns true if this scope contains the given line number.
     */
    public boolean containsLine(int line) {
        return line >= startLine && line <= endLine;
    }

    /**
     * Finds the most specific (innermost) scope containing the given line.
     * Returns this scope if no child contains the line.
     */
    public Scope getScopeAtLine(int line) {
        if (!containsLine(line)) {
            return null;
        }

        // Check children (innermost scope)
        for (Scope child : children) {
            Scope found = child.getScopeAtLine(line);
            if (found != null) {
                return found;
            }
        }

        return this;
    }

    public ScopeType getType() {
        return type;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public Scope getParent() {
        return parent;
    }

    public List<Scope> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the depth of this scope (0 for global, 1 for direct child, etc.).
     */
    public int getDepth() {
        int depth = 0;
        Scope current = parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    /**
     * Returns true if this scope or any parent is of the given type.
     */
    public boolean isInScopeType(ScopeType type) {
        if (this.type == type) {
            return true;
        }
        return parent != null && parent.isInScopeType(type);
    }

    @Override
    public String toString() {
        String nameStr = name != null ? " '" + name + "'" : "";
        return String.format("Scope{type=%s%s, lines=%d-%d, symbols=%d, children=%d}",
            type, nameStr, startLine, endLine, symbols.size(), children.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Scope scope = (Scope) obj;
        return startLine == scope.startLine && endLine == scope.endLine && type == scope.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + startLine;
        result = 31 * result + endLine;
        return result;
    }
}
