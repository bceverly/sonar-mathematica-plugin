package org.sonar.plugins.mathematica;

import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

class MathematicaLanguageTest {

    @Test
    void testLanguageKey() {
        MathematicaLanguage language = new MathematicaLanguage(new MapSettings().asConfig());
        assertThat(language.getKey()).isEqualTo("mathematica");
    }

    @Test
    void testLanguageName() {
        MathematicaLanguage language = new MathematicaLanguage(new MapSettings().asConfig());
        assertThat(language.getName()).isEqualTo("Mathematica");
    }

    @Test
    void testDefaultFileSuffixes() {
        MathematicaLanguage language = new MathematicaLanguage(new MapSettings().asConfig());
        String[] suffixes = language.getFileSuffixes();
        assertThat(suffixes).containsExactlyInAnyOrder(".m", ".wl", ".wls");
    }

    @Test
    void testCustomFileSuffixes() {
        MapSettings settings = new MapSettings();
        settings.setProperty(MathematicaLanguage.FILE_SUFFIXES_KEY, ".m,.nb");

        MathematicaLanguage language = new MathematicaLanguage(settings.asConfig());
        String[] suffixes = language.getFileSuffixes();
        assertThat(suffixes).containsExactlyInAnyOrder(".m", ".nb");
    }

    @Test
    void testPublishAllFiles() {
        MathematicaLanguage language = new MathematicaLanguage(new MapSettings().asConfig());
        assertThat(language.publishAllFiles()).isTrue();
    }
}
