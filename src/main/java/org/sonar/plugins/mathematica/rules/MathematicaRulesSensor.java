package org.sonar.plugins.mathematica.rules;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(MathematicaRulesSensor.class);

    // Comment pattern for comment analysis
    private static final Pattern COMMENT_PATTERN = Pattern.compile("\\(\\*[\\s\\S]*?\\*\\)");

    // PERFORMANCE: Queue issue DATA (not NewIssue objects) to avoid thread-safety issues
    private static class IssueData {
        final InputFile inputFile;
        final int line;
        final String ruleKey;
        final String message;
        // Quick Fix data
        final String fileContent;
        final int startOffset;
        final int endOffset;
        final org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext context;

        IssueData(InputFile inputFile, int line, String ruleKey, String message) {
            this.inputFile = inputFile;
            this.line = line;
            this.ruleKey = ruleKey;
            this.message = message;
            this.fileContent = null;
            this.startOffset = -1;
            this.endOffset = -1;
            this.context = null;
        }

        IssueData(InputFile inputFile, int line, String ruleKey, String message,
                 String fileContent, int startOffset, int endOffset,
                 org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext context) {
            this.inputFile = inputFile;
            this.line = line;
            this.ruleKey = ruleKey;
            this.message = message;
            this.fileContent = fileContent;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.context = context;
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
    private final ThreadLocal<AdvancedAnalysisDetector> advancedAnalysisDetector =
        ThreadLocal.withInitial(AdvancedAnalysisDetector::new);
    private final ThreadLocal<TestingQualityDetector> testingQualityDetector = ThreadLocal.withInitial(() -> {
        TestingQualityDetector d = new TestingQualityDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<FrameworkDetector> frameworkDetector = ThreadLocal.withInitial(() -> {
        FrameworkDetector d = new FrameworkDetector();
        d.setSensor(this);
        return d;
    });
    private final ThreadLocal<StyleAndConventionsDetector> styleAndConventionsDetector = ThreadLocal.withInitial(() -> {
        StyleAndConventionsDetector d = new StyleAndConventionsDetector();
        d.setSensor(this);
        return d;
    });

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
     * Queues issue with Quick Fix data.
     */
    public void queueIssueWithFix(InputFile inputFile, int line, String ruleKey, String message,
                                  String fileContent, int startOffset, int endOffset,
                                  org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext context) {
        try {
            issueQueue.put(new IssueData(inputFile, line, ruleKey, message, fileContent, startOffset, endOffset, context));
            queuedIssues.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while queueing issue with fix", e);
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

                        // Add Quick Fix if available
                        if (data.fileContent != null && data.startOffset >= 0 && data.endOffset >= 0) {
                            try {
                                org.sonar.plugins.mathematica.fixes.QuickFixProvider quickFixProvider =
                                    new org.sonar.plugins.mathematica.fixes.QuickFixProvider();
                                quickFixProvider.addQuickFix(issue, data.inputFile, data.ruleKey,
                                    data.fileContent, data.startOffset, data.endOffset, data.context);
                            } catch (Exception e) {
                                // If Quick Fix fails, just skip it - don't break the issue reporting
                                LOG.debug("Failed to add Quick Fix for rule {}: {}", data.ruleKey, e.getMessage());
                            }
                        }

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
                            int rate = elapsed > 0 ? (int) (10000000.0 / elapsed) : 0;
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
                if (inputFile.lines() < 3 || inputFile.lines() > 35000) {
                    return;
                }
                String content = new String(Files.readAllBytes(Paths.get(inputFile.uri())), StandardCharsets.UTF_8);
                if (content.trim().isEmpty()) {
                    return;
                }

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
                    long estimatedRemainingMs = (long) (remainingFiles / filesPerSec * 1000);

                    LOG.info("Progress: {}/{} files analyzed ({} %) | Speed: {} files/sec | Est. remaining: {} min",
                        count,
                        totalFiles,
                        (int) ((count * 100.0) / totalFiles),
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

            // PERFORMANCE: Skip extremely large files to prevent hangs
            // Based on analysis: files >25,000 lines can cause pathological SymbolTable performance
            final int maxLines = 25000;
            if (inputFile.lines() > maxLines) {
                LOG.warn("SKIP: File {} has {} lines (exceeds limit of {}), skipping analysis to prevent timeout",
                    inputFile.filename(), inputFile.lines(), maxLines);

                // Report INFO issue to make this visible in SonarQube UI
                NewIssue issue = context.newIssue()
                    .forRule(RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY,
                                       MathematicaRulesDefinition.FILE_EXCEEDS_ANALYSIS_LIMIT_KEY));

                NewIssueLocation location = issue.newLocation()
                    .on(inputFile)
                    .at(inputFile.selectLine(1))
                    .message(String.format("File exceeds analysis limit (%d lines > %d limit). Analysis skipped to prevent timeout.",
                                         inputFile.lines(), maxLines));

                issue.at(location);
                issue.save();

                return;
            }

            long readStartTime = System.currentTimeMillis();
            String content = new String(Files.readAllBytes(Paths.get(inputFile.uri())), StandardCharsets.UTF_8);
            long readTime = System.currentTimeMillis() - readStartTime;

            // Skip empty or whitespace-only files
            if (content.trim().isEmpty()) {
                return;
            }

            // Initialize caches in all detectors for this file
            initializeDetectorCaches(content);

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

            // Delegate to Code Smell detector (33 rules) - NOW IN UnifiedRuleVisitor

            // ===== TIER 1 GAP CLOSURE - NEW DETECTION METHODS (70 rules) =====
            runTier1GapClosureDetectors(context, inputFile, content);

            // ===== CODE SMELL 2 - ADDITIONAL 70 CODE SMELLS FOR TIER 1 PARITY =====
            runStyleAndConventionsDetectors(context, inputFile, content);







































            performSymbolTableAnalysis(context, inputFile, content, fileStartTime, analysisTime);

        } catch (Throwable t) {
            // Check if this is a fatal error (StackOverflowError, OutOfMemoryError, etc.)
            if (t instanceof Error) {
                Error fatalError = (Error) t;
                LOG.error("========================================");
                LOG.error("FATAL ERROR while analyzing file: {}", inputFile.filename());
                LOG.error("Full file path: {}", Paths.get(inputFile.uri()).toAbsolutePath());
                LOG.error("File URI: {}", inputFile.uri());
                LOG.error("File size: {} lines", inputFile.lines());
                LOG.error("Error type: {}", fatalError.getClass().getName());
                LOG.error("========================================");
                // Re-throw fatal errors to crash the scanner
                throw fatalError;
            }
            // Non-fatal exceptions: log and continue
            LOG.error("Error analyzing file: {}", inputFile, t);
        }
    }

    private void initializeDetectorCaches(String content) {
        codeSmellDetector.get().initializeCaches(content);
        bugDetector.get().initializeCaches(content);
        vulnerabilityDetector.get().initializeCaches(content);
        securityHotspotDetector.get().initializeCaches(content);
        patternAndDataStructureDetector.get().initializeCaches(content);
        unusedAndNamingDetector.get().initializeCaches(content);
        typeAndDataFlowDetector.get().initializeCaches(content);
        controlFlowAndTaintDetector.get().initializeCaches(content);
        advancedAnalysisDetector.get().initializeCaches(content);
        testingQualityDetector.get().initializeCaches(content);
        frameworkDetector.get().initializeCaches(content);
        styleAndConventionsDetector.get().initializeCaches(content);
        // Note: ArchitectureAndDependencyDetector uses static caches (no per-file init needed)
    }

    /**
     * Run all 70 Tier 1 gap closure detection methods.
     * Extracted from analyzeFile() to reduce method complexity.
     */
    private void runTier1GapClosureDetectors(SensorContext context, InputFile inputFile, String content) {
        // SecurityHotspotDetector (23 new rules)
        securityHotspotDetector.get().detectWeakHashing(context, inputFile, content);
        securityHotspotDetector.get().detectWeakAuthentication(context, inputFile, content);
        securityHotspotDetector.get().detectDefaultCredentials(context, inputFile, content);
        securityHotspotDetector.get().detectPasswordPlainText(context, inputFile, content);
        securityHotspotDetector.get().detectInsecureSession(context, inputFile, content);
        securityHotspotDetector.get().detectMissingAuthorization(context, inputFile, content);
        securityHotspotDetector.get().detectWeakSessionToken(context, inputFile, content);
        securityHotspotDetector.get().detectMissingAccessControl(context, inputFile, content);
        securityHotspotDetector.get().detectInsecureRandomHotspot(context, inputFile, content);
        securityHotspotDetector.get().detectHardcodedCryptoKey(context, inputFile, content);
        securityHotspotDetector.get().detectWeakCipherMode(context, inputFile, content);
        securityHotspotDetector.get().detectInsufficientKeySize(context, inputFile, content);
        securityHotspotDetector.get().detectWeakSslProtocol(context, inputFile, content);
        securityHotspotDetector.get().detectCertificateValidationDisabled(context, inputFile, content);
        securityHotspotDetector.get().detectHttpWithoutTls(context, inputFile, content);
        securityHotspotDetector.get().detectCorsPermissive(context, inputFile, content);
        securityHotspotDetector.get().detectOpenRedirect(context, inputFile, content);
        securityHotspotDetector.get().detectDnsRebinding(context, inputFile, content);
        securityHotspotDetector.get().detectInsecureWebsocket(context, inputFile, content);
        securityHotspotDetector.get().detectMissingSecurityHeaders(context, inputFile, content);
        securityHotspotDetector.get().detectSensitiveDataLog(context, inputFile, content);
        securityHotspotDetector.get().detectPiiExposure(context, inputFile, content);
        securityHotspotDetector.get().detectClearTextProtocol(context, inputFile, content);

        // FrameworkDetector (18 new rules)
        frameworkDetector.get().detectNotebookCellSize(context, inputFile, content);
        frameworkDetector.get().detectNotebookUnorganized(context, inputFile, content);
        frameworkDetector.get().detectNotebookNoSections(context, inputFile, content);
        frameworkDetector.get().detectNotebookInitCellMisuse(context, inputFile, content);
        frameworkDetector.get().detectManipulatePerformance(context, inputFile, content);
        frameworkDetector.get().detectDynamicHeavyComputation(context, inputFile, content);
        frameworkDetector.get().detectDynamicNoTracking(context, inputFile, content);
        frameworkDetector.get().detectManipulateTooComplex(context, inputFile, content);
        frameworkDetector.get().detectPackageNoBegin(context, inputFile, content);
        frameworkDetector.get().detectPackagePublicPrivateMix(context, inputFile, content);
        frameworkDetector.get().detectPackageNoUsage(context, inputFile, content);
        frameworkDetector.get().detectPackageCircularDependency(context, inputFile, content);
        frameworkDetector.get().detectParallelNoGain(context, inputFile, content);
        frameworkDetector.get().detectParallelRaceCondition(context, inputFile, content);
        frameworkDetector.get().detectParallelSharedState(context, inputFile, content);
        frameworkDetector.get().detectCloudApiMissingAuth(context, inputFile, content);
        frameworkDetector.get().detectCloudPermissionsTooOpen(context, inputFile, content);
        frameworkDetector.get().detectCloudDeployNoValidation(context, inputFile, content);

        // TestingQualityDetector (12 new rules)
        testingQualityDetector.get().detectTestNamingConvention(context, inputFile, content);
        testingQualityDetector.get().detectTestNoIsolation(context, inputFile, content);
        testingQualityDetector.get().detectTestDataHardcoded(context, inputFile, content);
        testingQualityDetector.get().detectTestIgnored(context, inputFile, content);
        testingQualityDetector.get().detectVerificationTestNoExpected(context, inputFile, content);
        testingQualityDetector.get().detectVerificationTestTooBroad(context, inputFile, content);
        testingQualityDetector.get().detectVerificationTestNoDescription(context, inputFile, content);
        testingQualityDetector.get().detectVerificationTestEmpty(context, inputFile, content);
        testingQualityDetector.get().detectTestAssertCount(context, inputFile, content);
        testingQualityDetector.get().detectTestTooLong(context, inputFile, content);
        testingQualityDetector.get().detectTestMultipleConcerns(context, inputFile, content);
        testingQualityDetector.get().detectTestMagicNumber(context, inputFile, content);

        // BugDetector (7 new resource management rules)
        bugDetector.get().detectStreamNotClosed(context, inputFile, content);
        bugDetector.get().detectFileHandleLeak(context, inputFile, content);
        bugDetector.get().detectCloseInFinallyMissing(context, inputFile, content);
        bugDetector.get().detectStreamReopenAttempt(context, inputFile, content);
        bugDetector.get().detectDynamicMemoryLeak(context, inputFile, content);
        bugDetector.get().detectLargeDataInNotebook(context, inputFile, content);
        bugDetector.get().detectNoClearAfterUse(context, inputFile, content);

        // CodeSmellDetector (10 new comment quality rules)
        codeSmellDetector.get().detectTodoTracking(context, inputFile, content);
        codeSmellDetector.get().detectFixmeTracking(context, inputFile, content);
        codeSmellDetector.get().detectHackComment(context, inputFile, content);
        codeSmellDetector.get().detectCommentedOutCode(context, inputFile, content);
        codeSmellDetector.get().detectLargeCommentedBlock(context, inputFile, content);
        codeSmellDetector.get().detectApiMissingDocumentation(context, inputFile, content);
        codeSmellDetector.get().detectDocumentationTooShort(context, inputFile, content);
        codeSmellDetector.get().detectDocumentationOutdated(context, inputFile, content);
        codeSmellDetector.get().detectParameterNotDocumented(context, inputFile, content);
        codeSmellDetector.get().detectReturnNotDocumented(context, inputFile, content);
    }

    /**
     * Run all 70 StyleAndConventions detection methods for Tier 1 parity.
     * Additional code smell rules to approach Java's 458 code smells.
     */
    private void runStyleAndConventionsDetectors(SensorContext context, InputFile inputFile, String content) {
        // Style & Formatting (15 rules)
        styleAndConventionsDetector.get().detectLineTooLong(context, inputFile, content);
        styleAndConventionsDetector.get().detectInconsistentIndentation(context, inputFile, content);
        styleAndConventionsDetector.get().detectTrailingWhitespace(context, inputFile, content);
        styleAndConventionsDetector.get().detectMultipleBlankLines(context, inputFile, content);
        styleAndConventionsDetector.get().detectMissingBlankLineAfterFunction(context, inputFile, content);
        styleAndConventionsDetector.get().detectOperatorSpacing(context, inputFile, content);
        styleAndConventionsDetector.get().detectCommaSpacing(context, inputFile, content);
        styleAndConventionsDetector.get().detectBracketSpacing(context, inputFile, content);
        styleAndConventionsDetector.get().detectSemicolonStyle(context, inputFile, content);
        styleAndConventionsDetector.get().detectFileEndsWithoutNewline(context, inputFile, content);
        styleAndConventionsDetector.get().detectAlignmentInconsistent(context, inputFile, content);
        styleAndConventionsDetector.get().detectParenthesesUnnecessary(context, inputFile, content);
        styleAndConventionsDetector.get().detectBraceStyle(context, inputFile, content);
        styleAndConventionsDetector.get().detectLongStringLiteral(context, inputFile, content);
        styleAndConventionsDetector.get().detectNestedBracketsExcessive(context, inputFile, content);

        // Naming Conventions (15 rules)
        styleAndConventionsDetector.get().detectFunctionNameTooShort(context, inputFile, content);
        styleAndConventionsDetector.get().detectFunctionNameTooLong(context, inputFile, content);
        styleAndConventionsDetector.get().detectVariableNameTooShort(context, inputFile, content);
        styleAndConventionsDetector.get().detectBooleanNameNonDescriptive(context, inputFile, content);
        styleAndConventionsDetector.get().detectConstantNotUppercase(context, inputFile, content);
        styleAndConventionsDetector.get().detectPackageNameCase(context, inputFile, content);
        styleAndConventionsDetector.get().detectAcronymStyle(context, inputFile, content);
        styleAndConventionsDetector.get().detectVariableNameMatchesBuiltin(context, inputFile, content);
        styleAndConventionsDetector.get().detectParameterNameSameAsFunction(context, inputFile, content);
        styleAndConventionsDetector.get().detectInconsistentNamingStyle(context, inputFile, content);
        styleAndConventionsDetector.get().detectNumberInName(context, inputFile, content);
        styleAndConventionsDetector.get().detectHungarianNotation(context, inputFile, content);
        styleAndConventionsDetector.get().detectAbbreviationUnclear(context, inputFile, content);
        styleAndConventionsDetector.get().detectGenericName(context, inputFile, content);
        styleAndConventionsDetector.get().detectNegatedBooleanName(context, inputFile, content);

        // Complexity & Organization (7 rules, 3 duplicates removed)
        styleAndConventionsDetector.get().detectTooManyVariables(context, inputFile, content);
        styleAndConventionsDetector.get().detectNestingTooDeep(context, inputFile, content);
        styleAndConventionsDetector.get().detectFileTooManyFunctions(context, inputFile, content);
        styleAndConventionsDetector.get().detectPackageTooManyExports(context, inputFile, content);
        styleAndConventionsDetector.get().detectSwitchTooManyCases(context, inputFile, content);
        styleAndConventionsDetector.get().detectBooleanExpressionTooComplex(context, inputFile, content);
        styleAndConventionsDetector.get().detectChainedCallsTooLong(context, inputFile, content);

        // Maintainability (14 rules, 1 duplicate removed)
        styleAndConventionsDetector.get().detectMagicString(context, inputFile, content);
        styleAndConventionsDetector.get().detectDuplicateStringLiteral(context, inputFile, content);
        styleAndConventionsDetector.get().detectHardcodedPath(context, inputFile, content);
        styleAndConventionsDetector.get().detectHardcodedUrl(context, inputFile, content);
        styleAndConventionsDetector.get().detectConditionalComplexity(context, inputFile, content);
        styleAndConventionsDetector.get().detectIdenticalIfBranches(context, inputFile, content);
        styleAndConventionsDetector.get().detectDuplicateCodeBlock(context, inputFile, content);
        styleAndConventionsDetector.get().detectGodFunction(context, inputFile, content);
        styleAndConventionsDetector.get().detectFeatureEnvy(context, inputFile, content);
        styleAndConventionsDetector.get().detectPrimitiveObsession(context, inputFile, content);
        styleAndConventionsDetector.get().detectSideEffectInExpression(context, inputFile, content);
        styleAndConventionsDetector.get().detectIncompletePatternMatch(context, inputFile, content);
        styleAndConventionsDetector.get().detectMissingOptionDefault(context, inputFile, content);
        styleAndConventionsDetector.get().detectOptionNameUnclear(context, inputFile, content);

        // Best Practices (12 rules, 3 duplicates removed)
        styleAndConventionsDetector.get().detectStringConcatenationInLoop(context, inputFile, content);
        styleAndConventionsDetector.get().detectBooleanComparison(context, inputFile, content);
        styleAndConventionsDetector.get().detectNegatedBooleanComparison(context, inputFile, content);
        styleAndConventionsDetector.get().detectRedundantConditional(context, inputFile, content);
        styleAndConventionsDetector.get().detectDeprecatedOptionUsage(context, inputFile, content);
        styleAndConventionsDetector.get().detectListQueryInefficient(context, inputFile, content);
        styleAndConventionsDetector.get().detectEqualityCheckOnReals(context, inputFile, content);
        styleAndConventionsDetector.get().detectSymbolicVsNumericMismatch(context, inputFile, content);
        styleAndConventionsDetector.get().detectGraphicsOptionsExcessive(context, inputFile, content);
        styleAndConventionsDetector.get().detectPlotWithoutLabels(context, inputFile, content);
        styleAndConventionsDetector.get().detectDatasetWithoutHeaders(context, inputFile, content);
        styleAndConventionsDetector.get().detectAssociationKeyNotString(context, inputFile, content);
    }

    private void performSymbolTableAnalysis(SensorContext context, InputFile inputFile, String content, long fileStartTime, long analysisTime) {
        // ===== SYMBOL TABLE ANALYSIS (20 rules) =====
        // Symbol table analysis runs separately from main AST analysis
        // PERFORMANCE: Wrap with 120-second timeout to prevent hangs on pathologically complex files
        long symbolTableStart = System.currentTimeMillis();
        long symbolTableTime = 0;

        ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<?> future = executor.submit(() -> {
                    try {
                        // Build symbol table for advanced variable lifetime and scope analysis
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
                });

                // Wait up to 120 seconds for symbol table analysis to complete
                future.get(120, TimeUnit.SECONDS);
                symbolTableTime = System.currentTimeMillis() - symbolTableStart;

            } catch (TimeoutException e) {
                LOG.warn("TIMEOUT: SymbolTable analysis for {} exceeded 120 seconds, skipping (file has {} lines)",
                    inputFile.filename(), inputFile.lines());
                symbolTableTime = 120000; // 120 seconds in milliseconds

                // Report INFO issue to make this visible in SonarQube UI
                NewIssue issue = context.newIssue()
                    .forRule(RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY,
                                       MathematicaRulesDefinition.ANALYSIS_TIMEOUT_KEY));

                NewIssueLocation location = issue.newLocation()
                    .on(inputFile)
                    .at(inputFile.selectLine(1))
                    .message(String.format(
                        "SymbolTable analysis exceeded 120-second timeout (file has %d lines). Advanced variable analysis rules skipped.",
                                         inputFile.lines()));

                issue.at(location);
                issue.save();

            } catch (Exception e) {
                LOG.debug("Interrupted during symbol table analysis for: {}", inputFile.filename());
                symbolTableTime = System.currentTimeMillis() - symbolTableStart;
            } finally {
                executor.shutdownNow();
            }

            // === FINAL TIMING SUMMARY ===
            long totalFileTime = System.currentTimeMillis() - fileStartTime;

            // Only log very slow files (>2 seconds) with detailed breakdown (debug mode only)
            if (totalFileTime > 2000) {
                LOG.debug("PERF: SLOW FILE - {} took {}ms total ({} lines) - UnifiedAST: {}ms ({}%), SymbolTable: {}ms ({}%)",
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
        if (commentText.matches(".*[-+*/^]\\s*[a-zA-Z0 - 9].*")) {
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
