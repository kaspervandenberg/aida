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

package com.aliasi.test.unit.util;

import com.aliasi.util.Reflection;

import com.aliasi.test.unit.BaseTestCase;

public class ReflectionTest extends BaseTestCase {

    public void testSubmittedRecursionBug() {
        Reflection.newInstance("java.lang.String",
                               new Object[] { "foo" },
                               new String[] { "java.lang.String" });
        succeed();
    }

    public void testNewInstance() {

        assertEquals("abc",
                     Reflection.newInstance("java.lang.String",
                                            new Object[] { "abc" }));
        assertEquals("",
                     Reflection.newInstance("java.lang.StringBuffer",
                                            new Object[] { }).toString());

        assertEquals("",
                     Reflection.newInstance("java.lang.StringBuffer").toString());

        // NoSuchMethodException
    try {
        Reflection.newInstance("java.lang.Object",
                   new Object[] { "abc" });
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }

    try {
        Reflection.newInstance("java.lang.Integer",
                   new Object[] { "abc" });
        // InvocationTargetException        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }

    try {
        Reflection.newInstance("java.lang.foobar",
                   new Object[] { "abc" });
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }



        // InstantiationException
        // need to write own class to do this

        // ExceptionInInitializerError
        // need to write own class to do this

        // IllegalArgumentException
        // shouldn't be able to throw this other than as wrapper, because of synch

    }

}
