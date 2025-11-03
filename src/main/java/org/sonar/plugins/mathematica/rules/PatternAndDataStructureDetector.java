package org.sonar.plugins.mathematica.rules;

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
 * Detector for Chunk 1 rules from ROADMAP_325.md (Items 16-50).
 *
 * Contains 35 rules across three categories:
 * - Pattern System Rules (Items 16-30): 15 rules
 * - List/Array Rules (Items 31-40): 10 rules
 * - Association Rules (Items 41-50): 10 rules
 */
public class PatternAndDataStructureDetector extends BaseDetector {

    // Pre-compiled patterns for Pattern System Rules
    private static final Pattern UNRESTRICTED_BLANK = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[([^\\]]*\\b\\w+_\\b[^\\]]*)\\]\\s*:?=");
    private static final Pattern PATTERN_TEST_CONDITION = Pattern.compile("\\b(\\w+)_\\s*/;\\s*(\\w+Q)\\[\\1\\]");
    private static final Pattern BLANKSEQUENCE = Pattern.compile("\\b\\w+__(?![a-zA-Z_])");
    private static final Pattern OPTIONAL_PATTERN = Pattern.compile("\\b(\\w+)_:(\\w+)");
    private static final Pattern PATTERN_NAME_USAGE = Pattern.compile("\\b(\\w+)_([a-zA-Z]\\w*)?");
    private static final Pattern PATTERN_ALTERNATIVES = Pattern.compile("(\\w+)_([a-zA-Z]\\w*)\\s*\\|\\s*\\1_");
    private static final Pattern PATTERN_TEST_PURE = Pattern.compile("\\w+_\\?\\(\\s*#");
    private static final Pattern OPTIONS_PATTERN_USAGE = Pattern.compile("\\b\\w+___(?!\\s*:?\\s*OptionsPattern)");
    private static final Pattern FUNCTION_DEFINITION = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[([^\\]]*)\\]\\s*:?=");
    private static final Pattern VERBATIM_USAGE = Pattern.compile("\\bVerbatim\\s*\\[");
    private static final Pattern HOLDPATTERN_USAGE = Pattern.compile("\\bHoldPattern\\s*\\[([^\\]]*)\\]");
    private static final Pattern LONGEST_SHORTEST = Pattern.compile("\\b(Longest|Shortest)\\s*\\[");
    private static final Pattern REPEATED_PATTERN_NAME = Pattern.compile("\\{\\s*(\\w+)_,\\s*\\1_\\s*\\}");
    // FIXED: Possessive quantifiers on all repeating groups prevent backtracking, avoiding DoS risk
    private static final Pattern ALTERNATIVES_COUNT = Pattern.compile("\\w++:\\s*+\\([^)]*+\\)");
    private static final Pattern CASES_LARGE_LIST = Pattern.compile("Cases\\s*\\[\\s*Range\\s*\\[\\s*(\\d+)\\s*\\]");

    // Pre-compiled patterns for List/Array Rules
    private static final Pattern LIST_INDEXING = Pattern.compile("(\\w+)\\s*\\[\\[\\s*(-?\\d+|\\w+)\\s*\\]\\]");
    private static final Pattern NEGATIVE_INDEX = Pattern.compile("\\[\\[\\s*-\\s*(\\d+|\\w+)\\s*\\]\\]");
    private static final Pattern PART_ASSIGNMENT = Pattern.compile("([^\\w]\\{[^}]+\\}|[A-Z]\\w*\\s*\\[[^\\]]+\\])\\s*\\[\\[");
    private static final Pattern JOIN_IN_LOOP = Pattern.compile("(Do|While|Table)\\s*\\[[^\\]]*Join\\s*\\[");
    private static final Pattern FLATTEN_USAGE = Pattern.compile("Flatten\\s*\\[\\s*\\{\\s*([^,}]++)(?:,\\s*([^,}]++))*+\\s*\\}\\s*\\]");
    private static final Pattern LENGTH_IN_DO = Pattern.compile("Do\\s*\\[[^,]*,\\s*\\{[^,]*,\\s*[^,]*,\\s*Length\\s*\\[");
    private static final Pattern REVERSE_TWICE = Pattern.compile("Reverse\\s*\\[\\s*Reverse\\s*\\[");
    private static final Pattern SORT_GREATER = Pattern.compile("Sort\\s*\\[\\s*\\w+\\s*,\\s*Greater\\s*\\]");
    private static final Pattern EXTRACT_POSITION = Pattern.compile("Extract\\s*\\[\\s*\\w+\\s*,\\s*Position\\s*\\[");
    private static final Pattern NESTED_PART = Pattern.compile("(\\w+)\\s*\\[\\[\\s*\\w+\\s*\\]\\]\\s*\\[\\[");

    // Pre-compiled patterns for Association Rules
    private static final Pattern ASSOC_ACCESS = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[\\s*\"[^\"]+\"\\s*\\]");
    private static final Pattern ASSOC_POSITIONAL = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[\\[\\s*\\d+\\s*\\]\\]");
    private static final Pattern SELECT_KEYS = Pattern.compile("Select\\s*\\[\\s*Keys\\s*\\[\\s*(\\w+)\\s*\\]");
    private static final Pattern QUERY_USAGE = Pattern.compile("Query\\s*\\[");
    private static final Pattern ASSOC_DIRECT_ASSIGN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[\\s*\"[^\"]+\"\\s*\\]\\s*=");
    private static final Pattern MERGE_USAGE = Pattern.compile("Merge\\s*\\[\\s*\\{[^}]+\\}\\s*\\](?!\\s*,)");
    private static final Pattern ASSOCIATETO_USAGE = Pattern.compile("AssociateTo\\s*\\[\\s*(<\\|[^|]+\\|>)");
    private static final Pattern KEYDROP_CHAIN = Pattern.compile("KeyDrop\\s*\\[\\s*KeyDrop\\s*\\[");
    private static final Pattern LOOKUP_MISSING = Pattern.compile("Lookup\\s*\\[\\s*\\w+\\s*,\\s*[^,]+,\\s*Missing\\s*\\[\\s*\\]\\s*\\]");
    private static final Pattern GROUPBY_USAGE = Pattern.compile("GroupBy\\s*\\[\\s*\\w+\\s*,\\s*[^,]+\\s*\\](?!\\s*,)");

    // ===== PATTERN SYSTEM RULES (Items 16-30) =====

    /**
     * Item 16: Detect unrestricted blank patterns that should have type restrictions.
     */
    public void detectUnrestrictedBlankPattern(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = UNRESTRICTED_BLANK.matcher(content);
            while (matcher.find()) {
                String functionName = matcher.group(1);
                String params = matcher.group(2);

                // Check if parameter uses simple blank without type restriction
                if (params.matches(".*\\b\\w+_(?![a-zA-Z?:/|]).*")) {
                    // Check if function body uses numeric operations that would benefit from type checking
                    int bodyStart = matcher.end();
                    int bodyEnd = findStatementEnd(content, bodyStart);
                    String body = content.substring(bodyStart, Math.min(bodyEnd, content.length()));

                    if (body.matches(".*[\\+\\-\\*/\\^].*") || body.contains("Power") || body.contains("Times")) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.UNRESTRICTED_BLANK_PATTERN_KEY,
                            String.format("Function '%s' uses unrestricted blank patterns but performs numeric operations. "
                                + "Consider adding type restrictions like '_?NumericQ'.", functionName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unrestricted blank patterns in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 17: Detect inefficient use of Condition instead of PatternTest.
     */
    public void detectPatternTestVsCondition(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PATTERN_TEST_CONDITION.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);
                String testFunc = matcher.group(2);

                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.PATTERN_TEST_VS_CONDITION_KEY,
                    String.format("Use PatternTest '%s_?%s' instead of Condition '%s_ /; %s[%s]' for better performance.",
                        varName, testFunc, varName, testFunc, varName));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting PatternTest vs Condition in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 18: Detect BlankSequence without type restrictions.
     */
    public void detectBlankSequenceWithoutRestriction(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = BLANKSEQUENCE.matcher(content);
            while (matcher.find()) {
                // Check if in function definition with numeric operations
                int position = matcher.start();
                String context50 = content.substring(Math.max(0, position - 50),
                    Math.min(content.length(), position + 150));

                if (context50.contains(":=") && context50.matches(".*[\\+\\-\\*/Total|Plus|Times].*")) {
                    int line = calculateLineNumber(content, position);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.BLANKSEQUENCE_WITHOUT_RESTRICTION_KEY,
                        "BlankSequence pattern '__' should have type restriction when used with numeric operations.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting BlankSequence in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 19: Detect nested optional patterns where defaults depend on other parameters.
     */
    public void detectNestedOptionalPatterns(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher funcMatcher = FUNCTION_DEFINITION.matcher(content);
            while (funcMatcher.find()) {
                String params = funcMatcher.group(2);

                // Find all optional patterns in this function
                Map<String, String> optionalDefaults = new HashMap<>();
                Matcher optMatcher = OPTIONAL_PATTERN.matcher(params);
                while (optMatcher.find()) {
                    String varName = optMatcher.group(1);
                    String defaultValue = optMatcher.group(2);
                    optionalDefaults.put(varName, defaultValue);
                }

                // Check if any default references another parameter
                for (Map.Entry<String, String> entry : optionalDefaults.entrySet()) {
                    String defaultVal = entry.getValue();
                    for (String paramName : optionalDefaults.keySet()) {
                        if (!paramName.equals(entry.getKey()) && defaultVal.contains(paramName)) {
                            int line = calculateLineNumber(content, funcMatcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.NESTED_OPTIONAL_PATTERNS_KEY,
                                String.format("Optional parameter default '%s' depends on another parameter '%s', causing evaluation order issues.",
                                    entry.getKey(), paramName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting nested optional patterns in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 20: Detect same pattern name with conflicting type restrictions.
     */
    public void detectPatternNamingConflicts(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher funcMatcher = FUNCTION_DEFINITION.matcher(content);
            while (funcMatcher.find()) {
                String params = funcMatcher.group(2);

                // Extract pattern names and their types
                Map<String, Set<String>> patternTypes = new HashMap<>();
                Matcher patternMatcher = PATTERN_NAME_USAGE.matcher(params);
                while (patternMatcher.find()) {
                    String name = patternMatcher.group(1);
                    String type = patternMatcher.group(2);

                    if (type != null && !type.isEmpty()) {
                        patternTypes.computeIfAbsent(name, k -> new HashSet<>()).add(type);
                    }
                }

                // Check for conflicts
                for (Map.Entry<String, Set<String>> entry : patternTypes.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        int line = calculateLineNumber(content, funcMatcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.PATTERN_NAMING_CONFLICTS_KEY,
                            String.format("Pattern name '%s' used with conflicting type restrictions: %s. This creates impossible-to-match patterns.",
                                entry.getKey(), entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting pattern naming conflicts in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 21: Detect redundant pattern names in alternatives.
     */
    public void detectRepeatedPatternAlternatives(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PATTERN_ALTERNATIVES.matcher(content);
            while (matcher.find()) {
                String patternName = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.REPEATED_PATTERN_ALTERNATIVES_KEY,
                    String.format("Pattern alternatives repeat name '%s' unnecessarily. Use 'x:(_Type1 | _Type2)' syntax instead.",
                        patternName));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting repeated pattern alternatives in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 22: Detect pure functions in PatternTest (performance issue).
     */
    public void detectPatternTestWithPureFunction(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PATTERN_TEST_PURE.matcher(content);
            while (matcher.find()) {
                // Check if in hot code (inside loop or frequently called function)
                int position = matcher.start();
                String surrounding = content.substring(Math.max(0, position - 200),
                    Math.min(content.length(), position + 50));

                if (surrounding.matches(".*(Do|While|Table|Map)\\s*\\[.*")) {
                    int line = calculateLineNumber(content, position);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.PATTERN_TEST_PURE_FUNCTION_KEY,
                        "Pure function in PatternTest creates closures on each match. Use built-in predicates or named functions instead.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting PatternTest with pure function in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 23: Detect optional arguments without proper defaults.
     */
    public void detectMissingPatternDefaults(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = OPTIONS_PATTERN_USAGE.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.MISSING_PATTERN_DEFAULTS_KEY,
                    "Optional sequence pattern '___' should use 'OptionsPattern[]' for validation.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting missing pattern defaults in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 24: Detect order-dependent pattern definitions.
     */
    public void detectOrderDependentPatterns(SensorContext context, InputFile inputFile, String content) {
        try {
            Map<String, List<String>> functionDefinitions = new HashMap<>();
            Matcher matcher = FUNCTION_DEFINITION.matcher(content);

            while (matcher.find()) {
                String funcName = matcher.group(1);
                String params = matcher.group(2);
                int position = matcher.start();

                functionDefinitions.computeIfAbsent(funcName, k -> new java.util.ArrayList<>())
                    .add(params + "@" + position);
            }

            // Check each function for order issues
            for (Map.Entry<String, List<String>> entry : functionDefinitions.entrySet()) {
                List<String> defs = entry.getValue();
                if (defs.size() > 1) {
                    for (int i = 0; i < defs.size(); i++) {
                        String[] parts1 = defs.get(i).split("@");
                        String params1 = parts1[0];

                        for (int j = i + 1; j < defs.size(); j++) {
                            String[] parts2 = defs.get(j).split("@");
                            String params2 = parts2[0];

                            // Check if later definition is more specific
                            if (isMoreSpecific(params2, params1)) {
                                int line = calculateLineNumber(content, Integer.parseInt(parts2[1]));
                                reportIssue(context, inputFile, line,
                                    MathematicaRulesDefinition.ORDER_DEPENDENT_PATTERNS_KEY,
                                    String.format("Function '%s' has more specific pattern defined after general pattern. "
                                        + "It will never match. Reorder definitions.", entry.getKey()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting order-dependent patterns in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 25: Detect Verbatim misuse.
     */
    public void detectVerbatimPatternMisuse(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = VERBATIM_USAGE.matcher(content);
            while (matcher.find()) {
                // Verbatim is often misunderstood - flag for review
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.VERBATIM_PATTERN_MISUSE_KEY,
                    "Verbatim has tricky semantics. Ensure it's used correctly for literal pattern matching.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Verbatim misuse in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 26: Detect unnecessary HoldPattern.
     */
    public void detectHoldPatternUnnecessary(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = HOLDPATTERN_USAGE.matcher(content);
            while (matcher.find()) {
                String innerPattern = matcher.group(1);

                // HoldPattern is unnecessary if pattern doesn't evaluate
                if (innerPattern.matches("_[a-zA-Z]*")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.HOLDPATTERN_UNNECESSARY_KEY,
                        "HoldPattern is unnecessary for simple blank patterns. Remove for clarity.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unnecessary HoldPattern in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 27: Detect Longest/Shortest without proper context.
     */
    public void detectLongestShortestWithoutOrdering(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = LONGEST_SHORTEST.matcher(content);
            while (matcher.find()) {
                // Check if followed by alternatives
                int position = matcher.start();
                String following = content.substring(position, Math.min(content.length(), position + 100));

                if (!following.contains("|")) {
                    int line = calculateLineNumber(content, position);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.LONGEST_SHORTEST_WITHOUT_ORDERING_KEY,
                        "Longest/Shortest modifiers need alternatives to be useful.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Longest/Shortest issues in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 28: Detect repeated pattern expecting same value twice.
     */
    public void detectPatternRepeatedDifferentTypes(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = REPEATED_PATTERN_NAME.matcher(content);
            while (matcher.find()) {
                String patternName = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.PATTERN_REPEATED_DIFFERENT_TYPES_KEY,
                    String.format("Pattern {%s_, %s_} doesn't check for equal values. "
                        + "Use {x_, y_} /; x == y if you want to enforce equality.", patternName, patternName));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting repeated pattern names in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 29: Detect alternatives with too many options.
     */
    public void detectAlternativesTooComplex(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ALTERNATIVES_COUNT.matcher(content);
            while (matcher.find()) {
                String pattern = matcher.group();
                // Count the number of pipe characters (alternatives)
                int pipeCount = 0;
                for (char c : pattern.toCharArray()) {
                    if (c == '|') {
                        pipeCount++;
                    }
                }
                // Report if 10 or more alternatives (9+ pipes means 10+ alternatives)
                if (pipeCount >= 9) {
                    int line = calculateLineNumber(content, matcher.start());
                    String message = String.format(
                        "Pattern alternatives with %d+ options cause backtracking explosion. "
                        + "Use MemberQ with condition instead.", pipeCount + 1);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.ALTERNATIVES_TOO_COMPLEX_KEY, message);
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting complex alternatives in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 30: Detect pattern matching on large lists.
     */
    public void detectPatternMatchingLargeLists(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CASES_LARGE_LIST.matcher(content);
            while (matcher.find()) {
                int size = Integer.parseInt(matcher.group(1));
                if (size > 1000) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.PATTERN_MATCHING_LARGE_LISTS_KEY,
                        String.format("Pattern matching on list of size %d is inefficient. Use Select, Pick, or Position instead.", size));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting pattern matching on large lists in {}", inputFile.filename(), e);
        }
    }

    // ===== LIST/ARRAY RULES (Items 31-40) =====

    /**
     * Item 31: Detect empty list indexing without length check.
     */
    public void detectEmptyListIndexing(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = LIST_INDEXING.matcher(content);
            while (matcher.find()) {
                String listVar = matcher.group(1);

                // Check if preceded by length check
                int position = matcher.start();
                String before = content.substring(Math.max(0, position - 200), position);

                if (!before.matches(".*Length\\s*\\[\\s*" + Pattern.quote(listVar) + "\\s*\\].*")) {
                    int line = calculateLineNumber(content, position);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.EMPTY_LIST_INDEXING_KEY,
                        String.format("List '%s' indexed without checking Length first. May fail on empty list.", listVar));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting empty list indexing in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 32: Detect negative index without validation.
     */
    public void detectNegativeIndexWithoutValidation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NEGATIVE_INDEX.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.NEGATIVE_INDEX_WITHOUT_VALIDATION_KEY,
                    "Negative index may fail if magnitude exceeds list length. Add validation.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting negative index in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 33: Detect Part assignment to non-variable.
     */
    public void detectPartAssignmentToImmutable(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PART_ASSIGNMENT.matcher(content);
            while (matcher.find()) {
                String expr = matcher.group(1).trim();

                // Check if starts with literal or function call
                if (expr.matches("^[A-Z{].*")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.PART_ASSIGNMENT_TO_IMMUTABLE_KEY,
                        "Part assignment to literal or expression doesn't modify anything. Assign to variable first.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Part assignment to immutable in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 34: Detect inefficient list concatenation in loops.
     */
    public void detectInefficientListConcatenation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = JOIN_IN_LOOP.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.INEFFICIENT_LIST_CONCATENATION_KEY,
                    "Join in loop has O(n²) complexity. Use Table, Reap/Sow, or pre-allocate instead.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting inefficient list concatenation in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 35: Detect unnecessary Flatten on already-flat lists.
     */
    public void detectUnnecessaryFlatten(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FLATTEN_USAGE.matcher(content);
            while (matcher.find()) {
                String firstElem = matcher.group(1);
                String secondElem = matcher.group(2);

                // If all elements are simple (no nested lists), Flatten is unnecessary
                if (secondElem == null || (!firstElem.contains("{") && !secondElem.contains("{"))) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.UNNECESSARY_FLATTEN_KEY,
                        "Flatten on already-flat list is unnecessary and wastes computation.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unnecessary Flatten in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 36: Detect Length recalculation in loop conditions.
     */
    public void detectLengthInLoopCondition(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = LENGTH_IN_DO.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.LENGTH_IN_LOOP_CONDITION_KEY,
                    "Length calculated in loop iterator. Cache value outside loop for better performance.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Length in loop condition in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 37: Detect double Reverse (no-op).
     */
    public void detectReverseTwice(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = REVERSE_TWICE.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.REVERSE_TWICE_KEY,
                    "Reverse[Reverse[...]] is a no-op. Remove redundant operations.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting double Reverse in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 38: Detect Sort with Greater instead of Reverse[Sort[...]].
     */
    public void detectSortWithoutComparison(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SORT_GREATER.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.SORT_WITHOUT_COMPARISON_KEY,
                    "Use Reverse[Sort[list]] instead of Sort[list, Greater] for better performance.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Sort with Greater in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 39: Detect Extract with Position instead of Select.
     */
    public void detectPositionVsSelect(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = EXTRACT_POSITION.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.POSITION_VS_SELECT_KEY,
                    "Use Select instead of Extract[..., Position[...]] for clearer and faster code.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Extract/Position pattern in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 40: Detect nested Part extraction.
     */
    public void detectNestedPartExtraction(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NESTED_PART.matcher(content);
            while (matcher.find()) {
                String listVar = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.NESTED_PART_EXTRACTION_KEY,
                    String.format("Use multi-dimensional Part syntax: %s[[i, j]] instead of %s[[i]][[j]]", listVar, listVar));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting nested Part extraction in {}", inputFile.filename(), e);
        }
    }

    // ===== ASSOCIATION RULES (Items 41-50) =====

    /**
     * Item 41: Detect association key access without existence check.
     */
    public void detectMissingKeyCheck(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSOC_ACCESS.matcher(content);
            while (matcher.find()) {
                String assocVar = matcher.group(1);
                int position = matcher.start();

                // Check if preceded by KeyExistsQ
                String before = content.substring(Math.max(0, position - 200), position);
                if (!before.contains("KeyExistsQ") && !before.contains("Lookup")) {
                    int line = calculateLineNumber(content, position);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.MISSING_KEY_CHECK_KEY,
                        String.format("Association '%s' accessed without KeyExistsQ check. May return Missing[\"KeyAbsent\"].", assocVar));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting missing key check in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 42: Detect list operations on associations.
     */
    public void detectAssociationVsListConfusion(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSOC_POSITIONAL.matcher(content);
            while (matcher.find()) {
                String assocVar = matcher.group(1);

                // Check if variable is likely an association (capitalized or contains "assoc")
                if (assocVar.toLowerCase().contains("assoc") || Character.isUpperCase(assocVar.charAt(0))) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.ASSOCIATION_VS_LIST_CONFUSION_KEY,
                        String.format(
                            "Positional indexing on '%s' may be incorrect for associations. Use Keys/Values or key-based access.", assocVar));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting association vs list confusion in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 43: Detect inefficient key lookup with Select instead of KeySelect.
     */
    public void detectInefficientKeyLookup(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SELECT_KEYS.matcher(content);
            while (matcher.find()) {
                String assocVar = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.INEFFICIENT_KEY_LOOKUP_KEY,
                    String.format("Use KeySelect[%s, ...] instead of Select[Keys[%s], ...] for better performance.", assocVar, assocVar));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting inefficient key lookup in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 44: Detect Query on non-Dataset.
     */
    public void detectQueryOnNonDataset(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = QUERY_USAGE.matcher(content);
            while (matcher.find()) {
                // Check if applied to Dataset
                int position = matcher.start();
                String following = content.substring(position, Math.min(content.length(), position + 150));

                if (!following.contains("Dataset")) {
                    int line = calculateLineNumber(content, position);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.QUERY_ON_NON_DATASET_KEY,
                        "Query requires Dataset wrapper. Use Dataset[data] before applying Query.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Query on non-Dataset in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 45: Detect ambiguous association update pattern.
     */
    public void detectAssociationUpdatePattern(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSOC_DIRECT_ASSIGN.matcher(content);
            while (matcher.find()) {
                String assocVar = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.ASSOCIATION_UPDATE_PATTERN_KEY,
                    String.format("Use AssociateTo[%s, key -> value] instead of direct assignment for clarity.", assocVar));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting association update pattern in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 46: Detect Merge without conflict strategy.
     */
    public void detectMergeWithoutConflictStrategy(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MERGE_USAGE.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.MERGE_WITHOUT_CONFLICT_STRATEGY_KEY,
                    "Merge without combining function uses List by default. Specify merge strategy explicitly.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Merge without strategy in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 47: Detect AssociateTo on non-symbol.
     */
    public void detectAssociateToOnNonSymbol(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSOCIATETO_USAGE.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.ASSOCIATETO_ON_NON_SYMBOL_KEY,
                    "AssociateTo requires a symbol (variable name), not a literal. Assign to variable first.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting AssociateTo on non-symbol in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 48: Detect chained KeyDrop operations.
     */
    public void detectKeyDropMultipleTimes(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = KEYDROP_CHAIN.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.KEYDROP_MULTIPLE_TIMES_KEY,
                    "Use KeyDrop[assoc, {key1, key2}] instead of chained KeyDrop calls for efficiency.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting chained KeyDrop in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 49: Detect redundant Missing default in Lookup.
     */
    public void detectLookupWithMissingDefault(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = LOOKUP_MISSING.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.LOOKUP_WITH_MISSING_DEFAULT_KEY,
                    "Missing[] is the default for Lookup. Third argument is redundant.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Lookup with Missing default in {}", inputFile.filename(), e);
        }
    }

    /**
     * Item 50: Detect GroupBy without aggregation where GatherBy may be clearer.
     */
    public void detectGroupByWithoutAggregation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GROUPBY_USAGE.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.GROUPBY_WITHOUT_AGGREGATION_KEY,
                    "GroupBy without aggregation function. Consider GatherBy for clearer intent when not aggregating.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting GroupBy without aggregation in {}", inputFile.filename(), e);
        }
    }

    // ===== HELPER METHODS =====

    private int findStatementEnd(String content, int start) {
        int semicolon = content.indexOf(';', start);
        int newline = content.indexOf('\n', start);
        if (semicolon == -1) {
            return newline == -1 ? content.length() : newline;
        }
        if (newline == -1) {
            return semicolon;
        }
        return Math.min(semicolon, newline);
    }

    private boolean isMoreSpecific(String params1, String params2) {
        // params1 is more specific if it has:
        // - No blanks while params2 has blanks
        // - Type restrictions while params2 doesn't
        // - Literal values while params2 has patterns

        boolean params1HasBlanks = params1.contains("_");
        boolean params2HasBlanks = params2.contains("_");

        if (!params1HasBlanks && params2HasBlanks) {
            return true; // Literal is more specific than pattern
        }

        // Check for type restrictions
        boolean params1HasTypes = params1.matches(".*_[a-zA-Z]\\w*.*");
        boolean params2HasTypes = params2.matches(".*_[a-zA-Z]\\w*.*");

        if (params1HasTypes && !params2HasTypes && params1HasBlanks && params2HasBlanks) {
            return true; // Type-restricted pattern is more specific than unrestricted
        }

        return false;
    }
}
