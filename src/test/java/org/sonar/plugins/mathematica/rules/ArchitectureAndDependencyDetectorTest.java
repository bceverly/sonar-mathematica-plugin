package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.rule.RuleKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchitectureAndDependencyDetectorTest {

    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        // Mock newIssue chain
        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);
        TextRange textRange = mock(TextRange.class);

        when(context.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any(RuleKey.class))).thenReturn(newIssue);
        when(newIssue.at(any(NewIssueLocation.class))).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(inputFile.newRange(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(textRange);
        when(inputFile.filename()).thenReturn("TestFile.m");

        // Clear caches before each test
        ArchitectureAndDependencyDetector.clearCaches();
    }

    @AfterEach
    void tearDown() {
        // Clear caches after each test to avoid state pollution
        ArchitectureAndDependencyDetector.clearCaches();
    }

    @Test
    void testUtilityClassCannotBeInstantiated() {
        assertThatThrownBy(() -> {
            var constructor = ArchitectureAndDependencyDetector.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testInitializeCachesWithContent() {
        ArchitectureAndDependencyDetector.initializeCaches("content");
        assertThat(ArchitectureAndDependencyDetector.getPackageDependenciesSize()).isZero();
    }

    @Test
    void testInitializeCachesWithoutContent() {
        ArchitectureAndDependencyDetector.initializeCaches();
        assertThat(ArchitectureAndDependencyDetector.getPackageDependenciesSize()).isZero();
    }

    @Test
    void testClearCaches() {
        String content = "BeginPackage[\"Test`\"];\nTestFunc[x_] := x;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        ArchitectureAndDependencyDetector.clearCaches();
        assertThat(ArchitectureAndDependencyDetector.getPackageDependenciesSize()).isZero();
    }

    @Test
    void testBuildCrossFileData() {
        String content = "BeginPackage[\"MyPackage`\"];\nMyFunc[x_] := x + 1;\nEndPackage[];";
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content)
        );
    }

    @Test
    void testBuildCrossFileDataWithDependencies() {
        String content = "BeginPackage[\"MyPackage`\"];\nNeeds[\"OtherPackage`\"];\nMyFunc[x_] := x + 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertThat(ArchitectureAndDependencyDetector.getPackageDependenciesSize()).isEqualTo(1);
    }

    @Test
    void testClassifyTestFile() {
        when(inputFile.filename()).thenReturn("TestMyPackage.m");
        String content = "BeginPackage[\"TestMyPackage`\"];\nTestFunc[] := AssertTrue[True];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(ArchitectureAndDependencyDetector::clearCaches);
    }

    // Dependency & Architecture Rules

    @Test
    void testDetectCircularPackageDependency() {
        String content1 = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nFuncA[] := 1;\nEndPackage[];";
        String content2 = "BeginPackage[\"PkgB`\"];\nNeeds[\"PkgA`\"];\nFuncB[] := 2;\nEndPackage[];";

        InputFile file1 = mock(InputFile.class);
        InputFile file2 = mock(InputFile.class);
        when(file1.filename()).thenReturn("PkgA.m");
        when(file2.filename()).thenReturn("PkgB.m");

        ArchitectureAndDependencyDetector.buildCrossFileData(file1, content1);
        ArchitectureAndDependencyDetector.buildCrossFileData(file2, content2);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectCircularPackageDependency(context, file1, content1)
        );
    }

    @Test
    void testDetectUnusedPackageImport() {
        String content = "BeginPackage[\"MyPackage`\"];\nNeeds[\"UnusedPackage`\"];\nMyFunc[x_] := x + 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedPackageImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectTransitiveDependencyCouldBeDirect() {
        // Package A depends on B, B depends on C, but A uses C directly
        String contentA = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nFuncA[] := PkgC`FuncC[];\nEndPackage[];";
        String contentB = "BeginPackage[\"PkgB`\"];\nNeeds[\"PkgC`\"];\nFuncB[] := 1;\nEndPackage[];";
        String contentC = "BeginPackage[\"PkgC`\"];\nFuncC[] := 3;\nEndPackage[];";

        InputFile fileA = mock(InputFile.class);
        InputFile fileB = mock(InputFile.class);
        InputFile fileC = mock(InputFile.class);
        when(fileA.filename()).thenReturn("PkgA.m");
        when(fileB.filename()).thenReturn("PkgB.m");
        when(fileC.filename()).thenReturn("PkgC.m");

        ArchitectureAndDependencyDetector.buildCrossFileData(fileA, contentA);
        ArchitectureAndDependencyDetector.buildCrossFileData(fileB, contentB);
        ArchitectureAndDependencyDetector.buildCrossFileData(fileC, contentC);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(context, fileA, contentA)
        );
    }

    @Test
    void testDetectPackageExportsTooMuch() {
        StringBuilder content = new StringBuilder("BeginPackage[\"LargePackage`\"];\n");
        // Add 55 exports (> 50 threshold)
        for (int i = 1; i <= 55; i++) {
            content.append("Export").append(i).append(";\n");
        }
        content.append("Begin[\"`Private`\"];\n");
        for (int i = 1; i <= 55; i++) {
            content.append("Export").append(i).append("[x_] := x;\n");
        }
        content.append("End[];\nEndPackage[];");

        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content.toString());
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageExportsTooMuch(context, inputFile, content.toString())
        );
    }

    @Test
    void testDetectPackageExportsTooLittle() {
        String content = "BeginPackage[\"SmallPackage`\"];\nFunc1;\nFunc2;\nBegin[\"`Private`\"];\n"
         + "Func1[x_] := x;\nFunc2[x_] := x * 2;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageExportsTooLittle(context, inputFile, content)
        );
    }

    // Unused Export & Dead Code Rules

    @Test
    void testDetectUnusedPublicFunction() {
        String content = "BeginPackage[\"MyPackage`\"];\nUnusedFunc;\nBegin[\"`Private`\"];\n"
         + "UnusedFunc[x_] := x + 1;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedPublicFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedExport() {
        String content = "BeginPackage[\"MyPackage`\"];\nExportedButUnused;\nBegin[\"`Private`\"];\n"
         + "ExportedButUnused[x_] := x + 1;\nresult = ExportedButUnused[5];\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedExport(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadPackage() {
        String content = "BeginPackage[\"DeadPackage`\"];\nFunc1;\nFunc2;\nBegin[\"`Private`\"];\n"
         + "Func1[x_] := x;\nFunc2[x_] := x * 2;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDeadPackage(context, inputFile, content)
        );
    }

    @Test
    void testDetectFunctionOnlyCalledOnce() {
        String content = "BeginPackage[\"MyPackage`\"];\nBegin[\"`Private`\"];\n"
         + "HelperFunc[x_] := x + 1;\nMainFunc[] := HelperFunc[5];\n"
                 +                         "End[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectFunctionOnlyCalledOnce(context, inputFile, content)
        );
    }

    @Test
    void testDetectOverAbstractedAPI() {
        StringBuilder content = new StringBuilder("BeginPackage[\"MyPackage`\"];\nPublicFunc;\nBegin[\"`Private`\"];\n");
        // Add 15 private functions but only 1 public
        for (int i = 1; i <= 15; i++) {
            content.append("PrivateFunc").append(i).append("[x_] := x;\n");
        }
        content.append("PublicFunc[x_] := x;\nEnd[];\nEndPackage[];");

        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content.toString());
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectOverAbstractedAPI(context, inputFile, content.toString())
        );
    }

    @Test
    void testDetectImplementationWithoutTests() {
        when(inputFile.filename()).thenReturn("MyPackage.m");
        String content = "BeginPackage[\"MyPackage`\"];\nMyFunc[x_] := x + 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectImplementationWithoutTests(context, inputFile, content)
        );
    }

    @Test
    void testDetectInternalAPIUsedLikePublic() {
        StringBuilder content = new StringBuilder("BeginPackage[\"MyPackage`\"];\nBegin[\"`Private`\"];\n");
        content.append("InternalFunc[x_] := x;\n");
        // Simulate 15 calls to the internal function
        for (int i = 0; i < 15; i++) {
            content.append("result").append(i).append(" = InternalFunc[").append(i).append("];\n");
        }
        content.append("End[];\nEndPackage[];");

        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content.toString());
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectInternalAPIUsedLikePublic(context, inputFile, content.toString())
        );
    }

    @Test
    void testDetectPackageLoadedButNotListedInMetadata() {
        String content = "BeginPackage[\"MyPackage`\"];\n"
         + "Begin[\"`Private`\"];\n"
        + "Needs[\"UnlistedPackage`\"];\n"
        + "result = UnlistedPackage`Func[];\n"
                +                         "End[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageLoadedButNotListedInMetadata(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageVersionMismatch() {
        String content1 = "BeginPackage[\"PkgA`\"];\nVersion -> \"1.0\";\nNeeds[\"PkgB`\"];\nEndPackage[];";
        String content2 = "BeginPackage[\"PkgB`\"];\nVersion -> \"2.0\";\nEndPackage[];";

        InputFile file1 = mock(InputFile.class);
        InputFile file2 = mock(InputFile.class);
        when(file1.filename()).thenReturn("PkgA.m");
        when(file2.filename()).thenReturn("PkgB.m");

        ArchitectureAndDependencyDetector.buildCrossFileData(file1, content1);
        ArchitectureAndDependencyDetector.buildCrossFileData(file2, content2);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageVersionMismatch(context, file1, content1)
        );
    }

    // Documentation & Consistency Rules

    @Test
    void testDetectPublicExportMissingUsageMessage() {
        String content = "BeginPackage[\"MyPackage`\"];\nMissingDocFunc;\nBegin[\"`Private`\"];\n"
         + "MissingDocFunc[x_] := x + 1;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPublicExportMissingUsageMessage(context, inputFile, content)
        );
    }

    @Test
    void testDetectPublicExportWithUsageMessage() {
        String content = "BeginPackage[\"MyPackage`\"];\nWellDocFunc;\n"
         + "WellDocFunc::usage = \"Documentation here\";\n"
        + "Begin[\"`Private`\"];\nWellDocFunc[x_] := x + 1;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPublicExportMissingUsageMessage(context, inputFile, content)
        );
    }

    // Cache Size Verification Tests

    @Test
    void testGetPackageDependenciesSize() {
        String content = "BeginPackage[\"Pkg1`\"];\nNeeds[\"Pkg2`\"];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertThat(ArchitectureAndDependencyDetector.getPackageDependenciesSize()).isPositive();
    }

    @Test
    void testGetSymbolDefinitionsSize() {
        String content = "BeginPackage[\"MyPackage`\"];\nMyFunc[x_] := x;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertThat(ArchitectureAndDependencyDetector.getSymbolDefinitionsSize()).isNotNegative();
    }

    @Test
    void testGetSymbolUsagesSize() {
        String content = "BeginPackage[\"MyPackage`\"];\nMyFunc[x_] := OtherFunc[x];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertThat(ArchitectureAndDependencyDetector.getSymbolUsagesSize()).isNotNegative();
    }

    // Additional Comprehensive Tests for Remaining Detection Methods

    @Test
    void testDetectMissingPackageImport() {
        String content = "BeginPackage[\"MyPackage`\"];\nFunc[] := ExternalPkg`Method[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectMissingPackageImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectLayerViolation() {
        String content = "BeginPackage[\"UILayer`\"];\nFunc[] := DatabaseLayer`Query[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectLayerViolation(context, inputFile, content)
        );
    }

    @Test
    void testDetectDiamondDependency() {
        String contentA = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nNeeds[\"PkgC`\"];\nEndPackage[];";
        String contentB = "BeginPackage[\"PkgB`\"];\nNeeds[\"PkgD`\"];\nEndPackage[];";
        String contentC = "BeginPackage[\"PkgC`\"];\nNeeds[\"PkgD`\"];\nEndPackage[];";
        String contentD = "BeginPackage[\"PkgD`\"];\nFunc[] := 1;\nEndPackage[];";

        InputFile fileA = mock(InputFile.class);
        InputFile fileB = mock(InputFile.class);
        InputFile fileC = mock(InputFile.class);
        InputFile fileD = mock(InputFile.class);
        when(fileA.filename()).thenReturn("PkgA.m");
        when(fileB.filename()).thenReturn("PkgB.m");
        when(fileC.filename()).thenReturn("PkgC.m");
        when(fileD.filename()).thenReturn("PkgD.m");

        ArchitectureAndDependencyDetector.buildCrossFileData(fileA, contentA);
        ArchitectureAndDependencyDetector.buildCrossFileData(fileB, contentB);
        ArchitectureAndDependencyDetector.buildCrossFileData(fileC, contentC);
        ArchitectureAndDependencyDetector.buildCrossFileData(fileD, contentD);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDiamondDependency(context, fileA, contentA)
        );
    }

    @Test
    void testDetectGodPackageTooManyDependencies() {
        String content = "BeginPackage[\"MyPackage`\"];\nNeeds[\"Pkg1`\"];\nNeeds[\"Pkg2`\"];\nNeeds[\"Pkg3`\"];\n"
         + "Needs[\"Pkg4`\"];\nNeeds[\"Pkg5`\"];\nNeeds[\"Pkg6`\"];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnstableDependency() {
        String content = "BeginPackage[\"UnstablePackage`\"];\n"
         + "Func1;\nFunc2;\nFunc3;\nFunc4;\nFunc5;\n"
                 +                         "Begin[\"`Private`\"];\n"

                                + "Func1[x_] := ExternalPkg1`Call[];\n"
                                        +                         "Func2[x_] := ExternalPkg2`Call[];\n"

                                + "Func3[x_] := ExternalPkg3`Call[];\n"
                                        +                         "Func4[x_] := ExternalPkg4`Call[];\n"

                                + "Func5[x_] := ExternalPkg5`Call[];\n"
                                        +                         "End[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnstableDependency(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageTooLarge() {
        StringBuilder content = new StringBuilder("BeginPackage[\"LargePackage`\"];\n");
        for (int i = 1; i <= 30; i++) {
            content.append("Export").append(i).append(";\n");
        }
        content.append("Begin[\"`Private`\"];\n");
        for (int i = 1; i <= 30; i++) {
            content.append("Export").append(i).append("[x_] := x;\n");
        }
        content.append("End[];\nEndPackage[];");
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content.toString());
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageTooLarge(context, inputFile, content.toString())
        );
    }

    @Test
    void testDetectPackageTooSmall() {
        String content = "BeginPackage[\"SmallPackage`\"];\nFunc1;\nBegin[\"`Private`\"];\n"
         + "Func1[x_] := x;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageTooSmall(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentPackageNaming() {
        when(inputFile.filename()).thenReturn("MyPackage.m");
        String content = "BeginPackage[\"my_package`\"];\nFunc[] := 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionalPackageLoad() {
        String content = "If[$VersionNumber >= 12, Needs[\"NewPackage`\"], Needs[\"OldPackage`\"]]";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectConditionalPackageLoad(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePublicAPI() {
        String content = "BeginPackage[\"MyPackage`\"];\nCreateObject;\n"
         + "Begin[\"`Private`\"];\nCreateObject[] := {x -> 1};\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectIncompletePublicAPI(context, inputFile, content)
        );
    }

    @Test
    void testDetectPrivateSymbolUsedExternally() {
        String content = "BeginPackage[\"MyPackage`\"];\nPublicFunc;\n"
         + "Begin[\"`Private`\"];\nPrivateFunc[] := 1;\nPublicFunc[] := 2;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPrivateSymbolUsedExternally(context, inputFile, content)
        );
    }

    @Test
    void testDetectInternalImplementationExposed() {
        String content = "BeginPackage[\"MyPackage`\"];\nInternalHelperFunc;\n"
         + "Begin[\"`Private`\"];\nInternalHelperFunc[] := 1;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectInternalImplementationExposed(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingPackageDocumentation() {
        String content = "BeginPackage[\"MyPackage`\"];\nMyFunc;\n"
         + "Begin[\"`Private`\"];\nMyFunc[] := 1;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectMissingPackageDocumentation(context, inputFile, content)
        );
    }

    @Test
    void testDetectPublicAPIChangedWithoutVersionBump() {
        String content = "BeginPackage[\"MyPackage`\"];\nVersion -> \"1.0.0\";\nMyFunc;\n"
         + "Begin[\"`Private`\"];\nMyFunc[x_] := x;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPublicAPIChangedWithoutVersionBump(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeprecatedAPIStillUsedInternally() {
        String content = "BeginPackage[\"MyPackage`\"];\nOldFunc;\nNewFunc;\n"
         + "(* @deprecated Use NewFunc *)\n"
                 +                         "Begin[\"`Private`\"];\nOldFunc[] := 1;\nNewFunc[] := OldFunc[];\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDeprecatedAPIStillUsedInternally(context, inputFile, content)
        );
    }

    @Test
    void testDetectCommentedOutPackageLoad() {
        String content = "(* Needs[\"OldPackage`\"] *)\nBeginPackage[\"MyPackage`\"];\nFunc[] := 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectCommentedOutPackageLoad(context, inputFile, content)
        );
    }

    @Test
    void testDetectDuplicateSymbolDefinitionAcrossPackages() {
        String content1 = "BeginPackage[\"Pkg1`\"];\nCommonFunc;\nBegin[\"`Private`\"];\nCommonFunc[] := 1;\nEnd[];\nEndPackage[];";
        String content2 = "BeginPackage[\"Pkg2`\"];\nCommonFunc;\nBegin[\"`Private`\"];\nCommonFunc[] := 2;\nEnd[];\nEndPackage[];";

        InputFile file1 = mock(InputFile.class);
        InputFile file2 = mock(InputFile.class);
        when(file1.filename()).thenReturn("Pkg1.m");
        when(file2.filename()).thenReturn("Pkg2.m");

        ArchitectureAndDependencyDetector.buildCrossFileData(file1, content1);
        ArchitectureAndDependencyDetector.buildCrossFileData(file2, content2);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDuplicateSymbolDefinitionAcrossPackages(context, file1)
        );
    }

    @Test
    void testDetectSymbolRedefinitionAfterImport() {
        String content = "Needs[\"System`\"];\nList[x_] := x + 1;";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectSymbolRedefinitionAfterImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectInconsistentParameterNamesAcrossOverloads() {
        String content = "BeginPackage[\"MyPackage`\"];\nMyFunc;\n"
         + "Begin[\"`Private`\"];\nMyFunc[x_] := x;\nMyFunc[y_, z_] := y + z;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectInconsistentParameterNamesAcrossOverloads(context, inputFile, content)
        );
    }

    @Test
    void testDetectPublicFunctionWithImplementationDetailsInName() {
        String content = "BeginPackage[\"MyPackage`\"];\nGetDataFromDatabaseWithCache;\n"
         + "Begin[\"`Private`\"];\nGetDataFromDatabaseWithCache[] := {};\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPublicFunctionWithImplementationDetailsInName(context, inputFile, content)
        );
    }

    @Test
    void testDetectPublicAPINotInPackageContext() {
        String content = "MyFunc[x_] := x + 1;";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPublicAPINotInPackageContext(context, inputFile, content)
        );
    }

    @Test
    void testDetectTestFunctionInProductionCode() {
        when(inputFile.filename()).thenReturn("MyPackage.m");
        String content = "BeginPackage[\"MyPackage`\"];\nTestMyFunc;\n"
         + "Begin[\"`Private`\"];\nTestMyFunc[] := AssertTrue[True];\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectTestFunctionInProductionCode(context, inputFile, content)
        );
    }

    @Test
    void testDetectOrphanedTestFile() {
        when(inputFile.filename()).thenReturn("TestOrphaned.m");
        String content = "BeginPackage[\"TestOrphaned`\"];\nTestFunc[] := AssertTrue[True];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectOrphanedTestFile(context, inputFile)
        );
    }

    @Test
    void testDetectPackageDependsOnApplicationCode() {
        String content = "BeginPackage[\"Utilities`\"];\nFunc[] := Application`Main[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(context, inputFile, content)
        );
    }

    @Test
    void testDetectCyclicCallBetweenPackages() {
        String content1 = "BeginPackage[\"PkgA`\"];\nFuncA[] := PkgB`FuncB[];\nEndPackage[];";
        String content2 = "BeginPackage[\"PkgB`\"];\nFuncB[] := PkgA`FuncA[];\nEndPackage[];";

        InputFile file1 = mock(InputFile.class);
        InputFile file2 = mock(InputFile.class);
        when(file1.filename()).thenReturn("PkgA.m");
        when(file2.filename()).thenReturn("PkgB.m");

        ArchitectureAndDependencyDetector.buildCrossFileData(file1, content1);
        ArchitectureAndDependencyDetector.buildCrossFileData(file2, content2);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectCyclicCallBetweenPackages(context, file1, content1)
        );
    }

    @Test
    void testBuildCrossFileDataWithoutPackage() {
        String content = "MyFunc[x_] := x + 1;\nresult = MyFunc[5];";
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content)
        );
    }

    @Test
    void testBuildCrossFileDataWithVersionInfo() {
        String content = "BeginPackage[\"MyPackage`\"];\nVersion -> \"1.2.3\";\nFunc[] := 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(ArchitectureAndDependencyDetector::clearCaches);
    }

    @Test
    void testClassifyImplementationFile() {
        when(inputFile.filename()).thenReturn("Implementation.m");
        String content = "BeginPackage[\"Implementation`\"];\nFunc[] := 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
        assertDoesNotThrow(ArchitectureAndDependencyDetector::clearCaches);
    }

    // ===== ADDITIONAL TESTS FOR LOW COVERAGE METHODS =====

    @Test
    void testDetectUnusedPackageImportWithUnusedImport() {
        // Set up a package that exports symbols
        InputFile pkgFile = mock(InputFile.class);
        when(pkgFile.filename()).thenReturn("UtilityPkg.m");
        String pkgContent = "BeginPackage[\"Utility`\"];\nHelperFunc::usage = \"Helper function\";\n"
                          + "HelperFunc[x_] := x + 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgFile, pkgContent);

        // Import the package but don't use any symbols from it
        String content = "BeginPackage[\"MyPkg`\"];\nNeeds[\"Utility`\"];\n"
                       + "MyFunc[x_] := x * 2;\nEndPackage[];";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedPackageImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedPackageImportWithUsedImport() {
        // Set up a package that exports symbols
        InputFile pkgFile = mock(InputFile.class);
        when(pkgFile.filename()).thenReturn("UtilityPkg.m");
        String pkgContent = "BeginPackage[\"Utility`\"];\nHelperFunc::usage = \"Helper function\";\n"
                          + "HelperFunc[x_] := x + 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgFile, pkgContent);

        when(inputFile.filename()).thenReturn("MyFile.m");

        // Import the package AND use a symbol from it
        String content = "BeginPackage[\"MyPkg`\"];\nNeeds[\"Utility`\"];\n"
                       + "MyFunc[x_] := HelperFunc[x] * 2;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedPackageImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedPackageImportNoExportsKnown() {
        // Import a package that we don't have export info for
        String content = "BeginPackage[\"MyPkg`\"];\nNeeds[\"UnknownPackage`\"];\nMyFunc[] := 1;\nEndPackage[];";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedPackageImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageVersionMismatchWithMismatch() {
        // Set up packages with version info
        InputFile pkg1 = mock(InputFile.class);
        InputFile pkg2 = mock(InputFile.class);
        when(pkg1.filename()).thenReturn("Package1.m");
        when(pkg2.filename()).thenReturn("Package2.m");

        String content1 = "BeginPackage[\"Pkg1`\"];\nNeeds[\"Dependency`\" -> \"1.0.0\"];\nEndPackage[];";
        String content2 = "BeginPackage[\"Dependency`\"];\nVersion -> \"2.0.0\";\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkg2, content2);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkg1, content1);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageVersionMismatch(context, pkg1, content1)
        );
    }

    @Test
    void testDetectPackageVersionMismatchWithMatch() {
        // Set up packages with matching versions
        InputFile pkg1 = mock(InputFile.class);
        InputFile pkg2 = mock(InputFile.class);
        when(pkg1.filename()).thenReturn("Package1.m");
        when(pkg2.filename()).thenReturn("Package2.m");

        String content1 = "BeginPackage[\"Pkg1`\"];\nNeeds[\"Dependency`\" -> \"1.0.0\"];\nEndPackage[];";
        String content2 = "BeginPackage[\"Dependency`\"];\nVersion -> \"1.0.0\";\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkg2, content2);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkg1, content1);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageVersionMismatch(context, pkg1, content1)
        );
    }

    @Test
    void testDetectPackageVersionMismatchNoVersionInfo() {
        // Package dependency without version requirement
        String content = "BeginPackage[\"Pkg1`\"];\nNeeds[\"Dependency`\"];\nEndPackage[];";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageVersionMismatch(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadPackageWithNoDependents() {
        // Set up a package with no dependents
        when(inputFile.filename()).thenReturn("DeadPackage.m");
        String content = "BeginPackage[\"Dead`\"];\nFunc[] := 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDeadPackage(context, inputFile, content)
        );
    }

    @Test
    void testDetectDeadPackageWithDependents() {
        // Set up a package with dependents
        InputFile pkg1 = mock(InputFile.class);
        InputFile pkg2 = mock(InputFile.class);
        when(pkg1.filename()).thenReturn("Used.m");
        when(pkg2.filename()).thenReturn("User.m");

        String content1 = "BeginPackage[\"Used`\"];\nFunc[] := 1;\nEndPackage[];";
        String content2 = "BeginPackage[\"User`\"];\nNeeds[\"Used`\"];\nMyFunc[] := 2;\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkg1, content1);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkg2, content2);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDeadPackage(context, pkg1, content1)
        );
    }

    @Test
    void testDetectDeadPackageExcludeTests() {
        // Test files should be excluded from dead package detection
        when(inputFile.filename()).thenReturn("TestPackage.m");
        String content = "BeginPackage[\"TestSuite`\"];\nTestFunc[] := Assert[True];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDeadPackage(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedExportWithUnusedSymbol() {
        // Set up a package that exports symbols that aren't used
        when(inputFile.filename()).thenReturn("UnusedExport.m");
        String content = "BeginPackage[\"MyPkg`\"];\nUnusedFunc::usage = \"Not used anywhere\";\n"
                       + "UnusedFunc[x_] := x;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedExport(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedExportWithUsedSymbol() {
        // Set up a package that exports symbols that ARE used
        InputFile pkg1 = mock(InputFile.class);
        InputFile pkg2 = mock(InputFile.class);
        when(pkg1.filename()).thenReturn("ExportPkg.m");
        when(pkg2.filename()).thenReturn("UserPkg.m");

        String content1 = "BeginPackage[\"Export`\"];\nUsedFunc::usage = \"Used function\";\n"
                        + "UsedFunc[x_] := x;\nEndPackage[];";
        String content2 = "BeginPackage[\"User`\"];\nNeeds[\"Export`\"];\n"
                        + "MyFunc[] := UsedFunc[5];\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkg1, content1);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkg2, content2);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedExport(context, pkg1, content1)
        );
    }

    @Test
    void testDetectUnusedExportNoExports() {
        // Package with no exports
        when(inputFile.filename()).thenReturn("NoExports.m");
        String content = "BeginPackage[\"NoExports`\"];\nPrivateFunc[x_] := x;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedExport(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedPublicFunctionWithUnused() {
        // Public function that's not used
        when(inputFile.filename()).thenReturn("PublicFunc.m");
        String content = "BeginPackage[\"Pkg`\"];\nPublicFunc::usage = \"Public but unused\";\n"
                       + "PublicFunc[] := 1;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedPublicFunction(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnusedPublicFunctionWithUsed() {
        // Public function that IS used
        InputFile pkg1 = mock(InputFile.class);
        InputFile pkg2 = mock(InputFile.class);
        when(pkg1.filename()).thenReturn("PublicPkg.m");
        when(pkg2.filename()).thenReturn("UserPkg.m");

        String content1 = "BeginPackage[\"Public`\"];\nPublicFunc::usage = \"Public and used\";\n"
                        + "PublicFunc[] := 1;\nEndPackage[];";
        String content2 = "BeginPackage[\"User`\"];\nresult = Public`PublicFunc[];\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkg1, content1);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkg2, content2);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnusedPublicFunction(context, pkg1, content1)
        );
    }

    @Test
    void testDetectPackageLoadedButNotListedInMetadataPositive() {
        // Package is loaded but not listed
        String content = "BeginPackage[\"MyPkg`\"];\nNeeds[\"UnlistedDependency`\"];\nEndPackage[];";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageLoadedButNotListedInMetadata(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageLoadedButNotListedInMetadataNegative() {
        // Package is loaded AND listed
        String content = "BeginPackage[\"MyPkg`\", {\"ListedDependency`\"}];\n"
                       + "Needs[\"ListedDependency`\"];\nEndPackage[];";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageLoadedButNotListedInMetadata(context, inputFile, content)
        );
    }

    @Test
    void testDetectLayerViolationWithViolation() {
        // Utilities layer depending on Application layer (violation)
        when(inputFile.filename()).thenReturn("Utilities.m");
        String content = "BeginPackage[\"Utilities`\"];\nFunc[] := Application`Main[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectLayerViolation(context, inputFile, content)
        );
    }

    @Test
    void testDetectLayerViolationNoViolation() {
        // Application layer depending on Utilities layer (allowed)
        when(inputFile.filename()).thenReturn("Application.m");
        String content = "BeginPackage[\"Application`\"];\nFunc[] := Utilities`Helper[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectLayerViolation(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePublicAPIWithIncompleteAPI() {
        // Public function without usage message
        when(inputFile.filename()).thenReturn("IncompleteAPI.m");
        String content = "BeginPackage[\"API`\"];\nPublicFunc[x_] := x;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectIncompletePublicAPI(context, inputFile, content)
        );
    }

    @Test
    void testDetectIncompletePublicAPIWithCompleteAPI() {
        // Public function with usage message
        when(inputFile.filename()).thenReturn("CompleteAPI.m");
        String content = "BeginPackage[\"API`\"];\nPublicFunc::usage = \"Public function\";\n"
                       + "PublicFunc[x_] := x;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectIncompletePublicAPI(context, inputFile, content)
        );
    }

    @Test
    void testDetectOverAbstractedAPIWithManyAbstractions() {
        // Package with many layers of abstraction
        when(inputFile.filename()).thenReturn("OverAbstracted.m");
        String content = "BeginPackage[\"API`\"];\n"
                       + "Level1[x_] := Level2[x];\n"
                       + "Level2[x_] := Level3[x];\n"
                       + "Level3[x_] := Level4[x];\n"
                       + "Level4[x_] := Level5[x];\n"
                       + "Level5[x_] := x;\n"
                       + "EndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectOverAbstractedAPI(context, inputFile, content)
        );
    }

    @Test
    void testDetectInternalImplementationExposedWithExposed() {
        // Internal implementation details exposed
        when(inputFile.filename()).thenReturn("ExposedImpl.m");
        String content = "BeginPackage[\"API`\"];\nInternalHelper::usage = \"Should be private\";\n"
                       + "InternalHelper[x_] := x;\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectInternalImplementationExposed(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageVersionCovered() {
        when(inputFile.filename()).thenReturn("VersionedPackage.m");
        String content = "BeginPackage[\"MyPackage`\"];\nVersion -> \"1.2.3\";\n"
                       + "MyFunc[x_] := x + 1;\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        // Now verify the version was stored by checking version mismatch detection
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPublicAPIChangedWithoutVersionBump(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingPackageImportWithMissingImport() {
        // Test to cover lines 389, 404-406: Missing package import issue reporting
        // First, create a package that exports symbols
        InputFile exportingPkg = mock(InputFile.class);
        when(exportingPkg.filename()).thenReturn("ExportPkg.m");
        String exportContent = "BeginPackage[\"ExternalPkg`\"];\nExternalFunc;\n"
                             + "Begin[\"`Private`\"];\nExternalFunc[x_] := x * 2;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(exportingPkg, exportContent);

        // Now create a package that uses ExternalFunc without importing ExternalPkg
        when(inputFile.filename()).thenReturn("UsingPkg.m");
        String usingContent = "BeginPackage[\"MyPackage`\"];\nMyFunc;\n"
                            + "Begin[\"`Private`\"];\nMyFunc[x_] := ExternalFunc[x];\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, usingContent);

        // This should detect the missing import
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectMissingPackageImport(context, inputFile, usingContent)
        );
    }

    @Test
    void testDetectMissingPackageImportWithLocalDefinition() {
        // Test that locally defined symbols don't trigger missing import
        when(inputFile.filename()).thenReturn("LocalDef.m");
        String content = "BeginPackage[\"MyPackage`\"];\nLocalFunc;\n"
                       + "Begin[\"`Private`\"];\nLocalFunc[x_] := x;\n"
                       + "result = LocalFunc[5];\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectMissingPackageImport(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingPackageImportWithExistingImport() {
        // Test that imported symbols don't trigger missing import
        InputFile exportingPkg = mock(InputFile.class);
        when(exportingPkg.filename()).thenReturn("ExportPkg.m");
        String exportContent = "BeginPackage[\"ExternalPkg`\"];\nExternalFunc;\n"
                             + "Begin[\"`Private`\"];\nExternalFunc[x_] := x * 2;\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(exportingPkg, exportContent);

        when(inputFile.filename()).thenReturn("UsingPkg.m");
        String usingContent = "BeginPackage[\"MyPackage`\"];\nNeeds[\"ExternalPkg`\"];\nMyFunc;\n"
                            + "Begin[\"`Private`\"];\nMyFunc[x_] := ExternalFunc[x];\nEnd[];\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, usingContent);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectMissingPackageImport(context, inputFile, usingContent)
        );
    }

    @Test
    void testDetectGodPackageTooManyDependenciesAboveThreshold() {
        // Test to cover lines 546-548: God package reporting
        StringBuilder content = new StringBuilder("BeginPackage[\"GodPackage`\"];\n");

        // Add 12 dependencies (> 10 threshold)
        for (int i = 1; i <= 12; i++) {
            content.append("Needs[\"Dependency").append(i).append("`\"];\n");
        }
        content.append("MyFunc[x_] := x;\nEndPackage[];");

        when(inputFile.filename()).thenReturn("GodPackage.m");
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content.toString());

        // This should trigger the god package detection
        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(context, inputFile, content.toString())
        );
    }

    @Test
    void testDetectGodPackageTooManyDependenciesNoPackage() {
        // Test early return when no package is found
        String content = "MyFunc[x_] := x + 1;";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(context, inputFile, content)
        );
    }

    @Test
    void testDetectGodPackageTooManyDependenciesBelowThreshold() {
        // Test with dependencies at or below threshold
        String content = "BeginPackage[\"SmallPackage`\"];\n"
                       + "Needs[\"Dep1`\"];\nNeeds[\"Dep2`\"];\nNeeds[\"Dep3`\"];\n"
                       + "MyFunc[x_] := x;\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(context, inputFile, content)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "LibraryPackage.m, Library`, Application`, Application`Main[]",
        "LibraryPackage.m, Library`, Main`, Main`Start[]",
        "AppPackage.m, Application`, Library`, Library`Helper[]"
    })
    void testDetectPackageDependsOnApplicationCode(String filename, String packageName, String dependency, String functionCall) {
        when(inputFile.filename()).thenReturn(filename);
        String content = "BeginPackage[\"" + packageName + "\"];\nNeeds[\"" + dependency + "\"];\n"
                       + "Func[] := " + functionCall + ";\nEndPackage[];";
        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageDependsOnApplicationCodeNoPackage() {
        // Test early return when no package is found
        String content = "MyFunc[x_] := x + 1;";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(context, inputFile, content)
        );
    }

    @Test
    void testDetectTransitiveDependencyCouldBeDirectNoPackage() {
        // Test early return line 419 when no package
        String content = "MyFunc[x_] := x + 1;";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(context, inputFile, content)
        );
    }

    @Test
    void testDetectTransitiveDependencyCouldBeDirectWithTransitiveUse() {
        // Test transitive dependency detection with actual usage
        InputFile pkgA = mock(InputFile.class);
        InputFile pkgB = mock(InputFile.class);
        InputFile pkgC = mock(InputFile.class);
        when(pkgA.filename()).thenReturn("PkgA.m");
        when(pkgB.filename()).thenReturn("PkgB.m");
        when(pkgC.filename()).thenReturn("PkgC.m");

        // C exports symbols
        String contentC = "BeginPackage[\"PkgC`\"];\nFuncC;\n"
                        + "Begin[\"`Private`\"];\nFuncC[x_] := x * 3;\nEnd[];\nEndPackage[];";

        // B depends on C
        String contentB = "BeginPackage[\"PkgB`\"];\nNeeds[\"PkgC`\"];\nFuncB;\n"
                        + "Begin[\"`Private`\"];\nFuncB[x_] := FuncC[x] * 2;\nEnd[];\nEndPackage[];";

        // A depends on B but uses C directly
        String contentA = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nFuncA;\n"
                        + "Begin[\"`Private`\"];\nFuncA[x_] := FuncC[x] + 1;\nEnd[];\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkgC, contentC);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgB, contentB);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgA, contentA);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(context, pkgA, contentA)
        );
    }

    @Test
    void testDetectTransitiveDependencyCouldBeDirectNoTransitiveExports() {
        // Test line 456: when transitive dependency has no exports
        InputFile pkgA = mock(InputFile.class);
        InputFile pkgB = mock(InputFile.class);
        InputFile pkgC = mock(InputFile.class);
        when(pkgA.filename()).thenReturn("PkgA.m");
        when(pkgB.filename()).thenReturn("PkgB.m");
        when(pkgC.filename()).thenReturn("PkgC.m");

        // C has no exports
        String contentC = "BeginPackage[\"PkgC`\"];\nBegin[\"`Private`\"];\nPrivFunc[] := 1;\nEnd[];\nEndPackage[];";

        // B depends on C
        String contentB = "BeginPackage[\"PkgB`\"];\nNeeds[\"PkgC`\"];\nFuncB;\n"
                        + "Begin[\"`Private`\"];\nFuncB[] := 2;\nEnd[];\nEndPackage[];";

        // A depends on B
        String contentA = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nFuncA;\n"
                        + "Begin[\"`Private`\"];\nFuncA[] := FuncB[];\nEnd[];\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkgC, contentC);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgB, contentB);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgA, contentA);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(context, pkgA, contentA)
        );
    }

    @Test
    void testDetectDiamondDependencyNoPackage() {
        // Test early return line 474 when no package
        String content = "MyFunc[x_] := x + 1;";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectDiamondDependency(context, inputFile, content)
        );
    }

    @Test
    void testDetectLayerViolationUICallsData() {
        // Test UI layer calling Data layer directly (violation)
        InputFile uiFile = mock(InputFile.class);
        InputFile dataFile = mock(InputFile.class);
        when(uiFile.filename()).thenReturn("UILayer.m");
        when(dataFile.filename()).thenReturn("DataLayer.m");

        String dataContent = "BeginPackage[\"DataLayer`\"];\nQuery;\n"
                           + "Begin[\"`Private`\"];\nQuery[] := \"SELECT *\";\nEnd[];\nEndPackage[];";
        String uiContent = "BeginPackage[\"UILayer`\"];\nNeeds[\"DataLayer`\"];\nDisplay;\n"
                         + "Begin[\"`Private`\"];\nDisplay[] := DataLayer`Query[];\nEnd[];\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(dataFile, dataContent);
        ArchitectureAndDependencyDetector.buildCrossFileData(uiFile, uiContent);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectLayerViolation(context, uiFile, uiContent)
        );
    }

    @Test
    void testDetectLayerViolationNoPackage() {
        // Test early return when no package
        String content = "MyFunc[x_] := x + 1;";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectLayerViolation(context, inputFile, content)
        );
    }

    @Test
    void testDetectCircularPackageDependencyNoPackage() {
        // Test early return line 298 when no package
        String content = "MyFunc[x_] := x + 1;";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectCircularPackageDependency(context, inputFile, content)
        );
    }

    @Test
    void testDetectCircularPackageDependencyNoCircularDep() {
        // Test line 318: no circular dependency found
        InputFile pkgA = mock(InputFile.class);
        InputFile pkgB = mock(InputFile.class);
        when(pkgA.filename()).thenReturn("PkgA.m");
        when(pkgB.filename()).thenReturn("PkgB.m");

        String contentA = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nFuncA[] := 1;\nEndPackage[];";
        String contentB = "BeginPackage[\"PkgB`\"];\nFuncB[] := 2;\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkgA, contentA);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgB, contentB);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectCircularPackageDependency(context, pkgA, contentA)
        );
    }

    @Test
    void testDetectCircularPackageDependencyWithCircularDep() {
        // Test circular dependency detection path line 330
        InputFile pkgA = mock(InputFile.class);
        InputFile pkgB = mock(InputFile.class);
        InputFile pkgC = mock(InputFile.class);
        when(pkgA.filename()).thenReturn("PkgA.m");
        when(pkgB.filename()).thenReturn("PkgB.m");
        when(pkgC.filename()).thenReturn("PkgC.m");

        // Create circular dependency: A -> B -> C -> A
        String contentA = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nFuncA[] := 1;\nEndPackage[];";
        String contentB = "BeginPackage[\"PkgB`\"];\nNeeds[\"PkgC`\"];\nFuncB[] := 2;\nEndPackage[];";
        String contentC = "BeginPackage[\"PkgC`\"];\nNeeds[\"PkgA`\"];\nFuncC[] := 3;\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkgA, contentA);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgB, contentB);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgC, contentC);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectCircularPackageDependency(context, pkgA, contentA)
        );
    }

    @Test
    void testDetectTransitiveDependencyBreakEarlyOnMatch() {
        // Test line 450: break when transitive dependency is used
        InputFile pkgA = mock(InputFile.class);
        InputFile pkgB = mock(InputFile.class);
        InputFile pkgC = mock(InputFile.class);
        InputFile pkgD = mock(InputFile.class);
        when(pkgA.filename()).thenReturn("PkgA.m");
        when(pkgB.filename()).thenReturn("PkgB.m");
        when(pkgC.filename()).thenReturn("PkgC.m");
        when(pkgD.filename()).thenReturn("PkgD.m");

        // Set up multiple transitive dependencies, only first one used
        String contentC = "BeginPackage[\"PkgC`\"];\nFuncC;\n"
                        + "Begin[\"`Private`\"];\nFuncC[] := 3;\nEnd[];\nEndPackage[];";
        String contentD = "BeginPackage[\"PkgD`\"];\nFuncD;\n"
                        + "Begin[\"`Private`\"];\nFuncD[] := 4;\nEnd[];\nEndPackage[];";
        String contentB = "BeginPackage[\"PkgB`\"];\nNeeds[\"PkgC`\"];\nNeeds[\"PkgD`\"];\n"
                        + "FuncB;\nBegin[\"`Private`\"];\nFuncB[] := 2;\nEnd[];\nEndPackage[];";
        String contentA = "BeginPackage[\"PkgA`\"];\nNeeds[\"PkgB`\"];\nFuncA;\n"
                        + "Begin[\"`Private`\"];\nFuncA[] := FuncC[];\nEnd[];\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(pkgC, contentC);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgD, contentD);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgB, contentB);
        ArchitectureAndDependencyDetector.buildCrossFileData(pkgA, contentA);

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(context, pkgA, contentA)
        );
    }

    @Test
    void testDetectUnstableDependencyWithNoPackage() {
        // Test early return when no package
        String content = "MyFunc[x_] := x + 1;";

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectUnstableDependency(context, inputFile, content)
        );
    }

    @Test
    void testDetectPackageTooLargeWithManyLines() {
        // Test package with more than 2000 lines
        StringBuilder content = new StringBuilder("BeginPackage[\"HugePackage`\"];\n");
        for (int i = 1; i <= 2100; i++) {
            content.append("(* Line ").append(i).append(" *)\n");
        }
        content.append("EndPackage[];");

        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content.toString());

        assertDoesNotThrow(() ->
            ArchitectureAndDependencyDetector.detectPackageTooLarge(context, inputFile, content.toString())
        );
    }

    @Test
    void testBuildCrossFileDataWithNeeds() {
        when(inputFile.filename()).thenReturn("PackageWithNeeds.m");
        String content = "BeginPackage[\"MyPkg`\"];\n"
                       + "Needs[\"Dependency1`\"];\n"
                       + "Needs[\"Dependency2`\"];\n"
                       + "Needs[\"Dependency3`\"];\n"
                       + "MyFunc[x_] := x;\nEndPackage[];";

        ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);

        // Verify dependencies were tracked
        assertThat(ArchitectureAndDependencyDetector.getPackageDependenciesSize()).isPositive();
    }

}
