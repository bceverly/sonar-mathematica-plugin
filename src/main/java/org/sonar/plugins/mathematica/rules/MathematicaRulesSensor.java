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
import org.sonar.plugins.mathematica.ast.ComprehensiveParser;
import org.sonar.plugins.mathematica.ast.UnifiedRuleVisitor;
import org.sonar.plugins.mathematica.ast.AstNode;

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
    private final ThreadLocal<VulnerabilityDetectorAst> vulnerabilityDetectorAst = ThreadLocal.withInitial(() -> {
        VulnerabilityDetectorAst d = new VulnerabilityDetectorAst();
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
            LOG.debug("Issue saver thread started");
            long lastLogTime = System.currentTimeMillis();
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
                        long saved = savedIssues.incrementAndGet();

                        // Log progress every 10,000 issues (DEBUG) or if save is extremely slow (DEBUG)
                        if (saveDuration > 2000) {
                            // Only log if save takes >2 seconds (truly problematic)
                            LOG.debug("⚠ SLOW SAVE: Issue #{} took {}ms (queue: {})",
                                saved, saveDuration, issueQueue.size());
                        } else if (saved % 10000 == 0) {
                            long elapsed = System.currentTimeMillis() - lastLogTime;
                            // Fix rate calculation to avoid overflow/nonsense values
                            int rate = elapsed > 0 ? (int)(10000000.0 / elapsed) : 0;
                            LOG.debug("Issue saver progress: {}/{} saved ({} issues/sec, queue: {})",
                                saved, queuedIssues.get(), rate, issueQueue.size());
                            lastLogTime = System.currentTimeMillis();
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
        LOG.debug("Stopping issue saver thread (queue size: {}, saved: {}/{})",
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

        // NO FILES SKIPPED - Complete analysis on all files

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

            // NO SIZE LIMITS - Analyze all files for complete run
            long readStartTime = System.currentTimeMillis();
            String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);
            long readTime = System.currentTimeMillis() - readStartTime;

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
            advancedAnalysisDetector.get().initializeCaches(content);
            // Note: ArchitectureAndDependencyDetector uses static caches (no per-file init needed)

            // Analyze comments once and cache for reuse
            List<int[]> commentRanges = analyzeComments(context, inputFile, content);

            // === UNIFIED AST-BASED ANALYSIS (~400 rules in ONE PASS) ===
            // PERFORMANCE: Single parse + single AST traversal replaces 400+ sequential regex scans
            long analysisStart = System.currentTimeMillis();

            // NO SIZE LIMITS - Analyze all files completely
            try {
                // STEP 1: Parse once to build complete AST
                ComprehensiveParser parser = new ComprehensiveParser();
                List<AstNode> ast = parser.parse(content);

                // STEP 2: Single visitor traversal checks ALL rules
                UnifiedRuleVisitor visitor = new UnifiedRuleVisitor(context, inputFile, this, content);
                for (AstNode node : ast) {
                    node.accept(visitor);
                }

                // STEP 3: Post-traversal checks (whole-file analyses)
                visitor.performPostTraversalChecks();

                long analysisTime = System.currentTimeMillis() - analysisStart;
                LOG.debug("Unified AST analysis for {} completed in {}ms", inputFile.filename(), analysisTime);

            } catch (Exception e) {
                LOG.error("Error in unified AST analysis for: {}", inputFile.filename(), e);
            }

            long analysisTime = System.currentTimeMillis() - analysisStart;

            // OLD DETECTOR APPROACH - REPLACED WITH UNIFIED AST ABOVE
            // === TIMING: Code Smell Detectors ===
            // long codeSmellStart = System.currentTimeMillis();

            // Delegate to Code Smell detector (33 rules) - NOW IN UnifiedRuleVisitor
            // REMOVED: codeSmellDetector.get().detectMagicNumbers(context, inputFile, content, commentRanges);
            // REMOVED: codeSmellDetector.get().detectEmptyBlocks(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectLongFunctions(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectEmptyCatchBlocks(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectDebugCode(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectUnusedVariables(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectDuplicateFunctions(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectTooManyParameters(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectDeeplyNested(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingDocumentation(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectInconsistentNaming(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectIdenticalBranches(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectExpressionTooComplex(context, inputFile, content, commentRanges);
            // REMOVED: codeSmellDetector.get().detectDeprecatedFunctions(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectEmptyStatement(context, inputFile, content);

            // REMOVED: // Performance rules (Code Smell)
            // REMOVED: codeSmellDetector.get().detectAppendInLoop(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectRepeatedFunctionCalls(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectStringConcatInLoop(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectUncompiledNumerical(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectPackedArrayBreaking(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectNestedMapTable(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectLargeTempExpressions(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectPlotInLoop(context, inputFile, content);

            // REMOVED: // Best practices rules (Code Smell)
            // REMOVED: codeSmellDetector.get().detectGenericVariableNames(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingUsageMessage(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingOptionsPattern(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectSideEffectsNaming(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectComplexBoolean(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectUnprotectedSymbols(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingReturn(context, inputFile, content);

            // REMOVED: long codeSmellTime = System.currentTimeMillis() - codeSmellStart;

            // REMOVED: // === TIMING: Bug Detectors ===
            // REMOVED: long bugStart = System.currentTimeMillis();

            // REMOVED: // Delegate to Bug detector (20 rules)
            // REMOVED: bugDetector.get().detectDivisionByZero(context, inputFile, content);
            // REMOVED: bugDetector.get().detectAssignmentInConditional(context, inputFile, content);
            // REMOVED: bugDetector.get().detectListIndexOutOfBounds(context, inputFile, content);
            // REMOVED: bugDetector.get().detectInfiniteRecursion(context, inputFile, content);
            // REMOVED: bugDetector.get().detectUnreachablePatterns(context, inputFile, content);
            // REMOVED: bugDetector.get().detectFloatingPointEquality(context, inputFile, content);
            // REMOVED: bugDetector.get().detectFunctionWithoutReturn(context, inputFile, content);
            // REMOVED: bugDetector.get().detectVariableBeforeAssignment(context, inputFile, content);
            // REMOVED: bugDetector.get().detectOffByOne(context, inputFile, content);
            // REMOVED: bugDetector.get().detectInfiniteLoop(context, inputFile, content);
            // REMOVED: bugDetector.get().detectMismatchedDimensions(context, inputFile, content);
            // REMOVED: bugDetector.get().detectTypeMismatch(context, inputFile, content);
            // REMOVED: bugDetector.get().detectSuspiciousPattern(context, inputFile, content);

            // REMOVED: // Pattern matching bugs
            // REMOVED: bugDetector.get().detectMissingPatternTest(context, inputFile, content);
            // REMOVED: bugDetector.get().detectPatternBlanksMisuse(context, inputFile, content);
            // REMOVED: bugDetector.get().detectSetDelayedConfusion(context, inputFile, content);
            // REMOVED: bugDetector.get().detectSymbolNameCollision(context, inputFile, content);
            // REMOVED: bugDetector.get().detectBlockModuleMisuse(context, inputFile, content);

            // REMOVED: // Resource management bugs
            // REMOVED: bugDetector.get().detectUnclosedFileHandle(context, inputFile, content);
            // REMOVED: bugDetector.get().detectGrowingDefinitionChain(context, inputFile, content);

            // REMOVED: long bugTime = System.currentTimeMillis() - bugStart;

            // REMOVED: // === TIMING: Vulnerability Detectors ===
            // REMOVED: long vulnStart = System.currentTimeMillis();
            // REMOVED: long symbolTableStart = 0; // Will be set later if vuln detection runs

            // REMOVED: // Check if file should skip vulnerability detection due to size
            // REMOVED: boolean skipVulnDetection = shouldSkipVulnerabilityDetection(content, inputFile.filename());
            // REMOVED: if (skipVulnDetection) {
            // REMOVED:     skippedVulnFiles.add(String.format("%s (%d lines)", inputFile.filename(), inputFile.lines()));
            // REMOVED: }

            // REMOVED: // === AST-BASED VULNERABILITY DETECTION (21 rules in one pass) ===
            // REMOVED: // PERFORMANCE: Single AST walk replaces 21 sequential regex scans
            // REMOVED: long ruleStart;

            // REMOVED: if (!skipVulnDetection) {

            // REMOVED: logRuleStart("AllVulnerabilities");
            // REMOVED: ruleStart = System.currentTimeMillis();
            // REMOVED: vulnerabilityDetectorAst.get().detectAllVulnerabilities(context, inputFile, content);
            // REMOVED: logSlowRule(inputFile, "AllVulnerabilities", ruleStart);

            // REMOVED: // Delegate to Security Hotspot detector (7 rules)
            // REMOVED: securityHotspotDetector.get().detectFileUploadValidation(context, inputFile, content);
            // REMOVED: securityHotspotDetector.get().detectExternalApiSafeguards(context, inputFile, content);
            // REMOVED: securityHotspotDetector.get().detectCryptoKeyGeneration(context, inputFile, content);
            // REMOVED: securityHotspotDetector.get().detectNetworkOperations(context, inputFile, content);
            // REMOVED: securityHotspotDetector.get().detectFileSystemModifications(context, inputFile, content);
            // REMOVED: securityHotspotDetector.get().detectEnvironmentVariable(context, inputFile, content);
            // REMOVED: securityHotspotDetector.get().detectImportWithoutFormat(context, inputFile, content);

            // REMOVED: // ===== PHASE 4: NEW RULE DETECTORS (50 rules) =====

            // REMOVED: // New Code Smell detectors (18 rules)
            // REMOVED: codeSmellDetector.get().detectOvercomplexPatterns(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectInconsistentRuleTypes(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingFunctionAttributes(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingDownValuesDoc(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingPatternTestValidation(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectExcessivePureFunctions(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingOperatorPrecedence(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectHardcodedFilePaths(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectInconsistentReturnTypes(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingErrorMessages(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectGlobalStateModification(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingLocalization(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectExplicitGlobalContext(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingTemporaryCleanup(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectNestedListsInsteadAssociation(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectRepeatedPartExtraction(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingMemoization(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectStringJoinForTemplates(context, inputFile, content);

            // REMOVED: // New Performance detectors (10 rules)
            // REMOVED: codeSmellDetector.get().detectLinearSearchInsteadLookup(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectRepeatedCalculations(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectPositionInsteadPattern(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectFlattenTableAntipattern(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingParallelization(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingSparseArray(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectUnnecessaryTranspose(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectDeleteDuplicatesOnLargeData(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectRepeatedStringParsing(context, inputFile, content);
            // REMOVED: codeSmellDetector.get().detectMissingCompilationTarget(context, inputFile, content);

            // REMOVED: // New Bug detectors (15 rules)
            // REMOVED: bugDetector.get().detectMissingEmptyListCheck(context, inputFile, content);
            // REMOVED: bugDetector.get().detectMachinePrecisionInSymbolic(context, inputFile, content);
            // REMOVED: bugDetector.get().detectMissingFailedCheck(context, inputFile, content);
            // REMOVED: bugDetector.get().detectZeroDenominator(context, inputFile, content);
            // REMOVED: bugDetector.get().detectMissingMatrixDimensionCheck(context, inputFile, content);
            // REMOVED: bugDetector.get().detectIncorrectSetInScoping(context, inputFile, content);
            // REMOVED: bugDetector.get().detectMissingHoldAttributes(context, inputFile, content);
            // REMOVED: bugDetector.get().detectEvaluationOrderAssumption(context, inputFile, content);
            // REMOVED: bugDetector.get().detectIncorrectLevelSpecification(context, inputFile, content);
            // REMOVED: bugDetector.get().detectUnpackingPackedArrays(context, inputFile, content);
            // REMOVED: bugDetector.get().detectMissingSpecialCaseHandling(context, inputFile, content);
            // REMOVED: bugDetector.get().detectIncorrectAssociationOperations(context, inputFile, content);
            // REMOVED: bugDetector.get().detectDateObjectValidation(context, inputFile, content);
            // REMOVED: bugDetector.get().detectTotalMeanOnNonNumeric(context, inputFile, content);
            // REMOVED: bugDetector.get().detectQuantityUnitMismatch(context, inputFile, content);

            // REMOVED: // New Vulnerability detectors (7 rules) - now handled by AST visitor above

            // REMOVED: // ===== CHUNK 1 DETECTORS (Items 16-50 from ROADMAP_325.md) =====

            // REMOVED: // Pattern System Rules (Items 16-30)
            // REMOVED: patternAndDataStructureDetector.get().detectUnrestrictedBlankPattern(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectPatternTestVsCondition(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectBlankSequenceWithoutRestriction(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectNestedOptionalPatterns(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectPatternNamingConflicts(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectRepeatedPatternAlternatives(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectPatternTestWithPureFunction(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectMissingPatternDefaults(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectOrderDependentPatterns(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectVerbatimPatternMisuse(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectHoldPatternUnnecessary(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectLongestShortestWithoutOrdering(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectPatternRepeatedDifferentTypes(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectAlternativesTooComplex(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectPatternMatchingLargeLists(context, inputFile, content);

            // REMOVED: // List/Array Rules (Items 31-40)
            // REMOVED: patternAndDataStructureDetector.get().detectEmptyListIndexing(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectNegativeIndexWithoutValidation(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectPartAssignmentToImmutable(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectInefficientListConcatenation(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectUnnecessaryFlatten(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectLengthInLoopCondition(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectReverseTwice(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectSortWithoutComparison(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectPositionVsSelect(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectNestedPartExtraction(context, inputFile, content);

            // REMOVED: // Association Rules (Items 41-50)
            // REMOVED: patternAndDataStructureDetector.get().detectMissingKeyCheck(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectAssociationVsListConfusion(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectInefficientKeyLookup(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectQueryOnNonDataset(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectAssociationUpdatePattern(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectMergeWithoutConflictStrategy(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectAssociateToOnNonSymbol(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectKeyDropMultipleTimes(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectLookupWithMissingDefault(context, inputFile, content);
            // REMOVED: patternAndDataStructureDetector.get().detectGroupByWithoutAggregation(context, inputFile, content);

            // REMOVED: // ===== CHUNK 2 DETECTORS (Items 61-100 from ROADMAP_325.md) =====

            // REMOVED: // Unused Code Detection (Items 61-75)
            // REMOVED: unusedAndNamingDetector.get().detectUnusedPrivateFunction(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUnusedFunctionParameter(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUnusedModuleVariable(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUnusedWithVariable(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUnusedImport(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUnusedPatternName(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUnusedOptionalParameter(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectDeadCodeAfterReturn(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUnreachableAfterAbortThrow(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectAssignmentNeverRead(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectFunctionDefinedButNeverCalled(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectRedefinedWithoutUse(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectLoopVariableUnused(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectCatchWithoutThrow(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectConditionAlwaysFalse(context, inputFile, content);

            // REMOVED: // Shadowing & Naming (Items 76-90)
            // REMOVED: unusedAndNamingDetector.get().detectLocalShadowsGlobal(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectParameterShadowsBuiltin(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectLocalShadowsParameter(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectMultipleDefinitionsSameSymbol(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectSymbolNameTooShort(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectSymbolNameTooLong(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectInconsistentNamingConvention(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectBuiltinNameInLocalScope(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectContextConflicts(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectReservedNameUsage(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectPrivateContextSymbolPublic(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectMismatchedBeginEnd(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectSymbolAfterEndPackage(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectGlobalInPackage(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectTempVariableNotTemp(context, inputFile, content);

            // REMOVED: // Undefined Symbol Detection (Items 91-100)
            // REMOVED: unusedAndNamingDetector.get().detectUndefinedFunctionCall(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectUndefinedVariableReference(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectTypoInBuiltinName(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectWrongCapitalization(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectMissingImport(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectContextNotFound(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectSymbolMaskedByImport(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectMissingPathEntry(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectCircularNeeds(context, inputFile, content);
            // REMOVED: unusedAndNamingDetector.get().detectForwardReferenceWithoutDeclaration(context, inputFile, content);

            // REMOVED: // ===== CHUNK 3 DETECTORS (Items 111-150 from ROADMAP_325.md) =====

            // REMOVED: // Type Mismatch Detection (Items 111-130)
            // REMOVED: typeAndDataFlowDetector.get().detectNumericOperationOnString(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectStringOperationOnNumber(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectWrongArgumentType(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectFunctionReturnsWrongType(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectComparisonIncompatibleTypes(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectMixedNumericTypes(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectIntegerDivisionExpectingReal(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectListFunctionOnAssociation(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectPatternTypeMismatch(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectOptionalTypeInconsistent(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectReturnTypeInconsistent(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectNullAssignmentToTypedVariable(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectTypeCastWithoutValidation(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectImplicitTypeConversion(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectGraphicsObjectInNumericContext(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectSymbolInNumericContext(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectImageOperationOnNonImage(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectSoundOperationOnNonSound(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectDatasetOperationOnList(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectGraphOperationOnNonGraph(context, inputFile, content);

            // REMOVED: // Data Flow Analysis (Items 135-150)
            // REMOVED: typeAndDataFlowDetector.get().detectUninitializedVariableUseEnhanced(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectVariableMayBeUninitialized(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectDeadStore(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectOverwrittenBeforeRead(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectVariableAliasingIssue(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectModificationOfLoopIterator(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectUseOfIteratorOutsideLoop(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectReadingUnsetVariable(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectDoubleAssignmentSameValue(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectMutationInPureFunction(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectSharedMutableState(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectVariableScopeEscape(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectClosureOverMutableVariable(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectAssignmentInConditionEnhanced(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectAssignmentAsReturnValue(context, inputFile, content);
            // REMOVED: typeAndDataFlowDetector.get().detectVariableNeverModified(context, inputFile, content);

            // REMOVED: // ===== CHUNK 4 DETECTORS (Items 161-200 from ROADMAP_325.md) =====

            // REMOVED: // Dead Code & Reachability (Items 161-175)
            // REMOVED: controlFlowAndTaintDetector.get().detectUnreachableCodeAfterReturn(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectUnreachableBranchAlwaysTrue(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectUnreachableBranchAlwaysFalse(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectImpossiblePattern(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectEmptyCatchBlockEnhanced(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectConditionAlwaysEvaluatesSame(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectInfiniteLoopProven(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectLoopNeverExecutes(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectCodeAfterAbort(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectMultipleReturnsMakeCodeUnreachable(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectElseBranchNeverTaken(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectSwitchCaseShadowed(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectPatternDefinitionShadowed(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectExceptionNeverThrown(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectBreakOutsideLoop(context, inputFile, content);

            // REMOVED: // Taint Analysis for Security (Items 181-195)
            // REMOVED: controlFlowAndTaintDetector.get().detectSqlInjectionTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectCommandInjectionTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectCodeInjectionTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectPathTraversalTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectXssTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectLdapInjection(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectXxeTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectUnsafeDeserializationTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectSsrfTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectInsecureRandomnessEnhanced(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectWeakCryptographyEnhanced(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectHardCodedCredentialsTaint(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectSensitiveDataInLogs(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectMassAssignment(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectRegexDoS(context, inputFile, content);

            // REMOVED: // Additional Control Flow Rules (Items 196-200)
            // REMOVED: controlFlowAndTaintDetector.get().detectMissingDefaultCase(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectEmptyIfBranch(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectNestedIfDepth(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectTooManyReturnPoints(context, inputFile, content);
            // REMOVED: controlFlowAndTaintDetector.get().detectMissingElseConsideredHarmful(context, inputFile, content);

            // REMOVED: // ===== CHUNK 5 DETECTORS (Items 211-250 from ROADMAP_325.md) =====

            // REMOVED: // Dependency & Architecture Rules (Items 211-230)
            // REMOVED: ArchitectureAndDependencyDetector.detectCircularPackageDependency(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectUnusedPackageImport(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectMissingPackageImport(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectTransitiveDependencyCouldBeDirect(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectDiamondDependency(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectGodPackageTooManyDependencies(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPackageDependsOnApplicationCode(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectCyclicCallBetweenPackages(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectLayerViolation(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectUnstableDependency(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPackageTooLarge(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPackageTooSmall(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectInconsistentPackageNaming(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPackageExportsTooMuch(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPackageExportsTooLittle(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectIncompletePublicAPI(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPrivateSymbolUsedExternally(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectInternalImplementationExposed(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectMissingPackageDocumentation(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPublicAPIChangedWithoutVersionBump(context, inputFile, content);

            // REMOVED: // Unused Export & Dead Code (Items 231-245)
            // REMOVED: ArchitectureAndDependencyDetector.detectUnusedPublicFunction(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectUnusedExport(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectDeadPackage(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectFunctionOnlyCalledOnce(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectOverAbstractedAPI(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectOrphanedTestFile(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectImplementationWithoutTests(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectDeprecatedAPIStillUsedInternally(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectInternalAPIUsedLikePublic(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectCommentedOutPackageLoad(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectConditionalPackageLoad(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPackageLoadedButNotListedInMetadata(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectDuplicateSymbolDefinitionAcrossPackages(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectSymbolRedefinitionAfterImport(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPackageVersionMismatch(context, inputFile, content);

            // REMOVED: // Documentation & Consistency (Items 246-250)
            // REMOVED: ArchitectureAndDependencyDetector.detectPublicExportMissingUsageMessage(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectInconsistentParameterNamesAcrossOverloads(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPublicFunctionWithImplementationDetailsInName(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectPublicAPINotInPackageContext(context, inputFile, content);
            // REMOVED: ArchitectureAndDependencyDetector.detectTestFunctionInProductionCode(context, inputFile, content);

            // REMOVED: // ===== CHUNK 6 & 7 DETECTORS (Items 251-325 from ROADMAP_325.md) =====

            // REMOVED: // Null Safety & Error Handling (Items 251-266)
            // REMOVED: AdvancedAnalysisDetector.detectNullDereference(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMissingNullCheck(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectNullPassedToNonNullable(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectInconsistentNullHandling(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectNullReturnNotDocumented(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectComparisonWithNull(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMissingCheckLeadsToNullPropagation(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectCheckPatternDoesntHandleAllCases(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectQuietSuppressingImportantMessages(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectOffDisablingImportantWarnings(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectCatchAllExceptionHandler(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectEmptyExceptionHandler(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectThrowWithoutCatch(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectAbortInLibraryCode(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMessageWithoutDefinition(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMissingMessageDefinition(context, inputFile, content);

            // REMOVED: // Constant & Expression Analysis (Items 267-280)
            // REMOVED: AdvancedAnalysisDetector.detectConditionAlwaysTrueConstantPropagation(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectConditionAlwaysFalseConstantPropagation(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectLoopBoundConstant(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectRedundantComputation(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectPureExpressionInLoop(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectConstantExpression(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectIdentityOperation(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectComparisonOfIdenticalExpressions(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectBooleanExpressionAlwaysTrue(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectBooleanExpressionAlwaysFalse(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectUnnecessaryBooleanConversion(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectDoubleNegation(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectComplexBooleanExpressionEnhanced(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectDeMorgansLawOpportunity(context, inputFile, content);

            // REMOVED: // Mathematica-Specific Patterns (Items 281-300)
            // REMOVED: AdvancedAnalysisDetector.detectHoldAttributeMissing(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectHoldFirstButUsesSecondArgumentFirst(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMissingUnevaluatedWrapper(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectUnnecessaryHold(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectReleaseHoldAfterHold(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectEvaluateInHeldContext(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectPatternWithSideEffect(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectReplacementRuleOrderMatters(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectReplaceAllVsReplaceConfusion(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectRuleDoesntMatchDueToEvaluation(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectPartSpecificationOutOfBounds(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectSpanSpecificationInvalid(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectAllSpecificationInefficient(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectThreadingOverNonLists(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMissingAttributesDeclaration(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectOneIdentityAttributeMisuse(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectOrderlessAttributeOnNonCommutative(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectFlatAttributeMisuse(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectSequenceInUnexpectedContext(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMissingSequenceWrapper(context, inputFile, content);

            // REMOVED: // Test Coverage Integration (Items 307-310)
            // REMOVED: AdvancedAnalysisDetector.detectLowTestCoverageWarning(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectUntestedPublicFunction(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectUntestedBranch(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectTestOnlyCodeInProduction(context, inputFile, content);

            // REMOVED: // Performance Analysis (Items 312-320)
            // REMOVED: AdvancedAnalysisDetector.detectCompilableFunctionNotCompiled(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectCompilationTargetMissing(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectNonCompilableConstructInCompile(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectPackedArrayUnpacked(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectInefficientPatternInPerformanceCriticalCode(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectNAppliedTooLate(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectMissingMemoizationOpportunityEnhanced(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectInefficientStringConcatenationEnhanced(context, inputFile, content);
            // REMOVED: AdvancedAnalysisDetector.detectListConcatenationInLoop(context, inputFile, content);

            // ===== SYMBOL TABLE ANALYSIS (20 rules) =====
            // Symbol table analysis runs separately from main AST analysis
            long symbolTableStart = System.currentTimeMillis();

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

            long symbolTableTime = System.currentTimeMillis() - symbolTableStart;

            // === FINAL TIMING SUMMARY ===
            long totalFileTime = System.currentTimeMillis() - fileStartTime;

            // Only log very slow files (>2 seconds) with detailed breakdown
            if (totalFileTime > 2000) {
                LOG.info("PERF: SLOW FILE - {} took {}ms total ({} lines) - UnifiedAST: {}ms ({}%), SymbolTable: {}ms ({}%)",
                    inputFile.filename(), totalFileTime, inputFile.lines(),
                    analysisTime, (analysisTime * 100 / Math.max(totalFileTime, 1)),
                    symbolTableTime, (symbolTableTime * 100 / Math.max(totalFileTime, 1)));
            }

            // Clear caches after processing file
            codeSmellDetector.get().clearCaches();
            bugDetector.get().clearCaches();
            vulnerabilityDetector.get().clearCaches();
            vulnerabilityDetectorAst.get().clearCaches();
            securityHotspotDetector.get().clearCaches();
            patternAndDataStructureDetector.get().clearCaches();
            unusedAndNamingDetector.get().clearCaches();
            typeAndDataFlowDetector.get().clearCaches();
            controlFlowAndTaintDetector.get().clearCaches();
            advancedAnalysisDetector.get().clearCaches();
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
        LOG.debug("→ Starting rule: {}", ruleName);
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

}
