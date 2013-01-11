package com.aliasi.matrix;

abstract class MatrixBackedVector extends AbstractVector {

    protected final Matrix mMatrix;
    protected final int mIndex;

    MatrixBackedVector(Matrix m, int index) {
        mMatrix = m;
        mIndex = index;
    }

    static class Row extends MatrixBackedVector implements Vector {
        Row(Matrix m, int index) {
            super(m,index);
        }
        public int numDimensions() {
            return mMatrix.numColumns();
        }
        public void setValue(int column, double value) {
            mMatrix.setValue(mIndex,column,value);
        }
        public double value(int column) {
            return mMatrix.value(mIndex,column);
        }
        public Vector add(Vector v) {
            return Matrices.add(this,v);
        }
    }

    static class Column extends MatrixBackedVector implements Vector {
        Column(Matrix m, int index) {
            super(m,index);
        }
        public int numDimensions() {
            return mMatrix.numRows();
        }
        public void setValue(int row, double value) {
            mMatrix.setValue(row,mIndex,value);
        }
        public double value(int row) {
            return mMatrix.value(row,mIndex);
        }
        public Vector add(Vector v) {
            return Matrices.add(this,v);
        }
    }

}
