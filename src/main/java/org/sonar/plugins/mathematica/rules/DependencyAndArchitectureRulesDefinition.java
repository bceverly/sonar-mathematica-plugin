package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CIRCULAR_PACKAGE_DEPENDENCY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.COMMENTED_OUT_PACKAGE_LOAD_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CONDITIONAL_PACKAGE_LOAD_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.CYCLIC_CALL_BETWEEN_PACKAGES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DEAD_PACKAGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DEPRECATED_API_STILL_USED_INTERNALLY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DIAMOND_DEPENDENCY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.DUPLICATE_SYMBOL_DEFINITION_ACROSS_PACKAGES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.FUNCTION_ONLY_CALLED_ONCE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.GOD_PACKAGE_TOO_MANY_DEPENDENCIES_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.IMPLEMENTATION_WITHOUT_TESTS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCOMPLETE_PUBLIC_API_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCONSISTENT_PACKAGE_NAMING_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INCONSISTENT_PARAMETER_NAMES_ACROSS_OVERLOADS_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INTERNAL_API_USED_LIKE_PUBLIC_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.INTERNAL_IMPLEMENTATION_EXPOSED_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.LAYER_VIOLATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_PACKAGE_DOCUMENTATION_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.MISSING_PACKAGE_IMPORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.ORPHANED_TEST_FILE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.OVER_ABSTRACTED_API_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKAGE_DEPENDS_ON_APPLICATION_CODE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKAGE_EXPORTS_TOO_LITTLE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKAGE_EXPORTS_TOO_MUCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKAGE_LOADED_BUT_NOT_LISTED_IN_METADATA_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKAGE_TOO_LARGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKAGE_TOO_SMALL_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PACKAGE_VERSION_MISMATCH_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PRIVATE_SYMBOL_USED_EXTERNALLY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PUBLIC_API_CHANGED_WITHOUT_VERSION_BUMP_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PUBLIC_API_NOT_IN_PACKAGE_CONTEXT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PUBLIC_EXPORT_MISSING_USAGE_MESSAGE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.PUBLIC_FUNCTION_WITH_IMPLEMENTATION_DETAILS_IN_NAME_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.SYMBOL_REDEFINITION_AFTER_IMPORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_DEAD_CODE;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_UNUSED;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TEST_FUNCTION_IN_PRODUCTION_CODE_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_10MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_15MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_30MIN;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TRANSITIVE_DEPENDENCY_COULD_BE_DIRECT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNSTABLE_DEPENDENCY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_EXPORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_PACKAGE_IMPORT_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.UNUSED_PUBLIC_FUNCTION_KEY;

/**
 * Dependency and Architecture rule definitions.
 * Covers package dependencies, architecture violations, and layering rules.
 * Extracted from MathematicaRulesDefinition for maintainability.
 */
final class DependencyAndArchitectureRulesDefinition {

    private DependencyAndArchitectureRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all rules in this group.
     */
    static void defineRules(NewRepository repository) {
        defineDependencyRules(repository);
        defineDependencyRulesContinued(repository);
        defineArchitectureRules(repository);
        defineArchitectureRulesContinued(repository);
        defineModularityRules(repository);
        defineModularityRulesContinued(repository);
    }

