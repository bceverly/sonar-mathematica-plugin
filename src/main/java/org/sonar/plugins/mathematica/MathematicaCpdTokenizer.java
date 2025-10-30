package org.sonar.plugins.mathematica;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Tokenizer for Mathematica code to enable Copy-Paste Detection (CPD).
 * This sensor breaks down Mathematica source files into tokens that can be
 * compared to find duplicated code blocks.
 */
public class MathematicaCpdTokenizer implements Sensor {

    private static final Logger LOG = Loggers.get(MathematicaCpdTokenizer.class);

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("Mathematica CPD Tokenizer")
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
            LOG.debug("Tokenizing file: {}", inputFile);
            try {
                tokenize(context, inputFile);
            } catch (Throwable t) {
                // Check if this is a fatal error (StackOverflowError, OutOfMemoryError, etc.)
                if (t instanceof Error) {
                    Error fatalError = (Error) t;
                    LOG.error("========================================");
                    LOG.error("FATAL ERROR in CPD Tokenizer while analyzing file: {}", inputFile.filename());
                    LOG.error("Full file path: {}", inputFile.path().toAbsolutePath());
                    LOG.error("File URI: {}", inputFile.uri());
                    LOG.error("File size: {} lines", inputFile.lines());
                    LOG.error("Error type: {}", fatalError.getClass().getName());
                    LOG.error("========================================");
                    // Re-throw fatal errors to crash the scanner
                    throw fatalError;
                }
                // Non-fatal exceptions already logged in tokenize()
            }
        }
    }

    private void tokenize(SensorContext context, InputFile inputFile) {
        try {
            // NOTE: Incremental analysis removed. SonarQube's file status doesn't detect
            // plugin changes, causing CPD to skip files even when tokenization logic changes.
            // This caused duplication detection to silently fail after plugin updates.

            String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);

            NewCpdTokens cpdTokens = context.newCpdTokens().onFile(inputFile);

            MathematicaTokenizer tokenizer = new MathematicaTokenizer(content);
            tokenizer.tokenize(cpdTokens);

            cpdTokens.save();

        } catch (IOException e) {
            LOG.error("Error reading file: {}", inputFile, e);
        } catch (Exception e) {
            // Catch any other tokenization errors and skip the file
            LOG.warn("Skipping file due to tokenization error: {}", inputFile, e);
        }
    }

    /**
     * Internal tokenizer that understands basic Mathematica syntax.
     */
    private static class MathematicaTokenizer {
        private final String content;
        private int line;
        private int column;
        private int position;

        // Patterns for different token types
        // More robust comment pattern that avoids catastrophic backtracking
        private static final Pattern COMMENT_PATTERN = Pattern.compile("\\(\\*[\\s\\S]*?\\*\\)");
        // PERFORMANCE FIX: Possessive quantifier (*+) prevents catastrophic backtracking on long strings
        private static final Pattern STRING_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*+\"");
        private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+\\.?\\d*(?:[eE][+-]?\\d+)?");
        private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z$][a-zA-Z0-9$]*");
        private static final Pattern OPERATOR_PATTERN = Pattern.compile(
            "->|:>|:=|@@|/@|//@|/\\.|//|/;|@@@|===|=!=|>=|<=|\\+\\+|--|&&|\\|\\||!=|==|"             + "[+\\-*/^=<>!&|~@#%;,\\[\\]{}().]"
        );

        MathematicaTokenizer(String content) {
            this.content = content;
            this.line = 1;
            this.column = 0;  // SonarQube uses 0-based column indexing
            this.position = 0;
        }

        /**
         * Safely add a token, catching any errors if column tracking gets out of sync.
         * This prevents the entire scan from failing on edge cases in Mathematica syntax.
         */
        private void safeAddToken(NewCpdTokens cpdTokens, int startLine, int startColumn, int endLine, int endColumn, String value) {
            try {
                cpdTokens.addToken(startLine, startColumn, endLine, endColumn, value);
            } catch (IllegalArgumentException e) {
                // Log but don't fail - column tracking got out of sync, skip this token
                LOG.debug("Skipping token due to position error at line {}, column {}: {}", startLine, startColumn, e.getMessage());
            }
        }

        public void tokenize(NewCpdTokens cpdTokens) {
            while (position < content.length()) {
                char ch = content.charAt(position);

                // Skip whitespace but track line/column
                if (Character.isWhitespace(ch)) {
                    if (ch == '\n') {
                        line++;
                        column = 0;  // Reset to 0 for new line
                    } else {
                        column++;
                    }
                    position++;
                    continue;
                }

                // Try to match comment
                Matcher commentMatcher = COMMENT_PATTERN.matcher(content.substring(position));
                if (commentMatcher.lookingAt()) {
                    String comment = commentMatcher.group();
                    // Count newlines in comment
                    for (char c : comment.toCharArray()) {
                        if (c == '\n') {
                            line++;
                            column = 0;  // Reset to 0 for new line (0-based indexing)
                        } else {
                            column++;
                        }
                    }
                    position += comment.length();
                    continue;
                }

                // Try to match string literal
                Matcher stringMatcher = STRING_PATTERN.matcher(content.substring(position));
                if (stringMatcher.lookingAt()) {
                    String str = stringMatcher.group();
                    // Add normalized token (all strings treated as equivalent for CPD)
                    int endColumn = column + str.length();  // End column is exclusive (one past the last char)
                    safeAddToken(cpdTokens, line, column, line, endColumn, "\"STRING\"");
                    column += str.length();
                    position += str.length();
                    continue;
                }

                // Try to match number
                Matcher numberMatcher = NUMBER_PATTERN.matcher(content.substring(position));
                if (numberMatcher.lookingAt()) {
                    String num = numberMatcher.group();
                    // Add normalized token (all numbers treated as equivalent for CPD)
                    int endColumn = column + num.length();  // End column is exclusive (one past the last char)
                    safeAddToken(cpdTokens, line, column, line, endColumn, "NUMBER");
                    column += num.length();
                    position += num.length();
                    continue;
                }

                // Try to match identifier (function names, variables, etc.)
                Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(content.substring(position));
                if (identifierMatcher.lookingAt()) {
                    String identifier = identifierMatcher.group();
                    // Keep actual identifier for CPD (so 'Module' differs from 'Block')
                    int endColumn = column + identifier.length();  // End column is exclusive (one past the last char)
                    safeAddToken(cpdTokens, line, column, line, endColumn, identifier);
                    column += identifier.length();
                    position += identifier.length();
                    continue;
                }

                // Try to match operator
                Matcher operatorMatcher = OPERATOR_PATTERN.matcher(content.substring(position));
                if (operatorMatcher.lookingAt()) {
                    String operator = operatorMatcher.group();
                    // Keep actual operator
                    int endColumn = column + operator.length();  // End column is exclusive (one past the last char)
                    safeAddToken(cpdTokens, line, column, line, endColumn, operator);
                    column += operator.length();
                    position += operator.length();
                    continue;
                }

                // Unknown character - skip it
                LOG.debug("Unknown character at line {}, column {}: {}", line, column, ch);
                column++;
                position++;
            }
        }
    }
}
