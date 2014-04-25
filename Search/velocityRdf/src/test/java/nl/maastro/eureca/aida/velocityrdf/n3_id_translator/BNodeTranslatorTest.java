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
	private final static Data data = new Data();

	@DataPoint
	public static BNode annonBNode1()
	{
		return data.annonBNode1();
	}

	@DataPoint
	public static BNode annonBNode2()
	{
		return data.annonBNode2();
	}

	@DataPoint
	public static BNode namedBNode1()
	{
		return data.namedBNode1();
	}

	@DataPoint
	public static BNode namedBNode2()
	{
		return data.namedBNode2();
	}

	@DataPoint
	public static BNode duplBNode1()
	{
		return data.duplBNode1();
	}

	@Nullable 
	private BNodeTranslator testee = null;

	public BNodeTranslatorTest()
	{
		// intentionally left blank
	}

	public void teardown()
	{
		testee = null;
	}

	@Override
	protected BNodeTranslator getTestee()
	{
		if (testee == null) {
			testee = new BNodeTranslator();
		}
		return testee;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

