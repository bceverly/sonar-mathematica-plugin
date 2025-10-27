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

        repository.done();
    }
}
