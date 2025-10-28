package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.mathematica.MathematicaLanguage;

/**
 * Defines code quality rules for Mathematica.
 */
public class MathematicaRulesDefinition implements RulesDefinition {

    public static final String REPOSITORY_KEY = "mathematica";
    public static final String REPOSITORY_NAME = "SonarAnalyzer";

    // Rule keys - Code Smells
    public static final String COMMENTED_CODE_KEY = "CommentedCode";
    public static final String MAGIC_NUMBER_KEY = "MagicNumber";
    public static final String TODO_FIXME_KEY = "TodoFixme";
    public static final String EMPTY_BLOCK_KEY = "EmptyBlock";
    public static final String FUNCTION_LENGTH_KEY = "FunctionLength";
    public static final String FILE_LENGTH_KEY = "FileLength";
    public static final String EMPTY_CATCH_KEY = "EmptyCatchBlock";
    public static final String DEBUG_CODE_KEY = "DebugCode";

    // Rule keys - Security Vulnerabilities
    public static final String HARDCODED_CREDENTIALS_KEY = "HardcodedCredentials";
    public static final String COMMAND_INJECTION_KEY = "CommandInjection";
    public static final String SQL_INJECTION_KEY = "SqlInjection";
    public static final String CODE_INJECTION_KEY = "CodeInjection";
    public static final String PATH_TRAVERSAL_KEY = "PathTraversal";
    public static final String WEAK_CRYPTOGRAPHY_KEY = "WeakCryptography";
    public static final String SSRF_KEY = "ServerSideRequestForgery";
    public static final String INSECURE_DESERIALIZATION_KEY = "InsecureDeserialization";

    // Rule keys - Bugs (Reliability)
    public static final String DIVISION_BY_ZERO_KEY = "DivisionByZero";
    public static final String ASSIGNMENT_IN_CONDITIONAL_KEY = "AssignmentInConditional";
    public static final String LIST_INDEX_OUT_OF_BOUNDS_KEY = "ListIndexOutOfBounds";
    public static final String INFINITE_RECURSION_KEY = "InfiniteRecursion";
    public static final String UNREACHABLE_PATTERN_KEY = "UnreachablePattern";

    // Rule keys - Security Hotspots
    public static final String FILE_UPLOAD_VALIDATION_KEY = "FileUploadValidation";
    public static final String EXTERNAL_API_SAFEGUARDS_KEY = "ExternalApiSafeguards";
    public static final String CRYPTO_KEY_GENERATION_KEY = "CryptoKeyGeneration";

    // Rule keys - New Code Smells (Phase 2)
    public static final String UNUSED_VARIABLES_KEY = "UnusedVariables";
    public static final String DUPLICATE_FUNCTION_KEY = "DuplicateFunctionDefinition";
    public static final String TOO_MANY_PARAMETERS_KEY = "TooManyParameters";
    public static final String DEEPLY_NESTED_KEY = "DeeplyNestedConditionals";
    public static final String MISSING_DOCUMENTATION_KEY = "MissingDocumentation";
    public static final String INCONSISTENT_NAMING_KEY = "InconsistentNaming";
    public static final String IDENTICAL_BRANCHES_KEY = "IdenticalBranches";
    public static final String EXPRESSION_TOO_COMPLEX_KEY = "ExpressionTooComplex";
    public static final String DEPRECATED_FUNCTION_KEY = "DeprecatedFunction";
    public static final String EMPTY_STATEMENT_KEY = "EmptyStatement";

    // Rule keys - New Bugs (Phase 2)
    public static final String FLOATING_POINT_EQUALITY_KEY = "FloatingPointEquality";
    public static final String FUNCTION_WITHOUT_RETURN_KEY = "FunctionWithoutReturn";
    public static final String VARIABLE_BEFORE_ASSIGNMENT_KEY = "VariableBeforeAssignment";
    public static final String OFF_BY_ONE_KEY = "OffByOne";
    public static final String INFINITE_LOOP_KEY = "InfiniteLoop";
    public static final String MISMATCHED_DIMENSIONS_KEY = "MismatchedDimensions";
    public static final String TYPE_MISMATCH_KEY = "TypeMismatch";
    public static final String SUSPICIOUS_PATTERN_KEY = "SuspiciousPattern";

    // Rule keys - New Vulnerabilities (Phase 2)
    public static final String UNSAFE_SYMBOL_KEY = "UnsafeSymbol";
    public static final String XXE_KEY = "XmlExternalEntity";
    public static final String MISSING_SANITIZATION_KEY = "MissingSanitization";
    public static final String INSECURE_RANDOM_EXPANDED_KEY = "InsecureRandomExpanded";

    // Rule keys - New Security Hotspots (Phase 2)
    public static final String NETWORK_OPERATIONS_KEY = "NetworkOperations";
    public static final String FILE_SYSTEM_MODIFICATIONS_KEY = "FileSystemModifications";
    public static final String ENVIRONMENT_VARIABLE_KEY = "EnvironmentVariable";

    // Rule keys - Phase 3: Performance Issues (8 rules)
    public static final String APPEND_IN_LOOP_KEY = "AppendInLoop";
    public static final String REPEATED_FUNCTION_CALLS_KEY = "RepeatedFunctionCalls";
    public static final String STRING_CONCAT_IN_LOOP_KEY = "StringConcatInLoop";
    public static final String UNCOMPILED_NUMERICAL_KEY = "UncompiledNumerical";
    public static final String PACKED_ARRAY_BREAKING_KEY = "PackedArrayBreaking";
    public static final String NESTED_MAP_TABLE_KEY = "NestedMapTable";
    public static final String LARGE_TEMP_EXPRESSIONS_KEY = "LargeTempExpressions";
    public static final String PLOT_IN_LOOP_KEY = "PlotInLoop";

    // Rule keys - Phase 3: Pattern Matching Issues (5 rules)
    public static final String MISSING_PATTERN_TEST_KEY = "MissingPatternTest";
    public static final String PATTERN_BLANKS_MISUSE_KEY = "PatternBlanksMisuse";
    public static final String SET_DELAYED_CONFUSION_KEY = "SetDelayedConfusion";
    public static final String SYMBOL_NAME_COLLISION_KEY = "SymbolNameCollision";
    public static final String BLOCK_MODULE_MISUSE_KEY = "BlockModuleMisuse";

    // Rule keys - Phase 3: Best Practices (7 rules)
    public static final String GENERIC_VARIABLE_NAMES_KEY = "GenericVariableNames";
    public static final String MISSING_USAGE_MESSAGE_KEY = "MissingUsageMessage";
    public static final String MISSING_OPTIONS_PATTERN_KEY = "MissingOptionsPattern";
    public static final String SIDE_EFFECTS_NAMING_KEY = "SideEffectsNaming";
    public static final String COMPLEX_BOOLEAN_KEY = "ComplexBooleanExpression";
    public static final String UNPROTECTED_SYMBOLS_KEY = "UnprotectedPublicSymbols";
    public static final String MISSING_RETURN_KEY = "MissingReturnInConditional";

    // Rule keys - Phase 3: Security & Safety (3 rules)
    public static final String UNSAFE_CLOUD_DEPLOY_KEY = "UnsafeCloudDeploy";
    public static final String DYNAMIC_INJECTION_KEY = "DynamicContentInjection";
    public static final String IMPORT_WITHOUT_FORMAT_KEY = "ImportWithoutFormat";

    // Rule keys - Phase 3: Resource Management (2 rules)
    public static final String UNCLOSED_FILE_HANDLE_KEY = "UnclosedFileHandle";
    public static final String GROWING_DEFINITION_CHAIN_KEY = "GrowingDefinitionChain";

    // Rule keys - Phase 4: Pattern Matching & Function Definition (5 new rules)
    public static final String OVERCOMPLEX_PATTERNS_KEY = "OvercomplexPatterns";
    public static final String INCONSISTENT_RULE_TYPES_KEY = "InconsistentRuleTypes";
    public static final String MISSING_FUNCTION_ATTRIBUTES_KEY = "MissingFunctionAttributes";
    public static final String MISSING_DOWNVALUES_DOC_KEY = "MissingDownValuesDocumentation";
    public static final String MISSING_PATTERN_TEST_VALIDATION_KEY = "MissingPatternTestValidation";

    // Rule keys - Phase 4: Code Clarity (8 new rules)
    public static final String EXCESSIVE_PURE_FUNCTIONS_KEY = "ExcessivePureFunctions";
    public static final String MISSING_OPERATOR_PRECEDENCE_KEY = "MissingOperatorPrecedence";
    public static final String HARDCODED_FILE_PATHS_KEY = "HardcodedFilePaths";
    public static final String INCONSISTENT_RETURN_TYPES_KEY = "InconsistentReturnTypes";
    public static final String MISSING_ERROR_MESSAGES_KEY = "MissingErrorMessages";
    public static final String GLOBAL_STATE_MODIFICATION_KEY = "GlobalStateModification";
    public static final String MISSING_LOCALIZATION_KEY = "MissingLocalization";
    public static final String EXPLICIT_GLOBAL_CONTEXT_KEY = "ExplicitGlobalContext";

    // Rule keys - Phase 4: Data Structures (5 new rules)
    public static final String MISSING_TEMPORARY_CLEANUP_KEY = "MissingTemporaryCleanup";
    public static final String NESTED_LISTS_INSTEAD_ASSOCIATION_KEY = "NestedListsInsteadOfAssociation";
    public static final String REPEATED_PART_EXTRACTION_KEY = "RepeatedPartExtraction";
    public static final String MISSING_MEMOIZATION_KEY = "MissingMemoization";
    public static final String STRINGJOIN_FOR_TEMPLATES_KEY = "StringJoinForTemplates";

    // Rule keys - Phase 4: Type & Value Errors (8 new bugs)
    public static final String MISSING_EMPTY_LIST_CHECK_KEY = "MissingEmptyListCheck";
    public static final String MACHINE_PRECISION_IN_SYMBOLIC_KEY = "MachinePrecisionInSymbolic";
    public static final String MISSING_FAILED_CHECK_KEY = "MissingFailedCheck";
    public static final String ZERO_DENOMINATOR_KEY = "ZeroDenominator";
    public static final String MISSING_MATRIX_DIMENSION_CHECK_KEY = "MissingMatrixDimensionCheck";
    public static final String INCORRECT_SET_IN_SCOPING_KEY = "IncorrectSetInScoping";
    public static final String MISSING_HOLD_ATTRIBUTES_KEY = "MissingHoldAttributes";
    public static final String EVALUATION_ORDER_ASSUMPTION_KEY = "EvaluationOrderAssumption";

    // Rule keys - Phase 4: Data Handling Bugs (7 new bugs)
    public static final String INCORRECT_LEVEL_SPECIFICATION_KEY = "IncorrectLevelSpecification";
    public static final String UNPACKING_PACKED_ARRAYS_KEY = "UnpackingPackedArrays";
    public static final String MISSING_SPECIAL_CASE_HANDLING_KEY = "MissingSpecialCaseHandling";
    public static final String INCORRECT_ASSOCIATION_OPERATIONS_KEY = "IncorrectAssociationOperations";
    public static final String DATEOBJECT_VALIDATION_KEY = "DateObjectValidation";
    public static final String TOTAL_MEAN_ON_NON_NUMERIC_KEY = "TotalMeanOnNonNumeric";
    public static final String QUANTITY_UNIT_MISMATCH_KEY = "QuantityUnitMismatch";

    // Rule keys - Phase 4: Performance (10 new rules)
    public static final String LINEAR_SEARCH_INSTEAD_LOOKUP_KEY = "LinearSearchInsteadOfLookup";
    public static final String REPEATED_CALCULATIONS_KEY = "RepeatedCalculations";
    public static final String POSITION_INSTEAD_PATTERN_KEY = "PositionInsteadOfPattern";
    public static final String FLATTEN_TABLE_ANTIPATTERN_KEY = "FlattenTableAntipattern";
    public static final String MISSING_PARALLELIZATION_KEY = "MissingParallelization";
    public static final String MISSING_SPARSE_ARRAY_KEY = "MissingSparseArray";
    public static final String UNNECESSARY_TRANSPOSE_KEY = "UnnecessaryTranspose";
    public static final String DELETEDUPS_ON_LARGE_DATA_KEY = "DeleteDuplicatesOnLargeData";
    public static final String REPEATED_STRING_PARSING_KEY = "RepeatedStringParsing";
    public static final String MISSING_COMPILATION_TARGET_KEY = "MissingCompilationTarget";

    // Rule keys - Phase 4: Security (7 new vulnerabilities)
    public static final String TOEXPRESSION_ON_INPUT_KEY = "ToExpressionOnExternalInput";
    public static final String UNSANITIZED_RUNPROCESS_KEY = "UnsanitizedRunProcess";
    public static final String MISSING_CLOUD_AUTH_KEY = "MissingCloudAuthentication";
    public static final String HARDCODED_API_KEYS_KEY = "HardcodedApiKeys";
    public static final String NEEDS_GET_UNTRUSTED_KEY = "NeedsGetUntrustedPaths";
    public static final String EXPOSING_SENSITIVE_DATA_KEY = "ExposingSensitiveData";
    public static final String MISSING_FORMFUNCTION_VALIDATION_KEY = "MissingFormFunctionValidation";

    // Rule keys - Chunk 1: Pattern System Rules (Items 16-30 from ROADMAP_325.md)
    public static final String UNRESTRICTED_BLANK_PATTERN_KEY = "UnrestrictedBlankPattern";
    public static final String PATTERN_TEST_VS_CONDITION_KEY = "PatternTestVsCondition";
    public static final String BLANKSEQUENCE_WITHOUT_RESTRICTION_KEY = "BlankSequenceWithoutRestriction";
    public static final String NESTED_OPTIONAL_PATTERNS_KEY = "NestedOptionalPatterns";
    public static final String PATTERN_NAMING_CONFLICTS_KEY = "PatternNamingConflicts";
    public static final String REPEATED_PATTERN_ALTERNATIVES_KEY = "RepeatedPatternAlternatives";
    public static final String PATTERN_TEST_PURE_FUNCTION_KEY = "PatternTestWithPureFunction";
    public static final String MISSING_PATTERN_DEFAULTS_KEY = "MissingPatternDefaults";
    public static final String ORDER_DEPENDENT_PATTERNS_KEY = "OrderDependentPatternDefinitions";
    public static final String VERBATIM_PATTERN_MISUSE_KEY = "VerbatimPatternMisuse";
    public static final String HOLDPATTERN_UNNECESSARY_KEY = "HoldPatternUnnecessary";
    public static final String LONGEST_SHORTEST_WITHOUT_ORDERING_KEY = "LongestShortestWithoutOrdering";
    public static final String PATTERN_REPEATED_DIFFERENT_TYPES_KEY = "PatternRepeatedWithDifferentTypes";
    public static final String ALTERNATIVES_TOO_COMPLEX_KEY = "AlternativesTooComplex";
    public static final String PATTERN_MATCHING_LARGE_LISTS_KEY = "PatternMatchingOnLargeLists";

    // Rule keys - Chunk 1: List/Array Rules (Items 31-40 from ROADMAP_325.md)
    public static final String EMPTY_LIST_INDEXING_KEY = "EmptyListIndexing";
    public static final String NEGATIVE_INDEX_WITHOUT_VALIDATION_KEY = "NegativeIndexWithoutValidation";
    public static final String PART_ASSIGNMENT_TO_IMMUTABLE_KEY = "PartAssignmentToImmutable";
    public static final String INEFFICIENT_LIST_CONCATENATION_KEY = "InefficientListConcatenation";
    public static final String UNNECESSARY_FLATTEN_KEY = "UnnecessaryFlatten";
    public static final String LENGTH_IN_LOOP_CONDITION_KEY = "LengthInLoopCondition";
    public static final String REVERSE_TWICE_KEY = "ReverseTwice";
    public static final String SORT_WITHOUT_COMPARISON_KEY = "SortWithoutComparison";
    public static final String POSITION_VS_SELECT_KEY = "PositionVsSelect";
    public static final String NESTED_PART_EXTRACTION_KEY = "NestedPartExtraction";

    // Rule keys - Chunk 1: Association Rules (Items 41-50 from ROADMAP_325.md)
    public static final String MISSING_KEY_CHECK_KEY = "MissingKeyCheck";
    public static final String ASSOCIATION_VS_LIST_CONFUSION_KEY = "AssociationVsListConfusion";
    public static final String INEFFICIENT_KEY_LOOKUP_KEY = "InefficientKeyLookup";
    public static final String QUERY_ON_NON_DATASET_KEY = "QueryOnNonDataset";
    public static final String ASSOCIATION_UPDATE_PATTERN_KEY = "AssociationUpdatePattern";
    public static final String MERGE_WITHOUT_CONFLICT_STRATEGY_KEY = "MergeWithoutConflictStrategy";
    public static final String ASSOCIATETO_ON_NON_SYMBOL_KEY = "AssociateToOnNonSymbol";
    public static final String KEYDROP_MULTIPLE_TIMES_KEY = "KeyDropMultipleTimes";
    public static final String LOOKUP_WITH_MISSING_DEFAULT_KEY = "LookupWithMissingDefault";
    public static final String GROUPBY_WITHOUT_AGGREGATION_KEY = "GroupByWithoutAggregation";

    // Rule keys - Chunk 2: Unused Code Detection (Items 61-75 from ROADMAP_325.md)
    public static final String UNUSED_PRIVATE_FUNCTION_KEY = "UnusedPrivateFunction";
    public static final String UNUSED_FUNCTION_PARAMETER_KEY = "UnusedFunctionParameter";
    public static final String UNUSED_MODULE_VARIABLE_KEY = "UnusedModuleVariable";
    public static final String UNUSED_WITH_VARIABLE_KEY = "UnusedWithVariable";
    public static final String UNUSED_IMPORT_KEY = "UnusedImport";
    public static final String UNUSED_PATTERN_NAME_KEY = "UnusedPatternName";
    public static final String UNUSED_OPTIONAL_PARAMETER_KEY = "UnusedOptionalParameter";
    public static final String DEAD_AFTER_RETURN_KEY = "DeadCodeAfterReturn";
    public static final String UNREACHABLE_AFTER_ABORT_THROW_KEY = "UnreachableAfterAbortThrow";
    public static final String ASSIGNMENT_NEVER_READ_KEY = "AssignmentNeverRead";
    public static final String FUNCTION_DEFINED_NEVER_CALLED_KEY = "FunctionDefinedButNeverCalled";
    public static final String REDEFINED_WITHOUT_USE_KEY = "RedefinedWithoutUse";
    public static final String LOOP_VARIABLE_UNUSED_KEY = "LoopVariableUnused";
    public static final String CATCH_WITHOUT_THROW_KEY = "CatchWithoutThrow";
    public static final String CONDITION_ALWAYS_FALSE_KEY = "ConditionAlwaysFalse";

