// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import nl.maastro.eureca.lucenerdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class LiteralTest extends QueryExpressionTest {
	private @MonotonicNonNull Literal testee;
	
	/**
	 * @return the {@link QueryExpression} to test
	 */
	@Override
	public abstract Literal getTestee();

	@EnsuresNonNull("testee")
	@Before
	@Override
	public void setup()
	{
		super.setup();
		this.testee = getTestee();
	}


	@Test
	public final void testLiteralAccept()
	{
		@SuppressWarnings("unchecked")
		QueryVisitor<Void> mockedVisitor = (QueryVisitor<Void>)mock(QueryVisitor.class);

		testee.accept(mockedVisitor);

		verify(mockedVisitor).visitLiteral(eq(testee));
	}
	

	@Test
	public final void testSubexpressionsEmpty()
	{
		assertThat (testee.subexpressions(),
				emptyIterableOf(QueryExpression.class));
	}


	@Test
	public final void testVariablesEmpty()
	{
		assertThat (testee.variables().values(),
				emptyCollectionOf(Variable.class));
		assertThat (testee.variables().keySet(),
				emptyCollectionOf(Identifier.class));
	}


	@Test
	public final void testDirectVariablesEmpty()
	{
		assertThat (testee.directVariables().values(),
				emptyCollectionOf(Variable.class));
		assertThat (testee.directVariables().keySet(),
				emptyCollectionOf(Identifier.class));
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */
