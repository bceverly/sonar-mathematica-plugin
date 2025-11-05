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
 * Detector for Chunk 2 rules (Items 61-100 from ROADMAP_325.md):
 * - Unused Code Detection (15 rules)
 * - Shadowing & Naming (15 rules)
 * - Undefined Symbol Detection (10 rules)
 */
public class UnusedAndNamingDetector extends BaseDetector {
    private static final Logger LOG = LoggerFactory.getLogger(UnusedAndNamingDetector.class);

    // ===== Pre-compiled patterns for Unused Code Detection =====

    // Pattern for function definitions
    private static final Pattern FUNCTION_DEF = Pattern.compile("([a-zA-Z]\\w*+)\\s*+\\[([^\\]]*+)\\]\\s*+:?+=");

    // Pattern for Return statements
    private static final Pattern RETURN_STATEMENT = Pattern.compile("\\bReturn\\s*+\\[");

    // Pattern for Abort/Throw
    private static final Pattern ABORT_THROW = Pattern.compile("\\b(Abort|Throw)\\s*+\\[");

    // Pattern for Module variables
    private static final Pattern MODULE_VARS = Pattern.compile("\\bModule\\s*+\\[\\s*+\\{([^}]++)\\}");

    // Pattern for With variables
    private static final Pattern WITH_VARS = Pattern.compile("\\bWith\\s*+\\[\\s*+\\{([^}]++)\\}");

    // Pattern for Needs/imports
    private static final Pattern NEEDS_IMPORT = Pattern.compile("\\bNeeds\\s*+\\[\\s*+\"([^\"]++)\"");

    // Pattern for optional parameters
    // Note: Cannot use possessive on \w+ before ___ since \w includes _
    private static final Pattern OPTIONAL_PARAM = Pattern.compile("(\\w+)___(?:\\s*+:?+=|\\s*+\\])");

    // Pattern for Do loop with iterator
    private static final Pattern DO_LOOP_ITERATOR = Pattern.compile("\\bDo\\s*+\\[([^\\]]++),\\s*+\\{\\s*+(\\w++)\\s*+,");

    // Pattern for Catch
    private static final Pattern CATCH_PATTERN = Pattern.compile("\\bCatch\\s*+\\[");

    // Pattern for Throw
    private static final Pattern THROW_PATTERN = Pattern.compile("\\bThrow\\s*+\\[");

    // Pattern for If[False
    private static final Pattern IF_FALSE = Pattern.compile("\\bIf\\s*+\\[\\s*+False\\s*+,");

    // ===== Pre-compiled patterns for Shadowing & Naming =====

    // Pattern for BeginPackage/EndPackage
    private static final Pattern BEGIN_PACKAGE = Pattern.compile("\\bBeginPackage\\s*+\\[");
    private static final Pattern END_PACKAGE = Pattern.compile("\\bEndPackage\\s*+\\[\\s*+\\]");

    // Pattern for Begin/End
    private static final Pattern BEGIN_CONTEXT = Pattern.compile("\\bBegin\\s*+\\[");
    private static final Pattern END_CONTEXT = Pattern.compile("\\bEnd\\s*+\\[\\s*+\\]");

    // Pattern for Global` context
    private static final Pattern GLOBAL_CONTEXT = Pattern.compile("\\bGlobal`\\w++");

    // Pattern for temp/tmp variables
    private static final Pattern TEMP_VAR = Pattern.compile("\\b(temp|tmp)\\s*+=");

    // Pattern for symbol names
    private static final Pattern SYMBOL_NAME = Pattern.compile("([a-zA-Z]\\w*+)\\s*+(?:=|:=|\\[)");

