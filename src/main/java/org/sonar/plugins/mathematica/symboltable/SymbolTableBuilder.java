package org.sonar.plugins.mathematica.symboltable;

import org.sonar.api.batch.fs.InputFile;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        "Module\\s*+\\[\\s*+\\{([^}]+)\\}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BLOCK_PATTERN = Pattern.compile(
        "Block\\s*+\\[\\s*+\\{([^}]+)\\}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern WITH_PATTERN = Pattern.compile(
        "With\\s*+\\[\\s*+\\{([^}]+)\\}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile(//NOSONAR
        "(\\w+)\\s*+\\[([^\\]]+)\\]\\s*+:=",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(//NOSONAR
        "(\\w+)\\s*+=\\s*+",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VARIABLE_REFERENCE = Pattern.compile(
        "\\b(\\w+)\\b"
    );

    // Mathematica built-in functions (don't treat as variables or user functions)
    // Comprehensive list to prevent false positives from treating built-ins as user code
    private static final Set<String> BUILTINS = new HashSet<>(Arrays.asList(
        // Control flow
        "If", "Which", "Switch", "Do", "While", "For", "Return", "Break", "Continue",

        // Functional programming
        "Map", "MapAt", "MapIndexed", "MapThread", "Apply", "Scan", "Fold", "FoldList",
        "Nest", "NestList", "NestWhile", "FixedPoint", "FixedPointList",

        // Scoping
        "Module", "Block", "With", "Function", "DynamicModule",

        // List operations
        "Table", "Range", "Array", "List", "Join", "Append", "Prepend", "AppendTo", "PrependTo",
        "Insert", "Delete", "Take", "Drop", "Part", "Extract", "Select", "Cases", "DeleteCases",
        "Flatten", "Partition", "Split", "Riffle", "Thread", "Transpose", "Reverse",

        // List queries
        "Length", "First", "Last", "Rest", "Most", "MemberQ", "FreeQ", "Count",
        "Position", "FirstPosition",

        // Type checking
        "Head", "AtomQ", "ListQ", "NumberQ", "IntegerQ", "RealQ", "StringQ", "SymbolQ",
        "VectorQ", "MatrixQ", "ArrayQ",

        // Association/property access
        "Key", "Lookup", "Keys", "Values", "KeyExistsQ", "Association",

        // String operations
        "StringJoin", "StringLength", "StringTake", "StringDrop", "ToString",

        // Basic math (cheap operations)
        "Plus", "Times", "Subtract", "Divide", "Power", "Mod",
        "Min", "Max", "Abs", "Sign", "Round", "Floor", "Ceiling",

        // Comparison
        "Equal", "Unequal", "Less", "Greater", "LessEqual", "GreaterEqual",
        "SameQ", "UnsameQ", "MatchQ",

        // Logic
        "And", "Or", "Not", "Xor", "Nand", "Nor",

        // Constants
        "True", "False", "Null", "None", "All", "Automatic", "Identity",

        // Pattern matching
        "Replace", "ReplaceAll", "ReplaceRepeated", "Rule", "RuleDelayed",

        // Set operations
        "Union", "Intersection", "Complement", "Subsets", "Tuples",

        // Common math functions
        "Sin", "Cos", "Tan", "Exp", "Log", "Sqrt", "Total",
        "Mean", "Integrate", "D", "Sum", "Product", "Solve", "NSolve", "FindRoot",

        // Graphics and output
        "Print", "Plot", "ListPlot", "Show", "Graphics"
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
        Deque<ScopeInfo> scopeStack = new ArrayDeque<>();
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
                int endLine = findScopeEnd(lines, i);
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
                int endLine = findScopeEnd(lines, i);
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
                int endLine = findScopeEnd(lines, i);
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

                // Skip built-in functions - they are not user-defined functions
                if (BUILTINS.contains(funcName)) {
                    continue;
                }

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
     * OPTIMIZED: Single-pass algorithm with caching to avoid O(n²) behavior.
     */
    private static void trackVariables(SymbolTable table, String[] lines) {
        Map<String, Symbol> symbolCache = new HashMap<>(256);
        Set<Integer> assignmentPositions = new HashSet<>(32);

        for (int i = 0; i < lines.length; i++) {
            processLineForVariables(table, lines[i], i + 1, symbolCache, assignmentPositions);
        }
    }

    /**
     * Process a single line for variable tracking.
     */
    private static void processLineForVariables(SymbolTable table, String line, int lineNumber,
                                                Map<String, Symbol> cache, Set<Integer> positions) {
        if (shouldSkipLineForTracking(line)) {
            return;
        }

        String trimmedLine = line.trim();
        Scope scope = resolveScope(table, lineNumber);
        cache.clear();
        positions.clear();

        trackAssignmentsOnLine(table, line, lineNumber, trimmedLine, scope, cache, positions);
        trackReferencesOnLine(line, lineNumber, trimmedLine, scope, cache, positions);
    }

    /**
     * Check if line should be skipped for variable tracking.
     */
    private static boolean shouldSkipLineForTracking(String line) {
        if (line.isEmpty() || line.length() < 3 || line.length() > 10000) {
            return true;
        }
        String trimmed = line.trim();
        return trimmed.startsWith("(*") || trimmed.startsWith("//");
    }

    /**
     * Resolve scope for a line, defaulting to global.
     */
    private static Scope resolveScope(SymbolTable table, int lineNumber) {
        Scope scope = table.getGlobalScope().getScopeAtLine(lineNumber);
        return scope != null ? scope : table.getGlobalScope();
    }

    /**
     * Track assignments on a line.
     */
    private static void trackAssignmentsOnLine(SymbolTable table, String line, int lineNumber,
                                               String trimmedLine, Scope scope,
                                               Map<String, Symbol> cache, Set<Integer> positions) {
        Matcher matcher = ASSIGNMENT_PATTERN.matcher(line);
        while (matcher.find()) {
            processAssignment(table, matcher, lineNumber, trimmedLine, scope, cache, positions);
        }
    }

    /**
     * Process a single assignment match.
     */
    private static void processAssignment(SymbolTable table, Matcher matcher, int lineNumber,
                                          String trimmedLine, Scope scope,
                                          Map<String, Symbol> cache, Set<Integer> positions) {
        String varName = matcher.group(1);
        if (BUILTINS.contains(varName)) {
            return;
        }

        Symbol symbol = getOrCreateSymbolForAssignment(table, varName, lineNumber, scope, cache);
        symbol.addAssignment(new SymbolReference(lineNumber, matcher.start(1), ReferenceType.WRITE, trimmedLine));
        positions.add(matcher.start(1));
    }

    /**
     * Get or create symbol for assignment.
     */
    private static Symbol getOrCreateSymbolForAssignment(SymbolTable table, String varName,
                                                         int lineNumber, Scope scope,
                                                         Map<String, Symbol> cache) {
        Symbol symbol = cache.get(varName);
        if (symbol != null) {
            return symbol;
        }

        symbol = scope.resolveSymbol(varName);
        if (symbol == null) {
            symbol = new Symbol(varName, lineNumber, table.getGlobalScope(), false, false);
            table.getGlobalScope().addSymbol(symbol);
            table.addSymbol(symbol);
        }
        cache.put(varName, symbol);
        return symbol;
    }

    /**
     * Track references on a line.
     */
    private static void trackReferencesOnLine(String line, int lineNumber, String trimmedLine,
                                              Scope scope, Map<String, Symbol> cache,
                                              Set<Integer> positions) {
        Matcher matcher = VARIABLE_REFERENCE.matcher(line);
        while (matcher.find()) {
            processReference(matcher, lineNumber, trimmedLine, scope, cache, positions);
        }
    }

    /**
     * Process a single reference match.
     */
    private static void processReference(Matcher matcher, int lineNumber, String trimmedLine,
                                         Scope scope, Map<String, Symbol> cache,
                                         Set<Integer> positions) {
        int pos = matcher.start(1);
        if (positions.contains(pos)) {
            return;
        }

        String varName = matcher.group(1);
        if (shouldSkipReference(varName)) {
            return;
        }

        Symbol symbol = resolveSymbolForReference(varName, scope, cache);
        if (symbol != null) {
            symbol.addReferenceIfNew(new SymbolReference(lineNumber, pos, ReferenceType.READ, trimmedLine));
        }
    }

    /**
     * Check if reference should be skipped.
     */
    private static boolean shouldSkipReference(String varName) {
        return BUILTINS.contains(varName) || Character.isDigit(varName.charAt(0));
    }

    /**
     * Resolve symbol for reference using cache.
     */
    private static Symbol resolveSymbolForReference(String varName, Scope scope, Map<String, Symbol> cache) {
        Symbol symbol = cache.get(varName);
        if (symbol == null) {
            symbol = scope.resolveSymbol(varName);
            if (symbol != null) {
                cache.put(varName, symbol);
            }
        }
        return symbol;
    }

    /**
     * Parses variable declarations from Module/Block/With.
     */
    private static void parseVariableDeclarations(SymbolTable table, Scope scope,
                                                  String vars, int lineNumber, boolean isModuleVariable) {
        String[] varList = vars.split(",");
        for (String varDecl : varList) {
            String varName = varDecl.trim();
            // Remove default values (x = 5 -> x)
            if (varName.contains("=")) {
                varName = varName.substring(0, varName.indexOf("=")).trim();
            }
            if (!varName.isEmpty() && !BUILTINS.contains(varName)) {
                Symbol symbol = new Symbol(varName, lineNumber, scope, false, isModuleVariable);
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
    private static int findScopeEnd(String[] lines, int startIdx) {
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
