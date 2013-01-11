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

package com.aliasi.xml;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>CompositeFilter</code> is a SAX filter that is composed of
 * several filters through which events are passed.  A composite
 * filter is constructed from an array of filters, and when used as a
 * handler, delegates the events to the first filter in the sequence,
 * which in turn delegates them to the next filter down the line, all
 * the way down to the default handler set with {@link
 * #setHandler(DefaultHandler)}.
 *
 * @author  Bob Carpenter
 * @version 1.0.3
 * @since   LingPipe1.0
 */
public class CompositeFilter extends SAXFilterHandler {

    /**
     * The filter chain for this composite filter.
     */
    private final SAXFilterHandler[] mFilters;

    /**
     * Construct a composite filter from the specified array of
     * filters.  Events passed to the first filter in the list will be
     * handled by that filter, and then passed to the next filter in
     * the list, and so on, until the contained handler for the
     * constructed composite handler is reached.  The supplied array
     * should contain at least one element, but should contain at
     * least two filters to be useful.
     *
     * @param filters Chain of SAX filters making up the constructed
     * composite filter.
     * @throws NoSuchElementException If the supplied array of
     * filters does not have at least one element.
     */
    public CompositeFilter(SAXFilterHandler[] filters) {
    super(filters[0]);
    mFilters = filters;
    for (int i = 0; i+1 < filters.length; ++i) 
        mFilters[i].setHandler(mFilters[i+1]);
    }

    /**
     * Sets the handler for the composite filter.  This will in turn
     * set the specified handler for the last filter in this composite
     * filter's chain of filters.
     *
     * @link handler New contained handler.
     */
    public void setHandler(DefaultHandler handler) {
    mFilters[mFilters.length-1].setHandler(handler);
    }

}
