/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants.SparqlQueries;
import org.openrdf.query.Query;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author kasper
 */
public class QueryCache {
	
	private class QueryProxyHandler implements InvocationHandler {
		private final SparqlQueries key;

		public QueryProxyHandler(SparqlQueries key_) throws RepositoryException {
			this.key = key_;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(CLOSABLE_CLOSE.equals(method)) {
				return doClose();
			} else {
				return invokeOnReal(method, args);
			}
		}

		private Object doClose() {
			release();
			return null;
		}

		private Object invokeOnReal(Method method, Object args[]) throws Throwable {
			Query realQuery = getRealQuery(key);
			checkMethodCallable(method, realQuery);
			return connectAndInvoke(realQuery, method, args);
		}

		private Object connectAndInvoke(Query query, Method method, Object args[]) throws Throwable {
			try {
				connectionLock.readLock().lock();
				if(connection != null && connection.isOpen()) {
					return method.invoke(query, args);
				} else {
					throw new IllegalStateException("Expecting RepossitoryConnection open");
				}
			} finally {
				connectionLock.readLock().unlock();
			}
		}

		private void checkMethodCallable(Method method, Query realQuery) {
			if(!method.getDeclaringClass().isAssignableFrom(realQuery.getClass())) {
				throw new ClassCastException(String.format(
						"Class %s has no method %s", realQuery.getClass(), method));
			}
		}
	}

	private static final long CLOSE_DELAY_MSEC = 100;
	private static final Method CLOSABLE_CLOSE;
	static {
		try {
			CLOSABLE_CLOSE = AutoClosableQuery.class.getDeclaredMethod("close");
		} catch (NoSuchMethodException ex) {
			throw new Error(ex);
		}
	}
	
	private final Repository repo;
	private RepositoryConnection connection;
	private final EnumMap<SparqlQueries, Query> preparedQueries;

	private final ReadWriteLock connectionLock;
	private final AtomicInteger users;
	private ScheduledFuture<Void> scheduledConnectionClose;
	
	private final Callable<Void> connectionCloseTask = new Callable<Void>() {
		@Override
		public Void call() throws Exception {
			if (users.get() == 0) {
				clearAndClose();
			}
			return null;
		}

		private void clearAndClose() throws RepositoryException {
			try {
				connectionLock.writeLock().lock();
				
				preparedQueries.clear();
				ensureClosedConnection();
			} finally {
				connectionLock.writeLock().unlock();
			}
		}
	};

	public QueryCache(Repository repo_) {
		this.repo = repo_;
		connection = null;
		preparedQueries = new EnumMap<>(SparqlQueries.class);
		connectionLock = new ReentrantReadWriteLock();
		users = new AtomicInteger(0);
	}

	/**
	 * Clean up to avoid resource leaks: close repository connections and shutdown scheduler
	 */
	@Override
	protected void finalize() throws Throwable {
		connectionCloseTask.call();
		super.finalize();
	}

	public AutoClosableQuery get(SparqlQueries key) throws RepositoryException {
		aquire();
		Query realQuery = getRealQuery(key);
		Class<?> interfaces[] = decorateWithAutoClosableQuery(realQuery.getClass().getInterfaces());
		
		return (AutoClosableQuery)Proxy.newProxyInstance(
				QueryCache.class.getClassLoader(), interfaces, new QueryProxyHandler(key));
	}

	/**
	 * Prevent connection from being closed
	 */
	private void aquire() throws RepositoryException {
		users.incrementAndGet();
		cancelClose();
	}

	/**
	 * Schedule close if last user
	 */
	private void release() {
		int curUsers = users.decrementAndGet();
		if(curUsers <= 0) {
			resetDelayedClose();
		}
	}

	/**
	 * If a close task is scheduled, cancel it.
	 */
	private void cancelClose() {
		if(scheduledConnectionClose != null) {
			scheduledConnectionClose.cancel(false);
			scheduledConnectionClose = null;
		}
	}
	
	/**
	 * Close repository connection after {@link #CLOSE_DELAY_MSEC}, restart the delay period if a previous 
	 * cancel task was scheduled.
	 */
	private void resetDelayedClose() {
		cancelClose();
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduledConnectionClose = scheduler.schedule(connectionCloseTask, CLOSE_DELAY_MSEC, TimeUnit.MILLISECONDS);
		scheduler.shutdown();
	}

	/**
	 * Lookup key in {@link #preparedQueries} creating it if it doesn't exist.
	 */
	private Query getRealQuery(SparqlQueries key) throws RepositoryException {
		Query result;
		if(preparedQueries.containsKey(key)) {
			result = preparedQueries.get(key);
		} else {
			result = createQuery(key);
			preparedQueries.put(key, result);
		}		
		return result;
	}

	/**
	 * Call {@link SparqlQueries#prepareQuery(org.openrdf.repository.RepositoryConnection) 
	 */
	private Query createQuery(SparqlQueries key) throws RepositoryException {
		ensureOpenConnection();
		return key.prepareQuery(connection);
	}

	/**
	 * Ensure {@link #connection} is available
	 */
	private void ensureOpenConnection() throws RepositoryException {
		try {
			connectionLock.writeLock().lock();
			if(connection == null || !connection.isOpen()) {
				connection = repo.getConnection();
			}
		} finally {
			connectionLock.writeLock().unlock();
		}
	}

	private void ensureClosedConnection() throws RepositoryException {
		try {
			connectionLock.writeLock().lock();
			if(connection != null && connection.isOpen()) {
				connection.close();
				connection = null;
			}
		} finally {
			connectionLock.writeLock().unlock();
		}
	}

	private Class<?>[] decorateWithAutoClosableQuery(Class<?>[] existingInterfaces) {
		Set<Class<?>> combinedInterfaces = new HashSet<>(existingInterfaces.length + 1);
		combinedInterfaces.add(AutoClosableQuery.class);
		combinedInterfaces.addAll(Arrays.asList(existingInterfaces));
		
		Class<?>[] result = new Class<?>[combinedInterfaces.size()];
		result = combinedInterfaces.toArray(result);
		return result;
	}
	
}
