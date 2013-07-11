// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;

/**
 * Pass a {@link Query} through a chain of {@link Searcher}s return the results
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ChainedSearcher extends SearcherBase {
	public enum CombinationStrategy {
		FIRST_FOUND {
			@Override
			ResultCombination combine(ResultCombination previous, SearchResult result) {
				return first(result);
			}

			@Override
			ResultCombination first(SearchResult result) {
				boolean doContinue =
						(result.equals(SearchResultImpl.NO_RESULT())) ||
						(Collections.singleton(EligibilityClassification.UNKNOWN)
							.containsAll(result.getClassification())); 
				return new ResultCombination(doContinue, result);
			}
		},
		
		COMBINE_ALL {
			@Override
			ResultCombination combine(ResultCombination previous, SearchResult result) {
				boolean doContinue = true;
				return new ResultCombination(doContinue, 
						SearchResultImpl.combine(previous.result, result));
			}

			@Override
			ResultCombination first(SearchResult result) {
				return new ResultCombination(true, result);
			}
		};

		abstract ResultCombination combine(ResultCombination previous,
				SearchResult result);
		
		abstract ResultCombination first(SearchResult result);
	}

	private static class ResultCombination {
		public final boolean doContinue;
		public final SearchResult result;

		public ResultCombination(boolean doContinue, SearchResult result) {
			this.doContinue = doContinue;
			this.result = result;
		}
	}
	
	private final Deque<Searcher> delegates;
	private CombinationStrategy strategy;

	public ChainedSearcher() {
		super("", 0);
		delegates = new LinkedList<>();
	}

	public ChainedSearcher(Collection<? extends Searcher> chain) {
		super("", 0);
		delegates = new LinkedList<>(chain);
	}
	
	@Override
	public SearchResult searchFor(Query query, Iterable<SemanticModifier> modifiers, PatisNumber patient) {
		Iterator<Searcher> i = delegates.iterator();
		if(i.hasNext()) {
			ResultCombination tmp = strategy.first(i.next().searchFor(query, modifiers, patient));
			while(tmp.doContinue && i.hasNext()) {
				tmp = strategy.combine(tmp, i.next().searchFor(query, modifiers, patient));
			}
			return tmp.result;
		} else {
			return SearchResultImpl.NO_RESULT();
		}
	}
	
	public void addFirst(Searcher s) {
		delegates.addFirst(s);
	}

	public void addLast(Searcher s) {
		delegates.addLast(s);
	}
}
