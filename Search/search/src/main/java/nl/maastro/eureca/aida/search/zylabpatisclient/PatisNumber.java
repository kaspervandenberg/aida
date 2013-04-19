// © Maastr Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * A Patisnumber identifies a patient
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PatisNumber {
	/**
	 * The (name of the) Lucene-field in which the PatisNumber a document is 
	 * about is stored.  This must match field name in the index.
	 */
	private static final transient String PATIS_FIELD = "Patisnummer";

	/**
	 * The actual number
	 */
	private final String value;

	private PatisNumber(String value_) {
		value = value_;
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Construct a Lucene {@link Query} composed of {@code other} combined with 
	 * this {@code PatisNumber}.  The resulting query will return the subset of
	 * result of {@code other} restricted to documents having this 
	 * {@code PatisNumber}.
	 * 
	 * @param other		the {@link Query} to restrict
	 * @return			a {@link BooleanQuery} with two clauses: this 
	 * 		{@code PatisNumber} and {@code other}
	 */
	public Query compose(final Query other) {
		BooleanQuery result = new BooleanQuery();
		result.add(new TermQuery(new Term(PATIS_FIELD, value)), BooleanClause.Occur.MUST);
		result.add(other, BooleanClause.Occur.MUST);
		return result;
	}

	/**
	 * Construct a Lucene query composed of {@code other} combined with 
	 * this {@code PatisNumber}.  The resulting query will return the subset of
	 * result of {@code other} restricted to documents having this 
	 * {@code PatisNumber}.
	 * 
	 * @param other		the query to restrict as a String
	 * @return			a {@link String} that Lucene's 
	 * 		{@link org.apache.lucene.queryparser.flexible.standard.StandardQueryParser standard}
	 * 		/ {@link org.apache.lucene.queryparser.classic.QueryParser classic}
	 * 		queryparser	can parse.
	 */
	public String compose(final String query) {
		StringBuilder result = new StringBuilder();
		result.append(PATIS_FIELD);
		result.append(":");
		result.append(value);
		result.append(" AND (");
		result.append(query);
		result.append(")");
		return result.toString();
	}
	
}
