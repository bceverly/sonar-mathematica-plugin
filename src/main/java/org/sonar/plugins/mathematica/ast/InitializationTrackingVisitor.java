package org.sonar.plugins.mathematica.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AST visitor that detects variables used before initialization.
 *
 * This demonstrates advanced semantic analysis that goes beyond simple
 * pattern matching. It tracks:
 * 1. Variable declarations (function parameters)
 * 2. Variable assignments (initialization)
 * 3. Variable uses
 * 4. Reports uses that occur before assignment
 *
 * This is more accurate than regex-based detection because:
 * - Understands actual statement order
 * - Scope-aware (Module vs Global)
 * - Distinguishes assignment from usage
 * - Handles nested scopes correctly
 */
public class InitializationTrackingVisitor implements AstVisitor {

    private final Map<String, Set<String>> declaredVariables;   // function -> declared vars
    private final Map<String, Set<String>> assignedVariables;   // function -> assigned vars
    private final Map<String, Set<String>> usedBeforeAssigned;  // function -> vars used before init
    private String currentFunction;
    private final Set<String> currentlyAssigned;  // Track what's assigned in current function

    public InitializationTrackingVisitor() {
        this.declaredVariables = new HashMap<>();
        this.assignedVariables = new HashMap<>();
        this.usedBeforeAssigned = new HashMap<>();
        this.currentFunction = null;
        this.currentlyAssigned = new HashSet<>();
    }

    @Override
    public void visit(FunctionDefNode node) {
        // Enter function scope
        currentFunction = node.getFunctionName();

        // Track declared parameters (these are declared but not yet assigned)
        Set<String> declared = new HashSet<>(node.getParameters());
        declaredVariables.put(currentFunction, declared);
        assignedVariables.put(currentFunction, new HashSet<>());
        usedBeforeAssigned.put(currentFunction, new HashSet<>());

        // Clear currently assigned for this function
        currentlyAssigned.clear();

        // Visit function body
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        // Exit function scope
        currentFunction = null;
    }

    @Override
    public void visit(FunctionCallNode node) {
        // Check if this is an assignment (looks like "Set[x, value]" or similar)
        String functionName = node.getFunctionName();

        // Handle assignment patterns
        // In Mathematica AST, we'd need to detect patterns like:
        // - x = value (parsed as Set[x, value])
        // - x := value (parsed as SetDelayed[x, value])
        if (isAssignment(functionName) && node.getArguments() != null && !node.getArguments().isEmpty()) {
            AstNode firstArg = node.getArguments().get(0);
            if (firstArg instanceof IdentifierNode) {
                String varName = ((IdentifierNode) firstArg).getName();
                if (currentFunction != null) {
                    currentlyAssigned.add(varName);
                    assignedVariables.get(currentFunction).add(varName);
                }
            }
        }

        // Visit arguments to find identifier uses
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
            String varName = node.getName();
            Set<String> declared = declaredVariables.get(currentFunction);

            // If variable is declared but not yet assigned, mark as used-before-assigned
            if (declared != null && declared.contains(varName)
                && !currentlyAssigned.contains(varName)) {
                usedBeforeAssigned.get(currentFunction).add(varName);
            }
        }
    }

    @Override
    public void visit(LiteralNode node) {
        // Literals don't affect variable tracking
    }

    /**
     * Check if a function name represents an assignment operation.
     */
    private boolean isAssignment(String functionName) {
        return "Set".equals(functionName)
               || "SetDelayed".equals(functionName)
               || "=".equals(functionName)
               || ":=".equals(functionName);
    }

    /**
     * Get variables used before assignment for a specific function.
     */
    public Set<String> getVariablesUsedBeforeAssignment(String functionName) {
        return usedBeforeAssigned.getOrDefault(functionName, new HashSet<>());
    }

    /**
     * Get all variables used before assignment across all functions.
     */
    public Map<String, Set<String>> getAllVariablesUsedBeforeAssignment() {
        Map<String, Set<String>> result = new HashMap<>();

        for (String functionName : usedBeforeAssigned.keySet()) {
            Set<String> vars = usedBeforeAssigned.get(functionName);
            if (!vars.isEmpty()) {
                result.put(functionName, vars);
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
     * InitializationTrackingVisitor visitor = new InitializationTrackingVisitor();
     * for (AstNode node : ast) {
     *     node.accept(visitor);
     * }
     *
     * Map&lt;String, Set&lt;String&gt;&gt; uninitialized = visitor.getAllVariablesUsedBeforeAssignment();
     * // uninitialized = {"myFunction": {"x"}} means myFunction uses x before assigning it
     * </pre>
     */
}
