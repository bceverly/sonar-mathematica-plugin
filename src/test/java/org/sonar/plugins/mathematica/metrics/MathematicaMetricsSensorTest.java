package org.sonar.plugins.mathematica.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.plugins.mathematica.MathematicaLanguage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MathematicaMetricsSensorTest {

    private MathematicaMetricsSensor sensor;
    private SensorContext sensorContext;
    private FileSystem fileSystem;
    private FilePredicates predicates;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        sensor = new MathematicaMetricsSensor();
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

        verify(descriptor).name("Mathematica Metrics Sensor");
        verify(descriptor).onlyOnLanguage(MathematicaLanguage.KEY);
    }

    @Test
    void testExecuteWithNoFiles() {
        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.emptyList());

        sensor.execute(sensorContext);

        verify(fileSystem).inputFiles(any());
    }

    @Test
    void testExecuteWithSimpleFile() throws IOException {
        Path testFile = tempDir.resolve("test.wl");
        Files.write(testFile, "x = 5;\nPrint[x]".getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "test.wl", 2);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithComplexCode() throws IOException {
        Path testFile = tempDir.resolve("complex.wl");
        String content = "myFunc[x_] := If[x > 0, x, -x];\n"
                        + "factorial[n_] := If[n == 0, 1, n * factorial[n - 1]]";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "complex.wl", 2);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithComments() throws IOException {
        Path testFile = tempDir.resolve("comments.wl");
        String content = "(* This is a comment *)\n"
                        + "x = 5;\n"
                        + "(* Another comment with\n"
                        + "   multiple lines *)\n"
                        + "y = 10;";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "comments.wl", 5);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithNestedComments() throws IOException {
        Path testFile = tempDir.resolve("nested.wl");
        String content = "(* outer (* inner *) outer *)\nx = 1;";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "nested.wl", 2);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithLoops() throws IOException {
        Path testFile = tempDir.resolve("loops.wl");
        String content = "For[i = 1, i <= 10, i++, Print[i]];\n"
                        + "While[x > 0, x--];\n"
                        + "Do[Print[i], {i, 10}]";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "loops.wl", 3);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithConditionals() throws IOException {
        Path testFile = tempDir.resolve("conditionals.wl");
        String content = "If[x > 0, Print[\"positive\"], Print[\"negative\"]];\n"
                        + "Which[x == 1, a, x == 2, b, True, c];\n"
                        + "Switch[y, 1, a, 2, b, _, c]";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "conditionals.wl", 3);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithFunctionDefinitions() throws IOException {
        Path testFile = tempDir.resolve("functions.wl");
        String content = "add[x_, y_] := x + y;\n"
                        + "multiply[x_, y_] := x * y;\n"
                        + "divide[x_, y_] := If[y != 0, x / y, $Failed]";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "functions.wl", 3);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithEmptyFile() throws IOException {
        Path testFile = tempDir.resolve("empty.wl");
        Files.write(testFile, "".getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "empty.wl", 0);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithMultipleFiles() throws IOException {
        Path file1 = tempDir.resolve("file1.wl");
        Path file2 = tempDir.resolve("file2.wl");
        Files.write(file1, "x = 1".getBytes(StandardCharsets.UTF_8));
        Files.write(file2, "y = 2".getBytes(StandardCharsets.UTF_8));

        InputFile inputFile1 = createInputFile(file1, "file1.wl", 1);
        InputFile inputFile2 = createInputFile(file2, "file2.wl", 1);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(java.util.Arrays.asList(inputFile1, inputFile2));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    @Test
    void testExecuteWithIOException() throws IOException {
        Path testFile = tempDir.resolve("deleted.wl");
        Files.write(testFile, "x = 1".getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "deleted.wl", 1);

        // Delete the file to cause IOException
        Files.delete(testFile);

        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        // Should not throw exception, just log error
        sensor.execute(sensorContext);
    }

    @Test
    void testExecuteWithHighComplexityCode() throws IOException {
        Path testFile = tempDir.resolve("high_complexity.wl");
        String content = "complexFunc[x_, y_, z_] := If[x > 0 && y < 10 || z == 5,\n"
                        + "  If[x > 100, For[i = 1, i <= x, i++, Print[i]], While[y > 0, y--]],\n"
                        + "  Which[z == 1, a, z == 2, b, z == 3, c, True, d]]";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = createInputFile(testFile, "high_complexity.wl", 3);
        setupMockMeasures();

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        sensor.execute(sensorContext);

        verify(sensorContext, atLeastOnce()).newMeasure();
    }

    private InputFile createInputFile(Path path, String filename, int lines) {
        InputFile inputFile = mock(InputFile.class);
        when(inputFile.uri()).thenReturn(path.toUri());
        when(inputFile.filename()).thenReturn(filename);
        when(inputFile.lines()).thenReturn(lines);
        return inputFile;
    }

    @SuppressWarnings("unchecked")
    private void setupMockMeasures() {
        NewMeasure<Integer> intMeasure = mock(NewMeasure.class);
        NewMeasure<String> stringMeasure = mock(NewMeasure.class);

        when(intMeasure.on(any())).thenReturn(intMeasure);
        when(intMeasure.forMetric(any())).thenReturn(intMeasure);
        when(intMeasure.withValue(any(Integer.class))).thenReturn(intMeasure);

        when(stringMeasure.on(any())).thenReturn(stringMeasure);
        when(stringMeasure.forMetric(any())).thenReturn(stringMeasure);
        when(stringMeasure.withValue(any(String.class))).thenReturn(stringMeasure);

        when(sensorContext.<Integer>newMeasure()).thenReturn(intMeasure);
        when(sensorContext.<String>newMeasure()).thenReturn(stringMeasure);
    }
}
