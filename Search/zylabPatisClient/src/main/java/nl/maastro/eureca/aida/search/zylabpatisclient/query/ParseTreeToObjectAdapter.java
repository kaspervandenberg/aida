// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;

/**
 * Convert a parse tree to a query object that Lucene search can execute
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ParseTreeToObjectAdapter extends LuceneObject implements Query {
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

		public ParseTreeToObjectAdapter adapt(ParseTree adapted_) {
			Objects.requireNonNull(queryBuilder, "Set queryBuider before calling adapt.");
			Objects.requireNonNull(suffix, "Set suffix before calling adapt.");
			
			return new ParseTreeToObjectAdapter(queryBuilder,
					appendDefaultSuffix(adapted_),
					adapted_);
		}

		private QName appendDefaultSuffix(Query q) {
			QName src = q.getName();
			return new QName(src.getNamespaceURI(),
					src.getLocalPart() + suffix,
					src.getPrefix());
		}
		
	}
	
	private final QueryBuilder builder;
	private final QName id;
	private final ParseTree adapted;
	private transient org.apache.lucene.search.Query builtObject = null;

	public ParseTreeToObjectAdapter(QueryBuilder builder_, QName id_, ParseTree adapted_) {
		this.builder = builder_;
		this.id = id_;
		this.adapted = adapted_;
	}

	@Override
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
