package org.sonar.plugins.mathematica.rules;

/**
 * Central registry of all Mathematica rule keys.
 *
 * This class contains all rule key constants used throughout the plugin.
 * Organized by category for easier navigation and maintenance.
 */
public final class MathematicaRuleKeys {

    private MathematicaRuleKeys() {
        // Utility class - prevent instantiation
    }

    // Repository configuration
    public static final String REPOSITORY_KEY = "wolfralyze";
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

    // Rule keys - Pattern System Rules (Items 16-30 from ROADMAP_325.md)
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

    // Rule keys - List/Array Rules (Items 31-40 from ROADMAP_325.md)
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

    // Rule keys - Association Rules (Items 41-50 from ROADMAP_325.md)
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

    // Rule keys - Unused Code Detection (Items 61-75 from ROADMAP_325.md)
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

    // Rule keys - Shadowing & Naming (Items 76-90 from ROADMAP_325.md)
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

    // Rule keys - Undefined Symbol Detection (Items 91-100 from ROADMAP_325.md)
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

    // Rule keys - Type Mismatch Detection (Items 111-130 from ROADMAP_325.md)
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

    // Rule keys - Data Flow Analysis (Items 135-150 from ROADMAP_325.md)
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

    // Rule keys - Dead Code & Reachability (Items 161-175 from ROADMAP_325.md)
    public static final String UNREACHABLE_CODE_AFTER_RETURN_KEY = "UnreachableCodeAfterReturn";
    public static final String UNREACHABLE_BRANCH_ALWAYS_TRUE_KEY = "UnreachableBranchAlwaysTrue";
    public static final String UNREACHABLE_BRANCH_ALWAYS_FALSE_KEY = "UnreachableBranchAlwaysFalse";
    public static final String IMPOSSIBLE_PATTERN_KEY = "ImpossiblePattern";
    public static final String EMPTY_CATCH_BLOCK_ENHANCED_KEY = "EmptyCatchBlockEnhanced";
    public static final String CONDITION_ALWAYS_EVALUATES_SAME_KEY = "ConditionAlwaysEvaluatesSame";
    public static final String INFINITE_LOOP_PROVEN_KEY = "InfiniteLoopProven";
    public static final String LOOP_NEVER_EXECUTES_KEY = "LoopNeverExecutes";
    public static final String CODE_AFTER_ABORT_KEY = "CodeAfterAbort";
    public static final String MULTIPLE_RETURNS_MAKE_CODE_UNREACHABLE_KEY = "MultipleReturnsMakeCodeUnreachable";
    public static final String ELSE_BRANCH_NEVER_TAKEN_KEY = "ElseBranchNeverTaken";
    public static final String SWITCH_CASE_SHADOWED_KEY = "SwitchCaseShadowed";
    public static final String PATTERN_DEFINITION_SHADOWED_KEY = "PatternDefinitionShadowed";
    public static final String EXCEPTION_NEVER_THROWN_KEY = "ExceptionNeverThrown";
    public static final String BREAK_OUTSIDE_LOOP_KEY = "BreakOutsideLoop";

    // Rule keys - Taint Analysis for Security (Items 181-195 from ROADMAP_325.md)
    public static final String SQL_INJECTION_TAINT_KEY = "SqlInjectionTaint";
    public static final String COMMAND_INJECTION_TAINT_KEY = "CommandInjectionTaint";
    public static final String CODE_INJECTION_TAINT_KEY = "CodeInjectionTaint";
    public static final String PATH_TRAVERSAL_TAINT_KEY = "PathTraversalTaint";
    public static final String XSS_TAINT_KEY = "XssTaint";
    public static final String LDAP_INJECTION_KEY = "LdapInjection";
    public static final String XXE_TAINT_KEY = "XxeTaint";
    public static final String UNSAFE_DESERIALIZATION_TAINT_KEY = "UnsafeDeserializationTaint";
    public static final String SSRF_TAINT_KEY = "SsrfTaint";
    public static final String INSECURE_RANDOMNESS_ENHANCED_KEY = "InsecureRandomnessEnhanced";
    public static final String WEAK_CRYPTOGRAPHY_ENHANCED_KEY = "WeakCryptographyEnhanced";
    public static final String HARD_CODED_CREDENTIALS_TAINT_KEY = "HardCodedCredentialsTaint";
    public static final String SENSITIVE_DATA_IN_LOGS_KEY = "SensitiveDataInLogs";
    public static final String MASS_ASSIGNMENT_KEY = "MassAssignment";
    public static final String REGEX_DOS_KEY = "RegexDoS";

    // Rule keys - Additional Control Flow Rules (Items 196-200 from ROADMAP_325.md)
    public static final String MISSING_DEFAULT_CASE_KEY = "MissingDefaultCase";
    public static final String EMPTY_IF_BRANCH_KEY = "EmptyIfBranch";
    public static final String NESTED_IF_DEPTH_KEY = "NestedIfDepth";
    public static final String TOO_MANY_RETURN_POINTS_KEY = "TooManyReturnPoints";
    public static final String MISSING_ELSE_CONSIDERED_HARMFUL_KEY = "MissingElseConsideredHarmful";

    // Rule keys - Dependency & Architecture Rules (Items 211-230 from ROADMAP_325.md)
    public static final String CIRCULAR_PACKAGE_DEPENDENCY_KEY = "CircularPackageDependency";
    public static final String UNUSED_PACKAGE_IMPORT_KEY = "UnusedPackageImport";
    public static final String MISSING_PACKAGE_IMPORT_KEY = "MissingPackageImport";
    public static final String TRANSITIVE_DEPENDENCY_COULD_BE_DIRECT_KEY = "TransitiveDependencyCouldBeDirect";
    public static final String DIAMOND_DEPENDENCY_KEY = "DiamondDependency";
    public static final String GOD_PACKAGE_TOO_MANY_DEPENDENCIES_KEY = "GodPackageTooManyDependencies";
    public static final String PACKAGE_DEPENDS_ON_APPLICATION_CODE_KEY = "PackageDependsOnApplicationCode";
    public static final String CYCLIC_CALL_BETWEEN_PACKAGES_KEY = "CyclicCallBetweenPackages";
    public static final String LAYER_VIOLATION_KEY = "LayerViolation";
    public static final String UNSTABLE_DEPENDENCY_KEY = "UnstableDependency";
    public static final String PACKAGE_TOO_LARGE_KEY = "PackageTooLarge";
    public static final String PACKAGE_TOO_SMALL_KEY = "PackageTooSmall";
    public static final String INCONSISTENT_PACKAGE_NAMING_KEY = "InconsistentPackageNaming";
    public static final String PACKAGE_EXPORTS_TOO_MUCH_KEY = "PackageExportsTooMuch";
    public static final String PACKAGE_EXPORTS_TOO_LITTLE_KEY = "PackageExportsTooLittle";
    public static final String INCOMPLETE_PUBLIC_API_KEY = "IncompletePublicAPI";
    public static final String PRIVATE_SYMBOL_USED_EXTERNALLY_KEY = "PrivateSymbolUsedExternally";
    public static final String INTERNAL_IMPLEMENTATION_EXPOSED_KEY = "InternalImplementationExposed";
    public static final String MISSING_PACKAGE_DOCUMENTATION_KEY = "MissingPackageDocumentation";
    public static final String PUBLIC_API_CHANGED_WITHOUT_VERSION_BUMP_KEY = "PublicAPIChangedWithoutVersionBump";

    // Rule keys - Unused Export & Dead Code (Items 231-245 from ROADMAP_325.md)
    public static final String UNUSED_PUBLIC_FUNCTION_KEY = "UnusedPublicFunction";
    public static final String UNUSED_EXPORT_KEY = "UnusedExport";
    public static final String DEAD_PACKAGE_KEY = "DeadPackage";
    public static final String FUNCTION_ONLY_CALLED_ONCE_KEY = "FunctionOnlyCalledOnce";
    public static final String OVER_ABSTRACTED_API_KEY = "OverAbstractedAPI";
    public static final String ORPHANED_TEST_FILE_KEY = "OrphanedTestFile";
    public static final String IMPLEMENTATION_WITHOUT_TESTS_KEY = "ImplementationWithoutTests";
    public static final String DEPRECATED_API_STILL_USED_INTERNALLY_KEY = "DeprecatedAPIStillUsedInternally";
    public static final String INTERNAL_API_USED_LIKE_PUBLIC_KEY = "InternalAPIUsedLikePublic";
    public static final String COMMENTED_OUT_PACKAGE_LOAD_KEY = "CommentedOutPackageLoad";
    public static final String CONDITIONAL_PACKAGE_LOAD_KEY = "ConditionalPackageLoad";
    public static final String PACKAGE_LOADED_BUT_NOT_LISTED_IN_METADATA_KEY = "PackageLoadedButNotListedInMetadata";
    public static final String DUPLICATE_SYMBOL_DEFINITION_ACROSS_PACKAGES_KEY = "DuplicateSymbolDefinitionAcrossPackages";
    public static final String SYMBOL_REDEFINITION_AFTER_IMPORT_KEY = "SymbolRedefinitionAfterImport";
    public static final String PACKAGE_VERSION_MISMATCH_KEY = "PackageVersionMismatch";

    // Rule keys - Documentation & Consistency (Items 246-250 from ROADMAP_325.md)
    public static final String PUBLIC_EXPORT_MISSING_USAGE_MESSAGE_KEY = "PublicExportMissingUsageMessage";
    public static final String INCONSISTENT_PARAMETER_NAMES_ACROSS_OVERLOADS_KEY = "InconsistentParameterNamesAcrossOverloads";
    public static final String PUBLIC_FUNCTION_WITH_IMPLEMENTATION_DETAILS_IN_NAME_KEY = "PublicFunctionWithImplementationDetailsInName";
    public static final String PUBLIC_API_NOT_IN_PACKAGE_CONTEXT_KEY = "PublicAPINotInPackageContext";
    public static final String TEST_FUNCTION_IN_PRODUCTION_CODE_KEY = "TestFunctionInProductionCode";

    // Rule keys - Null Safety (Items 251-265 from ROADMAP_325.md)
    public static final String NULL_DEREFERENCE_KEY = "NullDereference";
    public static final String MISSING_NULL_CHECK_KEY = "MissingNullCheck";
    public static final String NULL_PASSED_TO_NON_NULLABLE_KEY = "NullPassedToNonNullable";
    public static final String INCONSISTENT_NULL_HANDLING_KEY = "InconsistentNullHandling";
    public static final String NULL_RETURN_NOT_DOCUMENTED_KEY = "NullReturnNotDocumented";
    public static final String COMPARISON_WITH_NULL_KEY = "ComparisonWithNull";
    public static final String MISSING_CHECK_LEADS_TO_NULL_PROPAGATION_KEY = "MissingCheckLeadsToNullPropagation";
    public static final String CHECK_PATTERN_DOESNT_HANDLE_ALL_CASES_KEY = "CheckPatternDoesntHandleAllCases";
    public static final String QUIET_SUPPRESSING_IMPORTANT_MESSAGES_KEY = "QuietSuppressingImportantMessages";
    public static final String OFF_DISABLING_IMPORTANT_WARNINGS_KEY = "OffDisablingImportantWarnings";
    public static final String CATCH_ALL_EXCEPTION_HANDLER_KEY = "CatchAllExceptionHandler";
    public static final String EMPTY_EXCEPTION_HANDLER_KEY = "EmptyExceptionHandler";
    public static final String THROW_WITHOUT_CATCH_KEY = "ThrowWithoutCatch";
    public static final String ABORT_IN_LIBRARY_CODE_KEY = "AbortInLibraryCode";
    public static final String MESSAGE_WITHOUT_DEFINITION_KEY = "MessageWithoutDefinition";
    public static final String MISSING_MESSAGE_DEFINITION_KEY = "MissingMessageDefinition";

