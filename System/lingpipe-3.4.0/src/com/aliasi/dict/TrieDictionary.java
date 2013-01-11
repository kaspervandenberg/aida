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

// import com.aliasi.util.Arrays;
import com.aliasi.util.Iterators;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A <code>TrieDictionary</code> stores a dictionary using a character
 * trie structure.  This requires a constant amount of space for each
 * entry and each prefix of an entry's string.  Lookups take an amount
 * of time proportional to the length of the string being looked up,
 * with each character requiring a lookup in a map.  The lookup is
 * done with binary search in this implementation in time proportional
 * to the log of the number of characters, for a total lookup time
 * of <code><b>O</b>(n log c)</code> where <code>n</code> is the
 * number of characters in the string being looked up and <code>c</code>
 * is the number of charactes.
 *
 * <P>Tries are a popular data structure; see the <a
 * href="http://en.wikipedia.org/wiki/Trie">Wikipedia Trie</a> topic for
 * examples and references.  Tries are also used in the language model
 * classes {@link com.aliasi.lm.TrieCharSeqCounter} and {@link
 * com.aliasi.lm.TrieIntSeqCounter} and the compiled forms of all of
 * the language models.
 * 
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 */
public class TrieDictionary<C> extends AbstractDictionary<C> {
    
    Node mRootNode = new Node();

    /**
     * Construct a trie-based dictionary.
     */
    public TrieDictionary() { 
        /* do ntohing */
    }

    public DictionaryEntry<C>[] phraseEntries(String phrase) {
        Node node = mRootNode;
        for (int i = 0; i < phrase.length(); ++i) {
            node = node.getDtr(phrase.charAt(i));
            if (node == null) return Node.EMPTY_ENTRIES;
        }
        return node.mEntries;
    }
    
    public Iterator<DictionaryEntry<C>> phraseEntryIt(String phrase) {
        return new Iterators.Array<DictionaryEntry<C>>(phraseEntries(phrase));
    }

    /**
     * Equal entries will be ignored.
     */
    public void addEntry(DictionaryEntry<C> entry) {
        String phrase = entry.phrase();
        Node node = mRootNode;
        for (int i = 0; i < phrase.length(); ++i)
            node = node.getOrAddDtr(phrase.charAt(i));
        node.addEntry(entry);
    }

    public Iterator<DictionaryEntry<C>> iterator() {
        return new TrieIterator<C>(mRootNode);
    }

    private static class TrieIterator<D> 
        extends Iterators.Buffered<DictionaryEntry<D>> {
        LinkedList mQueue = new LinkedList();
        DictionaryEntry<D>[] mEntries;
        int mNextEntry = -1;
        TrieIterator(Node root) {
            mQueue.add(root);
        }
        protected DictionaryEntry<D> bufferNext() {
            while (mEntries == null && !mQueue.isEmpty()) {
                Node node = (Node) mQueue.removeFirst();
                if (node == null) System.out.println("GOT IT");
                addDtrs(node.mDtrNodes);
                if (node.mEntries.length > 0) {
                    mEntries = node.mEntries;
                    mNextEntry = 0;
                }
            }
            if (mEntries == null) return null;
            DictionaryEntry<D> result = mEntries[mNextEntry++];
            if (mNextEntry >= mEntries.length) mEntries = null;
            return result;
        }
        void addDtrs(Node[] dtrs) {
            for (int i = dtrs.length; --i >= 0; ) {
                if (dtrs[i] == null) System.out.println("ADDING=" + i);
                mQueue.addFirst(dtrs[i]);
            }
        }
    }

}
