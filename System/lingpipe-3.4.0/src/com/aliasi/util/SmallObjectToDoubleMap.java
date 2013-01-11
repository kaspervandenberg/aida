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

package com.aliasi.util;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.AbstractMap;
import java.util.AbstractSet;
// import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A <code>SmallObjectToDoubleMap</code> provides a space-efficient
 * implementation of an immutable map from objects to doubles.  The
 * map is constructed using any mapping from objects to doubles,
 * such as a {@link java.util.HashMap} or a {@link ObjectToDoubleMap}.
 *
 * <h3>Key Comparability</h3>
 *
 * <p>Keys in the map are required to implement the {@link Comparable}
 * interface.  If two keys are equal under the comparability
 * interface, the behavior of the get method is undefined.  This is
 * because {@link java.util.Arrays#binarySearch(Object[],Object)} is used to
 * look up keys rather than hash codes.  It is not strictly necessary
 * for the keys to implement equality and hash codes consistently with
 * their comparability for this class.
 *
 * <h3>Sparse Vector Method</h3>
 * <p>Like {@link ObjectToDoubleMap}, there is a {@link
 * #getValue(Comparable)} method which is defined to return
 * <code>0.0</code> if the specified key does not exist under the
 * mapping.  Mathematically, this makes the map a sparse vector in the
 * dimensionality of the number of possible key objects (usually
 * infinite).
 *
 * <h3>Serialization</h3>
 *
 * <p>An instance of <code>SmallObjectToDoubleMap</code> may be
 * serialized if its keys may be serialized.  If the keys are
 * not serializable, writing the object to an object output
 * stream will throw an exception.
 *
 * @author  Bob Carpenter
 * @version 3.1
 * @since   LingPipe3.1
 */
public class SmallObjectToDoubleMap<E extends Comparable<E>>
    extends AbstractMap<E,Double>
    implements Serializable {

    final E[] mKeys;
    final double[] mValues;

    /**
     * Construct a small object to double map from the specified map.
     * The numbers in the specified map's values will be converted
     * to double-precision floating point (<code>double</code>) values
     * before being stored.
     *
     * <p>Copies are made of all values, so changes to the specified
     * map will not affect this map or vice-versa.
     *
     * @param map Map from which to construct the small object to
     * double map.
     */
    public SmallObjectToDoubleMap(Map<E,? extends Number> map) {
        mKeys = (E[]) new Comparable[map.size()];
        map.keySet().<E>toArray(mKeys);
        java.util.Arrays.sort(mKeys);
        mValues = new double[mKeys.length];
        for (int i = 0; i < mKeys.length; ++i)
            mValues[i] = map.get(mKeys[i]).doubleValue();
    }

    SmallObjectToDoubleMap(E[] keys, double[] values) {
        mKeys = keys;
        mValues = values;
    }


    /**
     * Returns the value of the specified key as a primitive double
     * value.  If the key is not defined in the map, the value
     * <code>0.0</code> is returned.
     *
     * @param key Key whose value is returned.
     * @return Value for specified key as a primitive double.
     */
    public double getValue(E key) {
        int index = java.util.Arrays.binarySearch(mKeys,key);
        return index < 0
            ? 0.0
            : mValues[index];
    }

    public Set<Map.Entry<E,Double>> entrySet() {
        return new EntrySet();
    }

    public boolean containsKey(Object key) {
        return java.util.Arrays.binarySearch(mKeys,key) >= 0;
    }

    public boolean containsValue(Object value) {
        double val = ((Double) value).doubleValue();
        for (int i = 0; i < mValues.length; ++i)
            if (val == mValues[i])
                return true;
        return false;
    }

    public Double get(Object key) {
        int i = java.util.Arrays.binarySearch(mKeys,key);
        return i < 0
            ? null
            : new Double(mValues[i]);
    }

    public int size() {
        return mKeys.length;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the vector dot product of this map with the
     * specified map, taking the keys to be the dimensions.
     *
     * <p>The definition of dot product in this case is:
     *
     * <blockquote><pre>
     * dotprod(map1,map2) = <big><big>&Sigma;</big></big><sub><sub>e</sub></sub> map1.getValue(e) * map2.getValue(e)
     * </pre></blockquote>
     *
     * @param that Map to multiply with this one.
     * @return The dot product between this map and the specified one.
     */
    public double dotProduct(SmallObjectToDoubleMap<E> that) {
        double sum = 0.0;
        int i = 0;
        int j = 0;
        while (i < mKeys.length && j < that.mKeys.length) {
            int comp = mKeys[i].compareTo(that.mKeys[j]);
            if (comp == 0)
                sum += mValues[i++] * that.mValues[j++];
            else if (comp < 0)
                ++i;
            else
                ++j;
        }
        return sum;
    }

    /**
     * Returns a new map which has the same keys as this map with
     * values equal to the ones in this map multiplied by the
     * specified value.
     *
     * @param x Value to multiply each entry by.
     * @return Scaled map.
     */
    public SmallObjectToDoubleMap<E> multiply(double x) {
        double[] values = new double[mValues.length];
        for (int i = 0; i < values.length; ++i)
            values[i] = x * mValues[i];
        return new SmallObjectToDoubleMap<E>(mKeys,values);
    }

    // for serialization
    private Object writeReplace() {
        return new Externalizer<E>(this);
    }

    class EntrySet extends AbstractSet<Map.Entry<E,Double>> {
        public int size() {
            return mKeys.length;
        }
        public Iterator<Map.Entry<E,Double>> iterator() {
            return new EntryIterator();
        }
    }

    class EntryIterator implements Iterator<Map.Entry<E,Double>> {
        private int mNext = 0;
        public boolean hasNext() {
            return mNext < mKeys.length;
        }
        public Map.Entry<E,Double> next() {
            if (!hasNext()) {
                String msg = "No more elements in iterator.";
                throw new NoSuchElementException(msg);
            }
            return new Entry(mNext++);
        }
        public void remove() {
            String msg = "Removal not supported.";
            throw new UnsupportedOperationException(msg);
        }
    }

    class Entry implements Map.Entry<E,Double> {
        private final int mIndex;
        Entry(int index) {
            mIndex = index;
        }
        public E getKey() {
            return mKeys[mIndex];
        }
        public Double getValue() {
            return new Double(mValues[mIndex]);
        }
        public Double setValue(Double value) {
            String msg = "Not modifiable.";
            throw new UnsupportedOperationException(msg);
        }
        public boolean equals(Object that) {
            if (!(that instanceof Map.Entry)) return false;
            Map.Entry thatEntry = (Map.Entry) that;
            return (getKey()==null
                    ? thatEntry.getKey()==null
                    : getKey().equals(thatEntry.getKey()))
                && (getValue()==null ?
                    thatEntry.getValue()==null
                    : getValue().equals(thatEntry.getValue()));
        }
        public int hashCode() {
            return (getKey()==null
                    ? 0
                    : getKey().hashCode())
                ^
                (getValue()==null
                 ? 0
                 : getValue().hashCode());
        }
    }

    static class Externalizer<F extends Comparable<F>> extends AbstractExternalizable {
        // static final long serialVersionUID = ?;
        final SmallObjectToDoubleMap<F> mMap;
        public Externalizer() {
            this(null);
        }
        public Externalizer(SmallObjectToDoubleMap<F> map) {
            mMap = map;
        }
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            int len = in.readInt();
            F[] keys = (F[]) new Comparable[len];
            for (int i = 0; i < len; ++i)
                keys[i] = (F) in.readObject();
            double[] values = new double[len];
            for (int i = 0; i < len; ++i)
                values[i] = in.readDouble();
            return new SmallObjectToDoubleMap<F>(keys,values);
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            // length
            out.writeInt(mMap.mKeys.length);

            // keys
            for (int i = 0; i < mMap.mKeys.length; ++i) {
                if (mMap.mKeys[i] instanceof Compilable)
                    ((Compilable) mMap.mKeys[i]).compileTo(out);
                else
                    out.writeObject(mMap.mKeys[i]);
            }

            // vals
            for (int i = 0; i < mMap.mValues.length; ++i)
                out.writeDouble(mMap.mValues[i]);
        }
    }


}