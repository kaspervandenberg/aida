/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Concepts;

/**
 *
 * @author kasper
 */
public enum SearchedConcepts {
	EXPECTED_METASTASIS(Concepts.METASTASIS, Strategy.SIMULATED),
	METASTASIS(Concepts.METASTASIS, Strategy.REAL),
	EXPECTED_CHEMOKUUR(Concepts.CHEMOKUUR, Strategy.SIMULATED),
	CHEMOKUUR(Concepts.CHEMOKUUR, Strategy.REAL);

	private enum Strategy {

		REAL {
			@Override
			public void setSearcher(SearchedConcepts container, Searcher searcher_) {
				container.setSearcher_real(searcher_);
			}

			@Override
			public void addExpected(SearchedConcepts container, Map<PatisNumber, ConceptFoundStatus> expected_, boolean preserveExisting) {
				throw new IllegalStateException("Cannot set expected; non-simulated SearchConcepts " + "use a real searcher to get their results.");
			}
		}, SIMULATED {
			@Override
			public void setSearcher(SearchedConcepts container, Searcher searcher_) {
				throw new IllegalStateException("Cannot set searcher; simulated SearchedConcepts " + "use a DummySearcher.");
			}

			@Override
			public void addExpected(SearchedConcepts container, Map<PatisNumber, ConceptFoundStatus> expected_, boolean preserveExisting) {
				container.addExpected_sim(expected_, preserveExisting);
			}
		};

		public abstract void setSearcher(SearchedConcepts container, Searcher searcher_);

		public abstract void addExpected(SearchedConcepts container, Map<PatisNumber, ConceptFoundStatus> expected_, boolean preserveExisting);
	}
	private final Concepts concept;
	private final Strategy strat;
	private final LinkedHashMap<PatisNumber, ConceptFoundStatus> expected = new LinkedHashMap<>();
	private Searcher searcher = null;

	private SearchedConcepts(Concepts concept_, Strategy strat_) {
		this.strat = strat_;
		this.concept = concept_;
	}

	public static void init(Config config, Searcher defaultSearcher) {
		for (SearchedConcepts e : SearchedConcepts.values()) {
			if (e.isSimulated()) {
				e.addExpected(config.getPatients(e.getConcept(config).getName()), true);
			} else {
				e.setSearcher(defaultSearcher);
			}
		}
	}

	public void setSearcher(Searcher searcher_) {
		strat.setSearcher(this, searcher_);
	}

	private void setSearcher_real(Searcher searcher_) {
		this.searcher = searcher_;
	}

	public void addExpected(Map<PatisNumber, ConceptFoundStatus> expected_, boolean preserveExisting) {
		strat.addExpected(this, expected_, preserveExisting);
	}

	private void addExpected_sim(Map<PatisNumber, ConceptFoundStatus> expected_, boolean preserveExisting) {
		LinkedHashMap<PatisNumber, ConceptFoundStatus> toAdd = new LinkedHashMap<>(expected_);
		if (preserveExisting) {
			toAdd.keySet().removeAll(this.expected.keySet());
		}
		this.expected.putAll(toAdd);
		this.searcher = new DummySearcher(this.expected);
	}

	public boolean isSimulated() {
		return strat.equals(Strategy.SIMULATED);
	}

	public Searcher getSearcher() {
		return searcher;
	}

	public Set<PatisNumber> getPatients() {
		return expected.keySet();
	}

	public Concept getConcept(Config config) {
		return concept.getConcept(config);
	}
	
}
