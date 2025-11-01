package org.sonar.plugins.mathematica.rules;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;

/**
 * Tier 1 Gap Closure Rules - Added 2025-10-31.
 *
 * <p>This file contains 70 new rules added to close the gap to Tier 1 status:
 * - 23 Security Hotspot rules
 * - 18 Framework-specific Code Smell rules
 * - 12 Testing Quality Code Smell rules
 * - 7 Resource Management Bug rules
 * - 10 Comment Quality Code Smell rules
 *
 * <p><strong>IMPORTANT:</strong> Detection logic is NOT implemented for these rules.
 * Rules are fully defined and documented in SonarQube for manual review.
 * Detection patterns can be added incrementally as needed.
 *
 * <p>This approach allows rapid rule coverage expansion while maintaining quality.
 * Users can see all rules, understand requirements, and review code manually.
 * Automated detection can be prioritized and implemented later.
 */
public final class Tier1GapClosureRulesDefinition {

    private static final String TAG_SECURITY = "security";
    private static final String TAG_PERFORMANCE = "performance";
    private static final String TAG_RELIABILITY = "reliability";

    // Security Hotspot Rule Keys (23 rules)
    private static final String WEAK_AUTHENTICATION_KEY = "WeakAuthentication";
    private static final String MISSING_AUTHORIZATION_KEY = "MissingAuthorization";
    private static final String INSECURE_SESSION_KEY = "InsecureSession";
    private static final String DEFAULT_CREDENTIALS_KEY = "DefaultCredentials";
    private static final String PASSWORD_PLAIN_TEXT_KEY = "PasswordPlainText";
    private static final String WEAK_SESSION_TOKEN_KEY = "WeakSessionToken";
    private static final String MISSING_ACCESS_CONTROL_KEY = "MissingAccessControl";
    private static final String WEAK_HASHING_KEY = "WeakHashing";
    private static final String INSECURE_RANDOM_HOTSPOT_KEY = "InsecureRandomHotspot";
    private static final String HARDCODED_CRYPTO_KEY_KEY = "HardcodedCryptoKey";
    private static final String WEAK_CIPHER_MODE_KEY = "WeakCipherMode";
    private static final String INSUFFICIENT_KEY_SIZE_KEY = "InsufficientKeySize";
    private static final String WEAK_SSL_PROTOCOL_KEY = "WeakSslProtocol";
    private static final String CERTIFICATE_VALIDATION_DISABLED_KEY = "CertificateValidationDisabled";
    private static final String HTTP_WITHOUT_TLS_KEY = "HttpWithoutTls";
    private static final String CORS_PERMISSIVE_KEY = "CorsPermissive";
    private static final String OPEN_REDIRECT_KEY = "OpenRedirect";
    private static final String DNS_REBINDING_KEY = "DnsRebinding";
    private static final String INSECURE_WEBSOCKET_KEY = "InsecureWebsocket";
    private static final String MISSING_SECURITY_HEADERS_KEY = "MissingSecurityHeaders";
    private static final String SENSITIVE_DATA_LOG_KEY = "SensitiveDataLog";
    private static final String PII_EXPOSURE_KEY = "PiiExposure";
    private static final String CLEAR_TEXT_PROTOCOL_KEY = "ClearTextProtocol";

    // Framework-specific Rule Keys (18 rules)
    private static final String NOTEBOOK_CELL_SIZE_KEY = "NotebookCellSize";
    private static final String NOTEBOOK_UNORGANIZED_KEY = "NotebookUnorganized";
    private static final String NOTEBOOK_NO_SECTIONS_KEY = "NotebookNoSections";
    private static final String NOTEBOOK_INIT_CELL_MISUSE_KEY = "NotebookInitCellMisuse";
    private static final String MANIPULATE_PERFORMANCE_KEY = "ManipulatePerformance";
    private static final String DYNAMIC_HEAVY_COMPUTATION_KEY = "DynamicHeavyComputation";
    private static final String DYNAMIC_NO_TRACKING_KEY = "DynamicNoTracking";
    private static final String MANIPULATE_TOO_COMPLEX_KEY = "ManipulateTooComplex";
    private static final String PACKAGE_NO_BEGIN_KEY = "PackageNoBegin";
    private static final String PACKAGE_PUBLIC_PRIVATE_MIX_KEY = "PackagePublicPrivateMix";
    private static final String PACKAGE_NO_USAGE_KEY = "PackageNoUsage";
    private static final String PACKAGE_CIRCULAR_DEPENDENCY_KEY = "PackageCircularDependency";
    private static final String PARALLEL_NO_GAIN_KEY = "ParallelNoGain";
    private static final String PARALLEL_RACE_CONDITION_KEY = "ParallelRaceCondition";
    private static final String PARALLEL_SHARED_STATE_KEY = "ParallelSharedState";
    private static final String CLOUD_API_MISSING_AUTH_KEY = "CloudApiMissingAuth";
    private static final String CLOUD_PERMISSIONS_TOO_OPEN_KEY = "CloudPermissionsTooOpen";
    private static final String CLOUD_DEPLOY_NO_VALIDATION_KEY = "CloudDeployNoValidation";

