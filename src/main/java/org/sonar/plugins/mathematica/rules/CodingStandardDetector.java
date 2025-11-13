package org.sonar.plugins.mathematica.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * Detector for Coding Standard rules (32 rules).
 *
 * These rules cover:
 * - Syntax and Whitespace (18 rules)
 * - Local Variables (2 rules)
 * - Function Structure (6 rules)
 * - Code Organization (2 CRITICAL rules)
 * - Patterns (4 rules)
 */
public class CodingStandardDetector extends BaseDetector {

    // String constants for messages
    private static final String VARIABLE_PREFIX = "Variable '";
    private static final String FUNCTION_PREFIX = "Function '";

    // ===== SYNTAX AND WHITESPACE PATTERNS (18 rules) =====

    private static final Pattern BRACKET_SPACING_BEFORE_PATTERN = Pattern.compile("\\w\\s+\\[");
    private static final Pattern MODULE_ASSIGNMENT_PATTERN = Pattern.compile(
        "(?:Module|Block|With)\\s*\\[\\s*\\{[^}]*\\b(\\w+)\\s*=(?!=)[^}]*\\}"
    );
    private static final Pattern COMPLEX_BOOLEAN_SHORTHAND_PATTERN = Pattern.compile(
        "(?:&&|\\|\\|)[^\\n]*\\n[^\\n]*(?:&&|\\|\\|)"
    );
    private static final Pattern MULTILINE_MAP_SHORTHAND_PATTERN = Pattern.compile(
        "/@[^\\n]*\\n"
    );
    private static final Pattern ERROR_MESSAGE_DELAYED_PATTERN = Pattern.compile(
        "(\\w{1,100})::(\\w{1,100})\\s*:="
    );
    private static final Pattern CONDITIONAL_FUNCTION_DEF_PATTERN = Pattern.compile(
        "([A-Za-z]\\w*)\\s*\\[([^\\]]*)\\]\\s*:=[^/]*/;",
        Pattern.DOTALL
    );

    private static final Pattern DEREFERENCING_SYNTAX_PATTERN = Pattern.compile(
        "\\$\\w+\\[\\w+\\]"
    );
    private static final Pattern COMPOSITION_PATTERN = Pattern.compile(
        "\\b(?:Composition|RightComposition|Fold|FoldList|Nest|NestList)\\s*\\["
    );
    private static final Pattern IN_PLACE_MODIFICATION_PATTERN = Pattern.compile(
        "\\b(?:AppendTo|PrependTo|AssociateTo)\\s*\\["
    );

    // Variable names with more than 3 words - limit quantifier to prevent stack overflow
    private static final Pattern LONG_VARIABLE_NAME_PATTERN = Pattern.compile(
        "\\b[a-z]+(?:[A-Z][a-z]+){4,10}\\b"
    );

    // Custom Association/List of Rules as parameters
    private static final Pattern CUSTOM_ASSOCIATION_PARAM_PATTERN = Pattern.compile(
        "\\b([A-Z]\\w{0,100})\\s*\\[[^\\[]{0,500}(?:<\\||\\{[^}]{0,200}->)[^\\]]{0,500}\\]\\s*:="
    );
    private static final Pattern GLOBAL_NO_DOLLAR_PATTERN = Pattern.compile(
        "^\\s*([A-Z][a-zA-Z0-9]+)\\s*=(?!=)",
        Pattern.MULTILINE
    );

    // Function names with more than 3 words - limit quantifier to prevent stack overflow
    private static final Pattern LONG_FUNCTION_NAME_PATTERN = Pattern.compile(
        "\\b([A-Z][a-z]+(?:[A-Z][a-z]+){3,10})\\s*\\["
    );
    private static final Pattern LITTER_WORDS_PATTERN = Pattern.compile(
        "\\b(?:Do|Make|Get|And)([A-Z][a-z]+|[A-Z][a-z]+(?:Do|Make|Get|And))\\s*\\["
    );
    private static final Pattern LONG_PURE_FUNCTION_PATTERN = Pattern.compile(
        "Function\\s*\\[[^\\]\\n]*\\n[^\\]]*\\]\\s*&"
    );
    private static final Pattern TIME_CONSTRAINED_PATTERN = Pattern.compile(
        "\\bTimeConstrained\\s*\\["
    );

