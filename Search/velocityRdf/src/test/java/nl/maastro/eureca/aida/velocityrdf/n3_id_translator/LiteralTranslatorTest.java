// © Maastro, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;

@RunWith(Theories.class)
public class LiteralTranslatorTest 
		extends TranslatorTest<Literal> {
	/*>>>@Nullable*/
	private LiteralTranslator testee = null;


	public LiteralTranslatorTest()
	{
		// Intentionally left blank
	}


	@DataPoint
	public static Literal stringLiteral = new LiteralImpl("lit1");

	@DataPoint
	public static Literal string4Literal = new LiteralImpl("4");

	@DataPoint
	public static Literal emptyLiteral = new LiteralImpl("");

	@DataPoint
	public static Literal number4Literal = new NumericLiteralImpl(4);

	@DataPoint
	public static Literal number2Literal = new NumericLiteralImpl(2);


	@Before
	public void setup()
	{
		testee = new LiteralTranslator();
	}


	@After
	public void teardown()
	{
		testee = null;
	}

	@Override
	protected LiteralTranslator getTestee()
	{
		if (testee == null) {
			throw new IllegalStateException(
					"Call setup(), before calling getTestee()");
		}
		return testee;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

