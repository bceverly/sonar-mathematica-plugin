package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.mathematica.MathematicaLanguage;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MathematicaRulesSensorExtendedTest {

    private MathematicaRulesSensor sensor;
    private SensorContext mockContext;
    private SensorDescriptor mockDescriptor;
    private FileSystem mockFileSystem;
    private FilePredicates mockPredicates;
    private Configuration mockConfig;

    @BeforeEach
    void setUp() {
        sensor = new MathematicaRulesSensor();
        mockContext = mock(SensorContext.class);
        mockDescriptor = mock(SensorDescriptor.class);
        mockFileSystem = mock(FileSystem.class);
        mockPredicates = mock(FilePredicates.class);
        mockConfig = mock(Configuration.class);

        // Setup descriptor mock
        when(mockDescriptor.name(anyString())).thenReturn(mockDescriptor);
        when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

        // Setup context mocks
        when(mockContext.fileSystem()).thenReturn(mockFileSystem);
        when(mockContext.config()).thenReturn(mockConfig);
        when(mockFileSystem.predicates()).thenReturn(mockPredicates);

        // Setup config default values
        when(mockConfig.getInt(anyString())).thenReturn(Optional.of(1000));
    }

    @Test
    void testDescribe() {
        sensor.describe(mockDescriptor);

        verify(mockDescriptor).name("Mathematica Rules Sensor");
        verify(mockDescriptor).onlyOnLanguage(MathematicaLanguage.KEY);
    }

    @Test
    void testExecuteWithEmptyFileList() {
        // Setup empty file list
        when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
        when(mockFileSystem.inputFiles(any())).thenReturn(new ArrayList<>());

        // Execute should not crash on empty files
        assertThatCode(() -> sensor.execute(mockContext))
            .doesNotThrowAnyException();
    }

    @Test
    void testExecuteWithSingleSmallFile() throws IOException {
        // Create a temp file with simple Mathematica code
        Path tempFile = Files.createTempFile("test", ".m");
        Files.write(tempFile, "f[x_] := x + 1".getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 1);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            // Execute should process the file
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithFileThatHas2Lines() throws IOException {
        // Test file with exactly 2 lines (< 3 line threshold)
        Path tempFile = Files.createTempFile("test", ".m");
        Files.write(tempFile, "x=1\ny=2".getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 2);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            // Should skip file with <3 lines
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithEmptyFile() throws IOException {
        // Test file that is empty
        Path tempFile = Files.createTempFile("test", ".m");
        Files.write(tempFile, "   \n  \n  ".getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 3);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            // Should skip empty/whitespace-only file
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithLargeFile() throws IOException {
        // Create a file that exceeds the 25,000 line limit
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 26000; i++) {
            content.append("x").append(i).append(" = ").append(i).append(";\n");
        }
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 26000);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            // Setup for issue creation when file exceeds limit
            NewIssue mockIssue = mock(NewIssue.class);
            NewIssueLocation mockLocation = mock(NewIssueLocation.class);
            when(mockContext.newIssue()).thenReturn(mockIssue);
            when(mockIssue.forRule(any(RuleKey.class))).thenReturn(mockIssue);
            when(mockIssue.newLocation()).thenReturn(mockLocation);
            when(mockLocation.on(any(InputFile.class))).thenReturn(mockLocation);
            when(mockLocation.at(any())).thenReturn(mockLocation);
            when(mockLocation.message(anyString())).thenReturn(mockLocation);
            when(mockIssue.at(any())).thenReturn(mockIssue);

            // Should report issue for file exceeding analysis limit
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

            // Verify issue was created
            verify(mockContext, atLeastOnce()).newIssue();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithFileThatExceedsMaximumLines() throws IOException {
        // Test with file exceeding configured max lines (1000 default)
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1100; i++) {
            content.append("line").append(i).append(";\n");
        }
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 1100);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            // Setup for issue creation
            NewIssue mockIssue = mock(NewIssue.class);
            NewIssueLocation mockLocation = mock(NewIssueLocation.class);
            when(mockContext.newIssue()).thenReturn(mockIssue);
            when(mockIssue.forRule(any(RuleKey.class))).thenReturn(mockIssue);
            when(mockIssue.newLocation()).thenReturn(mockLocation);
            when(mockLocation.on(any(InputFile.class))).thenReturn(mockLocation);
            when(mockLocation.at(any())).thenReturn(mockLocation);
            when(mockLocation.message(anyString())).thenReturn(mockLocation);
            when(mockIssue.at(any())).thenReturn(mockIssue);

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithComments() throws IOException {
        Path tempFile = Files.createTempFile("test", ".m");
        Files.write(tempFile, "(* This is a comment *)\nf[x_] := x + 1\n(* TODO: fix this *)".getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 3);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            // Setup for issue creation
            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testQueueIssue() {
        InputFile mockFile = mock(InputFile.class);
        when(mockFile.selectLine(anyInt())).thenReturn(mock(org.sonar.api.batch.fs.TextRange.class));

        // This tests the queueIssue method
        assertThatCode(() ->
            sensor.queueIssue(mockFile, 1, "TEST_RULE", "Test message")
        ).doesNotThrowAnyException();
    }

    @Test
    void testExecuteWithMultipleFiles() throws IOException {
        // Test parallel processing with multiple files
        List<InputFile> files = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Path tempFile = Files.createTempFile("test" + i, ".m");
            Files.write(tempFile, ("f" + i + "[x_] := x + " + i).getBytes(StandardCharsets.UTF_8));
            InputFile mockInputFile = createMockInputFile(tempFile, 1);
            files.add(mockInputFile);
        }

        try {
            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            // Execute with multiple files to test parallel processing
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            // Cleanup
            for (InputFile file : files) {
                try {
                    Files.deleteIfExists(Paths.get(file.uri()));
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    @Test
    void testExecuteWithFileReadError() {
        // Test handling of file read errors
        InputFile mockInputFile = mock(InputFile.class);
        when(mockInputFile.lines()).thenReturn(10);
        when(mockInputFile.uri()).thenReturn(URI.create("file:///nonexistent/file.m"));
        when(mockInputFile.filename()).thenReturn("file.m");
        when(mockInputFile.selectLine(anyInt())).thenReturn(mock(org.sonar.api.batch.fs.TextRange.class));

        List<InputFile> files = new ArrayList<>();
        files.add(mockInputFile);

        when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
        when(mockFileSystem.inputFiles(any())).thenReturn(files);

        // Should handle file read error gracefully
        assertThatCode(() -> sensor.execute(mockContext))
            .doesNotThrowAnyException();
    }

    @Test
    void testSensorInstantiation() {
        MathematicaRulesSensor newSensor = new MathematicaRulesSensor();
        assertThat(newSensor).isNotNull();
    }

    @Test
    void testQueueIssueWithFix() throws IOException {
        // Test the queueIssueWithFix method
        Path tempFile = Files.createTempFile("test", ".m");
        Files.write(tempFile, "f[x_] := x + 1".getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 1);

            String content = "f[x_] := x + 1";
            org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext mockFixContext =
                mock(org.sonar.plugins.mathematica.fixes.QuickFixProvider.QuickFixContext.class);

            // This exercises the queueIssueWithFix method
            assertThatCode(() ->
                sensor.queueIssueWithFix(mockInputFile, 1, "TEST_RULE", "Test message",
                    content, 0, 10, mockFixContext)
            ).doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithRealCodeThatTriggersIssues() throws IOException {
        // Create a file with code that should trigger various rules
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("(* TODO: implement this *)\n");  // Should trigger TODO rule
        content.append("Print[x]\n");  // Should trigger debug code rule
        content.append("result = 42 + 3.14159\n");  // Magic numbers
        content.append("Module[{}, ]\n");  // Empty block
        content.append("f[x_] := x + 1\n");
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 6);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            // Execute - this should actually process the file and queue issues
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

            // Verify issues were queued
            verify(mockContext, atLeast(1)).newIssue();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithComplexMathematicaCode() throws IOException {
        // Create a file with complex Mathematica constructs
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("f[x_, y_, z_, a_, b_, c_, d_, e_] := x + y\n");  // Too many params
        content.append("Do[result = AppendTo[result, i], {i, 100}]\n");  // Append in loop
        content.append("If[a, If[b, If[c, If[d, result]]]]\n");  // Deep nesting
        content.append("MyFunc[x_] := x\n");  // Missing usage message
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 4);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithSymbolTableAnalysis() throws IOException {
        // Test that symbol table analysis is triggered
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("Module[{x, y, z},\n");
        content.append("  x = 1;\n");
        content.append("  y = 2;\n");
        content.append("  x + y\n");  // z is unused
        content.append("]\n");
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 5);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithCrossFileAnalysis() throws IOException {
        // Test cross-file dependency analysis
        List<InputFile> files = new ArrayList<>();

        // File 1: Defines a package
        Path file1 = Files.createTempFile("pkg1", ".m");
        Files.write(file1, "BeginPackage[\"MyPackage`\"]\nMyFunc[x_] := x + 1\nEndPackage[]".getBytes(StandardCharsets.UTF_8));
        files.add(createMockInputFile(file1, 3));

        // File 2: Uses the package
        Path file2 = Files.createTempFile("pkg2", ".m");
        Files.write(file2, "Needs[\"MyPackage`\"]\nresult = MyFunc[5]".getBytes(StandardCharsets.UTF_8));
        files.add(createMockInputFile(file2, 2));

        try {
            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            // This should trigger cross-file analysis
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);
        }
    }

    @Test
    void testExecuteWithASTAnalysis() throws IOException {
        // Test AST-based analysis paths
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("f[x_Integer] := x + 1\n");
        content.append("f[x_Real] := x + 2.0\n");
        content.append("f[x_String] := StringLength[x]\n");
        content.append("g[x_] := Module[{y = x * 2}, y + 1]\n");
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 4);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithPatternBasedDetectors() throws IOException {
        // Exercise pattern-based detector paths
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("(* FIXME: This needs work *)\n");
        content.append("str1 = \"hello\"\n");
        content.append("str2 = str1 <> \"world\" <> \"!\" <> \"test\"\n");  // String concat
        content.append("data = {1, 2, 3}\n");
        content.append("Do[data = AppendTo[data, i], {i, 10}]\n");  // Append in loop
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 5);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithEnhancedQualityDetectors() throws IOException {
        // Test enhanced quality detection methods
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("password = \"secret123\"\n");  // Hardcoded password
        content.append("stream = OpenRead[\"file.txt\"]\n");  // Stream not closed
        content.append("data = Import[\"http://example.com/data\"]\n");  // HTTP without TLS
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 3);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithStyleAndConventions() throws IOException {
        // Test style and conventions detectors
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        // Very long line - split for checkstyle
        content.append("result = function1[arg1] + function2[arg2] + function3[arg3] + ");
        content.append("function4[arg4] + function5[arg5] + function6[arg6] + function7[arg7] + ");
        content.append("function8[arg8] + function9[arg9] + function10[arg10]\n");
        content.append("temp = x\n");  // Generic name
        content.append("data = y\n");  // Generic name
        content.append("val = 999\n");  // Magic number + generic name
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 4);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteMultipleFilesParallelProcessing() throws IOException {
        // Test parallel processing with 10 files to exercise thread-local caching
        List<InputFile> files = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Path tempFile = Files.createTempFile("test" + i, ".m");
            StringBuilder content = new StringBuilder();
            content.append("(* File ").append(i).append(" *)\n");
            content.append("func").append(i).append("[x_] := x + ").append(i).append("\n");
            content.append("Print[\"debug\"]\n");  // Debug code
            content.append("Module[{}, ]\n");  // Empty block
            Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));
            files.add(createMockInputFile(tempFile, 4));
        }

        try {
            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            // This exercises parallel stream processing and thread-local cleanup
            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            for (InputFile file : files) {
                try {
                    Files.deleteIfExists(Paths.get(file.uri()));
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    @Test
    void testExecuteWithBugDetectors() throws IOException {
        // Exercise bug detection paths
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("If[x = 5, result]\n");  // Assignment in conditional
        content.append("data[[100]]\n");  // Potential out of bounds
        content.append("result = 1.0 / 0\n");  // Division by zero
        content.append("While[True, computation]\n");  // Infinite loop
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 4);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteWithVulnerabilityDetectors() throws IOException {
        // Exercise vulnerability detection
        Path tempFile = Files.createTempFile("test", ".m");
        StringBuilder content = new StringBuilder();
        content.append("sql = \"SELECT * FROM users WHERE id=\" <> userId\n");  // SQL injection
        content.append("cmd = \"rm -rf \" <> userInput\n");  // Command injection
        content.append("path = \"/tmp/\" <> fileName\n");  // Path traversal
        Files.write(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            InputFile mockInputFile = createMockInputFile(tempFile, 3);
            List<InputFile> files = new ArrayList<>();
            files.add(mockInputFile);

            when(mockPredicates.and(any(), any())).thenReturn(mock(org.sonar.api.batch.fs.FilePredicate.class));
            when(mockFileSystem.inputFiles(any())).thenReturn(files);

            setupIssueCreationMocks();

            assertThatCode(() -> sensor.execute(mockContext))
                .doesNotThrowAnyException();

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // Helper method to create a mock InputFile
    private InputFile createMockInputFile(Path path, int lineCount) {
        InputFile mockFile = mock(InputFile.class);
        when(mockFile.uri()).thenReturn(path.toUri());
        when(mockFile.lines()).thenReturn(lineCount);
        when(mockFile.filename()).thenReturn(path.getFileName().toString());
        when(mockFile.type()).thenReturn(InputFile.Type.MAIN);
        when(mockFile.selectLine(anyInt())).thenReturn(mock(org.sonar.api.batch.fs.TextRange.class));
        return mockFile;
    }

    // Helper to setup issue creation mocks
    private void setupIssueCreationMocks() {
        NewIssue mockIssue = mock(NewIssue.class);
        NewIssueLocation mockLocation = mock(NewIssueLocation.class);
        when(mockContext.newIssue()).thenReturn(mockIssue);
        when(mockIssue.forRule(any(RuleKey.class))).thenReturn(mockIssue);
        when(mockIssue.newLocation()).thenReturn(mockLocation);
        when(mockLocation.on(any(InputFile.class))).thenReturn(mockLocation);
        when(mockLocation.at(any())).thenReturn(mockLocation);
        when(mockLocation.message(anyString())).thenReturn(mockLocation);
        when(mockIssue.at(any())).thenReturn(mockIssue);
    }
}
