package org.sonar.plugins.mathematica.symboltable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive unit tests for SymbolReference.
 * Target: >80% coverage (currently 36.4%)
 */
class SymbolReferenceTest {

    @Test
    void testConstructor() {
        SymbolReference ref = new SymbolReference(10, 5, ReferenceType.READ, "x + y");

        assertEquals(10, ref.getLine());
        assertEquals(5, ref.getColumn());
        assertEquals(ReferenceType.READ, ref.getType());
        assertEquals("x + y", ref.getContext());
    }

    @Test
    void testIsReadForReadType() {
        SymbolReference ref = new SymbolReference(1, 0, ReferenceType.READ, "");

        assertTrue(ref.isRead());
        assertFalse(ref.isWrite());
    }

    @Test
    void testIsWriteForWriteType() {
        SymbolReference ref = new SymbolReference(1, 0, ReferenceType.WRITE, "");

        assertTrue(ref.isWrite());
        assertFalse(ref.isRead());
    }

    @Test
    void testIsReadWriteForReadWriteType() {
        SymbolReference ref = new SymbolReference(1, 0, ReferenceType.READ_WRITE, "");

        assertTrue(ref.isRead());
        assertTrue(ref.isWrite());
    }

    @Test
    void testToString() {
        SymbolReference ref = new SymbolReference(10, 5, ReferenceType.READ, "x + y");

        String str = ref.toString();

        assertTrue(str.contains("line=10"));
        assertTrue(str.contains("col=5"));
        assertTrue(str.contains("type=READ"));
        assertTrue(str.contains("context='x + y'"));
    }

    @Test
    void testEqualsSameObject() {
        SymbolReference ref = new SymbolReference(10, 5, ReferenceType.READ, "x");

        assertEquals(ref, ref);
    }

    @Test
    void testEqualsNullObject() {
        SymbolReference ref = new SymbolReference(10, 5, ReferenceType.READ, "x");

        assertNotEquals(null, ref);
    }

    @Test
    void testEqualsDifferentClass() {
        SymbolReference ref = new SymbolReference(10, 5, ReferenceType.READ, "x");

        assertNotEquals("not a symbol reference", ref);
    }

    @Test
    void testEqualsSameValues() {
        SymbolReference ref1 = new SymbolReference(10, 5, ReferenceType.READ, "x");
        SymbolReference ref2 = new SymbolReference(10, 5, ReferenceType.READ, "x");

        assertEquals(ref1, ref2);
    }

    @Test
    void testEqualsDifferentLine() {
        SymbolReference ref1 = new SymbolReference(10, 5, ReferenceType.READ, "x");
        SymbolReference ref2 = new SymbolReference(11, 5, ReferenceType.READ, "x");

        assertNotEquals(ref1, ref2);
    }

    @Test
    void testEqualsDifferentColumn() {
        SymbolReference ref1 = new SymbolReference(10, 5, ReferenceType.READ, "x");
        SymbolReference ref2 = new SymbolReference(10, 6, ReferenceType.READ, "x");

        assertNotEquals(ref1, ref2);
    }

    @Test
    void testEqualsDifferentType() {
        SymbolReference ref1 = new SymbolReference(10, 5, ReferenceType.READ, "x");
        SymbolReference ref2 = new SymbolReference(10, 5, ReferenceType.WRITE, "x");

        assertNotEquals(ref1, ref2);
    }

    @Test
    void testEqualsDifferentContextStillEqual() {
        // Context is not part of equals comparison
        SymbolReference ref1 = new SymbolReference(10, 5, ReferenceType.READ, "x + y");
        SymbolReference ref2 = new SymbolReference(10, 5, ReferenceType.READ, "x + z");

        assertEquals(ref1, ref2);
    }

    @Test
    void testHashCodeSameForEqualObjects() {
        SymbolReference ref1 = new SymbolReference(10, 5, ReferenceType.READ, "x");
        SymbolReference ref2 = new SymbolReference(10, 5, ReferenceType.READ, "x");

        assertEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    void testHashCodeDifferentForDifferentObjects() {
        SymbolReference ref1 = new SymbolReference(10, 5, ReferenceType.READ, "x");
        SymbolReference ref2 = new SymbolReference(11, 5, ReferenceType.READ, "x");

        assertNotEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    void testAllReferenceTypes() {
        for (ReferenceType type : ReferenceType.values()) {
            SymbolReference ref = new SymbolReference(1, 0, type, "test");
            assertEquals(type, ref.getType());
        }
    }
}
