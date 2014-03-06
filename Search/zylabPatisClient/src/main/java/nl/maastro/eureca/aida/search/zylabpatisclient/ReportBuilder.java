// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;
import checkers.nullness.quals.RequiresNonNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.Concepts;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.SemanticModifiers;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedPreviousResults;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResultsMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparisonTable;

/**
 * Build search and validation reports.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ReportBuilder {
	public enum Purpose {
		VALIDATION,
		PATIENT_INFO	
	}


	private static class TableUnderConstruction {
		protected final Config configContext;
		public final SearchResultTable searchResults;

		public TableUnderConstruction(
				Config configContext_,
				Searcher searcher_)
		{
			this.configContext = configContext_;
			this.searchResults = new SearchResultTable(searcher_);
		}


		public void addConceptColumns(
				Concepts predefinedConcept,
				Iterable<SemanticModifier> semanticModifiersToApply)
		{
			Concept concept = getConcept(predefinedConcept);
			addConceptSearchColumn(concept, semanticModifiersToApply);
		}

		
		/**
		 * Add a column containing results of searching for a concept.
		 */
		private void addConceptSearchColumn(
				Concept concept,
				Iterable<SemanticModifier> semanticModifiersToApply)
		{
			searchResults.addConceptSearchColumn(concept, semanticModifiersToApply);
		}



		protected Concept getConcept(Concepts predefinedConcept)
		{
			return predefinedConcept.getConcept(configContext);
		}
	}


	private static class ValidationAndSearchtableUnderconstruction extends TableUnderConstruction {
		public final ResultComparisonTable validation;

		public ValidationAndSearchtableUnderconstruction(
				Config configContext_,
				Searcher searcher_)
		{
			super (configContext_, searcher_);
			this.validation = new ResultComparisonTable(searchResults);
		}
		

		@Override
		public void addConceptColumns(
				Concepts predefinedConcept,
				Iterable<SemanticModifier> semanticModifiersToApply)
		{
			addValidationSources(predefinedConcept);
			super.addConceptColumns(predefinedConcept, semanticModifiersToApply);
		}
		
		
		private void addValidationSources(
				Concepts predefinedConcept)
		{
			addExternalValidationSource(predefinedConcept);
			tryAddPreviousResults(predefinedConcept);
		}


		private void addExternalValidationSource(
				Concepts predefinedConcept)
		{
			ExpectedResults exp = createExpectedResults(predefinedConcept);
			addExpectedResultsColumn(exp);
			addDefinedPatientsRows(exp);
		}


		private void tryAddPreviousResults(
				Concepts predefinedConcept)
		{
			try
			{
				ExpectedResults exp = readExpectedPreviousResults(predefinedConcept);
				addExpectedResultsColumn(exp);
				addDefinedPatientsRows(exp);
			}
			catch (IOException | IllegalArgumentException ex)
			{
				// Log and skip column
				LOGGER.log(Level.INFO, String.format(
						"No previous %s results",
						predefinedConcept.toString()),
						ex);
			}
		}	


		/**
		 * @return an {@link ExpectedResults}-object containing the expected results of {@code predefinedConcept}
		 * 		as read from a json file as via {@link Config}.

		 */
		private ExpectedResults createExpectedResults(
				Concepts predefinedConcept) {
			Concept concept = getConcept(predefinedConcept);
			Map<PatisNumber, ConceptFoundStatus> expectedClassifications = configContext.getPatients(concept.getName());
			
			return ExpectedResultsMap.createWrapper(concept, expectedClassifications);
		}


		/**
		 * @return	an {@link ExpectedResults}-object of the results of a previous report stored by using 
		 * 		{@link #storeResults(Concepts) }.

		 * @throws IllegalArgumentException 	when the current directory contains no file for
		 * 		{@code predefinedConcept}.
		 */
		private ExpectedResults readExpectedPreviousResults(Concepts predefinedConcept) 
				throws FileNotFoundException, IOException, IllegalArgumentException {
			Concept concept = getConcept(predefinedConcept);
			File file = new FileNames().getMostRecentJson(concept);
			FileReader input = new FileReader(file);
			
			return ExpectedPreviousResults.read(concept, input);
		}


		/**
		 * Add a column containing the expected results to the report.
		 * 
		 * The {@link ExpectedResults#getDefinedPatients() patients} in {@code newColumn} are not added automatically,
		 * call {@link #addDefinedPatients(ExpectedResults)} to add them. 
		 */
		private void addExpectedResultsColumn(ExpectedResults newColumn) {
			searchResults.addExpectedResultsColumn(newColumn);
			validation.addExpectedResult(newColumn);
		}

		/**
		 * Add rows for all patients for whom {@code patientSource} defines expected results.
		 * 
		 * Each added patient has a single row: adding a patient multiple times results only in a single row.  Adding 
		 * a patient multiple times will occur, for example, when multiple ExpectedResults define results the same 
		 * patient.
		 */
		private void addDefinedPatientsRows(ExpectedResults patientSource) {
			searchResults.addAll(patientSource.getDefinedPatients());
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(ReportBuilder.class.getName());
	
	private @MonotonicNonNull Purpose purpose = null;
	private @MonotonicNonNull Config configContext = null;
	private @MonotonicNonNull Searcher searcher = null;
	private List<Concepts> columns_toConstruct = new LinkedList<>();
	private @MonotonicNonNull List<SemanticModifier> modifiersToApply = null;
	private @Nullable TableUnderConstruction constructed = null;
	
	public ReportBuilder() {
	}


	@EnsuresNonNull("purpose")
	public ReportBuilder setPurpose(Purpose purpose_)
	{
		purpose = purpose_;
		invallidateConstructedTable();
		return this;
	}


	@EnsuresNonNull("configContext")
	public ReportBuilder setConfig(Config config_)
	{
		configContext = config_;
		invallidateConstructedTable();
		return this;
	}


	@RequiresNonNull("configContext")
	@EnsuresNonNull("searcher")
	public ReportBuilder useDefaultSearcher() throws ServiceException, IOException
	{
		searcher = configContext.getSearcher();
		return this;
	}
	
	@EnsuresNonNull("searcher")
	public ReportBuilder setSearcher(Searcher searcher_)
	{
		searcher = searcher_;
		invallidateConstructedTable();
		return this;
	}

	
	public ReportBuilder addConcept (Concepts predefinedConcept)
	{
		columns_toConstruct.add(predefinedConcept);
		invallidateConstructedTable();
		return this;
	}


	@EnsuresNonNull("modifiersToApply")
	public ReportBuilder usePredefinedSemanticModifiers()
	{
		if (configContext != null) {
			modifiersToApply = getPredefinedSemanticModifiers();
			invallidateConstructedTable();
			return this;
		} else {
			throw new IllegalStateException("Set config before using predefined semantic modifiers.");
		}
	}


	@EnsuresNonNull("modifiersToApply")
	public ReportBuilder setSemanticModifiers(List<SemanticModifier> newList)
	{
		modifiersToApply = new ArrayList<>(newList);
		invallidateConstructedTable();
		return this;
	}


	@EnsuresNonNull({"purpose", "configContext", "searcher", "modifiersToApply", "constructed"})
	public SearchResultTable buildSearchTable() throws IllegalStateException, ServiceException, IOException {
		checkSettings();
		if (constructed == null) {
			build();
		}
		return constructed.searchResults;
	}
	
	
	@EnsuresNonNull({"purpose", "configContext", "searcher", "modifiersToApply", "constructed"})
	public ResultComparisonTable buildValidationTable() throws IllegalStateException, ServiceException, IOException {
		checkSettings();
		if (!purpose.equals(Purpose.VALIDATION)) {
			throw new IllegalStateException("To build a validation table, set purpose to VALIDATION");
		}
		if (constructed == null) {
			build();
		}
		return ((ValidationAndSearchtableUnderconstruction)constructed).validation;
	}
	
	
	@RequiresNonNull({"configContext"})
	private List<SemanticModifier> getPredefinedSemanticModifiers() {
		List<SemanticModifier> result = new ArrayList<>(SemanticModifiers.values().length + 1);
		result.add(SemanticModifier.Constants.NULL_MODIFIER);
		for (SemanticModifiers semmod : SemanticModifiers.values()) {
			result.add(semmod.getModifier(configContext));
		}
		return result;
	}

	
	private void invallidateConstructedTable() {
		constructed = null;
	}


	@RequiresNonNull({"purpose", "configContext", "searcher", "modifiersToApply"})
	@EnsuresNonNull("constructed")
	private void build() {
		final @NonNull TableUnderConstruction toConstruct;
		if (purpose == Purpose.PATIENT_INFO) {
			toConstruct = new TableUnderConstruction(configContext, searcher);
		} else {
			toConstruct = new ValidationAndSearchtableUnderconstruction(configContext, searcher);
		}
		
		for (Concepts predefinedConcept : columns_toConstruct) {
			toConstruct.addConceptColumns(predefinedConcept, modifiersToApply);
		}

		constructed = toConstruct;
	}


	@EnsuresNonNull({"purpose", "configContext", "searcher", "modifiersToApply"})
	private void checkSettings()
			throws IllegalStateException, ServiceException, IOException 
	{
		if (purpose == null) {
			throw new IllegalStateException("Set report's purpose");
		}
		if (configContext == null) {
			throw new IllegalStateException("Set configuration to use");
		}
		if (searcher == null) {
			useDefaultSearcher();
		}
		if (modifiersToApply == null) {
			usePredefinedSemanticModifiers();
		}
	}
}
