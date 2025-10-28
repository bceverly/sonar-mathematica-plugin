package org.sonar.plugins.mathematica.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * Detector for Bug/Reliability rules (20 rules total).
 * Handles runtime errors, logic bugs, and correctness issues.
 */
public class BugDetector extends BaseDetector {

    // ===== PATTERNS FOR BUG DETECTION =====

    private static final Pattern DIVISION_PATTERN = Pattern.compile("/(?!=)");  // Not //= or /=
    private static final Pattern ASSIGNMENT_IN_IF_PATTERN = Pattern.compile(
        "(?:If|While|Which)\\s*\\[[^\\]]*\\b(\\w+)\\s*=\\s*(?!=)[^=]"
    );
    private static final Pattern LIST_ACCESS_PATTERN = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final Pattern RECURSIVE_FUNCTION_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[[^\\]]*\\]\\s*:="
    );
    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[([^\\]]*)\\]\\s*:=",
        Pattern.MULTILINE
    );

    // Phase 2 Bug patterns
    private static final Pattern FLOAT_EQUALITY_PATTERN = Pattern.compile(
        "\\d+\\.\\d+\\s*===?\\s*\\d+\\.\\d+|===?\\s*\\d+\\.\\d+"
    );
    private static final Pattern FUNCTION_END_SEMICOLON_PATTERN = Pattern.compile(
        "\\]\\s*:=\\s*\\([^)]*;\\s*\\)"
    );
    private static final Pattern MODULE_BLOCK_WITH_PATTERN = Pattern.compile(
        "(?:Module|Block|With)\\s*\\[\\s*\\{([^}]+)\\}"
    );
    private static final Pattern WHILE_TRUE_PATTERN = Pattern.compile("While\\s*\\[\\s*True\\s*,");
    private static final Pattern MATRIX_OPERATION_PATTERN = Pattern.compile("(?:Transpose|Dot)\\s*\\[");
    private static final Pattern STRING_PLUS_NUMBER_PATTERN = Pattern.compile(
        "\"[^\"]*\"\\s*\\+\\s*\\d+|\\d+\\s*\\+\\s*\"[^\"]*\""
    );
    private static final Pattern TRIPLE_UNDERSCORE_PATTERN = Pattern.compile("\\w+\\[___\\]");

    // Phase 3 Pattern matching patterns
    private static final Pattern FUNCTION_WITHOUT_TEST_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[\\s*([a-z]\\w*)_\\s*\\]\\s*:=\\s*[^;]*(?:Sqrt|Log|Sin|Cos|Exp|Power|N)\\["
    );
    private static final Pattern DOUBLE_UNDERSCORE_PATTERN = Pattern.compile(
        "([a-z]\\w*)__\\s*\\]\\s*:=\\s*[^;]*Length\\s*\\[\\s*\\1\\s*\\]"
    );
    private static final Pattern SET_FUNCTION_DEFINITION_PATTERN = Pattern.compile(
        "([a-zA-Z]\\w*)\\s*\\[\\s*\\w+_[^\\]]*\\]\\s*=(?!=)"
    );
    private static final Pattern BUILTIN_SHADOW_PATTERN = Pattern.compile(
        "\\b([NDICEKOPABSLMX]|Pi|Re|Im|Abs|Min|Max|Log|Sin|Cos|Tan|Exp)\\s*(?:\\[\\w+_[^\\]]*\\]\\s*:?=|=(?!=))"
    );

    // Phase 3 Resource management patterns
    private static final Pattern OPEN_FILE_PATTERN = Pattern.compile("(?:OpenRead|OpenWrite|OpenAppend)\\s*\\[");
    private static final Pattern DEFINITION_IN_LOOP_PATTERN = Pattern.compile(
        "(?:Do|While|For)\\s*\\[[^\\]]*?\\w+\\[[^\\]]+\\]\\s*="
    );

    // Phase 4 Bug patterns (optimized - pre-compiled for performance)
    private static final Pattern OFF_BY_ONE_PATTERN = Pattern.compile(
        "Do\\s*\\[[^,]+,\\s*\\{\\s*\\w+,\\s*(0|Length\\[[^\\]]+\\]\\s*\\+\\s*1)"
    );
    private static final Pattern BLOCK_WITH_ASSIGNMENT_PATTERN = Pattern.compile("Block\\s*\\[\\s*\\{[^}]*=");
    private static final Pattern FIRST_LAST_PATTERN = Pattern.compile("(?:First|Last)\\s*\\[([a-zA-Z]\\w*)\\]");
    private static final Pattern SYMBOLIC_WITH_FLOAT_PATTERN = Pattern.compile("(?:Solve|DSolve|Integrate|Limit)\\s*\\[[^\\]]*\\d+\\.\\d+");
    private static final Pattern ASSIGNMENT_FROM_IMPORT_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*=\\s*(?:Import|Get|URLFetch)\\s*\\[");
    private static final Pattern DIVISION_VARIABLES_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*/\\s*([a-zA-Z]\\w*)");
    private static final Pattern DOT_OPERATION_PATTERN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\.\\s*([a-zA-Z]\\w*)");
    private static final Pattern SCOPING_WITH_ASSIGNMENT_PATTERN = Pattern.compile("(?:Module|Block)\\s*\\[\\s*\\{[^}]*=");
    private static final Pattern HOLD_ATTR_PATTERN = Pattern.compile("\\{[^}]*\\+\\+[^}]*\\+\\+[^}]*\\}");
    private static final Pattern LEVEL_SPEC_PATTERN = Pattern.compile("(?:Map|Apply|Cases)\\s*\\[[^,]+,\\s*[^,]+,\\s*\\{-?\\d+\\}");
    private static final Pattern LOOP_WITH_MUTATION_PATTERN = Pattern.compile("(?:Do|While|For)\\s*\\[[^\\[]*(?:Append|Prepend|Delete)\\s*\\[");
    private static final Pattern FUNCTION_DEF_GENERAL_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*\\[[^\\]]*\\]\\s*:=");
    private static final Pattern ASSOCIATION_JOIN_PATTERN = Pattern.compile("Join\\s*\\[\\s*<\\|");
    private static final Pattern DATE_OBJECT_PATTERN = Pattern.compile("DateObject\\s*\\[\\s*\\{(\\d+),\\s*(\\d+),\\s*(\\d+)");
    private static final Pattern STATS_ON_VAR_PATTERN = Pattern.compile("(?:Mean|Total|StandardDeviation)\\s*\\[([a-zA-Z]\\w*)\\]");
    private static final Pattern QUANTITY_MISMATCH_PATTERN = Pattern.compile("Quantity\\[\\d+,\\s*\"([^\"]+)\"\\]\\s*[+\\-]\\s*Quantity\\[\\d+,\\s*\"([^\"]+)\"\\]");

    /**
     * Detect potential division by zero.
     */
    public void detectDivisionByZero(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DIVISION_PATTERN.matcher(content);

            while (matcher.find()) {
                int position = matcher.start();

                // Skip URLs (http://, https://)
                if (position > 0 && content.charAt(position - 1) == ':') {
                    if (position + 1 < content.length() && content.charAt(position + 1) == '/') {
                        continue;
                    }
                }

                // Skip if inside a string literal
                if (isInsideStringLiteral(content, position)) {
                    continue;
                }

                // Check if there's validation
                int lineStart = content.lastIndexOf('\n', position) + 1;
                int lineEnd = content.indexOf('\n', position);
                if (lineEnd == -1) lineEnd = content.length();
                String line = content.substring(lineStart, lineEnd);

                if (line.contains("Check[") || line.contains("!= 0") || line.contains("> 0")) {
                    continue;
                }

                int lineNumber = calculateLineNumber(content, position);
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.DIVISION_BY_ZERO_KEY,
                    "Ensure the divisor cannot be zero before performing division.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping division by zero detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect assignment in conditionals.
     */
    public void detectAssignmentInConditional(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSIGNMENT_IN_IF_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITIONAL_KEY,
                    "Use comparison (== or ===) instead of assignment (=) in this conditional.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping assignment in conditional detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect list index access without bounds checking.
     */
    public void detectListIndexOutOfBounds(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = LIST_ACCESS_PATTERN.matcher(content);

            while (matcher.find()) {
                int position = matcher.start();

                // Check if bounds are validated
                int lineStart = content.lastIndexOf('\n', position) + 1;
                int lineEnd = content.indexOf('\n', position);
                if (lineEnd == -1) lineEnd = content.length();
                String line = content.substring(lineStart, lineEnd);

                if (line.contains("Length[") || line.contains("Check[") ||
                    line.contains("<= Length") || line.contains("If[Length")) {
                    continue;
                }

                int lineNumber = calculateLineNumber(content, position);
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.LIST_INDEX_OUT_OF_BOUNDS_KEY,
                    "Verify the index is within bounds before accessing list elements.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping list index bounds detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect recursive functions without proper base cases.
     */
    public void detectInfiniteRecursion(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher defMatcher = RECURSIVE_FUNCTION_PATTERN.matcher(content);

            while (defMatcher.find()) {
                String functionName = defMatcher.group(1);
                int defStart = defMatcher.start();

                // Look for recursion
                int bodyEnd = content.indexOf(";", defStart);
                if (bodyEnd == -1) bodyEnd = content.length();
                int nextDef = content.indexOf(functionName + "[", defStart + functionName.length());

                if (nextDef > 0 && nextDef < bodyEnd) {
                    // Check for base case
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
                        int lineNumber = calculateLineNumber(content, defStart);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.INFINITE_RECURSION_KEY,
                            String.format("Function '%s' appears to be recursive but may lack a base case.", functionName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping infinite recursion detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect unreachable pattern definitions.
     */
    public void detectUnreachablePatterns(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
            Map<String, List<PatternInfo>> functionPatterns = new HashMap<>();

            while (matcher.find()) {
                String funcName = matcher.group(1);
                String pattern = matcher.group(2);
                int lineNumber = calculateLineNumber(content, matcher.start());

                functionPatterns.computeIfAbsent(funcName, k -> new java.util.ArrayList<>())
                    .add(new PatternInfo(pattern, lineNumber));
            }

            // Check each function's patterns
            for (Map.Entry<String, List<PatternInfo>> entry : functionPatterns.entrySet()) {
                List<PatternInfo> patterns = entry.getValue();
                if (patterns.size() < 2) continue;

                // Check if general pattern comes before specific patterns
                for (int i = 0; i < patterns.size() - 1; i++) {
                    String currentPattern = patterns.get(i).pattern;
                    if (currentPattern.matches("\\w+_\\s*")) {
                        for (int j = i + 1; j < patterns.size(); j++) {
                            String laterPattern = patterns.get(j).pattern;
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
     * Detect floating point equality comparisons.
     */
    public void detectFloatingPointEquality(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FLOAT_EQUALITY_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.FLOATING_POINT_EQUALITY_KEY,
                    "Floating point numbers should not be compared with == or ===. Use tolerance-based comparison.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping floating point equality detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect functions that end with semicolon and return Null.
     */
    public void detectFunctionWithoutReturn(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_END_SEMICOLON_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.FUNCTION_WITHOUT_RETURN_KEY,
                    "Function body ends with semicolon and returns Null. Remove semicolon to return value.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping function without return detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect variables used before assignment.
     */
    public void detectVariableBeforeAssignment(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher moduleMatcher = MODULE_BLOCK_WITH_PATTERN.matcher(content);
            while (moduleMatcher.find()) {
                String varList = moduleMatcher.group(1);
                String[] vars = varList.split(",");
                java.util.Set<String> declaredVars = new java.util.HashSet<>();

                for (String var : vars) {
                    declaredVars.add(var.trim().split("\\s|=")[0]);
                }

                int bodyStart = moduleMatcher.end();
                int bodyEnd = content.indexOf("];", bodyStart);
                if (bodyEnd == -1) continue;

                String body = content.substring(bodyStart, bodyEnd);
                String[] statements = body.split(";");
                java.util.Set<String> assigned = new java.util.HashSet<>();

                for (String stmt : statements) {
                    for (String var : declaredVars) {
                        // Escape variable name for regex to handle special characters like $
                        String escapedVar = Pattern.quote(var);
                        try {
                            if (!assigned.contains(var) && stmt.matches(".*\\b" + escapedVar + "\\b.*") &&
                                !stmt.matches(".*\\b" + escapedVar + "\\s*=.*")) {
                                int lineNumber = calculateLineNumber(content, bodyStart);
                                reportIssue(context, inputFile, lineNumber,
                                    MathematicaRulesDefinition.VARIABLE_BEFORE_ASSIGNMENT_KEY,
                                    String.format("Variable '%s' may be used before assignment.", var));
                            }
                            if (stmt.matches(".*\\b" + escapedVar + "\\s*=.*")) {
                                assigned.add(var);
                            }
                        } catch (Exception e) {
                            // Skip this variable if regex matching fails
                            LOG.debug("Skipping variable '{}' in pattern matching", var);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping variable before assignment detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect off-by-one errors in loops.
     */
    public void detectOffByOne(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = OFF_BY_ONE_PATTERN.matcher(content);

            while (matcher.find()) {
                String range = matcher.group(1);
                String message = range.equals("0") ?
                    "Loop starts at 0 but Mathematica lists are 1-indexed." :
                    "Loop goes beyond Length, causing out-of-bounds access.";
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.OFF_BY_ONE_KEY, message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping off-by-one detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect infinite loops (While[True] without Break).
     */
    public void detectInfiniteLoop(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = WHILE_TRUE_PATTERN.matcher(content);
            while (matcher.find()) {
                int bodyStart = matcher.end();
                int bodyEnd = content.indexOf("]", bodyStart);
                if (bodyEnd == -1) continue;

                String body = content.substring(bodyStart, bodyEnd);
                if (!body.contains("Break") && !body.contains("Return")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.INFINITE_LOOP_KEY,
                        "While[True] without Break or Return creates infinite loop.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping infinite loop detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect potential mismatched matrix dimensions.
     */
    public void detectMismatchedDimensions(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MATRIX_OPERATION_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.MISMATCHED_DIMENSIONS_KEY,
                    "Review: Matrix operation requires rectangular array. Verify dimensions match.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping mismatched dimensions detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect type mismatches (string + number).
     */
    public void detectTypeMismatch(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = STRING_PLUS_NUMBER_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TYPE_MISMATCH_KEY,
                    "Type mismatch: Cannot add string and number. Use <> for concatenation.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping type mismatch detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect suspicious pattern usage (___ matches zero or more).
     */
    public void detectSuspiciousPattern(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = TRIPLE_UNDERSCORE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.SUSPICIOUS_PATTERN_KEY,
                    "Pattern uses ___ which matches zero or more arguments. Consider __ (one or more) instead.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping suspicious pattern detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== PHASE 3 PATTERN MATCHING BUGS =====

    /**
     * Detect missing pattern tests on numeric functions.
     */
    public void detectMissingPatternTest(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_WITHOUT_TEST_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.MISSING_PATTERN_TEST_KEY,
                    "Numeric function should use pattern test (e.g., x_?NumericQ) to prevent symbolic evaluation.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing pattern test detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect misuse of pattern blanks (__ for sequences vs lists).
     */
    public void detectPatternBlanksMisuse(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DOUBLE_UNDERSCORE_PATTERN.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PATTERN_BLANKS_MISUSE_KEY,
                    String.format("Using __ creates sequence, not list. Use Length[{%s}] or %s_List.", varName, varName));
            }
        } catch (Exception e) {
            LOG.warn("Skipping pattern blanks misuse detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect Set (=) vs SetDelayed (:=) confusion.
     */
    public void detectSetDelayedConfusion(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SET_FUNCTION_DEFINITION_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.SET_DELAYED_CONFUSION_KEY,
                    "Function definition uses = instead of :=. RHS evaluates once, not each call. Use := for functions.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping set/delayed confusion detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect symbol name collisions with built-ins.
     */
    public void detectSymbolNameCollision(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = BUILTIN_SHADOW_PATTERN.matcher(content);
            while (matcher.find()) {
                String symbolName = matcher.group(1);
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.SYMBOL_NAME_COLLISION_KEY,
                    String.format("Symbol '%s' shadows Mathematica built-in function. Use different name.", symbolName));
            }
        } catch (Exception e) {
            LOG.warn("Skipping symbol name collision detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect Block vs Module misuse.
     */
    public void detectBlockModuleMisuse(SensorContext context, InputFile inputFile, String content) {
        try {
            // OPTIMIZED: Use contains pre-check
            if (content.contains("Block[{") && content.contains("=")) {
                Matcher matcher = BLOCK_WITH_ASSIGNMENT_PATTERN.matcher(content);
                if (matcher.find()) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.BLOCK_MODULE_MISUSE_KEY,
                        "Consider if Module (lexical scope) is more appropriate than Block (dynamic scope).");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Block/Module misuse detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== PHASE 3 RESOURCE MANAGEMENT BUGS =====

    /**
     * Detect unclosed file handles.
     */
    public void detectUnclosedFileHandle(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = OPEN_FILE_PATTERN.matcher(content);
            while (matcher.find()) {
                // Check if Close[] is present nearby
                int start = Math.max(0, matcher.start() - 100);
                int end = Math.min(content.length(), matcher.end() + 500);
                String contextWindow = content.substring(start, end);

                if (!contextWindow.contains("Close[")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.UNCLOSED_FILE_HANDLE_KEY,
                        "File opened but Close[] not found. Ensure file handle is closed to prevent resource leak.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping unclosed file handle detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect growing definition chains (memory leak).
     */
    public void detectGrowingDefinitionChain(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DEFINITION_IN_LOOP_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.GROWING_DEFINITION_CHAIN_KEY,
                    "Function redefined in loop creates growing definition chain (memory leak). Clear definitions or restructure.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping growing definition chain detection due to error in file: {}", inputFile.filename());
        }
    }

    // ===== PHASE 4: NEW BUG DETECTORS (15 methods) =====

    /**
     * Detect missing empty list checks before First/Last/Part.
     */
    public void detectMissingEmptyListCheck(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find First[, Last[, Part[ without prior Length check
            Matcher matcher = FIRST_LAST_PATTERN.matcher(content);

            while (matcher.find()) {
                String varName = matcher.group(1);
                int pos = matcher.start();
                // Look back for Length check
                String lookback = content.substring(Math.max(0, pos - 200), pos);
                if (!lookback.contains("Length[" + varName + "]") && !lookback.contains("!= {}") && !lookback.contains("=!= {}")) {
                    int line = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_EMPTY_LIST_CHECK_KEY,
                        String.format("Using First/Last on '%s' without checking for empty list.", varName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing empty list check detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect machine precision in symbolic calculations.
     */
    public void detectMachinePrecisionInSymbolic(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Solve, DSolve, etc. with decimal numbers
            Matcher matcher = SYMBOLIC_WITH_FLOAT_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.MACHINE_PRECISION_IN_SYMBOLIC_KEY,
                    "Machine precision float in symbolic calculation - use exact rationals instead.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping machine precision detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing $Failed checks after Import/Get/URLFetch.
     */
    public void detectMissingFailedCheck(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Import/Get followed by usage without $Failed check
            Matcher matcher = ASSIGNMENT_FROM_IMPORT_PATTERN.matcher(content);

            while (matcher.find()) {
                String varName = matcher.group(1);
                int pos = matcher.start();
                // Look ahead for $Failed check
                String lookahead = content.substring(pos, Math.min(pos + 300, content.length()));
                if (!lookahead.contains("=== $Failed") && !lookahead.contains("FailureQ[" + varName)) {
                    int line = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_FAILED_CHECK_KEY,
                        String.format("Variable '%s' from Import/Get/URLFetch used without $Failed check.", varName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing $Failed check detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect potential zero denominators.
     */
    public void detectZeroDenominator(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find division by variables without guards
            Matcher matcher = DIVISION_VARIABLES_PATTERN.matcher(content);

            while (matcher.find()) {
                String denominator = matcher.group(2);
                int pos = matcher.start();
                // Look back for zero check
                String lookback = content.substring(Math.max(0, pos - 150), pos);
                if (!lookback.contains(denominator + " != 0") && !lookback.contains(denominator + " > 0")) {
                    int line = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.ZERO_DENOMINATOR_KEY,
                        String.format("Division by '%s' without zero check may produce ComplexInfinity.", denominator));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping zero denominator detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing matrix dimension checks.
     */
    public void detectMissingMatrixDimensionCheck(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Dot operations without dimension validation
            Matcher matcher = DOT_OPERATION_PATTERN.matcher(content);

            while (matcher.find()) {
                String mat1 = matcher.group(1);
                String mat2 = matcher.group(2);
                int pos = matcher.start();
                String lookback = content.substring(Math.max(0, pos - 200), pos);
                if (!lookback.contains("Dimensions[" + mat1) && !lookback.contains("MatrixQ[")) {
                    int line = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_MATRIX_DIMENSION_CHECK_KEY,
                        String.format("Matrix multiplication %s.%s without dimension compatibility check.", mat1, mat2));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping matrix dimension check detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect incorrect Set in scoping constructs.
     */
    public void detectIncorrectSetInScoping(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Module[{x = ...}] pattern (should be Module[{x}, x = ...])
            Matcher matcher = SCOPING_WITH_ASSIGNMENT_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.INCORRECT_SET_IN_SCOPING_KEY,
                    "Assignment in Module/Block variable list causes immediate evaluation - use separate statement.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping incorrect Set in scoping detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing Hold attributes.
     */
    public void detectMissingHoldAttributes(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find functions that use Unevaluated but don't have Hold attributes
            if (content.contains("Unevaluated[") && !content.contains("SetAttributes") && !content.contains("HoldAll")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_HOLD_ATTRIBUTES_KEY,
                    "Functions using Unevaluated should have Hold attributes (HoldAll, HoldFirst, etc.).");
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing Hold attributes detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect reliance on evaluation order.
     */
    public void detectEvaluationOrderAssumption(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find {i++, i++, i++} patterns
            Matcher matcher = HOLD_ATTR_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.EVALUATION_ORDER_ASSUMPTION_KEY,
                    "Side effects in list construction have undefined evaluation order.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping evaluation order assumption detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect incorrect level specifications.
     */
    public void detectIncorrectLevelSpecification(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Map/Apply with explicit levels that might be wrong (heuristic)
            Matcher matcher = LEVEL_SPEC_PATTERN.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, MathematicaRulesDefinition.INCORRECT_LEVEL_SPECIFICATION_KEY,
                    "Verify level specification is correct - common source of silent failures.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping incorrect level specification detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect operations that unpack packed arrays.
     */
    public void detectUnpackingPackedArrays(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Append/Prepend/Delete on arrays in loops
            if (content.contains("PackedArray") || content.contains("Range[") || content.contains("Table[")) {
            Matcher matcher = LOOP_WITH_MUTATION_PATTERN.matcher(content);

                while (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.UNPACKING_PACKED_ARRAYS_KEY,
                        "Append/Delete in loop unpacks packed arrays causing 10-100x slowdown.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping unpacking packed arrays detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing special case handling.
     */
    public void detectMissingSpecialCaseHandling(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find functions without handling for 0, Infinity, etc.
            Matcher matcher = FUNCTION_DEF_GENERAL_PATTERN.matcher(content);

            while (matcher.find()) {
                String funcName = matcher.group(1);
                int pos = matcher.start();
                String funcBody = content.substring(pos, Math.min(pos + 500, content.length()));

                if (!funcBody.contains("Which[") && !funcBody.contains("Switch[") &&
                    !funcBody.contains("=== 0") && !funcBody.contains("=== Infinity")) {
                    int line = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.MISSING_SPECIAL_CASE_HANDLING_KEY,
                        String.format("Function '%s' may not handle special values (0, Infinity, Indeterminate).", funcName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing special case handling detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect incorrect Association operations.
     */
    public void detectIncorrectAssociationOperations(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Join on Associations (works differently than Lists)
            if (content.contains("<|") && content.contains("Join[")) {
                Matcher matcher = ASSOCIATION_JOIN_PATTERN.matcher(content);

                while (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.INCORRECT_ASSOCIATION_OPERATIONS_KEY,
                        "Join on Associations merges by key (not concatenates) - verify this is intended.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping incorrect Association operations detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect DateObject validation issues.
     */
    public void detectDateObjectValidation(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find DateObject with hardcoded dates
            Matcher matcher = DATE_OBJECT_PATTERN.matcher(content);

            while (matcher.find()) {
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                if (month > 12 || day > 31) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.DATEOBJECT_VALIDATION_KEY,
                        "Invalid date in DateObject - validate date components.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping DateObject validation detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect Total/Mean on non-numeric data.
     */
    public void detectTotalMeanOnNonNumeric(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Mean/Total without NumericQ checks
            Matcher matcher = STATS_ON_VAR_PATTERN.matcher(content);

            while (matcher.find()) {
                String varName = matcher.group(1);
                int pos = matcher.start();
                String lookback = content.substring(Math.max(0, pos - 150), pos);
                if (!lookback.contains("VectorQ[" + varName + ", NumericQ]") && !lookback.contains("NumericQ")) {
                    int line = calculateLineNumber(content, pos);
                    reportIssue(context, inputFile, line, MathematicaRulesDefinition.TOTAL_MEAN_ON_NON_NUMERIC_KEY,
                        String.format("Statistical function on '%s' without numeric validation.", varName));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Total/Mean detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect Quantity unit mismatches.
     */
    public void detectQuantityUnitMismatch(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find Quantity operations (basic heuristic)
            if (content.contains("Quantity[")) {
            Matcher matcher = QUANTITY_MISMATCH_PATTERN.matcher(content);

                while (matcher.find()) {
                    String unit1 = matcher.group(1);
                    String unit2 = matcher.group(2);
                    if (!unit1.equals(unit2)) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line, MathematicaRulesDefinition.QUANTITY_UNIT_MISMATCH_KEY,
                            String.format("Quantity operation with incompatible units: %s and %s.", unit1, unit2));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Quantity unit mismatch detection due to error in file: {}", inputFile.filename());
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
}
