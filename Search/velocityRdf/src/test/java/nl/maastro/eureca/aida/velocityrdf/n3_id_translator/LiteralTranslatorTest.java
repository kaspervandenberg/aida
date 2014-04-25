// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.openrdf.model.Literal;

@RunWith(Theories.class)
public class LiteralTranslatorTest 
		extends TranslatorTest<Literal> {
	private static final Data data = new Data();

	@Nullable
	private LiteralTranslator testee = null;


	public LiteralTranslatorTest()
	{
		// Intentionally left blank
	}


	@DataPoint
	public static Literal stringLiteral()
	{
		return data.stringLiteral();
	}


	@DataPoint
	public static Literal string4Literal()
	{
		return data.string4Literal();
	}


	@DataPoint
	public static Literal emptyLiteral()
	{
		return data.emptyLiteral();
	}


	@DataPoint
	public static Literal number4Literal()
	{
		return data.number4Literal();
	}


	@DataPoint
	public static Literal number2Literal()
	{
		return data.number2Literal();
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
			testee = new LiteralTranslator();
		}
		return testee;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

