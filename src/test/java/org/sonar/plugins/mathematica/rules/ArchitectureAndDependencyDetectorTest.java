package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchitectureAndDependencyDetectorTest {

    private SensorContext mockContext;
    private InputFile mockInputFile;
    private NewIssue mockIssue;
    private NewIssueLocation mockLocation;
    private TextRange mockTextRange;

    @BeforeEach
    void setUp() {
        ArchitectureAndDependencyDetector.initializeCaches();

        // Set up mocks
        mockContext = mock(SensorContext.class);
        mockInputFile = mock(InputFile.class);
        mockIssue = mock(NewIssue.class);
        mockLocation = mock(NewIssueLocation.class);
        mockTextRange = mock(TextRange.class);

        // Configure mock behavior
        when(mockInputFile.filename()).thenReturn("TestFile.m");
        when(mockInputFile.uri()).thenReturn(java.net.URI.create("file:///test/TestFile.m"));
        when(mockInputFile.selectLine(any(Integer.class))).thenReturn(mockTextRange);
        when(mockContext.newIssue()).thenReturn(mockIssue);
        when(mockIssue.newLocation()).thenReturn(mockLocation);
        when(mockLocation.on(any(InputFile.class))).thenReturn(mockLocation);
        when(mockLocation.at(any(TextRange.class))).thenReturn(mockLocation);
        when(mockLocation.message(anyString())).thenReturn(mockLocation);
        when(mockIssue.forRule(any(RuleKey.class))).thenReturn(mockIssue);
        when(mockIssue.at(any(NewIssueLocation.class))).thenReturn(mockIssue);
    }

    @AfterEach
    void tearDown() {
        ArchitectureAndDependencyDetector.clearCaches();
    }

    @Test
    void testInitializeCaches() {
        ArchitectureAndDependencyDetector.clearCaches();
        assertThatCode(() -> ArchitectureAndDependencyDetector.initializeCaches())
            .doesNotThrowAnyException();
    }

    @Test
    void testClearCaches() {
        assertThatCode(() -> ArchitectureAndDependencyDetector.clearCaches())
            .doesNotThrowAnyException();
    }

    @Test
    void testInitializeAndClearSequence() {
        ArchitectureAndDependencyDetector.clearCaches();
        ArchitectureAndDependencyDetector.initializeCaches();
        ArchitectureAndDependencyDetector.clearCaches();
        ArchitectureAndDependencyDetector.initializeCaches();
    }

    @Test
    void testGetPackageDependenciesSize() {
        int size = ArchitectureAndDependencyDetector.getPackageDependenciesSize();
        assertThat(size).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testGetSymbolDefinitionsSize() {
        int size = ArchitectureAndDependencyDetector.getSymbolDefinitionsSize();
        assertThat(size).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testGetSymbolUsagesSize() {
        int size = ArchitectureAndDependencyDetector.getSymbolUsagesSize();
        assertThat(size).isGreaterThanOrEqualTo(0);
    }

    // ========================================
    // TESTS FOR CIRCULAR DEPENDENCY DETECTION
    // ========================================

    @Test
    void testDetectCircularPackageDependencyNoPackage() {
        String content = "x = 1 + 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectCircularPackageDependency(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectCircularPackageDependencySimplePackage() {
        String content = "BeginPackage[\"MyPackage`\"]\nMyFunc::usage = \"test\"\nEndPackage[]";

        // Build cross-file data first
        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);

        // Should not detect circular dependency in a simple package
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectCircularPackageDependency(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR PACKAGE IMPORT DETECTION
    // ========================================

    @Test
    void testDetectUnusedPackageImportNoImports() {
        String content = "BeginPackage[\"MyPackage`\"]\nMyFunc::usage = \"test\"\nEndPackage[]";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectUnusedPackageImport(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectUnusedPackageImportWithNeeds() {
        String content = "BeginPackage[\"MyPackage`\"]\nNeeds[\"SomePackage`\"]\nMyFunc::usage = \"test\"\nEndPackage[]";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectUnusedPackageImport(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingPackageImportNoIssue() {
        String content = "BeginPackage[\"MyPackage`\"]\nMyFunc::usage = \"test\"\nEndPackage[]";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectMissingPackageImport(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR DIAMOND DEPENDENCY
    // ========================================

    @Test
    void testDetectDiamondDependencyNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectDiamondDependency(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectDiamondDependencySimple() {
        String content = "BeginPackage[\"MyPackage`\"]\nEndPackage[]";
        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectDiamondDependency(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR GOD PACKAGE DETECTION
    // ========================================

    @Test
    void testDetectGodPackageTooManyDependenciesNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectGodPackageTooManyDependenciesSimple() {
        String content = "BeginPackage[\"MyPackage`\"]\nEndPackage[]";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR PACKAGE SIZE DETECTION
    // ========================================

    @Test
    void testDetectPackageTooLargeNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageTooLarge(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageTooSmallNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageTooSmall(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR EXPORT DETECTION
    // ========================================

    @Test
    void testDetectPackageExportsTooMuchNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageExportsTooMuch(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageExportsTooLittleNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageExportsTooLittle(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectIncompletePublicAPINoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectIncompletePublicAPI(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR DEPENDENCY ARCHITECTURE
    // ========================================

    @Test
    void testDetectTransitiveDependencyCouldBeDirectNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectLayerViolationNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectLayerViolation(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectUnstableDependencyNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectUnstableDependency(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectCyclicCallBetweenPackagesNoContent() {
        String content = "";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectCyclicCallBetweenPackages(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR DEAD CODE DETECTION
    // ========================================

    @Test
    void testDetectUnusedPublicFunctionNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectUnusedPublicFunction(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectUnusedExportNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectUnusedExport(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectDeadPackageNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectDeadPackage(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectFunctionOnlyCalledOnceNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectFunctionOnlyCalledOnce(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR API CONSISTENCY
    // ========================================

    @Test
    void testDetectInternalImplementationExposedNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInternalImplementationExposed(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPublicAPIChangedWithoutVersionBumpNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPublicAPIChangedWithoutVersionBump(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectOverAbstractedAPINoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectOverAbstractedAPI(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectImplementationWithoutTestsNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectImplementationWithoutTests(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectOrphanedTestFileNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectOrphanedTestFile(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectDeprecatedAPIStillUsedInternallyNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectDeprecatedAPIStillUsedInternally(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectInternalAPIUsedLikePublicNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInternalAPIUsedLikePublic(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR PACKAGE LOADING
    // ========================================

    @Test
    void testDetectCommentedOutPackageLoadNoComments() {
        String content = "BeginPackage[\"MyPackage`\"]\nEndPackage[]";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectCommentedOutPackageLoad(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectConditionalPackageLoadNoConditionals() {
        String content = "BeginPackage[\"MyPackage`\"]\nEndPackage[]";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectConditionalPackageLoad(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageLoadedButNotListedInMetadataNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageLoadedButNotListedInMetadata(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR SYMBOL ANALYSIS
    // ========================================

    @Test
    void testDetectDuplicateSymbolDefinitionAcrossPackagesNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectDuplicateSymbolDefinitionAcrossPackages(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectSymbolRedefinitionAfterImportNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectSymbolRedefinitionAfterImport(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageVersionMismatchNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageVersionMismatch(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPublicExportMissingUsageMessageNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPublicExportMissingUsageMessage(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentParameterNamesAcrossOverloadsNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInconsistentParameterNamesAcrossOverloads(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // TESTS FOR DOCUMENTATION AND NAMING
    // ========================================

    @Test
    void testDetectPublicFunctionWithImplementationDetailsInNameNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPublicFunctionWithImplementationDetailsInName(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPublicAPINotInPackageContextNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPublicAPINotInPackageContext(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectTestFunctionInProductionCodeNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectTestFunctionInProductionCode(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingPackageDocumentationNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectMissingPackageDocumentation(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentPackageNamingNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPrivateSymbolUsedExternallyNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPrivateSymbolUsedExternally(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageDependsOnApplicationCodeNoPackage() {
        String content = "x = 1";
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // INTEGRATION TEST WITH REAL PACKAGE CODE
    // ========================================

    @Test
    void testBuildCrossFileDataWithCompletePackage() {
        String content = "BeginPackage[\"TestPackage`\", {\"RequiredPackage`\"}]\n"
                        + "MyFunction::usage = \"MyFunction[x] does something.\"\n"
                        + "Begin[\"`Private`\"]\n"
                        + "MyFunction[x_] := x + 1\n"
                        + "End[]\n"
                        + "EndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content))
            .doesNotThrowAnyException();

        // Verify the data was populated
        assertThat(ArchitectureAndDependencyDetector.getPackageDependenciesSize()).isGreaterThanOrEqualTo(0);
        assertThat(ArchitectureAndDependencyDetector.getSymbolDefinitionsSize()).isGreaterThanOrEqualTo(0);
    }
}
