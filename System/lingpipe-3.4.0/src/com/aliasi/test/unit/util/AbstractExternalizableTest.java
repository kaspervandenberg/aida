package com.aliasi.test.unit.util;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.aliasi.test.unit.BaseTestCase;

public class AbstractExternalizableTest extends BaseTestCase {

    public void testCompile() throws IOException, ClassNotFoundException {
        CompilableInteger ci = new CompilableInteger(5);
        assertEquals(new Integer(5),
                     AbstractExternalizable.compile(ci));

    }

    public void testExternalizable()
        throws IOException, ClassNotFoundException {

        ECompilableInteger ci = new ECompilableInteger(5);
        assertEquals(new Integer(5),
                     AbstractExternalizable.compile(ci));

    }

    public void testEitherOr() throws IOException, ClassNotFoundException {
        TestBoth t1 = new TestBoth();

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        AbstractExternalizable.serializeOrCompile(t1,objOut);
        ByteArrayInputStream bytesIn
            = new ByteArrayInputStream(bytesOut.toByteArray());
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);
        Object x = objIn.readObject();
        assertEquals(new Integer(5),x);

        bytesOut = new ByteArrayOutputStream();
        objOut = new ObjectOutputStream(bytesOut);
        AbstractExternalizable.compileOrSerialize(t1,objOut);
        bytesIn
            = new ByteArrayInputStream(bytesOut.toByteArray());
        objIn = new ObjectInputStream(bytesIn);
        x = objIn.readObject();
        assertEquals(new Integer(10),x);
    }

    static class TestBoth implements Compilable, Serializable {
        public void compileTo(ObjectOutput out) throws IOException {
            out.writeObject(new Integer(10));
        }
        public Object writeReplace() {
            return new Integer(5);
        }
    }


    private static class ECompilableInteger implements Compilable {
        private final int mVal;
        public ECompilableInteger(int val) { mVal = val; }
        public void compileTo(ObjectOutput out) throws IOException {
            out.writeObject(new Externalizer(this));
        }

        private static class Externalizer extends AbstractExternalizable {
            private static final long serialVersionUID = -8136168142449855303L;
            private final ECompilableInteger mCI;
            public Externalizer() {
                this(null);
            } // required for deserialization
            public Externalizer(ECompilableInteger ci) { mCI = ci; }
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeInt(mCI.mVal);
            }
            public Object read(ObjectInput in) throws IOException {
                return new Integer(in.readInt());
            }
        }
    }

    private static class CompilableInteger implements Compilable {
        private final int mVal;
        public CompilableInteger(int val) {
            mVal = val;
        }
        public void compileTo(ObjectOutput out) throws IOException {
            out.writeObject(new Integer(mVal));
        }
    }



}
