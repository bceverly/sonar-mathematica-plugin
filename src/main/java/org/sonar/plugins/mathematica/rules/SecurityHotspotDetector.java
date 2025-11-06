package org.sonar.plugins.mathematica.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * Detector for Security Hotspot rules (7 rules total).
 * Hotspots are security-sensitive code that requires manual review.
 */
public class SecurityHotspotDetector extends BaseDetector {

    private static final String PERMISSIONS = "Permissions";
    private static final String APIFUNCTION = "APIFunction";

    // ===== PATTERNS FOR SECURITY HOTSPOT DETECTION =====



    private static final Pattern FILE_IMPORT_PATTERN = Pattern.compile(
        "(?:Import|Get|OpenRead|OpenWrite|Put)\\s*+\\["
    );
    private static final Pattern API_CALL_PATTERN = Pattern.compile(
        "(?:URLRead|URLFetch|URLExecute|URLSubmit|ServiceExecute|ServiceConnect)\\s*+\\["
    );
    private static final Pattern KEY_GENERATION_PATTERN = Pattern.compile(
        "(?:RandomInteger|Random)\\s*+\\[|"
        + "(?:GenerateSymmetricKey|GenerateAsymmetricKeyPair)\\s*+\\[|"
        + "Table\\s*+\\[[^\\]]*Random"
    );

    // Phase 2 Security Hotspot patterns
    private static final Pattern NETWORK_PATTERN = Pattern.compile(
        "(?:SocketConnect|SocketOpen|SocketListen|WebExecute)\\s*+\\["
    );
    private static final Pattern FILE_DELETE_PATTERN = Pattern.compile(
        "(?:DeleteFile|DeleteDirectory|RenameFile|CopyFile|SetFileDate)\\s*+\\["
    );
    private static final Pattern ENVIRONMENT_PATTERN = Pattern.compile("Environment\\s*+\\["); //NOSONAR - Possessive quantifiers prevent backtracking

    // Phase 3 Security Hotspot patterns
    private static final Pattern IMPORT_NO_FORMAT_PATTERN = Pattern.compile(//NOSONAR
        "Import\\s*+\\[\\s*+[^,\\]]+\\s*+\\]"
    );

