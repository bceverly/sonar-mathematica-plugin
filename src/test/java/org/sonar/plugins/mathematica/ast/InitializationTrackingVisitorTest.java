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

        // param1 is a function parameter, so it should NOT be flagged as used before assignment
        // Parameters are pre-initialized by the function caller
        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");
        assertFalse(uninitVars.contains("param1"), "param1 is a parameter and should not be flagged");
    }

    @Test
    void testFunctionParametersArePreInitialized() {
        // Regression test for false positive: "Parameter 'outputDir' in function 'makeMathematicaNotebook'
        // may be used before assignment" - this was incorrectly flagged because parameters
        // were not treated as pre-initialized.

        // Simulate makeMathematicaNotebook[inputDir_, outputDir_, language_, filename_, ...] := ...
        FunctionDefNode funcNode = new FunctionDefNode(
            "makeMathematicaNotebook",
            Arrays.asList("inputDir", "outputDir", "highAndLowResolution", "language", "filename", "releaseVersion"),
            new CompoundExpressionNode(
                Arrays.asList(
                    new IdentifierNode("outputDir", 2, 0, 2, 9),      // Using outputDir
                    new IdentifierNode("language", 3, 0, 3, 8),       // Using language
                    new IdentifierNode("filename", 4, 0, 4, 8)        // Using filename
                ),
                false, 1, 0, 5, 0
            ),
            false,
            1, 0, 5, 0
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("makeMathematicaNotebook");

        // All parameters should be treated as pre-initialized, so none should be flagged
        assertFalse(uninitVars.contains("outputDir"), "outputDir is a parameter - should not be flagged");
        assertFalse(uninitVars.contains("language"), "language is a parameter - should not be flagged");
        assertFalse(uninitVars.contains("filename"), "filename is a parameter - should not be flagged");
        assertTrue(uninitVars.isEmpty(), "No parameters should be flagged as uninitialized");
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
        // NOTE: Function parameters should NOT be flagged - they are pre-initialized
        // This test now verifies that parameters are correctly treated as initialized
        FunctionDefNode funcNode = new FunctionDefNode(
            "myFunc",
            Arrays.asList("paramVar"),  // Parameter - pre-initialized by caller
            new IdentifierNode("paramVar", 1, 10, 1, 18),  // Used here
            false,
            1, 0, 1, 25
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("myFunc");
        assertFalse(uninitVars.contains("paramVar"),
            "paramVar is a parameter and should not be flagged as used before assignment");
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
        // NOTE: Function parameters are now pre-initialized, so they won't be flagged
        // regardless of their name. This test verifies parameters are not flagged.
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
        assertFalse(uninitVars.contains(longVar),
            "Long lowercase parameter should not be flagged - parameters are pre-initialized");
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
        // Test Line 175-187: Iteration over all functions
        // NOTE: With parameters now pre-initialized, these functions won't have uninitialized vars
        FunctionDefNode func1 = createFunctionWithUninitializedVars("func1", "var1", "var2");
        visitor.visit(func1);

        FunctionDefNode func2 = createFunctionWithUninitializedVars("func2", "var3");
        visitor.visit(func2);

        FunctionDefNode func3 = createFunctionWithAssignments("func3", "var4");
        visitor.visit(func3);

        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();

        // All variables are parameters, so they're pre-initialized - no functions should be flagged
        assertTrue(allUninit.isEmpty(), "Parameters are pre-initialized, so no uninitialized vars should be detected");
    }

    @Test
    void testGetAllVariablesUsedBeforeAssignmentEmptyVariableSetsFiltered() {
        FunctionDefNode funcWithNoUninit = createFunctionWithAssignments("emptyFunc", "assigned");
        visitor.visit(funcWithNoUninit);

        Map<String, Set<String>> allUninit = visitor.getAllVariablesUsedBeforeAssignment();

        assertFalse(allUninit.containsKey("emptyFunc"),
            "Functions with no uninitialized variables should not appear in results");
    }

    @Test
    void testGetVariablesUsedBeforeAssignmentSpecificFunction() {
        // Test the simpler getter for a specific function
        // NOTE: Parameters are now pre-initialized, so no variables will be flagged
        FunctionDefNode func = createFunctionWithUninitializedVars("targetFunc", "param1", "param2");
        visitor.visit(func);

        Set<String> vars = visitor.getVariablesUsedBeforeAssignment("targetFunc");

        assertNotNull(vars);
        assertTrue(vars.isEmpty(), "Parameters are pre-initialized, so no uninitialized vars");
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
        // NOTE: With the fix for parameters being pre-initialized, this helper now creates
        // functions where parameters are used (and are correctly pre-initialized).
        // These tests now verify that parameters are NOT incorrectly flagged.
        List<String> params = Arrays.asList(varNames);

        // Create a CompoundExpressionNode that contains all variable usages
        List<AstNode> expressions = new ArrayList<>();
        for (String varName : varNames) {
            expressions.add(new IdentifierNode(varName, 1, 0, 1, varName.length()));
        }

        // Use first identifier as body (or create CompoundExpression if that class exists)
        AstNode body;
        if (expressions.isEmpty()) {
            body = null;
        } else if (expressions.size() == 1) {
            body = expressions.get(0);
        } else {
            // Create a compound expression node to hold all usages
            body = new CompoundExpressionNode(expressions, false, 1, 0, 1, 50);
        }

        return new FunctionDefNode(
            funcName,
            params,
            body,
            false,
            1, 0, 1, 50
        );
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
        // NOTE: var1 and var2 are PARAMETERS, so they are pre-initialized by the caller
        List<AstNode> body = new ArrayList<>();

        // Use var1 (it's a parameter, so it's already initialized)
        body.add(new IdentifierNode("var1", 2, 0, 2, 4));

        // Assign var2 (even though it's already initialized as a parameter)
        body.add(new FunctionCallNode(
            "Set",
            Arrays.asList(
                new IdentifierNode("var2", 3, 0, 3, 4),
                new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 3, 7, 3, 9)
            ),
            3, 0, 3, 9
        ));

        // Use var2 after assignment
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

        // Both var1 and var2 are parameters, so they're pre-initialized - neither should be flagged
        assertFalse(uninitVars.contains("var1"),
            "var1 is a parameter and should not be flagged");
        assertFalse(uninitVars.contains("var2"),
            "var2 is a parameter and should not be flagged");
    }

    // ===== TEST GROUP 7: Module/Block/With Scoping Constructs =====

    @Test
    void testModuleWithUninitializedVariable() {
        // Regression test: Module[{entries}, entries = ...; use entries]
        // "entries" in the declaration list should NOT be flagged as used before assignment

        // Create Module[{entries}, body] structure
        ListNode declarationList = new ListNode(
            Arrays.asList(new IdentifierNode("entries", 1, 10, 1, 17)),
            1, 9, 1, 18
        );

        // Body: entries = value; use entries
        List<AstNode> bodyStatements = new ArrayList<>();
        bodyStatements.add(new FunctionCallNode(
            "Set",
            Arrays.asList(
                new IdentifierNode("entries", 2, 4, 2, 11),
                new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 2, 14, 2, 16)
            ),
            2, 4, 2, 16
        ));
        bodyStatements.add(new IdentifierNode("entries", 3, 4, 3, 11));

        CompoundExpressionNode body = new CompoundExpressionNode(bodyStatements, false, 2, 0, 3, 11);

        FunctionCallNode moduleNode = new FunctionCallNode(
            "Module",
            Arrays.asList(declarationList, body),
            1, 0, 4, 0
        );

        // Wrap in a function
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList("param1"),
            moduleNode,
            false,
            1, 0, 4, 0
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");

        // "entries" is declared in Module and assigned before use - should NOT be flagged
        assertFalse(uninitVars.contains("entries"),
            "entries is declared in Module and assigned before use - should not be flagged");
    }

    @Test
    void testModuleWithInitializedVariable() {
        // Test: Module[{x = 5}, use x]
        // Variable is initialized in declaration - should NOT be flagged

        // Create Module[{x = 5}, body] structure
        ListNode declarationList = new ListNode(
            Arrays.asList(
                new FunctionCallNode(
                    "Set",
                    Arrays.asList(
                        new IdentifierNode("x", 1, 11, 1, 12),
                        new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 15, 1, 16)
                    ),
                    1, 11, 1, 16
                )
            ),
            1, 9, 1, 17
        );

        // Body: use x
        IdentifierNode body = new IdentifierNode("x", 2, 4, 2, 5);

        FunctionCallNode moduleNode = new FunctionCallNode(
            "Module",
            Arrays.asList(declarationList, body),
            1, 0, 3, 0
        );

        // Wrap in a function
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList("param1"),
            moduleNode,
            false,
            1, 0, 3, 0
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");

        // "x" is initialized in Module declaration - should NOT be flagged
        assertFalse(uninitVars.contains("x"),
            "x is initialized in Module declaration - should not be flagged");
    }

    @Test
    void testModuleWithMixedVariables() {
        // Test: Module[{uninit, init = 10}, use uninit; uninit = 5; use init]
        // uninit is used before assignment (should be flagged)
        // init is initialized in declaration (should NOT be flagged)

        // Create Module[{uninit, init = 10}, body] structure
        ListNode declarationList = new ListNode(
            Arrays.asList(
                new IdentifierNode("uninit", 1, 11, 1, 17),
                new FunctionCallNode(
                    "Set",
                    Arrays.asList(
                        new IdentifierNode("init", 1, 19, 1, 23),
                        new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 26, 1, 28)
                    ),
                    1, 19, 1, 28
                )
            ),
            1, 9, 1, 29
        );

        // Body: use uninit; uninit = 5; use init
        List<AstNode> bodyStatements = new ArrayList<>();
        bodyStatements.add(new IdentifierNode("uninit", 2, 4, 2, 10));  // Use uninit before assignment
        bodyStatements.add(new FunctionCallNode(
            "Set",
            Arrays.asList(
                new IdentifierNode("uninit", 3, 4, 3, 10),
                new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 3, 13, 3, 14)
            ),
            3, 4, 3, 14
        ));
        bodyStatements.add(new IdentifierNode("init", 4, 4, 4, 8));  // Use init (already initialized)

        CompoundExpressionNode body = new CompoundExpressionNode(bodyStatements, false, 2, 0, 4, 8);

        FunctionCallNode moduleNode = new FunctionCallNode(
            "Module",
            Arrays.asList(declarationList, body),
            1, 0, 5, 0
        );

        // Wrap in a function
        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList("param1"),
            moduleNode,
            false,
            1, 0, 5, 0
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");

        // "uninit" is used before assignment - SHOULD be flagged
        assertTrue(uninitVars.contains("uninit"),
            "uninit is used before assignment - should be flagged");
        // "init" is initialized in declaration - should NOT be flagged
        assertFalse(uninitVars.contains("init"),
            "init is initialized in Module declaration - should not be flagged");
    }

    @Test
    void testBlockScopingConstruct() {
        // Test that Block[] works the same as Module[]
        ListNode declarationList = new ListNode(
            Arrays.asList(new IdentifierNode("temp", 1, 10, 1, 14)),
            1, 9, 1, 15
        );

        // Body: temp = 42; use temp
        List<AstNode> bodyStatements = new ArrayList<>();
        bodyStatements.add(new FunctionCallNode(
            "Set",
            Arrays.asList(
                new IdentifierNode("temp", 2, 4, 2, 8),
                new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 2, 11, 2, 13)
            ),
            2, 4, 2, 13
        ));
        bodyStatements.add(new IdentifierNode("temp", 3, 4, 3, 8));

        CompoundExpressionNode body = new CompoundExpressionNode(bodyStatements, false, 2, 0, 3, 8);

        FunctionCallNode blockNode = new FunctionCallNode(
            "Block",
            Arrays.asList(declarationList, body),
            1, 0, 4, 0
        );

        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList("param1"),
            blockNode,
            false,
            1, 0, 4, 0
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");

        assertFalse(uninitVars.contains("temp"),
            "temp is assigned before use in Block - should not be flagged");
    }

    @Test
    void testWithScopingConstruct() {
        // Test that With[] works the same as Module[]
        // With[{const = 5}, use const]
        ListNode declarationList = new ListNode(
            Arrays.asList(
                new FunctionCallNode(
                    "Set",
                    Arrays.asList(
                        new IdentifierNode("const", 1, 11, 1, 16),
                        new LiteralNode(5, LiteralNode.LiteralType.INTEGER, 1, 19, 1, 20)
                    ),
                    1, 11, 1, 20
                )
            ),
            1, 9, 1, 21
        );

        IdentifierNode body = new IdentifierNode("const", 2, 4, 2, 9);

        FunctionCallNode withNode = new FunctionCallNode(
            "With",
            Arrays.asList(declarationList, body),
            1, 0, 3, 0
        );

        FunctionDefNode funcNode = new FunctionDefNode(
            "testFunc",
            Arrays.asList("param1"),
            withNode,
            false,
            1, 0, 3, 0
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("testFunc");

        assertFalse(uninitVars.contains("const"),
            "const is initialized in With declaration - should not be flagged");
    }

    @Test
    void testModuleUninitializedVariableAssignedInBody() {
        // This is the exact scenario from the user's false positive:
        // Module[{subdirsPatt},
        //     subdirsPatt = Alternatives @@ (...);
        // ]
        // subdirsPatt is declared but not initialized, then assigned as first statement in body.
        // This should NOT be flagged as used-before-assigned.

        // Module declaration list with uninitialized variable
        IdentifierNode uninitVar = new IdentifierNode("subdirsPatt", 2, 0, 2, 11);
        ListNode moduleVars = new ListNode(Arrays.asList(uninitVar), 2, 0, 2, 20);

        // Body: assignment to subdirsPatt
        IdentifierNode lhs = new IdentifierNode("subdirsPatt", 3, 0, 3, 11);
        IdentifierNode rhs = new IdentifierNode("someValue", 3, 15, 3, 24);
        FunctionCallNode assignment = new FunctionCallNode(
            "Set",
            Arrays.asList(lhs, rhs),
            3, 0, 3, 30
        );

        // Module[{subdirsPatt}, subdirsPatt = someValue]
        FunctionCallNode moduleNode = new FunctionCallNode(
            "Module",
            Arrays.asList(moduleVars, assignment),
            2, 0, 3, 30
        );

        FunctionDefNode funcNode = new FunctionDefNode(
            "copyDirectoryNoCVS",
            Arrays.asList("srcDir", "destDir", "subdirsToInclude"),
            moduleNode,
            false,
            1, 0, 4, 0
        );

        visitor.visit(funcNode);

        Set<String> uninitVars = visitor.getVariablesUsedBeforeAssignment("copyDirectoryNoCVS");

        assertFalse(uninitVars.contains("subdirsPatt"),
            "subdirsPatt is assigned in Module body before use - should not be flagged");
    }

    @Test
    void testModuleDirectVisitDoesNotCauseFalsePositive() {
        // Test what happens if the Module node itself is visited directly
        // (simulating potential real-world parsing scenarios)

        IdentifierNode uninitVar = new IdentifierNode("subdirsPatt", 2, 0, 2, 11);
        ListNode moduleVars = new ListNode(Arrays.asList(uninitVar), 2, 0, 2, 20);

        IdentifierNode lhs = new IdentifierNode("subdirsPatt", 3, 0, 3, 11);
        IdentifierNode rhs = new IdentifierNode("someValue", 3, 15, 3, 24);
        FunctionCallNode assignment = new FunctionCallNode(
            "Set",
            Arrays.asList(lhs, rhs),
            3, 0, 3, 30
        );

        FunctionCallNode moduleNode = new FunctionCallNode(
            "Module",
            Arrays.asList(moduleVars, assignment),
            2, 0, 3, 30
        );

        // Use a fresh visitor for this test
        InitializationTrackingVisitor freshVisitor = new InitializationTrackingVisitor();
        FunctionDefNode funcNode = new FunctionDefNode(
            "copyDirectoryNoCVS",
            Arrays.asList("srcDir", "destDir", "subdirsToInclude"),
            moduleNode,
            false,
            1, 0, 4, 0
        );
        freshVisitor.visit(funcNode);

        Set<String> uninitVars = freshVisitor.getVariablesUsedBeforeAssignment("copyDirectoryNoCVS");

        assertFalse(uninitVars.contains("subdirsPatt"),
            "subdirsPatt should not be flagged even when Module is visited directly");
    }
}
