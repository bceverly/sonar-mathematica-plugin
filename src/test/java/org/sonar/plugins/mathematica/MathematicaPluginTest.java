package org.sonar.plugins.mathematica;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MathematicaPluginTest {

    @Test
    void testPluginDefinition() {
        SonarRuntime runtime = mock(SonarRuntime.class);
        when(runtime.getApiVersion()).thenReturn(Version.create(9, 9));
        when(runtime.getEdition()).thenReturn(SonarEdition.COMMUNITY);
        when(runtime.getSonarQubeSide()).thenReturn(SonarQubeSide.SCANNER);

        Plugin.Context context = new Plugin.Context(runtime);
        new MathematicaPlugin().define(context);

        // Verify that extensions are registered
        assertThat(context.getExtensions()).isNotEmpty();
        assertThat(context.getExtensions()).hasSizeGreaterThan(3);
    }
}
