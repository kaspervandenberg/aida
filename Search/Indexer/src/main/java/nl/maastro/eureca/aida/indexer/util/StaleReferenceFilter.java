/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.util;

import java.lang.ref.Reference;
import java.util.Iterator;

/**
 *
 * @author kasper
 */
public class StaleReferenceFilter<T> implements Iterable<T> {
	private final Iterable<? extends Reference<T>> delgate;
	private final boolean removeStaleReferences;

	public StaleReferenceFilter(Iterable<? extends Reference<T>> delgate_, boolean removeStaleReferences_) {
		this.delgate = delgate_;
		this.removeStaleReferences = removeStaleReferences_;
	}

	@Override
	public Iterator<T> iterator() {
		return new StaleReferenceSkippingIterator<>(delgate.iterator(), removeStaleReferences);
	}
	
}
