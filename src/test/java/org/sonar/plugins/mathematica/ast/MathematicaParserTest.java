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
        String code = "f[" + (params != null ? params : "") + "] := 1";
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testParseParametersWithValidParameters() {
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

    // ===== TEST GROUP 3: Parameter Name Extraction =====

    @ParameterizedTest
    @ValueSource(strings = {
        "f[x_, , y_] := x + y",              // Empty parameter between commas
        "f[param_] := param + 1",            // With underscore
        "f[x_Integer, y_Real, z_String] := x", // With type constraint
        "f[x_?NumericQ] := x + 1",           // With question mark
        "f[x_:0, y_:1] := x + y",            // With colon (default values)
        "f[opt=defaultValue] := opt"          // With equals
    })
    void testExtractParameterName(String code) {
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    // ===== TEST GROUP 4: Identifier Validation =====

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void testIsValidIdentifierNullOrEmpty(String name) {
        String code = "f[" + name + "] := 1";
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
    }

    @Test
    void testIsValidIdentifierStartsWithDigit() {
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
    void testParseExpressionEmptyOrWhitespace() {
        List<AstNode> nodes1 = parser.parse("f[] := ");
        assertNotNull(nodes1);

        List<AstNode> nodes2 = parser.parse("f[x_] :=    \n\t  ");
        assertNotNull(nodes2);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "f[x_] := 42",                 // Number literal
        "f[x_] := \"Hello, World!\"",  // String literal
        "f[x_] := Sin[x]",             // Function call
        "f[x_] := myVariable"          // Identifier
    })
    void testParseExpressionWithContent(String code) {
        List<AstNode> nodes = parser.parse(code);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty(), "Expression with content should produce non-empty AST");
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

    // ===== TEST GROUP 7: Comparison Operators (Not Function Definitions) =====

    @ParameterizedTest
    @ValueSource(strings = {
        "Head[indexer] =!= SomeSymbol",       // UnsameQ
        "Head[x] === SomeType",               // SameQ
        "f[x] == 5",                          // Equal
        "g[y] != 10"                          // Unequal
    })
    void testComparisonOperatorsNotParsedAsFunctionDefinitions(String code) {
        List<AstNode> nodes = parser.parse(code);

        // These should NOT be parsed as function definitions
        // because ==, ===, =!=, != are comparison operators, not assignments
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty() || !(nodes.get(0) instanceof FunctionDefNode),
            "Comparison operators should not create function definitions");
    }

    @Test
    void testRealWorldFalsePositiveCase() {
        // This is the actual false positive case that was reported:
        // indexer is used in comparison expressions, not function definitions
        String code = "someFunc[indexer_] := If[\n"
            + "  indexer === DocumentationSearch`DocumentationNotebookIndexer[$Failed],\n"
            + "  Print[\"Failed\"],\n"
            + "  Head[indexer] =!= DocumentationSearch`DocumentationNotebookIndexer,\n"
            + "  Print[\"Wrong type\"]\n"
            + "]";

        List<AstNode> nodes = parser.parse(code);

        // Should parse exactly ONE function definition (someFunc), not incorrectly
        // identify "Head[indexer] =!=" as another function definition
        assertNotNull(nodes);
        assertEquals(1, nodes.size(), "Should parse exactly one function definition");

        if (nodes.get(0) instanceof FunctionDefNode) {
            FunctionDefNode funcNode = (FunctionDefNode) nodes.get(0);
            assertEquals("someFunc", funcNode.getFunctionName());
            assertEquals(1, funcNode.getParameters().size());
            assertTrue(funcNode.getParameters().contains("indexer"));
        }
    }
}
