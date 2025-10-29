package org.sonar.plugins.mathematica.rules;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.mathematica.symboltable.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detector for symbol table-based rules.
 * Implements advanced variable lifetime and scope analysis rules.
 */
public class SymbolTableDetector {

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
     */
    public static void detectAssignedButNeverRead(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAssignedButNeverReadSymbols()) {
            createIssue(context, file, "AssignedButNeverRead", symbol.getDeclarationLine(),
                String.format("Variable '%s' is assigned but its value is never read", symbol.getName())
            ).save();
        }
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
                    .anyMatch(ref -> ref.getLine() > assignment.getLine() &&
                                    ref.getLine() < nextAssignment.getLine());

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
            if (!symbol.isModuleVariable()) {
                continue; // Only check Module/Block variables
            }

            List<SymbolReference> allRefs = symbol.getAllReferencesSorted();
            if (allRefs.isEmpty()) {
                continue;
            }

            // Check if all references are within a child scope
            Scope symbolScope = symbol.getScope();
            if (symbolScope.getChildren().isEmpty()) {
                continue;
            }

            for (Scope childScope : symbolScope.getChildren()) {
                boolean allInChild = allRefs.stream()
                    .allMatch(ref -> ref.getLine() >= childScope.getStartLine() &&
                                    ref.getLine() <= childScope.getEndLine());

                if (allInChild) {
                    createIssue(context, file, "VariableInWrongScope", symbol.getDeclarationLine(),
                        String.format("Variable '%s' could be declared in inner scope (lines %d-%d)",
                            symbol.getName(), childScope.getStartLine(), childScope.getEndLine())
                    ).save();
                    break;
                }
            }
        }
    }

    /**
     * Rule 10: Variable escapes scope via closure.
     * Module variable captured in closure will fail after Module exits.
     */
    public static void detectVariableEscapesScope(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (!symbol.isModuleVariable()) {
                continue;
            }

            // Check if symbol is used in a function definition (potential closure)
            Scope symbolScope = symbol.getScope();
            for (Scope childScope : symbolScope.getChildren()) {
                if (childScope.getType() == ScopeType.FUNCTION) {
                    // Check if symbol is referenced in function
                    for (SymbolReference ref : symbol.getReferences()) {
                        if (ref.getLine() >= childScope.getStartLine() &&
                            ref.getLine() <= childScope.getEndLine()) {
                            createIssue(context, file, "VariableEscapesScope", ref.getLine(),
                                String.format("Module variable '%s' captured in closure may fail after Module exits",
                                    symbol.getName())
                            ).save();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Rule 11: Variable lifetime extends beyond necessary scope.
     * Variable is used in limited range but declared in wider scope.
     */
    public static void detectLifetimeExtendsBeyondScope(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            if (!symbol.isModuleVariable()) {
                continue;
            }

            List<SymbolReference> allRefs = symbol.getAllReferencesSorted();
            if (allRefs.size() < 2) {
                continue;
            }

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
                    if (writeScope != null && readScope != null && writeScope != readScope &&
                        !isParentChildRelation(writeScope, readScope)) {
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
     */
    public static void detectCircularVariableDependencies(SensorContext context, InputFile file, SymbolTable table) {
        // Skip large files - O(n²) complexity makes this prohibitively expensive
        if (table.getAllSymbols().size() > 200) {
            return;
        }

        // Build dependency graph
        Map<String, Set<String>> dependencies = new java.util.HashMap<>();

        for (Symbol symbol : table.getAllSymbols()) {
            Set<String> deps = new java.util.HashSet<>();

            // For each assignment, find what variables are referenced
            for (SymbolReference assignment : symbol.getAssignments()) {
                String context_str = assignment.getContext();
                // Simple heuristic: find variable names in assignment context
                for (Symbol otherSymbol : table.getAllSymbols()) {
                    if (!otherSymbol.getName().equals(symbol.getName()) &&
                        context_str.contains(otherSymbol.getName())) {
                        deps.add(otherSymbol.getName());
                    }
                }
            }

            if (!deps.isEmpty()) {
                dependencies.put(symbol.getName(), deps);
            }
        }

        // Detect cycles using DFS
        for (String varName : dependencies.keySet()) {
            Set<String> visited = new java.util.HashSet<>();
            Set<String> recStack = new java.util.HashSet<>();
            if (hasCycle(varName, dependencies, visited, recStack)) {
                Symbol symbol = table.getSymbolByName(varName);
                if (symbol != null) {
                    createIssue(context, file, "CircularVariableDependencies", symbol.getDeclarationLine(),
                        String.format("Variable '%s' has circular dependency with other variables", varName)
                    ).save();
                }
            }
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
                createIssue(context, file, "NamingConventionViolations", symbol.getDeclarationLine(),
                    String.format("Variable '%s' has single-character name, use descriptive name", name)
                ).save();
            } else if (name.matches(".*\\d+$") && !name.matches("^[a-z].*")) {
                // Variable ending in number but not lowercase (temp1, temp2 is ok, Temp1 is not)
                createIssue(context, file, "NamingConventionViolations", symbol.getDeclarationLine(),
                    String.format("Variable '%s' uses numbered suffix, consider more descriptive name", name)
                ).save();
            } else if (name.matches("^[A-Z][A-Z]+$")) {
                // All caps (CONSTANT style) but not marked as constant
                createIssue(context, file, "NamingConventionViolations", symbol.getDeclarationLine(),
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
     */
    public static void detectTypeInconsistency(SensorContext context, InputFile file, SymbolTable table) {
        for (Symbol symbol : table.getAllSymbols()) {
            Set<String> suspectedTypes = new java.util.HashSet<>();

            for (SymbolReference ref : symbol.getAllReferencesSorted()) {
                String context_str = ref.getContext();

                // Heuristics for type detection
                if (context_str.matches(".*\\+\\s*\".*") || context_str.matches(".*\".*\\+.*")) {
                    suspectedTypes.add("string");
                } else if (context_str.matches(".*\\[\\[.*\\]\\].*") || context_str.matches(".*Part\\[.*")) {
                    suspectedTypes.add("list");
                } else if (context_str.matches(".*[\\+\\-\\*/]\\s*\\d+.*")) {
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
            if (!symbol.isModuleVariable()) {
                continue;
            }

            Scope symbolScope = symbol.getScope();
            boolean hasLoop = symbolScope.getName() != null &&
                              (symbolScope.getName().contains("Do") || symbolScope.getName().contains("Table"));

            // Check if used in function definition inside loop
            for (Scope childScope : symbolScope.getChildren()) {
                if (childScope.getType() == ScopeType.FUNCTION && hasLoop) {
                    for (SymbolReference ref : symbol.getReferences()) {
                        if (ref.getLine() >= childScope.getStartLine() &&
                            ref.getLine() <= childScope.getEndLine()) {
                            createIssue(context, file, "IncorrectClosureCapture", ref.getLine(),
                                String.format("Loop variable '%s' captured in closure, will capture final value only. Use With[] to capture current value.",
                                    symbol.getName())
                            ).save();
                            break;
                        }
                    }
                }
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
                String context_str = ref.getContext();

                // Check for dynamic evaluation functions
                if (context_str.matches(".*(ToExpression|Symbol|Evaluate|ReleaseHold)\\s*\\[.*" +
                                       java.util.regex.Pattern.quote(symbol.getName()) + ".*")) {
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
