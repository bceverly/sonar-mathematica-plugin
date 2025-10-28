package org.sonar.plugins.mathematica;

import org.junit.jupiter.api.Test;
import org.sonar.api.config.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MathematicaLanguageTest {

    @Test
    void testLanguageKey() {
        Configuration config = mock(Configuration.class);
        MathematicaLanguage language = new MathematicaLanguage(config);
        assertThat(language.getKey()).isEqualTo("mathematica");
    }

    @Test
    void testLanguageName() {
        Configuration config = mock(Configuration.class);
        MathematicaLanguage language = new MathematicaLanguage(config);
        assertThat(language.getName()).isEqualTo("Mathematica");
    }

    @Test
    void testDefaultFileSuffixes() {
        Configuration config = mock(Configuration.class);
        when(config.getStringArray(MathematicaLanguage.FILE_SUFFIXES_KEY))
            .thenReturn(new String[]{".m", ".wl", ".wls"});

        MathematicaLanguage language = new MathematicaLanguage(config);
        String[] suffixes = language.getFileSuffixes();
        assertThat(suffixes).containsExactlyInAnyOrder(".m", ".wl", ".wls");
    }

    @Test
    void testCustomFileSuffixes() {
        Configuration config = mock(Configuration.class);
        when(config.getStringArray(MathematicaLanguage.FILE_SUFFIXES_KEY))
            .thenReturn(new String[]{".m", ".nb"});

        MathematicaLanguage language = new MathematicaLanguage(config);
        String[] suffixes = language.getFileSuffixes();
        assertThat(suffixes).containsExactlyInAnyOrder(".m", ".nb");
    }

    @Test
    void testPublishAllFiles() {
        Configuration config = mock(Configuration.class);
        MathematicaLanguage language = new MathematicaLanguage(config);
        assertThat(language.publishAllFiles()).isTrue();
    }
}
