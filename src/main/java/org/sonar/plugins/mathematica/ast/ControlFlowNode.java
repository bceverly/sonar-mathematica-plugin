package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing control flow constructs: If, Which, Switch.
 *
 * Mathematica control flow:
 * - If[cond, thenExpr, elseExpr]: Simple conditional
 * - Which[cond1, expr1, cond2, expr2, ...]: Multiple conditions
 * - Switch[expr, pattern1, result1, pattern2, result2, ...]: Pattern matching dispatch
 *
 * This is Item 2 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class ControlFlowNode extends AstNode {

    public enum ControlFlowType {
        IF,
        WHICH,
        SWITCH
    }

    private final ControlFlowType controlFlowType;
    private final AstNode condition;  // For If and Switch (expression to match)
    private final List<AstNode> branches;  // All branches (conditions + results)
    private final AstNode elseClause;  // Optional else/default

    public ControlFlowNode(
        ControlFlowType controlFlowType,
        AstNode condition,
        List<AstNode> branches,
        AstNode elseClause,
        SourceLocation location
    ) {
        super(NodeType.CONTROL_FLOW, location);
        this.controlFlowType = controlFlowType;
        this.condition = condition;
        this.branches = branches != null ? branches : new ArrayList<>();
        this.elseClause = elseClause;
    }

    public ControlFlowNode(
        ControlFlowType controlFlowType,
        AstNode condition,
        List<AstNode> branches,
        AstNode elseClause,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        this(controlFlowType, condition, branches, elseClause,
             new SourceLocation(startLine, startColumn, endLine, endColumn));
    }

    public ControlFlowType getControlFlowType() {
        return controlFlowType;
    }

    public AstNode getCondition() {
        return condition;
    }

    public List<AstNode> getBranches() {
        return branches;
    }

    public AstNode getElseClause() {
        return elseClause;
    }

    @Override
    public void accept(AstVisitor visitor) {
        // Visit condition
        if (condition != null) {
            condition.accept(visitor);
        }
        // Visit all branches
        for (AstNode branch : branches) {
            if (branch != null) {
                branch.accept(visitor);
            }
        }
        // Visit else clause
        if (elseClause != null) {
            elseClause.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return String.format("%s[cond=%s, branches=%d]", controlFlowType, condition, branches.size());
    }
}
