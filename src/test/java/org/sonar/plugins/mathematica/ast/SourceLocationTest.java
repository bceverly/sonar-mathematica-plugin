package org.sonar.plugins.mathematica.ast;

import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class SourceLocationTest {

    @Test
    void testCreation() {
        SourceLocation location = new SourceLocation(1, 5, 3, 10);

        assertThat(location.getStartLine()).isEqualTo(1);
        assertThat(location.getStartColumn()).isEqualTo(5);
        assertThat(location.getEndLine()).isEqualTo(3);
        assertThat(location.getEndColumn()).isEqualTo(10);
    }

    @Test
    void testSingleLineLocation() {
        SourceLocation location = new SourceLocation(5, 10, 5, 25);

        assertThat(location.getStartLine()).isEqualTo(location.getEndLine());
        assertThat(location.getStartColumn()).isEqualTo(10);
        assertThat(location.getEndColumn()).isEqualTo(25);
    }

    @Test
    void testZeroValues() {
        SourceLocation location = new SourceLocation(0, 0, 0, 0);

        assertThat(location.getStartLine()).isZero();
        assertThat(location.getStartColumn()).isZero();
        assertThat(location.getEndLine()).isZero();
        assertThat(location.getEndColumn()).isZero();
    }
}
