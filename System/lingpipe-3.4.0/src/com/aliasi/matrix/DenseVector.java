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

import java.util.Arrays;

/**
 * A <code>DenseVector</code> is a vector implementation suitable for
 * vectors with primarily non-zero values.  The dimensioanality of
 * a dense vector is set at construction time and immutable afterwards.
 * Values may be specified at construction time or given default values.
 * Values may be set later.
 *
 * <P><i>Implementation Note:</i> A dense vector represents the values
 * with an array of primitive double values and the label with an
 * array of objects.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class DenseVector extends AbstractVector {

    static final boolean IGNORE = true; // ignore this value

    private final double[] mValues;

    /**
     * Construct a dense vector with the specified number of
     * dimensions.  All values will be set to <code>0.0</code>
     * initially.
     *
     * @param numDimensions The number of dimensions in this vector.
     * @throws IllegalArgumentException If the number of dimensions is
     * not positive.
     */
    public DenseVector(int numDimensions) {
        this(zeroValues(numDimensions),IGNORE);
        if (numDimensions < 1) {
            String msg = "Require positive number of dimensions."
                + " Found numDimensions=" + numDimensions;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Construct a dense vector with the specified values.  The
     * number of dimensions will be equal to the length of the
     * specified array of values.  The specified values are copied, so
     * subsequent changes to the specified values are not reflected
     * in this class.
     *
     * @param values Array of values for the vector.
     * @throws IllegalArgumentException If the specified values array
     * is zero length.
     */
    public DenseVector(double[] values) {
        this(copyValues(values),IGNORE);
        if (values.length < 1) {
            String msg = "Vectors must have positive length."
                + " Found length=" + values.length;
            throw new IllegalArgumentException(msg);
        }
    }

    DenseVector(double[] values, boolean ignore) {
        mValues = values;
    }

    /**
     * Sets the value of the specified dimension to the specified
     * value.
     *
     * @param dimension The specified dimension.
     * @param value The new value for the specified dimension.
     * @throws IndexOutOfBoundsException If the dimension is less than
     * 0 or greather than or equal to the number of dimensions of this
     * vector.
     */
    public void setValue(int dimension, double value) {
        mValues[dimension] = value;
    }

    /**
     * Returns the number of dimensions for this dense vector.  The
     * dimensionality is set at construction time and is immutable.
     *
     * @return The number of dimensions of this vector.
     */
    public int numDimensions() {
        return mValues.length;
    }

    public Vector add(Vector v) {
        return Matrices.add(this,v);
    }

    /**
     * Returns the value of this dense vector for the specified
     * dimension.
     *
     * @param dimension Specified dimension.
     * @return The value of this vector for the specified dimension.
     * @throws IndexOutOfBoundsException If the dimension is less than
     * 0 or greather than or equal to the number of dimensions of this
     * vector.
     */
    public double value(int dimension) {
        return mValues[dimension];
    }

    private static double[] zeroValues(int n) {
        double[] xs = new double[n];
        Arrays.fill(xs,0.0);
        return xs;
    }

    private static double[] copyValues(double[] values) {
        double[] xs = new double[values.length];
        System.arraycopy(values,0,xs,0,xs.length);
        return xs;
    }
}

