package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.mathematica.symboltable.Scope;
import org.sonar.plugins.mathematica.symboltable.ScopeType;
import org.sonar.plugins.mathematica.symboltable.Symbol;
import org.sonar.plugins.mathematica.symboltable.SymbolReference;
import org.sonar.plugins.mathematica.symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class SymbolTableDetectorTest {

    private SensorContext context;
    private InputFile inputFile;
    private SymbolTable symbolTable;

    @BeforeEach
    void setUp() {
        context = mock(SensorContext.class, RETURNS_DEEP_STUBS);
        inputFile = mock(InputFile.class);
        symbolTable = mock(SymbolTable.class);

        when(inputFile.filename()).thenReturn("test.m");
        when(inputFile.lines()).thenReturn(100);
        when(inputFile.selectLine(anyInt())).thenReturn(mock(org.sonar.api.batch.fs.TextRange.class));
    }

    // ===== BASIC VARIABLE USAGE TESTS =====

    @Test
    void testDetectUnusedVariable() {
        Symbol unusedSymbol = createMockSymbol("unused", 10, false, false);
        when(symbolTable.getUnusedSymbols()).thenReturn(Collections.singletonList(unusedSymbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUnusedVariable(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectUnusedVariableWithParameter() {
        Symbol paramSymbol = createMockSymbol("param", 5, true, false);
        when(symbolTable.getUnusedSymbols()).thenReturn(Collections.singletonList(paramSymbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUnusedVariable(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectAssignedButNeverRead() {
        Symbol symbol = createMockSymbol("assigned", 15, false, false);
        when(symbolTable.getAssignedButNeverReadSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectAssignedButNeverRead(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectDeadStore() {
        Symbol symbol = createMockSymbol("x", 10, false, false);
        SymbolReference assign1 = createMockReference(10, "x = 5");
        SymbolReference assign2 = createMockReference(15, "x = 10");

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbol.getReferences()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectDeadStore(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectUsedBeforeAssignment() {
        Symbol symbol = createMockSymbol("y", 5, false, false);
        SymbolReference use = createMockReference(5, "y + 1");
        SymbolReference assign = createMockReference(10, "y = 5");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbol.getReferences()).thenReturn(Collections.singletonList(use));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUsedBeforeAssignment(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectVariableShadowing() {
        Symbol outer = createMockSymbol("x", 5, false, false);
        Symbol inner = createMockSymbol("x", 15, false, false);

        SymbolTable.ShadowingPair pair = mock(SymbolTable.ShadowingPair.class);
        when(pair.getInner()).thenReturn(inner);
        when(pair.getOuter()).thenReturn(outer);

        when(symbolTable.findShadowingIssues()).thenReturn(Collections.singletonList(pair));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableShadowing(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectUnusedParameter() {
        Symbol param = createMockSymbol("param", 3, true, false);
        when(param.isUnused()).thenReturn(true);
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(param));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUnusedParameter(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectWriteOnlyVariable() {
        Symbol symbol = createMockSymbol("writeOnly", 10, false, false);
        SymbolReference assign = createMockReference(10, "writeOnly = 5");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbol.getReferences()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectWriteOnlyVariable(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectRedundantAssignment() {
        Symbol symbol = createMockSymbol("redundant", 10, false, false);
        SymbolReference assign1 = createMockReference(10, "x=5");
        SymbolReference assign2 = createMockReference(15, "x=5");

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectRedundantAssignment(context, inputFile, symbolTable)
        );
    }

    // ===== SCOPE ANALYSIS TESTS =====

    @Test
    void testDetectVariableInWrongScope() {
        Scope parentScope = createMockScope(1, 50, ScopeType.MODULE);
        Scope childScope = createMockScope(10, 20, ScopeType.BLOCK);
        when(parentScope.getChildren()).thenReturn(Collections.singletonList(childScope));

        Symbol symbol = createMockSymbolWithScope("local", 5, false, true, parentScope);
        SymbolReference ref = createMockReference(15, "local + 1");

        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableInWrongScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectVariableEscapesScope() {
        Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
        Scope functionScope = createMockScope(20, 30, ScopeType.FUNCTION);
        when(moduleScope.getChildren()).thenReturn(Collections.singletonList(functionScope));

        Symbol symbol = createMockSymbolWithScope("captured", 5, false, true, moduleScope);
        SymbolReference ref = createMockReference(25, "captured + 1");

        when(symbol.getReferences()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableEscapesScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectLifetimeExtendsBeyondScope() {
        Scope scope = createMockScope(1, 100, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope("narrow", 10, false, true, scope);

        SymbolReference ref1 = createMockReference(50, "narrow = 5");
        SymbolReference ref2 = createMockReference(55, "narrow + 1");

        when(symbol.getAllReferencesSorted()).thenReturn(List.of(ref1, ref2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectLifetimeExtendsBeyondScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectModifiedInUnexpectedScope() {
        Scope scope = createMockScope(1, 100, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope("modified", 10, false, false, scope);

        SymbolReference write = createMockReference(20, "modified = 5");
        SymbolReference read = createMockReference(50, "modified + 1");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(write));
        when(symbol.getReferences()).thenReturn(Collections.singletonList(read));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectModifiedInUnexpectedScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectGlobalVariablePollution() {
        Scope globalScope = createMockScope(1, 1000, ScopeType.GLOBAL);
        List<Symbol> globalVars = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            Symbol symbol = createMockSymbol("global" + i, i + 1, false, false);
            globalVars.add(symbol);
        }

        when(symbolTable.getGlobalScope()).thenReturn(globalScope);
        when(globalScope.getSymbols()).thenReturn(globalVars);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectGlobalVariablePollution(context, inputFile, symbolTable)
        );
    }

    // ===== ADVANCED ANALYSIS TESTS =====

    @Test
    void testDetectCircularVariableDependencies() {
        Symbol symbolA = createMockSymbol("a", 10, false, false);
        Symbol symbolB = createMockSymbol("b", 20, false, false);

        SymbolReference assignA = createMockReference(10, "a = b + 1");
        SymbolReference assignB = createMockReference(20, "b = a + 1");

        when(symbolA.getAssignments()).thenReturn(Collections.singletonList(assignA));
        when(symbolB.getAssignments()).thenReturn(Collections.singletonList(assignB));

        when(symbolTable.getAllSymbols()).thenReturn(List.of(symbolA, symbolB));
        when(symbolTable.getSymbolByName("a")).thenReturn(symbolA);
        when(symbolTable.getSymbolByName("b")).thenReturn(symbolB);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectNamingConventionViolations() {
        Symbol singleChar = createMockSymbol("x", 10, false, false);
        Symbol numbered = createMockSymbol("Var123", 20, false, false);
        Symbol allCaps = createMockSymbol("CONSTANT", 30, false, false);

        when(symbolTable.getAllSymbols()).thenReturn(List.of(singleChar, numbered, allCaps));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectNamingConventionViolations(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectConstantNotMarkedAsConstant() {
        Symbol constant = createMockSymbol("PI", 10, false, true);
        SymbolReference assign = createMockReference(10, "PI = 3.14159");
        SymbolReference ref1 = createMockReference(20, "PI * 2");
        SymbolReference ref2 = createMockReference(30, "PI * r");
        SymbolReference ref3 = createMockReference(40, "PI / 2");

        when(constant.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(constant.getReferences()).thenReturn(List.of(ref1, ref2, ref3));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(constant));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectConstantNotMarkedAsConstant(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectTypeInconsistency() {
        Symbol symbol = createMockSymbol("mixed", 10, false, false);
        SymbolReference stringRef = createMockReference(15, "mixed + \"text\"");
        SymbolReference numberRef = createMockReference(20, "mixed + 5");
        SymbolReference listRef = createMockReference(25, "mixed[[1]]");

        when(symbol.getAllReferencesSorted()).thenReturn(List.of(stringRef, numberRef, listRef));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectVariableReuseWithDifferentSemantics() {
        Symbol symbol = createMockSymbol("temp", 10, false, false);
        SymbolReference assign1 = createMockReference(10, "temp = list");
        SymbolReference assign2 = createMockReference(50, "temp = counter");

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableReuseWithDifferentSemantics(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectIncorrectClosureCapture() {
        Scope loopScope = createMockScope(1, 50, ScopeType.MODULE);
        when(loopScope.getName()).thenReturn("Do");

        Scope functionScope = createMockScope(20, 30, ScopeType.FUNCTION);
        when(loopScope.getChildren()).thenReturn(Collections.singletonList(functionScope));

        Symbol loopVar = createMockSymbolWithScope("i", 5, false, true, loopScope);
        SymbolReference ref = createMockReference(25, "i + 1");

        when(loopVar.getReferences()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(loopVar));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectIncorrectClosureCapture(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectScopeLeakThroughDynamicEvaluation() {
        Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope("dynamic", 10, false, true, moduleScope);

        SymbolReference ref = createMockReference(20, "ToExpression[\"dynamic\"]");

        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectScopeLeakThroughDynamicEvaluation(context, inputFile, symbolTable)
        );
    }

    // ===== EDGE CASES AND COMPREHENSIVE TESTS =====

    @Test
    void testAllMethodsWithEmptySymbolTable() {
        when(symbolTable.getUnusedSymbols()).thenReturn(Collections.emptyList());
        when(symbolTable.getAssignedButNeverReadSymbols()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.emptyList());
        when(symbolTable.findShadowingIssues()).thenReturn(Collections.emptyList());

        Scope emptyGlobalScope = createMockScope(1, 100, ScopeType.GLOBAL);
        when(emptyGlobalScope.getSymbols()).thenReturn(Collections.emptyList());
        when(symbolTable.getGlobalScope()).thenReturn(emptyGlobalScope);

        assertDoesNotThrow(() -> {
            SymbolTableDetector.detectUnusedVariable(context, inputFile, symbolTable);
            SymbolTableDetector.detectAssignedButNeverRead(context, inputFile, symbolTable);
            SymbolTableDetector.detectDeadStore(context, inputFile, symbolTable);
            SymbolTableDetector.detectUsedBeforeAssignment(context, inputFile, symbolTable);
            SymbolTableDetector.detectVariableShadowing(context, inputFile, symbolTable);
            SymbolTableDetector.detectUnusedParameter(context, inputFile, symbolTable);
            SymbolTableDetector.detectWriteOnlyVariable(context, inputFile, symbolTable);
            SymbolTableDetector.detectRedundantAssignment(context, inputFile, symbolTable);
            SymbolTableDetector.detectVariableInWrongScope(context, inputFile, symbolTable);
            SymbolTableDetector.detectVariableEscapesScope(context, inputFile, symbolTable);
            SymbolTableDetector.detectLifetimeExtendsBeyondScope(context, inputFile, symbolTable);
            SymbolTableDetector.detectModifiedInUnexpectedScope(context, inputFile, symbolTable);
            SymbolTableDetector.detectGlobalVariablePollution(context, inputFile, symbolTable);
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable);
            SymbolTableDetector.detectNamingConventionViolations(context, inputFile, symbolTable);
            SymbolTableDetector.detectConstantNotMarkedAsConstant(context, inputFile, symbolTable);
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable);
            SymbolTableDetector.detectVariableReuseWithDifferentSemantics(context, inputFile, symbolTable);
            SymbolTableDetector.detectIncorrectClosureCapture(context, inputFile, symbolTable);
            SymbolTableDetector.detectScopeLeakThroughDynamicEvaluation(context, inputFile, symbolTable);
        });
    }

    @Test
    void testParameterExclusions() {
        Symbol param = createMockSymbol("param", 5, true, false);
        when(param.isUnused()).thenReturn(false);

        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(param));
        when(symbolTable.getUnusedSymbols()).thenReturn(Collections.singletonList(param));

        assertDoesNotThrow(() -> {
            SymbolTableDetector.detectUnusedVariable(context, inputFile, symbolTable);
            SymbolTableDetector.detectUsedBeforeAssignment(context, inputFile, symbolTable);
            SymbolTableDetector.detectWriteOnlyVariable(context, inputFile, symbolTable);
        });
    }

    @Test
    void testComplexScenarioWithMultipleIssues() {
        // Create symbols with various issues
        Symbol unused = createMockSymbol("unused", 10, false, false);
        Symbol deadStore = createMockSymbol("deadStore", 20, false, false);
        Symbol shadowOuter = createMockSymbol("x", 5, false, false);
        Symbol shadowInner = createMockSymbol("x", 15, false, false);

        SymbolReference deadAssign1 = createMockReference(20, "deadStore = 1");
        SymbolReference deadAssign2 = createMockReference(25, "deadStore = 2");

        when(deadStore.getAssignments()).thenReturn(List.of(deadAssign1, deadAssign2));
        when(deadStore.getReferences()).thenReturn(Collections.emptyList());

        SymbolTable.ShadowingPair pair = mock(SymbolTable.ShadowingPair.class);
        when(pair.getInner()).thenReturn(shadowInner);
        when(pair.getOuter()).thenReturn(shadowOuter);

        when(symbolTable.getUnusedSymbols()).thenReturn(Collections.singletonList(unused));
        when(symbolTable.getAllSymbols()).thenReturn(List.of(unused, deadStore, shadowOuter, shadowInner));
        when(symbolTable.findShadowingIssues()).thenReturn(Collections.singletonList(pair));

        assertDoesNotThrow(() -> {
            SymbolTableDetector.detectUnusedVariable(context, inputFile, symbolTable);
            SymbolTableDetector.detectDeadStore(context, inputFile, symbolTable);
            SymbolTableDetector.detectVariableShadowing(context, inputFile, symbolTable);
        });
    }

    @Test
    void testLargeSymbolTableForCircularDeps() {
        List<Symbol> symbols = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            symbols.add(createMockSymbol("var" + i, i + 1, false, false));
        }
        when(symbolTable.getAllSymbols()).thenReturn(symbols);

        // Should skip analysis due to size
        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    // Helper methods

    private Symbol createMockSymbol(String name, int line, boolean isParameter, boolean isModuleVariable) {
        Symbol symbol = mock(Symbol.class);
        when(symbol.getName()).thenReturn(name);
        when(symbol.getDeclarationLine()).thenReturn(line);
        when(symbol.isParameter()).thenReturn(isParameter);
        when(symbol.isModuleVariable()).thenReturn(isModuleVariable);
        when(symbol.getAssignments()).thenReturn(Collections.emptyList());
        when(symbol.getReferences()).thenReturn(Collections.emptyList());
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.emptyList());

        Scope scope = createMockScope(1, 100, ScopeType.MODULE);
        when(symbol.getScope()).thenReturn(scope);

        return symbol;
    }

    private Symbol createMockSymbolWithScope(String name, int line, boolean isParameter,
                                            boolean isModuleVariable, Scope scope) {
        Symbol symbol = createMockSymbol(name, line, isParameter, isModuleVariable);
        when(symbol.getScope()).thenReturn(scope);
        return symbol;
    }

    private SymbolReference createMockReference(int line, String context) {
        SymbolReference ref = mock(SymbolReference.class);
        when(ref.getLine()).thenReturn(line);
        when(ref.getContext()).thenReturn(context);
        return ref;
    }

    private Scope createMockScope(int startLine, int endLine, ScopeType type) {
        Scope scope = mock(Scope.class);
        when(scope.getStartLine()).thenReturn(startLine);
        when(scope.getEndLine()).thenReturn(endLine);
        when(scope.getType()).thenReturn(type);
        when(scope.getChildren()).thenReturn(Collections.emptyList());
        when(scope.getSymbols()).thenReturn(Collections.emptyList());
        when(scope.getScopeAtLine(anyInt())).thenReturn(null);
        when(scope.getParent()).thenReturn(null);
        return scope;
    }
}
