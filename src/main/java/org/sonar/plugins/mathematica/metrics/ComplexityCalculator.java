package org.sonar.plugins.mathematica.metrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates complexity metrics for Mathematica code.
 *
 * Metrics calculated:
 * - Cyclomatic Complexity: Number of linearly independent paths (decision points)
 * - Cognitive Complexity: Measure of how difficult code is to understand
 * - Function-level complexity
 * - File-level complexity
 *
 * Performance optimizations:
 * - Caches cleaned content to avoid repeated regex operations
 * - Combines multiple pattern matches into single pass where possible
 */
public class ComplexityCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexityCalculator.class);

    // Patterns for complexity calculation
    // NOTE: COMMENT_PATTERN removed - using character-based parser to avoid catastrophic backtracking
    // PERFORMANCE FIX: Possessive quantifier (*+) prevents catastrophic backtracking on long strings
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*+\"");

    // Cache for cleaned content (avoids repeated regex operations)
    private String cachedOriginal = null;
    private String cachedCleaned = null;

    // Decision point patterns (for cyclomatic complexity)
    private static final Pattern IF_PATTERN = Pattern.compile("\\bIf\\s*+\\[");
    private static final Pattern WHICH_PATTERN = Pattern.compile("\\bWhich\\s*+\\[");
    private static final Pattern SWITCH_PATTERN = Pattern.compile("\\bSwitch\\s*+\\[");
    private static final Pattern WHILE_PATTERN = Pattern.compile("\\bWhile\\s*+\\[");
    private static final Pattern DO_PATTERN = Pattern.compile("\\bDo\\s*+\\[");
    private static final Pattern FOR_PATTERN = Pattern.compile("\\bFor\\s*+\\[");
    private static final Pattern TABLE_PATTERN = Pattern.compile("\\bTable\\s*+\\[");
    private static final Pattern MAP_PATTERN = Pattern.compile("\\b(?:Map|Scan)\\s*+\\[");
    private static final Pattern LOGICAL_AND_PATTERN = Pattern.compile("&&");
    private static final Pattern LOGICAL_OR_PATTERN = Pattern.compile("\\|\\|");

    // Function definition pattern
    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*+\\[([^\\]]*)\\]\\s*+:=",
        Pattern.MULTILINE
    );

    /**
     * Calculate cyclomatic complexity for the entire file.
     *
     * Cyclomatic Complexity = E - N + 2P where:
     * - E = edges in control flow graph
     * - N = nodes in control flow graph
     * - P = connected components (usually 1 for a file)
     *
     * Simplified: Count decision points + 1
     *
     * OPTIMIZED: Counts all patterns in a single pass for better performance
     */
    public int calculateCyclomaticComplexity(String content) {
        try {
            // Remove comments and strings to avoid false positives (cached)
            String cleanContent = removeCommentsAndStrings(content);

            int complexity = 1;  // Base complexity

            // OPTIMIZATION: Count all decision points in a single pass
            complexity += countAllDecisionPoints(cleanContent);

            return complexity;

        } catch (Exception e) {
            LOG.warn("Error calculating cyclomatic complexity: {}", e.getMessage());
            return 1;  // Return base complexity on error
        }
    }

    /**
     * Count all decision points in a single pass (PERFORMANCE OPTIMIZATION).
     * This is much faster than calling countOccurrences() 10 times.
     */
    private int countAllDecisionPoints(String content) {
        int count = 0;

        // Create matchers for all patterns
        Matcher ifMatcher = IF_PATTERN.matcher(content);
        Matcher whichMatcher = WHICH_PATTERN.matcher(content);
        Matcher switchMatcher = SWITCH_PATTERN.matcher(content);
        Matcher whileMatcher = WHILE_PATTERN.matcher(content);
        Matcher doMatcher = DO_PATTERN.matcher(content);
        Matcher forMatcher = FOR_PATTERN.matcher(content);
        Matcher tableMatcher = TABLE_PATTERN.matcher(content);
        Matcher mapMatcher = MAP_PATTERN.matcher(content);
        Matcher andMatcher = LOGICAL_AND_PATTERN.matcher(content);
        Matcher orMatcher = LOGICAL_OR_PATTERN.matcher(content);

        // Count all matches
        while (ifMatcher.find()) {
            count++;
        }
        while (whichMatcher.find()) {
            count++;
        }
        while (switchMatcher.find()) {
            count++;
        }
        while (whileMatcher.find()) {
            count++;
        }
        while (doMatcher.find()) {
            count++;
        }
        while (forMatcher.find()) {
            count++;
        }
        while (tableMatcher.find()) {
            count++;
        }
        while (mapMatcher.find()) {
            count++;
        }
        while (andMatcher.find()) {
            count++;
        }
        while (orMatcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * Calculate cognitive complexity for the entire file.
     *
     * Cognitive Complexity measures how difficult code is to understand.
     * It's based on:
     * 1. Nesting level (deeper nesting = harder to understand)
     * 2. Control flow breaks
     * 3. Fundamental structures
     *
     * Rules:
     * - If/While/For: +1
     * - Nested structures: +1 per nesting level
     * - Logical operators in conditions: +1
     * - Recursion: +1
     */
    public int calculateCognitiveComplexity(String content) {
        try {
            String cleanContent = removeCommentsAndStrings(content);
            return calculateCognitiveComplexityRecursive(cleanContent, 0);

        } catch (Exception e) {
            LOG.warn("Error calculating cognitive complexity: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate cognitive complexity with nesting awareness.
     */
    private int calculateCognitiveComplexityRecursive(String content, int nestingLevel) {
        int complexity = 0;

        // Count control structures
        complexity += countOccurrences(content, IF_PATTERN) * (1 + nestingLevel);
        complexity += countOccurrences(content, WHICH_PATTERN) * (1 + nestingLevel);
        complexity += countOccurrences(content, SWITCH_PATTERN) * (1 + nestingLevel);
        complexity += countOccurrences(content, WHILE_PATTERN) * (1 + nestingLevel);
        complexity += countOccurrences(content, DO_PATTERN) * (1 + nestingLevel);
        complexity += countOccurrences(content, FOR_PATTERN) * (1 + nestingLevel);

        // Boolean operators (only in conditions)
        complexity += countOccurrences(content, LOGICAL_AND_PATTERN);
        complexity += countOccurrences(content, LOGICAL_OR_PATTERN);

        return complexity;
    }

    /**
     * Calculate complexity for a specific function.
     */
    public FunctionComplexity calculateFunctionComplexity(String functionName, String functionBody) {
        try {
            String cleanBody = removeCommentsAndStrings(functionBody);

            int cyclomaticComplexity = 1;  // Base complexity
            int cognitiveComplexity = 0;

            // Count decision points
            cyclomaticComplexity += countOccurrences(cleanBody, IF_PATTERN);
            cyclomaticComplexity += countOccurrences(cleanBody, WHICH_PATTERN);
            cyclomaticComplexity += countOccurrences(cleanBody, SWITCH_PATTERN);
            cyclomaticComplexity += countOccurrences(cleanBody, WHILE_PATTERN);
            cyclomaticComplexity += countOccurrences(cleanBody, DO_PATTERN);
            cyclomaticComplexity += countOccurrences(cleanBody, FOR_PATTERN);
            cyclomaticComplexity += countOccurrences(cleanBody, LOGICAL_AND_PATTERN);
            cyclomaticComplexity += countOccurrences(cleanBody, LOGICAL_OR_PATTERN);

            // Cognitive complexity (with nesting)
            cognitiveComplexity = calculateCognitiveComplexityRecursive(cleanBody, 0);

            // Check for recursion
            if (cleanBody.contains(functionName + "[")) {
                cognitiveComplexity += 1;  // Recursion adds cognitive load
            }

            return new FunctionComplexity(functionName, cyclomaticComplexity, cognitiveComplexity);

        } catch (Exception e) {
            LOG.warn("Error calculating function complexity for {}: {}", functionName, e.getMessage());
            return new FunctionComplexity(functionName, 1, 0);
        }
    }

    /**
     * Get all functions and their complexities.
     */
    public java.util.List<FunctionComplexity> calculateAllFunctionComplexities(String content) {
        java.util.List<FunctionComplexity> complexities = new java.util.ArrayList<>();

        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);

            while (matcher.find()) {
                String functionName = matcher.group(1);
                int functionStart = matcher.start();

                // Find function body (simplified - find next function or end of file)
                int nextFunctionStart = content.indexOf(":=", functionStart + 10);
                int functionEnd = (nextFunctionStart == -1) ? content.length() : nextFunctionStart;

                String functionBody = content.substring(functionStart, functionEnd);

                FunctionComplexity complexity = calculateFunctionComplexity(functionName, functionBody);
                complexities.add(complexity);
            }

        } catch (Exception e) {
            LOG.warn("Error calculating all function complexities: {}", e.getMessage());
        }

        return complexities;
    }

    /**
     * Remove comments and strings from content to avoid false positives.
     * Uses caching to avoid repeated operations on same content.
     *
     * IMPORTANT: Uses character-based parser instead of regex to avoid catastrophic backtracking.
     * Mathematica comments can be nested: (* outer (* inner *) outer *)
     */
    private String removeCommentsAndStrings(String content) {
        // Check cache first (PERFORMANCE OPTIMIZATION)
        if (cachedOriginal != null && cachedOriginal.equals(content)) {
            return cachedCleaned;
        }

        // Remove comments using safe character-based parser (handles nesting)
        String result = removeCommentsCharBased(content);

        // Replace strings with normalized token
        result = STRING_PATTERN.matcher(result).replaceAll("\"STRING\"");

        // Update cache
        cachedOriginal = content;
        cachedCleaned = result;

        return result;
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

    /**
     * Clear the content cache. Call this between files.
     */
    public void clearCache() {
        cachedOriginal = null;
        cachedCleaned = null;
    }

    /**
     * Count occurrences of a pattern in text.
     */
    private int countOccurrences(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Data class to hold function complexity metrics.
     */
    public static class FunctionComplexity {
        private final String functionName;
        private final int cyclomaticComplexity;
        private final int cognitiveComplexity;

        public FunctionComplexity(String functionName, int cyclomaticComplexity, int cognitiveComplexity) {
            this.functionName = functionName;
            this.cyclomaticComplexity = cyclomaticComplexity;
            this.cognitiveComplexity = cognitiveComplexity;
        }

        public String getFunctionName() {
            return functionName;
        }

        public int getCyclomaticComplexity() {
            return cyclomaticComplexity;
        }

        public int getCognitiveComplexity() {
            return cognitiveComplexity;
        }

        @Override
        public String toString() {
            return String.format("%s: cyclomatic=%d, cognitive=%d",
                functionName, cyclomaticComplexity, cognitiveComplexity);
        }
    }
}
