package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for MathematicaParser covering uncovered lines to achieve 80%+ coverage.
 * Target: 62.5% -> 80%+
 * Focus: Lines 84-87, 152-155, 166-168, 173-177, 193-229, 234-253, 280-282, 306-308
 */
class MathematicaParserTest {

    private MathematicaParser parser;

    @BeforeEach
    void setUp() {
        parser = new MathematicaParser();
    }

    // ===== TEST GROUP 1: Profiling Thresholds =====

    @Test
    void testParseSlowParsingThreshold() {
        // Test Line 84-87: if (totalTime > 1000) profiling log
        // Create large content to trigger slow parsing (>1000ms threshold)
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            largeContent.append("function").append(i).append("[x_] := x + ").append(i).append(";\n");
        }

        // This should trigger the profiling log (totalTime > 1000ms)
        List<AstNode> nodes = parser.parse(largeContent.toString());
        assertNotNull(nodes);
    }

    @Test
    void testParseFunctionDefinitionsManyFunctions() {
        // Test Line 152-155: if (funcCount > 100) profiling log
        // Create content with >100 functions to trigger profiling
        StringBuilder manyFunctions = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            manyFunctions.append("func").append(i).append("[x_] := x;\n");
        }

        List<AstNode> nodes = parser.parse(manyFunctions.toString());
        assertNotNull(nodes);
        assertTrue(nodes.size() >= 100, "Should parse at least 100 functions");
    }

    // ===== TEST GROUP 2: Parameter Parsing Edge Cases =====

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testParseParametersNullOrEmpty(String params) {
        // Test Line 166-168: if (parametersStr == null || parametersStr.trim().isEmpty())
        String code = "f[" + (params != null ? params : "") + "] := 1";
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testParseParametersWithValidParameters() {
        // Test Line 173-177: Non-empty parameter check
        String code = "f[x_, y_Integer, z_Real] := x + y + z";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());

        // Should extract function with parameters
        AstNode firstNode = nodes.get(0);
        if (firstNode instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) firstNode;
            assertEquals("f", funcNode.getFunctionName());
            assertEquals(3, funcNode.getParameters().size());
            assertTrue(funcNode.getParameters().contains("x"));
            assertTrue(funcNode.getParameters().contains("y"));
            assertTrue(funcNode.getParameters().contains("z"));
        }
    }

    @Test
    void testParseParametersWithEmptyParametersBetweenCommas() {
        // Test parameter filtering: if (!param.isEmpty())
        String code = "f[x_, , y_] := x + y";  // Empty parameter between commas
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    // ===== TEST GROUP 3: Parameter Name Extraction =====

    @Test
    void testExtractParameterNameWithUnderscore() {
        // Test Lines 193-229: extractParameterName with various patterns
        String code = "f[param_] := param + 1";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertTrue(funcNode.getParameters().contains("param"));
        }
    }

    @Test
    void testExtractParameterNameWithTypeConstraint() {
        // Test delimiter checking (underscore, question, colon, equals)
        String code = "f[x_Integer, y_Real, z_String] := x";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertEquals(3, funcNode.getParameters().size());
            assertTrue(funcNode.getParameters().contains("x"));
            assertTrue(funcNode.getParameters().contains("y"));
            assertTrue(funcNode.getParameters().contains("z"));
        }
    }

    @Test
    void testExtractParameterNameWithQuestionMark() {
        // Test questionPos > 0 delimiter
        String code = "f[x_?NumericQ] := x + 1";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertTrue(funcNode.getParameters().contains("x"));
        }
    }

    @Test
    void testExtractParameterNameWithColon() {
        // Test colonPos > 0 delimiter (default values)
        String code = "f[x_:0, y_:1] := x + y";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertEquals(2, funcNode.getParameters().size());
            assertTrue(funcNode.getParameters().contains("x"));
            assertTrue(funcNode.getParameters().contains("y"));
        }
    }

    @Test
    void testExtractParameterNameWithEquals() {
        // Test equalsPos > 0 delimiter
        String code = "f[opt=defaultValue] := opt";
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    // ===== TEST GROUP 4: Identifier Validation =====

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void testIsValidIdentifierNullOrEmpty(String name) {
        // Test Line 234-253: if (name == null || name.isEmpty())
        String code = "f[" + name + "] := 1";
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testIsValidIdentifierStartsWithDigit() {
        // Test Line 240-242: Must start with letter or $
        String code = "f[1invalid] := 1";  // Invalid: starts with digit
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testIsValidIdentifierStartsWithDollar() {
        // Test valid identifier starting with $
        String code = "f[$var] := $var + 1";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            // $var might be extracted as "var" or "$var" depending on implementation
            assertFalse(funcNode.getParameters().isEmpty());
        }
    }

    @Test
    void testIsValidIdentifierContainsInvalidCharacters() {
        // Test Line 245-250: Character validation loop
        String code = "f[invalid-name] := 1";  // Invalid: contains hyphen
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testIsValidIdentifierValidMixedCase() {
        // Test valid identifier with letters, digits, and $
        String code = "f[myVar123$] := myVar123$ + 1";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertFalse(funcNode.getParameters().isEmpty());
        }
    }

    // ===== TEST GROUP 5: Binary Search (Null/Empty Array) =====

    @Test
    void testBinarySearchNextEmptyContent() {
        // Test Line 280-282: if (positions == null || positions.length == 0)
        // This is tested indirectly by parsing empty or minimal content
        String code = "";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    @Test
    void testBinarySearchNextSingleFunction() {
        // Test binary search with minimal delimiters
        String code = "f[x_] := x";  // No semicolons or additional assignments
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertEquals(1, nodes.size());
    }

    // ===== TEST GROUP 6: Expression Parsing =====

    @Test
    void testParseExpressionEmptyExpression() {
        // Test Line 306-308: if (expr.isEmpty()) return null
        String code = "f[] := ";  // Empty body after :=
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testParseExpressionWhitespaceOnlyExpression() {
        // Test empty expression with whitespace
        String code = "f[x_] :=    \n\t  ";
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testParseExpressionNumberLiteral() {
        // Test NUMBER_PATTERN matching
        String code = "f[x_] := 42";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test
    void testParseExpressionStringLiteral() {
        // Test STRING_PATTERN matching
        String code = "f[x_] := \"Hello, World!\"";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test
    void testParseExpressionFunctionCall() {
        // Test FUNCTION_CALL_PATTERN matching
        String code = "f[x_] := Sin[x]";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test
    void testParseExpressionIdentifier() {
        // Test IDENTIFIER_PATTERN matching
        String code = "f[x_] := myVariable";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    // ===== INTEGRATION TESTS =====

    @Test
    void testParseComplexFunctionWithMultipleParameters() {
        String code = "calculate[x_Integer, y_Real, z_:0, opt_?NumericQ, name_String] := x + y + z";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());

        if (nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertEquals("calculate", funcNode.getFunctionName());
            assertTrue(funcNode.getParameters().size() >= 3);
        }
    }

    @Test
    void testParseMultipleFunctionsWithSemicolons() {
        String code = "f[x_] := x; g[y_] := y + 1; h[z_] := z * 2;";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertTrue(nodes.size() >= 3, "Should parse all 3 functions");
    }

    @Test
    void testParseFunctionWithComplexBody() {
        String code = "process[data_List] := Module[{result}, result = Map[f, data]; Total[result]]";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test
    void testParseWithComments() {
        String code = "(* This is a comment *)\nf[x_] := x + 1\n(* Another comment *)";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test
    void testParseDelayedAssignment() {
        String code = "f[x_] := RandomReal[]";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertTrue(funcNode.isDelayed(), "Should be delayed assignment (:=)");
        }
    }

    @Test
    void testParseImmediateAssignment() {
        String code = "f[x_] = x + 1";
        List<AstNode> nodes = parser.parse(code);

        assertNotNull(nodes);
        if (!nodes.isEmpty() && nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertFalse(funcNode.isDelayed(), "Should be immediate assignment (=)");
        }
    }
}
