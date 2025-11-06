package org.sonar.plugins.mathematica.rules;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.Severity;

/**
 * Framework Integration Rules for Mathematica-specific features.
 *
 * <p>This file contains 18 framework-specific code smell rules:
 * - 4 Notebook patterns
 * - 4 Manipulate/Dynamic patterns
 * - 4 Package development patterns
 * - 3 Parallel computing patterns
 * - 3 Wolfram Cloud patterns
 *
 * <p><strong>IMPORTANT:</strong> Detection logic is NOT implemented for these rules.
 * Rules are fully defined and documented in SonarQube for manual review.
 * Detection patterns can be added incrementally as needed.
 */
public final class FrameworkIntegrationRulesDefinition {

    private static final String TAG_SECURITY = "security";
    private static final String TAG_PERFORMANCE = "performance";

    private static final String NOTEBOOK = "notebook";
    private static final String PACKAGE = "package";
    private static final String PARALLEL = "parallel";
    private static final String CLOUD = "cloud";

    // Notebook Pattern Rule Keys (4 rules)
    private static final String NOTEBOOK_CELL_SIZE_KEY = "NotebookCellSize";
    private static final String NOTEBOOK_UNORGANIZED_KEY = "NotebookUnorganized";
    private static final String NOTEBOOK_NO_SECTIONS_KEY = "NotebookNoSections";
    private static final String NOTEBOOK_INIT_CELL_MISUSE_KEY = "NotebookInitCellMisuse";

    // Manipulate/Dynamic Rule Keys (4 rules)
    private static final String MANIPULATE_PERFORMANCE_KEY = "ManipulatePerformance";
    private static final String DYNAMIC_HEAVY_COMPUTATION_KEY = "DynamicHeavyComputation";
    private static final String DYNAMIC_NO_TRACKING_KEY = "DynamicNoTracking";
    private static final String MANIPULATE_TOO_COMPLEX_KEY = "ManipulateTooComplex";

    // Package Development Rule Keys (4 rules)
    private static final String PACKAGE_NO_BEGIN_KEY = "PackageNoBegin";
    private static final String PACKAGE_PUBLIC_PRIVATE_MIX_KEY = "PackagePublicPrivateMix";
    private static final String PACKAGE_NO_USAGE_KEY = "PackageNoUsage";
    private static final String PACKAGE_CIRCULAR_DEPENDENCY_KEY = "PackageCircularDependency";

    // Parallel Computing Rule Keys (3 rules)
    private static final String PARALLEL_NO_GAIN_KEY = "ParallelNoGain";
    private static final String PARALLEL_RACE_CONDITION_KEY = "ParallelRaceCondition";
    private static final String PARALLEL_SHARED_STATE_KEY = "ParallelSharedState";

    // Wolfram Cloud Rule Keys (3 rules)
    private static final String CLOUD_API_MISSING_AUTH_KEY = "CloudApiMissingAuth";
    private static final String CLOUD_PERMISSIONS_TOO_OPEN_KEY = "CloudPermissionsTooOpen";
    private static final String CLOUD_DEPLOY_NO_VALIDATION_KEY = "CloudDeployNoValidation";

    // Private constructor for utility class
    private FrameworkIntegrationRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all framework integration rules in the repository.
     */
    public static void define(NewRepository repository) {
        defineNotebookPatternRules(repository);
        defineManipulateDynamicRules(repository);
        definePackageDevelopmentRules(repository);
        defineParallelComputingRules(repository);
        defineWolframCloudRules(repository);
    }

