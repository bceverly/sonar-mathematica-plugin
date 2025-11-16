package org.sonar.plugins.mathematica.ast;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition;
import org.sonar.plugins.mathematica.rules.MathematicaRulesSensor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unified visitor that checks ALL ~400 rules in a single AST traversal.
 *
 * This eliminates 400+ regex scans by checking all rules during one tree walk.
 *
 * PERFORMANCE: O(n) single pass instead of O(400n) sequential regex scans.
 */
public class UnifiedRuleVisitor implements AstVisitor {

    private static final String FUNCTION_PREFIX = "Function '";
    private static final String IMPORT = "Import";
    private static final String EXPORT = "Export";
    private static final String RANDOM = "Random";
    private static final String RANDOM_INTEGER = "RandomInteger";
    private static final String TO_EXPRESSION = "ToExpression";
    private static final String CLOUD_DEPLOY = "CloudDeploy";
    private static final String GET = "Get";
    private static final String PART = "Part";
    private static final String PUT = "Put";
    private static final String REVIEW_PREFIX = "Review: ";

    // Common ECL and domain-specific lightweight functions
    private static final Set<String> ECL_LIGHTWEIGHT_FUNCTIONS = Set.of(
        "Download", "Upload", "Test", "Example", "ValidQ",
        "Unitless", "Widget", "Field", "Adder", "ToList",
        "DefineOptions", "ReplaceRule", "SamplesIn",
        "Lookup", "SafeLookup", "Packet"
    );

    // Mathematica built-in functions from System` context
    // Based on Wolfram Language documentation - ~6000+ built-in functions
    // Only truly expensive functions (Solve, Integrate, NDSolve, etc.) should be flagged for caching
    private static final Set<String> MATHEMATICA_BUILTIN_FUNCTIONS = createBuiltinFunctionsSet();

    private final org.sonar.api.batch.sensor.SensorContext context;
    private final InputFile inputFile;
    private final MathematicaRulesSensor sensor;

    // State tracking for various analyses
    private final Set<String> definedFunctions = new HashSet<>();
    private final Set<String> calledFunctions = new HashSet<>();
    private final Map<String, java.util.List<Integer>> functionCallPositions = new HashMap<>();
    private final Map<String, Integer> variableAssignments = new HashMap<>();
    private final Map<String, Integer> operatorCounts = new HashMap<>();

    // Scope tracking
    private final Deque<Set<String>> scopeStack = new ArrayDeque<>();

    public UnifiedRuleVisitor(org.sonar.api.batch.sensor.SensorContext context, InputFile inputFile,
                             MathematicaRulesSensor sensor) {
        this.context = context;
        this.inputFile = inputFile;
        this.sensor = sensor;

        // Initialize global scope
        scopeStack.push(new HashSet<>());
    }

    @Override
    public void visit(FunctionDefNode node) {
        String funcName = node.getFunctionName();
        definedFunctions.add(funcName);

        // Check for function naming conventions
        checkFunctionNaming(node, funcName);

        // Check for long functions (count lines in body)
        checkLongFunction(node);

        // Check function complexity
        checkFunctionComplexity(node);

        // Visit children
        visitChildren(node);
    }

    @Override
    public void visit(FunctionCallNode node) {
        String funcName = node.getFunctionName();
        calledFunctions.add(funcName);

        // Track call positions for repeated call detection
        // Only track if arguments are empty or all literals (for identical call detection)
        String signature = buildFunctionSignature(node);
        if (signature != null) {
            functionCallPositions.computeIfAbsent(signature, k -> new java.util.ArrayList<>())
                .add(node.getStartLine());
        }

        // Check for specific vulnerability patterns
        checkCommandInjection(node);
        checkSqlInjection(node);
        checkCodeInjection(node);
        checkPathTraversal(node);
        checkWeakCryptography(node);
        checkSSRF(node);
        checkInsecureDeserialization(node);
        checkUnsafeSymbol(node);
        checkXXE(node);
        checkMissingSanitization();
        checkInsecureRandom(node);
        checkUnsafeCloudDeploy(node);
        checkDynamicInjection(node);
        checkToExpressionOnInput(node);
        checkUnsanitizedRunProcess(node);
        checkMissingCloudAuth(node);
        checkHardcodedApiKeys();
        checkNeedsGetUntrusted(node);
        checkExposingSensitiveData(node);
        checkMissingFormFunctionValidation(node);

        // Security Hotspots (7 rules)
        checkFileUploadValidation(node);
        checkExternalApiSafeguards(node);
        checkCryptoKeyGeneration(node);
        checkNetworkOperations(node);
        checkFileSystemModifications(node);
        checkEnvironmentVariable(node);
        checkImportWithoutFormat(node);

        // Check for deprecated functions
        checkDeprecatedFunction(node, funcName);

        // Check for list operations
        checkListIndexOutOfBounds(node);
        checkAssociationVsListConfusion(node);

        // Check for inefficient patterns
        checkInefficientKeyLookup(node);
        checkUseTableInsteadOfMap(node);
        checkUseAssociationInsteadOfList(node);

        // Visit children
        visitChildren(node);
    }

