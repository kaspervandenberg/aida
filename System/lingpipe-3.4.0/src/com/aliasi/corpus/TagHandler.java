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
 * The <code>TagHandler</code> interface specifies a single method for
 * operating on an array of tokens, whitespaces and tags.  The
 * standard use of a tag handler is as a visitor whose handle method
 * is invoked as a callback by a data parser, such as an
 * implementation of tag parser.
 *
 * <P>This handler interface does not specify the form of the tags
 * that are being handled.  Implementations of tag handlers may be
 * more particular about the form of tags they get.  In particular,
 * the begin-in-out (BIO) chunk tagging scheme is described in 
 * in {@link ChunkHandlerAdapter}.
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public interface TagHandler extends Handler {

    /**
     * Handles the specified tokens, whitespaces and tags.
     *
     * <p>Implementations may throw an illegal argument exception if the
     * number of tokens is not equal to the number of tags, or if the
     * number of whitespaces is not one greater than the number of
     * tokens; implemenetations may also choose to support calls with
     * a <code>null</code> array of whitespaces.
     *
     * @param toks Array of tokens.
     * @param whitespaces Array of whitespaces.
     * @param tags Array of tags.
     * @throws IllegalArgumentException If the specified tokens,
     * whitespaces and tags are not well-formed according to the
     * requirements of the handler.
     */
    public void handle(String[] toks, String[] whitespaces, String[] tags);

}

