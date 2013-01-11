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

package com.aliasi.lm;

import java.io.IOException;

import java.util.Stack;

/**
 * A <code>MultiTrieReader</code> merges two trie readers, providing
 * output that is the result of adding the counts from the two readers.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.3
 */
public class MultiTrieReader implements TrieReader {

    private final Stack mStack1 = new Stack();
    private final Stack mStack2 = new Stack();

    private final TrieReader mReader1;
    private final TrieReader mReader2;

    private boolean mNotInitialized = true;

    /**
     * Construct a multiple trie reader that returns counts that
     * are the sum of the two readers' counts.
     *
     * @param reader1 First reader.
     * @param reader2 Second reader.
     */
    public MultiTrieReader(TrieReader reader1, TrieReader reader2) {
    mReader1 = reader1;
    mReader2 = reader2;
    }

    public long readSymbol() throws IOException {
    // System.out.println("               readSymbol: Stack1=" + mStack1
    // + " Stack2=" + mStack2);
    if (mStack1.size() > mStack2.size()) {
        long symbol = peek(mStack1);
        if (symbol == -1L) {
        mStack1.pop();
        replace(mStack1,mReader1);
        } else {
        replace(mStack1,-2L);
        }
        // System.out.println("SYMBOLa=" + symbol);
        return symbol;
    }
    if (mStack1.size() < mStack2.size()) {
        long symbol = peek(mStack2);
        if (symbol == -1L) {
        mStack2.pop();
        replace(mStack2,mReader2);
        } else {
        replace(mStack2,-2L);
        }
        // System.out.println("SYMBOLb=" + symbol);
        return symbol;
    }

    // same stack size
    long top1 = peek(mStack1);
    long top2 = peek(mStack2);
    if (top1 == -1 && top2 == -1) {  
        mStack1.pop();
        replace(mStack1,mReader1);
        mStack2.pop();
        replace(mStack2,mReader2);
        // System.out.println("SYMBOLc=-1L");
        return -1L;
    }
    if (top2 == -1L || (top1 != -1L && top1 < top2)) {
        replace(mStack1,-2L);
        // System.out.println("SYMBOLd=" + top1);
        return top1;
    } 
    if (top1 == -1L || (top2 != -1L && top2 < top1)) {
        replace(mStack2,-2L);
        // System.out.println("SYMBOLe=" + top2);
        return top2;
    }
    
    // top1 == top2, top1 != -1
    replace(mStack1,-2L);
    replace(mStack2,-2L);
    // System.out.println("SYMBOLf=" + top1);
    return top1;
    }

    public long readCount() throws IOException {
    // System.out.println("               readCount: Stack1=" + mStack1
    // + " Stack2=" + mStack2);
    if (mNotInitialized) {
        mNotInitialized = false;
        long count = mReader1.readCount() + mReader2.readCount();
        push(mStack1,mReader1.readSymbol());
        push(mStack2,mReader2.readSymbol());
        // System.out.println("COUNTa=" + count);
        return count;
    } 
    if (mStack1.size() > mStack2.size()) {
        long count = mReader1.readCount();
        push(mStack1,mReader1.readSymbol());
        // System.out.println("COUNTb=" + count);
        return count;
    }
    if (mStack1.size() < mStack2.size()) {
        long count = mReader2.readCount();
        push(mStack2,mReader2.readSymbol());
        // System.out.println("COUNTc=" + count);
        return count;
    }
    if (peek(mStack1) == -2 && peek(mStack2) == -2) {
        long count = mReader1.readCount() + mReader2.readCount();
        push(mStack1,mReader1.readSymbol());
        push(mStack2,mReader2.readSymbol());
        // System.out.println("COUNTd=" + count);
        return count;
    }
    if (peek(mStack1) == -2) {
        long count = mReader1.readCount();
        push(mStack1,mReader1.readSymbol());
        // System.out.println("COUNTe=" + count);
        return count;
    } 
    if (peek(mStack2) == -2) {
        long count = mReader2.readCount();
        push(mStack2,mReader2.readSymbol());
        // System.out.println("COUNTf=" + count);
        return count;
    }
    throw new IllegalStateException("readCount(): Stack1=" + mStack1
                    + " Stack2=" + mStack2);
    }


    static long peek(Stack stack) {
    return ((Long)stack.peek()).longValue();
    }

    static void push(Stack stack, long x) {
    stack.push(new Long(x));
    }

    static long pop(Stack stack) {
    return ((Long)stack.pop()).longValue();
    }

    static void replace(Stack stack, TrieReader reader) throws IOException {
    if (stack.size() > 0)
        replace(stack,reader.readSymbol());
    }

    static void replace(Stack stack, long x) {
    if (stack.size() == 0) return;
    pop(stack);
    stack.push(new Long(x));
    }

}
