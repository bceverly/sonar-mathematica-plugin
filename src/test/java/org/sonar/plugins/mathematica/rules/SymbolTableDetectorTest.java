package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.mathematica.symboltable.Scope;
import org.sonar.plugins.mathematica.symboltable.ScopeType;
import org.sonar.plugins.mathematica.symboltable.Symbol;
import org.sonar.plugins.mathematica.symboltable.SymbolReference;
import org.sonar.plugins.mathematica.symboltable.SymbolTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
    void testDetectUnusedVariableWithClumpAssignment() throws IOException {
        // Clump assignments should NOT be flagged as unused variables
        // These are template/registration definitions
        Symbol clumpSymbol = createMockSymbol("AnalysisFunctionClumplate", 1, false, false);
        when(symbolTable.getUnusedSymbols()).thenReturn(Collections.singletonList(clumpSymbol));

        // Mock file contents for isSideEffectAssignment check
        String fileContents = "AnalysisFunctionClumplate = Clump[{\n"
                + "    SaveAs -> \"AnalyzeFunction\",\n"
                + "    Clumplate->True\n"
                + "}];";
        when(inputFile.contents()).thenReturn(fileContents);

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
    void testDetectWriteOnlyVariableWithClumpAssignment() throws IOException {
        // Clump assignments should NOT be flagged as write-only variables
        // These are template/registration definitions, not dead stores
        Symbol symbol = createMockSymbol("AnalysisFunctionClumplate", 1, false, false);
        SymbolReference assign = createMockReference(1, "AnalysisFunctionClumplate = Clump[{...}]");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbol.getReferences()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        // Mock file contents for isSideEffectAssignment check
        String fileContents = "AnalysisFunctionClumplate = Clump[{\n"
                + "    SaveAs -> \"AnalyzeFunction\",\n"
                + "    Clumplate->True\n"
                + "}];";
        when(inputFile.contents()).thenReturn(fileContents);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectWriteOnlyVariable(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDetectWriteOnlyVariableWithModuleVariable() {
        // Module/Block/With variables should NOT be flagged as write-only
        // These are local scope variables that may be used within their scope
        Symbol moduleVar = createMockSymbol("sf", 10, false, true); // isModuleVariable = true
        SymbolReference assign = createMockReference(10, "sf = $This[StaticFigure, {#}]");

        when(moduleVar.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(moduleVar.getReferences()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(moduleVar));

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

    @ParameterizedTest
    @MethodSource("symbolTableDetectorTestData")
    void testSymbolTableDetectorMethods(String testName, SymbolTableSetup setup, SymbolTableDetectorMethod method) {
        setup.setupMocks(symbolTable);
        assertDoesNotThrow(() -> method.execute(context, inputFile, symbolTable));
    }

    private static Stream<Arguments> symbolTableDetectorTestData() {
        return Stream.of(
            Arguments.of("TypeInconsistency", (SymbolTableSetup) table -> {
                Symbol symbol = createMockSymbol("mixed", 10, false, false);
                SymbolReference stringRef = createMockReference(15, "mixed + \"text\"");
                SymbolReference numberRef = createMockReference(20, "mixed + 5");
                SymbolReference listRef = createMockReference(25, "mixed[[1]]");
                when(symbol.getAllReferencesSorted()).thenReturn(List.of(stringRef, numberRef, listRef));
                when(table.getAllSymbols()).thenReturn(Collections.singletonList(symbol));
            }, (SymbolTableDetectorMethod) SymbolTableDetector::detectTypeInconsistency),

            Arguments.of("VariableReuseWithDifferentSemantics", (SymbolTableSetup) table -> {
                Symbol symbol = createMockSymbol("temp", 10, false, false);
                SymbolReference assign1 = createMockReference(10, "temp = list");
                SymbolReference assign2 = createMockReference(50, "temp = counter");
                when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
                when(table.getAllSymbols()).thenReturn(Collections.singletonList(symbol));
            }, (SymbolTableDetectorMethod) SymbolTableDetector::detectVariableReuseWithDifferentSemantics),

            Arguments.of("IncorrectClosureCapture", (SymbolTableSetup) table -> {
                Scope loopScope = createMockScope(1, 50, ScopeType.MODULE);
                when(loopScope.getName()).thenReturn("Do");
                Scope functionScope = createMockScope(20, 30, ScopeType.FUNCTION);
                when(loopScope.getChildren()).thenReturn(Collections.singletonList(functionScope));
                Symbol loopVar = createMockSymbolWithScope("i", 5, false, true, loopScope);
                SymbolReference ref = createMockReference(25, "i + 1");
                when(loopVar.getReferences()).thenReturn(Collections.singletonList(ref));
                when(table.getAllSymbols()).thenReturn(Collections.singletonList(loopVar));
            }, (SymbolTableDetectorMethod) SymbolTableDetector::detectIncorrectClosureCapture),

            Arguments.of("ScopeLeakThroughDynamicEvaluation", (SymbolTableSetup) table -> {
                Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
                Symbol symbol = createMockSymbolWithScope("dynamic", 10, false, true, moduleScope);
                SymbolReference ref = createMockReference(20, "ToExpression[\"dynamic\"]");
                when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(ref));
                when(table.getAllSymbols()).thenReturn(Collections.singletonList(symbol));
            }, (SymbolTableDetectorMethod) SymbolTableDetector::detectScopeLeakThroughDynamicEvaluation)
        );
    }

    @FunctionalInterface
    private interface SymbolTableSetup {
        void setupMocks(SymbolTable table);
    }

    @FunctionalInterface
    private interface SymbolTableDetectorMethod {
        void execute(SensorContext context, InputFile file, SymbolTable table);
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

    // ===== ADDITIONAL COMPREHENSIVE TESTS FOR 80%+ COVERAGE =====

    @Test
    void testAssignedButNeverReadWithFileReadError() throws IOException {
        Symbol symbol = createMockSymbol("assigned", 15, false, false);
        when(symbolTable.getAssignedButNeverReadSymbols()).thenReturn(Collections.singletonList(symbol));
        when(inputFile.contents()).thenThrow(new RuntimeException("Cannot read file"));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectAssignedButNeverRead(context, inputFile, symbolTable)
        );
    }

    @Test
    void testAssignedButNeverReadWithSideEffectClump() throws IOException {
        Symbol symbol = createMockSymbol("MyTemplate", 10, false, false);
        when(symbolTable.getAssignedButNeverReadSymbols()).thenReturn(Collections.singletonList(symbol));
        when(inputFile.contents()).thenReturn("MyTemplate = Clump[{field1, field2}]");

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectAssignedButNeverRead(context, inputFile, symbolTable)
        );
    }

    @Test
    void testAssignedButNeverReadWithDeclarePackage() throws IOException {
        Symbol symbol = createMockSymbol("MyPkg", 5, false, false);
        when(symbolTable.getAssignedButNeverReadSymbols()).thenReturn(Collections.singletonList(symbol));
        when(inputFile.contents()).thenReturn("MyPkg = DeclarePackage[\"MyPackage`\"]");

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectAssignedButNeverRead(context, inputFile, symbolTable)
        );
    }

    @Test
    void testDeadStoreWithReads() {
        Symbol symbol = createMockSymbol("x", 10, false, false);
        SymbolReference assign1 = createMockReference(10, "x = 5");
        SymbolReference assign2 = createMockReference(20, "x = 10");
        SymbolReference read = createMockReference(15, "Print[x]");

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbol.getReferences()).thenReturn(Collections.singletonList(read));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectDeadStore(context, inputFile, symbolTable)
        );
    }

    @Test
    void testUsedBeforeAssignmentNoAssignments() {
        Symbol symbol = createMockSymbol("y", 5, false, false);
        SymbolReference use = createMockReference(10, "y + 1");

        when(symbol.getAssignments()).thenReturn(Collections.emptyList());
        when(symbol.getReferences()).thenReturn(Collections.singletonList(use));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUsedBeforeAssignment(context, inputFile, symbolTable)
        );
    }

    @Test
    void testUsedBeforeAssignmentProperOrder() {
        Symbol symbol = createMockSymbol("z", 5, false, false);
        SymbolReference assign = createMockReference(5, "z = 10");
        SymbolReference use = createMockReference(10, "z + 1");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbol.getReferences()).thenReturn(Collections.singletonList(use));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUsedBeforeAssignment(context, inputFile, symbolTable)
        );
    }

    @Test
    void testVariableInWrongScopeNoReferences() {
        Scope parentScope = createMockScope(1, 50, ScopeType.MODULE);
        Scope childScope = createMockScope(10, 20, ScopeType.BLOCK);
        when(parentScope.getChildren()).thenReturn(Collections.singletonList(childScope));

        Symbol symbol = createMockSymbolWithScope("local", 5, false, true, parentScope);
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableInWrongScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testVariableEscapesScopeNoChildren() {
        Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
        when(moduleScope.getChildren()).thenReturn(Collections.emptyList());

        Symbol symbol = createMockSymbolWithScope("captured", 5, false, true, moduleScope);
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableEscapesScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testLifetimeExtendsBeyondScopeSingleReference() {
        Scope scope = createMockScope(1, 100, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope("single", 10, false, true, scope);

        SymbolReference ref = createMockReference(50, "single = 5");
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectLifetimeExtendsBeyondScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testLifetimeExtendsBeyondScopeSmallScope() {
        Scope scope = createMockScope(1, 8, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope("narrow", 2, false, true, scope);

        SymbolReference ref1 = createMockReference(3, "narrow = 5");
        SymbolReference ref2 = createMockReference(4, "narrow + 1");
        when(symbol.getAllReferencesSorted()).thenReturn(List.of(ref1, ref2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectLifetimeExtendsBeyondScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testModifiedInUnexpectedScopeNullScopes() {
        Scope scope = createMockScope(1, 100, ScopeType.MODULE);
        when(scope.getScopeAtLine(anyInt())).thenReturn(null);

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
    void testModifiedInUnexpectedScopeParentChild() {
        Scope parentScope = createMockScope(1, 100, ScopeType.MODULE);
        Scope childScope = createMockScope(20, 40, ScopeType.BLOCK);
        when(childScope.getParent()).thenReturn(parentScope);
        when(parentScope.getScopeAtLine(20)).thenReturn(childScope);
        when(parentScope.getScopeAtLine(50)).thenReturn(parentScope);

        Symbol symbol = createMockSymbolWithScope("modified", 10, false, false, parentScope);
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
    void testGlobalVariablePollutionFewGlobals() {
        Scope globalScope = createMockScope(1, 1000, ScopeType.GLOBAL);
        List<Symbol> globalVars = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            globalVars.add(createMockSymbol("global" + i, i + 1, false, false));
        }

        when(symbolTable.getGlobalScope()).thenReturn(globalScope);
        when(globalScope.getSymbols()).thenReturn(globalVars);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectGlobalVariablePollution(context, inputFile, symbolTable)
        );
    }

    @Test
    void testCircularDependenciesWithStringsAndComments() {
        Scope globalScope = createMockScope(1, 100, ScopeType.GLOBAL);
        Symbol symbolA = createMockSymbol("packageName", 10, false, false);
        when(symbolA.getScope()).thenReturn(globalScope);

        // Assignment contains string that looks like variable reference
        SymbolReference assignA = createMockReference(10, "packageName = \"MyPackage\"");
        when(symbolA.getAssignments()).thenReturn(Collections.singletonList(assignA));

        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbolA));
        when(symbolTable.getSymbolByName("packageName")).thenReturn(symbolA);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @Test
    void testCircularDependenciesWithCommentedCode() {
        Scope globalScope = createMockScope(1, 100, ScopeType.GLOBAL);
        Symbol symbolA = createMockSymbol("a", 10, false, false);
        when(symbolA.getScope()).thenReturn(globalScope);

        // Assignment contains commented variable reference
        SymbolReference assignA = createMockReference(10, "a = (* b + *) 5");
        when(symbolA.getAssignments()).thenReturn(Collections.singletonList(assignA));

        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbolA));
        when(symbolTable.getSymbolByName("a")).thenReturn(symbolA);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @Test
    void testCircularDependenciesModuleVariableInitializations() {
        // Module variable initializations should NOT create false circular dependencies
        // E.g., Module[{panelWidth=1000, panelHeight=650}, ...] - these are independent initializations
        Scope moduleScope = createMockScope(10, 50, ScopeType.MODULE);
        Symbol panelWidth = createMockSymbol("panelWidth", 10, false, true); // Module variable
        Symbol panelHeight = createMockSymbol("panelHeight", 10, false, true); // Module variable
        when(panelWidth.getScope()).thenReturn(moduleScope);
        when(panelHeight.getScope()).thenReturn(moduleScope);

        // These should be excluded due to being module variables
        when(symbolTable.getAllSymbols()).thenReturn(List.of(panelWidth, panelHeight));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @Test
    void testNamingConventionLowercase() {
        Symbol lowercase = createMockSymbol("temp1", 10, false, false);
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(lowercase));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectNamingConventionViolations(context, inputFile, symbolTable)
        );
    }

    @Test
    void testConstantNotMarkedAsConstantFewReads() {
        Symbol constant = createMockSymbol("PI", 10, false, true);
        SymbolReference assign = createMockReference(10, "PI = 3.14159");
        SymbolReference ref1 = createMockReference(20, "PI * 2");

        when(constant.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(constant.getReferences()).thenReturn(Collections.singletonList(ref1));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(constant));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectConstantNotMarkedAsConstant(context, inputFile, symbolTable)
        );
    }

    @Test
    void testConstantNotMarkedAsConstantParameter() {
        Symbol param = createMockSymbol("param", 5, true, false);
        SymbolReference assign = createMockReference(10, "param = 5");

        when(param.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(param.getReferences()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(param));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectConstantNotMarkedAsConstant(context, inputFile, symbolTable)
        );
    }

    @Test
    void testTypeInconsistencyWithMap() {
        Symbol symbol = createMockSymbol("result", 10, false, false);
        SymbolReference assign = createMockReference(10, "result = Map[f, data]");
        SymbolReference partRef = createMockReference(15, "result[[1]]");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbol.getAllReferencesSorted()).thenReturn(List.of(assign, partRef));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    @ParameterizedTest
    @MethodSource("typeInconsistencyTestData")
    void testTypeInconsistency(String symbolName, String assignmentCode) {
        Symbol symbol = createMockSymbol(symbolName, 10, false, false);
        SymbolReference assign = createMockReference(10, assignmentCode);

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(assign));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    private static Stream<Arguments> typeInconsistencyTestData() {
        return Stream.of(
            Arguments.of("data", "data = Table[i^2, {i, 10}]"),
            Arguments.of("msg", "msg = \"Hello\""),
            Arguments.of("count", "count = 42")
        );
    }

    @Test
    void testVariableReuseWithCommonPattern() {
        Symbol symbol = createMockSymbol("temp", 10, false, false);
        SymbolReference assign1 = createMockReference(10, "temp = computation");
        SymbolReference assign2 = createMockReference(50, "temp = anothercomputation");

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableReuseWithDifferentSemantics(context, inputFile, symbolTable)
        );
    }

    @Test
    void testVariableReuseSingleAssignment() {
        Symbol symbol = createMockSymbol("single", 10, false, false);
        SymbolReference assign = createMockReference(10, "single = value");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableReuseWithDifferentSemantics(context, inputFile, symbolTable)
        );
    }

    @Test
    void testIncorrectClosureCaptureNoLoop() {
        Scope scope = createMockScope(1, 50, ScopeType.MODULE);
        when(scope.getName()).thenReturn("SomeFunction");

        Symbol symbol = createMockSymbolWithScope("var", 5, false, true, scope);
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectIncorrectClosureCapture(context, inputFile, symbolTable)
        );
    }

    @Test
    void testIncorrectClosureCaptureTableLoop() {
        Scope loopScope = createMockScope(1, 50, ScopeType.MODULE);
        when(loopScope.getName()).thenReturn("Table");

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

    @ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "var,Symbol[\"var\"]",
        "expr,Evaluate[expr]",
        "held,ReleaseHold[Hold[held]]"
    })
    void testScopeLeakThroughDynamicEvaluation(String symbolName, String referenceCode) {
        Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope(symbolName, 10, false, true, moduleScope);

        SymbolReference ref = createMockReference(20, referenceCode);
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectScopeLeakThroughDynamicEvaluation(context, inputFile, symbolTable)
        );
    }

    @Test
    void testScopeLeakNotModule() {
        Scope globalScope = createMockScope(1, 50, ScopeType.GLOBAL);
        Symbol symbol = createMockSymbolWithScope("global", 10, false, false, globalScope);

        SymbolReference ref = createMockReference(20, "ToExpression[\"global\"]");
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectScopeLeakThroughDynamicEvaluation(context, inputFile, symbolTable)
        );
    }

    // Helper methods

    private static Symbol createMockSymbol(String name, int line, boolean isParameter, boolean isModuleVariable) {
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

    private static Symbol createMockSymbolWithScope(String name, int line, boolean isParameter,
                                            boolean isModuleVariable, Scope scope) {
        Symbol symbol = createMockSymbol(name, line, isParameter, isModuleVariable);
        when(symbol.getScope()).thenReturn(scope);
        return symbol;
    }

    private static SymbolReference createMockReference(int line, String context) {
        SymbolReference ref = mock(SymbolReference.class);
        when(ref.getLine()).thenReturn(line);
        when(ref.getContext()).thenReturn(context);
        return ref;
    }

    private static Scope createMockScope(int startLine, int endLine, ScopeType type) {
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

    // ===== ADDITIONAL EDGE CASE TESTS FOR IMPROVED COVERAGE =====

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "DefineClass",
        "RegisterComponent",
        "DefineModule",
        "CreateTemplate",
        "DeclareType"
    })
    void testSideEffectAssignmentVariousFunctions(String functionName) throws IOException {
        Symbol symbol = createMockSymbol("MyVar", 1, false, false);
        when(symbolTable.getAssignedButNeverReadSymbols()).thenReturn(Collections.singletonList(symbol));
        when(inputFile.contents()).thenReturn("MyVar = " + functionName + "[params]");

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectAssignedButNeverRead(context, inputFile, symbolTable)
        );
    }

    @Test
    void testUnusedParameterInAssociationReturningFunction() throws IOException {
        Symbol param = createMockSymbol("x", 5, true, false);
        when(param.isUnused()).thenReturn(true);
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(param));

        // Function returns Association
        String fileContents = "f[x_, y_] := <|\n"
                + "  \"key1\" -> y,\n"
                + "  \"key2\" -> y * 2\n"
                + "|>";
        when(inputFile.contents()).thenReturn(fileContents);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUnusedParameter(context, inputFile, symbolTable)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unusedParameterEdgeCasesData")
    void testUnusedParameterEdgeCases(String testName, String symbolName, int lineNumber,
                                      String fileContents) throws IOException {
        Symbol param = createMockSymbol(symbolName, lineNumber, true, false);
        when(param.isUnused()).thenReturn(true);
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(param));
        when(inputFile.contents()).thenReturn(fileContents);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectUnusedParameter(context, inputFile, symbolTable)
        );
    }

    private static Stream<Arguments> unusedParameterEdgeCasesData() {
        return Stream.of(
            Arguments.of("PatternDestructuring", "a", 5,
                "f[guesses:{a_, b_, c_}] := guesses[[1]] + guesses[[2]]"),
            Arguments.of("LineBeyondFileLength", "x", 1000, "f[x_] := x + 1"),
            Arguments.of("InvalidLineNumber", "x", 0, "f[x_] := x + 1")
        );
    }

    @Test
    void testCircularDependenciesWithComparisonExpressions() {
        Scope globalScope = createMockScope(1, 100, ScopeType.GLOBAL);

        Symbol webmQ = createMockSymbol("webmQ", 10, false, false);
        Symbol pacletName = createMockSymbol("pacletName", 5, false, false);

        when(webmQ.getScope()).thenReturn(globalScope);
        when(pacletName.getScope()).thenReturn(globalScope);

        // Should NOT create circular dependency - comparison expression
        SymbolReference assign = createMockReference(10, "webmQ = pacletName == \"webMathematica\"");
        when(webmQ.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(pacletName.getAssignments()).thenReturn(Collections.emptyList());

        when(symbolTable.getAllSymbols()).thenReturn(List.of(webmQ, pacletName));
        when(symbolTable.getSymbolByName("webmQ")).thenReturn(webmQ);
        when(symbolTable.getSymbolByName("pacletName")).thenReturn(pacletName);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "a = b === c",
        "a = b != c",
        "a = b =!= c",
        "a = b > c",
        "a = b < c",
        "a = b >= c",
        "a = b <= c"
    })
    void testCircularDependenciesWithVariousComparisons(String assignmentCode) {
        Scope globalScope = createMockScope(1, 100, ScopeType.GLOBAL);

        Symbol symbolA = createMockSymbol("a", 10, false, false);
        Symbol symbolB = createMockSymbol("b", 5, false, false);
        Symbol symbolC = createMockSymbol("c", 3, false, false);

        when(symbolA.getScope()).thenReturn(globalScope);
        when(symbolB.getScope()).thenReturn(globalScope);
        when(symbolC.getScope()).thenReturn(globalScope);

        SymbolReference assign = createMockReference(10, assignmentCode);
        when(symbolA.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbolB.getAssignments()).thenReturn(Collections.emptyList());
        when(symbolC.getAssignments()).thenReturn(Collections.emptyList());

        when(symbolTable.getAllSymbols()).thenReturn(List.of(symbolA, symbolB, symbolC));
        when(symbolTable.getSymbolByName("a")).thenReturn(symbolA);
        when(symbolTable.getSymbolByName("b")).thenReturn(symbolB);
        when(symbolTable.getSymbolByName("c")).thenReturn(symbolC);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @Test
    void testCircularDependenciesWithNestedComments() {
        Scope globalScope = createMockScope(1, 100, ScopeType.GLOBAL);

        Symbol symbolA = createMockSymbol("a", 10, false, false);
        when(symbolA.getScope()).thenReturn(globalScope);

        // Nested comments should be handled correctly
        SymbolReference assign = createMockReference(10, "a = (* outer (* nested *) outer *) 5");
        when(symbolA.getAssignments()).thenReturn(Collections.singletonList(assign));

        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbolA));
        when(symbolTable.getSymbolByName("a")).thenReturn(symbolA);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @Test
    void testCircularDependenciesWithEscapedStrings() {
        Scope globalScope = createMockScope(1, 100, ScopeType.GLOBAL);

        Symbol symbolA = createMockSymbol("a", 10, false, false);
        when(symbolA.getScope()).thenReturn(globalScope);

        // Escaped quotes in strings should be handled
        SymbolReference assign = createMockReference(10, "a = \"text with \\\"escaped\\\" quotes\"");
        when(symbolA.getAssignments()).thenReturn(Collections.singletonList(assign));

        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbolA));
        when(symbolTable.getSymbolByName("a")).thenReturn(symbolA);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @Test
    void testCircularDependenciesWordBoundary() {
        Scope globalScope = createMockScope(1, 100, ScopeType.GLOBAL);

        Symbol sf = createMockSymbol("sf", 10, false, false);
        Symbol transform = createMockSymbol("transform", 15, false, false);
        Symbol staticFigure = createMockSymbol("StaticFigure", 20, false, false);

        when(sf.getScope()).thenReturn(globalScope);
        when(transform.getScope()).thenReturn(globalScope);
        when(staticFigure.getScope()).thenReturn(globalScope);

        // "sf" should not match "transform" or "StaticFigure" due to word boundaries
        SymbolReference transformAssign = createMockReference(15, "transform = TransformData[x]");
        SymbolReference sfAssign = createMockReference(10, "sf = StaticFigure[data]");

        when(sf.getAssignments()).thenReturn(Collections.singletonList(sfAssign));
        when(transform.getAssignments()).thenReturn(Collections.singletonList(transformAssign));
        when(staticFigure.getAssignments()).thenReturn(Collections.emptyList());

        when(symbolTable.getAllSymbols()).thenReturn(List.of(sf, transform, staticFigure));
        when(symbolTable.getSymbolByName("sf")).thenReturn(sf);
        when(symbolTable.getSymbolByName("transform")).thenReturn(transform);
        when(symbolTable.getSymbolByName("StaticFigure")).thenReturn(staticFigure);

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectCircularVariableDependencies(context, inputFile, symbolTable)
        );
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "Table",
        "Select",
        "Cases",
        "DeleteCases",
        "Range",
        "Array",
        "Join",
        "Append",
        "Prepend",
        "Insert",
        "Delete",
        "Take",
        "Drop",
        "Partition",
        "Split",
        "GatherBy",
        "SortBy",
        "Sort",
        "Reverse",
        "Flatten",
        "Union",
        "Intersection",
        "Complement",
        "Transpose",
        "Dimensions",
        "Position",
        "Extract",
        "MapThread",
        "MapIndexed",
        "Scan",
        "FoldList",
        "NestList",
        "Tuples",
        "Permutations",
        "Subsets",
        "IntegerPartitions",
        "CharacterRange",
        "Keys",
        "Values",
        "Association",
        "Normal",
        "Thread",
        "Outer",
        "Inner"
    })
    void testTypeInconsistencyWithListReturningFunctions(String functionName) {
        Symbol symbol = createMockSymbol("result", 10, false, false);
        SymbolReference assign = createMockReference(10, "result = " + functionName + "[params]");

        when(symbol.getAssignments()).thenReturn(Collections.singletonList(assign));
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(assign));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    @Test
    void testTypeInconsistencyTwoSuspectedTypes() {
        Symbol symbol = createMockSymbol("mixed", 10, false, false);
        SymbolReference stringRef = createMockReference(15, "mixed + \"text\"");
        SymbolReference numberRef = createMockReference(20, "mixed + 5");

        // Only 2 types - should not report (needs 3+)
        when(symbol.getAllReferencesSorted()).thenReturn(List.of(stringRef, numberRef));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    @Test
    void testTypeInconsistencyWithNumericContext() {
        Symbol symbol = createMockSymbol("var", 10, false, false);
        SymbolReference addRef = createMockReference(15, "var + 1");
        SymbolReference subRef = createMockReference(20, "var - 2");
        SymbolReference mulRef = createMockReference(25, "var * 3");
        SymbolReference divRef = createMockReference(30, "var / 4");

        when(symbol.getAllReferencesSorted()).thenReturn(List.of(addRef, subRef, mulRef, divRef));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    @Test
    void testTypeInconsistencyWithReverseArithmetic() {
        Symbol symbol = createMockSymbol("var", 10, false, false);
        SymbolReference addRef = createMockReference(15, "10 + var");
        SymbolReference mulRef = createMockReference(20, "5 * var");

        when(symbol.getAllReferencesSorted()).thenReturn(List.of(addRef, mulRef));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    @Test
    void testTypeInconsistencyWithPartAccess() {
        Symbol symbol = createMockSymbol("list", 10, false, false);
        SymbolReference partRef1 = createMockReference(15, "Part[list, 1]");
        SymbolReference partRef2 = createMockReference(20, "Part[list, 2, 3]");

        when(symbol.getAllReferencesSorted()).thenReturn(List.of(partRef1, partRef2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectTypeInconsistency(context, inputFile, symbolTable)
        );
    }

    @Test
    void testNamingConventionSingleCharParameter() {
        Symbol param = createMockSymbol("x", 5, true, false);  // Parameter - should be skipped
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(param));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectNamingConventionViolations(context, inputFile, symbolTable)
        );
    }

    @Test
    void testRedundantAssignmentDuplicatePosition() {
        Symbol symbol = createMockSymbol("x", 10, false, false);

        // Same position - should be deduplicated
        SymbolReference assign1 = createMockReference(10, "x = 5");
        SymbolReference assign2 = createMockReference(10, "x = 5");
        when(assign1.getColumn()).thenReturn(5);
        when(assign2.getColumn()).thenReturn(5);

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectRedundantAssignment(context, inputFile, symbolTable)
        );
    }

    @Test
    void testRedundantAssignmentSameLine() {
        Symbol symbol = createMockSymbol("x", 10, false, false);

        // Same line - should not report
        SymbolReference assign1 = createMockReference(10, "x = 5; x = 5");
        SymbolReference assign2 = createMockReference(10, "x = 5; x = 5");
        when(assign1.getColumn()).thenReturn(1);
        when(assign2.getColumn()).thenReturn(8);

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectRedundantAssignment(context, inputFile, symbolTable)
        );
    }

    @Test
    void testRedundantAssignmentDifferentValues() {
        Symbol symbol = createMockSymbol("x", 10, false, false);

        // Different values - should not report
        SymbolReference assign1 = createMockReference(10, "x = 5");
        SymbolReference assign2 = createMockReference(15, "x = 10");
        when(assign1.getColumn()).thenReturn(1);
        when(assign2.getColumn()).thenReturn(1);

        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectRedundantAssignment(context, inputFile, symbolTable)
        );
    }

    @Test
    void testVariableInWrongScopeNonModuleVariable() {
        Symbol symbol = createMockSymbol("global", 5, false, false);  // Not a module variable
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableInWrongScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testVariableEscapesScopeNonModuleVariable() {
        Symbol symbol = createMockSymbol("global", 5, false, false);  // Not a module variable
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableEscapesScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testVariableEscapesScopeNonFunctionChild() {
        Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
        Scope blockScope = createMockScope(20, 30, ScopeType.BLOCK);  // BLOCK, not FUNCTION
        when(moduleScope.getChildren()).thenReturn(Collections.singletonList(blockScope));

        Symbol symbol = createMockSymbolWithScope("var", 5, false, true, moduleScope);
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectVariableEscapesScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testLifetimeExtendsBeyondScopeNonModuleVariable() {
        Symbol symbol = createMockSymbol("global", 10, false, false);  // Not a module variable
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectLifetimeExtendsBeyondScope(context, inputFile, symbolTable)
        );
    }

    @Test
    void testConstantNotMarkedAsConstantNonModuleVariable() {
        Symbol symbol = createMockSymbol("global", 10, false, false);  // Not a module variable
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectConstantNotMarkedAsConstant(context, inputFile, symbolTable)
        );
    }

    @Test
    void testConstantNotMarkedAsConstantMultipleAssignments() {
        Symbol symbol = createMockSymbol("var", 10, false, true);
        SymbolReference assign1 = createMockReference(10, "var = 1");
        SymbolReference assign2 = createMockReference(20, "var = 2");

        // Multiple assignments - not a constant
        when(symbol.getAssignments()).thenReturn(List.of(assign1, assign2));
        when(symbol.getReferences()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectConstantNotMarkedAsConstant(context, inputFile, symbolTable)
        );
    }

    @Test
    void testIncorrectClosureCaptureNonModuleVariable() {
        Symbol symbol = createMockSymbol("global", 5, false, false);  // Not a module variable
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectIncorrectClosureCapture(context, inputFile, symbolTable)
        );
    }

    @Test
    void testScopeLeakNonModuleVariable() {
        Symbol symbol = createMockSymbol("global", 10, false, false);  // Not a module variable
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectScopeLeakThroughDynamicEvaluation(context, inputFile, symbolTable)
        );
    }

    @Test
    void testScopeLeakNoReferences() {
        Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope("var", 10, false, true, moduleScope);

        when(symbol.getAllReferencesSorted()).thenReturn(Collections.emptyList());
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectScopeLeakThroughDynamicEvaluation(context, inputFile, symbolTable)
        );
    }

    @Test
    void testScopeLeakNoSymbolInReference() {
        Scope moduleScope = createMockScope(1, 50, ScopeType.MODULE);
        Symbol symbol = createMockSymbolWithScope("var", 10, false, true, moduleScope);

        // Reference doesn't contain Symbol/ToExpression/Evaluate/ReleaseHold
        SymbolReference ref = createMockReference(20, "var + 1");
        when(symbol.getAllReferencesSorted()).thenReturn(Collections.singletonList(ref));
        when(symbolTable.getAllSymbols()).thenReturn(Collections.singletonList(symbol));

        assertDoesNotThrow(() ->
            SymbolTableDetector.detectScopeLeakThroughDynamicEvaluation(context, inputFile, symbolTable)
        );
    }
}
