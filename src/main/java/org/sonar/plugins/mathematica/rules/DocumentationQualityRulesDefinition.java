package org.sonar.plugins.mathematica.rules;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.Severity;

/**
 * Documentation Quality Rules for comments and code documentation.
 *
 * <p>This file contains 10 comment quality code smell rules:
 * - 3 Comment Tracking rules (task markers and fix-me/hack style comments)
 * - 2 Commented Code rules
 * - 5 Documentation rules
 *
 * <p><strong>IMPORTANT:</strong> Detection logic is NOT implemented for these rules.
 * Rules are fully defined and documented in SonarQube for manual review.
 * Detection patterns can be added incrementally as needed.
 */
@SuppressWarnings("java:S1135") // This file defines rules for detecting task markers, not actual TODOs
public final class DocumentationQualityRulesDefinition {

    private static final String TECHNICAL_DEBT = "technical-debt";
    private static final String COMMENTS = "comments";
    private static final String DOCUMENTATION = "documentation";

    // Comment Tracking Rule Keys (3 rules)
    private static final String TODO_TRACKING_KEY = "TodoTracking";
    private static final String FIXME_TRACKING_KEY = "FixmeTracking";
    private static final String HACK_COMMENT_KEY = "HackComment";

    // Commented Code Rule Keys (2 rules)
    private static final String COMMENTED_OUT_CODE_KEY = "CommentedOutCode";
    private static final String LARGE_COMMENTED_BLOCK_KEY = "LargeCommentedBlock";

    // Documentation Rule Keys (5 rules)
    private static final String API_MISSING_DOCUMENTATION_KEY = "ApiMissingDocumentation";
    private static final String DOCUMENTATION_TOO_SHORT_KEY = "DocumentationTooShort";
    private static final String DOCUMENTATION_OUTDATED_KEY = "DocumentationOutdated";
    private static final String PARAMETER_NOT_DOCUMENTED_KEY = "ParameterNotDocumented";
    private static final String RETURN_NOT_DOCUMENTED_KEY = "ReturnNotDocumented";

    // Private constructor for utility class
    private DocumentationQualityRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all documentation quality rules in the repository.
     */
    public static void define(NewRepository repository) {
        defineCommentTrackingRules(repository);
        defineCommentedCodeRules(repository);
        defineDocumentationRules(repository);
    }

    private static void defineCommentTrackingRules(NewRepository repository) {
        repository.createRule(TODO_TRACKING_KEY)
            .setName("TODO comments should be tracked")
            .setHtmlDescription("<p>TODO comments indicate incomplete work. Track them in issue management system or complete them.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(COMMENTS, TECHNICAL_DEBT)
            .setStatus(RuleStatus.READY);

        repository.createRule(FIXME_TRACKING_KEY)
            .setName("FIXME comments should be tracked")
            .setHtmlDescription("<p>FIXME comments indicate known issues. Track them and resolve promptly.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(COMMENTS, TECHNICAL_DEBT)
            .setStatus(RuleStatus.READY);

        repository.createRule(HACK_COMMENT_KEY)
            .setName("HACK comments indicate technical debt")
            .setHtmlDescription("<p>HACK comments suggest workarounds that should be refactored properly.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(COMMENTS, TECHNICAL_DEBT)
            .setStatus(RuleStatus.READY);
    }

    private static void defineCommentedCodeRules(NewRepository repository) {
        repository.createRule(COMMENTED_OUT_CODE_KEY)
            .setName("Commented-out code should be removed")
            .setHtmlDescription("<p>Commented-out code clutters the codebase. Use version control instead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(COMMENTS, "dead-code")
            .setStatus(RuleStatus.READY);

        repository.createRule(LARGE_COMMENTED_BLOCK_KEY)
            .setName("Large blocks of commented code should be removed")
            .setHtmlDescription("<p>More than 10 lines of commented code suggests dead code. Remove it or use version control.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(COMMENTS, "dead-code")
            .setStatus(RuleStatus.READY);
    }

    private static void defineDocumentationRules(NewRepository repository) {
        repository.createRule(API_MISSING_DOCUMENTATION_KEY)
            .setName("Public functions should be documented")
            .setHtmlDescription("<p>Functions starting with capital letters (public API) should have usage messages "
                + "or comments explaining their purpose.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(DOCUMENTATION, "api")
            .setStatus(RuleStatus.READY);

        repository.createRule(DOCUMENTATION_TOO_SHORT_KEY)
            .setName("Documentation should be adequately detailed")
            .setHtmlDescription("<p>One-line documentation for complex functions is insufficient. "
                + "Explain parameters, return values, and examples.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(DOCUMENTATION)
            .setStatus(RuleStatus.READY);

        repository.createRule(DOCUMENTATION_OUTDATED_KEY)
            .setName("Documentation should be kept up to date")
            .setHtmlDescription("<p>Documentation referencing old behavior or parameters should be updated when code changes.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(DOCUMENTATION)
            .setStatus(RuleStatus.READY);

        repository.createRule(PARAMETER_NOT_DOCUMENTED_KEY)
            .setName("Function parameters should be documented")
            .setHtmlDescription("<p>Complex functions should document what each parameter does and what values are expected.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(DOCUMENTATION, "parameters")
            .setStatus(RuleStatus.READY);

        repository.createRule(RETURN_NOT_DOCUMENTED_KEY)
            .setName("Return values should be documented")
            .setHtmlDescription("<p>Functions with non-obvious return values should document what they return and in what format.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(DOCUMENTATION, "return-value")
            .setStatus(RuleStatus.READY);
    }
}
