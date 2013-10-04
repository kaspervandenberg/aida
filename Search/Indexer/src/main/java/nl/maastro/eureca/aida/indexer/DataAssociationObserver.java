/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer;

import java.lang.reflect.Method;
import java.net.URL;
import nl.maastro.eureca.aida.indexer.util.ObserverCollection;

/**
 * Observer pattern, interface observers have to implement so that a ZylabDocument can notify them when {@link #getDataUrl()}
 * changes.
 *
 * @return
 */
@ObserverCollection.Observer
public interface DataAssociationObserver<T> {

	@ObserverCollection.NotifyMethod
	public void dataAssociationChanged(T source, URL oldValue, URL currentValue);
}
