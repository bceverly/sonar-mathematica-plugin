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

        // Activate all rules
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.COMMENTED_CODE_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.MAGIC_NUMBER_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.TODO_FIXME_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.EMPTY_BLOCK_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.FUNCTION_LENGTH_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.FILE_LENGTH_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.EMPTY_CATCH_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.DEBUG_CODE_KEY
        );

        // Activate security rules
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.HARDCODED_CREDENTIALS_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.COMMAND_INJECTION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.SQL_INJECTION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.CODE_INJECTION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.PATH_TRAVERSAL_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.WEAK_CRYPTOGRAPHY_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.SSRF_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.INSECURE_DESERIALIZATION_KEY
        );

        // Activate BUG rules (Reliability)
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.DIVISION_BY_ZERO_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITIONAL_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.LIST_INDEX_OUT_OF_BOUNDS_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.INFINITE_RECURSION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.UNREACHABLE_PATTERN_KEY
        );

        // Activate Security Hotspot rules
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.FILE_UPLOAD_VALIDATION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.EXTERNAL_API_SAFEGUARDS_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.CRYPTO_KEY_GENERATION_KEY
        );

        // Activate NEW CODE_SMELL rules (Phase 2)
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.UNUSED_VARIABLES_KEY
        );

        defineActivations(profile);
    }

    private void defineActivations(NewBuiltInQualityProfile profile) {
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.DUPLICATE_FUNCTION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.TOO_MANY_PARAMETERS_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.DEEPLY_NESTED_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.MISSING_DOCUMENTATION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.INCONSISTENT_NAMING_KEY
        );

        definePerformanceAndPatternRules(profile);
    }

    private void definePerformanceAndPatternRules(NewBuiltInQualityProfile profile) {

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.IDENTICAL_BRANCHES_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.DEPRECATED_FUNCTION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.EMPTY_STATEMENT_KEY
        );

        // Activate NEW BUG rules (Phase 2)
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.FLOATING_POINT_EQUALITY_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.FUNCTION_WITHOUT_RETURN_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.VARIABLE_BEFORE_ASSIGNMENT_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.OFF_BY_ONE_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.INFINITE_LOOP_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.MISMATCHED_DIMENSIONS_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.TYPE_MISMATCH_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.SUSPICIOUS_PATTERN_KEY
        );

        // Activate NEW VULNERABILITY rules (Phase 2)
        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.UNSAFE_SYMBOL_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.XXE_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.MISSING_SANITIZATION_KEY
        );

        profile.activateRule(
            MathematicaRulesDefinition.REPOSITORY_KEY,
            MathematicaRulesDefinition.INSECURE_RANDOM_EXPANDED_KEY
        );

        defineAdvancedRules(profile);
    }

    private void defineAdvancedRules(NewBuiltInQualityProfile profile) {

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

        profile.done();
    }
}
