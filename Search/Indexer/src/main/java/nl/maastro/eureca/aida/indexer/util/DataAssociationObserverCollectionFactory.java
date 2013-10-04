/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.util;

import java.lang.reflect.Method;
import java.net.URL;
import nl.maastro.eureca.aida.indexer.DataAssociationObserver;

/**
 *
 * @author kasper
 */
public class DataAssociationObserverCollectionFactory<TSource> 
		implements ObserverCollectionFactory<DataAssociationObserver<TSource>, TSource> {
	private static final Method OBSERVER_METHOD;
	static {
		try {
			Class<DataAssociationObserver> type = DataAssociationObserver.class;
			OBSERVER_METHOD = type.getDeclaredMethod("dataAssociationChanged", Object.class, URL.class, URL.class);
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new Error("Error initialising OBSERVER_METHOD", ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ObserverCollection<DataAssociationObserver<TSource>, TSource> createObserverSupport(TSource source) {
		return new ObserverCollection<>(source, (Class<DataAssociationObserver<TSource>>)(Object)DataAssociationObserver.class, OBSERVER_METHOD);
	}
	
}
