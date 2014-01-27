// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.Nullable;
import java.util.Objects;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class QueryDisplay {
	private static @Nullable QueryDisplay singleton = null;

	private QueryDisplay() {
		// Intentionally left empty
	}

	@EnsuresNonNull("singleton")
	public static QueryDisplay instance() {
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
		} else if (q instanceof SpanNearQuery) {
			return dumpSpanNearQuery(indent, (SpanNearQuery)q);
		} else if (q instanceof SpanOrQuery) {
			return dumpSpanOrQuery(indent, (SpanOrQuery)q);
		} else if (q instanceof SpanTermQuery) {
			return dumpSpanTermQuery(indent, (SpanTermQuery)q);
		} if(q instanceof SpanMultiTermQueryWrapper) {
			return dumpSpanMultiTermWrapper(indent, (SpanMultiTermQueryWrapper<?>)q);
		} else {
			throw new Error(String.format("Unsupported Query type %s",q.getClass().getName()));
		}
	}
	
	private String dumpTermQuery(String indent, TermQuery q) {
		return String.format("%sTermQuery (%s): (%s, %s)\n",
				indent, q.getClass().getName(), q.getTerm().field(), q.getTerm().text());
	}

	private String dumpSpanTermQuery(String indent, SpanTermQuery q) {
		return String.format("%sSpanTermQuery (%s): (%s, %s)\n",
				indent, q.getClass().getName(), q.getTerm().field(), q.getTerm().text());
	}

	private String dumpBooleanQuery(String indent, BooleanQuery q) {
		String subIndent = indent + "\t";
		StringBuilder result = new StringBuilder(
				String.format("%sBooleanQuery (%s): [\n",
				indent,
				q.getClass().getName()));
		for (BooleanClause clause : q.clauses()) {
			result.append(String.format("%soccurence: %s; clause:",
					subIndent,
					Objects.toString(clause.getOccur().name(), "not set")));
			result.append(dumpQuery(subIndent, clause.getQuery()));
		}
		result.append(indent);
		result.append("]\n");

		return result.toString();
	}

	private String dumpFuzzyQuery(String indent, FuzzyQuery q) {
		return String.format("%sFuzzyQuery (%s): (%s, %s)~%d\n",
				indent, q.getClass().getName(), q.getTerm().field(),
				q.getTerm().text(), q.getMaxEdits());
	}

	private String dumpTermRangeQuery(String indent, TermRangeQuery q) {
		return String.format("%sTermRangeQuery (%s): (%s, %s--%s)\n",
				indent, q.getClass().getName(), q.getField(), 
				q.getLowerTerm().utf8ToString(), q.getUpperTerm().utf8ToString());
	}

	private String dumpSpanNearQuery(String indent, SpanNearQuery q) {
		String subIndent = indent +"\t";
		StringBuilder result = new StringBuilder(String.format("%sSpanNearQuery (slop:%s, type:%s) [\n",
				indent,
				Integer.valueOf(q.getSlop()).toString(),
				q.getClass().getName()));
		for (SpanQuery clause : q.getClauses()) {
			result.append(dumpQuery(subIndent, clause));
		}
		result.append(indent);
		result.append("]\n");
		return result.toString();
	}

	private String dumpSpanOrQuery(String indent, SpanOrQuery q) {
		String subIndent = indent +"\t";
		StringBuilder result = new StringBuilder(String.format("%sSpanOrQuery (%s) [\n",
				indent,
				q.getClass().getName()));
		for (SpanQuery clause : q.getClauses()) {
			result.append(dumpQuery(subIndent, clause));
		}
		result.append(indent);
		result.append("]\n");
		return result.toString();
	}

	private String dumpSpanMultiTermWrapper(String indent, SpanMultiTermQueryWrapper<?> q) {
		return String.format("%sSpan MT wrapper (%s): %s", 
			indent, q.getClass().getName(), q.toString(""));
	}
}
