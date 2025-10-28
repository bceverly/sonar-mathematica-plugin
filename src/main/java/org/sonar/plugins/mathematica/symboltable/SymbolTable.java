package org.sonar.plugins.mathematica.symboltable;

import org.sonar.api.batch.fs.InputFile;

import java.util.*;

/**
 * Symbol table for a single Mathematica file.
 * Contains all scopes and symbols defined in the file.
 */
public class SymbolTable {
    private final InputFile file;
    private final Scope globalScope;
    private final Map<String, List<Symbol>> symbolsByName;

    /**
     * Creates a symbol table for a file.
     *
     * @param file The input file
     * @param lineCount Number of lines in file
     */
    public SymbolTable(InputFile file, int lineCount) {
        this.file = file;
        this.globalScope = new Scope(ScopeType.GLOBAL, 1, lineCount, null);
        this.symbolsByName = new HashMap<>();
    }

    /**
     * Gets the global scope for this file.
     */
    public Scope getGlobalScope() {
        return globalScope;
    }

    /**
     * Gets the input file.
     */
    public InputFile getFile() {
        return file;
    }

    /**
     * Adds a symbol to the table and indexes it by name.
     */
    public void addSymbol(Symbol symbol) {
        symbolsByName.computeIfAbsent(symbol.getName(), k -> new ArrayList<>()).add(symbol);
    }

    /**
     * Finds all symbols with the given name across all scopes.
     */
    public List<Symbol> getSymbolsByName(String name) {
        return symbolsByName.getOrDefault(name, Collections.emptyList());
    }

    /**
     * Gets all symbols in the file (across all scopes).
     */
    public List<Symbol> getAllSymbols() {
        return globalScope.getAllSymbolsRecursive();
    }

    /**
     * Finds the symbol at a specific location (most specific scope).
     *
     * @param name Symbol name
     * @param line Line number
     * @return Symbol or null if not found
     */
    public Symbol getSymbolAtLocation(String name, int line) {
        Scope scope = globalScope.getScopeAtLine(line);
        if (scope != null) {
            return scope.resolveSymbol(name);
        }
        return null;
    }

    /**
     * Gets all unused symbols (declared but never referenced).
     */
    public List<Symbol> getUnusedSymbols() {
        List<Symbol> unused = new ArrayList<>();
        for (Symbol symbol : getAllSymbols()) {
            if (symbol.isUnused()) {
                unused.add(symbol);
            }
        }
        return unused;
    }

    /**
     * Gets all symbols assigned but never read.
     */
    public List<Symbol> getAssignedButNeverReadSymbols() {
        List<Symbol> assigned = new ArrayList<>();
        for (Symbol symbol : getAllSymbols()) {
            if (symbol.isAssignedButNeverRead()) {
                assigned.add(symbol);
            }
        }
        return assigned;
    }

    /**
     * Gets all symbols read but never assigned (potential uninitialized use).
     */
    public List<Symbol> getReadButNeverAssignedSymbols() {
        List<Symbol> uninitialized = new ArrayList<>();
        for (Symbol symbol : getAllSymbols()) {
            if (symbol.isReadButNeverAssigned()) {
                uninitialized.add(symbol);
            }
        }
        return uninitialized;
    }

    /**
     * Finds potential shadowing issues where inner scope shadows outer scope.
     */
    public List<ShadowingPair> findShadowingIssues() {
        List<ShadowingPair> issues = new ArrayList<>();

        for (String name : symbolsByName.keySet()) {
            List<Symbol> symbols = symbolsByName.get(name);
            if (symbols.size() < 2) {
                continue;
            }

            // Check each pair for shadowing
            for (int i = 0; i < symbols.size(); i++) {
                for (int j = i + 1; j < symbols.size(); j++) {
                    Symbol symbol1 = symbols.get(i);
                    Symbol symbol2 = symbols.get(j);

                    // Check if one shadows the other
                    if (isChildScope(symbol1.getScope(), symbol2.getScope())) {
                        issues.add(new ShadowingPair(symbol1, symbol2));
                    } else if (isChildScope(symbol2.getScope(), symbol1.getScope())) {
                        issues.add(new ShadowingPair(symbol2, symbol1));
                    }
                }
            }
        }

        return issues;
    }

    /**
     * Checks if scope1 is a child (direct or indirect) of scope2.
     */
    private boolean isChildScope(Scope scope1, Scope scope2) {
        Scope current = scope1.getParent();
        while (current != null) {
            if (current.equals(scope2)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("SymbolTable{file='%s', symbols=%d, scopes=%d}",
            file.filename(), getAllSymbols().size(), countScopes(globalScope));
    }

    private int countScopes(Scope scope) {
        int count = 1;
        for (Scope child : scope.getChildren()) {
            count += countScopes(child);
        }
        return count;
    }

    /**
     * Gets symbol by name (first occurrence).
     * Returns null if not found.
     */
    public Symbol getSymbolByName(String name) {
        List<Symbol> symbols = symbolsByName.get(name);
        if (symbols != null && !symbols.isEmpty()) {
            return symbols.get(0);
        }
        return null;
    }

    /**
     * Represents a shadowing issue (inner symbol shadows outer symbol).
     */
    public static class ShadowingPair {
        private final Symbol inner;
        private final Symbol outer;

        public ShadowingPair(Symbol inner, Symbol outer) {
            this.inner = inner;
            this.outer = outer;
        }

        public Symbol getInner() {
            return inner;
        }

        public Symbol getOuter() {
            return outer;
        }

        @Override
        public String toString() {
            return String.format("Shadowing{inner=%s (line %d), outer=%s (line %d)}",
                inner.getName(), inner.getDeclarationLine(),
                outer.getName(), outer.getDeclarationLine());
        }
    }
}
