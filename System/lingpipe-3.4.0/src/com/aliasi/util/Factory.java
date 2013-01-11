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

/**
 * A <code>Factory</code> provides a generic interface for object
 * creation of a specified type.  The interface specifies a single
 * method, {@link #create()}, which returns an object of the factory's
 * type.
 *
 * @author  Bob Carpenter
 * @version 3.3.1
 * @since   LingPipe2.0
 */
public abstract class Factory<E> {

    /**
     * Return an instance created by this factory.
     *
     * @return An instance.
     */
    public abstract E create();

}
