package org.sonar.plugins.mathematica;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

class MathematicaPluginTest {

    @Test
    void testPluginDefinition() {
        SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(
            Version.create(9, 9),
            SonarQubeSide.SCANNER,
            SonarEdition.COMMUNITY
        );

        Plugin.Context context = new Plugin.Context(runtime);
        new MathematicaPlugin().define(context);

        // Verify that extensions are registered
        assertThat(context.getExtensions()).isNotEmpty();
        assertThat(context.getExtensions()).hasSizeGreaterThan(3);
    }
}
