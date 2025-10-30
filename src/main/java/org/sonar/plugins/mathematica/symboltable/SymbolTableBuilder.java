package org.sonar.plugins.mathematica.symboltable;

import org.sonar.api.batch.fs.InputFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds symbol tables by parsing Mathematica code.
 * Extracts variables, assignments, references, and scopes.
 */
public final class SymbolTableBuilder {

    // Private constructor to prevent instantiation
    private SymbolTableBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Patterns for parsing Mathematica code
    private static final Pattern MODULE_PATTERN = Pattern.compile(
        "Module\\s*\\[\\s*\\{([^}]+)\\}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BLOCK_PATTERN = Pattern.compile(
        "Block\\s*\\[\\s*\\{([^}]+)\\}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern WITH_PATTERN = Pattern.compile(
        "With\\s*\\[\\s*\\{([^}]+)\\}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile(
        "(\\w+)\\s*\\[([^\\]]+)\\]\\s*:=",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(
        "(\\w+)\\s*=\\s*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VARIABLE_REFERENCE = Pattern.compile(
        "\\b(\\w+)\\b"
    );

    // Mathematica built-in functions (don't treat as variables)
    private static final Set<String> BUILTINS = new HashSet<>(Arrays.asList(
        "Module", "Block", "With", "If", "Which", "Switch", "While", "Do", "For",
        "Table", "Map", "Apply", "Print", "Plot", "Return", "List", "Null",
        "True", "False", "And", "Or", "Not", "Length", "Part", "First", "Last",
        "Rest", "Most", "Append", "Prepend", "Join", "Select", "Cases", "Count",
        "Position", "Sort", "Union", "Intersection", "Complement", "Range",
        "Sin", "Cos", "Tan", "Exp", "Log", "Sqrt", "Abs", "Max", "Min", "Total",
        "Mean", "Integrate", "D", "Sum", "Product", "Solve", "NSolve", "FindRoot"
    ));

    /**
     * Builds a symbol table for the given file content.
     */
    public static SymbolTable build(InputFile file, String content) {
        SymbolTable table = new SymbolTable(file, file.lines());
        String[] lines = content.split("\n");

        // Build scope hierarchy
        buildScopes(table, lines);

        // Track variables
        trackVariables(table, lines);

        return table;
    }

    /**
     * Detects and builds scope hierarchy.
     */
    private static void buildScopes(SymbolTable table, String[] lines) {
        Scope globalScope = table.getGlobalScope();
        Stack<ScopeInfo> scopeStack = new Stack<>();
        scopeStack.push(new ScopeInfo(globalScope, lines.length + 1)); // Global ends beyond file

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // Pop scopes that have ended
            while (scopeStack.size() > 1 && lineNumber > scopeStack.peek().endLine) {
                scopeStack.pop();
            }

            // Detect Module
            Matcher moduleMatcher = MODULE_PATTERN.matcher(line);
            if (moduleMatcher.find()) {
                int startLine = lineNumber;
                int endLine = findScopeEnd(lines, i, "Module");
                Scope currentScope = scopeStack.peek().scope;
                Scope moduleScope = new Scope(ScopeType.MODULE, startLine, endLine, currentScope);
                scopeStack.push(new ScopeInfo(moduleScope, endLine));

                // Parse variable declarations
                String vars = moduleMatcher.group(1);
                parseVariableDeclarations(table, moduleScope, vars, startLine, true);
            }

            // Detect Block
            Matcher blockMatcher = BLOCK_PATTERN.matcher(line);
            if (blockMatcher.find()) {
                int startLine = lineNumber;
                int endLine = findScopeEnd(lines, i, "Block");
                Scope currentScope = scopeStack.peek().scope;
                Scope blockScope = new Scope(ScopeType.BLOCK, startLine, endLine, currentScope);
                scopeStack.push(new ScopeInfo(blockScope, endLine));

                // Parse variable declarations
                String vars = blockMatcher.group(1);
                parseVariableDeclarations(table, blockScope, vars, startLine, true);
            }

            // Detect With
            Matcher withMatcher = WITH_PATTERN.matcher(line);
            if (withMatcher.find()) {
                int startLine = lineNumber;
                int endLine = findScopeEnd(lines, i, "With");
                Scope currentScope = scopeStack.peek().scope;
                Scope withScope = new Scope(ScopeType.WITH, startLine, endLine, currentScope);
                scopeStack.push(new ScopeInfo(withScope, endLine));

                // Parse variable declarations
                String vars = withMatcher.group(1);
                parseVariableDeclarations(table, withScope, vars, startLine, true);
            }

            // Detect function definitions (create function scope)
            Matcher funcMatcher = FUNCTION_DEF_PATTERN.matcher(line);
            if (funcMatcher.find()) {
                String funcName = funcMatcher.group(1);
                String params = funcMatcher.group(2);
                int startLine = lineNumber;
                int endLine = lineNumber; // Single-line function for now
                Scope currentScope = scopeStack.peek().scope;
                Scope funcScope = new Scope(ScopeType.FUNCTION, startLine, endLine, currentScope, funcName);

                // Parse parameters
                parseFunctionParameters(table, funcScope, params, startLine);
            }
        }
    }

