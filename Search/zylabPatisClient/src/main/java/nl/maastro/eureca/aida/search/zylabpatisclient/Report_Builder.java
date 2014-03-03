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
public class Report_Builder {
	public enum Purpose {
		VALIDATION,
		PATIENT_INFO	
	}


	private static class Table_Under_Construction {
		protected final Config config_context;
		public final SearchResultTable search_results;

		public Table_Under_Construction(
				Config config_context_,
				Searcher searcher_)
		{
			this.config_context = config_context_;
			this.search_results = new SearchResultTable(searcher_);
		}


		public void add_concept_columns(
				Concepts predefined_concept,
				Iterable<SemanticModifier> semantic_modifiers_toApply)
		{
			Concept concept = get_concept(predefined_concept);
			add_concept_search_column(concept, semantic_modifiers_toApply);
		}

		
		/**
		 * Add a column containing results of searching for a concept.
		 */
		private void add_concept_search_column(
				Concept concept,
				Iterable<SemanticModifier> semantic_modifiers_toApply) {
			search_results.addConceptSearchColumn(concept, semantic_modifiers_toApply);
		}



		protected Concept get_concept(Concepts predefined_concept)
		{
			return predefined_concept.getConcept(config_context);
		}
	}


	private static class Validation_And_Searchtable_Under_construction extends Table_Under_Construction {
		public final ResultComparisonTable validation;

		public Validation_And_Searchtable_Under_construction(
				Config config_context_,
				Searcher searcher_)
		{
			super (config_context_, searcher_);
			this.validation = new ResultComparisonTable(search_results);
		}
		

		@Override
		public void add_concept_columns(
				Concepts predefined_concept,
				Iterable<SemanticModifier> semantic_modifiers_toApply)
		{
			add_validation_sources(predefined_concept);
			super.add_concept_columns(predefined_concept, semantic_modifiers_toApply);
		}
		
		
		private void add_validation_sources(
				Concepts predefined_concept)
		{
			add_external_validation_source(predefined_concept);
			try_add_previous_results(predefined_concept);
		}


		private void add_external_validation_source(
				Concepts predefined_concept)
		{
			ExpectedResults exp = create_Expected_Results(predefined_concept);
			add_expected_results_column(exp);
			add_defined_patients_rows(exp);
		}


		private void try_add_previous_results(
				Concepts predefined_concept)
		{
			try
			{
				ExpectedResults exp = read_expected_previous_results(predefined_concept);
				add_expected_results_column(exp);
				add_defined_patients_rows(exp);
			}
			catch (IOException | IllegalArgumentException ex)
			{
				// Log and skip column
				LOGGER.log(Level.INFO, String.format(
						"No previous %s results",
						predefined_concept.toString()),
						ex);
			}
		}	


		/**
		 * @return an {@link ExpectedResults}-object containing the expected results of {@code predefinedConcept}
		 * 		as read from a json file as via {@link Config}.

		 */
		private ExpectedResults create_Expected_Results(
				Concepts predefined_concept) {
			Concept concept = get_concept(predefined_concept);
			Map<PatisNumber, ConceptFoundStatus> expectedClassifications = config_context.getPatients(concept.getName());
			
			return ExpectedResultsMap.createWrapper(concept, expectedClassifications);
		}


