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

/**
 * The <code>Matrices</code> class contains static utility methods
 * for various matrix properties and operations.
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.0
 */
public class Matrices {

    // don't allow instances
    private Matrices() {
        /* do nothing */
    }

    /**
     * Returns <code>true</code> if the specified matrix has
     * only zero values on its diagonal.  If the matrix is
     * not square or has non-zero values on the diagonal, the
     * return result is <code>false</code>.
     *
     * @param m Matrix to test.
     * @return <code>true</code> if the matrix is square and has
     * only zero values on its diagonal.
     */
    public static boolean hasZeroDiagonal(Matrix m) {
        int n = m.numRows();
        if (n != m.numColumns())
            return false;
        for (int i = 0; i < n; ++i)
            if (m.value(i,i) != 0.0)
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if the specified matrix is symmetric.
     * A matrix <code>m</code> is symmetric if it's equal to its
     * transpose, <code>m = m<sup><sup>T</sup></sup></code>.  Stated
     * directly, a matrix <code>m</code> is symmetric if it has the
     * same number of rows as columns:
     *
     * <blockquote><code>
     * m.numRows() == m.numColumns()
     * </code></blockquote>
     *
     * and meets the symmetry condition:
     *
     * <blockquote>
     * <code>m.value(i,j) == m.value(j,i)</code>
     * for <code>i,j &lt; m.numRows()</code>.
     * </blockquote>
     *
     * @param m Matrix to test.
     * @return <code>true</code> if the matrix is symmetric.
     */
    public static boolean isSymmetric(Matrix m) {
        int n = m.numRows();
        if (n != m.numColumns()) return false;
        for (int i = 0; i < n; ++i)
            for (int j = i+1; j < n; ++j)
                if (m.value(i,j) != m.value(j,i))
                    return false;
        return true;
    }


    /**
     * Returns <code>true</code> if the matrix contains only positive
     * numbers or zeros.  If it contains a finite negative number,
     * {@link Double#NaN}, or {@link Double#NEGATIVE_INFINITY}, the
     * result will be <code>false</code>.
     *
     * @param m Matrix to test.
     * @return <code>true</code> if the matrix contains only positive
     * entries or zeros.
     */
    public static boolean isNonNegative(Matrix m) {
        for (int i = 0; i < m.numRows(); ++i)
            for (int j = 0; j < m.numColumns(); ++j)
                if (m.value(i,j) < 0.0 || Double.isNaN(m.value(i,j)))
                    return false;
        return true;
    }

    /**
     * Returns the content of the specified vector as an array.
     *
     * @param v The vector.
     * @return The content of the vector as an array.
     */
    public static double[] toArray(Vector v) {
        double[] xs = new double[v.numDimensions()];
        for (int i = 0; i < xs.length; ++i)
            xs[i] = v.value(i);
        return xs;
    }

    static Vector add(Vector v1, Vector v2) {
        int numDimensions = v1.numDimensions();
        if (numDimensions != v2.numDimensions()) {
            String msg = "Can only add vectors of the same dimensionality."
                + " Found v1.numDimensions()=" + v1.numDimensions()
                + " v2.numDimensions()=" + v2.numDimensions();
            throw new IllegalArgumentException(msg);
        }
        double[] vals = new double[numDimensions];
        for (int i = 0; i < numDimensions; ++i)
            vals[i] = v1.value(i) + v2.value(i);
        return new DenseVector(vals);
    }


}


