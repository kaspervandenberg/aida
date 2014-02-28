// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import checkers.nullness.quals.EnsuresNonNull;
/*>>> import checkers.nullness.quals.Nullable; */
import java.util.Objects;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;

/**
 * Convert a parse tree to a query object that Lucene search can execute
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ParseTreeToObjectAdapter extends LuceneObjectBase implements Query {
	public static class Builder implements QueryAdapterBuilder<ParseTree, LuceneObject> {
		private static final QueryBuilder defaultQueryBuilder;
		static {
			StandardQueryParser luceneDefault = new StandardQueryParser();
			defaultQueryBuilder = luceneDefault.getQueryBuilder();
		}
		
		private static final String defaultSuffix = ".luceneObject";

		private QueryBuilder queryBuilder;
		private String suffix;
		
		public Builder() {
			queryBuilder = defaultQueryBuilder;
			suffix = defaultSuffix;
		}

		public Builder queryBuilder(QueryBuilder builder_) {
			queryBuilder = builder_;
			return this;
		}

		public Builder suffix(String suffix_) {
			suffix = suffix_;
			return this;
		}

		public Builder withIdenticalId() {
			suffix = "";
			return this;
		}

		@Override
		public ParseTreeToObjectAdapter adapt(ParseTree adapted_) {
			Objects.requireNonNull(queryBuilder, "Set queryBuider before calling adapt.");
			Objects.requireNonNull(suffix, "Set suffix before calling adapt.");
			
			return new ParseTreeToObjectAdapter(queryBuilder,
					appendDefaultSuffix(adapted_),
					adapted_);
		}

		private QName appendDefaultSuffix(Query q) {
			return QNameUtil.instance().append(q.getName(), suffix);
		}
		
	}
	
	private final QueryBuilder builder;
	private final QName id;
	private final ParseTree adapted;
	private transient org.apache.lucene.search./*@Nullable*/ Query builtObject = null;		// Nullable must be before class name and not before package name as per http://stackoverflow.com/a/21385939/814206

	public ParseTreeToObjectAdapter(QueryBuilder builder_, QName id_, ParseTree adapted_) {
		this.builder = builder_;
		this.id = id_;
		this.adapted = adapted_;
	}

	@Override
	@EnsuresNonNull("builtObject")
	public org.apache.lucene.search.Query getRepresentation() {
		if(builtObject == null) {
			try {
				Object o = builder.build(adapted.getRepresentation());
				if(o instanceof org.apache.lucene.search.Query) {
					builtObject = (org.apache.lucene.search.Query)o;
				} else {
					throw new ClassCastException("Cannot convert built query to Query");
				}
			} catch (QueryNodeException ex) {
				throw new Error(ex);
			}
		}
		return builtObject;
	}

	@Override
	public QName getName() {
		return id;
	}

	
}
