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

package com.aliasi.chunk;

import com.aliasi.util.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A <code>ChunkingImpl</code> provides a mutable, set-based
 * implementation of the chunking interface.  At construction time, a
 * character sequence or slice is specified.  Chunks may then be added
 * using the {@link #add(Chunk)} method.
 *
 * @author  Bob Carpenter
 * @version 3.1
 * @since   LingPipe2.1
 */
public class ChunkingImpl implements Chunking {

    private final String mString;
    private final Set<Chunk> mChunkSet = new LinkedHashSet<Chunk>();

    /**
     * Constructs a chunking implementation to hold chunks over the
     * specified character sequence.  The sequence is stored immutably
     * in this implementation, so later changes to the sequence
     * provided to this constructor will not affect the constructed
     * chunking implementation.  All chunks added must be within this
     * character sequence's bounds.
     *
     * @param cSeq Character sequence underlying the chunking.
     */
    public ChunkingImpl(CharSequence cSeq) {
        mString = cSeq.toString(); // no copy if already string
    }

    /**
     * Construct a chunking implementation to hold chunks over the
     * specified character slice.  The slice is copied, so later
     * changes to it do not affect the constructed chunking.  All
     * chunks added to this chunking must be within this character
     * slice's (relative) bounds.  The chunks themselves will have
     * indices relative to the start parameter of this constructor,
     * rather than absolute offsets into this character slice.
     *
     * @param cs Character array.
     * @param start Index in array of first element in chunk.
     * @param end Index in array of one past the last element in chunk.
     */
    public ChunkingImpl(char[] cs, int start, int end) {
        this(new String(cs,start,end-start));
    }

    /**
     * Adds all of the chunks in the specified collection to this
     * chunking.  If any of the chunks do not implement the
     * <code>Chunk</code> interface, an illegal argument exception is
     * thrown.
     *
     * @param chunks Chunks to add to this chunking.
     * @throws IllegalArgumentException If the collection contains an
     * object that does not implement <code>Chunk</code>.
     */
    public void addAll(Collection chunks) {
        Iterator it = chunks.iterator();
        while (it.hasNext()) {
            Object next = null;
            next = it.next();
            if (!(next instanceof Chunk)) {
                String foundClass
                    = (next == null) ? "null" : next.getClass().toString();
                String msg = "Chunks must implement Chunk interface."
                    + " Found class=" + foundClass;
                throw new IllegalArgumentException(msg);
            }
            add((Chunk) next);
        }
    }

    /**
     * Add a chunk this this chunking.  The chunk must have start
     * and end points within the bounds provided by the character
     * sequence underlying this chunking.
     *
     * @param chunk Chunk to add to this chunking.
     * @throws IllegalArgumentException If the end point is beyond the
     * underlying character sequence.
     */
    public void add(Chunk chunk) {
        if (chunk.end() > mString.length()) {
            String msg = "End point of chunk beyond end of char sequence."
                + "Char sequence length=" + mString.length()
                + " chunk.end()=" + chunk.end();
            throw new IllegalArgumentException(msg);
        }
        mChunkSet.add(chunk);
    }

    /**
     * Returns the character sequence underlying this chunking.
     *
     * @return The character sequence underlying this chunking.
     */
    public CharSequence charSequence() {
        return mString;
    }

    /**
     * Returns the set of chunks for this chunking.  The returned set
     * is an immutable view of the chunks in this set; it will change
     * as the set underlying this chunking changes, but it may not
     * be modified externally.
     *
     * @return The set of chunks for this chunking.
     */
    public Set<Chunk> chunkSet() {
        return Collections.<Chunk>unmodifiableSet(mChunkSet);
    }

    public boolean equals(Object that) {
        return (that instanceof Chunking)
            ? equal(this,(Chunking)that)
            : false;
    }

    public int hashCode() {
        return hashCode(this);
    }

    /**
     * Returns <code>true</code> if the specified chunkings are equal.
     * Chunking equality is defined in {@link Chunking#equals(Object)}
     * to be equality of character sequence yields and equality of
     * chunk sets.
     *
     * <P><i>Warning:</i> Equality is unstable if the chunkings
     * change.
     *
     * @param chunking1 First chunking.
     * @param chunking2 Second chunking.
     * @return <code>true</code> if the chunkings are equal.
     */
    public static boolean equal(Chunking chunking1, Chunking chunking2) {
        return Strings.equalCharSequence(chunking1.charSequence(),
                                         chunking2.charSequence())
            && chunking1.chunkSet().equals(chunking2.chunkSet());
    }

    /**
     * Returns the hash code for the specified chunking.  The hash
     * code for a chunking is defined by {@link Chunking#hashCode()}.
     *
     * <P><i>Warning:</i> Hash codes are unstable if the chunkings change.
     *
     * @param chunking Chunking whose hash code is returned.
     * @return The hash code for the specified chunking.
     */
    public static int hashCode(Chunking chunking) {
        return Strings.hashCode(chunking.charSequence())
            + 31 * chunking.chunkSet().hashCode();
    }

    /**
     * Returns a string-based representation of this chunking.  This
     * representation includes the character sequence and each
     * chunk in the chunk set.
     *
     * @return String-based representation of this chunking.
     */
    public String toString() {
        return charSequence()
            + " : " + chunkSet();
    }


}
