#!/usr/bin/env python3
"""
Extract all rule definitions from Mathematica SonarQube plugin and create a comprehensive catalog.
This script reads all *RulesDefinition.java files and extracts rule information.
"""

import os
import re
import json
from pathlib import Path
from typing import Dict, List, Any

def extract_rule_key(line: str) -> str:
    """Extract rule key constant from a line."""
    match = re.search(r'createRule\(([A-Z_]+_KEY)\)', line)
    if match:
        return match.group(1)
    return ""

def extract_name(content: str, start_idx: int) -> str:
    """Extract rule name from setName() call."""
    match = re.search(r'\.setName\("([^"]+)"\)', content[start_idx:start_idx+500])
    if match:
        return match.group(1)
    return ""

def extract_html_description(content: str, start_idx: int) -> str:
    """Extract HTML description from setHtmlDescription() call."""
    # Find the start of setHtmlDescription
    match = re.search(r'\.setHtmlDescription\(\s*"', content[start_idx:])
    if not match:
        return ""

    desc_start = start_idx + match.end() - 1
    depth = 0
    in_string = True
    i = desc_start
    desc_parts = []

    while i < len(content):
        char = content[i]

        if char == '"' and (i == 0 or content[i-1] != '\\'):
            if not in_string:
                in_string = True
            else:
                # Check if this ends the method call
                j = i + 1
                while j < len(content) and content[j] in ' \n\t':
                    j += 1
                if j < len(content) and content[j] == ')':
                    break
                in_string = False
        elif in_string and char != '"':
            if char == '\\' and i + 1 < len(content):
                # Handle escape sequences
                next_char = content[i + 1]
                if next_char == 'n':
                    desc_parts.append('\n')
                    i += 1
                elif next_char == 't':
                    desc_parts.append('\t')
                    i += 1
                elif next_char == '"':
                    desc_parts.append('"')
                    i += 1
                else:
                    desc_parts.append(char)
            elif char == '+' and not in_string:
                pass  # String concatenation
            else:
                desc_parts.append(char)

        i += 1
        if i - start_idx > 10000:  # Safety limit
            break

    return ''.join(desc_parts).strip()

def extract_severity(content: str, start_idx: int) -> str:
    """Extract severity from addDefaultImpact() call."""
    match = re.search(r'Severity\.(HIGH|MEDIUM|LOW)', content[start_idx:start_idx+1000])
    if match:
        return match.group(1)
    return "MEDIUM"

def extract_software_quality(content: str, start_idx: int) -> str:
    """Extract software quality from addDefaultImpact() call."""
    match = re.search(r'SoftwareQuality\.(SECURITY|RELIABILITY|MAINTAINABILITY)', content[start_idx:start_idx+1000])
    if match:
        return match.group(1)
    return "MAINTAINABILITY"

def extract_tags(content: str, start_idx: int) -> List[str]:
    """Extract tags from setTags() call."""
    match = re.search(r'\.setTags\(([^)]+)\)', content[start_idx:start_idx+1000])
    if match:
        tags_str = match.group(1)
        # Extract all quoted strings
        tags = re.findall(r'"([^"]+)"', tags_str)
        # Also extract TAG_ constants
        tag_constants = re.findall(r'TAG_([A-Z_]+)', tags_str)
        return tags + [t.lower() for t in tag_constants]
    return []

def extract_rule_type(content: str, start_idx: int) -> str:
    """Extract rule type if explicitly set."""
    match = re.search(r'\.setType\(.*?RuleType\.(SECURITY_HOTSPOT|BUG|VULNERABILITY|CODE_SMELL)\)', content[start_idx:start_idx+500])
    if match:
        return match.group(1)
    return ""

def determine_rule_type(software_quality: str, explicit_type: str, tags: List[str]) -> str:
    """Determine rule type based on quality, explicit type, and tags."""
    if explicit_type == "SECURITY_HOTSPOT":
        return "Security Hotspot"
    elif software_quality == "SECURITY":
        return "Vulnerability"
    elif software_quality == "RELIABILITY":
        return "Bug"
    else:
        return "Code Smell"

def parse_java_file(file_path: Path) -> List[Dict[str, Any]]:
    """Parse a Java file and extract all rules."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    rules = []

    # Find all createRule() calls
    pattern = r'repository\.createRule\('
    for match in re.finditer(pattern, content):
        start_idx = match.start()

        # Extract rule information
        key = extract_rule_key(content[start_idx:start_idx+100])
        if not key:
            continue

        name = extract_name(content, start_idx)
        description = extract_html_description(content, start_idx)
        severity = extract_severity(content, start_idx)
        quality = extract_software_quality(content, start_idx)
        tags = extract_tags(content, start_idx)
        explicit_type = extract_rule_type(content, start_idx)
        rule_type = determine_rule_type(quality, explicit_type, tags)

        rule = {
            "key": key,
            "name": name,
            "description": description,
            "severity": severity,
            "softwareQuality": quality,
            "type": rule_type,
            "tags": tags
        }

        rules.append(rule)

    return rules

def main():
    # Find all *RulesDefinition.java files
    rules_dir = Path("/Users/bceverly/dev/sonar-mathematica-plugin/src/main/java/org/sonar/plugins/mathematica/rules")
    java_files = list(rules_dir.glob("*RulesDefinition.java"))

    print(f"Found {len(java_files)} RulesDefinition files")

    all_rules = []

    for java_file in sorted(java_files):
        print(f"Processing {java_file.name}...")
        rules = parse_java_file(java_file)
        all_rules.extend(rules)
        print(f"  Extracted {len(rules)} rules")

    # Categorize rules
    categorized = {
        "securityVulnerabilities": [],
        "securityHotspots": [],
        "bugs": [],
        "codeSmells": []
    }

    for rule in all_rules:
        if rule["type"] == "Vulnerability":
            categorized["securityVulnerabilities"].append(rule)
        elif rule["type"] == "Security Hotspot":
            categorized["securityHotspots"].append(rule)
        elif rule["type"] == "Bug":
            categorized["bugs"].append(rule)
        else:
            categorized["codeSmells"].append(rule)

    # Create catalog
    catalog = {
        "metadata": {
            "pluginName": "SonarMathematica",
            "version": "1.0.0",
            "totalRules": len(all_rules),
            "categories": {
                "securityVulnerabilities": len(categorized["securityVulnerabilities"]),
                "securityHotspots": len(categorized["securityHotspots"]),
                "bugs": len(categorized["bugs"]),
                "codeSmells": len(categorized["codeSmells"])
            }
        },
        "rules": categorized
    }

    # Write catalog
    output_path = Path("/Users/bceverly/dev/sonar-mathematica-plugin/docs/rule-catalog.json")
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(catalog, f, indent=2, ensure_ascii=False)

    print(f"\n{'='*60}")
    print("RULE EXTRACTION COMPLETE")
    print(f"{'='*60}")
    print(f"Total rules extracted: {len(all_rules)}")
    print(f"\nBreakdown by category:")
    print(f"  Security Vulnerabilities: {len(categorized['securityVulnerabilities'])}")
    print(f"  Security Hotspots: {len(categorized['securityHotspots'])}")
    print(f"  Bugs: {len(categorized['bugs'])}")
    print(f"  Code Smells: {len(categorized['codeSmells'])}")
    print(f"\nCatalog written to: {output_path}")
    print(f"{'='*60}")

if __name__ == "__main__":
    main()
