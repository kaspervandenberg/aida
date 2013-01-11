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

package com.aliasi.matrix;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A <code>SparseFloatVector</code> implements an immutable sparse
 * vector with values represented as single-precision floating point
 * numbers.  Sparse vectors are specified in terms of mappings from
 * integer dimensions to single-precision floating-point values.  The
 * constructor allows the number of dimensions to be set, or to be
 * inferred as the largest dimension with a value in the mapping.
 * Dimensions for which no value is specified in the map provided to
 * the constructor will have values of 0.0.
 *
 * <p>A deep copy is made of the map provided to the constructor, so
 * that changes to the specified map do not affect this vector and
 * changes to this vector do not affect the map.
 *
 * <p><i>Implementation Note:</i> The underlying data is stored in a
 * pair of parallel arrays, one containing integer indexes and the
 * other values of type <code>float</code>.  The constructor computes
 * and stores the fixed number of dimensions.  The constructor also
 * stores the length of the vector by walking over the values.  Dot
 * products between sparse vectors are computed at double-precision by
 * walking over the indices and doing a merge, which is the most
 * efficient approach if the vectors are roughly the same size.  Dot
 * products with other vector implementations are computed by
 * iterating over the indexes in the sparse vector and looking up the
 * corresponding values in the argument vector.  Cosines are computed
 * by dividing dot products by lengths.
 *
 * <p>Equality versus other sparse float vectors only considers indexes
 * with values.  Hash codes also only consider indexes with values,
 * computing a shift and mask as well as an integer multiply and add
 * for each dimension.
 *
 * @author  Bob Carpenter
 * @version 3.1
 * @since   LingPipe3.1
 */
