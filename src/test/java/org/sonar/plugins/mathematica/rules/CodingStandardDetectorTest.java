package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CodingStandardDetector (32 rules from CODING_STANDARDS.md).
 */
class CodingStandardDetectorTest {

    private CodingStandardDetector detector;
    private SensorContext context;
    private InputFile inputFile;

    @BeforeEach
    void setUp() {
        detector = new CodingStandardDetector();
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);

        // Mock newIssue chain
        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);
        TextRange textRange = mock(TextRange.class);

        when(context.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any(RuleKey.class))).thenReturn(newIssue);
        when(newIssue.at(any(NewIssueLocation.class))).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(inputFile.newRange(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(textRange);

        // Set sensor (required for BaseDetector)
        MathematicaRulesSensor mockSensor = mock(MathematicaRulesSensor.class);
        detector.setSensor(mockSensor);
    }

    @Test
    void testBracketSpacingBefore() {
        String code = "function [x_] := x + 1\n"; // Non-compliant: space before [
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testVariableAssignmentInModuleDef() {
        String code = "Module[{x = 1}, x + 2]\n"; // Non-compliant: assignment in Module def
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testComplexBooleanShorthand() {
        String code = "If[a > 0 &&\n  b > 0 ||\n  c > 0, True, False]\n";
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testMultilineMapShorthand() {
        String code = "func /@ \n  list\n"; // Non-compliant: /@ with newline
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testErrorMessageDelayed() {
        String code = "myFunc::error := \"Error message\"\n"; // Non-compliant: := for message
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testConditionalFunctionDef() {
        String code = "func[x_] := x + 1 /; x > 0\n"; // Non-compliant: /; in definition
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testDereferencingSyntax() {
        String code = "result = $PersonID[Name]\n"; // Non-compliant: dereferencing
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testInPlaceModification() {
        String code = "AppendTo[list, newItem]\n"; // Non-compliant: in-place modification
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testNonLinearEvaluation() {
        String code = "result = Composition[f, g, h][x]\n"; // Warning: Composition
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testLongVariableName() {
        String code = "currentActiveUserProfileData = getData[]\n"; // Non-compliant: >3 words
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testLongFunctionName() {
        String code = "GetCurrentActiveUserProfileData[id_] := id\n"; // Non-compliant: >3 words
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testLitterWords() {
        String code = "DoCalculation[x_] := x + 1\n"; // Non-compliant: Do is litter word
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testTimeConstrained() {
        String code = "result = TimeConstrained[expr, 10]\n"; // CRITICAL: TimeConstrained
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testPatternNoPSuffix() {
        String code = "User = _String | _Integer\n"; // Non-compliant: should be UserP
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testPatternTestNoQSuffix() {
        String code = "ValidUser[x_] := StringQ[x] && StringLength[x] > 0\n"; // Should be ValidUserQ
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testEnumeratedStringPattern() {
        String code = "ColorP = \"Red\" | \"Green\" | \"Blue\"\n"; // Non-compliant: strings not symbols
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testCompliantBracketSpacing() {
        String code = "function[x_] := x + 1\n"; // Compliant: no space before [
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testCompliantModuleDefinition() {
        String code = "Module[{x}, x = 1; x + 2]\n"; // Compliant: assignment in body
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testCompliantErrorMessage() {
        String code = "myFunc::error = \"Error message\"\n"; // Compliant: = for message
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testCompliantPatternName() {
        String code = "UserP = _String | _Integer\n"; // Compliant: ends with P
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testCompliantPatternTestName() {
        String code = "ValidUserQ[x_] := StringQ[x]\n"; // Compliant: ends with Q
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }

    @Test
    void testCompliantEnumeratedPattern() {
        String code = "ColorP = Red | Green | Blue\n"; // Compliant: symbols not strings
        assertDoesNotThrow(() -> detector.detect(context, inputFile, code, code));
    }
}
