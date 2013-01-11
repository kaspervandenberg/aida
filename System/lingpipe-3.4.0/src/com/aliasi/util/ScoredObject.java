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
 * A <code>ScoredObject</code> provides an implementation of the
 * <code>Scored</code> interface with an attached object.  Scored
 * objects are immutable and identity is reference.  The object
 * returned by the getter {@link #getObject()} is the actual object
 * stored, so changes to it will affect the scored object of which it
 * is a part.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class ScoredObject<E> implements Scored {

    private final E mObj;
    private final double mScore;
    
    /**
     * Construct a scored object from the specified object
     * and score.
     *
     * @param obj Object for the constructed scored object.
     * @param score Score for the constructed scored object.
     */
    public ScoredObject(E obj, double score) {
        mObj = obj;
        mScore = score;
    }

    /**
     * Returns the object attached to this scored object.
     *
     * @return The object attached to this scored object.
     */
    public E getObject() {
        return mObj;
    }

    /**
     * Returns the score for this scored object.
     *
     * @return The score for this scored object.
     */
    public double score() {
        return mScore;
    }

    /**
     * Returns a string-based representation of this object consisting
     * of the score followed by a colon (<code>':'</code>), followed
     * by the object converted to a string.
     *
     * @return The string-based representation of this object.
     */
    public String toString() {
        return mScore + ":" + getObject();
    }


    // package privates can't go in interface, so park them here

    static class ScoredComparator implements Comparator<Scored> {
        public int compare(Scored obj1, Scored obj2) {
            return obj1.score() > obj2.score()
                ? 1
                : ( obj1.score() < obj2.score()
                    ? -1
                    : 0 );
        }
    };

    static class ReverseScoredComparator 
        implements Comparator<Scored> {

        public int compare(Scored obj1, Scored obj2) {
            return obj1.score() > obj2.score()
                ? -1
                : ( obj1.score() < obj2.score()
                    ? 1
                    : 0 );
        }
    };

    

}
