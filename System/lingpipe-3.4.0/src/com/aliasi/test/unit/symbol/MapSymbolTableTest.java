package com.aliasi.test.unit.symbol;

import com.aliasi.symbol.MapSymbolTable;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;

public class MapSymbolTableTest extends BaseTestCase {

    public void testOne() {
        MapSymbolTable table = new MapSymbolTable();
        assertEquals(0,table.numSymbols());

        assertEquals(-1,table.symbolToID("abc"));

        try {
            table.idToSymbol(1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            table.idToSymbol(-1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        assertEquals(0,table.getOrAddSymbol("abc"));
        assertEquals(0,table.getOrAddSymbol("abc"));
        assertEquals(0,table.symbolToID("abc"));
        assertEquals("abc",table.idToSymbol(0));

        assertEquals(1,table.getOrAddSymbol("xyz"));
        assertEquals(1,table.getOrAddSymbol("xyz"));
        assertEquals(1,table.symbolToID("xyz"));
        assertEquals("xyz",table.idToSymbol(1));

        assertEquals(2,table.getOrAddSymbol("mno"));
        assertEquals(2,table.getOrAddSymbol("mno"));
        assertEquals(2,table.symbolToID("mno"));
        assertEquals("mno",table.idToSymbol(2));

        assertEquals(-1,table.symbolToID("jk"));
        assertEquals(-1,table.symbolToID("abcd"));
    }

    public void testTwo() {
        MapSymbolTable table = new MapSymbolTable();
        assertEquals(0,table.numSymbols());
        table.getOrAddSymbol("a");
        assertEquals(1,table.numSymbols());
        assertEquals("a",table.idToSymbol(table.symbolToID("a")));

        assertEquals(table.symbolToID("a"),table.getOrAddSymbol("a"));
        assertEquals(1,table.numSymbols());

        table.getOrAddSymbol("b");
        assertEquals(2,table.numSymbols());

        int bId = table.symbolToID("b");
        assertEquals(bId,table.removeSymbol("b"));
        assertEquals(1,table.numSymbols());

        table.getOrAddSymbol("c");
        table.clear();
        assertEquals(0,table.numSymbols());
        assertEquals(-1,table.symbolToID("a"));
        assertEquals(-1,table.symbolToID("b"));
        assertEquals(-1,table.symbolToID("c"));
    }


    public void testThree() throws ClassNotFoundException, IOException {
        MapSymbolTable table = new MapSymbolTable();

        int aID = table.getOrAddSymbol("a");
        assertEquals(aID,table.getOrAddSymbol("a"));
        assertEquals(aID,table.symbolToID("a"));
        assertEquals("a",table.idToSymbol(table.symbolToID("a")));

        int bID = table.getOrAddSymbol("b");

        int cdID = table.getOrAddSymbol("cd");
    
        MapSymbolTable table2
            = (MapSymbolTable)
            AbstractExternalizable.compile(table);

        assertEquals(3,table2.numSymbols());
        assertEquals(bID,table2.symbolToID("b"));
        assertEquals(cdID,table2.symbolToID("cd"));
    }

}
