package org.sonar.plugins.mathematica.ast;

/**
 * AST node representing operators (binary, unary, and special Mathematica operators).
 *
 * Supports:
 * - Binary operators: +, -, *, /, ^, &&, ||, ==, !=, <, >, <=, >=, etc.
 * - Unary operators: !, -, +, Not
 * - Special operators: /@ (Map), @@ (Apply), @@@ (Apply at level 1), /. (ReplaceAll), etc.
 *
 * This is Items 7-9 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class OperatorNode extends AstNode {

    public enum OperatorType {
        // Binary arithmetic
        ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER,
        // Binary logical
        AND, OR, NOT,
        // Binary comparison
        EQUAL, NOT_EQUAL, LESS_THAN, GREATER_THAN, LESS_EQUAL, GREATER_EQUAL,
        // Unary
        UNARY_MINUS, UNARY_PLUS, LOGICAL_NOT,
        // Special Mathematica operators
        MAP,              // /@ //NOSONAR - Mathematica operator syntax example
        APPLY,            // @@ //NOSONAR - Mathematica operator syntax example
        APPLY_LEVEL_1,    // @@@ //NOSONAR - Mathematica operator syntax example
        REPLACE_ALL,      // /. //NOSONAR - Mathematica operator syntax example
        REPLACE_REPEATED, // //. //NOSONAR - Mathematica operator syntax example
        CONDITION,        // /; //NOSONAR - Mathematica operator syntax example, not commented code
        POSTFIX,          // // //NOSONAR - Mathematica operator syntax example
        PREFIX,           // @ //NOSONAR - Mathematica operator syntax example
        INFIX,            // ~f~ //NOSONAR - Mathematica operator syntax example
        FUNCTION_APPLICATION, // f[x] //NOSONAR - Mathematica syntax example
        PART,             // [[...]] //NOSONAR - Mathematica syntax example
        RULE,             // -> //NOSONAR - Mathematica operator syntax example
        DELAYED_RULE      // :> //NOSONAR - Mathematica operator syntax example
    }

    private final OperatorType operatorType;
    private final AstNode leftOperand;   // Null for unary operators
    private final AstNode rightOperand;
    private final String operatorSymbol; // Original symbol (for debugging)

    public OperatorNode(
        OperatorType operatorType,
        AstNode leftOperand,
        AstNode rightOperand,
        String operatorSymbol,
        SourceLocation location
    ) {
        super(NodeType.OPERATOR, location);
        this.operatorType = operatorType;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operatorSymbol = operatorSymbol;
    }

    @SuppressWarnings("java:S107") // Backward compatibility constructor, prefer SourceLocation-based one
    public OperatorNode(
        OperatorType operatorType,
        AstNode leftOperand,
        AstNode rightOperand,
        String operatorSymbol,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        this(operatorType, leftOperand, rightOperand, operatorSymbol,
             new SourceLocation(startLine, startColumn, endLine, endColumn));
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public AstNode getLeftOperand() {
        return leftOperand;
    }

    public AstNode getRightOperand() {
        return rightOperand;
    }

    public String getOperatorSymbol() {
        return operatorSymbol;
    }

    public boolean isUnary() {
        return leftOperand == null;
    }

    public boolean isBinary() {
        return leftOperand != null && rightOperand != null;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (leftOperand != null) {
            leftOperand.accept(visitor);
        }
        if (rightOperand != null) {
            rightOperand.accept(visitor);
        }
    }

    @Override
    public String toString() {
        if (isUnary()) {
            return String.format("%s %s", operatorSymbol, rightOperand);
        } else {
            return String.format("%s %s %s", leftOperand, operatorSymbol, rightOperand);
        }
    }
}
