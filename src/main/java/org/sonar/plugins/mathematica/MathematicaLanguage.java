package org.sonar.plugins.mathematica;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.Language;

/**
 * Defines the Wolfram Mathematica language for SonarQube.
 * This class registers Mathematica as a supported language and specifies
 * which file extensions are associated with it.
 */
public class MathematicaLanguage implements Language {

    public static final String KEY = "wolfralyze";
    public static final String NAME = "Mathematica";

    // Property key for configurable file suffixes
    public static final String FILE_SUFFIXES_KEY = "sonar.mathematica.file.suffixes";

    // Default file extensions for Mathematica
    // .m   - Mathematica package files
    // .wl  - Wolfram Language files
    // .wls - Wolfram Language script files
    // .nb  - Mathematica notebook files (will be basic support initially)
    public static final String DEFAULT_FILE_SUFFIXES = ".m,.wl,.wls";

    private final Configuration config;

    public MathematicaLanguage(Configuration config) {
        this.config = config;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getFileSuffixes() {
        String[] suffixes = config.getStringArray(FILE_SUFFIXES_KEY);
        if (suffixes.length == 0) {
            suffixes = DEFAULT_FILE_SUFFIXES.split(",");
        }
        return suffixes;
    }

    @Override
    public boolean publishAllFiles() {
        // Return true to publish all files, even those without issues
        return true;
    }
}