    // Pattern definitions not ending in P
    private static final Pattern PATTERN_NO_P_SUFFIX_PATTERN = Pattern.compile(
        "([A-Z]\\w*)(?<!P)\\s*=\\s*(?:_(?:_)?|Alternatives\\[|\\|)"
    );

    // Pattern test functions not ending in Q - unwrap unnecessary group
    private static final Pattern PATTERN_TEST_NO_Q_SUFFIX_PATTERN = Pattern.compile(
        "([A-Z]\\w{0,100})(?<!Q)\\s*\\[[^\\]]{0,200}_[^\\]]{0,200}\\]\\s*:=.{0,1000}?(?:Q\\[|MatchQ\\[)",
        Pattern.DOTALL
    );

    // Enumerated patterns with strings - limit quantifier to prevent stack overflow
    private static final Pattern ENUMERATED_STRING_PATTERN = Pattern.compile(
        "=\\s*(?:\"\\w+\"\\s*\\|\\s*){1,20}\"\\w+\""
    );

    // Helper patterns for abbreviation detection - bounded to prevent ReDoS
    private static final Pattern CONSONANT_CLUSTER_PATTERN = Pattern.compile(
        "[bcdfghjklmnpqrstvwxz]{3,}"
    );
    private static final Pattern CAMEL_CASE_SHORT_PATTERN = Pattern.compile(
        "\\w{1,2}[A-Z]"
    );
    private static final Pattern UPPERCASE_CLUSTER_PATTERN = Pattern.compile(
        "[A-Z]{2,}"
    );

    // ===== DETECTION METHODS =====

    public void detect(SensorContext context, InputFile inputFile, String fileContent, String contentWithoutComments) {
        // Syntax and Whitespace (18 rules)
        detectBracketSpacingBefore(context, inputFile, contentWithoutComments);
        detectVariableAssignmentInModuleDef(context, inputFile, contentWithoutComments);
        detectComplexBooleanShorthand(context, inputFile, contentWithoutComments);
        detectMultilineMapShorthand(context, inputFile, contentWithoutComments);
        detectErrorMessageDelayed(context, inputFile, contentWithoutComments);
        detectConditionalFunctionDef(context, inputFile, contentWithoutComments);
        detectDereferencingSyntax(context, inputFile, contentWithoutComments);
        detectNoEmptyLineBetweenCode(context, inputFile, fileContent);
        detectNonLinearEvaluation(context, inputFile, contentWithoutComments);
        detectInPlaceModification(context, inputFile, contentWithoutComments);

        // Local Variables (2 rules)
        detectAbbreviatedVariables(context, inputFile, contentWithoutComments);
        detectLongVariableNames(context, inputFile, contentWithoutComments);

        // Function Structure (6 rules)
        detectCustomAssociationParams(context, inputFile, contentWithoutComments);
        detectGlobalNoDollar(context, inputFile, contentWithoutComments);
        detectLongFunctionNames(context, inputFile, contentWithoutComments);
        detectLitterWords(context, inputFile, contentWithoutComments);
        detectLongPureFunctions(context, inputFile, contentWithoutComments);

        // Code Organization (2 CRITICAL rules)
        detectTimeConstrained(context, inputFile, contentWithoutComments);

        // Patterns (4 rules)
        detectPatternNoPSuffix(context, inputFile, contentWithoutComments);
        detectPatternTestNoQSuffix(context, inputFile, contentWithoutComments);
        detectEnumeratedStringPattern(context, inputFile, contentWithoutComments);
    }

    // ===== SYNTAX AND WHITESPACE DETECTION METHODS =====

