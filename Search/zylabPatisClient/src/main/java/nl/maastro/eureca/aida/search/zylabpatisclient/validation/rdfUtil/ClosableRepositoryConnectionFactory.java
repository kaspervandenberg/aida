/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author kasper
 */
public class ClosableRepositoryConnectionFactory {

	private static class Handler implements InvocationHandler {
		private final RepositoryConnection inner;
		
		public Handler(RepositoryConnection inner_) {
			this.inner = inner_;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(inner, args);
		}
	}


	public static ClosableRepositoryConnection decorate(RepositoryConnection inner) {
		Handler h = new Handler(inner);
		Object obj_proxy = Proxy.newProxyInstance(
				ClosableRepositoryConnectionFactory.class.getClassLoader(),
				new Class<?>[] { ClosableRepositoryConnection.class, RepositoryConnection.class, Closeable.class },
				h);
		ClosableRepositoryConnection proxy = (ClosableRepositoryConnection)obj_proxy;
		return proxy;
	}
}
