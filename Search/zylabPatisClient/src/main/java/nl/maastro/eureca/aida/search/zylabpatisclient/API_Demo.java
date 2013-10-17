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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResultsMap;

/**
 *
 * @author kasper2
 */
public class API_Demo {

	private final Config config;
	private final Searcher searcher;
	private final LinkedHashSet<PatisNumber> patients;
	private final List<SemanticModifier> modifiers;
	private final Classifier classifier;
	private final SearchResultFormatter formatter;
	private final SearchResultTable table;

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
		this.table = new SearchResultTable(searcher);
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
				SemanticModifiers.NEGATED_PREFIX.getModifier(config),
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.SUSPICION_PREFIX.getModifier(config),
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.SUSPICION_ANY_ORDER.getModifier(config),
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
	
	public void writeTable() {
		Date now = new Date();
		File f = new File(String.format("results-%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS.html", now));
		try {
			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(
					new FileOutputStream(f)), StandardCharsets.UTF_8);
			HtmlFormatter.writeDocStart(out,
					String.format("Results of %1$tT (on %1$ta %1$te %1$tb)\n", now));
			formatter.writeTable(out, table);
			HtmlFormatter.writeDocEnd(out);
			out.close();
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	public ExpectedResults createExpectedResults(Concepts predefinedConcept) {
		Concept concept = predefinedConcept.getConcept(config);
		Map<PatisNumber, EligibilityClassification> expectedClassifications = config.getPatients(concept.getName());
		
		return ExpectedResultsMap.createWrapper(concept, expectedClassifications);
	}

	public void addExpectedResultsColumn(ExpectedResults newColumn) {
		table.addExpectedResultsColumn(newColumn);
	}

	public void addDefinedPatients(ExpectedResults patientSource) {
		table.addAll(patientSource.getDefinedPatients());
	}

	public void addConceptSearchColumn(Concepts concept) {
		table.addConceptSearchColumn(concept.getConcept(config), modifiers);
	}

	
	static public void main(String[] args) {
		API_Demo instance = new API_Demo();
		
		ExpectedResults metastasisValidation = instance.createExpectedResults(Concepts.METASTASIS);
		instance.addExpectedResultsColumn(metastasisValidation);
		instance.addDefinedPatients(metastasisValidation);
		
		instance.addConceptSearchColumn(Concepts.METASTASIS);

		ExpectedResults chemokuurValidation = instance.createExpectedResults(Concepts.CHEMOKUUR);
		instance.addExpectedResultsColumn(chemokuurValidation);
		instance.addDefinedPatients(chemokuurValidation);

		instance.addConceptSearchColumn(Concepts.CHEMOKUUR);
		
		instance.writeTable();
	}

}
