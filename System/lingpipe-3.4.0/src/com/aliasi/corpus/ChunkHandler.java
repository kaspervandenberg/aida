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

import com.aliasi.chunk.Chunking;

/**
 * The <code>ChunkHandler</code> interface specifies a single method
 * for operating on a chunking.  The standard use of a chunk handler
 * is as a visitor whose handle method is involked as a callback by
 * a data parser or decoder.
 *
 * @author  Bob Carpenter
 * @version 3.3
 * @since   LingPipe2.1
 */
public interface ChunkHandler extends ObjectHandler<Chunking> {

    /**
     * Handle the specified chunking.
     *
     * @param chunking Chunking to handle.
     */
    public void handle(Chunking chunking);

}
