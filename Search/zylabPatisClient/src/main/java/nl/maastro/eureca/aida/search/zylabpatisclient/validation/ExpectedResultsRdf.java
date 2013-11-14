// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants.SparqlQueries;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants.RdfVariableBindings;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil.AutoClosableQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil.QueryCache;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

		
/**
 * Use expected results stored on an SPARQL Endpoint.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 *
 */
public class ExpectedResultsRdf implements ExpectedResults, Closeable {
	private static final ConceptFoundStatus DEFAULT_CLASSIFICATION = ConceptFoundStatus.UNKNOWN;
	private static final ConceptFoundStatus CONFLICTING_CLASSIFICATION = ConceptFoundStatus.UNKNOWN;
	
	private final Repository repo;
	private final ValueFactory valueFactory;
	private final Concept about;
	private final QName expectationId;
	private final QueryCache preparedQueries;
	
	public ExpectedResultsRdf(Repository repo_, Concept about_, QName expectationId_) {
		this.repo = repo_;
		this.valueFactory = repo.getValueFactory();
		this.about = about_;
		this.expectationId = expectationId_;
		this.preparedQueries = new QueryCache(this.repo);
	}

	@Override
	public Concept getAboutConcept() {
		return about;
	}

	@Override
	public String getTitle() {
		String conceptName = getAboutConcept().getName().getLocalPart();
		String idLabel = queryIdLabel();
		String fullTitle = String.format("Expected %s (%s)", conceptName, idLabel);
		return fullTitle;
	}

