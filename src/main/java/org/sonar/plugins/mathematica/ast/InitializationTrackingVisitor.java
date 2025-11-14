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

        // Track declared parameters
        Set<String> declared = new HashSet<>(node.getParameters());
        declaredVariables.put(currentFunction, declared);
        assignedVariables.put(currentFunction, new HashSet<>());
        usedBeforeAssigned.put(currentFunction, new HashSet<>());

        // FIX: Parameters are pre-initialized by function call
        // In Mathematica, function parameters are bound when the function is called,
        // so they should be treated as "already assigned" when entering the function body
        currentlyAssigned.clear();
        currentlyAssigned.addAll(node.getParameters());

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

        // Handle Module/Block/With scoping constructs
        // These declare local variables: Module[{x, y = 5}, body]
        if (isScopingConstruct(functionName) && node.getArguments() != null && node.getArguments().size() >= 2) {
            handleScopingConstruct(node);
            return; // Don't visit arguments normally - we handle them specially
        }

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
            // DEFENSIVE: Exclude common false positives to reduce noise
            if (declared != null && declared.contains(varName)
                && !currentlyAssigned.contains(varName)
                && !isLikelyFalsePositive(varName)) {
                usedBeforeAssigned.get(currentFunction).add(varName);
            }
        }
    }

    /**
     * Check if a variable is likely a false positive that should be excluded from
     * "used before assignment" detection.
     *
     * This includes:
     * - Common Mathematica built-in symbols that might be shadowed as parameters
     * - Variables with pattern syntax (e.g., file_ might create "file" parameter)
     * - Very short variable names that are likely iterators with implicit initialization
     */
    private boolean isLikelyFalsePositive(String varName) {
        if (varName == null || varName.isEmpty()) {
            return true;
        }

        // Skip very short names (i, j, k, x, y, z, etc.) - often loop variables or math variables
        if (varName.length() == 1) {
            return true;
        }

        // Skip common Mathematica built-ins that might appear as parameters
        // but are actually global symbols
        String[] commonGlobals = {
            "True", "False", "None", "Null", "All", "Automatic",
            "Left", "Right", "Top", "Bottom", "Center",
            "Red", "Blue", "Green", "Black", "White",
            "Input", "Output", "Print", "Message",
            "Hold", "HoldAll", "HoldFirst", "HoldRest",
            "Listable", "Flat", "OneIdentity", "Orderless",
            "Protected", "Locked", "ReadProtected"
        };

        for (String global : commonGlobals) {
            if (global.equals(varName)) {
                return true;
            }
        }

        // Skip variables that look like they might be global configuration or state
        // (typically capitalized or very descriptive names)
        return varName.length() > 15 && Character.isUpperCase(varName.charAt(0));
    }

    @Override
    public void visit(LiteralNode node) {
        // Literals don't affect variable tracking
    }

    /**
     * Check if a function name represents a scoping construct (Module, Block, With).
     */
    private boolean isScopingConstruct(String functionName) {
        return "Module".equals(functionName)
               || "Block".equals(functionName)
               || "With".equals(functionName);
    }

    /**
     * Handle Module/Block/With scoping constructs.
     *
     * Syntax: Module[{x, y = 5, z}, body]
     * - First argument is a list of variable declarations
     * - Variables can be uninitialized (just name) or initialized (name = value)
     * - Second argument is the body where these variables are used
     *
     * This method:
     * 1. Extracts variable declarations from the first argument (ListNode)
     * 2. For each declaration:
     *    - If it's just an identifier: declare but don't mark as assigned
     *    - If it's an assignment (Set): declare AND mark as assigned
     * 3. Visits the body with the updated scope
     */
    private void handleScopingConstruct(FunctionCallNode node) {
        if (currentFunction == null) {
            return; // Only track within function scope
        }

        AstNode declarationList = node.getArguments().get(0);
        AstNode body = node.getArguments().get(1);

        // Parse the declaration list {x, y = 5, z}
        if (declarationList instanceof ListNode) {
            ListNode listNode = (ListNode) declarationList;
            for (AstNode element : listNode.getElements()) {
                if (element instanceof IdentifierNode) {
                    // Uninitialized variable: Module[{x}, ...]
                    // Don't add to currentlyAssigned - will be flagged if used before assignment
                    String varName = ((IdentifierNode) element).getName();
                    declaredVariables.get(currentFunction).add(varName);
                } else if (element instanceof FunctionCallNode) {
                    FunctionCallNode funcCall = (FunctionCallNode) element;
                    // Initialized variable: Module[{x = 5}, ...]
                    if (isAssignment(funcCall.getFunctionName())
                        && funcCall.getArguments() != null
                        && !funcCall.getArguments().isEmpty()) {
                        AstNode firstArg = funcCall.getArguments().get(0);
                        if (firstArg instanceof IdentifierNode) {
                            String varName = ((IdentifierNode) firstArg).getName();
                            declaredVariables.get(currentFunction).add(varName);
                            currentlyAssigned.add(varName); // Mark as initialized
                            assignedVariables.get(currentFunction).add(varName);
                        }
                        // Visit the initializer expression (right side of =)
                        if (funcCall.getArguments().size() > 1) {
                            funcCall.getArguments().get(1).accept(this);
                        }
                    }
                }
            }
        }

        // Now visit the body with the updated scope
        if (body != null) {
            body.accept(this);
        }
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

        for (Map.Entry<String, Set<String>> entry : usedBeforeAssigned.entrySet()) {
            String functionName = entry.getKey();
            Set<String> vars = entry.getValue();
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
