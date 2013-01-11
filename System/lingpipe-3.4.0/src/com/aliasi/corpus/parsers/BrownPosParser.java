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
import com.aliasi.corpus.TagHandler;

import com.aliasi.util.Strings;

/**
 * The <code>BrownPosParser</code> class provides a parser for the
 * NLTK distribution of the Brown Corpus.  The data is formatted in
 * pure ASCII, with sentences delimited, tokens delimited and tags
 * separated from tokens by a forward slash.  An example from the
 * first file (<code>brown/cp01</code>) in the NLTK distribution is:
 * 
 * <blockquote>
 * <font size='-1'>
 * <table border='1' cellpadding='5'><tr><td>
 * <PRE>
 *      The/at Fulton/np-tl County/nn-tl Grand/jj-tl Jury/nn-tl said/vbd Friday/nr an/at investigation/nn of/in Atlanta's/np$ recent/jj primary/nn election/nn produced/vbd ``/`` no/at evidence/nn ''/'' that/cs any/dti irregularities/nns took/vbd place/nn ./.  
 *
 *      The/at jury/nn further/rbr said/vbd in/in term-end/nn presentments/nns that/cs the/at City/nn-tl Executive/jj-tl Committee/nn-tl ,/, which/wdt had/hvd over-all/jj charge/nn of/in the/at election/nn ,/, ``/`` deserves/vbz the/at praise/nn and/cc thanks/nns of/in the/at City/nn-tl of/in-tl Atlanta/np-tl ''/'' for/in the/at manner/nn in/in which/wdt the/at election/nn was/bedz conducted/vbn ./.  
 * 
 * ...
 * </PRE>
 * </td></tr></table>
 * </font>
 * </blockquote>
 *
 * Note that each sentence is on its own line, with a tab indentation.
 * The sentences themselves are separated by multiple blank lines, which
 * are simply ignored by this parser.
 *
 * <P>Each tag consists of a base tag and optional modifiers.  This
 * parser removes all of the modifiers.  The modifiers include
 * multiple tags separated by plus-signs (eg. <code>EX+BEZ</code>),
 * multiple tags concatenated in the case of negation
 * (eg. <code>BEZ*</code>), the prefix modifier <code>FW-</code> for
 * foreign words (e.g. <code>FW-JJ</code>), the suffix modifier
 * <code>-NC</code> for citations (e.g. <code>NN-NC</code>), the
 * suffix <code>-HL</code> for words in headlines (e.g. <code>NN-HL</code),
 * and the suffix <code>-TL</code> in titles (e.g. <code>NNS-TL</code>).
 *
 * <P>The full set of base tags is given in the following table:
 *
 * <BLOCKQUOTE>
 * <font size='-1'>
 * <TABLE border='1' cellpadding='3'>
 * <TR><TD><i>Tag</i></TD><TD><i>Description</i></TD><TD><i>Examples</i></TD></TR>
 * <TR><TD>'</TD><TD>apostrophe</TD><TD></TD>
 * <TR><TD>``</TD><TD>double open quote</TD><TD></TD>
 * <TR><TD>''</TD><TD>double close quote</TD><TD></TD>
 * <TR><TD>.</TD><TD>sentence closer</TD><TD>. ; ? !</TD>
 * <TR><TD>(</TD><TD>left paren</td><td>&nbsp;</TD>
 * <TR><TD>)</TD><TD>right paren</TD><td>&nbsp;</TD>
 * <TR><TD>*</TD><TD><I>not, n't</I></TD><TD>&nbsp;</TD>
 * <TR><TD>--</TD><TD>dash</TD><TD>&nbsp;</TD>
 * <TR><TD>,</TD><TD>comma</TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>:</TD><TD>colon</TD><TD>&nbsp;</TD>
 * <TR><TD>ABL</TD><TD>pre-qualifier</TD><TD><I>quite, rather</I></TD>
 * <TR><TD>ABN</TD><TD>pre-quantifier</TD><TD><I>half, all</I></TD>
 * <TR><TD>ABX</TD><TD>pre-quantifier</TD><TD><I>both</I></TD>
 * <TR><TD>AP</TD><TD>post-determiner</TD><TD><I>many, several, next</I></TD>
 * <TR><TD>AP$</TD><TD>possessive post-determiner</TD><TD><I>many, several, next</I></TD>
 * 
 * <TR><TD>AT</TD><TD>article</TD><TD><I>a, the, no</I></TD>
 * <TR><TD>BE</TD><TD><I>be</I></TD><TD>&nbsp;</TD>
 * <TR><TD>BED</TD><TD><I>were</I></TD><TD>&nbsp;</TD>
 * <TR><TD>BEDZ</TD><TD><I>was</I></TD><TD>&nbsp;</TD>
 * <TR><TD>BEG</TD><TD><I>being</I></TD><TD>&nbsp;</TD>
 * <TR><TD>BEM</TD><TD><I>am</I></TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>BEN</TD><TD><I>been</I></TD><TD>&nbsp;</TD>
 * <TR><TD>BER</TD><TD><I>are, art</I></TD><TD>&nbsp;</TD>
 * <TR><TD>BEZ</TD><TD><I>is</I></TD><TD>&nbsp;</TD>
 * <TR><TD>CC</TD><TD>coordinating conjunction</TD><TD><I>and, or</I></TD>
 * <TR><TD>CD</TD><TD>cardinal numeral</TD><TD><I>one, two, 2, etc.</I></TD>
 * <TR><TD>CD$</TD><TD>possessive cardinal numeral</TD><TD><I>one, two, 2, etc.</I></TD>
 * 
 * <TR><TD>CS</TD><TD>subordinating conjunction</TD><TD><I>if, although</I></TD>
 * <TR><TD>DO</TD><TD><I>do</I></TD><TD>&nbsp;</TD>
 * <TR><TD>DOD</TD><TD><I>did</I></TD><TD>&nbsp;</TD>
 * <TR><TD>DOZ</TD><TD><I>does</I></TD><TD>&nbsp;</TD>
 * <TR><TD>DT</TD><TD>singular determiner</TD><TD><I>this, that</I></TD>
 * <TR><TD>DT$</TD><TD>possessive singular determiner</TD><TD><I>this, that</I></TD>
 * 
 * <TR><TD>DTI</TD><TD>singular or plural determiner/quantifier</TD><TD><I>some, any</I></TD>
 * <TR><TD>DTS</TD><TD>plural determiner</TD><TD><I>these, those</I></TD>
 * <TR><TD>DTX</TD><TD>determiner/double conjunction</TD><TD><I>either</I></TD>
 * <TR><TD>EX</TD><td>existential <I>there</I></TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>HV</TD><TD><I>have</I></TD><TD>&nbsp;</TD>
 * <TR><TD>HVD</TD><TD><I>had</I> (past tense)</TD><TD>&nbsp;</TD>
 * <TR><TD>HVG</TD><TD><I>having</I></TD><TD>&nbsp;</TD>
 * <TR><TD>HVN</TD><TD><I>had</I> (past participle)</TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>HVZ</TD><TD><I>has</I></TD><TD>&nbsp;</TD>
 * <TR><TD>IN</TD><TD>preposition</TD><TD>&nbsp;</TD>
 * <TR><TD>JJ</TD><TD>adjective</TD><TD>&nbsp;</TD>
 * <TR><TD>JJ$</TD><TD>possessive adjective</TD><TD>&nbsp;</TD>
 * <TR><TD>JJR</TD><TD>comparative adjective</TD><TD>&nbsp;</TD>
 * <TR><TD>JJS</TD><TD>semantically superlative adjective</TD><TD><I> chief, top</I></TD>
 * 
 * <TR><TD>JJT</TD><TD>morphologically superlative adjective</TD><TD><I>biggest</I></TD>
 * <TR><TD>MD</TD><TD>modal auxiliary</TD><TD><I>can, should, will</I></TD>
 * <TR><TD>NIL</TD><TD>no category assigned</TD><TD>&nbsp;</TD>
 * <TR><TD>NN</TD><TD>singular or mass noun</TD><TD>&nbsp;</TD>
 * <TR><TD>NN$</TD><TD>possessive singular noun</TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>NNS</TD><TD>plural noun</TD><TD>&nbsp;</TD>
 * <TR><TD>NNS$</TD><TD>possessive plural noun</TD><TD>&nbsp;</TD>
 * <TR><TD>NP</TD><TD>proper noun or part of name phrase</TD><TD>&nbsp;</TD>
 * <TR><TD>NP$</TD><TD>possessive proper noun</TD><TD>&nbsp;</TD>
 * <TR><TD>NPS</TD><TD>plural proper noun</td><td>&nbsp;</TD>
 * <TR><TD>NPS$</TD><TD>possessive plural proper noun</TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>NR</TD><TD>adverbial noun</TD><TD><I>home, today, west</I></TD>
 * <TR><TD>NR$</TD><TD>possessive adverbial noun</TD><TD></TD>
 * <TR><TD>NRS</TD><TD>plural adverbial noun</TD><TD>&nbsp;</TD>
 * <TR><TD>OD</TD><TD>ordinal numeral</TD><TD><I>first, 2nd</I></TD>
 * <TR><TD>PN</TD><TD>nominal pronoun</TD><TD><I>everybody, nothing</I></TD>
 * <TR><TD>PN$</TD><TD>possessive nominal pronoun</TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>PP$</TD><TD>possessive personal pronoun</TD><TD><I>my, our</I></TD>
 * <TR><TD>PP$$</TD><TD>second (nominal) possessive pronoun</TD><TD><I>mine, ours</I></TD>
 * <TR><TD>PPL</TD><TD>singular reflexive/intensive personal pronoun</TD><TD><I>myself</I></TD>
 * <TR><TD>PPLS</TD><TD>plural reflexive/intensive personal pronoun</TD><TD><I>ourselves</I></TD>
 * <TR><TD>PPO</TD><TD>objective personal pronoun</TD><TD><I>me, him, it, them</I></TD>
 * 
 * <TR><TD>PPS</TD><TD>3rd. singular nominative pronoun</TD><TD><I>he, she, it, one</I></TD>
 * <TR><TD>PPSS</TD><TD>other nominative personal pronoun</TD><TD><I>I, we, they, you</I></TD>
 * <TR><TD>QL</TD><TD>qualifier</TD><TD><I>very, fairly</I></TD>
 * <TR><TD>QLP</TD><TD>post-qualifier</TD><TD><I>enough, indeed</I></TD>
 * <TR><TD>RB</TD><TD>adverb</TD><TD>&nbsp;</TD>
 * <TR><TD>RB$</TD><TD>possessive adverb</TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>RBR</TD><TD>comparative adverb</TD><TD>&nbsp;</TD>
 * <TR><TD>RBT</TD><TD>superlative adverb</TD><TD>&nbsp;</TD>
 * <TR><TD>RN</TD><TD>nominal adverb</TD><TD><I>here then, indoors</I></TD>
 * <TR><TD>RP</TD><TD>adverb/particle</TD><TD><I>about, off, up</I></TD>

 * <TR><TD>TO</TD><TD>infinitive marker <I>to</I></TD><TD>&nbsp;</TD>
 * <TR><TD>UH</TD><TD>interjection, exclamation</TD><TD>&nbsp;</TD>
 * <TR><TD>VB</TD><TD>verb, base form</TD><TD>&nbsp;</TD>
 * <TR><TD>VBD</TD><TD>verb, past tense</TD><TD>&nbsp;</TD>
 * <TR><TD>VBG</TD><TD>verb, present participle/gerund</TD><TD>&nbsp;</TD>
 * 
 * <TR><TD>VBN</TD><TD>verb, past participle</TD><TD>&nbsp;</TD>
 * <TR><TD>VBZ</TD><TD>verb, 3rd. singular present</TD><TD>&nbsp;</TD>
 * <TR><TD>WDT</TD><TD><I>wh- </I>determiner</TD><TD><I>what, which</I></TD>
 * <TR><TD>WP$</TD><TD>possessive <I>wh- </I>pronoun</TD><TD><I>whose</I></TD>
 * 
 * <TR><TD>WPO</TD><TD>objective <I>wh- </I>pronoun</TD><TD><I>whom, which, that</I></TD>
 * <TR><TD>WPS</TD><TD>nominative <I>wh- </I>pronoun</TD><TD><I>who, which, that</I></TD>
 * <TR><TD>WQL</TD><TD><I>wh- </I>qualifier</TD><TD><I>how</I></TD>
 * <TR><TD>WRB</TD><TD><I>wh- </I>adverb</TD><TD><I>how, where, when</I></TD>
 * </TABLE>
 * </font>
 * </BLOCKQUOTE>
 *
 *
 * <P>For information on NLTK and the Brown corpus, see:
 * <UL>
 * <LI> <a href="http://nltk.sourceforge.net/">Natural Language Tookit (NLTK) Home</a>
 * <LI> <a href="http://sourceforge.net/project/showfiles.php?group_id=30982&package_id=92151">NLTK Data Download</a>
 * <LI> <a href="http://khnt.hit.uib.no/icame/manuals/brown/INDEX.HTM">Brown Corpus Manual</a>. W. N. Francis and H. Kucera.  1964. Revised 1971.  Revised and amplified 1979.  Brown University.
 * </UL>
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public class BrownPosParser extends StringParser {

    /**
     * Construct a Brown corpus part-of-speech tag parser with
     * no handler specified.  
     */
    public BrownPosParser() {
        super();
    }

    /**
     * Construct a Brown corpus part-of-speech tag parser with
     * the specified tag handler.
     *
     * @param handler Tag handler.
     */
    public BrownPosParser(TagHandler handler) {
        super(handler);
    }

    /**
     * Returns the tag handler for this parser.
     *
     * @return The tag handler for this parser.
     */
    public TagHandler tagHandler() {
        return (TagHandler) getHandler();
    }

    /**
     * Parse the specified input source and send extracted taggings to
     * the current handler.  This string should correspond to the
     * contents of an input file.
     *
     * @param cs Character array underlying string.
     * @param start First character of string.
     * @param end Index of one past the last character in the string.
     */
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        String[] sentences = in.split("\n");
        for (int i = 0; i < sentences.length; ++i)
            if (!Strings.allWhitespace(sentences[i]))
                processSentence(sentences[i]);
    }

    /**
     * Return a normalized form of the tag stripping off all modifiers
     * and conjunctions.
     *
     * @param rawTag Tag to normalize.
     * @return Normalized form of tag.
     */
    public String normalizeTag(String rawTag) {
        String tag = rawTag;
        String startTag = tag;
        // remove plus, default to first
        int splitIndex = tag.indexOf('+');
        if (splitIndex >= 0)
            tag = tag.substring(0,splitIndex);

        int lastHyphen = tag.lastIndexOf('-');
        if (lastHyphen >= 0) {
            String first = tag.substring(0,lastHyphen);
            String suffix = tag.substring(lastHyphen+1);
            if (suffix.equalsIgnoreCase("HL") 
                || suffix.equalsIgnoreCase("TL")
                || suffix.equalsIgnoreCase("NC")) {
                tag = first;
            }
        }

        int firstHyphen = tag.indexOf('-');
        if (firstHyphen > 0) {
            String prefix = tag.substring(0,firstHyphen);
            String rest = tag.substring(firstHyphen+1);
            if (prefix.equalsIgnoreCase("FW")
                || prefix.equalsIgnoreCase("NC")
                || prefix.equalsIgnoreCase("NP"))
                tag = rest;
        }

        // neg last, and only if not whole thing
        int negIndex = tag.indexOf('*');
        if (negIndex > 0) {
            if (negIndex == tag.length()-1)
                tag = tag.substring(0,negIndex);
            else
                tag = tag.substring(0,negIndex)
                    + tag.substring(negIndex+1);
        }
        // multiple runs to normalize
        return tag.equals(startTag) ? tag : normalizeTag(tag);
    }

    private void processSentence(String sentence) {
        String[] tagTokenPairs = sentence.split(" ");
        String[] tokens = new String[tagTokenPairs.length];
        String[] tags = new String[tagTokenPairs.length];
    
        for (int i = 0; i < tagTokenPairs.length; ++i) {
            String pair = tagTokenPairs[i];
            int j = pair.lastIndexOf('/');
            tokens[i] = pair.substring(0,j);
            tags[i] = normalizeTag(pair.substring(j+1));
        }
        tagHandler().handle(tokens,null,tags);
    }
    
}
