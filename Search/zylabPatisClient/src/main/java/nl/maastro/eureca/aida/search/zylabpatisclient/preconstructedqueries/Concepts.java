// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import checkers.nullness.quals.NonNull;
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
	private final Map<Config, /*@NonNull*/Concept> instances = new HashMap<>();

	private Concepts(final LexicalPatterns... patterns_) {
		this.patterns = Arrays.asList(patterns_);
	}

	private class Impl implements Concept {
		private final QueryNode parsetreeRepresentation;
		private final SpanQuery luceneObjectRepresentation;
		private transient @MonotonicNonNull QName id = null;

		public Impl(final Iterable<LexicalPattern> pats) {
			parsetreeRepresentation = new OrQueryNode(LexicalPatterns.containedNodes(pats));
			luceneObjectRepresentation = new SpanOrQuery(LexicalPatterns.containedSpans(pats));
		};
		
		@Override
		public <T> T accept(Visitor<T> visitor) {
			return Config.getDefaultVisitableDelegate().accept(this, visitor);
		}

		@EnsuresNonNull("id")
		@Override
		public QName getName() {
			if (id == null) {
				try {
					id = QNameUtil.instance().createQNameInPreconstructedNamespace(
							Concepts.this.name().toLowerCase());
				} catch (URISyntaxException ex) {
					throw new Error(ex);
				}
			}
			return id;
		}

		@Override
		public SpanQuery getRepresentation() {
			return getLuceneObjectRepresentation();
		}

		/**
		 * @return the parsetreeRepresentation
		 */
		@Override
		public QueryNode getParsetreeRepresentation() {
			return parsetreeRepresentation;
		}

		/**
		 * @return the luceneObjectRepresentation
		 */
		@Override
		public SpanQuery getLuceneObjectRepresentation() {
			return luceneObjectRepresentation;
		}
	}
	
	public Concept getConcept(final Config config) {
		if(!instances.containsKey(config)) {
			Concept concept = new Impl(LexicalPatterns.toPatternIterable(config, patterns));
			instances.put(config, concept);
		}
		@SuppressWarnings("nullness")
		@NonNull Concept result = instances.get(config);
		
		return result;
	}
	
	
}
