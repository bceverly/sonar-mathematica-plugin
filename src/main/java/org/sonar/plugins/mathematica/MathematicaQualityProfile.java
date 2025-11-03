package org.sonar.plugins.mathematica;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition;

/**
 * Defines the default quality profile for Mathematica.
 * This is required by SonarQube - every language must have at least one quality profile.
 */
public class MathematicaQualityProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(
            "Sonar way",  // Profile name
            MathematicaLanguage.KEY
        );

        // Mark this as the default profile
        profile.setDefault(true);

        // Activate all advanced rules through categorized methods
        defineAdvancedRules(profile);
    }

    /**
     * Main orchestration method that calls all rule category methods.
     * Each category is organized by domain and kept under 150 lines.
     */
    private void defineAdvancedRules(NewBuiltInQualityProfile profile) {
        defineBugDetectionAndReliabilityRules(profile);
        definePerformanceRules(profile);
        defineMathematicaPatternRules(profile);
        defineCodeSmellsAndComplexityRules(profile);
        defineNamingAndDocumentationRules(profile);
        defineUnusedCodeRules(profile);
        defineVariableAndScopeRules(profile);
        definePackageStructureRules(profile);
        defineTestingRules(profile);
        defineSecurityAndScaRules(profile);
        profile.done();
    }

    /**
     * Bug Detection and Reliability Issues (108 rules total)
     * Covers critical bugs, reliability issues, type mismatches, and logic errors.
     */
    private void defineBugDetectionAndReliabilityRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITIONAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITION_ENHANCED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSIGNMENT_NEVER_READ_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSOCIATION_VS_LIST_CONFUSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BLOCK_MODULE_MISUSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BOOLEAN_EXPRESSION_ALWAYS_FALSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BOOLEAN_EXPRESSION_ALWAYS_TRUE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BREAK_OUTSIDE_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BUILTIN_NAME_IN_LOCAL_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CATCH_WITHOUT_THROW_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CHECK_PATTERN_DOESNT_HANDLE_ALL_CASES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CLOSE_IN_FINALLY_MISSING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CODE_AFTER_ABORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPARISON_INCOMPATIBLE_TYPES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPARISON_OF_IDENTICAL_EXPRESSIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONDITION_ALWAYS_EVALUATES_SAME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONDITION_ALWAYS_FALSE_CONSTANT_PROPAGATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONDITION_ALWAYS_FALSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONDITION_ALWAYS_TRUE_CONSTANT_PROPAGATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DATEOBJECT_VALIDATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEAD_AFTER_RETURN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DIVISION_BY_ZERO_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DYNAMIC_MEMORY_LEAK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ELSE_BRANCH_NEVER_TAKEN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_LIST_INDEXING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EVALUATE_IN_HELD_CONTEXT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EVALUATION_ORDER_ASSUMPTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EXCEPTION_NEVER_THROWN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FILE_HANDLE_LEAK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FLAT_ATTRIBUTE_MISUSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FLOATING_POINT_EQUALITY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FORWARD_REFERENCE_WITHOUT_DECLARATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FUNCTION_RETURNS_WRONG_TYPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GRAPH_OPERATION_ON_NON_GRAPH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HOLD_FIRST_BUT_USES_SECOND_ARGUMENT_FIRST_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IMAGE_OPERATION_ON_NON_IMAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IMPOSSIBLE_PATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCOMPLETE_PATTERN_MATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCORRECT_ASSOCIATION_OPERATIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCORRECT_CLOSURE_CAPTURE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCORRECT_LEVEL_SPECIFICATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCORRECT_SET_IN_SCOPING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INFINITE_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INFINITE_LOOP_PROVEN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INFINITE_RECURSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INTEGER_DIVISION_EXPECTING_REAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LIST_FUNCTION_ON_ASSOCIATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LIST_INDEX_OUT_OF_BOUNDS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LOOP_NEVER_EXECUTES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISMATCHED_BEGIN_END_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISMATCHED_DIMENSIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_CHECK_LEADS_TO_NULL_PROPAGATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_EMPTY_LIST_CHECK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_FAILED_CHECK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_HOLD_ATTRIBUTES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_KEY_CHECK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_MATRIX_DIMENSION_CHECK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_NULL_CHECK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MULTIPLE_RETURNS_MAKE_CODE_UNREACHABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MUTATION_IN_PURE_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NEGATIVE_INDEX_WITHOUT_VALIDATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NOTEBOOK_INIT_CELL_MISUSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NULL_ASSIGNMENT_TO_TYPED_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NULL_DEREFERENCE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NULL_PASSED_TO_NON_NULLABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OFF_BY_ONE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ONE_IDENTITY_ATTRIBUTE_MISUSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OVERWRITTEN_BEFORE_READ_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_VERSION_MISMATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PARALLEL_RACE_CONDITION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PART_SPECIFICATION_OUT_OF_BOUNDS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_BLANKS_MISUSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_TEST_VS_CONDITION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_TYPE_MISMATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_WITH_SIDE_EFFECT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.POSITION_VS_SELECT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.QUANTITY_UNIT_MISMATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.READING_UNSET_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.RELEASE_HOLD_AFTER_HOLD_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPLACE_ALL_VS_REPLACE_CONFUSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.RULE_DOESNT_MATCH_DUE_TO_EVALUATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SEQUENCE_IN_UNEXPECTED_CONTEXT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SET_DELAYED_CONFUSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SIDE_EFFECT_IN_EXPRESSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SOUND_OPERATION_ON_NON_SOUND_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SPAN_SPECIFICATION_INVALID_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.STREAM_NOT_CLOSED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.STREAM_REOPEN_ATTEMPT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SUSPICIOUS_PATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOLIC_VS_NUMERIC_MISMATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_MASKED_BY_IMPORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.THREADING_OVER_NON_LISTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.THROW_WITHOUT_CATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TYPE_MISMATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNCLOSED_FILE_HANDLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNINITIALIZED_VARIABLE_USE_ENHANCED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNREACHABLE_AFTER_ABORT_THROW_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNREACHABLE_BRANCH_ALWAYS_FALSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNREACHABLE_BRANCH_ALWAYS_TRUE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNREACHABLE_CODE_AFTER_RETURN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNREACHABLE_PATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.USED_BEFORE_ASSIGNMENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_IN_WRONG_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_MAY_BE_UNINITIALIZED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VERBATIM_PATTERN_MISUSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.WRONG_ARGUMENT_TYPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.WRONG_CAPITALIZATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ZERO_DENOMINATOR_KEY);
    }

    /**
     * Performance Optimization and Efficiency (40 rules)
     * Covers compilation, memoization, packed arrays, and algorithmic efficiency.
     */
    private void definePerformanceRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ALL_SPECIFICATION_INEFFICIENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.APPEND_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPILABLE_FUNCTION_NOT_COMPILED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPILATION_TARGET_MISSING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DELETEDUPS_ON_LARGE_DATA_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DYNAMIC_HEAVY_COMPUTATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GRAPHICS_OPTIONS_EXCESSIVE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INEFFICIENT_KEY_LOOKUP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INEFFICIENT_LIST_CONCATENATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.INEFFICIENT_PATTERN_IN_PERFORMANCE_CRITICAL_CODE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INEFFICIENT_STRING_CONCATENATION_ENHANCED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LARGE_DATA_IN_NOTEBOOK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LARGE_TEMP_EXPRESSIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LENGTH_IN_LOOP_CONDITION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LINEAR_SEARCH_INSTEAD_LOOKUP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LIST_QUERY_INEFFICIENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LOOP_BOUND_CONSTANT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MACHINE_PRECISION_IN_SYMBOLIC_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MANIPULATE_PERFORMANCE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_COMPILATION_TARGET_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_MEMOIZATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_MEMOIZATION_OPPORTUNITY_ENHANCED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PARALLELIZATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_SPARSE_ARRAY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTED_MAP_TABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NON_COMPILABLE_CONSTRUCT_IN_COMPILE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.N_APPLIED_TOO_LATE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKED_ARRAY_BREAKING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKED_ARRAY_UNPACKED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PARALLEL_NO_GAIN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_MATCHING_LARGE_LISTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PLOT_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PURE_EXPRESSION_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPEATED_CALCULATIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPEATED_FUNCTION_CALLS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPEATED_PART_EXTRACTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPEATED_STRING_PARSING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.STRING_CONCAT_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNCOMPILED_NUMERICAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNPACKING_PACKED_ARRAYS_KEY);
    }

    /**
     * Mathematica-Specific Language Patterns and Idioms (40 rules)
     * Covers attributes, associations, datasets, patterns, and Mathematica-specific constructs.
     */
    private void defineMathematicaPatternRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSOCIATION_KEY_NOT_STRING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSOCIATION_UPDATE_PATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BLANKSEQUENCE_WITHOUT_RESTRICTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BOOLEAN_COMPARISON_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONSTANT_EXPRESSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DATASET_OPERATION_ON_LIST_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DATASET_WITHOUT_HEADERS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DE_MORGANS_LAW_OPPORTUNITY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DOUBLE_NEGATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DYNAMIC_NO_TRACKING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FLATTEN_TABLE_ANTIPATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GROUPBY_WITHOUT_AGGREGATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HOLDPATTERN_UNNECESSARY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HOLD_ATTRIBUTE_MISSING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IDENTITY_OPERATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.KEYDROP_MULTIPLE_TIMES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LOOKUP_WITH_MISSING_DEFAULT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MERGE_WITHOUT_CONFLICT_STRATEGY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_OPTIONS_PATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PATTERN_DEFAULTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_UNEVALUATED_WRAPPER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NEGATED_BOOLEAN_COMPARISON_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTED_LISTS_INSTEAD_ASSOCIATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ORDERLESS_ATTRIBUTE_ON_NON_COMMUTATIVE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PARENTHESES_UNNECESSARY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PART_ASSIGNMENT_TO_IMMUTABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_DEFINITION_SHADOWED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_REPEATED_DIFFERENT_TYPES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.POSITION_INSTEAD_PATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.QUERY_ON_NON_DATASET_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPEATED_PATTERN_ALTERNATIVES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REVERSE_TWICE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SORT_WITHOUT_COMPARISON_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TOTAL_MEAN_ON_NON_NUMERIC_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNNECESSARY_BOOLEAN_CONVERSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNNECESSARY_FLATTEN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNNECESSARY_HOLD_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNNECESSARY_TRANSPOSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNPROTECTED_SYMBOLS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNRESTRICTED_BLANK_PATTERN_KEY);
    }

    /**
     * Code Smells, Complexity, and Quality Issues (96 rules total)
     * Covers code duplication, magic values, empty blocks, complexity metrics, and quality issues.
     */
    private void defineCodeSmellsAndComplexityRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ABORT_IN_LIBRARY_CODE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ALTERNATIVES_TOO_COMPLEX_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ANALYSIS_TIMEOUT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSIGNMENT_AS_RETURN_VALUE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSOCIATETO_ON_NON_SYMBOL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BOOLEAN_EXPRESSION_TOO_COMPLEX_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CATCH_ALL_EXCEPTION_HANDLER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CHAINED_CALLS_TOO_LONG_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPARISON_WITH_NULL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPLEX_BOOLEAN_EXPRESSION_ENHANCED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPLEX_BOOLEAN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONDITIONAL_COMPLEXITY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEBUG_CODE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEEPLY_NESTED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DOUBLE_ASSIGNMENT_SAME_VALUE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DUPLICATE_CODE_BLOCK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DUPLICATE_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DUPLICATE_STRING_LITERAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_BLOCK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_CATCH_BLOCK_ENHANCED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_CATCH_BLOCK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_CATCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_EXCEPTION_HANDLER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_IF_BRANCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EMPTY_STATEMENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EQUALITY_CHECK_ON_REALS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EXCESSIVE_PURE_FUNCTIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FILE_EXCEEDS_ANALYSIS_LIMIT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FILE_LENGTH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FILE_TOO_MANY_FUNCTIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FIXME_TRACKING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FUNCTION_LENGTH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FUNCTION_WITHOUT_RETURN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GOD_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GOD_PACKAGE_TOO_MANY_DEPENDENCIES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GROWING_DEFINITION_CHAIN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HACK_COMMENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HARDCODED_FILE_PATHS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HARDCODED_PATH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HARDCODED_URL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IDENTICAL_BRANCHES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IDENTICAL_IF_BRANCHES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IMPLICIT_TYPE_CONVERSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_NULL_HANDLING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_RETURN_TYPES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_RULE_TYPES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INSECURE_WEBSOCKET_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INSUFFICIENT_KEY_SIZE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LARGE_COMMENTED_BLOCK_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LIST_CONCATENATION_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MAGIC_NUMBER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MAGIC_STRING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MANIPULATE_TOO_COMPLEX_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_ATTRIBUTES_DECLARATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_DEFAULT_CASE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_ELSE_CONSIDERED_HARMFUL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_FORMFUNCTION_VALIDATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_FUNCTION_ATTRIBUTES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_OPERATOR_PRECEDENCE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_OPTION_DEFAULT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_RETURN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_SEQUENCE_WRAPPER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_SPECIAL_CASE_HANDLING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MIXED_NUMERIC_TYPES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTED_BRACKETS_EXCESSIVE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTED_IF_DEPTH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTED_OPTIONAL_PATTERNS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTED_PART_EXTRACTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTING_TOO_DEEP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NOTEBOOK_NO_SECTIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NOTEBOOK_UNORGANIZED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NUMERIC_OPERATION_ON_STRING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OPTIONAL_TYPE_INCONSISTENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ORDER_DEPENDENT_PATTERNS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OVERCOMPLEX_PATTERNS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OVER_ABSTRACTED_API_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_TOO_MANY_EXPORTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PARALLEL_SHARED_STATE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REGEX_DOS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPLACEMENT_RULE_ORDER_MATTERS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.RETURN_TYPE_INCONSISTENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.STRINGJOIN_FOR_TEMPLATES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.STRING_CONCATENATION_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.STRING_OPERATION_ON_NUMBER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SWITCH_CASE_SHADOWED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SWITCH_TOO_MANY_CASES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TODO_FIXME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TODO_TRACKING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TOO_MANY_PARAMETERS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TOO_MANY_RETURN_POINTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TOO_MANY_VARIABLES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TYPE_CAST_WITHOUT_VALIDATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TYPE_INCONSISTENCY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNDEFINED_FUNCTION_CALL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNDEFINED_VARIABLE_REFERENCE_KEY);
    }

    /**
     * Naming Conventions, Code Style, and Documentation (64 rules)
     * Covers naming standards, documentation requirements, and code formatting.
     */
    private void defineNamingAndDocumentationRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ABBREVIATION_UNCLEAR_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ACRONYM_STYLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ALIGNMENT_INCONSISTENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.API_MISSING_DOCUMENTATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BOOLEAN_NAME_NON_DESCRIPTIVE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BRACE_STYLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BRACKET_SPACING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMMA_SPACING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONSTANT_NOT_UPPERCASE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DOCUMENTATION_OUTDATED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DOCUMENTATION_TOO_SHORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FILE_ENDS_WITHOUT_NEWLINE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FUNCTION_NAME_TOO_LONG_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FUNCTION_NAME_TOO_SHORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GENERIC_NAME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GENERIC_VARIABLE_NAMES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.HUNGARIAN_NOTATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCOMPLETE_PUBLIC_API_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_INDENTATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_NAMING_CONVENTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_NAMING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_NAMING_STYLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_PACKAGE_NAMING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCONSISTENT_PARAMETER_NAMES_ACROSS_OVERLOADS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LINE_TOO_LONG_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LONG_STRING_LITERAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MESSAGE_WITHOUT_DEFINITION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_BLANK_LINE_AFTER_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_DOCUMENTATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_DOWNVALUES_DOC_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_ERROR_MESSAGES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_LOCALIZATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_MESSAGE_DEFINITION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PACKAGE_DOCUMENTATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_USAGE_MESSAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MULTIPLE_BLANK_LINES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NAMING_CONVENTION_VIOLATIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NEGATED_BOOLEAN_NAME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NOTEBOOK_CELL_SIZE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NULL_RETURN_NOT_DOCUMENTED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NUMBER_IN_NAME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OPERATOR_SPACING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OPTION_NAME_UNCLEAR_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_NAME_CASE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PARAMETER_NAME_SAME_AS_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PARAMETER_NOT_DOCUMENTED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_NAMING_CONFLICTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PLOT_WITHOUT_LABELS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PUBLIC_EXPORT_MISSING_USAGE_MESSAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.PUBLIC_FUNCTION_WITH_IMPLEMENTATION_DETAILS_IN_NAME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.RESERVED_NAME_USAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.RETURN_NOT_DOCUMENTED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SEMICOLON_STYLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SIDE_EFFECTS_NAMING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_NAME_COLLISION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_NAME_TOO_LONG_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_NAME_TOO_SHORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEMP_VARIABLE_NOT_TEMP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_NAMING_CONVENTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TRAILING_WHITESPACE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TYPO_IN_BUILTIN_NAME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_PATTERN_NAME_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_NAME_MATCHES_BUILTIN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_NAME_TOO_SHORT_KEY);
    }

    /**
     * Unused and Dead Code Detection (30 rules)
     * Covers unused variables, parameters, functions, imports, and commented code.
     */
    private void defineUnusedCodeRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSIGNED_BUT_NEVER_READ_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMMENTED_CODE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMMENTED_OUT_CODE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMMENTED_OUT_PACKAGE_LOAD_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEAD_PACKAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEAD_STORE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEPRECATED_API_STILL_USED_INTERNALLY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEPRECATED_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEPRECATED_OPTION_USAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FUNCTION_DEFINED_NEVER_CALLED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FUNCTION_ONLY_CALLED_ONCE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LOOP_VARIABLE_UNUSED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REDEFINED_WITHOUT_USE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REDUNDANT_ASSIGNMENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REDUNDANT_COMPUTATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REDUNDANT_CONDITIONAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_EXPORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_FUNCTION_PARAMETER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_IMPORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_MODULE_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_OPTIONAL_PARAMETER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_PACKAGE_IMPORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_PARAMETER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_PRIVATE_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_PUBLIC_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_VARIABLES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_WITH_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_NEVER_MODIFIED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.WRITE_ONLY_VARIABLE_KEY);
    }

    /**
     * Variable Scope and Lifetime Management (24 rules)
     * Covers scoping issues, closures, globals, shadowing, and variable lifetime.
     */
    private void defineVariableAndScopeRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CIRCULAR_VARIABLE_DEPENDENCIES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CLOSURE_OVER_MUTABLE_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONSTANT_NOT_MARKED_AS_CONSTANT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.EXPLICIT_GLOBAL_CONTEXT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GLOBAL_IN_PACKAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GLOBAL_STATE_MODIFICATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GLOBAL_VARIABLE_POLLUTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LIFETIME_EXTENDS_BEYOND_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LOCAL_SHADOWS_GLOBAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LOCAL_SHADOWS_PARAMETER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_TEMPORARY_CLEANUP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MODIFICATION_OF_LOOP_ITERATOR_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MODIFIED_IN_UNEXPECTED_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NO_CLEAR_AFTER_USE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PARAMETER_SHADOWS_BUILTIN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SCOPE_LEAK_THROUGH_DYNAMIC_EVALUATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SHARED_MUTABLE_STATE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.USE_OF_ITERATOR_OUTSIDE_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_ALIASING_ISSUE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_BEFORE_ASSIGNMENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_ESCAPES_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_REUSE_WITH_DIFFERENT_SEMANTICS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_SCOPE_ESCAPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_SHADOWING_KEY);
    }

    /**
     * Package Structure, Dependencies, and Module Organization (39 rules)
     * Covers package dependencies, circular imports, context management, and module structure.
     */
    private void definePackageStructureRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CIRCULAR_NEEDS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CIRCULAR_PACKAGE_DEPENDENCY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONDITIONAL_PACKAGE_LOAD_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONTEXT_CONFLICTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONTEXT_NOT_FOUND_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CYCLIC_CALL_BETWEEN_PACKAGES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DIAMOND_DEPENDENCY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DUPLICATE_SYMBOL_DEFINITION_ACROSS_PACKAGES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.FEATURE_ENVY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GRAPHICS_OBJECT_IN_NUMERIC_CONTEXT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INTERNAL_API_USED_LIKE_PUBLIC_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INTERNAL_IMPLEMENTATION_EXPOSED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LAYER_VIOLATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_IMPORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PACKAGE_IMPORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PATH_ENTRY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MULTIPLE_DEFINITIONS_SAME_SYMBOL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.OFF_DISABLING_IMPORTANT_WARNINGS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_CIRCULAR_DEPENDENCY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_DEPENDS_ON_APPLICATION_CODE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_EXPORTS_TOO_LITTLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_EXPORTS_TOO_MUCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_LOADED_BUT_NOT_LISTED_IN_METADATA_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_NO_BEGIN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_NO_USAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_PUBLIC_PRIVATE_MIX_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_TOO_LARGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKAGE_TOO_SMALL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PRIVATE_CONTEXT_SYMBOL_PUBLIC_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PRIVATE_SYMBOL_USED_EXTERNALLY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PUBLIC_API_CHANGED_WITHOUT_VERSION_BUMP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PUBLIC_API_NOT_IN_PACKAGE_CONTEXT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.QUIET_SUPPRESSING_IMPORTANT_MESSAGES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_AFTER_ENDPACKAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_IN_NUMERIC_CONTEXT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_REDEFINITION_AFTER_IMPORT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TRANSITIVE_DEPENDENCY_COULD_BE_DIRECT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNSTABLE_DEPENDENCY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VULNERABLE_DEPENDENCY_KEY);
    }

    /**
     * Testing Best Practices and Test Quality (22 rules)
     * Covers test coverage, test structure, test data, and verification tests.
     */
    private void defineTestingRules(NewBuiltInQualityProfile profile) {
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IMPLEMENTATION_WITHOUT_TESTS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LONGEST_SHORTEST_WITHOUT_ORDERING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LOW_TEST_COVERAGE_WARNING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PATTERN_TEST_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PATTERN_TEST_VALIDATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ORPHANED_TEST_FILE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_TEST_PURE_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_ASSERT_COUNT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_DATA_HARDCODED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_FUNCTION_IN_PRODUCTION_CODE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_IGNORED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_MAGIC_NUMBER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_MULTIPLE_CONCERNS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_NO_ISOLATION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_ONLY_CODE_IN_PRODUCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TEST_TOO_LONG_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNTESTED_BRANCH_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNTESTED_PUBLIC_FUNCTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VERIFICATION_TEST_EMPTY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VERIFICATION_TEST_NO_DESCRIPTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VERIFICATION_TEST_NO_EXPECTED_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VERIFICATION_TEST_TOO_BROAD_KEY);
    }

    /**
     * Security Vulnerabilities, Security Hotspots, and SCA (67 rules)
     * Covers injection attacks, cryptography, authentication, data exposure, and vulnerable dependencies.
     * This method is preserved from the original implementation.
     */
    private void defineSecurityAndScaRules(NewBuiltInQualityProfile profile) {
        // Activate NEW SECURITY_HOTSPOT rules (Phase 2)
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.NETWORK_OPERATIONS_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.FILE_SYSTEM_MODIFICATIONS_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.ENVIRONMENT_VARIABLE_KEY
        );

        // Activate PHASE 3 RULES (25 rules)

        // Performance rules (8 rules - CODE_SMELL)
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.APPEND_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REPEATED_FUNCTION_CALLS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.STRING_CONCAT_IN_LOOP_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNCOMPILED_NUMERICAL_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PACKED_ARRAY_BREAKING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NESTED_MAP_TABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LARGE_TEMP_EXPRESSIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PLOT_IN_LOOP_KEY);

        // Pattern matching rules (5 rules - 4 BUG, 1 CODE_SMELL)
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_PATTERN_TEST_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.PATTERN_BLANKS_MISUSE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SET_DELAYED_CONFUSION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SYMBOL_NAME_COLLISION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.BLOCK_MODULE_MISUSE_KEY);

        // Best practices rules (7 rules - CODE_SMELL)
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GENERIC_VARIABLE_NAMES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_USAGE_MESSAGE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_OPTIONS_PATTERN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SIDE_EFFECTS_NAMING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.COMPLEX_BOOLEAN_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNPROTECTED_SYMBOLS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MISSING_RETURN_KEY);

        // Security & Safety rules (2 VULNERABILITY, 1 SECURITY_HOTSPOT)
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNSAFE_CLOUD_DEPLOY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DYNAMIC_INJECTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.IMPORT_WITHOUT_FORMAT_KEY);

        // Resource management rules (2 BUG)
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNCLOSED_FILE_HANDLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GROWING_DEFINITION_CHAIN_KEY);

        // Symbol Table Analysis Rules (10 rules - 3 BUG, 6 CODE_SMELL, 1 CRITICAL BUG)
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.ASSIGNED_BUT_NEVER_READ_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.DEAD_STORE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.USED_BEFORE_ASSIGNMENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_SHADOWING_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.UNUSED_PARAMETER_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.WRITE_ONLY_VARIABLE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.REDUNDANT_ASSIGNMENT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_IN_WRONG_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_ESCAPES_SCOPE_KEY);

        // Advanced Symbol Table Analysis Rules (10 rules - 3 BUG, 6 CODE_SMELL, 1 CRITICAL BUG)
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.LIFETIME_EXTENDS_BEYOND_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.MODIFIED_IN_UNEXPECTED_SCOPE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.GLOBAL_VARIABLE_POLLUTION_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CIRCULAR_VARIABLE_DEPENDENCIES_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.NAMING_CONVENTION_VIOLATIONS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.CONSTANT_NOT_MARKED_AS_CONSTANT_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.TYPE_INCONSISTENCY_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VARIABLE_REUSE_WITH_DIFFERENT_SEMANTICS_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.INCORRECT_CLOSURE_CAPTURE_KEY);
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.SCOPE_LEAK_THROUGH_DYNAMIC_EVALUATION_KEY);

        // SCA - Software Composition Analysis
        profile.activateRule(MathematicaRulesDefinition.REPOSITORY_KEY, MathematicaRulesDefinition.VULNERABLE_DEPENDENCY_KEY);
    }
}
