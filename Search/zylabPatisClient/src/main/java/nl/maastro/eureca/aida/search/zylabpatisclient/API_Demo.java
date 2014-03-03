// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.SearchResultFormatter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.Classifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.InterDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.IntraDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.HtmlFormatter;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Concepts;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Patients;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.SemanticModifiers;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedPreviousResults;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResultsMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparison;
import static nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparison.Qualifications.*;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparisonTable;

/**
 * Demostrates how to use the classes in {@link nl.maastro.eureca.aida.search.zylabpatisclient}.
 *
 * Use the following methods of the {@link API_Demo} instance to define the report structure:
 * <ul><li>{@link #addExpectedResultsColumn(ExpectedResults)};</li>
 * 		<li>{@link #addConceptSearchColumn(Concepts)}; and<li>
 *		<li>{@link #addDefinedPatients(ExpectedResults)}.</li></ul>
 * Create ExpectedResults with:
 * <ul><li>{@link #createExpectedResults(Concepts)}; or
 *		<li>{@link #readExpectedPreviousResults(Concepts)}.</li></ul>
 * Finally, store for later use with:
 * <ul><li>{@link storeResults(Concepts)}</li></ul>.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class API_Demo {

	private final Config config;
	private final Searcher searcher;
	private final SearchResultFormatter formatter;
	private final SearchResultTable resultTable;
	private final ResultComparisonTable validationComparisonTable;

	public API_Demo() throws ServiceException, IOException {
		this.config = initConfig();
		this.searcher = initSearcher(config);
		initSearchedConcepts(config, searcher);
		HtmlFormatter tmp = new HtmlFormatter();
		tmp.setShowSnippetsStrategy(HtmlFormatter.SnippetDisplayStrategy.DYNAMIC_SHOW);
		this.formatter = tmp;
		Report_Builder builder = initReport(config, searcher, Report_Builder.Purpose.VALIDATION);
		this.resultTable = builder.build_search_table();
		this.validationComparisonTable = builder.build_validation_table();
	}

	
	private static Config initConfig() {
		// Read config file
		InputStream s = API_Demo.class.getResourceAsStream("/zpsc-config.xml");
		if (s != null) {
			return Config.init(s);
			// intentionally keeping s open, since Config will read from it at a later time
		} else {
			throw new Error("Cannot read '/zpsc-config.xml'");
		}
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
				ConceptFoundStatus.UNCERTAIN,
				ConceptFoundStatus.FOUND));
		return instance;
	}


	private static Report_Builder initReport(Config config, Searcher searcher, Report_Builder.Purpose purpose)
	{
		Report_Builder builder = new Report_Builder();
		builder	.set_config(config)
				.set_purpose(purpose)
				.set_searcher(searcher)
				.use_predefined_semantic_modifiers()
				
				.add_concept(Concepts.METASTASIS)
				.add_concept(Concepts.CHEMOKUUR);

		return builder;
	}	
		
	
	
	public void writeTable() {
		Date now = new Date();
		File f = new FileNames().createHtmlResultsFile();
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

	public void setValidationQualifications(Set<ResultComparison.Qualifications> newValidationQualifications) {
		validationComparisonTable.setQualifications(newValidationQualifications);
	}

	/**
	 * Store the lucene search results per concept in a json file.
	 * 
	 * Use {@code storeResults(Concepts)} to be able to read and show the results in the next rapport with 
	 * {@link #readExpectedPreviousResults(Concepts)}.
	 */
	public void storeResults(Concepts preConstructedConcept) throws IOException {
		Concept concept = preConstructedConcept.getConcept(config);
		Iterable<SearchResult> toStore = resultTable.getColumn(concept);
		ExpectedPreviousResults resultStorer = ExpectedPreviousResults.create(concept, toStore);
		File f = new FileNames().createJsonResultsFile(concept);
		FileWriter outputFile = new FileWriter(f);
		resultStorer.writeAsJson(outputFile);
		outputFile.flush();
	}

	/**
	 * {@code main()} drives API_Demo and defines the structure of the report.
	 * 
	 * Use the following methods of the {@link API_Demo} instance to define the report structure:
	 * <ul><li>{@link #addExpectedResultsColumn(ExpectedResults)};</li>
	 * 		<li>{@link #addConceptSearchColumn(Concepts)}; and<li>
	 *		<li>{@link #addDefinedPatients(ExpectedResults)}.</li></ul>
	 * Create ExpectedResults with:
	 * <ul><li>{@link #createExpectedResults(Concepts)}; or
	 *		<li>{@link #readExpectedPreviousResults(Concepts)}.</li></ul>
	 * Finally, store for later use with:
	 * <ul><li>{@link storeResults(Concepts)}</li></ul>.
	 */
	static public void main(String[] args) throws IOException, ServiceException {
		API_Demo instance = new API_Demo();
		
		instance.setValidationQualifications(EnumSet.of(
				ACTUAL_MATCHING_EXPECTED, ACTUAL_CONTAINIG_EXPECTED_AND_OTHERS, 
				ACTUAL_DIFFERING_FROM_EXPECTED, EXTRA_ACTUAL_RESULTS, MISSING_ACTUAL_RESULTS));

		instance.storeResults(Concepts.METASTASIS);
		instance.storeResults(Concepts.CHEMOKUUR);
		
		instance.writeTable();
	}

}
