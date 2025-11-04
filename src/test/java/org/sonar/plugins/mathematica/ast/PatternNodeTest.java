package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;

import org.sonar.plugins.mathematica.ast.AstNode.NodeType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PatternNodeTest {

    @Test
    void testBlankPattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 2);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.BLANK,
            null,
            null,
            null,
            null,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.BLANK);
        assertThat(node.getPatternName()).isNull();
        assertThat(node.getHeadType()).isNull();
        assertThat(node.getTestFunction()).isNull();
        assertThat(node.getCondition()).isNull();
        assertThat(node.getDefaultValue()).isNull();
        assertThat(node.getType()).isEqualTo(NodeType.PATTERN);
    }

    @Test
    void testNamedBlankPattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 3);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.NAMED_BLANK,
            "x",
            null,
            null,
            null,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.NAMED_BLANK);
        assertThat(node.getPatternName()).isEqualTo("x");
    }

    @Test
    void testTypedBlankPattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 10);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.TYPED_BLANK,
            "x",
            "Integer",
            null,
            null,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.TYPED_BLANK);
        assertThat(node.getPatternName()).isEqualTo("x");
        assertThat(node.getHeadType()).isEqualTo("Integer");
    }

    @Test
    void testPatternTestPattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 12);
        AstNode testFunc = new IdentifierNode("NumericQ", 1, 4, 1, 12);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.PATTERN_TEST,
            "x",
            null,
            testFunc,
            null,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.PATTERN_TEST);
        assertThat(node.getTestFunction()).isEqualTo(testFunc);
    }

    @Test
    void testConditionPattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 15);
        AstNode condition = new IdentifierNode("x > 0", 1, 7, 1, 12);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.CONDITION,
            "x",
            null,
            null,
            condition,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.CONDITION);
        assertThat(node.getCondition()).isEqualTo(condition);
    }

    @Test
    void testOptionalPattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 10);
        AstNode defaultValue = new LiteralNode("0", LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.OPTIONAL,
            "x",
            null,
            null,
            null,
            defaultValue,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.OPTIONAL);
        assertThat(node.getDefaultValue()).isEqualTo(defaultValue);
    }

    @Test
    void testAlternativesPattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 11);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.ALTERNATIVES,
            null,
            null,
            null,
            null,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.ALTERNATIVES);
    }

    @Test
    void testAcceptVisitor() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 20);
        AstNode testFunc = new IdentifierNode("test", 1, 5, 1, 9);
        AstNode condition = new IdentifierNode("cond", 1, 11, 1, 15);
        AstNode defaultVal = new LiteralNode("1", LiteralNode.LiteralType.INTEGER, 1, 17, 1, 18);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.PATTERN_TEST,
            "x",
            "Integer",
            testFunc,
            condition,
            defaultVal,
            loc
        );

        AstVisitor visitor = mock(AstVisitor.class);
        // Should process without exceptions
        node.accept(visitor);
        assertThat(node).isNotNull();
    }

    @Test
    void testToString() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 10);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.TYPED_BLANK,
            "myVar",
            "Real",
            null,
            null,
            null,
            loc
        );

        String str = node.toString();
        assertThat(str).contains("TYPED_BLANK").contains("myVar").contains("Real");
    }

    @Test
    void testBlankSequencePattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 3);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.BLANK_SEQUENCE,
            null,
            null,
            null,
            null,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.BLANK_SEQUENCE);
    }

    @Test
    void testBlankNullSequencePattern() {
        SourceLocation loc = new SourceLocation(1, 1, 1, 4);

        PatternNode node = new PatternNode(
            PatternNode.PatternType.BLANK_NULL_SEQUENCE,
            null,
            null,
            null,
            null,
            null,
            loc
        );

        assertThat(node.getPatternType()).isEqualTo(PatternNode.PatternType.BLANK_NULL_SEQUENCE);
    }
}
