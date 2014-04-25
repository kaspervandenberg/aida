// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * {@link TranslatorTest} uses {@code Identifier}s to test {@link Translator}s.
 *
 * The nested classes form a classification of {@code Identifier}s according to
 * whether they {@link WellFormed adhere to the syntax} or {@link SyntaxError
 * not}.
 *
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
class Identifier {
	public static class WellFormed extends Identifier
	{
		public WellFormed(String value_)
		{
			super(value_);
		}
	}


	public static class SyntaxError extends Identifier
	{
		public SyntaxError(String value_)
		{
			super(value_);
		}
	}

	private final String value;

	public Identifier(String value_)
	{
		this.value = value_;
	}


	public String getValue()
	{
		return this.value;
	}


	public WellFormed castToWellFormed()
	{
		return new WellFormed(value);
	}


	public SyntaxError castToSyntaxError()
	{
		return new SyntaxError(value);
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */


