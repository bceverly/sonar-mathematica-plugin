package org.sonar.plugins.mathematica.sca;

/**
 * Represents a Mathematica paclet dependency.
 */
public class PacletDependency {
    private final String name;
    private final String version;
    private final String versionConstraint;
    private final int lineNumber;

    public PacletDependency(String name, String version, int lineNumber) {
        this.name = name;
        this.version = version;
        this.versionConstraint = version;
        this.lineNumber = lineNumber;
    }

    public PacletDependency(String name, String version, String versionConstraint, int lineNumber) {
        this.name = name;
        this.version = version;
        this.versionConstraint = versionConstraint;
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionConstraint() {
        return versionConstraint;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return name + " " + versionConstraint;
    }
}
