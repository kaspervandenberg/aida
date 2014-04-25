// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.After;
import org.junit.experimental.theories.Theory;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import checkers.nullness.quals.Nullable;

/**
 * Test whether a {@link Translator} complies to the interface contract.
 * 
 * <p>To test whether a class complies to the {@link Translator}-interface:
 * <ol><li>Derive a class from {@code TranslatorTest}.</li>
 * 		<li>Supply an object of the class to test by implementing 
 * 			{@link #getTestee()}.</li>
 * 		<li>Either
 * 			<ul><li><ol type="a">
 * 				<li>annotate your derived {@code TranslatorTest}-class with
 * 					{@link org.junit.runner.RunWith}
 * 					{@link org.junit.experimental.theories.Theories}, and</li>
 * 				<li>supply datapoints via the 
 * 					{@link org.junit.experimental.theories.DataPoint}
 * 					annotation; or</li></ol></li>
 * 			<li><ol type="a">
 *		 		<li>mannually call the annotated {@code TranslatorTest}-methods
 * 					({@link org.junit.Before} and 
 * 					{@link org.junit.experimental.theories.Theory}), and</li>
 *		 		<li>supply any combination of parameters when calling the
 * 					{@code @Theory}-methods.</li></ol></li>
 * 			</ul></li>
 * </ol>
 * 			
 * 
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
public abstract class TranslatorTest<T> {
	private static final Logger LOG =
			LoggerFactory.getLogger(TranslatorTest.class);


	/**
	 * Derived classes must provide the object to perform these tests on.
	 * 
	 * <p>A single test (or theory) may call {@code getTestee()} more than
	 * once, during a single test {@code getTestee()} must return the same
	 * instance.  For a different test-method {@code getTestee()} must return
	 * a fresh instance.  Achieve this by setting a "testee"-field in a
	 * "test fixture setup"-method (i.e. a method annotated with 
	 * {@link org.junit.Before}).</p>
	 * 
	 * @return 	the Translator to test.
	 */
	protected abstract Translator<T> getTestee();

	@Nullable
	private Matchers<T> matchers = null;

	
	@Before
	public void setup()
	{
		Translator<T> testee = getTestee();
		assertTrue(testee != null);
		this.matchers = new Matchers(getTestee());
	}

	@After
	public void teardown()
	{
		this.matchers = null;
	}

	
	@Theory
	public final void testAnyGeneratedIsWellformed(final T val)
	{
		try {
			String id = getTestee().getId(val);
			

			LOG.debug(
					"Test: identifier {} generated for item "
					+ "{}(type {}) is well formed?",
					id, val, val.getClass());
			assertThat(String.format(
							"identifier %s generated for item "
							+ "%s (type %s) is not well formed",
							id, val, val.getClass()),
					id, matchers.isWellformedIdentifier());
		}
		catch (Exception ex) {
			assumeNoException(ex);
		}
	}


	@Theory
	public final void testGeneratedIdIsFunctional(final T val1, final T val2)
	{
		assumeThat(val1, is(val2));
		try {
			String id1 = getTestee().getId(val1);
			String id2 = getTestee().getId(val2);

			assertThat(id1, is(id2));					
		}
		catch (Exception ex) {
			assumeNoException(ex);
		}
	}

	
	@Theory
	public final void testGeneratedIdIsInjective(final T val1, final T val2)
	{
		assumeThat(val1, is(not(val2)));
		try {
			String id1 = getTestee().getId(val1);
			String id2 = getTestee().getId(val2);

			assertThat(id1, is(not(id2)));					
		}
		catch (Exception ex) {
			assumeNoException(ex);
		}
	}

	
	@Theory
	public final void testGeneratedMatches(final T val)
	{
		try {
			String id = getTestee().getId(val);
			
			assertThat(val, matchers.matchesIdentifier(id));
		}
		catch (Exception ex) {
			assumeNoException(ex);
		}
	}


	@Theory
	public final void testMatchingAnIdEquivalentEqual(final T val1, final T val2)
	{
		try {
			String id1 = getTestee().getId(val1);

			assertThat(val2, either(
					matchers.matchesIdentifier(id1)).or(
					is(not(val1))));
		}
		catch (Exception ex) {
			assumeNoException(ex);
		}
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

