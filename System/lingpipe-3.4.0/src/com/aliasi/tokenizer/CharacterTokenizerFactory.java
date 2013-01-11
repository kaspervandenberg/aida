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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A <code>CharacterTokenizerFactory</code> considers each
 * non-whitespace character in the input to be a distinct token.  This
 * factory is useful for handling languages such as Chinese, which
 * includes thousands of characters and presents a difficult tokenization
 * problem for standard tokenizers.
 *
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe1.0
 */
public class CharacterTokenizerFactory implements Compilable, TokenizerFactory {

    /**
     * Construct a character tokenizer factory.  
     *
     * <p><i>Implementation Note:</i> All character tokenizer
     * factories behave the same way, and they are thread safe, so the
     * constant {@link #FACTORY} may be used anywhere a freshly
     * constructed character tokenizer factory is used, without loss
     * of performance.
     */
    public CharacterTokenizerFactory() { 
        /* do nothing */
    }

    /**
     * Returns a character tokenizer for the specified character
     * array slice.
     *
     * @param ch Characters to tokenize.
     * @param start Index of first character to tokenize.
     * @param length Number of characters to tokenize.
     */
    public Tokenizer tokenizer(char[] ch, int start, int length) {
        return new CharacterTokenizer(ch,start,length);
    }

    /**
     * A constant instance of a character tokenizer factory.  Note that
     * compiled versions are all equal to this factory.
     */
    public static final TokenizerFactory FACTORY
        = new CharacterTokenizerFactory();

    /**
     * Compiles this tokenizer factory to the specified object output.
     * The tokenizer factory read back in is reference identical
     * to the static constant {@link #FACTORY}.
     *
     * @param objOut Object output to which this tokenizer factory is
     * compiled.
     * @throws IOException If there is an I/O error during the write.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer());
    }

    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 1313238312180578595L;
        public Externalizer() { 
            /* do nothing */
        }
        public void writeExternal(ObjectOutput objOut) { 
            /* do nothing */
        }
        public Object read(ObjectInput objIn) { return FACTORY; }
    }



}
