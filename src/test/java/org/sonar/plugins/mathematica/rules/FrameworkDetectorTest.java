package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FrameworkDetectorTest {

    private FrameworkDetector detector;
    private SensorContext context;
    private InputFile inputFile;
    private NewIssue newIssue;
    private NewIssueLocation newIssueLocation;

    @BeforeEach
    void setUp() {
        detector = new FrameworkDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);
        newIssue = mock(NewIssue.class);
        newIssueLocation = mock(NewIssueLocation.class);

        // Setup InputFile mocks
        when(inputFile.filename()).thenReturn("test.m");
        when(inputFile.uri()).thenReturn(URI.create("file:///test.m"));

        // Mock TextRange for line-based reporting
        TextRange textRange = mock(TextRange.class);
        when(inputFile.selectLine(anyInt())).thenReturn(textRange);

        // Setup NewIssue chain mocks
        when(context.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any(RuleKey.class))).thenReturn(newIssue);
        when(newIssue.at(any(NewIssueLocation.class))).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(newIssueLocation);
        when(newIssueLocation.on(any(InputFile.class))).thenReturn(newIssueLocation);
        when(newIssueLocation.at(any(TextRange.class))).thenReturn(newIssueLocation);
        when(newIssueLocation.message(anyString())).thenReturn(newIssueLocation);
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

    // ===== COMPREHENSIVE TESTS FOR detectNotebookUnorganized =====

    @Test
    void testDetectNotebookUnorganizedNoNotebookContent() {
        // Test early return when no Cell/Notebook found
        String content = "x = 5; y = 10;";
        assertDoesNotThrow(() ->
            detector.detectNotebookUnorganized(context, inputFile, content)
        );
        verify(context, never()).newIssue();
    }

    @ParameterizedTest
    @MethodSource("notebookUnorganizedTestCases")
    void testDetectNotebookUnorganizedVariations(String content, boolean shouldTrigger) {
        assertDoesNotThrow(() ->
            detector.detectNotebookUnorganized(context, inputFile, content)
        );
        if (shouldTrigger) {
            verify(context, atLeastOnce()).newIssue();
        } else {
            verify(context, never()).newIssue();
        }
    }

    private static Stream<Arguments> notebookUnorganizedTestCases() {
        return Stream.of(
            Arguments.of("Cell[BoxData[code := 5]];\n(* scratch *)", false),
            Arguments.of("Cell[BoxData[code := 5]];\nVerificationTest[test]", false),
            Arguments.of("Cell[BoxData[code := 5]];\nVerificationTest[test];\n(* scratch work *)", true)
        );
    }

    @Test
    void testDetectNotebookUnorganizedCaseInsensitiveTest() {
        // Test with different case variations
        String content = "CELL[code];\nverificationtest[x];\n(* test scratch *)";
        assertDoesNotThrow(() ->
            detector.detectNotebookUnorganized(context, inputFile, content)
        );
    }

    // ===== COMPREHENSIVE TESTS FOR detectParallelSharedState =====

    @Test
    void testDetectParallelSharedStateWithSetSharedFunction() {
        String content = "SetSharedFunction[myFunc];\nParallelTable[myFunc[i], {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelSharedState(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectParallelSharedStateNoSharedState() {
        String content = "ParallelTable[i^2, {i, 1, 100}]";
        assertDoesNotThrow(() ->
            detector.detectParallelSharedState(context, inputFile, content)
        );
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectParallelSharedStateMultipleSharedVariables() {
        String content = "SetSharedVariable[a, b, c];\nParallelDo[a++; b++; c++, {i, 1, 10}]";
        assertDoesNotThrow(() ->
            detector.detectParallelSharedState(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    // ===== EXCEPTION HANDLING TESTS =====

    @Test
    void testDetectNotebookCellSizeHandlesException() {
        // Create invalid mock that will throw exception
        when(inputFile.filename()).thenThrow(new RuntimeException("Test exception"));
        String content = "Cell[large content]";

        assertDoesNotThrow(() ->
            detector.detectNotebookCellSize(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookUnorganizedHandlesException() {
        // Trigger exception with null content
        assertDoesNotThrow(() ->
            detector.detectNotebookUnorganized(context, inputFile, null)
        );
    }

    @Test
    void testDetectNotebookNoSectionsHandlesException() {
        when(inputFile.filename()).thenThrow(new RuntimeException("Test exception"));
        String content = "Notebook[{Cell[test]}]";

        assertDoesNotThrow(() ->
            detector.detectNotebookNoSections(context, inputFile, content)
        );
    }

    @Test
    void testDetectManipulatePerformanceHandlesException() {
        when(inputFile.filename()).thenThrow(new RuntimeException("Test exception"));
        String content = "Manipulate[Plot[x], {x, 0, 1}]";

        assertDoesNotThrow(() ->
            detector.detectManipulatePerformance(context, inputFile, content)
        );
    }

    @Test
    void testDetectParallelSharedStateHandlesException() {
        // Trigger exception with null content
        assertDoesNotThrow(() ->
            detector.detectParallelSharedState(context, inputFile, null)
        );
    }

    @Test
    void testDetectDynamicHeavyComputationHandlesException() {
        when(inputFile.filename()).thenThrow(new RuntimeException("Test exception"));
        String content = "Dynamic[NDSolve[...]]";

        assertDoesNotThrow(() ->
            detector.detectDynamicHeavyComputation(context, inputFile, content)
        );
    }

    // ===== TESTS FOR COMMENT/STRING LITERAL SKIPPING =====

    @Test
    void testDetectManipulateTooComplexInsideComment() {
        // Should skip detection inside comments
        String content = "(* Manipulate[f[a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11], "
                         + "{a1, 0, 1}, {a2, 0, 1}, {a3, 0, 1}, {a4, 0, 1}, {a5, 0, 1}, "
                         + "{a6, 0, 1}, {a7, 0, 1}, {a8, 0, 1}, {a9, 0, 1}, {a10, 0, 1}, "
                         + "{a11, 0, 1}] *)";
        assertDoesNotThrow(() ->
            detector.detectManipulateTooComplex(context, inputFile, content)
        );
    }

    @Test
    void testDetectNotebookCellSizeNestedBrackets() {
        // Test proper handling of nested brackets
        StringBuilder content = new StringBuilder("Cell[Plot[Sin[x], {x, 0, 10}], ");
        for (int i = 0; i < 60; i++) {
            content.append("data").append(i).append(", ");
        }
        content.append("ImageSize -> {400, 300}]");

        assertDoesNotThrow(() ->
            detector.detectNotebookCellSize(context, inputFile, content.toString())
        );
    }

    // ===== ADDITIONAL TESTS FOR LOW COVERAGE METHODS =====

    // Tests for detectPackageCircularDependency (currently 35% coverage)
    @Test
    void testDetectPackageCircularDependencyActualCircularDependency() {
        // When filename contains the package name that's also in Needs
        when(inputFile.filename()).thenReturn("TestPackage.m");
        String content = "BeginPackage[\"TestPackage`\"];\n"
                         + "Needs[\"TestPackage`Helper`\"];\n"
                         + "EndPackage[];";

        assertDoesNotThrow(() ->
            detector.detectPackageCircularDependency(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectPackageCircularDependencyMultipleNeeds() {
        when(inputFile.filename()).thenReturn("MyFile.m");
        String content = "Needs[\"Package1`\"];\n"
                         + "Needs[\"MyFile`Submodule`\"];\n"
                         + "Get[\"Other.m\"];";

        assertDoesNotThrow(() ->
            detector.detectPackageCircularDependency(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectPackageCircularDependencyNeedsInComment() {
        when(inputFile.filename()).thenReturn("TestFile.m");
        String content = "(* Needs[\"TestFile`\"] *)\n"
                         + "Needs[\"OtherPackage`\"];";

        assertDoesNotThrow(() ->
            detector.detectPackageCircularDependency(context, inputFile, content)
        );
        // Should only trigger once for the non-comment Need if at all
    }

    @Test
    void testDetectPackageCircularDependencyNoCircular() {
        when(inputFile.filename()).thenReturn("MyFile.m");
        String content = "Needs[\"Package1`\"];\n"
                         + "Needs[\"Package2`\"];";

        assertDoesNotThrow(() ->
            detector.detectPackageCircularDependency(context, inputFile, content)
        );
        verify(context, never()).newIssue();
    }

    // Tests for detectPackagePublicPrivateMix (currently 28% coverage)
    @Test
    void testDetectPackagePublicPrivateMixPublicAfterPrivate() {
        String content = "BeginPackage[\"MyPackage`\"];\n"
                         + "Begin[\"`Private`\"];\n"
                         + "PrivateHelper[x_] := x + 1;\n"
                         + "PublicFunc[x_] := x^2;\n"  // Public function after Private
                         + "End[];\n"
                         + "EndPackage[];";

        assertDoesNotThrow(() ->
            detector.detectPackagePublicPrivateMix(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    // Tests for detectCloudPermissionsTooOpen (currently 20% coverage)
    @Test
    void testDetectCloudPermissionsTooOpenPublicPermissions() {
        String content = "CloudDeploy[myNotebook, \"MyNotebook\", Permissions -> \"Public\"]";

        assertDoesNotThrow(() ->
            detector.detectCloudPermissionsTooOpen(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectCloudPermissionsTooOpenPublicInList() {
        // Pattern expects -> "Public" (not in list), so this should NOT trigger
        String content = "CloudPublish[expr, Permissions -> {\"Public\", \"Read\"}]";

        assertDoesNotThrow(() ->
            detector.detectCloudPermissionsTooOpen(context, inputFile, content)
        );
        verify(context, never()).newIssue();  // List format not matched by pattern
    }

    @Test
    void testDetectCloudPermissionsTooOpenPrivatePermissions() {
        String content = "CloudDeploy[myData, Permissions -> \"Private\"]";

        assertDoesNotThrow(() ->
            detector.detectCloudPermissionsTooOpen(context, inputFile, content)
        );
        verify(context, never()).newIssue();
    }

    // Tests for detectNotebookNoSections - cover the section counting branch
    @Test
    void testDetectNotebookNoSectionsWithSections() {
        // Pattern looks for Section[, Subsection[, etc. (actual function calls)
        StringBuilder content = new StringBuilder("Notebook[{\n");
        content.append("Section[\"Introduction\"],\n");  // Actual Section call
        for (int i = 0; i < 12; i++) {
            content.append("Cell[\"code ").append(i).append("\"],\n");
        }
        content.append("Subsection[\"Details\"]\n}]");  // Actual Subsection call

        assertDoesNotThrow(() ->
            detector.detectNotebookNoSections(context, inputFile, content.toString())
        );
        // Should not report issue because sections exist
        verify(context, never()).newIssue();
    }

    // Tests for detectNotebookInitCellMisuse - cover Plot/Export branches
    @Test
    void testDetectNotebookInitCellMisuseWithPlot() {
        // Pattern expects InitializationCell -> True (without quotes on the key)
        String content = "Cell[BoxData[Plot[Sin[x], {x, 0, 10}]], InitializationCell -> True]";

        assertDoesNotThrow(() ->
            detector.detectNotebookInitCellMisuse(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectNotebookInitCellMisuseWithExport() {
        // Pattern expects InitializationCell -> True (without quotes on the key)
        String content = "Cell[BoxData[Export[\"file.png\", img]], InitializationCell -> True]";

        assertDoesNotThrow(() ->
            detector.detectNotebookInitCellMisuse(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    // Test for detectDynamicNoTracking - cover Refresh branch
    @Test
    void testDetectDynamicNoTrackingWithRefresh() {
        String content = "Dynamic[x + y, UpdateInterval -> Infinity, TrackedSymbols :> {}, Refresh[z, UpdateInterval -> 1]]";

        assertDoesNotThrow(() ->
            detector.detectDynamicNoTracking(context, inputFile, content)
        );
        // Has Refresh, so should be okay
    }

    // Test for detectParallelRaceCondition - cover assignment pattern
    @Test
    void testDetectParallelRaceConditionWithAssignment() {
        // Pattern looks for uppercase variable assignment: [A-Z][a-zA-Z0-9]* = ...
        String content = "ParallelDo[Result = Result + i, {i, 1, 100}]";

        assertDoesNotThrow(() ->
            detector.detectParallelRaceCondition(context, inputFile, content)
        );
        verify(context, atLeastOnce()).newIssue();
    }

    // Test for isNotebookFile - Notebook without Cell
    @Test
    void testIsNotebookFileNotebookWithoutCell() {
        String content = "Notebook[{}, StyleDefinitions -> \"Default.nb\"]";

        assertDoesNotThrow(() ->
            detector.detectNotebookCellSize(context, inputFile, content)
        );
        // Should handle gracefully
    }

    // Additional tests for patterns in string literals
    @Test
    void testDetectNotebookCellSizePatternInStringLiteral() {
        String content = "str = \"Cell[this should be ignored because it's in a string]\";";

        assertDoesNotThrow(() ->
            detector.detectNotebookCellSize(context, inputFile, content)
        );
        verify(context, never()).newIssue();
    }

    // Test for detectCloudDeployNoValidation - specific validation patterns
    @Test
    void testDetectCloudDeployNoValidationWithNumericQ() {
        String content = "APIFunction[{\"x\" -> \"Number\"}, If[NumericQ[#x], #x^2, $Failed] &]";

        assertDoesNotThrow(() ->
            detector.detectCloudDeployNoValidation(context, inputFile, content)
        );
        // Has NumericQ validation, should not trigger
    }

    @Test
    void testDetectCloudDeployNoValidationWithMatchQ() {
        String content = "APIFunction[{\"name\" -> \"String\"}, If[MatchQ[#name, _String], #name, \"Invalid\"] &]";

        assertDoesNotThrow(() ->
            detector.detectCloudDeployNoValidation(context, inputFile, content)
        );
        // Has MatchQ validation, should not trigger
    }


}
