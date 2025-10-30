package org.sonar.plugins.mathematica.metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.mathematica.MathematicaLanguage;
import org.sonar.plugins.mathematica.metrics.ComplexityCalculator.FunctionComplexity;

/**
 * Sensor that calculates and reports complexity metrics for Mathematica files.
 *
 * Metrics reported:
 * - Cyclomatic Complexity (COMPLEXITY): Counts decision points
 * - Cognitive Complexity (COGNITIVE_COMPLEXITY): Measures understandability
 * - Function count (FUNCTIONS): Number of functions in file
 * - Statements (STATEMENTS): Executable statements
 *
 * These metrics appear in:
 * - SonarQube UI (Measures tab)
 * - Quality Gates
 * - Project dashboard
 */
public class MathematicaMetricsSensor implements Sensor {

    private static final Logger LOG = Loggers.get(MathematicaMetricsSensor.class);

    private final ComplexityCalculator complexityCalculator = new ComplexityCalculator();

    // Pre-compiled patterns for statement estimation (PERFORMANCE OPTIMIZATION)
    // NOTE: COMMENT_REMOVAL_PATTERN removed - using character-based parser to avoid catastrophic backtracking
    private static final Pattern DELAYED_ASSIGN_PATTERN = Pattern.compile(":=");
    private static final Pattern ASSIGN_PATTERN = Pattern.compile("\\s=\\s");
    private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile("[a-zA-Z]\\w*\\[");

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("Mathematica Metrics Sensor")
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

        for (InputFile inputFile : inputFiles) {
            try {
                analyzeFile(context, inputFile);
            } catch (Throwable t) {
                // Check if this is a fatal error (StackOverflowError, OutOfMemoryError, etc.)
                if (t instanceof Error) {
                    Error fatalError = (Error) t;
                    LOG.error("========================================");
                    LOG.error("FATAL ERROR in Metrics Sensor while analyzing file: {}", inputFile.filename());
                    LOG.error("Full file path: {}", inputFile.path().toAbsolutePath());
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
    }

    private void analyzeFile(SensorContext context, InputFile inputFile) {
        try {
            // NOTE: Incremental analysis removed. SonarQube's file status doesn't detect
            // plugin changes, causing metrics to skip files even when calculation logic changes.
            // Always calculate metrics for consistency and correctness.

            String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);

            // PERFORMANCE: Clear cache before processing new file
            complexityCalculator.clearCache();

            // Calculate cyclomatic complexity (caches cleaned content)
            int cyclomaticComplexity = complexityCalculator.calculateCyclomaticComplexity(content);
            saveMeasure(context, inputFile, CoreMetrics.COMPLEXITY, cyclomaticComplexity);

            // Calculate cognitive complexity (reuses cached cleaned content)
            int cognitiveComplexity = complexityCalculator.calculateCognitiveComplexity(content);
            saveMeasure(context, inputFile, CoreMetrics.COGNITIVE_COMPLEXITY, cognitiveComplexity);

            // Count functions (reuses cached cleaned content)
            java.util.List<FunctionComplexity> functionComplexities =
                complexityCalculator.calculateAllFunctionComplexities(content);
            saveMeasure(context, inputFile, CoreMetrics.FUNCTIONS, functionComplexities.size());

            // Calculate statements (executable lines - rough estimate)
            int statements = estimateStatements(content);
            saveMeasure(context, inputFile, CoreMetrics.STATEMENTS, statements);

            // Log high complexity functions for debugging
            for (FunctionComplexity fc : functionComplexities) {
                if (fc.getCyclomaticComplexity() > 15 || fc.getCognitiveComplexity() > 15) {
                    LOG.debug("High complexity function in {}: {}", inputFile.filename(), fc);
                }
            }

            LOG.debug("Metrics for {}: cyclomatic={}, cognitive={}, functions={}",
                inputFile.filename(), cyclomaticComplexity, cognitiveComplexity, functionComplexities.size());

        } catch (IOException e) {
            LOG.error("Error reading file: {}", inputFile, e);
        } catch (Exception e) {
            LOG.warn("Error calculating metrics for file: {}", inputFile, e);
        }
    }

    /**
     * Estimate the number of executable statements in the file.
     * This is a rough heuristic based on counting semicolons and assignments.
     * OPTIMIZED: Uses character-based parser to avoid catastrophic regex backtracking.
     */
    private int estimateStatements(String content) {
        // Remove comments using safe character-based parser
        String cleanContent = removeCommentsCharBased(content);

        // Count statement indicators using pre-compiled patterns
        int statements = 0;

        // Count semicolons (statement terminators)
        statements += countChar(cleanContent, ';');

        // Count assignments using pre-compiled patterns
        statements += countPatternMatches(cleanContent, DELAYED_ASSIGN_PATTERN);
        statements += countPatternMatches(cleanContent, ASSIGN_PATTERN);

        // Count function calls using pre-compiled pattern
        statements += countPatternMatches(cleanContent, FUNCTION_CALL_PATTERN);

        // Return at least 1 if file has content
        return Math.max(1, statements / 2);  // Divide by 2 to avoid double-counting
    }

    /**
     * Remove Mathematica comments using character-based parsing.
     * This is O(n) and handles nested comments correctly without catastrophic backtracking.
     *
     * Mathematica comments: (* comment *) and can be nested.
     */
    private String removeCommentsCharBased(String content) {
        StringBuilder result = new StringBuilder(content.length());
        int depth = 0;
        int i = 0;

        while (i < content.length()) {
            // Check for comment start: (*
            if (i < content.length() - 1 && content.charAt(i) == '(' && content.charAt(i + 1) == '*') {
                depth++;
                i += 2;
                continue;
            }

            // Check for comment end: *)
            if (i < content.length() - 1 && content.charAt(i) == '*' && content.charAt(i + 1) == ')') {
                if (depth > 0) {
                    depth--;
                }
                i += 2;
                continue;
            }

            // If not in comment, add character to result
            if (depth == 0) {
                result.append(content.charAt(i));
            }

            i++;
        }

        return result.toString();
    }

    private int countChar(String text, char ch) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    /**
     * Count pattern matches using pre-compiled Pattern (PERFORMANCE OPTIMIZATION).
     */
    private int countPatternMatches(String text, Pattern pattern) {
        java.util.regex.Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Save a measure to SonarQube.
     */
    private <T extends java.io.Serializable> void saveMeasure(
        SensorContext context,
        InputFile inputFile,
        Metric<T> metric,
        T value
    ) {
        try {
            context.<T>newMeasure()
                .on(inputFile)
                .forMetric(metric)
                .withValue(value)
                .save();
        } catch (Exception e) {
            LOG.warn("Error saving measure {} for file {}: {}", metric.key(), inputFile, e.getMessage());
        }
    }
}
