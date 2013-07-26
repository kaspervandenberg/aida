// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LexicalPattern;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;

/**
 * Preconstructed concepts
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum Concepts {
	METASTASIS(LexicalPatterns.METASTASIS_NL, LexicalPatterns.METASTASIS_SHORT,
			LexicalPatterns.ANY_STAGE4, LexicalPatterns.ANY_UITZAAI),
	CHEMOKUUR(LexicalPatterns.ANY_CHEMOKUUR);

	private final List<LexicalPatterns> patterns;
	private final Map<Config, Concept> instances = new HashMap<>();

	private Concepts(final LexicalPatterns... patterns_) {
		this.patterns = Arrays.asList(patterns_);
	}

	private class Impl implements Concept {
		private final QueryNode parsetree_representation;
		private final SpanQuery luceneObject_representation;
		private transient QName id = null;

		public Impl(final Iterable<LexicalPattern> pats) {
			parsetree_representation = new OrQueryNode(LexicalPatterns.containedNodes(pats));
			luceneObject_representation = new SpanOrQuery(LexicalPatterns.containedSpans(pats));
		};
		
		@Override
		public <T> T accept(Visitor<T> visitor) {
			return Config.getDefaultVisitableDelegate().accept(this, visitor);
		}

		@Override
		public QName getName() {
			if (id == null) {
				try {
					id = QNameUtil.instance().createQName_inPreconstructedNamespace(
							Concepts.this.name().toLowerCase());
				} catch (URISyntaxException ex) {
					throw new Error(ex);
				}
			}
			return id;
		}

		@Override
		public SpanQuery getRepresentation() {
			return getLuceneObject_representation();
		}

		/**
		 * @return the parsetree_representation
		 */
		@Override
		public QueryNode getParsetree_representation() {
			return parsetree_representation;
		}

		/**
		 * @return the luceneObject_representation
		 */
		@Override
		public SpanQuery getLuceneObject_representation() {
			return luceneObject_representation;
		}
	}
	
	public Concept getConcept(final Config config) {
		if(!instances.containsKey(config)) {
			Concept concept = new Impl(LexicalPatterns.toPatternIterable(config, patterns));
			instances.put(config, concept);
		}
		return instances.get(config);
	}
	
	
}
