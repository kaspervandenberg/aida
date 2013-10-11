// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.matchers;

import java.util.Collection;
import java.util.Iterator;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class HasAllExpectedResults implements Iterable<Matcher<? super Iterable<SearchResult>>> {
	private class Iter implements Iterator<Matcher<? super Iterable<SearchResult>>> {
		private final Iterator<PatisNumber> delegate;

		public Iter(Iterator<PatisNumber> delegate_) {
			this.delegate = delegate_;
		}
		
		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public Matcher<? super Iterable<SearchResult>> next() {
			return Matchers.hasItem(SearchResultsMatchers.withPatisNumer(Matchers.equalTo(delegate.next())));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}
	
	private final ExpectedResults expectation;

	public HasAllExpectedResults(ExpectedResults expectation_) {
		this.expectation = expectation_;
	}

	@Override
	public Iterator<Matcher<? super Iterable<SearchResult>>> iterator() {
		return new Iter(expectation.getDefinedPatients().iterator());
	}
	
}