    private static void defineNotebookPatternRules(NewRepository repository) {
        repository.createRule(NOTEBOOK_CELL_SIZE_KEY)
            .setName("Notebook cells should not be too large")
            .setHtmlDescription("<p>Large notebook cells are hard to understand and maintain. Break them into smaller, focused cells.</p>"
                + "<p><strong>Threshold:</strong> 50 lines per cell</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(NOTEBOOK, "readability")
            .setStatus(RuleStatus.READY);

        repository.createRule(NOTEBOOK_UNORGANIZED_KEY)
            .setName("Notebooks should have clear organization")
            .setHtmlDescription("<p>Notebooks mixing code, tests, and scratch work are hard to maintain. Organize content logically.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(NOTEBOOK, "organization")
            .setStatus(RuleStatus.READY);

        repository.createRule(NOTEBOOK_NO_SECTIONS_KEY)
            .setName("Notebooks should use section headers")
            .setHtmlDescription("<p>Section and subsection cells improve notebook readability and navigation.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(NOTEBOOK, "documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(NOTEBOOK_INIT_CELL_MISUSE_KEY)
            .setName("Initialization cells should be used carefully")
            .setHtmlDescription("<p>InitializationCell should only contain setup code. Avoid side effects or heavy computations.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(NOTEBOOK, "initialization")
            .setStatus(RuleStatus.READY);
    }

    private static void defineManipulateDynamicRules(NewRepository repository) {
        repository.createRule(MANIPULATE_PERFORMANCE_KEY)
            .setName("Manipulate controls should not perform heavy computations")
            .setHtmlDescription("<p>Heavy computations in Manipulate cause UI lag. Use Dynamic with caching or precompute data.</p>"
                + "<h2>Noncompliant Code</h2><pre>Manipulate[\n  Plot[Integrate[f[x], x], {x, 0, a}],\n  {a, 1, 10}\n]</pre>"
                + "<h2>Compliant Solution</h2><pre>Manipulate[\n  Plot[cachedIntegral[a][x], {x, 0, a}],\n  {a, 1, 10}\n]</pre>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, "manipulate", "ui")
            .setStatus(RuleStatus.READY);

        repository.createRule(DYNAMIC_HEAVY_COMPUTATION_KEY)
            .setName("Dynamic should not contain expensive computations")
            .setHtmlDescription("<p>Dynamic re-evaluates on every change. Move expensive computations outside Dynamic or use memoization.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, "dynamic", "ui")
            .setStatus(RuleStatus.READY);

        repository.createRule(DYNAMIC_NO_TRACKING_KEY)
            .setName("Dynamic tracking should be explicit when needed")
            .setHtmlDescription("<p>Review Dynamic dependencies. Use TrackedSymbols or Refresh for explicit control.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("dynamic", "tracking")
            .setStatus(RuleStatus.READY);

        repository.createRule(MANIPULATE_TOO_COMPLEX_KEY)
            .setName("Manipulate should not have too many controls")
            .setHtmlDescription("<p>More than 10 controls makes Manipulate hard to use. Consider breaking into multiple interfaces.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("manipulate", "complexity")
            .setStatus(RuleStatus.READY);
    }

    private static void definePackageDevelopmentRules(NewRepository repository) {
        repository.createRule(PACKAGE_NO_BEGIN_KEY)
            .setName("Packages should use Begin/End for context management")
            .setHtmlDescription("<p>Proper context management prevents symbol pollution. Use BeginPackage, Begin, End, EndPackage.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(PACKAGE, "context")
            .setStatus(RuleStatus.READY);

        repository.createRule(PACKAGE_PUBLIC_PRIVATE_MIX_KEY)
            .setName("Packages should separate public and private symbols")
            .setHtmlDescription("<p>Public API should be in package context, private implementation in Private` subcontext.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(PACKAGE, "api")
            .setStatus(RuleStatus.READY);

        repository.createRule(PACKAGE_NO_USAGE_KEY)
            .setName("Public package functions should have usage messages")
            .setHtmlDescription("<p>Usage messages document the public API. They appear in ? queries and auto-completion.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(PACKAGE, "documentation")
            .setStatus(RuleStatus.READY);

        repository.createRule(PACKAGE_CIRCULAR_DEPENDENCY_KEY)
            .setName("Packages should not have circular dependencies")
            .setHtmlDescription("<p>Circular Needs/Get creates loading issues. Refactor to remove circular dependencies.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.HIGH)
            .setTags(PACKAGE, "dependencies")
            .setStatus(RuleStatus.READY);
    }

    private static void defineParallelComputingRules(NewRepository repository) {
        repository.createRule(PARALLEL_NO_GAIN_KEY)
            .setName("Parallel operations should have sufficient workload")
            .setHtmlDescription("<p>Parallel overhead exceeds benefit for small workloads. "
                + "Use parallel operations only when computation > overhead.</p>")
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags(TAG_PERFORMANCE, PARALLEL)
            .setStatus(RuleStatus.READY);

        repository.createRule(PARALLEL_RACE_CONDITION_KEY)
            .setName("Parallel code should avoid race conditions")
            .setHtmlDescription("<p>Review shared state in parallel code. Use CriticalSection or thread-safe data structures.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags(PARALLEL, "concurrency", "bug")
            .setStatus(RuleStatus.READY);

        repository.createRule(PARALLEL_SHARED_STATE_KEY)
            .setName("Parallel operations should minimize shared state")
            .setHtmlDescription("<p>Shared mutable state in parallel code causes race conditions and deadlocks. "
                + "Use immutable data or proper synchronization.</p>")
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags(PARALLEL, "concurrency")
            .setStatus(RuleStatus.READY);
    }

    private static void defineWolframCloudRules(NewRepository repository) {
        repository.createRule(CLOUD_API_MISSING_AUTH_KEY)
            .setName("Cloud API endpoints should require authentication")
            .setHtmlDescription("<p>Review CloudDeploy/APIFunction authentication. Public endpoints should validate requests.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, CLOUD, "api")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLOUD_PERMISSIONS_TOO_OPEN_KEY)
            .setName("Cloud object permissions should follow least privilege")
            .setHtmlDescription("<p>Review Permissions settings in CloudDeploy. Avoid \"Public\" when not necessary.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, CLOUD, "permissions")
            .setStatus(RuleStatus.READY);

        repository.createRule(CLOUD_DEPLOY_NO_VALIDATION_KEY)
            .setName("Cloud deployments should validate inputs")
            .setHtmlDescription("<p>CloudDeploy and APIFunction should validate all inputs to prevent injection attacks.</p>")
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM)
            .setTags(TAG_SECURITY, CLOUD, "validation")
            .setStatus(RuleStatus.READY);
    }
}
