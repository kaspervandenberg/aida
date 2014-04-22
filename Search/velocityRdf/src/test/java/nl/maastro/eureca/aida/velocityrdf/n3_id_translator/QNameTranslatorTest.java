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
import static java.util.Arrays.asList;


@RunWith(Theories.class)
public class QNameTranslatorTest
		extends TranslatorTest<URI> {
	private static final Data data = new Data();


	@Nullable
	private QNameTranslator testee = null;


	public QNameTranslatorTest()
	{
		// Intentionally left blank
	}
	

	@DataPoint
	public static URI ns1_a()
	{
		return data.prefixedNs1A();
	}

	
	@DataPoint
	public static URI ns1_b()
	{
		return data.prefixedNs1B();
	}

	
	@DataPoint
	public static URI ns2_a()
	{
		return data.prefixedNs2A();
	}

	
	@DataPoint
	public static URI ns3_a()
	{
		return data.prefixedNs3A();
	}

	
	@Before
	public void setup()
	{
		testee = new QNameTranslator(data.namespaces());
	}


	@After
	public void teardown()
	{
		testee = null;
	}


	@Override
	protected QNameTranslator getTestee()
	{
		if (testee == null) {
			throw new IllegalStateException(
					"Call setup(), before calling getTestee()");
		}
		return testee;
	}
}


/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

