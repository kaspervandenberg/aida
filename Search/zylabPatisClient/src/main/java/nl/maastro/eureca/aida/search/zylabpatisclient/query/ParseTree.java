// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

/**
 * Represent a query as a Lucene {@link org.apache.lucene.queryparser.flexible.core.nodes.QueryNode}.
 * Text can be parsed to query nodes; 
 * {@link org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor}s
 * can perform operations on {@code QueryNodes} to change them into other parse
 * trees; and {@link org.apache.lucene.search.Query}s can be built which Lucene
 * searchers can execute.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface ParseTree extends Query {

	/**
	 * @return the {@link org.apache.lucene.queryparser.flexible.core.nodes.QueryNode}
	 * 		that represents this {@code Query}.
	 */
	QueryNode getRepresentation();
	
}