    // Rule keys - Constant & Expression Analysis (Items 267-280 from ROADMAP_325.md)
    public static final String CONDITION_ALWAYS_TRUE_CONSTANT_PROPAGATION_KEY = "ConditionAlwaysTrueConstantPropagation";
    public static final String CONDITION_ALWAYS_FALSE_CONSTANT_PROPAGATION_KEY = "ConditionAlwaysFalseConstantPropagation";
    public static final String LOOP_BOUND_CONSTANT_KEY = "LoopBoundConstant";
    public static final String REDUNDANT_COMPUTATION_KEY = "RedundantComputation";
    public static final String PURE_EXPRESSION_IN_LOOP_KEY = "PureExpressionInLoop";
    public static final String CONSTANT_EXPRESSION_KEY = "ConstantExpression";
    public static final String IDENTITY_OPERATION_KEY = "IdentityOperation";
    public static final String COMPARISON_OF_IDENTICAL_EXPRESSIONS_KEY = "ComparisonOfIdenticalExpressions";
    public static final String BOOLEAN_EXPRESSION_ALWAYS_TRUE_KEY = "BooleanExpressionAlwaysTrue";
    public static final String BOOLEAN_EXPRESSION_ALWAYS_FALSE_KEY = "BooleanExpressionAlwaysFalse";
    public static final String UNNECESSARY_BOOLEAN_CONVERSION_KEY = "UnnecessaryBooleanConversion";
    public static final String DOUBLE_NEGATION_KEY = "DoubleNegation";
    public static final String COMPLEX_BOOLEAN_EXPRESSION_ENHANCED_KEY = "ComplexBooleanExpressionEnhanced";
    public static final String DE_MORGANS_LAW_OPPORTUNITY_KEY = "DeMorgansLawOpportunity";

    // Rule keys - Mathematica-Specific Patterns (Items 281-300 from ROADMAP_325.md)
    public static final String HOLD_ATTRIBUTE_MISSING_KEY = "HoldAttributeMissing";
    public static final String HOLD_FIRST_BUT_USES_SECOND_ARGUMENT_FIRST_KEY = "HoldFirstButUsesSecondArgumentFirst";
    public static final String MISSING_UNEVALUATED_WRAPPER_KEY = "MissingUnevaluatedWrapper";
    public static final String UNNECESSARY_HOLD_KEY = "UnnecessaryHold";
    public static final String RELEASE_HOLD_AFTER_HOLD_KEY = "ReleaseHoldAfterHold";
    public static final String EVALUATE_IN_HELD_CONTEXT_KEY = "EvaluateInHeldContext";
    public static final String PATTERN_WITH_SIDE_EFFECT_KEY = "PatternWithSideEffect";
    public static final String REPLACEMENT_RULE_ORDER_MATTERS_KEY = "ReplacementRuleOrderMatters";
    public static final String REPLACE_ALL_VS_REPLACE_CONFUSION_KEY = "ReplaceAllVsReplaceConfusion";
    public static final String RULE_DOESNT_MATCH_DUE_TO_EVALUATION_KEY = "RuleDoesntMatchDueToEvaluation";
    public static final String PART_SPECIFICATION_OUT_OF_BOUNDS_KEY = "PartSpecificationOutOfBounds";
    public static final String SPAN_SPECIFICATION_INVALID_KEY = "SpanSpecificationInvalid";
    public static final String ALL_SPECIFICATION_INEFFICIENT_KEY = "AllSpecificationInefficient";
    public static final String THREADING_OVER_NON_LISTS_KEY = "ThreadingOverNonLists";
    public static final String MISSING_ATTRIBUTES_DECLARATION_KEY = "MissingAttributesDeclaration";
    public static final String ONE_IDENTITY_ATTRIBUTE_MISUSE_KEY = "OneIdentityAttributeMisuse";
    public static final String ORDERLESS_ATTRIBUTE_ON_NON_COMMUTATIVE_KEY = "OrderlessAttributeOnNonCommutative";
    public static final String FLAT_ATTRIBUTE_MISUSE_KEY = "FlatAttributeMisuse";
    public static final String SEQUENCE_IN_UNEXPECTED_CONTEXT_KEY = "SequenceInUnexpectedContext";
    public static final String MISSING_SEQUENCE_WRAPPER_KEY = "MissingSequenceWrapper";

