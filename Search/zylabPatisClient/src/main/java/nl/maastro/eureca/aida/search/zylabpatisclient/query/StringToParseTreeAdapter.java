// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import java.util.Objects;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;

/**
 * Adapts a {@link StringQuery} to a {@link ParseTree} by parsing the string.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class StringToParseTreeAdapter extends ParseTreeBase implements Query {
	public static class Builder implements QueryAdapterBuilder<StringQuery, ParseTree>{
		private static final SyntaxParser defaultParser;
		static {
			StandardQueryParser luceneDefault = new StandardQueryParser();
			defaultParser = luceneDefault.getSyntaxParser();
		}

		private static final String defaultDefaultField = "content";
		private static final String defaultSuffix = ".parseTree";
		
		private SyntaxParser parser;
		private String defaultField;
		private String suffix;

		public Builder() {
			parser = defaultParser;
			defaultField = defaultDefaultField;
			suffix = defaultSuffix;
		}

		public Builder parser(SyntaxParser parser_) {
			parser = parser_;
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

		public Builder defaultField(String field_) {
			defaultField = field_;
			return this;
		}

		@Override
		public StringToParseTreeAdapter adapt(StringQuery adapted_) {
			Objects.requireNonNull(parser, "set default parser before calling adapt(…).");
			Objects.requireNonNull(defaultField, "Set default field before calling adapt(…).");
			Objects.requireNonNull(suffix, "Set default suffix before calling adapt(…).");
			
			return new StringToParseTreeAdapter(parser, appendDefaultSuffix(adapted_), adapted_, defaultField);
		}

		private QName appendDefaultSuffix(Query q) {
			return QNameUtil.instance().append(q.getName(), suffix);
		}
	}
	
	private final SyntaxParser parser;
	private final QName id;
	private final StringQuery adapted;
	private final String defaultField;
	private transient QueryNode parsedQuery = null;

	public StringToParseTreeAdapter(
			SyntaxParser parser_, QName id_, StringQuery adapted_, String defaultField_) {
		this.parser = parser_;
		this.adapted = adapted_;
		this.id = id_;
		this.defaultField = defaultField_;
	}
	
	@Override
	public QueryNode getRepresentation() {
		if(parsedQuery == null) {
			try {
				parsedQuery = parser.parse(adapted.getRepresentation(), defaultField);
			} catch (QueryNodeParseException ex) {
				throw new Error(ex);
			}
		}
		return parsedQuery;
	}

	@Override
	public QName getName() {
		return id;
	}
}
