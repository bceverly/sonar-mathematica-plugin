package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FrameworkDetectorTest {

    private FrameworkDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new FrameworkDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        when(inputFile.filename()).thenReturn("test.m");
    }

    // ===== NOTEBOOK FRAMEWORK TESTS =====

    @Test
    void testDetectNotebookCellSize() {
        StringBuilder largeCell = new StringBuilder("Cell[");
        for (int i = 0; i < 60; i++) {
            largeCell.append("line ").append(i).append("\n");
        }
        largeCell.append("]");
        String content = largeCell.toString();

        assertDoesNotThrow(() ->
            detector.detectNotebookCellSize(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookCellSizeSmall() {
        String content = "Cell[\"Small cell content\"]";
        assertDoesNotThrow(() ->
            detector.detectNotebookCellSize(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookUnorganized() {
        String content = "Cell[BoxData[code := 5]];\nVerificationTest[test];\n(* scratch test work *)";
        assertDoesNotThrow(() ->
            detector.detectNotebookUnorganized(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookOrganized() {
        String content = "Cell[BoxData[code := 5]];\nSection[\"Tests\"];\nVerificationTest[test];";
        assertDoesNotThrow(() ->
            detector.detectNotebookUnorganized(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookNoSections() {
        StringBuilder content = new StringBuilder("Notebook[{");
        for (int i = 0; i < 15; i++) {
            content.append("Cell[\"code ").append(i).append("\"],\n");
        }
        content.append("}]");

        assertDoesNotThrow(() ->
            detector.detectNotebookNoSections(context, inputFile, content.toString())
        );
    }

    @Test
    void testDetectNotebookWithSections() {
        String content = "Notebook[{\nSection[\"Main\"],\nCell[code1],\nCell[code2],\nSubsection[\"Details\"]\n}]";
        assertDoesNotThrow(() ->
            detector.detectNotebookNoSections(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookInitCellMisuse() {
        String content = "Cell[BoxData[InitializationCell -> True, Integrate[f[x], x]]]";
        assertDoesNotThrow(() ->
            detector.detectNotebookInitCellMisuse(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookInitCellProper() {
        String content = "Cell[BoxData[InitializationCell -> True, a = 5; b = 10;]]";
        assertDoesNotThrow(() ->
            detector.detectNotebookInitCellMisuse(context, inputFile, content)
        );
    }

    // ===== MANIPULATE/DYNAMIC FRAMEWORK TESTS =====

    private static Stream<Arguments> manipulatePerformanceTestData() {
        return Stream.of(
            Arguments.of("Manipulate[Plot[Integrate[f[x, a], x], {x, 0, 10}], {a, 0, 10}]"),
            Arguments.of("Manipulate[Solve[x^2 + a*x + b == 0, x], {a, -5, 5}, {b, -5, 5}]"),
            Arguments.of("Manipulate[Graphics[Circle[{a, b}]], {a, 0, 10}, {b, 0, 10}]")
        );
    }

    @ParameterizedTest
    @MethodSource("manipulatePerformanceTestData")
    void testDetectManipulatePerformance(String content) {
        assertDoesNotThrow(() ->
            detector.detectManipulatePerformance(context, inputFile, content)
        );
    }

    private static Stream<Arguments> dynamicHeavyComputationTestData() {
        return Stream.of(
            Arguments.of("Dynamic[NDSolve[{y'[x] == y[x], y[0] == 1}, y, {x, 0, 10}]]"),
            Arguments.of("Dynamic[Integrate[Sin[x], x]]"),
            Arguments.of("Dynamic[a + b]")
        );
    }

    @ParameterizedTest
    @MethodSource("dynamicHeavyComputationTestData")
    void testDetectDynamicHeavyComputation(String content) {
        assertDoesNotThrow(() ->
            detector.detectDynamicHeavyComputation(context, inputFile, content)
        );
    }

    @Test
    void testDetectDynamicNoTracking() {
        String content = "Dynamic[var1 + var2 + var3 + var4 + var5]";
        assertDoesNotThrow(() ->
            detector.detectDynamicNoTracking(context, inputFile, content)
        );
    }

    @Test
    void testDetectDynamicWithTracking() {
        String content = "Dynamic[a + b + c + d, TrackedSymbols :> {a, b}]";
        assertDoesNotThrow(() ->
            detector.detectDynamicNoTracking(context, inputFile, content)
        );
    }

    @Test
    void testDetectManipulateTooComplex() {
        StringBuilder content = new StringBuilder("Manipulate[Plot[f[");
        for (int i = 0; i < 15; i++) {
            content.append("a").append(i).append(", ");
        }
        content.append("x], {x, 0, 10}], ");
        for (int i = 0; i < 15; i++) {
            content.append("{a").append(i).append(", 0, 10}, ");
        }
        content.append("]");

        assertDoesNotThrow(() ->
            detector.detectManipulateTooComplex(context, inputFile, content.toString())
        );
    }

    @Test
    void testDetectManipulateSimple() {
        String content = "Manipulate[Plot[Sin[a*x], {x, 0, 10}], {a, 1, 5}]";
        assertDoesNotThrow(() ->
            detector.detectManipulateTooComplex(context, inputFile, content)
        );
    }

    // ===== PACKAGE FRAMEWORK TESTS =====

    private static Stream<Arguments> packageNoBeginTestData() {
        return Stream.of(
            Arguments.of("BeginPackage[\"MyPackage`\"];\nMyFunc[x_] := x + 1;\nEndPackage[];"),
            Arguments.of("BeginPackage[\"MyPackage`\"];\nBegin[`Private`];\nhelper[x_] := x * 2;"),
            Arguments.of("BeginPackage[\"MyPackage`\"];\nBegin[`Private`];\nhelper[x_] := x * 2;\nEnd[];\nEndPackage[];")
        );
    }

    @ParameterizedTest
    @MethodSource("packageNoBeginTestData")
    void testDetectPackageNoBegin(String content) {
        assertDoesNotThrow(() ->
            detector.detectPackageNoBegin(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackagePublicPrivateMix() {
        String content = "BeginPackage[\"MyPackage`\"];\nBegin[`Private`];\nPublicFunc[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectPackagePublicPrivateMix(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageProperPublicPrivate() {
        String content = "BeginPackage[\"MyPackage`\"];\nPublicFunc[x_] := helper[x];\nBegin[`Private`];\nhelper[x_] := x * 2;";
        assertDoesNotThrow(() ->
            detector.detectPackagePublicPrivateMix(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageNoUsage() {
        String content = "BeginPackage[\"MyPackage`\"];\nPublicFunc[x_] := x + 1;\nBegin[`Private`];";
        assertDoesNotThrow(() ->
            detector.detectPackageNoUsage(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageWithUsage() {
        String content = "BeginPackage[\"MyPackage`\"];\nPublicFunc::usage = \"Adds 1 to x\";\nPublicFunc[x_] := x + 1;";
        assertDoesNotThrow(() ->
            detector.detectPackageNoUsage(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageCircularDependency() {
        when(inputFile.filename()).thenReturn("MyPackage.wl");
        String content = "BeginPackage[\"MyPackage`\"];\nNeeds[\"MyPackage`Submodule`\"];";

        assertDoesNotThrow(() ->
            detector.detectPackageCircularDependency(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageNoCircularDependency() {
        when(inputFile.filename()).thenReturn("MyPackage.wl");
        String content = "BeginPackage[\"MyPackage`\"];\nNeeds[\"OtherPackage`\"];";

        assertDoesNotThrow(() ->
            detector.detectPackageCircularDependency(context, inputFile, content)
        );
    }

    // ===== PARALLEL FRAMEWORK TESTS =====

    @Test
    void testDetectParallelNoGain() {
        String content = "ParallelTable[i + 1, {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelNoGain(context, inputFile, content)
        );
    }

    @Test
    void testDetectParallelWithHeavyWork() {
        String content = "ParallelTable[Integrate[Sin[i*x], x], {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelNoGain(context, inputFile, content)
        );
    }

    @Test
    void testDetectParallelRaceCondition() {
        String content = "results = {};\nParallelDo[AppendTo[results, i^2], {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelRaceCondition(context, inputFile, content)
        );
    }

    @Test
    void testDetectParallelWithCriticalSection() {
        String content = "results = {};\nParallelDo[CriticalSection[AppendTo[results, i^2]], {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelRaceCondition(context, inputFile, content)
        );
    }

    @Test
    void testDetectParallelSharedState() {
        String content = "SetSharedVariable[counter];\nParallelDo[counter++, {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelSharedState(context, inputFile, content)
        );
    }

    @Test
    void testDetectParallelNoSharedState() {
        String content = "ParallelTable[i^2, {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelSharedState(context, inputFile, content)
        );
    }

    // ===== CLOUD FRAMEWORK TESTS =====

    private static Stream<Arguments> cloudApiMissingAuthTestData() {
        return Stream.of(
            Arguments.of("CloudDeploy[APIFunction[{\"x\" -> \"Integer\"}, #x^2 &]]"),
            Arguments.of("CloudDeploy[APIFunction[{\"x\" -> \"Integer\"}, #x^2 &, Authentication -> \"User\"]]"),
            Arguments.of("CloudDeploy[APIFunction[{\"x\" -> \"Integer\"}, #x^2 &, Permissions -> \"Private\"]]")
        );
    }

    @ParameterizedTest
    @MethodSource("cloudApiMissingAuthTestData")
    void testDetectCloudApiMissingAuth(String content) {
        assertDoesNotThrow(() ->
            detector.detectCloudApiMissingAuth(context, inputFile, content)
        );
    }

    @Test
    void testDetectCloudPermissionsTooOpen() {
        String content = "CloudDeploy[notebook, Permissions -> \"Public\"]";
        assertDoesNotThrow(() ->
            detector.detectCloudPermissionsTooOpen(context, inputFile, content)
        );
    }

    @Test
    void testDetectCloudPermissionsPrivate() {
        String content = "CloudDeploy[notebook, Permissions -> \"Private\"]";
        assertDoesNotThrow(() ->
            detector.detectCloudPermissionsTooOpen(context, inputFile, content)
        );
    }

    @Test
    void testDetectCloudDeployNoValidation() {
        String content = "CloudDeploy[APIFunction[{\"x\"}, #x^2 &]]";
        assertDoesNotThrow(() ->
            detector.detectCloudDeployNoValidation(context, inputFile, content)
        );
    }

    @Test
    void testDetectCloudDeployWithValidation() {
        String content = "CloudDeploy[APIFunction[{\"x\" -> \"Integer\"}, If[IntegerQ[#x], #x^2, $Failed] &]]";
        assertDoesNotThrow(() ->
            detector.detectCloudDeployNoValidation(context, inputFile, content)
        );
    }

    // ===== COMPREHENSIVE TESTS =====

    @Test
    void testAllNotebookMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectNotebookCellSize(context, inputFile, content);
            detector.detectNotebookUnorganized(context, inputFile, content);
            detector.detectNotebookNoSections(context, inputFile, content);
            detector.detectNotebookInitCellMisuse(context, inputFile, content);
        });
    }

    @Test
    void testAllManipulateMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectManipulatePerformance(context, inputFile, content);
            detector.detectDynamicHeavyComputation(context, inputFile, content);
            detector.detectDynamicNoTracking(context, inputFile, content);
            detector.detectManipulateTooComplex(context, inputFile, content);
        });
    }

    @Test
    void testAllPackageMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectPackageNoBegin(context, inputFile, content);
            detector.detectPackagePublicPrivateMix(context, inputFile, content);
            detector.detectPackageNoUsage(context, inputFile, content);
            detector.detectPackageCircularDependency(context, inputFile, content);
        });
    }

    @Test
    void testAllParallelMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectParallelNoGain(context, inputFile, content);
            detector.detectParallelRaceCondition(context, inputFile, content);
            detector.detectParallelSharedState(context, inputFile, content);
        });
    }

    @Test
    void testAllCloudMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectCloudApiMissingAuth(context, inputFile, content);
            detector.detectCloudPermissionsTooOpen(context, inputFile, content);
            detector.detectCloudDeployNoValidation(context, inputFile, content);
        });
    }

    @Test
    void testComplexNotebookWithMultipleIssues() {
        StringBuilder content = new StringBuilder("Notebook[{\n");
        // Large cell
        content.append("Cell[");
        for (int i = 0; i < 60; i++) {
            content.append("line ").append(i).append("\n");
        }
        content.append("],\n");
        // Many cells without sections
        for (int i = 0; i < 15; i++) {
            content.append("Cell[code").append(i).append("],\n");
        }
        // Init cell with heavy computation
        content.append("Cell[InitializationCell -> True, Integrate[f[x], x]]\n");
        content.append("}]");

        assertDoesNotThrow(() -> {
            detector.detectNotebookCellSize(context, inputFile, content.toString());
            detector.detectNotebookNoSections(context, inputFile, content.toString());
            detector.detectNotebookInitCellMisuse(context, inputFile, content.toString());
        });
    }

    @Test
    void testComplexPackageWithIssues() {
        String content = "BeginPackage[\"MyPackage`\"];\n"
                + "PublicFunc[x_] := helper[x];\n"
                + "AnotherFunc[y_] := y * 2;\n"
                + "Begin[`Private`];\n"
                + "MisplacedPublic[z_] := z + 1;\n"
                + "helper[x_] := x * 2;";

        assertDoesNotThrow(() -> {
            detector.detectPackageNoBegin(context, inputFile, content);
            detector.detectPackagePublicPrivateMix(context, inputFile, content);
            detector.detectPackageNoUsage(context, inputFile, content);
        });
    }

    @Test
    void testComplexParallelWithIssues() {
        String content = "sharedList = {};\n"
                + "SetSharedVariable[sharedList];\n"
                + "ParallelTable[AppendTo[sharedList, i + 1], {i, 1, 100}]";

        assertDoesNotThrow(() -> {
            detector.detectParallelNoGain(context, inputFile, content);
            detector.detectParallelRaceCondition(context, inputFile, content);
            detector.detectParallelSharedState(context, inputFile, content);
        });
    }

    @Test
    void testComplexCloudWithIssues() {
        String content = "CloudDeploy[APIFunction[{\"x\"}, #x^2 &, Permissions -> \"Public\"]]";

        assertDoesNotThrow(() -> {
            detector.detectCloudApiMissingAuth(context, inputFile, content);
            detector.detectCloudPermissionsTooOpen(context, inputFile, content);
            detector.detectCloudDeployNoValidation(context, inputFile, content);
        });
    }

    @Test
    void testNonNotebookFile() {
        String content = "f[x_] := x + 1;\ng[y_] := y * 2;";

        assertDoesNotThrow(() -> {
            detector.detectNotebookCellSize(context, inputFile, content);
            detector.detectNotebookUnorganized(context, inputFile, content);
            detector.detectNotebookNoSections(context, inputFile, content);
            detector.detectNotebookInitCellMisuse(context, inputFile, content);
        });
    }

    @Test
    void testNonPackageFile() {
        String content = "f[x_] := x + 1;\ng[y_] := y * 2;";

        assertDoesNotThrow(() -> {
            detector.detectPackageNoBegin(context, inputFile, content);
            detector.detectPackagePublicPrivateMix(context, inputFile, content);
            detector.detectPackageNoUsage(context, inputFile, content);
        });
    }

    @Test
    void testMultipleDynamicIssues() {
        String content = "Dynamic[Integrate[f[x, a, b, c, d, e], x]];\n"
                + "Dynamic[NDSolve[{y'[x] == y[x], y[0] == 1}, y, {x, 0, 10}]];";

        assertDoesNotThrow(() -> {
            detector.detectDynamicHeavyComputation(context, inputFile, content);
            detector.detectDynamicNoTracking(context, inputFile, content);
        });
    }

    @Test
    void testProperFrameworkUsage() {
        String content = "BeginPackage[\"MyPackage`\"];\n"
                + "MyFunc::usage = \"MyFunc[x] adds 1 to x\";\n"
                + "Begin[`Private`];\n"
                + "MyFunc[x_] := x + 1;\n"
                + "End[];\n"
                + "EndPackage[];\n"
                + "Manipulate[Plot[Sin[a*x], {x, 0, 10}], {a, 1, 5}];\n"
                + "Dynamic[counter, TrackedSymbols :> {counter}];\n"
                + "ParallelTable[Integrate[Sin[i*x], x], {i, 1, 100}];\n"
                + "CloudDeploy[APIFunction[{\"x\" -> \"Integer\"}, If[IntegerQ[#x], #x^2, $Failed] &, Authentication -> \"User\"]];";

        assertDoesNotThrow(() -> {
            detector.detectPackageNoBegin(context, inputFile, content);
            detector.detectPackageNoUsage(context, inputFile, content);
            detector.detectManipulatePerformance(context, inputFile, content);
            detector.detectDynamicNoTracking(context, inputFile, content);
            detector.detectParallelNoGain(context, inputFile, content);
            detector.detectCloudApiMissingAuth(context, inputFile, content);
        });
    }
}
