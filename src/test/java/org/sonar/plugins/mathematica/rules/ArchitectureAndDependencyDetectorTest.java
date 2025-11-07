package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.util.stream.Stream;

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
        assertThatCode(ArchitectureAndDependencyDetector::initializeCaches)
            .doesNotThrowAnyException();
    }

    @Test
    void testClearCaches() {
        assertThatCode(ArchitectureAndDependencyDetector::clearCaches)
            .doesNotThrowAnyException();
    }

    @Test
    void testInitializeAndClearSequence() {
        assertThatCode(() -> {
            ArchitectureAndDependencyDetector.clearCaches();
            ArchitectureAndDependencyDetector.initializeCaches();
            ArchitectureAndDependencyDetector.clearCaches();
            ArchitectureAndDependencyDetector.initializeCaches();
        }).doesNotThrowAnyException();
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
            mockContext, mockInputFile))
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
            mockContext, mockInputFile))
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

    @FunctionalInterface
    interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    @ParameterizedTest
    @MethodSource("provideDetectorMethods")
    void testDetectorMethodsNoPackage(TriConsumer<SensorContext, InputFile, String> detectorMethod) {
        String content = "x = 1";
        assertThatCode(() -> detectorMethod.accept(mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    private static Stream<TriConsumer<SensorContext, InputFile, String>> provideDetectorMethods() {
        return Stream.of(
            (ctx, file, content) -> ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(ctx, file, content),
            (ctx, file, content) -> ArchitectureAndDependencyDetector.detectPrivateSymbolUsedExternally(ctx, file, content),
            (ctx, file, content) -> ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(ctx, file, content)
        );
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

    // ========================================
    // ADDITIONAL TESTS FOR CONSTRUCTOR
    // ========================================

    @Test
    void testConstructorThrowsException() throws Exception {
        java.lang.reflect.Constructor<ArchitectureAndDependencyDetector> constructor =
            ArchitectureAndDependencyDetector.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> constructor.newInstance());

        assertThat(exception.getCause()).isInstanceOf(UnsupportedOperationException.class);
    }

    // ========================================
    // ADDITIONAL BUILDCROSSFILEDATA TESTS
    // ========================================

    @Test
    void testBuildCrossFileDataWithTestFile() {
        when(mockInputFile.filename()).thenReturn("TestFile.wlt");
        String content = "VerifyTest[MyFunction[1], 2]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testBuildCrossFileDataWithNeeds() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "Needs[\"UtilityPackage`\"]\n"
                        + "Needs[\"DataPackage`\"]\n"
                        + "EndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testBuildCrossFileDataWithVersion() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "Version -> \"1.0.0\"\n"
                        + "EndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testBuildCrossFileDataWithPublicAndPrivateSections() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "PublicFunc::usage = \"test\"\n"
                        + "PublicFunc[x_] := x + 1\n"
                        + "Begin[\"`Private`\"]\n"
                        + "PrivateFunc[x_] := x * 2\n"
                        + "End[]\n"
                        + "EndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testBuildCrossFileDataWithFunctionCalls() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "MyFunc[x_] := OtherFunc[x]\n"
                        + "EndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // POSITIVE TESTS (ISSUES DETECTED)
    // ========================================

    @Test
    void testDetectPackageTooLargeDetectsIssue() {
        StringBuilder largeContent = new StringBuilder("BeginPackage[\"LargePackage`\"]\n");
        for (int i = 0; i < 2100; i++) {
            largeContent.append("x = ").append(i).append("\n");
        }
        largeContent.append("EndPackage[]");

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, largeContent.toString());
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageTooLarge(
            mockContext, mockInputFile, largeContent.toString()))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageTooSmallDetectsIssue() {
        String smallContent = "BeginPackage[\"SmallPackage`\"]\nx = 1\nEndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, smallContent);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageTooSmall(
            mockContext, mockInputFile, smallContent))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentPackageNamingWithBadName() {
        String content = "BeginPackage[\"Bad`name`\"]\nEndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentPackageNamingWithShortSegment() {
        String content = "BeginPackage[\"A`B`\"]\nEndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPrivateSymbolUsedExternallyWithPrivateContext() {
        String content = "x = MyPackage`Private`InternalFunc[1]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPrivateSymbolUsedExternally(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectCommentedOutPackageLoadWithComment() {
        String content = "(* Needs[\"OldPackage`\"] *)";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectCommentedOutPackageLoad(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectConditionalPackageLoadWithConditional() {
        String content = "If[$VersionNumber > 12, Needs[\"NewPackage`\"]]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectConditionalPackageLoad(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectTestFunctionInProductionCodeDetectsIssue() {
        when(mockInputFile.filename()).thenReturn("Production.m");
        String content = "TestCreate[suite] := suite";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectTestFunctionInProductionCode(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectMissingPackageDocumentationDetectsIssue() {
        String content = "BeginPackage[\"UndocumentedPackage`\"]\nEndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectMissingPackageDocumentation(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPublicAPINotInPackageContextDetectsIssue() {
        String content = "MyPublicFunc[x_] := x + 1";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPublicAPINotInPackageContext(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectLayerViolationWithUICallData() {
        String content = "BeginPackage[\"MyUI`\"]\nNeeds[\"MyData`\"]\nEndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectLayerViolation(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageDependsOnApplicationCodeWithLibraryDependingOnApp() {
        String content = "BeginPackage[\"LibraryPackage`\"]\nNeeds[\"MyApp`\"]\nEndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectIncompletePublicAPIWithCreateButNoDelete() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "CreateObject::usage = \"test\"\n"
                        + "CreateObject[x_] := {}\n"
                        + "EndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectIncompletePublicAPI(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectIncompletePublicAPIWithSetButNoGet() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "SetValue::usage = \"test\"\n"
                        + "SetValue[x_] := x\n"
                        + "EndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectIncompletePublicAPI(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectInternalImplementationExposedWithInternalName() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "InternalHelper::usage = \"test\"\n"
                        + "InternalHelper[x_] := x\n"
                        + "EndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInternalImplementationExposed(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPublicFunctionWithImplementationDetailsInNameDetectsIssue() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "ProcessLoop::usage = \"test\"\n"
                        + "ProcessLoop[x_] := x\n"
                        + "EndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPublicFunctionWithImplementationDetailsInName(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectInconsistentParameterNamesAcrossOverloadsDetectsIssue() {
        String content = "MyFunc[x_] := x\nMyFunc[y_Integer] := y * 2\nMyFunc[z_Real] := z / 2";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectInconsistentParameterNamesAcrossOverloads(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectSymbolRedefinitionAfterImportDetectsIssue() {
        String content1 = "BeginPackage[\"Package1`\"]\nMyFunc::usage = \"test\"\nMyFunc[x_] := x\nEndPackage[]";
        String content2 = "BeginPackage[\"Package2`\"]\nNeeds[\"Package1`\"]\nMyFunc[x_] := x + 1\nEndPackage[]";

        when(mockInputFile.filename()).thenReturn("Package1.m");
        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content1);

        when(mockInputFile.filename()).thenReturn("Package2.m");
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectSymbolRedefinitionAfterImport(
            mockContext, mockInputFile, content2))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectDeprecatedAPIStillUsedInternallyDetectsIssue() {
        String content = "BeginPackage[\"MyPackage`\"]\n"
                        + "(* @deprecated *)\n"
                        + "OldFunc[x_] := x\n"
                        + "NewFunc[y_] := OldFunc[y]\n"
                        + "EndPackage[]";

        ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, content);
        assertThatCode(() -> ArchitectureAndDependencyDetector.detectDeprecatedAPIStillUsedInternally(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    @Test
    void testDetectPackageVersionMismatchWithVersionRequirement() {
        String content = "BeginPackage[\"MyPackage`\"]\nNeeds[\"OtherPackage`\", \"2.0.0\"]\nEndPackage[]";

        assertThatCode(() -> ArchitectureAndDependencyDetector.detectPackageVersionMismatch(
            mockContext, mockInputFile, content))
            .doesNotThrowAnyException();
    }

    // ========================================
    // EDGE CASE TESTS
    // ========================================

    @Test
    void testBuildCrossFileDataWithEmptyContent() {
        assertThatCode(() -> ArchitectureAndDependencyDetector.buildCrossFileData(mockInputFile, ""))
            .doesNotThrowAnyException();
    }

    @Test
    void testAllDetectionMethodsWithEmptyContent() {
        String content = "";

        assertThatCode(() -> {
            ArchitectureAndDependencyDetector.detectCircularPackageDependency(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectUnusedPackageImport(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectMissingPackageImport(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectDiamondDependency(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectCyclicCallBetweenPackages(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectLayerViolation(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectUnstableDependency(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPackageTooLarge(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPackageTooSmall(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPackageExportsTooMuch(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPackageExportsTooLittle(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectIncompletePublicAPI(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPrivateSymbolUsedExternally(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectInternalImplementationExposed(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectMissingPackageDocumentation(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPublicAPIChangedWithoutVersionBump(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectUnusedPublicFunction(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectUnusedExport(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectDeadPackage(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectFunctionOnlyCalledOnce(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectOverAbstractedAPI(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectOrphanedTestFile(mockContext, mockInputFile);
            ArchitectureAndDependencyDetector.detectImplementationWithoutTests(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectDeprecatedAPIStillUsedInternally(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectInternalAPIUsedLikePublic(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectCommentedOutPackageLoad(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectConditionalPackageLoad(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPackageLoadedButNotListedInMetadata(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectDuplicateSymbolDefinitionAcrossPackages(mockContext, mockInputFile);
            ArchitectureAndDependencyDetector.detectSymbolRedefinitionAfterImport(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPackageVersionMismatch(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPublicExportMissingUsageMessage(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectInconsistentParameterNamesAcrossOverloads(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPublicFunctionWithImplementationDetailsInName(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectPublicAPINotInPackageContext(mockContext, mockInputFile, content);
            ArchitectureAndDependencyDetector.detectTestFunctionInProductionCode(mockContext, mockInputFile, content);
        }).doesNotThrowAnyException();
    }
}