    @Override
    public void visit(AssignmentNode node) {
        if (node.getLhs() instanceof IdentifierNode) {
            String varName = ((IdentifierNode) node.getLhs()).getName();
            variableAssignments.merge(varName, 1, Integer::sum);
        }

        visitChildren(node);
    }

    @Override
    public void visit(OperatorNode node) {
        String op = node.getOperatorSymbol();
        operatorCounts.merge(op, 1, Integer::sum);

        // Check for expression complexity based on operator count
        checkExpressionComplexity(node);

        visitChildren(node);
    }

    @Override
    public void visit(LiteralNode node) {
        // Check for hardcoded credentials in string literals
        if (node.getValue() instanceof String) {
            String value = (String) node.getValue();
            checkHardcodedCredentials(node, value);

            // Check for magic numbers/strings
            checkMagicNumber(node);
        }
    }

    @Override
    public void visit(IdentifierNode node) {
        // Check for undefined function calls
        checkUndefinedIdentifier(node);
    }

    // ========== Helper method to traverse children ==========

    @Override
    public void visitChildren(AstNode node) {
        for (AstNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    // ========== Post-traversal analysis ==========

    /**
     * Call this after tree traversal to run whole-file analyses.
     */
    public void performPostTraversalChecks() {
        // Check for functions defined but never called
        for (String funcName : definedFunctions) {
            if (!calledFunctions.contains(funcName)) {
                reportIssue(1, MathematicaRulesDefinition.FUNCTION_DEFINED_NEVER_CALLED_KEY,
                    FUNCTION_PREFIX + funcName + "' is defined but never called.");
            }
        }

        // Check for repeated function calls with identical arguments
        // Only flags when same function is called with identical constant arguments
        for (Map.Entry<String, java.util.List<Integer>> entry : functionCallPositions.entrySet()) {
            String signature = entry.getKey();
            String funcName = extractFunctionName(signature);

            if (entry.getValue().size() > 3 && !isPropertyAccessorOrLightweight(funcName)) {
                java.util.List<Integer> allLineNumbers = entry.getValue();
                int primaryLine = allLineNumbers.get(0);
                String message = "Function call '" + signature + "' is repeated " + allLineNumbers.size()
                    + " times with identical arguments. Consider caching the result.";

                // Report with secondary locations showing all duplicates
                reportIssueWithSecondaryLocations(primaryLine,
                    MathematicaRulesDefinition.REPEATED_FUNCTION_CALLS_KEY, message, allLineNumbers);
            }
        }
    }

    /**
     * Builds a function signature string including arguments if all are literals.
     * Returns null if arguments contain non-literals (variables, expressions).
     * This ensures we only flag repeated calls with identical constant arguments.
     *
     * Examples:
     * - DatabaseConnection[] -> "DatabaseConnection[]"
     * - Solve[x^2 + 1 == 0, x] -> null (contains variables)
     * - Sqrt[2] -> "Sqrt[2]"
     * - Sqrt[x] -> null (contains variable)
     */
    private String buildFunctionSignature(FunctionCallNode node) {
        String funcName = node.getFunctionName();
        List<AstNode> args = node.getArguments();

        // No arguments - track it
        if (args == null || args.isEmpty()) {
            return funcName + "[]";
        }

        // Check if all arguments are literals
        StringBuilder signature = new StringBuilder(funcName);
        signature.append("[");

        for (int i = 0; i < args.size(); i++) {
            AstNode arg = args.get(i);

            // Only track if argument is a literal
            if (!(arg instanceof LiteralNode)) {
                return null; // Contains non-literal, don't track
            }

            LiteralNode literal = (LiteralNode) arg;
            if (i > 0) {
                signature.append(", ");
            }

            // Add literal value to signature
            Object value = literal.getValue();
            if (literal.getLiteralType() == LiteralNode.LiteralType.STRING) {
                signature.append("\"").append(value).append("\"");
            } else {
                signature.append(value);
            }
        }

        signature.append("]");
        return signature.toString();
    }

    /**
     * Extracts function name from a signature string.
     * Example: "Sqrt[2]" -> "Sqrt"
     */
    private String extractFunctionName(String signature) {
        int bracketIndex = signature.indexOf('[');
        if (bracketIndex > 0) {
            return signature.substring(0, bracketIndex);
        }
        return signature;
    }

    // ========== Rule Implementation Methods ==========

    /**
     * Checks if a function name represents a built-in Mathematica function
     * that should be excluded from repeated call warnings.
     *
     * This includes ALL common built-ins:
     * - Control flow (If, Which, Switch, Do, While, For)
     * - Functional programming (Map, Apply, Scan, Fold, Thread)
     * - Scoping constructs (Module, Block, With, Function)
     * - Common operations (Table, Range, Join, Append, List)
     * - Type checking and property access
     *
     * Only flag TRULY expensive computational functions like Solve, Integrate, etc.
     */
    /**
     * Creates the set of Mathematica built-in functions.
     * Separated into static methods to avoid creating the large set on every call.
     */
    private static Set<String> createBuiltinFunctionsSet() {
        Set<String> functions = new HashSet<>();
        addCoreBuiltinFunctions(functions);
        addExtendedBuiltinFunctions(functions);
        return functions;
    }

    /**
     * Adds core Mathematica built-in functions (control flow, list ops, etc.).
     */
    private static void addCoreBuiltinFunctions(Set<String> functions) {
        // Control flow
        functions.addAll(Set.of(
            "If", "Which", "Switch", "Do", "While", "For", "Return", "Break", "Continue",
            "Throw", "Catch", "CheckAbort", "TimeConstrained", "MemoryConstrained",
            "Goto", "Label", "Abort", "Interrupt"
        ));

        // Functional programming
        functions.addAll(Set.of(
            "Map", "MapAt", "MapIndexed", "MapThread", "Apply", "Scan", "Fold", "FoldList",
            "Nest", "NestList", "NestWhile", "FixedPoint", "FixedPointList", "NestWhileList",
            "Compose", "Composition", "RightComposition", "Identity", "Through"
        ));

        // Scoping
        functions.addAll(Set.of(
            "Module", "Block", "With", "Function", "DynamicModule", "Unique", "Temporary"
        ));

        // List operations
        functions.addAll(Set.of(
            "Table", "Range", "Array", "List", "Join", "Append", "Prepend", "AppendTo", "PrependTo",
            "Insert", "Delete", "Take", "Drop", "Part", "Extract", "Select", "Cases", "DeleteCases"
        ));
        functions.addAll(Set.of(
            "Flatten", "Partition", "Split", "Riffle", "Thread", "Transpose", "Reverse",
            "DeleteDuplicates", "Union", "Intersection", "Complement", "Subsets", "Tuples"
        ));
        functions.addAll(Set.of(
            "ConstantArray", "Normal", "Total", "Accumulate", "Differences", "Ratios",
            "Tally", "Sort", "SortBy", "Ordering", "OrderedQ", "Permutations", "GroupBy"
        ));
        functions.addAll(Set.of(
            "Merge", "JoinAcross", "AssociationThread", "AssociationMap", "Counts"
        ));

        // List queries
        functions.addAll(Set.of(
            "Length", "First", "Last", "Rest", "Most", "MemberQ", "FreeQ", "Count",
            "Position", "FirstPosition", "Depth", "ArrayDepth", "Dimensions", "TensorRank"
        ));

        // Type checking
        functions.addAll(Set.of(
            "Head", "AtomQ", "ListQ", "NumberQ", "IntegerQ", "RealQ", "StringQ", "SymbolQ",
            "VectorQ", "MatrixQ", "ArrayQ", "NumericQ", "ExactNumberQ", "InexactNumberQ",
            "EvenQ", "OddQ", "PrimeQ", "PolynomialQ", "QuantityQ"
        ));

        // Association/property access
        functions.addAll(Set.of(
            "Key", "Lookup", "Keys", "Values", "KeyExistsQ", "Association", "AssociationQ",
            "KeySort", "KeyTake", "KeyDrop", "KeyMap", "KeyValueMap"
        ));

        // String operations
        functions.addAll(Set.of(
            "StringJoin", "StringLength", "StringTake", "StringDrop", "ToString", TO_EXPRESSION,
            "StringReverse", "StringInsert", "StringDelete", "StringReplace", "StringSplit"
        ));
        functions.addAll(Set.of(
            "StringPosition", "StringContainsQ", "StringStartsQ", "StringEndsQ", "StringMatchQ",
            "StringCount", "StringCases", "StringQ", "DigitQ", "LetterQ", "UpperCaseQ", "LowerCaseQ"
        ));
        functions.addAll(Set.of(
            "StringRiffle", "StringPadLeft", "StringPadRight", "StringTrim", "StringRepeat"
        ));

        // Basic math (cheap operations)
        functions.addAll(Set.of(
            "Plus", "Times", "Subtract", "Divide", "Power", "Mod", "Quotient", "Divisors",
            "Min", "Max", "Abs", "Sign", "Round", "Floor", "Ceiling", "Clip", "Rescale"
        ));
        functions.addAll(Set.of(
            "N", "Rationalize", "Numerator", "Denominator", "Re", "Im", "Conjugate", "Arg",
            "RealAbs", "Chop", "GCD", "LCM", "FactorInteger", "Prime", "PrimePi"
        ));

        // Mathematical functions (common, not expensive)
        functions.addAll(Set.of(
            "Sqrt", "Exp", "Log", "Log10", "Log2", "Sin", "Cos", "Tan", "Csc", "Sec", "Cot",
            "ArcSin", "ArcCos", "ArcTan", "ArcCsc", "ArcSec", "ArcCot", "ArcTan2"
        ));
        functions.addAll(Set.of(
            "Sinh", "Cosh", "Tanh", "Csch", "Sech", "Coth", "ArcSinh", "ArcCosh", "ArcTanh",
            "Factorial", "Binomial", "Multinomial", "Pochhammer"
        ));

        // Comparison
        functions.addAll(Set.of(
            "Equal", "Unequal", "Less", "Greater", "LessEqual", "GreaterEqual",
            "SameQ", "UnsameQ", "MatchQ", "Order", "OrderedQ", "Positive", "Negative",
            "NonPositive", "NonNegative"
        ));

        // Logic
        functions.addAll(Set.of(
            "And", "Or", "Not", "Xor", "Nand", "Nor", "Implies", "Equivalent",
            "TrueQ", "BooleanQ"
        ));

        // Constants
        functions.addAll(Set.of(
            "True", "False", "Null", "None", "All", "Automatic", "Identity", "Missing",
            "Indeterminate", "Infinity", "ComplexInfinity", "Pi", "E", "EulerGamma",
            "GoldenRatio", "Degree", "I"
        ));

        // Pattern matching
        functions.addAll(Set.of(
            "Replace", "ReplaceAll", "ReplaceRepeated", "Rule", "RuleDelayed",
            "Alternatives", "Except", "Optional", "Repeated", "RepeatedNull"
        ));
        functions.addAll(Set.of(
            "Longest", "Shortest", "PatternTest", "Condition", "Blank", "BlankSequence",
            "BlankNullSequence", "Pattern", "Verbatim", "HoldPattern"
        ));
    }

    /**
     * Adds extended Mathematica built-in functions (eval, attributes, display, etc.).
     */
    private static void addExtendedBuiltinFunctions(Set<String> functions) {
        // Evaluation and holding
        functions.addAll(Set.of(
            "Evaluate", "Hold", "HoldForm", "HoldComplete", "ReleaseHold", "Unevaluated",
            "Defer", "Inactivate", "Activate"
        ));

        // Attributes and properties
        functions.addAll(Set.of(
            "Attributes", "SetAttributes", "ClearAttributes", "Protect", "Unprotect",
            "Options", "SetOptions", "OptionValue", "FilterRules", "AbsoluteOptions"
        ));

        // Symbols and contexts
        functions.addAll(Set.of(
            "Symbol", "SymbolName", "SymbolQ", "Context", "Contexts", "Begin", "BeginPackage",
            "End", "EndPackage", "Remove", "Clear", "ClearAll", "Names"
        ));

        // Messages and errors
        functions.addAll(Set.of(
            "Message", "MessageName", "Check", "Quiet", "Off", "On", "Print", "PrintTemporary",
            "Echo", "EchoFunction"
        ));

        // Dynamic and frontend
        functions.addAll(Set.of(
            "Dynamic", "DynamicWrapper", "Refresh", "UpdateInterval", "TrackedSymbols"
        ));

        // Data import/export (lightweight operations)
        functions.addAll(Set.of(
            IMPORT, EXPORT, GET, PUT, "Read", "Write", "OpenRead", "OpenWrite",
            "Close", "ReadList", "ReadString", "WriteString"
        ));

        // Graphics primitives (not rendering)
        functions.addAll(Set.of(
            "Point", "Line", "Circle", "Disk", "Rectangle", "Polygon", "Arrow",
            "Text", "Inset", "GeometricTransformation"
        ));

        // Colors and styling
        functions.addAll(Set.of(
            "RGBColor", "Hue", "GrayLevel", "CMYKColor", "Opacity", "ColorData",
            "Directive", "Style", "FontSize", "FontFamily", "FontWeight", "FontColor"
        ));

        // Formatting and display
        functions.addAll(Set.of(
            "Row", "Column", "Grid", "Item", "Spacer", "Pane", "Panel", "Framed",
            "Labeled", "Legended", "Placed", "Tooltip"
        ));

        // Date and time (simple operations)
        functions.addAll(Set.of(
            "DateList", "DateString", "DateObject", "TimeObject", "Now", "Today",
            "AbsoluteTime", "DateDifference", "DatePlus"
        ));

        // File operations (lightweight)
        functions.addAll(Set.of(
            "FileNameJoin", "FileNameSplit", "FileBaseName", "FileExtension",
            "DirectoryName", "FileExistsQ", "DirectoryQ", "FileNames", "FileNameTake"
        ));

        // Randomness (cheap random operations)
        functions.addAll(Set.of(
            RANDOM_INTEGER, "RandomReal", "RandomChoice", "RandomSample", "SeedRandom"
        ));

        // Events and triggers
        functions.addAll(Set.of(
            "WhenEvent", "EventHandler", "EventData"
        ));

        // Quantities and units (lightweight operations)
        functions.addAll(Set.of(
            "Quantity", "QuantityMagnitude", "QuantityUnit", "UnitConvert", "UnitDimensions"
        ));

        // Interpolation and numerical (cheap operations)
        functions.addAll(Set.of(
            "InterpolatingFunction", "Interpolation"
        ));

        // Pure list processing
        functions.addAll(Set.of(
            "Pick", "PadLeft", "PadRight", "RotateLeft", "RotateRight", "ArrayPad",
            "ArrayReshape", "ArrayFlatten", "ArrayRules", "SparseArray"
        ));
    }

    private boolean isPropertyAccessorOrLightweight(String funcName) {
        // Property accessors (object-oriented patterns)
        if (funcName.startsWith("$")) {
            return true; // $This, $Self, $Context, etc.
        }

        // ECL pattern validators (lightweight pattern constructors ending in P)
        // Examples: RangeP, GreaterP, LessP, ObjectP, QuantityArrayP, etc.
        if (funcName.endsWith("P") && funcName.length() > 1 && Character.isUpperCase(funcName.charAt(0))) {
            return true;
        }

        // Check ECL lightweight functions
        if (ECL_LIGHTWEIGHT_FUNCTIONS.contains(funcName)) {
            return true;
        }

        // Check Mathematica built-in functions
        return MATHEMATICA_BUILTIN_FUNCTIONS.contains(funcName);
    }

    private void checkFunctionNaming(FunctionDefNode node, String funcName) {
        if (!Character.isUpperCase(funcName.charAt(0))) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.WRONG_CAPITALIZATION_KEY,
                FUNCTION_PREFIX + funcName + "' should start with uppercase letter per Mathematica convention.");
        }
    }

