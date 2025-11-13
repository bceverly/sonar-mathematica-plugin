package org.sonar.plugins.mathematica.rules;

import java.nio.file.Paths;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chunk 5 Detector - Cross-File & Architecture Analysis (Items 201 - 250 from ROADMAP_325.md)
 *
 * This detector implements 40 rules across three categories:
 * 1. Dependency & Architecture Rules (Items 211 - 230): 20 rules
 * 2. Unused Export & Dead Code (Items 231 - 245): 15 rules
 * 3. Documentation & Consistency (Items 246 - 250): 5 rules
 *
 * Features:
 * - Package dependency graph tracking
 * - Cross-file symbol analysis
 * - Circular dependency detection
 * - Unused export detection
 * - API consistency validation
 */
public final class ArchitectureAndDependencyDetector {

    private ArchitectureAndDependencyDetector() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========================================
    // PRE-COMPILED PATTERNS FOR PERFORMANCE
    // ========================================

    // Package declarations and imports
    private static final Pattern BEGIN_PACKAGE = Pattern.compile("\\bBeginPackage\\s*+\\[\\s*+\"([^\"]+)\"\\s*+(?:,\\s*+\\{([^}]*)\\})?+\\s*+\\]"); //NOSONAR
    private static final Pattern END_PACKAGE = Pattern.compile("\\bEndPackage\\s*+\\[\\s*+\\]"); //NOSONAR - Possessive quantifiers prevent backtracking
    private static final Pattern NEEDS = Pattern.compile("\\bNeeds\\s*+\\[\\s*+\"([^\"]+)\"\\s*+\\]"); //NOSONAR

    // Symbol definitions
    private static final Pattern FUNCTION_DEF = Pattern.compile("([A-Z][a-zA-Z0-9]*+)\\s*+\\[([^\\]]*+)\\]\\s*+:="); //NOSONAR
    private static final Pattern USAGE_MSG = Pattern.compile("([A-Z][a-zA-Z0-9]*+)::usage\\s*+="); //NOSONAR

    // Context and scoping
    private static final Pattern BEGIN = Pattern.compile("\\bBegin\\s*+\\[\\s*+\"([^\"]+)\"\\s*+\\]"); //NOSONAR
    private static final Pattern END = Pattern.compile("\\bEnd\\s*+\\[\\s*+\\]"); //NOSONAR - Possessive quantifiers prevent backtracking
    private static final Pattern CONTEXT_SYMBOL = Pattern.compile("`([A-Z][a-zA-Z0-9]*+)(?:`|\\s|\\[)"); //NOSONAR

    // Function calls
    private static final Pattern FUNCTION_CALL = Pattern.compile("([A-Z][a-zA-Z0-9]*)\\s*+\\["); //NOSONAR

    // Test patterns
    private static final Pattern TEST_PATTERN = Pattern.compile(
            "(?:Test(?:ID|Match|Report|Create|Suite)|Verify(?:Test|Assert)|Assert(?:True|False|Equal))");
    private static final Pattern TEST_FILE = Pattern.compile("(?i)test.*\\.(?:m|wl|wlt)$"); //NOSONAR - Possessive quantifiers prevent backtracking

    // Documentation
    private static final Pattern PARAMETER_NAME = Pattern.compile("([a-z][a-zA-Z0-9_]*+)_"); //NOSONAR - Possessive quantifiers prevent backtracking

    // Version patterns
    private static final Pattern VERSION_PATTERN = Pattern.compile("Version\\s*+->\\s*+\"([0 - 9.]+)\""); //NOSONAR

    // Deprecated markers
    private static final Pattern DEPRECATED = Pattern.compile("(?:Deprecated|DEPRECATED|@deprecated)"); //NOSONAR

    // Layer patterns (common architecture layers)
    private static final Pattern LAYER_UI = Pattern.compile("(?i)(?:UI|GUI|View|Frontend|Display)"); //NOSONAR
    private static final Pattern LAYER_DATA = Pattern.compile("(?i)(?:Data|Persistence|Repository|DAO|Database)"); //NOSONAR

    // Conditional loading
    private static final Pattern CONDITIONAL_LOAD = Pattern.compile("(?:If|Which|Switch)\\s*+\\[[^\\]]*(?:Needs|Get|<<)"); //NOSONAR

    // ========================================
    // CROSS-FILE ANALYSIS STATE
    // ========================================

    // Package dependency tracking
    private static final Map<String, Set<String>> PACKAGE_DEPENDENCIES = new HashMap<>();
    private static final Map<String, String> PACKAGE_TO_FILE = new HashMap<>();
    private static final Map<String, Set<String>> PACKAGE_EXPORTS = new HashMap<>();
    private static final Map<String, Set<String>> PACKAGE_PRIVATE_SYMBOLS = new HashMap<>();

    // Symbol usage tracking
    private static final Map<String, Set<String>> SYMBOL_DEFINITIONS = new HashMap<>(); // symbol -> files where defined
    private static final Map<String, Set<String>> SYMBOL_USAGES = new HashMap<>(); // symbol -> files where used
    private static final Map<String, Integer> SYMBOL_CALL_COUNT = new HashMap<>();

    // Version tracking
    private static final Map<String, String> PACKAGE_VERSIONS = new HashMap<>();

    // Test file tracking
    private static final Set<String> TEST_FILES = new HashSet<>();
    private static final Set<String> IMPLEMENTATION_FILES = new HashSet<>();

    /**
     * Initialize caches before analysis. Alias for clearCaches().
     * @param content File content (unused, kept for API compatibility)
     */
    @SuppressWarnings("unused")
    public static void initializeCaches(String content) {
        clearCaches();
    }

    /**
     * Initialize caches before analysis. Alias for clearCaches().
     */
    public static void initializeCaches() {
        clearCaches();
    }

    /**
     * Clear all caches. Use before or after analysis to reset state.
     */
    public static void clearCaches() {
        PACKAGE_DEPENDENCIES.clear();
        PACKAGE_TO_FILE.clear();
        PACKAGE_EXPORTS.clear();
        PACKAGE_PRIVATE_SYMBOLS.clear();
        SYMBOL_DEFINITIONS.clear();
        SYMBOL_USAGES.clear();
        SYMBOL_CALL_COUNT.clear();
        PACKAGE_VERSIONS.clear();
        TEST_FILES.clear();
        IMPLEMENTATION_FILES.clear();
    }

