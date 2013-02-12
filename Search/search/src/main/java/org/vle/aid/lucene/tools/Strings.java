
package org.vle.aid.lucene.tools;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.math.BigDecimal;

import java.text.DecimalFormat;

/**
 * Static utility methods for processing strings, characters and
 * string buffers.
 *
 * @see     java.lang.Character
 * @see     java.lang.String
 * @see     java.lang.StringBuffer
 */
public class Strings {

    /**
     * Forbid instance construction.
     */
    private Strings() { }

    /**
     * String representing the charset consisting of UTF 8 encoded
     * unicode characters.
     */
    public static String UTF8 = "UTF-8";

    /**
     * Return a copy of the specified string, trimming any underlying
     * array characters to render the resulting string of minimal
     * size..
     *
     * @param s String to copy.
     * @return Copy of specified string.
     */
    public static String save(String s) {
        return new String(s.toCharArray());
    }

    /**
     * Returns <code>true</code> if the specified string contains
     * an instance of the specified character.
     *
     * @param s String to check for character.
     * @param c Character.
     * @return <code>true</code> if specified character occurs in
     * specified string.
     */
    public static boolean containsChar(String s, char c) {
        return s.indexOf(c) >= 0;
    }

    /**
     * Returns <code>true</code> if the specified buffer contains
     * only whitespace characters.
     *
     * @param sb String buffer to test for whitespace.
     * @return <code>true</code> if the specified buffer contains only
     * whitespace characters.
     */
    public static boolean allWhitespace(StringBuffer sb) {
        return allWhitespace(sb.toString());
    }

    /**
     * Returns <code>true</code> if the specified string contains
     * only whitespace characters.
     *
     * @param s Stirng to test for whitespace.
     * @return <code>true</code> if the specified string contains only
     * whitespace characters.
     */
    public static boolean allWhitespace(String s) {
        return allWhitespace(s.toCharArray(),0,s.length());
    }

    /**
     * Returns <code>true</code> if the specified range of the
     * specified character array only whitespace characters, as defined for
     * characters by {@link #isWhitespace(char c)}.
     *
     * @param ch Character array to test for whitespace characters in range.
     * @param start Beginning of range to test.
     * @param length Number of characters to test.
     * @return <code>true</code> if the specified string contains only
     * whitespace characters.
     */
    public static boolean allWhitespace(char[] ch, int start, int length) {
        for (int i = start; i < start+length; ++i)
            if (!isWhitespace(ch[i])) return false;
        return true;
    }

