/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public class NestedIterable<TOuter, TOutput> implements Iterable<TOutput> {
	private final Transformer<TOuter, Iterable<TOutput>> iterableGenerator;
	private final Iterable<TOuter> outer;

	public NestedIterable(Transformer<TOuter, Iterable<TOutput>> iterableGenerator_,
			Iterable<TOuter> outer_) {
		iterableGenerator = iterableGenerator_;
		outer = outer_;
	}

	@Override
	public Iterator<TOutput> iterator() {
		return new Iterator<TOutput>() {
			private Iterator<TOuter> iOuter = outer.iterator();
			private Iterator<TOutput> iInner = null;

			@Override
			public boolean hasNext() {
				return moveToNextInner();
			}

			@Override
			public TOutput next() {
				if(moveToNextInner()) {
					return iInner.next();
				} else {
					throw new NoSuchElementException("Iteration at end.");
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove() has no semantics for nested iterations.");
			}
			
			/**
			 * Position the wrapped iterators so that {@code inner}.{@link
			 * Iterator#next()} and {@code inner}.{@link Iterator#hasNext()}
			 * produce valid results for the nested iteration.
			 *
			 * @result	<ul><li>{@code true}: {@code inner} has more results;
			 * 		postcondition: {@link #innerVallid()}.</li>
			 * 		<li>{@code false}: the iteration is at end;
			 * 		postcondition: {@code !outer.hasNext() && !innerVallid()}
			 */
			private boolean moveToNextInner() {
				if (iInner != null) {
					if (iInner.hasNext()) {
						assert (innerVallid());
						return true;
					}
				}
				assert (!innerVallid());
				while (iOuter.hasNext()) {
					assert (!innerVallid());
					iInner = iterableGenerator.transform(iOuter.next()).iterator();
					if (iInner.hasNext()) {
						assert (innerVallid());
						return true;
					}
				}
				assert (!iOuter.hasNext() && !innerVallid());
				return false;
			}

			private boolean innerVallid() {
				return iInner != null && iInner.hasNext();
			}
		};
	}

	
}
