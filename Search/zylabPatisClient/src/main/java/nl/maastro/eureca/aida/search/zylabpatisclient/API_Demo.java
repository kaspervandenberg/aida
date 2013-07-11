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
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.ChainedSearcher.CombinationStrategy;
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
		EXPECTED_METASTASIS {{ setConcept(Concepts.METASTASIS); }},
		METASTASIS {{ setConcept(Concepts.METASTASIS); }},
		EXPECTED_CHEMOKUUR {{ setConcept(Concepts.CHEMOKUUR); }},
		CHEMOKUUR {{ setConcept(Concepts.CHEMOKUUR); }};

		private Searcher searcher = null;
		private Concepts concept = null;
		
		public Searcher setSearcher(Searcher searcher_) {
			Searcher tmp = this.searcher;
			this.searcher = searcher_;
			return tmp;
		}

		public Searcher getSearcher() {
			return searcher;
		}

		protected Concepts setConcept(Concepts concept_) {
			Concepts tmp = this.concept;
			this.concept = concept_;
			return tmp;
		}

		public Concepts getConcept() {
			return concept;
		}
	}

	private final Config config;
	private final Searcher searcher;
	private final List<PatisNumber> patients;
	private final List<SemanticModifier> modifiers;
	private final Classifier classifier;
	private final SearchResultFormatter formatter;

	public API_Demo() {
		this.config = initConfig();
		this.searcher = initSearcher(config);
		this.patients = initPatients();
		this.modifiers = initSemanticModifiers();
		this.classifier = initClassifier();
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
			
			SearchedConcepts.EXPECTED_METASTASIS.setSearcher(
					new ChainedSearcher(CombinationStrategy.FIRST_FOUND, Arrays.asList(
						Patients.instance().getDummySearcher(),
						new DummySearcher(config.getPatients())
					)));
			SearchedConcepts.METASTASIS.setSearcher(s);
			SearchedConcepts.EXPECTED_CHEMOKUUR.setSearcher(null);
			SearchedConcepts.CHEMOKUUR.setSearcher(s);
			
			return s;
		} catch (ServiceException | IOException ex) {
			throw new Error(ex);
		}
	}

	private static List<PatisNumber> initPatients() {
		List<PatisNumber> result = new LinkedList<>(
				Patients.instance().getExpectedMetastasis().keySet());
		result.addAll(Config.instance().getPatients().keySet());

		return result;
	}

	private static List<SemanticModifier> initSemanticModifiers() {
		List<SemanticModifier> result = new ArrayList<>(SemanticModifiers.values().length + 1);
		result.add(SemanticModifier.Constants.NULL_MODIFIER);
		result.addAll(Arrays.asList(SemanticModifiers.values()));
		return result;
	}
	
	private static Classifier initClassifier() {
		Classifier instance = Classifier.instance();
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.NEGATED,
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.SUSPICION,
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new InterDocOverride(
				EligibilityClassification.UNCERTAIN,
				EligibilityClassification.NOT_ELIGIBLE));
		return instance;
	}

	private Iterable<SearchResult> searchConcept(SearchedConcepts concept) {
		Iterable<SearchResult> results = concept.getSearcher().searchForAll(
				concept.getConcept(), modifiers, patients);
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
		table.put(SearchedConcepts.CHEMOKUUR.name(),
				instance.searchConcept(SearchedConcepts.CHEMOKUUR));
//		System.out.append(SearchedConcepts.METASTASIS.getConcept().getName().toString());
		
		instance.writeTable(table);
	}

}
