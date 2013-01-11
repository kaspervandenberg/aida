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

package com.aliasi.classify;

/**
 * A <code>Classification</code> provides a first-best category.
 * Subclasses provide n-best results with numerical scores of
 * various interpretations.
 *
 * @author Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Classification {

    private final String mBestCategory;

    /**
     * Construct a classification with the specified first-best
     * category.  
     *
     * @param bestCategory Category of this classification.
     */
    public Classification(String bestCategory) {
        mBestCategory = bestCategory;
    }

    /**
     * Returns the best-scoring category.
     *
     * @return Best category for this classification.
     */
    public String bestCategory() {
        return mBestCategory;
    }

    /**
     * Returns a string-based representation of the first-best
     * category of this classification.
     *
     * @return A string-based representation of this classification.
     */
    public String toString() {
        return "Rank    Category\n1=" + bestCategory();
    }

}
