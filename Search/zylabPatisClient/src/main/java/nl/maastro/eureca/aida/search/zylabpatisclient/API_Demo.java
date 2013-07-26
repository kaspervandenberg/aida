/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.BufferedOutputStream;
import java.io.File;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.SearchResultFormatter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.Classifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.InterDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.IntraDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.HtmlFormatter;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Concepts;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Patients;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.SemanticModifiers;

/**
 *
 * @author kasper2
 */
public class API_Demo {
	private enum SearchedConcepts {
		EXPECTED_METASTASIS(Concepts.METASTASIS, Strategy.SIMULATED),
		METASTASIS(Concepts.METASTASIS, Strategy.REAL),
		EXPECTED_CHEMOKUUR(Concepts.CHEMOKUUR, Strategy.SIMULATED),
		CHEMOKUUR(Concepts.CHEMOKUUR, Strategy.REAL);

		private enum Strategy {
			REAL {
				public void setSearcher(SearchedConcepts container, Searcher searcher_) {
					container.setSearcher_real(searcher_);
				}

				@Override
				public void addExpected(SearchedConcepts container, Map<PatisNumber, EligibilityClassification> expected_, boolean preserveExisting) {
					throw new IllegalStateException(
							"Cannot set expected; non-simulated SearchConcepts "
							+ "use a real searcher to get their results.");
				}
			},

			SIMULATED {
				@Override
				public void setSearcher(SearchedConcepts container, Searcher searcher_) {
					throw new IllegalStateException(
							"Cannot set searcher; simulated SearchedConcepts "
							+ "use a DummySearcher.");
				}

				@Override
				public void addExpected(SearchedConcepts container, Map<PatisNumber, EligibilityClassification> expected_, boolean preserveExisting) {
					container.addExpected_sim(expected_, preserveExisting);
				}
				
			};

			public abstract void setSearcher(SearchedConcepts container, Searcher searcher_); 
			public abstract void addExpected(SearchedConcepts container, Map<PatisNumber, EligibilityClassification> expected_, boolean preserveExisting);
		}

		private final Concepts concept;
		private final Strategy strat;
		private final LinkedHashMap<PatisNumber, EligibilityClassification> expected =
				new LinkedHashMap<>();
		private Searcher searcher = null;

		private SearchedConcepts(Concepts concept_, Strategy strat_) {
			this.strat = strat_;
			this.concept = concept_;
		}

		public static void init(Config config, Searcher defaultSearcher) {
			for (SearchedConcepts e : SearchedConcepts.values()) {
				if(e.isSimulated()) {
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


		public void addExpected(Map<PatisNumber,
				EligibilityClassification> expected_, boolean preserveExisting) {
			strat.addExpected(this, expected_, preserveExisting);
		}

		private void addExpected_sim(
				Map<PatisNumber, EligibilityClassification> expected_, boolean preserveExisting) {
			LinkedHashMap<PatisNumber, EligibilityClassification> toAdd =
					new LinkedHashMap<>(expected_);
			if(preserveExisting) {
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

	private final Config config;
	private final Searcher searcher;
	private final LinkedHashSet<PatisNumber> patients;
	private final List<SemanticModifier> modifiers;
	private final Classifier classifier;
	private final SearchResultFormatter formatter;

	public API_Demo() {
		this.config = initConfig();
		this.searcher = initSearcher(config);
		initSearchedConcepts(config, searcher);
		this.patients = initPatients();
		this.modifiers = initSemanticModifiers(config);
		this.classifier = initClassifier(config);
		HtmlFormatter tmp = new HtmlFormatter();
		tmp.setShowSnippetsStrategy(HtmlFormatter.SnippetDisplayStrategy.DYNAMIC_SHOW);
		this.formatter = tmp;
	}

	
	private static Config initConfig() {
		// Read config file
		InputStream s = API_Demo.class.getResourceAsStream("/zpsc-config.xml");
		return Config.init(s);
		// intentionally keeping s open, since Config will read from it at a later time
	}

	private static Searcher initSearcher(Config config) {
		// Use config to initialise a searcher
		try {
			Searcher s = config.getSearcher();
			return s;
		} catch (ServiceException | IOException ex) {
			throw new Error(ex);
		}
	}

	private static void initSearchedConcepts(Config config, Searcher searcher) {
		SearchedConcepts.EXPECTED_METASTASIS.addExpected(Patients.instance().getExpectedMetastasis(), false);
		SearchedConcepts.init(config, searcher);
	}

	private static LinkedHashSet<PatisNumber> initPatients() {
		LinkedHashSet<PatisNumber> result = new LinkedHashSet<>();
		for (SearchedConcepts e : SearchedConcepts.values()) {
			result.addAll(e.getPatients());
		}

		return result;
	}

	private static List<SemanticModifier> initSemanticModifiers(Config config) {
		List<SemanticModifier> result = new ArrayList<>(SemanticModifiers.values().length + 1);
		result.add(SemanticModifier.Constants.NULL_MODIFIER);
		for (SemanticModifiers semmod : SemanticModifiers.values()) {
			result.add(semmod.getModifier(config));
		}
		return result;
	}
	
	private static Classifier initClassifier(Config config) {
		Classifier instance = Classifier.instance();
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.NEGATED.getModifier(config),
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.SUSPICION.getModifier(config),
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new InterDocOverride(
				EligibilityClassification.UNCERTAIN,
				EligibilityClassification.NOT_ELIGIBLE));
		return instance;
	}

	private Iterable<SearchResult> searchConcept(SearchedConcepts concept) {
		Iterable<SearchResult> results = concept.getSearcher().searchForAll(
				concept.getConcept(config), modifiers, patients);
		List<SearchResult> conclusions = new LinkedList<>();
		for (SearchResult searchResult : results) {
			conclusions.add(classifier.resolve(searchResult));
		}
		return conclusions;
	}
	
	public void writeTable(LinkedHashMap<String, Iterable<SearchResult>> results) {
		Date now = new Date();
		File f = new File(String.format("results-%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS.html", now));
		try {
			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(
					new FileOutputStream(f)), StandardCharsets.UTF_8);
			HtmlFormatter.writeDocStart(out,
					String.format("Results of %1$tT (on %1$ta %1$te %1$tb)\n", now));
			formatter.writeTable(out, results);
			HtmlFormatter.writeDocEnd(out);
			out.close();
		} catch (IOException ex) {
			throw new Error(ex);
		}
				
	}
	
	static public void main(String[] args) {
		API_Demo instance = new API_Demo();
		LinkedHashMap<String, Iterable<SearchResult>> table = new LinkedHashMap<>();
		table.put(SearchedConcepts.EXPECTED_METASTASIS.name(),
				instance.searchConcept(SearchedConcepts.EXPECTED_METASTASIS));
		table.put(SearchedConcepts.METASTASIS.name(), 
				instance.searchConcept(SearchedConcepts.METASTASIS));
		table.put(SearchedConcepts.EXPECTED_CHEMOKUUR.name(),
				instance.searchConcept(SearchedConcepts.EXPECTED_CHEMOKUUR));
		table.put(SearchedConcepts.CHEMOKUUR.name(),
				instance.searchConcept(SearchedConcepts.CHEMOKUUR));
//		System.out.append(SearchedConcepts.METASTASIS.getConcept().getName().toString());
		
		instance.writeTable(table);
	}

}
