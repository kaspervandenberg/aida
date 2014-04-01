// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.junit.Before;
import org.junit.experimental.theories.Theory;

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
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
abstract class TranslatorTest<T> {
	private Translator<T> testee;

	protected abstract Translator<T> getTestee();

	@Before
	final public void setupTranslatorTest() {
		testee = getTestee();
	}
	
}
