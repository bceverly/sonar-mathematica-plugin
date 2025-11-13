package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CustomRuleDetectorTest {

    private CustomRuleDetector detector;

    @Mock
    private SensorContext context;

    @Mock
    private InputFile inputFile;

    @Mock
    private MathematicaRulesSensor sensor;

    @Mock
    private ActiveRule activeRule;

    @Mock
    private RuleKey ruleKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        detector = new CustomRuleDetector();
        detector.setSensor(sensor);
    }

    @Test
    void testExecuteCustomRulesWithEmptyCollection() {
        detector.executeCustomRules(context, inputFile, "some content", Collections.emptyList());

        verifyNoInteractions(sensor);
    }

    @Test
    void testExecuteCustomRulesWithNullCollection() {
        detector.executeCustomRules(context, inputFile, "some content", null);

        verifyNoInteractions(sensor);
    }

    @Test
    void testPatternMatchRuleSingleMatch() {
        String content = "x = AppendTo[list, item];\ny = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Use Join instead");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(inputFile, 1, "test-rule", "Use Join instead");
    }

    @Test
    void testPatternMatchRuleMultipleMatches() {
        String content = "f[x_]; g[y_]; h[z_];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("pattern-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("\\[\\w+_\\]");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Pattern found");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, atLeast(1)).queueIssue(eq(inputFile), anyInt(), eq("pattern-rule"), eq("Pattern found"));
    }

    static class NoMatchTestData {
        final String content;
        final String templateKey;
        final String param1Name;
        final String param1Value;
        final String param2Name;
        final String param2Value;
        final String description;

        NoMatchTestData(String content, String templateKey,
                       String param1Name, String param1Value,
                       String param2Name, String param2Value,
                       String description) {
            this.content = content;
            this.templateKey = templateKey;
            this.param1Name = param1Name;
            this.param1Value = param1Value;
            this.param2Name = param2Name;
            this.param2Value = param2Value;
            this.description = description;
        }
    }

    static Stream<NoMatchTestData> provideNoMatchScenarios() {
        return Stream.of(
            new NoMatchTestData("x = 5;\ny = 10;",
                CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY,
                CustomRuleTemplatesDefinition.PATTERN_PARAM, "NonExistent",
                CustomRuleTemplatesDefinition.MESSAGE_PARAM, "Not found",
                "pattern no matches"),
            new NoMatchTestData("x = 5;",
                CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY,
                CustomRuleTemplatesDefinition.PATTERN_PARAM, "",
                CustomRuleTemplatesDefinition.MESSAGE_PARAM, "Message",
                "pattern empty"),
            new NoMatchTestData("func[x_] := x;",
                CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY,
                CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM, "",
                CustomRuleTemplatesDefinition.MESSAGE_PARAM, "Message",
                "function name pattern empty"),
            new NoMatchTestData("Get[file]",
                CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY,
                CustomRuleTemplatesDefinition.API_NAME_PARAM, "",
                CustomRuleTemplatesDefinition.REASON_PARAM, "Reason",
                "api name empty"),
            new NoMatchTestData("x = 5;",
                null, null, null, null, null,
                "non-template rule"),
            new NoMatchTestData("x = 5;",
                "unknown-template", null, null, null, null,
                "unknown template type"),
            new NoMatchTestData("x = 5;",
                CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY,
                CustomRuleTemplatesDefinition.PATTERN_PARAM, "[invalid(regex",
                CustomRuleTemplatesDefinition.MESSAGE_PARAM, "Message",
                "invalid regex pattern")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNoMatchScenarios")
    void testRulesWithNoMatches(NoMatchTestData testData) {
        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(testData.templateKey);

        if (testData.param1Name != null) {
            when(activeRule.param(testData.param1Name)).thenReturn(testData.param1Value);
        }
        if (testData.param2Name != null) {
            when(activeRule.param(testData.param2Name)).thenReturn(testData.param2Value);
        }

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, testData.content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testPatternMatchRuleDefaultMessage() {
        String content = "AppendTo[list, x]";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn(null);

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(inputFile, 1, "test-rule",
                                           "Code matches forbidden pattern");
    }

    @Test
    void testFunctionNamePatternRuleMatches() {
        String content = "legacyFunc[x_, y_] := x + y;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("legacy.*");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Legacy function");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("func-rule"),
                                           contains("Legacy function"));
    }

    @Test
    void testFunctionNamePatternRuleWithAssignment() {
        String content = "tempFunction[x_] = x^2;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("temp-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("temp.*");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Temp function");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("temp-rule"),
                                           contains("Temp function"));
    }

    @Test
    void testFunctionNamePatternRuleMultipleFunctions() {
        String content = "test1[x_] := x;\ntest2[y_] := y;\ntest3[z_] := z;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("test\\d+");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Test function");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(3)).queueIssue(eq(inputFile), anyInt(), eq("test-rule"), anyString());
    }

    @Test
    void testForbiddenApiRuleMatches() {
        String content = "x = Get[\"file.m\"];\ny = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Get");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Security risk");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("api-rule"),
                                           contains("Forbidden API 'Get': Security risk"));
    }

    @Test
    void testForbiddenApiRuleMultipleOccurrences() {
        String content = "AppendTo[list, 1];\nAppendTo[list, 2];\nAppendTo[list, 3];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Use Join");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(3)).queueIssue(eq(inputFile), anyInt(), eq("api-rule"), anyString());
    }

    @Test
    void testForbiddenApiRuleWithoutBrackets() {
        String content = "x = SystemOpen;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("SystemOpen");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Not allowed");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("api-rule"),
                                           contains("Forbidden API 'SystemOpen': Not allowed"));
    }

    @Test
    void testForbiddenApiRuleDefaultReason() {
        String content = "OldApi[]";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("OldApi");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn(null);

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("api-rule"),
                                           contains("This API should not be used"));
    }

    @Test
    void testMultipleCustomRules() {
        String content = "AppendTo[list, x];\nlegacyFunc[y_] := y;";

        ActiveRule rule1 = mock(ActiveRule.class);
        RuleKey key1 = mock(RuleKey.class);
        when(rule1.ruleKey()).thenReturn(key1);
        when(key1.rule()).thenReturn("rule1");
        when(rule1.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(rule1.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("AppendTo");
        when(rule1.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Forbidden");

        ActiveRule rule2 = mock(ActiveRule.class);
        RuleKey key2 = mock(RuleKey.class);
        when(rule2.ruleKey()).thenReturn(key2);
        when(key2.rule()).thenReturn("rule2");
        when(rule2.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(rule2.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("legacy.*");
        when(rule2.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Legacy");

        Collection<ActiveRule> rules = new ArrayList<>();
        rules.add(rule1);
        rules.add(rule2);

        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("rule1"), anyString());
        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(2), eq("rule2"), anyString());
    }

    @Test
    void testRuleExceptionHandling() {
        String content = "x = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenThrow(new RuntimeException("Test exception"));

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        // Should not propagate exception
        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    // ===== COMPREHENSIVE ADDITIONAL TESTS =====

    @Test
    void testPatternMatchRuleInComment() {
        String content = "(* AppendTo[list, x] *)\ny = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Use Join instead");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testPatternMatchRuleInStringLiteral() {
        String content = "x = \"AppendTo[list, x]\";";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Use Join instead");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testPatternMatchRuleWithEmptyMessage() {
        String content = "AppendTo[list, x]";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(inputFile, 1, "test-rule",
                "Code matches forbidden pattern");
    }

    @Test
    void testPatternMatchRuleWithComplexRegex() {
        String content = "Module[{x}, x = 5];\nModule[{y, z}, y + z];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("Module\\s*\\[\\s*\\{[^}]+\\}");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Module found");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(2)).queueIssue(eq(inputFile), anyInt(), eq("test-rule"), eq("Module found"));
    }

    @Test
    void testPatternMatchRuleWithNullPattern() {
        String content = "x = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn(null);
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Message");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testFunctionNamePatternRuleInComment() {
        String content = "(* legacyFunc[x_] := x *)\ny = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("legacy.*");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Legacy function");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testFunctionNamePatternRuleInStringLiteral() {
        String content = "x = \"legacyFunc[x_] := x\";";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("legacy.*");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Legacy function");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testFunctionNamePatternRuleDefaultMessage() {
        String content = "legacyFunc[x_] := x;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("legacy.*");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn(null);

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("func-rule"),
                contains("Function name matches forbidden pattern"));
    }

    @Test
    void testFunctionNamePatternRuleWithEmptyMessage() {
        String content = "legacyFunc[x_] := x;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("legacy.*");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("func-rule"),
                contains("Function name matches forbidden pattern"));
    }

    @Test
    void testFunctionNamePatternRuleWithInvalidRegex() {
        String content = "legacyFunc[x_] := x;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("[invalid(regex");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Message");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testFunctionNamePatternRuleWithNullPattern() {
        String content = "legacyFunc[x_] := x;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn(null);
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Message");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testForbiddenApiRuleInComment() {
        String content = "(* Get[\"file.m\"] *)\ny = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Get");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Security risk");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testForbiddenApiRuleInStringLiteral() {
        String content = "x = \"Get[file]\";";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Get");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Security risk");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testForbiddenApiRuleWithEmptyReason() {
        String content = "Get[file]";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Get");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("api-rule"),
                contains("This API should not be used"));
    }

    @Test
    void testForbiddenApiRuleWithNullApiName() {
        String content = "Get[file]";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn(null);
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Reason");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testForbiddenApiRulePartialMatch() {
        String content = "x = MyGet[file];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Get");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Security risk");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testForbiddenApiRuleWithSpecialCharacters() {
        String content = "x = Import[file];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Import");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Use custom import");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("api-rule"), anyString());
    }

    @Test
    void testMultipleRulesWithSameContent() {
        String content = "AppendTo[list, 1];\nlegacyFunc[x_] := x;\nGet[file];";

        ActiveRule rule1 = mock(ActiveRule.class);
        RuleKey key1 = mock(RuleKey.class);
        when(rule1.ruleKey()).thenReturn(key1);
        when(key1.rule()).thenReturn("rule1");
        when(rule1.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(rule1.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(rule1.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Pattern match");

        ActiveRule rule2 = mock(ActiveRule.class);
        RuleKey key2 = mock(RuleKey.class);
        when(rule2.ruleKey()).thenReturn(key2);
        when(key2.rule()).thenReturn("rule2");
        when(rule2.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(rule2.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("legacy.*");
        when(rule2.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Function pattern");

        ActiveRule rule3 = mock(ActiveRule.class);
        RuleKey key3 = mock(RuleKey.class);
        when(rule3.ruleKey()).thenReturn(key3);
        when(key3.rule()).thenReturn("rule3");
        when(rule3.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(rule3.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Get");
        when(rule3.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("API forbidden");

        Collection<ActiveRule> rules = new ArrayList<>();
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);

        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("rule1"), anyString());
        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(2), eq("rule2"), anyString());
        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(3), eq("rule3"), anyString());
    }

    @Test
    void testRuleWithExceptionInTemplateKey() {
        String content = "x = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(activeRule.templateRuleKey()).thenThrow(new RuntimeException("Template exception"));

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        // Should handle exception gracefully
        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testPatternMatchRuleOnMultipleLines() {
        String content = "x = AppendTo[list, 1];\ny = AppendTo[list, 2];\nz = AppendTo[list, 3];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Use Join");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(3)).queueIssue(eq(inputFile), anyInt(), eq("test-rule"), eq("Use Join"));
    }

    @Test
    void testFunctionNamePatternRuleWithDelayedAssignment() {
        String content = "testFunc[x_] := Module[{}, x + 1]";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("test.*");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Test function");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("func-rule"), contains("Test function"));
    }

    @Test
    void testForbiddenApiRuleMatchingWholeWord() {
        String content = "GetValue[x]; MyGet[y];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Get");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Forbidden");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        // Should not match GetValue or MyGet, only exact "Get"
        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testEmptyContent() {
        String content = "";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("AppendTo");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Message");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testContentWithOnlyComments() {
        String content = "(* This is a comment *)\n(* Another comment *)";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("comment");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Found comment");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        // Should not match inside comments
        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testPatternMatchRuleWithCaseInsensitivePattern() {
        String content = "appendto[list, x]; APPENDTO[list, y];";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("(?i)appendto");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Found appendto");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(2)).queueIssue(eq(inputFile), anyInt(), eq("test-rule"), eq("Found appendto"));
    }

    @Test
    void testFunctionNamePatternRuleWithComplexPattern() {
        String content = "oldFunc1[x_] := x; oldFunc2[y_] := y; newFunc[z_] := z;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("func-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("old.*\\d+");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Old function");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, atLeast(1)).queueIssue(eq(inputFile), anyInt(), eq("func-rule"), contains("Old function"));
    }

    @Test
    void testAllRuleTypesExecutedOnce() {
        String content = "pattern; func[x_] := x; Api[call];";

        ActiveRule patternRule = mock(ActiveRule.class);
        RuleKey patternKey = mock(RuleKey.class);
        when(patternRule.ruleKey()).thenReturn(patternKey);
        when(patternKey.rule()).thenReturn("pattern-rule");
        when(patternRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(patternRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("pattern");
        when(patternRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Pattern found");

        ActiveRule funcRule = mock(ActiveRule.class);
        RuleKey funcKey = mock(RuleKey.class);
        when(funcRule.ruleKey()).thenReturn(funcKey);
        when(funcKey.rule()).thenReturn("func-rule");
        when(funcRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(funcRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("func");
        when(funcRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Function found");

        ActiveRule apiRule = mock(ActiveRule.class);
        RuleKey apiKey = mock(RuleKey.class);
        when(apiRule.ruleKey()).thenReturn(apiKey);
        when(apiKey.rule()).thenReturn("api-rule");
        when(apiRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(apiRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("Api");
        when(apiRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("API forbidden");

        Collection<ActiveRule> rules = new ArrayList<>();
        rules.add(patternRule);
        rules.add(funcRule);
        rules.add(apiRule);

        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("pattern-rule"), anyString());
        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("func-rule"), anyString());
        verify(sensor, times(1)).queueIssue(eq(inputFile), eq(1), eq("api-rule"), anyString());
    }
}
