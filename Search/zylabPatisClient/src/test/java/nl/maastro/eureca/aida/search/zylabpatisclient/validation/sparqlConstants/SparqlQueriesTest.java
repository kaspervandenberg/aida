// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants;

import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.CharacterPositionRuler;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants.SparqlQueries;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.SPARQLParser;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class SparqlQueriesTest {
	@DataPoint
	public static SparqlQueries DEFINED_PATIENTS = SparqlQueries.DEFINED_PATIENTS;

	@DataPoint
	public static SparqlQueries IS_PATIENT_DEFINED = SparqlQueries.IS_PATIENT_DEFINED;
	
	private SPARQLParser parser;
	
	@Before
	public void setup() {
		parser = new SPARQLParser();
	}

	@Theory
	public void testQueryParsable(SparqlQueries query) throws MalformedQueryException {
		try {
			parser.parseQuery(query.getContents(), Config.PropertyKeys.RDF_VALIDATION_URI.getValue());
		} catch (MalformedQueryException ex) {
			System.err.printf("\nMalformed SparQL:\n%s\n", query.getContents());
			System.err.append(createRuler(query.getContents().length()));
			
			ex.printStackTrace();
			throw ex;
		}
	}

	private String createRuler(int length) {
		return CharacterPositionRuler.createDecimalRuler(length);
	}
}