# Changelog

All notable changes to the SonarQube Mathematica Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- NCLOC (Non-Comment Lines of Code) metric calculation
- NCLOC_DATA metric for line-by-line code highlighting in SonarQube UI

## [0.9.7] - 2025-11-03

### Achievement
🎉 **Tier 1 Language Support Achieved** - Plugin now provides comprehensive analysis comparable to Java and Python support in SonarQube.

### Added

#### Security Analysis (56 rules)
- **27 Security Vulnerabilities** including:
  - SQL Injection detection (M:S1001)
  - Cross-Site Scripting (XSS) prevention (M:S1002)
  - Command Injection detection (M:S1003)
  - Path Traversal detection (M:S1004)
  - Hardcoded Credentials detection (M:S1005)
  - XML External Entity (XXE) prevention (M:S1006)
  - Server-Side Request Forgery (SSRF) detection (M:S1007)
  - And 20 more critical security rules

- **29 Security Hotspots** for security review:
  - Weak cryptography detection
  - Authentication verification
  - Certificate validation
  - Sensitive data exposure
  - Insecure random number generation
  - And 24 more security review points

#### Bug Detection (162 rules)
- Division by zero detection
- Infinite loop detection
- Null safety ($Failed, Missing[])
- Array bounds checking
- Resource leak detection
- Type mismatch detection
- Uninitialized variable detection
- Logic error detection

#### Code Quality (247 rules)
- **Performance rules** including:
  - AppendTo in loops detection (1000× speedup opportunity)
  - String concatenation optimization
  - Compilation opportunities
  - Pattern optimization
  - Memory leak prevention

- **Maintainability rules**:
  - Cognitive complexity analysis
  - Cyclomatic complexity analysis
  - Code duplication detection
  - Naming conventions
  - Function length limits
  - Best practices enforcement

#### Advanced Analysis Capabilities
- **Taint Analysis**: Track untrusted data flow through code
- **Symbol Table**: Cross-file analysis with full symbol resolution
- **Type Inference**: Understand variable types through pattern matching
- **Data Flow Analysis**: Track values, nulls, and resources
- **Control Flow Analysis**: Detect unreachable code and infinite loops

#### Developer Productivity
- **53 Quick Fixes**: One-click automated code corrections
- **3 Custom Rule Templates**: Create project-specific patterns
- **IDE Integration**: SonarLint support for real-time feedback

#### Metrics
- Cyclomatic Complexity
- Cognitive Complexity
- Function count
- Statement count
- Lines of code

#### Quality Features
- Copy-Paste Detection (CPD) with custom tokenizer
- Quality Profile with 529 rules enabled by default
- Full SonarQube 9.9+ and 10.x compatibility

#### SCA (Software Composition Analysis)
- Paclet dependency tracking
- Vulnerability detection in dependencies
- License compliance checking

### Performance
- 99.75% speed improvement in rule checking (from 400 minutes to 60 seconds on 12,000 file codebase)
- Optimized regex patterns to prevent catastrophic backtracking
- Character-based comment removal (O(n) performance)
- Rule visitor consolidation using UnifiedRuleVisitor pattern

### Documentation
- Comprehensive Wiki (293 KB, 10,778 lines)
- Complete rule catalog with examples
- Security vulnerability guide with OWASP mapping
- Best practices documentation
- Troubleshooting guide
- FAQ with 50 questions
- CI/CD integration guides (GitHub Actions, GitLab, Jenkins, Azure, CircleCI)

### Changed
- Refactored UnifiedRuleVisitor for better performance
- Consolidated duplicate rule implementations
- Improved error handling and logging
- Enhanced symbol table resolution

### Fixed
- StackOverflow errors in complex pattern matching
- Regex catastrophic backtracking issues
- Memory leaks in rule processing
- File descriptor leaks in metrics calculation

## [0.5.0] - 2024-10-15

### Added
- Initial public release
- Basic Mathematica language support
- 150 initial rules (bugs and code smells)
- Syntax highlighting support
- Basic metrics (complexity, LOC)

### Features
- File extensions: .m, .wl, .wls
- Quality profile with core rules
- SonarQube 9.9 LTS compatibility

## [0.1.0] - 2024-08-01

### Added
- Project initialization
- Basic plugin structure
- Mathematica language definition
- Proof of concept with 10 example rules

---

## Release Categories

### Added
New features and capabilities

### Changed
Changes to existing functionality

### Deprecated
Features that will be removed in future releases

### Removed
Features that have been removed

### Fixed
Bug fixes

### Security
Security-related changes and fixes

---

## Unreleased Features in Development

See [Roadmap](docs/Roadmap.md) and [Future Rules](docs/Future-Rules.md) for upcoming features.

### Planned for v1.1.0
- Advanced notebook (.nb) file parsing
- Package (.wl) structure validation
- Enhanced type inference system
- Performance profiling integration

### Planned for v1.2.0
- Machine learning code analysis
- Quantum computing construct detection
- Advanced visualization validation
- Parallel computing optimization rules

---

## Version Support

| Plugin Version | SonarQube Version | Java Version | Status |
|---------------|-------------------|--------------|---------|
| 0.9.7+ | 9.9 LTS, 10.x | 11+ | ✅ Current |
| 0.5.0 - 0.9.6 | 9.9 LTS | 11+ | ⚠️ Legacy |
| 0.1.0 - 0.4.x | 8.9+ | 11+ | ❌ Unsupported |

---

## Migration Guides

### Upgrading from 0.5.x to 0.9.7

**Breaking Changes:**
- None - fully backward compatible

**New Features:**
- 379 new rules (529 total)
- Security analysis with taint tracking
- Symbol table with cross-file analysis
- Quick fixes (53 automated corrections)

**Recommendations:**
1. Review new security vulnerability findings
2. Enable security hotspot review workflow
3. Configure taint analysis sources/sinks if needed
4. Consider adjusting quality gate to include new metrics

### Upgrading from 0.1.x to 0.9.7

**Major Changes:**
- Plugin key remains: `mathematica`
- Quality profile name remains: `Mathematica Way`
- All previous rules still available

**Action Required:**
1. Backup existing quality profiles
2. Re-run analysis to get new security findings
3. Review and address new vulnerabilities
4. Update CI/CD configurations if using specific rule keys

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for information on how to contribute to this project.

## License

GPL-3.0 - See [LICENSE](LICENSE) for details.
