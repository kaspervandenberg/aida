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

package com.aliasi.cluster;

import com.aliasi.util.Distance;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A <code>LinkDendrogram</code> consists of a pair of sub-dendrograms
 * which are joined at a specified cost.  Although typically used in
 * the case where the sub-dendrograms have lower costs than their
 * parent dendrograms, this condition is not enforced by this
 * implementation.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class LinkDendrogram<E> extends Dendrogram<E> {

    private final double mCost;
    private final Dendrogram<E> mDendrogram1;
    private final Dendrogram<E> mDendrogram2;

    /**
     * Construct a link dendrogram containing the specified object.
     *
     * @param dendrogram1 First dendrogram in cluster.
     * @param dendrogram2 Second dendrogram in cluster.
     * @param cost Cost of creating this dendrogram from the specified
     * dendrograms.
     * @throws IllegalArgumentException If the cost is less than
     * <code>0.0</code>.
     */
    public LinkDendrogram(Dendrogram<E> dendrogram1,
                          Dendrogram <E> dendrogram2,
                          double cost) {
        if (cost < 0.0 || Double.isNaN(cost)) {
            String msg = "Cost must be >= 0.0"
                + " Found cost=" + cost;
            throw new IllegalArgumentException(msg);
        }
        dendrogram1.setParent(this);
        dendrogram2.setParent(this);
        mDendrogram1 = dendrogram1;
        mDendrogram2 = dendrogram2;
        mCost = cost;
    }

    /**
     * Returns the cost of this dendogram.  The cost is specified at
     * construction time and is meant to indicate the proximity
     * between the elements.
     *
     * @return The proximity between the pair of component
     * dendrograms making up this dendrogram.
     */
    public double score() {
        return mCost;
    }

    public Set<E> memberSet() {
        HashSet<E> members = new HashSet<E>();
        addMembers(members);
        return members;
    }

    void addMembers(Set<E> set) {
        mDendrogram1.addMembers(set);
        mDendrogram2.addMembers(set);
    }

    void split(Collection<Set<E>> resultSet,
               Collection<Dendrogram<E>> queue) {
        queue.add(mDendrogram1);
        queue.add(mDendrogram2);
    }

    void partitionDistance(Set<Set<E>> clustering,
                           double maxProximity) {
        if (score() <= maxProximity) {
            clustering.add(memberSet());
        } else {
            mDendrogram1.partitionDistance(clustering,maxProximity);
            mDendrogram2.partitionDistance(clustering,maxProximity);
        }
    }

    /**
     * Returns the first dendrogram in the linked dendrogram.  This is
     * the first dendrogram in constructor argument order, but the
     * order is irrelevant in the semantics of dendrograms as they
     * represent unordered trees.
     *
     * @return The first dendrogram linked.
     */
    public Dendrogram dendrogram1() {
        return mDendrogram1;
    }

    /**
     * Returns the second dendrogram in the linked dendrogram.  This
     * is the second dendrogram in constructor argument order, but the
     * order is irrelevant in the semantics of dendrograms as they
     * represent unordered trees.
     *
     * @return The second dendrogram linked.
     */
    public Dendrogram dendrogram2() {
        return mDendrogram2;
    }

    Dendrogram[] daughters() {
        return new Dendrogram[] { mDendrogram1, mDendrogram2 };
    }

    int copheneticCorrelation(int i, double[] xs, double[] ys,
                              Distance<? super E> distance) {
        for (E e1 : mDendrogram1.memberSet()) {
            for (E e2 : mDendrogram2.memberSet()) {
                xs[i] = score();
                ys[i] = distance.distance(e1,e2);
                ++i;
            }
        }
        return i;
    }

    void toString(StringBuffer sb, int depth) {
        sb.append('{');
        mDendrogram1.toString(sb,depth+1);
        sb.append('+');
        mDendrogram2.toString(sb,depth+1);
        sb.append("}:");
        sb.append(mCost);
    }
    
    void prettyPrint(StringBuffer sb, int depth) {
        indent(sb,depth);
        sb.append(score());
        mDendrogram1.prettyPrint(sb,depth+1);
        mDendrogram2.prettyPrint(sb,depth+1);
    }

}
