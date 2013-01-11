package com.aliasi.test.unit.corpus;

import com.aliasi.corpus.TagHandler;

import com.aliasi.test.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.List;

public class CollectingTagHandler extends BaseTestCase implements TagHandler {

    
    List mTokenList = new ArrayList();
    List mTagList = new ArrayList();
    List mWhitespaceList = new ArrayList();

    public void handle(String[] tokens, String[] whitespaces,
                       String[] tags) {
        mTokenList.add(tokens);
        mWhitespaceList.add(whitespaces);
        mTagList.add(tags);
    }

    public void assertTokens(String[][] tokens) {
        assertArray(tokens,mTokenList);
    }

    public void assertWhitespaces(String[][] whitespaces) {
        assertArray(whitespaces,mWhitespaceList);
    }

    public void assertTags(String[][] tags) {
        assertArray(tags,mTagList);
    }

    public void assertTokenList(List tokenList) {
        assertTokens(toArray(tokenList));
    }

    public void assertWhitespaceList(List whiteList) {
        assertWhitespaces(toArray(whiteList));
    }

    public void assertTagList(List tagList) {
        assertTags(toArray(tagList));
    }

    static String[][] toArray(List xss) {
        return (String[][]) xss.toArray(new String[xss.size()][]);
    }

    void assertArray(String[][] xs, List xList) {
        for (int i = 0; i < xs.length; ++i) {
            assertEqualsArray(xs[i], (String[]) xList.get(i));
        }
    }
    
}

