/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;

/**
 *
 * @author kasper2
 */
public class QueryDisplay {
	private static QueryDisplay singleton;

	private QueryDisplay() {
		// Intentionally left empty
	}

	public QueryDisplay instance() {
		if(singleton == null) {
			singleton = new QueryDisplay();
		}
		return singleton;
	}
	
	public String dumpQuery(String indent, Query q) {
		if(q instanceof TermQuery) {
			return dumpTermQuery(indent, (TermQuery)q);
		} else if (q instanceof BooleanQuery) {
			return dumpBooleanQuery(indent, (BooleanQuery)q);
		} else if (q instanceof FuzzyQuery) {
			return dumpFuzzyQuery(indent, (FuzzyQuery)q);
		} else if (q instanceof TermRangeQuery) {
			return dumpTermRangeQuery(indent, (TermRangeQuery)q);
		} else {
			throw new Error(String.format("Unsupported Query type %s",q.getClass().getName()));
		}
	}
	
	private String dumpTermQuery(String indent, TermQuery q) {
		return String.format("%sTermQuery: (%s, %s)\n",
				indent, q.getTerm().field(), q.getTerm().text());
	}

	private String dumpBooleanQuery(String indent, BooleanQuery q) {
		String subIndent = indent + "\t";
		StringBuilder result = new StringBuilder(String.format("%sBooleanQuery: [\n", indent));
		for (BooleanClause clause : q.clauses()) {
			result.append(dumpQuery(subIndent, clause.getQuery()));
		}
		result.append(indent);
		result.append("]\n");

		return result.toString();
	}

	private String dumpFuzzyQuery(String indent, FuzzyQuery q) {
		return String.format("%sFuzzyQuery: (%s, %s)~%d\n",
				indent, q.getTerm().field(), q.getTerm().text(), q.getMaxEdits());
	}

	private String dumpTermRangeQuery(String indent, TermRangeQuery q) {
		return String.format("%sTermRangeQuery: (%s, %s--%s)\n",
				indent, q.getField(), q.getLowerTerm().utf8ToString(), q.getUpperTerm().utf8ToString());
	}
	
}
