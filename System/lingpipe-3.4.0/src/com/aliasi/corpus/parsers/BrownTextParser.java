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

import com.aliasi.corpus.TextHandler;
import com.aliasi.corpus.StringParser;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;

/**
 * The <code>BrownTextParser</code> parses the <a
 * href="http://nltk.sourceforge.net/">Natural Language Toolkit</a>
 * (NLTK) distribution of the <a
 * href="http://helmer.aksis.uib.no/icame/brown/bcm.html">Brown
 * Corpus</a>.  <a href=".  The results may be consumed by a text
 * handler.
 *
 * <P>NLTK distributes the corpus as a set of files in zip format.
 * This may be unzipped using the {@link java.util.zip} package and
 * each entry's input stream converted to an input source to
 * be provided tot his class.
 *
 * <P>Each file consists of lines of texts separated by zero or more
 * empty lines.  The lines of text are mostly sentences, but others
 * are document titles, closings of personal letters, etc.  The parser
 * handles each line independently, separating each line by a pair of
 * spaces as in the original Brown corpus.  Line-initial tabs indicate
 * paragraph breaks, and are retained as in the original corpus.
 * Other inter-sentential whitespace is removed.
 *
 * <P>The text in each line consists of an optional initial tab
 * followed by a sequence of token-tag pairs separated by single
 * spaces.  Each token-tag pair consists of a token followed by a
 * single forward-slash character followed by the tag.  Tokens are
 * retained and a single whitespace is inserted between each token,
 * except that the following tokens are never followed by spaces:
 *
 * <blockquote>
 * <table border=1 cellpadding=5 cellspacing=5>
 * <tr>
 * <td>``</td> <td>`</td> <td>(</td> <td>[</td> <td>{</td>
 * <td>$</td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * and the following tokens are never preceded by spaces:
 *
 * <blockquote>
 * <table border=1 cellpadding=5 cellspacing=5>
 * <tr>
 * <td>''</td> <td>'</td> <td>]</td> <td>}</td> <td>,</td>
 * <td>.</td> <td>!</td> <td>?</td> <td>:</td> <td>;</td>
 * <td>%</td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.0
 */
public class BrownTextParser extends StringParser {

    /**
     * Construct a Brown text parser with a null text handler.
     */
    public BrownTextParser() { 
        /* do nothing */
    }

    /**
     * Construct a Brown text parser with the specified text handler.
     *
     * @param handler Handler to use for text found by this parser.
     */
    public BrownTextParser(TextHandler handler) {
        super(handler);
    }

    /**
     * Parse the specified input stream representing the NLTK
     * distribution of the Brown corpus, passing characters to the
     * specified handler.
     *
     * @param cs Underlying characters.
     * @param start Index of first character.
     * @param end Index of one past the last character.
     * @throws IOException If there is an exception reading from the
     * specified input stream.
     */
    public void parseString(char[] cs, int start, int end) throws IOException {
        TextHandler handler = (TextHandler) getHandler();

        CharArrayReader reader = new CharArrayReader(cs,start,end-start);
        BufferedReader bufReader = new BufferedReader(reader);
        String line;
        StringBuffer sb = new StringBuffer();
        boolean continuationLine = false;
        while ((line = bufReader.readLine()) != null) {
            boolean startParagraph = line.startsWith("\t");
            String trimmedText = line.trim();
            if (trimmedText.length() == 0) continue;
            if (continuationLine) sb.append(' ');
            else if (startParagraph) sb.append('\t');
            continuationLine = true;
            String[] tokenTags = trimmedText.split(" ");
            String[] toks = new String[tokenTags.length];
            for (int i = 0; i < tokenTags.length; ++i) {
                String tokTag = tokenTags[i];
                int k = tokTag.lastIndexOf('/');
                toks[i] = (k < 0) ? tokTag : tokTag.substring(0,k);
            }
            for (int i = 0; i < toks.length; ++i) {
                sb.append(toks[i]);
                if (i+1 == toks.length) break; 
                if (toks[i].equals("``")) continue; 
                if (toks[i].equals("`")) continue;
                if (toks[i].equals("(")) continue;
                if (toks[i].equals("[")) continue;
                if (toks[i].equals("{")) continue;
                if (toks[i].equals("$")) continue;
                if (toks[i+1].equals("''")) continue;
                if (toks[i+1].equals("'")) continue;
                if (toks[i+1].equals("]")) continue;
                if (toks[i+1].equals("}")) continue;
                if (toks[i+1].equals(".")) continue;
                if (toks[i+1].equals("?")) continue;
                if (toks[i+1].equals("!")) continue;
                if (toks[i+1].equals(":")) continue;
                if (toks[i+1].equals(";")) continue;
                if (toks[i+1].equals(",")) continue;
                if (toks[i+1].equals("%")) continue;
                sb.append(' '); // not last or context w/o space
            }
        }
        char[] csFound = sb.toString().toCharArray();
        handler.handle(csFound,0,csFound.length);
    }
    



}
