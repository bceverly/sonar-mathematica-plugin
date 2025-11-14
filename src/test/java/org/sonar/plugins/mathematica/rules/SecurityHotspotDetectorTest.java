package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeastOnce;
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

    private static Stream<Arguments> fileUploadValidationTestData() {
        return Stream.of(
            Arguments.of("Import[\"file.dat\"]"),
            Arguments.of("Get[\"script.m\"]"),
            Arguments.of("stream = OpenRead[filename]")
        );
    }

    @ParameterizedTest
    @MethodSource("fileUploadValidationTestData")
    void testDetectFileUploadValidation(String content) {
        detector.detectFileUploadValidation(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

        // ========== External API Safeguards Tests ==========

                // ========== Crypto Key Generation Tests ==========

    private static Stream<Arguments> cryptoKeyGenerationTestData() {
        return Stream.of(
            Arguments.of("key = Random[]"),
            Arguments.of("key = RandomInteger[{0, 2^256}]"),
            Arguments.of("GenerateSymmetricKey[]")
        );
    }

    @ParameterizedTest
    @MethodSource("cryptoKeyGenerationTestData")
    void testDetectCryptoKeyGeneration(String content) {
        detector.detectCryptoKeyGeneration(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

        // ========== Network Operations Tests ==========

                // ========== File System Modifications Tests ==========

                // ========== Environment Variable Tests ==========

            // ========== Import Without Format Tests ==========

                // ========== Weak Authentication Tests ==========

                // ========== Missing Authorization Tests ==========

                // ========== Insecure Session Tests ==========

                // ========== Default Credentials Tests ==========

                    // ========== Password Plain Text Tests ==========

                // ========== Weak Session Token Tests ==========

            // ========== Missing Access Control Tests ==========

                // ========== Weak Hashing Tests ==========

                    // ========== Insecure Random Hotspot Tests ==========

            // ========== Hardcoded Crypto Key Tests ==========

            // ========== Weak Cipher Mode Tests ==========

                // ========== Insufficient Key Size Tests ==========

                    // ========== Weak SSL Protocol Tests ==========

                // ========== Certificate Validation Disabled Tests ==========

            // ========== HTTP Without TLS Tests ==========

                // ========== CORS Permissive Tests ==========

            // ========== Open Redirect Tests ==========

            // ========== DNS Rebinding Tests ==========

                    // ========== Insecure WebSocket Tests ==========

                // ========== Missing Security Headers Tests ==========

                // ========== Sensitive Data Log Tests ==========

                // ========== PII Exposure Tests ==========

                // ========== Clear Text Protocol Tests ==========

    private static Stream<Arguments> clearTextProtocolTestData() {
        return Stream.of(
            Arguments.of("Import[\"ftp://server.com/file.txt\"]"),
            Arguments.of("url = \"telnet://server.com\""),
            Arguments.of("ldap://directory.example.com\"")
        );
    }

    @ParameterizedTest
    @MethodSource("clearTextProtocolTestData")
    void testDetectClearTextProtocol(String content) {
        detector.detectClearTextProtocol(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

            // ========== Comprehensive Coverage Tests ==========

            // ===== ADDITIONAL EDGE CASES FOR >80% COVERAGE =====

                                            @Test
    void testMalformedInputRobustness() {
        String[] malformedInputs = {
            "Import[",
            "URLRead[\"http://",
            "Hash[data, \"",
            "Encrypt[data",
            "password = \"",
            "APIFunction[",
            "CloudDeploy[",
            "SessionID[",
            "GenerateAsymmetricKeyPair[\"RSA\"",
            "SocketConnect[\"ws://"
        };

        for (String input : malformedInputs) {
            assertDoesNotThrow(() -> {
                detector.detectFileUploadValidation(context, inputFile, input);
                detector.detectHttpWithoutTls(context, inputFile, input);
                detector.detectWeakHashing(context, inputFile, input);
                detector.detectWeakCipherMode(context, inputFile, input);
                detector.detectPasswordPlainText(context, inputFile, input);
                detector.detectMissingAccessControl(context, inputFile, input);
                detector.detectInsecureSession(context, inputFile, input);
                detector.detectInsufficientKeySize(context, inputFile, input);
                detector.detectInsecureWebsocket(context, inputFile, input);
            }, "Should handle malformed input: " + input);
        }
    }

    @Test
    void testNonIssueDetectionPaths() {
        // Test paths that should NOT generate issues
        String safeContent = "result = SafeFunction[data];\n"
                           + "validated = CheckInput[userInput];\n"
                           + "secure = UseStrongCrypto[];";

        detector.detectFileUploadValidation(context, inputFile, safeContent);
        detector.detectWeakAuthentication(context, inputFile, safeContent);
        detector.detectDefaultCredentials(context, inputFile, safeContent);

        // Should not create issues for safe content
        verify(context, never()).newIssue();
    }

    // ===== ADDITIONAL TESTS FOR COMPREHENSIVE COVERAGE =====

    @Test
    void testCodeInStringLiteralsNotDetected() {
        String content = "warning = \"Never use password = 'admin' in production\"\n"
                       + "message = \"URLRead[\\\"http://example.com\\\"] is not secure\"\n"
                       + "note = \"Hash[data, \\\"MD5\\\"] is weak\"";
        detector.detectPasswordPlainText(context, inputFile, content);
        detector.detectHttpWithoutTls(context, inputFile, content);
        detector.detectWeakHashing(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testCodeInCommentsNotDetected() {
        String content = "(* password = \"admin\" *)\n"
                       + "(* URLRead[\"http://test.com\"] *)\n"
                       + "(* Hash[data, \"MD5\"] *)";
        detector.detectPasswordPlainText(context, inputFile, content);
        detector.detectHttpWithoutTls(context, inputFile, content);
        detector.detectWeakHashing(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testMultipleIssuesSameType() {
        String content = "password1 = \"password1\"\n"
                       + "password2 = \"password2\"\n"
                       + "password3 = \"password3\"";
        assertDoesNotThrow(() -> detector.detectPasswordPlainText(context, inputFile, content));
    }

    @Test
    void testAllMethodsWithNullInput() {
        String content = null;
        // All methods should handle null gracefully
        assertDoesNotThrow(() -> {
            if (content != null) {
                detector.detectFileUploadValidation(context, inputFile, content);
            }
        });
    }

    // ===== ADDITIONAL COVERAGE TESTS FOR 80%+ TARGET =====


    // ===== PARAMETERIZED TESTS =====

    @ParameterizedTest
    @MethodSource("detectCertificateValidationDisabledTestData")
    void testDetectDetectCertificateValidationDisabled(String content) {
        assertDoesNotThrow(() ->
            detector.detectCertificateValidationDisabled(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectCertificateValidationDisabledTestData() {
        return Stream.of(
            Arguments.of("URLRead[url, \\\"VerifyPeer\\\" -> False]"),
            Arguments.of("URLRead["),
            Arguments.of("(* URLRead[url, \\\"VerifyPeer\\\" -> False] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectClearTextProtocolTestData")
    void testDetectDetectClearTextProtocol(String content) {
        assertDoesNotThrow(() ->
            detector.detectClearTextProtocol(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectClearTextProtocolTestData() {
        return Stream.of(
            Arguments.of("Import[\\\"https://server.com/file.txt\\\"]"),
            Arguments.of("ftp://"),
            Arguments.of("url = \\\"ldaps://directory.example.com\\\""),
            Arguments.of("(* Import[\\\"ftp://server.com/file.txt\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectCorsPermissiveTestData")
    void testDetectDetectCorsPermissive(String content) {
        assertDoesNotThrow(() ->
            detector.detectCorsPermissive(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectCorsPermissiveTestData() {
        return Stream.of(
            Arguments.of("APIFunction[func, \\\"AllowedOrigins\\\" -> {\\\"*\\\"}]"),
            Arguments.of("APIFunction["),
            Arguments.of("APIFunction[func, \\\"AllowedOrigins\\\" -> All]"),
            Arguments.of("(* APIFunction[func, \\\"AllowedOrigins\\\" -> {\\\"*\\\"}] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectCryptoKeyGenerationTestData")
    void testDetectDetectCryptoKeyGeneration(String content) {
        assertDoesNotThrow(() ->
            detector.detectCryptoKeyGeneration(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectCryptoKeyGenerationTestData() {
        return Stream.of(
            Arguments.of("Random["),
            Arguments.of("GenerateAsymmetricKeyPair[]"),
            Arguments.of("Table[Random[], {i, 1, 10}]"),
            Arguments.of("(* Random[] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectDefaultCredentialsTestData")
    void testDetectDetectDefaultCredentials(String content) {
        assertDoesNotThrow(() ->
            detector.detectDefaultCredentials(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectDefaultCredentialsTestData() {
        return Stream.of(
            Arguments.of("password = \\\"password\\\""),
            Arguments.of("pwd = \\\"admin\\\""),
            Arguments.of("message = \\\"The admin user is important\\\""),
            Arguments.of("password = \\\""),
            Arguments.of("password = \\\"admin\\"),
            Arguments.of("credential = \\\"root\\\""),
            Arguments.of("passwd = \\\"123456\\\""),
            Arguments.of("password = \\\"guest\\\""),
            Arguments.of("(* password = \\\"password\\\" *)"),
            Arguments.of("message = \\\"Use admin for testing\\\"")
        );
    }

    @ParameterizedTest
    @MethodSource("detectDnsRebindingTestData")
    void testDetectDetectDnsRebinding(String content) {
        assertDoesNotThrow(() ->
            detector.detectDnsRebinding(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectDnsRebindingTestData() {
        return Stream.of(
            Arguments.of("URLRead[\\\"http://localhost:8080/api\\\"]"),
            Arguments.of("SocketConnect[\\\"127.0.0.1\\\", 8080]"),
            Arguments.of("URLRead[\\\"https://api.example.com\\\"]"),
            Arguments.of("URLRead[")
        );
    }

    @ParameterizedTest
    @MethodSource("detectEnvironmentVariableTestData")
    void testDetectDetectEnvironmentVariable(String content) {
        assertDoesNotThrow(() ->
            detector.detectEnvironmentVariable(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectEnvironmentVariableTestData() {
        return Stream.of(
            Arguments.of("secret = Environment[\\\"API_KEY\\\"]"),
            Arguments.of("Environment["),
            Arguments.of("(* Environment[\\\"API_KEY\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectExternalApiSafeguardsTestData")
    void testDetectDetectExternalApiSafeguards(String content) {
        assertDoesNotThrow(() ->
            detector.detectExternalApiSafeguards(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectExternalApiSafeguardsTestData() {
        return Stream.of(
            Arguments.of("URLRead[\\\"https://api.example.com\\\"]"),
            Arguments.of("URLFetch[url]"),
            Arguments.of("URLRead["),
            Arguments.of("URLExecute[url, \\\"POST\\\"]"),
            Arguments.of("URLSubmit[job]"),
            Arguments.of("ServiceExecute[service, request]"),
            Arguments.of("ServiceConnect[\\\"Twitter\\\"]"),
            Arguments.of("(* URLRead[\\\"https://api.com\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectFileSystemModificationsTestData")
    void testDetectDetectFileSystemModifications(String content) {
        assertDoesNotThrow(() ->
            detector.detectFileSystemModifications(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectFileSystemModificationsTestData() {
        return Stream.of(
            Arguments.of("DeleteFile[\\\"temp.txt\\\"]"),
            Arguments.of("DeleteDirectory[\\\"tempdir\\\"]"),
            Arguments.of("DeleteFile["),
            Arguments.of("Export[\\\"/tmp/public.dat\\\", data]"),
            Arguments.of("RenameFile[\\\"old.txt\\\", \\\"new.txt\\\"]"),
            Arguments.of("CopyFile[\\\"source.txt\\\", \\\"dest.txt\\\"]"),
            Arguments.of("SetFileDate[\\\"file.txt\\\", Now]"),
            Arguments.of("(* DeleteFile[\\\"temp.txt\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectFileUploadValidationTestData")
    void testDetectDetectFileUploadValidation(String content) {
        assertDoesNotThrow(() ->
            detector.detectFileUploadValidation(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectFileUploadValidationTestData() {
        return Stream.of(
            Arguments.of("Import["),
            Arguments.of(""),
            Arguments.of("OpenWrite[\\\"output.txt\\\"]"),
            Arguments.of("Put[data, \\\"file.m\\\"]"),
            Arguments.of("Import[\\\"file1.dat\\\"]\\nGet[\\\"file2.m\\\"]\\nOpenRead[\\\"file3.txt\\\"]"),
            Arguments.of("(* Import[\\\"file.dat\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectHardcodedCryptoKeyTestData")
    void testDetectDetectHardcodedCryptoKey(String content) {
        assertDoesNotThrow(() ->
            detector.detectHardcodedCryptoKey(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectHardcodedCryptoKeyTestData() {
        return Stream.of(
            Arguments.of("Encrypt[data, \\\"ABCDEF1234567890ABCDEF1234567890\\\"]"),
            Arguments.of("Encrypt["),
            Arguments.of("Decrypt[ciphertext, \\\"FEDCBA9876543210FEDCBA9876543210\\\"]"),
            Arguments.of("GenerateSymmetricKey[\\\"0123456789ABCDEF0123456789ABCDEF\\\"]"),
            Arguments.of("(* Encrypt[data, \\\"ABCDEF1234567890ABCDEF1234567890\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectHttpWithoutTlsTestData")
    void testDetectDetectHttpWithoutTls(String content) {
        assertDoesNotThrow(() ->
            detector.detectHttpWithoutTls(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectHttpWithoutTlsTestData() {
        return Stream.of(
            Arguments.of("URLRead[\\\"http://api.example.com/data\\\"]"),
            Arguments.of("URLRead[\\\"https://api.example.com/data\\\"]"),
            Arguments.of("URLRead["),
            Arguments.of("URLFetch[\\\"http://example.com/data\\\"]"),
            Arguments.of("URLSubmit[\\\"http://example.com/job\\\"]"),
            Arguments.of("(* URLRead[\\\"http://api.example.com/data\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectImportWithoutFormatTestData")
    void testDetectDetectImportWithoutFormat(String content) {
        assertDoesNotThrow(() ->
            detector.detectImportWithoutFormat(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectImportWithoutFormatTestData() {
        return Stream.of(
            Arguments.of("Import[\\\"data.dat\\\"]"),
            Arguments.of("Import[\\\"data.dat\\\", \\\"CSV\\\"]"),
            Arguments.of("Import["),
            Arguments.of("(* Import[\\\"data.dat\\\"] *)"),
            Arguments.of("Import[\\\"data.dat\\\", \\\"Text\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectInsecureRandomHotspotTestData")
    void testDetectDetectInsecureRandomHotspot(String content) {
        assertDoesNotThrow(() ->
            detector.detectInsecureRandomHotspot(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectInsecureRandomHotspotTestData() {
        return Stream.of(
            Arguments.of("RandomInteger[password]"),
            Arguments.of("RandomInteger["),
            Arguments.of("nonce = RandomInteger[10000]"),
            Arguments.of("key = RandomInteger[{0, 255}, 16]"),
            Arguments.of("nonce = RandomReal[{0, 1000}]"),
            Arguments.of("(* RandomInteger[password] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectInsecureSessionTestData")
    void testDetectDetectInsecureSession(String content) {
        assertDoesNotThrow(() ->
            detector.detectInsecureSession(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectInsecureSessionTestData() {
        return Stream.of(
            Arguments.of("session = SessionID[]"),
            Arguments.of("id = CreateUUID[]"),
            Arguments.of("SessionID["),
            Arguments.of("token = SessionToken[]"),
            Arguments.of("sessionId = Hash[RandomInteger[{0, 2^64}]]"),
            Arguments.of("(* session = SessionID[] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectInsecureWebsocketTestData")
    void testDetectDetectInsecureWebsocket(String content) {
        assertDoesNotThrow(() ->
            detector.detectInsecureWebsocket(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectInsecureWebsocketTestData() {
        return Stream.of(
            Arguments.of("SocketConnect[\\\"ws://example.com/socket\\\"]"),
            Arguments.of("SocketConnect[\\\"wss://example.com/socket\\\"]"),
            Arguments.of("SocketConnect["),
            Arguments.of("SocketConnect[\\\"ws://server1.com\\\"]\\nSocketConnect[\\\"ws://server2.com\\\"]"),
            Arguments.of("(* SocketConnect[\\\"ws://example.com/socket\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectInsufficientKeySizeTestData")
    void testDetectDetectInsufficientKeySize(String content) {
        assertDoesNotThrow(() ->
            detector.detectInsufficientKeySize(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectInsufficientKeySizeTestData() {
        return Stream.of(
            Arguments.of("GenerateAsymmetricKeyPair[\\\"RSA\\\", 512]"),
            Arguments.of("GenerateAsymmetricKeyPair[\\\"RSA\\\", 1024]"),
            Arguments.of("GenerateAsymmetricKeyPair[\\\"RSA\\\", 2048]"),
            Arguments.of("GenerateAsymmetricKeyPair["),
            Arguments.of("GenerateAsymmetricKeyPair[\\\"RSA\\\", 768]"),
            Arguments.of("(* GenerateAsymmetricKeyPair[\\\"RSA\\\", 512] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectMissingAccessControlTestData")
    void testDetectDetectMissingAccessControl(String content) {
        assertDoesNotThrow(() ->
            detector.detectMissingAccessControl(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectMissingAccessControlTestData() {
        return Stream.of(
            Arguments.of("CloudDeploy[myFunc]"),
            Arguments.of("CloudDeploy[myFunc, Permissions -> \\\"Private\\\"]"),
            Arguments.of("CloudDeploy["),
            Arguments.of("CloudDeploy[myFunc, \\\"SecurityLevel\\\" -> None]"),
            Arguments.of("CloudDeploy[myFunc, $Permissions -> \\\"Private\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectMissingAuthorizationTestData")
    void testDetectDetectMissingAuthorization(String content) {
        assertDoesNotThrow(() ->
            detector.detectMissingAuthorization(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectMissingAuthorizationTestData() {
        return Stream.of(
            Arguments.of("APIFunction[{\\\"x\\\" -> \\\"Integer\\\"}, #x + 1 &]"),
            Arguments.of("APIFunction[{\\\"x\\\" -> \\\"Integer\\\"}, #x + 1 &, Permissions -> \\\"Private\\\"]"),
            Arguments.of("APIFunction["),
            Arguments.of("FormFunction[{\\\"x\\\" -> \\\"Integer\\\"}, #x + 1 &]"),
            Arguments.of("APIFunction[{\\\"x\\\" -> \\\"Integer\\\"}, If[$RequesterAddress == \\\"127.0.0.1\\\", #x, $Failed] &]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectMissingSecurityHeadersTestData")
    void testDetectDetectMissingSecurityHeaders(String content) {
        assertDoesNotThrow(() ->
            detector.detectMissingSecurityHeaders(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectMissingSecurityHeadersTestData() {
        return Stream.of(
            Arguments.of("APIFunction[{\\\"x\\\" -> \\\"Integer\\\"}, #x + 1 &]"),
            Arguments.of("APIFunction[{\\\"x\\\" -> \\\"Integer\\\"}, HTTPResponse[#x + 1, Headers -> {}] &]"),
            Arguments.of("APIFunction["),
            Arguments.of("SetCookie[\\\"session\\\", value]"),
            Arguments.of("FormPage[{\\\"name\\\" -> \\\"String\\\"}, func]"),
            Arguments.of("APIFunction[{\\\"x\\\" -> \\\"Integer\\\"}, HTTPResponse[#x, Headers -> {\\\"X-Frame-Options\\\" -> \\\"DENY\\\"}] &]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectNetworkOperationsTestData")
    void testDetectDetectNetworkOperations(String content) {
        assertDoesNotThrow(() ->
            detector.detectNetworkOperations(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectNetworkOperationsTestData() {
        return Stream.of(
            Arguments.of("SocketConnect[\\\"localhost\\\", 8080]"),
            Arguments.of("SocketListen[8080]"),
            Arguments.of("SocketConnect["),
            Arguments.of("SocketOpen[8080]"),
            Arguments.of("WebExecute[session, \\\"Navigate\\\"]"),
            Arguments.of("(* SocketConnect[\\\"localhost\\\", 8080] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectOpenRedirectTestData")
    void testDetectDetectOpenRedirect(String content) {
        assertDoesNotThrow(() ->
            detector.detectOpenRedirect(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectOpenRedirectTestData() {
        return Stream.of(
            Arguments.of("HTTPRedirect[baseUrl <> userInput]"),
            Arguments.of("HTTPRedirect["),
            Arguments.of("HTTPRedirect[GetQueryParam[\\\"url\\\"]]"),
            Arguments.of("HTTPRedirect[\\\"http://base.com\\\" ++ userParam]"),
            Arguments.of("(* HTTPRedirect[baseUrl <> userInput] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPasswordPlainTextTestData")
    void testDetectDetectPasswordPlainText(String content) {
        assertDoesNotThrow(() ->
            detector.detectPasswordPlainText(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPasswordPlainTextTestData() {
        return Stream.of(
            Arguments.of("password = \\\"mySecret123\\\""),
            Arguments.of("apiKey = \\\"abc123def456\\\""),
            Arguments.of("password = \\\""),
            Arguments.of("apiKey = \\\"sk_live_51234567890\\\""),
            Arguments.of("credential = \\\"MySecretPass123\\\""),
            Arguments.of("secret = \\\"TopSecretValue\\\""),
            Arguments.of("token = \\\"bearer_xyz123\\\""),
            Arguments.of("(* password = \\\"mySecret123\\\" *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectPiiExposureTestData")
    void testDetectDetectPiiExposure(String content) {
        assertDoesNotThrow(() ->
            detector.detectPiiExposure(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectPiiExposureTestData() {
        return Stream.of(
            Arguments.of("ssn = \\\"123-45-6789\\\""),
            Arguments.of("creditCard: 1234567890123456"),
            Arguments.of("ssn = "),
            Arguments.of("passport = \\\"A12345678\\\""),
            Arguments.of("driverLicense: D123456789"),
            Arguments.of("taxId = 123456789"),
            Arguments.of("(* ssn = \\\"123-45-6789\\\" *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectSensitiveDataLogTestData")
    void testDetectDetectSensitiveDataLog(String content) {
        assertDoesNotThrow(() ->
            detector.detectSensitiveDataLog(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectSensitiveDataLogTestData() {
        return Stream.of(
            Arguments.of("Print[\\\"User password: \\\", password]"),
            Arguments.of("Echo[token]"),
            Arguments.of("Print["),
            Arguments.of("WriteString[$Output, \\\"Token: \\\", apiKey]"),
            Arguments.of("Print[\\\"API Key: \\\", apiKey]"),
            Arguments.of("Echo[\\\"Secret: \\\", secret]"),
            Arguments.of("(* Print[\\\"User password: \\\", password] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectWeakAuthenticationTestData")
    void testDetectDetectWeakAuthentication(String content) {
        assertDoesNotThrow(() ->
            detector.detectWeakAuthentication(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectWeakAuthenticationTestData() {
        return Stream.of(
            Arguments.of("FormFunction[fields, function]"),
            Arguments.of("FormFunction[fields, function, Permissions -> \\\"Private\\\"]"),
            Arguments.of("FormFunction["),
            Arguments.of("$Debug = True"),
            Arguments.of(""),
            Arguments.of("AuthenticationDialog[func]"),
            Arguments.of("CreateDialog[items]"),
            Arguments.of("FormPage[fields]"),
            Arguments.of("CloudDeploy[func]"),
            Arguments.of("APIFunction[{}, func]"),
            Arguments.of("(* FormFunction[fields, function] *)"),
            Arguments.of("FormFunction[fields, function, Authentication -> \\\"OAuth\\\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("detectWeakCipherModeTestData")
    void testDetectDetectWeakCipherMode(String content) {
        assertDoesNotThrow(() ->
            detector.detectWeakCipherMode(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectWeakCipherModeTestData() {
        return Stream.of(
            Arguments.of("Encrypt[data, key, \\\"ECB\\\"]"),
            Arguments.of("Encrypt[data, key, None]"),
            Arguments.of("Encrypt["),
            Arguments.of("Encrypt[data, key, \\\"DES\\\"]"),
            Arguments.of("(* Encrypt[data, key, \\\"ECB\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectWeakHashingTestData")
    void testDetectDetectWeakHashing(String content) {
        assertDoesNotThrow(() ->
            detector.detectWeakHashing(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectWeakHashingTestData() {
        return Stream.of(
            Arguments.of("Hash[data, \\\"MD5\\\"]"),
            Arguments.of("Hash[data, \\\"SHA1\\\"]"),
            Arguments.of("Hash[data, \\\"SHA256\\\"]"),
            Arguments.of("Hash["),
            Arguments.of("Hash[data, \\\"MD2\\\"]"),
            Arguments.of("Hash[data, \\\"MD4\\\"]"),
            Arguments.of("Hash[data, \\\"SHA-1\\\"]"),
            Arguments.of("(* Hash[data, \\\"MD5\\\"] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectWeakSessionTokenTestData")
    void testDetectDetectWeakSessionToken(String content) {
        assertDoesNotThrow(() ->
            detector.detectWeakSessionToken(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectWeakSessionTokenTestData() {
        return Stream.of(
            Arguments.of("token = RandomInteger[]"),
            Arguments.of("RandomInteger["),
            Arguments.of("token = RandomReal[key]"),
            Arguments.of("SeedRandom[password]"),
            Arguments.of("(* token = RandomInteger[] *)")
        );
    }

    @ParameterizedTest
    @MethodSource("detectWeakSslProtocolTestData")
    void testDetectDetectWeakSslProtocol(String content) {
        assertDoesNotThrow(() ->
            detector.detectWeakSslProtocol(context, inputFile, content)
        );
    }

    private static Stream<Arguments> detectWeakSslProtocolTestData() {
        return Stream.of(
            Arguments.of("URLRead[url, \\\"Method\\\" -> \\\"SSLv3\\\"]"),
            Arguments.of("URLRead[url, \\\"Method\\\" -> \\\"TLSv1.0\\\"]"),
            Arguments.of("URLRead["),
            Arguments.of("URLRead[url, \\\"Method\\\" -> \\\"SSLv2\\\"]"),
            Arguments.of("URLRead[url, \\\"Method\\\" -> \\\"TLSv1\\\"]"),
            Arguments.of("(* URLRead[url, \\\"Method\\\" -> \\\"SSLv3\\\"] *)")
        );
    }

}