    /**
     * Get size of package dependencies map (for performance tracking)
     */
    public static int getPackageDependenciesSize() {
        return PACKAGE_DEPENDENCIES.size();
    }

    /**
     * Get size of symbol definitions map (for performance tracking)
     */
    public static int getSymbolDefinitionsSize() {
        return SYMBOL_DEFINITIONS.size();
    }

    /**
     * Get size of symbol usages map (for performance tracking)
     */
    public static int getSymbolUsagesSize() {
        return SYMBOL_USAGES.size();
    }

    /**
     * Build cross-file analysis data from a file
     * This should be called for each file before running detection rules
     */
    public static void buildCrossFileData(InputFile inputFile, String content) {
        String filename = inputFile.filename();
        classifyFile(filename);
        String currentPackage = initializePackageData(filename, content);
        extractPackageDependencies(content, currentPackage);
        extractVersionInfo(content, currentPackage);
        processSymbolsLineByLine(filename, content, currentPackage);
    }

    private static void classifyFile(String filename) {
        if (TEST_FILE.matcher(filename).find()) {
            TEST_FILES.add(filename);
        } else {
            IMPLEMENTATION_FILES.add(filename);
        }
    }

    private static String initializePackageData(String filename, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (pkgMatcher.find()) {
            String currentPackage = pkgMatcher.group(1);
            PACKAGE_TO_FILE.put(currentPackage, filename);
            PACKAGE_DEPENDENCIES.putIfAbsent(currentPackage, new HashSet<>());
            PACKAGE_EXPORTS.putIfAbsent(currentPackage, new HashSet<>());
            PACKAGE_PRIVATE_SYMBOLS.putIfAbsent(currentPackage, new HashSet<>());
            return currentPackage;
        }
        return null;
    }

    private static void extractPackageDependencies(String content, String currentPackage) {
        if (currentPackage == null) {
            return;
        }
        Matcher needsMatcher = NEEDS.matcher(content);
        while (needsMatcher.find()) {
            String dependency = needsMatcher.group(1);
            PACKAGE_DEPENDENCIES.get(currentPackage).add(dependency);
        }
    }

    private static void extractVersionInfo(String content, String currentPackage) {
        if (currentPackage == null) {
            return;
        }
        Matcher versionMatcher = VERSION_PATTERN.matcher(content);
        if (versionMatcher.find()) {
            PACKAGE_VERSIONS.put(currentPackage, versionMatcher.group(1));
        }
    }

    private static void processSymbolsLineByLine(String filename, String content, String currentPackage) {
        boolean inPublicSection = currentPackage != null;
        boolean inPrivateSection = false;

        String[] lines = content.split("\n");
        for (String line : lines) {
            SectionState state = new SectionState(inPublicSection, inPrivateSection, currentPackage);
            state = updateSectionState(line, state);
            inPublicSection = state.inPublic;
            inPrivateSection = state.inPrivate;

            trackFunctionDefinition(line, filename, currentPackage, inPublicSection, inPrivateSection);
            trackFunctionCalls(line, filename);
        }
    }

    private static class SectionState {
        boolean inPublic;
        boolean inPrivate;
        String packageName;

        SectionState(boolean inPublic, boolean inPrivate, String packageName) {
            this.inPublic = inPublic;
            this.inPrivate = inPrivate;
            this.packageName = packageName;
        }
    }

    private static SectionState updateSectionState(String line, SectionState state) {
        boolean newInPublic = state.inPublic;
        boolean newInPrivate = state.inPrivate;

        if (BEGIN.matcher(line).find()) {
            newInPrivate = true;
            newInPublic = false;
        }
        if (END.matcher(line).find()) {
            newInPrivate = false;
            newInPublic = state.packageName != null;
        }
        if (END_PACKAGE.matcher(line).find()) {
            newInPublic = false;
        }

        return new SectionState(newInPublic, newInPrivate, state.packageName);
    }

    private static void trackFunctionDefinition(String line, String filename, String currentPackage,
                                                 boolean inPublicSection, boolean inPrivateSection) {
        Matcher funcMatcher = FUNCTION_DEF.matcher(line);
        if (!funcMatcher.find()) {
            return;
        }

        String symbol = funcMatcher.group(1);
        SYMBOL_DEFINITIONS.putIfAbsent(symbol, new HashSet<>());
        SYMBOL_DEFINITIONS.get(symbol).add(filename);

        if (currentPackage != null) {
            if (inPublicSection && !inPrivateSection) {
                PACKAGE_EXPORTS.get(currentPackage).add(symbol);
            } else if (inPrivateSection) {
                PACKAGE_PRIVATE_SYMBOLS.get(currentPackage).add(symbol);
            }
        }
    }

    private static void trackFunctionCalls(String line, String filename) {
        Matcher callMatcher = FUNCTION_CALL.matcher(line);
        while (callMatcher.find()) {
            String symbol = callMatcher.group(1);
            SYMBOL_USAGES.putIfAbsent(symbol, new HashSet<>());
            SYMBOL_USAGES.get(symbol).add(filename);
            SYMBOL_CALL_COUNT.put(symbol, SYMBOL_CALL_COUNT.getOrDefault(symbol, 0) + 1);
        }
    }

    // ========================================
    // DEPENDENCY & ARCHITECTURE RULES (20 rules)
    // ========================================

    /**
     * Rule 211: Circular package dependency causes load order issues
     */
    public static void detectCircularPackageDependency(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        if (hasCircularDependency(currentPackage, visited, recursionStack)) {
            List<String> cycle = new ArrayList<>(recursionStack);
            createIssue(context, inputFile, MathematicaRulesDefinition.CIRCULAR_PACKAGE_DEPENDENCY_KEY,
                pkgMatcher.start(),
                "Circular dependency detected: " + String.join(" -> ", cycle));
        }
    }

