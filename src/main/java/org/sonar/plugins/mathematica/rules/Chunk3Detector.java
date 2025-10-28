package org.sonar.plugins.mathematica.rules;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Detector for Chunk 3 rules (Items 111-150 from ROADMAP_325.md):
 * - Type Mismatch Detection (20 rules)
 * - Data Flow Analysis (16 rules)
 */
public class Chunk3Detector extends BaseDetector {
    private static final Logger LOG = LoggerFactory.getLogger(Chunk3Detector.class);

    // ===== Pre-compiled patterns for Type Mismatch Detection =====

    // Pattern for numeric operations on strings
    private static final Pattern STRING_ARITHMETIC = Pattern.compile("\"[^\"]*\"\\s*[\\+\\-\\*/\\^]");

    // Pattern for string operations on numbers
    private static final Pattern STRING_FUNCTION_ON_NUMBER = Pattern.compile("\\b(StringJoin|StringLength|StringTake|StringDrop|StringReplace)\\s*\\[\\s*\\d+");

    // Pattern for common type-specific functions
    private static final Pattern MAP_FUNCTION = Pattern.compile("\\bMap\\s*\\[");
    private static final Pattern LENGTH_FUNCTION = Pattern.compile("\\bLength\\s*\\[");

    // Pattern for function definitions with type constraints
    private static final Pattern TYPED_FUNCTION_DEF = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[([^\\]]*_(?:Integer|Real|String|List)[^\\]]*)\\]\\s*:?=");

    // Pattern for integer division
    private static final Pattern INTEGER_DIVISION = Pattern.compile("\\b(\\d+)\\s*/\\s*(\\d+)\\b");

    // Pattern for ToExpression without validation
    private static final Pattern TO_EXPRESSION = Pattern.compile("\\bToExpression\\s*\\[([^\\]]+)\\]");

    // Pattern for ToString
    private static final Pattern TO_STRING = Pattern.compile("\\bToString\\s*\\[\"([^\"]+)\"\\]");

    // Pattern for Plot/Graphics in arithmetic
    private static final Pattern PLOT_ARITHMETIC = Pattern.compile("\\b(Plot|ListPlot|Graphics)\\s*\\[[^\\]]+\\]\\s*[\\+\\-\\*/]");

    // Pattern for Image operations
    private static final Pattern IMAGE_OPERATION = Pattern.compile("\\b(ImageData|ImageDimensions)\\s*\\[\\s*\\{");

    // Pattern for Audio operations
    private static final Pattern AUDIO_OPERATION = Pattern.compile("\\b(AudioData|SampleRate)\\s*\\[\\s*\\{");

    // Pattern for Dataset operations on lists
    private static final Pattern DATASET_OPERATION = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[All");

    // Pattern for Graph operations
    private static final Pattern GRAPH_OPERATION = Pattern.compile("\\b(VertexList|EdgeList)\\s*\\[\\s*\\{\\{");

    // ===== Pre-compiled patterns for Data Flow Analysis =====

    // Pattern for variable assignment
    private static final Pattern VARIABLE_ASSIGNMENT = Pattern.compile("([a-zA-Z]\\w*)\\s*=\\s*([^;\\n]+)");

    // Pattern for If statement
    private static final Pattern IF_STATEMENT = Pattern.compile("\\bIf\\s*\\[");

    // Pattern for Module/Block/With
    private static final Pattern MODULE_BLOCK_WITH = Pattern.compile("\\b(Module|Block|With)\\s*\\[\\s*\\{([^}]+)\\}");

    // Pattern for Do loop
    private static final Pattern DO_LOOP = Pattern.compile("\\bDo\\s*\\[([^\\]]+),\\s*\\{\\s*(\\w+)\\s*,");

    // Pattern for Unset/Clear
    private static final Pattern UNSET_CLEAR = Pattern.compile("\\b(Unset|Clear)\\s*\\[\\s*(\\w+)");

    // Pattern for pure functions with mutations
    private static final Pattern PURE_FUNCTION_MUTATION = Pattern.compile("([a-zA-Z]\\w*)\\s*\\+\\+");

    // Pattern for assignment in condition
    private static final Pattern ASSIGNMENT_IN_IF = Pattern.compile("\\bIf\\s*\\[\\s*([a-zA-Z]\\w*)\\s*=\\s*[^=]");

    // Pattern for function return
    private static final Pattern FUNCTION_RETURN = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[([^\\]]*)\\]\\s*:?=\\s*\\(([^\\)]*)\\)");

    // ===== TYPE MISMATCH DETECTION METHODS (Items 111-130) =====

    public void detectNumericOperationOnString(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = STRING_ARITHMETIC.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.NUMERIC_OPERATION_ON_STRING_KEY,
                    "Arithmetic operations on string literals produce unexpected results.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting numeric operations on strings in {}", inputFile.filename(), e);
        }
    }

    public void detectStringOperationOnNumber(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = STRING_FUNCTION_ON_NUMBER.matcher(content);
            while (matcher.find()) {
                String function = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.STRING_OPERATION_ON_NUMBER_KEY,
                    String.format("String function '%s' called on numeric literal.", function));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting string operations on numbers in {}", inputFile.filename(), e);
        }
    }

    public void detectWrongArgumentType(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect Map on non-list
            Matcher mapMatcher = MAP_FUNCTION.matcher(content);
            while (mapMatcher.find()) {
                int argsStart = mapMatcher.end();
                // Simple heuristic: check if second arg is a number
                String argsSection = content.substring(argsStart, Math.min(argsStart + 50, content.length()));
                if (argsSection.matches(".*,\\s*\\d+\\s*\\].*")) {
                    int line = calculateLineNumber(content, mapMatcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.WRONG_ARGUMENT_TYPE_KEY,
                        "Map expects a list as second argument, not a number.");
                }
            }

            // Detect Length on non-list
            Matcher lengthMatcher = LENGTH_FUNCTION.matcher(content);
            while (lengthMatcher.find()) {
                int argsStart = lengthMatcher.end();
                String argsSection = content.substring(argsStart, Math.min(argsStart + 20, content.length()));
                if (argsSection.matches("^\\s*\\d+\\s*\\].*")) {
                    int line = calculateLineNumber(content, lengthMatcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.WRONG_ARGUMENT_TYPE_KEY,
                        "Length expects a list/association/string, not a number.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting wrong argument types in {}", inputFile.filename(), e);
        }
    }

    public void detectFunctionReturnsWrongType(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect functions with inconsistent return types (simplified heuristic)
            Matcher matcher = FUNCTION_RETURN.matcher(content);
            while (matcher.find()) {
                String funcName = matcher.group(1);
                String body = matcher.group(3);

                // Check if body returns both numbers and strings
                boolean hasNumberReturn = body.matches(".*\\b\\d+\\b.*");
                boolean hasStringReturn = body.matches(".*\"[^\"]+\".*");

                if (hasNumberReturn && hasStringReturn) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.FUNCTION_RETURNS_WRONG_TYPE_KEY,
                        String.format("Function '%s' returns inconsistent types (numbers and strings).", funcName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting wrong return types in {}", inputFile.filename(), e);
        }
    }

    public void detectComparisonIncompatibleTypes(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect string compared to number
            Pattern pattern = Pattern.compile("\"[^\"]+\"\\s*(<|>|<=|>=)\\s*\\d+");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.COMPARISON_INCOMPATIBLE_TYPES_KEY,
                    "Comparing string to number produces meaningless result.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting incompatible type comparisons in {}", inputFile.filename(), e);
        }
    }

    public void detectMixedNumericTypes(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect exact (fraction) mixed with approximate (decimal)
            Pattern pattern = Pattern.compile("\\d+\\s*/\\s*\\d+\\s*[\\+\\-]\\s*\\d+\\.\\d+");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.MIXED_NUMERIC_TYPES_KEY,
                    "Mixing exact and approximate numbers causes precision loss.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting mixed numeric types in {}", inputFile.filename(), e);
        }
    }

    public void detectIntegerDivisionExpectingReal(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = INTEGER_DIVISION.matcher(content);
            while (matcher.find()) {
                // Check if in numeric context (simplified)
                int contextStart = Math.max(0, matcher.start() - 50);
                String beforeContext = content.substring(contextStart, matcher.start());

                if (beforeContext.contains("N[") || beforeContext.contains("//N")) {
                    // Already being converted, OK
                    continue;
                }

                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.INTEGER_DIVISION_EXPECTING_REAL_KEY,
                    "Integer division stays symbolic. Use real division (1./2) for numeric result.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting integer division expecting real in {}", inputFile.filename(), e);
        }
    }

    public void detectListFunctionOnAssociation(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect Append on association-like structure
            Pattern pattern = Pattern.compile("\\bAppend\\s*\\[\\s*<\\|");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.LIST_FUNCTION_ON_ASSOCIATION_KEY,
                    "Use AssociateTo instead of Append for associations.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting list functions on associations in {}", inputFile.filename(), e);
        }
    }

    public void detectPatternTypeMismatch(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher defMatcher = TYPED_FUNCTION_DEF.matcher(content);
            while (defMatcher.find()) {
                String funcName = defMatcher.group(1);
                String params = defMatcher.group(2);

                // Extract type constraint
                String typeConstraint = null;
                if (params.contains("_Integer")) {
                    typeConstraint = "Integer";
                } else if (params.contains("_Real")) {
                    typeConstraint = "Real";
                } else if (params.contains("_String")) {
                    typeConstraint = "String";
                }

                if (typeConstraint != null) {
                    // Look for calls to this function with wrong type
                    Pattern callPattern = Pattern.compile("\\b" + Pattern.quote(funcName) + "\\s*\\[([^\\]]+)\\]");
                    Matcher callMatcher = callPattern.matcher(content);

                    while (callMatcher.find()) {
                        String arg = callMatcher.group(1);
                        boolean typeMismatch = false;

                        if ("Integer".equals(typeConstraint) && arg.matches("\"[^\"]+\"")) {
                            typeMismatch = true;
                        } else if ("String".equals(typeConstraint) && arg.matches("\\d+")) {
                            typeMismatch = true;
                        }

                        if (typeMismatch) {
                            int line = calculateLineNumber(content, callMatcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.PATTERN_TYPE_MISMATCH_KEY,
                                String.format("Function '%s' expects %s but called with incompatible type.",
                                    funcName, typeConstraint));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting pattern type mismatches in {}", inputFile.filename(), e);
        }
    }

    public void detectOptionalTypeInconsistent(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect _Integer : 1.5 (Real default for Integer pattern)
            Pattern pattern = Pattern.compile("_Integer\\s*:\\s*\\d+\\.\\d+");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.OPTIONAL_TYPE_INCONSISTENT_KEY,
                    "Optional parameter default type (Real) doesn't match pattern type (Integer).");
            }

            // Detect _Real : integer
            Pattern pattern2 = Pattern.compile("_Real\\s*:\\s*\\d+(?!\\.\\d)");
            Matcher matcher2 = pattern2.matcher(content);
            while (matcher2.find()) {
                int line = calculateLineNumber(content, matcher2.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.OPTIONAL_TYPE_INCONSISTENT_KEY,
                    "Optional parameter default type (Integer) doesn't match pattern type (Real).");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting optional type inconsistencies in {}", inputFile.filename(), e);
        }
    }

    public void detectReturnTypeInconsistent(SensorContext context, InputFile inputFile, String content) {
        // Already partially covered by detectFunctionReturnsWrongType
        // This is a placeholder for enhanced detection
    }

    public void detectNullAssignmentToTypedVariable(SensorContext context, InputFile inputFile, String content) {
        try {
            Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*=\\s*Null");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String varName = matcher.group(1);
                int assignPos = matcher.end();

                // Check if variable later used in numeric context
                String afterAssign = content.substring(assignPos, Math.min(assignPos + 200, content.length()));
                if (afterAssign.contains(varName + " +") || afterAssign.contains(varName + " *") ||
                    afterAssign.contains(varName + "^")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.NULL_ASSIGNMENT_TO_TYPED_VARIABLE_KEY,
                        String.format("Variable '%s' assigned Null but used in numeric context.", varName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting null assignment to typed variables in {}", inputFile.filename(), e);
        }
    }

    public void detectTypeCastWithoutValidation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = TO_EXPRESSION.matcher(content);
            while (matcher.find()) {
                String arg = matcher.group(1);

                // Check if there's no StringQ or validation nearby
                int contextStart = Math.max(0, matcher.start() - 100);
                String beforeContext = content.substring(contextStart, matcher.start());

                if (!beforeContext.contains("StringQ[" + arg + "]") &&
                    !beforeContext.contains("If[StringQ")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.TYPE_CAST_WITHOUT_VALIDATION_KEY,
                        "ToExpression used without validating input is a string.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting type cast without validation in {}", inputFile.filename(), e);
        }
    }

    public void detectImplicitTypeConversion(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = TO_STRING.matcher(content);
            while (matcher.find()) {
                String stringArg = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.IMPLICIT_TYPE_CONVERSION_KEY,
                    String.format("ToString[\"%s\"] is redundant - argument is already a string.", stringArg));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting implicit type conversions in {}", inputFile.filename(), e);
        }
    }

    public void detectGraphicsObjectInNumericContext(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PLOT_ARITHMETIC.matcher(content);
            while (matcher.find()) {
                String graphicsFunc = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.GRAPHICS_OBJECT_IN_NUMERIC_CONTEXT_KEY,
                    String.format("%s graphics object used in arithmetic operation.", graphicsFunc));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting graphics objects in numeric context in {}", inputFile.filename(), e);
        }
    }

    public void detectSymbolInNumericContext(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect undefined variables in arithmetic (simplified heuristic)
            Pattern pattern = Pattern.compile("\\b([a-z]\\w*)\\s*[\\+\\-\\*/]\\s*\\d+");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String varName = matcher.group(1);

                // Check if variable was assigned earlier
                Pattern assignPattern = Pattern.compile("\\b" + Pattern.quote(varName) + "\\s*=");
                if (!assignPattern.matcher(content.substring(0, matcher.start())).find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.SYMBOL_IN_NUMERIC_CONTEXT_KEY,
                        String.format("Symbolic variable '%s' in numeric context may not evaluate.", varName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting symbols in numeric context in {}", inputFile.filename(), e);
        }
    }

    public void detectImageOperationOnNonImage(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = IMAGE_OPERATION.matcher(content);
            while (matcher.find()) {
                String operation = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.IMAGE_OPERATION_ON_NON_IMAGE_KEY,
                    String.format("%s expects Image object, not raw array. Wrap in Image[] first.", operation));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting image operations on non-images in {}", inputFile.filename(), e);
        }
    }

    public void detectSoundOperationOnNonSound(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = AUDIO_OPERATION.matcher(content);
            while (matcher.find()) {
                String operation = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.SOUND_OPERATION_ON_NON_SOUND_KEY,
                    String.format("%s expects Audio object, not raw array. Wrap in Audio[] first.", operation));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting audio operations on non-audio in {}", inputFile.filename(), e);
        }
    }

    public void detectDatasetOperationOnList(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DATASET_OPERATION.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);

                // Check if variable is a list (simple heuristic)
                Pattern listAssign = Pattern.compile("\\b" + Pattern.quote(varName) + "\\s*=\\s*\\{\\{");
                if (listAssign.matcher(content.substring(0, matcher.start())).find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.DATASET_OPERATION_ON_LIST_KEY,
                        String.format("Dataset operations on '%s' require Dataset wrapper. Use Dataset[%s].",
                            varName, varName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting dataset operations on lists in {}", inputFile.filename(), e);
        }
    }

    public void detectGraphOperationOnNonGraph(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GRAPH_OPERATION.matcher(content);
            while (matcher.find()) {
                String operation = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.GRAPH_OPERATION_ON_NON_GRAPH_KEY,
                    String.format("%s expects Graph object, not edge list. Wrap in Graph[] first.", operation));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting graph operations on non-graphs in {}", inputFile.filename(), e);
        }
    }

    // ===== DATA FLOW ANALYSIS METHODS (Items 135-150) =====

    public void detectUninitializedVariableUseEnhanced(SensorContext context, InputFile inputFile, String content) {
        try {
            Set<String> definedVars = new HashSet<>();

            // Track all assignments
            Matcher assignMatcher = VARIABLE_ASSIGNMENT.matcher(content);
            Map<Integer, String> assignments = new HashMap<>();

            while (assignMatcher.find()) {
                int pos = assignMatcher.start();
                String varName = assignMatcher.group(1);
                assignments.put(pos, varName);
                definedVars.add(varName);
            }

            // Check for uses before assignment
            Pattern usePattern = Pattern.compile("\\b([a-z]\\w*)\\b");
            Matcher useMatcher = usePattern.matcher(content);

            while (useMatcher.find()) {
                String varName = useMatcher.group(1);
                int usePos = useMatcher.start();

                // Check if used before any assignment
                boolean usedBeforeAssignment = true;
                for (Map.Entry<Integer, String> entry : assignments.entrySet()) {
                    if (entry.getValue().equals(varName) && entry.getKey() < usePos) {
                        usedBeforeAssignment = false;
                        break;
                    }
                }

                if (usedBeforeAssignment && definedVars.contains(varName)) {
                    int line = calculateLineNumber(content, usePos);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.UNINITIALIZED_VARIABLE_USE_ENHANCED_KEY,
                        String.format("Variable '%s' used before initialization.", varName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting uninitialized variable use in {}", inputFile.filename(), e);
        }
    }

    public void detectVariableMayBeUninitialized(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher ifMatcher = IF_STATEMENT.matcher(content);
            while (ifMatcher.find()) {
                int ifStart = ifMatcher.start();
                int ifEnd = findMatchingBracket(content, ifMatcher.end());

                if (ifEnd > ifStart) {
                    String ifBody = content.substring(ifStart, ifEnd);

                    // Look for assignments in If body
                    Matcher assignMatcher = VARIABLE_ASSIGNMENT.matcher(ifBody);
                    while (assignMatcher.find()) {
                        String varName = assignMatcher.group(1);

                        // Check if variable used after If
                        String afterIf = content.substring(ifEnd, Math.min(ifEnd + 200, content.length()));
                        if (afterIf.contains(varName)) {
                            int line = calculateLineNumber(content, ifStart);
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.VARIABLE_MAY_BE_UNINITIALIZED_KEY,
                                String.format("Variable '%s' may be uninitialized if condition is false.", varName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting variables that may be uninitialized in {}", inputFile.filename(), e);
        }
    }

    public void detectDeadStore(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = VARIABLE_ASSIGNMENT.matcher(content);
            Map<String, Integer> assignments = new HashMap<>();

            while (matcher.find()) {
                String varName = matcher.group(1);
                int pos = matcher.start();

                // Check if previous assignment to same variable was never read
                if (assignments.containsKey(varName)) {
                    int prevPos = assignments.get(varName);
                    String between = content.substring(prevPos, pos);

                    // Check if variable used between assignments
                    if (!between.contains(varName + " ") && !between.contains(varName + ")") &&
                        !between.contains(varName + "]")) {
                        int line = calculateLineNumber(content, prevPos);
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.DEAD_STORE_KEY,
                            String.format("Assignment to '%s' is never read before being overwritten.", varName));
                    }
                }

                assignments.put(varName, pos);
            }
        } catch (Exception e) {
            LOG.debug("Error detecting dead stores in {}", inputFile.filename(), e);
        }
    }

    public void detectOverwrittenBeforeRead(SensorContext context, InputFile inputFile, String content) {
        // Covered by detectDeadStore - this is essentially the same rule
    }

    public void detectVariableAliasingIssue(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect list1 = list2 followed by list2[[i]] = x
            Pattern aliasPattern = Pattern.compile("([a-zA-Z]\\w*)\\s*=\\s*([a-zA-Z]\\w*)\\s*;");
            Matcher matcher = aliasPattern.matcher(content);

            while (matcher.find()) {
                String var1 = matcher.group(1);
                String var2 = matcher.group(2);
                int aliasPos = matcher.end();

                // Check if either variable is modified via Part
                String afterAlias = content.substring(aliasPos, Math.min(aliasPos + 300, content.length()));
                if (afterAlias.matches(".*" + Pattern.quote(var1) + "\\[\\[.*") ||
                    afterAlias.matches(".*" + Pattern.quote(var2) + "\\[\\[.*")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.VARIABLE_ALIASING_ISSUE_KEY,
                        String.format("Variables '%s' and '%s' alias same mutable structure.", var1, var2));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting variable aliasing issues in {}", inputFile.filename(), e);
        }
    }

    public void detectModificationOfLoopIterator(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DO_LOOP.matcher(content);
            while (matcher.find()) {
                String iteratorName = matcher.group(2);
                String loopBody = matcher.group(1);

                // Check if iterator is modified in body
                Pattern modPattern = Pattern.compile("\\b" + Pattern.quote(iteratorName) + "\\s*[\\+\\-\\*/]?=");
                if (modPattern.matcher(loopBody).find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.MODIFICATION_OF_LOOP_ITERATOR_KEY,
                        String.format("Loop iterator '%s' should not be modified inside loop body.", iteratorName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting loop iterator modifications in {}", inputFile.filename(), e);
        }
    }

    public void detectUseOfIteratorOutsideLoop(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DO_LOOP.matcher(content);
            while (matcher.find()) {
                String iteratorName = matcher.group(2);
                int loopEnd = findMatchingBracket(content, matcher.end());

                // Check if iterator used after loop
                String afterLoop = content.substring(loopEnd, Math.min(loopEnd + 100, content.length()));
                if (afterLoop.contains(iteratorName)) {
                    int line = calculateLineNumber(content, loopEnd);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.USE_OF_ITERATOR_OUTSIDE_LOOP_KEY,
                        String.format("Loop iterator '%s' value after loop is undefined.", iteratorName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting iterator use outside loop in {}", inputFile.filename(), e);
        }
    }

    public void detectReadingUnsetVariable(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = UNSET_CLEAR.matcher(content);
            while (matcher.find()) {
                String command = matcher.group(1);
                String varName = matcher.group(2);
                int unsetPos = matcher.end();

                // Check if variable used after Unset
                String afterUnset = content.substring(unsetPos, Math.min(unsetPos + 100, content.length()));
                if (afterUnset.contains(varName)) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.READING_UNSET_VARIABLE_KEY,
                        String.format("Variable '%s' read after %s - returns symbol, not value.", varName, command));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting reading unset variables in {}", inputFile.filename(), e);
        }
    }

    public void detectDoubleAssignmentSameValue(SensorContext context, InputFile inputFile, String content) {
        try {
            Map<String, String> assignments = new HashMap<>();

            Matcher matcher = VARIABLE_ASSIGNMENT.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);
                String value = matcher.group(2).trim();

                if (assignments.containsKey(varName)) {
                    String prevValue = assignments.get(varName);
                    if (prevValue.equals(value)) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.DOUBLE_ASSIGNMENT_SAME_VALUE_KEY,
                            String.format("Variable '%s' assigned same value '%s' twice.", varName, value));
                    }
                }

                assignments.put(varName, value);
            }
        } catch (Exception e) {
            LOG.debug("Error detecting double assignment of same value in {}", inputFile.filename(), e);
        }
    }

    public void detectMutationInPureFunction(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect mutations (++) inside pure functions (&)
            Pattern pattern = Pattern.compile("\\(([^\\(\\)]*\\+\\+[^\\(\\)]*)\\s*\\&");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                Matcher mutMatcher = PURE_FUNCTION_MUTATION.matcher(matcher.group(1));
                if (mutMatcher.find()) {
                    String varName = mutMatcher.group(1);
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.MUTATION_IN_PURE_FUNCTION_KEY,
                        String.format("Pure function mutates outer variable '%s' - side effect.", varName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting mutations in pure functions in {}", inputFile.filename(), e);
        }
    }

    public void detectSharedMutableState(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect global variables modified by multiple functions
            Pattern globalAssign = Pattern.compile("^\\s*([a-zA-Z]\\w*)\\s*=", Pattern.MULTILINE);
            Matcher matcher = globalAssign.matcher(content);

            Set<String> globalVars = new HashSet<>();
            while (matcher.find()) {
                globalVars.add(matcher.group(1));
            }

            // Check if these globals are modified inside functions
            for (String varName : globalVars) {
                Pattern funcModPattern = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[[^\\]]*\\]\\s*:=.*" +
                    Pattern.quote(varName) + "\\s*[\\+\\-\\*/]?=");
                Matcher funcMatcher = funcModPattern.matcher(content);

                int modCount = 0;
                while (funcMatcher.find()) {
                    modCount++;
                }

                if (modCount >= 2) {
                    reportIssue(context, inputFile, 1,
                        MathematicaRulesDefinition.SHARED_MUTABLE_STATE_KEY,
                        String.format("Global variable '%s' modified by multiple functions.", varName));
                    break; // Report once per file
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting shared mutable state in {}", inputFile.filename(), e);
        }
    }

    public void detectVariableScopeEscape(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect Module[{x}, x] - returning uninitialized local
            Matcher matcher = MODULE_BLOCK_WITH.matcher(content);
            while (matcher.find()) {
                String scopeType = matcher.group(1);
                String vars = matcher.group(2);
                int bodyStart = matcher.end();
                int bodyEnd = findMatchingBracket(content, bodyStart);

                if (bodyEnd > bodyStart) {
                    String body = content.substring(bodyStart, bodyEnd);

                    // Check if body is just a variable name
                    String[] varList = vars.split(",");
                    for (String var : varList) {
                        String varName = var.trim().split("\\s*=")[0].trim();
                        if (body.trim().equals(varName)) {
                            int line = calculateLineNumber(content, matcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.VARIABLE_SCOPE_ESCAPE_KEY,
                                String.format("%s returns local variable '%s' as symbol.", scopeType, varName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting variable scope escape in {}", inputFile.filename(), e);
        }
    }

    public void detectClosureOverMutableVariable(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect Table[Function[... i ...], {i, ...}] - closure captures final i
            Pattern pattern = Pattern.compile("Table\\s*\\[\\s*Function\\s*\\[[^\\]]*\\b(\\w+)\\b[^\\]]*\\][^,]*,\\s*\\{\\s*\\1\\s*,");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String varName = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.CLOSURE_OVER_MUTABLE_VARIABLE_KEY,
                    String.format("Closure captures mutable variable '%s' - use With to capture value.", varName));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting closures over mutable variables in {}", inputFile.filename(), e);
        }
    }

    public void detectAssignmentInConditionEnhanced(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ASSIGNMENT_IN_IF.matcher(content);
            while (matcher.find()) {
                String varName = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITION_ENHANCED_KEY,
                    String.format("Assignment '%s = ...' in If condition - use '==' for comparison.", varName));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting assignment in conditions in {}", inputFile.filename(), e);
        }
    }

    public void detectAssignmentAsReturnValue(SensorContext context, InputFile inputFile, String content) {
        try {
            // Detect f[x_] := (y = x; y)
            Pattern pattern = Pattern.compile("([a-zA-Z]\\w*)\\s*\\[[^\\]]*\\]\\s*:=\\s*\\(\\s*([a-zA-Z]\\w*)\\s*=\\s*([^;]+);\\s*\\2\\s*\\)");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String funcName = matcher.group(1);
                String varName = matcher.group(2);
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.ASSIGNMENT_AS_RETURN_VALUE_KEY,
                    String.format("Function '%s' assigns to '%s' just to return it - return directly.", funcName, varName));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting assignment as return value in {}", inputFile.filename(), e);
        }
    }

    public void detectVariableNeverModified(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MODULE_BLOCK_WITH.matcher(content);
            while (matcher.find()) {
                String scopeType = matcher.group(1);
                if (!"Module".equals(scopeType)) {
                    continue; // Only check Module
                }

                String vars = matcher.group(2);
                int bodyStart = matcher.end();
                int bodyEnd = findMatchingBracket(content, bodyStart);

                if (bodyEnd > bodyStart) {
                    String body = content.substring(bodyStart, bodyEnd);

                    String[] varList = vars.split(",");
                    for (String var : varList) {
                        String varName = var.trim().split("\\s*=")[0].trim();

                        // Check if variable never modified (no assignments in body)
                        Pattern assignPattern = Pattern.compile("\\b" + Pattern.quote(varName) + "\\s*=");
                        if (!assignPattern.matcher(body).find()) {
                            int line = calculateLineNumber(content, matcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.VARIABLE_NEVER_MODIFIED_KEY,
                                String.format("Module variable '%s' never modified - use With instead.", varName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting variables never modified in {}", inputFile.filename(), e);
        }
    }

    // ===== Helper methods =====

    private int findMatchingBracket(String content, int start) {
        if (start >= content.length()) {
            return content.length();
        }

        char openBracket = content.charAt(start);
        char closeBracket;

        switch (openBracket) {
            case '[':
                closeBracket = ']';
                break;
            case '(':
                closeBracket = ')';
                break;
            case '{':
                closeBracket = '}';
                break;
            default:
                // If not starting at a bracket, find the next bracket
                openBracket = '[';
                closeBracket = ']';
                break;
        }

        int depth = 1;
        for (int i = start + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == openBracket) {
                depth++;
            } else if (c == closeBracket) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return content.length();
    }
}
