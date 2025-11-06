package org.sonar.plugins.mathematica.rules;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.Severity;

/**
 * Security Hotspot Rules for Authentication, Authorization, Cryptography, Network Security, and Data Protection.
 *
 * <p>This file contains 23 security hotspot rules that require manual review:
 * - 7 Authentication & Authorization rules
 * - 7 Cryptography rules
 * - 6 Network Security rules
 * - 3 Data Protection rules
 *
 * <p><strong>IMPORTANT:</strong> Detection logic is NOT implemented for these rules.
 * Rules are fully defined and documented in SonarQube for manual review.
 * Detection patterns can be added incrementally as needed.
 */
public final class SecurityHotspotRulesDefinition {

    private static final String TAG_SECURITY = "security";

    private static final String OWASP = "owasp";
    private static final String CRYPTOGRAPHY = "cryptography";
    private static final String NETWORK = "network";

    // Authentication & Authorization Rule Keys (7 rules)
    private static final String WEAK_AUTHENTICATION_KEY = "WeakAuthentication";
    private static final String MISSING_AUTHORIZATION_KEY = "MissingAuthorization";
    private static final String INSECURE_SESSION_KEY = "InsecureSession";
    private static final String DEFAULT_CREDENTIALS_KEY = "DefaultCredentials";
    private static final String PASSWORD_PLAIN_TEXT_KEY = "PasswordPlainText";
    private static final String WEAK_SESSION_TOKEN_KEY = "WeakSessionToken";
    private static final String MISSING_ACCESS_CONTROL_KEY = "MissingAccessControl";

    // Cryptography Rule Keys (7 rules)
    private static final String WEAK_HASHING_KEY = "WeakHashing";
    private static final String INSECURE_RANDOM_HOTSPOT_KEY = "InsecureRandomHotspot";
    private static final String HARDCODED_CRYPTO_KEY_KEY = "HardcodedCryptoKey";
    private static final String WEAK_CIPHER_MODE_KEY = "WeakCipherMode";
    private static final String INSUFFICIENT_KEY_SIZE_KEY = "InsufficientKeySize";
    private static final String WEAK_SSL_PROTOCOL_KEY = "WeakSslProtocol";
    private static final String CERTIFICATE_VALIDATION_DISABLED_KEY = "CertificateValidationDisabled";

    // Network Security Rule Keys (6 rules)
    private static final String HTTP_WITHOUT_TLS_KEY = "HttpWithoutTls";
    private static final String CORS_PERMISSIVE_KEY = "CorsPermissive";
    private static final String OPEN_REDIRECT_KEY = "OpenRedirect";
    private static final String DNS_REBINDING_KEY = "DnsRebinding";
    private static final String INSECURE_WEBSOCKET_KEY = "InsecureWebsocket";
    private static final String MISSING_SECURITY_HEADERS_KEY = "MissingSecurityHeaders";

    // Data Protection Rule Keys (3 rules)
    private static final String SENSITIVE_DATA_LOG_KEY = "SensitiveDataLog";
    private static final String PII_EXPOSURE_KEY = "PiiExposure";
    private static final String CLEAR_TEXT_PROTOCOL_KEY = "ClearTextProtocol";

    // Private constructor for utility class
    private SecurityHotspotRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all security hotspot rules in the repository.
     */
    public static void define(NewRepository repository) {
        defineAuthenticationAndAuthorizationRules(repository);
        defineCryptographyRules(repository);
        defineNetworkSecurityRules(repository);
        defineDataProtectionRules(repository);
    }

