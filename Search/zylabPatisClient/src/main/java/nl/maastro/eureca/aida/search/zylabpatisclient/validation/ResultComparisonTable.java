// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResultTable;
import checkers.nullness.quals.Nullable;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ResultComparisonTable {
	private class ActualResultLookupIterator implements Iterator<SearchResult> {
		private final Concept concept;
		private final Iterator<PatisNumber> patientIter;
		private @Nullable SearchResult peekedValue = null;

		public ActualResultLookupIterator(ExpectedResults expected) {
			this.concept = expected.getAboutConcept();
			this.patientIter = expected.getDefinedPatients().iterator();
		}

		@Override
		public boolean hasNext() {
			peek();
			return peekedValue != null;
		}

		@Override
		public SearchResult next() {
			peek();
			if(peekedValue != null) {
				SearchResult result = peekedValue;
				peekedValue = null;
				return result;
			} else {
				throw new NoSuchElementException("No more elements");
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported.");
		}

		private void peek() {
			while(patientIter.hasNext() && peekedValue == null) {
				PatisNumber patient = patientIter.next();
				if(searchResultSource.containsPatient(patient) && searchResultSource.containsColumn(concept)) {
					peekedValue = searchResultSource.getCell(patient, concept);
				}
			} 
		}
	}
	
	private class ExpectedToComparisonConversionIterator implements Iterator<ResultComparison> {
		private final Iterator<ExpectedResults> iter;

		public ExpectedToComparisonConversionIterator() {
			iter = expected.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public ResultComparison next() {
			return createResultComparison(iter.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported.");
		}
	}
	
	private final SearchResultTable searchResultSource;
	private final List<ExpectedResults> expected;
	private Iterable<ResultComparison.Qualifications> qualifications;

	public ResultComparisonTable(SearchResultTable searchResultSource_) {
		this.searchResultSource = searchResultSource_;
		this.expected = new LinkedList<>();
		this.qualifications = Collections.emptyList();
	}
	
	public void addExpectedResult(ExpectedResults expected) {
		this.expected.add(expected);
	}

	public Iterable<ResultComparison> getComparisons() {
		return new Iterable<ResultComparison>() {
			@Override
			public Iterator<ResultComparison> iterator() {
				return new ExpectedToComparisonConversionIterator();
			}
		};
	}

	public Iterable<ResultComparison.Qualifications> setQualifications(
			Iterable<ResultComparison.Qualifications> newValue) {
		Iterable<ResultComparison.Qualifications> oldValue = qualifications;
		qualifications = newValue;
		return oldValue;
	}
	
	public Iterable<ResultComparison.Qualifications> getQualifications() {
		return qualifications;
	}

	private ResultComparison createResultComparison(ExpectedResults expected) {
		return ResultComparison.compare(retrieveActualResults(expected), expected);
	}

	private Iterable<SearchResult> retrieveActualResults(final ExpectedResults expected) {
		return new Iterable<SearchResult>() {

			@Override
			public Iterator<SearchResult> iterator() {
				return new ActualResultLookupIterator(expected);
			}
		};
	}
}
