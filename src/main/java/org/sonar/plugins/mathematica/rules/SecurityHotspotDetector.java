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

    // ===== PATTERNS FOR SECURITY HOTSPOT DETECTION =====

    private static final Pattern FILE_IMPORT_PATTERN = Pattern.compile(
        "(?:Import|Get|OpenRead|OpenWrite|Put)\\s*\\["
    );
    private static final Pattern API_CALL_PATTERN = Pattern.compile(
        "(?:URLRead|URLFetch|URLExecute|URLSubmit|ServiceExecute|ServiceConnect)\\s*\\["
    );
    private static final Pattern KEY_GENERATION_PATTERN = Pattern.compile(
        "(?:RandomInteger|Random)\\s*\\[|"
        + "(?:GenerateSymmetricKey|GenerateAsymmetricKeyPair)\\s*\\[|"
        + "Table\\s*\\[[^\\]]*Random"
    );

    // Phase 2 Security Hotspot patterns
    private static final Pattern NETWORK_PATTERN = Pattern.compile(
        "(?:SocketConnect|SocketOpen|SocketListen|WebExecute)\\s*\\["
    );
    private static final Pattern FILE_DELETE_PATTERN = Pattern.compile(
        "(?:DeleteFile|DeleteDirectory|RenameFile|CopyFile|SetFileDate)\\s*\\["
    );
    private static final Pattern ENVIRONMENT_PATTERN = Pattern.compile("Environment\\s*\\[");

    // Phase 3 Security Hotspot patterns
    private static final Pattern IMPORT_NO_FORMAT_PATTERN = Pattern.compile(
        "Import\\s*\\[\\s*[^,\\]]+\\s*\\]"
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
}
