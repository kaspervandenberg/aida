/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.dict;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToSet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Iterator;


/**
 * A <code>MapDictionary</code> uses an underlying map from phrases to
 * their set of dictionary entries.  Map-based dictionaries are
 * compilable if their underlying entries are compilable, which
 * requires every category object to implement either the LingPipe
 * interface {@link Compilable} or the Java interface {@link java.io.Serializable}
 *
 * <p>The result is a fast
 * implementation of {@link #addEntry(DictionaryEntry)}, {@link
 * #iterator()} and {@link #phraseEntryIt(String)}.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.3.1
 */
public class MapDictionary<C>
    extends AbstractDictionary<C>
    implements Compilable {

    final ObjectToSet<String, DictionaryEntry<C>> mPhraseToEntrySet;

    /**
     * Construct an empty map-based dictionary.
     */
    public MapDictionary() {
        this(new ObjectToSet<String,DictionaryEntry<C>>());
    }

    private MapDictionary(ObjectToSet<String,DictionaryEntry<C>> phraseToEntrySet) {
        mPhraseToEntrySet = phraseToEntrySet;
    }

    public void addEntry(DictionaryEntry<C> entry) {
        mPhraseToEntrySet.addMember(entry.phrase(),entry);
    }

    public Iterator<DictionaryEntry<C>> iterator() {
        return mPhraseToEntrySet.memberIterator();
    }

    public Iterator<DictionaryEntry<C>> phraseEntryIt(String phrase) {
        return mPhraseToEntrySet.getSet(phrase).iterator();
    }

    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Externalizer(this));
    }

    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = -9136273040574611243L;
        final MapDictionary mDictionary;
        public Externalizer() { this(null); }
        public Externalizer(MapDictionary dictionary) {
            mDictionary = dictionary;
        }
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            MapDictionary dict = new MapDictionary();
            int numEntries = in.readInt();
            for (int i = 0; i < numEntries; ++i) {
                DictionaryEntry entry = (DictionaryEntry) in.readObject();
                dict.addEntry(entry);
            }
            return dict;
        }
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mDictionary.size());
            Iterator it = mDictionary.iterator();
            while (it.hasNext()) {
                DictionaryEntry entry = (DictionaryEntry) it.next();
                entry.compileTo(objOut);
            }
        }
    }

}
