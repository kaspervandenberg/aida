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
import java.util.EnumSet;
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
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResultsMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparison;
import static nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparison.Qualifications.*;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparisonTable;

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
	private final SearchResultTable resultTable;
	private final ResultComparisonTable validationComparisonTable;

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
		this.resultTable = new SearchResultTable(searcher);
		this.validationComparisonTable = new ResultComparisonTable(resultTable);
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
			HtmlFormatter.writeValidationCounts(out, validationComparisonTable);
			formatter.writeTable(out, resultTable);
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
		resultTable.addExpectedResultsColumn(newColumn);
		validationComparisonTable.addExpectedResult(newColumn);
	}

	public void addDefinedPatients(ExpectedResults patientSource) {
		resultTable.addAll(patientSource.getDefinedPatients());
	}

	public void addConceptSearchColumn(Concepts concept) {
		resultTable.addConceptSearchColumn(concept.getConcept(config), modifiers);
	}

	public void setValidationQualifications(Set<ResultComparison.Qualifications> newValidationQualifications) {
		validationComparisonTable.setQualifications(newValidationQualifications);
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
		
		instance.setValidationQualifications(EnumSet.of(
				ACTUAL_MATCHING_EXPECTED, ACTUAL_CONTAINIG_EXPECTED_AND_OTHERS, 
				ACTUAL_DIFFERING_FROM_EXPECTED, EXTRA_ACTUAL_RESULTS, MISSING_ACTUAL_RESULTS));

		instance.writeTable();
	}

}
