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

/**
 * A character tokenizer treats each character as a token.  A sequence
 * of whitespace is treated as a single token.
 *
 * @author  Bob Carpenter
 * @version 1.0.8
 * @since   LingPipe1.0
 */
class CharacterTokenizer extends Tokenizer {

    private final char[] mChars;
    private final int mLastPosition;
    private int mPosition;

    public CharacterTokenizer(char[] ch, int offset, int length) {
    mChars = ch;
    mPosition = offset;
    mLastPosition = offset+length;
    }

    public String nextWhitespace() {
        StringBuffer sb = new StringBuffer();
        while (hasMoreCharacters()
               && Character.isWhitespace(currentChar())) {
            sb.append(currentChar());
            ++mPosition;
        }
        return sb.toString();
    }

    public String nextToken() {
    skipWhitespace();
    if (!hasMoreCharacters()) return null;
    return new String(new char[] { mChars[mPosition++] });
    }

    private void skipWhitespace() {
    while (hasMoreCharacters() 
           && Character.isWhitespace(currentChar()))
        ++mPosition;
    }

    private boolean hasMoreCharacters() {
    return mPosition < mLastPosition;
    }

    private char currentChar() {
        return mChars[mPosition];
    }

}
