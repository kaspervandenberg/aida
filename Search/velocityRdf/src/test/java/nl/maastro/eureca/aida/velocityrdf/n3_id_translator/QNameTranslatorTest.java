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
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import static java.util.Arrays.asList;


@RunWith(Theories.class)
public class QNameTranslatorTest
		extends TranslatorTest<URI> {
	private static final ValueFactory VALUE_FACTORY =
			new ValueFactoryImpl();

	private static final String PREFIXES[] = {
			"ns1", "ns2", "ns3"};
	private static final String NAMESPACES[] = {
			"http://test.dummy.org/namespace/",
			"http://test.dummy.org/",
			"http://test.dummy.org/file.html#"};

	private static final NamespaceContainer namespaces = 
			new NamespaceContainer(asList(
					((Namespace) new NamespaceImpl(PREFIXES[0], NAMESPACES[0])),
					((Namespace) new NamespaceImpl(PREFIXES[1], NAMESPACES[1])),
					((Namespace) new NamespaceImpl(PREFIXES[2], NAMESPACES[2]))));

	@DataPoint
	public static final URI ns1_a = VALUE_FACTORY.createURI(NAMESPACES[0], "a");
	
	@DataPoint
	public static final URI ns1_b = VALUE_FACTORY.createURI(NAMESPACES[0], "b");
	
	@DataPoint
	public static final URI ns2_a = VALUE_FACTORY.createURI(NAMESPACES[1], "a");
	
	@DataPoint
	public static final URI ns3_a = VALUE_FACTORY.createURI(NAMESPACES[2], "a");
	

	@Nullable
	private QNameTranslator testee = null;


	public QNameTranslatorTest()
	{
		// Intentionally left blank
	}
	

	@Before
	public void setup()
	{
		testee = new QNameTranslator(namespaces);
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

