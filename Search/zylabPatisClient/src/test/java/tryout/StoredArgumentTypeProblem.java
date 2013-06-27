package tryout;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kasper
 */
	public class StoredArgumentTypeProblem {
		static class Operation<T> {
			T apply(T arg) {
				return arg;
			}
		}

		static class OperationContainer {
			private Map<Class<?>, Operation<?>> storedOperations = new HashMap<>();
			public <T> void put(Class<T> argType, Operation<T> opp) {
				storedOperations.put(argType, opp);
			}

			public Class<?> getSomeStoredKey() {
				return storedOperations.keySet().iterator().next();
			}

			public <T> Operation<T> get(Class<T> type) {
				// unchecked cast, but should work given restrictions on put.
				return (Operation<T>)storedOperations.get(type);	
			}
		}

		public void test() {
			OperationContainer container = new OperationContainer();
			container.put(Integer.class, new Operation<Integer>());
			container.get(Integer.class).apply(new Integer(1234));
			Class<?> keyType = container.getSomeStoredKey();

			// ERROR: method apply in Operation<T> cannot be applied to given types
			// container.get(keyType).apply(keyType.cast(new Integer(5678)));

			Operation<?> storedOpp = container.get(keyType);
			try {
				storedOpp.getClass().getMethod("apply", keyType).invoke(storedOpp, keyType.cast(new Integer(5678)));
			} catch (IllegalAccessException | IllegalArgumentException |
					InvocationTargetException | NoSuchMethodException ex) {
				throw new Error(ex);
			}
		}

		private <T> void apply(
				OperationContainer cont,
				Class<T> keyType,
				Object argument) {
			T castArgument = keyType.cast(argument);
			Operation<T> opp = cont.get(keyType);
			opp.apply(castArgument);
		}
	}
