// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.Nullable;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.openrdf.model.Statement;
import java.util.Set;

@RunWith(Theories.class)
public class StatementTranslatorTest 
		extends TranslatorTest<Statement> {
	private static final Data data = new Data();

	@Nullable
	private StatementTranslator testee = null;


	public StatementTranslatorTest()
	{
		// Intentionally left blank
	}

	
	@DataPoints
	public static Statement[] selectedStatements()
	{
		Set<Statement> set_statements = data.statementSelection();
		Statement[] arr_statements = new Statement[set_statements.size()];
		return set_statements.toArray(arr_statements);
	}


	@Before
	public void setup()
	{
		UriTranslator uriTrans = 
				new UriTranslator(
					data.namespaces(),
					new QNameTranslator(data.namespaces()),
					new FullUriTranslator());

		ResourceTranslator resourceTrans =
				new ResourceTranslator(
					uriTrans,
					new BNodeTranslator());

		ValueTranslator valueTrans =
				new ValueTranslator(
					resourceTrans,
					new LiteralTranslator());

		testee = new StatementTranslator(
				resourceTrans,
				uriTrans,
				valueTrans);
	}


	@After
	public void teardown()
	{
		testee = null;
	}

	@Override
	protected StatementTranslator getTestee()
	{
		if (testee == null) {
			throw new IllegalStateException(
					"Call setup(), before calling getTestee()");
		}
		return testee;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

