package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.issue.impact.Severity;

/**
 * Defines custom rule templates that users can instantiate and customize
 * through the SonarQube UI without modifying the plugin.
 */
public final class CustomRuleTemplatesDefinition {

    private static final String CUSTOM = "custom";

    private CustomRuleTemplatesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Template rule keys
    public static final String PATTERN_MATCH_TEMPLATE_KEY = "custom-pattern-match";
    public static final String FUNCTION_NAME_PATTERN_TEMPLATE_KEY = "custom-function-name-pattern";
    public static final String FORBIDDEN_API_TEMPLATE_KEY = "custom-forbidden-api";

    // Parameter keys
    public static final String PATTERN_PARAM = "pattern";
    public static final String MESSAGE_PARAM = "message";
    public static final String FUNCTION_NAME_PATTERN_PARAM = "functionNamePattern";
    public static final String API_NAME_PARAM = "apiName";
    public static final String REASON_PARAM = "reason";

    /**
     * Define all custom rule templates in the repository.
     */
    public static void defineTemplates(RulesDefinition.NewRepository repository) {
        definePatternMatchTemplate(repository);
        defineFunctionNamePatternTemplate(repository);
        defineForbiddenApiTemplate(repository);
    }

    /**
     * Template: Pattern Match Rule
     * Allows users to define a regex pattern to flag in code.
     */
    private static void definePatternMatchTemplate(RulesDefinition.NewRepository repository) {
        RulesDefinition.NewRule rule = repository.createRule(PATTERN_MATCH_TEMPLATE_KEY)
            .setName("Pattern Match Rule Template")
            .setHtmlDescription(
                "<p>This template allows you to create a custom rule that flags code matching a regular expression pattern.</p>"
                + "<h2>How to Use</h2>"
                + "<ol>"
                + "<li>Go to Rules → Custom Rules</li>"
                + "<li>Create a new rule from this template</li>"
                + "<li>Define your regex pattern</li>"
                + "<li>Write a descriptive message</li>"
                + "<li>Activate it in your Quality Profile</li>"
                + "</ol>"
                + "<h2>Example Use Cases</h2>"
                + "<ul>"
                + "<li>Flag legacy API usage: <code>OldAPI\\w+</code></li>"
                + "<li>Enforce naming conventions: <code>\\$[A-Z]</code> (global variables)</li>"
                + "<li>Detect anti-patterns: <code>AppendTo\\[.*,.*\\]</code></li>"
                + "</ul>"
                + "<h2>Parameters</h2>"
                + "<ul>"
                + "<li><strong>pattern</strong>: Java regular expression to match</li>"
                + "<li><strong>message</strong>: Issue message shown to users</li>"
                + "</ul>"
            )
            .setTemplate(true)  // Mark as template
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .addTags(CUSTOM, "pattern-match");

        // Define parameters
        rule.createParam(PATTERN_PARAM)
            .setDefaultValue("")
            .setType(RuleParamType.STRING)
            .setDescription("Regular expression pattern to match against code. Use Java regex syntax.");

        rule.createParam(MESSAGE_PARAM)
            .setDefaultValue("Code matches forbidden pattern")
            .setType(RuleParamType.TEXT)
            .setDescription("Message to display when the pattern is found.");
    }

    /**
     * Template: Function Name Pattern Rule
     * Allows users to flag functions with names matching a pattern.
     */
    private static void defineFunctionNamePatternTemplate(RulesDefinition.NewRepository repository) {
        RulesDefinition.NewRule rule = repository.createRule(FUNCTION_NAME_PATTERN_TEMPLATE_KEY)
            .setName("Function Name Pattern Rule Template")
            .setHtmlDescription(
                "<p>This template allows you to create a custom rule that flags function definitions with names matching a pattern.</p>"
                + "<h2>How to Use</h2>"
                + "<ol>"
                + "<li>Create a new rule from this template</li>"
                + "<li>Define a regex pattern for function names</li>"
                + "<li>Specify why these functions should be flagged</li>"
                + "</ol>"
                + "<h2>Example Use Cases</h2>"
                + "<ul>"
                + "<li>Deprecate legacy functions: <code>^Legacy.*</code></li>"
                + "<li>Enforce naming: <code>^[a-z].*</code> (lowercase start)</li>"
                + "<li>Flag temp/test functions: <code>.*(?i)(temp|test).*</code></li>"
                + "</ul>"
            )
            .setTemplate(true)
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .addTags(CUSTOM, "naming", "function");

        rule.createParam(FUNCTION_NAME_PATTERN_PARAM)
            .setDefaultValue("")
            .setType(RuleParamType.STRING)
            .setDescription("Regular expression to match function names. Example: ^Legacy.* to match functions starting with 'Legacy'");

        rule.createParam(MESSAGE_PARAM)
            .setDefaultValue("Function name matches forbidden pattern")
            .setType(RuleParamType.TEXT)
            .setDescription("Message to display when a matching function is found.");
    }

    /**
     * Template: Forbidden API Rule
     * Allows users to flag usage of specific APIs or functions.
     */
    private static void defineForbiddenApiTemplate(RulesDefinition.NewRepository repository) {
        RulesDefinition.NewRule rule = repository.createRule(FORBIDDEN_API_TEMPLATE_KEY)
            .setName("Forbidden API Rule Template")
            .setHtmlDescription(
                "<p>This template allows you to create a custom rule that flags usage of specific Mathematica functions or APIs.</p>"
                + "<h2>How to Use</h2>"
                + "<ol>"
                + "<li>Create a new rule from this template</li>"
                + "<li>Specify the API/function name to forbid</li>"
                + "<li>Explain why it's forbidden</li>"
                + "</ol>"
                + "<h2>Example Use Cases</h2>"
                + "<ul>"
                + "<li>Deprecated functions: <code>OldFunction</code></li>"
                + "<li>Performance anti-patterns: <code>AppendTo</code> in production code</li>"
                + "<li>Security: <code>Get</code> with user input</li>"
                + "<li>Platform-specific: <code>SystemOpen</code> in cloud code</li>"
                + "</ul>"
            )
            .setTemplate(true)
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .addTags(CUSTOM, "api", "forbidden");

        rule.createParam(API_NAME_PARAM)
            .setDefaultValue("")
            .setType(RuleParamType.STRING)
            .setDescription("Name of the forbidden API or function. Example: AppendTo, OldFunction");

        rule.createParam(REASON_PARAM)
            .setDefaultValue("This API should not be used")
            .setType(RuleParamType.TEXT)
            .setDescription("Explanation of why this API is forbidden. This will be shown to developers.");
    }
}
