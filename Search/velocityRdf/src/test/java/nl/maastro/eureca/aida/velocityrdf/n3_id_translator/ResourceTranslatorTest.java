// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.runner.RunWith;
import org.openrdf.model.Resource;

@RunWith(Theories.class)
public class ResourceTranslatorTest extends TranslatorTest<Resource> {
	private static final Data data = new Data();

	@Nullable
	private ResourceTranslator testee = null;

	public ResourceTranslatorTest()
	{
		// intentionally left blank
	}

	@DataPoint
	public static RdfEntityContainer<? extends Resource> prefixedNs1A()
	{
		return data.prefixedNs1A();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> prefixedNs1B()
	{
		return data.prefixedNs1B();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> prefixedNs2A()
	{
		return data.prefixedNs2A();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> prefixedNs3A()
	{
		return data.prefixedNs3A();
	}

	
	@DataPoint
	public static RdfEntityContainer<? extends Resource> fullNsA()
	{
		return data.fullNsA();
	}

	
	@DataPoint
	public static RdfEntityContainer<? extends Resource> fullNsB()
	{
		return data.fullNsB();
	}

	
	@DataPoint
	public static RdfEntityContainer<? extends Resource> fullOtherA()
	{
		return data.fullOtherA();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> annonBNode1()
	{
		return data.annonBNode1();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> annonBNode2()
	{
		return data.annonBNode2();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> namedBNode1()
	{
		return data.namedBNode1();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> namedBNode2()
	{
		return data.namedBNode2();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Resource> duplBNode1()
	{
		return data.duplBNode1();
	}


	@DataPoints
	public static Identifier.SyntaxError[] resourceSyntaxErrors()
	{
		return Data.IdentifierSets.RESOURCES.syntaxErrorIds();
	}


	@After
	public void teardown()
	{
		testee = null;
	}


	@Override
	protected ResourceTranslator getTestee()
	{
		if (testee == null) {
			testee = new ResourceTranslator(
				new UriTranslator(
					data.namespaces(),
					new QNameTranslator(data.namespaces()),
					new FullUriTranslator()),
				new BNodeTranslator());
		}
		return testee;
	}
}


/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

