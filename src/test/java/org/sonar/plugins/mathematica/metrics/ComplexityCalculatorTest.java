package org.sonar.plugins.mathematica.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.sonar.plugins.mathematica.metrics.ComplexityCalculator.FunctionComplexity;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class ComplexityCalculatorTest {

    private ComplexityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ComplexityCalculator();
    }

    static Stream<CyclomaticComplexityTestData> provideCyclomaticComplexityData() {
        return Stream.of(
            new CyclomaticComplexityTestData("If[x > 0, y, z]; While[x > 0, x--]; For[i = 1, i < 10, i++, Print[i]]", 4),
            new CyclomaticComplexityTestData("If[x > 0 && y < 10, Print[x]]", 3),
            new CyclomaticComplexityTestData("(* This is a comment with If[x, y, z] *) x = 5;", 1),
            new CyclomaticComplexityTestData("str = \"If[x > 0, y, z]\"; Print[str]", 1),
            new CyclomaticComplexityTestData("(* outer (* inner If[x, y] *) outer *) x = 5;", 1),
            new CyclomaticComplexityTestData("Which[x == 1, a, x == 2, b]; Switch[y, 1, a, 2, b]", 3),
            new CyclomaticComplexityTestData("Table[i^2, {i, 10}]; Map[f, list]", 3),
            new CyclomaticComplexityTestData("If[x > 0 || y < 10, Print[x]]", 3),
            new CyclomaticComplexityTestData("Do[Print[i], {i, 10}]", 2),
            new CyclomaticComplexityTestData("Scan[Print, list]", 2),
            new CyclomaticComplexityTestData("", 1),
            new CyclomaticComplexityTestData("If[x > 0 && y < 10 || z == 5, Print[x]]", 4)
        );
    }

    static class CyclomaticComplexityTestData {
        final String code;
        final int expectedComplexity;

        CyclomaticComplexityTestData(String code, int expectedComplexity) {
            this.code = code;
            this.expectedComplexity = expectedComplexity;
        }
    }

    @Test
    void testCyclomaticComplexityBasic() {
        String simpleCode = "x = 5;";
        int complexity = calculator.calculateCyclomaticComplexity(simpleCode);

        assertThat(complexity).isEqualTo(1);  // Base complexity
    }

    @Test
    void testCyclomaticComplexityWithIfStatement() {
        String code = "If[x > 0, Print[x], Print[-x]]";
        int complexity = calculator.calculateCyclomaticComplexity(code);

        assertThat(complexity).isPositive();  // Base + 1 for If
    }

    @ParameterizedTest
    @MethodSource("provideCyclomaticComplexityData")
    void testCyclomaticComplexityVariousCases(CyclomaticComplexityTestData testData) {
        int complexity = calculator.calculateCyclomaticComplexity(testData.code);
        assertThat(complexity).isEqualTo(testData.expectedComplexity);
    }

    @Test
    void testCognitiveComplexityBasic() {
        String simpleCode = "x = 5;";
        int complexity = calculator.calculateCognitiveComplexity(simpleCode);

        assertThat(complexity).isZero();  // No control structures
    }

    @Test
    void testCognitiveComplexityWithIfStatement() {
        String code = "If[x > 0, Print[x], Print[-x]]";
        int complexity = calculator.calculateCognitiveComplexity(code);

        assertThat(complexity).isPositive();  // If adds complexity
    }

    @Test
    void testCognitiveComplexityWithLogicalOperators() {
        String code = "If[x > 0 && y < 10, Print[x]]";
        int complexity = calculator.calculateCognitiveComplexity(code);

        assertThat(complexity).isGreaterThan(1);  // If + && add complexity
    }

    @Test
    void testFunctionComplexityBasic() {
        String functionBody = "x + y";
        FunctionComplexity fc = calculator.calculateFunctionComplexity("add", functionBody);

        assertThat(fc.getFunctionName()).isEqualTo("add");
        assertThat(fc.getCyclomaticComplexity()).isEqualTo(1);
        assertThat(fc.getCognitiveComplexity()).isZero();
    }

    @Test
    void testFunctionComplexityWithDecisionPoints() {
        String functionBody = "If[x > 0, x, -x]";
        FunctionComplexity fc = calculator.calculateFunctionComplexity("abs", functionBody);

        assertThat(fc.getFunctionName()).isEqualTo("abs");
        assertThat(fc.getCyclomaticComplexity()).isPositive();
        assertThat(fc.getCognitiveComplexity()).isPositive();
    }

    @Test
    void testFunctionComplexityWithRecursion() {
        String functionBody = "If[n == 0, 1, n * factorial[n - 1]]";
        FunctionComplexity fc = calculator.calculateFunctionComplexity("factorial", functionBody);

        assertThat(fc.getFunctionName()).isEqualTo("factorial");
        assertThat(fc.getCognitiveComplexity()).isPositive();  // Recursion adds cognitive load
    }

    @Test
    void testCalculateAllFunctionComplexities() {
        String code = "add[x_, y_] := x + y;\n"
                      + "multiply[x_, y_] := x * y;\n"
                      + "factorial[n_] := If[n == 0, 1, n * factorial[n - 1]]";

        List<FunctionComplexity> complexities = calculator.calculateAllFunctionComplexities(code);

        assertThat(complexities).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testCalculateAllFunctionComplexitiesEmptyCode() {
        String code = "x = 5; y = 10;";  // No function definitions

        List<FunctionComplexity> complexities = calculator.calculateAllFunctionComplexities(code);

        assertThat(complexities).isEmpty();
    }

    @Test
    void testClearCache() {
        String code = "If[x > 0, y, z]";

        // Calculate once to populate cache
        int complexity1 = calculator.calculateCyclomaticComplexity(code);

        // Clear cache
        calculator.clearCache();

        // Calculate again (should work fine)
        int complexity2 = calculator.calculateCyclomaticComplexity(code);

        assertThat(complexity1).isEqualTo(complexity2);
    }

    @Test
    void testCacheReuse() {
        String code = "If[x > 0, y, z]; While[x > 0, x--]";

        // Calculate both metrics on same content (should reuse cache)
        int cyclomatic = calculator.calculateCyclomaticComplexity(code);
        int cognitive = calculator.calculateCognitiveComplexity(code);

        assertThat(cyclomatic).isPositive();
        assertThat(cognitive).isPositive();
    }

    @Test
    void testFunctionComplexityToString() {
        FunctionComplexity fc = new FunctionComplexity("testFunc", 5, 3);

        String str = fc.toString();

        assertThat(str).contains("testFunc").contains("5").contains("3");
    }

    @Test
    void testCognitiveComplexityWithEmptyString() {
        String code = "";
        int complexity = calculator.calculateCognitiveComplexity(code);

        assertThat(complexity).isZero();
    }

    @Test
    void testFunctionComplexityGetters() {
        FunctionComplexity fc = new FunctionComplexity("myFunc", 10, 7);

        assertThat(fc.getFunctionName()).isEqualTo("myFunc");
        assertThat(fc.getCyclomaticComplexity()).isEqualTo(10);
        assertThat(fc.getCognitiveComplexity()).isEqualTo(7);
    }
}
