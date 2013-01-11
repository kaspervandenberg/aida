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

import com.aliasi.util.AbstractCommand;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Properties;

public class AbstractCommandTest extends BaseTestCase {


    public void test1() {
        assertTrue(true);
    }

    public void testCSVs() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "-arg=foo,bar," });
        assertEqualsArray(new String[] { "foo", "bar", "" },
                          args.getCSV("arg"));
        boolean threw = false;
        try {
            args.getCSV("notAnArg");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void testOne() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { });
        assertFalse(args.hasFlag("foo"));
        assertNull(args.getBareArgument(1));
        assertNull(args.getArgument("bar"));
    }

    private static class TestCmd extends AbstractCommand {
        public TestCmd(String[] args) { super(args); }
        public void run() {
            getArgumentCreatableFile("file1");
            // File f2 = getArgumentCreatableFile("file2");
        }
    }

    public void testFilePrefixes() {
        // test result of submitted bug that file1 failed w/o parent
        new TestCmd(new String[] {"-file1=abc","-file2=build/b","c"}).run();
    }

    public void testTwo() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "foo", "bar" });
        assertFalse(args.hasFlag("foo"));
        assertEquals("foo",args.getBareArgument(0));
        assertEquals("bar",args.getBareArgument(1));
        assertNull(args.getArgument("bar"));
    }

    public void testThree() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "-foo", "bar" });
        assertTrue(args.hasFlag("foo"));
        assertEquals("bar",args.getBareArgument(0));
        assertNull(args.getArgument("baz"));
    }

    public void testFour() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "-foo", "bar", "-baz=ping" });
        assertTrue(args.hasFlag("foo"));
        assertEquals("bar",args.getBareArgument(0));
        assertEquals("ping",args.getArgument("baz"));
    }

    public void testFive() {
        Properties defaults = new Properties();
        defaults.setProperty("a","b");
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "-foo", "bar", "-baz=ping" },
                                       defaults);
        assertTrue(args.hasFlag("foo"));
        assertEquals("bar",args.getBareArgument(0));
        assertEquals("ping",args.getArgument("baz"));
        assertEquals("b",args.getArgument("a"));
    }

    public void testExceptions() {
    new CommandLineArguments(new String[] { "bar", "-foo=" });

        try {
            new CommandLineArguments(new String[] { "-=val", "bar" });
        fail();
        } catch (IllegalArgumentException e) {
        succeed();
        }
    }

    public void testHasArgument() {
        String[] argArray = new String[] { "-foo", "-bar=17", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);
        assertTrue(args.hasArgument("bar"));
        assertFalse(args.hasArgument("abc"));
    }

    public void testGetInt() {
        String[] argArray = new String[] { "-foo", "-bar=17", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);
        assertEquals(17,args.getArgumentInt("bar"));
        boolean threw = false;
        try {
            args.getArgumentInt("baz");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void testGetDouble() {
        String[] argArray = new String[] { "-foo", "-bar=17.9", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);
        assertEquals(17.9,args.getArgumentDouble("bar"),0.005);
        boolean threw = false;
        try {
            args.getArgumentInt("baz");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void testHasProperty() {
        String[] argArray = new String[] { "-foo", "-bar=17.9", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);

        assertTrue(args.hasFlag("foo"));
        assertFalse(args.hasFlag("bar"));
        assertFalse(args.hasFlag("boo"));

        assertFalse(args.hasProperty("foo"));
        assertTrue(args.hasProperty("bar"));
        assertTrue(args.hasProperty("baz"));
    }

    private static class CommandLineArguments extends AbstractCommand {

        public CommandLineArguments(String[] args) {
            super(args);
        }

        public CommandLineArguments(String[] args, Properties props) {
            super(args,props);
        }

        public void run() { 
            /* do nothing */
        }
    }

}
