package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnusedAndNamingDetectorTest {

    private UnusedAndNamingDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new UnusedAndNamingDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        when(inputFile.filename()).thenReturn("test.m");
    }

    // ===== UNUSED CODE DETECTION TESTS (15 rules) =====

    @Test
    void testDetectUnusedPrivateFunction() {
        String content = "privateFunc[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectUnusedPrivateFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedFunctionParameter() {
        String content = "f[x_, y_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectUnusedFunctionParameter(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedModuleVariable() {
        String content = "Module[{x = 5, y = 10}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectUnusedModuleVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedWithVariable() {
        String content = "With[{x = 5, y = 10}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectUnusedWithVariable(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedImport() {
        String content = "Needs[\"UnusedPackage`\"];\nf[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectUnusedImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedPatternName() {
        String content = "f[x_] := 1;";
        assertDoesNotThrow(() ->
            detector.detectUnusedPatternName(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedOptionalParameter() {
        String content = "f[x_, y___ := 10] := x;";
        assertDoesNotThrow(() ->
            detector.detectUnusedOptionalParameter(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadCodeAfterReturn() {
        String content = "f[x_] := (Return[x]; Print[\"dead code\"])";
        assertDoesNotThrow(() ->
            detector.detectDeadCodeAfterReturn(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnreachableAfterAbortThrow() {
        String content = "f[x_] := (Abort[]; Print[\"unreachable\"])";
        assertDoesNotThrow(() ->
            detector.detectUnreachableAfterAbortThrow(context, inputFile, content)
        );
    }

    @Test
    void testDetectAssignmentNeverRead() {
        String content = "x = 5; x = 10; y = x;";
        assertDoesNotThrow(() ->
            detector.detectAssignmentNeverRead(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionDefinedButNeverCalled() {
        String content = "UnusedFunc[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectFunctionDefinedButNeverCalled(context, inputFile, content)
        );
    }

    @Test
    void testDetectRedefinedWithoutUse() {
        String content = "x = 5; x = 10;";
        assertDoesNotThrow(() ->
            detector.detectRedefinedWithoutUse(context, inputFile, content)
        );
    }

    @Test
    void testDetectLoopVariableUnused() {
        String content = "Do[Print[\"hello\"], {i, 1, 10}];";
        assertDoesNotThrow(() ->
            detector.detectLoopVariableUnused(context, inputFile, content)
        );
    }

    @Test
    void testDetectCatchWithoutThrow() {
        String content = "Catch[Print[\"test\"]]";
        assertDoesNotThrow(() ->
            detector.detectCatchWithoutThrow(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionAlwaysFalse() {
        String content = "If[False, result, other]";
        assertDoesNotThrow(() ->
            detector.detectConditionAlwaysFalse(context, inputFile, content)
        );
    }

    // ===== SHADOWING & NAMING TESTS (15 rules) =====

    @Test
    void testDetectLocalShadowsGlobal() {
        String content = "x = 10;\nModule[{x = 5}, x + 1]";
        assertDoesNotThrow(() ->
            detector.detectLocalShadowsGlobal(context, inputFile, content)
        );
    }

    @Test
    void testDetectParameterShadowsBuiltin() {
        String content = "f[List_] := List + 1;";
        assertDoesNotThrow(() ->
            detector.detectParameterShadowsBuiltin(context, inputFile, content)
        );
    }

    @Test
    void testDetectLocalShadowsParameter() {
        String content = "f[x_] := Module[{x = 5}, x + 1];";
        assertDoesNotThrow(() ->
            detector.detectLocalShadowsParameter(context, inputFile, content)
        );
    }

    @Test
    void testDetectMultipleDefinitionsSameSymbol() {
        String content = "f[x_] := x + 1;\nf[x_, y_] := x + y;";
        assertDoesNotThrow(() ->
            detector.detectMultipleDefinitionsSameSymbol(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolNameTooShort() {
        String longFunction = "f[x_] := (\n" + "a = 1;\n".repeat(60) + "a\n)";
        assertDoesNotThrow(() ->
            detector.detectSymbolNameTooShort(context, inputFile, longFunction)
        );
    }

    @Test
    void testDetectSymbolNameTooLong() {
        String content = "veryLongSymbolNameThatExceedsFiftyCharactersInLength = 5;";
        assertDoesNotThrow(() ->
            detector.detectSymbolNameTooLong(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentNamingConvention() {
        String content = "camelCaseVar = 1;\nsnake_case_var = 2;\nPascalCase = 3;";
        assertDoesNotThrow(() ->
            detector.detectInconsistentNamingConvention(context, inputFile, content)
        );
    }

    @Test
    void testDetectBuiltinNameInLocalScope() {
        String content = "Module[{List = {1, 2, 3}}, List[[1]]]";
        assertDoesNotThrow(() ->
            detector.detectBuiltinNameInLocalScope(context, inputFile, content)
        );
    }

    @Test
    void testDetectContextConflicts() {
        String content = "Context1`myFunc[x_] := x;\nContext2`myFunc[y_] := y;";
        assertDoesNotThrow(() ->
            detector.detectContextConflicts(context, inputFile, content)
        );
    }

    @Test
    void testDetectReservedNameUsage() {
        String content = "$Path = \"/custom/path\";";
        assertDoesNotThrow(() ->
            detector.detectReservedNameUsage(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrivateContextSymbolPublic() {
        String content = "MyPackage`Private`internalFunc[x]";
        assertDoesNotThrow(() ->
            detector.detectPrivateContextSymbolPublic(context, inputFile, content)
        );
    }

    @Test
    void testDetectMismatchedBeginEnd() {
        String content = "BeginPackage[\"Test`\"];\nBegin[\"`Private`\"];";
        assertDoesNotThrow(() ->
            detector.detectMismatchedBeginEnd(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolAfterEndPackage() {
        String content = "BeginPackage[\"Test`\"];\nEndPackage[];\nf[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectSymbolAfterEndPackage(context, inputFile, content)
        );
    }

    @Test
    void testDetectGlobalInPackage() {
        String content = "BeginPackage[\"Test`\"];\nGlobal`var = 5;\nEndPackage[];";
        assertDoesNotThrow(() ->
            detector.detectGlobalInPackage(context, inputFile, content)
        );
    }

    @Test
    void testDetectTempVariableNotTemp() {
        String content = "temp = 1;\ntemp = 2;\ntemp = 3;\ntemp = 4;";
        assertDoesNotThrow(() ->
            detector.detectTempVariableNotTemp(context, inputFile, content)
        );
    }

    // ===== UNDEFINED SYMBOL DETECTION TESTS (10 rules) =====

    @Test
    void testDetectUndefinedFunctionCall() {
        String content = "result = undefinedFunc[x];";
        assertDoesNotThrow(() ->
            detector.detectUndefinedFunctionCall(context, inputFile, content)
        );
    }

    @Test
    void testDetectUndefinedVariableReference() {
        String content = "result = undefinedVar + 5;";
        assertDoesNotThrow(() ->
            detector.detectUndefinedVariableReference(context, inputFile, content)
        );
    }

    @Test
    void testDetectTypoInBuiltinName() {
        String content = "result = Lenght[{1, 2, 3}];";
        assertDoesNotThrow(() ->
            detector.detectTypoInBuiltinName(context, inputFile, content)
        );
    }

    @Test
    void testDetectWrongCapitalization() {
        String content = "result = length[{1, 2, 3}];";
        assertDoesNotThrow(() ->
            detector.detectWrongCapitalization(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingImport() {
        String content = "result = UnknownContext`func[x];";
        assertDoesNotThrow(() ->
            detector.detectMissingImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectContextNotFound() {
        String content = "Needs[\"TestDebugPackage`\"];";
        assertDoesNotThrow(() ->
            detector.detectContextNotFound(context, inputFile, content)
        );
    }

    @Test
    void testDetectSymbolMaskedByImport() {
        String content = "f[x_] := x + 1;\nNeeds[\"SomePackage`\"];";
        assertDoesNotThrow(() ->
            detector.detectSymbolMaskedByImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingPathEntry() {
        String content = "Get[\"myfile.m\"]";
        assertDoesNotThrow(() ->
            detector.detectMissingPathEntry(context, inputFile, content)
        );
    }

    @Test
    void testDetectCircularNeeds() {
        String content = "Needs[\"test`\"];";
        assertDoesNotThrow(() ->
            detector.detectCircularNeeds(context, inputFile, content)
        );
    }

    @Test
    void testDetectForwardReferenceWithoutDeclaration() {
        String content = "result = forwardFunc[x];\nforwardFunc[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectForwardReferenceWithoutDeclaration(context, inputFile, content)
        );
    }

    // ===== COMPREHENSIVE AND EDGE CASE TESTS =====

    @Test
    void testAllUnusedMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectUnusedPrivateFunction(context, inputFile, content);
            detector.detectUnusedFunctionParameter(context, inputFile, content);
            detector.detectUnusedModuleVariable(context, inputFile, content);
            detector.detectUnusedWithVariable(context, inputFile, content);
            detector.detectUnusedImport(context, inputFile, content);
            detector.detectUnusedPatternName(context, inputFile, content);
            detector.detectUnusedOptionalParameter(context, inputFile, content);
            detector.detectDeadCodeAfterReturn(context, inputFile, content);
            detector.detectUnreachableAfterAbortThrow(context, inputFile, content);
            detector.detectAssignmentNeverRead(context, inputFile, content);
            detector.detectFunctionDefinedButNeverCalled(context, inputFile, content);
            detector.detectRedefinedWithoutUse(context, inputFile, content);
            detector.detectLoopVariableUnused(context, inputFile, content);
            detector.detectCatchWithoutThrow(context, inputFile, content);
            detector.detectConditionAlwaysFalse(context, inputFile, content);
        });
    }

    @Test
    void testAllShadowingMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectLocalShadowsGlobal(context, inputFile, content);
            detector.detectParameterShadowsBuiltin(context, inputFile, content);
            detector.detectLocalShadowsParameter(context, inputFile, content);
            detector.detectMultipleDefinitionsSameSymbol(context, inputFile, content);
            detector.detectSymbolNameTooShort(context, inputFile, content);
            detector.detectSymbolNameTooLong(context, inputFile, content);
            detector.detectInconsistentNamingConvention(context, inputFile, content);
            detector.detectBuiltinNameInLocalScope(context, inputFile, content);
            detector.detectContextConflicts(context, inputFile, content);
            detector.detectReservedNameUsage(context, inputFile, content);
            detector.detectPrivateContextSymbolPublic(context, inputFile, content);
            detector.detectMismatchedBeginEnd(context, inputFile, content);
            detector.detectSymbolAfterEndPackage(context, inputFile, content);
            detector.detectGlobalInPackage(context, inputFile, content);
            detector.detectTempVariableNotTemp(context, inputFile, content);
        });
    }

    @Test
    void testAllUndefinedSymbolMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectUndefinedFunctionCall(context, inputFile, content);
            detector.detectUndefinedVariableReference(context, inputFile, content);
            detector.detectTypoInBuiltinName(context, inputFile, content);
            detector.detectWrongCapitalization(context, inputFile, content);
            detector.detectMissingImport(context, inputFile, content);
            detector.detectContextNotFound(context, inputFile, content);
            detector.detectSymbolMaskedByImport(context, inputFile, content);
            detector.detectMissingPathEntry(context, inputFile, content);
            detector.detectCircularNeeds(context, inputFile, content);
            detector.detectForwardReferenceWithoutDeclaration(context, inputFile, content);
        });
    }

    @Test
    void testComplexCodeWithMultipleIssues() {
        String content = "BeginPackage[\"MyPackage`\"];\n"
                + "Begin[\"`Private`\"];\n"
                + "globalVar = 10;\n"
                + "unusedPrivateFunc[x_] := x + 1;\n"
                + "f[x_, y_] := Module[{x = 5, z = 10}, x + globalVar];\n"
                + "temp = 1; temp = 2; temp = 3; temp = 4;\n"
                + "result = undefinedFunc[x];\n"
                + "End[];\n"
                + "EndPackage[];";

        assertDoesNotThrow(() -> {
            detector.detectUnusedPrivateFunction(context, inputFile, content);
            detector.detectUnusedFunctionParameter(context, inputFile, content);
            detector.detectUnusedModuleVariable(context, inputFile, content);
            detector.detectLocalShadowsParameter(context, inputFile, content);
            detector.detectTempVariableNotTemp(context, inputFile, content);
            detector.detectUndefinedFunctionCall(context, inputFile, content);
        });
    }

    @Test
    void testValidCodeWithNoIssues() {
        String content = "f[x_] := Module[{y = x + 1}, y * 2];\n"
                + "result = f[5];\n"
                + "list = {1, 2, 3};\n"
                + "mapped = Map[f, list];";

        assertDoesNotThrow(() -> {
            detector.detectUnusedPrivateFunction(context, inputFile, content);
            detector.detectUnusedFunctionParameter(context, inputFile, content);
            detector.detectUnusedModuleVariable(context, inputFile, content);
        });
    }

    @Test
    void testPackageStructure() {
        String content = "BeginPackage[\"MyPackage`\"];\n"
                + "publicFunc::usage = \"Public function\";\n"
                + "Begin[\"`Private`\"];\n"
                + "publicFunc[x_] := privateHelper[x];\n"
                + "privateHelper[x_] := x + 1;\n"
                + "End[];\n"
                + "EndPackage[];";

        assertDoesNotThrow(() -> {
            detector.detectMismatchedBeginEnd(context, inputFile, content);
            detector.detectSymbolAfterEndPackage(context, inputFile, content);
        });
    }

    @Test
    void testShadowingScenarios() {
        String content = "x = 10;\n"
                + "Module[{x = 5}, x];\n"
                + "f[List_] := List;\n"
                + "g[y_] := Module[{y = 1}, y];";

        assertDoesNotThrow(() -> {
            detector.detectLocalShadowsGlobal(context, inputFile, content);
            detector.detectParameterShadowsBuiltin(context, inputFile, content);
            detector.detectLocalShadowsParameter(context, inputFile, content);
        });
    }

    @Test
    void testNamingConventions() {
        String content = "camelCaseVariable = 1;\n"
                + "snake_case_variable = 2;\n"
                + "PascalCaseVariable = 3;\n"
                + "veryLongVariableNameThatExceedsFiftyCharactersLimit = 4;";

        assertDoesNotThrow(() -> {
            detector.detectInconsistentNamingConvention(context, inputFile, content);
            detector.detectSymbolNameTooLong(context, inputFile, content);
        });
    }

    @Test
    void testContextAndImports() {
        String content = "Needs[\"Package1`\"];\n"
                + "Needs[\"Package2`\"];\n"
                + "result1 = Package1`func[x];\n"
                + "result2 = UnknownPackage`func[y];";

        assertDoesNotThrow(() -> {
            detector.detectUnusedImport(context, inputFile, content);
            detector.detectMissingImport(context, inputFile, content);
        });
    }
}