    /**
     * Tracks variable assignments and references.
     */
    private static void trackVariables(SymbolTable table, String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            Scope scope = table.getGlobalScope().getScopeAtLine(lineNumber);
            if (scope == null) {
                scope = table.getGlobalScope();
            }

            // Track assignments
            Matcher assignmentMatcher = ASSIGNMENT_PATTERN.matcher(line);
            while (assignmentMatcher.find()) {
                String varName = assignmentMatcher.group(1);
                if (!BUILTINS.contains(varName)) {
                    Symbol symbol = scope.resolveSymbol(varName);
                    if (symbol == null) {
                        // Create implicit global variable
                        symbol = new Symbol(varName, lineNumber, table.getGlobalScope(), false, false);
                        table.getGlobalScope().addSymbol(symbol);
                        table.addSymbol(symbol);
                    }
                    symbol.addAssignment(new SymbolReference(
                        lineNumber,
                        assignmentMatcher.start(1),
                        ReferenceType.WRITE,
                        line.trim()
                    ));
                }
            }

            // Track references (reads)
            trackReferences(table, line, lineNumber, scope);
        }
    }

    /**
     * Tracks variable references in a line.
     * PERFORMANCE: Uses O(1) HashSet check instead of O(n) stream search.
     */
    private static void trackReferences(SymbolTable table, String line, int lineNumber, Scope scope) {
        // Remove assignments to avoid double-counting
        String lineNoAssignments = line.replaceAll("\\w+\\s*=", "");

        Matcher refMatcher = VARIABLE_REFERENCE.matcher(lineNoAssignments);
        while (refMatcher.find()) {
            String varName = refMatcher.group(1);
            if (!BUILTINS.contains(varName) && !Character.isDigit(varName.charAt(0))) {
                Symbol symbol = scope.resolveSymbol(varName);
                if (symbol != null) {
                    // PERFORMANCE FIX: O(1) HashSet check instead of O(n) stream().anyMatch()
                    // This was causing O(n²) behavior as symbols accumulated references
                    symbol.addReferenceIfNew(new SymbolReference(
                        lineNumber,
                        refMatcher.start(1),
                        ReferenceType.READ,
                        line.trim()
                    ));
                }
            }
        }
    }

    /**
     * Parses variable declarations from Module/Block/With.
     */
    private static void parseVariableDeclarations(SymbolTable table, Scope scope,
                                                  String vars, int lineNumber, boolean isModuleVariable) {
        String[] varList = vars.split(",");
        for (String var : varList) {
            var = var.trim();
            // Remove default values (x = 5 -> x)
            if (var.contains("=")) {
                var = var.substring(0, var.indexOf("=")).trim();
            }
            if (!var.isEmpty() && !BUILTINS.contains(var)) {
                Symbol symbol = new Symbol(var, lineNumber, scope, false, isModuleVariable);
                scope.addSymbol(symbol);
                table.addSymbol(symbol);
            }
        }
    }

    /**
     * Parses function parameters.
     */
    private static void parseFunctionParameters(SymbolTable table, Scope funcScope,
                                                String params, int lineNumber) {
        String[] paramList = params.split(",");
        for (String param : paramList) {
            param = param.trim();
            // Extract parameter name (x_ or x_Integer -> x)
            if (param.contains("_")) {
                param = param.substring(0, param.indexOf("_")).trim();
            }
            if (!param.isEmpty() && !BUILTINS.contains(param)) {
                Symbol symbol = new Symbol(param, lineNumber, funcScope, true, false);
                funcScope.addSymbol(symbol);
                table.addSymbol(symbol);
            }
        }
    }

    /**
     * Finds the end line of a scope (simple bracket counting).
     */
    private static int findScopeEnd(String[] lines, int startIdx, String scopeType) {
        int bracketCount = 0;
        boolean started = false;

        for (int i = startIdx; i < lines.length; i++) {
            String line = lines[i];
            for (char c : line.toCharArray()) {
                if (c == '[') {
                    bracketCount++;
                    started = true;
                } else if (c == ']') {
                    bracketCount--;
                    if (started && bracketCount == 0) {
                        return i + 1; // Return line number (1-based)
                    }
                }
            }
        }

        // If not found, assume rest of file
        return lines.length;
    }

    /**
     * Helper class to track scope info during parsing.
     */
    private static class ScopeInfo {
        Scope scope;
        int endLine;

        ScopeInfo(Scope scope, int endLine) {
            this.scope = scope;
            this.endLine = endLine;
        }
    }
}
