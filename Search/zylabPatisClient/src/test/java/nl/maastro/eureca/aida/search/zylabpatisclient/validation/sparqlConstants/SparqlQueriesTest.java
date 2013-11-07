// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants;

import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
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
			System.err.append(printRuler(query.getContents().length()));
			
			ex.printStackTrace();
			throw ex;
		}
	}

	private String printRuler(int length) {
		StringBuilder unitLine = new StringBuilder(length + 2);
		StringBuilder decLine = new StringBuilder(length +2);
		StringBuilder hectoLine = new StringBuilder(length + 2);

		for (int i = 1; i <= length; i ++) {
			appendDigitOrDefault(unitLine, 2, i, ".", 1);
			appendDigitOrDefault(decLine, 10, i, " ", 10);
			appendDigitOrDefault(hectoLine, 100, i, " ", 100);
		}
		
		StringBuilder result = new StringBuilder(2 * length + 4);
		result.append(unitLine);
		result.append("\n");
		result.append(decLine);
		result.append("\n");
		result.append(hectoLine);
		result.append("\n");

		return result.toString();
	}

	private void appendDigitOrDefault(StringBuilder target, int digitSelectionDivisor, 
			int i, String nonDigit, int radix) {
		boolean isDividable = (i % digitSelectionDivisor) == 0;
		if(isDividable) {
			appendDigit(target, i, radix);
		} else {
			target.append(nonDigit);
		}
	}
	
	private void appendDigit(StringBuilder target, int i, int radix) {
		int digit = (i / radix) % 10;
		String str_digit = String.format("%1d", digit);
		target.append(str_digit);
	}
	
}