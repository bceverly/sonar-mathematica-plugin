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
            tokenize(context, inputFile);
        }
    }

    private void tokenize(SensorContext context, InputFile inputFile) {
        try {
            String content = new String(Files.readAllBytes(inputFile.path()), StandardCharsets.UTF_8);

            NewCpdTokens cpdTokens = context.newCpdTokens().onFile(inputFile);

            MathematicaTokenizer tokenizer = new MathematicaTokenizer(content);
            tokenizer.tokenize(cpdTokens);

            cpdTokens.save();

        } catch (IOException e) {
            LOG.error("Error reading file: {}", inputFile, e);
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
        private static final Pattern COMMENT_PATTERN = Pattern.compile("\\(\\*.*?\\*\\)", Pattern.DOTALL);
        private static final Pattern STRING_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");
        private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+\\.?\\d*(?:[eE][+-]?\\d+)?");
        private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z$][a-zA-Z0-9$]*");
        private static final Pattern OPERATOR_PATTERN = Pattern.compile(
            "->|:>|:=|@@|/@|//@|/\\.|//|/;|@@@|===|=!=|>=|<=|\\+\\+|--|&&|\\|\\||!=|==|" +
            "[+\\-*/^=<>!&|~@#%;,\\[\\]{}().]"
        );

        public MathematicaTokenizer(String content) {
            this.content = content;
            this.line = 1;
            this.column = 1;
            this.position = 0;
        }

        public void tokenize(NewCpdTokens cpdTokens) {
            while (position < content.length()) {
                char ch = content.charAt(position);

                // Skip whitespace but track line/column
                if (Character.isWhitespace(ch)) {
                    if (ch == '\n') {
                        line++;
                        column = 1;
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
                            column = 1;
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
                    cpdTokens.addToken(line, column, line, column + str.length(), "\"STRING\"");
                    column += str.length();
                    position += str.length();
                    continue;
                }

                // Try to match number
                Matcher numberMatcher = NUMBER_PATTERN.matcher(content.substring(position));
                if (numberMatcher.lookingAt()) {
                    String num = numberMatcher.group();
                    // Add normalized token (all numbers treated as equivalent for CPD)
                    cpdTokens.addToken(line, column, line, column + num.length(), "NUMBER");
                    column += num.length();
                    position += num.length();
                    continue;
                }

                // Try to match identifier (function names, variables, etc.)
                Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(content.substring(position));
                if (identifierMatcher.lookingAt()) {
                    String identifier = identifierMatcher.group();
                    // Keep actual identifier for CPD (so 'Module' differs from 'Block')
                    cpdTokens.addToken(line, column, line, column + identifier.length(), identifier);
                    column += identifier.length();
                    position += identifier.length();
                    continue;
                }

                // Try to match operator
                Matcher operatorMatcher = OPERATOR_PATTERN.matcher(content.substring(position));
                if (operatorMatcher.lookingAt()) {
                    String operator = operatorMatcher.group();
                    // Keep actual operator
                    cpdTokens.addToken(line, column, line, column + operator.length(), operator);
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
