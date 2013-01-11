package com.aliasi.test.unit.dict;

import com.aliasi.dict.DictionaryEntry;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.AbstractExternalizable;


public class DictionaryEntryTest extends BaseTestCase {
    
    public void testOne() throws Exception {
    // use serializable
    DictionaryEntry entry = new DictionaryEntry("foo",new Integer(1),4,2.75);
    DictionaryEntry compiledEntry 
        = (DictionaryEntry) AbstractExternalizable.compile(entry);
    assertEquals(entry,compiledEntry);

    // use compilable
    DictionaryEntry entry2 = new DictionaryEntry("bar",entry,5,14.4);
    DictionaryEntry compiledEntry2
        = (DictionaryEntry) AbstractExternalizable.compile(entry2);
    assertEquals(entry2,compiledEntry2);

    // neither
    DictionaryEntry entry3 = new DictionaryEntry("baz",new Object(), 4, 17.9);
    try {
        AbstractExternalizable.compile(entry3);
        fail();
    } catch (ClassCastException e) {
        succeed();
    }

    }

        
    


}


