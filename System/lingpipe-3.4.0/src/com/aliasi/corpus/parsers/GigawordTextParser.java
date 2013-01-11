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

package com.aliasi.corpus.parsers;

import com.aliasi.corpus.StringParser;
import com.aliasi.corpus.TextHandler;

import java.io.CharArrayReader;
import java.io.BufferedReader;
import java.io.IOException;


/**
 * A text parser for the Linguistic Data Consortium's English Gigaword
 * Corpus.  This parser extracts the text from the articles tagged as
 * stories, passing it to the handler one story at a time.
 *
 * <P>There is an extensive read-me for the corpus available online:
 *
 * <blockquote>
 * <a href="http://www.ldc.upenn.edu/Catalog/docs/LDC2003T05/0readme.txt"
 * >README File for the Gigaword English Text Corpus</a>
 * </blockquote>
 *
 * <P>The gigaword corpus contains over 1.75 billion words of English.
 * Roughly 1.5 billion words of that appear in documents of type
 * <code>story</code>.  The distribution directory is organized by
 * news source: Agence France, AP, New York Times, and Xinhua.  In
 * addition to stories, there are three other types of document:
 * multi-part blurbs, advisories to editors, and other.  The
 * &quot;other&quot; category includes lists like sports scores, stock
 * prices, etc.  Unfortunately, this division is only approximate,
 * and stock listings and scores appear in some stories, too.  This
 * is expected given the corpus README, which states:
 *
 * <blockquote>
 * ... the most frequent classification error will tend to be
 * the use of `` type=&quot;story&quot; '' on DOCs that are actually some other
 * type.
 * </blockquote>
 *
 * <P>The corpus is distributed in files organized by source.  Each
 * file is roughly 12MB gzipped and 36MB unzipped.  The format is
 * as a sequence of SGML documents.  Here's an example document from
 * the distribution's read-me:
 *
 * <blockquote><table border='1' cellpadding='5'><tr><td><pre>
 * &lt;DOC id=&quot;...&quot; type=&quot;...&quot;&gt;
 * &lt;HEADLINE&gt;
 * The Headline Element is Optional -- not all DOCs have one
 * &lt;/HEADLINE&gt;
 * &lt;DATELINE&gt;
 * The Dateline Element is Optional -- not all DOCs have one
 * &lt;/DATELINE&gt;
 * &lt;TEXT&gt;
 * &lt;P&gt;
 * Paragraph tags are only used if the 'type' attribute of the DOC happens
 * to be &quot;story&quot;
 * &lt;/P&gt;
 * &lt;P&gt;
 * Note that all data files use the UNIX-standard &quot;\n&quot; form of line
 * termination, and text lines are generally wrapped to a width of 80
 * characters or less.
 * &lt;/P&gt;
 * &lt;/TEXT&gt;
 * &lt;/DOC&gt;</td></tr></pre></table></blockquote>
 *
 * <P>This parser extracts the text content of the <code>TEXT</code>
 * elements with documents of type <code>story</code>.  The only
 * characters appearing in the corpus are printable ASCII characters
 * including whitespace. Newlines are replaced with spaces, a tab
 * character is inserted to statrt each paragraph, and the two
 * variations of the single entity used, <code>&AMP;AMP;</code> and
 * <code>&AMP;amp;</code>, are replaced with the ampersand
 * (<code>&AMP;</code>) character.  No other transformations on the
 * text are performed by this parser.  The creators of the corpus note
 * somewhat confusingly:
 *
 * <blockquote>
 * All other [besides ampersand escape] specialized control characters
 * have been filtered out, and unusual punctuation (such as the
 * underscore character, used in NYT and APW to represent an &quot;em-dash&quot;
 * character) has been left as-is, or converted to simple equivalents
 * (e.g. hyphens).
 * </blockquote>
 *
 * <P>Links to the sources for the corpus are:
 * <UL>
 * <LI>
 * <a href="http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2003T05">English Gigaword Corpus</a>
 * </UL>
 *
 * @author  Bob Carpenter
 * @version 3.1.2
 * @since   LingPipe2.0
 */

public class GigawordTextParser extends StringParser<TextHandler> {

    /**
     * Construct a Gigaword text parser with a <code>null</code>
     * handler.
     */
    public GigawordTextParser() {
        super();
    }

    /**
     * Construct a Gigaword text parser with the specified text
     * handler as the current handler.
     *
     * @param handler Text handler for extracted text.
     */
    public GigawordTextParser(TextHandler handler) {
        super(handler);
    }

    /**
     * Parse the specified character slice as a Gigaword document, passing
     * the text content of stories to the contained handler.  See the
     * class documentation above for more information.
     *
     * @param cs Underlying characters.
     * @param start Index of first character.
     * @param end Index of one past the last character.
     * @throws IOException If there is an exception reading from the
     * specified input stream.
     */
    public void parseString(char[] cs, int start, int end) throws IOException {

        TextHandler handler = (TextHandler) getHandler();

        CharArrayReader charReader = new CharArrayReader(cs,start,end-start);
        BufferedReader bufReader = new BufferedReader(charReader);
        String line;
        while ((line = bufReader.readLine()) != null) {
            // wait for doc
            if (!line.startsWith("<DOC ")) continue;
            // ignore non-story
            if (line.indexOf("type=\"story") < 0)
                while ((line = bufReader.readLine()) != null)
                    if (line.startsWith("</DOC>"))
                        break;
            while (!line.startsWith("<TEXT>"))
                if ((line = bufReader.readLine()) == null)
                    return;
            if (line.startsWith("<TEXT>")) {
                StringBuffer sb = new StringBuffer();
                boolean continuing = false;
                while ((line = bufReader.readLine()) != null) {
                    if (line.startsWith("</TEXT>")) {
                        char[] csFound = sb.toString().trim().toCharArray();
                        handler.handle(csFound,0,csFound.length);
                        break;
                    }
                    if (line.startsWith("<P>")) {
                        continuing = false;
                        sb.append("\t ");
                    } else if (!line.startsWith("</P>")) {
                        if (continuing) sb.append(' ');
                        else continuing = true;
                        sb.append(line.indexOf('&') >= 0
                                  ? removeEscapes(line)
                                  : line);
                    }
                }
            }
        }
    }

    private static String removeEscapes(String line) {
        return line.replaceAll("&(amp|AMP);","&");
    }

}
