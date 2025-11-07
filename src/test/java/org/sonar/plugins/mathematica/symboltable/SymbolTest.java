package org.sonar.plugins.mathematica.symboltable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive unit tests for Symbol.
 * Target: >80% coverage (currently 60.8%)
 */
class SymbolTest {

    private Scope globalScope;
    private Scope moduleScope;

    @BeforeEach
    void setUp() {
        globalScope = new Scope(ScopeType.GLOBAL, 1, 100, null);
        moduleScope = new Scope(ScopeType.MODULE, 10, 50, globalScope);
    }

    @Test
    void testConstructorBasicSymbol() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertEquals("x", symbol.getName());
        assertEquals(10, symbol.getDeclarationLine());
        assertEquals(globalScope, symbol.getScope());
        assertFalse(symbol.isParameter());
        assertFalse(symbol.isModuleVariable());
    }

    @Test
    void testConstructorParameterSymbol() {
        Symbol symbol = new Symbol("param", 5, globalScope, true, false);

        assertTrue(symbol.isParameter());
        assertFalse(symbol.isModuleVariable());
    }

    @Test
    void testConstructorModuleVariableSymbol() {
        Symbol symbol = new Symbol("local", 15, moduleScope, false, true);

        assertFalse(symbol.isParameter());
        assertTrue(symbol.isModuleVariable());
    }

    @Test
    void testAddAssignmentValidWrite() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference writeRef = new SymbolReference(12, 0, ReferenceType.WRITE, "x = 5");

        symbol.addAssignment(writeRef);

        assertEquals(1, symbol.getAssignments().size());
        assertEquals(writeRef, symbol.getAssignments().get(0));
    }

    @Test
    void testAddAssignmentValidReadWrite() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference readWriteRef = new SymbolReference(12, 0, ReferenceType.READ_WRITE, "x += 5");

        symbol.addAssignment(readWriteRef);

        assertEquals(1, symbol.getAssignments().size());
    }

    @Test
    void testAddAssignmentThrowsOnRead() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference readRef = new SymbolReference(12, 0, ReferenceType.READ, "x");

        assertThrows(IllegalArgumentException.class, () -> symbol.addAssignment(readRef));
    }

    @Test
    void testAddReferenceValidRead() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference readRef = new SymbolReference(15, 2, ReferenceType.READ, "Print[x]");

        symbol.addReference(readRef);

        assertEquals(1, symbol.getReferences().size());
        assertEquals(readRef, symbol.getReferences().get(0));
    }

    @Test
    void testAddReferenceValidReadWrite() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference readWriteRef = new SymbolReference(15, 2, ReferenceType.READ_WRITE, "x++");

        symbol.addReference(readWriteRef);

        assertEquals(1, symbol.getReferences().size());
    }

    @Test
    void testAddReferenceThrowsOnWrite() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference writeRef = new SymbolReference(15, 2, ReferenceType.WRITE, "x = 5");

        assertThrows(IllegalArgumentException.class, () -> symbol.addReference(writeRef));
    }

    @Test
    void testHasReferenceAtReturnsFalseInitially() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertFalse(symbol.hasReferenceAt(15, 2));
    }

    @Test
    void testHasReferenceAtReturnsTrueAfterAdd() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference ref = new SymbolReference(15, 2, ReferenceType.READ, "x");

        symbol.addReferenceIfNew(ref);

        assertTrue(symbol.hasReferenceAt(15, 2));
    }

    @Test
    void testAddReferenceIfNewAddFirstTime() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference ref = new SymbolReference(15, 2, ReferenceType.READ, "x");

        boolean added = symbol.addReferenceIfNew(ref);

        assertTrue(added);
        assertEquals(1, symbol.getReferences().size());
    }

    @Test
    void testAddReferenceIfNewSkipDuplicate() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference ref1 = new SymbolReference(15, 2, ReferenceType.READ, "x");
        SymbolReference ref2 = new SymbolReference(15, 2, ReferenceType.READ, "x + y");

        symbol.addReferenceIfNew(ref1);
        boolean added = symbol.addReferenceIfNew(ref2);

        assertFalse(added);
        assertEquals(1, symbol.getReferences().size());
    }

    @Test
    void testAddReferenceIfNewThrowsOnWrite() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        SymbolReference writeRef = new SymbolReference(15, 2, ReferenceType.WRITE, "x = 5");

        assertThrows(IllegalArgumentException.class, () -> symbol.addReferenceIfNew(writeRef));
    }

    @Test
    void testIsUnusedTrueWhenNoReferences() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertTrue(symbol.isUnused());
    }

    @Test
    void testIsUnusedFalseWhenHasReferences() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "x"));

        assertFalse(symbol.isUnused());
    }

    @Test
    void testIsAssignedButNeverReadTrueWhenOnlyAssignments() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        symbol.addAssignment(new SymbolReference(12, 0, ReferenceType.WRITE, "x = 5"));

        assertTrue(symbol.isAssignedButNeverRead());
    }

    @Test
    void testIsAssignedButNeverReadFalseWhenHasReads() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        symbol.addAssignment(new SymbolReference(12, 0, ReferenceType.WRITE, "x = 5"));
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "x"));

        assertFalse(symbol.isAssignedButNeverRead());
    }

    @Test
    void testIsAssignedButNeverReadFalseWhenNoAssignments() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertFalse(symbol.isAssignedButNeverRead());
    }

    @Test
    void testIsReadButNeverAssignedTrueWhenOnlyReads() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "x"));

        assertTrue(symbol.isReadButNeverAssigned());
    }

    @Test
    void testIsReadButNeverAssignedFalseWhenHasAssignments() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        symbol.addAssignment(new SymbolReference(12, 0, ReferenceType.WRITE, "x = 5"));
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "x"));

        assertFalse(symbol.isReadButNeverAssigned());
    }

    @Test
    void testIsReadButNeverAssignedFalseWhenNoReferences() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertFalse(symbol.isReadButNeverAssigned());
    }

    @Test
    void testGetAllReferencesSorted() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        symbol.addAssignment(new SymbolReference(20, 0, ReferenceType.WRITE, "x = 5"));
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "Print[x]"));
        symbol.addAssignment(new SymbolReference(12, 0, ReferenceType.WRITE, "x = 3"));
        symbol.addReference(new SymbolReference(25, 0, ReferenceType.READ, "x + 1"));

        List<SymbolReference> all = symbol.getAllReferencesSorted();

        assertEquals(4, all.size());
        assertEquals(12, all.get(0).getLine());
        assertEquals(15, all.get(1).getLine());
        assertEquals(20, all.get(2).getLine());
        assertEquals(25, all.get(3).getLine());
    }

    @Test
    void testGetAssignmentsReturnsUnmodifiableList() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        symbol.addAssignment(new SymbolReference(12, 0, ReferenceType.WRITE, "x = 5"));

        List<SymbolReference> assignments = symbol.getAssignments();
        SymbolReference newRef = new SymbolReference(15, 0, ReferenceType.WRITE, "x = 10");

        assertThrows(UnsupportedOperationException.class, () -> assignments.add(newRef));
    }

    @Test
    void testGetReferencesReturnsUnmodifiableList() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "x"));

        List<SymbolReference> references = symbol.getReferences();
        SymbolReference newRef = new SymbolReference(20, 0, ReferenceType.READ, "x + 1");

        assertThrows(UnsupportedOperationException.class, () -> references.add(newRef));
    }

    @Test
    void testToString() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        String str = symbol.toString();

        assertTrue(str.contains("x"));
        assertTrue(str.contains("line=10"));
        assertTrue(str.contains("scope=GLOBAL"));
    }

    @Test
    void testEqualsSameObject() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertEquals(symbol, symbol);
    }

    @Test
    void testEqualsNullObject() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertNotEquals(null, symbol);
    }

    @Test
    void testEqualsDifferentClass() {
        Symbol symbol = new Symbol("x", 10, globalScope, false, false);

        assertNotEquals("not a symbol", symbol);
    }

    @Test
    void testEqualsSameNameAndScope() {
        Symbol symbol1 = new Symbol("x", 10, globalScope, false, false);
        Symbol symbol2 = new Symbol("x", 20, globalScope, true, true); // Different line, params, but same name and scope

        assertEquals(symbol1, symbol2);
    }

    @Test
    void testEqualsDifferentName() {
        Symbol symbol1 = new Symbol("x", 10, globalScope, false, false);
        Symbol symbol2 = new Symbol("y", 10, globalScope, false, false);

        assertNotEquals(symbol1, symbol2);
    }

    @Test
    void testEqualsDifferentScope() {
        Symbol symbol1 = new Symbol("x", 10, globalScope, false, false);
        Symbol symbol2 = new Symbol("x", 10, moduleScope, false, false);

        assertNotEquals(symbol1, symbol2);
    }

    @Test
    void testHashCodeSameForEqualObjects() {
        Symbol symbol1 = new Symbol("x", 10, globalScope, false, false);
        Symbol symbol2 = new Symbol("x", 20, globalScope, true, true);

        assertEquals(symbol1.hashCode(), symbol2.hashCode());
    }

    @Test
    void testHashCodeDifferentForDifferentObjects() {
        Symbol symbol1 = new Symbol("x", 10, globalScope, false, false);
        Symbol symbol2 = new Symbol("y", 10, globalScope, false, false);

        assertNotEquals(symbol1.hashCode(), symbol2.hashCode());
    }

    @Test
    void testMultipleAssignmentsAndReferences() {
        Symbol symbol = new Symbol("counter", 5, globalScope, false, true);

        // Add multiple assignments and references
        symbol.addAssignment(new SymbolReference(10, 0, ReferenceType.WRITE, "counter = 0"));
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "counter < 10"));
        symbol.addAssignment(new SymbolReference(20, 0, ReferenceType.READ_WRITE, "counter++"));
        symbol.addReference(new SymbolReference(25, 0, ReferenceType.READ, "Print[counter]"));

        assertEquals(2, symbol.getAssignments().size());
        assertEquals(2, symbol.getReferences().size());
        assertFalse(symbol.isUnused());
        assertFalse(symbol.isAssignedButNeverRead());
        assertFalse(symbol.isReadButNeverAssigned());
    }
}
