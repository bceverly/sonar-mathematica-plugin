package org.sonar.plugins.mathematica;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.mathematica.metrics.MathematicaMetricsSensor;
import org.sonar.plugins.mathematica.rules.MathematicaRulesDefinition;
import org.sonar.plugins.mathematica.rules.MathematicaRulesSensor;

/**
 * Main plugin entry point for the Mathematica SonarQube plugin.
 * This class registers all extensions (language, sensors, rules, etc.) with SonarQube.
 */
public class MathematicaPlugin implements Plugin {

    @Override
    public void define(Context context) {
        // Register the Mathematica language
        context.addExtension(MathematicaLanguage.class);

        // Register quality profile (required - every language must have at least one)
        context.addExtension(MathematicaQualityProfile.class);

        // Register rules and rules sensor
        context.addExtension(MathematicaRulesDefinition.class);
        context.addExtension(MathematicaRulesSensor.class);

        // Register metrics sensor for complexity and other metrics
        context.addExtension(MathematicaMetricsSensor.class);

        // Register CPD (Copy-Paste Detector) tokenizer for duplication detection
        context.addExtension(MathematicaCpdTokenizer.class);

        // Add configuration properties
        context.addExtension(
            PropertyDefinition.builder(MathematicaLanguage.FILE_SUFFIXES_KEY)
                .name("File Suffixes")
                .description("Comma-separated list of file suffixes to analyze. Default: "
                    + MathematicaLanguage.DEFAULT_FILE_SUFFIXES)
                .defaultValue(MathematicaLanguage.DEFAULT_FILE_SUFFIXES)
                .category("Mathematica")
                .subCategory("General")
                .multiValues(true)
                .build()
        );

        context.addExtension(
            PropertyDefinition.builder("sonar.cpd.mathematica.minimumTokens")
                .name("Minimum Tokens")
                .description("Minimum number of tokens for duplication detection. Default: 250")
                .defaultValue("250")
                .category("Mathematica")
                .subCategory("Duplication")
                .type(PropertyType.INTEGER)
                .build()
        );

        context.addExtension(
            PropertyDefinition.builder("sonar.cpd.mathematica.minimumLines")
                .name("Minimum Lines")
                .description("Minimum number of lines for duplication detection. Default: 25")
                .defaultValue("25")
                .category("Mathematica")
                .subCategory("Duplication")
                .type(PropertyType.INTEGER)
                .build()
        );

        context.addExtension(
            PropertyDefinition.builder("sonar.mathematica.function.maximumLines")
                .name("Maximum Function Length")
                .description("Maximum number of lines allowed in a function. Default: 150")
                .defaultValue("150")
                .category("Mathematica")
                .subCategory("Code Quality")
                .type(PropertyType.INTEGER)
                .build()
        );

        context.addExtension(
            PropertyDefinition.builder("sonar.mathematica.file.maximumLines")
                .name("Maximum File Length")
                .description("Maximum number of lines allowed in a file. Default: 1000")
                .defaultValue("1000")
                .category("Mathematica")
                .subCategory("Code Quality")
                .type(PropertyType.INTEGER)
                .build()
        );
    }
}
