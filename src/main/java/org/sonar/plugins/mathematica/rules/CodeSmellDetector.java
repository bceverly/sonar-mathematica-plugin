package org.sonar.plugins.mathematica.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * Detector for Code Smell rules (33 rules total).
 * Handles maintainability, complexity, and performance issues.
 */
public class CodeSmellDetector extends BaseDetector {

    // ===== PATTERNS FOR CODE SMELL DETECTION =====

    // Basic code smell patterns
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\.?\\d*(?:[eE][+-]?\\d+)?\\b");
    private static final Pattern EMPTY_BLOCK_PATTERN = Pattern.compile(
        "(?:Module|Block|With)\\s*\\[\\s*\\{[^}]*\\}\\s*,?\\s*\\]",
        Pattern.MULTILINE
    );
    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[([^\\]]*)\\]\\s*:=",
        Pattern.MULTILINE
    );
    private static final Pattern DEBUG_CODE_PATTERN = Pattern.compile(
        "(?:Print|Echo|PrintTemporary|TracePrint|Trace|Monitor)\\s*\\[|" +
        "\\$DebugMessages\\s*=\\s*True"
    );
    private static final Pattern MODULE_BLOCK_WITH_PATTERN = Pattern.compile(
        "(?:Module|Block|With)\\s*\\[\\s*\\{([^}]+)\\}"
    );
    private static final Pattern DOUBLE_SEMICOLON_PATTERN = Pattern.compile(
        ";;|\\[\\s*,\\s*;|,\\s*;\\s*\\]"
    );
    private static final Pattern DEPRECATED_FUNCTIONS_PATTERN = Pattern.compile(
        "\\$RecursionLimit"
    );

    // Performance patterns
    private static final Pattern APPEND_IN_LOOP_PATTERN = Pattern.compile(
        "(?:Do|While|For|Table)\\s*\\[[^\\]]*?(?:AppendTo|Append)\\s*\\["
    );
    private static final Pattern STRING_CONCAT_LOOP_PATTERN = Pattern.compile(
        "(?:Do|While|For)\\s*\\[[^\\]]*?<>"
    );
    private static final Pattern NUMERICAL_LOOP_PATTERN = Pattern.compile(
        "Do\\s*\\[[^\\]]*(?:sum|total|count|result)\\s*[+\\-*/]?="
    );
    private static final Pattern NESTED_MAP_TABLE_PATTERN = Pattern.compile(
        "(?:Map|Table)\\s*\\[[^\\[]*(?:Map|Table)\\s*\\["
    );
    private static final Pattern PLOT_IN_LOOP_PATTERN = Pattern.compile(
        "(?:Do|While|For|Table)\\s*\\[[^\\]]*?(?:Plot|ListPlot|ListLinePlot|ContourPlot|Plot3D)\\s*\\["
    );
    private static final Pattern FUNCTION_CALL_EXTRACTION_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[[^\\[\\]]*\\]");

    // Best practices patterns
    private static final Pattern GENERIC_VARIABLE_PATTERN = Pattern.compile(
        "\\b(?:temp|data|result|x|y|z|val|value|item)\\s*=(?!=)"
    );
    private static final Pattern PUBLIC_FUNCTION_PATTERN = Pattern.compile(
        "([A-Z][a-zA-Z0-9]*)\\s*\\[[^\\]]*\\]\\s*:="
    );
    private static final Pattern MANY_OPTIONAL_PARAMS_PATTERN = Pattern.compile(
        "\\w+\\[([^\\]]*_:[^\\]]*,){3,}"
    );
    private static final Pattern GLOBAL_ASSIGNMENT_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[[^\\]]*\\]\\s*:=\\s*\\([^;]*(?:[A-Z][a-zA-Z0-9]*\\s*=)"
    );
    private static final Pattern COMPLEX_BOOLEAN_PATTERN = Pattern.compile(
        "If\\s*\\[[^\\[]*(?:&&|\\|\\|)[^\\[]*(?:&&|\\|\\|)[^\\[]*(?:&&|\\|\\|)[^\\[]*(?:&&|\\|\\|)[^\\[]*(?:&&|\\|\\|)"
    );

    // Pre-compiled patterns for performance
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("\\w+\\s*=\\s*[^=]");
    private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile("[a-zA-Z]\\w*\\s*\\[");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(?:Module|Block|With|Table|Map|Apply|Function|If|While|Do|For|Return|Print|Plot|Solve)\\s*\\[");
    private static final Pattern OPERATOR_PATTERN_OPTIMIZED = Pattern.compile("[-+*/^]\\s*[a-zA-Z0-9]");

    // Phase 4 patterns (performance optimization - pre-compiled)
    private static final Pattern OVERCOMPLEX_PATTERN_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[[^\\]]*(_\\w*\\s*\\|[^\\]]*\\|[^\\]]*\\|[^\\]]*\\|[^\\]]*\\|[^\\]]*)\\]");
    private static final Pattern MIXED_RULE_TYPES_PATTERN = Pattern.compile("\\{[^}]*->\\s*[^}]*:>[^}]*\\}|\\{[^}]*:>\\s*[^}]*->[^}]*\\}");
    private static final Pattern DOWNVALUES_FUNC_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*\\[[^\\]]*_[^\\]]*\\]\\s*:=");
    private static final Pattern PATTERN_TEST_FUNC_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[([^\\]]*_[a-zA-Z]\\w*[^\\]]*)\\]\\s*:=");
    private static final Pattern PURE_FUNC_COMPLEX_PATTERN = Pattern.compile("#\\d+[^&]*#\\d+[^&]*#\\d+[^&]*&");
    private static final Pattern OPERATOR_PRECEDENCE_PATTERN = Pattern.compile("[a-zA-Z]\\w*\\s*/[@/@]\\s*[a-zA-Z]\\w*\\s*[@/][@/]");
    private static final Pattern WINDOWS_PATH_PATTERN = Pattern.compile("\"[C-Z]:\\\\\\\\[^\"]+\"");
    private static final Pattern UNIX_PATH_PATTERN = Pattern.compile("\"/(?:Users|home)/[^\"]+\"");
    private static final Pattern RETURN_TYPE_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*\\[[^\\]]*\\]\\s*:=\\s*(\\{|<\\|)");
    private static final Pattern GLOBAL_MODIFY_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*\\[[^\\]]*\\]\\s*:=[^;]*:?=");
    private static final Pattern MANIPULATE_PATTERN = Pattern.compile("Manipulate\\s*\\[");
    private static final Pattern GLOBAL_CONTEXT_PATTERN = Pattern.compile("Global`[a-zA-Z]\\w*");
    private static final Pattern PART_ACCESS_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\[\\[(\\d+)\\]\\]");
    private static final Pattern REPEATED_PART_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\[\\[\\d+\\]\\];[^;]*([a-zA-Z]\\w*)\\[\\[\\d+\\]\\];[^;]*([a-zA-Z]\\w*)\\[\\[\\d+\\]\\]");
    private static final Pattern RECURSIVE_FUNC_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[([^\\]]+)\\]\\s*:=[^;]*\\1\\s*\\[");
    private static final Pattern STRINGJOIN_PATTERN = Pattern.compile("[^<]*<>[^<]*<>[^<]*<>");
    private static final Pattern SELECT_LINEAR_PATTERN = Pattern.compile("Select\\s*\\[[^,]+,\\s*#\\[\\[[^\\]]+\\]\\]\\s*==");
    private static final Pattern REPEATED_CALC_PATTERN = Pattern.compile("Do\\s*\\[[^,]*([A-Z][a-zA-Z0-9]+)\\s*\\[[^\\]]*\\][^,]*,\\s*\\{([a-z]\\w*),");
    private static final Pattern POSITION_PATTERN = Pattern.compile("Position\\s*\\[[^\\]]+\\]");
    private static final Pattern FLATTEN_TABLE_PATTERN = Pattern.compile("Flatten\\s*\\[\\s*Table\\s*\\[");
    private static final Pattern LARGE_TABLE_PATTERN = Pattern.compile("Table\\s*\\[[^,]+,\\s*\\{[^,]+,\\s*\\d{4,}");
    private static final Pattern ZERO_TABLE_PATTERN = Pattern.compile("Table\\s*\\[\\s*0\\s*,\\s*\\{[^,]+,\\s*(\\d+)");
    private static final Pattern DOUBLE_TRANSPOSE_PATTERN = Pattern.compile("Transpose\\s*\\[[^\\[]*Transpose\\s*\\[");
    private static final Pattern TOEXPRESSION_LOOP_PATTERN = Pattern.compile("(?:Do|Table|While)\\s*\\[[^\\[]*ToExpression\\s*\\[");
    private static final Pattern COMPILE_PATTERN = Pattern.compile("Compile\\s*\\[");
    // Additional patterns for original rules (optimized - pre-compiled for performance)
    private static final Pattern SIMPLE_CHECK_PATTERN = Pattern.compile("Check\\s*\\[[^,]+,\\s*(?:\\$Failed|Null|None)\\s*\\]");
    private static final Pattern QUIET_PATTERN = Pattern.compile("Quiet\\s*\\[");
    private static final Pattern IF_PATTERN = Pattern.compile("If\\s*\\[([^\\[]+),\\s*([^,]+),\\s*([^\\]]+)\\]");
    private static final Pattern FUNCTION_WITH_IF_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[[^\\]]*\\]\\s*:=\\s*(?:Module|Block)?\\s*\\[[^\\]]*If\\[");


    /**
     * Detect magic numbers in code.
     */
    public void detectMagicNumbers(SensorContext context, InputFile inputFile, String content, List<int[]> commentRanges) {
        try {
            Matcher numberMatcher = NUMBER_PATTERN.matcher(content);
            while (numberMatcher.find()) {
                String number = numberMatcher.group();
                // Skip common non-magic numbers
                if (number.equals("0") || number.equals("1") || number.equals("2")) {
                    continue;
                }

                int position = numberMatcher.start();

                // Skip if in comment or string
                if (isInsideComment(position, commentRanges) || isInsideStringLiteral(content, position)) {
                    continue;
                }

                // Skip if this is an association mapping (enum-like pattern: Key -> Number)
                if (isAssociationMapping(content, position)) {
                    continue;
                }

                int line = calculateLineNumber(content, position);
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.MAGIC_NUMBER_KEY,
                    "Replace this magic number with a named constant.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping magic number detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect empty blocks.
     */
    public void detectEmptyBlocks(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = EMPTY_BLOCK_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.EMPTY_BLOCK_KEY,
                    "Remove this empty block.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping empty block detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect functions that are too long.
     */
    public void detectLongFunctions(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);
                int startLine = calculateLineNumber(content, matcher.start());

                // Simple heuristic: count lines until next function or end
                int nextFunctionPos = content.indexOf(":=", matcher.end() + 1);
                int endPos = nextFunctionPos > 0 ? nextFunctionPos : content.length();
                int functionLines = calculateLineNumber(content, endPos) - startLine;

                if (functionLines > 100) {
                    reportIssue(context, inputFile, startLine, MathematicaRulesDefinition.FUNCTION_LENGTH_KEY,
                        String.format("Function '%s' is %d lines long (max 100 allowed).", functionName, functionLines));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping long function detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect empty catch blocks (Quiet with no error handling).
     */
    public void detectEmptyCatchBlocks(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SIMPLE_CHECK_PATTERN.matcher(content);
            matcher = SIMPLE_CHECK_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.EMPTY_CATCH_KEY,
                    "Empty error handling - consider logging or handling the error.");
            }

            matcher = QUIET_PATTERN.matcher(content);
            matcher = QUIET_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.EMPTY_CATCH_KEY,
                    "Quiet[] suppresses errors - consider explicit error handling.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping empty catch detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect debug code.
     */
    public void detectDebugCode(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DEBUG_CODE_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.DEBUG_CODE_KEY,
                    "Remove debug code before committing.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping debug code detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect unused variables.
     */
    public void detectUnusedVariables(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MODULE_BLOCK_WITH_PATTERN.matcher(content);
            while (matcher.find()) {
                String varBlock = matcher.group(1);
                String[] vars = varBlock.split(",");

                for (String var : vars) {
                    String varName = var.trim().split("[\\s=]")[0];
                    if (varName.isEmpty()) continue;

                    // Check if variable is used outside its declaration
                    int declEnd = matcher.end();
                    String remainingContent = content.substring(declEnd);
                    if (!remainingContent.contains(varName)) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line, MathematicaRulesDefinition.UNUSED_VARIABLES_KEY,
                            String.format("Variable '%s' is declared but never used.", varName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping unused variable detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect duplicate function definitions.
     */
    public void detectDuplicateFunctions(SensorContext context, InputFile inputFile, String content) {
        try {
            Map<String, List<Integer>> functionDefs = new java.util.HashMap<>();
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);

            while (matcher.find()) {
                String signature = matcher.group(0).trim();
                int line = calculateLineNumber(content, matcher.start());

                functionDefs.computeIfAbsent(signature, k -> new java.util.ArrayList<>()).add(line);
            }

            for (Map.Entry<String, List<Integer>> entry : functionDefs.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for (int line : entry.getValue()) {
                        reportIssue(context, inputFile, line, MathematicaRulesDefinition.DUPLICATE_FUNCTION_KEY,
                            "Duplicate function definition found.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping duplicate function detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect functions with too many parameters.
     */
    public void detectTooManyParameters(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            while (matcher.find()) {
                String params = matcher.group(2);
                int paramCount = params.isEmpty() ? 0 : params.split(",").length;

                if (paramCount > 7) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.TOO_MANY_PARAMETERS_KEY,
                        String.format("Function has %d parameters (max 7 allowed).", paramCount));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping too many parameters detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect deeply nested control structures.
     */
    public void detectDeeplyNested(SensorContext context, InputFile inputFile, String content) {
        try {
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int depth = 0;
                int maxDepth = 0;

                for (char c : line.toCharArray()) {
                    if (c == '[') depth++;
                    if (c == ']') depth--;
                    maxDepth = Math.max(maxDepth, depth);
                }

                if (maxDepth > 3 && line.matches(".*\\b(?:If|While|Do|For|Module|Block)\\b.*")) {
                    reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.DEEPLY_NESTED_KEY,
                        "Control structure is too deeply nested (depth > 3).");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping deeply nested detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing documentation in complex functions.
     */
    public void detectMissingDocumentation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);
                int startPos = matcher.start();

                // Check if function is complex (has multiple lines)
                int nextFunctionPos = content.indexOf(":=", matcher.end() + 1);
                int endPos = nextFunctionPos > 0 ? nextFunctionPos : content.length();
                String functionBody = content.substring(matcher.end(), endPos);

                if (functionBody.split("\n").length > 20) {
                    // Check for comment before function
                    int lineStart = content.lastIndexOf('\n', startPos) + 1;
                    String textBefore = content.substring(Math.max(0, lineStart - 200), lineStart);

                    if (!textBefore.contains("(*") || !textBefore.contains("*)")) {
                        int line = calculateLineNumber(content, startPos);
                        reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_DOCUMENTATION_KEY,
                            String.format("Complex function '%s' should have documentation.", functionName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing documentation detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect inconsistent naming conventions.
     */
    public void detectInconsistentNaming(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            Set<String> camelCaseNames = new HashSet<>();
            Set<String> underscoreNames = new HashSet<>();

            while (matcher.find()) {
                String name = matcher.group(1);
                if (name.contains("_")) {
                    underscoreNames.add(name);
                } else if (name.matches("[a-z][a-zA-Z0-9]*")) {
                    camelCaseNames.add(name);
                }
            }

            if (!camelCaseNames.isEmpty() && !underscoreNames.isEmpty()) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.INCONSISTENT_NAMING_KEY,
                    "Inconsistent naming: mix of camelCase and snake_case found.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping inconsistent naming detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect identical branches in If/Which statements.
     */
    public void detectIdenticalBranches(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = IF_PATTERN.matcher(content);
            matcher = IF_PATTERN.matcher(content);

            while (matcher.find()) {
                String trueBranch = matcher.group(2).trim();
                String falseBranch = matcher.group(3).trim();

                if (trueBranch.equals(falseBranch)) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.IDENTICAL_BRANCHES_KEY,
                        "If statement has identical true and false branches.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping identical branches detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect overly complex expressions.
     */
    public void detectExpressionTooComplex(SensorContext context, InputFile inputFile, String content, List<int[]> commentRanges) {
        try {
            String[] lines = content.split("\n");
            int currentPos = 0;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                // Skip if this line is inside a comment
                boolean inComment = false;
                for (int[] range : commentRanges) {
                    // Check if the start of this line is within a comment range
                    if (currentPos >= range[0] && currentPos < range[1]) {
                        inComment = true;
                        break;
                    }
                }

                if (!inComment) {
                    int operatorCount = countOccurrences(line, "[-+*/^&|<>=!]");

                    if (operatorCount > 10) {
                        reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY,
                            String.format("Expression has %d operators (max 10 allowed).", operatorCount));
                    }
                }

                // Move position forward (line length + newline character)
                currentPos += line.length() + 1;
            }
        } catch (Exception e) {
            LOG.warn("Skipping expression complexity detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect use of deprecated functions.
     */
    public void detectDeprecatedFunctions(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DEPRECATED_FUNCTIONS_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.DEPRECATED_FUNCTION_KEY,
                    "Use of deprecated $RecursionLimit - use $IterationLimit instead.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping deprecated function detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect empty statements (double semicolons).
     */
    public void detectEmptyStatement(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DOUBLE_SEMICOLON_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.EMPTY_STATEMENT_KEY,
                    "Remove empty statement.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping empty statement detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== PERFORMANCE RULES =====

    /**
     * Detect AppendTo in loops (O(n²) performance).
     */
    public void detectAppendInLoop(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = APPEND_IN_LOOP_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.APPEND_IN_LOOP_KEY,
                    "AppendTo in loops creates O(n²) performance. Use Table, Reap/Sow, or pre-allocate.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping AppendInLoop detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect repeated expensive function calls.
     */
    public void detectRepeatedFunctionCalls(SensorContext context, InputFile inputFile, String content) {
        try {
            Map<String, Integer> callCounts = new java.util.HashMap<>();
            Matcher matcher = FUNCTION_CALL_EXTRACTION_PATTERN.matcher(content);

            while (matcher.find()) {
                String call = matcher.group(0).trim();
                callCounts.put(call, callCounts.getOrDefault(call, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : callCounts.entrySet()) {
                if (entry.getValue() >= 3 && entry.getKey().matches(".*(?:Solve|NSolve|Integrate|NIntegrate).*")) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.REPEATED_FUNCTION_CALLS_KEY,
                        String.format("Expensive function call '%s' repeated %d times - consider caching.",
                            entry.getKey(), entry.getValue()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping repeated function calls detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect string concatenation in loops.
     */
    public void detectStringConcatInLoop(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = STRING_CONCAT_LOOP_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.STRING_CONCAT_IN_LOOP_KEY,
                    "String concatenation in loops is O(n²). Use StringJoin or Table.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping string concat in loop detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect uncompiled numerical code.
     */
    public void detectUncompiledNumerical(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NUMERICAL_LOOP_PATTERN.matcher(content);
            while (matcher.find()) {
                // Check if Compile is nearby
                int start = Math.max(0, matcher.start() - 200);
                int end = Math.min(content.length(), matcher.end() + 200);
                String context_text = content.substring(start, end);

                if (!context_text.contains("Compile")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.UNCOMPILED_NUMERICAL_KEY,
                        "Numerical loop should use Compile for 10-100x speed improvement.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping uncompiled numerical detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect operations that break packed arrays.
     */
    public void detectPackedArrayBreaking(SensorContext context, InputFile inputFile, String content) {
        try {
            // Simple heuristic: mixed numeric/symbolic operations
            if (content.contains("Append") && content.contains("Table") && content.contains("Symbol")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.PACKED_ARRAY_BREAKING_KEY,
                    "Operations may unpack arrays. Use Developer`PackedArrayQ to verify.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping packed array detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect nested Map/Table that could be optimized.
     */
    public void detectNestedMapTable(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NESTED_MAP_TABLE_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.NESTED_MAP_TABLE_KEY,
                    "Nested Map/Table can often be replaced with Outer or single vectorized operation.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping nested Map/Table detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect large temporary expressions that should be assigned.
     */
    public void detectLargeTempExpressions(SensorContext context, InputFile inputFile, String content) {
        try {
            // Heuristic: very long expressions without assignment
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.length() > 200 && !line.contains("=") && line.contains("[")) {
                    reportIssue(context, inputFile, i + 1, MathematicaRulesDefinition.LARGE_TEMP_EXPRESSIONS_KEY,
                        "Large expression should be assigned to variable for memory management visibility.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping large temp expressions detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect plotting functions in loops.
     */
    public void detectPlotInLoop(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PLOT_IN_LOOP_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.PLOT_IN_LOOP_KEY,
                    "Plotting in loops is very slow. Collect data first, then plot once.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping plot in loop detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== BEST PRACTICES RULES =====

    /**
     * Detect generic variable names.
     */
    public void detectGenericVariableNames(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GENERIC_VARIABLE_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.GENERIC_VARIABLE_NAMES_KEY,
                    "Use meaningful variable names instead of generic names like 'temp', 'data', etc.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping generic variable names detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect public functions without usage messages.
     */
    public void detectMissingUsageMessage(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PUBLIC_FUNCTION_PATTERN.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);

                // Check if ::usage exists for this function
                if (!content.contains(functionName + "::usage")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_USAGE_MESSAGE_KEY,
                        String.format("Public function '%s' should have ::usage documentation.", functionName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing usage message detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect functions with many optional parameters that should use OptionsPattern.
     */
    public void detectMissingOptionsPattern(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MANY_OPTIONAL_PARAMS_PATTERN.matcher(content);
            while (matcher.find()) {
                if (!matcher.group(0).contains("OptionsPattern")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_OPTIONS_PATTERN_KEY,
                        "Functions with 3+ optional parameters should use OptionsPattern.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing OptionsPattern detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect functions with side effects that have unclear names.
     */
    public void detectSideEffectsNaming(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GLOBAL_ASSIGNMENT_PATTERN.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);

                // Check if name indicates side effects
                if (!functionName.matches("(?i).*(?:set|update|modify|change|clear|reset).*") &&
                    !functionName.endsWith("!")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.SIDE_EFFECTS_NAMING_KEY,
                        String.format("Function '%s' has side effects but name doesn't indicate this. " +
                            "Consider Set*/Update* prefix or ! suffix.", functionName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping side effects naming detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect complex boolean expressions.
     */
    public void detectComplexBoolean(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = COMPLEX_BOOLEAN_PATTERN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.COMPLEX_BOOLEAN_KEY,
                    "Boolean expression with 5+ operators should be broken into named conditions.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping complex boolean detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect unprotected public symbols.
     */
    public void detectUnprotectedSymbols(SensorContext context, InputFile inputFile, String content) {
        try {
            // OPTIMIZED: Use contains pre-check
            if (!content.contains("Protect")) {
                Matcher matcher = PUBLIC_FUNCTION_PATTERN.matcher(content);
                int publicFunctionCount = 0;
                while (matcher.find()) {
                    publicFunctionCount++;
                }

                if (publicFunctionCount > 0) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.UNPROTECTED_SYMBOLS_KEY,
                        "Public API symbols should use Protect[] to prevent accidental modification.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping unprotected symbols detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect complex functions without explicit Return statements.
     */
    public void detectMissingReturn(SensorContext context, InputFile inputFile, String content) {
        try {
            // OPTIMIZED: Use contains pre-check
            if (content.contains("If[") || content.contains("Which[")) {
                Matcher matcher = FUNCTION_WITH_IF_PATTERN.matcher(content);

                while (matcher.find()) {
                    String functionBody = content.substring(matcher.start(),
                        Math.min(matcher.start() + 500, content.length()));

                    if (!functionBody.contains("Return[")) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_RETURN_KEY,
                            "Complex function with conditionals should use explicit Return[] for clarity.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing return detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== PHASE 4: NEW CODE SMELL DETECTORS (18 + 10 performance = 28 methods) =====

    /**
     * Detect overly complex pattern definitions.
     */
    public void detectOvercomplexPatterns(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect patterns with more than 5 alternatives (|)
            Matcher matcher = OVERCOMPLEX_PATTERN_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                String patternDef = matcher.group(2);
                int alternativeCount = patternDef.split("\\|").length;
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.OVERCOMPLEX_PATTERNS_KEY,
                    String.format("Pattern has %d alternatives (max 5 recommended).", alternativeCount));
            }
        } catch (Exception e) {
            LOG.warn("Skipping overcomplex patterns detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect inconsistent use of Rule (->) and RuleDelayed (:>).
     */
    public void detectInconsistentRuleTypes(SensorContext context, InputFile inputFile, String content) {
        try {
            // Look for {} or <||> containing mixed -> and :>
            Matcher matcher = MIXED_RULE_TYPES_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.INCONSISTENT_RULE_TYPES_KEY,
                    "Mixing Rule (->) and RuleDelayed (:>) in same list is confusing.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping inconsistent rule types detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing function attributes.
     */
    public void detectMissingFunctionAttributes(SensorContext context, InputFile inputFile, String content) {
        try {
            // Check if there are public functions but no SetAttributes calls
            if (!content.contains("SetAttributes")) {
                Matcher matcher = PUBLIC_FUNCTION_PATTERN.matcher(content);
                int count = 0;
                while (matcher.find() && count < 5) {  // Report once if multiple functions
                    count++;
                }
                if (count > 0) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_FUNCTION_ATTRIBUTES_KEY,
                        "Public functions should consider using attributes (Listable, Protected, etc.).");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing function attributes detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing documentation for complex pattern-based functions.
     */
    public void detectMissingDownValuesDoc(SensorContext context, InputFile inputFile, String content) {
        try {
            // Count functions with multiple definitions (same name appearing multiple times with patterns)
            if (!content.contains("::usage")) {
                Matcher matcher = DOWNVALUES_FUNC_PATTERN.matcher(content);
                Map<String, Integer> funcCounts = new java.util.HashMap<>();

                while (matcher.find()) {
                    String funcName = matcher.group(1);
                    funcCounts.put(funcName, funcCounts.getOrDefault(funcName, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : funcCounts.entrySet()) {
                    if (entry.getValue() >= 3) {
                        reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_DOWNVALUES_DOC_KEY,
                            String.format("Function '%s' has %d pattern definitions but no ::usage message.",
                                entry.getKey(), entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing DownValues documentation detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect functions accepting any input without pattern tests.
     */
    public void detectMissingPatternTestValidation(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find functions with generic patterns (x_) but no pattern test (?...Q)
            Matcher matcher = PATTERN_TEST_FUNC_PATTERN.matcher(content);

            while (matcher.find()) {
                String params = matcher.group(2);
                // Check if parameters have pattern tests
                if (!params.contains("?") && !params.contains("_Integer") && !params.contains("_Real") &&
                    !params.contains("_String") && !params.contains("_List")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_PATTERN_TEST_VALIDATION_KEY,
                        String.format("Function '%s' should validate input types with pattern tests (?NumericQ, ?ListQ, etc.).",
                            matcher.group(1)));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing pattern test validation detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect excessive use of pure functions (#, #2, #3).
     */
    public void detectExcessivePureFunctions(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find pure functions with #1, #2, #3 and complex expressions
            Matcher matcher = PURE_FUNC_COMPLEX_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.EXCESSIVE_PURE_FUNCTIONS_KEY,
                    "Complex pure function with multiple # slots should use Function[{x, y, z}, ...] for clarity.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping excessive pure functions detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing operator precedence clarity.
     */
    public void detectMissingOperatorPrecedence(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find expressions with mixed /@, @@, //@ without parentheses
            Matcher matcher = OPERATOR_PRECEDENCE_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_OPERATOR_PRECEDENCE_KEY,
                    "Complex operator expression should use parentheses for clarity.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing operator precedence detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect hardcoded file paths.
     */
    public void detectHardcodedFilePaths(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find absolute paths
            Matcher winMatcher = WINDOWS_PATH_PATTERN.matcher(content);
            while (winMatcher.find()) {
                int line = calculateLineNumber(content, winMatcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.HARDCODED_FILE_PATHS_KEY,
                    "Use FileNameJoin and $HomeDirectory instead of hardcoded paths.");
            }

            Matcher unixMatcher = UNIX_PATH_PATTERN.matcher(content);
            while (unixMatcher.find()) {
                int line = calculateLineNumber(content, unixMatcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.HARDCODED_FILE_PATHS_KEY,
                    "Use FileNameJoin and $HomeDirectory instead of hardcoded paths.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping hardcoded file paths detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect inconsistent return types (heuristic-based).
     */
    public void detectInconsistentReturnTypes(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find functions with same name but different return patterns
            Matcher matcher = RETURN_TYPE_PATTERN.matcher(content);
            Map<String, Set<String>> funcReturns = new java.util.HashMap<>();

            while (matcher.find()) {
                String funcName = matcher.group(1);
                String returnType = matcher.group(2).equals("{") ? "List" : "Association";
                funcReturns.computeIfAbsent(funcName, k -> new HashSet<>()).add(returnType);
            }

            for (Map.Entry<String, Set<String>> entry : funcReturns.entrySet()) {
                if (entry.getValue().size() > 1) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.INCONSISTENT_RETURN_TYPES_KEY,
                        String.format("Function '%s' returns inconsistent types: %s",
                            entry.getKey(), entry.getValue()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping inconsistent return types detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing error messages.
     */
    public void detectMissingErrorMessages(SensorContext context, InputFile inputFile, String content) {
        try {
            if (!content.contains("::") || !content.contains("Message[")) {
                // Has public functions but no error messages
                Matcher matcher = PUBLIC_FUNCTION_PATTERN.matcher(content);
                if (matcher.find()) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_ERROR_MESSAGES_KEY,
                        "Custom functions should define error messages for better usability.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing error messages detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect global state modification without naming convention.
     */
    public void detectGlobalStateModification(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find functions that assign to variables outside their parameters but don't end with !
            Matcher matcher = GLOBAL_MODIFY_PATTERN.matcher(content);

            while (matcher.find()) {
                String funcName = matcher.group(1);
                if (!funcName.endsWith("!") && !funcName.contains("Set") && !funcName.contains("Update")) {
                    String snippet = content.substring(matcher.start(), Math.min(matcher.end() + 50, content.length()));
                    if (snippet.contains("=") && !snippet.contains("Module[") && !snippet.contains("Block[")) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line, MathematicaRulesDefinition.GLOBAL_STATE_MODIFICATION_KEY,
                            String.format("Function '%s' modifies state but lacks ! suffix naming convention.", funcName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping global state modification detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing localization in dynamic interfaces.
     */
    public void detectMissingLocalization(SensorContext context, InputFile inputFile, String content) {
        try {
            if (content.contains("Manipulate[") && !content.contains("LocalizeVariables")) {
                Matcher matcher = MANIPULATE_PATTERN.matcher(content);
                while (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_LOCALIZATION_KEY,
                        "Manipulate should consider using LocalizeVariables to prevent variable leakage.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing localization detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect explicit Global` context usage.
     */
    public void detectExplicitGlobalContext(SensorContext context, InputFile inputFile, String content) {
        try {
            if (content.contains("Global`")) {
                Matcher matcher = GLOBAL_CONTEXT_PATTERN.matcher(content);
                while (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.EXPLICIT_GLOBAL_CONTEXT_KEY,
                        "Using Global` explicitly is a code smell indicating namespace confusion.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping explicit global context detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing temporary file cleanup.
     */
    public void detectMissingTemporaryCleanup(SensorContext context, InputFile inputFile, String content) {
        try {
            if ((content.contains("CreateFile[") || content.contains("CreateDirectory[")) &&
                !content.contains("DeleteFile") && !content.contains("DeleteDirectory")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_TEMPORARY_CLEANUP_KEY,
                    "Temporary files/directories should be cleaned up or use auto-deletion.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing temporary cleanup detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect nested lists where Association would be clearer.
     */
    public void detectNestedListsInsteadAssociation(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find repeated indexed access patterns like data[[1]], data[[5]], data[[7]]
            Matcher matcher = PART_ACCESS_PATTERN.matcher(content);
            Map<String, Set<Integer>> indexAccess = new java.util.HashMap<>();

            while (matcher.find()) {
                String varName = matcher.group(1);
                int index = Integer.parseInt(matcher.group(2));
                indexAccess.computeIfAbsent(varName, k -> new HashSet<>()).add(index);
            }

            for (Map.Entry<String, Set<Integer>> entry : indexAccess.entrySet()) {
                if (entry.getValue().size() >= 3) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.NESTED_LISTS_INSTEAD_ASSOCIATION_KEY,
                        String.format("Variable '%s' accessed by multiple indices (%d times) - consider using Association.",
                            entry.getKey(), entry.getValue().size()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping nested lists detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect repeated Part extractions.
     */
    public void detectRepeatedPartExtraction(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find multiple consecutive [[n]] accesses
            Matcher matcher = REPEATED_PART_PATTERN.matcher(content);

            while (matcher.find()) {
                if (matcher.group(1).equals(matcher.group(2)) && matcher.group(2).equals(matcher.group(3))) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.REPEATED_PART_EXTRACTION_KEY,
                        "Multiple Part extractions should use destructuring for clarity.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping repeated Part extraction detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing memoization (simple heuristic).
     */
    public void detectMissingMemoization(SensorContext context, InputFile inputFile, String content) {
        try {
            // Look for recursive functions without memoization pattern f[x_] := f[x] = ...
            Matcher matcher = RECURSIVE_FUNC_PATTERN.matcher(content);

            while (matcher.find()) {
                String snippet = content.substring(matcher.start(), Math.min(matcher.end() + 100, content.length()));
                if (!snippet.matches(".*:=.*=.*")) {  // Check for memoization pattern
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_MEMOIZATION_KEY,
                        String.format("Recursive function '%s' should consider memoization for performance.", matcher.group(1)));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing memoization detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect StringJoin used for templates.
     */
    public void detectStringJoinForTemplates(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find multiple <> operations in one expression
            Matcher matcher = STRINGJOIN_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.STRINGJOIN_FOR_TEMPLATES_KEY,
                    "Multiple StringJoin operations should use StringTemplate for readability.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping StringJoin for templates detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== PERFORMANCE DETECTION METHODS (10 methods) =====

    /**
     * Detect linear search when lookup table would be better.
     */
    public void detectLinearSearchInsteadLookup(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Select[list, #[[...]] == ... &] patterns
            Matcher matcher = SELECT_LINEAR_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.LINEAR_SEARCH_INSTEAD_LOOKUP_KEY,
                    "Use Association or Dispatch for O(1) lookup instead of Select (O(n) linear search).");
            }
        } catch (Exception e) {
            LOG.warn("Skipping linear search detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect repeated expensive calculations in loops.
     */
    public void detectRepeatedCalculations(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Do/Table/While with function calls that don't depend on loop variable
            Matcher matcher = REPEATED_CALC_PATTERN.matcher(content);

            while (matcher.find()) {
                String funcCall = matcher.group(1);
                String loopVar = matcher.group(2);
                // Check if function call doesn't contain loop variable
                if (!funcCall.contains(loopVar)) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.REPEATED_CALCULATIONS_KEY,
                        "Expensive expression calculated repeatedly in loop should be hoisted out.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping repeated calculations detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect Position used instead of pattern matching.
     */
    public void detectPositionInsteadPattern(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Position followed by Extract/Part
            if (content.contains("Position[") && (content.contains("Extract[") || content.contains("Part["))) {
                Matcher matcher = POSITION_PATTERN.matcher(content);
                while (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.POSITION_INSTEAD_PATTERN_KEY,
                        "Consider using Cases or Select with pattern matching instead of Position + Extract.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Position detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect Flatten[Table[...]] antipattern.
     */
    public void detectFlattenTableAntipattern(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FLATTEN_TABLE_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.FLATTEN_TABLE_ANTIPATTERN_KEY,
                    "Use Catenate, Join, or vectorization instead of Flatten[Table[...]].");
            }
        } catch (Exception e) {
            LOG.warn("Skipping Flatten Table antipattern detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing parallelization opportunities.
     */
    public void detectMissingParallelization(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find large Table operations without Parallel
            if (!content.contains("Parallel")) {
                Matcher matcher = LARGE_TABLE_PATTERN.matcher(content);
                while (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_PARALLELIZATION_KEY,
                        "Large independent iterations should use ParallelTable or ParallelMap.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing parallelization detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing SparseArray usage.
     */
    public void detectMissingSparseArray(SensorContext context, InputFile inputFile, String content) {
        try {
            // Heuristic: large array initialization with mostly zeros
            Matcher matcher = ZERO_TABLE_PATTERN.matcher(content);

            while (matcher.find()) {
                int size = Integer.parseInt(matcher.group(1));
                if (size > 100) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_SPARSE_ARRAY_KEY,
                        "Large arrays with many zeros should use SparseArray for efficiency.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing SparseArray detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect unnecessary Transpose operations.
     */
    public void detectUnnecessaryTranspose(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Transpose[...Transpose[...]]
            Matcher matcher = DOUBLE_TRANSPOSE_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.UNNECESSARY_TRANSPOSE_KEY,
                    "Repeated Transpose operations detected - work consistently row-wise or column-wise.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping unnecessary Transpose detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect DeleteDuplicates on large data.
     */
    public void detectDeleteDuplicatesOnLargeData(SensorContext context, InputFile inputFile, String content) {
        try {
            if (content.contains("DeleteDuplicates[")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.DELETEDUPS_ON_LARGE_DATA_KEY,
                    "For large lists, consider using Keys@GroupBy[list, Identity] instead of DeleteDuplicates.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping DeleteDuplicates detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect repeated string parsing.
     */
    public void detectRepeatedStringParsing(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find ToExpression in loops
            Matcher matcher = TOEXPRESSION_LOOP_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.REPEATED_STRING_PARSING_KEY,
                    "Parsing the same string repeatedly in loop - cache the result.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping repeated string parsing detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing CompilationTarget in Compile.
     */
    public void detectMissingCompilationTarget(SensorContext context, InputFile inputFile, String content) {
        try {
            if (content.contains("Compile[") && !content.contains("CompilationTarget")) {
                Matcher matcher = COMPILE_PATTERN.matcher(content);
                while (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_COMPILATION_TARGET_KEY,
                        "Compile should use CompilationTarget->\"C\" for 10-100x speedup.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing compilation target detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Checks if a number is part of an association mapping (enum-like pattern).
     * Examples: <| Location -> 1, Boundary -> 2 |>
     * These are structural indices, not magic numbers.
     */
    private boolean isAssociationMapping(String content, int numberPosition) {
        // Look back up to 50 characters for the -> operator
        int lookbackStart = Math.max(0, numberPosition - 50);
        String lookback = content.substring(lookbackStart, numberPosition);

        // Check if there's a -> followed by whitespace before this number
        // This catches patterns like "Key -> 1" or "PropertyName -> 42"
        return lookback.matches(".*->\\s*$");
    }
}
