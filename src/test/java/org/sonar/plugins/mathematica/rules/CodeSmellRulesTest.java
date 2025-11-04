package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class CodeSmellRulesTest {

    @Test
    void testPrivateConstructorThrowsException() throws Exception {
        Constructor<CodeSmellRules> constructor = CodeSmellRules.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(
            InvocationTargetException.class,
            constructor::newInstance
        );

        assertThat(exception.getCause())
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Utility class");
    }

    @Test
    void testDefineCallsAllSubMethods() {
        // Setup mock repository
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        // Make the mock return itself for method chaining
        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        // Execute
        CodeSmellRules.define(repository);

        // Verify createRule was called the expected number of times (33 rules total)
        // Basic: 8 rules
        // Advanced: 10 rules
        // Performance: 8 rules
        // Best Practice: 7 rules
        // Total: 33 rules
        verify(repository, times(33)).createRule(anyString());
    }

    @Test
    void testDefineBasicCodeSmells() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify specific basic code smell rules are created
        verify(repository).createRule(MathematicaRulesDefinition.COMMENTED_CODE_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.MAGIC_NUMBER_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.TODO_FIXME_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.EMPTY_BLOCK_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.FUNCTION_LENGTH_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.FILE_LENGTH_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.EMPTY_CATCH_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.DEBUG_CODE_KEY);
    }

    @Test
    void testDefineAdvancedCodeSmells() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify specific advanced code smell rules are created
        verify(repository).createRule(MathematicaRulesDefinition.UNUSED_VARIABLES_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.DUPLICATE_FUNCTION_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.TOO_MANY_PARAMETERS_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.DEEPLY_NESTED_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.MISSING_DOCUMENTATION_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.INCONSISTENT_NAMING_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.IDENTICAL_BRANCHES_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.DEPRECATED_FUNCTION_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.EMPTY_STATEMENT_KEY);
    }

    @Test
    void testDefinePerformanceRules() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify specific performance rules are created
        verify(repository).createRule(MathematicaRulesDefinition.APPEND_IN_LOOP_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.REPEATED_FUNCTION_CALLS_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.STRING_CONCAT_IN_LOOP_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.UNCOMPILED_NUMERICAL_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.PACKED_ARRAY_BREAKING_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.NESTED_MAP_TABLE_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.LARGE_TEMP_EXPRESSIONS_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.PLOT_IN_LOOP_KEY);
    }

    @Test
    void testDefineBestPracticeRules() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify specific best practice rules are created
        verify(repository).createRule(MathematicaRulesDefinition.GENERIC_VARIABLE_NAMES_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.MISSING_USAGE_MESSAGE_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.MISSING_OPTIONS_PATTERN_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.SIDE_EFFECTS_NAMING_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.COMPLEX_BOOLEAN_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.UNPROTECTED_SYMBOLS_KEY);
        verify(repository).createRule(MathematicaRulesDefinition.MISSING_RETURN_KEY);
    }

    @Test
    void testRulesHaveDescriptions() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify that setHtmlDescription was called 33 times (once per rule)
        verify(mockRule, times(33)).setHtmlDescription(anyString());
    }

    @Test
    void testRulesHaveNames() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify that setName was called 33 times (once per rule)
        verify(mockRule, times(33)).setName(anyString());
    }

    @Test
    void testRulesHaveImpacts() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify that addDefaultImpact was called 33 times (once per rule)
        verify(mockRule, times(33)).addDefaultImpact(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void testRulesHaveTags() {
        NewRepository repository = mock(NewRepository.class);
        NewRule mockRule = mock(NewRule.class);

        when(repository.createRule(anyString())).thenReturn(mockRule);
        when(mockRule.setName(anyString())).thenReturn(mockRule);
        when(mockRule.setHtmlDescription(anyString())).thenReturn(mockRule);
        when(mockRule.addDefaultImpact(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(mockRule);
        when(mockRule.setTags(org.mockito.ArgumentMatchers.<String>any())).thenReturn(mockRule);

        CodeSmellRules.define(repository);

        // Verify that setTags was called at least 32 times (all rules have tags)
        // Note: Some rules pass multiple tags to setTags() varargs method
        verify(mockRule, org.mockito.Mockito.atLeast(32)).setTags(org.mockito.ArgumentMatchers.any());
    }
}