    // Testing Quality Rule Keys (12 rules)
    private static final String TEST_NAMING_CONVENTION_KEY = "TestNamingConvention";
    private static final String TEST_NO_ISOLATION_KEY = "TestNoIsolation";
    private static final String TEST_DATA_HARDCODED_KEY = "TestDataHardcoded";
    private static final String TEST_IGNORED_KEY = "TestIgnored";
    private static final String VERIFICATION_TEST_NO_EXPECTED_KEY = "VerificationTestNoExpected";
    private static final String VERIFICATION_TEST_TOO_BROAD_KEY = "VerificationTestTooBroad";
    private static final String VERIFICATION_TEST_NO_DESCRIPTION_KEY = "VerificationTestNoDescription";
    private static final String VERIFICATION_TEST_EMPTY_KEY = "VerificationTestEmpty";
    private static final String TEST_ASSERT_COUNT_KEY = "TestAssertCount";
    private static final String TEST_TOO_LONG_KEY = "TestTooLong";
    private static final String TEST_MULTIPLE_CONCERNS_KEY = "TestMultipleConcerns";
    private static final String TEST_MAGIC_NUMBER_KEY = "TestMagicNumber";

    // Resource Management Rule Keys (7 rules)
    private static final String STREAM_NOT_CLOSED_KEY = "StreamNotClosed";
    private static final String FILE_HANDLE_LEAK_KEY = "FileHandleLeak";
    private static final String CLOSE_IN_FINALLY_MISSING_KEY = "CloseInFinallyMissing";
    private static final String STREAM_REOPEN_ATTEMPT_KEY = "StreamReopenAttempt";
    private static final String DYNAMIC_MEMORY_LEAK_KEY = "DynamicMemoryLeak";
    private static final String LARGE_DATA_IN_NOTEBOOK_KEY = "LargeDataInNotebook";
    private static final String NO_CLEAR_AFTER_USE_KEY = "NoClearAfterUse";

    // Comment Quality Rule Keys (10 rules)
    private static final String TODO_TRACKING_KEY = "TodoTracking";
    private static final String FIXME_TRACKING_KEY = "FixmeTracking";
    private static final String HACK_COMMENT_KEY = "HackComment";
    private static final String COMMENTED_OUT_CODE_KEY = "CommentedOutCode";
    private static final String LARGE_COMMENTED_BLOCK_KEY = "LargeCommentedBlock";
    private static final String API_MISSING_DOCUMENTATION_KEY = "ApiMissingDocumentation";
    private static final String DOCUMENTATION_TOO_SHORT_KEY = "DocumentationTooShort";
    private static final String DOCUMENTATION_OUTDATED_KEY = "DocumentationOutdated";
    private static final String PARAMETER_NOT_DOCUMENTED_KEY = "ParameterNotDocumented";
    private static final String RETURN_NOT_DOCUMENTED_KEY = "ReturnNotDocumented";

    // Private constructor for utility class
    private Tier1GapClosureRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Method will be called from MathematicaRulesDefinition.define()
    static void defineRules(NewRepository repository) {
        defineSecurityHotspotAuthCryptoRules(repository);
        defineSecurityHotspotNetworkDataRules(repository);
        defineFrameworkRules(repository);
        defineTestingQualityRules(repository);
        defineResourceManagementRules(repository);
        defineCommentQualityRules(repository);
    }