    // Pattern for camelCase, snake_case, PascalCase
    // FIXED: Use possessive quantifiers to prevent catastrophic backtracking
    private static final Pattern CAMEL_CASE = Pattern.compile("^[a-z]++(?:[A-Z][a-z0-9]*+)++$");
    private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z]++(?:_[a-z0-9]++)++$");
    // FIXED: Use possessive quantifier to prevent backtracking
    private static final Pattern PASCAL_CASE = Pattern.compile("^[A-Z][a-zA-Z0-9]*+$");

    // Pattern for Private` context
    private static final Pattern PRIVATE_CONTEXT = Pattern.compile("(\\w++)`Private`(\\w++)");

    // Common built-in functions
    private static final Set<String> BUILTIN_FUNCTIONS = createBuiltinSet();

    // Reserved system variables
    private static final Set<String> RESERVED_NAMES = createReservedSet();

    // ===== Pre-compiled patterns for Undefined Symbol Detection =====

    // Pattern for function calls
    private static final Pattern FUNCTION_CALL = Pattern.compile("([a-zA-Z]\\w*+)\\s*+\\[");

    // Pattern for variable references
    private static final Pattern VAR_REFERENCE = Pattern.compile("\\b([a-zA-Z]\\w*+)\\b");

    // Common typos in built-in names
    private static final Map<String, String> COMMON_TYPOS = createTypoMap();

    // Pattern for context references
    private static final Pattern CONTEXT_REF = Pattern.compile("(\\w++)`(\\w++)");

    // Pattern for Get[]
    private static final Pattern GET_PATTERN = Pattern.compile("\\bGet\\s*+\\[\\s*+\"([^\"]++)\"");

    private static Set<String> createBuiltinSet() {
        Set<String> builtins = new HashSet<>();
        // Add common built-in functions (subset for demonstration)
        builtins.add("List");
        builtins.add("Map");
        builtins.add("Apply");
        builtins.add("Select");
        builtins.add("Table");
        builtins.add("Do");
        builtins.add("Module");
        builtins.add("Block");
        builtins.add("With");
        builtins.add("Length");
        builtins.add("First");
        builtins.add("Last");
        builtins.add("Rest");
        builtins.add("Most");
        builtins.add("Join");
        builtins.add("Append");
        builtins.add("Prepend");
        builtins.add("Sort");
        builtins.add("Reverse");
        builtins.add("Flatten");
        builtins.add("Position");
        builtins.add("Extract");
        builtins.add("Part");
        builtins.add("Plot");
        builtins.add("Print");
        builtins.add("If");
        builtins.add("Which");
        builtins.add("Switch");
        builtins.add("Return");
        builtins.add("Throw");
        builtins.add("Catch");
        builtins.add("Abort");
        return builtins;
    }

    private static Set<String> createReservedSet() {
        Set<String> reserved = new HashSet<>();
        reserved.add("$SystemID");
        reserved.add("$Version");
        reserved.add("$MachineName");
        reserved.add("$ProcessID");
        reserved.add("$CommandLine");
        reserved.add("$Input");
        reserved.add("$Output");
        reserved.add("$Path");
        reserved.add("$ContextPath");
        reserved.add("$Context");
        reserved.add("$Failed");
        reserved.add("$Aborted");
        return reserved;
    }

    private static Map<String, String> createTypoMap() {
        Map<String, String> typos = new HashMap<>();
        typos.put("Lenght", "Length");
        typos.put("Frist", "First");
        typos.put("Lats", "Last");
        typos.put("Tabel", "Table");
        typos.put("Slect", "Select");
        typos.put("Apend", "Append");
        typos.put("Prepnd", "Prepend");
        typos.put("Rever", "Reverse");
        typos.put("Sorrt", "Sort");
        typos.put("Modul", "Module");
        typos.put("Blok", "Block");
        return typos;
    }

    // ===== UNUSED CODE DETECTION METHODS (Items 61-75) =====

    public void detectUnusedPrivateFunction(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find all function definitions
            Matcher defMatcher = FUNCTION_DEF.matcher(content);
            Set<String> definedFunctions = new HashSet<>();
            Map<String, Integer> functionLines = new HashMap<>();

            while (defMatcher.find()) {
                String funcName = defMatcher.group(1);
                definedFunctions.add(funcName);
                functionLines.put(funcName, calculateLineNumber(content, defMatcher.start()));
            }

            // Check which functions are never called
            for (String funcName : definedFunctions) {
                Pattern callPattern = Pattern.compile("\\b" + Pattern.quote(funcName) + "\\s*+\\[");
                Matcher callMatcher = callPattern.matcher(content);
                int callCount = 0;
                while (callMatcher.find()) {
                    callCount++;
                }

                // If function appears exactly once (definition only, no calls) and starts with lowercase
                // (convention for private functions)
                if (callCount == 1 && Character.isLowerCase(funcName.charAt(0))) {
                    int line = functionLines.get(funcName);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.UNUSED_PRIVATE_FUNCTION_KEY,
                        String.format("Private function '%s' is never called. Consider removing it.", funcName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused private functions in {}", inputFile.filename(), e);
        }
    }

    public void detectUnusedFunctionParameter(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF.matcher(content);
            while (matcher.find()) {
                String funcName = matcher.group(1);
                String params = matcher.group(2);

                // Extract parameter names
                String[] paramNames = params.split(",");
                for (String param : paramNames) {
                    param = param.trim();
                    // Extract pattern name (e.g., "x_" -> "x", "y_Integer" -> "y")
                    // Note: Cannot use possessive on \w* before _ since \w includes _
                    Matcher paramMatcher = Pattern.compile("([a-zA-Z]\\w*)_").matcher(param);
                    if (paramMatcher.find()) {
                        String paramName = paramMatcher.group(1);

                        // Find function body (simplified: look for content after :=)
                        int bodyStart = matcher.end();
                        int bodyEnd = findStatementEnd(content, bodyStart);
                        if (bodyEnd > bodyStart) {
                            String body = content.substring(bodyStart, Math.min(bodyEnd, content.length()));

                            // Check if parameter is used in body
                            if (!body.contains(paramName)) {
                                int line = calculateLineNumber(content, matcher.start());
                                reportIssue(context, inputFile, line,
                                    MathematicaRulesDefinition.UNUSED_FUNCTION_PARAMETER_KEY,
                                    String.format("Function '%s' has unused parameter '%s'. Consider using blank pattern '_'.",
                                        funcName, paramName));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused function parameters in {}", inputFile.filename(), e);
        }
    }

    public void detectUnusedModuleVariable(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MODULE_VARS.matcher(content);
            while (matcher.find()) {
                String vars = matcher.group(1);
                int moduleStart = matcher.end();

                // Find Module body
                int bodyEnd = findMatchingBracket(content, moduleStart);
                if (bodyEnd > moduleStart) {
                    String body = content.substring(moduleStart, bodyEnd);

                    // Check each variable
                    String[] varList = vars.split(",");
                    for (String var : varList) {
                        var = var.trim();
                        // Extract variable name (before = or alone)
                        String varName = var.split("\\s*+=")[0].trim();

                        if (!varName.isEmpty() && !body.contains(varName)) {
                            int line = calculateLineNumber(content, matcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.UNUSED_MODULE_VARIABLE_KEY,
                                String.format("Module variable '%s' is declared but never used.", varName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused Module variables in {}", inputFile.filename(), e);
        }
    }

    public void detectUnusedWithVariable(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = WITH_VARS.matcher(content);
            while (matcher.find()) {
                String vars = matcher.group(1);
                int withStart = matcher.end();

                // Find With body
                int bodyEnd = findMatchingBracket(content, withStart);
                if (bodyEnd > withStart) {
                    String body = content.substring(withStart, bodyEnd);

                    // Check each variable
                    String[] varList = vars.split(",");
                    for (String var : varList) {
                        var = var.trim();
                        // Extract variable name (before =)
                        String varName = var.split("\\s*+=")[0].trim();

                        if (!varName.isEmpty() && !body.contains(varName)) {
                            int line = calculateLineNumber(content, matcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.UNUSED_WITH_VARIABLE_KEY,
                                String.format("With variable '%s' is declared but never used.", varName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused With variables in {}", inputFile.filename(), e);
        }
    }

    public void detectUnusedImport(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NEEDS_IMPORT.matcher(content);
            while (matcher.find()) {
                String packageName = matcher.group(1);
                // Extract package context (e.g., "MyPackage`" -> "MyPackage")
                String packageContext = packageName.replace("`", "");

                // Check if any symbols from this package are used
                Pattern usagePattern = Pattern.compile("\\b" + Pattern.quote(packageContext) + "`\\w++");
                Matcher usageMatcher = usagePattern.matcher(content);

                // Count occurrences (skip the Needs line itself)
                int usageCount = 0;
                int needsLine = calculateLineNumber(content, matcher.start());
                while (usageMatcher.find()) {
                    int usageLine = calculateLineNumber(content, usageMatcher.start());
                    if (usageLine != needsLine) {
                        usageCount++;
                    }
                }

                if (usageCount == 0) {
                    reportIssue(context, inputFile, needsLine,
                        MathematicaRulesDefinition.UNUSED_IMPORT_KEY,
                        String.format("Package '%s' is imported but never used.", packageName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused imports in {}", inputFile.filename(), e);
        }
    }

    public void detectUnusedPatternName(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF.matcher(content);
            while (matcher.find()) {
                String funcName = matcher.group(1);
                String params = matcher.group(2);

                // Extract named patterns
                // Note: Cannot use possessive on \w* before _ since \w includes _
                Pattern namedPattern = Pattern.compile("([a-zA-Z]\\w*)_");
                Matcher nameMatcher = namedPattern.matcher(params);

                while (nameMatcher.find()) {
                    String paramName = nameMatcher.group(1);

                    // Find function body
                    int bodyStart = matcher.end();
                    int bodyEnd = findStatementEnd(content, bodyStart);
                    if (bodyEnd > bodyStart) {
                        String body = content.substring(bodyStart, Math.min(bodyEnd, content.length()));

                        // Check if pattern name is used
                        if (!body.contains(paramName)) {
                            int line = calculateLineNumber(content, matcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.UNUSED_PATTERN_NAME_KEY,
                                String.format("Pattern name '%s' is unused. Use blank pattern '_' instead.", paramName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused pattern names in {}", inputFile.filename(), e);
        }
    }

    public void detectUnusedOptionalParameter(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = OPTIONAL_PARAM.matcher(content);
            while (matcher.find()) {
                String paramName = matcher.group(1);

                // Find the function context
                int funcStart = content.lastIndexOf(":=", matcher.start());
                if (funcStart > 0) {
                    int funcEnd = findStatementEnd(content, matcher.end());
                    String functionBody = content.substring(funcStart, Math.min(funcEnd, content.length()));

                    // Check if optional parameter is used
                    if (!functionBody.contains(paramName) || functionBody.indexOf(paramName) == matcher.start() - funcStart) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.UNUSED_OPTIONAL_PARAMETER_KEY,
                            String.format("Optional parameter '%s' is never used.", paramName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused optional parameters in {}", inputFile.filename(), e);
        }
    }

    public void detectDeadCodeAfterReturn(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = RETURN_STATEMENT.matcher(content);
            while (matcher.find()) {
                int returnPos = matcher.start();
                int returnEnd = findMatchingBracket(content, matcher.end());

                // Check if there's code after the Return in the same scope
                // Look for the next semicolon or statement
                int nextStatementStart = returnEnd + 1;
                while (nextStatementStart < content.length()
                       && Character.isWhitespace(content.charAt(nextStatementStart))) {
                    nextStatementStart++;
                }

                if (nextStatementStart < content.length()) {
                    char nextChar = content.charAt(nextStatementStart);
                    // If next char is not a closing bracket/paren, there's dead code
                    if (nextChar != ')' && nextChar != ']' && nextChar != '}') {
                        int line = calculateLineNumber(content, nextStatementStart);
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.DEAD_AFTER_RETURN_KEY,
                            "Code after Return statement is unreachable.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting dead code after Return in {}", inputFile.filename(), e);
        }
    }

    public void detectUnreachableAfterAbortThrow(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ABORT_THROW.matcher(content);
            while (matcher.find()) {
                String statement = matcher.group(1);
                int statementPos = matcher.start();
                int statementEnd = findMatchingBracket(content, matcher.end());

                // Check if there's code after Abort/Throw
                int nextStatementStart = statementEnd + 1;
                while (nextStatementStart < content.length()
                       && Character.isWhitespace(content.charAt(nextStatementStart))) {
                    nextStatementStart++;
                }

                if (nextStatementStart < content.length()) {
                    char nextChar = content.charAt(nextStatementStart);
                    if (nextChar == ';') {
                        // There's a statement after semicolon
                        int line = calculateLineNumber(content, nextStatementStart);
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.UNREACHABLE_AFTER_ABORT_THROW_KEY,
                            String.format("Code after %s[] is unreachable.", statement));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unreachable code after Abort/Throw in {}", inputFile.filename(), e);
        }
    }

    public void detectAssignmentNeverRead(SensorContext context, InputFile inputFile, String content) {
        try {
            // Simplified detection: look for patterns like x = val1; x = val2 where val1 is never used
            Pattern assignment = Pattern.compile("\\b(\\w++)\\s*+=\\s*+([^;]++);");
            Matcher matcher = assignment.matcher(content);

            Map<String, Integer> lastAssignment = new HashMap<>();

            while (matcher.find()) {
                String varName = matcher.group(1);
                int line = calculateLineNumber(content, matcher.start());

                // If variable was assigned before and not used between, flag it
                if (lastAssignment.containsKey(varName)) {
                    int prevLine = lastAssignment.get(varName);
                    reportIssue(context, inputFile, prevLine,
                        MathematicaRulesDefinition.ASSIGNMENT_NEVER_READ_KEY,
                        String.format("Assignment to '%s' is never read before being overwritten.", varName));
                }

                lastAssignment.put(varName, line);
            }
        } catch (Exception e) {
            LOG.debug("Error detecting assignments never read in {}", inputFile.filename(), e);
        }
    }

    public void detectFunctionDefinedButNeverCalled(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF.matcher(content);
            Set<String> definedFunctions = new HashSet<>();
            Map<String, Integer> functionLines = new HashMap<>();

            while (matcher.find()) {
                String funcName = matcher.group(1);
                // Only check public functions (starting with uppercase)
                if (Character.isUpperCase(funcName.charAt(0))) {
                    definedFunctions.add(funcName);
                    functionLines.put(funcName, calculateLineNumber(content, matcher.start()));
                }
            }

            // Check which functions are never called
            for (String funcName : definedFunctions) {
                Pattern callPattern = Pattern.compile("\\b" + Pattern.quote(funcName) + "\\s*+\\[");
                Matcher callMatcher = callPattern.matcher(content);
                int callCount = 0;
                while (callMatcher.find()) {
                    callCount++;
                }

                // If function appears exactly once (definition only)
                if (callCount == 1) {
                    int line = functionLines.get(funcName);
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.FUNCTION_DEFINED_NEVER_CALLED_KEY,
                        String.format("Global function '%s' is defined but never called.", funcName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting functions defined but never called in {}", inputFile.filename(), e);
        }
    }

    public void detectRedefinedWithoutUse(SensorContext context, InputFile inputFile, String content) {
        try {
            Pattern assignment = Pattern.compile("\\b(\\w+)\\s*+=");
            Matcher matcher = assignment.matcher(content);

            Map<String, Integer> firstAssignment = new HashMap<>();
            Set<String> usedVariables = new HashSet<>();

            int lastAssignmentEnd = 0;
            String lastVarName = null;

            while (matcher.find()) {
                String varName = matcher.group(1);
                int currentPos = matcher.start();

                // Check if last variable was used between assignments
                if (lastVarName != null && varName.equals(lastVarName)) {
                    String betweenCode = content.substring(lastAssignmentEnd, currentPos);
                    if (!betweenCode.contains(varName)) {
                        int line = firstAssignment.get(varName);
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.REDEFINED_WITHOUT_USE_KEY,
                            String.format("Variable '%s' redefined without using previous value.", varName));
                    }
                }

                firstAssignment.put(varName, calculateLineNumber(content, matcher.start()));
                lastAssignmentEnd = matcher.end();
                lastVarName = varName;
            }
        } catch (Exception e) {
            LOG.debug("Error detecting redefined without use in {}", inputFile.filename(), e);
        }
    }

    public void detectLoopVariableUnused(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DO_LOOP_ITERATOR.matcher(content);
            while (matcher.find()) {
                String iteratorName = matcher.group(2);
                int loopStart = matcher.start();

                // Find loop body
                int bodyStart = content.indexOf(",", matcher.end());
                if (bodyStart > 0) {
                    bodyStart = content.indexOf("{", bodyStart);
                    if (bodyStart > 0) {
                        int bodyEnd = findMatchingBracket(content, bodyStart + 1);
                        String loopBody = content.substring(matcher.start(), Math.min(bodyEnd, content.length()));

                        // Count occurrences of iterator (should be at least 2: declaration + use)
                        int occurrences = 0;
                        int index = loopBody.indexOf(iteratorName);
                        while (index >= 0) {
                            occurrences++;
                            index = loopBody.indexOf(iteratorName, index + 1);
                        }

                        // If iterator appears only once (in declaration), it's unused
                        if (occurrences == 1) {
                            int line = calculateLineNumber(content, loopStart);
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.LOOP_VARIABLE_UNUSED_KEY,
                                String.format("Loop iterator '%s' is never used in loop body. Use Do[..., n] form instead.",
                                    iteratorName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting unused loop variables in {}", inputFile.filename(), e);
        }
    }

    public void detectCatchWithoutThrow(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher catchMatcher = CATCH_PATTERN.matcher(content);
            while (catchMatcher.find()) {
                int catchStart = catchMatcher.start();
                int catchEnd = findMatchingBracket(content, catchMatcher.end());

                if (catchEnd > catchStart) {
                    String catchBody = content.substring(catchStart, catchEnd);

                    // Check if there's a Throw in the catch body
                    if (!THROW_PATTERN.matcher(catchBody).find()) {
                        int line = calculateLineNumber(content, catchStart);
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.CATCH_WITHOUT_THROW_KEY,
                            "Catch statement has no corresponding Throw in its body.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Catch without Throw in {}", inputFile.filename(), e);
        }
    }

    public void detectConditionAlwaysFalse(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = IF_FALSE.matcher(content);
            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.CONDITION_ALWAYS_FALSE_KEY,
                    "Condition is always False. This branch never executes.");
            }
        } catch (Exception e) {
            LOG.debug("Error detecting always-false conditions in {}", inputFile.filename(), e);
        }
    }

    // ===== SHADOWING & NAMING METHODS (Items 76-90) =====

    public void detectLocalShadowsGlobal(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find global variable assignments (outside Module/Block/With)
            Set<String> globalVars = new HashSet<>();
            Pattern globalAssignment = Pattern.compile("^\\s*+([a-zA-Z]\\w*)\\s*+=", Pattern.MULTILINE);
            Matcher globalMatcher = globalAssignment.matcher(content);

            while (globalMatcher.find()) {
                String varName = globalMatcher.group(1);
                globalVars.add(varName);
            }

            // Check Module/Block/With for shadowing
            Matcher moduleMatcher = MODULE_VARS.matcher(content);
            while (moduleMatcher.find()) {
                String vars = moduleMatcher.group(1);
                String[] varList = vars.split(",");
                for (String var : varList) {
                    String varName = var.trim().split("\\s*+=")[0].trim();
                    if (globalVars.contains(varName)) {
                        int line = calculateLineNumber(content, moduleMatcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.LOCAL_SHADOWS_GLOBAL_KEY,
                            String.format("Local variable '%s' shadows global variable.", varName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting local shadows global in {}", inputFile.filename(), e);
        }
    }

    public void detectParameterShadowsBuiltin(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF.matcher(content);
            while (matcher.find()) {
                String params = matcher.group(2);

                // Extract parameter names
                Pattern paramPattern = Pattern.compile("([a-zA-Z]\\w*)_");
                Matcher paramMatcher = paramPattern.matcher(params);

                while (paramMatcher.find()) {
                    String paramName = paramMatcher.group(1);
                    if (BUILTIN_FUNCTIONS.contains(paramName)) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.PARAMETER_SHADOWS_BUILTIN_KEY,
                            String.format("Parameter '%s' shadows built-in function. Use lowercase name.", paramName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting parameter shadows built-in in {}", inputFile.filename(), e);
        }
    }

    public void detectLocalShadowsParameter(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher funcMatcher = FUNCTION_DEF.matcher(content);
            while (funcMatcher.find()) {
                String params = funcMatcher.group(2);

                // Extract parameter names
                Set<String> paramNames = new HashSet<>();
                Pattern paramPattern = Pattern.compile("([a-zA-Z]\\w*)_");
                Matcher paramMatcher = paramPattern.matcher(params);
                while (paramMatcher.find()) {
                    paramNames.add(paramMatcher.group(1));
                }

                // Find Module/Block/With in function body
                int bodyStart = funcMatcher.end();
                int bodyEnd = findStatementEnd(content, bodyStart);
                String body = content.substring(bodyStart, Math.min(bodyEnd, content.length()));

                Matcher moduleMatcher = MODULE_VARS.matcher(body);
                while (moduleMatcher.find()) {
                    String vars = moduleMatcher.group(1);
                    String[] varList = vars.split(",");
                    for (String var : varList) {
                        String varName = var.trim().split("\\s*+=")[0].trim();
                        if (paramNames.contains(varName)) {
                            int line = calculateLineNumber(content, bodyStart + moduleMatcher.start());
                            reportIssue(context, inputFile, line,
                                MathematicaRulesDefinition.LOCAL_SHADOWS_PARAMETER_KEY,
                                String.format("Local variable '%s' shadows function parameter.", varName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting local shadows parameter in {}", inputFile.filename(), e);
        }
    }

    public void detectMultipleDefinitionsSameSymbol(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FUNCTION_DEF.matcher(content);
            Map<String, Integer> functionDefinitions = new HashMap<>();

            while (matcher.find()) {
                String funcName = matcher.group(1);

                if (functionDefinitions.containsKey(funcName)) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.MULTIPLE_DEFINITIONS_SAME_SYMBOL_KEY,
                        String.format("Symbol '%s' is defined multiple times. This may be intentional (pattern overloading) or an error.",
                            funcName));
                }
                functionDefinitions.put(funcName, calculateLineNumber(content, matcher.start()));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting multiple definitions in {}", inputFile.filename(), e);
        }
    }

    public void detectSymbolNameTooShort(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find functions longer than 50 lines with single-letter variables
            Matcher funcMatcher = FUNCTION_DEF.matcher(content);
            while (funcMatcher.find()) {
                int funcStart = funcMatcher.start();
                int funcEnd = findStatementEnd(content, funcMatcher.end());
                String funcBody = content.substring(funcStart, Math.min(funcEnd, content.length()));

                // Count lines in function
                int lineCount = funcBody.split("\n").length;

                if (lineCount > 50) {
                    // Look for single-letter variable names
                    Pattern singleLetterVar = Pattern.compile("\\b([a-z])\\s*+=");
                    Matcher varMatcher = singleLetterVar.matcher(funcBody);

                    if (varMatcher.find()) {
                        int line = calculateLineNumber(content, funcStart + varMatcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.SYMBOL_NAME_TOO_SHORT_KEY,
                            "Single-letter variable names reduce readability in large functions.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting symbols with names too short in {}", inputFile.filename(), e);
        }
    }

    public void detectSymbolNameTooLong(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SYMBOL_NAME.matcher(content);
            while (matcher.find()) {
                String symbolName = matcher.group(1);
                if (symbolName.length() > 50) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.SYMBOL_NAME_TOO_LONG_KEY,
                        String.format("Symbol name '%s' is too long (%d characters). Keep names under 50 characters.",
                            symbolName, symbolName.length()));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting symbols with names too long in {}", inputFile.filename(), e);
        }
    }

    public void detectInconsistentNamingConvention(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SYMBOL_NAME.matcher(content);
            Set<String> conventions = new HashSet<>();

            while (matcher.find()) {
                String symbolName = matcher.group(1);

                if (CAMEL_CASE.matcher(symbolName).matches()) {
                    conventions.add("camelCase");
                } else if (SNAKE_CASE.matcher(symbolName).matches()) {
                    conventions.add("snake_case");
                } else if (PASCAL_CASE.matcher(symbolName).matches()) {
                    conventions.add("PascalCase");
                }
            }

            if (conventions.size() > 1) {
                reportIssue(context, inputFile, 1,
                    MathematicaRulesDefinition.INCONSISTENT_NAMING_CONVENTION_KEY,
                    String.format("File uses inconsistent naming conventions: %s. Stick to one convention.",
                        String.join(", ", conventions)));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting inconsistent naming conventions in {}", inputFile.filename(), e);
        }
    }

    public void detectBuiltinNameInLocalScope(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = MODULE_VARS.matcher(content);
            while (matcher.find()) {
                String vars = matcher.group(1);
                String[] varList = vars.split(",");

                for (String var : varList) {
                    String varName = var.trim().split("\\s*+=")[0].trim();
                    if (BUILTIN_FUNCTIONS.contains(varName)) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.BUILTIN_NAME_IN_LOCAL_SCOPE_KEY,
                            String.format("Built-in function name '%s' used as local variable.", varName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting built-in names in local scope in {}", inputFile.filename(), e);
        }
    }

    public void detectContextConflicts(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CONTEXT_REF.matcher(content);
            Map<String, Set<String>> symbolContexts = new HashMap<>();

            while (matcher.find()) {
                String context1 = matcher.group(1);
                String symbol = matcher.group(2);

                symbolContexts.putIfAbsent(symbol, new HashSet<>());
                symbolContexts.get(symbol).add(context1);
            }

            // Check for symbols defined in multiple contexts
            for (Map.Entry<String, Set<String>> entry : symbolContexts.entrySet()) {
                if (entry.getValue().size() > 1) {
                    reportIssue(context, inputFile, 1,
                        MathematicaRulesDefinition.CONTEXT_CONFLICTS_KEY,
                        String.format("Symbol '%s' is defined in multiple contexts: %s",
                            entry.getKey(), String.join(", ", entry.getValue())));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting context conflicts in {}", inputFile.filename(), e);
        }
    }

    public void detectReservedNameUsage(SensorContext context, InputFile inputFile, String content) {
        try {
            for (String reserved : RESERVED_NAMES) {
                Pattern pattern = Pattern.compile("\\b" + Pattern.quote(reserved) + "\\s*+=");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.RESERVED_NAME_USAGE_KEY,
                        String.format("Reserved system variable '%s' should not be assigned.", reserved));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting reserved name usage in {}", inputFile.filename(), e);
        }
    }

    public void detectPrivateContextSymbolPublic(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PRIVATE_CONTEXT.matcher(content);
            while (matcher.find()) {
                String packageName = matcher.group(1);
                String symbolName = matcher.group(2);
                int line = calculateLineNumber(content, matcher.start());

                reportIssue(context, inputFile, line,
                    MathematicaRulesDefinition.PRIVATE_CONTEXT_SYMBOL_PUBLIC_KEY,
                    String.format("Private context symbol '%s`Private`%s' used from outside package.",
                        packageName, symbolName));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting private context symbols used publicly in {}", inputFile.filename(), e);
        }
    }

    public void detectMismatchedBeginEnd(SensorContext context, InputFile inputFile, String content) {
        try {
            int beginPackageCount = 0;
            int endPackageCount = 0;
            int beginContextCount = 0;
            int endContextCount = 0;

            Matcher beginPackageMatcher = BEGIN_PACKAGE.matcher(content);
            while (beginPackageMatcher.find()) {
                beginPackageCount++;
            }

            Matcher endPackageMatcher = END_PACKAGE.matcher(content);
            while (endPackageMatcher.find()) {
                endPackageCount++;
            }

            Matcher beginContextMatcher = BEGIN_CONTEXT.matcher(content);
            while (beginContextMatcher.find()) {
                beginContextCount++;
            }

            Matcher endContextMatcher = END_CONTEXT.matcher(content);
            while (endContextMatcher.find()) {
                endContextCount++;
            }

            if (beginPackageCount != endPackageCount) {
                reportIssue(context, inputFile, 1,
                    MathematicaRulesDefinition.MISMATCHED_BEGIN_END_KEY,
                    String.format("Mismatched BeginPackage/EndPackage: %d Begin, %d End",
                        beginPackageCount, endPackageCount));
            }

            if (beginContextCount != endContextCount) {
                reportIssue(context, inputFile, 1,
                    MathematicaRulesDefinition.MISMATCHED_BEGIN_END_KEY,
                    String.format("Mismatched Begin/End: %d Begin, %d End",
                        beginContextCount, endContextCount));
            }
        } catch (Exception e) {
            LOG.debug("Error detecting mismatched Begin/End in {}", inputFile.filename(), e);
        }
    }

    public void detectSymbolAfterEndPackage(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher endPackageMatcher = END_PACKAGE.matcher(content);
            if (endPackageMatcher.find()) {
                int endPackagePos = endPackageMatcher.end();
                String afterEndPackage = content.substring(endPackagePos);

                // Look for function definitions after EndPackage
                Matcher funcMatcher = FUNCTION_DEF.matcher(afterEndPackage);
                if (funcMatcher.find()) {
                    int line = calculateLineNumber(content, endPackagePos + funcMatcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.SYMBOL_AFTER_ENDPACKAGE_KEY,
                        "Symbol defined after EndPackage[] is in wrong context.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting symbols after EndPackage in {}", inputFile.filename(), e);
        }
    }

    public void detectGlobalInPackage(SensorContext context, InputFile inputFile, String content) {
        try {
            // Check if file is a package (has BeginPackage)
            if (BEGIN_PACKAGE.matcher(content).find()) {
                Matcher globalMatcher = GLOBAL_CONTEXT.matcher(content);
                while (globalMatcher.find()) {
                    int line = calculateLineNumber(content, globalMatcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.GLOBAL_IN_PACKAGE_KEY,
                        "Global` context should not be used in package code.");
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting Global context in package in {}", inputFile.filename(), e);
        }
    }

    public void detectTempVariableNotTemp(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = TEMP_VAR.matcher(content);
            Map<String, Integer> tempVarCount = new HashMap<>();

            while (matcher.find()) {
                String varName = matcher.group(1);
                tempVarCount.put(varName, tempVarCount.getOrDefault(varName, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : tempVarCount.entrySet()) {
                if (entry.getValue() > 2) {
                    reportIssue(context, inputFile, 1,
                        MathematicaRulesDefinition.TEMP_VARIABLE_NOT_TEMP_KEY,
                        String.format("Variable '%s' named 'temp/tmp' but used %d times. Give it a descriptive name.",
                            entry.getKey(), entry.getValue()));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting temp variables not temp in {}", inputFile.filename(), e);
        }
    }

    // ===== UNDEFINED SYMBOL DETECTION METHODS (Items 91-100) =====

    public void detectUndefinedFunctionCall(SensorContext context, InputFile inputFile, String content) {
        try {
            Set<String> definedFunctions = new HashSet<>(BUILTIN_FUNCTIONS);

            // Collect all function definitions
            Matcher defMatcher = FUNCTION_DEF.matcher(content);
            while (defMatcher.find()) {
                definedFunctions.add(defMatcher.group(1));
            }

            // Check all function calls
            Matcher callMatcher = FUNCTION_CALL.matcher(content);
            while (callMatcher.find()) {
                String funcName = callMatcher.group(1);
                if (!definedFunctions.contains(funcName) && !funcName.matches("^[a-z]$")) {
                    int line = calculateLineNumber(content, callMatcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.UNDEFINED_FUNCTION_CALL_KEY,
                        String.format("Call to undefined function '%s'.", funcName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting undefined function calls in {}", inputFile.filename(), e);
        }
    }

    public void detectUndefinedVariableReference(SensorContext context, InputFile inputFile, String content) {
        try {
            Set<String> definedVars = new HashSet<>();

            // Collect all variable assignments
            Pattern assignment = Pattern.compile("\\b([a-zA-Z]\\w*)\\s*+=");
            Matcher assignMatcher = assignment.matcher(content);
            while (assignMatcher.find()) {
                definedVars.add(assignMatcher.group(1));
            }

            // Check for references to undefined variables (simplified)
            // This is a basic heuristic and would need data flow analysis for accuracy
        } catch (Exception e) {
            LOG.debug("Error detecting undefined variable references in {}", inputFile.filename(), e);
        }
    }

    public void detectTypoInBuiltinName(SensorContext context, InputFile inputFile, String content) {
        try {
            for (Map.Entry<String, String> entry : COMMON_TYPOS.entrySet()) {
                String typo = entry.getKey();
                String correct = entry.getValue();

                Pattern typoPattern = Pattern.compile("\\b" + Pattern.quote(typo) + "\\s*+\\[");
                Matcher matcher = typoPattern.matcher(content);

                if (matcher.find()) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.TYPO_IN_BUILTIN_NAME_KEY,
                        String.format("Possible typo: '%s' should be '%s'.", typo, correct));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting typos in built-in names in {}", inputFile.filename(), e);
        }
    }

    public void detectWrongCapitalization(SensorContext context, InputFile inputFile, String content) {
        try {
            // Check for lowercase versions of common built-ins
            for (String builtin : BUILTIN_FUNCTIONS) {
                String lowercase = builtin.toLowerCase();
                if (!lowercase.equals(builtin)) {
                    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(lowercase) + "\\s*+\\[");
                    Matcher matcher = pattern.matcher(content);

                    if (matcher.find()) {
                        int line = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.WRONG_CAPITALIZATION_KEY,
                            String.format("Wrong capitalization: '%s' should be '%s'.", lowercase, builtin));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting wrong capitalization in {}", inputFile.filename(), e);
        }
    }

    public void detectMissingImport(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find context-qualified symbols
            Matcher contextMatcher = CONTEXT_REF.matcher(content);
            Set<String> usedContexts = new HashSet<>();

            while (contextMatcher.find()) {
                String contextName = contextMatcher.group(1);
                usedContexts.add(contextName);
            }

            // Find imported contexts
            Matcher needsMatcher = NEEDS_IMPORT.matcher(content);
            Set<String> importedContexts = new HashSet<>();

            while (needsMatcher.find()) {
                String packageName = needsMatcher.group(1).replace("`", "");
                importedContexts.add(packageName);
            }

            // Check for contexts used but not imported
            for (String usedContext : usedContexts) {
                if (!importedContexts.contains(usedContext)) {
                    reportIssue(context, inputFile, 1,
                        MathematicaRulesDefinition.MISSING_IMPORT_KEY,
                        String.format("Package '%s' is used but not imported with Needs[].", usedContext));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting missing imports in {}", inputFile.filename(), e);
        }
    }

    public void detectContextNotFound(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NEEDS_IMPORT.matcher(content);
            while (matcher.find()) {
                String packageName = matcher.group(1);
                // In a real implementation, would check if package exists on filesystem
                // For now, just flag suspicious patterns
                if (packageName.contains("Test") || packageName.contains("Debug")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.CONTEXT_NOT_FOUND_KEY,
                        String.format("Suspicious package name '%s' - verify it exists.", packageName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting non-existent contexts in {}", inputFile.filename(), e);
        }
    }

    public void detectSymbolMaskedByImport(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find local function definitions
            Set<String> localFunctions = new HashSet<>();
            Matcher funcMatcher = FUNCTION_DEF.matcher(content);
            while (funcMatcher.find()) {
                localFunctions.add(funcMatcher.group(1));
            }

            // Find imported packages (would need to check what they export)
            Matcher needsMatcher = NEEDS_IMPORT.matcher(content);
            while (needsMatcher.find()) {
                int line = calculateLineNumber(content, needsMatcher.start());
                String packageName = needsMatcher.group(1);

                // Warn about potential masking
                if (!localFunctions.isEmpty()) {
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.SYMBOL_MASKED_BY_IMPORT_KEY,
                        String.format("Importing '%s' may mask local symbols. Check for conflicts.", packageName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting symbols masked by import in {}", inputFile.filename(), e);
        }
    }

    public void detectMissingPathEntry(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = GET_PATTERN.matcher(content);
            while (matcher.find()) {
                String filename = matcher.group(1);
                // Check if filename is relative (no directory separators or FileNameJoin)
                if (!filename.contains("/") && !filename.contains("\\") && !content.contains("FileNameJoin")) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.MISSING_PATH_ENTRY_KEY,
                        String.format("File '%s' loaded with Get[] may not be in $Path. Use absolute path.", filename));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting missing $Path entries in {}", inputFile.filename(), e);
        }
    }

    public void detectCircularNeeds(SensorContext context, InputFile inputFile, String content) {
        try {
            // Simplified detection: check if file imports a package with similar name
            String fileName = inputFile.filename();
            String baseName = fileName.replace(".m", "").replace(".wl", "");

            Matcher matcher = NEEDS_IMPORT.matcher(content);
            while (matcher.find()) {
                String packageName = matcher.group(1);
                if (packageName.contains(baseName)) {
                    int line = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, line,
                        MathematicaRulesDefinition.CIRCULAR_NEEDS_KEY,
                        String.format("Possible circular dependency: file '%s' imports package '%s'.",
                            fileName, packageName));
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting circular Needs in {}", inputFile.filename(), e);
        }
    }

    public void detectForwardReferenceWithoutDeclaration(SensorContext context, InputFile inputFile, String content) {
        try {
            // Find all function definitions and calls in order
            Map<String, Integer> firstCall = new HashMap<>();
            Map<String, Integer> firstDef = new HashMap<>();

            Matcher callMatcher = FUNCTION_CALL.matcher(content);
            while (callMatcher.find()) {
                String funcName = callMatcher.group(1);
                int pos = callMatcher.start();
                if (!firstCall.containsKey(funcName)) {
                    firstCall.put(funcName, pos);
                }
            }

            Matcher defMatcher = FUNCTION_DEF.matcher(content);
            while (defMatcher.find()) {
                String funcName = defMatcher.group(1);
                int pos = defMatcher.start();
                if (!firstDef.containsKey(funcName)) {
                    firstDef.put(funcName, pos);
                }
            }

            // Check for forward references
            for (Map.Entry<String, Integer> entry : firstCall.entrySet()) {
                String funcName = entry.getKey();
                int callPos = entry.getValue();

                if (firstDef.containsKey(funcName)) {
                    int defPos = firstDef.get(funcName);
                    if (callPos < defPos && !BUILTIN_FUNCTIONS.contains(funcName)) {
                        int line = calculateLineNumber(content, callPos);
                        reportIssue(context, inputFile, line,
                            MathematicaRulesDefinition.FORWARD_REFERENCE_WITHOUT_DECLARATION_KEY,
                            String.format("Forward reference to '%s' without explicit declaration. "
                                + "Add 'funcName[args_];' before first use.", funcName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("Error detecting forward references in {}", inputFile.filename(), e);
        }
    }

    // ===== Helper methods =====

    private int findStatementEnd(String content, int start) {
        // Find the next semicolon or newline
        int semicolon = content.indexOf(';', start);
        int newline = content.indexOf('\n', start);

        if (semicolon < 0 && newline < 0) {
            return content.length();
        } else if (semicolon < 0) {
            return newline;
        } else if (newline < 0) {
            return semicolon;
        } else {
            return Math.min(semicolon, newline);
        }
    }

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
                return start;
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
