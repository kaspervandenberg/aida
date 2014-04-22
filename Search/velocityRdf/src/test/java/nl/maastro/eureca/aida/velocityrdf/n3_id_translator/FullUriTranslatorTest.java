// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

@RunWith(Theories.class)
public class FullUriTranslatorTest
		extends TranslatorTest<URI> {
	private static final ValueFactory VALUE_FACTORY =
			new ValueFactoryImpl();

	@DataPoint
	public static final URI ns_a = VALUE_FACTORY.createURI(
			"http://test.dummy.org/namespace/a");

	@DataPoint
	public static final URI ns_b = VALUE_FACTORY.createURI(
			"http://test.dummy.org/namespace/b");

	@DataPoint
	public static final URI other_a = VALUE_FACTORY.createURI(
			"http://other.org/namespace/a");

	@Nullable
	private FullUriTranslator testee = null;


	public FullUriTranslatorTest()
	{
		// intentionally left blank
	}


	@Before
	public void setup()
	{
		this.testee = new FullUriTranslator();
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
			throw new IllegalStateException(
					"Call setup(), before calling getTestee()");
		}
		return testee;
	}

}


/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */


