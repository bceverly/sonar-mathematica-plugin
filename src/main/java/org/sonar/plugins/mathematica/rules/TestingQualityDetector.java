package org.sonar.plugins.mathematica.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * Detector for Testing Quality rules (12 rules total).
 * Handles test organization, VerificationTest quality, and test best practices.
 */
public class TestingQualityDetector extends BaseDetector {

    // ===== TIER 1 GAP CLOSURE - TESTING QUALITY DETECTION (12 rules) =====

    // Test Organization patterns
    private static final Pattern TEST_FUNCTION_PATTERN = Pattern.compile(
        "test([A-Z][a-zA-Z0-9]*)\\s*+\\[|"
        + "([A-Z][a-zA-Z0-9]*)Test\\s*+\\["
    );
    private static final Pattern VERIFICATION_TEST_PATTERN = Pattern.compile("VerificationTest\\s*+\\["); //NOSONAR
    private static final Pattern SHARED_TEST_DATA_PATTERN = Pattern.compile(
        "(?:Module|Block|With)\\s*+\\[[^\\]]*testData|testInput|expected"
    );
    private static final Pattern DISABLED_TEST_PATTERN = Pattern.compile(
        "\\(\\*\\s*+VerificationTest|"  + "VerificationTest\\s*+\\[[^\\]]*,\\s*+\"(?:Ignore|Skip|Disabled)\""
    );

    // VerificationTest patterns
    private static final Pattern VT_WITHOUT_EXPECTED_PATTERN = Pattern.compile(
        "VerificationTest\\s*+\\[([^,]+)\\]"  // Only one argument
    );
    private static final Pattern VT_TRUE_PATTERN = Pattern.compile(
        "VerificationTest\\s*+\\[[^,]+,\\s*+True\\s*+[,\\]]"
    );
    private static final Pattern VT_WITHOUT_DESC_PATTERN = Pattern.compile(
        "VerificationTest\\s*+\\[[^,]+,[^,]+(?:,[^,]+)?\\]"  // 2-3 args without TestID
    );
    private static final Pattern VT_EMPTY_PATTERN = Pattern.compile(
        "VerificationTest\\s*+\\[\\s*+,|VerificationTest\\s*+\\[\\s*+\\]"
    );

    // Test Quality patterns
    private static final Pattern VT_ASSERT_PATTERN = Pattern.compile("==|===|SameQ|MatchQ"); //NOSONAR - Possessive quantifiers prevent backtracking
    private static final Pattern TEST_MAGIC_NUMBER_PATTERN = Pattern.compile(
        "VerificationTest\\s*+\\[[^\\]]*\\b(\\d{3,}|\\d+\\.\\d{4,})\\b"
    );