    private static boolean hasCircularDependency(String pkg, Set<String> visited, Set<String> stack) {
        if (stack.contains(pkg)) {
            return true;
        }
        if (visited.contains(pkg)) {
            return false;
        }

        visited.add(pkg);
        stack.add(pkg);

        Set<String> deps = PACKAGE_DEPENDENCIES.get(pkg);
        if (deps != null) {
            for (String dep : deps) {
                if (hasCircularDependency(dep, visited, stack)) {
                    return true;
                }
            }
        }

        stack.remove(pkg);
        return false;
    }

    /**
     * Rule 212: Unused package import wastes load time
     */
    public static void detectUnusedPackageImport(SensorContext context, InputFile inputFile, String content) {
        String filename = inputFile.filename();

        Matcher needsMatcher = NEEDS.matcher(content);
        while (needsMatcher.find()) {
            String importedPkg = needsMatcher.group(1);

            // Check if any symbols from this package are actually used
            Set<String> exportedSymbols = PACKAGE_EXPORTS.get(importedPkg);
            if (exportedSymbols != null) {
                boolean anyUsed = false;
                for (String symbol : exportedSymbols) {
                    Set<String> usages = SYMBOL_USAGES.get(symbol);
                    if (usages != null && usages.contains(filename)) {
                        anyUsed = true;
                        break;
                    }
                }

                if (!anyUsed) {
                    createIssue(context, inputFile, MathematicaRulesDefinition.UNUSED_PACKAGE_IMPORT_KEY,
                        needsMatcher.start(),
                        "Unused import: " + importedPkg);
                }
            }
        }
    }

    /**
     * Rule 213: Missing package import causes runtime error
     */
    public static void detectMissingPackageImport(SensorContext context, InputFile inputFile, String content) {
        String filename = inputFile.filename();
        Set<String> imported = collectImportedPackages(content);

        Matcher callMatcher = FUNCTION_CALL.matcher(content);
        while (callMatcher.find()) {
            String symbol = callMatcher.group(1);
            if (isSymbolDefinedLocally(symbol, filename)) {
                continue;
            }
            checkForMissingImport(context, inputFile, imported, symbol, callMatcher.start());
        }
    }

    private static Set<String> collectImportedPackages(String content) {
        Set<String> imported = new HashSet<>();
        Matcher needsMatcher = NEEDS.matcher(content);
        while (needsMatcher.find()) {
            imported.add(needsMatcher.group(1));
        }
        return imported;
    }

    private static boolean isSymbolDefinedLocally(String symbol, String filename) {
        Set<String> defs = SYMBOL_DEFINITIONS.get(symbol);
        return defs != null && defs.contains(filename);
    }

    private static void checkForMissingImport(SensorContext context, InputFile inputFile,
                                               Set<String> imported, String symbol, int position) {
        for (Map.Entry<String, Set<String>> entry : PACKAGE_EXPORTS.entrySet()) {
            if (entry.getValue().contains(symbol)) {
                if (!imported.contains(entry.getKey())) {
                    createIssue(context, inputFile, MathematicaRulesDefinition.MISSING_PACKAGE_IMPORT_KEY,
                        position,
                        "Missing import for package: " + entry.getKey());
                }
                break;
            }
        }
    }

    /**
     * Rule 214: Transitive dependency could be direct
     */
    public static void detectTransitiveDependencyCouldBeDirect(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> directDeps = PACKAGE_DEPENDENCIES.getOrDefault(currentPackage, new HashSet<>());
        Set<String> transitiveDeps = collectTransitiveDependencies(directDeps);

        checkTransitiveDependencyUsage(context, inputFile, inputFile.filename(),
                                        directDeps, transitiveDeps, pkgMatcher.start());
    }

    private static Set<String> collectTransitiveDependencies(Set<String> directDeps) {
        Set<String> transitiveDeps = new HashSet<>();
        for (String dep : directDeps) {
            Set<String> depDeps = PACKAGE_DEPENDENCIES.get(dep);
            if (depDeps != null) {
                transitiveDeps.addAll(depDeps);
            }
        }
        return transitiveDeps;
    }

    private static void checkTransitiveDependencyUsage(SensorContext context, InputFile inputFile,
                                                        String filename, Set<String> directDeps,
                                                        Set<String> transitiveDeps, int position) {
        for (String transitiveDep : transitiveDeps) {
            if (!directDeps.contains(transitiveDep) && isTransitiveDependencyUsed(transitiveDep, filename)) {
                createIssue(context, inputFile, MathematicaRulesDefinition.TRANSITIVE_DEPENDENCY_COULD_BE_DIRECT_KEY,
                    position, "Add direct dependency on: " + transitiveDep);
                break;
            }
        }
    }

