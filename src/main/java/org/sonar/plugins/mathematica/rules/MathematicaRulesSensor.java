package org.sonar.plugins.mathematica.rules;

import java.io.IOException;
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

/**
 * Sensor that analyzes Mathematica files for code quality issues.
 *
 * Performance optimizations:
 * - All regex patterns are compiled once as static fields to avoid repeated compilation
 * - Comment ranges are cached during the first pass and reused by other detection methods
 * - Single-pass analysis for comments (both commented code and TODO/FIXME in one scan)
 * - File size limits prevent analysis of extremely large files (>25,000 lines or >1MB)
 * - Per-rule error handling ensures one failing rule doesn't stop the entire analysis
 * - StackOverflowError protection for complex regex patterns
 */
public class MathematicaRulesSensor implements Sensor {

    private static final Logger LOG = Loggers.get(MathematicaRulesSensor.class);

    // Patterns for different token types (pre-compiled for performance)
    // More robust comment pattern that avoids catastrophic backtracking
    // Matches (* followed by any characters (including newlines) then *)
    // Using [\s\S] instead of . with DOTALL for better performance
    private static final Pattern COMMENT_PATTERN = Pattern.compile("\\(\\*[\\s\\S]*?\\*\\)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\.?\\d*(?:[eE][+-]?\\d+)?\\b");
    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[([^\\]]*)\\]\\s*:=",
        Pattern.MULTILINE
    );
    private static final Pattern EMPTY_BLOCK_PATTERN = Pattern.compile(
        "(?:Module|Block|With)\\s*\\[\\s*\\{[^}]*\\}\\s*,?\\s*\\]",
        Pattern.MULTILINE
    );

    // Security patterns
    private static final Pattern HARDCODED_CREDENTIAL_PATTERN = Pattern.compile(
        "(?i)(password|passwd|pwd|secret|apikey|api_key|token|auth|credential|private_key|" +
        "access_key|secret_key|aws_access_key_id|aws_secret_access_key)\\s*=\\s*\"([^\"]{8,})\"",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile(
        "(?:Run|RunProcess|Import)\\s*\\[\\s*(?:\"[^\"]*\"\\s*<>|\\{\"sh\"|\\{\"bash\"|\"!\"\\s*<>)"
    );
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?:SQLExecute|SQLSelect|SQLInsert|SQLUpdate|SQLDelete)\\s*\\[[^,]*,\\s*\"[^\"]*\"\\s*<>"
    );
    private static final Pattern CODE_INJECTION_PATTERN = Pattern.compile(
        "(?:ToExpression|Evaluate)\\s*\\[\\s*(?!\")"
    );
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(?:Import|Export|Get|Put|OpenRead|OpenWrite|OpenAppend)\\s*\\[[^\\]]*<>[^\\]]*\\]"
    );
    private static final Pattern WEAK_CRYPTO_PATTERN = Pattern.compile(
        "(?:Hash\\s*\\[[^,]+,\\s*\"(?:MD5|SHA1|SHA-1)\")|(?:Random\\s*\\[)"
    );
    private static final Pattern SSRF_PATTERN = Pattern.compile(
        "(?:URLFetch|URLRead|URLExecute|ServiceExecute)\\s*\\[[^\\]]*<>|" +
        "Import\\s*\\[\\s*(?:\"https?://\"|\"http://\")\\s*<>"
    );
    private static final Pattern INSECURE_DESERIALIZATION_PATTERN = Pattern.compile(
        "(?:Import\\s*\\[[^,]+,\\s*\"(?:MX|WDX)\")|" +
        "(?:Get\\s*\\[(?:[^\\]]*<>|\"https?://))"
    );
    private static final Pattern SIMPLE_CHECK_PATTERN = Pattern.compile(
        "Check\\s*\\[[^,]+,\\s*(?:\\$Failed|Null|None)\\s*\\]"
    );
    private static final Pattern QUIET_PATTERN = Pattern.compile(
        "Quiet\\s*\\["
    );
    private static final Pattern DEBUG_CODE_PATTERN = Pattern.compile(
        "(?:Print|Echo|PrintTemporary|TracePrint|Trace|Monitor)\\s*\\[|" +
        "\\$DebugMessages\\s*=\\s*True"
    );

    // BUG detection patterns
    private static final Pattern DIVISION_PATTERN = Pattern.compile(
        "/(?!=)"  // Division operator, but not //= or /=
    );
    private static final Pattern ASSIGNMENT_IN_IF_PATTERN = Pattern.compile(
        "(?:If|While|Which)\\s*\\[[^\\]]*\\b(\\w+)\\s*=\\s*(?!=)[^=]"
    );
    private static final Pattern LIST_ACCESS_PATTERN = Pattern.compile(
        "\\[\\[([^\\]]+)\\]\\]"
    );
    private static final Pattern RECURSIVE_FUNCTION_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[[^\\]]*\\]\\s*:="
    );

    // Security hotspot patterns
    private static final Pattern FILE_IMPORT_PATTERN = Pattern.compile(
        "(?:Import|Get|OpenRead|OpenWrite|Put)\\s*\\["
    );
    private static final Pattern API_CALL_PATTERN = Pattern.compile(
        "(?:URLRead|URLFetch|URLExecute|URLSubmit|ServiceExecute|ServiceConnect)\\s*\\["
    );
    private static final Pattern KEY_GENERATION_PATTERN = Pattern.compile(
        "(?:RandomInteger|Random)\\s*\\[|" +
        "(?:GenerateSymmetricKey|GenerateAsymmetricKeyPair)\\s*\\[|" +
        "Table\\s*\\[[^\\]]*Random"
    );

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

        for (InputFile inputFile : inputFiles) {
            analyzeFile(context, inputFile);
        }
    }

    private void analyzeFile(SensorContext context, InputFile inputFile) {
        try {
            // Always check file length first (report violation even for large files)
            detectLongFile(context, inputFile);

            // Skip further analysis for extremely large files (performance)
            if (inputFile.lines() > 25000) {
                LOG.info("Skipping further analysis of large file (>25000 lines): {}", inputFile);
                return;
            }

            String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);

            // Skip files larger than 1MB
            if (content.length() > 1_000_000) {
                LOG.info("Skipping further analysis of large file (>1MB): {}", inputFile);
                return;
            }

            // Optimized: Single pass through comments, cache the ranges for reuse
            List<int[]> commentRanges = analyzeComments(context, inputFile, content);

            // Code smell rules
            detectMagicNumbers(context, inputFile, content, commentRanges);
            detectEmptyBlocks(context, inputFile, content);
            detectLongFunctions(context, inputFile, content);
            detectEmptyCatchBlocks(context, inputFile, content);
            detectDebugCode(context, inputFile, content);

            // Security rules
            detectHardcodedCredentials(context, inputFile, content);
            detectCommandInjection(context, inputFile, content);
            detectSqlInjection(context, inputFile, content);
            detectCodeInjection(context, inputFile, content);
            detectPathTraversal(context, inputFile, content);
            detectWeakCryptography(context, inputFile, content);
            detectSsrf(context, inputFile, content);
            detectInsecureDeserialization(context, inputFile, content);

            // BUG rules (Reliability)
            detectDivisionByZero(context, inputFile, content);
            detectAssignmentInConditional(context, inputFile, content);
            detectListIndexOutOfBounds(context, inputFile, content);
            detectInfiniteRecursion(context, inputFile, content);
            detectUnreachablePatterns(context, inputFile, content);

            // Security Hotspot rules
            detectFileUploadValidation(context, inputFile, content);
            detectExternalApiSafeguards(context, inputFile, content);
            detectCryptoKeyGeneration(context, inputFile, content);

            // Complexity metrics (per-function)
            calculateComplexityMetrics(context, inputFile, content);

        } catch (IOException e) {
            LOG.error("Error reading file: {}", inputFile, e);
        } catch (Exception e) {
            LOG.warn("Error analyzing file: {}", inputFile, e);
        }
    }

    /**
     * Single-pass analysis of all comments (faster than multiple passes).
     * Returns comment ranges for reuse in other detection methods.
     */
    private List<int[]> analyzeComments(SensorContext context, InputFile inputFile, String content) {
        List<int[]> commentRanges = new ArrayList<>();
        try {
            Matcher matcher = COMMENT_PATTERN.matcher(content);

            while (matcher.find()) {
                String comment = matcher.group();
                int lineNumber = calculateLineNumber(content, matcher.start());

                // Cache the comment range for reuse
                commentRanges.add(new int[]{matcher.start(), matcher.end()});

                // Check for commented-out code
                if (isCommentedCode(comment)) {
                    reportIssue(context, inputFile, lineNumber,
                        MathematicaRulesDefinition.COMMENTED_CODE_KEY,
                        "Remove this commented-out code.");
                }

                // Check for TODO/FIXME
                String commentUpper = comment.toUpperCase();
                if (commentUpper.contains("TODO") || commentUpper.contains("FIXME")) {
                    reportIssue(context, inputFile, lineNumber,
                        MathematicaRulesDefinition.TODO_FIXME_KEY,
                        "Track this TODO/FIXME comment.");
                }
            }
        } catch (StackOverflowError e) {
            LOG.warn("Skipping comment analysis due to regex complexity (StackOverflowError) in file: {}. " +
                    "File will still be analyzed for other rules.", inputFile.filename());
        } catch (Exception e) {
            LOG.warn("Skipping comment analysis due to error in file: {}. " +
                    "File will still be analyzed for other rules.", inputFile.filename());
        }
        return commentRanges;
    }

    /**
     * Helper to report an issue.
     */
    private void reportIssue(SensorContext context, InputFile inputFile, int lineNumber,
                             String ruleKey, String message) {
        RuleKey rule = RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY, ruleKey);
        NewIssue issue = context.newIssue().forRule(rule);
        NewIssueLocation location = issue.newLocation()
            .on(inputFile)
            .at(inputFile.selectLine(lineNumber))
            .message(message);
        issue.at(location);
        issue.save();
    }


    /**
     * Calculates the line number for a given offset in the content.
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
     * Determines if a comment contains code based on various heuristics.
     */
    private boolean isCommentedCode(String comment) {
        // Remove comment delimiters
        String inner = comment.substring(2, comment.length() - 2).trim();

        // Skip very short comments (likely just text)
        if (inner.length() < 10) {
            return false;
        }

        // Skip if it looks like a natural language sentence (contains common words)
        if (looksLikeNaturalLanguage(inner)) {
            return false;
        }

        int codeIndicators = 0;

        // Check for assignment operators
        if (inner.contains(":=") || Pattern.compile("\\w+\\s*=\\s*[^=]").matcher(inner).find()) {
            codeIndicators += 2;
        }

        // Check for function calls (identifier followed by brackets)
        if (Pattern.compile("[a-zA-Z]\\w*\\s*\\[").matcher(inner).find()) {
            codeIndicators += 2;
        }

        // Check for semicolons (statement terminators)
        if (inner.contains(";")) {
            codeIndicators += 1;
        }

        // Check for common Mathematica keywords/functions
        String[] keywords = {
            "Module", "Block", "With", "Table", "Map", "Apply", "Function",
            "If", "While", "Do", "For", "Return", "Print", "Plot", "Solve"
        };
        for (String keyword : keywords) {
            if (Pattern.compile("\\b" + keyword + "\\s*\\[").matcher(inner).find()) {
                codeIndicators += 2;
                break; // Only count once
            }
        }

        // Check for pattern matching syntax
        if (inner.contains("_") || inner.contains("__") || inner.contains("___")) {
            codeIndicators += 1;
        }

        // Check for operators
        if (Pattern.compile("[-+*/^]\\s*[a-zA-Z0-9]").matcher(inner).find()) {
            codeIndicators += 1;
        }

        // Check for arrow operators (rules and delayed assignments)
        if (inner.contains("->") || inner.contains(":>")) {
            codeIndicators += 2;
        }

        // If we have 3 or more code indicators, it's likely code
        return codeIndicators >= 3;
    }

    /**
     * Checks if the text looks like natural language rather than code.
     */
    private boolean looksLikeNaturalLanguage(String text) {
        String lowerText = text.toLowerCase();

        // Common English words that indicate natural language comments
        String[] commonWords = {
            "this is", "this function", "this will", "this should",
            "note that", "hack", "bug",
            "returns", "calculates", "computes", "sets", "gets",
            "the following", "for example", "such as"
        };

        int naturalLanguageIndicators = 0;
        for (String phrase : commonWords) {
            if (lowerText.contains(phrase)) {
                naturalLanguageIndicators++;
            }
        }

        // If we find multiple natural language phrases, it's probably documentation
        return naturalLanguageIndicators >= 2;
    }

    /**
     * Detects magic numbers in the code (optimized - uses cached comment ranges).
     */
    private void detectMagicNumbers(SensorContext context, InputFile inputFile, String content, List<int[]> commentRanges) {
        try {
            Matcher matcher = NUMBER_PATTERN.matcher(content);

            while (matcher.find()) {
                String number = matcher.group();
                int position = matcher.start();

                // Skip if inside a comment
                boolean inComment = false;
                for (int[] range : commentRanges) {
                    if (position >= range[0] && position < range[1]) {
                        inComment = true;
                        break;
                    }
                }
                if (inComment) {
                    continue;
                }

                // Skip common acceptable numbers
                if (isAcceptableNumber(number)) {
                    continue;
                }

                int lineNumber = calculateLineNumber(content, position);

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.MAGIC_NUMBER_KEY,
                    "Replace this magic number with a named constant.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping magic number detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Checks if a number is acceptable (not a magic number).
     * Only flag numbers that are likely to be meaningful constants.
     */
    private boolean isAcceptableNumber(String number) {
        // Accept 0, 1, 2 (very common)
        if (number.equals("0") || number.equals("1") || number.equals("2") ||
            number.equals("0.0") || number.equals("1.0") || number.equals("2.0")) {
            return true;
        }

        // Accept small integers 3-10 (array indices, small constants)
        try {
            double val = Double.parseDouble(number);
            if (val >= 3 && val <= 10 && !number.contains(".") && !number.contains("e") && !number.contains("E")) {
                return true;
            }
        } catch (NumberFormatException e) {
            // If parsing fails, consider it a magic number
            return false;
        }

        return false;
    }

    /**
     * Detects empty blocks in the code.
     */
    private void detectEmptyBlocks(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = EMPTY_BLOCK_PATTERN.matcher(content);

            while (matcher.find()) {
                String block = matcher.group();

                // Check if the block body is truly empty (only whitespace/commas)
                String bodyPattern = "\\{[^}]*\\}\\s*,?\\s*\\]";
                Matcher bodyMatcher = Pattern.compile(bodyPattern).matcher(block);

                if (bodyMatcher.find()) {
                    String body = bodyMatcher.group();
                    // Extract what's between the last } and ]
                    String afterBraces = body.substring(body.indexOf('}') + 1, body.lastIndexOf(']')).trim();

                    // Remove any commas
                    afterBraces = afterBraces.replace(",", "").trim();

                    // If empty, it's a violation
                    if (afterBraces.isEmpty()) {
                        int lineNumber = calculateLineNumber(content, matcher.start());

                        reportIssue(context, inputFile, lineNumber,
                            MathematicaRulesDefinition.EMPTY_BLOCK_KEY,
                            "Remove this empty block or add implementation.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping empty block detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects functions that are too long.
     */
    private void detectLongFunctions(SensorContext context, InputFile inputFile, String content) {
        try {
            // Get threshold from configuration
            int maxLines = context.config()
                .getInt("sonar.mathematica.function.maximumLines")
                .orElse(150);

            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);

            while (matcher.find()) {
                String functionName = matcher.group(1);
                int startOffset = matcher.start();
                int startLine = calculateLineNumber(content, startOffset);

                // Find the end of the function (heuristic: next function definition or end of file)
                int endOffset = content.length();
                Matcher nextFunctionMatcher = FUNCTION_DEF_PATTERN.matcher(content);

                // Find next function after this one
                while (nextFunctionMatcher.find()) {
                    if (nextFunctionMatcher.start() > matcher.end()) {
                        endOffset = nextFunctionMatcher.start();
                        break;
                    }
                }

                int endLine = calculateLineNumber(content, endOffset);
                int functionLength = endLine - startLine + 1;

                if (functionLength > maxLines) {
                    reportIssue(context, inputFile, startLine,
                        MathematicaRulesDefinition.FUNCTION_LENGTH_KEY,
                        String.format("Function '%s' has %d lines, which exceeds the maximum of %d.",
                            functionName, functionLength, maxLines));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping function length detection due to error in file: {}", inputFile.filename());
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

    // ===== SECURITY DETECTION METHODS =====

    /**
     * Detects hardcoded credentials in the code.
     */
    private void detectHardcodedCredentials(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = HARDCODED_CREDENTIAL_PATTERN.matcher(content);

            while (matcher.find()) {
                String variableName = matcher.group(1);
                String value = matcher.group(2);

                // Skip if it looks like a placeholder or example
                if (isPlaceholderValue(value)) {
                    continue;
                }

                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.HARDCODED_CREDENTIALS_KEY,
                    String.format("Remove this hard-coded credential '%s'.", variableName));
            }
        } catch (Exception e) {
            LOG.warn("Skipping hardcoded credential detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Checks if a value looks like a placeholder rather than a real credential.
     */
    private boolean isPlaceholderValue(String value) {
        String lower = value.toLowerCase();
        return lower.contains("example") ||
               lower.contains("placeholder") ||
               lower.contains("your_") ||
               lower.contains("xxx") ||
               lower.equals("password") ||
               lower.equals("secret") ||
               value.matches("^[*]+$") ||
               value.matches("^[x]+$");
    }

    /**
     * Detects potential command injection vulnerabilities.
     */
    private void detectCommandInjection(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = COMMAND_INJECTION_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.COMMAND_INJECTION_KEY,
                    "Make sure that executing this OS command is safe.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping command injection detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects potential SQL injection vulnerabilities.
     */
    private void detectSqlInjection(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SQL_INJECTION_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.SQL_INJECTION_KEY,
                    "Use parameterized queries to prevent SQL injection.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping SQL injection detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects potential code injection via ToExpression.
     */
    private void detectCodeInjection(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CODE_INJECTION_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.CODE_INJECTION_KEY,
                    "Make sure that evaluating this expression is safe.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping code injection detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects potential path traversal vulnerabilities.
     */
    private void detectPathTraversal(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PATH_TRAVERSAL_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.PATH_TRAVERSAL_KEY,
                    "Validate and sanitize this file path to prevent path traversal attacks.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping path traversal detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects weak cryptographic algorithms.
     */
    private void detectWeakCryptography(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = WEAK_CRYPTO_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                String match = matcher.group();

                String message;
                if (match.contains("MD5") || match.contains("SHA1") || match.contains("SHA-1")) {
                    message = "Use a stronger hash algorithm (SHA256, SHA512) instead of MD5/SHA1.";
                } else {
                    message = "Use RandomInteger instead of Random for security-sensitive operations.";
                }

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.WEAK_CRYPTOGRAPHY_KEY,
                    message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping weak cryptography detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects potential Server-Side Request Forgery (SSRF) vulnerabilities.
     */
    private void detectSsrf(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SSRF_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.SSRF_KEY,
                    "Validate and sanitize URLs to prevent Server-Side Request Forgery attacks.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping SSRF detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects insecure deserialization vulnerabilities.
     */
    private void detectInsecureDeserialization(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = INSECURE_DESERIALIZATION_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                String match = matcher.group();

                String message;
                if (match.contains("MX") || match.contains("WDX")) {
                    message = "Avoid importing MX/WDX files from untrusted sources. Use safe formats like JSON or CSV.";
                } else {
                    message = "Avoid loading code from untrusted sources with Get[]. Validate file paths and check integrity.";
                }

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.INSECURE_DESERIALIZATION_KEY,
                    message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping insecure deserialization detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects empty catch blocks that silently ignore exceptions.
     * Optimized to use pre-compiled static patterns.
     */
    private void detectEmptyCatchBlocks(SensorContext context, InputFile inputFile, String content) {
        try {
            // Look for Check[] with just $Failed or similar simple handlers (using static pattern)
            Matcher checkMatcher = SIMPLE_CHECK_PATTERN.matcher(content);

            while (checkMatcher.find()) {
                String match = checkMatcher.group();
                // Skip if there's actual error handling logic (like Print, Log, If, etc.)
                if (!match.contains("Print") && !match.contains("Log") &&
                    !match.contains("If") && !match.contains("Message")) {
                    int lineNumber = calculateLineNumber(content, checkMatcher.start());
                    reportIssue(context, inputFile, lineNumber,
                        MathematicaRulesDefinition.EMPTY_CATCH_KEY,
                        "Log or handle this exception instead of silently ignoring it.");
                }
            }

            // Look for Quiet[] which suppresses all messages (using static pattern)
            Matcher quietMatcher = QUIET_PATTERN.matcher(content);

            while (quietMatcher.find()) {
                int lineNumber = calculateLineNumber(content, quietMatcher.start());
                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.EMPTY_CATCH_KEY,
                    "Avoid using Quiet[] as it suppresses error messages. Use Check[] with proper error handling.");
            }

        } catch (Exception e) {
            LOG.warn("Skipping empty catch block detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects debug code left in production.
     */
    private void detectDebugCode(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DEBUG_CODE_PATTERN.matcher(content);

            while (matcher.find()) {
                String match = matcher.group();
                int lineNumber = calculateLineNumber(content, matcher.start());

                String message;
                if (match.contains("Print") || match.contains("Echo") || match.contains("PrintTemporary")) {
                    message = "Remove this debug print statement before deploying to production.";
                } else if (match.contains("Trace") || match.contains("TracePrint")) {
                    message = "Remove this trace/debug function before deploying to production.";
                } else if (match.contains("Monitor")) {
                    message = "Remove this debug monitor before deploying to production.";
                } else {
                    message = "Remove this debug configuration before deploying to production.";
                }

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.DEBUG_CODE_KEY,
                    message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping debug code detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== BUG DETECTION METHODS (Reliability) =====

    /**
     * Detects potential division by zero operations.
     */
    private void detectDivisionByZero(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DIVISION_PATTERN.matcher(content);

            while (matcher.find()) {
                int position = matcher.start();
                int lineNumber = calculateLineNumber(content, position);

                // Get context around the division to check if it's validated
                int lineStart = content.lastIndexOf('\n', position) + 1;
                int lineEnd = content.indexOf('\n', position);
                if (lineEnd == -1) lineEnd = content.length();
                String line = content.substring(lineStart, lineEnd);

                // Skip if there's obvious validation (Check, If checking != 0, etc.)
                if (line.contains("Check[") || line.contains("!= 0") || line.contains("> 0")) {
                    continue;
                }

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.DIVISION_BY_ZERO_KEY,
                    "Ensure the divisor cannot be zero before performing division.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping division by zero detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects assignment (=) used instead of comparison (==, ===) in conditionals.
     */
    private void detectAssignmentInConditional(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSIGNMENT_IN_IF_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITIONAL_KEY,
                    "Use comparison (== or ===) instead of assignment (=) in this conditional.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping assignment in conditional detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects list element access without bounds checking.
     */
    private void detectListIndexOutOfBounds(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = LIST_ACCESS_PATTERN.matcher(content);

            while (matcher.find()) {
                int position = matcher.start();
                int lineNumber = calculateLineNumber(content, position);

                // Get context to check if bounds are validated
                int lineStart = content.lastIndexOf('\n', position) + 1;
                int lineEnd = content.indexOf('\n', position);
                if (lineEnd == -1) lineEnd = content.length();
                String line = content.substring(lineStart, lineEnd);

                // Skip if there's obvious bounds checking
                if (line.contains("Length[") || line.contains("Check[") ||
                    line.contains("<= Length") || line.contains("If[Length")) {
                    continue;
                }

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.LIST_INDEX_OUT_OF_BOUNDS_KEY,
                    "Verify the index is within bounds before accessing list elements.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping list index bounds detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects recursive functions that may lack proper base cases.
     */
    private void detectInfiniteRecursion(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher defMatcher = RECURSIVE_FUNCTION_PATTERN.matcher(content);

            while (defMatcher.find()) {
                String functionName = defMatcher.group(1);
                int defStart = defMatcher.start();
                int lineNumber = calculateLineNumber(content, defStart);

                // Look for the function body (simplified: until next function or semicolon)
                int bodyEnd = content.indexOf(";", defStart);
                if (bodyEnd == -1) bodyEnd = content.length();
                int nextDef = content.indexOf(functionName + "[", defStart + functionName.length());
                if (nextDef > 0 && nextDef < bodyEnd) {
                    // Function calls itself - it's recursive
                    // Check if there's a base case defined elsewhere
                    Pattern baseCase = Pattern.compile(functionName + "\\s*\\[\\s*\\d+\\s*\\]\\s*=");
                    Matcher baseMatcher = baseCase.matcher(content);

                    boolean hasBaseCase = false;
                    while (baseMatcher.find()) {
                        if (baseMatcher.start() != defStart) {
                            hasBaseCase = true;
                            break;
                        }
                    }

                    if (!hasBaseCase) {
                        reportIssue(context, inputFile, lineNumber,
                            MathematicaRulesDefinition.INFINITE_RECURSION_KEY,
                            String.format("Function '%s' appears to be recursive but may lack a base case.", functionName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping infinite recursion detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects unreachable pattern definitions (general patterns before specific ones).
     */
    private void detectUnreachablePatterns(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find all function definitions
            Matcher matcher = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[([^\\]]+)\\]\\s*:=").matcher(content);
            java.util.Map<String, java.util.List<PatternInfo>> functionPatterns = new java.util.HashMap<>();

            while (matcher.find()) {
                String funcName = matcher.group(1);
                String pattern = matcher.group(2);
                int lineNumber = calculateLineNumber(content, matcher.start());

                if (!functionPatterns.containsKey(funcName)) {
                    functionPatterns.put(funcName, new java.util.ArrayList<>());
                }
                functionPatterns.get(funcName).add(new PatternInfo(pattern, lineNumber));
            }

            // Check each function's patterns
            for (java.util.Map.Entry<String, java.util.List<PatternInfo>> entry : functionPatterns.entrySet()) {
                java.util.List<PatternInfo> patterns = entry.getValue();
                if (patterns.size() < 2) continue;

                // Check if a general pattern (single underscore) comes before specific patterns
                for (int i = 0; i < patterns.size() - 1; i++) {
                    String currentPattern = patterns.get(i).pattern;
                    // If current pattern is very general (just x_ or similar)
                    if (currentPattern.matches("\\w+_\\s*")) {
                        // Check if there are more specific patterns after it
                        for (int j = i + 1; j < patterns.size(); j++) {
                            String laterPattern = patterns.get(j).pattern;
                            // If later pattern has type constraints, it's more specific
                            if (laterPattern.contains("_Integer") || laterPattern.contains("_String") ||
                                laterPattern.contains("_Real") || laterPattern.contains("_?") ||
                                laterPattern.contains("_Symbol")) {
                                reportIssue(context, inputFile, patterns.get(j).lineNumber,
                                    MathematicaRulesDefinition.UNREACHABLE_PATTERN_KEY,
                                    "This specific pattern will never match because a more general pattern was defined earlier.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping unreachable pattern detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Helper class for pattern tracking.
     */
    private static class PatternInfo {
        String pattern;
        int lineNumber;
        PatternInfo(String p, int ln) {
            this.pattern = p;
            this.lineNumber = ln;
        }
    }

    // ===== SECURITY HOTSPOT DETECTION METHODS =====

    /**
     * Detects file import/upload operations that should be reviewed for validation.
     */
    private void detectFileUploadValidation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FILE_IMPORT_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                String match = matcher.group();

                String message;
                if (match.contains("Import") || match.contains("Get")) {
                    message = "Review: Ensure file uploads/imports are validated for type, size, and content.";
                } else {
                    message = "Review: Ensure file operations validate and sanitize file paths.";
                }

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.FILE_UPLOAD_VALIDATION_KEY,
                    message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping file upload detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects external API calls that should be reviewed for proper safeguards.
     */
    private void detectExternalApiSafeguards(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = API_CALL_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.EXTERNAL_API_SAFEGUARDS_KEY,
                    "Review: Ensure this API call has timeout, error handling, and rate limiting.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping API safeguards detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detects cryptographic key generation that should be reviewed for security.
     */
    private void detectCryptoKeyGeneration(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = KEY_GENERATION_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                String match = matcher.group();

                String message;
                if (match.contains("Random[") && !match.contains("RandomInteger")) {
                    message = "Review: Random[] is not cryptographically secure. Use RandomInteger for keys.";
                } else {
                    message = "Review: Ensure cryptographic keys are generated with sufficient entropy and stored securely.";
                }

                reportIssue(context, inputFile, lineNumber,
                    MathematicaRulesDefinition.CRYPTO_KEY_GENERATION_KEY,
                    message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping crypto key generation detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== COMPLEXITY METRICS =====

    /**
     * Calculates cyclomatic and cognitive complexity for functions.
     * Logs complexity metrics that can be displayed in SonarQube.
     */
    private void calculateComplexityMetrics(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);

            while (matcher.find()) {
                String functionName = matcher.group(1);
                int startOffset = matcher.start();
                int startLine = calculateLineNumber(content, startOffset);

                // Find function body (simplified: until next function definition or end of file)
                int endOffset = content.length();
                Matcher nextFunctionMatcher = FUNCTION_DEF_PATTERN.matcher(content);
                while (nextFunctionMatcher.find()) {
                    if (nextFunctionMatcher.start() > matcher.end()) {
                        endOffset = nextFunctionMatcher.start();
                        break;
                    }
                }

                String functionBody = content.substring(startOffset, endOffset);

                // Calculate Cyclomatic Complexity
                int cyclomaticComplexity = calculateCyclomaticComplexity(functionBody);

                // Calculate Cognitive Complexity
                int cognitiveComplexity = calculateCognitiveComplexity(functionBody);

                // Report if complexity is high
                if (cyclomaticComplexity > 15) {
                    reportIssue(context, inputFile, startLine,
                        MathematicaRulesDefinition.FUNCTION_LENGTH_KEY,
                        String.format("Function '%s' has cyclomatic complexity of %d (max recommended: 15).",
                            functionName, cyclomaticComplexity));
                }

                if (cognitiveComplexity > 15) {
                    reportIssue(context, inputFile, startLine,
                        MathematicaRulesDefinition.FUNCTION_LENGTH_KEY,
                        String.format("Function '%s' has cognitive complexity of %d (max recommended: 15).",
                            functionName, cognitiveComplexity));
                }

                // Log metrics for SonarQube dashboard
                LOG.debug("Function '{}' at line {}: Cyclomatic={}, Cognitive={}",
                    functionName, startLine, cyclomaticComplexity, cognitiveComplexity);
            }
        } catch (Exception e) {
            LOG.warn("Skipping complexity calculation due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Calculates Cyclomatic Complexity - counts decision points.
     * Formula: CC = E - N + 2P (for connected graph)
     * Simplified: Count decision points + 1
     */
    private int calculateCyclomaticComplexity(String functionBody) {
        int complexity = 1; // Base complexity

        // Count decision points
        complexity += countOccurrences(functionBody, "\\bIf\\s*\\[");
        complexity += countOccurrences(functionBody, "\\bWhich\\s*\\[");
        complexity += countOccurrences(functionBody, "\\bSwitch\\s*\\[");
        complexity += countOccurrences(functionBody, "\\bWhile\\s*\\[");
        complexity += countOccurrences(functionBody, "\\bDo\\s*\\[");
        complexity += countOccurrences(functionBody, "\\bFor\\s*\\[");
        complexity += countOccurrences(functionBody, "\\bTable\\s*\\[");
        complexity += countOccurrences(functionBody, "&&");
        complexity += countOccurrences(functionBody, "\\|\\|");
        complexity += countOccurrences(functionBody, "\\bAnd\\s*\\[");
        complexity += countOccurrences(functionBody, "\\bOr\\s*\\[");
        complexity += countOccurrences(functionBody, "/;"); // Condition operator

        return complexity;
    }

    /**
     * Calculates Cognitive Complexity - measures how difficult code is to understand.
     * More sophisticated than cyclomatic - penalizes nesting.
     */
    private int calculateCognitiveComplexity(String functionBody) {
        int complexity = 0;
        int nestingLevel = 0;

        // This is a simplified version - a full implementation would parse the AST
        // Here we approximate by counting control structures with nesting awareness

        String[] lines = functionBody.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();

            // Increase nesting for block structures
            if (trimmed.matches(".*\\b(?:Module|Block|With|If|While|Do|For|Table)\\s*\\[.*")) {
                complexity += (1 + nestingLevel); // Add base complexity + nesting penalty
                nestingLevel++;
            }

            // Logical operators add complexity
            if (trimmed.contains("&&") || trimmed.contains("||")) {
                complexity += 1;
            }

            // Decrease nesting on closing brackets (simplified)
            int openBrackets = countOccurrences(trimmed, "\\[");
            int closeBrackets = countOccurrences(trimmed, "\\]");
            nestingLevel = Math.max(0, nestingLevel + openBrackets - closeBrackets);
        }

        return complexity;
    }

    /**
     * Helper to count pattern occurrences in a string.
     */
    private int countOccurrences(String text, String patternString) {
        try {
            Pattern pattern = Pattern.compile(patternString);
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
}
