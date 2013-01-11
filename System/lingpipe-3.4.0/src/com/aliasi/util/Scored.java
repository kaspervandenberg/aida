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

package com.aliasi.util;

import java.util.Comparator;

/**
 * The <code>Scored</code> interface should be implemented by objects
 * that return a double-valued score.  There is a simple score
 * comparator, and scored objects are the natural wrappers of other
 * objects in priority queues.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public interface Scored {

    /**
     * Returns the score for this object.  
     *
     * @return The score for this object.
     */
    public double score();


    /**
     * A comparator that compares scored objects by their score.  Note
     * that this comparator may not be consistent with natural
     * equality on a scored object which may depend on factors in
     * addition to the score.  This comparator may be used as the
     * priority ordering for a priority queue of objects sorted by
     * score.  It may also be passed to {@link
     * java.util.Arrays#sort(Object[],Comparator)}.
     */
    public static final Comparator<Scored> SCORE_COMPARATOR
        = new ScoredObject.ScoredComparator();

    /**
     * A comparator that orders scored objects in reverse score order.
     * Thus it returns the negative value of that returned by {@link
     * #SCORE_COMPARATOR}, and like that ordering, is not consistent
     * with object equality.  This comparator is useful for priority
     * queues.
     */
    public static final Comparator<Scored> REVERSE_SCORE_COMPARATOR
        = new ScoredObject.ReverseScoredComparator();

}
