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

package com.aliasi.sentences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A <code>HeuristicSentenceModel</code> determines sentence
 * boundaries based on sets of tokens, a pair of flags, and an
 * overridable method describing boundary conditions.  

 * <P>There are three sets of tokens specified for a heuristic model:
 *
 * <UL> 
 *
 * <LI> <b>Possible Stops</b>: These are tokens that are allowed
 * to be the final token in a sentence.  This set typically includes
 * sentence-final punctuation tokens such as periods (<code>.</code>)
 * and double quotes (<code>&quot;</code>).
 *
 * <LI> <b>Impossible Penultimates</b>: These are tokens that may
 * <i>not</i> be the penultimate (second-to-last) token in a sentence.
 * This set is typically made up of abbreviations or acronyms such as
 * <code>&quot;Mr&quot;</code>.
 *
 * <LI> <b>Impossible Starts</b>: These are tokens that may <i>not</i>
 * be the first token in a sentence.  This set typically includes
 * punctuation characters that should be attached to the previous
 * sentence such as end quotes (<code>''</code>).  Note that there is
 * a method, described below, which may enforce additional conditions
 * on start tokens.
 *
 * </UL>
 *
 * Note that all of these sets perform <i>case insensitive</i> tests.
 *
 * <P>There are also two flags in the constructor that determine
 * aspects of sentence boundary detection:
 *
 * <UL>

 * <LI> <b>Balance Parentheses</b>: If parentheses are being balanced,
 * then as long as there are open parentheses that have not been
 * closed, the current sentence may not end.  Square brackets
 * (<code>"[", "]"</code>) and round brackets (<code>"(", ")"</code>),
 * are balanced separately.  The brackets need not be nested, and
 * extra close parentheses (<code>")"</code>) and brackets
 * (<code>"]"</code>) are ignored.
 *
 * <LI> <b>Force Final Boundary</b>: If this flag is set to
 * <code>true</code>, the final token in any input is taken to be a
 * sentence terminator, whether or not is a possible stop token.  This
 * is useful for dealing with truncated inputs, such as those in
 * MEDLINE abstracts.
 * 
 * </UL>
 *
 * A further condition is imposed on sentence initial tokens by method
 * {@link #possibleStart(String[],String[],int,int)}.  This method
 * checks a given token in sequence of tokens and whitespaces to
 * determine if it is a possible sentence start.  The default
 * implementation in this class is to rule out tokens that start with
 * lowercase letters.
 *
 * <P>The final condition is that a token cannot be a stop unless it
 * is followed by non-empty whitespace.
 *
 * <p> The resulting model will miss tokens as boundaries that act as
 * both sentence boundaries and end-of-abbreviation markers for known
 * abbreviations.  It will add spurious sentence boundaries that
 * appear after unknown abbreviations and are followed by whitespace
 * and a capitalized word. 
 *
 * <p>Our approach is loosely based on the article:
 *
 * <blockquote>
 * Mikheev, Andrei. 2002. 
 * <a href="http://acl.ldc.upenn.edu/J/J02/J02-3002.pdf">Periods, Capitalized Words, etc.</a>
 * <i>Computational Linguistics</i> <b>28</b>(3):289-318.
 * </blockquote>
 *
 * @author  Mitzi Morris, Bob Carpenter
 * @version 3.0
 * @since   LingPipe1.0
 */
public class HeuristicSentenceModel extends AbstractSentenceModel {

    /**
     * The set of possible tokens on which a sentence may end.
     */
    Set<String> mPossibleStops;

    /**
     * The set of tokens which may not precede a sentence-ending
     * token.
     */
    Set<String> mBadPrevious;

    /**
     * The set of tokens which may not follow a sentence-ending
     * token.
     */
    Set<String> mBadFollowing;

    /**
     * Flags whether or not the final token must be a sentence-ending
     * token.  If true, allows retrieval of truncated sentences that
     * occur at the end of the input.  Default value is false.
     * 
     */
    private final boolean mForceFinalStop;
    
    /**
     * Flags whether or not to track open and close parentheticals.
     * Default value is false.
     */
    private final boolean mBalanceParens;


    /**
     * Constructs a capitalization-sensitive heuristic sentence model
     * with the specified set of possible stop tokens, impossible
     * penultimate tokens, and impossible sentence start tokens.  Note
     * that these sets are <i>case insensitive</i>.  The default
     * constructor sets the balance parentheses and force final stops
     * flags to <code>false</code>.
     *
     * @param possibleStops Possible tokens on which to stop a sentence.
     * @param impossiblePenultimate Tokens that may not precede a stop.
     * @param impossibleStarts Tokens that may not follow a stop.
     */
    public HeuristicSentenceModel(Set<String> possibleStops,
                                  Set<String> impossiblePenultimate,
                                  Set<String> impossibleStarts) {
        this(possibleStops,impossiblePenultimate,impossibleStarts,false,false);
    }

    /**
     * Construct a heuristic sentence model with the specified sets
     * of possible stop tokens, impossible penultimate tokens, impossible
     * start tokens, and flags for whether the final token is forced
     * to be a stop, and whether parentheses are balanced.  Note that
     * the token sets are <i>case insensitive</i>.
     *
     * @param possibleStops Possible tokens on which to stop a sentence.
     * @param impossiblePenultimate Tokens that may not precede a stop.
     * @param impossibleStarts Tokens that may not follow a stop.
     
    */
    public HeuristicSentenceModel(Set<String> possibleStops,
                                  Set<String> impossiblePenultimate,
                                  Set<String> impossibleStarts,
                                  boolean forceFinalStop,
                                  boolean balanceParens) {
        mPossibleStops = toLowerCase(possibleStops);
        mBadPrevious = toLowerCase(impossiblePenultimate);
        mBadFollowing = toLowerCase(impossibleStarts);
        mForceFinalStop = forceFinalStop;
        mBalanceParens = balanceParens;
    }

    /**
     * Returns <code>true</code> if this model treats any input-final
     * token as a stop.  This ensures that in truncated inputs, all
     * tokens are or are followed by a sentence boundary.  For
     * instance, if the input is the array of tokens
     * <code>{&quot;a&quot;, &quot;b&quot;, &quot;.&quot;,
     * &quot;c&quot;, &quot;d&quot;}</code>, then if
     * <code>&quot;d&quot;</code> is <i>not</i> in the set of possible
     * stops, then the tokens <code>&quot;c&quot;</code> and
     * <code>&quot;d&quot;</code> will not be assigned to a sentence.
     * If the allow-any-final-token flag is <code>true</code>, then in
     * the case where the <code>&quot;d&quot;</code> is final in the
     * input, it will be taken to end a sentence.
     *
     * <P>The value is set in the constructor {@link
     * #HeuristicSentenceModel(Set,Set,Set,boolean,boolean)}.
     * See the class documentation for more information.
     *
     * @return <code>true</code> if any token may be a stop if
     * it is final in the input.
     */
    public boolean forceFinalStop() {
        return mForceFinalStop;
    }

    /**
     * Returns <code>true</code> if this model does parenthesis
     * balancing.  Note that the value is set in the constructor
     * {@link #HeuristicSentenceModel(Set,Set,Set,boolean,boolean)}.
     * See the class documentation for more information.
     *
     *
     * @return <code>true</code> if this model does parenthesis
     * balancing.
     */
    public boolean balanceParens() {
        return mBalanceParens;
    }


    /**
     * Adds the sentence final token indices as <code>Integer</code>
     * instances to the specified collection, only considering tokens
     * between index <code>start</code> and <code>end-1</code>
     * inclusive.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @param start Index of first token to annotate.
     * @param length Number of tokens to annotate.
     * @param indices Collection into which to write the boundary
     * indices.
     */
    public void boundaryIndices(String[] tokens, String[] whitespaces,
                                int start, int length,
                                Collection<Integer> indices) {
        if (length == 0) return;

        if (length == 1) {
            if (mForceFinalStop 
                || mPossibleStops.contains(tokens[start].toLowerCase())) {
        
                indices.add(new Integer(start));
            }
            return;
        }

        // run from second to penultimate tag (first can't be stop)
        boolean inParens = false;
        if (tokens[start].equals("(")) inParens = true;
        boolean inBrackets = false;
        if (tokens[start].equals("[")) inBrackets = true;
        int end = start+length-1;
        for (int i = start+1; i < end; ++i) {
            // check paren balancing
            if (mBalanceParens) { 
                if (tokens[i].equals("(")) {
                    inParens=true;
                    continue;
                }
                if (tokens[i].equals(")")) { 
                    inParens = false;
                    continue;
                }
                if (tokens[i].equals("[")) {
                    inBrackets=true;
                    continue;
                }
                if (tokens[i].equals("]")) {
                    inBrackets=false;
                    continue;
                }
                // don't break if we're in parenthetical or bracketed
                if (inParens || inBrackets) continue;
            }

            // check that token is good end of sentence token
            if (!mPossibleStops.contains(tokens[i].toLowerCase())) continue;

            // only break after whitespace
            if (whitespaces[i+1].length() == 0) continue;

            // check that previous token is OK sentence end
            if (mBadPrevious.contains(tokens[i-1].toLowerCase())) continue;

            // check that following token is OK sentence start
            if (mBadFollowing.contains(tokens[i+1].toLowerCase())) continue;

            // check following tokens, as needed
            if (!possibleStart(tokens,whitespaces,i+1,end)) continue;
        
            indices.add(new Integer(i));
        }

        // deal with case of last tag
        if (mForceFinalStop 
            || ( mPossibleStops.contains(tokens[end].toLowerCase())
                 && !mBadPrevious.contains(tokens[end-1].toLowerCase())))
            indices.add(new Integer(end));
    }

    /**
     * Return <code>true</code> if the specified start index can
     * be a sentence start in the specified array of tokens and
     * whitespaces running up to the end token.  
     *
     * <P>The implementation in this class requires the first token to
     * be non-empty and have a first character that is not lower case
     * according to {@link Character#isLowerCase(char)}.
     *
     * <P>The start and end indices should be within range for the
     * tokens and whitespaces as a precondition to this method being
     * called.  For a precise definition, see {@link
     * #verifyBounds(String[],String[],int,int)}.  All calls from the
     * abstract sentence model obey this constraint.
     *
     * @param tokens Array of tokens to check.
     * @param whitespaces Array of whitespaces to check.
     * @param start Index of first token to check.
     * @param end Index of last token to check.
     */
    protected boolean possibleStart(String[] tokens, String[] whitespaces,
                                    int start, int end) {
        String tok = tokens[start];
        return tok.length() > 0 
            && !Character.isLowerCase(tok.charAt(0));
    }

    static Set toLowerCase(Set xs) {
        HashSet result = new HashSet();
        Iterator it = xs.iterator();
        while (it.hasNext())
            result.add(it.next().toString().toLowerCase());
        return result;
    }
}
