package org.sonar.plugins.mathematica.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * Detector for Code Smell 2 rules (70 rules total).
 * Handles Style & Formatting, Naming, Complexity, Maintainability, and Best Practices.
 */
public class StyleAndConventionsDetector extends BaseDetector {

    // ===== STYLE & FORMATTING PATTERNS (15 rules) =====

    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("[ \\t]+$", Pattern.MULTILINE);
    private static final Pattern OPERATOR_NO_SPACE_PATTERN = Pattern.compile("(?<![=<>!])([+\\-*/%=])(?![=])");
    private static final Pattern COMMA_NO_SPACE_PATTERN = Pattern.compile(",(?!\\s)");
    private static final Pattern BRACKET_SPACE_PATTERN = Pattern.compile("\\w\\s+\\[");
    private static final Pattern MULTIPLE_SEMICOLON_PATTERN = Pattern.compile(";;+");
    private static final Pattern EXCESSIVE_PARENS_PATTERN = Pattern.compile("\\(\\(\\([^)]+\\)\\)\\)");
    private static final Pattern BRACE_PATTERN = Pattern.compile("\\{[^}]*\\}");

    // ===== NAMING PATTERNS (15 rules) =====

    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*\\[[^\\]]*\\]\\s*:=");
    private static final Pattern VARIABLE_ASSIGN_PATTERN = Pattern.compile("([a-z][a-zA-Z0-9]*)\\s*=(?!=)");
    private static final Pattern BOOLEAN_VAR_PATTERN = Pattern.compile("([a-z][a-zA-Z0-9]*)\\s*=\\s*(?:True|False)");
    private static final Pattern CONSTANT_PATTERN = Pattern.compile("([A-Z][A-Z0-9_]*)\\s*=");
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("BeginPackage\\s*\\[\\s*\"([^\"]+)\"");
    private static final Pattern BUILTIN_NAMES = Pattern.compile("\\b([CDEINOPS])\\b\\s*=");
    private static final Pattern HUNGARIAN_PATTERN = Pattern.compile("\\b(?:str|int|num|lst|arr|tbl|obj|fn|func)[A-Z][a-zA-Z0-9]*");
    private static final Pattern NUMBER_IN_NAME_PATTERN = Pattern.compile("\\b[a-zA-Z]+\\d+[a-zA-Z]*\\b");
    private static final Pattern GENERIC_NAMES = Pattern.compile("\\b(?:data|temp|result|value|item|element|obj|var|info|stuff)\\b\\s*=");
    private static final Pattern NEGATED_BOOL_PATTERN = Pattern.compile("\\b(?:not|isNot|notIs|isntNot)[A-Z][a-zA-Z]*");

    // ===== COMPLEXITY PATTERNS (10 rules) =====

    private static final Pattern FUNCTION_PARAMS_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*\\[([^\\]]+)\\]\\s*:=");
    private static final Pattern MODULE_PATTERN = Pattern.compile("Module\\s*\\[\\s*\\{([^}]+)\\}");
    private static final Pattern BLOCK_PATTERN = Pattern.compile("Block\\s*\\[\\s*\\{([^}]+)\\}");
    private static final Pattern RETURN_PATTERN = Pattern.compile("\\bReturn\\s*\\[");
    private static final Pattern SWITCH_PATTERN = Pattern.compile("Switch\\s*\\[");
    private static final Pattern BOOLEAN_OP_PATTERN = Pattern.compile("&&|\\|\\|");
    private static final Pattern CHAIN_OP_PATTERN = Pattern.compile("//");

    // ===== MAINTAINABILITY PATTERNS (15 rules) =====

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)+\"");
    private static final Pattern PATH_PATTERN = Pattern.compile("\"(?:/[^/\"]+)+/?\"");
    private static final Pattern URL_PATTERN = Pattern.compile("\"https?://[^\"]+\"");
    private static final Pattern IF_PATTERN = Pattern.compile("If\\s*\\[");
    private static final Pattern GLOBAL_ASSIGN_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*=(?!=)");
    private static final Pattern ASSIGNMENT_IN_EXPR_PATTERN = Pattern.compile("[^:]=(?!=)");
    private static final Pattern PATTERN_MATCH_PATTERN = Pattern.compile("Match\\s*\\[|Cases\\s*\\[|Switch\\s*\\[");
    private static final Pattern OPTION_PATTERN = Pattern.compile("OptionValue\\s*\\[|Options\\s*\\[");
    private static final Pattern UNCLEAR_OPTION_PATTERN = Pattern.compile("\\b(?:flag|mode|setting|config|param)\\b\\s*->");

    // ===== BEST PRACTICES PATTERNS (15 rules) =====

    private static final Pattern STRING_CONCAT_PATTERN = Pattern.compile("<>");
    private static final Pattern LOOP_PATTERN = Pattern.compile("(?:Do|While|For)\\s*\\[");
    private static final Pattern BOOL_COMPARE_PATTERN = Pattern.compile("==\\s*(?:True|False)|(?:True|False)\\s*==");
    private static final Pattern NEGATED_COMPARE_PATTERN = Pattern.compile("!\\s*\\(.*==.*\\)");
    private static final Pattern REDUNDANT_IF_PATTERN = Pattern.compile("If\\s*\\[[^,]+,\\s*True\\s*,\\s*False\\s*\\]");
    private static final Pattern CATCH_PATTERN = Pattern.compile("Catch\\s*\\[");
    private static final Pattern THROW_PATTERN = Pattern.compile("Throw\\s*\\[");
    private static final Pattern MEMBER_Q_PATTERN = Pattern.compile("MemberQ\\s*\\[");
    private static final Pattern POSITION_PATTERN = Pattern.compile("Position\\s*\\[.*,.*,.*\\]");
    private static final Pattern REAL_EQUALITY_PATTERN = Pattern.compile("==.*\\.\\d+|\\d+\\..*==");
    private static final Pattern GRAPHICS_PATTERN = Pattern.compile("Graphics\\s*\\[");
    private static final Pattern PLOT_PATTERN = Pattern.compile("(?:Plot|ListPlot|Plot3D)\\s*\\[");
    private static final Pattern DATASET_PATTERN = Pattern.compile("Dataset\\s*\\[");
    private static final Pattern ASSOCIATION_PATTERN = Pattern.compile("Association\\s*\\[|<\\|");
    private static final Pattern PATTERN_TEST_PATTERN = Pattern.compile("/;");
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\?");

    // ===== DEPRECATED FUNCTIONS MAP =====

    private static final Set<String> DEPRECATED_OPTIONS = new HashSet<>();
    static {
        DEPRECATED_OPTIONS.add("PlotRange");
        DEPRECATED_OPTIONS.add("AspectRatio");
        DEPRECATED_OPTIONS.add("DisplayFunction");
    }

    // ===== STYLE & FORMATTING DETECTION METHODS (15 methods) =====

    /**
     * Detect lines longer than 150 characters.
     */
    public void detectLineTooLong(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.length() > 150) {
                    reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.LINE_TOO_LONG_KEY,
                        String.format("Line is %d characters long (max 150). Break into multiple lines.", line.length()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping line length detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect inconsistent indentation (mixed tabs and spaces).
     */
    public void detectInconsistentIndentation(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            boolean hasTabIndent = false;
            boolean hasSpaceIndent = false;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("\t")) {
                    hasTabIndent = true;
                } else if (line.startsWith(" ")) {
                    hasSpaceIndent = true;
                }

                if (hasTabIndent && hasSpaceIndent) {
                    reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.INCONSISTENT_INDENTATION_KEY,
                        "File has mixed tabs and spaces for indentation. Use consistent indentation.");
                    return;
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping indentation detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect trailing whitespace at end of lines.
     */
    public void detectTrailingWhitespace(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = TRAILING_WHITESPACE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TRAILING_WHITESPACE_KEY,
                    "Line has trailing whitespace. Remove trailing spaces/tabs.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping trailing whitespace detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect more than 2 consecutive blank lines.
     */
    public void detectMultipleBlankLines(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            int blankCount = 0;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().isEmpty()) {
                    blankCount++;
                    if (blankCount > 2) {
                        reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.MULTIPLE_BLANK_LINES_KEY,
                            "More than 2 consecutive blank lines. Reduce to maximum 2 blank lines.");
                        blankCount = 0; // Reset to avoid multiple reports
                    }
                } else {
                    blankCount = 0;
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping multiple blank lines detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect functions without a blank line after their definition.
     */
    public void detectMissingBlankLineAfterFunction(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());

                // Check if next line is blank
                if (lineNumber < lines.length) {
                    String nextLine = lines[lineNumber].trim();
                    if (!nextLine.isEmpty() && !nextLine.startsWith("(*")) {
                        // Check if it's another function definition
                        if (nextLine.matches("^[A-Z][a-zA-Z0-9]*\\s*\\[.*")) {
                            reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.MISSING_BLANK_LINE_AFTER_FUNCTION_KEY,
                                "Missing blank line after function definition. Add blank line for readability.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping blank line after function detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect operators without proper spacing like "x=5" instead of "x = 5".
     */
    public void detectOperatorSpacing(SensorContext context, InputFile inputFile, String content) {
        try {
            // Remove comments and strings to avoid false positives
            List<int[]> commentRanges = analyzeComments(content);

            Matcher matcher = OPERATOR_NO_SPACE_PATTERN.matcher(content);
            while (matcher.find()) {
                int pos = matcher.start();

                // Skip if in comment or string
                if (isInsideComment(pos, commentRanges) || isInsideStringLiteral(content, pos)) {
                    continue;
                }

                // Check if there's no space around operator
                boolean noSpaceBefore = pos > 0 && !Character.isWhitespace(content.charAt(pos - 1));
                boolean noSpaceAfter = pos < content.length() - 1 && !Character.isWhitespace(content.charAt(pos + 1));

                if (noSpaceBefore || noSpaceAfter) {
                    int lineNumber = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.OPERATOR_SPACING_KEY,
                        "Operator should have space on both sides for readability.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping operator spacing detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect commas without space after like "f[a,b]" instead of "f[a, b]".
     */
    public void detectCommaSpacing(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = COMMA_NO_SPACE_PATTERN.matcher(content);
            while (matcher.find()) {
                int pos = matcher.start();

                // Skip if in string
                if (isInsideStringLiteral(content, pos)) {
                    continue;
                }

                int lineNumber = calculateLineNumber(content, pos);
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.COMMA_SPACING_KEY,
                    "Comma should be followed by a space. Use 'f[a, b]' instead of 'f[a,b]'.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping comma spacing detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect space before opening bracket like "f [x]" instead of "f[x]".
     */
    public void detectBracketSpacing(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = BRACKET_SPACE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.BRACKET_SPACING_KEY,
                    "No space before opening bracket. Use 'f[x]' not 'f [x]'.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping bracket spacing detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect multiple semicolons like ";;".
     */
    public void detectSemicolonStyle(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MULTIPLE_SEMICOLON_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.SEMICOLON_STYLE_KEY,
                    "Multiple consecutive semicolons detected. Use single semicolon or separate statements.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping semicolon style detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect file that doesn't end with a newline.
     */
    public void detectFileEndsWithoutNewline(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.isEmpty() && !content.endsWith("\n")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.FILE_ENDS_WITHOUT_NEWLINE_KEY,
                    "File should end with a newline character.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping file ending detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect inconsistent alignment in lists.
     */
    public void detectAlignmentInconsistent(SensorContext context, InputFile inputFile, String content) {
        try {
            Pattern listPattern = Pattern.compile("\\{[^}]{50,}\\}");
            Matcher matcher = listPattern.matcher(content);

            while (matcher.find()) {
                String listContent = matcher.group();
                String[] parts = listContent.split(",");

                if (parts.length > 3) {
                    // Check if some elements are aligned and some are not
                    int withNewline = 0;
                    int withoutNewline = 0;

                    for (String part : parts) {
                        if (part.contains("\n")) {
                            withNewline++;
                        } else {
                            withoutNewline++;
                        }
                    }

                    if (withNewline > 0 && withoutNewline > 0) {
                        int lineNumber = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.ALIGNMENT_INCONSISTENT_KEY,
                            "List has inconsistent alignment. Align all elements or put on same line.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping alignment detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect excessive unnecessary parentheses.
     */
    public void detectParenthesesUnnecessary(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = EXCESSIVE_PARENS_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PARENTHESES_UNNECESSARY_KEY,
                    "Excessive parentheses detected. Remove unnecessary parentheses for clarity.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping unnecessary parentheses detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect inconsistent brace style placement.
     */
    public void detectBraceStyle(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = BRACE_PATTERN.matcher(content);
            int sameLine = 0;
            int newLine = 0;

            while (matcher.find()) {
                String match = matcher.group();
                int pos = matcher.start();

                // Check if opening brace is on same line as previous content
                int prevNewline = content.lastIndexOf('\n', pos);
                if (prevNewline >= 0) {
                    String textBefore = content.substring(prevNewline + 1, pos).trim();
                    if (!textBefore.isEmpty()) {
                        sameLine++;
                    } else {
                        newLine++;
                    }
                }
            }

            if (sameLine > 2 && newLine > 2) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.BRACE_STYLE_KEY,
                    "Inconsistent brace style. Place opening braces consistently (same line or new line).");
            }
        } catch (Exception e) {
            LOG.warn("Skipping brace style detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect string literals longer than 100 characters.
     */
    public void detectLongStringLiteral(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = STRING_LITERAL_PATTERN.matcher(content);
            while (matcher.find()) {
                String literal = matcher.group();
                if (literal.length() > 102) { // Account for quotes
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.LONG_STRING_LITERAL_KEY,
                        String.format("String literal is %d characters (max 100). Break into multiple parts.", literal.length() - 2));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping long string literal detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect excessive nested brackets (more than 5 levels).
     */
    public void detectNestedBracketsExcessive(SensorContext context, InputFile inputFile, String content) {
        try {
            int maxDepth = 0;
            int currentDepth = 0;
            int maxDepthPos = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '[') {
                    currentDepth++;
                    if (currentDepth > maxDepth) {
                        maxDepth = currentDepth;
                        maxDepthPos = i;
                    }
                } else if (c == ']') {
                    currentDepth--;
                }
            }

            if (maxDepth > 5) {
                int lineNumber = calculateLineNumber(content, maxDepthPos);
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NESTED_BRACKETS_EXCESSIVE_KEY,
                    String.format("Bracket nesting depth is %d (max 5). Extract to intermediate variables.", maxDepth));
            }
        } catch (Exception e) {
            LOG.warn("Skipping nested brackets detection: {}", inputFile.filename());
        }
    }

    // ===== NAMING DETECTION METHODS (15 methods) =====

    /**
     * Detect function names that are too short (less than 3 characters, except f, g, h).
     */
    public void detectFunctionNameTooShort(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);
                if (functionName.length() < 3 && !functionName.matches("[fgh]")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.FUNCTION_NAME_TOO_SHORT_KEY,
                        String.format("Function name '%s' is too short (min 3 chars). Use descriptive names.", functionName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping function name too short detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect function names that are too long (more than 50 characters).
     */
    public void detectFunctionNameTooLong(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);
                if (functionName.length() > 50) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.FUNCTION_NAME_TOO_LONG_KEY,
                        String.format("Function name '%s' is %d chars (max 50). Use shorter, clearer name.",
                            functionName, functionName.length()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping function name too long detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect variable names that are too short (single letter except i, j, k).
     */
    public void detectVariableNameTooShort(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = VARIABLE_ASSIGN_PATTERN.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);
                if (varName.length() == 1 && !varName.matches("[ijk]")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.VARIABLE_NAME_TOO_SHORT_KEY,
                        String.format("Variable '%s' is single letter (except i,j,k). Use descriptive names.", varName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping variable name too short detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect boolean variables not starting with is/has/can.
     */
    public void detectBooleanNameNonDescriptive(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = BOOLEAN_VAR_PATTERN.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);
                if (!varName.matches("^(is|has|can|should|will)[A-Z].*")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.BOOLEAN_NAME_NON_DESCRIPTIVE_KEY,
                        String.format("Boolean '%s' should start with is/has/can/should/will.", varName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping boolean name detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect constants not in UPPER_CASE.
     */
    public void detectConstantNotUppercase(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CONSTANT_PATTERN.matcher(content);
            while (matcher.find()) {
                String constName = matcher.group(1);
                if (!constName.equals(constName.toUpperCase()) || constName.contains("_")) {
                    // Allow some exceptions for Mathematica symbols
                    if (!constName.matches("^[A-Z][a-zA-Z0-9]*$")) {
                        int lineNumber = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CONSTANT_NOT_UPPERCASE_KEY,
                            String.format("Constant '%s' should be UPPER_CASE.", constName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping constant uppercase detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect package names not in CamelCase.
     */
    public void detectPackageNameCase(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PACKAGE_NAME_PATTERN.matcher(content);
            while (matcher.find()) {
                String packageName = matcher.group(1);
                String[] parts = packageName.split("`");
                for (String part : parts) {
                    if (!part.isEmpty() && !part.matches("^[A-Z][a-zA-Z0-9]*$")) {
                        int lineNumber = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PACKAGE_NAME_CASE_KEY,
                            String.format("Package name '%s' should be CamelCase.", part));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping package name case detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect inconsistent acronym casing.
     */
    public void detectAcronymStyle(SensorContext context, InputFile inputFile, String content) {
        try {
            Pattern acronymPattern = Pattern.compile("\\b([A-Z]{2,}[a-z]+|[a-z]+[A-Z]{2,})\\b");
            Matcher matcher = acronymPattern.matcher(content);

            while (matcher.find()) {
                String word = matcher.group(1);
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.ACRONYM_STYLE_KEY,
                    String.format("Inconsistent acronym style in '%s'. Use 'XmlParser' or 'XMLParser' consistently.", word));
            }
        } catch (Exception e) {
            LOG.warn("Skipping acronym style detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect variables named like built-in symbols (C, D, E, I, N, O).
     */
    public void detectVariableNameMatchesBuiltin(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = BUILTIN_NAMES.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.VARIABLE_NAME_MATCHES_BUILTIN_KEY,
                    String.format("Variable '%s' shadows built-in symbol. Rename to avoid conflicts.", varName));
            }
        } catch (Exception e) {
            LOG.warn("Skipping builtin name detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect parameters with same name as function.
     */
    public void detectParameterNameSameAsFunction(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_PARAMS_PATTERN.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);
                String params = matcher.group(2);

                if (params.toLowerCase().contains(functionName.toLowerCase())) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PARAMETER_NAME_SAME_AS_FUNCTION_KEY,
                        "Parameter name matches function name. Use different names to avoid confusion.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping parameter name detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect mixing of camelCase and snake_case.
     */
    public void detectInconsistentNamingStyle(SensorContext context, InputFile inputFile, String content) {
        try {
            boolean hasCamelCase = content.matches(".*\\b[a-z]+[A-Z][a-zA-Z]*\\b.*");
            boolean hasSnakeCase = content.matches(".*\\b[a-z]+_[a-z]+\\b.*");

            if (hasCamelCase && hasSnakeCase) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.INCONSISTENT_NAMING_STYLE_KEY,
                    "File mixes camelCase and snake_case. Use consistent naming convention.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping naming style detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect numbers in names like var1, func2.
     */
    public void detectNumberInName(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NUMBER_IN_NAME_PATTERN.matcher(content);
            while (matcher.find()) {
                String name = matcher.group();
                // Allow common patterns like x1, y2 for coordinates
                if (!name.matches("^[xyz]\\d$")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NUMBER_IN_NAME_KEY,
                        String.format("Name '%s' contains number. Use descriptive names instead of numbering.", name));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping number in name detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect Hungarian notation (strName, intCount, etc.).
     */
    public void detectHungarianNotation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = HUNGARIAN_PATTERN.matcher(content);
            while (matcher.find()) {
                String name = matcher.group();
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.HUNGARIAN_NOTATION_KEY,
                    String.format("Hungarian notation detected: '%s'. Use type-agnostic names.", name));
            }
        } catch (Exception e) {
            LOG.warn("Skipping Hungarian notation detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect unclear abbreviations.
     */
    public void detectAbbreviationUnclear(SensorContext context, InputFile inputFile, String content) {
        try {
            Pattern abbrevPattern = Pattern.compile("\\b([a-z]{1,2}[A-Z][a-z]*|[a-z]*[bcdfghjklmnpqrstvwxz]{4,})\\b");
            Matcher matcher = abbrevPattern.matcher(content);

            Set<String> reported = new HashSet<>();
            while (matcher.find()) {
                String abbrev = matcher.group();
                if (!reported.contains(abbrev) && abbrev.length() < 5) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.ABBREVIATION_UNCLEAR_KEY,
                        String.format("Unclear abbreviation '%s'. Spell out for clarity.", abbrev));
                    reported.add(abbrev);
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping abbreviation detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect generic names like data, temp, result.
     */
    public void detectGenericName(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GENERIC_NAMES.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.GENERIC_NAME_KEY,
                    "Generic variable name. Use specific, descriptive names.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping generic name detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect negated boolean names like notValid, isNotEnabled.
     */
    public void detectNegatedBooleanName(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NEGATED_BOOL_PATTERN.matcher(content);
            while (matcher.find()) {
                String name = matcher.group();
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NEGATED_BOOLEAN_NAME_KEY,
                    String.format("Negated boolean name '%s'. Use positive names and negate in logic.", name));
            }
        } catch (Exception e) {
            LOG.warn("Skipping negated boolean name detection: {}", inputFile.filename());
        }
    }

    // ===== COMPLEXITY DETECTION METHODS (10 methods) =====

    /**
     * Detect functions with too many parameters (more than 7).
     */
    public void detectTooManyParameters(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_PARAMS_PATTERN.matcher(content);
            while (matcher.find()) {
                String params = matcher.group(2);
                int paramCount = params.split(",").length;

                if (paramCount > 7) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TOO_MANY_PARAMETERS_KEY,
                        String.format("Function has %d parameters (max 7). Use options or parameter object.", paramCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping too many parameters detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect too many variables in Module/Block (more than 15).
     */
    public void detectTooManyVariables(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher moduleMatcher = MODULE_PATTERN.matcher(content);
            while (moduleMatcher.find()) {
                String vars = moduleMatcher.group(1);
                int varCount = vars.split(",").length;

                if (varCount > 15) {
                    int lineNumber = calculateLineNumber(content, moduleMatcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TOO_MANY_VARIABLES_KEY,
                        String.format("Module has %d local variables (max 15). Break into smaller functions.", varCount));
                }
            }

            Matcher blockMatcher = BLOCK_PATTERN.matcher(content);
            while (blockMatcher.find()) {
                String vars = blockMatcher.group(1);
                int varCount = vars.split(",").length;

                if (varCount > 15) {
                    int lineNumber = calculateLineNumber(content, blockMatcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TOO_MANY_VARIABLES_KEY,
                        String.format("Block has %d local variables (max 15). Break into smaller functions.", varCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping too many variables detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect too many return points (more than 5 Return statements).
     */
    public void detectTooManyReturnPoints(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher functionMatcher = FUNCTION_DEF_PATTERN.matcher(content);

            while (functionMatcher.find()) {
                int functionStart = functionMatcher.start();
                int functionEnd = functionStart + 1000; // Check next 1000 chars
                if (functionEnd > content.length()) {
                    functionEnd = content.length();
                }

                String functionBody = content.substring(functionStart, functionEnd);
                Matcher returnMatcher = RETURN_PATTERN.matcher(functionBody);

                int returnCount = 0;
                while (returnMatcher.find()) {
                    returnCount++;
                }

                if (returnCount > 5) {
                    int lineNumber = calculateLineNumber(content, functionStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TOO_MANY_RETURN_POINTS_KEY,
                        String.format("Function has %d return points (max 5). Simplify control flow.", returnCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping too many return points detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect nesting too deep (more than 4 levels).
     */
    public void detectNestingTooDeep(SensorContext context, InputFile inputFile, String content) {
        try {
            int maxDepth = 0;
            int currentDepth = 0;
            int maxDepthPos = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{' || c == '[') {
                    currentDepth++;
                    if (currentDepth > maxDepth) {
                        maxDepth = currentDepth;
                        maxDepthPos = i;
                    }
                } else if (c == '}' || c == ']') {
                    currentDepth--;
                }
            }

            if (maxDepth > 4) {
                int lineNumber = calculateLineNumber(content, maxDepthPos);
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NESTING_TOO_DEEP_KEY,
                    String.format("Nesting depth is %d (max 4). Extract to helper functions.", maxDepth));
            }
        } catch (Exception e) {
            LOG.warn("Skipping nesting depth detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect files with too many function definitions (more than 50).
     */
    public void detectFileTooManyFunctions(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            int functionCount = 0;
            while (matcher.find()) {
                functionCount++;
            }

            if (functionCount > 50) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.FILE_TOO_MANY_FUNCTIONS_KEY,
                    String.format("File has %d functions (max 50). Split into multiple files.", functionCount));
            }
        } catch (Exception e) {
            LOG.warn("Skipping file too many functions detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect packages exporting too many symbols (more than 30).
     */
    public void detectPackageTooManyExports(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.contains("BeginPackage[")) {
                return;
            }

            // Count public functions (capital letter start)
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            Set<String> publicFunctions = new HashSet<>();

            while (matcher.find()) {
                publicFunctions.add(matcher.group(1));
            }

            if (publicFunctions.size() > 30) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.PACKAGE_TOO_MANY_EXPORTS_KEY,
                    String.format("Package exports %d symbols (max 30). Keep package focused.", publicFunctions.size()));
            }
        } catch (Exception e) {
            LOG.warn("Skipping package exports detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect expressions with too many operators (more than 20).
     */
    public void detectExpressionTooComplex(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int operatorCount = countOccurrences(line, "[+\\-*/%<>=&|!]+");

                if (operatorCount > 20) {
                    reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY,
                        String.format("Expression has %d operators (max 20). Break into smaller parts.", operatorCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping expression complexity detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect Switch with too many cases (more than 15).
     */
    public void detectSwitchTooManyCases(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SWITCH_PATTERN.matcher(content);
            while (matcher.find()) {
                int switchStart = matcher.start();
                int switchEnd = switchStart + 2000; // Look ahead
                if (switchEnd > content.length()) {
                    switchEnd = content.length();
                }

                String switchBody = content.substring(switchStart, switchEnd);
                int caseCount = switchBody.split(",").length - 1; // Rough estimate

                if (caseCount > 15) {
                    int lineNumber = calculateLineNumber(content, switchStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.SWITCH_TOO_MANY_CASES_KEY,
                        String.format("Switch has ~%d cases (max 15). Use dispatch table or pattern matching.", caseCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping switch cases detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect boolean expressions with too many operators (more than 5 && or ||).
     */
    public void detectBooleanExpressionTooComplex(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                Matcher matcher = BOOLEAN_OP_PATTERN.matcher(line);
                int opCount = 0;
                while (matcher.find()) {
                    opCount++;
                }

                if (opCount > 5) {
                    reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.BOOLEAN_EXPRESSION_TOO_COMPLEX_KEY,
                        String.format("Boolean expression has %d operators (max 5). Extract to intermediate variables.", opCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping boolean complexity detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect chained calls too long (more than 4 operations like a//b//c//d//e).
     */
    public void detectChainedCallsTooLong(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                Matcher matcher = CHAIN_OP_PATTERN.matcher(line);
                int chainCount = 0;
                while (matcher.find()) {
                    chainCount++;
                }

                if (chainCount > 4) {
                    reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.CHAINED_CALLS_TOO_LONG_KEY,
                        String.format("Chained calls has %d operations (max 4). Break into steps.", chainCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping chained calls detection: {}", inputFile.filename());
        }
    }

    // ===== MAINTAINABILITY DETECTION METHODS (15 methods) =====

    /**
     * Detect magic strings (repeated string literals).
     */
    public void detectMagicString(SensorContext context, InputFile inputFile, String content) {
        try {
            Map<String, List<Integer>> stringOccurrences = new HashMap<>();
            Matcher matcher = STRING_LITERAL_PATTERN.matcher(content);

            while (matcher.find()) {
                String literal = matcher.group();
                if (literal.length() > 5) { // Ignore very short strings
                    stringOccurrences.computeIfAbsent(literal, k -> new ArrayList<>()).add(matcher.start());
                }
            }

            for (Map.Entry<String, List<Integer>> entry : stringOccurrences.entrySet()) {
                if (entry.getValue().size() >= 2) {
                    int lineNumber = calculateLineNumber(content, entry.getValue().get(0));
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.MAGIC_STRING_KEY,
                        String.format("Magic string used %d times. Define as named constant.", entry.getValue().size()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping magic string detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect duplicate string literals (used 3+ times).
     */
    public void detectDuplicateStringLiteral(SensorContext context, InputFile inputFile, String content) {
        try {
            Map<String, Integer> stringCounts = new HashMap<>();
            Matcher matcher = STRING_LITERAL_PATTERN.matcher(content);

            while (matcher.find()) {
                String literal = matcher.group();
                stringCounts.put(literal, stringCounts.getOrDefault(literal, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : stringCounts.entrySet()) {
                if (entry.getValue() >= 3) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.DUPLICATE_STRING_LITERAL_KEY,
                        String.format("String '%s' duplicated %d times. Extract to constant.",
                            entry.getKey().substring(0, Math.min(30, entry.getKey().length())), entry.getValue()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping duplicate string detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect hardcoded file paths.
     */
    public void detectHardcodedPath(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PATH_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.HARDCODED_PATH_KEY,
                    "Hardcoded file path. Use configuration or relative paths.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping hardcoded path detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect hardcoded URLs.
     */
    public void detectHardcodedUrl(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = URL_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.HARDCODED_URL_KEY,
                    "Hardcoded URL. Use configuration for environment-specific URLs.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping hardcoded URL detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect complex conditional expressions.
     */
    public void detectConditionalComplexity(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = IF_PATTERN.matcher(content);
            while (matcher.find()) {
                int ifStart = matcher.start();
                int ifEnd = Math.min(ifStart + 200, content.length());
                String condition = content.substring(ifStart, ifEnd);

                int complexity = countOccurrences(condition, "&&|\\|\\|");
                if (complexity > 3) {
                    int lineNumber = calculateLineNumber(content, ifStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CONDITIONAL_COMPLEXITY_KEY,
                        "Complex If condition. Extract to intermediate boolean variables.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping conditional complexity detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect identical If/else branches.
     */
    public void detectIdenticalIfBranches(SensorContext context, InputFile inputFile, String content) {
        try {
            Pattern ifElsePattern = Pattern.compile("If\\s*\\[([^,]+),([^,]+),([^\\]]+)\\]");
            Matcher matcher = ifElsePattern.matcher(content);

            while (matcher.find()) {
                String trueBranch = matcher.group(2).trim();
                String falseBranch = matcher.group(3).trim();

                if (trueBranch.equals(falseBranch)) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.IDENTICAL_IF_BRANCHES_KEY,
                        "If branches are identical. Remove conditional or fix logic error.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping identical branches detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect duplicate code blocks.
     */
    public void detectDuplicateCodeBlock(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = linesCache.get();
            if (lines == null) {
                lines = content.split("\n", -1);
            }

            Map<String, List<Integer>> blockHashes = new HashMap<>();

            // Check 5-line blocks
            for (int i = 0; i <= lines.length - 5; i++) {
                StringBuilder block = new StringBuilder();
                for (int j = 0; j < 5; j++) {
                    block.append(lines[i + j].trim()).append("\n");
                }

                String blockStr = block.toString();
                if (blockStr.trim().length() > 50) { // Ignore trivial blocks
                    blockHashes.computeIfAbsent(blockStr, k -> new ArrayList<>()).add(i);
                }
            }

            for (Map.Entry<String, List<Integer>> entry : blockHashes.entrySet()) {
                if (entry.getValue().size() >= 2) {
                    int lineNumber = entry.getValue().get(0) + 1;
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.DUPLICATE_CODE_BLOCK_KEY,
                        String.format("Duplicate code block found at %d locations. Extract to function.", entry.getValue().size()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping duplicate code detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect god functions (doing too many things).
     */
    public void detectGodFunction(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            while (matcher.find()) {
                int functionStart = matcher.start();
                int functionEnd = functionStart;

                // Find function end (approximate)
                int depth = 0;
                for (int i = functionStart; i < content.length(); i++) {
                    char c = content.charAt(i);
                    if (c == '[') {
                        depth++;
                    } else if (c == ']') {
                        depth--;
                        if (depth == 0) {
                            functionEnd = i;
                            break;
                        }
                    }
                }

                String functionBody = content.substring(functionStart, Math.min(functionEnd + 1, content.length()));
                int functionLines = functionBody.split("\n").length;

                if (functionLines > 100) {
                    int lineNumber = calculateLineNumber(content, functionStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.GOD_FUNCTION_KEY,
                        String.format("Function is %d lines (max 100). Break into smaller functions.", functionLines));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping god function detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect feature envy (function using mostly other module's data).
     */
    public void detectFeatureEnvy(SensorContext context, InputFile inputFile, String content) {
        try {
            // Simple heuristic: function calls other module functions heavily
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            while (matcher.find()) {
                int functionStart = matcher.start();
                int functionEnd = Math.min(functionStart + 500, content.length());
                String functionBody = content.substring(functionStart, functionEnd);

                // Count external function calls (Module`Function pattern)
                int externalCalls = countOccurrences(functionBody, "[A-Z][a-zA-Z0-9]*`[A-Z][a-zA-Z0-9]*");

                if (externalCalls > 5) {
                    int lineNumber = calculateLineNumber(content, functionStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.FEATURE_ENVY_KEY,
                        "Function uses external module heavily. Consider moving to that module.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping feature envy detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect primitive obsession (many primitive parameters).
     */
    public void detectPrimitiveObsession(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_PARAMS_PATTERN.matcher(content);
            while (matcher.find()) {
                String params = matcher.group(2);
                String[] paramList = params.split(",");

                // Count parameters without type hints or structure
                int primitiveCount = 0;
                for (String param : paramList) {
                    if (!param.contains("_") && !param.contains("?") && !param.contains(":")) {
                        primitiveCount++;
                    }
                }

                if (primitiveCount > 5) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PRIMITIVE_OBSESSION_KEY,
                        String.format("Function has %d unstructured parameters. Use Association or custom structure.", primitiveCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping primitive obsession detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect modification of global state.
     */
    public void detectGlobalStateModification(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GLOBAL_ASSIGN_PATTERN.matcher(content);
            Set<String> reported = new HashSet<>();

            while (matcher.find()) {
                String varName = matcher.group(1);
                if (!reported.contains(varName)) {
                    // Check if it's not a local variable
                    int pos = matcher.start();
                    String contextBefore = content.substring(Math.max(0, pos - 100), pos);

                    if (!contextBefore.contains("Module[") && !contextBefore.contains("Block[")) {
                        int lineNumber = calculateLineNumber(content, pos);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.GLOBAL_STATE_MODIFICATION_KEY,
                            String.format("Global variable '%s' modified. Minimize global state mutations.", varName));
                        reported.add(varName);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping global state detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect side effects in expressions (assignments inside expressions).
     */
    public void detectSideEffectInExpression(SensorContext context, InputFile inputFile, String content) {
        try {
            // Look for assignments inside function calls
            Pattern sideEffectPattern = Pattern.compile("\\[[^\\[\\]]*=[^=][^\\[\\]]*\\]");
            Matcher matcher = sideEffectPattern.matcher(content);

            while (matcher.find()) {
                String match = matcher.group();
                if (!match.contains(":=") && !match.contains("->")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.SIDE_EFFECT_IN_EXPRESSION_KEY,
                        "Assignment inside expression. Extract to separate statement for clarity.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping side effect detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect incomplete pattern matching (no default case).
     */
    public void detectIncompletePatternMatch(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PATTERN_MATCH_PATTERN.matcher(content);
            while (matcher.find()) {
                int matchStart = matcher.start();
                int matchEnd = Math.min(matchStart + 300, content.length());
                String matchBody = content.substring(matchStart, matchEnd);

                if (!matchBody.contains("_,") && !matchBody.contains("_, ")) {
                    int lineNumber = calculateLineNumber(content, matchStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.INCOMPLETE_PATTERN_MATCH_KEY,
                        "Pattern match without default case. Add _ pattern for completeness.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping incomplete pattern detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect options without default values.
     */
    public void detectMissingOptionDefault(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = OPTION_PATTERN.matcher(content);
            while (matcher.find()) {
                int optionStart = matcher.start();
                int optionEnd = Math.min(optionStart + 100, content.length());
                String optionContext = content.substring(optionStart, optionEnd);

                if (!optionContext.contains(",") || !optionContext.matches(".*,\\s*[^\\]]+\\].*")) {
                    int lineNumber = calculateLineNumber(content, optionStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.MISSING_OPTION_DEFAULT_KEY,
                        "OptionValue without default. Provide default value as second argument.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing option default detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect unclear option names like flag, mode, setting.
     */
    public void detectOptionNameUnclear(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = UNCLEAR_OPTION_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.OPTION_NAME_UNCLEAR_KEY,
                    "Unclear option name. Use specific, descriptive option names.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping unclear option detection: {}", inputFile.filename());
        }
    }

    // ===== BEST PRACTICES DETECTION METHODS (15 methods) =====

    /**
     * Detect string concatenation inside loops.
     */
    public void detectStringConcatenationInLoop(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher loopMatcher = LOOP_PATTERN.matcher(content);
            while (loopMatcher.find()) {
                int loopStart = loopMatcher.start();
                int loopEnd = Math.min(loopStart + 300, content.length());
                String loopBody = content.substring(loopStart, loopEnd);

                Matcher concatMatcher = STRING_CONCAT_PATTERN.matcher(loopBody);
                if (concatMatcher.find()) {
                    int lineNumber = calculateLineNumber(content, loopStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.STRING_CONCATENATION_IN_LOOP_KEY,
                        "String concatenation in loop is inefficient. Use StringJoin or Table.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping string concatenation in loop detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect boolean comparisons like "x == True".
     */
    public void detectBooleanComparison(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = BOOL_COMPARE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.BOOLEAN_COMPARISON_KEY,
                    "Comparing boolean to True/False. Use 'x' instead of 'x == True'.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping boolean comparison detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect negated boolean comparisons like "!(x == y)" instead of "x != y".
     */
    public void detectNegatedBooleanComparison(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NEGATED_COMPARE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NEGATED_BOOLEAN_COMPARISON_KEY,
                    "Negated comparison. Use '!=' instead of '!(x == y)'.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping negated comparison detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect redundant conditionals like "If[cond, True, False]".
     */
    public void detectRedundantConditional(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = REDUNDANT_IF_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.REDUNDANT_CONDITIONAL_KEY,
                    "Redundant conditional. Use 'cond' instead of 'If[cond, True, False]'.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping redundant conditional detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect empty Catch blocks.
     */
    public void detectEmptyCatchBlock(SensorContext context, InputFile inputFile, String content) {
        try {
            Pattern emptyCatchPattern = Pattern.compile("Catch\\s*\\[\\s*[^,]+\\s*\\]");
            Matcher matcher = emptyCatchPattern.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.EMPTY_CATCH_BLOCK_KEY,
                    "Empty Catch block. Add error handling or remove Catch.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping empty catch detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect Catch without corresponding Throw.
     */
    public void detectCatchWithoutThrow(SensorContext context, InputFile inputFile, String content) {
        try {
            boolean hasCatch = content.contains("Catch[");
            boolean hasThrow = content.contains("Throw[");

            if (hasCatch && !hasThrow) {
                Matcher matcher = CATCH_PATTERN.matcher(content);
                if (matcher.find()) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CATCH_WITHOUT_THROW_KEY,
                        "Catch without Throw. Remove unnecessary Catch or add Throw.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping catch without throw detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect usage of deprecated options.
     */
    public void detectDeprecatedOptionUsage(SensorContext context, InputFile inputFile, String content) {
        try {
            for (String deprecatedOption : DEPRECATED_OPTIONS) {
                if (content.contains(deprecatedOption)) {
                    int pos = content.indexOf(deprecatedOption);
                    int lineNumber = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.DEPRECATED_OPTION_USAGE_KEY,
                        String.format("Deprecated option '%s'. Use modern alternative.", deprecatedOption));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping deprecated option detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect inefficient list queries.
     */
    public void detectListQueryInefficient(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher memberMatcher = MEMBER_Q_PATTERN.matcher(content);
            while (memberMatcher.find()) {
                int pos = memberMatcher.start();
                String contextWindow = content.substring(Math.max(0, pos - 50), Math.min(content.length(), pos + 100));

                if (contextWindow.contains("Do[") || contextWindow.contains("Table[")) {
                    int lineNumber = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.LIST_QUERY_INEFFICIENT_KEY,
                        "MemberQ in loop. Use Intersection or Association for better performance.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping inefficient list query detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect equality checks on real numbers.
     */
    public void detectEqualityCheckOnReals(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = REAL_EQUALITY_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.EQUALITY_CHECK_ON_REALS_KEY,
                    "Equality check on real numbers. Use approximate comparison with tolerance.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping real equality detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect mixing symbolic and numeric computation.
     */
    public void detectSymbolicVsNumericMismatch(SensorContext context, InputFile inputFile, String content) {
        try {
            boolean hasSymbolic = content.contains("Integrate[") || content.contains("D[") || content.contains("Solve[");
            boolean hasNumeric = content.contains("NIntegrate[") || content.contains("ND[") || content.contains("NSolve[");

            if (hasSymbolic && hasNumeric) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.SYMBOLIC_VS_NUMERIC_MISMATCH_KEY,
                    "File mixes symbolic and numeric methods. Separate concerns or document intent.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping symbolic/numeric mismatch detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect Graphics with excessive options (more than 20).
     */
    public void detectGraphicsOptionsExcessive(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GRAPHICS_PATTERN.matcher(content);
            while (matcher.find()) {
                int graphicsStart = matcher.start();
                int graphicsEnd = Math.min(graphicsStart + 500, content.length());
                String graphicsBody = content.substring(graphicsStart, graphicsEnd);

                int optionCount = countOccurrences(graphicsBody, "->");
                if (optionCount > 20) {
                    int lineNumber = calculateLineNumber(content, graphicsStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.GRAPHICS_OPTIONS_EXCESSIVE_KEY,
                        String.format("Graphics has %d options (max 20). Extract to theme or style.", optionCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping graphics options detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect Plot without axis labels.
     */
    public void detectPlotWithoutLabels(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PLOT_PATTERN.matcher(content);
            while (matcher.find()) {
                int plotStart = matcher.start();
                int plotEnd = Math.min(plotStart + 300, content.length());
                String plotBody = content.substring(plotStart, plotEnd);

                if (!plotBody.contains("AxesLabel") && !plotBody.contains("FrameLabel")) {
                    int lineNumber = calculateLineNumber(content, plotStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PLOT_WITHOUT_LABELS_KEY,
                        "Plot without axis labels. Add AxesLabel for clarity.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping plot labels detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect Dataset without column headers.
     */
    public void detectDatasetWithoutHeaders(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DATASET_PATTERN.matcher(content);
            while (matcher.find()) {
                int datasetStart = matcher.start();
                int datasetEnd = Math.min(datasetStart + 200, content.length());
                String datasetBody = content.substring(datasetStart, datasetEnd);

                if (!datasetBody.contains("<|") && !datasetBody.contains("Association[")) {
                    int lineNumber = calculateLineNumber(content, datasetStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.DATASET_WITHOUT_HEADERS_KEY,
                        "Dataset without column headers. Use Association with keys for clarity.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping dataset headers detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect Association with non-string keys.
     */
    public void detectAssociationKeyNotString(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSOCIATION_PATTERN.matcher(content);
            while (matcher.find()) {
                int assocStart = matcher.start();
                int assocEnd = Math.min(assocStart + 200, content.length());
                String assocBody = content.substring(assocStart, assocEnd);

                // Look for numeric or symbol keys
                if (assocBody.matches(".*\\d+\\s*->.*") || assocBody.matches(".*[A-Z][a-zA-Z0-9]*\\s*->.*")) {
                    int lineNumber = calculateLineNumber(content, assocStart);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.ASSOCIATION_KEY_NOT_STRING_KEY,
                        "Association with non-string key. Use string keys for consistency.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping association key detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect pattern test vs condition usage.
     */
    public void detectPatternTestVsCondition(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher conditionMatcher = PATTERN_TEST_PATTERN.matcher(content);
            while (conditionMatcher.find()) {
                int pos = conditionMatcher.start();
                String contextWindow = content.substring(Math.max(0, pos - 20), Math.min(content.length(), pos + 50));

                // Check if simple type test would be better
                if (contextWindow.matches(".*\\w+_/;\\s*(?:IntegerQ|NumericQ|StringQ).*")) {
                    int lineNumber = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PATTERN_TEST_VS_CONDITION_KEY,
                        "Use PatternTest (?) instead of Condition (/;) for simple type checks.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping pattern test vs condition detection: {}", inputFile.filename());
        }
    }
}