    // Rule keys - Test Coverage Integration (Items 307-310 from ROADMAP_325.md)
    public static final String LOW_TEST_COVERAGE_WARNING_KEY = "LowTestCoverageWarning";
    public static final String UNTESTED_PUBLIC_FUNCTION_KEY = "UntestedPublicFunction";
    public static final String UNTESTED_BRANCH_KEY = "UntestedBranch";
    public static final String TEST_ONLY_CODE_IN_PRODUCTION_KEY = "TestOnlyCodeInProduction";

    // Rule keys - Performance Analysis (Items 312-320 from ROADMAP_325.md)
    public static final String COMPILABLE_FUNCTION_NOT_COMPILED_KEY = "CompilableFunctionNotCompiled";
    public static final String COMPILATION_TARGET_MISSING_KEY = "CompilationTargetMissing";
    public static final String NON_COMPILABLE_CONSTRUCT_IN_COMPILE_KEY = "NonCompilableConstructInCompile";
    public static final String PACKED_ARRAY_UNPACKED_KEY = "PackedArrayUnpacked";
    public static final String INEFFICIENT_PATTERN_IN_PERFORMANCE_CRITICAL_CODE_KEY = "InefficientPatternInPerformanceCriticalCode";
    public static final String N_APPLIED_TOO_LATE_KEY = "NAppliedTooLate";
    public static final String MISSING_MEMOIZATION_OPPORTUNITY_ENHANCED_KEY = "MissingMemoizationOpportunityEnhanced";
    public static final String INEFFICIENT_STRING_CONCATENATION_ENHANCED_KEY = "InefficientStringConcatenationEnhanced";
    public static final String LIST_CONCATENATION_IN_LOOP_KEY = "ListConcatenationInLoop";

    // Rule keys - Symbol Table Analysis (Items 326-335 - New advanced rules)
    public static final String UNUSED_VARIABLE_KEY = "UnusedVariable";
    public static final String ASSIGNED_BUT_NEVER_READ_KEY = "AssignedButNeverRead";
    // Note: DEAD_STORE_KEY already defined above - reusing that definition
    public static final String USED_BEFORE_ASSIGNMENT_KEY = "UsedBeforeAssignment";
    public static final String VARIABLE_SHADOWING_KEY = "VariableShadowing";
    public static final String UNUSED_PARAMETER_KEY = "UnusedParameter";
    public static final String WRITE_ONLY_VARIABLE_KEY = "WriteOnlyVariable";
    public static final String REDUNDANT_ASSIGNMENT_KEY = "RedundantAssignment";
    public static final String VARIABLE_IN_WRONG_SCOPE_KEY = "VariableInWrongScope";
    public static final String VARIABLE_ESCAPES_SCOPE_KEY = "VariableEscapesScope";

    // Rule keys - Advanced Symbol Table Analysis (Items 336-345 - Enhanced rules)
    public static final String LIFETIME_EXTENDS_BEYOND_SCOPE_KEY = "LifetimeExtendsBeyondScope";
    public static final String MODIFIED_IN_UNEXPECTED_SCOPE_KEY = "ModifiedInUnexpectedScope";
    public static final String GLOBAL_VARIABLE_POLLUTION_KEY = "GlobalVariablePollution";
    public static final String CIRCULAR_VARIABLE_DEPENDENCIES_KEY = "CircularVariableDependencies";
    public static final String NAMING_CONVENTION_VIOLATIONS_KEY = "NamingConventionViolations";
    public static final String CONSTANT_NOT_MARKED_AS_CONSTANT_KEY = "ConstantNotMarkedAsConstant";
    public static final String TYPE_INCONSISTENCY_KEY = "TypeInconsistency";
    public static final String VARIABLE_REUSE_WITH_DIFFERENT_SEMANTICS_KEY = "VariableReuseWithDifferentSemantics";
    public static final String INCORRECT_CLOSURE_CAPTURE_KEY = "IncorrectClosureCapture";
    public static final String SCOPE_LEAK_THROUGH_DYNAMIC_EVALUATION_KEY = "ScopeLeakThroughDynamicEvaluation";

