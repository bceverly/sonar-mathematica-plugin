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

        profile.done();
    }
}