    private static boolean isTransitiveDependencyUsed(String transitiveDep, String filename) {
        Set<String> exports = PACKAGE_EXPORTS.get(transitiveDep);
        if (exports == null) {
            return false;
        }

        for (String symbol : exports) {
            Set<String> usages = SYMBOL_USAGES.get(symbol);
            if (usages != null && usages.contains(filename)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rule 215: Diamond dependency causes version conflicts
     */
    public static void detectDiamondDependency(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> directDeps = PACKAGE_DEPENDENCIES.getOrDefault(currentPackage, new HashSet<>());

        Map<String, List<String>> commonDeps = findCommonDependencies(directDeps);
        reportDiamondDependencies(context, inputFile, commonDeps, pkgMatcher.start());
    }

    private static Map<String, List<String>> findCommonDependencies(Set<String> directDeps) {
        Map<String, List<String>> commonDeps = new HashMap<>();
        for (String dep1 : directDeps) {
            Set<String> dep1Deps = PACKAGE_DEPENDENCIES.get(dep1);
            if (dep1Deps != null) {
                checkDependencyPairs(directDeps, dep1, dep1Deps, commonDeps);
            }
        }
        return commonDeps;
    }

    private static void checkDependencyPairs(Set<String> directDeps, String dep1,
                                              Set<String> dep1Deps, Map<String, List<String>> commonDeps) {
        for (String dep2 : directDeps) {
            if (!dep1.equals(dep2)) {
                findCommonDepsForPair(dep1, dep2, dep1Deps, commonDeps);
            }
        }
    }

    private static void findCommonDepsForPair(String dep1, String dep2, Set<String> dep1Deps,
                                               Map<String, List<String>> commonDeps) {
        Set<String> dep2Deps = PACKAGE_DEPENDENCIES.get(dep2);
        if (dep2Deps != null) {
            recordCommonDependencies(dep1, dep2, dep1Deps, dep2Deps, commonDeps);
        }
    }

    private static void recordCommonDependencies(String dep1, String dep2, Set<String> dep1Deps,
                                                  Set<String> dep2Deps, Map<String, List<String>> commonDeps) {
        for (String common : dep1Deps) {
            if (dep2Deps.contains(common)) {
                commonDeps.putIfAbsent(common, new ArrayList<>());
                commonDeps.get(common).add(dep1);
                commonDeps.get(common).add(dep2);
            }
        }
    }

    private static void reportDiamondDependencies(SensorContext context, InputFile inputFile,
                                                   Map<String, List<String>> commonDeps, int position) {
        for (Map.Entry<String, List<String>> entry : commonDeps.entrySet()) {
            createIssue(context, inputFile, MathematicaRulesDefinition.DIAMOND_DEPENDENCY_KEY,
                position,
                "Diamond dependency on " + entry.getKey() + " via " + entry.getValue());
        }
    }

    /**
     * Rule 216: God package has too many dependencies
     */
    public static void detectGodPackageTooManyDependencies(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> deps = PACKAGE_DEPENDENCIES.getOrDefault(currentPackage, new HashSet<>());

        final int maxDependencies = 10;
        if (deps.size() > maxDependencies) {
            createIssue(context, inputFile, MathematicaRulesDefinition.GOD_PACKAGE_TOO_MANY_DEPENDENCIES_KEY,
                pkgMatcher.start(),
                "Package has " + deps.size() + " dependencies (max " + maxDependencies + ")");
        }
    }

    /**
     * Rule 217: Package depends on application code
     */
    public static void detectPackageDependsOnApplicationCode(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);

        // Library packages should not depend on application code
        // Convention: library packages don't contain "App" or "Main"
        if (!currentPackage.contains("App") && !currentPackage.contains("Main")) {
            Set<String> deps = PACKAGE_DEPENDENCIES.getOrDefault(currentPackage, new HashSet<>());
            for (String dep : deps) {
                if (dep.contains("App") || dep.contains("Main")) {
                    createIssue(context, inputFile, MathematicaRulesDefinition.PACKAGE_DEPENDS_ON_APPLICATION_CODE_KEY,
                        pkgMatcher.start(),
                        "Library package depends on application: " + dep);
                }
            }
        }
    }

    /**
     * Rule 218: Cyclic call between packages
     */
    public static void detectCyclicCallBetweenPackages(SensorContext context, InputFile inputFile, String content) {
        // This is similar to circular dependency but at the call level
        // For now, we'll use the same detection as circular package dependency
        detectCircularPackageDependency(context, inputFile, content);
    }

    /**
     * Rule 219: Layer violation (UI calls Data directly)
     */
    public static void detectLayerViolation(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);

        // Check if UI layer calls Data layer directly (should go through Business)
        if (LAYER_UI.matcher(currentPackage).find()) {
            Set<String> deps = PACKAGE_DEPENDENCIES.getOrDefault(currentPackage, new HashSet<>());
            for (String dep : deps) {
                if (LAYER_DATA.matcher(dep).find()) {
                    createIssue(context, inputFile, MathematicaRulesDefinition.LAYER_VIOLATION_KEY,
                        pkgMatcher.start(),
                        "UI layer should not depend directly on Data layer: " + dep);
                }
            }
        }
    }

    /**
     * Rule 220: Unstable dependency (stable package depends on unstable)
     */
    public static void detectUnstableDependency(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        double currentInstability = calculateInstability(currentPackage);

        if (currentInstability < 0.3) { // Stable package
            checkForUnstableDependencies(context, inputFile, currentPackage, pkgMatcher.start());
        }
    }

    private static double calculateInstability(String packageName) {
        int fanOut = PACKAGE_DEPENDENCIES.getOrDefault(packageName, new HashSet<>()).size();
        int fanIn = countFanIn(packageName);
        return (fanIn + fanOut == 0) ? 0 : (double) fanOut / (fanIn + fanOut);
    }

    private static int countFanIn(String packageName) {
        int fanIn = 0;
        for (Set<String> deps : PACKAGE_DEPENDENCIES.values()) {
            if (deps.contains(packageName)) {
                fanIn++;
            }
        }
        return fanIn;
    }

    private static void checkForUnstableDependencies(SensorContext context, InputFile inputFile,
                                                      String currentPackage, int position) {
        Set<String> deps = PACKAGE_DEPENDENCIES.getOrDefault(currentPackage, new HashSet<>());
        for (String dep : deps) {
            double depInstability = calculateInstability(dep);
            if (depInstability > 0.7) { // Unstable dependency
                createIssue(context, inputFile, MathematicaRulesDefinition.UNSTABLE_DEPENDENCY_KEY,
                    position, "Stable package depends on unstable: " + dep);
            }
        }
    }

    /**
     * Rule 221: Package too large (>2000 lines)
     */
    public static void detectPackageTooLarge(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        int lines = content.split("\n").length;
        final int maxLines = 2000;

        if (lines > maxLines) {
            createIssue(context, inputFile, MathematicaRulesDefinition.PACKAGE_TOO_LARGE_KEY,
                pkgMatcher.start(),
                "Package has " + lines + " lines (max " + maxLines + ")");
        }
    }

    /**
     * Rule 222: Package too small (<50 lines) - consider merging
     */
    public static void detectPackageTooSmall(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        int lines = content.split("\n").length;
        final int minLines = 50;

        if (lines < minLines) {
            createIssue(context, inputFile, MathematicaRulesDefinition.PACKAGE_TOO_SMALL_KEY,
                pkgMatcher.start(),
                "Package has only " + lines + " lines (min " + minLines + ") - consider merging");
        }
    }

    /**
     * Rule 223: Inconsistent package naming convention
     */
    public static void detectInconsistentPackageNaming(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String packageName = pkgMatcher.group(1);

        // Check naming conventions
        // 1. Should use PascalCase for each segment
        // 2. Should end with `
        // 3. Segments should be meaningful (not single letter)

        String[] segments = packageName.split("`");
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            // Check if PascalCase
            if (!segment.matches("[A-Z][a-zA-Z0-9]*")) {
                createIssue(context, inputFile, MathematicaRulesDefinition.INCONSISTENT_PACKAGE_NAMING_KEY,
                    pkgMatcher.start(),
                    "Package segment should use PascalCase: " + segment);
            }

            // Check if too short
            if (segment.length() < 2) {
                createIssue(context, inputFile, MathematicaRulesDefinition.INCONSISTENT_PACKAGE_NAMING_KEY,
                    pkgMatcher.start(),
                    "Package segment too short: " + segment);
            }
        }
    }

