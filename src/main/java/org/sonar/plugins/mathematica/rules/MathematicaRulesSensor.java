package org.sonar.plugins.mathematica.rules;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.mathematica.MathematicaLanguage;
import org.sonar.plugins.mathematica.symboltable.SymbolTable;
import org.sonar.plugins.mathematica.symboltable.SymbolTableBuilder;
import org.sonar.plugins.mathematica.symboltable.SymbolTableManager;

/**
 * Main sensor that coordinates analysis of Mathematica files.
 * Delegates rule detection to specialized detector classes for better maintainability.
 *
 * Architecture:
 * - CodeSmellDetector: Handles 33 code smell/maintainability rules
 * - BugDetector: Handles 20 bug/reliability rules
 * - VulnerabilityDetector: Handles 14 security vulnerability rules
 * - SecurityHotspotDetector: Handles 7 security hotspot rules
 *
 * Total: 74 rules across 4 detector classes.
 */
public class MathematicaRulesSensor implements Sensor {

    private static final Logger LOG = Loggers.get(MathematicaRulesSensor.class);

    // Comment pattern for comment analysis
    private static final Pattern COMMENT_PATTERN = Pattern.compile("\\(\\*[\\s\\S]*?\\*\\)");

    // Instantiate detector classes (reused across files)
    private final CodeSmellDetector codeSmellDetector = new CodeSmellDetector();
    private final BugDetector bugDetector = new BugDetector();
    private final VulnerabilityDetector vulnerabilityDetector = new VulnerabilityDetector();
    private final SecurityHotspotDetector securityHotspotDetector = new SecurityHotspotDetector();
    private final Chunk1Detector chunk1Detector = new Chunk1Detector();
    private final Chunk2Detector chunk2Detector = new Chunk2Detector();
    private final Chunk3Detector chunk3Detector = new Chunk3Detector();
    private final Chunk4Detector chunk4Detector = new Chunk4Detector();
    private final Chunk5Detector chunk5Detector = new Chunk5Detector();
    private final Chunk67Detector chunk67Detector = new Chunk67Detector();

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("Mathematica Rules Sensor")
            .onlyOnLanguage(MathematicaLanguage.KEY);
    }

    @Override
    public void execute(SensorContext context) {
        FileSystem fs = context.fileSystem();
        FilePredicates predicates = fs.predicates();

        Iterable<InputFile> inputFiles = fs.inputFiles(
            predicates.and(
                predicates.hasLanguage(MathematicaLanguage.KEY),
                predicates.hasType(InputFile.Type.MAIN)
            )
        );

        // Count total files and log start
        List<InputFile> fileList = new ArrayList<>();
        inputFiles.forEach(fileList::add);
        int totalFiles = fileList.size();

        LOG.info("Starting analysis of {} Mathematica file(s)...", totalFiles);
        LOG.info("Using parallel processing with {} threads", Runtime.getRuntime().availableProcessors());
        LOG.info("Progress will be reported every 100 files");

        long startTime = System.currentTimeMillis();

        // === PHASE 1: Build cross-file analysis data for Chunk5 ===
        LOG.info("Phase 1: Building cross-file dependency graph...");
        Chunk5Detector.initializeCaches();

        fileList.parallelStream().forEach(inputFile -> {
            try {
                if (inputFile.lines() < 3 || inputFile.lines() > 35000) return;
                String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);
                if (content.length() > 2_000_000 || content.trim().isEmpty()) return;

                Chunk5Detector.buildCrossFileData(inputFile, content);
            } catch (Exception e) {
                LOG.debug("Error building cross-file data for: {}", inputFile.filename());
            }
        });

        LOG.info("Phase 1 complete. Starting Phase 2: Rule detection...");

        // === PHASE 2: Run all detectors ===
        java.util.concurrent.atomic.AtomicInteger processedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        int progressInterval = 100; // Log every 100 files

        // Use parallel stream for faster processing
        fileList.parallelStream().forEach(inputFile -> {
            try {
                analyzeFile(context, inputFile);
                int count = processedCount.incrementAndGet();

                // Log progress every 100 files (thread-safe)
                if (count % progressInterval == 0) {
                    long elapsedMs = System.currentTimeMillis() - startTime;
                    double filesPerSec = count / (elapsedMs / 1000.0);
                    int remainingFiles = totalFiles - count;
                    long estimatedRemainingMs = (long)(remainingFiles / filesPerSec * 1000);

                    LOG.info("Progress: {}/{} files analyzed ({} %) | Speed: {} files/sec | Est. remaining: {} min",
                        count,
                        totalFiles,
                        (int)((count * 100.0) / totalFiles),
                        String.format("%.1f", filesPerSec),
                        estimatedRemainingMs / 60000);
                }
            } catch (Exception e) {
                LOG.error("Error processing file: {}", inputFile.filename(), e);
            }
        });

        long totalTimeMs = System.currentTimeMillis() - startTime;
        double avgFilesPerSec = totalFiles / (totalTimeMs / 1000.0);
        LOG.info("Analysis complete: {} files analyzed in {} seconds ({} files/sec)",
            totalFiles,
            totalTimeMs / 1000,
            String.format("%.1f", avgFilesPerSec));

        // Clean up cross-file analysis caches
        Chunk5Detector.clearCaches();
    }

    /**
     * Analyzes a single file by delegating to specialized detectors.
     *
     * NOTE: Incremental analysis removed from rules sensor because SonarQube's
     * file status doesn't detect plugin changes. When rules change, files with
     * status=SAME would be skipped even though they need re-analysis.
     * Performance optimization kept in metrics/CPD sensors where risk is lower.
     */
    private void analyzeFile(SensorContext context, InputFile inputFile) {
        try {
            // Skip very small files quickly (likely empty or trivial)
            if (inputFile.lines() < 3) {
                return;
            }

            // Always check file length first
            detectLongFile(context, inputFile);

            // Skip further analysis for extremely large files (performance)
            if (inputFile.lines() > 35000) {
                LOG.debug("Skipping further analysis of large file (>35000 lines): {}", inputFile);
                return;
            }

            String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);

            // Skip files larger than 2MB
            if (content.length() > 2_000_000) {
                LOG.debug("Skipping further analysis of large file (>2MB): {}", inputFile);
                return;
            }

            // Skip empty or whitespace-only files
            if (content.trim().isEmpty()) {
                return;
            }

            // Initialize caches in all detectors for this file
            codeSmellDetector.initializeCaches(content);
            bugDetector.initializeCaches(content);
            vulnerabilityDetector.initializeCaches(content);
            securityHotspotDetector.initializeCaches(content);
            chunk1Detector.initializeCaches(content);
            chunk2Detector.initializeCaches(content);
            chunk3Detector.initializeCaches(content);
            chunk4Detector.initializeCaches(content);

            // Analyze comments once and cache for reuse
            List<int[]> commentRanges = analyzeComments(context, inputFile, content);

            // Delegate to Code Smell detector (33 rules)
            codeSmellDetector.detectMagicNumbers(context, inputFile, content, commentRanges);
            codeSmellDetector.detectEmptyBlocks(context, inputFile, content);
            codeSmellDetector.detectLongFunctions(context, inputFile, content);
            codeSmellDetector.detectEmptyCatchBlocks(context, inputFile, content);
            codeSmellDetector.detectDebugCode(context, inputFile, content);
            codeSmellDetector.detectUnusedVariables(context, inputFile, content);
            codeSmellDetector.detectDuplicateFunctions(context, inputFile, content);
            codeSmellDetector.detectTooManyParameters(context, inputFile, content);
            codeSmellDetector.detectDeeplyNested(context, inputFile, content);
            codeSmellDetector.detectMissingDocumentation(context, inputFile, content);
            codeSmellDetector.detectInconsistentNaming(context, inputFile, content);
            codeSmellDetector.detectIdenticalBranches(context, inputFile, content);
            codeSmellDetector.detectExpressionTooComplex(context, inputFile, content, commentRanges);
            codeSmellDetector.detectDeprecatedFunctions(context, inputFile, content);
            codeSmellDetector.detectEmptyStatement(context, inputFile, content);

            // Performance rules (Code Smell)
            codeSmellDetector.detectAppendInLoop(context, inputFile, content);
            codeSmellDetector.detectRepeatedFunctionCalls(context, inputFile, content);
            codeSmellDetector.detectStringConcatInLoop(context, inputFile, content);
            codeSmellDetector.detectUncompiledNumerical(context, inputFile, content);
            codeSmellDetector.detectPackedArrayBreaking(context, inputFile, content);
            codeSmellDetector.detectNestedMapTable(context, inputFile, content);
            codeSmellDetector.detectLargeTempExpressions(context, inputFile, content);
            codeSmellDetector.detectPlotInLoop(context, inputFile, content);

            // Best practices rules (Code Smell)
            codeSmellDetector.detectGenericVariableNames(context, inputFile, content);
            codeSmellDetector.detectMissingUsageMessage(context, inputFile, content);
            codeSmellDetector.detectMissingOptionsPattern(context, inputFile, content);
            codeSmellDetector.detectSideEffectsNaming(context, inputFile, content);
            codeSmellDetector.detectComplexBoolean(context, inputFile, content);
            codeSmellDetector.detectUnprotectedSymbols(context, inputFile, content);
            codeSmellDetector.detectMissingReturn(context, inputFile, content);

            // Delegate to Bug detector (20 rules)
            bugDetector.detectDivisionByZero(context, inputFile, content);
            bugDetector.detectAssignmentInConditional(context, inputFile, content);
            bugDetector.detectListIndexOutOfBounds(context, inputFile, content);
            bugDetector.detectInfiniteRecursion(context, inputFile, content);
            bugDetector.detectUnreachablePatterns(context, inputFile, content);
            bugDetector.detectFloatingPointEquality(context, inputFile, content);
            bugDetector.detectFunctionWithoutReturn(context, inputFile, content);
            bugDetector.detectVariableBeforeAssignment(context, inputFile, content);
            bugDetector.detectOffByOne(context, inputFile, content);
            bugDetector.detectInfiniteLoop(context, inputFile, content);
            bugDetector.detectMismatchedDimensions(context, inputFile, content);
            bugDetector.detectTypeMismatch(context, inputFile, content);
            bugDetector.detectSuspiciousPattern(context, inputFile, content);

            // Pattern matching bugs
            bugDetector.detectMissingPatternTest(context, inputFile, content);
            bugDetector.detectPatternBlanksMisuse(context, inputFile, content);
            bugDetector.detectSetDelayedConfusion(context, inputFile, content);
            bugDetector.detectSymbolNameCollision(context, inputFile, content);
            bugDetector.detectBlockModuleMisuse(context, inputFile, content);

            // Resource management bugs
            bugDetector.detectUnclosedFileHandle(context, inputFile, content);
            bugDetector.detectGrowingDefinitionChain(context, inputFile, content);

            // Delegate to Vulnerability detector (14 rules)
            vulnerabilityDetector.detectHardcodedCredentials(context, inputFile, content);
            vulnerabilityDetector.detectCommandInjection(context, inputFile, content);
            vulnerabilityDetector.detectSqlInjection(context, inputFile, content);
            vulnerabilityDetector.detectCodeInjection(context, inputFile, content);
            vulnerabilityDetector.detectPathTraversal(context, inputFile, content);
            vulnerabilityDetector.detectWeakCryptography(context, inputFile, content);
            vulnerabilityDetector.detectSsrf(context, inputFile, content);
            vulnerabilityDetector.detectInsecureDeserialization(context, inputFile, content);
            vulnerabilityDetector.detectUnsafeSymbol(context, inputFile, content);
            vulnerabilityDetector.detectXXE(context, inputFile, content);
            vulnerabilityDetector.detectMissingSanitization(context, inputFile, content);
            vulnerabilityDetector.detectInsecureRandomExpanded(context, inputFile, content);
            vulnerabilityDetector.detectUnsafeCloudDeploy(context, inputFile, content);
            vulnerabilityDetector.detectDynamicInjection(context, inputFile, content);

            // Delegate to Security Hotspot detector (7 rules)
            securityHotspotDetector.detectFileUploadValidation(context, inputFile, content);
            securityHotspotDetector.detectExternalApiSafeguards(context, inputFile, content);
            securityHotspotDetector.detectCryptoKeyGeneration(context, inputFile, content);
            securityHotspotDetector.detectNetworkOperations(context, inputFile, content);
            securityHotspotDetector.detectFileSystemModifications(context, inputFile, content);
            securityHotspotDetector.detectEnvironmentVariable(context, inputFile, content);
            securityHotspotDetector.detectImportWithoutFormat(context, inputFile, content);

            // ===== PHASE 4: NEW RULE DETECTORS (50 rules) =====

            // New Code Smell detectors (18 rules)
            codeSmellDetector.detectOvercomplexPatterns(context, inputFile, content);
            codeSmellDetector.detectInconsistentRuleTypes(context, inputFile, content);
            codeSmellDetector.detectMissingFunctionAttributes(context, inputFile, content);
            codeSmellDetector.detectMissingDownValuesDoc(context, inputFile, content);
            codeSmellDetector.detectMissingPatternTestValidation(context, inputFile, content);
            codeSmellDetector.detectExcessivePureFunctions(context, inputFile, content);
            codeSmellDetector.detectMissingOperatorPrecedence(context, inputFile, content);
            codeSmellDetector.detectHardcodedFilePaths(context, inputFile, content);
            codeSmellDetector.detectInconsistentReturnTypes(context, inputFile, content);
            codeSmellDetector.detectMissingErrorMessages(context, inputFile, content);
            codeSmellDetector.detectGlobalStateModification(context, inputFile, content);
            codeSmellDetector.detectMissingLocalization(context, inputFile, content);
            codeSmellDetector.detectExplicitGlobalContext(context, inputFile, content);
            codeSmellDetector.detectMissingTemporaryCleanup(context, inputFile, content);
            codeSmellDetector.detectNestedListsInsteadAssociation(context, inputFile, content);
            codeSmellDetector.detectRepeatedPartExtraction(context, inputFile, content);
            codeSmellDetector.detectMissingMemoization(context, inputFile, content);
            codeSmellDetector.detectStringJoinForTemplates(context, inputFile, content);

            // New Performance detectors (10 rules)
            codeSmellDetector.detectLinearSearchInsteadLookup(context, inputFile, content);
            codeSmellDetector.detectRepeatedCalculations(context, inputFile, content);
            codeSmellDetector.detectPositionInsteadPattern(context, inputFile, content);
            codeSmellDetector.detectFlattenTableAntipattern(context, inputFile, content);
            codeSmellDetector.detectMissingParallelization(context, inputFile, content);
            codeSmellDetector.detectMissingSparseArray(context, inputFile, content);
            codeSmellDetector.detectUnnecessaryTranspose(context, inputFile, content);
            codeSmellDetector.detectDeleteDuplicatesOnLargeData(context, inputFile, content);
            codeSmellDetector.detectRepeatedStringParsing(context, inputFile, content);
            codeSmellDetector.detectMissingCompilationTarget(context, inputFile, content);

            // New Bug detectors (15 rules)
            bugDetector.detectMissingEmptyListCheck(context, inputFile, content);
            bugDetector.detectMachinePrecisionInSymbolic(context, inputFile, content);
            bugDetector.detectMissingFailedCheck(context, inputFile, content);
            bugDetector.detectZeroDenominator(context, inputFile, content);
            bugDetector.detectMissingMatrixDimensionCheck(context, inputFile, content);
            bugDetector.detectIncorrectSetInScoping(context, inputFile, content);
            bugDetector.detectMissingHoldAttributes(context, inputFile, content);
            bugDetector.detectEvaluationOrderAssumption(context, inputFile, content);
            bugDetector.detectIncorrectLevelSpecification(context, inputFile, content);
            bugDetector.detectUnpackingPackedArrays(context, inputFile, content);
            bugDetector.detectMissingSpecialCaseHandling(context, inputFile, content);
            bugDetector.detectIncorrectAssociationOperations(context, inputFile, content);
            bugDetector.detectDateObjectValidation(context, inputFile, content);
            bugDetector.detectTotalMeanOnNonNumeric(context, inputFile, content);
            bugDetector.detectQuantityUnitMismatch(context, inputFile, content);

            // New Vulnerability detectors (7 rules)
            vulnerabilityDetector.detectToExpressionOnInput(context, inputFile, content);
            vulnerabilityDetector.detectUnsanitizedRunProcess(context, inputFile, content);
            vulnerabilityDetector.detectMissingCloudAuth(context, inputFile, content);
            vulnerabilityDetector.detectHardcodedApiKeys(context, inputFile, content);
            vulnerabilityDetector.detectNeedsGetUntrusted(context, inputFile, content);
            vulnerabilityDetector.detectExposingSensitiveData(context, inputFile, content);
            vulnerabilityDetector.detectMissingFormFunctionValidation(context, inputFile, content);

            // ===== CHUNK 1 DETECTORS (Items 16-50 from ROADMAP_325.md) =====

            // Pattern System Rules (Items 16-30)
            chunk1Detector.detectUnrestrictedBlankPattern(context, inputFile, content);
            chunk1Detector.detectPatternTestVsCondition(context, inputFile, content);
            chunk1Detector.detectBlankSequenceWithoutRestriction(context, inputFile, content);
            chunk1Detector.detectNestedOptionalPatterns(context, inputFile, content);
            chunk1Detector.detectPatternNamingConflicts(context, inputFile, content);
            chunk1Detector.detectRepeatedPatternAlternatives(context, inputFile, content);
            chunk1Detector.detectPatternTestWithPureFunction(context, inputFile, content);
            chunk1Detector.detectMissingPatternDefaults(context, inputFile, content);
            chunk1Detector.detectOrderDependentPatterns(context, inputFile, content);
            chunk1Detector.detectVerbatimPatternMisuse(context, inputFile, content);
            chunk1Detector.detectHoldPatternUnnecessary(context, inputFile, content);
            chunk1Detector.detectLongestShortestWithoutOrdering(context, inputFile, content);
            chunk1Detector.detectPatternRepeatedDifferentTypes(context, inputFile, content);
            chunk1Detector.detectAlternativesTooComplex(context, inputFile, content);
            chunk1Detector.detectPatternMatchingLargeLists(context, inputFile, content);

            // List/Array Rules (Items 31-40)
            chunk1Detector.detectEmptyListIndexing(context, inputFile, content);
            chunk1Detector.detectNegativeIndexWithoutValidation(context, inputFile, content);
            chunk1Detector.detectPartAssignmentToImmutable(context, inputFile, content);
            chunk1Detector.detectInefficientListConcatenation(context, inputFile, content);
            chunk1Detector.detectUnnecessaryFlatten(context, inputFile, content);
            chunk1Detector.detectLengthInLoopCondition(context, inputFile, content);
            chunk1Detector.detectReverseTwice(context, inputFile, content);
            chunk1Detector.detectSortWithoutComparison(context, inputFile, content);
            chunk1Detector.detectPositionVsSelect(context, inputFile, content);
            chunk1Detector.detectNestedPartExtraction(context, inputFile, content);

            // Association Rules (Items 41-50)
            chunk1Detector.detectMissingKeyCheck(context, inputFile, content);
            chunk1Detector.detectAssociationVsListConfusion(context, inputFile, content);
            chunk1Detector.detectInefficientKeyLookup(context, inputFile, content);
            chunk1Detector.detectQueryOnNonDataset(context, inputFile, content);
            chunk1Detector.detectAssociationUpdatePattern(context, inputFile, content);
            chunk1Detector.detectMergeWithoutConflictStrategy(context, inputFile, content);
            chunk1Detector.detectAssociateToOnNonSymbol(context, inputFile, content);
            chunk1Detector.detectKeyDropMultipleTimes(context, inputFile, content);
            chunk1Detector.detectLookupWithMissingDefault(context, inputFile, content);
            chunk1Detector.detectGroupByWithoutAggregation(context, inputFile, content);

            // ===== CHUNK 2 DETECTORS (Items 61-100 from ROADMAP_325.md) =====

            // Unused Code Detection (Items 61-75)
            chunk2Detector.detectUnusedPrivateFunction(context, inputFile, content);
            chunk2Detector.detectUnusedFunctionParameter(context, inputFile, content);
            chunk2Detector.detectUnusedModuleVariable(context, inputFile, content);
            chunk2Detector.detectUnusedWithVariable(context, inputFile, content);
            chunk2Detector.detectUnusedImport(context, inputFile, content);
            chunk2Detector.detectUnusedPatternName(context, inputFile, content);
            chunk2Detector.detectUnusedOptionalParameter(context, inputFile, content);
            chunk2Detector.detectDeadCodeAfterReturn(context, inputFile, content);
            chunk2Detector.detectUnreachableAfterAbortThrow(context, inputFile, content);
            chunk2Detector.detectAssignmentNeverRead(context, inputFile, content);
            chunk2Detector.detectFunctionDefinedButNeverCalled(context, inputFile, content);
            chunk2Detector.detectRedefinedWithoutUse(context, inputFile, content);
            chunk2Detector.detectLoopVariableUnused(context, inputFile, content);
            chunk2Detector.detectCatchWithoutThrow(context, inputFile, content);
            chunk2Detector.detectConditionAlwaysFalse(context, inputFile, content);

            // Shadowing & Naming (Items 76-90)
            chunk2Detector.detectLocalShadowsGlobal(context, inputFile, content);
            chunk2Detector.detectParameterShadowsBuiltin(context, inputFile, content);
            chunk2Detector.detectLocalShadowsParameter(context, inputFile, content);
            chunk2Detector.detectMultipleDefinitionsSameSymbol(context, inputFile, content);
            chunk2Detector.detectSymbolNameTooShort(context, inputFile, content);
            chunk2Detector.detectSymbolNameTooLong(context, inputFile, content);
            chunk2Detector.detectInconsistentNamingConvention(context, inputFile, content);
            chunk2Detector.detectBuiltinNameInLocalScope(context, inputFile, content);
            chunk2Detector.detectContextConflicts(context, inputFile, content);
            chunk2Detector.detectReservedNameUsage(context, inputFile, content);
            chunk2Detector.detectPrivateContextSymbolPublic(context, inputFile, content);
            chunk2Detector.detectMismatchedBeginEnd(context, inputFile, content);
            chunk2Detector.detectSymbolAfterEndPackage(context, inputFile, content);
            chunk2Detector.detectGlobalInPackage(context, inputFile, content);
            chunk2Detector.detectTempVariableNotTemp(context, inputFile, content);

            // Undefined Symbol Detection (Items 91-100)
            chunk2Detector.detectUndefinedFunctionCall(context, inputFile, content);
            chunk2Detector.detectUndefinedVariableReference(context, inputFile, content);
            chunk2Detector.detectTypoInBuiltinName(context, inputFile, content);
            chunk2Detector.detectWrongCapitalization(context, inputFile, content);
            chunk2Detector.detectMissingImport(context, inputFile, content);
            chunk2Detector.detectContextNotFound(context, inputFile, content);
            chunk2Detector.detectSymbolMaskedByImport(context, inputFile, content);
            chunk2Detector.detectMissingPathEntry(context, inputFile, content);
            chunk2Detector.detectCircularNeeds(context, inputFile, content);
            chunk2Detector.detectForwardReferenceWithoutDeclaration(context, inputFile, content);

            // ===== CHUNK 3 DETECTORS (Items 111-150 from ROADMAP_325.md) =====

            // Type Mismatch Detection (Items 111-130)
            chunk3Detector.detectNumericOperationOnString(context, inputFile, content);
            chunk3Detector.detectStringOperationOnNumber(context, inputFile, content);
            chunk3Detector.detectWrongArgumentType(context, inputFile, content);
            chunk3Detector.detectFunctionReturnsWrongType(context, inputFile, content);
            chunk3Detector.detectComparisonIncompatibleTypes(context, inputFile, content);
            chunk3Detector.detectMixedNumericTypes(context, inputFile, content);
            chunk3Detector.detectIntegerDivisionExpectingReal(context, inputFile, content);
            chunk3Detector.detectListFunctionOnAssociation(context, inputFile, content);
            chunk3Detector.detectPatternTypeMismatch(context, inputFile, content);
            chunk3Detector.detectOptionalTypeInconsistent(context, inputFile, content);
            chunk3Detector.detectReturnTypeInconsistent(context, inputFile, content);
            chunk3Detector.detectNullAssignmentToTypedVariable(context, inputFile, content);
            chunk3Detector.detectTypeCastWithoutValidation(context, inputFile, content);
            chunk3Detector.detectImplicitTypeConversion(context, inputFile, content);
            chunk3Detector.detectGraphicsObjectInNumericContext(context, inputFile, content);
            chunk3Detector.detectSymbolInNumericContext(context, inputFile, content);
            chunk3Detector.detectImageOperationOnNonImage(context, inputFile, content);
            chunk3Detector.detectSoundOperationOnNonSound(context, inputFile, content);
            chunk3Detector.detectDatasetOperationOnList(context, inputFile, content);
            chunk3Detector.detectGraphOperationOnNonGraph(context, inputFile, content);

            // Data Flow Analysis (Items 135-150)
            chunk3Detector.detectUninitializedVariableUseEnhanced(context, inputFile, content);
            chunk3Detector.detectVariableMayBeUninitialized(context, inputFile, content);
            chunk3Detector.detectDeadStore(context, inputFile, content);
            chunk3Detector.detectOverwrittenBeforeRead(context, inputFile, content);
            chunk3Detector.detectVariableAliasingIssue(context, inputFile, content);
            chunk3Detector.detectModificationOfLoopIterator(context, inputFile, content);
            chunk3Detector.detectUseOfIteratorOutsideLoop(context, inputFile, content);
            chunk3Detector.detectReadingUnsetVariable(context, inputFile, content);
            chunk3Detector.detectDoubleAssignmentSameValue(context, inputFile, content);
            chunk3Detector.detectMutationInPureFunction(context, inputFile, content);
            chunk3Detector.detectSharedMutableState(context, inputFile, content);
            chunk3Detector.detectVariableScopeEscape(context, inputFile, content);
            chunk3Detector.detectClosureOverMutableVariable(context, inputFile, content);
            chunk3Detector.detectAssignmentInConditionEnhanced(context, inputFile, content);
            chunk3Detector.detectAssignmentAsReturnValue(context, inputFile, content);
            chunk3Detector.detectVariableNeverModified(context, inputFile, content);

            // ===== CHUNK 4 DETECTORS (Items 161-200 from ROADMAP_325.md) =====

            // Dead Code & Reachability (Items 161-175)
            chunk4Detector.detectUnreachableCodeAfterReturn(context, inputFile, content);
            chunk4Detector.detectUnreachableBranchAlwaysTrue(context, inputFile, content);
            chunk4Detector.detectUnreachableBranchAlwaysFalse(context, inputFile, content);
            chunk4Detector.detectImpossiblePattern(context, inputFile, content);
            chunk4Detector.detectEmptyCatchBlockEnhanced(context, inputFile, content);
            chunk4Detector.detectConditionAlwaysEvaluatesSame(context, inputFile, content);
            chunk4Detector.detectInfiniteLoopProven(context, inputFile, content);
            chunk4Detector.detectLoopNeverExecutes(context, inputFile, content);
            chunk4Detector.detectCodeAfterAbort(context, inputFile, content);
            chunk4Detector.detectMultipleReturnsMakeCodeUnreachable(context, inputFile, content);
            chunk4Detector.detectElseBranchNeverTaken(context, inputFile, content);
            chunk4Detector.detectSwitchCaseShadowed(context, inputFile, content);
            chunk4Detector.detectPatternDefinitionShadowed(context, inputFile, content);
            chunk4Detector.detectExceptionNeverThrown(context, inputFile, content);
            chunk4Detector.detectBreakOutsideLoop(context, inputFile, content);

            // Taint Analysis for Security (Items 181-195)
            chunk4Detector.detectSqlInjectionTaint(context, inputFile, content);
            chunk4Detector.detectCommandInjectionTaint(context, inputFile, content);
            chunk4Detector.detectCodeInjectionTaint(context, inputFile, content);
            chunk4Detector.detectPathTraversalTaint(context, inputFile, content);
            chunk4Detector.detectXssTaint(context, inputFile, content);
            chunk4Detector.detectLdapInjection(context, inputFile, content);
            chunk4Detector.detectXxeTaint(context, inputFile, content);
            chunk4Detector.detectUnsafeDeserializationTaint(context, inputFile, content);
            chunk4Detector.detectSsrfTaint(context, inputFile, content);
            chunk4Detector.detectInsecureRandomnessEnhanced(context, inputFile, content);
            chunk4Detector.detectWeakCryptographyEnhanced(context, inputFile, content);
            chunk4Detector.detectHardCodedCredentialsTaint(context, inputFile, content);
            chunk4Detector.detectSensitiveDataInLogs(context, inputFile, content);
            chunk4Detector.detectMassAssignment(context, inputFile, content);
            chunk4Detector.detectRegexDoS(context, inputFile, content);

            // Additional Control Flow Rules (Items 196-200)
            chunk4Detector.detectMissingDefaultCase(context, inputFile, content);
            chunk4Detector.detectEmptyIfBranch(context, inputFile, content);
            chunk4Detector.detectNestedIfDepth(context, inputFile, content);
            chunk4Detector.detectTooManyReturnPoints(context, inputFile, content);
            chunk4Detector.detectMissingElseConsideredHarmful(context, inputFile, content);

            // ===== CHUNK 5 DETECTORS (Items 211-250 from ROADMAP_325.md) =====

            // Dependency & Architecture Rules (Items 211-230)
            Chunk5Detector.detectCircularPackageDependency(context, inputFile, content);
            Chunk5Detector.detectUnusedPackageImport(context, inputFile, content);
            Chunk5Detector.detectMissingPackageImport(context, inputFile, content);
            Chunk5Detector.detectTransitiveDependencyCouldBeDirect(context, inputFile, content);
            Chunk5Detector.detectDiamondDependency(context, inputFile, content);
            Chunk5Detector.detectGodPackageTooManyDependencies(context, inputFile, content);
            Chunk5Detector.detectPackageDependsOnApplicationCode(context, inputFile, content);
            Chunk5Detector.detectCyclicCallBetweenPackages(context, inputFile, content);
            Chunk5Detector.detectLayerViolation(context, inputFile, content);
            Chunk5Detector.detectUnstableDependency(context, inputFile, content);
            Chunk5Detector.detectPackageTooLarge(context, inputFile, content);
            Chunk5Detector.detectPackageTooSmall(context, inputFile, content);
            Chunk5Detector.detectInconsistentPackageNaming(context, inputFile, content);
            Chunk5Detector.detectPackageExportsTooMuch(context, inputFile, content);
            Chunk5Detector.detectPackageExportsTooLittle(context, inputFile, content);
            Chunk5Detector.detectIncompletePublicAPI(context, inputFile, content);
            Chunk5Detector.detectPrivateSymbolUsedExternally(context, inputFile, content);
            Chunk5Detector.detectInternalImplementationExposed(context, inputFile, content);
            Chunk5Detector.detectMissingPackageDocumentation(context, inputFile, content);
            Chunk5Detector.detectPublicAPIChangedWithoutVersionBump(context, inputFile, content);

            // Unused Export & Dead Code (Items 231-245)
            Chunk5Detector.detectUnusedPublicFunction(context, inputFile, content);
            Chunk5Detector.detectUnusedExport(context, inputFile, content);
            Chunk5Detector.detectDeadPackage(context, inputFile, content);
            Chunk5Detector.detectFunctionOnlyCalledOnce(context, inputFile, content);
            Chunk5Detector.detectOverAbstractedAPI(context, inputFile, content);
            Chunk5Detector.detectOrphanedTestFile(context, inputFile, content);
            Chunk5Detector.detectImplementationWithoutTests(context, inputFile, content);
            Chunk5Detector.detectDeprecatedAPIStillUsedInternally(context, inputFile, content);
            Chunk5Detector.detectInternalAPIUsedLikePublic(context, inputFile, content);
            Chunk5Detector.detectCommentedOutPackageLoad(context, inputFile, content);
            Chunk5Detector.detectConditionalPackageLoad(context, inputFile, content);
            Chunk5Detector.detectPackageLoadedButNotListedInMetadata(context, inputFile, content);
            Chunk5Detector.detectDuplicateSymbolDefinitionAcrossPackages(context, inputFile, content);
            Chunk5Detector.detectSymbolRedefinitionAfterImport(context, inputFile, content);
            Chunk5Detector.detectPackageVersionMismatch(context, inputFile, content);

            // Documentation & Consistency (Items 246-250)
            Chunk5Detector.detectPublicExportMissingUsageMessage(context, inputFile, content);
            Chunk5Detector.detectInconsistentParameterNamesAcrossOverloads(context, inputFile, content);
            Chunk5Detector.detectPublicFunctionWithImplementationDetailsInName(context, inputFile, content);
            Chunk5Detector.detectPublicAPINotInPackageContext(context, inputFile, content);
            Chunk5Detector.detectTestFunctionInProductionCode(context, inputFile, content);

            // ===== CHUNK 6 & 7 DETECTORS (Items 251-325 from ROADMAP_325.md) =====

            // Null Safety & Error Handling (Items 251-266)
            Chunk67Detector.detectNullDereference(context, inputFile, content);
            Chunk67Detector.detectMissingNullCheck(context, inputFile, content);
            Chunk67Detector.detectNullPassedToNonNullable(context, inputFile, content);
            Chunk67Detector.detectInconsistentNullHandling(context, inputFile, content);
            Chunk67Detector.detectNullReturnNotDocumented(context, inputFile, content);
            Chunk67Detector.detectComparisonWithNull(context, inputFile, content);
            Chunk67Detector.detectMissingCheckLeadsToNullPropagation(context, inputFile, content);
            Chunk67Detector.detectCheckPatternDoesntHandleAllCases(context, inputFile, content);
            Chunk67Detector.detectQuietSuppressingImportantMessages(context, inputFile, content);
            Chunk67Detector.detectOffDisablingImportantWarnings(context, inputFile, content);
            Chunk67Detector.detectCatchAllExceptionHandler(context, inputFile, content);
            Chunk67Detector.detectEmptyExceptionHandler(context, inputFile, content);
            Chunk67Detector.detectThrowWithoutCatch(context, inputFile, content);
            Chunk67Detector.detectAbortInLibraryCode(context, inputFile, content);
            Chunk67Detector.detectMessageWithoutDefinition(context, inputFile, content);
            Chunk67Detector.detectMissingMessageDefinition(context, inputFile, content);

            // Constant & Expression Analysis (Items 267-280)
            Chunk67Detector.detectConditionAlwaysTrueConstantPropagation(context, inputFile, content);
            Chunk67Detector.detectConditionAlwaysFalseConstantPropagation(context, inputFile, content);
            Chunk67Detector.detectLoopBoundConstant(context, inputFile, content);
            Chunk67Detector.detectRedundantComputation(context, inputFile, content);
            Chunk67Detector.detectPureExpressionInLoop(context, inputFile, content);
            Chunk67Detector.detectConstantExpression(context, inputFile, content);
            Chunk67Detector.detectIdentityOperation(context, inputFile, content);
            Chunk67Detector.detectComparisonOfIdenticalExpressions(context, inputFile, content);
            Chunk67Detector.detectBooleanExpressionAlwaysTrue(context, inputFile, content);
            Chunk67Detector.detectBooleanExpressionAlwaysFalse(context, inputFile, content);
            Chunk67Detector.detectUnnecessaryBooleanConversion(context, inputFile, content);
            Chunk67Detector.detectDoubleNegation(context, inputFile, content);
            Chunk67Detector.detectComplexBooleanExpressionEnhanced(context, inputFile, content);
            Chunk67Detector.detectDeMorgansLawOpportunity(context, inputFile, content);

            // Mathematica-Specific Patterns (Items 281-300)
            Chunk67Detector.detectHoldAttributeMissing(context, inputFile, content);
            Chunk67Detector.detectHoldFirstButUsesSecondArgumentFirst(context, inputFile, content);
            Chunk67Detector.detectMissingUnevaluatedWrapper(context, inputFile, content);
            Chunk67Detector.detectUnnecessaryHold(context, inputFile, content);
            Chunk67Detector.detectReleaseHoldAfterHold(context, inputFile, content);
            Chunk67Detector.detectEvaluateInHeldContext(context, inputFile, content);
            Chunk67Detector.detectPatternWithSideEffect(context, inputFile, content);
            Chunk67Detector.detectReplacementRuleOrderMatters(context, inputFile, content);
            Chunk67Detector.detectReplaceAllVsReplaceConfusion(context, inputFile, content);
            Chunk67Detector.detectRuleDoesntMatchDueToEvaluation(context, inputFile, content);
            Chunk67Detector.detectPartSpecificationOutOfBounds(context, inputFile, content);
            Chunk67Detector.detectSpanSpecificationInvalid(context, inputFile, content);
            Chunk67Detector.detectAllSpecificationInefficient(context, inputFile, content);
            Chunk67Detector.detectThreadingOverNonLists(context, inputFile, content);
            Chunk67Detector.detectMissingAttributesDeclaration(context, inputFile, content);
            Chunk67Detector.detectOneIdentityAttributeMisuse(context, inputFile, content);
            Chunk67Detector.detectOrderlessAttributeOnNonCommutative(context, inputFile, content);
            Chunk67Detector.detectFlatAttributeMisuse(context, inputFile, content);
            Chunk67Detector.detectSequenceInUnexpectedContext(context, inputFile, content);
            Chunk67Detector.detectMissingSequenceWrapper(context, inputFile, content);

            // Test Coverage Integration (Items 307-310)
            Chunk67Detector.detectLowTestCoverageWarning(context, inputFile, content);
            Chunk67Detector.detectUntestedPublicFunction(context, inputFile, content);
            Chunk67Detector.detectUntestedBranch(context, inputFile, content);
            Chunk67Detector.detectTestOnlyCodeInProduction(context, inputFile, content);

            // Performance Analysis (Items 312-320)
            Chunk67Detector.detectCompilableFunctionNotCompiled(context, inputFile, content);
            Chunk67Detector.detectCompilationTargetMissing(context, inputFile, content);
            Chunk67Detector.detectNonCompilableConstructInCompile(context, inputFile, content);
            Chunk67Detector.detectPackedArrayUnpacked(context, inputFile, content);
            Chunk67Detector.detectInefficientPatternInPerformanceCriticalCode(context, inputFile, content);
            Chunk67Detector.detectNAppliedTooLate(context, inputFile, content);
            Chunk67Detector.detectMissingMemoizationOpportunityEnhanced(context, inputFile, content);
            Chunk67Detector.detectInefficientStringConcatenationEnhanced(context, inputFile, content);
            Chunk67Detector.detectListConcatenationInLoop(context, inputFile, content);

            // ===== SYMBOL TABLE ANALYSIS (10 rules) =====
            // Build symbol table for advanced variable lifetime and scope analysis
            try {
                SymbolTable symbolTable = SymbolTableBuilder.build(inputFile, content);

                // Run symbol table-based detectors
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

                // Advanced symbol table analysis (10 additional rules)
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
            } catch (Exception e) {
                LOG.debug("Error in symbol table analysis for: {}", inputFile.filename());
            }

            // Clear caches after processing file
            codeSmellDetector.clearCaches();
            bugDetector.clearCaches();
            vulnerabilityDetector.clearCaches();
            securityHotspotDetector.clearCaches();
            chunk1Detector.clearCaches();
            chunk2Detector.clearCaches();
            chunk3Detector.clearCaches();
            chunk4Detector.clearCaches();
            // Note: Chunk5Detector uses static caches cleared at end of execute()

        } catch (Exception e) {
            LOG.error("Error analyzing file: {}", inputFile, e);
        }
    }

    /**
     * Detects files that are too long.
     * This check runs even for very large files to ensure they're reported.
     */
    private void detectLongFile(SensorContext context, InputFile inputFile) {
        try {
            // Get threshold from configuration
            int maxLines = context.config()
                .getInt("sonar.mathematica.file.maximumLines")
                .orElse(1000);

            int lineCount = inputFile.lines();

            if (lineCount > maxLines) {
                reportIssue(context, inputFile, 1,
                    MathematicaRulesDefinition.FILE_LENGTH_KEY,
                    String.format("File has %d lines, which exceeds the maximum of %d.",
                        lineCount, maxLines));
            }
        } catch (Exception e) {
            LOG.warn("Skipping file length detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Analyzes comments in the file, detecting commented code and TODO/FIXME comments.
     * Returns comment ranges for use by other detectors.
     *
     * This is a single-pass optimization: parse comments once, extract issues, and
     * return ranges for other detectors to skip comment content.
     */
    private List<int[]> analyzeComments(SensorContext context, InputFile inputFile, String content) {
        List<int[]> commentRanges = new ArrayList<>();

        try {
            Matcher matcher = COMMENT_PATTERN.matcher(content);

            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                commentRanges.add(new int[]{start, end});

                String commentText = matcher.group().substring(2, matcher.group().length() - 2).trim();

                // Detect commented code
                if (isCommentedCode(commentText)) {
                    int line = calculateLineNumber(content, start);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.COMMENTED_CODE_KEY,
                        "Remove this commented out code.");
                }

                // Detect TODO/FIXME comments
                String lowerComment = commentText.toLowerCase();
                if (lowerComment.contains("todo") || lowerComment.contains("fixme")) {
                    int line = calculateLineNumber(content, start);
                    String issueType = lowerComment.contains("fixme") ? "FIXME" : "TODO";
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.TODO_FIXME_KEY,
                        String.format("Take action on this %s comment.", issueType));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping comment analysis due to error in file: {}", inputFile.filename());
        }

        return commentRanges;
    }

    /**
     * Checks if a comment looks like commented-out code vs natural language.
     * Uses heuristics: presence of assignments, function calls, operators, keywords.
     */
    private static final String[] NATURAL_LANGUAGE_PHRASES = {
        "this is", "this function", "this will", "this should",
        "note that", "hack", "bug",
        "returns", "calculates", "computes", "sets", "gets",
        "the following", "for example", "such as"
    };

    private boolean isCommentedCode(String commentText) {
        // Skip very short comments
        if (commentText.length() < 10) {
            return false;
        }

        // Check for natural language indicators
        String lowerText = commentText.toLowerCase();
        int naturalLanguageIndicators = 0;

        for (String phrase : NATURAL_LANGUAGE_PHRASES) {
            if (lowerText.contains(phrase)) {
                naturalLanguageIndicators++;
                if (naturalLanguageIndicators >= 2) {
                    return false;  // Likely natural language, not code
                }
            }
        }

        // Check for code indicators
        int codeIndicators = 0;

        // Check for assignments
        if (commentText.matches(".*\\w+\\s*:?=\\s*[^=].*")) {
            codeIndicators++;
        }

        // Check for function calls
        if (commentText.matches(".*[a-zA-Z]\\w*\\s*\\[.*")) {
            codeIndicators++;
        }

        // Check for mathematical operators
        if (commentText.matches(".*[-+*/^]\\s*[a-zA-Z0-9].*")) {
            codeIndicators++;
        }

        // Check for Mathematica keywords
        if (commentText.matches(".*\\b(?:Module|Block|With|Table|Map|Apply|Function|If|While|Do|For|Return|Print|Plot|Solve)\\s*\\[.*")) {
            codeIndicators += 2;
        }

        // If we have multiple code indicators and few natural language indicators, it's likely code
        return codeIndicators >= 2;
    }

    /**
     * Calculates line number from character offset using simple linear scan.
     * Note: Detectors use cached O(log n) version from BaseDetector for better performance.
     */
    private int calculateLineNumber(String content, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    /**
     * Reports an issue at a specific line.
     */
    private void reportIssue(SensorContext context, InputFile inputFile, int line, String ruleKey, String message) {
        NewIssue issue = context.newIssue()
            .forRule(RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY, ruleKey));

        NewIssueLocation location = issue.newLocation()
            .on(inputFile)
            .at(inputFile.selectLine(line))
            .message(message);

        issue.at(location);
        issue.save();
    }
}
