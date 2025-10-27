# SonarQube Mathematica Plugin

A SonarQube plugin that provides code analysis for Wolfram Mathematica language files.

## Current Features

### ✅ Code Duplication Detection
The plugin currently supports **Copy-Paste Detection (CPD)** to identify duplicated code blocks across your Mathematica codebase. This helps you:
- Find repeated code patterns
- Identify opportunities for refactoring
- Improve code maintainability
- Reduce technical debt

## Supported File Types

- `.m` - Mathematica package files
- `.wl` - Wolfram Language files
- `.wls` - Wolfram Language script files

## Requirements

- Java 11 or higher
- SonarQube 9.9 or higher
- Gradle 7.0 or higher (for building)

## Building the Plugin

1. Navigate to the project directory:
```bash
cd ~/dev/sonar-mathematica-plugin
```

2. Build the plugin JAR:
```bash
./gradlew clean build
```

The plugin JAR will be created at: `build/libs/sonar-mathematica-plugin-0.1.0-SNAPSHOT.jar`

## Installation

### Option 1: Manual Installation

1. Build the plugin (see above)

2. Copy the JAR to your SonarQube plugins directory:
```bash
cp build/libs/sonar-mathematica-plugin-*.jar $SONARQUBE_HOME/extensions/plugins/
```

3. Restart SonarQube:
```bash
$SONARQUBE_HOME/bin/[your-os]/sonar.sh restart
```

### Option 2: Using Gradle Task

```bash
./gradlew installPlugin
```

This will copy the plugin to `~/.sonar/extensions/plugins/`

**Note:** After installation, you must restart SonarQube for the plugin to be loaded.

## Verifying Installation

1. Start SonarQube and log in as administrator

2. Navigate to: **Administration → Marketplace → Installed**

3. You should see "Mathematica" listed among installed plugins

4. Navigate to: **Administration → General Settings → Mathematica**

5. You should see configuration options for:
   - File Suffixes
   - Duplication → Minimum Tokens
   - Duplication → Minimum Lines

## Usage

### 1. Configure Your Project

Create a `sonar-project.properties` file in your Mathematica project root:

```properties
# Project identification
sonar.projectKey=my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.projectVersion=1.0

# Source code location
sonar.sources=.

# File encoding
sonar.sourceEncoding=UTF-8

# Language
sonar.language=mathematica

# Mathematica-specific settings (optional)
sonar.mathematica.file.suffixes=.m,.wl,.wls

# CPD settings (optional)
sonar.cpd.mathematica.minimumTokens=100
sonar.cpd.mathematica.minimumLines=10
```

### 2. Run Analysis

#### Using SonarScanner

```bash
sonar-scanner
```

#### Using Gradle with SonarQube Plugin

Add to your `build.gradle`:

```gradle
plugins {
    id "org.sonarqube" version "4.4.1.3373"
}

sonar {
    properties {
        property "sonar.projectKey", "my-mathematica-project"
        property "sonar.host.url", "http://localhost:9000"
        property "sonar.sources", "."
    }
}
```

Then run:
```bash
./gradlew sonar
```

### 3. View Results

1. Open SonarQube web interface: http://localhost:9000

2. Navigate to your project

3. Go to **Measures → Duplications** to see:
   - Duplicated blocks
   - Duplicated lines
   - Duplicated files
   - Density of duplication

## Configuration Options

### File Suffixes
**Key:** `sonar.mathematica.file.suffixes`
**Default:** `.m,.wl,.wls`
**Description:** Comma-separated list of file extensions to analyze

### Minimum Tokens
**Key:** `sonar.cpd.mathematica.minimumTokens`
**Default:** `100`
**Description:** Minimum number of tokens required to flag a duplication

### Minimum Lines
**Key:** `sonar.cpd.mathematica.minimumLines`
**Default:** `10`
**Description:** Minimum number of lines required to flag a duplication

## Example Mathematica Code

Create a test file `test.m`:

```mathematica
(* Example Mathematica code for testing duplication detection *)

Module[{x, y, z},
  x = 1;
  y = 2;
  z = x + y;
  Print[z]
]

(* This is duplicated code - CPD should detect it *)
Module[{a, b, c},
  a = 1;
  b = 2;
  c = a + b;
  Print[c]
]
```

## How Duplication Detection Works

The plugin uses a **tokenizer** to break down Mathematica code into meaningful tokens:

1. **Comments** (`(* ... *)`) are ignored
2. **Strings** are normalized to a generic `"STRING"` token
3. **Numbers** are normalized to a generic `NUMBER` token
4. **Identifiers** (function names, variables) are preserved
5. **Operators** (`->`, `:=`, `@@`, etc.) are preserved

SonarQube's built-in CPD engine then compares token sequences across files to find duplications.

### What Gets Detected

✅ **Will be detected as duplicates:**
- Identical function definitions with different variable names
- Copy-pasted code blocks
- Similar algorithm implementations

❌ **Won't be detected as duplicates:**
- Comments (ignored during tokenization)
- Different string literals (normalized)
- Different numeric values (normalized)

## Development

### Project Structure

```
sonar-mathematica-plugin/
├── build.gradle                          # Build configuration
├── settings.gradle                       # Gradle settings
├── gradle.properties                     # Gradle properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/sonar/plugins/mathematica/
│   │   │       ├── MathematicaPlugin.java           # Plugin entry point
│   │   │       ├── MathematicaLanguage.java         # Language definition
│   │   │       └── MathematicaCpdTokenizer.java     # Duplication detector
│   │   └── resources/
│   └── test/
│       └── java/
│           └── org/sonar/plugins/mathematica/
└── README.md                             # This file
```

### Adding Tests

Create test files in `src/test/java/org/sonar/plugins/mathematica/`:

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MathematicaLanguageTest {
    @Test
    void testLanguageKey() {
        MathematicaLanguage language = new MathematicaLanguage(new MapSettings().asConfig());
        assertThat(language.getKey()).isEqualTo("mathematica");
    }
}
```

Run tests:
```bash
./gradlew test
```

## Roadmap

### Future Features (Planned)

- [ ] Basic code metrics (lines of code, complexity)
- [ ] Rule-based analysis (code smells, best practices)
- [ ] Function/Module complexity metrics
- [ ] Security vulnerability detection
- [ ] Performance anti-pattern detection
- [ ] Notebook (`.nb`) file support
- [ ] Custom rule templates

## Contributing

Contributions are welcome! Areas where help is needed:

1. **Tokenizer improvements** - Handle more Mathematica syntax edge cases
2. **Rule definitions** - Define what constitutes "bad" Mathematica code
3. **Testing** - Add test cases with various Mathematica code patterns
4. **Documentation** - Improve usage guides and examples

## License

LGPL-3.0

## Troubleshooting

### Plugin doesn't appear in SonarQube

- Verify the JAR was copied to the correct plugins directory
- Check `$SONARQUBE_HOME/logs/sonar.log` for errors
- Ensure SonarQube was restarted after plugin installation

### Files not being analyzed

- Check that file extensions match `sonar.mathematica.file.suffixes`
- Verify `sonar.sources` points to the correct directory
- Check SonarQube scanner logs for errors

### No duplications detected

- Lower `sonar.cpd.mathematica.minimumTokens` or `minimumLines`
- Verify files actually contain duplicated code
- Check that files are being scanned (see scanner logs)

## Contact

- **Issues:** [Create an issue on GitHub]
- **Email:** [Your email]

## Acknowledgments

- Inspired by SonarSource language plugins (SonarJS, SonarPython, etc.)
- Built using the SonarQube Plugin API