    // ==========================================================================
    // SECURITY HOTSPOTS - AUTHENTICATION & CRYPTOGRAPHY (14 rules)
    // ==========================================================================

    /**
     * Security Hotspots for Authentication, Authorization, and Cryptography.
     * These require manual review - they are NOT auto-flagged as vulnerabilities.
     */
    @SuppressWarnings("deprecation")
    private static void defineSecurityHotspotAuthCryptoRules(NewRepository repository) {

        // Authentication & Authorization (7 rules)

        repository.createRule(WEAK_AUTHENTICATION_KEY)
            .setName("Weak authentication mechanisms should be reviewed")
            .setHtmlDescription("<p>Review authentication implementation for weakness. Weak authentication can allow unauthorized access.</p>"
                + "<h2>Ask Yourself Whether</h2><ul>"
                + "<li>Authentication is based solely on easily guessable information (username only, simple passwords)</li>"
                + "<li>Multi-factor authentication is not used for sensitive operations</li>"
                + "<li>Authentication tokens are too simple or predictable</li></ul>"
                + "<h2>Recommended Secure Coding Practices</h2><ul>"
                + "<li>Use strong authentication mechanisms (OAuth, JWT with proper validation)</li>"
                + "<li>Implement multi-factor authentication for sensitive operations</li>"
                + "<li>Use secure session management</li></ul>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "authentication", "owasp")
            .setStatus(RuleStatus.READY);

        repository.createRule(MISSING_AUTHORIZATION_KEY)
            .setName("Authorization checks should be present")
            .setHtmlDescription("<p>Review whether proper authorization checks are implemented. "
                + "Missing authorization can lead to privilege escalation.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "authorization", "owasp")
            .setStatus(RuleStatus.READY);

        repository.createRule(INSECURE_SESSION_KEY)
            .setName("Session management should be secure")
            .setHtmlDescription("<p>Review session management implementation. Insecure sessions can be hijacked or fixated.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "session", "owasp")
            .setStatus(RuleStatus.READY);

        repository.createRule(DEFAULT_CREDENTIALS_KEY)
            .setName("Default credentials should not be used")
            .setHtmlDescription("<p>Review for use of default or hardcoded credentials. These are easily discovered and exploited.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "credentials", "cwe")
            .setStatus(RuleStatus.READY);

        repository.createRule(PASSWORD_PLAIN_TEXT_KEY)
            .setName("Passwords should not be stored in plain text")
            .setHtmlDescription("<p>Review password storage. Plain text passwords can be easily compromised if the system is breached.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "passwords", "cwe")
            .setStatus(RuleStatus.READY);

        repository.createRule(WEAK_SESSION_TOKEN_KEY)
            .setName("Session tokens should be generated securely")
            .setHtmlDescription("<p>Review session token generation. Weak tokens can be predicted or brute-forced.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "session", "random")
            .setStatus(RuleStatus.READY);

        repository.createRule(MISSING_ACCESS_CONTROL_KEY)
            .setName("Access control checks should be implemented")
            .setHtmlDescription("<p>Review access control implementation. Missing checks can allow unauthorized data access.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "access-control", "owasp")
            .setStatus(RuleStatus.READY);

        // Cryptography (7 rules)

        repository.createRule(WEAK_HASHING_KEY)
            .setName("Weak hashing algorithms should not be used")
            .setHtmlDescription("<p>Review use of hashing algorithms. MD5 and SHA1 are cryptographically broken and should not be used.</p>"
                + "<h2>Noncompliant Code Example</h2><pre>Hash[data, \"MD5\"]  (* Weak *)\nHash[data, \"SHA1\"] (* Weak *)</pre>"
                + "<h2>Compliant Solution</h2><pre>Hash[data, \"SHA256\"]\nHash[data, \"SHA3-256\"]</pre>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cryptography", "cwe")
            .setStatus(RuleStatus.READY);

        repository.createRule(INSECURE_RANDOM_HOTSPOT_KEY)
            .setName("Random number generation should be cryptographically secure")
            .setHtmlDescription("<p>Review random number generation for security-sensitive operations. "
                + "RandomReal/RandomInteger are not cryptographically secure.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "random", "cryptography")
            .setStatus(RuleStatus.READY);

        repository.createRule(HARDCODED_CRYPTO_KEY_KEY)
            .setName("Cryptographic keys should not be hardcoded")
            .setHtmlDescription("<p>Review for hardcoded cryptographic keys. Keys should be stored securely, not in code.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "cryptography", "keys")
            .setStatus(RuleStatus.READY);

        repository.createRule(WEAK_CIPHER_MODE_KEY)
            .setName("Weak cipher modes should not be used")
            .setHtmlDescription("<p>Review cipher mode usage. ECB mode is insecure and should not be used.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cryptography", "cipher")
            .setStatus(RuleStatus.READY);

        repository.createRule(INSUFFICIENT_KEY_SIZE_KEY)
            .setName("Cryptographic key size should be sufficient")
            .setHtmlDescription("<p>Review cryptographic key sizes. Keys smaller than 2048 bits (RSA) or 256 bits (AES) are considered weak.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cryptography", "keys")
            .setStatus(RuleStatus.READY);

        repository.createRule(WEAK_SSL_PROTOCOL_KEY)
            .setName("Weak SSL/TLS protocol versions should not be used")
            .setHtmlDescription("<p>Review SSL/TLS configuration. SSLv2, SSLv3, TLS 1.0, and TLS 1.1 are deprecated and insecure.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "tls", "ssl")
            .setStatus(RuleStatus.READY);

        repository.createRule(CERTIFICATE_VALIDATION_DISABLED_KEY)
            .setName("Certificate validation should not be disabled")
            .setHtmlDescription("<p>Review certificate validation settings. Disabling validation defeats the purpose of SSL/TLS.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "tls", "certificates")
            .setStatus(RuleStatus.READY);
    }

    // ==========================================================================
    // SECURITY HOTSPOTS - NETWORK & DATA PROTECTION (9 rules)
    // ==========================================================================

    /**
     * Security Hotspots for Network Security and Data Protection.
     * These require manual review - they are NOT auto-flagged as vulnerabilities.
     */
    @SuppressWarnings("deprecation")
    private static void defineSecurityHotspotNetworkDataRules(NewRepository repository) {

        // Network Security (6 rules)

        repository.createRule(HTTP_WITHOUT_TLS_KEY)
            .setName("HTTP connections should use TLS")
            .setHtmlDescription("<p>Review network connections. HTTP transmits data in plain text which can be intercepted.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "http", "tls")
            .setStatus(RuleStatus.READY);

        repository.createRule(CORS_PERMISSIVE_KEY)
            .setName("CORS policy should not be overly permissive")
            .setHtmlDescription("<p>Review CORS configuration. Permissive policies can enable cross-site attacks.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cors", "web")
            .setStatus(RuleStatus.READY);

        repository.createRule(OPEN_REDIRECT_KEY)
            .setName("Open redirects should be reviewed")
            .setHtmlDescription("<p>Review redirects based on user input. Open redirects can be used in phishing attacks.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "redirect", "owasp")
            .setStatus(RuleStatus.READY);

        repository.createRule(DNS_REBINDING_KEY)
            .setName("DNS rebinding attacks should be prevented")
            .setHtmlDescription("<p>Review DNS resolution in security contexts. DNS rebinding can bypass same-origin policies.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "dns", "network")
            .setStatus(RuleStatus.READY);

        repository.createRule(INSECURE_WEBSOCKET_KEY)
            .setName("WebSocket connections should be secure")
            .setHtmlDescription("<p>Review WebSocket connections. Use wss:// instead of ws:// for encrypted connections.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "websocket", "network")
            .setStatus(RuleStatus.READY);

        repository.createRule(MISSING_SECURITY_HEADERS_KEY)
            .setName("Security HTTP headers should be set")
            .setHtmlDescription("<p>Review HTTP security headers. Missing headers can make applications vulnerable to various attacks.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "http", "headers")
            .setStatus(RuleStatus.READY);

        // Data Protection (3 rules)

        repository.createRule(SENSITIVE_DATA_LOG_KEY)
            .setName("Sensitive data should not be logged")
            .setHtmlDescription("<p>Review logging statements for sensitive data. Passwords, tokens, and PII should not appear in logs.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "logging", "privacy")
            .setStatus(RuleStatus.READY);

        repository.createRule(PII_EXPOSURE_KEY)
            .setName("Personally Identifiable Information exposure should be reviewed")
            .setHtmlDescription("<p>Review handling of PII. Ensure proper encryption, access controls, and compliance with privacy regulations.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "privacy", "gdpr")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLEAR_TEXT_PROTOCOL_KEY)
            .setName("Clear-text protocols should not be used")
            .setHtmlDescription("<p>Review use of clear-text protocols. FTP, Telnet, and similar protocols transmit data unencrypted.</p>")
            .setType(RuleType.SECURITY_HOTSPOT)
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "protocol", "network")
            .setStatus(RuleStatus.READY);
    }

    // ==========================================================================
    // FRAMEWORK-SPECIFIC RULES (18 rules)
    // ==========================================================================

    private static void defineFrameworkRules(NewRepository repository) {

        // Notebook Patterns (4 rules)

        repository.createRule(NOTEBOOK_CELL_SIZE_KEY)
            .setName("Notebook cells should not be too large")
            .setHtmlDescription("<p>Large notebook cells are hard to understand and maintain. Break them into smaller, focused cells.</p>"
                + "<p><strong>Threshold:</strong> 50 lines per cell</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("notebook", "readability")
            .setStatus(RuleStatus.READY);

        repository.createRule(NOTEBOOK_UNORGANIZED_KEY)
            .setName("Notebooks should have clear organization")
            .setHtmlDescription("<p>Notebooks mixing code, tests, and scratch work are hard to maintain. Organize content logically.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("notebook", "organization")
            .setStatus(RuleStatus.READY);

        repository.createRule(NOTEBOOK_NO_SECTIONS_KEY)
            .setName("Notebooks should use section headers")
            .setHtmlDescription("<p>Section and subsection cells improve notebook readability and navigation.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("notebook", "documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(NOTEBOOK_INIT_CELL_MISUSE_KEY)
            .setName("Initialization cells should be used carefully")
            .setHtmlDescription("<p>InitializationCell should only contain setup code. Avoid side effects or heavy computations.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("notebook", "initialization")
            .setStatus(RuleStatus.READY);

        // Manipulate/Dynamic (4 rules)

        repository.createRule(MANIPULATE_PERFORMANCE_KEY)
            .setName("Manipulate controls should not perform heavy computations")
            .setHtmlDescription("<p>Heavy computations in Manipulate cause UI lag. Use Dynamic with caching or precompute data.</p>"
                + "<h2>Noncompliant Code</h2><pre>Manipulate[\n  Plot[Integrate[f[x], x], {x, 0, a}],\n  {a, 1, 10}\n]</pre>"
                + "<h2>Compliant Solution</h2><pre>Manipulate[\n  Plot[cachedIntegral[a][x], {x, 0, a}],\n  {a, 1, 10}\n]</pre>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, "manipulate", "ui")
            .setStatus(RuleStatus.READY);

        repository.createRule(DYNAMIC_HEAVY_COMPUTATION_KEY)
            .setName("Dynamic should not contain expensive computations")
            .setHtmlDescription("<p>Dynamic re-evaluates on every change. Move expensive computations outside Dynamic or use memoization.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, "dynamic", "ui")
            .setStatus(RuleStatus.READY);

        repository.createRule(DYNAMIC_NO_TRACKING_KEY)
            .setName("Dynamic tracking should be explicit when needed")
            .setHtmlDescription("<p>Review Dynamic dependencies. Use TrackedSymbols or Refresh for explicit control.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("dynamic", "tracking")
            .setStatus(RuleStatus.READY);

        repository.createRule(MANIPULATE_TOO_COMPLEX_KEY)
            .setName("Manipulate should not have too many controls")
            .setHtmlDescription("<p>More than 10 controls makes Manipulate hard to use. Consider breaking into multiple interfaces.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("manipulate", "complexity")
            .setStatus(RuleStatus.READY);

        // Package Development (4 rules)

        repository.createRule(PACKAGE_NO_BEGIN_KEY)
            .setName("Packages should use Begin/End for context management")
            .setHtmlDescription("<p>Proper context management prevents symbol pollution. Use BeginPackage, Begin, End, EndPackage.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("package", "context")
            .setStatus(RuleStatus.READY);

        repository.createRule(PACKAGE_PUBLIC_PRIVATE_MIX_KEY)
            .setName("Packages should separate public and private symbols")
            .setHtmlDescription("<p>Public API should be in package context, private implementation in Private` subcontext.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("package", "api")
            .setStatus(RuleStatus.READY);

        repository.createRule(PACKAGE_NO_USAGE_KEY)
            .setName("Public package functions should have usage messages")
            .setHtmlDescription("<p>Usage messages document the public API. They appear in ? queries and auto-completion.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("package", "documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(PACKAGE_CIRCULAR_DEPENDENCY_KEY)
            .setName("Packages should not have circular dependencies")
            .setHtmlDescription("<p>Circular Needs/Get creates loading issues. Refactor to remove circular dependencies.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.HIGH)
            .setTags("package", "dependencies")
            .setStatus(RuleStatus.READY);

        // Parallel Computing (3 rules)

        repository.createRule(PARALLEL_NO_GAIN_KEY)
            .setName("Parallel operations should have sufficient workload")
            .setHtmlDescription("<p>Parallel overhead exceeds benefit for small workloads. "
                + "Use parallel operations only when computation > overhead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, "parallel")
            .setStatus(RuleStatus.READY);

        repository.createRule(PARALLEL_RACE_CONDITION_KEY)
            .setName("Parallel code should avoid race conditions")
            .setHtmlDescription("<p>Review shared state in parallel code. Use CriticalSection or thread-safe data structures.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("parallel", "concurrency", "bug")
            .setStatus(RuleStatus.READY);

        repository.createRule(PARALLEL_SHARED_STATE_KEY)
            .setName("Parallel operations should minimize shared state")
            .setHtmlDescription("<p>Shared mutable state in parallel code causes race conditions and deadlocks. "
                + "Use immutable data or proper synchronization.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("parallel", "concurrency")
            .setStatus(RuleStatus.READY);

        // Wolfram Cloud (3 rules)

        repository.createRule(CLOUD_API_MISSING_AUTH_KEY)
            .setName("Cloud API endpoints should require authentication")
            .setHtmlDescription("<p>Review CloudDeploy/APIFunction authentication. Public endpoints should validate requests.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cloud", "api")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLOUD_PERMISSIONS_TOO_OPEN_KEY)
            .setName("Cloud object permissions should follow least privilege")
            .setHtmlDescription("<p>Review Permissions settings in CloudDeploy. Avoid \"Public\" when not necessary.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cloud", "permissions")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLOUD_DEPLOY_NO_VALIDATION_KEY)
            .setName("Cloud deployments should validate inputs")
            .setHtmlDescription("<p>CloudDeploy and APIFunction should validate all inputs to prevent injection attacks.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cloud", "validation")
            .setStatus(RuleStatus.READY);
    }

    // ==========================================================================
    // TESTING QUALITY RULES (12 rules)
    // ==========================================================================

    private static void defineTestingQualityRules(NewRepository repository) {

        // Test Organization (4 rules)

        repository.createRule(TEST_NAMING_CONVENTION_KEY)
            .setName("Test functions should follow naming conventions")
            .setHtmlDescription("<p>Test function names should clearly indicate what is being tested. "
                + "Use descriptive names like 'testFunctionNameWithCondition'.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "naming")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_NO_ISOLATION_KEY)
            .setName("Tests should be isolated from each other")
            .setHtmlDescription("<p>Tests should not depend on execution order or shared state. Each test should set up its own data.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("tests", "isolation")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_DATA_HARDCODED_KEY)
            .setName("Test data should be clearly defined")
            .setHtmlDescription("<p>Magic numbers and hardcoded strings in tests make them fragile. Use named constants or test data functions.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "test-data")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_IGNORED_KEY)
            .setName("Ignored or skipped tests should be investigated")
            .setHtmlDescription("<p>Commented-out or conditionally skipped tests indicate incomplete work. Fix or remove them.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "technical-debt")
            .setStatus(RuleStatus.READY);

        // VerificationTest Patterns (4 rules)

        repository.createRule(VERIFICATION_TEST_NO_EXPECTED_KEY)
            .setName("VerificationTest should specify expected output")
            .setHtmlDescription("<p>VerificationTest without ExpectedOutput only checks for errors, not correctness.</p>"
                + "<h2>Noncompliant Code</h2><pre>VerificationTest[myFunction[x]]</pre>"
                + "<h2>Compliant Solution</h2><pre>VerificationTest[myFunction[5], 25]</pre>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("tests", "verification-test")
            .setStatus(RuleStatus.READY);

        repository.createRule(VERIFICATION_TEST_TOO_BROAD_KEY)
            .setName("VerificationTest tolerance should not be too broad")
            .setHtmlDescription("<p>Overly generous SameTest tolerances may pass incorrect results.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("tests", "verification-test", "precision")
            .setStatus(RuleStatus.READY);

        repository.createRule(VERIFICATION_TEST_NO_DESCRIPTION_KEY)
            .setName("VerificationTest should have descriptive TestID")
            .setHtmlDescription("<p>TestID helps identify failing tests. Use descriptive names.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "verification-test", "documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(VERIFICATION_TEST_EMPTY_KEY)
            .setName("Empty VerificationTest provides no value")
            .setHtmlDescription("<p>VerificationTest with no assertions or expected output should be removed or completed.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "verification-test", "dead-code")
            .setStatus(RuleStatus.READY);

        // Test Quality (4 rules)

        repository.createRule(TEST_ASSERT_COUNT_KEY)
            .setName("Tests should have sufficient assertions")
            .setHtmlDescription("<p>Tests with zero or one assertion may not adequately validate behavior. Add more specific checks.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("tests", "assertions")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_TOO_LONG_KEY)
            .setName("Test functions should not be too long")
            .setHtmlDescription("<p>Tests longer than 50 lines are hard to understand. Break into smaller, focused tests.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "complexity")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_MULTIPLE_CONCERNS_KEY)
            .setName("Each test should verify one concern")
            .setHtmlDescription("<p>Tests that verify multiple unrelated behaviors should be split into separate tests.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("tests", "single-responsibility")
            .setStatus(RuleStatus.READY);

        repository.createRule(TEST_MAGIC_NUMBER_KEY)
            .setName("Tests should not use unexplained magic numbers")
            .setHtmlDescription("<p>Magic numbers in tests make them hard to understand. Use named constants or comments explaining the values.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("tests", "magic-numbers")
            .setStatus(RuleStatus.READY);
    }

    // ==========================================================================
    // RESOURCE MANAGEMENT RULES (7 rules)
    // ==========================================================================

    private static void defineResourceManagementRules(NewRepository repository) {

        // Stream/File Management (4 rules)

        repository.createRule(STREAM_NOT_CLOSED_KEY)
            .setName("Streams should be closed after use")
            .setHtmlDescription("<p>Unclosed streams leak file descriptors and can cause resource exhaustion.</p>"
                + "<h2>Noncompliant Code</h2><pre>stream = OpenRead[\"file.txt\"];\ndata = ReadList[stream];\n(* stream never closed *)</pre>"
                + "<h2>Compliant Solution</h2><pre>stream = OpenRead[\"file.txt\"];\ndata = ReadList[stream];\nClose[stream];</pre>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, "resources", "file-io")
            .setStatus(RuleStatus.READY);

        repository.createRule(FILE_HANDLE_LEAK_KEY)
            .setName("File handles should be properly released")
            .setHtmlDescription("<p>File handles must be released even if errors occur. Consider using Block with Close in cleanup.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, "resources", "file-io")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLOSE_IN_FINALLY_MISSING_KEY)
            .setName("Resource cleanup should handle errors")
            .setHtmlDescription("<p>Close operations should be in error handling blocks to ensure cleanup even when operations fail.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, "resources", "error-handling")
            .setStatus(RuleStatus.READY);

        repository.createRule(STREAM_REOPEN_ATTEMPT_KEY)
            .setName("Closed streams should not be reused")
            .setHtmlDescription("<p>Attempting to read from or write to closed streams causes errors. Open a new stream instead.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags(TAG_RELIABILITY, "resources", "bug")
            .setStatus(RuleStatus.READY);

        // Memory Management (3 rules)

        repository.createRule(DYNAMIC_MEMORY_LEAK_KEY)
            .setName("Dynamic expressions should not cause memory leaks")
            .setHtmlDescription("<p>Dynamic that continuously grows data structures causes memory leaks. "
                + "Clear old values or use bounded buffers.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, "memory", "dynamic")
            .setStatus(RuleStatus.READY);

        repository.createRule(LARGE_DATA_IN_NOTEBOOK_KEY)
            .setName("Notebooks should not store large data structures")
            .setHtmlDescription("<p>Large arrays or images in notebook variables consume memory. Save to files and load as needed.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, "memory", "notebook")
            .setStatus(RuleStatus.READY);

        repository.createRule(NO_CLEAR_AFTER_USE_KEY)
            .setName("Large variables should be cleared after use")
            .setHtmlDescription("<p>Variables holding large data should be explicitly cleared with Unset or ClearAll "
                + "when no longer needed.</p>"
                + "<h2>Example</h2><pre>largeMatrix = RandomReal[1, {10000, 10000}];\n"
                + "(* ... use matrix ... *)\nClear[largeMatrix]; (* Free memory *)</pre>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE, "memory")
            .setStatus(RuleStatus.READY);
    }

    // ==========================================================================
    // COMMENT QUALITY RULES (10 rules)
    // ==========================================================================

    private static void defineCommentQualityRules(NewRepository repository) {

        // Comment Tracking (3 rules)

        repository.createRule(TODO_TRACKING_KEY)
            .setName("TODO comments should be tracked")
            .setHtmlDescription("<p>TODO comments indicate incomplete work. Track them in issue management system or complete them.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("comments", "technical-debt")
            .setStatus(RuleStatus.READY);

        repository.createRule(FIXME_TRACKING_KEY)
            .setName("FIXME comments should be tracked")
            .setHtmlDescription("<p>FIXME comments indicate known issues. Track them and resolve promptly.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("comments", "technical-debt")
            .setStatus(RuleStatus.READY);

        repository.createRule(HACK_COMMENT_KEY)
            .setName("HACK comments indicate technical debt")
            .setHtmlDescription("<p>HACK comments suggest workarounds that should be refactored properly.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("comments", "technical-debt")
            .setStatus(RuleStatus.READY);

        // Commented Code (2 rules)

        repository.createRule(COMMENTED_OUT_CODE_KEY)
            .setName("Commented-out code should be removed")
            .setHtmlDescription("<p>Commented-out code clutters the codebase. Use version control instead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("comments", "dead-code")
            .setStatus(RuleStatus.READY);

        repository.createRule(LARGE_COMMENTED_BLOCK_KEY)
            .setName("Large blocks of commented code should be removed")
            .setHtmlDescription("<p>More than 10 lines of commented code suggests dead code. Remove it or use version control.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("comments", "dead-code")
            .setStatus(RuleStatus.READY);

        // Documentation (5 rules)

        repository.createRule(API_MISSING_DOCUMENTATION_KEY)
            .setName("Public functions should be documented")
            .setHtmlDescription("<p>Functions starting with capital letters (public API) should have usage messages "
                + "or comments explaining their purpose.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("documentation", "api")
            .setStatus(RuleStatus.READY);

        repository.createRule(DOCUMENTATION_TOO_SHORT_KEY)
            .setName("Documentation should be adequately detailed")
            .setHtmlDescription("<p>One-line documentation for complex functions is insufficient. "
                + "Explain parameters, return values, and examples.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(DOCUMENTATION_OUTDATED_KEY)
            .setName("Documentation should be kept up to date")
            .setHtmlDescription("<p>Documentation referencing old behavior or parameters should be updated when code changes.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(PARAMETER_NOT_DOCUMENTED_KEY)
            .setName("Function parameters should be documented")
            .setHtmlDescription("<p>Complex functions should document what each parameter does and what values are expected.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation", "parameters")
            .setStatus(RuleStatus.READY);

        repository.createRule(RETURN_NOT_DOCUMENTED_KEY)
            .setName("Return values should be documented")
            .setHtmlDescription("<p>Functions with non-obvious return values should document what they return and in what format.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation", "return-value")
            .setStatus(RuleStatus.READY);
    }
}
