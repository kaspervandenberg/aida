// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable;
import org.junit.Test;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

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
	public final void testAccept()
	{
		final int callCount[] = {0};
		
		@SuppressWarnings("unchecked")
		QueryVisitor<Void> mockedVisitor = (QueryVisitor<Void>)mock(QueryVisitor.class,
				new Answer<Void>() {
					@Override
					public Void answer(InvocationOnMock invocation) throws Throwable
					{
						if ((invocation.getArguments()[0] instanceof QueryExpression) &&
								invocation.getArguments()[0].equals(testee)) {
							callCount[0]++;
						}
						return null;
					}
				});
		
		
		testee.accept(mockedVisitor);

		assertThat(callCount[0], equalTo(1));
	}

	
	@Test
	public final void testSubexpressionsIsAcyclicDirectedGraph()
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


	/**
	 * {@link QueryExpression#directVariables()} ⊆ 
	 * {@link QueryExpression#variables()}.
	 * 
	 * <p>Part I of test: {@code variables()} = ({@code directVariables()}
	 * ∪ (⋃ {@code subexpression} ∈ {@link QueryExpression#subexpressions()} :
	 * {@code subexpression.variables()}).</p>
	 * 
	 * @see #testVariablesContainsAllSubexpressionVariables() 
	 * @see #testVariablesContainsOnlyAcceptableVariables() 
	 */
	@Test
	public final void testVariablesContainsAllDirectVariables()
	{
		assertThat (testee.directVariables().values(),
				everyItem(isIn(testee.variables().values())));
	}


	/**
	 * ∀ s ∈ {@link QueryExpression#subexpressions()} : {@code s.variables()}
	 * ⊆ {@link QueryExpression#variables()}.
	 * 
	 * <p>Part II of test: {@code variables()} = 
	 * ({@link QueryExpression#directVariables()} ∪ (⋃ {@code subexpression} ∈ 
	 * {@code subexpressions()} : {@code subexpression.variables()}).</p>
	 * 
	 * @see #testVariablesContainsAllDirectVariables() 
	 * @see #testVariablesContainsOnlyAcceptableVariables() 
	 */
	@Test
	public final void testVariablesContainsAllSubexpressionVariables()
	{
		for (QueryExpression subexpression : testee.subexpressions()) {
			assertThat(subexpression.variables().values(), 
					everyItem(isIn(testee.variables().values())));
		}
	}


	/**
	 * {@link QueryExpression#variables()} ⊆ 
	 * {@link QueryExpression#directVariables()} ∪ (⋃ {@code subexpression} ∈ 
	 * {@link QueryExpression#subexpressions()} : {@code 
	 * subexpression.variables()}).
	 * 
	 * <p>Part III of test: {@code variables()} = * ({@code directVariables()}
	 * ∪ (⋃ {@code subexpression} ∈ * {@code subexpressions()} : {@code 
	 * subexpression.variables()}).</p>
	 * 
	 * @see #testVariablesContainsAllDirectVariables() 
	 * @see #testVariablesContainsAllSubexpressionVariables() 
	 */
	@Test
	public final void testVariablesContainsOnlyAcceptableVariables()
	{
		Set<Variable> acceptableVariables = new HashSet<>();

		acceptableVariables.addAll(testee.directVariables().values());
		for (QueryExpression subexpression : testee.subexpressions()) {
			acceptableVariables.addAll(subexpression.variables().values());
		}

		assertThat (testee.variables().values(),
				everyItem(isIn(acceptableVariables)));
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