    /**
     * Detect test functions with poor naming conventions.
     */
    public void detectTestNamingConvention(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = TEST_FUNCTION_PATTERN.matcher(content);
            while (matcher.find()) {
                String funcName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (funcName != null) {
                    // Check if name is too generic or unclear
                    if (funcName.matches("(?i)test[0-9]+|test|testA|testB|testCase")) {
                        int lineNumber = calculateLineNumber(content, matcher.start());
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_NAMING_CONVENTION_KEY,
                            String.format("Test name '%s' is too generic. Use descriptive names like 'testValidateInputRange'.", funcName));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping test naming detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect tests that lack isolation (shared state).
     */
    public void detectTestNoIsolation(SensorContext context, InputFile inputFile, String content) {
        try {
            // Look for tests that use global variables without cleanup
            if (content.contains("VerificationTest")
                && (content.contains("global") || content.matches(".*[A-Z][a-zA-Z0-9]*\\s*+=(?!=).*"))) { //NOSONAR

                Matcher vtMatcher = VERIFICATION_TEST_PATTERN.matcher(content);
                while (vtMatcher.find()) {
                    int testStart = vtMatcher.start();
                    int testEnd = findMatchingBracket(content, vtMatcher.end() - 1);
                    if (testEnd > testStart) {
                        String testBody = content.substring(testStart, testEnd);

                        // Check for global assignments without Clear
                        if (testBody.matches(".*[A-Z][a-zA-Z0-9]*\\s*+=.*") && !testBody.contains("Clear[")) { //NOSONAR
                            int lineNumber = calculateLineNumber(content, testStart);
                            reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_NO_ISOLATION_KEY,
                                "Test modifies global state without cleanup. Use Module/Block for isolation.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping test isolation detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect hardcoded test data that should be parameterized.
     */
    public void detectTestDataHardcoded(SensorContext context, InputFile inputFile, String content) {
        try {
            // Look for repeated literal values across multiple tests
            if (!content.contains("testData") && !content.contains("testInput")) {
                Matcher vtMatcher = VERIFICATION_TEST_PATTERN.matcher(content);
                int vtCount = 0;
                while (vtMatcher.find()) {
                    vtCount++;
                }

                // If many tests but no shared test data, suggest refactoring
                if (vtCount >= 5) {
                    reportIssue(context, inputFile, 1, MathematicaRulesDefinition.TEST_DATA_HARDCODED_KEY,
                        "Multiple tests with hardcoded data. Extract to shared test fixtures.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping hardcoded test data detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect disabled/ignored tests that should be removed or fixed.
     */
    public void detectTestIgnored(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = DISABLED_TEST_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_IGNORED_KEY,
                    "Disabled test found. Either fix and enable it, or remove it entirely.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping ignored test detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect VerificationTest without expected result.
     */
    public void detectVerificationTestNoExpected(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = VT_WITHOUT_EXPECTED_PATTERN.matcher(content);
            while (matcher.find()) {
                // Ensure it's not a pattern with optional args
                String match = matcher.group();
                if (!match.contains(",")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.VERIFICATION_TEST_NO_EXPECTED_KEY,
                        "VerificationTest without expected result. Add second argument for comparison.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping VerificationTest no expected detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect VerificationTest with overly broad assertions.
     */
    public void detectVerificationTestTooBroad(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = VT_TRUE_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.VERIFICATION_TEST_TOO_BROAD_KEY,
                    "VerificationTest comparing to True is too broad. Test specific expected values.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping VerificationTest too broad detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect VerificationTest without description/TestID.
     */
    public void detectVerificationTestNoDescription(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = VT_WITHOUT_DESC_PATTERN.matcher(content);
            while (matcher.find()) {
                String match = matcher.group();
                // Check if TestID option is missing
                if (!match.contains("TestID")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.VERIFICATION_TEST_NO_DESCRIPTION_KEY,
                        "VerificationTest should have TestID option for clear failure reporting.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping VerificationTest no description detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect empty VerificationTest.
     */
    public void detectVerificationTestEmpty(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = VT_EMPTY_PATTERN.matcher(content);
            while (matcher.find()) {
                int lineNumber = calculateLineNumber(content, matcher.start());
                reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.VERIFICATION_TEST_EMPTY_KEY,
                    "Empty VerificationTest. Remove or implement the test.");
            }
        } catch (Exception e) {
            LOG.warn("Skipping empty VerificationTest detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect tests with too few or too many assertions.
     */
    public void detectTestAssertCount(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher vtMatcher = VERIFICATION_TEST_PATTERN.matcher(content);
            while (vtMatcher.find()) {
                int testStart = vtMatcher.start();
                int testEnd = findMatchingBracket(content, vtMatcher.end() - 1);
                if (testEnd > testStart) {
                    String testBody = content.substring(testStart, testEnd);

                    // Count assertions (==, ===, SameQ, MatchQ)
                    Matcher assertMatcher = VT_ASSERT_PATTERN.matcher(testBody);
                    int assertCount = 0;
                    while (assertMatcher.find()) {
                        assertCount++;
                    }

                    if (assertCount == 0) {
                        int lineNumber = calculateLineNumber(content, testStart);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_ASSERT_COUNT_KEY,
                            "Test has no assertions. Add comparison operators.");
                    } else if (assertCount > 5) {
                        int lineNumber = calculateLineNumber(content, testStart);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_ASSERT_COUNT_KEY,
                            String.format("Test has %d assertions (max 5 recommended). Split into multiple tests.", assertCount));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping test assert count detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect tests that are too long.
     */
    public void detectTestTooLong(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher vtMatcher = VERIFICATION_TEST_PATTERN.matcher(content);
            while (vtMatcher.find()) {
                int testStart = vtMatcher.start();
                int testEnd = findMatchingBracket(content, vtMatcher.end() - 1);
                if (testEnd > testStart) {
                    String testBody = content.substring(testStart, testEnd);
                    int lineCount = testBody.split("\n").length;

                    if (lineCount > 20) {
                        int lineNumber = calculateLineNumber(content, testStart);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_TOO_LONG_KEY,
                            String.format("Test is %d lines long (max 20). Extract setup to helper functions.", lineCount));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping test length detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect tests testing multiple unrelated concerns.
     */
    public void detectTestMultipleConcerns(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher vtMatcher = VERIFICATION_TEST_PATTERN.matcher(content);
            while (vtMatcher.find()) {
                int testStart = vtMatcher.start();
                int testEnd = findMatchingBracket(content, vtMatcher.end() - 1);
                if (testEnd > testStart) {
                    String testBody = content.substring(testStart, testEnd);

                    // Heuristic: Multiple different function calls suggests multiple concerns
                    int uniqueFunctionCalls = countUniqueFunctionCalls(testBody);
                    if (uniqueFunctionCalls > 3) {
                        int lineNumber = calculateLineNumber(content, testStart);
                        reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_MULTIPLE_CONCERNS_KEY,
                            "Test appears to test multiple concerns. One test per behavior.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping multiple concerns detection: {}", inputFile.filename());
        }
    }

    /**
     * Detect magic numbers in tests.
     */
    public void detectTestMagicNumber(SensorContext context, InputFile inputFile, String content) {
        try {
            Matcher matcher = TEST_MAGIC_NUMBER_PATTERN.matcher(content);
            while (matcher.find()) {
                String number = matcher.group(1);
                // Skip common test values
                if (!number.equals("100") && !number.equals("1000")) {
                    int lineNumber = calculateLineNumber(content, matcher.start());
                    reportIssue(context, inputFile, lineNumber, MathematicaRulesDefinition.TEST_MAGIC_NUMBER_KEY,
                        String.format("Magic number '%s' in test. Use named constant for clarity.", number));
                }
            }
        } catch (Exception e) {
            LOG.warn("Skipping test magic number detection: {}", inputFile.filename());
        }
    }

    /**
     * Helper to find matching closing bracket.
     */
    private int findMatchingBracket(String content, int openPos) {
        int depth = 1;
        int pos = openPos + 1;
        while (pos < content.length() && depth > 0) {
            char c = content.charAt(pos);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
            }
            pos++;
        }
        return depth == 0 ? pos : -1;
    }

    /**
     * Helper to count unique function calls in test body.
     */
    private int countUniqueFunctionCalls(String testBody) {
        Pattern funcPattern = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*+\\["); //NOSONAR - Possessive quantifiers prevent backtracking
        Matcher matcher = funcPattern.matcher(testBody);
        java.util.Set<String> uniqueFunctions = new java.util.HashSet<>();
        while (matcher.find()) {
            String funcName = matcher.group(1);
            // Skip common test utilities
            if (!funcName.equals("Equal") && !funcName.equals("SameQ")
                && !funcName.equals("MatchQ") && !funcName.equals("Length")) {
                uniqueFunctions.add(funcName);
            }
        }
        return uniqueFunctions.size();
    }
}
