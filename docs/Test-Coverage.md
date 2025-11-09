# Test Coverage

The Mathematica SonarQube plugin includes **native test coverage support** that integrates seamlessly with SonarQube's coverage tracking and visualization features.

## Overview

Test coverage shows which lines of your Mathematica code are executed by your tests, helping you identify untested code and improve test quality. The plugin automatically imports and displays coverage data in the SonarQube UI.

## Quick Start

### 1. Set Up Your Test Project

First, ensure you have a test runner that uses Mathematica's built-in testing framework or your custom test framework.

### 2. Use CoverageUtils.wl

The plugin includes `CoverageUtils.wl`, a reusable package for exporting coverage data:

```mathematica
#!/usr/bin/env wolframscript

(* Load the coverage utility *)
Get["CoverageUtils.wl"]

(* Run your tests and track coverage *)
(* ... your test execution code ... *)

(* Generate coverage data structure *)
coverageData = <|
  "/path/to/Source.wl" -> <|
    "FileName" -> "Source",
    "FullPath" -> "/path/to/Source.wl",
    "TotalLines" -> 100,
    "CodeLines" -> 75,
    "CoveredLines" -> 50,
    "Coverage" -> 0.667,
    "LineCoverage" -> <|
      1 -> <|"Line" -> 1, "IsCode" -> True, "Hits" -> 5|>,
      2 -> <|"Line" -> 2, "IsCode" -> False, "Hits" -> 0|>,
      (* ... *)
    |>
  |>
|>

(* Export to JSON format *)
CoverageUtils`ExportCoverageJSON[coverageData, "coverage/coverage.json"]
```

### 3. Run SonarQube Scan

```bash
sonar-scanner
```

The coverage data is automatically imported during the scan. You'll see output like:

```
INFO  Sensor Mathematica Coverage Sensor [mathematica]
INFO  Reading coverage report from: /path/to/coverage/coverage.json
INFO  Coverage import complete: 3 files processed, 0 files skipped
```

### 4. View Coverage in SonarQube

After the scan completes, coverage data appears in:
- **Project Dashboard** - Overall coverage percentage
- **Measures** tab - Detailed coverage metrics
- **Code** tab - Line-by-line coverage highlighting

## Complete Working Example

See the **[Wolfralyze Test Project](https://github.com/bceverly/wolfralyze-test-project)** for a complete, ready-to-use example:

```bash
# Clone the test project
git clone https://github.com/bceverly/wolfralyze-test-project.git
cd wolfralyze-test-project

# Run tests (generates coverage.json)
make test

# Run SonarQube scan
make scan
```

The test project includes:
- ✅ `RunTests.wl` - Complete test runner with coverage tracking
- ✅ `CoverageUtils.wl` - Reusable coverage export utility
- ✅ Sample Mathematica code with intentional issues
- ✅ Unit tests using `VerificationTest`
- ✅ Makefile for easy automation
- ✅ Full documentation and examples

## Coverage Data Format

### Required Structure

The coverage data must follow this structure:

```mathematica
coverageData = <|
  "file-path-1" -> <|
    "FileName" -> "MyModule",           (* String: File name without extension *)
    "FullPath" -> "/abs/path/to/file",  (* String: Absolute path to source file *)
    "TotalLines" -> 100,                 (* Integer: Total lines in file *)
    "CodeLines" -> 75,                   (* Integer: Lines with code *)
    "CoveredLines" -> 50,                (* Integer: Lines covered by tests *)
    "Coverage" -> 0.667,                 (* Real: Coverage ratio (0.0 to 1.0) *)
    "LineCoverage" -> <|
      lineNum -> <|
        "Line" -> lineNum,               (* Integer: Line number (1-based) *)
        "IsCode" -> True|False,          (* Boolean: Is this a code line? *)
        "Hits" -> hitCount              (* Integer: Number of times executed *)
      |>,
      (* ... one entry per line ... *)
    |>
  |>,
  (* ... one entry per source file ... *)
|>
```

### Validation

`CoverageUtils`ExportCoverageJSON` automatically validates the data structure before export. If validation fails, you'll see error messages indicating which fields are missing or incorrect.

## Configuration

### Coverage File Path

By default, the plugin looks for `coverage/coverage.json` in your project root. You can customize this in `sonar-project.properties`:

```properties
# Custom coverage file path
sonar.mathematica.coverage.reportPath=path/to/coverage.json
```

### Project Configuration Example

```properties
sonar.projectKey=my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.sources=src
sonar.tests=Tests
sonar.inclusions=**/*.wl,**/*.m,**/*.wls

# Coverage configuration
sonar.mathematica.coverage.reportPath=coverage/coverage.json
```

## Implementing Coverage Tracking

### Option 1: Use the Test Project Template

The easiest way to get started is to use the test project as a template:

1. Copy `RunTests.wl` and `CoverageUtils.wl` to your project
2. Adapt `RunTests.wl` to your test structure
3. Run tests with `make test` or `wolframscript RunTests.wl`

### Option 2: Custom Implementation

If you need custom coverage tracking:

```mathematica
(* 1. Track which lines are executed *)
(* You'll need to instrument your code or use execution tracing *)

(* 2. Build the coverage data structure *)
coverageData = <||>;

(* For each source file *)
Do[
  Module[{file, lines, lineCov},
    file = sourceFiles[[i]];
    lines = Import[file, "Lines"];

    (* Build line coverage map *)
    lineCov = <||>;
    Do[
      lineCov[j] = <|
        "Line" -> j,
        "IsCode" -> isCodeLine[lines[[j]]],
        "Hits" -> getHitCount[file, j]
      |>,
      {j, Length[lines]}
    ];

    (* Add file entry *)
    coverageData[file] = <|
      "FileName" -> FileBaseName[file],
      "FullPath" -> file,
      "TotalLines" -> Length[lines],
      "CodeLines" -> countCodeLines[lines],
      "CoveredLines" -> countCoveredLines[lineCov],
      "Coverage" -> calculateCoverage[lineCov],
      "LineCoverage" -> lineCov
    |>;
  ],
  {i, Length[sourceFiles]}
];

(* 3. Export using CoverageUtils *)
Get["CoverageUtils.wl"]
CoverageUtils`ExportCoverageJSON[coverageData, "coverage/coverage.json"]
```

## Troubleshooting

### Coverage Not Appearing in SonarQube

**Symptoms:** No coverage data in SonarQube UI after scan

**Solutions:**
1. Check the scan output for coverage sensor messages:
   ```
   INFO  Sensor Mathematica Coverage Sensor [mathematica]
   INFO  Reading coverage report from: ...
   ```

2. Verify `coverage/coverage.json` exists and is valid JSON:
   ```bash
   ls -la coverage/coverage.json
   python -m json.tool coverage/coverage.json > /dev/null
   ```

3. Check file paths match between coverage data and SonarQube:
   - Coverage data uses absolute paths
   - SonarQube resolves files relative to project base directory

### Files Skipped During Import

**Symptoms:** "X files skipped" in sensor output

**Possible causes:**
- File paths in coverage.json don't match actual source files
- Files are excluded from SonarQube analysis
- Source files are in different location than coverage data indicates

**Solutions:**
1. Check file paths are absolute in coverage data
2. Verify files are included in `sonar.sources` or `sonar.inclusions`
3. Run scan with `-X` flag for debug output

### Coverage Data Validation Errors

**Symptoms:** Error messages from `ExportCoverageJSON`

**Solutions:**
1. Check all required fields are present:
   - `FileName`, `FullPath`, `TotalLines`, `CodeLines`, `CoveredLines`, `Coverage`, `LineCoverage`

2. Verify data types:
   - Strings: `FileName`, `FullPath`
   - Integers: `TotalLines`, `CodeLines`, `CoveredLines`, `Line`, `Hits`
   - Real: `Coverage` (0.0 to 1.0)
   - Boolean: `IsCode`

3. Ensure `LineCoverage` is an Association, not a List

## Best Practices

### 1. Automate Coverage Generation

Use a Makefile or build script to automate test execution and coverage generation:

```makefile
.PHONY: test scan

test:
	wolframscript RunTests.wl

scan: test
	sonar-scanner
```

### 2. Version Control

- ✅ **DO** commit: `RunTests.wl`, `CoverageUtils.wl`, `sonar-project.properties`
- ❌ **DON'T** commit: `coverage/` directory (add to `.gitignore`)

### 3. CI/CD Integration

Example GitHub Actions workflow:

```yaml
- name: Run tests
  run: make test

- name: SonarQube scan
  run: sonar-scanner
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### 4. Coverage Goals

Set coverage targets in your Quality Gate:
- **Minimum coverage**: 70-80% for new code
- **Coverage on new code**: 80% or higher
- Review uncovered critical paths

## Advanced Topics

### Custom Coverage Metrics

You can enhance the coverage data with additional metrics:

```mathematica
coverageData[file] = <|
  (* ... standard fields ... *)
  "BranchCoverage" -> 0.75,  (* Custom: branch coverage *)
  "FunctionCoverage" -> 0.90, (* Custom: function coverage *)
  (* Note: Plugin currently uses line coverage only *)
|>
```

### Large Codebases

For projects with many files:
- Coverage JSON files can be large (10MB+)
- Consider parallel test execution
- Use incremental coverage (only changed files)

### Multiple Test Suites

Merge coverage from multiple test runs:

```mathematica
(* Run unit tests *)
unitCoverage = runUnitTests[];

(* Run integration tests *)
integrationCoverage = runIntegrationTests[];

(* Merge coverage data *)
mergedCoverage = mergeCoverageData[unitCoverage, integrationCoverage];

(* Export merged data *)
CoverageUtils`ExportCoverageJSON[mergedCoverage, "coverage/coverage.json"]
```

## See Also

- [Installation](Installation) - Set up the plugin
- [Configuration](Configuration) - Configure your project
- [Best Practices](Best-Practices) - Write quality Mathematica code
- [CI/CD Integration](CI-CD-Integration) - Automate with GitHub Actions, GitLab, Jenkins
- [Test Project](https://github.com/bceverly/wolfralyze-test-project) - Complete working example

## Support

- **Documentation**: [GitHub Wiki](https://github.com/bceverly/wolfralyze/wiki)
- **Issues**: [GitHub Issues](https://github.com/bceverly/wolfralyze/issues)
- **Example Project**: [wolfralyze-test-project](https://github.com/bceverly/wolfralyze-test-project)
