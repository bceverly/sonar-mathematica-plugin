package org.sonar.plugins.mathematica.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * Detector for Framework-specific rules (18 rules total).
 * Handles Notebook, Manipulate/Dynamic, Package, Parallel, and Cloud patterns.
 */
public class FrameworkDetector extends BaseDetector {

    // ===== TIER 1 GAP CLOSURE - FRAMEWORK DETECTION (18 rules) =====

    // Notebook patterns
    private static final Pattern CELL_PATTERN = Pattern.compile("Cell\\["); //NOSONAR - Possessive quantifiers prevent backtracking
    private static final Pattern SECTION_PATTERN = Pattern.compile("(?:Section|Subsection|Title|Subtitle)\\["); //NOSONAR
    private static final Pattern INIT_CELL_PATTERN = Pattern.compile("InitializationCell\\s*+->\\s*+True"); //NOSONAR

    // Manipulate/Dynamic patterns
    private static final Pattern MANIPULATE_PATTERN = Pattern.compile("Manipulate\\s*+\\["); //NOSONAR - Possessive quantifiers prevent backtracking
    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("Dynamic\\s*+\\["); //NOSONAR - Possessive quantifiers prevent backtracking
    private static final Pattern MANIPULATE_CONTROLS_PATTERN = Pattern.compile("\\{\\s*+\\w+\\s*+,"); //NOSONAR
    private static final Pattern HEAVY_COMPUTE_PATTERN = Pattern.compile(
        "(?:Integrate|DSolve|NDSolve|NIntegrate|FindRoot|Solve)\\s*+\\["
    );
    private static final Pattern TRACKING_PATTERN = Pattern.compile(
        "(?:TrackedSymbols|Refresh|UpdateInterval)\\s*+->"
    );

