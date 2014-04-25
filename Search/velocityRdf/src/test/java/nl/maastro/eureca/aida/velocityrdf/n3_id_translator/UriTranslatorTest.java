// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.runner.RunWith;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.openrdf.model.URI;
import org.openrdf.model.Namespace;

@RunWith(Theories.class)
public class UriTranslatorTest
		extends TranslatorTest<URI> {
	private static final Data data = new Data();

	@Nullable
	private UriTranslator testee = null;

	public UriTranslatorTest()
	{
		// intentionally left blank
	}


	@DataPoint
	public static URI prefixedNs1A()
	{
		return data.prefixedNs1A();
	}

	
	@DataPoint
	public static URI prefixedNs1B()
	{
		return data.prefixedNs1B();
	}

	
	@DataPoint
	public static URI prefixedNs2A()
	{
		return data.prefixedNs2A();
	}

	
	@DataPoint
	public static URI prefixedNs3a()
	{
		return data.prefixedNs3A();
	}

	
	@DataPoint
	public static URI fullNsA()
	{
		return data.fullNsA();
	}


	@DataPoint
	public static URI fullNsB()
	{
		return data.fullNsB();
	}


	@DataPoint
	public static URI fullOtherA()
	{
		return data.fullOtherA();
	}


	@After
	public void teardown()
	{
		testee = null;
	}


	@Override
	protected UriTranslator getTestee()
	{
		if (testee == null) {
			testee = new UriTranslator(
				data.namespaces(),
				new QNameTranslator(data.namespaces()),
				new FullUriTranslator());
		}
		return testee;
	}

}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

