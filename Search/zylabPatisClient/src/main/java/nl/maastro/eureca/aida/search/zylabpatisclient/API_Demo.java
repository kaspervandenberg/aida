// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

/*>>>import checkers.initialization.quals.UnderInitialization; */
import java.io.BufferedOutputStream;
import java.io.File;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.SearchResultFormatter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.Classifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.InterDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.IntraDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.CommandLineParser;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.HtmlFormatter;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Concepts;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Patients;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.SemanticModifiers;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedPreviousResults;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import static nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparison.Qualifications.*;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparisonTable;
import org.apache.commons.cli.ParseException;

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
	private class ResultTableContainer {
		private final SearchResultTable resultTable;

		public ResultTableContainer(ReportBuilder builder)
				throws ServiceException, IOException
		{
			this.resultTable = builder.buildSearchTable();
		}


		public void write(Appendable target, SearchResultFormatter formatter)
				throws IOException
		{
			formatter.writeTable(target, resultTable);
		}


		public Iterable<SearchResult> getColumn(Concept concept)
		{
			return resultTable.getColumn(concept);
		}
		

		public void addRows(Collection<PatisNumber> patients)
		{
			resultTable.addAll(patients);
		}
	}

	private class ValidationResultTableContainer extends ResultTableContainer {
		private final ResultComparisonTable validationComparisonTable;
		
		public ValidationResultTableContainer(ReportBuilder builder)
				throws ServiceException, IllegalStateException, IOException
		{
			super(builder);
			validationComparisonTable = builder.buildValidationTable();
			
			validationComparisonTable.setQualifications(EnumSet.of(
					ACTUAL_MATCHING_EXPECTED, ACTUAL_CONTAINIG_EXPECTED_AND_OTHERS, 
					ACTUAL_DIFFERING_FROM_EXPECTED, EXTRA_ACTUAL_RESULTS, MISSING_ACTUAL_RESULTS));
		}


		@Override
		public void write(Appendable target, SearchResultFormatter formatter) throws IOException
		{
			if (formatter instanceof HtmlFormatter) {
				((HtmlFormatter)formatter).writeValidationCounts(target, validationComparisonTable);
			}
			super.write(target, formatter);
		}
	}

	private final Config config;
	private final Searcher searcher;
	private final SearchResultFormatter formatter;
	private final ReportBuilder.Purpose reportPurpose;
	private final ResultTableContainer tables;

	public API_Demo(CommandLineParser commandline) throws ServiceException, IOException {
		this.config = initConfig(commandline);
		this.searcher = initSearcher(config);
		HtmlFormatter tmp = new HtmlFormatter();
		tmp.setShowSnippetsStrategy(
				tmp.createSnippetStrategy(HtmlFormatter.SnippetDisplayStrategy.BUFFERED_SHOW));
		this.formatter = tmp;
		reportPurpose = commandline.getReportPurpose();

		initSearchedConcepts(config, searcher, reportPurpose);
		ReportBuilder builder = initReport(config, searcher, reportPurpose);
		
		if (reportPurpose.equals(ReportBuilder.Purpose.VALIDATION)) {
			this.tables = new ValidationResultTableContainer(builder);
		} else {
			this.tables = new ResultTableContainer(builder);
			
			@SuppressWarnings("unchecked")	// Up–down cast to remove the checker framework "@KeyFor"-annotation; as per http://stackoverflow.com/a/7505867/814206
			final Set<PatisNumber> dummyPatients = (Set<PatisNumber>)(Object)Patients.instance().getExpectedMetastasis().keySet();
			this.tables.addRows(dummyPatients);
		}
	}

	
	private static Config initConfig(CommandLineParser commandline_) {
		// Read config file
		InputStream s = API_Demo.class.getResourceAsStream("/zpsc-config.xml");
		if (s != null) {
			return Config.init(s, commandline_);
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

	private static void initSearchedConcepts(Config config, Searcher searcher, ReportBuilder.Purpose reportPurpose) {
		if (reportPurpose.equals(ReportBuilder.Purpose.VALIDATION)) {
			SearchedConcepts.EXPECTED_METASTASIS.addExpected(Patients.instance().getExpectedMetastasis(), false);
		}
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


	private static ReportBuilder initReport(Config config, Searcher searcher, ReportBuilder.Purpose purpose)
	{
		ReportBuilder builder = new ReportBuilder();
		builder	.setConfig(config)
				.setPurpose(purpose)
				.setSearcher(searcher)
				.usePredefinedSemanticModifiers()
				
				.addConcept(Concepts.METASTASIS)
				.addConcept(Concepts.CHEMOKUUR)
				
				.addPatients(config.getPatients().getPatients());

		return builder;
	}	

	public void writeTable() {
		Date now = new Date();
		File f = config.getResultsDirectory().createHtmlResultsFile();
		try (OutputStreamWriter out = new OutputStreamWriter(
					new BufferedOutputStream(
						new FileOutputStream(f)), StandardCharsets.UTF_8)) {
			HtmlFormatter.writeDocStart(out,
					String.format("Results of %1$tT (on %1$ta %1$te %1$tb)\n", now));
			tables.write(out, formatter);
			HtmlFormatter.writeDocEnd(out);
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Store the lucene search results per concept in a json file.
	 * 
	 * Use {@code storeResults(Concepts)} to be able to read and show the results in the next rapport with 
	 * {@link #readExpectedPreviousResults(Concepts)}.
	 */
	public void storeResults(Concepts preConstructedConcept) throws IOException {
		Concept concept = preConstructedConcept.getConcept(config);
		Iterable<SearchResult> toStore = tables.getColumn(concept);
		ExpectedPreviousResults resultStorer = ExpectedPreviousResults.create(concept, toStore);
		File f = config.getResultsDirectory().createJsonResultsFile(concept);
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
	static public void main(String[] args) throws IOException, ServiceException, ParseException {
		CommandLineParser commandline = new CommandLineParser(args);
		if (!commandline.isHelpRequested()) {
			API_Demo instance = new API_Demo(commandline);
			
			instance.storeResults(Concepts.METASTASIS);
			instance.storeResults(Concepts.CHEMOKUUR);
			
			instance.writeTable();
		} else {
			commandline.printUseage();
		}
	}

}