    // Rule keys - Chunk 2: Shadowing & Naming (Items 76-90 from ROADMAP_325.md)
    public static final String LOCAL_SHADOWS_GLOBAL_KEY = "LocalShadowsGlobal";
    public static final String PARAMETER_SHADOWS_BUILTIN_KEY = "ParameterShadowsBuiltin";
    public static final String LOCAL_SHADOWS_PARAMETER_KEY = "LocalVariableShadowsParameter";
    public static final String MULTIPLE_DEFINITIONS_SAME_SYMBOL_KEY = "MultipleDefinitionsSameSymbol";
    public static final String SYMBOL_NAME_TOO_SHORT_KEY = "SymbolNameTooShort";
    public static final String SYMBOL_NAME_TOO_LONG_KEY = "SymbolNameTooLong";
    public static final String INCONSISTENT_NAMING_CONVENTION_KEY = "InconsistentNamingConvention";
    public static final String BUILTIN_NAME_IN_LOCAL_SCOPE_KEY = "BuiltinNameInLocalScope";
    public static final String CONTEXT_CONFLICTS_KEY = "ContextConflicts";
    public static final String RESERVED_NAME_USAGE_KEY = "ReservedNameUsage";
    public static final String PRIVATE_CONTEXT_SYMBOL_PUBLIC_KEY = "PrivateContextSymbolPublic";
    public static final String MISMATCHED_BEGIN_END_KEY = "MismatchedBeginEnd";
    public static final String SYMBOL_AFTER_ENDPACKAGE_KEY = "SymbolAfterEndPackage";
    public static final String GLOBAL_IN_PACKAGE_KEY = "GlobalInPackage";
    public static final String TEMP_VARIABLE_NOT_TEMP_KEY = "TempVariableNotTemp";

    // Rule keys - Chunk 2: Undefined Symbol Detection (Items 91-100 from ROADMAP_325.md)
    public static final String UNDEFINED_FUNCTION_CALL_KEY = "UndefinedFunctionCall";
    public static final String UNDEFINED_VARIABLE_REFERENCE_KEY = "UndefinedVariableReference";
    public static final String TYPO_IN_BUILTIN_NAME_KEY = "TypoInBuiltinName";
    public static final String WRONG_CAPITALIZATION_KEY = "WrongCapitalization";
    public static final String MISSING_IMPORT_KEY = "MissingImport";
    public static final String CONTEXT_NOT_FOUND_KEY = "ContextNotFound";
    public static final String SYMBOL_MASKED_BY_IMPORT_KEY = "SymbolMaskedByImport";
    public static final String MISSING_PATH_ENTRY_KEY = "MissingPathEntry";
    public static final String CIRCULAR_NEEDS_KEY = "CircularNeeds";
    public static final String FORWARD_REFERENCE_WITHOUT_DECLARATION_KEY = "ForwardReferenceWithoutDeclaration";

    // Rule keys - Chunk 3: Type Mismatch Detection (Items 111-130 from ROADMAP_325.md)
    public static final String NUMERIC_OPERATION_ON_STRING_KEY = "NumericOperationOnString";
    public static final String STRING_OPERATION_ON_NUMBER_KEY = "StringOperationOnNumber";
    public static final String WRONG_ARGUMENT_TYPE_KEY = "WrongArgumentType";
    public static final String FUNCTION_RETURNS_WRONG_TYPE_KEY = "FunctionReturnsWrongType";
    public static final String COMPARISON_INCOMPATIBLE_TYPES_KEY = "ComparisonIncompatibleTypes";
    public static final String MIXED_NUMERIC_TYPES_KEY = "MixedNumericTypes";
    public static final String INTEGER_DIVISION_EXPECTING_REAL_KEY = "IntegerDivisionExpectingReal";
    public static final String LIST_FUNCTION_ON_ASSOCIATION_KEY = "ListFunctionOnAssociation";
    public static final String PATTERN_TYPE_MISMATCH_KEY = "PatternTypeMismatch";
    public static final String OPTIONAL_TYPE_INCONSISTENT_KEY = "OptionalTypeInconsistent";
    public static final String RETURN_TYPE_INCONSISTENT_KEY = "ReturnTypeInconsistent";
    public static final String NULL_ASSIGNMENT_TO_TYPED_VARIABLE_KEY = "NullAssignmentToTypedVariable";
    public static final String TYPE_CAST_WITHOUT_VALIDATION_KEY = "TypeCastWithoutValidation";
    public static final String IMPLICIT_TYPE_CONVERSION_KEY = "ImplicitTypeConversion";
    public static final String GRAPHICS_OBJECT_IN_NUMERIC_CONTEXT_KEY = "GraphicsObjectInNumericContext";
    public static final String SYMBOL_IN_NUMERIC_CONTEXT_KEY = "SymbolInNumericContext";
    public static final String IMAGE_OPERATION_ON_NON_IMAGE_KEY = "ImageOperationOnNonImage";
    public static final String SOUND_OPERATION_ON_NON_SOUND_KEY = "SoundOperationOnNonSound";
    public static final String DATASET_OPERATION_ON_LIST_KEY = "DatasetOperationOnList";
    public static final String GRAPH_OPERATION_ON_NON_GRAPH_KEY = "GraphOperationOnNonGraph";

    // Rule keys - Chunk 3: Data Flow Analysis (Items 135-150 from ROADMAP_325.md)
    public static final String UNINITIALIZED_VARIABLE_USE_ENHANCED_KEY = "UninitializedVariableUseEnhanced";
    public static final String VARIABLE_MAY_BE_UNINITIALIZED_KEY = "VariableMayBeUninitialized";
    public static final String DEAD_STORE_KEY = "DeadStore";
    public static final String OVERWRITTEN_BEFORE_READ_KEY = "OverwrittenBeforeRead";
    public static final String VARIABLE_ALIASING_ISSUE_KEY = "VariableAliasingIssue";
    public static final String MODIFICATION_OF_LOOP_ITERATOR_KEY = "ModificationOfLoopIterator";
    public static final String USE_OF_ITERATOR_OUTSIDE_LOOP_KEY = "UseOfIteratorOutsideLoop";
    public static final String READING_UNSET_VARIABLE_KEY = "ReadingUnsetVariable";
    public static final String DOUBLE_ASSIGNMENT_SAME_VALUE_KEY = "DoubleAssignmentSameValue";
    public static final String MUTATION_IN_PURE_FUNCTION_KEY = "MutationInPureFunction";
    public static final String SHARED_MUTABLE_STATE_KEY = "SharedMutableState";
    public static final String VARIABLE_SCOPE_ESCAPE_KEY = "VariableScopeEscape";
    public static final String CLOSURE_OVER_MUTABLE_VARIABLE_KEY = "ClosureOverMutableVariable";
    public static final String ASSIGNMENT_IN_CONDITION_ENHANCED_KEY = "AssignmentInConditionEnhanced";
    public static final String ASSIGNMENT_AS_RETURN_VALUE_KEY = "AssignmentAsReturnValue";
    public static final String VARIABLE_NEVER_MODIFIED_KEY = "VariableNeverModified";

