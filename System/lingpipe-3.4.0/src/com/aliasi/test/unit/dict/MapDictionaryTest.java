package com.aliasi.test.unit.dict;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.AbstractExternalizable;

import java.util.HashSet;
import java.util.Arrays;

public class MapDictionaryTest extends BaseTestCase {

    public void testOne() throws Exception {
    MapDictionary dict = new MapDictionary();
    assertCompile(dict);
    assertEquals(0,dict.size());
    
    DictionaryEntry entry1 = new DictionaryEntry("foo","X1");
    dict.addEntry(entry1);
    assertCompile(dict);
    assertEquals(1,dict.size());

    DictionaryEntry entry2 = new DictionaryEntry("bar","Y2");
    dict.addEntry(entry2);
    assertCompile(dict);

    DictionaryEntry entry3 = new DictionaryEntry("bar","Z3");
    dict.addEntry(entry3);
    assertCompile(dict);
    assertEquals(3,dict.size());

    HashSet fooSet = new HashSet();
    fooSet.add(entry1);
    assertEquals(fooSet,new HashSet(Arrays.asList(dict.phraseEntries("foo"))));

    HashSet barSet = new HashSet();
    barSet.add(entry2);
    barSet.add(entry3);
    assertEquals(barSet,new HashSet(Arrays.asList(dict.phraseEntries("bar"))));

    assertEquals(3,dict.size());
    
    }

    void assertCompile(MapDictionary dictionary) throws Exception {
    MapDictionary compiledDictionary
        = (MapDictionary) AbstractExternalizable.compile(dictionary);
    assertEquals(dictionary,compiledDictionary);
    }


}