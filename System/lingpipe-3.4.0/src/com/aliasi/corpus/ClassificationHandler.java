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

import com.aliasi.classify.Classification;

/**
 * The <code>ClassificationHandler</code> interface specifies a single
 * method for operating on a classification input and result.  Because
 * {@link Classification} objects may actually implement a subinterface
 * of <code>Classification</code>, this handler may be used for
 * training on richer than first-best classification results.
 *
 * @author  Bob Carpenter
 * @version 2.3.1
 * @since   LingPipe2.3.1
 */
public interface ClassificationHandler<E, C extends Classification> extends Handler {

    /**
     * Handle the specified object and its classification.
     *
     * <p>Implementations may throw an illegal argument exception
     * if the classification is not of the required
     *
     */
    public void handle(E input, C classification);

}
