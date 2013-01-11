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

import java.util.Locale;

/**
 * A <code>LowerCaseFilterTokenizer</code> renders all of its
 * tokens in lower case as defined by {@link String#toLowerCase()}.
 * The scheme for lower-casing is determined by a {@link Locale}.
 *
 * @author  Bob Carpenter
 * @version 2.4
 * @since   LingPipe1.0
 */
public class LowerCaseFilterTokenizer extends TokenFilterTokenizer {


    private final Locale mLocale;

    /**
     * Construct a new lower case filter tokenizer with a default
     * locale of {@link Locale#ENGLISH}.
     *
     * @param tokenizer Contained tokenizer.
     */
    public LowerCaseFilterTokenizer(Tokenizer tokenizer) {
        this(tokenizer,Locale.ENGLISH);
    }

    /**
     * Construct a lower case filter tokenizer with the specified
     * tokenizer using the locale-specific lower-casing rules.
     *
     * @param tokenizer Contained tokenizer.
     * @param locale Locale to use for lower-casing.
     */
    public LowerCaseFilterTokenizer(Tokenizer tokenizer, Locale locale) {
        super(tokenizer);
        mLocale = locale;
    }

    /**
     * Returns the next token from the contained tokenizer,
     * converted to lower case.
     *
     * @param token Token to convert to lower case.
     * @return Lower-cased version of specified token.
     */
    public String filter(String token) {
        return token.toLowerCase(mLocale);
    }



}
