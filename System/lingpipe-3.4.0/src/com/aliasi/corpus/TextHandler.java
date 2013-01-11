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

package com.aliasi.corpus;

/**
 * The <code>TextHandler</code> interface specifies a single method
 * for operating on a slice of characters.  The typical usage of a
 * text handler is as a visitor whose handle method is invoked as a
 * callback by a data parser, such as an implementation of {@link
 * Parser}.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public interface TextHandler extends Handler {

    /**
     * Handles the specified character slice.  
     *
     * @param cs Underlying array of characters.
     * @param start First character in slice.
     * @param length Number of characters in slice.
     */
    public void handle(char[] cs, int start, int length);

}