    private void detectBracketSpacingBefore(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = BRACKET_SPACING_BEFORE_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.BRACKET_SPACING_BEFORE_KEY,
                "Remove whitespace before opening bracket");
        }
    }

    private void detectVariableAssignmentInModuleDef(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = MODULE_ASSIGNMENT_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            String varName = matcher.group(1);
            reportIssue(context, inputFile, line, MathematicaRuleKeys.VARIABLE_ASSIGNMENT_IN_MODULE_DEF_KEY,
                VARIABLE_PREFIX + varName + "' should be assigned in Module body, not in definition");
        }
    }

    private void detectComplexBooleanShorthand(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = COMPLEX_BOOLEAN_SHORTHAND_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.EXPLICIT_AND_OR_FOR_COMPLEX_BOOLEAN_KEY,
                "Use explicit And[...] or Or[...] for complex multi-line boolean expressions");
        }
    }

    private void detectMultilineMapShorthand(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = MULTILINE_MAP_SHORTHAND_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.MAP_NOT_SHORTHAND_MULTILINE_KEY,
                "Use Map[...] instead of /@ for multi-line statements");
        }
    }

    private void detectErrorMessageDelayed(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = ERROR_MESSAGE_DELAYED_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            String funcName = matcher.group(1);
            String msgName = matcher.group(2);
            reportIssue(context, inputFile, line, MathematicaRuleKeys.ERROR_MESSAGE_WITH_SET_KEY,
                "Error message " + funcName + "::" + msgName + " should be defined with = not :=");
        }
    }

    private void detectConditionalFunctionDef(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = CONDITIONAL_FUNCTION_DEF_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            String funcName = matcher.group(1);
            reportIssue(context, inputFile, line, MathematicaRuleKeys.CONDITIONAL_FUNCTION_DEFINITION_KEY,
                FUNCTION_PREFIX + funcName + "' should not use /; in definition. Use If or Which in body instead");
        }
    }

    private void detectDereferencingSyntax(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = DEREFERENCING_SYNTAX_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.DEREFERENCING_SYNTAX_KEY,
                "Do not use dereferencing syntax. Use explicit Download or Lookup instead");
        }
    }

    private void detectNoEmptyLineBetweenCode(SensorContext context, InputFile inputFile, String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length - 1; i++) {
            String currentLine = lines[i].trim();
            String nextLine = lines[i + 1].trim();

            // Check if both lines have code (non-empty, non-comment) and look like separate statements
            if (!currentLine.isEmpty()
                && !nextLine.isEmpty()
                && !currentLine.startsWith("(*")
                && !nextLine.startsWith("(*")
                && (currentLine.endsWith(";") || currentLine.endsWith("]"))
                && (nextLine.matches("^[A-Z]\\w*\\[.*") || nextLine.matches("^\\w+\\s*=.*"))) {
                reportIssue(context, inputFile, i + 2, MathematicaRuleKeys.EMPTY_LINE_BETWEEN_CODE_KEY,
                    "Consider adding an empty line between code sections");
            }
        }
    }

    private void detectNonLinearEvaluation(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = COMPOSITION_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.NON_LINEAR_EVALUATION_KEY,
                "Non-linear evaluation structures should be used sparingly for clarity");
        }
    }

    private void detectInPlaceModification(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = IN_PLACE_MODIFICATION_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.LIST_MODIFICATION_IN_PLACE_KEY,
                "Avoid in-place list modification. Use immutable patterns instead");
        }
    }

    // ===== LOCAL VARIABLES DETECTION METHODS =====

    private void detectAbbreviatedVariables(SensorContext context, InputFile inputFile, String content) {
        // Look for variable assignments with abbreviated names
        Pattern varPattern = Pattern.compile("\\b([a-z]\\w*)\\s*=(?!=)");
        Matcher matcher = varPattern.matcher(content);
        while (matcher.find()) {
            String varName = matcher.group(1);
            // Check if it looks abbreviated (short consonant clusters, single letters, etc.)
            if (looksAbbreviated(varName)) {
                int line = getLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRuleKeys.VARIABLE_FULL_WORDS_KEY,
                    VARIABLE_PREFIX + varName + "' appears abbreviated. Use full words");
            }
        }
    }

    private boolean looksAbbreviated(String name) {
        // Skip standard abbreviations
        if (name.equals("ops") || name.equals("qual")) {
            return false;
        }
        // Check for common abbreviation patterns
        return name.length() < 3
               || CONSONANT_CLUSTER_PATTERN.matcher(name).find()
               || CAMEL_CASE_SHORT_PATTERN.matcher(name).find();
    }

    private void detectLongVariableNames(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = LONG_VARIABLE_NAME_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            String varName = matcher.group();
            reportIssue(context, inputFile, line, MathematicaRuleKeys.VARIABLE_NAME_THREE_WORDS_KEY,
                VARIABLE_PREFIX + varName + "' has more than 3 words. Keep names concise");
        }
    }

    // ===== FUNCTION STRUCTURE DETECTION METHODS =====

    private void detectCustomAssociationParams(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = CUSTOM_ASSOCIATION_PARAM_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            String funcName = matcher.group(1);
            reportIssue(context, inputFile, line, MathematicaRuleKeys.CUSTOM_ASSOCIATIONS_AS_INPUTS_KEY,
                FUNCTION_PREFIX + funcName + "' should not use custom associations/lists of rules as parameters");
        }
    }

    private void detectGlobalNoDollar(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = GLOBAL_NO_DOLLAR_PATTERN.matcher(content);
        while (matcher.find()) {
            String varName = matcher.group(1);
            // Check if it looks like a global constant (not a function definition)
            if (!UPPERCASE_CLUSTER_PATTERN.matcher(varName).find() && !content.contains(varName + "[")) {
                int line = getLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRuleKeys.GLOBAL_VARIABLE_DOLLAR_PREFIX_KEY,
                    "Global variable '" + varName + "' should start with $ prefix");
            }
        }
    }

    private void detectLongFunctionNames(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = LONG_FUNCTION_NAME_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            String funcName = matcher.group(1);
            reportIssue(context, inputFile, line, MathematicaRuleKeys.FUNCTION_NAME_THREE_WORDS_KEY,
                FUNCTION_PREFIX + funcName + "' has more than 3 words. Keep names concise");
        }
    }

    private void detectLitterWords(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = LITTER_WORDS_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.FUNCTION_NAME_LITTER_WORDS_KEY,
                "Function name contains litter words (Do, Make, Get, And). Use more meaningful names");
        }
    }

    private void detectLongPureFunctions(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = LONG_PURE_FUNCTION_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.PURE_FUNCTION_SHORT_OPERATIONS_KEY,
                "Pure functions should be used for short (<1 line) operations only");
        }
    }

    // ===== CODE ORGANIZATION DETECTION METHODS =====

    private void detectTimeConstrained(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = TIME_CONSTRAINED_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.TIME_CONSTRAINED_USAGE_KEY,
                "CRITICAL: TimeConstrained can kill WSTP programs. Avoid unless absolutely necessary");
        }
    }

    // ===== PATTERN DETECTION METHODS =====

    private void detectPatternNoPSuffix(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = PATTERN_NO_P_SUFFIX_PATTERN.matcher(content);
        while (matcher.find()) {
            String patternName = matcher.group(1);
            // Only flag if it looks like a pattern definition
            if (patternName.length() > 2 && !patternName.endsWith("P")) {
                int line = getLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRuleKeys.PATTERN_NAME_ENDS_WITH_P_KEY,
                    "Pattern '" + patternName + "' should end with uppercase P");
            }
        }
    }

    private void detectPatternTestNoQSuffix(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = PATTERN_TEST_NO_Q_SUFFIX_PATTERN.matcher(content);
        while (matcher.find()) {
            String funcName = matcher.group(1);
            if (!funcName.endsWith("Q")) {
                int line = getLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRuleKeys.PATTERN_TEST_NAME_ENDS_WITH_Q_KEY,
                    "Pattern test function '" + funcName + "' should end with uppercase Q");
            }
        }
    }

    private void detectEnumeratedStringPattern(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = ENUMERATED_STRING_PATTERN.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            reportIssue(context, inputFile, line, MathematicaRuleKeys.ENUMERATED_PATTERN_SYMBOLS_KEY,
                "Enumerated type patterns should use symbols, not strings");
        }
    }

    // ===== UTILITY METHODS =====

    private int getLineNumber(String content, int position) {
        return content.substring(0, position).split("\n", -1).length;
    }
}
