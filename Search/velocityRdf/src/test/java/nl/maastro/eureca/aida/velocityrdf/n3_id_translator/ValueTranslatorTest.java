// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.openrdf.model.Value;

@RunWith(Theories.class)
public class ValueTranslatorTest 
		extends TranslatorTest<Value> {
	private static final Data data = new Data();

	@Nullable
	private ValueTranslator testee = null;


	public ValueTranslatorTest()
	{
		// Intentionally left blank
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> stringLiteral()
	{
		return data.stringLiteral();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> string4Literal()
	{
		return data.string4Literal();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> emptyLiteral()
	{
		return data.emptyLiteral();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> number4Literal()
	{
		return data.number4Literal();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> number2Literal()
	{
		return data.number2Literal();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> prefixedNs1A()
	{
		return data.prefixedNs1A();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> prefixedNs1B()
	{
		return data.prefixedNs1B();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> prefixedNs2A()
	{
		return data.prefixedNs2A();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> prefixedNs3A()
	{
		return data.prefixedNs3A();
	}

	
	@DataPoint
	public static RdfEntityContainer<? extends Value> fullNsA()
	{
		return data.fullNsA();
	}

	
	@DataPoint
	public static RdfEntityContainer<? extends Value> fullNsB()
	{
		return data.fullNsB();
	}

	
	@DataPoint
	public static RdfEntityContainer<? extends Value> fullOtherA()
	{
		return data.fullOtherA();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> annonBNode1()
	{
		return data.annonBNode1();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> annonBNode2()
	{
		return data.annonBNode2();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> namedBNode1()
	{
		return data.namedBNode1();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> namedBNode2()
	{
		return data.namedBNode2();
	}


	@DataPoint
	public static RdfEntityContainer<? extends Value> duplBNode1()
	{
		return data.duplBNode1();
	}


	@DataPoints
	public static Identifier.SyntaxError[] valueSyntaxErrors()
	{
		return Data.IdentifierSets.VALUES.syntaxErrorIds();
	}


	@After
	public void teardown()
	{
		testee = null;
	}

	@Override
	protected ValueTranslator getTestee()
	{
		if (testee == null) {
			testee = new ValueTranslator(
				new ResourceTranslator(
					new UriTranslator(
						data.namespaces(),
						new QNameTranslator(data.namespaces()),
						new FullUriTranslator()),
					new BNodeTranslator()),
				new LiteralTranslator());
		}
		return testee;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

