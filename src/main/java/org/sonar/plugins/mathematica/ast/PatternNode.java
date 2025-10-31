package org.sonar.plugins.mathematica.ast;

/**
 * AST node representing pattern matching expressions.
 *
 * Mathematica patterns:
 * - _ (Blank): Matches any expression
 * - __ (BlankSequence): Matches sequence of one or more expressions
 * - ___ (BlankNullSequence): Matches sequence of zero or more expressions
 * - x_Integer (Blank with head): Matches expressions with specific head
 * - x_?NumericQ (PatternTest): Pattern with test function
 * - x_ /; x > 0 (Condition): Pattern with condition
 *
 * This is Item 4 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class PatternNode extends AstNode {

    public enum PatternType {
        BLANK,              // _
        BLANK_SEQUENCE,     // __
        BLANK_NULL_SEQUENCE, // ___
        NAMED_BLANK,        // x_
        TYPED_BLANK,        // x_Integer
        PATTERN_TEST,       // x_?NumericQ
        CONDITION,          // x_ /; x > 0
        OPTIONAL,           // x_:default
        ALTERNATIVES,       // a | b | c
        REPEATED,           // x..
        VERBATIM,           // Verbatim[expr]
        HOLD_PATTERN        // HoldPattern[patt]
    }

    private final PatternType patternType;
    private final String patternName;  // Variable name (if any)
    private final String headType;     // Type constraint (e.g., "Integer")
    private final AstNode testFunction; // Test function for PatternTest
    private final AstNode condition;    // Condition for Condition pattern
    private final AstNode defaultValue; // Default for Optional pattern

    public PatternNode(
        PatternType patternType,
        String patternName,
        String headType,
        AstNode testFunction,
        AstNode condition,
        AstNode defaultValue,
        SourceLocation location
    ) {
        super(NodeType.PATTERN, location);
        this.patternType = patternType;
        this.patternName = patternName;
        this.headType = headType;
        this.testFunction = testFunction;
        this.condition = condition;
        this.defaultValue = defaultValue;
    }

    public PatternType getPatternType() {
        return patternType;
    }

    public String getPatternName() {
        return patternName;
    }

    public String getHeadType() {
        return headType;
    }

    public AstNode getTestFunction() {
        return testFunction;
    }

    public AstNode getCondition() {
        return condition;
    }

    public AstNode getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (testFunction != null) {
            testFunction.accept(visitor);
        }
        if (condition != null) {
            condition.accept(visitor);
        }
        if (defaultValue != null) {
            defaultValue.accept(visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(patternType.toString());
        if (patternName != null) {
            sb.append("[name=").append(patternName).append("]");
        }
        if (headType != null) {
            sb.append("[type=").append(headType).append("]");
        }
        return sb.toString();
    }
}
