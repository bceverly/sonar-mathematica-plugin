# Software Bill of Materials (SBOM)

## Overview

The SonarQube Mathematica Plugin includes a **Software Bill of Materials (SBOM)** with every release, providing complete transparency about the plugin's dependencies, components, and supply chain.

## What is an SBOM?

A Software Bill of Materials (SBOM) is a comprehensive inventory of all components, libraries, and dependencies that make up a software application. Think of it as an "ingredients list" for software.

### Key Components

An SBOM typically includes:

- **Component names and versions** - Exact versions of all dependencies
- **Package URLs (PURLs)** - Standardized identifiers for components
- **Licenses** - License information for each component
- **Dependencies** - Relationships between components
- **Hashes** - Cryptographic checksums for verification
- **Metadata** - Build information, timestamps, and tool details

## Why We Provide an SBOM

### 1. **Security Transparency**

- **Vulnerability Tracking**: Quickly identify if the plugin contains vulnerable dependencies
- **Supply Chain Security**: Understand the complete dependency tree
- **Incident Response**: Rapidly assess impact of newly disclosed vulnerabilities
- **Zero-Day Response**: Know exactly what versions are in use

### 2. **Compliance & Governance**

- **License Compliance**: Verify all licenses are acceptable for your use case
- **Regulatory Requirements**: Meet SBOM requirements (e.g., Executive Order 14028)
- **Audit Trails**: Provide evidence of component tracking
- **Policy Enforcement**: Validate against organizational policies

### 3. **Risk Management**

- **Component Inventory**: Know what's in the software you're deploying
- **Supply Chain Risk**: Assess risk from third-party components
- **Vendor Management**: Understand dependencies on external projects
- **End-of-Life Tracking**: Monitor for deprecated or abandoned components

### 4. **DevSecOps Integration**

- **Automated Scanning**: Feed into vulnerability scanners
- **CI/CD Pipelines**: Automated policy checks
- **Security Tools**: Compatible with dependency scanning tools
- **Dependency Analysis**: Track changes across releases

## SBOM Format

We use **CycloneDX 1.5** in JSON format, an industry-standard SBOM specification.

### Why CycloneDX?

- **Industry Standard**: OWASP-maintained, widely adopted
- **Machine Readable**: Easy to parse and automate
- **Comprehensive**: Supports detailed component metadata
- **Tool Support**: Compatible with major security tools
- **VEX Support**: Vulnerability Exploitability eXchange integration

### Alternative Formats

CycloneDX is compatible with other SBOM standards including:

- **SPDX** (Software Package Data Exchange)
- **SWID** (Software Identification Tags)
- Can be converted using open-source tools

## How the SBOM is Generated

### Automatic Generation

The SBOM is automatically generated during the build process using the **CycloneDX Gradle Plugin**.

```gradle
// build.gradle configuration
plugins {
    id 'org.cyclonedx.bom' version '1.8.2'
}

cyclonedxBom {
    includeConfigs = ['runtimeClasspath']
    outputName = "sonar-mathematica-plugin-${project.version}-sbom"
    outputFormat = 'json'
    includeBomSerialNumber = true
}
```

### Build Integration

```bash
# SBOM is generated as part of the standard build
gradle clean build

# Output location
build/reports/sonar-mathematica-plugin-{version}-sbom.json
```

### What's Included

The SBOM captures all runtime dependencies, including:

- **Direct Dependencies**: Libraries explicitly declared in build.gradle
- **Transitive Dependencies**: Dependencies of dependencies
- **Runtime Classpath**: Everything needed to run the plugin
- **Component Metadata**: Versions, licenses, checksums

### What's Excluded

- **Compile-only Dependencies**: SonarQube API (provided at runtime)
- **Test Dependencies**: Only used during development
- **Development Tools**: Build plugins, linters, etc.

## SBOM Contents

### Structure

```json
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.5",
  "serialNumber": "urn:uuid:...",
  "version": 1,
  "metadata": {
    "timestamp": "2025-11-04T12:40:24Z",
    "tools": [
      {
        "vendor": "CycloneDX",
        "name": "cyclonedx-gradle-plugin",
        "version": "1.8.2"
      }
    ],
    "component": {
      "group": "org.sonar.plugins",
      "name": "sonar-mathematica-plugin",
      "version": "0.9.8",
      "purl": "pkg:maven/org.sonar.plugins/sonar-mathematica-plugin@0.9.8",
      "type": "library"
    }
  },
  "dependencies": [
    {
      "ref": "pkg:maven/org.sonar.plugins/sonar-mathematica-plugin@0.9.8",
      "dependsOn": []
    }
  ]
}
```