public class SparseFloatVector
    extends AbstractVector
    implements Serializable {

    final int[] mKeys;
    final float[] mValues;
    final int mNumDimensions;
    final double mLength;

    /**
     * Construct a sparse vector from the specified map.  The
     * dimensionality will be fixed to the largest integer with a
     * value specified in the map.  See the class documentation for
     * information details.
     *
     * @param map Mapping from dimensions to values.
     * @throws IllegalArgumentException If there are negative keys.
     */
    public SparseFloatVector(Map<Integer,? extends Number> map) {
        this(map,-1,false);
    }

    /**
     * Constructs a sparse vector from the specified map with the
     * specified number of dimensions.  See the class documentation
     * for further implementation details.
     *
     * @param map Mapping from dimensions to values.
     * @param numDimensions Number of dimensions for the constructed vector.
     * @throws IllegalArgumentException If there are negative keys, or if the
     * specified number of dimensions is negative, or if the specified number of
     * dimensions is not greater than or equal to the largest integer key.
     */
    public SparseFloatVector(Map<Integer,? extends Number> map, int numDimensions) {
        this(map,numDimensions,true);
    }


    SparseFloatVector(int[] keys, float[] values,
                      int numDimensions, double length) {
        if (numDimensions < 0) {
            String msg = "Dimensionality must be positive."
                + " Found numDimensions=" + numDimensions;
            throw new IllegalArgumentException(msg);
        }
        mKeys = keys;
        mValues = values;
        mNumDimensions = numDimensions;
        mLength = length;
    }

    private SparseFloatVector(Map<Integer,? extends Number> map,
                              int numDimensions, boolean useDims) {
        Integer[] keys = map.keySet().<Integer>toArray(new Integer[map.size()]);
        Arrays.sort(keys);
        int[] newKeys = new int[keys.length];
        for (int i = 0; i < keys.length; ++i)
            newKeys[i] = keys[i].intValue();
        if (newKeys.length > 0 && newKeys[0] < 0) {
            String msg = "All keys must be non-negative."
                + " Found key=" + newKeys[0];
            throw new IllegalArgumentException(msg);
        }
        float[] values = new float[keys.length];
        for (int i = 0; i < keys.length; ++i)
            values[i] = map.get(keys[i]).floatValue();
        mKeys = newKeys;
        mValues = values;
        if (mKeys.length > 0 && mKeys[mKeys.length-1] == Integer.MAX_VALUE) {
            String msg = "Maximum dimension is Integer.MAX_VALUE-1"
                + " Found dimension=Integer.MAX_VALUE";
            throw new IllegalArgumentException(msg);
        }
        int maxFoundDimensions
            = mKeys.length == 0 ? 0 : (mKeys[mKeys.length-1] + 1);
        if (useDims) {
            if (numDimensions < 0) {
                String msg = "Number of dimensions must be non-negative."
                    + " Found numDimensions=" + numDimensions;
                throw new IllegalArgumentException(msg);
            }
            if (numDimensions < maxFoundDimensions) {
                String msg = "Specified number of dimensions lower than largest index."
                    + " Num dimensions specified=" + numDimensions
                    + " Largest dimension found=" + mKeys[mKeys.length-1];
                throw new IllegalArgumentException(msg);
            }
            mNumDimensions = numDimensions;
        } else {
            mNumDimensions = maxFoundDimensions;
        }
        mLength = computeLength(values);
    }

    public int numDimensions() {
        return mNumDimensions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mValues.length; ++i) {
            if (i > 0) sb.append(' ');
            sb.append(mKeys[i] + "=" + mValues[i]);
        }
        return sb.toString();
    }

    public double value(int dimension) {
        if (dimension < 0 || dimension >= mNumDimensions) {
            String msg = "Dimension out of range."
                + " num dimensions in vector=" + mNumDimensions
                + " found dimension=" + dimension;
            throw new IndexOutOfBoundsException(msg);
        }
        int index = Arrays.binarySearch(mKeys,dimension);
        return index < 0 ? 0.0 : mValues[index];
    }

    public double length() {
        return mLength;
    }

    static double computeLength(float[] vals) {
        double sum = 0;
        for (int i = 0; i < vals.length; ++i) {
            double val = vals[i];
            sum += val * val;
        }
        return Math.sqrt(sum);
    }

    public Vector add(Vector v) {
        if (!(v instanceof SparseFloatVector))
            return Matrices.add(this,v);
        verifyMatchingDimensions(v);
        SparseFloatVector spv = (SparseFloatVector) v;
        int[] keys1 = mKeys;
        int[] keys2 = spv.mKeys;

        int numMatching = 0;
        int index1 = 0;
        int index2 = 0;
        while (index1 < keys1.length && index2 < keys2.length) {
            ++numMatching;
            int comp = keys1[index1] - keys2[index2];
            if (comp == 0) {
                ++index1;
                ++index2;
            } else if (comp < 0) {
                ++index1;
            } else {
                ++index2;
            }
        }
        while (index1 < keys1.length) {
            ++numMatching;
            ++index1;
        }
        while (index2 < keys2.length) {
            ++numMatching;
            ++index2;
        }

        float[] vals1 = mValues;
        float[] vals2 = spv.mValues;

        int[] resultKeys = new int[numMatching];
        float[] resultVals = new float[numMatching];

        int resultIndex = 0;
        index1 = 0;
        index2 = 0;
        while (index1 < keys1.length && index2 < keys2.length) {
            int comp = keys1[index1] - keys2[index2];
            if (comp == 0) {
                resultKeys[resultIndex] = index1;
                resultVals[resultIndex] = vals1[index1] + vals2[index2];
                ++index1;
                ++index2;
                ++resultIndex;
            } else if (comp < 0) {
                resultKeys[resultIndex] = index1;
                resultVals[resultIndex] = vals1[index1];
                ++index1;
                ++resultIndex;
            } else {
                resultKeys[resultIndex] = index2;
                resultVals[resultIndex] = vals2[index2];
                ++index2;
                ++resultIndex;
            }
        }
        while (index1 < keys1.length) {
            resultKeys[resultIndex] = index1;
            resultVals[resultIndex] = vals1[index1];
            ++index1;
            ++resultIndex;
        }
        while (index2 < keys2.length) {
            resultKeys[resultIndex] = index2;
            resultVals[resultIndex] = vals2[index2];
            ++index2;
            ++resultIndex;
        }
        double lengthSquared = 0;
        for (int i = 0; i < resultVals.length; ++i)
            lengthSquared += resultVals[i] * resultVals[i];
        double length = Math.sqrt(lengthSquared);
        return new SparseFloatVector(resultKeys,resultVals,numDimensions(),length);
    }




    public double dotProduct(Vector v) {
        verifyMatchingDimensions(v);
        if (v instanceof SparseFloatVector) {
            SparseFloatVector spv = (SparseFloatVector) v;
            int[] keys1 = mKeys;
            float[] vals1 = mValues;
            int[] keys2 = spv.mKeys;
            float[] vals2 = spv.mValues;

            double sum = 0.0;
            int index1 = 0;
            int index2 = 0;
            while (index1 < keys1.length && index2 < keys2.length) {
                int comp = keys1[index1] - keys2[index2];
                if (comp == 0)
                    sum += vals1[index1++] * vals2[index2++];
                else if (comp < 0)
                    ++index1;
                else
                    ++index2;
            }
            return sum;
        } else {
            double sum = 0.0;
            int[] keys1 = mKeys;
            float[] vals1 = mValues;
            for (int i = 0; i < keys1.length; ++i)
                sum += vals1[i] * v.value(keys1[i]);
            return sum;
        }
    }

    /**
     * Returns true if the specified object is a vector
     * with the same dimensionality and values as this vector.
     *
     * <p><i>Implementation Note:</i> This method requires a
     * get and comparison for each dimension with a non-zero
     * value in this vector.
     */
    public boolean equals(Object that) {
        if (that instanceof SparseFloatVector) {
            SparseFloatVector thatVector = (SparseFloatVector) that;
            if (mKeys.length != thatVector.mKeys.length)
                return false;
            if (mNumDimensions != thatVector.mNumDimensions)
                return false;
            if (mLength != thatVector.mLength)
                return false;
            for (int i = 0; i < mKeys.length; ++i)
                if (mKeys[i] != thatVector.mKeys[i])
                    return false;
            for (int i = 0; i < mValues.length; ++i)
                if (mValues[i] != thatVector.mValues[i])
                    return false;
            return true;
        } else if (that instanceof Vector) {
            Vector thatVector = (Vector) that;
            if (mNumDimensions != thatVector.numDimensions())
                return false;
            if (mLength != thatVector.length())
                return false;
            for (int i = 0; i < mKeys.length; ++i)
                if (mValues[i] != thatVector.value(mKeys[i]))
                    return false;
            return true;
        }
        return super.equals(that);
    }

    /**
     * Returns the hash code for this sparse float vector.  The
     * hash code is the same as it would be for the equivalent
     * dense vector.
     *
     * <p><i>Implementation Note:</i> hashing requires a long integer
     * shift and mask, as well as a normal integer multiply and
     * add for each dimension with a value.
     *
     * @return The hash code for this sparse float vector.
     */
    public int hashCode() {
        int code = 1;
        int numDimensions = numDimensions();
        for (int i = 0; i < mValues.length; ++i) {
            long v = Double.doubleToLongBits(mValues[i]);
            int valHash = (int)(v^(v>>>32));
            code = 31 * code + valHash;
        }
        return code;
    }


    public double cosine(Vector v) {
        return dotProduct(v) / (v.length() * length());
    }

    private Object writeReplace() {
        return new Externalizer(this);
    }


    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -7216149275959287094L;
        final SparseFloatVector mVector;
        public Externalizer() {
            this(null);
        }
        public Externalizer(SparseFloatVector vector) {
            mVector = vector;
        }
        public Object read(ObjectInput in) throws IOException {
            int len = in.readInt();
            int numDimensions = in.readInt();
            double length = in.readDouble();
            int[] keys = new int[len];
            for (int i = 0; i < keys.length; ++i)
                keys[i] = in.readInt();
            float[] values = new float[len];
            for (int i = 0; i < len; ++i)
                values[i] = in.readFloat();
            return new SparseFloatVector(keys,values,numDimensions,length);
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mVector.mKeys.length);
            out.writeInt(mVector.mNumDimensions);
            out.writeDouble(mVector.mLength);
            for (int i = 0; i < mVector.mKeys.length; ++i)
                out.writeInt(mVector.mKeys[i]);
            for (int i = 0; i < mVector.mValues.length; ++i)
                out.writeFloat(mVector.mValues[i]);
        }
    }

}

