package org.sonar.plugins.mathematica.rules;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all rule detectors providing shared utilities and patterns.
 */
public abstract class BaseDetector {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseDetector.class);

    /**
     * Parameter object for issue reporting with Quick Fix data.
     * Reduces method parameter count from 8 to 2.
     */
    protected static class IssueWithFixData {
        final InputFile inputFile;
        final int line;
        final String ruleKey;
        final String message;
        final int startOffset;
        final int endOffset;
        final org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext fixContext;

        public IssueWithFixData(InputFile inputFile, int line, String ruleKey, String message,
                                int startOffset, int endOffset,
                                org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext fixContext) {
            this.inputFile = inputFile;
            this.line = line;
            this.ruleKey = ruleKey;
            this.message = message;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.fixContext = fixContext;
        }
    }

    // Reference to sensor for queuing issues
    protected MathematicaRulesSensor sensor;

    public void setSensor(MathematicaRulesSensor sensor) {
        this.sensor = sensor;
    }

    // Thread-local caches for performance (cleared per file)
    protected ThreadLocal<int[]> lineOffsetCache = new ThreadLocal<>();
    protected ThreadLocal<String[]> linesCache = new ThreadLocal<>();
    protected ThreadLocal<String> contentCache = new ThreadLocal<>();
    protected ThreadLocal<java.util.List<org.sonar.plugins.mathematica.ast.AstNode>> astCache = new ThreadLocal<>();

    // Pattern cache for dynamic patterns
    protected static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    // Common patterns used across detectors
    // NOTE: COMMENT_PATTERN removed - use removeCommentsCharBased() to avoid catastrophic backtracking
    //NOSONAR - Possessive quantifiers prevent backtracking
    protected static final Pattern STRING_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)++\""); //NOSONAR

    /**
     * Initialize caches for a file. Alias for clearCaches(String content).
     * PERFORMANCE: Parse AST once per file and cache for reuse by multiple rules.
     */
    public void initializeCaches(String content) {
        clearCaches(content);
    }

    /**
     * Initialize caches for a file.
     * PERFORMANCE: Parse AST once per file and cache for reuse by multiple rules.
     */
    protected void clearCaches(String content) {
        contentCache.set(content);
        lineOffsetCache.set(buildLineOffsetArray(content));
        linesCache.set(content.split("\n", -1));

        // PERFORMANCE: Parse AST once and cache
        try {
            org.sonar.plugins.mathematica.ast.MathematicaParser parser =
                new org.sonar.plugins.mathematica.ast.MathematicaParser();
            java.util.List<org.sonar.plugins.mathematica.ast.AstNode> ast = parser.parse(content);
            astCache.set(ast);
        } catch (Exception e) {
            LOG.debug("Failed to cache AST: {}", e.getMessage());
            astCache.remove();
        }
    }

    /**
     * Clear all caches after processing a file.
     */
    protected void clearCaches() {
        lineOffsetCache.remove();
        linesCache.remove();
        contentCache.remove();
        astCache.remove();
    }

    /**
     * Builds an array of line start offsets for fast O(log n) line number lookup.
     */
    protected int[] buildLineOffsetArray(String content) {
        int lineCount = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lineCount++;
            }
        }

        int[] offsets = new int[lineCount];
        offsets[0] = 0;
        int lineIndex = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n' && lineIndex < offsets.length) {
                offsets[lineIndex++] = i + 1;
            }
        }
        return offsets;
    }

    /**
     * Calculates line number using cached offset array (O(log n) binary search).
     */
    protected int calculateLineNumber(String content, int offset) {
        int[] offsets = lineOffsetCache.get();
        if (offsets == null) {
            // Fallback
            int line = 1;
            for (int i = 0; i < offset && i < content.length(); i++) {
                if (content.charAt(i) == '\n') {
                    line++;
                }
            }
            return line;
        }

        // Binary search
        int left = 0;
        int right = offsets.length - 1;
        while (left < right) {
            int mid = (left + right + 1) / 2;
            if (offsets[mid] <= offset) {
                left = mid;
            } else {
                right = mid - 1;
            }
        }
        return left + 1;
    }

    /**
     * Reports an issue at a specific line by queuing it for the background saver thread.
     */
    protected void reportIssue(SensorContext context, InputFile inputFile, int line, String ruleKey, String message) {
        if (sensor != null) {
            // Queue the issue data for background thread to create and save
            sensor.queueIssue(inputFile, line, ruleKey, message);
        } else {
            // Fallback: create and save directly (shouldn't happen)
            LOG.warn("Sensor not set, falling back to direct save");
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

    /**
     * Reports an issue with Quick Fix data.
     *
     * @param context Sensor context
     * @param inputFile The file with the issue
     * @param line Line number of the issue
     * @param ruleKey Rule key identifying the rule
     * @param message Issue message
     * @param startOffset Character offset where the issue starts
     * @param endOffset Character offset where the issue ends
     */
    protected void reportIssueWithFix(SensorContext context, InputFile inputFile, int line, String ruleKey, String message,
                                     int startOffset, int endOffset) {
        if (sensor != null) {
            String fileContent = contentCache.get();
            org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext fixContext =
                new org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext();
            MathematicaRulesSensor.QuickFixData quickFixData =
                new MathematicaRulesSensor.QuickFixData(fileContent, startOffset, endOffset, fixContext);
            sensor.queueIssueWithFix(inputFile, line, ruleKey, message, quickFixData);
        } else {
            // Fallback: report without fix
            reportIssue(context, inputFile, line, ruleKey, message);
        }
    }

    /**
     * Reports an issue with Quick Fix data using parameter object.
     * Refactored to reduce parameter count from 8 to 2.
     */
    protected void reportIssueWithFix(SensorContext context, IssueWithFixData issueData) {
        if (sensor != null) {
            String fileContent = contentCache.get();
            MathematicaRulesSensor.QuickFixData quickFixData =
                new MathematicaRulesSensor.QuickFixData(fileContent, issueData.startOffset,
                                                        issueData.endOffset, issueData.fixContext);
            sensor.queueIssueWithFix(issueData.inputFile, issueData.line, issueData.ruleKey,
                                     issueData.message, quickFixData);
        } else {
            // Fallback: report without fix
            reportIssue(context, issueData.inputFile, issueData.line, issueData.ruleKey, issueData.message);
        }
    }

    /**
     * Helper to count pattern occurrences using cached patterns.
     */
    protected int countOccurrences(String text, String patternString) {
        try {
            Pattern pattern = PATTERN_CACHE.computeIfAbsent(patternString, Pattern::compile);
            Matcher matcher = pattern.matcher(text);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Checks if a position is inside a string literal.
     * Properly handles multi-line strings by scanning from the beginning of content.
     * Uses both forward scanning and context checking for robustness.
     */
    protected boolean isInsideStringLiteral(String content, int position) {
        if (position >= content.length() || position < 0) {
            return false;
        }

        // First, do a quick context check: if there's a quote before this position
        // on the same line and no closing quote between the quote and position,
        // we're likely inside a string
        int lineStart = content.lastIndexOf('\n', position) + 1;
        // Defensive: ensure lineStart is not after position
        if (lineStart > position) {
            lineStart = 0;
        }
        String linePrefix = content.substring(lineStart, position);

        // Count unescaped quotes in the line up to this position
        int quoteCount = countUnescapedQuotes(linePrefix);

        // If we have an odd number of quotes before this position on this line,
        // we're inside a string
        if (quoteCount % 2 == 1) {
            return true;
        }

        // For multi-line strings, do full scan from beginning
        return isInsideMultiLineString(content, position);
    }

    private int countUnescapedQuotes(String text) {
        int quoteCount = 0;
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                quoteCount++;
            }
        }
        return quoteCount;
    }

    private boolean isInsideMultiLineString(String content, int position) {
        boolean insideString = false;
        boolean escaped = false;

        for (int i = 0; i < position; i++) {
            char c = content.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                insideString = !insideString;
            }
        }

        return insideString;
    }

    /**
     * Checks if text looks like natural language rather than code.
     */
    protected static final String[] NATURAL_LANGUAGE_PHRASES = {
        "this is", "this function", "this will", "this should",
        "note that", "hack", "bug",
        "returns", "calculates", "computes", "sets", "gets",
        "the following", "for example", "such as"
    };

    protected boolean looksLikeNaturalLanguage(String text) {
        String lowerText = text.toLowerCase();
        int naturalLanguageIndicators = 0;
        for (String phrase : NATURAL_LANGUAGE_PHRASES) {
            if (lowerText.contains(phrase)) {
                naturalLanguageIndicators++;
                if (naturalLanguageIndicators >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Analyzes comments and returns their ranges using character-based parsing.
     * This is O(n) and handles nested comments without catastrophic backtracking.
     */
    protected List<int[]> analyzeComments(String content) {
        List<int[]> commentRanges = new java.util.ArrayList<>();
        CommentState state = new CommentState();
        int i = 0;

        while (i < content.length()) {
            i = processCommentCharacter(content, i, state, commentRanges);
        }

        return commentRanges;
    }

    /**
     * Process a single character position for comment parsing.
     */
    private int processCommentCharacter(String content, int i, CommentState state, List<int[]> commentRanges) {
        if (isCommentStart(content, i)) {
            return handleCommentStart(i, state);
        }

        if (isCommentEnd(content, i)) {
            return handleCommentEnd(i, state, commentRanges);
        }

        return i + 1;
    }

    /**
     * Check if position marks the start of a comment.
     */
    private boolean isCommentStart(String content, int i) {
        return i < content.length() - 1 && content.charAt(i) == '(' && content.charAt(i + 1) == '*';
    }

    /**
     * Check if position marks the end of a comment.
     */
    private boolean isCommentEnd(String content, int i) {
        return i < content.length() - 1 && content.charAt(i) == '*' && content.charAt(i + 1) == ')';
    }

    /**
     * Handle comment start delimiter.
     */
    private int handleCommentStart(int i, CommentState state) {
        if (state.depth == 0) {
            state.commentStart = i;
        }
        state.depth++;
        return i + 2;
    }

    /**
     * Handle comment end delimiter.
     */
    private int handleCommentEnd(int i, CommentState state, List<int[]> commentRanges) {
        if (state.depth > 0) {
            state.depth--;
            if (state.depth == 0 && state.commentStart >= 0) {
                commentRanges.add(new int[]{state.commentStart, i + 2});
                state.commentStart = -1;
            }
        }
        return i + 2;
    }

    /**
     * Helper class to track comment parsing state.
     */
    private static final class CommentState {
        int depth = 0;
        int commentStart = -1;
    }

    /**
     * Checks if a position is inside a comment.
     */
    protected boolean isInsideComment(int position, List<int[]> commentRanges) {
        for (int[] range : commentRanges) {
            if (position >= range[0] && position < range[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a position is inside a Mathematica comment (* ... *).
     * Handles nested comments properly.
     */
    protected boolean isInsideComment(String content, int position) {
        int depth = 0;
        int i = 0;
        while (i < position) {
            if (i < content.length() - 1) {
                if (content.charAt(i) == '(' && content.charAt(i + 1) == '*') {
                    depth++;
                    i += 2; // Skip both '(' and '*'
                } else if (content.charAt(i) == '*' && content.charAt(i + 1) == ')') {
                    depth--;
                    i += 2; // Skip both '*' and ')'
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }
        return depth > 0;
    }

    /**
     * Check if a function definition has non-numeric parameter types.
     * Useful for rules that should only apply to numeric functions.
     *
     * Returns true if parameters are typed as String, List, Association, etc.
     * Detects patterns like:
     * - _String, _?StringQ (string parameters)
     * - _List, {___} (list parameters)
     * - _Association, _Symbol, _Image, _Graphics, _Graph
     *
     * @param funcDef The function definition string to check
     * @return true if function has non-numeric parameters
     */
    protected boolean hasNonNumericParameters(String funcDef) {
        // Check for explicit non-numeric type patterns
        return funcDef.contains("_String")
            || funcDef.contains("_?StringQ")     // String with predicate
            || funcDef.contains("_List")
            || funcDef.contains("{___")          // List pattern {___?StringQ}, {___}, etc.
            || funcDef.contains("_Association")
            || funcDef.contains("_Symbol")
            || funcDef.contains("_Image")
            || funcDef.contains("_Graphics")
            || funcDef.contains("_Graph");
    }
}
