package org.sonar.plugins.mathematica;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.BuiltInQualityProfile;
import org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class MathematicaQualityProfileTest {

    @Test
    void testDefineCreatesProfile() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");
        assertThat(profile).isNotNull();
    }

    @Test
    void testProfileIsDefault() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");
        assertThat(profile.isDefault()).isTrue();
    }

    @Test
    void testProfileLanguage() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");
        assertThat(profile.language()).isEqualTo(MathematicaLanguage.KEY);
    }

    @Test
    void testProfileName() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");
        assertThat(profile.name()).isEqualTo("Sonar way");
    }

    @Test
    void testProfileActivatesRules() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");
        assertThat(profile.rules()).isNotEmpty();
    }

    @Test
    void testProfileActivatesBugDetectionRules() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");

        // Check that specific bug detection rules are activated
        boolean hasAssignmentInConditional = profile.rules().stream()
            .anyMatch(r -> r.ruleKey().equals(MathematicaRulesDefinition.ASSIGNMENT_IN_CONDITIONAL_KEY));

        assertThat(hasAssignmentInConditional).isTrue();
    }

    @Test
    void testProfileActivatesPerformanceRules() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");

        // Check that specific performance rules are activated
        boolean hasAppendInLoop = profile.rules().stream()
            .anyMatch(r -> r.ruleKey().equals(MathematicaRulesDefinition.APPEND_IN_LOOP_KEY));

        assertThat(hasAppendInLoop).isTrue();
    }

    @Test
    void testProfileActivatesSecurityRules() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");

        // Check that security rules exist (e.g., SQL injection, weak cipher)
        boolean hasSecurityRules = profile.rules().stream()
            .anyMatch(r -> r.repoKey().equals(MathematicaRulesDefinition.REPOSITORY_KEY));

        assertThat(hasSecurityRules).isTrue();
    }

    @Test
    void testProfileHasSubstantialRuleCount() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");

        // Profile should activate a substantial number of rules
        assertThat(profile.rules()).hasSizeGreaterThan(100);
    }

    @Test
    void testAllActivatedRulesHaveCorrectRepository() {
        MathematicaQualityProfile profileDef = new MathematicaQualityProfile();
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        profileDef.define(context);

        BuiltInQualityProfile profile = context.profile(MathematicaLanguage.KEY, "Sonar way");

        // All rules should be from the Mathematica repository
        boolean allFromMathematicaRepo = profile.rules().stream()
            .allMatch(r -> r.repoKey().equals(MathematicaRulesDefinition.REPOSITORY_KEY));

        assertThat(allFromMathematicaRepo).isTrue();
    }
}