    // Package patterns
    private static final Pattern BEGIN_PACKAGE_PATTERN = Pattern.compile("BeginPackage\\s*+\\["); //NOSONAR
    private static final Pattern BEGIN_PATTERN = Pattern.compile("Begin\\s*+\\[\\s*+\"`Private`\"\\s*+\\]"); //NOSONAR
    private static final Pattern END_PACKAGE_PATTERN = Pattern.compile("EndPackage\\s*+\\["); //NOSONAR - Possessive quantifiers prevent backtracking
    private static final Pattern USAGE_MESSAGE_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)::usage\\s*+="); //NOSONAR
    private static final Pattern PUBLIC_FUNCTION_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*+\\[[^\\]]*\\]\\s*+:="); //NOSONAR
    private static final Pattern NEEDS_PATTERN = Pattern.compile("(?:Needs|Get)\\s*+\\[\\s*+\"([^\"]+)\""); //NOSONAR

    // Parallel patterns
    private static final Pattern PARALLEL_PATTERN = Pattern.compile(
        "(?:ParallelTable|ParallelMap|ParallelDo)\\s*+\\["
    );
    private static final Pattern PARALLEL_SHARED_PATTERN = Pattern.compile(
        "(?:SetSharedVariable|SetSharedFunction)\\s*+\\["
    );
    private static final Pattern CRITICAL_SECTION_PATTERN = Pattern.compile("CriticalSection\\s*+\\["); //NOSONAR

    // Cloud patterns
    private static final Pattern CLOUD_DEPLOY_PATTERN = Pattern.compile("CloudDeploy\\s*+\\["); //NOSONAR
    private static final Pattern API_FUNCTION_PATTERN = Pattern.compile("APIFunction\\s*+\\["); //NOSONAR
    private static final Pattern PERMISSIONS_PATTERN = Pattern.compile("Permissions\\s*+->\\s*+\"Public\""); //NOSONAR

    /**
     * Detect notebook cells that are too large.
     */
    public void detectNotebookCellSize(SensorContext context, InputFile inputFile, String content) {
        try {
            // Simple heuristic: check file structure suggests notebook
            if (!content.contains("Cell[") && !content.contains("Notebook[")) {
                return;
            }

            Matcher matcher = CELL_PATTERN.matcher(content);
            while (matcher.find()) {
                int cellStart = matcher.start();
                // Find matching closing bracket
                int depth = 1;
                int pos = matcher.end();
                while (pos < content.length() && depth > 0) {
                    char c = content.charAt(pos);
                    if (c == '[') {
                        depth++;
                    } else if (c == ']') {
                        depth--;
                    }
                    pos++;
                }

                String cellContent = content.substring(cellStart, Math.min(pos, content.length()));
                int cellLines = cellContent.split("\n").length;

                if (cellLines > 50) {
                    int lineNumber = calculateLineNumber(content, cellStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NOTEBOOK_CELL_SIZE_KEY,
                        String.format("Notebook cell is %d lines (max 50). Break into smaller cells.", cellLines));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping notebook cell size detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect unorganized notebooks.
     */
    public void detectNotebookUnorganized(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.contains("Cell[") && !content.contains("Notebook[")) {
                return;
            }

            // Check if notebook has both code and test cells mixed
            boolean hasCode = content.contains("Cell[") && content.contains(":=");
            boolean hasTests = content.contains("VerificationTest");
            boolean hasScratch = content.contains("(*") && content.contains("test") || content.contains("scratch");

            if (hasCode && hasTests && hasScratch) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.NOTEBOOK_UNORGANIZED_KEY,
                    "Notebook mixes code, tests, and scratch work. Organize into sections.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping notebook organization detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect notebooks without section headers.
     */
    public void detectNotebookNoSections(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.contains("Cell[") && !content.contains("Notebook[")) {
                return;
            }

            Matcher cellMatcher = CELL_PATTERN.matcher(content);
            int cellCount = 0;
            while (cellMatcher.find()) {
                cellCount++;
            }

            Matcher sectionMatcher = SECTION_PATTERN.matcher(content);
            int sectionCount = 0;
            while (sectionMatcher.find()) {
                sectionCount++;
            }

            if (cellCount > 10 && sectionCount == 0) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.NOTEBOOK_NO_SECTIONS_KEY,
                    "Notebook has no section headers. Add Section/Subsection cells for organization.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping notebook sections detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect initialization cell misuse.
     */
    public void detectNotebookInitCellMisuse(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = INIT_CELL_PATTERN.matcher(content);
            while (matcher.find()) {
                // Check context for heavy computations or side effects
                String contextWindow = content.substring(Math.max(0, matcher.start() - 200),
                    Math.min(content.length(), matcher.end() + 200));

                if (contextWindow.contains("Integrate[") || contextWindow.contains("Solve[")
                    || contextWindow.contains("Plot[") || contextWindow.contains("Export[")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NOTEBOOK_INIT_CELL_MISUSE_KEY,
                        "Initialization cell contains heavy computation or side effects. Keep init cells lightweight.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping init cell misuse detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect performance issues in Manipulate.
     */
    public void detectManipulatePerformance(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher manipulateMatcher = MANIPULATE_PATTERN.matcher(content);
            while (manipulateMatcher.find()) {
                int manipulateStart = manipulateMatcher.start();
                String manipulateBody = content.substring(manipulateStart,
                    Math.min(manipulateStart + 500, content.length()));

                Matcher heavyMatcher = HEAVY_COMPUTE_PATTERN.matcher(manipulateBody);
                if (heavyMatcher.find()) {
                    int lineNumber = calculateLineNumber(content, manipulateStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.MANIPULATE_PERFORMANCE_KEY,
                        "Manipulate contains heavy computation causing UI lag. Precompute or cache results.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Manipulate performance detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect heavy computation in Dynamic.
     */
    public void detectDynamicHeavyComputation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher dynamicMatcher = DYNAMIC_PATTERN.matcher(content);
            while (dynamicMatcher.find()) {
                int dynamicStart = dynamicMatcher.start();
                String dynamicBody = content.substring(dynamicStart,
                    Math.min(dynamicStart + 300, content.length()));

                Matcher heavyMatcher = HEAVY_COMPUTE_PATTERN.matcher(dynamicBody);
                if (heavyMatcher.find()) {
                    int lineNumber = calculateLineNumber(content, dynamicStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.DYNAMIC_HEAVY_COMPUTATION_KEY,
                        "Dynamic contains expensive computation. Move outside Dynamic or use memoization.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Dynamic heavy computation detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing tracking in Dynamic.
     */
    public void detectDynamicNoTracking(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher dynamicMatcher = DYNAMIC_PATTERN.matcher(content);
            while (dynamicMatcher.find()) {
                int dynamicStart = dynamicMatcher.start();
                String dynamicBody = content.substring(dynamicStart,
                    Math.min(dynamicStart + 200, content.length()));

                if (!dynamicBody.contains("TrackedSymbols") && !dynamicBody.contains("Refresh")) {
                    // Only report if Dynamic has multiple variable references
                    int varCount = countOccurrences(dynamicBody, "[a-z][a-zA-Z0-9]*");
                    if (varCount > 3) {
                        int lineNumber = calculateLineNumber(content, dynamicStart);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.DYNAMIC_NO_TRACKING_KEY,
                            "Dynamic with multiple variables should use TrackedSymbols for explicit control.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Dynamic tracking detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect overly complex Manipulate.
     */
    public void detectManipulateTooComplex(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MANIPULATE_PATTERN.matcher(content);
            while (matcher.find()) {
                int manipulateStart = matcher.start();
                String manipulateBody = content.substring(manipulateStart,
                    Math.min(manipulateStart + 1000, content.length()));

                Matcher controlMatcher = MANIPULATE_CONTROLS_PATTERN.matcher(manipulateBody);
                int controlCount = 0;
                while (controlMatcher.find()) {
                    controlCount++;
                }

                if (controlCount > 10) {
                    int lineNumber = calculateLineNumber(content, manipulateStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.MANIPULATE_TOO_COMPLEX_KEY,
                        String.format("Manipulate has %d controls (max 10). Break into multiple interfaces.", controlCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Manipulate complexity detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect packages without Begin/End.
     */
    public void detectPackageNoBegin(SensorContext context, InputFile inputFile, String content) {
        try {
            boolean hasBeginPackage = content.contains("BeginPackage[");
            boolean hasBegin = content.contains("Begin[`Private`]");
            boolean hasEndPackage = content.contains("EndPackage[");

            if (hasBeginPackage && !hasBegin) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.PACKAGE_NO_BEGIN_KEY,
                    "Package missing Begin[`Private`]. Use proper context management.");
            }

            if (hasBeginPackage && !hasEndPackage) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.PACKAGE_NO_BEGIN_KEY,
                    "Package missing EndPackage[]. Ensure proper context closure.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping package Begin/End detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect mixing of public and private symbols.
     */
    public void detectPackagePublicPrivateMix(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.contains("BeginPackage[")) {
                return;
            }

            // Check if public functions are defined after Begin[`Private`]
            Matcher beginMatcher = BEGIN_PATTERN.matcher(content);
            if (beginMatcher.find()) {
                int privateStart = beginMatcher.start();
                String afterPrivate = content.substring(privateStart);

                Matcher publicMatcher = PUBLIC_FUNCTION_PATTERN.matcher(afterPrivate);
                if (publicMatcher.find()) {
                    int lineNumber = calculateLineNumber(content, privateStart + publicMatcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PACKAGE_PUBLIC_PRIVATE_MIX_KEY,
                        "Public function defined in Private` context. Move before Begin[`Private`].");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping package public/private mix detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect public functions without usage messages.
     */
    public void detectPackageNoUsage(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.contains("BeginPackage[")) {
                return;
            }

            Matcher funcMatcher = PUBLIC_FUNCTION_PATTERN.matcher(content);
            java.util.Set<String> publicFunctions = new java.util.HashSet<>();
            while (funcMatcher.find()) {
                publicFunctions.add(funcMatcher.group(1));
            }

            Matcher usageMatcher = USAGE_MESSAGE_PATTERN.matcher(content);
            java.util.Set<String> documented = new java.util.HashSet<>();
            while (usageMatcher.find()) {
                documented.add(usageMatcher.group(1));
            }

            for (String func : publicFunctions) {
                if (!documented.contains(func)) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.PACKAGE_NO_USAGE_KEY,
                        String.format("Public function '%s' missing ::usage message.", func));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping package usage detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect circular package dependencies.
     */
    public void detectPackageCircularDependency(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NEEDS_PATTERN.matcher(content);
            java.util.List<String> dependencies = new java.util.ArrayList<>();
            while (matcher.find()) {
                dependencies.add(matcher.group(1));
            }

            // Simple heuristic: detect if file name appears in dependencies
            String fileName = inputFile.filename().replace(".wl", "").replace(".m", "");
            for (String dep : dependencies) {
                if (dep.contains(fileName)) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.PACKAGE_CIRCULAR_DEPENDENCY_KEY,
                        String.format("Potential circular dependency with package '%s'.", dep));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping circular dependency detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect parallel operations without sufficient workload.
     */
    public void detectParallelNoGain(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PARALLEL_PATTERN.matcher(content);
            while (matcher.find()) {
                int parallelStart = matcher.start();
                String parallelBody = content.substring(parallelStart,
                    Math.min(parallelStart + 200, content.length()));

                // Check if workload is trivial
                if (!parallelBody.contains("Integrate") && !parallelBody.contains("Solve")
                    && !parallelBody.contains("NDSolve") && !parallelBody.contains("NIntegrate")) {
                    int lineNumber = calculateLineNumber(content, parallelStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PARALLEL_NO_GAIN_KEY,
                        "Parallel operation on trivial workload. Overhead may exceed benefit.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping parallel no gain detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect race conditions in parallel code.
     */
    public void detectParallelRaceCondition(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.contains("Parallel")) {
                return;
            }

            Matcher matcher = PARALLEL_PATTERN.matcher(content);
            while (matcher.find()) {
                int parallelStart = matcher.start();
                String parallelBody = content.substring(parallelStart,
                    Math.min(parallelStart + 300, content.length()));

                // Check for shared variable mutations without CriticalSection
                if (parallelBody.contains("AppendTo[") || parallelBody.contains("PrependTo[")
                    || parallelBody.matches(".*[A-Z][a-zA-Z0-9]*\\s*+=(?!=).*")) {
                    if (!parallelBody.contains("CriticalSection")) {
                        int lineNumber = calculateLineNumber(content, parallelStart);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PARALLEL_RACE_CONDITION_KEY,
                            "Parallel code modifies shared state without CriticalSection. Race condition risk.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping parallel race condition detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect shared mutable state in parallel operations.
     */
    public void detectParallelSharedState(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher sharedMatcher = PARALLEL_SHARED_PATTERN.matcher(content);
            if (sharedMatcher.find()) {
                int lineNumber = calculateLineNumber(content, sharedMatcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PARALLEL_SHARED_STATE_KEY,
                    "Shared mutable state in parallel code can cause race conditions. Minimize sharing.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping parallel shared state detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect cloud API without authentication.
     */
    public void detectCloudApiMissingAuth(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = API_FUNCTION_PATTERN.matcher(content);
            while (matcher.find()) {
                int apiStart = matcher.start();
                String apiBody = content.substring(apiStart,
                    Math.min(apiStart + 300, content.length()));

                if (!apiBody.contains("Authentication") && !apiBody.contains("Permissions")) {
                    int lineNumber = calculateLineNumber(content, apiStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CLOUD_API_MISSING_AUTH_KEY,
                        "Cloud API endpoint missing authentication. Add Authentication or Permissions.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping cloud API auth detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect overly permissive cloud permissions.
     */
    public void detectCloudPermissionsTooOpen(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PERMISSIONS_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CLOUD_PERMISSIONS_TOO_OPEN_KEY,
                    "Cloud permissions set to 'Public'. Use least privilege principle.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping cloud permissions detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect cloud deployments without input validation.
     */
    public void detectCloudDeployNoValidation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher cloudMatcher = CLOUD_DEPLOY_PATTERN.matcher(content);
            Matcher apiMatcher = API_FUNCTION_PATTERN.matcher(content);

            if (cloudMatcher.find() || apiMatcher.find()) {
                // Check for validation patterns
                if (!content.contains("StringQ") && !content.contains("NumericQ")
                    && !content.contains("IntegerQ") && !content.contains("MatchQ")) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.CLOUD_DEPLOY_NO_VALIDATION_KEY,
                        "Cloud deployment missing input validation. Validate all user inputs.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping cloud deployment validation detection: {}", inputFile.filename());
        }
    }
}
