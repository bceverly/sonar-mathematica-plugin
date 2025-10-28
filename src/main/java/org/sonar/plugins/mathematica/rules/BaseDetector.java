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
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.mathematica.MathematicaLanguage;

/**
 * Base class for all rule detectors providing shared utilities and patterns.
 */
public abstract class BaseDetector {

    protected static final Logger LOG = Loggers.get(BaseDetector.class);

    // Thread-local caches for performance (cleared per file)
    protected ThreadLocal<int[]> lineOffsetCache = new ThreadLocal<>();
    protected ThreadLocal<String[]> linesCache = new ThreadLocal<>();
    protected ThreadLocal<String> contentCache = new ThreadLocal<>();
    protected ThreadLocal<java.util.List<org.sonar.plugins.mathematica.ast.AstNode>> astCache = new ThreadLocal<>();

    // Pattern cache for dynamic patterns
    protected static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    // Common patterns used across detectors
    protected static final Pattern COMMENT_PATTERN = Pattern.compile("\\(\\*[\\s\\S]*?\\*\\)");
    protected static final Pattern STRING_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");

    /**
     * Initialize caches for a file.
     * PERFORMANCE: Parse AST once per file and cache for reuse by multiple rules.
     */
    protected void initializeCaches(String content) {
        contentCache.set(content);
        lineOffsetCache.set(buildLineOffsetArray(content));
        linesCache.set(content.split("\n", -1));

        // PERFORMANCE: Parse AST once and cache (only if content is small enough)
        if (content.length() <= 2_000_000) {
            try {
                org.sonar.plugins.mathematica.ast.MathematicaParser parser =
                    new org.sonar.plugins.mathematica.ast.MathematicaParser();
                java.util.List<org.sonar.plugins.mathematica.ast.AstNode> ast = parser.parse(content);
                astCache.set(ast);
            } catch (Exception e) {
                LOG.debug("Failed to cache AST: {}", e.getMessage());
                astCache.set(null);
            }
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
            if (content.charAt(i) == '\n') {
                if (lineIndex < offsets.length) {
                    offsets[lineIndex++] = i + 1;
                }
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
     * Reports an issue at a specific line.
     */
    protected void reportIssue(SensorContext context, InputFile inputFile, int line, String ruleKey, String message) {
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
     */
    protected boolean isInsideStringLiteral(String content, int position) {
        int quoteCount = 0;
        int lineStart = content.lastIndexOf('\n', position) + 1;
        for (int i = lineStart; i < position; i++) {
            if (content.charAt(i) == '"') {
                if (i > 0 && content.charAt(i - 1) != '\\') {
                    quoteCount++;
                }
            }
        }
        return quoteCount % 2 == 1;
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
     * Analyzes comments and returns their ranges.
     */
    protected List<int[]> analyzeComments(String content) {
        List<int[]> commentRanges = new java.util.ArrayList<>();
        Matcher matcher = COMMENT_PATTERN.matcher(content);
        while (matcher.find()) {
            commentRanges.add(new int[]{matcher.start(), matcher.end()});
        }
        return commentRanges;
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
}
