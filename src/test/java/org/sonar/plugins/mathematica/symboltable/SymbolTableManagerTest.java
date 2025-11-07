package org.sonar.plugins.mathematica.symboltable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for SymbolTableManager.
 * Target: >80% coverage (currently 27.3%)
 */
class SymbolTableManagerTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        // Clear state between tests
        SymbolTableManager.clear();
    }

    private InputFile createMockFile(String key, String filename, int lines) {
        InputFile file = mock(InputFile.class);
        when(file.key()).thenReturn(key);
        when(file.filename()).thenReturn(filename);
        when(file.lines()).thenReturn(lines);
        return file;
    }

    @Test
    void testConstructorThrowsException() {
        // Test utility class constructor throws exception via reflection
        Exception exception = assertThrows(Exception.class, () -> {
            // Use reflection to call private constructor
            java.lang.reflect.Constructor<SymbolTableManager> constructor =
                SymbolTableManager.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });

        // Verify the cause is UnsupportedOperationException
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    void testGetOrCreateCreatesNewTable() {
        InputFile file = createMockFile("file1", "test.wl", 100);

        SymbolTable table = SymbolTableManager.getOrCreate(file);

        assertNotNull(table);
        assertEquals(file, table.getFile());
        assertNotNull(table.getGlobalScope());
    }

    @Test
    void testGetOrCreateReturnsExistingTable() {
        InputFile file = createMockFile("file1", "test.wl", 100);

        SymbolTable table1 = SymbolTableManager.getOrCreate(file);
        SymbolTable table2 = SymbolTableManager.getOrCreate(file);

        assertSame(table1, table2);
    }

    @Test
    void testGetReturnsNullForNonExistent() {
        InputFile file = createMockFile("file1", "test.wl", 100);

        SymbolTable table = SymbolTableManager.get(file);

        assertNull(table);
    }

    @Test
    void testGetReturnsExistingTable() {
        InputFile file = createMockFile("file1", "test.wl", 100);
        SymbolTableManager.getOrCreate(file);

        SymbolTable table = SymbolTableManager.get(file);

        assertNotNull(table);
    }

    @Test
    void testGetByKeyReturnsNullForNonExistent() {
        SymbolTable table = SymbolTableManager.get("nonexistent");

        assertNull(table);
    }

    @Test
    void testGetByKeyReturnsExistingTable() {
        InputFile file = createMockFile("file1", "test.wl", 100);
        SymbolTableManager.getOrCreate(file);

        SymbolTable table = SymbolTableManager.get("file1");

        assertNotNull(table);
    }

    @Test
    void testHasReturnsFalseForNonExistent() {
        InputFile file = createMockFile("file1", "test.wl", 100);

        assertFalse(SymbolTableManager.has(file));
    }

    @Test
    void testHasReturnsTrueForExisting() {
        InputFile file = createMockFile("file1", "test.wl", 100);
        SymbolTableManager.getOrCreate(file);

        assertTrue(SymbolTableManager.has(file));
    }

    @Test
    void testGetAllReturnsEmptyMap() {
        Map<String, SymbolTable> all = SymbolTableManager.getAll();

        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void testGetAllReturnsAllTables() {
        InputFile file1 = createMockFile("file1", "test1.wl", 100);
        InputFile file2 = createMockFile("file2", "test2.wl", 200);

        SymbolTableManager.getOrCreate(file1);
        SymbolTableManager.getOrCreate(file2);

        Map<String, SymbolTable> all = SymbolTableManager.getAll();

        assertEquals(2, all.size());
        assertTrue(all.containsKey("file1"));
        assertTrue(all.containsKey("file2"));
    }

    @Test
    void testGetAllReturnsDefensiveCopy() {
        InputFile file = createMockFile("file1", "test.wl", 100);
        SymbolTableManager.getOrCreate(file);

        Map<String, SymbolTable> all = SymbolTableManager.getAll();
        all.clear();

        // Original should still have the entry
        assertEquals(1, SymbolTableManager.size());
    }

    @Test
    void testClearRemovesAllTables() {
        InputFile file1 = createMockFile("file1", "test1.wl", 100);
        InputFile file2 = createMockFile("file2", "test2.wl", 200);

        SymbolTableManager.getOrCreate(file1);
        SymbolTableManager.getOrCreate(file2);
        assertEquals(2, SymbolTableManager.size());

        SymbolTableManager.clear();

        assertEquals(0, SymbolTableManager.size());
        assertFalse(SymbolTableManager.has(file1));
        assertFalse(SymbolTableManager.has(file2));
    }

    @Test
    void testSizeReturnsZeroInitially() {
        assertEquals(0, SymbolTableManager.size());
    }

    @Test
    void testSizeReturnsCorrectCount() {
        InputFile file1 = createMockFile("file1", "test1.wl", 100);
        InputFile file2 = createMockFile("file2", "test2.wl", 200);
        InputFile file3 = createMockFile("file3", "test3.wl", 300);

        SymbolTableManager.getOrCreate(file1);
        assertEquals(1, SymbolTableManager.size());

        SymbolTableManager.getOrCreate(file2);
        assertEquals(2, SymbolTableManager.size());

        SymbolTableManager.getOrCreate(file3);
        assertEquals(3, SymbolTableManager.size());
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // Test concurrent access doesn't throw exceptions
        InputFile file = createMockFile("file1", "test.wl", 100);

        Thread t1 = new Thread(() -> SymbolTableManager.getOrCreate(file));
        Thread t2 = new Thread(() -> SymbolTableManager.getOrCreate(file));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(1, SymbolTableManager.size());
    }
}
