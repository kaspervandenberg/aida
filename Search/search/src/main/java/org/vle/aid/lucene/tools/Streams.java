package org.vle.aid.lucene.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import java.net.URL;

import org.xml.sax.InputSource;

/**
 * Static utility methods for processing input and output streams,
 * readers and writers.
 *
 */
public class Streams {

    /**
     * Forbid instance construction.
     */
    private Streams() { }

    /**
     * Returns Java's canonical name of the default character set for
     * the system's current default locale.  Note that this is
     * returned as a Java charset name, not an official mime name.
     *
     * <P><i>Note:</i> This method is available in the J2EE version
     * of Java as <code>javax.mail.internet.getDefaultJavaCharset()</code>.
     *
     * <P><i>Note 2:</i> For example, the standard English install
     * of Sun's J2SE 1.4.2 on windows sets the default character set to
     * <code>&quot;Cp1252&quot;</code>, the Windows variant of Latin1.
     *
     * @return The default charset for the current platform.
     */
    public static String getDefaultJavaCharset() {
        byte[] bytes = new byte[0];
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        InputStreamReader defaultReader = new InputStreamReader(bytesIn);
        return defaultReader.getEncoding();
    }

    /**
     * Reads the full contents of the specified reader and returns
     * it as a character array.
     *
     * @param reader Reader from which to get characters.
     * @throws IOException If there is an underlying I/O exception.
     */
    public static char[] toCharArray(Reader reader) throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        copy(reader,writer);
        return writer.toCharArray();
    }

    /**
     * Reads the character content from the specified input source and
     * returns it as a character array.  If the input source has a
     * specified character stream (a <code>Reader</code>), then that
     * is used.  If it has a specified byte stream (an
     * <code>InputStream</code>), then that is used.  If it has
     * neither a character nor a byte stream, a byte stream is created
     * from the system identifier (URL).  For both specified and
     * URL-constructed byte streams, the input source's specified
     * character set will be used if it is specified.
     *
     * <P>I/O errors will arise from errors reading from a specified
     * stream, from the character set conversion on a byte stream, or
     * from errors forming or opening a URL specified as a system
     * identifier.
     *
     * <P>Note that this method does <i>not</i> close the streams within the
     * input source.
     *
     * @param in Input source from which to read.
     * @throws IOException If there is an I/O error reading.
     */
    public static char[] toCharArray(InputSource in) throws IOException {
	Reader reader = null;
	InputStream inStr = null;
	reader = in.getCharacterStream();
	if (reader == null) {
	    inStr = in.getByteStream();
	    if (inStr == null)
		inStr = new URL(in.getSystemId()).openStream();
	    String charset = in.getEncoding();
	    if (charset == null)
		reader = new InputStreamReader(inStr);
	    else
		reader = new InputStreamReader(inStr,charset);
	}
	return toCharArray(reader);
    }

    /**
     * Copies the content of the reader into the writer.  Blocks if
     * the reader or writer block.  Does not close the reader or
     * writer when finished.
     *
     * @param reader Reader to copy from.
     * @param writer Writer to copy to.
     * @throws IOException If there is an underlying I/O exception.
     */
    public static void copy(Reader reader, Writer writer)
        throws IOException {

        char[] buffer = new char[CHAR_COPY_BUFFER_SIZE];
        int numChars;
        while ((numChars = reader.read(buffer)) > 0)
            writer.write(buffer,0,numChars);
    }


    /**
     * Copies the content of the input stream into the output stream.
     * Blocks if the input or output streams block.  Does not close the
     * streams.
     *
     * @param in Input stream to copy from.
     * @param out Output stream to copy to.
     * @throws IOException If there is an underlying I/O exception.
     */
    public static void copy(InputStream in, OutputStream out)
        throws IOException {

        byte[] buffer = new byte[BYTE_COPY_BUFFER_SIZE];
        int numBytes;
        while ((numBytes = in.read(buffer)) > 0)
            out.write(buffer,0,numBytes);
    }

    /**
     * Close an input stream.  Any IO exceptions will be caught and
     * logged as warnings.  Input stream may be <code>null</code>
     * without generating an exception.
     *
     * @param in Input stream to close.
     */
    public static void closeInputStream(InputStream in) {
        if (in == null) return;
        try {
            in.close();
        } catch (java.io.IOException e) {
            // ignore
        }
    }

    /**
     * Close an output stream.  Any IO exceptions will be caught and
     * logged as warnings.  Output stream may be <code>null</code>
     * without generating an exception.
     *
     * @param out Output stream to close.
     */
    public static void closeOutputStream(OutputStream out) {

        if (out == null) return;
        try {
            out.close();
        } catch (java.io.IOException e) {
            // ignore
        }
    }

    /**
     * Close a reader.  Any IO exceptions will be caught and
     * logged as warnings.  Reader may be <code>null</code>
     * without generating an exception.
     *
     * @param reader Reader to close.
     */
    public static void closeReader(java.io.Reader reader) {
        if (reader == null) return;
        try { reader.close(); }
        catch (java.io.IOException e) {
            // ignore
        }
    }

    /**
     * Close a writer.  Any IO exceptions will be caught and
     * logged as warnings.  Writer may be <code>null</code>
     * without generating an exception.
     *
     * @param writer Writer to close.
     */
    public static void closeWriter(java.io.Writer writer) {
        if (writer == null) return;
        try { writer.close(); }
        catch (java.io.IOException e) {
            // ignore
        }
    }

    /**
     * Size of buffer used for copying streams.
     */
    private static final int BYTE_COPY_BUFFER_SIZE = 1024*8;

    /**
     * Size of buffer used for copying readers to writers.
     */
    private static final int CHAR_COPY_BUFFER_SIZE
        = BYTE_COPY_BUFFER_SIZE / 2;

}