    /**
     * Returns true if specified character is a whitespace character.
     * The definition in {@link
     * java.lang.Character#isWhitespace(char)} is extended to include
     * the unicode non-breakable space character (unicode 160).
     *
     * @param c Character to test.
     * @return <code>true</code> if specified character is a
     * whitespace.
     * @see java.lang.Character#isWhitespace(char)
     */
    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || c == NBSP_CHAR;
    }

    /**
     * Appends a whitespace-normalized form of the specified character
     * sequence into the specified string buffer.  Initial and final
     * whitespaces are not appended, and every other maximal sequence
     * of contiguous whitespace is replaced with a single whitespace
     * character.  For instance, <code>&quot; a\tb\n&quot;</code>
     * would append the following characters to <code>&quot;a
     * b&quot;</code>.
     *
     * <P>This command is useful for text inputs for web or GUI
     * applications.
     *
     * @param cs Character sequence whose normalization is appended to
     * the buffer.
     * @param sb String buffer to which the normalized character
     * sequence is appended.
     */
    public static void normalizeWhitespace(CharSequence cs, StringBuffer sb) {
        int i = 0;
        int length = cs.length();
        while (length > 0 && isWhitespace(cs.charAt(length-1)))
            --length;
        while (i < length && isWhitespace(cs.charAt(i)))
            ++i;
        boolean inWhiteSpace = false;
        for ( ; i < length; ++i) {
            char nextChar = cs.charAt(i);
            if (isWhitespace(nextChar)) {
                if (!inWhiteSpace) {
                    sb.append(' ');
                    inWhiteSpace = true;
                }
            } else {
                inWhiteSpace = false;
                sb.append(nextChar);
            }
        }
    }

    /**
     * Returns <code>true</code> if all of the characters
     * making up the specified string are digits.
     *
     * @param s String to test.
     * @return <code>true</code> if all of the characters making up
     * the specified string are digits.
     */
    public static boolean allDigits(String s) {
        return allDigits(s.toCharArray(),0,s.length());
    }

    /**
     * Returns <code>true</code> if all of the characters
     * in the specified range are digits.
     *
     * @param cs Underlying characters to test.
     * @param start Index of first character to test.
     * @param length Number of characters to test.
     * @return <code>true</code> if all of the characters making up
     * the specified string are digits.
     */
    public static boolean allDigits(char[] cs, int start, int length) {
        for (int i = 0; i < length; ++i)
            if (!Character.isDigit(cs[i+start])) return false;
        return true;
    }

    /**
     * Returns true if specified character is a punctuation character.
     * Punctuation includes comma, period, exclamation point, question
     * mark, colon and semicolon.  Note that quotes and apostrophes
     * are not considered punctuation by this method.
     *
     * @param c Character to test.
     * @return <code>true</code> if specified character is a
     * whitespace.
     * @see java.lang.Character
     */
    public static boolean isPunctuation(char c) {
        return c == ','
            || c == '.'
            || c == '!'
            || c == '?'
            || c == ':'
            || c == ';'
            ;
    }

    /**
     * Returns the result of concatenating the specified number of
     * copies of the specified string.  Note that there are no spaces
     * inserted between the specified strings in the output.
     *
     * @param s String to concatenate.
     * @param count Number of copies of string to concatenate.
     * @return Specified string concatenated with itself the specified
     * number of times.
     */
    public static String power(String s, int count) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; ++i)
            sb.append(s);
        return sb.toString();
    }

    /**
     * Concatenate the elements of the specified array as strings,
     * separating with the default separator {@link
     * #DEFAULT_SEPARATOR_STRING}.
     *
     * @param xs Array of objects whose string representations are
     * concatenated.
     * @return Concatenation of string representations of specified
     * objects separated by the default separator.
     */
    public static String concatenate(Object[] xs) {
        return concatenate(xs,DEFAULT_SEPARATOR_STRING);
    }

    /**
     * Concatenate the elements of the specified array as strings,
     * separating with the specified string spacer.
     *
     * @param xs Array of objects whose string representations are
     * concatenated.
     * @param spacer String to insert between the string
     * representations.
     * @return Concatenation of string representations of specified
     * objects separated by the specified spacer.
     */
    public static String concatenate(Object[] xs, String spacer) {
        return concatenate(xs,0,spacer);
    }

    /**
     * Concatenate the elements of the specified array as strings,
     * starting at the object at the specified index and continuing
     * through the rest of the string, separating with the specified
     * string spacer.
     *
     * @param xs Array of objects whose string representations are
     * concatenated.
     * @param start Index of first object to include.
     * @param spacer String to insert between the string
v     * representations.
     * @return Concatenation of string representations of specified
     * objects separated by the specified spacer.
     */
    public static String concatenate(Object[] xs, int start,
                                     String spacer) {
        return concatenate(xs,start,xs.length,spacer);
    }

    /**
     * Concatenate the elements of the specified array as strings,
     * starting at the object at the specified index and continuing
     * through one element before the specified end index, separating
     * with the default spacer {@link #DEFAULT_SEPARATOR_STRING}.
     *
     * @param xs Array of objects whose string representations are
     * concatenated.
     * @param start Index of first object to include.
     * @param end The index of the last element to include plus
     * <code>1</code>.
     * @return Concatenation of string representations of specified
     * objects separated by the specified spacer.
     */
    public static String concatenate(Object[] xs, int start, int end) {
        return concatenate(xs,start,end,DEFAULT_SEPARATOR_STRING);
    }

    /**
     * Concatenate the elements of the specified array as strings,
     * starting at the object at the specified index and continuing
     * through one element before the specified end index, separating
     * with the specified spacer.
     *
     * @param xs Array of objects whose string representations are
     * concatenated.
     * @param start Index of first object to include.
     * @param end The index of the last element to include plus
     * <code>1</code>.
     * @param spacer String to insert between the string
     * representations.
     * @return Concatenation of string representations of specified
     * objects separated by the specified spacer.
     */
    public static String concatenate(Object[] xs, int start, int end,
                                     String spacer) {
        StringBuffer sb = new StringBuffer();
        for (int i = start; i < end; ++i) {
            if (i > start) sb.append(spacer);
            sb.append(xs[i]);
        }
        sb.setLength(sb.length());
        return sb.toString();
    }

    /**
     * Appends an ``indentation'' to the specified string buffer,
     * consisting of a newline character and the specified number of
     * space characters to the specified string buffer.
     *
     * @param sb String buffer to indent.
     * @param length Number of spaces to append after a newline to the
     * specified string buffer.
     */
    public static void indent(StringBuffer sb, int length) {
        sb.append(NEWLINE_CHAR);
        padding(sb,length);
    }

    /**
     * Return a string consisting of the initial segment of the
     * specified string trimmed or padded with spaces to fit the
     * specified length.
     *
     * @param in String to fit to specified length.
     * @param length Length to fit.
     * @return String fitted to specified length.
     */
    public static String fit(String in, int length) {
        return in.length() > length
            ? in.substring(0,length)
            : in + padding(length-in.length());
    }

    /**
     * Returns a string consisting of the specified number of default
     * separator characters {@link #DEFAULT_SEPARATOR_CHAR}.
     *
     * @param length Number of separator characters in returned
     * string.
     * @return String of specified number of default separator
     * characters.
     */
    public static String padding(int length) {
        StringBuffer sb = new StringBuffer();
        padding(sb,length);
        return sb.toString();
    }

    /**
     * Append the specified number of default separator characters
     * {@link #DEFAULT_SEPARATOR_CHAR} to the specified string buffer.
     *
     * @param sb String buffer to which to append specified number of
     * default separator characters.
     * @param length Number of separator characters to append.
     */
    public static void padding(StringBuffer sb, int length) {
        for (int i = 0; i < length; ++i) sb.append(DEFAULT_SEPARATOR_CHAR);
    }

    /**
     * Return a string representation of a function applied
     * to its arguments.  Arguments will be converted to
     * strings and separated with commas.
     *
     * @param functionName Name of function.
     * @param args Arguments to function.
     * @return String representation of specified function applied to
     * specified arguments.
     */
    public static String functionArgs(String functionName, Object[] args) {
        return functionName + functionArgsList(args);
    }

    /**
     * Returns a string representation of the specified array as a
     * function's argument list.  Each object is converted to a
     * string, and the list of objects is separated by commas, and the
     * whole is surrounded by round parentheses.
     *
     * @param args Objects to represent arguments.
     * @return String representation of argument list.
     */
    public static String functionArgsList(Object[] args) {
        return "(" + concatenate(args,",") + ")";
    }

    /**
     * Returns <code>true</code> if all of the characters in the
     * specified array are lower case letters.
     *
     * @param chars Array of characters to test.
     * @return <code>true</code> if all of the characters in the
     * specified array are lower case letters.
     */
    public static boolean allLowerCase(char[] chars) {
        for (int i = 0; i < chars.length; ++i)
            if (!Character.isLowerCase(chars[i]))
                return false;
        return true;
    }
    /**
     * Returns <code>true</code> if the specified character sequence
     * contains only lowercase letters.  The test is performed by
     * {@link Character#isLowerCase(char)}.  This is the same test as
     * performed by {@link #allLowerCase(char[])}.
     *
     * @param token Token to check.
     * @return <code>true</code> if token is all lower-case.
     */
    public static boolean allLowerCase(CharSequence token) {
	int len = token.length();
	for (int i=0; i < len; i++) {
	    if (!Character.isLowerCase(token.charAt(i)))
		return false;
	}
	return true;
    }

    /**
     * Returns <code>true</code> if all of the characters in the
     * specified array are upper case letters.
     *
     * @param chars Array of characters to test.
     * @return <code>true</code> if all of the characters in the
     * specified array are upper case letters.
     */
    public static boolean allUpperCase(char[] chars) {
        for (int i = 0; i < chars.length; ++i)
            if (!Character.isUpperCase(chars[i]))
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all of the characters in the
     * specified array are letters.
     *
     * @param chars Array of characters to test.
     * @return <code>true</code> if all of the characters in the
     * specified array are letters.
     */
    public static boolean allLetters(char[] chars) {
        for (int i = 0; i < chars.length; ++i)
            if (!Character.isLetter(chars[i]))
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all of the characters in the
     * specified array are punctuation as specified by
     * {@link Strings#isPunctuation(char)}.
     *
     * @param chars Array of characters to test.
     * @return <code>true</code> if all of the characters in the
     * specified array are punctuation.
     */
    public static boolean allPunctuation(char[] chars) {
        for (int i = 0; i < chars.length; ++i)
            if (!Strings.isPunctuation(chars[i]))
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all of the characters in the
     * specified string are punctuation as specified by
     * {@link Strings#isPunctuation(char)}.
     *
     * @param token Token string to test.
     * @return <code>true</code> if all of the characters in the
     * specified string are punctuation.
     */
    public static boolean allPunctuation(String token) {
        for (int i = token.length(); --i >= 0; )
            if (!Strings.isPunctuation(token.charAt(i)))
                return false;
        return true;
    }

    /**
     * Returns an array of substrings of the specified string,
     * in order, with divisions before and after any instance
     * of the specified character.  The returned array will always
     * have at least one element.  Elements in the returned array
     * may be empty.  The following examples illustrate this behavior:
     *
     * <br/><br/>
     * <table border="1" cellpadding="5">
     * <tr><td><b>Call</b></td><td><b>Result</b></td></tr>
     * <tr>
     *   <td><code>split("",' ')</code></td>
     *   <td><code>{ "" }</code></td>
     * </tr>
     * <tr>
     *   <td><code>split("a",' ')</code></td>
     *   <td><code>{ "a" }</code></td>
     * </tr>
     * <tr>
     *   <td><code>split("a b",' ')</code></td>
     *   <td><code>{ "a", "b" }</code></td>
     * </tr>
     * <tr>
     *   <td><code>split("aaa bb cccc",' ')</code></td>
     *   <td><code>{ "aaa", "bb", "cccc" }</code></td>
     * </tr>
     * <tr>
     *   <td><code>split(" a",' ')</code></td>
     *   <td><code>{ "", "a" }</code></td>
     * </tr>
     * <tr>
     *   <td><code>split("a ",' ')</code></td>
     *   <td><code>{ "a", "" }</code></td>
     * </tr>
     * <tr>
     *   <td><code>split(" a ",' ')</code></td>
     *   <td><code>{ "", "a", "" }</code></td>
     * </tr>
     * </table>
     *
     * @param s String to split.
     * @param c Character on which to split the string.
     * @return The array of substrings resulting from splitting the
     * specified string on the specified character.
     */
    public static String[] split(String s, char c) {
        char[] cs = s.toCharArray();
        int tokCount = 1;
        for (int i = 0; i < cs.length; ++i)
            if (cs[i] == c) ++tokCount;
        String[] result = new String[tokCount];
        int tokIndex = 0;
        int start = 0;
        for (int end = 0; end < cs.length; ++end) {
            if (cs[end] == c) {
                result[tokIndex] = new String(cs,start,end-start);
                ++tokIndex;
                start = end+1;
            }
        }
        result[tokIndex] = new String(cs,start,cs.length-start);
        return result;
    }


    /**
     * Returns <code>true</code> if none of the characters in the
     * specified array are letters or digits.
     *
     * @param cs Array of characters to test.
     * @return <code>true</code> if none of the characters in the
     * specified array are letters or digits.
     */
    public static boolean allSymbols(char[] cs) {
        for (int i = 0; i < cs.length; ++i)
            if (Character.isLetter(cs[i]) || Character.isDigit(cs[i]))
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if at least one of the characters in
     * the specified array is a digit.
     *
     * @param chars Array of characters to test.
     * @return <code>true</code> if at least one of the characters in
     * the specified array is a digit.
     */
    public static boolean containsDigits(char[] chars) {
        for (int i = 0; i < chars.length; ++i)
            if (Character.isDigit(chars[i]))
                return true;
        return false;
    }

    /**
     * Returns <code>true</code> if at least one of the characters in
     * the specified array is a letter.
     *
     * @param chars Array of characters to test.
     * @return <code>true</code> if at least one of the characters in
     * the specified array is a letter.
     */
    public static boolean containsLetter(char[] chars) {
        for (int i = 0; i < chars.length; ++i)
            if (Character.isLetter(chars[i]))
                return true;
        return false;
    }


    /**
     * Returns <code>true</code> if the first character in the
     * specified array is an upper case letter and all subsequent
     * characters are lower case letters.
     *
     * @param chars Array of characters to test.
     * @return <code>true</code> if all of the characters in the
     * specified array are lower case letters.
     */
    public static boolean capitalized(char[] chars) {
        if (chars.length == 0) return false;
        if (!Character.isUpperCase(chars[0])) return false;
        for (int i = 1; i < chars.length; ++i)
            if (!Character.isLowerCase(chars[i]))
                return false;
        return true;
    }

    /**
     * Returns a title-cased version of the specified word,
     * which involves capitalizing the first character in
     * the word if it is a letter.
     *
     * @param word The word to convert to title case.
     * @return Title cased version of specified word.
     */
    public static String titleCase(String word) {
        if (word.length() < 1) return word;
        if (!Character.isLetter(word.charAt(0))) return word;
        return Character.toUpperCase(word.charAt(0))
            + word.substring(1);
    }

    /**
     * Returns a hexadecimal string-based representation of the
     * specified byte array.  Each byte is converted using {@link
     * #byteToHex(byte)} and the results are concatenated into
     * the final string representation.  Letter-based digits are
     * lowercase.
     *
     * @param bytes Array of bytes to convert.
     * @return The hexadecimal string-based representation of the
     * specified bytes.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i)
            sb.append(byteToHex(bytes[i]));
        return sb.toString();
    }

    /**
     * Converts the specified byte into a two-digit hexadecimal string
     * representation.  The byte is read as an unsigned value using
     * {@link Math#byteAsUnsigned(byte)}.  The result will always be two
     * characters, even if the unsigned byte value is less than 16.
     * Letter-based digits are lowercase.
     *
     * @param b Byte to convert.
     * @return Hexadecimal string representation of byte.
     */
    public static String byteToHex(byte b) {
        String result = Integer.toHexString(Math.byteAsUnsigned(b));
        switch (result.length()) {
        case 0: return "00";
        case 1: return "0" + result;
        case 2: return result;
        default: throw new IllegalArgumentException("byteToHex(" + b + ")=" + result);
        }
    }


    /**
     * Writes an array of strings to a data output stream.  This is
     * done by writing out the length and then writing out the strings
     * one at a time using the method {@link
     * DataOutput#writeUTF(String)}.
     *
     * @param dataOut Data output stream to which to write the string.
     * @param strings Array of strings to write.
     * @throws IOException If there is an I/O exception writing to the
     * stream.
     */
    public static void writeArrayTo(DataOutput dataOut,
                                    String[] strings)
        throws IOException {

        int len = strings.length;
        dataOut.writeInt(len);
        for (int i = 0; i < len; ++i)
            dataOut.writeUTF(strings[i]);
    }

    /**
     * Reads an array of strings from a data input stream.  See
     * {@link #writeArrayTo(DataOutput,String[])} for
     * information on the encoding used.
     *
     * @param dataIn Data input stream from  which array is read.
     * @throws IOException If there is an I/O exception reading from
     * the stream.
     */
    public static String[] readArrayFrom(DataInput dataIn)
        throws IOException {

        int len = dataIn.readInt();
        String[] result = new String[len];
        for (int i = 0; i < len; ++i)
            result[i] = dataIn.readUTF();
        return result;

    }


    /**
     * Formats and pads a specified number with the specified decimal
     * format pattern and pads it with leading spaces so that it is
     * the specified length.
     *
     * <P>For a full description Java's decimal formatting pattern
     * language, see {@link DecimalFormat}.
     *
     * <P>For English-style number formatting, the following patterns
     * are examples that allow arbitrarily long whole-number portions,
     * and exactly two decimal places.  Examples for formatting two numbers
     * are given in the last two columns.
     *
     * <blockquote>
     * <table cellpadding='5' border='1'>
     * <tr><td><i>Pattern</i></td><td>Leading Zero?</td><td>Thousands Commas?</td><td>2798.389</td><td>0.391</td></tr>
     * <tr><td><code>&quot;#,##0.00&quot;</code></td><td>yes</td><td>yes</td><td>2,798.39</td><td>0.39</td></tr>
     * <tr><td><code>&quot;#0.00&quot;</code></td><td>yes</td><td>no</td><td>2798.39</td><td>0.39</tr></tr>
     * <tr><td><code>&quot;#,###.00&quot;</code></td><td>no</td><td>yes</td><td>2,798.39</td><td>.39</td></tr>
     * <tr><td><code>&quot;#.00&quot;</code></td><td>no</td><td>no</td><td>2798.39</td><td>.39</td></tr>
     * </table>
     * </blockquote>
     *
     * If a variable-length decimal portion is required, the
     * <code>0</code>s may be replaced with <code>#</code>s.  Note
     * that the formatted numbers are rounded, not truncated; see
     * {@link BigDecimal#ROUND_HALF_EVEN} for a full description.
     *
     * @param x The number to format.
     * @param pattern The decimal pattern used to guide formatting.
     * @param length Length of result in characters.
     * @throws IllegalArgumentException If the result is longer than
     * the specified length, or if the specified pattern is invalid.
     */
    public static String decimalFormat(double x,
                                       String pattern,
                                       int length) {
        DecimalFormat formatter = new DecimalFormat(pattern);
        String result = formatter.format(x);
        if (result.length() > length) {
            String msg = "Formatted number exceeds specified length."
                + " Max length=" + length
                + " Pattern=" + pattern
                + " Raw number=" + x
                + " Formatted number=" + result;
            throw new IllegalArgumentException(msg);
        }
        if (result.length() == length) return result;
        StringBuffer sb = new StringBuffer(length);
        for (int i = (length-result.length()); --i >= 0; )
            sb.append(' ');
        sb.append(result);
        return sb.toString();
    }

    /**
     * Throws an exception if the start and end plus one indices are not
     * in the range for the specified array of characters.
     *
     * @param cs Array of characters.
     * @param start Index of first character.
     * @param end Index of one past last character.
     * @throws IndexOutOfBoundsException If the specified indices are out of
     * bounds of the specified character array.
     */
    public static void checkArgsStartEnd(char[] cs, int start, int end) {
        if (end < start) {
            String msg = "End must be >= start."
                + " Found start=" + start
                + " end=" + end;
            throw new IndexOutOfBoundsException(msg);
        }
        if (start >= 0 && end <= cs.length) return; // faster check
        if (start < 0 || start >= cs.length) {
            String msg = "Start must be greater than 0 and less than length of array."
                + " Found start=" + start
                + " Array length=" + cs.length;
            throw new IndexOutOfBoundsException(msg);

        }
        if (end < 0 || end > cs.length) {
            String msg = "End must be between 0 and  the length of the array."
                + " Found end=" + end
                + " Array length=" + cs.length;
            throw new IndexOutOfBoundsException(msg);
        }
    }

    /**
     * Returns an array of characters corresponding to the specified
     * character sequence.  The result is a copy, so modifying it will
     * not affect the argument sequence.
     *
     * @param cSeq Character sequence to convert.
     * @return Array of characters from the specified character sequence.
     */
    public static char[] toCharArray(CharSequence cSeq) {
        char[] cs = new char[cSeq.length()];
        for (int i = 0; i < cs.length; ++i)
            cs[i] = cSeq.charAt(i);
        return cs;
    }

    /**
     * Takes a time in milliseconds and returns an hours, minutes and
     * seconds representation.  Fractional ms times are rounded down.
     * Leading zeros and all-zero slots are removed.  A table of input
     * and output examples follows.
     *
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Input ms</i></td><td><i>Output String</i></td></tr>
     * <tr><td>0</td><td><code>:00</code></td></tr>
     * <tr><td>999</td><td><code>:00</code></td></tr>
     * <tr><td>1001</td><td><code>:01</code></td></tr>
     * <tr><td>32,000</td><td><code>:32</code></td></tr>
     * <tr><td>61,000</td><td><code>1:01</code></td></tr>
     * <tr><td>11,523,000</td><td><code>3:12:03</code></td></tr>
     * </table>
     *
     * @param ms Time in milliseconds.
     * @return String-based representation of time in hours, minutes
     * and second format.
     */
    public static String msToString(long ms) {
        long totalSecs = ms/1000;
        long hours = (totalSecs / 3600);
        long mins = (totalSecs / 60) % 60;
        long secs = totalSecs % 60;
        String minsString = (mins == 0)
            ? "00"
            : ((mins < 10)
               ? "0" + mins
               : "" + mins);
        String secsString = (secs == 0)
            ? "00"
            : ((secs < 10)
               ? "0" + secs
               : "" + secs);
        if (hours > 0)
            return hours + ":" + minsString + ":" + secsString;
        else if (mins > 0)
            return mins + ":" + secsString;
        else return ":" + secsString;
    }

    /**
     * Return <code>true</code> if the two character sequences have
     * the same length and the same characters.  Recall that equality
     * is not refined in the specification of {@link CharSequence}, but
     * rather inherited from {@link Object#equals(Object)}.
     *
     * The related method {@link #hashCode(CharSequence)} returns
     * hash codes consistent with this notion of equality.
     *
     * @param cs1 First character sequence.
     * @param cs2 Second character sequence.
     * @return <code>true</code> if the character sequences yield
     * the same strings.
     */
    public static boolean equalCharSequence(CharSequence cs1, 
					    CharSequence cs2) {
	if (cs1 == cs2) return true;
	int len = cs1.length();
	if (len != cs2.length()) return false;
	for (int i = 0; i < len; ++i)
	    if (cs1.charAt(i) != cs2.charAt(i)) return false;
	return true;
    }


    /**
     * Returns a hash code for a character sequence that is equivalent
     * to the hash code generated for a its string yield.  Recall that
     * the interface {@link CharSequence} does not refine the definition
     * of equality beyond that of {@link Object#equals(Object)}.
     *
     * <P>The return result is the same as would be produced by:
     * 
     * <pre>
     *    hashCode(cSeq) = cSeq.toString().hashCode()</pre>
     *
     * Recall that the {@link CharSequence} interface requires its
     * {@link CharSequence#toString()} to return a string
     * corresponding to its characters as returned by
     * <code>charAt(0),...,charAt(length()-1)</code>.  This value
     * can be defined directly by inspecting the hash code for strings:
     *
     * <pre>
     *      int h = 0;
     *      for (int i = 0; i < cSeq.length(); ++i)
     *          h = 31*h + cSeq.charAt(i);
     *      return h;</pre>
     *
     * @param cSeq The character sequence.
     * @return The hash code for the specified character sequence.
     */
    public static int hashCode(CharSequence cSeq) {
	if (cSeq instanceof String) return cSeq.hashCode();
	int h = 0;
	for (int i = 0; i < cSeq.length(); ++i)
	    h = 31*h + cSeq.charAt(i);
	return h;
    }

    /**
     * The non-breakable space character.
     */
    public static char NBSP_CHAR = (char)160;

    /**
     * The newline character.
     */
    public static char NEWLINE_CHAR = '\n';

    /**
     * The default separator character, a single space.
     */
    public static char DEFAULT_SEPARATOR_CHAR = ' ';

    /**
     * The default separator string.  The string is length
     * <code>1</code>, consisting of the default separator character
     * {@link #DEFAULT_SEPARATOR_CHAR}.
     */
    public static String DEFAULT_SEPARATOR_STRING
        = String.valueOf(DEFAULT_SEPARATOR_CHAR);

    /**
     * A string consisting of a single space.
     */
    public static final String SINGLE_SPACE_STRING = " ";

    /**
     * The empty string.
     */
    public static final String EMPTY_STRING = "";

    /**
     * The zero-length character array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];



}
