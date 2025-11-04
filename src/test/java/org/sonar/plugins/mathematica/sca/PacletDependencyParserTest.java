package org.sonar.plugins.mathematica.sca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PacletDependencyParserTest {

    private PacletDependencyParser parser;

    @BeforeEach
    void setUp() {
        parser = new PacletDependencyParser();
    }

    @Test
    void testParseModernFormatSingleDependency() {
        String content = "Paclet[Name -> \"MyPaclet\", Version -> \"1.0\", "
                        + "Dependencies -> {\"HttpClient\" -> \"1.2+\"}]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getName()).isEqualTo("HttpClient");
        assertThat(dependencies.get(0).getVersion()).isEqualTo("1.2+");
        assertThat(dependencies.get(0).getLineNumber()).isEqualTo(1);
    }

    @Test
    void testParseModernFormatMultipleDependencies() {
        String content = "Paclet[Name -> \"MyPaclet\", Version -> \"1.0\",\n"
                        + "Dependencies -> {\n"
                        + "  \"HttpClient\" -> \"1.2+\",\n"
                        + "  \"Database\" -> \"2.0\",\n"
                        + "  \"Utils\" -> \"3.1+\"\n"
                        + "}]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(3);
        assertThat(dependencies.get(0).getName()).isEqualTo("HttpClient");
        assertThat(dependencies.get(1).getName()).isEqualTo("Database");
        assertThat(dependencies.get(2).getName()).isEqualTo("Utils");
    }

    @Test
    void testParseModernFormatWithWhitespace() {
        String content = "Dependencies  ->  { \"Foo\"  ->  \"1.0\"  ,  \"Bar\"  ->  \"2.0+\"  }";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(2);
        assertThat(dependencies.get(0).getName()).isEqualTo("Foo");
        assertThat(dependencies.get(1).getName()).isEqualTo("Bar");
    }

    @Test
    void testParseModernFormatVersionWithoutPlus() {
        String content = "Dependencies -> {\"Package\" -> \"1.0\"}";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getVersion()).isEqualTo("1.0");
    }

    @Test
    void testParseLegacyFormatSingleNeeds() {
        String content = "Needs[\"MyPackage`\"]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getName()).isEqualTo("MyPackage");
        assertThat(dependencies.get(0).getVersion()).isEqualTo("unknown");
    }

    @Test
    void testParseLegacyFormatMultipleNeeds() {
        String content = "Needs[\"Package1`\"]\nNeeds[\"Package2`\"]\nNeeds[\"Package3`\"]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(3);
        assertThat(dependencies.get(0).getName()).isEqualTo("Package1");
        assertThat(dependencies.get(1).getName()).isEqualTo("Package2");
        assertThat(dependencies.get(2).getName()).isEqualTo("Package3");
    }

    @Test
    void testParseMixedFormats() {
        String content = "Dependencies -> {\"ModernPkg\" -> \"1.0+\"}\n"
                        + "Needs[\"LegacyPkg`\"]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(2);
        assertThat(dependencies.get(0).getName()).isEqualTo("ModernPkg");
        assertThat(dependencies.get(0).getVersion()).isEqualTo("1.0+");
        assertThat(dependencies.get(1).getName()).isEqualTo("LegacyPkg");
        assertThat(dependencies.get(1).getVersion()).isEqualTo("unknown");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "Paclet[Name -> \"MyPaclet\", Version -> \"1.0\"]",
        "Dependencies -> {}"
    })
    void testParseReturnsNoDependencies(String content) {
        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).isEmpty();
    }

    @Test
    void testLineNumberCalculation() {
        String content = "Line 1\n"
                        + "Line 2\n"
                        + "Dependencies -> {\"Package\" -> \"1.0\"}\n"
                        + "Line 4";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getLineNumber()).isEqualTo(3);
    }

    @Test
    void testLineNumberForMultipleEntries() {
        String content = "Line 1\nLine 2\nNeeds[\"Pkg1`\"]\nLine 4\nNeeds[\"Pkg2`\"]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(2);
        assertThat(dependencies.get(0).getLineNumber()).isEqualTo(3);
        assertThat(dependencies.get(1).getLineNumber()).isEqualTo(5);
    }

    @Test
    void testParseMultilineDependenciesSection() {
        String content = "Paclet[\n"
                        + "  Name -> \"Test\",\n"
                        + "  Dependencies -> {\n"
                        + "    \"Pkg1\" -> \"1.0\",\n"
                        + "    \"Pkg2\" -> \"2.0\"\n"
                        + "  }\n"
                        + "]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(2);
        assertThat(dependencies.get(0).getLineNumber()).isEqualTo(3);
    }

    @Test
    void testParseNestedPackageName() {
        String content = "Needs[\"Developer`Utilities`\"]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getName()).isEqualTo("Developer`Utilities");
    }

    @Test
    void testParseComplexVersionConstraints() {
        String content = "Dependencies -> {"
                        + "\"Pkg1\" -> \"1.2.3+\","
                        + "\"Pkg2\" -> \"2.0.0\","
                        + "\"Pkg3\" -> \"0.9+\"}";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(3);
        assertThat(dependencies.get(0).getVersion()).isEqualTo("1.2.3+");
        assertThat(dependencies.get(1).getVersion()).isEqualTo("2.0.0");
        assertThat(dependencies.get(2).getVersion()).isEqualTo("0.9+");
    }

    @Test
    void testParseWithComments() {
        String content = "(* Comment *) Dependencies -> {\"Pkg\" -> \"1.0\"} (* Another comment *)";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getName()).isEqualTo("Pkg");
    }

    @Test
    void testParseMultipleDependenciesSections() {
        String content = "Dependencies -> {\"Pkg1\" -> \"1.0\"}\n"
                        + "SomeOtherStuff\n"
                        + "Dependencies -> {\"Pkg2\" -> \"2.0\"}";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(2);
        assertThat(dependencies.get(0).getName()).isEqualTo("Pkg1");
        assertThat(dependencies.get(1).getName()).isEqualTo("Pkg2");
    }

    @Test
    void testParseWithSpecialCharactersInPackageName() {
        String content = "Dependencies -> {\"My-Package_v2\" -> \"1.0\"}";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getName()).isEqualTo("My-Package_v2");
    }

    @Test
    void testParseRealWorldPacletInfo() {
        String content = "Paclet[\n"
                        + "  Name -> \"WebServices\",\n"
                        + "  Version -> \"2.1.0\",\n"
                        + "  WolframVersion -> \"13.0+\",\n"
                        + "  Description -> \"Web service utilities\",\n"
                        + "  Dependencies -> {\n"
                        + "    \"HttpClient\" -> \"1.5+\",\n"
                        + "    \"JSON\" -> \"2.0\",\n"
                        + "    \"Security\" -> \"1.0+\"\n"
                        + "  },\n"
                        + "  Extensions -> {\n"
                        + "    {\"Kernel\", Root -> \"Kernel\", Context -> \"WebServices`\"}\n"
                        + "  }\n"
                        + "]";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(3);
        assertThat(dependencies.get(0).getName()).isEqualTo("HttpClient");
        assertThat(dependencies.get(0).getVersion()).isEqualTo("1.5+");
        assertThat(dependencies.get(1).getName()).isEqualTo("JSON");
        assertThat(dependencies.get(1).getVersion()).isEqualTo("2.0");
        assertThat(dependencies.get(2).getName()).isEqualTo("Security");
        assertThat(dependencies.get(2).getVersion()).isEqualTo("1.0+");
    }

    @Test
    void testParseHandlesExceptions() {
        String content = "Dependencies -> {Malformed without quotes}";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).isEmpty();
    }

    @Test
    void testToStringMethod() {
        String content = "Dependencies -> {\"TestPkg\" -> \"1.0+\"}";

        List<PacletDependency> dependencies = parser.parsePacletInfo(content);

        assertThat(dependencies).hasSize(1);
        String str = dependencies.get(0).toString();
        assertThat(str).contains("TestPkg").contains("1.0+");
    }
}
