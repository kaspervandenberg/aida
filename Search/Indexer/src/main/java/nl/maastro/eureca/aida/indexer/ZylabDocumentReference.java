// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabDocumentReference implements ZylabDocument {
	private ZylabDocument referenced;
	private final ReadWriteLock referenceLock;

	public ZylabDocumentReference(ZylabDocument referenced_) {
		this.referenced = referenced_;
		this.referenceLock = new ReentrantReadWriteLock();
	}

	@Override
	public URL getDataUrl() {
		try {
			referenceLock.readLock().lock();
			return referenced.getDataUrl();
		} finally {
			referenceLock.readLock().unlock();
		}
	}

	@Override
	public List<IndexableField> getFields() {
		try {
			referenceLock.readLock().lock();
			return referenced.getFields();
		} finally {
			referenceLock.readLock().unlock();
		}
	}

	@Override
	public Term getId() {
		try {
			referenceLock.readLock().lock();
			return referenced.getId();
		} finally {
			referenceLock.readLock().unlock();
		}
	}

	@Override
	public void initDataUrl(URL value) throws IllegalStateException {
		try {
			referenceLock.readLock().lock();
			referenced.initDataUrl(value);
		} finally {
			referenceLock.readLock().unlock();
		}
	}

	@Override
	public void merge(nl.maastro.eureca.aida.indexer.ZylabDocument other) throws IllegalArgumentException {
		try {
			referenceLock.readLock().lock();
			referenced.merge(other);
		} finally {
			referenceLock.readLock().unlock();
		}
	}

	@Override
	public void setField(String fieldName, String value) {
		try {
			referenceLock.readLock().lock();
			referenced.setField(fieldName, value);
		} finally {
			referenceLock.readLock().unlock();
		}
	}

	@Override
	public void setField(FieldsToIndex field, String value) {
		try {
			referenceLock.readLock().lock();
			referenced.setField(field, value);
		} finally {
			referenceLock.readLock().unlock();
		}
	}

	@Override
	public void setField(FieldsToIndex field, Date value) {
		try {
			referenceLock.readLock().lock();
			referenced.setField(field, value);
		} finally {
			referenceLock.readLock().unlock();
		}
	}
	
	@Override
	public void subscribe(DataAssociationObserver<ZylabDocument> observer) {
		referenced.subscribe(observer);
	}

	public void switchToAndMerge(ZylabDocument newReferenced) {
		try {
			referenceLock.writeLock().lock();
			ZylabDocument currentRefference = this.referenced;
			newReferenced.merge(currentRefference);
			switchTo(newReferenced);
		} finally {
			referenceLock.writeLock().unlock();
		}
	};

	public void switchTo(ZylabDocument newReferenced) {
		try {
			referenceLock.writeLock().lock();
			this.referenced = newReferenced;
		} finally {
			referenceLock.writeLock().unlock();
		}
	}
}
