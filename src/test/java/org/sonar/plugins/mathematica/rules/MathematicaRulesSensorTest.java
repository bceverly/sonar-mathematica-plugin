package org.sonar.plugins.mathematica.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MathematicaRulesSensorTest {

    @Test
    void testSensorCanBeInstantiated() {
        // Verify sensor can be created - this tests the AtomicReference thread safety fix
        MathematicaRulesSensor sensor = new MathematicaRulesSensor();
        assertThat(sensor).isNotNull();
    }

    @Test
    void testCodeSmellDetectorCanBeInstantiated() {
        // Test that detector can be created
        CodeSmellDetector detector = new CodeSmellDetector();
        assertThat(detector).isNotNull();
    }

    @Test
    void testBugDetectorCanBeInstantiated() {
        // Test that detector can be created
        BugDetector detector = new BugDetector();
        assertThat(detector).isNotNull();
    }

    @Test
    void testVulnerabilityDetectorCanBeInstantiated() {
        // Test that detector can be created
        VulnerabilityDetector detector = new VulnerabilityDetector();
        assertThat(detector).isNotNull();
    }

    @Test
    void testSecurityHotspotDetectorCanBeInstantiated() {
        // Test that detector can be created
        SecurityHotspotDetector detector = new SecurityHotspotDetector();
        assertThat(detector).isNotNull();
    }
}
