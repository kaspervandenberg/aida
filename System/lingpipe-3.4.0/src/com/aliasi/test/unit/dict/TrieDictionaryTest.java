package com.aliasi.test.unit.dict;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.TrieDictionary;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class TrieDictionaryTest extends BaseTestCase {


    public void testOne() {
	TrieDictionary dict = new TrieDictionary();
	assertFalse(dict.iterator().hasNext());
	assertEquals(0,dict.size());
    
	DictionaryEntry entryThis = new DictionaryEntry("this","DET");
	DictionaryEntry entryThe = new DictionaryEntry("the","DET");
	DictionaryEntry entryThat = new DictionaryEntry("that","DET");
	DictionaryEntry entryThat2 = new DictionaryEntry("that","NP");
	DictionaryEntry entryA = new DictionaryEntry("a","DET");
	DictionaryEntry entryMember = new DictionaryEntry("member","N");

	dict.addEntry(entryThis);
	assertDict(new DictionaryEntry[] { entryThis },
		   dict);

	dict.addEntry(entryThe);
	assertDict(new DictionaryEntry[] { entryThe, entryThis },
		   dict);

	dict.addEntry(entryA);
	dict.addEntry(entryA);
	assertDict(new DictionaryEntry[] { entryA, entryThe, entryThis },
		   dict);
    
	dict.addEntry(entryMember);
	assertDict(new DictionaryEntry[] { entryA, entryMember, 
					   entryThe, entryThis },
		   dict);

	dict.addEntry(entryThat);
	dict.addEntry(entryThat2);
	DictionaryEntry[] entries = new DictionaryEntry[] {
	    entryA, entryMember, entryThe, entryThis, entryThat, entryThat2
	};
	HashSet expectedEntrySet = new HashSet(Arrays.asList(entries));
	assertEquals(entries.length,expectedEntrySet.size());
	assertEquals(expectedEntrySet,
		     new HashSet(Arrays.asList(dict.entries())));

	assertPhraseEntries(dict,"that",
			    new Object[] { entryThat, entryThat2 });
	assertPhraseEntries(dict,"the",
			    new Object[] { entryThe });
	assertPhraseEntries(dict,"member",
			    new Object[] { entryMember });
	assertPhraseEntries(dict,"foo",
			    new Object[] { });
    
	assertCatEntries(dict,"DET",
			 new Object[] { entryA, entryThe, 
					entryThis, entryThat });

	assertCatEntries(dict,"NP",
			 new Object[] { entryThat2 });

	assertCatEntries(dict,"V",
			 new Object[] { } );

    }
    
    void assertCatEntries(TrieDictionary dict, Object cat,
			  Object[] entries) {
	HashSet expectedEntrySet 
	    = new HashSet(Arrays.asList(entries));
	HashSet foundSet 
	    = new HashSet(Arrays.asList(dict.categoryEntries(cat)));
	assertEquals(expectedEntrySet,foundSet);
    }
    

    void assertPhraseEntries(TrieDictionary dict, String phrase, 
			     Object[] entries) {
	HashSet expectedEntrySet 
	    = new HashSet(Arrays.asList(entries));
	HashSet foundSet 
	    = new HashSet(Arrays.asList(dict.phraseEntries(phrase)));
	assertEquals(expectedEntrySet,foundSet);
    }

    void assertDict(DictionaryEntry[] entries, TrieDictionary dict) {
	assertEquals(entries.length,dict.size());
	Iterator it = dict.iterator();
	for (int i = 0; i < entries.length; ++i)
	    assertEquals(entries[i],it.next());
    }

}
