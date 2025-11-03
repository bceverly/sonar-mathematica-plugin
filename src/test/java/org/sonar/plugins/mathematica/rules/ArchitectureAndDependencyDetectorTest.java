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
}
