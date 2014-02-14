// © Maastr Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Arrays;
import java.util.Objects;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObjectBase;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTreeBase;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQueryBase;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.HasString;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

/**
 * A Patisnumber identifies a patient
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PatisNumber extends HasString {
	/**
	 * The (name of the) Lucene-field in which the PatisNumber a document is 
	 * about is stored.  This must match field name in the index.
	 */
	private static final transient String PATIS_FIELD = "Patisnummer";

	/**
	 * The actual number
	 */
//	public final String value;

	private PatisNumber(String value_) {
		super(value_);
	}

	/**
	 * Create a new patis number; check that {@code value_} is a well formed 
	 * patisnumber.
	 * 
	 * @param value_	string to convert into a patisnumber
	 * @return	the constructed {@link PatisNumber}
	 * 
	 * @throws IllegalArgumentException	when {@code value_} is not a number 
	 */
	public static PatisNumber create(String value_) throws IllegalArgumentException {
		try {
			Integer v = Integer.parseInt(value_);
			if(v < 0 || v > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						String.format("Patis number value (%s) is out of range.",
						value_));
			} 
			return new PatisNumber(value_);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException(
					String.format("%s is not a wellformed PatisNumber", value_),
					ex);
		}
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 41 * hash + Objects.hashCode(this.value);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PatisNumber other = (PatisNumber) obj;
		if (!Objects.equals(this.value, other.value)) {
			return false;
		}
		return true;
	}

	public org.apache.lucene.search.Query as_lucene_query()
	{
		return new TermQuery(new Term(PATIS_FIELD, value));
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
		return other.accept(new Query.Visitor<Query>() {
			@Override
			public Query visit(final LuceneObject element) {
				return new LuceneObjectBase() {
					@Override
					public org.apache.lucene.search.Query getRepresentation() {
						BooleanQuery result = new BooleanQuery();
						result.add(new TermQuery(new Term(PATIS_FIELD, value)), BooleanClause.Occur.MUST);
						result.add(element.getRepresentation(), BooleanClause.Occur.MUST);
						return result;
					}

					@Override
					public QName getName() {
						return QNameUtil.instance().append(element.getName(),
								"-" + PatisNumber.this.getValue());
					}
				};
			}

			@Override
			public Query visit(final StringQuery element) {
				return new StringQueryBase() {
					@Override
					public String getRepresentation() {
						return String.format("%s:%s AND %s",
								PATIS_FIELD,value, element.getRepresentation());
					}

					@Override
					public QName getName() {
						return QNameUtil.instance().append(element.getName(),
								"-" + PatisNumber.this.getValue());
					}
				};
			}

			@Override
			public Query visit(final ParseTree element) {
				return new ParseTreeBase() {
					@Override
					public QueryNode getRepresentation() {
						return new AndQueryNode(
								Arrays.asList(new FieldQueryNode(PATIS_FIELD, value, 0, value.length()),
								element.getRepresentation()));
					}

					@Override
					public QName getName() {
						return QNameUtil.instance().append(element.getName(),
								"-" + PatisNumber.this.getValue());
					}
				};
			}
		});
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
