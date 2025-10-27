package org.sonar.plugins.mathematica;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

/**
 * Main plugin entry point for the Mathematica SonarQube plugin.
 * This class registers all extensions (language, sensors, rules, etc.) with SonarQube.
 */
public class MathematicaPlugin implements Plugin {

    @Override
    public void define(Context context) {
        // Register the Mathematica language
        context.addExtension(MathematicaLanguage.class);

        // Register CPD (Copy-Paste Detector) tokenizer for duplication detection
        context.addExtension(MathematicaCpdTokenizer.class);

        // Add configuration properties
        context.addExtension(
            PropertyDefinition.builder(MathematicaLanguage.FILE_SUFFIXES_KEY)
                .name("File Suffixes")
                .description("Comma-separated list of file suffixes to analyze. Default: " +
                            MathematicaLanguage.DEFAULT_FILE_SUFFIXES)
                .defaultValue(MathematicaLanguage.DEFAULT_FILE_SUFFIXES)
                .category("Mathematica")
                .subCategory("General")
                .type(PropertyType.STRING)
                .build()
        );

        context.addExtension(
            PropertyDefinition.builder("sonar.cpd.mathematica.minimumTokens")
                .name("Minimum Tokens")
                .description("Minimum number of tokens for duplication detection. Default: 100")
                .defaultValue("100")
                .category("Mathematica")
                .subCategory("Duplication")
                .type(PropertyType.INTEGER)
                .build()
        );

        context.addExtension(
            PropertyDefinition.builder("sonar.cpd.mathematica.minimumLines")
                .name("Minimum Lines")
                .description("Minimum number of lines for duplication detection. Default: 10")
                .defaultValue("10")
                .category("Mathematica")
                .subCategory("Duplication")
                .type(PropertyType.INTEGER)
                .build()
        );
    }
}
