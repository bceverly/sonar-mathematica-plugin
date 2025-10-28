package org.sonar.plugins.mathematica.symboltable;

import org.sonar.api.batch.fs.InputFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages symbol tables across all files in a project.
 * Thread-safe for parallel processing.
 */
public class SymbolTableManager {
    private static final Map<String, SymbolTable> tables = new ConcurrentHashMap<>();

    /**
     * Gets or creates a symbol table for the given file.
     */
    public static SymbolTable getOrCreate(InputFile file) {
        return tables.computeIfAbsent(file.key(), k -> new SymbolTable(file, file.lines()));
    }

    /**
     * Gets an existing symbol table (returns null if not exists).
     */
    public static SymbolTable get(InputFile file) {
        return tables.get(file.key());
    }

    /**
     * Gets an existing symbol table by key (returns null if not exists).
     */
    public static SymbolTable get(String fileKey) {
        return tables.get(fileKey);
    }

    /**
     * Checks if a symbol table exists for the file.
     */
    public static boolean has(InputFile file) {
        return tables.containsKey(file.key());
    }

    /**
     * Gets all symbol tables.
     */
    public static Map<String, SymbolTable> getAll() {
        return new ConcurrentHashMap<>(tables);
    }

    /**
     * Clears all symbol tables (call at end of analysis).
     */
    public static void clear() {
        tables.clear();
    }

    /**
     * Gets the number of symbol tables.
     */
    public static int size() {
        return tables.size();
    }
}
