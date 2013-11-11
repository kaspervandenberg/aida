// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

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
public class ExpectedResultsRdf implements ExpectedResults {
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
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isInDefined(PatisNumber patient) {
		try (AutoClosableQuery obj_query = preparedQueries.get(SparqlQueries.IS_PATIENT_DEFINED)) {
			synchronized (obj_query) {
				BooleanQuery query = cast(BooleanQuery.class, obj_query, SparqlQueries.IS_PATIENT_DEFINED);
				addCommonBindings(query);
				RdfVariableBindings.PATIENT.bind(valueFactory, query, patient);
				return query.evaluate();
			}
		} catch (RepositoryException | QueryEvaluationException ex) {
			throw new Error(ex);
		}
	}

	@Override
	public Iterable<PatisNumber> getDefinedPatients() {
		try (AutoClosableQuery obj_query = preparedQueries.get(SparqlQueries.SELECT_DEFINED_PATIENTS)) {
			synchronized (obj_query) {
				TupleQuery query = cast(TupleQuery.class, obj_query, SparqlQueries.SELECT_DEFINED_PATIENTS);
				addCommonBindings(query);
				List<PatisNumber> result = queryResultsToPatientList(query);
				return result;
			}
		} catch (RepositoryException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while executing query %s; using empty list of expected results",
					SparqlQueries.SELECT_DEFINED_PATIENTS),
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
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<SearchResult> createAllExpectedResults() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private void addCommonBindings(Query query) {
		query.clearBindings();
		RdfVariableBindings.CONCEPT.bind(valueFactory, query, about);
		RdfVariableBindings.EXPECTATION_ID.bind(valueFactory, query, expectationId);
	}
	
	private static <T extends Query> T cast(Class<T> targetClass, Query query, SparqlQueries id) {
		if (!targetClass.isInstance(query)) {
			throw new Error(new ClassCastException(String.format(
					"Expecting query for %s to be a %s", id, targetClass.getSimpleName())));
		}
		return targetClass.cast(query);
	}

	private static <T> Collection<? super T> addQueryResults(Collection<? super T> target, TupleQuery query,
			Class<T> valueType, RdfVariableBindings binding) {
		try {
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
		} catch (QueryEvaluationException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, 
					"Sesame repository error while executing query; collection of expected results might be incomplete",
					ex);
		}
		return target;
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

	private List<PatisNumber> queryResultsToPatientList(TupleQuery query) {
		List<PatisNumber> target = new LinkedList<>();
		addQueryResults(target, query, PatisNumber.class, RdfVariableBindings.PATIENT);
		return target;
	}
	
	private Set<ConceptFoundStatus> queryResultsToStatusSet(TupleQuery query) {
		Set<ConceptFoundStatus> target = new HashSet<>();
		addQueryResults(target, query, ConceptFoundStatus.class, RdfVariableBindings.STATUS);
		return target;
	}
	
	private boolean isAsExpectedSingleStatus(PatisNumber patient, ConceptFoundStatus status) {
		try (AutoClosableQuery obj_query = preparedQueries.get(SparqlQueries.IS_AS_EXPECTED)) {
			synchronized (obj_query) {
				BooleanQuery query = cast(BooleanQuery.class, obj_query, SparqlQueries.IS_AS_EXPECTED);
				addCommonBindings(query);
				RdfVariableBindings.PATIENT.bind(valueFactory, query, patient);
				RdfVariableBindings.STATUS.bind(valueFactory, query, status);
				return query.evaluate();
			}
		} catch (RepositoryException | QueryEvaluationException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while executing query %s; using empty list of expected results",
					SparqlQueries.SELECT_DEFINED_PATIENTS),
					ex);
			return status.equals(DEFAULT_CLASSIFICATION);
		}
	}

	private Set<ConceptFoundStatus> selectConceptFoundStatus(PatisNumber patient) {
		try (AutoClosableQuery obj_query = preparedQueries.get(SparqlQueries.SELECT_EXPECTED_STATUS)) {
			TupleQuery query = cast(TupleQuery.class,obj_query, SparqlQueries.SELECT_EXPECTED_STATUS);
			addCommonBindings(query);
			RdfVariableBindings.PATIENT.bind(valueFactory, query, patient);
			Set<ConceptFoundStatus> expectedStatusses = queryResultsToStatusSet(query);
			return expectedStatusses;
		} catch (RepositoryException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while executing query %s; using UNKNOWN as expected results",
					SparqlQueries.SELECT_EXPECTED_STATUS),
					ex);
			return Collections.singleton(DEFAULT_CLASSIFICATION);
		}

	}
}
