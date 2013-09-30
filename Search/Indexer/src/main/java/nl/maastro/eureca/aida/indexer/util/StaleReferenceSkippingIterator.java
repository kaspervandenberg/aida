/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.util;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author kasper
 */
public class StaleReferenceSkippingIterator<T> implements Iterator<T> {
	private enum State {
		PEEKED_AT_NEXT {
			@Override
			public State findNext(StaleReferenceSkippingIterator<?> context) {
				return PEEKED_AT_NEXT;
			}

			@Override
			public State remove(StaleReferenceSkippingIterator<?> context) {
				throw new IllegalStateException("Cannot currently remove element; StaleReferenceSkippingIterator only supports remove() before hasNext()");
			}
		},
		
		RETURNED_NEXT {
			@Override
			public State findNext(StaleReferenceSkippingIterator<?> context) {
				return context.peekNext(RETURNED_NEXT);
			}

			@Override
			public State remove(StaleReferenceSkippingIterator<?> context) {
				return context.forwardRemoveToDelegate();
			}
		},
		
		NEXT_UNKNOWN {
			@Override
			public State findNext(StaleReferenceSkippingIterator<?> context) {
				return context.peekNext(NEXT_UNKNOWN);
			}

			@Override
			public State remove(StaleReferenceSkippingIterator<?> context) {
				throw new IllegalStateException("Cannot currently remove element; call next() before remove()");
			}
		};

		public abstract State findNext(StaleReferenceSkippingIterator<?> context);
		public abstract State remove(StaleReferenceSkippingIterator<?> context);
	}
	
	private final Iterator<? extends Reference<T>> delegate;
	private final boolean removeStaleReferences;
	private State state;
	private T next;

	public StaleReferenceSkippingIterator(Iterator<? extends Reference<T>> delegate_, boolean removeStaleReferences_) {
		this.delegate = delegate_;
		this.removeStaleReferences = removeStaleReferences_;
		this.state = State.NEXT_UNKNOWN;
		this.next = null;
	}

	@Override
	public boolean hasNext() {
		state = state.findNext(this);
		return (next != null);
	}

	@Override
	public T next() {
		state = state.findNext(this);
		if(next == null) {
			throw new NoSuchElementException("At end of iteration");
		}
		return returnNext();
	}

	@Override
	public void remove() {
		state = state.remove(this);
	}
	
	private State peekNext(State cur) {
		State result = cur;
		while(delegate.hasNext() && next == null) {
			Reference<T> ref = delegate.next();
			result = State.PEEKED_AT_NEXT;
			next = ref.get();
			if(removeStaleReferences && next == null) {
				delegate.remove();
			}
		}
		return result;
	}

	private State forwardRemoveToDelegate() {
		delegate.remove();
		next = null;
		return State.NEXT_UNKNOWN;
	}

	private T returnNext() {
		T result = next;
		next = null;
		state = State.RETURNED_NEXT;
		return result;
	}
}
