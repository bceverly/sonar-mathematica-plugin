package org.sonar.plugins.mathematica.ast;

import java.util.List;

/**
 * AST node representing a function definition.
 *
 * Examples:
 * - f[x_] := x^2
 * - g[x_?NumericQ, y_List] := Module[{z}, z = x + y]
 * - MyFunc[opts:OptionsPattern[]] := OptionValue[option]
 */
public class FunctionDefNode extends AstNode {

    private final String functionName;
    private final List<String> parameters;
    private final AstNode body;
    private final boolean isDelayed;  // := vs =

    public FunctionDefNode(String functionName, List<String> parameters, AstNode body,
                           boolean isDelayed, int startLine, int startColumn, int endLine, int endColumn) {
        super(NodeType.FUNCTION_DEF, startLine, startColumn, endLine, endColumn);
        this.functionName = functionName;
        this.parameters = parameters;
        this.body = body;
        this.isDelayed = isDelayed;

        if (body != null) {
            addChild(body);
        }
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public AstNode getBody() {
        return body;
    }

    public boolean isDelayed() {
        return isDelayed;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
        if (body != null) {
            body.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return String.format("FunctionDef(%s, params=%s, delayed=%s)",
            functionName, parameters, isDelayed);
    }
}
