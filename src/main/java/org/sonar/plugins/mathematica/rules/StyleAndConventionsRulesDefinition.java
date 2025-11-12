package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;

/**
 * Additional Code Smell rules (70 rules) to approach parity with Tier 1 languages.
 * Focuses on style, naming, complexity, maintainability, and best practices.
 *
 * This brings total code smells from 221 to 291 (closer to Java's 458).
 */
public final class StyleAndConventionsRulesDefinition {

    private static final String TAG_STYLE = "style";
    private static final String TAG_CONVENTION = "convention";
    private static final String TAG_COMPLEXITY = "complexity";
    private static final String TAG_MAINTAINABILITY = "maintainability";
    private static final String TAG_BEST_PRACTICE = "best-practice";
    private static final String TAG_FORMATTING = "formatting";
    private static final String TAG_READABILITY = "readability";

    private static final String CONFUSING = "confusing";

    private StyleAndConventionsRulesDefinition() {
        // Utility class
    }

    /**
     * Register all 70 additional code smell rules.
     */
    public static void defineRules(NewRepository repository) {
        defineStyleAndFormattingRules(repository);      // 15 rules
        defineNamingConventionRules(repository);        // 15 rules
        defineComplexityRules(repository);              // 10 rules
        defineMaintainabilityRules(repository);         // 15 rules
        defineBestPracticesRules(repository);           // 15 rules
    }

    // ===== STYLE AND FORMATTING (15 rules) =====

