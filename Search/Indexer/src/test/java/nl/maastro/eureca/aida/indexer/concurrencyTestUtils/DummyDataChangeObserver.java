/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.net.URL;
import nl.maastro.eureca.aida.indexer.DataAssociationObserver;

/**
 *
 * @author kasper
 */
public class DummyDataChangeObserver<T> extends DummyObserverBase<UrlChangeEvent<T>> implements DataAssociationObserver<T> {

	@Override
	public void dataAssociationChanged(T source, URL oldValue, URL currentValue) {
		UrlChangeEvent<T> event = UrlChangeEvent.register(source, oldValue, currentValue);
		add(event);
	}
	
}
