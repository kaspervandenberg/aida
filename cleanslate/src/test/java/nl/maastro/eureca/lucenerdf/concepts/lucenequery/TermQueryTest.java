// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import org.junit.Before;
import org.junit.Test;
import org.hamcrest.Matchers;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class TermQueryTest extends QueryExpressionTest {
	private @MonotonicNonNull TermQuery testee;

	@Override
	public abstract TermQuery getTestee();

	@EnsuresNonNull("testee")
	@Before
	@Override
	public void setup()
	{
		super.setup();
		this.testee = getTestee();
	}


	@Test
	public final void testTermQueryAccept()
	{
		@SuppressWarnings("unchecked")
		QueryVisitor<Void> mockedVisitor = (QueryVisitor<Void>)mock(QueryVisitor.class);

		testee.accept(mockedVisitor);

		verify(mockedVisitor).visitTerm(eq(testee));
	}

	@Test
	public final void testExactly2subexpressions()
	{
		assertThat ((Iterable<TokenExpression>)testee.subexpressions(),
				Matchers.<TokenExpression>iterableWithSize(2));
	}


	@Test
	public final void testSubexpressionSequence()
	{
		assertThat ((Iterable<TokenExpression>)testee.subexpressions(),
				contains(testee.getField(), testee.getToken()));
	}

}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */


