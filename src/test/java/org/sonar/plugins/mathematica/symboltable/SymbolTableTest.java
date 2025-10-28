package org.sonar.plugins.mathematica.symboltable;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for symbol table core classes.
 */
public class SymbolTableTest {

    private InputFile createMockFile(String filename) {
        InputFile file = mock(InputFile.class);
        when(file.filename()).thenReturn(filename);
        return file;
    }

    @Test
    public void testSymbolCreation() {
        Scope globalScope = new Scope(ScopeType.GLOBAL, 1, 100, null);
        Symbol symbol = new Symbol("x", 10, globalScope, false, true);

        assertEquals("x", symbol.getName());
        assertEquals(10, symbol.getDeclarationLine());
        assertEquals(globalScope, symbol.getScope());
        assertTrue(symbol.isModuleVariable());
        assertFalse(symbol.isParameter());
    }

    @Test
    public void testSymbolReference() {
        SymbolReference ref = new SymbolReference(10, 5, ReferenceType.WRITE, "x = 5");

        assertEquals(10, ref.getLine());
        assertEquals(5, ref.getColumn());
        assertEquals(ReferenceType.WRITE, ref.getType());
        assertTrue(ref.isWrite());
        assertFalse(ref.isRead());
    }

    @Test
    public void testScopeHierarchy() {
        Scope globalScope = new Scope(ScopeType.GLOBAL, 1, 100, null);
        Scope moduleScope = new Scope(ScopeType.MODULE, 10, 50, globalScope);
        Scope blockScope = new Scope(ScopeType.BLOCK, 20, 40, moduleScope);

        assertEquals(0, globalScope.getDepth());
        assertEquals(1, moduleScope.getDepth());
        assertEquals(2, blockScope.getDepth());

        assertEquals(1, globalScope.getChildren().size());
        assertEquals(moduleScope, globalScope.getChildren().get(0));
    }

    @Test
    public void testSymbolLookup() {
        Scope globalScope = new Scope(ScopeType.GLOBAL, 1, 100, null);
        Scope moduleScope = new Scope(ScopeType.MODULE, 10, 50, globalScope);

        Symbol globalX = new Symbol("x", 5, globalScope, false, false);
        Symbol moduleX = new Symbol("x", 15, moduleScope, false, true);

        globalScope.addSymbol(globalX);
        moduleScope.addSymbol(moduleX);

        // Module scope should find its own x
        assertEquals(moduleX, moduleScope.getSymbol("x"));
        assertEquals(moduleX, moduleScope.resolveSymbol("x"));

        // Global scope should find its x
        assertEquals(globalX, globalScope.getSymbol("x"));
        assertEquals(globalX, globalScope.resolveSymbol("x"));

        // Looking up unknown symbol from module should check parent
        Symbol globalY = new Symbol("y", 6, globalScope, false, false);
        globalScope.addSymbol(globalY);
        assertNull(moduleScope.getSymbol("y")); // Not in module scope directly
        assertEquals(globalY, moduleScope.resolveSymbol("y")); // But found in parent
    }

    @Test
    public void testSymbolTable() {
        InputFile file = createMockFile("test.m");

        SymbolTable table = new SymbolTable(file, 100);
        Scope globalScope = table.getGlobalScope();

        Symbol x = new Symbol("x", 10, globalScope, false, false);
        globalScope.addSymbol(x);
        table.addSymbol(x);

        assertEquals(1, table.getAllSymbols().size());
        assertEquals(1, table.getSymbolsByName("x").size());
        assertEquals(x, table.getSymbolAtLocation("x", 10));
    }

    @Test
    public void testUnusedSymbol() {
        Scope globalScope = new Scope(ScopeType.GLOBAL, 1, 100, null);
        Symbol symbol = new Symbol("unused", 10, globalScope, false, true);

        // Initially: no assignments, no references
        assertTrue(symbol.isUnused());
        assertFalse(symbol.isAssignedButNeverRead());
        assertFalse(symbol.isReadButNeverAssigned());

        // Add assignment - symbol is still "unused" (not read), but assigned
        symbol.addAssignment(new SymbolReference(10, 0, ReferenceType.WRITE, "unused = 5"));
        assertTrue(symbol.isUnused()); // Still unused (not read)
        assertTrue(symbol.isAssignedButNeverRead()); // Assigned but not read

        // Add reference (read) - now the symbol is used
        symbol.addReference(new SymbolReference(15, 0, ReferenceType.READ, "Print[unused]"));
        assertFalse(symbol.isUnused()); // Now used (read)
        assertFalse(symbol.isAssignedButNeverRead()); // Assigned and read
    }

    @Test
    public void testShadowingDetection() {
        InputFile file = createMockFile("test.m");

        SymbolTable table = new SymbolTable(file, 100);
        Scope globalScope = table.getGlobalScope();
        Scope moduleScope = new Scope(ScopeType.MODULE, 10, 50, globalScope);

        Symbol globalX = new Symbol("x", 5, globalScope, false, false);
        Symbol moduleX = new Symbol("x", 15, moduleScope, false, true);

        globalScope.addSymbol(globalX);
        moduleScope.addSymbol(moduleX);
        table.addSymbol(globalX);
        table.addSymbol(moduleX);

        List<SymbolTable.ShadowingPair> shadowing = table.findShadowingIssues();
        assertEquals(1, shadowing.size());
        assertEquals(moduleX, shadowing.get(0).getInner());
        assertEquals(globalX, shadowing.get(0).getOuter());
    }

    @Test
    public void testScopeContainsLine() {
        Scope scope = new Scope(ScopeType.MODULE, 10, 50, null);

        assertFalse(scope.containsLine(5));
        assertTrue(scope.containsLine(10));
        assertTrue(scope.containsLine(30));
        assertTrue(scope.containsLine(50));
        assertFalse(scope.containsLine(51));
    }

    @Test
    public void testGetScopeAtLine() {
        Scope globalScope = new Scope(ScopeType.GLOBAL, 1, 100, null);
        Scope moduleScope = new Scope(ScopeType.MODULE, 10, 50, globalScope);
        Scope blockScope = new Scope(ScopeType.BLOCK, 20, 30, moduleScope);

        // Line 5 is in global only
        assertEquals(globalScope, globalScope.getScopeAtLine(5));

        // Line 15 is in module (not block)
        assertEquals(moduleScope, globalScope.getScopeAtLine(15));

        // Line 25 is in block (innermost)
        assertEquals(blockScope, globalScope.getScopeAtLine(25));

        // Line 60 is in global only
        assertEquals(globalScope, globalScope.getScopeAtLine(60));
    }
}
