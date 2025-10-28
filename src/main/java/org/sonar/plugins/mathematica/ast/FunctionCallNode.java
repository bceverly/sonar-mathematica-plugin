package org.sonar.plugins.mathematica.ast;

import java.util.List;

/**
 * AST node representing a function call.
 *
 * Examples:
 * - Sin[x]
 * - Map[f, list]
 * - Module[{x = 5}, x + 1]
 */
public class FunctionCallNode extends AstNode {

    private final String functionName;
    private final List<AstNode> arguments;

    public FunctionCallNode(String functionName, List<AstNode> arguments,
                           int startLine, int startColumn, int endLine, int endColumn) {
        super(NodeType.FUNCTION_CALL, startLine, startColumn, endLine, endColumn);
        this.functionName = functionName;
        this.arguments = arguments;

        if (arguments != null) {
            children.addAll(arguments);
        }
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<AstNode> getArguments() {
        return arguments;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
        if (arguments != null) {
            for (AstNode arg : arguments) {
                arg.accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("FunctionCall(%s, args=%d)", functionName,
            arguments != null ? arguments.size() : 0);
    }
}
