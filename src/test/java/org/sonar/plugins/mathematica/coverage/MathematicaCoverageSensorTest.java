package org.sonar.plugins.mathematica.coverage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.mathematica.MathematicaLanguage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MathematicaCoverageSensorTest {

    private MathematicaCoverageSensor sensor;
    private SensorContext sensorContext;
    private FileSystem fileSystem;
    private FilePredicates predicates;
    private Configuration configuration;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        sensor = new MathematicaCoverageSensor();
        sensorContext = mock(SensorContext.class);
        fileSystem = mock(FileSystem.class);
        predicates = mock(FilePredicates.class);
        configuration = mock(Configuration.class);

        when(sensorContext.fileSystem()).thenReturn(fileSystem);
        when(sensorContext.config()).thenReturn(configuration);
        when(fileSystem.predicates()).thenReturn(predicates);
        when(fileSystem.baseDir()).thenReturn(tempDir.toFile());
    }

    @Test
    void testDescribe() {
        SensorDescriptor descriptor = mock(SensorDescriptor.class);
        when(descriptor.name(anyString())).thenReturn(descriptor);
        when(descriptor.onlyOnLanguage(anyString())).thenReturn(descriptor);

        sensor.describe(descriptor);

        verify(descriptor).name("Mathematica Coverage Sensor");
        verify(descriptor).onlyOnLanguage(MathematicaLanguage.KEY);
    }

    @Test
    void testExecuteWithNoCoverageFile() {
        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));

        File nonExistentFile = new File(tempDir.toFile(), "coverage/coverage.json");
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(nonExistentFile);

        sensor.execute(sensorContext);

        // Verify no coverage was created since file doesn't exist
        verify(sensorContext, never()).newCoverage();
    }

    @Test
    void testExecuteWithDefaultPath() {
        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.empty());

        File nonExistentFile = new File(tempDir.toFile(), MathematicaCoverageSensor.DEFAULT_REPORT_PATH);
        when(fileSystem.resolvePath(MathematicaCoverageSensor.DEFAULT_REPORT_PATH)).thenReturn(nonExistentFile);

        sensor.execute(sensorContext);

        // Should use default path
        verify(fileSystem).resolvePath(MathematicaCoverageSensor.DEFAULT_REPORT_PATH);
        verify(sensorContext, never()).newCoverage();
    }

    @Test
    void testExecuteWithValidCoverageFile() throws IOException {
        // Create coverage file
        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        String coverageJson = String.format(
            "[\n"
            + "  {\n"
            + "    \"FileName\": \"TestFile.wl\",\n"
            + "    \"FullPath\": \"%s/TestFile.wl\",\n"
            + "    \"TotalLines\": 10,\n"
            + "    \"CodeLines\": 8,\n"
            + "    \"CoveredLines\": 6,\n"
            + "    \"Coverage\": 0.75,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 2},\n"
            + "      {\"Line\": 2, \"IsCode\": true, \"Hits\": 1},\n"
            + "      {\"Line\": 3, \"IsCode\": false, \"Hits\": 0},\n"
            + "      {\"Line\": 4, \"IsCode\": true, \"Hits\": 0}\n"
            + "    ]\n"
            + "  }\n"
            + "]\n",
            tempDir.toString());

        Files.writeString(coverageFile, coverageJson, StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        // Mock input file and coverage
        InputFile inputFile = mock(InputFile.class);
        NewCoverage coverage = mock(NewCoverage.class);

        FilePredicate absolutePredicate = mock(FilePredicate.class);
        when(predicates.hasAbsolutePath(anyString())).thenReturn(absolutePredicate);
        when(fileSystem.inputFile(absolutePredicate)).thenReturn(inputFile);

        when(sensorContext.newCoverage()).thenReturn(coverage);
        when(coverage.onFile(any(InputFile.class))).thenReturn(coverage);
        when(coverage.lineHits(any(Integer.class), any(Integer.class))).thenReturn(coverage);

        sensor.execute(sensorContext);

        // Verify coverage was created and saved
        verify(sensorContext).newCoverage();
        verify(coverage).onFile(inputFile);
        verify(coverage).lineHits(1, 2);  // Line 1 with 2 hits
        verify(coverage).lineHits(2, 1);  // Line 2 with 1 hit
        verify(coverage).lineHits(4, 0);  // Line 4 with 0 hits
        verify(coverage, never()).lineHits(3, 0);  // Line 3 has IsCode=false, should be skipped
        verify(coverage).save();
    }

    @Test
    void testExecuteWithMultipleCoverageFiles() throws IOException {
        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        String coverageJson = String.format(
            "[\n"
            + "  {\n"
            + "    \"FileName\": \"File1.wl\",\n"
            + "    \"FullPath\": \"%s/File1.wl\",\n"
            + "    \"TotalLines\": 5,\n"
            + "    \"CodeLines\": 5,\n"
            + "    \"CoveredLines\": 5,\n"
            + "    \"Coverage\": 1.0,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 1}\n"
            + "    ]\n"
            + "  },\n"
            + "  {\n"
            + "    \"FileName\": \"File2.wl\",\n"
            + "    \"FullPath\": \"%s/File2.wl\",\n"
            + "    \"TotalLines\": 5,\n"
            + "    \"CodeLines\": 5,\n"
            + "    \"CoveredLines\": 3,\n"
            + "    \"Coverage\": 0.6,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 2}\n"
            + "    ]\n"
            + "  }\n"
            + "]\n",
            tempDir.toString(), tempDir.toString());

        Files.writeString(coverageFile, coverageJson, StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        // Mock input files and coverage
        InputFile inputFile1 = mock(InputFile.class);
        InputFile inputFile2 = mock(InputFile.class);
        NewCoverage coverage1 = mock(NewCoverage.class);
        NewCoverage coverage2 = mock(NewCoverage.class);

        FilePredicate absolutePredicate = mock(FilePredicate.class);
        when(predicates.hasAbsolutePath(anyString())).thenReturn(absolutePredicate);
        when(fileSystem.inputFile(absolutePredicate))
            .thenReturn(inputFile1)
            .thenReturn(inputFile2);

        when(sensorContext.newCoverage())
            .thenReturn(coverage1)
            .thenReturn(coverage2);
        when(coverage1.onFile(any(InputFile.class))).thenReturn(coverage1);
        when(coverage1.lineHits(any(Integer.class), any(Integer.class))).thenReturn(coverage1);
        when(coverage2.onFile(any(InputFile.class))).thenReturn(coverage2);
        when(coverage2.lineHits(any(Integer.class), any(Integer.class))).thenReturn(coverage2);

        sensor.execute(sensorContext);

        // Verify both files were processed
        verify(sensorContext, times(2)).newCoverage();
        verify(coverage1).save();
        verify(coverage2).save();
    }

    @Test
    void testExecuteWithFileNotFoundInProject() throws IOException {
        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        String coverageJson =
            "[\n"
            + "  {\n"
            + "    \"FileName\": \"NotInProject.wl\",\n"
            + "    \"FullPath\": \"/some/other/path/NotInProject.wl\",\n"
            + "    \"TotalLines\": 5,\n"
            + "    \"CodeLines\": 5,\n"
            + "    \"CoveredLines\": 3,\n"
            + "    \"Coverage\": 0.6,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 1}\n"
            + "    ]\n"
            + "  }\n"
            + "]\n";

        Files.writeString(coverageFile, coverageJson, StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        // Mock predicates to return null (file not found)
        FilePredicate absolutePredicate = mock(FilePredicate.class);
        FilePredicate relativePredicate = mock(FilePredicate.class);
        FilePredicate patternPredicate = mock(FilePredicate.class);

        when(predicates.hasAbsolutePath(anyString())).thenReturn(absolutePredicate);
        when(predicates.hasRelativePath(anyString())).thenReturn(relativePredicate);
        when(predicates.matchesPathPattern(anyString())).thenReturn(patternPredicate);

        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(null);
        when(fileSystem.inputFiles(any(FilePredicate.class))).thenReturn(Collections.emptyList());

        sensor.execute(sensorContext);

        // Verify no coverage was created since file was not found
        verify(sensorContext, never()).newCoverage();
    }

    @Test
    void testExecuteWithMalformedJson() throws IOException {
        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        // Invalid JSON
        Files.writeString(coverageFile, "{ this is not valid json }", StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        // Should not throw exception, should handle gracefully
        sensor.execute(sensorContext);

        // Verify no coverage was created
        verify(sensorContext, never()).newCoverage();
    }

    @Test
    void testFindInputFileByAbsolutePath() throws IOException {
        Path sourceFile = tempDir.resolve("TestFile.wl");
        Files.createFile(sourceFile);

        InputFile inputFile = mock(InputFile.class);
        FilePredicate absolutePredicate = mock(FilePredicate.class);

        when(predicates.hasAbsolutePath(sourceFile.toString())).thenReturn(absolutePredicate);
        when(fileSystem.inputFile(absolutePredicate)).thenReturn(inputFile);

        // Use reflection to test private method indirectly through execute
        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        String coverageJson = String.format(
            "[\n"
            + "  {\n"
            + "    \"FileName\": \"TestFile.wl\",\n"
            + "    \"FullPath\": \"%s\",\n"
            + "    \"TotalLines\": 5,\n"
            + "    \"CodeLines\": 5,\n"
            + "    \"CoveredLines\": 5,\n"
            + "    \"Coverage\": 1.0,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 1}\n"
            + "    ]\n"
            + "  }\n"
            + "]\n",
            sourceFile.toString());

        Files.writeString(coverageFile, coverageJson, StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        NewCoverage coverage = mock(NewCoverage.class);
        when(sensorContext.newCoverage()).thenReturn(coverage);
        when(coverage.onFile(any(InputFile.class))).thenReturn(coverage);
        when(coverage.lineHits(any(Integer.class), any(Integer.class))).thenReturn(coverage);

        sensor.execute(sensorContext);

        // Verify coverage was created (meaning file was found)
        verify(sensorContext).newCoverage();
        verify(coverage).onFile(inputFile);
    }

    @Test
    void testFindInputFileByRelativePath() throws IOException {
        Path sourceFile = tempDir.resolve("src/TestFile.wl");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        InputFile inputFile = mock(InputFile.class);
        FilePredicate relativePredicate = mock(FilePredicate.class);

        // Absolute path lookup fails
        FilePredicate absolutePredicate = mock(FilePredicate.class);
        when(predicates.hasAbsolutePath(anyString())).thenReturn(absolutePredicate);
        when(fileSystem.inputFile(absolutePredicate)).thenReturn(null);

        // Relative path lookup succeeds
        when(predicates.hasRelativePath("src/TestFile.wl")).thenReturn(relativePredicate);
        when(fileSystem.inputFile(relativePredicate)).thenReturn(inputFile);

        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        String coverageJson = String.format(
            "[\n"
            + "  {\n"
            + "    \"FileName\": \"TestFile.wl\",\n"
            + "    \"FullPath\": \"%s\",\n"
            + "    \"TotalLines\": 5,\n"
            + "    \"CodeLines\": 5,\n"
            + "    \"CoveredLines\": 5,\n"
            + "    \"Coverage\": 1.0,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 1}\n"
            + "    ]\n"
            + "  }\n"
            + "]\n",
            sourceFile.toString());

        Files.writeString(coverageFile, coverageJson, StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        NewCoverage coverage = mock(NewCoverage.class);
        when(sensorContext.newCoverage()).thenReturn(coverage);
        when(coverage.onFile(any(InputFile.class))).thenReturn(coverage);
        when(coverage.lineHits(any(Integer.class), any(Integer.class))).thenReturn(coverage);

        sensor.execute(sensorContext);

        // Verify coverage was created (meaning file was found)
        verify(sensorContext).newCoverage();
        verify(coverage).onFile(inputFile);
    }

    @Test
    void testFindInputFileByFilenamePattern() throws IOException {
        Path sourceFile = tempDir.resolve("TestFile.wl");
        Files.createFile(sourceFile);

        InputFile inputFile = mock(InputFile.class);
        FilePredicate patternPredicate = mock(FilePredicate.class);

        // Absolute and relative path lookups fail
        FilePredicate absolutePredicate = mock(FilePredicate.class);
        FilePredicate relativePredicate = mock(FilePredicate.class);
        when(predicates.hasAbsolutePath(anyString())).thenReturn(absolutePredicate);
        when(predicates.hasRelativePath(anyString())).thenReturn(relativePredicate);
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(null);

        // Filename pattern lookup succeeds with exactly one match
        when(predicates.matchesPathPattern("**/TestFile.*")).thenReturn(patternPredicate);
        when(fileSystem.inputFiles(patternPredicate)).thenReturn(Collections.singletonList(inputFile));

        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        String coverageJson = String.format(
            "[\n"
            + "  {\n"
            + "    \"FileName\": \"TestFile\",\n"
            + "    \"FullPath\": \"%s\",\n"
            + "    \"TotalLines\": 5,\n"
            + "    \"CodeLines\": 5,\n"
            + "    \"CoveredLines\": 5,\n"
            + "    \"Coverage\": 1.0,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 1}\n"
            + "    ]\n"
            + "  }\n"
            + "]\n",
            sourceFile.toString());

        Files.writeString(coverageFile, coverageJson, StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        NewCoverage coverage = mock(NewCoverage.class);
        when(sensorContext.newCoverage()).thenReturn(coverage);
        when(coverage.onFile(any(InputFile.class))).thenReturn(coverage);
        when(coverage.lineHits(any(Integer.class), any(Integer.class))).thenReturn(coverage);

        sensor.execute(sensorContext);

        // Verify coverage was created (meaning file was found by pattern)
        verify(sensorContext).newCoverage();
        verify(coverage).onFile(inputFile);
    }

    @Test
    void testFindInputFileWithMultipleFilenameMatches() throws IOException {
        InputFile inputFile1 = mock(InputFile.class);
        InputFile inputFile2 = mock(InputFile.class);
        FilePredicate patternPredicate = mock(FilePredicate.class);

        // Absolute and relative path lookups fail
        FilePredicate absolutePredicate = mock(FilePredicate.class);
        FilePredicate relativePredicate = mock(FilePredicate.class);
        when(predicates.hasAbsolutePath(anyString())).thenReturn(absolutePredicate);
        when(predicates.hasRelativePath(anyString())).thenReturn(relativePredicate);
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(null);

        // Filename pattern lookup returns multiple matches
        when(predicates.matchesPathPattern(anyString())).thenReturn(patternPredicate);
        when(fileSystem.inputFiles(patternPredicate)).thenReturn(Arrays.asList(inputFile1, inputFile2));

        Path coverageDir = tempDir.resolve("coverage");
        Files.createDirectories(coverageDir);
        Path coverageFile = coverageDir.resolve("coverage.json");

        String coverageJson =
            "[\n"
            + "  {\n"
            + "    \"FileName\": \"TestFile\",\n"
            + "    \"FullPath\": \"/some/path/TestFile.wl\",\n"
            + "    \"TotalLines\": 5,\n"
            + "    \"CodeLines\": 5,\n"
            + "    \"CoveredLines\": 5,\n"
            + "    \"Coverage\": 1.0,\n"
            + "    \"LineCoverage\": [\n"
            + "      {\"Line\": 1, \"IsCode\": true, \"Hits\": 1}\n"
            + "    ]\n"
            + "  }\n"
            + "]\n";

        Files.writeString(coverageFile, coverageJson, StandardCharsets.UTF_8);

        when(configuration.get(MathematicaCoverageSensor.REPORT_PATH_KEY))
            .thenReturn(Optional.of("coverage/coverage.json"));
        when(fileSystem.resolvePath("coverage/coverage.json")).thenReturn(coverageFile.toFile());

        sensor.execute(sensorContext);

        // Verify no coverage was created (multiple matches are ambiguous)
        verify(sensorContext, never()).newCoverage();
    }

    @Test
    void testConstantsAreCorrect() {
        assertThat(MathematicaCoverageSensor.REPORT_PATH_KEY).isEqualTo("sonar.mathematica.coverage.reportPath");
        assertThat(MathematicaCoverageSensor.DEFAULT_REPORT_PATH).isEqualTo("coverage/coverage.json");
    }
}
