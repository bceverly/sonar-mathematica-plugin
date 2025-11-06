package org.sonar.plugins.mathematica.rules;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.Severity;

/**
 * Resource Management Rules for stream/file handling and memory management.
 *
 * <p>This file contains 7 resource management bug rules:
 * - 4 Stream/File Management rules
 * - 3 Memory Management rules
 *
 * <p><strong>IMPORTANT:</strong> Detection logic is NOT implemented for these rules.
 * Rules are fully defined and documented in SonarQube for manual review.
 * Detection patterns can be added incrementally as needed.
 */
public final class ResourceManagementRulesDefinition {

    private static final String TAG_RELIABILITY = "reliability";
    private static final String TAG_PERFORMANCE = "performance";

    private static final String RESOURCES = "resources";
    private static final String MEMORY = "memory";

    // Stream/File Management Rule Keys (4 rules)
    private static final String STREAM_NOT_CLOSED_KEY = "StreamNotClosed";
    private static final String FILE_HANDLE_LEAK_KEY = "FileHandleLeak";
    private static final String CLOSE_IN_FINALLY_MISSING_KEY = "CloseInFinallyMissing";
    private static final String STREAM_REOPEN_ATTEMPT_KEY = "StreamReopenAttempt";

    // Memory Management Rule Keys (3 rules)
    private static final String DYNAMIC_MEMORY_LEAK_KEY = "DynamicMemoryLeak";
    private static final String LARGE_DATA_IN_NOTEBOOK_KEY = "LargeDataInNotebook";
    private static final String NO_CLEAR_AFTER_USE_KEY = "NoClearAfterUse";

    // Private constructor for utility class
    private ResourceManagementRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all resource management rules in the repository.
     */
    public static void define(NewRepository repository) {
        defineStreamFileManagementRules(repository);
        defineMemoryManagementRules(repository);
    }

    private static void defineStreamFileManagementRules(NewRepository repository) {
        repository.createRule(STREAM_NOT_CLOSED_KEY)
            .setName("Streams should be closed after use")
            .setHtmlDescription("<p>Unclosed streams leak file descriptors and can cause resource exhaustion.</p>"
                + "<h2>Noncompliant Code</h2><pre>stream = OpenRead[\"file.txt\"];\ndata = ReadList[stream];\n(* stream never closed *)</pre>"
                + "<h2>Compliant Solution</h2><pre>stream = OpenRead[\"file.txt\"];\ndata = ReadList[stream];\nClose[stream];</pre>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, RESOURCES, "file-io")
            .setStatus(RuleStatus.READY);

        repository.createRule(FILE_HANDLE_LEAK_KEY)
            .setName("File handles should be properly released")
            .setHtmlDescription("<p>File handles must be released even if errors occur. Consider using Block with Close in cleanup.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, RESOURCES, "file-io")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLOSE_IN_FINALLY_MISSING_KEY)
            .setName("Resource cleanup should handle errors")
            .setHtmlDescription("<p>Close operations should be in error handling blocks to ensure cleanup even when operations fail.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, RESOURCES, "error-handling")
            .setStatus(RuleStatus.READY);

        repository.createRule(STREAM_REOPEN_ATTEMPT_KEY)
            .setName("Closed streams should not be reused")
            .setHtmlDescription("<p>Attempting to read from or write to closed streams causes errors. Open a new stream instead.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags(TAG_RELIABILITY, RESOURCES, "bug")
            .setStatus(RuleStatus.READY);
    }

    private static void defineMemoryManagementRules(NewRepository repository) {
        repository.createRule(DYNAMIC_MEMORY_LEAK_KEY)
            .setName("Dynamic expressions should not cause memory leaks")
            .setHtmlDescription("<p>Dynamic that continuously grows data structures causes memory leaks. "
                + "Clear old values or use bounded buffers.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(TAG_RELIABILITY, MEMORY, "dynamic")
            .setStatus(RuleStatus.READY);

        repository.createRule(LARGE_DATA_IN_NOTEBOOK_KEY)
            .setName("Notebooks should not store large data structures")
            .setHtmlDescription("<p>Large arrays or images in notebook variables consume memory. Save to files and load as needed.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, MEMORY, "notebook")
            .setStatus(RuleStatus.READY);

        repository.createRule(NO_CLEAR_AFTER_USE_KEY)
            .setName("Large variables should be cleared after use")
            .setHtmlDescription("<p>Variables holding large data should be explicitly cleared with Unset or ClearAll "
                + "when no longer needed.</p>"
                + "<h2>Example</h2><pre>largeMatrix = RandomReal[1, {10000, 10000}];\n"
                + "(* ... use matrix ... *)\nClear[largeMatrix]; (* Free memory *)</pre>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_PERFORMANCE, MEMORY)
            .setStatus(RuleStatus.READY);
    }
}
