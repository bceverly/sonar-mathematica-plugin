package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.issue.impact.Severity;

/**
 * Defines all Code Smell rules (33 rules).
 * Split from MathematicaRulesDefinition for maintainability.
 */
public final class CodeSmellRules {

    // Private constructor to prevent instantiation
    private CodeSmellRules() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all code smell rules in the repository.
     */
    public static void define(NewRepository repository) {
        defineBasicCodeSmells(repository);
        definePhase2CodeSmells(repository);
        definePerformanceRules(repository);
        defineBestPracticeRules(repository);
    }

    private static void defineBasicCodeSmells(NewRepository repository) {
        // Commented Code
        repository.createRule(MathematicaRulesDefinition.COMMENTED_CODE_KEY)
            .setName("Sections of code should not be commented out")
            .setHtmlDescription(
                "<p>Programmers should not comment out code as it bloats programs and reduces readability.</p>"
                + "<p>Unused code should be deleted and can be retrieved from source control history if required.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("unused", "clutter");

        // Magic Numbers
        repository.createRule(MathematicaRulesDefinition.MAGIC_NUMBER_KEY)
            .setName("Magic numbers should not be used")
            .setHtmlDescription(
                "<p>Magic numbers are unexplained numeric literals that make code harder to understand and maintain.</p>"
                + "<p>Replace magic numbers with named constants to improve readability.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("readability");

        // TODO/FIXME
        repository.createRule(MathematicaRulesDefinition.TODO_FIXME_KEY)
            .setName("Track TODO and FIXME comments")
            .setHtmlDescription(
                "<p>TODO and FIXME comments indicate incomplete or problematic code that needs attention.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("todo");

        // Empty Blocks
        repository.createRule(MathematicaRulesDefinition.EMPTY_BLOCK_KEY)
            .setName("Empty blocks should be removed")
            .setHtmlDescription(
                "<p>Empty code blocks serve no purpose and should be removed.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("unused");

        // Function Length
        repository.createRule(MathematicaRulesDefinition.FUNCTION_LENGTH_KEY)
            .setName("Functions should not be too long")
            .setHtmlDescription(
                "<p>Functions longer than 100 lines are hard to understand and maintain.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("complexity");

        // File Length
        repository.createRule(MathematicaRulesDefinition.FILE_LENGTH_KEY)
            .setName("Files should not be too long")
            .setHtmlDescription(
                "<p>Files longer than 1000 lines are hard to navigate and maintain.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("complexity");

        // Empty Catch
        repository.createRule(MathematicaRulesDefinition.EMPTY_CATCH_KEY)
            .setName("Empty catch blocks should be avoided")
            .setHtmlDescription(
                "<p>Silently ignoring exceptions makes debugging difficult.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("error-handling");

        // Debug Code
        repository.createRule(MathematicaRulesDefinition.DEBUG_CODE_KEY)
            .setName("Debug code should be removed")
            .setHtmlDescription(
                "<p>Print statements and debug code should be removed before commit.</p>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("debug");

    }

    private static void definePhase2CodeSmells(NewRepository repository) {
        // Phase 2 Code Smells
        repository.createRule(MathematicaRulesDefinition.UNUSED_VARIABLES_KEY)
            .setName("Unused variables should be removed")
            .setHtmlDescription("<p>Variables declared but never used clutter code.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("unused");

        repository.createRule(MathematicaRulesDefinition.DUPLICATE_FUNCTION_KEY)
            .setName("Duplicate function definitions should be avoided")
            .setHtmlDescription("<p>Multiple identical function definitions indicate copy-paste code.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("duplication");

        repository.createRule(MathematicaRulesDefinition.TOO_MANY_PARAMETERS_KEY)
            .setName("Functions should not have too many parameters")
            .setHtmlDescription("<p>Functions with more than 7 parameters are hard to use.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("complexity");

        repository.createRule(MathematicaRulesDefinition.DEEPLY_NESTED_KEY)
            .setName("Control structures should not be deeply nested")
            .setHtmlDescription("<p>Nesting deeper than 3 levels makes code hard to understand.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("complexity");

        repository.createRule(MathematicaRulesDefinition.MISSING_DOCUMENTATION_KEY)
            .setName("Complex functions should have documentation")
            .setHtmlDescription("<p>Functions with high complexity should have comments explaining their logic.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation");

        repository.createRule(MathematicaRulesDefinition.INCONSISTENT_NAMING_KEY)
            .setName("Naming should be consistent")
            .setHtmlDescription("<p>Use consistent naming conventions throughout code.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("naming");

        repository.createRule(MathematicaRulesDefinition.IDENTICAL_BRANCHES_KEY)
            .setName("Identical branches should be merged")
            .setHtmlDescription("<p>If/Switch branches with identical code should be merged.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("duplication");

        repository.createRule(MathematicaRulesDefinition.EXPRESSION_TOO_COMPLEX_KEY)
            .setName("Expressions should not be too complex")
            .setHtmlDescription("<p>Expressions with more than 10 operators are hard to understand.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("complexity");

        repository.createRule(MathematicaRulesDefinition.DEPRECATED_FUNCTION_KEY)
            .setName("Deprecated functions should not be used")
            .setHtmlDescription("<p>Use current APIs instead of deprecated functions.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("deprecated");

        repository.createRule(MathematicaRulesDefinition.EMPTY_STATEMENT_KEY)
            .setName("Empty statements should be removed")
            .setHtmlDescription("<p>Double semicolons create empty statements.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("unused");
    }

    private static void definePerformanceRules(NewRepository repository) {
        // Performance rules - see full implementations in original file
        // Keeping concise for space - full HTML descriptions available in original

        repository.createRule(MathematicaRulesDefinition.APPEND_IN_LOOP_KEY)
            .setName("AppendTo should not be used in loops")
            .setHtmlDescription("<p>AppendTo in loops creates O(n²) performance. Use Table or Sow/Reap.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("performance");

        repository.createRule(MathematicaRulesDefinition.REPEATED_FUNCTION_CALLS_KEY)
            .setName("Expensive function calls should not be repeated")
            .setHtmlDescription("<p>Cache repeated expensive computations.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("performance");

        repository.createRule(MathematicaRulesDefinition.STRING_CONCAT_IN_LOOP_KEY)
            .setName("String concatenation should not be used in loops")
            .setHtmlDescription("<p>String concat in loops is O(n²). Use StringJoin.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("performance");

        repository.createRule(MathematicaRulesDefinition.UNCOMPILED_NUMERICAL_KEY)
            .setName("Numerical loops should use Compile")
            .setHtmlDescription("<p>Numerical code can be 10-100x faster with Compile.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("performance");

        repository.createRule(MathematicaRulesDefinition.PACKED_ARRAY_BREAKING_KEY)
            .setName("Operations should preserve packed arrays")
            .setHtmlDescription("<p>Packed arrays are 10x+ faster. Avoid unpacking operations.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("performance");

        repository.createRule(MathematicaRulesDefinition.NESTED_MAP_TABLE_KEY)
            .setName("Nested Map/Table should be refactored")
            .setHtmlDescription("<p>Nested Map/Table can often be single operation or Outer.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("performance");

        repository.createRule(MathematicaRulesDefinition.LARGE_TEMP_EXPRESSIONS_KEY)
            .setName("Large temporary expressions should be assigned to variables")
            .setHtmlDescription("<p>Large intermediate results (>100MB) should be assigned for visibility.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("memory");

        repository.createRule(MathematicaRulesDefinition.PLOT_IN_LOOP_KEY)
            .setName("Plotting functions should not be called in loops")
            .setHtmlDescription("<p>Plots in loops are very slow. Collect data first, plot once.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("performance");
    }

    private static void defineBestPracticeRules(NewRepository repository) {
        repository.createRule(MathematicaRulesDefinition.GENERIC_VARIABLE_NAMES_KEY)
            .setName("Variables should have meaningful names")
            .setHtmlDescription("<p>Avoid generic names like 'temp', 'data', 'result'.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("naming");

        repository.createRule(MathematicaRulesDefinition.MISSING_USAGE_MESSAGE_KEY)
            .setName("Public functions should have usage messages")
            .setHtmlDescription("<p>Public functions should define ::usage documentation.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation");

        repository.createRule(MathematicaRulesDefinition.MISSING_OPTIONS_PATTERN_KEY)
            .setName("Functions with multiple optional parameters should use OptionsPattern")
            .setHtmlDescription("<p>3+ optional parameters should use OptionsPattern.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("api-design");

        repository.createRule(MathematicaRulesDefinition.SIDE_EFFECTS_NAMING_KEY)
            .setName("Functions with side effects should have descriptive names")
            .setHtmlDescription("<p>Functions modifying global state should use Set*/Update* or end with !.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("naming");

        repository.createRule(MathematicaRulesDefinition.COMPLEX_BOOLEAN_KEY)
            .setName("Complex boolean expressions should be simplified")
            .setHtmlDescription("<p>Boolean with 5+ operators should be broken into named conditions.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("readability");

        repository.createRule(MathematicaRulesDefinition.UNPROTECTED_SYMBOLS_KEY)
            .setName("Public API symbols should be protected")
            .setHtmlDescription("<p>Public functions in packages should use Protect[].</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("api-design");

        repository.createRule(MathematicaRulesDefinition.MISSING_RETURN_KEY)
            .setName("Complex functions should have explicit Return statements")
            .setHtmlDescription("<p>Functions with conditionals should use explicit Return[] for clarity.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("readability");
    }
}
