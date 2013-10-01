// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ObserverCollection<TObserver, TSource> extends AbstractCollection<TObserver> {
	private final CopyOnWriteArrayList<WeakReference<TObserver>> observers;
	private final Class<TObserver> observerType;
	private final TSource defaultSource;
	private final Method observerMethod;

	public ObserverCollection(TSource source_, Class<TObserver> observerType_, Method observerMethod_) {
		this.observers = new CopyOnWriteArrayList<>();
		this.observerType = observerType_;
		this.defaultSource = source_;
		assertMethodExists(observerType, observerMethod_);
		this.observerMethod = observerMethod_;
	}

	public <TEvent> void fireEvent(TEvent event) {
		Object[] args = createObserverMethodArgs(defaultSource, event);
		for (TObserver observer : this) {
			invokeObserverMethod(observer, args);
		}
	}

	@Override
	public Iterator<TObserver> iterator() {
		Iterator<WeakReference<TObserver>> deletingIterator = 
				new IteratorDecoratorSupportingDelete<>(observers, observers.iterator());
		
		return new StaleReferenceSkippingIterator<>(deletingIterator, true);
	}

	@Override
	public int size() {
		return observers.size();
	}
	
	@Override
	public boolean add(TObserver observer) {
		return observers.add(new WeakReference<>(observer));
	}

	@Override
	public boolean remove(Object obj_observer) {
		if(observerType.isInstance(obj_observer)) {
			return removeObserver(observerType.cast(obj_observer));
		} else {
			return false;
		}
	}

	private static <TObserver> void assertMethodExists(Class<TObserver> observerType, Method method) {
		if(!method.getDeclaringClass().isAssignableFrom(observerType)) {
			throw new IllegalArgumentException(String.format("%s is not a method of %s", method, observerType));
		}
	}
	
	private static <TSource, TEvent> Object[] createObserverMethodArgs(TSource source, TEvent event, Object... other) {
		Object[] result = new Object[other.length + 2];
		result[0] = source;
		result[1] = event;
		System.arraycopy(other, 0, result, 2, other.length);
		return result;
	}

	private boolean removeObserver(TObserver observer) {
		Iterator<TObserver> iter = iterator();
		while (iter.hasNext()) {
			TObserver element = iter.next();
			if(element.equals(observer)) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	private void invokeObserverMethod(TObserver observer, Object[] arguments) {
		try {
			observerMethod.invoke(observer, arguments);
		} catch (InvocationTargetException ex) {
			Logger.getLogger(ObserverCollection.class.getName()).log(Level.SEVERE, "Exception when notifying observer", ex);
		} catch (IllegalAccessException | IllegalArgumentException ex) {
			throw new Error("Unexpected exception", ex);
		}
	}

}