    /**
     * CHUNK 5 RULE DEFINITIONS (Items 211-250 from ROADMAP_325.md) (Part 1) (13 rules)
     */
    private static void defineDependencyRules(NewRepository repository) {
        // ===== CHUNK 5 RULE DEFINITIONS (Items 211-250 from ROADMAP_325.md) =====

        // Dependency & Architecture Rules (Items 211-230)

        NewRule rule271 = repository.createRule(CIRCULAR_PACKAGE_DEPENDENCY_KEY)
            .setName("Circular package dependency causes load order issues")
            .setHtmlDescription(
                "<p>Circular dependencies between packages create load order issues and prevent clean separation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* A.m *)\n"
                + "BeginPackage[\"A`\"];\n"
                + "Needs[\"B`\"];  (* B also needs A - circular! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Break cycle by extracting shared code to C.m *)\n"
                + "BeginPackage[\"A`\"];\n"
                + "Needs[\"C`\"];</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("architecture", "circular-dependency");

            rule271.setDebtRemediationFunction(rule271.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        NewRule rule272 = repository.createRule(UNUSED_PACKAGE_IMPORT_KEY)
            .setName("Unused package import should be removed")
            .setHtmlDescription(
                "<p>Importing packages that are never used creates unnecessary dependencies.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Needs[\"MyPackage`\"];  (* No symbols from MyPackage used *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove unused import *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, "dependency");

            rule272.setDebtRemediationFunction(rule272.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule273 = repository.createRule(MISSING_PACKAGE_IMPORT_KEY)
            .setName("Using symbol from package without Needs may fail in fresh kernel")
            .setHtmlDescription(
                "<p>Using symbols from other packages without explicit Needs[] may fail in a fresh kernel.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = MyPackage`MyFunction[x];  (* No Needs[\"MyPackage`\"] *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Needs[\"MyPackage`\"];\n"
                + "result = MyPackage`MyFunction[x];</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("missing-import", "runtime-error");

            rule273.setDebtRemediationFunction(rule273.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule274 = repository.createRule(TRANSITIVE_DEPENDENCY_COULD_BE_DIRECT_KEY)
            .setName("Relying on transitive dependency is fragile")
            .setHtmlDescription(
                "<p>Using symbols from packages imported transitively creates fragile dependencies.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Needs[\"A`\"];  (* A needs B, using B's symbols directly *)\n"
                + "B`MyFunction[];</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Needs[\"A`\"];\n"
                + "Needs[\"B`\"];  (* Explicit dependency *)\n"
                + "B`MyFunction[];</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("dependency", "fragile");

            rule274.setDebtRemediationFunction(rule274.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule275 = repository.createRule(DIAMOND_DEPENDENCY_KEY)
            .setName("Diamond dependency pattern may cause version conflicts")
            .setHtmlDescription(
                "<p>Diamond dependencies (A depends on B and C, both depend on D) can cause version conflicts.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* A.m depends on B and C, both depend on different versions of D *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Coordinate dependency versions or refactor to avoid diamond *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("architecture", "dependency");

            rule275.setDebtRemediationFunction(rule275.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule276 = repository.createRule(GOD_PACKAGE_TOO_MANY_DEPENDENCIES_KEY)
            .setName("Package with too many dependencies (>10) has high coupling")
            .setHtmlDescription(
                "<p>Packages depending on more than 10 other packages are highly coupled and hard to maintain.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];\n"
                + "Needs[\"A`\"]; Needs[\"B`\"]; Needs[\"C`\"];\n"
                + "(* ... 8 more Needs calls *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Split package or reduce dependencies *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("architecture", "coupling");

            rule276.setDebtRemediationFunction(rule276.debtRemediationFunctions().constantPerIssue("5min"));

    }

    private static void defineDependencyRulesContinued(NewRepository repository) {
        NewRule rule277 = repository.createRule(PACKAGE_DEPENDS_ON_APPLICATION_CODE_KEY)
            .setName("Library package should not depend on application-specific code")
            .setHtmlDescription(
                "<p>Library packages depending on application code violate dependency direction.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* MyLibrary.m *)\n"
                + "Needs[\"MyApp`\"];  (* Library depends on application! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Application depends on library, not vice versa *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("architecture", "dependency-direction");

            rule277.setDebtRemediationFunction(rule277.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule278 = repository.createRule(CYCLIC_CALL_BETWEEN_PACKAGES_KEY)
            .setName("Cyclic function calls between packages indicate tight coupling")
            .setHtmlDescription(
                "<p>Package A calling Package B which calls back to Package A indicates tight coupling.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* A calls B`Func which calls back A`Func2 *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Extract shared logic to a third package *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("architecture", "coupling");

            rule278.setDebtRemediationFunction(rule278.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule279 = repository.createRule(LAYER_VIOLATION_KEY)
            .setName("Lower layer depending on higher layer violates architecture")
            .setHtmlDescription(
                "<p>Architectural layers should have unidirectional dependencies (lower layers should not depend on higher layers).</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* DataLayer depends on UILayer - wrong direction! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* UILayer depends on DataLayer *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("architecture", "layering");

            rule279.setDebtRemediationFunction(rule279.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule280 = repository.createRule(UNSTABLE_DEPENDENCY_KEY)
            .setName("Stable package depending on unstable package causes ripple effects")
            .setHtmlDescription(
                "<p>Stable packages should not depend on frequently changing (unstable) packages.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* CoreLibrary depends on ExperimentalFeatures *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Invert dependency or stabilize the unstable package *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("architecture", "stability");

            rule280.setDebtRemediationFunction(rule280.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule281 = repository.createRule(PACKAGE_TOO_LARGE_KEY)
            .setName("Package with more than 3000 lines should be split")
            .setHtmlDescription(
                "<p>Very large packages are hard to maintain and should be split into smaller modules.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* MyPackage.m with 4000 lines *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Split into MyPackageCore.m, MyPackageUtils.m, etc. *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("size", "maintainability");

            rule281.setDebtRemediationFunction(rule281.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule282 = repository.createRule(PACKAGE_TOO_SMALL_KEY)
            .setName("Package with fewer than 50 lines may be over-modularized")
            .setHtmlDescription(
                "<p>Very small packages may indicate over-modularization.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* MyTinyPackage.m with 20 lines *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Consider merging with related package *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("size", "over-modularization");

            rule282.setDebtRemediationFunction(rule282.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule283 = repository.createRule(INCONSISTENT_PACKAGE_NAMING_KEY)
            .setName("Package names should follow consistent naming convention")
            .setHtmlDescription(
                "<p>Inconsistent package naming reduces discoverability and maintainability.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"mypackage`\"];  (* lowercase *)\n"
                + "BeginPackage[\"AnotherPkg`\"];  (* mixed *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];  (* consistent PascalCase *)\n"
                + "BeginPackage[\"AnotherPackage`\"];</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("naming", "consistency");

            rule283.setDebtRemediationFunction(rule283.debtRemediationFunctions().constantPerIssue("2min"));
    }

    /**
     * CHUNK 5 RULE DEFINITIONS (Items 211-250 from ROADMAP_325.md) (Part 2) (13 rules)
     */
    private static void defineArchitectureRules(NewRepository repository) {

        NewRule rule284 = repository.createRule(PACKAGE_EXPORTS_TOO_MUCH_KEY)
            .setName("Package exporting more than 50 symbols has poor cohesion")
            .setHtmlDescription(
                "<p>Packages with too many public symbols may lack cohesion and should be split.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\", {\"Func1\", \"Func2\", ... \"Func60\"}];</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Split into multiple focused packages *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("api", "cohesion");

            rule284.setDebtRemediationFunction(rule284.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule285 = repository.createRule(PACKAGE_EXPORTS_TOO_LITTLE_KEY)
            .setName("Package exporting 0-1 symbols may have questionable design")
            .setHtmlDescription(
                "<p>Packages with very few exports may not justify being a separate package.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\", {\"OnlyOneFunction\"}];</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Consider merging with another package or adding more API *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("api", "design");

            rule285.setDebtRemediationFunction(rule285.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule286 = repository.createRule(INCOMPLETE_PUBLIC_API_KEY)
            .setName("Public function relying on private function breaks encapsulation")
            .setHtmlDescription(
                "<p>Public functions returning or using private symbols break encapsulation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>PublicFunc[] := Private`HelperFunc[];  (* Returns private symbol *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>PublicFunc[] := Module[{result}, result = Private`HelperFunc[]; result];</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("encapsulation", "api");

            rule286.setDebtRemediationFunction(rule286.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule287 = repository.createRule(PRIVATE_SYMBOL_USED_EXTERNALLY_KEY)
            .setName("Private` symbol used from another package breaks encapsulation")
            .setHtmlDescription(
                "<p>Using symbols from another package's Private` context breaks encapsulation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>result = MyPackage`Private`InternalFunc[];  (* Accessing private! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>result = MyPackage`PublicFunc[];  (* Use public API *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("encapsulation", "private-access");

            rule287.setDebtRemediationFunction(rule287.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule288 = repository.createRule(INTERNAL_IMPLEMENTATION_EXPOSED_KEY)
            .setName("Internal` symbols used from outside are unstable API")
            .setHtmlDescription(
                "<p>Using Internal` symbols from outside the defining package couples to unstable implementation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>MyPackage`Internal`ExperimentalFunc[];</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Use public API or request feature be made public *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM)
            .setTags("api", "stability");

            rule288.setDebtRemediationFunction(rule288.debtRemediationFunctions().constantPerIssue(TIME_15MIN));

        NewRule rule289 = repository.createRule(MISSING_PACKAGE_DOCUMENTATION_KEY)
            .setName("Package without usage message reduces discoverability")
            .setHtmlDescription(
                "<p>Packages should have top-level documentation for discoverability.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];\n"
                + "(* No package-level documentation *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];\n"
                + "MyPackage::usage = \"MyPackage provides utilities for...\";</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation", "discoverability");

            rule289.setDebtRemediationFunction(rule289.debtRemediationFunctions().constantPerIssue("2min"));

    }

    private static void defineArchitectureRulesContinued(NewRepository repository) {
        NewRule rule290 = repository.createRule(PUBLIC_API_CHANGED_WITHOUT_VERSION_BUMP_KEY)
            .setName("Breaking changes to public API should bump version")
            .setHtmlDescription(
                "<p>Breaking changes to public symbols require version bump for semantic versioning.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* Changed MyFunc[x_] to MyFunc[x_, y_] without version bump *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Increment major version when breaking changes occur *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("versioning", "api");

            rule290.setDebtRemediationFunction(rule290.debtRemediationFunctions().constantPerIssue("5min"));

        // Unused Export & Dead Code (Items 231-245)

        NewRule rule291 = repository.createRule(UNUSED_PUBLIC_FUNCTION_KEY)
            .setName("Public function never called from outside may be dead code")
            .setHtmlDescription(
                "<p>Public functions never called from outside the package may be over-engineering or dead code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>PublicButUnused[] := ...;  (* Never called externally *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Make private or remove if truly unused *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule291.setDebtRemediationFunction(rule291.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule292 = repository.createRule(UNUSED_EXPORT_KEY)
            .setName("Symbol exported but never imported anywhere")
            .setHtmlDescription(
                "<p>Symbols listed in package exports but never imported expand API surface unnecessarily.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\", {\"UnusedFunc\"}];  (* Never imported *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove from exports if truly unused *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, "api");

            rule292.setDebtRemediationFunction(rule292.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule293 = repository.createRule(DEAD_PACKAGE_KEY)
            .setName("Package never imported by anyone is dead code")
            .setHtmlDescription(
                "<p>Packages that are never imported anywhere are dead code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* MyUnusedPackage.m - never has Needs[\"MyUnusedPackage`\"] anywhere *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove dead package or add usage *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags(TAG_UNUSED, TAG_DEAD_CODE);

            rule293.setDebtRemediationFunction(rule293.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule294 = repository.createRule(FUNCTION_ONLY_CALLED_ONCE_KEY)
            .setName("Function called from exactly one place should be inlined")
            .setHtmlDescription(
                "<p>Functions called from only one location add unnecessary indirection.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Helper[] := ...;  (* Called once *)\n"
                + "Main[] := ... Helper[] ...;</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Main[] := ... (* inline Helper logic *) ...;</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("abstraction", "yagni");

            rule294.setDebtRemediationFunction(rule294.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule295 = repository.createRule(OVER_ABSTRACTED_API_KEY)
            .setName("API with single implementation violates YAGNI")
            .setHtmlDescription(
                "<p>Creating abstract interfaces with only one implementation violates YAGNI.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>ProcessorInterface[] := ...;\n"
                + "ConcreteProcessor[] := ...;  (* Only implementation *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Use concrete implementation directly until 2nd impl needed *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("abstraction", "yagni");

            rule295.setDebtRemediationFunction(rule295.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule296 = repository.createRule(ORPHANED_TEST_FILE_KEY)
            .setName("Test file for non-existent implementation")
            .setHtmlDescription(
                "<p>Test files without corresponding implementation files are stale tests.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* MyFunctionTest.m exists but MyFunction.m doesn't *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove orphaned test or restore implementation *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("testing", "orphaned");

            rule296.setDebtRemediationFunction(rule296.debtRemediationFunctions().constantPerIssue("5min"));
    }

    /**
     * CHUNK 5 RULE DEFINITIONS (Items 211-250 from ROADMAP_325.md) (Part 3) (14 rules)
     */
    private static void defineModularityRules(NewRepository repository) {

        NewRule rule297 = repository.createRule(IMPLEMENTATION_WITHOUT_TESTS_KEY)
            .setName("Implementation file without corresponding test file")
            .setHtmlDescription(
                "<p>Implementation files should have corresponding test files for test coverage.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* MyFunction.m exists but MyFunctionTest.m doesn't *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Create MyFunctionTest.m with tests *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("testing", "coverage");

            rule297.setDebtRemediationFunction(rule297.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule298 = repository.createRule(DEPRECATED_API_STILL_USED_INTERNALLY_KEY)
            .setName("Deprecated function still called from same package should migrate")
            .setHtmlDescription(
                "<p>Deprecated functions should not be used even within the defining package.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>OldFunc::deprecated = \"Use NewFunc instead\";\n"
                + "InternalFunc[] := ... OldFunc[] ...;  (* Still using deprecated! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>InternalFunc[] := ... NewFunc[] ...;  (* Migrated *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("deprecated", "migration");

            rule298.setDebtRemediationFunction(rule298.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule299 = repository.createRule(INTERNAL_API_USED_LIKE_PUBLIC_KEY)
            .setName("Internal` symbol called from multiple packages should be public or private")
            .setHtmlDescription(
                "<p>Internal symbols used from multiple packages should be made public or truly private.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* PackageA`Internal`Func used by PackageB and PackageC *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Make it public: PackageA`SharedFunc *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.LOW)
            .setTags("api", "encapsulation");

            rule299.setDebtRemediationFunction(rule299.debtRemediationFunctions().constantPerIssue(TIME_10MIN));

        NewRule rule300 = repository.createRule(COMMENTED_OUT_PACKAGE_LOAD_KEY)
            .setName("Commented out Needs[] is dead dependency or TODO")
            .setHtmlDescription(
                "<p>Commented-out package loads should be removed or uncommented.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* Needs[\"MyPackage`\"] *)  (* Dead or TODO? *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Remove if dead, uncomment if needed *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("commented-code", "dependency");

            rule300.setDebtRemediationFunction(rule300.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule301 = repository.createRule(CONDITIONAL_PACKAGE_LOAD_KEY)
            .setName("Conditional Needs[] creates fragile dependency")
            .setHtmlDescription(
                "<p>Loading packages conditionally makes dependencies unclear and fragile.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>If[condition, Needs[\"MyPackage`\"]];</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>Needs[\"MyPackage`\"];  (* Unconditional *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("dependency", "fragile");

            rule301.setDebtRemediationFunction(rule301.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule302 = repository.createRule(PACKAGE_LOADED_BUT_NOT_LISTED_IN_METADATA_KEY)
            .setName("Needs[] not reflected in PacletInfo.m is incomplete metadata")
            .setHtmlDescription(
                "<p>Package dependencies should be reflected in PacletInfo.m for completeness.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Needs[\"MyPackage`\"];  (* Not in PacletInfo.m dependencies *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Add \"MyPackage\" to PacletInfo.m Extensions list *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("metadata", "dependency");

            rule302.setDebtRemediationFunction(rule302.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule303 = repository.createRule(DUPLICATE_SYMBOL_DEFINITION_ACROSS_PACKAGES_KEY)
            .setName("Same symbol defined in multiple packages causes conflict")
            .setHtmlDescription(
                "<p>Defining the same symbol in multiple packages creates conflicts.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* MyFunc defined in both PackageA and PackageB *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Rename to PackageA`MyFunc and PackageB`MyFunc *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("conflict", "naming");

            rule303.setDebtRemediationFunction(rule303.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

    }

    private static void defineModularityRulesContinued(NewRepository repository) {
        NewRule rule304 = repository.createRule(SYMBOL_REDEFINITION_AFTER_IMPORT_KEY)
            .setName("Symbol defined locally after importing package with same symbol")
            .setHtmlDescription(
                "<p>Redefining imported symbols locally causes confusing shadowing.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Needs[\"MyPackage`\"];\n"
                + "MyFunc[] := ...;  (* Shadows MyPackage`MyFunc! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Use different name or explicit context *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("shadowing", "conflict");

            rule304.setDebtRemediationFunction(rule304.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule305 = repository.createRule(PACKAGE_VERSION_MISMATCH_KEY)
            .setName("Importing incompatible package versions causes runtime errors")
            .setHtmlDescription(
                "<p>Importing packages with incompatible versions leads to runtime errors.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>Needs[\"MyPackage`\"];  (* Requires v2.x but v1.x is loaded *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Ensure compatible versions or add version checks *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.HIGH)
            .setTags("versioning", "compatibility");

            rule305.setDebtRemediationFunction(rule305.debtRemediationFunctions().constantPerIssue(TIME_30MIN));

        // Documentation & Consistency (Items 246-250)

        NewRule rule306 = repository.createRule(PUBLIC_EXPORT_MISSING_USAGE_MESSAGE_KEY)
            .setName("Exported package function missing usage message")
            .setHtmlDescription(
                "<p>Functions exported from a package should have usage messages for API documentation.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];\n"
                + "MyFunc[x_] := x^2;  (* Exported but no usage message *)\n"
                + "EndPackage[];</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];\n"
                + "MyFunc::usage = \"MyFunc[x] returns x squared.\";\n"
                + "Begin[\"Private`\"];\n"
                + "MyFunc[x_] := x^2;\n"
                + "End[];\n"
                + "EndPackage[];</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("documentation", "api", "package");

            rule306.setDebtRemediationFunction(rule306.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule307 = repository.createRule(INCONSISTENT_PARAMETER_NAMES_ACROSS_OVERLOADS_KEY)
            .setName("Inconsistent parameter names across overloads is confusing")
            .setHtmlDescription(
                "<p>Function overloads should use consistent parameter naming for clarity.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>f[x_] := x^2;\n"
                + "f[y_, z_] := y + z;  (* x vs y inconsistent *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>f[x_] := x^2;\n"
                + "f[x_, y_] := x + y;  (* Consistent *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("naming", "consistency");

            rule307.setDebtRemediationFunction(rule307.debtRemediationFunctions().constantPerIssue("2min"));

        NewRule rule308 = repository.createRule(PUBLIC_FUNCTION_WITH_IMPLEMENTATION_DETAILS_IN_NAME_KEY)
            .setName("Public symbol with 'Internal', 'Helper', 'Private' in name is leaky abstraction")
            .setHtmlDescription(
                "<p>Public symbols should not expose implementation details in their names.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>PublicHelperFunc[] := ...;  (* 'Helper' suggests private *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>PublicUtilityFunc[] := ...;  (* Better abstraction *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("naming", "abstraction");

            rule308.setDebtRemediationFunction(rule308.debtRemediationFunctions().constantPerIssue("5min"));

        NewRule rule309 = repository.createRule(PUBLIC_API_NOT_IN_PACKAGE_CONTEXT_KEY)
            .setName("Public symbol not in package context is wrong context")
            .setHtmlDescription(
                "<p>Public API symbols must be defined in the package context, not Private` or Global`.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];\n"
                + "Begin[\"Private`\"];\n"
                + "PublicFunc[] := ...;  (* In Private! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>BeginPackage[\"MyPackage`\"];\n"
                + "PublicFunc[]; (* Declare in package context *)\n"
                + "Begin[\"Private`\"];\n"
                + "PublicFunc[] := ...;  (* Define *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.RELIABILITY, Severity.MEDIUM)
            .setTags("context", "api");

            rule309.setDebtRemediationFunction(rule309.debtRemediationFunctions().constantPerIssue(TIME_20MIN));

        NewRule rule310 = repository.createRule(TEST_FUNCTION_IN_PRODUCTION_CODE_KEY)
            .setName("Function with 'Test' in name should be in test package")
            .setHtmlDescription(
                "<p>Test functions should be in test files/packages, not production code.</p>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>(* In MyPackage.m *)\n"
                + "TestMyFunc[] := ...;  (* Test in production code! *)</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>(* Move to MyPackageTest.m *)</pre>"
            )
            .addDefaultImpact(SoftwareQuality.MAINTAINABILITY, Severity.LOW)
            .setTags("testing", "organization");

            rule310.setDebtRemediationFunction(rule310.debtRemediationFunctions().constantPerIssue("5min"));
    }

}
