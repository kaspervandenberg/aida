/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.Iterator;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 *
 * @author kasper
 */
class ExpectedResultsToSearchResultsConvertor implements Iterable<SearchResult> {
	final ExpectedResults expected;

	public ExpectedResultsToSearchResultsConvertor(ExpectedResults expected_) {
		this.expected = expected_;
	}

	@Override
	public Iterator<SearchResult> iterator() {
		return new Iterator<SearchResult>() {
			private final Iterator<PatisNumber> delegate = expected.getDefinedPatients().iterator();

			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			@Override
			public SearchResult next() {
				return expected.createExpectedResult(delegate.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		};
	}
	
}
