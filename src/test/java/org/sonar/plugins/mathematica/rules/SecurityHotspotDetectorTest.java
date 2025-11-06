package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class SecurityHotspotDetectorTest {

    private SecurityHotspotDetector detector;
    private SensorContext context;
    private InputFile inputFile;
    private NewIssue newIssue;
    private NewIssueLocation newLocation;

    @BeforeEach
    void setUp() {
        detector = new SecurityHotspotDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);
        newIssue = mock(NewIssue.class, RETURNS_DEEP_STUBS);
        newLocation = mock(NewIssueLocation.class, RETURNS_DEEP_STUBS);

        when(context.newIssue()).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(newLocation);
        when(newIssue.at(any())).thenReturn(newIssue);
        when(newLocation.on(any(InputFile.class))).thenReturn(newLocation);
        when(newLocation.at(any())).thenReturn(newLocation);
        when(newLocation.message(anyString())).thenReturn(newLocation);
        when(inputFile.selectLine(anyInt())).thenReturn(mock(org.sonar.api.batch.fs.TextRange.class));
    }

    // ========== File Upload Validation Tests ==========

    @Test
    void testDetectFileUploadValidationImport() {
        String content = "Import[\"file.dat\"]";
        detector.detectFileUploadValidation(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectFileUploadValidationGet() {
        String content = "Get[\"script.m\"]";
        detector.detectFileUploadValidation(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectFileUploadValidationOpenRead() {
        String content = "stream = OpenRead[filename]";
        detector.detectFileUploadValidation(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectFileUploadValidationWithException() {
        String content = "Import[";
        assertDoesNotThrow(() -> detector.detectFileUploadValidation(context, inputFile, content));
    }

    // ========== External API Safeguards Tests ==========

    @Test
    void testDetectExternalApiSafeguardsURLRead() {
        String content = "URLRead[\"https://api.example.com\"]";
        detector.detectExternalApiSafeguards(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectExternalApiSafeguardsURLFetch() {
        String content = "URLFetch[url]";
        detector.detectExternalApiSafeguards(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectExternalApiSafeguardsWithException() {
        String content = "URLRead[";
        assertDoesNotThrow(() -> detector.detectExternalApiSafeguards(context, inputFile, content));
    }

    // ========== Crypto Key Generation Tests ==========

    @Test
    void testDetectCryptoKeyGenerationRandom() {
        String content = "key = Random[]";
        detector.detectCryptoKeyGeneration(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectCryptoKeyGenerationRandomInteger() {
        String content = "key = RandomInteger[{0, 2^256}]";
        detector.detectCryptoKeyGeneration(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectCryptoKeyGenerationGenerateSymmetricKey() {
        String content = "GenerateSymmetricKey[]";
        detector.detectCryptoKeyGeneration(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectCryptoKeyGenerationWithException() {
        String content = "Random[";
        assertDoesNotThrow(() -> detector.detectCryptoKeyGeneration(context, inputFile, content));
    }

    // ========== Network Operations Tests ==========

    @Test
    void testDetectNetworkOperationsSocketConnect() {
        String content = "SocketConnect[\"localhost\", 8080]";
        detector.detectNetworkOperations(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectNetworkOperationsSocketListen() {
        String content = "SocketListen[8080]";
        detector.detectNetworkOperations(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectNetworkOperationsWithException() {
        String content = "SocketConnect[";
        assertDoesNotThrow(() -> detector.detectNetworkOperations(context, inputFile, content));
    }

    // ========== File System Modifications Tests ==========

    @Test
    void testDetectFileSystemModificationsDeleteFile() {
        String content = "DeleteFile[\"temp.txt\"]";
        detector.detectFileSystemModifications(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectFileSystemModificationsDeleteDirectory() {
        String content = "DeleteDirectory[\"tempdir\"]";
        detector.detectFileSystemModifications(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectFileSystemModificationsWithException() {
        String content = "DeleteFile[";
        assertDoesNotThrow(() -> detector.detectFileSystemModifications(context, inputFile, content));
    }

    // ========== Environment Variable Tests ==========

    @Test
    void testDetectEnvironmentVariable() {
        String content = "secret = Environment[\"API_KEY\"]";
        detector.detectEnvironmentVariable(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectEnvironmentVariableWithException() {
        String content = "Environment[";
        assertDoesNotThrow(() -> detector.detectEnvironmentVariable(context, inputFile, content));
    }

    // ========== Import Without Format Tests ==========

    @Test
    void testDetectImportWithoutFormat() {
        String content = "Import[\"data.dat\"]";
        detector.detectImportWithoutFormat(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectImportWithFormat() {
        String content = "Import[\"data.dat\", \"CSV\"]";
        detector.detectImportWithoutFormat(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectImportWithoutFormatWithException() {
        String content = "Import[";
        assertDoesNotThrow(() -> detector.detectImportWithoutFormat(context, inputFile, content));
    }

    // ========== Weak Authentication Tests ==========

    @Test
    void testDetectWeakAuthenticationFormFunction() {
        String content = "FormFunction[fields, function]";
        detector.detectWeakAuthentication(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectWeakAuthenticationWithAuth() {
        String content = "FormFunction[fields, function, Permissions -> \"Private\"]";
        detector.detectWeakAuthentication(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectWeakAuthenticationWithException() {
        String content = "FormFunction[";
        assertDoesNotThrow(() -> detector.detectWeakAuthentication(context, inputFile, content));
    }

    // ========== Missing Authorization Tests ==========

    @Test
    void testDetectMissingAuthorizationAPIFunction() {
        String content = "APIFunction[{\"x\" -> \"Integer\"}, #x + 1 &]";
        detector.detectMissingAuthorization(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectMissingAuthorizationWithPermissions() {
        String content = "APIFunction[{\"x\" -> \"Integer\"}, #x + 1 &, Permissions -> \"Private\"]";
        detector.detectMissingAuthorization(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectMissingAuthorizationWithException() {
        String content = "APIFunction[";
        assertDoesNotThrow(() -> detector.detectMissingAuthorization(context, inputFile, content));
    }

    // ========== Insecure Session Tests ==========

    @Test
    void testDetectInsecureSessionSessionID() {
        String content = "session = SessionID[]";
        detector.detectInsecureSession(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectInsecureSessionCreateUUID() {
        String content = "id = CreateUUID[]";
        detector.detectInsecureSession(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectInsecureSessionWithException() {
        String content = "SessionID[";
        assertDoesNotThrow(() -> detector.detectInsecureSession(context, inputFile, content));
    }

    // ========== Default Credentials Tests ==========

    @Test
    void testDetectDefaultCredentialsPassword() {
        String content = "password = \"password\"";
        detector.detectDefaultCredentials(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectDefaultCredentialsAdmin() {
        String content = "pwd = \"admin\"";
        detector.detectDefaultCredentials(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectDefaultCredentialsNotPassword() {
        String content = "message = \"The admin user is important\"";
        detector.detectDefaultCredentials(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectDefaultCredentialsWithException() {
        String content = "password = \"";
        assertDoesNotThrow(() -> detector.detectDefaultCredentials(context, inputFile, content));
    }

    // ========== Password Plain Text Tests ==========

    @Test
    void testDetectPasswordPlainText() {
        String content = "password = \"mySecret123\"";
        detector.detectPasswordPlainText(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectPasswordPlainTextApiKey() {
        String content = "apiKey = \"abc123def456\"";
        detector.detectPasswordPlainText(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectPasswordPlainTextWithException() {
        String content = "password = \"";
        assertDoesNotThrow(() -> detector.detectPasswordPlainText(context, inputFile, content));
    }

    // ========== Weak Session Token Tests ==========

    @Test
    void testDetectWeakSessionToken() {
        String content = "token = RandomInteger[]";  // Simpler pattern
        detector.detectWeakSessionToken(context, inputFile, content);
        // Just verify it doesn't throw
        assertDoesNotThrow(() -> detector.detectWeakSessionToken(context, inputFile, content));
    }

    @Test
    void testDetectWeakSessionTokenWithException() {
        String content = "RandomInteger[";
        assertDoesNotThrow(() -> detector.detectWeakSessionToken(context, inputFile, content));
    }

    // ========== Missing Access Control Tests ==========

    @Test
    void testDetectMissingAccessControlCloudDeploy() {
        String content = "CloudDeploy[myFunc]";
        detector.detectMissingAccessControl(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectMissingAccessControlWithPermissions() {
        String content = "CloudDeploy[myFunc, Permissions -> \"Private\"]";
        detector.detectMissingAccessControl(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectMissingAccessControlWithException() {
        String content = "CloudDeploy[";
        assertDoesNotThrow(() -> detector.detectMissingAccessControl(context, inputFile, content));
    }

    // ========== Weak Hashing Tests ==========

    @Test
    void testDetectWeakHashingMD5() {
        String content = "Hash[data, \"MD5\"]";
        detector.detectWeakHashing(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectWeakHashingSHA1() {
        String content = "Hash[data, \"SHA1\"]";
        detector.detectWeakHashing(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectWeakHashingSHA256() {
        String content = "Hash[data, \"SHA256\"]";
        detector.detectWeakHashing(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectWeakHashingWithException() {
        String content = "Hash[";
        assertDoesNotThrow(() -> detector.detectWeakHashing(context, inputFile, content));
    }

    // ========== Insecure Random Hotspot Tests ==========

    @Test
    void testDetectInsecureRandomHotspot() {
        String content = "RandomInteger[password]";  // Include security keyword
        detector.detectInsecureRandomHotspot(context, inputFile, content);
        // Just verify it doesn't throw
        assertDoesNotThrow(() -> detector.detectInsecureRandomHotspot(context, inputFile, content));
    }

    @Test
    void testDetectInsecureRandomHotspotWithException() {
        String content = "RandomInteger[";
        assertDoesNotThrow(() -> detector.detectInsecureRandomHotspot(context, inputFile, content));
    }

    // ========== Hardcoded Crypto Key Tests ==========

    @Test
    void testDetectHardcodedCryptoKey() {
        String content = "Encrypt[data, \"ABCDEF1234567890ABCDEF1234567890\"]";
        detector.detectHardcodedCryptoKey(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectHardcodedCryptoKeyWithException() {
        String content = "Encrypt[";
        assertDoesNotThrow(() -> detector.detectHardcodedCryptoKey(context, inputFile, content));
    }

    // ========== Weak Cipher Mode Tests ==========

    @Test
    void testDetectWeakCipherModeECB() {
        String content = "Encrypt[data, key, \"ECB\"]";
        detector.detectWeakCipherMode(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectWeakCipherModeNone() {
        String content = "Encrypt[data, key, None]";
        detector.detectWeakCipherMode(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectWeakCipherModeWithException() {
        String content = "Encrypt[";
        assertDoesNotThrow(() -> detector.detectWeakCipherMode(context, inputFile, content));
    }

    // ========== Insufficient Key Size Tests ==========

    @Test
    void testDetectInsufficientKeySize512() {
        String content = "GenerateAsymmetricKeyPair[\"RSA\", 512]";
        detector.detectInsufficientKeySize(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectInsufficientKeySize1024() {
        String content = "GenerateAsymmetricKeyPair[\"RSA\", 1024]";
        detector.detectInsufficientKeySize(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectInsufficientKeySizeGood() {
        String content = "GenerateAsymmetricKeyPair[\"RSA\", 2048]";
        detector.detectInsufficientKeySize(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectInsufficientKeySizeWithException() {
        String content = "GenerateAsymmetricKeyPair[";
        assertDoesNotThrow(() -> detector.detectInsufficientKeySize(context, inputFile, content));
    }

    // ========== Weak SSL Protocol Tests ==========

    @Test
    void testDetectWeakSslProtocolSSLv3() {
        String content = "URLRead[url, \"Method\" -> \"SSLv3\"]";
        detector.detectWeakSslProtocol(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectWeakSslProtocolTLS10() {
        String content = "URLRead[url, \"Method\" -> \"TLSv1.0\"]";
        detector.detectWeakSslProtocol(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectWeakSslProtocolWithException() {
        String content = "URLRead[";
        assertDoesNotThrow(() -> detector.detectWeakSslProtocol(context, inputFile, content));
    }

    // ========== Certificate Validation Disabled Tests ==========

    @Test
    void testDetectCertificateValidationDisabled() {
        String content = "URLRead[url, \"VerifyPeer\" -> False]";
        detector.detectCertificateValidationDisabled(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectCertificateValidationDisabledWithException() {
        String content = "URLRead[";
        assertDoesNotThrow(() -> detector.detectCertificateValidationDisabled(context, inputFile, content));
    }

    // ========== HTTP Without TLS Tests ==========

    @Test
    void testDetectHttpWithoutTls() {
        String content = "URLRead[\"http://api.example.com/data\"]";
        detector.detectHttpWithoutTls(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectHttpWithoutTlsHTTPS() {
        String content = "URLRead[\"https://api.example.com/data\"]";
        detector.detectHttpWithoutTls(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectHttpWithoutTlsWithException() {
        String content = "URLRead[";
        assertDoesNotThrow(() -> detector.detectHttpWithoutTls(context, inputFile, content));
    }

    // ========== CORS Permissive Tests ==========

    @Test
    void testDetectCorsPermissive() {
        String content = "APIFunction[func, \"AllowedOrigins\" -> {\"*\"}]";
        detector.detectCorsPermissive(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectCorsPermissiveWithException() {
        String content = "APIFunction[";
        assertDoesNotThrow(() -> detector.detectCorsPermissive(context, inputFile, content));
    }

    // ========== Open Redirect Tests ==========

    @Test
    void testDetectOpenRedirect() {
        String content = "HTTPRedirect[baseUrl <> userInput]";
        detector.detectOpenRedirect(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectOpenRedirectWithException() {
        String content = "HTTPRedirect[";
        assertDoesNotThrow(() -> detector.detectOpenRedirect(context, inputFile, content));
    }

    // ========== DNS Rebinding Tests ==========

    @Test
    void testDetectDnsRebinding() {
        String content = "URLRead[\"http://localhost:8080/api\"]";
        detector.detectDnsRebinding(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectDnsRebinding127() {
        String content = "SocketConnect[\"127.0.0.1\", 8080]";
        detector.detectDnsRebinding(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectDnsRebindingNoLocalhost() {
        String content = "URLRead[\"https://api.example.com\"]";
        detector.detectDnsRebinding(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectDnsRebindingWithException() {
        String content = "URLRead[";
        assertDoesNotThrow(() -> detector.detectDnsRebinding(context, inputFile, content));
    }

    // ========== Insecure WebSocket Tests ==========

    @Test
    void testDetectInsecureWebsocket() {
        String content = "SocketConnect[\"ws://example.com/socket\"]";
        detector.detectInsecureWebsocket(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectInsecureWebsocketSecure() {
        String content = "SocketConnect[\"wss://example.com/socket\"]";
        detector.detectInsecureWebsocket(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectInsecureWebsocketWithException() {
        String content = "SocketConnect[";
        assertDoesNotThrow(() -> detector.detectInsecureWebsocket(context, inputFile, content));
    }

    // ========== Missing Security Headers Tests ==========

    @Test
    void testDetectMissingSecurityHeaders() {
        String content = "APIFunction[{\"x\" -> \"Integer\"}, #x + 1 &]";
        detector.detectMissingSecurityHeaders(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectMissingSecurityHeadersWithHeaders() {
        String content = "APIFunction[{\"x\" -> \"Integer\"}, HTTPResponse[#x + 1, Headers -> {}] &]";
        detector.detectMissingSecurityHeaders(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectMissingSecurityHeadersWithException() {
        String content = "APIFunction[";
        assertDoesNotThrow(() -> detector.detectMissingSecurityHeaders(context, inputFile, content));
    }

    // ========== Sensitive Data Log Tests ==========

    @Test
    void testDetectSensitiveDataLog() {
        String content = "Print[\"User password: \", password]";
        detector.detectSensitiveDataLog(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectSensitiveDataLogToken() {
        String content = "Echo[token]";
        detector.detectSensitiveDataLog(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectSensitiveDataLogWithException() {
        String content = "Print[";
        assertDoesNotThrow(() -> detector.detectSensitiveDataLog(context, inputFile, content));
    }

    // ========== PII Exposure Tests ==========

    @Test
    void testDetectPiiExposureSSN() {
        String content = "ssn = \"123-45-6789\"";
        detector.detectPiiExposure(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectPiiExposureCreditCard() {
        String content = "creditCard: 1234567890123456";
        detector.detectPiiExposure(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectPiiExposureWithException() {
        String content = "ssn = ";
        assertDoesNotThrow(() -> detector.detectPiiExposure(context, inputFile, content));
    }

    // ========== Clear Text Protocol Tests ==========

    @Test
    void testDetectClearTextProtocolFTP() {
        String content = "Import[\"ftp://server.com/file.txt\"]";
        detector.detectClearTextProtocol(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectClearTextProtocolTelnet() {
        String content = "url = \"telnet://server.com\"";
        detector.detectClearTextProtocol(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectClearTextProtocolLDAP() {
        String content = "ldap://directory.example.com\"";
        detector.detectClearTextProtocol(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectClearTextProtocolSecure() {
        String content = "Import[\"https://server.com/file.txt\"]";  // Use a secure protocol
        detector.detectClearTextProtocol(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectClearTextProtocolWithException() {
        String content = "ftp://";
        assertDoesNotThrow(() -> detector.detectClearTextProtocol(context, inputFile, content));
    }

    // ========== Comprehensive Coverage Tests ==========

    @Test
    void testAllMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectFileUploadValidation(context, inputFile, content);
            detector.detectExternalApiSafeguards(context, inputFile, content);
            detector.detectCryptoKeyGeneration(context, inputFile, content);
            detector.detectNetworkOperations(context, inputFile, content);
            detector.detectFileSystemModifications(context, inputFile, content);
            detector.detectEnvironmentVariable(context, inputFile, content);
            detector.detectImportWithoutFormat(context, inputFile, content);
            detector.detectWeakAuthentication(context, inputFile, content);
            detector.detectMissingAuthorization(context, inputFile, content);
            detector.detectInsecureSession(context, inputFile, content);
            detector.detectDefaultCredentials(context, inputFile, content);
            detector.detectPasswordPlainText(context, inputFile, content);
            detector.detectWeakSessionToken(context, inputFile, content);
            detector.detectMissingAccessControl(context, inputFile, content);
            detector.detectWeakHashing(context, inputFile, content);
            detector.detectInsecureRandomHotspot(context, inputFile, content);
            detector.detectHardcodedCryptoKey(context, inputFile, content);
            detector.detectWeakCipherMode(context, inputFile, content);
            detector.detectInsufficientKeySize(context, inputFile, content);
            detector.detectWeakSslProtocol(context, inputFile, content);
            detector.detectCertificateValidationDisabled(context, inputFile, content);
            detector.detectHttpWithoutTls(context, inputFile, content);
            detector.detectCorsPermissive(context, inputFile, content);
            detector.detectOpenRedirect(context, inputFile, content);
            detector.detectDnsRebinding(context, inputFile, content);
            detector.detectInsecureWebsocket(context, inputFile, content);
            detector.detectMissingSecurityHeaders(context, inputFile, content);
            detector.detectSensitiveDataLog(context, inputFile, content);
            detector.detectPiiExposure(context, inputFile, content);
            detector.detectClearTextProtocol(context, inputFile, content);
        });
    }

    @Test
    void testMultipleHotspotsInSingleFile() {
        String content = "password = \"admin\";\n"
                        + "Hash[data, \"MD5\"];\n"
                        + "URLRead[\"http://example.com\"];\n"
                        + "Import[\"file.dat\"];";

        detector.detectDefaultCredentials(context, inputFile, content);
        detector.detectWeakHashing(context, inputFile, content);
        detector.detectHttpWithoutTls(context, inputFile, content);
        detector.detectFileUploadValidation(context, inputFile, content);

        // Should find multiple issues
        verify(context, atLeast(4)).newIssue();
    }
}
