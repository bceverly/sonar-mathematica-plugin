package org.sonar.plugins.mathematica.rules;

import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.VULNERABLE_DEPENDENCY_KEY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TAG_SECURITY;
import static org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition.TIME_20MIN;

/**
 * Software Composition Analysis (SCA) Rules definitions.
 * Detects vulnerable Mathematica paclet dependencies.
 */
final class SCADependencyRulesDefinition {

    private SCADependencyRulesDefinition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Define all SCA rules.
     */
    static void defineRules(NewRepository repository) {

        // Define the vulnerable dependency rule
        NewRule vulnerableDependencyRule = repository.createRule(VULNERABLE_DEPENDENCY_KEY)
            .setName("Dependencies with known vulnerabilities should not be used")
            .setHtmlDescription(
                "<p>Using paclet dependencies with known security vulnerabilities exposes your application to potential attacks.</p>"
                + "<p>Dependencies should be kept up-to-date and replaced if they contain unpatched security issues.</p>"
                + "<h2>What's at Risk</h2>"
                + "<ul>"
                + "<li><strong>HIGH severity:</strong> SQL injection, path traversal, SSL/TLS validation bypasses, remote code execution</li>"
                + "<li><strong>MEDIUM severity:</strong> Weak cryptography, insecure default permissions, denial of service</li>"
                + "<li><strong>LOW severity:</strong> Deprecated packages with better alternatives available</li>"
                + "</ul>"
                + "<h2>Noncompliant Code Example</h2>"
                + "<pre>\n"
                + "Paclet[\n"
                + "  Name -> \"MyPaclet\",\n"
                + "  Version -> \"1.0.0\",\n"
                + "  Dependencies -> {\n"
                + "    \"HTTPClient\" -> \"1.5\",  (* Vulnerable to SSL validation bypass *)\n"
                + "    \"DatabaseLink\" -> \"8.0\", (* SQL injection risk *)\n"
                + "    \"CryptoUtils\" -> \"1.0\"  (* Uses weak MD5/SHA1 *)\n"
                + "  }\n"
                + "]\n"
                + "</pre>"
                + "<h2>Compliant Solution</h2>"
                + "<pre>\n"
                + "Paclet[\n"
                + "  Name -> \"MyPaclet\",\n"
                + "  Version -> \"1.0.0\",\n"
                + "  Dependencies -> {\n"
                + "    \"HTTPClient\" -> \"2.0+\",    (* Fixed SSL validation *)\n"
                + "    \"DatabaseLink\" -> \"9.0+\",  (* Parameterized queries *)\n"
                + "    \"CryptoUtils\" -> \"1.5+\"   (* Modern SHA256/SHA512 *)\n"
                + "  }\n"
                + "]\n"
                + "</pre>"
                + "<h2>See Also</h2>"
                + "<ul>"
                + "<li>OWASP Top 10 2021 - A06:2021 Vulnerable and Outdated Components</li>"
                + "<li>CWE-1035 - Using Components with Known Vulnerabilities</li>"
                + "<li>Wolfram Security Bulletins</li>"
                + "</ul>"
            )
            .addDefaultImpact(SoftwareQuality.SECURITY, Severity.HIGH)
            .setTags(TAG_SECURITY, "dependency", "sca", "cwe-1035", "owasp-a06");

        vulnerableDependencyRule.setDebtRemediationFunction(
            vulnerableDependencyRule.debtRemediationFunctions().constantPerIssue(TIME_20MIN));
    }
}