    @Override
    public void define(Context context) {
        NewRepository repository = context
            .createRepository(REPOSITORY_KEY, MathematicaLanguage.KEY)
            .setName(REPOSITORY_NAME);

        // Define the commented-out code rule
        repository.createRule(COMMENTED_CODE_KEY)
            .setName("Sections of code should not be commented out")
            .setHtmlDescription(
                "<p>Programmers should not comment out code as it bloats programs and reduces readability.</p>" +
                "<p>Unused code should be deleted and can be retrieved from source control history if required.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* result = Solve[equation, x]; *)\n" +
                "(* \n" +
                "oldFunction[x_] := x^2 + 3x - 1;\n" +
                "*)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Calculate the result using a new algorithm *)\n" +
                "result = ImprovedSolve[equation, x];\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "clutter");

        // Define the magic number rule
        repository.createRule(MAGIC_NUMBER_KEY)
            .setName("Magic numbers should not be used")
            .setHtmlDescription(
                "<p>Magic numbers are unexplained numeric literals that make code harder to understand and maintain.</p>" +
                "<p>Replace magic numbers with named constants to improve readability.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "area = radius * 3.14159;\n" +
                "threshold = 42;\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "pi = 3.14159;\n" +
                "area = radius * pi;\n" +
                "defaultThreshold = 42;\n" +
                "threshold = defaultThreshold;\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability");

        // Define the TODO/FIXME comment rule
        repository.createRule(TODO_FIXME_KEY)
            .setName("Track TODO and FIXME comments")
            .setHtmlDescription(
                "<p>TODO and FIXME comments indicate incomplete or problematic code that needs attention.</p>" +
                "<p>These should be tracked and resolved systematically.</p>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "(* TODO: Add error handling for edge cases *)\n" +
                "(* FIXME: This algorithm has performance issues *)\n" +
                "</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("todo");

        // Define the empty block rule
        repository.createRule(EMPTY_BLOCK_KEY)
            .setName("Empty blocks should be removed")
            .setHtmlDescription(
                "<p>Empty code blocks are usually a sign of incomplete implementation or unnecessary code.</p>" +
                "<p>They should be either filled with proper logic or removed.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Module[{}, ]\n" +
                "If[condition, action, ]\n" +
                "Block[{x}, ]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Module[{result}, result = DoSomething[]]\n" +
                "If[condition, action, defaultAction]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious");

        // Define the function length rule
        repository.createRule(FUNCTION_LENGTH_KEY)
            .setName("Functions should not be too long")
            .setHtmlDescription(
                "<p>Long functions are hard to understand, test, and maintain.</p>" +
                "<p>Consider splitting large functions into smaller, focused functions.</p>" +
                "<p>Default maximum: 150 lines (configurable via sonar.mathematica.function.maximumLines)</p>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Define the file length rule
        repository.createRule(FILE_LENGTH_KEY)
            .setName("Files should not be too long")
            .setHtmlDescription(
                "<p>Files containing too many lines of code are difficult to navigate and maintain.</p>" +
                "<p>Consider splitting large files into multiple focused modules.</p>" +
                "<p>Default maximum: 1000 lines (configurable via sonar.mathematica.file.maximumLines)</p>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Define the empty catch block rule
        repository.createRule(EMPTY_CATCH_KEY)
            .setName("Exceptions should not be silently ignored")
            .setHtmlDescription(
                "<p>Empty exception handlers (catch blocks) hide errors and make debugging extremely difficult.</p>" +
                "<p>This is particularly dangerous for security-related operations where failures should be logged and monitored.</p>" +
                "<p>Always handle exceptions appropriately by logging them, re-throwing them, or taking corrective action.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Silently ignoring all errors *)\n" +
                "Check[riskyOperation[], $Failed];\n" +
                "\n" +
                "(* Catching but not handling *)\n" +
                "Catch[dangerousCode[]; $Failed];\n" +
                "\n" +
                "(* Quiet suppresses messages without logging *)\n" +
                "Quiet[securityCheck[]];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Log the error before returning default *)\n" +
                "Check[riskyOperation[],\n" +
                "  (Print[\"Error in riskyOperation: \", $MessageList]; $Failed)\n" +
                "];\n" +
                "\n" +
                "(* Handle specific error cases *)\n" +
                "result = Check[operation[],\n" +
                "  If[$MessageList =!= {},\n" +
                "    LogError[\"Operation failed\", $MessageList];\n" +
                "    NotifyAdmin[];\n" +
                "    $Failed\n" +
                "  ]\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/391.html'>CWE-391</a> - Unchecked Error Condition</li>" +
                "<li><a href='https://owasp.org/Top10/A09_2021-Security_Logging_and_Monitoring_Failures/'>OWASP Top 10 2021 A09</a> - Security Logging and Monitoring Failures</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("error-handling", "security", "owasp");

        // Define the debug code rule
        repository.createRule(DEBUG_CODE_KEY)
            .setName("Debug code should not be left in production")
            .setHtmlDescription(
                "<p>Debug statements left in production code can expose sensitive information, degrade performance, " +
                "and indicate incomplete development.</p>" +
                "<p>Print statements, trace functions, and debug flags should be removed before deployment.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Debug print statements *)\n" +
                "Print[\"User password: \", userPassword];\n" +
                "Echo[sensitiveData, \"Debug:\"];\n" +
                "\n" +
                "(* Debug tracing *)\n" +
                "TracePrint[securityFunction[credentials]];\n" +
                "Trace[authenticationLogic[]];\n" +
                "\n" +
                "(* Debug monitoring *)\n" +
                "Monitor[calculation[], progress];\n" +
                "\n" +
                "(* Debug messages enabled *)\n" +
                "$DebugMessages = True;\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use proper logging instead *)\n" +
                "If[$DevelopmentMode,\n" +
                "  WriteLog[\"Authentication attempt for user: \" <> username]\n" +
                "];\n" +
                "\n" +
                "(* Or remove debug code entirely *)\n" +
                "result = securityFunction[credentials];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/489.html'>CWE-489</a> - Active Debug Code</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/215.html'>CWE-215</a> - Information Exposure Through Debug Information</li>" +
                "<li><a href='https://owasp.org/Top10/A05_2021-Security_Misconfiguration/'>OWASP Top 10 2021 A05</a> - Security Misconfiguration</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("security", "owasp", "production-readiness");

        // ===== SECURITY RULES =====

        // Define hardcoded credentials rule
        repository.createRule(HARDCODED_CREDENTIALS_KEY)
            .setName("Credentials should not be hard-coded")
            .setHtmlDescription(
                "<p>Hard-coded credentials are a critical security risk. If the code is shared or leaked, " +
                "attackers can gain unauthorized access to systems and data.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "apiKey = \"sk_live_1234567890abcdef\";\n" +
                "password = \"mySecretPassword\";\n" +
                "awsAccessKey = \"AKIAIOSFODNN7EXAMPLE\";\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Load from environment or secure config *)\n" +
                "apiKey = Environment[\"API_KEY\"];\n" +
                "password = Import[\"!security-manager get-password\", \"String\"];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/798.html'>CWE-798</a> - Use of Hard-coded Credentials</li>" +
                "<li><a href='https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/'>OWASP Top 10 2021 A07</a></li>" +
                "</ul>"
            )
            .setSeverity("BLOCKER")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "security");

        // Define command injection rule
        repository.createRule(COMMAND_INJECTION_KEY)
            .setName("OS commands should not be constructed from user input")
            .setHtmlDescription(
                "<p>Constructing OS commands from user-controlled data can lead to command injection vulnerabilities.</p>" +
                "<p>Attackers can execute arbitrary system commands, potentially compromising the entire system.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Run[\"ls \" &lt;&gt; userInput];\n" +
                "RunProcess[{\"sh\", \"-c\", \"grep \" &lt;&gt; searchTerm}];\n" +
                "Import[\"!\" &lt;&gt; command, \"Text\"];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use safe APIs, validate input *)\n" +
                "files = FileNames[validatedPattern];\n" +
                "RunProcess[{\"grep\", searchTerm, \"file.txt\"}]; (* Array form is safer *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/78.html'>CWE-78</a> - OS Command Injection</li>" +
                "<li><a href='https://owasp.org/Top10/A03_2021-Injection/'>OWASP Top 10 2021 A03</a> - Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "injection", "security");

        // Define SQL injection rule
        repository.createRule(SQL_INJECTION_KEY)
            .setName("SQL queries should not be constructed from user input")
            .setHtmlDescription(
                "<p>Concatenating user input into SQL queries enables SQL injection attacks.</p>" +
                "<p>Use parameterized queries or prepared statements instead.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "SQLExecute[conn, \"SELECT * FROM users WHERE id=\" &lt;&gt; userId];\n" +
                "SQLSelect[conn, \"DELETE FROM data WHERE name='\" &lt;&gt; userName &lt;&gt; \"'\"];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use parameterized queries *)\n" +
                "SQLExecute[conn, \"SELECT * FROM users WHERE id=?\", {userId}];\n" +
                "SQLSelect[conn, SQLColumn[\"id\"], SQLTable[\"users\"], SQLWhere[\"id\" == userId]];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/89.html'>CWE-89</a> - SQL Injection</li>" +
                "<li><a href='https://owasp.org/Top10/A03_2021-Injection/'>OWASP Top 10 2021 A03</a> - Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "sql", "injection", "security");

        // Define code injection rule
        repository.createRule(CODE_INJECTION_KEY)
            .setName("Code should not be evaluated from user input")
            .setHtmlDescription(
                "<p>Using ToExpression or similar functions on user-controlled data allows code injection attacks.</p>" +
                "<p>Attackers can execute arbitrary Mathematica code with your application's privileges.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = ToExpression[userInput];\n" +
                "Evaluate[StringToExpression[formulaFromWeb]];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Parse and validate input, use safe evaluation *)\n" +
                "(* Only allow specific whitelisted functions *)\n" +
                "If[StringMatchQ[input, SafePattern], ToExpression[input], $Failed]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li>" +
                "<li><a href='https://owasp.org/Top10/A03_2021-Injection/'>OWASP Top 10 2021 A03</a> - Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "injection", "security");

        // Define path traversal rule
        repository.createRule(PATH_TRAVERSAL_KEY)
            .setName("File paths should not be constructed from user input")
            .setHtmlDescription(
                "<p>Constructing file paths from user input without validation can lead to path traversal attacks.</p>" +
                "<p>Attackers can access files outside the intended directory using sequences like '../'.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "data = Import[baseDir &lt;&gt; userFileName];\n" +
                "Export[outputPath &lt;&gt; requestedFile, content];\n" +
                "(* User supplies: \"../../etc/passwd\" *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Validate and sanitize paths *)\n" +
                "safeFileName = FileNameTake[userFileName]; (* Only basename *)\n" +
                "fullPath = FileNameJoin[{baseDir, safeFileName}];\n" +
                "If[StringStartsQ[fullPath, baseDir], Import[fullPath], $Failed]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/22.html'>CWE-22</a> - Path Traversal</li>" +
                "<li><a href='https://owasp.org/Top10/A01_2021-Broken_Access_Control/'>OWASP Top 10 2021 A01</a> - Broken Access Control</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "path-traversal", "security");

        // Define weak cryptography rule
        repository.createRule(WEAK_CRYPTOGRAPHY_KEY)
            .setName("Weak cryptographic algorithms should not be used")
            .setHtmlDescription(
                "<p>Using weak or broken cryptographic algorithms compromises data security.</p>" +
                "<p>MD5 and SHA-1 are cryptographically broken and should not be used for security purposes. " +
                "Using Mathematica's Random[] function for security tokens is unsafe as it's not cryptographically secure.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Weak hash algorithms *)\n" +
                "hash = Hash[data, \"MD5\"];\n" +
                "signature = Hash[message, \"SHA1\"];\n" +
                "\n" +
                "(* Using Random[] for security tokens *)\n" +
                "token = ToString[Random[Integer, {10^20, 10^21}]];\n" +
                "key = Table[Random[], {16}];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use strong hash algorithms *)\n" +
                "hash = Hash[data, \"SHA256\"];\n" +
                "signature = Hash[message, \"SHA512\"];\n" +
                "\n" +
                "(* Use cryptographically secure random *)\n" +
                "token = IntegerString[RandomInteger[{10^20, 10^21}], 16];\n" +
                "key = RandomInteger[{0, 255}, 16]; (* Use RandomInteger, not Random *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/327.html'>CWE-327</a> - Use of a Broken or Risky Cryptographic Algorithm</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/338.html'>CWE-338</a> - Use of Cryptographically Weak PRNG</li>" +
                "<li><a href='https://owasp.org/Top10/A02_2021-Cryptographic_Failures/'>OWASP Top 10 2021 A02</a> - Cryptographic Failures</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "cryptography", "security");

        // Define SSRF rule
        repository.createRule(SSRF_KEY)
            .setName("URLs should not be constructed from user input")
            .setHtmlDescription(
                "<p>Server-Side Request Forgery (SSRF) vulnerabilities allow attackers to make requests to unintended destinations.</p>" +
                "<p>Concatenating user input into URLs used by URLFetch, URLRead, Import, or ServiceExecute " +
                "can allow attackers to access internal services, cloud metadata endpoints, or other restricted resources.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* URL construction from user input *)\n" +
                "data = URLFetch[\"https://api.example.com/\" &lt;&gt; userEndpoint];\n" +
                "content = URLRead[baseURL &lt;&gt; userPath];\n" +
                "result = Import[\"https://\" &lt;&gt; userDomain &lt;&gt; \"/data.json\"];\n" +
                "response = ServiceExecute[service, \"Query\", {\"url\" -&gt; userURL}];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Validate against whitelist *)\n" +
                "allowedEndpoints = {\"users\", \"posts\", \"comments\"};\n" +
                "If[MemberQ[allowedEndpoints, userEndpoint],\n" +
                "  URLFetch[\"https://api.example.com/\" &lt;&gt; userEndpoint],\n" +
                "  $Failed\n" +
                "];\n" +
                "\n" +
                "(* Validate URL format and domain *)\n" +
                "If[StringMatchQ[userURL, \"https://trusted-domain.com/*\"],\n" +
                "  Import[userURL],\n" +
                "  $Failed\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/918.html'>CWE-918</a> - Server-Side Request Forgery</li>" +
                "<li><a href='https://owasp.org/Top10/A10_2021-Server-Side_Request_Forgery_%28SSRF%29/'>OWASP Top 10 2021 A10</a> - SSRF</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "ssrf", "security");

        // Define insecure deserialization rule
        repository.createRule(INSECURE_DESERIALIZATION_KEY)
            .setName("Deserialization of untrusted data should be avoided")
            .setHtmlDescription(
                "<p>Deserializing data from untrusted sources can lead to remote code execution and other attacks.</p>" +
                "<p>Mathematica's .mx and .wdx files can contain arbitrary code. Loading packages with Get[] from " +
                "user-controlled paths or untrusted URLs allows attackers to execute malicious code.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Loading serialized data from untrusted sources *)\n" +
                "data = Import[userFile, \"MX\"]; (* .mx files can execute code *)\n" +
                "dataset = Import[uploadedFile, \"WDX\"];\n" +
                "\n" +
                "(* Loading packages from user input *)\n" +
                "Get[userPackagePath]; (* Executes code from file *)\n" +
                "Get[\"http://\" &lt;&gt; userDomain &lt;&gt; \"/package.m\"];\n" +
                "\n" +
                "(* Evaluating expressions from strings *)\n" +
                "expr = ToExpression[Import[untrustedURL, \"String\"]];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use safe formats for untrusted data *)\n" +
                "data = Import[userFile, \"JSON\"]; (* JSON is data-only *)\n" +
                "data = Import[userFile, \"CSV\"];\n" +
                "\n" +
                "(* Validate file paths against whitelist *)\n" +
                "trustedPackages = {\"/usr/local/mathematica/packages/TrustedPackage.m\"};\n" +
                "If[MemberQ[trustedPackages, packagePath],\n" +
                "  Get[packagePath],\n" +
                "  $Failed\n" +
                "];\n" +
                "\n" +
                "(* Verify integrity with checksums before loading *)\n" +
                "If[Hash[Import[file, \"String\"], \"SHA256\"] === expectedHash,\n" +
                "  Get[file],\n" +
                "  $Failed\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/502.html'>CWE-502</a> - Deserialization of Untrusted Data</li>" +
                "<li><a href='https://owasp.org/Top10/A08_2021-Software_and_Data_Integrity_Failures/'>OWASP Top 10 2021 A08</a> - Software and Data Integrity Failures</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "deserialization", "security");

        // ===== BUG RULES (Reliability) =====

        // Define division by zero rule
        repository.createRule(DIVISION_BY_ZERO_KEY)
            .setName("Division operations should check for zero divisors")
            .setHtmlDescription(
                "<p>Division by zero causes runtime errors and program crashes.</p>" +
                "<p>Always validate that divisors are not zero before performing division operations.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = numerator / denominator;\n" +
                "value = x / (y - 5);  (* What if y == 5? *)\n" +
                "ratio = total / count;  (* What if count == 0? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Check before dividing *)\n" +
                "If[denominator != 0, numerator / denominator, $Failed];\n" +
                "\n" +
                "(* Or use Mathematica's safe division *)\n" +
                "result = Check[numerator / denominator, $Failed];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/369.html'>CWE-369</a> - Divide By Zero</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "error-handling");

        // Define assignment in conditional rule
        repository.createRule(ASSIGNMENT_IN_CONDITIONAL_KEY)
            .setName("Assignments should not be used in conditional expressions")
            .setHtmlDescription(
                "<p>Using assignment (=) instead of comparison (==, ===) in conditionals is a common bug.</p>" +
                "<p>This causes unintended assignment and always evaluates to True.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Assignment instead of comparison *)\n" +
                "If[x = 5, doSomething[]];  (* Sets x to 5, always True! *)\n" +
                "\n" +
                "While[status = \"running\", process[]];  (* Always loops! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use comparison operators *)\n" +
                "If[x == 5, doSomething[]];\n" +
                "If[x === 5, doSomething[]];  (* Strict equality *)\n" +
                "\n" +
                "While[status == \"running\", process[]];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/480.html'>CWE-480</a> - Use of Incorrect Operator</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "logic-error");

        // Define list index out of bounds rule
        repository.createRule(LIST_INDEX_OUT_OF_BOUNDS_KEY)
            .setName("List access should be bounds-checked")
            .setHtmlDescription(
                "<p>Accessing list elements without bounds checking can cause Part::partw errors at runtime.</p>" +
                "<p>Always verify index is within valid range before accessing list elements.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* No bounds checking *)\n" +
                "element = myList[[index]];\n" +
                "value = data[[userInput]];\n" +
                "first = items[[1]];  (* What if items is empty? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Check bounds before access *)\n" +
                "If[1 <= index <= Length[myList],\n" +
                "  myList[[index]],\n" +
                "  $Failed\n" +
                "];\n" +
                "\n" +
                "(* Use safe accessors *)\n" +
                "element = If[Length[items] > 0, First[items], $Failed];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/125.html'>CWE-125</a> - Out-of-bounds Read</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "error-handling");

        // Define infinite recursion rule
        repository.createRule(INFINITE_RECURSION_KEY)
            .setName("Recursive functions must have a base case")
            .setHtmlDescription(
                "<p>Recursive functions without proper base cases cause stack overflow errors.</p>" +
                "<p>Every recursive function must have at least one termination condition.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* No base case - infinite recursion! *)\n" +
                "factorial[n_] := n * factorial[n - 1];\n" +
                "\n" +
                "(* Base case never reached *)\n" +
                "count[x_] := If[x > 100, x, count[x + 1]];  (* But what if x starts > 100? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Proper base case *)\n" +
                "factorial[0] = 1;\n" +
                "factorial[n_] := n * factorial[n - 1];\n" +
                "\n" +
                "(* Multiple base cases *)\n" +
                "fibonacci[0] = 0;\n" +
                "fibonacci[1] = 1;\n" +
                "fibonacci[n_] := fibonacci[n-1] + fibonacci[n-2];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/674.html'>CWE-674</a> - Uncontrolled Recursion</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "stack-overflow");

        // Define unreachable pattern rule
        repository.createRule(UNREACHABLE_PATTERN_KEY)
            .setName("Pattern definitions should not be unreachable")
            .setHtmlDescription(
                "<p>When multiple patterns are defined for the same function, more specific patterns must come before general ones.</p>" +
                "<p>Otherwise, the specific patterns will never match because the general pattern catches everything first.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* General pattern first - specific patterns never match! *)\n" +
                "process[x_] := defaultProcess[x];\n" +
                "process[x_Integer] := integerProcess[x];  (* NEVER CALLED *)\n" +
                "process[x_String] := stringProcess[x];    (* NEVER CALLED *)\n" +
                "\n" +
                "(* Overlapping patterns *)\n" +
                "calculate[n_] := n^2;\n" +
                "calculate[n_?Positive] := n^3;  (* NEVER CALLED - all numbers match n_ first *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Specific patterns first, general last *)\n" +
                "process[x_Integer] := integerProcess[x];\n" +
                "process[x_String] := stringProcess[x];\n" +
                "process[x_] := defaultProcess[x];  (* Catch-all last *)\n" +
                "\n" +
                "(* Most specific first *)\n" +
                "calculate[n_?Positive] := n^3;\n" +
                "calculate[n_] := n^2;\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "logic-error", "pattern-matching");

        // ===== SECURITY HOTSPOT RULES =====

        // Define file upload validation rule
        repository.createRule(FILE_UPLOAD_VALIDATION_KEY)
            .setName("File uploads should be validated")
            .setHtmlDescription(
                "<p>File uploads from users should be validated for type, size, and content before processing.</p>" +
                "<p>Unvalidated file uploads can lead to malicious file execution, denial of service, or data exfiltration.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper validation is in place.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>File extension is validated against whitelist</li>" +
                "<li>File size is checked and limited</li>" +
                "<li>File content type is verified (not just extension)</li>" +
                "<li>Files are scanned for malware if possible</li>" +
                "<li>Uploaded files are stored outside web root</li>" +
                "<li>File names are sanitized (no path traversal)</li>" +
                "</ul>" +
                "<h2>Example File Operations to Review</h2>" +
                "<pre>\n" +
                "(* Review these operations *)\n" +
                "Import[uploadedFile];  (* What type? Size? Content? *)\n" +
                "Get[userProvidedPath];  (* Could load malicious code! *)\n" +
                "Import[formData[\"file\"], \"MX\"];  (* MX files execute code! *)\n" +
                "</pre>" +
                "<h2>Secure Validation Example</h2>" +
                "<pre>\n" +
                "(* Validate extension *)\n" +
                "allowedExtensions = {\".csv\", \".json\", \".txt\"};\n" +
                "ext = FileExtension[uploadedFile];\n" +
                "If[!MemberQ[allowedExtensions, \".\" <> ext], Return[$Failed]];\n" +
                "\n" +
                "(* Check file size *)\n" +
                "maxSize = 10 * 1024 * 1024;  (* 10MB *)\n" +
                "If[FileSize[uploadedFile] > maxSize, Return[$Failed]];\n" +
                "\n" +
                "(* Use safe import formats only *)\n" +
                "data = Import[uploadedFile, \"CSV\"];  (* CSV is data-only *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/434.html'>CWE-434</a> - Unrestricted Upload of File with Dangerous Type</li>" +
                "<li><a href='https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload'>OWASP</a> - Unrestricted File Upload</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "file-upload", "owasp");

        // Define external API safeguards rule
        repository.createRule(EXTERNAL_API_SAFEGUARDS_KEY)
            .setName("External API calls should have proper safeguards")
            .setHtmlDescription(
                "<p>Calls to external APIs should have proper error handling, timeouts, and rate limiting.</p>" +
                "<p>Without safeguards, API calls can cause performance issues, expose sensitive errors, or enable abuse.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper safeguards are in place.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Timeout is set (don't wait forever)</li>" +
                "<li>Rate limiting is implemented (prevent abuse)</li>" +
                "<li>Errors are caught and logged (don't expose stack traces)</li>" +
                "<li>Sensitive data is not logged (API keys, tokens)</li>" +
                "<li>Retry logic has exponential backoff</li>" +
                "<li>Circuit breaker pattern for failing services</li>" +
                "</ul>" +
                "<h2>Example API Calls to Review</h2>" +
                "<pre>\n" +
                "(* Review these operations *)\n" +
                "URLRead[apiEndpoint];  (* Timeout? Error handling? *)\n" +
                "URLExecute[\"POST\", url, data];  (* Rate limiting? *)\n" +
                "ServiceExecute[service, \"Query\", params];  (* What if service is down? *)\n" +
                "</pre>" +
                "<h2>Secure API Call Example</h2>" +
                "<pre>\n" +
                "(* Add timeout and error handling *)\n" +
                "result = TimeConstrained[\n" +
                "  Check[\n" +
                "    URLRead[apiEndpoint],\n" +
                "    (LogError[\"API call failed\"]; $Failed)\n" +
                "  ],\n" +
                "  30  (* 30 second timeout *)\n" +
                "];\n" +
                "\n" +
                "(* Implement rate limiting *)\n" +
                "If[apiCallCount > maxCallsPerMinute,\n" +
                "  Pause[60];  (* Wait before next call *)\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/400.html'>CWE-400</a> - Uncontrolled Resource Consumption</li>" +
                "<li><a href='https://owasp.org/www-community/controls/Blocking_Brute_Force_Attacks'>OWASP</a> - Rate Limiting</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "api", "availability");

        // Define crypto key generation rule
        repository.createRule(CRYPTO_KEY_GENERATION_KEY)
            .setName("Cryptographic keys should be generated securely")
            .setHtmlDescription(
                "<p>Cryptographic keys and secrets must be generated using secure methods with sufficient entropy.</p>" +
                "<p>Weak key generation can compromise the entire security of encrypted data.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure secure key generation.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Using RandomInteger (not Random) for cryptographic purposes</li>" +
                "<li>Key length is sufficient (256 bits minimum for symmetric)</li>" +
                "<li>Keys are generated with cryptographically secure randomness</li>" +
                "<li>Keys are stored securely (not in code or logs)</li>" +
                "<li>Keys are rotated regularly</li>" +
                "<li>Consider using established crypto libraries</li>" +
                "</ul>" +
                "<h2>Example Key Generation to Review</h2>" +
                "<pre>\n" +
                "(* Review these operations *)\n" +
                "key = Table[Random[], {16}];  (* Random is NOT cryptographically secure! *)\n" +
                "password = ToString[Random[Integer, {1000, 9999}]];  (* Too short! *)\n" +
                "secret = IntegerString[RandomInteger[999999], 16];  (* Too little entropy! *)\n" +
                "</pre>" +
                "<h2>Secure Key Generation Example</h2>" +
                "<pre>\n" +
                "(* Use RandomInteger with sufficient length *)\n" +
                "aesKey = RandomInteger[{0, 255}, 32];  (* 256-bit key *)\n" +
                "\n" +
                "(* Generate secure token *)\n" +
                "token = IntegerString[RandomInteger[{10^30, 10^31 - 1}], 16];\n" +
                "\n" +
                "(* Store securely, don't log *)\n" +
                "Export[\"/secure/path/key.bin\", aesKey, \"Byte\"];\n" +
                "SystemExecute[\"chmod\", \"600\", \"/secure/path/key.bin\"];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/326.html'>CWE-326</a> - Inadequate Encryption Strength</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/330.html'>CWE-330</a> - Use of Insufficiently Random Values</li>" +
                "<li><a href='https://owasp.org/Top10/A02_2021-Cryptographic_Failures/'>OWASP Top 10 2021 A02</a> - Cryptographic Failures</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "cryptography", "owasp");

        // ===== NEW CODE SMELL RULES (Phase 2) =====

        // Unused Variables
        repository.createRule(UNUSED_VARIABLES_KEY)
            .setName("Variables should not be declared and not used")
            .setHtmlDescription(
                "<p>Variables declared in Module, Block, or With but never used waste memory and reduce code clarity.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Module[{x, y, z},\n" +
                "  x = 5;\n" +
                "  x + 10\n" +
                "];  (* y and z are declared but never used *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Module[{x},\n" +
                "  x = 5;\n" +
                "  x + 10\n" +
                "];\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "clutter");

        // Duplicate Function Definitions
        repository.createRule(DUPLICATE_FUNCTION_KEY)
            .setName("Functions should not be redefined with same signature")
            .setHtmlDescription(
                "<p>Defining the same function multiple times with the same pattern signature overwrites previous definitions.</p>" +
                "<p>This is usually a mistake and causes confusion about which definition is active.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "myFunc[x_] := x^2;  (* First definition *)\n" +
                "myFunc[y_] := y^3;  (* Overwrites first! Both have same pattern x_ *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use different patterns *)\n" +
                "myFunc[x_Integer] := x^2;\n" +
                "myFunc[x_Real] := x^3;\n" +
                "\n" +
                "(* Or use different function names *)\n" +
                "myFuncSquare[x_] := x^2;\n" +
                "myFuncCube[x_] := x^3;\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("confusing", "pitfall");

        // Too Many Parameters
        repository.createRule(TOO_MANY_PARAMETERS_KEY)
            .setName("Functions should not have too many parameters")
            .setHtmlDescription(
                "<p>Functions with more than 7 parameters are difficult to use and maintain.</p>" +
                "<p>Consider using associations or grouping related parameters into structures.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "processData[name_, age_, address_, phone_, email_, city_, state_, zip_] := ...\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use an Association *)\n" +
                "processData[userData_Association] := ...\n" +
                "processData[<|\"name\" -> \"John\", \"age\" -> 30, ...|>]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Deeply Nested Conditionals
        repository.createRule(DEEPLY_NESTED_KEY)
            .setName("Conditionals should not be nested too deeply")
            .setHtmlDescription(
                "<p>Deeply nested If/Which/Switch statements (more than 3 levels) are difficult to understand and test.</p>" +
                "<p>Consider extracting nested logic into separate functions or using Which for multiple conditions.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[a,\n" +
                "  If[b,\n" +
                "    If[c,\n" +
                "      If[d, result]  (* 4 levels deep! *)\n" +
                "    ]\n" +
                "  ]\n" +
                "]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use Which for multiple conditions *)\n" +
                "Which[\n" +
                "  a && b && c && d, result,\n" +
                "  a && b && c, otherResult,\n" +
                "  True, defaultResult\n" +
                "]\n" +
                "\n" +
                "(* Or extract to helper functions *)\n" +
                "If[a, processA[], defaultResult]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload", "complexity");

        // Missing Documentation
        repository.createRule(MISSING_DOCUMENTATION_KEY)
            .setName("Public functions should be documented")
            .setHtmlDescription(
                "<p>Public functions (starting with uppercase) should have usage documentation.</p>" +
                "<p>This helps users understand what the function does without reading the implementation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "ProcessUserData[data_, options_] := Module[{...}, ...]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* ProcessUserData[data, options] processes user data and returns cleaned result.\n" +
                "   Parameters:\n" +
                "     data: List of user records\n" +
                "     options: Association of processing options\n" +
                "   Returns: Processed data list\n" +
                "*)\n" +
                "ProcessUserData[data_, options_] := Module[{...}, ...]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation");

        // Inconsistent Naming
        repository.createRule(INCONSISTENT_NAMING_KEY)
            .setName("Naming conventions should be consistent")
            .setHtmlDescription(
                "<p>Mixing different naming conventions (camelCase, PascalCase, snake_case) in the same file reduces readability.</p>" +
                "<p>Mathematica convention: PascalCase for public functions, camelCase for private.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "ProcessData[x_] := ...    (* PascalCase *)\n" +
                "calculateResult[y_] := ... (* camelCase *)\n" +
                "get_user_name[] := ...    (* snake_case - inconsistent! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Consistent PascalCase for public *)\n" +
                "ProcessData[x_] := ...\n" +
                "CalculateResult[y_] := ...\n" +
                "GetUserName[] := ...\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("convention");

        // Identical Branches
        repository.createRule(IDENTICAL_BRANCHES_KEY)
            .setName("Conditional branches should not be identical")
            .setHtmlDescription(
                "<p>If/Which with identical then/else branches is a copy-paste error or indicates dead code.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[condition, DoSomething[], DoSomething[]]  (* Both branches identical *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* If both branches are same, condition is useless *)\n" +
                "DoSomething[]\n" +
                "\n" +
                "(* Or fix the copy-paste error *)\n" +
                "If[condition, DoSomething[], DoSomethingElse[]]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious", "pitfall");

        // Expression Too Complex
        repository.createRule(EXPRESSION_TOO_COMPLEX_KEY)
            .setName("Expressions should not be too complex")
            .setHtmlDescription(
                "<p>Single expressions with more than 20 operations should be split into intermediate steps.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = a + b * c - d / e ^ f + g * h - i / j + k * l - m / n + o * p;\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "term1 = b * c + a;\n" +
                "term2 = d / e ^ f;\n" +
                "term3 = g * h - i / j;\n" +
                "result = term1 - term2 + term3;\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Deprecated Functions
        repository.createRule(DEPRECATED_FUNCTION_KEY)
            .setName("Deprecated functions should not be used")
            .setHtmlDescription(
                "<p>Some Mathematica functions have been deprecated in favor of newer alternatives.</p>" +
                "<p>Using deprecated functions may cause compatibility issues in future versions.</p>" +
                "<h2>Examples of Deprecated Functions</h2>" +
                "<ul>" +
                "<li><code>$RecursionLimit</code> - Use <code>$IterationLimit</code> or explicit checks</li>" +
                "<li><code>Sqrt[-1]</code> pattern - Use <code>I</code> directly</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("obsolete");

        // Empty Statement
        repository.createRule(EMPTY_STATEMENT_KEY)
            .setName("Empty statements should be removed")
            .setHtmlDescription(
                "<p>Empty statements created by double semicolons or misplaced semicolons are usually mistakes.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "x = 5;;  (* Double semicolon *)\n" +
                "If[condition, ;]  (* Empty statement in branch *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "x = 5;\n" +
                "If[condition, DoSomething[]]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious");

        // ===== NEW BUG RULES (Phase 2) =====

        // Floating Point Equality
        repository.createRule(FLOATING_POINT_EQUALITY_KEY)
            .setName("Floating point numbers should not be tested for equality")
            .setHtmlDescription(
                "<p>Using == or === to compare floating point numbers is unreliable due to rounding errors.</p>" +
                "<p>Use a tolerance-based comparison instead.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[0.1 + 0.2 == 0.3, ...]  (* May be False due to rounding! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use tolerance-based comparison *)\n" +
                "If[Abs[(0.1 + 0.2) - 0.3] < 10^-10, ...]\n" +
                "\n" +
                "(* Or use Mathematica's Chop *)\n" +
                "If[Chop[0.1 + 0.2 - 0.3] == 0, ...]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/1077.html'>CWE-1077</a> - Floating Point Comparison</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "floating-point");

        // Function Without Return
        repository.createRule(FUNCTION_WITHOUT_RETURN_KEY)
            .setName("Functions should return a value")
            .setHtmlDescription(
                "<p>Functions that end with a semicolon return Null, which is usually unintended.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "calculateResult[x_] := (\n" +
                "  result = x^2 + x;\n" +
                "  Print[result];\n" +
                ");  (* Returns Null! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "calculateResult[x_] := (\n" +
                "  result = x^2 + x;\n" +
                "  Print[result];\n" +
                "  result  (* Return the value *)\n" +
                ");\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Variable Before Assignment
        repository.createRule(VARIABLE_BEFORE_ASSIGNMENT_KEY)
            .setName("Variables should not be used before assignment")
            .setHtmlDescription(
                "<p>Using a variable before assigning it a value leads to undefined behavior.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Module[{x, y},\n" +
                "  y = x + 5;  (* x used before being assigned *)\n" +
                "  x = 10;\n" +
                "  y\n" +
                "]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Module[{x, y},\n" +
                "  x = 10;\n" +
                "  y = x + 5;\n" +
                "  y\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Off-by-One
        repository.createRule(OFF_BY_ONE_KEY)
            .setName("Loop ranges should not cause off-by-one errors")
            .setHtmlDescription(
                "<p>Common indexing errors: starting at 0 (Mathematica lists are 1-indexed) or going beyond Length.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Do[Print[list[[i]]], {i, 0, Length[list]}]  (* 0 is invalid! *)\n" +
                "Do[Print[list[[i]]], {i, 1, Length[list] + 1}]  (* Beyond length! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Do[Print[list[[i]]], {i, 1, Length[list]}]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Infinite Loop
        repository.createRule(INFINITE_LOOP_KEY)
            .setName("While loops should have an exit condition")
            .setHtmlDescription(
                "<p>While[True] without Break or Return inside creates an infinite loop.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "While[True, DoSomething[]]  (* No way to exit! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "While[True,\n" +
                "  DoSomething[];\n" +
                "  If[condition, Break[]]\n" +
                "]\n" +
                "\n" +
                "(* Or use proper condition *)\n" +
                "While[!done, DoSomething[]]\n" +
                "</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Mismatched Dimensions
        repository.createRule(MISMATCHED_DIMENSIONS_KEY)
            .setName("Matrix operations should use rectangular arrays")
            .setHtmlDescription(
                "<p>Operations like Transpose, Dot assume rectangular (uniform) arrays.</p>" +
                "<p>Non-rectangular arrays cause errors or unexpected results.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "data = {{1, 2}, {3, 4, 5}};  (* Non-rectangular *)\n" +
                "Transpose[data]  (* Will fail *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Ensure rectangular before matrix ops *)\n" +
                "If[Apply[SameQ, Map[Length, data]],\n" +
                "  Transpose[data],\n" +
                "  $Failed\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Type Mismatch
        repository.createRule(TYPE_MISMATCH_KEY)
            .setName("Operations should use compatible types")
            .setHtmlDescription(
                "<p>Mixing incompatible types in operations leads to errors or unexpected results.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = \"hello\" + 5  (* String + Number error *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "result = \"hello\" <> ToString[5]  (* String concatenation *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Suspicious Pattern
        repository.createRule(SUSPICIOUS_PATTERN_KEY)
            .setName("Pattern matching should not have contradictions")
            .setHtmlDescription(
                "<p>Patterns with contradictory constraints will never match or match too broadly.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Impossible constraint *)\n" +
                "func[x_Integer /; x > 10 && x < 5] := ...  (* Never matches *)\n" +
                "\n" +
                "(* Too broad *)\n" +
                "func[___] := ...  (* Matches everything including 0 arguments *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "func[x_Integer /; x > 10] := ...\n" +
                "func[x__] := ...  (* At least one argument *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "pattern-matching");

        // ===== NEW VULNERABILITY RULES (Phase 2) =====

        // Unsafe Symbol Construction
        repository.createRule(UNSAFE_SYMBOL_KEY)
            .setName("Symbol construction from user input should be avoided")
            .setHtmlDescription(
                "<p>Using Symbol[] or ToExpression to dynamically construct function names from user input allows code injection.</p>" +
                "<p>Attackers can control which functions are called with your data.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "funcName = userInput;  (* e.g., \"DeleteFile\" *)\n" +
                "Symbol[funcName][userFile]  (* User controls function call! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use explicit whitelist *)\n" +
                "allowedFunctions = <|\"Save\" -> SaveData, \"Load\" -> LoadData|>;\n" +
                "If[KeyExistsQ[allowedFunctions, userInput],\n" +
                "  allowedFunctions[userInput][userFile],\n" +
                "  $Failed\n" +
                "]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/470.html'>CWE-470</a> - Use of Externally-Controlled Input</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "injection", "security");

        // XML External Entity (XXE)
        repository.createRule(XXE_KEY)
            .setName("XML imports should disable external entity processing")
            .setHtmlDescription(
                "<p>Importing XML without disabling external entities allows XML External Entity (XXE) attacks.</p>" +
                "<p>Attackers can read local files, perform SSRF, or cause denial of service.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Import[userFile, \"XML\"]  (* XXE vulnerable! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Disable DTD processing *)\n" +
                "Import[userFile, {\"XML\", \"ProcessDTD\" -> False}]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/611.html'>CWE-611</a> - XML External Entity</li>" +
                "<li><a href='https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing'>OWASP</a> - XXE</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "xxe", "security");

        // Missing Input Sanitization
        repository.createRule(MISSING_SANITIZATION_KEY)
            .setName("User input should be sanitized before use with dangerous functions")
            .setHtmlDescription(
                "<p>User input passed directly to dangerous functions without validation enables attacks.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "DeleteFile[userProvidedPath]  (* User could delete anything! *)\n" +
                "SystemOpen[userURL]  (* User could open malicious URLs *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Validate against whitelist *)\n" +
                "allowedFiles = {\"/tmp/file1.txt\", \"/tmp/file2.txt\"};\n" +
                "If[MemberQ[allowedFiles, userPath],\n" +
                "  DeleteFile[userPath],\n" +
                "  $Failed\n" +
                "]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/20.html'>CWE-20</a> - Improper Input Validation</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "validation", "security");

        // Insecure Random (Expanded)
        repository.createRule(INSECURE_RANDOM_EXPANDED_KEY)
            .setName("Secure random should be used for security-sensitive operations")
            .setHtmlDescription(
                "<p>Using Random[] or predictable RandomChoice for session tokens, passwords, or keys is insecure.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Insecure random for tokens *)\n" +
                "sessionToken = StringJoin @@ RandomChoice[CharacterRange[\"a\", \"z\"], 20];\n" +
                "\n" +
                "(* Random[] is not cryptographically secure *)\n" +
                "secretKey = Table[Random[], {32}];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use RandomInteger for crypto *)\n" +
                "sessionToken = IntegerString[RandomInteger[{10^40, 10^41 - 1}], 16];\n" +
                "secretKey = RandomInteger[{0, 255}, 32];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/338.html'>CWE-338</a> - Weak PRNG</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "cryptography", "security");

        // ===== NEW SECURITY HOTSPOT RULES (Phase 2) =====

        // Network Operations
        repository.createRule(NETWORK_OPERATIONS_KEY)
            .setName("Network operations should be reviewed for security")
            .setHtmlDescription(
                "<p>Network operations expose the application to external systems and should be reviewed.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper security measures.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>TLS/SSL is used for sensitive data</li>" +
                "<li>Certificate validation is enabled</li>" +
                "<li>Timeouts are set to prevent hanging</li>" +
                "<li>Authentication is required where appropriate</li>" +
                "<li>Error messages don't leak sensitive information</li>" +
                "</ul>" +
                "<h2>Example Operations to Review</h2>" +
                "<pre>\n" +
                "SocketConnect[\"server.com\", 8080]  (* Check: TLS? Auth? *)\n" +
                "SocketOpen[8080]  (* Check: What data is exposed? *)\n" +
                "WebExecute[session, \"Click\", ...]  (* Check: Auth? Session security? *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "network");

        // File System Modifications
        repository.createRule(FILE_SYSTEM_MODIFICATIONS_KEY)
            .setName("Destructive file operations should be reviewed")
            .setHtmlDescription(
                "<p>Destructive file system operations should be reviewed for security and safety.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper safeguards.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Path validation prevents directory traversal</li>" +
                "<li>User permissions are checked</li>" +
                "<li>Critical files are protected</li>" +
                "<li>Operations are logged for audit</li>" +
                "<li>Rollback/undo is possible if needed</li>" +
                "</ul>" +
                "<h2>Example Operations to Review</h2>" +
                "<pre>\n" +
                "DeleteFile[path]  (* Check: Path validated? Logged? *)\n" +
                "RenameFile[old, new]  (* Check: Both paths validated? *)\n" +
                "SetFileDate[file, ...]  (* Check: Why modifying timestamps? *)\n" +
                "CopyFile[src, dst]  (* Check: Destination validated? *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "file-system");

        // Environment Variables
        repository.createRule(ENVIRONMENT_VARIABLE_KEY)
            .setName("Environment variable access should be reviewed")
            .setHtmlDescription(
                "<p>Environment variables may contain secrets and should be reviewed for proper handling.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure secrets are protected.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Environment variable values are not logged</li>" +
                "<li>Values are not exposed in error messages</li>" +
                "<li>Secrets are not passed to external systems</li>" +
                "<li>Variables are not used in URLs or queries</li>" +
                "<li>Consider using secure secret management instead</li>" +
                "</ul>" +
                "<h2>Example to Review</h2>" +
                "<pre>\n" +
                "apiKey = Environment[\"SECRET_API_KEY\"]  (* Check: Logged anywhere? *)\n" +
                "URLFetch[\"https://api.com?key=\" <> Environment[\"KEY\"]]  (* Exposed in URL! *)\n" +
                "</pre>" +
                "<h2>Secure Practices</h2>" +
                "<pre>\n" +
                "(* Don't log secrets *)\n" +
                "apiKey = Environment[\"SECRET_API_KEY\"];\n" +
                "(* Use in headers, not URLs *)\n" +
                "URLFetch[url, \"Headers\" -> {\"Authorization\" -> \"Bearer \" <> apiKey}]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "secrets");

        // ===== PHASE 3 RULES (25 rules) =====

        // PERFORMANCE ISSUES (8 rules)

        repository.createRule(APPEND_IN_LOOP_KEY)
            .setName("AppendTo should not be used in loops")
            .setHtmlDescription(
                "<p>Using AppendTo or Append inside loops creates O(n²) performance due to repeated list copying.</p>" +
                "<p>Use Table, Sow/Reap, or build a list then Join instead.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = {};\n" +
                "Do[result = Append[result, f[i]], {i, 1000}]  (* O(n²) - Very slow! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use Table - O(n) *)\n" +
                "result = Table[f[i], {i, 1000}]\n\n" +
                "(* Or Sow/Reap *)\n" +
                "result = Reap[Do[Sow[f[i]], {i, 1000}]][[2, 1]]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/1050.html'>CWE-1050</a> - Excessive Platform Resource Consumption</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "mathematica-specific");

        repository.createRule(REPEATED_FUNCTION_CALLS_KEY)
            .setName("Expensive function calls should not be repeated")
            .setHtmlDescription(
                "<p>Calling the same function multiple times with identical arguments wastes computation.</p>" +
                "<p>Cache the result in a variable or use memoization.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = ExpensiveComputation[data] + ExpensiveComputation[data]  (* Computes twice! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "cached = ExpensiveComputation[data];\n" +
                "result = cached + cached\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(STRING_CONCAT_IN_LOOP_KEY)
            .setName("String concatenation should not be used in loops")
            .setHtmlDescription(
                "<p>Using <> to concatenate strings in loops is O(n²) due to string immutability.</p>" +
                "<p>Collect strings in a list, then use StringJoin.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = \"\";\n" +
                "Do[result = result <> ToString[i], {i, 1000}]  (* O(n²) *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "result = StringJoin[Table[ToString[i], {i, 1000}]]  (* O(n) *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(UNCOMPILED_NUMERICAL_KEY)
            .setName("Numerical loops should use Compile")
            .setHtmlDescription(
                "<p>Numerical computations in loops can be 10-100x faster when compiled.</p>" +
                "<p>Consider using Compile for numerical code with loops.</p>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "(* Without Compile - slower *)\n" +
                "sum = 0; Do[sum += i^2, {i, 10000}]\n\n" +
                "(* With Compile - much faster *)\n" +
                "compiled = Compile[{}, Module[{sum = 0}, Do[sum += i^2, {i, 10000}]; sum]];\n" +
                "result = compiled[]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "optimization");

        repository.createRule(PACKED_ARRAY_BREAKING_KEY)
            .setName("Operations should preserve packed arrays")
            .setHtmlDescription(
                "<p>Packed arrays are 10x+ faster than unpacked arrays.</p>" +
                "<p>Avoid operations that unpack arrays (mixing types, using symbolic expressions).</p>" +
                "<h2>Operations that Unpack Arrays</h2>" +
                "<ul>" +
                "<li>Mixing integers and reals</li>" +
                "<li>Using symbolic values</li>" +
                "<li>Applying non-numerical functions</li>" +
                "</ul>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "arrays");

        repository.createRule(NESTED_MAP_TABLE_KEY)
            .setName("Nested Map/Table should be refactored")
            .setHtmlDescription(
                "<p>Nested Map or Table calls can often be replaced with more efficient single operations.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = Table[Table[i*j, {j, 10}], {i, 10}]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "result = Table[i*j, {i, 10}, {j, 10}]  (* More efficient *)\n" +
                "(* Or use Outer for many cases *)\n" +
                "result = Outer[Times, Range[10], Range[10]]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "readability");

        repository.createRule(LARGE_TEMP_EXPRESSIONS_KEY)
            .setName("Large temporary expressions should be assigned to variables")
            .setHtmlDescription(
                "<p>Large intermediate results (>100MB) that aren't assigned can cause memory issues.</p>" +
                "<p>Assign large results to variables to make memory usage explicit.</p>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("memory", "performance");

        repository.createRule(PLOT_IN_LOOP_KEY)
            .setName("Plotting functions should not be called in loops")
            .setHtmlDescription(
                "<p>Generating plots in loops is very slow. Collect data first, then plot once.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Do[ListPlot[data[[i]]], {i, 100}]  (* Creates 100 separate plots *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "ListPlot[data, PlotRange -> All]  (* One plot with all data *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "visualization");

        // PATTERN MATCHING ISSUES (5 rules)

        repository.createRule(MISSING_PATTERN_TEST_KEY)
            .setName("Numeric functions should test argument types")
            .setHtmlDescription(
                "<p>Functions expecting numeric arguments should use pattern tests to prevent symbolic evaluation errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "f[x_] := Sqrt[x] + x^2  (* Will evaluate symbolically for f[a] *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "f[x_?NumericQ] := Sqrt[x] + x^2  (* Only evaluates for numbers *)\n" +
                "(* Or use _Real, _Integer, etc. *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/704.html'>CWE-704</a> - Incorrect Type Conversion</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "type-safety");

        repository.createRule(PATTERN_BLANKS_MISUSE_KEY)
            .setName("Pattern blanks should be used correctly")
            .setHtmlDescription(
                "<p>Using __ or ___ creates sequences, not lists. This often causes errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "f[x__] := Length[x]  (* ERROR: x is a sequence, Length expects list *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "f[x__] := Length[{x}]  (* Wrap sequence in list *)\n" +
                "(* Or use List pattern *)\n" +
                "f[x_List] := Length[x]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "pattern-matching");

        repository.createRule(SET_DELAYED_CONFUSION_KEY)
            .setName("Use SetDelayed (:=) for function definitions")
            .setHtmlDescription(
                "<p>Using Set (=) instead of SetDelayed (:=) evaluates the right-hand side immediately, which is usually wrong for functions.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "f[x_] = RandomReal[]  (* Evaluates once, same random number always returned! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "f[x_] := RandomReal[]  (* Evaluates each time function is called *)\n" +
                "</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "common-mistake");

        repository.createRule(SYMBOL_NAME_COLLISION_KEY)
            .setName("User symbols should not shadow built-in functions")
            .setHtmlDescription(
                "<p>Defining functions with single-letter names or common words collides with Mathematica built-ins.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "N[x_] := ...  (* Shadows built-in N[] for numerical evaluation! *)\n" +
                "D[x_] := ...  (* Shadows built-in D[] for derivatives! *)\n" +
                "I = 5;  (* Shadows imaginary unit I! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "myN[x_] := ...\n" +
                "derivative[x_] := ...\n" +
                "index = 5;\n" +
                "</pre>" +
                "<h2>Common Built-ins to Avoid</h2>" +
                "<p>N, D, I, C, O, E, K, Pi, Re, Im, Abs, Min, Max, Log, Sin, Cos</p>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "naming");

        repository.createRule(BLOCK_MODULE_MISUSE_KEY)
            .setName("Block and Module should be used correctly")
            .setHtmlDescription(
                "<p>Block provides dynamic scope, Module provides lexical scope. Using the wrong one causes bugs.</p>" +
                "<h2>When to Use Each</h2>" +
                "<ul>" +
                "<li><strong>Module</strong>: For local variables (most common case)</li>" +
                "<li><strong>Block</strong>: To temporarily change global values</li>" +
                "<li><strong>With</strong>: For constant local values</li>" +
                "</ul>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "(* Use Module for local variables *)\n" +
                "f[x_] := Module[{temp = x^2}, temp + 1]\n\n" +
                "(* Use Block to temporarily override globals *)\n" +
                "Block[{$RecursionLimit = 1024}, RecursiveFunction[]]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "scope");

        // BEST PRACTICES (7 rules)

        repository.createRule(GENERIC_VARIABLE_NAMES_KEY)
            .setName("Variables should have meaningful names")
            .setHtmlDescription(
                "<p>Generic names like 'x', 'temp', 'data' provide no context and hurt readability.</p>" +
                "<p>Use descriptive names except in very small scopes (&lt;5 lines) or mathematical contexts.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "data = Import[\"file.csv\"];\n" +
                "result = ProcessData[data];  (* What kind of data? *)\n" +
                "temp = result[[1]];  (* Temp what? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "salesData = Import[\"sales.csv\"];\n" +
                "processedSales = ProcessSalesData[salesData];\n" +
                "firstQuarterSales = processedSales[[1]];\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability", "naming");

        repository.createRule(MISSING_USAGE_MESSAGE_KEY)
            .setName("Public functions should have usage messages")
            .setHtmlDescription(
                "<p>Public functions (starting with uppercase) should define usage messages for documentation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "ProcessUserData[data_, options___] := Module[...]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "ProcessUserData::usage = \"ProcessUserData[data, options] processes user data with specified options.\";\n" +
                "ProcessUserData[data_, options___] := Module[...]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation");

        repository.createRule(MISSING_OPTIONS_PATTERN_KEY)
            .setName("Functions with multiple optional parameters should use OptionsPattern")
            .setHtmlDescription(
                "<p>Functions with 3+ optional parameters should use OptionsPattern for better maintainability.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "PlotData[data_, color_: Blue, size_: 10, style_: Solid, width_: 2] := ...\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Options[PlotData] = {\"Color\" -> Blue, \"Size\" -> 10, \"Style\" -> Solid, \"Width\" -> 2};\n" +
                "PlotData[data_, opts: OptionsPattern[]] := Module[{color, size, style, width},\n" +
                "  color = OptionValue[\"Color\"];\n" +
                "  size = OptionValue[\"Size\"];\n" +
                "  ...\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("maintainability", "api-design");

        repository.createRule(SIDE_EFFECTS_NAMING_KEY)
            .setName("Functions with side effects should have descriptive names")
            .setHtmlDescription(
                "<p>Functions that modify global state should use naming conventions: SetXXX or ending with !.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Process[data_] := (globalCache = data; data)  (* Hidden side effect! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "SetCache[data_] := (globalCache = data; data)  (* Clear from name *)\n" +
                "(* Or use ! suffix like Mathematica built-ins *)\n" +
                "UpdateCache![data_] := (globalCache = data; data)\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", "side-effects");

        repository.createRule(COMPLEX_BOOLEAN_KEY)
            .setName("Complex boolean expressions should be simplified")
            .setHtmlDescription(
                "<p>Boolean expressions with >5 operators without clear grouping are hard to understand.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[a && b || c && d && !e || f && g, ...]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "condition1 = a && b;\n" +
                "condition2 = c && d && !e;\n" +
                "condition3 = f && g;\n" +
                "If[condition1 || condition2 || condition3, ...]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability", "complexity");

        repository.createRule(UNPROTECTED_SYMBOLS_KEY)
            .setName("Public API symbols should be protected")
            .setHtmlDescription(
                "<p>Public functions in packages should be Protected to prevent accidental redefinition by users.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* At end of package *)\n" +
                "Protect[PublicFunction1, PublicFunction2, PublicConstant];\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("api-design", "safety");

        repository.createRule(MISSING_RETURN_KEY)
            .setName("Complex functions should have explicit Return statements")
            .setHtmlDescription(
                "<p>Functions with multiple branches or complex logic should use explicit Return[] for clarity.</p>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "ProcessData[data_] := Module[{result},\n" +
                "  If[data === {}, Return[$Failed]];\n" +
                "  result = ComputeResult[data];\n" +
                "  If[!ValidQ[result], Return[Default]];\n" +
                "  Return[result]\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability");

        // SECURITY & SAFETY (3 rules)

        repository.createRule(UNSAFE_CLOUD_DEPLOY_KEY)
            .setName("CloudDeploy should specify Permissions")
            .setHtmlDescription(
                "<p>CloudDeploy without Permissions parameter creates public cloud objects accessible to anyone.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "CloudDeploy[form]  (* Public by default! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "CloudDeploy[form, Permissions -> \"Private\"]\n" +
                "CloudDeploy[form, Permissions -> {\"user@example.com\" -> \"Read\"}]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/276.html'>CWE-276</a> - Incorrect Default Permissions</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("security", "cloud", "permissions");

        repository.createRule(DYNAMIC_INJECTION_KEY)
            .setName("Dynamic content should not use ToExpression on user input")
            .setHtmlDescription(
                "<p>Using ToExpression or Symbol on user input in Dynamic creates code injection vulnerabilities.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Dynamic[ToExpression[userInput]]  (* User can execute arbitrary code! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Dynamic[SafeEvaluate[userInput]]  (* Use whitelist/sanitization *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("security", "injection");

        repository.createRule(IMPORT_WITHOUT_FORMAT_KEY)
            .setName("Import should specify format explicitly")
            .setHtmlDescription(
                "<p>Import without format specification guesses by file extension, which attackers can manipulate.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review to ensure format is validated.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Import[userFile]  (* Guesses format - could execute .mx! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Import[userFile, \"CSV\"]  (* Explicit format, safe *)\n" +
                "Import[userFile, \"JSON\"]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/434.html'>CWE-434</a> - Unrestricted Upload of File with Dangerous Type</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "file-upload");

        // RESOURCE MANAGEMENT (2 rules)

        repository.createRule(UNCLOSED_FILE_HANDLE_KEY)
            .setName("File handles should be closed")
            .setHtmlDescription(
                "<p>OpenRead, OpenWrite, OpenAppend create file handles that must be closed with Close[].</p>" +
                "<p>Unclosed file handles leak resources and can prevent file access.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "stream = OpenRead[\"file.txt\"];\n" +
                "data = Read[stream, String];\n" +
                "(* Missing Close[stream]! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "stream = OpenRead[\"file.txt\"];\n" +
                "data = Read[stream, String];\n" +
                "Close[stream];\n\n" +
                "(* Or use Import which handles cleanup automatically *)\n" +
                "data = Import[\"file.txt\", \"String\"];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/772.html'>CWE-772</a> - Missing Release of Resource</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "resource-leak");

        repository.createRule(GROWING_DEFINITION_CHAIN_KEY)
            .setName("Definitions should not grow unbounded")
            .setHtmlDescription(
                "<p>Repeatedly adding definitions (e.g., memoization in loop) without clearing causes memory leaks.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Memoization in loop - definitions grow forever! *)\n" +
                "Do[\n" +
                "  f[i] = ExpensiveComputation[i],\n" +
                "  {i, 1, 100000}\n" +
                "]  (* Creates 100k definitions, never cleared! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use temporary memoization *)\n" +
                "Block[{f},\n" +
                "  Do[f[i] = ExpensiveComputation[i], {i, 1, 100000}];\n" +
                "  (* Use f here *)\n" +
                "]  (* Definitions cleared when Block exits *)\n\n" +
                "(* Or use Association/Dictionary *)\n" +
                "cache = Association[];\n" +
                "Do[cache[i] = ExpensiveComputation[i], {i, 1, 100000}];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/401.html'>CWE-401</a> - Memory Leak</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "memory-leak");

        // ===== PHASE 4: 50 NEW RULES =====

        // Pattern Matching & Function Definition Rules (5 rules)

        repository.createRule(OVERCOMPLEX_PATTERNS_KEY)
            .setName("Pattern definitions should not be overly complex")
            .setHtmlDescription(
                "<p>Patterns with more than 5 alternatives or deeply nested conditions are difficult to understand and maintain.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nf[x_Integer | x_Real | x_Rational | x_String | x_Symbol | x_List] := x  (* 6 alternatives *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nf[x_?AtomQ] := x  (* Simplified using pattern test *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("complexity", "maintainability");

        repository.createRule(INCONSISTENT_RULE_TYPES_KEY)
            .setName("Rule and RuleDelayed should be used consistently")
            .setHtmlDescription(
                "<p>Mixing Rule (->) and RuleDelayed (:>) without clear justification makes code behavior unpredictable.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nrules = {a -> RandomReal[], b :> RandomReal[]}  (* Inconsistent *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nrules = {a :> RandomReal[], b :> RandomReal[]}  (* Both delayed *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("consistency");

        repository.createRule(MISSING_FUNCTION_ATTRIBUTES_KEY)
            .setName("Public functions should have appropriate attributes")
            .setHtmlDescription(
                "<p>Functions should use Listable, HoldAll, Protected, or other attributes where appropriate.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nMyFunction[x_] := x^2  (* Could be Listable *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nSetAttributes[MyFunction, Listable];\nMyFunction[x_] := x^2\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("best-practice");

        repository.createRule(MISSING_DOWNVALUES_DOC_KEY)
            .setName("Complex pattern-based functions should have documentation")
            .setHtmlDescription(
                "<p>Functions with multiple DownValues patterns should include usage messages.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nMyFunc::usage = \"MyFunc[x] computes...\";\nMyFunc[x_Integer] := x;\nMyFunc[x_Real] := x^2;\n</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation");

        repository.createRule(MISSING_PATTERN_TEST_VALIDATION_KEY)
            .setName("Functions should validate input types with pattern tests")
            .setHtmlDescription(
                "<p>Use ?NumericQ, ?ListQ, ?MatrixQ to validate inputs and prevent runtime errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nf[x_] := x^2  (* Accepts anything *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nf[x_?NumericQ] := x^2  (* Only numeric input *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "validation");

        // Code Clarity Rules (8 rules)

        repository.createRule(EXCESSIVE_PURE_FUNCTIONS_KEY)
            .setName("Complex pure functions should use named parameters")
            .setHtmlDescription(
                "<p>Pure functions with #, #2, #3 in complex expressions are hard to read.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nMap[#1^2 + #2*#3 - #1/#2 &, data]  (* Hard to read *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nMap[Function[{x, y, z}, x^2 + y*z - x/y], data]\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability");

        repository.createRule(MISSING_OPERATOR_PRECEDENCE_KEY)
            .setName("Complex operator expressions should use parentheses for clarity")
            .setHtmlDescription(
                "<p>Mixing /@, @@, //@, @@ without parentheses makes precedence unclear.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nf /@ g @@ data  (* Unclear precedence *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nf /@ (g @@ data)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability");

        repository.createRule(HARDCODED_FILE_PATHS_KEY)
            .setName("File paths should not be hardcoded")
            .setHtmlDescription(
                "<p>Absolute paths like C:\\ or /Users/ are not portable. Use FileNameJoin, $HomeDirectory.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\ndata = Import[\"/Users/john/data.csv\"];\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\ndata = Import[FileNameJoin[{$HomeDirectory, \"data.csv\"}]];\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("portability");

        repository.createRule(INCONSISTENT_RETURN_TYPES_KEY)
            .setName("Functions should return consistent types")
            .setHtmlDescription(
                "<p>A function that returns List in one case and Association in another is confusing.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nf[x_Integer] := {x};\nf[x_Real] := <|\"value\" -> x|>;  (* Inconsistent *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("consistency");

        repository.createRule(MISSING_ERROR_MESSAGES_KEY)
            .setName("Custom functions should define error messages")
            .setHtmlDescription(
                "<p>Use Message[...] definitions to provide helpful error feedback.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nMyFunc::badarg = \"Invalid argument: `1`\";\nMyFunc[x_] := Message[MyFunc::badarg, x] /; x < 0\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("usability");

        repository.createRule(GLOBAL_STATE_MODIFICATION_KEY)
            .setName("Functions modifying global state should be clearly named")
            .setHtmlDescription(
                "<p>Functions with side effects should end with ! to indicate state modification.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nUpdateCounter[n_] := (globalCount = n)  (* No ! suffix *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nUpdateCounter![n_] := (globalCount = n)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("conventions", "side-effects");

        repository.createRule(MISSING_LOCALIZATION_KEY)
            .setName("Dynamic interfaces should use LocalizeVariables")
            .setHtmlDescription(
                "<p>Manipulate and DynamicModule should localize variables properly.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nManipulate[Plot[Sin[a x], {x, 0, 2Pi}], {a, 1, 5}]  (* 'a' may leak *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("scoping");

        repository.createRule(EXPLICIT_GLOBAL_CONTEXT_KEY)
            .setName("Global` context should not be used explicitly")
            .setHtmlDescription(
                "<p>Using Global`symbol explicitly is a code smell indicating namespace confusion.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nGlobal`myVariable = 5;\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nmyVariable = 5;  (* Implicit global *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("conventions");

        // Data Structure Rules (5 rules)

        repository.createRule(MISSING_TEMPORARY_CLEANUP_KEY)
            .setName("Temporary files and directories should be cleaned up")
            .setHtmlDescription(
                "<p>CreateFile, CreateDirectory should use auto-deletion or manual cleanup.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\ntempFile = CreateFile[]  (* Auto-deleted at end of session *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("resource-management");

        repository.createRule(NESTED_LISTS_INSTEAD_ASSOCIATION_KEY)
            .setName("Use Association instead of nested indexed lists")
            .setHtmlDescription(
                "<p>Accessing data[[5]], data[[7]] repeatedly suggests Association would be clearer.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\ndata = {\"John\", 25, \"Engineer\", 50000};\nname = data[[1]]; salary = data[[4]];\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\ndata = <|\"name\" -> \"John\", \"age\" -> 25, \"salary\" -> 50000|>;\nname = data[\"name\"]; salary = data[\"salary\"];\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability", "maintainability");

        repository.createRule(REPEATED_PART_EXTRACTION_KEY)
            .setName("Repeated Part extractions should be destructured")
            .setHtmlDescription(
                "<p>Use destructuring to extract multiple parts at once.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nx = data[[1]]; y = data[[2]]; z = data[[3]];\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n{x, y, z} = data[[{1, 2, 3}]];\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("clarity");

        repository.createRule(MISSING_MEMOIZATION_KEY)
            .setName("Expensive pure computations should use memoization")
            .setHtmlDescription(
                "<p>Functions doing expensive calculations should cache results.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nf[x_] := f[x] = ExpensiveComputation[x]  (* Memoized *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(STRINGJOIN_FOR_TEMPLATES_KEY)
            .setName("Use StringTemplate instead of repeated StringJoin")
            .setHtmlDescription(
                "<p>Multiple <> operations are less readable than StringTemplate.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nmsg = \"User: \" <> name <> \", Age: \" <> ToString[age];\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nmsg = StringTemplate[\"User: `name`, Age: `age`\"][<|\"name\" -> name, \"age\" -> age|>];\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability");

        // Type & Value Error Rules (8 new bugs)

        repository.createRule(MISSING_EMPTY_LIST_CHECK_KEY)
            .setName("First, Last, and Part should check for empty lists")
            .setHtmlDescription(
                "<p>Using First, Last, Part on empty lists causes runtime errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nresult = First[data]  (* Crashes if data == {} *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nresult = If[Length[data] > 0, First[data], defaultValue]\n</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "crash");

        repository.createRule(MACHINE_PRECISION_IN_SYMBOLIC_KEY)
            .setName("Avoid machine precision floats in symbolic calculations")
            .setHtmlDescription(
                "<p>Using 1.5 instead of 3/2 in exact calculations loses precision.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nSolve[x^2 == 2.0, x]  (* Machine precision contaminates result *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nSolve[x^2 == 2, x]  (* Exact *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("precision", "correctness");

        repository.createRule(MISSING_FAILED_CHECK_KEY)
            .setName("Check for $Failed after Import, Get, URLFetch operations")
            .setHtmlDescription(
                "<p>Operations like Import can return $Failed on error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\ndata = Import[\"file.csv\"];\nresult = Mean[data]  (* Crashes if Import failed *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\ndata = Import[\"file.csv\"];\nIf[data === $Failed, Return[$Failed]];\nresult = Mean[data]\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "error-handling");

        repository.createRule(ZERO_DENOMINATOR_KEY)
            .setName("Division operations should guard against zero denominators")
            .setHtmlDescription(
                "<p>Symbolic division can produce ComplexInfinity if denominator is zero.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nresult = a / b  (* May produce ComplexInfinity *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nresult = If[b != 0, a / b, $Failed]\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        repository.createRule(MISSING_MATRIX_DIMENSION_CHECK_KEY)
            .setName("Matrix operations should validate compatible dimensions")
            .setHtmlDescription(
                "<p>Dot, Times on matrices with incompatible dimensions cause errors.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nIf[Dimensions[a][[2]] == Dimensions[b][[1]], a.b, $Failed]\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "linear-algebra");

        repository.createRule(INCORRECT_SET_IN_SCOPING_KEY)
            .setName("Use proper assignment inside Module and Block")
            .setHtmlDescription(
                "<p>Using = instead of := in scoping constructs can cause evaluation issues.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nModule[{x = RandomReal[]}, ...]  (* Evaluated at definition time *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nModule[{x}, x = RandomReal[]; ...]  (* Evaluated at runtime *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("scoping", "evaluation");

        repository.createRule(MISSING_HOLD_ATTRIBUTES_KEY)
            .setName("Functions delaying evaluation should use Hold attributes")
            .setHtmlDescription(
                "<p>Functions that manipulate unevaluated expressions need HoldAll, HoldFirst, etc.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nSetAttributes[MyIf, HoldRest];\nMyIf[test_, true_, false_] := If[test, true, false]\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("evaluation");

        repository.createRule(EVALUATION_ORDER_ASSUMPTION_KEY)
            .setName("Do not rely on implicit evaluation order")
            .setHtmlDescription(
                "<p>Mathematica evaluation order is not always left-to-right.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n{i++, i++, i++}  (* Order not guaranteed *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("undefined-behavior");

        // Data Handling Bug Rules (7 rules)

        repository.createRule(INCORRECT_LEVEL_SPECIFICATION_KEY)
            .setName("Map, Apply, Cases should use correct level specifications")
            .setHtmlDescription(
                "<p>Wrong level specifications cause silent failures or unexpected results.</p>" +
                "<h2>Example</h2>" +
                "<pre>\nMap[f, {{1, 2}, {3, 4}}]  (* Level 1 by default *)\nMap[f, {{1, 2}, {3, 4}}, {2}]  (* Level 2 *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("correctness");

        repository.createRule(UNPACKING_PACKED_ARRAYS_KEY)
            .setName("Avoid operations that unpack packed arrays")
            .setHtmlDescription(
                "<p>Operations like Append, Delete on packed arrays cause 10-100x slowdowns.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nDo[data = Append[data, i], {i, 1000}]  (* Unpacks repeatedly *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\ndata = Range[1000]  (* Stays packed *)\n</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("performance", "packed-arrays");

        repository.createRule(MISSING_SPECIAL_CASE_HANDLING_KEY)
            .setName("Handle special values: 0, Infinity, ComplexInfinity, Indeterminate")
            .setHtmlDescription(
                "<p>Functions should handle edge cases like division by zero, limits at infinity.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nf[x_] := f[x] = Which[\n  x === 0, 0,\n  x === Infinity, Infinity,\n  True, NormalComputation[x]\n]\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("edge-cases");

        repository.createRule(INCORRECT_ASSOCIATION_OPERATIONS_KEY)
            .setName("Association operations differ from List operations")
            .setHtmlDescription(
                "<p>Join on Associations merges by keys, not concatenates like Lists.</p>" +
                "<h2>Example</h2>" +
                "<pre>\nJoin[<|a->1|>, <|a->2|>]  (* Result: <|a->2|>, not <|a->1, a->2|> *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("correctness", "associations");

        repository.createRule(DATEOBJECT_VALIDATION_KEY)
            .setName("Validate DateObject inputs for invalid dates")
            .setHtmlDescription(
                "<p>DateObject can accept invalid dates without errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\ndate = DateObject[{2024, 2, 30}]  (* Invalid date *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("validation");

        repository.createRule(TOTAL_MEAN_ON_NON_NUMERIC_KEY)
            .setName("Total, Mean should only operate on numeric data")
            .setHtmlDescription(
                "<p>Statistical functions on mixed or symbolic data produce unexpected results.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nMean[{1, 2, \"x\"}]  (* Produces symbolic result *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("correctness", "statistics");

        repository.createRule(QUANTITY_UNIT_MISMATCH_KEY)
            .setName("Quantity operations should have compatible units")
            .setHtmlDescription(
                "<p>Adding Quantity[5, \"Meters\"] + Quantity[3, \"Seconds\"] produces errors.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nUnitConvert[Quantity[5, \"Meters\"] + Quantity[3, \"Centimeters\"], \"Meters\"]\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("units", "correctness");

        // Performance Rules (10 rules)

        repository.createRule(LINEAR_SEARCH_INSTEAD_LOOKUP_KEY)
            .setName("Use Association or Dispatch for lookups instead of Select")
            .setHtmlDescription(
                "<p>Select[list, #[[1]] == key &] is O(n), Association lookup is O(1).</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nFirst@Select[data, #[[\"id\"]] == targetId &]  (* O(n) *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nassoc = AssociationThread[data[[All, \"id\"]] -> data];\nassoc[targetId]  (* O(1) *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "algorithmic");

        repository.createRule(REPEATED_CALCULATIONS_KEY)
            .setName("Expensive expressions should not be recalculated in loops")
            .setHtmlDescription(
                "<p>Hoist invariant calculations out of loops.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nDo[result += data[[i]] * ExpensiveFunc[], {i, n}]  (* Recalculates each time *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nconst = ExpensiveFunc[];\nDo[result += data[[i]] * const, {i, n}]\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(POSITION_INSTEAD_PATTERN_KEY)
            .setName("Use pattern matching instead of Position when possible")
            .setHtmlDescription(
                "<p>Cases is often faster and clearer than Position + Part extraction.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\npositions = Position[data, _?EvenQ];\nresults = Extract[data, positions]\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nresults = Cases[data, _?EvenQ]\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "idiomatic");

        repository.createRule(FLATTEN_TABLE_ANTIPATTERN_KEY)
            .setName("Avoid Flatten[Table[...]] pattern")
            .setHtmlDescription(
                "<p>Use Catenate, Join, or vectorization instead of Flatten@Table.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nFlatten[Table[f[i], {i, n}]]  (* Creates intermediate list *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nCatenate[Table[f[i], {i, n}]]  (* More efficient *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(MISSING_PARALLELIZATION_KEY)
            .setName("Large independent iterations should use parallelization")
            .setHtmlDescription(
                "<p>Use ParallelTable, ParallelMap for CPU-bound independent tasks.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nParallelTable[ExpensiveFunc[i], {i, 10000}]  (* Uses all cores *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "parallelization");

        repository.createRule(MISSING_SPARSE_ARRAY_KEY)
            .setName("Use SparseArray for arrays with >80% zeros")
            .setHtmlDescription(
                "<p>Dense arrays waste memory and computation on zeros.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nmatrix = SparseArray[{{1,1}->5, {100,100}->3}, {100, 100}]  (* Efficient *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "memory");

        repository.createRule(UNNECESSARY_TRANSPOSE_KEY)
            .setName("Avoid repeatedly transposing data")
            .setHtmlDescription(
                "<p>Work column-wise or row-wise consistently instead of transposing back and forth.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nresult = Transpose[Map[f, Transpose[data]]]  (* Transpose twice *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(DELETEDUPS_ON_LARGE_DATA_KEY)
            .setName("DeleteDuplicates on large lists should use alternative methods")
            .setHtmlDescription(
                "<p>For lists >10,000 elements, use Association or Dataset for faster deduplication.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nKeys@GroupBy[largeList, Identity]  (* Faster than DeleteDuplicates *)\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(REPEATED_STRING_PARSING_KEY)
            .setName("Parsing the same string multiple times should be avoided")
            .setHtmlDescription(
                "<p>Cache parsed results instead of re-parsing.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nDo[result += ToExpression[str], {i, 1000}]  (* Parses 1000 times *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nexpr = ToExpression[str];\nDo[result += expr, {i, 1000}]\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(MISSING_COMPILATION_TARGET_KEY)
            .setName("Numerical code should use CompilationTarget->C")
            .setHtmlDescription(
                "<p>Compiled Mathematica code runs 10-100x faster with C compilation.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nCompile[{{x, _Real}}, x^2 + Sin[x], CompilationTarget -> \"C\"]\n</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "compilation");

        // Security Vulnerability Rules (7 rules)

        repository.createRule(TOEXPRESSION_ON_INPUT_KEY)
            .setName("ToExpression on external input enables code injection")
            .setHtmlDescription(
                "<p>ToExpression[userInput] allows arbitrary code execution.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nresult = ToExpression[input]  (* CRITICAL VULNERABILITY *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nresult = Interpreter[\"Number\"][input]  (* Safe parsing *)\n</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "injection", "owasp-a03");

        repository.createRule(UNSANITIZED_RUNPROCESS_KEY)
            .setName("RunProcess with user input enables command injection")
            .setHtmlDescription(
                "<p>Shell command injection via RunProcess or Run with unsanitized input.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nRunProcess[{\"bash\", \"-c\", userInput}]  (* Command injection *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n(* Validate and sanitize input first *)\n</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/78.html'>CWE-78</a> - OS Command Injection</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "injection", "owasp-a03");

        repository.createRule(MISSING_CLOUD_AUTH_KEY)
            .setName("Cloud functions should have authentication and authorization")
            .setHtmlDescription(
                "<p>APIFunction and FormFunction without Permissions checks are publicly accessible.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nCloudDeploy[APIFunction[{}, DoSomething[]]]  (* No auth *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nCloudDeploy[APIFunction[{}, DoSomething[], Permissions -> \"Private\"]]\n</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/306.html'>CWE-306</a> - Missing Authentication</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "authentication", "owasp-a01");

        repository.createRule(HARDCODED_API_KEYS_KEY)
            .setName("API keys and tokens should not be hardcoded")
            .setHtmlDescription(
                "<p>Hardcoded credentials in CloudDeploy, ServiceConnect expose secrets.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nServiceConnect[\"OpenAI\", \"APIKey\" -> \"sk-abc123...\"]  (* Exposed *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nServiceConnect[\"OpenAI\", \"APIKey\" -> SystemCredential[\"OpenAIKey\"]]\n</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/798.html'>CWE-798</a> - Hard-coded Credentials</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "credentials", "owasp-a07");

        repository.createRule(NEEDS_GET_UNTRUSTED_KEY)
            .setName("Needs and Get should not load code from untrusted paths")
            .setHtmlDescription(
                "<p>Loading packages from user-controlled paths enables code execution.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nGet[userProvidedPath]  (* Code injection *)\n</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/829.html'>CWE-829</a> - Untrusted Control Sphere</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "code-injection");

        repository.createRule(EXPOSING_SENSITIVE_DATA_KEY)
            .setName("Cloud functions should not expose sensitive system information")
            .setHtmlDescription(
                "<p>Deploying functions that return $UserName, $MachineName, credentials leaks information.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nCloudDeploy[APIFunction[{}, $UserName]]  (* Leaks username *)\n</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/200.html'>CWE-200</a> - Information Exposure</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "information-disclosure");

        repository.createRule(MISSING_FORMFUNCTION_VALIDATION_KEY)
            .setName("FormFunction inputs should be validated and sanitized")
            .setHtmlDescription(
                "<p>Cloud forms accepting arbitrary input without validation enable attacks.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nFormFunction[{\"age\" -> Restricted[\"Integer\", {1, 120}]}, DoSomething]\n</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/20.html'>CWE-20</a> - Improper Input Validation</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "validation", "owasp-a03");

        // ===== CHUNK 1 RULES (Items 16-50 from ROADMAP_325.md) =====

        // Pattern System Rules (Items 16-30)
        repository.createRule(UNRESTRICTED_BLANK_PATTERN_KEY)
            .setName("Blank patterns should have type restrictions when appropriate")
            .setHtmlDescription(
                "<p>Unrestricted blank patterns like <code>f[x_] := ...</code> accept any type, potentially causing runtime errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_] := x^2  (* Fails on non-numeric input *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_?NumericQ] := x^2  (* Type-safe *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("patterns", "type-safety");

        repository.createRule(PATTERN_TEST_VS_CONDITION_KEY)
            .setName("PatternTest (?) is more efficient than Condition (/;) for simple tests")
            .setHtmlDescription(
                "<p>PatternTest (<code>?</code>) evaluates during pattern matching, while Condition (<code>/;</code>) evaluates after.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_ /; IntegerQ[x]] := x  (* Inefficient *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_?IntegerQ] := x  (* More efficient *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns", "performance");

        repository.createRule(BLANKSEQUENCE_WITHOUT_RESTRICTION_KEY)
            .setName("BlankSequence should have type restrictions when possible")
            .setHtmlDescription(
                "<p>Unrestricted <code>x__</code> patterns can match inappropriate sequences.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>sum[x__] := Total[{x}]  (* No type check *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>sum[x__?NumericQ] := Total[{x}]  (* Type-safe *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("patterns", "performance");

        repository.createRule(NESTED_OPTIONAL_PATTERNS_KEY)
            .setName("Optional pattern defaults should not depend on other parameters")
            .setHtmlDescription(
                "<p>Patterns like <code>f[x_:1, y_:x]</code> have evaluation order issues.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_:1, y_:x] := x + y  (* y default depends on x *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_:1, y_:1] := x + y  (* Independent defaults *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns", "evaluation-order");

        repository.createRule(PATTERN_NAMING_CONFLICTS_KEY)
            .setName("Pattern names should not have conflicting type restrictions")
            .setHtmlDescription(
                "<p>Using the same pattern name with different types creates impossible-to-match patterns.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_Integer, x_Real] := x  (* Impossible to match *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_Integer, y_Real] := x + y  (* Use different names *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns");

        repository.createRule(REPEATED_PATTERN_ALTERNATIVES_KEY)
            .setName("Pattern alternatives should use correct syntax")
            .setHtmlDescription(
                "<p>Redundant pattern names in alternatives should be refactored.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_Integer | x_Real] := x  (* Redundant *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x:(_Integer | _Real)] := x  (* Correct syntax *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("patterns", "clarity");

        repository.createRule(PATTERN_TEST_PURE_FUNCTION_KEY)
            .setName("Avoid pure functions in PatternTest for hot code")
            .setHtmlDescription(
                "<p>Pure functions in patterns create closures on each match attempt.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_?(# > 0 &)] := x  (* Creates closure each time *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_?Positive] := x  (* Built-in predicate *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("patterns", "performance");

        repository.createRule(MISSING_PATTERN_DEFAULTS_KEY)
            .setName("Optional arguments should have sensible defaults")
            .setHtmlDescription(
                "<p>Optional parameters without validation can cause issues.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_, opts___] := DoSomething[x, opts]  (* No validation *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_, opts:OptionsPattern[]] := DoSomething[x, opts]</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns", "validation");

        repository.createRule(ORDER_DEPENDENT_PATTERNS_KEY)
            .setName("Specific patterns should be defined before general ones")
            .setHtmlDescription(
                "<p>Pattern matching tries definitions in order, so specific patterns after general ones never match.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\nf[x_] := x^2;\nf[0] := 0;  (* Never matches - f[x_] already handles f[0] *)\n</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\nf[0] := 0;  (* Specific first *)\nf[x_] := x^2;  (* General second *)\n</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns", "unreachable-code");

        repository.createRule(VERBATIM_PATTERN_MISUSE_KEY)
            .setName("Verbatim should only be used when necessary")
            .setHtmlDescription(
                "<p>Verbatim has tricky semantics and is often misused.</p>" +
                "<h2>Example</h2>" +
                "<pre>Cases[{1, 2, x, y}, Verbatim[x]]  (* Only matches literal x *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns");

        repository.createRule(HOLDPATTERN_UNNECESSARY_KEY)
            .setName("HoldPattern should be removed when not needed")
            .setHtmlDescription(
                "<p>Unnecessary HoldPattern adds clutter without benefit.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Cases[list, HoldPattern[_Integer]]  (* Unnecessary *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Cases[list, _Integer]  (* HoldPattern not needed *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("patterns", "clutter");

        repository.createRule(LONGEST_SHORTEST_WITHOUT_ORDERING_KEY)
            .setName("Longest and Shortest require proper context")
            .setHtmlDescription(
                "<p>Longest and Shortest modifiers may not work as expected without proper alternatives.</p>" +
                "<h2>Example</h2>" +
                "<pre>StringCases[str, Longest[__]]  (* Needs alternatives to be useful *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns");

        repository.createRule(PATTERN_REPEATED_DIFFERENT_TYPES_KEY)
            .setName("Use conditions instead of repeated pattern names for equality checks")
            .setHtmlDescription(
                "<p>Pattern <code>f[{x_, x_}]</code> matches lists with same symbolic name, not same value.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[{x_, x_}] := x  (* Doesn't check equality *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[{x_, y_} /; x == y] := x  (* Checks equality *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns");

        repository.createRule(ALTERNATIVES_TOO_COMPLEX_KEY)
            .setName("Pattern alternatives with many options cause backtracking")
            .setHtmlDescription(
                "<p>Alternatives with 10+ options can cause exponential backtracking.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x:(a|b|c|d|e|f|g|h|i|j|k|l) := ...  (* Slow matching *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x_ /; MemberQ[{a,b,c,d,e,f,g,h,i,j,k,l}, x] := ...</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("patterns", "performance");

        repository.createRule(PATTERN_MATCHING_LARGE_LISTS_KEY)
            .setName("Avoid pattern matching on large lists")
            .setHtmlDescription(
                "<p>Pattern matching lists with thousands of elements is inefficient.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Cases[Range[10000], x_ /; x > 100]  (* Slow *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Select[Range[10000], # > 100 &]  (* Much faster *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("patterns", "performance");

        // List/Array Rules (Items 31-40)
        repository.createRule(EMPTY_LIST_INDEXING_KEY)
            .setName("Check list length before indexing")
            .setHtmlDescription(
                "<p>Indexing empty lists causes runtime errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>result = list[[1]]  (* Error if list is {} *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>If[Length[list] > 0, result = list[[1]], result = Missing[]]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("lists", "bounds-check");

        repository.createRule(NEGATIVE_INDEX_WITHOUT_VALIDATION_KEY)
            .setName("Validate negative indices against list length")
            .setHtmlDescription(
                "<p>Negative index <code>list[[-n]]</code> fails if n > Length[list].</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>last = list[[-n]]  (* Error if n too large *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>If[n <= Length[list], last = list[[-n]], ...]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("lists", "bounds-check");

        repository.createRule(PART_ASSIGNMENT_TO_IMMUTABLE_KEY)
            .setName("Part assignment requires a variable")
            .setHtmlDescription(
                "<p>Assigning to <code>expr[[i]]</code> where expr is not a variable doesn't modify anything.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>{1,2,3}[[1]] = 5  (* Doesn't modify anything *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>list = {1,2,3}; list[[1]] = 5  (* Modifies list *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("lists", "mutation");

        repository.createRule(INEFFICIENT_LIST_CONCATENATION_KEY)
            .setName("Avoid repeated Join operations in loops")
            .setHtmlDescription(
                "<p>Using <code>Join[list, {x}]</code> in a loop has O(n²) complexity.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Do[result = Join[result, {i}], {i, 1000}]  (* Quadratic *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>result = Table[i, {i, 1000}]  (* Linear *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "performance");

        repository.createRule(UNNECESSARY_FLATTEN_KEY)
            .setName("Don't flatten already-flat lists")
            .setHtmlDescription(
                "<p>Flatten on flat lists wastes computation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Flatten[{a, b, c}]  (* Already flat *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>{a, b, c}  (* No Flatten needed *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "performance");

        repository.createRule(LENGTH_IN_LOOP_CONDITION_KEY)
            .setName("Cache list length outside loops")
            .setHtmlDescription(
                "<p>Recalculating Length in loop conditions is wasteful.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Do[..., {i, 1, Length[list]}]  (* Recalculates each iteration *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>n = Length[list]; Do[..., {i, 1, n}]</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "performance");

        repository.createRule(REVERSE_TWICE_KEY)
            .setName("Double Reverse is a no-op")
            .setHtmlDescription(
                "<p>Reversing a list twice returns the original.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Reverse[Reverse[list]]  (* No-op *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>list  (* Remove double Reverse *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "redundant");

        repository.createRule(SORT_WITHOUT_COMPARISON_KEY)
            .setName("Use Reverse[Sort[list]] instead of Sort with Greater")
            .setHtmlDescription(
                "<p>Built-in Sort is optimized; custom comparisons are slower.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Sort[list, Greater]  (* Slower *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Reverse[Sort[list]]  (* Faster *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "performance");

        repository.createRule(POSITION_VS_SELECT_KEY)
            .setName("Use Select instead of Extract with Position")
            .setHtmlDescription(
                "<p>Combining Extract and Position is inefficient and unclear.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Extract[list, Position[list, _?EvenQ]]  (* Complex *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Select[list, EvenQ]  (* Clearer and faster *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "clarity");

        repository.createRule(NESTED_PART_EXTRACTION_KEY)
            .setName("Use multi-dimensional Part syntax")
            .setHtmlDescription(
                "<p>Nested Part extractions should use direct syntax.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>list[[i]][[j]]  (* Nested *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>list[[i, j]]  (* Cleaner *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("lists", "clarity");

        // Association Rules (Items 41-50)
        repository.createRule(MISSING_KEY_CHECK_KEY)
            .setName("Check if association key exists before accessing")
            .setHtmlDescription(
                "<p>Accessing non-existent keys returns Missing[\"KeyAbsent\", key].</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>value = assoc[\"key\"]  (* May return Missing *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>If[KeyExistsQ[assoc, \"key\"], value = assoc[\"key\"], ...]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "validation");

        repository.createRule(ASSOCIATION_VS_LIST_CONFUSION_KEY)
            .setName("Don't use list operations on associations")
            .setHtmlDescription(
                "<p>Some list operations don't work correctly on associations.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>assoc[[1]]  (* Wrong - associations aren't positional *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>First[Values[assoc]]  (* Correct *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations");

        repository.createRule(INEFFICIENT_KEY_LOOKUP_KEY)
            .setName("Use KeySelect instead of Select on Keys")
            .setHtmlDescription(
                "<p>KeySelect is optimized for association key filtering.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Select[Keys[assoc], StringQ]  (* Inefficient *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>KeySelect[assoc, StringQ]  (* Faster *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "performance");

        repository.createRule(QUERY_ON_NON_DATASET_KEY)
            .setName("Query requires Dataset wrapper")
            .setHtmlDescription(
                "<p>Query syntax only works on Dataset objects.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Query[All, \"name\"][list]  (* Error on plain list *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Query[All, \"name\"][Dataset[list]]  (* Correct *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "datasets");

        repository.createRule(ASSOCIATION_UPDATE_PATTERN_KEY)
            .setName("Use AssociateTo or Append for association updates")
            .setHtmlDescription(
                "<p>Direct assignment syntax <code>assoc[\"key\"] = value</code> creates confusion.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>assoc[\"key\"] = value  (* Ambiguous *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>AssociateTo[assoc, \"key\" -> value]  (* Clear intent *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "clarity");

        repository.createRule(MERGE_WITHOUT_CONFLICT_STRATEGY_KEY)
            .setName("Specify merge function for Merge")
            .setHtmlDescription(
                "<p>Merge without a combining function uses List by default, which may not be desired.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Merge[{a1, a2}]  (* Uses List by default *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Merge[{a1, a2}, Total]  (* Explicit strategy *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "clarity");

        repository.createRule(ASSOCIATETO_ON_NON_SYMBOL_KEY)
            .setName("AssociateTo requires a symbol")
            .setHtmlDescription(
                "<p>AssociateTo modifies in place, so the first argument must be a symbol.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>AssociateTo[<|\"a\"->1|>, \"b\"->2]  (* Doesn't modify anything *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>assoc = <|\"a\"->1|>; AssociateTo[assoc, \"b\"->2]  (* Modifies assoc *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("associations", "mutation");

        repository.createRule(KEYDROP_MULTIPLE_TIMES_KEY)
            .setName("Drop multiple keys in one call")
            .setHtmlDescription(
                "<p>Chained KeyDrop is less efficient than a single call.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>KeyDrop[KeyDrop[assoc, \"a\"], \"b\"]  (* Two passes *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>KeyDrop[assoc, {\"a\", \"b\"}]  (* Single pass *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "performance");

        repository.createRule(LOOKUP_WITH_MISSING_DEFAULT_KEY)
            .setName("Don't specify Missing as Lookup default")
            .setHtmlDescription(
                "<p>Missing is already the default return value for Lookup.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Lookup[assoc, key, Missing[]]  (* Redundant *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Lookup[assoc, key]  (* Same behavior *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "redundant");

        repository.createRule(GROUPBY_WITHOUT_AGGREGATION_KEY)
            .setName("Use GatherBy when not aggregating")
            .setHtmlDescription(
                "<p>GroupBy creates associations; GatherBy creates lists and may be clearer without aggregation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>GroupBy[data, First]  (* No aggregation *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>GatherBy[data, First]  (* Clearer intent *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("associations", "clarity");

        // ===== CHUNK 2 RULE DEFINITIONS (Items 61-100 from ROADMAP_325.md) =====

        // Unused Code Detection Rules (Items 61-75)

        repository.createRule(UNUSED_PRIVATE_FUNCTION_KEY)
            .setName("Unused private functions should be removed")
            .setHtmlDescription(
                "<p>Private functions that are never called are dead code and should be removed.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>privateHelper[x_] := x^2  (* Never called *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Remove unused function *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "dead-code");

        repository.createRule(UNUSED_FUNCTION_PARAMETER_KEY)
            .setName("Unused function parameters should be removed or prefixed with underscore")
            .setHtmlDescription(
                "<p>Function parameters that are never used in the body may indicate a logic error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>compute[x_, y_] := x^2  (* y is unused *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>compute[x_, _] := x^2  (* Use blank for unused parameter *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "parameters");

        repository.createRule(UNUSED_MODULE_VARIABLE_KEY)
            .setName("Unused Module variables should be removed")
            .setHtmlDescription(
                "<p>Variables declared in Module but never used are clutter.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Module[{x, y}, x = 5; x^2]  (* y is unused *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Module[{x}, x = 5; x^2]</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "scoping");

        repository.createRule(UNUSED_WITH_VARIABLE_KEY)
            .setName("Unused With variables should be removed")
            .setHtmlDescription(
                "<p>Variables declared in With but never used indicate unclear intent.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>With[{a = 1, b = 2}, a^2]  (* b is unused *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>With[{a = 1}, a^2]</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "scoping");

        repository.createRule(UNUSED_IMPORT_KEY)
            .setName("Unused package imports should be removed")
            .setHtmlDescription(
                "<p>Importing packages that are never used adds load time and unnecessary dependencies.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Needs[\"MyPackage`\"]  (* No symbols from MyPackage used *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Remove unused Needs *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "imports");

        repository.createRule(UNUSED_PATTERN_NAME_KEY)
            .setName("Unused pattern names should use blank patterns")
            .setHtmlDescription(
                "<p>Named patterns that are never referenced should use unnamed blanks.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_, y_] := x  (* y is named but unused *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_, _] := x  (* Use blank for unused pattern *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "patterns");

        repository.createRule(UNUSED_OPTIONAL_PARAMETER_KEY)
            .setName("Unused optional parameters should be removed")
            .setHtmlDescription(
                "<p>Optional parameters that are never used even when provided create confusing APIs.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_, opts___] := x  (* opts never used *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_] := x  (* Remove unused optional *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "parameters");

        repository.createRule(DEAD_AFTER_RETURN_KEY)
            .setName("Code after Return statement is unreachable")
            .setHtmlDescription(
                "<p>Code after a Return statement in the same scope will never execute.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_] := (Return[x]; Print[\"Never executes\"])</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_] := Return[x]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("dead-code", "control-flow");

        repository.createRule(UNREACHABLE_AFTER_ABORT_THROW_KEY)
            .setName("Code after Abort or Throw is unreachable")
            .setHtmlDescription(
                "<p>Code after Abort[] or Throw[] will never execute.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>If[error, Abort[]]; processData[]  (* Never executes if error *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>If[!error, processData[]]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("dead-code", "control-flow");

        repository.createRule(ASSIGNMENT_NEVER_READ_KEY)
            .setName("Assignment value is never read")
            .setHtmlDescription(
                "<p>Assigning a value that is never read before being overwritten is useless work.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x = 1; x = 2; Print[x]  (* First assignment wasted *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 2; Print[x]</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "dead-code");

        repository.createRule(FUNCTION_DEFINED_NEVER_CALLED_KEY)
            .setName("Global function defined but never called")
            .setHtmlDescription(
                "<p>Global-scope functions that are never called may be dead code or part of a public API.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>utilityFunction[x_] := x^2  (* Never called anywhere *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Remove or document as public API *)</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "dead-code");

        repository.createRule(REDEFINED_WITHOUT_USE_KEY)
            .setName("Variable redefined without using previous value")
            .setHtmlDescription(
                "<p>Redefining a variable without using its previous value indicates a logic error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>result = Solve[eq1]; result = Solve[eq2]  (* First solve wasted *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>result = Solve[eq2]  (* Remove first assignment *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("logic-error", "dead-code");

        repository.createRule(LOOP_VARIABLE_UNUSED_KEY)
            .setName("Loop iterator variable is never used in body")
            .setHtmlDescription(
                "<p>When the loop iterator is never used, use the simpler form without iterator.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Do[Print[\"Hello\"], {i, 1, 10}]  (* i is unused *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Do[Print[\"Hello\"], 10]  (* Simpler form *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "loops");

        repository.createRule(CATCH_WITHOUT_THROW_KEY)
            .setName("Catch statement without corresponding Throw")
            .setHtmlDescription(
                "<p>A Catch without any Throw in its body is unnecessary overhead.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Catch[result = compute[x]]  (* No Throw in compute *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>result = compute[x]  (* Remove unnecessary Catch *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "error-handling");

        repository.createRule(CONDITION_ALWAYS_FALSE_KEY)
            .setName("Condition is always false")
            .setHtmlDescription(
                "<p>Conditions that are always false indicate dead code or logic errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>If[False, doSomething[]]  (* Never executes *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Remove dead branch or fix condition *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("dead-code", "logic-error");

        // Shadowing & Naming Rules (Items 76-90)

        repository.createRule(LOCAL_SHADOWS_GLOBAL_KEY)
            .setName("Local variable shadows global variable")
            .setHtmlDescription(
                "<p>Local variables shadowing global variables can be confusing and may be unintended.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>data = {1,2,3};\nModule[{data}, data = {4,5,6}]  (* Shadows global *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Module[{localData}, localData = {4,5,6}]</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("shadowing", "naming");

        repository.createRule(PARAMETER_SHADOWS_BUILTIN_KEY)
            .setName("Parameter shadows built-in function")
            .setHtmlDescription(
                "<p>Parameters that shadow built-in functions will prevent their use and cause confusion.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[List_] := Length[List]  (* Shadows built-in List *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[list_] := Length[list]  (* Use lowercase *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("shadowing", "built-ins");

        repository.createRule(LOCAL_SHADOWS_PARAMETER_KEY)
            .setName("Local variable shadows function parameter")
            .setHtmlDescription(
                "<p>Local variables shadowing parameters is confusing and probably an error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_] := Module[{x}, x = 5; x^2]  (* Shadows parameter *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_] := Module[{y}, y = 5; y^2]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("shadowing", "scoping");

        repository.createRule(MULTIPLE_DEFINITIONS_SAME_SYMBOL_KEY)
            .setName("Symbol defined multiple times")
            .setHtmlDescription(
                "<p>Redefining the same symbol multiple times may be intentional (patterns) or an error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_] := x^2;\nf[x_] := x^3  (* Overwrites previous definition *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_Integer] := x^2;\nf[x_Real] := x^3  (* Pattern-based overloading *)</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("redefinition", "patterns");

        repository.createRule(SYMBOL_NAME_TOO_SHORT_KEY)
            .setName("Symbol name is too short in large function")
            .setHtmlDescription(
                "<p>Single-letter variable names in large functions reduce readability.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>largeFunction[x_] := Module[{a,b,c,d,e}, ...]  (* Many single letters *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>largeFunction[input_] := Module[{result, temp, index}, ...]</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", "readability");

        repository.createRule(SYMBOL_NAME_TOO_LONG_KEY)
            .setName("Symbol name exceeds 50 characters")
            .setHtmlDescription(
                "<p>Very long variable names (>50 characters) reduce readability.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>thisIsAReallyLongVariableNameThatExceedsFiftyCharactersAndIsHardToRead = 5</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>computationResult = 5  (* Concise but descriptive *)</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", "readability");

        repository.createRule(INCONSISTENT_NAMING_CONVENTION_KEY)
            .setName("Inconsistent naming convention (mix of camelCase, snake_case, PascalCase)")
            .setHtmlDescription(
                "<p>Mixing naming conventions reduces code consistency.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>myVariable = 1;\nmy_other_variable = 2;\nMyThirdVariable = 3</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>myVariable = 1;\nmyOtherVariable = 2;\nmyThirdVariable = 3  (* Consistent camelCase *)</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", "consistency");

        repository.createRule(BUILTIN_NAME_IN_LOCAL_SCOPE_KEY)
            .setName("Built-in function name used in local scope")
            .setHtmlDescription(
                "<p>Using built-in names as local variables is confusing and prevents using those built-ins.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Module[{Map, Apply}, ...]  (* Shadows built-ins *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Module[{mapper, applier}, ...]</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("shadowing", "built-ins");

        repository.createRule(CONTEXT_CONFLICTS_KEY)
            .setName("Symbol defined in multiple contexts")
            .setHtmlDescription(
                "<p>Symbols defined in multiple contexts cause ambiguity and confusion.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>PackageA`mySymbol = 1;\nPackageB`mySymbol = 2  (* Ambiguous *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Use unique names or proper context management *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("contexts", "ambiguity");

        repository.createRule(RESERVED_NAME_USAGE_KEY)
            .setName("Reserved system variable name used")
            .setHtmlDescription(
                "<p>Using reserved names like $SystemID, $Version as variable names can cause issues.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>$SystemID = \"custom\"  (* Overwrites system variable *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>systemID = \"custom\"  (* Use non-reserved name *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reserved", "system-variables");

        repository.createRule(PRIVATE_CONTEXT_SYMBOL_PUBLIC_KEY)
            .setName("Private context symbol used from outside package")
            .setHtmlDescription(
                "<p>Symbols in Private` context should not be used from outside the package.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>MyPackage`Private`helperFunction[x]  (* Breaks encapsulation *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Export function or use public API *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("encapsulation", "packages");

        repository.createRule(MISMATCHED_BEGIN_END_KEY)
            .setName("Mismatched BeginPackage/EndPackage or Begin/End")
            .setHtmlDescription(
                "<p>Mismatched package/context delimiters corrupt the context system.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>BeginPackage[\"MyPackage`\"]\n(* Missing EndPackage *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>BeginPackage[\"MyPackage`\"]\n...\nEndPackage[]</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("packages", "contexts");

        repository.createRule(SYMBOL_AFTER_ENDPACKAGE_KEY)
            .setName("Symbol defined after EndPackage")
            .setHtmlDescription(
                "<p>Symbols defined after EndPackage[] are in the wrong context.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>BeginPackage[\"MyPackage`\"]\n...\nEndPackage[]\nf[x_] := x  (* Wrong context *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>BeginPackage[\"MyPackage`\"]\nf[x_] := x\nEndPackage[]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("packages", "contexts");

        repository.createRule(GLOBAL_IN_PACKAGE_KEY)
            .setName("Global context used in package code")
            .setHtmlDescription(
                "<p>Package code should use the package context, not Global`.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Global`temp = 5  (* In package code *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>temp = 5  (* Uses package context *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("packages", "contexts");

        repository.createRule(TEMP_VARIABLE_NOT_TEMP_KEY)
            .setName("Variables named 'temp' or 'tmp' used multiple times")
            .setHtmlDescription(
                "<p>Variables named 'temp' or 'tmp' that persist should have better names.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>temp = compute1[];\nresult1 = process[temp];\ntemp = compute2[]  (* Reused multiple times *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>intermediateResult1 = compute1[];\nresult1 = process[intermediateResult1]</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", "readability");

        // Undefined Symbol Detection Rules (Items 91-100)

        repository.createRule(UNDEFINED_FUNCTION_CALL_KEY)
            .setName("Call to undefined function")
            .setHtmlDescription(
                "<p>Calling a function that is not defined or imported will cause a runtime error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>result = undefinedFunction[x]  (* Function not defined *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Define function or import package *)\nundefinedFunction[x_] := x^2</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("undefined", "runtime-error");

        repository.createRule(UNDEFINED_VARIABLE_REFERENCE_KEY)
            .setName("Reference to undefined variable")
            .setHtmlDescription(
                "<p>Using a variable before it is defined will return the symbol itself or cause an error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>result = x + 1  (* x never defined *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 5;\nresult = x + 1</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("undefined", "runtime-error");

        repository.createRule(TYPO_IN_BUILTIN_NAME_KEY)
            .setName("Possible typo in built-in function name")
            .setHtmlDescription(
                "<p>Common typos in built-in names like 'Lenght' instead of 'Length'.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>size = Lenght[list]  (* Typo: should be Length *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>size = Length[list]</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("typo", "built-ins");

        repository.createRule(WRONG_CAPITALIZATION_KEY)
            .setName("Wrong capitalization of built-in function")
            .setHtmlDescription(
                "<p>Mathematica is case-sensitive; 'length' is not the same as 'Length'.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>size = length[list]  (* Should be Length with capital L *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>size = Length[list]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("capitalization", "built-ins");

        repository.createRule(MISSING_IMPORT_KEY)
            .setName("Missing package import for external symbol")
            .setHtmlDescription(
                "<p>Using package symbols without Needs[] may work in notebook but fail in scripts.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>result = ExternalPackage`Function[x]  (* Package not imported *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Needs[\"ExternalPackage`\"]\nresult = ExternalPackage`Function[x]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("imports", "packages");

        repository.createRule(CONTEXT_NOT_FOUND_KEY)
            .setName("Needs references non-existent context")
            .setHtmlDescription(
                "<p>Attempting to load a package that doesn't exist causes a runtime error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Needs[\"NonExistentPackage`\"]  (* Package doesn't exist *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Use correct package name or create package *)</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("imports", "runtime-error");

        repository.createRule(SYMBOL_MASKED_BY_IMPORT_KEY)
            .setName("Local symbol masked by package import")
            .setHtmlDescription(
                "<p>Importing a package can silently override local symbols with same name.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>myFunction[x_] := x^2;\nNeeds[\"Package`\"]  (* Package also defines myFunction *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Rename local symbol or manage contexts carefully *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("shadowing", "imports");

        repository.createRule(MISSING_PATH_ENTRY_KEY)
            .setName("Get references file not in $Path")
            .setHtmlDescription(
                "<p>Loading a file with Get[] that's not in $Path will cause a runtime error.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Get[\"myfile.m\"]  (* File not in $Path *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Get[FileNameJoin[{Directory[], \"myfile.m\"}]]  (* Use absolute path *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("imports", "file-system");

        repository.createRule(CIRCULAR_NEEDS_KEY)
            .setName("Circular package dependency detected")
            .setHtmlDescription(
                "<p>Package A needs Package B which needs Package A causes load errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>(* In PackageA.m *)\nNeeds[\"PackageB`\"]\n(* In PackageB.m *)\nNeeds[\"PackageA`\"]  (* Circular *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Refactor to break circular dependency *)</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("circular-dependency", "packages");

        repository.createRule(FORWARD_REFERENCE_WITHOUT_DECLARATION_KEY)
            .setName("Forward reference without explicit declaration")
            .setHtmlDescription(
                "<p>Using a symbol before defining it may fail in a fresh kernel without forward declaration.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>g[x_] := f[x] + 1;\nf[x_] := x^2  (* f used before defined *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_];  (* Forward declaration *)\ng[x_] := f[x] + 1;\nf[x_] := x^2</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("forward-reference", "declaration");

        // ===== CHUNK 3 RULE DEFINITIONS (Items 111-150 from ROADMAP_325.md) =====

        // Type Mismatch Detection Rules (Items 111-130)

        repository.createRule(NUMERIC_OPERATION_ON_STRING_KEY)
            .setName("Numeric operations on strings cause runtime errors")
            .setHtmlDescription(
                "<p>Performing arithmetic operations on string values produces unexpected results or errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\"hello\" + 1  (* Concatenates, doesn't add *)\n" +
                "\"hello\"^2  (* Returns unevaluated *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>ToExpression[\"5\"] + 1  (* Convert string to number first *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "runtime-error");

        repository.createRule(STRING_OPERATION_ON_NUMBER_KEY)
            .setName("String operations on numbers cause runtime errors")
            .setHtmlDescription(
                "<p>Using string functions on numeric values causes runtime errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>StringJoin[123, \"abc\"]  (* Wrong argument type *)\n" +
                "StringLength[42]  (* Expects string *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>StringJoin[ToString[123], \"abc\"]  (* Convert to string first *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "runtime-error");

        repository.createRule(WRONG_ARGUMENT_TYPE_KEY)
            .setName("Function called with wrong argument type")
            .setHtmlDescription(
                "<p>Passing wrong types to built-in functions causes runtime errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Map[f, 123]  (* Expects list, not integer *)\n" +
                "Length[5]  (* Expects list/association/string *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Map[f, {1,2,3}]  (* Use correct type *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "argument-type");

        repository.createRule(FUNCTION_RETURNS_WRONG_TYPE_KEY)
            .setName("Function returns type different from declaration")
            .setHtmlDescription(
                "<p>Functions should return consistent types matching their documentation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>(* Documented to return Integer *)\n" +
                "calculate[x_] := If[x > 0, x, \"error\"]  (* Returns String sometimes *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>calculate[x_?Positive] := x  (* Type-safe *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "return-type");

        repository.createRule(COMPARISON_INCOMPATIBLE_TYPES_KEY)
            .setName("Comparison of incompatible types")
            .setHtmlDescription(
                "<p>Comparing values of incompatible types produces meaningless results.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\"hello\" < 5  (* Compares but meaningless *)\n" +
                "{1,2} == 3  (* Always False *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>StringLength[\"hello\"] < 5  (* Compare compatible types *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("type-mismatch", "comparison");

        repository.createRule(MIXED_NUMERIC_TYPES_KEY)
            .setName("Mixing exact and approximate numbers loses precision")
            .setHtmlDescription(
                "<p>Mixing exact (Integer/Rational) with approximate (Real) numbers causes precision loss.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>1/3 + 0.5  (* Converts to approximate *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>1/3 + 1/2  (* Keep exact *)\nN[1/3] + 0.5  (* Or be explicit *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("numeric-precision", "type-mismatch");

        repository.createRule(INTEGER_DIVISION_EXPECTING_REAL_KEY)
            .setName("Integer division stays symbolic, use real division for numeric result")
            .setHtmlDescription(
                "<p>Division of integers stays symbolic unless converted to real.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>1/2  (* Evaluates to 1/2, not 0.5 *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>1./2  (* Evaluates to 0.5 *)\nN[1/2]  (* Explicit conversion *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("numeric-precision", "integer-division");

        repository.createRule(LIST_FUNCTION_ON_ASSOCIATION_KEY)
            .setName("List functions should not be used on associations")
            .setHtmlDescription(
                "<p>Using list functions on associations has different semantics than association functions.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Append[<|a->1|>, b->2]  (* Wrong semantics *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>AssociateTo[<|a->1|>, b->2]  (* Correct for associations *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "associations");

        repository.createRule(PATTERN_TYPE_MISMATCH_KEY)
            .setName("Function call doesn't match pattern types")
            .setHtmlDescription(
                "<p>Calling function with argument that doesn't match pattern type constraint.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_Integer] := x^2;\n" +
                "f[\"hello\"]  (* Won't match, returns unevaluated *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[5]  (* Matches pattern *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns", "type-mismatch");

        repository.createRule(OPTIONAL_TYPE_INCONSISTENT_KEY)
            .setName("Optional parameter default has wrong type")
            .setHtmlDescription(
                "<p>Default value for optional parameter should match the pattern type.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_Integer : 1.5] := x  (* Default is Real, not Integer *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_Integer : 1] := x  (* Consistent types *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("patterns", "optional-parameters");

        repository.createRule(RETURN_TYPE_INCONSISTENT_KEY)
            .setName("Function returns inconsistent types")
            .setHtmlDescription(
                "<p>Functions that return different types from different branches are confusing.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_] := If[x > 0, x, \"negative\"]  (* Returns Integer or String *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_] := If[x > 0, x, -1]  (* Consistent Integer return *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("return-type", "api-design");

        repository.createRule(NULL_ASSIGNMENT_TO_TYPED_VARIABLE_KEY)
            .setName("Null assigned to variable expected to be numeric")
            .setHtmlDescription(
                "<p>Assigning Null to variables used in numeric contexts causes errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x = Null; result = x + 1  (* Error *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 0; result = x + 1  (* Or use Missing[\"NotAvailable\"] *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("null-safety", "type-mismatch");

        repository.createRule(TYPE_CAST_WITHOUT_VALIDATION_KEY)
            .setName("Type conversion without validation")
            .setHtmlDescription(
                "<p>Converting types without checking validity can cause runtime errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>ToExpression[userInput]  (* May not be valid expression *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>If[StringQ[userInput], ToExpression[userInput], $Failed]</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-casting", "validation");

        repository.createRule(IMPLICIT_TYPE_CONVERSION_KEY)
            .setName("Redundant type conversion")
            .setHtmlDescription(
                "<p>Converting values that are already the target type is redundant.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>ToString[\"hello\"]  (* Already a string *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\"hello\"  (* No conversion needed *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("redundant", "type-conversion");

        repository.createRule(GRAPHICS_OBJECT_IN_NUMERIC_CONTEXT_KEY)
            .setName("Graphics object used in numeric computation")
            .setHtmlDescription(
                "<p>Using graphics objects in numeric contexts doesn't make sense.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Plot[x^2, {x, 0, 1}] + 1  (* Graphics + Number? *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Extract data first or fix logic error *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "graphics");

        repository.createRule(SYMBOL_IN_NUMERIC_CONTEXT_KEY)
            .setName("Symbolic variable in numeric context")
            .setHtmlDescription(
                "<p>Using undefined symbolic variables in numeric computations may not evaluate.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x + 1  (* If x undefined, returns x+1 symbolically *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 5; x + 1  (* Assign value first *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("symbolic", "numeric-context");

        repository.createRule(IMAGE_OPERATION_ON_NON_IMAGE_KEY)
            .setName("Image operation on non-Image object")
            .setHtmlDescription(
                "<p>Image functions require Image objects, not raw arrays.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>ImageData[{{0,0},{1,1}}]  (* Expects Image, not array *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>ImageData[Image[{{0,0},{1,1}}]]  (* Wrap in Image first *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "image-processing");

        repository.createRule(SOUND_OPERATION_ON_NON_SOUND_KEY)
            .setName("Audio operation on non-Audio object")
            .setHtmlDescription(
                "<p>Audio functions require Audio objects, not raw arrays.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>AudioData[{1,2,3}]  (* Expects Audio, not list *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>AudioData[Audio[{1,2,3}]]  (* Wrap in Audio first *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "audio-processing");

        repository.createRule(DATASET_OPERATION_ON_LIST_KEY)
            .setName("Dataset operations require Dataset wrapper")
            .setHtmlDescription(
                "<p>Dataset-specific operations need data wrapped in Dataset.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>data = {{1,2},{3,4}};\ndata[All, \"col1\"]  (* Doesn't work on list *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Dataset[data][All, \"col1\"]  (* Wrap in Dataset *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "dataset");

        repository.createRule(GRAPH_OPERATION_ON_NON_GRAPH_KEY)
            .setName("Graph operation on non-Graph object")
            .setHtmlDescription(
                "<p>Graph functions require Graph objects, not edge lists.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>VertexList[{{1,2},{2,3}}]  (* Expects Graph, not edge list *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>VertexList[Graph[{{1,2},{2,3}}]]  (* Create Graph first *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("type-mismatch", "graph-theory");

        // Data Flow Analysis Rules (Items 135-150)

        repository.createRule(UNINITIALIZED_VARIABLE_USE_ENHANCED_KEY)
            .setName("Variable used before initialization")
            .setHtmlDescription(
                "<p>Using variables before assigning a value causes runtime errors or unexpected behavior.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>result = x + 1  (* x never initialized *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 0; result = x + 1  (* Initialize first *)</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("uninitialized", "data-flow");

        repository.createRule(VARIABLE_MAY_BE_UNINITIALIZED_KEY)
            .setName("Variable may be uninitialized in some code paths")
            .setHtmlDescription(
                "<p>Variable initialized in some branches but not all causes logic errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>If[condition, x = 1];\nresult = x  (* x undefined if condition False *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = If[condition, 1, 0];\nresult = x  (* Always initialized *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("uninitialized", "data-flow");

        repository.createRule(DEAD_STORE_KEY)
            .setName("Value assigned but never read")
            .setHtmlDescription(
                "<p>Assigning values that are never read is useless computation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x = expensiveComputation[];  (* Value never used *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Remove unused assignment or use the value *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("dead-store", "performance");

        repository.createRule(OVERWRITTEN_BEFORE_READ_KEY)
            .setName("Assignment overwritten before being read")
            .setHtmlDescription(
                "<p>Assigning a value that's overwritten before being read is wasteful.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x = 1; x = 2; Print[x]  (* First assignment wasted *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 2; Print[x]  (* Remove redundant assignment *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("redundant", "data-flow");

        repository.createRule(VARIABLE_ALIASING_ISSUE_KEY)
            .setName("Multiple variables point to same mutable structure")
            .setHtmlDescription(
                "<p>Aliasing mutable structures causes unexpected modifications.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>list1 = {1,2,3};\nlist2 = list1;\nlist2[[1]] = 99  (* Also modifies list1 *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>list2 = list1  (* Copy if needed for independent modification *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("aliasing", "mutable-state");

        repository.createRule(MODIFICATION_OF_LOOP_ITERATOR_KEY)
            .setName("Loop iterator should not be modified inside loop")
            .setHtmlDescription(
                "<p>Modifying loop iterators inside the loop body is confusing and error-prone.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Do[i = i + 1; Print[i], {i, 1, 10}]  (* Modifying iterator *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Do[Print[i], {i, 1, 10}]  (* Don't modify iterator *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("loops", "iterator-modification");

        repository.createRule(USE_OF_ITERATOR_OUTSIDE_LOOP_KEY)
            .setName("Loop iterator value after loop is undefined")
            .setHtmlDescription(
                "<p>Using loop iterator after loop ends is unreliable - value is implementation-dependent.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Do[..., {i, 1, 10}];\nPrint[i]  (* i value after loop undefined *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Don't rely on iterator value after loop *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("loops", "iterator-scope");

        repository.createRule(READING_UNSET_VARIABLE_KEY)
            .setName("Reading variable after Unset or Clear")
            .setHtmlDescription(
                "<p>Reading a variable after Unset/Clear returns the symbol itself, not a value.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x = 5; Unset[x]; Print[x]  (* Prints symbol x, not value *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 5; Print[x]; Unset[x]  (* Read before unsetting *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("unset", "data-flow");

        repository.createRule(DOUBLE_ASSIGNMENT_SAME_VALUE_KEY)
            .setName("Variable assigned same value twice")
            .setHtmlDescription(
                "<p>Assigning the same value to a variable multiple times is redundant.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>x = 5; ...; x = 5  (* Same value assigned twice *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>x = 5;  (* Remove redundant assignment *)</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("redundant", "code-smell");

        repository.createRule(MUTATION_IN_PURE_FUNCTION_KEY)
            .setName("Pure function mutates outer variable")
            .setHtmlDescription(
                "<p>Pure functions with side effects are confusing and break functional paradigm.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>counter = 0;\nMap[(counter++; #) &, list]  (* Side effect in pure function *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>MapIndexed[...  (* Use stateless approach *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("pure-functions", "side-effects");

        repository.createRule(SHARED_MUTABLE_STATE_KEY)
            .setName("Global mutable state accessed from multiple functions")
            .setHtmlDescription(
                "<p>Shared mutable global state is hard to reason about and debug.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>globalCounter = 0;\nf[] := globalCounter++\ng[] := globalCounter--  (* Both modify global *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>(* Pass state as parameters or use Module *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("global-state", "mutable-state");

        repository.createRule(VARIABLE_SCOPE_ESCAPE_KEY)
            .setName("Module local variable escapes its scope")
            .setHtmlDescription(
                "<p>Returning Module local variables causes them to escape as symbols.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Module[{x}, x]  (* Returns symbol, not value *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>Module[{x = 5}, x]  (* Return value, not symbol *)</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("scope", "module");

        repository.createRule(CLOSURE_OVER_MUTABLE_VARIABLE_KEY)
            .setName("Pure function captures mutable variable")
            .setHtmlDescription(
                "<p>Closures capturing mutable variables may not capture the expected value.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>funcs = Table[Function[x + i], {i, 3}]  (* All capture final i *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>funcs = Table[With[{j = i}, Function[x + j]], {i, 3}]  (* Capture value *)</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("closures", "variable-capture");

        repository.createRule(ASSIGNMENT_IN_CONDITION_ENHANCED_KEY)
            .setName("Assignment in condition instead of comparison")
            .setHtmlDescription(
                "<p>Using = instead of == in conditions is almost always a bug.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>If[x = 5, ...]  (* Assigns 5 to x, always true *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>If[x == 5, ...]  (* Compare, don't assign *)</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("assignment", "condition");

        repository.createRule(ASSIGNMENT_AS_RETURN_VALUE_KEY)
            .setName("Unnecessary variable assignment before return")
            .setHtmlDescription(
                "<p>Assigning to variable just to return it immediately is unnecessary.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>f[x_] := (y = x; y)  (* Unnecessary variable *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>f[x_] := x  (* Return directly *)</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("redundant", "return-value");

        repository.createRule(VARIABLE_NEVER_MODIFIED_KEY)
            .setName("Module variable never modified, use With instead")
            .setHtmlDescription(
                "<p>Variables that are never modified should use With for immutability guarantees.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>Module[{x = 1}, computeWith[x]]  (* x never modified *)</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>With[{x = 1}, computeWith[x]]  (* Immutable *)</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("immutability", "best-practice");

        repository.done();
    }
}