    private static void defineStyleAndFormattingRules(NewRepository repository) {
        repository.createRule(MathematicaRulesDefinition.LINE_TOO_LONG_KEY)
            .setName("Lines should not be too long")
            .setHtmlDescription("<p>Long lines are hard to read and often indicate complex code.</p>"
                + "<p>Split lines over 150 characters into multiple lines.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.INCONSISTENT_INDENTATION_KEY)
            .setName("Indentation should be consistent")
            .setHtmlDescription("<p>Mixing tabs and spaces, or inconsistent indentation levels makes code hard to read.</p>"
                + "<p>Use consistent indentation (2 or 4 spaces recommended).</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.TRAILING_WHITESPACE_KEY)
            .setName("Lines should not have trailing whitespace")
            .setHtmlDescription("<p>Trailing whitespace clutters diffs and serves no purpose.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.MULTIPLE_BLANK_LINES_KEY)
            .setName("Multiple consecutive blank lines should be avoided")
            .setHtmlDescription("<p>More than 2 consecutive blank lines adds unnecessary whitespace.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.MISSING_BLANK_LINE_AFTER_FUNCTION_KEY)
            .setName("Function definitions should be followed by a blank line")
            .setHtmlDescription("<p>Blank lines after function definitions improve readability.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        // PERMANENTLY REMOVED: These two style rules (OPERATOR_SPACING and COMMA_SPACING)
        // generated 1.6M+ issues (50% of all issues) and caused significant performance overhead.
        // The detection methods have been completely removed from StyleAndConventionsDetector.
        // Rule definitions kept below with DEPRECATED status for historical tracking only.
        repository.createRule(MathematicaRulesDefinition.OPERATOR_SPACING_KEY)
            .setName("[Removed] Operators should be surrounded by spaces")
            .setHtmlDescription("<p><strong>This rule has been permanently removed due to performance considerations.</strong></p>"
                + "<p>The rule generated over 800,000 issues and caused significant analysis overhead.</p>"
                + "<p>Historical note: Checked that operators have surrounding spaces (e.g., <code>x = 5</code> instead of <code>x=5</code>)</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING, TAG_READABILITY)
            .setStatus(RuleStatus.DEPRECATED);

        repository.createRule(MathematicaRulesDefinition.COMMA_SPACING_KEY)
            .setName("[Removed] Commas should be followed by a space")
            .setHtmlDescription("<p><strong>This rule has been permanently removed due to performance considerations.</strong></p>"
                + "<p>The rule generated over 800,000 issues and caused significant analysis overhead.</p>"
                + "<p>Historical note: Checked that commas have space after them "
                + "(e.g., <code>f[a, b, c]</code> instead of <code>f[a,b,c]</code>)</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.DEPRECATED);

        repository.createRule(MathematicaRulesDefinition.BRACKET_SPACING_KEY)
            .setName("Opening brackets should not be preceded by whitespace")
            .setHtmlDescription("<p>Whitespace before opening brackets is unconventional.</p>"
                + "<p>Example: <code>f [x]</code> → <code>f[x]</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.SEMICOLON_STYLE_KEY)
            .setName("Semicolons should not be used excessively")
            .setHtmlDescription("<p>Multiple semicolons or semicolons in unnecessary places reduce readability.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.FILE_ENDS_WITHOUT_NEWLINE_KEY)
            .setName("Files should end with a newline")
            .setHtmlDescription("<p>Files should end with a newline character per POSIX standard.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.ALIGNMENT_INCONSISTENT_KEY)
            .setName("Table/list elements should be consistently aligned")
            .setHtmlDescription("<p>Inconsistent alignment in multi-line lists or tables reduces readability.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.PARENTHESES_UNNECESSARY_KEY)
            .setName("Unnecessary parentheses should be removed")
            .setHtmlDescription("<p>Excessive parentheses clutter code without adding clarity.</p>"
                + "<p>Example: <code>(x + y)</code> in <code>f[(x + y)]</code> is often unnecessary.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.BRACE_STYLE_KEY)
            .setName("Curly braces should follow consistent style")
            .setHtmlDescription("<p>Inconsistent brace placement reduces code readability.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.LONG_STRING_LITERAL_KEY)
            .setName("Long string literals should be split")
            .setHtmlDescription("<p>String literals exceeding 100 characters should be split for readability.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.NESTED_BRACKETS_EXCESSIVE_KEY)
            .setName("Excessive bracket nesting should be avoided")
            .setHtmlDescription("<p>More than 5 levels of bracket nesting is hard to read.</p>"
                + "<p>Extract complex nested expressions into named variables.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_STYLE, TAG_COMPLEXITY, TAG_READABILITY)
            .setStatus(RuleStatus.READY);
    }

    // ===== NAMING CONVENTIONS (15 rules) =====

    private static void defineNamingConventionRules(NewRepository repository) {
        repository.createRule(MathematicaRulesDefinition.FUNCTION_NAME_TOO_SHORT_KEY)
            .setName("Function names should not be too short")
            .setHtmlDescription("<p>Function names with fewer than 3 characters (except common ones like f, g, h for math) are unclear.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.FUNCTION_NAME_TOO_LONG_KEY)
            .setName("Function names should not be too long")
            .setHtmlDescription("<p>Function names exceeding 50 characters are unwieldy.</p>"
                + "<p>Use concise, descriptive names.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.VARIABLE_NAME_TOO_SHORT_KEY)
            .setName("Variable names should not be too short")
            .setHtmlDescription("<p>Single-letter variable names (except loop iterators i, j, k) are unclear.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.BOOLEAN_NAME_NON_DESCRIPTIVE_KEY)
            .setName("Boolean variables should have descriptive names")
            .setHtmlDescription("<p>Boolean variables should start with is/has/can/should for clarity.</p>"
                + "<p>Example: <code>flag</code> → <code>isValid</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.CONSTANT_NOT_UPPERCASE_KEY)
            .setName("Constants should use UPPER_CASE naming")
            .setHtmlDescription("<p>Constants assigned once should use UPPER_CASE naming.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.PACKAGE_NAME_CASE_KEY)
            .setName("Package names should follow naming conventions")
            .setHtmlDescription("<p>Package names should use CamelCase or proper context naming.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.ACRONYM_STYLE_KEY)
            .setName("Acronyms in names should be properly cased")
            .setHtmlDescription("<p>Acronyms should be either all uppercase (HTTP) or title case (Http) consistently.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.VARIABLE_NAME_MATCHES_BUILTIN_KEY)
            .setName("Variables should not shadow built-in symbols")
            .setHtmlDescription("<p>Using names like 'C', 'D', 'E', 'I', 'N', 'O' shadows built-ins.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, CONFUSING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.PARAMETER_NAME_SAME_AS_FUNCTION_KEY)
            .setName("Parameters should not have the same name as their function")
            .setHtmlDescription("<p>Parameter names matching the function name are confusing.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, CONFUSING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.INCONSISTENT_NAMING_STYLE_KEY)
            .setName("Naming style should be consistent within a file")
            .setHtmlDescription("<p>Mixing camelCase and snake_case within a file is inconsistent.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.NUMBER_IN_NAME_KEY)
            .setName("Names with numbers should be meaningful")
            .setHtmlDescription("<p>Names like 'var1', 'func2' are non-descriptive.</p>"
                + "<p>Use meaningful names instead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.HUNGARIAN_NOTATION_KEY)
            .setName("Hungarian notation should be avoided")
            .setHtmlDescription("<p>Type prefixes like 'strName', 'intCount' are obsolete.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.ABBREVIATION_UNCLEAR_KEY)
            .setName("Unclear abbreviations should be avoided")
            .setHtmlDescription("<p>Abbreviations like 'calc', 'mgr', 'proc' are unclear.</p>"
                + "<p>Use full words: 'calculate', 'manager', 'process'.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.GENERIC_NAME_KEY)
            .setName("Generic names should be avoided")
            .setHtmlDescription("<p>Names like 'data', 'temp', 'result', 'value' are too generic.</p>"
                + "<p>Use specific, descriptive names.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.NEGATED_BOOLEAN_NAME_KEY)
            .setName("Negated boolean names should be avoided")
            .setHtmlDescription("<p>Names like 'notValid' or 'isNotEnabled' are confusing.</p>"
                + "<p>Use positive names: 'isValid', 'isDisabled'.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, CONFUSING)
            .setStatus(RuleStatus.READY);
    }

    // ===== COMPLEXITY & ORGANIZATION (10 rules) =====

    private static void defineComplexityRules(NewRepository repository) {
        repository.createRule(MathematicaRulesDefinition.TOO_MANY_VARIABLES_KEY)
            .setName("Functions should not have too many local variables")
            .setHtmlDescription("<p>Functions with more than 15 local variables are hard to understand.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_COMPLEXITY, TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.NESTING_TOO_DEEP_KEY)
            .setName("Code nesting should not be too deep")
            .setHtmlDescription("<p>Nesting deeper than 4 levels is hard to read.</p>"
                + "<p>Extract nested blocks into separate functions.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_COMPLEXITY, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.FILE_TOO_MANY_FUNCTIONS_KEY)
            .setName("Files should not have too many functions")
            .setHtmlDescription("<p>Files with more than 50 function definitions should be split.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_COMPLEXITY, TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.PACKAGE_TOO_MANY_EXPORTS_KEY)
            .setName("Packages should not export too many symbols")
            .setHtmlDescription("<p>Packages exporting more than 30 symbols have too large an API surface.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_COMPLEXITY, TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.SWITCH_TOO_MANY_CASES_KEY)
            .setName("Switch statements should not have too many cases")
            .setHtmlDescription("<p>Switch statements with more than 15 cases should use a lookup table.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_COMPLEXITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.BOOLEAN_EXPRESSION_TOO_COMPLEX_KEY)
            .setName("Boolean expressions should not be too complex")
            .setHtmlDescription("<p>Boolean expressions with more than 5 operators are hard to understand.</p>"
                + "<p>Extract sub-expressions into named boolean variables.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_COMPLEXITY, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.CHAINED_CALLS_TOO_LONG_KEY)
            .setName("Method chains should not be too long")
            .setHtmlDescription("<p>Chains of more than 4 operations are hard to debug.</p>"
                + "<p>Break into intermediate variables.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_COMPLEXITY, TAG_READABILITY)
            .setStatus(RuleStatus.READY);
    }

    // ===== MAINTAINABILITY (15 rules) =====

    private static void defineMaintainabilityRules(NewRepository repository) {
        repository.createRule(MathematicaRulesDefinition.MAGIC_STRING_KEY)
            .setName("Magic strings should be replaced with named constants")
            .setHtmlDescription("<p>String literals used multiple times should be defined as constants.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_MAINTAINABILITY, TAG_BEST_PRACTICE)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.DUPLICATE_STRING_LITERAL_KEY)
            .setName("Duplicate string literals should be avoided")
            .setHtmlDescription("<p>String literals repeated 3+ times should be extracted to a constant.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.HARDCODED_PATH_KEY)
            .setName("File paths should not be hardcoded")
            .setHtmlDescription("<p>Hardcoded paths like '/Users/john/file.txt' reduce portability.</p>"
                + "<p>Use FileNameJoin, NotebookDirectory, or configuration.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY, "portability")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.HARDCODED_URL_KEY)
            .setName("URLs should not be hardcoded")
            .setHtmlDescription("<p>Hardcoded URLs should be moved to configuration.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.CONDITIONAL_COMPLEXITY_KEY)
            .setName("Complex conditionals should be simplified")
            .setHtmlDescription("<p>If statements with complex boolean logic should extract conditions to named variables.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.IDENTICAL_IF_BRANCHES_KEY)
            .setName("If branches should not be identical")
            .setHtmlDescription("<p>Identical if/else branches indicate a logic error.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY, "suspicious")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.DUPLICATE_CODE_BLOCK_KEY)
            .setName("Duplicate code blocks should be extracted")
            .setHtmlDescription("<p>Code blocks duplicated 2+ times should be extracted to a function.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.GOD_FUNCTION_KEY)
            .setName("Functions should not do too many things")
            .setHtmlDescription("<p>Functions that do many unrelated things should be split.</p>"
                + "<p>Follow Single Responsibility Principle.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY, TAG_COMPLEXITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.FEATURE_ENVY_KEY)
            .setName("Functions should not excessively use other modules")
            .setHtmlDescription("<p>Functions that primarily manipulate data from other modules indicate poor organization.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY, "design")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.PRIMITIVE_OBSESSION_KEY)
            .setName("Complex data should use Association instead of primitives")
            .setHtmlDescription("<p>Passing many primitive parameters suggests an Association or dataset would be better.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY, "design")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.SIDE_EFFECT_IN_EXPRESSION_KEY)
            .setName("Side effects should not be hidden in expressions")
            .setHtmlDescription("<p>Side effects like assignments inside complex expressions are confusing.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY, CONFUSING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.INCOMPLETE_PATTERN_MATCH_KEY)
            .setName("Pattern matching should be exhaustive")
            .setHtmlDescription("<p>Pattern matching without a default case can fail unexpectedly.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.MISSING_OPTION_DEFAULT_KEY)
            .setName("Function options should have default values")
            .setHtmlDescription("<p>Options without defaults via OptionValue pattern are fragile.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_MAINTAINABILITY, TAG_BEST_PRACTICE)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.OPTION_NAME_UNCLEAR_KEY)
            .setName("Function options should have clear names")
            .setHtmlDescription("<p>Option names like 'flag', 'mode', 'type' are unclear.</p>"
                + "<p>Use descriptive names following Mathematica conventions.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_MAINTAINABILITY, TAG_CONVENTION)
            .setStatus(RuleStatus.READY);
    }

    // ===== BEST PRACTICES (15 rules) =====

    private static void defineBestPracticesRules(NewRepository repository) {
        repository.createRule(MathematicaRulesDefinition.STRING_CONCATENATION_IN_LOOP_KEY)
            .setName("String concatenation in loops should use StringJoin")
            .setHtmlDescription("<p>Repeated string concatenation with &lt;&gt; in loops is inefficient.</p>"
                + "<p>Build a list and use StringJoin.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_BEST_PRACTICE, "performance")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.BOOLEAN_COMPARISON_KEY)
            .setName("Boolean values should not be compared with True/False")
            .setHtmlDescription("<p>Comparing <code>x == True</code> is redundant, use <code>x</code> directly.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.NEGATED_BOOLEAN_COMPARISON_KEY)
            .setName("Negated comparisons should be simplified")
            .setHtmlDescription("<p>Patterns like <code>!(x == y)</code> should use <code>x != y</code>.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.REDUNDANT_CONDITIONAL_KEY)
            .setName("Redundant conditionals should be simplified")
            .setHtmlDescription("<p>Patterns like <code>If[condition, True, False]</code> should just use <code>condition</code>.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.DEPRECATED_OPTION_USAGE_KEY)
            .setName("Deprecated function options should not be used")
            .setHtmlDescription("<p>Options marked as deprecated in Mathematica docs should be avoided.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, "deprecated")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.LIST_QUERY_INEFFICIENT_KEY)
            .setName("List queries should use efficient methods")
            .setHtmlDescription("<p>Use Position/Select/Cases instead of inefficient patterns.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, "performance")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.EQUALITY_CHECK_ON_REALS_KEY)
            .setName("Equality checks on reals should use approximate comparison")
            .setHtmlDescription("<p>Use <code>Abs[x - y] &lt; epsilon</code> instead of <code>x == y</code> for reals.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_BEST_PRACTICE, "numerical")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.SYMBOLIC_VS_NUMERIC_MISMATCH_KEY)
            .setName("Symbolic and numeric code should not be mixed carelessly")
            .setHtmlDescription("<p>Mixing symbolic operations with numeric ones can cause unexpected results.</p>"
                + "<p>Use N[] explicitly when needed.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_BEST_PRACTICE, "numerical")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.GRAPHICS_OPTIONS_EXCESSIVE_KEY)
            .setName("Graphics options should not be excessive")
            .setHtmlDescription("<p>Graphics with more than 20 option settings are hard to maintain.</p>"
                + "<p>Extract common styles into themes.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, TAG_MAINTAINABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.PLOT_WITHOUT_LABELS_KEY)
            .setName("Plots should have axis labels")
            .setHtmlDescription("<p>Plots without AxesLabel or FrameLabel are hard to interpret.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, "visualization")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.DATASET_WITHOUT_HEADERS_KEY)
            .setName("Datasets should have column headers")
            .setHtmlDescription("<p>Datasets without named columns are hard to work with.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRulesDefinition.ASSOCIATION_KEY_NOT_STRING_KEY)
            .setName("Association keys should be strings for clarity")
            .setHtmlDescription("<p>Using symbols or integers as Association keys is less clear than strings.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, TAG_CONVENTION)
            .setStatus(RuleStatus.READY);
    }
}