    private void checkLongFunction(FunctionDefNode node) {
        int lines = node.getEndLine() - node.getStartLine();
        if (lines > 50) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.FUNCTION_LENGTH_KEY,
                "Function is " + lines + " lines long. Consider breaking it into smaller functions (max 50 lines).");
        }
    }

    private void checkFunctionComplexity(FunctionDefNode node) {
        // Count nested levels and branches
        int complexity = calculateComplexity(node);
        if (complexity > 15) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY,
                "Function has cyclomatic complexity of " + complexity + " (max 15 allowed).");
        }
    }

    private int calculateComplexity(AstNode node) {
        int complexity = 1;
        for (AstNode child : node.getChildren()) {
            if (child instanceof ControlFlowNode || child instanceof LoopNode) {
                complexity++;
            }
            complexity += calculateComplexity(child) - 1;
        }
        return complexity;
    }

    private void checkExpressionComplexity(OperatorNode node) {
        // Count total operators in expression
        int count = countOperators(node);
        if (count > 10) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY,
                "Expression has " + count + " operators (max 10 allowed). Consider breaking into smaller expressions.");
        }
    }

    private int countOperators(AstNode node) {
        int count = (node instanceof OperatorNode) ? 1 : 0;
        for (AstNode child : node.getChildren()) {
            count += countOperators(child);
        }
        return count;
    }

    private void checkDeprecatedFunction(FunctionCallNode node, String funcName) {
        Set<String> deprecated = Set.of("$Version", "Removed", "Experimental");
        if (deprecated.contains(funcName)) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.DEPRECATED_FUNCTION_KEY,
                FUNCTION_PREFIX + funcName + "' is deprecated.");
        }
    }

    private void checkUndefinedIdentifier(IdentifierNode node) {
        String name = node.getName();
        // Might be undefined function call
        if (Character.isUpperCase(name.charAt(0)) && !isBuiltin(name) && !definedFunctions.contains(name)
            && !calledFunctions.contains(name)) {
            calledFunctions.add(name);  // Track it
        }
    }

    private boolean isBuiltin(String name) {
        // Simplified builtin check
        return "Print".equals(name) || "If".equals(name) || "Module".equals(name)
               || "Block".equals(name) || "With".equals(name) || "Map".equals(name)
               || "Table".equals(name) || "Length".equals(name) || PART.equals(name);
    }

    private void checkListIndexOutOfBounds(FunctionCallNode node) {
        if (node.getFunctionName().equals(PART)) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.LIST_INDEX_OUT_OF_BOUNDS_KEY,
                "Potential list index out of bounds. Use Quiet[Part[...]] or check Length first.");
        }
    }

    private void checkAssociationVsListConfusion(FunctionCallNode node) {
        String funcName = node.getFunctionName();
        if (funcName.equals(PART) && !node.getArguments().isEmpty()) {
            // Check if first arg looks like an association variable
            AstNode firstArg = node.getArguments().get(0);
            if (firstArg instanceof IdentifierNode) {
                String varName = ((IdentifierNode) firstArg).getName();
                if (varName.toLowerCase().contains("assoc")) {
                    reportIssue(node.getStartLine(), MathematicaRulesDefinition.ASSOCIATION_VS_LIST_CONFUSION_KEY,
                        "Positional indexing on '" + varName + "' may be incorrect for associations. Use Keys/Values or key-based access.");
                }
            }
        }
    }

    private void checkInefficientKeyLookup(FunctionCallNode node) {
        if (node.getFunctionName().equals("Select") && !node.getArguments().isEmpty()) {
            AstNode firstArg = node.getArguments().get(0);
            if (firstArg instanceof FunctionCallNode) {
                FunctionCallNode inner = (FunctionCallNode) firstArg;
                if (inner.getFunctionName().equals("Keys")) {
                    reportIssue(node.getStartLine(), MathematicaRulesDefinition.INEFFICIENT_KEY_LOOKUP_KEY,
                        "Use KeySelect instead of Select[Keys[...]] for better performance.");
                }
            }
        }
    }

    private void checkUseTableInsteadOfMap(FunctionCallNode node) {
        if (node.getFunctionName().equals("Map") && node.getArguments().size() == 2) {
            reportIssue(node.getStartLine(), "UseTableInsteadOfMap",
                "Consider using Table instead of Map for simple transformations (may be faster).");
        }
    }

    private void checkUseAssociationInsteadOfList(FunctionCallNode node) {
        // Check for list of rules that should be Association
        if (node.getFunctionName().equals("List")) {
            boolean allRules = true;
            for (AstNode arg : node.getArguments()) {
                if (!(arg instanceof OperatorNode && ((OperatorNode) arg).getOperatorSymbol().equals("->"))) {
                    allRules = false;
                    break;
                }
            }
            if (allRules && node.getArguments().size() > 2) {
                reportIssue(node.getStartLine(), "UseAssociationInsteadOfList",
                    "List of rules should be an Association for better performance and clarity.");
            }
        }
    }

    private void checkMagicNumber(LiteralNode node) {
        if (node.getValue() instanceof Number) {
            double value = ((Number) node.getValue()).doubleValue();
            if (value != 0 && value != 1 && value != -1) {
                reportIssue(node.getStartLine(), MathematicaRulesDefinition.MAGIC_NUMBER_KEY,
                    "Magic number " + value + " should be a named constant.");
            }
        }
    }

    private void checkHardcodedCredentials(LiteralNode node, String value) {
        // Skip strings that are clearly UI text or documentation
        // UI text contains spaces, newlines, common punctuation, or sentence-like patterns
        if (value.contains(" ") || value.contains("\n") || value.contains("\t")) {
            return;
        }

        // Skip strings with common UI punctuation patterns
        // But allow underscores and hyphens which are common in credentials
        if (value.contains(",") || value.contains(".") || value.contains(":")
            || value.contains(";") || value.contains("!") || value.contains("?")
            || value.contains("+") || value.contains("/") || value.contains("\\")) {
            return;
        }

        // Check for credential keywords in the string value
        String lower = value.toLowerCase();

        // Flag strings that contain credential keywords AND are long enough
        // Note: This checks the string content, not variable names
        // For variable-based detection, see VulnerabilityDetector.java
        if ((lower.contains("password") || lower.contains("secret") || lower.contains("key")
            || lower.contains("token") || lower.contains("auth"))
            && value.length() > 8) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.HARDCODED_CREDENTIALS_KEY,
                "Avoid hardcoding credentials. Use SystemCredential[] or environment variables.");
        }
    }

    // Vulnerability checks (simplified versions)
    private void checkCommandInjection(FunctionCallNode node) {
        if (node.getFunctionName().equals("Run") || node.getFunctionName().equals("RunProcess")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.COMMAND_INJECTION_KEY,
                "Potential command injection. Sanitize inputs to " + node.getFunctionName() + ".");
        }
    }

    private void checkSqlInjection(FunctionCallNode node) {
        if (node.getFunctionName().equals("SQLExecute") || node.getFunctionName().equals("SQLSelect")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.SQL_INJECTION_KEY,
                "Potential SQL injection. Use parameterized queries.");
        }
    }

    private void checkCodeInjection(FunctionCallNode node) {
        if (node.getFunctionName().equals(TO_EXPRESSION) && !node.getArguments().isEmpty()) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.CODE_INJECTION_KEY,
                "Potential code injection via ToExpression. Validate and sanitize input.");
        }
    }

    private void checkPathTraversal(FunctionCallNode node) {
        Set<String> fileOps = Set.of(IMPORT, EXPORT, GET, PUT, "DeleteFile", "RenameFile", "CopyFile");
        if (fileOps.contains(node.getFunctionName())) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.PATH_TRAVERSAL_KEY,
                "Potential path traversal. Validate file paths in " + node.getFunctionName() + ".");
        }
    }

    private void checkWeakCryptography(FunctionCallNode node) {
        if (node.getFunctionName().equals("Hash") && node.getArguments().size() > 1) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.WEAK_CRYPTOGRAPHY_KEY,
                "Use strong hashing algorithms (SHA256, SHA512) instead of weak ones.");
        }
    }

    private void checkSSRF(FunctionCallNode node) {
        Set<String> urlFuncs = Set.of("URLFetch", "URLRead", "URLExecute", "URLDownload");
        if (urlFuncs.contains(node.getFunctionName())) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.SSRF_KEY,
                "Potential SSRF. Validate and whitelist URLs in " + node.getFunctionName() + ".");
        }
    }

    private void checkInsecureDeserialization(FunctionCallNode node) {
        if (node.getFunctionName().equals(IMPORT) && !node.getArguments().isEmpty()) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.INSECURE_DESERIALIZATION_KEY,
                "Potential insecure deserialization. Validate imported data format and source.");
        }
    }

    private void checkUnsafeSymbol(FunctionCallNode node) {
        if (node.getFunctionName().equals("Symbol") || node.getFunctionName().equals("SymbolName")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.UNSAFE_SYMBOL_KEY,
                "Dynamic symbol creation can be unsafe. Validate symbol names.");
        }
    }

    private void checkXXE(FunctionCallNode node) {
        if (node.getFunctionName().equals("XMLObject") || node.getFunctionName().equals(IMPORT)) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.XXE_KEY,
                "Potential XXE vulnerability. Disable external entity processing for XML.");
        }
    }

    private void checkMissingSanitization() {
        // Already covered by other checks
    }

    private void checkInsecureRandom(FunctionCallNode node) {
        if (node.getFunctionName().equals(RANDOM) || node.getFunctionName().equals(RANDOM_INTEGER)) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.INSECURE_RANDOM_EXPANDED_KEY,
                "Use RandomCrypto for cryptographic random numbers instead of Random/RandomInteger.");
        }
    }

    private void checkUnsafeCloudDeploy(FunctionCallNode node) {
        if (node.getFunctionName().equals(CLOUD_DEPLOY) || node.getFunctionName().equals("CloudPublish")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.UNSAFE_CLOUD_DEPLOY_KEY,
                "Ensure proper authentication and permissions for cloud deployment.");
        }
    }

    private void checkDynamicInjection(FunctionCallNode node) {
        if (node.getFunctionName().equals("Dynamic")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.DYNAMIC_INJECTION_KEY,
                "Potential injection via Dynamic. Validate dynamic content sources.");
        }
    }

    private void checkToExpressionOnInput(FunctionCallNode node) {
        if (node.getFunctionName().equals(TO_EXPRESSION)) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.TOEXPRESSION_ON_INPUT_KEY,
                "ToExpression on user input is dangerous. Use safer alternatives.");
        }
    }

    private void checkUnsanitizedRunProcess(FunctionCallNode node) {
        if (node.getFunctionName().equals("RunProcess")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.UNSANITIZED_RUNPROCESS_KEY,
                "Sanitize all inputs to RunProcess to prevent command injection.");
        }
    }

    private void checkMissingCloudAuth(FunctionCallNode node) {
        Set<String> cloudFuncs = Set.of("APIFunction", "FormFunction", CLOUD_DEPLOY);
        if (cloudFuncs.contains(node.getFunctionName())) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.MISSING_CLOUD_AUTH_KEY,
                "Cloud functions should require authentication. Use Permissions option.");
        }
    }

    private void checkHardcodedApiKeys() {
        // Checked via literals
    }

    private void checkNeedsGetUntrusted(FunctionCallNode node) {
        if (node.getFunctionName().equals(GET) || node.getFunctionName().equals("Needs")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.NEEDS_GET_UNTRUSTED_KEY,
                "Loading untrusted packages is dangerous. Verify source of " + node.getFunctionName() + ".");
        }
    }

    private void checkExposingSensitiveData(FunctionCallNode node) {
        Set<String> exposeFuncs = Set.of(CLOUD_DEPLOY, EXPORT, PUT);
        if (exposeFuncs.contains(node.getFunctionName())) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.EXPOSING_SENSITIVE_DATA_KEY,
                "Ensure no sensitive data is exposed via " + node.getFunctionName() + ".");
        }
    }

    private void checkMissingFormFunctionValidation(FunctionCallNode node) {
        if (node.getFunctionName().equals("FormFunction")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.MISSING_FORMFUNCTION_VALIDATION_KEY,
                "FormFunction should validate all inputs before processing.");
        }
    }

    // ========== Security Hotspot Rules ==========

    private void checkFileUploadValidation(FunctionCallNode node) {
        Set<String> fileOps = Set.of(IMPORT, GET, "OpenRead", "OpenWrite", PUT);
        if (fileOps.contains(node.getFunctionName())) {
            String message;
            if (node.getFunctionName().equals(IMPORT) || node.getFunctionName().equals(GET)) {
                message = REVIEW_PREFIX + "Ensure file uploads/imports are validated for type, size, and content.";
            } else {
                message = REVIEW_PREFIX + "Ensure file operations validate and sanitize file paths.";
            }
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.FILE_UPLOAD_VALIDATION_KEY, message);
        }
    }

    private void checkExternalApiSafeguards(FunctionCallNode node) {
        Set<String> apiCalls = Set.of("URLRead", "URLFetch", "URLExecute", "URLSubmit", "ServiceExecute", "ServiceConnect");
        if (apiCalls.contains(node.getFunctionName())) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.EXTERNAL_API_SAFEGUARDS_KEY,
                REVIEW_PREFIX + "Ensure this API call has timeout, error handling, and rate limiting.");
        }
    }

    private void checkCryptoKeyGeneration(FunctionCallNode node) {
        Set<String> keyFuncs = Set.of(RANDOM_INTEGER, RANDOM, "GenerateSymmetricKey", "GenerateAsymmetricKeyPair");
        if (keyFuncs.contains(node.getFunctionName())) {
            String message;
            if (node.getFunctionName().equals(RANDOM)) {
                message = REVIEW_PREFIX + "Random[] is not cryptographically secure. Use RandomInteger for keys.";
            } else {
                message = REVIEW_PREFIX + "Ensure cryptographic keys are generated with sufficient entropy and stored securely.";
            }
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.CRYPTO_KEY_GENERATION_KEY, message);
        }
    }

    private void checkNetworkOperations(FunctionCallNode node) {
        Set<String> networkOps = Set.of("SocketConnect", "SocketOpen", "SocketListen", "WebExecute");
        if (networkOps.contains(node.getFunctionName())) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.NETWORK_OPERATIONS_KEY,
                REVIEW_PREFIX + "Network operation should use TLS, have timeout, and proper error handling.");
        }
    }

    private void checkFileSystemModifications(FunctionCallNode node) {
        Set<String> fileMods = Set.of("DeleteFile", "DeleteDirectory", "RenameFile", "CopyFile", "SetFileDate");
        if (fileMods.contains(node.getFunctionName())) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.FILE_SYSTEM_MODIFICATIONS_KEY,
                REVIEW_PREFIX + "File system modification should validate paths and log operations.");
        }
    }

    private void checkEnvironmentVariable(FunctionCallNode node) {
        if (node.getFunctionName().equals("Environment")) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.ENVIRONMENT_VARIABLE_KEY,
                REVIEW_PREFIX + "Environment variable may contain secrets. Ensure not logged or exposed.");
        }
    }

    private void checkImportWithoutFormat(FunctionCallNode node) {
        // Check if Import has only 1 argument (no format specified)
        // This is a simple heuristic - more sophisticated would parse arguments
        if (node.getFunctionName().equals(IMPORT) && node.getArguments().size() == 1) {
            reportIssue(node.getStartLine(), MathematicaRulesDefinition.IMPORT_WITHOUT_FORMAT_KEY,
                REVIEW_PREFIX + "Import without explicit format relies on file extension. Specify format for security.");
        }
    }

    // ========== Issue Reporting ==========

    private void reportIssue(int line, String ruleKey, String message) {
        if (sensor != null) {
            sensor.queueIssue(inputFile, line, ruleKey, message);
        }
    }

    /**
     * Reports an issue with secondary locations showing all related occurrences.
     * Used for duplicate detection to show all duplicate locations.
     */
    private void reportIssueWithSecondaryLocations(int primaryLine, String ruleKey, String message,
                                                   java.util.List<Integer> allLines) {
        org.sonar.api.batch.sensor.issue.NewIssue issue = context.newIssue()
            .forRule(org.sonar.api.rule.RuleKey.of(
                org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.REPOSITORY_KEY, ruleKey));

        // Primary location (first occurrence)
        org.sonar.api.batch.sensor.issue.NewIssueLocation primaryLocation = issue.newLocation()
            .on(inputFile)
            .at(inputFile.selectLine(primaryLine))
            .message(message);

        issue.at(primaryLocation);

        // Add secondary locations for all other duplicates
        for (int i = 1; i < allLines.size(); i++) {
            int secondaryLine = allLines.get(i);
            org.sonar.api.batch.sensor.issue.NewIssueLocation secondaryLocation = issue.newLocation()
                .on(inputFile)
                .at(inputFile.selectLine(secondaryLine))
                .message("Duplicate #" + (i + 1));

            issue.addLocation(secondaryLocation);
        }

        issue.save();
    }
}
