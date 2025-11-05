package org.sonar.plugins.mathematica.sca;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses PacletInfo.wl files to extract paclet dependencies.
 *
 * Supports multiple PacletInfo formats:
 * - Modern format: Paclet[Name -> "Foo", Version -> "1.0", Dependencies -> {...}]
 * - Legacy format: BeginPackage with dependency lists
 */
public class PacletDependencyParser {

    private static final Logger LOG = LoggerFactory.getLogger(PacletDependencyParser.class);

    // Pattern to match Dependencies section in PacletInfo.wl
    // Dependencies -> {"PacletName" -> "1.0+", "OtherPaclet" -> "2.0+"}
    private static final Pattern DEPENDENCIES_SECTION = Pattern.compile(
        "Dependencies\\s*+->\\s*+\\{([^}]+)\\}",
        Pattern.DOTALL
    );

    // Pattern to match individual dependency entries
    // "PacletName" -> "1.0+" or "PacletName" -> "1.0"
    private static final Pattern DEPENDENCY_ENTRY = Pattern.compile(
        "\"([^\"]+)\"\\s*+->\\s*+\"([^\"]+)\"");

    // Pattern for PacletManager` dependencies (legacy format)
    private static final Pattern NEEDS_PATTERN = Pattern.compile(
        "Needs\\[\"([^\"]+)`\"\\]"
    );

    /**
     * Parse PacletInfo.wl content and extract all dependencies.
     */
    public List<PacletDependency> parsePacletInfo(String content) {
        List<PacletDependency> dependencies = new ArrayList<>();

        try {
            // Parse modern format: Dependencies -> {...}
            dependencies.addAll(parseModernFormat(content));

            // Parse legacy format: Needs[...]
            dependencies.addAll(parseLegacyFormat(content));

        } catch (Exception e) {
            LOG.error("Error parsing PacletInfo.wl: {}", e.getMessage());
        }

        return dependencies;
    }

    /**
     * Parse modern PacletInfo.wl format with Dependencies section.
     */
    private List<PacletDependency> parseModernFormat(String content) {
        List<PacletDependency> dependencies = new ArrayList<>();

        Matcher sectionMatcher = DEPENDENCIES_SECTION.matcher(content);

        while (sectionMatcher.find()) {
            String dependenciesBlock = sectionMatcher.group(1);
            int lineNumber = calculateLineNumber(content, sectionMatcher.start());

            // Extract individual dependency entries
            Matcher entryMatcher = DEPENDENCY_ENTRY.matcher(dependenciesBlock);

            while (entryMatcher.find()) {
                String name = entryMatcher.group(1);
                String version = entryMatcher.group(2);

                dependencies.add(new PacletDependency(name, version, lineNumber));
            }
        }

        return dependencies;
    }

    /**
     * Parse legacy format with Needs["Package`"] declarations.
     */
    private List<PacletDependency> parseLegacyFormat(String content) {
        List<PacletDependency> dependencies = new ArrayList<>();

        Matcher matcher = NEEDS_PATTERN.matcher(content);

        while (matcher.find()) {
            String packageName = matcher.group(1);
            int lineNumber = calculateLineNumber(content, matcher.start());

            // Remove trailing backtick from package name
            if (packageName.endsWith("`")) {
                packageName = packageName.substring(0, packageName.length() - 1);
            }

            // No version info for Needs[] format - use "unknown"
            dependencies.add(new PacletDependency(packageName, "unknown", lineNumber));
        }

        return dependencies;
    }

    /**
     * Calculate line number from string offset.
     */
    private int calculateLineNumber(String content, int offset) {
        int lineNumber = 1;
        for (int i = 0; i < offset && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lineNumber++;
            }
        }
        return lineNumber;
    }
}
