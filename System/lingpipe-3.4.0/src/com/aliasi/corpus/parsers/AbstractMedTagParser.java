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

import java.util.Arrays;

/**
 * The <code>AbstractMedTagParser</code> class provides an adapter for
 * NCBI's MedTag corpora, including GeneTag and MedPost.  The MedTag
 * format is sentence based, consisting of a number of pairs of lines
 * of the following form:
 *
 * <blockquote><table border="1" cellpadding="5"><tr><td><pre>
 * P00073344A0367
 * tok_tag tok_tag ... tok_tag   
 * P00083846T0000  
 * tok_tag tok_tag ... tok_tag   
 * ...
 * </pre></td></tr></table></blockquote>
 *
 * The initial part of the first line, <code>P00073344</code>,
 * provides the PubMed identifier from which the text was abstracted.
 * The second part of the first line, <code>A0367</code> indicates
 * that the sentence was from the abstract, beginning at character
 * offset 367.  The text may be extracted from titles or abstracts;
 * the third line indicates a line beginning with the first character
 * (index 0000) of the title (T) of the citation with PubMed ID 83846.

 * <p> The second (and fourth) line consist of a sequence of tokens
 * and tags, separated by an underscore.  The tags are part-of-speech
 * tags in the MedPost corpus and chunk entity tags in the GeneTag
 * corpus.  Note that with this format, whitespace information is
 * lost.
 *
 * <p>Subclasses
 * must override the {@link
 * #parseTokensTags(String[],String[],String[])} method to actually do
 * the parsing of a sentence once its tags are extracted.
 *
 * <p>For more information on the MedTag project, see:
 *
 * <ul> <li>LH Smith, L Tanabe, T Rindflesch and WJ Wilbur. 2005.
 * 2005. <a href="http://acl.ldc.upenn.edu/W/W05/W05-1305.pdf">MedTag:
 * a collection of biomedical annotations</a>.  <i>Proceedings of
 * ACL-ISMB Workshop on Linking Biological LIterature, Ontologies and
 * Databases</i>.  Detroit.  
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public abstract class AbstractMedTagParser extends StringParser {
    
    /**
     * Construct an abstract MedTag parser with no handler specified.
     */
    public AbstractMedTagParser() {
    super();
    }

    /**
     * Construct an abstract MedTag parser with the specified tag
     * handler.
     *
     * @param handler Tag handler.
     */
    public AbstractMedTagParser(TagHandler handler) {
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
    for (int i = 0; i < sentences.length; ++i) {
        if (Strings.allWhitespace(sentences[i])) continue;
        if (sentences[i].indexOf('_') < 0) continue;
        processSentence(sentences[i]);
    }
    }

    /**
     * This method handles the raw tokens and tags pulled from a
     * MedTag corpus.  This method must be implemented by subclasses,
     * and it must call the contained handler on the result.
     *
     * @param tokens Raw tokens to handle.
     * @param whitespaces Raw whitespaces to handle.
     * @param tags Raw tags to handle.
     */
    protected abstract void parseTokensTags(String[] tokens, 
                        String[] whitespaces,
                        String[] tags);

    private void processSentence(String sentence) {
    String[] tagTokenPairs = sentence.split(" ");
    String[] tokens = new String[tagTokenPairs.length];
    String[] tags = new String[tagTokenPairs.length];
    
    for (int i = 0; i < tagTokenPairs.length; ++i) {
        String pair = tagTokenPairs[i];
        int j = pair.lastIndexOf('_');
        tokens[i] = pair.substring(0,j).trim();
        tags[i] = pair.substring(j+1).trim();
    }
    String[] whitespaces = new String[tokens.length+1];
    if (whitespaces.length > 3) 
        Arrays.fill(whitespaces,1,whitespaces.length-2," ");
    whitespaces[0] = "";
    whitespaces[whitespaces.length-1] = "";
    parseTokensTags(tokens,whitespaces,tags);
    }

    

    
}



