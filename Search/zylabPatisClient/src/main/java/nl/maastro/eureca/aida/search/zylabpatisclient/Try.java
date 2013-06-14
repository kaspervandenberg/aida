/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ProximityQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.SlopQueryNode;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.nodes.MultiPhraseQueryNode;

/**
 *
 * @author kasper2
 */
public class Try {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			final String def = "content";
			SyntaxParser parser = new StandardQueryParser().getSyntaxParser();
			QueryNodeProcessor processor = new StandardQueryParser().getQueryNodeProcessor();
			
			ArrayList<QueryNode> parsed = new ArrayList<>();
			parsed.add(0, parser.parse("\"test boo\"~2", def));
			parsed.add(1, parser.parse("confusion~2", def));
			parsed.add(2, parser.parse("foo AND bar", def));
			parsed.add(3, parser.parse("optional OR keuze", def));
			
			ArrayList<QueryNode> queries = new ArrayList<>();
			queries.addAll(parsed);

			queries.add(4, new OrQueryNode(parsed));
			queries.add(5, new ProximityQueryNode(parsed, def, ProximityQueryNode.Type.NUMBER, 5, false));
			queries.add(6, new ProximityQueryNode(Arrays.asList(queries.get(3), queries.get(4)), null, ProximityQueryNode.Type.NUMBER, 8, false));
			

			
			
			for (QueryNode n : queries) {
				System.out.println(n.toString());
				System.out.println(processor.process(n).toString());
				System.out.println();
				System.out.println();
			}
		} catch (QueryNodeException ex) {
			throw new Error(ex);
		}

		
		// TODO code application logic here
	}
}
