/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tryout;

import java.util.ArrayList;
import java.util.Arrays;
import nl.maastro.eureca.aida.search.zylabpatisclient.QueryDisplay;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.ProximityQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;

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
			QueryBuilder builder = new StandardQueryParser().getQueryBuilder();
			
			QueryNode q = new ProximityQueryNode(Arrays.<QueryNode>asList(
					new org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode(def, "foo bar", 0, "foo bar".length())),
					def, ProximityQueryNode.Type.NUMBER, 3, false);

			
			ArrayList<QueryNode> parsed = new ArrayList<>();
//			parsed.add(parser.parse("\"test boo\"~2", def));
//			parsed.add(parser.parse("confusion~2", def));
//			parsed.add(parser.parse("foo AND bar", def));
//			parsed.add(parser.parse("(optional OR keuze) AND (field OR metastase)", def));
			parsed.add(parser.parse("(\"letter comes\"~3 \"agitated employees\"~4 \"worried about salaries\"~1)" +
" AND (\"letter comes agitated employees worried about salaries\"~8)", def));
			parsed.add(parser.parse("\"d.d\"", def));
			
			ArrayList<QueryNode> queries = new ArrayList<>();
			queries.addAll(parsed);
//			queries.add(q);

//			queries.add(new OrQueryNode(parsed));
//			queries.add(new ProximityQueryNode(parsed, def, ProximityQueryNode.Type.NUMBER, 5, false));
//			queries.add(new ProximityQueryNode(Arrays.asList(queries.get(3), queries.get(4)), null, ProximityQueryNode.Type.NUMBER, 8, false));
			

			
			
			for (QueryNode n : queries) {
				System.out.println(n.toString());
				System.out.println();
				System.out.println(processor.process(n).toString());
				System.out.println();
				System.out.println(QueryDisplay.instance().dumpQuery("", (Query) builder.build(n)));
				System.out.println();
				System.out.println(QueryDisplay.instance().dumpQuery("", (Query) builder.build(processor.process(n))));
				System.out.println();
				System.out.println();
				System.out.println();
			}
		} catch (QueryNodeException ex) {
			throw new Error(ex);
		}

		
		// TODO code application logic here
	}
}
