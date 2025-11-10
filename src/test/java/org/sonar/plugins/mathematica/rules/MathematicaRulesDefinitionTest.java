package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for MathematicaRulesDefinition.
 * This validates that all rules are properly registered with required metadata.
 */
class MathematicaRulesDefinitionTest {

    @Test
    void testDefineCreatesRepository() {
        MathematicaRulesDefinition definition = new MathematicaRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();

        definition.define(context);

        RulesDefinition.Repository repository = context.repository(MathematicaRulesDefinition.REPOSITORY_KEY);
        assertThat(repository).isNotNull();
        assertThat(repository.name()).isNotEmpty();
        assertThat(repository.language()).isEqualTo("mathematica");
        assertThat(repository.key()).isEqualTo(MathematicaRulesDefinition.REPOSITORY_KEY);
    }

    @Test
    void testAllRulesAreDefined() {
        MathematicaRulesDefinition definition = new MathematicaRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();

        definition.define(context);

        RulesDefinition.Repository repository = context.repository(MathematicaRulesDefinition.REPOSITORY_KEY);
        assertThat(repository).isNotNull();

        // Verify rules are defined (should have 200+ rules across all categories)
        assertThat(repository.rules()).isNotEmpty().hasSizeGreaterThan(200);
    }

    @Test
    void testAllRulesHaveRequiredMetadata() {
        MathematicaRulesDefinition definition = new MathematicaRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();

        definition.define(context);

        RulesDefinition.Repository repository = context.repository(MathematicaRulesDefinition.REPOSITORY_KEY);
        assertThat(repository).isNotNull();

        // Verify each rule has required metadata
        for (RulesDefinition.Rule rule : repository.rules()) {
            assertThat(rule.key())
                .as("Rule key should not be empty")
                .isNotEmpty();

            assertThat(rule.name())
                .as("Rule name should not be empty for rule %s", rule.key())
                .isNotEmpty();

            assertThat(rule.htmlDescription())
                .as("Rule HTML description should not be empty for rule %s", rule.key())
                .isNotEmpty();

            // Rule should have either new-style impacts or old-style type/severity
            boolean hasImpacts = !rule.defaultImpacts().isEmpty();
            @SuppressWarnings("deprecation")
            boolean hasDeprecatedFields = rule.type() != null;

            assertThat(hasImpacts || hasDeprecatedFields)
                .as("Rule %s should have either impacts or type/severity defined", rule.key())
                .isTrue();
        }
    }

    @Test
    void testSpecificRuleKeysExist() {
        MathematicaRulesDefinition definition = new MathematicaRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();

        definition.define(context);

        RulesDefinition.Repository repository = context.repository(MathematicaRulesDefinition.REPOSITORY_KEY);
        assertThat(repository).isNotNull();

        // Verify some key rules exist
        assertThat(repository.rule(MathematicaRulesDefinition.COMMENTED_CODE_KEY)).isNotNull();
        assertThat(repository.rule(MathematicaRulesDefinition.MAGIC_NUMBER_KEY)).isNotNull();
        assertThat(repository.rule(MathematicaRulesDefinition.HARDCODED_CREDENTIALS_KEY)).isNotNull();
        assertThat(repository.rule(MathematicaRulesDefinition.DIVISION_BY_ZERO_KEY)).isNotNull();
        assertThat(repository.rule(MathematicaRulesDefinition.FILE_UPLOAD_VALIDATION_KEY)).isNotNull();
    }

    @Test
    void testRuleHasValidRemediationCost() {
        MathematicaRulesDefinition definition = new MathematicaRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();

        definition.define(context);

        RulesDefinition.Repository repository = context.repository(MathematicaRulesDefinition.REPOSITORY_KEY);
        assertThat(repository).isNotNull();

        // Check that at least one rule has remediation cost defined
        RulesDefinition.Rule rule = repository.rule(MathematicaRulesDefinition.COMMENTED_CODE_KEY);
        assertThat(rule).isNotNull();
        assertThat(rule.debtRemediationFunction()).isNotNull();
    }

    @Test
    void testSecurityRulesHaveSecurityImpacts() {
        MathematicaRulesDefinition definition = new MathematicaRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();

        definition.define(context);

        RulesDefinition.Repository repository = context.repository(MathematicaRulesDefinition.REPOSITORY_KEY);
        assertThat(repository).isNotNull();

        // Verify security vulnerabilities have security impacts
        RulesDefinition.Rule hardcodedCreds = repository.rule(MathematicaRulesDefinition.HARDCODED_CREDENTIALS_KEY);
        assertThat(hardcodedCreds).isNotNull();
        assertThat(hardcodedCreds.defaultImpacts()).isNotEmpty();
    }

    @Test
    void testRulesHaveTags() {
        MathematicaRulesDefinition definition = new MathematicaRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();

        definition.define(context);

        RulesDefinition.Repository repository = context.repository(MathematicaRulesDefinition.REPOSITORY_KEY);
        assertThat(repository).isNotNull();

        // Check that rules have appropriate tags
        int rulesWithTags = 0;
        for (RulesDefinition.Rule rule : repository.rules()) {
            if (!rule.tags().isEmpty()) {
                rulesWithTags++;
            }
        }

        // Most rules should have at least one tag
        assertThat(rulesWithTags)
            .as("Most rules should have at least one tag")
            .isGreaterThan(repository.rules().size() / 2);
    }
}
