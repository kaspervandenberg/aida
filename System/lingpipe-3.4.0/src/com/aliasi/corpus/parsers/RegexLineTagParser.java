package com.aliasi.corpus.parsers;

import com.aliasi.corpus.StringParser;
import com.aliasi.corpus.TagHandler;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a means of generating a tag parser based on a extracting
 * zone boundaries and token/tag pairs from lines of data using
 * regular expressions.  This provides a useful base implementation of
 * implementing the CoNLL text parsers, which zone inputs by sentence.
 *
 * <p>The parser is specified by means of three regular expressions.
 * If the ignore regular expression is matched, an input line is
 * ignored.  This is useful for ignoring empty lines and comments in
 * some inputs.  The eos regular expression recognizes lines that are
 * ends of sentences.  Whenever such a line is found, the zone
 * currently being processed is sent to the handler.  Finally, the
 * match regular expression is used to extract tags and tokens from
 * input lines, with the token index and tag index specifying the
 * subgroup matched in the regular expression.
 *
 * <p>Here is a worked example for the CoNLL 2002 data set, a subsequence
 * of which looks like:
 *
 * <blockquote><pre>
 * -DOCSTART- -DOCSTART- O
 * Met Prep O
 * tien Num O
 * miljoen Num O
 * komen V O
 * we Pron O
 * , Punc O
 * denk V O
 * ik Pron O
 * , Punc O
 * al Adv O
 * een Art O
 * heel Adj O
 * eind N O
 * . Punc O
 * 
 * Dirk N B-PER
 * ...
 * </pre></blockquote>
 *
 * And here's the regular expressions used to parse it:
 * 
 * <blockquote><pre>
 * String TOKEN_TAG_LINE_REGEX
 *     = "(\\S+)\\s(\\S+\\s)?(O|[B|I]-\\S+)"; // token ?posTag entityTag
 *
 * int TOKEN_GROUP = 1; // token
 * int TAG_GROUP = 3;   // entityTag
 *
 * String IGNORE_LINE_REGEX
 *     = "-DOCSTART(.*)";  // lines that start with "-DOCSTART"
 *
 * String EOS_REGEX
 *     = "\\A\\Z";         // empty/blank lines
 *
 * Parser parser 
 *     = new RegexLineTagParser(TOKEN_TAG_LINE_REGEX, 
 *                              TOKEN_GROUP, TAG_GROUP,
 *                              IGNORE_LINE_REGEX, 
 *                              EOS_REGEX);
 * </pre></blockquote>
 *
 * Lines starting with <code>&quot;-DOCSTART&quot;</code> are
 * ignored, blank lines end sentences; tokens and entity tags
 * are extracted by matching the regular expression and pulling
 * out match group 1 as the token and match group 3 as the tag.
 * An optional part-of-speech tag between the token and tag
 * on the line is ignored.
 *
 * @author  Bob Carpenter
 * @version 2.4.0
 * @since   LingPipe2.4.0
 */
public class RegexLineTagParser extends StringParser {

    private final Pattern mTokenTagPattern;
    private final Pattern mIgnoreLinePattern;
    private final Pattern mEosPattern;
    private final int mTokenGroup;
    private final int mTagGroup;

    /**
     * Construct a regular expression tag parser from the specified
     * regular expressions and indexes. See the class documentation
     * for further information.
     * 
     * @param matchRegex Regular expression for matching tokens and tags.
     * @param tokenGroup Index of group in regular expression for token.
     * @param tagGroup Index of group in regular expression for tag.
     * @param ignoreRegex Lines matching this regular expression are
     * skipped.
     * @param eosRegex Matches end of sentence for grouping handle
     * events.
     */
    public RegexLineTagParser(String matchRegex, int tokenGroup, int tagGroup,
                              String ignoreRegex,
                              String eosRegex) {
        this(null,matchRegex,tokenGroup,tagGroup,ignoreRegex,eosRegex);
    }

    /**
     * Construct a regular expression tag parser from the specified
     * regular expressions and indexes. See the class documentation
     * for further information.
     * 
     * @param handler Tag handler for this parser.
     * @param matchRegex Regular expression for matching tokens and tags.
     * @param tokenGroup Index of group in regular expression for token.
     * @param tagGroup Index of group in regular expression for tag.
     * @param ignoreRegex Lines matching this regular expression are
     * skipped.
     * @param eosRegex Matches end of sentence for grouping handle
     * events.
     */
    public RegexLineTagParser(TagHandler handler, 
                              String matchRegex, int tokenGroup, int tagGroup, 
                              String ignoreRegex, String eosRegex) {
        super(handler);
        mTokenTagPattern = Pattern.compile(matchRegex);
        mTokenGroup = tokenGroup;
        mTagGroup = tagGroup;
        mIgnoreLinePattern = Pattern.compile(ignoreRegex);
        mEosPattern = Pattern.compile(eosRegex);
    }
                          
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        String[] lines = in.split("\n");
        ArrayList tokenList = new ArrayList();
        ArrayList tagList = new ArrayList();
        for (int i = 0; i < lines.length; ++i) {

            Matcher lineIgnorer = mIgnoreLinePattern.matcher(lines[i]);
            if (lineIgnorer.matches()) continue;

            Matcher eosMatcher = mEosPattern.matcher(lines[i]);
            if (eosMatcher.matches()) {
                handle(tokenList,tagList);
                continue;
            }


            Matcher matcher = mTokenTagPattern.matcher(lines[i]);
            if (!matcher.matches()) {
                String msg = "Illegal line=" + lines[i];
                throw new IllegalArgumentException(msg);
            }
            String token = matcher.group(mTokenGroup);
            String tag = matcher.group(mTagGroup);
            tokenList.add(token);
            tagList.add(tag);
        }
        handle(tokenList,tagList);
    }

    /**
     * Returns the tag handler for this tag parser.  This
     * is just a convenience cast of {@link #getHandler()}.
     * 
     * @return Tag handler.
     */
    public TagHandler getTagHandler() {
        return (TagHandler) getHandler();
    }

    private void handle(ArrayList tokenList, ArrayList tagList) {
        int len = tokenList.size();
        String[] tokens = (String[]) tokenList.toArray(new String[len]);
        String[] tags = (String[]) tagList.toArray(new String[len]);
        getTagHandler().handle(tokens,null,tags);
        tokenList.clear();
        tagList.clear();
    }

}
