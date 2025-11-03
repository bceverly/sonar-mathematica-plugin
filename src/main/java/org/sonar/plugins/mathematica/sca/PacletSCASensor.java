package org.sonar.plugins.mathematica.sca;

import java.io.IOException;
import java.util.List;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.mathematica.MathematicaLanguage;
import org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition;

/**
 * Sensor that performs Software Composition Analysis (SCA) on Mathematica paclet dependencies.
 *
 * Scans PacletInfo.wl files to identify:
 * - Vulnerable paclet dependencies
 * - Deprecated packages
 * - Outdated dependencies with known security issues
 */
public class PacletSCASensor implements Sensor {

    private static final Logger LOG = LoggerFactory.getLogger(PacletSCASensor.class);

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("Mathematica Paclet SCA")
            .onlyOnLanguage(MathematicaLanguage.KEY);
    }

    @Override
    public void execute(SensorContext context) {
        LOG.info("Starting Paclet Software Composition Analysis (SCA)...");

        FileSystem fileSystem = context.fileSystem();
        FilePredicates predicates = fileSystem.predicates();

        // Find all PacletInfo.wl files
        Iterable<InputFile> pacletFiles = fileSystem.inputFiles(
            predicates.and(
                predicates.hasLanguage(MathematicaLanguage.KEY),
                predicates.matchesPathPattern("**/PacletInfo.wl")
            )
        );

        PacletDependencyParser parser = new PacletDependencyParser();
        int filesScanned = 0;
        int vulnerabilitiesFound = 0;

        for (InputFile pacletFile : pacletFiles) {
            try {
                filesScanned++;
                String content = pacletFile.contents();
                List<PacletDependency> dependencies = parser.parsePacletInfo(content);

                LOG.debug("Found {} dependencies in {}", dependencies.size(), pacletFile.filename());

                for (PacletDependency dep : dependencies) {
                    List<PacletVulnerabilityDatabase.Vulnerability> vulns =
                        PacletVulnerabilityDatabase.checkDependency(dep);

                    if (!vulns.isEmpty()) {
                        for (PacletVulnerabilityDatabase.Vulnerability vuln : vulns) {
                            reportVulnerability(context, pacletFile, dep, vuln);
                            vulnerabilitiesFound++;
                        }
                    }
                }

            } catch (IOException e) {
                LOG.error("Failed to read {}: {}", pacletFile, e.getMessage());
            }
        }

        LOG.info("Paclet SCA complete: Scanned {} PacletInfo.wl files, found {} vulnerabilities",
                filesScanned, vulnerabilitiesFound);
    }

    /**
     * Report a vulnerability as a SonarQube issue.
     */
    private void reportVulnerability(SensorContext context, InputFile file,
                                     PacletDependency dep,
                                     PacletVulnerabilityDatabase.Vulnerability vuln) {
        try {
            NewIssue issue = context.newIssue()
                .forRule(RuleKey.of(MathematicaRulesDefinition.REPOSITORY_KEY,
                                   MathematicaRulesDefinition.VULNERABLE_DEPENDENCY_KEY));

            // Build descriptive message
            String message = String.format(
                "Vulnerable dependency: %s %s - %s (%s). %s",
                dep.getName(),
                dep.getVersionConstraint(),
                vuln.getDescription(),
                vuln.getIdentifier(),
                vuln.getRemediation()
            );

            NewIssueLocation location = issue.newLocation()
                .on(file)
                .at(file.selectLine(dep.getLineNumber()))
                .message(message);

            issue.at(location);
            issue.save();

            LOG.debug("Reported vulnerability: {} in {}:{}",
                     vuln.getIdentifier(), file.filename(), dep.getLineNumber());

        } catch (Exception e) {
            LOG.error("Failed to report vulnerability for {}: {}", dep.getName(), e.getMessage());
        }
    }
}
