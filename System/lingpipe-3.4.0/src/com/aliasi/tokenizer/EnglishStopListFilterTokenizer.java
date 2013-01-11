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

package com.aliasi.tokenizer;

import java.util.HashSet;

/**
 * An <code>EnglishStopListFilterTokenizer</code> filters its input by
 * removing words on the English stop list.  The stoplist is:
 *
 * <blockquote>
 *   a, be, had, it, only, she, was, about, because, has,
 *   its, of, some, we, after, been, have, last, on, such, were, all,
 *   but, he, more, one, than, when, also, by, her, most, or, that,
 *   which, an, can, his, mr, other, the, who, any, co, if, mrs, out,
 *   their, will, and, corp, in, ms, over, there, with, are, could, inc,
 *   mz, s, they, would, as, for, into, no, so, this, up, at, from, is,
 *   not, says, to
 * </blockquote>
 *
 * Note that the stoplist entries are all lowercase.  The input should
 * first be filtered by a {@link LowerCaseFilterTokenizer}.
 *
 * @author  Bob Carpenter
 * @version 1.0.3
 * @since   LingPipe1.0
 */
public class EnglishStopListFilterTokenizer extends StopListFilterTokenizer {

    /**
     * Construct an English stoplist filter tokenizer.
     */
    public EnglishStopListFilterTokenizer(Tokenizer tokenizer) {
        super(tokenizer,ENGLISH_STOP_LIST);
    }

    /**
     * The set of stop words, all lowercased.
     */
    private static final HashSet ENGLISH_STOP_LIST = new HashSet();
    static {
        ENGLISH_STOP_LIST.add("a");
        ENGLISH_STOP_LIST.add("be");
        ENGLISH_STOP_LIST.add("had");
        ENGLISH_STOP_LIST.add("it");
        ENGLISH_STOP_LIST.add("only");
        ENGLISH_STOP_LIST.add("she");
        ENGLISH_STOP_LIST.add("was");
        ENGLISH_STOP_LIST.add("about");
        ENGLISH_STOP_LIST.add("because");
        ENGLISH_STOP_LIST.add("has");
        ENGLISH_STOP_LIST.add("its");
        ENGLISH_STOP_LIST.add("of");
        ENGLISH_STOP_LIST.add("some");
        ENGLISH_STOP_LIST.add("we");
        ENGLISH_STOP_LIST.add("after");
        ENGLISH_STOP_LIST.add("been");
        ENGLISH_STOP_LIST.add("have");
        ENGLISH_STOP_LIST.add("last");
        ENGLISH_STOP_LIST.add("on");
        ENGLISH_STOP_LIST.add("such");
        ENGLISH_STOP_LIST.add("were");
        ENGLISH_STOP_LIST.add("all");
        ENGLISH_STOP_LIST.add("but");
        ENGLISH_STOP_LIST.add("he");
        ENGLISH_STOP_LIST.add("more");
        ENGLISH_STOP_LIST.add("one");
        ENGLISH_STOP_LIST.add("than");
        ENGLISH_STOP_LIST.add("when");
        ENGLISH_STOP_LIST.add("also");
        ENGLISH_STOP_LIST.add("by");
        ENGLISH_STOP_LIST.add("her");
        ENGLISH_STOP_LIST.add("most");
        ENGLISH_STOP_LIST.add("or");
        ENGLISH_STOP_LIST.add("that");
        ENGLISH_STOP_LIST.add("which");
        ENGLISH_STOP_LIST.add("an");
        ENGLISH_STOP_LIST.add("can");
        ENGLISH_STOP_LIST.add("his");
        ENGLISH_STOP_LIST.add("mr");
        ENGLISH_STOP_LIST.add("other");
        ENGLISH_STOP_LIST.add("the");
        ENGLISH_STOP_LIST.add("who");
        ENGLISH_STOP_LIST.add("any");
        ENGLISH_STOP_LIST.add("co");
        ENGLISH_STOP_LIST.add("if");
        ENGLISH_STOP_LIST.add("mrs");
        ENGLISH_STOP_LIST.add("out");
        ENGLISH_STOP_LIST.add("their");
        ENGLISH_STOP_LIST.add("will");
        ENGLISH_STOP_LIST.add("and");
        ENGLISH_STOP_LIST.add("corp");
        ENGLISH_STOP_LIST.add("in");
        ENGLISH_STOP_LIST.add("ms");
        ENGLISH_STOP_LIST.add("over");
        ENGLISH_STOP_LIST.add("there");
        ENGLISH_STOP_LIST.add("with");
        ENGLISH_STOP_LIST.add("are");
        ENGLISH_STOP_LIST.add("could");
        ENGLISH_STOP_LIST.add("inc");
        ENGLISH_STOP_LIST.add("mz");
        ENGLISH_STOP_LIST.add("s");
        ENGLISH_STOP_LIST.add("they");
        ENGLISH_STOP_LIST.add("would");
        ENGLISH_STOP_LIST.add("as");
        ENGLISH_STOP_LIST.add("for");
        ENGLISH_STOP_LIST.add("into");
        ENGLISH_STOP_LIST.add("no");
        ENGLISH_STOP_LIST.add("so");
        ENGLISH_STOP_LIST.add("this");
        ENGLISH_STOP_LIST.add("up");
        ENGLISH_STOP_LIST.add("at");
        ENGLISH_STOP_LIST.add("from");
        ENGLISH_STOP_LIST.add("is");
        ENGLISH_STOP_LIST.add("not");
        ENGLISH_STOP_LIST.add("says");
        ENGLISH_STOP_LIST.add("to");
    }
}