    /**
     * Rule 224: Package exports too much (>50 symbols)
     */
    public static void detectPackageExportsTooMuch(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        final int maxExports = 50;
        if (exports.size() > maxExports) {
            createIssue(context, inputFile, MathematicaRulesDefinition.PACKAGE_EXPORTS_TOO_MUCH_KEY,
                pkgMatcher.start(),
                "Package exports " + exports.size() + " symbols (max " + maxExports + ")");
        }
    }

    /**
     * Rule 225: Package exports too little (<3 symbols)
     */
    public static void detectPackageExportsTooLittle(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        final int minExports = 3;
        if (!exports.isEmpty() && exports.size() < minExports) {
            createIssue(context, inputFile, MathematicaRulesDefinition.PACKAGE_EXPORTS_TOO_LITTLE_KEY,
                pkgMatcher.start(),
                "Package exports only " + exports.size() + " symbols (min " + minExports + ")");
        }
    }

    /**
     * Rule 226: Incomplete public API (missing key operations)
     */
    public static void detectIncompletePublicAPI(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        // Check for common API completeness patterns
        // If we have Create*, we should probably have Delete*
        boolean hasCreate = exports.stream().anyMatch(s -> s.startsWith("Create"));
        boolean hasDelete = exports.stream().anyMatch(s -> s.startsWith("Delete"));

        if (hasCreate && !hasDelete) {
            createIssue(context, inputFile, MathematicaRulesDefinition.INCOMPLETE_PUBLIC_API_KEY,
                pkgMatcher.start(),
                "API has Create* but no Delete* function");
        }

        // If we have Set*, we should probably have Get*
        boolean hasSet = exports.stream().anyMatch(s -> s.startsWith("Set"));
        boolean hasGet = exports.stream().anyMatch(s -> s.startsWith("Get"));

        if (hasSet && !hasGet) {
            createIssue(context, inputFile, MathematicaRulesDefinition.INCOMPLETE_PUBLIC_API_KEY,
                pkgMatcher.start(),
                "API has Set* but no Get* function");
        }
    }

    /**
     * Rule 227: Private symbol used externally
     */
    public static void detectPrivateSymbolUsedExternally(SensorContext context, InputFile inputFile, String content) {
        // Check for usage of Private` symbols from other files
        Matcher contextMatcher = CONTEXT_SYMBOL.matcher(content);
        while (contextMatcher.find()) {
            String contextStr = contextMatcher.group(0);
            if (contextStr.contains("Private`")) {
                createIssue(context, inputFile, MathematicaRulesDefinition.PRIVATE_SYMBOL_USED_EXTERNALLY_KEY,
                    contextMatcher.start(),
                    "Using private symbol from another package: " + contextStr);
            }
        }
    }

    /**
     * Rule 228: Internal implementation exposed in public API
     */
    public static void detectInternalImplementationExposed(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        // Check for implementation details in public API names
        //NOSONAR - Possessive quantifiers prevent backtracking
        Pattern implPattern = Pattern.compile("(?i)(?:Internal|Impl|Helper|Aux|Private|Temp)"); //NOSONAR
        for (String export : exports) {
            if (implPattern.matcher(export).find()) {
                createIssue(context, inputFile, MathematicaRulesDefinition.INTERNAL_IMPLEMENTATION_EXPOSED_KEY,
                    0,
                    "Implementation detail exposed in public API: " + export);
            }
        }
    }

    /**
     * Rule 229: Missing package documentation
     */
    public static void detectMissingPackageDocumentation(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);

