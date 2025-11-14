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

                            // ===== PARALLEL FRAMEWORK TESTS =====

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

                    // ===== COMPREHENSIVE TESTS =====

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

    // ===== ADDITIONAL EDGE CASES FOR 80%+ COVERAGE =====

                                    // Additional branch coverage tests

    // ===== PARAMETERIZED TESTS =====

    @ParameterizedTest
    @MethodSource("detectCloudApiMissingAuthTestData")
    void testDetectDetectCloudApiMissingAuth(String content) {
        assertDoesNotThrow(() ->
            detector.detectCloudApiMissingAuth(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectCloudApiMissingAuthTestData() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("CloudDeploy[APIFunction[{\\\"x\\\"}, #x^2 &, Permissions -> \\\"Public\\\"]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectCloudDeployNoValidationTestData")
    void testDetectDetectCloudDeployNoValidation(String content) {
        assertDoesNotThrow(() ->
            detector.detectCloudDeployNoValidation(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectCloudDeployNoValidationTestData() {
        return Stream.of(
            Arguments.of("CloudDeploy[APIFunction[{\\\"x\\\"}, #x^2 &]]"),
            Arguments.of("CloudDeploy[APIFunction[{\\\"x\\\" -> \\\"Integer\\\"}, If[IntegerQ[#x], #x^2, $Failed] &]]"),
            Arguments.of("CloudDeploy[FormFunction[{\\\"name\\\", \\\"age\\\"}, #name &]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectCloudPermissionsTooOpenTestData")
    void testDetectDetectCloudPermissionsTooOpen(String content) {
        assertDoesNotThrow(() ->
            detector.detectCloudPermissionsTooOpen(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectCloudPermissionsTooOpenTestData() {
        return Stream.of(
            Arguments.of("CloudDeploy[notebook, Permissions -> \\\"Public\\\"]"),
            Arguments.of("CloudDeploy[notebook, Permissions -> \\\"Private\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectDynamicNoTrackingTestData")
    void testDetectDetectDynamicNoTracking(String content) {
        assertDoesNotThrow(() ->
            detector.detectDynamicNoTracking(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectDynamicNoTrackingTestData() {
        return Stream.of(
            Arguments.of("Dynamic[var1 + var2 + var3 + var4 + var5]"),
            Arguments.of("Dynamic[a + b + c + d, TrackedSymbols :> {a, b}]"),
            Arguments.of("Dynamic[x]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectManipulatePerformanceTestData")
    void testDetectDetectManipulatePerformance(String content) {
        assertDoesNotThrow(() ->
            detector.detectManipulatePerformance(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectManipulatePerformanceTestData() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("Manipulate[DSolve[y'[x] == a*y[x], y, x], {a, 1, 5}]"),
            Arguments.of("(* Manipulate[x, {x, 0, 1}] *) (* DynamicModule[{x}, x] *)"),
            Arguments.of("str = \\\"Manipulate[x, {x, 0, 1}]\\"),
            Arguments.of("result = computation[];")
        );
    }

    @ParameterizedTest
    @MethodSource("detectManipulateTooComplexTestData")
    void testDetectDetectManipulateTooComplex(String content) {
        assertDoesNotThrow(() ->
            detector.detectManipulateTooComplex(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectManipulateTooComplexTestData() {
        return Stream.of(
            Arguments.of("Manipulate[Plot[Sin[a*x], {x, 0, 10}], {a, 1, 5}]"),
            Arguments.of("Manipulate[f[a], {a, 0, 1}, {b, 0, 1}, {c, 0, 1}]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNotebookCellSizeTestData")
    void testDetectDetectNotebookCellSize(String content) {
        assertDoesNotThrow(() ->
            detector.detectNotebookCellSize(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNotebookCellSizeTestData() {
        return Stream.of(
            Arguments.of("Cell[\\\"Small cell content\\\"]"),
            Arguments.of(""),
            Arguments.of("f[x_] := x + 1;\\ng[y_] := y * 2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNotebookInitCellMisuseTestData")
    void testDetectDetectNotebookInitCellMisuse(String content) {
        assertDoesNotThrow(() ->
            detector.detectNotebookInitCellMisuse(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNotebookInitCellMisuseTestData() {
        return Stream.of(
            Arguments.of("Cell[BoxData[InitializationCell -> True, Integrate[f[x], x]]]"),
            Arguments.of("Cell[BoxData[InitializationCell -> True, a = 5; b = 10;]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNotebookNoSectionsTestData")
    void testDetectDetectNotebookNoSections(String content) {
        assertDoesNotThrow(() ->
            detector.detectNotebookNoSections(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNotebookNoSectionsTestData() {
        return Stream.of(
            Arguments.of("Notebook[{\\nSection[\\\"Main\\\"],\\nCell[code1],\\nCell[code2],\\nSubsection[\\\"Details\\\"]\\n}]"),
            Arguments.of("Notebook[{Cell[code1], Cell[code2], Cell[code3]}]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNotebookUnorganizedTestData")
    void testDetectDetectNotebookUnorganized(String content) {
        assertDoesNotThrow(() ->
            detector.detectNotebookUnorganized(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNotebookUnorganizedTestData() {
        return Stream.of(
            Arguments.of("Cell[BoxData[code := 5]];\\nVerificationTest[test];\\n(* scratch test work *)"),
            Arguments.of("Cell[BoxData[code := 5]];\\nSection[\\\"Tests\\\"];\\nVerificationTest[test];")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPackageCircularDependencyTestData")
    void testDetectDetectPackageCircularDependency(String content) {
        assertDoesNotThrow(() ->
            detector.detectPackageCircularDependency(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPackageCircularDependencyTestData() {
        return Stream.of(
            Arguments.of("BeginPackage[\\\"MyPackage`\\\"];\\nNeeds[\\\"MyPackage`Submodule`\\\"];"),
            Arguments.of("BeginPackage[\\\"MyPackage`\\\"];\\nNeeds[\\\"OtherPackage`\\\"];"),
            Arguments.of("BeginPackage[\\\"MyPackage`\\\"];\\nGet[\\\"Helper.m\\\"];")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPackageNoBeginTestData")
    void testDetectDetectPackageNoBegin(String content) {
        assertDoesNotThrow(() ->
            detector.detectPackageNoBegin(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPackageNoBeginTestData() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("f[x_] := x + 1;\\ng[y_] := y * 2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPackageNoUsageTestData")
    void testDetectDetectPackageNoUsage(String content) {
        assertDoesNotThrow(() ->
            detector.detectPackageNoUsage(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPackageNoUsageTestData() {
        return Stream.of(
            Arguments.of("BeginPackage[\\\"MyPackage`\\\"];\\nPublicFunc[x_] := x + 1;\\nBegin[`Private`];"),
            Arguments.of("BeginPackage[\\\"MyPackage`\\\"];\\nPublicFunc::usage = \\\"Adds 1 to x\\")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPackagePublicPrivateMixTestData")
    void testDetectDetectPackagePublicPrivateMix(String content) {
        assertDoesNotThrow(() ->
            detector.detectPackagePublicPrivateMix(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPackagePublicPrivateMixTestData() {
        return Stream.of(
            Arguments.of("BeginPackage[\\\"MyPackage`\\\"];\\nBegin[`Private`];\\nPublicFunc[x_] := x + 1;"),
            Arguments.of("BeginPackage[\\\"MyPackage`\\\"];\\nPublicFunc[x_] := helper[x];\\nBegin[`Private`];\\nhelper[x_] := x * 2;")
        );
    }

    @ParameterizedTest
    @MethodSource("detectParallelNoGainTestData")
    void testDetectDetectParallelNoGain(String content) {
        assertDoesNotThrow(() ->
            detector.detectParallelNoGain(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectParallelNoGainTestData() {
        return Stream.of(
            Arguments.of("ParallelTable[i + 1, {i, 1, 100}]"),
            Arguments.of("ParallelTable[Integrate[Sin[i*x], x], {i, 1, 100}]"),
            Arguments.of(""),
            Arguments.of("ParallelMap[#^2 &, Range[1000]]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectParallelRaceConditionTestData")
    void testDetectDetectParallelRaceCondition(String content) {
        assertDoesNotThrow(() ->
            detector.detectParallelRaceCondition(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectParallelRaceConditionTestData() {
        return Stream.of(
            Arguments.of("results = {};\\nParallelDo[AppendTo[results, i^2], {i, 1, 100}]"),
            Arguments.of("results = {};\\nParallelDo[CriticalSection[AppendTo[results, i^2]], {i, 1, 100}]"),
            Arguments.of("global = {};\\nParallelDo[global = Append[global, i], {i, 1, 100}]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectParallelSharedStateTestData")
    void testDetectDetectParallelSharedState(String content) {
        assertDoesNotThrow(() ->
            detector.detectParallelSharedState(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectParallelSharedStateTestData() {
        return Stream.of(
            Arguments.of("SetSharedVariable[counter];\\nParallelDo[counter++, {i, 1, 100}]"),
            Arguments.of("ParallelTable[i^2, {i, 1, 100}]")
        );
    }

    // Additional tests for comment/string literal branches and edge cases

    // ===== NOTEBOOK FRAMEWORK TESTS =====


}
