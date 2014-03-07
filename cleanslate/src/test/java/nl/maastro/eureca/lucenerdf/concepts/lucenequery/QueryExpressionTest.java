// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Tests whether a concrete {@link QueryExpression} follows the contract that
 * the {@code QueryExpression} interface specifies.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class QueryExpressionTest {
	private @MonotonicNonNull QueryExpression testee;
	
	/**
	 * @return the {@link QueryExpression} to test
	 */
	public abstract QueryExpression getTestee();

	@EnsuresNonNull("testee")
	@Before
	public void setup()
	{
		this.testee = getTestee();
	}

	@Test
	public void testSubexpressionsIsAcyclicDirectedGraph()
	{
		class NodeForbiddenNodesPair {
			private final QueryExpression node;
			private final Set<QueryExpression> forbiddenNodes;

			public NodeForbiddenNodesPair(QueryExpression node_, Set<QueryExpression> forbiddenNodes_)
			{
				this.node = node_;
				this.forbiddenNodes = forbiddenNodes_;
			}

			public void check()
			{
				assertThat (node, not(isIn(forbiddenNodes)));
			}

			public void queueChildNodes(Queue<NodeForbiddenNodesPair> target)
			{
				final Set<QueryExpression> childrenForbiddenNodes = new HashSet<>(forbiddenNodes);
				childrenForbiddenNodes.add(node);
				

				for (QueryExpression child : node.subexpressions()) {
					NodeForbiddenNodesPair childConditions = 
							new NodeForbiddenNodesPair(child, childrenForbiddenNodes);
					target.add(childConditions);
				}
			}
		}

		final Queue<NodeForbiddenNodesPair> nodesToCheck = new LinkedList<>();
		nodesToCheck.add(new NodeForbiddenNodesPair(testee, Collections.<QueryExpression>emptySet()));

		while (!nodesToCheck.isEmpty()) {
			NodeForbiddenNodesPair entry = nodesToCheck.remove();
			entry.check();
			entry.queueChildNodes(nodesToCheck);
		}
	}


	@Test
	public void testVariablesContainsAllDirectVariables()
	{
		assertThat (testee.variables().values(),
				containsInAnyOrder(testee.directVariables().values().toArray()));
	}


	@Test
	public void testVariablesContainsAllSubexpressionVariables()
	{
		Set<QueryExpression> subexpressionVariables =
				new HashSet<>();
		for (QueryExpression subexpression : testee.subexpressions()) {
			subexpressionVariables.addAll(subexpression.variables().values());
		}

		assertThat (testee.variables().values(),
				containsInAnyOrder(subexpressionVariables.toArray()));
	}


	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent : */

