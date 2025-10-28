package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.mathematica.MathematicaLanguage;

/**
 * Defines code quality rules for Mathematica.
 */
public class MathematicaRulesDefinition implements RulesDefinition {

    public static final String REPOSITORY_KEY = "mathematica";
    public static final String REPOSITORY_NAME = "SonarAnalyzer";

    // Rule keys - Code Smells
    public static final String COMMENTED_CODE_KEY = "CommentedCode";
    public static final String MAGIC_NUMBER_KEY = "MagicNumber";
    public static final String TODO_FIXME_KEY = "TodoFixme";
    public static final String EMPTY_BLOCK_KEY = "EmptyBlock";
    public static final String FUNCTION_LENGTH_KEY = "FunctionLength";
    public static final String FILE_LENGTH_KEY = "FileLength";
    public static final String EMPTY_CATCH_KEY = "EmptyCatchBlock";
    public static final String DEBUG_CODE_KEY = "DebugCode";

    // Rule keys - Security Vulnerabilities
    public static final String HARDCODED_CREDENTIALS_KEY = "HardcodedCredentials";
    public static final String COMMAND_INJECTION_KEY = "CommandInjection";
    public static final String SQL_INJECTION_KEY = "SqlInjection";
    public static final String CODE_INJECTION_KEY = "CodeInjection";
    public static final String PATH_TRAVERSAL_KEY = "PathTraversal";
    public static final String WEAK_CRYPTOGRAPHY_KEY = "WeakCryptography";
    public static final String SSRF_KEY = "ServerSideRequestForgery";
    public static final String INSECURE_DESERIALIZATION_KEY = "InsecureDeserialization";

    // Rule keys - Bugs (Reliability)
    public static final String DIVISION_BY_ZERO_KEY = "DivisionByZero";
    public static final String ASSIGNMENT_IN_CONDITIONAL_KEY = "AssignmentInConditional";
    public static final String LIST_INDEX_OUT_OF_BOUNDS_KEY = "ListIndexOutOfBounds";
    public static final String INFINITE_RECURSION_KEY = "InfiniteRecursion";
    public static final String UNREACHABLE_PATTERN_KEY = "UnreachablePattern";

    // Rule keys - Security Hotspots
    public static final String FILE_UPLOAD_VALIDATION_KEY = "FileUploadValidation";
    public static final String EXTERNAL_API_SAFEGUARDS_KEY = "ExternalApiSafeguards";
    public static final String CRYPTO_KEY_GENERATION_KEY = "CryptoKeyGeneration";

    // Rule keys - New Code Smells (Phase 2)
    public static final String UNUSED_VARIABLES_KEY = "UnusedVariables";
    public static final String DUPLICATE_FUNCTION_KEY = "DuplicateFunctionDefinition";
    public static final String TOO_MANY_PARAMETERS_KEY = "TooManyParameters";
    public static final String DEEPLY_NESTED_KEY = "DeeplyNestedConditionals";
    public static final String MISSING_DOCUMENTATION_KEY = "MissingDocumentation";
    public static final String INCONSISTENT_NAMING_KEY = "InconsistentNaming";
    public static final String IDENTICAL_BRANCHES_KEY = "IdenticalBranches";
    public static final String EXPRESSION_TOO_COMPLEX_KEY = "ExpressionTooComplex";
    public static final String DEPRECATED_FUNCTION_KEY = "DeprecatedFunction";
    public static final String EMPTY_STATEMENT_KEY = "EmptyStatement";

    // Rule keys - New Bugs (Phase 2)
    public static final String FLOATING_POINT_EQUALITY_KEY = "FloatingPointEquality";
    public static final String FUNCTION_WITHOUT_RETURN_KEY = "FunctionWithoutReturn";
    public static final String VARIABLE_BEFORE_ASSIGNMENT_KEY = "VariableBeforeAssignment";
    public static final String OFF_BY_ONE_KEY = "OffByOne";
    public static final String INFINITE_LOOP_KEY = "InfiniteLoop";
    public static final String MISMATCHED_DIMENSIONS_KEY = "MismatchedDimensions";
    public static final String TYPE_MISMATCH_KEY = "TypeMismatch";
    public static final String SUSPICIOUS_PATTERN_KEY = "SuspiciousPattern";

    // Rule keys - New Vulnerabilities (Phase 2)
    public static final String UNSAFE_SYMBOL_KEY = "UnsafeSymbol";
    public static final String XXE_KEY = "XmlExternalEntity";
    public static final String MISSING_SANITIZATION_KEY = "MissingSanitization";
    public static final String INSECURE_RANDOM_EXPANDED_KEY = "InsecureRandomExpanded";

    // Rule keys - New Security Hotspots (Phase 2)
    public static final String NETWORK_OPERATIONS_KEY = "NetworkOperations";
    public static final String FILE_SYSTEM_MODIFICATIONS_KEY = "FileSystemModifications";
    public static final String ENVIRONMENT_VARIABLE_KEY = "EnvironmentVariable";

    // Rule keys - Phase 3: Performance Issues (8 rules)
    public static final String APPEND_IN_LOOP_KEY = "AppendInLoop";
    public static final String REPEATED_FUNCTION_CALLS_KEY = "RepeatedFunctionCalls";
    public static final String STRING_CONCAT_IN_LOOP_KEY = "StringConcatInLoop";
    public static final String UNCOMPILED_NUMERICAL_KEY = "UncompiledNumerical";
    public static final String PACKED_ARRAY_BREAKING_KEY = "PackedArrayBreaking";
    public static final String NESTED_MAP_TABLE_KEY = "NestedMapTable";
    public static final String LARGE_TEMP_EXPRESSIONS_KEY = "LargeTempExpressions";
    public static final String PLOT_IN_LOOP_KEY = "PlotInLoop";

    // Rule keys - Phase 3: Pattern Matching Issues (5 rules)
    public static final String MISSING_PATTERN_TEST_KEY = "MissingPatternTest";
    public static final String PATTERN_BLANKS_MISUSE_KEY = "PatternBlanksMisuse";
    public static final String SET_DELAYED_CONFUSION_KEY = "SetDelayedConfusion";
    public static final String SYMBOL_NAME_COLLISION_KEY = "SymbolNameCollision";
    public static final String BLOCK_MODULE_MISUSE_KEY = "BlockModuleMisuse";

    // Rule keys - Phase 3: Best Practices (7 rules)
    public static final String GENERIC_VARIABLE_NAMES_KEY = "GenericVariableNames";
    public static final String MISSING_USAGE_MESSAGE_KEY = "MissingUsageMessage";
    public static final String MISSING_OPTIONS_PATTERN_KEY = "MissingOptionsPattern";
    public static final String SIDE_EFFECTS_NAMING_KEY = "SideEffectsNaming";
    public static final String COMPLEX_BOOLEAN_KEY = "ComplexBooleanExpression";
    public static final String UNPROTECTED_SYMBOLS_KEY = "UnprotectedPublicSymbols";
    public static final String MISSING_RETURN_KEY = "MissingReturnInConditional";

    // Rule keys - Phase 3: Security & Safety (3 rules)
    public static final String UNSAFE_CLOUD_DEPLOY_KEY = "UnsafeCloudDeploy";
    public static final String DYNAMIC_INJECTION_KEY = "DynamicContentInjection";
    public static final String IMPORT_WITHOUT_FORMAT_KEY = "ImportWithoutFormat";

    // Rule keys - Phase 3: Resource Management (2 rules)
    public static final String UNCLOSED_FILE_HANDLE_KEY = "UnclosedFileHandle";
    public static final String GROWING_DEFINITION_CHAIN_KEY = "GrowingDefinitionChain";

