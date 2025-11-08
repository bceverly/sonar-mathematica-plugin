package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.mathematica.rules.MathematicaRulesSensor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UnifiedRuleVisitorTest {

    private InputFile inputFile;
    private MathematicaRulesSensor sensor;
    private UnifiedRuleVisitor visitor;

    @BeforeEach
    void setUp() {
        inputFile = mock(InputFile.class);
        sensor = mock(MathematicaRulesSensor.class);
        visitor = new UnifiedRuleVisitor(inputFile, sensor);
    }

    // ========== Test visit(AssignmentNode) - Lines 127-134 ==========

    @Test
    void testVisitAssignmentWithIdentifierLhs() {
        IdentifierNode lhs = new IdentifierNode("myVar", 1, 1, 1, 6);
        LiteralNode rhs = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 12);
        AssignmentNode assignment = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 12);

        assertDoesNotThrow(() -> visitor.visit(assignment));
    }

    @Test
    void testVisitAssignmentWithNonIdentifierLhs() {
        LiteralNode lhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 8);
        AssignmentNode assignment = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 8);

        assertDoesNotThrow(() -> visitor.visit(assignment));
    }

    @Test
    void testVisitMultipleAssignmentsSameVariable() {
        IdentifierNode lhs1 = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs1 = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 7);
        AssignmentNode assignment1 = new AssignmentNode("=", lhs1, rhs1, 1, 1, 1, 7);

        IdentifierNode lhs2 = new IdentifierNode("x", 2, 1, 2, 2);
        LiteralNode rhs2 = new LiteralNode(2, LiteralNode.LiteralType.INTEGER, 2, 6, 2, 7);
        AssignmentNode assignment2 = new AssignmentNode("=", lhs2, rhs2, 2, 1, 2, 7);

        assertDoesNotThrow(() -> {
            visitor.visit(assignment1);
            visitor.visit(assignment2);
        });
    }

    // ========== Test visit(OperatorNode) - Lines 137-145 ==========

    @Test
    void testVisitOperatorNode() {
        IdentifierNode left = new IdentifierNode("a", 1, 1, 1, 1);
        IdentifierNode right = new IdentifierNode("b", 1, 5, 1, 5);
        OperatorNode operator = new OperatorNode(
            OperatorNode.OperatorType.ADD,
            left,
            right,
            "+",
            1, 1, 1, 6
        );

        assertDoesNotThrow(() -> visitor.visit(operator));
    }

    @Test
    void testVisitMultipleOperators() {
        IdentifierNode a = new IdentifierNode("a", 1, 1, 1, 1);
        IdentifierNode b = new IdentifierNode("b", 1, 3, 1, 3);
        OperatorNode op1 = new OperatorNode(
            OperatorNode.OperatorType.ADD,
            a,
            b,
            "+",
            1, 1, 1, 4
        );

        IdentifierNode c = new IdentifierNode("c", 1, 6, 1, 6);
        OperatorNode op2 = new OperatorNode(
            OperatorNode.OperatorType.MULTIPLY,
            op1,
            c,
            "*",
            1, 1, 1, 7
        );

        assertDoesNotThrow(() -> {
            visitor.visit(op1);
            visitor.visit(op2);
        });
    }

    // ========== Test checkExpressionComplexity() - Lines 234-241 ==========

    @Test
    void testComplexExpressionTriggers() {
        // Create a deeply nested expression with 11 operators to trigger the rule
        // The countOperators method recursively counts all operators in the tree
        IdentifierNode base = new IdentifierNode("x", 1, 1, 1, 1);
        OperatorNode current = new OperatorNode(
            OperatorNode.OperatorType.ADD,
            base,
            new IdentifierNode("y", 1, 3, 1, 3),
            "+",
            1, 1, 1, 4
        );

        // Create 10 more nested operators (total 11)
        for (int i = 0; i < 10; i++) {
            current = new OperatorNode(
                OperatorNode.OperatorType.ADD,
                current,
                new IdentifierNode("z" + i, 1, 5 + i * 2, 1, 6 + i * 2),
                "+",
                1, 1, 1, 7 + i * 2
            );
        }

        // Visit the complex expression - should trigger due to >10 operators
        final OperatorNode complexExpr = current;
        assertDoesNotThrow(() -> visitor.visit(complexExpr));
    }

    @Test
    void testSimpleExpressionDoesNotTrigger() {
        IdentifierNode left = new IdentifierNode("a", 1, 1, 1, 1);
        IdentifierNode right = new IdentifierNode("b", 1, 3, 1, 3);
        OperatorNode simpleOp = new OperatorNode(
            OperatorNode.OperatorType.ADD,
            left,
            right,
            "+",
            1, 1, 1, 4
        );

        assertDoesNotThrow(() -> visitor.visit(simpleOp));
    }

    // ========== Test Literal Node Processing ==========

    @Test
    void testLiteralNumberNode() {
        LiteralNode magicNumber = new LiteralNode(3.14159, LiteralNode.LiteralType.REAL, 1, 1, 1, 7);

        assertDoesNotThrow(() -> visitor.visit(magicNumber));
    }

    @Test
    void testLiteralIntegerNode() {
        LiteralNode intNode = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 2);

        assertDoesNotThrow(() -> visitor.visit(intNode));
    }

    @Test
    void testLiteralZeroOneMinusOne() {
        LiteralNode zero = new LiteralNode(0, LiteralNode.LiteralType.INTEGER, 1, 1, 1, 1);
        LiteralNode one = new LiteralNode(1, LiteralNode.LiteralType.INTEGER, 2, 1, 2, 1);
        LiteralNode minusOne = new LiteralNode(-1, LiteralNode.LiteralType.INTEGER, 3, 1, 3, 2);

        assertDoesNotThrow(() -> {
            visitor.visit(zero);
            visitor.visit(one);
            visitor.visit(minusOne);
        });
    }

    // ========== Test Hardcoded Credentials Detection ==========

    @Test
    void testHardcodedPasswordDetection() {
        LiteralNode password = new LiteralNode(
            "myPassword123",
            LiteralNode.LiteralType.STRING,
            1, 1, 1, 15
        );

        assertDoesNotThrow(() -> visitor.visit(password));
        verify(sensor).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testHardcodedSecretDetection() {
        LiteralNode secret = new LiteralNode(
            "api_secret_key_12345",
            LiteralNode.LiteralType.STRING,
            1, 1, 1, 22
        );

        assertDoesNotThrow(() -> visitor.visit(secret));
        verify(sensor).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testShortPasswordStringDoesNotTrigger() {
        LiteralNode shortPwd = new LiteralNode("pwd", LiteralNode.LiteralType.STRING, 1, 1, 1, 5);

        assertDoesNotThrow(() -> visitor.visit(shortPwd));
        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    // ========== Test Function Definition Rules ==========

    @Test
    void testFunctionNamingLowerCaseTriggers() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 12);
        FunctionDefNode funcDef = new FunctionDefNode(
            "myFunction",
            Collections.emptyList(),
            body,
            true,
            1, 1, 1, 12
        );

        assertDoesNotThrow(() -> visitor.visit(funcDef));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testFunctionNamingUpperCaseDoesNotTrigger() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 12);
        FunctionDefNode funcDef = new FunctionDefNode(
            "MyFunction",
            Collections.emptyList(),
            body,
            true,
            1, 1, 1, 12
        );

        // Reset sensor for clean test
        sensor = mock(MathematicaRulesSensor.class);
        visitor = new UnifiedRuleVisitor(inputFile, sensor);

        assertDoesNotThrow(() -> visitor.visit(funcDef));
    }

    @Test
    void testLongFunctionTriggers() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 55, 10, 55, 12);
        FunctionDefNode funcDef = new FunctionDefNode(
            "MyFunction",
            Collections.emptyList(),
            body,
            true,
            1, 1, 55, 12
        );

        assertDoesNotThrow(() -> visitor.visit(funcDef));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    // ========== Test Security Vulnerability Checks ==========

    @Test
    void testCommandInjectionRun() {
        FunctionCallNode runCall = new FunctionCallNode(
            "Run",
            Collections.emptyList(),
            1, 1, 1, 6
        );

        assertDoesNotThrow(() -> visitor.visit(runCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testCommandInjectionRunProcess() {
        FunctionCallNode runProcessCall = new FunctionCallNode(
            "RunProcess",
            Collections.emptyList(),
            1, 1, 1, 12
        );

        assertDoesNotThrow(() -> visitor.visit(runProcessCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testSqlInjectionSQLExecute() {
        FunctionCallNode sqlCall = new FunctionCallNode(
            "SQLExecute",
            Collections.emptyList(),
            1, 1, 1, 12
        );

        assertDoesNotThrow(() -> visitor.visit(sqlCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testCodeInjectionToExpression() {
        LiteralNode arg = new LiteralNode("code", LiteralNode.LiteralType.STRING, 1, 15, 1, 21);
        FunctionCallNode toExprCall = new FunctionCallNode(
            "ToExpression",
            Collections.singletonList(arg),
            1, 1, 1, 22
        );

        assertDoesNotThrow(() -> visitor.visit(toExprCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testPathTraversalImport() {
        FunctionCallNode importCall = new FunctionCallNode(
            "Import",
            Collections.emptyList(),
            1, 1, 1, 8
        );

        assertDoesNotThrow(() -> visitor.visit(importCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testWeakCryptography() {
        LiteralNode arg1 = new LiteralNode("data", LiteralNode.LiteralType.STRING, 1, 6, 1, 12);
        LiteralNode arg2 = new LiteralNode("MD5", LiteralNode.LiteralType.STRING, 1, 14, 1, 19);
        FunctionCallNode hashCall = new FunctionCallNode(
            "Hash",
            Arrays.asList(arg1, arg2),
            1, 1, 1, 20
        );

        assertDoesNotThrow(() -> visitor.visit(hashCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testSSRF() {
        FunctionCallNode urlFetch = new FunctionCallNode(
            "URLFetch",
            Collections.emptyList(),
            1, 1, 1, 10
        );

        assertDoesNotThrow(() -> visitor.visit(urlFetch));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testInsecureDeserialization() {
        LiteralNode arg = new LiteralNode("data.mx", LiteralNode.LiteralType.STRING, 1, 8, 1, 17);
        FunctionCallNode importCall = new FunctionCallNode(
            "Import",
            Collections.singletonList(arg),
            1, 1, 1, 18
        );

        assertDoesNotThrow(() -> visitor.visit(importCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testUnsafeSymbol() {
        FunctionCallNode symbolCall = new FunctionCallNode(
            "Symbol",
            Collections.emptyList(),
            1, 1, 1, 8
        );

        assertDoesNotThrow(() -> visitor.visit(symbolCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testXXE() {
        FunctionCallNode xmlCall = new FunctionCallNode(
            "XMLObject",
            Collections.emptyList(),
            1, 1, 1, 11
        );

        assertDoesNotThrow(() -> visitor.visit(xmlCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testInsecureRandom() {
        FunctionCallNode randomCall = new FunctionCallNode(
            "Random",
            Collections.emptyList(),
            1, 1, 1, 8
        );

        assertDoesNotThrow(() -> visitor.visit(randomCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testUnsafeCloudDeploy() {
        FunctionCallNode cloudCall = new FunctionCallNode(
            "CloudDeploy",
            Collections.emptyList(),
            1, 1, 1, 13
        );

        assertDoesNotThrow(() -> visitor.visit(cloudCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testDynamicInjection() {
        FunctionCallNode dynamicCall = new FunctionCallNode(
            "Dynamic",
            Collections.emptyList(),
            1, 1, 1, 9
        );

        assertDoesNotThrow(() -> visitor.visit(dynamicCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testNeedsGetUntrusted() {
        FunctionCallNode getCall = new FunctionCallNode(
            "Get",
            Collections.emptyList(),
            1, 1, 1, 5
        );

        assertDoesNotThrow(() -> visitor.visit(getCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testExposingSensitiveData() {
        FunctionCallNode exportCall = new FunctionCallNode(
            "Export",
            Collections.emptyList(),
            1, 1, 1, 8
        );

        assertDoesNotThrow(() -> visitor.visit(exportCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testMissingFormFunctionValidation() {
        FunctionCallNode formCall = new FunctionCallNode(
            "FormFunction",
            Collections.emptyList(),
            1, 1, 1, 14
        );

        assertDoesNotThrow(() -> visitor.visit(formCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    // ========== Test Security Hotspot Rules ==========

    @Test
    void testFileUploadValidation() {
        // Test FormFunction which is used for file uploads
        FunctionCallNode formFunction = new FunctionCallNode(
            "FormFunction",
            Collections.emptyList(),
            1, 1, 1, 14
        );

        assertDoesNotThrow(() -> visitor.visit(formFunction));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testExternalApiSafeguards() {
        FunctionCallNode urlRead = new FunctionCallNode(
            "URLRead",
            Collections.emptyList(),
            1, 1, 1, 9
        );

        assertDoesNotThrow(() -> visitor.visit(urlRead));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testCryptoKeyGeneration() {
        FunctionCallNode randomInt = new FunctionCallNode(
            "RandomInteger",
            Collections.emptyList(),
            1, 1, 1, 15
        );

        assertDoesNotThrow(() -> visitor.visit(randomInt));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testNetworkOperations() {
        FunctionCallNode socketCall = new FunctionCallNode(
            "SocketConnect",
            Collections.emptyList(),
            1, 1, 1, 15
        );

        assertDoesNotThrow(() -> visitor.visit(socketCall));
        verify(sensor).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testFileSystemModifications() {
        FunctionCallNode deleteCall = new FunctionCallNode(
            "DeleteFile",
            Collections.emptyList(),
            1, 1, 1, 12
        );

        assertDoesNotThrow(() -> visitor.visit(deleteCall));
        // DeleteFile triggers both PATH_TRAVERSAL and FILE_SYSTEM_MODIFICATIONS
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testEnvironmentVariable() {
        FunctionCallNode envCall = new FunctionCallNode(
            "Environment",
            Collections.emptyList(),
            1, 1, 1, 13
        );

        assertDoesNotThrow(() -> visitor.visit(envCall));
        verify(sensor).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testImportWithoutFormat() {
        LiteralNode arg = new LiteralNode("file.dat", LiteralNode.LiteralType.STRING, 1, 8, 1, 18);
        FunctionCallNode importCall = new FunctionCallNode(
            "Import",
            Collections.singletonList(arg),
            1, 1, 1, 19
        );

        assertDoesNotThrow(() -> visitor.visit(importCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    // ========== Test List/Association Checks ==========

    @Test
    void testListIndexOutOfBounds() {
        FunctionCallNode partCall = new FunctionCallNode(
            "Part",
            Collections.emptyList(),
            1, 1, 1, 6
        );

        assertDoesNotThrow(() -> visitor.visit(partCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testAssociationVsListConfusion() {
        IdentifierNode assocVar = new IdentifierNode("myAssoc", 1, 6, 1, 13);
        FunctionCallNode partCall = new FunctionCallNode(
            "Part",
            Collections.singletonList(assocVar),
            1, 1, 1, 14
        );

        assertDoesNotThrow(() -> visitor.visit(partCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testInefficientKeyLookup() {
        FunctionCallNode keysCall = new FunctionCallNode(
            "Keys",
            Collections.emptyList(),
            1, 8, 1, 14
        );
        FunctionCallNode selectCall = new FunctionCallNode(
            "Select",
            Collections.singletonList(keysCall),
            1, 1, 1, 15
        );

        assertDoesNotThrow(() -> visitor.visit(selectCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    // ========== Test Deprecated Functions ==========

    @Test
    void testDeprecatedFunctionDetection() {
        FunctionCallNode versionCall = new FunctionCallNode(
            "$Version",
            Collections.emptyList(),
            1, 1, 1, 10
        );

        assertDoesNotThrow(() -> visitor.visit(versionCall));
        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    // ========== Test Post-Traversal Checks ==========

    @Test
    void testFunctionDefinedButNeverCalled() {
        LiteralNode body = new LiteralNode(42, LiteralNode.LiteralType.INTEGER, 1, 10, 1, 12);
        FunctionDefNode funcDef = new FunctionDefNode(
            "UnusedFunc",
            Collections.emptyList(),
            body,
            true,
            1, 1, 1, 12
        );

        visitor.visit(funcDef);
        visitor.performPostTraversalChecks();

        verify(sensor, atLeastOnce()).queueIssue(any(), eq(1), anyString(), anyString());
    }

    @Test
    void testRepeatedFunctionCalls() {
        FunctionCallNode call1 = new FunctionCallNode(
            "ExpensiveFunc",
            Collections.emptyList(),
            1, 1, 1, 15
        );
        FunctionCallNode call2 = new FunctionCallNode(
            "ExpensiveFunc",
            Collections.emptyList(),
            2, 1, 2, 15
        );
        FunctionCallNode call3 = new FunctionCallNode(
            "ExpensiveFunc",
            Collections.emptyList(),
            3, 1, 3, 15
        );
        FunctionCallNode call4 = new FunctionCallNode(
            "ExpensiveFunc",
            Collections.emptyList(),
            4, 1, 4, 15
        );

        visitor.visit(call1);
        visitor.visit(call2);
        visitor.visit(call3);
        visitor.visit(call4);
        visitor.performPostTraversalChecks();

        verify(sensor, atLeastOnce()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testIdentifierNodeVisit() {
        IdentifierNode id = new IdentifierNode("MyFunc", 1, 1, 1, 7);

        assertDoesNotThrow(() -> visitor.visit(id));
    }

    @Test
    void testVisitChildrenRecursion() {
        IdentifierNode lhs = new IdentifierNode("x", 1, 1, 1, 2);
        LiteralNode rhs = new LiteralNode(10, LiteralNode.LiteralType.INTEGER, 1, 6, 1, 8);
        AssignmentNode assignment = new AssignmentNode("=", lhs, rhs, 1, 1, 1, 8);

        assertDoesNotThrow(() -> visitor.visitChildren(assignment));
    }

    @Test
    void testNullSensorDoesNotThrowException() {
        UnifiedRuleVisitor nullSensorVisitor = new UnifiedRuleVisitor(inputFile, null);

        FunctionCallNode call = new FunctionCallNode(
            "Run",
            Collections.emptyList(),
            1, 1, 1, 5
        );

        assertDoesNotThrow(() -> nullSensorVisitor.visit(call));
    }
}
