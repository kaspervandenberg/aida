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
 * The <code>Classifier</code> interface specifies a single method
 * that returns the classification of an input object.  A
 * classification is only required to provide a first-best
 * categorization of the input.  Classifiers may return subclasses of
 * <code>Classification</code> that include n-best results with
 * numerical scores of various interpretations.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public interface Classifier<E,C extends Classification> {

    /**
     * Return the classification of the specified object.
     * Implementations may return more fine-grained results by
     * returning a subclass of <code>Classification</code>.
     *
     * @param input Object to classify.
     * @return Classification of the object.
     */ 
    public C classify(E input);

}
