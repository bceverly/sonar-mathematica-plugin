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
            Pattern simpleCheckPattern = Pattern.compile("Check\\s*\\[[^,]+,\\s*(?:\\$Failed|Null|None)\\s*\\]");
            Matcher matcher = simpleCheckPattern.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.EMPTY_CATCH_KEY,
                    "Empty error handling - consider logging or handling the error.");
            }

            Pattern quietPattern = Pattern.compile("Quiet\\s*\\[");
            matcher = quietPattern.matcher(content);
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
            Pattern ifPattern = Pattern.compile("If\\s*\\[([^\\[]+),\\s*([^,]+),\\s*([^\\]]+)\\]");
            Matcher matcher = ifPattern.matcher(content);

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
                Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[[^\\]]*\\]\\s*:=\\s*(?:Module|Block)?\\s*\\[[^\\]]*If\\[");
                Matcher matcher = pattern.matcher(content);

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
