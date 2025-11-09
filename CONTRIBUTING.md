# Contributing to SonarQube Mathematica Plugin

Thank you for your interest in contributing to the Mathematica SonarQube Plugin! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Building the Plugin](#building-the-plugin)
- [Running Tests](#running-tests)
- [Adding New Rules](#adding-new-rules)
- [Code Style](#code-style)
- [Submitting Changes](#submitting-changes)
- [Release Process](#release-process)

## Code of Conduct

This project follows the standard open-source code of conduct:

- **Be respectful**: Treat all contributors with respect
- **Be constructive**: Provide helpful feedback and suggestions
- **Be collaborative**: Work together to improve the project
- **Be patient**: Remember that everyone is learning

## Getting Started

### Prerequisites

Before contributing, make sure you have:

- **Java 11 or higher** (JDK 11+)
- **Gradle 8.0+** (included via wrapper)
- **SonarQube 9.9+ or 10.x** (for testing)
- **Git** for version control
- **A code editor** (IntelliJ IDEA recommended)

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone git@github.com:YOUR_USERNAME/wolfralyze.git
   cd wolfralyze
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream git@github.com:bceverly/wolfralyze.git
   ```

## Development Setup

### 1. Install SonarQube Locally

Download and install SonarQube for testing:

```bash
# Download SonarQube Community Edition
wget https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-10.3.0.82913.zip
unzip sonarqube-10.3.0.82913.zip
export SONARQUBE_HOME=/path/to/sonarqube-10.3.0.82913

# Start SonarQube
$SONARQUBE_HOME/bin/macosx-universal-64/sonar.sh start
```

### 2. Build the Plugin

```bash
# Build the plugin JAR
make build

# Or use Gradle directly
./gradlew clean build
```

### 3. Install Plugin Locally

```bash
# Install to your local SonarQube instance
SONARQUBE_HOME=/path/to/sonarqube make install
```

This will:
- Stop SonarQube
- Remove old plugin versions
- Install the new plugin
- Restart SonarQube
- Wait until ready

### 4. Verify Installation

1. Open http://localhost:9000
2. Login (default: admin/admin)
3. Go to **Administration → Marketplace → Installed**
4. Verify "Mathematica" plugin is listed

## Building the Plugin

### Using Make (Recommended)

```bash
# Show all available targets
make help

# Build the plugin
make build

# Run tests
make test

# Run code style checks
make lint

# Clean build artifacts
make clean

# Build and install to SonarQube
SONARQUBE_HOME=/path/to/sonarqube make install

# Analyze plugin's own Java code
make self-scan
```

### Using Gradle Directly

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Run checkstyle
./gradlew checkstyleMain checkstyleTest

# Clean
./gradlew clean
```

### Build Output

The plugin JAR will be created at:
```
build/libs/wolfralyze-VERSION.jar
```

## Running Tests

### Unit Tests

```bash
# Run all tests
make test

# Or with Gradle
./gradlew test

# Run specific test class
./gradlew test --tests "*SqlInjectionDetectorTest"

# Run with verbose output
./gradlew test --info
```

### Test Coverage

Test coverage is important! Aim for:
- **80%+ line coverage** for new code
- **100% coverage** for security-related rules

### Writing Tests

Example test structure:

```java
package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MyNewRuleDetectorTest {

    private MyNewRuleDetector detector = new MyNewRuleDetector();

    @Test
    void shouldDetectViolation() {
        String code = "badPattern[x]";

        var issues = detector.analyze(code);

        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getRuleKey()).isEqualTo("M:1234");
    }

    @Test
    void shouldNotDetectViolationInValidCode() {
        String code = "goodPattern[x]";

        var issues = detector.analyze(code);

        assertThat(issues).isEmpty();
    }
}
```

## Adding New Rules

### 1. Choose a Rule Category

Rules are organized by category:
- **Security Vulnerabilities** (`M:S1001-S1100`)
- **Security Hotspots** (`M:H1001-H1100`)
- **Bugs** (`M:B1001-B1300`)
- **Code Smells** (`M:C1001-C1500`)

### 2. Define the Rule

Add rule definition to `*RulesDefinition.java`:

```java
repository.createRule("M:C1501")
    .setName("Functions should have descriptive names")
    .setHtmlDescription(
        "<p>Function names should be descriptive and follow PascalCase convention.</p>" +
        "<h2>Noncompliant Code Example</h2>" +
        "<pre>f[x_] := x^2  (* BAD: Single letter name *)</pre>" +
        "<h2>Compliant Solution</h2>" +
        "<pre>SquareValue[x_] := x^2  (* GOOD: Descriptive name *)</pre>"
    )
    .setSeverity(Severity.MINOR)
    .setType(RuleType.CODE_SMELL)
    .setTags("convention", "readability")
    .setDebtRemediationFunction(
        repository.debtRemediationFunctions().constantPerIssue("5min")
    );
```

### 3. Implement the Detector

Create detector class in appropriate package:

```java
package org.sonar.plugins.mathematica.rules.codesmells;

public class FunctionNamingDetector {

    private static final String RULE_KEY = "M:C1501";
    private static final Pattern FUNCTION_PATTERN =
        Pattern.compile("([a-z][a-zA-Z0-9]*)\\[");

    public List<Issue> analyze(String content, SymbolTable symbolTable) {
        List<Issue> issues = new ArrayList<>();

        Matcher matcher = FUNCTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            if (isSingleLetter(functionName) || !isPascalCase(functionName)) {
                issues.add(new Issue(
                    RULE_KEY,
                    "Function '" + functionName + "' should have a descriptive PascalCase name",
                    matcher.start(),
                    matcher.end()
                ));
            }
        }

        return issues;
    }

    private boolean isSingleLetter(String name) {
        return name.length() == 1;
    }

    private boolean isPascalCase(String name) {
        return Character.isUpperCase(name.charAt(0));
    }
}
```

### 4. Add to UnifiedRuleVisitor

Register your detector in `UnifiedRuleVisitor.java`:

```java
// In analyzeFile() method
issues.addAll(functionNamingDetector.analyze(content, symbolTable));
```

### 5. Write Tests

Create comprehensive test file:

```java
class FunctionNamingDetectorTest {

    @Test
    void shouldDetectSingleLetterFunctionName() {
        String code = "f[x_] := x^2";
        var issues = detector.analyze(code, symbolTable);
        assertThat(issues).hasSize(1);
    }

    @Test
    void shouldDetectLowercaseFunctionName() {
        String code = "myFunction[x_] := x^2";
        var issues = detector.analyze(code, symbolTable);
        assertThat(issues).hasSize(1);
    }

    @Test
    void shouldNotDetectValidPascalCaseName() {
        String code = "MyFunction[x_] := x^2";
        var issues = detector.analyze(code, symbolTable);
        assertThat(issues).isEmpty();
    }
}
```

### 6. Update Documentation

Add your rule to:
- `docs/Rule-Catalog.md` - Add to appropriate category
- `docs/Code-Smells.md` (or appropriate category file) - Add detailed example

### 7. Test End-to-End

```bash
# Build and install
make build
SONARQUBE_HOME=/path/to/sonarqube make install

# Run analysis on test project
cd /path/to/test/mathematica/project
sonar-scanner

# Verify rule appears in SonarQube UI
```

## Code Style

### Java Code Style

We use **Checkstyle** to enforce code style:

```bash
# Check code style
make lint

# View detailed violations
cat build/reports/checkstyle/main.html
cat build/reports/checkstyle/test.html
```

### Style Guidelines

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters maximum
- **Braces**: Required for all control structures
- **Naming**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Comments**: Javadoc for public methods
- **Imports**: No wildcard imports

### Example

```java
/**
 * Detector for SQL injection vulnerabilities in Mathematica code.
 *
 * This detector uses taint analysis to track untrusted data flowing
 * into SQL query construction.
 */
public class SqlInjectionDetector {

    private static final String RULE_KEY = "M:S1001";
    private static final Logger LOG = LoggerFactory.getLogger(SqlInjectionDetector.class);

    /**
     * Analyze code for SQL injection vulnerabilities.
     *
     * @param content The Mathematica source code to analyze
     * @param symbolTable Symbol table with cross-file information
     * @return List of issues found
     */
    public List<Issue> analyze(String content, SymbolTable symbolTable) {
        // Implementation here
        return Collections.emptyList();
    }
}
```

## Submitting Changes

### 1. Create a Branch

```bash
# Update your fork
git fetch upstream
git checkout master
git merge upstream/master

# Create feature branch
git checkout -b feature/my-new-rule
```

### 2. Make Changes

- Write code following style guidelines
- Add tests with good coverage
- Update documentation
- Ensure all tests pass

### 3. Test Your Changes

```bash
# Run all checks
make lint
make test
make build

# Test in real SonarQube
SONARQUBE_HOME=/path/to/sonarqube make install
# Run analysis on test projects
```

### 4. Commit Changes

Write clear, descriptive commit messages:

```bash
git add .
git commit -m "Add function naming convention rule (M:C1501)

- Detects single-letter function names
- Enforces PascalCase naming convention
- Includes 8 test cases with 100% coverage
- Updates documentation with examples"
```

### 5. Push and Create PR

```bash
# Push to your fork
git push origin feature/my-new-rule

# Create Pull Request on GitHub
# Include:
# - Clear description of changes
# - Reference to any related issues
# - Screenshots if UI changes
# - Test results
```

### PR Checklist

Before submitting, ensure:

- [ ] All tests pass (`make test`)
- [ ] Code style is clean (`make lint`)
- [ ] New code has test coverage (80%+)
- [ ] Documentation is updated
- [ ] CHANGELOG.md is updated
- [ ] Commit messages are clear
- [ ] PR description explains changes

### Review Process

1. **Automated checks**: CI will run tests and linting
2. **Code review**: Maintainers will review your code
3. **Feedback**: Address any comments or suggestions
4. **Approval**: Once approved, PR will be merged
5. **Release**: Changes included in next release

## Release Process

Releases are managed by maintainers. The process:

1. Update version in git tags
2. Update CHANGELOG.md
3. Build release JAR
4. Create GitHub Release
5. Update SonarQube Marketplace
6. Announce in community forum

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **Major** (1.0.0): Breaking changes
- **Minor** (0.1.0): New features, backward compatible
- **Patch** (0.0.1): Bug fixes, backward compatible

## Getting Help

### Questions?

- **GitHub Discussions**: https://github.com/bceverly/wolfralyze/discussions
- **GitHub Issues**: https://github.com/bceverly/wolfralyze/issues
- **Documentation**: https://github.com/bceverly/wolfralyze/wiki

### Found a Bug?

1. Check existing issues
2. Create new issue with:
   - Clear description
   - Steps to reproduce
   - Expected vs actual behavior
   - Plugin version
   - SonarQube version
   - Sample code (if applicable)

### Want to Add a Rule?

1. Check [Future Rules](docs/Future-Rules.md) for ideas
2. Open an issue to discuss the rule first
3. Get feedback before implementing
4. Follow the process above

## Resources

### SonarQube Plugin Development

- [SonarQube Plugin API](https://docs.sonarqube.org/latest/extend/developing-plugin/)
- [Plugin Examples](https://github.com/SonarSource/sonar-custom-plugin-example)
- [SonarQube Community Forum](https://community.sonarsource.com/)

### Mathematica Language

- [Wolfram Language Documentation](https://reference.wolfram.com/language/)
- [Mathematica Stack Exchange](https://mathematica.stackexchange.com/)

### Security Rules

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CWE List](https://cwe.mitre.org/)

## License

By contributing, you agree that your contributions will be licensed under the GPL-3.0 License.

## Thank You!

Your contributions make this project better for everyone in the Mathematica community. Thank you for taking the time to contribute! 🎉
