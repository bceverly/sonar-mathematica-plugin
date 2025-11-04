package org.sonar.plugins.mathematica.sca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PacletDependencyTest {

    @Test
    void testConstructorWithVersionOnly() {
        PacletDependency dep = new PacletDependency("TestPaclet", "1.0.0", 5);

        assertThat(dep.getName()).isEqualTo("TestPaclet");
        assertThat(dep.getVersion()).isEqualTo("1.0.0");
        assertThat(dep.getVersionConstraint()).isEqualTo("1.0.0");
        assertThat(dep.getLineNumber()).isEqualTo(5);
    }

    @Test
    void testConstructorWithVersionAndConstraint() {
        PacletDependency dep = new PacletDependency("TestPaclet", "1.0.0", ">=1.0.0", 10);

        assertThat(dep.getName()).isEqualTo("TestPaclet");
        assertThat(dep.getVersion()).isEqualTo("1.0.0");
        assertThat(dep.getVersionConstraint()).isEqualTo(">=1.0.0");
        assertThat(dep.getLineNumber()).isEqualTo(10);
    }

    @Test
    void testToString() {
        PacletDependency dep = new PacletDependency("MyPaclet", "2.5.1", "^2.0.0", 15);

        String result = dep.toString();

        assertThat(result)
            .contains("MyPaclet")
            .contains("^2.0.0");
    }

    @Test
    void testToStringWithSimpleVersion() {
        PacletDependency dep = new PacletDependency("SimplePaclet", "1.0.0", 20);

        String result = dep.toString();

        assertThat(result).isEqualTo("SimplePaclet 1.0.0");
    }

    @Test
    void testGettersReturnCorrectValues() {
        String name = "ComplexPaclet";
        String version = "3.2.1";
        String constraint = "~3.2.0";
        int line = 42;

        PacletDependency dep = new PacletDependency(name, version, constraint, line);

        assertThat(dep.getName()).isSameAs(name);
        assertThat(dep.getVersion()).isSameAs(version);
        assertThat(dep.getVersionConstraint()).isSameAs(constraint);
        assertThat(dep.getLineNumber()).isEqualTo(line);
    }

    @Test
    void testDifferentLineNumbers() {
        PacletDependency dep1 = new PacletDependency("Paclet1", "1.0.0", 1);
        PacletDependency dep2 = new PacletDependency("Paclet2", "2.0.0", 100);
        PacletDependency dep3 = new PacletDependency("Paclet3", "3.0.0", 9999);

        assertThat(dep1.getLineNumber()).isEqualTo(1);
        assertThat(dep2.getLineNumber()).isEqualTo(100);
        assertThat(dep3.getLineNumber()).isEqualTo(9999);
    }

    @Test
    void testNullSafetyForName() {
        PacletDependency dep = new PacletDependency(null, "1.0.0", 5);

        assertThat(dep.getName()).isNull();
        assertThat(dep.toString()).contains("null");
    }

    @Test
    void testNullSafetyForVersion() {
        PacletDependency dep = new PacletDependency("TestPaclet", null, 5);

        assertThat(dep.getVersion()).isNull();
        assertThat(dep.getVersionConstraint()).isNull();
    }

    @Test
    void testEmptyStrings() {
        PacletDependency dep = new PacletDependency("", "", "", 0);

        assertThat(dep.getName()).isEmpty();
        assertThat(dep.getVersion()).isEmpty();
        assertThat(dep.getVersionConstraint()).isEmpty();
        assertThat(dep.getLineNumber()).isZero();
    }
}