    @Override
    public void define(Context context) {
        NewRepository repository = context
            .createRepository(REPOSITORY_KEY, MathematicaLanguage.KEY)
            .setName(REPOSITORY_NAME);

        // Define the commented-out code rule
        repository.createRule(COMMENTED_CODE_KEY)
            .setName("Sections of code should not be commented out")
            .setHtmlDescription(
                "<p>Programmers should not comment out code as it bloats programs and reduces readability.</p>" +
                "<p>Unused code should be deleted and can be retrieved from source control history if required.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* result = Solve[equation, x]; *)\n" +
                "(* \n" +
                "oldFunction[x_] := x^2 + 3x - 1;\n" +
                "*)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Calculate the result using a new algorithm *)\n" +
                "result = ImprovedSolve[equation, x];\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "clutter");

        // Define the magic number rule
        repository.createRule(MAGIC_NUMBER_KEY)
            .setName("Magic numbers should not be used")
            .setHtmlDescription(
                "<p>Magic numbers are unexplained numeric literals that make code harder to understand and maintain.</p>" +
                "<p>Replace magic numbers with named constants to improve readability.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "area = radius * 3.14159;\n" +
                "threshold = 42;\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "pi = 3.14159;\n" +
                "area = radius * pi;\n" +
                "defaultThreshold = 42;\n" +
                "threshold = defaultThreshold;\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability");

        // Define the TODO/FIXME comment rule
        repository.createRule(TODO_FIXME_KEY)
            .setName("Track TODO and FIXME comments")
            .setHtmlDescription(
                "<p>TODO and FIXME comments indicate incomplete or problematic code that needs attention.</p>" +
                "<p>These should be tracked and resolved systematically.</p>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "(* TODO: Add error handling for edge cases *)\n" +
                "(* FIXME: This algorithm has performance issues *)\n" +
                "</pre>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("todo");

        // Define the empty block rule
        repository.createRule(EMPTY_BLOCK_KEY)
            .setName("Empty blocks should be removed")
            .setHtmlDescription(
                "<p>Empty code blocks are usually a sign of incomplete implementation or unnecessary code.</p>" +
                "<p>They should be either filled with proper logic or removed.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Module[{}, ]\n" +
                "If[condition, action, ]\n" +
                "Block[{x}, ]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Module[{result}, result = DoSomething[]]\n" +
                "If[condition, action, defaultAction]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious");

        // Define the function length rule
        repository.createRule(FUNCTION_LENGTH_KEY)
            .setName("Functions should not be too long")
            .setHtmlDescription(
                "<p>Long functions are hard to understand, test, and maintain.</p>" +
                "<p>Consider splitting large functions into smaller, focused functions.</p>" +
                "<p>Default maximum: 150 lines (configurable via sonar.mathematica.function.maximumLines)</p>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Define the file length rule
        repository.createRule(FILE_LENGTH_KEY)
            .setName("Files should not be too long")
            .setHtmlDescription(
                "<p>Files containing too many lines of code are difficult to navigate and maintain.</p>" +
                "<p>Consider splitting large files into multiple focused modules.</p>" +
                "<p>Default maximum: 1000 lines (configurable via sonar.mathematica.file.maximumLines)</p>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Define the empty catch block rule
        repository.createRule(EMPTY_CATCH_KEY)
            .setName("Exceptions should not be silently ignored")
            .setHtmlDescription(
                "<p>Empty exception handlers (catch blocks) hide errors and make debugging extremely difficult.</p>" +
                "<p>This is particularly dangerous for security-related operations where failures should be logged and monitored.</p>" +
                "<p>Always handle exceptions appropriately by logging them, re-throwing them, or taking corrective action.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Silently ignoring all errors *)\n" +
                "Check[riskyOperation[], $Failed];\n" +
                "\n" +
                "(* Catching but not handling *)\n" +
                "Catch[dangerousCode[]; $Failed];\n" +
                "\n" +
                "(* Quiet suppresses messages without logging *)\n" +
                "Quiet[securityCheck[]];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Log the error before returning default *)\n" +
                "Check[riskyOperation[],\n" +
                "  (Print[\"Error in riskyOperation: \", $MessageList]; $Failed)\n" +
                "];\n" +
                "\n" +
                "(* Handle specific error cases *)\n" +
                "result = Check[operation[],\n" +
                "  If[$MessageList =!= {},\n" +
                "    LogError[\"Operation failed\", $MessageList];\n" +
                "    NotifyAdmin[];\n" +
                "    $Failed\n" +
                "  ]\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/391.html'>CWE-391</a> - Unchecked Error Condition</li>" +
                "<li><a href='https://owasp.org/Top10/A09_2021-Security_Logging_and_Monitoring_Failures/'>OWASP Top 10 2021 A09</a> - Security Logging and Monitoring Failures</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("error-handling", "security", "owasp");

        // Define the debug code rule
        repository.createRule(DEBUG_CODE_KEY)
            .setName("Debug code should not be left in production")
            .setHtmlDescription(
                "<p>Debug statements left in production code can expose sensitive information, degrade performance, " +
                "and indicate incomplete development.</p>" +
                "<p>Print statements, trace functions, and debug flags should be removed before deployment.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Debug print statements *)\n" +
                "Print[\"User password: \", userPassword];\n" +
                "Echo[sensitiveData, \"Debug:\"];\n" +
                "\n" +
                "(* Debug tracing *)\n" +
                "TracePrint[securityFunction[credentials]];\n" +
                "Trace[authenticationLogic[]];\n" +
                "\n" +
                "(* Debug monitoring *)\n" +
                "Monitor[calculation[], progress];\n" +
                "\n" +
                "(* Debug messages enabled *)\n" +
                "$DebugMessages = True;\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use proper logging instead *)\n" +
                "If[$DevelopmentMode,\n" +
                "  WriteLog[\"Authentication attempt for user: \" <> username]\n" +
                "];\n" +
                "\n" +
                "(* Or remove debug code entirely *)\n" +
                "result = securityFunction[credentials];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/489.html'>CWE-489</a> - Active Debug Code</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/215.html'>CWE-215</a> - Information Exposure Through Debug Information</li>" +
                "<li><a href='https://owasp.org/Top10/A05_2021-Security_Misconfiguration/'>OWASP Top 10 2021 A05</a> - Security Misconfiguration</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("security", "owasp", "production-readiness");

        // ===== SECURITY RULES =====

        // Define hardcoded credentials rule
        repository.createRule(HARDCODED_CREDENTIALS_KEY)
            .setName("Credentials should not be hard-coded")
            .setHtmlDescription(
                "<p>Hard-coded credentials are a critical security risk. If the code is shared or leaked, " +
                "attackers can gain unauthorized access to systems and data.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "apiKey = \"sk_live_1234567890abcdef\";\n" +
                "password = \"mySecretPassword\";\n" +
                "awsAccessKey = \"AKIAIOSFODNN7EXAMPLE\";\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Load from environment or secure config *)\n" +
                "apiKey = Environment[\"API_KEY\"];\n" +
                "password = Import[\"!security-manager get-password\", \"String\"];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/798.html'>CWE-798</a> - Use of Hard-coded Credentials</li>" +
                "<li><a href='https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/'>OWASP Top 10 2021 A07</a></li>" +
                "</ul>"
            )
            .setSeverity("BLOCKER")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "security");

        // Define command injection rule
        repository.createRule(COMMAND_INJECTION_KEY)
            .setName("OS commands should not be constructed from user input")
            .setHtmlDescription(
                "<p>Constructing OS commands from user-controlled data can lead to command injection vulnerabilities.</p>" +
                "<p>Attackers can execute arbitrary system commands, potentially compromising the entire system.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Run[\"ls \" &lt;&gt; userInput];\n" +
                "RunProcess[{\"sh\", \"-c\", \"grep \" &lt;&gt; searchTerm}];\n" +
                "Import[\"!\" &lt;&gt; command, \"Text\"];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use safe APIs, validate input *)\n" +
                "files = FileNames[validatedPattern];\n" +
                "RunProcess[{\"grep\", searchTerm, \"file.txt\"}]; (* Array form is safer *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/78.html'>CWE-78</a> - OS Command Injection</li>" +
                "<li><a href='https://owasp.org/Top10/A03_2021-Injection/'>OWASP Top 10 2021 A03</a> - Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "injection", "security");

        // Define SQL injection rule
        repository.createRule(SQL_INJECTION_KEY)
            .setName("SQL queries should not be constructed from user input")
            .setHtmlDescription(
                "<p>Concatenating user input into SQL queries enables SQL injection attacks.</p>" +
                "<p>Use parameterized queries or prepared statements instead.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "SQLExecute[conn, \"SELECT * FROM users WHERE id=\" &lt;&gt; userId];\n" +
                "SQLSelect[conn, \"DELETE FROM data WHERE name='\" &lt;&gt; userName &lt;&gt; \"'\"];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use parameterized queries *)\n" +
                "SQLExecute[conn, \"SELECT * FROM users WHERE id=?\", {userId}];\n" +
                "SQLSelect[conn, SQLColumn[\"id\"], SQLTable[\"users\"], SQLWhere[\"id\" == userId]];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/89.html'>CWE-89</a> - SQL Injection</li>" +
                "<li><a href='https://owasp.org/Top10/A03_2021-Injection/'>OWASP Top 10 2021 A03</a> - Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "sql", "injection", "security");

        // Define code injection rule
        repository.createRule(CODE_INJECTION_KEY)
            .setName("Code should not be evaluated from user input")
            .setHtmlDescription(
                "<p>Using ToExpression or similar functions on user-controlled data allows code injection attacks.</p>" +
                "<p>Attackers can execute arbitrary Mathematica code with your application's privileges.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = ToExpression[userInput];\n" +
                "Evaluate[StringToExpression[formulaFromWeb]];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Parse and validate input, use safe evaluation *)\n" +
                "(* Only allow specific whitelisted functions *)\n" +
                "If[StringMatchQ[input, SafePattern], ToExpression[input], $Failed]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li>" +
                "<li><a href='https://owasp.org/Top10/A03_2021-Injection/'>OWASP Top 10 2021 A03</a> - Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "injection", "security");

        // Define path traversal rule
        repository.createRule(PATH_TRAVERSAL_KEY)
            .setName("File paths should not be constructed from user input")
            .setHtmlDescription(
                "<p>Constructing file paths from user input without validation can lead to path traversal attacks.</p>" +
                "<p>Attackers can access files outside the intended directory using sequences like '../'.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "data = Import[baseDir &lt;&gt; userFileName];\n" +
                "Export[outputPath &lt;&gt; requestedFile, content];\n" +
                "(* User supplies: \"../../etc/passwd\" *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Validate and sanitize paths *)\n" +
                "safeFileName = FileNameTake[userFileName]; (* Only basename *)\n" +
                "fullPath = FileNameJoin[{baseDir, safeFileName}];\n" +
                "If[StringStartsQ[fullPath, baseDir], Import[fullPath], $Failed]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/22.html'>CWE-22</a> - Path Traversal</li>" +
                "<li><a href='https://owasp.org/Top10/A01_2021-Broken_Access_Control/'>OWASP Top 10 2021 A01</a> - Broken Access Control</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "path-traversal", "security");

        // Define weak cryptography rule
        repository.createRule(WEAK_CRYPTOGRAPHY_KEY)
            .setName("Weak cryptographic algorithms should not be used")
            .setHtmlDescription(
                "<p>Using weak or broken cryptographic algorithms compromises data security.</p>" +
                "<p>MD5 and SHA-1 are cryptographically broken and should not be used for security purposes. " +
                "Using Mathematica's Random[] function for security tokens is unsafe as it's not cryptographically secure.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Weak hash algorithms *)\n" +
                "hash = Hash[data, \"MD5\"];\n" +
                "signature = Hash[message, \"SHA1\"];\n" +
                "\n" +
                "(* Using Random[] for security tokens *)\n" +
                "token = ToString[Random[Integer, {10^20, 10^21}]];\n" +
                "key = Table[Random[], {16}];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use strong hash algorithms *)\n" +
                "hash = Hash[data, \"SHA256\"];\n" +
                "signature = Hash[message, \"SHA512\"];\n" +
                "\n" +
                "(* Use cryptographically secure random *)\n" +
                "token = IntegerString[RandomInteger[{10^20, 10^21}], 16];\n" +
                "key = RandomInteger[{0, 255}, 16]; (* Use RandomInteger, not Random *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/327.html'>CWE-327</a> - Use of a Broken or Risky Cryptographic Algorithm</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/338.html'>CWE-338</a> - Use of Cryptographically Weak PRNG</li>" +
                "<li><a href='https://owasp.org/Top10/A02_2021-Cryptographic_Failures/'>OWASP Top 10 2021 A02</a> - Cryptographic Failures</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "cryptography", "security");

        // Define SSRF rule
        repository.createRule(SSRF_KEY)
            .setName("URLs should not be constructed from user input")
            .setHtmlDescription(
                "<p>Server-Side Request Forgery (SSRF) vulnerabilities allow attackers to make requests to unintended destinations.</p>" +
                "<p>Concatenating user input into URLs used by URLFetch, URLRead, Import, or ServiceExecute " +
                "can allow attackers to access internal services, cloud metadata endpoints, or other restricted resources.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* URL construction from user input *)\n" +
                "data = URLFetch[\"https://api.example.com/\" &lt;&gt; userEndpoint];\n" +
                "content = URLRead[baseURL &lt;&gt; userPath];\n" +
                "result = Import[\"https://\" &lt;&gt; userDomain &lt;&gt; \"/data.json\"];\n" +
                "response = ServiceExecute[service, \"Query\", {\"url\" -&gt; userURL}];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Validate against whitelist *)\n" +
                "allowedEndpoints = {\"users\", \"posts\", \"comments\"};\n" +
                "If[MemberQ[allowedEndpoints, userEndpoint],\n" +
                "  URLFetch[\"https://api.example.com/\" &lt;&gt; userEndpoint],\n" +
                "  $Failed\n" +
                "];\n" +
                "\n" +
                "(* Validate URL format and domain *)\n" +
                "If[StringMatchQ[userURL, \"https://trusted-domain.com/*\"],\n" +
                "  Import[userURL],\n" +
                "  $Failed\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/918.html'>CWE-918</a> - Server-Side Request Forgery</li>" +
                "<li><a href='https://owasp.org/Top10/A10_2021-Server-Side_Request_Forgery_%28SSRF%29/'>OWASP Top 10 2021 A10</a> - SSRF</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "ssrf", "security");

        // Define insecure deserialization rule
        repository.createRule(INSECURE_DESERIALIZATION_KEY)
            .setName("Deserialization of untrusted data should be avoided")
            .setHtmlDescription(
                "<p>Deserializing data from untrusted sources can lead to remote code execution and other attacks.</p>" +
                "<p>Mathematica's .mx and .wdx files can contain arbitrary code. Loading packages with Get[] from " +
                "user-controlled paths or untrusted URLs allows attackers to execute malicious code.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Loading serialized data from untrusted sources *)\n" +
                "data = Import[userFile, \"MX\"]; (* .mx files can execute code *)\n" +
                "dataset = Import[uploadedFile, \"WDX\"];\n" +
                "\n" +
                "(* Loading packages from user input *)\n" +
                "Get[userPackagePath]; (* Executes code from file *)\n" +
                "Get[\"http://\" &lt;&gt; userDomain &lt;&gt; \"/package.m\"];\n" +
                "\n" +
                "(* Evaluating expressions from strings *)\n" +
                "expr = ToExpression[Import[untrustedURL, \"String\"]];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use safe formats for untrusted data *)\n" +
                "data = Import[userFile, \"JSON\"]; (* JSON is data-only *)\n" +
                "data = Import[userFile, \"CSV\"];\n" +
                "\n" +
                "(* Validate file paths against whitelist *)\n" +
                "trustedPackages = {\"/usr/local/mathematica/packages/TrustedPackage.m\"};\n" +
                "If[MemberQ[trustedPackages, packagePath],\n" +
                "  Get[packagePath],\n" +
                "  $Failed\n" +
                "];\n" +
                "\n" +
                "(* Verify integrity with checksums before loading *)\n" +
                "If[Hash[Import[file, \"String\"], \"SHA256\"] === expectedHash,\n" +
                "  Get[file],\n" +
                "  $Failed\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/502.html'>CWE-502</a> - Deserialization of Untrusted Data</li>" +
                "<li><a href='https://owasp.org/Top10/A08_2021-Software_and_Data_Integrity_Failures/'>OWASP Top 10 2021 A08</a> - Software and Data Integrity Failures</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "deserialization", "security");

        // ===== BUG RULES (Reliability) =====

        // Define division by zero rule
        repository.createRule(DIVISION_BY_ZERO_KEY)
            .setName("Division operations should check for zero divisors")
            .setHtmlDescription(
                "<p>Division by zero causes runtime errors and program crashes.</p>" +
                "<p>Always validate that divisors are not zero before performing division operations.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = numerator / denominator;\n" +
                "value = x / (y - 5);  (* What if y == 5? *)\n" +
                "ratio = total / count;  (* What if count == 0? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Check before dividing *)\n" +
                "If[denominator != 0, numerator / denominator, $Failed];\n" +
                "\n" +
                "(* Or use Mathematica's safe division *)\n" +
                "result = Check[numerator / denominator, $Failed];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/369.html'>CWE-369</a> - Divide By Zero</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "error-handling");

        // Define assignment in conditional rule
        repository.createRule(ASSIGNMENT_IN_CONDITIONAL_KEY)
            .setName("Assignments should not be used in conditional expressions")
            .setHtmlDescription(
                "<p>Using assignment (=) instead of comparison (==, ===) in conditionals is a common bug.</p>" +
                "<p>This causes unintended assignment and always evaluates to True.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Assignment instead of comparison *)\n" +
                "If[x = 5, doSomething[]];  (* Sets x to 5, always True! *)\n" +
                "\n" +
                "While[status = \"running\", process[]];  (* Always loops! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use comparison operators *)\n" +
                "If[x == 5, doSomething[]];\n" +
                "If[x === 5, doSomething[]];  (* Strict equality *)\n" +
                "\n" +
                "While[status == \"running\", process[]];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/480.html'>CWE-480</a> - Use of Incorrect Operator</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "logic-error");

        // Define list index out of bounds rule
        repository.createRule(LIST_INDEX_OUT_OF_BOUNDS_KEY)
            .setName("List access should be bounds-checked")
            .setHtmlDescription(
                "<p>Accessing list elements without bounds checking can cause Part::partw errors at runtime.</p>" +
                "<p>Always verify index is within valid range before accessing list elements.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* No bounds checking *)\n" +
                "element = myList[[index]];\n" +
                "value = data[[userInput]];\n" +
                "first = items[[1]];  (* What if items is empty? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Check bounds before access *)\n" +
                "If[1 <= index <= Length[myList],\n" +
                "  myList[[index]],\n" +
                "  $Failed\n" +
                "];\n" +
                "\n" +
                "(* Use safe accessors *)\n" +
                "element = If[Length[items] > 0, First[items], $Failed];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/125.html'>CWE-125</a> - Out-of-bounds Read</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "error-handling");

        // Define infinite recursion rule
        repository.createRule(INFINITE_RECURSION_KEY)
            .setName("Recursive functions must have a base case")
            .setHtmlDescription(
                "<p>Recursive functions without proper base cases cause stack overflow errors.</p>" +
                "<p>Every recursive function must have at least one termination condition.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* No base case - infinite recursion! *)\n" +
                "factorial[n_] := n * factorial[n - 1];\n" +
                "\n" +
                "(* Base case never reached *)\n" +
                "count[x_] := If[x > 100, x, count[x + 1]];  (* But what if x starts > 100? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Proper base case *)\n" +
                "factorial[0] = 1;\n" +
                "factorial[n_] := n * factorial[n - 1];\n" +
                "\n" +
                "(* Multiple base cases *)\n" +
                "fibonacci[0] = 0;\n" +
                "fibonacci[1] = 1;\n" +
                "fibonacci[n_] := fibonacci[n-1] + fibonacci[n-2];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/674.html'>CWE-674</a> - Uncontrolled Recursion</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "stack-overflow");

        // Define unreachable pattern rule
        repository.createRule(UNREACHABLE_PATTERN_KEY)
            .setName("Pattern definitions should not be unreachable")
            .setHtmlDescription(
                "<p>When multiple patterns are defined for the same function, more specific patterns must come before general ones.</p>" +
                "<p>Otherwise, the specific patterns will never match because the general pattern catches everything first.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* General pattern first - specific patterns never match! *)\n" +
                "process[x_] := defaultProcess[x];\n" +
                "process[x_Integer] := integerProcess[x];  (* NEVER CALLED *)\n" +
                "process[x_String] := stringProcess[x];    (* NEVER CALLED *)\n" +
                "\n" +
                "(* Overlapping patterns *)\n" +
                "calculate[n_] := n^2;\n" +
                "calculate[n_?Positive] := n^3;  (* NEVER CALLED - all numbers match n_ first *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Specific patterns first, general last *)\n" +
                "process[x_Integer] := integerProcess[x];\n" +
                "process[x_String] := stringProcess[x];\n" +
                "process[x_] := defaultProcess[x];  (* Catch-all last *)\n" +
                "\n" +
                "(* Most specific first *)\n" +
                "calculate[n_?Positive] := n^3;\n" +
                "calculate[n_] := n^2;\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "logic-error", "pattern-matching");

        // ===== SECURITY HOTSPOT RULES =====

        // Define file upload validation rule
        repository.createRule(FILE_UPLOAD_VALIDATION_KEY)
            .setName("File uploads should be validated")
            .setHtmlDescription(
                "<p>File uploads from users should be validated for type, size, and content before processing.</p>" +
                "<p>Unvalidated file uploads can lead to malicious file execution, denial of service, or data exfiltration.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper validation is in place.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>File extension is validated against whitelist</li>" +
                "<li>File size is checked and limited</li>" +
                "<li>File content type is verified (not just extension)</li>" +
                "<li>Files are scanned for malware if possible</li>" +
                "<li>Uploaded files are stored outside web root</li>" +
                "<li>File names are sanitized (no path traversal)</li>" +
                "</ul>" +
                "<h2>Example File Operations to Review</h2>" +
                "<pre>\n" +
                "(* Review these operations *)\n" +
                "Import[uploadedFile];  (* What type? Size? Content? *)\n" +
                "Get[userProvidedPath];  (* Could load malicious code! *)\n" +
                "Import[formData[\"file\"], \"MX\"];  (* MX files execute code! *)\n" +
                "</pre>" +
                "<h2>Secure Validation Example</h2>" +
                "<pre>\n" +
                "(* Validate extension *)\n" +
                "allowedExtensions = {\".csv\", \".json\", \".txt\"};\n" +
                "ext = FileExtension[uploadedFile];\n" +
                "If[!MemberQ[allowedExtensions, \".\" <> ext], Return[$Failed]];\n" +
                "\n" +
                "(* Check file size *)\n" +
                "maxSize = 10 * 1024 * 1024;  (* 10MB *)\n" +
                "If[FileSize[uploadedFile] > maxSize, Return[$Failed]];\n" +
                "\n" +
                "(* Use safe import formats only *)\n" +
                "data = Import[uploadedFile, \"CSV\"];  (* CSV is data-only *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/434.html'>CWE-434</a> - Unrestricted Upload of File with Dangerous Type</li>" +
                "<li><a href='https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload'>OWASP</a> - Unrestricted File Upload</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "file-upload", "owasp");

        // Define external API safeguards rule
        repository.createRule(EXTERNAL_API_SAFEGUARDS_KEY)
            .setName("External API calls should have proper safeguards")
            .setHtmlDescription(
                "<p>Calls to external APIs should have proper error handling, timeouts, and rate limiting.</p>" +
                "<p>Without safeguards, API calls can cause performance issues, expose sensitive errors, or enable abuse.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper safeguards are in place.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Timeout is set (don't wait forever)</li>" +
                "<li>Rate limiting is implemented (prevent abuse)</li>" +
                "<li>Errors are caught and logged (don't expose stack traces)</li>" +
                "<li>Sensitive data is not logged (API keys, tokens)</li>" +
                "<li>Retry logic has exponential backoff</li>" +
                "<li>Circuit breaker pattern for failing services</li>" +
                "</ul>" +
                "<h2>Example API Calls to Review</h2>" +
                "<pre>\n" +
                "(* Review these operations *)\n" +
                "URLRead[apiEndpoint];  (* Timeout? Error handling? *)\n" +
                "URLExecute[\"POST\", url, data];  (* Rate limiting? *)\n" +
                "ServiceExecute[service, \"Query\", params];  (* What if service is down? *)\n" +
                "</pre>" +
                "<h2>Secure API Call Example</h2>" +
                "<pre>\n" +
                "(* Add timeout and error handling *)\n" +
                "result = TimeConstrained[\n" +
                "  Check[\n" +
                "    URLRead[apiEndpoint],\n" +
                "    (LogError[\"API call failed\"]; $Failed)\n" +
                "  ],\n" +
                "  30  (* 30 second timeout *)\n" +
                "];\n" +
                "\n" +
                "(* Implement rate limiting *)\n" +
                "If[apiCallCount > maxCallsPerMinute,\n" +
                "  Pause[60];  (* Wait before next call *)\n" +
                "];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/400.html'>CWE-400</a> - Uncontrolled Resource Consumption</li>" +
                "<li><a href='https://owasp.org/www-community/controls/Blocking_Brute_Force_Attacks'>OWASP</a> - Rate Limiting</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "api", "availability");

        // Define crypto key generation rule
        repository.createRule(CRYPTO_KEY_GENERATION_KEY)
            .setName("Cryptographic keys should be generated securely")
            .setHtmlDescription(
                "<p>Cryptographic keys and secrets must be generated using secure methods with sufficient entropy.</p>" +
                "<p>Weak key generation can compromise the entire security of encrypted data.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure secure key generation.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Using RandomInteger (not Random) for cryptographic purposes</li>" +
                "<li>Key length is sufficient (256 bits minimum for symmetric)</li>" +
                "<li>Keys are generated with cryptographically secure randomness</li>" +
                "<li>Keys are stored securely (not in code or logs)</li>" +
                "<li>Keys are rotated regularly</li>" +
                "<li>Consider using established crypto libraries</li>" +
                "</ul>" +
                "<h2>Example Key Generation to Review</h2>" +
                "<pre>\n" +
                "(* Review these operations *)\n" +
                "key = Table[Random[], {16}];  (* Random is NOT cryptographically secure! *)\n" +
                "password = ToString[Random[Integer, {1000, 9999}]];  (* Too short! *)\n" +
                "secret = IntegerString[RandomInteger[999999], 16];  (* Too little entropy! *)\n" +
                "</pre>" +
                "<h2>Secure Key Generation Example</h2>" +
                "<pre>\n" +
                "(* Use RandomInteger with sufficient length *)\n" +
                "aesKey = RandomInteger[{0, 255}, 32];  (* 256-bit key *)\n" +
                "\n" +
                "(* Generate secure token *)\n" +
                "token = IntegerString[RandomInteger[{10^30, 10^31 - 1}], 16];\n" +
                "\n" +
                "(* Store securely, don't log *)\n" +
                "Export[\"/secure/path/key.bin\", aesKey, \"Byte\"];\n" +
                "SystemExecute[\"chmod\", \"600\", \"/secure/path/key.bin\"];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/326.html'>CWE-326</a> - Inadequate Encryption Strength</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/330.html'>CWE-330</a> - Use of Insufficiently Random Values</li>" +
                "<li><a href='https://owasp.org/Top10/A02_2021-Cryptographic_Failures/'>OWASP Top 10 2021 A02</a> - Cryptographic Failures</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "cryptography", "owasp");

        // ===== NEW CODE SMELL RULES (Phase 2) =====

        // Unused Variables
        repository.createRule(UNUSED_VARIABLES_KEY)
            .setName("Variables should not be declared and not used")
            .setHtmlDescription(
                "<p>Variables declared in Module, Block, or With but never used waste memory and reduce code clarity.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Module[{x, y, z},\n" +
                "  x = 5;\n" +
                "  x + 10\n" +
                "];  (* y and z are declared but never used *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Module[{x},\n" +
                "  x = 5;\n" +
                "  x + 10\n" +
                "];\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("unused", "clutter");

        // Duplicate Function Definitions
        repository.createRule(DUPLICATE_FUNCTION_KEY)
            .setName("Functions should not be redefined with same signature")
            .setHtmlDescription(
                "<p>Defining the same function multiple times with the same pattern signature overwrites previous definitions.</p>" +
                "<p>This is usually a mistake and causes confusion about which definition is active.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "myFunc[x_] := x^2;  (* First definition *)\n" +
                "myFunc[y_] := y^3;  (* Overwrites first! Both have same pattern x_ *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use different patterns *)\n" +
                "myFunc[x_Integer] := x^2;\n" +
                "myFunc[x_Real] := x^3;\n" +
                "\n" +
                "(* Or use different function names *)\n" +
                "myFuncSquare[x_] := x^2;\n" +
                "myFuncCube[x_] := x^3;\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("confusing", "pitfall");

        // Too Many Parameters
        repository.createRule(TOO_MANY_PARAMETERS_KEY)
            .setName("Functions should not have too many parameters")
            .setHtmlDescription(
                "<p>Functions with more than 7 parameters are difficult to use and maintain.</p>" +
                "<p>Consider using associations or grouping related parameters into structures.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "processData[name_, age_, address_, phone_, email_, city_, state_, zip_] := ...\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use an Association *)\n" +
                "processData[userData_Association] := ...\n" +
                "processData[<|\"name\" -> \"John\", \"age\" -> 30, ...|>]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Deeply Nested Conditionals
        repository.createRule(DEEPLY_NESTED_KEY)
            .setName("Conditionals should not be nested too deeply")
            .setHtmlDescription(
                "<p>Deeply nested If/Which/Switch statements (more than 3 levels) are difficult to understand and test.</p>" +
                "<p>Consider extracting nested logic into separate functions or using Which for multiple conditions.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[a,\n" +
                "  If[b,\n" +
                "    If[c,\n" +
                "      If[d, result]  (* 4 levels deep! *)\n" +
                "    ]\n" +
                "  ]\n" +
                "]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use Which for multiple conditions *)\n" +
                "Which[\n" +
                "  a && b && c && d, result,\n" +
                "  a && b && c, otherResult,\n" +
                "  True, defaultResult\n" +
                "]\n" +
                "\n" +
                "(* Or extract to helper functions *)\n" +
                "If[a, processA[], defaultResult]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload", "complexity");

        // Missing Documentation
        repository.createRule(MISSING_DOCUMENTATION_KEY)
            .setName("Public functions should be documented")
            .setHtmlDescription(
                "<p>Public functions (starting with uppercase) should have usage documentation.</p>" +
                "<p>This helps users understand what the function does without reading the implementation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "ProcessUserData[data_, options_] := Module[{...}, ...]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* ProcessUserData[data, options] processes user data and returns cleaned result.\n" +
                "   Parameters:\n" +
                "     data: List of user records\n" +
                "     options: Association of processing options\n" +
                "   Returns: Processed data list\n" +
                "*)\n" +
                "ProcessUserData[data_, options_] := Module[{...}, ...]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation");

        // Inconsistent Naming
        repository.createRule(INCONSISTENT_NAMING_KEY)
            .setName("Naming conventions should be consistent")
            .setHtmlDescription(
                "<p>Mixing different naming conventions (camelCase, PascalCase, snake_case) in the same file reduces readability.</p>" +
                "<p>Mathematica convention: PascalCase for public functions, camelCase for private.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "ProcessData[x_] := ...    (* PascalCase *)\n" +
                "calculateResult[y_] := ... (* camelCase *)\n" +
                "get_user_name[] := ...    (* snake_case - inconsistent! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Consistent PascalCase for public *)\n" +
                "ProcessData[x_] := ...\n" +
                "CalculateResult[y_] := ...\n" +
                "GetUserName[] := ...\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("convention");

        // Identical Branches
        repository.createRule(IDENTICAL_BRANCHES_KEY)
            .setName("Conditional branches should not be identical")
            .setHtmlDescription(
                "<p>If/Which with identical then/else branches is a copy-paste error or indicates dead code.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[condition, DoSomething[], DoSomething[]]  (* Both branches identical *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* If both branches are same, condition is useless *)\n" +
                "DoSomething[]\n" +
                "\n" +
                "(* Or fix the copy-paste error *)\n" +
                "If[condition, DoSomething[], DoSomethingElse[]]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious", "pitfall");

        // Expression Too Complex
        repository.createRule(EXPRESSION_TOO_COMPLEX_KEY)
            .setName("Expressions should not be too complex")
            .setHtmlDescription(
                "<p>Single expressions with more than 20 operations should be split into intermediate steps.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = a + b * c - d / e ^ f + g * h - i / j + k * l - m / n + o * p;\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "term1 = b * c + a;\n" +
                "term2 = d / e ^ f;\n" +
                "term3 = g * h - i / j;\n" +
                "result = term1 - term2 + term3;\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("brain-overload");

        // Deprecated Functions
        repository.createRule(DEPRECATED_FUNCTION_KEY)
            .setName("Deprecated functions should not be used")
            .setHtmlDescription(
                "<p>Some Mathematica functions have been deprecated in favor of newer alternatives.</p>" +
                "<p>Using deprecated functions may cause compatibility issues in future versions.</p>" +
                "<h2>Examples of Deprecated Functions</h2>" +
                "<ul>" +
                "<li><code>$RecursionLimit</code> - Use <code>$IterationLimit</code> or explicit checks</li>" +
                "<li><code>Sqrt[-1]</code> pattern - Use <code>I</code> directly</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("obsolete");

        // Empty Statement
        repository.createRule(EMPTY_STATEMENT_KEY)
            .setName("Empty statements should be removed")
            .setHtmlDescription(
                "<p>Empty statements created by double semicolons or misplaced semicolons are usually mistakes.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "x = 5;;  (* Double semicolon *)\n" +
                "If[condition, ;]  (* Empty statement in branch *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "x = 5;\n" +
                "If[condition, DoSomething[]]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("suspicious");

        // ===== NEW BUG RULES (Phase 2) =====

        // Floating Point Equality
        repository.createRule(FLOATING_POINT_EQUALITY_KEY)
            .setName("Floating point numbers should not be tested for equality")
            .setHtmlDescription(
                "<p>Using == or === to compare floating point numbers is unreliable due to rounding errors.</p>" +
                "<p>Use a tolerance-based comparison instead.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[0.1 + 0.2 == 0.3, ...]  (* May be False due to rounding! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use tolerance-based comparison *)\n" +
                "If[Abs[(0.1 + 0.2) - 0.3] < 10^-10, ...]\n" +
                "\n" +
                "(* Or use Mathematica's Chop *)\n" +
                "If[Chop[0.1 + 0.2 - 0.3] == 0, ...]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/1077.html'>CWE-1077</a> - Floating Point Comparison</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "floating-point");

        // Function Without Return
        repository.createRule(FUNCTION_WITHOUT_RETURN_KEY)
            .setName("Functions should return a value")
            .setHtmlDescription(
                "<p>Functions that end with a semicolon return Null, which is usually unintended.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "calculateResult[x_] := (\n" +
                "  result = x^2 + x;\n" +
                "  Print[result];\n" +
                ");  (* Returns Null! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "calculateResult[x_] := (\n" +
                "  result = x^2 + x;\n" +
                "  Print[result];\n" +
                "  result  (* Return the value *)\n" +
                ");\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Variable Before Assignment
        repository.createRule(VARIABLE_BEFORE_ASSIGNMENT_KEY)
            .setName("Variables should not be used before assignment")
            .setHtmlDescription(
                "<p>Using a variable before assigning it a value leads to undefined behavior.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Module[{x, y},\n" +
                "  y = x + 5;  (* x used before being assigned *)\n" +
                "  x = 10;\n" +
                "  y\n" +
                "]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Module[{x, y},\n" +
                "  x = 10;\n" +
                "  y = x + 5;\n" +
                "  y\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Off-by-One
        repository.createRule(OFF_BY_ONE_KEY)
            .setName("Loop ranges should not cause off-by-one errors")
            .setHtmlDescription(
                "<p>Common indexing errors: starting at 0 (Mathematica lists are 1-indexed) or going beyond Length.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Do[Print[list[[i]]], {i, 0, Length[list]}]  (* 0 is invalid! *)\n" +
                "Do[Print[list[[i]]], {i, 1, Length[list] + 1}]  (* Beyond length! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Do[Print[list[[i]]], {i, 1, Length[list]}]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Infinite Loop
        repository.createRule(INFINITE_LOOP_KEY)
            .setName("While loops should have an exit condition")
            .setHtmlDescription(
                "<p>While[True] without Break or Return inside creates an infinite loop.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "While[True, DoSomething[]]  (* No way to exit! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "While[True,\n" +
                "  DoSomething[];\n" +
                "  If[condition, Break[]]\n" +
                "]\n" +
                "\n" +
                "(* Or use proper condition *)\n" +
                "While[!done, DoSomething[]]\n" +
                "</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Mismatched Dimensions
        repository.createRule(MISMATCHED_DIMENSIONS_KEY)
            .setName("Matrix operations should use rectangular arrays")
            .setHtmlDescription(
                "<p>Operations like Transpose, Dot assume rectangular (uniform) arrays.</p>" +
                "<p>Non-rectangular arrays cause errors or unexpected results.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "data = {{1, 2}, {3, 4, 5}};  (* Non-rectangular *)\n" +
                "Transpose[data]  (* Will fail *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Ensure rectangular before matrix ops *)\n" +
                "If[Apply[SameQ, Map[Length, data]],\n" +
                "  Transpose[data],\n" +
                "  $Failed\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Type Mismatch
        repository.createRule(TYPE_MISMATCH_KEY)
            .setName("Operations should use compatible types")
            .setHtmlDescription(
                "<p>Mixing incompatible types in operations leads to errors or unexpected results.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = \"hello\" + 5  (* String + Number error *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "result = \"hello\" <> ToString[5]  (* String concatenation *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability");

        // Suspicious Pattern
        repository.createRule(SUSPICIOUS_PATTERN_KEY)
            .setName("Pattern matching should not have contradictions")
            .setHtmlDescription(
                "<p>Patterns with contradictory constraints will never match or match too broadly.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Impossible constraint *)\n" +
                "func[x_Integer /; x > 10 && x < 5] := ...  (* Never matches *)\n" +
                "\n" +
                "(* Too broad *)\n" +
                "func[___] := ...  (* Matches everything including 0 arguments *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "func[x_Integer /; x > 10] := ...\n" +
                "func[x__] := ...  (* At least one argument *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "pattern-matching");

        // ===== NEW VULNERABILITY RULES (Phase 2) =====

        // Unsafe Symbol Construction
        repository.createRule(UNSAFE_SYMBOL_KEY)
            .setName("Symbol construction from user input should be avoided")
            .setHtmlDescription(
                "<p>Using Symbol[] or ToExpression to dynamically construct function names from user input allows code injection.</p>" +
                "<p>Attackers can control which functions are called with your data.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "funcName = userInput;  (* e.g., \"DeleteFile\" *)\n" +
                "Symbol[funcName][userFile]  (* User controls function call! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use explicit whitelist *)\n" +
                "allowedFunctions = <|\"Save\" -> SaveData, \"Load\" -> LoadData|>;\n" +
                "If[KeyExistsQ[allowedFunctions, userInput],\n" +
                "  allowedFunctions[userInput][userFile],\n" +
                "  $Failed\n" +
                "]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/470.html'>CWE-470</a> - Use of Externally-Controlled Input</li>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "injection", "security");

        // XML External Entity (XXE)
        repository.createRule(XXE_KEY)
            .setName("XML imports should disable external entity processing")
            .setHtmlDescription(
                "<p>Importing XML without disabling external entities allows XML External Entity (XXE) attacks.</p>" +
                "<p>Attackers can read local files, perform SSRF, or cause denial of service.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Import[userFile, \"XML\"]  (* XXE vulnerable! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Disable DTD processing *)\n" +
                "Import[userFile, {\"XML\", \"ProcessDTD\" -> False}]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/611.html'>CWE-611</a> - XML External Entity</li>" +
                "<li><a href='https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing'>OWASP</a> - XXE</li>" +
                "</ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "owasp", "xxe", "security");

        // Missing Input Sanitization
        repository.createRule(MISSING_SANITIZATION_KEY)
            .setName("User input should be sanitized before use with dangerous functions")
            .setHtmlDescription(
                "<p>User input passed directly to dangerous functions without validation enables attacks.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "DeleteFile[userProvidedPath]  (* User could delete anything! *)\n" +
                "SystemOpen[userURL]  (* User could open malicious URLs *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Validate against whitelist *)\n" +
                "allowedFiles = {\"/tmp/file1.txt\", \"/tmp/file2.txt\"};\n" +
                "If[MemberQ[allowedFiles, userPath],\n" +
                "  DeleteFile[userPath],\n" +
                "  $Failed\n" +
                "]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/20.html'>CWE-20</a> - Improper Input Validation</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "validation", "security");

        // Insecure Random (Expanded)
        repository.createRule(INSECURE_RANDOM_EXPANDED_KEY)
            .setName("Secure random should be used for security-sensitive operations")
            .setHtmlDescription(
                "<p>Using Random[] or predictable RandomChoice for session tokens, passwords, or keys is insecure.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Insecure random for tokens *)\n" +
                "sessionToken = StringJoin @@ RandomChoice[CharacterRange[\"a\", \"z\"], 20];\n" +
                "\n" +
                "(* Random[] is not cryptographically secure *)\n" +
                "secretKey = Table[Random[], {32}];\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use RandomInteger for crypto *)\n" +
                "sessionToken = IntegerString[RandomInteger[{10^40, 10^41 - 1}], 16];\n" +
                "secretKey = RandomInteger[{0, 255}, 32];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul>" +
                "<li><a href='https://cwe.mitre.org/data/definitions/338.html'>CWE-338</a> - Weak PRNG</li>" +
                "</ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("cwe", "cryptography", "security");

        // ===== NEW SECURITY HOTSPOT RULES (Phase 2) =====

        // Network Operations
        repository.createRule(NETWORK_OPERATIONS_KEY)
            .setName("Network operations should be reviewed for security")
            .setHtmlDescription(
                "<p>Network operations expose the application to external systems and should be reviewed.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper security measures.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>TLS/SSL is used for sensitive data</li>" +
                "<li>Certificate validation is enabled</li>" +
                "<li>Timeouts are set to prevent hanging</li>" +
                "<li>Authentication is required where appropriate</li>" +
                "<li>Error messages don't leak sensitive information</li>" +
                "</ul>" +
                "<h2>Example Operations to Review</h2>" +
                "<pre>\n" +
                "SocketConnect[\"server.com\", 8080]  (* Check: TLS? Auth? *)\n" +
                "SocketOpen[8080]  (* Check: What data is exposed? *)\n" +
                "WebExecute[session, \"Click\", ...]  (* Check: Auth? Session security? *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "network");

        // File System Modifications
        repository.createRule(FILE_SYSTEM_MODIFICATIONS_KEY)
            .setName("Destructive file operations should be reviewed")
            .setHtmlDescription(
                "<p>Destructive file system operations should be reviewed for security and safety.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure proper safeguards.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Path validation prevents directory traversal</li>" +
                "<li>User permissions are checked</li>" +
                "<li>Critical files are protected</li>" +
                "<li>Operations are logged for audit</li>" +
                "<li>Rollback/undo is possible if needed</li>" +
                "</ul>" +
                "<h2>Example Operations to Review</h2>" +
                "<pre>\n" +
                "DeleteFile[path]  (* Check: Path validated? Logged? *)\n" +
                "RenameFile[old, new]  (* Check: Both paths validated? *)\n" +
                "SetFileDate[file, ...]  (* Check: Why modifying timestamps? *)\n" +
                "CopyFile[src, dst]  (* Check: Destination validated? *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "file-system");

        // Environment Variables
        repository.createRule(ENVIRONMENT_VARIABLE_KEY)
            .setName("Environment variable access should be reviewed")
            .setHtmlDescription(
                "<p>Environment variables may contain secrets and should be reviewed for proper handling.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review this code to ensure secrets are protected.</p>" +
                "<h2>What to Review</h2>" +
                "<p>When you see this issue, verify that:</p>" +
                "<ul>" +
                "<li>Environment variable values are not logged</li>" +
                "<li>Values are not exposed in error messages</li>" +
                "<li>Secrets are not passed to external systems</li>" +
                "<li>Variables are not used in URLs or queries</li>" +
                "<li>Consider using secure secret management instead</li>" +
                "</ul>" +
                "<h2>Example to Review</h2>" +
                "<pre>\n" +
                "apiKey = Environment[\"SECRET_API_KEY\"]  (* Check: Logged anywhere? *)\n" +
                "URLFetch[\"https://api.com?key=\" <> Environment[\"KEY\"]]  (* Exposed in URL! *)\n" +
                "</pre>" +
                "<h2>Secure Practices</h2>" +
                "<pre>\n" +
                "(* Don't log secrets *)\n" +
                "apiKey = Environment[\"SECRET_API_KEY\"];\n" +
                "(* Use in headers, not URLs *)\n" +
                "URLFetch[url, \"Headers\" -> {\"Authorization\" -> \"Bearer \" <> apiKey}]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "secrets");

        // ===== PHASE 3 RULES (25 rules) =====

        // PERFORMANCE ISSUES (8 rules)

        repository.createRule(APPEND_IN_LOOP_KEY)
            .setName("AppendTo should not be used in loops")
            .setHtmlDescription(
                "<p>Using AppendTo or Append inside loops creates O(n²) performance due to repeated list copying.</p>" +
                "<p>Use Table, Sow/Reap, or build a list then Join instead.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = {};\n" +
                "Do[result = Append[result, f[i]], {i, 1000}]  (* O(n²) - Very slow! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use Table - O(n) *)\n" +
                "result = Table[f[i], {i, 1000}]\n\n" +
                "(* Or Sow/Reap *)\n" +
                "result = Reap[Do[Sow[f[i]], {i, 1000}]][[2, 1]]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/1050.html'>CWE-1050</a> - Excessive Platform Resource Consumption</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "mathematica-specific");

        repository.createRule(REPEATED_FUNCTION_CALLS_KEY)
            .setName("Expensive function calls should not be repeated")
            .setHtmlDescription(
                "<p>Calling the same function multiple times with identical arguments wastes computation.</p>" +
                "<p>Cache the result in a variable or use memoization.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = ExpensiveComputation[data] + ExpensiveComputation[data]  (* Computes twice! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "cached = ExpensiveComputation[data];\n" +
                "result = cached + cached\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(STRING_CONCAT_IN_LOOP_KEY)
            .setName("String concatenation should not be used in loops")
            .setHtmlDescription(
                "<p>Using <> to concatenate strings in loops is O(n²) due to string immutability.</p>" +
                "<p>Collect strings in a list, then use StringJoin.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = \"\";\n" +
                "Do[result = result <> ToString[i], {i, 1000}]  (* O(n²) *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "result = StringJoin[Table[ToString[i], {i, 1000}]]  (* O(n) *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance");

        repository.createRule(UNCOMPILED_NUMERICAL_KEY)
            .setName("Numerical loops should use Compile")
            .setHtmlDescription(
                "<p>Numerical computations in loops can be 10-100x faster when compiled.</p>" +
                "<p>Consider using Compile for numerical code with loops.</p>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "(* Without Compile - slower *)\n" +
                "sum = 0; Do[sum += i^2, {i, 10000}]\n\n" +
                "(* With Compile - much faster *)\n" +
                "compiled = Compile[{}, Module[{sum = 0}, Do[sum += i^2, {i, 10000}]; sum]];\n" +
                "result = compiled[]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "optimization");

        repository.createRule(PACKED_ARRAY_BREAKING_KEY)
            .setName("Operations should preserve packed arrays")
            .setHtmlDescription(
                "<p>Packed arrays are 10x+ faster than unpacked arrays.</p>" +
                "<p>Avoid operations that unpack arrays (mixing types, using symbolic expressions).</p>" +
                "<h2>Operations that Unpack Arrays</h2>" +
                "<ul>" +
                "<li>Mixing integers and reals</li>" +
                "<li>Using symbolic values</li>" +
                "<li>Applying non-numerical functions</li>" +
                "</ul>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "arrays");

        repository.createRule(NESTED_MAP_TABLE_KEY)
            .setName("Nested Map/Table should be refactored")
            .setHtmlDescription(
                "<p>Nested Map or Table calls can often be replaced with more efficient single operations.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "result = Table[Table[i*j, {j, 10}], {i, 10}]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "result = Table[i*j, {i, 10}, {j, 10}]  (* More efficient *)\n" +
                "(* Or use Outer for many cases *)\n" +
                "result = Outer[Times, Range[10], Range[10]]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "readability");

        repository.createRule(LARGE_TEMP_EXPRESSIONS_KEY)
            .setName("Large temporary expressions should be assigned to variables")
            .setHtmlDescription(
                "<p>Large intermediate results (>100MB) that aren't assigned can cause memory issues.</p>" +
                "<p>Assign large results to variables to make memory usage explicit.</p>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("memory", "performance");

        repository.createRule(PLOT_IN_LOOP_KEY)
            .setName("Plotting functions should not be called in loops")
            .setHtmlDescription(
                "<p>Generating plots in loops is very slow. Collect data first, then plot once.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Do[ListPlot[data[[i]]], {i, 100}]  (* Creates 100 separate plots *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "ListPlot[data, PlotRange -> All]  (* One plot with all data *)\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("performance", "visualization");

        // PATTERN MATCHING ISSUES (5 rules)

        repository.createRule(MISSING_PATTERN_TEST_KEY)
            .setName("Numeric functions should test argument types")
            .setHtmlDescription(
                "<p>Functions expecting numeric arguments should use pattern tests to prevent symbolic evaluation errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "f[x_] := Sqrt[x] + x^2  (* Will evaluate symbolically for f[a] *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "f[x_?NumericQ] := Sqrt[x] + x^2  (* Only evaluates for numbers *)\n" +
                "(* Or use _Real, _Integer, etc. *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/704.html'>CWE-704</a> - Incorrect Type Conversion</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "type-safety");

        repository.createRule(PATTERN_BLANKS_MISUSE_KEY)
            .setName("Pattern blanks should be used correctly")
            .setHtmlDescription(
                "<p>Using __ or ___ creates sequences, not lists. This often causes errors.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "f[x__] := Length[x]  (* ERROR: x is a sequence, Length expects list *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "f[x__] := Length[{x}]  (* Wrap sequence in list *)\n" +
                "(* Or use List pattern *)\n" +
                "f[x_List] := Length[x]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "pattern-matching");

        repository.createRule(SET_DELAYED_CONFUSION_KEY)
            .setName("Use SetDelayed (:=) for function definitions")
            .setHtmlDescription(
                "<p>Using Set (=) instead of SetDelayed (:=) evaluates the right-hand side immediately, which is usually wrong for functions.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "f[x_] = RandomReal[]  (* Evaluates once, same random number always returned! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "f[x_] := RandomReal[]  (* Evaluates each time function is called *)\n" +
                "</pre>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "common-mistake");

        repository.createRule(SYMBOL_NAME_COLLISION_KEY)
            .setName("User symbols should not shadow built-in functions")
            .setHtmlDescription(
                "<p>Defining functions with single-letter names or common words collides with Mathematica built-ins.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "N[x_] := ...  (* Shadows built-in N[] for numerical evaluation! *)\n" +
                "D[x_] := ...  (* Shadows built-in D[] for derivatives! *)\n" +
                "I = 5;  (* Shadows imaginary unit I! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "myN[x_] := ...\n" +
                "derivative[x_] := ...\n" +
                "index = 5;\n" +
                "</pre>" +
                "<h2>Common Built-ins to Avoid</h2>" +
                "<p>N, D, I, C, O, E, K, Pi, Re, Im, Abs, Min, Max, Log, Sin, Cos</p>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "naming");

        repository.createRule(BLOCK_MODULE_MISUSE_KEY)
            .setName("Block and Module should be used correctly")
            .setHtmlDescription(
                "<p>Block provides dynamic scope, Module provides lexical scope. Using the wrong one causes bugs.</p>" +
                "<h2>When to Use Each</h2>" +
                "<ul>" +
                "<li><strong>Module</strong>: For local variables (most common case)</li>" +
                "<li><strong>Block</strong>: To temporarily change global values</li>" +
                "<li><strong>With</strong>: For constant local values</li>" +
                "</ul>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "(* Use Module for local variables *)\n" +
                "f[x_] := Module[{temp = x^2}, temp + 1]\n\n" +
                "(* Use Block to temporarily override globals *)\n" +
                "Block[{$RecursionLimit = 1024}, RecursiveFunction[]]\n" +
                "</pre>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "scope");

        // BEST PRACTICES (7 rules)

        repository.createRule(GENERIC_VARIABLE_NAMES_KEY)
            .setName("Variables should have meaningful names")
            .setHtmlDescription(
                "<p>Generic names like 'x', 'temp', 'data' provide no context and hurt readability.</p>" +
                "<p>Use descriptive names except in very small scopes (&lt;5 lines) or mathematical contexts.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "data = Import[\"file.csv\"];\n" +
                "result = ProcessData[data];  (* What kind of data? *)\n" +
                "temp = result[[1]];  (* Temp what? *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "salesData = Import[\"sales.csv\"];\n" +
                "processedSales = ProcessSalesData[salesData];\n" +
                "firstQuarterSales = processedSales[[1]];\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability", "naming");

        repository.createRule(MISSING_USAGE_MESSAGE_KEY)
            .setName("Public functions should have usage messages")
            .setHtmlDescription(
                "<p>Public functions (starting with uppercase) should define usage messages for documentation.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "ProcessUserData[data_, options___] := Module[...]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "ProcessUserData::usage = \"ProcessUserData[data, options] processes user data with specified options.\";\n" +
                "ProcessUserData[data_, options___] := Module[...]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("documentation");

        repository.createRule(MISSING_OPTIONS_PATTERN_KEY)
            .setName("Functions with multiple optional parameters should use OptionsPattern")
            .setHtmlDescription(
                "<p>Functions with 3+ optional parameters should use OptionsPattern for better maintainability.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "PlotData[data_, color_: Blue, size_: 10, style_: Solid, width_: 2] := ...\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Options[PlotData] = {\"Color\" -> Blue, \"Size\" -> 10, \"Style\" -> Solid, \"Width\" -> 2};\n" +
                "PlotData[data_, opts: OptionsPattern[]] := Module[{color, size, style, width},\n" +
                "  color = OptionValue[\"Color\"];\n" +
                "  size = OptionValue[\"Size\"];\n" +
                "  ...\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("maintainability", "api-design");

        repository.createRule(SIDE_EFFECTS_NAMING_KEY)
            .setName("Functions with side effects should have descriptive names")
            .setHtmlDescription(
                "<p>Functions that modify global state should use naming conventions: SetXXX or ending with !.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Process[data_] := (globalCache = data; data)  (* Hidden side effect! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "SetCache[data_] := (globalCache = data; data)  (* Clear from name *)\n" +
                "(* Or use ! suffix like Mathematica built-ins *)\n" +
                "UpdateCache![data_] := (globalCache = data; data)\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("naming", "side-effects");

        repository.createRule(COMPLEX_BOOLEAN_KEY)
            .setName("Complex boolean expressions should be simplified")
            .setHtmlDescription(
                "<p>Boolean expressions with >5 operators without clear grouping are hard to understand.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "If[a && b || c && d && !e || f && g, ...]\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "condition1 = a && b;\n" +
                "condition2 = c && d && !e;\n" +
                "condition3 = f && g;\n" +
                "If[condition1 || condition2 || condition3, ...]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability", "complexity");

        repository.createRule(UNPROTECTED_SYMBOLS_KEY)
            .setName("Public API symbols should be protected")
            .setHtmlDescription(
                "<p>Public functions in packages should be Protected to prevent accidental redefinition by users.</p>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* At end of package *)\n" +
                "Protect[PublicFunction1, PublicFunction2, PublicConstant];\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("api-design", "safety");

        repository.createRule(MISSING_RETURN_KEY)
            .setName("Complex functions should have explicit Return statements")
            .setHtmlDescription(
                "<p>Functions with multiple branches or complex logic should use explicit Return[] for clarity.</p>" +
                "<h2>Example</h2>" +
                "<pre>\n" +
                "ProcessData[data_] := Module[{result},\n" +
                "  If[data === {}, Return[$Failed]];\n" +
                "  result = ComputeResult[data];\n" +
                "  If[!ValidQ[result], Return[Default]];\n" +
                "  Return[result]\n" +
                "]\n" +
                "</pre>"
            )
            .setSeverity("MINOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL)
            .setTags("readability");

        // SECURITY & SAFETY (3 rules)

        repository.createRule(UNSAFE_CLOUD_DEPLOY_KEY)
            .setName("CloudDeploy should specify Permissions")
            .setHtmlDescription(
                "<p>CloudDeploy without Permissions parameter creates public cloud objects accessible to anyone.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "CloudDeploy[form]  (* Public by default! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "CloudDeploy[form, Permissions -> \"Private\"]\n" +
                "CloudDeploy[form, Permissions -> {\"user@example.com\" -> \"Read\"}]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/276.html'>CWE-276</a> - Incorrect Default Permissions</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("security", "cloud", "permissions");

        repository.createRule(DYNAMIC_INJECTION_KEY)
            .setName("Dynamic content should not use ToExpression on user input")
            .setHtmlDescription(
                "<p>Using ToExpression or Symbol on user input in Dynamic creates code injection vulnerabilities.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Dynamic[ToExpression[userInput]]  (* User can execute arbitrary code! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Dynamic[SafeEvaluate[userInput]]  (* Use whitelist/sanitization *)\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/94.html'>CWE-94</a> - Code Injection</li></ul>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY)
            .setTags("security", "injection");

        repository.createRule(IMPORT_WITHOUT_FORMAT_KEY)
            .setName("Import should specify format explicitly")
            .setHtmlDescription(
                "<p>Import without format specification guesses by file extension, which attackers can manipulate.</p>" +
                "<p><strong>This is a Security Hotspot</strong> - Review to ensure format is validated.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "Import[userFile]  (* Guesses format - could execute .mx! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "Import[userFile, \"CSV\"]  (* Explicit format, safe *)\n" +
                "Import[userFile, \"JSON\"]\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/434.html'>CWE-434</a> - Unrestricted Upload of File with Dangerous Type</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setTags("security", "file-upload");

        // RESOURCE MANAGEMENT (2 rules)

        repository.createRule(UNCLOSED_FILE_HANDLE_KEY)
            .setName("File handles should be closed")
            .setHtmlDescription(
                "<p>OpenRead, OpenWrite, OpenAppend create file handles that must be closed with Close[].</p>" +
                "<p>Unclosed file handles leak resources and can prevent file access.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "stream = OpenRead[\"file.txt\"];\n" +
                "data = Read[stream, String];\n" +
                "(* Missing Close[stream]! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "stream = OpenRead[\"file.txt\"];\n" +
                "data = Read[stream, String];\n" +
                "Close[stream];\n\n" +
                "(* Or use Import which handles cleanup automatically *)\n" +
                "data = Import[\"file.txt\", \"String\"];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/772.html'>CWE-772</a> - Missing Release of Resource</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "resource-leak");

        repository.createRule(GROWING_DEFINITION_CHAIN_KEY)
            .setName("Definitions should not grow unbounded")
            .setHtmlDescription(
                "<p>Repeatedly adding definitions (e.g., memoization in loop) without clearing causes memory leaks.</p>" +
                "<h2>Noncompliant Code Example</h2>" +
                "<pre>\n" +
                "(* Memoization in loop - definitions grow forever! *)\n" +
                "Do[\n" +
                "  f[i] = ExpensiveComputation[i],\n" +
                "  {i, 1, 100000}\n" +
                "]  (* Creates 100k definitions, never cleared! *)\n" +
                "</pre>" +
                "<h2>Compliant Solution</h2>" +
                "<pre>\n" +
                "(* Use temporary memoization *)\n" +
                "Block[{f},\n" +
                "  Do[f[i] = ExpensiveComputation[i], {i, 1, 100000}];\n" +
                "  (* Use f here *)\n" +
                "]  (* Definitions cleared when Block exits *)\n\n" +
                "(* Or use Association/Dictionary *)\n" +
                "cache = Association[];\n" +
                "Do[cache[i] = ExpensiveComputation[i], {i, 1, 100000}];\n" +
                "</pre>" +
                "<h2>See</h2>" +
                "<ul><li><a href='https://cwe.mitre.org/data/definitions/401.html'>CWE-401</a> - Memory Leak</li></ul>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.BUG)
            .setTags("reliability", "memory-leak");

        repository.done();
    }
}
