package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControlFlowAndTaintDetectorTest {

    private ControlFlowAndTaintDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new ControlFlowAndTaintDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        when(inputFile.filename()).thenReturn("test.m");
    }

    // ===== DEAD CODE & REACHABILITY TESTS =====

    @Test
    void testDetectUnreachableCodeAfterReturn() {
        String content = "f[x_] := (Return[x]; y = x + 1;)";
        assertDoesNotThrow(() ->
            detector.detectUnreachableCodeAfterReturn(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnreachableBranchAlwaysTrue() {
        String content = "If[True, result, unreachable]";
        assertDoesNotThrow(() ->
            detector.detectUnreachableBranchAlwaysTrue(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnreachableBranchAlwaysFalse() {
        String content = "If[False, unreachable, result]";
        assertDoesNotThrow(() ->
            detector.detectUnreachableBranchAlwaysFalse(context, inputFile, content)
        );
    }

    @Test
    void testDetectImpossiblePattern() {
        String content = "f[x_Integer?StringQ] := x;";
        assertDoesNotThrow(() ->
            detector.detectImpossiblePattern(context, inputFile, content)
        );
    }

    @Test
    void testDetectImpossibleCondition() {
        String content = "If[x > 10 && x < 5, result, other]";
        assertDoesNotThrow(() ->
            detector.detectImpossiblePattern(context, inputFile, content)
        );
    }

    @Test
    void testDetectEmptyCatchBlockEnhanced() {
        String content = "Catch[expr]";
        assertDoesNotThrow(() ->
            detector.detectEmptyCatchBlockEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectConditionAlwaysEvaluatesSame() {
        String content = "x = 5; If[x == 5, True, False]";
        assertDoesNotThrow(() ->
            detector.detectConditionAlwaysEvaluatesSame(context, inputFile, content)
        );
    }

    @Test
    void testDetectInfiniteLoopProven() {
        String content = "While[True, Print[\"loop\"]]";
        assertDoesNotThrow(() ->
            detector.detectInfiniteLoopProven(context, inputFile, content)
        );
    }

    @Test
    void testDetectLoopNeverExecutesWhileFalse() {
        String content = "While[False, Print[\"never\"]]";
        assertDoesNotThrow(() ->
            detector.detectLoopNeverExecutes(context, inputFile, content)
        );
    }

    @Test
    void testDetectLoopNeverExecutesInvertedRange() {
        String content = "Do[Print[i], {i, 10, 1}]";
        assertDoesNotThrow(() ->
            detector.detectLoopNeverExecutes(context, inputFile, content)
        );
    }

    @Test
    void testDetectCodeAfterAbort() {
        String content = "Abort[]; result = 5;";
        assertDoesNotThrow(() ->
            detector.detectCodeAfterAbort(context, inputFile, content)
        );
    }

    @Test
    void testDetectMultipleReturnsMakeCodeUnreachable() {
        String content = "f[x_] := (Return[1]; Return[2]; Return[3]; Return[4];)";
        assertDoesNotThrow(() ->
            detector.detectMultipleReturnsMakeCodeUnreachable(context, inputFile, content)
        );
    }

    @Test
    void testDetectElseBranchNeverTaken() {
        String content = "x = 5; If[x == 5, True, False]";
        assertDoesNotThrow(() ->
            detector.detectElseBranchNeverTaken(context, inputFile, content)
        );
    }

    @Test
    void testDetectSwitchCaseShadowed() {
        String content = "Switch[x, 1, \"one\", _, \"default\", 2, \"two\"]";
        assertDoesNotThrow(() ->
            detector.detectSwitchCaseShadowed(context, inputFile, content)
        );
    }

    @Test
    void testDetectPatternDefinitionShadowed() {
        String content = "f[x_] := 1;\nf[y_Integer] := 2;";
        assertDoesNotThrow(() ->
            detector.detectPatternDefinitionShadowed(context, inputFile, content)
        );
    }

    @Test
    void testDetectExceptionNeverThrown() {
        String content = "Catch[expr, tag]";
        assertDoesNotThrow(() ->
            detector.detectExceptionNeverThrown(context, inputFile, content)
        );
    }

    @Test
    void testDetectBreakOutsideLoop() {
        String content = "x = 5; Break[];";
        assertDoesNotThrow(() ->
            detector.detectBreakOutsideLoop(context, inputFile, content)
        );
    }

    @Test
    void testDetectBreakInsideLoop() {
        String content = "Do[If[i == 5, Break[]], {i, 1, 10}]";
        assertDoesNotThrow(() ->
            detector.detectBreakOutsideLoop(context, inputFile, content)
        );
    }

    // ===== TAINT ANALYSIS FOR SECURITY TESTS =====

    @Test
    void testDetectSqlInjectionTaint() {
        String content = "SQLExecute[conn, \"SELECT * FROM users WHERE name='\" <> userName <> \"'\"]";
        assertDoesNotThrow(() ->
            detector.detectSqlInjectionTaint(context, inputFile, content)
        );
    }

    @Test
    void testDetectCommandInjectionTaint() {
        String content = "userInput = Import[\"input.txt\"];\nRunProcess[{\"sh\", \"-c\", userInput}]";
        detector.initializeCaches(content);
        assertDoesNotThrow(() ->
            detector.detectCommandInjectionTaint(context, inputFile, content)
        );
        detector.clearCaches();
    }

    @Test
    void testDetectCodeInjectionTaint() {
        String content = "userInput = URLFetch[url];\nToExpression[userInput]";
        detector.initializeCaches(content);
        assertDoesNotThrow(() ->
            detector.detectCodeInjectionTaint(context, inputFile, content)
        );
        detector.clearCaches();
    }

    @Test
    void testDetectPathTraversalTaint() {
        String content = "fileName = Import[\"input.txt\"];\nImport[\"/path/to/\" <> fileName]";
        detector.initializeCaches(content);
        assertDoesNotThrow(() ->
            detector.detectPathTraversalTaint(context, inputFile, content)
        );
        detector.clearCaches();
    }

    @Test
    void testDetectXssTaint() {
        String content = "userInput = URLFetch[url];\nExportString[userInput, \"HTML\"]";
        detector.initializeCaches(content);
        assertDoesNotThrow(() ->
            detector.detectXssTaint(context, inputFile, content)
        );
        detector.clearCaches();
    }

    @Test
    void testDetectLdapInjection() {
        String content = "ldapQuery = \"cn=\" <> userName";
        assertDoesNotThrow(() ->
            detector.detectLdapInjection(context, inputFile, content)
        );
    }

    @Test
    void testDetectXxeTaint() {
        String content = "Import[untrustedFile, \"XML\"]";
        assertDoesNotThrow(() ->
            detector.detectXxeTaint(context, inputFile, content)
        );
    }

    @Test
    void testDetectUnsafeDeserializationTaint() {
        String content = "Import[untrustedFile, \"MX\"]";
        assertDoesNotThrow(() ->
            detector.detectUnsafeDeserializationTaint(context, inputFile, content)
        );
    }

    @Test
    void testDetectSsrfTaint() {
        String content = "serverAddress = Import[\"input.txt\"];\nURLFetch[\"http://\" <> serverAddress]";
        detector.initializeCaches(content);
        assertDoesNotThrow(() ->
            detector.detectSsrfTaint(context, inputFile, content)
        );
        detector.clearCaches();
    }

    @Test
    void testDetectInsecureRandomnessEnhanced() {
        String content = "sessionToken = RandomInteger[{1, 1000000}]";
        assertDoesNotThrow(() ->
            detector.detectInsecureRandomnessEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectWeakCryptographyEnhanced() {
        String content = "Hash[password, \"MD5\"]";
        assertDoesNotThrow(() ->
            detector.detectWeakCryptographyEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectWeakCryptographySHA1() {
        String content = "Hash[data, \"SHA1\"]";
        assertDoesNotThrow(() ->
            detector.detectWeakCryptographyEnhanced(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardCodedCredentialsTaint() {
        String content = "password = \"mySecretPassword123\"";
        assertDoesNotThrow(() ->
            detector.detectHardCodedCredentialsTaint(context, inputFile, content)
        );
    }

    @Test
    void testDetectHardCodedApiKey() {
        String content = "apikey = \"sk_live_abc123def456ghi789\"";
        assertDoesNotThrow(() ->
            detector.detectHardCodedCredentialsTaint(context, inputFile, content)
        );
    }

    @Test
    void testDetectSensitiveDataInLogs() {
        String content = "Print[\"User password: \" <> userPassword]";
        assertDoesNotThrow(() ->
            detector.detectSensitiveDataInLogs(context, inputFile, content)
        );
    }

    @Test
    void testDetectSensitiveTokenInLogs() {
        String content = "Print[\"Session token: \" <> sessionToken]";
        assertDoesNotThrow(() ->
            detector.detectSensitiveDataInLogs(context, inputFile, content)
        );
    }

    @Test
    void testDetectMassAssignment() {
        String content = "userData = URLFetch[url];\nSQLExecute[conn, \"UPDATE users SET \" <> userData]";
        detector.initializeCaches(content);
        assertDoesNotThrow(() ->
            detector.detectMassAssignment(context, inputFile, content)
        );
        detector.clearCaches();
    }

    @Test
    void testDetectRegexDoS() {
        String content = "userInput = Import[\"input.txt\"];\nStringMatchQ[text, RegularExpression[userInput]]";
        detector.initializeCaches(content);
        assertDoesNotThrow(() ->
            detector.detectRegexDoS(context, inputFile, content)
        );
        detector.clearCaches();
    }

    // ===== ADDITIONAL CONTROL FLOW TESTS =====

    @Test
    void testDetectMissingDefaultCase() {
        String content = "Switch[x, 1, \"one\", 2, \"two\"]";
        assertDoesNotThrow(() ->
            detector.detectMissingDefaultCase(context, inputFile, content)
        );
    }

    @Test
    void testDetectSwitchWithDefaultCase() {
        String content = "Switch[x, 1, \"one\", 2, \"two\", _, \"default\"]";
        assertDoesNotThrow(() ->
            detector.detectMissingDefaultCase(context, inputFile, content)
        );
    }

    @Test
    void testDetectEmptyIfBranch() {
        String content = "If[condition, , elseBody]";
        assertDoesNotThrow(() ->
            detector.detectEmptyIfBranch(context, inputFile, content)
        );
    }

    @Test
    void testDetectNestedIfDepth() {
        String content = "If[a, If[b, If[c, If[d, If[e, result]]]]]";
        assertDoesNotThrow(() ->
            detector.detectNestedIfDepth(context, inputFile, content)
        );
    }

    @Test
    void testDetectNestedIfDepthMultipleLines() {
        String content = "If[a,\n  If[b,\n    If[c,\n      If[d,\n        If[e, result]\n      ]\n    ]\n  ]\n]";
        assertDoesNotThrow(() ->
            detector.detectNestedIfDepth(context, inputFile, content)
        );
    }

    @Test
    void testDetectTooManyReturnPoints() {
        String content = "f[x_] := (Return[1]; Return[2]; Return[3]; Return[4]; Return[5]; Return[6];)";
        assertDoesNotThrow(() ->
            detector.detectTooManyReturnPoints(context, inputFile, content)
        );
    }

    @Test
    void testDetectMissingElseConsideredHarmful() {
        String content = "If[condition, result]";
        assertDoesNotThrow(() ->
            detector.detectMissingElseConsideredHarmful(context, inputFile, content)
        );
    }

    // ===== COMPREHENSIVE TESTS =====

    @Test
    void testAllDeadCodeMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectUnreachableCodeAfterReturn(context, inputFile, content);
            detector.detectUnreachableBranchAlwaysTrue(context, inputFile, content);
            detector.detectUnreachableBranchAlwaysFalse(context, inputFile, content);
            detector.detectImpossiblePattern(context, inputFile, content);
            detector.detectEmptyCatchBlockEnhanced(context, inputFile, content);
            detector.detectConditionAlwaysEvaluatesSame(context, inputFile, content);
            detector.detectInfiniteLoopProven(context, inputFile, content);
            detector.detectLoopNeverExecutes(context, inputFile, content);
            detector.detectCodeAfterAbort(context, inputFile, content);
            detector.detectMultipleReturnsMakeCodeUnreachable(context, inputFile, content);
            detector.detectElseBranchNeverTaken(context, inputFile, content);
            detector.detectSwitchCaseShadowed(context, inputFile, content);
            detector.detectPatternDefinitionShadowed(context, inputFile, content);
            detector.detectExceptionNeverThrown(context, inputFile, content);
            detector.detectBreakOutsideLoop(context, inputFile, content);
        });
    }

    @Test
    void testAllTaintMethodsWithEmptyContent() {
        String content = "";
        detector.initializeCaches(content);

        assertDoesNotThrow(() -> {
            detector.detectSqlInjectionTaint(context, inputFile, content);
            detector.detectCommandInjectionTaint(context, inputFile, content);
            detector.detectCodeInjectionTaint(context, inputFile, content);
            detector.detectPathTraversalTaint(context, inputFile, content);
            detector.detectXssTaint(context, inputFile, content);
            detector.detectLdapInjection(context, inputFile, content);
            detector.detectXxeTaint(context, inputFile, content);
            detector.detectUnsafeDeserializationTaint(context, inputFile, content);
            detector.detectSsrfTaint(context, inputFile, content);
            detector.detectInsecureRandomnessEnhanced(context, inputFile, content);
            detector.detectWeakCryptographyEnhanced(context, inputFile, content);
            detector.detectHardCodedCredentialsTaint(context, inputFile, content);
            detector.detectSensitiveDataInLogs(context, inputFile, content);
            detector.detectMassAssignment(context, inputFile, content);
            detector.detectRegexDoS(context, inputFile, content);
        });

        detector.clearCaches();
    }

    @Test
    void testAllControlFlowMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectMissingDefaultCase(context, inputFile, content);
            detector.detectEmptyIfBranch(context, inputFile, content);
            detector.detectNestedIfDepth(context, inputFile, content);
            detector.detectTooManyReturnPoints(context, inputFile, content);
            detector.detectMissingElseConsideredHarmful(context, inputFile, content);
        });
    }

    @Test
    void testComplexCodeWithMultipleIssues() {
        String content = "f[x_] := (\n"
                + "  Return[x];\n"
                + "  y = x + 1;\n"
                + "  Return[y];\n"
                + "  Return[z];\n"
                + ");\n"
                + "If[True, result, unreachable];\n"
                + "While[False, Print[\"never\"]];\n"
                + "password = \"hardcoded123\";\n"
                + "Hash[data, \"MD5\"];";

        assertDoesNotThrow(() -> {
            detector.detectUnreachableCodeAfterReturn(context, inputFile, content);
            detector.detectMultipleReturnsMakeCodeUnreachable(context, inputFile, content);
            detector.detectUnreachableBranchAlwaysTrue(context, inputFile, content);
            detector.detectLoopNeverExecutes(context, inputFile, content);
            detector.detectHardCodedCredentialsTaint(context, inputFile, content);
            detector.detectWeakCryptographyEnhanced(context, inputFile, content);
        });
    }

    @Test
    void testSecureCodeWithNoIssues() {
        String content = "f[x_?NumericQ] := If[x != 0, 1/x, $Failed];\n"
                + "password = Environment[\"PASSWORD\"];\n"
                + "Hash[data, \"SHA256\"];\n"
                + "If[condition, result, alternative];\n"
                + "Switch[x, 1, \"one\", 2, \"two\", _, \"default\"];";

        assertDoesNotThrow(() -> {
            detector.detectUnreachableCodeAfterReturn(context, inputFile, content);
            detector.detectHardCodedCredentialsTaint(context, inputFile, content);
            detector.detectWeakCryptographyEnhanced(context, inputFile, content);
            detector.detectMissingDefaultCase(context, inputFile, content);
        });
    }

    @Test
    void testTaintTrackingWithCacheInitialization() {
        String content = "userInput = Import[\"data.txt\"];\n"
                + "query = \"SELECT * FROM users WHERE id=\" <> userInput;\n"
                + "SQLExecute[conn, query];\n"
                + "RunProcess[{\"sh\", \"-c\", userInput}];\n"
                + "ToExpression[userInput];";

        detector.initializeCaches(content);

        assertDoesNotThrow(() -> {
            detector.detectCommandInjectionTaint(context, inputFile, content);
            detector.detectCodeInjectionTaint(context, inputFile, content);
            detector.detectSqlInjectionTaint(context, inputFile, content);
        });

        detector.clearCaches();
    }

    @Test
    void testMultipleTaintSourcesTracking() {
        String content = "data1 = URLFetch[url1];\n"
                + "data2 = Import[file];\n"
                + "data3 = InputString[\"Enter data:\"];\n"
                + "URLFetch[\"http://\" <> data1];\n"
                + "Import[\"/path/\" <> data2];\n"
                + "ToExpression[data3];";

        detector.initializeCaches(content);

        assertDoesNotThrow(() -> {
            detector.detectSsrfTaint(context, inputFile, content);
            detector.detectPathTraversalTaint(context, inputFile, content);
            detector.detectCodeInjectionTaint(context, inputFile, content);
        });

        detector.clearCaches();
    }

    @Test
    void testControlFlowComplexScenarios() {
        String content = "f[x_] := Switch[x,\n"
                + "  1, \"one\",\n"
                + "  2, \"two\",\n"
                + "  _, \"default\",\n"
                + "  3, \"three\"\n"
                + "];\n"
                + "g[y_] := (\n"
                + "  Return[1];\n"
                + "  Return[2];\n"
                + "  Return[3];\n"
                + "  Return[4];\n"
                + "  Return[5];\n"
                + "  Return[6];\n"
                + ");";

        assertDoesNotThrow(() -> {
            detector.detectSwitchCaseShadowed(context, inputFile, content);
            detector.detectTooManyReturnPoints(context, inputFile, content);
        });
    }

    @Test
    void testInitializeAndClearCaches() {
        String content = "data = Import[\"file.txt\"];\nresult = data + 5;";

        assertDoesNotThrow(() -> {
            detector.initializeCaches(content);
            detector.clearCaches();
            detector.initializeCaches(content);
            detector.clearCaches();
        });
    }
}
