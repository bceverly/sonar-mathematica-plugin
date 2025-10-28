package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all AST (Abstract Syntax Tree) nodes.
 *
 * An AST represents the structure of Mathematica code in a tree format,
 * enabling semantic analysis that goes beyond regex pattern matching.
 *
 * Node types:
 * - FunctionDefNode: Function definitions (f[x_] := ...)
 * - FunctionCallNode: Function calls (f[x])
 * - IdentifierNode: Variable/function names
 * - LiteralNode: Numbers, strings
 * - ListNode: Lists {1, 2, 3}
 * - PatternNode: Pattern matching (x_)
 * - OperatorNode: Operators (+, -, *, /, etc.)
 */
public abstract class AstNode {

    protected final NodeType type;
    protected final int startLine;
    protected final int startColumn;
    protected final int endLine;
    protected final int endColumn;
    protected final List<AstNode> children;

    public enum NodeType {
        FUNCTION_DEF,      // f[x_] := body
        FUNCTION_CALL,     // f[x]
        IDENTIFIER,        // variable or function name
        LITERAL,           // number or string
        LIST,              // {1, 2, 3}
        ASSOCIATION,       // <|a -> 1, b -> 2|>
        PATTERN,           // x_, x__, x___, x_?test
        OPERATOR,          // +, -, *, /, :=, ->, etc.
        CONTROL_FLOW,      // If, While, Do, For, etc.
        SCOPING,           // Module, Block, With
        COMPOUND,          // Multiple statements
        LOOP,              // Do, While, For, Table, NestWhile
        PURE_FUNCTION,     // #1 + #2 &, Function[{x, y}, x + y]
        PART,              // list[[i]]
        SPAN               // 1;;10
    }

    protected AstNode(NodeType type, int startLine, int startColumn, int endLine, int endColumn) {
        this.type = type;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.children = new ArrayList<>();
    }

    public NodeType getType() {
        return type;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public List<AstNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(AstNode child) {
        if (child != null) {
            children.add(child);
        }
    }

    /**
     * Accept a visitor for tree traversal.
     */
    public abstract void accept(AstVisitor visitor);

    /**
     * Get a string representation of this node and its children.
     */
    public String toTree() {
        return toTree(0);
    }

    protected String toTree(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent));
        sb.append(toString());
        sb.append("\n");
        for (AstNode child : children) {
            sb.append(child.toTree(indent + 1));
        }
        return sb.toString();
    }

    @Override
    public abstract String toString();
}
