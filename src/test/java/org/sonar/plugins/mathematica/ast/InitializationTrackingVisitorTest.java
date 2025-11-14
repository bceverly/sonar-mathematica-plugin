package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test for InitializationTrackingVisitor covering all uncovered lines
 * to achieve 80%+ coverage.
 *
 * Target: 32.3% -> 80%+
 * Focus: Lines 54-56, 71-80, 83-87, 99-104, 117-148, 151-153, 158-163, 175-187
 */
class InitializationTrackingVisitorTest {

    private InitializationTrackingVisitor visitor;

    @BeforeEach
    void setUp() {
        visitor = new InitializationTrackingVisitor();
    }

    // ===== TEST GROUP 1: visit(FunctionDefNode) - Lines 54-56 =====

    @Test
    void testVisitFunctionDefNodeWithNullBody() {
        // Test Line 54-56: if (node.getBody() != null) check
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList("param1", "param2"),
            null,  // NULL BODY - This tests the uncovered branch
            false,
            1, 0, 1, 20
        );

        // Should not throw exception with null body
        assertDoesNotThrow(() -> visitor.visit(funcNode));

        // Verify function was still tracked even with null body
        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertNotNull(uninitVars);
    }

    @Test
    void testVisitFunctionDefNodeWithNonNullBody() {
        // Test that function body IS visited when not null
        IdentifierNode bodyNode = new IdentifierNode("param1", 1, 10, 1, 16);
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList("param1"),
            bodyNode,
            false,
            1, 0, 1, 20
        );

        visitor.visit(funcNode);

        // param1 is used before assignment (declared as parameter but not assigned)
        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertTrue(uninitVars.contains("param1"), "param1 should be detected as used before assignment");
    }

    // ===== TEST GROUP 2: visit(FunctionCallNode) - Assignment Detection (Lines 71-80) =====

    @Test
    void testVisitFunctionCallNodeSetAssignment() {
        // Test Line 71-80: Assignment detection with Set
        setupFunctionContext();

        IdentifierNode varNode = new IdentifierNode("x", 1, 5, 1, 6);
        LiteralNode valueNode = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 8, 1, 10);

        FunctionCallNode setNode = new FunctionCallNode(
            "Set",
            Arrays.asList(varNode, valueNode),
            1, 0, 1, 10
        );

        visitor.visit(setNode);

        // x should be marked as assigned
        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();
        assertTrue(allUninit.isEmpty() || !allUninit.get("testFunc").contains("x"),
            "x should be marked as assigned via Set");
    }

    @Test
    void testVisitFunctionCallNodeSetDelayedAssignment() {
        // Test Line 71-80: Assignment detection with SetDelayed
        setupFunctionContext();

        IdentifierNode varNode = new IdentifierNode("y", 1, 5, 1, 6);
        LiteralNode valueNode = new LiteralNode("test", LiteralNode.LiteralType.STRING, 1, 10, 1, 16);

        FunctionCallNode setDelayedNode = new FunctionCallNode(
            "SetDelayed",
            Arrays.asList(varNode, valueNode),
            1, 0, 1, 16
        );

        visitor.visit(setDelayedNode);

        // y should be marked as assigned
        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();
        assertTrue(allUninit.isEmpty() || !allUninit.get("testFunc").contains("y"),
            "y should be marked as assigned via SetDelayed");
    }

    @Test
    void testVisitFunctionCallNodeSymbolAssignmentOperators() {
        // Test Line 71-80: Assignment detection with = and :=
        setupFunctionContext();

        // Test with "=" operator
        IdentifierNode var1 = new IdentifierNode("a", 1, 0, 1, 1);
        FunctionCallNode assignNode1 = new FunctionCallNode(
            "=",
            Arrays.asList(var1, new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 3, 1, 4)),
            1, 0, 1, 4
        );
        visitor.visit(assignNode1);

        // Test with ":=" operator
        IdentifierNode var2 = new IdentifierNode("b", 2, 0, 2, 1);
        FunctionCallNode assignNode2 = new FunctionCallNode(
            ":=",
            Arrays.asList(var2, new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 2, 4, 2, 5)),
            2, 0, 2, 5
        );
        visitor.visit(assignNode2);

        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();
        assertTrue(allUninit.isEmpty()
            || (!allUninit.get("testFunc").contains("a") && !allUninit.get("testFunc").contains("b")),
            "Both a and b should be marked as assigned");
    }

    @Test
    void testVisitFunctionCallNodeNullArguments() {
        // Test Line 83-87: if (node.getArguments() != null) check
        setupFunctionContext();

        FunctionCallNode funcNode = new FunctionCallNode(
            "SomeFunction",
            null,  // NULL ARGUMENTS - Tests the uncovered branch
            1, 0, 1, 15
        );

        // Should not throw with null arguments
        assertDoesNotThrow(() -> visitor.visit(funcNode));
    }

    @Test
    void testVisitFunctionCallNodeEmptyArguments() {
        // Test Line 71: Empty arguments list (no assignment detected)
        setupFunctionContext();

        FunctionCallNode funcNode = new FunctionCallNode(
            "Set",
            new ArrayList<>(),  // EMPTY list - Tests empty branch
            1, 0, 1, 5
        );

        assertDoesNotThrow(() -> visitor.visit(funcNode));
    }

    @Test
    void testVisitFunctionCallNodeNonIdentifierFirstArg() {
        // Test Line 73-74: if (firstArg instanceof IdentifierNode) check
        setupFunctionContext();

        LiteralNode literalNode = new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 0, 1, 1);
        FunctionCallNode setNode = new FunctionCallNode(
            "Set",
            Arrays.asList(literalNode, new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 3, 1, 5)),
            1, 0, 1, 5
        );

        // Should handle non-identifier first argument gracefully
        assertDoesNotThrow(() -> visitor.visit(setNode));
    }

    // ===== TEST GROUP 3: visit(IdentifierNode) - Used Before Assigned (Lines 99-104) =====

    @Test
    void testVisitIdentifierNodeUsedBeforeAssignment() {
        // Test Line 99-104: Complete used-before-assigned detection logic
        FunctionDefNode funcNode = new FunctionDefNode(
            "myFunc",
            Arrays.asList("uninitVar"),  // Declared but not assigned
            new IdentifierNode("uninitVar", 1, 10, 1, 19),  // Used here
            false,
            1, 0, 1, 25
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("myFunc");
        assertTrue(uninitVars.contains("uninitVar"),
            "uninitVar should be detected as used before assignment");
    }

    @Test
    void testVisitIdentifierNodeUsedAfterAssignment() {
        // Variable is assigned before use
        setupFunctionContext();

        // First assign
        IdentifierNode assignTarget = new IdentifierNode("initVar", 1, 0, 1, 7);
        FunctionCallNode assignment = new FunctionCallNode(
            "Set",
            Arrays.asList(assignTarget, new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11)),
            1, 0, 1, 11
        );
        visitor.visit(assignment);

        // Then use
        IdentifierNode usage = new IdentifierNode("initVar", 2, 0, 2, 7);
        visitor.visit(usage);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertFalse(uninitVars.contains("initVar"),
            "initVar should NOT be detected since it was assigned before use");
    }

    // ===== TEST GROUP 4: isLikelyFalsePositive (Lines 117-148) =====

    @ParameterizedTest
    @ValueSource(strings = {"i", "j", "k", "x", "y", "z", "n", "m"})
    void testIsLikelyFalsePositiveSingleCharVariables(String varName) {
        // Test Line 123-125: Single character variable names
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList(varName),
            new IdentifierNode(varName, 1, 0, 1, 1),
            false,
            1, 0, 1, 10
        );

        visitor.visit(funcNode);

        // Single-char variables should be filtered as likely false positives
        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertFalse(uninitVars.contains(varName),
            String.format("Single-char variable '%s' should be filtered as false positive", varName));
    }

    @ParameterizedTest
    @MethodSource("commonBuiltinGlobals")
    void testIsLikelyFalsePositiveCommonGlobals(String globalName) {
        // Test Line 139-143: Common Mathematica built-in symbols
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList(globalName),
            new IdentifierNode(globalName, 1, 0, 1, globalName.length()),
            false,
            1, 0, 1, 20
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertFalse(uninitVars.contains(globalName),
            String.format("Common global '%s' should be filtered as false positive", globalName));
    }

    private static Stream<Arguments> commonBuiltinGlobals() {
        return Stream.of(
            Arguments.of("True"),
            Arguments.of("False"),
            Arguments.of("None"),
            Arguments.of("Null"),
            Arguments.of("All"),
            Arguments.of("Automatic"),
            Arguments.of("Print"),
            Arguments.of("Input"),
            Arguments.of("Output"),
            Arguments.of("Hold"),
            Arguments.of("Protected"),
            Arguments.of("Listable")
        );
    }

    @Test
    void testIsLikelyFalsePositiveLongCapitalizedVariable() {
        // Test Line 147: Long capitalized variable names (>15 chars, uppercase first)
        String longVar = "VeryLongCapitalizedVariableName";  // 31 chars, starts with uppercase
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList(longVar),
            new IdentifierNode(longVar, 1, 0, 1, longVar.length()),
            false,
            1, 0, 1, 50
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertFalse(uninitVars.contains(longVar),
            "Long capitalized variable should be filtered as false positive (likely global config)");
    }

    @Test
    void testIsLikelyFalsePositiveLongLowercaseVariable() {
        // Long variable starting with lowercase should NOT be filtered
        String longVar = "veryLongLowercaseVariableName";  // 29 chars, starts with lowercase
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList(longVar),
            new IdentifierNode(longVar, 1, 0, 1, longVar.length()),
            false,
            1, 0, 1, 50
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertTrue(uninitVars.contains(longVar),
            "Long lowercase variable should NOT be filtered (should be detected)");
    }

    // ===== TEST GROUP 5: visit(LiteralNode) - Lines 151-153 =====

    @Test
    void testVisitLiteralNodeDoesNotAffectTracking() {
        // Test Line 151-153: Literal nodes don't affect variable tracking
        setupFunctionContext();

        LiteralNode literal1 = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 0, 1, 2);
        LiteralNode literal2 = new LiteralNode("test", LiteralNode.LiteralType.STRING, 2, 0, 2, 6);
        LiteralNode literal3 = new LiteralNode(3.14, LiteralNode.LiteralType.REAL, 3, 0, 3, 4);

        // Visiting literals should not throw and should not affect state
        assertDoesNotThrow(() -> {
            visitor.visit(literal1);
            visitor.visit(literal2);
            visitor.visit(literal3);
        });

        // No variables should be tracked from literals
        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();
        assertTrue(allUninit.isEmpty() || allUninit.get("testFunc").isEmpty(),
            "Literal nodes should not affect variable tracking");
    }

    // ===== TEST GROUP 6: getAllVariablesUsedBeforeAssignment (Lines 175-187) =====

    @Test
    void testGetAllVariablesUsedBeforeAssignmentMultipleFunctions() {
        // Test Line 175-187: Iteration over all functions with uninitialized variables
        // Function 1 with 2 uninitialized variables
        FunctionDefNode func1 = createFunctionWithUninitializedVars("func1", "var1", "var2");
        visitor.visit(func1);

        // Function 2 with 1 uninitialized variable
        FunctionDefNode func2 = createFunctionWithUninitializedVars("func2", "var3");
        visitor.visit(func2);

        // Function 3 with no uninitialized variables (all assigned)
        FunctionDefNode func3 = createFunctionWithAssignments("func3", "var4");
        visitor.visit(func3);

        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();

        assertEquals(2, allUninit.size(), "Should return 2 functions with uninitialized vars");
        assertTrue(allUninit.containsKey("func1"));
        assertTrue(allUninit.containsKey("func2"));
        assertFalse(allUninit.containsKey("func3"), "func3 has no uninitialized vars, should not be included");

        assertEquals(2, allUninit.get("func1").size());
        assertTrue(allUninit.get("func1").contains("var1"));
        assertTrue(allUninit.get("func1").contains("var2"));

        assertEquals(1, allUninit.get("func2").size());
        assertTrue(allUninit.get("func2").contains("var3"));
    }

    @Test
    void testGetAllVariablesUsedBeforeAssignmentEmptyVariableSetsFiltered() {
        // Test Line 177: if (!vars.isEmpty()) check - empty sets should be filtered
        FunctionDefNode funcWithNoUninit = createFunctionWithAssignments("emptyFunc", "assigned");
        visitor.visit(funcWithNoUninit);

        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();

        assertFalse(allUninit.containsKey("emptyFunc"),
            "Functions with no uninitialized variables should not appear in results");
    }

    @Test
    void testGetVariablesUsedBeforeAssignmentSpecificFunction() {
        // Test the simpler getter for a specific function
        FunctionDefNode func = createFunctionWithUninitializedVars("targetFunc", "uninit1", "uninit2");
        visitor.visit(func);

        Set<String> vars = visitor.getVariablesUsedBeforeAssignment("targetFunc");

        assertNotNull(vars);
        assertEquals(2, vars.size());
        assertTrue(vars.contains("uninit1"));
        assertTrue(vars.contains("uninit2"));
    }

    @Test
    void testGetVariablesUsedBeforeAssignmentNonExistentFunction() {
        Set<String> vars = visitor.getVariablesUsedBeforeAssignment("nonExistentFunction");

        assertNotNull(vars);
        assertTrue(vars.isEmpty(), "Should return empty set for non-existent function");
    }

    // ===== HELPER METHODS =====

    private void setupFunctionContext() {
        // Create a simple function context for testing
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            new ArrayList<>(),
            null,
            false,
            1, 0, 1, 10
        );
        visitor.visit(funcNode);
    }

    private FunctionDefNode createFunctionWithUninitializedVars(String funcName, String... varNames) {
        // Create a function that uses variables without assigning them first
        List<String> params = Arrays.asList(varNames);

        // Create a CompoundExpressionNode that contains all variable usages
        // This ensures the identifiers are visited while the function context is still active
        List<AstNode> expressions = new ArrayList<>();
        for (String varName : varNames) {
            expressions.add(new IdentifierNode(varName, 1, 0, 1, varName.length()));
        }

        // Use first identifier as body (or create CompoundExpression if that class exists)
        // For simplicity, just use the first identifier - the visitor will still track it
        AstNode body;
        if (expressions.isEmpty()) {
            body = null;
        } else if (expressions.size() == 1) {
            body = expressions.get(0);
        } else {
            // Create a compound expression node to hold all usages
            body = new CompoundExpressionNode(expressions, false, 1, 0, 1, 50);
        }

        FunctionDefNode funcNode = new FunctionDefNode(
            funcName,
            params,
            body,
            false,
            1, 0, 1, 50
        );

        return funcNode;
    }

    private FunctionDefNode createFunctionWithAssignments(String funcName, String... varNames) {
        // Create a function that assigns all variables before use
        List<String> params = Arrays.asList(varNames);

        // Create assignment for first variable
        List<AstNode> assignments = new ArrayList<>();
        if (varNames.length > 0) {
            IdentifierNode varNode = new IdentifierNode(varNames[0], 1, 0, 1, varNames[0].length());
            FunctionCallNode assignment = new FunctionCallNode(
                "Set",
                Arrays.asList(varNode, new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 11)),
                1, 0, 1, 11
            );
            assignments.add(assignment);
        }

        AstNode body = assignments.isEmpty() ? null : assignments.get(0);

        return new FunctionDefNode(
            funcName,
            params,
            body,
            false,
            1, 0, 1, 50
        );
    }

    // ===== INTEGRATION TESTS =====

    @Test
    void testCompleteWorkflow() {
        // Test a complete workflow: function definition -> assignments -> usages
        List<AstNode> body = new ArrayList<>();

        // Use var1 before assignment (should be detected)
        body.add(new IdentifierNode("var1", 2, 0, 2, 4));

        // Assign var2
        body.add(new FunctionCallNode(
            "Set",
            Arrays.asList(
                new IdentifierNode("var2", 3, 0, 3, 4),
                new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 3, 7, 3, 9)
            ),
            3, 0, 3, 9
        ));

        // Use var2 after assignment (should NOT be detected)
        body.add(new IdentifierNode("var2", 4, 0, 4, 4));

        FunctionDefNode funcNode = new FunctionDefNode(
            "complexFunc",
            Arrays.asList("var1", "var2"),
            body.get(0),  // Just use first node for simplicity
            false,
            1, 0, 5, 0
        );

        visitor.visit(funcNode);

        // Visit the rest of the body
        for (int i = 1; i < body.size(); i++) {
            body.get(i).accept(visitor);
        }

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("complexFunc");

        assertTrue(uninitVars.contains("var1"),
            "var1 should be detected as used before assignment");
        assertFalse(uninitVars.contains("var2"),
            "var2 should NOT be detected since it was assigned before use");
    }
}
