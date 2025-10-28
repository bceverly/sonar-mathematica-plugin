package org.sonar.plugins.mathematica.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Example AST visitor that finds unused variables.
 *
 * This demonstrates how the AST can be used for semantic analysis
 * that goes beyond simple pattern matching.
 *
 * Algorithm:
 * 1. Track all variable declarations (function parameters)
 * 2. Track all variable uses (identifiers in expressions)
 * 3. Report variables that are declared but never used
 *
 * This is more accurate than regex-based detection because:
 * - It understands scope (Module vs. Global)
 * - It can distinguish declarations from uses
 * - It doesn't match variable names in comments/strings
 */
public class UnusedVariableVisitor implements AstVisitor {

    private final Map<String, Set<String>> declaredVariables;  // function -> variables
    private final Map<String, Set<String>> usedVariables;      // function -> variables
    private String currentFunction;

    public UnusedVariableVisitor() {
        this.declaredVariables = new HashMap<>();
        this.usedVariables = new HashMap<>();
        this.currentFunction = null;
    }

    @Override
    public void visit(FunctionDefNode node) {
        // Enter function scope
        currentFunction = node.getFunctionName();

        // Track declared parameters
        Set<String> declared = new HashSet<>(node.getParameters());
        declaredVariables.put(currentFunction, declared);
        usedVariables.put(currentFunction, new HashSet<>());

        // Visit function body
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        // Exit function scope
        currentFunction = null;
    }

    @Override
    public void visit(FunctionCallNode node) {
        // Function call - doesn't use parameters directly
        // But visit arguments to find identifier uses
        if (node.getArguments() != null) {
            for (AstNode arg : node.getArguments()) {
                arg.accept(this);
            }
        }
    }

    @Override
    public void visit(IdentifierNode node) {
        // Variable use
        if (currentFunction != null) {
            usedVariables.get(currentFunction).add(node.getName());
        }
    }

    @Override
    public void visit(LiteralNode node) {
        // Literals don't use variables
    }

    /**
     * Get unused variables for a specific function.
     */
    public Set<String> getUnusedVariables(String functionName) {
        Set<String> declared = declaredVariables.getOrDefault(functionName, new HashSet<>());
        Set<String> used = usedVariables.getOrDefault(functionName, new HashSet<>());

        Set<String> unused = new HashSet<>(declared);
        unused.removeAll(used);
        return unused;
    }

    /**
     * Get all unused variables across all functions.
     */
    public Map<String, Set<String>> getAllUnusedVariables() {
        Map<String, Set<String>> result = new HashMap<>();

        for (String functionName : declaredVariables.keySet()) {
            Set<String> unused = getUnusedVariables(functionName);
            if (!unused.isEmpty()) {
                result.put(functionName, unused);
            }
        }

        return result;
    }

    /**
     * Example usage:
     *
     * <pre>
     * MathematicaParser parser = new MathematicaParser();
     * List&lt;AstNode&gt; ast = parser.parse(content);
     *
     * UnusedVariableVisitor visitor = new UnusedVariableVisitor();
     * for (AstNode node : ast) {
     *     node.accept(visitor);
     * }
     *
     * Map&lt;String, Set&lt;String&gt;&gt; unused = visitor.getAllUnusedVariables();
     * // unused = {"myFunction": {"x", "y"}} means myFunction has unused params x and y
     * </pre>
     */
}
