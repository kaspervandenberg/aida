// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.openrdf.model.BNode;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;


@RunWith(Theories.class)
public class BNodeTranslatorTest extends TranslatorTest<BNode> {
	private final static ValueFactory VALUE_FACTORY = new ValueFactoryImpl();

	@DataPoint
	public final static BNode annonBNode1 = VALUE_FACTORY.createBNode();

	@DataPoint
	public final static BNode annonBNode2 = VALUE_FACTORY.createBNode();

	@DataPoint
	public final static BNode namedBNode1 = VALUE_FACTORY.createBNode("n1");

	@DataPoint
	public final static BNode namedBNode2 = VALUE_FACTORY.createBNode("a");

	@DataPoint
	public final static BNode duplBNode1 = VALUE_FACTORY.createBNode("n1");

	@Nullable 
	private BNodeTranslator testee = null;

	public BNodeTranslatorTest()
	{
		// intentionally left blank
	}

	@Before
	public void setup()
	{
		testee = new BNodeTranslator();
	}

	@After
	public void teardown()
	{
		testee = null;
	}

	@Override
	protected BNodeTranslator getTestee()
	{
		if (testee == null) {
			throw new IllegalStateException(
					"Call setup(), before calling getTestee()");
		}
		return testee;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

