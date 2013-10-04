// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ObserverCollection<TObserver, TSource> extends AbstractCollection<TObserver> {
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Observer { }

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface NotifyMethod { }
	
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

	public ObserverCollection(TSource source, Class<TObserver> observerType_) {
		this(source, observerType_, detectNotifyMethod(observerType_));
	}

	public <TEvent> void fireEvent(TEvent event) {
		Object[] args = createObserverMethodArgs(defaultSource, event);
		invokeAll(args);
	}

	public <TData> void fireChangeEvent(TData oldValue, TData newValue) {
		Object[] args = createObserverMethodArgs(defaultSource, oldValue, newValue);
		invokeAll(args);
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

	private static Method detectNotifyMethod(Class<?> observerType) {
		if (observerType.getAnnotation(Observer.class) == null) {
			throw new IllegalArgumentException(String.format("Class %s not annotated with @Observer", observerType));
		}
		Set<Method> annotatedMethods = new HashSet<>();
		for (Method method : observerType.getMethods()) {
			if(method.getAnnotation(NotifyMethod.class) != null) {
				annotatedMethods.add(method);
			}
		}
		if (annotatedMethods.size() != 1) {
			throw new IllegalArgumentException("Exactly one method must be annotated with @NotifyMethod");
		}
		return annotatedMethods.iterator().next();
	}

	private static <TObserver> void assertMethodExists(Class<TObserver> observerType, Method method) {
		if(!method.getDeclaringClass().isAssignableFrom(observerType)) {
			throw new IllegalArgumentException(String.format("%s is not a method of %s", method, observerType));
		}
	}
	
	private static <TSource, TEvent> Object[] createObserverMethodArgs(TSource source, TEvent event) {
		Object[] result = new Object[2];
		result[0] = source;
		result[1] = event;
		return result;
	}

	private static <TSource, TData> Object[] createObserverMethodArgs(TSource source, TData oldValue, TData newValue) {
		Object[] result = new Object[3];
		result[0] = source;
		result[1] = oldValue;
		result[2] = newValue;
		return result;
	}

	private void invokeAll(Object[] args) {
		for (TObserver observer : this) {
			invokeObserverMethod(observer, args);
		}
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
