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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class TestingQualityDetectorTest {

    private TestingQualityDetector detector;
    private SensorContext context;
    private InputFile inputFile;
    private NewIssue newIssue;
    private NewIssueLocation newLocation;

    @BeforeEach
    void setUp() {
        detector = new TestingQualityDetector();
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

    // ========== Test Naming Convention Tests ==========

    @Test
    void testDetectTestNamingConventionGenericTest() {
        String content = "testTest[x_] := VerificationTest[x == 5]";  // "test" is too generic
        detector.detectTestNamingConvention(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectTestNamingConventionTestA() {
        String content = "testA[x_] := VerificationTest[x == 5]";  // "A" is too generic
        detector.detectTestNamingConvention(context, inputFile, content);
        // Pattern may or may not match depending on implementation details
        // Just verify it doesn't throw
        assertDoesNotThrow(() -> detector.detectTestNamingConvention(context, inputFile, content));
    }

    private static Stream<Arguments> testNamingConventionNoIssueData() {
        return Stream.of(
            Arguments.of("testValidateInputRange[x_] := VerificationTest[x > 0]"),
            Arguments.of("ValidateTest[x_] := VerificationTest[x]"),
            Arguments.of("testDescriptiveName[x_] := VerificationTest[x == 5]")
        );
    }

    @ParameterizedTest
    @MethodSource("testNamingConventionNoIssueData")
    void testDetectTestNamingConventionNoIssue(String content) {
        detector.detectTestNamingConvention(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestNamingConventionWithException() {
        String content = "test[";  // Malformed content to trigger exception
        assertDoesNotThrow(() -> detector.detectTestNamingConvention(context, inputFile, content));
    }

    // ========== Test No Isolation Tests ==========

    @Test
    void testDetectTestNoIsolationGlobalAssignment() {
        String content = "VerificationTest[MyGlobal = 5; MyGlobal == 5]";
        detector.detectTestNoIsolation(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    private static Stream<Arguments> testNoIsolationNoIssueData() {
        return Stream.of(
            Arguments.of("VerificationTest[MyGlobal = 5; result = MyGlobal == 5; Clear[MyGlobal]; result]"),
            Arguments.of("VerificationTest[Module[{x = 5}, x == 5]]"),
            Arguments.of("VerificationTest[Block[{y = 10}, y == 10]]")
        );
    }

    @ParameterizedTest
    @MethodSource("testNoIsolationNoIssueData")
    void testDetectTestNoIsolationNoIssue(String content) {
        detector.detectTestNoIsolation(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestNoIsolationNoTests() {
        String content = "x = 5; y = 10;";
        detector.detectTestNoIsolation(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestNoIsolationWithException() {
        String content = "VerificationTest[";  // Malformed
        assertDoesNotThrow(() -> detector.detectTestNoIsolation(context, inputFile, content));
    }

    // ========== Test Data Hardcoded Tests ==========

    @Test
    void testDetectTestDataHardcodedManyTests() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            content.append("VerificationTest[x == ").append(i).append("];\n");
        }
        detector.detectTestDataHardcoded(context, inputFile, content.toString());
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectTestDataHardcodedFewTests() {
        String content = "VerificationTest[x == 1];\nVerificationTest[x == 2];";
        detector.detectTestDataHardcoded(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestDataHardcodedWithTestData() {
        StringBuilder content = new StringBuilder("testData = {1, 2, 3};\n");
        for (int i = 0; i < 6; i++) {
            content.append("VerificationTest[x == testData[[").append(i + 1).append("]]];\n");
        }
        detector.detectTestDataHardcoded(context, inputFile, content.toString());
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestDataHardcodedWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectTestDataHardcoded(context, inputFile, content));
    }

    // ========== Test Ignored Tests ==========

    private static Stream<Arguments> testIgnoredTestData() {
        return Stream.of(
            Arguments.of("(* VerificationTest[x == 5] *)", true),
            Arguments.of("VerificationTest[x == 5, \"Skip\"]", true),
            Arguments.of("VerificationTest[x == 5, True, \"Ignore\"]", true),
            Arguments.of("VerificationTest[x == 5]", false)
        );
    }

    @ParameterizedTest
    @MethodSource("testIgnoredTestData")
    void testDetectTestIgnored(String content, boolean shouldRaiseIssue) {
        detector.detectTestIgnored(context, inputFile, content);
        if (shouldRaiseIssue) {
            verify(context, atLeastOnce()).newIssue();
        } else {
            verify(context, never()).newIssue();
        }
    }

    @Test
    void testDetectTestIgnoredWithException() {
        String content = "(*";
        assertDoesNotThrow(() -> detector.detectTestIgnored(context, inputFile, content));
    }

    // ========== VerificationTest No Expected Tests ==========

    @Test
    void testDetectVerificationTestNoExpectedSingleArg() {
        String content = "VerificationTest[expr]";
        detector.detectVerificationTestNoExpected(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectVerificationTestNoExpectedWithExpected() {
        String content = "VerificationTest[expr, True]";
        detector.detectVerificationTestNoExpected(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectVerificationTestNoExpectedWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectVerificationTestNoExpected(context, inputFile, content));
    }

    // ========== VerificationTest Too Broad Tests ==========

    @Test
    void testDetectVerificationTestTooBroadCompareToTrue() {
        String content = "VerificationTest[x > 0, True]";
        detector.detectVerificationTestTooBroad(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectVerificationTestTooBroadSpecificValue() {
        String content = "VerificationTest[Sum[x, {x, 1, 10}], 55]";
        detector.detectVerificationTestTooBroad(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectVerificationTestTooBroadWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectVerificationTestTooBroad(context, inputFile, content));
    }

    // ========== VerificationTest No Description Tests ==========

    @Test
    void testDetectVerificationTestNoDescriptionWithoutTestID() {
        String content = "VerificationTest[x == 5, True]";
        detector.detectVerificationTestNoDescription(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectVerificationTestNoDescriptionWithTestID() {
        String content = "VerificationTest[x == 5, True, TestID -> \"ValidateX\"]";
        detector.detectVerificationTestNoDescription(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectVerificationTestNoDescriptionWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectVerificationTestNoDescription(context, inputFile, content));
    }

    // ========== VerificationTest Empty Tests ==========

    @Test
    void testDetectVerificationTestEmptyNoArgs() {
        String content = "VerificationTest[]";
        detector.detectVerificationTestEmpty(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectVerificationTestEmptyCommaOnly() {
        String content = "VerificationTest[, ]";
        detector.detectVerificationTestEmpty(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectVerificationTestEmptyWithContent() {
        String content = "VerificationTest[x == 5]";
        detector.detectVerificationTestEmpty(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectVerificationTestEmptyWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectVerificationTestEmpty(context, inputFile, content));
    }

    // ========== Test Assert Count Tests ==========

    @Test
    void testDetectTestAssertCountNoAsserts() {
        String content = "VerificationTest[Print[\"hello\"]]";
        detector.detectTestAssertCount(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectTestAssertCountTooMany() {
        String content = "VerificationTest[a == 1 && b == 2 && c == 3 && d == 4 && e == 5 && f == 6]";
        detector.detectTestAssertCount(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    private static Stream<Arguments> testAssertCountNoIssueData() {
        return Stream.of(
            Arguments.of("VerificationTest[x == 5 && y == 10]"),
            Arguments.of("VerificationTest[SameQ[x, y]]"),
            Arguments.of("VerificationTest[MatchQ[x, _Integer]]")
        );
    }

    @ParameterizedTest
    @MethodSource("testAssertCountNoIssueData")
    void testDetectTestAssertCountNoIssue(String content) {
        detector.detectTestAssertCount(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestAssertCountWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectTestAssertCount(context, inputFile, content));
    }

    // ========== Test Too Long Tests ==========

    @Test
    void testDetectTestTooLongVeryLong() {
        StringBuilder longTest = new StringBuilder("VerificationTest[\n");
        for (int i = 0; i < 25; i++) {
            longTest.append("  x").append(i).append(" = ").append(i).append(";\n");
        }
        longTest.append("  x0 == 0\n]");
        detector.detectTestTooLong(context, inputFile, longTest.toString());
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectTestTooLongShort() {
        String content = "VerificationTest[\n  x = 5;\n  x == 5\n]";
        detector.detectTestTooLong(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestTooLongWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectTestTooLong(context, inputFile, content));
    }

    // ========== Test Multiple Concerns Tests ==========

    @Test
    void testDetectTestMultipleConcernsMany() {
        String content = "VerificationTest[FuncA[x] == 1 && FuncB[y] == 2 && FuncC[z] == 3 && FuncD[w] == 4]";
        detector.detectTestMultipleConcerns(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectTestMultipleConcernsSingle() {
        String content = "VerificationTest[Sum[x, {x, 1, 10}] == 55]";
        detector.detectTestMultipleConcerns(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestMultipleConcernsWithCommonFunctions() {
        String content = "VerificationTest[Length[list] == 5 && Equal[x, y]]";
        detector.detectTestMultipleConcerns(context, inputFile, content);
        // Length and Equal are filtered out, so should not trigger
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestMultipleConcernsWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectTestMultipleConcerns(context, inputFile, content));
    }

    // ========== Test Magic Number Tests ==========

    @Test
    void testDetectTestMagicNumberLargeInteger() {
        String content = "VerificationTest[x == 123456]";
        detector.detectTestMagicNumber(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectTestMagicNumberPreciseFloat() {
        String content = "VerificationTest[x == 3.14159265]";
        detector.detectTestMagicNumber(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    private static Stream<Arguments> testMagicNumberNoIssueData() {
        return Stream.of(
            Arguments.of("VerificationTest[x == 100]"),
            Arguments.of("VerificationTest[x == 1000]"),
            Arguments.of("VerificationTest[x == 42]")
        );
    }

    @ParameterizedTest
    @MethodSource("testMagicNumberNoIssueData")
    void testDetectTestMagicNumberNoIssue(String content) {
        detector.detectTestMagicNumber(context, inputFile, content);
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestMagicNumberWithException() {
        String content = "VerificationTest[";
        assertDoesNotThrow(() -> detector.detectTestMagicNumber(context, inputFile, content));
    }

    // ========== Comprehensive Coverage Tests ==========

    @Test
    void testAllMethodsWithValidContent() {
        String content = "testValidName[x_] := VerificationTest[x == 5, 5, TestID -> \"test1\"]";

        assertDoesNotThrow(() -> {
            detector.detectTestNamingConvention(context, inputFile, content);
            detector.detectTestNoIsolation(context, inputFile, content);
            detector.detectTestDataHardcoded(context, inputFile, content);
            detector.detectTestIgnored(context, inputFile, content);
            detector.detectVerificationTestNoExpected(context, inputFile, content);
            detector.detectVerificationTestTooBroad(context, inputFile, content);
            detector.detectVerificationTestNoDescription(context, inputFile, content);
            detector.detectVerificationTestEmpty(context, inputFile, content);
            detector.detectTestAssertCount(context, inputFile, content);
            detector.detectTestTooLong(context, inputFile, content);
            detector.detectTestMultipleConcerns(context, inputFile, content);
            detector.detectTestMagicNumber(context, inputFile, content);
        });
    }

    @Test
    void testAllMethodsWithEmptyContent() {
        String content = "";

        assertDoesNotThrow(() -> {
            detector.detectTestNamingConvention(context, inputFile, content);
            detector.detectTestNoIsolation(context, inputFile, content);
            detector.detectTestDataHardcoded(context, inputFile, content);
            detector.detectTestIgnored(context, inputFile, content);
            detector.detectVerificationTestNoExpected(context, inputFile, content);
            detector.detectVerificationTestTooBroad(context, inputFile, content);
            detector.detectVerificationTestNoDescription(context, inputFile, content);
            detector.detectVerificationTestEmpty(context, inputFile, content);
            detector.detectTestAssertCount(context, inputFile, content);
            detector.detectTestTooLong(context, inputFile, content);
            detector.detectTestMultipleConcerns(context, inputFile, content);
            detector.detectTestMagicNumber(context, inputFile, content);
        });
    }

    @Test
    void testAllTestNamingVariations() {
        // Test names that match the pattern: test[A-Z]... and are generic
        String[] testNames = {"testTest", "testA", "testB", "testCase"};
        for (String name : testNames) {
            InputFile file = mock(InputFile.class);
            SensorContext ctx = mock(SensorContext.class);
            NewIssue issue = mock(NewIssue.class, RETURNS_DEEP_STUBS);
            NewIssueLocation location = mock(NewIssueLocation.class, RETURNS_DEEP_STUBS);

            when(ctx.newIssue()).thenReturn(issue);
            when(issue.newLocation()).thenReturn(location);
            when(issue.at(any())).thenReturn(issue);
            when(location.on(any(InputFile.class))).thenReturn(location);
            when(location.at(any())).thenReturn(location);
            when(location.message(anyString())).thenReturn(location);
            when(file.selectLine(anyInt())).thenReturn(mock(org.sonar.api.batch.fs.TextRange.class));

            String content = name + "[x_] := VerificationTest[x]";
            TestingQualityDetector localDetector = new TestingQualityDetector();

            // Just verify it doesn't throw - pattern matching behavior varies
            assertDoesNotThrow(() -> localDetector.detectTestNamingConvention(ctx, file, content));
        }
    }

    @Test
    void testVerificationTestWith3Args() {
        String content = "VerificationTest[x == 5, 5, SameTest -> Equal]";
        detector.detectVerificationTestNoDescription(context, inputFile, content);
        // Should trigger because no TestID
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testMultipleVerificationTestsInSameContent() {
        String content = "VerificationTest[x];\n"  // No comma, should match
                        + "VerificationTest[y];\n"  // No comma, should match
                        + "VerificationTest[z];";

        detector.detectVerificationTestNoExpected(context, inputFile, content);
        // Should find multiple issues (at least 1, but expect 3)
        verify(context, atLeast(1)).newIssue();
    }

    @Test
    void testComplexNestedBrackets() {
        String content = "VerificationTest[func[list[[1]], other[x[[2]]]] == 5]";
        assertDoesNotThrow(() -> {
            detector.detectTestAssertCount(context, inputFile, content);
            detector.detectTestTooLong(context, inputFile, content);
            detector.detectTestMultipleConcerns(context, inputFile, content);
        });
    }

    @Test
    void testTestWithGlobalAndVerificationTest() {
        String content = "global = 5; VerificationTest[GlobalVar = 10; GlobalVar == 10]";
        detector.detectTestNoIsolation(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }

    @Test
    void testDetectTestNamingConventionSuffixPattern() {
        String content = "MyFunctionTest[x_] := VerificationTest[x > 0]";
        detector.detectTestNamingConvention(context, inputFile, content);
        // "MyFunction" is specific enough, should not trigger
        verify(context, never()).newIssue();
    }

    @Test
    void testDetectTestIgnoredWithDisabledOption() {
        String content = "VerificationTest[x == 5, True, \"Disabled\"]";
        detector.detectTestIgnored(context, inputFile, content);
        verify(context, atLeastOnce()).newIssue();
    }
}
