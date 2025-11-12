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
    private static final String TAG_NAMING = "naming";
    private static final String TAG_PATTERNS = "patterns";

    private static final String CONFUSING = "confusing";

    private StyleAndConventionsRulesDefinition() {
        // Utility class
    }

    /**
     * Register all 70 additional code smell rules plus 32 ECL-style rules.
     */
    public static void defineRules(NewRepository repository) {
        defineStyleAndFormattingRules(repository);      // 15 rules
        defineNamingConventionRules(repository);        // 15 rules
        defineComplexityRules(repository);              // 10 rules
        defineMaintainabilityRules(repository);         // 15 rules
        defineBestPracticesRules(repository);           // 15 rules
        defineECLStyleRules(repository);                // 32 new rules from CODING_STANDARDS.md
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

    // ===== ECL-STYLE RULES FROM CODING_STANDARDS.md (32 rules) =====

    private static void defineECLStyleRules(NewRepository repository) {
        defineECLSyntaxRules(repository);        // 18 rules
        defineECLVariableRules(repository);      // 2 rules
        defineECLFunctionRules(repository);      // 6 rules
        defineECLOrganizationRules(repository);  // 2 CRITICAL rules
        defineECLPatternRules(repository);       // 4 rules
    }

    private static void defineECLSyntaxRules(NewRepository repository) {
        // Syntax and Whitespace (18 rules)

        repository.createRule(MathematicaRuleKeys.BRACKET_SPACING_BEFORE_KEY)
            .setName("Opening brackets should not be preceded by whitespace")
            .setHtmlDescription("<p>Opening brackets should not have whitespace before them.</p>"
                + "<p><strong>Non-compliant:</strong> <code>function [x]</code></p>"
                + "<p><strong>Compliant:</strong> <code>function[x]</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.EGYPTIAN_BRACKETS_WITH_TABS_KEY)
            .setName("Use Egyptian-style bracketing with tabs (not spaces)")
            .setHtmlDescription(
                "<p>Consistent bracketing style improves readability. "
                + "Use Egyptian style (opening brace on same line) with tab indentation.</p>"
                + "<p><strong>Compliant:</strong></p>"
                + "<pre>Module[{x},\n\tx = 1;\n\tx + 2\n]</pre>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.VARIABLE_ASSIGNMENT_IN_MODULE_DEF_KEY)
            .setName("Variables must not be assigned in Module definition")
            .setHtmlDescription("<p>Variables should be declared in Module but assigned in the body.</p>"
                + "<p><strong>Non-compliant:</strong> <code>Module[{x = 1}, ...]</code></p>"
                + "<p><strong>Compliant:</strong> <code>Module[{x}, x = 1; ...]</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_BEST_PRACTICE)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.EXPLICIT_AND_OR_FOR_COMPLEX_BOOLEAN_KEY)
            .setName("Use explicit And[...] and Or[...] for complex boolean logic")
            .setHtmlDescription(
                "<p>For complex boolean expressions spanning multiple lines, "
                + "use explicit And[...] and Or[...] instead of && and ||.</p>"
                + "<p>This improves readability for complex conditions.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY, TAG_CONVENTION)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.MAP_NOT_SHORTHAND_MULTILINE_KEY)
            .setName("Use Map[...] not /@ for multi-line statements")
            .setHtmlDescription("<p>For multi-line Map operations, use the explicit Map[...] form instead of /@.</p>"
                + "<p><strong>Non-compliant:</strong> Multi-line /@ expressions</p>"
                + "<p><strong>Compliant:</strong> <code>Map[function, list]</code> for multi-line</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY, TAG_CONVENTION)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.ERROR_MESSAGE_WITH_SET_KEY)
            .setName("Error messages must be defined with = not :=")
            .setHtmlDescription("<p>Message definitions should use immediate assignment (=) not delayed assignment (:=).</p>"
                + "<p><strong>Compliant:</strong> <code>func::error = \"Error message\";</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_BEST_PRACTICE)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.MODULE_FOR_LOCAL_VARIABLES_KEY)
            .setName("Must use Module to protect local variable space")
            .setHtmlDescription("<p>Use Module, Block, or With to scope local variables and avoid polluting the global namespace.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_BEST_PRACTICE, "scoping")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.CONDITIONAL_FUNCTION_DEFINITION_KEY)
            .setName("Never use conditional function definitions (/; syntax)")
            .setHtmlDescription("<p>Avoid using /; (Condition) in function definitions as it can be unclear.</p>"
                + "<p>Use If or Which inside the function body instead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.DEREFERENCING_SYNTAX_KEY)
            .setName("Must not use dereferencing syntax in code")
            .setHtmlDescription("<p>Avoid dereferencing syntax like <code>$PersonID[Name]</code>.</p>"
                + "<p>Use explicit Download or Lookup instead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.EMPTY_LINE_BETWEEN_CODE_KEY)
            .setName("Should have at least one empty line between code sections")
            .setHtmlDescription("<p>Separate logical sections of code with empty lines for better readability.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.EGYPTIAN_BRACKETS_IF_KEY)
            .setName("If statements should use Egyptian brackets")
            .setHtmlDescription("<p>For consistency, If statements should use Egyptian-style bracketing (opening bracket on same line).</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.SWITCH_WHICH_SEPARATE_LINES_KEY)
            .setName("Switch/Which conditions and returns should be on separate lines")
            .setHtmlDescription("<p>For non-trivial logic, put each Switch/Which condition and result on separate lines.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.EGYPTIAN_BRACKETS_MAP_KEY)
            .setName("Should use Egyptian-style bracketing for explicit Map[...]")
            .setHtmlDescription("<p>When using explicit Map[...], use Egyptian-style bracketing for consistency.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_STYLE, TAG_FORMATTING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.VARIABLE_NAME_LENGTH_KEY)
            .setName("Local variable names should be 3 words or less")
            .setHtmlDescription("<p>Keep variable names concise - 3 words or fewer in camelCase.</p>"
                + "<p><strong>Example:</strong> <code>userProfile</code> instead of <code>currentActiveUserProfileData</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_NAMING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.LIST_MODIFICATION_IN_PLACE_KEY)
            .setName("Code should not modify lists in place (use immutable patterns)")
            .setHtmlDescription("<p>Prefer immutable functional patterns over in-place list modification.</p>"
                + "<p>Use Append, Prepend, Insert instead of AppendTo, PrependTo.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_BEST_PRACTICE, "functional")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.SHORTHAND_SYNTAX_READABLE_KEY)
            .setName("Should use shorthand syntax when readable")
            .setHtmlDescription("<p>Use shorthand syntax (/@ for Map, <| |> for Association) when it improves readability.</p>"
                + "<p>For simple, single-line operations, shorthand is preferred.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_READABILITY)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.NON_LINEAR_EVALUATION_KEY)
            .setName("Avoid non-linear evaluation structures without justification")
            .setHtmlDescription(
                "<p>Composition, Folding, and other non-linear evaluation structures "
                + "can be hard to understand.</p>"
                + "<p>Use them only when they significantly improve code clarity.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY, TAG_COMPLEXITY)
            .setStatus(RuleStatus.READY);
    }

    private static void defineECLVariableRules(NewRepository repository) {
        // Local Variables (2 rules)

        repository.createRule(MathematicaRuleKeys.VARIABLE_FULL_WORDS_KEY)
            .setName("Local variables must use full words (no abbreviations)")
            .setHtmlDescription(
                "<p>Variable names should use complete words, not abbreviations "
                + "(except standard ones like 'ops' and 'qual').</p>"
                + "<p><strong>Non-compliant:</strong> <code>usrNm</code>, <code>cnt</code></p>"
                + "<p><strong>Compliant:</strong> <code>userName</code>, <code>count</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_NAMING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.VARIABLE_NAME_THREE_WORDS_KEY)
            .setName("Variable names should be three words or less")
            .setHtmlDescription(
                "<p>Keep variable names concise and focused - three words or fewer.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_NAMING)
            .setStatus(RuleStatus.READY);
    }

    private static void defineECLFunctionRules(NewRepository repository) {
        // Function Structure (6 rules)

        repository.createRule(MathematicaRuleKeys.PRIVATE_FUNCTION_PACKAGE_SCOPE_KEY)
            .setName("Private functions must only be used within their defining package")
            .setHtmlDescription(
                "<p>Functions with lowercase names (private) should only be called "
                + "within the same package.</p>"
                + "<p>Cross-package usage indicates the function should be public (uppercase) "
                + "or needs refactoring.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, "encapsulation")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.CUSTOM_ASSOCIATIONS_AS_INPUTS_KEY)
            .setName("Functions must not use custom associations/lists of rules as inputs/outputs")
            .setHtmlDescription(
                "<p>Avoid using custom Association structures or lists of rules "
                + "as function parameters.</p>"
                + "<p>Use explicit parameters or proper data structures instead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_BEST_PRACTICE, "api-design")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.GLOBAL_VARIABLE_DOLLAR_PREFIX_KEY)
            .setName("Global variables must start with $ symbol and be unchanging")
            .setHtmlDescription("<p>Global variables should use $ prefix and be constants.</p>"
                + "<p><strong>Example:</strong> <code>$MaxIterations = 1000</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_NAMING, "global")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.FUNCTION_NAME_THREE_WORDS_KEY)
            .setName("Function names should be 3 words or less")
            .setHtmlDescription(
                "<p>Keep function names concise - three words or fewer in PascalCase.</p>"
                + "<p><strong>Example:</strong> <code>GetUserData</code> instead of "
                + "<code>GetCurrentActiveUserProfileData</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_NAMING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.FUNCTION_NAME_LITTER_WORDS_KEY)
            .setName("Function names should avoid litter words")
            .setHtmlDescription(
                "<p>Avoid meaningless words like 'Do', 'Make', 'Get', 'And' in function names.</p>"
                + "<p><strong>Example:</strong> Use <code>CalculateTotal</code> instead of "
                + "<code>DoTotalCalculation</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_CONVENTION, TAG_NAMING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.PURE_FUNCTION_SHORT_OPERATIONS_KEY)
            .setName("Pure functions should be used for short (<1 line) operations")
            .setHtmlDescription(
                "<p>Use pure functions (#1, #2, etc.) only for simple, single-line operations.</p>"
                + "<p>For longer operations, use named functions with Function[{x, y}, ...].</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_READABILITY, TAG_CONVENTION)
            .setStatus(RuleStatus.READY);
    }

    private static void defineECLOrganizationRules(NewRepository repository) {
        // Code Organization (2 CRITICAL rules)

        repository.createRule(MathematicaRuleKeys.TIME_CONSTRAINED_USAGE_KEY)
            .setName("Must not use TimeConstrained unless absolutely necessary")
            .setHtmlDescription(
                "<p><strong>CRITICAL:</strong> TimeConstrained can kill WSTP (WebServices) "
                + "programs.</p>"
                + "<p>Only use when absolutely necessary and with extreme caution in production "
                + "code.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("critical", "reliability", "wstp")
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.PATTERN_LOADING_ORDER_KEY)
            .setName("Pattern functions must be loaded ahead of function definitions")
            .setHtmlDescription(
                "<p><strong>CRITICAL:</strong> Pattern definitions must be loaded before "
                + "functions that use them.</p>"
                + "<p>Otherwise, the patterns won't be available at function definition time.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("critical", TAG_PATTERNS, "initialization")
            .setStatus(RuleStatus.READY);
    }

    private static void defineECLPatternRules(NewRepository repository) {
        // Patterns (4 rules)

        repository.createRule(MathematicaRuleKeys.PATTERN_NAME_ENDS_WITH_P_KEY)
            .setName("Pattern definition names must end in uppercase P")
            .setHtmlDescription("<p>Pattern definitions should end with uppercase 'P' by convention.</p>"
                + "<p><strong>Example:</strong> <code>UserP = _String | _Integer</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_PATTERNS, TAG_NAMING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.PATTERN_TEST_NAME_ENDS_WITH_Q_KEY)
            .setName("Pattern test function names must end in uppercase Q")
            .setHtmlDescription(
                "<p>Pattern test functions (predicates) should end with uppercase 'Q' "
                + "by convention.</p>"
                + "<p><strong>Example:</strong> <code>ValidUserQ[x_] := StringQ[x] "
                + "&& StringLength[x] > 0</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_PATTERNS, TAG_NAMING)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.ENUMERATED_PATTERN_SYMBOLS_KEY)
            .setName("Enumerated type patterns must use symbols, not strings")
            .setHtmlDescription(
                "<p>For enumerated types in patterns, use symbols instead of strings.</p>"
                + "<p><strong>Example:</strong> <code>ColorP = Red | Green | Blue</code> not "
                + "<code>\"Red\" | \"Green\" | \"Blue\"</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_PATTERNS)
            .setStatus(RuleStatus.READY);

        repository.createRule(MathematicaRuleKeys.PATTERN_GENERATING_FUNCTION_ENDS_WITH_P_KEY)
            .setName("Pattern-generating functions must end in uppercase P")
            .setHtmlDescription(
                "<p>Functions that generate patterns should also end with 'P'.</p>"
                + "<p><strong>Example:</strong> <code>RangeP[min_, max_] := "
                + "_?((# >= min && # <= max)&)</code></p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_CONVENTION, TAG_PATTERNS, TAG_NAMING)
            .setStatus(RuleStatus.READY);
    }
}
