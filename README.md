# SonarQube Plugin for Wolfram Mathematica

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Quality Gate](https://img.shields.io/badge/Quality%20Gate-Tier%201-brightgreen)]()

A **Tier 1** SonarQube plugin providing comprehensive code quality analysis for Wolfram Mathematica with **529+ rules**, comparable to Java and Python support.

---

## 🚀 Quick Start

```bash
# 1. Install the plugin
cp sonar-mathematica-plugin-*.jar $SONARQUBE_HOME/extensions/plugins/
# Restart SonarQube

# 2. Configure your project
cat > sonar-project.properties << EOF
sonar.projectKey=my-mathematica-project
sonar.sources=.
sonar.inclusions=**/*.m,**/*.wl,**/*.wls
EOF

# 3. Run analysis
sonar-scanner
```

**View results** at http://localhost:9000

📖 **Full Documentation**: https://github.com/bceverly/sonar-mathematica-plugin/wiki

---

## ✨ Features

### Comprehensive Rule Coverage (529 Rules)

| Category | Rules | Description |
|----------|-------|-------------|
| **Security Vulnerabilities** | 27 | SQL injection, XSS, command injection, hardcoded credentials |
| **Security Hotspots** | 29 | Weak cryptography, authentication issues, certificate validation |
| **Bugs** | 162 | Null safety, resource leaks, type mismatches, infinite loops |
| **Code Smells** | 247 | Performance issues, complexity, readability, best practices |
| **Performance** | 35 | AppendTo in loops (1000× speedup!), string concatenation, patterns |

### Advanced Analysis

- ✅ **Taint Analysis** - Track untrusted data through your code
- ✅ **Symbol Table** - Cross-file analysis with full symbol resolution
- ✅ **Type Inference** - Understand variable types through patterns
- ✅ **Data Flow** - Track values, nulls, and resources
- ✅ **Control Flow** - Detect unreachable code and infinite loops
- ✅ **Quick Fixes** - 53 automated code corrections
- ✅ **Custom Rules** - 3 templates for project-specific patterns

### OWASP Top 10 Coverage

Covers **9 of 10** OWASP Top 10 2021 categories including injection, broken authentication, XSS, and more.

---

## 📚 Documentation

**Complete documentation available on the Wiki:**

### Getting Started
- **[Installation](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Installation)** - Install in 5 minutes
- **[Configuration](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Configuration)** - Set up your first project
- **[Quick Start](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Home)** - Run your first scan

### Understanding Results
- **[Rule Catalog](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Rule-Catalog)** - All 529 rules indexed
- **[Security Vulnerabilities](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Security-Vulnerabilities)** - Critical security issues
- **[Bug Detection](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Bug-Detection)** - Reliability problems
- **[Code Smells](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Code-Smells)** - Maintainability issues

### Help & Best Practices
- **[Best Practices](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Best-Practices)** - Writing clean Mathematica code
- **[Troubleshooting](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Troubleshooting)** - Common issues and solutions
- **[FAQ](https://github.com/bceverly/sonar-mathematica-plugin/wiki/FAQ)** - Frequently asked questions
- **[CI/CD Integration](https://github.com/bceverly/sonar-mathematica-plugin/wiki/CI-CD-Integration)** - GitHub Actions, GitLab, Jenkins

### For Developers
- **[Architecture](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Architecture)** - Plugin internals
- **[Contributing](CONTRIBUTING.md)** - How to contribute
- **[Roadmap](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Roadmap)** - Future development

---

## 🎯 Why Use This Plugin?

### Security
- **Detect vulnerabilities** before they reach production
- **OWASP Top 10** coverage with real Mathematica examples
- **Taint analysis** tracks untrusted data flows
- **Hardcoded secret detection** prevents credential leaks

### Performance
- **1000× speedups** by detecting AppendTo in loops
- **Compilation opportunities** - Find code that should use Compile[]
- **Pattern optimization** - Avoid catastrophic backtracking
- **Memory leak detection** - Prevent growing definition chains

### Quality
- **162 bug rules** catch runtime errors before they happen
- **247 code smell rules** improve maintainability
- **Complexity metrics** highlight code that needs refactoring
- **Duplicate detection** finds copy-paste code

### Productivity
- **53 Quick Fixes** - One-click automated corrections
- **IDE integration** - Real-time feedback via SonarLint
- **CI/CD ready** - Works with GitHub Actions, GitLab, Jenkins
- **Custom rules** - Define project-specific patterns

---

## 📊 Tier 1 Language Support

This plugin provides **best-in-class** Mathematica support, comparable to major languages:

| Feature | Java | Python | Mathematica |
|---------|------|--------|-------------|
| Rules | 733 | 410 | **529** ✅ |
| AST Parser | ✅ | ✅ | ✅ |
| Symbol Table | ✅ | ✅ | ✅ |
| Type System | ✅ | ✅ | ✅ |
| Data Flow | ✅ | ✅ | ✅ |
| Control Flow | ✅ | ✅ | ✅ |
| Taint Analysis | ✅ | ✅ | ✅ |
| Test Coverage | ✅ | ✅ | ✅ |
| Quick Fixes | ✅ | ✅ | ✅ |
| Custom Rules | ✅ | ✅ | ✅ |

---

## 🏗️ Building from Source

```bash
# Clone the repository
git clone git@github.com:bceverly/sonar-mathematica-plugin.git
cd sonar-mathematica-plugin

# Build
gradle clean build

# JAR file created at:
# build/libs/sonar-mathematica-plugin-*.jar
```

### Requirements
- Java 11+
- Gradle 8.0+
- SonarQube 9.9+ (LTS) or 10.x

---

## 🤝 Contributing

Contributions are welcome! Please see:
- [Contributing Guidelines](CONTRIBUTING.md)
- [Architecture Documentation](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Architecture)
- [Future Rules Ideas](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Future-Rules)

### Adding New Rules

1. Define rule in `*RulesDefinition.java`
2. Implement detector in `*Detector.java`
3. Add tests
4. Update documentation
5. Submit pull request

See [Architecture](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Architecture) for details.

---

## 📝 License

GPL-3.0 License - See [LICENSE](LICENSE) for details.

---

## 📞 Support

- **Documentation**: https://github.com/bceverly/sonar-mathematica-plugin/wiki
- **Issues**: https://github.com/bceverly/sonar-mathematica-plugin/issues
- **Discussions**: https://github.com/bceverly/sonar-mathematica-plugin/discussions

---

## 📈 Statistics

- **Total Rules**: 529
- **Lines of Code**: 50,000+
- **Test Coverage**: Comprehensive detector tests
- **Performance**: Optimized for 12,000+ file codebases
- **Documentation**: 293 KB complete wiki
- **Status**: Tier 1 - Production Ready

---

**Latest Version**: 0.9.7+
**Status**: ✅ Tier 1 Achieved (November 2025)

[View Full Documentation →](https://github.com/bceverly/sonar-mathematica-plugin/wiki)
