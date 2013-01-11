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
package com.aliasi.symbol;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A <code>MapSymbolTable</code > is a dynamic symbol table based on a
 * pair of underlying maps.  After creating a map symbol table, new
 * symbols may be added using {@link #getOrAddSymbol(String)}.
 *
 * <p>Map symbol tables are serializable.  The result of writing
 * to an object output stream and reading back in through an
 * object input stream produces an instance of this same class,
 * <code>MapSymbolTable</code>, with the same behavior as the
 * instance serialized.
 *
 * <P><i>Implementation Note:</i> This table uses a pair of
 * maps, one in each direction between symbols represented
 * by instances of <code>String</code> and identifiers
 * represented by instance of <code>Integer</code>.
 *
 * @author  Bob Carpenter
 * @version 3.1
 * @since   LingPipe2.0
 */
public class MapSymbolTable implements Compilable, Serializable, SymbolTable {

    final HashMap<String,Integer> mSymbolToId = new HashMap<String,Integer>();
    final HashMap<Integer,String> mIdToSymbol = new HashMap<Integer,String>();
    private int mNextSymbol = 0;

    /**
     * Construct an empty map symbol table.  The default first
     * symbol identifier is zero (<code>0</code>) and subsequent
     * symbols are assigned successive integer identifiers.
     */
    public MapSymbolTable() {
        this(0);
    }

    /**
     * Construct an empty map symbol table that begins allocating
     * identifiers at the specified value.  Subsequent symbols
     * will be assigned identifiers to successive identifiers.
     *
     * @param firstId Identifier of first symbol.
     */
    public MapSymbolTable(int firstId) {
        mNextSymbol = firstId;
    }

    /**
     * Returns the complete set of symbols in this symbol table.
     *
     * @return The set of symbols for this symbol table.
     */
    public Set<String> symbolSet() {
        return Collections.<String>unmodifiableSet(mSymbolToId.keySet());
    }

    private MapSymbolTable(ObjectInput objIn) throws IOException {
        int numEntries = objIn.readInt();
        int max = 0;
        for (int i = 0; i < numEntries; ++i) {
            String symbol = objIn.readUTF();
            Integer id = new Integer(objIn.readInt());
            max = Math.max(max,id.intValue());
            mSymbolToId.put(symbol,id);
            mIdToSymbol.put(id,symbol);
        }
        mNextSymbol = max+1;
    }

    /**
     * Writes this map symbol table to the specified object output.
     * The object that will be read back in will be an instance of
     * {@link MapSymbolTable} with the same entries as this symbol
     * table.
     *
     * <p>Calling this method, <code>writeTo(objOut)</code> produces
     * the same result as standard serialization,
     * <code>objOut.writeObject(this)</code>.  This method has been
     * deprecated in favor of standard serialization.
     *
     * @param objOut Object output to which to write this symbol
     * table.
     * @throws IOException If there is an I/O error writing.
     * @deprecated As of LingPipe 3.1, use <code>objOut.writeObject(this)</code>.
     */
    public void writeTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }


    // for serialization support
    private Object writeReplace() {
        return new Externalizer(this);
    }

    /**
     * Compiles this map symbol table to the specified object output.
     * The object that will be read back in will be an instance of
     * {@link MapSymbolTable} with the same entries as this symbol
     * table, so this is in some sense a no-op compilation.
     *
     * <p>Calling this method, <code>compileTo(objOut)</code> produces
     * the same result as standard serialization,
     * <code>objOut.writeObject(this)</code>.  This method has been
     * deprecated in favor of standard serialization.
     *
     * @param objOut Object output to which to write this symbol
     * table.
     * @throws IOException If there is an I/O error writing.
     * @deprecated As of LingPipe 3.1, use <code>objOut.writeObject(this)</code>.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }


    public int numSymbols() {
        return mSymbolToId.size();
    }

    public int symbolToID(String symbol) {
        Integer result = symbolToIDInteger(symbol);
        return result == null ? -1 : result.intValue();
    }

    /**
     * Returns an Integer representation of the symbol if
     * it exists in the table or null if it does not.
     *
     * @param symbol Symbol whose identifer is returned.
     * @return Integer identifier for symbol, or null if it
     * does not exist.
     */
    public Integer symbolToIDInteger(String symbol) {
        return mSymbolToId.get(symbol);
    }

    /**
     * Returns the symbol for the specified identifier.
     * If the identifier has no defined symbol, an exception
     * is thrown.
     *
     * @param id Integer identifier.
     * @return The symbol for the identifier.
     * @throws IndexOutOfBoundsException If the symbol could
     * not be found in the symbol table.
     */
    public String idToSymbol(Integer id) {
        String symbol = mIdToSymbol.get(id);
        if (symbol == null) {
            String msg="Could not find id=" + id;
            throw new IndexOutOfBoundsException(msg);
        }
        return symbol;
    }

    public String idToSymbol(int id) {
        return idToSymbol(new Integer(id));
    }

    /**
     * Removes the specified symbol from the symbol table.  After the
     * symbol is removed, its identifier will not be assigned to
     * another symbol.
     *
     * @param symbol Symbol to remove.
     * @return The previous id of the symbol if it was in the table,
     * or -1 if it was not.
     */
    public int removeSymbol(String symbol) {
        int id = symbolToID(symbol);
        if (id >= 0) {
            mSymbolToId.remove(symbol);
            mIdToSymbol.remove(new Integer(id));
        }
        return id;
    }

    /**
     * Clears all of the symbols from the symbol table.  It does
     * not reset the symbol counter, so the removed identifiers
     * will not be reused.
     */
    public void clear() {
        mSymbolToId.clear();
        mIdToSymbol.clear();
    }

    /**
     * Returns the identifier for the specified symbol, adding
     * it to the symbol table if necessary.
     *
     * @param symbol Symbol to get or add to the table.
     * @return Identifier for specified symbol.
     */
    public int getOrAddSymbol(String symbol) {
        return getOrAddSymbolInteger(symbol).intValue();
    }

    /**
     * Returns the integer identifier for the specified symbol,
     * adding it to the symbol table if necessary.
     *
     * @param symbol Symbol to get or add to the table.
     * @return Identifier for specified symbol.
     */
    public Integer getOrAddSymbolInteger(String symbol) {
        Integer id = mSymbolToId.get(symbol);
        if (id != null) return id;
        Integer freshId = new Integer(mNextSymbol++);
        mSymbolToId.put(symbol,freshId);
        mIdToSymbol.put(freshId,symbol);
        return freshId;
    }

    /**
     * Returns a string-based representation of this symbol table
     * by printing the underlying identifier to symbol mapping.
     *
     * @return A string-based representation of this symbol table.
     */
    public String toString() {
        return mIdToSymbol.toString();
    }

    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = -6040616216389802649L;
        final MapSymbolTable mSymbolTable;
        public Externalizer() { mSymbolTable = null; }
        public Externalizer(MapSymbolTable symbolTable) {
            mSymbolTable = symbolTable;
        }
        public Object read(ObjectInput in) throws IOException {
            return new MapSymbolTable(in);
        }
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mSymbolTable.mSymbolToId.size());
            Iterator it = mSymbolTable.mSymbolToId.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                objOut.writeUTF(entry.getKey().toString());
                objOut.writeInt(((Integer)entry.getValue()).intValue());
            }
        }
    }






}