### Main Sections

#### 1. **Metadata**
- Build timestamp
- Tool information (CycloneDX plugin version)
- Plugin details (group, name, version)

#### 2. **Components**
- List of all dependencies
- Package URLs (PURLs) for identification
- Licenses (when available)
- Hashes for verification

#### 3. **Dependencies**
- Dependency relationships
- Transitive dependency graph

## How to Use the SBOM

### 1. Download the SBOM

```bash
# Download with each release
curl -L -o sbom.json \
  https://github.com/bceverly/sonar-mathematica-plugin/releases/download/v0.9.8/sonar-mathematica-plugin-0.9.8-sbom.json
```

### 2. Verify Authenticity

```bash
# Check SHA256 hash (from release notes)
shasum -a 256 sonar-mathematica-plugin-0.9.8-sbom.json

# Compare with hash in release notes
```

### 3. View Human-Readable Format

```bash
# Pretty-print JSON
cat sbom.json | jq .

# Extract component list
cat sbom.json | jq '.components[] | {name: .name, version: .version}'
```

### 4. Vulnerability Scanning

#### Using Dependency-Track

[Dependency-Track](https://dependencytrack.org/) is an open-source SBOM analysis platform:

```bash
# Upload to Dependency-Track
curl -X POST "http://dtrack-server/api/v1/bom" \
  -H "X-Api-Key: $API_KEY" \
  -F "project=$PROJECT_UUID" \
  -F "bom=@sonar-mathematica-plugin-0.9.8-sbom.json"
```

#### Using OSS Index

```bash
# Scan with OSS Index (Sonatype)
npm install -g auditjs
auditjs sbom --input sbom.json
```

#### Using Grype

```bash
# Scan with Grype (Anchore)
grype sbom:sbom.json
```

### 5. License Compliance

```bash
# Extract license information
cat sbom.json | jq '.components[] | {name: .name, license: .licenses}'

# Check for specific licenses
cat sbom.json | jq '.components[] | select(.licenses != null)'
```

### 6. Component Inventory

```bash
# List all components
cat sbom.json | jq -r '.components[]? | "\(.name):\(.version)"'

# Count components
cat sbom.json | jq '.components | length'

# Find specific component
cat sbom.json | jq '.components[] | select(.name | contains("slf4j"))'
```

### 7. CI/CD Integration

#### GitHub Actions Example

```yaml
name: SBOM Vulnerability Scan

on:
  release:
    types: [published]

jobs:
  scan-sbom:
    runs-on: ubuntu-latest
    steps:
      - name: Download SBOM
        run: |
          curl -L -o sbom.json ${{ github.event.release.assets[1].browser_download_url }}

      - name: Scan with Grype
        uses: anchore/scan-action@v3
        with:
          sbom: sbom.json
          fail-build: true
          severity-cutoff: high
```

## SBOM Verification

Each release includes SHA256 checksums for both the plugin JAR and SBOM file in the release notes.

### Verification Process

1. **Download both files**
   ```bash
   curl -L -O https://github.com/.../sonar-mathematica-plugin-0.9.8.jar
   curl -L -O https://github.com/.../sonar-mathematica-plugin-0.9.8-sbom.json
   ```

2. **Check hashes**
   ```bash
   shasum -a 256 sonar-mathematica-plugin-0.9.8.jar
   shasum -a 256 sonar-mathematica-plugin-0.9.8-sbom.json
   ```

3. **Compare with release notes**
   - JAR SHA256: `<hash from release notes>`
   - SBOM SHA256: `<hash from release notes>`

## Understanding Dependencies

### Current Dependencies

The Mathematica plugin has minimal runtime dependencies:

- **No external runtime dependencies**: The plugin is self-contained
- **Compile-only dependencies**:
  - SonarQube Plugin API (provided by SonarQube at runtime)
  - SLF4J API (provided by SonarQube at runtime)

This minimal dependency footprint means:
- **Reduced attack surface**: Fewer third-party components
- **Lower vulnerability risk**: Less code to audit
- **Faster updates**: No dependency conflicts
- **Smaller SBOM**: Easy to review

### Dependency Updates

We follow these practices:

1. **Regular Updates**: Dependencies reviewed quarterly
2. **Security Patches**: Applied within 48 hours of disclosure
3. **Version Pinning**: Exact versions specified (no ranges)
4. **Automated Scanning**: Dependabot alerts enabled
5. **Release Notes**: Dependency changes documented

## SBOM Maintenance

### Generation Schedule

- **Every Build**: SBOM generated with each build
- **Every Release**: SBOM published with release artifacts
- **On Demand**: Can be generated locally during development

### Versioning

- SBOM version matches plugin version
- Serial number changes with each generation
- Timestamp reflects generation time

### Updates

- SBOM updated when dependencies change
- Reviewed during security updates
- Validated in CI/CD pipeline

## Tools and Resources

### SBOM Tools

- **[CycloneDX](https://cyclonedx.org/)** - SBOM standard and tooling
- **[Dependency-Track](https://dependencytrack.org/)** - SBOM analysis platform
- **[Grype](https://github.com/anchore/grype)** - Vulnerability scanner
- **[Syft](https://github.com/anchore/syft)** - SBOM generator
- **[OSS Index](https://ossindex.sonatype.org/)** - Vulnerability database

### Standards

- **[CycloneDX Specification](https://cyclonedx.org/specification/overview/)** - Format details
- **[SPDX](https://spdx.dev/)** - Alternative SBOM format
- **[NTIA Guidelines](https://www.ntia.gov/sbom)** - SBOM best practices
- **[CISA SBOM](https://www.cisa.gov/sbom)** - Federal SBOM requirements

### Educational Resources

- **[SBOM Tool Guide](https://www.ntia.gov/files/ntia/publications/sbom_tool_guide-20230818.pdf)** - Tool selection
- **[SBOM at a Glance](https://www.ntia.gov/files/ntia/publications/sbom_at_a_glance_apr2021.pdf)** - Introduction
- **[Executive Order 14028](https://www.whitehouse.gov/briefing-room/presidential-actions/2021/05/12/executive-order-on-improving-the-nations-cybersecurity/)** - Federal requirements

## Frequently Asked Questions

### Do I need to use the SBOM?

No, it's optional. The SBOM is provided for transparency and security-conscious organizations. The plugin works the same whether you use the SBOM or not.

### Can I generate my own SBOM?

Yes! Build from source and the SBOM will be generated automatically:

```bash
gradle clean build
# SBOM at: build/reports/sonar-mathematica-plugin-*-sbom.json
```

### What if a vulnerability is found?

1. We'll be notified through automated scanning
2. Security patch released within 48 hours (critical) or next release (others)
3. Updated SBOM published with patch
4. Security advisory published on GitHub

### How does this help my organization?

- **Compliance**: Meet SBOM requirements (federal, industry)
- **Security**: Track vulnerabilities in dependencies
- **Risk Management**: Understand supply chain risk
- **Procurement**: Validate vendor transparency
- **Audit**: Evidence of software composition tracking

### Is the SBOM signed?

The SBOM is verified through:
- SHA256 checksums in release notes
- GitHub release signatures
- Reproducible builds (same source = same SBOM)

Digital signatures for SBOMs are planned for future releases.

### What about license compliance?

The SBOM includes license information when available. The plugin itself is GPL-3.0, and the compile-only dependencies (SonarQube API, SLF4J) are LGPL-compatible.

## Related Documentation

- [Installation Guide](Installation.md) - Download and verify plugin
- [Security](https://github.com/bceverly/sonar-mathematica-plugin/security) - Security policies
- [Contributing](../CONTRIBUTING.md) - Development guidelines
- [Release Process](https://github.com/bceverly/sonar-mathematica-plugin/wiki/Release-Process) - How releases are made

---

**SBOM Format**: CycloneDX 1.5 JSON
**Generation Tool**: CycloneDX Gradle Plugin 1.8.2
**Update Frequency**: Every release
**Verification**: SHA256 checksums in release notes

For questions about SBOMs, please [open an issue](https://github.com/bceverly/sonar-mathematica-plugin/issues).