	@Override
	public boolean isInDefined(PatisNumber patient) {
		try {
			RdfVariableBindings.Binder patientInputVariableBinder = createPatientBinder(patient);
			boolean result = evaluateQuery(SparqlQueries.IS_PATIENT_DEFINED, patientInputVariableBinder);
			return result;
		} catch (QueryEvaluationException | RepositoryException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while checking expected results for "
					+ "concept %s, expectation %s, patient %s (query %s); "
					+ "assuming no results and continuing.",
					about.getName(), expectationId, patient.getValue(), SparqlQueries.IS_PATIENT_DEFINED),
					ex);
		}
		return false;
	}

	@Override
	public Iterable<PatisNumber> getDefinedPatients() {
		try {
			RdfVariableBindings.Binder inputVariableBinder = createCommonBinder();
			List<PatisNumber> target = new LinkedList<>();
			List<PatisNumber> definedPatients = evaluateQuery(
					SparqlQueries.SELECT_DEFINED_PATIENTS, inputVariableBinder,
					target, PatisNumber.class, RdfVariableBindings.PATIENT);
			return definedPatients;
		} catch (QueryEvaluationException | RepositoryException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while retrieving defined patients for "
					+ "concept %s, expectation %s (query %s); "
					+ "using empty list of expected results and continuing.",
					about.getName(), expectationId, SparqlQueries.SELECT_DEFINED_PATIENTS),
					ex);
			return Collections.emptyList();
		}
	}

	@Override
	public boolean isAsExpected(SearchResult searchResult) {
		if(isSingleton(searchResult.getClassification())) {
			return isAsExpectedSingleStatus(searchResult.getPatient(), getSingleElement(searchResult.getClassification()));
		} else {
			return false;
		}
	}

	@Override
	public boolean containsExpected(SearchResult searchResult) {
		Set<ConceptFoundStatus> expectedStatuses = selectConceptFoundStatus(searchResult.getPatient());
		Set<ConceptFoundStatus> actualStatuses = searchResult.getClassification();
		return actualStatuses.containsAll(expectedStatuses);
	}

	@Override
	public ConceptFoundStatus getClassification(PatisNumber patient) {
		Set<ConceptFoundStatus> expectedStatuses = selectConceptFoundStatus(patient);
		if(!expectedStatuses.isEmpty()) {
			if(isSingleton(expectedStatuses)) {
				return getSingleElement(expectedStatuses);
			} else {
				return CONFLICTING_CLASSIFICATION;
			}
		} else {
			return DEFAULT_CLASSIFICATION;
		}
	}

	@Override
	public SearchResult createExpectedResult(PatisNumber patient) {
		ConceptFoundStatus expectedStatus = getClassification(patient);
		DummySearchResult.Creators factory = DummySearchResult.Creators.valueOf(expectedStatus);
		DummySearchResult result = factory.create(patient);
		return result;
	}

	@Override
	public Iterable<SearchResult> createAllExpectedResults() {
		return new ExpectedResultsToSearchResultsConvertor(this);
	}

	@Override
	public void close() {
		preparedQueries.close();
	}

	

	private void addCommonBindings(Query query) {
		query.clearBindings();
		RdfVariableBindings.CONCEPT.bind(valueFactory, query, about);
		RdfVariableBindings.EXPECTATION_ID.bind(valueFactory, query, expectationId);
	}
	
	private String queryIdLabel() {
		String idLabel = "unknown";
		try {
			RdfVariableBindings.Binder titleBinder = createCommonBinder();
			Set<String> target = new HashSet<>();
			Set<String> titles = evaluateQuery(SparqlQueries.GET_TITLE,titleBinder, 
					target, String.class, RdfVariableBindings.TITLE);
			
			if(isSingleton(titles)) {
				idLabel = getSingleElement(titles);
			} else if (titles.size() > 1) {
				idLabel = compose(titles);
			}
		} catch (QueryEvaluationException | RepositoryException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while reading title label for "
					+ "concept %s, expectation %s (query %s); "
					+ "continuing with incorrect label.",
					about.getName(), expectationId, SparqlQueries.GET_TITLE),
					ex);
			idLabel = "<<ERROR>>";
		}
		return idLabel;
	}

	private boolean isAsExpectedSingleStatus(PatisNumber patient, ConceptFoundStatus status) {
		try {
			RdfVariableBindings.Binder patientStatusBindingsBinder = createPatientStatusBinder(patient, status);
			boolean result = evaluateQuery(SparqlQueries.IS_AS_EXPECTED, patientStatusBindingsBinder);
			return result;
		} catch (RepositoryException | QueryEvaluationException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while checking status is as expected for "
					+ "concept %s, expectation %s, patient %s, and expected status %s (query %s); "
					+ "using %s as expected classification and continuing.",
					about.getName(), expectationId, patient.getValue(), status.getQName(),
					SparqlQueries.SELECT_DEFINED_PATIENTS, DEFAULT_CLASSIFICATION.name()),
					ex);
			return status.equals(DEFAULT_CLASSIFICATION);
		}
	}

	private Set<ConceptFoundStatus> selectConceptFoundStatus(PatisNumber patient) {
		try {
			RdfVariableBindings.Binder patientBindingsBinder = createPatientBinder(patient);
			Set<ConceptFoundStatus> target = new HashSet<>();
			Set<ConceptFoundStatus> expectedStatusses = evaluateQuery(
					SparqlQueries.SELECT_EXPECTED_STATUS, patientBindingsBinder, target,
					ConceptFoundStatus.class, RdfVariableBindings.STATUS);
			return expectedStatusses;
		} catch (RepositoryException | QueryEvaluationException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while retrieving expected status for "
					+ "concept %s, expectation %s, and patient %s (query %s); "
					+ "using %s as expected results and continuing.",
					about.getName(), expectationId, patient.getValue(),
					SparqlQueries.SELECT_EXPECTED_STATUS, DEFAULT_CLASSIFICATION),
					ex);
			return Collections.singleton(DEFAULT_CLASSIFICATION);
		}
	}


	private RdfVariableBindings.Binder createCommonBinder() {
		RdfVariableBindings.Binder conceptBinder = RdfVariableBindings.CONCEPT.createBinder(about);
		RdfVariableBindings.Binder expectationIdBinder = RdfVariableBindings.EXPECTATION_ID.createBinder(expectationId);
		RdfVariableBindings.Binder commonBindingsBinders = RdfVariableBindings.createCompoundBinder(
				conceptBinder, expectationIdBinder);
		return commonBindingsBinders;
	}

	private RdfVariableBindings.Binder createPatientBinder(PatisNumber patient) {
		RdfVariableBindings.Binder commonBindingsBinder = createCommonBinder();
		RdfVariableBindings.Binder patientBinder = RdfVariableBindings.PATIENT.createBinder(patient);
		RdfVariableBindings.Binder allPatientBinder = RdfVariableBindings.createCompoundBinder(
				commonBindingsBinder, patientBinder);
		return allPatientBinder;
	}

	private RdfVariableBindings.Binder createPatientStatusBinder(PatisNumber patient, ConceptFoundStatus status) {
		RdfVariableBindings.Binder patientBinder = createPatientBinder(patient);
		RdfVariableBindings.Binder statusBinder = RdfVariableBindings.STATUS.createBinder(status);
		RdfVariableBindings.Binder allPatientStatusBinder = RdfVariableBindings.createCompoundBinder(
				patientBinder, statusBinder);
		return allPatientStatusBinder;
	}

	private <TElement, TCollection extends Collection<TElement>> TCollection evaluateQuery(
			SparqlQueries queryId, 
			RdfVariableBindings.Binder inputVariableBinder, 
			TCollection target, Class<TElement> valueElementType,
			RdfVariableBindings resultBinding) 
				throws RepositoryException, QueryEvaluationException {
		try (AutoClosableQuery obj_query = preparedQueries.get(queryId)) {
			synchronized (obj_query) {
				TupleQuery query = cast(TupleQuery.class, obj_query, queryId);
				TCollection result = evaluateTupleQuery(
						query, inputVariableBinder, target, valueElementType, resultBinding);
				return result;
			}
		} 
	}

	private boolean evaluateQuery(SparqlQueries queryId, RdfVariableBindings.Binder inputVariableBinder) 
			throws QueryEvaluationException, RepositoryException {
		try (AutoClosableQuery obj_query = preparedQueries.get(queryId)) {
			synchronized (obj_query) {
				BooleanQuery query = cast(BooleanQuery.class, obj_query, queryId);
				boolean result = evaluateBooleanQuery(query, inputVariableBinder);
				return result;
			}
		}
	}

	private static <T extends Query> T cast(Class<T> targetClass, Query query, SparqlQueries id) {
		if (!targetClass.isInstance(query)) {
			throw new Error(new ClassCastException(String.format(
					"Expecting query for %s to be a %s", id, targetClass.getSimpleName())));
		}
		return targetClass.cast(query);
	}

	private <TElement, TCollection extends Collection<TElement>> TCollection evaluateTupleQuery(
			TupleQuery query, RdfVariableBindings.Binder inputVariableBinder,
			TCollection target, Class<TElement> valueElementType,
			RdfVariableBindings resultBinding) throws QueryEvaluationException {
		query.clearBindings();
		inputVariableBinder.bind(valueFactory, query);
		addQueryResults(target, query, valueElementType, resultBinding);
		return target;
	}

	private static <T> Collection<? super T> addQueryResults(
			Collection<? super T> target, TupleQuery query,
			Class<T> valueType, RdfVariableBindings binding) throws QueryEvaluationException {
		TupleQueryResult result = null;
		try {
				result = query.evaluate();
				while(result.hasNext()) {
					BindingSet boundVariables = result.next();
					target.add(binding.getValue(valueType, boundVariables));
				}
		} finally {
			if (result != null) {
				result.close();
			}
		}
		return target;
	}

	private boolean evaluateBooleanQuery(BooleanQuery query, RdfVariableBindings.Binder inputVariableBinder) 
			throws QueryEvaluationException {
		query.clearBindings();
		inputVariableBinder.bind(valueFactory, query);
		boolean result = query.evaluate();
		return result;
	}

	private static boolean isSingleton(Set<?> set) {
		return set.size() == 1;
	}

	private static <T> T getSingleElement(Set<T> statusSingleton) {
		if(!isSingleton(statusSingleton)) {
			throw new IllegalArgumentException(String.format("Set %s is not a singleton", statusSingleton));
		}
		Iterator<T> i = statusSingleton.iterator();
		return i.next();
	}

	private static String compose(Set<String> items) {
		StringBuilder result = new StringBuilder();
		result.append("{");
		Iterator<String> iter = items.iterator();
		while(iter.hasNext()) {
			result.append(iter.next());
			if(iter.hasNext()) {
				result.append(", ");
			}
		}
		return result.toString();
	}
}
