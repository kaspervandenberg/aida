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

import com.aliasi.corpus.TagHandler;
import com.aliasi.corpus.StringParser;


import java.util.ArrayList;
import java.util.List;

/**
 * The <code>GeniaPosParser</code> extracts the part-of-speech (POS)
 * tags from the GENIA text POS corpus and sends them to the specified
 * tag handler.
 *
 * <P>An example from the start of the GENIA POS corpus is:
 * 
 * <blockquote><table border='1' cellpadding='5'><tr><td>
 * <font size='-1'>
 * <pre>
 * UI/LS
 * -/:
 * 95369245/CD
 * ====================
 * TI/LS
 * -/:
 * IL-2/NN
 * gene/NN
 * expression/NN
 * and/CC
 * NF-kappa/NN
 * B/NN
 * activation/NN
 * through/IN
 * CD28/NN
 * requires/VBZ
 * reactive/JJ
 * oxygen/NN
 * production/NN
 * by/IN
 * 5-lipoxygenase/NN
 * ./.
 * ====================
 * AB/LS
 * -/:
 * Activation/NN
 * of/IN
 * the/DT
 * CD28/NN
 * surface/NN
 * receptor/NN
 * provides/VBZ
 * a/DT
 * major/JJ
 * costimulatory/JJ
 * signal/NN
 * for/IN
 * T/NN
 * cell/NN
 * activation/NN
 * resulting/VBG
 * in/IN
 * enhanced/VBN
 * production/NN
 * of/IN
 * interleukin-2/NN
 * (/(
 * IL-2/NN
 * )/)
 * and/CC
 * cell/NN
 * proliferation/NN
 * ./.
 * ====================
 * In/IN
 * primary/JJ
 * T/NN
 * lymphocytes/NNS
 * 
 *    <i>......snip.....</i>
 * </pre>
 * </font>
 * </td></tr></table></blockquote>
 *
 * The parser handles entries by &quot;sentence&quot;, where a
 * sentence is the set of token/tag pairs between the double-lines
 * composed of equal signs (<code>=</code>).  Some of these sentences
 * begin with a special token drawn from the following set:
 * 
 * <UL>
 * <LI> <code>UI</code>: Begin Citation 
 * <LI> <code>TI</code>: Citation Title
 * <LI> <code>AB</code>: Begin Abstract
 * </UL>
 * 
 * Note that all of these are tagged with the
 * &quot;part-of-speech&quot; <code>LS</code> and followed by a single
 * hyphen (<code>-</code>) tagged as part-of-speech colon
 * (<code>:</code>).  Further note that the begin citation includes a
 * PubMed identifier drawn from the MEDLINE corpus (see the {@link
 * com.aliasi.medline} package for more information on MEDLINE).
 * Further note that continuing sentences in the same abstract are not
 * tagged with any prefix.
 *
 * <P>The GENIA corpus itself and extensive information about it
 * is available from:
 * <UL>
 * <LI> <a href="http://www-tsujii.is.s.u-tokyo.ac.jp/~genia/topics/Corpus/">GENIA Corpus Home Page</a>
 * </UL>
 * 
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public class GeniaPosParser extends StringParser {

    /**
     * Construct a GENIA part-of-speech parser with no handler
     * specified.
     */
    public GeniaPosParser() {
    super();
    }

    /**
     * Construct a GENIA part-of-speech parser with the specified
     * tag handler.
     *
     * @param handler Tag handler for the parser.
     */
    public GeniaPosParser(TagHandler handler) {
    super(handler);
    }

    /**
     * Returns the tag handler for this parser.
     *
     * @return The tag handler for this parser.
     * @throws ClassCastException If a handler that does not implement
     * {@link TagHandler} was set using {@link #setHandler(Handler)}.
     */
    public TagHandler getTagHandler() {
    return (TagHandler) getHandler();
    }

    /**
     * Implementation of the parser for the GENIA corpus.
     *
     * @param cs Underlying characters.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     */
    public void parseString(char[] cs, int start, int end) {
    TagHandler handler = getTagHandler();

    ArrayList tokList = new ArrayList();
    ArrayList tagList = new ArrayList();

    String input = new String(cs,start,end-start);
    String[] lines = input.split("\n");
    for (int i = 0; i < lines.length; ++i) {
        if (lines[i].startsWith("====================")) {
        handle(handler,tokList,tagList);
        } else {
        int split = lines[i].lastIndexOf('/');
        if (split < 0) continue;
        String tok = lines[i].substring(0,split);
        String tag = lines[i].substring(split+1).trim();
        int splitIndex = tag.indexOf('|');
        if (splitIndex >= 0)
            tag = tag.substring(0,splitIndex);
        tokList.add(tok);
        tagList.add(tag);
        }
    }
    handle(handler,tokList,tagList);
    }

    private static void handle(TagHandler handler, List tokList, List tagList) {
    String[] toks = clearToArray(tokList);
    String[] tags = clearToArray(tagList);
    handler.handle(toks,null,tags);
    }

    private static String[] clearToArray(List xList) {
    String[] xs = new String[xList.size()];
    xList.toArray(xs);
    xList.clear();
    return xs;
    }

}
