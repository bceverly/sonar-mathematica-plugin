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

    // Track files that skip vulnerability detection due to size
    private final List<String> skippedVulnFiles = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final int MAX_FILE_SIZE_FOR_VULN_DETECTION = 50_000; // lines

    // Comment pattern for comment analysis
    private static final Pattern COMMENT_PATTERN = Pattern.compile("\\(\\*[\\s\\S]*?\\*\\)");

    // PERFORMANCE: Queue issue DATA (not NewIssue objects) to avoid thread-safety issues
    private static class IssueData {
        final InputFile inputFile;
        final int line;
        final String ruleKey;
        final String message;

        IssueData(InputFile inputFile, int line, String ruleKey, String message) {
            this.inputFile = inputFile;
            this.line = line;
            this.ruleKey = ruleKey;
            this.message = message;
        }
    }

    private final java.util.concurrent.BlockingQueue<IssueData> issueQueue =
        new java.util.concurrent.LinkedBlockingQueue<>();
    private volatile Thread issueSaverThread;
    private volatile boolean shutdownSaver = false;
    private final java.util.concurrent.atomic.AtomicLong queuedIssues = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong savedIssues = new java.util.concurrent.atomic.AtomicLong(0);
    private SensorContext sensorContext; // Store context for saver thread

    // Thread-local detector instances for parallel processing
    private final ThreadLocal<CodeSmellDetector> codeSmellDetector = ThreadLocal.withInitial(() -> {
        CodeSmellDetector d = new CodeSmellDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<BugDetector> bugDetector = ThreadLocal.withInitial(() -> {
        BugDetector d = new BugDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<VulnerabilityDetector> vulnerabilityDetector = ThreadLocal.withInitial(() -> {
        VulnerabilityDetector d = new VulnerabilityDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<SecurityHotspotDetector> securityHotspotDetector = ThreadLocal.withInitial(() -> {
        SecurityHotspotDetector d = new SecurityHotspotDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<PatternAndDataStructureDetector> patternAndDataStructureDetector = ThreadLocal.withInitial(() -> {
        PatternAndDataStructureDetector d = new PatternAndDataStructureDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<UnusedAndNamingDetector> unusedAndNamingDetector = ThreadLocal.withInitial(() -> {
        UnusedAndNamingDetector d = new UnusedAndNamingDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<TypeAndDataFlowDetector> typeAndDataFlowDetector = ThreadLocal.withInitial(() -> {
        TypeAndDataFlowDetector d = new TypeAndDataFlowDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<ControlFlowAndTaintDetector> controlFlowAndTaintDetector = ThreadLocal.withInitial(() -> {
        ControlFlowAndTaintDetector d = new ControlFlowAndTaintDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<ArchitectureAndDependencyDetector> architectureAndDependencyDetector =
        ThreadLocal.withInitial(ArchitectureAndDependencyDetector::new);
    private final ThreadLocal<AdvancedAnalysisDetector> advancedAnalysisDetector =
        ThreadLocal.withInitial(AdvancedAnalysisDetector::new);

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("Mathematica Rules Sensor")
            .onlyOnLanguage(MathematicaLanguage.KEY);
    }

    /**
     * Queues issue DATA (not NewIssue object) to be created and saved by the background thread.
     * This eliminates lock contention AND thread-safety issues.
     */
    public void queueIssue(InputFile inputFile, int line, String ruleKey, String message) {
        try {
            issueQueue.put(new IssueData(inputFile, line, ruleKey, message));
            queuedIssues.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while queueing issue", e);
        }
    }

    /**
     * Starts the background thread that creates and saves issues from queued data.
     */
    private void startIssueSaverThread(SensorContext context) {
        this.sensorContext = context;
        shutdownSaver = false;
        issueSaverThread = new Thread(() -> {
            LOG.info("Issue saver thread started");
            try {
                while (!shutdownSaver || !issueQueue.isEmpty()) {
                    IssueData data = issueQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (data != null) {
                        long saveStart = System.currentTimeMillis();

                        // Create and save issue on THIS thread (saver thread)
                        org.sonar.api.batch.sensor.issue.NewIssue issue = context.newIssue()
                            .forRule(RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY, data.ruleKey));

                        org.sonar.api.batch.sensor.issue.NewIssueLocation location = issue.newLocation()
                            .on(data.inputFile)
                            .at(data.inputFile.selectLine(data.line))
                            .message(data.message);

                        issue.at(location);
                        issue.save();

                        long saveDuration = System.currentTimeMillis() - saveStart;
                        savedIssues.incrementAndGet();

                        if (saveDuration > 10) {
                            LOG.info("⚠ SLOW SAVE: Issue #{} took {}ms (queue: {})",
                                savedIssues.get(), saveDuration, issueQueue.size());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.error("Issue saver thread interrupted", e);
            } catch (Exception e) {
                LOG.error("Error in issue saver thread", e);
            }
            LOG.info("Issue saver thread finished. Saved {}/{} issues", savedIssues.get(), queuedIssues.get());
        }, "MathematicaIssueSaver");

        issueSaverThread.setDaemon(false); // Ensure it completes before shutdown
        issueSaverThread.start();
    }

    /**
     * Stops the background saver thread and waits for queue to drain.
     */
    private void stopIssueSaverThread() {
        LOG.info("Stopping issue saver thread (queue size: {}, saved: {}/{})",
            issueQueue.size(), savedIssues.get(), queuedIssues.get());
        shutdownSaver = true;

        try {
            if (issueSaverThread != null) {
                issueSaverThread.join(60000); // Wait up to 1 minute
                if (issueSaverThread.isAlive()) {
                    LOG.warn("Issue saver thread did not finish in time");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted waiting for issue saver thread", e);
        }
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

        // Start background issue saver thread
        startIssueSaverThread(context);

        LOG.info("Starting analysis of {} Mathematica file(s)...", totalFiles);
        LOG.info("Using parallel processing with {} threads (thread-local caching enabled)", Runtime.getRuntime().availableProcessors());
        LOG.info("Progress will be reported every 100 files");

        long startTime = System.currentTimeMillis();

        // === PHASE 1: Build cross-file analysis data for Chunk5 ===
        LOG.info("Phase 1: Building cross-file dependency graph...");
        ArchitectureAndDependencyDetector.initializeCaches();

        fileList.stream().forEach(inputFile -> {
            try {
                if (inputFile.lines() < 3 || inputFile.lines() > 35000) return;
                String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);
                if (content.length() > 2_000_000 || content.trim().isEmpty()) return;

                ArchitectureAndDependencyDetector.buildCrossFileData(inputFile, content);
            } catch (Exception e) {
                LOG.debug("Error building cross-file data for: {}", inputFile.filename());
            }
        });

        LOG.info("Phase 1 complete. Starting Phase 2: Rule detection...");

        // === PHASE 2: Run all detectors ===
        java.util.concurrent.atomic.AtomicInteger processedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        int progressInterval = 100; // Log every 100 files

        // Use parallel stream with thread-local detector instances for correct ThreadLocal caching
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

        // Report files that skipped vulnerability detection
        if (!skippedVulnFiles.isEmpty()) {
            LOG.warn("=== SKIPPED VULNERABILITY DETECTION ({} files) ===", skippedVulnFiles.size());
            for (String file : skippedVulnFiles) {
                LOG.warn("  - {}", file);
            }
            LOG.warn("Reason: Files >{}K lines skipped for performance", MAX_FILE_SIZE_FOR_VULN_DETECTION / 1000);
        }

        // Clean up cross-file analysis caches
        ArchitectureAndDependencyDetector.clearCaches();
        SymbolTableManager.clear();

        // Stop issue saver thread and wait for queue to drain
        stopIssueSaverThread();

        LOG.info("Cleared all static caches - ready for next scan");
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
        long fileStartTime = System.currentTimeMillis();
        int issueCountBefore = 0;

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

            long readStartTime = System.currentTimeMillis();
            String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);
            long readTime = System.currentTimeMillis() - readStartTime;

            // File read timing removed - too noisy

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
            codeSmellDetector.get().initializeCaches(content);
            bugDetector.get().initializeCaches(content);
            vulnerabilityDetector.get().initializeCaches(content);
            securityHotspotDetector.get().initializeCaches(content);
            patternAndDataStructureDetector.get().initializeCaches(content);
            unusedAndNamingDetector.get().initializeCaches(content);
            typeAndDataFlowDetector.get().initializeCaches(content);
            controlFlowAndTaintDetector.get().initializeCaches(content);

            // Analyze comments once and cache for reuse
            List<int[]> commentRanges = analyzeComments(context, inputFile, content);

            // === TIMING: Code Smell Detectors ===
            long codeSmellStart = System.currentTimeMillis();

            // Delegate to Code Smell detector (33 rules)
            codeSmellDetector.get().detectMagicNumbers(context, inputFile, content, commentRanges);
            codeSmellDetector.get().detectEmptyBlocks(context, inputFile, content);
            codeSmellDetector.get().detectLongFunctions(context, inputFile, content);
            codeSmellDetector.get().detectEmptyCatchBlocks(context, inputFile, content);
            codeSmellDetector.get().detectDebugCode(context, inputFile, content);
            codeSmellDetector.get().detectUnusedVariables(context, inputFile, content);
            codeSmellDetector.get().detectDuplicateFunctions(context, inputFile, content);
            codeSmellDetector.get().detectTooManyParameters(context, inputFile, content);
            codeSmellDetector.get().detectDeeplyNested(context, inputFile, content);
            codeSmellDetector.get().detectMissingDocumentation(context, inputFile, content);
            codeSmellDetector.get().detectInconsistentNaming(context, inputFile, content);
            codeSmellDetector.get().detectIdenticalBranches(context, inputFile, content);
            codeSmellDetector.get().detectExpressionTooComplex(context, inputFile, content, commentRanges);
            codeSmellDetector.get().detectDeprecatedFunctions(context, inputFile, content);
            codeSmellDetector.get().detectEmptyStatement(context, inputFile, content);

            // Performance rules (Code Smell)
            codeSmellDetector.get().detectAppendInLoop(context, inputFile, content);
            codeSmellDetector.get().detectRepeatedFunctionCalls(context, inputFile, content);
            codeSmellDetector.get().detectStringConcatInLoop(context, inputFile, content);
            codeSmellDetector.get().detectUncompiledNumerical(context, inputFile, content);
            codeSmellDetector.get().detectPackedArrayBreaking(context, inputFile, content);
            codeSmellDetector.get().detectNestedMapTable(context, inputFile, content);
            codeSmellDetector.get().detectLargeTempExpressions(context, inputFile, content);
            codeSmellDetector.get().detectPlotInLoop(context, inputFile, content);

            // Best practices rules (Code Smell)
            codeSmellDetector.get().detectGenericVariableNames(context, inputFile, content);
            codeSmellDetector.get().detectMissingUsageMessage(context, inputFile, content);
            codeSmellDetector.get().detectMissingOptionsPattern(context, inputFile, content);
            codeSmellDetector.get().detectSideEffectsNaming(context, inputFile, content);
            codeSmellDetector.get().detectComplexBoolean(context, inputFile, content);
            codeSmellDetector.get().detectUnprotectedSymbols(context, inputFile, content);
            codeSmellDetector.get().detectMissingReturn(context, inputFile, content);

            long codeSmellTime = System.currentTimeMillis() - codeSmellStart;

            // === TIMING: Bug Detectors ===
            long bugStart = System.currentTimeMillis();

            // Delegate to Bug detector (20 rules)
            bugDetector.get().detectDivisionByZero(context, inputFile, content);
            bugDetector.get().detectAssignmentInConditional(context, inputFile, content);
            bugDetector.get().detectListIndexOutOfBounds(context, inputFile, content);
            bugDetector.get().detectInfiniteRecursion(context, inputFile, content);
            bugDetector.get().detectUnreachablePatterns(context, inputFile, content);
            bugDetector.get().detectFloatingPointEquality(context, inputFile, content);
            bugDetector.get().detectFunctionWithoutReturn(context, inputFile, content);
            bugDetector.get().detectVariableBeforeAssignment(context, inputFile, content);
            bugDetector.get().detectOffByOne(context, inputFile, content);
            bugDetector.get().detectInfiniteLoop(context, inputFile, content);
            bugDetector.get().detectMismatchedDimensions(context, inputFile, content);
            bugDetector.get().detectTypeMismatch(context, inputFile, content);
            bugDetector.get().detectSuspiciousPattern(context, inputFile, content);

            // Pattern matching bugs
            bugDetector.get().detectMissingPatternTest(context, inputFile, content);
            bugDetector.get().detectPatternBlanksMisuse(context, inputFile, content);
            bugDetector.get().detectSetDelayedConfusion(context, inputFile, content);
            bugDetector.get().detectSymbolNameCollision(context, inputFile, content);
            bugDetector.get().detectBlockModuleMisuse(context, inputFile, content);

            // Resource management bugs
            bugDetector.get().detectUnclosedFileHandle(context, inputFile, content);
            bugDetector.get().detectGrowingDefinitionChain(context, inputFile, content);

            long bugTime = System.currentTimeMillis() - bugStart;

            // === TIMING: Vulnerability Detectors ===
            long vulnStart = System.currentTimeMillis();

            // Check if file should skip vulnerability detection due to size
            boolean skipVulnDetection = shouldSkipVulnerabilityDetection(content, inputFile.filename());
            if (skipVulnDetection) {
                skippedVulnFiles.add(String.format("%s (%d lines)", inputFile.filename(), inputFile.lines()));
            }

            // Delegate to Vulnerability detector (14 rules) - with timing for slow rules
            long ruleStart;

            if (!skipVulnDetection) {

            logRuleStart("HardcodedCredentials");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectHardcodedCredentials(context, inputFile, content);
            logSlowRule(inputFile, "HardcodedCredentials", ruleStart);

            logRuleStart("CommandInjection");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectCommandInjection(context, inputFile, content);
            logSlowRule(inputFile, "CommandInjection", ruleStart);

            logRuleStart("SqlInjection");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectSqlInjection(context, inputFile, content);
            logSlowRule(inputFile, "SqlInjection", ruleStart);

            logRuleStart("CodeInjection");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectCodeInjection(context, inputFile, content);
            logSlowRule(inputFile, "CodeInjection", ruleStart);

            logRuleStart("PathTraversal");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectPathTraversal(context, inputFile, content);
            logSlowRule(inputFile, "PathTraversal", ruleStart);

            logRuleStart("WeakCryptography");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectWeakCryptography(context, inputFile, content);
            logSlowRule(inputFile, "WeakCryptography", ruleStart);

            logRuleStart("Ssrf");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectSsrf(context, inputFile, content);
            logSlowRule(inputFile, "Ssrf", ruleStart);

            logRuleStart("InsecureDeserialization");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectInsecureDeserialization(context, inputFile, content);
            logSlowRule(inputFile, "InsecureDeserialization", ruleStart);

            logRuleStart("UnsafeSymbol");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectUnsafeSymbol(context, inputFile, content);
            logSlowRule(inputFile, "UnsafeSymbol", ruleStart);

            logRuleStart("XXE");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectXXE(context, inputFile, content);
            logSlowRule(inputFile, "XXE", ruleStart);

            logRuleStart("MissingSanitization");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectMissingSanitization(context, inputFile, content);
            logSlowRule(inputFile, "MissingSanitization", ruleStart);

            logRuleStart("InsecureRandomExpanded");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectInsecureRandomExpanded(context, inputFile, content);
            logSlowRule(inputFile, "InsecureRandomExpanded", ruleStart);

            logRuleStart("UnsafeCloudDeploy");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectUnsafeCloudDeploy(context, inputFile, content);
            logSlowRule(inputFile, "UnsafeCloudDeploy", ruleStart);

            logRuleStart("DynamicInjection");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectDynamicInjection(context, inputFile, content);
            logSlowRule(inputFile, "DynamicInjection", ruleStart);

            // Delegate to Security Hotspot detector (7 rules)
            securityHotspotDetector.get().detectFileUploadValidation(context, inputFile, content);
            securityHotspotDetector.get().detectExternalApiSafeguards(context, inputFile, content);
            securityHotspotDetector.get().detectCryptoKeyGeneration(context, inputFile, content);
            securityHotspotDetector.get().detectNetworkOperations(context, inputFile, content);
            securityHotspotDetector.get().detectFileSystemModifications(context, inputFile, content);
            securityHotspotDetector.get().detectEnvironmentVariable(context, inputFile, content);
            securityHotspotDetector.get().detectImportWithoutFormat(context, inputFile, content);

            // ===== PHASE 4: NEW RULE DETECTORS (50 rules) =====

            // New Code Smell detectors (18 rules)
            codeSmellDetector.get().detectOvercomplexPatterns(context, inputFile, content);
            codeSmellDetector.get().detectInconsistentRuleTypes(context, inputFile, content);
            codeSmellDetector.get().detectMissingFunctionAttributes(context, inputFile, content);
            codeSmellDetector.get().detectMissingDownValuesDoc(context, inputFile, content);
            codeSmellDetector.get().detectMissingPatternTestValidation(context, inputFile, content);
            codeSmellDetector.get().detectExcessivePureFunctions(context, inputFile, content);
            codeSmellDetector.get().detectMissingOperatorPrecedence(context, inputFile, content);
            codeSmellDetector.get().detectHardcodedFilePaths(context, inputFile, content);
            codeSmellDetector.get().detectInconsistentReturnTypes(context, inputFile, content);
            codeSmellDetector.get().detectMissingErrorMessages(context, inputFile, content);
            codeSmellDetector.get().detectGlobalStateModification(context, inputFile, content);
            codeSmellDetector.get().detectMissingLocalization(context, inputFile, content);
            codeSmellDetector.get().detectExplicitGlobalContext(context, inputFile, content);
            codeSmellDetector.get().detectMissingTemporaryCleanup(context, inputFile, content);
            codeSmellDetector.get().detectNestedListsInsteadAssociation(context, inputFile, content);
            codeSmellDetector.get().detectRepeatedPartExtraction(context, inputFile, content);
            codeSmellDetector.get().detectMissingMemoization(context, inputFile, content);
            codeSmellDetector.get().detectStringJoinForTemplates(context, inputFile, content);

            // New Performance detectors (10 rules)
            codeSmellDetector.get().detectLinearSearchInsteadLookup(context, inputFile, content);
            codeSmellDetector.get().detectRepeatedCalculations(context, inputFile, content);
            codeSmellDetector.get().detectPositionInsteadPattern(context, inputFile, content);
            codeSmellDetector.get().detectFlattenTableAntipattern(context, inputFile, content);
            codeSmellDetector.get().detectMissingParallelization(context, inputFile, content);
            codeSmellDetector.get().detectMissingSparseArray(context, inputFile, content);
            codeSmellDetector.get().detectUnnecessaryTranspose(context, inputFile, content);
            codeSmellDetector.get().detectDeleteDuplicatesOnLargeData(context, inputFile, content);
            codeSmellDetector.get().detectRepeatedStringParsing(context, inputFile, content);
            codeSmellDetector.get().detectMissingCompilationTarget(context, inputFile, content);

            // New Bug detectors (15 rules)
            bugDetector.get().detectMissingEmptyListCheck(context, inputFile, content);
            bugDetector.get().detectMachinePrecisionInSymbolic(context, inputFile, content);
            bugDetector.get().detectMissingFailedCheck(context, inputFile, content);
            bugDetector.get().detectZeroDenominator(context, inputFile, content);
            bugDetector.get().detectMissingMatrixDimensionCheck(context, inputFile, content);
            bugDetector.get().detectIncorrectSetInScoping(context, inputFile, content);
            bugDetector.get().detectMissingHoldAttributes(context, inputFile, content);
            bugDetector.get().detectEvaluationOrderAssumption(context, inputFile, content);
            bugDetector.get().detectIncorrectLevelSpecification(context, inputFile, content);
            bugDetector.get().detectUnpackingPackedArrays(context, inputFile, content);
            bugDetector.get().detectMissingSpecialCaseHandling(context, inputFile, content);
            bugDetector.get().detectIncorrectAssociationOperations(context, inputFile, content);
            bugDetector.get().detectDateObjectValidation(context, inputFile, content);
            bugDetector.get().detectTotalMeanOnNonNumeric(context, inputFile, content);
            bugDetector.get().detectQuantityUnitMismatch(context, inputFile, content);

            // New Vulnerability detectors (7 rules) - with timing
            logRuleStart("ToExpressionOnInput");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectToExpressionOnInput(context, inputFile, content);
            logSlowRule(inputFile, "ToExpressionOnInput", ruleStart);

            logRuleStart("UnsanitizedRunProcess");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectUnsanitizedRunProcess(context, inputFile, content);
            logSlowRule(inputFile, "UnsanitizedRunProcess", ruleStart);

            logRuleStart("MissingCloudAuth");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectMissingCloudAuth(context, inputFile, content);
            logSlowRule(inputFile, "MissingCloudAuth", ruleStart);

            logRuleStart("HardcodedApiKeys");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectHardcodedApiKeys(context, inputFile, content);
            logSlowRule(inputFile, "HardcodedApiKeys", ruleStart);

            logRuleStart("NeedsGetUntrusted");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectNeedsGetUntrusted(context, inputFile, content);
            logSlowRule(inputFile, "NeedsGetUntrusted", ruleStart);

            logRuleStart("ExposingSensitiveData");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectExposingSensitiveData(context, inputFile, content);
            logSlowRule(inputFile, "ExposingSensitiveData", ruleStart);

            logRuleStart("MissingFormFunctionValidation");
            ruleStart = System.currentTimeMillis();
            vulnerabilityDetector.get().detectMissingFormFunctionValidation(context, inputFile, content);
            logSlowRule(inputFile, "MissingFormFunctionValidation", ruleStart);

            // ===== CHUNK 1 DETECTORS (Items 16-50 from ROADMAP_325.md) =====

            // Pattern System Rules (Items 16-30)
            patternAndDataStructureDetector.get().detectUnrestrictedBlankPattern(context, inputFile, content);
            patternAndDataStructureDetector.get().detectPatternTestVsCondition(context, inputFile, content);
            patternAndDataStructureDetector.get().detectBlankSequenceWithoutRestriction(context, inputFile, content);
            patternAndDataStructureDetector.get().detectNestedOptionalPatterns(context, inputFile, content);
            patternAndDataStructureDetector.get().detectPatternNamingConflicts(context, inputFile, content);
            patternAndDataStructureDetector.get().detectRepeatedPatternAlternatives(context, inputFile, content);
            patternAndDataStructureDetector.get().detectPatternTestWithPureFunction(context, inputFile, content);
            patternAndDataStructureDetector.get().detectMissingPatternDefaults(context, inputFile, content);
            patternAndDataStructureDetector.get().detectOrderDependentPatterns(context, inputFile, content);
            patternAndDataStructureDetector.get().detectVerbatimPatternMisuse(context, inputFile, content);
            patternAndDataStructureDetector.get().detectHoldPatternUnnecessary(context, inputFile, content);
            patternAndDataStructureDetector.get().detectLongestShortestWithoutOrdering(context, inputFile, content);
            patternAndDataStructureDetector.get().detectPatternRepeatedDifferentTypes(context, inputFile, content);
            patternAndDataStructureDetector.get().detectAlternativesTooComplex(context, inputFile, content);
            patternAndDataStructureDetector.get().detectPatternMatchingLargeLists(context, inputFile, content);

            // List/Array Rules (Items 31-40)
            patternAndDataStructureDetector.get().detectEmptyListIndexing(context, inputFile, content);
            patternAndDataStructureDetector.get().detectNegativeIndexWithoutValidation(context, inputFile, content);
            patternAndDataStructureDetector.get().detectPartAssignmentToImmutable(context, inputFile, content);
            patternAndDataStructureDetector.get().detectInefficientListConcatenation(context, inputFile, content);
            patternAndDataStructureDetector.get().detectUnnecessaryFlatten(context, inputFile, content);
            patternAndDataStructureDetector.get().detectLengthInLoopCondition(context, inputFile, content);
            patternAndDataStructureDetector.get().detectReverseTwice(context, inputFile, content);
            patternAndDataStructureDetector.get().detectSortWithoutComparison(context, inputFile, content);
            patternAndDataStructureDetector.get().detectPositionVsSelect(context, inputFile, content);
            patternAndDataStructureDetector.get().detectNestedPartExtraction(context, inputFile, content);

            // Association Rules (Items 41-50)
            patternAndDataStructureDetector.get().detectMissingKeyCheck(context, inputFile, content);
            patternAndDataStructureDetector.get().detectAssociationVsListConfusion(context, inputFile, content);
            patternAndDataStructureDetector.get().detectInefficientKeyLookup(context, inputFile, content);
            patternAndDataStructureDetector.get().detectQueryOnNonDataset(context, inputFile, content);
            patternAndDataStructureDetector.get().detectAssociationUpdatePattern(context, inputFile, content);
            patternAndDataStructureDetector.get().detectMergeWithoutConflictStrategy(context, inputFile, content);
            patternAndDataStructureDetector.get().detectAssociateToOnNonSymbol(context, inputFile, content);
            patternAndDataStructureDetector.get().detectKeyDropMultipleTimes(context, inputFile, content);
            patternAndDataStructureDetector.get().detectLookupWithMissingDefault(context, inputFile, content);
            patternAndDataStructureDetector.get().detectGroupByWithoutAggregation(context, inputFile, content);

            // ===== CHUNK 2 DETECTORS (Items 61-100 from ROADMAP_325.md) =====

            // Unused Code Detection (Items 61-75)
            unusedAndNamingDetector.get().detectUnusedPrivateFunction(context, inputFile, content);
            unusedAndNamingDetector.get().detectUnusedFunctionParameter(context, inputFile, content);
            unusedAndNamingDetector.get().detectUnusedModuleVariable(context, inputFile, content);
            unusedAndNamingDetector.get().detectUnusedWithVariable(context, inputFile, content);
            unusedAndNamingDetector.get().detectUnusedImport(context, inputFile, content);
            unusedAndNamingDetector.get().detectUnusedPatternName(context, inputFile, content);
            unusedAndNamingDetector.get().detectUnusedOptionalParameter(context, inputFile, content);
            unusedAndNamingDetector.get().detectDeadCodeAfterReturn(context, inputFile, content);
            unusedAndNamingDetector.get().detectUnreachableAfterAbortThrow(context, inputFile, content);
            unusedAndNamingDetector.get().detectAssignmentNeverRead(context, inputFile, content);
            unusedAndNamingDetector.get().detectFunctionDefinedButNeverCalled(context, inputFile, content);
            unusedAndNamingDetector.get().detectRedefinedWithoutUse(context, inputFile, content);
            unusedAndNamingDetector.get().detectLoopVariableUnused(context, inputFile, content);
            unusedAndNamingDetector.get().detectCatchWithoutThrow(context, inputFile, content);
            unusedAndNamingDetector.get().detectConditionAlwaysFalse(context, inputFile, content);

            // Shadowing & Naming (Items 76-90)
            unusedAndNamingDetector.get().detectLocalShadowsGlobal(context, inputFile, content);
            unusedAndNamingDetector.get().detectParameterShadowsBuiltin(context, inputFile, content);
            unusedAndNamingDetector.get().detectLocalShadowsParameter(context, inputFile, content);
            unusedAndNamingDetector.get().detectMultipleDefinitionsSameSymbol(context, inputFile, content);
            unusedAndNamingDetector.get().detectSymbolNameTooShort(context, inputFile, content);
            unusedAndNamingDetector.get().detectSymbolNameTooLong(context, inputFile, content);
            unusedAndNamingDetector.get().detectInconsistentNamingConvention(context, inputFile, content);
            unusedAndNamingDetector.get().detectBuiltinNameInLocalScope(context, inputFile, content);
            unusedAndNamingDetector.get().detectContextConflicts(context, inputFile, content);
            unusedAndNamingDetector.get().detectReservedNameUsage(context, inputFile, content);
            unusedAndNamingDetector.get().detectPrivateContextSymbolPublic(context, inputFile, content);
            unusedAndNamingDetector.get().detectMismatchedBeginEnd(context, inputFile, content);
            unusedAndNamingDetector.get().detectSymbolAfterEndPackage(context, inputFile, content);
            unusedAndNamingDetector.get().detectGlobalInPackage(context, inputFile, content);
            unusedAndNamingDetector.get().detectTempVariableNotTemp(context, inputFile, content);

            // Undefined Symbol Detection (Items 91-100)
            unusedAndNamingDetector.get().detectUndefinedFunctionCall(context, inputFile, content);
            unusedAndNamingDetector.get().detectUndefinedVariableReference(context, inputFile, content);
            unusedAndNamingDetector.get().detectTypoInBuiltinName(context, inputFile, content);
            unusedAndNamingDetector.get().detectWrongCapitalization(context, inputFile, content);
            unusedAndNamingDetector.get().detectMissingImport(context, inputFile, content);
            unusedAndNamingDetector.get().detectContextNotFound(context, inputFile, content);
            unusedAndNamingDetector.get().detectSymbolMaskedByImport(context, inputFile, content);
            unusedAndNamingDetector.get().detectMissingPathEntry(context, inputFile, content);
            unusedAndNamingDetector.get().detectCircularNeeds(context, inputFile, content);
            unusedAndNamingDetector.get().detectForwardReferenceWithoutDeclaration(context, inputFile, content);

            // ===== CHUNK 3 DETECTORS (Items 111-150 from ROADMAP_325.md) =====

            // Type Mismatch Detection (Items 111-130)
            typeAndDataFlowDetector.get().detectNumericOperationOnString(context, inputFile, content);
            typeAndDataFlowDetector.get().detectStringOperationOnNumber(context, inputFile, content);
            typeAndDataFlowDetector.get().detectWrongArgumentType(context, inputFile, content);
            typeAndDataFlowDetector.get().detectFunctionReturnsWrongType(context, inputFile, content);
            typeAndDataFlowDetector.get().detectComparisonIncompatibleTypes(context, inputFile, content);
            typeAndDataFlowDetector.get().detectMixedNumericTypes(context, inputFile, content);
            typeAndDataFlowDetector.get().detectIntegerDivisionExpectingReal(context, inputFile, content);
            typeAndDataFlowDetector.get().detectListFunctionOnAssociation(context, inputFile, content);
            typeAndDataFlowDetector.get().detectPatternTypeMismatch(context, inputFile, content);
            typeAndDataFlowDetector.get().detectOptionalTypeInconsistent(context, inputFile, content);
            typeAndDataFlowDetector.get().detectReturnTypeInconsistent(context, inputFile, content);
            typeAndDataFlowDetector.get().detectNullAssignmentToTypedVariable(context, inputFile, content);
            typeAndDataFlowDetector.get().detectTypeCastWithoutValidation(context, inputFile, content);
            typeAndDataFlowDetector.get().detectImplicitTypeConversion(context, inputFile, content);
            typeAndDataFlowDetector.get().detectGraphicsObjectInNumericContext(context, inputFile, content);
            typeAndDataFlowDetector.get().detectSymbolInNumericContext(context, inputFile, content);
            typeAndDataFlowDetector.get().detectImageOperationOnNonImage(context, inputFile, content);
            typeAndDataFlowDetector.get().detectSoundOperationOnNonSound(context, inputFile, content);
            typeAndDataFlowDetector.get().detectDatasetOperationOnList(context, inputFile, content);
            typeAndDataFlowDetector.get().detectGraphOperationOnNonGraph(context, inputFile, content);

            // Data Flow Analysis (Items 135-150)
            typeAndDataFlowDetector.get().detectUninitializedVariableUseEnhanced(context, inputFile, content);
            typeAndDataFlowDetector.get().detectVariableMayBeUninitialized(context, inputFile, content);
            typeAndDataFlowDetector.get().detectDeadStore(context, inputFile, content);
            typeAndDataFlowDetector.get().detectOverwrittenBeforeRead(context, inputFile, content);
            typeAndDataFlowDetector.get().detectVariableAliasingIssue(context, inputFile, content);
            typeAndDataFlowDetector.get().detectModificationOfLoopIterator(context, inputFile, content);
            typeAndDataFlowDetector.get().detectUseOfIteratorOutsideLoop(context, inputFile, content);
            typeAndDataFlowDetector.get().detectReadingUnsetVariable(context, inputFile, content);
            typeAndDataFlowDetector.get().detectDoubleAssignmentSameValue(context, inputFile, content);
            typeAndDataFlowDetector.get().detectMutationInPureFunction(context, inputFile, content);
            typeAndDataFlowDetector.get().detectSharedMutableState(context, inputFile, content);
            typeAndDataFlowDetector.get().detectVariableScopeEscape(context, inputFile, content);
            typeAndDataFlowDetector.get().detectClosureOverMutableVariable(context, inputFile, content);
            typeAndDataFlowDetector.get().detectAssignmentInConditionEnhanced(context, inputFile, content);
            typeAndDataFlowDetector.get().detectAssignmentAsReturnValue(context, inputFile, content);
            typeAndDataFlowDetector.get().detectVariableNeverModified(context, inputFile, content);

            // ===== CHUNK 4 DETECTORS (Items 161-200 from ROADMAP_325.md) =====

            // Dead Code & Reachability (Items 161-175)
            controlFlowAndTaintDetector.get().detectUnreachableCodeAfterReturn(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectUnreachableBranchAlwaysTrue(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectUnreachableBranchAlwaysFalse(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectImpossiblePattern(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectEmptyCatchBlockEnhanced(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectConditionAlwaysEvaluatesSame(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectInfiniteLoopProven(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectLoopNeverExecutes(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectCodeAfterAbort(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectMultipleReturnsMakeCodeUnreachable(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectElseBranchNeverTaken(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectSwitchCaseShadowed(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectPatternDefinitionShadowed(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectExceptionNeverThrown(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectBreakOutsideLoop(context, inputFile, content);

            // Taint Analysis for Security (Items 181-195)
            controlFlowAndTaintDetector.get().detectSqlInjectionTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectCommandInjectionTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectCodeInjectionTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectPathTraversalTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectXssTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectLdapInjection(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectXxeTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectUnsafeDeserializationTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectSsrfTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectInsecureRandomnessEnhanced(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectWeakCryptographyEnhanced(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectHardCodedCredentialsTaint(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectSensitiveDataInLogs(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectMassAssignment(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectRegexDoS(context, inputFile, content);

            // Additional Control Flow Rules (Items 196-200)
            controlFlowAndTaintDetector.get().detectMissingDefaultCase(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectEmptyIfBranch(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectNestedIfDepth(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectTooManyReturnPoints(context, inputFile, content);
            controlFlowAndTaintDetector.get().detectMissingElseConsideredHarmful(context, inputFile, content);

            // ===== CHUNK 5 DETECTORS (Items 211-250 from ROADMAP_325.md) =====

            // Dependency & Architecture Rules (Items 211-230)
            ArchitectureAndDependencyDetector.detectCircularPackageDependency(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectUnusedPackageImport(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectMissingPackageImport(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectDiamondDependency(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectCyclicCallBetweenPackages(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectLayerViolation(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectUnstableDependency(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPackageTooLarge(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPackageTooSmall(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPackageExportsTooMuch(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPackageExportsTooLittle(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectIncompletePublicAPI(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPrivateSymbolUsedExternally(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectInternalImplementationExposed(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectMissingPackageDocumentation(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPublicAPIChangedWithoutVersionBump(context, inputFile, content);

            // Unused Export & Dead Code (Items 231-245)
            ArchitectureAndDependencyDetector.detectUnusedPublicFunction(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectUnusedExport(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectDeadPackage(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectFunctionOnlyCalledOnce(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectOverAbstractedAPI(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectOrphanedTestFile(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectImplementationWithoutTests(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectDeprecatedAPIStillUsedInternally(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectInternalAPIUsedLikePublic(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectCommentedOutPackageLoad(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectConditionalPackageLoad(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPackageLoadedButNotListedInMetadata(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectDuplicateSymbolDefinitionAcrossPackages(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectSymbolRedefinitionAfterImport(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPackageVersionMismatch(context, inputFile, content);

            // Documentation & Consistency (Items 246-250)
            ArchitectureAndDependencyDetector.detectPublicExportMissingUsageMessage(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectInconsistentParameterNamesAcrossOverloads(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPublicFunctionWithImplementationDetailsInName(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectPublicAPINotInPackageContext(context, inputFile, content);
            ArchitectureAndDependencyDetector.detectTestFunctionInProductionCode(context, inputFile, content);

            // ===== CHUNK 6 & 7 DETECTORS (Items 251-325 from ROADMAP_325.md) =====

            // Null Safety & Error Handling (Items 251-266)
            AdvancedAnalysisDetector.detectNullDereference(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingNullCheck(context, inputFile, content);
            AdvancedAnalysisDetector.detectNullPassedToNonNullable(context, inputFile, content);
            AdvancedAnalysisDetector.detectInconsistentNullHandling(context, inputFile, content);
            AdvancedAnalysisDetector.detectNullReturnNotDocumented(context, inputFile, content);
            AdvancedAnalysisDetector.detectComparisonWithNull(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingCheckLeadsToNullPropagation(context, inputFile, content);
            AdvancedAnalysisDetector.detectCheckPatternDoesntHandleAllCases(context, inputFile, content);
            AdvancedAnalysisDetector.detectQuietSuppressingImportantMessages(context, inputFile, content);
            AdvancedAnalysisDetector.detectOffDisablingImportantWarnings(context, inputFile, content);
            AdvancedAnalysisDetector.detectCatchAllExceptionHandler(context, inputFile, content);
            AdvancedAnalysisDetector.detectEmptyExceptionHandler(context, inputFile, content);
            AdvancedAnalysisDetector.detectThrowWithoutCatch(context, inputFile, content);
            AdvancedAnalysisDetector.detectAbortInLibraryCode(context, inputFile, content);
            AdvancedAnalysisDetector.detectMessageWithoutDefinition(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingMessageDefinition(context, inputFile, content);

            // Constant & Expression Analysis (Items 267-280)
            AdvancedAnalysisDetector.detectConditionAlwaysTrueConstantPropagation(context, inputFile, content);
            AdvancedAnalysisDetector.detectConditionAlwaysFalseConstantPropagation(context, inputFile, content);
            AdvancedAnalysisDetector.detectLoopBoundConstant(context, inputFile, content);
            AdvancedAnalysisDetector.detectRedundantComputation(context, inputFile, content);
            AdvancedAnalysisDetector.detectPureExpressionInLoop(context, inputFile, content);
            AdvancedAnalysisDetector.detectConstantExpression(context, inputFile, content);
            AdvancedAnalysisDetector.detectIdentityOperation(context, inputFile, content);
            AdvancedAnalysisDetector.detectComparisonOfIdenticalExpressions(context, inputFile, content);
            AdvancedAnalysisDetector.detectBooleanExpressionAlwaysTrue(context, inputFile, content);
            AdvancedAnalysisDetector.detectBooleanExpressionAlwaysFalse(context, inputFile, content);
            AdvancedAnalysisDetector.detectUnnecessaryBooleanConversion(context, inputFile, content);
            AdvancedAnalysisDetector.detectDoubleNegation(context, inputFile, content);
            AdvancedAnalysisDetector.detectComplexBooleanExpressionEnhanced(context, inputFile, content);
            AdvancedAnalysisDetector.detectDeMorgansLawOpportunity(context, inputFile, content);

            // Mathematica-Specific Patterns (Items 281-300)
            AdvancedAnalysisDetector.detectHoldAttributeMissing(context, inputFile, content);
            AdvancedAnalysisDetector.detectHoldFirstButUsesSecondArgumentFirst(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingUnevaluatedWrapper(context, inputFile, content);
            AdvancedAnalysisDetector.detectUnnecessaryHold(context, inputFile, content);
            AdvancedAnalysisDetector.detectReleaseHoldAfterHold(context, inputFile, content);
            AdvancedAnalysisDetector.detectEvaluateInHeldContext(context, inputFile, content);
            AdvancedAnalysisDetector.detectPatternWithSideEffect(context, inputFile, content);
            AdvancedAnalysisDetector.detectReplacementRuleOrderMatters(context, inputFile, content);
            AdvancedAnalysisDetector.detectReplaceAllVsReplaceConfusion(context, inputFile, content);
            AdvancedAnalysisDetector.detectRuleDoesntMatchDueToEvaluation(context, inputFile, content);
            AdvancedAnalysisDetector.detectPartSpecificationOutOfBounds(context, inputFile, content);
            AdvancedAnalysisDetector.detectSpanSpecificationInvalid(context, inputFile, content);
            AdvancedAnalysisDetector.detectAllSpecificationInefficient(context, inputFile, content);
            AdvancedAnalysisDetector.detectThreadingOverNonLists(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingAttributesDeclaration(context, inputFile, content);
            AdvancedAnalysisDetector.detectOneIdentityAttributeMisuse(context, inputFile, content);
            AdvancedAnalysisDetector.detectOrderlessAttributeOnNonCommutative(context, inputFile, content);
            AdvancedAnalysisDetector.detectFlatAttributeMisuse(context, inputFile, content);
            AdvancedAnalysisDetector.detectSequenceInUnexpectedContext(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingSequenceWrapper(context, inputFile, content);

            // Test Coverage Integration (Items 307-310)
            AdvancedAnalysisDetector.detectLowTestCoverageWarning(context, inputFile, content);
            AdvancedAnalysisDetector.detectUntestedPublicFunction(context, inputFile, content);
            AdvancedAnalysisDetector.detectUntestedBranch(context, inputFile, content);
            AdvancedAnalysisDetector.detectTestOnlyCodeInProduction(context, inputFile, content);

            // Performance Analysis (Items 312-320)
            AdvancedAnalysisDetector.detectCompilableFunctionNotCompiled(context, inputFile, content);
            AdvancedAnalysisDetector.detectCompilationTargetMissing(context, inputFile, content);
            AdvancedAnalysisDetector.detectNonCompilableConstructInCompile(context, inputFile, content);
            AdvancedAnalysisDetector.detectPackedArrayUnpacked(context, inputFile, content);
            AdvancedAnalysisDetector.detectInefficientPatternInPerformanceCriticalCode(context, inputFile, content);
            AdvancedAnalysisDetector.detectNAppliedTooLate(context, inputFile, content);
            AdvancedAnalysisDetector.detectMissingMemoizationOpportunityEnhanced(context, inputFile, content);
            AdvancedAnalysisDetector.detectInefficientStringConcatenationEnhanced(context, inputFile, content);
            AdvancedAnalysisDetector.detectListConcatenationInLoop(context, inputFile, content);

            // ===== SYMBOL TABLE ANALYSIS (10 rules) =====
            // === TIMING: Symbol Table Analysis ===
            long symbolTableStart = System.currentTimeMillis();

            // Build symbol table for advanced variable lifetime and scope analysis
            try {
                long buildStart = System.currentTimeMillis();
                SymbolTable symbolTable = SymbolTableBuilder.build(inputFile, content);
                long buildTime = System.currentTimeMillis() - buildStart;

                long detectStart = System.currentTimeMillis();

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

                // Symbol table timing removed - too noisy
            } catch (Exception e) {
                LOG.debug("Error in symbol table analysis for: {}", inputFile.filename());
            }

            } // End of !skipVulnDetection block

            // === FINAL TIMING SUMMARY ===
            long totalFileTime = System.currentTimeMillis() - fileStartTime;
            long vulnTime = vulnStart > 0 ? (System.currentTimeMillis() - vulnStart) : 0;

            // Only log very slow files (>2 seconds) with detailed breakdown
            if (totalFileTime > 2000) {
                LOG.info("PERF: SLOW FILE - {} took {}ms total ({} lines) - CodeSmell: {}ms ({}%), Bug: {}ms ({}%), Vuln: {}ms ({}%)",
                    inputFile.filename(), totalFileTime, inputFile.lines(),
                    codeSmellTime, (codeSmellTime * 100 / totalFileTime),
                    bugTime, (bugTime * 100 / totalFileTime),
                    vulnTime, (vulnTime * 100 / totalFileTime));
            }

            // Clear caches after processing file
            codeSmellDetector.get().clearCaches();
            bugDetector.get().clearCaches();
            vulnerabilityDetector.get().clearCaches();
            securityHotspotDetector.get().clearCaches();
            patternAndDataStructureDetector.get().clearCaches();
            unusedAndNamingDetector.get().clearCaches();
            typeAndDataFlowDetector.get().clearCaches();
            controlFlowAndTaintDetector.get().clearCaches();
            // Note: ArchitectureAndDependencyDetector uses static caches cleared at end of execute()

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

    /**
     * Logs when a rule starts execution to help identify hanging rules.
     */
    private void logRuleStart(String ruleName) {
        LOG.info("→ Starting rule: {}", ruleName);
    }

    /**
     * Logs slow rules (>100ms) to help identify performance bottlenecks.
     */
    private void logSlowRule(InputFile inputFile, String ruleName, long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > 100) {
            LOG.info("PERF: SLOW RULE - {} in {} took {}ms", ruleName, inputFile.filename(), elapsed);
        }
    }

    /**
     * Check if file should skip vulnerability detection due to size.
     * Very large files can cause performance issues with regex-based vulnerability detection.
     */
    private boolean shouldSkipVulnerabilityDetection(String content, String filename) {
        int lineCount = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lineCount++;
                if (lineCount > MAX_FILE_SIZE_FOR_VULN_DETECTION) {
                    return true;
                }
            }
        }
        return false;
    }
}
