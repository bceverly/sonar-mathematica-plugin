package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

    @Test
    void testPatternMatchRuleNoMatches() {
        String content = "x = 5;\ny = 10;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("NonExistent");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Not found");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testPatternMatchRuleEmptyPattern() {
        String content = "x = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Message");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

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
    void testFunctionNamePatternRuleEmptyPattern() {
        String content = "func[x_] := x;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.FUNCTION_NAME_PATTERN_PARAM)).thenReturn("");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Message");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
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
    void testForbiddenApiRuleEmptyApiName() {
        String content = "Get[file]";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("api-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.FORBIDDEN_API_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.API_NAME_PARAM)).thenReturn("");
        when(activeRule.param(CustomRuleTemplatesDefinition.REASON_PARAM)).thenReturn("Reason");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
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
    void testNonTemplateRule() {
        String content = "x = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("regular-rule");
        when(activeRule.templateRuleKey()).thenReturn(null);

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void testUnknownTemplateType() {
        String content = "x = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("unknown-rule");
        when(activeRule.templateRuleKey()).thenReturn("unknown-template");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
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
    void testPatternMatchRuleInvalidRegex() {
        String content = "x = 5;";

        when(activeRule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn("test-rule");
        when(activeRule.templateRuleKey()).thenReturn(CustomRuleTemplatesDefinition.PATTERN_MATCH_TEMPLATE_KEY);
        when(activeRule.param(CustomRuleTemplatesDefinition.PATTERN_PARAM)).thenReturn("[invalid(regex");
        when(activeRule.param(CustomRuleTemplatesDefinition.MESSAGE_PARAM)).thenReturn("Message");

        Collection<ActiveRule> rules = Collections.singletonList(activeRule);
        detector.executeCustomRules(context, inputFile, content, rules);

        verify(sensor, never()).queueIssue(any(), anyInt(), anyString(), anyString());
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
}
