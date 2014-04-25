// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;
import org.openrdf.model.URI;

@RunWith(Theories.class)
public class FullUriTranslatorTest
		extends TranslatorTest<URI> {
	private static final Data data = new Data();

	@DataPoint
	public static URI ns_a()
	{
		return data.fullNsA();
	}


	@DataPoint
	public static URI ns_b()
	{
		return data.fullNsB();
	}


	@DataPoint
	public static URI other_a()
	{
		return data.fullOtherA();
	}


	@Nullable
	private FullUriTranslator testee = null;


	public FullUriTranslatorTest()
	{
		// intentionally left blank
	}


	@After
	public void teardown()
	{
		this.testee = null;
	}


	@Override
	protected FullUriTranslator getTestee()
	{
		if (testee == null) {
			this.testee = new FullUriTranslator();
		}
		return testee;
	}

}


/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */


