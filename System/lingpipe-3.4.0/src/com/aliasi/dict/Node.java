package com.aliasi.dict;

import java.util.Arrays;

class Node {
    static final DictionaryEntry[] EMPTY_ENTRIES = new DictionaryEntry[0];
    static final char[] EMPTY_CHARS = new char[0];
    static final Node[] EMPTY_NODES = new Node[0];

    DictionaryEntry[] mEntries = EMPTY_ENTRIES;
    char[] mDtrChars = EMPTY_CHARS;
    Node[] mDtrNodes = EMPTY_NODES;
    Node getDtr(char c) {
    int i = Arrays.binarySearch(mDtrChars,c);
    return i < 0 ? null : mDtrNodes[i];
    }
    Node getOrAddDtr(char c) {
    Node dtr = getDtr(c);
    if (dtr != null) return dtr;
    Node result = new Node();
    char[] oldDtrChars = mDtrChars;
    Node[] oldDtrNodes = mDtrNodes;
    mDtrChars = new char[mDtrChars.length+1];
    mDtrNodes = new Node[mDtrNodes.length+1];
    int i = 0;
    for (; i < oldDtrChars.length; ++i) {
        if (oldDtrChars[i] > c) break;
        mDtrChars[i] = oldDtrChars[i];
        mDtrNodes[i] = oldDtrNodes[i];
    }
    mDtrChars[i] = c;
    mDtrNodes[i] = result;
    for (; i < oldDtrChars.length; ++i) {
        mDtrChars[i+1] = oldDtrChars[i];
        mDtrNodes[i+1] = oldDtrNodes[i];
    }
    return result;
        
    }
    void addEntry(DictionaryEntry entry) {
    DictionaryEntry[] oldEntries = mEntries;
    for (int i = 0; i < oldEntries.length; ++i)
        if (oldEntries[i].equals(entry)) return;
    mEntries = new DictionaryEntry[oldEntries.length+1];
    mEntries[0] = entry;
    System.arraycopy(oldEntries,0,mEntries,1,oldEntries.length);
    }
}

