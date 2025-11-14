package org.sonar.plugins.mathematica.rules;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.mathematica.symboltable.Scope;
import org.sonar.plugins.mathematica.symboltable.ScopeType;
import org.sonar.plugins.mathematica.symboltable.Symbol;
import org.sonar.plugins.mathematica.symboltable.SymbolReference;
import org.sonar.plugins.mathematica.symboltable.SymbolTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detector for symbol table-based rules.
 * Implements advanced variable lifetime and scope analysis rules.
 */
public final class SymbolTableDetector {

    private static final String NAMINGCONVENTIONVIOLATIONS = "NamingConventionViolations";

    private SymbolTableDetector() {
        // Utility class - prevent instantiation
    }

    /**
     * Rule 1: Variable declared but never used.
     * Detects variables that are never referenced anywhere.
     */
    public static void detectUnusedVariable(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getUnusedSymbols()) {
            // Skip parameters (they might be required by interface)
            if (symbol.isParameter()) {
                continue;
            }

            createIssue(context, file, "UnusedVariable", symbol.getDeclarationLine(),
                String.format("Variable '%s' is declared but never used", symbol.getName())
            ).save();
        }
    }

    /**
     * Rule 2: Variable assigned but never read.
     * Detects dead stores - assignments whose values are never used.
     *
     * NOTE: Excludes side-effect assignments (e.g., Clump definitions, package declarations)
     * where the assignment itself is the meaningful action, not preparing a value for later use.
     */
    public static void detectAssignedButNeverRead(SensorContext context, InputFile file, SymbolTable table) {
        try {
            String content = file.contents();
            String[] lines = content.split("\n");

            for (Symbol symbol : table.getAssignedButNeverReadSymbols()) {
                int lineNum = symbol.getDeclarationLine();

                // Get the actual assignment line (1-indexed to 0-indexed)
                if (lineNum > 0 && lineNum <= lines.length) {
                    String line = lines[lineNum - 1];

                    // Skip side-effect assignments (template/class-like definitions)
                    if (isSideEffectAssignment(line, symbol.getName())) {
                        continue;
                    }
                }

                createIssue(context, file, "AssignedButNeverRead", lineNum,
                    String.format("Variable '%s' is assigned but its value is never read", symbol.getName())
                ).save();
            }
        } catch (Exception e) {
            // If we can't read the file, skip this check
        }
    }

    /**
     * Checks if an assignment is a side-effect assignment (registration/declaration)
     * where the assignment itself is meaningful, not preparing a value for later use.
     *
     * Examples:
     * - MyTemplate = Clump[{...}]        -> Template registration
     * - MyPackage = DeclarePackage[...]  -> Package declaration
     * - MyClass = DefineClass[...]       -> Class definition
     */
    private static boolean isSideEffectAssignment(String line, String varName) {
        // Remove leading whitespace for pattern matching
        String trimmed = line.trim();

        // Pattern: VarName = SideEffectFunction[...]
        // Common side-effect functions that register/declare rather than compute
        String[] sideEffectFunctions = {
            "Clump",           // Template/object system
            "DeclarePackage",  // Package declaration
            "DefineClass",     // Class definition
            "RegisterComponent", // Component registration
            "DefineModule",    // Module definition
            "CreateTemplate",  // Template creation
            "DeclareType"      // Type declaration
        };

        // Check if line matches: varName = SideEffectFunction[
        for (String func : sideEffectFunctions) {
            String pattern = varName + "\\s*=\\s*" + func + "\\s*\\[";
            if (trimmed.matches(".*" + pattern + ".*")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Rule 3: Dead store - variable reassigned before previous value is read.
     */
    public static void detectDeadStore(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            List<SymbolReference> assignments = symbol.getAssignments();
            List<SymbolReference> references = symbol.getReferences();

            for (int i = 0; i < assignments.size() - 1; i++) {
                SymbolReference assignment = assignments.get(i);
                SymbolReference nextAssignment = assignments.get(i + 1);

                // Check if any reads between these two assignments
                boolean hasReadBetween = references.stream()
                    .anyMatch(ref -> ref.getLine() > assignment.getLine()
                                    && ref.getLine() < nextAssignment.getLine());

                if (!hasReadBetween) {
                    createIssue(context, file, "DeadStore", assignment.getLine(),
                        String.format("Value assigned to '%s' is overwritten before being read",
                            symbol.getName())
                    ).save();
                }
            }
        }
    }

    /**
     * Rule 4: Variable used before assignment.
     * Detects potential use of uninitialized variables.
     */
    public static void detectUsedBeforeAssignment(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isParameter()) {
                continue; // Parameters are initialized by caller
            }

            List<SymbolReference> assignments = symbol.getAssignments();
            List<SymbolReference> references = symbol.getReferences();

            if (assignments.isEmpty() && !references.isEmpty()) {
                // Used but never assigned in this scope
                SymbolReference firstUse = references.get(0);
                createIssue(context, file, "UsedBeforeAssignment", firstUse.getLine(),
                    String.format("Variable '%s' is used before being assigned", symbol.getName())
                ).save();
            } else if (!assignments.isEmpty() && !references.isEmpty()) {
                // Check if first use comes before first assignment
                SymbolReference firstUse = references.get(0);
                SymbolReference firstAssignment = assignments.get(0);

                if (firstUse.getLine() < firstAssignment.getLine()) {
                    createIssue(context, file, "UsedBeforeAssignment", firstUse.getLine(),
                        String.format("Variable '%s' is used on line %d before being assigned on line %d",
                            symbol.getName(), firstUse.getLine(), firstAssignment.getLine())
                    ).save();
                }
            }
        }
    }

    /**
     * Rule 5: Variable shadows outer scope variable.
     * Detects shadowing which can lead to confusion.
     */
    public static void detectVariableShadowing(SensorContext context, InputFile file, SymbolTable table) {
        for (SymbolTable.ShadowingPair pair : table.findShadowingIssues()) {
            Symbol inner = pair.getInner();
            Symbol outer = pair.getOuter();

            createIssue(context, file, "VariableShadowing", inner.getDeclarationLine(),
                String.format("Variable '%s' shadows outer variable declared on line %d",
                    inner.getName(), outer.getDeclarationLine())
            ).save();
        }
    }

    /**
     * Rule 6: Unused function parameter.
     * Detects function parameters that are never used in the function body.
     */
    public static void detectUnusedParameter(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isParameter() && symbol.isUnused()) {
                createIssue(context, file, "UnusedParameter", symbol.getDeclarationLine(),
                    String.format("Parameter '%s' is never used in function body",
                        symbol.getName())
                ).save();
            }
        }
    }

    /**
     * Rule 7: Variable only written, never read (enhanced version of Rule 2).
     * More sophisticated analysis considering all code paths.
     */
    public static void detectWriteOnlyVariable(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isParameter()) {
                continue;
            }

            List<SymbolReference> assignments = symbol.getAssignments();
            List<SymbolReference> references = symbol.getReferences();

            // Has assignments but no reads
            if (!assignments.isEmpty() && references.isEmpty()) {
                createIssue(context, file, "WriteOnlyVariable", symbol.getDeclarationLine(),
                    String.format("Variable '%s' is only written to, never read",
                        symbol.getName())
                ).save();
            }
        }
    }

    /**
     * Rule 8: Multiple assignments of same value (redundant).
     */
    public static void detectRedundantAssignment(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            List<SymbolReference> assignments = symbol.getAssignments();

            for (int i = 0; i < assignments.size() - 1; i++) {
                SymbolReference assign1 = assignments.get(i);
                SymbolReference assign2 = assignments.get(i + 1);

                // Simple heuristic: if contexts are very similar, likely redundant
                String ctx1 = assign1.getContext().replaceAll("\\s+", "");
                String ctx2 = assign2.getContext().replaceAll("\\s+", "");

                if (ctx1.equals(ctx2)) {
                    createIssue(context, file, "RedundantAssignment", assign2.getLine(),
                        String.format("Variable '%s' assigned same value twice", symbol.getName())
                    ).save();
                    break; // Only report once per symbol
                }
            }
        }
    }

    /**
     * Rule 9: Variable declared in wrong scope.
     * Variable could be in more specific (inner) scope.
     */
    public static void detectVariableInWrongScope(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isModuleVariable()) {
                checkSymbolScopeOptimization(context, file, symbol);
            }
        }
    }

    private static void checkSymbolScopeOptimization(SensorContext context, InputFile file, Symbol symbol) {
        List<SymbolReference> allRefs = symbol.getAllReferencesSorted();
        if (allRefs.isEmpty()) {
            return;
        }

        Scope symbolScope = symbol.getScope();
        for (Scope childScope : symbolScope.getChildren()) {
            if (areAllReferencesInScope(allRefs, childScope)) {
                createIssue(context, file, "VariableInWrongScope", symbol.getDeclarationLine(),
                    String.format("Variable '%s' could be declared in inner scope (lines %d-%d)",
                        symbol.getName(), childScope.getStartLine(), childScope.getEndLine())
                ).save();
                break;
            }
        }
    }

    private static boolean areAllReferencesInScope(List<SymbolReference> refs, Scope scope) {
        return refs.stream().allMatch(ref ->
            ref.getLine() >= scope.getStartLine() && ref.getLine() <= scope.getEndLine());
    }

    /**
     * Rule 10: Variable escapes scope via closure.
     * Module variable captured in closure will fail after Module exits.
     */
    public static void detectVariableEscapesScope(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isModuleVariable()) {
                checkForClosureEscape(context, file, symbol);
            }
        }
    }

    private static void checkForClosureEscape(SensorContext context, InputFile file, Symbol symbol) {
        Scope symbolScope = symbol.getScope();
        for (Scope childScope : symbolScope.getChildren()) {
            if (childScope.getType() == ScopeType.FUNCTION) {
                checkSymbolReferencesInFunction(context, file, symbol, childScope);
            }
        }
    }

    private static void checkSymbolReferencesInFunction(SensorContext context, InputFile file,
                                                         Symbol symbol, Scope functionScope) {
        for (SymbolReference ref : symbol.getReferences()) {
            if (isReferenceInScope(ref, functionScope)) {
                createIssue(context, file, "VariableEscapesScope", ref.getLine(),
                    String.format("Module variable '%s' captured in closure may fail after Module exits",
                        symbol.getName())
                ).save();
                break;
            }
        }
    }

    private static boolean isReferenceInScope(SymbolReference ref, Scope scope) {
        return ref.getLine() >= scope.getStartLine() && ref.getLine() <= scope.getEndLine();
    }

    /**
     * Rule 11: Variable lifetime extends beyond necessary scope.
     * Variable is used in limited range but declared in wider scope.
     */
    public static void detectLifetimeExtendsBeyondScope(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isModuleVariable()) {
                List<SymbolReference> allRefs = symbol.getAllReferencesSorted();
                if (allRefs.size() >= 2) {
                    // Check if all references are within a narrow line range
                    int firstLine = allRefs.get(0).getLine();
                    int lastLine = allRefs.get(allRefs.size() - 1).getLine();
                    int scopeSize = symbol.getScope().getEndLine() - symbol.getScope().getStartLine();
                    int usageRange = lastLine - firstLine;

                    // If usage range is <20% of scope size, variable could be more local
                    if (scopeSize > 10 && usageRange < scopeSize * 0.2) {
                        createIssue(context, file, "LifetimeExtendsBeyondScope", symbol.getDeclarationLine(),
                            String.format("Variable '%s' used only in lines %d-%d but declared in scope spanning %d lines",
                                symbol.getName(), firstLine, lastLine, scopeSize)
                        ).save();
                    }
                }
            }
        }
    }

    /**
     * Rule 12: Variable modified in unexpected scope.
     * Variable is read in one scope but modified in another.
     */
    public static void detectModifiedInUnexpectedScope(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            Scope declarationScope = symbol.getScope();
            Set<Integer> writeScopeLines = new java.util.HashSet<>();
            Set<Integer> readScopeLines = new java.util.HashSet<>();

            for (SymbolReference ref : symbol.getAssignments()) {
                writeScopeLines.add(ref.getLine());
            }

            for (SymbolReference ref : symbol.getReferences()) {
                readScopeLines.add(ref.getLine());
            }

            // Check if writes happen in different scope than reads
            for (int writeLine : writeScopeLines) {
                Scope writeScope = declarationScope.getScopeAtLine(writeLine);
                for (int readLine : readScopeLines) {
                    Scope readScope = declarationScope.getScopeAtLine(readLine);
                    if (writeScope != null && readScope != null && writeScope != readScope
                        && !isParentChildRelation(writeScope, readScope)) {
                        createIssue(context, file, "ModifiedInUnexpectedScope", writeLine,
                            String.format("Variable '%s' modified here but read in unrelated scope (line %d)",
                                symbol.getName(), readLine)
                        ).save();
                        return; // Report once per symbol
                    }
                }
            }
        }
    }

    /**
     * Rule 13: Global variable pollution.
     * Too many global variables defined, polluting global namespace.
     */
    public static void detectGlobalVariablePollution(SensorContext context, InputFile file, SymbolTable table) {
        List<Symbol> globalVars = new java.util.ArrayList<>();
        Scope globalScope = table.getGlobalScope();

        for (Symbol symbol : globalScope.getSymbols()) {
            if (!symbol.isParameter()) {
                globalVars.add(symbol);
            }
        }

        // Threshold: more than 20 global variables in a single file
        if (globalVars.size() > 20) {
            createIssue(context, file, "GlobalVariablePollution", 1,
                String.format("File defines %d global variables, polluting global namespace. Consider using Package or Context.",
                    globalVars.size())
            ).save();
        }
    }

    /**
     * Rule 14: Circular variable dependencies.
     * Variable A depends on B, B depends on C, C depends on A.
     *
     * SCOPE-AWARE: Only checks file-level (GLOBAL) variables.
     * Excludes local variables from Module, Block, With, and function parameters.
     */
    public static void detectCircularVariableDependencies(SensorContext context, InputFile file, SymbolTable table) {
        // Skip large files - O(n²) complexity makes this prohibitively expensive
        if (table.getAllSymbols().size() > 200) {
            return;
        }

        Map<String, Set<String>> dependencies = buildDependencyGraph(table);
        detectAndReportCycles(context, file, table, dependencies);
    }

    private static Map<String, Set<String>> buildDependencyGraph(SymbolTable table) {
        Map<String, Set<String>> dependencies = new java.util.HashMap<>();

        for (Symbol symbol : table.getAllSymbols()) {
            // SCOPE-AWARE: Only check file-level (GLOBAL) variables
            // Exclude local variables from Module, Block, With, and function parameters
            if (isLocalScopeVariable(symbol)) {
                continue;
            }

            Set<String> deps = findSymbolDependencies(symbol, table);
            if (!deps.isEmpty()) {
                dependencies.put(symbol.getName(), deps);
            }
        }

        return dependencies;
    }

    /**
     * Returns true if this symbol is a local scope variable that should be excluded
     * from file-level circular dependency analysis.
     */
    private static boolean isLocalScopeVariable(Symbol symbol) {
        // Exclude function parameters
        if (symbol.isParameter()) {
            return true;
        }

        // Exclude Module/Block/With variables
        if (symbol.isModuleVariable()) {
            return true;
        }

        // Exclude any symbol not in GLOBAL scope
        org.sonar.plugins.mathematica.symboltable.ScopeType scopeType = symbol.getScope().getType();
        return scopeType != org.sonar.plugins.mathematica.symboltable.ScopeType.GLOBAL;
    }

    private static Set<String> findSymbolDependencies(Symbol symbol, SymbolTable table) {
        Set<String> deps = new java.util.HashSet<>();

        for (SymbolReference assignment : symbol.getAssignments()) {
            String contextStr = assignment.getContext();
            collectReferencedVariables(symbol, table, contextStr, deps);
        }

        return deps;
    }

    private static void collectReferencedVariables(Symbol symbol, SymbolTable table, String contextStr, Set<String> deps) {
        // Remove string literals and comments from context to avoid false positives
        // Example: webmQ = pacletName == "webMathematica"
        // Should not treat "webMathematica" as a variable reference
        String cleanedContext = removeStringsAndComments(contextStr);

        for (Symbol otherSymbol : table.getAllSymbols()) {
            // Skip same symbol or local scope variables
            if (otherSymbol.getName().equals(symbol.getName()) || isLocalScopeVariable(otherSymbol)) {
                continue;
            }

            // Use word boundary matching to avoid false positives
            // e.g., "sf" won't match "StaticFigure" or "transform"
            if (containsAsWord(cleanedContext, otherSymbol.getName())) {
                deps.add(otherSymbol.getName());
            }
        }
    }

    /**
     * Removes string literals, comments, and comparison expressions from context to avoid false positives.
     * Replaces removed content with spaces to preserve word boundaries.
     */
    private static String removeStringsAndComments(String context) {
        StringBuilder result = new StringBuilder(context);
        removeStringLiterals(result);
        String withoutStrings = result.toString();
        String withoutComments = removeComments(withoutStrings);
        return removeComparisonExpressions(withoutComments);
    }

    /**
     * Removes comparison expressions to avoid false circular dependencies.
     * Example: "webmQ = pacletName === \"webMathematica\"" should not treat pacletName as a dependency
     * since it's only used in a comparison, not in a computational dependency.
     */
    private static String removeComparisonExpressions(String context) {
        // Remove patterns like: variable === value, variable == value, variable != value, variable =!= value
        // Also handles: value === variable, value == variable, etc.
        String result = context;

        // Match comparison operators with surrounding content
        // Use possessive quantifiers to prevent backtracking
        result = result.replaceAll("\\w++\\s*+===\\s*+\\S++", "   "); //NOSONAR
        result = result.replaceAll("\\w++\\s*+==\\s*+\\S++", "   "); //NOSONAR
        result = result.replaceAll("\\w++\\s*+!=\\s*+\\S++", "   "); //NOSONAR
        result = result.replaceAll("\\w++\\s*+=!=\\s*+\\S++", "   "); //NOSONAR
        result = result.replaceAll("\\w++\\s*+>\\s*+\\S++", "   "); //NOSONAR
        result = result.replaceAll("\\w++\\s*+<\\s*+\\S++", "   "); //NOSONAR
        result = result.replaceAll("\\w++\\s*+>=\\s*+\\S++", "   "); //NOSONAR
        result = result.replaceAll("\\w++\\s*+<=\\s*+\\S++", "   "); //NOSONAR

        return result;
    }

    /**
     * Removes string literals from the content, replacing them with spaces.
     */
    private static void removeStringLiterals(StringBuilder result) {
        boolean insideString = false;
        boolean escaped = false;

        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);

            if (escaped) {
                replaceCharIfInsideString(result, i, insideString);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                replaceCharIfInsideString(result, i, insideString);
            } else if (c == '"') {
                result.setCharAt(i, ' ');
                insideString = !insideString;
            } else if (insideString) {
                result.setCharAt(i, ' ');
            }
        }
    }

    /**
     * Replaces a character with a space if inside a string literal.
     */
    private static void replaceCharIfInsideString(StringBuilder result, int index, boolean insideString) {
        if (insideString) {
            result.setCharAt(index, ' ');
        }
    }

    /**
     * Removes Mathematica comments (* ... *) from the content.
     */
    private static String removeComments(String withoutStrings) {
        StringBuilder result = new StringBuilder(withoutStrings);
        int commentDepth = 0;
        int i = 0;

        while (i < result.length() - 1) {
            if (isCommentStart(result, i)) {
                commentDepth++;
                replaceCommentDelimiter(result, i);
                i += 2;
            } else if (isCommentEnd(result, i)) {
                if (commentDepth > 0) {
                    commentDepth--;
                }
                replaceCommentDelimiter(result, i);
                i += 2;
            } else {
                replaceCharIfInsideComment(result, i, commentDepth);
                i++;
            }
        }

        return result.toString();
    }

    /**
     * Checks if position is at the start of a comment.
     */
    private static boolean isCommentStart(StringBuilder result, int i) {
        return result.charAt(i) == '(' && result.charAt(i + 1) == '*';
    }

    /**
     * Checks if position is at the end of a comment.
     */
    private static boolean isCommentEnd(StringBuilder result, int i) {
        return result.charAt(i) == '*' && result.charAt(i + 1) == ')';
    }

    /**
     * Replaces comment delimiter characters with spaces.
     */
    private static void replaceCommentDelimiter(StringBuilder result, int i) {
        result.setCharAt(i, ' ');
        result.setCharAt(i + 1, ' ');
    }

    /**
     * Replaces a character with a space if inside a comment.
     */
    private static void replaceCharIfInsideComment(StringBuilder result, int i, int commentDepth) {
        if (commentDepth > 0) {
            result.setCharAt(i, ' ');
        }
    }

    /**
     * Checks if the context string contains the target as a complete word, not as a substring.
     * Uses word boundary matching to avoid false positives.
     *
     * Examples:
     * - containsAsWord("x = y + 1", "y") -> true
     * - containsAsWord("transform[x]", "sf") -> false
     * - containsAsWord("StaticFigure", "sf") -> false
     */
    private static boolean containsAsWord(String context, String target) {
        // Use regex word boundary matching: \b
        // This ensures we match whole words only
        String pattern = "\\b" + java.util.regex.Pattern.quote(target) + "\\b";
        return java.util.regex.Pattern.compile(pattern).matcher(context).find();
    }

    private static void detectAndReportCycles(SensorContext context, InputFile file, SymbolTable table,
                                               Map<String, Set<String>> dependencies) {
        for (String varName : dependencies.keySet()) {
            Set<String> visited = new java.util.HashSet<>();
            Set<String> recStack = new java.util.HashSet<>();
            if (hasCycle(varName, dependencies, visited, recStack)) {
                reportCircularDependency(context, file, table, varName);
            }
        }
    }

    private static void reportCircularDependency(SensorContext context, InputFile file, SymbolTable table, String varName) {
        Symbol symbol = table.getSymbolByName(varName);
        if (symbol != null) {
            createIssue(context, file, "CircularVariableDependencies", symbol.getDeclarationLine(),
                String.format("Variable '%s' has circular dependency with other variables", varName)
            ).save();
        }
    }

    /**
     * Rule 15: Variable naming convention violations (enhanced).
     * Variables should follow consistent naming conventions.
     */
    public static void detectNamingConventionViolations(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            String name = symbol.getName();

            // Check for various naming issues
            if (name.length() == 1 && !symbol.isParameter()) {
                createIssue(context, file, NAMINGCONVENTIONVIOLATIONS, symbol.getDeclarationLine(),
                    String.format("Variable '%s' has single-character name, use descriptive name", name)
                ).save();
            } else if (name.matches(".*\\d+$") && !name.matches("^[a-z].*")) { //NOSONAR
                // Variable ending in number but not lowercase (temp1, temp2 is ok, Temp1 is not)
                createIssue(context, file, NAMINGCONVENTIONVIOLATIONS, symbol.getDeclarationLine(),
                    String.format("Variable '%s' uses numbered suffix, consider more descriptive name", name)
                ).save();
            } else if (name.matches("^[A-Z][A-Z]+$")) {
                // All caps (CONSTANT style) but not marked as constant
                createIssue(context, file, NAMINGCONVENTIONVIOLATIONS, symbol.getDeclarationLine(),
                    String.format("Variable '%s' uses all-caps naming, typically reserved for constants", name)
                ).save();
            }
        }
    }

    /**
     * Rule 16: Constant variables not marked as such.
     * Variable assigned once and never modified should be constant.
     */
    public static void detectConstantNotMarkedAsConstant(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isParameter() || !symbol.isModuleVariable()) {
                continue;
            }

            List<SymbolReference> assignments = symbol.getAssignments();

            // Exactly one assignment and multiple reads = likely constant
            if (assignments.size() == 1 && symbol.getReferences().size() > 2) {
                createIssue(context, file, "ConstantNotMarkedAsConstant", symbol.getDeclarationLine(),
                    String.format("Variable '%s' assigned once and read multiple times, consider using With[] for constants",
                        symbol.getName())
                ).save();
            }
        }
    }

    /**
     * Rule 17: Variable type inconsistency (enhanced).
     * Variable used with inconsistent types (number vs list vs string).
     *
     * Enhanced to understand higher-order functions that return lists.
     */
    public static void detectTypeInconsistency(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            Set<String> suspectedTypes = new java.util.HashSet<>();

            // First, check assignments to understand the initial type
            String assignmentType = inferTypeFromAssignments(symbol);
            if (assignmentType != null) {
                suspectedTypes.add(assignmentType);
            }

            // Then check how it's used
            for (SymbolReference ref : symbol.getAllReferencesSorted()) {
                String contextStr = ref.getContext();

                // Heuristics for type detection
                if (contextStr.matches(".*\\+\\s*+\".*") || contextStr.matches(".*\".*\\+.*")) { //NOSONAR
                    suspectedTypes.add("string");
                } else if (contextStr.matches(".*\\[\\[.*\\]\\].*") || contextStr.matches(".*Part\\[.*")) { //NOSONAR
                    suspectedTypes.add("list");
                } else if (contextStr.matches(".*[\\+\\-\\*/]\\s*+\\d+.*")) { //NOSONAR
                    suspectedTypes.add("number");
                }
            }

            if (suspectedTypes.size() > 1) {
                createIssue(context, file, "TypeInconsistency", symbol.getDeclarationLine(),
                    String.format("Variable '%s' used inconsistently as: %s",
                        symbol.getName(), String.join(", ", suspectedTypes))
                ).save();
            }
        }
    }

    /**
     * Infers the type of a variable from its assignment expressions.
     * Understands higher-order functions that return lists.
     */
    private static String inferTypeFromAssignments(Symbol symbol) {
        for (SymbolReference assignment : symbol.getAssignments()) {
            String context = assignment.getContext();

            // Check for list-returning functions (higher-order functions)
            if (returnsListType(context)) {
                return "list";
            }

            // Check for string assignment
            if (context.contains("\"")) {
                return "string";
            }

            // Check for numeric literal
            if (context.matches(".*=\\s*+\\d+.*")) { //NOSONAR
                return "number";
            }
        }
        return null;
    }

    /**
     * Checks if the context contains a function call that returns a list.
     * Covers common higher-order and list-generating functions.
     */
    private static boolean returnsListType(String context) {
        // List of functions that always return lists
        String[] listFunctions = {
            "Map\\[", "Table\\[", "Select\\[", "Cases\\[", "DeleteCases\\[",
            "Range\\[", "Array\\[", "List\\[", "Join\\[", "Append\\[", "Prepend\\[",
            "Insert\\[", "Delete\\[", "Take\\[", "Drop\\[", "Partition\\[",
            "Split\\[", "GatherBy\\[", "SortBy\\[", "Sort\\[", "Reverse\\[",
            "Flatten\\[", "Union\\[", "Intersection\\[", "Complement\\[",
            "Transpose\\[", "Dimensions\\[", "Position\\[", "Extract\\[",
            "MapThread\\[", "MapIndexed\\[", "Scan\\[", "FoldList\\[", "NestList\\[",
            "Tuples\\[", "Permutations\\[", "Subsets\\[", "IntegerPartitions\\[",
            "CharacterRange\\[", "Keys\\[", "Values\\[", "Association\\[",
            "Normal\\[", "Thread\\[", "Outer\\[", "Inner\\["
        };

        for (String func : listFunctions) {
            if (context.matches(".*" + func + ".*")) { //NOSONAR
                return true;
            }
        }

        return false;
    }

    /**
     * Rule 18: Variable reuse with different semantics.
     * Variable is reused for different purposes (counter, then accumulator, etc).
     */
    public static void detectVariableReuseWithDifferentSemantics(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            List<SymbolReference> assignments = symbol.getAssignments();

            if (assignments.size() >= 2) {
                // Check if assignment contexts are very different
                String firstContext = assignments.get(0).getContext().replaceAll("\\s+", "");
                String lastContext = assignments.get(assignments.size() - 1).getContext().replaceAll("\\s+", "");

                // If contexts are completely different (no common substrings > 5 chars)
                if (!hasCommonPattern(firstContext, lastContext)) {
                    createIssue(context, file, "VariableReuseWithDifferentSemantics",
                        assignments.get(1).getLine(),
                        String.format("Variable '%s' reused for different purposes, consider using separate variables",
                            symbol.getName())
                    ).save();
                }
            }
        }
    }

    /**
     * Rule 19: Variable captured incorrectly in closures.
     * Variable captured by reference when value capture was intended.
     */
    public static void detectIncorrectClosureCapture(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (symbol.isModuleVariable()) {
                checkForIncorrectClosure(context, file, symbol);
            }
        }
    }

    private static void checkForIncorrectClosure(SensorContext context, InputFile file, Symbol symbol) {
        Scope symbolScope = symbol.getScope();
        boolean hasLoop = isLoopScope(symbolScope);

        if (hasLoop) {
            checkFunctionScopesInLoop(context, file, symbol, symbolScope);
        }
    }

    private static boolean isLoopScope(Scope scope) {
        return scope.getName() != null
               && (scope.getName().contains("Do") || scope.getName().contains("Table"));
    }

    private static void checkFunctionScopesInLoop(SensorContext context, InputFile file, Symbol symbol, Scope symbolScope) {
        for (Scope childScope : symbolScope.getChildren()) {
            if (childScope.getType() == ScopeType.FUNCTION) {
                checkSymbolCaptureInFunction(context, file, symbol, childScope);
            }
        }
    }

    private static void checkSymbolCaptureInFunction(SensorContext context, InputFile file, Symbol symbol, Scope childScope) {
        for (SymbolReference ref : symbol.getReferences()) {
            if (isReferenceInScope(ref, childScope)) {
                createIssue(context, file, "IncorrectClosureCapture", ref.getLine(),
                    String.format(
                        "Loop variable '%s' captured in closure, will capture final value only. Use With[] to capture current value.",
                        symbol.getName())
                ).save();
                break;
            }
        }
    }

    /**
     * Rule 20: Variable scope leaks through dynamic evaluation.
     * Variable escapes scope via ToExpression or similar dynamic evaluation.
     */
    public static void detectScopeLeakThroughDynamicEvaluation(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (!symbol.isModuleVariable()) {
                continue;
            }

            for (SymbolReference ref : symbol.getAllReferencesSorted()) {
                String contextStr = ref.getContext();

                // Check for dynamic evaluation functions
                if (contextStr.matches(".*(ToExpression|Symbol|Evaluate|ReleaseHold)\\s*+\\[.*"
                        + java.util.regex.Pattern.quote(symbol.getName()) + ".*")) {
                    createIssue(context, file, "ScopeLeakThroughDynamicEvaluation", ref.getLine(),
                        String.format("Module variable '%s' used in dynamic evaluation, may leak scope",
                            symbol.getName())
                    ).save();
                    break;
                }
            }
        }
    }

    // Helper methods

    private static boolean isParentChildRelation(Scope scope1, Scope scope2) {
        // Check if scope1 is parent of scope2 or vice versa
        Scope current = scope2.getParent();
        while (current != null) {
            if (current == scope1) {
                return true;
            }
            current = current.getParent();
        }

        current = scope1.getParent();
        while (current != null) {
            if (current == scope2) {
                return true;
            }
            current = current.getParent();
        }

        return false;
    }

    private static boolean hasCycle(String node, Map<String, Set<String>> graph,
                                    Set<String> visited, Set<String> recStack) {
        visited.add(node);
        recStack.add(node);

        Set<String> neighbors = graph.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    if (hasCycle(neighbor, graph, visited, recStack)) {
                        return true;
                    }
                } else if (recStack.contains(neighbor)) {
                    return true;
                }
            }
        }

        recStack.remove(node);
        return false;
    }

    private static boolean hasCommonPattern(String str1, String str2) {
        // Check if strings share common substring > 5 characters
        int minLen = Math.min(str1.length(), str2.length());
        for (int len = minLen; len >= 5; len--) {
            for (int i = 0; i <= str1.length() - len; i++) {
                String substring = str1.substring(i, i + len);
                if (str2.contains(substring)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper to create issue.
     */
    private static NewIssue createIssue(SensorContext context, InputFile file,
                                       String ruleKey, int line, String message) {
        NewIssue issue = context.newIssue()
            .forRule(RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY, ruleKey));

        issue.at(issue.newLocation()
            .on(file)
            .at(file.selectLine(Math.max(1, Math.min(line, file.lines()))))
            .message(message));

        return issue;
    }
}
