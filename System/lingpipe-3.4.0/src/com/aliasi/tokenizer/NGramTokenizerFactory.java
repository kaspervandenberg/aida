package com.aliasi.tokenizer;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.Serializable;

/**
 * An <code>NGramTokenizerFactory</code> creates n-gram tokenizers
 * of a specified minimum and maximun length.
 *
 * <p>An <code>NGramTokenizer</code> is a tokenizer that returns the
 * character n-grams from a specified sequence between a minimum
 * and maximum length.  Whitespace takes the default behavior from
 * {@link Tokenizer#nextWhitespace()}, returning a string consisting of
 * a single space character.
 *
 * <p>For example, the result of
 * <blockquote>
 *   <code>
 *     new NGramTokenizer("abcd".toCharArray(),0,4,2,3).tokenize()
 *   </code>
 * </blockquote>
 * is the string array:
 * <blockquote>
 *   <code>
 *     { "ab", "bc", "cd", "abc", "bcd" }
 *   </code>
 * </blockquote>
 *
 * <h3>Serialization and Compilation</h3>

 * <p>N-gram tokenizers are serializable and compilable.  Both
 * operations write the n-gram bounds to the output stream and read
 * back in an instance of this class with those bounds.
 *
 * @author  Bob Carpenter
 * @version 3.1.3
 * @since   LingPipe1.0
*/
public class NGramTokenizerFactory
    implements TokenizerFactory, Serializable, Compilable {

    private final int mMinNGram;
    private final int mMaxNGram;

    /**
     * Create an n-gram tokenizer factory with the specified minimum
     * and maximum n-gram lengths.
     *
     * @param minNGram Minimum n-gram length.
     * @param maxNGram Maximum n-gram length.
     * @throws IllegalArgumentException If the minimum is greater than
     * the maximum or if the maximum is less than one.
    */
    public NGramTokenizerFactory(int minNGram, int maxNGram) {
    if (maxNGram < 1) {
        String msg = "Require max >= 1."
        + " Found maxNGram=" + maxNGram;
        throw new IllegalArgumentException(msg);
    }
           if (minNGram > maxNGram) {
        String msg = "Require min <= max."
        + " Found min=" + minNGram
        + " max=" + maxNGram;
        throw new IllegalArgumentException(msg);
    }
    mMinNGram = minNGram;
    mMaxNGram = maxNGram;
    }

    /**
     * Compiles this n-gram tokenizer factory to the specified
     * object output stream.
     *
     * @param objOut Output stream to which to write the tokenizer
     * factory.
     * @throws IOException If there is an exception writing the
     * parameters.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    private void writeExternal(ObjectOutput objOut) throws IOException {
        compileTo(objOut);
    }

    /**
     * Returns an n-gram tokenizer for the specified characters
     * with the minimum and maximum n-gram lengths as specified
     * in the constructor.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in array to tokenize.
     * @param length Number of characters to tokenize.
     */
    public Tokenizer tokenizer(char[] cs, int start, int length) {
        return new NGramTokenizer(cs,start,length,mMinNGram,mMaxNGram);
    }

    private static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = 7342984199917470310L;
        final NGramTokenizerFactory mFactory;
        public Externalizer() {
            this(null);
        }
        public Externalizer(NGramTokenizerFactory factory) {
            mFactory = factory;
        }
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mFactory.mMinNGram);
            objOut.writeInt(mFactory.mMaxNGram);
        }
        public Object read(ObjectInput objIn) throws IOException {
            int minNGram = objIn.readInt();
            int maxNGram = objIn.readInt();
            return new NGramTokenizerFactory(minNGram,maxNGram);
        }
    }


}
