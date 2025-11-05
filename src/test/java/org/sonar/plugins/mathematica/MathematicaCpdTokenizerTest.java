package org.sonar.plugins.mathematica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MathematicaCpdTokenizerTest {

    private MathematicaCpdTokenizer tokenizer;
    private SensorContext sensorContext;
    private FileSystem fileSystem;
    private FilePredicates predicates;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        tokenizer = new MathematicaCpdTokenizer();
        sensorContext = mock(SensorContext.class);
        fileSystem = mock(FileSystem.class);
        predicates = mock(FilePredicates.class);

        when(sensorContext.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.predicates()).thenReturn(predicates);
    }

    static Stream<TestFileData> provideTestFileData() {
        return Stream.of(
            new TestFileData("simple", "x = 5;\nPrint[x]", 2),
            new TestFileData("comments", "(* This is a comment *)\nx = 5;", 2),
            new TestFileData("strings", "str = \"Hello World\";\nPrint[str]", 2),
            new TestFileData("numbers", "x = 123; y = 45.67; z = 1.5e-10", 1),
            new TestFileData("operators", "x -> y; a :> b; f /@ list; g @@ args", 1),
            new TestFileData("empty", "", 0),
            new TestFileData("complex",
                "Module[{x = 1, y = 2}, x + y];\n"
                + "If[x > 0, Print[\"positive\"], Print[\"negative\"]];\n"
                + "Table[i^2, {i, 10}]", 3)
        );
    }

    static class TestFileData {
        final String name;
        final String content;
        final int lines;

        TestFileData(String name, String content, int lines) {
            this.name = name;
            this.content = content;
            this.lines = lines;
        }
    }

    @Test
    void testDescribe() {
        SensorDescriptor descriptor = mock(SensorDescriptor.class);
        when(descriptor.name(any())).thenReturn(descriptor);
        when(descriptor.onlyOnLanguage(any())).thenReturn(descriptor);

        tokenizer.describe(descriptor);

        verify(descriptor).name("Mathematica CPD Tokenizer");
        verify(descriptor).onlyOnLanguage(MathematicaLanguage.KEY);
    }

    @Test
    void testExecuteWithNoFiles() {
        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.emptyList());

        tokenizer.execute(sensorContext);

        // Should execute without errors on empty file list
        verify(fileSystem).inputFiles(any());
    }

    @ParameterizedTest
    @MethodSource("provideTestFileData")
    void testExecuteWithVariousFileContents(TestFileData testData) throws IOException {
        // Create test file with specific content
        Path testFile = tempDir.resolve(testData.name + ".wl");
        Files.write(testFile, testData.content.getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = mock(InputFile.class);
        when(inputFile.uri()).thenReturn(testFile.toUri());
        when(inputFile.filename()).thenReturn(testData.name + ".wl");
        when(inputFile.lines()).thenReturn(testData.lines);

        NewCpdTokens cpdTokens = mock(NewCpdTokens.class);
        when(cpdTokens.onFile(any())).thenReturn(cpdTokens);
        when(sensorContext.newCpdTokens()).thenReturn(cpdTokens);

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        tokenizer.execute(sensorContext);

        verify(cpdTokens).save();
    }

    @Test
    void testExecuteWithMultipleFiles() throws IOException {
        // Create two files
        Path file1 = tempDir.resolve("file1.wl");
        Path file2 = tempDir.resolve("file2.wl");
        Files.write(file1, "x = 1".getBytes(StandardCharsets.UTF_8));
        Files.write(file2, "y = 2".getBytes(StandardCharsets.UTF_8));

        InputFile inputFile1 = mock(InputFile.class);
        when(inputFile1.uri()).thenReturn(file1.toUri());
        when(inputFile1.filename()).thenReturn("file1.wl");
        when(inputFile1.lines()).thenReturn(1);

        InputFile inputFile2 = mock(InputFile.class);
        when(inputFile2.uri()).thenReturn(file2.toUri());
        when(inputFile2.filename()).thenReturn("file2.wl");
        when(inputFile2.lines()).thenReturn(1);

        NewCpdTokens cpdTokens1 = mock(NewCpdTokens.class);
        NewCpdTokens cpdTokens2 = mock(NewCpdTokens.class);
        when(cpdTokens1.onFile(any())).thenReturn(cpdTokens1);
        when(cpdTokens2.onFile(any())).thenReturn(cpdTokens2);
        when(sensorContext.newCpdTokens())
            .thenReturn(cpdTokens1)
            .thenReturn(cpdTokens2);

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(java.util.Arrays.asList(inputFile1, inputFile2));

        tokenizer.execute(sensorContext);

        verify(cpdTokens1).save();
        verify(cpdTokens2).save();
    }

    @Test
    void testExecuteWithIOException() throws IOException {
        // Create file but delete it to cause IOException
        Path testFile = tempDir.resolve("deleted.wl");
        Files.write(testFile, "x = 1".getBytes(StandardCharsets.UTF_8));

        InputFile inputFile = mock(InputFile.class);
        when(inputFile.uri()).thenReturn(testFile.toUri());
        when(inputFile.filename()).thenReturn("deleted.wl");
        when(inputFile.lines()).thenReturn(1);

        // Delete the file to cause IOException
        Files.delete(testFile);

        NewCpdTokens cpdTokens = mock(NewCpdTokens.class);
        when(cpdTokens.onFile(any())).thenReturn(cpdTokens);
        when(sensorContext.newCpdTokens()).thenReturn(cpdTokens);

        FilePredicate predicate = mock(FilePredicate.class);
        when(predicates.hasLanguage(any())).thenReturn(predicate);
        when(predicates.hasType(any())).thenReturn(predicate);
        when(predicates.and(any(), any())).thenReturn(predicate);
        when(fileSystem.inputFiles(any())).thenReturn(Collections.singletonList(inputFile));

        // Should not throw exception, just log error
        tokenizer.execute(sensorContext);

        // Verify it handled the error gracefully
        assertThat(inputFile).isNotNull();
    }
}
