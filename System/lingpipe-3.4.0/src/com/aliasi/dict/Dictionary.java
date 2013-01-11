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

import java.util.Set;
import java.util.Iterator;

/**
 * The <code>Dictionary</code> interface represents a dictionary as a
 * set of entries. Dictionary entries povide a string, a category, and
 * a score.  
 *
 * <P>Equality conditions and basic access is available through the
 * {@link Set} interface.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 */
public interface Dictionary<C> extends Set<DictionaryEntry<C>> {

    /**
     * Returns an iterator over the dictionary entries with the
     * specified phrase.
     *
     * @param phrase The phrase to look up.
     * @return Iterator over the entries with the specified phrase.
     */
    public Iterator<DictionaryEntry<C>> phraseEntryIt(String phrase);

    /**
     * Returns the dictionary entries with the specified phrase.
     *
     * @param phrase The phrase to look up.
     * @return The entries with the specified phrase.
     */
    public DictionaryEntry<C>[] phraseEntries(String phrase);

    /**
     * Returns an iterator over the dictionary entries with the
     * specified category.
     *
     * @param category Category of entries.
     * @return Iterator over entries with specified category.
     */
    public Iterator<DictionaryEntry<C>> categoryEntryIt(C category);

    /**
     * Returns the dictionary entries with the specified category.
     *
     * @param category Category of entries.
     * @return Entries with specified category.
     */
    public DictionaryEntry<C>[] categoryEntries(C category);

    /**
     * Returns the size of this dictionary as measured by number
     * of dictionary entries.  
     *
     * @return Size of this dictionary.
     */
    public int size();

    /**
     * Returns all of the dictionary entries for this dictionary.
     *
     * @return This dictionary's entries.
     */
    public DictionaryEntry<C>[] entries();

    /**
     * Adds the specified dictionary entry to the dictionary.  If an
     * implementation of <code>Dictionary</code> is immutable, then
     * this method may throw an unsupported operation exception.
     *
     * @param entry Dictionary entry to add.
     * @throws UnsupportedOperationException If this operation is not
     * supported by a subclass implementation.
     */
    public void addEntry(DictionaryEntry<C> entry);
    
}
