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

package com.aliasi.tokenizer;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Counter;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.util.Map;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A <code>TokenFeatureExtractor</code> produces feature vectors from
 * character sequences representing token counts.
 *
 * <h3>Serialization</h3>
 *
 * <p>The token feature extractors implement the {@link Serializable}
 * interface.  A token feature extractor will actually be serializable
 * if the underlying tokenizer factory is serializable, either by
 * implementing the {@link Serializable} interface or the {@link
 * Compilable} interface.  If it is not, attempting to serialize the
 * feature extractor will throw an exception.
 *
 * @author  Bob Carpenter
 * @version 3.1.3
 * @since   LingPipe3.1
 */
public class TokenFeatureExtractor
    implements FeatureExtractor<CharSequence>,
               Serializable {

    static final long serialVersionUID = -1946484959983081450L;

    private final TokenizerFactory mTokenizerFactory;

    /**
     * Construct a token-based feature extractor from the
     * specified tokenizer factory.
     *
     * @param factory Tokenizer factory to use for tokenization.
     */
    public TokenFeatureExtractor(TokenizerFactory factory) {
        mTokenizerFactory = factory;
    }

    /**
     * Return the feature vector for the specified character sequence.
     * The keys are the tokens extracted and their values is the count
     * of the token in the input character sequence.
     *
     * @param in Character sequence from which to extract features.
     * @return Mapping from tokens in the input sequence to their
     * counts.
     */
    public Map<String,Counter> features(CharSequence in) {
        ObjectToCounterMap<String> map = new ObjectToCounterMap<String>();
        char[] cs = Strings.toCharArray(in);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        for (String token : tokenizer)
            map.increment(token);
        return map;
    }

    private Object writeReplace() {
        return new Externalizer(this);
    }

    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = 4716086241839692672L;
        private final TokenFeatureExtractor mExtractor;
        public Externalizer() {
            this(null);
        }
        public Externalizer(TokenFeatureExtractor extractor) {
            mExtractor = extractor;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            TokenizerFactory factory = mExtractor.mTokenizerFactory;
            if (factory instanceof Compilable)
                ((Compilable) factory).compileTo(out);
            else
                out.writeObject(factory);
        }
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            TokenizerFactory factory
                = (TokenizerFactory) in.readObject();
            return new TokenFeatureExtractor(factory);
        }
    }

}

