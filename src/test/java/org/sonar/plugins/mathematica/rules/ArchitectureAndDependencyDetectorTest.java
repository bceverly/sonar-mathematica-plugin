package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ArchitectureAndDependencyDetectorTest {

    @Test
    void testInitializeCaches() {
        // Call the public static method to exercise the code
        assertThatCode(() -> ArchitectureAndDependencyDetector.initializeCaches())
            .doesNotThrowAnyException();
    }

    @Test
    void testClearCaches() {
        // Initialize first
        ArchitectureAndDependencyDetector.initializeCaches();

        // Then clear
        assertThatCode(() -> ArchitectureAndDependencyDetector.clearCaches())
            .doesNotThrowAnyException();
    }

    @Test
    void testInitializeAndClearSequence() {
        // Test the full lifecycle
        ArchitectureAndDependencyDetector.initializeCaches();
        ArchitectureAndDependencyDetector.clearCaches();
        ArchitectureAndDependencyDetector.initializeCaches();
        ArchitectureAndDependencyDetector.clearCaches();
    }

    @Test
    void testGetPackageDependenciesSize() {
        // Test the getter methods for performance tracking
        ArchitectureAndDependencyDetector.initializeCaches();

        int size = ArchitectureAndDependencyDetector.getPackageDependenciesSize();

        assertThatCode(() -> {
            int s = ArchitectureAndDependencyDetector.getPackageDependenciesSize();
        }).doesNotThrowAnyException();

        ArchitectureAndDependencyDetector.clearCaches();
    }

    @Test
    void testGetSymbolDefinitionsSize() {
        // Test the getter methods for performance tracking
        ArchitectureAndDependencyDetector.initializeCaches();

        int size = ArchitectureAndDependencyDetector.getSymbolDefinitionsSize();

        assertThatCode(() -> {
            int s = ArchitectureAndDependencyDetector.getSymbolDefinitionsSize();
        }).doesNotThrowAnyException();

        ArchitectureAndDependencyDetector.clearCaches();
    }

    @Test
    void testGetSymbolUsagesSize() {
        // Test the getter method for symbol usages - covers the uncovered line!
        ArchitectureAndDependencyDetector.initializeCaches();

        int size = ArchitectureAndDependencyDetector.getSymbolUsagesSize();

        assertThatCode(() -> {
            int s = ArchitectureAndDependencyDetector.getSymbolUsagesSize();
        }).doesNotThrowAnyException();

        ArchitectureAndDependencyDetector.clearCaches();
    }
}