    /**
     * Detect file upload/import operations that need validation review.
     */
    public void detectFileUploadValidation(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FILE_IMPORT_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                String match = matcher.group();

                String message;
                if (match.contains("Import") || match.contains("Get")) {
                    message = "Review: Ensure file uploads/imports are validated for type, size, and content.";
                } else {
                    message = "Review: Ensure file operations validate and sanitize file paths.";
                }

                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.FILE_UPLOAD_VALIDATION_KEY, message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping file upload detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect external API calls that need safeguards review.
     */
    public void detectExternalApiSafeguards(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = API_CALL_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.EXTERNAL_API_SAFEGUARDS_KEY,
                    "Review: Ensure this API call has timeout, error handling, and rate limiting.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping API safeguards detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect cryptographic key generation that needs security review.
     */
    public void detectCryptoKeyGeneration(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = KEY_GENERATION_PATTERN.matcher(content);

            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                String match = matcher.group();

                String message;
                if (match.contains("Random[") && !match.contains("RandomInteger")) {
                    message = "Review: Random[] is not cryptographically secure. Use RandomInteger for keys.";
                } else {
                    message = "Review: Ensure cryptographic keys are generated with sufficient entropy and stored securely.";
                }

                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CRYPTO_KEY_GENERATION_KEY, message);
            }
        } catch (Exception e) {
            LOG.warn("Skipping crypto key generation detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect network operations that need security review.
     */
    public void detectNetworkOperations(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = NETWORK_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.NETWORK_OPERATIONS_KEY,
                    "Review: Network operation should use TLS, have timeout, and proper error handling.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping network operations detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect file system modifications that need security review.
     */
    public void detectFileSystemModifications(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = FILE_DELETE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.FILE_SYSTEM_MODIFICATIONS_KEY,
                    "Review: File system modification should validate paths and log operations.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping file system modifications detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect environment variable access that needs security review.
     */
    public void detectEnvironmentVariable(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ENVIRONMENT_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.ENVIRONMENT_VARIABLE_KEY,
                    "Review: Environment variable may contain secrets. Ensure not logged or exposed.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping environment variable detection due to error in file: {}", inputFile.filename());
        }
    }

    /**
     * Detect Import without explicit format specification.
     */
    public void detectImportWithoutFormat(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = IMPORT_NO_FORMAT_PATTERN.matcher(content);
            while (matcher.find()) {
                // Check if it's truly without format (no second argument)
                String match = matcher.group();
                if (!match.contains(",")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.IMPORT_WITHOUT_FORMAT_KEY,
                        "Review: Import without explicit format relies on file extension. Specify format for security.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping Import without format detection due to error in file: {}", inputFile.filename());
        }
    }

    // ==========================================================================
    // TIER 1 GAP CLOSURE - SECURITY HOTSPOT DETECTION (23 rules)
    // ==========================================================================

    // Tier 1 Security Hotspot patterns - Authentication & Authorization
    private static final Pattern AUTH_FUNCTIONS_PATTERN = Pattern.compile(
        "(?:AuthenticationDialog|CreateDialog|FormFunction|FormPage|CloudDeploy|APIFunction)\\s*+\\["
    );
    private static final Pattern PASSWORD_VAR_PATTERN = Pattern.compile(
        "(?:password|passwd|pwd|credential|secret|apiKey|token)\\s*+=\\s*+\"[^\"]+\""
    );
    private static final Pattern SESSION_PATTERN = Pattern.compile(
        "(?:SessionID|SessionToken|CreateUUID|Hash\\[.*RandomInteger)\\s*+\\["
    );
    private static final Pattern DEFAULT_CRED_PATTERN = Pattern.compile(
        "\"(?:admin|root|password|123456|default|guest)\""
    );

    // Tier 1 Security Hotspot patterns - Cryptography
    private static final Pattern WEAK_HASH_PATTERN = Pattern.compile(
        "Hash\\s*+\\[[^,]+,\\s*+\"(?:MD5|SHA1|MD2|MD4|SHA-1)\"\\s*+\\]"
    );
    private static final Pattern RANDOM_SECURITY_PATTERN = Pattern.compile(
        "(?:RandomInteger|RandomReal|Random|SeedRandom)\\s*+\\[[^\\]]*(?:key|password|token|salt|nonce|iv)"
    );
    private static final Pattern CRYPTO_KEY_HARDCODED_PATTERN = Pattern.compile(
        "(?:Encrypt|Decrypt|GenerateSymmetricKey)\\s*+\\[[^\\]]*\"[0-9a-fA-F]{16,}\""
    );
    private static final Pattern ECB_MODE_PATTERN = Pattern.compile(
        "Encrypt\\s*+\\[[^\\]]*,\\s*+(?:None|\"ECB\")"
    );
    private static final Pattern SMALL_KEY_SIZE_PATTERN = Pattern.compile(
        "GenerateAsymmetricKeyPair\\s*+\\[[^\\]]*,\\s*+(?:512|768|1024)\\s*+\\]"
    );
    private static final Pattern SSL_PROTOCOL_PATTERN = Pattern.compile(
        "URLRead\\s*+\\[[^\\]]*\"Method\"\\s*+->\\s*+\"(?:SSLv2|SSLv3|TLSv1\\.0|TLSv1)\""
    );
    private static final Pattern CERT_VALIDATION_PATTERN = Pattern.compile(
        "URLRead\\s*+\\[[^\\]]*\"VerifyPeer\"\\s*+->\\s*+False"
    );

    // Tier 1 Security Hotspot patterns - Network & Data
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile(
        "(?:URLRead|URLFetch|URLExecute|URLSubmit)\\s*+\\[\\s*+\"http://[^\"]+\""
    );
    private static final Pattern CORS_PATTERN = Pattern.compile(
        "APIFunction\\s*+\\[[^\\]]*\"AllowedOrigins\"\\s*+->\\s*+\\{\"\\*\"\\}"
    );
    private static final Pattern REDIRECT_PATTERN = Pattern.compile(
        "HTTPRedirect\\s*+\\[[^\\[]*\\+\\+|<>"
    );
    private static final Pattern WEBSOCKET_PATTERN = Pattern.compile(
        "SocketConnect\\s*+\\[\\s*+\"ws://[^\"]+\""
    );
    private static final Pattern LOG_SENSITIVE_PATTERN = Pattern.compile(
        "(?:Print|Echo|WriteString)\\s*+\\[[^\\]]*(?:password|token|apiKey|secret|credential)"
    );
    private static final Pattern PII_PATTERN = Pattern.compile(
        "(?:ssn|creditCard|passport|driverLicense|taxId|nationalId)\\s*+[=:]"
    );
    private static final Pattern CLEARTEXT_PROTOCOL_PATTERN = Pattern.compile(
        "(?:ftp://|telnet://|ldap://[^s])"
    );

    /**
     * Detect weak authentication mechanisms.
     */
    public void detectWeakAuthentication(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = AUTH_FUNCTIONS_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                String contextWindow = content.substring(Math.max(0, matcher.start() - 50),
                    Math.min(content.length(), matcher.end() + 200));

                if (!contextWindow.contains("Authentication") && !contextWindow.contains(PERMISSIONS)) {
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.WEAK_AUTHENTICATION_KEY,
                        "Review: Authentication implementation for weakness. Use strong authentication mechanisms.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping weak authentication detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing authorization checks.
     */
    public void detectMissingAuthorization(SensorContext context, InputFile inputFile, String content) {
        try {
            if ((content.contains(APIFUNCTION) || content.contains("FormFunction"))
                && !content.contains(PERMISSIONS) && !content.contains("$RequesterAddress")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_AUTHORIZATION_KEY,
                    "Review: Missing authorization checks in API/Form functions.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing authorization detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect insecure session management.
     */
    public void detectInsecureSession(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SESSION_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.INSECURE_SESSION_KEY,
                    "Review: Session management implementation. Ensure secure session tokens and expiration.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping insecure session detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect default or hardcoded credentials.
     */
    public void detectDefaultCredentials(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DEFAULT_CRED_PATTERN.matcher(content);
            while (matcher.find()) {
                if (content.substring(Math.max(0, matcher.start() - 20), matcher.start())
                    .matches(".*(?:password|pwd|passwd|credential)\\s*+=\\s*+$")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.DEFAULT_CREDENTIALS_KEY,
                        "Review: Default credential detected. Never use default passwords.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping default credentials detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect passwords stored in plain text.
     */
    public void detectPasswordPlainText(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PASSWORD_VAR_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PASSWORD_PLAIN_TEXT_KEY,
                    "Review: Password stored in plain text. Use secure storage and hashing.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping password plain text detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect weak session token generation.
     */
    public void detectWeakSessionToken(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = RANDOM_SECURITY_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.WEAK_SESSION_TOKEN_KEY,
                    "Review: Weak random generation for security token. Use cryptographically secure methods.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping weak session token detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing access control checks.
     */
    public void detectMissingAccessControl(SensorContext context, InputFile inputFile, String content) {
        try {
            if ((content.contains("CloudDeploy") || content.contains(APIFUNCTION))
                && !content.contains(PERMISSIONS) && !content.contains("$Permissions")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_ACCESS_CONTROL_KEY,
                    "Review: Missing access control checks for cloud/API functions.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing access control detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect weak hashing algorithms.
     */
    public void detectWeakHashing(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = WEAK_HASH_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.WEAK_HASHING_KEY,
                    "Review: Weak hashing algorithm (MD5/SHA1). Use SHA-256 or SHA3-256.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping weak hashing detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect insecure random for security contexts.
     */
    public void detectInsecureRandomHotspot(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = RANDOM_SECURITY_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.INSECURE_RANDOM_HOTSPOT_KEY,
                    "Review: Insecure random for security. RandomInteger is not cryptographically secure.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping insecure random hotspot detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect hardcoded cryptographic keys.
     */
    public void detectHardcodedCryptoKey(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CRYPTO_KEY_HARDCODED_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.HARDCODED_CRYPTO_KEY_KEY,
                    "Review: Hardcoded cryptographic key. Store keys securely, not in code.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping hardcoded crypto key detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect weak cipher modes (ECB).
     */
    public void detectWeakCipherMode(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = ECB_MODE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.WEAK_CIPHER_MODE_KEY,
                    "Review: ECB cipher mode is insecure. Use CBC, GCM, or CTR modes.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping weak cipher mode detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect insufficient key sizes.
     */
    public void detectInsufficientKeySize(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SMALL_KEY_SIZE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.INSUFFICIENT_KEY_SIZE_KEY,
                    "Review: Insufficient key size (<2048 bits). Use at least 2048 bits for RSA.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping insufficient key size detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect weak SSL/TLS protocols.
     */
    public void detectWeakSslProtocol(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = SSL_PROTOCOL_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.WEAK_SSL_PROTOCOL_KEY,
                    "Review: Weak SSL/TLS protocol. Use TLS 1.2 or 1.3.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping weak SSL protocol detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect disabled certificate validation.
     */
    public void detectCertificateValidationDisabled(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CERT_VALIDATION_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CERTIFICATE_VALIDATION_DISABLED_KEY,
                    "Review: Certificate validation disabled. This defeats the purpose of SSL/TLS.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping certificate validation detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect HTTP without TLS.
     */
    public void detectHttpWithoutTls(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = HTTP_URL_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.HTTP_WITHOUT_TLS_KEY,
                    "Review: HTTP connection without TLS. Use HTTPS to protect data in transit.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping HTTP without TLS detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect overly permissive CORS.
     */
    public void detectCorsPermissive(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CORS_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CORS_PERMISSIVE_KEY,
                    "Review: Permissive CORS policy (*). Restrict to specific origins.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping CORS permissive detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect open redirect vulnerabilities.
     */
    public void detectOpenRedirect(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = REDIRECT_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.OPEN_REDIRECT_KEY,
                    "Review: Open redirect based on user input. Validate redirect targets.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping open redirect detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect DNS rebinding risks.
     */
    public void detectDnsRebinding(SensorContext context, InputFile inputFile, String content) {
        try {
            if ((content.contains("URLRead") || content.contains("SocketConnect"))
                && (content.contains("localhost") || content.contains("127.0.0.1"))) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.DNS_REBINDING_KEY,
                    "Review: DNS rebinding risk with localhost connections. Validate origins.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping DNS rebinding detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect insecure WebSocket connections.
     */
    public void detectInsecureWebsocket(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = WEBSOCKET_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.INSECURE_WEBSOCKET_KEY,
                    "Review: Insecure WebSocket (ws://). Use wss:// for encrypted connections.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping insecure WebSocket detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect missing security headers.
     */
    public void detectMissingSecurityHeaders(SensorContext context, InputFile inputFile, String content) {
        try {
            if ((content.contains(APIFUNCTION) || content.contains("FormPage"))
                && !content.contains("HTTPResponse") && !content.contains("Headers")) {
                reportIssue(context, inputFile, 1, MathematicaRulesDefinition.MISSING_SECURITY_HEADERS_KEY,
                    "Review: Missing HTTP security headers (X-Frame-Options, CSP, etc.).");
            }
        } catch (Exception e) {
            LOG.warn("Skipping missing security headers detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect sensitive data in logs.
     */
    public void detectSensitiveDataLog(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = LOG_SENSITIVE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.SENSITIVE_DATA_LOG_KEY,
                    "Review: Sensitive data (password/token) in logs. Remove before logging.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping sensitive data log detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect PII exposure risks.
     */
    public void detectPiiExposure(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = PII_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.PII_EXPOSURE_KEY,
                    "Review: PII handling. Ensure encryption, access controls, and compliance.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping PII exposure detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect clear-text protocols.
     */
    public void detectClearTextProtocol(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = CLEARTEXT_PROTOCOL_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.CLEAR_TEXT_PROTOCOL_KEY,
                    "Review: Clear-text protocol (FTP/Telnet/LDAP). Use encrypted alternatives.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping clear-text protocol detection: {}", inputFile.filename());
        }
    }
}
