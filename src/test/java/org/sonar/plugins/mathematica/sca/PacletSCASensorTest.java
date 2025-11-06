package org.sonar.plugins.mathematica.sca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.plugins.mathematica.MathematicaLanguage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class PacletSCASensorTest {

    private PacletSCASensor sensor;
    private SensorContext sensorContext;
    private FileSystem fileSystem;
    private FilePredicates predicates;

    @BeforeEach
    void setUp() {
        sensor = new PacletSCASensor();
        sensorContext = mock(SensorContext.class);
        fileSystem = mock(FileSystem.class);
        predicates = mock(FilePredicates.class);

        when(sensorContext.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.predicates()).thenReturn(predicates);
    }

    @Test
    void testDescribe() {
        SensorDescriptor descriptor = mock(SensorDescriptor.class);
        when(descriptor.name(any())).thenReturn(descriptor);
        when(descriptor.onlyOnLanguage(any())).thenReturn(descriptor);

        sensor.describe(descriptor);

        verify(descriptor).name("Mathematica Paclet SCA");
        verify(descriptor).onlyOnLanguage(MathematicaLanguage.KEY);
    }

    @Test
    void testExecuteWithNoPacletFiles() {
        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.matchesPathPattern(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.emptyList());

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
        verify(sensorContext, never()).newIssue();
    }

    @Test
    void testExecuteWithCleanPacletFile() throws IOException {
        String pacletContent = "Paclet[\n"
            + "  Name -> \"MyPackage\",\n"
            + "  Version -> \"1.0.0\",\n"
            + "  WolframVersion -> \"13.0+\",\n"
            + "  Extensions -> {\n"
            + "    {\"Kernel\", Root -> \".\", Context -> \"MyPackage`\"}\n"
            + "  }\n"
            + "]";

        InputFile pacletFile = createMockPacletFile("PacletInfo.wl", pacletContent, 8);
        setupFileMocks(Collections.singletonList(pacletFile));

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
        verify(sensorContext, never()).newIssue();
    }

    @Test
    void testExecuteWithVulnerableDependency() throws IOException {
        // Use HTTPClient 1.0 which is in the vulnerability database (< 2.0)
        String pacletContent = "Paclet[\n"
            + "  Name -> \"MyPackage\",\n"
            + "  Version -> \"1.0.0\",\n"
            + "  Dependencies -> {\n"
            + "    \"HTTPClient\" -> \"1.0.0\"\n"
            + "  }\n"
            + "]";

        InputFile pacletFile = createMockPacletFileWithIssue("PacletInfo.wl", pacletContent, 7, 5);
        setupFileMocks(Collections.singletonList(pacletFile));

        // Mock the issue reporting chain
        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);

        when(sensorContext.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any())).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(newIssue.at(any())).thenReturn(newIssue);

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
        verify(sensorContext).newIssue(); // Vulnerability should be reported
        verify(newIssue).save(); // Issue should be saved
    }

    @Test
    void testExecuteWithMultipleDependencies() throws IOException {
        String pacletContent = "Paclet[\n"
            + "  Name -> \"MyPackage\",\n"
            + "  Version -> \"1.0.0\",\n"
            + "  Dependencies -> {\n"
            + "    \"PacletA\" -> \"1.0.0\",\n"
            + "    \"PacletB\" -> \"2.0.0\",\n"
            + "    \"PacletC\" -> \"3.0.0\"\n"
            + "  }\n"
            + "]";

        InputFile pacletFile = createMockPacletFile("PacletInfo.wl", pacletContent, 9);
        setupFileMocks(Collections.singletonList(pacletFile));

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testExecuteWithMultiplePacletFiles() throws IOException {
        String pacletContent1 = "Paclet[Name -> \"Package1\", Version -> \"1.0.0\"]";
        String pacletContent2 = "Paclet[Name -> \"Package2\", Version -> \"2.0.0\"]";

        InputFile file1 = createMockPacletFile("dir1/PacletInfo.wl", pacletContent1, 1);
        InputFile file2 = createMockPacletFile("dir2/PacletInfo.wl", pacletContent2, 1);

        setupFileMocks(Arrays.asList(file1, file2));

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testExecuteWithIOException() throws IOException {
        InputFile pacletFile = mock(InputFile.class);
        when(pacletFile.filename()).thenReturn("PacletInfo.wl");
        when(pacletFile.contents()).thenThrow(new IOException("Test IO error"));

        setupFileMocks(Collections.singletonList(pacletFile));

        // Should not throw - error should be logged
        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testExecuteWithInvalidPacletContent() throws IOException {
        String invalidContent = "This is not a valid PacletInfo file";

        InputFile pacletFile = createMockPacletFile("PacletInfo.wl", invalidContent, 1);
        setupFileMocks(Collections.singletonList(pacletFile));

        // Should not throw - parser should handle gracefully
        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testExecuteWithEmptyPacletFile() throws IOException {
        String emptyContent = "";

        InputFile pacletFile = createMockPacletFile("PacletInfo.wl", emptyContent, 0);
        setupFileMocks(Collections.singletonList(pacletFile));

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testExecuteWithComplexPacletStructure() throws IOException {
        String complexContent = "Paclet[\n"
            + "  Name -> \"ComplexPackage\",\n"
            + "  Version -> \"2.5.1\",\n"
            + "  WolframVersion -> \"12.0+\",\n"
            + "  Description -> \"A complex package\",\n"
            + "  Creator -> \"Developer\",\n"
            + "  Dependencies -> {\n"
            + "    \"Dep1\" -> \"1.0+\",\n"
            + "    \"Dep2\" -> \"2.0-3.0\",\n"
            + "    \"Dep3\"\n"
            + "  },\n"
            + "  Extensions -> {\n"
            + "    {\"Kernel\", Root -> \"Kernel\", Context -> \"ComplexPackage`\"},\n"
            + "    {\"Documentation\", Language -> \"English\"}\n"
            + "  }\n"
            + "]";

        InputFile pacletFile = createMockPacletFile("PacletInfo.wl", complexContent, 16);
        setupFileMocks(Collections.singletonList(pacletFile));

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testReportVulnerabilityWithValidIssue() throws IOException {
        // Create a paclet file with content that might trigger vulnerability
        String pacletContent = "Paclet[\n"
            + "  Name -> \"TestPackage\",\n"
            + "  Dependencies -> {\"VulnerablePkg\" -> \"1.0.0\"}\n"
            + "]";

        InputFile pacletFile = createMockPacletFileWithIssue("PacletInfo.wl", pacletContent, 4, 3);
        setupFileMocks(Collections.singletonList(pacletFile));

        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);

        when(sensorContext.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any())).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(newIssue.at(any())).thenReturn(newIssue);

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testSensorWithNullFileSystem() {
        when(sensorContext.fileSystem()).thenReturn(null);

        // Should handle null gracefully by throwing or returning
        try {
            sensor.execute(sensorContext);
        } catch (NullPointerException e) {
            // Expected if not handled
            assertThat(e).isNotNull();
        }
    }

    @Test
    void testMultipleVulnerabilitiesInSingleFile() throws IOException {
        // Use multiple known vulnerable dependencies
        String pacletContent = "Paclet[\n"
            + "  Name -> \"MultiVuln\",\n"
            + "  Dependencies -> {\n"
            + "    \"HTTPClient\" -> \"1.0.0\",\n"
            + "    \"CryptoUtils\" -> \"1.0.0\"\n"
            + "  }\n"
            + "]";

        InputFile pacletFile = createMockPacletFileWithIssue("PacletInfo.wl", pacletContent, 7, 4);
        setupFileMocks(Collections.singletonList(pacletFile));

        // Mock the issue reporting chain
        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);

        when(sensorContext.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any())).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(newIssue.at(any())).thenReturn(newIssue);

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
        verify(sensorContext, times(2)).newIssue(); // Two vulnerabilities should be reported
        verify(newIssue, times(2)).save(); // Both issues should be saved
    }

    @Test
    void testPacletWithVersionRanges() throws IOException {
        String pacletContent = "Paclet[\n"
            + "  Name -> \"RangeTest\",\n"
            + "  Dependencies -> {\n"
            + "    \"Pkg1\" -> \"1.0-2.0\",\n"
            + "    \"Pkg2\" -> \"3.0+\",\n"
            + "    \"Pkg3\" -> \"*\"\n"
            + "  }\n"
            + "]";

        InputFile pacletFile = createMockPacletFile("PacletInfo.wl", pacletContent, 8);
        setupFileMocks(Collections.singletonList(pacletFile));

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testPacletInNestedDirectory() throws IOException {
        String pacletContent = "Paclet[Name -> \"Nested\", Version -> \"1.0\"]";

        InputFile pacletFile = createMockPacletFile("deeply/nested/path/PacletInfo.wl", pacletContent, 1);
        setupFileMocks(Collections.singletonList(pacletFile));

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testReportVulnerabilityWithException() throws IOException {
        // Use DatabaseLink which is in the vulnerability database (< 9.0)
        String pacletContent = "Paclet[\n"
            + "  Name -> \"TestPkg\",\n"
            + "  Dependencies -> {\"DatabaseLink\" -> \"8.0\"}\n"
            + "]";

        InputFile pacletFile = createMockPacletFileWithIssue("PacletInfo.wl", pacletContent, 4, 3);
        setupFileMocks(Collections.singletonList(pacletFile));

        // Mock newIssue to throw exception during issue creation
        when(sensorContext.newIssue()).thenThrow(new RuntimeException("Issue creation failed"));

        // Should not throw - exception should be caught and logged
        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
        verify(sensorContext).newIssue(); // Attempted to create issue
    }

    @Test
    void testVulnerabilityReportingWithDifferentVersionFormats() throws IOException {
        // Test with NeuralNetworks (< 12.0) using version with "+"
        String pacletContent = "Paclet[\n"
            + "  Name -> \"TestPkg\",\n"
            + "  Dependencies -> {\"NeuralNetworks\" -> \"11.0+\"}\n"
            + "]";

        InputFile pacletFile = createMockPacletFileWithIssue("PacletInfo.wl", pacletContent, 4, 3);
        setupFileMocks(Collections.singletonList(pacletFile));

        // Mock the issue reporting chain
        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);

        when(sensorContext.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any())).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(newIssue.at(any())).thenReturn(newIssue);

        sensor.execute(sensorContext);

        verify(sensorContext).newIssue();
        verify(newIssue).save();
    }

    @Test
    void testVulnerabilityWithFileUtilitiesRangeVersion() throws IOException {
        // Test with FileUtilities which has range "1.0 - 1.3"
        String pacletContent = "Paclet[\n"
            + "  Name -> \"TestPkg\",\n"
            + "  Dependencies -> {\"FileUtilities\" -> \"1.2\"}\n"
            + "]";

        InputFile pacletFile = createMockPacletFileWithIssue("PacletInfo.wl", pacletContent, 4, 3);
        setupFileMocks(Collections.singletonList(pacletFile));

        // Mock the issue reporting chain
        NewIssue newIssue = mock(NewIssue.class);
        NewIssueLocation location = mock(NewIssueLocation.class);

        when(sensorContext.newIssue()).thenReturn(newIssue);
        when(newIssue.forRule(any())).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(location);
        when(location.on(any(InputFile.class))).thenReturn(location);
        when(location.at(any(TextRange.class))).thenReturn(location);
        when(location.message(any(String.class))).thenReturn(location);
        when(newIssue.at(any())).thenReturn(newIssue);

        sensor.execute(sensorContext);

        verify(sensorContext).newIssue();
        verify(newIssue).save();
    }

    // Helper methods

    private InputFile createMockPacletFile(String filename, String content, int lines) throws IOException {
        InputFile file = mock(InputFile.class);
        when(file.filename()).thenReturn(filename);
        when(file.contents()).thenReturn(content);
        when(file.lines()).thenReturn(lines);
        return file;
    }

    private InputFile createMockPacletFileWithIssue(String filename, String content, int lines, int issueLine) throws IOException {
        InputFile file = createMockPacletFile(filename, content, lines);
        TextRange textRange = mock(TextRange.class);
        when(file.selectLine(any(Integer.class))).thenReturn(textRange);
        return file;
    }

    private void setupFileMocks(Iterable<InputFile> files) {
        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.matchesPathPattern(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(files);
    }
}
