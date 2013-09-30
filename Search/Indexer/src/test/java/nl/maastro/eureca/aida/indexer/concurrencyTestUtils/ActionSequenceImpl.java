/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author kasper
 */
public class ActionSequenceImpl<T> implements ActionSequence<T> {
	public abstract class ActionTypeReference<T2 extends Action<?>> {
		private final Type type;
		
	    protected ActionTypeReference() {
			Type superclass = getClass().getGenericSuperclass();
			if (superclass instanceof Class) {
				throw new RuntimeException("Missing type parameter.");
			}
			this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
		}

		public boolean isInstance(Object obj) {
			return getRawType().isInstance(obj);
		}

		private Class<?> getRawType() {
			if (type instanceof Class<?>) {
				return (Class<?>)type;
			} else {
				return (Class<?>) ((ParameterizedType) type).getRawType();
			}
		}
	}

	public static abstract class Builder<T> {
		private final List<Action<T>> toBuild = new LinkedList<>();

		public ActionSequenceImpl<T> build() {
			return new ActionSequenceImpl<>(toBuild);
		}

		protected void schedule(int i) {
			toBuild.add(new Schedule<T>(i));
		}

		protected void enableEnd(int i) {
			toBuild.add(new EnableEnd<T>(i));
		}

		protected void enableStart(int i) {
			toBuild.add(new EnableStart<T>(i));
		}
	}
	
	private final List<Action<T>> actions;

	public ActionSequenceImpl(List<Action<T>> actions_) {
		this.actions = actions_;
	}

	@Override
	public void execute(ConcurrentTestContext<T> context) {
		for (Action<T> action : actions) {
			action.execute(context);
		}
	}

	@Override
	public boolean willSchedule(int taskId) {
		return contains(new ActionTypeReference<Schedule<T>>() { }, taskId);
	}

	@Override
	public boolean willStart(int taskId) {
		return willSchedule(taskId) && contains(new ActionTypeReference<EnableStart<T>>() { }, taskId);
	}

	@Override
	public boolean willFinish(int taskId) {
		return willStart(taskId) && contains(new ActionTypeReference<EnableEnd<T>>() { }, taskId);
	}

	private boolean contains(ActionTypeReference<?> type, int taskId) {
		for (Iterator<Action<T>> it = actions.iterator(); it.hasNext();) {
			Action<T> action = it.next();
			if (action.appliesTo(taskId) && type.isInstance(action)) {
				return true;
			}
		}
		return false;
	}
	
}
