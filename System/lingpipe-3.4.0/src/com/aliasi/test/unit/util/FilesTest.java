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

import com.aliasi.util.Files;

import com.aliasi.test.unit.BaseTestCase;

import java.io.File;
import java.io.IOException;

public class FilesTest extends BaseTestCase {

    public void testTempDir() {
        assertTrue(Files.TEMP_DIRECTORY.exists());
        assertTrue(Files.TEMP_DIRECTORY.isDirectory());
    }

    public void testNonCVSDirectoryFilter() {
        File cvsDirectory = new File(Files.TEMP_DIRECTORY,"CVS");
        assertNotNull(cvsDirectory);
        cvsDirectory.mkdir();
        assertFalse(Files.NON_CVS_DIRECTORY_FILE_FILTER.accept(cvsDirectory));
        assertFalse(Files.NON_CVS_DIRECTORY_FILE_FILTER.accept(cvsDirectory));
        assertFalse(Files.NON_CVS_DIRECTORY_FILE_FILTER.accept(new File(cvsDirectory,"foo")));
    }

    public void testTempFile() {
        File file = Files.createTempFile("foobar");
        assertNotNull(file);
        assertFalse(file.exists());
    }

    public void testReadWriteChars() throws IOException {
        readWriteToFile(new char[] { });
        readWriteToFile(new char[] { 'a', 'b', 'c' });
        char[] chars = new char[100000];
        for (int i = 0; i < 100000; ++i) chars[i] = 'a';
        readWriteToFile(chars);
    }

    public void testReadWriteString() throws IOException {
        readWriteToFile("");
        readWriteToFile("abc");
        readWriteToFile("flkajsdf;lkjadflkjadfljadslfj\n");
    }

    public void testReadWriteBytes() throws IOException {

        readWriteToFile(new byte[] { });
        readWriteToFile(new byte[] { (byte) 12, (byte) 13, (byte) 14 });
        byte[] bytes = new byte[100000];
        for (int i = 0; i < 100000; ++i) bytes[i] = (byte) 12;
        readWriteToFile(bytes);
    }

    public void testPrefix() {
        assertEquals("",Files.prefix(""));
        assertEquals("foo",Files.prefix("foo.bar"));
        assertEquals("foo",Files.prefix("foo"));
    }

    public void testSuffix() {
        assertEquals("",Files.suffix(""));
        assertEquals("bar",Files.suffix("foo.bar"));
        assertEquals("",Files.suffix("foo"));
    }

    public void testBaseName() {
        assertEquals("foo",Files.baseName(Files.createTempFile("foo.bar")));
        assertEquals("foo",Files.baseName(Files.createTempFile("foo")));
    }

    public void testRemoveRecursive() throws IOException {
        File dir1 = Files.createTempFile("dir1");
        dir1.mkdirs();
        File dir2 = new File(dir1,"dir2");
        dir2.mkdirs();
        File file1 = new File(dir2,"test.txt");
        Files.writeBytesToFile(new byte[] { (byte) 12 },file1);
        assertTrue(dir1.exists());
        assertTrue(file1.exists());
        int numRemoved = Files.removeRecursive(dir1);
        assertEquals(3,numRemoved);
        assertFalse(dir1.exists());
        assertFalse(file1.exists());
    }

    public void testRemoveDescendants() throws IOException {
        File dir1 = Files.createTempFile("dir1");
        dir1.mkdirs();
        File file2 = new File(dir1,"foo.txt");
        Files.writeBytesToFile(new byte[] { (byte) 13 }, file2);
        File dir2 = new File(dir1,"dir2");
        dir2.mkdirs();
        File file1 = new File(dir2,"test.txt");
        Files.writeBytesToFile(new byte[] { (byte) 12 },file1);
        assertTrue(dir1.exists());
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        int numRemoved = Files.removeDescendants(dir1);
        assertEquals(3,numRemoved);
        assertTrue(dir1.exists());
        assertFalse(file1.exists());
        assertFalse(file2.exists());
    }

    public void testReadLines() throws IOException {
        File f1 = Files.createTempFile("file1");
        Files.writeCharsToFile("abc\nde".toCharArray(),f1,"UTF8");
        assertEqualsArray(new String[] { "abc", "de" },
                          Files.readLinesFromFile(f1,"UTF8"));
        Files.writeCharsToFile("abc\nde\n".toCharArray(),f1,"ISO8859-1");
        assertEqualsArray(new String[] { "abc", "de" },
                          Files.readLinesFromFile(f1,"ISO8859-1"));

    }

    public void testMakeCleanDir() throws IOException {
        File dir1 = Files.createTempFile("cleandir");
        assertFalse(dir1.exists());
        Files.makeCleanDir(dir1);
        assertTrue(dir1.exists());

        File dir2 = Files.createTempFile("cleandir");
        dir2.mkdir();
        File dir3 = new File(dir1,"dtr");
        dir3.mkdir();
        assertTrue(dir2.exists());
        assertTrue(dir3.exists());
        Files.makeCleanDir(dir1);
        assertTrue(dir2.exists());
        assertFalse(dir3.exists());
    }

    public void testMakeCleanDir2() throws IOException {
        File dir4 = Files.createTempFile("dir4");
        Files.makeCleanDir(dir4);
        assertFalse(new File(dir4,"cleandir").exists());
        File cleanDir = Files.makeCleanDir(dir4,"cleandir");
        assertTrue(cleanDir.exists());

    }


    private void readWriteToFile(byte[] bytes) throws IOException {
        File file = Files.createTempFile("bytesTest.tmp");
        Files.writeBytesToFile(bytes,file);
        byte[] bytes2 = Files.readBytesFromFile(file);
        assertEqualsArray(bytes,bytes2);
    }

    private void readWriteToFile(char[] chars) throws IOException {
        File file = Files.createTempFile("charsTest.tmp");
        Files.writeCharsToFile(chars,file);
        char[] chars2 = Files.readCharsFromFile(file);
        assertEqualsArray(chars,chars2);

        Files.writeCharsToFile(chars,file,"ISO8859-1");
        char[] chars3 = Files.readCharsFromFile(file,"ISO8859-1");
        assertEqualsArray(chars,chars3);
    }

    private void readWriteToFile(String s) throws IOException {
        File file = Files.createTempFile("charsTest.tmp");
        Files.writeStringToFile(s,file);
        char[] chars2 = Files.readCharsFromFile(file);
        assertEqualsArray(s.toCharArray(),chars2);

        Files.writeStringToFile(s,file,"ISO-8859-1");
        char[] chars3 = Files.readCharsFromFile(file,"ISO-8859-1");
        assertEqualsArray(s.toCharArray(),chars3);
    }

    public void testFileToURLName() throws IOException {
        File tempFile = new File(Files.TEMP_DIRECTORY,"foo");
        assertEquals("file:///" + tempFile.getCanonicalPath(),
                     Files.fileToURLName(tempFile));
    }

    public void testSerializeDeserialize() throws IOException, ClassNotFoundException {
        Integer one = new Integer(1);
        String foo = "foo";
        assertFullEquals(one, Files.serializeDeserialize(one));
        assertFullEquals(foo, Files.serializeDeserialize(foo));
    }
}