    /**
     * Security Hotspots for Authentication and Authorization.
     * These require manual review - they are NOT auto-flagged as vulnerabilities.
     */
    @SuppressWarnings("deprecation")
    private static void defineAuthenticationAndAuthorizationRules(NewRepository repository) {
        repository.createRule(WEAK_AUTHENTICATION_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
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
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "authentication", OWASP)
            .setStatus(RuleStatus.READY);

        repository.createRule(MISSING_AUTHORIZATION_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Authorization checks should be present")
            .setHtmlDescription("<p>Review whether proper authorization checks are implemented. "
                + "Missing authorization can lead to privilege escalation.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "authorization", OWASP)
            .setStatus(RuleStatus.READY);

        repository.createRule(INSECURE_SESSION_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Session management should be secure")
            .setHtmlDescription("<p>Review session management implementation. Insecure sessions can be hijacked or fixated.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "session", OWASP)
            .setStatus(RuleStatus.READY);

        repository.createRule(DEFAULT_CREDENTIALS_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Default credentials should not be used")
            .setHtmlDescription("<p>Review for use of default or hardcoded credentials. These are easily discovered and exploited.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "credentials", "cwe")
            .setStatus(RuleStatus.READY);

        repository.createRule(PASSWORD_PLAIN_TEXT_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Passwords should not be stored in plain text")
            .setHtmlDescription("<p>Review password storage. Plain text passwords can be easily compromised if the system is breached.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "passwords", "cwe")
            .setStatus(RuleStatus.READY);

        repository.createRule(WEAK_SESSION_TOKEN_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Session tokens should be generated securely")
            .setHtmlDescription("<p>Review session token generation. Weak tokens can be predicted or brute-forced.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "session", "random")
            .setStatus(RuleStatus.READY);

        repository.createRule(MISSING_ACCESS_CONTROL_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Access control checks should be implemented")
            .setHtmlDescription("<p>Review access control implementation. Missing checks can allow unauthorized data access.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "access-control", OWASP)
            .setStatus(RuleStatus.READY);
    }

    /**
     * Security Hotspots for Cryptography.
     * These require manual review - they are NOT auto-flagged as vulnerabilities.
     */
    @SuppressWarnings("deprecation")
    private static void defineCryptographyRules(NewRepository repository) {
        repository.createRule(WEAK_HASHING_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Weak hashing algorithms should not be used")
            .setHtmlDescription("<p>Review use of hashing algorithms. MD5 and SHA1 are cryptographically broken and should not be used.</p>"
                + "<h2>Noncompliant Code Example</h2><pre>Hash[data, \"MD5\"]  (* Weak *)\nHash[data, \"SHA1\"] (* Weak *)</pre>"
                + "<h2>Compliant Solution</h2><pre>Hash[data, \"SHA256\"]\nHash[data, \"SHA3-256\"]</pre>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, CRYPTOGRAPHY, "cwe")
            .setStatus(RuleStatus.READY);

        repository.createRule(INSECURE_RANDOM_HOTSPOT_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Random number generation should be cryptographically secure")
            .setHtmlDescription("<p>Review random number generation for security-sensitive operations. "
                + "RandomReal/RandomInteger are not cryptographically secure.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "random", CRYPTOGRAPHY)
            .setStatus(RuleStatus.READY);

        repository.createRule(HARDCODED_CRYPTO_KEY_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Cryptographic keys should not be hardcoded")
            .setHtmlDescription("<p>Review for hardcoded cryptographic keys. Keys should be stored securely, not in code.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, CRYPTOGRAPHY, "keys")
            .setStatus(RuleStatus.READY);

        repository.createRule(WEAK_CIPHER_MODE_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Weak cipher modes should not be used")
            .setHtmlDescription("<p>Review cipher mode usage. ECB mode is insecure and should not be used.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, CRYPTOGRAPHY, "cipher")
            .setStatus(RuleStatus.READY);

        repository.createRule(INSUFFICIENT_KEY_SIZE_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Cryptographic key size should be sufficient")
            .setHtmlDescription("<p>Review cryptographic key sizes. Keys smaller than 2048 bits (RSA) or 256 bits (AES) are considered weak.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, CRYPTOGRAPHY, "keys")
            .setStatus(RuleStatus.READY);

        repository.createRule(WEAK_SSL_PROTOCOL_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Weak SSL/TLS protocol versions should not be used")
            .setHtmlDescription("<p>Review SSL/TLS configuration. SSLv2, SSLv3, TLS 1.0, and TLS 1.1 are deprecated and insecure.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "tls", "ssl")
            .setStatus(RuleStatus.READY);

        repository.createRule(CERTIFICATE_VALIDATION_DISABLED_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Certificate validation should not be disabled")
            .setHtmlDescription("<p>Review certificate validation settings. Disabling validation defeats the purpose of SSL/TLS.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "tls", "certificates")
            .setStatus(RuleStatus.READY);
    }

    /**
     * Security Hotspots for Network Security.
     * These require manual review - they are NOT auto-flagged as vulnerabilities.
     */
    @SuppressWarnings("deprecation")
    private static void defineNetworkSecurityRules(NewRepository repository) {
        repository.createRule(HTTP_WITHOUT_TLS_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("HTTP connections should use TLS")
            .setHtmlDescription("<p>Review network connections. HTTP transmits data in plain text which can be intercepted.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "http", "tls")
            .setStatus(RuleStatus.READY);

        repository.createRule(CORS_PERMISSIVE_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("CORS policy should not be overly permissive")
            .setHtmlDescription("<p>Review CORS configuration. Permissive policies can enable cross-site attacks.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "cors", "web")
            .setStatus(RuleStatus.READY);

        repository.createRule(OPEN_REDIRECT_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Open redirects should be reviewed")
            .setHtmlDescription("<p>Review redirects based on user input. Open redirects can be used in phishing attacks.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "redirect", OWASP)
            .setStatus(RuleStatus.READY);

        repository.createRule(DNS_REBINDING_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("DNS rebinding attacks should be prevented")
            .setHtmlDescription("<p>Review DNS resolution in security contexts. DNS rebinding can bypass same-origin policies.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "dns", NETWORK)
            .setStatus(RuleStatus.READY);

        repository.createRule(INSECURE_WEBSOCKET_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("WebSocket connections should be secure")
            .setHtmlDescription("<p>Review WebSocket connections. Use wss:// instead of ws:// for encrypted connections.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "websocket", NETWORK)
            .setStatus(RuleStatus.READY);

        repository.createRule(MISSING_SECURITY_HEADERS_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Security HTTP headers should be set")
            .setHtmlDescription("<p>Review HTTP security headers. Missing headers can make applications vulnerable to various attacks.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "http", "headers")
            .setStatus(RuleStatus.READY);
    }

    /**
     * Security Hotspots for Data Protection and Privacy.
     * These require manual review - they are NOT auto-flagged as vulnerabilities.
     */
    @SuppressWarnings("deprecation")
    private static void defineDataProtectionRules(NewRepository repository) {
        repository.createRule(SENSITIVE_DATA_LOG_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Sensitive data should not be logged")
            .setHtmlDescription("<p>Review logging statements for sensitive data. Passwords, tokens, and PII should not appear in logs.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "logging", "privacy")
            .setStatus(RuleStatus.READY);

        repository.createRule(PII_EXPOSURE_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Personally Identifiable Information exposure should be reviewed")
            .setHtmlDescription("<p>Review handling of PII. Ensure proper encryption, access controls, and compliance with privacy regulations.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "privacy", "gdpr")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLEAR_TEXT_PROTOCOL_KEY)
            .setType(org.sonar.api.rules.RuleType.SECURITY_HOTSPOT)
            .setName("Clear-text protocols should not be used")
            .setHtmlDescription("<p>Review use of clear-text protocols. FTP, Telnet, and similar protocols transmit data unencrypted.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, "protocol", NETWORK)
            .setStatus(RuleStatus.READY);
    }
}
