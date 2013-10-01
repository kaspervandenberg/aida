// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Some {@link Iterators} do not support {@link Iterator#remove()}, this decorator uses the collection to support remove on such 
 * iterators. 
 *
 * Decorator pattern
 * 
 * @param <TElement>	Type of element contains in the collection and delegate iterator.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class IteratorDecoratorSupportingDelete<TElement> implements Iterator<TElement> {
	/*
	 * State pattern; Flyweight pattern
	 */
	private enum State {
		LAST_UNKNOWN {
			@Override
			public <TElement> void remove(IteratorDecoratorSupportingDelete<TElement> context) {
				throw new IllegalStateException("Call next() before removing an element");
			}
		}, LAST_AVAILABLE {
			@Override
			public <TElement> void remove(IteratorDecoratorSupportingDelete<TElement> context) {
				context.doRemove();
			}
		};

		public abstract <TElement> void remove(IteratorDecoratorSupportingDelete<TElement> context);

		public <TElement> TElement next(IteratorDecoratorSupportingDelete<TElement> context) {
			return context.delegateNext();
		}

		public <TElement> boolean hasNext(IteratorDecoratorSupportingDelete<TElement> context) {
			return context.delegateHasNext();
		}
	}
	private final Collection<TElement> items;
	private final Iterator<TElement> delegate;
	private State state;
	/*@Nullable*/ private TElement lastReturned;

	public IteratorDecoratorSupportingDelete(Collection<TElement> items_, Iterator<TElement> delegate_) {
		this.items = items_;
		this.delegate = delegate_;
		this.state = State.LAST_UNKNOWN;
		this.lastReturned = null;
	}

	@Override
	public boolean hasNext() {
		return state.hasNext(this);
	}

	@Override
	public TElement next() {
		return state.next(this);
	}

	@Override
	public void remove() {
		state.remove(this);
	}

	private void setNextState(State next) {
		state = next;
	}

	private TElement delegateNext() {
		TElement result = delegate.next();
		setNextState(State.LAST_AVAILABLE);
		return result;
	}

	private boolean delegateHasNext() {
		return delegate.hasNext();
	}

	private void doRemove() {
		items.remove(lastReturned);
		setNextState(State.LAST_UNKNOWN);
	}
}
