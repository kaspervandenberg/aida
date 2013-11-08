// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil.ClosableRepositoryConnection;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil.ClosableRepositoryConnectionFactory;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil.QueryCache;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

		
/**
 * Use expected results stored on an SPARQL Endpoint.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 *
 */
public class ExpectedResultsRdf implements ExpectedResults {
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
	public boolean isInDefined(PatisNumber patient) {
		try (AutoClosableQuery obj_query = preparedQueries.get(SparqlQueries.IS_PATIENT_DEFINED)) {
			synchronized (obj_query) {
				addBindings(obj_query);
				BooleanQuery query = cast(BooleanQuery.class, obj_query, SparqlQueries.IS_PATIENT_DEFINED);
				RdfVariableBindings.PATIENT.bind(valueFactory, query, patient);
				return query.evaluate();
			}
		} catch (RepositoryException | QueryEvaluationException ex) {
			throw new Error(ex);
		}
	}

	@Override
	public String getTitle() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<PatisNumber> getDefinedPatients() {
		try (AutoClosableQuery obj_query = preparedQueries.get(SparqlQueries.DEFINED_PATIENTS)) {
			synchronized (obj_query) {
				addBindings(obj_query);
				TupleQuery query = cast(TupleQuery.class, obj_query, SparqlQueries.DEFINED_PATIENTS);
				List<PatisNumber> result = queryResultsToList(query);
				return result;
			}
		} catch (RepositoryException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, String.format(
					"Sesame repository error while executing query %s; using empty list of expected results",
					SparqlQueries.DEFINED_PATIENTS),
					ex);
			return Collections.emptyList();
		}
	}

	@Override
	public boolean isAsExpected(SearchResult searchResult) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean containsExpected(SearchResult searchResult) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ConceptFoundStatus getClassification(PatisNumber patient) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public SearchResult createExpectedResult(PatisNumber patient) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<SearchResult> createAllExpectedResults() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private void addBindings(Query query) {
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

	private List<PatisNumber> queryResultsToList(TupleQuery query) {
		List<PatisNumber> target = new LinkedList<>();
		try {
			TupleQueryResult result = null;
			try {
					result = query.evaluate();
					while(result.hasNext()) {
						target.add(extractPatient(result.next()));
					}
			} finally {
				if (result != null) {
					result.close();
				}
			}
		} catch (QueryEvaluationException ex) {
			Logger.getLogger(ExpectedResultsRdf.class.getName()).log(Level.WARNING, 
					"Sesame repository error while executing query; list of expected results might be incomplete",
					ex);
		}
		return target;
	}

	private PatisNumber extractPatient(BindingSet binding) {
		return RdfVariableBindings.PATIENT.getPatient(binding);
	}
}