		/**
		 * @return	an {@link ExpectedResults}-object of the results of a previous report stored by using 
		 * 		{@link #storeResults(Concepts) }.

		 * @throws IllegalArgumentException 	when the current directory contains no file for
		 * 		{@code predefinedConcept}.
		 */
		private ExpectedResults read_expected_previous_results(Concepts predefined_concept) 
				throws FileNotFoundException, IOException, IllegalArgumentException {
			Concept concept = get_concept(predefined_concept);
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
		private void add_expected_results_column(ExpectedResults newColumn) {
			search_results.addExpectedResultsColumn(newColumn);
			validation.addExpectedResult(newColumn);
		}

		/**
		 * Add rows for all patients for whom {@code patientSource} defines expected results.
		 * 
		 * Each added patient has a single row: adding a patient multiple times results only in a single row.  Adding 
		 * a patient multiple times will occur, for example, when multiple ExpectedResults define results the same 
		 * patient.
		 */
		private void add_defined_patients_rows(ExpectedResults patientSource) {
			search_results.addAll(patientSource.getDefinedPatients());
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(Report_Builder.class.getName());
	
	private @MonotonicNonNull Purpose purpose = null;
	private @MonotonicNonNull Config config_context = null;
	private @MonotonicNonNull Searcher searcher = null;
	private List<Concepts> columns_to_construct = new LinkedList<>();
	private @MonotonicNonNull List<SemanticModifier> modifiers_to_apply = null;
	private @Nullable Table_Under_Construction constructed = null;
	
	public Report_Builder() {
	}


	@EnsuresNonNull("purpose")
	public Report_Builder set_purpose(Purpose purpose_)
	{
		purpose = purpose_;
		invallidate_constructed_table();
		return this;
	}


	@EnsuresNonNull("config_context")
	public Report_Builder set_config(Config config_)
	{
		config_context = config_;
		invallidate_constructed_table();
		return this;
	}


	@RequiresNonNull("config_context")
	@EnsuresNonNull("searcher")
	public Report_Builder use_default_searcher() throws ServiceException, IOException
	{
		searcher = config_context.getSearcher();
		return this;
	}
	
	@EnsuresNonNull("searcher")
	public Report_Builder set_searcher(Searcher searcher_)
	{
		searcher = searcher_;
		invallidate_constructed_table();
		return this;
	}

	
	public Report_Builder add_concept (Concepts predefined_concept)
	{
		columns_to_construct.add(predefined_concept);
		invallidate_constructed_table();
		return this;
	}


	@EnsuresNonNull("modifiers_to_apply")
	public Report_Builder use_predefined_semantic_modifiers()
	{
		if (config_context != null) {
			modifiers_to_apply = get_predefined_semantic_modifiers();
			invallidate_constructed_table();
			return this;
		} else {
			throw new IllegalStateException("Set config before using predefined semantic modifiers.");
		}
	}


	@EnsuresNonNull("modifiers_to_apply")
	public Report_Builder set_semantic_modifiers(List<SemanticModifier> new_list)
	{
		modifiers_to_apply = new ArrayList<>(new_list);
		invallidate_constructed_table();
		return this;
	}


	@EnsuresNonNull({"purpose", "config_context", "searcher", "modifiers_to_apply", "constructed"})
	public SearchResultTable build_search_table() throws IllegalStateException, ServiceException, IOException {
		check_settings();
		if (constructed == null) {
			build();
		}
		return constructed.search_results;
	}
	
	
	@EnsuresNonNull({"purpose", "config_context", "searcher", "modifiers_to_apply", "constructed"})
	public ResultComparisonTable build_validation_table() throws IllegalStateException, ServiceException, IOException {
		check_settings();
		if (!purpose.equals(Purpose.VALIDATION)) {
			throw new IllegalStateException("To build a validation table, set purpose to VALIDATION");
		}
		if (constructed == null) {
			build();
		}
		return ((Validation_And_Searchtable_Under_construction)constructed).validation;
	}
	
	
	@RequiresNonNull({"config_context"})
	private List<SemanticModifier> get_predefined_semantic_modifiers() {
		List<SemanticModifier> result = new ArrayList<>(SemanticModifiers.values().length + 1);
		result.add(SemanticModifier.Constants.NULL_MODIFIER);
		for (SemanticModifiers semmod : SemanticModifiers.values()) {
			result.add(semmod.getModifier(config_context));
		}
		return result;
	}

	
	private void invallidate_constructed_table() {
		constructed = null;
	}


	@RequiresNonNull({"purpose", "config_context", "searcher", "modifiers_to_apply"})
	@EnsuresNonNull("constructed")
	private void build() {
		final @NonNull Table_Under_Construction to_construct;
		if (purpose == Purpose.PATIENT_INFO) {
			to_construct = new Table_Under_Construction(config_context, searcher);
		} else {
			to_construct = new Validation_And_Searchtable_Under_construction(config_context, searcher);
		}
		
		for (Concepts predefined_concept : columns_to_construct) {
			to_construct.add_concept_columns(predefined_concept, modifiers_to_apply);
		}

		constructed = to_construct;
	}


	@EnsuresNonNull({"purpose", "config_context", "searcher", "modifiers_to_apply"})
	private void check_settings()
			throws IllegalStateException, ServiceException, IOException 
	{
		if (purpose == null) {
			throw new IllegalStateException("Set report's purpose");
		}
		if (config_context == null) {
			throw new IllegalStateException("Set configuration to use");
		}
		if (searcher == null) {
			use_default_searcher();
		}
		if (modifiers_to_apply == null) {
			use_predefined_semantic_modifiers();
		}
	}
}