    // ===== CODING STANDARD RULES (32 rules) =====

    // Coding Standards: Syntax and Whitespace (18 rules)
    public static final String BRACKET_SPACING_BEFORE_KEY = "BracketSpacingBefore";
    public static final String EGYPTIAN_BRACKETS_WITH_TABS_KEY = "EgyptianBracketsWithTabs";
    public static final String VARIABLE_ASSIGNMENT_IN_MODULE_DEF_KEY = "VariableAssignmentInModuleDef";
    public static final String EXPLICIT_AND_OR_FOR_COMPLEX_BOOLEAN_KEY = "ExplicitAndOrForComplexBoolean";
    public static final String MAP_NOT_SHORTHAND_MULTILINE_KEY = "MapNotShorthandMultiline";
    public static final String ERROR_MESSAGE_WITH_SET_KEY = "ErrorMessageWithSet";
    public static final String MODULE_FOR_LOCAL_VARIABLES_KEY = "ModuleForLocalVariables";
    public static final String CONDITIONAL_FUNCTION_DEFINITION_KEY = "ConditionalFunctionDefinition";
    public static final String DEREFERENCING_SYNTAX_KEY = "DereferencingSyntax";
    public static final String EMPTY_LINE_BETWEEN_CODE_KEY = "EmptyLineBetweenCode";
    public static final String EGYPTIAN_BRACKETS_IF_KEY = "EgyptianBracketsIf";
    public static final String SWITCH_WHICH_SEPARATE_LINES_KEY = "SwitchWhichSeparateLines";
    public static final String EGYPTIAN_BRACKETS_MAP_KEY = "EgyptianBracketsMap";
    public static final String VARIABLE_NAME_LENGTH_KEY = "VariableNameLength";
    public static final String LIST_MODIFICATION_IN_PLACE_KEY = "ListModificationInPlace";
    public static final String SHORTHAND_SYNTAX_READABLE_KEY = "ShorthandSyntaxReadable";
    public static final String NON_LINEAR_EVALUATION_KEY = "NonLinearEvaluation";

    // Coding Standards: Local Variables (2 rules)
    public static final String VARIABLE_FULL_WORDS_KEY = "VariableFullWords";
    public static final String VARIABLE_NAME_THREE_WORDS_KEY = "VariableNameThreeWords";

    // Coding Standards: Function Structure (6 rules)
    public static final String PRIVATE_FUNCTION_PACKAGE_SCOPE_KEY = "PrivateFunctionPackageScope";
    public static final String CUSTOM_ASSOCIATIONS_AS_INPUTS_KEY = "CustomAssociationsAsInputs";
    public static final String GLOBAL_VARIABLE_DOLLAR_PREFIX_KEY = "GlobalVariableDollarPrefix";
    public static final String FUNCTION_NAME_THREE_WORDS_KEY = "FunctionNameThreeWords";
    public static final String FUNCTION_NAME_LITTER_WORDS_KEY = "FunctionNameLitterWords";
    public static final String PURE_FUNCTION_SHORT_OPERATIONS_KEY = "PureFunctionShortOperations";

    // Coding Standards: Code Organization (2 CRITICAL rules)
    public static final String TIME_CONSTRAINED_USAGE_KEY = "TimeConstrainedUsage";
    public static final String PATTERN_LOADING_ORDER_KEY = "PatternLoadingOrder";

    // Coding Standards: Patterns (4 rules)
    public static final String PATTERN_NAME_ENDS_WITH_P_KEY = "PatternNameEndsWithP";
    public static final String PATTERN_TEST_NAME_ENDS_WITH_Q_KEY = "PatternTestNameEndsWithQ";
    public static final String ENUMERATED_PATTERN_SYMBOLS_KEY = "EnumeratedPatternSymbols";
    public static final String PATTERN_GENERATING_FUNCTION_ENDS_WITH_P_KEY = "PatternGeneratingFunctionEndsWithP";
}