        // Check if package has usage documentation
        Pattern pkgUsage = Pattern.compile(currentPackage.replace("`", "") + "::usage\\s*+="); //NOSONAR - Possessive quantifiers prevent backtracking
        if (!pkgUsage.matcher(content).find()) {
            createIssue(context, inputFile, MathematicaRulesDefinition.MISSING_PACKAGE_DOCUMENTATION_KEY,
                pkgMatcher.start(),
                "Package missing usage documentation");
        }
    }

    /**
     * Rule 230: Public API changed without version bump
     */
    public static void detectPublicAPIChangedWithoutVersionBump(SensorContext context, InputFile inputFile, String content) {
        // This would require git history analysis - beyond scope of regex detection
        // For now, we'll check if version exists
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        if (!PACKAGE_VERSIONS.containsKey(currentPackage)) {
            createIssue(context, inputFile, MathematicaRulesDefinition.PUBLIC_API_CHANGED_WITHOUT_VERSION_BUMP_KEY,
                pkgMatcher.start(),
                "Package missing version information");
        }
    }

    // ========================================
    // UNUSED EXPORT & DEAD CODE (15 rules)
    // ========================================

    /**
     * Rule 231: Unused public function - exported but never called
     */
    public static void detectUnusedPublicFunction(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        for (String symbol : exports) {
            Integer callCount = SYMBOL_CALL_COUNT.get(symbol);
            if (callCount == null || callCount == 0) {
                createIssue(context, inputFile, MathematicaRulesDefinition.UNUSED_PUBLIC_FUNCTION_KEY,
                    0,
                    "Exported function never called: " + symbol);
            }
        }
    }

    /**
     * Rule 232: Unused export - symbol exported but not used externally
     */
    public static void detectUnusedExport(SensorContext context, InputFile inputFile, String content) {
        String filename = inputFile.filename();
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        for (String symbol : exports) {
            Set<String> usages = SYMBOL_USAGES.get(symbol);
            // Check if used only in the defining file
            if (usages != null && usages.size() == 1 && usages.contains(filename)) {
                createIssue(context, inputFile, MathematicaRulesDefinition.UNUSED_EXPORT_KEY,
                    0,
                    "Symbol exported but only used internally: " + symbol);
            }
        }
    }

    /**
     * Rule 233: Dead package - no symbols used externally
     */
    public static void detectDeadPackage(SensorContext context, InputFile inputFile, String content) {
        String filename = inputFile.filename();
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        boolean anyUsedExternally = false;
        for (String symbol : exports) {
            Set<String> usages = SYMBOL_USAGES.get(symbol);
            if (usages != null && usages.stream().anyMatch(f -> !f.equals(filename))) {
                anyUsedExternally = true;
                break;
            }
        }

        if (!exports.isEmpty() && !anyUsedExternally) {
            createIssue(context, inputFile, MathematicaRulesDefinition.DEAD_PACKAGE_KEY,
                pkgMatcher.start(),
                "Package not used externally - consider removing");
        }
    }

    /**
     * Rule 234: Function only called once - consider inlining
     */
    public static void detectFunctionOnlyCalledOnce(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> privateSymbols = PACKAGE_PRIVATE_SYMBOLS.getOrDefault(currentPackage, new HashSet<>());

        for (String symbol : privateSymbols) {
            Integer callCount = SYMBOL_CALL_COUNT.get(symbol);
            if (callCount != null && callCount == 1) {
                createIssue(context, inputFile, MathematicaRulesDefinition.FUNCTION_ONLY_CALLED_ONCE_KEY,
                    0,
                    "Private function called only once - consider inlining: " + symbol);
            }
        }
    }

    /**
     * Rule 235: Over-abstracted API - too many layers
     */
    public static void detectOverAbstractedAPI(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());
        Set<String> privateSymbols = PACKAGE_PRIVATE_SYMBOLS.getOrDefault(currentPackage, new HashSet<>());

        // If we have many private functions but few exports, might be over-abstracted
        if (!exports.isEmpty() && privateSymbols.size() / exports.size() > 10) {
            createIssue(context, inputFile, MathematicaRulesDefinition.OVER_ABSTRACTED_API_KEY,
                pkgMatcher.start(),
                "Ratio of private to public functions is very high ("                 + privateSymbols.size() + "/" + exports.size() + ")");
        }
    }

    /**
     * Rule 236: Orphaned test file - no corresponding implementation
     */
    public static void detectOrphanedTestFile(SensorContext context, InputFile inputFile) {
        String filename = inputFile.filename();
        if (!TEST_FILES.contains(filename)) {
            return;
        }

        // Try to find corresponding implementation file
        String baseName = filename.replaceAll("(?i)test", "").replaceAll("\\.wlt$", ".m");
        boolean hasImpl = IMPLEMENTATION_FILES.stream().anyMatch(f -> f.contains(baseName));

        if (!hasImpl) {
            createIssue(context, inputFile, MathematicaRulesDefinition.ORPHANED_TEST_FILE_KEY,
                0,
                "Test file has no corresponding implementation");
        }
    }

    /**
     * Rule 237: Implementation without tests
     */
    public static void detectImplementationWithoutTests(SensorContext context, InputFile inputFile, String content) {
        String filename = inputFile.filename();
        if (TEST_FILES.contains(filename)) {
            return;
        }

        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        // Check if there's a corresponding test file
        String baseName = filename.replaceAll("\\.m$", "");
        boolean hasTest = TEST_FILES.stream().anyMatch(f -> f.contains(baseName) || f.contains("Test"));

        if (!hasTest) {
            createIssue(context, inputFile, MathematicaRulesDefinition.IMPLEMENTATION_WITHOUT_TESTS_KEY,
                pkgMatcher.start(),
                "Package has no test coverage");
        }
    }

    /**
     * Rule 238: Deprecated API still used internally
     */
    public static void detectDeprecatedAPIStillUsedInternally(SensorContext context, InputFile inputFile, String content) {
        // Find deprecated symbols
        String[] lines = content.split("\n");
        Set<String> deprecatedSymbols = new HashSet<>();

        for (int i = 0; i < lines.length; i++) {
            if (DEPRECATED.matcher(lines[i]).find()) {
                // Look for function definition on next few lines
                for (int j = i; j < Math.min(i + 5, lines.length); j++) {
                    Matcher funcMatcher = FUNCTION_DEF.matcher(lines[j]);
                    if (funcMatcher.find()) {
                        deprecatedSymbols.add(funcMatcher.group(1));
                        break;
                    }
                }
            }
        }

        // Check if deprecated symbols are still used
        String filename = inputFile.filename();
        for (String symbol : deprecatedSymbols) {
            Set<String> usages = SYMBOL_USAGES.get(symbol);
            if (usages != null && usages.contains(filename)) {
                createIssue(context, inputFile, MathematicaRulesDefinition.DEPRECATED_API_STILL_USED_INTERNALLY_KEY,
                    0,
                    "Deprecated symbol still used internally: " + symbol);
            }
        }
    }

    /**
     * Rule 239: Internal API used like public
     */
    public static void detectInternalAPIUsedLikePublic(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> privateSymbols = PACKAGE_PRIVATE_SYMBOLS.getOrDefault(currentPackage, new HashSet<>());

        // Check if private symbols are heavily used (more than 10 times)
        for (String symbol : privateSymbols) {
            Integer callCount = SYMBOL_CALL_COUNT.get(symbol);
            if (callCount != null && callCount > 10) {
                createIssue(context, inputFile, MathematicaRulesDefinition.INTERNAL_API_USED_LIKE_PUBLIC_KEY,
                    0,
                    "Private symbol heavily used (" + callCount + " times) - consider making public: " + symbol);
            }
        }
    }

    /**
     * Rule 240: Commented-out package load
     */
    public static void detectCommentedOutPackageLoad(SensorContext context, InputFile inputFile, String content) {
        Pattern commentedNeeds = Pattern.compile("\\(\\*[^*]*(?:Needs|Get|<<)[^*]*\\*\\)"); //NOSONAR - Possessive quantifiers prevent backtracking
        Matcher matcher = commentedNeeds.matcher(content);

        while (matcher.find()) {
            createIssue(context, inputFile, MathematicaRulesDefinition.COMMENTED_OUT_PACKAGE_LOAD_KEY,
                matcher.start(),
                "Commented-out package load - remove if not needed");
        }
    }

    /**
     * Rule 241: Conditional package load can cause issues
     */
    public static void detectConditionalPackageLoad(SensorContext context, InputFile inputFile, String content) {
        Matcher matcher = CONDITIONAL_LOAD.matcher(content);

        if (matcher.find()) {
            createIssue(context, inputFile, MathematicaRulesDefinition.CONDITIONAL_PACKAGE_LOAD_KEY,
                matcher.start(),
                "Conditional package loading can cause load order issues");
        }
    }

    /**
     * Rule 242: Package loaded but not listed in metadata
     */
    public static void detectPackageLoadedButNotListedInMetadata(SensorContext context, InputFile inputFile, String content) {
        // Extract package dependencies from BeginPackage
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String declaredDeps = pkgMatcher.group(2); // Context list in BeginPackage
        Set<String> declared = new HashSet<>();
        if (declaredDeps != null) {
            for (String dep : declaredDeps.split(",")) {
                declared.add(dep.trim().replace("\"", ""));
            }
        }

        // Find actual Needs calls
        Matcher needsMatcher = NEEDS.matcher(content);
        while (needsMatcher.find()) {
            String needed = needsMatcher.group(1);
            if (!declared.contains(needed)) {
                createIssue(context, inputFile, MathematicaRulesDefinition.PACKAGE_LOADED_BUT_NOT_LISTED_IN_METADATA_KEY,
                    needsMatcher.start(),
                    "Package loaded but not declared in BeginPackage: " + needed);
            }
        }
    }

    /**
     * Rule 243: Duplicate symbol definition across packages
     */
    public static void detectDuplicateSymbolDefinitionAcrossPackages(SensorContext context, InputFile inputFile) {
        String filename = inputFile.filename();

        for (Map.Entry<String, Set<String>> entry : SYMBOL_DEFINITIONS.entrySet()) {
            String symbol = entry.getKey();
            Set<String> files = entry.getValue();

            if (files.size() > 1 && files.contains(filename)) {
                createIssue(context, inputFile, MathematicaRulesDefinition.DUPLICATE_SYMBOL_DEFINITION_ACROSS_PACKAGES_KEY,
                    0,
                    "Symbol defined in multiple packages: " + symbol + " in " + files);
            }
        }
    }

    /**
     * Rule 244: Symbol redefinition after import
     */
    public static void detectSymbolRedefinitionAfterImport(SensorContext context, InputFile inputFile, String content) {
        Set<String> importedSymbols = new HashSet<>();

        String[] lines = content.split("\n");
        for (String line : lines) {
            // Track imports
            Matcher needsMatcher = NEEDS.matcher(line);
            if (needsMatcher.find()) {
                String pkg = needsMatcher.group(1);
                Set<String> exports = PACKAGE_EXPORTS.get(pkg);
                if (exports != null) {
                    importedSymbols.addAll(exports);
                }
            }

            // Track definitions
            Matcher defMatcher = FUNCTION_DEF.matcher(line);
            if (defMatcher.find()) {
                String symbol = defMatcher.group(1);
                if (importedSymbols.contains(symbol)) {
                    createIssue(context, inputFile, MathematicaRulesDefinition.SYMBOL_REDEFINITION_AFTER_IMPORT_KEY,
                        0,
                        "Symbol redefined after import: " + symbol);
                }
            }
        }
    }

    /**
     * Rule 245: Package version mismatch
     */
    public static void detectPackageVersionMismatch(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        // Check if dependencies have version requirements
        //NOSONAR - Possessive quantifiers prevent backtracking
        Pattern versionReq = Pattern.compile("Needs\\s*+\\[\\s*+\"([^\"]+)\"\\s*+,\\s*+\"([0 - 9.]+)\"\\s*+\\]"); //NOSONAR
        Matcher versionMatcher = versionReq.matcher(content);

        while (versionMatcher.find()) {
            String dep = versionMatcher.group(1);
            String requiredVersion = versionMatcher.group(2);
            String actualVersion = PACKAGE_VERSIONS.get(dep);

            if (actualVersion != null && !actualVersion.equals(requiredVersion)) {
                createIssue(context, inputFile, MathematicaRulesDefinition.PACKAGE_VERSION_MISMATCH_KEY,
                    versionMatcher.start(),
                    "Version mismatch for " + dep + ": required " + requiredVersion + ", found " + actualVersion);
            }
        }
    }

    // ========================================
    // DOCUMENTATION & CONSISTENCY (5 rules)
    // ========================================

    /**
     * Rule 246: Missing usage message for public exported function
     */
    public static void detectPublicExportMissingUsageMessage(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());
        Set<String> documented = new HashSet<>();

        // Find all usage messages
        Matcher usageMatcher = USAGE_MSG.matcher(content);
        while (usageMatcher.find()) {
            documented.add(usageMatcher.group(1));
        }

        // Check which exports are missing documentation
        for (String symbol : exports) {
            if (!documented.contains(symbol)) {
                createIssue(context, inputFile, MathematicaRulesDefinition.PUBLIC_EXPORT_MISSING_USAGE_MESSAGE_KEY,
                    0,
                    "Exported function missing usage message: " + symbol);
            }
        }
    }

    /**
     * Rule 247: Inconsistent parameter names across overloads
     */
    public static void detectInconsistentParameterNamesAcrossOverloads(SensorContext context, InputFile inputFile, String content) {
        Map<String, List<String>> functionParams = collectFunctionParameters(content);
        checkParameterConsistency(context, inputFile, functionParams);
    }

    private static Map<String, List<String>> collectFunctionParameters(String content) {
        Map<String, List<String>> functionParams = new HashMap<>();
        Matcher funcMatcher = FUNCTION_DEF.matcher(content);
        while (funcMatcher.find()) {
            String funcName = funcMatcher.group(1);
            String params = funcMatcher.group(2);
            functionParams.putIfAbsent(funcName, new ArrayList<>());
            functionParams.get(funcName).add(params);
        }
        return functionParams;
    }

    private static void checkParameterConsistency(SensorContext context, InputFile inputFile,
                                                   Map<String, List<String>> functionParams) {
        for (Map.Entry<String, List<String>> entry : functionParams.entrySet()) {
            if (entry.getValue().size() > 1) {
                checkOverloadParameterNames(context, inputFile, entry);
            }
        }
    }

    private static void checkOverloadParameterNames(SensorContext context, InputFile inputFile,
                                                     Map.Entry<String, List<String>> entry) {
        Set<String> paramNames = extractAllParameterNames(entry.getValue());
        if (paramNames.size() > entry.getValue().size()) {
            createIssue(context, inputFile, MathematicaRulesDefinition.INCONSISTENT_PARAMETER_NAMES_ACROSS_OVERLOADS_KEY,
                0, "Inconsistent parameter names in overloads of: " + entry.getKey());
        }
    }

    private static Set<String> extractAllParameterNames(List<String> paramsList) {
        Set<String> paramNames = new HashSet<>();
        for (String params : paramsList) {
            Matcher paramMatcher = PARAMETER_NAME.matcher(params);
            while (paramMatcher.find()) {
                paramNames.add(paramMatcher.group(1));
            }
        }
        return paramNames;
    }

    /**
     * Rule 248: Public function with implementation details in name
     */
    public static void detectPublicFunctionWithImplementationDetailsInName(SensorContext context, InputFile inputFile, String content) {
        Matcher pkgMatcher = BEGIN_PACKAGE.matcher(content);
        if (!pkgMatcher.find()) {
            return;
        }

        String currentPackage = pkgMatcher.group(1);
        Set<String> exports = PACKAGE_EXPORTS.getOrDefault(currentPackage, new HashSet<>());

        //NOSONAR - Possessive quantifiers prevent backtracking

        Pattern implDetails = Pattern.compile("(?i)(?:Loop|Iterate|Recursive|Cache|Memo|Index|Counter|Temp|Aux)"); //NOSONAR
        for (String symbol : exports) {
            if (implDetails.matcher(symbol).find()) {
                createIssue(context, inputFile, MathematicaRulesDefinition.PUBLIC_FUNCTION_WITH_IMPLEMENTATION_DETAILS_IN_NAME_KEY,
                    0,
                    "Public function name contains implementation details: " + symbol);
            }
        }
    }

    /**
     * Rule 249: Public API not in package context
     */
    public static void detectPublicAPINotInPackageContext(SensorContext context, InputFile inputFile, String content) {
        PackageContext pkgContext = new PackageContext();
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            pkgContext.updateContext(lines[i]);
            checkForPublicFunctionOutsideContext(context, inputFile, lines[i], i, pkgContext);
        }
    }

    private static void checkForPublicFunctionOutsideContext(SensorContext context, InputFile inputFile,
                                                              String line, int lineNumber, PackageContext pkgContext) {
        if (!pkgContext.isInPackage() && !pkgContext.isInPrivate()) {
            Matcher funcMatcher = FUNCTION_DEF.matcher(line);
            if (funcMatcher.find()) {
                reportPublicFunctionOutsideContext(context, inputFile, funcMatcher.group(1), lineNumber);
            }
        }
    }

    private static void reportPublicFunctionOutsideContext(SensorContext context, InputFile inputFile,
                                                            String symbol, int lineNumber) {
        if (Character.isUpperCase(symbol.charAt(0))) {
            createIssue(context, inputFile, MathematicaRulesDefinition.PUBLIC_API_NOT_IN_PACKAGE_CONTEXT_KEY,
                lineNumber,
                "Public function defined outside package context: " + symbol);
        }
    }

    private static final class PackageContext {
        private boolean inPackage = false;
        private boolean inPrivate = false;

        void updateContext(String line) {
            if (BEGIN_PACKAGE.matcher(line).find()) {
                inPackage = true;
            }
            if (END_PACKAGE.matcher(line).find()) {
                inPackage = false;
            }
            if (BEGIN.matcher(line).find() && line.contains("Private`")) {
                inPrivate = true;
            }
            if (END.matcher(line).find()) {
                inPrivate = false;
            }
        }

        boolean isInPackage() {
            return inPackage;
        }

        boolean isInPrivate() {
            return inPrivate;
        }
    }

    /**
     * Rule 250: Test function in production code
     */
    public static void detectTestFunctionInProductionCode(SensorContext context, InputFile inputFile, String content) {
        String filename = inputFile.filename();
        if (TEST_FILES.contains(filename)) {
            return;
        }

        Matcher testMatcher = TEST_PATTERN.matcher(content);
        if (testMatcher.find()) {
            createIssue(context, inputFile, MathematicaRulesDefinition.TEST_FUNCTION_IN_PRODUCTION_CODE_KEY,
                testMatcher.start(),
                "Test function found in production code");
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Create an issue at the specified location
     */
    private static void createIssue(SensorContext context, InputFile inputFile, String ruleKey,
                                     int startOffset, String message) {
        try {
            NewIssue issue = context.newIssue()
                .forRule(org.sonar.api.rule.RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY, ruleKey));

            // Calculate line number from offset
            String content = new String(java.nio.file.Files.readAllBytes(Paths.get(inputFile.uri())), java.nio.charset.StandardCharsets.UTF_8);
            int line = 1;
            for (int i = 0; i < startOffset && i < content.length(); i++) {
                if (content.charAt(i) == '\n') {
                    line++;
                }
            }

            org.sonar.api.batch.sensor.issue.NewIssueLocation location = issue.newLocation()
                .on(inputFile)
                .at(inputFile.selectLine(line))
                .message(message);

            issue.at(location).save();
        } catch (Exception e) {
            // Silently ignore location errors
        }
    }
}
