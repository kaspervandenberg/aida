/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTreeBase;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.spans.SpanQuery;

/**
 *
 * @author kasper
 */
public interface DualRepresentationQuery extends Query  {

	/**
	 * @return the parsetree_representation
	 */
	QueryNode getParsetree_representation();

	/**
	 * @return the luceneObject_representation
	 */
	SpanQuery getLuceneObject_representation();

	/**
	 * Allows implementors of {@link DualRepresentationQuery} to forward
	 * {@link Query#accept(nl.maastro.eureca.aida.search.zylabpatisclient.query.Query.Visitor) }
	 * and present themselves as either {@link ParseTree} 
	 * ({@link Visitable#AS_PARSE_TREE}) or as {@link LuceneObject}
	 */
	static enum Visitable {
		AS_PARSE_TREE {
			/**
			 * Present {@link DualRepresentationQuery}
			 * as {@link ParseTree} to
			 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.query.Query.Visitor visitors}.
			 */
			@Override
			public <T> T accept(final DualRepresentationQuery src,
					final Query.Visitor<T> visitor) {
				return visitor.visit(new ParseTreeBase() {
					@Override
					public QueryNode getRepresentation() {
						return src.getParsetree_representation();
					}

					@Override
					public QName getName() {
						return src.getName();
					}
				});
			}
		},
		AS_LUCENE_OBJECT {
			/**
			 * Present {@link DualRepresentationQuery}
			 * as {@link LuceneObject} to 
			 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.query.Query.Visitor visitors}.
			 */
			@Override
			public <T> T accept(final DualRepresentationQuery src,
					final Query.Visitor<T> visitor) {
				return visitor.visit(new LuceneObjectBase() {
					@Override
					public org.apache.lucene.search.Query getRepresentation() {
						return src.getLuceneObject_representation();
					}

					@Override
					public QName getName() {
						return src.getName();
					}
				});
			}
		};

		/**
		 * Allow {@link DualRepresentationQuery} {@code src} to present itself
		 * as either {@link ParseTree} or {@link LuceneObject} to 
		 * {@code visitor}.
		 * 
		 * @see Query#accept(nl.maastro.eureca.aida.search.zylabpatisclient.query.Query.Visitor) 
		 * 
		 * @param <T>	type that {@code visitor} uses during the visiting of this 
		 * 		{@code Query}.
		 * @param src		the {@link DualRepresentationQuery} to which to
		 * 		forward {@link Query#getName()}, {@link ParseTree#getRepresentation()},
		 * 		and {@link LuceneObject#getRepresentation()}.
		 * @param visitor	the object to call back. 
		 * 
		 * @return the value {@code visitor.accept(…)} returned; {@code Query} 
		 * 		should treat {@code accepts} return value as opaque.
		 */
		public abstract <T> T accept(final DualRepresentationQuery src,
					final Query.Visitor<T> visitor);
	}
}
