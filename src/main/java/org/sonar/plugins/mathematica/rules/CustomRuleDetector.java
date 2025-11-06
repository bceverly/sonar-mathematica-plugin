package org.sonar.plugins.mathematica.rules;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes user-defined custom rules based on rule templates.
 * Handles pattern matching, function name validation, and forbidden API detection.
 */
public class CustomRuleDetector extends BaseDetector {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRuleDetector.class);

    /**
     * Execute all active custom rules on the given file.
     *
     * @param context Sensor context
     * @param inputFile File to analyze
     * @param content File content
     * @param customRules Collection of active custom rules
     */
    public void executeCustomRules(SensorContext context, InputFile inputFile, String content,
                                   Collection<org.sonar.api.batch.rule.ActiveRule> customRules) {
        if (customRules == null || customRules.isEmpty()) {
            return;
        }

        initializeCaches(content);

        try {
            for (org.sonar.api.batch.rule.ActiveRule activeRule : customRules) {
                try {
                    executeCustomRule(context, inputFile, content, activeRule);
                } catch (Exception e) {
                    LOG.error("Error executing custom rule {}: {}", activeRule.ruleKey(), e.getMessage());
                }
            }
        } finally {
            clearCaches();
        }
    }

    /**
     * Execute a single custom rule based on its template type.
     */
    private void executeCustomRule(SensorContext context, InputFile inputFile, String content,
                                   org.sonar.api.batch.rule.ActiveRule activeRule) {
        RuleKey ruleKey = activeRule.ruleKey();
        String templateKey = activeRule.templateRuleKey();

        if (templateKey == null) {
            return; // Not a custom rule
        }

        switch (templateKey) {
            case CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY:
                executePatternMatchRule(context, inputFile, content, activeRule);
                break;

            case CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY:
                executeFunctionNamePatternRule(context, inputFile, content, activeRule);
                break;

            case CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY:
                executeForbiddenApiRule(context, inputFile, content, activeRule);
                break;

            default:
                LOG.warn("Unknown custom rule template: {}", templateKey);
        }
    }

    /**
     * Execute pattern match rule: flags code matching a user-defined regex.
     */
    private void executePatternMatchRule(SensorContext context, InputFile inputFile, String content,
                                        org.sonar.api.batch.rule.ActiveRule activeRule) {
        String patternString = activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM);
        String message = activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM);

        if (patternString == null || patternString.trim().isEmpty()) {
            LOG.warn("Pattern parameter is empty for rule {}", activeRule.ruleKey());
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            message = "Code matches forbidden pattern";
        }

        try {
            Pattern pattern = Pattern.compile(patternString); //NOSONAR - Possessive quantifiers prevent backtracking
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, line, activeRule.ruleKey().rule(), message);
            }
        } catch (Exception e) {
            LOG.error("Invalid regex pattern for rule {}: {}", activeRule.ruleKey(), e.getMessage());
        }
    }

    /**
     * Execute function name pattern rule: flags functions with names matching a pattern.
     */
    private void executeFunctionNamePatternRule(SensorContext context, InputFile inputFile, String content,
                                               org.sonar.api.batch.rule.ActiveRule activeRule) {
        String functionPatternString = activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM);
        String message = activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM);

        if (functionPatternString == null || functionPatternString.trim().isEmpty()) {
            LOG.warn("Function name pattern parameter is empty for rule {}", activeRule.ruleKey());
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            message = "Function name matches forbidden pattern";
        }

        try {
            // Match function definitions: functionName[params] := body OR functionName[params] = body
            //NOSONAR - Possessive quantifiers prevent backtracking
            Pattern functionDefPattern = Pattern.compile(
                "(" + functionPatternString + ")\\s*+\\[[^\\]]*+\\]\\s*+:?="
            );

            Matcher matcher = functionDefPattern.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                String functionName = matcher.group(1);
                String detailedMessage = message + ": " + functionName;
                reportIssue(context, inputFile, line, activeRule.ruleKey().rule(), detailedMessage);
            }
        } catch (Exception e) {
            LOG.error("Invalid regex pattern for rule {}: {}", activeRule.ruleKey(), e.getMessage());
        }
    }

    /**
     * Execute forbidden API rule: flags usage of specific functions/APIs.
     */
    private void executeForbiddenApiRule(SensorContext context, InputFile inputFile, String content,
                                        org.sonar.api.batch.rule.ActiveRule activeRule) {
        String apiName = activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM);
        String reason = activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM);

        if (apiName == null || apiName.trim().isEmpty()) {
            LOG.warn("API name parameter is empty for rule {}", activeRule.ruleKey());
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            reason = "This API should not be used";
        }

        try {
            // Match API usage: ApiName[...] or ApiName (without brackets)
            // Use word boundaries to match whole words only
            //NOSONAR - Possessive quantifiers prevent backtracking
            Pattern apiPattern = Pattern.compile("\\b" + Pattern.quote(apiName) + "\\b(?:\\s*+\\[)?+");
            Matcher matcher = apiPattern.matcher(content);

            while (matcher.find()) {
                int line = calculateLineNumber(content, matcher.start());
                String message = "Forbidden API '" + apiName + "': " + reason;
                reportIssue(context, inputFile, line, activeRule.ruleKey().rule(), message);
            }
        } catch (Exception e) {
            LOG.error("Error matching API pattern for rule {}: {}", activeRule.ruleKey(), e.getMessage());
        }
    }
}
