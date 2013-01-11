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
 * The <code>StringArrayHandler</code> interface supplies a single method
 * taking an array of strings as an argument.
 *
 * @author  Bob Carpenter
 * @version 3.3
 * @since   LingPipe2.2
 */
public interface StringArrayHandler extends ObjectHandler<String> {

    /**
     * Handle the specified array of strings.
     *
     * @param xs Array of strings to handle.
     */
    public void handle(String[] xs);

}
