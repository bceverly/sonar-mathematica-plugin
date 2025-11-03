package org.sonar.plugins.mathematica.rules;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.Severity;

/**
 * Testing Quality Rules for test organization and VerificationTest patterns.
 *
 * <p>This file contains 12 testing quality code smell rules:
 * - 4 Test Organization rules
 * - 4 VerificationTest Pattern rules
 * - 4 Test Quality rules
 *
 * <p><strong>IMPORTANT:</strong> Detection logic is NOT implemented for these rules.
 * Rules are fully defined and documented in SonarQube for manual review.
 * Detection patterns can be added incrementally as needed.
 */
public final class TestingQualityRulesDefinition {

    private static final String TAG_RELIABILITY = "reliability";

    // Test Organization Rule Keys (4 rules)
    private static final String TEST_NAMING_CONVENTION_KEY = "TestNamingConvention";
    private static final String TEST_NO_ISOLATION_KEY = "TestNoIsolation";
    private static final String TEST_DATA_HARDCODED_KEY = "TestDataHardcoded";
    private static final String TEST_IGNORED_KEY = "TestIgnored";

    // VerificationTest Pattern Rule Keys (4 rules)
    private static final String VERIFICATION_TEST_NO_EXPECTED_KEY = "VerificationTestNoExpected";
    private static final String VERIFICATION_TEST_TOO_BROAD_KEY = "VerificationTestTooBroad";
    private static final String VERIFICATION_TEST_NO_DESCRIPTION_KEY = "VerificationTestNoDescription";
    private static final String VERIFICATION_TEST_EMPTY_KEY = "VerificationTestEmpty";

    // Test Quality Rule Keys (4 rules)
    private static final String TEST_ASSERT_COUNT_KEY = "TestAssertCount";
    private static final String TEST_TOO_LONG_KEY = "TestTooLong";
    private static final String TEST_MULTIPLE_CONCERNS_KEY = "TestMultipleConcerns";
    private static final String TEST_MAGIC_NUMBER_KEY = "TestMagicNumber";

    // Private constructor for utility class
    private TestingQualityRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all testing quality rules in the repository.
     */
    public static void define(NewRepository repository) {
        defineTestOrganizationRules(repository);
        defineVerificationTestPatternRules(repository);
        defineTestQualityRules(repository);
    }

    private static void defineTestOrganizationRules(NewRepository repository) {
        repository.createRule(TEST_NAMING_CONVENTION_KEY)
            .setName("Test functions should follow naming conventions")
            .setHtmlDescription("<p>Test function names should clearly indicate what is being tested. "
                + "Use descriptive names like 'testFunctionNameWithCondition'.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "naming")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_NO_ISOLATION_KEY)
            .setName("Tests should be isolated from each other")
            .setHtmlDescription("<p>Tests should not depend on execution order or shared state. Each test should set up its own data.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("tests", "isolation")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_DATA_HARDCODED_KEY)
            .setName("Test data should be clearly defined")
            .setHtmlDescription("<p>Magic numbers and hardcoded strings in tests make them fragile. Use named constants or test data functions.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "test-data")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_IGNORED_KEY)
            .setName("Ignored or skipped tests should be investigated")
            .setHtmlDescription("<p>Commented-out or conditionally skipped tests indicate incomplete work. Fix or remove them.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "technical-debt")
            .setStatus(RuleStatus.READY);
    }

    private static void defineVerificationTestPatternRules(NewRepository repository) {
        repository.createRule(VERIFICATION_TEST_NO_EXPECTED_KEY)
            .setName("VerificationTest should specify expected output")
            .setHtmlDescription("<p>VerificationTest without ExpectedOutput only checks for errors, not correctness.</p>"
                + "<h2>Noncompliant Code</h2><pre>VerificationTest[myFunction[x]]</pre>"
                + "<h2>Compliant Solution</h2><pre>VerificationTest[myFunction[5], 25]</pre>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("tests", "verification-test")
            .setStatus(RuleStatus.READY);

        repository.createRule(VERIFICATION_TEST_TOO_BROAD_KEY)
            .setName("VerificationTest tolerance should not be too broad")
            .setHtmlDescription("<p>Overly generous SameTest tolerances may pass incorrect results.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("tests", "verification-test", "precision")
            .setStatus(RuleStatus.READY);

        repository.createRule(VERIFICATION_TEST_NO_DESCRIPTION_KEY)
            .setName("VerificationTest should have descriptive TestID")
            .setHtmlDescription("<p>TestID helps identify failing tests. Use descriptive names.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "verification-test", "documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(VERIFICATION_TEST_EMPTY_KEY)
            .setName("Empty VerificationTest provides no value")
            .setHtmlDescription("<p>VerificationTest with no assertions or expected output should be removed or completed.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "verification-test", "dead-code")
            .setStatus(RuleStatus.READY);
    }

    private static void defineTestQualityRules(NewRepository repository) {
        repository.createRule(TEST_ASSERT_COUNT_KEY)
            .setName("Tests should have sufficient assertions")
            .setHtmlDescription("<p>Tests with zero or one assertion may not adequately validate behavior. Add more specific checks.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("tests", "assertions")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_TOO_LONG_KEY)
            .setName("Test functions should not be too long")
            .setHtmlDescription("<p>Tests longer than 50 lines are hard to understand. Break into smaller, focused tests.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "complexity")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_MULTIPLE_CONCERNS_KEY)
            .setName("Each test should verify one concern")
            .setHtmlDescription("<p>Tests that verify multiple unrelated behaviors should be split into separate tests.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "single-responsibility")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_MAGIC_NUMBER_KEY)
            .setName("Tests should not use unexplained magic numbers")
            .setHtmlDescription("<p>Magic numbers in tests make them hard to understand. Use named constants or comments explaining the values.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "magic-numbers")
            .setStatus(RuleStatus.READY);
    }
}
