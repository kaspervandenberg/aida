/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

import java.util.Iterator;

/**
 * Transform an Iterable of {@code <TIn>} to one of {@code <TOut>} by applying 
 * {@link #transformer}
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public class TransformingIterable<TIn, TOut> implements Iterable<TOut> {
	private final Transformer<TIn, TOut> transformer;
	private final Iterable<TIn> wrapped;
	
	public TransformingIterable(Transformer<TIn, TOut> transformer_, Iterable<TIn> wrapped_) {
		transformer = transformer_;
		wrapped = wrapped_;
	}

	@Override
	public Iterator<TOut> iterator() {
		return new Iterator<TOut>() {
			private final Iterator<TIn> iWrapped = wrapped.iterator();

			@Override
			public boolean hasNext() {
				return iWrapped.hasNext();
			}

			@Override
			public TOut next() {
				return transformer.transform(iWrapped.next());
			}

			@Override
			public void remove() {
				iWrapped.remove();
			}
		};
	}
	
	
}
